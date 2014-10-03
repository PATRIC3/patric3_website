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

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import edu.vt.vbi.patric.beans.Genome;
import edu.vt.vbi.patric.common.SiteHelper;
import edu.vt.vbi.patric.common.SolrCore;
import edu.vt.vbi.patric.common.SolrInterface;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SequenceSummaryPortlet extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(SequenceSummaryPortlet.class);

	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		response.setContentType("text/html");
		String cType = request.getParameter("context_type");
		String cId = request.getParameter("context_id");

		// TODO: implement redirection for p2_genome_id to new genome_id

		if (cType != null && cType.equals("genome")) {
			new SiteHelper().setHtmlMetaElements(request, response, "Genome Overview");
		}

		PortletRequestDispatcher prd;

		if (cType != null && cId != null) {
			prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/overview/sequence_summary_init.jsp");
			prd.include(request, response);
		}
		else {
			PrintWriter writer = response.getWriter();
			writer.write("<p>Invalid Parameter - missing context information</p>");
			writer.close();
		}
	}

	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		String contextType = request.getParameter("context_type");
		String contextId = request.getParameter("context_id");

		SolrInterface solr = new SolrInterface();

		if (contextType != null && contextId != null) {

			if (contextType.equals("genome")) {
				try {
					solr.setCurrentInstance(SolrCore.GENOME);

					SolrQuery query = new SolrQuery("genome_id:" + contextId);

					QueryResponse qr = solr.getServer().query(query);

					List<Genome> genomes = qr.getBeans(Genome.class);

					if (!genomes.isEmpty()) {
						Genome genome = genomes.get(0);

						genome.getGenomeLength(); genome.getChromosomes();

						request.setAttribute("genome", genome);

						response.setContentType("text/html");
						PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/overview/sequence_summary.jsp");
						prd.include(request, response);
					}

				} catch (MalformedURLException | SolrServerException e) {
					LOGGER.error(e.getMessage(), e);
				}
			}
			else {
				// taxon level

				List<String> annotations = Arrays.asList("PATRIC", "RefSeq", "BRC1");

				for (String annotation: annotations) {

					try {
						Map<String, Long> counts = new HashMap<>();

						// query to Solr
						solr.setCurrentInstance(SolrCore.GENOME);

						SolrQuery query = new SolrQuery();

						switch (annotation) {
						case "PATRIC":
							query.setQuery("patric_cds:[1 TO *]");
							break;
						case "RefSeq":
							query.setQuery("refseq_cds:[1 TO *]");
							break;
						case "BRC1":
							query.setQuery("brc1_cds:[1 TO *]");
							break;
						default:
							query.setQuery("*:*");
							break;
						}

						query.setFacet(true);
						query.addFacetField("genome_status");
						query.setRows(0);

						query.setFilterQueries("taxon_lineage_ids:" + contextId);

						QueryResponse qr = solr.getServer().query(query);

						FacetField facetField = qr.getFacetField("genome_status");

						for (FacetField.Count facetValue: facetField.getValues()) {
							counts.put(facetValue.getName(), facetValue.getCount());
						}
						counts.put("Total", qr.getResults().getNumFound());

						// save to req
						request.setAttribute(annotation, counts);
					} catch (MalformedURLException | SolrServerException e) {
						LOGGER.error(e.getMessage(), e);
					}
				}

				response.setContentType("text/html");
				PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/overview/sequence_summary.jsp");
				prd.include(request, response);
			}
		}
	}
}
