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

import edu.vt.vbi.patric.mashup.KLEIOInterface;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;

public class KLEIOPortlet extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(KLEIOInterface.class);

	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		response.setContentType("text/html");
		PortletRequestDispatcher prd;

		String cType = request.getParameter("display_mode");

		if (cType != null && !cType.equals("")) {
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/kleio.jsp");
		}
		else {
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/kleio_iframe.jsp");
		}

		prd.include(request, response);
	}

	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		response.setContentType("application/json");
		String qKeyword;
		String type = request.getParameter("type");

		KLEIOInterface api = new KLEIOInterface();
		JSONObject temp;
		String jsonResult = "";

		try {
			if (type != null && type.equals("grid")) {

				qKeyword = request.getParameter("keyword");
				String start = request.getParameter("start");
				String end = request.getParameter("limit");

				temp = api.getDocumentList(qKeyword, null, false, Integer.parseInt(start), Integer.parseInt(end));

				jsonResult = temp.toString();
			}

			if (type != null && type.equals("tree")) {

				qKeyword = request.getParameter("keyword");
				temp = api.getFacets(qKeyword);
				jsonResult = temp.get("result").toString();
			}
		}
		catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
		}

		PrintWriter writer = response.getWriter();
		writer.write(jsonResult);
		writer.close();
	}
}
