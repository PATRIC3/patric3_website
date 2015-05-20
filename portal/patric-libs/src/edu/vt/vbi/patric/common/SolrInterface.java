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

	String startDate = "1990-01-01T00:00:000Z";

	String endDate = "2020-01-01T00:00:000Z";

	String rangeDate = "+1YEAR";

	Date startDateFormat, endDateFormat;

	SimpleDateFormat shortDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	String solrServerUrl;

	LBHttpSolrServer server = null;

	ObjectMapper objectMapper;

	private static final Logger LOGGER = LoggerFactory.getLogger(SolrInterface.class);

	public SolrInterface() {
		objectMapper = new ObjectMapper();
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

	public SolrQuery buildSolrQuery (Map<String, String> key, String sorts, String facets, int start, int end, boolean highlight) {

		SolrQuery query = new SolrQuery();

		// Processing key map
		query.setQuery(KeywordReplace(key.get("keyword")));

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

		if (key.containsKey("fields") && !key.get("fields").equals("")) {
			query.addField(key.get("fields"));
		}

		// sort conditions
		if (sorts != null && !sorts.equals("") && !sorts.equals("[]")) {
			try {
				JSONArray sorter = (JSONArray) new JSONParser().parse(sorts);
				for (Object aSort : sorter) {
					JSONObject jsonSort = (JSONObject) aSort;
					query.addSort(SolrQuery.SortClause.create(jsonSort.get("property").toString(),
							jsonSort.get("direction").toString().toLowerCase()));
				}
			}
			catch (ParseException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}

		// facet conditions
		if (facets != null && !facets.equals("") && !facets.equals("{}")) {
			query.setFacet(true).setFacetMinCount(1).setFacetLimit(-1).setFacetSort(FacetParams.FACET_SORT_COUNT);

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
						query.addDateRangeFacet(field, startDateFormat, endDateFormat, rangeDate);
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
			query.setRows(500000);
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

	public JSONObject getData(ResultType key, Map<String, String> sort, String facets, int start, int end, boolean facet, boolean highlight,
			boolean grouping) throws IOException {

		if (end == -1)
			end = 500000;

		SolrQuery query = new SolrQuery();
		query.setQuery(KeywordReplace(key.get("keyword")));
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

	public String KeywordReplace(String keyword) {

		keyword = keyword.replaceAll("%20", " ");
		keyword = keyword.replaceAll("%22", "\"");
		keyword = keyword.replaceAll("%27", "'");
		keyword = keyword.replaceAll("%2F", "\\\\/");

		keyword = StringHelper.parseSolrKeywordOperator(keyword);
		return keyword;
	}

	public JSONObject getSingleFacetsData(String keyword, String single_facet, String[] facets, String fq, boolean grouping) throws IOException,
			ParseException {
		keyword = KeywordReplace(keyword);

		LOGGER.debug(keyword);

		int beginindex = keyword.indexOf(" AND (" + single_facet);
		int endindex = 0;

		StringBuffer s = new StringBuffer(keyword);

		if (beginindex < 0) {
			beginindex = keyword.indexOf("(" + single_facet);
			endindex = keyword.indexOf(") AND ", beginindex);
			if (endindex < 0) {
				endindex = keyword.indexOf("))", beginindex);

				// TODO: this cause java.lang.StringIndexOutOfBoundsException: String index out of range: -1
				// when Patric Libs keyword - (*) and endindex: 2
				LOGGER.debug("string:{}, beginIndex: {}, endIndex:{}", s, beginindex, endindex);
				if (endindex > 0) {
					s.delete(beginindex, endindex + 2);
				}
			}
			else {
				s.delete(beginindex, endindex + 6);
			}
		}
		else {
			endindex = keyword.indexOf("))", beginindex);
			if (endindex == -1) {
				endindex = keyword.indexOf("])", beginindex);
			}
			s.delete(beginindex, endindex + 2);
		}
		if (s.length() == 0)
			s.append("(*)");

		SolrQuery query = new SolrQuery();
		query.setQuery(s.toString());
		if (fq != null) {
			query.setFilterQueries(fq);
		}

		query.setStart(0); // setting starting index
		query.setRows(1);
		query.setFacet(true);
		query.setFacetMinCount(1);

		if (grouping) {
			query.set("group", true);
			query.set("group.field", "pos_group");
			query.set("group.sort", "annotation_sort asc");
			query.set("group.ngroups", "true");
			query.set("group.truncate", "true");
		}

		for (String facet : facets) {
			if (!facet.equals("completion_date") && !facet.equals("release_date")) {
				query.addFacetField(facet);
			}
			else {
				query.addDateRangeFacet(facet, startDateFormat, endDateFormat, rangeDate);
			}
		}

		return ConverttoJSON(server, query, true, false);
	}

	public JSONObject getSpellCheckerResult(String keyword) throws SolrServerException, MalformedURLException {

		SolrQuery query = new SolrQuery();
		query.setQuery(KeywordReplace(keyword));
		query.setRows(0);
		query.set("spellcheck.q", KeywordReplace(keyword));
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

	public JSONArray processStateAndTree(ResultType key, String need, JSONObject facet_fields, String facet, String state, String fq, int limit, boolean grouping) throws PortletException, IOException {

		Map<String, String> mapKey = key.toHashMap();

		return processStateAndTree(mapKey, need, facet_fields, facet, state, fq, limit, grouping);
	}

	public JSONArray processStateAndTree(Map<String, String> key, String need, JSONObject facet_fields, String facet, String state, String fq, int limit,
			boolean grouping) throws PortletException, IOException {

		JSONObject facet_data = null;

		try {
			facet_data = (JSONObject) new JSONParser().parse(facet);
		}
		catch (ParseException e2) {
			e2.printStackTrace();
		}

		String[] a = facet_data.get("facet").toString().split(",");
		String[] a_text = facet_data.get("facet_text").toString().split(",");

		JSONObject state_object = null;

		try {
			if (!state.equals("")) {
				state_object = (JSONObject) new JSONParser().parse(state);
			}
		}
		catch (ParseException e1) {
			e1.printStackTrace();
		}

		JSONArray x = new JSONArray();

		for (int i = 0; i < a.length; i++) {

			JSONObject f = null;
			JSONObject sent;

			if (state.equals("") || state_object.get(a[i]) == null) {
				sent = (JSONObject) facet_fields.get(a[i]);

				LOGGER.debug("attributes: {}, {}, {}", i, a[i], sent);
				f = createNode(sent, i, need, false, "", limit, a_text[i]);
			}
			else {
				if (state_object != null && state_object.get(a[i]) != null) {
					JSONObject object = null;

					try {
						object = getSingleFacetsData(key.get("keyword"), a[i], a, fq, grouping);
					}
					catch (ParseException e) {
						e.printStackTrace();
					}
					JSONObject obj = (JSONObject) object.get("facets");
					sent = (JSONObject) obj.get(a[i]);
					f = createNode(sent, i, "tree", true, key.get("keyword"), limit, a_text[i]);
				}
			}
			x.add(f);
		}
		return x;
	}

	public JSONObject createNode(JSONObject sent, int i, String need, boolean clear, String keyword, int limit, String facet_text) {

		JSONArray arrsub = (JSONArray) sent.get("attributes");

		// int count = 0;
		JSONArray arr_more_children = new JSONArray();
		JSONArray arr_return = new JSONArray();

		JSONObject f = new JSONObject();

		String ai = sent.get("value").toString();
		f.put("id", ai);
		f.put("renderstep", "1");
		f.put("leaf", false);

		if (clear) {

			f.put("checked", true);
			JSONObject clear_object = new JSONObject();

			clear_object.put("id", ai + "_clear");
			clear_object.put("parentID", ai);
			clear_object.put("leaf", true);
			clear_object.put("text", "<b>clear</b>");
			clear_object.put("checked", false);
			clear_object.put("renderstep", "3");

			arr_return.add(clear_object);
		}

		for (int j = 0; j < arrsub.size(); j++) {

			JSONObject object = (JSONObject) arrsub.get(j);
			JSONObject temp = new JSONObject();

			temp.put("renderstep", "2");
			temp.put("text", object.get("text").toString());
			temp.put("count", object.get("count").toString());
			temp.put("id", object.get("value") + "##" + ai);

			if (j < limit) {
				temp.put("parentID", ai);
				arr_return.add(temp);
			}
			else if (j >= limit) {
				temp.put("parentID", ai + "_more");
				arr_more_children.add(temp);
			}
			if (clear) {

				String[] split = keyword.split(ai + ":");

				for (int sp = 1; sp < split.length; sp++) {
					int endindex = split[sp].indexOf(")");
					String lookup = "";

					if (ai.equals("completion_date") || ai.equals("release_date"))
						lookup = split[sp].substring(2, endindex);
					else
						lookup = split[sp].substring(1, endindex);


					if (lookup.indexOf(" OR ") > 0) {
						String[] lookup_arr = lookup.split(" OR ");
						for (int k = 0; k < lookup_arr.length; k++) {
							if (ai.equals("completion_date") || ai.equals("release_date")) {
								lookup_arr[k] = lookup_arr[k].split("-")[0];
								if (k > 0)
									lookup_arr[k] = lookup_arr[k].split("\\[")[1];
							}
							if (lookup_arr[k].equals(object.get("value").toString())
									|| lookup_arr[k].equals("\"" + object.get("value").toString() + "\"")) {
								temp.put("checked", true);
								break;
							}
						}
					}
					else {
						if (ai.equals("completion_date") || ai.equals("release_date"))
							lookup = lookup.split("-")[0];

						if (lookup.equals(object.get("value").toString()) || lookup.equals("\"" + object.get("value").toString() + "\"")
								|| lookup.equals("*") || lookup.equals("* TO *]")) {
							temp.put("checked", true);
							break;
						}
					}
				}
			}
		}

		if (arrsub.size() > limit) {

			JSONObject temp = new JSONObject();
			temp.put("parentID", ai + "_more");
			temp.put("id", ai + "_less");
			temp.put("text", " <b>less</b>");
			temp.put("leaf", true);
			temp.put("renderstep", "3");
			arr_more_children.add(temp);

			JSONObject more = new JSONObject();
			more.put("id", ai + "_more");
			more.put("leaf", false);
			more.put("renderstep", "3");
			more.put("children", arr_more_children);
			more.put("text", "<b>more</b>");
			arr_return.add(more);
		}

		f.put("expanded", true);
		f.put("children", arr_return);
		f.put("count", sent.get("count"));

		if (need.equals("tree")) {
			f.put("text", "<span style=\"color: #CC6600; margin: 0; padding: 0 0 2px; font-weight: bold;\">" + facet_text
					+ "</span><span style=\"color: #888;\"> (" + sent.get("count").toString() + ")</span>");
		}
		else {
			f.put("text", facet_text + " <b>(" + sent.get("count").toString() + ")</b>");
		}
		return f;
	}

	public String ConstructKeyword(String field_name, String id) {
		String keyword = "";
		if (id.contains(",")) {
			List<String> ids = Arrays.asList(id.split(","));
			keyword += field_name + ":(" + StringUtils.join(ids, " OR ") + ")";
		}
		else {
			keyword = field_name + ":" + id;
		}
		return keyword;
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
				for (Iterator<Map.Entry<String, Object>> i = d.iterator(); i.hasNext();) {
					Map.Entry<String, Object> el = i.next();
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

	// helper methods
	public JSONObject formatFacetTree(Map facet) {
		JSONObject result = new JSONObject();

		Map<String, Object> fieldFacets = (Map<String, Object>) facet.get("facet_fields");

		for (Map.Entry fieldFacet : fieldFacets.entrySet()) {

			JSONObject facet_json = new JSONObject();
			int count = 0;
			JSONArray attributes = new JSONArray();

			List facetEntries = (ArrayList) fieldFacet.getValue();
			if (facetEntries != null) {

				// LOGGER.trace("facet_field: {}, entries:{}", fieldFacet.getKey(), facetEntries);
				for (int i = 0; i < facetEntries.size(); i=i+2) {
					String entryName = (String) facetEntries.get(i);
					Integer entryCount = (Integer) facetEntries.get(i+1);

					JSONObject attribute = new JSONObject();
					attribute.put("text", entryName + " <span>(" + entryCount + ")</span>");
					attribute.put("value", entryName);
					attribute.put("count", entryCount);

					attributes.add(attribute);
					count += entryCount;
				}
			}

			facet_json.put("value", fieldFacet.getKey());
			facet_json.put("count", count);
			facet_json.put("text", fieldFacet.getKey() + " <span>(" + count + ")</span>");
			facet_json.put("attributes", attributes);

			result.put(fieldFacet.getKey(), facet_json);
		}

		Map<String, Object> rangeFacets = (Map<String, Object>) facet.get("facet_ranges");
		List<String> yearRoundUpFields = Arrays.asList("completion_date", "release_date");

		for (Map.Entry rangeFacet : rangeFacets.entrySet()) {

			JSONObject facet_json = new JSONObject();
			int count = 0;
			JSONArray attributes = new JSONArray();

			List rangeEntries = (ArrayList) ((Map<String, Object>) rangeFacet.getValue()).get("counts");
			if (rangeEntries != null) {

				// LOGGER.trace("facet_range: {}, entries:{}", rangeFacet.getKey(), rangeEntries);
				for (int i = 0; i < rangeEntries.size(); i=i+2) {
					String entryName;
					if (yearRoundUpFields.contains(rangeFacet.getKey())) {
						entryName = ((String) rangeEntries.get(i)).substring(0,4);
					}
					else {
						entryName = (String) rangeEntries.get(i);
					}
					Integer entryCount = (Integer) rangeEntries.get(i+1);

					if (entryCount > 0) {
						JSONObject attribute_json = new JSONObject();
						attribute_json.put("text", entryName + " <span>(" + entryCount + ")</span>");
						attribute_json.put("value", entryName);
						attribute_json.put("count", entryCount);

						attributes.add(attribute_json);
						count += entryCount;
					}
				}
			}
			// range facet sort is not working correctly. need to sort results
			for (int i = 0; i < attributes.size(); i++) {
				for (int j = 0; j < attributes.size(); j++) {
					JSONObject a = (JSONObject) attributes.get(i);
					JSONObject b = (JSONObject) attributes.get(j);
					if (Integer.parseInt(a.get("count").toString()) > Integer.parseInt(b.get("count").toString())) {
						attributes.set(i, b);
						attributes.set(j, a);
					}
				}
			}

			facet_json.put("value", rangeFacet.getKey());
			facet_json.put("count", count);
			facet_json.put("text", rangeFacet.getKey() + " <span>(" + count + ")</span>");
			facet_json.put("attributes", attributes);

			result.put(rangeFacet.getKey(), facet_json);
		}

		return result;
	}

}
