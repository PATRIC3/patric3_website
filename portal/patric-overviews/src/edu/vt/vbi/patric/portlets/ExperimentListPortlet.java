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

import edu.vt.vbi.patric.common.*;
import org.apache.commons.lang.StringUtils;
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

public class ExperimentListPortlet extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExperimentListPortlet.class);

	private ObjectReader jsonReader;

	private ObjectWriter jsonWriter;

	@Override
	public void init() throws PortletException {
		super.init();

		ObjectMapper objectMapper = new ObjectMapper();
		jsonReader = objectMapper.reader(Map.class);
		jsonWriter = objectMapper.writerWithType(Map.class);
	}

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		response.setContentType("text/html");

		SiteHelper.setHtmlMetaElements(request, response, "Experiment List");
		response.setTitle("Experiment List");

		String cType = request.getParameter("context_type");
		String cId = request.getParameter("context_id");

		if (cType != null && cId != null) {

			String kw = (request.getParameter("keyword") != null) ? request.getParameter("keyword") : "";
			if (kw != null && (kw.startsWith("/") || kw.startsWith("#"))) {
				kw = "";
			}

			String keyword = "(*)";
			String filter;
			String eid;

			if (cType.equals("taxon") && cId.equals("2")) {
				filter = "*";
				eid = "";
			}
			else {
				DataApiHandler dataApi = new DataApiHandler(request);
				List<String> items = new ArrayList<>();

				SolrQuery query = new SolrQuery();

				if (cType.equals("taxon")) {
					query.setQuery("*:*");
					query.setFilterQueries(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_ids", "taxon_lineage_ids:" + cId));
				}
				else if (cType.equals("genome")) {
					query = new SolrQuery("genome_ids:" + cId);
				}

				query.setRows(10000).setFields("eid");
				String apiResponse = dataApi.solrQuery(SolrCore.TRANSCRIPTOMICS_EXPERIMENT, query);

				Map resp = jsonReader.readValue(apiResponse);
				Map respBody = (Map) resp.get("response");
				List<Map> sdl = (List<Map>) respBody.get("docs");

				for (Map doc : sdl) {
					items.add(doc.get("eid").toString());
				}

				if (items.size() > 0) {
					eid = StringUtils.join(items, "##");
				}
				else {
					eid = "0";
				}
				filter = eid;
			}

			request.setAttribute("keyword", keyword);
			request.setAttribute("kw", kw);
			request.setAttribute("eid", eid);
			request.setAttribute("filter", filter);

			PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/experiment/list.jsp");
			prd.include(request, response);
		}
	}

	@SuppressWarnings("unchecked")
	public void serveResource(ResourceRequest request, ResourceResponse response)
			throws PortletException, IOException {

		String sraction = request.getParameter("sraction");

		if (sraction != null && sraction.equals("save_params")) {

			Map<String, String> key = new HashMap<>();

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
			long pk = (new Random()).nextLong();

			SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, jsonWriter.writeValueAsString(key));

			PrintWriter writer = response.getWriter();
			writer.write("" + pk);
			writer.close();

		}
		else {

			String need = request.getParameter("need");
			JSONObject jsonResult = new JSONObject();

			switch (need) {
			case "0": {
				// Experiments
				String pk = request.getParameter("pk");
				Map data = processExperimentTab(request);

				Map<String, String> key = (Map) data.get("key");
				int numFound = (Integer) data.get("numFound");
				List<Map> experiments = (List<Map>) data.get("experiments");

				JSONArray docs = new JSONArray();
				for (Map item : experiments) {
					JSONObject doc = new JSONObject();
					doc.putAll(item);
					docs.add(doc);
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
				writer.write(jsonResult.toString());
				writer.close();

				break;
			}
			case "1": {
				// Comparisons
				Map data = processComparisonTab(request);

				int numFound = (Integer) data.get("numFound");
				List<Map> comparisons = (List<Map>) data.get("comparisons");

				JSONArray docs = new JSONArray();
				for (Map item : comparisons) {
					JSONObject doc = new JSONObject();
					doc.putAll(item);
					docs.add(doc);
				}

//				key.put("keyword", orig_keyword);

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
				Map<String, String> key = jsonReader.readValue(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk));
				String state;

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
					if (key.containsKey("facets") && !key.get("facets").equals("{}")) {

						JSONObject facet_fields = (JSONObject) new JSONParser().parse(key.get("facets"));
						DataApiHandler dataApi = new DataApiHandler(request);
						tree = FacetHelper.processStateAndTree(dataApi, SolrCore.TRANSCRIPTOMICS_EXPERIMENT, key, need, facet_fields, key.get("facet"), state, null, 4);
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

				String fileName = "Transcriptomics";
				String fileFormat = request.getParameter("fileformat");

				String aT = request.getParameter("aT");

				switch (aT) {
				case "0": {
					// experiments
					Map data = processExperimentTab(request);
					List<Map> experiments = (List<Map>) data.get("experiments");

					for (Map item : experiments) {
						JSONObject doc = new JSONObject();
						doc.putAll(item);
						tableSource.add(doc);
					}

					tableHeader.addAll(DownloadHelper.getHeaderForTranscriptomicsExperiment());
					tableField.addAll(DownloadHelper.getFieldsForTranscriptomicsExperiment());
					break;
				}
				case "1": {
					// comparisons
					Map data = processComparisonTab(request);
					List<Map> comparisons = (List<Map>) data.get("comparisons");

					for (Map item : comparisons) {
						JSONObject doc = new JSONObject();
						doc.putAll(item);
						tableSource.add(doc);
					}

					tableHeader.addAll(DownloadHelper.getHeaderForTranscriptomicsComparison());
					tableField.addAll(DownloadHelper.getFieldsForTranscriptomicsComparison());
					break;
				}
				}

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
				break;
			}
			}
		}
	}

	private Map processExperimentTab(ResourceRequest request) throws IOException {

		String pk = request.getParameter("pk");
		String keyword = request.getParameter("keyword");
		String facet = request.getParameter("facet");
		String sort = request.getParameter("sort");
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
				"eid,expid,accession,institution,pi,author,pmid,release_date,title,organism,strain,mutant,timeseries,condition,samples,platform,genes");

		DataApiHandler dataApi = new DataApiHandler(request);

		SolrQuery query = dataApi.buildSolrQuery(key, sort, facet, start, end, hl);

		LOGGER.debug("[{}] {}", SolrCore.TRANSCRIPTOMICS_EXPERIMENT.getSolrCoreName(), query.toString());

		String apiResponse = dataApi.solrQuery(SolrCore.TRANSCRIPTOMICS_EXPERIMENT, query);

		Map resp = jsonReader.readValue(apiResponse);
		Map respBody = (Map) resp.get("response");

		int numFound = (Integer) respBody.get("numFound");
		List<Map> experiments = (List<Map>) respBody.get("docs");

		Map response = new HashMap();
		response.put("key", key);
		response.put("numFound", numFound);
		response.put("experiments", experiments);
		if (resp.containsKey("facet_counts")) {
			response.put("facets", resp.get("facet_counts"));
		}

		return response;
	}

	private Map processComparisonTab(ResourceRequest request) throws IOException {

		String pk = request.getParameter("pk");
		String keyword = request.getParameter("keyword");
		String eId = request.getParameter("eId");
		String facet = request.getParameter("facet");
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

//		String orig_keyword = key.get("keyword");
		DataApiHandler dataApi = new DataApiHandler(request);

		if (eId != null && !eId.equals("")) {
			key.put("keyword", "eid: (" + eId.replaceAll(",", " OR ") + ")");

		}
		else if (eId != null && eId.equals("")) {

			List<String> eIdList = new ArrayList<>();

			SolrQuery query = dataApi.buildSolrQuery(key, null, facet, 0, 10000, false);
			String apiResponse = dataApi.solrQuery(SolrCore.TRANSCRIPTOMICS_EXPERIMENT, query);

			Map resp = jsonReader.readValue(apiResponse);
			Map respBody = (Map) resp.get("response");

			List<Map> sdl = (List<Map>) respBody.get("docs");
			for (Map doc : sdl) {
				eIdList.add(doc.get("eid").toString());
			}

			key.put("keyword", "eid: (" + StringUtils.join(eIdList, " OR ") + ")");

			if (resp.containsKey("facet_counts")) {
				JSONObject facets = FacetHelper.formatFacetTree((Map) resp.get("facet_counts"));
				key.put("facets", facets.toJSONString());
				SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, jsonWriter.writeValueAsString(key));
			}
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

		String sort = request.getParameter("sort");

		key.put("fields",
				"eid,expid,accession,pid,samples,expname,release_date,pmid,organism,strain,mutant,timepoint,condition,genes,sig_log_ratio,sig_z_score");

		SolrQuery query = dataApi.buildSolrQuery(key, sort, null, start, end, false);

		String apiResponse = dataApi.solrQuery(SolrCore.TRANSCRIPTOMICS_COMPARISON, query);

		Map resp = jsonReader.readValue(apiResponse);
		Map respBody = (Map) resp.get("response");
		List<Map> comparisons = (List<Map>) respBody.get("docs");

		int numFound = (Integer) respBody.get("numFound");

		Map response = new HashMap();
		response.put("key", key);
		response.put("numFound", numFound);
		response.put("comparisons", comparisons);

		return response;
	}
}
