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
import edu.vt.vbi.patric.mashup.PDBInterface;
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
import java.util.List;
import java.util.Map;

public class JmolPortlet extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExperimentSummaryPortlet.class);

	ObjectReader jsonReader;

	@Override
	public void init() throws PortletException {
		super.init();

		ObjectMapper objectMapper = new ObjectMapper();
		jsonReader = objectMapper.reader(Map.class);
	}

	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		SiteHelper.setHtmlMetaElements(request, response, "3D Structure");

		response.setContentType("text/html");
		String pdbID = request.getParameter("pdb_id");

		if (pdbID != null) {

			String chainID = request.getParameter("chain_id");

			String _context_path = request.getContextPath();
			String _codebase = _context_path + "/jmol";
			String _datafile = "https://" + request.getServerName() + _context_path + "/jsp/readPDB.jsp?pdbID=" + pdbID;

			String urlNCBIStructure = "http://www.ncbi.nlm.nih.gov/sites/entrez?db=structure&cmd=DetailsSearch&term=";
			String urlPDB = "http://www.pdb.org/pdb/explore/explore.do?structureId=";
			String urlSSGCID = "http://www.ssgcid.org/";
			String urlCSGID = "http://www.csgid.org/";

			String nameSSGCID = "Seattle Structural Genomics Center for Infectious Disease";
			String nameCSGID = "Center for Structural Genomics of Infectious Diseases";

			PDBInterface api = new PDBInterface();
			Map<String, String> description = api.getDescription(pdbID);

			if (description != null) {

				DataApiHandler dataApi = new DataApiHandler(request);
				List<GenomeFeature> features = new ArrayList<>();
				List<String> targetIDs = new ArrayList<>();

				// 1. read associated features for given PDB ID
				// 1.1 read uniprotkb_accession
				List<String> uniprotKbAccessions = new ArrayList<>();
				SolrQuery query = new SolrQuery("id_type:PDB AND id_value:" + pdbID);

				LOGGER.trace("[{}] {}", SolrCore.ID_REF.getSolrCoreName(), query.toString());
				String apiResponse = dataApi.solrQuery(SolrCore.ID_REF, query);

				Map resp = jsonReader.readValue(apiResponse);
				Map respBody = (Map<String, Object>) resp.get("response");

				int numFound = (Integer) respBody.get("numFound");
				if (numFound > 0) {
					List<Map> records = (List<Map>) respBody.get("docs");
					for (Map item : records) {
						uniprotKbAccessions.add(item.get("uniprotkb_accession").toString());
					}
				}

				// 1.2 read features with uniprotkb_accession
				if (!uniprotKbAccessions.isEmpty()) {
					query = new SolrQuery("uniprotkb_accession:(" + StringUtils.join(uniprotKbAccessions, " OR ") + ")");

					LOGGER.trace("[{}] {}", SolrCore.FEATURE.getSolrCoreName(), query.toString());
					apiResponse = dataApi.solrQuery(SolrCore.FEATURE, query);
					resp = jsonReader.readValue(apiResponse);
					respBody = (Map<String, Object>) resp.get("response");

					features = dataApi.bindDocuments((List<Map>) respBody.get("docs"), GenomeFeature.class);
				}

				// 2. retrieve structural meta data
				if (!uniprotKbAccessions.isEmpty()) {
					List<String> ids = new ArrayList<>();
					for (String uniprotkbAccession : uniprotKbAccessions) {
						ids.add("\"UniProt:" + uniprotkbAccession + "\"");
					}

					query = new SolrQuery("gene_symbol_collection:(" + StringUtils.join(ids, " OR ") + ")");
					query.setRows(uniprotKbAccessions.size());

					LOGGER.trace("[{}] {}", SolrCore.STRUCTURE.getSolrCoreName(), query.toString());
					apiResponse = dataApi.solrQuery(SolrCore.STRUCTURE, query);
					resp = jsonReader.readValue(apiResponse);
					respBody = (Map<String, Object>) resp.get("response");

					List<Map> sdl = (List<Map>) respBody.get("docs");
					for (Map doc : sdl) {
						targetIDs.add(doc.get("target_id").toString());
					}
				}

				//
				request.setAttribute("pdbID", pdbID);
				request.setAttribute("chainID", chainID);
				request.setAttribute("_codebase", _codebase);
				request.setAttribute("_datafile", _datafile);
				request.setAttribute("urlNCBIStructure", urlNCBIStructure);
				request.setAttribute("urlPDB", urlPDB);
				request.setAttribute("urlSSGCID", urlSSGCID);
				request.setAttribute("urlCSGID", urlCSGID);
				request.setAttribute("nameSSGCID", nameSSGCID);
				request.setAttribute("nameCSGID", nameCSGID);
				request.setAttribute("description", description); // Map<String, String>
				request.setAttribute("features", features); // List<GenomeFeature>
				request.setAttribute("targetIDs", targetIDs); // List<String>

				PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/jmol.jsp");
				prd.include(request, response);
			}
			else {
				PrintWriter writer = response.getWriter();
				writer.write("No data available.");
				writer.close();
			}
		}
		else {
			PrintWriter writer = response.getWriter();
			writer.write("Invalid Parameter - missing context information");
			writer.close();
		}
	}
}
