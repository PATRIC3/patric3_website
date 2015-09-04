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

import edu.vt.vbi.patric.common.DataApiHandler;
import edu.vt.vbi.patric.common.SolrCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class ProteinFeatureSummaryPortlet extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProteinFeatureSummaryPortlet.class);

	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		response.setContentType("text/html");
		String cType = request.getParameter("context_type");
		String cId = request.getParameter("context_id");

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

			// TODO: use json.facet after moving to Solr5. query facet in heliosearch has a bug that not using post filter.
			DataApiHandler dataApi = new DataApiHandler(request);

			// set default params
			String filterParam;
			if (contextType.equals("taxon")) {
				filterParam = SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + contextId);
			}
			else { // genome
				filterParam = "genome_id:" + contextId;
			}

			// hypothetical
			Map facets = dataApi
					.getFieldFacets(SolrCore.FEATURE, "product:(hypothetical AND protein) AND feature_type:CDS", filterParam, "annotation");
			Map<String, Integer> hypotheticalProteins = (Map) ((Map) facets.get("facets")).get("annotation");

			// funtional assigned
			facets = dataApi.getFieldFacets(SolrCore.FEATURE, "!product:(hypothetical AND protein) AND feature_type:CDS", filterParam, "annotation");
			Map<String, Integer> functionalProteins = (Map) ((Map) facets.get("facets")).get("annotation");

			// ec assigned
			facets = dataApi.getFieldFacets(SolrCore.FEATURE, "ec:[* TO *]", filterParam, "annotation");
			Map<String, Integer> ecAssignedProteins = (Map) ((Map) facets.get("facets")).get("annotation");

			// go assigned
			facets = dataApi.getFieldFacets(SolrCore.FEATURE, "go:[* TO *]", filterParam, "annotation");
			Map<String, Integer> goAssignedProteins = (Map) ((Map) facets.get("facets")).get("annotation");

			// pathway assigned
			facets = dataApi.getFieldFacets(SolrCore.FEATURE, "pathway:[* TO *]", filterParam, "annotation");
			Map<String, Integer> pathwayAssignedProteins = (Map) ((Map) facets.get("facets")).get("annotation");

			// figfam assigned
			facets = dataApi.getFieldFacets(SolrCore.FEATURE, "figfam_id:[* TO *]", filterParam, "annotation");
			Map<String, Integer> figfamAssignedProteins = (Map) ((Map) facets.get("facets")).get("annotation");

			facets = dataApi.getFieldFacets(SolrCore.FEATURE, "plfam_id:[* TO *]", filterParam, "annotation");
			Map<String, Integer> plfamAssignedProteins = (Map) ((Map) facets.get("facets")).get("annotation");

			facets = dataApi.getFieldFacets(SolrCore.FEATURE, "pgfam_id:[* TO *]", filterParam, "annotation");
			Map<String, Integer> pgfamAssignedProteins = (Map) ((Map) facets.get("facets")).get("annotation");

			request.setAttribute("contextType", contextType);
			request.setAttribute("contextId", contextId);

			request.setAttribute("hypotheticalProteins", hypotheticalProteins);
			request.setAttribute("functionalProteins", functionalProteins);
			request.setAttribute("ecAssignedProteins", ecAssignedProteins);
			request.setAttribute("goAssignedProteins", goAssignedProteins);
			request.setAttribute("pathwayAssignedProteins", pathwayAssignedProteins);
			request.setAttribute("figfamAssignedProteins", figfamAssignedProteins);
			request.setAttribute("plfamAssignedProteins", plfamAssignedProteins);
			request.setAttribute("pgfamAssignedProteins", pgfamAssignedProteins);

			response.setContentType("text/html");
			PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/overview/protein_feature_summary.jsp");
			prd.include(request, response);
		}
	}
}
