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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.Map.Entry;

import javax.portlet.PortletException;

import edu.vt.vbi.patric.beans.Genome;
import edu.vt.vbi.patric.beans.GenomeFeature;
import edu.vt.vbi.patric.beans.Taxonomy;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.Group;
import org.apache.solr.client.solrj.response.GroupCommand;
import org.apache.solr.client.solrj.response.GroupResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.RangeFacet;
import org.apache.solr.client.solrj.response.SpellCheckResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.FacetParams;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.vt.vbi.patric.dao.ResultType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public class SolrInterface {

	SolrCore core;

	String startDate = "1990-01-01T00:00:00Z";

	String endDate = "2020-01-01T00:00:00Z";

	String rangeDate = "+1YEAR";

	Date startDateFormat, endDateFormat;

	SimpleDateFormat shortDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	String solrServerUrl;

	LBHttpSolrServer server = null;

	CloudSolrServer cloud = null;

	private static final Logger LOGGER = LoggerFactory.getLogger(SolrInterface.class);

	public SolrInterface() {
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

//	public LBHttpSolrServer getServer() {
//		return server;
//	}

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
	/**
	 * will be replaced by getSolrServer(SolrCore core) later
	 * @return
	 */
	public String getServerUrl(SolrCore core) {
		String url = null;
		if (solrServerUrl.contains(",")) {
			String[] urls = solrServerUrl.split(",");
			List<String> listUrl = new ArrayList<>();
			for (String u: urls) {
				listUrl.add(u + "/solr/" + core.getSolrCoreName());
			}
			url = StringUtils.join(listUrl, ",");
		}
		else {
			url = solrServerUrl + "/solr/" + core.getSolrCoreName();
		}
		return url;
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

			// if (!type.equals("FigFamSorter")) {
			// if (this.core != SolrCore.FIGFAM) { // figfam core is replaced by dnafeature and no need to prevent facet
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
			// }
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

		return ConverttoJSON(server, query, facet, highlight);
	}

	public JSONObject getIdsForCart(ResultType key, String field, int rows) {

		SolrQuery query = new SolrQuery();
		query.setQuery(KeywordReplace(key.get("keyword")));

		query.setStart(0); // setting starting index
		query.setRows(rows);
		query.setFields(field);

		return ConverttoJSON(server, query, false, false);

	}

	@SuppressWarnings("rawtypes")
	public JSONObject ConverttoJSON(SolrServer server, SolrQuery query, boolean faceted, boolean highlighted) {

		JSONObject result = new JSONObject();

		try {
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

	public JSONObject getSingleFacetsData(String keyword, String single_facet, String[] facets, boolean grouping) throws IOException, ParseException {
		return getSingleFacetsData(keyword, single_facet, facets, null, grouping);
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

//	public JSONObject getGenomeTabJSON(String genome_info_id, String solr_instance) throws IOException, ParseException {
//
//		SolrQuery query = new SolrQuery();
//		query.setQuery("genome_id:" + genome_info_id);
//
//		return ConverttoJSON(server, query, false, false);
//
//	}

	public JSONObject getGenomeIDsfromSolr(String keyword, String facets, boolean faceted) throws MalformedURLException {

		SolrQuery query = new SolrQuery();
		query.setQuery(KeywordReplace(keyword));

		if (faceted) {
			// if (type.equals("GenomeFinder")) {
			if (this.core == SolrCore.GENOME) {
				JSONObject facet_data = null;

				try {
					facet_data = (JSONObject) new JSONParser().parse(facets);
				}
				catch (ParseException e2) {
					e2.printStackTrace();
				}

				query.setFacet(true);
				query.setFacetMinCount(1);
				String[] ff = facet_data.get("facet").toString().split(",");

				for (int i = 0; i < ff.length; i++)
					if (!ff[i].equals("completion_date") && !ff[i].equals("release_date"))
						query.addFacetField(ff[i]);
					else
						query.addDateRangeFacet(ff[i], startDateFormat, endDateFormat, rangeDate);
			}
		}

		query.setRows(100000);

		return ConverttoJSON(server, query, faceted, false);
	}

	public JSONObject getSummaryforGlobalSearch(String keyword) throws SolrServerException {
		SolrQuery query = new SolrQuery();
		query.setQuery(KeywordReplace(keyword));
		query.setRows(3);
		query.set("hl", "on");
		query.set("hl.fl", "*");

		// if (type.equals("GenomicFeature")) {
		if (this.core == SolrCore.FEATURE) {
			query.setFields("genome_id,genome_name,accession,alt_locus_tag,refseq_locus_tag,gene,annotation,feature_type,feature_id,start,end,na_length,strand,protein_id,aa_length,product,figfam_id");
			query.addFilterQuery("!(feature_type:source)");
			query.set("group", true);
			query.set("group.field", "pos_group");
			query.set("group.sort", "annotation_sort asc");
			query.set("group.ngroups", "true");
		}
		else if (this.core == SolrCore.GENOME) {
			query.setFields("genome_id,genome_name,taxon_id,genome_status,genome_length,chromosomes,plasmids,contigs,sequences,rast_cds,refseq_cds,isolation_country,host_name,disease,collection_date,completion_date,mlst,strain,serovar,biovar,pathovar,culture_collection,type_strain,sequencing_centers,publication,ncbi_project_id,refseq_project_id,genbank_accessions,refseq_accessions,sequencing_platform,sequencing_depth,assembly_method,gc_content,isolation_site,isolation_source,isolation_comments,geographic_location,latitude,longitude,altitude,depth,host_gender,host_age,host_health,body_sample_site,body_sample_subsite,gram_stain,cell_shape,motility,sporulation,temperature_range,salinity,oxygen_requirement,habitat,comments");
		}
		else if (this.core == SolrCore.TAXONOMY) {
			query.setFields("taxon_id,taxon_name,taxon_rank");
		}
		else if (this.core == SolrCore.TRANSCRIPTOMICS_EXPERIMENT) {
			query.setFields("eid,accession,title,description,organism,strain,mutant,timeseries,condition");
		}

		return ConverttoJSON(server, query, false, true);
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

	/**
	 * @param need
	 * @param facet_fields
	 * @param facet
	 * @return
	 */
	public JSONArray processStateAndTree(ResultType key, String need, JSONObject facet_fields, String facet, String state, int limit, boolean grouping)
			throws PortletException, IOException {
		return processStateAndTree(key, need, facet_fields, facet, state, null, limit, grouping);
	}

	public JSONArray processStateAndTree(ResultType key, String need, JSONObject facet_fields, String facet, String state, String fq, int limit,
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
			if (state != "")
				state_object = (JSONObject) new JSONParser().parse(state);
		}
		catch (ParseException e1) {
			e1.printStackTrace();
		}

		JSONArray x = new JSONArray();

		for (int i = 0; i < a.length; i++) {

			JSONObject f = null;
			JSONObject sent = null;

			if (state == "" || state_object.get(a[i]) == null) {
				sent = (JSONObject) facet_fields.get(a[i]);
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

	/*
	 * public String ConstructSequenceFinderKeyword(String gid) {
	 * 
	 * String keyword = "";
	 * 
	 * if (gid.contains(",")) { String[] gids = gid.split(","); if (gids.length > 0) { keyword += "(gid:(" + gids[0]; for (int i = 1; i < gids.length;
	 * i++) { keyword += " OR " + gids[i]; } keyword += "))"; } } else { keyword = "(gid:" + gid + ")"; } return keyword; }
	 */
	public String ConstructKeyword(String field_name, String id) {
		String keyword = "";
		if (id.contains(",")) {
			String[] ids = id.split(",");
			keyword += "(" + field_name + ":" + ids[0];
			for (int i = 1; i < ids.length; i++) {
				keyword += " OR " + field_name + ":" + ids[i];
			}
			keyword += ")";
		}
		else {
			keyword = "(" + field_name + ":" + id + ")";
		}
		return keyword;
	}

	public String getGenomeIdsfromSolrOutput(JSONObject solr_output) {

		String solrId = "";
		try {
			JSONObject obj = (JSONObject) solr_output.get("response");
			JSONArray obj1 = (JSONArray) obj.get("docs");
			JSONObject a = (JSONObject) obj1.get(0);

			if (a.containsKey("genome_info_id"))
				solrId = a.get("genome_info_id").toString();

			for (int i = 1; i < obj1.size(); i++) {
				a = (JSONObject) obj1.get(i);
				if (a.containsKey("genome_info_id")) {
					solrId += "," + a.get("genome_info_id").toString();
				}
			}
		}
		catch (Exception ex) {
			System.out.println("error getSolrIds" + ex.toString());
		}

		return solrId;
	}

	/**
	 * Retrieve transcriptomics comparison table from Solr with given experiment id(s) and sample id(s)
	 * @author Oral Dalay
	 * param experiment Ids or/and Comparison Ids
	 * @return JSONObject
	 * @throws MalformedURLException
	 */

	public JSONObject getTranscriptomicsSamples(String sampleId, String expId, String fields, int start, int end, HashMap<String, String> sort)
			throws MalformedURLException {
		JSONObject res = new JSONObject();
		String query = "";

		if (expId != null && !expId.equals("")) {
			query += "eid:(" + expId.replaceAll(",", " OR ") + ")";
		}

		if (sampleId != null && !sampleId.equals("")) {
			if (query.length() > 0) {
				query += " AND ";
			}
			query += "pid:(" + sampleId.replaceAll(",", " OR ") + ")";
		}

		ResultType key = new ResultType();
		key.put("keyword", query);
		if (!fields.equals(""))
			key.put("fields", fields);

		try {
			this.setCurrentInstance(SolrCore.TRANSCRIPTOMICS_COMPARISON);
			key.put("fields", "eid,expid,accession,pid,samples,expname,release_date,pmid,organism,strain,mutant,timepoint,condition,genes,sig_log_ratio,sig_z_score");
			res = this.getData(key, sort, null, start, end, false, false, false);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		JSONObject ret = new JSONObject();
		ret.put("data", ((JSONObject) res.get("response")).get("docs"));
		ret.put("total", ((JSONObject) res.get("response")).get("numFound"));

		return ret;
	}

	/**
	 * Retrieve transcriptomics comparison ids from Solr with given keyword
	 * @author Oral Dalay
	 * @param keyword (locus tags or free text)
	 * @return String
	 */
	public String getTranscriptomicsSamplePIds(String keyword) {

		String ret = "";

		JSONObject res = new JSONObject();

		ResultType key = new ResultType();
		key.put("keyword", "locus_tag:(" + keyword + ") OR refseq_locus_tag:(" + keyword + ") ");
		key.put("fields", "pid");

		try {
			this.setCurrentInstance(SolrCore.TRANSCRIPTOMICS_GENE);
			res = this.getData(key, null, null, 0, -1, false, false, false);
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		JSONArray arr = (JSONArray) ((JSONObject) res.get("response")).get("docs");

		for (int i = 0; i < arr.size(); i++) {
			JSONObject obj = (JSONObject) arr.get(i);

			if (obj.get("pid") != null && !ret.contains(String.valueOf(obj.get("pid")))) {
				ret += String.valueOf(obj.get("pid")) + ",";
			}

		}

		return ret.length() > 0 ? ret.substring(0, ret.length() - 1) : ret;

	}

	/**
	 * Retrieve transcriptomics genes from Solr with given experiment id(s) and sample id(s)
	 * @author Oral Dalay
	 * param experiment Ids or/and Comparison Ids
	 * @return JSONObject
	 * @throws MalformedURLException
	 */

	public JSONArray getTranscriptomicsGenes(String sampleId, String expId, String keyword) throws MalformedURLException {
		JSONObject res = new JSONObject();
		String query = "";

		if (keyword != null && !keyword.equals("")) {
			query += "(alt_locus_tag:(" + keyword + ") OR refseq_locus_tag:(" + keyword + ")) ";
		}

		if (expId != null && !expId.equals("")) {
			if (query.length() > 0) {
				query += " AND ";
			}
			query += "eid:(" + expId.replaceAll(",", " OR ") + ")";
		}

		if (sampleId != null && !sampleId.equals("")) {
			if (query.length() > 0) {
				query += " AND ";
			}
			query += "pid:(" + sampleId.replaceAll(",", " OR ") + ")";
		}

		ResultType key = new ResultType();
		key.put("keyword", query);
		key.put("fields", "pid,refseq_locus_tag,feature_id,log_ratio,z_score");

		try {
			this.setCurrentInstance(SolrCore.TRANSCRIPTOMICS_GENE);
			res = this.getData(key, null, null, 0, -1, false, false, false);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return (JSONArray) ((JSONObject) res.get("response")).get("docs");
	}

	/**
	 * Retrieve genomic features from Solr with given na_feature_id(s)
	 * @author Harry Yoo
	 * @param key HashMap of search keys.
	 * @return JSONObject
	 * @throws MalformedURLException
	 */
	public JSONObject getFeaturesByID(Map<String, Object> key) throws MalformedURLException {

		JSONObject res = new JSONObject();
		int queryCount = 0;
		// long start, end;

		StringBuffer queryParam = new StringBuffer();
		String startParam = null;
		if (key.containsKey("startParam") && key.get("startParam") != null) {
			startParam = key.get("startParam").toString();
		}
		String limitParam = null;
		if (key.containsKey("limitParam") && key.get("limitParam") != null) {
			limitParam = key.get("limitParam").toString();
		}
		String sortParam = null;
		if (key.containsKey("sortParam") && key.get("sortParam") != null) {
			sortParam = key.get("sortParam").toString();
		}
		else {
			sortParam = "[{\"property\":\"locus_tag\",\"direction\":\"ASC\"}]";
		}

		// building query parameter string
		queryParam.append("(");

		JSONArray tracks = null;
		if (key.containsKey("tracks")) {
			tracks = (JSONArray) key.get("tracks");
			if (tracks.size() > 0) {
				queryParam.append("na_feature_id:(");
				for (int i = 0; i < tracks.size(); i++) {
					JSONObject tr = (JSONObject) tracks.get(i);
					if (tr.get("trackType").toString().equals("Feature") && tr.get("internalId").toString().equals("") == false) {
						if (i > 0) {
							queryParam.append(" OR ");
						}
						queryParam.append(tr.get("internalId").toString());
						queryCount++;
					}
				}
				queryParam.append(")");
			}
		}
		else if (key.containsKey("na_feature_id")) {
			queryParam.append("na_feature_id:" + key.get("na_feature_id").toString());
			queryCount++;
		}
		else if (key.containsKey("na_feature_ids")) {
			queryParam.append("na_feature_id:(");
			boolean isFirst = true;
			for (String fid : key.get("na_feature_ids").toString().split(",")) {
				if (fid.equals("") == false) {
					if (isFirst) {
						isFirst = false;
					}
					else {
						queryParam.append(" OR ");
					}
					queryParam.append(fid);
					queryCount++;
				}
			}
			queryParam.append(")");
		}
		queryParam.append(")");

		// query to Solr
		if (queryCount > 0) {
			this.setCurrentInstance(SolrCore.FEATURE);
			res = this.querySolr(queryParam.toString(), startParam, limitParam, sortParam, queryCount);
		}
		return res;
	}

	/**
	 * Retrieve genome info from Solr with given genome_info_id(s) or tracks info
	 * @author Harry Yoo
	 * @param key HashMap of search keys
	 * @return
	 * @throws MalformedURLException
	 */
	public JSONObject getGenomesByID(Map<String, Object> key) throws MalformedURLException {
		JSONObject res = new JSONObject();
		int queryCount = 0;

		StringBuffer queryParam = new StringBuffer();
		String startParam = null;
		if (key.containsKey("startParam") && key.get("startParam") != null) {
			startParam = key.get("startParam").toString();
		}
		String limitParam = null;
		if (key.containsKey("limitParam") && key.get("limitParam") != null) {
			limitParam = key.get("limitParam").toString();
		}
		String sortParam = null;
		if (key.containsKey("sortParam") && key.get("sortParam") != null) {
			sortParam = key.get("sortParam").toString();
		}

		// build query parameter string
		queryParam.append("(");
		JSONArray tracks = null;
		if (key.containsKey("tracks")) {
			tracks = (JSONArray) key.get("tracks");
			if (tracks.size() > 0) {
				queryParam.append("gid:(");
				for (int i = 0; i < tracks.size(); i++) {
					JSONObject tr = (JSONObject) tracks.get(i);
					if (tr.get("trackType").toString().equals("Genome") && tr.get("internalId").toString().equals("") == false) {
						if (i > 0) {
							queryParam.append(" OR ");
						}
						queryParam.append(tr.get("internalId").toString());
						queryCount++;
					}
				}
				queryParam.append(")");
			}
		}
		else if (key.containsKey("genome_info_id")) {
			queryParam.append("gid:" + key.get("genome_info_id").toString());
			queryCount++;
		}
		else if (key.containsKey("genome_info_ids")) {

			if (key.get("genome_info_ids") != null && key.get("genome_info_ids").equals("") == false) {
				queryParam.append("gid:(");
				boolean isFirst = true;
				for (String gid : key.get("genome_info_ids").toString().split(",")) {
					if (gid.equals("") == false) {
						if (isFirst) {
							isFirst = false;
						}
						else {
							queryParam.append(" OR ");
						}
						queryParam.append(gid);
						queryCount++;
					}
				}
				queryParam.append(")");
			}
		}
		queryParam.append(")");

		// query to Solr
		if (queryCount > 0) {
			this.setCurrentInstance(SolrCore.GENOME);
			res = this.querySolr(queryParam.toString(), startParam, limitParam, sortParam, queryCount);
		}

		return res;
	}

	public JSONObject getExperimentsByID(Map<String, Object> key) throws MalformedURLException {
		JSONObject res = new JSONObject();
		int queryCount = 0;

		StringBuffer queryParam = new StringBuffer();
		String startParam = null;
		if (key.containsKey("startParam") && key.get("startParam") != null) {
			startParam = key.get("startParam").toString();
		}
		String limitParam = null;
		if (key.containsKey("limitParam") && key.get("limitParam") != null) {
			limitParam = key.get("limitParam").toString();
		}
		String sortParam = null;
		if (key.containsKey("sortParam") && key.get("sortParam") != null) {
			sortParam = key.get("sortParam").toString();
		}

		// build query parameter string
		queryParam.append("(");
		JSONArray tracks = null;
		if (key.containsKey("tracks")) {
			tracks = (JSONArray) key.get("tracks");

			if (tracks.size() > 0) {
				queryParam.append("expid:(");
				for (int i = 0; i < tracks.size(); i++) {
					JSONObject tr = (JSONObject) tracks.get(i);
					if (tr.get("trackType").toString().equals("ExpressionExperiment") && tr.get("internalId").toString().equals("") == false) {
						if (i > 0) {
							queryParam.append(" OR ");
						}
						queryParam.append(tr.get("internalId").toString());
						queryCount++;
					}
				}
				queryParam.append(")");
			}
		}
		queryParam.append(")");

		// query to Solr
		if (queryCount > 0) {
			this.setCurrentInstance(SolrCore.TRANSCRIPTOMICS_EXPERIMENT);
			res = this.querySolr(queryParam.toString(), startParam, limitParam, sortParam, queryCount);
		}

		return res;
	}

	/**
	 * Submit query to Solr and get result back
	 * @author Harry Yoo
	 * @param queryParam
	 * @param startParam
	 * @param limitParam
	 * @param sortParam
	 * @param queryCount JSONObject
	 * <dl>
	 * <dd><code>total</code> - total number of rows found</dd>
	 * <dd><code>results</code> - JSONArray of rows in JSONObject format</dd>
	 * </dl>
	 * @return
	 */
	public JSONObject querySolr(String queryParam, String startParam, String limitParam, String sortParam, int queryCount) {
		JSONObject res = new JSONObject();
		SolrQuery query = new SolrQuery();

		query.setQuery(queryParam);
		if (startParam != null) {
			query.setStart(Integer.parseInt(startParam));
		}
		if (limitParam != null) {
			query.setRows(Integer.parseInt(limitParam));
		}
		else {
			query.setRows(queryCount);
		}

		if (sortParam != null) {
			try {
				JSONParser parser = new JSONParser();
				JSONObject jsonSort = (JSONObject) ((JSONArray) parser.parse(sortParam)).get(0);
				query.setSort(jsonSort.get("property").toString(), SolrQuery.ORDER.valueOf(jsonSort.get("direction").toString().toLowerCase()));
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		try {
			QueryResponse qr = server.query(query, SolrRequest.METHOD.POST);
			SolrDocumentList sdl = qr.getResults();
			JSONArray docs = new JSONArray();

			for (SolrDocument d : sdl) {
				JSONObject values = new JSONObject();
				for (Iterator<Map.Entry<String, Object>> i = d.iterator(); i.hasNext();) {
					Map.Entry<String, Object> el = i.next();

					if (el.getKey().equals("release_date") || el.getKey().equals("completion_date")) {
						values.put(el.getKey(), transformDate((Date) el.getValue()));
					}
					else {
						values.put(el.getKey(), el.getValue());
					}
				}
				docs.add(values);
			}
			res.put("total", sdl.getNumFound());
			res.put("results", docs);
		}
		catch (SolrServerException e) {
			e.printStackTrace();
		}

		return res;
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
		query.setFacet(true);
		query.addFacetField(facet);
		query.setFacetLimit(-1);
		query.setFacetMinCount(1);
		query.setFacetSort("count");

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

//	public JSONArray searchSolrRecords(String queryParam) {
//		return searchSolrRecords(queryParam, null);
//	}
//
//	public JSONArray searchSolrRecords(String queryParam, HashMap<?, ?> options) {
//
//		JSONArray docs = new JSONArray();
//		SolrQuery query = new SolrQuery();
//		query.setQuery(queryParam);
//
//		// options
//		if (options != null && options.containsKey("sort")) {
//			String sortParam = options.get("sort").toString();
//			try {
//				JSONParser parser = new JSONParser();
//				JSONObject jsonSort = (JSONObject) ((JSONArray) parser.parse(sortParam)).get(0);
//				query.setSort(jsonSort.get("property").toString(), SolrQuery.ORDER.valueOf(jsonSort.get("direction").toString().toLowerCase()));
//			}
//			catch (Exception ex) {
//				ex.printStackTrace();
//			}
//		}
//
//		try {
//			QueryResponse qr = server.query(query);
//			SolrDocumentList sdl = qr.getResults();
//
//			for (SolrDocument d : sdl) {
//				JSONObject r = new JSONObject();
//				for (Iterator<Map.Entry<String, Object>> i = d.iterator(); i.hasNext();) {
//					Map.Entry<String, Object> el = i.next();
//
//					if (el.getKey().equals("release_date")) {
//						r.put(el.getKey(), transformDate((Date) el.getValue()));
//					}
//					else {
//						r.put(el.getKey(), el.getValue());
//					}
//				}
//				docs.add(r);
//			}
//		}
//		catch (SolrServerException e) {
//			e.printStackTrace();
//		}
//
//		return docs;
//	}

//	public <T> List<T> searchSolrRecords(Class<T> type, SolrQuery query) {
//		List<T> list = null;
//
//		try {
//			QueryResponse qr = server.query(query);
//			list = qr.getBeans(type);
//		}
//		catch (SolrServerException e) {
//			e.printStackTrace();
//		}
//
//		return list;
//	}

	public String transformDate(Date solrDate) {
		if (solrDate != null) {
			return shortDateFormat.format(solrDate);
		}
		else {
			return null;
		}
	}

	public GenomeFeature getFeature(String feature_id) {
		GenomeFeature feature = null;

		try {
			SolrQuery query = new SolrQuery();
			query.setQuery("feature_id:" + feature_id);

			QueryResponse qr = this.getSolrServer(SolrCore.FEATURE).query(query);
			List<GenomeFeature> features = qr.getBeans(GenomeFeature.class);

			if (!features.isEmpty()) {
				feature = features.get(0);
			}
		}
		catch (MalformedURLException | SolrServerException e) {
			e.printStackTrace();
		}


		return feature;
	}

	public Taxonomy getTaxonomy(int taxonId) {
		Taxonomy taxonomy = null;

		try {
			SolrQuery query = new SolrQuery("taxon_id:" + taxonId);

			QueryResponse qr = this.getSolrServer(SolrCore.TAXONOMY).query(query);
			List<Taxonomy> taxonomies = qr.getBeans(Taxonomy.class);

			if (!taxonomies.isEmpty()) {
				taxonomy = taxonomies.get(0);
			}
		}
		catch (MalformedURLException | SolrServerException e) {
			LOGGER.error(e.getMessage(), e);
		}

		return taxonomy;
	}

	public Genome getGenome(String genomeId) {
		Genome genome = null;

		try {
			SolrQuery query = new SolrQuery("genome_id:" + genomeId);

			QueryResponse qr = this.getSolrServer(SolrCore.GENOME).query(query);
			List<Genome> genomes = qr.getBeans(Genome.class);

			if (!genomes.isEmpty()) {
				genome = genomes.get(0);
			}
		}
		catch (MalformedURLException | SolrServerException e) {
			LOGGER.error(e.getMessage(), e);
		}

		return genome;
	}

	/**
	 * used by feature level breadcrumb
	 * @param feature_id
	 * @return
	 */
	public GenomeFeature getPATRICFeature(String feature_id) {
		GenomeFeature pf = null;

		try {
			LBHttpSolrServer lbHttpSolrServer = this.getSolrServer(SolrCore.FEATURE);

			SolrQuery query = new SolrQuery();
			query.setQuery("feature_id:" + feature_id);

			QueryResponse qr = lbHttpSolrServer.query(query);
			List<GenomeFeature> res = qr.getBeans(GenomeFeature.class);

			if (!res.isEmpty()) {
				GenomeFeature f = res.get(0);

				if (f.getAnnotation().equals("PATRIC")) { // if this is PATRIC
					pf = f;
				}
				else { // get corresponding PARIC
					SolrQuery query2 = new SolrQuery();
					query2.setQuery("pos_group:" + f.getPosGroupInQuote() + " AND feature_type:" + f.getFeatureType() + " AND annotation:PATRIC");

					QueryResponse qr2 = lbHttpSolrServer.query(query2);
					List<GenomeFeature> res2 = qr2.getBeans(GenomeFeature.class);
					if (!res2.isEmpty()) { // found PATRIC feature
						pf = res2.get(0);
					}
					else {
						pf = f;
					}
				}
			}
		}
		catch (MalformedURLException | SolrServerException e) {
			e.printStackTrace();
		}

		return pf;
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
	public JSONObject facetFieldstoJSONObject(QueryResponse qr) {
		JSONObject result = new JSONObject();

		List<FacetField> facets = qr.getFacetFields();
		for (FacetField facet : facets) {
			JSONObject facet_json = new JSONObject();
			int count = 0;
			JSONArray attributes = new JSONArray();
			List<FacetField.Count> facetEntries = facet.getValues();

			if (facet.getValues() != null) {
				for (FacetField.Count entry : facetEntries) {
					JSONObject attribute = new JSONObject();
					// attribute_json.put("text", facetEntry.getName() + " <span style=\"color: #888;\"> (" + facetEntry.getCount() + ") </span>");
					attribute.put("text", entry.getName() + " <span>(" + entry.getCount() + ")</span>");
					attribute.put("value", entry.getName());
					attribute.put("count", entry.getCount());
					attributes.add(attribute);

					count += (int)(long)entry.getCount();
				}
			}

			facet_json.put("value", facet.getName());
			facet_json.put("count", count);
			// facet_json.put("text", facet.getName() + " <span style=\"color: #888;\"> (" + count + ") </span>");
			facet_json.put("text", facet.getName() + " <span>(" + count + ")</span>");
			facet_json.put("attributes", attributes);
			result.put(facet.getName(), facet_json);
		}

		List<RangeFacet> ranges = qr.getFacetRanges();
		for (RangeFacet range : ranges) {
			JSONObject facet_json = new JSONObject();
			int count = 0;
			JSONArray attributes = new JSONArray();
			List<RangeFacet.Count> rangeEntries = range.getCounts();

			if (rangeEntries != null) {
				for (RangeFacet.Count entry : rangeEntries) {
					if (entry.getCount() > 0) {
						JSONObject attribute_json = new JSONObject();
						String rangeValue = entry.getValue().split("-")[0];
						// attribute_json.put("text", rangeValue + " <span style=\"color: #888;\"> (" + entry.getCount() + ") </span>");
						attribute_json.put("text", rangeValue + " <span>(" + entry.getCount() + ")</span>");
						attribute_json.put("value", rangeValue);
						attribute_json.put("count", entry.getCount());
						attributes.add(attribute_json);

						count += (int)(long)entry.getCount();
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

			facet_json.put("value", range.getName());
			facet_json.put("count", count);
			// facet_json.put("text", range.getName() + " <span style=\"color: #888;\"> (" + count + ") </span>");
			facet_json.put("text", range.getName() + " <span>(" + count + ")</span>");
			facet_json.put("attributes", attributes);
			result.put(range.getName(), facet_json);
		}
		return result;
	}
}
