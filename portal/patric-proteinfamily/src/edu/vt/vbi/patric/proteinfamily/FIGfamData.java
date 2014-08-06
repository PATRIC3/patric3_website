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
package edu.vt.vbi.patric.proteinfamily;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


//import javax.portlet.PortletSession;
import javax.portlet.ResourceRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.apache.solr.client.solrj.response.FieldStatsInfo;
import org.apache.solr.client.solrj.response.PivotField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.lob.SerializableClob;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import edu.vt.vbi.patric.msa.Aligner;
import edu.vt.vbi.patric.msa.SequenceData;
import edu.vt.vbi.patric.common.SolrCore;
import edu.vt.vbi.patric.common.SolrInterface;

public class FIGfamData {

	public final static int FIGFAM_COUNT = 0;

	public final static int SPECIES_COUNT = 1;

	public final static int MIN_AA = 2;

	public final static int MAX_AA = 3;

	public final static int FEATURE_COUNT = 4;

	public final static int DETAILS_STATS_ROOM = 5;

	public final static String FIGFAM_ID = "figfamId";

	private final static String algorithmRestrict = " and algorithm ='RAST'";;

	protected static SessionFactory factory = null;

	public static void setSessionFactory(SessionFactory sf) {
		factory = sf;
	}

	private final static String TAXON_ID_TO_NAME = "select tn.name from sres.taxon t, sres.taxonname tn" + " where t.taxon_id = tn.taxon_id"
			+ " and name_class = 'scientific name'" + " and ncbi_tax_id = ";

	private final static String emptyText = "";

	private final static String ORTHO_ENV_NAME = "ORTHO_WORK_HOME";

	private final static int IN_MAX = 333;

	private final static String GET_GROUP_TAIL = " order by fi.name, gi.genome_name";

	private final static String GET_DETAILS_TAIL = "order by genome_name";

	// Harry modified
	private final static String GET_FASTA_START = "select genome_name, accession, source_id, product, translation"
			+ " from app.dnafeature where name = 'CDS' ";

	private final static String GET_ALIGN_START = "select gi.common_name, df.source_id, df.translation"
			+ " from app.dnafeature df, cas.genomeinfo gi where" + " gi.genome_info_id = df.genome_info_id";

	private final static String START_BLAST_FASTA = "select translation from app.dnafeature where na_feature_id = ";

	private final static String GET_HTML_START = "select fi.name, nf.genome_name, nf.source_id, nf.aa_length, nf.product "
			+ "from sres.figfaminfo fi, dots.figfamassociation fa, app.dnafeature nf where nf.na_feature_id = fa.na_feature_id "
			+ "and fa.figfam_info_id = fi.figfam_info_id and nf.name = 'CDS'";

	private SolrInterface solr = new SolrInterface();

	private Object[] replaceNulls(Object[] objs) {
		int count = objs.length;
		Object[] result = new Object[count];
		System.arraycopy(objs, 0, result, 0, count);
		for (int i = 0; i < count; i++) {
			if (result[i] == null) {
				result[i] = emptyText;
			}
		}
		return result;
	}

