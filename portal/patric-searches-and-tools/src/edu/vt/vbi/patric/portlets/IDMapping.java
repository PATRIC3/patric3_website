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

import com.google.gson.Gson;
import edu.vt.vbi.patric.beans.GenomeFeature;
import edu.vt.vbi.patric.common.*;
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

		String contextType = request.getParameter("context_type");
		String contextId = request.getParameter("context_id");
		String pk = request.getParameter("param_key");
		Gson gson = new Gson();

		LOGGER.trace("mode:{}, contextType:{}, contextId:{}, paramKey:{}", mode, contextType, contextId, pk);
		PortletRequestDispatcher prd;
		if (mode != null && mode.equals("result")) {

			String to = "", toGroup = "", from = "", fromGroup = "", keyword = "";

			Map<String, String> key = gson.fromJson(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk), Map.class);

			if (key != null) {
				to = key.get("to");
				toGroup = key.get("toGroup");
				from = key.get("from");
				fromGroup = key.get("fromGroup");
				keyword = key.get("keyword");
			}

			request.setAttribute("contextType", contextType);
			request.setAttribute("contextId", contextId);
			request.setAttribute("paramKey", pk);

			request.setAttribute("to", to);
			request.setAttribute("toGroup", toGroup);
			request.setAttribute("from", from);
			request.setAttribute("fromGroup", fromGroup);
			request.setAttribute("keyword", keyword);

			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/id_mapping_result.jsp");
		}
		else {

			String to = request.getParameter("to") == null?"UniProtKB-ID":request.getParameter("to");
			String from = request.getParameter("from") == null?"seed_id":request.getParameter("from");
			String keyword = request.getParameter("id") == null?"":request.getParameter("id");

			if(pk != null){
				Map<String, String> key = gson.fromJson(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk), Map.class);

				if(key != null){
					to = key.get("to");
					from = key.get("from");
					keyword = key.get("keyword");
				}
			}

			boolean isLoggedInd = Downloads.isLoggedIn(request);
			request.setAttribute("isLoggedIn", isLoggedInd);
			request.setAttribute("contextType", contextType);
			request.setAttribute("contextId", contextId);
			request.setAttribute("paramKey", pk);

			request.setAttribute("to", to);
			request.setAttribute("from", from);
			request.setAttribute("keyword", keyword);

			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/id_mapping.jsp");
		}
		prd.include(request, response);
	}

	@SuppressWarnings("unchecked")
	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		String sraction = request.getParameter("sraction");
		Gson gson = new Gson();

		if (sraction != null && sraction.equals("save_params")) {

			Map<String, String> key = new HashMap<>();
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
			long pk = (new Random()).nextLong();

			SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, gson.toJson(key, Map.class));

			PrintWriter writer = response.getWriter();
			writer.write("" + pk);
			writer.close();
		}
		else if (sraction != null && sraction.equals("filters")) {
			// this.responseWriteFilters(response);
			this.responseWriteFiltersStatic(response);
		}
		else if (sraction != null && sraction.equals("download")) {
			processDownload(request, response);
		}
		else {
			String pk = request.getParameter("pk");

			Map<String, String> key = gson.fromJson(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk), Map.class);

			LOGGER.debug("id mapping param: {}", key);

			JSONObject jsonResult = processIDMapping(key.get("from"), key.get("fromGroup"), key.get("to"), key.get("toGroup"), key.get("keyword"));

			response.setContentType("application/json");
			jsonResult.writeJSONString(response.getWriter());
		}
	}

	private void processDownload(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		String paramFrom = request.getParameter("from");
		String paramFromGroup = request.getParameter("fromGroup");
		String paramTo = request.getParameter("to");
		String paramToGroup = request.getParameter("toGroup");
		String paramKeyword = request.getParameter("keyword");

		String _header, _field;

		switch (paramTo) {
		case "refseq_locus_tag":
			_header = "RefSeq Locus Tag";
			_field = paramTo;
			break;
		case "protein_id":
			_header = "Protein ID";
			_field = paramTo;
			break;
		case "gene_id":
			_header = "Gene ID";
			_field = paramTo;
			break;
		case "gi":
			_header = "GI";
			_field = paramTo;
			break;
		case "feature_id":
			_header = "PATRIC ID";
			_field = paramTo;
			break;
		case "alt_locus_tag":
			_header = "Alt Locus Tag";
			_field = paramTo;
			break;
		case "seed_id":
			switch (paramFrom) {
			case "refseq_locus_tag":
				_header = "RefSeq Locus Tag";
				_field = paramFrom;
				break;
			case "protein_id":
				_header = "RefSeq";
				_field = paramFrom;
				break;
			case "gene_id":
				_header = "Gene ID";
				_field = paramFrom;
				break;
			case "gi":
				_header = "GI";
				_field = paramFrom;
				break;
			case "feature_id":
				_header = "Feature ID";
				_field = paramFrom;
				break;
			case "alt_locus_tag":
				_header = "Alt Locus Tag";
				_field = paramFrom;
				break;
			case "seed_id":
				_header = "PATRIC ID";
				_field = paramFrom;
				break;
			default:
				_header = paramFrom;
				_field = "target";
				break;
			}
			break;
		default:
			_header = paramTo;
			_field = "target";
			break;
		}

		JSONArray _tbl_source;
		List<String> _tbl_header = Arrays
				.asList("Genome", "Accession", "PATRIC ID", "RefSeq Locus Tag", "Alt Locus Tag", _header, "Annotation", "Feature Type", "Start",
						"End",
						"Length(NT)", "Strand", "Length (AA)", "Product Description");
		List<String> _tbl_field = Arrays
				.asList("genome_name", "accession", "seed_id", "refseq_locus_tag", "alt_locus_tag", _field, "annotation", "feature_type", "start",
						"end",
						"na_length", "strand", "aa_length", "product");

		String fileFormat = request.getParameter("fileformat");
		String fileName = "IDMapping";

		JSONObject jsonResult = processIDMapping(paramFrom, paramFromGroup, paramTo, paramToGroup, paramKeyword);
		_tbl_source = (JSONArray) jsonResult.get("results");

		ExcelHelper excel = new ExcelHelper("xssf", _tbl_header, _tbl_field, _tbl_source);
		excel.buildSpreadsheet();

		if (fileFormat.equalsIgnoreCase("xlsx")) {

			response.setContentType("application/octetstream");
			response.setProperty("Content-Disposition", "attachment; filename=\"" + fileName + "." + fileFormat + "\"");

			excel.writeSpreadsheettoBrowser(response.getPortletOutputStream());
		}
		else if (fileFormat.equalsIgnoreCase("txt")) {

			response.setContentType("application/octetstream");
			response.setProperty("Content-Disposition", "attachment; filename=\"" + fileName + "." + fileFormat + "\"");

			response.getWriter().write(excel.writeToTextFile());
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

					LOGGER.trace("PATRIC TO PATRIC: {}", query.toString());
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

				LOGGER.trace("Other to PATRIC 1/3: {}", query.toString());
				QueryResponse qr = solr.getSolrServer(SolrCore.ID_REF).query(query, SolrRequest.METHOD.POST);
				SolrDocumentList accessions = qr.getResults();

				for (SolrDocument doc : accessions) {
					accessionTargetMap.put(doc.get("uniprotkb_accession").toString(), doc.get("id_value").toString());
				}
			}
			catch (MalformedURLException | SolrServerException e) {
				LOGGER.error(e.getMessage(), e);
			}

			if (!accessionTargetMap.isEmpty()) {
				try {
					SolrQuery query = new SolrQuery("uniprotkb_accession:(" + StringUtils.join(accessionTargetMap.keySet(), " OR ") + ")");
					query.addFilterQuery("id_type:GI").setRows(10000);

					LOGGER.trace("Other to PATRIC 2/3: {}", query.toString());
					QueryResponse qr = solr.getSolrServer(SolrCore.ID_REF).query(query, SolrRequest.METHOD.POST);
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
			}

			LOGGER.trace("giTargetList:{}", giTargetList);
			if (!giList.isEmpty()) {
				try {
					SolrQuery query = new SolrQuery("gi:(" + StringUtils.join(giList, " OR ") + ")");
					query.setRows(10000);

					LOGGER.trace("Other to PATRIC 3/3: {}", query.toString());
					QueryResponse qr = solr.getSolrServer(SolrCore.FEATURE).query(query, SolrRequest.METHOD.POST);
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
			}

			total = results.size();
		}

		JSONObject jsonResult = new JSONObject();
		jsonResult.put("total", total);
		jsonResult.put("results", results);

		return jsonResult;
	}

	private void responseWriteFiltersStatic(ResourceResponse response) throws IOException {

		String filter = "{\"id_types\":[{\"id\":\"<h5>PATRIC Identifier<\\/h5>\",\"value\":\"\"},{\"id\":\"PATRIC ID\",\"value\":\"seed_id\",\"group\":\"PATRIC\"},{\"id\":\"Feature ID\",\"value\":\"feature_id\",\"group\":\"PATRIC\"},{\"id\":\"Alt Locus Tag\",\"value\":\"alt_locus_tag\",\"group\":\"PATRIC\"},{\"id\":\"<h5>RefSeq Identifiers<\\/h5>\",\"value\":\"\"},{\"id\":\"RefSeq\",\"value\":\"protein_id\",\"group\":\"PATRIC\"},{\"id\":\"RefSeq Locus Tag\",\"value\":\"refseq_locus_tag\",\"group\":\"PATRIC\"},{\"id\":\"Gene ID\",\"value\":\"gene_id\",\"group\":\"PATRIC\"},{\"id\":\"GI\",\"value\":\"gi\",\"group\":\"PATRIC\"},{\"id\":\"<h5>Other Identifiers<\\/h5>\",\"value\":\"\"},{\"id\":\"Allergome\",\"value\":\"Allergome\",\"group\":\"Other\"},{\"id\":\"BioCyc\",\"value\":\"BioCyc\",\"group\":\"Other\"},{\"id\":\"ChEMBL\",\"value\":\"ChEMBL\",\"group\":\"Other\"},{\"id\":\"DIP\",\"value\":\"DIP\",\"group\":\"Other\"},{\"id\":\"DNASU\",\"value\":\"DNASU\",\"group\":\"Other\"},{\"id\":\"DisProt\",\"value\":\"DisProt\",\"group\":\"Other\"},{\"id\":\"DrugBank\",\"value\":\"DrugBank\",\"group\":\"Other\"},{\"id\":\"EMBL\",\"value\":\"EMBL\",\"group\":\"Other\"},{\"id\":\"EMBL-CDS\",\"value\":\"EMBL-CDS\",\"group\":\"Other\"},{\"id\":\"EchoBASE\",\"value\":\"EchoBASE\",\"group\":\"Other\"},{\"id\":\"EcoGene\",\"value\":\"EcoGene\",\"group\":\"Other\"},{\"id\":\"EnsemblGenome\",\"value\":\"EnsemblGenome\",\"group\":\"Other\"},{\"id\":\"GenoList\",\"value\":\"GenoList\",\"group\":\"Other\"},{\"id\":\"HOGENOM\",\"value\":\"HOGENOM\",\"group\":\"Other\"},{\"id\":\"KEGG\",\"value\":\"KEGG\",\"group\":\"Other\"},{\"id\":\"KO\",\"value\":\"KO\",\"group\":\"Other\"},{\"id\":\"LegioList\",\"value\":\"LegioList\",\"group\":\"Other\"},{\"id\":\"Leproma\",\"value\":\"Leproma\",\"group\":\"Other\"},{\"id\":\"MEROPS\",\"value\":\"MEROPS\",\"group\":\"Other\"},{\"id\":\"MINT\",\"value\":\"MINT\",\"group\":\"Other\"},{\"id\":\"NCBI_TaxID\",\"value\":\"NCBI_TaxID\",\"group\":\"Other\"},{\"id\":\"OMA\",\"value\":\"OMA\",\"group\":\"Other\"},{\"id\":\"OrthoDB\",\"value\":\"OrthoDB\",\"group\":\"Other\"},{\"id\":\"PATRIC\",\"value\":\"PATRIC\",\"group\":\"Other\"},{\"id\":\"PDB\",\"value\":\"PDB\",\"group\":\"Other\"},{\"id\":\"PeroxiBase\",\"value\":\"PeroxiBase\",\"group\":\"Other\"},{\"id\":\"PhosSite\",\"value\":\"PhosSite\",\"group\":\"Other\"},{\"id\":\"PptaseDB\",\"value\":\"PptaseDB\",\"group\":\"Other\"},{\"id\":\"ProtClustDB\",\"value\":\"ProtClustDB\",\"group\":\"Other\"},{\"id\":\"PseudoCAP\",\"value\":\"PseudoCAP\",\"group\":\"Other\"},{\"id\":\"REBASE\",\"value\":\"REBASE\",\"group\":\"Other\"},{\"id\":\"Reactome\",\"value\":\"Reactome\",\"group\":\"Other\"},{\"id\":\"RefSeq_NT\",\"value\":\"RefSeq_NT\",\"group\":\"Other\"},{\"id\":\"STRING\",\"value\":\"STRING\",\"group\":\"Other\"},{\"id\":\"TCDB\",\"value\":\"TCDB\",\"group\":\"Other\"},{\"id\":\"TubercuList\",\"value\":\"TubercuList\",\"group\":\"Other\"},{\"id\":\"UniGene\",\"value\":\"UniGene\",\"group\":\"Other\"},{\"id\":\"UniParc\",\"value\":\"UniParc\",\"group\":\"Other\"},{\"id\":\"UniPathway\",\"value\":\"UniPathway\",\"group\":\"Other\"},{\"id\":\"UniProtKB-Accession\",\"value\":\"uniprotkb_accession\",\"group\":\"PATRIC\"},{\"id\":\"UniProtKB-ID\",\"value\":\"UniProtKB-ID\",\"group\":\"Other\"},{\"id\":\"UniRef100\",\"value\":\"UniRef100\",\"group\":\"Other\"},{\"id\":\"UniRef50\",\"value\":\"UniRef50\",\"group\":\"Other\"},{\"id\":\"UniRef90\",\"value\":\"UniRef90\",\"group\":\"Other\"},{\"id\":\"World-2DPAGE\",\"value\":\"World-2DPAGE\",\"group\":\"Other\"},{\"id\":\"eggNOG\",\"value\":\"eggNOG\",\"group\":\"Other\"}]}";

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
		// add UniProtKB-Accession, for easier processing, treat UniProtKB-Accession as a PATRIC attribute
		JSONObject item = new JSONObject();
		item.put("id", "UniProtKB-ID");
		item.put("value", "UniProtKB-ID");
		item.put("group", idGroupOther);
		int idx = jsonIdTypes.indexOf(item);
		item.put("id", "UniProtKB-Accession");
		item.put("value", "uniprotkb_accession");
		item.put("group", idGroupPATRIC);
		jsonIdTypes.add(idx + 1, item);

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
