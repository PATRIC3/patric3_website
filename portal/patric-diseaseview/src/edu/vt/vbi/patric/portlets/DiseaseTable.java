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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.UnavailableException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.vt.vbi.patric.common.SiteHelper;
import edu.vt.vbi.patric.dao.DBDisease;
import edu.vt.vbi.patric.dao.ResultType;

public class DiseaseTable extends GenericPortlet {

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.portlet.GenericPortlet#doView(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
	 */
	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException, UnavailableException {

		new SiteHelper().setHtmlMetaElements(request, response, "Disease Table");

		response.setContentType("text/html");
		response.setTitle("Disease Table");

		String type = request.getParameter("type").split("/")[0];

		PortletRequestDispatcher prd = null;

		if (type.equals("vfdb")) {
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/disease_table_vfdb.jsp");
		}
		else if (type.equals("ctd")) {
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/disease_table_ctd.jsp");
		}
		else {
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/disease_table_gad.jsp");
		}
		prd.include(request, response);
	}

	@SuppressWarnings("unchecked")
	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		response.setContentType("application/json");

		String type = request.getParameter("type");

		String start_id = request.getParameter("start");
		String limit = request.getParameter("limit");
		int start = Integer.parseInt(start_id);
		int end = start + Integer.parseInt(limit);

		Map<String, String> key = new HashMap<>();
		Map<String, String> sort = null;

		if (request.getParameter("sort") != null) {
			// sorting
			JSONParser a = new JSONParser();
			JSONArray sorter;
			String sort_field = "";
			String sort_dir = "";
			try {
				sorter = (JSONArray) a.parse(request.getParameter("sort").toString());
				sort_field += ((JSONObject) sorter.get(0)).get("property").toString();
				sort_dir += ((JSONObject) sorter.get(0)).get("direction").toString();
				for (int i = 1; i < sorter.size(); i++) {
					sort_field += "," + ((JSONObject) sorter.get(i)).get("property").toString();
				}
			}
			catch (ParseException e) {
				e.printStackTrace();
			}

			sort = new HashMap<String, String>();

			if (!sort_field.equals("") && !sort_dir.equals("")) {
				sort.put("field", sort_field);
				sort.put("direction", sort_dir);
			}
		}

		DBDisease conn_disease = new DBDisease();

		int count_total = 0;
		List<ResultType> items = null;

		if (type.equals("0")) {

			String cId = request.getParameter("cId");
			key.put("cId", cId);

			count_total = conn_disease.getVFDBCount(key);
			items = conn_disease.getVFDBList(key, sort, start, end);
		}
		if (type.equals("1")) {

			String cId = request.getParameter("cId");
			String vfgId = request.getParameter("vfgId");

			key.put("cId", cId);
			if (vfgId != null && !vfgId.equals("")) {
				key.put("vfgId", vfgId);
			}

			count_total = conn_disease.getVFDBFeatureCount(key);
			items = conn_disease.getVFDBFeatureList(key, sort, start, end);
		}
		else if (type.equals("ctd")) {

			String name = request.getParameter("name");
			key.put("name", name);

			count_total = conn_disease.getCTDCount(key);
			items = conn_disease.getCTDList(key, sort, start, end);
		}
		else if (type.equals("gad")) {

			String name = request.getParameter("name");
			key.put("name", name);

			count_total = conn_disease.getGADCount(key);
			items = conn_disease.getGADList(key, sort, start, end);
		}
		else if (type.equals("gadgraph")) {

			String name = request.getParameter("name");
			key.put("name", name);

			count_total = conn_disease.getGADGraphCount(key);
			items = conn_disease.getGADGraphList(key, sort, start, end);
		}
		else if (type.equals("ctdgraph")) {

			String name = request.getParameter("name");
			key.put("name", name);

			count_total = conn_disease.getCTDGraphCount(key);
			items = conn_disease.getCTDGraphList(key, sort, start, end);
		}

		JSONObject jsonResult = new JSONObject();

		try {
			jsonResult.put("total", count_total);

			JSONArray results = new JSONArray();

			for (int i = 0; i < items.size(); i++) {
				ResultType g = items.get(i);
				JSONObject obj = new JSONObject();
				obj.putAll(g);
				results.add(obj);
			}
			jsonResult.put("results", results);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		PrintWriter writer = response.getWriter();
		jsonResult.writeJSONString(writer);
		writer.close();
	}
}
