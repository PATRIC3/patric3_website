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

import edu.vt.vbi.patric.common.*;
import edu.vt.vbi.patric.dao.ResultType;
import org.apache.solr.client.solrj.SolrQuery;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.codehaus.jackson.map.ObjectWriter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import javax.xml.crypto.Data;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SingleExperiment extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(SingleExperiment.class);

	private ObjectReader jsonReader;

	@Override
	public void init() throws PortletException {
		super.init();

		ObjectMapper objectMapper = new ObjectMapper();
		jsonReader = objectMapper.reader(Map.class);
	}

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		SiteHelper.setHtmlMetaElements(request, response, "Single Experiment");

		response.setContentType("text/html");
		response.setTitle("Single Experiment");

		PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/SingleExperiment.jsp");
		prd.include(request, response);
	}

	@SuppressWarnings("unchecked")
	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		String callType = request.getParameter("callType");

		switch(callType) {
		case "getTable": {

			Map data = processComparisonTab(request);
			int numFound = (Integer) data.get("numFound");
			List<Map> comparisons = (List<Map>) data.get("comparisons");

			JSONArray docs = new JSONArray();
			for (Map item : comparisons) {
				JSONObject doc = new JSONObject();
				doc.putAll(item);
				docs.add(doc);
			}

			JSONObject jsonResult = new JSONObject();
			jsonResult.put("results", docs);
			jsonResult.put("total", numFound);

			response.setContentType("text/html");
			PrintWriter writer = response.getWriter();
			jsonResult.writeJSONString(writer);
			writer.close();
			break;
		}
		case "getSummary": {
			String eid = request.getParameter("eid");
			Map<String, String> key = new HashMap<>();
			key.put("keyword", "eid:(" + eid + ")");
			key.put("fields", "description,condition,pi,title,institution,release_date,accession,organism,strain,timeseries");

			DataApiHandler dataApi = new DataApiHandler(request);
			SolrInterface solr = new SolrInterface();
			SolrQuery query = solr.buildSolrQuery(key, null, null, 0, 1, false);
			String apiResponse = dataApi.solrQuery(SolrCore.TRANSCRIPTOMICS_EXPERIMENT, query);

			Map resp = jsonReader.readValue(apiResponse);
			Map respBody = (Map) resp.get("response");
			List<Map> experiments = (List<Map>) respBody.get("docs");
			JSONObject experiment = new JSONObject();
			experiment.putAll(experiments.get(0));

			JSONObject jsonResult = new JSONObject();
			jsonResult.put("summary", experiment);

			PrintWriter writer = response.getWriter();
			jsonResult.writeJSONString(writer);
			writer.close();
			break;
		}
		case "download": {

			List<String> tableHeader = new ArrayList<>();
			List<String> tableField = new ArrayList<>();
			JSONArray tableSource = new JSONArray();

			String fileName = "SingleExperiment";
			String fileFormat = request.getParameter("fileformat");

			Map data = processComparisonTab(request);
			List<Map> comparisons = (List<Map>) data.get("comparisons");

			for (Map item : comparisons) {
				JSONObject doc = new JSONObject();
				doc.putAll(item);
				tableSource.add(doc);
			}

			tableHeader.addAll(DownloadHelper.getHeaderForTranscriptomicsComparison());
			tableField.addAll(DownloadHelper.getFieldsForTranscriptomicsComparison());

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

			break;
		}
		}
	}

	private Map processComparisonTab(ResourceRequest request) throws IOException {

		SolrInterface solr = new SolrInterface();
		DataApiHandler dataApi = new DataApiHandler(request);

		String eid = request.getParameter("eid");
		String sort = request.getParameter("sort");

		Map<String, String> key = new HashMap<>();
		key.put("keyword", "eid:(" + eid + ")");
		key.put("fields",
				"eid,expid,accession,pid,samples,expname,release_date,pmid,organism,strain,mutant,timepoint,condition,genes,sig_log_ratio,sig_z_score");

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

		SolrQuery query = solr.buildSolrQuery(key, sort, null, start, end, false);

		LOGGER.debug("getTable: [{}] {}", SolrCore.TRANSCRIPTOMICS_COMPARISON.getSolrCoreName(), query.toString());

		String apiResponse = dataApi.solrQuery(SolrCore.TRANSCRIPTOMICS_COMPARISON, query);
		Map resp = jsonReader.readValue(apiResponse);
		Map respBody = (Map) resp.get("response");

		int numFound = (Integer) respBody.get("numFound");
		List<Map> comparisons = (List<Map>) respBody.get("docs");

		Map response = new HashMap();
		response.put("key", key);
		response.put("numFound", numFound);
		response.put("comparisons", comparisons);

		return response;

	}
}
