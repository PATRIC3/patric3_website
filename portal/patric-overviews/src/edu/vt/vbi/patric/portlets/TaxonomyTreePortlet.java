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
import edu.vt.vbi.patric.common.OrganismTreeBuilder;
import edu.vt.vbi.patric.common.SiteHelper;
import edu.vt.vbi.patric.common.SolrCore;
import org.apache.solr.client.solrj.SolrQuery;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TaxonomyTreePortlet extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaxonomyTreePortlet.class);

	ObjectReader jsonReader;

	@Override
	public void init() throws PortletException {
		super.init();

		ObjectMapper objectMapper = new ObjectMapper();
		jsonReader = objectMapper.reader(Map.class);
	}

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		response.setContentType("text/html");

		SiteHelper.setHtmlMetaElements(request, response, "Taxonomy Tree");

		String cType = request.getParameter("context_type");
		String cId = request.getParameter("context_id");
		int taxonId = -1;
		if (cType != null && cType.equals("taxon") && cId != null) {
			taxonId = Integer.parseInt(cId);
		}

		request.setAttribute("taxonId", taxonId);

		PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/taxonomy_tree.jsp");
		prd.include(request, response);
	}

	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
		int taxonId = Integer.parseInt(request.getParameter("taxonId"));
		String mode = request.getParameter("mode");

		DataApiHandler dataApi = new DataApiHandler(request);
		JSONArray tree;
		response.setContentType("application/json");

		switch (mode) {
		case "txtree":
			tree = OrganismTreeBuilder.buildGenomeTree(dataApi, taxonId);
			tree.writeJSONString(response.getWriter());
			break;
		case "azlist":
			tree = OrganismTreeBuilder.buildGenomeList(dataApi, taxonId);
			tree.writeJSONString(response.getWriter());
			break;
		case "tgm":
			tree = OrganismTreeBuilder.buildTaxonGenomeMapping(dataApi, taxonId);
			tree.writeJSONString(response.getWriter());
			break;
		case "search":
			String searchOn = request.getParameter("searchon");
			String keyword = request.getParameter("query");

			tree = new JSONArray();
			SolrQuery query = new SolrQuery();
			if (searchOn.equals("txtree")) {
				query.setQuery("taxon_name:" + keyword + " AND genomes:[1 TO *] AND lineage_ids:" + taxonId);
				query.setRows(10000).addField("taxon_name,taxon_id");

				String apiResponse = dataApi.solrQuery(SolrCore.TAXONOMY, query);
				Map resp = jsonReader.readValue(apiResponse);
				Map respBody = (Map) resp.get("response");

				List<Taxonomy> taxonomyList = dataApi.bindDocuments((List<Map>) respBody.get("docs"), Taxonomy.class);

				for (Taxonomy taxon : taxonomyList) {
					JSONObject item = new JSONObject();
					item.put("display_name", taxon.getTaxonName());
					item.put("taxon_id", taxon.getId());

					tree.add(item);
				}
			}
			else {
				// searchOn is "azlist"
				query.setQuery("genome_name:" + keyword + " AND taxon_lineage_ids:" + taxonId);
				query.setRows(10000).addField("genome_name,genome_id");

				String apiResponse = dataApi.solrQuery(SolrCore.TAXONOMY, query);
				Map resp = jsonReader.readValue(apiResponse);
				Map respBody = (Map) resp.get("response");

				List<Genome> genomeList = dataApi.bindDocuments((List<Map>) respBody.get("docs"), Genome.class);

				for (Genome genome : genomeList) {
					JSONObject item = new JSONObject();
					item.put("display_name", genome.getGenomeName());
					item.put("genome_id", genome.getId());

					tree.add(item);
				}
			}

			JSONObject result = new JSONObject();
			result.put("genomeList", tree);
			result.put("keyword", keyword);
			result.put("totalCount", tree.size());

			result.writeJSONString(response.getWriter());
			break;
		default:
			tree = new JSONArray();
			tree.writeJSONString(response.getWriter());
			break;
		}
	}
}
