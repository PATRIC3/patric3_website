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

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;

public class DataLanding extends GenericPortlet {

	protected final String ftpUrl = "ftp://ftp.patricbrc.org/patric2/patric3";

	private static final Logger LOGGER = LoggerFactory.getLogger(DataLanding.class);

	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		response.setContentType("text/html");
		String windowID = request.getWindowID();
		PortletRequestDispatcher prd;

		if (windowID.indexOf("Genomes") >= 1) {
			JSONObject jsonData = readJsonData(request, "/patric-common/data/genomes.json");
			request.setAttribute("jsonData", jsonData);
			request.setAttribute("ftpUrl", ftpUrl);

			response.setTitle("Genomes");
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/data_landing/Genomes.jsp");
			prd.include(request, response);
		}
		else if (windowID.indexOf("GenomicFeatures") >= 1) {
			JSONObject jsonData = readJsonData(request, "/patric-common/data/features.json");
			request.setAttribute("jsonData", jsonData);
			request.setAttribute("ftpUrl", ftpUrl);

			response.setTitle("Features");
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/data_landing/GenomicFeatures.jsp");
			prd.include(request, response);
		}
		else if (windowID.indexOf("SpecialtyGenes") >= 1) {
			JSONObject jsonData = readJsonData(request, "/patric-common/data/specialtygenes.json");
			request.setAttribute("jsonData", jsonData);

			response.setTitle("Specialty Genes");
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/data_landing/SpecialtyGenes.jsp");
			prd.include(request, response);
		}
		else if (windowID.indexOf("AntibioticResistance") >= 1) {
			JSONObject jsonData = readJsonData(request, "/patric-common/data/antibioticresistance.json");
			request.setAttribute("jsonData", jsonData);

			response.setTitle("Antibiotic Resistance");
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/data_landing/AntibioticResistance.jsp");
			prd.include(request, response);
		}
		else if (windowID.indexOf("ProteinFamilies") >= 1) {
			JSONObject jsonData = readJsonData(request, "/patric-common/data/proteinfamilies.json");
			request.setAttribute("jsonData", jsonData);
			request.setAttribute("ftpUrl", ftpUrl);

			response.setTitle("Protein Families");
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/data_landing/ProteinFamilies.jsp");
			prd.include(request, response);
		}
		else if (windowID.indexOf("Transcriptomics") >= 1) {
			JSONObject jsonData = readJsonData(request, "/patric-common/data/transcriptomics.json");
			request.setAttribute("jsonData", jsonData);

			response.setTitle("Transcriptomics");
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/data_landing/Transcriptomics.jsp");
			prd.include(request, response);
		}
		else if (windowID.indexOf("Proteomics") >= 1) {
			JSONObject jsonData = readJsonData(request, "/patric-common/data/proteomics.json");
			request.setAttribute("jsonData", jsonData);

			response.setTitle("Proteomics");
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/data_landing/Proteomics.jsp");
			prd.include(request, response);
		}
		else if (windowID.indexOf("PPInteractions") >= 1) {
			JSONObject jsonData = readJsonData(request, "/patric-common/data/ppinteractions.json");
			request.setAttribute("jsonData", jsonData);

			response.setTitle("Protein Protein Interactions");
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/data_landing/PPInteractions.jsp");
			prd.include(request, response);
		}
		else if (windowID.indexOf("Pathways") >= 1) {
			JSONObject jsonData = readJsonData(request, "/patric-common/data/pathways.json");
			request.setAttribute("jsonData", jsonData);
			request.setAttribute("ftpUrl", ftpUrl);

			response.setTitle("Pathways");
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/data_landing/Pathways.jsp");
			prd.include(request, response);
		}
		else {
			PrintWriter writer = response.getWriter();
			writer.write(" ");
			writer.close();
		}
	}

	private JSONObject readJsonData(PortletRequest request, String fileUrl) {

//		String url = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + fileUrl;
		String url = "http://localhost" + fileUrl;
		LOGGER.debug("requesting.. {}", url);
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpGet httpRequest = new HttpGet(url);
		JSONObject jsonData = null;

		try {
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String strResponseBody = httpclient.execute(httpRequest, responseHandler);

			JSONParser parser = new JSONParser();
			jsonData = (JSONObject) parser.parse(strResponseBody);
		}
		catch (IOException | ParseException e) {
			LOGGER.error(e.getMessage(), e);
		}
		finally {
			httpclient.getConnectionManager().shutdown();
		}

		return jsonData;
	}
}
