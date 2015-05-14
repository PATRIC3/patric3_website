<%@ page
	import="edu.vt.vbi.patric.common.ExcelHelper" %><%@ page
	import="edu.vt.vbi.patric.dao.ResultType" %><%@ page
	import="java.io.BufferedReader" %><%@ page
	import="java.io.OutputStream" %><%@ page
	import="java.io.StringReader" %><%@ page
	import="java.util.ArrayList" %><%@ page
	import="java.util.Arrays" %><%@ page 
	import="java.util.List" %>
<%
	String data = request.getParameter("_data");
	List<String> _tbl_header = new ArrayList<String>();
	List<String> _tbl_field = new ArrayList<String>();

	if (request.getParameter("_tablesource") != null && request.getParameter("_tablesource").equals("Table_Cell")) {

		String _fileformat = request.getParameter("_fileformat");
		String _filename = "MapTable_Cell";
		List<ResultType> _tbl_source = new ArrayList<ResultType>();

		if (_fileformat.equalsIgnoreCase("xls") || _fileformat.equalsIgnoreCase("xlsx")) {
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
			response.setHeader("Content-Disposition", "attachment; filename=\"" + _filename + "." + _fileformat + "\"");

			OutputStream outs = response.getOutputStream();
			excel.writeSpreadsheettoBrowser(outs);
		}
		else if (_fileformat.equalsIgnoreCase("txt")) {
			response.setContentType("application/octetstream");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + _filename + "." + _fileformat + "\"");

			response.getWriter().write(data);
		}
	}
%>
