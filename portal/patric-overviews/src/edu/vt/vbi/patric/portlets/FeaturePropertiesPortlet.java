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
import edu.vt.vbi.patric.common.SiteHelper;
import edu.vt.vbi.patric.common.SolrCore;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeaturePropertiesPortlet extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(FeaturePropertiesPortlet.class);

	private ObjectReader jsonReader;

	@Override
	public void init() throws PortletException {
		super.init();

		ObjectMapper objectMapper = new ObjectMapper();
		jsonReader = objectMapper.reader(Map.class);
	}

	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		SiteHelper.setHtmlMetaElements(request, response, "Feature Overview");

		response.setContentType("text/html");
		String cType = request.getParameter("context_type");
		String cId = request.getParameter("context_id");

		if (cType != null && cId != null && cType.equals("feature")) {

			DataApiHandler dataApi = new DataApiHandler(request);
			GenomeFeature feature = dataApi.getPATRICFeature(cId);

			if (feature != null) {
				List<GenomeFeature> listReleateFeatures = null;
				List<String> listUniprotkbAccessions = feature.getUniprotkbAccession();
				List<Map<String, String>> listUniprotIds = null;
				String refseqLink = null;
				String refseqLocusTag = null;
				Map<String, String> virulenceFactor = null;

				SolrQuery query = new SolrQuery("pos_group:" + feature.getPosGroupInQuote());
				query.setSort("annotation_sort", SolrQuery.ORDER.asc);

				String apiResponse = dataApi.solrQuery(SolrCore.FEATURE, query);

				Map resp = jsonReader.readValue(apiResponse);
				Map respBody = (Map) resp.get("response");

				listReleateFeatures = dataApi.bindDocuments((List<Map>) respBody.get("docs"), GenomeFeature.class);

				if (listUniprotkbAccessions != null) {
					query = new SolrQuery("uniprotkb_accession:(" + StringUtils.join(listUniprotkbAccessions, " OR ") + ")");
					query.setFields("uniprotkb_accession,id_type,id_value");
					query.setRows(1000);

					apiResponse = dataApi.solrQuery(SolrCore.ID_REF, query);

					resp = jsonReader.readValue(apiResponse);
					respBody = (Map) resp.get("response");

					List<Map> sdl = (List<Map>) respBody.get("docs");

					if (!sdl.isEmpty()) {
						listUniprotIds = new ArrayList<>();
					}

					for (Map doc : sdl) {
						Map<String, String> uniprot = new HashMap<>();

						uniprot.put("Accession", doc.get("uniprotkb_accession").toString());
						uniprot.put("idType", doc.get("id_type").toString());
						uniprot.put("idValue", doc.get("id_value").toString());

						listUniprotIds.add(uniprot);
					}
				}

				if (feature.getAnnotation().equals("PATRIC")) {

					if (feature.hasGeneId()) {
						refseqLink = SiteHelper.getExternalLinks("ncbi_gene").replace("&", "&amp;") + feature.getGeneId();
					}
					refseqLocusTag = feature.getRefseqLocusTag();
				}
				else if (feature.getAnnotation().equals("RefSeq")) {
					refseqLocusTag = feature.getAltLocusTag();
				}

				query = new SolrQuery("(locus_tag:" + feature.getAltLocusTag()
							+ (feature.hasRefseqLocusTag() ? " OR locus_tag: " + feature.getRefseqLocusTag() : "") + ")");
				query.setFilterQueries("source:PATRIC_VF");
				query.setFields("source,source_id");

				apiResponse = dataApi.solrQuery(SolrCore.SPECIALTY_GENE, query);

				resp = jsonReader.readValue(apiResponse);
				respBody = (Map) resp.get("response");

				List<Map> sdl = (List<Map>) respBody.get("docs");

				for (Map doc : sdl) {
					virulenceFactor = new HashMap<>();

					virulenceFactor.put("source", doc.get("source").toString());
					virulenceFactor.put("sourceId", doc.get("source_id").toString());
				}

				request.setAttribute("feature", feature);
				request.setAttribute("listReleateFeatures", listReleateFeatures);
				request.setAttribute("listUniprotkbAccessions", listUniprotkbAccessions);
				request.setAttribute("listUniprotIds", listUniprotIds);
				request.setAttribute("refseqLink", refseqLink);
				request.setAttribute("refseqLocusTag", refseqLocusTag);
				request.setAttribute("virulenceFactor", virulenceFactor);

				PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/overview/feature_properties.jsp");
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
