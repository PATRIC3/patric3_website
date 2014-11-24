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

import edu.vt.vbi.patric.common.OrganismTreeBuilder;
import edu.vt.vbi.patric.common.SiteHelper;
import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;

public class TaxonomyTreePortlet extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaxonomyTreePortlet.class);

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		response.setContentType("text/html");

		new SiteHelper().setHtmlMetaElements(request, response, "Taxonomy Tree");

		String cType = request.getParameter("context_type");
		String cId = request.getParameter("context_id");
		int taxonId = -1;
		if (cType != null && cType.equals("taxon") && cId!=null) {
			taxonId = Integer.parseInt(cId);
		}

		request.setAttribute("taxonId", taxonId);

		PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/taxonomy_tree.jsp");
		prd.include(request, response);
	}

	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
		int taxonId = Integer.parseInt(request.getParameter("taxonId"));
		String mode = request.getParameter("mode");

		JSONArray tree;

		switch (mode) {
		case "txtree":
			tree = OrganismTreeBuilder.buildGenomeTree(taxonId);
			break;
		case "azlist":
			tree = OrganismTreeBuilder.buildGenomeList(taxonId);
			break;
		case "tgm":
			tree = OrganismTreeBuilder.buildTaxonGenomeMapping(taxonId);
			break;
		default:
			tree = new JSONArray();
			break;
		}

		response.setContentType("application/json");
		tree.writeJSONString(response.getWriter());
	}
}
