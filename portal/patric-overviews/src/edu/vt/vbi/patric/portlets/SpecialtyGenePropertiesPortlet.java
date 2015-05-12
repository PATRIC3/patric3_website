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

import edu.vt.vbi.patric.beans.SpecialtyGene;
import edu.vt.vbi.patric.common.DataApiHandler;
import edu.vt.vbi.patric.common.SolrCore;
import org.apache.solr.client.solrj.SolrQuery;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class SpecialtyGenePropertiesPortlet extends GenericPortlet {

	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SpecialtyGenePropertiesPortlet.class);

	ObjectReader jsonReader;

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

			DataApiHandler dataApi = new DataApiHandler(request);

			SolrQuery query = new SolrQuery("feature_id:" + cId);
			query.setFields("evidence,property,source,source_id,organism,pmid,subject_coverage,query_coverage,identity,e_value");
			query.addSort("evidence", SolrQuery.ORDER.desc);
			query.addSort("property", SolrQuery.ORDER.asc);
			query.addSort("source", SolrQuery.ORDER.asc);

			String apiResponse = dataApi.solrQuery(SolrCore.SPECIALTY_GENE_MAPPING, query);
			Map resp = jsonReader.readValue(apiResponse);
			Map respBody = (Map) resp.get("response");

			List<SpecialtyGene> listSpecialtyGenes = dataApi.bindDocuments((List<Map>) respBody.get("docs"), SpecialtyGene.class);

			if (!listSpecialtyGenes.isEmpty()) {
				request.setAttribute("listSpecialtyGenes", listSpecialtyGenes);

				PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/overview/specialty_gene_properties.jsp");
				prd.include(request, response);
			}
			else {
				PrintWriter writer = response.getWriter();
				writer.write("<!-- no specialty gene property found -->");
				writer.close();
			}
		}
		else {
			PrintWriter writer = response.getWriter();
			writer.write("<p>Invalid Parameter - missing context information</p>");
			writer.close();
		}
	}
}
