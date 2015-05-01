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

import edu.vt.vbi.patric.beans.Genome;
import edu.vt.vbi.patric.beans.SpecialtyGene;
import edu.vt.vbi.patric.beans.Taxonomy;
import edu.vt.vbi.patric.common.*;
import org.apache.solr.client.solrj.SolrQuery;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.codehaus.jackson.map.ObjectWriter;
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
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SpecialtyGeneSearch extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpecialtyGeneSearch.class);

	ObjectReader jsonReader;

	ObjectWriter jsonWriter;

	@Override
	public void init() throws PortletException {
		super.init();

		ObjectMapper objectMapper = new ObjectMapper();
		jsonReader = objectMapper.reader(Map.class);
		jsonWriter = objectMapper.writerWithType(Map.class);
	}

	JSONParser jsonParser = new JSONParser();

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		response.setContentType("text/html");
		String mode = request.getParameter("display_mode");
		SiteHelper.setHtmlMetaElements(request, response, "Specialty Gene Search");

		PortletRequestDispatcher prd;
		if (mode != null && mode.equals("result")) {

			String contextType = request.getParameter("context_type");
			String contextId = request.getParameter("context_id");
			String pk = request.getParameter("param_key");

			Map<String, String> key = jsonReader.readValue(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk));

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

			request.setAttribute("contextType", contextType);
			request.setAttribute("contextId", contextId);
			request.setAttribute("pk", pk);
			request.setAttribute("taxonId", taxonId);
			request.setAttribute("genomeId", genomeId);
			request.setAttribute("keyword", keyword);
			request.setAttribute("exactSearchTerm", exactSearchTerm);

			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/specialty_gene_search_result.jsp");
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

			DataApiHandler dataApi = new DataApiHandler(request);

			if (contextType.equals("taxon")) {
				taxonomy = dataApi.getTaxonomy(Integer.parseInt(contextId));
				organismName = taxonomy.getTaxonName();
			}
			else if (contextType.equals("genome")) {
				Genome genome = dataApi.getGenome(contextId);
				taxonomy = dataApi.getTaxonomy(genome.getTaxonId());
				organismName = genome.getGenomeName();
			}

			request.setAttribute("taxonId", taxonomy.getId());
			request.setAttribute("organismName", organismName);
			request.setAttribute("cType", contextType);
			request.setAttribute("cId", contextId);

			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/specialty_gene_search.jsp");
		}
		prd.include(request, response);
	}

	@SuppressWarnings("unchecked") public void serveResource(ResourceRequest request, ResourceResponse response)
			throws PortletException, IOException {

		String sraction = request.getParameter("sraction");

		if (sraction != null && sraction.equals("save_params")) {

			Map<String, String> key = new HashMap<>();

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
			// random
			long pk = (new Random()).nextLong();

			SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, jsonWriter.writeValueAsString(key));

			PrintWriter writer = response.getWriter();
			writer.write("" + pk);
			writer.close();
		}
		else if (sraction != null && sraction.equals("get_params")) {
			String ret = "";
			String pk = request.getParameter("pk");

			String json = SessionHandler.getInstance().get(SessionHandler.PREFIX + pk);
			if (json != null) {
				Map<String, String> key = jsonReader.readValue(json);
				ret = key.get("keyword");
			}

			PrintWriter writer = response.getWriter();
			writer.write("" + ret);
			writer.close();
		}
		else {

			String need = request.getParameter("need");
			String facet, sort, keyword, pk, state, taxonId, genomeId;
			boolean hl;

			Map<String, String> key = new HashMap<>();
			JSONObject jsonResult = new JSONObject();

			if (need.equals("0") || need.equals("specialtygenemapping")) {

				pk = request.getParameter("pk");
				keyword = request.getParameter("keyword");
				facet = request.getParameter("facet");
				sort = request.getParameter("sort");
				taxonId = request.getParameter("taxonId");
				genomeId = request.getParameter("genomeId");

				String highlight = request.getParameter("highlight");
				hl = Boolean.parseBoolean(highlight);

				String json = SessionHandler.getInstance().get(SessionHandler.PREFIX + pk);
				if (json == null) {
					key.put("facet", facet);
					key.put("keyword", keyword);

					SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, jsonWriter.writeValueAsString(key));
				}
				else {
					key = jsonReader.readValue(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk));
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

				key.put("fields",
						"genome_id,genome_name,taxon_id,feature_id,seed_id,alt_locus_tag,refseq_locus_tag,gene,product,property,source,property_source,source_id,organism,function,classification,pmid,query_coverage,subject_coverage,identity,e_value,same_species,same_genus,same_genome,evidence");

				// add join condition
				if (taxonId != null && !taxonId.equals("")) {
					key.put("join", SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + taxonId));
				}
				if (genomeId != null && !genomeId.equals("")) {
					key.put("join", "genome_id:" + genomeId);
				}

				SolrInterface solr = new SolrInterface();
				SolrQuery query = solr.buildSolrQuery(key, sort, facet, start, end, hl);

				LOGGER.debug("query: {}", query.toString());
				DataApiHandler dataApi = new DataApiHandler(request);
				String apiResponse = dataApi.solrQuery(SolrCore.SPECIALTY_GENE_MAPPING, query);

				Map<String, Object> resp = jsonReader.readValue(apiResponse);
				Map<String, Object> respBody = (Map<String, Object>) resp.get("response");

				int numFound = (Integer) respBody.get("numFound");
				JSONArray docs = new JSONArray();
				List<SpecialtyGene> records = dataApi.bindDocuments((List<Map>) respBody.get("docs"), SpecialtyGene.class);
				for (SpecialtyGene item : records) {
					docs.add(item.toJSONObject());
				}

				if (resp.containsKey("facet_counts")) {
					JSONObject facets = solr.formatFacetTree((Map) resp.get("facet_counts"));
					key.put("facets", facets.toJSONString());
					SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, jsonWriter.writeValueAsString(key));
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
				key = jsonReader.readValue(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk));

				if (key.containsKey("state")) {
					state = key.get("state");
				}
				else {
					state = request.getParameter("state");
				}

				key.put("state", state);

				SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, jsonWriter.writeValueAsString(key));

				JSONArray tree = new JSONArray();
				try {
					if (key.containsKey("facets") && !key.get("facets").isEmpty()) {

						JSONObject facet_fields = (JSONObject) jsonParser.parse(key.get("facets"));
						SolrInterface solr = new SolrInterface();
						solr.setCurrentInstance(SolrCore.SPECIALTY_GENE_MAPPING);
						tree = solr.processStateAndTree(key, need, facet_fields, key.get("facet"), state, key.get("join"), 10, false);
					}
				}
				catch (ParseException e) {
					LOGGER.error(e.getMessage(), e);
				}

				response.setContentType("application/json");
				PrintWriter writer = response.getWriter();
				tree.writeJSONString(writer);
				writer.close();
			}
		}
	}
}
