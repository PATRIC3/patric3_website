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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import edu.vt.vbi.patric.common.SiteHelper;
import edu.vt.vbi.patric.dao.DBPathways;
import edu.vt.vbi.patric.dao.ResultType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompPathwayMap extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(CompPathwayMap.class);

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		response.setContentType("text/html");

		new SiteHelper().setHtmlMetaElements(request, response, "Comparative Pathway Map");

		response.setTitle("Comparative Pathway Map");
		PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/comp_pathway_map.jsp");
		prd.include(request, response);
	}

	@SuppressWarnings("unchecked")
	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		response.setContentType("application/json");

		String sort_field = request.getParameter("sort");
		String sort_dir = request.getParameter("dir");

		HashMap<String, String> sort = null;

		if (sort_field != null && sort_dir != null) {
			sort = new HashMap<>();
			sort.put("field", sort_field);
			sort.put("direction", sort_dir);
		}

		String genomeId = request.getParameter("genomeId");
		String taxonId = request.getParameter("taxonId");
		String cType = request.getParameter("cType");
		String map = request.getParameter("map");
		String algorithm = request.getParameter("algorithm");

		HashMap<String, String> key = null;

		if (cType.equals("taxon") && taxonId != null && !taxonId.equals("")) {
			key = new HashMap<>();

			key.put("taxonId", taxonId);
			key.put("map", map);
			key.put("algorithm", algorithm);
		}
		else if (cType.equals("genome") && genomeId != null && !genomeId.equals("")) {

			key = new HashMap<>();

			key.put("genomeId", genomeId);
			key.put("map", map);
			key.put("algorithm", algorithm);
		}
		else {

			String pk = request.getParameter("pk");
			PortletSession sess = request.getPortletSession();

			ResultType keytemp = (ResultType) sess.getAttribute("key" + pk, PortletSession.APPLICATION_SCOPE);
			if (keytemp != null) {
				key = (HashMap<String, String>) keytemp.clone();
				key.put("map", map);
				key.put("algorithm", algorithm);
			}
		}

		int count_total;

		DBPathways conn_pathways = new DBPathways();
		List<ResultType> items = conn_pathways.getCompPathwayMapGridList(key, sort, 0, -1);
		count_total = conn_pathways.getCompPathwayMapGridCount(key);
		JSONObject jsonResult = new JSONObject();

//		if (algorithm.equals("RAST") || algorithm.equals("PATRIC"))
//			algorithm = "RAST";
//		else if (algorithm.equals("Curation") || algorithm.equals("Legacy BRC"))
//			algorithm = "Curation";

		try {
			jsonResult.put("total", count_total);
			JSONArray results = new JSONArray();

			for (ResultType item : items) {
				JSONObject obj = new JSONObject();
				obj.putAll(item);
				results.add(obj);
			}

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