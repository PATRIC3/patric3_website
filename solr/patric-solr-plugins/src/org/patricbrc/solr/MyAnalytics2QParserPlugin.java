/**
 * This is an AnalyticsQuery API test class
 *
 * @author: Harry Yoo (hyun@vbi.vt.edu)
 */

package org.patricbrc.solr;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DocValues;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.queries.function.valuesource.SimpleFloatFunction;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.uninverting.UninvertingReader;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyAnalytics2QParserPlugin extends QParserPlugin {

	public void init(NamedList params) {
	}

	public QParser createParser(String query, SolrParams localParams, SolrParams params, SolrQueryRequest request) {
		return new MyAnalytics2QueryParser(query, localParams, params, request);
	}

	class MyAnalytics2QueryParser extends QParser {

		public MyAnalytics2QueryParser(String query, SolrParams localParams, SolrParams params, SolrQueryRequest request) {
			super(query, localParams, params, request);
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

		public MyAnalytics2Query(SolrParams localParams, SolrParams params, SolrQueryRequest request) throws IOException {
		}

		public DelegatingCollector getAnalyticsCollector(ResponseBuilder rb, IndexSearcher indexSearcher) {
			try {
				SolrIndexSearcher searcher = (SolrIndexSearcher) indexSearcher;

				return new MyAnalytics2Collector(rb, searcher);

			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "CanBeFinal" })
	class MyAnalytics2Collector extends DelegatingCollector {
		private ResponseBuilder rb;

		private int count;

		private SortedDocValues genomeIds;

		private SortedDocValues genomeNames;

		private UninvertingReader uninvertingReader;

		private List<String> list;

		public final Logger logger = LoggerFactory.getLogger(MyAnalytics2Collector.class);

		public MyAnalytics2Collector(ResponseBuilder rb, SolrIndexSearcher searcher) throws IOException {

			Map<String, UninvertingReader.Type> mapping = new HashMap<>();
			mapping.put("patric_cds", UninvertingReader.Type.INTEGER);
			mapping.put("gc_content", UninvertingReader.Type.FLOAT);
			uninvertingReader = new UninvertingReader(searcher.getLeafReader(), mapping);

			genomeIds = DocValues.getSorted(searcher.getLeafReader(), "genome_id");
			genomeNames = DocValues.getSorted(searcher.getLeafReader(), "genome_name");

			this.rb = rb;
			this.list = new ArrayList<>();
		}

		public void collect(int doc) throws IOException {
			++count;

			// float gc = floats.get(doc);
			float gc = uninvertingReader.document(doc).getField("gc_content").numericValue().floatValue();
			int value = uninvertingReader.document(doc).getField("gc_content").numericValue().intValue();
			int ord_genomeID = genomeIds.getOrd(doc);
			int ord_genome = genomeNames.getOrd(doc);

			String genomeId = null;
			String genomeName = null;

			if (ord_genomeID > -1 && ord_genome > -1) {
				genomeId = genomeIds.lookupOrd(ord_genomeID).utf8ToString();
				genomeName = genomeNames.lookupOrd(ord_genome).utf8ToString();
			}

			if (value > 0 && genomeId != null) {

				logger.info("patric_cds: {}, gc_content: {}, genome_id: {}, genome_name: {}", value, gc, genomeId, genomeName);

				list.add("patric_cds: " + value + ", gc_content: " + gc + ", genome_id: " + genomeId + ", genome_name: " + genomeName);
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