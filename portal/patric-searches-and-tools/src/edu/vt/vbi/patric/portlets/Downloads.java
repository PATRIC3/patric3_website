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

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import edu.vt.vbi.patric.beans.Genome;
import edu.vt.vbi.patric.beans.Taxonomy;
import edu.vt.vbi.patric.common.*;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.util.*;

public class Downloads extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(Downloads.class);

	private ObjectReader jsonReader;

	@Override
	public void init() throws PortletException {
		super.init();

		ObjectMapper objectMapper = new ObjectMapper();
		jsonReader = objectMapper.reader(Map.class);
	}

	public static boolean isLoggedIn(PortletRequest request) {

		String sessionId = request.getPortletSession(true).getId();
		Gson gson = new Gson();
		LinkedTreeMap sessionMap = gson.fromJson(SessionHandler.getInstance().get(sessionId), LinkedTreeMap.class);

		return sessionMap.containsKey("authorizationToken");
	}

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		response.setContentType("text/html");
		response.setTitle("Download Tool");

		SiteHelper.setHtmlMetaElements(request, response, "Download Tool");

		boolean isLoggedIn = isLoggedIn(request);
		request.setAttribute("isLoggedIn", isLoggedIn);

		String contextType = request.getParameter("context_type");
		String contextId = request.getParameter("context_id");
		Taxonomy taxonomy = null;
		String organismName = null;

		if (contextId == null || contextId.equals("") || contextType == null || contextType.equals("")) {
			throw new PortletException("Important parameter (cId) is missing");
		}

		DataApiHandler dataApi = new DataApiHandler(request);

		if (contextType.equals("taxon")) {
			taxonomy = dataApi.getTaxonomy(Integer.parseInt(contextId));
			organismName = taxonomy.getTaxonName();
		}
		else if (contextType.equals("genome")) {
			Genome genome = dataApi.getGenome(contextId);
			taxonomy = dataApi.getTaxonomy(genome.getTaxonId());
			organismName = genome.getGenomeName();
		}

		assert taxonomy != null;
		request.setAttribute("taxonId", taxonomy.getId());
		request.setAttribute("organismName", organismName);

		PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/downloads.jsp");
		prd.include(request, response);

	}

	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		String mode = request.getParameter("mode");

		if (mode.equals("getGenomeCount")) {

			String taxonId = request.getParameter("taxonId");
			String dataSource = request.getParameter("data_source");

			DataApiHandler dataApi = new DataApiHandler(request);

			SolrQuery query = new SolrQuery();

			if (taxonId != null) {
				query.setQuery("taxon_lineage_ids:" + taxonId);
			}
			else {
				query.setQuery("*:*");
			}

			if (dataSource != null) {
				String[] sources = dataSource.split(",");
				List<String> conditions = new ArrayList<>();

				for (String source : sources) {
					switch (source) {
					case ".PATRIC":
						conditions.add("patric_cds:[1 TO *]");
						break;
					case ".RefSeq":
						conditions.add("refseq_cds:[1 TO *]");
						break;
					}
				}

				query.setFilterQueries(StringUtils.join(conditions, " OR "));
			}
			query.setRows(0);

			LOGGER.debug("[{}] {}", SolrCore.GENOME, query.toString());
			String apiResponse = dataApi.solrQuery(SolrCore.GENOME, query);

			Map resp = jsonReader.readValue(apiResponse);
			Map respBody = (Map) resp.get("response");

			int numFound = (Integer) respBody.get("numFound");

			response.getWriter().write("" + numFound);
		}
		else if (mode.equals("download")) {

			String genomeId = request.getParameter("genomeId");
			String taxonId = request.getParameter("taxonId");
			String fileTypes = request.getParameter("finalfiletype");
			String annotations = request.getParameter("finalalgorithm");

			Logger LOGGER = LoggerFactory.getLogger(CreateZip.class);

			List<String> genomeIdList = new LinkedList<>();

			DataApiHandler dataApi = new DataApiHandler(request);

			SolrQuery query = new SolrQuery("*:*");

			if (genomeId != null && !genomeId.equals("")) {
				query.addFilterQuery("genome_id:(" + genomeId.replaceAll(",", " OR ") + ")");
			}
			if (taxonId != null && !taxonId.equals("")) {
				query.addFilterQuery("taxon_lineage_ids:" + taxonId);
			}
			query.setRows(dataApi.MAX_ROWS).addField("genome_id");

			LOGGER.debug("[{}] {}", SolrCore.GENOME, query.toString());

			String apiResponse = dataApi.solrQuery(SolrCore.GENOME, query);
			Map resp = jsonReader.readValue(apiResponse);
			Map respBody = (Map) resp.get("response");

			List<Genome> genomeList = dataApi.bindDocuments((List<Map>) respBody.get("docs"), Genome.class);

			for (Genome genome : genomeList) {
				genomeIdList.add(genome.getId());
			}

			CreateZip zip = new CreateZip();
			byte[] bytes = zip.ZipIt(genomeIdList, Arrays.asList(annotations.split(",")), Arrays.asList(fileTypes.split(",")));

			if (bytes.length > 0) {
				response.setContentType("application/octetstream");
				response.setProperty("Cache-Control", "cache");
				response.setProperty("Content-Disposition", "attachment; filename=\"patric_downloads.zip\"");
				response.setContentLength(bytes.length);
				response.getPortletOutputStream().write(bytes);
			}
			else {
				response.getWriter().write("Sorry. The requested file(s) are not available.");
			}
		}
	}
}
