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
package edu.vt.vbi.patric.cache;

import edu.vt.vbi.patric.beans.Genome;
import edu.vt.vbi.patric.beans.Taxonomy;
import edu.vt.vbi.patric.common.DataApiHandler;
import edu.vt.vbi.patric.common.SolrCore;
import edu.vt.vbi.patric.common.SolrInterface;
import edu.vt.vbi.patric.dao.ResultType;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.PivotField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.RangeFacet;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.util.DateUtil;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.*;

@SuppressWarnings("unchecked")
public class DataLandingGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(DataLandingGenerator.class);

	final String baseURL = "http://enews.patricbrc.org";

	final String[] REFERENCE_GENOME_IDS = { "83332.12", "511145.12", "99287.12", "198215.6", "214092.21", "169963.11", "158879.11", "373153.27",
			"224914.11", "85962.8" };

	final String[] REFERENCE_GENOME_IDS_TRANSCRIPTOMICS = { "83332.12", "511145.12", "99287.12", "208964.12", "214092.21", "169963.11", "158879.11",
			"373153.27", "224914.11", "85962.8" };

	final Integer[] GENUS_TAXON_IDS = { 1386, 773, 138, 234, 32008, 194, 83553, 1485, 776, 943, 561, 262, 209, 1637, 1763, 780, 590, 620, 1279, 1301,
			662, 629 };

	final String URL_GENOMEOVERVIEW_TAB = "Genome?cType={cType}&cId={cId}";

	final String URL_FEATURETABLE_TAB = "FeatureTable?cType={cType}&cId={cId}&featuretype={featureType}&annotation=PATRIC&filtertype={filterType}";

	final String URL_PROTEINFAMILY_TAB = "FIGfam?cType={cType}&cId={cId}&dm=result&bm=";

	final String URL_PATHWAY_TAB = "CompPathwayTable?cType={cType}&cId={cId}&algorithm=PATRIC&ec_number=#aP0=1&aP1=1&aP2=1&aT=0&alg=PATRIC&cwEC=false&cwP=true&pId={pId}&pClass=&ecN=";

	final String URL_TRANSCRIPTOMICS_TAB = "ExperimentList?cType={cType}&cId={cId}&kw={kw}";

	final String URL_SINGLE_EXP = "SingleExperiment?cType=taxon&cId=2&eid={eid}";

	final String URL_GENOMEBROWSER = "GenomeBrowser?cType={cType}&cId={cId}&loc=0..10000&tracks=";

	final String URL_PATHWAY_EC_TAB = "CompPathwayTable?cType=genome&cId={cId}&algorithm=PATRIC&ec_number=#aP0=1&aP1=1&aP2=1&aT=1&alg=PATRIC&cwEC=false&cwP=true&pId={pId}&pClass=&ecN=";

	final String URL_PATHWAY_GENE_TAB = "CompPathwayTable?cType=genome&cId={cId}&algorithm=PATRIC&ec_number=#aP0=1&aP1=1&aP2=1&aT=2&alg=PATRIC&cwEC=false&cwP=true&pId={pId}&pClass=&ecN=";

	final String URL_SPECIALTY_GENE_TAB = "SpecialtyGeneList?cType=genome&cId={cId}&kw=source:{source}";

	DataApiHandler dataApi;

	public DataLandingGenerator() {
		dataApi = new DataApiHandler();
	}

	public boolean createCacheFileGenomes(String filePath) {
		boolean isSuccess = false;
		JSONObject jsonData = new JSONObject();
		JSONObject data;
		// from WP
		// data
		data = read(baseURL + "/tab/dlp-genomes-data/?req=passphrase");
		if (data != null) {
			jsonData.put("data", data);
		}
		// tools
		data = read(baseURL + "/tab/dlp-genomes-tools/?req=passphrase");
		if (data != null) {
			jsonData.put("tools", data);
		}
		// process
		data = read(baseURL + "/tab/dlp-genomes-process/?req=passphrase");
		if (data != null) {
			jsonData.put("process", data);
		}
		// download
		data = read(baseURL + "/tab/dlp-genomes-download/?req=passphrase");
		if (data != null) {
			jsonData.put("download", data);
		}
		// from solr or database
		// add popularGenomes
		data = getPopularGenomes();
		if (data != null) {
			jsonData.put("popularGenomes", data);
		}
		// add top5_1
		data = getTop5List("host_name");
		if (data != null) {
			jsonData.put("top5_1", data);
		}
		// add top5_2
		data = getTop5List("isolation_country");
		if (data != null) {
			jsonData.put("top5_2", data);
		}
		// add numberGenomes
		data = getGenomeCounts();
		if (data != null) {
			jsonData.put("numberGenomes", data);
		}

		// add genomeStatus
		data = getGenomeStatus();
		if (data != null) {
			jsonData.put("genomeStatus", data);
		}

		// save jsonData to file
		try {
			PrintWriter jsonOut = new PrintWriter(new FileWriter(filePath));
			jsonData.writeJSONString(jsonOut);
			jsonOut.close();
			isSuccess = true;
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return isSuccess;
	}

	public boolean createCacheFileProteinFamilies(String filePath) {
		boolean isSuccess = false;
		JSONObject jsonData = new JSONObject();
		JSONObject data;

		// from WP
		// data
		data = read(baseURL + "/tab/dlp-proteinfamilies-data/?req=passphrase");
		if (data != null) {
			jsonData.put("data", data);
		}
		// tools
		data = read(baseURL + "/tab/dlp-proteinfamilies-tools/?req=passphrase");
		if (data != null) {
			jsonData.put("tools", data);
		}
		// process
		data = read(baseURL + "/tab/dlp-proteinfamilies-process/?req=passphrase");
		if (data != null) {
			jsonData.put("process", data);
		}
		// download
		data = read(baseURL + "/tab/dlp-proteinfamilies-download/?req=passphrase");
		if (data != null) {
			jsonData.put("download", data);
		}
		//
		// add popularGenra
		data = getPopularGeneraFigfam();
		if (data != null) {
			jsonData.put("popularGenomes", data);
		}
		// add figfam graph data
		data = getProteinFamilies();
		if (data != null) {
			jsonData.put("FIGfams", data);
		}

		// save jsonData to file
		try {
			PrintWriter jsonOut = new PrintWriter(new FileWriter(filePath));
			jsonData.writeJSONString(jsonOut);
			jsonOut.close();
			isSuccess = true;
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return isSuccess;
	}

	public boolean createCacheFileGenomicFeatures(String filePath) {
		boolean isSuccess = false;
		JSONObject jsonData = new JSONObject();
		JSONObject data;

		// from WP
		// data
		data = read(baseURL + "/tab/dlp-genomicfeatures-data/?req=passphrase");
		if (data != null) {
			jsonData.put("data", data);
		}
		// popular genomes
		data = getPopularGenomesForGenomicFeature();
		if (data != null) {
			jsonData.put("popularGenomes", data);
		}
		// tools
		data = read(baseURL + "/tab/dlp-genomicfeatures-tools/?req=passphrase");
		if (data != null) {
			jsonData.put("tools", data);
		}
		// process
		data = read(baseURL + "/tab/dlp-genomicfeatures-process/?req=passphrase");
		if (data != null) {
			jsonData.put("process", data);
		}
		// download
		data = read(baseURL + "/tab/dlp-genomicfeatures-download/?req=passphrase");
		if (data != null) {
			jsonData.put("download", data);
		}

		// save jsonData to file
		try {
			PrintWriter jsonOut = new PrintWriter(new FileWriter(filePath));
			jsonData.writeJSONString(jsonOut);
			jsonOut.close();
			isSuccess = true;
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return isSuccess;
	}

	public boolean createCacheFileSpecialtyGenes(String filePath) {
		boolean isSuccess = false;
		JSONObject jsonData = new JSONObject();
		JSONObject data;

		// from WP
		// data
		data = read(baseURL + "/tab/dlp-specialtygenes-data/?req=passphrase");
		if (data != null) {
			jsonData.put("data", data);
		}
		// popular genomes
		data = getPopularGenomesForSpecialtyGene();
		if (data != null) {
			jsonData.put("popularGenomes", data);
		}
		// tools
		data = read(baseURL + "/tab/dlp-specialtygenes-tools/?req=passphrase");
		if (data != null) {
			jsonData.put("tools", data);
		}
		// process
		data = read(baseURL + "/tab/dlp-specialtygenes-process/?req=passphrase");
		if (data != null) {
			jsonData.put("process", data);
		}
		// download
		data = read(baseURL + "/tab/dlp-specialtygenes-download/?req=passphrase");
		if (data != null) {
			jsonData.put("download", data);
		}

		// save jsonData to file
		try {
			PrintWriter jsonOut = new PrintWriter(new FileWriter(filePath));
			jsonData.writeJSONString(jsonOut);
			jsonOut.close();
			isSuccess = true;
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return isSuccess;
	}

	public boolean createCacheFileAntibioticResistanceGenes(String filePath) {
		boolean isSuccess = false;
		JSONObject jsonData = new JSONObject();
		JSONObject data;

		// from WP
		// data
		data = read(baseURL + "/tab/dlp-antibioticresistance-data/?req=passphrase");
		if (data != null) {
			jsonData.put("data", data);
		}
		// popular genomes
		data = this.getPopularGenomesForAntibioticResistanceGene();
		if (data != null) {
			jsonData.put("popularGenomes", data);
		}
		// tools
		data = read(baseURL + "/tab/dlp-antibioticresistance-tools/?req=passphrase");
		if (data != null) {
			jsonData.put("tools", data);
		}
		// process
		data = read(baseURL + "/tab/dlp-antibioticresistance-process/?req=passphrase");
		if (data != null) {
			jsonData.put("process", data);
		}
		// download
		data = read(baseURL + "/tab/dlp-antibioticresistance-download/?req=passphrase");
		if (data != null) {
			jsonData.put("download", data);
		}

		// save jsonData to file
		try {
			PrintWriter jsonOut = new PrintWriter(new FileWriter(filePath));
			jsonData.writeJSONString(jsonOut);
			jsonOut.close();
			isSuccess = true;
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return isSuccess;
	}

	public boolean createCacheFileTranscriptomics(String filePath) {
		boolean isSuccess = false;
		JSONObject jsonData = new JSONObject();
		JSONObject data;

		// from WP
		// data
		data = read(baseURL + "/tab/dlp-transcriptomics-data/?req=passphrase");
		if (data != null) {
			jsonData.put("data", data);
		}
		// tools
		data = read(baseURL + "/tab/dlp-transcriptomics-tools/?req=passphrase");
		if (data != null) {
			jsonData.put("tools", data);
		}
		// process
		data = read(baseURL + "/tab/dlp-transcriptomics-process/?req=passphrase");
		if (data != null) {
			jsonData.put("process", data);
		}
		// download
		data = read(baseURL + "/tab/dlp-transcriptomics-download/?req=passphrase");
		if (data != null) {
			jsonData.put("download", data);
		}
		// topSpecies
		data = getTopSpeciesForTranscriptomics();
		if (data != null) {
			jsonData.put("topSpecies", data);
		}
		// featuredExperiment
		data = getFeaturedExperimentForTranscriptomics();
		if (data != null) {
			jsonData.put("featuredExperiment", data);
		}
		// popularGenomes
		data = getPopularGenomesForTranscriptomics();
		if (data != null) {
			jsonData.put("popularGenomes", data);
		}

		// save jsonData to file
		try {
			PrintWriter jsonOut = new PrintWriter(new FileWriter(filePath));
			jsonData.writeJSONString(jsonOut);
			jsonOut.close();
			isSuccess = true;
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return isSuccess;
	}

	public boolean createCacheFileProteomics(String filePath) {
		boolean isSuccess = false;
		JSONObject jsonData = new JSONObject();
		JSONObject data;

		// from WP
		// data
		data = read(baseURL + "/tab/dlp-proteomics-data/?req=passphrase");
		if (data != null) {
			jsonData.put("data", data);
		}
		// tools
		data = read(baseURL + "/tab/dlp-proteomics-tools/?req=passphrase");
		if (data != null) {
			jsonData.put("tools", data);
		}
		// process
		data = read(baseURL + "/tab/dlp-proteomics-process/?req=passphrase");
		if (data != null) {
			jsonData.put("process", data);
		}
		// download
		data = read(baseURL + "/tab/dlp-proteomics-download/?req=passphrase");
		if (data != null) {
			jsonData.put("download", data);
		}

		// save jsonData to file
		try {
			PrintWriter jsonOut = new PrintWriter(new FileWriter(filePath));
			jsonData.writeJSONString(jsonOut);
			jsonOut.close();
			isSuccess = true;
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return isSuccess;
	}

	public boolean createCacheFilePPInteractions(String filePath) {
		boolean isSuccess = false;
		JSONObject jsonData = new JSONObject();
		JSONObject data;

		// from WP
		// data
		data = read(baseURL + "/tab/dlp-ppinteractions-data/?req=passphrase");
		if (data != null) {
			jsonData.put("data", data);
		}
		// tools
		data = read(baseURL + "/tab/dlp-ppinteractions-tools/?req=passphrase");
		if (data != null) {
			jsonData.put("tools", data);
		}
		// process
		data = read(baseURL + "/tab/dlp-ppinteractions-process/?req=passphrase");
		if (data != null) {
			jsonData.put("process", data);
		}
		// download
		data = read(baseURL + "/tab/dlp-ppinteractions-download/?req=passphrase");
		if (data != null) {
			jsonData.put("download", data);
		}

		// save jsonData to file
		try {
			PrintWriter jsonOut = new PrintWriter(new FileWriter(filePath));
			jsonData.writeJSONString(jsonOut);
			jsonOut.close();
			isSuccess = true;
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return isSuccess;
	}

	public boolean createCacheFilePathways(String filePath) {
		boolean isSuccess = false;
		JSONObject jsonData = new JSONObject();
		JSONObject data;

		// from WP
		// data
		data = read(baseURL + "/tab/dlp-pathways-data/?req=passphrase");
		if (data != null) {
			jsonData.put("data", data);
		}
		// conservation
		data = getPathwayECDist();
		if (data != null) {
			jsonData.put("conservation", data);
		}
		// populargenomes
		data = getPopularGenomesForPathways();
		if (data != null) {
			jsonData.put("popularGenomes", data);
		}
		// tools
		data = read(baseURL + "/tab/dlp-pathways-tools/?req=passphrase");
		if (data != null) {
			jsonData.put("tools", data);
		}
		// process
		data = read(baseURL + "/tab/dlp-pathways-process/?req=passphrase");
		if (data != null) {
			jsonData.put("process", data);
		}
		// download
		data = read(baseURL + "/tab/dlp-pathways-download/?req=passphrase");
		if (data != null) {
			jsonData.put("download", data);
		}

		// save jsonData to file
		try {
			PrintWriter jsonOut = new PrintWriter(new FileWriter(filePath));
			jsonData.writeJSONString(jsonOut);
			jsonOut.close();
			isSuccess = true;
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return isSuccess;
	}

	private JSONObject getProteinFamilies() {
		JSONObject jsonData;
		JSONArray series = new JSONArray();

		for (Integer txId : GENUS_TAXON_IDS) {
			Map<String, Integer> stat = getFIGFamStat(txId);

			Taxonomy taxonomy = dataApi.getTaxonomy(txId);

			JSONObject item = new JSONObject();
			item.put("pathogen", taxonomy.getTaxonName());
			item.put("genomes", taxonomy.getGenomeCount());
			item.put("total", stat.get("total"));
			item.put("functional", stat.get("functional"));
			item.put("hypothetical", stat.get("hypothetical"));
			item.put("core", stat.get("core"));
			item.put("accessory", stat.get("accessory"));

			series.add(item);
		}

		jsonData = new JSONObject();
		jsonData.put("chart_title", "Protein Family Conservation in Pathogenic Bacteria");
		jsonData.put("chart_desc", "The graph below summarizes conservation of protein families across all the genomes in each pathogenic genus.");
		jsonData.put("data", series);

		return jsonData;
	}

	private JSONObject getPopularGeneraFigfam() {
		JSONObject jsonData;
		JSONArray list = new JSONArray();

		for (Integer txId : GENUS_TAXON_IDS) {
			Taxonomy taxonomy = dataApi.getTaxonomy(txId);

			JSONArray data = getFIGFamConservationDistribution(txId);

			JSONObject item = new JSONObject();
			item.put("link", "/portal/portal/patric/FIGfam?cType=taxon&cId=" + txId + "&dm=result&bm=&pk=");
			item.put("popularName", taxonomy.getTaxonName());
			item.put("popularData", data);

			list.add(item);
		}
		jsonData = new JSONObject();
		jsonData.put("popularTitle", "Popular Genera");
		jsonData.put("popularList", list);

		return jsonData;
	}

	private JSONObject getGenomeStatus() {
		JSONObject jsonData = null;

		try {
			Map status = dataApi.getFieldFacets(SolrCore.GENOME, "*:*", null, "genome_status");
			LOGGER.debug("{}", status);

			if (status != null) {
				int total = (Integer) status.get("total");
				Map facets = (Map) status.get("facets");
				Map<String, Integer> genomeStatus = (Map) facets.get("genome_status");

				JSONArray data = new JSONArray();
				for (String key : genomeStatus.keySet()) {
					JSONObject item = new JSONObject();
					if (key.equals("WGS")) {
						item.put("label", "Whole Genome Shotgun");
						item.put("m_label", "gsc_shotgun_sequence");
					}
					else if (key.equals("Complete") || key.equals("Plasmid")) {
						item.put("label", key);
						item.put("m_label", "gsc_" + key.toLowerCase());
					}
					float percentage = genomeStatus.get(key).floatValue() * 100.00f / total;
					item.put("value", Math.round(percentage));
					item.put("reported", Math.round(percentage) + "%");

					data.add(item);
				}
				jsonData = new JSONObject();
				jsonData.put("chart_title", "Genome Status");
				jsonData.put("data", data);
			}
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

		return jsonData;
	}

	private JSONObject getGenomeCounts() {
		SolrInterface solr = new SolrInterface();
		JSONArray series = new JSONArray();

		SolrQuery queryComplete = new SolrQuery("genome_status:Complete");
		SolrQuery queryWGS = new SolrQuery("genome_status:WGS");

		queryComplete.setFacet(true).setRows(0).setFacetSort(FacetParams.FACET_SORT_INDEX);
		//.set("json.facet", "{genome_count:{range:{field:completion_date,start:\"2010-01-01T00:00:00.000Z\",end:\"2016-01-01T00:00:00.000Z\",gap:\"%2B1YEAR\",other:\"before\"}}}");
		queryWGS.setFacet(true).setRows(0).setFacetSort(FacetParams.FACET_SORT_INDEX);
		//.set("json.facet", "{genome_count:{range:{field:completion_date,start:\"2010-01-01T00:00:00.000Z\",end:\"2016-01-01T00:00:00.000Z\",gap:\"%2B1YEAR\",other:\"before\"}}}");

		try {
			Date rangeStartDate = DateUtil.parseDate("2010-01-01'T'00:00:00.000'Z'");
			Date rangeEndDate = DateUtil.parseDate("2016-01-01'T'00:00:00.000'Z'");

			queryComplete.addDateRangeFacet("completion_date", rangeStartDate, rangeEndDate, "+1YEAR").add(FacetParams.FACET_RANGE_OTHER, "before");
			queryWGS.addDateRangeFacet("completion_date", rangeStartDate, rangeEndDate, "+1YEAR").add(FacetParams.FACET_RANGE_OTHER, "before");
		}
		catch (java.text.ParseException e) {
			LOGGER.error(e.getMessage(), e);
		}

		try {
			LOGGER.debug("getGenomeCount query: {}", queryComplete.toString());
			QueryResponse qrComplete = solr.getSolrServer(SolrCore.GENOME).query(queryComplete);
			QueryResponse qrWGS = solr.getSolrServer(SolrCore.GENOME).query(queryWGS);

			//			List<SimpleOrderedMap> bucketsComplete = (List) ((SimpleOrderedMap) ((SimpleOrderedMap) qrComplete.getResponse().get("facets")).get("genome_count")).get("buckets");
			//			List<SimpleOrderedMap> bucketsWGS = (List) ((SimpleOrderedMap) ((SimpleOrderedMap) qrWGS.getResponse().get("facets")).get("genome_count")).get("buckets");

			RangeFacet rfComplete = qrComplete.getFacetRanges().get(0);
			RangeFacet rfWGS = qrWGS.getFacetRanges().get(0);

			int countComplete = (int) rfComplete.getBefore();
			int countWGS = (int) rfWGS.getBefore();

			Map<String, Integer> mapCountComplete = new HashMap<>();
			Map<String, Integer> mapCountWGS = new HashMap<>();

			List<RangeFacet.Count> listComplete = rfComplete.getCounts();
			for (RangeFacet.Count facet : listComplete) {
				countComplete = countComplete + facet.getCount();
				mapCountComplete.put(facet.getValue().substring(0, 4), countComplete);
			}
			List<RangeFacet.Count> listWGS = rfWGS.getCounts();
			for (RangeFacet.Count facet : listWGS) {
				countWGS = countWGS + facet.getCount();
				mapCountWGS.put(facet.getValue().substring(0, 4), countWGS);

				String year = facet.getValue().substring(0, 4);
				JSONObject item = new JSONObject();
				item.put("year", Integer.parseInt(year));
				item.put("complete", mapCountComplete.get(year));
				item.put("wgs", mapCountWGS.get(year));
				series.add(item);
			}
		}
		catch (MalformedURLException | SolrServerException e) {
			LOGGER.error(e.getMessage(), e);
		}

		JSONObject jsonData = new JSONObject();
		jsonData.put("chart_title", "Number of Bacterial Genomes");
		jsonData.put("data", series);

		return jsonData;
	}

	private JSONObject getTop5List(String type) {
		JSONObject jsonData = null;
		// http://macleod.vbi.vt.edu:8983/solr/genomesummary/select?q=*%3A*&rows=0&wt=xml&facet=true&facet.field=host_name
		// http://macleod.vbi.vt.edu:8983/solr/genomesummary/select?q=*%3A*&rows=0&wt=xml&facet=true&facet.field=genome_status
		SolrInterface solr = new SolrInterface();
		try {
			solr.setCurrentInstance(SolrCore.GENOME);
			JSONObject status = solr.queryFacet("*:*", type);

			if (status != null) {
				JSONArray facet = (JSONArray) status.get("facet");
				JSONArray data = new JSONArray();
				long cntTop = 0, cntSecond = 0;

				for (int i = 0; i < 5; i++) {
					JSONObject f = (JSONObject) facet.get(i);
					JSONObject item = new JSONObject();

					String icon;
					switch (f.get("value").toString()) {
					case "Human, Homo sapiens":
						icon = "/patric/images/hosts/human.png";
						break;
					case "Cattle, Bos sp.":
						icon = "/patric/images/hosts/cow.png";
						break;
					case "Cattle, Bos taurus":
						icon = "/patric/images/hosts/cow.png";
						break;
					case "Bovine":
						icon = "/patric/images/hosts/cow.png";
						break;
					case "Pig, Sus scrofa domesticus":
						icon = "/patric/images/hosts/pig.png";
						break;
					case "Pig, Sus scrofa":
						icon = "/patric/images/hosts/pig.png";
						break;
					case "Cassava, Manihot esculenta":
						icon = "/patric/images/hosts/cassava.png";
						break;
					case "Bovine, Bovinae":
						icon = "/patric/images/hosts/cow.png";
						break;
					case "Cow, Bos taurus":
						icon = "/patric/images/hosts/cow.png";
						break;
					case "American bison, Bison bison":
						icon = "/patric/images/hosts/bison.png";
						break;
					case "Mouse, Mus musculus":
						icon = "/patric/images/hosts/mouse.png";
						break;
					case "cow":
						icon = "/patric/images/hosts/cow.png";
						break;
					case "pig":
						icon = "/patric/images/hosts/pig.png";
						break;
					case "tick":
						icon = "/patric/images/hosts/tick.png";
						break;
					case "sheep":
						icon = "/patric/images/hosts/sheep.png";
						break;
					case "Chicken, Gallus gallus":
						icon = "/patric/images/hosts/blank.png";
						break;
					case "USA":
						icon = "/patric/images/flags/United-States.png";
						break;
					default:
						icon = "/patric/images/flags/" + f.get("value").toString().replaceAll(" ", "-") + ".png";
						break;
					}

					item.put("icon", icon);
					item.put("label", f.get("value"));
					item.put("m_label", f.get("value").toString().replaceAll(" ", "_").toLowerCase());
					item.put("value", f.get("count"));

					data.add(item);
					//
					if (i == 0) {
						cntTop = (long) f.get("count");
					}
					else if (i == 1) {
						cntSecond = (long) f.get("count");
					}
				}
				// reported?
				if (cntTop > 2 * cntSecond) {
					JSONObject item = (JSONObject) data.get(0);
					item.put("reported", Math.round(cntSecond * 1.5));
					data.set(0, item);
				}
				jsonData = new JSONObject();
				if (type.equals("host_name")) {
					jsonData.put("chart_title", "Bacterial Host");
					jsonData.put("chart_desc", "Top 5 Bacterial Hosts");
					jsonData.put("tab_title", "Host");
				}
				else if (type.equals("isolation_country")) {
					jsonData.put("chart_title", "Isolation Country");
					jsonData.put("chart_desc", "Top 5 Isolation Countries");
					jsonData.put("tab_title", "Isolation Country");
				}
				jsonData.put("data", data);
			}
		}
		catch (MalformedURLException e) {
			LOGGER.error(e.getMessage(), e);
		}

		return jsonData;
	}

	private JSONObject getTopSpeciesForTranscriptomics() {

		SolrInterface solr = new SolrInterface();
		ResultType key = new ResultType();
		JSONObject res;

		JSONObject jsonData = new JSONObject();
		jsonData.put("title", "TOP 5 Species with Transcriptomics Data");

		JSONArray data = new JSONArray();
		try {
			solr.setCurrentInstance(SolrCore.TRANSCRIPTOMICS_EXPERIMENT);

			key.put("keyword", "*:*");
			res = solr.getData(key, null, "{\"facet\":\"organism\"}", 0, 0, true, false, false);

			JSONObject facets = (JSONObject) res.get("facets");
			JSONObject annotations = (JSONObject) facets.get("organism");
			JSONArray attrs = (JSONArray) annotations.get("attributes");

			int i = 0;
			for (Object attr : attrs) {
				JSONObject j = (JSONObject) attr;

				JSONObject organism = new JSONObject();
				organism.put("label", j.get("value"));
				organism.put("value", j.get("count"));
				data.add(organism);
				i++;
				if (i > 4) {
					break;
				}
			}
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		jsonData.put("data", data);
		return jsonData;
	}

	private JSONObject getFeaturedExperimentForTranscriptomics() {
		JSONObject jsonData = new JSONObject();
		JSONArray data = new JSONArray();

		SolrInterface solr = new SolrInterface();
		ResultType key = new ResultType();
		Map<String, String> sort = new HashMap<>();
		try {
			key.put("keyword", "*:*");
			sort.put("field", "release_date");
			sort.put("direction", "desc");

			solr.setCurrentInstance(SolrCore.TRANSCRIPTOMICS_EXPERIMENT);
			JSONObject res = solr.getData(key, sort, null, 0, 3, false, false, false);
			JSONArray docs = (JSONArray) ((JSONObject) res.get("response")).get("docs");

			for (Object obj : docs) {
				JSONObject row = (JSONObject) obj;
				JSONObject exp = new JSONObject();

				exp.put("title", row.get("title"));
				exp.put("pmid", row.get("pmid"));
				exp.put("accession", row.get("accession"));
				List<String> organisms = (List<String>) row.get("organism");
				exp.put("organism", organisms.get(0));
				exp.put("link", URL_SINGLE_EXP.replace("{eid}", row.get("eid").toString()));

				data.add(exp);
			}
			jsonData.put("data", data);
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

		return jsonData;
	}

	private JSONObject getPopularGenomes() {
		JSONObject jsonData = null;
		JSONArray list = new JSONArray();

		SolrInterface solr = new SolrInterface();

		for (String genomeId : REFERENCE_GENOME_IDS) {

			Genome genome = solr.getGenome(genomeId);

			// construct genome
			JSONObject popGenome = new JSONObject();
			popGenome.put("link", URL_GENOMEOVERVIEW_TAB.replace("{cType}", "genome").replace("{cId}", genomeId));
			popGenome.put("popularName", genome.getGenomeName());
			popGenome.put("gb_link", URL_GENOMEBROWSER.replace("{cType}", "genome").replace("{cId}", genomeId));

			// meta data
			JSONObject meta = new JSONObject();
			meta.put("genome_status", genome.getGenomeStatus());
			meta.put("completion_date", genome.hasCompletionDate() ? genome.getCompletionDate() : "");
			meta.put("collection_date", genome.hasCollectionDate() ? genome.getCollectionDate() : "");
			meta.put("isolation_country", genome.hasIsolationCountry() ? genome.getIsolationCountry() : "");
			meta.put("host_name", genome.hasHostName() ? genome.getHostName() : "");
			meta.put("disease", genome.hasDisease() ? StringUtils.join(genome.getDisease(), ", ") : "");
			meta.put("chromosomes", genome.getChromosomes());
			meta.put("plasmids", genome.getPlasmids());
			meta.put("contigs", genome.getContigs());
			meta.put("genome_length", genome.getGenomeLength());

			popGenome.put("metadata", meta);

			JSONArray data = new JSONArray();

			// Features
			JSONObject ft = new JSONObject();
			ft.put("description", "Features");
			ft.put("link", URL_FEATURETABLE_TAB.replace("{cType}", "genome").replace("{cId}", genomeId).replace("{featureType}", "")
					.replace("{filterType}", ""));
			ft.put("picture", "/patric/images/icon-popular-feature.png");
			ft.put("data", genome.getPatricCds());
			data.add(ft);

			// Pathways
			JSONObject pw = new JSONObject();
			pw.put("description", "Pathways");
			pw.put("link", URL_PATHWAY_TAB.replace("{cType}", "genome").replace("{cId}", genomeId).replace("{pId}", ""));
			pw.put("picture", "/patric/images/icon-popular-pathway.png");

			int cntPathway = 0;
			try {
				SolrQuery query = new SolrQuery("genome_id:" + genomeId);
				// {stat:{field:{field:genome_id,facet:{pathway_count:"unique(pathway_id)"}}}}}
				query.setRows(0).setFacet(true).set("json.facet", "{stat:{field:{field:genome_id,facet:{pathway_count:\"unique(pathway_id)\"}}}}}");

				QueryResponse qr = solr.getSolrServer(SolrCore.PATHWAY).query(query);
				List<SimpleOrderedMap> buckets = (List) ((SimpleOrderedMap) ((SimpleOrderedMap) qr.getResponse().get("facets")).get("stat"))
						.get("buckets");
				SimpleOrderedMap pathwayStat = buckets.get(0);
				cntPathway = (Integer) pathwayStat.get("pathway_count");
			}
			catch (MalformedURLException | SolrServerException e) {
				LOGGER.error(e.getMessage(), e);
			}
			pw.put("data", cntPathway);
			data.add(pw);

			// Protein Family
			JSONObject pf = new JSONObject();
			pf.put("description", "Protein Families");
			pf.put("link", URL_PROTEINFAMILY_TAB.replace("{cType}", "genome").replace("{cId}", genomeId));
			pf.put("picture", "/patric/images/icon-popular-proteinfamily.png");

			// Experiment
			JSONObject tr = new JSONObject();
			tr.put("description", "Transcriptomic Experiments");
			tr.put("link", URL_TRANSCRIPTOMICS_TAB.replace("{cType}", "genome").replace("{cId}", genomeId).replace("{kw}", ""));

			long numFound = 0;
			try {
				SolrQuery query = new SolrQuery("genome_ids:" + genomeId);
				query.setRows(0);

				QueryResponse qr = solr.getSolrServer(SolrCore.TRANSCRIPTOMICS_EXPERIMENT).query(query);
				numFound = qr.getResults().getNumFound();
			}
			catch (MalformedURLException | SolrServerException e) {
				LOGGER.error(e.getMessage(), e);
			}
			tr.put("picture", "/patric/images/icon-popular-experiment.png");
			tr.put("data", (int) numFound);

			data.add(tr);

			try {
				SolrQuery query = new SolrQuery("figfam_id:[* TO *] AND annotation:PATRIC AND genome_id:" + genomeId);
				query.setRows(0);
				QueryResponse qr = solr.getSolrServer(SolrCore.FEATURE).query(query);

				pf.put("data", qr.getResults().getNumFound());
			}
			catch (MalformedURLException | SolrServerException e) {
				LOGGER.error(e.getMessage(), e);
			}
			data.add(pf);

			popGenome.put("popularData", data);

			list.add(popGenome);
		}
		if (list.size() > 0) {
			jsonData = new JSONObject();
			jsonData.put("popularList", list);
			jsonData.put("popularTitle", "Select Genomes");
		}
		return jsonData;
	}

	private JSONObject getPopularGenomesForGenomicFeature() {
		JSONObject jsonData = null;
		JSONArray list = new JSONArray();

		SolrInterface solr = new SolrInterface();

		for (String genomeId : REFERENCE_GENOME_IDS) {

			Genome genome = dataApi.getGenome(genomeId);

			JSONObject hypotheticalProteins = new JSONObject();
			JSONObject functionalProteins = new JSONObject();
			JSONObject ecAssignedProteins = new JSONObject();
			JSONObject goAssignedProteins = new JSONObject();
			JSONObject pathwayAssignedProteins = new JSONObject();
			JSONObject figfamAssignedProteins = new JSONObject();

			// construct genome
			JSONObject popGenome = new JSONObject();
			popGenome.put("link", URL_GENOMEOVERVIEW_TAB.replace("{cType}", "genome").replace("{cId}", genomeId));
			popGenome.put("popularName", genome.getGenomeName());

			JSONArray featureTypes = new JSONArray();
			JSONArray proteinSummary = new JSONArray();
			JSONArray specialtyGenes = new JSONArray();
			try {
				solr.setCurrentInstance(SolrCore.FEATURE);
				ResultType key = new ResultType();
				key.put("filter", String.format("genome_id:(%s) AND annotation:PATRIC", genomeId));

				// top 5 feature type
				key.put("keyword", "*:*");
				JSONObject res = solr.getData(key, null, "{\"facet\":\"feature_type\"}", 0, 0, true, false, false);

				JSONObject facets = (JSONObject) res.get("facets");
				JSONObject annotations = (JSONObject) facets.get("feature_type");
				JSONArray attrs = (JSONArray) annotations.get("attributes");

				int i = 0;
				for (Object attr : attrs) {
					JSONObject j = (JSONObject) attr;

					JSONObject fTypes = new JSONObject();
					fTypes.put("description", j.get("value"));
					fTypes.put(
							"link",
							URL_FEATURETABLE_TAB.replace("{cType}", "genome").replace("{cId}", genomeId)
									.replace("{featureType}", j.get("value").toString()).replace("{filterType}", ""));
					fTypes.put("data", j.get("count"));
					featureTypes.add(fTypes);
					i++;
					if (i > 4) {
						break;
					}
				}

				// Protein Summary
				// hypothetical
				key.put("keyword", "product:(hypothetical AND protein) AND feature_type:CDS");
				res = solr.getData(key, null, "{\"facet\":\"annotation\"}", 0, 0, true, false, false);

				facets = (JSONObject) res.get("facets");
				annotations = (JSONObject) facets.get("annotation");
				attrs = (JSONArray) annotations.get("attributes");

				hypotheticalProteins.put("description", "Unknown functions");
				hypotheticalProteins.put("link", URL_FEATURETABLE_TAB.replace("{cType}", "genome").replace("{cId}", genomeId)
						.replace("{featureType}", "CDS").replace("{filterType}", "hypothetical_proteins"));
				for (Object attr : attrs) {
					JSONObject j = (JSONObject) attr;
					hypotheticalProteins.put("data", j.get("count"));
				}
				proteinSummary.add(hypotheticalProteins);

				// funtional assigned
				key.put("keyword", "!product:(hypothetical AND protein) AND feature_type:CDS");
				res = solr.getData(key, null, "{\"facet\":\"annotation\"}", 0, 0, true, false, false);

				facets = (JSONObject) res.get("facets");
				annotations = (JSONObject) facets.get("annotation");
				attrs = (JSONArray) annotations.get("attributes");

				functionalProteins.put("description", "Functional assignments");
				functionalProteins.put("link",
						URL_FEATURETABLE_TAB.replace("{cType}", "genome").replace("{cId}", genomeId).replace("{featureType}", "CDS")
								.replace("{filterType}", "functional_proteins"));
				for (Object attr : attrs) {
					JSONObject j = (JSONObject) attr;
					functionalProteins.put("data", j.get("count"));
				}
				proteinSummary.add(functionalProteins);

				// ec assigned
				key.put("keyword", "ec:[*%20TO%20*]");
				res = solr.getData(key, null, "{\"facet\":\"annotation\"}", 0, 0, true, false, false);

				facets = (JSONObject) res.get("facets");
				annotations = (JSONObject) facets.get("annotation");
				attrs = (JSONArray) annotations.get("attributes");

				ecAssignedProteins.put("description", "EC assignments");
				ecAssignedProteins.put("link",
						URL_FEATURETABLE_TAB.replace("{cType}", "genome").replace("{cId}", genomeId).replace("{featureType}", "CDS")
								.replace("{filterType}", "ec"));
				for (Object attr : attrs) {
					JSONObject j = (JSONObject) attr;
					ecAssignedProteins.put("data", j.get("count"));
				}
				proteinSummary.add(ecAssignedProteins);

				// go assigned
				key.put("keyword", "go:[*%20TO%20*]");
				res = solr.getData(key, null, "{\"facet\":\"annotation\"}", 0, 0, true, false, false);

				facets = (JSONObject) res.get("facets");
				annotations = (JSONObject) facets.get("annotation");
				attrs = (JSONArray) annotations.get("attributes");

				goAssignedProteins.put("description", "GO assignments");
				goAssignedProteins.put("link",
						URL_FEATURETABLE_TAB.replace("{cType}", "genome").replace("{cId}", genomeId).replace("{featureType}", "CDS")
								.replace("{filterType}", "go"));
				for (Object attr : attrs) {
					JSONObject j = (JSONObject) attr;
					goAssignedProteins.put("data", j.get("count"));
				}
				proteinSummary.add(goAssignedProteins);

				// pathway assigned
				key.put("keyword", "pathway:[*%20TO%20*]");
				res = solr.getData(key, null, "{\"facet\":\"annotation\"}", 0, 0, true, false, false);

				facets = (JSONObject) res.get("facets");
				annotations = (JSONObject) facets.get("annotation");
				attrs = (JSONArray) annotations.get("attributes");

				pathwayAssignedProteins.put("description", "Pathways assignments");
				pathwayAssignedProteins.put("link", URL_FEATURETABLE_TAB.replace("{cType}", "genome").replace("{cId}", genomeId)
						.replace("{featureType}", "CDS").replace("{filterType}", "pathway"));
				for (Object attr : attrs) {
					JSONObject j = (JSONObject) attr;
					pathwayAssignedProteins.put("data", j.get("count"));
				}
				proteinSummary.add(pathwayAssignedProteins);

				// figfam assigned
				key.put("keyword", "figfam_id:[*%20TO%20*]");
				res = solr.getData(key, null, "{\"facet\":\"annotation\"}", 0, 0, true, false, false);

				facets = (JSONObject) res.get("facets");
				annotations = (JSONObject) facets.get("annotation");
				attrs = (JSONArray) annotations.get("attributes");

				figfamAssignedProteins.put("description", "FIGfam assignments");
				figfamAssignedProteins.put("link", URL_FEATURETABLE_TAB.replace("{cType}", "genome").replace("{cId}", genomeId)
						.replace("{featureType}", "CDS").replace("{filterType}", "figfam_id"));
				for (Object attr : attrs) {
					JSONObject j = (JSONObject) attr;
					figfamAssignedProteins.put("data", j.get("count"));
				}
				proteinSummary.add(figfamAssignedProteins);

				// Specialty Gene Queries
				try {
					SolrQuery query = new SolrQuery("genome_id:" + genomeId);
					query.setRows(0).setFacet(true).setFacetMinCount(1);
					query.addFacetPivotField("property,source").setFacetSort(FacetParams.FACET_SORT_INDEX);

					LOGGER.debug("getPopularGenomesForGenomicFeature: {}", query.toString());
					QueryResponse qr = solr.getSolrServer(SolrCore.SPECIALTY_GENE_MAPPING).query(query);
					List<PivotField> pivotList = qr.getFacetPivot().get("property,source");
					for (PivotField pivot : pivotList) {
						String property = pivot.getValue().toString();

						for (PivotField pv : pivot.getPivot()) {
							String source = pv.getValue().toString();

							JSONObject sp = new JSONObject();
							sp.put("description", property + ": " + source);
							sp.put("link", URL_SPECIALTY_GENE_TAB.replace("{cId}", genomeId).replace("{source}", source));
							sp.put("data", pv.getCount());

							specialtyGenes.add(sp);
						}
					}
				}
				catch (SolrServerException e) {
					LOGGER.error(e.getMessage(), e);
				}
			}
			catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
			//
			popGenome.put("featureTypes", featureTypes);
			popGenome.put("proteinSummary", proteinSummary);
			popGenome.put("specialtyGenes", specialtyGenes);

			// link outs
			JSONArray links = new JSONArray();
			// Genome Browser
			JSONObject link = new JSONObject();
			link.put("name", "Genome Browser");
			link.put("link", URL_GENOMEBROWSER.replace("{cType}", "genome").replace("{cId}", genomeId));
			links.add(link);

			// Feature Table
			link = new JSONObject();
			link.put("name", "Feature Table");
			link.put("link", URL_FEATURETABLE_TAB.replace("{cType}", "genome").replace("{cId}", genomeId)
					.replace("{featureType}", "").replace("{filterType}", ""));
			links.add(link);

			// Protein Family
			link = new JSONObject();
			link.put("name", "Protein Family Sorter");
			link.put("link", URL_PROTEINFAMILY_TAB.replace("{cType}", "genome").replace("{cId}", genomeId));
			links.add(link);

			// Pathway
			link = new JSONObject();
			link.put("name", "Pathway");
			link.put("link", URL_PATHWAY_TAB.replace("{cType}", "genome").replace("{cId}", genomeId).replace("{pId}", ""));
			links.add(link);

			// Transcriptomics
			link = new JSONObject();
			link.put("name", "Transcriptomics");
			link.put("link", URL_TRANSCRIPTOMICS_TAB.replace("{cType}", "genome").replace("{cId}", genomeId).replace("{kw}", ""));
			links.add(link);

			popGenome.put("links", links);
			list.add(popGenome);
		}
		if (list.size() > 0) {
			jsonData = new JSONObject();
			jsonData.put("popularList", list);
			jsonData.put("popularTitle", "Select Genomes");
		}
		return jsonData;
	}

	private JSONObject getPopularGenomesForSpecialtyGene() {
		JSONObject jsonData = null;
		JSONArray list = new JSONArray();

		SolrInterface solr = new SolrInterface();
		LBHttpSolrServer lbHttpSolrServer;
		try {
			lbHttpSolrServer = solr.getSolrServer(SolrCore.SPECIALTY_GENE_MAPPING);
		}
		catch (MalformedURLException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}

		for (String genomeId : REFERENCE_GENOME_IDS) {

			Genome genome = dataApi.getGenome(genomeId);

			// construct genome
			JSONObject popGenome = new JSONObject();
			popGenome.put("link", URL_GENOMEOVERVIEW_TAB.replace("{cType}", "genome").replace("{cId}", genomeId));
			popGenome.put("popularName", genome.getGenomeName());

			JSONArray specialtyGenes = new JSONArray();

			try {
				SolrQuery query = new SolrQuery("genome_id:" + genomeId);
				query.setRows(0).setFacet(true).setFacetMinCount(1);
				query.addFacetPivotField("property,source").setFacetSort(FacetParams.FACET_SORT_INDEX);

				LOGGER.debug("getPopularGenomesForSpecialtyGene: {}", query.toString());
				QueryResponse qr = lbHttpSolrServer.query(query);
				List<PivotField> pivotList = qr.getFacetPivot().get("property,source");
				for (PivotField pivot : pivotList) {
					String property = pivot.getValue().toString();

					for (PivotField pv : pivot.getPivot()) {
						String source = pv.getValue().toString();

						JSONObject sp = new JSONObject();
						sp.put("description", property + ": " + source);
						sp.put("link", URL_SPECIALTY_GENE_TAB.replace("{cId}", genomeId).replace("{source}", source));
						sp.put("data", pv.getCount());

						specialtyGenes.add(sp);
					}
				}
			}
			catch (SolrServerException e) {
				LOGGER.error(e.getMessage(), e);
			}
			//
			popGenome.put("specialtyGenes", specialtyGenes);

			// link outs
			JSONArray links = new JSONArray();
			// Genome Browser
			JSONObject link = new JSONObject();
			link.put("name", "Genome Browser");
			link.put("link", URL_GENOMEBROWSER.replace("{cType}", "genome").replace("{cId}", genomeId));
			links.add(link);

			// Feature Table
			link = new JSONObject();
			link.put("name", "Feature Table");
			link.put("link", URL_FEATURETABLE_TAB.replace("{cType}", "genome").replace("{cId}", genomeId)
					.replace("{featureType}", "").replace("{filterType}", ""));
			links.add(link);

			// Protein Family
			link = new JSONObject();
			link.put("name", "Protein Family Sorter");
			link.put("link", URL_PROTEINFAMILY_TAB.replace("{cType}", "genome").replace("{cId}", genomeId));
			links.add(link);

			// Pathway
			link = new JSONObject();
			link.put("name", "Pathway");
			link.put("link", URL_PATHWAY_TAB.replace("{cType}", "genome").replace("{cId}", genomeId).replace("{pId}", ""));
			links.add(link);

			// Transcriptomics
			link = new JSONObject();
			link.put("name", "Transcriptomics");
			link.put("link", URL_TRANSCRIPTOMICS_TAB.replace("{cType}", "genome").replace("{cId}", genomeId).replace("{kw}", ""));
			links.add(link);

			popGenome.put("links", links);
			list.add(popGenome);
		}
		if (list.size() > 0) {
			jsonData = new JSONObject();
			jsonData.put("popularList", list);
			jsonData.put("popularTitle", "Select Genomes");
		}
		return jsonData;
	}

	private JSONObject getPopularGenomesForAntibioticResistanceGene() {
		JSONObject jsonData = null;
		JSONArray list = new JSONArray();

		SolrInterface solr = new SolrInterface();
		LBHttpSolrServer lbHttpSolrServer;
		try {
			lbHttpSolrServer = solr.getSolrServer(SolrCore.SPECIALTY_GENE_MAPPING);
		}
		catch (MalformedURLException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}

		for (String genomeId : REFERENCE_GENOME_IDS) {

			Genome genome = dataApi.getGenome(genomeId);

			// construct genome
			JSONObject popGenome = new JSONObject();
			popGenome.put("link", URL_GENOMEOVERVIEW_TAB.replace("{cType}", "genome").replace("{cId}", genomeId));
			popGenome.put("popularName", genome.getGenomeName());

			JSONArray specialtyGenes = new JSONArray();
			// Specialty Gene Queries
			try {
				SolrQuery query = new SolrQuery();
				query.setQuery("property:\"Antibiotic Resistance\"");
				query.setFilterQueries("genome_id:" + genomeId);
				query.setFacet(true).setFacetMinCount(1).addFacetField("source").setFacetSort(FacetParams.FACET_SORT_INDEX);

				QueryResponse qr = lbHttpSolrServer.query(query);
				FacetField ff = qr.getFacetField("source");
				for (FacetField.Count fc : ff.getValues()) {
					JSONObject sp = new JSONObject();
					String source = fc.getName().trim();
					sp.put("description", source);
					sp.put("link", URL_SPECIALTY_GENE_TAB.replace("{cId}", genomeId).replace("{source}", source));
					sp.put("data", fc.getCount());

					specialtyGenes.add(sp);
				}
			}
			catch (SolrServerException e) {
				LOGGER.error(e.getMessage(), e);
			}
			//
			popGenome.put("specialtyGenes", specialtyGenes);

			// link outs
			JSONArray links = new JSONArray();
			// Genome Browser
			JSONObject link = new JSONObject();
			link.put("name", "Genome Browser");
			link.put("link", URL_GENOMEBROWSER.replace("{cType}", "genome").replace("{cId}", genomeId));
			links.add(link);

			// Feature Table
			link = new JSONObject();
			link.put("name", "Feature Table");
			link.put("link", URL_FEATURETABLE_TAB.replace("{cType}", "genome").replace("{cId}", genomeId)
					.replace("{featureType}", "").replace("{filterType}", ""));
			links.add(link);

			// Protein Family
			link = new JSONObject();
			link.put("name", "Protein Family Sorter");
			link.put("link", URL_PROTEINFAMILY_TAB.replace("{cType}", "genome").replace("{cId}", genomeId));
			links.add(link);

			// Pathway
			link = new JSONObject();
			link.put("name", "Pathway");
			link.put("link", URL_PATHWAY_TAB.replace("{cType}", "genome").replace("{cId}", genomeId).replace("{pId}", ""));
			links.add(link);

			// Transcriptomics
			link = new JSONObject();
			link.put("name", "Transcriptomics");
			link.put("link", URL_TRANSCRIPTOMICS_TAB.replace("{cType}", "genome").replace("{cId}", genomeId).replace("{kw}", ""));
			links.add(link);

			popGenome.put("links", links);
			list.add(popGenome);
		}
		if (list.size() > 0) {
			jsonData = new JSONObject();
			jsonData.put("popularList", list);
			jsonData.put("popularTitle", "Select Genomes");
		}
		return jsonData;
	}

	private JSONObject getPopularGenomesForPathways() {

		JSONObject jsonData = null;
		JSONArray list = new JSONArray();
		SolrInterface solr = new SolrInterface();

		for (String genomeId : REFERENCE_GENOME_IDS) {
			Genome genome = dataApi.getGenome(genomeId);

			// construct genome
			JSONObject popGenome = new JSONObject();

			popGenome.put("popularName", genome.getGenomeName());
			popGenome.put("link", URL_PATHWAY_TAB.replace("{cType}", "genome").replace("{cId}", genomeId).replace("{pId}", ""));

			JSONArray data = new JSONArray();

			LinkedList<Map<String, String>> pathwayList = new LinkedList<>();

			try {
				//{stat:{field:{field:pathway_id,sort:{ec_count:desc},facet:{ec_count:"unique(ec_number)",gene_count:"unique(feature_id)",field:{field:pathway_name}}}}}
				SolrQuery query = new SolrQuery("genome_id:" + genomeId).addFilterQuery("annotation:PATRIC");
				query.setRows(0).setFacet(true).set("json.facet",
						"{stat:{field:{field:pathway_id,sort:{ec_count:desc},facet:{ec_count:\"unique(ec_number)\",gene_count:\"unique(feature_id)\",field:{field:pathway_name}}}}}");

				LOGGER.debug("getPopularGenomesForPathways: {}", query.toString());
				QueryResponse qr = solr.getSolrServer(SolrCore.PATHWAY).query(query);
				List<SimpleOrderedMap> buckets = (List) ((SimpleOrderedMap) ((SimpleOrderedMap) qr.getResponse().get("facets")).get("stat"))
						.get("buckets");

				for (SimpleOrderedMap bucket : buckets) {
					Map<String, String> pathway = new HashMap<>();
					pathway.put("id", bucket.get("val").toString());
					pathway.put("ec_count", bucket.get("ec_count").toString());
					pathway.put("gene_count", bucket.get("gene_count").toString());

					// getting name
					List<SimpleOrderedMap> subBuckets = (List) ((SimpleOrderedMap) bucket.get("field")).get("buckets");
					pathway.put("name", subBuckets.get(0).get("val").toString());

					pathwayList.add(pathway);
				}
			}
			catch (MalformedURLException | SolrServerException e) {
				LOGGER.error(e.getMessage(), e);
			}

			//			List<ResultType> items = connPW.getCompPathwayPathwayList(key, sort, 0, 10);
			// for (ResultType item : items) {
			for (Map<String, String> pathway : pathwayList) {
				JSONObject pw = new JSONObject();

				pw.put("name", pathway.get("name"));
				pw.put("name_link", URL_PATHWAY_TAB.replace("{cType}", "genome").replace("{cId}", genomeId).replace("{pId}", pathway.get("id")));
				// pw.put("class", item.get("pathway_class"));
				pw.put("gene_count", pathway.get("gene_count"));
				pw.put("gene_link", URL_PATHWAY_GENE_TAB.replace("{cId}", genomeId).replace("{pId}", pathway.get("id")));
				pw.put("ec_count", pathway.get("ec_count"));
				pw.put("ec_link", URL_PATHWAY_EC_TAB.replace("{cId}", genomeId).replace("{pId}", pathway.get("id")));

				data.add(pw);
			}
			popGenome.put("popularData", data);
			list.add(popGenome);
		}
		if (list.size() > 0) {
			jsonData = new JSONObject();
			jsonData.put("popularList", list);
			jsonData.put("popularTitle", "Select Genomes");
		}
		return jsonData;
	}

	private JSONObject getPopularGenomesForTranscriptomics() {
		JSONObject jsonData = null;
		JSONArray list = new JSONArray();
		SolrInterface solr = new SolrInterface();
		LBHttpSolrServer lbHttpSolrServer;
		try {
			lbHttpSolrServer = solr.getSolrServer(SolrCore.TRANSCRIPTOMICS_EXPERIMENT);
		}
		catch (MalformedURLException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}

		for (String genomeId : REFERENCE_GENOME_IDS_TRANSCRIPTOMICS) {
			ResultType key = new ResultType();
			JSONObject res;

			// construct genome node
			JSONObject popGenome = new JSONObject();

			Genome genome = dataApi.getGenome(genomeId);

			popGenome.put("popularName", genome.getGenomeName());
			popGenome.put("link", URL_TRANSCRIPTOMICS_TAB.replace("{cType}", "genome").replace("{cId}", genomeId).replace("{kw}", ""));

			// Retrieve eId associated a given genome
			List<String> eIds = new ArrayList<>();
			try {
				SolrQuery query = new SolrQuery("genome_ids:" + genomeId);
				query.setRows(1000);
				query.setFields("eid");

				QueryResponse qr = lbHttpSolrServer.query(query);
				SolrDocumentList sdl = qr.getResults();

				for (SolrDocument doc : sdl) {
					eIds.add(doc.get("eid").toString());
				}
			}
			catch (SolrServerException e) {
				LOGGER.error(e.getMessage(), e);
			}

			JSONObject gm = new JSONObject();
			JSONObject ec = new JSONObject();
			gm.put("title", "Gene Modifications");
			ec.put("title", "Experiment Conditions");
			JSONArray data = new JSONArray();
			try {
				solr.setCurrentInstance(SolrCore.TRANSCRIPTOMICS_EXPERIMENT);
				key.put("filter", String.format("eid:(%s)", StringUtils.join(eIds, " OR ")));

				// top 5 gene modifications
				key.put("keyword", "*:*");
				res = solr.getData(key, null, "{\"facet\":\"mutant,condition\"}", 0, 0, true, false, false);

				JSONObject facets = (JSONObject) res.get("facets");
				JSONObject mutants = (JSONObject) facets.get("mutant");
				JSONObject conditions = (JSONObject) facets.get("condition");

				JSONArray attrs = (JSONArray) mutants.get("attributes");

				int i = 0;
				for (Object attr : attrs) {
					JSONObject j = (JSONObject) attr;

					JSONObject mutant = new JSONObject();
					mutant.put("label", j.get("value"));
					mutant.put("value", j.get("count"));
					data.add(mutant);
					i++;
					if (i > 4) {
						break;
					}
				}
				gm.put("data", data);

				// top 5 experiment conditoins
				data = new JSONArray();
				attrs = (JSONArray) conditions.get("attributes");
				i = 0;
				for (Object attr : attrs) {
					JSONObject j = (JSONObject) attr;

					JSONObject mutant = new JSONObject();
					mutant.put("label", j.get("value"));
					mutant.put("value", j.get("count"));
					data.add(mutant);
					i++;
					if (i > 4) {
						break;
					}
				}
				ec.put("data", data);
			}
			catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}

			popGenome.put("GeneModifications", gm);
			popGenome.put("ExperimentConditions", ec);

			list.add(popGenome);
		}
		if (list.size() > 0) {
			jsonData = new JSONObject();
			jsonData.put("popularList", list);
			jsonData.put("popularTitle", "Select Genomes");
		}
		return jsonData;
	}

	private JSONObject getPathwayECDist() {
		JSONObject jsonData;
		JSONArray series = new JSONArray();

		SolrInterface solr = new SolrInterface();

		for (Integer txId : GENUS_TAXON_IDS) {
			int total = 0;
			List<Integer> distribution = new LinkedList<>();

			// {stat:{field:{field:pathway_id,limit:-1,facet:{ec_count:"unique(ec_number)",genome_count:"unique(genome_id)",genome_ec_count:"unique(genome_ec)"}}}}
			try {
				SolrQuery query = new SolrQuery("annotation:PATRIC");
				query.addFilterQuery(
						SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "genome_status:(Complete OR WGS) AND taxon_lineage_ids:" + txId));
				query.setRows(0).setFacet(true).set("json.facet",
						"{stat:{field:{field:pathway_id,limit:-1,facet:{ec_count:\"unique(ec_number)\",genome_count:\"unique(genome_id)\",genome_ec_count:\"unique(genome_ec)\"}}}}");

				LOGGER.debug("getPathwayECDist: {}", query.toString());
				QueryResponse qr = solr.getSolrServer(SolrCore.PATHWAY).query(query);
				List<SimpleOrderedMap> buckets = (List) ((SimpleOrderedMap) ((SimpleOrderedMap) qr.getResponse().get("facets")).get("stat"))
						.get("buckets");

				int bin1 = 0, bin2 = 0, bin3 = 0, bin4 = 0, bin5 = 0;

				for (SimpleOrderedMap bucket : buckets) {

					double ec_count = ((Integer) bucket.get("ec_count")).doubleValue();
					double genome_count = ((Integer) bucket.get("genome_count")).doubleValue();
					double genome_ec_count = ((Integer) bucket.get("genome_ec_count")).doubleValue();

					long bin = Math.round(genome_ec_count / genome_count / ec_count * 100);

					LOGGER.trace("calculating conservation, ec:{}, genome:{}, genome_ec:{}, bin:{}", ec_count, genome_count, genome_ec_count, bin);
					if (bin < 20) {
						bin5++;
					}
					else if (bin >= 20 && bin < 40) {
						bin4++;
					}
					else if (bin >= 40 && bin < 60) {
						bin3++;
					}
					else if (bin >= 60 && bin < 80) {
						bin2++;
					}
					else if (bin >= 80) {
						bin1++;
					}
					total++;
				}
				distribution.addAll(Arrays.asList(bin1, bin2, bin3, bin4, bin5));
			}
			catch (MalformedURLException | SolrServerException e) {
				LOGGER.error(e.getMessage(), e);
			}

			Taxonomy taxonomy = dataApi.getTaxonomy(txId);

			JSONObject item = new JSONObject();
			item.put("pathogen", taxonomy.getTaxonName());
			item.put("total", total);
			item.put("dist", distribution);

			series.add(item);
		}

		jsonData = new JSONObject();
		jsonData.put("chart_title", "Pathway Conservation in Pathogenic Bacteria");
		jsonData.put("chart_desc",
				"The graph below shows conservation of metabolic pathways across percentage of total genomes in each pathogenic genus.");
		jsonData.put("data", series);

		return jsonData;
	}

	private JSONObject read(String url) {

		JSONObject jsonData = null;

		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			HttpGet httpRequest = new HttpGet(url);
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String response = client.execute(httpRequest, responseHandler);

			JSONParser parser = new JSONParser();
			jsonData = (JSONObject) parser.parse(response);
		}
		catch (IOException | ParseException e) {
			LOGGER.error(e.getMessage(), e);
		}

		return jsonData;
	}

	private Map<String, Integer> getFIGFamStat(int taxonId) {
		Map<String, Integer> stat = new HashMap<>();

		SolrInterface solr = new SolrInterface();

		try {
			LBHttpSolrServer lbHttpSolrServer = solr.getSolrServer(SolrCore.FEATURE);

			SolrQuery query = new SolrQuery("*:*");
			query.addFilterQuery("feature_type:CDS AND annotation:PATRIC");
			query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + taxonId));
			query.setRows(0).setFacet(true).setFacetMinCount(1).setFacetLimit(-1).addFacetField("figfam_id");

			QueryResponse qrTotal = lbHttpSolrServer.query(query);
			int total = qrTotal.getFacetField("figfam_id").getValueCount();

			query.setQuery("product:(hypothetical AND protein)");
			LOGGER.debug("getFIGFamStat(), 1/2, {}", query.toString());
			QueryResponse qrHypo = lbHttpSolrServer.query(query);
			int hypothetical = qrHypo.getFacetField("figfam_id").getValueCount();

			stat.put("total", total);
			stat.put("hypothetical", hypothetical);
			stat.put("functional", (total - hypothetical));

			// counting core vs accessary
			int countCore = 0;
			query.setQuery("*:*");
			query.set("json.facet", "{stat:{field:{field:figfam_id,limit:-1,allBuckets:true,facet:{genome_count:\"unique(genome_id)\"}}}}");

			LOGGER.debug("getFIGFamStat(), 2/2, {}", query.toString());
			QueryResponse qrCore = lbHttpSolrServer.query(query);

			SimpleOrderedMap allBuckets = (SimpleOrderedMap) ((SimpleOrderedMap) ((SimpleOrderedMap) qrCore.getResponse().get("facets")).get("stat"))
					.get("allBuckets");
			double genomeCount = ((Integer) allBuckets.get("genome_count")).doubleValue();
			double cutoff = 0.95 * genomeCount;

			List<SimpleOrderedMap> buckets = (List) ((SimpleOrderedMap) ((SimpleOrderedMap) qrCore.getResponse().get("facets")).get("stat"))
					.get("buckets");

			for (SimpleOrderedMap bucket : buckets) {
				if (((Integer) bucket.get("genome_count")).doubleValue() > cutoff) {
					countCore++;
				}
			}
			stat.put("core", countCore);
			stat.put("accessory", (total - countCore));
		}
		catch (MalformedURLException | SolrServerException e) {
			LOGGER.error(e.getMessage(), e);
		}

		return stat;
	}

	private JSONArray getFIGFamConservationDistribution(int taxonId) {
		JSONArray dist = new JSONArray();

		SolrInterface solr = new SolrInterface();

		try {
			Map<Integer, List<String>> distMap = new LinkedHashMap<>();

			SolrQuery query = new SolrQuery("*:*");
			query.addFilterQuery("feature_type:CDS AND annotation:PATRIC");
			query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + taxonId));
			query.setRows(0).setFacet(true)
					.set("json.facet", "{stat:{field:{field:figfam_id,limit:-1,allBuckets:true,facet:{genome_count:\"unique(genome_id)\"}}}}");

			LOGGER.debug("getFIGFamConservationDistribution(), {}", query.toString());
			QueryResponse qrCore = solr.getSolrServer(SolrCore.FEATURE).query(query);

			SimpleOrderedMap allBuckets = (SimpleOrderedMap) ((SimpleOrderedMap) ((SimpleOrderedMap) qrCore.getResponse().get("facets")).get("stat"))
					.get("allBuckets");
			double totalGenomeCount = ((Integer) allBuckets.get("genome_count")).doubleValue();

			List<SimpleOrderedMap> buckets = (List) ((SimpleOrderedMap) ((SimpleOrderedMap) qrCore.getResponse().get("facets")).get("stat"))
					.get("buckets");

			for (SimpleOrderedMap bucket : buckets) {
				String figfamID = (String) bucket.get("val");
				double genomeCount = ((Integer) bucket.get("genome_count")).doubleValue();
				int groupHash = ((Double) Math.ceil(genomeCount / totalGenomeCount * 10.0d)).intValue();

				LOGGER.trace("group hashing.. {}:{}/{} -> {}", figfamID, genomeCount, totalGenomeCount, groupHash);
				if (distMap.get(groupHash) == null) {
					distMap.put(groupHash, new LinkedList<String>());
				}
				distMap.get(groupHash).add(figfamID);
			}

			for (int i = 1; i <= 10; i++) {
				JSONObject item = new JSONObject();
				if (distMap.get(i) != null && !distMap.get(i).isEmpty()) {
					item.put("x", (i) + "0%");
					item.put("y", distMap.get(i).size());
				}
				else {
					item.put("x", (i) + "0%");
					item.put("y", 0);
				}
				dist.add(item);
			}
		}
		catch (MalformedURLException | SolrServerException e) {
			LOGGER.error(e.getMessage(), e);
		}

		return dist;
	}
}
