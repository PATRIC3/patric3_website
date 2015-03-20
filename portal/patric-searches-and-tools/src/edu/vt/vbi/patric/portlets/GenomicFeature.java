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

import com.google.gson.Gson;
import edu.vt.vbi.patric.beans.Genome;
import edu.vt.vbi.patric.beans.GenomeFeature;
import edu.vt.vbi.patric.beans.Taxonomy;
import edu.vt.vbi.patric.common.SessionHandler;
import edu.vt.vbi.patric.common.SiteHelper;
import edu.vt.vbi.patric.common.SolrCore;
import edu.vt.vbi.patric.common.SolrInterface;
import edu.vt.vbi.patric.dao.ResultType;
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
import java.util.ArrayList;
import java.util.HashMap;
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
		new SiteHelper().setHtmlMetaElements(request, response, "Feature Finder");

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

			if(key != null && key.containsKey("taxonId")){
				taxonId = key.get("taxonId");
			}

			if(key != null && key.containsKey("genomeId")){
				genomeId = key.get("genomeId");
			}

			if(key != null && key.containsKey("keyword")){
				keyword = key.get("keyword");
			}

			if(key != null && key.containsKey("exact_search_term")){
				exactSearchTerm = key.get("exact_search_term");
			}
			String algorithm = "";
			if(keyword.contains("annotation:)")){
				algorithm = keyword.split("annotation:\\(")[1].split("\\)")[0];
			}

			String featureType = "";
			if(keyword.contains("feature_type:")){
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

			String facet, keyword, pk, state;
			boolean grouping = false;
			boolean hl;

			ResultType key = new ResultType();

			JSONObject jsonResult = new JSONObject();

			if (need.equals("feature")) {
				// global search
				solr.setCurrentInstance(SolrCore.FEATURE);

				pk = request.getParameter("pk");
				keyword = request.getParameter("keyword");
				facet = request.getParameter("facet");
				String highlight = request.getParameter("highlight");

				hl = Boolean.parseBoolean(highlight);

				if (request.getParameter("grouping") != null) {
					grouping = Boolean.parseBoolean(request.getParameter("grouping"));
				}

				String json = SessionHandler.getInstance().get(SessionHandler.PREFIX + pk);
				if (json == null) {
					key.put("facet", facet);
					key.put("keyword", keyword);

					SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, gson.toJson(key, ResultType.class));
				}
				else {
					key = gson.fromJson(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk), ResultType.class);
					key.put("facet", facet);
				}

				// To support grouping option
				key.put("grouping", true);

				String start_id = request.getParameter("start");
				String limit = request.getParameter("limit");
				int start = Integer.parseInt(start_id);
				int end = Integer.parseInt(limit);

				HashMap<String, String> sort = null;
				if (request.getParameter("sort") != null) {
					// sorting
					// JSONParser a = new JSONParser();
					JSONArray sorter;
					String sort_field = "";
					String sort_dir = "";
					try {
						sorter = (JSONArray) jsonParser.parse(request.getParameter("sort"));
						sort_field += ((JSONObject) sorter.get(0)).get("property").toString();
						sort_dir += ((JSONObject) sorter.get(0)).get("direction").toString();
						for (int i = 1; i < sorter.size(); i++) {
							sort_field += "," + ((JSONObject) sorter.get(i)).get("property").toString();
						}
					}
					catch (ParseException e) {
						LOGGER.error(e.getMessage(), e);
					}

					sort = new HashMap<>();

					if (!sort_field.equals("") && !sort_dir.equals("")) {
						sort.put("field", sort_field);
						sort.put("direction", sort_dir);
					}
				}
				// add join condition
				if (key.containsKey("taxonId") && key.get("taxonId") != null) {
					key.put("join", SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + key.get("taxonId")));
				}
				if (key.containsKey("genomeId") && key.get("genomeId") != null && !key.get("genomeId").equals("")) {
					key.put("join", "genome_id:(" + key.get("genomeId").replaceAll(",", " OR ") + ")");
				}

				key.put("fields",
						"genome_id,genome_name,sequence_id,accession,seed_id,alt_locus_tag,refseq_locus_tag,gene,annotation,feature_type,feature_id,start,end,na_length,strand,protein_id,aa_length,product,figfam_id");

				JSONObject object = solr.getData(key, sort, facet, start, end, true, hl, grouping);

				JSONObject obj = (JSONObject) object.get("response");
				JSONArray obj1 = (JSONArray) obj.get("docs");

				if (!key.containsKey("facets")) {
					JSONObject facets = (JSONObject) object.get("facets");
					key.put("facets", facets.toJSONString());
					SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, gson.toJson(key, ResultType.class));
				}

				jsonResult.put("results", obj1);
				jsonResult.put("total", obj.get("numFound"));

				response.setContentType("application/json");
				PrintWriter writer = response.getWriter();
				jsonResult.writeJSONString(writer);
				writer.close();

			}
			else if (need.equals("featurewofacet")) {

				pk = request.getParameter("pk");
				keyword = request.getParameter("keyword");
				facet = request.getParameter("facet");

				key.put("keyword", keyword);

				if (request.getParameter("taxonId") != null && !request.getParameter("taxonId").equals("")) {
					key.put("taxonId", request.getParameter("taxonId"));
				}
				if (request.getParameter("genomeId") != null && !request.getParameter("genomeId").equals("")) {
					key.put("genomeId", request.getParameter("genomeId"));
				}

				LOGGER.debug("need:featurewofacet, key:{}", key);

				String start_id = request.getParameter("start");
				String limit = request.getParameter("limit");
				int start = Integer.parseInt(start_id);
				int end = Integer.parseInt(limit);

				SolrQuery query = new SolrQuery();
				query.setQuery(solr.KeywordReplace(key.get("keyword")));

				// set fields
				query.addField(
						"genome_id,genome_name,sequence_id,accession,seed_id,alt_locus_tag,refseq_locus_tag,gene,annotation,feature_type,feature_id,start,end,na_length,strand,protein_id,aa_length,product,figfam_id");

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
				if (key.containsKey("taxonId") && key.get("taxonId") != null) {
					query.setFilterQueries(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + key.get("taxonId")));
				}
				if (key.containsKey("genomeId")) {
					query.setFilterQueries("genome_id:" + key.get("genomeId"));
				}

				JSONArray docs = new JSONArray();
				long numFound = 0l;
				try {
					LOGGER.debug("featurewofacet:{}", query.toString());

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

			}
			else if (need.equals("tree")) {

				pk = request.getParameter("pk");

				String json = SessionHandler.getInstance().get(SessionHandler.PREFIX + pk);
				if (json != null) {
					key = gson.fromJson(json, ResultType.class);
				}

				if (key != null && key.containsKey("state")) {
					state = key.get("state");
				}
				else {
					state = request.getParameter("state");
				}

				SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, gson.toJson(key, ResultType.class));

				try {
					if (!key.containsKey("tree")) {
						JSONObject facet_fields = (JSONObject) jsonParser.parse(key.get("facets"));
						JSONArray arr1 = solr.processStateAndTree(key, need, facet_fields, key.get("facet"), state, 4, true);
						jsonResult.put("results", arr1);

						key.put("tree", arr1);
					}
					else {
						jsonResult.put("results", key.get("tree"));
					}
				}
				catch (ParseException e) {
					LOGGER.error(e.getMessage(), e);
				}

				response.setContentType("application/json");
				PrintWriter writer = response.getWriter();
				writer.write(jsonResult.get("results").toString());
				writer.close();
			}
			else if (need.equals("getIdsForCart")) {

				solr.setCurrentInstance(SolrCore.FEATURE);

				pk = request.getParameter("pk");
				int rows = Integer.parseInt(request.getParameter("limit"));
				String field = request.getParameter("field");

				key = gson.fromJson(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk), ResultType.class);

				JSONObject object = solr.getIdsForCart(key, field, rows);

				JSONObject obj = (JSONObject) object.get("response");
				JSONArray obj1 = (JSONArray) obj.get("docs");

				jsonResult.put("results", obj1);
				jsonResult.put("total", obj.get("numFound"));

				response.setContentType("application/json");
				PrintWriter writer = response.getWriter();
				jsonResult.writeJSONString(writer);
				writer.close();
			}
		}
	}
}
