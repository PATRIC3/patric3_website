/**
 * This is an AnalyticsQuery API test class
 *
 * @author: Harry Yoo (hyun@vbi.vt.edu)
 */

package org.patricbrc.solr;

import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyAnalytics2QParserPlugin extends QParserPlugin {

	public void init(NamedList params) {
	}

	public QParser createParser(String query, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
		return new MyAnalytics2QueryParser(query, localParams, params, req);
	}

	class MyAnalytics2QueryParser extends QParser {

		public MyAnalytics2QueryParser(String query, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
			super(query, localParams, params, req);
		}

		public Query parse() throws SyntaxError {
			try {
				return new MyAnalytics2Query(localParams, params, req);
			}
			catch (Exception e) {
				throw new SyntaxError(e.getMessage(), e);
			}
		}
	}

	@SuppressWarnings("CanBeFinal")
	class MyAnalytics2Query extends AnalyticsQuery {

		private String field = "rast_cds";

		public MyAnalytics2Query(SolrParams localParams, SolrParams params, SolrQueryRequest request) throws IOException {
		}

		public DelegatingCollector getAnalyticsCollector(ResponseBuilder rb, IndexSearcher indexSearcher) {
			try {
				SolrIndexSearcher searcher = (SolrIndexSearcher) indexSearcher;
				IndexSchema schema = searcher.getSchema();
				SchemaField schemaField = schema.getField(this.field);
				SortedDocValues docValues;

				if (schemaField.hasDocValues()) {
					docValues = searcher.getAtomicReader().getSortedDocValues(this.field);
				}
				else {
					docValues = FieldCache.DEFAULT.getTermsIndex(searcher.getAtomicReader(), this.field);
				}

				SortedDocValues dvGenomeIDs = FieldCache.DEFAULT.getTermsIndex(searcher.getAtomicReader(), "genome_info_id");

				SortedDocValues dvGenomeNames;
				if (schema.getField("common_name").hasDocValues()) {
					dvGenomeNames = searcher.getAtomicReader().getSortedDocValues("common_name");
				}
				else {
					dvGenomeNames = FieldCache.DEFAULT.getTermsIndex(searcher.getAtomicReader(), "common_name");
				}

				return new MyAnalytics2Collector(rb, docValues, dvGenomeIDs, dvGenomeNames);

			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "CanBeFinal" })
	class MyAnalytics2Collector extends DelegatingCollector {
		ResponseBuilder rb;

		int count;

		private SortedDocValues values;

		private SortedDocValues genomeIDs;

		private SortedDocValues genomes;

		private List<String> list;

		public MyAnalytics2Collector(ResponseBuilder rb, SortedDocValues values, SortedDocValues genomeIDs, SortedDocValues genomes) {
			this.rb = rb;
			this.values = values;
			this.genomeIDs = genomeIDs;
			this.genomes = genomes;
			this.list = new ArrayList<>();
		}

		public void collect(int doc) throws IOException {
			++count;

			int ord_value = values.getOrd(doc);
			int ord_genomeID = genomeIDs.getOrd(doc);
			int ord_genome = genomes.getOrd(doc);

			int value = -1;
			int genomeID = -1;
			String genome = null;

			if (ord_value > -1 && ord_genomeID > -1 && ord_genome > -1) {
				value = FieldCache.NUMERIC_UTILS_INT_PARSER.parseInt(values.lookupOrd(ord_value));
				genomeID = FieldCache.NUMERIC_UTILS_INT_PARSER.parseInt(genomeIDs.lookupOrd(ord_genomeID));
				genome = genomes.lookupOrd(ord_genome).utf8ToString();
			}

			if (value > 0 && genomeID > 0 && genome != null) {

				System.out.println(String.format("%d, %d, %s", value, genomeID, genome));

				list.add(value + "," + genomeID + "," + genome);

				delegate.collect(doc);
			}
		}

		public void finish() throws IOException {

			NamedList analytics = new NamedList();
			rb.rsp.add("analytics2", analytics);
			analytics.add("count", count);
			analytics.add("size", list.size());
			analytics.add("targetGenes", list.toString());

			if (this.delegate instanceof DelegatingCollector) {
				((DelegatingCollector) this.delegate).finish();
			}
		}
	}
}