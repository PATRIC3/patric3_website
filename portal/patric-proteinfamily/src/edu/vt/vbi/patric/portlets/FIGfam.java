/**
 * ****************************************************************************
 * Copyright 2014 Virginia Polytechnic Institute and State University
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ****************************************************************************
 */
package edu.vt.vbi.patric.portlets;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import edu.vt.vbi.ci.util.CommandResults;
import edu.vt.vbi.ci.util.ExecUtilities;
import edu.vt.vbi.patric.beans.Genome;
import edu.vt.vbi.patric.beans.Taxonomy;
import edu.vt.vbi.patric.common.DataApiHandler;
import edu.vt.vbi.patric.common.ExcelHelper;
import edu.vt.vbi.patric.common.SessionHandler;
import edu.vt.vbi.patric.common.SiteHelper;
import edu.vt.vbi.patric.dao.ResultType;
import edu.vt.vbi.patric.proteinfamily.FIGfamData;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.codehaus.jackson.map.ObjectWriter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.*;
import java.util.*;

public class FIGfam extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(FIGfam.class);

	private final boolean removeAfterClusterComputed = true;

	private ObjectReader jsonReader;

	private ObjectWriter jsonWriter;

	@Override
	public void init() throws PortletException {
		super.init();

		ObjectMapper objectMapper = new ObjectMapper();
		jsonReader = objectMapper.reader(Map.class);
		jsonWriter = objectMapper.writerWithType(Map.class);
	}

	public boolean isLoggedIn(PortletRequest request) {

		String sessionId = request.getPortletSession(true).getId();
		Gson gson = new Gson();
		LinkedTreeMap sessionMap = gson.fromJson(SessionHandler.getInstance().get(sessionId), LinkedTreeMap.class);

		return sessionMap.containsKey("authorizationToken");
	}

	public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		response.setContentType("text/html");

		String mode = request.getParameter("display_mode");

		SiteHelper.setHtmlMetaElements(request, response, "Protein Families");

		PortletRequestDispatcher prd;
		if ((mode != null) && (mode.equals("result"))) {

			String contextType = request.getParameter("context_type");
			if (contextType == null) {
				contextType = "";
			}
			String contextId = request.getParameter("context_id");
			if (contextId == null) {
				contextId = "";
			}

			Map<String, String> key = null;
			String pk = request.getParameter("param_key");
			String keyword = "";

			if (pk != null && !pk.isEmpty()) {
				key = jsonReader.readValue(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk));
			}

			if (key == null) {
				key = new HashMap<>();
				key.put("keyword", "");
				key.put("genera", "");
				key.put("genomeIds", "");
			}
			else {
				keyword = key.get("keyword");
			}

			String familyType = request.getParameter("family_type");
			if (familyType == null) familyType = "figfam"; // TODO: change to plfam later
			String genomeFilter = request.getParameter("genome_filter");
			if (genomeFilter == null) genomeFilter = "";

			request.setAttribute("contextType", contextType);
			request.setAttribute("contextId", contextId);
			request.setAttribute("pk", pk);
			request.setAttribute("keyword", keyword);
			request.setAttribute("key", key);
			request.setAttribute("familyType", familyType);
			request.setAttribute("genomeFilter", genomeFilter);

			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/proteinfamily_tab.jsp");
		}
		else if ((mode != null) && (mode.equals("treeSee"))) {
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/msa.jsp");
		}
		else {

			boolean isLoggedIn = isLoggedIn(request);

			String cType = request.getParameter("context_type");
			String cId = request.getParameter("context_id");
			String familyType = request.getParameter("family_type");
			if (familyType == null) familyType = "figfam";

			String taxonName = "";
			int taxonId = -1;

			if(cId == null || cId.equals("")) {
				taxonId = 131567;
				taxonName = "cellular organism";
			}
			else {
				DataApiHandler dataApi = new DataApiHandler(request);
				if(cType.equals("taxon")) {
					Taxonomy taxonomy = dataApi.getTaxonomy(Integer.parseInt(cId));
					taxonId = taxonomy.getId();
					taxonName = taxonomy.getTaxonName();

				} else if (cType.equals("genome")) {
					Genome genome = dataApi.getGenome(cId);
					taxonId = genome.getTaxonId();
					taxonName = genome.getGenomeName();
				}
			}

			request.setAttribute("isLoggedIn", isLoggedIn);
			request.setAttribute("taxonName", taxonName);
			request.setAttribute("taxonId", taxonId);
			request.setAttribute("familyType", familyType);

			// Protein Family Sorter Tool Landing Page
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/proteinfamily_tool.jsp");
		}
		prd.include(request, response);
	}

	private void getGenomeIds(ResourceRequest request, Map<String, String> key) throws IOException {
		String result = request.getParameter("genomeIds");

		DataApiHandler dataApi = new DataApiHandler(request);
		FIGfamData access = new FIGfamData(dataApi);
		if (result != null && !result.equals("")) {
			key.put("genomeIds", result);
		}
		else {
			String cType = request.getParameter("cType");
			if ((cType != null) && (cType.equals("taxon"))) {
				String cId = request.getParameter("cId");
				if (cId == null || cId.equals("")) {
					cId = "2";
				}
				result = access.getGenomeIdsForTaxon(request);
				Taxonomy taxonomy = dataApi.getTaxonomy(Integer.parseInt(cId));
				key.put("genera", taxonomy.getTaxonName());
				// key.put("genera", access.getTaxonName(cId));
			}
			key.put("genomeIds", result);
		}
	}

	private void getTaxonIds(ResourceRequest request, PrintWriter writer) throws IOException {
		String cId = request.getParameter("taxonId");
		if ((cId != null) && (0 < cId.length())) {
			DataApiHandler dataApi = new DataApiHandler(request);
			FIGfamData access = new FIGfamData(dataApi);
			writer.write(access.getGenomeIdsForTaxon(request));
		}
	}

	private void setKeyValues(String name, ResourceRequest request, Map key) {
		String result = request.getParameter(name);
		if (result == null) {
			result = "";
		}
		key.put(name, result);
	}

	private String getKeyValue(String name, Map key) {
		if (key != null && key.containsKey(name) && key.get(name) != null) {
			return (String) key.get(name);
		}
		else {
			return "";
		}
	}

	// async requests and responses are processed here
	public void serveResource(ResourceRequest request, ResourceResponse resp) throws PortletException, IOException {
		resp.setContentType("text/html");
		String callType = request.getParameter("callType");

		if (callType != null) {
			switch (callType) {
			case "toSorter": {
				Map<String, String> key = new HashMap<>();
				// Added by OralDALAY

				if (request.getParameter("keyword") != null && !request.getParameter("keyword").equals(""))
					key.put("keyword", request.getParameter("keyword"));

				if (request.getParameter("familyType") != null && !request.getParameter("familyType").equals(""))
					key.put("familyType", request.getParameter("familyType"));

				getGenomeIds(request, key);

				long pk = (new Random()).nextLong();
				SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, jsonWriter.writeValueAsString(key));

				PrintWriter writer = resp.getWriter();
				writer.write("" + pk);
				writer.close();
				break;
			}
			case "getGenomeDetails": {
				resp.setContentType("application/json");
				PrintWriter writer = resp.getWriter();
				FIGfamData access = new FIGfamData();
				access.getGenomeDetails(request, writer);
				writer.close();
				break;
			}
			case "getTaxonIds": {
				PrintWriter writer = resp.getWriter();
				getTaxonIds(request, writer);
				writer.close();
				break;
			}
			case "toAligner": {
				Map<String, String> key = new HashMap<>();
				setKeyValues("featureIds", request, key);
				setKeyValues("figfamId", request, key);
				setKeyValues("product", request, key);

				long pk = (new Random()).nextLong();
				SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, jsonWriter.writeValueAsString(key));

				PrintWriter writer = resp.getWriter();
				writer.write("" + pk);
				writer.close();
				break;
			}
			case "toDetails": {
				Map<String, String> key = new HashMap<>();
				setKeyValues("genomeIds", request, key);
				setKeyValues("figfamIds", request, key);

				long pk = (new Random()).nextLong();
				SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, jsonWriter.writeValueAsString(key));

				PrintWriter writer = resp.getWriter();
				writer.write("" + pk);
				writer.close();
				break;
			}
			case "getJsp":
				String jspName = request.getParameter("JSP_NAME");
				jspName = "/WEB-INF/jsp/" + jspName + ".jsp";
				resp.setContentType("text/html");
				PortletContext context = this.getPortletContext();
				PortletRequestDispatcher reqDispatcher = context.getRequestDispatcher(jspName);
				reqDispatcher.include(request, resp);
				break;
			case "getFeatureIds": {
				PrintWriter writer = resp.getWriter();
				FIGfamData access = new FIGfamData();
				access.getFeatureIds(request, writer, request.getParameter("keyword"));
				writer.close();
				break;
			}
			case "getGroupStats": {
				resp.setContentType("application/json");
				PrintWriter writer = resp.getWriter();
				FIGfamData access = new FIGfamData();
				access.getGroupStats(request, writer);
				writer.close();
				break;
			}
			case "getLocusTags": {
				PrintWriter writer = resp.getWriter();
				FIGfamData access = new FIGfamData();
				access.getLocusTags(request, writer);
				writer.close();
				break;
			}
			case "getSessionId": {
				PrintWriter writer = resp.getWriter();
				PortletSession session = request.getPortletSession(true);
				writer.write(session.getId());
				writer.close();
				break;
			}
			case "saveState": {
				String keyType = request.getParameter("keyType");
				// ResultType key = new ResultType();
				Map<String, String> key = new HashMap<>();
				setKeyValues("pageAt", request, key);
				setKeyValues("syntonyId", request, key);
				setKeyValues("regex", request, key);
				setKeyValues("filter", request, key);
				setKeyValues("perfectFamMatch", request, key);
				setKeyValues("minnumber_of_members", request, key);
				setKeyValues("maxnumber_of_members", request, key);
				setKeyValues("minnumber_of_species", request, key);
				setKeyValues("maxnumber_of_species", request, key);
				setKeyValues("ClusterRowOrder", request, key);
				setKeyValues("ClusterColumnOrder", request, key);
				setKeyValues("heatmapAxis", request, key);
				setKeyValues("colorScheme", request, key);
				setKeyValues("heatmapState", request, key);
				setKeyValues("steps", request, key);

				long pk = (new Random()).nextLong();
				SessionHandler.getInstance().set(SessionHandler.PREFIX + pk, jsonWriter.writeValueAsString(key));

				PrintWriter writer = resp.getWriter();
				writer.write("" + pk);
				writer.close();
				break;
			}
			case "getState": {
				PrintWriter writer = resp.getWriter();

				String keyType = request.getParameter("keyType");
				String pk = request.getParameter("random");
				if ((pk != null) && (keyType != null)) {
					Map<String, String> key = jsonReader.readValue(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk));

					writer.write(getKeyValue("pageAt", key));
					writer.write("\t" + getKeyValue("syntonyId", key));
					writer.write("\t" + getKeyValue("regex", key));
					writer.write("\t" + getKeyValue("filter", key));
					writer.write("\t" + getKeyValue("perfectFamMatch", key));
					writer.write("\t" + getKeyValue("minnumber_of_members", key));
					writer.write("\t" + getKeyValue("maxnumber_of_members", key));
					writer.write("\t" + getKeyValue("minnumber_of_species", key));
					writer.write("\t" + getKeyValue("maxnumber_of_species", key));
					writer.write("\t" + getKeyValue("steps", key));
					writer.write("\t" + getKeyValue("ClusterRowOrder", key));
					writer.write("\t" + getKeyValue("ClusterColumnOrder", key));
					writer.write("\t" + getKeyValue("heatmapAxis", key));
					writer.write("\t" + getKeyValue("colorScheme", key));
					writer.write("\t" + getKeyValue("heatmapState", key));
					writer.close();
				}
				break;
			}
			case "doClustering": {
				PrintWriter writer = resp.getWriter();
				String data = request.getParameter("data");
				String g = request.getParameter("g");
				String e = request.getParameter("e");
				String m = request.getParameter("m");
				String ge = request.getParameter("ge");
				String pk = request.getParameter("pk");
				String action = request.getParameter("action");

				String tmpDir = System.getProperty("java.io.tmpdir", "/tmp");
				String filename = tmpDir + "/tmp_" + pk + ".txt";
				String output_filename = tmpDir + "/cluster_tmp_" + pk;
				try {
					PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
					out.write(data);
					out.close();
				}
				catch (Exception es) {
					LOGGER.error(es.getMessage(), es);
				}

				if (action.equals("Run"))
					writer.write(doCLustering(filename, output_filename, g, e, m, ge).toString());

				writer.close();
				break;
			}
			case "getSyntonyOrder": {
				PrintWriter writer = resp.getWriter();
				FIGfamData access = new FIGfamData();
				JSONArray json = access.getSyntonyOrder(request);
				json.writeJSONString(writer);
				writer.close();
				break;
			}
			case "DetailsFromMain": {
				processDetailFromMain(request, resp);
				break;
			}
			case "GetMainTable": {
				processGetMainTable(request, resp);
				break;
			}
			default: {
				PrintWriter writer = resp.getWriter();
				writer.write(callType);
				writer.close();
				break;
			}
			}
		}
	}

	private void processDetailFromMain(ResourceRequest request, ResourceResponse response) throws IOException {
		final String familyType = request.getParameter("familyType");
		final String familyId = familyType + "_id";

		DataApiHandler dataApi = new DataApiHandler(request);
		FIGfamData figfamData = new FIGfamData(dataApi);

		String fileName = "ProteinFamilyFeatures";
		String fileFormat = request.getParameter("detailsType");

		List<String> tableHeader = new ArrayList<>();
		List<String> tableField = new ArrayList<>();

		JSONArray tableSource = figfamData.getDetails(request);

		tableHeader.addAll(Arrays.asList("Group Id", "Genome Name", "Accession", "PATRIC ID", "RefSeq Locus Tag", "Alt Locus Tag", "Start", "End", "Length(NT)", "Strand",
				"Length(AA)", "Gene Symbol", "Product Description"));
		tableField.addAll(Arrays.asList(familyId, "genome_name", "accession", "patric_id", "refseq_locus_tag", "alt_locus_tag", "start", "end", "na_length", "strand",
				"aa_length", "gene", "product"));

		ExcelHelper excel = new ExcelHelper("xssf", tableHeader, tableField, tableSource);
		excel.buildSpreadsheet();

		if (fileFormat.equalsIgnoreCase("xlsx")) {
			response.setContentType("application/octetstream");
			response.addProperty("Content-Disposition", "attachment; filename=\"" + fileName + "." + fileFormat + "\"");

			excel.writeSpreadsheettoBrowser(response.getPortletOutputStream());
		}
		else if (fileFormat.equalsIgnoreCase("txt")) {
			response.setContentType("application/octetstream");
			response.addProperty("Content-Disposition", "attachment; filename=\"" + fileName + "." + fileFormat + "\"");

			response.getWriter().write(excel.writeToTextFile());
		}
	}

	private void processGetMainTable(ResourceRequest request, ResourceResponse response) throws IOException {
		String fileName = request.getParameter("OrthoFileName");
		String fileType = request.getParameter("OrthoFileType");
		String data = request.getParameter("data");

		if (fileType.equals("xls") || fileType.equals("xlsx")) {

			List<String> tableHeader = new ArrayList<>();
			List<String> tableField = new ArrayList<>();
			List<ResultType> tableSource = new ArrayList<>();

			BufferedReader br = new BufferedReader(new StringReader(data));
			String line;
			boolean isHeader = true;
			while ((line = br.readLine()) != null) {
				String[] tabs = line.split("\t");

				if (isHeader) {
					isHeader = false;
					tableHeader.addAll(Arrays.asList(tabs));

					for (String tab : tabs) {
						tableField.add(tab.replaceAll(" ", "_").toLowerCase());
					}
				}
				else {
					ResultType row = new ResultType();
					for (int i = 0; i < tabs.length; i++ ) {
						row.put(tableField.get(i), tabs[i]);
					}
					tableSource.add(row);
				}
			}

			ExcelHelper excel = new ExcelHelper("xssf", tableHeader, tableField, tableSource);
			excel.buildSpreadsheet();

			response.setContentType("application/octetstream");
			response.addProperty("Content-Disposition", "attachment; filename=\"" + fileName + "." + fileType + "\"");

			excel.writeSpreadsheettoBrowser(response.getPortletOutputStream());
		}
		else if (fileType.equals("txt")) {
			response.setContentType("application/octetstream");
			response.addProperty("Content-Disposition", "attachment; filename=\"" + fileName + "." + fileType + "\"");

			response.getWriter().write(data);
		}
	}

	@SuppressWarnings("unchecked")
	public JSONObject doCLustering(String filename, String outputfilename, String g, String e, String m, String ge) throws IOException {

		JSONObject output = new JSONObject();

		String exec = "runMicroArrayClustering.sh " + filename + " " + outputfilename + " " + ((g.equals("1")) ? ge : "0") + " "
				+ ((e.equals("1")) ? ge : "0") + " " + m;

		LOGGER.debug("doClustering() {}", exec);

		CommandResults callClustering = ExecUtilities.exec(exec);

		if (callClustering.getStdout()[0].equals("done")) {

			BufferedReader in = new BufferedReader(new FileReader(outputfilename + ".cdt"));
			String strLine;
			int count = 0;
			JSONArray rows = new JSONArray();
			while ((strLine = in.readLine()) != null) {
				String[] tabs = strLine.split("\t");
				if (count == 0) {
					JSONArray columns = new JSONArray();
					for (int i = 4; i < tabs.length; i++) {
						columns.add(tabs[i].split("-")[0]);
					}
					output.put("columns", columns);
				}
				if (count >= 3) {
					rows.add(tabs[1].split("-")[0]);
				}
				count++;
			}
			in.close();
			output.put("rows", rows);
		}

		if (removeAfterClusterComputed) {
			exec = "rm " + filename + " " + outputfilename + "*";
			ExecUtilities.exec(exec);
		}

		return output;
	}
}
