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

import edu.vt.vbi.patric.beans.GenomeFeature;
import edu.vt.vbi.patric.common.SiteHelper;
import edu.vt.vbi.patric.common.SolrCore;
import edu.vt.vbi.patric.common.SolrInterface;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SpecialtyGeneEvidencePortlet extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpecialtyGeneEvidencePortlet.class);

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		response.setContentType("text/html");

		new SiteHelper().setHtmlMetaElements(request, response, "Specialty Gene Evidence");
		response.setTitle("Specialty Gene Evidence");

		String source = request.getParameter("sp_source");
		String sourceId = request.getParameter("sp_source_id"); //lmo0433, Rv3875

		if (source != null && !source.equals("") && sourceId != null && !sourceId.equals("")) {

			List<String> properties = Arrays.asList("property", "source", "source_id", "gene_name", "organism", "product", "gi", "gene_id");
			List<String> headers = Arrays.asList("Property", "Source", "Source ID", "Gene", "Organism", "Product", "GI Number", "Gene ID");

			SolrInterface solr = new SolrInterface();

			// get properties of gene
			Map<String, Object> gene = null;
			try {
				SolrQuery query = new SolrQuery("source:" + source + " AND source_id:" + sourceId);

				QueryResponse qr = solr.getSolrServer(SolrCore.SPECIALTY_GENE).query(query);
				SolrDocumentList sdl = qr.getResults();
				if (!sdl.isEmpty()) {
					gene = sdl.get(0).getFieldValueMap();
				}
			}
			catch (MalformedURLException | SolrServerException e) {
				LOGGER.error(e.getMessage(), e);
			}


			// get PATRIC feature
			GenomeFeature feature = null;
			try {
				SolrQuery query = new SolrQuery("source:" + source + " AND source_id:" + sourceId + " AND evidence:Literature");
				query.setFields("feature_id");

				QueryResponse qr = solr.getSolrServer(SolrCore.SPECIALTY_GENE_MAPPING).query(query);
				SolrDocumentList sdl = qr.getResults();
				if (!sdl.isEmpty()) {
					SolrDocument doc = sdl.get(0);
					feature = solr.getFeature(doc.get("feature_id").toString());
				}
				else {
					query = new SolrQuery("alt_locus_tag:" + sourceId);
					qr = solr.getSolrServer(SolrCore.FEATURE).query(query);
					List<GenomeFeature> features = qr.getBeans(GenomeFeature.class);

					if (!features.isEmpty()) {
						feature = features.get(0);
					}
				}
			}
			catch (MalformedURLException | SolrServerException e) {
				LOGGER.error(e.getMessage(), e);
			}

			// get Homolog count
			int cntHomolog = 0;
			try {
				SolrQuery query = new SolrQuery("source:" + source + " AND source_id:" + sourceId);

				QueryResponse qr = solr.getSolrServer(SolrCore.SPECIALTY_GENE_MAPPING).query(query);
				cntHomolog = (int) qr.getResults().getNumFound();
			}
			catch (MalformedURLException | SolrServerException e) {
				LOGGER.error(e.getMessage(), e);
			}

			// get list of evidence
			List<Map<String, Object>> specialtyGeneEvidence = new ArrayList<>();
			try {
				SolrQuery query = new SolrQuery("source:" + source + " AND source_id:" + sourceId);
				query.addSort("specific_organism", SolrQuery.ORDER.asc);
				query.addSort("specific_host", SolrQuery.ORDER.asc);
				query.addSort("classification", SolrQuery.ORDER.asc);

				QueryResponse qr = solr.getSolrServer(SolrCore.SPECIALTY_GENE_EVIDENCE).query(query);
				SolrDocumentList evidence = qr.getResults();
				for (SolrDocument doc: evidence) {
					specialtyGeneEvidence.add(doc.getFieldValueMap());
				}
			}
			catch (MalformedURLException | SolrServerException e) {
				LOGGER.error(e.getMessage(), e);
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
