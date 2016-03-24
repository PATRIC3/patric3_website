/**
 * ****************************************************************************
 * Copyright 2014 Virginia Polytechnic Institute and State University
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ****************************************************************************
 */
package edu.vt.vbi.patric.portlets;

import edu.vt.vbi.patric.beans.Genome;
import edu.vt.vbi.patric.common.DataApiHandler;
import edu.vt.vbi.patric.common.SiteHelper;
import edu.vt.vbi.patric.common.SolrCore;
import org.apache.solr.client.solrj.SolrQuery;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SequenceSummaryPortlet extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(SequenceSummaryPortlet.class);

	private ObjectReader jsonReader;

	@Override
	public void init() throws PortletException {
		super.init();

		ObjectMapper objectMapper = new ObjectMapper();
		jsonReader = objectMapper.reader(Map.class);
	}

	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		response.setContentType("text/html");
		String cType = request.getParameter("context_type");
		String cId = request.getParameter("context_id");

		if (cType != null && cType.equals("genome")) {
			SiteHelper.setHtmlMetaElements(request, response, "Genome Overview");
		}

		PortletRequestDispatcher prd;

		if (cType != null && cId != null) {
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/overview/sequence_summary_init.jsp");
			prd.include(request, response);
		}
		else {
			PrintWriter writer = response.getWriter();
			writer.write("<p>Invalid Parameter - missing context information</p>");
			writer.close();
		}
	}

	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		String contextType = request.getParameter("context_type");
		String contextId = request.getParameter("context_id");
		String genomeFilter = request.getParameter("genome_filter");

		DataApiHandler dataApi = new DataApiHandler(request);

		if (contextType != null && contextId != null) {

			if (contextType.equals("genome")) {

				Genome genome = dataApi.getGenome(contextId);

				if (genome != null) {

					// check genome_amr data is available.
					SolrQuery query = new SolrQuery("genome_id:" + genome.getId());
					dataApi = new DataApiHandler(request);
					String apiResponse = dataApi.solrQuery(SolrCore.GENOME_AMR, query);

					Map resp = jsonReader.readValue(apiResponse);
					Map respBody = (Map) resp.get("response");

					List<Map> amr = (List<Map>) respBody.get("docs");

					request.setAttribute("genome", genome);
					request.setAttribute("amr", amr);

					response.setContentType("text/html");
					PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/overview/sequence_summary.jsp");
					prd.include(request, response);
				}
			}
			else {
				// taxon level

				List<String> annotations = Arrays.asList("PATRIC", "RefSeq");

				for (String annotation : annotations) {

					String queryParam;

					switch (annotation) {
					case "PATRIC":
						queryParam = "patric_cds:[1 TO *]";
						break;
					case "RefSeq":
						queryParam = "refseq_cds:[1 TO *]";
						break;
					default:
						queryParam = "*:*";
						break;
					}

					if (genomeFilter != null && !genomeFilter.equals("")) {
						queryParam += " AND (" + genomeFilter + ")";
					}

					Map counts = dataApi.getFieldFacets(SolrCore.GENOME, queryParam, "taxon_lineage_ids:" + contextId, "genome_status");

					request.setAttribute(annotation, counts);
				}

				response.setContentType("text/html");
				PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/overview/sequence_summary.jsp");
				prd.include(request, response);
			}
		}
	}
}
