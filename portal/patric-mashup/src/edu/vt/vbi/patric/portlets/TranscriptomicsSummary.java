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
import edu.vt.vbi.patric.dao.DBPRC;
import edu.vt.vbi.patric.mashup.ArrayExpressInterface;
import edu.vt.vbi.patric.mashup.EutilInterface;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class TranscriptomicsSummary extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(TranscriptomicsSummary.class);

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		response.setContentType("text/html");
		String contextType = request.getParameter("context_type");

		if (contextType != null) {
			String contextId = request.getParameter("context_id");

			request.setAttribute("contextType", contextType);
			request.setAttribute("contextId", contextId);

			PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/summary_transcriptomics_init.jsp");
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

			int taxonId = -1;
			String contextId = request.getParameter("cId");
			String errorMsg = "Data is not available temporarily";

			DBPRC conn_prc = new DBPRC();

			DataApiHandler dataApi = new DataApiHandler(request);
			String speciesName = "";

			if (contextType.equals("taxon")) {
				Taxonomy taxonomy = dataApi.getTaxonomy(Integer.parseInt(contextId));
				speciesName = taxonomy.getTaxonName();
				taxonId = taxonomy.getId();
			}
			else if (contextType.equals("genome")) {
				Genome genome = dataApi.getGenome(contextId);
				speciesName = genome.getGenomeName();
				taxonId = genome.getTaxonId();
			}

			// GEO
			String strQueryTerm = "txid" + taxonId + "[Organism:exp]+NOT+gsm[ETYP]";
			EutilInterface eutil_api = new EutilInterface();

			Map<String, String> gds_taxon = null;
			Map<String, String> gds_keyword = null;
			try {
				gds_taxon = eutil_api.getCounts("gds", strQueryTerm, "");
				gds_keyword = eutil_api.getCounts("gds", speciesName.replaceAll(" ", "+") + "+NOT+gsm[ETYP]", "");
			}
			catch (Exception ex) {
				LOGGER.error(ex.getMessage(), ex);
			}

			// ArrayExpress
			ArrayExpressInterface api = new ArrayExpressInterface();
			JSONObject arex_species = api.getResults("", speciesName);
			JSONObject arex_keyword = api.getResults(speciesName, "");

			// PRC
			int prc_ma = conn_prc.getPRCCount("" + taxonId, "MA");

			// pass attributes through request
			request.setAttribute("contextType", contextType);
			request.setAttribute("contextId", contextId);
			request.setAttribute("speciesName", speciesName);
			request.setAttribute("gds_taxon", gds_taxon); // Map
			request.setAttribute("prc_ma", prc_ma); // int
			request.setAttribute("gds_keyword", gds_keyword); // Map
			request.setAttribute("arex_keyword", arex_keyword); // JSONObject
			request.setAttribute("arex_species", arex_species); // JSONObject
			request.setAttribute("errorMsg", errorMsg);

			PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/summary_transcriptomics.jsp");
			prd.include(request, response);
		}
		else {
			PrintWriter writer = response.getWriter();
			writer.write("<p>Invalid Parameter - missing context information</p>");
			writer.close();
		}
	}
}