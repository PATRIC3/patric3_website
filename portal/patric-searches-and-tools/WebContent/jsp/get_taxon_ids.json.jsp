<%@ page import="java.util.*" 
%><%@ page import="edu.vt.vbi.patric.beans.Genome"
%><%@ page import="org.json.simple.*"
%><%@ page import="edu.vt.vbi.patric.common.SolrInterface"
%><%@ page import="edu.vt.vbi.patric.common.SolrCore"
%><%@ page import="org.apache.solr.client.solrj.*"
%><%@ page import="org.apache.solr.client.solrj.response.*"
%><%@ page import="java.net.MalformedURLException"
%><%@ page import="org.slf4j.Logger"
%><%@ page import="org.slf4j.LoggerFactory"
%><%
	final Logger LOGGER = LoggerFactory.getLogger("GET_TAXON_IDS.JSON.JSP");

	String cId = request.getParameter("cId");
	String cType = request.getParameter("cType");
	String genomeId = request.getParameter("genomeId");
	String algorithm = request.getParameter("algorithm");
	String status = request.getParameter("status");
	JSONObject json = new JSONObject();
/*
	JSONObject json = new JSONObject();
	DBSearch conn_search = new DBSearch();
	
	try {
		ArrayList<ResultType> items = conn_search.getTaxonIdList(cId, cType, genomeId, algorithm, status);
		json.put("ids",items);
	}
	catch (NullPointerException nex) {
	}
	
	out.println(json.toString());
*/
	try {
		SolrInterface solr = new SolrInterface();
		SolrQuery query = new SolrQuery("*:*");
		query.setRows(10000).addField("genome_id");

		if (cType.equals("taxon") && cId != null && !cId.equals("")) {
			query.addFilterQuery("taxon_lineage_ids:" + cId);
		}

		if (genomeId != null && !genomeId.equals("")) {
			query.addFilterQuery("genome_id:(" + genomeId.replaceAll(",", " OR ") + ")");
		}

		QueryResponse qr = solr.getSolrServer(SolrCore.GENOME).query(query);
		List<Genome> genomeList = qr.getBeans(Genome.class);

		JSONArray items = new JSONArray();
		for (Genome genome : genomeList) {
			JSONObject item = new JSONObject();
			item.put("id", genome.getId());

			items.add(item);
		}
		json.put("ids", items);
	}
	catch (MalformedURLException me) {
		LOGGER.error(me.getMessage(), me);
	}
	catch (SolrServerException e) {
		LOGGER.error(e.getMessage(), e);
	}

	json.writeJSONString(out);
%>