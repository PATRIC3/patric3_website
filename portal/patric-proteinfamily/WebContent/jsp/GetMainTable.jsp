<%@ page import="edu.vt.vbi.patric.common.ExcelHelper"%>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="edu.vt.vbi.patric.dao.ResultType" %>
<%@ page import="java.io.OutputStream" %>
<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.io.StringReader" %><%

	String parameter = request.getParameter("OrthoFileName");
	String fileType = request.getParameter("OrthoFileType");
	String data = request.getParameter("data");

	if (fileType.equals("xls") || fileType.equals("xlsx")) {

		List<String> _tbl_header = new ArrayList<String>();
		List<String> _tbl_field = new ArrayList<String>();
		List<ResultType> _tbl_source = new ArrayList<ResultType>();

		BufferedReader br = new BufferedReader(new StringReader(data));
		String line;
		boolean isHeader = true;
		while ((line = br.readLine()) != null) {
			String[] tabs = line.split("\t");
			
			if (isHeader) {
				isHeader = false;
				_tbl_header.addAll(Arrays.asList(tabs));
				
				for (int i = 0; i < tabs.length; i++) {
					_tbl_field.add(tabs[i].replaceAll(" ", "_").toLowerCase());
				}
			}
			else {
				ResultType row = new ResultType();
				for (int i = 0; i < tabs.length; i++ ) {
					row.put(_tbl_field.get(i), tabs[i]);
				}
				_tbl_source.add(row);
			}
		}

		ExcelHelper excel = new ExcelHelper("xssf", _tbl_header, _tbl_field, _tbl_source);
		excel.buildSpreadsheet();

		response.setContentType("application/octetstream");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + parameter + "." + fileType + "\"");

		OutputStream outs = response.getOutputStream();
		excel.writeSpreadsheettoBrowser(outs);
	}
	else if (fileType.equals("txt")) {
		response.setContentType("application/octetstream");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + parameter + "." + fileType + "\"");
		
		response.getWriter().write(data);
	}
%>
