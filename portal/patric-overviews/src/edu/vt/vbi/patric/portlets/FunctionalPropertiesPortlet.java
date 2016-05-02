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
import edu.vt.vbi.patric.beans.GenomeFeature;
import edu.vt.vbi.patric.common.DataApiHandler;
import edu.vt.vbi.patric.common.SolrCore;
import edu.vt.vbi.patric.dao.DBSummary;
import edu.vt.vbi.patric.dao.ResultType;
import org.apache.solr.client.solrj.SolrQuery;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FunctionalPropertiesPortlet extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(FeaturePropertiesPortlet.class);

	ObjectReader jsonReader;

	@Override
	public void init() throws PortletException {
		super.init();

		ObjectMapper objectMapper = new ObjectMapper();
		jsonReader = objectMapper.reader(Map.class);
	}

	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		response.setContentType("text/html");
		String cType = request.getParameter("context_type");
		String cId = request.getParameter("context_id");

		PortletRequestDispatcher prd;

		DBSummary conn_summary = new DBSummary();

		GenomeFeature feature = null;

		DataApiHandler dataApi = new DataApiHandler(request);

		if (cType != null && cId != null && cType.equals("feature")) {
			feature = dataApi.getPATRICFeature(cId);
		}

		if (feature != null) {

			if (feature.getFeatureType().equals("CDS") || feature.getFeatureType().equals("mat_peptide")) {

				// to stuffs to here //
				// UniprotKBAccession
				String uniprotkbAccession = null;
				List<String> pdbIds = new ArrayList<>();
				{
					SolrQuery query = new SolrQuery("id_value:(" + feature.getGi() + ")");
					query.addFilterQuery("id_type:GI");

					LOGGER.debug("[{}] {}", SolrCore.ID_REF.getSolrCoreName(), query);

					String apiResponse = dataApi.solrQuery(SolrCore.ID_REF, query);

					Map resp = jsonReader.readValue(apiResponse);
					Map respBody = (Map) resp.get("response");

					List<Map<String, String>> docs = (List) respBody.get("docs");

					for (Map<String, String> doc : docs) {
						uniprotkbAccession = doc.get("uniprotkb_accession");
					}
				}
				{
					SolrQuery query = new SolrQuery("uniprotkb_accession:(" + uniprotkbAccession + ")");
					query.setRows(DataApiHandler.MAX_ROWS);
					LOGGER.debug("[{}] {}", SolrCore.ID_REF.getSolrCoreName(), query);

					String apiResponse = dataApi.solrQuery(SolrCore.ID_REF, query);

					Map resp = jsonReader.readValue(apiResponse);
					Map respBody = (Map) resp.get("response");

					List<Map<String, String>> docs = (List) respBody.get("docs");

					for (Map<String, String> doc : docs) {
						if (doc.get("id_type").equals("PDB")) {
							pdbIds.add(doc.get("id_value"));
						}
					}
				}

				// Structure Center related
				List<Map> listStructure = null;

				SolrQuery query = new SolrQuery("gene_symbol_collection:\"PATRIC_ID:" + feature.getPatricId() + "\"");
				query.addField("target_id,target_status,has_clones,has_proteins,selection_criteria");
				LOGGER.trace("[{}] {}", SolrCore.STRUCTURE, query);

				String apiResponse = dataApi.solrQuery(SolrCore.STRUCTURE, query);

				Map resp = jsonReader.readValue(apiResponse);
				Map respBody = (Map) resp.get("response");

				List<Map> docs = (List) respBody.get("docs");

				if (!docs.isEmpty()) {
					listStructure = docs;
				}

				// taxonomy genus name
				String genusName = null;
				Genome genome = dataApi.getGenome(feature.getGenomeId());
				if (genome != null) {
					genusName = genome.getGenus();
				}

				request.setAttribute("feature", feature);
				request.setAttribute("uniprotkbAccession", uniprotkbAccession);
				request.setAttribute("pdbIds", pdbIds);
				request.setAttribute("listStructure", listStructure);
				request.setAttribute("genusName", genusName);

				prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/functional_properties/protein.jsp");
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
				prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/functional_properties/rna.jsp");
				prd.include(request, response);
			}
			else if (feature.getFeatureType().equals("misc_feature")) {
				String comment = ""; // conn_shared.getNaFeatureComment("" + feature.getP2FeatureId());
				request.setAttribute("comment", comment);
				prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/functional_properties/misc_feature.jsp");
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
