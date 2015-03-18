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

import com.google.gson.Gson;
import edu.vt.vbi.patric.beans.Genome;
import edu.vt.vbi.patric.common.ExcelHelper;
import edu.vt.vbi.patric.common.SiteHelper;
import edu.vt.vbi.patric.common.SolrCore;
import edu.vt.vbi.patric.common.SolrInterface;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.*;

public class PathwayFinder extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(PathwayFinder.class);

	public boolean isLoggedIn(PortletRequest request) {
		boolean isLoggedIn = false;

		PortletSession session = request.getPortletSession(true);

		if (session.getAttribute("authorizationToken", PortletSession.APPLICATION_SCOPE) != null) {
			isLoggedIn = true;
		}

		return isLoggedIn;
	}

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		response.setContentType("text/html");
		PortletRequestDispatcher prd;
		response.setTitle("Comparative Pathway Tool");
		new SiteHelper().setHtmlMetaElements(request, response, "Comparative Pathway Tool");

		String mode = request.getParameter("display_mode");
		Gson gson = new Gson();

		if (mode != null && mode.equals("result")) {

			String contextType = request.getParameter("context_type");
			String contextId = request.getParameter("context_id");

			String pk = request.getParameter("param_key");
			String ecNumber = request.getParameter("ec_number");
			String annotation = request.getParameter("algorithm");
			String pathwayId = request.getParameter("map");

			PortletSession session = request.getPortletSession(true);
			Map<String, String> key = gson.fromJson((String) session.getAttribute("key" + pk, PortletSession.APPLICATION_SCOPE), Map.class);

			String searchOn = "";
			String keyword = "";
			String genomeId = "";
			String taxonId = "";

			if (key != null && key.containsKey("search_on")) {
				searchOn = key.get("search_on");
			}
			if (key != null && key.containsKey("taxonId")) {
				taxonId = key.get("taxonId");
			}
			if (key != null && key.containsKey("genomeId")) {
				genomeId = key.get("genomeId");
			}
			if (searchOn.equalsIgnoreCase("Keyword") && key != null && key.get("keyword") != null) {
				keyword = key.get("keyword");
			}

			request.setAttribute("contextType", contextType);
			request.setAttribute("contextId", contextId);

			request.setAttribute("pk", pk);
			request.setAttribute("searchOn", searchOn);
			request.setAttribute("ecNumber", ecNumber);
			request.setAttribute("annotation", annotation);
			request.setAttribute("pathwayId", pathwayId);
			request.setAttribute("keyword", keyword);

			request.setAttribute("genomeId", genomeId);
			request.setAttribute("taxonId", taxonId);
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/comp_pathway_finder_result.jsp");
		}
		else if (mode != null && mode.equals("featurelist")) {

			String pk = request.getParameter("param_key");
			PortletSession session = request.getPortletSession(true);
			Map<String, String> key = gson.fromJson((String) session.getAttribute("key" + pk, PortletSession.APPLICATION_SCOPE), Map.class);

			String ecNumber = request.getParameter("ec_number");
			String annotation = request.getParameter("algorithm");
			String pathwayId = request.getParameter("map");

			if (ecNumber != null && !ecNumber.equals("")) {
				key.put("ec_number", ecNumber);
			}
			if (annotation != null && !annotation.equals("")) {
				key.put("algorithm", annotation);
			}
			if (pathwayId != null && !pathwayId.equals("")) {
				key.put("map", pathwayId);
			}

			LOGGER.debug("saving params:{}", key.toString());
			session.setAttribute("key" + pk, gson.toJson(key, Map.class), PortletSession.APPLICATION_SCOPE);

			request.setAttribute("pk", pk);

			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/comp_pathway_finder_result.jsp");
		}
		else {

			SolrInterface solr = new SolrInterface();
			String taxonId;
			String genomeId = "";
			String contextType = request.getParameter("context_type");
			String contextId = request.getParameter("context_id");

			if (contextType == null || contextType.equals("")) {
				contextType = "taxon";
			}
			else {
				if (contextId == null || contextId.equals("")) {
					contextId = "131567";
				}
			}

			String taxonName;
			if (contextType.equals("genome")) {
				genomeId = contextId;
				Genome genome = solr.getGenome(genomeId);
				taxonId = "" + genome.getTaxonId();
				taxonName = solr.getTaxonomy(genome.getTaxonId()).getTaxonName();
			}
			else {
				taxonId = contextId;
				taxonName = solr.getTaxonomy(Integer.parseInt(taxonId)).getTaxonName();
			}

			boolean isLoggedIn = isLoggedIn(request);

			request.setAttribute("contextType", contextType);
			request.setAttribute("contextId", contextId);
			request.setAttribute("taxonId", taxonId);
			request.setAttribute("genomeId", genomeId);
			request.setAttribute("taxonName", taxonName);
			request.setAttribute("isLoggedIn", isLoggedIn);

			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/comp_pathway_finder.jsp");
		}
		prd.include(request, response);
	}

	@SuppressWarnings("unchecked")
	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		String sraction = request.getParameter("sraction");
		Gson gson = new Gson();

		if (sraction != null && sraction.equals("save_params")) {

			String search_on = request.getParameter("search_on");
			String keyword = request.getParameter("keyword");
			String taxonId = request.getParameter("taxonId");
			String algorithm = request.getParameter("algorithm");
			String genomeId = request.getParameter("genomeId");
			String feature_id = request.getParameter("feature_id");

			Map<String, String> key = new HashMap<>();

			if (search_on != null) {
				key.put("search_on", search_on.trim());
				if (search_on.equalsIgnoreCase("Map_ID")) {
					key.put("map", keyword.trim());
				}
				else if (search_on.equalsIgnoreCase("Ec_Number")) {
					key.put("ec_number", keyword.trim());
				}
				else if (search_on.equalsIgnoreCase("Keyword")) {
					key.put("keyword", keyword.trim());
				}
			}
			if (taxonId != null && !taxonId.equals("")) {
				key.put("taxonId", taxonId);
			}

			if (genomeId != null && !genomeId.equals("")) {
				key.put("genomeId", genomeId);
			}

			if (algorithm != null && !algorithm.equals("")) {
				key.put("algorithm", algorithm);
			}

			if (feature_id != null && !feature_id.equalsIgnoreCase("")) {
				key.put("feature_id", feature_id);
			}

			Random g = new Random();
			int random = g.nextInt();

			LOGGER.debug("PathwayFinder params:{}", key.toString());
			PortletSession session = request.getPortletSession(true);
			session.setAttribute("key" + random, gson.toJson(key, Map.class), PortletSession.APPLICATION_SCOPE);

			PrintWriter writer = response.getWriter();
			writer.write("" + random);
			writer.close();
		}
		else {

			String need = request.getParameter("need");

			String pk = request.getParameter("pk");
			PortletSession session = request.getPortletSession(true);
			Map<String, String> key = gson.fromJson((String) session.getAttribute("key" + pk, PortletSession.APPLICATION_SCOPE), Map.class);

			switch (need) {
			case "0":
				JSONObject jsonResult = processPathwayTab(key.get("map"), key.get("ec_number"), key.get("algorithm"), key.get("taxonId"),
						key.get("genomeId"), key.get("keyword"));
				response.setContentType("application/json");
				jsonResult.writeJSONString(response.getWriter());
				break;
			case "1":
				jsonResult = processEcNumberTab(key.get("map"), key.get("ec_number"), key.get("algorithm"), key.get("taxonId"), key.get("genomeId"),
						key.get("keyword"));
				response.setContentType("application/json");
				jsonResult.writeJSONString(response.getWriter());
				break;
			case "2":
				jsonResult = processGeneTab(key.get("map"), key.get("ec_number"), key.get("algorithm"), key.get("taxonId"), key.get("genomeId"),
						key.get("keyword"));
				response.setContentType("application/json");
				jsonResult.writeJSONString(response.getWriter());
				break;
			case "download":
				processDownload(request, response);
				break;
			case "downloadMapFeatureTable":
				processDownloadMapFeatureTable(request, response);
				break;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private JSONObject processPathwayTab(String pathwayId, String ecNumber, String annotation, String taxonId, String genomeId, String keyword)
			throws PortletException, IOException {

		JSONObject jsonResult = new JSONObject();
		SolrQuery query = new SolrQuery("*:*");

		if (pathwayId != null && !pathwayId.equals("")) {
			query.addFilterQuery("pathway_id:" + pathwayId);
		}

		if (ecNumber != null && !ecNumber.equals("")) {
			query.addFilterQuery("ec_number:" + ecNumber);
		}

		if (annotation != null && !annotation.equals("") && !annotation.equalsIgnoreCase("ALL")) {
			query.addFilterQuery("annotation:" + annotation);
		}

		if (taxonId != null && !taxonId.equals("")) {
			query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + taxonId));
		}

		if (genomeId != null && !genomeId.equals("")) {
			query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "genome_id:(" + genomeId.replaceAll(",", " OR ") + ")"));
		}

		if (keyword != null && !keyword.equals("")) {
			query.setQuery(keyword);
		}

		SolrInterface solr = new SolrInterface();
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

			LOGGER.debug("processPathwayTab 1:{}", query.toString());
			QueryResponse qr = solr.getSolrServer(SolrCore.PATHWAY).query(query, SolrRequest.METHOD.POST);
			List<SimpleOrderedMap> buckets = (List) ((SimpleOrderedMap) ((SimpleOrderedMap) qr.getResponse().get("facets")).get("stat")).get(
					"buckets");

			Map<String, SimpleOrderedMap> mapStat = new HashMap<>();
			for (SimpleOrderedMap value : buckets) {
				mapStat.put(value.get("val").toString(), value);
				listPathwayIds.add(value.get("val").toString());
			}

			if (!listPathwayIds.isEmpty()) {
				// get pathway list
				SolrQuery pathwayQuery = new SolrQuery("pathway_id:(" + StringUtils.join(listPathwayIds, " OR ") + ")");
				pathwayQuery.setFields("pathway_id,pathway_name,pathway_class");
				pathwayQuery.setRows(10000);

				QueryResponse pathwayQueryResponse = solr.getSolrServer(SolrCore.PATHWAY_REF).query(pathwayQuery);
				SolrDocumentList sdl = pathwayQueryResponse.getResults();

				for (SolrDocument doc : sdl) {
					String aPathwayId = doc.get("pathway_id").toString();
					SimpleOrderedMap stat = mapStat.get(aPathwayId);

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
		catch (MalformedURLException | SolrServerException e) {
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
	private JSONObject processEcNumberTab(String pathwayId, String ecNumber, String annotation, String taxonId, String genomeId, String keyword)
			throws PortletException, IOException {

		JSONObject jsonResult = new JSONObject();
		SolrQuery query = new SolrQuery("*:*");

		if (pathwayId != null && !pathwayId.equals("")) {
			query.addFilterQuery("pathway_id:" + pathwayId);
		}

		if (ecNumber != null && !ecNumber.equals("")) {
			query.addFilterQuery("ec_number:" + ecNumber);
		}

		if (annotation != null && !annotation.equals("")) {
			query.addFilterQuery("annotation:" + annotation);
		}

		if (taxonId != null && !taxonId.equals("")) {
			query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + taxonId));
		}

		if (genomeId != null && !genomeId.equals("")) {
			query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "genome_id:(" + genomeId.replaceAll(",", " OR ") + ")"));
		}

		if (keyword != null && !keyword.equals("")) {
			query.setQuery(keyword);
		}

		SolrInterface solr = new SolrInterface();
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

			QueryResponse qr = solr.getSolrServer(SolrCore.PATHWAY).query(query, SolrRequest.METHOD.POST);

			List<SimpleOrderedMap> buckets = (List) ((SimpleOrderedMap) ((SimpleOrderedMap) qr.getResponse().get("facets")).get("stat"))
					.get("buckets");

			Map<String, SimpleOrderedMap> mapStat = new HashMap<>();
			for (SimpleOrderedMap value : buckets) {

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
				pathwayQuery.setRows(10000);
				// LOGGER.debug("{}", pathwayQuery.toString());
				QueryResponse pathwayQueryResponse = solr.getSolrServer(SolrCore.PATHWAY_REF).query(pathwayQuery, SolrRequest.METHOD.POST);
				SolrDocumentList sdl = pathwayQueryResponse.getResults();

				for (SolrDocument doc : sdl) {
					String aPathwayId = doc.get("pathway_id").toString();
					String aEcNumber = doc.get("ec_number").toString();
					SimpleOrderedMap stat = mapStat.get(aPathwayId + "_" + aEcNumber);

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
		catch (MalformedURLException | SolrServerException e) {
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
	private JSONObject processGeneTab(String pathwayId, String ecNumber, String annotation, String taxonId, String genomeId, String keyword)
			throws PortletException, IOException {

		JSONObject jsonResult = new JSONObject();
		SolrQuery query = new SolrQuery("*:*");

		if (pathwayId != null && !pathwayId.equals("")) {
			query.addFilterQuery("pathway_id:" + pathwayId);
		}

		if (ecNumber != null && !ecNumber.equals("")) {
			query.addFilterQuery("ec_number:" + ecNumber);
		}

		if (annotation != null && !annotation.equals("")) {
			query.addFilterQuery("annotation:" + annotation);
		}

		if (taxonId != null && !taxonId.equals("")) {
			query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + taxonId));
		}

		if (genomeId != null && !genomeId.equals("")) {
			query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "genome_id:(" + genomeId.replaceAll(",", " OR ") + ")"));
		}

		if (keyword != null && !keyword.equals("")) {
			query.setQuery(keyword);
		}

		SolrInterface solr = new SolrInterface();
		JSONArray items = new JSONArray();
		int count_total = 0;
		int count_unique = 0;

		try {
			Set<String> listFeatureIds = new HashSet<>();

			query.setFields("pathway_id,pathway_name,feature_id,ec_number,ec_description");
			query.setRows(100000);

			LOGGER.debug("processGeneTab 1/2: {}", query.toString());

			QueryResponse qr = solr.getSolrServer(SolrCore.PATHWAY).query(query, SolrRequest.METHOD.POST);
			SolrDocumentList sdl = qr.getResults();

			Map<String, SolrDocument> mapStat = new HashMap<>();
			for (SolrDocument doc : sdl) {

				mapStat.put(doc.get("feature_id").toString(), doc);
				listFeatureIds.add(doc.get("feature_id").toString());
			}

			// get pathway list
			if (!listFeatureIds.isEmpty()) {
				SolrQuery featureQuery = new SolrQuery("feature_id:(" + StringUtils.join(listFeatureIds, " OR ") + ")");
				featureQuery.setFields("genome_name,genome_id,accession,alt_locus_tag,refseq_locus_tag,seed_id,feature_id,gene,product");
				featureQuery.setRows(100000);

				LOGGER.debug("processGeneTab 2/2: {}", featureQuery.toString());

				QueryResponse featureQueryResponse = solr.getSolrServer(SolrCore.FEATURE).query(featureQuery, SolrRequest.METHOD.POST);
				sdl = featureQueryResponse.getResults();

				for (SolrDocument doc : sdl) {
					String featureId = doc.get("feature_id").toString();
					SolrDocument stat = mapStat.get(featureId);

					JSONObject item = new JSONObject();
					item.put("genome_name", doc.get("genome_name"));
					item.put("genome_id", doc.get("genome_id"));
					item.put("accession", doc.get("accession"));
					item.put("feature_id", doc.get("feature_id"));
					item.put("alt_locus_tag", doc.get("alt_locus_tag"));
					item.put("refseq_locus_tag", doc.get("refseq_locus_tag"));
					item.put("algorithm", annotation);
					item.put("seed_id", doc.get("seed_id"));
					item.put("gene", doc.get("gene"));
					item.put("product", doc.get("product"));

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
		catch (MalformedURLException | SolrServerException e) {
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

	private void processDownload(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		List<String> _tbl_header = new ArrayList<>();
		List<String> _tbl_field = new ArrayList<>();
		JSONArray _tbl_source = null;
		String fileFormat = request.getParameter("fileformat");
		String fileName;

		String search_on = request.getParameter("search_on");
		String keyword = request.getParameter("keyword");
		String ecNumber = request.getParameter("ecN");
		String pathwayId = request.getParameter("pId");

		if (search_on.equalsIgnoreCase("Map_ID")) {
			pathwayId = keyword.trim();
		}
		else if (search_on.equalsIgnoreCase("Ec_Number")) {
			ecNumber = keyword.trim();
		}
		else if (search_on.equalsIgnoreCase("Keyword")) {
			keyword = keyword.trim();
		}

		String genomeId = request.getParameter("genomeId");
		String taxonId = request.getParameter("taxonId");
		String annotation = request.getParameter("alg");

		if (request.getParameter("aT").equals("0")) {
			_tbl_source = (JSONArray) this.processPathwayTab(pathwayId, ecNumber, annotation, taxonId, genomeId, keyword).get("results");
			_tbl_header.addAll(Arrays
					.asList("Pathway ID", "Pathway Name", "Pathway Class", "Annotation", "Genome Count", "Unique Gene Count", "Unique EC Count",
							"Ec Conservation %", "Gene Conservation"));
			_tbl_field.addAll(Arrays
					.asList("pathway_id", "pathway_name", "pathway_class", "algorithm", "genome_count", "gene_count", "ec_count", "ec_cons",
							"gene_cons"));
		}
		else if (request.getParameter("aT").equals("1")) {
			_tbl_source = (JSONArray) this.processEcNumberTab(pathwayId, ecNumber, annotation, taxonId, genomeId, keyword).get("results");
			_tbl_header.addAll(Arrays
					.asList("Pathway ID", "Pathway Name", "Pathway Class", "Annotation", "EC Number", "EC Description", "Genome Count",
							"Unique Gene Count"));
			_tbl_field.addAll(Arrays
					.asList("pathway_id", "pathway_name", "pathway_class", "algorithm", "ec_number", "ec_name", "genome_count", "gene_count"));
		}
		else if (request.getParameter("aT").equals("2")) {
			_tbl_source = (JSONArray) this.processGeneTab(pathwayId, ecNumber, annotation, taxonId, genomeId, keyword).get("results");
			_tbl_header.addAll(Arrays
					.asList("Feature ID", "Genome Name", "Accession", "PATRIC ID", "RefSeq Locus Tag", "Alt Locus Tag", "Gene Symbol", "Product Name",
							"Annotation",
							"Pathway ID", "Pathway Name", "Ec Number", "EC Description"));
			_tbl_field.addAll(Arrays
					.asList("feature_id", "genome_name", "accession", "seed_id", "refseq_locus_tag", "alt_locus_tag", "gene", "product", "algorithm",
							"pathway_id",
							"pathway_name", "ec_number", "ec_name"));
		}

		fileName = "CompPathwayTable";
		ExcelHelper excel = new ExcelHelper("xssf", _tbl_header, _tbl_field, _tbl_source);
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

	private void processDownloadMapFeatureTable(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		List<String> _tbl_header = new ArrayList<>();
		List<String> _tbl_field = new ArrayList<>();

		String fileFormat = request.getParameter("fileformat");
		String fileName;

		String pathwayId = request.getParameter("map");
		String ecNumber = request.getParameter("ec_number");
		String annotation = request.getParameter("algorithm");

		String taxonId = request.getParameter("taxonId");
		String genomeId = request.getParameter("genomeId");

		JSONArray _tbl_source = (JSONArray) this.processGeneTab(pathwayId, ecNumber, annotation, taxonId, genomeId, "").get("results");
		_tbl_header.addAll(Arrays
				.asList("Feature ID", "Genome Name", "Accession", "PATRIC ID", "RefSeq Locus Tag", "Alt Locus Tag", "Gene Symbol", "Product Name",
						"Annotation", "Pathway ID", "Pathway Name", "Ec Number", "EC Description"));
		_tbl_field.addAll(Arrays
				.asList("feature_id", "genome_name", "accession", "seed_id", "refseq_locus_tag", "alt_locus_tag", "gene", "product", "algorithm",
						"pathway_id", "pathway_name", "ec_number", "ec_name"));

		fileName = "MapFeatureTable";
		ExcelHelper excel = new ExcelHelper("xssf", _tbl_header, _tbl_field, _tbl_source);
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
}
