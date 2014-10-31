<%@ page import="java.util.*" %>
<%@ page import="edu.vt.vbi.patric.dao.DBPathways" %>
<%@ page import="edu.vt.vbi.patric.dao.ResultType" %>
<%@ page import="org.json.simple.JSONObject" %>
<%@ page import="org.json.simple.JSONArray" %>
<%@ page import="org.json.simple.parser.JSONParser" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
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
    Logger LOGGER = LoggerFactory.getLogger(DBPathways.class);

	Map<String, String> key = new HashMap<String, String>();
	DBPathways conn_pathways = new DBPathways();
	SolrInterface solr = new SolrInterface();
	JSONObject ret = new JSONObject();
	JSONParser a = new JSONParser();
	JSONObject val = (JSONObject) a.parse(request.getParameter("val").toString());
	
	List<ResultType> items = null;
	
	String need = val.get("need").toString();
	String genomeId = "", taxonId = "", map = "";
	
	if (need.equals("all")){
		if(val.get("genomeId") != null) {
			key.put("genomeId", val.get("genomeId").toString());
			genomeId = val.get("genomeId").toString();
		}
		if(val.get("taxonId") != null) {
			key.put("taxonId", val.get("taxonId").toString());
			taxonId = val.get("taxonId").toString();
		}
		key.put("map", val.get("map").toString());
		map = val.get("map").toString();

        // getting coordinates
		try {
			// items = conn_pathways.getCompPathwayCoordinates(key,  0, -1);

            Set<String> ecNumbers = new HashSet<String>();

            // step1. genome_count, feature_count
            // pathway/select?q=pathway_id:00053+AND+annotation:PATRIC&fq={!join+from=genome_id+to=genome_id+fromIndex=genome}taxon_lineage_ids:83332+AND+genome_status:(complete+OR+wgs)
            // &rows=0&facet=true&facet.limit=-1&facet.field=ec_number&facet.stat=genome_count:unique(genome_id)&facet.stat=gene_count:unique(feature_id)

            SolrQuery query = new SolrQuery("pathway_id:" + map);
            if (!taxonId.equals("")) {
                query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + taxonId + " AND genome_status:(complete OR wgs)"));
            }
            if (!genomeId.equals("")) {
                query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "genome_id:" + genomeId + " AND genome_status:(complete OR wgs)"));
            }
            query.setRows(0).setFacet(true).setFacetLimit(-1).addFacetField("ec_number");
            // TODO: change to Heliosearch JSON API later (with SolrHS 0.8)
            query.add("subfacet.ec_number.field", "annotation_sort");
            query.add("facet.stat", "genome_count:unique(genome_id)");
            query.add("facet.stat", "gene_count:unique(feature_id)");

            QueryResponse qr = solr.getSolrServer(SolrCore.PATHWAY).query(query);
            List<SimpleOrderedMap> buckets = (List) ((SimpleOrderedMap) ((SimpleOrderedMap) qr.getResponse().get("facets")).get("ec_number")).get("buckets");

            Map<String, SimpleOrderedMap> mapStat = new HashMap<String, SimpleOrderedMap>();
            for (SimpleOrderedMap value: buckets) {
                if (Integer.parseInt(value.get("gene_count").toString()) > 0) {
                    mapStat.put(value.get("val").toString(), value);
                    ecNumbers.add(value.get("val").toString());
                }
            }

            // step2. coordinates, occurrence
            // pathway_ref/select?q=pathway_id:00010+AND+map_type:enzyme%+AND+ec_number:("1.2.1.3"+OR+"1.1.1.1")&fl=ec_number,ec_description,map_location,occurrence

            query = new SolrQuery("pathway_id:" + map + " AND map_type:enzyme AND ec_number:(" + StringUtils.join(ecNumbers, " OR ") + ")");
            query.setFields("ec_number,ec_description,map_location,occurrence");
            query.setRows(100000);

            qr = solr.getSolrServer(SolrCore.PATHWAY_REF).query(query);
            SolrDocumentList sdl = qr.getResults();

            JSONArray listCoordinates = new JSONArray();
            for (SolrDocument doc: sdl) {
                String ecNumber = doc.get("ec_number").toString();
                SimpleOrderedMap stat = mapStat.get(ecNumber);

                List<SimpleOrderedMap> annotationBuckets = (List) ((SimpleOrderedMap) stat.get("annotation_sort")).get("buckets");

                for (SimpleOrderedMap annotationBucket: annotationBuckets) {
                    String annotation = annotationBucket.get("val").toString();

                    if (!annotationBucket.get("gene_count").toString().equals("0")) {
                        // LOGGER.debug("{}", annotationBucket);
                        List<String> locations = (List<String>) doc.get("map_location");
                        for (String location : locations) {

                            JSONObject coordinate = new JSONObject();
                            coordinate.put("algorithm", annotation);
                            coordinate.put("description", doc.get("ec_description"));
                            coordinate.put("ec_number", ecNumber);
                            coordinate.put("genome_count", stat.get("genome_count"));

                            String[] loc = location.split(",");
                            coordinate.put("x", loc[0]);
                            coordinate.put("y", loc[1]);

                            listCoordinates.add(coordinate);
                        }
                    }
                }
            }

			ret.put("genome_x_y", listCoordinates);
        }
		catch (MalformedURLException ex) {
		    LOGGER.error(ex.getMessage(), ex);
		}
		catch (SolrServerException e) {
        	LOGGER.error(e.getMessage(), e);
        }

        // get pathways
        try {
			// key.remove("map");
			// items = conn_pathways.getCompPathwayPathwayIds(key, 0, -1);

            SolrQuery query = new SolrQuery("*:*");
            if (!taxonId.equals("")) {
                query.addFilterQuery(SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + taxonId + " AND genome_status:(complete OR wgs)"));
            }
            if (!genomeId.equals("")) {
                query.addFilterQuery("genome_id:" + genomeId);
            }
            query.setFields("pathway_id,pathway_name,annotation").setRows(100000);

            QueryResponse qr = solr.getSolrServer(SolrCore.PATHWAY).query(query);
            SolrDocumentList sdl = qr.getResults();

            JSONArray listEnzymes = new JSONArray();
            Set<String> hash = new HashSet<String>();

            for (SolrDocument doc: sdl) {
                // TODO: need to improve this using solr
                String hashKey = doc.get("pathway_id").toString() + ":" + doc.get("annotation").toString();

                if (hash.contains(hashKey) == false) {

                    hash.add(hashKey);

                    JSONObject enzyme = new JSONObject();
                    enzyme.put("algorithm", doc.get("annotation"));
                    enzyme.put("map_id", doc.get("pathway_id"));
                    enzyme.put("map_name", doc.get("pathway_name"));

                    listEnzymes.add(enzyme);
                 }
            }

			ret.put("genome_pathway_x_y", listEnzymes);
        }
		catch (MalformedURLException ex) {
		    LOGGER.error(ex.getMessage(), ex);
		}
		catch (SolrServerException e) {
        	LOGGER.error(e.getMessage(), e);
        }

        try {
			// items = conn_pathways.getMapIdsInMap(val.get("map").toString());
			SolrQuery query = new SolrQuery("pathway_id:" + map + " AND map_type:path");
            query.setFields("ec_number,ec_description,map_location").setRows(10000);

            QueryResponse qr = solr.getSolrServer(SolrCore.PATHWAY_REF).query(query);
            SolrDocumentList sdl = qr.getResults();

            JSONArray listCoordinates = new JSONArray();
            for (SolrDocument doc: sdl) {
                List<String> locations = (List<String>) doc.get("map_location");
                for (String location : locations) {

                    JSONObject coordinate = new JSONObject();
                    coordinate.put("source_id", doc.get("ec_number"));

                    String[] loc = location.split(",");
                    coordinate.put("x", loc[0]);
                    coordinate.put("y", loc[1]);
                    coordinate.put("width", loc[2]);
                    coordinate.put("height", loc[3]);

                    listCoordinates.add(coordinate);
                }
            }

			ret.put("map_ids_in_map", listCoordinates);
        }
		catch (MalformedURLException ex) {
		    LOGGER.error(ex.getMessage(), ex);
		}
		catch (SolrServerException e) {
        	LOGGER.error(e.getMessage(), e);
        }

		// all coordinates
		try {
			// items = conn_pathways.getAllCoordinatesInMap(val.get("map").toString());
            SolrQuery query = new SolrQuery("pathway_id:" + map + " AND map_type:enzyme");
            query.setFields("ec_number,ec_description,map_location").setRows(10000);

            QueryResponse qr = solr.getSolrServer(SolrCore.PATHWAY_REF).query(query);
            SolrDocumentList sdl = qr.getResults();

            JSONArray listCoordinates = new JSONArray();
            for (SolrDocument doc: sdl) {
                List<String> locations = (List<String>) doc.get("map_location");
                for (String location : locations) {

                    JSONObject coordinate = new JSONObject();
                    coordinate.put("ec", doc.get("ec_number"));
                    coordinate.put("description", doc.get("ec_description"));

                    String[] loc = location.split(",");
                    coordinate.put("x", loc[0]);
                    coordinate.put("y", loc[1]);

                    listCoordinates.add(coordinate);
                }
            }

			ret.put("all_coordinates", listCoordinates);
		}
		catch (MalformedURLException ex) {
		    LOGGER.error(ex.getMessage(), ex);
		}
		catch (SolrServerException e) {
        	LOGGER.error(e.getMessage(), e);
        }
		
	}
	else {

		key.put("map", val.get("map").toString());
		key.put(need, val.get("value").toString());

		try {
			if(need.equals("ec_number")) {
				items = conn_pathways.getCompPathwayEcCoordinates(key, 0, -1);
			}
			else {
				items = conn_pathways.getCompPathwayFeatureCoordinates(key, 0, -1);
			}
			
			ret.put("coordinates", items);
		} catch (NullPointerException nex) {
		}		
	}

    ret.writeJSONString(out);
%>