package edu.vt.vbi.patric.circos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CircosData {

	private String baseUrlSolr;

	private static final Logger logger = LoggerFactory.getLogger(CircosData.class);

	public CircosData() {
		boolean isProduction = System.getProperty("solr.isProduction", "false").equals("true");
		if (isProduction) {
			baseUrlSolr = "http://macleod.vbi.vt.edu:8080/solr/";
		}
		else {
			baseUrlSolr = "http://macleod.vbi.vt.edu:8983/solr/";
		}
	}

	public List<Map<String, Object>> getFeatures(String genome_info_id, String feature_type, String strand, String keyword) {
		List<Map<String, Object>> docs = new LinkedList<Map<String, Object>>();

		Map<String, String> solrQueryByType = new HashMap<>();
		solrQueryByType.put("cds", "feature_type:CDS");
		solrQueryByType.put("rna", "feature_type:*RNA");
		solrQueryByType.put("misc", "!(feature_type:*RNA OR feature_type:CDS OR feature_type:source)");

		SolrQuery query = new SolrQuery();
		query.setQuery("gid:" + genome_info_id + ((keyword != null) ? " AND (" + keyword + ")" : "")
				+ ((strand != null) ? " AND strand:\"" + strand + "\"" : ""));
		query.addFilterQuery("annotation_f:PATRIC AND " + solrQueryByType.get(feature_type));
		query.setFields("accession, start_max, end_min, sequence_info_id, gid, na_feature_id");
		List<SortClause> sorts = new ArrayList<>();
		sorts.add(SortClause.create("accession", SolrQuery.ORDER.asc));
		sorts.add(SortClause.create("start_max", SolrQuery.ORDER.asc));
		query.setSorts(sorts);
		query.setRows(10000);

		logger.info("SolrRequest [DNAFeature]{}", query.toString());
		QueryResponse qr;
		try {
			SolrServer solrServer = new HttpSolrServer(baseUrlSolr + "dnafeature");
			qr = solrServer.query(query);
			SolrDocumentList sdl = qr.getResults();

			for (SolrDocument sd : sdl) {
				HashMap<String, Object> doc = new HashMap<String, Object>();
				doc.put("accession", sd.get("accession"));
				doc.put("start_max", sd.get("start_max"));
				doc.put("end_min", sd.get("end_min"));
				doc.put("sequence_info_id", sd.get("sequence_info_id"));
				doc.put("gid", sd.get("gid"));
				doc.put("na_feature_id", sd.get("na_feature_id"));
				docs.add(doc);
			}
		}
		catch (SolrServerException e) {
			e.printStackTrace();
		}
		return docs;
	}

	public List<Map<String, Object>> getAccessions(String genome_info_id) {
		List<Map<String, Object>> accessions = new LinkedList<Map<String, Object>>();

		SolrQuery query = new SolrQuery();
		query.setQuery("gid:" + genome_info_id);
		query.setFields("genome_name, accession, length, sequence");
		query.setSort("accession", SolrQuery.ORDER.asc);
		query.setRows(10000);

		// logger.info("SolrRequest [SequenceInfo]{}", query.toString());
		QueryResponse qr;
		try {
			SolrServer solrServer = new HttpSolrServer(baseUrlSolr + "sequenceinfo");
			qr = solrServer.query(query);
			SolrDocumentList sdl = qr.getResults();
			for (SolrDocument sd : sdl) {
				HashMap<String, Object> doc = new HashMap<String, Object>();

				doc.put("accession", sd.get("accession"));
				doc.put("length", sd.get("length"));
				doc.put("sequence", sd.get("sequence"));
				accessions.add(doc);
			}
		}
		catch (SolrServerException e) {
			e.printStackTrace();
		}
		return accessions;
	}

	public String getGenomeName(String genome_info_id) {
		String genomeName = null;
		SolrQuery query = new SolrQuery();
		query.setQuery("gid:" + genome_info_id);
		query.setFields("genome_name");

		// logger.info("SolrRequest [GenomeSummary]{}", query.toString());
		QueryResponse qr;
		try {
			SolrServer solrServer = new HttpSolrServer(baseUrlSolr + "genomesummary");
			qr = solrServer.query(query);
			SolrDocumentList sdl = qr.getResults();
			for (SolrDocument sd : sdl) {
				genomeName = sd.get("genome_name").toString();
			}
		}
		catch (SolrServerException e) {
			e.printStackTrace();
		}
		return genomeName;
	}
}
