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

import edu.vt.vbi.patric.common.ExcelHelper;
import edu.vt.vbi.patric.common.SiteHelper;
import edu.vt.vbi.patric.common.SolrCore;
import edu.vt.vbi.patric.common.SolrInterface;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.PivotField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;

public class CompPathwayTable extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(CompPathwayTable.class);

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		new SiteHelper().setHtmlMetaElements(request, response, "Pathways");
		response.setContentType("text/html");
		response.setTitle("Pathways");
		PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/comp_pathway_table.jsp");
		prd.include(request, response);
	}

	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		String need = request.getParameter("need");

		String pathwayClass = request.getParameter("pathway_class");
		String pathwayId = request.getParameter("pathway_id");
		String ecNumber = request.getParameter("ec_number");
		String annotation = request.getParameter("algorithm");
		String contextId = request.getParameter("cId");
		String contextType = request.getParameter("cType");

		JSONObject jsonResult;

		switch (need) {
		case "0":
			jsonResult = this.processPathwayTab(pathwayClass, pathwayId, ecNumber, annotation, contextType, contextId);
			response.setContentType("application/json");
			jsonResult.writeJSONString(response.getWriter());
			break;
		case "1":
			jsonResult = this.processEcNumberTab(pathwayClass, pathwayId, ecNumber, annotation, contextType, contextId);
			response.setContentType("application/json");
			jsonResult.writeJSONString(response.getWriter());
			break;
		case "2":
			jsonResult = this.processGeneTab(pathwayClass, pathwayId, ecNumber, annotation, contextType, contextId);
			response.setContentType("application/json");
			jsonResult.writeJSONString(response.getWriter());
			break;
		case "filter":
			getFilterData(request, response);
			break;
		case "download":
			this.processDownload(request, response);
			break;
		}
	}

	@SuppressWarnings("unchecked")
	private JSONObject processPathwayTab(String pathwayClass, String pathwayId, String ecNumber, String annotation, String contextType,
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

			QueryResponse qr = solr.getSolrServer(SolrCore.PATHWAY).query(query);
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
				pathwayQuery.setRows(listPathwayIds.size());

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
	private JSONObject processEcNumberTab(String pathwayClass, String pathwayId, String ecNumber, String annotation, String contextType,
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

			QueryResponse qr = solr.getSolrServer(SolrCore.PATHWAY).query(query);

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
				pathwayQuery.setRows(listPathwayIds.size());
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
	private JSONObject processGeneTab(String pathwayClass, String pathwayId, String ecNumber, String annotation, String contextType, String contextId)
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

		SolrInterface solr = new SolrInterface();
		JSONArray items = new JSONArray();
		int count_total = 0;
		int count_unique = 0;

		try {
			Set<String> listFeatureIds = new HashSet<>();

			query.setFields("pathway_id,pathway_name,feature_id,ec_number,ec_description");
			query.setRows(1000000);
			QueryResponse qr = solr.getSolrServer(SolrCore.PATHWAY).query(query);
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
				featureQuery.setRows(listFeatureIds.size());
				// LOGGER.debug("{}", featureQuery.toString());
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

		JSONObject json = new JSONObject();

//		String algorithm = val.get("alg") != null ? val.get("alg").toString() : "";
//		String pid = val.get("pId") != null ? val.get("pId").toString() : "";
//		String pathway_class = val.get("pClass") != null ? val.get("pClass").toString() : "";
//		String ec_number = val.get("ecN") != null ? val.get("ecN").toString() : "";
//		String cType = val.get("cType") != null ? val.get("cType").toString() : "";
//		String cId = val.get("cId") != null ? val.get("cId").toString() : "";
		String need = val.get("need") != null ? val.get("need").toString() : "";

		SolrInterface solr = new SolrInterface();

		JSONObject defaultItem = new JSONObject();
		defaultItem.put("name", "ALL");
		defaultItem.put("value", "ALL");

		JSONArray items = new JSONArray();
		items.add(defaultItem);

		// common solrQuery
//		SolrQuery query = new SolrQuery("annotation:" + algorithm);
//		if (cType.equals("taxon")) {
//			query.addFilterQuery(
//					SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "genome_status:(complete OR wgs) AND taxon_lineage_ids:" + cId));
//		}
//		else {
//			query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "genome_status:(complete OR wgs) AND genome_id:" + cId));
//		}
//		query.setRows(0).setFacet(true).setFacetMinCount(1).setFacetLimit(-1);
//
//		if (!pathway_class.equals("")) {
//			query.addFilterQuery("pathway_class:" + pathway_class);
//		}
//		if (!pid.equals("")) {
//			query.addFilterQuery("pathway_name:" + pid);
//		}
//		if (!ec_number.equals("")) {
//			query.addFilterQuery("ec_number:" + ec_number);
//		}
		SolrQuery query = new SolrQuery("*:*");
		query.setRows(0).setFacet(true).setFacetMinCount(1).setFacetLimit(-1);

		switch (need) {
		case "pathway":
			try {
				query.addFacetPivotField("pathway_id,pathway_name");
				// QueryResponse qr = solr.getSolrServer(SolrCore.PATHWAY).query(query);
				QueryResponse qr = solr.getSolrServer(SolrCore.PATHWAY_REF).query(query);
				List<PivotField> pivotFacet = qr.getFacetPivot().get("pathway_id,pathway_name");

				for (PivotField field : pivotFacet) {
					JSONObject item = new JSONObject();
					item.put("name", field.getPivot().get(0).getValue());
					item.put("value", field.getValue());

					items.add(item);
					// LOGGER.debug("{},{}", field.getValue(), field.getPivot().get(0).getValue());
				}

				json.put(need, items);
			}
			catch (MalformedURLException | SolrServerException e) {
				LOGGER.error(e.getMessage(), e);
			}

			break;
		case "ec":
			try {
				query.addFacetField("ec_number");

				QueryResponse qr = solr.getSolrServer(SolrCore.PATHWAY_REF).query(query);
				FacetField facet = qr.getFacetField("ec_number");

				for (FacetField.Count item : facet.getValues()) {
					JSONObject i = new JSONObject();
					i.put("name", item.getName());
					i.put("value", item.getName());

					items.add(i);
				}

				json.put(need, items);
			}
			catch (MalformedURLException | SolrServerException e) {
				LOGGER.error(e.getMessage(), e);
			}

			break;
		case "parent":
			try {
				query.addFacetField("pathway_class");

				QueryResponse qr = solr.getSolrServer(SolrCore.PATHWAY_REF).query(query);
				FacetField facet = qr.getFacetField("pathway_class");

				for (FacetField.Count item : facet.getValues()) {
					JSONObject i = new JSONObject();
					i.put("name", item.getName());
					i.put("value", item.getName());

					items.add(i);
				}

				json.put(need, items);
			}
			catch (MalformedURLException | SolrServerException e) {
				LOGGER.error(e.getMessage(), e);
			}
			break;
		case "algorithm":
//			try {
//				query.addFacetField("annotation");
//
//				QueryResponse qr = solr.getSolrServer(SolrCore.PATHWAY).query(query);
//				FacetField facet = qr.getFacetField("annotation");
//
//				for (FacetField.Count item : facet.getValues()) {
//					JSONObject i = new JSONObject();
//					i.put("name", item.getName());
//					i.put("value", item.getName());
//
//					items.add(i);
//				}
				JSONObject annotationPATRIC = new JSONObject();
				annotationPATRIC.put("name", "PATRIC");
				annotationPATRIC.put("value", "PATRIC");
				items.add(annotationPATRIC);

				json.put(need, items);
//			}
//			catch (MalformedURLException | SolrServerException e) {
//				LOGGER.error(e.getMessage(), e);
//			}
			break;
		}
		response.setContentType("application/json");
		json.writeJSONString(response.getWriter());
	}

	private void processDownload(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		List<String> _tbl_header = new ArrayList<>();
		List<String> _tbl_field = new ArrayList<>();
		JSONArray _tbl_source = null;
		String fileFormat = request.getParameter("fileformat");
		String fileName;

		String pathwayClass = request.getParameter("pClass");
		String pathwayId = request.getParameter("pId");
		String ecNumber = request.getParameter("ecN");
		String annotation = request.getParameter("alg");
		String contextId = request.getParameter("cId");
		String contextType = request.getParameter("cType");

		if (request.getParameter("aT").equals("0")) {
			_tbl_source = (JSONArray) this.processPathwayTab(pathwayClass, pathwayId, ecNumber, annotation, contextType, contextId).get("results");

			if (contextType.equals("taxon")) {
				_tbl_header.addAll(Arrays.asList("Pathway ID", "Pathway Name", "Pathway Class", "Annotation", "Genome Count",
						"Unique Gene Count", "Unique EC Count", "Ec Conservation %", "Gene Conservation"));
				_tbl_field.addAll(Arrays.asList("pathway_id", "pathway_name", "pathway_class", "algorithm", "genome_count", "gene_count",
						"ec_count", "ec_cons", "gene_cons"));
			}
			else if (contextType.equals("genome")) {
				_tbl_header.addAll(Arrays.asList("Pathway ID", "Pathway Name", "Pathway Class", "Annotation", "Unique Gene Count",
						"Unique EC Count", "Ec Conservation %", "Gene Conservation"));
				_tbl_field.addAll(Arrays.asList("pathway_id", "pathway_name", "pathway_class", "algorithm", "gene_count", "ec_count",
						"ec_cons", "gene_cons"));
			}
		}
		else if (request.getParameter("aT").equals("1")) {
			_tbl_source = (JSONArray) this.processEcNumberTab(pathwayClass, pathwayId, ecNumber, annotation, contextType, contextId).get("results");
			if (contextType.equals("taxon")) {
				_tbl_header.addAll(Arrays.asList("Pathway ID", "Pathway Name", "Pathway Class", "Annotation", "EC Number", "EC Description",
						"Genome Count", "Unique Gene Count"));
				_tbl_field.addAll(Arrays.asList("pathway_id", "pathway_name", "pathway_class", "algorithm", "ec_number", "ec_name",
						"genome_count", "gene_count"));
			}
			else if (contextType.equals("genome")) {
				_tbl_header.addAll(Arrays.asList("Pathway ID", "Pathway Name", "Pathway Class", "Annotation", "EC Number", "EC Description",
						"Unique Gene Count"));
				_tbl_field.addAll(Arrays.asList("pathway_id", "pathway_name", "pathway_class", "algorithm", "ec_number", "ec_name",
						"gene_count"));
			}
		}
		else if (request.getParameter("aT").equals("2")) {
			_tbl_source = (JSONArray) this.processGeneTab(pathwayClass, pathwayId, ecNumber, annotation, contextType, contextId).get("results");
			_tbl_header.addAll(Arrays
					.asList("Feature ID", "Genome Name", "Accession", "PATRIC ID", "RefSeq Locus Tag", "Alt Locus Tag", "Gene Symbol", "Product Name",
							"Annotation", "Pathway ID", "Pathway Name", "Ec Number", "EC Description"));
			_tbl_field.addAll(Arrays
					.asList("feature_id", "genome_name", "accession", "seed_id", "refseq_locus_tag", "alt_locus_tag", "gene", "product", "algorithm",
							"pathway_id", "pathway_name", "ec_number", "ec_name"));
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
}
