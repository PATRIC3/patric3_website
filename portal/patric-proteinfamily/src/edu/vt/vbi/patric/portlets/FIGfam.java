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
package edu.vt.vbi.patric.portlets;

import com.google.gson.Gson;
import edu.vt.vbi.ci.util.CommandResults;
import edu.vt.vbi.ci.util.ExecUtilities;
import edu.vt.vbi.patric.common.SiteHelper;
import edu.vt.vbi.patric.proteinfamily.FIGfamData;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class FIGfam extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(FIGfam.class);

	private final boolean removeAfterClusterComputed = true;

	public boolean isLoggedIn(PortletRequest request) {
		boolean isLoggedIn = false;

		PortletSession session = request.getPortletSession(true);

		if (session.getAttribute("authorizationToken", PortletSession.APPLICATION_SCOPE) != null) {
			isLoggedIn = true;
		}

		return isLoggedIn;
	}

	public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		response.setContentType("text/html");

		String mode = request.getParameter("display_mode");

		new SiteHelper().setHtmlMetaElements(request, response, "Protein Families");

		PortletRequestDispatcher prd;
		if ((mode != null) && (mode.equals("result"))) {
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/proteinfamily_tab.jsp");
		}
		else if ((mode != null) && (mode.equals("treeSee"))) {
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/msa.jsp");
		}
		else {

			boolean isLoggedIn = isLoggedIn(request);
			request.setAttribute("isLoggedIn", isLoggedIn);

			// Protein Family Sorter Tool Landing Page
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/proteinfamily_tool.jsp");
		}
		prd.include(request, response);
	}

	private void getGenomeIds(ResourceRequest req, Map<String, String> key) {
		String result = req.getParameter("genomeIds");
		FIGfamData access = new FIGfamData();
		if (result != null && !result.equals("")) {
			key.put("genomeIds", result);
		}
		else {
			String cType = req.getParameter("cType");
			if ((cType != null) && (cType.equals("taxon"))) {
				String cId = req.getParameter("cId");
				if (cId == null || cId.equals("")) {
					cId = "2";
				}
				result = access.getGenomeIdsForTaxon(cId);
				key.put("genera", access.getTaxonName(cId));
			}
			key.put("genomeIds", result);
		}
	}

	private void getTaxonIds(ResourceRequest req, PrintWriter writer) {
		String cId = req.getParameter("taxonId");
		if ((cId != null) && (0 < cId.length())) {
			FIGfamData access = new FIGfamData();
			writer.write(access.getGenomeIdsForTaxon(cId));
		}
	}

	private void setKeyValues(String name, ResourceRequest req, Map key) {
		String result = req.getParameter(name);
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
	public void serveResource(ResourceRequest req, ResourceResponse resp) throws PortletException, IOException {
		resp.setContentType("text/html");
		String callType = req.getParameter("callType");

		if (callType != null) {
			switch (callType) {
			case "toSorter": {
				Map<String, String> key = new HashMap<>();
				// Added by OralDALAY

				if (req.getParameter("keyword") != null && !req.getParameter("keyword").equals(""))
					key.put("keyword", req.getParameter("keyword"));

				getGenomeIds(req, key);
				Random g = new Random();
				int random = g.nextInt();
				Gson gson = new Gson();
				PortletSession session = req.getPortletSession(true);
				session.setAttribute("key" + random, gson.toJson(key, Map.class), PortletSession.APPLICATION_SCOPE);
				PrintWriter writer = resp.getWriter();
				writer.write("" + random);
				writer.close();
				break;
			}
			case "getGenomeDetails": {
				resp.setContentType("application/json");
				PrintWriter writer = resp.getWriter();
				FIGfamData access = new FIGfamData();
				access.getGenomeDetails(req, writer);
				writer.close();
				break;
			}
			case "getTaxonIds": {
				PrintWriter writer = resp.getWriter();
				getTaxonIds(req, writer);
				writer.close();
				break;
			}
			case "toAligner": {
				Map<String, String> key = new HashMap<>();
				setKeyValues("featureIds", req, key);
				setKeyValues("figfamId", req, key);
				setKeyValues("product", req, key);
				Random g = new Random();
				int random = g.nextInt();
				Gson gson = new Gson();
				PortletSession session = req.getPortletSession(true);
				session.setAttribute("key" + random, gson.toJson(key, Map.class), PortletSession.APPLICATION_SCOPE);
				PrintWriter writer = resp.getWriter();
				writer.write("" + random);
				writer.close();
				break;
			}
			case "toDetails": {
				Map<String, String> key = new HashMap<>();
				setKeyValues("genomeIds", req, key);
				setKeyValues("figfamIds", req, key);
				Random g = new Random();
				int random = g.nextInt();
				Gson gson = new Gson();
				PortletSession session = req.getPortletSession(true);
				session.setAttribute("key" + random, gson.toJson(key, Map.class), PortletSession.APPLICATION_SCOPE);
				PrintWriter writer = resp.getWriter();
				writer.write("" + random);
				writer.close();
				break;
			}
			case "getJsp":
				String jspName = req.getParameter("JSP_NAME");
				jspName = "/WEB-INF/jsp/" + jspName + ".jsp";
				resp.setContentType("text/html");
				PortletContext context = this.getPortletContext();
				PortletRequestDispatcher reqDispatcher = context.getRequestDispatcher(jspName);
				reqDispatcher.include(req, resp);
				break;
			case "getFeatureIds": {
				PrintWriter writer = resp.getWriter();
				FIGfamData access = new FIGfamData();
				access.getFeatureIds(req, writer, req.getParameter("keyword"));
				writer.close();
				break;
			}
			case "getGroupStats": {
				resp.setContentType("application/json");
				PrintWriter writer = resp.getWriter();
				FIGfamData access = new FIGfamData();
				access.getGroupStats(req, writer);
				writer.close();
				break;
			}
			case "getLocusTags": {
				PrintWriter writer = resp.getWriter();
				FIGfamData access = new FIGfamData();
				access.getLocusTags(req, writer);
				writer.close();
				break;
			}
			case "getSessionId": {
				PrintWriter writer = resp.getWriter();
				PortletSession session = req.getPortletSession(true);
				writer.write(session.getId());
				writer.close();
				break;
			}
			case "saveState": {
				String keyType = req.getParameter("keyType");
				// ResultType key = new ResultType();
				Map<String, String> key = new HashMap<>();
				setKeyValues("pageAt", req, key);
				setKeyValues("syntonyId", req, key);
				setKeyValues("regex", req, key);
				setKeyValues("filter", req, key);
				setKeyValues("perfectFamMatch", req, key);
				setKeyValues("minnumber_of_members", req, key);
				setKeyValues("maxnumber_of_members", req, key);
				setKeyValues("minnumber_of_species", req, key);
				setKeyValues("maxnumber_of_species", req, key);
				setKeyValues("ClusterRowOrder", req, key);
				setKeyValues("ClusterColumnOrder", req, key);
				setKeyValues("heatmapAxis", req, key);
				setKeyValues("colorScheme", req, key);
				setKeyValues("heatmapState", req, key);
				setKeyValues("steps", req, key);
				Random g = new Random();
				int random = 0;
				while (random == 0) {
					random = g.nextInt();
				}
				Gson gson = new Gson();
				PortletSession session = req.getPortletSession(true);
				session.setAttribute(keyType + random, gson.toJson(key, Map.class), PortletSession.APPLICATION_SCOPE);
				PrintWriter writer = resp.getWriter();
				writer.write("" + random);
				writer.close();
				break;
			}
			case "getState": {
				PrintWriter writer = resp.getWriter();
				PortletSession session = req.getPortletSession(true);
				Gson gson = new Gson();
				String keyType = req.getParameter("keyType");
				String random = req.getParameter("random");
				if ((random != null) && (keyType != null)) {
					Map<String, String> key = gson.fromJson((String) session.getAttribute(keyType + random, PortletSession.APPLICATION_SCOPE), Map.class);
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
				String data = req.getParameter("data");
				String g = req.getParameter("g");
				String e = req.getParameter("e");
				String m = req.getParameter("m");
				String ge = req.getParameter("ge");
				String pk = req.getParameter("pk");
				String action = req.getParameter("action");

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
				JSONArray json = access.getSyntonyOrder(req);
				json.writeJSONString(writer);
				writer.close();
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
