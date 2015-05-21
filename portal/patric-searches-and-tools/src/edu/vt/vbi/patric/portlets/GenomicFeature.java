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
import edu.vt.vbi.patric.beans.GenomeFeature;
import edu.vt.vbi.patric.beans.Taxonomy;
import edu.vt.vbi.patric.common.*;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.codehaus.jackson.map.ObjectWriter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class GenomicFeature extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(GenomicFeature.class);

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
		String mode = request.getParameter("display_mode");
		SiteHelper.setHtmlMetaElements(request, response, "Feature Finder");

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
			// Feature Finder Search Interface
			boolean isLoggedInd = Downloads.isLoggedIn(request);
			request.setAttribute("isLoggedIn", isLoggedInd);

			String contextType = request.getParameter("context_type");
			String contextId = request.getParameter("context_id");
			Taxonomy taxonomy = null;
			String organismName = null;
			List<String> featureTypes = new ArrayList<>();

			DataApiHandler dataApi = new DataApiHandler(request);

			if (contextType.equals("taxon")) {
				taxonomy = dataApi.getTaxonomy(Integer.parseInt(contextId));
				organismName = taxonomy.getTaxonName();

				Map resp = dataApi.getFieldFacets(SolrCore.FEATURE, "*:*",
						SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + taxonomy.getId()), "feature_type");
				featureTypes.addAll(((Map) ((Map) resp.get("facets")).get("feature_type")).keySet());
			}
			else if (contextType.equals("genome")) {
				Genome genome = dataApi.getGenome(contextId);
				taxonomy = dataApi.getTaxonomy(genome.getTaxonId());
				organismName = genome.getGenomeName();

				Map resp = dataApi.getFieldFacets(SolrCore.FEATURE, ("genome_id:" + genome.getId()), null, "feature_type");
				featureTypes.addAll(((Map) ((Map) resp.get("facets")).get("feature_type")).keySet());
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
	public void serveResource(ResourceRequest request, ResourceResponse response)
			throws PortletException, IOException {

		String sraction = request.getParameter("sraction");

		if (sraction != null && sraction.equals("save_params")) {

			LOGGER.debug("{}", request.getParameterMap());

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

			SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, jsonWriter.writeValueAsString(key));

			PrintWriter writer = response.getWriter();
			writer.write("" + pk);
			writer.close();

		}
		else {

			String need = request.getParameter("need");
			JSONObject jsonResult = new JSONObject();

			switch (need) {
			case "feature":
			case "featurewofacet": {
				// Getting Feature List
				Map data = processFeatureTab(request);

				int numFound = (Integer) data.get("numFound");
				List<GenomeFeature> records = (List<GenomeFeature>) data.get("features");

				JSONArray docs = new JSONArray();
				for (GenomeFeature item : records) {
					docs.add(item.toJSONObject());
				}

				jsonResult.put("results", docs);
				jsonResult.put("total", numFound);

				response.setContentType("application/json");
				PrintWriter writer = response.getWriter();
				jsonResult.writeJSONString(writer);
				writer.close();

				break;
			}
			case "download": {
				List<String> tableHeader = new ArrayList<>();
				List<String> tableField = new ArrayList<>();
				JSONArray tableSource = new JSONArray();

				String fileName = "FeatureTable";
				String fileFormat = request.getParameter("fileformat");

				// features
				Map data = processFeatureTab(request);
				List<GenomeFeature> features = (List<GenomeFeature>) data.get("features");

				for (GenomeFeature feature : features) {
					tableSource.add(feature.toJSONObject());
				}

				tableHeader.addAll(DownloadHelper.getHeaderForFeatures());
				tableField.addAll(DownloadHelper.getFieldsForFeatures());

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

	private Map processFeatureTab(ResourceRequest request) throws IOException {

		String pk = request.getParameter("pk");
		String keyword = request.getParameter("keyword");
		String sort = request.getParameter("sort");
		String taxonId = request.getParameter("taxonId");
		String genomeId = request.getParameter("genomeId");

		Map<String, String> key = new HashMap<>();

		if (pk != null) {
			String json = SessionHandler.getInstance().get(SessionHandler.PREFIX + pk);
			if (json == null) {
				key.put("keyword", keyword);

				SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, jsonWriter.writeValueAsString(key));
			}
			else {
				key = jsonReader.readValue(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk));
			}
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

		key.put("fields", StringUtils.join(DownloadHelper.getFieldsForFeatures(), ","));

		// add join condition
		if (taxonId != null && !taxonId.equals("")) {
			key.put("join", SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + taxonId));
		}
		if (genomeId != null && !genomeId.equals("")) {
			key.put("join", "genome_id:" + genomeId);
		}

		DataApiHandler dataApi = new DataApiHandler(request);

		SolrQuery query = dataApi.buildSolrQuery(key, sort, null, start, end, false);

		LOGGER.debug("query: {}", query.toString());

		String apiResponse = dataApi.solrQuery(SolrCore.FEATURE, query);

		Map resp = jsonReader.readValue(apiResponse);
		Map respBody = (Map) resp.get("response");

		int numFound = (Integer) respBody.get("numFound");
		List<GenomeFeature> features = dataApi.bindDocuments((List<Map>) respBody.get("docs"), GenomeFeature.class);

		Map response = new HashMap();
		response.put("key", key);
		response.put("numFound", numFound);
		response.put("features", features);

		return response;
	}
}
