<%@ page import="java.util.*"
%><%@ page import="edu.vt.vbi.patric.beans.Genome"
%><%
List<Map<String, Object>> lineage = (List<Map<String, Object>>) request.getAttribute("lineage");
boolean isBelowGenus = (Boolean) request.getAttribute("isBelowGenus");
boolean hasPATRICAnnotation = (Boolean) request.getAttribute("hasPATRICAnnotation");
boolean isPublicGenome = (Boolean) request.getAttribute("isPublicGenome");
Genome context = (Genome) request.getAttribute("context");

String gId = context.getId();
String flag = "";
%>
	<nav class="breadcrumbs left">
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
			<li><%=context.getGenomeName()%> <img id="breadcrumb_btn" alt="expand or shrink bread crumb" src="/patric/images/spacer.gif" onclick="toggleBreadcrumb()" class="toggleButton toggleButtonHide" /></li>
		</ul>
	</nav>
	<div id="utilitybox" class="smallest right no-underline-links">
		<% if (isPublicGenome) { %>
		<a class="double-arrow-link" href="ftp://ftp.patricbrc.org/patric2/patric3/genomes/<%=context.getId() %>/" target="_blank">Download genome data</a>
		<% } %>
	</div>
	<div class="clear"></div>

	<article class="tabs">
		<ul class="tab-headers no-decoration"> 	
			<li id="tabs_genomeoverview" class="first"><a href="Genome?cType=genome&amp;cId=<%=gId %>"><span>Overview</span></a></li>
			<li id="tabs_phylogeny"><a href="Phylogeny?cType=genome&amp;cId=<%=gId %>"><span>Phylogeny</span></a></li>
			<li id="tabs_genomebrowser"><a href="GenomeBrowser?cType=genome&amp;cId=<%=gId %>&amp;loc=0..10000&amp;tracks=DNA,PATRICGenes,RefSeqGenes"><span>Genome Browser</span></a></li>
			<li id="tabs_circosviewer"><a href="CircosGenomeViewer?cType=genome&amp;cId=<%=gId %>"><span>Circular Viewer</span></a></li>
			<li id="tabs_featuretable"><a href="FeatureTable?cType=genome&amp;cId=<%=gId %>&amp;featuretype=&amp;annotation=PATRIC&amp;filtertype=" 
				title="Feature Tables contain a summary list of all features (e.g., CDS, rRNA, tRNA, etc.) associated with a givenGenome."><span>Feature Table</span></a></li>
			<% if (hasPATRICAnnotation) { %>
			<li id="tabs_specialtygenes"><a href="SpecialtyGeneList?cType=genome&amp;cId=<%=gId %>&amp;kw="
				title=""><span>Specialty Genes</span></a></li>
			<li id="tabs_pathways"><a href="CompPathwayTable?cType=genome&amp;cId=<%=gId %>&amp;algorithm=PATRIC&amp;ec_number="><span>Pathways</span></a></li>
			<li id="tabs_proteinfamilysorter"><a href="FIGfam?cType=genome&amp;cId=<%=gId %>&amp;dm=result&amp;bm=&pk=&famType=figfam"><span>Protein Families</span></a></li>
				<% if (isPublicGenome) { %>
			<li id="tabs_explist"><a href="ExperimentList?cType=genome&amp;cId=<%=gId %>&amp;kw=" 
				title=""><span>Transcriptomics</span></a></li>
			<%--<li id="tabs_proteomics"><a href="ProteomicsList?cType=genome&amp;cId=<%=gId %>&amp;kw=" title=""><span>Proteomics</span></a></li>--%>
			<li id="tabs_interaction"><a href="HPITool?dm=tab&amp;cType=genome&amp;cId=<%=gId %>&amp;hpi=false&amp;bm=" title=""><span>Interactions</span></a></li>
			<%--<li id="tabs_disease"><a href="DiseaseOverview?cType=genome&amp;cId=<%=gId %>"><span>Diseases</span></a></li>--%>
				<% } %>
			<% } %>
			<li id="tabs_literature"><a href="Literature?cType=genome&amp;cId=<%=gId %>&amp;time=a&amp;kw=none"><span>Literature</span></a></li>
		</ul>
	</article>
