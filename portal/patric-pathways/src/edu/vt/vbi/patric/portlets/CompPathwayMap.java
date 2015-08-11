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
import edu.vt.vbi.patric.common.*;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class CompPathwayMap extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(CompPathwayMap.class);

	private ObjectReader jsonReader;

	@Override
	public void init() throws PortletException {
		super.init();

		ObjectMapper objectMapper = new ObjectMapper();
		jsonReader = objectMapper.reader(Map.class);
	}

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		response.setContentType("text/html");

		SiteHelper.setHtmlMetaElements(request, response, "Comparative Pathway Map");

		DataApiHandler dataApi = new DataApiHandler(request);

		String pk = request.getParameter("param_key") != null ? request.getParameter("param_key") : "";
		String dm = request.getParameter("display_mode") != null ? request.getParameter("display_mode") : "";
		String cType = request.getParameter("context_type") != null ? request.getParameter("context_type") : "";
		String map = request.getParameter("map") != null ? request.getParameter("map") : "";
		String algorithm = request.getParameter("algorithm") != null ? request.getParameter("algorithm") : "";
		String cId = request.getParameter("context_id") != null ? request.getParameter("context_id") : "";
		String ec_number = request.getParameter("ec_number") != null ? request.getParameter("ec_number") : "";
		String feature_id = request.getParameter("feature_id") != null ? request.getParameter("feature_id") : "";
		String ec_names = "";
		int occurrences = 0;
		String genomeId = "";
		String taxonId = "";
		String pathway_name = "";
		String pathway_class = "";
		String definition = "";

		int patricGenomeCount = 0;
		int brc1GenomeCount = 0;
		int refseqGenomeCount = 0;

		// get attributes
		try {
			SolrQuery query = new SolrQuery("pathway_id:" + map);
			query.setFields("pathway_name,pathway_class,ec_description");

			String apiResponse = dataApi.solrQuery(SolrCore.PATHWAY_REF, query);

			Map resp = jsonReader.readValue(apiResponse);
			Map respBody = (Map) resp.get("response");

			List<Map> sdl = (List<Map>) respBody.get("docs");

			if (!sdl.isEmpty()) {
				Map doc = sdl.get(0);

				definition = doc.get("ec_description").toString();
				pathway_name = doc.get("pathway_name").toString();
				pathway_class = doc.get("pathway_class").toString();
			}
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

		try {
			if (cType != null && cType.equals("taxon")) {
				taxonId = cId;
			}
			else if (cType != null && cType.equals("genome")) {
				genomeId = cId;
			}

			if (pk != null && !pk.isEmpty()) {
				Map<String, String> key = jsonReader.readValue(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk));

				if (key != null && key.containsKey("genomeId") && !key.get("genomeId").equals("")) {
					genomeId = key.get("genomeId");
				}
				else if (key != null && key.containsKey("taxonId") && !key.get("taxonId").equals("")) {
					taxonId = key.get("taxonId");
				}
				if (key != null && key.containsKey("feature_id") && !key.get("feature_id").equals("")) {
					feature_id = key.get("feature_id");
				}
			}

			SolrQuery query = new SolrQuery("*:*");
			query.setRows(dataApi.MAX_ROWS);
			if (!genomeId.equals("")) {
				if (genomeId.contains(",")) {
					query.addFilterQuery("genome_id:(" + StringUtils.join(genomeId.split(","), " OR ") + ")");
					query.setRows(genomeId.split(",").length);
				}
				else {
					query.addFilterQuery("genome_id:" + genomeId);
				}
			}
			if (!taxonId.equals("")) {
				query.addFilterQuery("taxon_lineage_ids:" + taxonId);
			}

			LOGGER.debug("counting features: {}", query.toString());

			String apiResponse = dataApi.solrQuery(SolrCore.GENOME, query);

			Map resp = jsonReader.readValue(apiResponse);
			Map respBody = (Map) resp.get("response");

			List<Genome> genomes = dataApi.bindDocuments((List<Map>) respBody.get("docs"), Genome.class);

			for (Genome genome : genomes) {
				if (genome.hasPatricCds()) {
					patricGenomeCount++;
				}
//				if (doc.get("brc1_cds") != null && !doc.get("brc1_cds").toString().equals("0")) {
//					brc1GenomeCount++;
//				}
				if (genome.hasRefseqCds()) {
					refseqGenomeCount++;
				}
			}
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

		// TODO: implement with solr
		if (dm != null && dm.equals("ec")) {

			SolrQuery query = new SolrQuery("ec_number:(" + ec_number + ") AND pathway_id:(" + map + ")");

			LOGGER.debug("[{}]:{}", SolrCore.PATHWAY_REF.getSolrCoreName(), query);

			String apiResponse = dataApi.solrQuery(SolrCore.PATHWAY_REF, query);

			Map resp = jsonReader.readValue(apiResponse);
			Map respBody = (Map) resp.get("response");

			List<Map> sdl = (List<Map>) respBody.get("docs");
			for (Map doc : sdl) {
				ec_names = (String) doc.get("ec_description");
				occurrences = (Integer) doc.get("occurrence");
			}
		}
		else if (dm != null && dm.equals("feature")) {

			// q=pathway_id:00051&fq={!join%20from=ec_number%20to=ec_number%20fromIndex=pathway}feature_id:PATRIC.235.14.JMSA01000002.CDS.537.665.fwd+AND+pathway_id:00051
			SolrQuery query = new SolrQuery("pathway_id:" + map);
			query.addFilterQuery(SolrCore.PATHWAY.getSolrCoreJoin("ec_number", "ec_number", "feature_id:(" + feature_id +") AND pathway_id:(" + map + ")"));

			LOGGER.debug("[{}]:{}", SolrCore.PATHWAY_REF.getSolrCoreName(), query);

			String apiResponse = dataApi.solrQuery(SolrCore.PATHWAY_REF, query);

			Map resp = jsonReader.readValue(apiResponse);
			Map respBody = (Map) resp.get("response");

			List<Map> sdl = (List<Map>) respBody.get("docs");
			for (Map doc : sdl) {
				ec_number = (String) doc.get("ec_number");
				ec_names = (String) doc.get("ec_description");
				occurrences = (Integer) doc.get("occurrence");
			}
		}

		/////
		request.setAttribute("cType", cType);
		request.setAttribute("cId", cId);
		request.setAttribute("taxonId", taxonId);
		request.setAttribute("genomeId", genomeId);
		request.setAttribute("algorithm", algorithm);
		request.setAttribute("map", map);
		request.setAttribute("dm", dm);
		request.setAttribute("pk", pk);
		request.setAttribute("feature_id", feature_id);

		request.setAttribute("ec_number", ec_number);
		request.setAttribute("ec_names", ec_names);
		request.setAttribute("occurrences", occurrences);

		request.setAttribute("taxongenomecount_patric", patricGenomeCount);
		request.setAttribute("taxongenomecount_brc1", brc1GenomeCount);
		request.setAttribute("taxongenomecount_refseq", refseqGenomeCount);

		request.setAttribute("definition", definition);
		request.setAttribute("pathway_name", pathway_name);
		request.setAttribute("pathway_class", pathway_class);

		PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/comp_pathway_map.jsp");
		prd.include(request, response);
	}

	@SuppressWarnings("unchecked")
	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		response.setContentType("application/json");

		String callType = request.getParameter("callType");

		switch (callType) {
		case "EcTable": {
			processEcTable(request, response);
			break;
		}
		case "KeggMap": {
			processKeggMap(request, response);
			break;
		}
		case "HeatMap": {
			processHeatMap(request, response);
			break;
		}
		}
	}

	private void processEcTable(ResourceRequest request, ResourceResponse response) throws IOException {
		String genomeId = request.getParameter("genomeId");
		String taxonId = request.getParameter("taxonId");
		String cType = request.getParameter("cType");
		String map = request.getParameter("map");
		String algorithm = request.getParameter("algorithm");
		String pk = request.getParameter("pk");

		Map<String, String> key = null;
		if (pk != null && !pk.isEmpty()) {
			key = jsonReader.readValue(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk));
		}

		int count_total = 0;
		JSONArray results = new JSONArray();
		try {
			Set<String> ecNumbers = new HashSet<>();

			DataApiHandler dataApi = new DataApiHandler(request);

			SolrQuery query = new SolrQuery("pathway_id:" + map + " AND annotation:" + algorithm);
			if (taxonId != null && !taxonId.equals("")) {
				query.addFilterQuery(SolrCore.GENOME
						.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + taxonId));
			}
			if (genomeId != null && !genomeId.equals("")) {
				query.addFilterQuery(
						SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id",
								"genome_id:(" + genomeId.replaceAll(",", " OR ") + ")"));
			}
			if (key != null && key.containsKey("genomeId") && !key.get("genomeId").equals("")) {
				query.addFilterQuery("genome_id:(" + key.get("genomeId").replaceAll(",", " OR ") + ")");
			}

			query.setRows(0).setFacet(true);
			query.add("json.facet",
					"{stat:{field:{field:ec_number,limit:-1,facet:{genome_count:\"unique(genome_id)\",gene_count:\"unique(feature_id)\"}}}}");

			String apiResponse = dataApi.solrQuery(SolrCore.PATHWAY, query);

			Map resp = jsonReader.readValue(apiResponse);
			Map respBody = (Map) resp.get("response");
			int numFound = (Integer) respBody.get("numFound");

			if (numFound > 0) {
				List<Map> buckets = (List<Map>) ((Map) ((Map) resp.get("facets")).get("stat")).get("buckets");

				Map<String, Map> mapStat = new HashMap<>();
				for (Map value : buckets) {
					if (Integer.parseInt(value.get("gene_count").toString()) > 0) {
						mapStat.put(value.get("val").toString(), value);
						ecNumbers.add(value.get("val").toString());
					}
				}

				if (!ecNumbers.isEmpty()) {
					query = new SolrQuery("pathway_id:" + map + " AND ec_number:(" + StringUtils.join(ecNumbers, " OR ") + ")");
					query.setRows(ecNumbers.size()).setFields("ec_number,ec_description,occurrence");
					query.addSort("ec_number", SolrQuery.ORDER.asc);

					apiResponse = dataApi.solrQuery(SolrCore.PATHWAY_REF, query);
					resp = jsonReader.readValue(apiResponse);
					Map respBdoby = (Map) resp.get("response");

					count_total = (Integer) respBdoby.get("numFound");
					List<Map> sdl = (List<Map>) respBdoby.get("docs");

					for (Map doc : sdl) {
						String ecNumber = doc.get("ec_number").toString();
						Map stat = mapStat.get(ecNumber);

						JSONObject item = new JSONObject();
						item.put("algorithm", algorithm);
						item.put("ec_name", doc.get("ec_description"));
						item.put("ec_number", ecNumber);
						item.put("occurrence", doc.get("occurrence"));
						item.put("gene_count", stat.get("gene_count"));
						item.put("genome_count", stat.get("genome_count"));

						results.add(item);
					}
				}
			}
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

		JSONObject jsonResult = new JSONObject();
		try {
			jsonResult.put("total", count_total);
			jsonResult.put("results", results);
		}
		catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
		}

		PrintWriter writer = response.getWriter();
		jsonResult.writeJSONString(writer);
		writer.close();
	}

	private void processKeggMap(ResourceRequest request, ResourceResponse response) throws IOException {

		Map<String, String> key = new HashMap<>();

		DataApiHandler dataApi = new DataApiHandler(request);
		JSONObject ret = new JSONObject();
		JSONObject val = new JSONObject();
		try {
			val = (JSONObject) (new JSONParser()).parse(request.getParameter("val").toString());
		}
		catch (ParseException e) {
			LOGGER.error(e.getMessage(), e);
		}

		String need = val.get("need").toString();
		String genomeId = null, taxonId = null, map = null, pk = null;
		if (val.containsKey("genomeId") && val.get("genomeId") != null && !val.get("genomeId").equals("")) {
			genomeId = val.get("genomeId").toString();
		}

		if (val.containsKey("taxonId") && val.get("taxonId") != null && !val.get("taxonId").equals("")) {
			taxonId = val.get("taxonId").toString();
		}

		if (val.containsKey("map") && val.get("map") != null && !val.get("map").equals("")) {
			map = val.get("map").toString();
		}

		if (val.containsKey("pk") && val.get("pk") != null && !val.get("pk").equals("")) {
			pk = val.get("pk").toString();
		}

		if (need.equals("all")) {
			if(genomeId != null) {
				key.put("genomeId", genomeId);
			}
			if(taxonId != null) {
				key.put("taxonId", taxonId);
			}
			if (map != null) {
				key.put("map", map);
			}

			Map<String, String> sessKey = null;
			if (pk != null && !pk.isEmpty()) {
				sessKey = jsonReader.readValue(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk));
				if (sessKey != null && sessKey.containsKey("genomeId") && !sessKey.get("genomeId").equals("")) {
					genomeId = sessKey.get("genomeId");
				}
			}

			List<String> annotations = Arrays.asList("PATRIC", "RefSeq");

			// getting coordinates
			try {
				JSONArray listCoordinates = new JSONArray();

				for (String annotation : annotations) {
					Set<String> ecNumbers = new HashSet<String>();

					// step1. genome_count, feature_count
					// pathway/select?q=pathway_id:00053+AND+annotation:PATRIC&fq={!join+from=genome_id+to=genome_id+fromIndex=genome}taxon_lineage_ids:83332+AND+genome_status:(complete+OR+wgs)
					// &rows=0&facet=true&json.facet={stat:{field:{field:ec_number,limit:-1,facet:{genome_count:"unique(genome_id)",gene_count:"unique(feature_id)"}}}}

					SolrQuery query = new SolrQuery("pathway_id:" + map + " AND annotation:" + annotation);
					if (taxonId != null) {
						query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + taxonId));
					}
					if (genomeId != null) {
						query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "genome_id:(" + genomeId.replaceAll(",", " OR ") + ")"));
					}
					query.setRows(0).setFacet(true);

					query.add("json.facet","{stat:{field:{field:ec_number,limit:-1,facet:{genome_count:\"unique(genome_id)\",gene_count:\"unique(feature_id)\"}}}}");

					LOGGER.debug("step 1. [{}] {}", SolrCore.PATHWAY.getSolrCoreName(), query);

					String apiResponse = dataApi.solrQuery(SolrCore.PATHWAY, query);

					Map resp = jsonReader.readValue(apiResponse);
					Map respBody = (Map) resp.get("response");
					int numFound = (Integer) respBody.get("numFound");

					if (numFound > 0) {
						List<Map> buckets = (List<Map>) ((Map) ((Map) resp.get("facets")).get("stat")).get("buckets");

						Map<String, Map> mapStat = new HashMap<>();
						for (Map value : buckets) {
							if (Integer.parseInt(value.get("count").toString()) > 0) {
								mapStat.put(value.get("val").toString(), value);
								ecNumbers.add(value.get("val").toString());
							}
						}

						// step2. coordinates, occurrence
						// pathway_ref/select?q=pathway_id:00010+AND+map_type:enzyme%+AND+ec_number:("1.2.1.3"+OR+"1.1.1.1")&fl=ec_number,ec_description,map_location,occurrence

						if (!ecNumbers.isEmpty()) {
							query = new SolrQuery("pathway_id:" + map + " AND map_type:enzyme AND ec_number:(" + StringUtils.join(ecNumbers, " OR ") + ")");
							query.setFields("ec_number,ec_description,map_location,occurrence");
							query.setRows(dataApi.MAX_ROWS);

							LOGGER.trace("genome_x_y: [{}] {}", SolrCore.PATHWAY_REF.getSolrCoreName(), query);

							apiResponse = dataApi.solrQuery(SolrCore.PATHWAY_REF, query);

							resp = jsonReader.readValue(apiResponse);
							respBody = (Map) resp.get("response");

							List<Map> sdl = (List<Map>) respBody.get("docs");

							for (Map doc : sdl) {
								String ecNumber = doc.get("ec_number").toString();
								Map stat = mapStat.get(ecNumber);

								if (!stat.get("gene_count").toString().equals("0")) {

									List<String> locations = (List<String>) doc.get("map_location");
									for (String location : locations) {

										JSONObject coordinate = new JSONObject();
										coordinate.put("algorithm", annotation);
										coordinate.put("description", doc.get("ec_description"));
										coordinate.put("ec_number", ecNumber);
										coordinate.put("genome_count", stat.get("genome_count"));

										String[] loc = location.split(",");
										coordinate.put("x", loc[0]);
										coordinate.put("y", loc[1]);

										listCoordinates.add(coordinate);
									}
								}
							}
						}
					}
				}

				ret.put("genome_x_y", listCoordinates);
			}
			catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}

			// get pathways
			try {

				SolrQuery query = new SolrQuery("annotation:(" + StringUtils.join(annotations, " OR ") + ")");
				if (taxonId != null) {
					query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + taxonId));
				}
				if (genomeId != null) {
					query.addFilterQuery("genome_id:(" + genomeId.replaceAll(",", " OR ") + ")");
				}
				query.setFields("pathway_id,pathway_name,annotation").setRows(dataApi.MAX_ROWS);

				LOGGER.trace("genome_pathway_x_y: [{}] {}", SolrCore.PATHWAY.getSolrCoreName(), query);

				String apiResponse = dataApi.solrQuery(SolrCore.PATHWAY, query);

				Map resp = jsonReader.readValue(apiResponse);
				Map respBody = (Map) resp.get("response");

				List<Map> sdl = (List<Map>) respBody.get("docs");

				JSONArray listEnzymes = new JSONArray();
				Set<String> hash = new HashSet<>();

				for (Map doc: sdl) {
					// TODO: need to improve this using solr
					String hashKey = doc.get("pathway_id").toString() + ":" + doc.get("annotation").toString();

					if (!hash.contains(hashKey)) {

						hash.add(hashKey);

						JSONObject enzyme = new JSONObject();
						enzyme.put("algorithm", doc.get("annotation"));
						enzyme.put("map_id", doc.get("pathway_id"));
						enzyme.put("map_name", doc.get("pathway_name"));

						listEnzymes.add(enzyme);
					}
				}

				ret.put("genome_pathway_x_y", listEnzymes);
			}
			catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}

			// map_ids_in_map
			try {
				SolrQuery query = new SolrQuery("pathway_id:" + map + " AND map_type:path");
				query.setFields("ec_number,ec_description,map_location").setRows(dataApi.MAX_ROWS);

				LOGGER.trace("map_ids_in_map: [{}] {}", SolrCore.PATHWAY_REF.getSolrCoreName(), query);

				String apiResponse = dataApi.solrQuery(SolrCore.PATHWAY_REF, query);

				Map resp = jsonReader.readValue(apiResponse);
				Map respBody = (Map) resp.get("response");

				List<Map> sdl = (List<Map>) respBody.get("docs");

				JSONArray listCoordinates = new JSONArray();
				for (Map doc: sdl) {
					List<String> locations = (List<String>) doc.get("map_location");
					for (String location : locations) {

						JSONObject coordinate = new JSONObject();
						coordinate.put("source_id", doc.get("ec_number"));

						String[] loc = location.split(",");
						coordinate.put("x", loc[0]);
						coordinate.put("y", loc[1]);
						coordinate.put("width", loc[2]);
						coordinate.put("height", loc[3]);

						listCoordinates.add(coordinate);
					}
				}

				ret.put("map_ids_in_map", listCoordinates);
			}
			catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}

			// all coordinates
			try {
				SolrQuery query = new SolrQuery("pathway_id:" + map + " AND map_type:enzyme");
				query.setFields("ec_number,ec_description,map_location").setRows(dataApi.MAX_ROWS);

				LOGGER.trace("all_coordinates: [{}] {}", SolrCore.PATHWAY_REF.getSolrCoreName(), query);

				String apiResponse = dataApi.solrQuery(SolrCore.PATHWAY_REF, query);

				Map resp = jsonReader.readValue(apiResponse);
				Map respBody = (Map) resp.get("response");

				List<Map> sdl = (List<Map>) respBody.get("docs");

				JSONArray listCoordinates = new JSONArray();
				for (Map doc: sdl) {
					List<String> locations = (List<String>) doc.get("map_location");
					for (String location : locations) {

						JSONObject coordinate = new JSONObject();
						coordinate.put("ec", doc.get("ec_number"));
						coordinate.put("description", doc.get("ec_description"));

						String[] loc = location.split(",");
						coordinate.put("x", loc[0]);
						coordinate.put("y", loc[1]);

						listCoordinates.add(coordinate);
					}
				}

				ret.put("all_coordinates", listCoordinates);
			}
			catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}

		}
		else {
			// need: feature_id or ec_number
			JSONArray coordinates = new JSONArray();

			if (need.equals("ec_number")) {
				try {
					SolrQuery query = new SolrQuery("*:*");
					if (map != null) {
						query.addFilterQuery("pathway_id:(" + map + ")");
					}

					LOGGER.trace("[{}] {}", SolrCore.PATHWAY_REF.getSolrCoreName(), query);

					String apiResponse = dataApi.solrQuery(SolrCore.PATHWAY_REF, query);

					Map resp = jsonReader.readValue(apiResponse);
					Map respBody = (Map) resp.get("response");

					List<Map> sdl = (List<Map>) respBody.get("docs");

					for (Map doc : sdl) {
						List<String> locations = (List<String>) doc.get("map_location");

						for (String loc : locations) {
							JSONObject coordinate = new JSONObject();
							coordinate.put("ec_number", doc.get("ec_number"));
							String[] xy = loc.split(",");
							coordinate.put("x", xy[0]);
							coordinate.put("y", xy[1]);

							coordinates.add(coordinate);
						}
					}
				}
				catch (IOException e) {
					LOGGER.error(e.getMessage(), e);
				}
			}
			else {
				// feature
				String featureIds = null;
				if (val.containsKey("value") && val.get("value") != null && !val.get("value").equals("")) {
					featureIds = val.get("value").toString();
				}

				try {
					SolrQuery query = new SolrQuery("*:*");
					if (map != null) {
						query.addFilterQuery("pathway_id:(" + map + ")");
					}
					if (featureIds != null) {
						query.addFilterQuery(SolrCore.PATHWAY.getSolrCoreJoin("ec_number", "ec_number", "feature_id:(" + featureIds.replaceAll(",", " OR ") + ")"));
					}

					LOGGER.trace("coordinates: [{}] {}", SolrCore.PATHWAY_REF.getSolrCoreName(), query);

					String apiResponse = dataApi.solrQuery(SolrCore.PATHWAY_REF, query);

					Map resp = jsonReader.readValue(apiResponse);
					Map respBody = (Map) resp.get("response");

					List<Map> sdl = (List<Map>) respBody.get("docs");

					for (Map doc : sdl) {
						List<String> locations =  (List<String>) doc.get("map_location");

						for (String loc : locations) {
							JSONObject coordinate = new JSONObject();
							coordinate.put("ec_number", doc.get("ec_number"));
							String[] xy = loc.split(",");
							coordinate.put("x", xy[0]);
							coordinate.put("y", xy[1]);

							coordinates.add(coordinate);
						}
					}
				}
				catch (IOException e) {
					LOGGER.error(e.getMessage(), e);
				}
			}

			ret.put("coordinates", coordinates);
		}

		response.setContentType("application/json");
		ret.writeJSONString(response.getWriter());
	}

	private void processHeatMap(ResourceRequest request, ResourceResponse response) throws IOException {

		DataApiHandler dataApi = new DataApiHandler(request);

		String genomeId, taxonId, algorithm, map;

		genomeId = request.getParameter("genomeId");
		taxonId = request.getParameter("taxonId");
		algorithm = request.getParameter("algorithm");
		map = request.getParameter("map");

		JSONObject json = new JSONObject();

		try {

			SolrQuery query = new SolrQuery("pathway_id:" + map);

			if (algorithm != null && !algorithm.equals("")) {
				query.addFilterQuery("annotation:" + algorithm);
			}

			if (taxonId != null && !taxonId.equals("")) {
				query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + taxonId));
			}
			if (genomeId != null && !genomeId.equals("")) {
				query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "genome_id:(" + genomeId.replaceAll(",", " OR ") + ")"));
			}
			query.setRows(dataApi.MAX_ROWS).setFields("genome_id,annotation,ec_number,ec_description").setFacet(true);
			// {stat:{field:{field:genome_ec,limit:-1,facet:{gene_count:\"unique(feature_id)\"}}}}
			query.add("json.facet","{stat:{field:{field:genome_ec,limit:-1}}}}");

			LOGGER.debug("heatmap step 1: {}", query.toString());

			String apiResponse = dataApi.solrQuery(SolrCore.PATHWAY, query);

			Map resp = jsonReader.readValue(apiResponse);
			Map respBody = (Map) resp.get("response");
			int numFound = (Integer) respBody.get("numFound");

			if (numFound > 0) {
				List<Map> buckets = (List<Map>) ((Map) ((Map) resp.get("facets")).get("stat")).get("buckets");

				respBody = (Map) resp.get("response");
				List<Map> sdl = (List<Map>) respBody.get("docs");

				Map<String, Integer> mapStat = new HashMap<>();
				for (Map value : buckets) {
					if (Integer.parseInt(value.get("count").toString()) > 0) {
						mapStat.put(value.get("val").toString(), (Integer) value.get("count"));
					}
				}

				JSONArray items = new JSONArray();
				for (Map doc : sdl) {
					JSONObject item = new JSONObject();
					item.put("genome_id", doc.get("genome_id"));
					item.put("algorithm", doc.get("annotation"));
					item.put("ec_number", doc.get("ec_number"));
					item.put("ec_name", doc.get("ec_description"));
					Integer count = mapStat.get(doc.get("genome_id") + "_" + doc.get("ec_number"));
					item.put("gene_count", String.format("%02x", count)); // 2-digit hex string

					items.add(item);
				}

				json.put("data", items);
			}
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

		try {
			SolrQuery query = new SolrQuery("*:*");

			if (taxonId != null && !taxonId.equals("")) {
				query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + taxonId));
			}
			if (genomeId != null && !genomeId.equals("")) {
				query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "genome_id:(" + genomeId.replaceAll(",", " OR ") + ")"));
			}
			if (algorithm != null && !algorithm.equals("")) {
				switch (algorithm) {
				case "PATRIC":
					query.addFilterQuery("patric_cds:[1 TO *]");
					break;
				case "RefSeq":
					query.addFilterQuery("refseq_cds:[1 TO *]");
					break;
				case "BRC1":
					query.addFilterQuery("brc1_cds:[1 TO *]");
					break;
				}
			}
			query.setFields("genome_id,genome_name").setRows(dataApi.MAX_ROWS).addSort("genome_name", SolrQuery.ORDER.asc);

			LOGGER.debug("step 2: [{}] {}", SolrCore.GENOME.getSolrCoreName(), query.toString());

			String apiResponse = dataApi.solrQuery(SolrCore.GENOME, query);

			Map resp = jsonReader.readValue(apiResponse);
			Map respBody = (Map) resp.get("response");
			List<Genome> genomes = dataApi.bindDocuments((List<Map>) respBody.get("docs"), Genome.class);

			JSONArray items = new JSONArray();
			for (Genome genome: genomes) {
				JSONObject item = new JSONObject();
				item.put("genome_id", genome.getId());
				item.put("genome_name", genome.getGenomeName());

				items.add(item);
			}

			json.put("genomes", items);

		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

		response.setContentType("application/json");
		json.writeJSONString(response.getWriter());
	}
}