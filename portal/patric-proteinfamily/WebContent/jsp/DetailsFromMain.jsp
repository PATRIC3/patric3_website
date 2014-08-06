<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Arrays"%>
<%@ page import="java.io.OutputStream"%>
<%@ page import="org.json.simple.JSONArray"%>
<%@ page import="edu.vt.vbi.patric.common.ExcelHelper"%>
<%@ page import="edu.vt.vbi.patric.proteinfamily.FIGfamData"%>
<%
	String genomeIds = request.getParameter("detailsGenomes");
	String figfamIds = request.getParameter("detailsFigfams");
	FIGfamData figger = new FIGfamData();

	String _fileformat = request.getParameter("detailsType");

	List<String> _tbl_header = new ArrayList<String>();
	List<String> _tbl_field = new ArrayList<String>();

	JSONArray _tbl_source = figger.getDetails(genomeIds, figfamIds);

	_tbl_header.addAll(Arrays.asList("Group Id", "Genome Name", "Accession", "Locus Tag", "Start", "End", "Length(NT)", "Strand",
			"Length(AA)", "Gene Symbol", "Product Description"));
	_tbl_field.addAll(Arrays.asList("figfam_id", "genome_name", "accession", "locus_tag", "start_max", "end_min", "na_length", "strand",
			"aa_length", "gene", "product"));

	ExcelHelper excel = new ExcelHelper("xssf", _tbl_header, _tbl_field, _tbl_source);
	excel.buildSpreadsheet();

	String _filename = "ProteinFamilyFeatures";

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
