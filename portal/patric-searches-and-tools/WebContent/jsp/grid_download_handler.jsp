<%@ page import="edu.vt.vbi.patric.common.ExcelHelper"%><%@ page 
	import="edu.vt.vbi.patric.common.SolrInterface"%><%@ page
	import="edu.vt.vbi.patric.common.SolrCore"%><%@ page
	import="edu.vt.vbi.patric.dao.*"%><%@ page
	import="org.json.simple.JSONArray"%><%@ page
	import="org.json.simple.JSONObject"%><%@ page 
	import="java.util.*"%><%@ page
	import="java.io.OutputStream"%><%@ page
	import="org.slf4j.Logger"%><%@ page
    import="org.slf4j.LoggerFactory"%><%

	final Logger LOGGER = LoggerFactory.getLogger("GRID_DOWNLOAD_HANDLER.JSP");

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

	if (_tablesource.equalsIgnoreCase("Proteomics_Experiment")) {

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

//			solr.setCurrentInstance(SolrCore.PROTEOMICS_EXPERIMENT);
//			JSONObject object = solr.getData(key, sort, null, 0, -1, false, false, false);
//			JSONObject obj = (JSONObject) object.get("response");
//			_tbl_source = (JSONArray) obj.get("docs");
			_tbl_header.addAll(Arrays.asList("Sample Name", "Taxon Name", "Proteins", "Project Name", "Experiment Label", "Experiment Title",
					"Experiment Type", "Source", "Contact Name", "Institution"));

			_tbl_field.addAll(Arrays.asList("sample_name", "taxon_name", "proteins", "project_name", "experiment_label", "experiment_title",
					"experiment_type", "source", "contact_name", "institution"));
		}
		else if (request.getParameter("aT").equals("1")) {

			String solrId = "";
//			solr.setCurrentInstance(SolrCore.PROTEOMICS_PROTEIN);

			if (experiment_id != null && !experiment_id.equals("")) {
				keyword += " AND experiment_id:(" + experiment_id + ")";
			}

			key.put("keyword", keyword.trim());

//			JSONObject object_t = solr.getData(key, null, null, 0, -1, false, false, false);
//			JSONObject obj_t = (JSONObject) object_t.get("response");
//			_tbl_source = (JSONArray) obj_t.get("docs");

			_tbl_header.addAll(Arrays.asList("Experiment Title", "Experiment Label", "Source", "Genome Name", "Accession", "Locus Tag",
					"RefSeq Locus Tag", "Gene Symbol", "Description"));
			_tbl_field.addAll(Arrays.asList("experiment_title", "experiment_label", "source", "genome_name", "accession", "locus_tag",
					"refseq_locus_tag", "refseq_gene", "product"));
		}

		_filename = "Proteomics";
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
