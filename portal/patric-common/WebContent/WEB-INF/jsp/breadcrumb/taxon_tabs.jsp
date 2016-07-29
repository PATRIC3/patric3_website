<%@ page import="java.util.*"
%><%
String tId = request.getParameter("context_id");
int taxonId = Integer.parseInt(tId);

List<Map<String, Object>> lineage = (List<Map<String, Object>>) request.getAttribute("lineage");
boolean isBelowGenus = (Boolean) request.getAttribute("isBelowGenus");
String genomeFilter = (String) request.getAttribute("genomeFilter");

if (!lineage.isEmpty()) {

	String flag = "";
	boolean expandable = true;
	if (lineage.size() <= 6) {
		expandable = false;
	}
	%>
	<nav class="breadcrumbs left">
		<ul id="breadcrumbs" class="inline no-decoration">
		<%
		for (Map<String, Object> node: lineage) {

			if ((Integer) node.get("taxonId") == taxonId) {
				%>
				<li><%=node.get("name")%>
					<% if (expandable) { %>
					<img id="breadcrumb_btn" alt="expand or shrink bread crumb" src="/patric/images/spacer.gif" onclick="toggleBreadcrumb()" class="toggleButton toggleButtonHide" />
					<% } %>
				</li>
				<%
			} else {
			    String rank = node.get("rank").toString();

				if (!expandable || (rank.equalsIgnoreCase("superkingdom") ||
						rank.equalsIgnoreCase("phylum") ||
						rank.equalsIgnoreCase("class") ||
						rank.equalsIgnoreCase("order") ||
						rank.equalsIgnoreCase("family") ||
						rank.equalsIgnoreCase("genus") )) {
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
		}
		%>
		</ul>
	</nav>
<%--	<input type="checkbox" id="genome_filter_toggle" <%=(genomeFilter != null && genomeFilter.equals("reference_genome:*"))?"checked":""%> data-toggle="toggle" data-on="Reference Genome Only" data-off="All Genomes" />--%>
	<div id="utilitybox" class="smallest right no-underline-links">
		<a class="double-arrow-link" href="http://enews.patricbrc.org/patric-data-organization-overview/" target="_blank">Data Overview Tutorial</a>
		<br/><a class="double-arrow-link" href="Downloads?cType=taxon&amp;cId=<%=tId %>" target="_blank">Download genome data</a>
	</div>
	<div class="clear"></div>

	<article class="tabs">
		<ul class="tab-headers no-decoration"> 
			<li id="tabs_taxonoverview"><a href="Taxon?cType=taxon&amp;cId=<%=tId %>"><span>Overview</span></a></li>
			<li id="tabs_taxontree"><a href="TaxonomyTree?cType=taxon&amp;cId=<%=tId %>"><span>Taxonomy</span></a></li>
			<li id="tabs_phylogeny"><a href="Phylogeny?cType=taxon&amp;cId=<%=tId %>"><span>Phylogeny</span></a></li>
			<li id="tabs_genomelist"><a href="GenomeList?cType=taxon&amp;cId=<%=tId %>&amp;dataSource=&amp;displayMode=&amp;pk=&amp;kw="
				title="Genome Lists contain a summary list of all genomes associated with a given Phylum, Class, Order, Family, Genus or Species."><span>Genome List</span></a></li>
			<li id="tabs_featuretable"><a href="FeatureTable?cType=taxon&amp;cId=<%=tId %>&amp;featuretype=&amp;annotation=PATRIC&amp;filtertype="
				title="Feature Tables contain a summary list of all features (e.g., CDS, rRNA, tRNA, etc.) associated with a given Phylum, Class, Order, Family, Genus, Species or Genome."><span>Feature Table</span></a></li>
			<li id="tabs_specialtygenes"><a href="SpecialtyGeneList?cType=taxon&amp;cId=<%=tId %>&amp;kw="
				title=""><span>Specialty Genes</span></a></li>
			<% if (isBelowGenus) { %>
			<li id="tabs_proteinfamilysorter"><a href="FIGfam?cType=taxon&amp;cId=<%=tId %>&amp;dm=result&amp;bm=&pk=&famType=figfam<%=(!genomeFilter.equals(""))?"&genomeFilter=" + genomeFilter:""%>"><span>Protein Families</span></a></li>
			<% } %>
			<li id="tabs_pathways"><a href="CompPathwayTable?cType=taxon&amp;cId=<%=tId %>&amp;algorithm=PATRIC&amp;ec_number="><span>Pathways</span></a></li>
			<li id="tabs_explist"><a href="ExperimentList?cType=taxon&amp;cId=<%=tId %>&amp;kw=" 
				title=""><span>Transcriptomics</span></a></li>
			<%--<li id="tabs_proteomics"><a href="ProteomicsList?cType=taxon&amp;cId=<%=tId %>&amp;kw=" title=""><span>Proteomics</span></a></li>--%>
			<li id="tabs_interaction"><a href="HPITool?dm=tab&amp;cType=taxon&amp;cId=<%=tId %>&amp;hpi=false&amp;bm=" title=""><span>Interactions</span></a></li>
			<%--<li id="tabs_disease"><a href="DiseaseOverview?cType=taxon&amp;cId=<%=tId %>"><span>Diseases</span></a></li>--%>
			<li id="tabs_literature"><a href="Literature?cType=taxon&amp;cId=<%=tId %>&amp;time=a&amp;kw=none"><span>Literature</span></a></li>
		</ul>
	</article>
<% } %>
