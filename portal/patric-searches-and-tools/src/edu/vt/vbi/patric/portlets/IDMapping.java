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
import edu.vt.vbi.patric.dao.ResultType;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IDMapping extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(IDMapping.class);

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		response.setContentType("text/html");
		response.setTitle("ID Mapping");
		String mode = request.getParameter("display_mode");
		new SiteHelper().setHtmlMetaElements(request, response, "ID Mapping");

		PortletRequestDispatcher prd;
		if (mode != null && mode.equals("result")) {
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/id_mapping_result.jsp");
		}
		else {
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/id_mapping.jsp");
		}
		prd.include(request, response);
	}

	@SuppressWarnings("unchecked")
	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		String sraction = request.getParameter("sraction");

		if (sraction != null && sraction.equals("save_params")) {

			ResultType key = new ResultType();
			String keyword = request.getParameter("keyword");
			String from = request.getParameter("from");
			String fromGroup = request.getParameter("fromGroup");
			String to = request.getParameter("to");
			String toGroup = request.getParameter("toGroup");

			if (!keyword.equals("")) {
				key.put("keyword", keyword.replaceAll("\n", " OR ").replaceAll(",", " OR "));
			}

			key.put("from", from);
			key.put("to", to);
			key.put("fromGroup", fromGroup);
			key.put("toGroup", toGroup);

			// random
			Random g = new Random();
			int random = g.nextInt();

			PortletSession session = request.getPortletSession(true);
			session.setAttribute("key" + random, key);

			PrintWriter writer = response.getWriter();
			writer.write("" + random);
			writer.close();
		}
		else if (sraction != null && sraction.equals("filters")) {

			this.responseWriteFilters(response);
			// this.responseWriteFiltersStatic(response);
		}
		else {

			String pk = request.getParameter("pk");

			// String start_id = request.getParameter("start");
			// String limit = request.getParameter("limit");
			// int start = Integer.parseInt(start_id);
			// int end = start + Integer.parseInt(limit);
			PortletSession session = request.getPortletSession();
			ResultType key = (ResultType) session.getAttribute("key" + pk);

			// TODO: implement sorting, paging
			// sorting
			//			Map<String, String> sort = null;
			//			if (request.getParameter("sort") != null) {
			//				// sorting
			//				JSONParser a = new JSONParser();
			//				JSONArray sorter;
			//				String sort_field = "";
			//				String sort_dir = "";
			//				try {
			//					sorter = (JSONArray) a.parse(request.getParameter("sort"));
			//					sort_field += ((JSONObject) sorter.get(0)).get("property").toString();
			//					sort_dir += ((JSONObject) sorter.get(0)).get("direction").toString();
			//					for (int i = 1; i < sorter.size(); i++) {
			//						sort_field += "," + ((JSONObject) sorter.get(i)).get("property").toString();
			//					}
			//				}
			//				catch (ParseException e) {
			//					LOGGER.error(e.getMessage(), e);
			//				}
			//
			//				sort = new HashMap<>();
			//
			//				if (!sort_field.equals("") && !sort_dir.equals("")) {
			//					sort.put("field", sort_field);
			//					sort.put("direction", sort_dir);
			//				}
			//
			//			}

			LOGGER.debug("id mapping param: {}", key);

			SolrInterface solr = new SolrInterface();

			JSONArray results = new JSONArray();
			int total;

			if (key.get("fromGroup").equals("PATRIC")) {
				if (key.get("toGroup").equals("PATRIC")) { // from PATRIC to PATRIC

					// query to GenomeFeature
					try {
						SolrQuery query = new SolrQuery(key.get("from") + ":(" + key.get("keyword") + ")");
						query.setRows(10000);

						LOGGER.debug("PATRIC TO PATRIC: {}", query.toString());
						QueryResponse qr = solr.getSolrServer(SolrCore.FEATURE).query(query);
						List<GenomeFeature> featureList = qr.getBeans(GenomeFeature.class);

						for (GenomeFeature feature : featureList) {
							results.add(feature.toJSONObject());
						}
					}
					catch (MalformedURLException | SolrServerException e) {
						LOGGER.error(e.getMessage(), e);
					}

					total = results.size();
				}
				else { // from PATRIC to Other
					SolrDocumentList idMap = null;
					try {
						SolrQuery query = new SolrQuery();
						query.setQuery("id_type:" + key.get("to"));
						query.addFilterQuery(SolrCore.FEATURE
								.getSolrCoreJoin("uniprotkb_accession", "uniprotkb_accession", key.get("from") + ":(" + key.get("keyword") + ")"));

						LOGGER.debug("PATRIC TO Other 1/2: {}", query.toString());
						QueryResponse qr = solr.getSolrServer(SolrCore.ID_REF).query(query);
						idMap = qr.getResults();
					}
					catch (MalformedURLException | SolrServerException e) {
						LOGGER.error(e.getMessage(), e);
					}

					// query to GenomeFeature
					try {
						SolrQuery query = new SolrQuery(key.get("from") + ":(" + key.get("keyword") + ")");
						query.setRows(10000);

						LOGGER.debug("PATRIC TO Other 2/2: {}", query.toString());
						QueryResponse qr = solr.getSolrServer(SolrCore.FEATURE).query(query);
						List<GenomeFeature> featureList = qr.getBeans(GenomeFeature.class);

						for (GenomeFeature feature : featureList) {
							for (String uniprotkbAccession : feature.getUniprotkbAccession()) {
								assert idMap != null;
								for (SolrDocument doc : idMap) {
									if (doc.get("uniprotkb_accession").equals(uniprotkbAccession)) {
										JSONObject item = feature.toJSONObject();
										item.put("target", doc.get("id_value"));

										results.add(item);
									}
								}
							}
						}
					}
					catch (MalformedURLException | SolrServerException e) {
						LOGGER.error(e.getMessage(), e);
					}

					total = results.size();
				}
			}
			else { // from Other to PATRIC (seed_id)
				SolrDocumentList idMap = null;
				try {
					SolrQuery query = new SolrQuery("id_type:" + key.get("from") + " AND id_value:(" + key.get("keyword") + ")");
					query.setRows(10000).addField("uniprotkb_accession,id_value");
					query.addFilterQuery(SolrCore.FEATURE.getSolrCoreJoin("uniprotkb_accession", "uniprotkb_accession", "*:*"));

					LOGGER.debug("Other to PATRIC 1/2: {}", query.toString());
					QueryResponse qr = solr.getSolrServer(SolrCore.ID_REF).query(query);
					idMap = qr.getResults();
				}
				catch (MalformedURLException | SolrServerException e) {
					LOGGER.error(e.getMessage(), e);
				}

				try {
					SolrQuery query = new SolrQuery("*:*");
					query.setRows(10000);
					query.addFilterQuery(SolrCore.ID_REF.getSolrCoreJoin("uniprotkb_accession", "uniprotkb_accession",
							"id_type:" + key.get("from") + " AND id_value:(" + key.get("keyword") + ")"));

					LOGGER.debug("PATRIC TO Other 2/2: {}", query.toString());
					QueryResponse qr = solr.getSolrServer(SolrCore.FEATURE).query(query);
					List<GenomeFeature> featureList = qr.getBeans(GenomeFeature.class);

					for (GenomeFeature feature : featureList) {
						for (String uniprotkbAccession : feature.getUniprotkbAccession()) {
							assert idMap != null;
							for (SolrDocument doc : idMap) {
								if (doc.get("uniprotkb_accession").equals(uniprotkbAccession)) {
									JSONObject item = feature.toJSONObject();
									item.put("target", doc.get("id_value"));

									results.add(item);
								}
							}
						}
					}

				}
				catch (MalformedURLException | SolrServerException e) {
					LOGGER.error(e.getMessage(), e);
				}

				total = results.size();
			}

			JSONObject jsonResult = new JSONObject();
			jsonResult.put("total", total);
			jsonResult.put("results", results);

			response.setContentType("application/json");
			jsonResult.writeJSONString(response.getWriter());
		}
	}

	//	private void responseWriteFiltersStatic(ResourceResponse response) throws IOException {
	//
	//		String filter = "{\"id_types\":[{\"id\":\"<h5>PATRIC Identifier<\\/h5>\",\"value\":\"\"},{\"id\":\"PATRIC Locus Tag\",\"value\":\"seed_id\",\"group\":\"PATRIC\"},{\"id\":\"PATRIC ID\",\"value\":\"feature_id\",\"group\":\"PATRIC\"},{\"id\":\"PATRIC2 Locus Tag\",\"value\":\"alt_locus_tag\",\"group\":\"PATRIC\"},{\"id\":\"<h5>RefSeq Identifiers<\\/h5>\",\"value\":\"\"},{\"id\":\"RefSeq\",\"value\":\"protein_id\",\"group\":\"PATRIC\"},{\"id\":\"RefSeq Locus Tag\",\"value\":\"refseq_locus_tag\",\"group\":\"PATRIC\"},{\"id\":\"Gene ID\",\"value\":\"gene_id\",\"group\":\"PATRIC\"},{\"id\":\"GI\",\"value\":\"gi\",\"group\":\"PATRIC\"},{\"id\":\"<h5>Other Identifiers<\\/h5>\",\"value\":\"\"},{\"id\":\"Allergome\",\"value\":\"Allergome\",\"group\":\"Other\"},{\"id\":\"BioCyc\",\"value\":\"BioCyc\",\"group\":\"Other\"},{\"id\":\"ChEMBL\",\"value\":\"ChEMBL\",\"group\":\"Other\"},{\"id\":\"DIP\",\"value\":\"DIP\",\"group\":\"Other\"},{\"id\":\"DNASU\",\"value\":\"DNASU\",\"group\":\"Other\"},{\"id\":\"DisProt\",\"value\":\"DisProt\",\"group\":\"Other\"},{\"id\":\"DrugBank\",\"value\":\"DrugBank\",\"group\":\"Other\"},{\"id\":\"EMBL\",\"value\":\"EMBL\",\"group\":\"Other\"},{\"id\":\"EMBL-CDS\",\"value\":\"EMBL-CDS\",\"group\":\"Other\"},{\"id\":\"EchoBASE\",\"value\":\"EchoBASE\",\"group\":\"Other\"},{\"id\":\"EcoGene\",\"value\":\"EcoGene\",\"group\":\"Other\"},{\"id\":\"EnsemblGenome\",\"value\":\"EnsemblGenome\",\"group\":\"Other\"},{\"id\":\"GenoList\",\"value\":\"GenoList\",\"group\":\"Other\"},{\"id\":\"HOGENOM\",\"value\":\"HOGENOM\",\"group\":\"Other\"},{\"id\":\"KEGG\",\"value\":\"KEGG\",\"group\":\"Other\"},{\"id\":\"KO\",\"value\":\"KO\",\"group\":\"Other\"},{\"id\":\"LegioList\",\"value\":\"LegioList\",\"group\":\"Other\"},{\"id\":\"Leproma\",\"value\":\"Leproma\",\"group\":\"Other\"},{\"id\":\"MEROPS\",\"value\":\"MEROPS\",\"group\":\"Other\"},{\"id\":\"MINT\",\"value\":\"MINT\",\"group\":\"Other\"},{\"id\":\"NCBI_TaxID\",\"value\":\"NCBI_TaxID\",\"group\":\"Other\"},{\"id\":\"OMA\",\"value\":\"OMA\",\"group\":\"Other\"},{\"id\":\"OrthoDB\",\"value\":\"OrthoDB\",\"group\":\"Other\"},{\"id\":\"PATRIC\",\"value\":\"PATRIC\",\"group\":\"Other\"},{\"id\":\"PDB\",\"value\":\"PDB\",\"group\":\"Other\"},{\"id\":\"PeroxiBase\",\"value\":\"PeroxiBase\",\"group\":\"Other\"},{\"id\":\"PhosSite\",\"value\":\"PhosSite\",\"group\":\"Other\"},{\"id\":\"PptaseDB\",\"value\":\"PptaseDB\",\"group\":\"Other\"},{\"id\":\"ProtClustDB\",\"value\":\"ProtClustDB\",\"group\":\"Other\"},{\"id\":\"PseudoCAP\",\"value\":\"PseudoCAP\",\"group\":\"Other\"},{\"id\":\"REBASE\",\"value\":\"REBASE\",\"group\":\"Other\"},{\"id\":\"Reactome\",\"value\":\"Reactome\",\"group\":\"Other\"},{\"id\":\"RefSeq_NT\",\"value\":\"RefSeq_NT\",\"group\":\"Other\"},{\"id\":\"STRING\",\"value\":\"STRING\",\"group\":\"Other\"},{\"id\":\"TCDB\",\"value\":\"TCDB\",\"group\":\"Other\"},{\"id\":\"TubercuList\",\"value\":\"TubercuList\",\"group\":\"Other\"},{\"id\":\"UniGene\",\"value\":\"UniGene\",\"group\":\"Other\"},{\"id\":\"UniParc\",\"value\":\"UniParc\",\"group\":\"Other\"},{\"id\":\"UniPathway\",\"value\":\"UniPathway\",\"group\":\"Other\"},{\"id\":\"UniProtKB-ID\",\"value\":\"UniProtKB-ID\",\"group\":\"Other\"},{\"id\":\"UniRef100\",\"value\":\"UniRef100\",\"group\":\"Other\"},{\"id\":\"UniRef50\",\"value\":\"UniRef50\",\"group\":\"Other\"},{\"id\":\"UniRef90\",\"value\":\"UniRef90\",\"group\":\"Other\"},{\"id\":\"World-2DPAGE\",\"value\":\"World-2DPAGE\",\"group\":\"Other\"},{\"id\":\"eggNOG\",\"value\":\"eggNOG\",\"group\":\"Other\"}]}";
	//
	//		response.setContentType("application/json");
	//		response.getWriter().write(filter);
	//	}

	@SuppressWarnings("unchecked")
	private void responseWriteFilters(ResourceResponse response) throws IOException {

		final String idGroupPATRIC = "PATRIC";
		final String idGroupOther = "Other";

		JSONObject grpPATRIC = new JSONObject();
		JSONObject grpPATRIC1 = new JSONObject();
		JSONObject grpPATRIC2 = new JSONObject();
		JSONObject grpPATRIC3 = new JSONObject();

		JSONObject grpRefSeq = new JSONObject();
		JSONObject grpRefSeq1 = new JSONObject();
		JSONObject grpRefSeq2 = new JSONObject();
		JSONObject grpRefSeq3 = new JSONObject();
		JSONObject grpRefSeq4 = new JSONObject();

		JSONObject grpOther = new JSONObject();

		// PATRIC Identifiers
		grpPATRIC.put("id", "<h5>PATRIC Identifier</h5>");
		grpPATRIC.put("value", "");

		grpPATRIC1.put("id", "PATRIC Locus Tag");
		grpPATRIC1.put("value", "seed_id");
		grpPATRIC1.put("group", idGroupPATRIC);

		grpPATRIC2.put("id", "PATRIC ID");
		grpPATRIC2.put("value", "feature_id");
		grpPATRIC2.put("group", idGroupPATRIC);

		grpPATRIC3.put("id", "PATRIC2 Locus Tag");
		grpPATRIC3.put("value", "alt_locus_tag");
		grpPATRIC3.put("group", idGroupPATRIC);

		// RefSeq Identifiers
		grpRefSeq.put("id", "<h5>RefSeq Identifiers</h5>");
		grpRefSeq.put("value", "");

		grpRefSeq1.put("id", "RefSeq");
		grpRefSeq1.put("value", "protein_id");
		grpRefSeq1.put("group", idGroupPATRIC);

		grpRefSeq2.put("id", "RefSeq Locus Tag");
		grpRefSeq2.put("value", "refseq_locus_tag");
		grpRefSeq2.put("group", idGroupPATRIC);

		grpRefSeq3.put("id", "Gene ID");
		grpRefSeq3.put("value", "gene_id");
		grpRefSeq3.put("group", idGroupPATRIC);

		grpRefSeq4.put("id", "GI");
		grpRefSeq4.put("value", "gi");
		grpRefSeq4.put("group", idGroupPATRIC);

		// Other Identifiers
		grpOther.put("id", "<h5>Other Identifiers</h5>");
		grpOther.put("value", "");

		JSONArray jsonIdTypes = new JSONArray();
		jsonIdTypes.add(grpPATRIC);
		jsonIdTypes.add(grpPATRIC1);
		jsonIdTypes.add(grpPATRIC2);
		jsonIdTypes.add(grpPATRIC3);

		jsonIdTypes.add(grpRefSeq);
		jsonIdTypes.add(grpRefSeq1);
		jsonIdTypes.add(grpRefSeq2);
		jsonIdTypes.add(grpRefSeq3);
		jsonIdTypes.add(grpRefSeq4);

		jsonIdTypes.add(grpOther);
		List<String> otherTypes = getIdTypes();
		for (String type : otherTypes) {
			JSONObject item = new JSONObject();
			item.put("id", type);
			item.put("value", type);
			item.put("group", idGroupOther);

			jsonIdTypes.add(item);
		}

		JSONObject json = new JSONObject();
		json.put("id_types", jsonIdTypes);

		response.setContentType("application/json");
		json.writeJSONString(response.getWriter());
	}

	private List<String> getIdTypes() {
		List<String> idTypes = new ArrayList<>();

		SolrInterface solr = new SolrInterface();

		try {
			SolrQuery query = new SolrQuery("*:*");
			query.addFacetField("id_type").setFacetLimit(-1);

			QueryResponse qr = solr.getSolrServer(SolrCore.ID_REF).query(query);
			FacetField ffIdType = qr.getFacetField("id_type");

			for (FacetField.Count type : ffIdType.getValues()) {
				if (!type.getName().equals("RefSeq") && !type.getName().equals("GeneID") && !type.getName().equals("GI")) {
					idTypes.add(type.getName());
				}
			}
		}
		catch (MalformedURLException | SolrServerException e) {
			LOGGER.error(e.getMessage(), e);
		}

		return idTypes;
	}

}
