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

import edu.vt.vbi.patric.beans.Taxonomy;
import edu.vt.vbi.patric.common.DataApiHandler;
import edu.vt.vbi.patric.common.SolrCore;
import org.apache.solr.client.solrj.SolrQuery;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TB extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(TB.class);

	private ObjectReader jsonReader;

	@Override
	public void init() throws PortletException {
		super.init();

		ObjectMapper objectMapper = new ObjectMapper();
		jsonReader = objectMapper.reader(Map.class);
	}

	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		response.setContentType("text/html");
		response.setTitle("TB");

		int mtbTaxon = 77643;
		DataApiHandler dataApi = new DataApiHandler(request);

		Map<Integer, Integer> genomes = new HashMap<>();
		int cntExperiments = -1;

		// getting genome count
		{
			SolrQuery query = new SolrQuery("taxon_id:(1773 OR 77643)");
			query.setRows(2).addField("taxon_id,genomes");

			String apiResponse = dataApi.solrQuery(SolrCore.TAXONOMY, query);

			Map resp = jsonReader.readValue(apiResponse);
			Map respBody = (Map) resp.get("response");

			List<Taxonomy> taxonomyList = dataApi.bindDocuments((List<Map>) respBody.get("docs"), Taxonomy.class);

			for (Taxonomy taxonomy : taxonomyList) {
				genomes.put(taxonomy.getId(), taxonomy.getGenomeCount());
			}
		}

		// getting expression data count
		{
			SolrQuery query = new SolrQuery("*:*");
			query.setRows(0).addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_ids", "taxon_lineage_ids:1763"));

			String apiResponse = dataApi.solrQuery(SolrCore.TRANSCRIPTOMICS_EXPERIMENT, query);

			Map resp = jsonReader.readValue(apiResponse);
			Map respBody = (Map) resp.get("response");

			cntExperiments = (Integer) respBody.get("numFound");
		}
		request.setAttribute("genomes", genomes);
		request.setAttribute("cntExperiments", cntExperiments);
		request.setAttribute("mtbTaxon", mtbTaxon);

		PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/community/tb.jsp");
		prd.include(request, response);
	}
}
