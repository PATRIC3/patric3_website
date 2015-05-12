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
import edu.vt.vbi.patric.beans.Genome;
import edu.vt.vbi.patric.beans.GenomeFeature;
import edu.vt.vbi.patric.common.*;
import edu.vt.vbi.patric.dao.ResultType;
import edu.vt.vbi.patric.jbrowse.CRFeature;
import edu.vt.vbi.patric.jbrowse.CRResultSet;
import edu.vt.vbi.patric.jbrowse.CRTrack;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.theseed.servers.SAPserver;

import javax.portlet.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class CompareRegionViewer extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(CompareRegionViewer.class);

	private ObjectReader jsonReader;

	@Override
	public void init() throws PortletException {
		super.init();

		ObjectMapper objectMapper = new ObjectMapper();
		jsonReader = objectMapper.reader(Map.class);
	}

	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		SiteHelper.setHtmlMetaElements(request, response, "Compare Region Viewer");

		response.setContentType("text/html");
		PortletRequestDispatcher prd;
		prd = getPortletContext().getRequestDispatcher("/WEB-INF/CRViewer.jsp");
		prd.include(request, response);
	}

	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
		String mode = request.getParameter("mode");

		switch (mode) {
		case "getRefSeqs":
			printRefSeqInfo(request, response);
			break;
		case "getTrackList":
			printTrackList(request, response);
			break;
		case "getTrackInfo":
			printTrackInfo(request, response);
			break;
		case "downloadInExcel":
			exportInExcelFormat(request, response);
			break;
		default:
			response.getWriter().write("wrong param");
			break;
		}
	}

	@SuppressWarnings("unchecked")
	private void printRefSeqInfo(ResourceRequest request, ResourceResponse response) throws IOException {

		String contextType = request.getParameter("cType");
		String contextId = request.getParameter("cId");
		String pinFeatureSeedId = request.getParameter("feature"); // pin feature
		String windowSize = request.getParameter("window"); // window size

		// if pin feature is not given, retrieve from the database based on na_feature_id
		if (pinFeatureSeedId == null && (contextType != null && contextType.equals("feature") && contextId != null)) {

			DataApiHandler dataApi = new DataApiHandler(request);
			GenomeFeature feature = dataApi.getFeature(contextId);
			pinFeatureSeedId = feature.getSeedId();
		}

		if (pinFeatureSeedId != null && !pinFeatureSeedId.equals("") && windowSize != null) {

			JSONObject seq = new JSONObject();
			seq.put("length", (Integer.parseInt(windowSize)));
			seq.put("name", pinFeatureSeedId);
			seq.put("seqDir", "");
			seq.put("start", 1);
			seq.put("end", (Integer.parseInt(windowSize)));
			seq.put("seqChunkSize", 20000);

			JSONArray json = new JSONArray();
			json.add(seq);

			response.setContentType("application/json");
			json.writeJSONString(response.getWriter());
			response.getWriter().close();
		}
		else {
			response.getWriter().write("[]");
		}
	}

	@SuppressWarnings("unchecked")
	private void printTrackList(ResourceRequest request, ResourceResponse response) throws IOException {

		String contextType = request.getParameter("cType");
		String contextId = request.getParameter("cId");
		String pinFeatureSeedId = request.getParameter("feature"); // pin feature
		String windowSize = request.getParameter("window"); // window size

		int _numRegion = Integer.parseInt(request.getParameter("regions")); // number of genomes to compare
		int _numRegion_buffer = 10; // number of genomes to use as a buffer in case that PATRIC has no genome data,
		// which was retrieved from API
		String _key = "";

		DataApiHandler dataApi = new DataApiHandler(request);

		// if pin feature is not given, retrieve from the database based on na_feature_id
		if (pinFeatureSeedId == null && (contextType != null && contextType.equals("feature") && contextId != null)) {

			GenomeFeature feature = dataApi.getFeature(contextId);
			pinFeatureSeedId = feature.getSeedId();
		}

		if (pinFeatureSeedId != null && !pinFeatureSeedId.equals("") && windowSize != null) {
			CRResultSet crRS = null;

			try {
				SAPserver sapling = new SAPserver("http://servers.nmpdr.org/pseed/sapling/server.cgi");
				crRS = new CRResultSet(pinFeatureSeedId,
						sapling.compared_regions(pinFeatureSeedId, _numRegion + _numRegion_buffer, Integer.parseInt(windowSize) / 2));

				long pk = (new Random()).nextLong();
				_key = "" + pk;

				Gson gson = new Gson();
				SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, gson.toJson(crRS, CRResultSet.class));
				SessionHandler.getInstance().set(SessionHandler.PREFIX + "_windowsize" + pk, windowSize);

			}
			catch (Exception ex) {
				LOGGER.error(ex.getMessage(), ex);
			}

			JSONObject trackList = new JSONObject();
			JSONArray tracks = new JSONArray();
			JSONObject trStyle = new JSONObject();
			trStyle.put("className", "feature5");
			trStyle.put("showLabels", false);
			trStyle.put("label", "function( feature ) { return feature.get('seed_id'); }");
			JSONObject trHooks = new JSONObject();
			trHooks.put("modify",
					"function(track, feature, div) { div.style.backgroundColor = ['red','#1F497D','#938953','#4F81BD','#9BBB59','#806482','#4BACC6','#F79646'][feature.get('phase')];}");

			// query genome metadata
			SolrQuery query = new SolrQuery("genome_id:(" + StringUtils.join(crRS.getGenomeIds(), " OR ") + ")");
			query.setFields("genome_id,genome_name,isolation_country,host_name,disease,collection_date,completion_date");
			query.setRows(_numRegion + _numRegion_buffer);

			String apiResponse = dataApi.solrQuery(SolrCore.GENOME, query);
			Map resp = jsonReader.readValue(apiResponse);
			Map respBody = (Map) resp.get("response");

			List<Genome> patricGenomes = dataApi.bindDocuments((List<Map>) respBody.get("docs"), Genome.class);

			int count_genomes = 1;
			if (crRS.getGenomeNames().size() > 0) {
				for (Integer idx : crRS.getTrackMap().keySet()) {
					if (count_genomes > _numRegion) {
						break;
					}
					CRTrack crTrack = crRS.getTrackMap().get(idx);
					Genome currentGenome = null;
					for (Genome genome : patricGenomes) {
						if (genome.getId().equals(crTrack.getGenomeID())) {
							currentGenome = genome;
						}
					}
					if (currentGenome != null) {
						count_genomes++;
						crRS.addToDefaultTracks(crTrack);
						JSONObject tr = new JSONObject();
						tr.put("style", trStyle);
						tr.put("hooks", trHooks);
						tr.put("type", "FeatureTrack");
						tr.put("tooltip",
								"<div style='line-height:1.7em'><b>{seed_id}</b> | {refseq_locus_tag} | {alt_locus_tag} | {gene}<br>{product}<br>{type}:{start}...{end} ({strand_str})<br> <i>Click for detail information</i></div>");
						tr.put("urlTemplate",
								"/portal/portal/patric/CompareRegionViewer/CRWindow?action=b&cacheability=PAGE&mode=getTrackInfo&key=" + _key
										+ "&rowId=" + crTrack.getRowID() + "&format=.json");
						tr.put("key", crTrack.getGenomeName());
						tr.put("label", "CR" + idx);
						tr.put("dataKey", _key);
						JSONObject metaData = new JSONObject();

						if (currentGenome.getIsolationCountry() != null) {
							metaData.put("Isolation Country", currentGenome.getIsolationCountry());
						}
						if (currentGenome.getHostName() != null) {
							metaData.put("Host Name", currentGenome.getHostName());
						}
						if (currentGenome.getDisease() != null) {
							metaData.put("Disease", currentGenome.getDisease());
						}
						if (currentGenome.getCollectionDate() != null) {
							metaData.put("Collection Date", currentGenome.getCollectionDate());
						}
						if (currentGenome.getCompletionDate() != null) {
							metaData.put("Completion Date", currentGenome.getCompletionDate());
						}

						tr.put("metadata", metaData);
						tracks.add(tr);
					}
				}
			}
			trackList.put("tracks", tracks);

			JSONObject facetedTL = new JSONObject();
			JSONArray dpColumns = new JSONArray();
			dpColumns.addAll(Arrays.asList("key", "Isolation Country", "Host Name", "Disease", "Collection Date", "Completion Date"));
			facetedTL.put("displayColumns", dpColumns);
			facetedTL.put("type", "Faceted");
			facetedTL.put("escapeHTMLInData", false);
			trackList.put("trackSelector", facetedTL);
			trackList.put("defaultTracks", crRS.getDefaultTracks());

			response.setContentType("application/json");
			trackList.writeJSONString(response.getWriter());
			response.getWriter().close();
		}
		else {
			response.getWriter().write("{}");
		}
	}

	@SuppressWarnings("unchecked")
	private void printTrackInfo(ResourceRequest request, ResourceResponse response) throws IOException {
		String _rowID = request.getParameter("rowId");
		String pk = request.getParameter("key");

		Gson gson = new Gson();
		String pin_strand = null;
		CRTrack crTrack = null;
		String pseed_ids = null;
		try {
			CRResultSet crRS = gson.fromJson(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk), CRResultSet.class);
			pin_strand = crRS.getPinStrand();
			crTrack = crRS.getTrackMap().get(Integer.parseInt(_rowID));
			pseed_ids = crTrack.getSeedIds();
		}
		catch (Exception e) {
			LOGGER.error(e.getMessage());
			LOGGER.debug("{}", SessionHandler.getInstance().get(SessionHandler.PREFIX + pk));
		}

		int _window_size = 0;
		try {
			_window_size = Integer.parseInt(SessionHandler.getInstance().get(SessionHandler.PREFIX + "_windowsize" + pk));
		}
		catch (Exception e) {
			LOGGER.error(e.getMessage());
			LOGGER.debug("pk:{}, {}", SessionHandler.getInstance().get(SessionHandler.PREFIX + "_windowsize" + pk));
		}

		int features_count = 0;
		try {
			crTrack.relocateFeatures(_window_size, pin_strand);
			Collections.sort(crTrack.getFeatureList());
			features_count = crTrack.getFeatureList().size();
		}
		catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
		}

		Map<String, GenomeFeature> pseedMap = getPSeedMapping(request, pseed_ids);

		// formatting
		JSONArray nclist = new JSONArray();
		CRFeature feature;
		GenomeFeature feature_patric;
		for (int i = 0; i < features_count; i++) {

			feature = crTrack.getFeatureList().get(i);
			feature_patric = pseedMap.get(feature.getfeatureID());

			if (feature_patric != null) {

				JSONArray alist = new JSONArray();

				alist.addAll(Arrays.asList(0, feature.getStartPosition(), feature.getStartString(), feature.getEndPosition(),
						(feature.getStrand().equals("+") ? 1 : -1), feature.getStrand(),

						feature_patric.getId(), feature_patric.getSeedId(), feature_patric.getRefseqLocusTag(),
						feature_patric.getAltLocusTag(), "PATRIC", feature_patric.getFeatureType(), feature_patric.getProduct(),

						feature_patric.getGene(), feature_patric.getGenomeName(), feature_patric.getAccession(), feature.getPhase()));

				nclist.add(alist);
			}
		}
		// formatter.close();

		JSONObject track = new JSONObject();
		track.put("featureCount", features_count);
		track.put("formatVersion", 1);
		track.put("histograms", new JSONObject());

		JSONObject intervals = new JSONObject();
		JSONArray _clses = new JSONArray();
		JSONObject _cls = new JSONObject();
		_cls.put("attributes",
				Arrays.asList("Start", "Start_str", "End", "Strand", "strand_str", "id", "seed_id", "refseq_locus_tag", "alt_locus_tag", "source",
						"type", "product", "gene", "genome_name", "accession", "phase"));
		_cls.put("isArrayAttr", new JSONObject());
		_clses.add(_cls);
		intervals.put("classes", _clses);
		intervals.put("lazyClass", 5);
		intervals.put("minStart", 1);
		intervals.put("maxEnd", 20000);
		intervals.put("urlTemplate", "lf-{Chunk}.json");
		intervals.put("nclist", nclist);
		track.put("intervals", intervals);

		response.setContentType("application/json");
		track.writeJSONString(response.getWriter());
		response.getWriter().close();
	}

	private void exportInExcelFormat(ResourceRequest request, ResourceResponse response) throws IOException {

		String pk = request.getParameter("key");
		Gson gson = new Gson();
		CRResultSet crRS = gson.fromJson(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk), CRResultSet.class);

		CRTrack crTrack;
		CRFeature crFeature;
		String genome_name;

		List<String> _tbl_header = new ArrayList<>();
		List<String> _tbl_field = new ArrayList<>();
		List<ResultType> _tbl_source = new ArrayList<>();

		_tbl_header.addAll(Arrays.asList("Genome Name", "Feature", "Start", "End", "Strand", "FigFam", "Product", "Group"));
		_tbl_field.addAll(Arrays.asList("genome_name", "feature_id", "start", "end", "strand", "figfam_id", "product", "group_id"));

		if (crRS != null && crRS.getGenomeNames().size() > 0) {
			for (Integer idx : crRS.getTrackMap().keySet()) {
				crTrack = crRS.getTrackMap().get(idx);
				genome_name = crTrack.getGenomeName();
				for (Object aCrTrack : crTrack.getFeatureList()) {
					crFeature = (CRFeature) aCrTrack;
					ResultType f = new ResultType();
					f.put("genome_name", genome_name);
					f.put("feature_id", crFeature.getfeatureID());
					f.put("start", crFeature.getStartPosition());
					f.put("end", crFeature.getEndPosition());
					f.put("strand", crFeature.getStrand());
					f.put("figfam_id", crFeature.getFigfam());
					f.put("product", crFeature.getProduct());
					f.put("group_id", crFeature.getGrpNum());
					_tbl_source.add(f);
				}
			}
		}
		// print out to xlsx file
		response.setContentType("application/octetstream");
		response.setProperty("Content-Disposition", "attachment; filename=\"CompareRegionView.xlsx\"");

		OutputStream outs = response.getPortletOutputStream();
		ExcelHelper excel = new ExcelHelper("xssf", _tbl_header, _tbl_field, _tbl_source);
		excel.buildSpreadsheet();
		excel.writeSpreadsheettoBrowser(outs);
	}

	/**
	 * Retrieves features that can be mapped by PSEED peg ID. This is used for CompareRegionViewer to map
	 * features each other.
	 *
	 * @param IDs IDs
	 * @return list of features (na_feature_id, pseed_id, source_id, start, end, strand, na_length, aa_length, product, genome_name, accession)
	 */
	private Map<String, GenomeFeature> getPSeedMapping(ResourceRequest request, String IDs) throws IOException {

		Map<String, GenomeFeature> result = new HashMap<>();

		DataApiHandler dataApi = new DataApiHandler(request);

		SolrQuery query = new SolrQuery("seed_id:(" + IDs + ")");
		query.setFields("feature_id,seed_id,alt_locus_tag,start,end,strand,feature_type,product,gene,refseq_locus_tag,genome_name,accession");
		query.setRows(1000);

		String apiResponse = dataApi.solrQuery(SolrCore.FEATURE, query);
		Map resp = jsonReader.readValue(apiResponse);
		Map respBody = (Map) resp.get("response");

		List<GenomeFeature> features = dataApi.bindDocuments((List<Map>) respBody.get("docs"), GenomeFeature.class);

		for (GenomeFeature feature : features) {
			result.put(feature.getSeedId(), feature);
		}
		return result;
	}
}
