<%@ page import="edu.vt.vbi.patric.dao.DBDisease" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%
	Map<String, String> key = new HashMap<String, String>();
	String type = request.getParameter("type");
	DBDisease conn_disease = new DBDisease();
	int distinct_items = 0;

	if (type.equals("0")) {
		key.put("cId", request.getParameter("cId"));

		distinct_items = conn_disease.getVFDBBreadCrumbCount(key);
	}
	else if (type.equals("1")) {
		key.put("vfgId", request.getParameter("vfgId"));
		key.put("cId", request.getParameter("cId"));

		distinct_items = conn_disease.getVFDBFeatureBreadCrumbCount(key);
	}
	else if (type.equals("ctd")) {
		key.put("name", request.getParameter("name"));

		distinct_items = conn_disease.getCTDBreadCrumbCount(key);
	}
	else if (type.equals("gad")) {
		key.put("name", request.getParameter("name"));

		distinct_items = conn_disease.getGADBreadCrumbCount(key);
	}
	else if (type.equals("gadgraph")) {
		key.put("name", request.getParameter("name"));

		distinct_items = conn_disease.getGADGraphBreadCrumbCount(key);
	}
	else if (type.equals("ctdgraph")) {
		key.put("name", request.getParameter("name"));

		distinct_items = conn_disease.getCTDGraphBreadCrumbCount(key);
	}

	out.println(distinct_items);
%>