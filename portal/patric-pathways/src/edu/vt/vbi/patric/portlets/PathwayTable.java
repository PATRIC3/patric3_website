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

import edu.vt.vbi.patric.common.SolrCore;
import edu.vt.vbi.patric.common.SolrInterface;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PathwayTable extends GenericPortlet {

	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PathwayTable.class);

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		response.setContentType("text/html");
		response.setTitle("Pathways");

		PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/pathway_table.jsp");
		prd.include(request, response);
	}

	@SuppressWarnings("unchecked")
	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		response.setContentType("application/json");
		SolrInterface solr = new SolrInterface();

		int start = Integer.parseInt(request.getParameter("start"));
		int end = start + Integer.parseInt(request.getParameter("limit"));

		HashMap<String, String> key = new HashMap<>();

		// sorting
		HashMap<String, String> sort;
		if (request.getParameter("sort") != null) {
			// sorting
			JSONParser a = new JSONParser();
			JSONArray sorter;
			String sort_field = "";
			String sort_dir = "";
			try {
				sorter = (JSONArray) a.parse(request.getParameter("sort"));
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

		if (request.getParameter("id") != null) {
			key.put("na_feature_id", request.getParameter("id"));
		}

		int count_total = 0;
		JSONArray results = new JSONArray();
		List<String> pathwayKeys = new ArrayList<>();
		Map<String, Integer> mapOccurrence = new HashMap<>();
		try {
			SolrQuery query = new SolrQuery("feature_id:" + request.getParameter("id"));
			query.setRows(10000);
			QueryResponse qr = solr.getSolrServer(SolrCore.PATHWAY).query(query);
			SolrDocumentList sdl = qr.getResults();
			count_total = (int) sdl.getNumFound();

			for (SolrDocument doc : sdl) {
				String pathwayKey = "(pathway_id:" + doc.get("pathway_id").toString() + " AND ec_number:" + doc.get("ec_number").toString() + ")";

				pathwayKeys.add(pathwayKey);
			}

			SolrQuery queryRef = new SolrQuery(StringUtils.join(pathwayKeys, " OR "));
			queryRef.setFields("pathway_id,ec_number,occurrence");
			queryRef.setRows(pathwayKeys.size());
			QueryResponse qrRef = solr.getSolrServer(SolrCore.PATHWAY_REF).query(queryRef);

			for (SolrDocument doc : qrRef.getResults()) {
				mapOccurrence.put(doc.get("pathway_id") + "_" + doc.get("ec_number"), (Integer) doc.get("occurrence"));
			}

			for (SolrDocument doc : sdl) {
				JSONObject item = new JSONObject();
				item.put("pathway_id", doc.get("pathway_id"));
				item.put("feature_id", doc.get("feature_id"));
				item.put("pathway_name", doc.get("pathway_name"));
				item.put("pathway_class", doc.get("pathway_class"));
				item.put("algorithm", doc.get("annotation"));
				item.put("ec_number", doc.get("ec_number"));
				item.put("ec_name", doc.get("ec_description"));
				item.put("taxon_id", doc.get("taxon_id"));
				item.put("genome_id", doc.get("genome_id"));

				item.put("occurrence", mapOccurrence.get(doc.get("pathway_id") + "_" + doc.get("ec_number")));

				results.add(item);
			}
		}
		catch (MalformedURLException | SolrServerException e) {
			LOGGER.error(e.getMessage(), e);
		}

		JSONObject jsonResult = new JSONObject();

		try {
			jsonResult.put("total", count_total);
			jsonResult.put("results", results);
		}
		catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
		}

		PrintWriter writer = response.getWriter();
		jsonResult.writeJSONString(writer);
		writer.close();
	}
}
