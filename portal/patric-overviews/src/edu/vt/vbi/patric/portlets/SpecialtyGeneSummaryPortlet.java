/*******************************************************************************
 * Copyright 2014 Virginia Polytechnic Institute and State University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package edu.vt.vbi.patric.portlets;

import edu.vt.vbi.patric.common.SolrCore;
import edu.vt.vbi.patric.common.SolrInterface;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.PivotField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SpecialtyGeneSummaryPortlet extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpecialtyGeneSummaryPortlet.class);

	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		response.setContentType("text/html");
		String cType = request.getParameter("context_type");
		String cId = request.getParameter("context_id");

		// TODO: implement redirection for p2_genome_id to new genome_id

		if (cType != null && cId != null) {
			PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/overview/specialty_gene_summary_init.jsp");
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

		if (contextType != null && contextId != null) {

			Map<String, Map<String, Integer>> summary = new LinkedHashMap<>();

			try {
				SolrInterface solr = new SolrInterface();

				SolrQuery query = new SolrQuery("*:*");
				if (contextType.equals("taxon")) {
					query.setFilterQueries(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + contextId));
				}
				else { // genome
					query.setFilterQueries("genome_id:" + contextId);
				}
				query.setRows(0).setFacet(true).setFacetMinCount(1).addFacetPivotField("property,source").setFacetSort(FacetParams.FACET_SORT_INDEX);

				QueryResponse qr = solr.getSolrServer(SolrCore.SPECIALTY_GENE_MAPPING).query(query);
				NamedList<List<PivotField>> pivotFacetList = qr.getFacetPivot();

				for (Map.Entry<String, List<PivotField>> pivotFacet : pivotFacetList) {

					List<PivotField> pivotFields = pivotFacet.getValue();

					for (PivotField pivotField : pivotFields) {
						Map<String, Integer> entry = new LinkedHashMap<>();

						String property = pivotField.getValue().toString();
						List<PivotField> pivots = pivotField.getPivot();
						for (PivotField p : pivots) {
							entry.put(p.getValue().toString(), p.getCount());
						}

						summary.put(property, entry);
					}
				}

			}
			catch (MalformedURLException | SolrServerException e) {
				LOGGER.error(e.getMessage(), e);
			}

			request.setAttribute("summary", summary);

			response.setContentType("text/html");
			PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/overview/specialty_gene_summary.jsp");
			prd.include(request, response);
		}
	}
}
