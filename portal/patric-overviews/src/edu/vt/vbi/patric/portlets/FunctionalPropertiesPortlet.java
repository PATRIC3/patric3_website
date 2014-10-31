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
import edu.vt.vbi.patric.beans.GenomeFeature;
import edu.vt.vbi.patric.common.SolrCore;
import edu.vt.vbi.patric.common.SolrInterface;
import edu.vt.vbi.patric.dao.DBShared;
import edu.vt.vbi.patric.dao.DBSummary;
import edu.vt.vbi.patric.dao.ResultType;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FunctionalPropertiesPortlet extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(FeaturePropertiesPortlet.class);

	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		response.setContentType("text/html");
		String cType = request.getParameter("context_type");
		String cId = request.getParameter("context_id");

		PortletRequestDispatcher prd;

		DBShared conn_shared = new DBShared();
		DBSummary conn_summary = new DBSummary();

		GenomeFeature feature = null;

		SolrInterface solr = new SolrInterface();

		if (cType != null && cId != null && cType.equals("feature")) {
			feature = solr.getPATRICFeature(cId);
		}

		if (feature != null) {

			if (feature.getFeatureType().equals("CDS") || feature.getFeatureType().equals("mat_peptide")) {

				// to stuffs to here //
				// UniprotKBAccession
				List<String> listUniprotkbAccessions = feature.getUniprotkbAccession();
				String uniprotkbAccession = null;
				if (listUniprotkbAccessions != null && listUniprotkbAccessions.size() > 1) {
					uniprotkbAccession = listUniprotkbAccessions.get(0);
				}
				// UniprotIDs (PDBs)
				List<String> externalIds = feature.getExternalId();
				List<String> pdbIds = new ArrayList<>();

				if (externalIds != null) {
					for (String externalId : externalIds) {
						if (externalId.contains("PDB|")) {
							pdbIds.add(externalId.replace("PDB|", ""));
						}
					}
				}

				// Structure Center related
				List<Map<String, Object>> listStructure = null;
				try {
					SolrQuery query = new SolrQuery("gene_symbol_collection:\"PATRIC_ID:" + feature.getId() + "\"");

					QueryResponse qr = solr.getSolrServer(SolrCore.STRUCTURE).query(query);
					SolrDocumentList sdl = qr.getResults();

					for (SolrDocument doc : sdl) {
						listStructure.add(doc.getFieldValueMap());
					}

				}
				catch (MalformedURLException | SolrServerException e) {
					LOGGER.error(e.getMessage(), e);
				}

				// taxonomy genus name
				String genusName = null;
				try {
					String genomeId = feature.getGenomeId();

					SolrQuery query = new SolrQuery("genome_id:" + genomeId);
					QueryResponse qr = solr.getSolrServer(SolrCore.GENOME).query(query);

					List<Genome> listGenome = qr.getBeans(Genome.class);

					for (Genome genome : listGenome) {
						genusName = genome.getGenus();
					}

				}
				catch (MalformedURLException | SolrServerException e) {
					LOGGER.error(e.getMessage(), e);
				}
				// //

				request.setAttribute("feature", feature);
				request.setAttribute("uniprotkbAccession", uniprotkbAccession);
				request.setAttribute("pdbIds", pdbIds);
				request.setAttribute("listStructure", listStructure);
				request.setAttribute("genusName", genusName);

				prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/funtional_properties/protein.jsp");
				prd.include(request, response);
			}
			else if (feature.getFeatureType().contains("RNA")) {
				ResultType rnaInfo = conn_summary.getRNAInfo("" + feature.getP2FeatureId());

				if (rnaInfo.containsKey("comment_string") && rnaInfo.get("comment_string").contains("structure:")) {
					String[] tmp = rnaInfo.get("comment_string").split("structure:");
					if (tmp[0] != null) {
						rnaInfo.put("comment", tmp[0]);
					}
					if (tmp[1] != null) {
						rnaInfo.put("structure", tmp[1]);
					}
				}
				else if (rnaInfo.containsKey("comment_string")) {
					rnaInfo.put("comment", rnaInfo.get("comment_string"));
				}

				request.setAttribute("rna", rnaInfo);
				prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/funtional_properties/rna.jsp");
				prd.include(request, response);
			}
			else if (feature.getFeatureType().equals("misc_feature")) {
				String comment = conn_shared.getNaFeatureComment("" + feature.getP2FeatureId());
				request.setAttribute("comment", comment);
				prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/funtional_properties/misc_feature.jsp");
				prd.include(request, response);
			}
			else {
				PrintWriter writer = response.getWriter();
				writer.write("No information is available.");
				writer.close();
			}
		}
		else {
			PrintWriter writer = response.getWriter();
			writer.write("No information is available.");
			writer.close();
		}
	}
}
