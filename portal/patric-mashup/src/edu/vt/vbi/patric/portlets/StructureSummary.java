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
import edu.vt.vbi.patric.common.SolrInterface;
import edu.vt.vbi.patric.mashup.EutilInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class StructureSummary extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(TranscriptomicsSummary.class);

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		response.setContentType("text/html");
		String contextType = request.getParameter("context_type");

		if (contextType != null) {
			String contextId = request.getParameter("context_id");

			request.setAttribute("contextType", contextType);
			request.setAttribute("contextId", contextId);

			PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/summary_structure_init.jsp");
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
			SolrInterface solr = new SolrInterface();
			int taxonId = -1;

			if (contextType.equals("taxon")) {
				Taxonomy taxonomy = solr.getTaxonomy(Integer.parseInt(contextId));
				speciesName = taxonomy.getTaxonName();
				taxonId = taxonomy.getId();
			}
			else if (contextType.equals("genome")) {
				Genome genome = solr.getGenome(contextId);
				speciesName = genome.getGenomeName();
				taxonId = genome.getTaxonId();
			}

			String strQueryTerm = "txid" + taxonId + "[Organism:exp]";
			EutilInterface eutil_api = new EutilInterface();

			Map<String, String> st = null;
			Map<String, String> st_ssgcid = null;
			Map<String, String> st_csgid = null;
			try {
				st = eutil_api.getCounts("structure", strQueryTerm, "");
				st_ssgcid = eutil_api.getCounts("structure", strQueryTerm + "%20AND%20\"ssgcid\"", "");
				st_csgid = eutil_api.getCounts("structure", strQueryTerm + "%20AND%20\"csgid\"", "");
			}
			catch (Exception ex) {
				LOGGER.error(ex.getMessage(), ex);
			}

			// pass attributes through request
			request.setAttribute("contextType", contextType);
			request.setAttribute("contextId", contextId);
			request.setAttribute("speciesName", speciesName);
			request.setAttribute("st", st); // Map
			request.setAttribute("st_ssgcid", st_ssgcid); // Map
			request.setAttribute("st_csgid", st_csgid); // Map
			request.setAttribute("errorMsg", errorMsg);

			PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/summary_structure.jsp");
			prd.include(request, response);
		}
		else {
			PrintWriter writer = response.getWriter();
			writer.write("<p>Invalid Parameter - missing context information</p>");
			writer.close();
		}
	}
}
