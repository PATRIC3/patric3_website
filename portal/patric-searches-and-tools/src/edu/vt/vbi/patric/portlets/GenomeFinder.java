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
import edu.vt.vbi.patric.beans.GenomeSequence;
import edu.vt.vbi.patric.beans.Taxonomy;
import edu.vt.vbi.patric.common.SessionHandler;
import edu.vt.vbi.patric.common.SiteHelper;
import edu.vt.vbi.patric.common.SolrCore;
import edu.vt.vbi.patric.common.SolrInterface;
import edu.vt.vbi.patric.dao.ResultType;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.FacetParams;
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

public class GenomeFinder extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(GenomeFinder.class);

	SolrInterface solr = new SolrInterface();

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		response.setContentType("text/html");
		String mode = request.getParameter("display_mode");

		new SiteHelper().setHtmlMetaElements(request, response, "Genome Finder");

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
			String searchOn = "", exactSearchTerm = "";

			if (key != null && key.containsKey("taxonId")) {
				taxonId = key.get("taxonId");
			}

			if (key != null && key.containsKey("genomeId")) {
				genomeId = key.get("genomeId");
			}

			if (key != null && key.containsKey("keyword")) {
				keyword = key.get("keyword");
			}

			if (key != null && key.containsKey("search_on")) {
				searchOn = key.get("search_on");
			}

			if (key != null && key.containsKey("exact_search_term")) {
				exactSearchTerm = key.get("exact_search_term");
			}

			request.setAttribute("contextType", contextType);
			request.setAttribute("contextId", contextId);
			request.setAttribute("pk", pk);
			request.setAttribute("taxonId", taxonId);
			request.setAttribute("genomeId", genomeId);
			request.setAttribute("keyword", keyword);
			request.setAttribute("searchOn", searchOn);
			request.setAttribute("exactSearchTerm", exactSearchTerm);

			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/genome_finder_result.jsp");
		}
		else {

			boolean isLoggedInd = Downloads.isLoggedIn(request);
			request.setAttribute("isLoggedIn", isLoggedInd);

			String contextType = request.getParameter("context_type");
			String contextId = request.getParameter("context_id");
			Taxonomy taxonomy = null;
			String organismName = null;

			if (contextId == null || contextId.equals("")) {
				throw new PortletException("Important parameter (cId) is missing");
			}

			SolrInterface solr = new SolrInterface();

			if (contextType.equals("taxon")) {
				taxonomy = solr.getTaxonomy(Integer.parseInt(contextId));
				organismName = taxonomy.getTaxonName();
			}
			else if (contextType.equals("genome")) {
				Genome genome = solr.getGenome(contextId);
				taxonomy = solr.getTaxonomy(genome.getTaxonId());
				organismName = genome.getGenomeName();
			}

			request.setAttribute("taxonId", taxonomy.getId());
			request.setAttribute("organismName", organismName);
			request.setAttribute("cType", contextType);
			request.setAttribute("cId", contextId);

			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/genome_finder.jsp");
		}
		prd.include(request, response);
	}

	@SuppressWarnings("unchecked")
	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		String sraction = request.getParameter("sraction");
		Gson gson = new Gson();

		if (sraction != null && sraction.equals("save_params")) {

			ResultType key = new ResultType();

			String genomeId = request.getParameter("genomeId");
			String taxonId = request.getParameter("taxonId");
			String cType = request.getParameter("cType");
			String cId = request.getParameter("cId");
			if (cType != null && cId != null && cType.equals("taxon") && !cId.equals("")) {
				taxonId = cId;
			}
			String keyword = request.getParameter("keyword");
			String state = request.getParameter("state");
			String ncbi_taxon_id = request.getParameter("ncbi_taxon_id");
			String exact_search_term = request.getParameter("exact_search_term");
			String search_on = request.getParameter("search_on");

			if (genomeId != null && !genomeId.equals("")) {
				key.put("genomeId", genomeId);
			}
			if (taxonId != null && !taxonId.equals("")) {
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
			if (search_on != null) {
				key.put("search_on", search_on);
			}

			if (!key.containsKey("genomeId") && cType != null && cType.equals("genome") && cId != null && !cId.equals("")) {
				key.put("genomeId", cId);
			}
			if (!key.containsKey("taxonId") && cType != null && cType.equals("taxon") && cId != null && !cId.equals("")) {
				key.put("taxonId", cId);
			}
			LOGGER.debug("param:{}", request.getParameterMap());
			LOGGER.debug("key:{}", key.toString());

			long pk = (new Random()).nextLong();

			SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, gson.toJson(key, ResultType.class));

			PrintWriter writer = response.getWriter();
			writer.write("" + pk);
			writer.close();
		}
		else if (sraction != null && sraction.equals("get_params")) {

			String ret = "";
			String pk = request.getParameter("pk");
			String json = SessionHandler.getInstance().get(SessionHandler.PREFIX + pk);
			if (json != null) {
				ResultType key = gson.fromJson(json, ResultType.class);
				ret = key.get("keyword");
			}

			PrintWriter writer = response.getWriter();
			writer.write("" + ret);
			writer.close();

		}
		else {

			String need = request.getParameter("need");
			String facet, keyword, pk, state, taxonId, genomeId;
			boolean hl;

			ResultType key = new ResultType();
			JSONObject jsonResult = new JSONObject();
			taxonId = request.getParameter("taxonId");

			if (need.equals("1")) {
				// getting Genome Sequence List
				pk = request.getParameter("pk");
				keyword = request.getParameter("keyword");
				facet = request.getParameter("facet");

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

				String orig_keyword = key.get("keyword");
				if ((!key.containsKey("taxonId") || key.get("taxonId") == null) && taxonId != null) {
					key.put("taxonId", taxonId);
				}

				// Pre-processing. Query genone core and get facets and genome_info_ids for next query on sequence core
				SolrQuery query = new SolrQuery();

				query.setQuery(key.get("keyword"));
				query.setFilterQueries("taxon_lineage_ids:" + key.get("taxonId"));
				query.addField("genome_id");
				query.setRows(500000);

				query.setFacet(true);
				query.setFacetMinCount(1);
				query.setFacetLimit(-1);
				query.setFacetSort(FacetParams.FACET_SORT_COUNT);
				JSONObject facet_data = new JSONObject();
				try {
					facet_data = (JSONObject) new JSONParser().parse(facet);
				}
				catch (ParseException e) {
					LOGGER.error(e.getMessage(), e);
				}
				String[] facetFields = facet_data.get("facet").toString().split(",");

				for (String facetField : facetFields) {
					if (!facetField.equals("completion_date") && !facetField.equals("release_date")) {
						query.addFacetField(facetField);
					}
					else {
						query.addDateRangeFacet(facetField, solr.getRangeStartDate(), solr.getRangeEndDate(), "+1YEAR");
					}
				}

				List<String> listGenomeId = new ArrayList<>();
				try {
					QueryResponse qr = solr.getSolrServer(SolrCore.GENOME).query(query);
					List<Genome> records = qr.getBeans(Genome.class);

					JSONObject facets;
					if (facet != null) {
						facets = solr.facetFieldstoJSONObject(qr);
						key.put("facets", facets.toJSONString());
						SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, gson.toJson(key, ResultType.class));
					}

					for (Genome item : records) {
						listGenomeId.add(item.getId());
					}
				}
				catch (SolrServerException e) {
					LOGGER.error(e.getMessage(), e);
				}

				if (listGenomeId.size() > 0) {
					key.put("keyword", "genome_id:(" + StringUtils.join(listGenomeId, " OR ") + ")");
				}

				// 
				String start_id = request.getParameter("start");
				String limit = request.getParameter("limit");
				int start = Integer.parseInt(start_id);
				int end = Integer.parseInt(limit);

				query = new SolrQuery();
				query.setQuery(solr.KeywordReplace(key.get("keyword")));

				query.addField("genome_id,genome_name,sequence_id,accession,length,sequence_type,gc_content,description");

				query.setStart(start);
				if (end != -1) {
					query.setRows(end);
				}

				// sorting
				if (request.getParameter("sort") != null) {
					JSONArray sorter;
					try {
						sorter = (JSONArray) new JSONParser().parse(request.getParameter("sort"));
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

				if (key.containsKey("taxonId") && key.get("taxonId") != null) {
					query.setFilterQueries(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + key.get("taxonId")));
				}

				// fetch
				JSONArray docs = new JSONArray();
				long numFound = 0l;
				try {
					QueryResponse qr = solr.getSolrServer(SolrCore.SEQUENCE).query(query, SolrRequest.METHOD.POST);
					List<GenomeSequence> records = qr.getBeans(GenomeSequence.class);
					numFound = qr.getResults().getNumFound();

					for (GenomeSequence item : records) {
						docs.add(item.toJSONObject());
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
			else if (need.equals("0")) {
				// Getting Genome List

				pk = request.getParameter("pk");
				keyword = request.getParameter("keyword");
				facet = request.getParameter("facet");
				taxonId = request.getParameter("taxonId");
				genomeId = request.getParameter("genomeId");

				String highlight = request.getParameter("highlight");

				hl = Boolean.parseBoolean(highlight);

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

				if ((taxonId == null || taxonId.equals("")) && key.containsKey("taxonId") && !key.get("taxonId").equals("")) {
					taxonId = key.get("taxonId");
				}
				if ((genomeId == null || genomeId.equals("")) && key.containsKey("genomeId") && !key.get("genomeId").equals("")) {
					genomeId = key.get("genomeId");
				}

				String start_id = request.getParameter("start");
				String limit = request.getParameter("limit");
				int start = Integer.parseInt(start_id);
				int end = Integer.parseInt(limit);

				SolrQuery query = new SolrQuery();
				query.setQuery(solr.KeywordReplace(key.get("keyword")));

				// highlight
				if (hl) {
					query.set("hl", "on").set("hl.fl", "*");
				}

				// set fields
				query.addField(
						"genome_id,genome_name,taxon_id,genome_status,genome_length,chromosomes,plasmids,contigs,sequences,patric_cds,brc1_cds,refseq_cds,isolation_country,host_name,disease,collection_date,completion_date,strain,serovar,biovar,pathovar,mlst,other_typing,culture_collection,type_strain,sequencing_centers,publication,bioproject_accession,biosample_accession,assembly_accession,ncbi_project_id,refseq_project_id,genbank_accessions,refseq_accessions,sequencing_platform,sequencing_depth,assembly_method,gc_content,isolation_site,isolation_source,isolation_comments,geographic_location,latitude,longitude,altitude,depth,other_environmental,host_gender,host_age,host_health,body_sample_site,body_sample_subsite,other_clinical,antimicrobial_resistance,antimicrobial_resistance_evidence,gram_stain,cell_shape,motility,sporulation,temperature_range,salinity,oxygen_requirement,habitat,comments,additional_metadata");

				// paging
				query.setStart(start);
				if (end != -1) {
					query.setRows(end);
				}

				// parse sorting conditions
				if (request.getParameter("sort") != null) {
					JSONArray sorter;
					try {
						sorter = (JSONArray) new JSONParser().parse(request.getParameter("sort"));
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
				// set facet
				if (facet != null) {
					query.setFacet(true);
					query.setFacetMinCount(1);
					query.setFacetLimit(-1);
					query.setFacetSort(FacetParams.FACET_SORT_COUNT);

					try {
						JSONObject facet_data = (JSONObject) new JSONParser().parse(facet);

						String[] facetFields = facet_data.get("facet").toString().split(",");

						for (String facetField : facetFields) {
							if (!facetField.equals("completion_date") && !facetField.equals("release_date")) {
								query.addFacetField(facetField);
							}
							else {
								query.addDateRangeFacet(facetField, solr.getRangeStartDate(), solr.getRangeEndDate(), "+1YEAR");
							}
						}
					}
					catch (ParseException e) {
						LOGGER.error(e.getMessage(), e);
					}
				}

				// add join condition
				// if (key.containsKey("taxonId") && key.get("taxonId") != null) {
				if (taxonId != null && !taxonId.equals("")) {
					key.put("join", "taxon_lineage_ids:" + taxonId);
					query.addFilterQuery("taxon_lineage_ids:" + taxonId);
				}
				if (genomeId != null && !genomeId.equals("")) {
					key.put("join", "genome_id:(" + genomeId.replaceAll(",", " OR ") + ")");
					query.addFilterQuery("genome_id:(" + genomeId.replaceAll(",", " OR ") + ")");
				}

				JSONArray docs = new JSONArray();
				long numFound = 0l;
				try {
					LOGGER.debug("{}", query.toString());
					QueryResponse qr = solr.getSolrServer(SolrCore.GENOME).query(query, SolrRequest.METHOD.POST);
					List<Genome> records = qr.getBeans(Genome.class);
					numFound = qr.getResults().getNumFound();

					if (facet != null) {
						JSONObject facets = solr.facetFieldstoJSONObject(qr);
						key.put("facets", facets.toJSONString());
						SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, gson.toJson(key, ResultType.class));
					}

					for (Genome item : records) {
						docs.add(item.toJSONObject());
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

				solr.setCurrentInstance(SolrCore.GENOME);

				pk = request.getParameter("pk");
				key = gson.fromJson(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk), ResultType.class);

				if (key.containsKey("state")) {
					state = key.get("state");
				}
				else {
					state = request.getParameter("state");
				}

				key.put("state", state);

				SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, gson.toJson(key, ResultType.class));

				try {
					if (!key.containsKey("tree") && !key.get("facets").isEmpty()) {

						JSONObject facet_fields = (JSONObject) new JSONParser().parse(key.get("facets"));
						JSONArray arr1 = solr.processStateAndTree(key, need, facet_fields, key.get("facet"), state, key.get("join"), 4, false);
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
			else if (need.equals("tree_for_taxon")) {

				solr.setCurrentInstance(SolrCore.GENOME);
				pk = request.getParameter("pk");
				facet = request.getParameter("facet");
				keyword = request.getParameter("keyword");
				state = request.getParameter("state");

				String json = SessionHandler.getInstance().get(SessionHandler.PREFIX + pk);
				if (json == null) {
					key.put("facet", facet);
					key.put("keyword", keyword);
					key.put("state", state);

					SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, gson.toJson(key, ResultType.class));
				}
				else {
					key = gson.fromJson(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk), ResultType.class);
					key.put("facet", facet);
				}

				HashMap<String, String> sort = null;
				JSONObject object = solr.getData(key, sort, facet, 0, -1, true, false, false);

				if (!key.containsKey("tree")) {

					JSONObject facet_fields = (JSONObject) object.get("facets");
					JSONArray arr1 = solr.processStateAndTree(key, need, facet_fields, facet, key.get("state"), 4, false);
					jsonResult.put("results", arr1);

					key.put("tree", arr1);
				}
				else {
					jsonResult.put("results", key.get("tree"));
				}

				response.setContentType("application/json");
				PrintWriter writer = response.getWriter();
				writer.write(jsonResult.get("results").toString());
				writer.close();

			}
			else if (need.equals("getGenome")) {

				Genome genome = solr.getGenome(request.getParameter("id"));

				response.setContentType("application/json");
				PrintWriter writer = response.getWriter();
				if (genome != null) {
					genome.toJSONObject().writeJSONString(writer);
				}
				writer.close();
			}
			else if (need.equals("getIdsForCart")) {

				solr.setCurrentInstance(SolrCore.GENOME);

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
