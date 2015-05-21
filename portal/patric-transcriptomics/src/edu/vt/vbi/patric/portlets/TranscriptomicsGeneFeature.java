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
import edu.vt.vbi.patric.common.*;
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

public class TranscriptomicsGeneFeature extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(TranscriptomicsGeneFeature.class);

	ObjectReader jsonReader;

	ObjectWriter jsonWriter;

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
		response.setTitle("Transcriptomics Feature");

		SiteHelper.setHtmlMetaElements(request, response, "Transcriptomics Feature");

		String contextType = request.getParameter("context_type");
		String contextId = request.getParameter("context_id");
		String pk = request.getParameter("param_key");

		String featureIds = SessionHandler.getInstance().get(SessionHandler.PREFIX + pk);

		if (featureIds == null) {
			featureIds = "";
		}

		request.setAttribute("contextType", contextType);
		request.setAttribute("contextId", contextId);
		request.setAttribute("pk", pk);
		request.setAttribute("featureIds", featureIds);

		PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/TranscriptomicsFeature.jsp");
		prd.include(request, response);
	}

	@SuppressWarnings("unchecked")
	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
		response.setContentType("text/html");

		String callType = request.getParameter("callType");

		switch (callType) {
		case "saveFeatureParams": {

			String featureId = request.getParameter("feature_id");

			long pk = (new Random()).nextLong();

			SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, featureId);

			PrintWriter writer = response.getWriter();
			writer.write("" + pk);
			writer.close();
			break;
		}
		case "getFeatureTable": {

			Map data = processFeatureTab(request);

			int numFound = (Integer) data.get("numFound");
			List<GenomeFeature> features = (List<GenomeFeature>) data.get("features");

			JSONArray docs = new JSONArray();
			for (GenomeFeature feature : features) {
				docs.add(feature.toJSONObject());
			}

			JSONObject jsonResult = new JSONObject();

			jsonResult.put("results", docs);
			jsonResult.put("total", numFound);

			PrintWriter writer = response.getWriter();
			jsonResult.writeJSONString(writer);
			writer.close();
			break;
		}
		case "download":

			List<String> tableHeader = new ArrayList<>();
			List<String> tableField = new ArrayList<>();
			JSONArray tableSource = new JSONArray();

			String fileFormat = request.getParameter("fileFormat");
			String fileName = "Table_Gene";

			Map data = processFeatureTab(request);

			List<GenomeFeature> features = (List<GenomeFeature>) data.get("features");

			for (GenomeFeature feature : features) {
				tableSource.add(feature.toJSONObject());
			}

			tableHeader.addAll(DownloadHelper.getHeaderForFeatures());
			tableField.addAll(DownloadHelper.getFieldsForFeatures());

			ExcelHelper excel = new ExcelHelper("xssf", tableHeader, tableField, tableSource);
			excel.buildSpreadsheet();

			if (fileFormat.equalsIgnoreCase("xlsx")) {
				response.setContentType("application/octetstream");
				response.addProperty("Content-Disposition", "attachment; filename=\"" + fileName + "." + fileFormat + "\"");

				excel.writeSpreadsheettoBrowser(response.getPortletOutputStream());
			}
			else {
				response.setContentType("application/octetstream");
				response.addProperty("Content-Disposition", "attachment; filename=\"" + fileName + "." + fileFormat + "\"");

				response.getWriter().write(excel.writeToTextFile());
			}

			break;
		}
	}

	private Map processFeatureTab(ResourceRequest request) throws IOException {

		String pk = request.getParameter("pk");
		String sort = request.getParameter("sort");
		String start_id = request.getParameter("start");
		String limit = request.getParameter("limit");
		int start = 0;
		int end = -1;
		if (start_id != null) {
			start = Integer.parseInt(start_id);
		}
		if (limit != null) {
			end = Integer.parseInt(limit);
		}

		String featureId = SessionHandler.getInstance().get(SessionHandler.PREFIX + pk);

		Map<String, String> key = new HashMap<>();
		key.put("keyword", "feature_id:(" + featureId.replaceAll(",", " OR ") + ")");

		DataApiHandler dataApi = new DataApiHandler(request);

		SolrQuery query = dataApi.buildSolrQuery(key, sort, null, start, end, false);

		String apiResponse = dataApi.solrQuery(SolrCore.FEATURE, query);

		Map resp = jsonReader.readValue(apiResponse);
		Map respBody = (Map) resp.get("response");

		int numFound = (Integer) respBody.get("numFound");
		List<GenomeFeature> features = dataApi.bindDocuments((List<Map>) respBody.get("docs"), GenomeFeature.class);

		Map response = new HashMap();
		response.put("key", key);
		response.put("numFound", numFound);
		response.put("features", features);

		return response;
	}
}
