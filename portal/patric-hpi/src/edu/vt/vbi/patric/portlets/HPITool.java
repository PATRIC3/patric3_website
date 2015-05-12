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
import edu.vt.vbi.patric.common.SiteHelper;

import javax.portlet.*;
import java.io.IOException;

public class HPITool extends GenericPortlet {

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		SiteHelper.setHtmlMetaElements(request, response, "Host-Pathogen Interaction Finder");
		response.setContentType("text/html");
		response.setTitle("Host-Pathogen Interactions");
		PortletRequestDispatcher prd;

		String mode = request.getParameter("display_mode");

		if (mode != null && mode.equals("tab")) {

			String contextType = request.getParameter("context_type");
			String contextId = request.getParameter("context_id");
			String taxon_id;
			String hpi_only = request.getParameter("hpi_only");

			String myUrl = "/patric/pig/viewer/index.html?";
			String hp_ppi_checked;
			String ppi_checked;

			if (hpi_only == null) {
				hpi_only = "false";
			}

			if (hpi_only.equals("true")) {
				hp_ppi_checked = "checked";
				ppi_checked = "";
			}
			else {
				hp_ppi_checked = "";
				ppi_checked = "checked";
			}

			switch (contextType) {
			case "genome":
				DataApiHandler dataApi = new DataApiHandler(request);
				Genome context = dataApi.getGenome(contextId);
				taxon_id = "" + context.getTaxonId();

				myUrl += "taxids=" + taxon_id + "&hpisOnly=" + hpi_only + "&btwnOnly=false&page=1&w=1100&h=800";
				break;
			case "taxon":
				taxon_id = contextId;
				myUrl += "taxids=" + taxon_id + "&hpisOnly=" + hpi_only + "&btwnOnly=false&page=1&w=1100&h=800";
				break;
			default:
				myUrl += "keywds=" + contextId + "&hpisOnly=" + hpi_only + "&btwnOnly=false&page=1&w=1100&h=800";
				break;
			}

			request.setAttribute("contextType", contextType);
			request.setAttribute("contextId", contextId);
			request.setAttribute("myUrl", myUrl);
			request.setAttribute("hp_ppi_checked", hp_ppi_checked);
			request.setAttribute("ppi_checked", ppi_checked);

			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/hpi_finder_tab.jsp");
		}
		else {
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/hpi_finder.jsp");
		}

		prd.include(request, response);
	}
}
