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

import edu.vt.vbi.patric.beans.Taxonomy;
import edu.vt.vbi.patric.common.DataApiHandler;
import edu.vt.vbi.patric.common.SiteHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaxonSummaryPortlet extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaxonSummaryPortlet.class);

	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		SiteHelper.setHtmlMetaElements(request, response, "Taxon Overview");

		response.setContentType("text/html");
		String cType = request.getParameter("context_type");
		String cId = request.getParameter("context_id");

		if (cType != null && cId != null && cType.equals("taxon")) {

			List<Map<String, Object>> lineage = null;

			DataApiHandler dataApi = new DataApiHandler(request);

			Taxonomy taxonomy = dataApi.getTaxonomy(Integer.parseInt(cId));

			if (taxonomy != null) {
				lineage = new ArrayList<>();

				List<Integer> txIds = taxonomy.getLineageIds();
				List<String> txNames = taxonomy.getLineageNames();
				List<String> txRanks = taxonomy.getLineageRanks();

				for (Integer taxonId : txIds) {
					int idx = txIds.indexOf(taxonId);
					Map<String, Object> taxon = new HashMap<>();
					taxon.put("taxonId", taxonId);
					taxon.put("name", txNames.get(idx));
					taxon.put("rank", txRanks.get(idx));

					lineage.add(taxon);
				}
			}

			request.setAttribute("lineage", lineage);

			PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/overview/taxon_summary.jsp");
			prd.include(request, response);
		}
		else {
			PrintWriter writer = response.getWriter();
			writer.write("<p>Invalid Parameter - missing context information</p>");
			writer.close();
		}
	}
}
