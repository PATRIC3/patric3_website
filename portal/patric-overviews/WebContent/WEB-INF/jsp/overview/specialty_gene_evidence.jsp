<%@ page import="java.util.ArrayList" 
%><%@ page import="java.util.Arrays"
%><%@ page import="java.util.HashMap"
%><%@ page import="edu.vt.vbi.patric.dao.ResultType" 
%><%@ page import="edu.vt.vbi.patric.beans.DNAFeature" 
%><%@ page import="edu.vt.vbi.patric.common.SolrInterface" 
%><%@ page import="edu.vt.vbi.patric.common.SolrCore" 
%><%@ page import="edu.vt.vbi.patric.common.SiteHelper" 
%><%@ page import="org.json.simple.JSONObject"
%><%@ page import="org.json.simple.JSONArray"
%><%
String source = request.getParameter("sp_source");
String sourceId = request.getParameter("sp_source_id"); //lmo0433, Rv3875


if (source != null && source.equals("") == false && sourceId != null && sourceId.equals("") == false) {

	SolrInterface solr = new SolrInterface();
	ResultType key = new ResultType();

	// get properties of gene
	solr.setCurrentInstance(SolrCore.SPECIALTY_GENE);
	key.put("keyword", "source: " + source + " AND source_id: " + sourceId);
	JSONObject res = solr.getData(key, null, null, 0, 1, false, false, false);
	JSONArray genes = (JSONArray)((JSONObject)res.get("response")).get("docs");
	JSONObject gene = (JSONObject) genes.get(0);
	ArrayList<String> properties = new ArrayList<String>(Arrays.asList("property", "source", "source_id", "gene_name", "organism", "product", "gi", "gene_id"));
	ArrayList<String> headers = new ArrayList<String>(Arrays.asList("Property", "Source", "Source ID", "Gene", "Organism", "Product", "GI Number", "Gene ID"));
	
	// get PATRIC feature
	solr.setCurrentInstance(SolrCore.SPECIALTY_GENE_MAPPING);
	key.put("keyword", "source: " + source + " AND source_id: " + sourceId + " AND evidence: Literature");
	res = solr.getData(key, null, null, 0, 1, false, false, false);
	JSONArray mappings = (JSONArray)((JSONObject)res.get("response")).get("docs");
	DNAFeature feature = null;
	if (mappings.size() > 0) {
		JSONObject mapping = (JSONObject) mappings.get(0);
		String na_feature_id = mapping.get("na_feature_id").toString();
		feature = solr.getPATRICFeature(na_feature_id);
	}
	else {
		solr.setCurrentInstance(SolrCore.FEATURE);
		key.put("keyword", "locus_tag: " + sourceId);
		res = solr.getData(key, null, null, 0, 1, false, false, false);
		JSONArray features = (JSONArray)((JSONObject)res.get("response")).get("docs");

		if (features.size() > 0) {
			JSONObject mapping = (JSONObject) features.get(0);
			String na_feature_id = mapping.get("na_feature_id").toString();
			feature = solr.getPATRICFeature(na_feature_id);
		}
	}
	
	// get Homolog count
	solr.setCurrentInstance(SolrCore.SPECIALTY_GENE_MAPPING);
	key.put("keyword", "source: " + source + " AND source_id: " + sourceId);
	res = solr.getData(key, null, null, 0, -1, false, false, false);
	int cntHomolog = Integer.parseInt(((JSONObject)res.get("response")).get("numFound").toString());

	// get list of evidence
	solr.setCurrentInstance(SolrCore.SPECIALTY_GENE_EVIDENCE);
	key.put("keyword", "source:"+ source + " AND source_id:" + sourceId);
		// sort by Organism, Host, Classification
	HashMap<String, String >sort = new HashMap<String, String>();
	sort.put("field", "specific_organism, specific_host, classification");
	sort.put("direction", "asc");
	
	res = solr.getData(key, sort, null, 0, -1, false, false, false);
	JSONArray specialtyGeneEvidence = (JSONArray)((JSONObject)res.get("response")).get("docs");
%>
	<h3 class="section-title normal-case close2x"><span class="wrap">Specialty Genes &gt; <%=gene.get("property") %> &gt; <%=gene.get("source") %> &gt; <%=gene.get("source_id") %> </span></h3>
	<table class="basic stripe far2x">
	<tbody>
	<% for (int i = 0; i < properties.size(); i++) {
		if (headers.get(i).equals("Source ID") && feature != null) { %>
		<tr>
			<th scope="row"><%=headers.get(i) %></th><td><a href="Feature?cType=feature&amp;cId=<%=feature.getId() %>"><%=gene.get(properties.get(i)) %></a></td>
		</tr>
		<% } else { %>
		<tr>
			<th scope="row"><%=headers.get(i) %></th><td><%=gene.get(properties.get(i)) %></td>
		</tr>
		<% } %>
	<% } %>
	<% if (feature != null) { %>
		<tr>
			<th scope="row">See this feature in </th>
			<td>
				<a href="GenomeBrowser?cType=feature&cId=<%=feature.getId() %>&loc=<%=(feature.getStart() - 1000) %>..<%=(feature.getEnd() + 1000) %>&tracks=PATRICGenes">Genome Browser</a>
				&nbsp; <a href="CompareRegionViewer?cType=feature&cId=<%=feature.getId() %>&tracks=&regions=5&window=10000&loc=1..10000">Compare Region Viewer</a>
				&nbsp; <a href="TranscriptomicsGeneExp?cType=feature&cId=<%=feature.getId() %>&sampleId=&colId=&log_ratio=&zscore=">Transcriptomics Data</a>
				&nbsp; <a href="TranscriptomicsGeneCorrelated?cType=feature&cId=<%=feature.getId() %>">Correlated genes</a>
			</td>
		</tr>
	<% } %>
	<% if (cntHomolog > 0) { %>
		<tr>
			<th scope="row">Homologs </th>
			<td><a href="SpecialtyGeneList?cType=taxon&cId=2&kw=source:<%=source %>+source_id:<%=sourceId%>"><%=cntHomolog %></a>
			</td>
		</tr>
	<% } %>
	</tbody>
	</table>

	<h3 class="section-title normal-case close2x"><span class="wrap">Evidence</span></h3>
	<table class="basic stripe far2x">
	<thead>
		<tr>
			<th scope="col">Organism</th>
			<th scope="col">Host</th>
			<th scope="col">Classification</th>
			<th scope="col">PubMed</th>
			<th scope="col">Assertion</th>
		</tr>
	</thead>
	<tbody>
		<% for (int i = 0; i < specialtyGeneEvidence.size(); i++) { 
			JSONObject obj = (JSONObject) specialtyGeneEvidence.get(i); 
		%>
		<tr <%=(i%2==0)?" class=\"alt\"":"" %>>
			<td><%=(obj.get("specific_organism")!=null)?obj.get("specific_organism"):"&nbsp;" %></td>
			<td><%=(obj.get("specific_host")!=null)?obj.get("specific_host"):"&nbsp;" %></td>
			<td><%=(obj.get("classification")!=null)?obj.get("classification"):"&nbsp;" %></td>
			<td><%=(obj.get("pmid")!=null)?"<a class=\"arrow-slate-e\" href=\"//www.ncbi.nlm.nih.gov/pubmed/" + obj.get("pmid") + "\" target=_blank>" + obj.get("pmid") + "</a>":"&nbsp;" %></td>
			<td><%=(obj.get("assertion")!=null)?obj.get("assertion"):"&nbsp;" %></td>
		</tr>
		<% } %>
	</tbody>
	</table>
<% } else { %>
	Missing parameters
<% } %>