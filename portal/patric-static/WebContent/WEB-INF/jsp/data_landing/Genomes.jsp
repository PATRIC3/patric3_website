<%@ page import="org.json.simple.JSONObject" %>
<%@ page import="org.json.simple.JSONArray" %>
<%
JSONObject jsonData = (JSONObject) request.getAttribute("jsonData");
String ftpUrl = (String) request.getAttribute("ftpUrl");
%>
<link rel="stylesheet" href="/patric/css/dlp.css"></link>
<script type="text/javascript" src="/patric/js/libs/d3.v3.min.js"></script>
<script type="text/javascript" src="/patric/js/libs/jquery.scrollTo-1.4.11-min.js"></script>
<script type="text/javascript" src="/patric/js/libs/waypoints.min.js"></script>

<script type="text/javascript" src="/patric/js/genome-charts-init.js"></script>
<script type="text/javascript" src="/patric/js/charts.js"></script>
<script type="text/javascript" src="/patric/js/landing-page-init.js"></script>

<div class="container tabs-above">
	<nav class="breadcrumbs">
		<ul id="breadcrumbs" class="inline no-decoration">
			<li class="" style=""><a href="Home" title="">Home</a></li>
			<li>Genomes</li>
		</ul>
	</nav>
</div>
<div id="sticky-anchor"></div>
<div class="sticky">
	<div class="container">
		<ul class="no-decoration data-tab-toolbar upper large letters-wide">
			<li class="left"><a class="active scrollTo" data-tab-target="tab1" style="background-image: url(/patric/images/data-tab-icon-data.png)" href="javascript:void(0)">Data</a></li>
			<li class="left"><a class="scrollTo" data-tab-target="tab2" style="background-image: url(/patric/images/data-tab-icon-tools.png)" href="javascript:void(0)">Tools</a></li>
			<li class="left"><a class="scrollTo" data-tab-target="tab3" style="background-image: url(/patric/images/data-tab-icon-process.png)" href="javascript:void(0)">Process</a></li>
			<li class="left"><a class="scrollTo" data-tab-target="tab4" style="background-image: url(/patric/images/data-tab-icon-download.png)" href="javascript:void(0)">Download</a></li>
		</ul>
	</div>
