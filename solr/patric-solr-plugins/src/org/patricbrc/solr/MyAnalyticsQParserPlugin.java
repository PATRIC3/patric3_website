/**
 * This is an AnalyticsQuery API Example drawn from
 *
 * http://heliosearch.org/solrs-new-analyticsquery-api/
 *
 */
package org.patricbrc.solr;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.component.MergeStrategy;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.ShardRequest;
import org.apache.solr.handler.component.ShardResponse;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.*;

import java.io.IOException;

public class MyAnalyticsQParserPlugin extends QParserPlugin {

	public void init(NamedList params) {
	}

	public QParser createParser(String query, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
		return new MyAnalyticsQueryParser(query, localParams, params, req);
	}

	class MyAnalyticsQueryParser extends QParser {

		public MyAnalyticsQueryParser(String query, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
			super(query, localParams, params, req);
		}

		public Query parse() throws SyntaxError {
			return new MyAnalyticsQuery(new MyAnalyticsMergeStrategy());
		}
	}

	class MyAnalyticsQuery extends AnalyticsQuery {

		public MyAnalyticsQuery(MergeStrategy mergeStrategy) {
			super(mergeStrategy);
		}

		public DelegatingCollector getAnalyticsCollector(ResponseBuilder rb, IndexSearcher searcher) {
			return new MyAnalyticsCollector(rb);
		}
	}

	@SuppressWarnings({ "unchecked", "CanBeFinal" })
	class MyAnalyticsCollector extends DelegatingCollector {
		ResponseBuilder rb;

		int count;

		public MyAnalyticsCollector(ResponseBuilder rb) {
			this.rb = rb;
		}

		public void collect(int doc) throws IOException {
			++count;
//			delegate.collect(doc);
		}

		public void finish() throws IOException {
			NamedList analytics = new NamedList();
			rb.rsp.add("analytics", analytics);
			analytics.add("count", count);

			if (this.delegate instanceof DelegatingCollector) {
				((DelegatingCollector) this.delegate).finish();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private class MyAnalyticsMergeStrategy implements MergeStrategy {

		public boolean mergesIds() {
			return false;
		}

		public boolean handlesMergeFields() {
			return false;
		}

		public int getCost() {
			return 100;
		}

		public void handleMergeFields(ResponseBuilder rb, SolrIndexSearcher searcher) {
		}

		public void merge(ResponseBuilder rb, ShardRequest shardRequest) {
			int count = 0;
			NamedList merged = new NamedList();

			for (ShardResponse shardResponse : shardRequest.responses) {
				NamedList response = shardResponse.getSolrResponse().getResponse();
				NamedList analytics = (NamedList) response.get("analytics");
				Integer c = (Integer) analytics.get("count");
				count += c;
			}

			merged.add("count", count);
			rb.rsp.add("analytics", merged);
		}
	}
}