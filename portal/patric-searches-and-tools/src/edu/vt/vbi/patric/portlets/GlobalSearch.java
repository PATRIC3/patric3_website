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

import com.google.gson.Gson;
import edu.vt.vbi.patric.common.SessionHandler;
import edu.vt.vbi.patric.dao.ResultType;

import javax.portlet.*;
import java.io.IOException;

public class GlobalSearch extends GenericPortlet {

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		response.setContentType("text/html");

		response.setTitle("PATRIC Search");

		String pk = request.getParameter("param_key");
		Gson gson = new Gson();

		ResultType key = gson.fromJson(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk), ResultType.class);
		String keyword = "";
		if (key != null && key.get("keyword") != null) {
			keyword = key.get("keyword");
		}

		request.setAttribute("pk", pk);
		request.setAttribute("keyword", keyword);

		PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/globalsearch.jsp");
		prd.include(request, response);

	}

}
