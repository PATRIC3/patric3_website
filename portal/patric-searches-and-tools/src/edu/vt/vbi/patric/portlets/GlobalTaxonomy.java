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

public class GlobalTaxonomy extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalTaxonomy.class);

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
	}

	@SuppressWarnings("unchecked")
	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		String sraction = request.getParameter("sraction");

		if (sraction != null && sraction.equals("save_params")) {

			String search_on = request.getParameter("search_on");
			String keyword = request.getParameter("keyword");
			String taxonId = request.getParameter("taxonId");
			String genomeId = request.getParameter("genomeId");
			String algorithm = request.getParameter("algorithm");
			String exact_search_term = request.getParameter("exact_search_term");

			Map<String, String> key = new HashMap<>();

			if (search_on != null) {
				key.put("search_on", search_on.trim());
			}

			key.put("keyword", keyword.trim());

			if (taxonId != null && !taxonId.equalsIgnoreCase("")) {
				key.put("taxonId", taxonId);
			}

			if (genomeId != null && !genomeId.equalsIgnoreCase("")) {
				key.put("genomeId", genomeId);
			}

			if (algorithm != null && !algorithm.equalsIgnoreCase("")) {
				key.put("algorithm", algorithm);
			}

			if (exact_search_term != null) {
				key.put("exact_search_term", exact_search_term);
			}

			long pk = (new Random()).nextLong();

			SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, jsonWriter.writeValueAsString(key));

			PrintWriter writer = response.getWriter();
			writer.write("" + pk);
			writer.close();
		}
		else {

			String need = request.getParameter("need");
			String facet, keyword, pk, state;
			boolean hl;

			Map<String, String> key = new HashMap<>();

			if (need.equals("taxonomy")) {

				DataApiHandler dataApi = new DataApiHandler(request);
//				solr.setCurrentInstance(SolrCore.TAXONOMY);

				pk = request.getParameter("pk");
				keyword = request.getParameter("keyword");
				facet = request.getParameter("facet");
				String highlight = request.getParameter("highlight");
				hl = Boolean.parseBoolean(highlight);
				String sort = request.getParameter("sort");

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
				int start = Integer.parseInt(start_id);
				int end = Integer.parseInt(limit);

				// sorting
//				String sort_field = request.getParameter("sort");
//				String sort_dir = request.getParameter("dir");
//
//				Map<String, String> sort = null;
//
//				if (sort_field != null && sort_dir != null && !sort_field.equals("") && !sort_dir.equals("")) {
//					sort = new HashMap<>();
//					sort.put("field", sort_field);
//					sort.put("direction", sort_dir);
//				}

				SolrQuery query = dataApi.buildSolrQuery(key, sort, facet, start, end, hl);

				LOGGER.trace("GlobalTaxonomy: [{}] {}", SolrCore.TAXONOMY.getSolrCoreName(), query);
				String apiResponse = dataApi.solrQuery(SolrCore.TAXONOMY, query);

				Map resp = jsonReader.readValue(apiResponse);
				Map respBody = (Map) resp.get("response");

				int numFound = (Integer) respBody.get("numFound");
				List<Taxonomy> sdl = dataApi.bindDocuments((List<Map>) respBody.get("docs"), Taxonomy.class);

				JSONArray docs = new JSONArray();
				for (Taxonomy taxonomy : sdl) {
					docs.add(taxonomy.toJSONObject());
				}

				if (resp.containsKey("facet_counts")) {
					JSONObject facets = FacetHelper.formatFacetTree((Map) resp.get("facet_counts"));
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
			}
			else if (need.equals("tree")) {

				pk = request.getParameter("pk");
				key = jsonReader.readValue(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk));
				state = request.getParameter("state");
				key.put("state", state);
				SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, jsonWriter.writeValueAsString(key));

				JSONArray tree = new JSONArray();
				try {
					if (key.containsKey("facets") && !key.get("facets").isEmpty()) {
						DataApiHandler dataApi = new DataApiHandler(request);
						JSONObject facet_fields = (JSONObject) new JSONParser().parse(key.get("facets"));
						tree = FacetHelper.processStateAndTree(dataApi, SolrCore.TAXONOMY, key, need, facet_fields, key.get("facet"), state, null, 4);
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
