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

import edu.vt.vbi.patric.beans.GenomeFeature;
import edu.vt.vbi.patric.common.DataApiHandler;
import edu.vt.vbi.patric.common.SessionHandler;
import edu.vt.vbi.patric.common.SolrCore;
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

public class PathwayTableSingle extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(PathwayTableSingle.class);

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
		response.setTitle("Pathway Table");

		String pk = request.getParameter("param_key");

		String ec_number = "", algorithm = "", map = "", genomeId = "";

		Map<String, String> key = jsonReader.readValue(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk));

		if (key != null && key.containsKey("algorithm")) {
			algorithm = key.get("algorithm");
		}

		if (key != null && key.containsKey("ec_number")) {
			ec_number = key.get("ec_number");
		}

		if (key != null && key.containsKey("map")) {
			map = key.get("map");
		}

		if (key != null && key.containsKey("genomeId")) {
			genomeId = key.get("genomeId");
		}

		request.setAttribute("pk", pk);
		request.setAttribute("algorithm", algorithm);
		request.setAttribute("ec_number", ec_number);
		request.setAttribute("map", map);
		request.setAttribute("genomeId", genomeId);

		PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/single_pathway_table.jsp");
		prd.include(request, response);
	}

	@SuppressWarnings("unchecked")
	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		response.setContentType("application/json");

		String callType = request.getParameter("callType");

		if (callType != null && callType.equals("savetopk")) {

			String cId = request.getParameter("cId");
			String cType = request.getParameter("cType");
			String map = request.getParameter("map");
			String algorithm = request.getParameter("algorithm");
			String ec_number = request.getParameter("ec_number");

			Map<String, String> key = new HashMap<>();

			if (cId != null && !cId.equals("")) {
				key.put("genomeId", cId);
			}
			if (cType != null && !cType.equals("")) {
				key.put("cType", cType);
			}

			if (map != null && !map.equals("")) {
				key.put("map", map);
			}

			if (algorithm != null && !algorithm.equals("")) {
				key.put("algorithm", algorithm);
			}

			if (ec_number != null && !ec_number.equals("")) {
				key.put("ec_number", ec_number);
			}

			key.put("which", "download_from_heatmap_feature");

			long pk = (new Random()).nextLong();

			SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, jsonWriter.writeValueAsString(key));

			PrintWriter writer = response.getWriter();
			writer.write("" + pk);
			writer.close();

		}
		else if (callType != null && callType.equals("show")) {

			JSONObject jsonResult = new JSONObject();
			JSONArray results = new JSONArray();
			String pk = request.getParameter("pk");
			Map<String, String> key = jsonReader.readValue(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk));

			DataApiHandler dataApi = new DataApiHandler(request);
			try {
				SolrQuery query = new SolrQuery("*:*");
				List<String> joinConditions = new ArrayList<>();

				if (key.containsKey("map")) {
					joinConditions.add("pathway_id:(" + key.get("map") + ")");
				}
				if (key.containsKey("algorithm")) {
					joinConditions.add("annotation:(" + key.get("algorithm") + ")");
				}
				if (key.containsKey("ec_number")) {
					joinConditions.add("ec_number:(" + key.get("ec_number").replaceAll(",", " OR ") + ")");
				}
				if (key.containsKey("genomeId")) {
					joinConditions.add("genome_id:(" + key.get("genomeId").replace(",", " OR ") + ")");
				}

				if (!joinConditions.isEmpty()) {
					query.addFilterQuery(SolrCore.PATHWAY.getSolrCoreJoin("feature_id", "feature_id", StringUtils.join(joinConditions, " AND ")));
				}
				query.setRows(10000);

				LOGGER.debug("[{}] {}", SolrCore.FEATURE.getSolrCoreName(), query.toString());

				String apiResponse = dataApi.solrQuery(SolrCore.FEATURE, query);

				Map resp = jsonReader.readValue(apiResponse);
				Map respBody = (Map) resp.get("response");

				int numFound = (Integer) respBody.get("numFound");
				List<GenomeFeature> features = dataApi.bindDocuments((List<Map>) respBody.get("docs"), GenomeFeature.class);

				for (GenomeFeature feature : features) {

					results.add(feature.toJSONObject());
				}

				jsonResult.put("total", numFound);
				jsonResult.put("results", results);
			}
			catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}

			PrintWriter writer = response.getWriter();
			jsonResult.writeJSONString(writer);
			writer.close();
		}
	}
}
