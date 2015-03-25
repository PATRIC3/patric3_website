<%@ page import="org.json.simple.JSONObject" %>
<%@ page import="org.json.simple.JSONArray" %>
<%
JSONObject jsonData = (JSONObject) request.getAttribute("jsonData");
%>
<link rel="stylesheet" href="/patric/css/dlp.css"></link>
<script type="text/javascript" src="/patric/js/libs/d3.v3.min.js"></script>
<script type="text/javascript" src="/patric/js/libs/jquery.scrollTo-1.4.11-min.js"></script>
<script type="text/javascript" src="/patric/js/libs/waypoints.min.js"></script>

<script type="text/javascript" src="/patric/js/charts.js"></script>
<script type="text/javascript" src="/patric/js/transcriptomics-charts-init.js"></script>
<script type="text/javascript" src="/patric/js/landing-page-init.js"></script>

<div class="container tabs-above">
	<nav class="breadcrumbs">
		<ul id="breadcrumbs" class="inline no-decoration">
			<li><a href="Home" title="">Home</a></li>
			<li>Transcriptomics</li>
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
			<%--<li class="left"><a class="scrollTo" data-tab-target="tab4" style="background-image: url(/patric/images/data-tab-icon-download.png)" href="javascript:void(0)">Download</a></li>--%>
		</ul>
	</div>
</div>
<div class="clear"></div>
<div class="container main-container">
	<section class="main">
		<!-- Section: Data -->
		<%
			JSONObject topSpecies = (JSONObject)jsonData.get("topSpecies");
			JSONObject featuredExperiment = (JSONObject)jsonData.get("featuredExperiment");
			JSONArray experiments = (JSONArray)featuredExperiment.get("data");
			JSONObject jsonTopline = (JSONObject)jsonData.get("data"); 
		%>
		<div class="data-tab" id="tab1">
			<h3><%=jsonTopline.get("subtitle") %></h3>
			<% if(jsonTopline.get("content") != null) { %>
				<div class="large"><%=jsonTopline.get("content")%></div>
			<% } %>
			<div class="data-box left" id="dlp-transcriptomics-top-species">
				<h3 class="ribbon-title"><%=topSpecies.get("title") %></h3>
				<div class="chart"></div>
			</div>
			<div class="data-box right" style="width:410px; line-height: 1.6em">
				<h3 class="ribbon-title">Featured Experiments</h3>
				<% for (Object obj: experiments) { 
					JSONObject exp = (JSONObject) obj;
				%>
				<div class="far">
					<img src="/patric/images/global_experiment.png" alt="Experiment" style="float: left; padding: 3px 3px 0px 3px;">
					<div style="margin-left: 60px">
						<a href="<%=exp.get("link")%>"><%=exp.get("title") %></a>
						<br/><span style="color: #C60;"><%=exp.get("organism")%></span>
						<br/><span>Accession: <%=exp.get("accession") %></span>
						<% if (exp.get("pmid") != "") { %>
							, &nbsp; <span>PubMed: <a class="arrow-slate-e" href="http://www.ncbi.nlm.nih.gov/pubmed/<%=exp.get("pmid") %>" target="_blank"><%=exp.get("pmid") %></a></span>
						<% } %>
					</div>
				</div>
				<% } %>
			</div>
			<div class="clear"></div>
			<%
			JSONObject popular = (JSONObject)jsonData.get("popularGenomes");
			%>
			<div class="data-box popular-box tabbed hover-tabs no-underline-links">
				<h3 class="ribbon-title"><%=popular.get("popularTitle") %></h3>
				<div class="group">
				<%
					JSONArray popularItems = (JSONArray) popular.get("popularList");
					String linkout = ((JSONObject) popularItems.get(0)).get("link").toString(); // initial link
					// list of genome-tabs
					for(int popCt = 0; popCt < popularItems.size(); popCt++)
					{
						out.println("<div id='genome-tab" + (popCt+1) + "' style='display:none'></div>");
					}
					
					out.println("<div class='genome-data right half'>");
					out.println("	<div id='dlp-transcriptomics-top-mutants'><span class='bold'>Top 5 Gene Modifications</span><div class='chart'></div></div>");
					out.println("	<div id='dlp-transcriptomics-top-conditions'><span class='bold'>Top 5 Experiment Conditions</span><div class='chart'></div></div>");
					out.println("	<p><a class='double-arrow-link' id='dlp-transcriptomics-linkout' href='" + linkout + "'>View All Experiment for This Genome</a></p>");
					// View All Experiments for this genome
					out.println("</div>");
					
					// list of genomes
					out.println("<ul class='no-decoration genome-list tab-headers third'>");
					for(int nameCt = 0; nameCt < popularItems.size(); nameCt++)
					{
						JSONObject name = (JSONObject)popularItems.get(nameCt);
						out.println("<li><a onmouseover='topGeneModifications.updateChart("+(nameCt)+");topExperimentConditions.updateChart("+(nameCt)+");updateLinkout(\""+ name.get("link") + "\")' data-genome-href='" + name.get("link") + "' class='genome-link' href='#genome-tab" + (nameCt+1) + "'>" + name.get("popularName") + "</a>");
						out.println("<div class='arrow'></div></li>");
					}
					out.println("</ul>");
				%>
				</div>
 				<p class="down"><a class="double-arrow-link" href="ExperimentList?cType=taxon&cId=2&kw=">View All Experiments in PATRIC</a></p>
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
						out.println("<h3 class=\"ribbon-title close2x\">View Transcriptomics Tutorials</h3>");
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
						out.println("<div class='clear'></div>");
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
			//JSONObject jsonDownload = (JSONObject) jsonData.get("download");
		%>
		<%--
		<div class="data-tab" id="tab4">
			<div class="data-box">
				<h3 class="ribbon-title"><%=jsonDownload.get("subtitle") %></h3>
				<% if (jsonDownload.containsKey("content") && jsonDownload.get("content") != null) { %>
					<%=jsonDownload.get("content") %>
				<% } %>
			</div>
		</div>--%>
	</section>
</div>
<svg class="definitions">
	<defs>
		<!-- Gradient defs for the top5 bar charts -->
		<!-- Bluish -->
		<linearGradient id="bar-0" x1="0" x2="0" y1="0%" y2="100%">
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
		<!-- drop shadow -->
		<filter id="drop-shadow">
			<feGaussianBlur in="SourceAlpha" stdDeviation="2.2"/>
			<feOffset dx="2" dy="2" result="offsetblur"/>
			<feFlood flood-color="rgba(0,0,0,1)"/>
			<feComposite in2="offsetblur" operator="in"/>
			<feMerge>
				<feMergeNode/>
				<feMergeNode in="SourceGraphic"/>
			</feMerge>
		</filter>
	</defs>
</svg>