</div>
<div class="clear"></div>
<div class="container main-container">
	<section class="main">
		<!-- Section: Data -->
		<div class="data-tab" id="tab1">
			<% JSONObject jsonTopline = (JSONObject)jsonData.get("data"); %>
			<h3><%=jsonTopline.get("subtitle") %></h3>
			<% if(jsonTopline.get("content") != null) { %>
				<div class="large"><%=jsonTopline.get("content")%></div>
			<% } %>
			<div class="group">
			<!-- Genome status chart (stacked bar) -->
				<div class="col span-8 append-1">
					<div class="data-box" id="dlp-genomes-genomeStatus-header">
						<h3 class="ribbon-title"><!-- Genome Status Title --></h3>
						<div id="dlp-genomes-genomeStatus">
							<div class="chart"></div>
						</div><!-- / genomeStatus -->
					</div><!-- data-box -->
				</div><!-- / span-8 -->
				<!-- Number of genomes (Two-value line chart ) -->
				<div class="col span-13 append-1">
					<div class="data-box" id="dlp-genomes-numberGenomes-header">
						<h3 class="ribbon-title"><!-- Genome Number Title --></h3>
						<div id="dlp-genomes-numberGenomes">
							<div class="chart"></div>
						</div><!-- / numberGenomes -->
					</div><!-- / data-box -->
				</div><!-- / span-13 -->
				<!-- Three tabs with a vertical bar chart. -->
				<div class="col span-13 last">
					<div class="tabbed" id="dlp-genomes-chart">
					<!-- Tab controls are identified seperately. -->
						<ul class="tab-headers no-decoration inline no-underline-links">
							<li><a href="#dlp-genomes-chart-tab1">Tab Title 1</a></li>
							<li><a href="#dlp-genomes-chart-tab2">Tab Title 2</a></li>
						</ul>
					<!-- First tab contents -->
						<div class="data-box" id="dlp-genomes-chart-tab1">
							<%--<h4>Chart Title Here</h4> --%>
							<p class="desc">Very brief sentence</p>
								<div class="top-5">
									<div class="chart"></div>
								</div><!-- / top-5 -->
						</div><!-- / data-box -->
					<!-- Second tab contents -->
						<div class="data-box" id="dlp-genomes-chart-tab2">
							<%--<h4>Chart Title Here</h4>--%>
							<p class="desc">Very brief sentence</p>
							<div class="top-5">
								<div class="chart"></div>
							</div><!-- / top-5 -->
						</div><!-- / data-box -->
					<!-- Third tab contents -->
					</div><!-- / tabbed -->
				</div><!-- / span-13 -->
			</div><!-- / group -->
			<%
			JSONObject popular = (JSONObject)jsonData.get("popularGenomes");
			%>
			<div class="data-box popular-box tabbed hover-tabs no-underline-links">
				<h3 class="ribbon-title"><%=popular.get("popularTitle") %></h3>
				<div class="group">
					<%
						JSONArray popularItems = (JSONArray) popular.get("popularList");
						for(int popCt = 0; popCt < popularItems.size(); popCt++)
						{
							JSONObject pop = (JSONObject)popularItems.get(popCt);
							out.println("<div class='genome-data right half group' id='genome-tab" + (popCt+1) + "'>");
							// metadata
							JSONObject meta = (JSONObject)pop.get("metadata");
							//out.println("<div class='far2x'>");
							out.println("<table class='no-decoration basic far2x'>");
							out.println("<tr>");
								out.println("<th class='italic' width='25%' scope='row'>Genome Status: </th><td width='25%'>&nbsp;" + meta.get("genome_status") + "</td>");
								out.println("<th class='italic' width='25%' scope='row'>Isolation Country: </th><td width='25%'>&nbsp;" + meta.get("isolation_country") + "</td>");
							out.println("</tr>");
							
							out.println("<tr>");
								out.println("<th class='italic' scope='row'>Genomic Sequences: </th><td> ");
								if (meta.get("chromosomes") != null && !meta.get("chromosomes").toString().equals("0")) {
									if (meta.get("chromosomes").toString().equals("1")) {
										out.println(meta.get("chromosomes") + " Chromosome");
									} else {
										out.println(meta.get("chromosomes") + " Chromosomes");
									}
								}
								if (meta.get("plasmids") != null && !meta.get("plasmids").toString().equals("0")) {
									if (meta.get("plasmids").toString().equals("1")) {
										out.println("<br/>" + meta.get("plasmids") + " Plasmid");
									} else {
										out.println("<br/>" + meta.get("plasmids") + " Plasmids");
									}
								}
								if (meta.get("contigs") != null && !meta.get("contigs").toString().equals("0")) {
									if (meta.get("contigs").toString().equals("1")) {
										out.println("<br/>" + meta.get("contigs" + " Contig"));
									} else {
										out.println("<br/>" + meta.get("contigs") + " Contigs");
									}
								}
								out.println("</td>");
								out.println("<th class='italic' scope='row'>Host Name: </th><td>&nbsp;" + meta.get("host_name") + "</td>");
							out.println("</tr>");
							
							out.println("<tr>");
								out.println("<th class='italic' scope='row'>Genome Length: </th><td>&nbsp;" + meta.get("genome_length") + " bp</td>");
								out.println("<th class='italic' scope='row'>Disease: </th><td>&nbsp;" + meta.get("disease") + "</td>");
							out.println("</tr>");
							
							out.println("<tr>");
								out.println("<th class='italic' scope='row'>Completion Date: </th><td>&nbsp;" + meta.get("completion_date") + "</td>");
								out.println("<th class='italic' scope='row'>Collection Date: </th><td>&nbsp;" + meta.get("collection_date") + "</td>");
							out.println("</tr>");

							out.println("</table>");
							//out.println("</div>");
							// data type summary
							JSONArray popularData = (JSONArray)pop.get("popularData");
							out.println("	<div class='column'>");
							for (int left = 0; left < popularData.size(); left=left+2)
							{
								JSONObject leftitem = (JSONObject)popularData.get(left);
								out.println("<a class='genome-data-item clear' href='" + leftitem.get("link") + "'>");
								out.println("<div class='genome-data-icon left' style='background-image: url(" + leftitem.get("picture") + ")'></div>");
								out.println("<h3 class='down2x close highlight-e'>" + leftitem.get("data") + "</h3>");
								out.println("<p class='small'>" + leftitem.get("description") + "</p>");
								out.println("</a>");
							}
							out.println("	</div>");
							out.println("	<div class='column'>");
							for (int right = 1; right < popularData.size(); right=right+2)
							{
								JSONObject rightitem = (JSONObject)popularData.get(right);
								out.println("<a class='genome-data-item clear' href='" + rightitem.get("link") + "'>");
								out.println("<div class='genome-data-icon left' style='background-image: url(" + rightitem.get("picture") + ")'></div>");
								out.println("<h3 class='down2x close highlight-e'>" + rightitem.get("data") + "</h3>");
								out.println("<p class='small'>" + rightitem.get("description") + "</p>");
								out.println("</a>");
							}
							out.println("	</div>");
							out.println("<div class='clear'></div>");
							
							// links to
							out.println("<p><a class='double-arrow-link' href='" + pop.get("gb_link") + "'>View This Genome in Genome Browser</a></p><br/>");
							out.println("</div>"); 	
						}
						out.println("<ul class='no-decoration genome-list tab-headers third'>");
						for(int nameCt = 0; nameCt < popularItems.size(); nameCt++)
						{
							JSONObject name = (JSONObject)popularItems.get(nameCt);
							out.println("<li>");
							out.println("<a data-genome-href='" + name.get("link") + "' class='genome-link' href='#genome-tab" + (nameCt+1) + "'>" + name.get("popularName") + "</a>");
							out.println("<div class='arrow'></div>");
							out.println("</li>");
						}
						out.println("</ul>");
					%>
				</div><!-- / group -->
				<p class='down'><a class="double-arrow-link" href="GenomeList?cType=taxon&cId=2&dataSource=&displayMode=&pk=&kw=">View All PATRIC Genomes</a></p>
			</div>
		</div>

		<!-- Section: Tools -->
		<%
			JSONObject jsonTools = (JSONObject) jsonData.get("tools");
		%>
		<div class="data-tab" id="tab2">
			<div class="data-box">
				<h3 class="ribbon-title"><%=jsonTools.get("subtitle")%></h3>
				<%
					// list of tools available for this data type
					JSONArray tools = (JSONArray) jsonTools.get("tools");
					if((tools != null) && (tools.size() > 0))
					{
						out.println("<ul class='no-decoration inline tools-image-list far2x'>");
						for(int toolct = 0; toolct < tools.size();toolct++)
						{
							JSONObject tool = (JSONObject)tools.get(toolct);
							out.println("<li>");
							out.println("	<a href='" + tool.get("url") +"'><img alt='" + tool.get("title") + "' src='" + tool.get("image") + "' />");
							out.println("	<div class='overlay'><p>" + tool.get("title") + "</p></div>");
							out.println("	</a>");
							out.println("</li>");
						}
						out.println("</ul>");
					}
					if(jsonTools.get("content") != null) {
						out.println(jsonTools.get("content").toString());
					}
					// tool version workflow
					JSONArray workflows = (JSONArray)jsonTools.get("workflows");
					if((workflows != null ) && (workflows.size() > 0)) {
						out.println("<h3 class=\"ribbon-title close2x\">View Genome Tutorials</h3>");
						out.println("<div class='tools-workflow-list'>");
						int rows = workflows.size()/2+1;
						// 1st column
						out.println("<ul class='no-decoration column-left'>");
						for(int wfct = 0; wfct < workflows.size();wfct=wfct+2)
						{
							JSONObject wf = (JSONObject)workflows.get(wfct);
							out.println("<li>");
							out.println("	<figure class='figure-left'><a href='" + wf.get("url") + "'><img src='" + wf.get("image") + "' /></a></figure>");
							out.println("	<h3 class='no-underline-links close'><a href='" + wf.get("url") + "'>" + wf.get("title") + "</a></h3>");
							out.println(	wf.get("content"));
							out.println("</li>");
						}
						out.println("</ul>");
						// 2nd column
						out.println("<ul class='no-decoration column-right'>");
						for(int wfct = 1; wfct < workflows.size();wfct=wfct+2)
						{
							JSONObject wf = (JSONObject)workflows.get(wfct);
							out.println("<li>");
							out.println("	<figure class='figure-left'><a href='" + wf.get("url") + "'><img src='" + wf.get("image") + "' /></a></figure>");
							out.println("	<h3 class='no-underline-links close'><a href='" + wf.get("url") + "'>" + wf.get("title") + "</a></h3>");
							out.println(	wf.get("content"));
							out.println("</li>");
						}
						out.println("</ul>");
						out.println("</div>");
					}
				%>
				<div class="clear"></div>
			</div>
		</div>

		<!-- Section: Process -->
		<%
			JSONObject jsonProcess = (JSONObject) jsonData.get("process");
		%>
		<div class="data-tab" id="tab3">
			<div class="data-box">
				<h3 class="ribbon-title"><%=jsonProcess.get("subtitle") %></h3>
				<% if (jsonProcess.containsKey("content") && jsonProcess.get("content") != null) { %>
					<%=jsonProcess.get("content") %>
				<% } %>
				<% if(jsonProcess.containsKey("image") && jsonProcess.get("image") != null) { %>
					<img alt="<%=jsonProcess.get("subtitle")%>" src="<%=jsonProcess.get("image")%>" />
				<% } %>
			</div>
		</div>

		<!-- Section: Download -->
		<%
			JSONObject jsonDownload = (JSONObject) jsonData.get("download");
		%>
		<div class="data-tab" id="tab4">
			<div class="data-box">
				<h3 class="ribbon-title left"><%=jsonDownload.get("subtitle") %></h3>
				<div class="no-underline-links right" style="width:480px; padding-top:13px;">
					<a class="arrow-slate-e" href="<%=ftpUrl%>" target="_blank">Download files via FTP Server</a>
				</div>
				<div class="clear"></div>
				<% if (jsonDownload.containsKey("content") && jsonDownload.get("content") != null) { %>
					<%=jsonDownload.get("content") %>
				<% } %>
				<script src="/patric/js/vbi/SimpleGenomeSelector.js"></script>
				<div class="left" id="GenomeSelector"></div>
				<div class="right">
					<form id="searchForm" name="searchForm" action="/portal/portal/patric/Downloads/DownloadsWindow?action=b&cacheability=PAGE&mode=download" target="" method="post" onsubmit="return false;">
					<input type="hidden" id="cType" name="cType" value="" />
					<input type="hidden" id="cId" name="cId" value="" />
					<input type="hidden" id="genomeId" name="genomeId" value="" />
					<input type="hidden" id="taxonId" name="taxonId" value="" />
					<input type="hidden" id="finalfiletype" name="finalfiletype" value="" />
					<input type="hidden" id="finalalgorithm" name="finalalgorithm" value="" />

					<h3>Annotation Source</h3>
					<div class="far queryblock">
						<input id="annotation_patric" type="checkbox" name="algorithm" value=".PATRIC" checked="checked"/> <label for="annotation_patric">PATRIC</label>
						<input id="annotation_refseq" type="checkbox" name="algorithm" value=".RefSeq"/> <label for="annotation_refseq">RefSeq</label>
					</div>
					<h3>File Type</h3>
					<div class="far">
						<div class="left queryblock">
							<input type="radio" name="filetype" value=".fna" id="filetype_fna" checked="checked"/> <label for="filetype_fna">Genomic Sequences in FASTA (*.fna)</label>
							<input type="radio" name="filetype" value=".faa" id="filetype_faa"/> <label for="filetype_faa">Protein Sequences in FASTA (*.faa)</label>
							<input type="radio" name="filetype" value=".gbf" id="filetype_gbf"/> <label for="filetype_gbf">All annotations in GenBank file format (*.gbf)</label>
							<input type="radio" name="filetype" value=".features.tab" id="filetype_features"/> <label for="filetype_features">All genomic features in tab-delimited format (*.features.tab)</label>
							<input type="radio" name="filetype" value=".cds.tab" id="filetype_cds"/> <label for="filetype_cds">Protein coding genes in tab-delimited format (*.cds)</label>
							<input type="radio" name="filetype" value=".rna.tab" id="filetype_rna"/> <label for="filetype_rna">RNAs in tab-delimited format (*.rna)</label>
						</div>
						<div class="right queryblock">
							<input type="radio" name="filetype" value=".ffn" id="filetype_ffn"/> <label for="filetype_ffn">DNA Sequences of Protein Coding Genes (*.ffn)</label>
							<input type="radio" name="filetype" value=".frn" id="filetype_frn"/> <label for="filetype_frn">DNA Sequences of RNA Coding Genes (*.frn)</label>
							<input type="radio" name="filetype" value=".pathway.tab" id="filetype_path"/> <label for="filetype_path">Pathway assignments in tab-delimited format (*.pathway.tab)</label>
						</div>
						<div class="clear"></div>
					</div>
					
					<input type="submit" value="Download" onclick="download()" class="button right" style="cursor:pointer" />
					</form>
				</div>
				<div class="clear"></div>
			</div>
		</div>
	</section>
