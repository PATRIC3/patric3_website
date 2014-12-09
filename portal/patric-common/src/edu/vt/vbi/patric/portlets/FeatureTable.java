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

import edu.vt.vbi.patric.common.FASTAHelper;
import edu.vt.vbi.patric.common.SiteHelper;
import edu.vt.vbi.patric.common.SolrCore;
import edu.vt.vbi.patric.common.SolrInterface;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.FacetParams;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeatureTable extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(FASTAHelper.class);

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		new SiteHelper().setHtmlMetaElements(request, response, "Feature Table");

		response.setContentType("text/html");
		String cType = request.getParameter("context_type");

		if (cType != null) {
			PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/featuretable/featuretable.jsp");
			prd.include(request, response);
		}
		else {
			PrintWriter writer = response.getWriter();
			writer.write("<p>Invalid Parameter - missing context information</p>");
			writer.close();
		}
	}

	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		String mode = request.getParameter("mode");

		switch (mode) {
		case "fasta":
			fastDownloadHandler(request, response);
			break;
		case "filter":
			populateFilterValues(request, response);
			break;
		}
	}

	private void populateFilterValues(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		String keyword = request.getParameter("keyword");
		String taxonId = request.getParameter("taxonId");
		String genomeId = request.getParameter("genomeId");

		SolrInterface solr = new SolrInterface();

		try {
			SolrQuery query = new SolrQuery(keyword);
			query.addFilterQuery("!annotation:BRC1"); // remove BRC1 from the filter
			query.setRows(0).setFacet(true).setFacetMinCount(1).setFacetLimit(-1).setFacetSort(FacetParams.FACET_SORT_COUNT)
					.addFacetField("annotation", "feature_type");

			if (taxonId != null && !taxonId.equals("")) {
				query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + taxonId));
			}
			if (genomeId != null && !genomeId.equals("")) {
				query.addFilterQuery("genome_id:" + genomeId);
			}
			QueryResponse qr = solr.getSolrServer(SolrCore.FEATURE).query(query);
			JSONObject facets = solr.facetFieldstoJSONObject(qr);

			JSONObject res = new JSONObject();
			res.put("facets", facets);
			res.writeJSONString(response.getWriter());
		}
		catch (MalformedURLException | SolrServerException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	private void fastDownloadHandler(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		String fileName = "sequence.fasta";
		List<String> featureIds = new ArrayList<>();

		// getting common params
		String fastaAction = request.getParameter("fastaaction"); // download or display
		String fastaType = request.getParameter("fastatype"); // DNA(NA), Protein (AA) or both (ALL)
		String fastaScope = request.getParameter("fastascope"); // all features (ALL) or selected (SEL)
		String fids = request.getParameter("fids");

		if (fastaType != null && (fastaType.equalsIgnoreCase("dna") || fastaType.equalsIgnoreCase("protein") || fastaType.equalsIgnoreCase("both"))) {

			if (fastaScope != null && fastaScope.equalsIgnoreCase("Selected")) {

				if (fids != null && fids.contains(",")) {
					featureIds.addAll(Arrays.asList(fids.split(",")));
				}
				else if (fids != null && !fids.equalsIgnoreCase("")) {
					featureIds.add(fids);
				}
			}
		}
		assert fastaType != null;

		if (fastaAction != null && fastaAction.equalsIgnoreCase("download")) {

			response.setContentType("application/octetstream");
			response.setProperty("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

			String output = FASTAHelper.getFASTASequence(featureIds, fastaType);

			response.getPortletOutputStream().write(output.getBytes());

		}
		else if (fastaAction != null && fastaAction.equalsIgnoreCase("display")) {

			StringBuilder output = new StringBuilder();
			output.append("<div class=\"fixed-width-font\">\n");
			output.append("<pre>\n");

			output.append(FASTAHelper.getFASTASequence(featureIds, fastaType));

			output.append("</pre>");
			output.append("</div>");

			getPortletContext().getRequestDispatcher("/jsp/popup_header.jsp").include(request, response);

			response.getWriter().print(output.toString());

			getPortletContext().getRequestDispatcher("/jsp/popup_footer.jsp").include(request, response);
		}
	}
}
