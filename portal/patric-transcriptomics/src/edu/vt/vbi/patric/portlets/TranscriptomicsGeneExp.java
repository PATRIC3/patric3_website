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
import edu.vt.vbi.patric.common.ExcelHelper;
import edu.vt.vbi.patric.common.SiteHelper;
import edu.vt.vbi.patric.common.SolrCore;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class TranscriptomicsGeneExp extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(TranscriptomicsGeneExp.class);

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
		response.setTitle("Transcriptomics Feature");

		SiteHelper.setHtmlMetaElements(request, response, "Transcriptomics Feature");

		PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/GeneExpression.jsp");
		prd.include(request, response);
	}

	@SuppressWarnings("unchecked")
	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		response.setContentType("application/json");

		String storeType = request.getParameter("storeType");

		switch (storeType) {
		case "summary": {
			JSONObject jsonResult = this.processSummary(request);

			PrintWriter writer = response.getWriter();
			jsonResult.writeJSONString(writer);
			writer.close();

			break;
		}
		case "summary_download": {

			List<String> tableHeader = new ArrayList<>();
			List<String> tableField = new ArrayList<>();
//			JSONArray tableSource = new JSONArray();

			String fileName = "GeneExpression";
			String fileFormat = request.getParameter("fileformat");

			JSONObject jsonResult = this.processSummary(request);
			JSONArray tableSource = (JSONArray) jsonResult.get("features");

			tableHeader.addAll(Arrays.asList("Platform", "Samples", "Locus Tag", "Title", "PubMed", "Accession", "Strain", "Gene Modification",
					"Experimental Condition", "Time Point", "Avg Intensity", "Log Ratio", "Z-score"));
			tableField.addAll(Arrays.asList("exp_platform", "exp_samples", "exp_locustag", "exp_name", "pmid", "exp_accession", "exp_strain",
					"exp_mutant", "exp_condition", "exp_timepoint", "exp_pavg", "exp_pratio", "exp_zscore"));

			ExcelHelper excel = new ExcelHelper("xssf", tableHeader, tableField, tableSource);
			excel.buildSpreadsheet();

			if (fileFormat.equalsIgnoreCase("xlsx")) {
				response.setContentType("application/octetstream");
				response.addProperty("Content-Disposition", "attachment; filename=\"" + fileName + "." + fileFormat + "\"");

				excel.writeSpreadsheettoBrowser(response.getPortletOutputStream());
			}
			else if (fileFormat.equalsIgnoreCase("txt")) {

				response.setContentType("application/octetstream");
				response.addProperty("Content-Disposition", "attachment; filename=\"" + fileName + "." + fileFormat + "\"");

				response.getPortletOutputStream().write(excel.writeToTextFile().getBytes());
			}
			break;
		}
		case "correlation": {

			JSONObject jsonResult = this.processCorrelation(request);

			PrintWriter writer = response.getWriter();
			jsonResult.writeJSONString(writer);
			writer.close();
			break;
		}
		case "correlation_download": {

			List<String> tableHeader = new ArrayList<>();
			List<String> tableField = new ArrayList<>();
//			JSONArray tableSource = new JSONArray();

			String fileName = "Correlated Genes";
			String fileFormat = request.getParameter("fileformat");

			JSONObject jsonResult = this.processCorrelation(request);
			JSONArray tableSource = (JSONArray) jsonResult.get("results");

			tableHeader.addAll(
					Arrays.asList("Genome Name", "Accession", "PATRIC ID", "Alt Locus Tag", "RefSeq Locus Tag", "Gene Symbol", "Annotation",
							"Feature Type", "Start", "End", "Length(NT)", "Strand", "Protein ID", "Length(AA)", "Product Description", "Correlations",
							"Comparisons"));
			tableField.addAll(Arrays.asList("genome_name", "accession", "patric_id", "alt_locus_tag", "refseq_locus_tag", "gene", "annotation", "feature_type",
					"start", "end", "na_length", "strand", "protein_id", "aa_length", "product", "correlation", "count"));

			ExcelHelper excel = new ExcelHelper("xssf", tableHeader, tableField, tableSource);
			excel.buildSpreadsheet();

			if (fileFormat.equalsIgnoreCase("xlsx")) {
				response.setContentType("application/octetstream");
				response.addProperty("Content-Disposition", "attachment; filename=\"" + fileName + "." + fileFormat + "\"");

				excel.writeSpreadsheettoBrowser(response.getPortletOutputStream());
			}
			else if (fileFormat.equalsIgnoreCase("txt")) {

				response.setContentType("application/octetstream");
				response.addProperty("Content-Disposition", "attachment; filename=\"" + fileName + "." + fileFormat + "\"");

				response.getPortletOutputStream().write(excel.writeToTextFile().getBytes());
			}
			break;
		}
		}
	}

	private JSONObject processSummary(ResourceRequest request) {
		JSONObject jsonResult = new JSONObject();

		String paramFeatureId = request.getParameter("featureId");
		String paramSampleId = request.getParameter("sampleId");
		String paramKeyword = request.getParameter("keyword");
		String paramLogRatio = request.getParameter("log_ratio");
		String paramZScore = request.getParameter("zscore");

		try {
			DataApiHandler dataApi = new DataApiHandler(request);
			//select?q=feature_id:PATRIC.83332.12.NC_000962.CDS.34.1524.fwd&rows=0&facet=true&facet.range.other=before&facet.range.other=after
			// &facet.range.start=-2&facet.range.end=2&facet.range.gap=0.5&facet.range=z_score&facet.range=log_ratio

			//select?q=feature_id:PATRIC.83332.12.NC_000962.CDS.34.1524.fwd&rows=0&facet=true&facet.mincount=1&facet.field=strain&facet.field=mutant&facet.field=condition
			SolrQuery query = new SolrQuery();

			if (paramKeyword != null && !paramKeyword.equals("")) {
				query.setQuery(paramKeyword + " AND feature_id:" + paramFeatureId);
			}
			else {
				query.setQuery("feature_id:" + paramFeatureId);
			}

			if (paramSampleId != null && !paramSampleId.equals("")) {
				String[] pids = paramSampleId.split(",");

				query.addFilterQuery("pid:(" + StringUtils.join(pids, " OR ") + ")");
			}
			if (paramLogRatio != null && !paramLogRatio.equals("") && !paramLogRatio.equals("0")) {
				query.addFilterQuery("log_ratio:[* TO -" + paramLogRatio + "] OR log_ratio:[" + paramLogRatio + " TO *]");
			}
			if (paramZScore != null && !paramZScore.equals("") && !paramZScore.equals("0")) {
				query.addFilterQuery("z_score:[* TO -" + paramZScore + "] OR z_score:[" + paramZScore + " TO *]");
			}

			query.setRows(dataApi.MAX_ROWS);
			query.setFacet(true);
			query.set("facet.range.other", "before");
			query.set("facet.range.other", "after");
			query.addNumericRangeFacet("log_ratio", -2, 2, 0.5);
			query.addNumericRangeFacet("z_score", -2, 2, 0.5);
			query.setFacetMinCount(1);
			query.addFacetField("strain");
			query.addFacetField("mutant");
			query.addFacetField("condition");

			LOGGER.debug("[{}] {}", SolrCore.TRANSCRIPTOMICS_GENE.getSolrCoreName(), query.toString());

			String apiResponse = dataApi.solrQuery(SolrCore.TRANSCRIPTOMICS_GENE, query);

			Map resp = jsonReader.readValue(apiResponse);
			Map respBody = (Map) resp.get("response");

			List<Map> sdl = (List<Map>) respBody.get("docs");

			// features
			JSONArray features = new JSONArray();
			for (Map doc : sdl) {
				JSONObject feature = new JSONObject();
				feature.put("exp_accession", doc.get("accession"));
				// feature.put("exp_channels", doc.get(""));
				feature.put("exp_condition", doc.get("condition"));
				feature.put("exp_id", doc.get("eid"));
				feature.put("exp_locustag", doc.get("refseq_locus_tag"));
				feature.put("exp_mutant", doc.get("mutant"));
				feature.put("exp_name", doc.get("expname"));
				feature.put("exp_organism", doc.get("organism"));
				feature.put("exp_pavg", doc.get("avg_intensity"));
				feature.put("exp_platform", doc.get("")); // ??
				feature.put("exp_pratio", doc.get("log_ratio"));
				feature.put("exp_samples", doc.get("")); // ??
				feature.put("exp_strain", doc.get("")); // ??
				feature.put("exp_timepoint", doc.get("timepoint"));
				feature.put("exp_zscore", doc.get("z_score"));
				// feature.put("figfam_id", doc.get("")); // ??
				feature.put("locus_tag", doc.get("alt_locus_tag"));
				feature.put("feature_id", doc.get("feature_id"));
				feature.put("pid", doc.get("pid"));
				feature.put("pmid", doc.get("pmid"));

				features.add(feature);
			}
			jsonResult.put("features", features);

			Map facets = (Map) resp.get("facet_counts");

			Map facetRanges = (Map) facets.get("facet_ranges");

			if (facetRanges.containsKey("log_ratio")) {
				List facetLogRatio = (List) ((Map) facetRanges.get("log_ratio")).get("counts");
				List<JSONObject> list = new ArrayList<>();
				for (int i = 0; i < facetLogRatio.size(); i = i + 2) {
					JSONObject json = new JSONObject();
					json.put("category", facetLogRatio.get(i));
					json.put("count", facetLogRatio.get(i+1));

					list.add(json);
				}
				jsonResult.put("log_ratio", list);
			}

			if (facetRanges.containsKey("z_score")) {
				List facetZscore = (List) ((Map) facetRanges.get("z_score")).get("counts");
				List<JSONObject> list = new ArrayList<>();
				for (int i = 0; i < facetZscore.size(); i = i + 2) {
					JSONObject json = new JSONObject();
					json.put("category", facetZscore.get(i));
					json.put("count", facetZscore.get(i+1));

					list.add(json);
				}
				jsonResult.put("z_score", list);
			}

			Map facetFields = (Map) facets.get("facet_fields");

			// strain
			if (facetFields.containsKey("strain")) {
				List facetStrain = (List) facetFields.get("strain");
				List<JSONObject> list = new ArrayList<>();
				for (int i = 0; i < facetStrain.size(); i = i + 2) {
					JSONObject json = new JSONObject();
					json.put("category", facetStrain.get(i));
					json.put("count", facetStrain.get(i + 1));

					list.add(json);
				}
				jsonResult.put("strain", list);
			}

			// mutant
			if (facetFields.containsKey("mutant")) {
				List facetMutant = (List) facetFields.get("mutant");
				List<JSONObject> list = new ArrayList<>();
				for (int i = 0; i < facetMutant.size(); i = i + 2) {
					JSONObject json = new JSONObject();
					json.put("category", facetMutant.get(i));
					json.put("count", facetMutant.get(i + 1));

					list.add(json);
				}
				jsonResult.put("mutant", list);
			}

			// condition
			if (facetFields.containsKey("condition")) {
				List facetCondition = (List) facetFields.get("condition");
				List<JSONObject> list = new ArrayList<>();
				for (int i = 0; i < facetCondition.size(); i = i + 2) {
					JSONObject json = new JSONObject();
					json.put("category", facetCondition.get(i));
					json.put("count", facetCondition.get(i + 1));

					list.add(json);
				}
				jsonResult.put("condition", list);
			}
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

		return jsonResult;
	}

	@SuppressWarnings("unchecked")
	private JSONObject processCorrelation(ResourceRequest request) {

		String featureId = request.getParameter("featureId");
		String cutoffValue = request.getParameter("cutoffValue");
		String cutoffDir = request.getParameter("cutoffDir");

		JSONObject jsonResult = new JSONObject();
		DataApiHandler dataApi = new DataApiHandler(request);

		GenomeFeature feature = dataApi.getFeature(featureId);
		Map<String, Map<String, Object>> correlationMap = new HashMap<>();
		int numFound = 0;

		try {
			SolrQuery query = new SolrQuery("genome_id:" + feature.getGenomeId());
			query.setFilterQueries(
					"{!correlation fieldId=refseq_locus_tag fieldCondition=pid fieldValue=log_ratio srcId=" + feature.getRefseqLocusTag()
							+ " filterCutOff=" + cutoffValue + " filterDir=" + cutoffDir.substring(0, 3) + " cost=101}");
			query.setRows(0).set("json.nl", "map");

			LOGGER.trace("[{}] {}", SolrCore.TRANSCRIPTOMICS_GENE.getSolrCoreName(), query.toString());
			String apiResponse = dataApi.solrQuery(SolrCore.TRANSCRIPTOMICS_GENE, query);

			Map resp = jsonReader.readValue(apiResponse);
			List<Map> transcriptomicsGenes = (List) resp.get("correlation");

			numFound = transcriptomicsGenes.size();

			for (Map doc : transcriptomicsGenes) {
				correlationMap.put(doc.get("id").toString(), doc);
			}
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

		jsonResult.put("total", numFound);
		JSONArray results = new JSONArray();

		if (!correlationMap.isEmpty()) {
			try {
				SolrQuery query = new SolrQuery("refseq_locus_tag:(" + StringUtils.join(correlationMap.keySet(), " OR ") + ")");
				query.setFilterQueries("annotation:PATRIC");
				query.setFields(
						"genome_id,genome_name,accession,feature_id,start,end,strand,feature_type,annotation,alt_locus_tag,refseq_locus_tag,patric_id,na_length,aa_length,protein_id,gene,product");
				query.setRows(numFound);

				LOGGER.trace("[{}] {}", SolrCore.FEATURE.getSolrCoreName(), query.toString());
				String apiResponse = dataApi.solrQuery(SolrCore.FEATURE, query);

				Map resp = jsonReader.readValue(apiResponse);
				Map respBody = (Map) resp.get("response");

				List<GenomeFeature> features = dataApi.bindDocuments((List<Map>) respBody.get("docs"), GenomeFeature.class);

				for (GenomeFeature f : features) {
					JSONObject obj = new JSONObject();
					obj.put("genome_id", f.getGenomeId());
					obj.put("genome_name", f.getGenomeName());
					obj.put("accession", f.getAccession());
					obj.put("feature_id", f.getId());
					obj.put("alt_locus_tag", f.getAltLocusTag());
					obj.put("refseq_locus_tag", f.getRefseqLocusTag());
					obj.put("patric_id", f.getPatricId());
					obj.put("gene", f.getGene());
					obj.put("annotation", f.getAnnotation());
					obj.put("feature_type", f.getFeatureType());
					obj.put("start", f.getStart());
					obj.put("end", f.getEnd());
					obj.put("na_length", f.getNaSequenceLength());
					obj.put("strand", f.getStrand());
					obj.put("protein_id", f.getProteinId());
					obj.put("aa_length", f.getProteinLength());
					obj.put("product", f.getProduct());

					Map<String, Object> corr = correlationMap.get(f.getRefseqLocusTag());
					obj.put("correlation", corr.get("correlation"));
					obj.put("count", corr.get("conditions"));

					results.add(obj);
				}
			}
			catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
		jsonResult.put("results", results);

		return jsonResult;
	}
}
