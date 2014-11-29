<%@ page import="edu.vt.vbi.patric.beans.GenomeFeature"
%><%@ page import="java.util.*"
%><%

List<Map<String, Object>> lineage = (List<Map<String, Object>>) request.getAttribute("lineage");
boolean hasPATRICAnnotation = (Boolean) request.getAttribute("hasPATRICAnnotation");
GenomeFeature feature = (GenomeFeature) request.getAttribute("feature");

String fId = feature.getId();


if (feature != null) {

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
		for (Map<String, Object> node: lineage) {
			String rank = node.get("rank").toString();
			if (rank.equalsIgnoreCase("superkingdom") || rank.equalsIgnoreCase("phylum") || rank.equalsIgnoreCase("class") ||
					rank.equalsIgnoreCase("order") || rank.equalsIgnoreCase("family") || rank.equalsIgnoreCase("genus")) {
				flag = "";
			} else {
				flag = "full";
			}
			%>
			<li class="<%=flag %>" style="<%=flag.equals("")?"":"display:none" %>">
				<a href="Taxon?cType=taxon&amp;cId=<%=node.get("taxonId") %>" title="taxonomy rank:<%=node.get("rank")%>"><%=node.get("name")%></a>
			</li>
			<%
		}
		%>
			<li><a title="genome" href="Genome?cType=genome&amp;cId=<%=feature.getGenomeId() %>"><%=feature.getGenomeName() %></a></li>
			<li id="feature_breadcrumb">
				<% if (hasPATRICAnnotation) { %>
                    <%=feature.getSeedId() %>
                    <%=feature.hasAltLocusTag()?" <span class='pipe'>|</span> " + feature.getAltLocusTag():"" %>
                    <%=feature.hasRefseqLocusTag()?" <span class='pipe'>|</span> " + feature.getRefseqLocusTag():"" %>
                    <%=feature.hasGene()?" <span class='pipe'>|</span> " + feature.getGene():"" %>
				<% } else { %>
					<%=feature.hasAltLocusTag()?feature.getAltLocusTag():"" %>
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
			
		 	<% if (hasPATRICAnnotation) { %>
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
