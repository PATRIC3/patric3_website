<%@ page import="java.util.*" 
%><%@ page import="edu.vt.vbi.patric.beans.DNAFeature" 
%><%@ page import="edu.vt.vbi.patric.common.SiteHelper" 
%><%@ page import="edu.vt.vbi.patric.dao.DBShared" 
%><%@ page import="edu.vt.vbi.patric.dao.DBSummary" 
%><%@ page import="edu.vt.vbi.patric.dao.ResultType" 
%><%@ page import="edu.vt.vbi.patric.common.SolrInterface" 
%><%@ page import="edu.vt.vbi.patric.common.SolrCore" 
%><%@ page import="org.json.simple.JSONObject" 
%><%@ page import="org.json.simple.JSONArray" 
%><%

String fId = request.getParameter("context_id");
ResultType refseqInfo = null;
String refseqLink = null;
String refseqLocusTag = null;
DNAFeature feature = null;
JSONArray relatedFeatures = new JSONArray();
SolrInterface solr = new SolrInterface();

if (fId!=null) {	
	// getting feature info from Solr 	
	feature = solr.getPATRICFeature(fId);
}

if (feature != null) {
	
	// get related annotations
	JSONArray sortParam = new JSONArray();
	JSONObject sort1 = new JSONObject();
	sort1.put("property", "annotation_sort");
	sort1.put("direction", "asc");
	sortParam.add(sort1);
	
	HashMap<String, String> opt = new HashMap<String, String>();
	opt.put("sort", sortParam.toJSONString());
	
	relatedFeatures = solr.searchSolrRecords("pos_group:\"" + feature.getPosGroup() + "\"", opt);
	
	//TODO: need to migrate to solr query
	DBSummary conn_summary = new DBSummary();
	
	String uniprot_link = "";
	String uniprot_accession = "";
	ArrayList<ResultType> uniprot = null;
	
	if (feature.getAnnotation().equals("PATRIC")) {
		// TODO: cannot replace to solr since solr does not have entire uniprot IDs
		uniprot = conn_summary.getUniprotAccession(Long.toString(feature.getId()));
		
		if (uniprot != null && uniprot.size() > 0) {
			uniprot_accession = uniprot.get(0).get("uniprotkb_accession");
		}
	}
	
	if (feature.getAnnotation().equals("PATRIC")) {
		refseqInfo = conn_summary.getRefSeqInfo("PATRIC", Long.toString(feature.getId()));

		if (refseqInfo.get("gene_id")!=null && refseqInfo.get("gene_id").equals("") == false) {
			refseqLink = SiteHelper.getExternalLinks("ncbi_gene").replace("&","&amp;")+refseqInfo.get("gene_id");
		}
		refseqLocusTag = feature.getRefseqLocusTag();
	} 
	else if (feature.getAnnotation().equals("RefSeq")) {
		refseqLocusTag = feature.getLocusTag();
	}
%>
	<% if (feature.getAnnotation().equals("RefSeq")) { %>
	<div class="close2x" id="note_refseq_only_feature"><b>NOTE:</b> There is no corresponding PATRIC feature. Comparative tools are not available at this time.</div>
	<% } %>
	<table class="basic stripe far2x left" style="width:600px">
	<tbody>
		<tr>
			<th scope="row" style="width:20%">Gene ID</th>
			<td>
				<% if (feature.getAnnotation().equals("PATRIC") && feature.hasLocusTag()) { %>
					<span><b>PATRIC</b></span>: 
					<span><%=feature.getLocusTag() %> </span>
					&nbsp;&nbsp;&nbsp;&nbsp;
				<% } %>
				
				<% if (refseqLocusTag != null) { %>
					<span><b>RefSeq</b></span>: 
					<% if (refseqLink != null) { %>
						<a href="<%=refseqLink %> " target="_blank"><%=refseqLocusTag%></a>
					<% } else { %>
						<%=refseqLocusTag%>
					<% } %>
					&nbsp;&nbsp;
				<% } %>
				&nbsp;
			</td>
		</tr>
		<tr>
			<th scope="row">Protein ID</th>
			<td>
				<% if (feature.hasRefseqProteinId()) { %>
					<span><b>RefSeq</b></span>:
					<% if (refseqInfo != null) { %>
						<span><a href="<%=SiteHelper.getExternalLinks("ncbi_protein")+refseqInfo.get("gi_number")%>" target="_blank"><%=feature.getRefseqProteinId() %></a></span>
					<% } else { %>
						<span><%=feature.getRefseqProteinId() %></span>
					<% } %>
					&nbsp;&nbsp;&nbsp;&nbsp;
				<% } %>
				<% if (uniprot != null && uniprot.size() > 0) { %>
				<span><b>UnitProt</b></span>: 
				<span><a href="<%=SiteHelper.getExternalLinks("UniProtKB-Accession") %><%=uniprot_accession %>" target="_blank"><%=uniprot_accession %></a></span>
				&nbsp;&nbsp;
				<span> <a href="#" onclick="toggleLayer('uniprot_detail');return false;"><%=uniprot.size() %> IDs are mapped</a></span>
				<div id="uniprot_detail" class="table-container" style="display:none">
					<table class="basic">
					<% for (int u=0; u<uniprot.size(); u++) { %>
					<tr>
						<th scope="row" style="width:20%"><%=uniprot.get(u).get("id_type") %></th>
						<td><%
							uniprot_link = SiteHelper.getExternalLinks(uniprot.get(u).get("id_type").trim()).replace("&","&amp;");
							if (uniprot_link != "" && uniprot.get(u).get("id_type").matches("HOGENOM|OMA|ProtClustDB|eggNOG")) {
								%><a href="<%=uniprot_link%><%=uniprot_accession %>" target="_blank"><%=uniprot.get(u).get("id") %></a><%
							} else if (uniprot_link != "") {
								%><a href="<%=uniprot_link%><%=uniprot.get(u).get("id") %>" target="_blank"><%=uniprot.get(u).get("id") %></a><%
							} else {
								%><%=uniprot.get(u).get("id") %><%
							}
						%></td>
					</tr>
					<% } %>
					</table>
				</div>
				<% } %>
				&nbsp;
			</td>
		</tr>
		<%
		// check Virulence factor
		solr.setCurrentInstance(SolrCore.SPECIALTY_GENE);
		ResultType key = new ResultType();
		key.put("keyword", "source: PATRIC_VF AND (locus_tag: " + feature.getLocusTag() + (feature.hasRefseqLocusTag()?" OR locus_tag: " + feature.getRefseqLocusTag() + ")": ")"));
	
		JSONObject res = solr.getData(key, null, null, 0, 1, false, false, false);

		int numFound = Integer.parseInt(((JSONObject)res.get("response")).get("numFound").toString());

		if (numFound > 0) {
			JSONArray specialtyGeneProperties = (JSONArray)((JSONObject)res.get("response")).get("docs");
			JSONObject sp = (JSONObject) specialtyGeneProperties.get(0);
			%><tr><td colspan="2"><span class="label-box-orange">VF</span> This gene is identified as a virulence factor by PATRIC based on literature curation. <a href="SpecialtyGeneEvidence?source=<%=sp.get("source") %>&sourceId=<%=sp.get("source_id") %>">See details</a></td></tr><%
		}
		%>
	</tbody>
	</table>

	<div id="feature_box" class="far2x right">
		<div id="gene_symbol"><%=feature.hasGene()?feature.getGene():"" %></div>
		<% if (feature.getStrand().equals("+")) { %>
		<img id="strand" alt="forward strand" src="/patric/images/forward.png"/>
		<% } else { %>
		<img id="strand" alt="reverse strand" src="/patric/images/reverse.png"/>
		<% } %>
		<div id="feature_type"><%=feature.getFeatureType() %></div>
	</div>

	<div class="clear"></div>

	<table class="basic stripe far2x">
	<thead>
		<tr>
			<th scope="col">Annotation</th>
			<th scope="col">Locus Tag</th>
			<th scope="col">Start</th>
			<th scope="col">End</th>
			<th scope="col">NT Length</th>
			<th scope="col">AA Length</th>
			<th scope="col">Product</th>
		</tr>
	</thead>
	<tbody>
	<% for (Object obj: relatedFeatures) { JSONObject an = (JSONObject) obj; %>
		<tr>
			<td><%=an.get("annotation") %></td>
			<td><%=(an.get("locus_tag")!=null)?an.get("locus_tag"):"-" %></td>
			<td class="right-align-text"><%=an.get("start_max") %></td>
			<td class="right-align-text"><%=an.get("end_min") %></td>
			<td class="right-align-text"><%=an.get("na_length") %></td>
			<td class="right-align-text"><%=(an.get("aa_length")!=null)?an.get("aa_length"):"-" %></td>
			<td><% if (an.get("product")!=null) { %>
					<%=an.get("product")%>
				<% } else if (an.get("feature_type").equals("CDS") == false) { %>
					(feature type: <%=an.get("feature_type") %>)
				<% } else { %>
					-
				<% } %>
			</td>
		</tr>
	<% } %>
	</tbody>
	</table>
	
<% } %>

<script type="text/javascript">
//<![CDATA[
Ext.onReady(function () {
	if (Ext.get("tabs_featureoverview")!=null) {
		Ext.get("tabs_featureoverview").addCls("sel");
	}
});
//]]>
</script>