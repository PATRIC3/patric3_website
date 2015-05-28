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
import java.util.*;

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
			JSONObject jsonResult = new JSONObject();

			switch (need) {
			case "0":
			case "specialtygenemapping": {

				String pk = request.getParameter("pk");
				Map data = processSpecialtyGeneTab(request);

				Map<String, String> key = (Map) data.get("key");
				int numFound = (Integer) data.get("numFound");
				List<SpecialtyGene> records = (List<SpecialtyGene>) data.get("specialtyGenes");

				JSONArray docs = new JSONArray();
				for (SpecialtyGene item : records) {
					docs.add(item.toJSONObject());
				}

				if (data.containsKey("facets")) {
					JSONObject facets = FacetHelper.formatFacetTree((Map) data.get("facets"));
					key.put("facets", facets.toJSONString());
					SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, jsonWriter.writeValueAsString(key));
				}

				jsonResult.put("results", docs);
				jsonResult.put("total", numFound);

				response.setContentType("application/json");
				PrintWriter writer = response.getWriter();
				jsonResult.writeJSONString(writer);
				writer.close();
				break;
			}
			case "tree": {

				String pk = request.getParameter("pk");
				String state;
				Map<String, String> key = jsonReader.readValue(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk));

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
						DataApiHandler dataApi = new DataApiHandler(request);
						tree = FacetHelper.processStateAndTree(dataApi, SolrCore.SPECIALTY_GENE_MAPPING, key, need, facet_fields, key.get("facet"), state, key.get("join"), 10);
					}
				}
				catch (ParseException e) {
					LOGGER.error(e.getMessage(), e);
				}

				response.setContentType("application/json");
				PrintWriter writer = response.getWriter();
				tree.writeJSONString(writer);
				writer.close();
				break;
			}
			case "download": {
				List<String> tableHeader = new ArrayList<>();
				List<String> tableField = new ArrayList<>();
				JSONArray tableSource = new JSONArray();

				String fileName = "SpecialtyGene";
				String fileFormat = request.getParameter("fileformat");

				Map data = processSpecialtyGeneTab(request);
				List<SpecialtyGene> specialtygenes = (List<SpecialtyGene>) data.get("specialtyGenes");

				for (SpecialtyGene gene : specialtygenes) {
					tableSource.add(gene.toJSONObject());
				}

				tableHeader.addAll(DownloadHelper.getHeaderForSpecialtyGeneMapping());
				tableField.addAll(DownloadHelper.getFieldsForSpecialtyGeneMapping());

				ExcelHelper excel = new ExcelHelper("xssf", tableHeader, tableField, tableSource);
				excel.buildSpreadsheet();

				if (fileFormat.equalsIgnoreCase("xlsx")) {
					response.setContentType("application/octetstream");
					response.addProperty("Content-Disposition", "attachment; filename=\"" + fileName + "." + fileFormat + "\"");

					excel.writeSpreadsheettoBrowser(response.getPortletOutputStream());
				}
				else if (fileFormat.equalsIgnoreCase("txt")) {

					response.setContentType("application/octetstream");
					response.addProperty("Content-Disposition", "attachment; filename=\"" + fileName + "." + fileFormat + "\"");

					response.getPortletOutputStream().write(excel.writeToTextFile().getBytes());
				}
			}
			}
		}
	}

	private Map processSpecialtyGeneTab(ResourceRequest request) throws IOException {
		String pk = request.getParameter("pk");
		String keyword = request.getParameter("keyword");
		String facet = request.getParameter("facet");
		String sort = request.getParameter("sort");
		String taxonId = request.getParameter("taxonId");
		String genomeId = request.getParameter("genomeId");

		String highlight = request.getParameter("highlight");
		boolean hl = Boolean.parseBoolean(highlight);
		Map<String, String> key = new HashMap<>();

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
		int start = 0;
		int end = -1;
		if (start_id != null) {
			start = Integer.parseInt(start_id);
		}
		if (limit != null) {
			end = Integer.parseInt(limit);
		}

		key.put("fields",
				"genome_id,genome_name,taxon_id,feature_id,patric_id,alt_locus_tag,refseq_locus_tag,gene,product,property,source,property_source,source_id,organism,function,classification,pmid,query_coverage,subject_coverage,identity,e_value,same_species,same_genus,same_genome,evidence");

		// add join condition
		if (taxonId != null && !taxonId.equals("")) {
			key.put("join", SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + taxonId));
		}
		if (genomeId != null && !genomeId.equals("")) {
			key.put("join", "genome_id:" + genomeId);
		}

		DataApiHandler dataApi = new DataApiHandler(request);
		SolrQuery query = dataApi.buildSolrQuery(key, sort, facet, start, end, hl);

		LOGGER.debug("query: {}", query.toString());

		String apiResponse = dataApi.solrQuery(SolrCore.SPECIALTY_GENE_MAPPING, query);

		Map resp = jsonReader.readValue(apiResponse);
		Map respBody = (Map) resp.get("response");

		int numFound = (Integer) respBody.get("numFound");
		List<SpecialtyGene> specialtyGenes = dataApi.bindDocuments((List<Map>) respBody.get("docs"), SpecialtyGene.class);

		Map response = new HashMap();
		response.put("key", key);
		response.put("numFound", numFound);
		response.put("specialtyGenes", specialtyGenes);
		if (resp.containsKey("facet_counts")) {
			response.put("facets", resp.get("facet_counts"));
		}

		return response;
	}
}
