<%@ page session="true" 
%><%@ page import="edu.vt.vbi.patric.common.SolrInterface"
%><%@ page import="edu.vt.vbi.patric.common.SolrCore"
%><%@ page import="org.json.simple.JSONArray" 
%><%@ page import="org.json.simple.JSONObject" 
%><%@ page import="java.io.OutputStream" 
%><%

	SolrInterface solr = new SolrInterface();
	String keyword = request.getParameter("keyword");
	String spellcheck = request.getParameter("spellcheck");
	JSONObject result = new JSONObject();
	
	if(Boolean.parseBoolean(spellcheck)){
		JSONObject a = solr.getSpellCheckerResult(keyword);
		result.put("suggestion", a.get("suggestion"));
	}
	
	JSONArray data = new JSONArray();
	solr.setCurrentInstance(SolrCore.FEATURE);
	JSONObject obj = solr.getSummaryforGlobalSearch(keyword);
	data.add(obj);
	solr.setCurrentInstance(SolrCore.GENOME);
	obj = solr.getSummaryforGlobalSearch(keyword);
	data.add(obj);
	solr.setCurrentInstance(SolrCore.TAXONOMY);
	obj = solr.getSummaryforGlobalSearch(keyword);
	data.add(obj);
	solr.setCurrentInstance(SolrCore.TRANSCRIPTOMICS_EXPERIMENT);
	obj = solr.getSummaryforGlobalSearch(keyword);
	data.add(obj);
	
	result.put("data", data);
	result.writeJSONString(out);
%>
