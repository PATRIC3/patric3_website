<%@ page session="true" %><%@ page
	import="edu.vt.vbi.patric.common.DownloadHelper" %><%@ page
	import="edu.vt.vbi.patric.common.ExcelHelper" %><%@ page
	import="java.util.ArrayList" %><%@ page 
	import="java.util.List" %><%@ page 
	import="java.util.Map" %><%@ page 
	import="java.util.HashMap" %><%@ page 
	import="java.util.Arrays" %><%@ page 
	import="edu.vt.vbi.patric.dao.ResultType" %><%@ page
	import="edu.vt.vbi.patric.common.SolrInterface" %><%@ page
	import="org.json.simple.JSONArray" %><%@ page
	import="org.json.simple.JSONObject" %><%@ page
	import="java.io.OutputStream" %><%@ page
	import="java.io.BufferedReader" %><%@ page
	import="java.io.StringReader" %>
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
	else {

		String _fileformat = request.getParameter("_fileformat");
		String _filename = "Table_Gene";
		JSONArray sort = new JSONArray();
		Map<String, Object> condition = new HashMap<String, Object>();

		String sort_field = request.getParameter("sort");
		String sort_dir = request.getParameter("dir");

		if (sort_field != null && sort_dir != null) {
			JSONObject x = new JSONObject();
			x.put("property", sort_field);
			x.put("direction", sort_dir);
			sort.add(x);
			condition.put("sortParam", sort.toString());
		}
		condition.put("feature_ids", request.getParameter("featureIds"));
		SolrInterface solr = new SolrInterface();
		JSONObject object = solr.getFeaturesByID(condition);
		JSONArray _tbl_source = (JSONArray) object.get("results");

		_tbl_header.addAll(DownloadHelper.getHeaderForFeatures());
		_tbl_field.addAll(DownloadHelper.getFieldsForFeatures());

		ExcelHelper excel = new ExcelHelper("xssf", _tbl_header, _tbl_field, _tbl_source);
		excel.buildSpreadsheet();

		if (_fileformat.equalsIgnoreCase("xlsx")) {
			response.setContentType("application/octetstream");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + _filename + "." + _fileformat + "\"");

			OutputStream outs = response.getOutputStream();
			excel.writeSpreadsheettoBrowser(outs);
		}
		else {
			response.setContentType("application/octetstream");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + _filename + "." + _fileformat + "\"");

			response.getWriter().write(excel.writeToTextFile());
		}
	}
%>
