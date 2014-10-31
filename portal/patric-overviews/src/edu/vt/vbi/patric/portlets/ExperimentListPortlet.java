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

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.vt.vbi.patric.common.SiteHelper;
import edu.vt.vbi.patric.common.SolrCore;
import edu.vt.vbi.patric.common.SolrInterface;
import edu.vt.vbi.patric.dao.ResultType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExperimentListPortlet extends GenericPortlet {

	SolrInterface solr = new SolrInterface();

	private static final Logger LOGGER = LoggerFactory.getLogger(ExperimentListPortlet.class);

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		response.setContentType("text/html");

		new SiteHelper().setHtmlMetaElements(request, response, "Experiment List");
		response.setTitle("Experiment List");

		String cType = request.getParameter("context_type");
		String cId = request.getParameter("context_id");

		if (cType != null && cId != null) {
			String kw = (request.getParameter("keyword") != null) ? request.getParameter("keyword") : "";
			if (kw != null && (kw.startsWith("/") || kw.startsWith("#"))) {
				kw = "";
			}

			// DBTranscriptomics conn_transcriptopics = new DBTranscriptomics();
			SolrInterface solr = new SolrInterface();

			String keyword = "(*)";
			String filter = "";
			String eid = "NA";

			if (cType.equals("taxon") && cId.equals("2")) {
				filter = "*";
				eid = "";
			}
			else {
				List<String> items = new ArrayList<>();
				if (cType.equals("taxon")) {

					try {
						SolrQuery query = new SolrQuery("*:*");
						query.setRows(10000);
						query.setFields("eid");
						query.setFilterQueries(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_ids", "taxon_lineage_ids:" + cId));

						QueryResponse qr = solr.getSolrServer(SolrCore.TRANSCRIPTOMICS_EXPERIMENT).query(query);
						SolrDocumentList sdl = qr.getResults();

						for (SolrDocument doc: sdl) {
							items.add(doc.get("eid").toString());
						}

					} catch (MalformedURLException | SolrServerException e) {
						LOGGER.error(e.getMessage(), e);
					}
				}
				else if (cType.equals("genome")) {

					try {
						SolrQuery query = new SolrQuery("genome_ids:" + cId);
						query.setRows(10000);
						query.setFields("eid");

						QueryResponse qr = solr.getSolrServer(SolrCore.TRANSCRIPTOMICS_EXPERIMENT).query(query);
						SolrDocumentList sdl = qr.getResults();

						for (SolrDocument doc: sdl) {
							items.add(doc.get("eid").toString());
						}
					} catch (MalformedURLException | SolrServerException e) {
						LOGGER.error(e.getMessage(), e);
					}
				}

				if (items.size() > 0) {
					eid = StringUtils.join(items, "##");
				}
				else {
					eid = "0";
				}
				filter = eid;
			}

			request.setAttribute("keyword", keyword);
			request.setAttribute("kw", kw);
			request.setAttribute("eid", eid);
			request.setAttribute("filter", filter);

			PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/experiment/list.jsp");
			prd.include(request, response);
		}
	}

	@SuppressWarnings("unchecked")
	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		String sraction = request.getParameter("sraction");

		if (sraction != null && sraction.equals("save_params")) {

			ResultType key = new ResultType();

			String genomeId = request.getParameter("genomeId");
			String taxonId = "";
			String cType = request.getParameter("context_type");
			String cId = request.getParameter("context_id");
			if (cType != null && cId != null && cType.equals("taxon") && !cId.equals("")) {
				taxonId = cId;
			}
			String keyword = request.getParameter("keyword");
			String state = request.getParameter("state");
			String ncbi_taxon_id = request.getParameter("ncbi_taxon_id");

			if (genomeId != null && !genomeId.equalsIgnoreCase("")) {
				key.put("genomeId", genomeId);
			}
			if (!taxonId.equalsIgnoreCase("")) {
				key.put("taxonId", taxonId);
			}
			if (keyword != null) {
				key.put("keyword", keyword.trim());
			}
			if (ncbi_taxon_id != null) {
				key.put("ncbi_taxon_id", ncbi_taxon_id);
			}
			if (state != null) {
				key.put("state", state);
			}
			// random
			Random g = new Random();
			int random = g.nextInt();

			PortletSession sess = request.getPortletSession(true);
			sess.setAttribute("key" + random, key, PortletSession.APPLICATION_SCOPE);

			PrintWriter writer = response.getWriter();
			writer.write("" + random);
			writer.close();

		}
		else {

			String need = request.getParameter("need");
			String facet, keyword, pk, state, eId;
			boolean hl;
			PortletSession sess = request.getPortletSession();
			ResultType key = new ResultType();
			JSONObject jsonResult = new JSONObject();

			if (need.equals("1")) {

				pk = request.getParameter("pk");

				keyword = request.getParameter("keyword");
				eId = request.getParameter("eId");
				facet = request.getParameter("facet");

				if (sess.getAttribute("key" + pk, PortletSession.APPLICATION_SCOPE) == null) {
					key.put("facet", facet);
					key.put("keyword", keyword);
					sess.setAttribute("key" + pk, key, PortletSession.APPLICATION_SCOPE);
				}
				else {
					key = (ResultType) sess.getAttribute("key" + pk, PortletSession.APPLICATION_SCOPE);
					key.put("facet", facet);
				}

				String orig_keyword = key.get("keyword");

				if (eId != null && !eId.equals("")) {
					key.put("keyword", solr.ConstructKeyword("eid", eId));

				}
				else if (eId != null && eId.equals("")) {

					String solrId = "";
					solr.setCurrentInstance(SolrCore.TRANSCRIPTOMICS_EXPERIMENT);
					JSONObject object = solr.getData(key, null, facet, 0, 10000, true, false, false);

					JSONObject obj = (JSONObject) object.get("response");
					JSONArray obj1 = (JSONArray) obj.get("docs");

					for (Object ob : obj1) {
						JSONObject doc = (JSONObject) ob;
						if (solrId.length() == 0) {
							solrId += doc.get("eid").toString();
						}
						else {
							solrId += "," + doc.get("eid").toString();
						}
					}

					key.put("keyword", solr.ConstructKeyword("eid", solrId));

					JSONObject facets = (JSONObject) object.get("facets");
					if (facets != null) {
						key.put("facets", facets.toString());
					}
				}

				String start_id = request.getParameter("start");
				String limit = request.getParameter("limit");
				int start = Integer.parseInt(start_id);
				int end = Integer.parseInt(limit);

				// sorting
				JSONParser a = new JSONParser();
				JSONArray sorter;
				String sort_field = "";
				String sort_dir = "";
				try {
					sorter = (JSONArray) a.parse(request.getParameter("sort"));
					sort_field += ((JSONObject) sorter.get(0)).get("property").toString();
					sort_dir += ((JSONObject) sorter.get(0)).get("direction").toString();
					for (int i = 1; i < sorter.size(); i++) {
						sort_field += "," + ((JSONObject) sorter.get(i)).get("property").toString();
					}
				}
				catch (ParseException e) {
					LOGGER.error(e.getMessage(), e);
				}

				Map<String, String> sort = new HashMap<>();

				if (!sort_field.equals("") && !sort_dir.equals("")) {
					sort.put("field", sort_field);
					sort.put("direction", sort_dir);
				}
				solr.setCurrentInstance(SolrCore.TRANSCRIPTOMICS_COMPARISON);
				key.put("fields", "eid,expid,accession,pid,samples,expname,release_date,pmid,organism,strain,mutant,timepoint,condition,genes,sig_log_ratio,sig_z_score");

				JSONObject object = solr.getData(key, sort, facet, start, end, false, false, false);

				JSONObject obj = (JSONObject) object.get("response");
				JSONArray obj1 = (JSONArray) obj.get("docs");

				if (!key.containsKey("facets")) {
					JSONObject facets = (JSONObject) object.get("facets");
					key.put("facets", facets.toString());
				}

				key.put("keyword", orig_keyword);

				jsonResult.put("results", obj1);
				jsonResult.put("total", obj.get("numFound"));

				response.setContentType("application/json");
				PrintWriter writer = response.getWriter();
				writer.write(jsonResult.toString());
				writer.close();

			}
			else if (need.equals("0")) {

				solr.setCurrentInstance(SolrCore.TRANSCRIPTOMICS_EXPERIMENT);

				pk = request.getParameter("pk");
				keyword = request.getParameter("keyword");
				facet = request.getParameter("facet");
				String highlight = request.getParameter("highlight");

				hl = Boolean.parseBoolean(highlight);

				if (sess.getAttribute("key" + pk, PortletSession.APPLICATION_SCOPE) == null) {
					key.put("facet", facet);
					key.put("keyword", keyword);

					sess.setAttribute("key" + pk, key, PortletSession.APPLICATION_SCOPE);
				}
				else {
					key = (ResultType) sess.getAttribute("key" + pk, PortletSession.APPLICATION_SCOPE);
					key.put("facet", facet);
				}

				String start_id = request.getParameter("start");
				String limit = request.getParameter("limit");
				int start = Integer.parseInt(start_id);
				int end = Integer.parseInt(limit);

				Map<String, String> sort = null;
				if (request.getParameter("sort") != null) {
					// sorting
					JSONParser a = new JSONParser();
					JSONArray sorter;
					String sort_field = "";
					String sort_dir = "";
					try {
						sorter = (JSONArray) a.parse(request.getParameter("sort"));
						sort_field += ((JSONObject) sorter.get(0)).get("property").toString();
						sort_dir += ((JSONObject) sorter.get(0)).get("direction").toString();
						for (int i = 1; i < sorter.size(); i++) {
							sort_field += "," + ((JSONObject) sorter.get(i)).get("property").toString();
						}
					}
					catch (ParseException e) {
						LOGGER.error(e.getMessage(), e);
					}

					sort = new HashMap<>();

					if (!sort_field.equals("") && !sort_dir.equals("")) {
						sort.put("field", sort_field);
						sort.put("direction", sort_dir);
					}
				}
				key.put("fields", "eid,expid,accession,institution,pi,author,pmid,release_date,title,organism,strain,mutant,timeseries,condition,samples,platform,genes");
				JSONObject object = solr.getData(key, sort, facet, start, end, true, hl, false);

				JSONObject obj = (JSONObject) object.get("response");
				JSONArray obj1 = (JSONArray) obj.get("docs");

				if (!key.containsKey("facets")) {
					if (object.containsKey("facets")) {
						JSONObject facets = (JSONObject) object.get("facets");
						key.put("facets", facets.toString());
					}
				}

				if (!key.containsKey("solrId")) {
					key.put("solrId", solr.getGenomeIdsfromSolrOutput(solr.getGenomeIDsfromSolr(key.get("keyword"), facet, false)));
				}

				jsonResult.put("results", obj1);
				jsonResult.put("total", obj.get("numFound"));

				response.setContentType("application/json");
				PrintWriter writer = response.getWriter();
				writer.write(jsonResult.toString());
				writer.close();

			}
			else if (need.equals("tree")) {

				solr.setCurrentInstance(SolrCore.TRANSCRIPTOMICS_EXPERIMENT);

				pk = request.getParameter("pk");
				key = (ResultType) sess.getAttribute("key" + pk, PortletSession.APPLICATION_SCOPE);

				if (key.containsKey("state"))
					state = key.get("state");
				else
					state = request.getParameter("state");

				key.put("state", state);
				sess.setAttribute("key" + pk, key, PortletSession.APPLICATION_SCOPE);

				try {
					if (!key.containsKey("tree")) {
						JSONObject facet_fields = (JSONObject) new JSONParser().parse(key.get("facets"));
						JSONArray arr1 = solr.processStateAndTree(key, need, facet_fields, key.get("facet"), state, 4, false);
						jsonResult.put("results", arr1);
						key.put("tree", arr1);
					}
					else {
						jsonResult.put("results", key.get("tree"));
					}
				}
				catch (ParseException e) {
					LOGGER.error(e.getMessage(), e);
				}

				response.setContentType("application/json");
				PrintWriter writer = response.getWriter();
				writer.write(jsonResult.get("results").toString());
				writer.close();
			}
		}
	}
}
