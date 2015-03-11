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

import edu.vt.vbi.patric.common.SolrInterface;
import edu.vt.vbi.patric.dao.DBPRC;
import edu.vt.vbi.patric.dao.ResultType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@SuppressWarnings("unchecked")
public class PRCPortlet extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(PRCPortlet.class);

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		response.setContentType("text/html");

		String contextType = request.getParameter("context_type");
		String contextId = request.getParameter("context_id");
		String filter = request.getParameter("filter");
		String organismName = ExperimentDataPortlet.getSpeciesName(contextType, contextId);

		request.setAttribute("contextType", contextType);
		request.setAttribute("contextId", contextId);
		request.setAttribute("filter", filter);
		request.setAttribute("organismName", organismName);

		PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/prc_list.jsp");
		prd.include(request, response);
	}

	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		response.setContentType("application/json");

		String contextType = request.getParameter("cType");
		String contextId = request.getParameter("cId");
		String filter = request.getParameter("filter");
		String start_id = request.getParameter("start");
		String limit = request.getParameter("limit");

		int start = Integer.parseInt(start_id);
		int end = start + Integer.parseInt(limit);

		SolrInterface solr = new SolrInterface();
		int taxonId = -1;

		if (contextType.equals("taxon")) {
			taxonId = Integer.parseInt(contextId);
		}
		else if (contextType.equals("genome")) {
			taxonId = solr.getGenome(contextId).getTaxonId();
		}

		if (filter == null) {
			filter = "";
		}

		String sort_field = "";
		String sort_dir = "";

		if (request.getParameter("sort") != null) {
			// sorting
			JSONParser a = new JSONParser();
			JSONArray sorter;

			try {
				sorter = (JSONArray) a.parse(request.getParameter("sort").toString());
				sort_field += ((JSONObject) sorter.get(0)).get("property").toString();
				sort_dir += ((JSONObject) sorter.get(0)).get("direction").toString();
				for (int i = 1; i < sorter.size(); i++) {
					sort_field += "," + ((JSONObject) sorter.get(i)).get("property").toString();
				}
			}
			catch (ParseException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}

		DBPRC conn_prc = new DBPRC();
		List<ResultType> items = null;
		JSONObject jsonResult = new JSONObject();
		JSONArray results = new JSONArray();

		int count_total = conn_prc.getPRCCount("" + taxonId, filter);

		if (count_total > 0) {
			items = conn_prc.getPRCData("" + taxonId, filter, start, end, sort_field, sort_dir);
		}

		jsonResult.put("total", count_total);

		for (ResultType item : items) {
			JSONObject obj = new JSONObject();
			obj.putAll(item);

			results.add(obj);
		}

		jsonResult.put("results", results);

		PrintWriter writer = response.getWriter();
		jsonResult.writeJSONString(writer);
		writer.close();
	}
}
