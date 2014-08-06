<%@ page import="edu.vt.vbi.patric.dao.DBShared" 
%><%@ page import="edu.vt.vbi.patric.beans.DNAFeature" 
%><%@ page import="edu.vt.vbi.patric.dao.ResultType" 
%><%@ page import="edu.vt.vbi.patric.common.SolrInterface" 
%><%@ page import="org.json.simple.JSONObject" 
%><%@ page import="org.json.simple.JSONArray" 
%><%@ page import="java.util.List" 
%><%
String fId = request.getParameter("context_id");
int featureId = -1;

try {
	featureId = Integer.parseInt(fId);
} catch (Exception ex) {
	fId = null;
}

//JSONObject feature = new JSONObject();
DNAFeature feature = null;

if (fId!=null && !fId.equals("") && featureId > 0) 
{
	// getting feature info from Solr 
	SolrInterface solr = new SolrInterface();
	feature = solr.getPATRICFeature(fId);
	// end of Solr query
}

if (feature != null) {
	
	DBShared conn_shared = new DBShared();
	String tId = Integer.toString(feature.getNcbiTaxonId());
	
	List<ResultType> parents = conn_shared.getTaxonParentTree(tId);
	ResultType node = null;
	String flag = "";
	
	String tracks = "DNA,PATRICGenes,RefSeqGenes";
	
	int window_start = feature.getStart();
	if (window_start >= 1000) {
		window_start = window_start - 1000;
	}
	int window_end = feature.getEnd() + 1000;
	String gb_link = "GenomeBrowser?cType=feature&amp;cId="+fId+"&amp;loc="+window_start+".."+window_end+"&amp;tracks="+tracks;
	String crv_link = "CompareRegionViewer?cType=feature&amp;cId=" + feature.getId() +"&amp;tracks=&amp;regions=5&amp;window=10000&amp;loc=1..10000";
	%>
	<nav class="breadcrumbs" style="width=100%">
		<ul class="inline no-decoration">
		<%
		for (int i=parents.size()-1; i>=0; i--) {
			node = parents.get(i);
			if (node.get("rank").equalsIgnoreCase("superkingdom") ||
					node.get("rank").equalsIgnoreCase("phylum") ||
					node.get("rank").equalsIgnoreCase("class") ||
					node.get("rank").equalsIgnoreCase("order") ||
					node.get("rank").equalsIgnoreCase("family") ||
					node.get("rank").equalsIgnoreCase("genus") ) 
			{
				flag = "";
			} else {
				flag = "full";
			}
			%>
			<li class="<%=flag %>" style="<%=flag.equals("")?"":"display:none" %>">
				<a href="Taxon?cType=taxon&amp;cId=<%=node.get("ncbi_tax_id") %>" title="taxonomy rank:<%=node.get("rank")%>"><%=node.get("name")%></a>
			</li>
			<%
		}
		%>
			<li><a title="genome" href="Genome?cType=genome&amp;cId=<%=feature.getGenomeInfoId() %>"><%=feature.getGenomeName() %></a></li>
			<li id="feature_breadcrumb">
				<% if (feature.getAnnotation().equals("PATRIC")) { %>
					
					<%=feature.getLocusTag() %>
					<%=feature.hasRefseqLocusTag()?" <span class='pipe'>|</span> " + feature.getRefseqLocusTag():"" %>
					<%=feature.hasGene()?" <span class='pipe'>|</span> " + feature.getGene():"" %>
				<% } else { %>
					<%=feature.hasLocusTag()?feature.getLocusTag():"" %>
				<% } %>
				
				<%=feature.hasProduct()?" <span class='pipe'>|</span> " + feature.getProduct():"" %>
				<img id="breadcrumb_btn" alt="expand or shrink bread crumb" src="/patric/images/spacer.gif" onclick="toggleBreadcrumb()" class="toggleButton toggleButtonHide" />
			</li>
		</ul>
	</nav>
	
	<div class="clear"></div>
	<article class="tabs">
		<ul class="tab-headers no-decoration">
			<li id="tabs_featureoverview" class="first"><a href="Feature?cType=feature&amp;cId=<%=fId %>"><span>Overview</span></a></li>
			<li id="tabs_genomebrowser"><a href="<%=gb_link%>"><span>Genome Browser</span></a></li>
			
		 	<% if (feature.getAnnotation().equals("PATRIC")) { %>
			<li id="tabs_crviewer"><a href="<%=crv_link%>"><span>Compare Region Viewer</span></a></li>
			<li id="tabs_pathways"><a href="PathwayTable?cType=feature&amp;cId=<%=feature.getId() %>"><span>Pathways</span></a></li>
			<li id="tabs_expression"><a href="TranscriptomicsGeneExp?cType=feature&amp;cId=<%=feature.getId() %>&amp;sampleId=&amp;colId=&amp;log_ratio=&amp;zscore=" title=""><span>Transcriptomics</span></a></li>
			<%--<li id="tabs_proteomics"><a href="ProteomicsList?cType=feature&amp;cId=<%=feature.getId() %>&amp;kw="><span>Proteomics</span></a></li> --%>
			<li id="tabs_interaction"><a href="HPITool?dm=tab&amp;cType=feature&amp;cId=<%=fId %>&amp;hpi=false&amp;bm=" title=""><span>Interactions</span></a></li>
			<li id="tabs_correlated"><a href="TranscriptomicsGeneCorrelated?cType=feature&amp;cId=<%=feature.getId() %>" title=""><span>Correlated Genes</span></a></li>
			<% } %>
			
			<li id="tabs_literature"><a href="Literature?cType=feature&amp;cId=<%=fId %>&amp;time=a&amp;kw=none"><span>Literature</span></a></li>
		</ul>
	</article>
<% } else { %>
	&nbsp;
<% } %>
