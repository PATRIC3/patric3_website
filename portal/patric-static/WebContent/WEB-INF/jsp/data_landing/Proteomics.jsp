<%@ page import="org.json.simple.JSONObject" %>
<%@ page import="org.json.simple.JSONArray" %>
<%
JSONObject jsonData = (JSONObject) request.getAttribute("jsonData");
%>
<link rel="stylesheet" href="/patric/css/dlp.css"></link>
<script type="text/javascript" src="/patric/js/libs/d3.v3.min.js"></script>
<script type="text/javascript" src="/patric/js/libs/jquery.scrollTo-1.4.11-min.js"></script>
<script type="text/javascript" src="/patric/js/libs/waypoints.min.js"></script>

<script type="text/javascript" src="/patric/js/genome-charts-init.js"></script>
<script type="text/javascript" src="/patric/js/charts.js"></script>
<script type="text/javascript" src="/patric/js/landing-page-init.js"></script>
<%--
<section class='sans-alternate workflow-title no-border letters-wider'>
	<div class='container'>
		<h1 class='upper down'>Genomes</h1>
	</div>
</section>
--%>
<div class="container tabs-above">
	<nav class="breadcrumbs">
		<ul id="breadcrumbs" class="inline no-decoration">
			<li class="" style=""><a href="Home" title="">Home</a></li>
			<li>Proteomics</li>
		</ul>
	</nav>
</div>
<div id='sticky-anchor'></div>
<div class='sticky'>
	<div class='container'>
		<ul class='no-decoration data-tab-toolbar upper large letters-wide'>
			<li class='left'><a class="active scrollTo" data-tab-target="tab1" style="background-image: url(/patric/images/data-tab-icon-data.png)" href="javascript:void(0)">Data</a></li>
			<li class='left'><a class="scrollTo" data-tab-target="tab2" style="background-image: url(/patric/images/data-tab-icon-tools.png)" href="javascript:void(0)">Tools</a></li>
			<li class='left'><a class="scrollTo" data-tab-target="tab3" style="background-image: url(/patric/images/data-tab-icon-process.png)" href="javascript:void(0)">Process</a></li>
			<li class='left'><a class="scrollTo" data-tab-target="tab4" style="background-image: url(/patric/images/data-tab-icon-download.png)" href="javascript:void(0)">Download</a></li>
		</ul>
	</div>
</div>
<div class="clear"></div>
<div class='container main-container'>
	<section class='main'>
		<!-- Section: Data -->
		<div class='data-tab' id='tab1'>
			[Placeholder for DATA TAB]
		</div>

		<!-- Section: Tools -->
		<%
			JSONObject jsonTools = (JSONObject) jsonData.get("tools");
		%>
		<div class='data-tab' id='tab2'>
			<div class='data-box'>
				<h3 class='ribbon-title'><%=jsonTools.get("subtitle")%></h3>
				<%
					// list of tools available for this data type
					JSONArray tools = (JSONArray) jsonTools.get("tools");
					if((tools != null) && (tools.size() > 0))
					{
						out.println("<ul class='no-decoration inline tools-image-list'>");
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
							out.println("	<p>" + wf.get("content") + "</p>");
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
							out.println("	<p>" + wf.get("content") + "</p>");
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
		<div class='data-tab' id='tab3'>
			<div class='data-box'>
				<h3 class='ribbon-title'><%=jsonProcess.get("subtitle") %></h3>
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
		<div class='data-tab' id='tab4'>
			<div class='data-box'>
				<h3 class='ribbon-title'><%=jsonDownload.get("subtitle") %></h3>
				<% if (jsonDownload.containsKey("content") && jsonDownload.get("content") != null) { %>
					<%=jsonDownload.get("content") %>
				<% } %>
			</div>
		</div>
	</section>
</div>
<%-- 
<svg class='definitions' >
	<defs>
		<!-- Gradients used for FigFam bars -->
		<lineargradient id='grad1' x1='0' x2='0' y1='100%' y2='0'>
			<stop offset='0' stop-color='rgb(149,0,0)' stop-opacity='1'></stop>
			<stop offset='.6' stop-color='rgb(149,7,50)' stop-opacity='1'></stop>
			<stop offset='100%' stop-color='rgb(255,180,0)' stop-opacity='1'></stop>
		</lineargradient>
		<lineargradient id='grad2' x1='0' x2='0' y1='100%' y2='0'>
			<stop offset='0' stop-color='rgb(3,87,182)'></stop>
			<stop offset='100%' stop-color='rgb(3,180,255)'></stop>
		</lineargradient>
		<!-- Hatch pattern for FigFam bars. -->
		<pattern height='8' id='tile1' patternUnits='userSpaceOnUse' width='8' x='0' y='0'>
			<rect class='function' height='8' width='8' x='0' y='0'></rect>
			<line class='function' x1='0' x2='8' y1='0' y2='8'></line>
		</pattern>
		<!-- Gradient defs for the top5 bar charts -->
		<!-- Bluish -->
		<linearGradient id='bar-0' x1='0' x2='0' y1='0%' y2='100%'>
			<stop offset='0%' stop-color='rgb(66,117,151)' stop-opacity='1'></stop>
			<stop offset='100%' stop-color='rgb(91,138,170)' stop-opacity='1'></stop>
		</linearGradient>
		<!-- Greenish -->
		<linearGradient id='bar-1' x1='0' x2='0' y1='0' y2='100%'>
			<stop offset='0' stop-color='rgb(109,156,47)' stop-opacity='1'></stop>
			<stop offset='100%' stop-color='rgb(151,199,90)' stop-opacity='1'></stop>
		</linearGradient>
		<!-- Yellowish -->
		<linearGradient id='bar-2' x1='0' x2='0' y1='0' y2='100%'>
			<stop offset='0' stop-color='rgb(246,218,98)' stop-opacity='1'></stop>
			<stop offset='100%' stop-color='rgb(251,232,153)' stop-opacity='1'></stop>
		</linearGradient>
		<!-- Blue-grayish -->
		<linearGradient id='bar-3' x1='0' x2='0' y1='0' y2='100%'>
			<stop offset='0' stop-color='rgb(56,93,117)' stop-opacity='1'></stop>
			<stop offset='100%' stop-color='rgb(102,130,149)' stop-opacity='1'></stop>
		</linearGradient>
		<!-- Khaki -->
		<linearGradient id='bar-4' x1='0' x2='0' y1='0' y2='100%'>
			<stop offset='0' stop-color='rgb(230,218,174)' stop-opacity='1'></stop>
			<stop offset='100%' stop-color='rgb(206,192,142)' stop-opacity='1'></stop>
		</linearGradient>
	</defs>
</svg>
--%>