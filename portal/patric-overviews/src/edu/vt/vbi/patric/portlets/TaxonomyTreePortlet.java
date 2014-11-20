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

import edu.vt.vbi.patric.beans.Taxonomy;
import edu.vt.vbi.patric.common.SiteHelper;
import edu.vt.vbi.patric.common.SolrCore;
import edu.vt.vbi.patric.common.SolrInterface;
import edu.vt.vbi.patric.tree.TaxonomyTreeNode;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;

public class TaxonomyTreePortlet extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaxonomyTreePortlet.class);

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		response.setContentType("text/html");

		new SiteHelper().setHtmlMetaElements(request, response, "Taxonomy Tree");

		String cType = request.getParameter("context_type");
		String cId = request.getParameter("context_id");
		int taxonId = -1;
		if (cType != null && cType.equals("taxon") && cId!=null) {
			taxonId = Integer.parseInt(cId);
		}

		request.setAttribute("taxonId", taxonId);

		PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/taxonomy_tree.jsp");
		prd.include(request, response);
	}

	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
		int taxonId = Integer.parseInt(request.getParameter("taxonId"));

		JSONArray tree = processTree(taxonId);

		response.setContentType("application/json");
		tree.writeJSONString(response.getWriter());
	}

	private JSONArray processTree(int taxonId) {

		SolrInterface solr = new SolrInterface();
		JSONArray treeJSON = new JSONArray();

		try {
			SolrQuery query = new SolrQuery("lineage_ids:" + taxonId + " AND genomes:[1 TO *]");
			query.addField("taxon_id,taxon_rank,taxon_name,genomes,lineage_ids").setRows(100000);
			// LOGGER.debug(query.toString());

			QueryResponse qr = solr.getSolrServer(SolrCore.TAXONOMY).query(query);

			List<Taxonomy> taxonomyList = qr.getBeans(Taxonomy.class);
			Map<Integer, Taxonomy> taxonomyMap = new HashMap<>();

			// 1 populate map for detail info
			for (Taxonomy tx: taxonomyList) {
				taxonomyMap.put(tx.getId(), tx);
			}

			// 2 add to rawData array
			List<List<TaxonomyTreeNode>> rawData = new ArrayList<>();
			for (Taxonomy tx : taxonomyList) {
				List<Integer> lineage = tx.getLineageIds();
				List<Integer> descendantIds = lineage.subList(lineage.indexOf(taxonId), lineage.size());

				List<TaxonomyTreeNode> descendant = new LinkedList<>();
				for (Integer txId : descendantIds) {
					descendant.add(new TaxonomyTreeNode(taxonomyMap.get(txId)));
				}
				rawData.add(descendant);
			}

			// 3 build a tree
			TaxonomyTreeNode wrapper = new TaxonomyTreeNode();
			TaxonomyTreeNode current = wrapper;
			for (List<TaxonomyTreeNode> tree: rawData) {
				TaxonomyTreeNode root = current;

				int parentId = taxonId;
				for (TaxonomyTreeNode node : tree) {
					node.setParentId(parentId);
					current = current.child(node);
					parentId = node.getTaxonId();
				}

				current = root;
			}

			TaxonomyTreeNode root = wrapper.getFirstChild();
			treeJSON.add(root.getJSONObject());
			// LOGGER.debug(treeJSON.toJSONString());
		}
		catch (MalformedURLException | SolrServerException e) {
			LOGGER.error(e.getMessage(), e);
		}

		return treeJSON;
	}
}
