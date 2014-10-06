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

import edu.vt.vbi.patric.beans.Genome;
import edu.vt.vbi.patric.common.SiteHelper;
import edu.vt.vbi.patric.common.SolrCore;
import edu.vt.vbi.patric.common.SolrInterface;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.*;

public class PhylogeneticTree extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(PhylogeneticTree.class);

	SolrInterface solr;

	@Override
	public void init() throws PortletException {
		super.init();

		// update genome-id mapping cache
		try {
			solr = new SolrInterface();
			solr.setCurrentInstance(SolrCore.GENOME);

			SolrQuery query = new SolrQuery("*:*");
			query.setFields("genome_name,genome_id");

			QueryResponse qr = solr.getServer().query(query);
			List<Genome> genomes = qr.getBeans(Genome.class);

			StringBuilder sb = new StringBuilder();
			sb.append("var genomeMap = new Array();\n");

			for (Genome genome : genomes) {
				sb.append("genomeMap[\"").append(genome.getGenomeName().replaceAll("[\\s\\(\\)\\:\\[\\],]+", "_")).append("\"] = \"")
						.append(genome.getId()).append("\";\n");
			}

			PrintWriter out = new PrintWriter(new FileWriter(getPortletContext().getRealPath("/js/genomeMaps.js")));
			out.println(sb.toString());
			out.close();
		}
		catch (MalformedURLException | SolrServerException e) {
			LOGGER.error(e.getMessage(), e);
		}
		catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		response.setContentType("text/html");

		new SiteHelper().setHtmlMetaElements(request, response, "Phylogeny");

		List<Integer> phylogenyOrderIds = Arrays
				.asList(2037, 1385, 80840, 213849, 51291, 186802, 91347, 186826, 118969, 356, 766, 136, 72273, 135623);

		String contextType = request.getParameter("context_type");
		String contextId = request.getParameter("context_id");

		if (contextType != null && contextId != null) {

			List<Map<String, Object>> orderList = new ArrayList<>();
			int taxonId = 0;

			try {
				if (contextType.equals("genome")) {
					solr.setCurrentInstance(SolrCore.GENOME);
					SolrQuery query = new SolrQuery("genome_id:" + contextId);
					query.setFields("taxonId");

					QueryResponse qr = solr.getServer().query(query);
					List<Genome> genomes = qr.getBeans(Genome.class);

					for (Genome genome : genomes) {
						taxonId = genome.getTaxonId();
					}
				}
				else {
					taxonId = Integer.parseInt(contextId);
				}

				solr.setCurrentInstance(SolrCore.TAXONOMY);

				// Step1. has Order in lineage?
				SolrQuery query = new SolrQuery("taxon_id:" + taxonId + " AND lineage_ranks:order");
				query.setFields("lineage_ids,lineage_names,lineage_ranks");
				QueryResponse qr = solr.getServer().query(query);

				SolrDocumentList sdl = qr.getResults();
				for (SolrDocument doc : sdl) {
					List<Integer> txIds = (List<Integer>) doc.get("lineage_ids");
					List<String> txNames = (List<String>) doc.get("lineage_names");
					List<String> txRanks = (List<String>) doc.get("lineage_ranks");

					for (Integer txId : txIds) {
						int idx = txIds.indexOf(txId);

						if (txRanks.get(idx).equals("order")) {
							Map<String, Object> node = new HashMap<>();
							node.put("taxonId", txId);
							node.put("name", txNames.get(idx));

							orderList.add(node);
						}
					}
				}

				if (orderList.isEmpty()) {
					// no rank Order found in lineage, then,
					// Step2. has Order rank in descendants
					query = new SolrQuery(
							"lineage_ids:" + taxonId + " AND taxon_rank:order AND taxon_id:(" + StringUtils.join(phylogenyOrderIds, " OR ") + ")");
					query.setFields("taxon_id,taxon_name,taxon_rank");
					query.setRows(100);
					qr = solr.getServer().query(query);

					sdl = qr.getResults();
					for (SolrDocument doc : sdl) {

						if (doc.get("taxon_rank").equals("order")) {
							Map<String, Object> node = new HashMap<>();
							node.put("taxonId", (Integer) doc.get("taxon_id"));
							node.put("name", doc.get("taxon_name").toString());

							orderList.add(node);
						}
					}
				}
			}
			catch (MalformedURLException | SolrServerException e) {
				LOGGER.error(e.getMessage(), e);
			}

			LOGGER.trace("{}", orderList);

			request.setAttribute("orderList", orderList);
			request.setAttribute("taxonId", taxonId);

			PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/index.jsp");
			prd.include(request, response);
		}
	}
}
