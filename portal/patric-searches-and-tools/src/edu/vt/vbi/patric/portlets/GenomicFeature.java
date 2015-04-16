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

import com.google.gson.Gson;
import edu.vt.vbi.patric.beans.Genome;
import edu.vt.vbi.patric.beans.GenomeFeature;
import edu.vt.vbi.patric.beans.Taxonomy;
import edu.vt.vbi.patric.common.*;
import edu.vt.vbi.patric.dao.ResultType;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GenomicFeature extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(GenomicFeature.class);

	SolrInterface solr = new SolrInterface();

	JSONParser jsonParser = new JSONParser();

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		response.setContentType("text/html");
		String mode = request.getParameter("display_mode");
		SiteHelper.setHtmlMetaElements(request, response, "Feature Finder");

		PortletRequestDispatcher prd;
		if (mode != null && mode.equals("result")) {

			String contextType = request.getParameter("context_type");
			String contextId = request.getParameter("context_id");
			String pk = request.getParameter("param_key");
			Gson gson = new Gson();

			ResultType key = gson.fromJson(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk), ResultType.class);

			String taxonId = "";
			String genomeId = "";
			String keyword = "";
			String exactSearchTerm = "";

			if (key != null && key.containsKey("taxonId")) {
				taxonId = key.get("taxonId");
			}

			if (key != null && key.containsKey("genomeId")) {
				genomeId = key.get("genomeId");
			}

			if (key != null && key.containsKey("keyword")) {
				keyword = key.get("keyword");
			}

			if (key != null && key.containsKey("exact_search_term")) {
				exactSearchTerm = key.get("exact_search_term");
			}
			String algorithm = "";
			if (keyword.contains("annotation:)")) {
				algorithm = keyword.split("annotation:\\(")[1].split("\\)")[0];
			}

			String featureType = "";
			if (keyword.contains("feature_type:")) {
				featureType = keyword.split("feature_type:\\(")[1].split("\\)")[0];
			}

			request.setAttribute("contextType", contextType);
			request.setAttribute("contextId", contextId);
			request.setAttribute("pk", pk);
			request.setAttribute("taxonId", taxonId);
			request.setAttribute("genomeId", genomeId);
			request.setAttribute("keyword", keyword);
			request.setAttribute("exactSearchTerm", exactSearchTerm);
			request.setAttribute("algorithm", algorithm);
			request.setAttribute("featureType", featureType);

			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/feature_finder_result.jsp");
		}
		else {

			boolean isLoggedInd = Downloads.isLoggedIn(request);
			request.setAttribute("isLoggedIn", isLoggedInd);

			String contextType = request.getParameter("context_type");
			String contextId = request.getParameter("context_id");
			Taxonomy taxonomy = null;
			String organismName = null;
			List<String> featureTypes = new ArrayList<>();

			SolrInterface solr = new SolrInterface();
			LBHttpSolrServer lbHttpSolrServer = solr.getSolrServer(SolrCore.FEATURE);

			if (contextType.equals("taxon")) {
				taxonomy = solr.getTaxonomy(Integer.parseInt(contextId));
				organismName = taxonomy.getTaxonName();

				try {
					SolrQuery query = new SolrQuery();
					query.setQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + taxonomy.getId()));
					query.setRows(0).setFacet(true).setFacetMinCount(1).setFacetLimit(-1).addFacetField("feature_type");

					QueryResponse qr = lbHttpSolrServer.query(query);
					FacetField ffFeatureTypes = qr.getFacetField("feature_type");

					for (FacetField.Count featureType : ffFeatureTypes.getValues()) {
						featureTypes.add(featureType.getName());
					}
				}
				catch (SolrServerException e) {
					LOGGER.error(e.getMessage(), e);
				}
			}
			else if (contextType.equals("genome")) {
				Genome genome = solr.getGenome(contextId);
				taxonomy = solr.getTaxonomy(genome.getTaxonId());
				organismName = genome.getGenomeName();

				try {
					SolrQuery query = new SolrQuery("genome_id:" + genome.getId());
					query.setRows(0).setFacet(true).setFacetMinCount(1).setFacetLimit(-1).addFacetField("feature_type");

					QueryResponse qr = lbHttpSolrServer.query(query);
					FacetField ffFeatureTypes = qr.getFacetField("feature_type");

					for (FacetField.Count featureType : ffFeatureTypes.getValues()) {
						featureTypes.add(featureType.getName());
					}
				}
				catch (SolrServerException e) {
					LOGGER.error(e.getMessage(), e);
				}
			}

			assert taxonomy != null;
			request.setAttribute("taxonId", taxonomy.getId());
			request.setAttribute("organismName", organismName);
			request.setAttribute("contextType", contextType);
			request.setAttribute("contextId", contextId);
			request.setAttribute("featureTypes", featureTypes);

			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/feature_finder.jsp");
		}
		prd.include(request, response);
	}

	@SuppressWarnings("unchecked")
	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		String sraction = request.getParameter("sraction");
		Gson gson = new Gson();

		if (sraction != null && sraction.equals("save_params")) {

			LOGGER.debug("{}", request.getParameterMap());

			ResultType key = new ResultType();

			String genomeId = request.getParameter("genomeId");
			String taxonId = "";
			String cType = request.getParameter("cType");
			String cId = request.getParameter("cId");

			if (cType != null && cId != null && cType.equals("taxon") && !cId.equals("")) {
				taxonId = cId;
			}

			String keyword = request.getParameter("keyword");
			String state = request.getParameter("state");
			String ncbi_taxon_id = request.getParameter("ncbi_taxon_id");
			String exact_search_term = request.getParameter("exact_search_term");

			if (genomeId != null && !genomeId.equalsIgnoreCase("")) {
				key.put("genomeId", genomeId);
			}
			if (!taxonId.equalsIgnoreCase("")) {
				key.put("taxonId", taxonId);
			}
			if (keyword != null) {
				key.put("keyword", keyword.trim());
			}
			if (ncbi_taxon_id != null) {
				key.put("ncbi_taxon_id", ncbi_taxon_id);
			}
			if (state != null) {
				key.put("state", state);
			}
			if (exact_search_term != null) {
				key.put("exact_search_term", exact_search_term);
			}

			if (!key.containsKey("genomeId") && cType != null && cType.equals("genome") && cId != null && !cId.equals("")) {
				key.put("genomeId", cId);
			}
			if (!key.containsKey("taxonId") && cType != null && cType.equals("taxon") && cId != null && !cId.equals("")) {
				key.put("taxonId", cId);
			}
			// random
			long pk = (new Random()).nextLong();

			SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, gson.toJson(key, ResultType.class));

			PrintWriter writer = response.getWriter();
			writer.write("" + pk);
			writer.close();

		}
		else {

			String need = request.getParameter("need");

			String keyword, pk;

			ResultType key;

			JSONObject jsonResult = new JSONObject();

			switch (need) {
			case "feature":
			case "featurewofacet": {

				pk = request.getParameter("pk");
				keyword = "*:*";

				if (pk != null) {
					key = gson.fromJson(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk), ResultType.class);
					if (key != null && key.containsKey("keyword") && key.get("keyword") != null && !key.get("keyword").equals("")) {
						keyword = StringHelper.parseSolrKeywordOperator(URLDecoder.decode(key.get("keyword"), "UTF-8"));
					}
				}

				if (request.getParameterMap().containsKey("keyword")
						&& request.getParameter("keyword") != null
						&& !request.getParameter("keyword").equals("")
						&& !request.getParameter("keyword").equals("*:*")) {
					keyword = StringHelper.parseSolrKeywordOperator(URLDecoder.decode(request.getParameter("keyword"), "UTF-8"));
				}

				String taxonId = null;
				String genomeId = null;

				if (request.getParameter("taxonId") != null && !request.getParameter("taxonId").equals("")) {
					taxonId = request.getParameter("taxonId");
				}
				if (request.getParameter("genomeId") != null && !request.getParameter("genomeId").equals("")) {
					genomeId = request.getParameter("genomeId");
				}

				String start_id = request.getParameter("start");
				String limit = request.getParameter("limit");
				int start = Integer.parseInt(start_id);
				int end = Integer.parseInt(limit);

				SolrQuery query = new SolrQuery();
				query.setQuery(keyword);

				// set fields
				query.addField(StringUtils.join(DownloadHelper.getFieldsForFeatures(), ","));

				// paging
				query.setStart(start);
				if (end != -1) {
					query.setRows(end);
				}

				// parse sorting conditions
				if (request.getParameter("sort") != null) {
					JSONArray sorter;
					try {
						sorter = (JSONArray) jsonParser.parse(request.getParameter("sort"));
						for (Object aSort : sorter) {
							JSONObject jsonSort = (JSONObject) aSort;
							query.addSort(SolrQuery.SortClause
									.create(jsonSort.get("property").toString(), jsonSort.get("direction").toString().toLowerCase()));
						}
					}
					catch (ParseException e) {
						LOGGER.error(e.getMessage(), e);
					}
				}
				// add join condition
				if (taxonId != null) {
					query.setFilterQueries(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + taxonId));
				}
				if (genomeId != null) {
					query.setFilterQueries("genome_id:" + genomeId);
				}

				JSONArray docs = new JSONArray();
				long numFound = 0l;
				try {
					QueryResponse qr = solr.getSolrServer(SolrCore.FEATURE).query(query);
					List<GenomeFeature> records = qr.getBeans(GenomeFeature.class);
					numFound = qr.getResults().getNumFound();

					for (GenomeFeature feature : records) {
						docs.add(feature.toJSONObject());
					}
				}
				catch (SolrServerException e) {
					LOGGER.error(e.getMessage(), e);
				}

				jsonResult.put("results", docs);
				jsonResult.put("total", numFound);

				response.setContentType("application/json");
				PrintWriter writer = response.getWriter();
				jsonResult.writeJSONString(writer);
				writer.close();

				break;
			}
//			case "tree": {
//
//				pk = request.getParameter("pk");
//
//				String json = SessionHandler.getInstance().get(SessionHandler.PREFIX + pk);
//				if (json != null) {
//					key = gson.fromJson(json, ResultType.class);
//				}
//
//				if (key != null && key.containsKey("state")) {
//					state = key.get("state");
//				}
//				else {
//					state = request.getParameter("state");
//				}
//
//				SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, gson.toJson(key, ResultType.class));
//
//				try {
//					if (!key.containsKey("tree")) {
//						JSONObject facet_fields = (JSONObject) jsonParser.parse(key.get("facets"));
//						JSONArray arr1 = solr.processStateAndTree(key, need, facet_fields, key.get("facet"), state, 4, true);
//						jsonResult.put("results", arr1);
//
//						key.put("tree", arr1);
//					}
//					else {
//						jsonResult.put("results", key.get("tree"));
//					}
//				}
//				catch (ParseException e) {
//					LOGGER.error(e.getMessage(), e);
//				}
//
//				response.setContentType("application/json");
//				PrintWriter writer = response.getWriter();
//				writer.write(jsonResult.get("results").toString());
//				writer.close();
//				break;
//			}
			}
		}
	}
}
