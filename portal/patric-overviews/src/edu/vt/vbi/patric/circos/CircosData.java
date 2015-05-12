package edu.vt.vbi.patric.circos;

import edu.vt.vbi.patric.beans.Genome;
import edu.vt.vbi.patric.beans.GenomeFeature;
import edu.vt.vbi.patric.beans.GenomeSequence;
import edu.vt.vbi.patric.common.DataApiHandler;
import edu.vt.vbi.patric.common.SolrCore;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.PortletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CircosData {

	private DataApiHandler dataApi;

	private ObjectReader jsonReader;

	private static final Logger LOGGER = LoggerFactory.getLogger(CircosData.class);

	public CircosData(PortletRequest request) {
		dataApi = new DataApiHandler(request);
		ObjectMapper objectMapper = new ObjectMapper();
		jsonReader = objectMapper.reader(Map.class);
	}

	public List<Map<String, Object>> getFeatures(String genome_id, String feature_type, String strand, String keyword) throws IOException {
		List<Map<String, Object>> docs = new LinkedList<>();

		Map<String, String> solrQueryByType = new HashMap<>();
		solrQueryByType.put("cds", "feature_type:CDS");
		solrQueryByType.put("rna", "feature_type:*RNA");
		solrQueryByType.put("misc", "!(feature_type:*RNA OR feature_type:CDS OR feature_type:source)");

		SolrQuery query = new SolrQuery();
		query.setQuery("genome_id:" + genome_id + ((keyword != null) ? " AND (" + keyword + ")" : "") + ((strand != null) ?
				" AND strand:\"" + strand + "\"" :
				""));
		query.addFilterQuery("annotation:PATRIC AND " + solrQueryByType.get(feature_type));
		query.setFields("accession,start,end,sequence_id,genome_id,feature_id");

		query.addSort(SortClause.create("accession", SolrQuery.ORDER.asc));
		query.addSort(SortClause.create("start", SolrQuery.ORDER.asc));

		query.setRows(10000);

		LOGGER.trace("getFeatures: [{}] {}", SolrCore.FEATURE.getSolrCoreName(), query.toString());

		String apiResponse = dataApi.solrQuery(SolrCore.FEATURE, query);
		Map resp = jsonReader.readValue(apiResponse);
		Map respBody = (Map) resp.get("response");

		List<GenomeFeature> features = dataApi.bindDocuments((List<Map>) respBody.get("docs"), GenomeFeature.class);

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

		return docs;
	}

	public List<Map<String, Object>> getAccessions(String genome_id) throws IOException {
		List<Map<String, Object>> accessions = new LinkedList<>();

		SolrQuery query = new SolrQuery();
		query.setQuery("genome_id:" + genome_id);
		query.setFields("genome_name,accession,length,sequence");
		query.setSort("accession", SolrQuery.ORDER.asc);
		query.setRows(10000);

		LOGGER.trace("getAccessions: [{}] {}", SolrCore.SEQUENCE.getSolrCoreName(), query.toString());

		String apiResponse = dataApi.solrQuery(SolrCore.SEQUENCE, query);
		Map resp = jsonReader.readValue(apiResponse);
		Map respBody = (Map) resp.get("response");

		List<GenomeSequence> sequences = dataApi.bindDocuments((List<Map>) respBody.get("docs"), GenomeSequence.class);

		for (GenomeSequence sequence : sequences) {
			HashMap<String, Object> doc = new HashMap<>();

			doc.put("accession", sequence.getAccession());
			doc.put("length", sequence.getLength());
			doc.put("sequence", sequence.getSequence());
			accessions.add(doc);
		}

		return accessions;
	}

	public String getGenomeName(String genome_id) throws IOException {
		String genomeName = null;

		SolrQuery query = new SolrQuery();
		query.setQuery("genome_id:" + genome_id);
		query.setFields("genome_name");

		LOGGER.trace("getGenomeName: [{}] {}", SolrCore.GENOME.getSolrCoreName(), query.toString());

		String apiResponse = dataApi.solrQuery(SolrCore.GENOME, query);
		Map resp = jsonReader.readValue(apiResponse);
		Map respBody = (Map) resp.get("response");

		List<Genome> genomes = dataApi.bindDocuments((List<Map>) respBody.get("docs"), Genome.class);


		for (Genome genome : genomes) {
			genomeName = genome.getGenomeName();
		}

		return genomeName;
	}
}
