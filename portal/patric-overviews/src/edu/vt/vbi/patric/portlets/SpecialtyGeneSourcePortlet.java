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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class SpecialtyGeneSourcePortlet extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpecialtyGeneSourcePortlet.class);

	private ObjectReader jsonReader;

	private ObjectWriter jsonWriter;

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

		PortletRequestDispatcher prd;

		SiteHelper.setHtmlMetaElements(request, response, "Specialty Gene Source");
		response.setTitle("Specialty Gene Source");
		prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/specialty_gene_source.jsp");
		prd.include(request, response);
	}

	@SuppressWarnings("unchecked")
	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		String sraction = request.getParameter("sraction");

		if (sraction != null && sraction.equals("save_params")) {
			Map<String, String> key = new HashMap<>();
			String source = request.getParameter("source");
			String keyword = request.getParameter("keyword");
			String state = request.getParameter("state");
			String exact_search_term = request.getParameter("exact_search_term");
			String search_on = request.getParameter("search_on");

			if (source != null && !source.equalsIgnoreCase("")) {
				key.put("source", source);
			}
			if (keyword != null) {
				key.put("keyword", keyword.trim());
			}
			if (state != null) {
				key.put("state", state);
			}
			if (exact_search_term != null) {
				key.put("exact_search_term", exact_search_term);
			}
			if (search_on != null) {
				key.put("search_on", search_on);
			}

			long pk = (new Random()).nextLong();

			SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, jsonWriter.writeValueAsString(key));

			PrintWriter writer = response.getWriter();
			writer.write("" + pk);
			writer.close();
		}
		else {
			if (sraction != null && sraction.equals("get_params")) {
				String ret = "";
				String pk = request.getParameter("pk");

				String json = SessionHandler.getInstance().get(SessionHandler.PREFIX + pk);
				if (json != null) {
					Map<String, String> key = jsonReader.readValue(json);
					ret = key.get("keyword");
				}

				PrintWriter writer = response.getWriter();
				writer.write("" + ret);
				writer.close();
			}
			else {

				String need = request.getParameter("need");
				JSONObject jsonResult = new JSONObject();

				switch (need) {
				case "0": {

					String pk = request.getParameter("pk");
					Map data = processSpecialtyGeneSourceTab(request);

					Map<String, String> key = (Map) data.get("key");
					int numFound = (Integer) data.get("numFound");
					JSONArray docs = (JSONArray) data.get("specialtyGeneSources");

					jsonResult.put("results", docs);
					jsonResult.put("total", numFound);

					// process facets
					if (data.containsKey("facets")) {
						SolrInterface solr = new SolrInterface();
						JSONObject facets = solr.formatFacetTree((Map) data.get("facets"));
						key.put("facets", facets.toJSONString());
						SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, jsonWriter.writeValueAsString(key));
					}

					response.setContentType("application/json");
					PrintWriter writer = response.getWriter();
					jsonResult.writeJSONString(writer);
					writer.close();
					break;
				}
				case "tree": {

					String pk = request.getParameter("pk");
					String state;
					Map<String, String> key = jsonReader.readValue(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk));

					if (key.containsKey("state")) {
						state = key.get("state");
					}
					else {
						state = request.getParameter("state");
					}

					key.put("state", state);

					SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, jsonWriter.writeValueAsString(key));

					JSONArray tree = new JSONArray();
					try {
						if (key.containsKey("facets") && !key.get("facets").isEmpty()) {

							JSONObject facet_fields = (JSONObject) (new JSONParser()).parse(key.get("facets"));
							SolrInterface solr = new SolrInterface();
							solr.setCurrentInstance(SolrCore.SPECIALTY_GENE);
							tree = solr.processStateAndTree(key, need, facet_fields, key.get("facet"), state, null, 4, false);
						}
					}
					catch (ParseException e) {
						LOGGER.error(e.getMessage(), e);
					}

					response.setContentType("application/json");
					PrintWriter writer = response.getWriter();
					tree.writeJSONString(writer);
					writer.close();
					break;
				}
				case "download": {
					List<String> tableHeader = new ArrayList<>();
					List<String> tableField = new ArrayList<>();
//					JSONArray tableSource = new JSONArray();

					String fileName = "SpecialtyGeneSource";
					String fileFormat = request.getParameter("fileformat");

					Map data = processSpecialtyGeneSourceTab(request);
					JSONArray tableSource = (JSONArray) data.get("specialtyGeneSources");

					tableHeader.addAll(DownloadHelper.getHeaderForSpecialtyGeneSource());
					tableField.addAll(DownloadHelper.getFieldsForSpecialtyGeneSource());

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
		}
	}

	private Map processSpecialtyGeneSourceTab(ResourceRequest request) throws IOException {

		String pk = request.getParameter("pk");
		String keyword = request.getParameter("keyword");
		String facet = request.getParameter("facet");
		String source = request.getParameter("source");

		DataApiHandler dataApi = new DataApiHandler(request);

		// homolog counts
		Map facets = dataApi.getFieldFacets(SolrCore.SPECIALTY_GENE_MAPPING, "source:" + source, null, "source_id");
		Map<String, Integer> hmCounts = (Map) ((Map) facets.get("facets")).get("source_id");

		// sp_genes
		//&sort=source_id+asc,+locus_tag+asc&facet.sort=count&start=0
		//&facet.field=genus&facet.field=species&facet.field=organism&facet.field=classification&wt=&fq=source:PATRIC_VF

		SolrInterface solr = new SolrInterface();
		Map<String, String> key = new HashMap<>();
		key.put("keyword", keyword);
		key.put("join", "source:" + source);
		key.put("fields", "property,source,source_id,gene_name,locus_tag,gene_id,gi,genus,species,organism,product,function,classification,pmid");

		// paging params
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

		String json = SessionHandler.getInstance().get(SessionHandler.PREFIX + pk);
		if (json == null) {
			key.put("facet", facet);
			key.put("keyword", keyword);

			SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, jsonWriter.writeValueAsString(key));
		}
		else {
			key = jsonReader.readValue(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk));
			key.put("facet", facet);
		}

		SolrQuery query = solr.buildSolrQuery(key, null, facet, start, end, false);

		LOGGER.trace("processSpecialtyGeneSourceTab: [{}] {}", SolrCore.SPECIALTY_GENE.getSolrCoreName(), query.toString());

		String apiResponse = dataApi.solrQuery(SolrCore.SPECIALTY_GENE, query);

		Map resp = jsonReader.readValue(apiResponse);
		Map respBody = (Map) resp.get("response");

		int numFound = (Integer) respBody.get("numFound");
		JSONArray docs = new JSONArray();
		List<Map> sdl = (List<Map>) respBody.get("docs");

		for (Map doc : sdl) {
			JSONObject row = new JSONObject();
			row.putAll(doc);
			String k = row.get("source_id").toString();

			if (hmCounts.containsKey(k)) {
				row.put("homologs", hmCounts.get(k));
			}
			else {
				row.put("homologs", 0);
			}
			docs.add(row);
		}

		Map response = new HashMap();
		response.put("key", key);
		response.put("numFound", numFound);
		response.put("specialtyGeneSources", docs);
		if (resp.containsKey("facet_counts")) {
			response.put("facets", resp.get("facet_counts"));
		}

		return response;
	}
}
