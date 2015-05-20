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

import edu.vt.vbi.patric.beans.Genome;
import edu.vt.vbi.patric.beans.GenomeFeature;
import edu.vt.vbi.patric.beans.Taxonomy;
import edu.vt.vbi.patric.common.*;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.util.*;

public class GlobalSearch extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalSearch.class);

	private ObjectReader jsonReader;

	@Override public void init() throws PortletException {
		super.init();

		ObjectMapper objectMapper = new ObjectMapper();
		jsonReader = objectMapper.reader(Map.class);
	}

	@Override protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		response.setContentType("text/html");

		SiteHelper.setHtmlMetaElements(request, response, "PATRIC Search");

		String pk = request.getParameter("param_key");

		Map<String, String> key = jsonReader.readValue(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk));
		String keyword = "";
		if (key != null && key.get("keyword") != null) {
			keyword = key.get("keyword");
		}

		request.setAttribute("pk", pk);
		request.setAttribute("keyword", keyword);

		PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/globalsearch.jsp");
		prd.include(request, response);
	}

	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		String need = request.getParameter("need");

		switch (need) {
		case "search":
			processGlobalSearch(request, response);
			break;
		case "download": {

			String category = request.getParameter("cat");

			DataApiHandler dataApi = new DataApiHandler(request);
			List<String> tableHeader = new ArrayList<>();
			List<String> tableField = new ArrayList<>();
			JSONArray tableSource = new JSONArray();

			String fileFormat = request.getParameter("fileformat");
			String fileName = "GlobalSearch";

			switch (category) {
			case "0": {
				tableHeader.addAll(DownloadHelper.getHeaderForFeatures());
				tableField.addAll(DownloadHelper.getFieldsForFeatures());

				Map resp = processGlobalSearchFeature(request);
				Map respBody = (Map) resp.get("response");
				List<GenomeFeature> features = dataApi.bindDocuments((List<Map>) respBody.get("docs"), GenomeFeature.class);

				for (GenomeFeature feature : features) {
					tableSource.add(feature.toJSONObject());
				}
				break;
			}
			case "1": {
				tableHeader.addAll(DownloadHelper.getHeaderForGenomes());
				tableField.addAll(DownloadHelper.getFieldsForGenomes());

				Map resp = processGlobalSearchGenome(request);
				Map respBody = (Map) resp.get("response");
				List<Genome> genomes = dataApi.bindDocuments((List<Map>) respBody.get("docs"), Genome.class);

				for (Genome genome : genomes) {
					tableSource.add(genome.toJSONObject());
				}
				break;
			}
			case "2": {
				tableHeader.addAll(Arrays.asList("Taxon ID", "Taxon Name", "# of Genomes"));
				tableField.addAll(Arrays.asList("taxon_id", "taxon_name", "genomes"));

				Map resp = processGlobalSearchTaxonomy(request);
				Map respBody = (Map) resp.get("response");
				List<Taxonomy> taxonomies = dataApi.bindDocuments((List<Map>) respBody.get("docs"), Taxonomy.class);

				for (Taxonomy taxonomy : taxonomies) {
					tableSource.add(taxonomy.toJSONObject());
				}
				break;
			}
			case "3": {
				tableHeader.addAll(DownloadHelper.getHeaderForTranscriptomicsExperiment());
				tableField.addAll(DownloadHelper.getFieldsForTranscriptomicsExperiment());

				Map resp = processGlobalSearchExperiment(request);
				Map respBody = (Map) resp.get("response");
				List<Map> docs = (List<Map>) respBody.get("docs");

				for (Map doc : docs) {
					JSONObject item = new JSONObject();
					item.putAll(doc);

					tableSource.add(item);
				}
				break;
			}
			}

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

	private void processGlobalSearch(ResourceRequest request, ResourceResponse response) throws IOException {

		//		String spellCheck = request.getParameter("spellcheck");
		JSONObject result = new JSONObject();

		try {
			//			if(Boolean.parseBoolean(spellCheck)){
			//				JSONObject a = solr.getSpellCheckerResult(keyword);
			//				result.put("suggestion", a.get("suggestion"));
			//			}
			//			result.put("suggestion", new JSONArray());

			JSONArray data = new JSONArray();

			{
				Map resp = processGlobalSearchFeature(request);

				JSONObject obj = new JSONObject();
				obj.putAll(resp);

				data.add(obj);
			}
			{
				Map resp = processGlobalSearchGenome(request);

				JSONObject obj = new JSONObject();
				obj.putAll(resp);

				data.add(obj);
			}
			{
				Map resp = processGlobalSearchTaxonomy(request);

				JSONObject obj = new JSONObject();
				obj.putAll(resp);

				data.add(obj);
			}
			{
				Map resp = processGlobalSearchExperiment(request);

				JSONObject obj = new JSONObject();
				obj.putAll(resp);

				data.add(obj);
			}
			result.put("data", data);
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

		result.writeJSONString(response.getWriter());
	}

	private Map processGlobalSearchFeature(ResourceRequest request) throws IOException {

		DataApiHandler dataApi = new DataApiHandler(request);
		String keyword = request.getParameter("keyword");
		String need = request.getParameter("need");

		SolrQuery query = new SolrQuery(StringHelper.stripQuoteAndParseSolrKeywordOperator(keyword));
		if (need.equals("search")) {
			query.setRows(3).set("hl", "on").set("hl.fl", "*");
		}
		else {
			query.setRows(dataApi.MAX_ROWS);
		}

		query.setFields(StringUtils.join(DownloadHelper.getFieldsForFeatures(), ","));
		query.addFilterQuery("!feature_type:source");
		query.addFilterQuery("!annotation:BRC1");

		LOGGER.trace("processGlobalSearch: [{}] {}", SolrCore.FEATURE.getSolrCoreName(), query);
		String apiResponse = dataApi.solrQuery(SolrCore.FEATURE, query);

		Map resp = jsonReader.readValue(apiResponse);

		return resp;
	}

	private Map processGlobalSearchGenome(ResourceRequest request) throws IOException {

		DataApiHandler dataApi = new DataApiHandler(request);
		String keyword = request.getParameter("keyword");
		String need = request.getParameter("need");

		SolrQuery query = new SolrQuery(StringHelper.stripQuoteAndParseSolrKeywordOperator(keyword));
		if (need.equals("search")) {
			query.setRows(3).set("hl", "on").set("hl.fl", "*");
		}
		else {
			query.setRows(dataApi.MAX_ROWS);
		}

		query.setFields(StringUtils.join(DownloadHelper.getFieldsForGenomes(), ","));

		LOGGER.trace("processGlobalSearch: [{}] {}", SolrCore.GENOME.getSolrCoreName(), query);
		String apiResponse = dataApi.solrQuery(SolrCore.GENOME, query);

		Map resp = jsonReader.readValue(apiResponse);

		return resp;
	}

	private Map processGlobalSearchTaxonomy(ResourceRequest request) throws IOException {

		DataApiHandler dataApi = new DataApiHandler(request);
		String keyword = request.getParameter("keyword");
		String need = request.getParameter("need");

		SolrQuery query = new SolrQuery(StringHelper.stripQuoteAndParseSolrKeywordOperator(keyword));
		if (need.equals("search")) {
			query.setRows(3).set("hl", "on").set("hl.fl", "*");
		}
		else {
			query.setRows(dataApi.MAX_ROWS);
		}

		query.setFields("taxon_id,taxon_name,taxon_rank,genomes");

		LOGGER.trace("processGlobalSearch: [{}] {}", SolrCore.TAXONOMY.getSolrCoreName(), query);
		String apiResponse = dataApi.solrQuery(SolrCore.TAXONOMY, query);

		Map resp = jsonReader.readValue(apiResponse);

		return resp;
	}

	private Map processGlobalSearchExperiment(ResourceRequest request) throws IOException {

		DataApiHandler dataApi = new DataApiHandler(request);
		String keyword = request.getParameter("keyword");
		String need = request.getParameter("need");

		SolrQuery query = new SolrQuery(StringHelper.stripQuoteAndParseSolrKeywordOperator(keyword));
		if (need.equals("search")) {
			query.setRows(3).set("hl", "on").set("hl.fl", "*");
		}
		else {
			query.setRows(dataApi.MAX_ROWS);
		}

		query.setFields("eid,accession,title,description,organism,strain,mutant,timeseries,condition");

		LOGGER.trace("processGlobalSearch: [{}] {}", SolrCore.TRANSCRIPTOMICS_EXPERIMENT.getSolrCoreName(), query);
		String apiResponse = dataApi.solrQuery(SolrCore.TRANSCRIPTOMICS_EXPERIMENT, query);

		Map resp = jsonReader.readValue(apiResponse);

		return resp;
	}
}
