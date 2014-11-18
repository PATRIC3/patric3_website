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

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.*;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import edu.vt.vbi.patric.common.SolrCore;
import edu.vt.vbi.patric.common.SolrInterface;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import edu.vt.vbi.patric.common.SiteHelper;
import edu.vt.vbi.patric.dao.DBPathways;
import edu.vt.vbi.patric.dao.ResultType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompPathwayMap extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(CompPathwayMap.class);

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		response.setContentType("text/html");

		new SiteHelper().setHtmlMetaElements(request, response, "Comparative Pathway Map");

		//////
		SolrInterface solr = new SolrInterface();

		ResultType item;
		ArrayList<ResultType> ecAssignments;
//		ArrayList<ResultType> taxongenomecounts = null;

		String pk = request.getParameter("param_key") != null || request.getParameter("param_key") != ""?request.getParameter("param_key"):"",
				dm = request.getParameter("display_mode") != null || request.getParameter("display_mode") != ""?request.getParameter("display_mode"):"",
				cType = request.getParameter("context_type") != null || request.getParameter("context_type") != ""?request.getParameter("context_type"):"",
				map = request.getParameter("map") != null || request.getParameter("map") != ""?request.getParameter("map"):"",
				algorithm = request.getParameter("algorithm") != null || request.getParameter("algorithm") != ""?request.getParameter("algorithm"):"",
				cId = request.getParameter("context_id") != null || request.getParameter("context_id") != ""?request.getParameter("context_id"):"",
				ec_number = request.getParameter("ec_number") != null || request.getParameter("ec_number") != ""?request.getParameter("ec_number"):"",
				feature_id = request.getParameter("feature_info_id") != null || request.getParameter("feature_info_id") != ""?request.getParameter("feature_info_id"):"",
				ec_names = "",
				occurrences = "",
				genomeId = "",
				taxonId = "",
				pathway_name = "",
				pathway_class = "",
				definition = "";
//				attributes = conn_pathways.getPathwayAttributes(map);
		int taxongenomecount_patric = 0;
		int taxongenomecount_brc1 = 0;
		int taxongenomecount_refseq = 0;

		// get attributes
		try {
			SolrQuery query = new SolrQuery("pathway_id:" + map);
			query.setFields("pathway_name,pathway_class,ec_description");

			QueryResponse qr = solr.getSolrServer(SolrCore.PATHWAY_REF).query(query);
			SolrDocumentList sdl = qr.getResults();
			if (!sdl.isEmpty()) {
				SolrDocument doc = sdl.get(0);

				definition = doc.get("ec_description").toString();
				pathway_name = doc.get("pathway_name").toString();
				pathway_class = doc.get("pathway_class").toString();
			}
		}
		catch (MalformedURLException | SolrServerException e) {
			LOGGER.error(e.getMessage(), e);
		}

//		if(algorithm != null && !algorithm.equals("")){
//			if(algorithm.equals("BRC") || algorithm.equals("Legacy BRC") || algorithm.equals("Legacy"))
//				algorithm = "Curation";
//			else if(algorithm.equals("PATRIC"))
//				algorithm = "RAST";
//		}

