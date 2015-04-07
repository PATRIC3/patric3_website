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
import edu.vt.vbi.patric.circos.Circos;
import edu.vt.vbi.patric.circos.CircosGenerator;
import edu.vt.vbi.patric.common.SiteHelper;
import edu.vt.vbi.patric.common.SolrInterface;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.portlet.PortletFileUpload;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CircosGenomeViewerPortlet extends GenericPortlet {

	CircosGenerator circosGenerator;

	private static final Logger LOGGER = LoggerFactory.getLogger(CircosGenomeViewerPortlet.class);

	@Override
	public void init(PortletConfig config) throws PortletException {
		String contextPath = config.getPortletContext().getRealPath(File.separator);
		circosGenerator = new CircosGenerator(contextPath);
		super.init(config);
	}

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		response.setContentType("text/html");

		SiteHelper.setHtmlMetaElements(request, response, "Circos Genome Viewer");
		response.setTitle("Circos Genome Viewer");

		SolrInterface solr = new SolrInterface();

		String actionUrl = "/portal/portal/patric/CircosGenomeViewer/CircosGenomeViewerWindow?action=1";
		String polyomicUrl = System.getProperty("polyomic.baseUrl", "http://polyomic.patricbrc.org:8888");
		String genomeId = request.getParameter("context_id");
		Genome genome = solr.getGenome(genomeId);

		request.setAttribute("actionUrl", actionUrl);
		request.setAttribute("polyomicUrl", polyomicUrl);
		request.setAttribute("genome", genome);

		PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/circos_html.jsp");
		prd.include(request, response);
	}

	@SuppressWarnings("unchecked")
	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		String imageId = request.getParameter("imageId");
		String trackList = request.getParameter("trackList");
		JSONObject res = new JSONObject();
		res.put("success", true);
		res.put("imageId", imageId);
		res.put("trackList", trackList);

		PrintWriter writer = response.getWriter();
		res.writeJSONString(writer);
		writer.close();
	}

	public void processAction(ActionRequest request, ActionResponse response) throws PortletException, IOException {

		Map<String, Object> parameters = new LinkedHashMap<>();
		int fileCount = 0;

		try {
			List<FileItem> items = new PortletFileUpload(new DiskFileItemFactory()).parseRequest(request);
			for (FileItem item : items) {
				if (item.isFormField()) {
					parameters.put(item.getFieldName(), item.getString());
				}
				else {
					if (item.getFieldName().matches("file_(\\d+)$")) {
						parameters.put("file_" + fileCount, item);
						fileCount++;
					}
				}
			}
		}
		catch (FileUploadException e) {
			LOGGER.error(e.getMessage(), e);
		}

		LOGGER.debug(parameters.toString());

		// Generate Circo Image
		Circos circosConf = circosGenerator.createCircosImage(parameters);

		if (circosConf != null) {
			String baseUrl = "https://" + request.getServerName();
			String redirectUrl = baseUrl + "/portal/portal/patric/CircosGenomeViewer/CircosGenomeViewerWindow?action=b&cacheability=PAGE&imageId="
					+ circosConf.getUuid() + "&trackList=" + StringUtils.join(circosConf.getTrackList(), ",");

			LOGGER.trace("redirect: {}", redirectUrl);
			response.sendRedirect(redirectUrl);
		}
	}
}
