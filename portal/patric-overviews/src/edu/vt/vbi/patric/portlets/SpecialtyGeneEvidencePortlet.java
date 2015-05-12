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

import edu.vt.vbi.patric.beans.GenomeFeature;
import edu.vt.vbi.patric.common.DataApiHandler;
import edu.vt.vbi.patric.common.SiteHelper;
import edu.vt.vbi.patric.common.SolrCore;
import org.apache.solr.client.solrj.SolrQuery;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SpecialtyGeneEvidencePortlet extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpecialtyGeneEvidencePortlet.class);

	private ObjectReader jsonReader;

	@Override
	public void init() throws PortletException {
		super.init();

		ObjectMapper objectMapper = new ObjectMapper();
		jsonReader = objectMapper.reader(Map.class);
	}

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		response.setContentType("text/html");

		SiteHelper.setHtmlMetaElements(request, response, "Specialty Gene Evidence");
		response.setTitle("Specialty Gene Evidence");

		String source = request.getParameter("sp_source");
		String sourceId = request.getParameter("sp_source_id"); //lmo0433, Rv3875

		if (source != null && !source.equals("") && sourceId != null && !sourceId.equals("")) {

			List<String> properties = Arrays.asList("property", "source", "source_id", "gene_name", "organism", "product", "gi", "gene_id");
			List<String> headers = Arrays.asList("Property", "Source", "Source ID", "Gene", "Organism", "Product", "GI Number", "Gene ID");

			// get properties of gene
			Map<String, Object> gene = null;
			SolrQuery query = new SolrQuery("source:" + source + " AND source_id:" + sourceId);

			DataApiHandler dataApi = new DataApiHandler(request);
			String apiResponse = dataApi.solrQuery(SolrCore.SPECIALTY_GENE, query);

			Map resp = jsonReader.readValue(apiResponse);
			Map respBody = (Map) resp.get("response");

			List<Map> sdl = (List<Map>) respBody.get("docs");

			if (!sdl.isEmpty()) {
				gene = sdl.get(0);
			}

			// get PATRIC feature
			GenomeFeature feature = null;
			query = new SolrQuery("source:" + source + " AND source_id:" + sourceId + " AND evidence:Literature");
			query.setFields("feature_id");

			apiResponse = dataApi.solrQuery(SolrCore.SPECIALTY_GENE_MAPPING, query);

			resp = jsonReader.readValue(apiResponse);
			respBody = (Map) resp.get("response");

			sdl = (List<Map>) respBody.get("docs");

			if (!sdl.isEmpty()) {
				Map doc = sdl.get(0);
				feature = dataApi.getFeature(doc.get("feature_id").toString());
			}
			else {
				query = new SolrQuery("alt_locus_tag:" + sourceId);

				apiResponse = dataApi.solrQuery(SolrCore.FEATURE, query);

				resp = jsonReader.readValue(apiResponse);
				respBody = (Map) resp.get("response");

				List<GenomeFeature> features  = dataApi.bindDocuments((List<Map>) respBody.get("docs"), GenomeFeature.class);

				if (!features.isEmpty()) {
					feature = features.get(0);
				}
			}

			// get Homolog count
			int cntHomolog = 0;
			query = new SolrQuery("source:" + source + " AND source_id:" + sourceId);

			apiResponse = dataApi.solrQuery(SolrCore.SPECIALTY_GENE_MAPPING, query);

			resp = jsonReader.readValue(apiResponse);
			respBody = (Map) resp.get("response");

			cntHomolog = (Integer) respBody.get("numFound");

			// get list of evidence
			List<Map<String, Object>> specialtyGeneEvidence = new ArrayList<>();
			query = new SolrQuery("source:" + source + " AND source_id:" + sourceId);
			query.addSort("specific_organism", SolrQuery.ORDER.asc);
			query.addSort("specific_host", SolrQuery.ORDER.asc);
			query.addSort("classification", SolrQuery.ORDER.asc);

			apiResponse = dataApi.solrQuery(SolrCore.SPECIALTY_GENE_EVIDENCE, query);

			resp = jsonReader.readValue(apiResponse);
			respBody = (Map) resp.get("response");

			List<Map> evidence = (List<Map>) respBody.get("docs");
			for (Map doc : evidence) {
				specialtyGeneEvidence.add(doc);
			}

			request.setAttribute("source", source);
			request.setAttribute("sourceId", sourceId);
			request.setAttribute("gene", gene);
			request.setAttribute("properties", properties);
			request.setAttribute("headers", headers);
			request.setAttribute("feature", feature);
			request.setAttribute("cntHomolog", cntHomolog);
			request.setAttribute("specialtyGeneEvidence", specialtyGeneEvidence);

			PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/overview/specialty_gene_evidence.jsp");
			prd.include(request, response);
		}
	}
}