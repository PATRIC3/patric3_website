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

import edu.vt.vbi.patric.common.SiteHelper;

import javax.portlet.*;
import java.io.IOException;

public class GenomeListPortlet extends GenericPortlet {

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		response.setContentType("text/html");

		SiteHelper.setHtmlMetaElements(request, response, "Genome List");
		response.setTitle("Genome List");

		String contextType = request.getParameter("context_type");
		String contextId = request.getParameter("context_id");
		String algorithm = request.getParameter("data_source");
		String status = request.getParameter("display_mode");
		String kw = (request.getParameter("keyword") != null)?request.getParameter("keyword"):"";
		if(kw != null && (kw.startsWith("/") || kw.startsWith("#"))){
			kw = "";
		}
		String pk = request.getParameter("param_key");
		if (pk == null)
			pk = "";
		if (status == null)
			status = "";
		if (algorithm == null)
			algorithm = "";

		String keyword = "(*)";
		String genomeId = "NA";
		String taxonId = "";

		if (contextType.equals("taxon")) {
			genomeId = "";
			taxonId = contextId;
		}
		else if (contextType.equals("genome")) {
			genomeId = contextId;
		}

		request.setAttribute("contextType", contextType);
		request.setAttribute("contextId", contextId);
		request.setAttribute("taxonId", taxonId);
		request.setAttribute("genomeId", genomeId);

		request.setAttribute("status", status);
		request.setAttribute("algorithm", algorithm);
		request.setAttribute("keyword", keyword);
		request.setAttribute("pk", pk);
		request.setAttribute("kw", kw);

		PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/genome_list.jsp");
		prd.include(request, response);
	}
}
