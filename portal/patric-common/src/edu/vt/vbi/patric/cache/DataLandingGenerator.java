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
package edu.vt.vbi.patric.cache;

import edu.vt.vbi.patric.beans.Genome;
import edu.vt.vbi.patric.beans.Taxonomy;
import edu.vt.vbi.patric.common.SolrCore;
import edu.vt.vbi.patric.common.SolrInterface;
import edu.vt.vbi.patric.dao.DBPathways;
import edu.vt.vbi.patric.dao.DBSummary;
import edu.vt.vbi.patric.dao.ResultType;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.FacetParams;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class DataLandingGenerator {

	final String baseURL = "http://enews.patricbrc.org";

	final String[] REFERENCE_GENOME_IDS = { "83332.12", "511145.12", "99287.12", "198215.6", "214092.21", "169963.11", "158879.11", "373153.27", "224914.11", "85962.8" };

	final String[] REFERENCE_GENOME_IDS_TRANSCRIPTOMICS = { "83332.12", "511145.12", "99287.12", "208964.12", "214092.21", "169963.11", "158879.11", "373153.27", "224914.11", "85962.8" };

	final Integer[] GENUS_TAXON_IDS = { 1386, 773, 138, 234, 32008, 194, 83553, 1485, 776, 943, 561, 262, 209, 1637, 1763, 780, 590, 620, 1279, 1301,
			662, 629 };

	final String URL_GENOMEOVERVIEW_TAB = "Genome?cType={cType}&cId={cId}";

	final String URL_FEATURETABLE_TAB = "FeatureTable?cType={cType}&cId={cId}&featuretype={featureType}&annotation=PATRIC&filtertype={filterType}";

	final String URL_PROTEINFAMILY_TAB = "FIGfam?cType={cType}&cId={cId}&dm=result&bm=";

	final String URL_PATHWAY_TAB = "CompPathwayTable?cType={cType}&cId={cId}&algorithm=PATRIC&ec_number=#aP0=1&aP1=1&aP2=1&aT=0&alg=RAST&cwEC=false&cwP=true&pId={pId}&pClass=&ecN=";

	final String URL_TRANSCRIPTOMICS_TAB = "ExperimentList?cType={cType}&cId={cId}&kw={kw}";

	final String URL_SINGLE_EXP = "SingleExperiment?cType=taxon&cId=2&eid={eid}";

	final String URL_GENOMEBROWSER = "GenomeBrowser?cType={cType}&cId={cId}&loc=0..10000&tracks=";

	final String URL_PATHWAY_EC_TAB = "CompPathwayTable?cType=genome&cId={cId}&algorithm=PATRIC&ec_number=#aP0=1&aP1=1&aP2=1&aT=1&alg=RAST&cwEC=false&cwP=true&pId={pId}&pClass=&ecN=";

	final String URL_PATHWAY_GENE_TAB = "CompPathwayTable?cType=genome&cId={cId}&algorithm=PATRIC&ec_number=#aP0=1&aP1=1&aP2=1&aT=2&alg=RAST&cwEC=false&cwP=true&pId={pId}&pClass=&ecN=";

	final String ULR_SPECIALTY_GENE_TAB = "SpecialtyGeneList?cType=genome&cId={cId}&kw=source:{source}";

	private static final Logger LOGGER = LoggerFactory.getLogger(DataLandingGenerator.class);

//	public void setBaseURL(String url) {
//		baseURL = url;
//	}

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

		DBSummary conn = new DBSummary();
		SolrInterface solr = new SolrInterface();

		for (Integer txId : GENUS_TAXON_IDS) {
			// TODO: need to implement this with solr
			ResultType stat = conn.getFIGFamStat(txId);
			// Map<String, Integer> stat = getFIGFamStat(txId);

			Taxonomy taxonomy = solr.getTaxonomy(txId);

			JSONObject item = new JSONObject();
			item.put("pathogen", taxonomy.getTaxonName());
			item.put("genomes", taxonomy.getGenomeCount());
			item.put("total", Integer.parseInt(stat.get("total")));
			item.put("functional", Integer.parseInt(stat.get("functional")));
			item.put("hypotheticals", Integer.parseInt(stat.get("hypotheticals")));
			item.put("core", Integer.parseInt(stat.get("core")));
			item.put("accessory", Integer.parseInt(stat.get("accessory")));

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
		DBSummary conn = new DBSummary();
		SolrInterface solr = new SolrInterface();
		List<ResultType> dist;

		for (Integer txId : GENUS_TAXON_IDS) {
			// TODO: implement this with solr
			dist = conn.getFIGFamConservDist(txId);
			Taxonomy taxonomy = solr.getTaxonomy(txId);
			JSONArray data = new JSONArray();

			// initialize map
			Map<Integer, Integer> distMap = new HashMap<>();
			for (int i = 1; i <= 10; i++) {
				distMap.put(i, 0);
			}
			// update map
			for (ResultType pt : dist) {
				distMap.put(Integer.parseInt(pt.get("grp")), Integer.parseInt(pt.get("cnt")));
			}
			// convert map to JSONObject;
			for (int i = 1; i <= 10; i++) {
				JSONObject o = new JSONObject();
				o.put("x", i + "0%");
				o.put("y", distMap.get(i));
				data.add(o);
			}

			JSONObject item = new JSONObject();
			item.put("link", "/portal/portal/patric/FIGfam?cType=taxon&cId=" + txId + "&dm=result&bm=");
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

		SolrInterface solr = new SolrInterface();
		try {
			solr.setCurrentInstance(SolrCore.GENOME);
			JSONObject status = solr.queryFacet("*:*", "genome_status");

			if (status != null) {
				long total = (long) status.get("total");
				JSONArray facet = (JSONArray) status.get("facet");

				JSONArray data = new JSONArray();
				for (Object _f : facet) {
					JSONObject f = (JSONObject) _f;
					JSONObject item = new JSONObject();
					if (f.get("value").equals("WGS")) {
						item.put("label", "Whole Genome Shotgun");
						item.put("m_label", "gsc_shotgun_sequence");
					}
					else if (f.get("value").equals("Complete") || f.get("value").equals("Plasmid")) {
						item.put("label", f.get("value"));
						item.put("m_label", "gsc_" + f.get("value").toString().toLowerCase());
					}
					float percentage = ((long) f.get("count")) * 100.00f / total;
					item.put("value", Math.round(percentage));
					item.put("reported", Math.round(percentage) + "%");

					data.add(item);
				}
				jsonData = new JSONObject();
				jsonData.put("chart_title", "Genome Status");
				jsonData.put("data", data);
			}
		}
		catch (MalformedURLException e) {
			LOGGER.error(e.getMessage(), e);
		}

		return jsonData;
	}

	private JSONObject getGenomeCounts() {
		SolrInterface solr = new SolrInterface();
		String solrServer = solr.getServerUrl(SolrCore.GENOME);
		// TODO: convert to solrJ call, since solrJ now support range.other param
		String cUrl = solrServer
				+ "/select?q=genome_status:Complete&facet=true&facet.range=completion_date&f.completion_date.facet.range.start=2010-01-01T00%3A00%3A00.000Z&f.completion_date.facet.range.end=2015-01-01T00%3A00%3A00.000Z&f.completion_date.facet.range.gap=%2B1YEAR&facet.sort=index&facet.range.other=before&rows=0&wt=json";
		String wUrl = solrServer
				+ "/select?q=genome_status:WGS&facet=true&facet.range=completion_date&f.completion_date.facet.range.start=2010-01-01T00%3A00%3A00.000Z&f.completion_date.facet.range.end=2015-01-01T00%3A00%3A00.000Z&f.completion_date.facet.range.gap=%2B1YEAR&facet.sort=index&facet.range.other=before&rows=0&wt=json";

		JSONObject cRet = read(cUrl);
		JSONObject cFacetCounts = (JSONObject) cRet.get("facet_counts");
		JSONObject cFacetRanges = (JSONObject) cFacetCounts.get("facet_ranges");
		JSONObject cCompletionDates = (JSONObject) cFacetRanges.get("completion_date");

		JSONObject wRet = read(wUrl);
		JSONObject wFacetCounts = (JSONObject) wRet.get("facet_counts");
		JSONObject wFacetRanges = (JSONObject) wFacetCounts.get("facet_ranges");
		JSONObject wCompletionDates = (JSONObject) wFacetRanges.get("completion_date");

		long cBefore = (long) cCompletionDates.get("before");
		long wBefore = (long) wCompletionDates.get("before");
		JSONArray cCounts = (JSONArray) cCompletionDates.get("counts");
		JSONArray wCounts = (JSONArray) wCompletionDates.get("counts");

		JSONArray series = new JSONArray();

		for (int i = 0; i < cCounts.size(); i = i + 2) {
			String year = cCounts.get(i).toString().substring(0, 4);

			long cCount = (long) cCounts.get(i + 1);
			long wCount = (long) wCounts.get(i + 1);

			JSONObject item = new JSONObject();
			item.put("year", Integer.parseInt(year));
			item.put("complete", cBefore + cCount);
			item.put("wgs", wBefore + wCount);
			series.add(item);

			cBefore += cCount;
			wBefore += wCount;
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
				if (type.equals("host_name_f")) {
					jsonData.put("chart_title", "Bacterial Host");
					jsonData.put("chart_desc", "Top 5 Bacterial Hosts");
					jsonData.put("tab_title", "Host");
				}
				else if (type.equals("isolation_country_f")) {
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

		DBPathways connPW = new DBPathways();
		SolrInterface solr = new SolrInterface();

		for (String genomeId : REFERENCE_GENOME_IDS) {
			Map<String, String> key = new HashMap<>();
			key.put("genomeId", genomeId);
			key.put("algorithm", "RAST");

			Genome genome = solr.getGenome(genomeId);

			// construct genome
			JSONObject popGenome = new JSONObject();
			popGenome.put("link", URL_GENOMEOVERVIEW_TAB.replace("{cType}", "genome").replace("{cId}", genomeId));
			popGenome.put("popularName", genome.getGenomeName());
			popGenome.put("gb_link", URL_GENOMEBROWSER.replace("{cType}", "genome").replace("{cId}", genomeId));

			// meta data
			JSONObject meta = new JSONObject();
			meta.put("genome_status", genome.getGenomeStatus());
			meta.put("completion_date", genome.hasCompletionDate()? genome.getCompletionDate(): "");
			meta.put("collection_date", genome.hasCollectionDate()? genome.getCollectionDate(): "");
			meta.put("isolation_country", genome.hasIsolationCountry()? genome.getIsolationCountry(): "");
			meta.put("host_name", genome.hasHostName()? genome.getHostName(): "");
			meta.put("disease", genome.hasDisease()? genome.getDisease(): "");
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
			int cntPathway = connPW.getCompPathwayPathwayCount(key);
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

			Genome genome = solr.getGenome(genomeId);

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
					query.setFacet(true).setFacetMinCount(1).addFacetField("property_source").setFacetSort(FacetParams.FACET_SORT_INDEX);

					QueryResponse qr = solr.getSolrServer(SolrCore.SPECIALTY_GENE_MAPPING).query(query);
					FacetField ff = qr.getFacetField("property_source");
					for (FacetField.Count fc : ff.getValues()) {
						JSONObject sp = new JSONObject();
						String source = fc.getName().split(":")[1].trim();
						sp.put("description", fc.getName().replace(" : ", ": "));
						sp.put("link", ULR_SPECIALTY_GENE_TAB.replace("{cId}", genomeId).replace("{source}", source));
						sp.put("data", fc.getCount());

						specialtyGenes.add(sp);
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
			link.put("link", URL_PATHWAY_TAB.replace("{cType}", "genome").replace("{cId}", genomeId).replace("{pId}", "") );
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
		LBHttpSolrServer lbHttpSolrServer = null;
		try {
			lbHttpSolrServer = solr.getSolrServer(SolrCore.SPECIALTY_GENE_MAPPING);
		}
		catch (MalformedURLException e) {
			LOGGER.error(e.getMessage(), e);
		}

		for (String genomeId : REFERENCE_GENOME_IDS) {

			Genome genome = solr.getGenome(genomeId);

			// construct genome
			JSONObject popGenome = new JSONObject();
			popGenome.put("link", URL_GENOMEOVERVIEW_TAB.replace("{cType}", "genome").replace("{cId}", genomeId));
			popGenome.put("popularName", genome.getGenomeName());

			JSONArray specialtyGenes = new JSONArray();

			try {
				SolrQuery query = new SolrQuery("genome_id:" + genomeId);
				query.setFacet(true).setFacetMinCount(1).addFacetField("property_source").setFacetSort(FacetParams.FACET_SORT_INDEX);

				QueryResponse qr = lbHttpSolrServer.query(query);
				FacetField ff = qr.getFacetField("property_source");
				for (FacetField.Count fc : ff.getValues()) {
					JSONObject sp = new JSONObject();
					String source = fc.getName().split(":")[1].trim();
					sp.put("description", fc.getName());
					sp.put("link", ULR_SPECIALTY_GENE_TAB.replace("{cId}", genomeId).replace("{source}", source));
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

	private JSONObject getPopularGenomesForAntibioticResistanceGene() {
		JSONObject jsonData = null;
		JSONArray list = new JSONArray();

		SolrInterface solr = new SolrInterface();
		LBHttpSolrServer lbHttpSolrServer = null;
		try {
			lbHttpSolrServer = solr.getSolrServer(SolrCore.SPECIALTY_GENE_MAPPING);
		}
		catch (MalformedURLException e) {
			LOGGER.error(e.getMessage(), e);
		}

		for (String genomeId : REFERENCE_GENOME_IDS) {

			Genome genome = solr.getGenome(genomeId);

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
				// TODO: avoid useing property_source
				query.setFacet(true).setFacetMinCount(1).addFacetField("property_source").setFacetSort(FacetParams.FACET_SORT_INDEX);

				QueryResponse qr = lbHttpSolrServer.query(query);
				FacetField ff = qr.getFacetField("property_source");
				for (FacetField.Count fc : ff.getValues()) {
					JSONObject sp = new JSONObject();
					String source = fc.getName().split(":")[1].trim();
					sp.put("description", source);
					sp.put("link", ULR_SPECIALTY_GENE_TAB.replace("{cId}", genomeId).replace("{source}", source));
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
		DBPathways connPW = new DBPathways();
		SolrInterface solr = new SolrInterface();

		HashMap<String, String> sort = new HashMap<>();
		sort.put("field", "ec_count");
		sort.put("direction", "DESC");

		for (String genomeId : REFERENCE_GENOME_IDS) {
			Map<String, String> key = new HashMap<>();
			key.put("genomeId", genomeId);
			key.put("algorithm", "RAST");

			Genome genome = solr.getGenome(genomeId);

			// construct genome
			JSONObject popGenome = new JSONObject();

			popGenome.put("popularName", genome.getGenomeName());
			popGenome.put("link", URL_PATHWAY_TAB.replace("{cType}", "genome").replace("{cId}", genomeId).replace("{pId}", ""));

			JSONArray data = new JSONArray();

			// TODO: implement this with solr
			List<ResultType> items = connPW.getCompPathwayPathwayList(key, sort, 0, 10);
			for (ResultType item : items) {
				JSONObject pathway = new JSONObject();

				pathway.put("name", item.get("pathway_name"));
				pathway.put("name_link",
						URL_PATHWAY_TAB.replace("{cType}", "genome").replace("{cId}", genomeId).replace("{pId}", item.get("pathway_id")));
				pathway.put("class", item.get("pathway_class"));
				pathway.put("gene_count", item.get("gene_count"));
				pathway.put("gene_link", URL_PATHWAY_GENE_TAB.replace("{cId}", genomeId).replace("{pId}", item.get("pathway_id")));
				pathway.put("ec_count", item.get("ec_count"));
				pathway.put("ec_link", URL_PATHWAY_EC_TAB.replace("{cId}", genomeId).replace("{pId}", item.get("pathway_id")));

				data.add(pathway);
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
		LBHttpSolrServer lbHttpSolrServer = null;
		try {
			lbHttpSolrServer = solr.getSolrServer(SolrCore.SPECIALTY_GENE_MAPPING);
		}
		catch (MalformedURLException e) {
			LOGGER.error(e.getMessage(), e);
		}

		for (String genomeId : REFERENCE_GENOME_IDS_TRANSCRIPTOMICS) {
			ResultType key = new ResultType();
			JSONObject res;

			// construct genome node
			JSONObject popGenome = new JSONObject();

			Genome genome = solr.getGenome(genomeId);

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

				for (SolrDocument doc: sdl) {
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

		DBPathways conn = new DBPathways();
		SolrInterface solr = new SolrInterface();

		for (Integer txId : GENUS_TAXON_IDS) {

			// TODO: implement this with solr
			List<Integer> dist = conn.getDistECConservation("taxon", txId.toString());

			Taxonomy taxonomy = solr.getTaxonomy(txId);

			JSONObject item = new JSONObject();
			item.put("pathogen", taxonomy.getTaxonName());

			int total = 0;
			for (Integer v : dist) {
				total += v;
			}
			item.put("total", total);
//			JSONArray jDist = new JSONArray();
//			jDist.addAll(dist);
//			item.put("dist", jDist);
			item.put("dist", dist);

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
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpGet httpRequest = new HttpGet(url);
		JSONObject jsonData = null;

		try {
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String response = httpclient.execute(httpRequest, responseHandler);

			JSONParser parser = new JSONParser();
			jsonData = (JSONObject) parser.parse(response);
		}
		catch (IOException | ParseException e) {
			LOGGER.error(e.getMessage(), e);
		}
		finally {
			httpclient.getConnectionManager().shutdown();
		}
		return jsonData;
	}
/*
	private Map<String, Integer> getFIGFamStat(int taxonId) {
		Map<String, Integer> stat = new HashMap<>();

		SolrInterface solr = new SolrInterface();
		LBHttpSolrServer lbHttpSolrServer = null;
		try {
			lbHttpSolrServer = solr.getSolrServer(SolrCore.FEATURE);
		}
		catch (MalformedURLException e) {
			LOGGER.error(e.getMessage(), e);
		}

		try {
			SolrQuery query = new SolrQuery();
			query.setQuery("*:*");
			query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + taxonId));
			query.setRows(0).setFacet(true).setFacetMinCount(1).setFacetLimit(-1).addFacetField("figfam_id");

			QueryResponse qrTotal = lbHttpSolrServer.query(query);
			int total = qrTotal.getFacetField("figfam_id").getValueCount();

			query.setQuery("product:hypothetical");
			QueryResponse qrHypo = lbHttpSolrServer.query(query);
			int hypothetical = qrHypo.getFacetField("figfam_id").getValueCount();

			stat.put("total", total);
			stat.put("hypothetical", hypothetical);
			stat.put("functional", (total - hypothetical));
		}
		catch (MalformedURLException | SolrServerException e) {
			LOGGER.error(e.getMessage(), e);
		}

		return stat;
	}
*/
}
