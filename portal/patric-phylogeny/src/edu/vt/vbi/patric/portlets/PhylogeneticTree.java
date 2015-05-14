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
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
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

	private ObjectReader jsonReader;

	@Override
	public void init() throws PortletException {
		super.init();

		ObjectMapper objectMapper = new ObjectMapper();
		jsonReader = objectMapper.reader(Map.class);

		// update genome-id mapping cache
		try {
			DataApiHandler dataApi = new DataApiHandler();

			SolrQuery query = new SolrQuery("*:*");
			query.setFields("genome_name,genome_id").setRows(dataApi.MAX_ROWS);

			String apiResponse = dataApi.solrQuery(SolrCore.GENOME, query);

			Map resp = jsonReader.readValue(apiResponse);
			Map respBody = (Map) resp.get("response");
			List<Genome> genomes = dataApi.bindDocuments((List<Map>) respBody.get("docs"), Genome.class);

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
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		response.setContentType("text/html");

		SiteHelper.setHtmlMetaElements(request, response, "Phylogeny");

		List<Integer> phylogenyOrderIds = Arrays
				.asList(2037, 1385, 80840, 213849, 51291, 186802, 91347, 186826, 118969, 356, 766, 136, 72273, 135623);

		String contextType = request.getParameter("context_type");
		String contextId = request.getParameter("context_id");

		if (contextType != null && contextId != null) {

			DataApiHandler dataApi = new DataApiHandler(request);

			List<Map<String, Object>> orderList = new ArrayList<>();
			int taxonId = 0;

			try {
				if (contextType.equals("genome")) {

					Genome genome = dataApi.getGenome(contextId);
					taxonId = genome.getTaxonId();
				}
				else {
					taxonId = Integer.parseInt(contextId);
				}

				// Step1. has Order in lineage?
				Taxonomy taxonomy = dataApi.getTaxonomy(taxonId);

				List<String> lineageRanks = taxonomy.getLineageRanks();

				if (lineageRanks.contains("order")) {
					List<Integer> lineageIds = taxonomy.getLineageIds();
					List<String> lineageNames = taxonomy.getLineageNames();

					int index = lineageRanks.indexOf("order");
					int orderTaxonId = lineageIds.get(index);

					if (phylogenyOrderIds.contains(orderTaxonId)) {
						Map order = new HashMap();
						order.put("name", lineageNames.get(index));
						order.put("taxonId", lineageIds.get(index));

						orderList.add(order);
					}
				}

				if (orderList.isEmpty()) {
					// no rank Order found in lineage, then,
					// Step2. has Order rank in descendants
					SolrQuery query = new SolrQuery(
							"lineage_ids:" + taxonId + " AND taxon_rank:order AND taxon_id:(" + StringUtils.join(phylogenyOrderIds, " OR ") + ")");
					query.setFields("taxon_id,taxon_name,taxon_rank");
					query.setRows(100);

					LOGGER.trace("[{}] {}", SolrCore.TAXONOMY.getSolrCoreName(), query.toString());
					String apiResponse = dataApi.solrQuery(SolrCore.TAXONOMY, query);

					Map resp = jsonReader.readValue(apiResponse);
					Map respBody = (Map) resp.get("response");

					List<Map> sdl = (List<Map>) respBody.get("docs");
					for (Map doc : sdl) {
						if (doc.get("taxon_rank").equals("order")) {
							Map node = new HashMap<>();
							node.put("taxonId", doc.get("taxon_id"));
							node.put("name", doc.get("taxon_name").toString());

							orderList.add(node);
						}
					}
				}
			}
			catch (MalformedURLException e) {
				LOGGER.error(e.getMessage(), e);
			}

			request.setAttribute("orderList", orderList);
			request.setAttribute("taxonId", taxonId);

			PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/index.jsp");
			prd.include(request, response);
		}
	}
}
