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
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.solr.client.solrj.SolrQuery;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.PortletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataApiHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(DataApiHandler.class);

	private String baseUrl;

	private String token;

	private ObjectReader jsonParser;

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
	}

	public String solrQuery(SolrCore core, SolrQuery query) {

		String responseBody = null;
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {

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
				feature = this.bindDocument((Map) resp.get("doc"), GenomeFeature.class);
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
				taxonomy = this.bindDocument((Map) resp.get("doc"), Taxonomy.class);
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
				genome = this.bindDocument((Map) resp.get("doc"), Genome.class);
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
	 * @param p2GenomeId
	 * @return
	 */
	public Genome getGenomeByP2GenomeId(int p2GenomeId) {
		Genome genome = null;

		String response = this.get(SolrCore.GENOME.getSolrCoreName() + "/?eq(p2_genome_id," + p2GenomeId + ")");
		if (response != null) {
			try {
				Map<String, Object> resp = jsonParser.readValue(response);
				genome = this.bindDocument((Map) resp.get("doc"), Genome.class);
			}
			catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}

		return genome;
	}

	/**
	 * Retrieve feature by old feature ID (na_feature_id, numeric ID used in PATRIC2)
	 *
	 * @param p2FeatureId
	 * @return
	 */
	public GenomeFeature getPATRICFeatureByP2FeatureId(int p2FeatureId) {
		GenomeFeature patricFeature = null;

		String response = this.get(SolrCore.FEATURE.getSolrCoreName() + "/?eq(p2_feature_id," + p2FeatureId + ")");
		if (response != null) {
			try {
				Map<String, Object> resp = jsonParser.readValue(response);
				GenomeFeature feature = this.bindDocument((Map) resp.get("doc"), GenomeFeature.class);

				if (feature.getAnnotation().equals("PATRIC")) {
					patricFeature = feature;
				}
				else {
					patricFeature = this.getPATRICFeature(feature.getId());
				}
			}
			catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}

		return patricFeature;
	}

	/**
	 * Retrieve PATRIC annotated feature with given featureId, which could be an Id of RefSeq or from other annotation
	 *
	 * @param featureId
	 * @return
	 */
	public GenomeFeature getPATRICFeature(String featureId) {
		GenomeFeature patricFeature = null;

		GenomeFeature feature = this.getFeature(featureId);

		if (feature.getAnnotation().equals("PATRIC")) {
			patricFeature = feature;
		}
		else {
			String response = this
					.get(SolrCore.FEATURE.getSolrCoreName() + "/?eq(pos_group," + feature.getPosGroupInQuote() + ") AND feature_type:" + feature
							.getFeatureType() + " AND annotation:PATRIC");
			if (response != null) {
				try {
					Map<String, Object> resp = jsonParser.readValue(response);
					patricFeature = this.bindDocument((Map) resp.get("doc"), GenomeFeature.class);
				}
				catch (IOException e) {
					LOGGER.error(e.getMessage(), e);
				}
			}
		}

		return patricFeature;
	}
}
