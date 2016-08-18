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
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.util.DateUtil;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@SuppressWarnings("unchecked")
public class DataLandingGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(DataLandingGenerator.class);

	final String baseURL = "http://enews.patricbrc.org";

	final String[] REFERENCE_GENOME_IDS = { "83332.12", "511145.12", "99287.12", "198215.6", "214092.21", "169963.11", "158879.11", "373153.27",
			"224914.11", "85962.8" };

	final String[] REFERENCE_GENOME_IDS_TRANSCRIPTOMICS = { "83332.12", "511145.12", "99287.12", "208964.12", "214092.21", "169963.11", "158879.11",
			"373153.27", "224914.11", "85962.8" };

	final Integer[] GENUS_TAXON_IDS = { 1386, 773, 138, 234, 32008, 194, 810, 1485, 776, 943, 561, 262, 209, 1637, 1763, 780, 590, 620, 1279, 1301,
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

	private ObjectReader jsonReader;

	public DataLandingGenerator() {
		dataApi = new DataApiHandler();

		ObjectMapper objectMapper = new ObjectMapper();
		jsonReader = objectMapper.reader(Map.class);
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
		try (
			PrintWriter jsonOut = new PrintWriter(Files.newBufferedWriter(FileSystems.getDefault().getPath(filePath), Charset.defaultCharset()));
		) {
			jsonData.writeJSONString(jsonOut);
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
		try (
			PrintWriter jsonOut = new PrintWriter(Files.newBufferedWriter(FileSystems.getDefault().getPath(filePath), Charset.defaultCharset()));
		) {
			jsonData.writeJSONString(jsonOut);
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
		try (
			PrintWriter jsonOut = new PrintWriter(Files.newBufferedWriter(FileSystems.getDefault().getPath(filePath), Charset.defaultCharset()));
		) {
			jsonData.writeJSONString(jsonOut);
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
		try (
			PrintWriter jsonOut = new PrintWriter(Files.newBufferedWriter(FileSystems.getDefault().getPath(filePath), Charset.defaultCharset()));
		) {
			jsonData.writeJSONString(jsonOut);
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
		try (
			PrintWriter jsonOut = new PrintWriter(Files.newBufferedWriter(FileSystems.getDefault().getPath(filePath), Charset.defaultCharset()));
		) {
			jsonData.writeJSONString(jsonOut);
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
		try (
			PrintWriter jsonOut = new PrintWriter(Files.newBufferedWriter(FileSystems.getDefault().getPath(filePath), Charset.defaultCharset()));
		) {
			jsonData.writeJSONString(jsonOut);
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
		try (
			PrintWriter jsonOut = new PrintWriter(Files.newBufferedWriter(FileSystems.getDefault().getPath(filePath), Charset.defaultCharset()));
		) {
			jsonData.writeJSONString(jsonOut);
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
		try (
			PrintWriter jsonOut = new PrintWriter(Files.newBufferedWriter(FileSystems.getDefault().getPath(filePath), Charset.defaultCharset()));
		) {
			jsonData.writeJSONString(jsonOut);
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
		// popular genomes
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
		try (
			PrintWriter jsonOut = new PrintWriter(Files.newBufferedWriter(FileSystems.getDefault().getPath(filePath), Charset.defaultCharset()));
		) {
			jsonData.writeJSONString(jsonOut);
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
		JSONArray series = new JSONArray();

		// TODO: reimplement with json facet using sub faceting
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
			LOGGER.debug("getGenomeCount: [{}] {}", SolrCore.GENOME.getSolrCoreName(), queryComplete);

			String apiResponse = dataApi.solrQuery(SolrCore.GENOME, queryComplete);

			Map resp = jsonReader.readValue(apiResponse);
			Map completeFacets = (Map) ((Map) ((Map) resp.get("facet_counts")).get("facet_ranges")).get("completion_date");

			LOGGER.debug("getGenomeCount: [{}] {}", SolrCore.GENOME.getSolrCoreName(), queryWGS);

			apiResponse = dataApi.solrQuery(SolrCore.GENOME, queryWGS);

			resp = jsonReader.readValue(apiResponse);
			Map wgsFacets = (Map) ((Map) ((Map) resp.get("facet_counts")).get("facet_ranges")).get("completion_date");

			int countComplete = (Integer) completeFacets.get("before");
			int countWGS = (Integer) wgsFacets.get("before");

			Map<String, Integer> mapCountComplete = new HashMap<>();
			Map<String, Integer> mapCountWGS = new HashMap<>();

			List listComplete = (List) completeFacets.get("counts");
			for (int i = 0; i < listComplete.size(); i=i+2) {
				countComplete = countComplete + (Integer) listComplete.get(i+1);
				mapCountComplete.put(((String) listComplete.get(i)).substring(0, 4), countComplete);
			}
			List listWGS = (List) wgsFacets.get("counts");
			for (int i = 0; i < listWGS.size(); i=i+2) {
				String year = ((String) listWGS.get(i)).substring(0, 4);

				countWGS = countWGS + (Integer) listWGS.get(i+1);
				mapCountWGS.put(year, countWGS);

				JSONObject item = new JSONObject();
				item.put("year", Integer.parseInt(year));
				item.put("complete", mapCountComplete.get(year));
				item.put("wgs", mapCountWGS.get(year));
				series.add(item);
			}
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

		JSONObject jsonData = new JSONObject();
		jsonData.put("chart_title", "Number of Bacterial Genomes");
		jsonData.put("data", series);

		return jsonData;
	}

	private JSONObject getTop5List(String type) {
		JSONObject jsonData = null;

		try {
			Map facets = dataApi.getFieldFacets(SolrCore.GENOME, "*:*", null, type);
			Map status = (Map) ((Map) facets.get("facets")).get(type);

			if (!status.isEmpty()) {
				JSONArray data = new JSONArray();
				long cntTop = 0, cntSecond = 0;

				int i = 0;
				for (Map.Entry<String, Integer> entry : (Iterable<Map.Entry>) status.entrySet()) {
					String key = entry.getKey();
					int count = entry.getValue();

					JSONObject item = new JSONObject();

					String icon;
					switch (key) {
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
						icon = "/patric/images/flags/" + key.replaceAll(" ", "-") + ".png";
						break;
					}

					item.put("icon", icon);
					item.put("label", key);
					item.put("m_label", key.replaceAll(" ", "_").toLowerCase());
					item.put("value", count);

					data.add(item);
					//
					if (i == 0) {
						cntTop = count;
					}
					else if (i == 1) {
						cntSecond = count;
					}
					else if (i == 4) {
						break;
					}
					i++;
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
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

		return jsonData;
	}

	private JSONObject getTopSpeciesForTranscriptomics() {

		JSONObject jsonData = new JSONObject();
		jsonData.put("title", "TOP 5 Species with Transcriptomics Data");

		JSONArray data = new JSONArray();
		try {
			Map response = dataApi.getFieldFacets(SolrCore.TRANSCRIPTOMICS_EXPERIMENT, "*:*", null, "organism");
			Map facets = (Map) response.get("facets");
			Map organismFacet = (Map) facets.get("organism");

			int i = 0;
			for (Map.Entry<String, Integer> entry : (Iterable<Map.Entry>) organismFacet.entrySet()) {

				JSONObject organism = new JSONObject();
				organism.put("label", entry.getKey());
				organism.put("value", entry.getValue());
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

		Map<String, String> key = new HashMap<>();
		try {
			key.put("keyword", "*:*");

			SolrQuery query = dataApi.buildSolrQuery(key, "[{\"property\":\"release_date\",\"direction\":\"desc\"}]", null, 0, 3, false);

			LOGGER.trace("[{}] {}", SolrCore.TRANSCRIPTOMICS_EXPERIMENT.getSolrCoreName(), query);

			String apiResponse = dataApi.solrQuery(SolrCore.TRANSCRIPTOMICS_EXPERIMENT, query);

			Map resp = jsonReader.readValue(apiResponse);
			Map respBody = (Map) resp.get("response");

			List<Map> docs = (List<Map>) respBody.get("docs");

			for (Map row : docs) {
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

		DataApiHandler dataApi = new DataApiHandler();

		for (String genomeId : REFERENCE_GENOME_IDS) {

			Genome genome = dataApi.getGenome(genomeId);

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

				LOGGER.trace("[{}] {}", SolrCore.PATHWAY.getSolrCoreName(), query);

				String apiResponse = dataApi.solrQuery(SolrCore.PATHWAY, query);

				Map resp = jsonReader.readValue(apiResponse);
				Map facets = (Map) resp.get("facets");
				List<Map> buckets = (List) ((Map) facets.get("stat")).get("buckets");
				Map firstPathway = buckets.get(0);
				cntPathway = (Integer) firstPathway.get("pathway_count");
			}
			catch (IOException e) {
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

				LOGGER.trace("[{}] {}", SolrCore.TRANSCRIPTOMICS_EXPERIMENT.getSolrCoreName(), query);

				String apiResponse = dataApi.solrQuery(SolrCore.TRANSCRIPTOMICS_EXPERIMENT, query);

				Map resp = jsonReader.readValue(apiResponse);
				Map respBody = (Map) resp.get("response");
				numFound = (Integer) respBody.get("numFound");
			}
			catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
			tr.put("picture", "/patric/images/icon-popular-experiment.png");
			tr.put("data", (int) numFound);

			data.add(tr);

			try {
				SolrQuery query = new SolrQuery("figfam_id:[* TO *] AND annotation:PATRIC AND genome_id:" + genomeId);
				query.setRows(0);

				LOGGER.trace("[{}] {}", SolrCore.FEATURE.getSolrCoreName(), query);

				String apiResponse = dataApi.solrQuery(SolrCore.FEATURE, query);

				Map resp = jsonReader.readValue(apiResponse);
				Map respBody = (Map) resp.get("response");

				pf.put("data", respBody.get("numFound"));
			}
			catch (IOException e) {
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
				final String filterCondition = "annotation:PATRIC AND genome_id:" + genomeId;
				Map featureTypeResponse = dataApi.getFieldFacets(SolrCore.FEATURE, "*:*", filterCondition, "feature_type");
				Map featureTypeFacets = (Map) ((Map) featureTypeResponse.get("facets")).get("feature_type");

				// top 5 feature type
				int i = 0;
				for (Map.Entry<String, Integer> entry : (Iterable<Map.Entry>) featureTypeFacets.entrySet()) {
					JSONObject fTypes = new JSONObject();
					fTypes.put("description", entry.getKey());
					fTypes.put("link",
							URL_FEATURETABLE_TAB.replace("{cType}", "genome").replace("{cId}", genomeId).replace("{featureType}", entry.getKey())
									.replace("{filterType}", ""));
					fTypes.put("data", entry.getValue());
					featureTypes.add(fTypes);
					i++;
					if (i > 4) {
						break;
					}
				}

				// Protein Summary
				// hypothetical
				Map hypotheticalResponse = dataApi.getFieldFacets(SolrCore.FEATURE, "product:(hypothetical AND protein) AND feature_type:CDS", filterCondition, "annotation");
				Map hypotheticalFacets = (Map) ((Map) hypotheticalResponse.get("facets")).get("annotation");

				hypotheticalProteins.put("description", "Unknown functions");
				hypotheticalProteins.put("link", URL_FEATURETABLE_TAB.replace("{cType}", "genome").replace("{cId}", genomeId)
						.replace("{featureType}", "CDS").replace("{filterType}", "hypothetical_proteins"));

				for (Map.Entry<String, Integer> entry : (Iterable<Map.Entry>) hypotheticalFacets.entrySet()) {
					hypotheticalProteins.put("data", entry.getValue());
				}
				proteinSummary.add(hypotheticalProteins);

				// functional assigned
				Map functionalResponse = dataApi.getFieldFacets(SolrCore.FEATURE, "!product:(hypothetical AND protein) AND feature_type:CDS", filterCondition, "annotation");
				Map functionalFacets = (Map) ((Map) functionalResponse.get("facets")).get("annotation");

				functionalProteins.put("description", "Functional assignments");
				functionalProteins.put("link",
						URL_FEATURETABLE_TAB.replace("{cType}", "genome").replace("{cId}", genomeId).replace("{featureType}", "CDS")
								.replace("{filterType}", "functional_proteins"));

				for (Map.Entry<String, Integer> entry : (Iterable<Map.Entry>) functionalFacets.entrySet()) {
					functionalProteins.put("data", entry.getValue());
				}
				proteinSummary.add(functionalProteins);

				// ec assigned
				Map ecResponse = dataApi.getFieldFacets(SolrCore.FEATURE, "ec:[* TO *]", filterCondition, "annotation");
				Map ecFacets = (Map) ((Map) ecResponse.get("facets")).get("annotation");

				ecAssignedProteins.put("description", "EC assignments");
				ecAssignedProteins.put("link",
						URL_FEATURETABLE_TAB.replace("{cType}", "genome").replace("{cId}", genomeId).replace("{featureType}", "CDS")
								.replace("{filterType}", "ec"));
				for (Map.Entry<String, Integer> entry : (Iterable<Map.Entry>) ecFacets.entrySet()) {
					ecAssignedProteins.put("data", entry.getValue());
				}
				proteinSummary.add(ecAssignedProteins);

				// go assigned
				Map goResponse = dataApi.getFieldFacets(SolrCore.FEATURE, "go:[* TO *]", filterCondition, "annotation");
				Map goFacets = (Map) ((Map) goResponse.get("facets")).get("annotation");

				goAssignedProteins.put("description", "GO assignments");
				goAssignedProteins.put("link",
						URL_FEATURETABLE_TAB.replace("{cType}", "genome").replace("{cId}", genomeId).replace("{featureType}", "CDS")
								.replace("{filterType}", "go"));

				for (Map.Entry<String, Integer> entry : (Iterable<Map.Entry>) goFacets.entrySet()) {
					goAssignedProteins.put("data", entry.getValue());
				}
				proteinSummary.add(goAssignedProteins);

				// pathway assigned
				Map pathwayResponse = dataApi.getFieldFacets(SolrCore.FEATURE, "pathway:[* TO *]", filterCondition, "annotation");
				Map pathwayFacets = (Map) ((Map) pathwayResponse.get("facets")).get("annotation");

				pathwayAssignedProteins.put("description", "Pathways assignments");
				pathwayAssignedProteins.put("link", URL_FEATURETABLE_TAB.replace("{cType}", "genome").replace("{cId}", genomeId)
						.replace("{featureType}", "CDS").replace("{filterType}", "pathway"));
				for (Map.Entry<String, Integer> entry : (Iterable<Map.Entry>) pathwayFacets.entrySet()) {
					pathwayAssignedProteins.put("data", entry.getValue());
				}
				proteinSummary.add(pathwayAssignedProteins);

				// figfam assigned
				Map figfamResponse = dataApi.getFieldFacets(SolrCore.FEATURE, "figfam_id:[* TO *]", filterCondition, "annotation");
				Map figfamFacets= (Map) ((Map) figfamResponse.get("facets")).get("annotation");

				figfamAssignedProteins.put("description", "FIGfam assignments");
				figfamAssignedProteins.put("link", URL_FEATURETABLE_TAB.replace("{cType}", "genome").replace("{cId}", genomeId)
						.replace("{featureType}", "CDS").replace("{filterType}", "figfam_id"));

				for (Map.Entry<String, Integer> entry : (Iterable<Map.Entry>) figfamFacets.entrySet()) {
					figfamAssignedProteins.put("data", entry.getValue());
				}
				proteinSummary.add(figfamAssignedProteins);

				// Specialty Gene Queries
				Map response = dataApi.getPivotFacets(SolrCore.SPECIALTY_GENE_MAPPING, "genome_id:" + genomeId, null, "property,source");
				Map facets = (Map) response.get("property,source");

				for (Map.Entry<String, Map> entry : (Iterable<Map.Entry>) facets.entrySet()) {
					String property = entry.getKey();

					for (Map.Entry<String, Integer> entrySource : (Iterable<Map.Entry>) (entry.getValue()).entrySet()) {
						String source = entrySource.getKey();

						JSONObject sp = new JSONObject();
						sp.put("description", property + ": " + source);
						sp.put("link", URL_SPECIALTY_GENE_TAB.replace("{cId}", genomeId).replace("{source}", source));
						sp.put("data", entrySource.getValue());

						specialtyGenes.add(sp);
					}
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

		for (String genomeId : REFERENCE_GENOME_IDS) {

			Genome genome = dataApi.getGenome(genomeId);

			// construct genome
			JSONObject popGenome = new JSONObject();
			popGenome.put("link", URL_GENOMEOVERVIEW_TAB.replace("{cType}", "genome").replace("{cId}", genomeId));
			popGenome.put("popularName", genome.getGenomeName());

			JSONArray specialtyGenes = new JSONArray();

			try {
				Map response = dataApi.getPivotFacets(SolrCore.SPECIALTY_GENE_MAPPING, "genome_id:" + genomeId, null, "property,source");

				Map facets = (Map) response.get("property,source");

				for (Map.Entry<String, Map> entry : (Iterable<Map.Entry>) facets.entrySet()) {
					String property = entry.getKey();

					for (Map.Entry<String, Integer> pv : (Iterable<Map.Entry>) entry.getValue().entrySet()) {
						String source = pv.getKey();

						JSONObject sp = new JSONObject();
						sp.put("description", property + ": " + source);
						sp.put("link", URL_SPECIALTY_GENE_TAB.replace("{cId}", genomeId).replace("{source}", source));
						sp.put("data", pv.getValue());

						specialtyGenes.add(sp);
					}
				}
			}
			catch (IOException e) {
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

		for (String genomeId : REFERENCE_GENOME_IDS) {

			Genome genome = dataApi.getGenome(genomeId);

			// construct genome
			JSONObject popGenome = new JSONObject();
			popGenome.put("link", URL_GENOMEOVERVIEW_TAB.replace("{cType}", "genome").replace("{cId}", genomeId));
			popGenome.put("popularName", genome.getGenomeName());

			JSONArray specialtyGenes = new JSONArray();
			// Specialty Gene Queries
			try {
				Map response = dataApi.getFieldFacets(SolrCore.SPECIALTY_GENE_MAPPING, "property:\"Antibiotic Resistance\"", "genome_id:" + genomeId, "source");
				Map facets = (Map) ((Map) response.get("facets")).get("source");

				for (Map.Entry<String, Integer> entry : (Iterable<Map.Entry>) facets.entrySet()) {
					JSONObject sp = new JSONObject();
					String source = entry.getKey();
					sp.put("description", source);
					sp.put("link", URL_SPECIALTY_GENE_TAB.replace("{cId}", genomeId).replace("{source}", source));
					sp.put("data", entry.getValue());

					specialtyGenes.add(sp);
				}
			}
			catch (IOException e) {
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

				LOGGER.debug("getPopularGenomesForPathways: [{}] {}", SolrCore.PATHWAY.getSolrCoreName(), query);

				String apiResponse = dataApi.solrQuery(SolrCore.PATHWAY, query);

				Map resp = jsonReader.readValue(apiResponse);
				Map facets = (Map) resp.get("facets");
				List<Map> buckets = (List) ((Map) facets.get("stat")).get("buckets");

				for (Map bucket : buckets) {
					Map<String, String> pathway = new HashMap<>();
					pathway.put("id", bucket.get("val").toString());
					pathway.put("ec_count", bucket.get("ec_count").toString());
					pathway.put("gene_count", bucket.get("gene_count").toString());

					// getting name
					List<Map> subBuckets = (List) ((Map) bucket.get("field")).get("buckets");
					pathway.put("name", subBuckets.get(0).get("val").toString());

					pathwayList.add(pathway);
				}
			}
			catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}

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

		for (String genomeId : REFERENCE_GENOME_IDS_TRANSCRIPTOMICS) {

			// construct genome node
			JSONObject popGenome = new JSONObject();

			Genome genome = dataApi.getGenome(genomeId);

			popGenome.put("popularName", genome.getGenomeName());
			popGenome.put("link", URL_TRANSCRIPTOMICS_TAB.replace("{cType}", "genome").replace("{cId}", genomeId).replace("{kw}", ""));

			// Retrieve eId associated a given genome
			List<String> eIds = new ArrayList<>();
			try {
				SolrQuery query = new SolrQuery("genome_ids:" + genomeId);
				query.setRows(1000).setFields("eid");

				LOGGER.trace("[{}] {}", SolrCore.TRANSCRIPTOMICS_EXPERIMENT.getSolrCoreName(), query);

				String apiResponse = dataApi.solrQuery(SolrCore.TRANSCRIPTOMICS_EXPERIMENT, query);

				Map resp = jsonReader.readValue(apiResponse);
				Map respBody = (Map) resp.get("response");
				List<Map> sdl = (List) respBody.get("docs");

				for (Map doc : sdl) {
					eIds.add(doc.get("eid").toString());
				}
			}
			catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}

			JSONObject gm = new JSONObject();
			JSONObject ec = new JSONObject();
			gm.put("title", "Gene Modifications");
			ec.put("title", "Experiment Conditions");
			JSONArray data = new JSONArray();
			try {
				Map response = dataApi.getFieldFacets(SolrCore.TRANSCRIPTOMICS_EXPERIMENT, "*:*", "eid:(" + StringUtils.join(eIds, " OR ") + ")", "mutant,condition");
				Map facets = (Map) response.get("facets");

				Map mutantsFacet = (Map) facets.get("mutant");

				int i = 0;
				for (Map.Entry<String, Integer> entry : (Iterable<Map.Entry>) mutantsFacet.entrySet()) {
					JSONObject mutant = new JSONObject();
					mutant.put("label", entry.getKey());
					mutant.put("value", entry.getValue());
					data.add(mutant);
					i++;
					if (i > 4) {
						break;
					}
				}
				gm.put("data", data);

				// top 5 experiment conditoins
				data = new JSONArray();
				Map conditionsFacet = (Map) facets.get("condition");
				i = 0;
				for (Map.Entry<String, Integer> entry : (Iterable<Map.Entry>) conditionsFacet.entrySet()) {
					JSONObject condition = new JSONObject();
					condition.put("label", entry.getKey());
					condition.put("value", entry.getValue());
					data.add(condition);
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

				LOGGER.debug("getPathwayECDist: [{}] {}", SolrCore.PATHWAY.getSolrCoreName(), query);

				String apiResponse = dataApi.solrQuery(SolrCore.PATHWAY, query);

				Map resp = jsonReader.readValue(apiResponse);
				Map facets = (Map) resp.get("facets");
				List<Map> buckets = (List) ((Map) facets.get("stat")).get("buckets");

				int bin1 = 0, bin2 = 0, bin3 = 0, bin4 = 0, bin5 = 0;

				for (Map bucket : buckets) {

					double ec_count = ((Integer) bucket.get("ec_count")).doubleValue();
					double genome_count = ((Integer) bucket.get("genome_count")).doubleValue();
					double genome_ec_count = ((Integer) bucket.get("genome_ec_count")).doubleValue();

					long bin = Math.round(genome_ec_count / genome_count / ec_count * 100);

//					LOGGER.trace("calculating conservation, ec:{}, genome:{}, genome_ec:{}, bin:{}", ec_count, genome_count, genome_ec_count, bin);
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
			catch (IOException e) {
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

		try {
			final String filterCondition = SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + taxonId);

			Map response = dataApi.getFieldFacets(SolrCore.FEATURE, "feature_type:CDS AND annotation:PATRIC", filterCondition, "figfam_id");
			Map figfamFacets = (Map)((Map) response.get("facets")).get("figfam_id");
			int total = figfamFacets.size();

			response = dataApi.getFieldFacets(SolrCore.FEATURE, "product:(hypothetical AND protein)", filterCondition, "figfam_id");
			Map hypotheticalFacets = (Map)((Map) response.get("facets")).get("figfam_id");
			int hypothetical = hypotheticalFacets.size();

			stat.put("total", total);
			stat.put("hypothetical", hypothetical);
			stat.put("functional", (total - hypothetical));

			// counting core vs accessary
			int countCore = 0;
			SolrQuery query = new SolrQuery("*:*").addFilterQuery(filterCondition).setRows(0).setFacet(true);
			query.set("json.facet", "{stat:{field:{field:figfam_id,limit:-1,allBuckets:true,facet:{genome_count:\"unique(genome_id)\"}}}}");

			LOGGER.debug("getFIGFamStat(): [{}] {}", SolrCore.FEATURE.getSolrCoreName(), query);

			String apiResponse = dataApi.solrQuery(SolrCore.FEATURE, query);

			Map resp = jsonReader.readValue(apiResponse);
			Map facets = (Map) resp.get("facets");
			Map allBuckets = (Map) ((Map) facets.get("stat")).get("allBuckets");

			double genomeCount = ((Integer) allBuckets.get("genome_count")).doubleValue();
			double cutoff = 0.95 * genomeCount;

			List<Map> buckets = (List) ((Map) facets.get("stat")).get("buckets");

			for (Map bucket : buckets) {
				if (((Integer) bucket.get("genome_count")).doubleValue() > cutoff) {
					countCore++;
				}
			}
			stat.put("core", countCore);
			stat.put("accessory", (total - countCore));
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

		return stat;
	}

	private JSONArray getFIGFamConservationDistribution(int taxonId) {
		JSONArray dist = new JSONArray();

		try {
			Map<Integer, List<String>> distMap = new LinkedHashMap<>();

			SolrQuery query = new SolrQuery("*:*");
			query.addFilterQuery("feature_type:CDS AND annotation:PATRIC");
			query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + taxonId));
			query.setRows(0).setFacet(true)
					.set("json.facet", "{stat:{field:{field:figfam_id,limit:-1,allBuckets:true,facet:{genome_count:\"unique(genome_id)\"}}}}");

			LOGGER.trace("getFIGFamConservationDistribution(), [{}] {}", SolrCore.FEATURE.getSolrCoreName(), query);

			String apiResponse = dataApi.solrQuery(SolrCore.FEATURE, query);

			Map resp = jsonReader.readValue(apiResponse);
			Map facets = (Map) resp.get("facets");

			Map allBuckets = (Map)((Map) facets.get("stat")).get("allBuckets");
			double totalGenomeCount = ((Integer) allBuckets.get("genome_count")).doubleValue();

			List<Map> buckets = (List<Map>)((Map) facets.get("stat")).get("buckets");

			for (Map bucket : buckets) {
				String figfamID = (String) bucket.get("val");
				double genomeCount = ((Integer) bucket.get("genome_count")).doubleValue();
				int groupHash = ((Double) Math.ceil(genomeCount / totalGenomeCount * 10.0d)).intValue();

//				LOGGER.trace("group hashing.. {}:{}/{} -> {}", figfamID, genomeCount, totalGenomeCount, groupHash);
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
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

		return dist;
	}
}