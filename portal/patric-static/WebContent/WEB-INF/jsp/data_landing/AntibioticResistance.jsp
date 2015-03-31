<%@ page import="org.json.simple.JSONObject" %>
<%@ page import="org.json.simple.JSONArray" %>
<%
JSONObject jsonData = (JSONObject) request.getAttribute("jsonData");
%>
<link rel="stylesheet" href="/patric/css/dlp.css"></link>
<script type="text/javascript" src="/patric/js/libs/d3.v3.min.js"></script>
<script type="text/javascript" src="/patric/js/libs/jquery.scrollTo-1.4.11-min.js"></script>
<script type="text/javascript" src="/patric/js/libs/waypoints.min.js"></script>

<!-- <script type="text/javascript" src="/patric/js/genome-charts-init.js"></script>-->
<script type="text/javascript" src="/patric/js/charts.js"></script>
<script type="text/javascript" src="/patric/js/landing-page-init.js"></script>

<div class="container tabs-above">
	<nav class="breadcrumbs">
		<ul id="breadcrumbs" class="inline no-decoration">
			<li><a href="Home" title="">Home</a></li>
			<li>Antibiotic Resistance</li>
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
			<%--<li class="left"><a class="scrollTo" data-tab-target="tab4" style="background-image: url(/patric/images/data-tab-icon-download.png)" href="javascript:void(0)">Download</a></li> --%>
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
				<div class='large'><%=jsonTopline.get("content")%></div>
			<% } %>
			<%--
			<div class="data-box" id="dlp-features-data">
				<h3 class="ribbon-title">What do we mean by ...</h3>
				<br/>
				<div class="data-box">
					<h4>Virulence Factors:</h4>
					<p>Virulence factors refer to the gene products that enable a bacteria to establish itself on or within a host organism and enhance its potential to cause disease. We have integrated and map virulence factor genes from the following sources:</p>
					<ul>
						<li>PATRIC_VF</li>
						<li>VFDB</li>
						<li>Victors</li>
					</ul>
				</div>
				<div class="data-box">
					<h4>Antibiotic Resistance:</h4>
					<p>Antibiotic Resistance refers to the ability of a bacteria to develop resistance to antibiotics through gene mutation or acquisition of antibiotic resistance genes. We have integrated and map known antibiotic resistance genes from the following sources:</p>
					<ul>
						<li>ARDB</li>
						<li>CARD</li>
					</ul>
				</div>
				<div class="data-box">
					<h4>Drug Targets:</h4>
					<p>Drug Targets refer to the proteins being targeted by known/approved/experimental small molecule drugs. We have integrated and map such drug targets from following two sources:</p>
					<ul>
						<li>DrugBank</li>
						<li>TTD</li>
					</ul>
				</div>
				<div class="data-box">
					<h4>Human Homologs:</h4>
					<p>Human Homologs refer to the bacterial proteins that share high sequence similarity with human proteins. We have integrated and map human proteome available from NCBI RefSeq database.</p>
				</div>
				<div class="clear"></div>
			</div>
			--%>
			<!-- popular genomes -->
			<%
			JSONObject popular = (JSONObject)jsonData.get("popularGenomes");
			%>
			<div class="data-box popular-box tabbed hover-tabs no-underline-links">
				<h3 class="ribbon-title"><%=popular.get("popularTitle") %></h3>
				<div class="group">
				<%
					JSONArray popularItems = (JSONArray) popular.get("popularList");
					// list of genome-tabs
					for(int popCt = 0; popCt < popularItems.size(); popCt++)
					{
						JSONObject pop = (JSONObject) popularItems.get(popCt);
						out.println("<div class='genome-data right half group' id='genome-tab" + (popCt+1) + "'>");
						JSONArray specialtyGenes = (JSONArray)pop.get("specialtyGenes");
						JSONArray popularLinks = (JSONArray)pop.get("links");
						
							//list of properties
							out.println("<div class='far2x'>");
								// specialty genes
								out.println("<div class='left left-align-text'><h3>Antibiotic Resistance Genes:</h3>");
								for(int i = 0; i < specialtyGenes.size(); i++)
								{
									JSONObject item = (JSONObject)specialtyGenes.get(i);
	
									out.println("<a class='left right-align-text' style='width:60px; padding-right:10px' href='" + item.get("link") + "'>");
									out.println("<span class='highlight-e' style='font-size:14px'>" + item.get("data") + "</span>");
									out.println("</a>");
									
									out.println("<span class='left small' style='padding-top:5px'>" + item.get("description") + "</span>");
									out.println("<div class='clear'></div>");
								}
								out.println("</div>");
								
								out.println("<div class='clear'></div>");
							out.println("</div>");
							
							// list of linkouts
							out.println("<h3>Explore Genomic Features in </h3>");
							for(int i = 0; i < popularLinks.size(); i++)
							{
								JSONObject item = (JSONObject)popularLinks.get(i);
								String margin = null;
								String align = null;
								if ((i % 2) == 0) {
									align = "left";
									margin = "";
								} else {
									align = "right";
									margin = "margin-right:100px;";
								}
								out.println("<a class='double-arrow-link " + align + "' style='" + margin +"' href=\"" + item.get("link") + "\">"+ item.get("name") + "</a>");
								if ((i % 2) == 1) {
									out.println("<br/>");
								}
							}
						out.println("</div>");
					}
					// list of genomes
					out.println("<ul class='no-decoration genome-list tab-headers third'>");
					for(int nameCt = 0; nameCt < popularItems.size(); nameCt++)
					{
						JSONObject name = (JSONObject)popularItems.get(nameCt);
						out.println("<li><a data-genome-href='" + name.get("link") + "' class='genome-link' href='#genome-tab" + (nameCt+1) + "'>" + name.get("popularName") + "</a>");
						out.println("<div class='arrow'></div></li>");
					}
					out.println("</ul>");
				%>
				</div>
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
						out.println("<h3 class=\"ribbon-title close2x\">View Tutorials</h3>");
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
		<%--
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
		</div> --%>
	</section>
</div>
<script type="text/javascript">
//<![CDATA[
// var sgs;
// Ext.onReady(function(){
// 	sgs = Ext.create('VBI.SimpleGenomeSelector',{});
// });

// function getSelected(type){
// 	var s = new Array();
// 	if (document.searchForm[type].length == undefined) {
// 		if (document.searchForm[type].checked) {
// 			s.push(document.searchForm[type].value);
// 		}
// 	} else {
// 		for (var i=0; i < document.searchForm[type].length; i++) {
// 			if (document.searchForm[type][i].checked){
// 				s.push(document.searchForm[type][i].value);
// 			}
// 		}
// 	}
// 	return s.join(",");
// }

// function download() {
// 	var selectedGenomes = sgs.child("#searchResult").value;
// 	var size = selectedGenomes.length;

// 	if (size == 0) {
// 		Ext.MessageBox.alert('No genome selected','Please choose genomes to download.');
// 	} else if(size > 100) {
// 		Ext.MessageBox.alert(size+" genomes", 'Current resources can not handle more than 100 genomes..');
// 	} else {
// 		Ext.getDom("genomeId").value = selectedGenomes.join(",");
// 		Ext.getDom("finalfiletype").value = getSelected("filetype");
// 		Ext.getDom("finalalgorithm").value = getSelected("algorithm");
		
// 		if(Ext.getDom("finalfiletype").value != "" && Ext.getDom("finalalgorithm").value != "") {
// 			Ext.getDom("searchForm").submit();
// 		} else {
// 			Ext.MessageBox.alert('Error', 'Please choose at least one Annotation source and one File Type');
// 		}
// 	}
// }
//]]>
</script>