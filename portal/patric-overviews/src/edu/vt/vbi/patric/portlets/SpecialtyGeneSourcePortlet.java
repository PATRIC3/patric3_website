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
import java.util.Random;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.UnavailableException;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
//import org.apache.solr.client.solrj.response.PivotField;
import org.apache.solr.client.solrj.response.QueryResponse;
//import org.apache.solr.common.util.NamedList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.vt.vbi.patric.common.SiteHelper;
import edu.vt.vbi.patric.common.SolrCore;
import edu.vt.vbi.patric.common.SolrInterface;
import edu.vt.vbi.patric.dao.ResultType;

public class SpecialtyGeneSourcePortlet extends GenericPortlet {

	SolrInterface solr = new SolrInterface();

	JSONParser jsonParser = new JSONParser();

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.portlet.GenericPortlet#doView(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
	 */
	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException, UnavailableException {

		response.setContentType("text/html");

		PortletRequestDispatcher prd = null;

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
				ret = key.get("keyword").toString();
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

				solr.setCurrentInstance(SolrCore.SPECIALTY_GENE);

				pk = request.getParameter("pk");
				keyword = request.getParameter("keyword");
				facet = request.getParameter("facet");
				source = request.getParameter("source");

				String highlight = request.getParameter("highlight");
				hl = Boolean.parseBoolean(highlight);

				if (sess.getAttribute("key" + pk, PortletSession.APPLICATION_SCOPE) == null) {
					key.put("facet", facet);
					key.put("keyword", keyword);
					key.put("source", source);
					sess.setAttribute("key" + pk, key, PortletSession.APPLICATION_SCOPE);
				}
				else {
					key = (ResultType) sess.getAttribute("key" + pk, PortletSession.APPLICATION_SCOPE);
					key.put("facet", facet);
					key.put("source", source);
				}
				
				// set source field as a filter query condition
				key.put("filter", "source:" + key.get("source"));
				//System.out.println(key.toString());
				
				String start_id = request.getParameter("start");
				String limit = request.getParameter("limit");
				int start = Integer.parseInt(start_id);
				int end = Integer.parseInt(limit);

				HashMap<String, String> sort = null;
				if (request.getParameter("sort") != null) {
					// sorting
					JSONArray sorter;
					String sort_field = "";
					String sort_dir = "";
					try {
						sorter = (JSONArray) jsonParser.parse(request.getParameter("sort").toString());
						sort_field += ((JSONObject) sorter.get(0)).get("property").toString();
						sort_dir += ((JSONObject) sorter.get(0)).get("direction").toString();
						for (int i = 1; i < sorter.size(); i++) {
							sort_field += "," + ((JSONObject) sorter.get(i)).get("property").toString();
						}
					}
					catch (ParseException e) {
						e.printStackTrace();
					}

					sort = new HashMap<String, String>();

					if (!sort_field.equals("") && !sort_dir.equals("")) {
						sort.put("field", sort_field);
						sort.put("direction", sort_dir);
					}
				}

				JSONObject object = solr.getData(key, sort, facet, start, end, facet != null ? true : false, hl, false);

				JSONObject obj = (JSONObject) object.get("response");
				JSONArray obj1 = (JSONArray) obj.get("docs");

				// counts for genus, species, genome level mapping
				SolrQuery query = new SolrQuery();
				query.setQuery("*:*");
				query.setFilterQueries("source: " + key.get("source"));
				query.setFacet(true);
				query.addFacetField("source_id");
				//query.addFacetPivotField("source_id,same_genus");
				query.setFacetMinCount(1);
				query.setFacetLimit(-1);
				query.setRows(0);
				
				//HashMap<String, HashMap<String, Integer>> hmCounts = new HashMap<String, HashMap<String, Integer>>();
				HashMap<String, Long> hmCounts = new HashMap<String, Long>();
				
				try {
					solr.setCurrentInstance(SolrCore.SPECIALTY_GENE_MAPPING);
					QueryResponse res = solr.getServer().query(query);
					FacetField ff = res.getFacetField("source_id");
					List<Count> ffSourceId = ff.getValues();
					for (Count ffsi: ffSourceId) {
						hmCounts.put(ffsi.getName(), ffsi.getCount());
					}
					/*
					NamedList<List<PivotField>> nlPivot = res.getFacetPivot();
					List<PivotField> lPivot = nlPivot.getVal(0);
					
					for (PivotField pivot: lPivot) {
						String sourceId = pivot.getValue().toString();
						int cnt = pivot.getCount();
						hmCounts.put(sourceId, cnt);
						///
						int cntSameGenus = 0, cntSameSpecies = 0, cntSameGenome = 0;
						
						List<PivotField> pvSameGenus = pivot.getPivot();
						for (PivotField pvSG: pvSameGenus) {
							if (pvSG.getValue().toString().equals("1")) {
								cntSameGenus = pvSG.getCount();
								//
								List<PivotField> pvSameSpecies = pvSG.getPivot();
								for (PivotField pvSS: pvSameSpecies) {
									if (pvSS.getValue().toString().equals("1")) {
										cntSameSpecies = pvSS.getCount();
										//
										List<PivotField> pvSameGenome = pvSS.getPivot();
										for (PivotField pvSN: pvSameGenome) {
											if (pvSN.getValue().toString().equals("1")) {
												cntSameGenome = pvSN.getCount();
											}
										}
									}
								}
							}
						}
						HashMap<String, Integer> cnt = new HashMap<String, Integer>();
						cnt.put("sameGenus", cntSameGenus);
						cnt.put("sameSpecies", cntSameSpecies);
						cnt.put("sameGenome", cntSameGenome);
						hmCounts.put(sourceId, cnt);
					}
					*/
				}
				catch (SolrServerException e) {
					e.printStackTrace();
				}
				//System.out.println(hmCounts.toString());

				//JSONArray obj1 = (JSONArray) obj.get("docs");
				JSONArray results = new JSONArray();
				for (int i = 0; i < obj1.size(); i++) {
					JSONObject row = (JSONObject) obj1.get(i);
					if (hmCounts.containsKey(row.get("source_id"))) {
						//row.put("same_genus", hmCounts.get(row.get("source_id")).get("sameGenus"));
						//row.put("same_species", hmCounts.get(row.get("source_id")).get("sameSpecies"));
						//row.put("same_genome", hmCounts.get(row.get("source_id")).get("sameGenome"));
						row.put("homologs", hmCounts.get(row.get("source_id")));
					}
					else {
						row.put("homologs", 0);
					}
					results.add(row);
				}
				if (!key.containsKey("facets")) {
					if (object.containsKey("facets")) {
						JSONObject facets = (JSONObject) object.get("facets");
						key.put("facets", facets.toString());
					}
				}

				//jsonResult.put("results", obj1);
				jsonResult.put("results", results);
				jsonResult.put("total", obj.get("numFound"));

				response.setContentType("application/json");
				PrintWriter writer = response.getWriter();
				jsonResult.writeJSONString(writer);
				writer.close();

			}
			else if (need.equals("tree")) {

				solr.setCurrentInstance(SolrCore.SPECIALTY_GENE);

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
					e.printStackTrace();
				}

				response.setContentType("application/json");
				PrintWriter writer = response.getWriter();
				writer.write(jsonResult.get("results").toString());
				writer.close();
			}
		}
	}
}
