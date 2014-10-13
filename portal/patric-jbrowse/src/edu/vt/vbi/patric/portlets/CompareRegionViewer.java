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

import edu.vt.vbi.patric.beans.Genome;
import edu.vt.vbi.patric.beans.GenomeFeature;
import edu.vt.vbi.patric.common.ExcelHelper;
import edu.vt.vbi.patric.common.SiteHelper;
import edu.vt.vbi.patric.common.SolrCore;
import edu.vt.vbi.patric.common.SolrInterface;
import edu.vt.vbi.patric.dao.DBSummary;
import edu.vt.vbi.patric.dao.ResultType;
import edu.vt.vbi.patric.jbrowse.CRFeature;
import edu.vt.vbi.patric.jbrowse.CRResultSet;
import edu.vt.vbi.patric.jbrowse.CRTrack;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.theseed.servers.SAPserver;

import javax.portlet.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.*;

public class CompareRegionViewer extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(CompareRegionViewer.class);

	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		new SiteHelper().setHtmlMetaElements(request, response, "Compare Region Viewer");

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

			SolrInterface solr = new SolrInterface();
			GenomeFeature feature = solr.getFeature(contextId);
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

		// DBSummary conn_summary = new DBSummary();
		SolrInterface solr = new SolrInterface();
		PortletSession session = request.getPortletSession(true);

		// if pin feature is not given, retrieve from the database based on na_feature_id
		if (pinFeatureSeedId == null && (contextType != null && contextType.equals("feature") && contextId != null)) {

			GenomeFeature feature = solr.getFeature(contextId);
			pinFeatureSeedId = feature.getSeedId();
		}

		if (pinFeatureSeedId != null && !pinFeatureSeedId.equals("") && windowSize != null) {
			CRResultSet crRS = null;

			try {
				SAPserver sapling = new SAPserver("http://servers.nmpdr.org/pseed/sapling/server.cgi");
				crRS = new CRResultSet(pinFeatureSeedId, sapling.compared_regions(pinFeatureSeedId, _numRegion + _numRegion_buffer, Integer.parseInt(windowSize) / 2));

				Random g = new Random();
				int random = g.nextInt();
				_key = "key" + random;
				session.setAttribute(_key, crRS);
				session.setAttribute("window_size", windowSize);
			}
			catch (Exception ex) {
				LOGGER.error(ex.getMessage(), ex);
			}

			JSONObject trackList = new JSONObject();
			JSONArray tracks = new JSONArray();
			JSONObject trStyle = new JSONObject();
			trStyle.put("className", "feature5");
			trStyle.put("showLabels", false);
			trStyle.put("label", "function( feature ) { return feature.get('locus_tag'); }");
			JSONObject trHooks = new JSONObject();
			trHooks.put(
					"modify",
					"function(track, feature, div) { div.style.backgroundColor = ['red','#1F497D','#938953','#4F81BD','#9BBB59','#806482','#4BACC6','#F79646'][feature.get('phase')];}");

			// query genome metadata
			// Map<String, ResultType> gMetaData = conn_summary.getGenomeMetadata(crRS.getGenomeNames());
			List<Genome> patricGenomes = null;
			try {
				solr.setCurrentInstance(SolrCore.GENOME);

				SolrQuery query = new SolrQuery("genome_id:(" + StringUtils.join(crRS.getGenomeIds(), " OR ") + ")");
				query.setFields("genome_id,genome_name,isolation_country,host_name,disease,collection_date,completion_date");
				query.setRows(_numRegion + _numRegion_buffer);

				QueryResponse qr = solr.getServer().query(query);
				patricGenomes = qr.getBeans(Genome.class);

			} catch (MalformedURLException | SolrServerException e) {
				LOGGER.error(e.getMessage(), e);
			}

			int count_genomes = 1;
			if (crRS.getGenomeNames().size() > 0) {
				for (Integer idx : crRS.keySet()) {
					if (count_genomes > _numRegion) {
						break;
					}
					CRTrack crTrack = crRS.get(idx);
					Genome currentGenome = null;
					for (Genome genome: patricGenomes) {
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
								"<div style='line-height:1.7em'><b>{locus_tag}</b> | {refseq} | {gene}<br>{product}<br>{type}:{start}...{end} ({strand_str})<br> <i>Click for detail information</i></div>");
						tr.put("urlTemplate", "/portal/portal/patric/CompareRegionViewer/CRWindow?action=b&cacheability=PAGE&mode=getTrackInfo&key="
								+ _key + "&rowId=" + crTrack.getRowID() + "&format=.json");
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
		String _key = request.getParameter("key");

		PortletSession session = request.getPortletSession(true);
		CRResultSet crRS = (CRResultSet) session.getAttribute(_key);
		int _window_size = Integer.parseInt(session.getAttribute("window_size").toString());

		String pin_strand = crRS.getPinStrand();
		CRTrack crTrack = crRS.get(Integer.parseInt(_rowID));
		String pseed_ids = crTrack.getSeedIds();

		int features_count = 0;
		try {
			crTrack.relocateFeatures(_window_size, pin_strand);
			Collections.sort(crTrack);
			features_count = crTrack.size();
		}
		catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
		}

		DBSummary conn_summary = new DBSummary();
		Map<String, ResultType> pseedMap = conn_summary.getPSeedMapping("seed", pseed_ids);

		// formatting
		JSONArray nclist = new JSONArray();
		CRFeature feature;
		ResultType feature_patric;
		for (int i = 0; i < features_count; i++) {

			feature = crTrack.get(i);
			feature_patric = pseedMap.get(feature.getfeatureID());

			if (feature_patric != null) {

				JSONArray alist = new JSONArray();

				alist.addAll(Arrays.asList(0,
						feature.getStartPosition(),
						feature.getStartString(),
						feature.getEndPosition(),
						(feature.getStrand().equals("+") ? 1 : -1),
						feature.getStrand(),

						feature_patric.get("feature_id"),
						feature_patric.get("alt_locus_tag"),
						"PATRIC",
						feature_patric.get("feature_type"),
						feature_patric.get("product"),

						feature_patric.get("gene"),
						feature_patric.get("refseq_locus_tag"),
						feature_patric.get("genome_name"),
						feature_patric.get("accession"),
						feature.getPhase()
				));

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
		_cls.put("attributes", Arrays.asList("Start", "Start_str", "End", "Strand", "strand_str", "id", "locus_tag", "source", "type", "product",
				"gene", "refseq", "genome_name", "accession", "phase"));
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
		PortletSession session = request.getPortletSession(true);
		String _key = request.getParameter("key");
		CRResultSet crRS = (CRResultSet) session.getAttribute(_key);

		CRTrack crTrack;
		CRFeature crFeature;
		String genome_name;

		List<String> _tbl_header = new ArrayList<>();
		List<String> _tbl_field = new ArrayList<>();
		List<ResultType> _tbl_source = new ArrayList<>();

		_tbl_header.addAll(Arrays.asList("Genome Name", "Feature", "Start", "End", "Strand", "FigFam", "Product", "Group"));
		_tbl_field.addAll(Arrays.asList("genome_name", "feature_id", "start", "end", "strand", "figfam_id", "product", "group_id"));

		if (crRS != null && crRS.getGenomeNames().size() > 0) {
			for (Integer idx : crRS.keySet()) {
				crTrack = crRS.get(idx);
				genome_name = crTrack.getGenomeName();
				for (Object aCrTrack : crTrack) {
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
}
