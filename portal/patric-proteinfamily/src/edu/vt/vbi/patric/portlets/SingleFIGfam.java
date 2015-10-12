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

public class SingleFIGfam extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(SingleFIGfam.class);

	ObjectReader jsonReader;

	ObjectWriter jsonWriter;

	@Override public void init() throws PortletException {
		super.init();

		ObjectMapper objectMapper = new ObjectMapper();
		jsonReader = objectMapper.reader(Map.class);
		jsonWriter = objectMapper.writerWithType(Map.class);
	}

	public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		SiteHelper.setHtmlMetaElements(request, response, "Protein Family");
		response.setContentType("text/html");

		String pk = request.getParameter("param_key");
		String contextType = request.getParameter("context_type");
		String contextId = request.getParameter("context_id");

		String genomeIds = "";
		String familyIds = "";
		String familyType = "";
		String familyId = "";

		int length = 1;

		Map<String, String> key = jsonReader.readValue(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk));

		if (key != null && key.containsKey("genomeIds")) {
			genomeIds = key.get("genomeIds");
		}

		if (key != null && key.containsKey("familyIds")) {
			familyIds = key.get("familyIds");
			length = familyIds.split("##").length;
		}

		if (key != null && key.containsKey("familyType")) {
			familyType = key.get("familyType");
			familyId = familyType + "_id";
		}

		request.setAttribute("contextType", contextType);
		request.setAttribute("contextId", contextId);
		request.setAttribute("genomeIds", genomeIds);
		request.setAttribute("familyIds", familyIds);
		request.setAttribute("familyType", familyType);
		request.setAttribute("familyId", familyId);
		request.setAttribute("length", length);
		request.setAttribute("pk", pk);

		PortletRequestDispatcher reqDispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/single.jsp");
		reqDispatcher.include(request, response);
	}

	@SuppressWarnings("unchecked") public void serveResource(ResourceRequest request, ResourceResponse response)
			throws PortletException, IOException {
		String callType = request.getParameter("callType");

		if (callType != null) {
			Map<String, String> key = new HashMap<>();
			if (callType.equals("saveState")) {
				String genomeIds = request.getParameter("genomeIds");
				String familyIds = request.getParameter("familyIds");
				String familyType = request.getParameter("familyType");

				key.put("genomeIds", genomeIds);
				key.put("familyIds", familyIds);
				key.put("familyType", familyType);

				Random g = new Random();
				long pk = g.nextLong();

				SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, jsonWriter.writeValueAsString(key));

				PrintWriter writer = response.getWriter();
				writer.write("" + pk);
				writer.close();

			}
			else if (callType.equals("getData")) {

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

				response.setContentType("application/json");
				PrintWriter writer = response.getWriter();
				jsonResult.writeJSONString(writer);
				writer.close();
			}
			else if (callType.equals("download")) {

				List<String> tableHeader = new ArrayList<>();
				List<String> tableField = new ArrayList<>();
				JSONArray tableSource = new JSONArray();

				String fileName = "FeatureTable";
				String fileFormat = request.getParameter("fileformat");

				// features
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
				else if (fileFormat.equalsIgnoreCase("txt")) {

					response.setContentType("application/octetstream");
					response.addProperty("Content-Disposition", "attachment; filename=\"" + fileName + "." + fileFormat + "\"");

					response.getPortletOutputStream().write(excel.writeToTextFile().getBytes());
				}
			}
		}
	}

	private Map processFeatureTab(ResourceRequest request) throws IOException {

//		LOGGER.debug("params: {}", request.getParameterMap());

		String keyword = request.getParameter("keyword");
		String sort = request.getParameter("sort");

		if (request.getParameter("callType").equals("download")) {
			keyword = request.getParameter("download_keyword");
		}

		Map<String, String> key = new HashMap<>();

		key.put("keyword", keyword);

		DataApiHandler dataApi = new DataApiHandler(request);

		String start_id = request.getParameter("start");
		String limit = request.getParameter("limit");
		int start = 0;
		int end = -1;

		if (start_id != null)
			start = Integer.parseInt(start_id);
		if (limit != null)
			end = Integer.parseInt(limit);

		key.put("filter", "annotation:PATRIC AND feature_type:CDS");
		key.put("fields", StringUtils.join(DownloadHelper.getFieldsForFeatures(), ","));

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
