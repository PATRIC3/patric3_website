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
import edu.vt.vbi.patric.beans.Taxonomy;
import edu.vt.vbi.patric.common.DataApiHandler;
import org.json.simple.JSONObject;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;

public class ProteomicsSummary extends GenericPortlet {

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		response.setContentType("text/html");
		String contextType = request.getParameter("context_type");

		if (contextType != null) {
			String contextId = request.getParameter("context_id");

			request.setAttribute("contextType", contextType);
			request.setAttribute("contextId", contextId);

			PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/summary_proteomics_init.jsp");
			prd.include(request, response);
		}
		else {
			PrintWriter writer = response.getWriter();
			writer.write("<p>Invalid Parameter - missing context information</p>");
			writer.close();
		}
	}

	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
		response.setContentType("text/html");
		String contextType = request.getParameter("cType");

		if (contextType != null) {

			String contextId = request.getParameter("cId");
			String speciesName = "";
			String errorMsg = "Data is not available temporarily";

			DataApiHandler dataApi = new DataApiHandler(request);

			if (contextType.equals("taxon")) {
				Taxonomy taxonomy = dataApi.getTaxonomy(Integer.parseInt(contextId));
				speciesName = taxonomy.getTaxonName();
			}
			else if (contextType.equals("genome")) {
				Genome genome = dataApi.getGenome(contextId);
				speciesName = genome.getGenomeName();
			}

			//PRIDE
			JSONObject result = new JSONObject();
			result.put("hasData", false);

			//PRC
			int result_ms = 0;// conn_prc.getPRCCount("" + taxonId, "MS");

			// pass attributes through request
			request.setAttribute("contextType", contextType);
			request.setAttribute("contextId", contextId);
			request.setAttribute("result", result); // JSONObject
			request.setAttribute("result_ms", result_ms); // int
			request.setAttribute("errorMsg", errorMsg);

			PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/summary_proteomics.jsp");
			prd.include(request, response);
		}
		else {
			PrintWriter writer = response.getWriter();
			writer.write("<p>Invalid Parameter - missing context information</p>");
			writer.close();
		}
	}
}
