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
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.*;

public class CompPathwayTable extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(CompPathwayTable.class);

//	JSONParser jsonParser = new JSONParser();

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		new SiteHelper().setHtmlMetaElements(request, response, "Pathways");
		response.setContentType("text/html");
		response.setTitle("Pathways");
		PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/comp_pathway_table.jsp");
		prd.include(request, response);
	}

	@SuppressWarnings("unchecked")
	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		String need = request.getParameter("need");

		switch (need) {
		case "0":
			getGridData(request, response);
			break;
		case "1":
			getGridData(request, response);
			break;
		case "2":
			getGridData(request, response);
			break;
		case "filter":
			getFilterData(request, response);
			break;
		}
	}

	private void getGridData (ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
		String need = request.getParameter("need");
		JSONObject jsonResult = new JSONObject();
//		JSONArray results = new JSONArray();
//		HashMap<String, String> key = new HashMap<>();

		SolrQuery query = new SolrQuery("*:*");

		String pathway_class = request.getParameter("pathway_class");
		if (pathway_class != null && !pathway_class.equals("")) {
			query.addFilterQuery("pathway_class:" + pathway_class);
		}

		String map = request.getParameter("pathway_id");
		if (map != null && !map.equals("")) {
			query.addFilterQuery("pathway_id:" + map);
		}

		String ec_number = request.getParameter("ec_number");
		if (ec_number != null && !ec_number.equals("")) {
			query.addFilterQuery("ec_number:" + ec_number);
		}

		String algorithm = request.getParameter("algorithm");
		if (algorithm != null && !algorithm.equals("")) {
			query.addFilterQuery("annotation:" + algorithm);
		}

		String cId = request.getParameter("cId");
		String cType = request.getParameter("cType");
		if (cType.equals("genome")) {
			query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "genome_id:" + cId));
		} else if (cType.equals("taxon")) {
			query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + cId));
		}

		// paging
//		int start = Integer.parseInt(request.getParameter("start"));
//		int end = start + Integer.parseInt(request.getParameter("limit"));
//		query.setStart(start);
//		if (end != -1) {
//			query.setRows(end);
//		}


		//		HashMap<String, String> sort = null;
//		if (request.getParameter("sort") != null) {
//			// sorting
//			JSONParser a = new JSONParser();
//			JSONArray sorter;
//			String sort_field = "";
//			String sort_dir = "";
//			try {
//				sorter = (JSONArray) a.parse(request.getParameter("sort"));
//				sort_field += ((JSONObject) sorter.get(0)).get("property").toString();
//				sort_dir += ((JSONObject) sorter.get(0)).get("direction").toString();
//				for (int i = 1; i < sorter.size(); i++) {
//					sort_field += "," + ((JSONObject) sorter.get(i)).get("property").toString();
//				}
//			}
//			catch (ParseException e) {
//				LOGGER.error(e.getMessage(), e);
//			}
//
//			sort = new HashMap<>();
//
//			if (!sort_field.equals("") && !sort_dir.equals("")) {
//				sort.put("field", sort_field);
//				sort.put("direction", sort_dir);
//			}
//
//		}
//		// sorting
//		if (request.getParameter("sort") != null) {
//			JSONArray sorter;
//			try {
//				sorter = (JSONArray) new JSONParser().parse(request.getParameter("sort"));
//				for (Object aSort: sorter) {
//					JSONObject jsonSort = (JSONObject) aSort;
//					query.addSort(SolrQuery.SortClause.create(jsonSort.get("property").toString(), jsonSort.get("direction").toString().toLowerCase()));
//				}
//			}
//			catch (ParseException e) {
//				LOGGER.error(e.getMessage(), e);
//			}
//		}

