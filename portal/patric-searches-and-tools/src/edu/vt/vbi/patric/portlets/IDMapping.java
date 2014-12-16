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
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
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
import java.util.*;

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

			// this.responseWriteFilters(response);
			this.responseWriteFiltersStatic(response);
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

			JSONObject jsonResult = this
					.processIDMapping(key.get("from"), key.get("fromGroup"), key.get("to"), key.get("toGroup"), key.get("keyword"));

			response.setContentType("application/json");
			jsonResult.writeJSONString(response.getWriter());
		}
	}

	@SuppressWarnings("unchecked")
	private JSONObject processIDMapping(String fromId, String fromIdGroup, String toId, String toIdGroup, String keyword) throws IOException {
		SolrInterface solr = new SolrInterface();

		JSONArray results = new JSONArray();
		int total;

		if (fromIdGroup.equals("PATRIC")) {
			if (toIdGroup.equals("PATRIC")) { // from PATRIC to PATRIC

				// query to GenomeFeature
				try {
					SolrQuery query = new SolrQuery(fromId + ":(" + keyword + ")");
					query.setRows(10000);

					if (toId.equals("gene_id") || toId.equals("gi")) {
						query.addFilterQuery(toId + ":[1 TO *]");
					}

					LOGGER.debug("PATRIC TO PATRIC: {}", query.toString());
					QueryResponse qr = solr.getSolrServer(SolrCore.FEATURE).query(query, SolrRequest.METHOD.POST);
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

				Set<Long> giList = new HashSet<>();
				Map<String, String> accessionGiMap = new LinkedHashMap<>();
				List<Map<Long, String>> giTargetList = new LinkedList<>();
				List<GenomeFeature> featureList = new ArrayList<>();

				// Query GenomeFeature, get GInumbers
				try {
					SolrQuery query = new SolrQuery(fromId + ":(" + keyword + ")");
					query.setRows(10000);
					LOGGER.trace("PATRIC TO Other 1/3: {}", query.toString());

					QueryResponse qr = solr.getSolrServer(SolrCore.FEATURE).query(query, SolrRequest.METHOD.POST);
					featureList = qr.getBeans(GenomeFeature.class);

					for (GenomeFeature feature : featureList) {
						giList.add(feature.getGi());
					}
				}
				catch (MalformedURLException | SolrServerException e) {
					LOGGER.error(e.getMessage(), e);
				}

				// get UniprotKBAccessions wigh GI
				try {
					SolrQuery query = new SolrQuery("id_value:(" + StringUtils.join(giList, " OR ") + ")");
					query.addFilterQuery("id_type:GI").setRows(10000);

					LOGGER.trace("PATRIC TO Other 2/3: {}", query.toString());
					QueryResponse qr = solr.getSolrServer(SolrCore.ID_REF).query(query, SolrRequest.METHOD.POST);
					SolrDocumentList uniprotList = qr.getResults();

					for (SolrDocument doc : uniprotList) {

						accessionGiMap.put(doc.get("uniprotkb_accession").toString(), doc.get("id_value").toString());
					}
				}
				catch (MalformedURLException | SolrServerException e) {
					LOGGER.error(e.getMessage(), e);
				}
				LOGGER.trace("accessionGiMap:{}", accessionGiMap);

				// get Target Value
				try {
					SolrQuery query = new SolrQuery("uniprotkb_accession:(" + StringUtils.join(accessionGiMap.keySet(), " OR ") + ")");
					query.addFilterQuery("id_type:(" + toId + ")").setRows(accessionGiMap.size());

					LOGGER.trace("PATRIC TO Other 3/3: {}", query.toString());
					QueryResponse qr = solr.getSolrServer(SolrCore.ID_REF).query(query, SolrRequest.METHOD.POST);
					SolrDocumentList targets = qr.getResults();

					for (SolrDocument doc : targets) {
						String accession = doc.get("uniprotkb_accession").toString();
						String target = doc.get("id_value").toString();

						Long targetGi = Long.parseLong(accessionGiMap.get(accession));

						Map<Long, String> giTarget = new HashMap<>();
						giTarget.put(targetGi, target);
						giTargetList.add(giTarget);
					}
				}
				catch (MalformedURLException | SolrServerException e) {
					LOGGER.error(e.getMessage(), e);
				}

				LOGGER.trace("giTargetList:{}", giTargetList);
				// query to GenomeFeature
				for (GenomeFeature feature : featureList) {
					for (Map<Long, String> targetMap : giTargetList) {
						if (targetMap.containsKey(feature.getGi())) {
							JSONObject item = feature.toJSONObject();
							item.put("target", targetMap.get(feature.getGi()));

							results.add(item);
						}
					}
				}

				total = results.size();
			}
		}
		else { // from Other to PATRIC (seed_id)

			Map<String, String> accessionTargetMap = new LinkedHashMap<>();
			Set<Long> giList = new HashSet<>();
			List<Map<Long, String>> giTargetList = new LinkedList<>();

			try {
				SolrQuery query = new SolrQuery("id_value:(" + keyword + ")");
				query.addFilterQuery("id_type:" + fromId).setRows(10000).addField("uniprotkb_accession,id_value");

				LOGGER.debug("Other to PATRIC 1/3: {}", query.toString());
				QueryResponse qr = solr.getSolrServer(SolrCore.ID_REF).query(query);
				SolrDocumentList accessions = qr.getResults();

				for (SolrDocument doc : accessions) {
					accessionTargetMap.put(doc.get("uniprotkb_accession").toString(), doc.get("id_value").toString());
				}
			}
			catch (MalformedURLException | SolrServerException e) {
				LOGGER.error(e.getMessage(), e);
			}

			try {
				SolrQuery query = new SolrQuery("uniprotkb_accession:(" + StringUtils.join(accessionTargetMap.keySet(), " OR ") + ")");
				query.addFilterQuery("id_type:GI").setRows(10000);

				LOGGER.debug("Other to PATRIC 2/3: {}", query.toString());
				QueryResponse qr = solr.getSolrServer(SolrCore.ID_REF).query(query);
				SolrDocumentList accessions = qr.getResults();

				for (SolrDocument doc : accessions) {
					Long targetGi = Long.parseLong(doc.get("id_value").toString());
					String accession = doc.get("uniprotkb_accession").toString();
					String target = accessionTargetMap.get(accession);

					giList.add(targetGi);

					Map<Long, String> targetMap = new HashMap<>();
					targetMap.put(targetGi, target);
					giTargetList.add(targetMap);
				}
			}
			catch (MalformedURLException | SolrServerException e) {
				LOGGER.error(e.getMessage(), e);
			}

			LOGGER.debug("giTargetList:{}", giTargetList);
			try {
				SolrQuery query = new SolrQuery("gi:(" + StringUtils.join(giList, " OR ") + ")");
				query.setRows(10000);

				LOGGER.debug("Other to PATRIC 3/3: {}", query.toString());
				QueryResponse qr = solr.getSolrServer(SolrCore.FEATURE).query(query);
				List<GenomeFeature> featureList = qr.getBeans(GenomeFeature.class);

				for (GenomeFeature feature : featureList) {
					for (Map<Long, String> targetMap : giTargetList) {
						if (targetMap.containsKey(feature.getGi())) {
							JSONObject item = feature.toJSONObject();
							item.put("target", targetMap.get(feature.getGi()));

							results.add(item);
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

		return jsonResult;
	}

	private void responseWriteFiltersStatic(ResourceResponse response) throws IOException {

		String filter = "{\"id_types\":[{\"id\":\"<h5>PATRIC Identifier<\\/h5>\",\"value\":\"\"},{\"id\":\"PATRIC ID\",\"value\":\"seed_id\",\"group\":\"PATRIC\"},{\"id\":\"Feature ID\",\"value\":\"feature_id\",\"group\":\"PATRIC\"},{\"id\":\"Alt Locus Tag\",\"value\":\"alt_locus_tag\",\"group\":\"PATRIC\"},{\"id\":\"<h5>RefSeq Identifiers<\\/h5>\",\"value\":\"\"},{\"id\":\"RefSeq\",\"value\":\"protein_id\",\"group\":\"PATRIC\"},{\"id\":\"RefSeq Locus Tag\",\"value\":\"refseq_locus_tag\",\"group\":\"PATRIC\"},{\"id\":\"Gene ID\",\"value\":\"gene_id\",\"group\":\"PATRIC\"},{\"id\":\"GI\",\"value\":\"gi\",\"group\":\"PATRIC\"},{\"id\":\"<h5>Other Identifiers<\\/h5>\",\"value\":\"\"},{\"id\":\"Allergome\",\"value\":\"Allergome\",\"group\":\"Other\"},{\"id\":\"BioCyc\",\"value\":\"BioCyc\",\"group\":\"Other\"},{\"id\":\"ChEMBL\",\"value\":\"ChEMBL\",\"group\":\"Other\"},{\"id\":\"DIP\",\"value\":\"DIP\",\"group\":\"Other\"},{\"id\":\"DNASU\",\"value\":\"DNASU\",\"group\":\"Other\"},{\"id\":\"DisProt\",\"value\":\"DisProt\",\"group\":\"Other\"},{\"id\":\"DrugBank\",\"value\":\"DrugBank\",\"group\":\"Other\"},{\"id\":\"EMBL\",\"value\":\"EMBL\",\"group\":\"Other\"},{\"id\":\"EMBL-CDS\",\"value\":\"EMBL-CDS\",\"group\":\"Other\"},{\"id\":\"EchoBASE\",\"value\":\"EchoBASE\",\"group\":\"Other\"},{\"id\":\"EcoGene\",\"value\":\"EcoGene\",\"group\":\"Other\"},{\"id\":\"EnsemblGenome\",\"value\":\"EnsemblGenome\",\"group\":\"Other\"},{\"id\":\"GenoList\",\"value\":\"GenoList\",\"group\":\"Other\"},{\"id\":\"HOGENOM\",\"value\":\"HOGENOM\",\"group\":\"Other\"},{\"id\":\"KEGG\",\"value\":\"KEGG\",\"group\":\"Other\"},{\"id\":\"KO\",\"value\":\"KO\",\"group\":\"Other\"},{\"id\":\"LegioList\",\"value\":\"LegioList\",\"group\":\"Other\"},{\"id\":\"Leproma\",\"value\":\"Leproma\",\"group\":\"Other\"},{\"id\":\"MEROPS\",\"value\":\"MEROPS\",\"group\":\"Other\"},{\"id\":\"MINT\",\"value\":\"MINT\",\"group\":\"Other\"},{\"id\":\"NCBI_TaxID\",\"value\":\"NCBI_TaxID\",\"group\":\"Other\"},{\"id\":\"OMA\",\"value\":\"OMA\",\"group\":\"Other\"},{\"id\":\"OrthoDB\",\"value\":\"OrthoDB\",\"group\":\"Other\"},{\"id\":\"PATRIC\",\"value\":\"PATRIC\",\"group\":\"Other\"},{\"id\":\"PDB\",\"value\":\"PDB\",\"group\":\"Other\"},{\"id\":\"PeroxiBase\",\"value\":\"PeroxiBase\",\"group\":\"Other\"},{\"id\":\"PhosSite\",\"value\":\"PhosSite\",\"group\":\"Other\"},{\"id\":\"PptaseDB\",\"value\":\"PptaseDB\",\"group\":\"Other\"},{\"id\":\"ProtClustDB\",\"value\":\"ProtClustDB\",\"group\":\"Other\"},{\"id\":\"PseudoCAP\",\"value\":\"PseudoCAP\",\"group\":\"Other\"},{\"id\":\"REBASE\",\"value\":\"REBASE\",\"group\":\"Other\"},{\"id\":\"Reactome\",\"value\":\"Reactome\",\"group\":\"Other\"},{\"id\":\"RefSeq_NT\",\"value\":\"RefSeq_NT\",\"group\":\"Other\"},{\"id\":\"STRING\",\"value\":\"STRING\",\"group\":\"Other\"},{\"id\":\"TCDB\",\"value\":\"TCDB\",\"group\":\"Other\"},{\"id\":\"TubercuList\",\"value\":\"TubercuList\",\"group\":\"Other\"},{\"id\":\"UniGene\",\"value\":\"UniGene\",\"group\":\"Other\"},{\"id\":\"UniParc\",\"value\":\"UniParc\",\"group\":\"Other\"},{\"id\":\"UniPathway\",\"value\":\"UniPathway\",\"group\":\"Other\"},{\"id\":\"UniProtKB-ID\",\"value\":\"UniProtKB-ID\",\"group\":\"Other\"},{\"id\":\"UniRef100\",\"value\":\"UniRef100\",\"group\":\"Other\"},{\"id\":\"UniRef50\",\"value\":\"UniRef50\",\"group\":\"Other\"},{\"id\":\"UniRef90\",\"value\":\"UniRef90\",\"group\":\"Other\"},{\"id\":\"World-2DPAGE\",\"value\":\"World-2DPAGE\",\"group\":\"Other\"},{\"id\":\"eggNOG\",\"value\":\"eggNOG\",\"group\":\"Other\"}]}";

		response.setContentType("application/json");
		response.getWriter().write(filter);
	}

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

		grpPATRIC1.put("id", "PATRIC ID");
		grpPATRIC1.put("value", "seed_id");
		grpPATRIC1.put("group", idGroupPATRIC);

		grpPATRIC2.put("id", "Feature ID");
		grpPATRIC2.put("value", "feature_id");
		grpPATRIC2.put("group", idGroupPATRIC);

		grpPATRIC3.put("id", "Alt Locus Tag");
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