	// This function is called to get an ordering of the Figfams based on order of occurrence only for the ref. genome
	// It has nothing to do with the other genomes in a display.
	// This function returns an ordering of the Figfam ID's for the reference genome with paralogs removed
	// The counting for the number of paralogs occurs in the javascript code (I think)
	public JSONArray getSyntonyOrder(ResourceRequest req) {
		String idText = req.getParameter("syntonyId");
		JSONArray json_arr = null;

		if (idText != null) {
			int genomeId = Integer.parseInt(idText);

			try {
				solr.setCurrentInstance(SolrCore.FEATURE);
			}
			catch (MalformedURLException e1) {
				e1.printStackTrace();
			}

			long end_ms, start_ms = System.currentTimeMillis();

			LBHttpSolrServer server = solr.getServer();

			SolrQuery solr_query = new SolrQuery("gid:" + genomeId);
			solr_query.setRows(1);
			solr_query.setFilterQueries("annotation:PATRIC AND feature_type:CDS");
			solr_query.addField("figfam_id");

			QueryResponse qr;
			SolrDocumentList sdl;

			try {
				qr = server.query(solr_query, SolrRequest.METHOD.POST);
				sdl = qr.getResults();

				solr_query.setRows((int) sdl.getNumFound());
				solr_query.addSort("locus_tag", SolrQuery.ORDER.asc);
			}
			catch (SolrServerException e) {
				e.printStackTrace();
			}

			System.out.println("FIGfam:getSyntonyOrder() " + solr_query.toString());

			int orderSet = 0;
			ArrayList<SyntonyOrder> collect = new ArrayList<SyntonyOrder>();
			try {
				qr = server.query(solr_query, SolrRequest.METHOD.POST);
				sdl = qr.getResults();

				end_ms = System.currentTimeMillis();

				System.out.println("Genome anchoring query time - " + (end_ms - start_ms));

				start_ms = System.currentTimeMillis();
				for (SolrDocument d : sdl) {
					for (Iterator<Map.Entry<String, Object>> i = d.iterator(); i.hasNext();) {
						Map.Entry<String, Object> el = i.next();
						if (el.getKey().toString().equals("figfam_id")) {
							collect.add(new SyntonyOrder(el.getValue().toString(), orderSet));
							++orderSet;
						}
					}
				}

			}
			catch (SolrServerException e) {
				e.printStackTrace();
			}

			if (0 < collect.size()) {
				json_arr = new JSONArray();
				SyntonyOrder[] orderSave = new SyntonyOrder[collect.size()];
				collect.toArray(orderSave);// orderSave is array in order of Figfam ID
				SyntonyOrder[] toSort = new SyntonyOrder[collect.size()];
				System.arraycopy(orderSave, 0, toSort, 0, toSort.length);// copy the array so it can be sorted based on
																			// position in the genome
				Arrays.sort(toSort); // sort based on figfamIDs
				SyntonyOrder start = toSort[0];
				for (int i = 1; i < toSort.length; i++) {
					start = start.mergeSameId(toSort[i]);// set syntonyAt -1 to those objects which occur multiple times
				}
				orderSet = 0;
				for (int i = 0; i < orderSave.length; i++) {
					orderSet = (orderSave[i]).compressAt(orderSet); // adjusts the syntonyAt number to get the correct
																	// column based on replicon with -1's removed
				}
				for (int i = 0; i < toSort.length; i++) {// writes all those that don't have -1's
					(toSort[i]).write(json_arr);
				}
			}

			end_ms = System.currentTimeMillis();

			System.out.println("Genome anchoring post processing time - " + (end_ms - start_ms));
		}

		return json_arr;
	}

