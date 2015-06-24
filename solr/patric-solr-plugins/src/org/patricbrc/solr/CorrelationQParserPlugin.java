
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
import org.apache.lucene.index.DocValues;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.request.SolrQueryRequest;
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

				return new CorrelationCollector(rb, searcher, srcId, filterCutOff, filterDirection, fieldId, fieldCondition, fieldValue);
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

		private SortedDocValues ids;

		private NumericDocValues conditions;

		private NumericDocValues values;

		private String srcId;

		private double filterCutOff;

		private String filterDirection;

		private String fieldId;

		private String fieldCondition;

		private String fieldValue;

		private Map<StringKey, Float> data;

		private Map<String, Float> referenceData;

		private Set<String> targets;

		public CorrelationCollector(ResponseBuilder rb, SolrIndexSearcher searcher, String srcId, String filterCutOff, String filterDirection, String fieldId, String fieldCondition, String fieldValue)
				throws IOException {

			this.rb = rb;
			this.srcId = srcId;
			this.filterCutOff = Double.parseDouble(filterCutOff);
			this.filterDirection = filterDirection;
			this.fieldId = fieldId;
			this.fieldCondition = fieldCondition;
			this.fieldValue = fieldValue;

			this.ids = DocValues.getSorted(searcher.getLeafReader(), this.fieldId);
			this.conditions = DocValues.getNumeric(searcher.getLeafReader(), this.fieldCondition);
			this.values = DocValues.getNumeric(searcher.getLeafReader(), this.fieldValue);

			this.data = new LinkedHashMap<>();
			this.referenceData = new HashMap<>();
			this.targets =  new HashSet<>();
		}

		@Override
		public void collect(int contextDoc) throws IOException {

			int globalDoc = contextDoc + this.docBase;

			int ordId = ids.getOrd(globalDoc);

			if (ordId > -1) {

				String id = ids.lookupOrd(ordId).utf8ToString();
				int condition = (int) conditions.get(globalDoc);
				float value = Float.intBitsToFloat((int) values.get(globalDoc));

				String strCondition = "" + condition;

//				LOGGER.info("refseq_locus_tag: {}, pid: {}, log_ratio: {}", id, condition, value);

				targets.add(id);
				data.put(new StringKey(id, strCondition), value);
				if (id.equals(srcId)) {
					referenceData.put(strCondition, value);
				}
			}
		}

		@Override
		public void finish() throws IOException {

			ArrayList<NamedList> correlations = new ArrayList<>();
			rb.rsp.add("correlation", correlations);

			long start = System.currentTimeMillis();

			// calculate correlations;
			long cntTarget = 0;
			int referenceDataSize = referenceData.size();
			int conditionCufOff = (int) Math.round(0.8 * referenceDataSize);

			for (String target : targets) {

				RealMatrix matrix = new Array2DRowRealMatrix(referenceDataSize, 2);
				int rows = 0;
				for (Map.Entry<String, Float> entry : referenceData.entrySet()) {
					StringKey strKey = new StringKey(target, entry.getKey());

					if (data.containsKey(strKey)) {
						matrix.addToEntry(rows, 0, entry.getValue());
						matrix.addToEntry(rows, 1, data.get(strKey));
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

						NamedList<Object> entry = new NamedList<>();
						entry.add("id", target);
						entry.add("correlation", coefficient);
						entry.add("conditions", rows);
						entry.add("p_value", p_value);
						cntTarget++;
						correlations.add(entry);
					}
				}
			}

			long end = System.currentTimeMillis();
			DecimalFormat formatter = new DecimalFormat("#,###");

			LOGGER.info("{} ms, for {} with conditions >= {}: {} correlations out of {} datapoints",
					(end - start), srcId, conditionCufOff, cntTarget, formatter.format((long)data.size()));

			super.finish();
		}
	}

	@SuppressWarnings("CanBeFinal")
	class StringKey {
		private final String str1;

		private final String str2;

		@Override
		public boolean equals(Object obj) {
			if (obj == null || !(obj instanceof StringKey)) return false;

			StringKey s = (StringKey) obj;
			return str1.equals(s.str1) && str2.equals(s.str2);
		}

		@Override
		public int hashCode() {
			return str2.concat(str1).hashCode();
		}

		public StringKey(String str1, String str2) {
			this.str1 = str1;
			this.str2 = str2;
		}
	}
}