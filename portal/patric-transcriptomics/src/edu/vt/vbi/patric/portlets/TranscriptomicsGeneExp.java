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
import edu.vt.vbi.patric.dao.DBTranscriptomics;
import edu.vt.vbi.patric.dao.ResultType;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.RangeFacet;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.*;

public class TranscriptomicsGeneExp extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(TranscriptomicsGeneExp.class);

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		response.setContentType("text/html");
		response.setTitle("Transcriptomics Feature");

		new SiteHelper().setHtmlMetaElements(request, response, "Transcriptomics Feature");

		PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/GeneExpression.jsp");
		prd.include(request, response);
	}

	@SuppressWarnings("unchecked")
	public void serveResource(ResourceRequest req, ResourceResponse resp) throws PortletException, IOException {

		resp.setContentType("application/json");

		String paramFeatureId = req.getParameter("featureId");
		String paramStoreType = req.getParameter("storeType");
		String paramSampleId = req.getParameter("sampleId");
		String paramKeyword = req.getParameter("keyword");
		String paramLogRatio = req.getParameter("log_ratio");
		String paramZScore = req.getParameter("zscore");

		String paramStart = req.getParameter("start");
		String paramLimit = req.getParameter("limit");
		String paramSort = req.getParameter("sort");

		SolrInterface solr = new SolrInterface();

//		JSONParser a = new JSONParser();
//		JSONArray sorter = null;
//		String sort_field = "";
//		String sort_dir = "";
//		if (paramSort != null) {
//			try {
//				sorter = (JSONArray) a.parse(paramSort);
//				sort_field += ((JSONObject) sorter.get(0)).get("property").toString();
//				sort_dir += ((JSONObject) sorter.get(0)).get("direction").toString();
//				for (int i = 1; i < sorter.size(); i++) {
//					sort_field += "," + ((JSONObject) sorter.get(i)).get("property").toString();
//				}
//			}
//			catch (ParseException e) {
//				LOGGER.error(e.getMessage(), e);
//			}
//		}

//		HashMap<String, String> key = new HashMap<>();
//		HashMap<String, String> sort = new HashMap<>();
//		DBTranscriptomics conn_transcriptopics = new DBTranscriptomics();
//		if (paramFeatureId != null && !paramFeatureId.equals("")) {
//			key.put("feature_id", paramFeatureId);
//		}
//		if (paramSampleId != null && !paramSampleId.equals("")) {
//			key.put("pid", paramSampleId);
//		}
//		if (paramKeyword != null && !paramKeyword.equals("")) {
//			key.put("keyword", paramKeyword);
//		}
//		if (paramLogRatio != null && !paramLogRatio.equals("") && !paramLogRatio.equals("0")) {
//			key.put("log_ratio", paramLogRatio);
//		}
//		if (paramZScore != null && !paramZScore.equals("") && !paramZScore.equals("0")) {
//			key.put("zscore", paramZScore);
//		}
//		if (sorter != null) {
//			sort.put("field", sort_field);
//			sort.put("direction", sort_dir);
//		}

		JSONObject jsonResult = new JSONObject();


		if (paramStoreType.equals("summary")) {

			try {
				LBHttpSolrServer lbHttpSolrServer = solr.getSolrServer(SolrCore.TRANSCRIPTOMICS_GENE);
				//select?q=feature_id:PATRIC.83332.12.NC_000962.CDS.34.1524.fwd&rows=0&facet=true&facet.range.other=before&facet.range.other=after
				// &facet.range.start=-2&facet.range.end=2&facet.range.gap=0.5&facet.range=z_score&facet.range=log_ratio

				//select?q=feature_id:PATRIC.83332.12.NC_000962.CDS.34.1524.fwd&rows=0&facet=true&facet.mincount=1&facet.field=strain&facet.field=mutant&facet.field=condition
				SolrQuery query = new SolrQuery();

				if (paramKeyword != null && !paramKeyword.equals("")) {
					query.setQuery( paramKeyword + " AND feature_id:" + paramFeatureId);
				} else {
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
				QueryResponse qr = lbHttpSolrServer.query(query);
				long numFound = qr.getResults().getNumFound();

				query.setRows((int)numFound);
				query.setFacet(true);
				query.set("facet.range.other","before");
				query.set("facet.range.other","after");
				query.addNumericRangeFacet("log_ratio", -2, 2, 0.5);
				query.addNumericRangeFacet("z_score", -2, 2, 0.5);
				query.setFacetMinCount(1);
				query.addFacetField("strain");
				query.addFacetField("mutant");
				query.addFacetField("condition");

				qr = lbHttpSolrServer.query(query);

				LOGGER.debug("{}", query.toString());
				// LOGGER.debug("{}", qr.getResponse());

				// features
				JSONArray features = new JSONArray();
				SolrDocumentList sdl = qr.getResults();
				for (SolrDocument doc: sdl) {
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

				// log_ratio, z_score
				RangeFacet ffLogRatio = null;
				RangeFacet ffZScore = null;
				List<RangeFacet> rangeFacets = qr.getFacetRanges();
				for (RangeFacet rangeFacet: rangeFacets) {
					if (rangeFacet.getName().equals("log_ratio")) {
						ffLogRatio = rangeFacet;
					} else {
						ffZScore = rangeFacet;
					}
				}
				if (ffLogRatio != null) {
					List<JSONObject> list = new ArrayList<>();
					List<RangeFacet.Count> counts = ffLogRatio.getCounts();
					for (RangeFacet.Count count: counts) {
						JSONObject json = new JSONObject();
						json.put("category", count.getValue());
						json.put("count", count.getCount());
						list.add(json);
					}
					jsonResult.put("log_ratio", list);
				}
				if (ffZScore != null) {
					List<JSONObject> list = new ArrayList<>();
					List<RangeFacet.Count> counts = ffZScore.getCounts();
					for (RangeFacet.Count count: counts) {
						JSONObject json = new JSONObject();
						json.put("category", count.getValue());
						json.put("count", count.getCount());
						list.add(json);
					}
					jsonResult.put("z_score", list);
				}

				// strain
				FacetField ffStrain = qr.getFacetField("strain");
				List<JSONObject> list = new ArrayList<>();
				for (FacetField.Count facetCount: ffStrain.getValues()) {
					JSONObject json = new JSONObject();
					json.put("category", facetCount.getName());
					json.put("count", facetCount.getCount());
					list.add(json);
				}
				jsonResult.put("strain", list);

				// mutant
				FacetField ffMutant = qr.getFacetField("mutant");
				list = new ArrayList<>();
				for (FacetField.Count facetCount: ffMutant.getValues()) {
					JSONObject json = new JSONObject();
					json.put("category", facetCount.getName());
					json.put("count", facetCount.getCount());
					list.add(json);
				}
				jsonResult.put("mutant", list);

				// condition
				FacetField ffCondition = qr.getFacetField("condition");
				list = new ArrayList<>();
				for (FacetField.Count facetCount: ffCondition.getValues()) {
					JSONObject json = new JSONObject();
					json.put("category", facetCount.getName());
					json.put("count", facetCount.getCount());
					list.add(json);
				}
				jsonResult.put("condition", list);

			} catch (MalformedURLException | SolrServerException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
//		if (paramStoreType.equals("features")) {
//
//			List<ResultType> items = conn_transcriptopics.getGeneLvlExpression(key);
//			try {
//				jsonResult.put("total", items.size());
//				JSONArray results = new JSONArray();
//
//				for (ResultType item : items) {
//					JSONObject obj = new JSONObject();
//					obj.putAll(item);
//					results.add(obj);
//				}
//				jsonResult.put("results", results);
//			}
//			catch (Exception ex) {
//				LOGGER.error(ex.getMessage(), ex);
//			}
//		}
//		else if (paramStoreType.equals("strain") || paramStoreType.equals("mutant") || paramStoreType.equals("condition")) {
//			// meta data fields
//			items = conn_transcriptopics.getGeneLvlExpressionCounts(paramStoreType, key);
//			try {
//				JSONArray results = new JSONArray();
//
//				for (ResultType item : items) {
//					JSONObject obj = new JSONObject();
//					obj.putAll(item);
//					results.add(obj);
//				}
//				jsonResult.put("exp_stat", results);
//			}
//			catch (Exception ex) {
//				LOGGER.error(ex.getMessage(), ex);
//			}
//		}
//		else if (paramStoreType.equals("log_ratio") || paramStoreType.equals("z_score")) {
//			// bar-charts
//			items = conn_transcriptopics.getGeneLvlExpressionHistogram(paramStoreType, key);
//			try {
//				JSONArray results = new JSONArray();
//
//				for (ResultType item : items) {
//					JSONObject obj = new JSONObject();
//					obj.putAll(item);
//					results.add(obj);
//				}
//				jsonResult.put("exp_stat", results);
//			}
//			catch (Exception ex) {
//				LOGGER.error(ex.getMessage(), ex);
//			}
//		}
		else if (paramStoreType.equals("correlation")) {

			String cutoffValue = req.getParameter("cutoffValue");
			String cutoffDir = req.getParameter("cutoffDir");
			// paramFeatureId;

			GenomeFeature feature = solr.getFeature(paramFeatureId);
			Map<String,Map<String, Object>> correlationMap = new HashMap<>();
			long numFound = 0;

			try {
				SolrQuery query = new SolrQuery("genome_id:" + feature.getGenomeId());
				query.setFilterQueries("{!correlation fieldId=refseq_locus_tag fieldCondition=pid fieldValue=log_ratio srcId=" + feature.getRefseqLocusTag() + " filterCutOff=" + cutoffValue + " filterDir=" + cutoffDir.substring(0,3) + " cost=101}");
				query.setRows(0);

				QueryResponse qr = solr.getSolrServer(SolrCore.TRANSCRIPTOMICS_GENE).query(query);

				SolrDocumentList sdl = (SolrDocumentList) qr.getResponse().get("correlation");
				numFound = sdl.getNumFound();

				for (SolrDocument doc: sdl) {
					Map<String, Object> corr = new HashMap<>();
					corr.put("id", doc.get("id"));
					corr.put("correlation", doc.get("correlation"));
					corr.put("conditions", doc.get("conditions"));
					corr.put("p_value", doc.get("p_value"));

					correlationMap.put(doc.get("id").toString(), corr);
				}

			} catch (MalformedURLException | SolrServerException e) {
				LOGGER.error(e.getMessage(), e);
			}

			jsonResult.put("total", numFound);
			JSONArray results = new JSONArray();

			try {
				SolrQuery query = new SolrQuery("refseq_locus_tag:(" + StringUtils.join(correlationMap.keySet(), " OR ") + ")");
				query.setFilterQueries("annotation:PATRIC");
				query.setFields("genome_id,genome_name,accession,feature_id,start,end,strand,feature_type,annotation,alt_locus_tag,refseq_locus_tag,na_length,aa_length,protein_id,gene,product");
				query.setRows((int) numFound);

				QueryResponse qr = solr.getSolrServer(SolrCore.FEATURE).query(query);
				List<GenomeFeature> features = qr.getBeans(GenomeFeature.class);

				for (GenomeFeature f: features) {
					JSONObject obj = new JSONObject();
					obj.put("genome_id", f.getGenomeId());
					obj.put("genome_name", f.getGenomeName());
					obj.put("accession", f.getAccession());
					obj.put("alt_locus_tag", f.getAltLocusTag());
					obj.put("refseq_locus_tag", f.getRefseqLocusTag());
					obj.put("gene", f.getGene());
					obj.put("annotation", f.getAnnotation());
					obj.put("feature_type", f.getFeatureType());
					obj.put("start", f.getStart());
					obj.put("end", f.getEnd());
					obj.put("na_length", f.getNaSequenceLength());
					obj.put("strand", f.getStrand().equals("+")?0:1);
					obj.put("protein_id", f.getProteinId());
					obj.put("aa_length", f.getProteinLength());
					obj.put("product", f.getProduct());

					Map<String, Object> corr = correlationMap.get(f.getRefseqLocusTag());
					obj.put("correlation", corr.get("correlation"));
					obj.put("count", corr.get("conditions"));

					results.add(obj);
				}
				jsonResult.put("results", results);

			} catch (MalformedURLException | SolrServerException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}

		PrintWriter writer = resp.getWriter();
		jsonResult.writeJSONString(writer);
		writer.close();
	}
}
