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

import org.apache.solr.client.solrj.SolrQuery;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FacetHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(SolrInterface.class);

	private static ObjectMapper objectMapper = new ObjectMapper();

	private static ObjectReader jsonMapReader = objectMapper.reader(Map.class);

	public static JSONArray processStateAndTree(DataApiHandler dataApi, SolrCore core, Map<String, String> key, String need, JSONObject facet_fields,
			String facet, String state, String fq, int limit) throws PortletException, IOException {

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

				//				LOGGER.debug("attributes: {}, {}, {}", i, a[i], sent);
				f = createNode(sent, need, false, "", limit, a_text[i]);
			}
			else {
				if (state_object != null && state_object.get(a[i]) != null) {
					JSONObject object = null;

					try {
						object = getSingleFacetsData(dataApi, core, key.get("keyword"), a[i], a, fq);
					}
					catch (ParseException e) {
						e.printStackTrace();
					}
					JSONObject obj = (JSONObject) object.get("facets");
					sent = (JSONObject) obj.get(a[i]);
					f = createNode(sent, "tree", true, key.get("keyword"), limit, a_text[i]);
				}
			}
			x.add(f);
		}
		return x;
	}

	private static JSONObject createNode(JSONObject node, String need, boolean addClearOption, String keyword, int limit, String facetTitle) {

		String fieldName = (String) node.get("value");
		JSONArray attributes = (JSONArray) node.get("attributes");

		JSONArray children = new JSONArray();
		JSONArray grandChildren = new JSONArray();

		JSONObject returnObject = new JSONObject();
		returnObject.put("id", fieldName);
		returnObject.put("renderstep", "1");
		returnObject.put("leaf", false);

		if (addClearOption) {

			returnObject.put("checked", true);
			JSONObject clearObject = new JSONObject();

			clearObject.put("id", fieldName + "_clear");
			clearObject.put("parentID", fieldName);
			clearObject.put("leaf", true);
			clearObject.put("text", "<b>clear</b>");
			clearObject.put("checked", false);
			clearObject.put("renderstep", "3");

			children.add(clearObject);
		}

		for (int j = 0; j < attributes.size(); j++) {

			JSONObject object = (JSONObject) attributes.get(j);
			JSONObject temp = new JSONObject();

			temp.put("renderstep", "2");
			temp.put("text", object.get("text").toString());
			temp.put("count", object.get("count").toString());
			temp.put("id", object.get("value") + "##" + fieldName);

			if (j < limit) {
				temp.put("parentID", fieldName);
				children.add(temp);
			}
			else if (j >= limit) {
				temp.put("parentID", fieldName + "_more");
				grandChildren.add(temp);
			}
			if (addClearOption) {

				String[] split = keyword.split(fieldName + ":");

				for (int sp = 1; sp < split.length; sp++) {
					int endIndex = split[sp].indexOf(")");
					String lookup;

					if (fieldName.equals("completion_date") || fieldName.equals("release_date")) {
						lookup = split[sp].substring(2, endIndex);
					}
					else {
						lookup = split[sp].substring(1, endIndex);
					}

					if (lookup.indexOf(" OR ") > 0) {
						String[] lookup_arr = lookup.split(" OR ");
						for (int k = 0; k < lookup_arr.length; k++) {
							if (fieldName.equals("completion_date") || fieldName.equals("release_date")) {
								lookup_arr[k] = lookup_arr[k].split("-")[0];
								if (k > 0) {
									lookup_arr[k] = lookup_arr[k].split("\\[")[1];
								}
							}
							if (lookup_arr[k].equals(object.get("value").toString()) || lookup_arr[k]
									.equals("\"" + object.get("value").toString() + "\"")) {
								temp.put("checked", true);
								break;
							}
						}
					}
					else {
						if (fieldName.equals("completion_date") || fieldName.equals("release_date")) {
							lookup = lookup.split("-")[0];
						}

						if (lookup.equals(object.get("value").toString()) || lookup.equals("\"" + object.get("value").toString() + "\"") || lookup
								.equals("*") || lookup.equals("* TO *]")) {
							temp.put("checked", true);
							break;
						}
					}
				}
			}
		}

		if (attributes.size() > limit) {

			JSONObject temp = new JSONObject();
			temp.put("parentID", fieldName + "_more");
			temp.put("id", fieldName + "_less");
			temp.put("text", " <b>less</b>");
			temp.put("leaf", true);
			temp.put("renderstep", "3");
			grandChildren.add(temp);

			JSONObject more = new JSONObject();
			more.put("id", fieldName + "_more");
			more.put("leaf", false);
			more.put("renderstep", "3");
			more.put("children", grandChildren);
			more.put("text", "<b>more</b>");
			children.add(more);
		}

		returnObject.put("expanded", true);
		returnObject.put("children", children);
		returnObject.put("count", node.get("count"));

		if (need.equals("tree")) {
			returnObject.put("text", "<span style=\"color: #CC6600; margin: 0; padding: 0 0 2px; font-weight: bold;\">" + facetTitle
					+ "</span><span style=\"color: #888;\"> (" + node.get("count").toString() + ")</span>");
		}
		else {
			returnObject.put("text", facetTitle + " <b>(" + node.get("count").toString() + ")</b>");
		}

		return returnObject;
	}

	private static JSONObject getSingleFacetsData(DataApiHandler dataApi, SolrCore core, String keyword, String single_facet, String[] facets,
			String fq) throws IOException, ParseException {

		SolrInterface solr = new SolrInterface();

		keyword = StringHelper.stripQuoteAndParseSolrKeywordOperator(keyword);

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

		query.setStart(0).setRows(1).setFacet(true).setFacetMinCount(1);

		for (String facet : facets) {
			if (!facet.equals("completion_date") && !facet.equals("release_date")) {
				query.addFacetField(facet);
			}
			else {
				query.addDateRangeFacet(facet, solr.getRangeStartDate(), solr.getRangeEndDate(), solr.getRangeDate());
			}
		}

		String apiResponse = dataApi.solrQuery(core, query);

		Map resp = jsonMapReader.readValue(apiResponse);
		Map respBody = (Map) resp.get("response");

		JSONObject ret = new JSONObject();
		ret.put("response", new JSONObject(respBody));
		ret.put("facets", FacetHelper.formatFacetTree((Map) resp.get("facet_counts")));

		return ret;
	}

	public static JSONObject formatFacetTree(Map facet) {
		JSONObject result = new JSONObject();

		Map<String, Object> fieldFacets = (Map<String, Object>) facet.get("facet_fields");

		for (Map.Entry fieldFacet : fieldFacets.entrySet()) {

			JSONObject facet_json = new JSONObject();
			int count = 0;
			JSONArray attributes = new JSONArray();

			List facetEntries = (ArrayList) fieldFacet.getValue();
			if (facetEntries != null) {

				// LOGGER.trace("facet_field: {}, entries:{}", fieldFacet.getKey(), facetEntries);
				for (int i = 0; i < facetEntries.size(); i = i + 2) {
					String entryName = (String) facetEntries.get(i);
					Integer entryCount = (Integer) facetEntries.get(i + 1);

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
				for (int i = 0; i < rangeEntries.size(); i = i + 2) {
					String entryName;
					if (yearRoundUpFields.contains(rangeFacet.getKey())) {
						entryName = ((String) rangeEntries.get(i)).substring(0, 4);
					}
					else {
						entryName = (String) rangeEntries.get(i);
					}
					Integer entryCount = (Integer) rangeEntries.get(i + 1);

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
