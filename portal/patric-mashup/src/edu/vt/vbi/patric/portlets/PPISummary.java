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
import edu.vt.vbi.patric.mashup.PSICQUICInterface;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;

public class PPISummary extends GenericPortlet {

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		response.setContentType("text/html");
		String contextType = request.getParameter("context_type");

		if (contextType != null) {
			String contextId = request.getParameter("context_id");

			request.setAttribute("contextType", contextType);
			request.setAttribute("contextId", contextId);

			PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/summary_ppi_init.jsp");
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

			String psicquicSpeciesName;
			int taxonId = -1;
			String speciesName = "";
			String errorMsg = "Data is not available temporarily";
			String contextId = request.getParameter("cId");

			DataApiHandler dataApi = new DataApiHandler(request);

			switch (contextType) {
			case "taxon":
				Taxonomy taxonomy = dataApi.getTaxonomy(Integer.parseInt(contextId));
				speciesName = taxonomy.getTaxonName();
				taxonId = taxonomy.getId();
				psicquicSpeciesName = "species:" + taxonId;
				break;
			case "genome":
				Genome genome = dataApi.getGenome(contextId);
				speciesName = genome.getGenomeName();
				taxonId = genome.getTaxonId();
				psicquicSpeciesName = "species:" + taxonId;
				break;
			default:
				psicquicSpeciesName = "";
				break;
			}

			if (!psicquicSpeciesName.equals("")) {
				PSICQUICInterface api = new PSICQUICInterface();
				String result = api.getCounts("intact", psicquicSpeciesName);
				int result_pi = 0; // conn_prc.getPRCCount("" + taxonId, "PI");

				// pass attributes through request
				request.setAttribute("contextType", contextType);
				request.setAttribute("contextId", contextId);
				request.setAttribute("speciesName", speciesName);
				request.setAttribute("result", result);
				request.setAttribute("errorMsg", errorMsg);
			}

			PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/summary_ppi.jsp");
			prd.include(request, response);
		}
		else {
			PrintWriter writer = response.getWriter();
			writer.write("<p>Invalid Parameter - missing context information</p>");
			writer.close();
		}
	}
}
