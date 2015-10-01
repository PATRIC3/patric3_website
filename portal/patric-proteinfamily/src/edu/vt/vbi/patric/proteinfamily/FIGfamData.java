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
package edu.vt.vbi.patric.proteinfamily;

import edu.vt.vbi.patric.beans.Genome;
import edu.vt.vbi.patric.beans.GenomeFeature;
import edu.vt.vbi.patric.common.*;
import edu.vt.vbi.patric.msa.Aligner;
import edu.vt.vbi.patric.msa.SequenceData;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.codehaus.jackson.map.ObjectWriter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.ResourceRequest;
import javax.swing.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.*;

public class FIGfamData {

	public final static String FIGFAM_ID = "figfamId";

	private static final Logger LOGGER = LoggerFactory.getLogger(FIGfamData.class);

	DataApiHandler dataApiHandler;

	ObjectReader jsonReader;

	ObjectWriter jsonWriter;

	public FIGfamData() {

		ObjectMapper objectMapper = new ObjectMapper();
		jsonReader = objectMapper.reader(Map.class);
		jsonWriter = objectMapper.writerWithType(Map.class);
	}

	public FIGfamData(DataApiHandler dataApi) {
		dataApiHandler = dataApi;

		ObjectMapper objectMapper = new ObjectMapper();
		jsonReader = objectMapper.reader(Map.class);
		jsonWriter = objectMapper.writerWithType(Map.class);
	}

	// This function is called to get an ordering of the Figfams based on order of occurrence only for the ref. genome
	// It has nothing to do with the other genomes in a display.
	// This function returns an ordering of the Figfam ID's for the reference genome with paralogs removed
	// The counting for the number of paralogs occurs in the javascript code (I think)
	public JSONArray getSyntonyOrder(ResourceRequest request) {
		String genomeId = request.getParameter("syntonyId");
		final String familyType = request.getParameter("familyType");
		final String familyId = familyType + "_id";
		JSONArray json_arr = null;

		if (genomeId != null) {

			DataApiHandler dataApi = new DataApiHandler(request);
			long end_ms, start_ms = System.currentTimeMillis();

			SolrQuery solr_query = new SolrQuery("genome_id:" + genomeId);
			solr_query.setRows(1);
			solr_query.setFilterQueries("annotation:PATRIC AND feature_type:CDS AND " + familyId + ":[* TO *]");
			solr_query.addField(familyId);

			solr_query.setRows(DataApiHandler.MAX_ROWS);
			solr_query.addSort("accession", SolrQuery.ORDER.asc);
			solr_query.addSort("start", SolrQuery.ORDER.asc);

			LOGGER.debug("getSyntonyOrder() [{}] {}", SolrCore.FEATURE.getSolrCoreName(), solr_query);

			int orderSet = 0;
			List<SyntonyOrder> collect = new ArrayList<>();
			try {
				String apiResponse = dataApi.solrQuery(SolrCore.FEATURE, solr_query);

				Map resp = jsonReader.readValue(apiResponse);
				Map respBody = (Map) resp.get("response");

				List<GenomeFeature> features = dataApi.bindDocuments((List<Map>) respBody.get("docs"), GenomeFeature.class);

				end_ms = System.currentTimeMillis();

				LOGGER.debug("Genome anchoring query time - {}", (end_ms - start_ms));

				start_ms = System.currentTimeMillis();
				for (GenomeFeature feature : features) {
					if (familyType.equals("figfam") && feature.hasFigfamId()) {
						collect.add(new SyntonyOrder(feature.getFigfamId(), orderSet));
						++orderSet;
					}
					else if (familyType.equals("plfam") && feature.hasPlfamId()) {
						collect.add(new SyntonyOrder(feature.getPlfamId(), orderSet));
						++orderSet;
					}
					else if (familyType.equals("pgfam") && feature.hasPgfamId()) {
						collect.add(new SyntonyOrder(feature.getPgfamId(), orderSet));
						++orderSet;
					}
				}

			}
			catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}

			if (0 < collect.size()) {
				json_arr = new JSONArray();
				SyntonyOrder[] orderSave = new SyntonyOrder[collect.size()];
				collect.toArray(orderSave);// orderSave is array in order of Figfam ID
				SyntonyOrder[] toSort = new SyntonyOrder[collect.size()];
				System.arraycopy(orderSave, 0, toSort, 0, toSort.length);// copy the array so it can be sorted based on position in the genome
				Arrays.sort(toSort); // sort based on figfamIDs
				SyntonyOrder start = toSort[0];
				for (int i = 1; i < toSort.length; i++) {
					start = start.mergeSameId(toSort[i]);// set syntonyAt -1 to those objects which occur multiple times
				}
				orderSet = 0;
				for (SyntonyOrder anOrderSave : orderSave) {
					orderSet = (anOrderSave).compressAt(orderSet); // adjusts the syntonyAt number to get the correct
					// column based on replicon with -1's removed
				}
				for (SyntonyOrder aToSort : toSort) {// writes all those that don't have -1's
					(aToSort).write(json_arr);
				}
			}

			end_ms = System.currentTimeMillis();

			LOGGER.debug("Genome anchoring post processing time - {} ms", (end_ms - start_ms));
		}

