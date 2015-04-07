package edu.vt.vbi.patric.circos;

import edu.vt.vbi.patric.beans.Genome;
import edu.vt.vbi.patric.beans.GenomeFeature;
import edu.vt.vbi.patric.beans.GenomeSequence;
import edu.vt.vbi.patric.common.SolrCore;
import edu.vt.vbi.patric.common.SolrInterface;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CircosData {

	SolrInterface solr;

	private static final Logger LOGGER = LoggerFactory.getLogger(CircosData.class);

	public CircosData() {
		solr = new SolrInterface();
	}

	public List<Map<String, Object>> getFeatures(String genome_id, String feature_type, String strand, String keyword) {
		List<Map<String, Object>> docs = new LinkedList<>();

		Map<String, String> solrQueryByType = new HashMap<>();
		solrQueryByType.put("cds", "feature_type:CDS");
		solrQueryByType.put("rna", "feature_type:*RNA");
		solrQueryByType.put("misc", "!(feature_type:*RNA OR feature_type:CDS OR feature_type:source)");

		SolrQuery query = new SolrQuery();
		query.setQuery("genome_id:" + genome_id + ((keyword != null) ? " AND (" + keyword + ")" : "")
				+ ((strand != null) ? " AND strand:\"" + strand + "\"" : ""));
		query.addFilterQuery("annotation:PATRIC AND " + solrQueryByType.get(feature_type));
		query.setFields("accession,start,end,sequence_id,genome_id,feature_id");

		query.addSort(SortClause.create("accession", SolrQuery.ORDER.asc));
		query.addSort(SortClause.create("start", SolrQuery.ORDER.asc));

		query.setRows(10000);

		LOGGER.debug("{}", query.toString());

		try {
			QueryResponse qr = solr.getSolrServer(SolrCore.FEATURE).query(query);

			List<GenomeFeature> features = qr.getBeans(GenomeFeature.class);

			for (GenomeFeature feature : features) {
				HashMap<String, Object> doc = new HashMap<>();
				doc.put("accession", feature.getAccession());
				doc.put("start", feature.getStart());
				doc.put("end", feature.getEnd());
				doc.put("sequence_id", feature.getSequenceId());
				doc.put("genome_id", feature.getGenomeId());
				doc.put("feature_id", feature.getId());
				docs.add(doc);
			}
		}
		catch (MalformedURLException | SolrServerException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return docs;
	}

	public List<Map<String, Object>> getAccessions(String genome_id) {
		List<Map<String, Object>> accessions = new LinkedList<>();

		SolrQuery query = new SolrQuery();
		query.setQuery("genome_id:" + genome_id);
		query.setFields("genome_name,accession,length,sequence");
		query.setSort("accession", SolrQuery.ORDER.asc);
		query.setRows(10000);

		LOGGER.debug("SolrRequest [SequenceInfo]{}", query.toString());

		try {
			QueryResponse qr = solr.getSolrServer(SolrCore.SEQUENCE).query(query);
			List<GenomeSequence> sequences = qr.getBeans(GenomeSequence.class);
			for (GenomeSequence sequence : sequences) {
				HashMap<String, Object> doc = new HashMap<>();

				doc.put("accession", sequence.getAccession());
				doc.put("length", sequence.getLength());
				doc.put("sequence", sequence.getSequence());
				accessions.add(doc);
			}
		}
		catch (MalformedURLException | SolrServerException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return accessions;
	}

	public String getGenomeName(String genome_id) {
		String genomeName = null;

		SolrQuery query = new SolrQuery();
		query.setQuery("genome_id:" + genome_id);
		query.setFields("genome_name");

		LOGGER.debug("{}", query.toString());

		try {
			QueryResponse qr = solr.getSolrServer(SolrCore.GENOME).query(query);

			List<Genome> genomes = qr.getBeans(Genome.class);
			for (Genome genome : genomes) {
				genomeName = genome.getGenomeName();
			}
		}
		catch (MalformedURLException | SolrServerException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return genomeName;
	}
}
