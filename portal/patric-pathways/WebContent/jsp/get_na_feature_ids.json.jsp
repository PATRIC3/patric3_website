<%@ page import="java.util.*" %><%@ page
import="edu.vt.vbi.patric.dao.DBPathways" %><%@ page
import="edu.vt.vbi.patric.dao.ResultType" %><%@ page
import="org.json.simple.*" %><%@ page
import="org.slf4j.Logger" %><%@ page
import="org.slf4j.LoggerFactory" %><%@ page
import="edu.vt.vbi.patric.beans.Genome" %><%@ page
import="edu.vt.vbi.patric.common.SolrCore" %><%@ page
import="edu.vt.vbi.patric.common.SolrInterface" %><%@ page
import="org.apache.commons.lang.StringUtils" %><%@ page
import="org.apache.solr.client.solrj.SolrQuery" %><%@ page
import="org.apache.solr.client.solrj.SolrRequest" %><%@ page
import="org.apache.solr.client.solrj.SolrServerException" %><%@ page
import="org.apache.solr.client.solrj.response.FacetField" %><%@ page
import="org.apache.solr.client.solrj.response.QueryResponse" %><%@ page
import="org.apache.solr.common.SolrDocument" %><%@ page
import="org.apache.solr.common.SolrDocumentList" %><%@ page
import="org.apache.solr.common.util.SimpleOrderedMap" %><%@ page
import="java.net.MalformedURLException" %><%

    Logger LOGGER = LoggerFactory.getLogger(DBPathways.class);

	String cId = request.getParameter("cId");
	String cType = request.getParameter("cType");
	String map = request.getParameter("map");
	String algorithm = request.getParameter("algorithm");	
	String ec_number = request.getParameter("ec_number");
	String featureList = request.getParameter("featureList");

//	JSONObject json = new JSONObject();
	SolrInterface solr = new SolrInterface();
    JSONArray items = new JSONArray();

//	DBPathways conn_pathways = new DBPathways();
	
	try {
        SolrQuery query = new SolrQuery("*:*");

        if (cType != null && cType.equals("taxon")) {
            query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + cId + " AND genome_status:(complete OR wgs)"));
        }
        else if (cType != null && cType.equals("genome")) {
            query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "genome_id:(" + cId + ") AND genome_status:(complete OR wgs)"));
        }

        if (map != null && !map.equals("")) {
            query.addFilterQuery("pathway_id:(" + map.replaceAll(",", " OR ") + ")");
        }

        if (algorithm != null && !algorithm.equals("")) {
            query.addFilterQuery("annotation:(" + algorithm + ")");
        }

        if (ec_number != null && !ec_number.equals("")) {
            query.addFilterQuery("ec_number:(" + ec_number.replaceAll(",", " OR ") + ")");
        }

        if (featureList != null && !featureList.equals("")) {
            query.addFilterQuery("feature_id:(" + featureList.replaceAll(",", " OR ") + ")");
        }

        query.setRows(500000).setFields("feature_id");

        LOGGER.debug("{}", query.toString());

        QueryResponse qr = solr.getSolrServer(SolrCore.PATHWAY).query(query, SolrRequest.METHOD.POST);
        SolrDocumentList sdl = qr.getResults();

        for (SolrDocument doc: sdl) {
            items.add(doc.get("feature_id"));
        }

        //json.put("genes",items);
	}
	catch (MalformedURLException ex) {
        LOGGER.error(ex.getMessage(), ex);
    }
    catch (SolrServerException e) {
        LOGGER.error(e.getMessage(), e);
    }

	items.writeJSONString(out);
%>