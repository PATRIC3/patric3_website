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

public class ProteomicsListPortlet extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProteomicsListPortlet.class);

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
		PortletRequestDispatcher prd;

		SiteHelper.setHtmlMetaElements(request, response, "Proteomics Experiment List");
		response.setTitle("Proteomics Experiment List");
		String type = request.getParameter("context_type");

		if (type.equals("feature")) {
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/experiment/proteomics_list_feature.jsp");
		}
		else {
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/experiment/proteomics_list.jsp");
		}

		prd.include(request, response);
	}

	@SuppressWarnings("unchecked")
	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		String sraction = request.getParameter("sraction");

		if (sraction != null && sraction.equals("save_params")) {

			Map<String, String> key = new HashMap<>();

			String taxonId = "";
			String cType = request.getParameter("context_type");
			String cId = request.getParameter("context_id");
			if (cType != null && cId != null && cType.equals("taxon") && !cId.equals("")) {
				taxonId = cId;
			}
			String keyword = request.getParameter("keyword");
			String state = request.getParameter("state");

			if (!taxonId.equalsIgnoreCase("")) {
				key.put("taxonId", taxonId);
			}
			if (keyword != null) {
				key.put("keyword", keyword.trim());
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

			switch (need) {
			case "0": {

				String pk = request.getParameter("pk");
				//
				Map data = processExperimentTab(request);
				Map<String, String> key = (Map) data.get("key");
				int numFound = (Integer) data.get("numFound");
				List<Map> sdl = (List<Map>) data.get("experiments");

				JSONArray docs = new JSONArray();
				for (Map doc : sdl) {
					JSONObject item = new JSONObject();
					item.putAll(doc);
					docs.add(item);
				}

				if (data.containsKey("facets")) {
					JSONObject facets = FacetHelper.formatFacetTree((Map) data.get("facets"));
					key.put("facets", facets.toJSONString());
					SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, jsonWriter.writeValueAsString(key));
				}

				JSONObject jsonResult = new JSONObject();
				jsonResult.put("results", docs);
				jsonResult.put("total", numFound);

				response.setContentType("application/json");
				PrintWriter writer = response.getWriter();
				jsonResult.writeJSONString(writer);
				writer.close();

				break;
			}
			case "1": {

				String pk = request.getParameter("pk");
				//
				Map data = processProteinTab(request);
				Map<String, String> key = (Map) data.get("key");
				int numFound = (Integer) data.get("numFound");
				List<Map> sdl = (List<Map>) data.get("proteins");

				JSONArray docs = new JSONArray();
				for (Map doc : sdl) {
					JSONObject item = new JSONObject(doc);
					docs.add(item);
				}

				if (data.containsKey("facets")) {
					JSONObject facets = FacetHelper.formatFacetTree((Map) data.get("facets"));
					key.put("facets", facets.toJSONString());
					SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, jsonWriter.writeValueAsString(key));
				}

				JSONObject jsonResult = new JSONObject();
				jsonResult.put("results", docs);
				jsonResult.put("total", numFound);

				response.setContentType("application/json");
				PrintWriter writer = response.getWriter();
				writer.write(jsonResult.toString());
				writer.close();

				break;
			}
			case "tree": {

				String pk = request.getParameter("pk");
				String state;
				Map<String, String> key = jsonReader.readValue(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk));

				if (key.containsKey("state"))
					state = key.get("state");
				else
					state = request.getParameter("state");

				key.put("state", state);
				SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, jsonWriter.writeValueAsString(key));

				JSONArray tree = new JSONArray();
				try {
					if (!key.containsKey("facets") && !key.get("facets").isEmpty()) {
						JSONObject facet_fields = (JSONObject) new JSONParser().parse(key.get("facets"));
						DataApiHandler dataApi = new DataApiHandler(request);
						tree = FacetHelper.processStateAndTree(dataApi, SolrCore.PROTEOMICS_EXPERIMENT, key, need, facet_fields, key.get("facet"), state, null, 4);
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
			case "getFeatureIds": {

				String keyword = request.getParameter("keyword");
				Map<String, String> key = new HashMap<>();
				key.put("keyword", keyword);

				DataApiHandler dataApi = new DataApiHandler(request);

				SolrQuery query = dataApi.buildSolrQuery(key, null, null, 0, -1, false);

				String apiResponse = dataApi.solrQuery(SolrCore.PROTEOMICS_PROTEIN, query);

				Map resp = jsonReader.readValue(apiResponse);
				Map respBody = (Map) resp.get("response");

				JSONObject object = new JSONObject(respBody);
//				solr.setCurrentInstance(SolrCore.PROTEOMICS_PROTEIN);
//				JSONObject object = null; //solr.getData(key, null, facet, 0, -1, false, false, false);

				response.setContentType("application/json");
				PrintWriter writer = response.getWriter();
				object.writeJSONString(writer);
//				writer.write(object.get("response").toString());
				writer.close();

				break;
			}
			case "getPeptides": {

				String experiment_id = request.getParameter("experiment_id");
				String na_feature_id = request.getParameter("na_feature_id");

				Map<String, String> key = new HashMap<>();
				key.put("keyword", "na_feature_id:" + na_feature_id + " AND experiment_id:" + experiment_id);
				key.put("fields", "peptide_sequence");

				DataApiHandler dataApi = new DataApiHandler(request);

				SolrQuery query = dataApi.buildSolrQuery(key, null, null, 0, -1, false);

				String apiResponse = dataApi.solrQuery(SolrCore.PROTEOMICS_PEPTIDE, query);

				Map resp = jsonReader.readValue(apiResponse);
				Map respBody = (Map) resp.get("response");

				JSONObject object = new JSONObject();
				object.putAll(respBody);

//				solr.setCurrentInstance(SolrCore.PROTEOMICS_PEPTIDE);
//				JSONObject object = solr.getData(key, null, facet, 0, -1, false, false, false);
//				object = (JSONObject) object.get("response");
				object.put("aa", FASTAHelper.getFASTASequence(Arrays.asList(na_feature_id), "protein"));

				response.setContentType("application/json");
				PrintWriter writer = response.getWriter();
				object.writeJSONString(writer);
				writer.close();
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
		Map<String, String> key = new HashMap<>();

		boolean hl = Boolean.parseBoolean(highlight);

		if (pk != null) {
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
		}
		else {
			key.put("keyword", keyword);
		}

		String start_id = request.getParameter("start");
		String limit = request.getParameter("limit");
		int start = Integer.parseInt(start_id);
		int end = Integer.parseInt(limit);

		DataApiHandler dataApi = new DataApiHandler(request);
		SolrQuery query = dataApi.buildSolrQuery(key, sort, facet, start, end, hl);

		LOGGER.trace("[{}] {}", SolrCore.PROTEOMICS_EXPERIMENT.getSolrCoreName(), query);
		String apiResponse = dataApi.solrQuery(SolrCore.PROTEOMICS_EXPERIMENT, query);

		Map resp = jsonReader.readValue(apiResponse);
		Map respBody = (Map) resp.get("response");

		int numFound = (Integer) respBody.get("numFound");
		List<Map> sdl = (List<Map>) respBody.get("docs");

		Map response = new HashMap();
		response.put("key", key);
		response.put("numFound", numFound);
		response.put("experiments", sdl);
		if (resp.containsKey("facet_counts")) {
			response.put("facets", resp.get("facet_counts"));
		}

		return response;
	}

	private Map processProteinTab(ResourceRequest request) throws IOException {

		String pk = request.getParameter("pk");
		String keyword = request.getParameter("keyword");
		String experiment_id = request.getParameter("experiment_id");
		String facet = request.getParameter("facet");
		String sort = request.getParameter("sort");
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

		DataApiHandler dataApi = new DataApiHandler(request);
		String orig_keyword = key.get("keyword");

		if (experiment_id != null && !experiment_id.equals("")) {
			key.put("keyword", "experiment_id: (" + experiment_id.replaceAll(",", " OR ") + ")");
		}
		else if (experiment_id != null && experiment_id.equals("")) {

			List<String> experimentIds = new ArrayList<>();

			SolrQuery query = dataApi.buildSolrQuery(key, sort, facet, 0, -1, false);

			LOGGER.trace("[{}] {}", SolrCore.PROTEOMICS_EXPERIMENT.getSolrCoreName(), query);
			String apiResponse = dataApi.solrQuery(SolrCore.PROTEOMICS_EXPERIMENT, query);

			Map resp = jsonReader.readValue(apiResponse);
			Map respBody = (Map) resp.get("response");

			List<Map> sdl = (List<Map>) respBody.get("docs");
			for (Map doc : sdl) {
				experimentIds.add((String) doc.get("experiment_id"));
			}
//			solr.setCurrentInstance(SolrCore.PROTEOMICS_EXPERIMENT);
//			JSONObject object = null; //solr.getData(key, null, facet, 0, 10000, true, false, false);
//
//			JSONObject obj = (JSONObject) object.get("response");
//			JSONArray obj1 = (JSONArray) obj.get("docs");
//
//			for (Object ob : obj1) {
//				JSONObject doc = (JSONObject) ob;
//				if (solrId.length() == 0) {
//					solrId += doc.get("experiment_id").toString();
//				}
//				else {
//					solrId += "," + doc.get("experiment_id").toString();
//				}
//			}

			key.put("keyword", "experiment_id: (" + StringUtils.join(experimentIds, " OR ") + ")");

			if (resp.containsKey("facet_counts")) {
				JSONObject facets = FacetHelper.formatFacetTree((Map) resp.get("facet_counts"));
				key.put("facets", facets.toJSONString());
				SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, jsonWriter.writeValueAsString(key));
			}
//			JSONObject facets = (JSONObject) object.get("facets");
//			if (facets != null) {
//				key.put("facets", facets.toString());
//			}
		}

		String start_id = request.getParameter("start");
		String limit = request.getParameter("limit");
		int start = Integer.parseInt(start_id);
		int end = Integer.parseInt(limit);

		SolrQuery query = dataApi.buildSolrQuery(key, sort, facet, start, end, false);

		LOGGER.trace("[{}] {}", SolrCore.PROTEOMICS_PROTEIN.getSolrCoreName(), query);

		String apiResponse = dataApi.solrQuery(SolrCore.PROTEOMICS_PROTEIN, query);

		Map resp = jsonReader.readValue(apiResponse);
		Map respBody = (Map) resp.get("response");

		int numFound = (Integer) respBody.get("numFound");
		List<Map> sdl = (List<Map>) respBody.get("docs");

		key.put("keyword", orig_keyword);

		Map response = new HashMap();
		response.put("key", key);
		response.put("numFound", numFound);
		response.put("proteins", sdl);

		return response;
	}
}