//		if (cType == null || cType.equals("")) {
//			PortletSession session = request.getPortletSession(true);
//			ResultType key = (ResultType) session.getAttribute("key"+pk, PortletSession.APPLICATION_SCOPE);
//
//			if (key != null && key.containsKey("genomeId") && !key.get("genomeId").equals("")){
//				taxonId = "";
//				genomeId = key.get("genomeId");
//				taxongenomecounts = new DBPathways().getTaxonGenomeCount(genomeId, "genomelist");
//			}
//			else {
//				if(key != null && key.containsKey("taxonId") && !key.get("taxonId").equals("")){
//					taxonId = key.get("taxonId");
//				}
//				else {
//					taxonId = "2";
//				}
//				taxongenomecounts = new DBPathways().getTaxonGenomeCount(taxonId, "taxon");
//				genomeId = "";
//			}
//
//			if(key != null && key.containsKey("feature_info_id")){
//				feature_id = key.get("feature_info_id");
//			}
//
//		}
//		else if(cType.equals("taxon")){
//			genomeId = "";
//			if (cId == null || cId == "") {
//				taxonId = "2";
//			} else {
//				taxonId = cId;
//			}
//			taxongenomecounts = new DBPathways().getTaxonGenomeCount(taxonId, "taxon");
//		}
//		else if(cType.equals("genome")){
//			taxonId = "";
//			genomeId = cId;
//			taxongenomecounts = new DBPathways().getTaxonGenomeCount(genomeId, "genome");
//		}

		try {
			if (cType != null && cType.equals("taxon")) {
				taxonId = cId;
			}
			else if (cType != null && cType.equals("genome")) {
				genomeId = cId;
			}
			else {
				PortletSession session = request.getPortletSession(true);
				ResultType key = (ResultType) session.getAttribute("key"+pk, PortletSession.APPLICATION_SCOPE);

				if (key != null && key.containsKey("genomeId") && !key.get("genomeId").equals("")) {
					genomeId = key.get("genomeId");
				}
				else if (key != null && key.containsKey("taxonId") && !key.get("taxonId").equals("")) {
					taxonId = key.get("taxonId");
				}
			}

			SolrQuery query = new SolrQuery("*:*");
			if (!genomeId.equals("")) {
				if (genomeId.contains(",")) {
					query.addFilterQuery("genome_id:(" + StringUtils.join( genomeId.split(","), " OR") + ")");
				} else {
					query.addFilterQuery("genome_id:" + genomeId);
				}
			}
			if (!taxonId.equals("")) {
				query.addFilterQuery("taxon_lineage_ids:" + taxonId);
			}
			query.setRows(10000);

			QueryResponse qr = solr.getSolrServer(SolrCore.GENOME).query(query);
			SolrDocumentList sdl = qr.getResults();

			for (SolrDocument doc: sdl) {
				if (!doc.get("patric_cds").toString().equals("0")) {
					taxongenomecount_patric++;
				}
				if (!doc.get("brc1_cds").toString().equals("0")) {
					taxongenomecount_brc1++;
				}
				if (!doc.get("refseq_cds").toString().equals("0")) {
					taxongenomecount_refseq++;
				}
			}

		}
		catch (MalformedURLException | SolrServerException e) {
			LOGGER.error(e.getMessage(), e);
		}

		if (dm != null && dm.equals("ec")){
			ecAssignments = new DBPathways().EC2ECProperties(ec_number, map);
			for ( Iterator<ResultType> iter = ecAssignments.iterator(); iter.hasNext(); ) {
				item = iter.next();
				ec_names = item.get("description");
				occurrences = item.get("occurrence");
			}
		}
		else if(dm != null && dm.equals("feature")){
			ecAssignments = new DBPathways().aaSequence2ECAssignments(feature_id, map);
			for ( Iterator<ResultType> iter = ecAssignments.iterator(); iter.hasNext(); ) {
				item = iter.next();
				ec_number = item.get("ec_number");
				ec_names = item.get("description");
				occurrences = item.get("occurrence") ;
			}
		}

//		for(int i =0; i < taxongenomecounts.size(); i++){
//			ResultType g = taxongenomecounts.get(i);
//			if(g.get("algorithm").equals("RAST")){
//				taxongenomecount_patric = g.get("count");
//			}else if(g.get("algorithm").equals("Curation")){
//				taxongenomecount_brc = g.get("count");
//			}else if(g.get("algorithm").equals("RefSeq")){
//				taxongenomecount_refseq = g.get("count");
//			}
//		}

		/////
		request.setAttribute("cType", cType);
		request.setAttribute("cId", cId);
		request.setAttribute("taxonId", taxonId);
		request.setAttribute("genomeId", genomeId);
		request.setAttribute("algorithm", algorithm);
		request.setAttribute("map", map);
		request.setAttribute("dm", dm);
		request.setAttribute("pk", pk);
		request.setAttribute("feature_id", feature_id);

		request.setAttribute("ec_number", ec_number);
		request.setAttribute("ec_names", ec_names);
		request.setAttribute("occurrences", occurrences);

		request.setAttribute("taxongenomecount_patric", taxongenomecount_patric);
		request.setAttribute("taxongenomecount_brc1", taxongenomecount_brc1);
		request.setAttribute("taxongenomecount_refseq", taxongenomecount_refseq);

		request.setAttribute("definition", definition);
		request.setAttribute("pathway_name", pathway_name);
		request.setAttribute("pathway_class", pathway_class);

		PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/comp_pathway_map.jsp");
		prd.include(request, response);
	}

	@SuppressWarnings("unchecked")
	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		response.setContentType("application/json");

//		String sort_field = request.getParameter("sort");
//		String sort_dir = request.getParameter("dir");
//
//		HashMap<String, String> sort = null;
//
//		if (sort_field != null && sort_dir != null) {
//			sort = new HashMap<>();
//			sort.put("field", sort_field);
//			sort.put("direction", sort_dir);
//		}

		String genomeId = request.getParameter("genomeId");
		String taxonId = request.getParameter("taxonId");
		String cType = request.getParameter("cType");
		String map = request.getParameter("map");
		String algorithm = request.getParameter("algorithm");