	@SuppressWarnings("unchecked")
	public void getLocusTags(ResourceRequest req, PrintWriter writer) {

		JSONArray arr = new JSONArray();
		try {
			solr.setCurrentInstance(SolrCore.FEATURE);
		}
		catch (MalformedURLException e1) {
			e1.printStackTrace();
		}

		SolrQuery solr_query = new SolrQuery();
		solr_query.setQuery("gid:(" + req.getParameter("genomeIds") + ") AND figfam_id:(" + req.getParameter("figfamIds") + ")");
		solr_query.setFilterQueries("annotation:PATRIC AND feature_type:CDS");
		solr_query.addField("locus_tag");
		solr_query.setRows(150000);

		System.out.println("FIGfamData:getLocusTags() " + solr_query.toString());

		QueryResponse qr;
		SolrDocumentList sdl;

		try {
			qr = solr.getServer().query(solr_query, SolrRequest.METHOD.POST);
			sdl = qr.getResults();

			for (SolrDocument d : sdl) {
				for (Iterator<Map.Entry<String, Object>> i = d.iterator(); i.hasNext();) {
					Map.Entry<String, Object> el = i.next();
					arr.add(el.getValue());
				}
			}
		}
		catch (SolrServerException e) {
			e.printStackTrace();
		}

		JSONObject data = new JSONObject();
		data.put("data", arr);
		try {
			data.writeJSONString(writer);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public JSONArray getDetails(String genomeIds, String figfamIds) {

		JSONArray arr = new JSONArray();

		try {
			solr.setCurrentInstance(SolrCore.FEATURE);
		}
		catch (MalformedURLException e1) {
			e1.printStackTrace();
		}

		SolrQuery solr_query = new SolrQuery();
		solr_query.setQuery("gid:(" + genomeIds + ") AND figfam_id:(" + figfamIds + ")");
		solr_query.setFields("figfam_id, genome_name, accession, locus_tag, start_max, end_min, na_length, strand, aa_length, gene, product");
		solr_query.setFilterQueries("annotation:PATRIC AND feature_type:CDS");
		solr_query.setRows(1500000);

		System.out.println("FIGfamData:getDetails() " + solr_query.toString());

		QueryResponse qr;
		SolrDocumentList sdl;

		try {
			qr = solr.getServer().query(solr_query, SolrRequest.METHOD.POST);
			sdl = qr.getResults();

			for (SolrDocument d : sdl) {
				JSONObject values = new JSONObject();
				for (Iterator<Map.Entry<String, Object>> i = d.iterator(); i.hasNext();) {
					Map.Entry<String, Object> el = i.next();
					values.put(el.getKey(), el.getValue());
				}
				arr.add(values);
			}
		}
		catch (SolrServerException e) {
			e.printStackTrace();
		}
		return arr;
	}

	public Aligner getAlignment(char needHtml, ResourceRequest req) {
		Aligner result = null;
		String groupID = req.getParameter(FIGFAM_ID);
		try {
			SequenceData[] sequences = getProteins(req);
			result = new Aligner(needHtml, groupID, sequences);
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public Aligner getFeatureAlignment(char needHtml, ResourceRequest req) {
		Aligner result = null;
		try {
			SequenceData[] sequences = getFeatureProteins(req);
			result = new Aligner(needHtml, req.getParameter(FIGFAM_ID), sequences);
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static File getOrthoPath(String subDir) {
		File result = null;
		Map<?, ?> check = System.getenv();
		String homeDir = (String) (check.get(ORTHO_ENV_NAME));
		if (homeDir == null) {
			homeDir = "/tmp";
		}
		if (homeDir != null) {
			result = new File(homeDir + File.separator + subDir);
		}
		return result;
	}

	public int[] getIntArray(String text) {

		String[] b = text.split(",");
		int[] ret = new int[b.length];
		for (int i = 0; i < b.length; i++) {
			ret[i] = Integer.parseInt(b[i]);
		}

		return ret;

	}

	public String getGenomeIdsForTaxon(String taxon) {
		String genomeIds = null;
		SolrQuery query = new SolrQuery();
		query.setQuery("rast_cds:[1 TO *] AND taxon_lineage_ids:" + taxon);
		query.addField("genome_info_id");
		query.setRows(500000);
		QueryResponse qr;
		
		System.out.println("FIGfamData:getGenomeIdsForTaxon() " + query.toString());
		
		try {
			solr.setCurrentInstance(SolrCore.GENOME);
			qr = solr.getServer().query(query);
			List<String> listGenomeId = qr.getBeans(String.class);
			genomeIds = StringUtils.join(listGenomeId, ",");
		}
		catch (SolrServerException e) {
			e.printStackTrace();
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return genomeIds;
	}

	public String getTaxonName(String taxon) {
		StringBuilder query = new StringBuilder(0x1000);
		query.append(TAXON_ID_TO_NAME);
		query.append(taxon);
		String result = null;
		Iterator<?> iter = getSqlIterator(query);
		if (iter.hasNext()) {
			result = (String) (iter.next());
		}
		return result;
	}

	private SequenceData[] getProteins(ResourceRequest req) throws SQLException {
		SequenceData[] result = null;
		String featureList = req.getParameter("featureIds");
		if (featureList != null) {
			String[] featuresText = featureList.split(",");
			int[] featureIds = new int[featuresText.length];
			for (int i = 0; i < featureIds.length; i++) {
				featureIds[i] = Integer.parseInt(featuresText[i]);
			}
			result = getFeatureSequences(featureIds);
		}
		return result;
	}

	private SequenceData[] getFeatureSequences(int[] featureIds) {
		ArrayList<SequenceData> collect = new ArrayList<SequenceData>();
		StringBuilder query = new StringBuilder(0x1000);
		query.append(GET_ALIGN_START);
		query.append(" and ");
		restrictByNumbers("df.na_feature_id", featureIds, query);
		query.append(" order by gi.common_name");
		Iterator<?> iter = getSqlIterator(query);
		while (iter.hasNext()) {
			Object[] line = replaceNulls((Object[]) iter.next());
			SequenceData toAdd = new SequenceData(line[0], line[1], line[2]);
			collect.add(toAdd);
		}
		SequenceData[] result = new SequenceData[collect.size()];
		collect.toArray(result);
		return result;
	}

	private SequenceData[] getFeatureProteins(ResourceRequest req) throws SQLException {
		SequenceData[] result = null;
		String featuresString = req.getParameter("featureIds");
		if (featuresString != null) {
			String[] idList = featuresString.split(",");
			int[] featureIds = new int[idList.length];
			for (int i = 0; i < featureIds.length; i++) {
				featureIds[i] = Integer.parseInt(idList[i]);
			}
			result = getFeatureSequences(featureIds);
		}
		return result;
	}

	public String[] getFastaLines(ResourceRequest req) {
		String featureCount = req.getParameter("featureCount");
		ArrayList<String> lines = new ArrayList<String>();
		if (featureCount != null) {
			int[] featureIds = new int[Integer.parseInt(featureCount)];
			for (int i = 0; i < featureIds.length; i++) {
				featureIds[i] = Integer.parseInt(req.getParameter("feature" + i));
			}
			StringBuilder query = new StringBuilder(0x1000);
			query.append(GET_FASTA_START);
			query.append(algorithmRestrict);
			query.append(" and ");
			restrictByNumbers("na_feature_id", featureIds, query);
			query.append(GET_DETAILS_TAIL);

			Iterator<?> iter = getSqlIterator(query);
			while (iter.hasNext()) {
				Object[] line = replaceNulls((Object[]) iter.next());
				String titleLine = ">locus|";
				titleLine += line[2] + " ";
				titleLine += line[3] + " [";
				titleLine += line[0] + " | ";
				titleLine += line[1] + "]";
				lines.add(titleLine);
				try {
					SerializableClob clobSeq = (SerializableClob) (line[4]);
					String strSeq = IOUtils.toString(clobSeq.getAsciiStream(), "UTF-8");
					lines.add(strSeq);
				}
				catch (Exception ex) {
					System.out.println(ex.toString());
				}

			}
		}
		String[] result = new String[lines.size()];
		lines.toArray(result);
		return result;
	}

	public void setHtmlDetailRows(ArrayList<?> genomesList, ArrayList<?> figfamIds, StringBuilder output) {
		output.append("<tr>");
		output.append("<th>Id</th>");
		output.append("<th>Genome</th>");
		output.append("<th>Source Id</th>");
		output.append("<th>AA Length</th>");
		output.append("<th>Protein Product</th>");
		output.append("</tr>");
		StringBuilder query = new StringBuilder(0x1000);
		query.append(GET_HTML_START);
		query.append(algorithmRestrict);
		query.append(" and ");
		restrictByNumbers("nf.genome_info_id", genomesList, query);
		query.append(" and ");
		restrictByList("fi.name", figfamIds, query);
		query.append(GET_GROUP_TAIL);

		Iterator<?> iter = getSqlIterator(query);
		while (iter.hasNext()) {
			Object[] line = replaceNulls((Object[]) iter.next());
			output.append("<tr>");
			output.append("<td>" + line[0] + "</td>");
			output.append("<td>" + line[1] + "</td>");
			output.append("<td>" + line[2] + "</td>");
			output.append("<td>" + line[3] + "</td>");
			output.append("<td>" + line[4] + "</td>");
			output.append("</tr>");
		}
	}

	/*
	 * private GenomeBitSetter[] getGenomeBitSetters(ResourceRequest req) { GenomeBitSetter[] result = null; String listText =
	 * req.getParameter("genomeIds"); if (listText != null) { String[] textIds = listText.split(","); result = new GenomeBitSetter[textIds.length];
	 * for (int i = 0; i < result.length; i++) { result[i] = new GenomeBitSetter(i, Integer.parseInt(textIds[i])); } Arrays.sort(result); }
	 * 
	 * return result; }
	 */
	private void restrictByNumbers(String restricted, ArrayList<?> textNumbers, StringBuilder toBuild) {
		int count = textNumbers.size();
		if (0 < count) {
			Iterator<?> it = textNumbers.iterator();
			String value = (String) (it.next());
			if (count == 1) {
				toBuild.append(restricted + " = " + value);
			}
			else if (count <= IN_MAX) {
				toBuild.append(restricted + " in (" + value);
				while (it.hasNext()) {
					value = (String) (it.next());
					toBuild.append(", " + value);
				}
				toBuild.append(")");
			}
			else {
				toBuild.append("((" + restricted + " in (" + value);
				int at = 1;
				int nextBreak = IN_MAX;
				while (it.hasNext()) {
					value = (String) (it.next());
					if (at <= nextBreak) {
						toBuild.append(", " + value);
					}
					else {
						toBuild.append(")) or (" + restricted + " in (" + value);
						nextBreak += IN_MAX;
					}
					++at;
				}
				toBuild.append(")))");
			}
		}
	}

	private void restrictByNumbers(String restricted, int[] idList, StringBuilder toBuild) {
		int count = idList.length;
		if (0 < count) {
			if (count == 1) {
				toBuild.append(restricted + " = " + idList[0]);
			}
			else if (count <= IN_MAX) {
				toBuild.append(restricted + " in (" + idList[0]);
				for (int i = 1; i < count; i++) {
					toBuild.append(", " + idList[i]);
				}
				toBuild.append(")");
			}
			else {
				toBuild.append("((" + restricted + " in (" + idList[0]);
				int nextBreak = IN_MAX;
				for (int at = 1; at < count; at++) {
					if (at < nextBreak) {
						toBuild.append(", " + idList[at]);
					}
					else {
						toBuild.append(")) or (" + restricted + " in (" + idList[at]);
						nextBreak += IN_MAX;
					}
				}
				toBuild.append(")))");
			}
		}
	}

	/*
	 * private void restrictByNumbers(String restricted, GenomeBitSetter[] idList, StringBuilder toBuild) { int count = idList.length; if (0 < count)
	 * { if (count == 1) { toBuild.append(restricted + " = " + (idList[0]).genomeId); } else if (count <= IN_MAX) { toBuild.append(restricted +
	 * " in (" + (idList[0]).genomeId); for (int i = 1; i < count; i++) { toBuild.append(", " + (idList[i]).genomeId); } toBuild.append(")"); } else {
	 * toBuild.append("((" + restricted + " in (" + (idList[0]).genomeId); int nextBreak = IN_MAX; for (int at = 1; at < count; at++) { if (at <=
	 * nextBreak) { toBuild.append(", " + (idList[at]).genomeId); } else { toBuild.append(")) or (" + restricted + " in (" + (idList[at]).genomeId);
	 * nextBreak += IN_MAX; } ++at; } toBuild.append(")))"); } } }
	 */
	private void restrictByList(String restricted, ArrayList<?> list, StringBuilder toBuild) {
		int count = list.size();
		if (0 < count) {
			Iterator<?> it = list.iterator();
			String value = (String) (it.next());
			if (count == 1) {
				toBuild.append(restricted + " = '" + value + "'");
			}
			else if (count <= IN_MAX) {
				toBuild.append(restricted + " in ('" + value + "'");
				while (it.hasNext()) {
					value = (String) (it.next());
					toBuild.append(", '" + value + "'");
				}
				toBuild.append(")");
			}
			else {
				toBuild.append("((" + restricted + " in ('" + value + "'");
				int at = 1;
				int nextBreak = IN_MAX;
				while (it.hasNext()) {
					value = (String) (it.next());
					if (at <= nextBreak) {
						toBuild.append(", '" + value + "'");
					}
					else {
						toBuild.append(")) or (" + restricted + " in ('" + value + "'");
						nextBreak += IN_MAX;
					}
					++at;
				}
				toBuild.append(")))");
			}
		}
	}

	public void getFeatureIds(ResourceRequest req, PrintWriter writer, String keyword) {

		try {
			solr.setCurrentInstance(SolrCore.FEATURE);
		}
		catch (MalformedURLException e1) {
			e1.printStackTrace();
		}

		SolrQuery solr_query = new SolrQuery();
		solr_query.setQuery(keyword);
		solr_query.setRows(1);
		solr_query.addField("na_feature_id");

		QueryResponse qr;
		SolrDocumentList sdl;
		long rows = 0;
		String ret = "";
		try {
			qr = solr.getServer().query(solr_query, SolrRequest.METHOD.POST);
			sdl = qr.getResults();
			rows = sdl.getNumFound();
			solr_query.setRows((int) rows);
			qr = solr.getServer().query(solr_query, SolrRequest.METHOD.POST);
			sdl = qr.getResults();

			for (SolrDocument d : sdl) {
				for (Iterator<Map.Entry<String, Object>> i = d.iterator(); i.hasNext();) {
					Map.Entry<String, Object> el = i.next();
					if (el.getKey().equals("na_feature_id")) {
						ret += el.getValue().toString() + ",";
					}
				}
			}

			writer.write(ret.substring(0, ret.length() - 1));
		}
		catch (SolrServerException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void getGenomeDetails(ResourceRequest req, PrintWriter writer) throws IOException {
		solr.setCurrentInstance(SolrCore.GENOME);

		// ResultType key = new ResultType();
		// key.put("keyword", req.getParameter("keyword"));
		//
		// String keyword = req.getParameter("keyword");
		// if (keyword != null && !keyword.equals("")) {
		// key.put("keyword", req.getParameter("keyword"));
		// }
		// String fields = req.getParameter("fields");
		// if (fields != null && !fields.equals("")) {
		// key.put("fields", fields);
		// }
		// JSONObject object = solr.getData(key, null, null, 0, -1, false, false, false);

		// System.out.println(req.getParameterMap().toString());

		String cType = req.getParameter("context_type");
		String cId = req.getParameter("context_id");
		String keyword = "";
		if (cType != null && cType.equals("taxon") && cId != null && cId.equals("") == false) {
			keyword = "rast_cds:[1 TO *] AND taxon_lineage_ids:" + cId;
		}
		else if (req.getParameter("keyword") != null) {
			keyword = req.getParameter("keyword");
		}
		String fields = req.getParameter("fields");

		SolrQuery query = new SolrQuery();
		query.setQuery(keyword);
		if (fields != null && !fields.equals("")) {
			query.addField(fields);
		}
		query.setRows(500000);

		System.out.println("FIGfamData:getGenomeDetails() " + query.toString());
		JSONObject object = null;
		try {
			object = solr.ConverttoJSON(solr.getServer(), query, false, false);
		}
		catch (Exception ex) {
			System.out.println(ex.getMessage());
			System.out.println("params: " +req.getParameterMap().toString());
		}

		JSONObject obj = (JSONObject) object.get("response");
		JSONArray obj1 = (JSONArray) obj.get("docs");

		JSONObject jsonResult = new JSONObject();
		jsonResult.put("results", obj1);
		jsonResult.put("total", obj.get("numFound"));
		jsonResult.writeJSONString(writer);
	}

	@SuppressWarnings("unchecked")
	public void getGroupStats(ResourceRequest req, PrintWriter writer) throws IOException {
		solr.setCurrentInstance(SolrCore.FEATURE);

		// long start_ms, end_ms;
		JSONObject figfams = new JSONObject();
		String figfam_ids = "";
		String[] genomeIds = req.getParameter("genomeIds").toString().split(",");
		String[] genomeIdsStr = new String[genomeIds.length];
		List<String> genomeIdsArr = Arrays.asList(genomeIds);

		// getting genome counts per figfamID (figfam)
		SolrQuery solr_query = new SolrQuery("*:*");
		solr_query.addFilterQuery(getSolrQuery(req));
		solr_query.addFilterQuery("annotation:PATRIC AND feature_type:CDS");
		solr_query.setRows(0);
		solr_query.setFacet(true);
		solr_query.addFacetPivotField("figfam_id,gid");
		solr_query.setFacetMinCount(1);
		solr_query.setFacetLimit(-1);
		solr_query.setSort("figfam_id", SolrQuery.ORDER.asc);

		System.out.println("FIGfam:getStroupStats() 1/3 " + solr_query.toString());

		try {
			// long start_ms = System.currentTimeMillis();
			QueryResponse qr = solr.getServer().query(solr_query);// , SolrRequest.METHOD.POST);
			// long end_ms = System.currentTimeMillis();
			// System.out.println("Query time - 1st - " + (end_ms - start_ms));
			// start_ms = System.currentTimeMillis();

			NamedList<List<PivotField>> pivots = qr.getFacetPivot();

			for (Map.Entry<String, List<PivotField>> pivot : pivots) {
				List<PivotField> pivotEntries = pivot.getValue();
				if (pivotEntries != null) {
					for (PivotField pivotEntry : pivotEntries) {
						JSONObject obj2 = new JSONObject();

						List<PivotField> pivotGenomes = pivotEntry.getPivot();
						int count = 0, index;
						String hex;

						Arrays.fill(genomeIdsStr, "00");

						for (PivotField pivotGenome : pivotGenomes) {
							// index = Arrays.asList(genomeIds).indexOf(pivotGenome.getValue().toString());
							index = genomeIdsArr.indexOf(pivotGenome.getValue().toString());
							hex = Integer.toHexString(pivotGenome.getCount());
							genomeIdsStr[index] = hex.length() < 2 ? "0" + hex : hex;
							count += pivotGenome.getCount();
						}

						if (figfam_ids.length() == 0)
							figfam_ids += pivotEntry.getValue();
						else
							figfam_ids += " OR " + pivotEntry.getValue();

						obj2.put("genomes", StringUtils.join(genomeIdsStr, ""));
						obj2.put("genome_count", pivotGenomes.size());
						obj2.put("feature_count", count);

						figfams.put(pivotEntry.getValue(), obj2);
					}
				}
			}
			// end_ms = System.currentTimeMillis();
			// System.out.println("Procesing time - 1st - " + (end_ms - start_ms));

			// System.out.println(figfams.toJSONString());
		}
		catch (Exception e) {
			// e.printStackTrace();
			System.out.println(e.getMessage());
			System.out.println("params: " +req.getParameterMap().toString());
		}

		// getting distribution of aa length in each proteim family (dnafeature: need to modify the type of figfam_id column)

		solr_query = new SolrQuery("*:*");
		solr_query.addFilterQuery(getSolrQuery(req));
		solr_query.setRows(0);
		solr_query.set("stats", "true");
		solr_query.set("stats.field", "aa_length");
		solr_query.set("stats.facet", "figfam_id");

		// System.out.println("FIGfam:getStroupStats() 2/3 " + solr_query.toString());

		try {
			// start_ms = System.currentTimeMillis();
			QueryResponse qr = solr.getServer().query(solr_query, SolrRequest.METHOD.POST);
			// end_ms = System.currentTimeMillis();
			// System.out.println("Query time - 2nd - "+ (end_ms - start_ms));
			// start_ms = System.currentTimeMillis();
			Map<?, ?> stats_map = qr.getFieldStatsInfo();

			if (stats_map != null) {
				FieldStatsInfo stats = (FieldStatsInfo) stats_map.get("aa_length");
				if (stats != null) {
					List<FieldStatsInfo> fieldStats = stats.getFacets().get("figfam_id");
					for (FieldStatsInfo fieldStat : fieldStats) {
						JSONObject obj = (JSONObject) figfams.get(fieldStat.getName());
						JSONObject obj2 = new JSONObject();
						obj2.put("max", fieldStat.getMax());
						obj2.put("min", fieldStat.getMin());
						obj2.put("mean", fieldStat.getMean());
						obj2.put("stddev", Math.round(fieldStat.getStddev() * 1000) / (double) 1000);

						if (obj == null) {
							obj = new JSONObject();
						}

						obj.put("stats", obj2);
						figfams.put(fieldStat.getName(), obj);
					}
					// end_ms = System.currentTimeMillis();
					// System.out.println("Processing time - 2nd - " + (end_ms-start_ms));
				}
			}
			// System.out.println(figfams.toJSONString());
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println("params: " +req.getParameterMap().toString());
		}

		// getting distinct figfam_product
		if (!figfam_ids.isEmpty()) {

			solr.setCurrentInstance(SolrCore.FIGFAM_DIC);

			solr_query = new SolrQuery();
			solr_query.setQuery("*:*");
			solr_query.addFilterQuery("figfam_id:(" + figfam_ids + ")");
			solr_query.addField("figfam_id, figfam_product");
			solr_query.setRows(figfams.size());

			// System.out.println("FIGfam:getStroupStats() 3/3 " + solr_query.toString());

			try {
				// start_ms = System.currentTimeMillis();
				QueryResponse qr = solr.getServer().query(solr_query, SolrRequest.METHOD.POST);
				// end_ms = System.currentTimeMillis();
				// System.out.println("Query time - 3rd - "+ (end_ms - start_ms));
				// start_ms = System.currentTimeMillis();
				SolrDocumentList sdl = qr.getResults();

				for (SolrDocument d : sdl) {
					JSONObject obj = null;
					String k = "", description = "";
					for (Iterator<Map.Entry<String, Object>> i = d.iterator(); i.hasNext();) {
						Map.Entry<String, Object> el = i.next();
						if (el.getKey().equals("figfam_id")) {
							k = el.getValue().toString();
							if (!description.equals("")) {
								obj = (JSONObject) figfams.get(k);
								obj.put("description", description);
								figfams.put(k, obj);
							}
						}
						// System.out.println(el.getValue().toString());

						if (el.getKey().equals("figfam_product")) {
							if (!k.equals("")) {
								obj = (JSONObject) figfams.get(k);
								obj.put("description", el.getValue().toString());
								figfams.put(k, obj);
							}
							else {
								description = el.getValue().toString();
							}
						}
					}
				}
				// end_ms = System.currentTimeMillis();
				// System.out.println("Processing time - 3rd - " + (end_ms-start_ms));
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
				System.out.println("params: " +req.getParameterMap().toString());
			}
			// start_ms = System.currentTimeMillis();
			figfams.writeJSONString(writer);
			// end_ms = System.currentTimeMillis();
			// System.out.println("Writing to response - "+ (end_ms - start_ms));
		}
	}

	public String getSingleFasta(String featureId) {
		String result = null;
		StringBuilder query = new StringBuilder(0x1000);
		query.append(START_BLAST_FASTA);
		query.append(featureId);
		query.append(algorithmRestrict);

		Iterator<?> iter = getSqlIterator(query);
		if (iter.hasNext()) {
			SerializableClob clobSeq = (SerializableClob) (iter.next());
			try {
				result = IOUtils.toString(clobSeq.getAsciiStream(), "UTF-8");
			}
			catch (Exception ex) {
				System.out.println(ex.toString());
			}
		}
		return result;
	}

	private Iterator<?> getSqlIterator(StringBuilder query) {
		Session session = factory.getCurrentSession();
		session.beginTransaction();
		SQLQuery q = session.createSQLQuery(query.toString());

		List<?> rset = q.list();
		session.getTransaction().commit();

		return (rset.iterator());
	}

	private class SyntonyOrder implements Comparable<SyntonyOrder> {
		String groupId;

		int syntonyAt;

		SyntonyOrder(String id, int at) {
			groupId = id;
			syntonyAt = at;
		}

		int compressAt(int orderSet) {
			if (0 <= syntonyAt) {
				syntonyAt = orderSet;
				++orderSet;
			}
			return orderSet;
		}

		@SuppressWarnings("unchecked")
		void write(JSONArray json_arr) {
			if (0 <= syntonyAt) {
				JSONObject j = new JSONObject();
				j.put("syntonyAt", this.syntonyAt);
				j.put("groupId", this.groupId);
				json_arr.add(j);
			}
		}

		SyntonyOrder mergeSameId(SyntonyOrder other) {
			SyntonyOrder result = other;
			if ((this.groupId).equals(other.groupId)) {
				other.syntonyAt = -1;
				result = this;
			}
			return result;
		}

		public int compareTo(SyntonyOrder o) {
			int result = (this.groupId).compareTo(o.groupId);
			if (result == 0) {
				result = this.syntonyAt - o.syntonyAt;
			}
			return result;
		}
	}

	public String getSolrQuery(ResourceRequest req) {
		String keyword = "";

		if (req.getParameter("keyword") != null && !req.getParameter("keyword").equals("")) {
			keyword += "(" + solr.KeywordReplace(req.getParameter("keyword")) + ")";
		}

		String cType = req.getParameter("context_type");
		String cId = req.getParameter("context_id");
		if (cType != null && cType.equals("taxon") && cId != null && cId.equals("") == false) {
			keyword += SolrCore.GENOME.getSolrCoreJoin("gid", "gid", "taxon_lineage_ids:" + cId);
		}
		else {
			String listText = req.getParameter("genomeIds");

			if (listText != null) {
				if (req.getParameter("keyword") != null && !req.getParameter("keyword").equals(""))
					keyword += " AND ";
				keyword += "(gid:(" + listText.replaceAll(",", " OR ") + "))";
			}
		}

		return keyword;
	}
}
