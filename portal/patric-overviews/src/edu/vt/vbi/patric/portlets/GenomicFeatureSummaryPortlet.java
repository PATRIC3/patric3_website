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

public class GenomicFeatureSummaryPortlet extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(GenomicFeatureSummaryPortlet.class);

	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		response.setContentType("text/html");
		String cType = request.getParameter("context_type");
		String cId = request.getParameter("context_id");

		if (cType != null && cId != null && (cType.equals("genome") || cType.equals("taxon"))) {
			PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/overview/genomic_feature_summary_init.jsp");
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
		String viewOption = request.getParameter("view");

		if (contextType != null && contextId != null) {

			String contextLink = null;
			if (contextType.equals("genome")) {
				contextLink = "cType=genome&amp;cId=" + contextId;
			}
			else if (contextType.equals("taxon")) {
				contextLink = "cType=taxon&amp;cId=" + contextId;
			}

			DataApiHandler dataApi = new DataApiHandler(request);

			String queryParam, filterParam = null;

			if (contextType.equals("taxon")) {
				if (!viewOption.equals("full")) {
					queryParam = "feature_type:(CDS OR *RNA)";
				}
				else {
					queryParam = "*:*";
				}
				filterParam = SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + contextId);
			}
			else {
				if (!viewOption.equals("full")) {
					queryParam = "genome_id:" + contextId + " AND feature_type:(CDS OR *RNA)";
				}
				else {
					queryParam = "genome_id:" + contextId;
				}
			}

			Map facets = dataApi.getPivotFacets(SolrCore.FEATURE, queryParam, filterParam, "feature_type,annotation");
			Map<String, Map<String, Integer>> summary = (Map) facets.get("feature_type,annotation");

			response.setContentType("text/html");
			request.setAttribute("summary", summary);
			request.setAttribute("contextLink", contextLink);
			request.setAttribute("viewOption", viewOption);

			PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/overview/genomic_feature_summary.jsp");
			prd.include(request, response);
		}
	}
}