//		Map<String, String> key = null;
//
//		if (cType.equals("taxon") && taxonId != null && !taxonId.equals("")) {
//			key = new HashMap<>();
//
//			key.put("taxonId", taxonId);
//			key.put("map", map);
//			key.put("algorithm", algorithm);
//		}
//		else if (cType.equals("genome") && genomeId != null && !genomeId.equals("")) {
//
//			key = new HashMap<>();
//
//			key.put("genomeId", genomeId);
//			key.put("map", map);
//			key.put("algorithm", algorithm);
//		}
//		else {
//
//			String pk = request.getParameter("pk");
//			PortletSession sess = request.getPortletSession();
//
//			ResultType keytemp = (ResultType) sess.getAttribute("key" + pk, PortletSession.APPLICATION_SCOPE);
//			if (keytemp != null) {
//				key = (HashMap<String, String>) keytemp.clone();
//				key.put("map", map);
//				key.put("algorithm", algorithm);
//			}
//		}

		int count_total = 0;

//		DBPathways conn_pathways = new DBPathways();
//		List<ResultType> items = conn_pathways.getCompPathwayMapGridList(key, sort, 0, -1);
//		count_total = conn_pathways.getCompPathwayMapGridCount(key);
		JSONArray results = new JSONArray();
		try {
			Set<String> ecNumbers = new HashSet<>();

			SolrInterface solr = new SolrInterface();
			SolrQuery query = new SolrQuery("pathway_id:" + map + " AND annotation:" + algorithm);
			if (taxonId != null && !taxonId.equals("")) {
				query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + taxonId + " AND genome_status:(complete OR wgs)"));
			}
			if (genomeId != null && !genomeId.equals("")) {
				query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "genome_id:" + genomeId + " AND genome_status:(complete OR wgs)"));
			}
			query.setRows(0).setFacet(true);
			query.add("json.facet","{stat:{field:{field:ec_number,limit:-1,facet:{genome_count:\"unique(genome_id)\",gene_count:\"unique(feature_id)\"}}}}");

			QueryResponse qr = solr.getSolrServer(SolrCore.PATHWAY).query(query);
			List<SimpleOrderedMap> buckets = (List) ((SimpleOrderedMap) ((SimpleOrderedMap) qr.getResponse().get("facets")).get("stat")).get("buckets");

			Map<String, SimpleOrderedMap> mapStat = new HashMap<>();
			for (SimpleOrderedMap value: buckets) {
				if (Integer.parseInt(value.get("gene_count").toString()) > 0) {
					mapStat.put(value.get("val").toString(), value);
					ecNumbers.add(value.get("val").toString());
				}
			}

			query = new SolrQuery("pathway_id:" + map + " AND ec_number:(" + StringUtils.join(ecNumbers, " OR ") + ")");
			query.setRows(ecNumbers.size()).setFields("ec_number,ec_description,occurrence");
			query.addSort("ec_number", SolrQuery.ORDER.asc);

			qr = solr.getSolrServer(SolrCore.PATHWAY_REF).query(query);
			SolrDocumentList sdl = qr.getResults();
			count_total = (int) sdl.getNumFound();

			for (SolrDocument doc: sdl) {
				String ecNumber = doc.get("ec_number").toString();
				SimpleOrderedMap stat = mapStat.get(ecNumber);

				JSONObject item = new JSONObject();
				item.put("algorithm", algorithm);
				item.put("ec_name", doc.get("ec_description"));
				item.put("ec_number", ecNumber);
				item.put("occurrence", doc.get("occurrence"));
				item.put("gene_count", stat.get("gene_count"));
				item.put("genome_count", stat.get("genome_count"));

				results.add(item);
			}

		}
		catch (MalformedURLException | SolrServerException e) {
			LOGGER.error(e.getMessage(), e);
		}
		JSONObject jsonResult = new JSONObject();

//		if (algorithm.equals("RAST") || algorithm.equals("PATRIC"))
//			algorithm = "RAST";
//		else if (algorithm.equals("Curation") || algorithm.equals("Legacy BRC"))
//			algorithm = "Curation";

		try {
			jsonResult.put("total", count_total);
//			JSONArray results = new JSONArray();

//			for (ResultType item : items) {
//				JSONObject obj = new JSONObject();
//				obj.putAll(item);
//				results.add(obj);
//			}

			jsonResult.put("results", results);
		}
		catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
		}

		PrintWriter writer = response.getWriter();
		jsonResult.writeJSONString(writer);
		writer.close();
	}
}