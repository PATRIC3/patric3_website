<%@ page import="java.util.*"%><%@ page
	import="edu.vt.vbi.patric.common.SQLHelper"%><%@ page
	import="edu.vt.vbi.patric.common.StringHelper"%><%@ page
	import="edu.vt.vbi.patric.common.ExcelHelper"%><%@ page
	import="edu.vt.vbi.patric.dao.ResultType"%><%@ page
	import="edu.vt.vbi.patric.dao.DBPathways"%><%@ page
	import="edu.vt.vbi.patric.dao.DBTranscriptomics"%><%@ page
	import="javax.portlet.PortletSession"%><%@ page
	import="java.io.OutputStream"%><%

	DBPathways conn_pathways = new DBPathways();
	String _filename = "";
	List<String> _tbl_header = new ArrayList<String>();
	List<String> _tbl_field = new ArrayList<String>();
	List<ResultType> _tbl_source = null;

	// getting common params
	String _fileformat = request.getParameter("fileformat");
	String _tablesource = request.getParameter("tablesource");

	Map<String, String> key = new HashMap<String, String>();
	Map<String, String> sort = null;
	String sort_field;
	String sort_dir;

	if (_tablesource == null || _fileformat == null) {
		_fileformat = null;
	}

	ExcelHelper excel = null;

	if (_tablesource.equalsIgnoreCase("PathwayTable")) {
		if (request.getParameter("cType") != null && !request.getParameter("cType").equals("")) {
			key.put("cType", request.getParameter("cType").toString());
		}
		if (request.getParameter("cId") != null && !request.getParameter("cId").equals("")) {
			key.put("cId", request.getParameter("cId").toString());
		}

		if (request.getParameter("ec_number") != null) {
			key.put("ec_number", request.getParameter("ec_number").toString());
		}

		if (request.getParameter("pathway_name") != null) {
			key.put("pathway_name", request.getParameter("pathway_name").toString());
		}

		if (request.getParameter("pathway_class") != null) {
			key.put("pathway_class", request.getParameter("pathway_class").toString());
		}

		if (request.getParameter("keyword") != null) {
			key.put("keyword", request.getParameter("keyword").toString());
		}

		_filename = "PathwayTable";
		_tbl_source = conn_pathways.getFeaturePathwayList(key, sort, 0, -1);
	}
	else if (_tablesource.equalsIgnoreCase("MapFeatureTable_Cell")) {
		// download heatmap data from pathway heatmap

		_filename = "MapFeatureTable_Cell";

		if (_fileformat.equalsIgnoreCase("xls")) {
			response.setContentType("application/vnd.ms-excel");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + _filename + "." + _fileformat + "\"");
		}
		else if (_fileformat.equalsIgnoreCase("txt")) {
			response.setContentType("application/octetstream");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + _filename + "." + _fileformat + "\"");
		}
		_fileformat = "";
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

		response.getWriter().write(excel.writeToTextFile());
	}
	else {
		// TODO: _tablesource == MapFeatureTable_Cell. downloads with .xls
		String output = request.getParameter("data");
		out.println(output);
	}
%>