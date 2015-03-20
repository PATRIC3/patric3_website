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
import com.google.gson.internal.LinkedTreeMap;
import edu.vt.vbi.patric.beans.Genome;
import edu.vt.vbi.patric.beans.Taxonomy;
import edu.vt.vbi.patric.common.*;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Downloads extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(Downloads.class);

	public boolean isLoggedIn(PortletRequest request) {

		String sessionId = request.getPortletSession(true).getId();
		Gson gson = new Gson();
		LinkedTreeMap sessionMap = gson.fromJson(SessionHandler.getInstance().get(sessionId), LinkedTreeMap.class);

		return sessionMap.containsKey("authorizationToken");
	}

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		response.setContentType("text/html");
		response.setTitle("Download Tool");

		new SiteHelper().setHtmlMetaElements(request, response, "Download Tool");

		boolean isLoggedIn = isLoggedIn(request);
		request.setAttribute("isLoggedIn", isLoggedIn);

		String contextType = request.getParameter("context_type");
		String contextId = request.getParameter("context_id");
		Taxonomy taxonomy = null;
		String organismName = null;

		if (contextId == null || contextId.equals("") || contextType == null || contextType.equals("")) {
			throw new PortletException("Important parameter (cId) is missing");
		}

		SolrInterface solr = new SolrInterface();

		if (contextType.equals("taxon")) {
			taxonomy = solr.getTaxonomy(Integer.parseInt(contextId));
			organismName = taxonomy.getTaxonName();
		}
		else if (contextType.equals("genome")) {
			Genome genome = solr.getGenome(contextId);
			taxonomy = solr.getTaxonomy(genome.getTaxonId());
			organismName = genome.getGenomeName();
		}

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

			try {
				SolrInterface solr = new SolrInterface();

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

				QueryResponse qr = solr.getSolrServer(SolrCore.GENOME).query(query);
				long numFound = qr.getResults().getNumFound();

				response.getWriter().write("" + numFound);

			}
			catch (MalformedURLException | SolrServerException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
		else if (mode.equals("download")) {

			String genomeId = request.getParameter("genomeId");
			String taxonId = request.getParameter("taxonId");
			String fileTypes = request.getParameter("finalfiletype");
			String annotations = request.getParameter("finalalgorithm");

			Logger LOGGER = LoggerFactory.getLogger(CreateZip.class);

			List<String> genomeIdList = new LinkedList<>();

			SolrInterface solr = new SolrInterface();
			try {
				SolrQuery query = new SolrQuery("*:*");

				if (genomeId != null && !genomeId.equals("")) {
					query.addFilterQuery("genome_id:(" + genomeId.replaceAll(",", " OR ") + ")");
				}
				if (taxonId != null && !taxonId.equals("")) {
					query.addFilterQuery("taxon_lineage_ids:" + taxonId);
				}
				query.setRows(10000).addField("genome_id");

				LOGGER.debug("{}", query.toString());
				QueryResponse qr = solr.getSolrServer(SolrCore.GENOME).query(query, SolrRequest.METHOD.POST);
				List<Genome> genomeList = qr.getBeans(Genome.class);

				for (Genome genome : genomeList) {
					genomeIdList.add(genome.getId());
				}
			}
			catch (MalformedURLException | SolrServerException e) {
				LOGGER.error(e.getMessage(), e);
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
