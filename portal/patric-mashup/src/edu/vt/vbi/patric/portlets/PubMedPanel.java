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

import edu.vt.vbi.patric.mashup.EutilInterface;
import edu.vt.vbi.patric.mashup.PubMedHelper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class PubMedPanel extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(PubMedPanel.class);

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		response.setContentType("text/html");

		String cId = request.getParameter("context_id");

		if (cId != null) {
			PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/pubmed_panel.jsp");
			prd.include(request, response);
		}
		else {
			PrintWriter writer = response.getWriter();
			writer.write("<p>Invalid Parameter - missing context information</p>");
			writer.close();
		}
	}

	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		String cType = request.getParameter("context_type");
		String cId = request.getParameter("context_id");
		String qKeyword = request.getParameter("keyword");
		String db = request.getParameter("db");

		String contextLink = "";
		Map<String, String> key = new HashMap<>();

		if (cType != null && cType.equals("taxon")) {
			key.put("taxon_id", cId);
			key.put("context", "taxon");
			contextLink = "cType=taxon&amp;cId=" + cId;
		}
		else if (cType != null && cType.equals("genome")) {
			key.put("genome_id", cId);
			key.put("context", "genome");
			contextLink = "cType=genome&amp;cId=" + cId;
		}
		else if (cType != null && cType.equals("feature")) {
			key.put("feature_id", cId);
			key.put("context", "feature");
			contextLink = "cType=feature&amp;cId=" + cId;
		}

		if (qKeyword != null) {
			key.put("keyword", qKeyword);
			contextLink += "&amp;kw=" + qKeyword;
		}
		else {
			contextLink += "&amp;time=a&amp;kw=";
		}

		StringBuilder sb = new StringBuilder();

		try {
			String strPubmedQuery = PubMedHelper.getPubmedQueryString(key);

			EutilInterface eutil_api = new EutilInterface();

			JSONObject jsonResult;
			if (db != null && db.equals("pmc")) {
				jsonResult = eutil_api.getResults("pmc", strPubmedQuery, "&sort=pub+date", "&sort=pub+date&retmode=xml", 0, 5);
			}
			else {
				jsonResult = eutil_api.getResults("pubmed", strPubmedQuery, "&sort=pub+date", "&sort=pub+date&retmode=xml", 0, 5);
			}

			JSONArray results = (JSONArray) jsonResult.get("results");

			sb.append("<ul class=\"no-decoration small\">");
			for (Object result : results) {
				JSONObject row = (JSONObject) result;

				sb.append("<li>");
				if (row.containsKey("PubDate")) {
					sb.append("<div>").append(row.get("PubDate")).append("</div>");
				}
				if (row.containsKey("pubmed_id") && !row.get("pubmed_id").equals("")) {
					sb.append("<div><a href=\"http://view.ncbi.nlm.nih.gov/pubmed/").append(row.get("pubmed_id")).append("\" target=\"_blank\">")
							.append(row.get("Title")).append("</a></div>");
				}
				else {
					sb.append("<div>").append(row.get("Title")).append("</div>");
				}
				sb.append("<div>").append(row.get("abbrAuthorList")).append("</div>");
				sb.append("<div>").append(row.get("Source")).append("</div>");
				sb.append("</li>");
			}

			if (results.size() == 0) {
				// sb.append("<div> No pubmed record is available.</div>");
				sb.append("<div class=\"far\"> No pubmed record is available.</div>");
				sb.append("<div> Please try ");
				sb.append(" <a href=\"http://www.ncbi.nlm.nih.gov/pmc/?term=").append(strPubmedQuery).append("\" target=_blank>PMC</a>");
				sb.append(" or <a href=\"http://scholar.google.com/scholar?q=").append(strPubmedQuery).append("\" target=_blank>Google Scholar</a>");
				sb.append("</div>");
			}
			else {
				sb.append("<div class=\"left\"><a class=\"double-arrow-link\" href=\"Literature?").append(contextLink).append("\">more</a></div>");
			}
			sb.append("</ul>");

		}
		catch (Exception ex) {
			LOGGER.debug(ex.getMessage(), ex);
			sb.append("<div> No pubmed record is available.</div>");
		}

		PrintWriter writer = response.getWriter();
		writer.write(sb.toString());
		writer.close();
	}
}
