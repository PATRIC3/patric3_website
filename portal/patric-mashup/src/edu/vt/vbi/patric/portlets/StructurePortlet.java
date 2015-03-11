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

import edu.vt.vbi.patric.common.SolrInterface;
import edu.vt.vbi.patric.mashup.EutilInterface;
import org.json.simple.JSONObject;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;

public class StructurePortlet extends GenericPortlet {

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		response.setContentType("text/html");
		String contextType = request.getParameter("context_type");

		if (contextType != null) {

			String contextId = request.getParameter("context_id");
			String filter = request.getParameter("filter");
			String organismName = ExperimentDataPortlet.getSpeciesName(contextType, contextId);

			request.setAttribute("contextType", contextType);
			request.setAttribute("contextId", contextId);
			request.setAttribute("filter", filter);
			request.setAttribute("organismName", organismName);

			PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/structure_list.jsp");
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
		String filter = request.getParameter("filter");
		int start = 0;
		int limit = 0;

		if (request.getParameter("start") != null) {
			start = Integer.parseInt(request.getParameter("start"));
		}
		if (request.getParameter("limit") != null) {
			limit = Integer.parseInt(request.getParameter("limit"));
		}
		SolrInterface solr = new SolrInterface();
		int taxonId = -1;
		String contextType = request.getParameter("cType");
		String contextId = request.getParameter("cId");
		if (contextType.equals("taxon")) {
			taxonId = Integer.parseInt(contextId);
		}
		else if (contextType.equals("genome")) {
			taxonId = solr.getGenome(contextId).getTaxonId();
		}

		String strQueryTerm = "txid" + taxonId + "[Organism:exp]";
		if (filter != null && filter.equalsIgnoreCase("ssgcid")) {
			strQueryTerm += "%20AND%20\"ssgcid\"";
		}
		else if (filter != null && filter.equalsIgnoreCase("csgid")) {
			strQueryTerm += "%20AND%20\"csgid\"";
		}

		EutilInterface eutil_api = new EutilInterface();

		JSONObject jsonResult = eutil_api.getResults("structure", strQueryTerm, "", "", start, limit);

		PrintWriter writer = response.getWriter();
		jsonResult.writeJSONString(writer);
		writer.close();
	}
}
