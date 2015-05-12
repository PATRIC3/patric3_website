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

import edu.vt.vbi.patric.beans.Genome;
import edu.vt.vbi.patric.beans.Taxonomy;
import edu.vt.vbi.patric.common.DataApiHandler;
import edu.vt.vbi.patric.common.SiteHelper;
import edu.vt.vbi.patric.common.SolrCore;
import edu.vt.vbi.patric.dao.DBDisease;
import edu.vt.vbi.patric.dao.ResultType;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DiseaseOverview extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(DiseaseOverview.class);

	private ObjectReader jsonReader;

	@Override
	public void init() throws PortletException {
		super.init();

		ObjectMapper objectMapper = new ObjectMapper();
		jsonReader = objectMapper.reader(Map.class);
	}

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		SiteHelper.setHtmlMetaElements(request, response, "Disease Overview");

		response.setContentType("text/html");
		response.setTitle("Disease Overview");

		String contextType = request.getParameter("context_type");
		String contextId = request.getParameter("context_id");
		int taxonId;
		List<Integer> targetGenusList = Arrays
				.asList(1386,773,138,234,32008,194,83553,1485,776,943,561,262,209,1637,1763,780,590,620,1279,1301,662,629);

		DataApiHandler dataApi = new DataApiHandler();

		if (contextType.equals("genome")) {
			Genome genome = dataApi.getGenome(contextId);
			taxonId = genome.getTaxonId();
		} else {
			taxonId = Integer.parseInt(contextId);
		}

		Taxonomy taxonomy = dataApi.getTaxonomy(taxonId);
		List<String> taxonLineageNames = taxonomy.getLineageNames();
		List<String> taxonLineageRanks = taxonomy.getLineageRanks();
		List<Integer> taxonLineageIds = taxonomy.getLineageIds();

		List<Taxonomy> genusList = new LinkedList<>();

		for (int i = 0; i < taxonLineageIds.size(); i++) {
			if (taxonLineageRanks.get(i).equals("genus") && targetGenusList.contains(taxonLineageIds.get(i))) {
				Taxonomy genus = new Taxonomy();
				genus.setId(taxonLineageIds.get(i));
				genus.setTaxonName(taxonLineageNames.get(i));

				genusList.add(genus);
			}
		}

		if (genusList.isEmpty()) {
			SolrQuery query = new SolrQuery("lineage_ids:" + taxonId + " AND taxon_rank:genus AND taxon_id:(" + StringUtils.join(targetGenusList, " OR ") + ")");

			String apiResponse = dataApi.solrQuery(SolrCore.TAXONOMY, query);

			Map resp = jsonReader.readValue(apiResponse);
			Map respBody = (Map) resp.get("response");

			genusList = dataApi.bindDocuments((List<Map>) respBody.get("docs"), Taxonomy.class);
		}

		request.setAttribute("contextType", contextType);
		request.setAttribute("contextId", contextId);
		request.setAttribute("genusList", genusList);

		PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/disease_overview.jsp");
		prd.include(request, response);
	}

	@SuppressWarnings("unchecked")
	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		response.setContentType("application/json");

		String type = request.getParameter("type");
		String cId = request.getParameter("cId");

		DBDisease conn_disease = new DBDisease();
		int count_total;
		JSONArray results = new JSONArray();
		PrintWriter writer = response.getWriter();

		if (type.equals("incidence")) {

			JSONObject jsonResult = new JSONObject();
			// String cType = request.getParameter("cType");

			// sorting
			// String sort_field = request.getParameter("sort");
			// String sort_dir = request.getParameter("dir");

			// Map<String, String> key = new HashMap<>();
			// Map<String, String> sort = null;
			//
			// if (sort_field != null && sort_dir != null) {
			// sort = new HashMap<String, String>();
			// sort.put("field", sort_field);
			// sort.put("direction", sort_dir);
			// }
			//
			// key.put("cId", cId);
			// key.put("cType", cType);
			count_total = 1;
			jsonResult.put("total", count_total);

			JSONObject obj = new JSONObject();
			obj.put("rownum", "1");
			obj.put("pathogen", "Pathogen");
			obj.put("disease", "Disease");
			obj.put("incidence", "10");
			obj.put("infection", "5");

			results.add(obj);
			jsonResult.put("results", results);

			jsonResult.writeJSONString(writer);
		}
		else if (type.equals("disease_tree")) {

			JSONArray jsonResult = new JSONArray();
			String tree_node = request.getParameter("node");
			List<ResultType> items = conn_disease.getMeshHierarchy(cId, tree_node);

			if (items.size() > 0) {
				int min = Integer.parseInt(items.get(0).get("lvl"));
				try {
					for (ResultType item : items) {
						if (min == Integer.parseInt(item.get("lvl"))) {

							boolean flag = false;
							JSONObject obj = DiseaseOverview.encodeNodeJSONObject(item);

							String mesh_id = (String) obj.get("tree_node");

							for (int j = 0; j < jsonResult.size(); j++) {

								JSONObject temp = (JSONObject) jsonResult.get(j);

								if (temp.get("tree_node").equals(mesh_id)) {
									flag = true;
									temp.put("pathogen", temp.get("pathogen") + "<br>" + obj.get("pathogen"));
									temp.put("genome", temp.get("genome") + "<br>" + obj.get("genome"));
									temp.put("vfdb", temp.get("vfdb") + "<br>" + obj.get("vfdb"));
									temp.put("gad", temp.get("gad") + "<br>" + obj.get("gad"));
									temp.put("ctd", temp.get("ctd") + "<br>" + obj.get("ctd"));
									temp.put("taxon_id", temp.get("taxon_id") + "<br>" + obj.get("taxon_id"));

									jsonResult.set(j, temp);
								}
							}
							if (!flag) {
								jsonResult.add(obj);
							}
						}
					}
				}
				catch (Exception ex) {
					LOGGER.error(ex.getMessage(), ex);
				}
			}
			jsonResult.writeJSONString(writer);
		}
		writer.close();
	}

	@SuppressWarnings("unchecked")
	public static JSONObject encodeNodeJSONObject(ResultType rt) {

		JSONObject obj = new JSONObject();
		obj.putAll(rt);

		obj.put("id", rt.get("tree_node"));
		obj.put("node", rt.get("tree_node"));
		obj.put("expanded", "true");

		if (rt.get("leaf").equals("1")) {
			obj.put("leaf", "true");
		}
		else {
			obj.put("leaf", "false");
		}
		return obj;
	}
}