		return json_arr;
	}

	@SuppressWarnings("unchecked")
	public void getLocusTags(ResourceRequest request, PrintWriter writer) {

		JSONArray arr = new JSONArray();

		DataApiHandler dataApi = new DataApiHandler(request);
		try {
			final String familyType = request.getParameter("familyType");
			final String familyId = familyType + "_id";

			SolrQuery solr_query = new SolrQuery();
			solr_query.setQuery("genome_id:(" + request.getParameter("genomeIds") + ") AND " + familyId + ":(" + request.getParameter("familyIds") + ")");
			solr_query.setFilterQueries("annotation:PATRIC AND feature_type:CDS");
			solr_query.addField("feature_id,patric_id,refseq_locus_tag,alt_locus_tag");
			solr_query.setRows(DataApiHandler.MAX_ROWS);

			LOGGER.debug("getLocusTags(): [{}] {}", SolrCore.FEATURE.toString(), solr_query);

			String apiResponse = dataApi.solrQuery(SolrCore.FEATURE, solr_query);
			Map resp = jsonReader.readValue(apiResponse);
			Map respBody = (Map) resp.get("response");

			List<GenomeFeature> features = dataApi.bindDocuments((List<Map>) respBody.get("docs"), GenomeFeature.class);

			for (GenomeFeature feature : features) {
				arr.add(feature.toJSONObject());
			}
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

		JSONObject data = new JSONObject();
		data.put("data", arr);
		try {
			data.writeJSONString(writer);
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public JSONArray getDetails(ResourceRequest request) throws IOException {

		JSONArray arr = new JSONArray();

		String genomeIds = request.getParameter("detailsGenomes");
		String familyIds = request.getParameter("detailsFamilyIds");
		String familyType = request.getParameter("familyType");

		LOGGER.debug("params for getDetails:{}", request.getParameterMap());
		final String familyId = familyType + "_id";

		SolrQuery query = new SolrQuery();
		query.setQuery("genome_id:(" + genomeIds + ") AND " + familyId + ":(" + familyIds + ")");
		query.setFields(StringUtils.join(DownloadHelper.getFieldsForFeatures(), ","));
		query.setFilterQueries("annotation:PATRIC AND feature_type:CDS");
		query.setRows(DataApiHandler.MAX_ROWS);

		LOGGER.debug("getDetails(): [{}] {}", SolrCore.FEATURE.getSolrCoreName(), query);

		String apiResponse = dataApiHandler.solrQuery(SolrCore.FEATURE, query);
		Map resp = jsonReader.readValue(apiResponse);
		Map respBody = (Map) resp.get("response");

		List<GenomeFeature> features = dataApiHandler.bindDocuments((List<Map>) respBody.get("docs"), GenomeFeature.class);

		for (GenomeFeature feature : features) {
			arr.add(feature.toJSONObject());
		}

		return arr;
	}

	public Aligner getFeatureAlignment(char needHtml, ResourceRequest request) {
		Aligner result = null;
		try {
			SequenceData[] sequences = getFeatureProteins(request);
			result = new Aligner(needHtml, request.getParameter(FIGFAM_ID), sequences);
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return result;
	}

	public String getGenomeIdsForTaxon(ResourceRequest request) throws IOException {
		String taxon = request.getParameter("taxonId");
		String genomeFilter = request.getParameter("genomeFilter");
		List<String> gIds = new ArrayList<>();

		SolrQuery query = new SolrQuery("patric_cds:[1 TO *] AND taxon_lineage_ids:" + taxon);
		if (genomeFilter != null && !genomeFilter.equals("")) {
			query.addFilterQuery(genomeFilter);
		}
		query.addField("genome_id");
		query.setSort("genome_name", SolrQuery.ORDER.asc);
		query.setRows(DataApiHandler.MAX_ROWS);

		LOGGER.trace("getGenomeIdsForTaxon: [{}] {}", SolrCore.GENOME.getSolrCoreName(), query);

		String apiResponse = dataApiHandler.solrQuery(SolrCore.GENOME, query);
		Map resp = jsonReader.readValue(apiResponse);
		Map respBody = (Map) resp.get("response");

		List<Genome> genomes = dataApiHandler.bindDocuments((List<Map>) respBody.get("docs"), Genome.class);

		for (final Genome g : genomes) {
			gIds.add(g.getId());
		}

		// TODO: remove this when data API limit is removed
		if (gIds.size() == 25000) {
			query.setStart(25000);

			apiResponse = dataApiHandler.solrQuery(SolrCore.GENOME, query);
			resp = jsonReader.readValue(apiResponse);
			respBody = (Map) resp.get("response");

			genomes = dataApiHandler.bindDocuments((List<Map>) respBody.get("docs"), Genome.class);

			for (final Genome g : genomes) {
				gIds.add(g.getId());
			}
		}

		return StringUtils.join(gIds, ",");
	}

	private SequenceData[] getFeatureSequences(String[] featureIds) throws IOException {
		List<SequenceData> collect = new ArrayList<>();

		SolrQuery query = new SolrQuery("feature_id:(" + StringUtils.join(featureIds, " OR ") + ")");
		query.addField("genome_name,patric_id,refseq_locus_tag,alt_locus_tag,aa_sequence");
		query.setRows(featureIds.length);

		LOGGER.trace("getFeatureSequences: [{}] {}", SolrCore.FEATURE.getSolrCoreName(), query);
		String apiResponse = dataApiHandler.solrQuery(SolrCore.FEATURE, query);
		Map resp = jsonReader.readValue(apiResponse);
		Map respBody = (Map) resp.get("response");

		List<GenomeFeature> features = dataApiHandler.bindDocuments((List<Map>) respBody.get("docs"), GenomeFeature.class);

		for (GenomeFeature feature : features) {
			String locusTag = "";
			if (feature.hasPatricId()) {
				locusTag = feature.getPatricId();
			}
			else {
				if (feature.hasRefseqLocusTag()) {
					locusTag = feature.getRefseqLocusTag();
				}
				else {
					if (feature.hasAltLocusTag()) {
						locusTag = feature.getAltLocusTag();
					}
				}
			}
			collect.add(new SequenceData(feature.getGenomeName().replace(" ", "_"), locusTag, feature.getAaSequence()));
		}

		SequenceData[] result = new SequenceData[collect.size()];
		collect.toArray(result);
		return result;
	}

	private SequenceData[] getFeatureProteins(ResourceRequest request) throws IOException {
		SequenceData[] result = null;
		String featuresString = request.getParameter("featureIds");
		LOGGER.debug("getFeatureProteins: {}", featuresString);
		if (featuresString != null && !featuresString.equals("")) {
			String[] idList = featuresString.split(",");
			this.dataApiHandler = new DataApiHandler(request);
			result = getFeatureSequences(idList);
		}
		return result;
	}

	public void getFeatureIds(ResourceRequest request, PrintWriter writer, String keyword) {

		try {
			DataApiHandler dataApi = new DataApiHandler(request);
			SolrQuery query = new SolrQuery(keyword);
			query.addField("feature_id");
			query.setRows(DataApiHandler.MAX_ROWS);


			String apiResponse = dataApi.solrQuery(SolrCore.FEATURE, query);
			Map resp = jsonReader.readValue(apiResponse);
			Map respBody = (Map) resp.get("response");

			List<GenomeFeature> features = dataApi.bindDocuments((List<Map>) respBody.get("docs"), GenomeFeature.class);

			List<String> featureIds = new ArrayList<>();
			for (GenomeFeature feature : features) {
				featureIds.add(feature.getId());
			}

			writer.write(StringUtils.join(featureIds, ","));
		}
		catch (IOException e) {
			LOGGER.debug(e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public void getGenomeDetails(ResourceRequest request, PrintWriter writer) throws IOException {

		String cType = request.getParameter("context_type");
		String cId = request.getParameter("context_id");
		String keyword = "";
		if (cType != null && cType.equals("taxon") && cId != null && !cId.equals("")) {
			keyword = "patric_cds:[1 TO *] AND taxon_lineage_ids:" + cId;
		}
		else if (request.getParameter("keyword") != null) {
			keyword = request.getParameter("keyword");
		}
		String fields = request.getParameter("fields");

		DataApiHandler dataApi = new DataApiHandler(request);

		SolrQuery query = new SolrQuery(keyword);
		if (fields != null && !fields.equals("")) {
			query.addField(fields);
		}
		query.setRows(DataApiHandler.MAX_ROWS).addSort("genome_name", SolrQuery.ORDER.asc);

		String pk = request.getParameter("param_key");
		Map<String, String> key = null;
		if (pk != null) {
			key = jsonReader.readValue(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk));
		}

		if (key != null && key.containsKey("genomeIds") && !key.get("genomeIds").equals("")) {
			query.addFilterQuery("genome_id:(" + key.get("genomeIds").replaceAll(",", " OR ") + ")");
		}

		LOGGER.trace("getGenomeDetails(): [{}] {}", SolrCore.GENOME.getSolrCoreName(), query);

		String apiResponse = dataApi.solrQuery(SolrCore.GENOME, query);
		Map resp = jsonReader.readValue(apiResponse);
		Map respBody = (Map) resp.get("response");

		int numFound = (Integer)  respBody.get("numFound");
		List<Map> sdl = (List<Map>) respBody.get("docs");

		JSONArray docs = new JSONArray();
		for (Map doc : sdl) {
			JSONObject  item = new JSONObject();
			item.putAll(doc);
			docs.add(item);
		}

		JSONObject jsonResult = new JSONObject();
		jsonResult.put("results", docs);
		jsonResult.put("total", numFound);
		jsonResult.writeJSONString(writer);
	}

	@SuppressWarnings("unchecked")
	public void getGroupStats(ResourceRequest request, PrintWriter writer) throws IOException {

		DataApiHandler dataApi = new DataApiHandler(request);

		JSONObject figfams = new JSONObject();
		Set<String> figfamIdList = new HashSet<>();
		List<String> genomeIdList = new LinkedList<>();
		// get family Type
		final String familyType = request.getParameter("familyType");
		final String familyId = familyType + "_id";

		// get genome list in order
		String genomeIds = request.getParameter("genomeIds");
		try {
			SolrQuery query = new SolrQuery("genome_id:(" + genomeIds.replaceAll(",", " OR ") + ")");
			query.addSort("genome_name", SolrQuery.ORDER.asc).addField("genome_id").setRows(DataApiHandler.MAX_ROWS);

			LOGGER.trace("[{}] {}", SolrCore.GENOME.getSolrCoreName(), query);

			String apiResponse = dataApi.solrQuery(SolrCore.GENOME, query);
			Map resp = jsonReader.readValue(apiResponse);
			Map respBody = (Map) resp.get("response");

			List<Genome> genomes = dataApi.bindDocuments((List<Map>) respBody.get("docs"), Genome.class);

			for (final Genome genome : genomes) {
				genomeIdList.add(genome.getId());
			}

			if (genomeIdList.size() == 25000) {
				query.setStart(25000);

				apiResponse = dataApi.solrQuery(SolrCore.GENOME, query);
				resp = jsonReader.readValue(apiResponse);
				respBody = (Map) resp.get("response");

				genomes = dataApi.bindDocuments((List<Map>) respBody.get("docs"), Genome.class);

				for (final Genome genome : genomes) {
					genomeIdList.add(genome.getId());
				}
			}
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

//		LOGGER.debug("genomeIdList: {}", genomeIdList);

		// getting genome counts per figfamID (figfam)
		// {stat:{field:{field:figfam_id,limit:-1,facet:{min:"min(aa_length)",max:"max(aa_length)",mean:"avg(aa_length)",ss:"sumsq(aa_length)",sum:"sum(aa_length)",dist:"percentile(aa_length,50,75,99,99.9)",field:{field:genome_id}}}}}

		try {
			long start = System.currentTimeMillis();
			SolrQuery query = new SolrQuery("annotation:PATRIC AND feature_type:CDS");
			query.addFilterQuery(getSolrQuery(request));
			query.addFilterQuery("!" + familyId + ":\"\"");
			query.setRows(0).setFacet(true).set("facet.threads", 15);
			query.add("json.facet", "{stat:{type:field,field:genome_id,limit:-1,facet:{figfams:{type:field,field:" + familyId +",limit:-1,sort:{index:asc}}}}}");

			LOGGER.trace("getGroupStats() 1/3: [{}] {}", SolrCore.FEATURE.getSolrCoreName(), query);
			String apiResponse = dataApi.solrQuery(SolrCore.FEATURE, query);

			long point = System.currentTimeMillis();
			LOGGER.debug("1st query: {} ms", (point - start));
			start = point;

			Map resp = jsonReader.readValue(apiResponse);
			Map facets = (Map) resp.get("facets");
			Map stat = (Map) facets.get("stat");

			final Map<String, String> figfamGenomeIdStr = new LinkedHashMap<>();
			final Map<String, Integer> figfamGenomeCount = new LinkedHashMap<>();

			final int genomeTotal = genomeIdList.size();
			final Map<String, Integer> genomePosMap = new LinkedHashMap<>();
			for (String genomeId : genomeIdList) {
				genomePosMap.put(genomeId, genomeIdList.indexOf(genomeId));
			}

			final Map<String,List> figfamGenomeIdCountMap = new ConcurrentHashMap<>();
			final Map<String,Set> figfamGenomeIdSet = new ConcurrentHashMap<>();

			List<Map> genomeBuckets = (List<Map>) stat.get("buckets");

			for (final Map bucket : genomeBuckets) {

				final String genomeId = (String) bucket.get("val");
				final List<Map> figfamBucket = (List<Map>) ((Map) bucket.get("figfams")).get("buckets");

				for (final Map figfam : figfamBucket) {
					final String figfamId = (String) figfam.get("val");
					final String genomeCount = String.format("%02x", (Integer) figfam.get("count"));

					if (figfamGenomeIdCountMap.containsKey(figfamId)) {
						figfamGenomeIdCountMap.get(figfamId).set(genomePosMap.get(genomeId), genomeCount);
					}
					else {
						final List<String> genomeIdCount = new LinkedList<>(Collections.nCopies(genomeTotal, "00"));
						genomeIdCount.set(genomePosMap.get(genomeId), genomeCount);
						figfamGenomeIdCountMap.put(figfamId, genomeIdCount);
					}

					if (figfamGenomeIdSet.containsKey(figfamId)) {
						figfamGenomeIdSet.get(figfamId).add(genomeId);
					}
					else {
						final Set<String> genomeIdSet = new HashSet<>();
						genomeIdSet.add(genomeId);
						figfamGenomeIdSet.put(figfamId, genomeIdSet);
					}
				}
			}

			for (String figfamId : figfamGenomeIdCountMap.keySet()) {
				final List genomeIdStr = figfamGenomeIdCountMap.get(figfamId);
				figfamGenomeIdStr.put(figfamId, StringUtils.join(genomeIdStr,""));
				figfamGenomeCount.put(figfamId, figfamGenomeIdSet.get(figfamId).size());
			}

			point = System.currentTimeMillis();
			LOGGER.debug("1st query process : {} ms, figfamGenomeIdStr:{}, figfamGenomeCount:{}", (point - start), figfamGenomeIdStr.size(), figfamGenomeCount.size());

			long start2nd = System.currentTimeMillis();
			// 2nd query

			query.set("json.facet",
					"{stat:{type:field,field:" + familyId + ",limit:-1,facet:{min:\"min(aa_length)\",max:\"max(aa_length)\",mean:\"avg(aa_length)\",ss:\"sumsq(aa_length)\",sum:\"sum(aa_length)\"}}}");

			LOGGER.trace("getGroupStats() 2/3: [{}] {}", SolrCore.FEATURE.getSolrCoreName(), query);
			apiResponse = dataApi.solrQuery(SolrCore.FEATURE, query);

			point = System.currentTimeMillis();
			LOGGER.debug("2st query: {} ms", (point - start2nd));
			start2nd = point;

			resp = jsonReader.readValue(apiResponse);
			facets = (Map) resp.get("facets");
			stat = (Map) facets.get("stat");

			List<Map> buckets = (List<Map>) stat.get("buckets");

			for (Map bucket : buckets) {
				final String figfamId = (String) bucket.get("val");
				final int count = (Integer) bucket.get("count");

				double min, max, mean, sumsq, sum;
				if (bucket.get("min") instanceof Double) {
					min = (Double) bucket.get("min");
				}
				else if (bucket.get("min") instanceof Integer) {
					min = ((Integer) bucket.get("min")).doubleValue();
				}
				else {
					min = 0;
				}
				if (bucket.get("max") instanceof Double) {
					max = (Double) bucket.get("max");
				}
				else if (bucket.get("max") instanceof Integer) {
					max = ((Integer) bucket.get("max")).doubleValue();
				}
				else {
					max = 0;
				}
				if (bucket.get("mean") instanceof Double) {
					mean = (Double) bucket.get("mean");
				}
				else if (bucket.get("mean") instanceof Integer) {
					mean = ((Integer) bucket.get("mean")).doubleValue();
				}
				else {
					mean = 0;
				}
				if (bucket.get("ss") instanceof Double) {
					sumsq = (Double) bucket.get("ss");
				}
				else if (bucket.get("ss") instanceof Integer) {
					sumsq = ((Integer) bucket.get("ss")).doubleValue();
				}
				else {
					sumsq = 0;
				}
				if (bucket.get("sum") instanceof Double) {
					sum = (Double) bucket.get("sum");
				}
				else if (bucket.get("sum") instanceof Integer) {
					sum = ((Integer) bucket.get("sum")).doubleValue();
				}
				else {
					sum = 0;
				}

//				LOGGER.debug("bucket:{}, sumsq:{}, count: {}", bucket, sumsq, count);
				double std;
				if (count > 1 ) {
					// std = Math.sqrt(sumsq / (count - 1));
					final double realSq = sumsq - (sum * sum)/count;
					std = Math.sqrt(realSq / (count - 1));
				}
				else {
					std = 0;
				}
				final JSONObject aaLength = new JSONObject();
				aaLength.put("min", min);
				aaLength.put("max", max);
				aaLength.put("mean", mean);
				aaLength.put("stddev", std);

				figfamIdList.add(figfamId);

				final JSONObject figfam = new JSONObject();
				figfam.put("genomes", figfamGenomeIdStr.get(figfamId));
				figfam.put("genome_count", figfamGenomeCount.get(figfamId));
				figfam.put("feature_count", count);
				figfam.put("stats", aaLength);

				figfams.put(figfamId, figfam);
			}

			point = System.currentTimeMillis();
			LOGGER.debug("2st query process: {} ms", (point - start2nd));
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

		// getting distinct figfam_product
		if (!figfamIdList.isEmpty()) {

			figfamIdList.remove("");

			try {
				SolrQuery query = new SolrQuery("family_id:(" + StringUtils.join(figfamIdList, " OR ") + ")");
				query.addFilterQuery("family_type:" + familyType);
				query.addField("family_id,family_product").setRows(figfamIdList.size());

				LOGGER.debug("getGroupStats() 3/3: [{}] {}", SolrCore.FIGFAM_DIC.getSolrCoreName(), query);

				String apiResponse = dataApi.solrQuery(SolrCore.FIGFAM_DIC, query);

				Map resp = jsonReader.readValue(apiResponse);
				Map respBody = (Map) resp.get("response");

				List<Map> sdl = (List<Map>) respBody.get("docs");

				for (final Map doc : sdl) {
					final JSONObject figfam = (JSONObject) figfams.get(doc.get("family_id"));
					figfam.put("description", doc.get("family_product"));
					figfams.put(doc.get("family_id").toString(), figfam);
				}

				int i = 1;
				while (sdl.size() == 25000) {
					query.setStart(25000 * i);

					apiResponse = dataApi.solrQuery(SolrCore.FIGFAM_DIC, query);
					resp = jsonReader.readValue(apiResponse);
					respBody = (Map) resp.get("response");

					sdl = (List<Map>) respBody.get("docs");

					for (final Map doc : sdl) {
						final JSONObject figfam = (JSONObject) figfams.get(doc.get("family_id"));
						figfam.put("description", doc.get("family_product"));
						figfams.put(doc.get("family_id").toString(), figfam);
					}
					i++;
				}
			}
			catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
				LOGGER.debug("::getGroupStats() 3/3, params: {}", request.getParameterMap().toString());
			}
			figfams.writeJSONString(writer);
		}
	}

	public String getSolrQuery(ResourceRequest request) throws IOException {
		String keyword = "";

		String paramKeyword = request.getParameter("keyword");
		if (paramKeyword != null && !paramKeyword.equals("") && !paramKeyword.equals("null")) {
			String processedKeyword = StringHelper.stripQuoteAndParseSolrKeywordOperator(paramKeyword);

			if (processedKeyword != null) {
				keyword += "(" + processedKeyword +")";
			}
		}

		String cType = request.getParameter("context_type");
		String cId = request.getParameter("context_id");
		if (cType != null && cType.equals("taxon") && cId != null && !cId.equals("")) {
			keyword += SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + cId);
		}
		else {
			String listText = request.getParameter("genomeIds");

			if (listText != null) {
				if (!keyword.equals("")) {
					keyword += " AND ";
				}
				keyword += "genome_id:(" + listText.replaceAll(",", " OR ") + ")";
			}
		}

		String pk = request.getParameter("param_key");

		Map<String, String> key = null;
		if (pk != null) {
			key = jsonReader.readValue(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk));
		}

		if (key != null && key.containsKey("genomeIds") && !key.get("genomeIds").equals("")) {
			if (!keyword.equals("")) {
				keyword += " AND ";
			}
			keyword += "genome_id:(" + key.get("genomeIds").replaceAll(",", " OR ") + ")";
		}

		return keyword;
	}

	private class SyntonyOrder implements Comparable<SyntonyOrder> {
		String groupId;

		int syntonyAt;

		SyntonyOrder(String id, int at) {
			groupId = id;
			syntonyAt = at;
		}

		int compressAt(int orderSet) {
			if (0 <= syntonyAt) {
				syntonyAt = orderSet;
				++orderSet;
			}
			return orderSet;
		}

		@SuppressWarnings("unchecked") void write(JSONArray json_arr) {
			if (0 <= syntonyAt) {
				JSONObject j = new JSONObject();
				j.put("syntonyAt", this.syntonyAt);
				j.put("groupId", this.groupId);
				json_arr.add(j);
			}
		}

		SyntonyOrder mergeSameId(SyntonyOrder other) {
			SyntonyOrder result = other;
			if ((this.groupId).equals(other.groupId)) {
				other.syntonyAt = -1;
				result = this;
			}
			return result;
		}

		public int compareTo(SyntonyOrder o) {
			int result = (this.groupId).compareTo(o.groupId);
			if (result == 0) {
				result = this.syntonyAt - o.syntonyAt;
			}
			return result;
		}
	}
}
