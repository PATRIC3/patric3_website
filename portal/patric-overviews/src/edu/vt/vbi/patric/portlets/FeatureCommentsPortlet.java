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

import edu.vt.vbi.patric.beans.GenomeFeature;
import edu.vt.vbi.patric.common.DataApiHandler;
import edu.vt.vbi.patric.common.SolrCore;
import org.apache.solr.client.solrj.SolrQuery;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FeatureCommentsPortlet extends GenericPortlet {

	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(FeatureCommentsPortlet.class);

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

		if (cType != null && cId != null && cType.equals("feature")) {

			List<Map> listAnnotation = new LinkedList<>();
			DataApiHandler dataApi = new DataApiHandler(request);

			GenomeFeature feature = dataApi.getPATRICFeature(cId);

			if (feature != null) {

				if (feature.hasRefseqLocusTag()) {
					SolrQuery query = new SolrQuery("refseq_locus_tag:" + feature.getRefseqLocusTag());
					query.addFilterQuery("!property:Interaction");
					query.addSort("property", SolrQuery.ORDER.asc).addSort("evidence_code", SolrQuery.ORDER.asc);
					query.setRows(dataApi.MAX_ROWS);

					LOGGER.debug("[{}]: {}", SolrCore.STRUCTURED_ASSERTION.getSolrCoreName(), query);
					String apiResponse = dataApi.solrQuery(SolrCore.STRUCTURED_ASSERTION, query);

					Map resp = jsonReader.readValue(apiResponse);
					Map respBody = (Map) resp.get("response");

					listAnnotation.addAll((List<Map>) respBody.get("docs"));


					query.setFilterQueries("property:Interaction");
					query.setSort("value", SolrQuery.ORDER.asc).addSort("evidence_code", SolrQuery.ORDER.asc);

					LOGGER.debug("[{}]: {}", SolrCore.STRUCTURED_ASSERTION.getSolrCoreName(), query);
					apiResponse = dataApi.solrQuery(SolrCore.STRUCTURED_ASSERTION, query);

					resp = jsonReader.readValue(apiResponse);
					respBody = (Map) resp.get("response");

					listAnnotation.addAll((List<Map>) respBody.get("docs"));
				}

				if (!listAnnotation.isEmpty()) {

					request.setAttribute("listAnnotation", listAnnotation);

					PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/overview/feature_comments.jsp");
					prd.include(request, response);
				}
				else {
					PrintWriter writer = response.getWriter();
					writer.write("<!-- no feature comment found -->");
					writer.close();
				}
			}
		}
		else {
			PrintWriter writer = response.getWriter();
			writer.write("<p>Invalid Parameter - missing context information</p>");
			writer.close();
		}
	}
}