//		DBPathways conn_summary = new DBPathways();
//		List<ResultType> items = new ArrayList<>();
		SolrInterface solr = new SolrInterface();
		JSONArray items = new JSONArray();
		int count_total = 0;
		int count_unique = 0;

		response.setContentType("application/json");

		switch (need) {
		case "0":

//			count_total = conn_summary.getCompPathwayPathwayCount(key);
//			if (count_total > 0) {
//				items = conn_summary.getCompPathwayPathwayList(key, sort, start, end);
//			}
			try {
				Set<String> listPathwayIds = new HashSet<>();
				Map<String, JSONObject> uniquePathways = new HashMap<>();

				// get pathway stat
				query.setRows(0).setFacet(true).setFacetMinCount(1).setFacetLimit(-1);
				query.addFacetField("pathway_id");
				query.add("facet.stat", "genome_count:unique(genome_id)");
				query.add("facet.stat", "gene_count:unique(feature_id)");
				query.add("facet.stat", "ec_count:unique(ec_number)");
				query.add("facet.stat", "genome_ec:unique(genome_ec)");

				QueryResponse qr = solr.getSolrServer(SolrCore.PATHWAY).query(query);

//				SimpleOrderedMap facets = (SimpleOrderedMap) qr.getResponse().get("facets");
//				SimpleOrderedMap pathway_id = (SimpleOrderedMap) facets.get("pathway_id");
//				List<Map<String, Object>> buckets = (List) pathway_id.get("buckets");
				List<SimpleOrderedMap> buckets = (List) ((SimpleOrderedMap) ((SimpleOrderedMap) qr.getResponse().get("facets")).get("pathway_id")).get("buckets");

				Map<String, SimpleOrderedMap> mapStat = new HashMap<>();
				for (SimpleOrderedMap value: buckets) {
					mapStat.put(value.get("val").toString(), value);
					listPathwayIds.add(value.get("val").toString());
				}

				// get pathway list
				SolrQuery pathwayQuery = new SolrQuery("pathway_id:(" + StringUtils.join(listPathwayIds, " OR ") + ")");
				pathwayQuery.setFields("pathway_id,pathway_name,pathway_class");
				pathwayQuery.setRows(10000);

				QueryResponse pathwayQueryResponse = solr.getSolrServer(SolrCore.PATHWAY_REF).query(pathwayQuery);
				SolrDocumentList sdl = pathwayQueryResponse.getResults();

				for (SolrDocument doc: sdl) {
					String pathwayId = doc.get("pathway_id").toString();
					SimpleOrderedMap stat = mapStat.get(pathwayId);

					if (!uniquePathways.containsKey(pathwayId) && !stat.get("genome_count").toString().equals("0")) {
						JSONObject item = new JSONObject();
						item.put("pathway_id", pathwayId);
						item.put("pathway_name", doc.get("pathway_name"));
						item.put("pathway_class", doc.get("pathway_class"));

						float genome_ec = Float.parseFloat(stat.get("genome_ec").toString());
						float genome_count = Float.parseFloat(stat.get("genome_count").toString());
						float ec_count = Float.parseFloat(stat.get("ec_count").toString());
						float gene_count = Float.parseFloat(stat.get("gene_count").toString());

						float ec_cons = 0; float gene_cons = 0;
						if (genome_count > 0 && ec_count > 0) {
							ec_cons = genome_ec / genome_count / ec_count * 100;
							gene_cons =  gene_count / genome_count / ec_count;
						}

						item.put("ec_cons", ec_cons);
						item.put("ec_count", ec_count);
						item.put("gene_cons", gene_cons);
						item.put("gene_count", gene_count);
						item.put("genome_count", genome_count);
						item.put("algorithm", algorithm);

						uniquePathways.put(pathwayId, item);
					}
				}

				for (Map.Entry<String, JSONObject> pathway : uniquePathways.entrySet()) {
					items.add(pathway.getValue());
				}
				count_total = uniquePathways.entrySet().size();
				count_unique = count_total;

			} catch (MalformedURLException | SolrServerException e) {
				LOGGER.error(e.getMessage(), e);
			}

			break;
		case "1":

//			count_total = conn_summary.getCompPathwayECCount(key);
//			if (count_total > 0) {
//				items = conn_summary.getCompPathwayECList(key, sort, start, end);
//			}
			// EC Numbers tab
			try {
				Set<String> listPathwayIds = new HashSet<>();
				Set<String> listEcNumbers = new HashSet<>();

				// get pathway stat
				query.setRows(0).setFacet(true).setFacetMinCount(1).setFacetLimit(-1);
				query.addFacetField("pathway_ec");
				query.add("facet.stat", "genome_count:unique(genome_id)");
				query.add("facet.stat", "gene_count:unique(feature_id)");
				query.add("facet.stat", "ec_count:unique(ec_number)");

				// LOGGER.debug("Solr [PATHWAY]: {}", query.toString());
				QueryResponse qr = solr.getSolrServer(SolrCore.PATHWAY).query(query);

				List<SimpleOrderedMap> buckets = (List) ((SimpleOrderedMap) ((SimpleOrderedMap) qr.getResponse().get("facets")).get("pathway_ec")).get("buckets");

				Map<String, SimpleOrderedMap> mapStat = new HashMap<>();
				for (SimpleOrderedMap value: buckets) {

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
				}
				pathwayQuery.setFields("pathway_id,pathway_name,pathway_class,ec_number,ec_description");
				pathwayQuery.setRows(10000);
				// LOGGER.debug("{}", pathwayQuery.toString());
				QueryResponse pathwayQueryResponse = solr.getSolrServer(SolrCore.PATHWAY_REF).query(pathwayQuery, SolrRequest.METHOD.POST);
				SolrDocumentList sdl = pathwayQueryResponse.getResults();

				for (SolrDocument doc: sdl) {
					String pathwayId = doc.get("pathway_id").toString();
					String ecNumber = doc.get("ec_number").toString();
					SimpleOrderedMap stat = mapStat.get(pathwayId + "_" + ecNumber);

					if (stat != null && !stat.get("genome_count").toString().equals("0")) {
						JSONObject item = new JSONObject();
						item.put("pathway_id", pathwayId);
						item.put("pathway_name", doc.get("pathway_name"));
						item.put("pathway_class", doc.get("pathway_class"));

						float genome_count = Float.parseFloat(stat.get("genome_count").toString());
						float gene_count = Float.parseFloat(stat.get("gene_count").toString());

						item.put("ec_name", doc.get("ec_description"));
						item.put("ec_number", doc.get("ec_number"));
						item.put("gene_count", gene_count);
						item.put("genome_count", genome_count);
						item.put("algorithm", algorithm);

						items.add(item);
					}
				}
				count_total = items.size();
				count_unique = listEcNumbers.size();

			} catch (MalformedURLException | SolrServerException e) {
				LOGGER.error(e.getMessage(), e);
			}

			break;
		case "2":

//			count_total = conn_summary.getCompPathwayFeatureCount(key);
//			if (count_total > 0) {
//				items = conn_summary.getCompPathwayFeatureList(key, sort, start, end);
//			}
			// Gene tab
			try {
				Set<String> listFeatureIds = new HashSet<>();

				query.setFields("pathway_id,pathway_name,feature_id,ec_number,ec_description");
				query.setRows(100000);
				QueryResponse qr = solr.getSolrServer(SolrCore.PATHWAY).query(query);
				SolrDocumentList sdl = qr.getResults();

				Map<String, SolrDocument> mapStat = new HashMap<>();
				for (SolrDocument doc: sdl) {

					mapStat.put(doc.get("feature_id").toString(), doc);
					listFeatureIds.add(doc.get("feature_id").toString());
				}

				// get pathway list
				SolrQuery featureQuery = new SolrQuery("feature_id:(" + StringUtils.join(listFeatureIds, " OR ") + ")");
				featureQuery.setFields("genome_name,genome_id,accession,alt_locus_tag,refseq_locus_tag,seed_id,feature_id,gene,product");
				featureQuery.setRows(100000);
				// LOGGER.debug("{}", featureQuery.toString());
				QueryResponse featureQueryResponse = solr.getSolrServer(SolrCore.FEATURE).query(featureQuery, SolrRequest.METHOD.POST);
				sdl = featureQueryResponse.getResults();

				for (SolrDocument doc: sdl) {
					String featureId = doc.get("feature_id").toString();
					SolrDocument stat = mapStat.get(featureId);

					JSONObject item = new JSONObject();
					item.put("genome_name", doc.get("genome_name"));
					item.put("genome_id", doc.get("genome_id"));
					item.put("accession", doc.get("accession"));
					item.put("feature_id", doc.get("feature_id"));
					item.put("al_locus_tag", doc.get("alt_locus_tag"));
					item.put("refseq_locus_tag", doc.get("refseq_locus_tag"));
					item.put("algorithm", algorithm);
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

			} catch (MalformedURLException | SolrServerException e) {
				LOGGER.error(e.getMessage(), e);
			}

			break;
		}

		try {
			jsonResult.put("total", count_total);
			jsonResult.put("results", items);
			jsonResult.put("unique", count_unique);
		}
		catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
		}

		PrintWriter writer = response.getWriter();
		jsonResult.writeJSONString(writer);
		writer.close();
	}

	private void getFilterData (ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
		JSONObject val = null;
		try {
			val = (JSONObject) (new JSONParser()).parse(request.getParameter("val"));
		}
		catch (ParseException e) {
			LOGGER.error(e.getMessage(), e);
		}

		JSONObject json = new JSONObject();

		String algorithm = val.get("alg") != null ? val.get("alg").toString() : "";
		String pid = val.get("pId") != null ? val.get("pId").toString() : "";
		String pathway_class = val.get("pClass") != null ? val.get("pClass").toString() : "";
		String ec_number = val.get("ecN") != null ? val.get("ecN").toString() : "";
		String cType = val.get("cType") != null ? val.get("cType").toString() : "";
		String cId = val.get("cId") != null ? val.get("cId").toString() : "";
		String need = val.get("need") != null ? val.get("need").toString() : "";

//		HashMap<String, String> key = new HashMap<>();

//		if (cType.equals("genome")) {
//			key.put("genomeId", cId);
//		} else {
//			key.put("taxonId", cId);
//		}
//		key.put("algorithm",algorithm);
//		key.put("map",pid);
//		key.put("pathway_class",pathway_class);
//		key.put("ec_number",ec_number);

		// DBPathways conn_pathways = new DBPathways();
		SolrInterface solr = new SolrInterface();

		JSONObject defaultItem = new JSONObject();
		defaultItem.put("name", "ALL");
		defaultItem.put("value", "ALL");

		JSONArray items = new JSONArray();
		items.add(defaultItem);

		// common solrQuery
		SolrQuery query = new SolrQuery("annotation:" + algorithm);
		if (cType.equals("taxon")) {
			query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "genome_status:(complete OR wgs) AND taxon_lineage_ids:" + cId));
		} else {
			query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "genome_status:(complete OR wgs) AND genome_id:" + cId));
		}
		query.setRows(0).setFacet(true).setFacetMinCount(1).setFacetLimit(-1);

		if (!pathway_class.equals("")) {
			query.addFilterQuery("pathway_class:" + pathway_class);
		}
		if (!pid.equals("")) {
			query.addFilterQuery("pathway_name:" + pid);
		}
		if (!ec_number.equals("")) {
			query.addFilterQuery("ec_number:" + ec_number);
		}


		switch (need) {
		case "pathway":
			//			items = conn_pathways.getListOfPathwayNameList(key);
			try {
				query.addFacetPivotField("pathway_id,pathway_name");
				QueryResponse qr = solr.getSolrServer(SolrCore.PATHWAY).query(query);
				List<PivotField> pivotFacet = qr.getFacetPivot().get("pathway_id,pathway_name");

				for (PivotField field: pivotFacet) {
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
			//			items = conn_pathways.getListOfEc_NumberList(key);
			try {
				query.addFacetField("ec_number");

				QueryResponse qr = solr.getSolrServer(SolrCore.PATHWAY).query(query);
				FacetField facet = qr.getFacetField("ec_number");

				for (FacetField.Count item: facet.getValues()) {
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
			//			items = conn_pathways.getListOfPathwayParentList(key);
			try {
				query.addFacetField("pathway_class");

				QueryResponse qr = solr.getSolrServer(SolrCore.PATHWAY).query(query);
				FacetField facet = qr.getFacetField("pathway_class");

				for (FacetField.Count item: facet.getValues()) {
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
			//			items = conn_pathways.getListOfAlgorithmList(key);
			try {
				query.addFacetField("annotation");

				QueryResponse qr = solr.getSolrServer(SolrCore.PATHWAY).query(query);
				FacetField facet = qr.getFacetField("annotation");

				for (FacetField.Count item: facet.getValues()) {
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
		}
		response.setContentType("application/json");
		json.writeJSONString(response.getWriter());
	}
}
