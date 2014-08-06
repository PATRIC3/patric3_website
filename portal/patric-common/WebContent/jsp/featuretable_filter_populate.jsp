<%@ page import="java.util.*"%>
<%@ page import="edu.vt.vbi.patric.common.SolrInterface"%>
<%@ page import="edu.vt.vbi.patric.common.SolrCore"%>
<%@ page import="edu.vt.vbi.patric.dao.ResultType"%>
<%@ page import="org.json.simple.JSONObject"%>
<%@ page import="org.json.simple.JSONArray"%>
<%@ page import="org.json.simple.parser.JSONParser"%>
<%
	String keyword = request.getParameter("keyword");
	String facet = request.getParameter("facet");
	String taxonId = request.getParameter("taxonId");

	SolrInterface solr = new SolrInterface();
	solr.setCurrentInstance(SolrCore.FEATURE);

	ResultType key = new ResultType();
	key.put("facet", facet);
	key.put("keyword", keyword);

	if (taxonId != null && taxonId.equals("") == false) {
		key.put("join", SolrCore.GENOME.getSolrCoreJoin("gid", "gid", "taxon_lineage_ids:" + taxonId));
	}

	JSONObject object = solr.getData(key, null, facet, 0, 0, true, false, false);
	object.writeJSONString(out);
%>