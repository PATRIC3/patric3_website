<%@ page import="java.util.*"%><%@ page
	import="edu.vt.vbi.patric.common.ExcelHelper"%><%@ page
	import="edu.vt.vbi.patric.dao.ResultType"%><%@ page
	import="java.io.OutputStream"%><%

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

	if (_tablesource.equalsIgnoreCase("MapFeatureTable_Cell")) {
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