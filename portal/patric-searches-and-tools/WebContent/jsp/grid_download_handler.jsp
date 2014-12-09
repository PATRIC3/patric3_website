<%@ page import="edu.vt.vbi.patric.common.ExcelHelper"%><%@ page 
	import="edu.vt.vbi.patric.common.PolyomicHandler"%><%@ page 
	import="edu.vt.vbi.patric.common.SolrInterface"%><%@ page 
	import="edu.vt.vbi.patric.common.SolrCore"%><%@ page 
	import="edu.vt.vbi.patric.common.SQLHelper"%><%@ page 
	import="edu.vt.vbi.patric.common.StringHelper"%><%@ page 
	import="edu.vt.vbi.patric.common.DownloadHelper"%><%@ page
	import="edu.vt.vbi.patric.dao.*"%><%@ page
	import="edu.vt.vbi.patric.beans.*"%><%@ page
	import="org.json.simple.JSONArray"%><%@ page 
	import="org.json.simple.JSONObject"%><%@ page 
	import="org.json.simple.parser.JSONParser"%><%@ page 
	import="org.json.simple.parser.ParseException"%><%@ page 
	import="java.util.*"%><%@ page
	import="org.apache.solr.common.*"%><%@ page
	import="org.apache.solr.client.solrj.*"%><%@ page
	import="org.apache.solr.client.solrj.response.*"%><%@ page
	import="org.apache.solr.client.solrj.impl.LBHttpSolrServer"%><%@ page
	import="java.net.MalformedURLException"%><%@ page
	import="java.io.OutputStream"%><%@ page
	import="org.slf4j.Logger"%><%@ page
    import="org.slf4j.LoggerFactory"%><%@ page
	import="org.apache.commons.lang.StringUtils"%><%

	final Logger LOGGER = LoggerFactory.getLogger("GRID_DOWNLOAD_HANDLER.JSP");
	DBSearch conn_search = new DBSearch();

	String _filename = "";

	List<String> _tbl_header = new ArrayList<String>();
	List<String> _tbl_field = new ArrayList<String>();
	JSONArray _tbl_source = null;

	// getting common params
	String _fileformat = request.getParameter("fileformat");
	String _tablesource = request.getParameter("tablesource");
	ResultType key = new ResultType();

	String sort_field;
	String sort_dir;
	HashMap<String, String> sort = null;

	if (_tablesource == null || _fileformat == null) {
		_fileformat = null;
	}

	ExcelHelper excel = null;

	if (_tablesource.equalsIgnoreCase("Genome")) {

		SolrInterface solr = new SolrInterface();
		String keyword = request.getParameter("download_keyword");
		String genomeId = request.getParameter("gId");
		String taxonId = request.getParameter("cId");
		String cType = request.getParameter("cType");

		if (keyword != null) {
			key.put("keyword", keyword.trim());
		}
		if (cType != null && cType.equals("taxon") && taxonId != null && taxonId.equals("") == false) {
			key.put("taxonId", taxonId);
			key.put("join", SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + key.get("taxonId")));
		}

		
		if (request.getParameter("aT") != null && request.getParameter("aT").equals("0")) {

			sort_field = request.getParameter("gsort");
			sort_dir = request.getParameter("gdir");

			if (sort_field != null && sort_dir != null) {
				sort = new HashMap<String, String>();
				sort.put("field", sort_field);
				sort.put("direction", sort_dir);
			}

			solr.setCurrentInstance(SolrCore.GENOME);
			JSONObject object = solr.getData(key, sort, null, 0, -1, false, false, false);

			JSONObject obj = (JSONObject) object.get("response");
			_tbl_source = (JSONArray) obj.get("docs");

			// read array of header name and fields from a static class
			_tbl_header.addAll(DownloadHelper.getHeaderForGenomes());
			_tbl_field.addAll(DownloadHelper.getFieldsForGenomes());
		}
		else if (request.getParameter("aT") != null && request.getParameter("aT").equals("1")) {

			SolrQuery query = new SolrQuery();

			query.setQuery(key.get("keyword"));
			query.setFilterQueries("taxon_lineage_ids:" + taxonId);
			query.addField("genome_id");
			query.setRows(500000);

			JSONObject gObject = solr.ConverttoJSON(solr.getSolrServer(SolrCore.GENOME), query, false, false);
			List<String> listGenomeId = new ArrayList<String>();
			JSONArray ret = (JSONArray) ((JSONObject) gObject.get("response")).get("docs");
			for (Object row : ret) {
				JSONObject gid = (JSONObject) row;
				listGenomeId.add(gid.get("genome_id").toString());
			}
			if (listGenomeId.size() > 0) {
				key.put("keyword", "genome_id: (" + StringUtils.join(listGenomeId, " OR ") + ")");
			}

			solr.setCurrentInstance(SolrCore.SEQUENCE);
            key.put("fields", StringUtils.join(DownloadHelper.getFieldsForGenomeSequence(), ","));
			JSONObject object = solr.getData(key, sort, null, 0, -1, false, false, false);

			JSONObject obj = (JSONObject) object.get("response");
			_tbl_source = (JSONArray) obj.get("docs");

			sort_field = request.getParameter("sort");
			sort_dir = request.getParameter("dir");

			if (sort_field != null && sort_dir != null) {
				sort = new HashMap<String, String>();
				sort.put("field", sort_field);
				sort.put("direction", sort_dir);
			}

			_tbl_header.addAll(DownloadHelper.getHeaderForGenomeSequence());
			_tbl_field.addAll(DownloadHelper.getFieldsForGenomeSequence());
		}

		_filename = "GenomeFinder";
	}
	else if (_tablesource.equalsIgnoreCase("Feature")) {

		SolrInterface solr = new SolrInterface();
		String keyword = request.getParameter("download_keyword");
		String taxonId = request.getParameter("taxonId");

		key.put("keyword", keyword);
		if (taxonId != null && taxonId.equals("") == false) {
			key.put("taxonId", taxonId);
			key.put("join", SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + key.get("taxonId")));
		}
		key.put("fields", StringUtils.join(DownloadHelper.getFieldsForFeatures(), ","));

		sort_field = request.getParameter("sort");
		sort_dir = request.getParameter("dir");

		if (sort_field != null && sort_dir != null) {
			sort = new HashMap<String, String>();
			sort.put("field", sort_field);
			sort.put("direction", sort_dir);
		}
		solr.setCurrentInstance(SolrCore.FEATURE);
		JSONObject object = solr.getData(key, sort, null, 0, -1, false, false, false);

		JSONObject obj = (JSONObject) object.get("response");
		_tbl_source = (JSONArray) obj.get("docs");

		if (request.getParameter("subtablesource") != null && request.getParameter("subtablesource").toString().equals("FigFam")) {
			_tbl_header.addAll(Arrays.asList("ID"));
			_tbl_field.addAll(Arrays.asList("figfam_id"));
		}

		_tbl_header.addAll(DownloadHelper.getHeaderForFeatures());
		_tbl_field.addAll(DownloadHelper.getFieldsForFeatures());

		_filename = "FeatureTable";
	}
	else if (_tablesource.equalsIgnoreCase("GlobalSearch")) {

		SolrInterface solr = new SolrInterface();
		String keyword = request.getParameter("download_keyword");
		String cat = request.getParameter("cat");

		key.put("keyword", keyword);

		if (cat.equals("2")) {
			solr.setCurrentInstance(SolrCore.TAXONOMY);
			_tbl_header.addAll(Arrays.asList("Taxon ID", "Taxon Name", "# of Genomes"));
			_tbl_field.addAll(Arrays.asList("taxon_id", "taxon_name", "genomes"));
		}
		else if (cat.equals("3")) {
			solr.setCurrentInstance(SolrCore.TRANSCRIPTOMICS_EXPERIMENT);
			_tbl_header.addAll(Arrays.asList("Experiment ID", "Title", "Comparisons", "Genes", "PubMed", "Accession", "Organism", "Strain",
					"Gene Modification", "Experimental Condition", "Time Series", "Release Date", "Author", "PI", "Institution"));
			_tbl_field.addAll(Arrays.asList("eid", "title", "samples", "genes", "pmid", "accession", "organism", "strain", "mutant",
					"condition", "timeseries", "release_date", "author", "pi", "institution"));
		}
		else if (cat.equals("1")) {
			solr.setCurrentInstance(SolrCore.GENOME);
			_tbl_header.addAll(DownloadHelper.getHeaderForGenomes());
			_tbl_field.addAll(DownloadHelper.getFieldsForGenomes());
		}
		else if (cat.equals("0")) {
			solr.setCurrentInstance(SolrCore.FEATURE);
			_tbl_header.addAll(DownloadHelper.getHeaderForFeatures());
			_tbl_field.addAll(DownloadHelper.getFieldsForFeatures());
		}

		JSONObject object = solr.getData(key, sort, null, 0, -1, false, false, false);
		JSONObject obj = (JSONObject) object.get("response");
		_tbl_source = (JSONArray) obj.get("docs");
		_tbl_header.addAll(Arrays.asList(new String[] {}));
		_tbl_field.addAll(Arrays.asList(new String[] {}));

		_filename = "GlobalSearch";
	}
	else if (_tablesource.equalsIgnoreCase("IDMapping")) {

		String from = request.getParameter("from");
		String to = request.getParameter("to");
		String keyword = request.getParameter("keyword");

		key.put("from", from);
		key.put("to", to);
		key.put("keyword", keyword);

		sort_field = request.getParameter("sort");
		sort_dir = request.getParameter("dir");

		if (sort_field != null && sort_dir != null) {
			sort = new HashMap<String, String>();
			sort.put("field", sort_field);
			sort.put("direction", sort_dir);
		}

		String header = "", id = "";

		if (to.equals("UniProtKB-ID")) {
			header = "UniProtKB-ID";
			id = "uniprotkb_accession";
		}
		else if (to.equals("RefSeq Locus Tag")) {
			header = "RefSeq Locus Tag";
			id = "refseq_source_id";
		}
		else if (to.equals("RefSeq")) {
			header = "RefSeq";
			id = "protein_id";
		}
		else if (to.equals("Gene ID")) {
			header = "GeneID";
			id = "gene_id";
		}
		else if (to.equals("GI")) {
			header = "GI";
			id = "gi_number";
		}
		else if (to.equals("PATRIC ID")) {
			header = "PATRIC ID";
			id = "na_feature_id";
		}
		else if (to.equals("PATRIC Locus Tag")) {
			header = from;

			if (from.equals("RefSeq Locus Tag")) {
				id = "refseq_source_id";
			}
			else if (from.equals("RefSeq")) {
				id = "protein_id";
			}
			else if (from.equals("UniProtKB-ID")) {
				id = "uniprotkb_accession";
			}
			else if (from.equals("Gene ID")) {
				id = "gene_id";
			}
			else if (from.equals("GI")) {
				id = "gi_number";
			}
			else if (from.equals("PATRIC ID")) {
				id = "na_feature_id";
			}
			else if (from.equals("PSEED ID")) {
				id = "pseed_id";
			}
			else {
				id = "requested_data";
			}
		}
		else if (to.equals("PSEED ID")) {
			header = "PSEED ID";
			id = "pseed_id";
		}
		else {
			header = to;
			id = "requested_data";
		}

		ArrayList<ResultType> _tbl_source_ = conn_search.getIDSearchResult(key.toHashMap(), sort, 0, -1);
		_tbl_source = new JSONArray();
		JSONObject object = null;
		for (ResultType obj : _tbl_source_) {
			object = new JSONObject();
			object.putAll(obj);
			_tbl_source.add(object);
		}
		_tbl_header.addAll(Arrays.asList("Genome", "Accession", "Locus Tag", header, "Annotation", "Feature Type", "Start", "End",
				"Length(NT)", "Strand", "Length (AA)", "Product Description"));
		_tbl_field.addAll(Arrays.asList("genome_name", "accession", "locus_tag", id, "algorithm", "name", "start_max", "end_min",
				"na_length", "strand", "aa_length", "product"));

		_filename = "IDMapping";
	}
	else if (_tablesource.equalsIgnoreCase("Workspace")) {
		String idType = request.getParameter("idType");
		String idList = request.getParameter("fids");

		SolrInterface solr = new SolrInterface();
		JSONObject object = null;
		Map<String, Object> condition = new HashMap<String, Object>();

		if (idType.equals("Feature")) {
			if (idList.contains(",")) {
				condition.put("na_feature_ids", idList);
			}
			else {
				condition.put("na_feature_id", idList);
			}
			object = solr.getFeaturesByID(condition);
			_tbl_header.addAll(DownloadHelper.getHeaderForFeatures());
			_tbl_field.addAll(DownloadHelper.getFieldsForFeatures());

			_tbl_source = (JSONArray) object.get("results");
			_filename = "Workspace_Features";
		}
		else if (idType.equals("Genome")) {

			if (idList.contains(",")) {
				condition.put("genome_info_ids", idList);
			}
			else {
				condition.put("genome_info_id", idList);
			}
			object = solr.getGenomesByID(condition);

			_tbl_header.addAll(DownloadHelper.getHeaderForGenomes());
			_tbl_field.addAll(DownloadHelper.getFieldsForGenomes());

			_tbl_source = (JSONArray) object.get("results");
			_filename = "Workspace_Genomes";
		}
		else if (idType.equals("ExpressionExperiment")) {

			ArrayList<String> collectionIds = new ArrayList<String>();
			JSONArray PATRICExperiments = new JSONArray();
			JSONArray results = new JSONArray();
			JSONObject resPATRIC = null;
			JSONObject resUser = null;

			if (idList != null && idList != "{}") {

				JSONParser parser = new JSONParser();
				JSONObject fids = (JSONObject) parser.parse(idList);
				String token = fids.get("token").toString();

				if (fids.containsKey("PATRICExperiments")) {
					PATRICExperiments = (JSONArray) parser.parse(fids.get("PATRICExperiments").toString());

					// reading PATRIC Experiments
					HashMap<String, Object> _key = new HashMap<String, Object>();
					_key.put("tracks", PATRICExperiments);
					resPATRIC = solr.getExperimentsByID(_key);
				}

				if (fids.containsKey("USERExperiments")) {
					collectionIds.addAll((JSONArray) fids.get("USERExperiments"));

					// reading USER Experiments from collection
					PolyomicHandler polyomic = new PolyomicHandler();
					polyomic.setAuthenticationToken(token);
					resUser = polyomic.getExperiments(collectionIds);
				}

				// merging
				if (resPATRIC.containsKey("results")) {
					for (Object exp : (JSONArray) resPATRIC.get("results")) {
						JSONObject jsonExp = (JSONObject) exp;
						jsonExp.put("source", "PATRIC");
						results.add(jsonExp);
					}
				}

				if (resUser.containsKey("results")) {
					for (Object exp : (JSONArray) resUser.get("results")) {
						JSONObject jsonExp = (JSONObject) exp;
						jsonExp.put("source", "me");
						results.add(jsonExp);
					}
				}
			}

			_tbl_header.addAll(Arrays.asList("Source", "Experiment Title", "PubMed", "Comparisons", "Organism"));
			_tbl_field.addAll(Arrays.asList("source", "title", "pmid", "samples", "organism"));

			_tbl_source = results;
			_filename = "Workspace_Experiments";
		}
		else if (idType.equals("ExpressionSample")) {

			JSONArray results = new JSONArray();

			if (idList != null && idList != "{}") {

				JSONParser parser = new JSONParser();
				JSONObject fids = (JSONObject) parser.parse(idList);
				String token = fids.get("token").toString();

				if (fids.get("source").equals("PATRIC")) {

					ResultType _key = new ResultType();
					_key.put("keyword", "eid:" + fids.get("eid"));
					solr.setCurrentInstance(SolrCore.TRANSCRIPTOMICS_COMPARISON);
					JSONObject samples = solr.getData(_key, null, null, 0, 10000, false, false, false);
					JSONObject obj = (JSONObject) samples.get("response");

					results = (JSONArray) obj.get("docs");
				}
				else {
					PolyomicHandler polyomic = new PolyomicHandler();
					polyomic.setAuthenticationToken(token);

					results = polyomic.getSamples(fids.get("expId").toString(), null);
				}
			}

			_tbl_header.addAll(Arrays.asList("Accession", "Title", "Genes", "Significant genes(Log Ratio)", "Significant genes(Z score)",
					"Organism", "Strain", "Gene Modification", "Experimental Condition", "Time Point"));
			_tbl_field.addAll(Arrays.asList("accession", "expname", "genes", "sig_log_ratio", "sig_z_score", "organism", "strain", "mutant",
					"condition", "timepoint"));

			_tbl_source = results;
			_filename = "Workspace_Comparisons";
		}
	}
	else if (_tablesource.equalsIgnoreCase("GENEXP_Experiment")) {

		SolrInterface solr = new SolrInterface();
		String keyword = request.getParameter("download_keyword");

		sort_field = request.getParameter("sort");
		sort_dir = request.getParameter("dir");

		if (sort_field != null && sort_dir != null) {
			sort = new HashMap<String, String>();
			sort.put("field", sort_field);
			sort.put("direction", sort_dir);
		}

		if (keyword != null) {
			key.put("keyword", keyword.trim());
		}

		if (request.getParameter("aT").equals("0")) {

			solr.setCurrentInstance(SolrCore.TRANSCRIPTOMICS_EXPERIMENT);
			JSONObject object = solr.getData(key, sort, null, 0, -1, false, false, false);
			JSONObject obj = (JSONObject) object.get("response");
			_tbl_source = (JSONArray) obj.get("docs");
			_tbl_header.addAll(Arrays.asList("Experiment ID", "Title", "Comparisons", "Genes", "PubMed", "Accession", "Organism", "Strain",
					"Gene Modification", "Experimental Condition", "Time Series", "Release Date", "Author", "PI", "Institution"));

			_tbl_field.addAll(Arrays.asList("eid", "title", "samples", "genes", "pmid", "accession", "organism", "strain", "mutant",
					"condition", "timeseries", "release_date", "author", "pi", "institution"));
		}
		else if (request.getParameter("aT").equals("1")) {

			String solrId = "";
			solr.setCurrentInstance(SolrCore.TRANSCRIPTOMICS_EXPERIMENT);

			JSONObject object_t = solr.getData(key, null, null, 0, 10000, false, false, false);
			JSONObject obj_t = (JSONObject) object_t.get("response");
			JSONArray obj1 = (JSONArray) obj_t.get("docs");

			for (Object ob : obj1) {
				JSONObject doc = (JSONObject) ob;
				if (solrId.length() == 0) {
					solrId += doc.get("accession").toString();
				}
				else {
					solrId += "," + doc.get("accession").toString();
				}
			}

			key.put("keyword", solr.ConstructKeyword("accession", solrId));

			////////////////////////

			solr.setCurrentInstance(SolrCore.TRANSCRIPTOMICS_COMPARISON);

			JSONObject object = solr.getData(key, sort, null, 0, -1, false, false, false);
			JSONObject obj = (JSONObject) object.get("response");
			_tbl_source = (JSONArray) obj.get("docs");
			_tbl_header.addAll(Arrays.asList("Experiment ID", "Comparison ID", "Title", "Genes", "Significant genes(Log Ratio)",
					"Significant genes(Z Score)", "PubMed", "Accession", "Organism", "Strain", "Gene Modification", "Experiment Condition",
					"Time Point", "Release Date"));

			_tbl_field.addAll(Arrays.asList("eid", "pid", "expname", "genes", "sig_log_ratio", "sig_z_score", "pmid", "accession",
					"organism", "strain", "mutant", "condition", "timepoint", "release_date"));
		}

		_filename = "Transcriptomics";
	}
	else if (_tablesource.equalsIgnoreCase("Proteomics_Experiment")) {

		SolrInterface solr = new SolrInterface();
		String keyword = request.getParameter("download_keyword");
		String experiment_id = request.getParameter("experiment_id");

		sort_field = request.getParameter("sort");
		sort_dir = request.getParameter("dir");

		if (sort_field != null && sort_dir != null) {
			sort = new HashMap<String, String>();
			sort.put("field", sort_field);
			sort.put("direction", sort_dir);
		}

		if (keyword != null) {
			key.put("keyword", keyword.trim());
		}

		if (request.getParameter("aT").equals("0")) {

			solr.setCurrentInstance(SolrCore.PROTEOMICS_EXPERIMENT);
			JSONObject object = solr.getData(key, sort, null, 0, -1, false, false, false);
			JSONObject obj = (JSONObject) object.get("response");
			_tbl_source = (JSONArray) obj.get("docs");
			_tbl_header.addAll(Arrays.asList("Sample Name", "Taxon Name", "Proteins", "Project Name", "Experiment Label", "Experiment Title",
					"Experiment Type", "Source", "Contact Name", "Institution"));

			_tbl_field.addAll(Arrays.asList("sample_name", "taxon_name", "proteins", "project_name", "experiment_label", "experiment_title",
					"experiment_type", "source", "contact_name", "institution"));
		}
		else if (request.getParameter("aT").equals("1")) {

			String solrId = "";
			solr.setCurrentInstance(SolrCore.PROTEOMICS_PROTEIN);

			if (experiment_id != null && !experiment_id.equals("")) {
				keyword += " AND experiment_id:(" + experiment_id + ")";
			}

			key.put("keyword", keyword.trim());

			JSONObject object_t = solr.getData(key, null, null, 0, -1, false, false, false);
			JSONObject obj_t = (JSONObject) object_t.get("response");
			_tbl_source = (JSONArray) obj_t.get("docs");

			_tbl_header.addAll(Arrays.asList("Experiment Title", "Experiment Label", "Source", "Genome Name", "Accession", "Locus Tag",
					"RefSeq Locus Tag", "Gene Symbol", "Description"));
			_tbl_field.addAll(Arrays.asList("experiment_title", "experiment_label", "source", "genome_name", "accession", "locus_tag",
					"refseq_locus_tag", "refseq_gene", "product"));
		}

		_filename = "Proteomics";
	}
	else if (_tablesource.equalsIgnoreCase("GeneExpression")) {

// TranscriptomicsGeneExp.java //

		String idList = request.getParameter("fids");
		JSONParser parser = new JSONParser();
		JSONObject fids = (JSONObject) parser.parse(idList);

		String paramFeatureId =  fids.get("feature_id").toString();
        String paramSampleId = fids.get("pid").toString();

        JSONObject jsonResult = new JSONObject();
		SolrInterface solr = new SolrInterface();
        try {
            LBHttpSolrServer lbHttpSolrServer = solr.getSolrServer(SolrCore.TRANSCRIPTOMICS_GENE);
            SolrQuery query = new SolrQuery();

//            if (paramKeyword != null && !paramKeyword.equals("")) {
//               query.setQuery(paramKeyword + " AND feature_id:" + paramFeatureId);
//            }
//            else {
                query.setQuery("feature_id:" + paramFeatureId);
//            }

            if (paramSampleId != null && !paramSampleId.equals("")) {
                String[] pids = paramSampleId.split(",");

                query.addFilterQuery("pid:(" + StringUtils.join(pids, " OR ") + ")");
            }
//            if (paramLogRatio != null && !paramLogRatio.equals("") && !paramLogRatio.equals("0")) {
//                query.addFilterQuery("log_ratio:[* TO -" + paramLogRatio + "] OR log_ratio:[" + paramLogRatio + " TO *]");
//            }
//            if (paramZScore != null && !paramZScore.equals("") && !paramZScore.equals("0")) {
//                query.addFilterQuery("z_score:[* TO -" + paramZScore + "] OR z_score:[" + paramZScore + " TO *]");
//            }

            LOGGER.debug("{}", query.toString());

            QueryResponse qr = lbHttpSolrServer.query(query);
            long numFound = qr.getResults().getNumFound();

            query.setRows((int) numFound);

            qr = lbHttpSolrServer.query(query);

            // features
            JSONArray features = new JSONArray();
            SolrDocumentList sdl = qr.getResults();
            for (SolrDocument doc : sdl) {
                JSONObject feature = new JSONObject();
					feature.put("exp_accession", doc.get("accession"));
					// feature.put("exp_channels", doc.get(""));
					feature.put("exp_condition", doc.get("condition"));
					feature.put("exp_id", doc.get("eid"));
					feature.put("exp_locustag", doc.get("refseq_locus_tag"));
					feature.put("exp_mutant", doc.get("mutant"));
					feature.put("exp_name", doc.get("expname"));
					feature.put("exp_organism", doc.get("organism"));
					feature.put("exp_pavg", doc.get("avg_intensity"));
					feature.put("exp_platform", doc.get("")); // ??
					feature.put("exp_pratio", doc.get("log_ratio"));
					feature.put("exp_samples", doc.get("")); // ??
					feature.put("exp_strain", doc.get("")); // ??
					feature.put("exp_timepoint", doc.get("timepoint"));
					feature.put("exp_zscore", doc.get("z_score"));
					// feature.put("figfam_id", doc.get("")); // ??
					feature.put("locus_tag", doc.get("alt_locus_tag"));
					feature.put("feature_id", doc.get("feature_id"));
					feature.put("pid", doc.get("pid"));
					feature.put("pmid", doc.get("pmid"));

                features.add(feature);
            }
            jsonResult.put("features", features);
        }
        catch (MalformedURLException me) {
            LOGGER.error(me.getMessage(), me);
        }
        catch (SolrServerException e) {
            LOGGER.error(e.getMessage(), e);
        }
// TranscriptomicsGeneExp.java //

        _tbl_source = (JSONArray) jsonResult.get("features");

		_tbl_header.addAll(Arrays.asList("Platform", "Samples", "Locus Tag", "Title", "PubMed", "Accession", "Strain", "Gene Modification",
				"Experimental Condition", "Time Point", "Avg Intensity", "Log Ratio", "Z-score"));
		_tbl_field.addAll(Arrays.asList("exp_platform", "exp_samples", "exp_locustag", "exp_name", "pmid", "exp_accession", "exp_strain",
				"exp_mutant", "exp_condition", "exp_timepoint", "exp_pavg", "exp_pratio", "exp_zscore"));

		_filename = "GeneExpression";
	}
	else if (_tablesource.equalsIgnoreCase("Correlation")) {

		String cutoffValue = request.getParameter("cutoffValue");
		String cutoffDir = request.getParameter("cutoffDir");
		String featureId = request.getParameter("cId");

// TranscriptomicsGeneExp.java //
		JSONObject jsonResult = new JSONObject();
		SolrInterface solr = new SolrInterface();

		GenomeFeature feature = solr.getFeature(featureId);
		Map<String,Map<String, Object>> correlationMap = new HashMap<String,Map<String, Object>>();
		long numFound = 0;

		try {
			SolrQuery query = new SolrQuery("genome_id:" + feature.getGenomeId());
			query.setFilterQueries("{!correlation fieldId=refseq_locus_tag fieldCondition=pid fieldValue=log_ratio srcId=" + feature.getRefseqLocusTag() + " filterCutOff=" + cutoffValue + " filterDir=" + cutoffDir.substring(0,3) + " cost=101}");
			query.setRows(0);

			QueryResponse qr = solr.getSolrServer(SolrCore.TRANSCRIPTOMICS_GENE).query(query);

			SolrDocumentList sdl = (SolrDocumentList) qr.getResponse().get("correlation");
			numFound = sdl.getNumFound();

			for (SolrDocument doc: sdl) {
				Map<String, Object> corr = new HashMap<String, Object>();
				corr.put("id", doc.get("id"));
				corr.put("correlation", doc.get("correlation"));
				corr.put("conditions", doc.get("conditions"));
				corr.put("p_value", doc.get("p_value"));

				correlationMap.put(doc.get("id").toString(), corr);
			}

		} catch (MalformedURLException me) {
	    	LOGGER.error(me.getMessage(), me);
		} catch (SolrServerException e) {
			LOGGER.error(e.getMessage(), e);
		}

		jsonResult.put("total", numFound);
		JSONArray results = new JSONArray();

		try {
			SolrQuery query = new SolrQuery("refseq_locus_tag:(" + StringUtils.join(correlationMap.keySet(), " OR ") + ")");
			query.setFilterQueries("annotation:PATRIC");
			query.setFields("genome_id,genome_name,accession,feature_id,start,end,strand,feature_type,annotation,alt_locus_tag,refseq_locus_tag,seed_id,na_length,aa_length,protein_id,gene,product");
			query.setRows((int) numFound);

			QueryResponse qr = solr.getSolrServer(SolrCore.FEATURE).query(query);
			List<GenomeFeature> features = qr.getBeans(GenomeFeature.class);

			for (GenomeFeature f: features) {
				JSONObject obj = new JSONObject();
				obj.put("genome_id", f.getGenomeId());
				obj.put("genome_name", f.getGenomeName());
				obj.put("accession", f.getAccession());
				obj.put("feature_id", f.getId());
				obj.put("alt_locus_tag", f.getAltLocusTag());
				obj.put("refseq_locus_tag", f.getRefseqLocusTag());
				obj.put("seed_id", f.getSeedId());
				obj.put("gene", f.getGene());
				obj.put("annotation", f.getAnnotation());
				obj.put("feature_type", f.getFeatureType());
				obj.put("start", f.getStart());
				obj.put("end", f.getEnd());
				obj.put("na_length", f.getNaSequenceLength());
				obj.put("strand", f.getStrand());
				obj.put("protein_id", f.getProteinId());
				obj.put("aa_length", f.getProteinLength());
				obj.put("product", f.getProduct());

				Map<String, Object> corr = correlationMap.get(f.getRefseqLocusTag());
				obj.put("correlation", corr.get("correlation"));
				obj.put("count", corr.get("conditions"));

				results.add(obj);
			}
			jsonResult.put("results", results);

		} catch (MalformedURLException me) {
	    	LOGGER.error(me.getMessage(), me);
		} catch (SolrServerException e) {
			LOGGER.error(e.getMessage(), e);
		}
// TranscriptomicsGeneExp.java //

        _tbl_source = (JSONArray) jsonResult.get("results");

		_tbl_header.addAll(Arrays.asList("Genome Name", "Accession", "SEED ID", "Alt Locus Tag", "RefSeq Locus Tag", "Gene Symbol", "Annotation",
				"Feature Type", "Start", "End", "Length(NT)", "Strand", "Protein ID", "Length(AA)", "Product Description", "Correlations",
				"Comparisons"));
		_tbl_field.addAll(Arrays.asList("genome_name", "accession", "seed_id", "alt_locus_tag", "refseq_locus_tag", "gene", "annotation", "feature_type",
				"start", "end", "na_length", "strand", "protein_id", "aa_length", "product", "correlation", "count"));

		_filename = "Correlated Genes";
	}
	else if (_tablesource.equalsIgnoreCase("SingleExperiment")) {

		HashMap<String, String> k = new HashMap<String, String>();

		String cId = request.getParameter("cId");
		String cType = request.getParameter("cType");
		String eid = request.getParameter("eid");

		k.put("cId", cId);
		k.put("cType", cType);
		k.put("eid", eid);

		sort_field = request.getParameter("sort");
		sort_dir = request.getParameter("dir");

		if (sort_field != null && sort_dir != null) {
			sort = new HashMap<String, String>();
			sort.put("field", sort_field);
			sort.put("direction", sort_dir);
		}

		SolrInterface solr = new SolrInterface();
		_tbl_source = (JSONArray) ((JSONObject) solr.getTranscriptomicsSamples(null, eid, "", 0, -1, sort)).get("data");

		_tbl_header.addAll(Arrays.asList("Experiment ID", "Comparison ID", "Title", "Genes", "Significant genes(Log Ratio)",
				"Significant genes(Z Score)", "PubMed", "Accession", "Organism", "Strain", "Gene Modification", "Experiment Condition",
				"Time Point", "Release Date"));

		_tbl_field.addAll(Arrays.asList("eid", "pid", "expname", "genes", "sig_log_ratio", "sig_z_score", "pmid", "accession", "organism",
				"strain", "mutant", "condition", "timepoint", "release_date"));

		_filename = "SingleExperiment";
	}
	else if (_tablesource.equalsIgnoreCase("SpecialtyGeneMapping")) {

		String keyword = request.getParameter("download_keyword");
		String taxonId = request.getParameter("cId");
		String cType = request.getParameter("cType");

		key.put("keyword", keyword);
		if (cType != null && cType.equals("taxon") && taxonId != null && taxonId.equals("") == false) {
			key.put("taxonId", taxonId);
			key.put("join", SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + key.get("taxonId")));
		}

		sort_field = request.getParameter("sort");
		sort_dir = request.getParameter("dir");

		if (sort_field != null && sort_dir != null) {
			sort = new HashMap<String, String>();
			sort.put("field", sort_field);
			sort.put("direction", sort_dir);
		}

		SolrInterface solr = new SolrInterface();
		solr.setCurrentInstance(SolrCore.SPECIALTY_GENE_MAPPING);
		JSONObject object_t = solr.getData(key, sort, null, 0, -1, false, false, false);
		JSONObject obj_t = (JSONObject) object_t.get("response");
		_tbl_source = (JSONArray) obj_t.get("docs");

		_tbl_header.addAll(Arrays.asList("Evidence", "Property", "Source", "Genome Name", "SEED ID", "RefSeq Locus Tag", "Alt Locus Tag", "Source ID",
				"Source Organism", "Gene", "Product", "Function", "Classification", "PubMed", "Subject Coverage", "Query Coverage",
				"Identity", "E-value"));

		_tbl_field.addAll(Arrays.asList("evidence", "property", "source", "genome_name", "seed_id", "refseq_locus_tag", "alt_locus_tag", "source_id",
				"organism", "gene", "product", "function", "classification", "pmid", "subject_coverage", "query_coverage", "identity",
				"e_value"));

		_filename = "SpecialtyGene";
	}
	else if (_tablesource.equalsIgnoreCase("SpecialtyGene")) {

		String keyword = request.getParameter("download_keyword");
		key.put("keyword", keyword);

		sort_field = request.getParameter("sort");
		sort_dir = request.getParameter("dir");

		if (sort_field != null && sort_dir != null) {
			sort = new HashMap<String, String>();
			sort.put("field", sort_field);
			sort.put("direction", sort_dir);
		}

		SolrInterface solr = new SolrInterface();
		solr.setCurrentInstance(SolrCore.SPECIALTY_GENE);
		JSONObject object_t = solr.getData(key, sort, null, 0, -1, false, false, false);
		JSONObject obj_t = (JSONObject) object_t.get("response");
		_tbl_source = (JSONArray) obj_t.get("docs");

		_tbl_header.addAll(Arrays.asList("Property", "Source", "Source ID", "Gene", "Organism", "Locus Tag", "Gene ID", "GI", "Product",
				"Function", "Classification", "PubMed"));

		_tbl_field.addAll(Arrays.asList("property", "source", "source_id", "gene_name", "organism", "locus_tag", "gene_id", "gi", "product",
				"function", "classification", "pmid"));

		_filename = "SpecialtyGeneSource";
	}

	excel = new ExcelHelper("xssf", _tbl_header, _tbl_field, _tbl_source);
	excel.buildSpreadsheet();

	if (_fileformat.equalsIgnoreCase("xlsx")) {
		response.setContentType("application/octetstream");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + _filename + "." + _fileformat + "\"");

		OutputStream outs = response.getOutputStream();
		excel.writeSpreadsheettoBrowser(outs);
	}
	else if (_fileformat.equalsIgnoreCase("txt")) {

		response.setContentType("application/octetstream");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + _filename + "." + _fileformat + "\"");

		out.println(excel.writeToTextFile());
	}
%>
