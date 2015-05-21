/*******************************************************************************
 * Copyright 2014 Virginia Polytechnic Institute and State University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package edu.vt.vbi.patric.common;

import edu.vt.vbi.patric.dao.ResultType;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.apache.solr.client.solrj.response.*;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.FacetParams;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.PortletException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

@SuppressWarnings("unchecked")
public class SolrInterface {

	SolrCore core;

	private String startDate = "1990-01-01T00:00:000Z";

	private String endDate = "2020-01-01T00:00:000Z";

	private String rangeDate = "+1YEAR";

	private Date startDateFormat, endDateFormat;

	private SimpleDateFormat shortDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	String solrServerUrl;

	LBHttpSolrServer server = null;

	private ObjectReader jsonMapReader;

	private static final Logger LOGGER = LoggerFactory.getLogger(SolrInterface.class);

	public SolrInterface() {
		ObjectMapper objectMapper = new ObjectMapper();
		jsonMapReader = objectMapper.reader(Map.class);

		solrServerUrl = System.getProperty("solr.serverUrls", "http://localhost:8983");

		try {
			startDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(startDate);
			endDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(endDate);

			// reset timezone for short date format
			shortDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		}
		catch (java.text.ParseException e) {
			e.printStackTrace();
		}
	}

	public Date getRangeStartDate() {
		return startDateFormat;
	}

	public Date getRangeEndDate() {
		return endDateFormat;
	}

	public String getRangeDate() { return rangeDate; }

	/**
	 * will be replaced by getSolrServer(SolrCore core) later
	 * @return
	 */
	public void setCurrentInstance(SolrCore core) throws MalformedURLException {
		String coreName = "/solr/" + core.getSolrCoreName();
		this.core = core;

		if (solrServerUrl.contains(",")) {
			String[] urls = solrServerUrl.split(",");

			server = new LBHttpSolrServer();
			for (String url: urls) {
				server.addSolrServer(url + coreName);
			}
		}
		else {
			server = new LBHttpSolrServer(solrServerUrl + coreName);
		}
	}

	public LBHttpSolrServer getSolrServer(SolrCore core) throws MalformedURLException {
		String coreName = "/solr/" + core.getSolrCoreName();

		if (solrServerUrl.contains(",")) {
			String[] urls = solrServerUrl.split(",");

			LBHttpSolrServer server = new LBHttpSolrServer();
			for (String url: urls) {
				server.addSolrServer(url + coreName);
			}
			return server;
		}
		else {
			return new LBHttpSolrServer(solrServerUrl + coreName);
		}
	}

	public JSONObject getData(ResultType key, Map<String, String> sort, String facets, int start, int end, boolean facet, boolean highlight,
			boolean grouping) throws IOException {

		if (end == -1)
			end = 500000;

		SolrQuery query = new SolrQuery();
		query.setQuery(StringHelper.stripQuoteAndParseSolrKeywordOperator(key.get("keyword")));
		if (key.containsKey("filter") && key.get("filter") != null) {
			query.setFilterQueries(key.get("filter"));
		}
		if (key.containsKey("filter2") && key.get("filter2") != null) {
			query.addFilterQuery(key.get("filter2"));
		}
		// use SolrJoin if possible
		if (key.containsKey("join")) {
			query.setFilterQueries(key.get("join"));
		}
		query.setStart(start); // setting starting index

		if (end != -1) {
			query.setRows(end);
		}

		if (grouping) {
			query.set("group", true);
			query.set("group.field", "pos_group");
			query.set("group.sort", "annotation_sort asc");
			query.set("group.ngroups", "true");
			if (facet)
				query.set("group.truncate", "true");
		}

		if (facet) {
			query.setFacet(true);
			query.setFacetMinCount(1);
			query.setFacetLimit(-1);
			query.setFacetSort(FacetParams.FACET_SORT_COUNT);

			JSONObject facet_data = null;

			try {
				facet_data = (JSONObject) new JSONParser().parse(facets);
			}
			catch (ParseException e2) {
				e2.printStackTrace();
			}
			if (facet_data.containsKey("facet.sort")) {
				String fsort = facet_data.get("facet.sort").toString();
				if (fsort.equals("index")) {
					query.setFacetSort(FacetParams.FACET_SORT_INDEX);
				}
			}
			String[] ff = facet_data.get("facet").toString().split(",");

			for (int i = 0; i < ff.length; i++) {
				if (!ff[i].equals("completion_date") && !ff[i].equals("release_date")) {
					query.addFacetField(ff[i]);
				}
				else {
					query.addDateRangeFacet(ff[i], startDateFormat, endDateFormat, rangeDate);
				}
			}
		}

		if (sort != null && sort.get("field") != null && sort.get("field") != "") {

			String[] sort_field = sort.get("field").split(",");
			String s = "";
			s += sort_field[0] + ((sort.get("direction").equalsIgnoreCase("asc")) ? " asc" : " desc");

			for (int i = 1; i < sort_field.length; i++) {
				s += ", " + sort_field[i] + ((sort.get("direction").equalsIgnoreCase("asc")) ? " asc" : " desc");
			}
			query.set("sort", s);
		}

		if (highlight) {
			query.set("hl", "on");
			query.set("hl.fl", "*");
		}

		if (key.containsKey("fields") && !key.get("fields").equals("")) {
			query.addField(key.get("fields"));
		}

		LOGGER.debug("getData():{}", query.toString());

		return ConverttoJSON(server, query, facet, highlight);
	}

	@SuppressWarnings("rawtypes")
	public JSONObject ConverttoJSON(SolrServer server, SolrQuery query, boolean faceted, boolean highlighted) {

		JSONObject result = new JSONObject();

		try {
			LOGGER.debug("ConverttoJSON: {}", query.toString());
			QueryResponse qr = server.query(query, SolrRequest.METHOD.POST);

			SolrDocumentList sdl = new SolrDocumentList();
			GroupResponse groupResponse = qr.getGroupResponse();
			JSONObject response = new JSONObject();
			JSONArray docs = new JSONArray();

			if (this.core == SolrCore.FEATURE && groupResponse != null) {
				// Read the group results per command
				for (GroupCommand command : groupResponse.getValues()) {
					response.put("numFound", command.getNGroups().intValue());
					for (Group group : command.getValues()) {
						SolrDocumentList docList = group.getResult();
						for (SolrDocument doc : docList) {
							sdl.add(doc);
						}
					}
				}
			}
			else {
				sdl = qr.getResults();
				response.put("numFound", sdl.getNumFound());
			}

			Map<String, Map<String, List<String>>> highlight_id = null;

			if (highlighted) {
				highlight_id = qr.getHighlighting();
			}

			for (SolrDocument d : sdl) {
				Map<String, List<String>> highlight_fields = null;

				JSONObject values = new JSONObject();
				for (Entry<String, Object> el : d) {
					if (el.getKey().equals("completion_date") || el.getKey().equals("release_date")) {
						values.put(el.getKey(), transformDate((Date) el.getValue()));
					}
					else {
						values.put(el.getKey(), el.getValue());
					}

					if (el.getKey().equals("rownum")) {
						if (highlighted) {
							if (highlight_id.containsKey(el.getValue().toString())) {
								highlight_fields = highlight_id.get(el.getValue().toString());
							}
						}
					}
				}

				if (highlight_fields != null) {

					Iterator<Entry<String,List<String>>> it = highlight_fields.entrySet().iterator();
					JSONObject highlight_values_json = new JSONObject();

					while (it.hasNext()) {
						Entry<String, List<String>> entry = it.next();
						List<String> highlight_values = entry.getValue();
						String highlight_key = entry.getKey();
						JSONArray highlight_values_array = new JSONArray();

						for (String highlight_value : highlight_values) {
							highlight_values_array.add(highlight_value);
						}
						highlight_values_json.put(highlight_key, highlight_values_array);
					}
					values.put("highlight", highlight_values_json);
				}
				docs.add(values);
			}

			response.put("docs", docs);
			result.put("response", response);

			JSONObject facets_json = new JSONObject();

			if (faceted) {
				List<FacetField> facets = qr.getFacetFields();
				for (FacetField facet : facets) {
					JSONObject facet_json = new JSONObject();
					int count = 0;
					JSONArray attributes_json = new JSONArray();
					List<FacetField.Count> facetEntries = facet.getValues();

					if (facet.getValues() != null) {
						for (FacetField.Count fcount : facetEntries) {
							JSONObject attribute_json = new JSONObject();
							attribute_json.put("text", fcount.getName() + " <span style=\"color: #888;\"> (" + fcount.getCount() + ") </span>");
							attribute_json.put("value", fcount.getName());
							attribute_json.put("count", fcount.getCount());
							attributes_json.add(attribute_json);

							count += Integer.parseInt(String.valueOf(fcount.getCount()));
						}
					}

					facet_json.put("value", facet.getName());
					facet_json.put("count", count);
					facet_json.put("text", facet.getName() + " <span style=\"color: #888;\"> (" + count + ") </span>");
					facet_json.put("attributes", attributes_json);
					facets_json.put(facet.getName(), facet_json);
				}

				List<RangeFacet> ranges = qr.getFacetRanges();
				for (RangeFacet range : ranges) {
					JSONObject facet_json = new JSONObject();
					int count = 0;
					JSONArray attributes_json = new JSONArray();
					List<RangeFacet.Count> rangeEntries = range.getCounts();

					if (rangeEntries != null) {
						for (RangeFacet.Count fcount : rangeEntries) {
							if (fcount.getCount() > 0) {
								JSONObject attribute_json = new JSONObject();
								attribute_json.put("text", fcount.getValue().split("-")[0] + " <span style=\"color: #888;\"> (" + fcount.getCount()
										+ ") </span>");
								attribute_json.put("value", fcount.getValue().split("-")[0]);
								attribute_json.put("count", fcount.getCount());
								attributes_json.add(attribute_json);
								count += Integer.parseInt(String.valueOf(fcount.getCount()));
							}
						}
					}
					JSONArray tmp = attributes_json;
					for (int i = 0; i < attributes_json.size(); i++) {
						for (int j = 0; j < attributes_json.size(); j++) {
							JSONObject a = (JSONObject) attributes_json.get(i);
							JSONObject b = (JSONObject) attributes_json.get(j);
							if (Integer.parseInt(a.get("count").toString()) > Integer.parseInt(b.get("count").toString())) {
								tmp.set(i, b);
								tmp.set(j, a);
							}
						}
					}

					facet_json.put("value", range.getName());
					facet_json.put("count", count);
					facet_json.put("text", range.getName() + " <span style=\"color: #888;\"> (" + count + ") </span>");
					facet_json.put("attributes", tmp);
					facets_json.put(range.getName(), facet_json);
				}
				result.put("facets", facets_json);
			}
		}
		catch (SolrServerException e) {
			e.printStackTrace();
		}
		return result;
	}


	public JSONObject getSpellCheckerResult(String keyword) throws SolrServerException, MalformedURLException {

		SolrQuery query = new SolrQuery();
		query.setQuery(StringHelper.stripQuoteAndParseSolrKeywordOperator(keyword));
		query.setRows(0);
		query.set("spellcheck.q", StringHelper.stripQuoteAndParseSolrKeywordOperator(keyword));
		query.set("spellcheck", "true");
		query.set("spellcheck.collate", "true");
		query.set("spellcheck.onlyMorePopular", "true");
		query.set("spellcheck.extendedResults", "true");

		JSONObject result = new JSONObject();
		JSONArray suggestion = new JSONArray();

		for (int i = 0; i < 4; i++) {

			if (i == 0) {
				this.setCurrentInstance(SolrCore.FEATURE);
			}
			else if (i == 1) {
				this.setCurrentInstance(SolrCore.GENOME);
			}
			else if (i == 2) {
				this.setCurrentInstance(SolrCore.TAXONOMY);
			}
			else if (i == 3) {
				this.setCurrentInstance(SolrCore.TRANSCRIPTOMICS_EXPERIMENT);
			}

			QueryResponse qr = server.query(query, SolrRequest.METHOD.POST);
			try {
				SpellCheckResponse spellCheckRes = qr.getSpellCheckResponse();
				if (spellCheckRes.getCollatedResult() != null && !suggestion.contains(spellCheckRes.getCollatedResult()))
					suggestion.add(spellCheckRes.getCollatedResult());
			} catch (NullPointerException e) {

			}
		}
		result.put("suggestion", suggestion);

		// hypotetical - suggestion + numFound > 0 + get alternativKW
		// south koree - suggestion + numFound = 0 + continue

		return result;
	}




	/**
	 * used at
	 *
	 * - patric-common/src/edu/vt/vbi/patric/cache/DataLandingGenerator.java
	 * - patric-common/src/edu/vt/vbi/patric/portlets/WorkspacePortlet.java
	 *
	 * @param queryParam
	 * @param facet
	 * @return
	 */
	public JSONObject queryFacet(String queryParam, String facet) {
		JSONObject res = new JSONObject();
		SolrQuery query = new SolrQuery();

		query.setQuery(queryParam);
		query.setRows(0).setFacet(true).setFacetLimit(-1).setFacetMinCount(1).setFacetSort(FacetParams.FACET_SORT_COUNT);
		query.addFacetField(facet);

		try {
			QueryResponse qr = server.query(query);
			// skip passing records
			SolrDocumentList sdl = qr.getResults();

			// get facet list and counts
			FacetField ff = qr.getFacetField(facet);

			JSONArray facetfield = new JSONArray();
			List<FacetField.Count> facetEntries = ff.getValues();

			if (ff.getValues() != null) {

				for (FacetField.Count fcount : facetEntries) {
					JSONObject attr = new JSONObject();
					attr.put("value", fcount.getName());
					attr.put("count", fcount.getCount());
					facetfield.add(attr);
				}
			}
			res.put("facet", facetfield);
			res.put("total", sdl.getNumFound());
		}
		catch (SolrServerException e) {
			e.printStackTrace();
		}

		return res;
	}

	public String transformDate(Date solrDate) {
		if (solrDate != null) {
			return shortDateFormat.format(solrDate);
		}
		else {
			return null;
		}
	}

	public String getProteomicsTaxonIdFromFeatureId(String id) {
		SolrQuery query = new SolrQuery();
		query.setQuery("na_feature_id:" + id);
		query.setRows(1000000);
		String experiment_id = "";

		try {
			QueryResponse qr = server.query(query);
			SolrDocumentList sdl = qr.getResults();

			for (SolrDocument d : sdl) {
				for (Entry<String, Object> el : d) {
					if (el.getKey().equals("experiment_id")) {
						if (experiment_id.length() == 0)
							experiment_id = el.getValue().toString();
						else
							experiment_id += "##" + el.getValue().toString();
					}
				}
			}
		}
		catch (SolrServerException e) {
			e.printStackTrace();
		}

		return experiment_id;
	}

}
