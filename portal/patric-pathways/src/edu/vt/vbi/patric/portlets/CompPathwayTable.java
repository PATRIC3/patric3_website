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
import edu.vt.vbi.patric.common.DataApiHandler;
import edu.vt.vbi.patric.common.ExcelHelper;
import edu.vt.vbi.patric.common.SiteHelper;
import edu.vt.vbi.patric.common.SolrCore;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.util.*;

public class CompPathwayTable extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(CompPathwayTable.class);

	private ObjectReader jsonReader;

	@Override
	public void init() throws PortletException {
		super.init();

		ObjectMapper objectMapper = new ObjectMapper();
		jsonReader = objectMapper.reader(Map.class);
	}

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		SiteHelper.setHtmlMetaElements(request, response, "Pathways");
		response.setContentType("text/html");
		response.setTitle("Pathways");
		PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/comp_pathway_table.jsp");
		prd.include(request, response);
	}

	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		String need = request.getParameter("need");

		DataApiHandler dataApi = new DataApiHandler(request);

		String pathwayClass = request.getParameter("pathway_class");
		String pathwayId = request.getParameter("pathway_id");
		String ecNumber = request.getParameter("ec_number");
		String annotation = request.getParameter("algorithm");
		String contextId = request.getParameter("cId");
		String contextType = request.getParameter("cType");

		JSONObject jsonResult;

		switch (need) {
		case "0":
			jsonResult = this.processPathwayTab(dataApi, pathwayClass, pathwayId, ecNumber, annotation, contextType, contextId);
			response.setContentType("application/json");
			jsonResult.writeJSONString(response.getWriter());
			break;
		case "1":
			jsonResult = this.processEcNumberTab(dataApi, pathwayClass, pathwayId, ecNumber, annotation, contextType, contextId);
			response.setContentType("application/json");
			jsonResult.writeJSONString(response.getWriter());
			break;
		case "2":
			jsonResult = this.processGeneTab(dataApi, pathwayClass, pathwayId, ecNumber, annotation, contextType, contextId);
			response.setContentType("application/json");
			jsonResult.writeJSONString(response.getWriter());
			break;
		case "filter":
			getFilterData(request, response);
			break;
		case "download":
			this.processDownload(request, response);
			break;
		case "getFeatureIds":
			this.processFeatureIds(request, response);
			break;
		}
	}

	@SuppressWarnings("unchecked")
	private JSONObject processPathwayTab(DataApiHandler dataApi, String pathwayClass, String pathwayId, String ecNumber, String annotation, String contextType,
			String contextId) throws PortletException, IOException {

		JSONObject jsonResult = new JSONObject();
		SolrQuery query = new SolrQuery("*:*");

		if (pathwayClass != null && !pathwayClass.equals("")) {
			query.addFilterQuery("pathway_class:" + pathwayClass);
		}

		if (pathwayId != null && !pathwayId.equals("")) {
			query.addFilterQuery("pathway_id:" + pathwayId);
		}

		if (ecNumber != null && !ecNumber.equals("")) {
			query.addFilterQuery("ec_number:" + ecNumber);
		}

		if (annotation != null && !annotation.equals("")) {
			query.addFilterQuery("annotation:" + annotation);
		}

		if (contextType.equals("genome")) {
			query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "genome_id:" + contextId));
		}
		else if (contextType.equals("taxon")) {
			query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + contextId));
		}

		JSONArray items = new JSONArray();
		int count_total = 0;
		int count_unique = 0;

		try {
			Set<String> listPathwayIds = new HashSet<>();
			Map<String, JSONObject> uniquePathways = new HashMap<>();

			// get pathway stat
			query.setRows(0).setFacet(true);
			query.add("json.facet",
					"{stat:{field:{field:pathway_id,limit:-1,facet:{genome_count:\"unique(genome_id)\",gene_count:\"unique(feature_id)\",ec_count:\"unique(ec_number)\",genome_ec:\"unique(genome_ec)\"}}}}");

			LOGGER.trace("[{}] {}", SolrCore.PATHWAY.getSolrCoreName(), query);

			String apiResponse = dataApi.solrQuery(SolrCore.PATHWAY, query);

			Map resp = jsonReader.readValue(apiResponse);
			List<Map> buckets = (List<Map>) ((Map) ((Map) resp.get("facets")).get("stat")).get("buckets");

			Map<String, Map> mapStat = new HashMap<>();
			for (Map value : buckets) {
				mapStat.put((String) value.get("val"), value);
				listPathwayIds.add((String) value.get("val"));
			}

			if (!listPathwayIds.isEmpty()) {
				// get pathway list
				SolrQuery pathwayQuery = new SolrQuery("pathway_id:(" + StringUtils.join(listPathwayIds, " OR ") + ")");
				pathwayQuery.setFields("pathway_id,pathway_name,pathway_class");
				pathwayQuery.setRows(Math.max(dataApi.MAX_ROWS, listPathwayIds.size()));

				LOGGER.trace("[{}] {}", SolrCore.PATHWAY_REF.getSolrCoreName(), pathwayQuery);

				apiResponse = dataApi.solrQuery(SolrCore.PATHWAY_REF, pathwayQuery);
				resp = jsonReader.readValue(apiResponse);
				Map respBody = (Map) resp.get("response");

				List<Map> sdl = (List<Map>) respBody.get("docs");

				for (Map doc : sdl) {
					String aPathwayId = doc.get("pathway_id").toString();
					Map stat = mapStat.get(aPathwayId);

					if (!uniquePathways.containsKey(aPathwayId) && !stat.get("genome_count").toString().equals("0")) {
						JSONObject item = new JSONObject();
						item.put("pathway_id", aPathwayId);
						item.put("pathway_name", doc.get("pathway_name"));
						item.put("pathway_class", doc.get("pathway_class"));

						float genome_ec = Float.parseFloat(stat.get("genome_ec").toString());
						float genome_count = Float.parseFloat(stat.get("genome_count").toString());
						float ec_count = Float.parseFloat(stat.get("ec_count").toString());
						float gene_count = Float.parseFloat(stat.get("gene_count").toString());

						float ec_cons = 0;
						float gene_cons = 0;
						if (genome_count > 0 && ec_count > 0) {
							ec_cons = genome_ec / genome_count / ec_count * 100;
							gene_cons = gene_count / genome_count / ec_count;
						}

						item.put("ec_cons", ec_cons);
						item.put("ec_count", ec_count);
						item.put("gene_cons", gene_cons);
						item.put("gene_count", gene_count);
						item.put("genome_count", genome_count);
						item.put("algorithm", annotation);

						uniquePathways.put(aPathwayId, item);
					}
				}

				for (Map.Entry<String, JSONObject> pathway : uniquePathways.entrySet()) {
					items.add(pathway.getValue());
				}
				count_total = uniquePathways.entrySet().size();
				count_unique = count_total;
			}
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

		// Wrapping jsonResult
		try {
			jsonResult.put("total", count_total);
			jsonResult.put("results", items);
			jsonResult.put("unique", count_unique);
		}
		catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
		}

		return jsonResult;
	}

	@SuppressWarnings("unchecked")
	private JSONObject processEcNumberTab(DataApiHandler dataApi, String pathwayClass, String pathwayId, String ecNumber, String annotation, String contextType,
			String contextId) throws PortletException, IOException {

		JSONObject jsonResult = new JSONObject();
		SolrQuery query = new SolrQuery("*:*");

		if (pathwayClass != null && !pathwayClass.equals("")) {
			query.addFilterQuery("pathway_class:" + pathwayClass);
		}

		if (pathwayId != null && !pathwayId.equals("")) {
			query.addFilterQuery("pathway_id:" + pathwayId);
		}

		if (ecNumber != null && !ecNumber.equals("")) {
			query.addFilterQuery("ec_number:" + ecNumber);
		}

		if (annotation != null && !annotation.equals("")) {
			query.addFilterQuery("annotation:" + annotation);
		}

		if (contextType.equals("genome")) {
			query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "genome_id:" + contextId));
		}
		else if (contextType.equals("taxon")) {
			query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + contextId));
		}

		JSONArray items = new JSONArray();
		int count_total = 0;
		int count_unique = 0;

		try {
			Set<String> listPathwayIds = new HashSet<>();
			Set<String> listEcNumbers = new HashSet<>();

			// get pathway stat
			query.setRows(0).setFacet(true);
			query.add("json.facet",
					"{stat:{field:{field:pathway_ec,limit:-1,facet:{genome_count:\"unique(genome_id)\",gene_count:\"unique(feature_id)\",ec_count:\"unique(ec_number)\"}}}}");

			LOGGER.trace("[{}] {}", SolrCore.PATHWAY.getSolrCoreName(), query);

			String apiResponse = dataApi.solrQuery(SolrCore.PATHWAY, query);
			Map resp = jsonReader.readValue(apiResponse);
			List<Map> buckets = (List<Map>) ((Map) ((Map) resp.get("facets")).get("stat")).get("buckets");

			Map<String, Map> mapStat = new HashMap<>();
			for (Map value : buckets) {

				if (!value.get("genome_count").toString().equals("0")) {
					mapStat.put(value.get("val").toString(), value);

					String[] pathway_ec = value.get("val").toString().split("_");
					listPathwayIds.add(pathway_ec[0]);
					listEcNumbers.add(pathway_ec[1]);
				}
			}

			// get pathway list
			SolrQuery pathwayQuery = new SolrQuery("*:*");
			if (!listPathwayIds.isEmpty()) {
				pathwayQuery.setQuery("pathway_id:(" + StringUtils.join(listPathwayIds, " OR ") + ")");

				pathwayQuery.setFields("pathway_id,pathway_name,pathway_class,ec_number,ec_description");
				pathwayQuery.setRows(Math.max(dataApi.MAX_ROWS, listPathwayIds.size()));

				LOGGER.trace("[{}] {}", SolrCore.PATHWAY_REF.getSolrCoreName(), pathwayQuery);

				apiResponse = dataApi.solrQuery(SolrCore.PATHWAY_REF, pathwayQuery);
				resp = jsonReader.readValue(apiResponse);
				Map respBody = (Map) resp.get("response");

				List<Map> sdl = (List<Map>) respBody.get("docs");

				for (Map doc : sdl) {
					String aPathwayId = doc.get("pathway_id").toString();
					String aEcNumber = doc.get("ec_number").toString();
					Map stat = mapStat.get(aPathwayId + "_" + aEcNumber);

					if (stat != null && !stat.get("genome_count").toString().equals("0")) {
						JSONObject item = new JSONObject();
						item.put("pathway_id", aPathwayId);
						item.put("pathway_name", doc.get("pathway_name"));
						item.put("pathway_class", doc.get("pathway_class"));

						float genome_count = Float.parseFloat(stat.get("genome_count").toString());
						float gene_count = Float.parseFloat(stat.get("gene_count").toString());

						item.put("ec_name", doc.get("ec_description"));
						item.put("ec_number", doc.get("ec_number"));
						item.put("gene_count", gene_count);
						item.put("genome_count", genome_count);
						item.put("algorithm", annotation);

						items.add(item);
					}
				}
				count_total = items.size();
				count_unique = listEcNumbers.size();
			}
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

		// Wrapping jsonResult
		jsonResult.put("total", count_total);
		jsonResult.put("results", items);
		jsonResult.put("unique", count_unique);

		return jsonResult;
	}

	@SuppressWarnings("unchecked")
	private JSONObject processGeneTab(DataApiHandler dataApi, String pathwayClass, String pathwayId, String ecNumber, String annotation, String contextType, String contextId)
			throws PortletException, IOException {

		JSONObject jsonResult = new JSONObject();
		SolrQuery query = new SolrQuery("*:*");

		if (pathwayClass != null && !pathwayClass.equals("")) {
			query.addFilterQuery("pathway_class:" + pathwayClass);
		}

		if (pathwayId != null && !pathwayId.equals("")) {
			query.addFilterQuery("pathway_id:" + pathwayId);
		}

		if (ecNumber != null && !ecNumber.equals("")) {
			query.addFilterQuery("ec_number:" + ecNumber);
		}

		if (annotation != null && !annotation.equals("")) {
			query.addFilterQuery("annotation:" + annotation);
		}

		if (contextType.equals("genome")) {
			query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "genome_id:" + contextId));
		}
		else if (contextType.equals("taxon")) {
			query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + contextId));
		}

		JSONArray items = new JSONArray();
		int count_total = 0;
		int count_unique = 0;

		try {
			Set<String> listFeatureIds = new HashSet<>();

			query.setFields("pathway_id,pathway_name,feature_id,ec_number,ec_description");
			query.setRows(dataApi.MAX_ROWS);

			LOGGER.trace("[{}] {}", SolrCore.PATHWAY.getSolrCoreName(), query);

			String apiResponse = dataApi.solrQuery(SolrCore.PATHWAY, query);
			Map resp = jsonReader.readValue(apiResponse);
			Map respBody = (Map) resp.get("response");

			List<Map> sdl = (List<Map>) respBody.get("docs");

			Map<String, Map> mapStat = new HashMap<>();
			for (Map doc : sdl) {

				mapStat.put(doc.get("feature_id").toString(), doc);
				listFeatureIds.add(doc.get("feature_id").toString());
			}

			// get pathway list
			if (!listFeatureIds.isEmpty()) {
				SolrQuery featureQuery = new SolrQuery("feature_id:(" + StringUtils.join(listFeatureIds, " OR ") + ")");
				featureQuery.setFields("genome_name,genome_id,accession,alt_locus_tag,refseq_locus_tag,patric_id,feature_id,gene,product");
				featureQuery.setRows(Math.max(dataApi.MAX_ROWS, listFeatureIds.size()));

				LOGGER.trace("[{}] {}", SolrCore.FEATURE.getSolrCoreName(), featureQuery);

				apiResponse = dataApi.solrQuery(SolrCore.FEATURE, featureQuery);
				resp = jsonReader.readValue(apiResponse);
				respBody = (Map) resp.get("response");

				List<GenomeFeature> features = dataApi.bindDocuments((List<Map>) respBody.get("docs"), GenomeFeature.class);

				for (GenomeFeature feature : features) {
					String featureId = feature.getId();
					Map stat = mapStat.get(featureId);

					JSONObject item = new JSONObject();
					item.put("genome_name", feature.getGenomeName());
					item.put("genome_id", feature.getGenomeId());
					item.put("accession", feature.getAccession());
					item.put("feature_id", feature.getId());
					item.put("alt_locus_tag", feature.getAltLocusTag());
					item.put("refseq_locus_tag", feature.getRefseqLocusTag());
					item.put("algorithm", annotation);
					item.put("patric_id", feature.getPatricId());
					item.put("gene", feature.getGene());
					item.put("product", feature.getProduct());

					item.put("ec_name", stat.get("ec_description"));
					item.put("ec_number", stat.get("ec_number"));
					item.put("pathway_id", stat.get("pathway_id"));
					item.put("pathway_name", stat.get("pathway_name"));

					items.add(item);
				}
				count_total = items.size();
				count_unique = count_total;
			}
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

		// Wrapping jsonResult
		jsonResult.put("total", count_total);
		jsonResult.put("results", items);
		jsonResult.put("unique", count_unique);

		return jsonResult;
	}

	@SuppressWarnings("unchecked")
	private void getFilterData(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
		JSONObject val = new JSONObject();
		try {
			if (request.getParameter("val") != null) {
				LOGGER.trace("parsing param: {}", request.getParameter("val"));
				val = (JSONObject) (new JSONParser()).parse(request.getParameter("val"));
			}
		}
		catch (ParseException e) {
			LOGGER.error(e.getMessage(), e);
		}

		DataApiHandler dataApi = new DataApiHandler(request);
		JSONObject json = new JSONObject();

		String need = val.get("need") != null ? val.get("need").toString() : "";

		JSONObject defaultItem = new JSONObject();
		defaultItem.put("name", "ALL");
		defaultItem.put("value", "ALL");

		JSONArray items = new JSONArray();
		items.add(defaultItem);

		switch (need) {
		case "pathway":
			try {
				Map facets = dataApi.getPivotFacets(SolrCore.PATHWAY_REF, "*:*", null, "pathway_id,pathway_name");

				Map pivot = (Map) facets.get("pathway_id,pathway_name");

				for (Map.Entry entry : (Iterable<Map.Entry>) pivot.entrySet()) {

					Map entryValue = (Map) entry.getValue();
					String key = (String) entryValue.keySet().iterator().next();

					JSONObject item = new JSONObject();
					item.put("name", key);
					item.put("value", entry.getKey());

					items.add(item);
				}

				json.put(need, items);
			}
			catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}

			break;
		case "ec":
			try {
				Map facets = dataApi.getFieldFacets(SolrCore.PATHWAY_REF, "*:*", null, "ec_number");
				Map list = (Map) ((Map) facets.get("facets")).get("ec_number");

				for (Map.Entry entry : (Iterable<Map.Entry>) list.entrySet()) {
					JSONObject i = new JSONObject();
					i.put("name", entry.getKey());
					i.put("value", entry.getKey());

					items.add(i);
				}

				json.put(need, items);
			}
			catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}

			break;
		case "parent":
			try {
				Map facets = dataApi.getFieldFacets(SolrCore.PATHWAY_REF, "*:*", null, "pathway_class");
				Map list = (Map) ((Map) facets.get("facets")).get("pathway_class");

				for (Map.Entry entry : (Iterable<Map.Entry>) list.entrySet()) {
					JSONObject i = new JSONObject();
					i.put("name", entry.getKey());
					i.put("value", entry.getKey());

					items.add(i);
				}

				json.put(need, items);
			}
			catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
			break;
		case "algorithm":
			JSONObject annotationPATRIC = new JSONObject();
			annotationPATRIC.put("name", "PATRIC");
			annotationPATRIC.put("value", "PATRIC");
			items.add(annotationPATRIC);

			json.put(need, items);
			break;
		}
		response.setContentType("application/json");
		json.writeJSONString(response.getWriter());
	}

	private void processDownload(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		List<String> tableHeader = new ArrayList<>();
		List<String> tableField = new ArrayList<>();
		JSONArray tableSource = null;
		String fileFormat = request.getParameter("fileformat");
		String fileName;

		DataApiHandler dataApi = new DataApiHandler(request);

		String pathwayClass = request.getParameter("pClass");
		String pathwayId = request.getParameter("pId");
		String ecNumber = request.getParameter("ecN");
		String annotation = request.getParameter("alg");
		String contextId = request.getParameter("cId");
		String contextType = request.getParameter("cType");

		if (request.getParameter("aT").equals("0")) {
			tableSource = (JSONArray) this.processPathwayTab(dataApi, pathwayClass, pathwayId, ecNumber, annotation, contextType, contextId).get("results");

			if (contextType.equals("taxon")) {
				tableHeader.addAll(Arrays.asList("Pathway ID", "Pathway Name", "Pathway Class", "Annotation", "Genome Count",
						"Unique Gene Count", "Unique EC Count", "Ec Conservation %", "Gene Conservation"));
				tableField.addAll(Arrays.asList("pathway_id", "pathway_name", "pathway_class", "algorithm", "genome_count", "gene_count",
						"ec_count", "ec_cons", "gene_cons"));
			}
			else if (contextType.equals("genome")) {
				tableHeader.addAll(Arrays.asList("Pathway ID", "Pathway Name", "Pathway Class", "Annotation", "Unique Gene Count",
						"Unique EC Count", "Ec Conservation %", "Gene Conservation"));
				tableField.addAll(Arrays.asList("pathway_id", "pathway_name", "pathway_class", "algorithm", "gene_count", "ec_count",
						"ec_cons", "gene_cons"));
			}
		}
		else if (request.getParameter("aT").equals("1")) {
			tableSource = (JSONArray) this.processEcNumberTab(dataApi, pathwayClass, pathwayId, ecNumber, annotation, contextType, contextId).get("results");
			if (contextType.equals("taxon")) {
				tableHeader.addAll(Arrays.asList("Pathway ID", "Pathway Name", "Pathway Class", "Annotation", "EC Number", "EC Description",
						"Genome Count", "Unique Gene Count"));
				tableField.addAll(Arrays.asList("pathway_id", "pathway_name", "pathway_class", "algorithm", "ec_number", "ec_name",
						"genome_count", "gene_count"));
			}
			else if (contextType.equals("genome")) {
				tableHeader.addAll(Arrays.asList("Pathway ID", "Pathway Name", "Pathway Class", "Annotation", "EC Number", "EC Description",
						"Unique Gene Count"));
				tableField.addAll(Arrays.asList("pathway_id", "pathway_name", "pathway_class", "algorithm", "ec_number", "ec_name",
						"gene_count"));
			}
		}
		else if (request.getParameter("aT").equals("2")) {
			tableSource = (JSONArray) this.processGeneTab(dataApi, pathwayClass, pathwayId, ecNumber, annotation, contextType, contextId).get("results");
			tableHeader.addAll(Arrays
					.asList("Feature ID", "Genome Name", "Accession", "PATRIC ID", "RefSeq Locus Tag", "Alt Locus Tag", "Gene Symbol", "Product Name",
							"Annotation", "Pathway ID", "Pathway Name", "Ec Number", "EC Description"));
			tableField.addAll(Arrays
					.asList("feature_id", "genome_name", "accession", "patric_id", "refseq_locus_tag", "alt_locus_tag", "gene", "product", "algorithm",
							"pathway_id", "pathway_name", "ec_number", "ec_name"));
		}

		fileName = "CompPathwayTable";
		ExcelHelper excel = new ExcelHelper("xssf", tableHeader, tableField, tableSource);
		excel.buildSpreadsheet();

		if (fileFormat.equalsIgnoreCase("xlsx")) {

			response.setContentType("application/octetstream");
			response.setProperty("Content-Disposition", "attachment; filename=\"" + fileName + "." + fileFormat + "\"");

			excel.writeSpreadsheettoBrowser(response.getPortletOutputStream());
		}
		else if (fileFormat.equalsIgnoreCase("txt")) {

			response.setContentType("application/octetstream");
			response.setProperty("Content-Disposition", "attachment; filename=\"" + fileName + "." + fileFormat + "\"");

			response.getWriter().write(excel.writeToTextFile());
		}
	}

	private void processFeatureIds(ResourceRequest request, ResourceResponse response) throws IOException {
		String cId = request.getParameter("cId");
		String cType = request.getParameter("cType");
		String map = request.getParameter("map");
		String algorithm = request.getParameter("algorithm");
		String ec_number = request.getParameter("ec_number");
		String featureList = request.getParameter("featureList");

		DataApiHandler dataApi = new DataApiHandler(request);
		JSONArray items = new JSONArray();

		try {
			SolrQuery query = new SolrQuery("*:*");

			if (cType != null && cType.equals("taxon")) {
				query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + cId));
			}
			else if (cType != null && cType.equals("genome")) {
				query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "genome_id:(" + cId + ")"));
			}

			if (map != null && !map.equals("")) {
				query.addFilterQuery("pathway_id:(" + map.replaceAll(",", " OR ") + ")");
			}

			if (algorithm != null && !algorithm.equals("")) {
				query.addFilterQuery("annotation:(" + algorithm + ")");
			}

			if (ec_number != null && !ec_number.equals("")) {
				query.addFilterQuery("ec_number:(" + ec_number.replaceAll(",", " OR ") + ")");
			}

			if (featureList != null && !featureList.equals("")) {
				query.addFilterQuery("feature_id:(" + featureList.replaceAll(",", " OR ") + ")");
			}

			query.setRows(dataApi.MAX_ROWS).setFields("feature_id");

			LOGGER.debug("processFeatureIds: [{}]{}", SolrCore.PATHWAY.getSolrCoreName(), query);

			String apiResponse = dataApi.solrQuery(SolrCore.PATHWAY, query);

			Map resp = jsonReader.readValue(apiResponse);
			Map respBody = (Map) resp.get("response");

			List<Map> features = (List<Map>) respBody.get("docs");

			for (Map feature : features) {
				items.add(feature.get("feature_id").toString());
			}
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

		items.writeJSONString(response.getWriter());
	}
}
