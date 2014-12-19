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
import edu.vt.vbi.patric.beans.GenomeSequence;
import edu.vt.vbi.patric.common.SiteHelper;
import edu.vt.vbi.patric.common.SolrCore;
import edu.vt.vbi.patric.common.SolrInterface;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.RangeFacet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;

public class GenomeBrowser extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(CompareRegionViewer.class);

	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		new SiteHelper().setHtmlMetaElements(request, response, "Genome Browser");

		response.setContentType("text/html");
		PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/index.jsp");
		prd.include(request, response);
	}

	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
		String mode = request.getParameter("mode");

		switch (mode) {
		case "getRefSeqs":
			printRefSeqInfo(request, response);
			break;
		case "getTrackInfo":
			printTrackInfo(request, response);
			break;
		case "getSequence":
			printSequenceInfo(request, response);
			break;
		case "getHistogram":
			printHistogram(request, response);
			break;
		default:
			response.getWriter().write("wrong param");
			break;
		}
	}

	private void printRefSeqInfo(ResourceRequest request, ResourceResponse response) throws IOException {

		String contextType = request.getParameter("cType");
		String contextId = request.getParameter("cId");

		SolrQuery query = new SolrQuery();

		if (contextType.equals("genome")) {
			query.setQuery("genome_id:" + contextId);
		}
		else if (contextType.equals("feature")) {
			query.setQuery(SolrCore.FEATURE.getSolrCoreJoin("genome_id", "genome_id", "feature_id:" + contextId));
		}
		query.setRows(1000).addSort("accession", SolrQuery.ORDER.asc);

		JSONArray jsonResult = new JSONArray();

		try {
			SolrInterface solr = new SolrInterface();
			QueryResponse qr = solr.getSolrServer(SolrCore.SEQUENCE).query(query);
			List<GenomeSequence> sequences = qr.getBeans(GenomeSequence.class);

			for (GenomeSequence sequence: sequences) {

				JSONObject seq = new JSONObject();
				seq.put("length", sequence.getLength());
				seq.put("name", sequence.getAccession());
				seq.put("accn", sequence.getAccession());
				seq.put("sid", sequence.getId());
				seq.put("start", 0);
				seq.put("end", sequence.getLength());
				seq.put("seqDir", "");
				seq.put("seqChunkSize", sequence.getLength());

				jsonResult.add(seq);
			}

		} catch (MalformedURLException | SolrServerException e) {
			LOGGER.error(e.getMessage(), e);
		}

		response.setContentType("application/json");
		jsonResult.writeJSONString(response.getWriter());
		response.getWriter().close();
	}

	private void printTrackInfo(ResourceRequest request, ResourceResponse response) throws IOException {

		String accession = request.getParameter("accession");
		String annotation = request.getParameter("annotation");

		if (accession != null && annotation != null) {
			SolrInterface solr = new SolrInterface();

			JSONArray nclist = new JSONArray();
			try {
				// Calculate Avg. Feature counts
				List<Integer> histogram = this.getFeatureCountHistogram(accession, annotation);
				Integer sum = 0;
				for (Integer hist: histogram) {
					sum += hist;
				}
				double avgCount = sum.doubleValue() / histogram.size();

				SolrQuery query = new SolrQuery("accession:" + accession + " AND annotation:" + annotation + " AND !(feature_type:source)");
				query.setRows(10000);
				query.addSort("start", SolrQuery.ORDER.asc);

				QueryResponse qr = solr.getSolrServer(SolrCore.FEATURE).query(query);
				List<GenomeFeature> features = qr.getBeans(GenomeFeature.class);

				for (GenomeFeature f: features) {

					JSONArray alist = new JSONArray();

					alist.addAll(Arrays.asList(0,
							(f.getStart() - 1),
							f.getStart(),
							f.getEnd(),
							(f.getStrand().equals("+") ? 1 : -1),
							f.getStrand(),

							f.getId(),
							f.hasSeedId()?f.getSeedId():"",
							f.hasRefseqLocusTag()?f.getRefseqLocusTag():"",
							f.hasAltLocusTag()?f.getAltLocusTag():"",
							annotation,
							f.getFeatureType(),
							f.hasProduct()?f.getProduct():"",

							f.hasGene()?f.getGene():"",
							(f.getFeatureType().equals("CDS") ? 0 : (f.getFeatureType().contains("RNA") ? 1 : 2))
					));

					nclist.add(alist);
				}

//			{
//				"featureCount": <%=features_count %>,
//				"formatVersion": 1,
//				"histograms": {
//					"meta": [{
//						"arrayParams": {
//							"chunkSize": 10000,
//							"length": <%=hist.size()%>,
//							"urlTemplate": "Hist.json.jsp?accession=<%=_accession%>&algorithm=<%=_algorithm%>&chunk={Chunk}&format=.json"
//						},
//						"basesPerBin": "10000"
//					}],
//					"stats": [{
//						"basesPerBin": "10000",
//						"max": <%=(hist.isEmpty())?"0":Collections.max(hist)%>,
//						"mean": <%=hist_avg%>
//					}]
//				},
//				"intervals": {
//					"classes": [{
//						"attributes": [
//							"Start", "Start_str", "End", "Strand", "strand_str",
//							"id", "locus_tag", "source", "type", "product",
//							"gene", "refseq", "phase"],
//						"isArrayAttr": {}
//					}],
//					"count": <%=features_count %>,
//					"lazyClass": 5,
//					"maxEnd": 20000,
//					"minStart": 1,
//					"nclist": [<%=nclist.toString() %>],
//					"urlTemplate": "lf-{Chunk}.json"
//				}
//			}
				JSONObject track = new JSONObject();
				track.put("featureCount", features.size());
				track.put("formatVersion", 1);
				// histogram
					JSONObject histograms = new JSONObject();
					// meta
					JSONArray meta = new JSONArray();
					JSONObject aMeta = new JSONObject();
						// arrayParams
						JSONObject arrayParams = new JSONObject();
						arrayParams.put("chunkSize", 10000);
						arrayParams.put("length", histogram.size());
						arrayParams.put("urlTemplate", "/portal/portal/patric/GenomeBrowser/GBWindow?action=b&cacheability=PAGE&mode=getHistogram&accession=" + accession + "&annotation=" + annotation + "&chunk={Chunk}");
					aMeta.put("arrayParams", arrayParams);
					aMeta.put("basesPerBin", 10000);
					meta.add(aMeta);
					// stats
					JSONArray stats = new JSONArray();
					JSONObject aStat = new JSONObject();
						aStat.put("basesPerBin", 10000);
						aStat.put("max", (histogram.isEmpty()?0:Collections.max(histogram)));
						aStat.put("mean", avgCount);
					stats.add(aStat);

				histograms.put("meta", meta);
				histograms.put("stats", stats);

				// intervals
					JSONObject intervals = new JSONObject();
					// classes
						JSONArray classes = new JSONArray();
						JSONObject aClass = new JSONObject();
							JSONArray attributes = new JSONArray();
							attributes.addAll(Arrays.asList("Start", "Start_str", "End", "Strand", "strand_str", "id", "seed_id", "refseq_locus_tag", "alt_locus_tag", "source", "type", "product", "gene", "phase"));

							aClass.put("attributes", attributes);
							aClass.put("isArrayAttr", new JSONObject());
						classes.add(aClass);
					intervals.put("classes", classes);
					intervals.put("count", features.size());
					intervals.put("lazyClass", 5);
					intervals.put("maxEnd", 20000);
					intervals.put("minStart", 1);
					intervals.put("nclist", nclist);
					intervals.put("urlTemplate", "lf-{Chunk}.json");

				track.put("histograms", histograms);
				track.put("intervals", intervals);

				// print track info
				response.setContentType("application/json");
				track.writeJSONString(response.getWriter());
			}
			catch (MalformedURLException | SolrServerException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

	private void printSequenceInfo(ResourceRequest request, ResourceResponse response) throws IOException {

		String sequenceId = request.getParameter("sequence_id");

		SolrInterface solr = new SolrInterface();
		String SequenceString = null;
		try {
			SolrQuery query = new SolrQuery("sequence_id:" + sequenceId);
			query.setFields("sequence");

			QueryResponse qr = solr.getSolrServer(SolrCore.SEQUENCE).query(query);
			List<GenomeSequence> sequences = qr.getBeans(GenomeSequence.class);

			for (GenomeSequence sequence: sequences) {
				SequenceString = sequence.getSequence();
			}
		} catch (MalformedURLException | SolrServerException e) {
			LOGGER.error(e.getMessage(), e);
		}

		response.getWriter().println(SequenceString);
	}

	private void printHistogram(ResourceRequest request, ResourceResponse response) throws IOException {
		String accession = request.getParameter("accession");
		String annotation = request.getParameter("annotation");

		if (accession != null && annotation != null) {
			List<Integer> histogram = getFeatureCountHistogram(accession, annotation);

			response.getWriter().write(histogram.toString());
		}
	}

	private List<Integer> getFeatureCountHistogram(String accession, String annotation) {

		SolrQuery query = new SolrQuery("accession:" + accession);
		query.setFilterQueries("annotation:" + annotation + " AND !(feature_type:source)");
		query.setRows(0);
		query.setFacet(true);
		query.setFacetMinCount(1);
		query.addNumericRangeFacet("start", 0, 10000000, 10000);

		List<Integer> results = new ArrayList<>();
		SolrInterface solr = new SolrInterface();

		try {
			QueryResponse qr = solr.getSolrServer(SolrCore.FEATURE).query(query);

			for (RangeFacet range : qr.getFacetRanges()) {
				List<RangeFacet.Count> rangeEntries = range.getCounts();
				if (rangeEntries != null) {
					for (RangeFacet.Count count : rangeEntries) {
						results.add(count.getCount());
					}
				}
			}
		}
		catch (MalformedURLException | SolrServerException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return results;
	}
}
