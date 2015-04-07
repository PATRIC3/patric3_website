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

import edu.vt.vbi.patric.beans.Genome;
import edu.vt.vbi.patric.beans.GenomeFeature;
import edu.vt.vbi.patric.common.SiteHelper;
import edu.vt.vbi.patric.common.SolrInterface;
import edu.vt.vbi.patric.mashup.EutilInterface;
import edu.vt.vbi.patric.mashup.PubMedHelper;
import org.json.simple.JSONObject;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PubMedPortlet extends GenericPortlet {

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		SiteHelper.setHtmlMetaElements(request, response, "Literature");
		response.setContentType("text/html");
		String cType = request.getParameter("context_type");
		String cId = request.getParameter("context_id");

		if (cType != null) {

			String scopeUrl = "";
			String qScope = request.getParameter("scope");
			if (qScope != null) {
				scopeUrl = "&amp;scope=" + qScope;
			}

			String dateUrl = "";
			String qDate = request.getParameter("time");
			if (qDate != null) {
				dateUrl = "&amp;time=" + qDate;
			}
			else {
				qDate = "a";
			}

			String kwUrl = "";
			String qKeyword = request.getParameter("keyword");

			if (qKeyword != null) {
				kwUrl = "&amp;kw=" + qKeyword;
			}
			else {
				qKeyword = "none";
			}

			String contextUrl = "cType=" + cType + "&amp;cId=" + cId;

			Map<String, List<String>> hashKeyword = PubMedHelper.getKeywordHash();

			SolrInterface solr = new SolrInterface();
			String genome_name = "";
			String feature_name = "";

			if (cType.equals("genome")) {
				if (qScope == null) {
					qScope = "g";
				}
				Genome genome = solr.getGenome(cId);
				genome_name = genome.getGenomeName();

			}
			else if (cType.equals("feature")) {
				if (qScope == null) {
					qScope = "f";
				}

				GenomeFeature feature = solr.getFeature(cId);
				if (feature != null) {
					genome_name = feature.getGenomeName();

					if (feature.hasProduct()) {
						feature_name = feature.getProduct();
					}
					else if (feature.hasAltLocusTag()) {
						feature_name = feature.getAltLocusTag();
					}
				}
			}

			request.setAttribute("scopeUrl", scopeUrl);
			request.setAttribute("qScope", qScope);
			request.setAttribute("dateUrl", dateUrl);
			request.setAttribute("qDate", qDate);
			request.setAttribute("kwUrl", kwUrl);
			request.setAttribute("qKeyword", qKeyword);
			request.setAttribute("contextUrl", contextUrl);

			request.setAttribute("hashKeyword", hashKeyword);
			request.setAttribute("genome_name", genome_name);
			request.setAttribute("feature_name", feature_name);

			PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/pubmed_list.jsp");
			prd.include(request, response);
		}
		else {
			PrintWriter writer = response.getWriter();
			writer.write("<p>Invalid Parameter - missing context information</p>");
			writer.close();
		}
	}

	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		response.setContentType("application/json");

		String qScope = request.getParameter("scope");
		String qDate = request.getParameter("date");
		String qKeyword = request.getParameter("keyword");

		int start = 0;
		int limit = 0;

		if (request.getParameter("start") != null) {
			start = Integer.parseInt(request.getParameter("start"));
		}
		if (request.getParameter("limit") != null) {
			limit = Integer.parseInt(request.getParameter("limit"));
		}
		String tId = null;
		String gId = null;
		String fId = null;

		String cType = request.getParameter("cType");
		String cId = request.getParameter("cId");
		JSONObject jsonResult;

		if (cType != null) {
			switch (cType) {
			case "taxon":
				tId = cId;
				break;
			case "genome":
				gId = cId;
				break;
			case "feature":
				fId = cId;
				break;
			}

			Map<String, String> key = new HashMap<>();
			key.put("scope", qScope);
			key.put("date", qDate);
			key.put("keyword", qKeyword);
			key.put("taxon_id", tId);
			key.put("genome_id", gId);
			key.put("feature_id", fId);
			key.put("context", cType);

			String strPubmedQuery = PubMedHelper.getPubmedQueryString(key);

			EutilInterface eutil_api = new EutilInterface();

			jsonResult = eutil_api.getResults("pubmed", strPubmedQuery, "&sort=pub+date", "&sort=pub+date&retmode=xml", start, limit);
		}
		else {
			jsonResult = new JSONObject();
		}

		PrintWriter writer = response.getWriter();
		jsonResult.writeJSONString(writer);
		writer.close();
	}
}
