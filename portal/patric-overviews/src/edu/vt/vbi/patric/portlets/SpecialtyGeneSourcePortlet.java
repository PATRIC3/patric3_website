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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.FacetParams;
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

public class SpecialtyGeneSourcePortlet extends GenericPortlet {

	SolrInterface solr = new SolrInterface();

	private static final Logger LOGGER = LoggerFactory.getLogger(SpecialtyGeneSourcePortlet.class);

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		response.setContentType("text/html");

		PortletRequestDispatcher prd;

		new SiteHelper().setHtmlMetaElements(request, response, "Specialty Gene Source");
		response.setTitle("Specialty Gene Source");
		prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/specialty_gene_source.jsp");
		prd.include(request, response);
	}

	@SuppressWarnings("unchecked")
	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		String sraction = request.getParameter("sraction");

		if (sraction != null && sraction.equals("save_params")) {
			ResultType key = new ResultType();
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
			
			// random
			Random g = new Random();
			int random = g.nextInt();

			PortletSession sess = request.getPortletSession(true);
			sess.setAttribute("key" + random, key, PortletSession.APPLICATION_SCOPE);

			PrintWriter writer = response.getWriter();
			writer.write("" + random);
			writer.close();
		}
		else if (sraction != null && sraction.equals("get_params")) {
			String ret = "";
			String pk = request.getParameter("pk");
			PortletSession sess = request.getPortletSession();

			if (sess.getAttribute("key" + pk, PortletSession.APPLICATION_SCOPE) != null) {
				ResultType key = (ResultType) sess.getAttribute("key" + pk, PortletSession.APPLICATION_SCOPE);
				ret = key.get("keyword");
			}

			PrintWriter writer = response.getWriter();
			writer.write("" + ret);
			writer.close();
		}
		else {
			String need = request.getParameter("need");
			String facet = "", keyword = "", pk = "", state = "", source = "";
			boolean hl = false;
			PortletSession sess = request.getPortletSession();
			ResultType key = new ResultType();
			JSONObject jsonResult = new JSONObject();

			if (need.equals("0")) {

				pk = request.getParameter("pk");
				keyword = request.getParameter("keyword");
				facet = request.getParameter("facet");
				source = request.getParameter("source");

				// homolog counts
				Map<String, Long> hmCounts = new HashMap<>();

				try {
					SolrQuery query = new SolrQuery("*:*");
					query.setFilterQueries("source:" + source);
					query.setRows(0).setFacet(true).setFacetMinCount(1).setFacetLimit(-1).addFacetField("source_id");

					QueryResponse res = solr.getSolrServer(SolrCore.SPECIALTY_GENE_MAPPING).query(query);
					FacetField ff = res.getFacetField("source_id");
					List<FacetField.Count> ffSourceId = ff.getValues();

					for (FacetField.Count sourceId: ffSourceId) {
						hmCounts.put(sourceId.getName(), sourceId.getCount());
					}
				}
				catch (SolrServerException e) {
					LOGGER.error(e.getMessage(), e);
				}

				// sp_genes
				try {
					//&sort=source_id+asc,+locus_tag+asc&facet.sort=count&start=0
					//&facet.field=genus&facet.field=species&facet.field=organism&facet.field=classification&wt=&fq=source:PATRIC_VF
					SolrQuery query = new SolrQuery(keyword);
					query.setFilterQueries("source:" + source);
					query.setFacet(true).setFacetMinCount(1).setFacetLimit(-1).setFacetSort(FacetParams.FACET_SORT_COUNT).addFacetField("genus", "species", "organism",
							"classification");
					query.setFields("property,source,source_id,gene_name,locus_tag,gene_id,gi,genus,species,organism,product,function,classification,pmid");

					// paging params
					String start_id = request.getParameter("start");
					String limit = request.getParameter("limit");
					int start = Integer.parseInt(start_id);
					int end = Integer.parseInt(limit);

					query.setStart(start);
					if (end != -1) {
						query.setRows(end);
					}

					// sorting params
					if (request.getParameter("sort") != null) {
						JSONArray sorter;
						try {
							sorter = (JSONArray) new JSONParser().parse(request.getParameter("sort"));
							for (Object aSort: sorter) {
								JSONObject jsonSort = (JSONObject) aSort;
								query.addSort(SolrQuery.SortClause.create(jsonSort.get("property").toString(), jsonSort.get("direction").toString().toLowerCase()));
							}
						}
						catch (ParseException e) {
							LOGGER.error(e.getMessage(), e);
						}
					}

					QueryResponse qr = solr.getSolrServer(SolrCore.SPECIALTY_GENE).query(query);
					SolrDocumentList sdl = qr.getResults();
					long numFound = sdl.getNumFound();
					JSONArray results = new JSONArray();

					for (SolrDocument doc: sdl) {
						JSONObject row = new JSONObject();
						row.putAll(doc);
						if (hmCounts.containsKey(row.get("source_id").toString())) {
							row.put("homologs", hmCounts.get(row.get("source_id").toString()));
						}
						else {
							row.put("homologs", 0);
						}
						results.add(row);
					}

					jsonResult.put("results", results);
					jsonResult.put("total", (int) numFound);

					// process facets
					if (facet != null) {
						JSONObject facets = solr.facetFieldstoJSONObject(qr);
						key.put("facets", facets.toString());
					}
					key.put("facet", facet);
					key.put("keyword", keyword);
					key.put("source", source);
					sess.setAttribute("key" + pk, key, PortletSession.APPLICATION_SCOPE);
				}
				catch (SolrServerException e) {
					LOGGER.error(e.getMessage(), e);
				}

				response.setContentType("application/json");
				PrintWriter writer = response.getWriter();
				jsonResult.writeJSONString(writer);
				writer.close();

			}
			else if (need.equals("tree")) {

				pk = request.getParameter("pk");
				key = (ResultType) sess.getAttribute("key" + pk, PortletSession.APPLICATION_SCOPE);

				if (key.containsKey("state")) {
					state = key.get("state");
				}
				else {
					state = request.getParameter("state");
				}

				key.put("state", state);

				sess.setAttribute("key" + pk, key, PortletSession.APPLICATION_SCOPE);

				try {
					if (!key.containsKey("tree")) {
						solr.setCurrentInstance(SolrCore.SPECIALTY_GENE);
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
