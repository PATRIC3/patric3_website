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

import edu.vt.vbi.patric.common.SolrCore;
import edu.vt.vbi.patric.common.SolrInterface;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpecialtyGenePropertiesPortlet extends GenericPortlet {

	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SpecialtyGenePropertiesPortlet.class);

	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		response.setContentType("text/html");
		String cType = request.getParameter("context_type");
		String cId = request.getParameter("context_id");

		if (cType != null && cId != null && cType.equals("feature")) {

			List<Map<String, Object>> listSpecialtyGenes = null;

			try {
				SolrInterface solr = new SolrInterface();

				SolrQuery query = new SolrQuery("feature_id:" + cId);
				query.setFields("evidence,property,source,source_id,organism,pmid,subject_coverage,query_coverage,identity,e_value");
				query.addSort("evidence", SolrQuery.ORDER.desc);
				query.addSort("property", SolrQuery.ORDER.asc);
				query.addSort("source", SolrQuery.ORDER.asc);

				QueryResponse qr = solr.getSolrServer(SolrCore.SPECIALTY_GENE_MAPPING).query(query);
				SolrDocumentList sdl = qr.getResults();

				if (sdl.getNumFound() > 0) {
					listSpecialtyGenes = new ArrayList<>();
				}

				for (SolrDocument doc : sdl) {
					Map<String, Object> gene = new HashMap<>();

					gene.put("evidence", doc.get("evidence"));
					gene.put("property", doc.get("property"));
					gene.put("source", doc.get("source"));
					gene.put("sourceId", doc.get("source_id"));
					gene.put("organism", doc.get("organism"));
					gene.put("pmid", doc.get("pmid"));
					gene.put("subjectCoverage", doc.get("subject_coverage"));
					gene.put("queryCoverage", doc.get("query_coverage"));
					gene.put("identity", doc.get("identity"));
					gene.put("eValue", doc.get("e_value"));

					listSpecialtyGenes.add(gene);
				}

			}
			catch (MalformedURLException | SolrServerException e) {
				LOGGER.error(e.getMessage(), e);
			}

			if (listSpecialtyGenes != null) {
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
