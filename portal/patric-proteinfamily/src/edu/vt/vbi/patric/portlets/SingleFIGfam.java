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
import edu.vt.vbi.patric.common.SiteHelper;
import edu.vt.vbi.patric.common.SolrCore;
import edu.vt.vbi.patric.common.SolrInterface;
import edu.vt.vbi.patric.dao.ResultType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Random;

public class SingleFIGfam extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(SingleFIGfam.class);

	public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		new SiteHelper().setHtmlMetaElements(request, response, "Protein Family");
		response.setContentType("text/html");
		PortletContext context = this.getPortletContext();
		PortletRequestDispatcher reqDispatcher = context.getRequestDispatcher("/WEB-INF/jsp/single.jsp");
		reqDispatcher.include(request, response);
	}

	@SuppressWarnings("unchecked")
	public void serveResource(ResourceRequest req, ResourceResponse resp) throws PortletException, IOException {
		String callType = req.getParameter("callType");
		SolrInterface solr = new SolrInterface();
		Gson gson = new Gson();

		if (callType != null) {
			ResultType key = new ResultType();
			if (callType.equals("saveState")) {
				String gid = req.getParameter("gid");
				String figfam = req.getParameter("figfam");

				key.put("gid", gid);
				key.put("figfam", figfam);

				Random g = new Random();
				int random = g.nextInt();

				PortletSession session = req.getPortletSession(true);
				session.setAttribute("key" + random, gson.toJson(key, ResultType.class), PortletSession.APPLICATION_SCOPE);

				PrintWriter writer = resp.getWriter();
				writer.write("" + random);
				writer.close();

			}
			else if (callType.equals("getData")) {
				String keyword = req.getParameter("keyword");
				JSONObject jsonResult = new JSONObject();
				key.put("keyword", keyword);

				solr.setCurrentInstance(SolrCore.FEATURE);
				String start_id = req.getParameter("start");
				String limit = req.getParameter("limit");
				int start = Integer.parseInt(start_id);
				int end = Integer.parseInt(limit);

				// sorting
				HashMap<String, String> sort = null;
				if (req.getParameter("sort") != null) {
					// sorting
					JSONParser a = new JSONParser();
					JSONArray sorter;
					String sort_field = "";
					String sort_dir = "";
					try {
						sorter = (JSONArray) a.parse(req.getParameter("sort"));
						sort_field += ((JSONObject) sorter.get(0)).get("property").toString();
						sort_dir += ((JSONObject) sorter.get(0)).get("direction").toString();
						for (int i = 1; i < sorter.size(); i++) {
							sort_field += "," + ((JSONObject) sorter.get(i)).get("property").toString();
						}
					}
					catch (ParseException e) {
						LOGGER.debug(e.getMessage(), e);
					}

					sort = new HashMap<>();

					if (!sort_field.equals("") && !sort_dir.equals("")) {
						sort.put("field", sort_field);
						sort.put("direction", sort_dir);
					}
				}
				key.put("fields",
						"genome_id,genome_name,accession,seed_id,alt_locus_tag,refseq_locus_tag,gene,annotation,feature_type,feature_id,start,end,na_length,strand,protein_id,aa_length,product,figfam_id");

				JSONObject object = solr.getData(key, sort, null, start, end, false, false, false);

				JSONObject obj = (JSONObject) object.get("response");
				JSONArray obj1 = (JSONArray) obj.get("docs");

				jsonResult.put("results", obj1);
				jsonResult.put("total", obj.get("numFound"));

				resp.setContentType("application/json");
				PrintWriter writer = resp.getWriter();
				jsonResult.writeJSONString(writer);
				writer.close();
			}
		}
	}
}
