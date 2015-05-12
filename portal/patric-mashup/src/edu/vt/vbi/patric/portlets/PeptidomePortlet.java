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
import edu.vt.vbi.patric.common.DataApiHandler;
import edu.vt.vbi.patric.mashup.EutilInterface;
import org.json.simple.JSONObject;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;

public class PeptidomePortlet extends GenericPortlet {

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		response.setContentType("text/html");
		PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/peptidome_list.jsp");
		prd.include(request, response);
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
		String tId = null;
		String cType = request.getParameter("context_type");
		String cId = request.getParameter("context_id");

		if (cType.equals("taxon")) {
			tId = cId;
		}
		else if (cType.equals("genome")) {
			DataApiHandler dataApi = new DataApiHandler(request);
			Genome genome = dataApi.getGenome(cId);
			tId = "" + genome.getTaxonId();
		}

		String strQueryTerm = "txid" + tId + "[Organism:exp]";
		if (filter != null) {
			strQueryTerm = strQueryTerm + "+AND+" + filter + "[ETYP]";
		}

		EutilInterface eutil_api = new EutilInterface();

		JSONObject jsonResult = eutil_api.getResults("pepdome", strQueryTerm, "", "", start, limit);

		PrintWriter writer = response.getWriter();
		jsonResult.writeJSONString(writer);
		writer.close();
	}
}
