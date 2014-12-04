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
import edu.vt.vbi.patric.beans.GenomeFeature;
import edu.vt.vbi.patric.beans.Taxonomy;
import edu.vt.vbi.patric.cache.ENewsGenerator;
import edu.vt.vbi.patric.common.OrganismTreeBuilder;
import edu.vt.vbi.patric.common.SolrInterface;
import edu.vt.vbi.patric.dao.*;
import org.hibernate.jmx.StatisticsService;
import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.portlet.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BreadCrumb extends GenericPortlet {

	private final boolean initCache = true;

	private static final Logger LOGGER = LoggerFactory.getLogger(BreadCrumb.class);

	@Override
	public void init() throws PortletException {
		super.init();

		String k = "PATRIC_DB.cfg.xml";
		HibernateHelper.buildSessionFactory(k, k);
		DBShared.setSessionFactory(HibernateHelper.getSessionFactory(k));
		DBSummary.setSessionFactory(HibernateHelper.getSessionFactory(k));
		DBSearch.setSessionFactory(HibernateHelper.getSessionFactory(k));
		DBPathways.setSessionFactory(HibernateHelper.getSessionFactory(k));
		DBDisease.setSessionFactory(HibernateHelper.getSessionFactory(k));
		DBPIG.setSessionFactory(HibernateHelper.getSessionFactory(k));
		DBPRC.setSessionFactory(HibernateHelper.getSessionFactory(k));
		DBTranscriptomics.setSessionFactory(HibernateHelper.getSessionFactory(k));
		try {
			List<MBeanServer> list = MBeanServerFactory.findMBeanServer(null);
			MBeanServer server = list.get(0);
			ObjectName on = new ObjectName("Hibernate:type=statistics,application=PATRIC2");
			StatisticsService mBean = new StatisticsService();
			mBean.setSessionFactory(DBShared.getSessionFactory());
			server.registerMBean(mBean, on);
		}
		catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}

		// create cache for Genome Selector (all bacteria level)
		if (initCache) {
			Map<String, String> key = new HashMap<>();
			key.put("ncbi_taxon_id", "2");

			try (BufferedWriter out = new BufferedWriter(new FileWriter(getPortletContext().getRealPath("txtree-bacteria.js")))) {
				JSONArray list = OrganismTreeBuilder.buildGenomeTree(131567);
				list.writeJSONString(out);
			}
			catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}

			try (BufferedWriter out = new BufferedWriter(new FileWriter(getPortletContext().getRealPath("azlist-bacteria.js")))) {
				JSONArray list = OrganismTreeBuilder.buildGenomeList(131567);
				list.writeJSONString(out);
			}
			catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}

			try (BufferedWriter out = new BufferedWriter(new FileWriter(getPortletContext().getRealPath("tgm-bacteria.js")))) {
				JSONArray list = OrganismTreeBuilder.buildTaxonGenomeMapping(131567);
				list.writeJSONString(out);
			}
			catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}

		// create cache for enews
		try {
			if (initCache) {
				ENewsGenerator cacheGen = new ENewsGenerator();
				if (cacheGen.createCacheFile(getPortletContext().getRealPath("/js/enews_data.js"))) {
					LOGGER.info("eNews cache is generated");
				}
				else {
					LOGGER.error("problem in generating eNews cache");
				}
			}
		}
		catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	public void destroy() {
		try {
			ArrayList<MBeanServer> list = MBeanServerFactory.findMBeanServer(null);
			MBeanServer server = list.get(0);
			ObjectName on = new ObjectName("Hibernate:type=statistics,application=PATRIC2");
			server.unregisterMBean(on);
		}
		catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		response.setContentType("text/html");

		String cType = request.getParameter("context_type");
		String cId = request.getParameter("context_id");
		String bm = request.getParameter("breadcrumb_mode");
		if (bm == null) {
			bm = "";
		}

		if (cId != null && cId.equals("")) {
			// if cId == "", redirect to cellular organism
			String origUrl = response.createRenderURL().toString();
			String newUrl = origUrl.replace("/BreadCrumbWindow", "").replace("&action=2", "").replace("context_id=", "cId=131567")
					.replace("context_type", "cType");
			LOGGER.debug("{} redirects to {}", origUrl, newUrl);

			response.getWriter().write("<meta http-equiv=\"refresh\" content=\"0;url=" + newUrl + "\">");
			response.getWriter().close();

		}
		else {

			SolrInterface solr = new SolrInterface();

			// for backward compatibility, check id format for genome & feature level request, and redirect url as needed.

			if (cType != null) {
				if (cType.equals("feature")) {
					try {
						LOGGER.debug("backward compatible check: feature_id: {}", cId);

						int p2_feature_id = Integer.parseInt(cId);
						GenomeFeature feature = solr.getPATRICFeatureByP2FeatureId(p2_feature_id);

						if (feature != null) {
							LOGGER.debug("compatible feature_id: {}", feature.getId());

							String origUrl = response.createRenderURL().toString();
							String newUrl = origUrl.replace("/BreadCrumbWindow", "").replace("&action=2", "")
									.replace("context_id=" + cId, "cId=" + feature.getId()).replace("context_type", "cType");
							LOGGER.debug("{} redirects to {}", origUrl, newUrl);

							response.getWriter().write("<meta http-equiv=\"refresh\" content=\"0;url=" + newUrl + "\">");
							response.getWriter().close();
						}
						return;
					}
					catch (NumberFormatException nfe) {
					}
				}
				else if (cType.equals("genome")) {
					try {
						LOGGER.debug("backward compatible check: genome_id: {}", cId);

						int p2_genome_id = Integer.parseInt(cId);
						Genome genome = solr.getGenomeByP2GenomeId(p2_genome_id);

						if (genome != null) {
							LOGGER.debug("compatible genome_id: {}", genome.getId());

							String origUrl = response.createRenderURL().toString();
							String newUrl = origUrl.replace("/BreadCrumbWindow", "").replace("&action=2", "")
									.replace("context_id=" + cId, "cId=" + genome.getId()).replace("context_type", "cType");
							LOGGER.debug("{} redirects to {}", origUrl, newUrl);

							response.getWriter().write("<meta http-equiv=\"refresh\" content=\"0;url=" + newUrl + "\">");
							response.getWriter().close();
						}
						return;
					}
					catch (NumberFormatException nfe) {
					}
				}
			}

			String windowID = request.getWindowID();

			if (windowID.indexOf("ECSearch") >= 1 || windowID.indexOf("GOSearch") >= 1 || windowID.indexOf("GenomicFeature") >= 1
					|| windowID.indexOf("GenomeFinder") >= 1 || windowID.indexOf("PathwayFinder") >= 1 || windowID.indexOf("Downloads") >= 1
					|| windowID.indexOf("AntibioticResistanceGeneSearch") >= 1 || windowID.indexOf("SpecialtyGeneSearch") >= 1
					|| windowID.indexOf("IDMapping") >= 1 || (windowID.indexOf("HPITool") >= 1 && bm.equals("tool"))
					|| (windowID.indexOf("FIGfamSorter") >= 1 && windowID.indexOf("FIGfamSorterB") < 1)
					|| (windowID.indexOf("FIGfamViewer") >= 1 && windowID.indexOf("FIGfamViewerB") < 1)
					|| (windowID.indexOf("FIGfam") >= 1 && bm.equals("tool")) || (windowID.indexOf("SingleFIGfam") >= 1 && bm.equals("tool"))
					|| windowID.indexOf("ExperimentData") >= 1 || windowID.indexOf("GEO") >= 1 || windowID.indexOf("ArrayExpress") >= 1
					|| windowID.indexOf("PRC") >= 1 || windowID.indexOf("PRIDE") >= 1 || windowID.indexOf("Structure") >= 1
					|| windowID.indexOf("IntAct") >= 1 || windowID.indexOf("RAST") >= 1 || windowID.indexOf("MGRAST") >= 1
					|| windowID.indexOf("TranscriptomicsEnrichment") >= 1) {

				request.setAttribute("WindowID", windowID);

				List<Map<String, Object>> lineage = new ArrayList<>();
				Taxonomy taxonomy = null;
				if (cType.equals("taxon")) {
					taxonomy = solr.getTaxonomy(Integer.parseInt(cId));
				}
				else if (cType.equals("genome")) {
					Genome genome = solr.getGenome(cId);
					taxonomy = solr.getTaxonomy(genome.getTaxonId());
				}
				List<Integer> taxonIds = taxonomy.getLineageIds();
				List<String> txNames = taxonomy.getLineageNames();
				List<String> txRanks = taxonomy.getLineageRanks();

				if (taxonIds == null) { // if taxonId = 131567 (cellular organism)
					taxonIds = new ArrayList<>();
				}

				for (Integer txId : taxonIds) {
					int idx = taxonIds.indexOf(txId);
					Map<String, Object> taxon = new HashMap<>();
					taxon.put("taxonId", txId);
					taxon.put("name", txNames.get(idx));
					taxon.put("rank", txRanks.get(idx));

					lineage.add(taxon);
				}
				request.setAttribute("lineage", lineage);
				request.setAttribute("taxonId", taxonomy.getId());

				PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/breadcrumb/other_tabs.jsp");
				prd.include(request, response);

			}
			else {

				if (cType == null || cType.equals("")) {
					// show nothing
					PrintWriter writer = response.getWriter();
					writer.write(" ");
					writer.close();
				}
				else if (cType.equals("feature")) {

					boolean hasPATRICAnnotation = false;
					List<Map<String, Object>> lineage = new ArrayList<>();

					GenomeFeature feature = solr.getPATRICFeature(cId);
					int taxonId = feature.getTaxonId();

					if (feature.getAnnotation().equals("PATRIC")) {
						hasPATRICAnnotation = true;
					}

					Taxonomy taxonomy = solr.getTaxonomy(taxonId);
					List<Integer> taxonIds = taxonomy.getLineageIds();
					List<String> txNames = taxonomy.getLineageNames();
					List<String> txRanks = taxonomy.getLineageRanks();

					for (Integer txId : taxonIds) {
						int idx = taxonIds.indexOf(txId);
						Map<String, Object> taxon = new HashMap<>();
						taxon.put("taxonId", txId);
						taxon.put("name", txNames.get(idx));
						taxon.put("rank", txRanks.get(idx));

						lineage.add(taxon);
					}

					request.setAttribute("lineage", lineage);
					request.setAttribute("hasPATRICAnnotation", hasPATRICAnnotation);
					request.setAttribute("feature", feature);

					PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/breadcrumb/feature_tabs.jsp");
					prd.include(request, response);
				}
				else if (cType.equals("genome")) {

					List<Map<String, Object>> lineage = new ArrayList<>();
					boolean isBelowGenus = false;
					boolean hasPATRICAnnotation = false;

					Genome genome = solr.getGenome(cId);
					int taxonId = genome.getTaxonId();
					if (genome.getPatricCds() > 0) {
						hasPATRICAnnotation = true;
					}

					Taxonomy taxonomy = solr.getTaxonomy(taxonId);

					List<Integer> txIds = taxonomy.getLineageIds();
					List<String> txNames = taxonomy.getLineageNames();
					List<String> txRanks = taxonomy.getLineageRanks();

					for (Integer txId : txIds) {
						int idx = txIds.indexOf(txId);
						Map<String, Object> taxon = new HashMap<>();
						taxon.put("taxonId", txId);
						taxon.put("name", txNames.get(idx));
						taxon.put("rank", txRanks.get(idx));

						if (txRanks.get(idx).equals("genus")) {
							isBelowGenus = true;
						}
						lineage.add(taxon);
					}

					LOGGER.trace("{},{}", lineage, isBelowGenus);

					request.setAttribute("lineage", lineage);
					request.setAttribute("isBelowGenus", isBelowGenus);
					request.setAttribute("hasPATRICAnnotation", hasPATRICAnnotation);
					request.setAttribute("context", genome);

					PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/breadcrumb/genome_tabs.jsp");
					prd.include(request, response);
				}
				else if (cType.equals("taxon")) {

					List<Map<String, Object>> lineage = new ArrayList<>();
					boolean isBelowGenus = false;

					Taxonomy taxonomy = solr.getTaxonomy(Integer.parseInt(cId));

					List<Integer> txIds = taxonomy.getLineageIds();
					List<String> txNames = taxonomy.getLineageNames();
					List<String> txRanks = taxonomy.getLineageRanks();

					for (Integer taxonId : txIds) {
						int idx = txIds.indexOf(taxonId);
						Map<String, Object> taxon = new HashMap<>();
						taxon.put("taxonId", taxonId);
						taxon.put("name", txNames.get(idx));
						taxon.put("rank", txRanks.get(idx));

						if (txRanks.get(idx).equals("genus")) {
							isBelowGenus = true;
						}
						lineage.add(taxon);
					}

					LOGGER.trace("{},{}", lineage, isBelowGenus);

					request.setAttribute("lineage", lineage);
					request.setAttribute("isBelowGenus", isBelowGenus);

					PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/breadcrumb/taxon_tabs.jsp");
					prd.include(request, response);
				}
			}
		}
	}
}
