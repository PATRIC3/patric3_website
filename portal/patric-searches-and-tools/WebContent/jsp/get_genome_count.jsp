<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="edu.vt.vbi.patric.dao.DBSummary" %>
<%@ page import="edu.vt.vbi.patric.dao.ResultType" %>
<%

	String taxonId = request.getParameter("taxonId");
	String data_source = request.getParameter("data_source");
	
	Map<String, String> key = new HashMap<String, String>();
	
	if(taxonId.equals(""))
		taxonId= "2";
		
	key.put("ncbi_taxon_id", taxonId);
	key.put("data_source", data_source);
	
	DBSummary conn_summary = new DBSummary();
	
	ResultType counts = conn_summary.getGenomeCount(key);
	String total_count = counts.get("cnt_all");
	
	out.println(total_count);
%>