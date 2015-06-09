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

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import edu.vt.vbi.ci.util.CommandResults;
import edu.vt.vbi.ci.util.ExecUtilities;
import edu.vt.vbi.patric.beans.GenomeFeature;
import edu.vt.vbi.patric.common.*;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.codehaus.jackson.map.ObjectWriter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class TranscriptomicsGene extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(TranscriptomicsGene.class);

	ObjectReader jsonReader;

	ObjectWriter jsonWriter;

	@Override
	public void init() throws PortletException {
		super.init();

		ObjectMapper objectMapper = new ObjectMapper();
		jsonReader = objectMapper.reader(Map.class);
		jsonWriter = objectMapper.writerWithType(Map.class);
	}

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		SiteHelper.setHtmlMetaElements(request, response, "Transcriptomics Gene");

		response.setContentType("text/html");

		String mode = request.getParameter("display_mode");
		PortletRequestDispatcher prd;

		if (mode != null && mode.equals("result")) {

			String contextType = (request.getParameter("context_type") == null) ? "" : request.getParameter("context_type");
			String contextId = (request.getParameter("context_id") == null) ? "" : request.getParameter("context_id");
			String expId = (request.getParameter("expId") == null) ? "" : request.getParameter("expId");
			String sampleId = (request.getParameter("sampleId") == null) ? "" : request.getParameter("sampleId");
			String wsExperimentId = (request.getParameter("wsExperimentId") == null) ? "" : request.getParameter("wsExperimentId");
			String wsSampleId = (request.getParameter("wsSampleId") == null) ? "" : request.getParameter("wsSampleId");

			String log_ratio = request.getParameter("log_ratio");
			if (log_ratio == null || log_ratio.equals("")) {
				log_ratio = "-";
			}

			String zscore = request.getParameter("zscore");
			if (zscore == null || zscore.equals("")) {
				zscore = "-";
			}

			String keyword = "";
			String pk = request.getParameter("param_key");

			if (pk != null && !pk.equals("")) {
				pk = pk.split("/")[0];

				Map<String, String> key = jsonReader.readValue(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk));
				if (key != null && key.get("keyword") != null) {
					keyword = key.get("keyword");
					sampleId = key.get("sampleId");
				}
			}

			request.setAttribute("contextType", contextType);
			request.setAttribute("contextId", contextId);
			request.setAttribute("expId", expId);
			request.setAttribute("sampleId", sampleId);
			request.setAttribute("wsExperimentId", wsExperimentId);
			request.setAttribute("wsSampleId", wsSampleId);
			request.setAttribute("log_ratio", log_ratio);
			request.setAttribute("zscore", zscore);
			request.setAttribute("keyword", keyword);

			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/TranscriptomicsGene.jsp");
		}
		else {
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/tree.jsp");
		}

		prd.include(request, response);

	}

	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
		String callType = request.getParameter("callType");

		if (callType != null) {
			switch (callType) {
			case "saveParams": {

				String keyword = request.getParameter("keyword");
				DataApiHandler dataApi = new DataApiHandler(request);

				Map<String, String> key = new HashMap<>();
				key.put("keyword", "locus_tag:(" + keyword + ") OR refseq_locus_tag:(" + keyword + ") ");
				key.put("fields", "pid");

				SolrQuery query = dataApi.buildSolrQuery(key, null, null, 0, -1, false);

				String apiResponse = dataApi.solrQuery(SolrCore.TRANSCRIPTOMICS_GENE, query);

				Map resp = jsonReader.readValue(apiResponse);
				Map respBody = (Map) resp.get("response");

				List<Map> sdl = (List<Map>) respBody.get("docs");

				Set<String> sampleIds = new HashSet<>();

				for (Map doc : sdl) {
					sampleIds.add(doc.get("pid").toString());
				}

				String sId = StringUtils.join(sampleIds, ",");

				key = new HashMap();
				if (!keyword.equals("")) {
					key.put("keyword", keyword);
				}

				response.setContentType("text/html");
				PrintWriter writer = response.getWriter();

				if (!sId.equals("")) {
					key.put("sampleId", sId);
					long pk = (new Random()).nextLong();

					SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, jsonWriter.writeValueAsString(key));

					writer.write("" + pk);
				}
				else {
					writer.write("");
				}
				writer.close();

				break;
			}
			case "getTables": {

				String expId = request.getParameter("expId");
				String sampleId = request.getParameter("sampleId");
				String wsExperimentId = request.getParameter("wsExperimentId");
				String wsSampleId = request.getParameter("wsSampleId");
				String keyword = request.getParameter("keyword");
				DataApiHandler dataApi = new DataApiHandler(request);

				JSONArray sample = new JSONArray();

				if ((sampleId != null && !sampleId.equals("")) || (expId != null && !expId.equals(""))) {

					String query_keyword = "";

					if (expId != null && !expId.equals("")) {
						query_keyword += "eid:(" + expId.replaceAll(",", " OR ") + ")";
					}

					if (sampleId != null && !sampleId.equals("")) {
						if (query_keyword.length() > 0) {
							query_keyword += " AND ";
						}
						query_keyword += "pid:(" + sampleId.replaceAll(",", " OR ") + ")";
					}

					Map<String, String> key = new HashMap<>();
					key.put("keyword", query_keyword);
					key.put("fields", "pid,expname,expmean,timepoint,mutant,strain,condition");

					SolrQuery query = dataApi.buildSolrQuery(key, null, null, 0, -1, false);

					String apiResponse = dataApi.solrQuery(SolrCore.TRANSCRIPTOMICS_COMPARISON, query);

					Map resp = jsonReader.readValue(apiResponse);
					Map respBody = (Map) resp.get("response");

					List<Map> sdl = (List<Map>) respBody.get("docs");

					for (final Map doc : sdl) {
						final JSONObject item = new JSONObject(doc);
						sample.add(item);
					}
				}

				// Read from JSON if collection parameter is there
				ExpressionDataCollection parser = null;
				if (wsExperimentId != null && !wsExperimentId.equals("")) {
					String token = getAuthorizationToken(request);

					parser = new ExpressionDataCollection(wsExperimentId, token);
					parser.read(ExpressionDataCollection.CONTENT_SAMPLE);
					if (wsSampleId != null && !wsSampleId.equals("")) {
						parser.filter(wsSampleId, ExpressionDataCollection.CONTENT_SAMPLE);
					}
					// Append samples from collection to samples from DB
					sample = parser.append(sample, ExpressionDataCollection.CONTENT_SAMPLE);
				}

				String sampleList = "";
				sampleList += ((JSONObject) sample.get(0)).get("pid");

				for (int i = 1; i < sample.size(); i++) {
					sampleList += "," + ((JSONObject) sample.get(i)).get("pid");
				}

				JSONObject jsonResult = new JSONObject();
				jsonResult.put(ExpressionDataCollection.CONTENT_SAMPLE + "Total", sample.size());
				jsonResult.put(ExpressionDataCollection.CONTENT_SAMPLE, sample);
				JSONArray expression = new JSONArray();

				if ((sampleId != null && !sampleId.equals("")) || (expId != null && !expId.equals(""))) {

					String query_keyword = "";

					if (keyword != null && !keyword.equals("")) {
						query_keyword += "(alt_locus_tag:(" + keyword + ") OR refseq_locus_tag:(" + keyword + ")) ";
					}

					if (expId != null && !expId.equals("")) {
						if (query_keyword.length() > 0) {
							query_keyword += " AND ";
						}
						query_keyword += "eid:(" + expId.replaceAll(",", " OR ") + ")";
					}

					if (sampleId != null && !sampleId.equals("")) {
						if (query_keyword.length() > 0) {
							query_keyword += " AND ";
						}
						query_keyword += "pid:(" + sampleId.replaceAll(",", " OR ") + ")";
					}

					Map<String, String> key = new HashMap<>();
					key.put("keyword", query_keyword);
					key.put("fields", "pid,refseq_locus_tag,feature_id,log_ratio,z_score");

					SolrQuery query = dataApi.buildSolrQuery(key, null, null, 0, -1, false);

					LOGGER.trace("getTables: [{}] {}", SolrCore.TRANSCRIPTOMICS_GENE.getSolrCoreName(), query);

					String apiResponse = dataApi.solrQuery(SolrCore.TRANSCRIPTOMICS_GENE, query);

					Map resp = jsonReader.readValue(apiResponse);
					Map respBody = (Map) resp.get("response");

					List<Map> sdl = (List<Map>) respBody.get("docs");

					for (final Map doc : sdl) {
						final JSONObject item = new JSONObject(doc);
						expression.add(item);
					}

					// TODO: re-implement when data API removes limit 25k records
					int start = 0;
					int fetchedSize = sdl.size();
					while (fetchedSize == 25000) {
						start += 25000;
						query.setStart(start);

						LOGGER.trace("getTables: [{}] {}", SolrCore.TRANSCRIPTOMICS_GENE.getSolrCoreName(), query);

						final String apiResponseSub = dataApi.solrQuery(SolrCore.TRANSCRIPTOMICS_GENE, query);
						final Map respSub = jsonReader.readValue(apiResponseSub);
						final Map respBodySub = (Map) respSub.get("response");

						sdl = (List<Map>) respBodySub.get("docs");
						fetchedSize = sdl.size();

						for (final Map doc : sdl) {
							final JSONObject item = new JSONObject(doc);
							expression.add(item);
						}
					}
				}

				if (wsExperimentId != null && !wsExperimentId.equals("")) {

					parser.read(ExpressionDataCollection.CONTENT_EXPRESSION);
					if (wsSampleId != null && !wsSampleId.equals(""))
						parser.filter(wsSampleId, ExpressionDataCollection.CONTENT_EXPRESSION);

					// Append expression from collection to expression from DB
					expression = parser.append(expression, ExpressionDataCollection.CONTENT_EXPRESSION);
				}

				JSONArray stats = getExperimentStats(dataApi, expression, sampleList, sample);
				jsonResult.put(ExpressionDataCollection.CONTENT_EXPRESSION + "Total", stats.size());
				jsonResult.put(ExpressionDataCollection.CONTENT_EXPRESSION, stats);

				response.setContentType("application/json");
				PrintWriter writer = response.getWriter();
				jsonResult.writeJSONString(writer);
				writer.close();

				break;
			}
			case "doClustering": {

				String data = request.getParameter("data");
				String g = request.getParameter("g");
				String e = request.getParameter("e");
				String m = request.getParameter("m");
				String ge = request.getParameter("ge");
				String pk = request.getParameter("pk");
				String action = request.getParameter("action");

				String folder = "/tmp/";
				String filename = folder + "tmp_" + pk + ".txt";
				String output_filename = folder + "cluster_tmp_" + pk;
				try {

					PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
					out.write(data);
					out.close();

				}
				catch (Exception es) {
					LOGGER.error(es.getMessage(), es);
				}

				response.setContentType("text/html");
				PrintWriter writer = response.getWriter();
				if (action.equals("Run"))
					writer.write(doCLustering(filename, output_filename, g, e, m, ge).toString());

				writer.close();

				break;
			}
			case "saveState": {

				String keyType = request.getParameter("keyType");
				String pageAt = request.getParameter("pageAt");
				String sampleFilter = request.getParameter("sampleFilter");
				String regex = request.getParameter("regex");
				String regexGN = request.getParameter("regexGN");
				String upFold = request.getParameter("upFold");
				String downFold = request.getParameter("downFold");
				String upZscore = request.getParameter("upZscore");
				String downZscore = request.getParameter("downZscore");
				String significantGenes = request.getParameter("significantGenes");
				String ClusterColumnOrder = request.getParameter("ClusterColumnOrder");
				String ClusterRowOrder = request.getParameter("ClusterRowOrder");
				String heatmapState = request.getParameter("heatmapState");
				String heatmapAxis = request.getParameter("heatmapAxis");
				String colorScheme = request.getParameter("colorScheme");
				String filterOffset = request.getParameter("filterOffset");

				Map<String, String> key = new HashMap<>();
				key.put("sampleFilter", (sampleFilter == null) ? "" : sampleFilter);
				key.put("pageAt", (pageAt == null) ? "" : pageAt);
				key.put("regex", (regex == null) ? "" : regex);
				key.put("regexGN", (regexGN == null) ? "" : regexGN);
				key.put("upFold", (upFold == null) ? "" : upFold);
				key.put("downFold", (downFold == null) ? "" : downFold);
				key.put("upZscore", (upZscore == null) ? "" : upZscore);
				key.put("downZscore", (downZscore == null) ? "" : downZscore);
				key.put("significantGenes", (significantGenes == null) ? "" : significantGenes);
				key.put("ClusterRowOrder", (ClusterRowOrder == null) ? "" : ClusterRowOrder);
				key.put("ClusterColumnOrder", (ClusterColumnOrder == null) ? "" : ClusterColumnOrder);
				key.put("heatmapState", (heatmapState == null) ? "" : heatmapState);
				key.put("heatmapAxis", (heatmapAxis == null) ? "" : heatmapAxis);
				key.put("colorScheme", (colorScheme == null) ? "" : colorScheme);
				key.put("filterOffset", (filterOffset == null) ? "" : filterOffset);

				long pk = (new Random()).nextLong();
				SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, jsonWriter.writeValueAsString(key));

				response.setContentType("text/html");
				PrintWriter writer = response.getWriter();
				writer.write("" + pk);
				writer.close();

				break;
			}
			case "getState": {

				String keyType = request.getParameter("keyType");
				String pk = request.getParameter("random");

				if ((pk != null) && (keyType != null)) {
					JSONArray results = new JSONArray();
					JSONObject a = new JSONObject();
					Map<String, String> key = jsonReader.readValue(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk));
					if (key != null) {
						a.put("sampleFilter", key.get("sampleFilter"));
						a.put("pageAt", key.get("pageAt"));
						a.put("regex", key.get("regex"));
						a.put("regexGN", key.get("regexGN"));
						a.put("upFold", key.get("upFold"));
						a.put("downFold", key.get("downFold"));
						a.put("upZscore", key.get("upZscore"));
						a.put("downZscore", key.get("downZscore"));
						a.put("significantGenes", key.get("significantGenes"));
						a.put("ClusterRowOrder", key.get("ClusterRowOrder"));
						a.put("ClusterColumnOrder", key.get("ClusterColumnOrder"));
						a.put("heatmapState", key.get("heatmapState"));
						a.put("heatmapAxis", key.get("heatmapAxis"));
						a.put("colorScheme", key.get("colorScheme"));
						a.put("filterOffset", key.get("filterOffset"));
					}
					results.add(a);
					response.setContentType("application/json");
					PrintWriter writer = response.getWriter();
					results.writeJSONString(writer);
					writer.close();
				}
				break;
			}
			}
		}
	}

	public JSONObject doCLustering(String filename, String outputfilename, String g, String e, String m, String ge) throws IOException {

		boolean remove = true;
		JSONObject output = new JSONObject();

		String exec = "runMicroArrayClustering.sh " + filename + " " + outputfilename + " " + ((g.equals("1")) ? ge : "0") + " "
				+ ((e.equals("1")) ? ge : "0") + " " + m;

		LOGGER.debug(exec);

		CommandResults callClustering = ExecUtilities.exec(exec);

		if (callClustering.getStdout()[0].equals("done")) {

			BufferedReader in = new BufferedReader(new FileReader(outputfilename + ".cdt"));
			String strLine;
			int count = 0;
			JSONArray rows = new JSONArray();
			while ((strLine = in.readLine()) != null) {
				String[] tabs = strLine.split("\t");
				if (count == 0) {
					JSONArray columns = new JSONArray();
					// copy from 4th column to all
					columns.addAll(Arrays.asList(tabs).subList(4, tabs.length));
					output.put("columns", columns);
				}
				if (count >= 3) {
					rows.add(tabs[1]);
				}
				count++;
			}
			in.close();
			output.put("rows", rows);
		}

		if (remove) {

			exec = "rm " + filename + " " + outputfilename;

			callClustering = ExecUtilities.exec(exec);
			LOGGER.debug("{}", callClustering);
		}

		return output;
	}

	private JSONArray getExperimentStats(DataApiHandler dataApi, JSONArray data, String samples, JSONArray sample_data) throws IOException {

		JSONArray results = new JSONArray();

		Map<String, ExpressionDataGene> genes = new HashMap<>();
		Map<String, String> sample = new HashMap<>();

		for (Object aSample_data : sample_data) {
			JSONObject a = (JSONObject) aSample_data;
			sample.put(a.get("pid").toString(), a.get("expname").toString());
		}

		for (final Object aData : data) {

			final JSONObject a = (JSONObject) aData;
			String id;

			if (a.containsKey("feature_id")) {
				id = (String) a.get("feature_id");
			}
			else if (a.containsKey("na_feature_id")) {
				id = (String) a.get("na_feature_id");
			}
			else {
				LOGGER.debug("data error: {}", a);
				id = "";
			}
			ExpressionDataGene b;

			if (genes.containsKey(id)) {
				b = genes.get(id);
			}
			else {
				b = new ExpressionDataGene(a);
			}

			b.addSamplestoGene(a, sample); // Sample HashMap is used to create absence/presence string
			genes.put(id, b);
		}

		List<String> featureIdList = new ArrayList<>();
		List<String> p2FeatureIdList = new ArrayList<>();

		JSONObject temp = new JSONObject();

		for (final Map.Entry<String, ExpressionDataGene> entry : genes.entrySet()) {

			final ExpressionDataGene value = entry.getValue();

			JSONObject a = new JSONObject();

			a.put("refseq_locus_tag", value.getRefSeqLocusTag());
			a.put("feature_id", value.getFeatureID());
			value.setSampleBinary(samples);
			a.put("sample_binary", value.getSampleBinary());
			a.put("sample_size", value.getSampleCounts());
			a.put("samples", value.getSamples());

			if (value.hasFeatureId()) {
				featureIdList.add(value.getFeatureID());
				temp.put(value.getFeatureID(), a);
			}
			else {
				p2FeatureIdList.add(value.getP2FeatureId());
				temp.put(value.getP2FeatureId(), a);
			}
		}

		featureIdList.remove(null);
		p2FeatureIdList.remove(null);

		LOGGER.trace("featureIdList[{}], p2FeatureIdList[{}]", featureIdList.size(), p2FeatureIdList.size());

		SolrQuery query = new SolrQuery("*:*");
		if (!featureIdList.isEmpty() && !p2FeatureIdList.isEmpty()) {
			query.addFilterQuery(
					"feature_id:(" + StringUtils.join(featureIdList, " OR ") + ") OR p2_feature_id:(" + StringUtils.join(p2FeatureIdList, " OR ")
							+ ")");
		}
		else if (featureIdList.isEmpty() && !p2FeatureIdList.isEmpty()) {
			query.addFilterQuery("p2_feature_id:(" + StringUtils.join(p2FeatureIdList, " OR ") + ")");
		}
		else if (!featureIdList.isEmpty() && p2FeatureIdList.isEmpty()) {
			query.addFilterQuery("feature_id:(" + StringUtils.join(featureIdList, " OR ") + ")");
		}
		else {
			// this should not occur
			query.addFilterQuery("feature_id:1");
		}
		query.setFields("feature_id,p2_feature_id,strand,product,accession,start,end,patric_id,alt_locus_tag,genome_name,gene");
		query.setRows(featureIdList.size() + p2FeatureIdList.size());

		LOGGER.trace("getExperimentStats:[{}] {}", SolrCore.FEATURE.getSolrCoreName(), query);

		final String apiResponse = dataApi.solrQuery(SolrCore.FEATURE, query);

		final Map resp = jsonReader.readValue(apiResponse);
		final Map respBody = (Map) resp.get("response");

		final List<GenomeFeature> features = dataApi.bindDocuments((List<Map>) respBody.get("docs"), GenomeFeature.class);

		for (final GenomeFeature feature : features) {
			JSONObject json = (JSONObject) temp.get(feature.getId());
			if (json == null) {
				json = (JSONObject) temp.get("" + feature.getP2FeatureId());
			}

			json.put("feature_id", feature.getId());
			json.put("strand", feature.getStrand());
			json.put("patric_product", feature.getProduct());
			json.put("patric_accession", feature.getAccession());
			json.put("start", feature.getStart());
			json.put("end", feature.getEnd());
			json.put("alt_locus_tag", feature.getAltLocusTag());
			json.put("patric_id", feature.getPatricId());
			json.put("genome_name", feature.getGenomeName());
			json.put("gene", feature.getGene());

			results.add(json);
		}

		return results;
	}

	public String getAuthorizationToken(PortletRequest request) {
		String token = null;

		String sessionId = request.getPortletSession(true).getId();
		Gson gson = new Gson();
		LinkedTreeMap sessionMap = gson.fromJson(SessionHandler.getInstance().get(sessionId), LinkedTreeMap.class);

		if (sessionMap.containsKey("authorizationToken")) {
			token = (String) sessionMap.get("authorizationToken");
		}

		return token;
	}
}
