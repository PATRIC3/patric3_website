
/**
 * CorrelationQParserPlugin returns correlations and condition counts used for the calculation.
 *
 * {!correlation fieldId=refseq_locus_tag fieldCondition=pid fieldValue=log_ratio srcId=Rv2429 filterCutOff=0.4 filterDir=pos cost=101}
 *
 * @author: Harry Yoo (hyun@vbi.vt.edu)
 */

package org.patricbrc.solr;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class CorrelationQParserPlugin extends QParserPlugin {

	private final Logger LOGGER = LoggerFactory.getLogger(CorrelationQParserPlugin.class);

	public void init(NamedList params) {
	}

	public QParser createParser(String query, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
		return new CorrelationQueryParser(query, localParams, params, req);
	}

	class CorrelationQueryParser extends QParser {

		public CorrelationQueryParser(String query, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
			super(query, localParams, params, req);
		}

		public Query parse() throws SyntaxError {
			try {
				// return new CorrelationQuery(localParams, params, req);
				return new CorrelationQuery(localParams);
			}
			catch (Exception e) {
				throw new SyntaxError(e.getMessage(), e);
			}
		}
	}

	@SuppressWarnings("CanBeFinal")
	class CorrelationQuery extends AnalyticsQuery {

		private String fieldValue;

		private String fieldId;

		private String fieldCondition;

		private String srcId;

		private String filterCutOff;

		private String filterDirection;

		// public CorrelationQuery(SolrParams localParams, SolrParams params, SolrQueryRequest request) {
		public CorrelationQuery(SolrParams localParams) {

			this.fieldValue = localParams.get("fieldValue");
			if (this.fieldValue == null) {
				throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Required 'fieldValue' param is missing.");
			}
			this.fieldId = localParams.get("fieldId");
			if (this.fieldId == null) {
				throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Required 'fieldId' param is missing.");
			}
			this.fieldCondition = localParams.get("fieldCondition");
			if (this.fieldCondition == null) {
				throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Required 'fieldCondition' param is missing.");
			}
			this.srcId = localParams.get("srcId");
			if (this.srcId == null) {
				throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Required 'srcId' param is missing.");
			}
			this.filterCutOff = localParams.get("filterCutOff");
			if (this.filterCutOff == null) {
				this.filterCutOff = "0";
			}
			this.filterDirection = localParams.get("filterDir");
			if (this.filterDirection == null) {
				this.filterDirection = "pos";
			}
		}

		public DelegatingCollector getAnalyticsCollector(ResponseBuilder rb, IndexSearcher indexSearcher) {
			try {
				SolrIndexSearcher searcher = (SolrIndexSearcher) indexSearcher;
				IndexSchema schema = searcher.getSchema();
				int segments = searcher.getTopReaderContext().leaves().size();
				SortedDocValues dvIds;
				SortedDocValues dvConditions;
				SortedDocValues dvValues;

				if (schema.getField(this.fieldId).hasDocValues()) {
					dvIds = searcher.getAtomicReader().getSortedDocValues(this.fieldId);
				}
				else {
					dvIds = FieldCache.DEFAULT.getTermsIndex(searcher.getAtomicReader(), this.fieldId);
				}

				if (schema.getField(this.fieldCondition).hasDocValues()) {
					dvConditions = searcher.getAtomicReader().getSortedDocValues(this.fieldCondition);
				}
				else {
					dvConditions = FieldCache.DEFAULT.getTermsIndex(searcher.getAtomicReader(), this.fieldCondition);
				}
				if (schema.getField(this.fieldValue).hasDocValues()) {
					dvValues = searcher.getAtomicReader().getSortedDocValues(this.fieldValue);
				}
				else {
					dvValues = FieldCache.DEFAULT.getTermsIndex(searcher.getAtomicReader(), this.fieldValue);
				}

				return new CorrelationCollector(rb, segments, srcId, filterCutOff, filterDirection, dvIds, dvConditions, dvValues);

			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

//		public MergeStrategy getMergeStrategy() {
//			return null;
//		}
	}

	@SuppressWarnings("CanBeFinal")
	class CorrelationCollector extends DelegatingCollector {
		ResponseBuilder rb;

		private int docBase;

		private AtomicReaderContext[] contexts;

		private SortedDocValues ids;

		private SortedDocValues conditions;

		private SortedDocValues values;

		private String srcId;

		private double filterCutOff;

		private String filterDirection;

		private Map<StringKey, Double> data;

		private Set<String> targets;

		public CorrelationCollector(ResponseBuilder rb, int segments, String srcId, String filterCutOff, String filterDirection, SortedDocValues ids,
				SortedDocValues conditions, SortedDocValues values) {
			this.rb = rb;
			this.contexts = new AtomicReaderContext[segments];

			this.ids = ids;
			this.conditions = conditions;
			this.values = values;

			this.srcId = srcId;
			this.filterCutOff = Double.parseDouble(filterCutOff);
			this.filterDirection = filterDirection;

			this.data = new HashMap<>();
			this.targets = new HashSet<>();
		}

		@Override
		public void setNextReader(AtomicReaderContext context) throws IOException {
			this.contexts[context.ord] = context;
			this.docBase = context.docBase;
		}

		public void collect(int doc) throws IOException {
			int globalDoc = doc + this.docBase;

			int ordId = ids.getOrd(globalDoc);
			int ordCondition = conditions.getOrd(globalDoc);
			int ordValue = values.getOrd(globalDoc);

			if (ordId > -1 && ordCondition > -1 && ordValue > -1) {

				String id = ids.lookupOrd(ordId).utf8ToString();
				String condition = "" + FieldCache.NUMERIC_UTILS_INT_PARSER.parseInt(conditions.lookupOrd(ordCondition));
				double value = (double) FieldCache.NUMERIC_UTILS_FLOAT_PARSER.parseFloat(values.lookupOrd(ordValue));

				targets.add(id);
				data.put(new StringKey(id, condition), value);
			}
			delegate.collect(doc);
		}

		public void finish() throws IOException {

			SolrDocumentList correlations = new SolrDocumentList();
			rb.rsp.add("correlation", correlations);

			long start = System.currentTimeMillis();

			// populate refHash
			Map<String, Double> refHash = new HashMap<>();
			for (Map.Entry<StringKey, Double> entry : data.entrySet()) {
				if (entry.getKey().equalFirstKey(srcId)) {
					refHash.put(entry.getKey().getSecondKey(), entry.getValue());
				}
			}

			// calculate correlations;
			long cntTarget = 0;
			int conditionCufOff = (int) Math.round(0.8 * refHash.size());
			for (String target : targets) {
				RealMatrix matrix = new Array2DRowRealMatrix(refHash.size(), 2);
				Iterator<String> itKeys = refHash.keySet().iterator();
				int rows = 0;
				for (int i = 0; itKeys.hasNext(); i++) {
					String key = itKeys.next();
					StringKey strKey = new StringKey(target, key);

					if (data.containsKey(strKey)) {
						matrix.addToEntry(i, 0, refHash.get(key));
						matrix.addToEntry(i, 1, data.get(strKey));
						rows++;
					}
				}

				if (rows >= conditionCufOff) {
					PearsonsCorrelation correlation = new PearsonsCorrelation(matrix.getSubMatrix(0, rows - 1, 0, 1));

					double coefficient = correlation.getCorrelationMatrix().getEntry(0, 1);
					double p_value = correlation.getCorrelationPValues().getEntry(0, 1);

					LOGGER.debug("{}, # conditions: {}, Correlation coefficient: {}, p_value: {}", target, rows, coefficient, p_value);

					if ((filterDirection.equals("pos") && coefficient >= filterCutOff)
							|| (filterDirection.equals("neg") && coefficient <= (-1) * filterCutOff)) {

						SolrDocument entry = new SolrDocument();
						entry.put("id", target);
						entry.put("correlation", coefficient);
						entry.put("conditions", rows);
						entry.put("p_value", p_value);
						cntTarget++;
						correlations.add(entry);
					}
				}
			}

			correlations.setNumFound(cntTarget);

			long end = System.currentTimeMillis();
			DecimalFormat formatter = new DecimalFormat("#,###");

			LOGGER.info("{} ms, for {} with conditions >= {}: {} correlations out of {} datapoints",
					(end - start), srcId, conditionCufOff, cntTarget, formatter.format((long)data.size()));

			if (this.delegate instanceof DelegatingCollector) {
				((DelegatingCollector) this.delegate).finish();
			}
		}
	}

	@SuppressWarnings("CanBeFinal")
	class StringKey {
		private String str1;

		private String str2;

		@Override
		public boolean equals(Object obj) {
			if (obj != null && obj instanceof StringKey) {
				StringKey s = (StringKey) obj;
				return str1.equals(s.str1) && str2.equals(s.str2);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return (str1 + str2).hashCode();
		}

		public StringKey(String str1, String str2) {
			this.str1 = str1;
			this.str2 = str2;
		}

		public boolean equalFirstKey(String str) {
			return str1.equalsIgnoreCase(str);
		}

		public String getSecondKey() {
			return str2;
		}
	}
}