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
import edu.vt.vbi.patric.beans.Taxonomy;
import edu.vt.vbi.patric.common.SessionHandler;
import edu.vt.vbi.patric.common.SiteHelper;
import edu.vt.vbi.patric.common.SolrCore;
import edu.vt.vbi.patric.common.SolrInterface;
import edu.vt.vbi.patric.dao.ResultType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AntibioticResistanceGeneSearch extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(AntibioticResistanceGeneSearch.class);

	SolrInterface solr = new SolrInterface();

	JSONParser jsonParser = new JSONParser();

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		response.setContentType("text/html");
		String mode = request.getParameter("display_mode");
		new SiteHelper().setHtmlMetaElements(request, response, "Antibiotic Resistance Gene Search");

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
			String exact_search_term = "";

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
				exact_search_term = key.get("exact_search_term");
			}

			request.setAttribute("contextType", contextType);
			request.setAttribute("contextId", contextId);
			request.setAttribute("pk", pk);
			request.setAttribute("taxonId", taxonId);
			request.setAttribute("genomeId", genomeId);
			request.setAttribute("keyword", keyword);
			request.setAttribute("exact_search_term", exact_search_term);

			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/antibiotic_resistance_gene_search_result.jsp");
		}
		else {

			boolean isLoggedInd = Downloads.isLoggedIn(request);
			request.setAttribute("isLoggedIn", isLoggedInd);

			String contextType = request.getParameter("context_type");
			String contextId = request.getParameter("context_id");
			Taxonomy taxonomy = null;
			String organismName = null;

			// LOGGER.debug("AntibioticResistanceGeneSearch: {}", contextId);

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

			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/antibiotic_resistance_gene_search.jsp");
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
			String taxonId = "";
			String cType = request.getParameter("context_type");
			String cId = request.getParameter("context_id");
			if (cType != null && cId != null && cType.equals("taxon") && !cId.equals("")) {
				taxonId = cId;
			}
			String keyword = request.getParameter("keyword");
			String state = request.getParameter("state");
			String ncbi_taxon_id = request.getParameter("ncbi_taxon_id");
			String exact_search_term = request.getParameter("exact_search_term");
			String search_on = request.getParameter("search_on");

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
			if (search_on != null) {
				key.put("search_on", search_on);
			}

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
			String facet, keyword, pk, state, taxonId;
			boolean hl;
			ResultType key = new ResultType();
			JSONObject jsonResult = new JSONObject();
			taxonId = request.getParameter("taxonId");

			if (need.equals("0") || need.equals("specialtygenemapping")) {

				solr.setCurrentInstance(SolrCore.SPECIALTY_GENE_MAPPING);

				pk = request.getParameter("pk");
				keyword = request.getParameter("keyword");
				facet = request.getParameter("facet");

				String highlight = request.getParameter("highlight");
				hl = Boolean.parseBoolean(highlight);

				String json = SessionHandler.getInstance().get(SessionHandler.PREFIX + pk);
				if (json == null) {
					key.put("facet", facet);
					key.put("keyword", keyword);

					SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, gson.toJson(key, ResultType.class));
				}
				else {
					key = gson.fromJson(json, ResultType.class);
					key.put("facet", facet);
					SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, gson.toJson(key, ResultType.class));
				}

				String start_id = request.getParameter("start");
				String limit = request.getParameter("limit");
				int start = Integer.parseInt(start_id);
				int end = Integer.parseInt(limit);

				Map<String, String> sort = null;
				if (request.getParameter("sort") != null) {
					// sorting
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
				key.put("fields",
						"genome_id,genome_name,taxon_id,feature_id,alt_locus_tag,refseq_locus_tag,gene,product,property,source,property_source,source_id,organism,function,classification,pmid,query_coverage,subject_coverage,identity,e_value,same_species,same_genus,same_genome,evidence");
				// add join condition
				if (taxonId != null && !taxonId.equals("")) {
					key.put("taxonId", taxonId);
				}
				if (key.containsKey("taxonId") && key.get("taxonId") != null) {
					key.put("join", SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + key.get("taxonId")));
				}

				JSONObject object = solr.getData(key, sort, facet, start, end, facet != null, hl, false);

				JSONObject obj = (JSONObject) object.get("response");
				JSONArray obj1 = (JSONArray) obj.get("docs");

				if (!key.containsKey("facets")) {
					if (object.containsKey("facets")) {
						JSONObject facets = (JSONObject) object.get("facets");
						key.put("facets", facets.toJSONString());
						SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, gson.toJson(key, ResultType.class));
					}
				}

				jsonResult.put("results", obj1);
				jsonResult.put("total", obj.get("numFound"));

				response.setContentType("application/json");
				PrintWriter writer = response.getWriter();
				jsonResult.writeJSONString(writer);
				writer.close();

			}
			else if (need.equals("tree")) {

				solr.setCurrentInstance(SolrCore.SPECIALTY_GENE_MAPPING);

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
					if (!key.containsKey("tree")) {
						JSONObject facet_fields = (JSONObject) jsonParser.parse(key.get("facets"));
						JSONArray arr1 = solr.processStateAndTree(key, need, facet_fields, key.get("facet"), state, key.get("join"), 10, false);
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
		}
	}
}