</div>
<script type="text/javascript">
//<![CDATA[
var sgs;
Ext.onReady(function(){
	sgs = Ext.create("VBI.SimpleGenomeSelector",{});
});

function getSelected(type){
	var s = new Array();
	if (document.searchForm[type].length == undefined) {
		if (document.searchForm[type].checked) {
			s.push(document.searchForm[type].value);
		}
	} else {
		for (var i=0; i < document.searchForm[type].length; i++) {
			if (document.searchForm[type][i].checked){
				s.push(document.searchForm[type][i].value);
			}
		}
	}
	return s.join(",");
}

function download() {
	var selectedGenomes = sgs.child("#searchResult").value;
	var size = selectedGenomes.length;

	if (size == 0) {
		Ext.MessageBox.alert("No genome selected", "Please choose genomes to download.");
	} else if(size > 100) {
		Ext.MessageBox.alert(size+" genomes", "Current resources can not handle more than 100 genomes..");
	} else {
		Ext.getDom("genomeId").value = selectedGenomes.join(",");
		Ext.getDom("finalfiletype").value = getSelected("filetype");
		Ext.getDom("finalalgorithm").value = getSelected("algorithm");
		
		if(Ext.getDom("finalfiletype").value != "" && Ext.getDom("finalalgorithm").value != "") {
			Ext.getDom("searchForm").submit();
		} else {
			Ext.MessageBox.alert("Error", "Please choose at least one Annotation source and one File Type");
		}
	}
}
//]]>
</script>
<svg class="definitions">
	<defs>
		<!-- Gradient defs for the top5 bar charts -->
		<!-- Bluish -->
		<linearGradient id="bar-0" x1="0" x2="0" y1="0" y2="100%">
			<stop offset="0%" stop-color="rgb(66,117,151)" stop-opacity="1"/>
			<stop offset="100%" stop-color="rgb(91,138,170)" stop-opacity="1"/>
		</linearGradient>
		<!-- Greenish -->
		<linearGradient id="bar-1" x1="0" x2="0" y1="0" y2="100%">
			<stop offset="0" stop-color="rgb(109,156,47)" stop-opacity="1"/>
			<stop offset="100%" stop-color="rgb(151,199,90)" stop-opacity="1"/>
		</linearGradient>
		<!-- Yellowish -->
		<linearGradient id="bar-2" x1="0" x2="0" y1="0" y2="100%">
			<stop offset="0" stop-color="rgb(246,218,98)" stop-opacity="1"/>
			<stop offset="100%" stop-color="rgb(251,232,153)" stop-opacity="1"/>
		</linearGradient>
		<!-- Blue-grayish -->
		<linearGradient id="bar-3" x1="0" x2="0" y1="0" y2="100%">
			<stop offset="0" stop-color="rgb(56,93,117)" stop-opacity="1"/>
			<stop offset="100%" stop-color="rgb(102,130,149)" stop-opacity="1"/>
		</linearGradient>
		<!-- Khaki -->
		<linearGradient id="bar-4" x1="0" x2="0" y1="0" y2="100%">
			<stop offset="0" stop-color="rgb(230,218,174)" stop-opacity="1"/>
			<stop offset="100%" stop-color="rgb(206,192,142)" stop-opacity="1"/>
		</linearGradient>
	</defs>
</svg>