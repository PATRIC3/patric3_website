/*
 * Copyright 2015. Virginia Polytechnic Institute and State University
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package edu.vt.vbi.patric.common;

import edu.vt.vbi.patric.beans.Genome;
import edu.vt.vbi.patric.beans.GenomeFeature;
import edu.vt.vbi.patric.beans.Taxonomy;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.FacetParams;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.PortletRequest;
import java.io.IOException;
import java.util.*;

public class DataApiHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(DataApiHandler.class);

	private String baseUrl;

	private String token;

	public static final int MAX_ROWS = 1000000;

	public static final int timeout = 15 * 60 * 1000;

	private ObjectReader jsonParser;

	private ObjectReader jsonListParser;

	public DataApiHandler() {
		this.token = null;
		this.init();
	}

	public DataApiHandler(PortletRequest request) {
		this.init();

		String sessionId = request.getPortletSession(true).getId();
		try {
			Map session = jsonParser.readValue(SessionHandler.getInstance().get(sessionId));
			if (session.containsKey("authorizationToken")) {
				token = (String) session.get("authorizationToken");
			}
			else {
				token = null;
			}
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	private void init() {
		baseUrl = System.getProperty("dataapi.url", "http://localhost:3001/");
		ObjectMapper objectMapper = new ObjectMapper();
		jsonParser = objectMapper.reader(Map.class);
		jsonListParser = objectMapper.reader(List.class);
	}

	public String solrQuery(SolrCore core, SolrQuery query) {

		String responseBody = null;
		RequestConfig config = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(timeout).setConnectionRequestTimeout(timeout).build();
		try (CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build()) {

			HttpPost request = new HttpPost(baseUrl + core.getSolrCoreName());

			request.setHeader("Accept", "application/solr+json");
			request.setHeader("Content-Type", "application/solrquery+x-www-form-urlencoded");

			if (token != null) {
				request.setHeader("Authorization", token);
			}

			request.setEntity(new StringEntity(query.toString()));

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			responseBody = client.execute(request, responseHandler);

		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

		return responseBody;
	}

	public <T> List bindDocuments(List<Map> docs, Class<T> klass) {

		return new DocumentMapBinder().getBeans(klass, docs);
	}

	public <T> T bindDocument(Map doc, Class<T> klass) {
		return new DocumentMapBinder().getBean(klass, doc);
	}

	public String get(String url) {
		return get(null, url);
	}

	public String get(Map<String, String> headers, String url) {

		String responseBody = null;
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			HttpGet request = new HttpGet(baseUrl + url);

			if (headers != null) {
				for (Map.Entry<String, String> header : headers.entrySet()) {
					request.addHeader(header.getKey(), header.getValue());
				}
			}

			if (headers == null || !headers.containsKey("accept")) {
				request.addHeader("Accept", "application/json");
			}

			if (token != null) {
				request.setHeader("Authorization", token);
			}

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			responseBody = client.execute(request, responseHandler);

		}
		catch (HttpResponseException e) {
			if (e.getMessage().equals("Unauthorized")) {
				// handle unauthorized case
				LOGGER.debug(e.getMessage());
			}
			else {
				LOGGER.error(e.getMessage(), e);
			}
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

		return responseBody;
	}

	public String post(Map<String, String> headers, Map<String, String> body) {

		String responseBody = null;

		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {

			HttpPost request = new HttpPost(baseUrl);

			if (headers != null) {
				for (Map.Entry<String, String> header : headers.entrySet()) {
					request.addHeader(header.getKey(), header.getValue());
				}
			}

			if (headers == null || !headers.containsKey("accept")) {
				request.addHeader("Accept", "application/json");
			}

			if (token != null) {
				request.setHeader("Authorization", token);
			}

			if (body != null && !body.isEmpty()) {
				List<BasicNameValuePair> nvps = new ArrayList<>();

				for (Map.Entry<String, String> pair : body.entrySet()) {
					nvps.add(new BasicNameValuePair(pair.getKey(), pair.getValue()));
				}
				request.setEntity(new UrlEncodedFormEntity(nvps));
			}

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			responseBody = client.execute(request, responseHandler);

		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

		return responseBody;
	}

	public GenomeFeature getFeature(String featureId) {
		GenomeFeature feature = null;

		String response = this.get(SolrCore.FEATURE.getSolrCoreName() + "/" + featureId);
		if (response != null) {
			try {
				Map<String, Object> resp = jsonParser.readValue(response);
				feature = this.bindDocument(resp, GenomeFeature.class);
			}
			catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}

		return feature;
	}

	public Taxonomy getTaxonomy(int taxonId) {
		Taxonomy taxonomy = null;

		String response = this.get(SolrCore.TAXONOMY.getSolrCoreName() + "/" + taxonId);
		if (response != null) {
			try {
				Map<String, Object> resp = jsonParser.readValue(response);
				taxonomy = this.bindDocument(resp, Taxonomy.class);
			}
			catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}

		return taxonomy;
	}

	public Genome getGenome(String genomeId) {
		Genome genome = null;

		String response = this.get(SolrCore.GENOME.getSolrCoreName() + "/" + genomeId);

		if (response != null) {
			try {
				Map<String, Object> resp = jsonParser.readValue(response);
				genome = this.bindDocument(resp, Genome.class);
			}
			catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}

		return genome;
	}

	/**
	 * Retrieve genome by old genome ID (numeric ID used in PATRIC2)
	 *
	 * @param p2GenomeId old genome id
	 * @return Genome
	 */
	public Genome getGenomeByP2GenomeId(int p2GenomeId) {
		Genome genome = null;

		String response = this.get(SolrCore.GENOME.getSolrCoreName() + "/?eq(p2_genome_id," + p2GenomeId + ")");
		if (response != null) {
			try {
				List<Map> result = jsonListParser.readValue(response);
				if (!result.isEmpty()) {
					Map<String, Object> resp = result.get(0);
					genome = this.bindDocument(resp, Genome.class);
				}
			}
			catch (IOException e) {
				LOGGER.error(e.getMessage() + ", while looking up {}", p2GenomeId);
			}
		}

		return genome;
	}

	/**
	 * Retrieve feature by old feature ID (na_feature_id, numeric ID used in PATRIC2)
	 *
	 * @param p2FeatureId old feature id (na_feature_id)
	 * @return GenomeFeature
	 */
	public GenomeFeature getPATRICFeatureByP2FeatureId(int p2FeatureId) {
		GenomeFeature patricFeature = null;

		String response = this.get(SolrCore.FEATURE.getSolrCoreName() + "/?eq(p2_feature_id," + p2FeatureId + ")");
		if (response != null) {
			try {
				LOGGER.trace("parsing: {}", response);
				List<Map> result = jsonListParser.readValue(response);
				if (!result.isEmpty()) {
					Map<String, Object> resp = result.get(0);
					GenomeFeature feature = this.bindDocument(resp, GenomeFeature.class);

					if (feature.getAnnotation().equals("PATRIC")) {
						patricFeature = feature;
					}
					else {
						patricFeature = this.getPATRICFeature(feature.getId());
					}
				}
			}
			catch (IOException e) {
				LOGGER.error(e.getMessage() + ", while looking up {}", p2FeatureId);
			}
		}

		return patricFeature;
	}

	/**
	 * Retrieve PATRIC annotated feature with given featureId, which could be an Id of RefSeq or from other annotation
	 *
	 * @param featureId feature ID
	 * @return GenomeFeature
	 */
	public GenomeFeature getPATRICFeature(String featureId) {
		GenomeFeature patricFeature = null;

		GenomeFeature feature = this.getFeature(featureId);

		if (feature.getAnnotation().equals("PATRIC")) {
			patricFeature = feature;
		}
		else {
			String query = "/?and(eq(pos_group," + feature.getPosGroupEncoded() + "),eq(feature_type," + feature.getFeatureType()
					+ "),eq(annotation,PATRIC))";
			String response = this.get(SolrCore.FEATURE.getSolrCoreName() + query);
			if (response != null) {
				try {
					List<Map> resp = jsonListParser.readValue(response);
					if (!resp.isEmpty()) {
						patricFeature = this.bindDocument(resp.get(0), GenomeFeature.class);
					}
					else {
						patricFeature = feature; // if no corresponding PATRIC feature, return original
					}
				}
				catch (IOException e) {
					LOGGER.error(e.getMessage(), e);
				}
			}
		}

		return patricFeature;
	}

	/**
	 * wrapper function of field facet query
	 *
	 * @param core SolrCore
	 * @param queryParam query condition
	 * @param filterParam filter condition
	 * @param facetFields comma separated list of fields
	 */
	public Map getFieldFacets(SolrCore core, String queryParam, String filterParam, String facetFields) throws IOException {
		Map<String, Object> res = new HashMap<>();
		SolrQuery query = new SolrQuery();

		query.setQuery(queryParam);
		if (filterParam != null) {
			query.addFilterQuery(filterParam);
		}
		query.setRows(0).setFacet(true).setFacetLimit(-1).setFacetMinCount(1).setFacetSort(FacetParams.FACET_SORT_COUNT).set("json.nl", "map");
		query.addFacetField(facetFields.split(","));

		List<String> fields = Arrays.asList(facetFields.split(","));

		LOGGER.trace("getFieldFacets: [{}] {}", core.getSolrCoreName(), query.toString());
		String response = this.solrQuery(core, query);
		Map resp = jsonParser.readValue(response);
		Map facet_fields = (Map) ((Map) resp.get("facet_counts")).get("facet_fields");

		Map<String, Object> facets = new HashMap<>();
		for (String field : fields) {
			Map values = (Map) facet_fields.get(field);
			Map<String, Integer> facetValues = new LinkedHashMap<>();

			for (Map.Entry<String, Integer> entry : (Iterable<Map.Entry>) values.entrySet()) {
				facetValues.put(entry.getKey(), entry.getValue());
			}

			facets.put(field, facetValues);
		}

		res.put("total", ((Map) resp.get("response")).get("numFound"));
		res.put("facets", facets);

		return res;
	}

	/**
	 * wrapper function of pivot facet query
	 *
	 * @param core SolrCore
	 * @param queryParam query condition
	 * @param filterParam filter condition
	 * @param facetFields comma separated list of fields
	 */

	public Map getPivotFacets(SolrCore core, String queryParam, String filterParam, String facetFields) throws IOException {
		Map<String, Object>  res = new HashMap<>();
		SolrQuery query = new SolrQuery();

		query.setQuery(queryParam);
		if (filterParam != null) {
			query.addFilterQuery(filterParam);
		}
		query.setRows(0).setFacet(true).setFacetLimit(-1).setFacetMinCount(1).setFacetSort(FacetParams.FACET_SORT_INDEX);
		query.addFacetPivotField(facetFields);

		LOGGER.trace("getPivotFacets: [{}] {}", core.getSolrCoreName(), query.toString());
		String response = this.solrQuery(core, query);
		Map<String, Object> resp = jsonParser.readValue(response);
		Map facet_fields = (Map) ((Map) resp.get("facet_counts")).get("facet_pivot");
		List<Map> values = (List) facet_fields.get(facetFields);

		Map<String, Object>  facet = new LinkedHashMap<>();

		for (Map value : values) {
			String localKey = value.get("value").toString();
			List<Map> localValues = (List) value.get("pivot");

			Map<String, Integer> pivotValues = new LinkedHashMap<>();
			for (Map local : localValues) {
				pivotValues.put(local.get("value").toString(), (Integer) local.get("count"));
			}
			facet.put(localKey, pivotValues);
		}

		res.put("total", ((Map) resp.get("response")).get("numFound"));
		res.put(facetFields, facet);

		return res;
	}

	public SolrQuery buildSolrQuery(Map<String, String> key, String sorts, String facets, int start, int end, boolean highlight) {

		SolrQuery query = new SolrQuery();
		SolrInterface solr = new SolrInterface();

		// Processing key map
		query.setQuery(StringHelper.stripQuoteAndParseSolrKeywordOperator(key.get("keyword")));

		if (key.containsKey("filter") && key.get("filter") != null) {
			query.addFilterQuery(key.get("filter"));
		}
		if (key.containsKey("filter2") && key.get("filter2") != null) {
			query.addFilterQuery(key.get("filter2"));
		}

		// use SolrJoin if possible
		if (key.containsKey("join")) {
			query.addFilterQuery(key.get("join"));
		}

		if (key.containsKey("fields") && !key.get("fields").equals("")) {
			query.addField(key.get("fields"));
		}

		// sort conditions
		if (sorts != null && !sorts.equals("") && !sorts.equals("[]")) {
			try {
				JSONArray sorter = (JSONArray) new JSONParser().parse(sorts);
				for (Object aSort : sorter) {
					JSONObject jsonSort = (JSONObject) aSort;
					query.addSort(
							SolrQuery.SortClause.create(jsonSort.get("property").toString(), jsonSort.get("direction").toString().toLowerCase()));
				}
			}
			catch (ParseException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}

		// facet conditions
		if (facets != null && !facets.equals("") && !facets.equals("{}")) {
			query.setFacet(true).setFacetMinCount(1).setFacetLimit(-1).setFacetSort(FacetParams.FACET_SORT_COUNT).set("json.nl", "map");

			try {
				JSONObject facetConditions = (JSONObject) new JSONParser().parse(facets);

				if (facetConditions.containsKey("facet.sort")) {
					String facetSort = facetConditions.get("facet.sort").toString();
					if (facetSort.equals("index")) {
						query.setFacetSort(FacetParams.FACET_SORT_INDEX);
					}
				}

				if (facetConditions.containsKey("field_facets")) {
					String[] fieldFacets = facetConditions.get("field_facets").toString().split(",");
					query.addFacetField(fieldFacets);
				}

				if (facetConditions.containsKey("date_range_facets")) {
					String[] dateRangeFacets = facetConditions.get("date_range_facets").toString().split(",");
					for (String field : dateRangeFacets) {
						query.addDateRangeFacet(field, solr.getRangeStartDate(), solr.getRangeEndDate(), solr.getRangeDate());
					}
				}
			}
			catch (ParseException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}

		// start & end
		query.setStart(start); // setting starting index

		if (end == -1) {
			query.setRows(MAX_ROWS);
		}
		else {
			query.setRows(end);
		}

		// highlight
		if (highlight) {
			query.set("hl", "on").set("hl.fl", "*");
		}

		return query;
	}
}
