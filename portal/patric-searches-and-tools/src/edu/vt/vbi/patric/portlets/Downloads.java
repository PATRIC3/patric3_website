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

import edu.vt.vbi.patric.beans.Genome;
import edu.vt.vbi.patric.beans.Taxonomy;
import edu.vt.vbi.patric.common.SiteHelper;
import edu.vt.vbi.patric.common.SolrCore;
import edu.vt.vbi.patric.common.SolrInterface;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class Downloads extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(Downloads.class);

	public static boolean isLoggedIn(PortletRequest request) {
		boolean isLoggedIn = false;

		PortletSession session = request.getPortletSession(true);

		if (session.getAttribute("authorizationToken", PortletSession.APPLICATION_SCOPE) != null) {
			isLoggedIn = true;
		}

		return isLoggedIn;
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
	}
}
