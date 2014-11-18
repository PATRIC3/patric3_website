<%@ page import="java.util.*" %>
<%@ page import="edu.vt.vbi.patric.dao.DBPathways" %>
<%@ page import="edu.vt.vbi.patric.dao.DBSummary" %>
<%@ page import="edu.vt.vbi.patric.dao.ResultType" %>
<%@ page import="org.json.simple.*" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="edu.vt.vbi.patric.beans.Genome" %>
<%@ page import="edu.vt.vbi.patric.common.SolrCore" %>
<%@ page import="edu.vt.vbi.patric.common.SolrInterface" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.apache.solr.client.solrj.SolrQuery" %>
<%@ page import="org.apache.solr.client.solrj.SolrRequest" %>
<%@ page import="org.apache.solr.client.solrj.SolrServerException" %>
<%@ page import="org.apache.solr.client.solrj.response.FacetField" %>
<%@ page import="org.apache.solr.client.solrj.response.QueryResponse" %>
<%@ page import="org.apache.solr.common.SolrDocument" %>
<%@ page import="org.apache.solr.common.SolrDocumentList" %>
<%@ page import="org.apache.solr.common.util.SimpleOrderedMap" %>
<%@ page import="java.net.MalformedURLException" %>
<%
// TODO: move this business logic into portlet class

    Logger LOGGER = LoggerFactory.getLogger(DBPathways.class);

    SolrInterface solr = new SolrInterface();

	String genomeId, taxonId, algorithm, map;
	
	genomeId = request.getParameter("genomeId");
	taxonId = request.getParameter("taxonId");
	algorithm = request.getParameter("algorithm");
	map = request.getParameter("map");

	JSONObject json = new JSONObject();

	try {
		// items = conn_pathways.getHeatMapData(key, 0, -1);

		SolrQuery query = new SolrQuery("pathway_id:" + map);

		if (algorithm != null && !algorithm.equals("")) {
           query.addFilterQuery("annotation:" + algorithm);
        }

        if (taxonId != null && !taxonId.equals("")) {
             query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + taxonId + " AND genome_status:(complete OR wgs)"));
        }
        if (genomeId != null && !genomeId.equals("")) {
            query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "genome_id:" + genomeId + " AND genome_status:(complete OR wgs)"));
        }
        query.setRows(100000).setFacet(true);
        query.add("json.facet","{stat:{field:{field:genome_ec,limit:-1,facet:{gene_count:\"unique(feature_id)\"}}}}");

        QueryResponse qr = solr.getSolrServer(SolrCore.PATHWAY).query(query);
        List<SimpleOrderedMap> buckets = (List) ((SimpleOrderedMap) ((SimpleOrderedMap) qr.getResponse().get("facets")).get("stat")).get("buckets");

        Map<String, Integer> mapStat = new HashMap<String, Integer>();
        for (SimpleOrderedMap value: buckets) {
            if (Integer.parseInt(value.get("gene_count").toString()) > 0) {
                mapStat.put(value.get("val").toString(), (Integer) value.get("gene_count"));
            }
        }

        JSONArray items = new JSONArray();
        for (SolrDocument doc: qr.getResults()) {
            JSONObject item = new JSONObject();
            item.put("genome_id", doc.get("genome_id"));
            item.put("algorithm", doc.get("annotation"));
            item.put("ec_number", doc.get("ec_number"));
            item.put("ec_name", doc.get("ec_description"));
            Integer count = mapStat.get(doc.get("genome_id") + "_" + doc.get("ec_number"));
            item.put("gene_count", String.format("%02x", count)); // 2-digit hex string

            items.add(item);
        }

		json.put("data", items);
	}
	catch (MalformedURLException ex) {
        LOGGER.error(ex.getMessage(), ex);
    }
    catch (SolrServerException e) {
        LOGGER.error(e.getMessage(), e);
    }

	try {
		SolrQuery query = new SolrQuery("*:*");

		if (taxonId != null && !taxonId.equals("")) {
		     query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + taxonId + " AND genome_status:(complete OR wgs)"));
		}
		if (genomeId != null && !genomeId.equals("")) {
            query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "genome_id:" + genomeId + " AND genome_status:(complete OR wgs)"));
		}
		if (algorithm != null && !algorithm.equals("")) {
		    if (algorithm.equals("PATRIC")) {
		        query.addFilterQuery("patric_cds:[1 TO *]");
		    }
		    else if (algorithm.equals("RefSeq")) {
		        query.addFilterQuery("refseq_cds:[1 TO *]");
		    }
		    else if (algorithm.equals("BRC1")) {
		        query.addFilterQuery("brc1_cds:[1 TO *]");
		    }
		}
        query.setFields("genome_id,genome_name").setRows(10000);

        QueryResponse qr = solr.getSolrServer(SolrCore.GENOME).query(query);
        List<Genome> genomes = qr.getBeans(Genome.class);

        JSONArray items = new JSONArray();
        for (Genome genome: genomes) {
            JSONObject item = new JSONObject();
            item.put("genome_id", genome.getId());
            item.put("genome_name", genome.getGenomeName());

            items.add(item);
        }

		json.put("genomes", items);
		
	}
	catch (MalformedURLException ex) {
        LOGGER.error(ex.getMessage(), ex);
    }
    catch (SolrServerException e) {
        LOGGER.error(e.getMessage(), e);
    }

	json.writeJSONString(out);
%>