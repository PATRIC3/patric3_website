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
import java.util.*;

public class GenomicFeatureSummaryPortlet extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(GenomicFeatureSummaryPortlet.class);

	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		response.setContentType("text/html");
		String cType = request.getParameter("context_type");
		String cId = request.getParameter("context_id");

		// TODO: implement redirection for p2_genome_id to new genome_id

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

			Map<String, Map<String, Long>> featureCounts = new HashMap<>();

			String contextLink = null;
			if (contextType.equals("genome")) {
				contextLink = "cType=genome&amp;cId=" + contextId;
			}
			else if (contextType.equals("taxon")) {
				contextLink = "cType=taxon&amp;cId=" + contextId;
			}

			List<String> annotations = Arrays.asList("PATRIC", "RefSeq", "BRC1");
			int maxFeatureCount = -1;
			Set<String> allFeatureTypes = null;

			for (String annotation : annotations) {
				Map<String, Long> featureSummary = new LinkedHashMap<>();
				SolrQuery query = new SolrQuery();

				if (contextType.equals("taxon")) {
					if (!viewOption.equals("full")) {
						query.setQuery("annotation:" + annotation + " AND feature_type:(CDS OR *RNA)");
					}
					else {
						query.setQuery("annotation:" + annotation);
					}
					query.setFilterQueries(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + contextId));
				}
				else {
					if (!viewOption.equals("full")) {
						query.setQuery("annotation:" + annotation + " AND genome_id:" + contextId + " AND feature_type:(CDS OR *RNA)");
					}
					else {
						query.setQuery("annotation:" + annotation + " AND genome_id:" + contextId);
					}
				}

				query.setRows(0).setFacet(true).setFacetMinCount(1).addFacetField("feature_type");

				try {
					SolrInterface solr = new SolrInterface();
					solr.setCurrentInstance(SolrCore.FEATURE);

					QueryResponse qr = solr.getServer().query(query);

					FacetField facetField = qr.getFacetField("feature_type");

					for (FacetField.Count facetValue : facetField.getValues()) {
						featureSummary.put(facetValue.getName(), facetValue.getCount());
					}

					featureCounts.put(annotation, featureSummary);
					if (featureSummary.size() > maxFeatureCount) {
						allFeatureTypes = featureSummary.keySet();
						maxFeatureCount = featureCounts.size();
					}

				}
				catch (MalformedURLException | SolrServerException e) {
					LOGGER.error(e.getMessage(), e);
				}
			}

			// transpose arrays
			Map<String, Map<String, Long>> summary = new LinkedHashMap<>();
			assert allFeatureTypes != null;
			for (String type : allFeatureTypes) {
				Map<String, Long> sum = new HashMap<>();
				for (String annotation : annotations) {
					if (featureCounts.containsKey(annotation) && featureCounts.get(annotation).containsKey(type)) {
						sum.put(annotation, featureCounts.get(annotation).get(type));
					}
				}
				summary.put(type, sum);
			}

			response.setContentType("text/html");
			request.setAttribute("summary", summary);
			request.setAttribute("contextLink", contextLink);
			request.setAttribute("viewOption", viewOption);

			PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/overview/genomic_feature_summary.jsp");
			prd.include(request, response);
		}
	}
}
