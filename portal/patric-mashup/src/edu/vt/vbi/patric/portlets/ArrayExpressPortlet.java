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

import edu.vt.vbi.patric.dao.DBShared;
import edu.vt.vbi.patric.dao.ResultType;
import edu.vt.vbi.patric.mashup.ArrayExpressInterface;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

@SuppressWarnings("unchecked")
public class ArrayExpressPortlet extends GenericPortlet {

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		response.setContentType("text/html");

		String contextType = request.getParameter("context_type");

		if (contextType != null) {

			String contextId = request.getParameter("context_id");
			String keyword = request.getParameter("keyword");
			String organismName = ExperimentDataPortlet.getSpeciesName(contextType, contextId);

			request.setAttribute("contextType", contextType);
			request.setAttribute("contextId", contextId);
			request.setAttribute("keyword", keyword);
			request.setAttribute("organismName", organismName);

			PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/arrayexpress_list.jsp");
			prd.include(request, response);
		}
		else {
			PrintWriter writer = response.getWriter();
			writer.write("<p>Invalid Parameter - missing context information</p>");
			writer.close();
		}
	}

	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		response.setContentType("application/json");
		String keyword = request.getParameter("keyword");
		String cType = request.getParameter("cType");
		String cId = request.getParameter("cId");
		String start_id = request.getParameter("start");
		String limit = request.getParameter("limit");
		int start = Integer.parseInt(start_id);
		int end = Integer.parseInt(limit);

		DBShared conn_shared = new DBShared();
		String species_name = "";

		if (cType.equals("taxon")) {
			ArrayList<ResultType> parents = conn_shared.getTaxonParentTree(cId);
			if (parents.size() > 0) {
				species_name = parents.get(0).get("name");
			}
		}
		else if (cType.equals("genome")) {
			ResultType names = conn_shared.getNamesFromGenomeInfoId(cId);
			species_name = names.get("genome_name");
		}

		ArrayExpressInterface api = new ArrayExpressInterface();

		JSONObject jsonAll = api.getResults(keyword, species_name);
		JSONObject jsonResult = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		jsonResult.put("total", jsonAll.get("total"));
		jsonResult.put("hasData", jsonAll.get("hasData"));
		for (int i = start; i < start + end; i++) {
			if (i < ((JSONArray) jsonAll.get("results")).size()) {
				JSONObject j = (JSONObject) ((JSONArray) jsonAll.get("results")).get(i);
				jsonArr.add(i - start, j);
			}
		}
		jsonResult.put("results", jsonArr);
		PrintWriter writer = response.getWriter();
		jsonResult.writeJSONString(writer);
		writer.close();
	}
}
