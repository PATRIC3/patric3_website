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
import java.util.List;

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

import edu.vt.vbi.patric.common.SiteHelper;
import edu.vt.vbi.patric.dao.DBDisease;
import edu.vt.vbi.patric.dao.ResultType;

public class DiseaseOverview extends GenericPortlet {

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.portlet.GenericPortlet#doView(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
	 */
	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException, UnavailableException {

		new SiteHelper().setHtmlMetaElements(request, response, "Disease Overview");

		response.setContentType("text/html");
		response.setTitle("Disease Overview");

		PortletRequestDispatcher prd = null;
		prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/disease_overview.jsp");
		prd.include(request, response);
	}

	@SuppressWarnings("unchecked")
	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		response.setContentType("application/json");

		String type = request.getParameter("type");
		String cId = request.getParameter("cId");

		DBDisease conn_disease = new DBDisease();
		int count_total = 0;
		JSONArray results = new JSONArray();
		PrintWriter writer = response.getWriter();

		if (type.equals("incidence")) {

			JSONObject jsonResult = new JSONObject();
			// String cType = request.getParameter("cType");

			// sorting
			// String sort_field = request.getParameter("sort");
			// String sort_dir = request.getParameter("dir");

			// Map<String, String> key = new HashMap<>();
			// Map<String, String> sort = null;
			//
			// if (sort_field != null && sort_dir != null) {
			// sort = new HashMap<String, String>();
			// sort.put("field", sort_field);
			// sort.put("direction", sort_dir);
			// }
			//
			// key.put("cId", cId);
			// key.put("cType", cType);
			count_total = 1;
			jsonResult.put("total", count_total);

			JSONObject obj = new JSONObject();
			obj.put("rownum", (Object) "1");
			obj.put("pathogen", (Object) "Pathogen");
			obj.put("disease", (Object) "Disease");
			obj.put("incidence", (Object) "10");
			obj.put("infection", (Object) "5");

			results.add(obj);
			jsonResult.put("results", results);

			jsonResult.writeJSONString(writer);
		}
		else if (type.equals("disease_tree")) {

			JSONArray jsonResult = new JSONArray();
			String tree_node = request.getParameter("node");
			List<ResultType> items = conn_disease.getMeshHierarchy(cId, tree_node);

			if (items.size() > 0) {
				int min = Integer.parseInt((String) items.get(0).get("lvl"));
				try {
					for (int i = 0; i < items.size(); i++) {
						ResultType item = items.get(i);
						if (min == Integer.parseInt((String) items.get(i).get("lvl"))) {

							boolean flag = false;
							JSONObject obj = DiseaseOverview.encodeNodeJSONObject(item);

							String mesh_id = (String) obj.get("tree_node");

							for (int j = 0; j < jsonResult.size(); j++) {

								JSONObject temp = (JSONObject) jsonResult.get(j);

								if (temp.get("tree_node").equals(mesh_id)) {
									flag = true;
									temp.put("pathogen", (String) temp.get("pathogen") + "<br>" + obj.get("pathogen"));
									temp.put("genome", (String) temp.get("genome") + "<br>" + obj.get("genome"));
									temp.put("vfdb", (String) temp.get("vfdb") + "<br>" + obj.get("vfdb"));
									temp.put("gad", (String) temp.get("gad") + "<br>" + obj.get("gad"));
									temp.put("ctd", (String) temp.get("ctd") + "<br>" + obj.get("ctd"));
									temp.put("taxon_id", (String) temp.get("taxon_id") + "<br>" + obj.get("taxon_id"));

									jsonResult.set(j, temp);
								}
							}
							if (flag == false) {
								jsonResult.add(obj);
							}
						}
					}
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			jsonResult.writeJSONString(writer);
		}

		writer.close();
	}

	@SuppressWarnings("unchecked")
	public static JSONObject encodeNodeJSONObject(ResultType rt) {

		JSONObject obj = new JSONObject();
		obj.putAll(rt);

		obj.put("id", rt.get("tree_node"));
		obj.put("node", rt.get("tree_node"));
		obj.put("expanded", "true");

		if (rt.get("leaf").equals("1")) {
			obj.put("leaf", "true");
		}
		else {
			obj.put("leaf", "false");
		}
		return obj;
	}
}
