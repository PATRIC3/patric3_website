<%@ page import="edu.vt.vbi.patric.dao.DBDisease" %>
<%@ page import="edu.vt.vbi.patric.dao.ResultType" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="org.json.simple.*" %>
<%
	Map<String, String> key = new HashMap<String, String>();

	DBDisease conn_disease = new DBDisease();

	JSONObject json = new JSONObject();
	
	key.put("vfgId", request.getParameter("vfgId"));
	key.put("cId", request.getParameter("cId"));
	
	try {
		List<ResultType> items = conn_disease.getVDFBNaFeatureIdList(key);
		json.put("genes", items);
	}
	catch (NullPointerException nex) {
	}
	
	json.writeJSONString(out);
%>