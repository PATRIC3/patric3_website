<%@ page import="org.json.simple.JSONObject" %>
<%@ page import="org.json.simple.JSONArray" %>
<%
JSONObject jsonData = (JSONObject) request.getAttribute("jsonData");
%>
<link rel="stylesheet" href="/patric/css/dlp.css"></link>
<script type="text/javascript" src="/patric/js/libs/jquery.scrollTo-1.4.11-min.js"></script>
<script type="text/javascript" src="/patric/js/libs/waypoints.min.js"></script>
<script type="text/javascript" src="/patric/js/libs/d3.v3.min.js"></script>

<script type="text/javascript" src="/patric/js/charts.js"></script>
<script type="text/javascript" src="/patric/js/figfam-charts-init.js"></script>
<script type="text/javascript" src="/patric/js/landing-page-init.js"></script>

<div class="container tabs-above">
	<nav class="breadcrumbs">
		<ul id="breadcrumbs" class="inline no-decoration">
			<li class="" style=""><a href="Home" title="">Home</a></li>
			<li>Protein Families</li>
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
			<div class="data-box collapse-bottom" id="dlp-proteinfamilies-dist-genera-header">
				<h3 class="ribbon-title"><!-- FIGfam Title --></h3>
				<p class="desc">Since this is a chart built by D3, this text should be replaced by relevant information from the data file.</p>
			</div><!-- /data-box -->
			<div class="far4x" id="dlp-proteinfamilies-dist-genera">
				<div class="chart-wrapper">
				<!-- This nav structure drives interactivity on the bar chart. -->
					<nav>
						<span class="label">Scale</span>
						<ul class="scale">
							<li class="real active">Real Values</li>
							<li class="normalize">Normalize</li>
						</ul>
						<span class="label">Data Set</span>
						<ul class="dataset">
							<li class="fvh active">Functional vs. Hypothetical</li>
							<li class="cva">Core vs. Accessory</li>
						</ul>
						<span class="label">Order by</span>
						<ul class="sort">
							<li class="index active">Genus</li>
						</ul>
						<ul class="sort">
							<li class="functional">Functional</li>
							<li class="hypothetical">Hypothetical</li>
						</ul>
					</nav>
					<!-- This is the div where the chart is actually drawn. -->
					<div class="chart"></div>
					<!-- The bar chart looks for this legend. There"s some SVG in here for the circles. -->
					<p class="legend">
						Legend:
						<svg height="24" width="24">
							<circle class="bar1-sample" cx="12" cy="12" r="12"></circle>
						</svg>
						<span class="bar1-label">Functional</span>
						<svg height="24" width="24">
							<circle class="bar2-sample" cx="12" cy="12" r="12"></circle>
						</svg>
						<span class="bar2-label">Hypothetical</span>
					</p>
				</div><!-- / chart-wrapper -->
			</div><!-- / figfam -->
			<div class="data-box popular-box tabbed hover-tabs no-underline-links" id="dlp-proteinfamily-dist">
				<h3 class="ribbon-title">Protein Family Distribution</h3>
				<div class="group">
				<%
					JSONArray popularItems = (JSONArray) ((JSONObject)jsonData.get("popularGenomes")).get("popularList");
					for(int popCt = 0; popCt < popularItems.size(); popCt++)
					{
						out.println("<div id='genome-tab" + (popCt+1) + "' style='display:none'></div>");
					}
					//
					out.println("<div class='genome-data right half'>");
					out.println("	<div id='dlp-proteinfamilies-dist-genus'><h4></h4><div class='chart'></div></div>");
					out.println("</div>");
					
					out.println("<ul class='no-decoration genome-list tab-headers third'>");
					
					for(int nameCt = 0; nameCt < 11; nameCt++)
					{
						JSONObject name = (JSONObject)popularItems.get(nameCt);
						out.println("<li class='left'>");
						out.println("	<a onmouseover='fifamDistChart.updateChart(" + nameCt + ")' data-genome-href='" + name.get("link") + "' class='genome-link ui-tabs-anchor' href='#genome-tab" + (nameCt+1) + "'>" + name.get("popularName") + "</a>");
						out.println("	<div class='arrow_far'></div>");
						out.println("</li>");
						
						name = (JSONObject)popularItems.get(nameCt+11);
						out.println("<li class='right'>");
						out.println("	<a onmouseover='fifamDistChart.updateChart(" + (nameCt+11) + ")' data-genome-href='" + name.get("link") + "' class='genome-link ui-tabs-anchor' href='#genome-tab" + (nameCt+11+1) + "'>" + name.get("popularName") + "</a>");
						out.println("	<div class='arrow'></div>");
						out.println("</li>");
					}
					out.println("<div class='clear'></div>");
					out.println("</ul>");
				%>
				</div><!-- / group -->
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
						out.println("<h3 class=\"ribbon-title close2x\">View Protein Family Tutorials</h3>");
						out.println("<div class='tools-workflow-list'>");
						int rows = workflows.size()/2+1;
						// 1st column
						out.println("<ul class='no-decoration column-left'>");
						for(int wfct = 0; wfct < workflows.size();wfct=wfct+2)
						{
							JSONObject wf = (JSONObject)workflows.get(wfct);
							out.println("<li>");
							out.println("	<figure class='figure-left'><a href=''><img src='" + wf.get("image") + "' /></a></figure>");
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
							out.println("	<figure class='figure-left'><a href=''><img src='" + wf.get("image") + "' /></a></figure>");
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
					<a class="arrow-slate-e" href="ftp://ftp.patricbrc.org/patric2" target="_blank">Download files via FTP Server</a>
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
						<input id="annotation_patric" type="radio" name="algorithm" value=".PATRIC" checked="checked"/> <label for="annotation_patric">PATRIC</label>
						<%--<input id="annotation_brc" type="checkbox" name="algorithm" value=".BRC"/> <label for="annotation_brc">Legacy BRC</label>
						<input id="annotation_refseq" type="checkbox" name="algorithm" value=".RefSeq"/> <label for="annotation_refseq">RefSeq</label>--%>
					</div>
					<h3>File Type</h3>
					<div class="far">
						<div class="left queryblock" style="width:480px">
							<input type="radio" name="filetype" value=".figfam" id="filetype_figfam" checked="checked" /> <label for="filetype_figfam">FIGfam assignments in tab-delimited format (*.figfam)</label>
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
		Ext.MessageBox.alert("No genome selected","Please choose genomes to download.");
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
		<!-- Hatch pattern for FigFam bars. -->
		<pattern height="8" id="tile1" patternUnits="userSpaceOnUse" width="8" x="0" y="0">
			<rect class="function" height="8" width="8" x="0" y="0"></rect>
			<line class="function" x1="0" x2="8" y1="0" y2="8"></line>
		</pattern>
	</defs>
</svg>