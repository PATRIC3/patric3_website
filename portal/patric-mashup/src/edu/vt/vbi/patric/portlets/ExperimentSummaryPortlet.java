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
import edu.vt.vbi.patric.dao.DBPRC;
import edu.vt.vbi.patric.dao.DBSummary;
import edu.vt.vbi.patric.mashup.ArrayExpressInterface;
import edu.vt.vbi.patric.mashup.EutilInterface;
import edu.vt.vbi.patric.mashup.PRIDEInterface;
import edu.vt.vbi.patric.mashup.PSICQUICInterface;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class ExperimentSummaryPortlet extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExperimentSummaryPortlet.class);

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		response.setContentType("text/html");

		String contextId = request.getParameter("context_id");
		String contextType = request.getParameter("context_type");

		if (contextType != null && contextId != null) {

			request.setAttribute("contextType", contextType);
			request.setAttribute("contextId", contextId);

			PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/summary_experiment_tab_init.jsp");
			prd.include(request, response);
		}
		else {
			LOGGER.debug("Invalid Parameter - cType:{}, cId:{}", contextType, contextId);
			PrintWriter writer = response.getWriter();
			writer.write("<p>Invalid Parameter - missing context information</p>");
			writer.close();
		}
	}

	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
		response.setContentType("text/html");
		String contextType = request.getParameter("cType");
		String contextId = request.getParameter("cId");

		if (contextType != null) {

			int taxonId = -1;
			DataApiHandler dataApi = new DataApiHandler(request);

			String species_name = ExperimentDataPortlet.getSpeciesName(contextType, contextId);
			String psicquic_species_name = "";
			String pride_species_name = "";

			DBSummary conn_summary = new DBSummary();
			DBPRC conn_prc = new DBPRC();

			if (contextType.equals("taxon")) {

				taxonId = dataApi.getTaxonomy(Integer.parseInt(contextId)).getId();

				psicquic_species_name = "species:" + taxonId;
				pride_species_name = conn_summary.getPRIDESpecies("" + taxonId);

			}
			else if (contextType.equals("genome")) {

				Genome genome = dataApi.getGenome(contextId);

				psicquic_species_name = "species:" + genome.getTaxonId();
				pride_species_name = conn_summary.getPRIDESpecies("" + taxonId);
			}
			// Transcriptomics
			// GEO
			String strQueryTerm = "txid" + taxonId + "[Organism:exp]+NOT+gsm[ETYP]";
			EutilInterface eutil_api = new EutilInterface();
			Map<String, String> gds_taxon = null;
			try {
				gds_taxon = eutil_api.getCounts("gds", strQueryTerm, "");
			}
			catch (Exception ex) {
				LOGGER.error(ex.getMessage(), ex);
			}

			// ArrayExpress
			ArrayExpressInterface api = new ArrayExpressInterface();
			JSONObject arex_keyword = api.getResults(species_name, "");

			// Proteomics
			PRIDEInterface pride_api = new PRIDEInterface();
			JSONObject proteomics_result = pride_api.getResults(pride_species_name);

			// Structure
			strQueryTerm = "txid" + taxonId + "[Organism:exp]";
			Map<String, String> st = null;
			try {
				st = eutil_api.getCounts("structure", strQueryTerm, "");
			}
			catch (Exception ex) {
				LOGGER.error(ex.getMessage(), ex);
			}

			// Protein Protein Interaction
			PSICQUICInterface psicquic_api = new PSICQUICInterface();
			String result = psicquic_api.getCounts("intact", psicquic_species_name);
			int result_pi = conn_prc.getPRCCount("" + taxonId, "PI");

			// passing attributes
			request.setAttribute("cType", contextType);
			request.setAttribute("cId", contextId);
			request.setAttribute("errorMsg", "Data is not available temporarily");
			request.setAttribute("gds_taxon", gds_taxon); // Map<String, String>
			request.setAttribute("arex_keyword", arex_keyword); // JSONObject
			request.setAttribute("proteomics_result", proteomics_result); // JSONObject
			request.setAttribute("st", st); // Map<String, String>
			request.setAttribute("result", result); // String
			request.setAttribute("result_pi", result_pi); // result_pi
			request.setAttribute("species_name", species_name);

			PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/summary_experiment_tab.jsp");
			prd.include(request, response);
		}
		else {
			PrintWriter writer = response.getWriter();
			writer.write("<p>Invalid Parameter - missing context information</p>");
			writer.close();
		}
	}
}
