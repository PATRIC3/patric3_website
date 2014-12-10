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

import edu.vt.vbi.patric.common.SiteHelper;
import edu.vt.vbi.patric.common.SolrInterface;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TranscriptomicsGeneFeature extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(TranscriptomicsGeneFeature.class);

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		response.setContentType("text/html");
		response.setTitle("Transcriptomics Feature");

		new SiteHelper().setHtmlMetaElements(request, response, "Transcriptomics Feature");

		PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/TranscriptomicsFeature.jsp");
		prd.include(request, response);
	}

	@SuppressWarnings("unchecked")
	public void serveResource(ResourceRequest req, ResourceResponse resp) throws PortletException, IOException {
		resp.setContentType("text/html");

		String callType = req.getParameter("callType");

		if (callType.equals("saveFeatureParams")) {

			HashMap<String, String> key = new HashMap<>();
			key.put("feature_id", req.getParameter("feature_id"));

			Random g = new Random();
			int random = g.nextInt();

			PortletSession session = req.getPortletSession(true);
			session.setAttribute("key" + random, key, PortletSession.APPLICATION_SCOPE);

			PrintWriter writer = resp.getWriter();
			writer.write("" + random);
			writer.close();
		}
		else if (callType.equals("getFeatureTable")) {

			PortletSession session = req.getPortletSession();

			String pk = req.getParameter("pk");
			String start_id = req.getParameter("start");
			String limit = req.getParameter("limit");
			int start = Integer.parseInt(start_id);
			int end = Integer.parseInt(limit);

			HashMap<String, String> key = (HashMap<String, String>) session.getAttribute("key" + pk, PortletSession.APPLICATION_SCOPE);

			Map<String, Object> condition = new HashMap<>();
			condition.put("feature_ids", key.get("feature_id"));
			condition.put("sortParam", req.getParameter("sort").toString());
			condition.put("startParam", Integer.toString(start));
			condition.put("limitParam", Integer.toString(end));
			SolrInterface solr = new SolrInterface();
			JSONObject object = solr.getFeaturesByID(condition);
			JSONArray obj_array = (JSONArray) object.get("results");

			JSONObject jsonResult = new JSONObject();

			jsonResult.put("results", obj_array);
			jsonResult.put("total", object.get("total").toString());

			PrintWriter writer = resp.getWriter();
			writer.write(jsonResult.toString());
			writer.close();
		}
	}
}
