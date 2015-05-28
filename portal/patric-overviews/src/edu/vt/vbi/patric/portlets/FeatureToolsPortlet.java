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

import edu.vt.vbi.patric.beans.GenomeFeature;
import edu.vt.vbi.patric.common.DataApiHandler;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;

public class FeatureToolsPortlet extends GenericPortlet {

	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		response.setContentType("text/html");
		String cType = request.getParameter("context_type");
		String cId = request.getParameter("context_id");

		if (cType != null && cId != null && cType.equals("feature")) {

			DataApiHandler dataApi = new DataApiHandler(request);
			GenomeFeature feature = dataApi.getPATRICFeature(cId);

			if (feature != null) {
				String dispRefseqLocusTag = null, dispSeedId = null, dispProteinSequence = null;
				StringBuilder dispSequenceID = new StringBuilder();

				if (feature.getAnnotation().equals("PATRIC")) {

					dispRefseqLocusTag = feature.getRefseqLocusTag();
					dispSeedId = feature.getPatricId();

					if (feature.hasAltLocusTag()) {
						dispSequenceID.append(feature.getAltLocusTag());
					}
					if (feature.hasRefseqLocusTag()) {
						dispSequenceID.append("|").append(feature.getRefseqLocusTag());
					}
					if (feature.hasProduct()) {
						dispSequenceID.append(" ").append(feature.getProduct());
					}

				}
				else if (feature.getAnnotation().equals("RefSeq")) {

					dispRefseqLocusTag = feature.getAltLocusTag();
					dispSequenceID.append(feature.getAltLocusTag()).append(" ").append(feature.getProduct());
				}

				// getting Protein Sequence
				if (feature.getFeatureType().equals("CDS")) {
					dispProteinSequence = feature.getAaSequence();
				}

				request.setAttribute("feature", feature);
				request.setAttribute("dispRefseqLocusTag", dispRefseqLocusTag);
				request.setAttribute("dispSeedId", dispSeedId);
				request.setAttribute("dispSequenceID", dispSequenceID.toString());
				request.setAttribute("dispProteinSequence", dispProteinSequence);

				PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/overview/feature_tools.jsp");
				prd.include(request, response);
			}
			else {
				PrintWriter writer = response.getWriter();
				writer.write(" ");
				writer.close();
			}
		}
		else {
			PrintWriter writer = response.getWriter();
			writer.write("<p>Invalid Parameter - missing context information</p>");
			writer.close();
		}
	}
}
