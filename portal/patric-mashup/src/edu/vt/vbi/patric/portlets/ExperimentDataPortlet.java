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

import edu.vt.vbi.patric.common.SiteHelper;
import edu.vt.vbi.patric.common.SolrInterface;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;

public class ExperimentDataPortlet extends GenericPortlet {

	public static String getSpeciesName(String contextType, String contextId) {

		SolrInterface solr = new SolrInterface();
		String speciesName = "";

		if (contextType.equals("taxon")) {
			speciesName = solr.getTaxonomy(Integer.parseInt(contextId)).getTaxonName();
		}
		else if (contextType.equals("genome")) {
			speciesName = solr.getGenome(contextId).getGenomeName();
		}

		return speciesName;
	}

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		new SiteHelper().setHtmlMetaElements(request, response, "Experiment Data");

		response.setContentType("text/html");
		String contextType = request.getParameter("context_type");

		if (contextType != null) {

			String contextId = request.getParameter("context_id");
			String speciesName = getSpeciesName(contextType, contextId);

			request.setAttribute("speciesName", speciesName);

			PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/summary_experimentdata.jsp");
			prd.include(request, response);
		}
		else {
			PrintWriter writer = response.getWriter();
			writer.write("<p>Invalid Parameter - missing context information</p>");
			writer.close();
		}
	}
}
