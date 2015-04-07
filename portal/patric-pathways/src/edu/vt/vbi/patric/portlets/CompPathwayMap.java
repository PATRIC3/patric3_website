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

import com.google.gson.Gson;
import edu.vt.vbi.patric.common.SessionHandler;
import edu.vt.vbi.patric.common.SiteHelper;
import edu.vt.vbi.patric.common.SolrCore;
import edu.vt.vbi.patric.common.SolrInterface;
import edu.vt.vbi.patric.dao.DBPathways;
import edu.vt.vbi.patric.dao.ResultType;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
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

public class CompPathwayMap extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(CompPathwayMap.class);

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		response.setContentType("text/html");

		SiteHelper.setHtmlMetaElements(request, response, "Comparative Pathway Map");

		SolrInterface solr = new SolrInterface();

		String pk = request.getParameter("param_key") != null ? request.getParameter("param_key") : "";
		String dm = request.getParameter("display_mode") != null ? request.getParameter("display_mode") : "";
		String cType = request.getParameter("context_type") != null ? request.getParameter("context_type") : "";
		String map = request.getParameter("map") != null ? request.getParameter("map") : "";
		String algorithm = request.getParameter("algorithm") != null ? request.getParameter("algorithm") : "";
		String cId = request.getParameter("context_id") != null ? request.getParameter("context_id") : "";
		String ec_number = request.getParameter("ec_number") != null ? request.getParameter("ec_number") : "";
		String feature_id = request.getParameter("feature_id") != null ? request.getParameter("feature_id") : "";
		String ec_names = "";
		String occurrences = "";
		String genomeId = "";
		String taxonId = "";
		String pathway_name = "";
		String pathway_class = "";
		String definition = "";

		int patricGenomeCount = 0;
		int brc1GenomeCount = 0;
		int refseqGenomeCount = 0;

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

		try {
			if (cType != null && cType.equals("taxon")) {
				taxonId = cId;
			}
			else if (cType != null && cType.equals("genome")) {
				genomeId = cId;
			}
			else {
				Gson gson = new Gson();
				Map<String, String> key = gson.fromJson(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk), Map.class);

				if (key != null && key.containsKey("genomeId") && !key.get("genomeId").equals("")) {
					genomeId = key.get("genomeId");
				}
				else if (key != null && key.containsKey("taxonId") && !key.get("taxonId").equals("")) {
					taxonId = key.get("taxonId");
				}
			}

			SolrQuery query = new SolrQuery("*:*");
			query.setRows(1000000);
			if (!genomeId.equals("")) {
				if (genomeId.contains(",")) {
					query.addFilterQuery("genome_id:(" + StringUtils.join(genomeId.split(","), " OR ") + ")");
					query.setRows(genomeId.split(",").length);
				}
				else {
					query.addFilterQuery("genome_id:" + genomeId);
				}
			}
			if (!taxonId.equals("")) {
				query.addFilterQuery("taxon_lineage_ids:" + taxonId);
			}

			QueryResponse qr = solr.getSolrServer(SolrCore.GENOME).query(query);
			SolrDocumentList sdl = qr.getResults();

			for (SolrDocument doc : sdl) {
				if (doc.get("patric_cds") != null && !doc.get("patric_cds").toString().equals("0")) {
					patricGenomeCount++;
				}
				if (doc.get("brc1_cds") != null && !doc.get("brc1_cds").toString().equals("0")) {
					brc1GenomeCount++;
				}
				if (doc.get("refseq_cds") != null && !doc.get("refseq_cds").toString().equals("0")) {
					refseqGenomeCount++;
				}
			}

		}
		catch (MalformedURLException | SolrServerException e) {
			LOGGER.error(e.getMessage(), e);
		}

		// TODO: implement with solr
		if (dm != null && dm.equals("ec")) {

			List<ResultType> ecAssignments = (new DBPathways()).EC2ECProperties(ec_number, map);
			for (ResultType item : ecAssignments) {
				ec_names = item.get("description");
				occurrences = item.get("occurrence");
			}
		}
		else if (dm != null && dm.equals("feature")) {

			List<ResultType> ecAssignments = (new DBPathways()).aaSequence2ECAssignments(feature_id, map);
			for (ResultType item : ecAssignments) {
				ec_number = item.get("ec_number");
				ec_names = item.get("description");
				occurrences = item.get("occurrence");
			}
		}

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

		request.setAttribute("taxongenomecount_patric", patricGenomeCount);
		request.setAttribute("taxongenomecount_brc1", brc1GenomeCount);
		request.setAttribute("taxongenomecount_refseq", refseqGenomeCount);

		request.setAttribute("definition", definition);
		request.setAttribute("pathway_name", pathway_name);
		request.setAttribute("pathway_class", pathway_class);

		PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/comp_pathway_map.jsp");
		prd.include(request, response);
	}

	@SuppressWarnings("unchecked")
	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		response.setContentType("application/json");

		String genomeId = request.getParameter("genomeId");
		String taxonId = request.getParameter("taxonId");
		String cType = request.getParameter("cType");
		String map = request.getParameter("map");
		String algorithm = request.getParameter("algorithm");

		int count_total = 0;
		JSONArray results = new JSONArray();
		try {
			Set<String> ecNumbers = new HashSet<>();

			SolrInterface solr = new SolrInterface();
			SolrQuery query = new SolrQuery("pathway_id:" + map + " AND annotation:" + algorithm);
			if (taxonId != null && !taxonId.equals("")) {
				query.addFilterQuery(SolrCore.GENOME
						.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + taxonId + " AND genome_status:(complete OR wgs)"));
			}
			if (genomeId != null && !genomeId.equals("")) {
				query.addFilterQuery(
						SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id",
								"genome_id:(" + genomeId.replaceAll(",", " OR ") + ") AND genome_status:(complete OR wgs)"));
			}
			query.setRows(0).setFacet(true);
			query.add("json.facet",
					"{stat:{field:{field:ec_number,limit:-1,facet:{genome_count:\"unique(genome_id)\",gene_count:\"unique(feature_id)\"}}}}");

			QueryResponse qr = solr.getSolrServer(SolrCore.PATHWAY).query(query);
			List<SimpleOrderedMap> buckets = (List) ((SimpleOrderedMap) ((SimpleOrderedMap) qr.getResponse().get("facets")).get("stat"))
					.get("buckets");

			Map<String, SimpleOrderedMap> mapStat = new HashMap<>();
			for (SimpleOrderedMap value : buckets) {
				if (Integer.parseInt(value.get("gene_count").toString()) > 0) {
					mapStat.put(value.get("val").toString(), value);
					ecNumbers.add(value.get("val").toString());
				}
			}

			if (!ecNumbers.isEmpty()) {
				query = new SolrQuery("pathway_id:" + map + " AND ec_number:(" + StringUtils.join(ecNumbers, " OR ") + ")");
				query.setRows(ecNumbers.size()).setFields("ec_number,ec_description,occurrence");
				query.addSort("ec_number", SolrQuery.ORDER.asc);

				qr = solr.getSolrServer(SolrCore.PATHWAY_REF).query(query);
				SolrDocumentList sdl = qr.getResults();
				count_total = (int) sdl.getNumFound();

				for (SolrDocument doc : sdl) {
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
		}
		catch (MalformedURLException | SolrServerException e) {
			LOGGER.error(e.getMessage(), e);
		}

		JSONObject jsonResult = new JSONObject();
		try {
			jsonResult.put("total", count_total);
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