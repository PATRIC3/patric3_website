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

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.UnavailableException;

import edu.vt.vbi.patric.common.SiteHelper;

public class HPITool extends GenericPortlet {

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.portlet.GenericPortlet#doView(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
	 */
	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException, UnavailableException {

		new SiteHelper().setHtmlMetaElements(request, response, "Host-Pathogen Interaction Finder");
		response.setContentType("text/html");
		response.setTitle("Host-Pathogen Interactions");
		PortletRequestDispatcher prd = null;

		String mode = request.getParameter("display_mode");

		if (mode != null && mode.equals("tab")) {
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/hpi_finder_tab.jsp");
		}
		else {
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/hpi_finder.jsp");
		}

		prd.include(request, response);
	}
/*
	@SuppressWarnings("unchecked")
	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		response.setContentType("application/json");

		String type_ = request.getParameter("type");
		String sraction = request.getParameter("sraction");

		// System.out.print("type=" + type_);

		JSONObject jsonResult = new JSONObject();
		DBPIG conn_pig = new DBPIG();

		if (type_ != null && type_.equals("genome-selector")) {

			ArrayList<ResultType> items = null;

			try {
				if (type_.equals("genome-selector")) {
					items = conn_pig.getGenomeInteractionTree();
				}
				else if (type_.equals("dmethod")) {
					String genomeIdsToFilter = request.getParameter("genomeIds");
					System.out.print("filtering on " + genomeIdsToFilter);
					items = conn_pig.getPIGMethods(genomeIdsToFilter);
				}
				else if (type_.equals("itype")) {
					String genomeIdsToFilter = request.getParameter("genomeIds");
					System.out.print("filtering on " + genomeIdsToFilter);
					items = conn_pig.getPIGTypes(genomeIdsToFilter);
				}
				else if (type_.equals("isource")) {
					String genomeIdsToFilter = request.getParameter("genomeIds");
					System.out.print("filtering on " + genomeIdsToFilter);
					items = conn_pig.getPIGSources(genomeIdsToFilter);
				}

				// System.out.print("size=" + items.size());

				JSONArray results = new JSONArray();

				for (int i = 0; i < items.size(); i++) {
					ResultType g = (ResultType) items.get(i);
					if (g.get("taxon_id_a").equals("9606"))
						continue;
					else {
						JSONObject obj = new JSONObject();
						obj.putAll(g);
						results.add(obj);
					}
				}
				jsonResult.put("results", results);

			}
			catch (Exception ex) {
				System.out.println("**mm*" + ex.toString());
			}

			response.setContentType("application/json");
			PrintWriter writer = response.getWriter();
			writer.write(jsonResult.get("results").toString());
			writer.close();
		}
		else if (sraction != null && sraction.equals("save_params")) {

			String source = request.getParameter("source");
			String types = request.getParameter("type");
			String method = request.getParameter("method");
			String genomeIds = request.getParameter("genomeIds");

			ResultType key = new ResultType();

			if (source != null && !source.equalsIgnoreCase("")) {
				key.put("source", source);
			}
			if (types != null && !types.equalsIgnoreCase("")) {
				key.put("type", types);
			}
			if (method != null && !method.equalsIgnoreCase("")) {
				key.put("method", method);
			}
			if (genomeIds != null && !genomeIds.equalsIgnoreCase("")) {
				key.put("genomeIds", genomeIds);
			}

			Random g = new Random();
			int random = g.nextInt();

			PortletSession sess = request.getPortletSession(true);
			sess.setAttribute("key" + random, key, PortletSession.APPLICATION_SCOPE);

			PrintWriter writer = response.getWriter();
			writer.write("" + random);
			writer.close();
		}
		else if (type_ != null && type_.equals("graph")) {

			ArrayList<ResultType> nodes_ = null;
			ArrayList<ResultType> interactions_ = null;
			String pk = request.getParameter("pk");
			JSONObject jsonAll = new JSONObject();
			JSONArray grid_data = new JSONArray();
			PortletSession sess = request.getPortletSession();
			ResultType key = (ResultType) sess.getAttribute("key" + pk, PortletSession.APPLICATION_SCOPE);

			// System.out.print(key.get("genomeIds"));
			// System.out.print(key.get("type"));
			// System.out.print(key.get("source"));
			// System.out.print(key.get("method"));

			int count_nodes = conn_pig.getNodesCount(key.toHashMap());

			System.out.print("count_nodes" + count_nodes);

			if (count_nodes > 0) {

				nodes_ = conn_pig.getNodes(key.toHashMap());

				// System.out.print("nodes size" + nodes_.size());

				try {

					JSONArray nodes = new JSONArray();
					JSONArray taxa = new JSONArray();
					JSONObject taxas = new JSONObject();
					JSONArray mol = new JSONArray();
					JSONObject mols = new JSONObject();

					HashMap<String, String> lookup_nodes = new HashMap<String, String>();
					HashMap<String, String> lookup_taxa = new HashMap<String, String>();
					for (int i = 0; i < nodes_.size(); i++) {

						ResultType g = (ResultType) nodes_.get(i);

						if (!lookup_nodes.containsKey(g.get("interactor_id"))) {
							JSONObject temp_sub = new JSONObject();

							temp_sub.put("id", g.get("interactor_id"));
							temp_sub.put("na_feature_id", g.get("na_feature_id"));
							temp_sub.put("locus_tag", g.get("locus_tag"));
							temp_sub.put("taxid", Integer.parseInt(g.get("tax_id").toString()));
							temp_sub.put("mol_id", Integer.parseInt("1"));
							temp_sub.put("name", g.get("label"));

							lookup_nodes.put(g.get("interactor_id"), "true");
							nodes.add(temp_sub);
						}

						if (!lookup_taxa.containsKey(g.get("tax_id"))) {
							taxas.put("group", "nodes");
							taxas.put("idInGroup", "taxid");

							JSONObject temp_sub = new JSONObject();

							temp_sub.put("id", Integer.parseInt(g.get("tax_id").toString()));
							temp_sub.put("name", g.get("name"));
							temp_sub.put("visible", true);

							lookup_taxa.put(g.get("tax_id"), "true");

							taxa.add(temp_sub);
							taxas.put("entries", taxa);
						}
					}

					mols.put("group", "nodes");
					mols.put("idInGroup", "mol_id");

					JSONObject temp_sub = new JSONObject();

					temp_sub.put("id", Integer.parseInt("1"));
					temp_sub.put("name", "protein");
					temp_sub.put("visible", true);

					mol.add(temp_sub);
					mols.put("entries", mol);

					jsonResult.put("nodes", nodes);
					jsonResult.put("taxa", taxas);
					jsonResult.put("mol", mols);
				}
				catch (Exception ex) {
					System.out.println("**nodes*" + ex.toString());
				}

			}

			int count_interactions = conn_pig.getInteractionsCount(key.toHashMap());

			if (count_interactions > 0) {

				try {

					interactions_ = conn_pig.getInteractions(key.toHashMap(), 0, -1);

					JSONArray edges = new JSONArray();
					JSONArray type = new JSONArray();
					JSONObject types = new JSONObject();
					JSONArray method = new JSONArray();
					JSONObject methods = new JSONObject();
					JSONArray source = new JSONArray();
					JSONObject sources = new JSONObject();
					JSONArray reference = new JSONArray();
					JSONObject references = new JSONObject();

					HashMap<String, String> lookup_edges = new HashMap<String, String>();
					HashMap<String, String> lookup_type = new HashMap<String, String>();
					HashMap<String, String> lookup_method = new HashMap<String, String>();
					HashMap<String, String> lookup_source = new HashMap<String, String>();
					HashMap<String, String> lookup_reference = new HashMap<String, String>();
					int count = 0;

					// HashMap<String, String> lookup_nodes = new HashMap<String, String>();

					for (int i = 0; i < interactions_.size(); i++) {
						ResultType g = (ResultType) interactions_.get(i);

						// Grid Construction
						JSONObject obj = new JSONObject();
						obj.putAll(g);
						grid_data.add(obj);

						if (!lookup_edges.containsKey(g.get("pig_id"))) {
							count++;
							JSONObject temp_sub = new JSONObject();

							temp_sub.put("id", "" + count + "");
							temp_sub.put("source", g.get("source_mol_id"));
							temp_sub.put("target", g.get("target_mol_id"));
							temp_sub.put("pig_id", Integer.parseInt(g.get("pig_id").toString()));
							temp_sub.put("method_id", Integer.parseInt(g.get("method_id").toString()));
							temp_sub.put("type_id", Integer.parseInt(g.get("type_id").toString()));
							temp_sub.put("source_id", Integer.parseInt(g.get("source_id").toString()));
							temp_sub.put("reference_id", Integer.parseInt(g.get("reference_id").toString()));
							temp_sub.put("score", Double.parseDouble(g.get("interaction_score").toString()));

							lookup_edges.put(g.get("pig_id"), "true");
							edges.add(temp_sub);

						}

						if (!lookup_type.containsKey(g.get("type_id"))) {
							types.put("group", "edges");
							types.put("idInGroup", "type_id");

							JSONObject temp_sub = new JSONObject();

							temp_sub.put("id", Integer.parseInt(g.get("type_id").toString()));
							temp_sub.put("name", g.get("type_name"));
							temp_sub.put("source", g.get("type_source"));
							temp_sub.put("source_id", g.get("type_source_id"));
							temp_sub.put("visible", true);

							lookup_type.put(g.get("type_id"), "true");

							type.add(temp_sub);
							types.put("entries", type);
						}

						if (!lookup_method.containsKey(g.get("method_id"))) {
							methods.put("group", "edges");
							methods.put("idInGroup", "method_id");

							JSONObject temp_sub = new JSONObject();

							temp_sub.put("id", Integer.parseInt(g.get("method_id").toString()));
							temp_sub.put("name", g.get("method_name"));
							temp_sub.put("source", g.get("method_source"));
							temp_sub.put("source_id", g.get("method_source_id"));
							temp_sub.put("visible", true);

							lookup_method.put(g.get("method_id"), "true");

							method.add(temp_sub);
							methods.put("entries", method);

						}

						if (!lookup_source.containsKey(g.get("source_id"))) {
							sources.put("group", "edges");
							sources.put("idInGroup", "source_id");

							JSONObject temp_sub = new JSONObject();

							temp_sub.put("id", Integer.parseInt(g.get("source_id").toString()));
							temp_sub.put("name", g.get("source_name"));
							temp_sub.put("source_dbid", g.get("source_dbid"));
							temp_sub.put("visible", true);

							lookup_source.put(g.get("source_id"), "true");

							source.add(temp_sub);
							sources.put("entries", source);
						}

						if (!lookup_reference.containsKey(g.get("reference_id"))) {
							references.put("group", "edges");
							references.put("idInGroup", "reference_id");

							JSONObject temp_sub = new JSONObject();

							temp_sub.put("id", Integer.parseInt(g.get("reference_id").toString()));
							temp_sub.put("name", g.get("reference_name"));
							temp_sub.put("source", g.get("reference_source"));
							temp_sub.put("source_id", g.get("reference_source_id"));
							temp_sub.put("visible", true);

							lookup_reference.put(g.get("reference_id"), "true");

							reference.add(temp_sub);
							references.put("entries", reference);
						}
					}
					jsonResult.put("edges", edges);
					jsonResult.put("type", types);
					jsonResult.put("reference", references);
					jsonResult.put("method", methods);
					jsonResult.put("source", sources);
				}
				catch (Exception ex) {
					System.out.println("**interactions*" + ex.toString());
				}

			}

			jsonAll.put("graph", jsonResult);

			JSONObject jsonGrid = new JSONObject();
			jsonGrid.put("results", grid_data);
			jsonGrid.put("total", grid_data.size());
			key.put("grid", jsonGrid);
			sess.setAttribute("key" + pk, key, PortletSession.APPLICATION_SCOPE);

			response.setContentType("application/json");
			PrintWriter writer = response.getWriter();
			writer.write(jsonAll.toString());
			writer.close();
		}
		else if (type_ != null && type_.equals("grid")) {

			String pk = request.getParameter("pk");
			PortletSession sess = request.getPortletSession();
			ResultType key = (ResultType) sess.getAttribute("key" + pk, PortletSession.APPLICATION_SCOPE);

			response.setContentType("application/json");
			PrintWriter writer = response.getWriter();

			String start_id = request.getParameter("start");
			String limit = request.getParameter("limit");
			int start = Integer.parseInt(start_id);
			int end = Integer.parseInt(limit) + start;

			HashMap<String, String> sort = null;
			if (request.getParameter("sort") != null) {
				// sorting
				JSONParser a = new JSONParser();
				JSONArray sorter;
				String sort_field = "";
				String sort_dir = "";
				try {
					sorter = (JSONArray) a.parse(request.getParameter("sort").toString());
					sort_field += ((JSONObject) sorter.get(0)).get("property").toString();
					sort_dir += ((JSONObject) sorter.get(0)).get("direction").toString();
					for (int i = 1; i < sorter.size(); i++) {
						sort_field += "," + ((JSONObject) sorter.get(i)).get("property").toString();
					}
					System.out.println(sort_field);
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

			String filterList = request.getParameter("filterList");

			JSONParser parser = new JSONParser();
			JSONObject json = new JSONObject();
			JSONObject ret = new JSONObject();
			try {
				json = (JSONObject) parser.parse(key.get("grid"));
				JSONArray data = (JSONArray) json.get("results");
				JSONArray jsonArr = new JSONArray();
				JSONObject jsonfilterList = null;

				if (filterList != null && !filterList.equals(""))
					jsonfilterList = (JSONObject) parser.parse(filterList);

				int addcount = 0;
				ret.put("total", data.size());
				for (int i = start; i < end; i++) {
					if (i < data.size()) {
						boolean push_flag = true;
						JSONObject j = (JSONObject) data.get(i);
						if (jsonfilterList != null && jsonfilterList.get("type") != null) {
							JSONArray arr = ((JSONArray) jsonfilterList.get("type"));
							for (int k = 0; k < arr.size(); k++) {
								// System.out.println(j.get("type_name")+" -- "+arr.get(k).toString());
								if (j.get("type_id").equals(arr.get(k).toString())) {
									push_flag = false;
									break;
								}
							}
						}
						if (push_flag && jsonfilterList != null && jsonfilterList.get("method") != null) {
							JSONArray arr = ((JSONArray) jsonfilterList.get("method"));
							for (int k = 0; k < arr.size(); k++) {
								if (j.get("method_id").equals(arr.get(k).toString())) {
									push_flag = false;
									break;
								}
							}
						}
						if (push_flag && jsonfilterList != null && jsonfilterList.get("source") != null) {
							JSONArray arr = ((JSONArray) jsonfilterList.get("source"));
							for (int k = 0; k < arr.size(); k++) {
								if (j.get("source_id").equals(arr.get(k).toString())) {
									push_flag = false;
									break;
								}
							}
						}
						if (push_flag && jsonfilterList != null && jsonfilterList.get("taxa") != null) {
							JSONArray arr = ((JSONArray) jsonfilterList.get("taxa"));
							for (int k = 0; k < arr.size(); k++) {
								if (j.get("ncbi_tax_id_a").equals(arr.get(k).toString())) {
									push_flag = false;
									break;
								}
							}
						}
						if (push_flag) {
							jsonArr.add(addcount, j);
							addcount++;
						}
					}
				}
				if (jsonfilterList != null
						&& (jsonfilterList.get("taxa") != null || jsonfilterList.get("type") != null || jsonfilterList.get("method") != null || jsonfilterList
								.get("source") != null)) {
					ret.put("total", addcount);
				}

				System.out.println(addcount);
				System.out.println(data.size());

				ret.put("results", jsonArr);

				writer.write(ret.toString());
				writer.close();
			}
			catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}

	public String getName(String id, String data, ArrayList<ResultType> list, String which) {
		for (int j = 0; j < list.size(); j++) {
			ResultType gj = (ResultType) list.get(j);
			if (gj.get(id).equals(data)) {
				if (gj.get("desc_" + which).toString().equalsIgnoreCase("shortlabel")) {
					return gj.get("name_" + which);
				}
			}
		}

		for (int j = 0; j < list.size(); j++) {
			ResultType gj = (ResultType) list.get(j);
			if (gj.get(id).equals(data)) {
				if (gj.get("desc_" + which).toString().equalsIgnoreCase("gene name")) {
					return gj.get("name_" + which);
				}
			}
		}

		for (int j = 0; j < list.size(); j++) {
			ResultType gj = (ResultType) list.get(j);
			if (gj.get(id).equals(data)) {
				if (gj.get("desc_" + which).toString().equalsIgnoreCase("gene name synonym")) {
					return gj.get("name_" + which);
				}
			}
		}

		for (int j = 0; j < list.size(); j++) {
			ResultType gj = (ResultType) list.get(j);
			if (gj.get(id).equals(data)) {
				if (gj.get("desc_" + which).toString().equalsIgnoreCase("locus name")) {
					return gj.get("name_" + which);
				}
			}
		}

		for (int j = 0; j < list.size(); j++) {
			ResultType gj = (ResultType) list.get(j);
			if (gj.get(id).equals(data)) {
				if (gj.get("desc_" + which).toString().equalsIgnoreCase("null")) {
					return gj.get("name_" + which);
				}
			}
		}

		return "";
	}
	*/
}
