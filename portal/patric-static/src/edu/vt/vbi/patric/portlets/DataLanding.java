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

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;

public class DataLanding extends GenericPortlet {

	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		response.setContentType("text/html");
		String windowID = request.getWindowID();
		PortletRequestDispatcher prd;

		if (windowID.indexOf("Genomes") >= 1) {
			response.setTitle("Genomes");
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/data_landing/Genomes.jsp");
			prd.include(request, response);
		}
		else if (windowID.indexOf("GenomicFeatures") >= 1) {
			response.setTitle("Features");
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/data_landing/GenomicFeatures.jsp");
			prd.include(request, response);
		}
		else if (windowID.indexOf("SpecialtyGenes") >= 1) {
			response.setTitle("Features");
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/data_landing/SpecialtyGenes.jsp");
			prd.include(request, response);
		}
		else if (windowID.indexOf("AntibioticResistance") >= 1) {
			response.setTitle("Features");
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/data_landing/AntibioticResistance.jsp");
			prd.include(request, response);
		}
		else if (windowID.indexOf("ProteinFamilies") >= 1) {
			response.setTitle("Protein Families");
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/data_landing/ProteinFamilies.jsp");
			prd.include(request, response);
		}
		else if (windowID.indexOf("Transcriptomics") >= 1) {
			response.setTitle("Transcriptomics");
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/data_landing/Transcriptomics.jsp");
			prd.include(request, response);
		}
		else if (windowID.indexOf("Proteomics") >= 1) {
			response.setTitle("Proteomics");
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/data_landing/Proteomics.jsp");
			prd.include(request, response);
		}
		else if (windowID.indexOf("PPInteractions") >= 1) {
			response.setTitle("Protein Protein Interactions");
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/data_landing/PPInteractions.jsp");
			prd.include(request, response);
		}
		else if (windowID.indexOf("Pathways") >= 1) {
			response.setTitle("Protein Protein Interactions");
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/data_landing/Pathways.jsp");
			prd.include(request, response);
		}
		else {
			PrintWriter writer = response.getWriter();
			writer.write(" ");
			writer.close();
		}
	}
}
