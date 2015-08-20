<%@ page import="edu.vt.vbi.patric.common.ExcelHelper"%><%@ page 
	import="java.util.Arrays" %><%@ page 
	import="java.util.ArrayList" %><%@ page 
	import="java.util.List" %><%@ page 
	import="edu.vt.vbi.patric.dao.ResultType" %><%@ page 
	import="java.io.OutputStream" %><%@ page
	import="java.io.BufferedReader" %><%@ page
	import="java.io.IOException" %><%@ page
	import="java.io.StringReader" %><%

	String parameter = null; // request.getParameter("GeneFileName");
	String fileType = null; // request.getParameter("GeneFileType");
	String data = null; // request.getParameter("data");

	StringBuilder stringBuilder = new StringBuilder();
	BufferedReader bufferedReader = null;
	try {
		bufferedReader = request.getReader();
		char[] charBuffer = new char[1024];
		int bytesRead = -1;
		while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
			stringBuilder.append(charBuffer, 0, bytesRead);
		}
	}
	catch (IOException ex) {
		throw ex;
	} finally {
		if (bufferedReader != null) {
			try {
				bufferedReader.close();
			} catch (IOException ex) {
				throw ex;
			}
		}
	}

	String[] params = stringBuilder.toString().split("&");

	for (String param : params) {
		if (param.contains("GeneFileName=")) {
			parameter = param.replace("GeneFileName=", "");
		}
		else if (param.contains("GeneFileType=")) {
			fileType = param.replace("GeneFileType=","");
		}
		else if (param.contains("data=")) {
			data = java.net.URLDecoder.decode(param.replace("data=",""), "UTF-8");
		}
	}

//	System.out.println("data: " + data);

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
		response.setHeader("Content-Disposition", "attachment; filename=\"" + parameter + ".xlsx\"");

		OutputStream outs = response.getOutputStream();
		excel.writeSpreadsheettoBrowser(outs);
	}
	else if (fileType.equals("txt")) {
		response.setContentType("application/octetstream");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + parameter + "." + fileType + "\"");

		response.getWriter().write(data);
	}
%>
