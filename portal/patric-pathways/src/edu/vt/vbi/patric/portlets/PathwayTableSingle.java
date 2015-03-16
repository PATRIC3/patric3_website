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
import edu.vt.vbi.patric.beans.GenomeFeature;
import edu.vt.vbi.patric.common.SolrCore;
import edu.vt.vbi.patric.common.SolrInterface;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.*;

public class PathwayTableSingle extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(PathwayTableSingle.class);

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		response.setContentType("text/html");
		response.setTitle("Pathway Table");

		// String contextType = request.getParameter("context_type");
		// String contextId = request.getParameter("context_id");

		String pk = request.getParameter("param_key");

		String ec_number = "", algorithm = "", map = "", genomeId = "";
		Gson gson = new Gson();

		PortletSession session = request.getPortletSession(true);
		Map<String, String> key = gson.fromJson((String) session.getAttribute("key" + pk, PortletSession.APPLICATION_SCOPE), Map.class);

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
		Gson gson = new Gson();

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

			Random g = new Random();
			int random = g.nextInt();

			PortletSession session = request.getPortletSession(true);
			session.setAttribute("key" + random, gson.toJson(key, Map.class), PortletSession.APPLICATION_SCOPE);

			PrintWriter writer = response.getWriter();
			writer.write("" + random);
			writer.close();

		}
		else if (callType != null && callType.equals("show")) {

			JSONObject jsonResult = new JSONObject();
			JSONArray results = new JSONArray();
			String pk = request.getParameter("pk");
			PortletSession session = request.getPortletSession();
			Map<String, String> key = gson.fromJson((String) session.getAttribute("key" + pk, PortletSession.APPLICATION_SCOPE), Map.class);

			SolrInterface solr = new SolrInterface();
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

				LOGGER.debug("{}", query.toString());
				QueryResponse qr = solr.getSolrServer(SolrCore.FEATURE).query(query, SolrRequest.METHOD.POST);
				List<GenomeFeature> featureList = qr.getBeans(GenomeFeature.class);

				for (GenomeFeature feature : featureList) {

					results.add(feature.toJSONObject());
				}

				jsonResult.put("total", featureList.size());
				jsonResult.put("results", results);
			}
			catch (MalformedURLException | SolrServerException e) {
				LOGGER.error(e.getMessage(), e);
			}

			PrintWriter writer = response.getWriter();
			jsonResult.writeJSONString(writer);
			writer.close();
		}
	}
}
