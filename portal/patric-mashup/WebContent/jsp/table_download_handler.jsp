<%@ page import="java.util.ArrayList"%><%@ page
	import="java.util.Arrays"%><%@ page 
	import="java.util.List"%><%@ page
	import="org.json.simple.JSONArray"%><%@ page
	import="org.json.simple.JSONObject"%><%@ page
	import="edu.vt.vbi.patric.common.ExcelHelper"%><%@ page
	import="edu.vt.vbi.patric.mashup.EutilInterface"%><%@ page
	import="edu.vt.vbi.patric.mashup.ArrayExpressInterface"%><%@ page
	import="edu.vt.vbi.patric.mashup.PRIDEInterface"%><%@ page
	import="edu.vt.vbi.patric.mashup.PSICQUICInterface"%><%@ page
	import="edu.vt.vbi.patric.dao.DBSummary"%><%@ page
	import="java.io.OutputStream"%>
<%@ page import="edu.vt.vbi.patric.common.DataApiHandler" %>
<%@ page import="edu.vt.vbi.patric.beans.Genome" %>
<%@ page import="edu.vt.vbi.patric.beans.Taxonomy" %>
<%
	String _filename = "";
	List<String> _tbl_header = new ArrayList<String>();
	List<String> _tbl_field = new ArrayList<String>();
	JSONArray _tbl_source = null;

	// getting common params
	String _fileformat = request.getParameter("fileformat");
	String _tablesource = request.getParameter("tablesource");

	String cType = "";
	String cId = "";
	String keyword = null;

	if (_tablesource == null || _fileformat == null) {
		_fileformat = null;
	}

	if (_tablesource.equalsIgnoreCase("GEO")) {
		String filter = "";
		if (request.getParameter("context_type") != null) {
			cType = request.getParameter("context_type");
		}
		if (request.getParameter("context_id") != null && !request.getParameter("context_id").equals("")) {
			cId = request.getParameter("context_id");
		}
		if (request.getParameter("filter") != null) {
			filter = request.getParameter("filter");
		}
		if (request.getParameter("keyword") != null) {
			keyword = request.getParameter("keyword");
		}

		_filename = "GEO_Data";

		String strQueryTerm = "txid" + cId + "[Organism:exp]";
		if (filter != null && !filter.equals("")) {
			strQueryTerm = strQueryTerm + "+AND+" + filter + "[ETYP]";
		}
		else {
			strQueryTerm = strQueryTerm + "+NOT+gsm[ETYP]";
		}
		if (keyword != null && !keyword.equals("")) {
			strQueryTerm = keyword;
		}

		EutilInterface eutil_api = new EutilInterface();

		JSONObject jsonResult = eutil_api.getResults("gds", strQueryTerm, "", "", 0, -1);

		_tbl_source = (JSONArray) jsonResult.get("results");
		_tbl_header.addAll(Arrays.asList("Data Type", "ID", "Title", "Organism", "Experiment Type", "Samples", "Publication", "Date",
				"Summary", "Platform", "Subset Info", "Download(SOFT)", "Download(MINiML)", "Download(SeriesMatrix)",
				"Download(Supplementary)"));
		_tbl_field.addAll(Arrays.asList("dataType", "ID", "title", "taxon", "expType", "n_samples", "pubmed_id", "PDAT", "summary",
				"platform", "subsetInfo", "link_soft_format", "link_miniml_format", "link_seriesmatrix", "link_supplementary"));

		_filename = "PATRIC_GEO";

	}
	else if (_tablesource.equalsIgnoreCase("ArrayExpress")) {
		if (request.getParameter("context_type") != null) {
			cType = request.getParameter("context_type");
		}
		if (request.getParameter("context_id") != null && !request.getParameter("context_id").equals("")) {
			cId = request.getParameter("context_id");
		}
		if (request.getParameter("keyword") != null) {
			keyword = request.getParameter("keyword");
		}

		_filename = "ArrayExpress_Data";

		DataApiHandler dataApi = new DataApiHandler();
		String species_name = "";

		if (cType.equals("taxon")) {
			Taxonomy taxon = dataApi.getTaxonomy(Integer.parseInt(cId));
			species_name = taxon.getTaxonName();
		}
		else if (cType.equals("genome")) {
			Genome genome = dataApi.getGenome(cId);
			species_name = genome.getGenomeName();
		}

		ArrayExpressInterface api = new ArrayExpressInterface();

		JSONObject jsonResult = api.getResults(keyword, species_name);

		_tbl_source = (JSONArray) jsonResult.get("results");
		_tbl_header.addAll(Arrays.asList("ID", "Title", "Organism", "Type", "Assays", "Samples", "Publication", "Data Download", "Date",
				"Descriptoin", "Experiment Design"));
		_tbl_field.addAll(Arrays.asList("accession", "name", "species", "experimenttype", "assays", "samples", "pubmed_id", "link_data",
				"releasedate", "description", "experimentdesign"));

		_filename = "PATRIC_ArrayExpress";
	}
	else if (_tablesource.equalsIgnoreCase("Peptidome")) {
		String filter = "";
		if (request.getParameter("context_type") != null) {
			cType = request.getParameter("context_type");
		}
		if (request.getParameter("context_id") != null && !request.getParameter("context_id").equals("")) {
			cId = request.getParameter("context_id");
		}
		if (request.getParameter("filter") != null) {
			filter = request.getParameter("filter");
		}
		String tId = "";

		_filename = "Peptidome_Data";

		if (cType.equals("taxon")) {
			tId = cId;
		}
		else if (cType.equals("genome")) {
			DataApiHandler dataApi = new DataApiHandler();
			Genome genome = dataApi.getGenome(cId);
			tId = "" + genome.getTaxonId();
		}

		String strQueryTerm = "txid" + tId + "[Organism:exp]";
		if (filter != null) {
			strQueryTerm = strQueryTerm + "+AND+" + filter + "[ETYP]";
		}

		EutilInterface eutil_api = new EutilInterface();

		JSONObject jsonResult = eutil_api.getResults("pepdome", strQueryTerm, "", "", 0, -1);

		_tbl_source = (JSONArray) jsonResult.get("results");
		_tbl_header.addAll(Arrays.asList("Accession", "Title", "Organism", "Samples", "Proteins", "Peptides", "Spectra", "Publication",
				"Summary", "Download"));
		_tbl_field.addAll(Arrays.asList("Accession", "title", "TaxName", "SampleCount", "ProteinCount", "PeptideCount", "SpectraCount",
				"pubmed_id", "summary", "link_data_file"));

		_filename = "PATRIC_Peptidome";
	}
	else if (_tablesource.equalsIgnoreCase("PRIDE")) {

		if (request.getParameter("context_type") != null) {
			cType = request.getParameter("context_type");
		}
		if (request.getParameter("context_id") != null && !request.getParameter("context_id").equals("")) {
			cId = request.getParameter("context_id");
		}

		_filename = "PRIDE_Data";

		DBSummary conn_summary = new DBSummary();
		String species_name = "";

		if (cType.equals("taxon")) {
			species_name = conn_summary.getPRIDESpecies(cId);
		}
		else {
			species_name = "";
		}

		PRIDEInterface api = new PRIDEInterface();

		JSONObject jsonResult = api.getResults(species_name);

		_tbl_source = (JSONArray) jsonResult.get("results");
		_tbl_header.addAll(Arrays.asList("Accession", "Title", "Short Label", "Organism", "Publication", "Download"));
		_tbl_field.addAll(Arrays.asList("experiment_ac", "experiment_title", "experiment_short_title", "newt_name", "pubmed_id",
				"link_data_file"));

		_filename = "PATRIC_PRIDE";
	}
	else if (_tablesource.equalsIgnoreCase("Structure")) {
		if (request.getParameter("context_type") != null) {
			cType = request.getParameter("context_type");
		}
		if (request.getParameter("context_id") != null && !request.getParameter("context_id").equals("")) {
			cId = request.getParameter("context_id");
		}
		String tId = "";

		_filename = "Structure_Data";

		if (cType.equals("taxon")) {
			tId = cId;
		}
		else if (cType.equals("genome")) {
			//need to query ncbi_tax_id from DB
			DataApiHandler dataApi = new DataApiHandler();
			Genome genome = dataApi.getGenome(cId);
			tId = "" + genome.getTaxonId();
		}

		String strQueryTerm = "txid" + tId + "[Organism:exp]";
		EutilInterface eutil_api = new EutilInterface();
		JSONObject jsonResult = eutil_api.getResults("structure", strQueryTerm, "", "", 0, -1);

		_tbl_source = (JSONArray) jsonResult.get("results");
		_tbl_header.addAll(Arrays.asList("Accession", "Title", "EC No", "Class", "Experiment Method", "Resolution", "Date", "Organism",
				"LigCode"));
		_tbl_field.addAll(Arrays.asList("PdbAcc", "PdbDescr", "EC", "PdbClass", "ExpMethod", "Resolution", "PdbDepositDate", "Organism",
				"LigCode"));

		_filename = "PATRIC_Structure";

	}
	else if (_tablesource.equalsIgnoreCase("IntAct")) {
		if (request.getParameter("context_type") != null) {
			cType = request.getParameter("context_type");
		}
		if (request.getParameter("context_id") != null && !request.getParameter("context_id").equals("")) {
			cId = request.getParameter("context_id");
		}

		_filename = "IntAct_Data";

		String species_name = "";

		if (cType.equals("taxon")) {
			species_name = "species:" + cId;
		}
		else if (cType.equals("genome")) {
			DataApiHandler dataApi = new DataApiHandler();
			Genome genome = dataApi.getGenome(cId);
			species_name = "species:" + genome.getTaxonId();
		}

		PSICQUICInterface api = new PSICQUICInterface();

		String count = api.getCounts("intact", species_name);
		JSONObject jsonResult = api.getResults("intact", species_name, 0, Integer.parseInt(count));

		_tbl_source = (JSONArray) jsonResult.get("results");
		_tbl_header.addAll(Arrays.asList("Interaction Accession", "Label", "Interaction Type", "Count Participants", "Count Experiments",
				"Exp Name", "Method", "Organism", "Publication", "Participatns", "Experiments"));
		_tbl_field.addAll(Arrays.asList("interaction_ac", "label", "interaction_type", "count_participants", "count_exp_ref", "exp_name",
				"exp_method", "exp_org", "exp_pubmed", "participants", "experiments"));

		_filename = "PATRIC_IntAct";
	}

	ExcelHelper excel = new ExcelHelper("xssf", _tbl_header, _tbl_field, _tbl_source);
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

		response.getWriter().write(excel.writeToTextFile());
	}
%>