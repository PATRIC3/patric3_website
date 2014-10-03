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
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

public class ProteinFeatureSummaryPortlet extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProteinFeatureSummaryPortlet.class);

	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException, UnavailableException {

		response.setContentType("text/html");
		String cType = request.getParameter("context_type");
		String cId = request.getParameter("context_id");

		// TODO: redirect p2_genome_id to new genome_id

		if (cType != null && cId != null) {
			PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/overview/protein_feature_summary_init.jsp");
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

			Map<String, Long> hypotheticalProteins = new HashMap<>();
			Map<String, Long> functionalProteins = new HashMap<>();
			Map<String, Long> ecAssignedProteins = new HashMap<>();
			Map<String, Long> goAssignedProteins = new HashMap<>();
			Map<String, Long> pathwayAssignedProteins = new HashMap<>();
			Map<String, Long> figfamAssignedProteins = new HashMap<>();

			try {
				SolrInterface solr = new SolrInterface();
				solr.setCurrentInstance(SolrCore.FEATURE);

				// set default params
				SolrQuery query = new SolrQuery();
				if (contextType.equals("taxon")) {
					query.setFilterQueries(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + contextId));
				}
				else { // genome
					query.setFilterQueries("genome_id:" + contextId);
				}
				query.setRows(0).setFacet(true).setFacetMinCount(1).addFacetField("annotation");

				// hypothetical
				query.setQuery("product:(hypothetical AND protein) AND feature_type:CDS");

				QueryResponse qr = solr.getServer().query(query);
				FacetField ff = qr.getFacetField("annotation");
				for (FacetField.Count fc : ff.getValues()) {
					hypotheticalProteins.put(fc.getName(), fc.getCount());
				}

				// funtional assigned
				query.setQuery("!product:(hypothetical AND protein) AND feature_type:CDS");
				qr = solr.getServer().query(query);
				ff = qr.getFacetField("annotation");
				for (FacetField.Count fc : ff.getValues()) {
					functionalProteins.put(fc.getName(), fc.getCount());
				}

				// ec assigned
				query.setQuery("ec:[* TO *]");
				qr = solr.getServer().query(query);
				ff = qr.getFacetField("annotation");
				for (FacetField.Count fc : ff.getValues()) {
					ecAssignedProteins.put(fc.getName(), fc.getCount());
				}

				// go assigned
				query.setQuery("go:[* TO *]");
				qr = solr.getServer().query(query);
				ff = qr.getFacetField("annotation");
				for (FacetField.Count fc : ff.getValues()) {
					goAssignedProteins.put(fc.getName(), fc.getCount());
				}

				// pathway assigned
				query.setQuery("pathway:[* TO *]");
				qr = solr.getServer().query(query);
				ff = qr.getFacetField("annotation");
				for (FacetField.Count fc : ff.getValues()) {
					pathwayAssignedProteins.put(fc.getName(), fc.getCount());
				}

				// figfam assigned
				query.setQuery("figfam_id:[* TO *]");

				qr = solr.getServer().query(query);
				ff = qr.getFacetField("annotation");
				for (FacetField.Count fc : ff.getValues()) {
					figfamAssignedProteins.put(fc.getName(), fc.getCount());
				}
			}
			catch (MalformedURLException | SolrServerException e) {
				LOGGER.error(e.getMessage(), e);
			}

			request.setAttribute("hypotheticalProteins", hypotheticalProteins);
			request.setAttribute("functionalProteins", functionalProteins);
			request.setAttribute("ecAssignedProteins", ecAssignedProteins);
			request.setAttribute("goAssignedProteins", goAssignedProteins);
			request.setAttribute("pathwayAssignedProteins", pathwayAssignedProteins);
			request.setAttribute("figfamAssignedProteins", figfamAssignedProteins);

			response.setContentType("text/html");
			PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/overview/protein_feature_summary.jsp");
			prd.include(request, response);
		}
	}
}
