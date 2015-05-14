<%@ page import="java.util.Map" %>
<%

Map<Integer, Integer> genomes = (Map<Integer, Integer>) request.getAttribute("genomes");
int cntExperiments = (Integer) request.getAttribute("cntExperiments");
int mtbTaxon = (Integer) request.getAttribute("mtbTaxon");

%>
<script type="text/javascript">
//<![CDATA[
var name,url,cId,cType,genomeId,keyword;

function getGenomeByTaxon(taxonId, callback) {

	Ext.Ajax.request({
		disableCaching: false,
		headers: {
			'Accept': 'application/json'
		},
		url: '/api/genome/?eq(taxon_lineage_ids,' + taxonId + ')&limit(10000)',
		method: 'GET',
		success: function(response) {
			var genomes = Ext.JSON.decode(response.responseText);
			var genomeIds = [];
			for (var i = 0; i < genomes.length; i++) {
				genomeIds.push(genomes[i].genome_id);
			}

			callback(genomeIds);
		}
	});
}

function searchGenome() {
	var taxonId = '<%=mtbTaxon %>'; //Mtb genomes
	
	name = "Genome";
	url = "/portal/portal/patric/GenomeFinder/GenomeFinderWindow?action=b&cacheability=PAGE";
	cId = taxonId;
	cType = "taxon";
	genomeId = "";
	keyword = Ext.getDom("keyword_genome").value;

	var object = {};
	if (keyword == "" || keyword == "*") { 
		object["Keyword"] = "(*)";	
	} else {
		object["Keyword"] = "("+EncodeKeyword(keyword)+")";
	}

	getGenomeByTaxon(taxonId, function(genomeIds) {

		genomeIds.push('246196.19','216594.6'); //add two outgroup genomes

		object["genome_id"] = genomeIds.join("##");
		search__(constructKeyword(object, name), cId, cType);
	});
}

function searchFeature() {
	var need_genomes = true;
	
	name = "Feature";
	url = "/portal/portal/patric/GenomicFeature/GenomicFeatureWindow?action=b&cacheability=PAGE";
	cId = getScope("tb-home-proteins");
	cType = "taxon";
	genomeId = "";
	keyword = Ext.getDom("keyword_feature").value;
	var outgroup = getOutgroupGenomes();
	
	if (cId == 83332) {
		need_genomes = false;
		cType = "genome";
		cId = 83332.12;
		genomeId = "83332.12";
		if (outgroup.length > 0) {
			genomeId += "##"+outgroup.join("##");
		}
	}
	
	var object = {};
	object["annotation"] = "";
	object["feature_type"] = "";
	if (keyword == "" || keyword == "*") { 
		object["Keyword"] = "(*)";	
	} else {
		object["Keyword"] = "("+EncodeKeyword(keyword)+")";
	}
	
	if (need_genomes) {

		getGenomeByTaxon(cId, function(genomeIds) {

			if (outgroup.length > 0) {
				genomeIds.push(outgroup);
			}

			object["genome_id"] = genomeIds.join("##");
			search__(constructKeyword(object, name), cId, cType);
		});
	} else {
		object["genome_id"] = genomeId;
		search__(constructKeyword(object, name), cId, cType);
	}
}

function searchSpecialtyGene() {
	var need_genomes = true;
	
	name = "SpecialtyGeneMapping";
	url = "/portal/portal/patric/SpecialtyGeneSearch/SpecialtyGeneSearchWindow?action=b&cacheability=PAGE";
	cId = getScope("tb-home-sp-genes");
	cType = "taxon";
	genomeId = "";
	keyword = Ext.getDom("keyword_spgene").value;

	if (cId == 83332) {
		need_genomes = false;
		cType = "genome";
		cId = 83332.12;
		genomeId = "83332.12";
	}
	
	var object = {};
	var properties = getTargetProperties();
	if (properties.length > 0) {
		object["property"] = properties.join("##");
	}

	if (keyword == "" || keyword == "*") { 
		object["Keyword"] = "(*)";	
	} else {
		object["Keyword"] = "("+EncodeKeyword(keyword)+")";
	}
	
	if (need_genomes) {
		getGenomeByTaxon(cId, function(genomeIds) {

			object["genome_id"] = genomeIds.join("##");
			search__(constructKeyword(object, name), cId, cType);
		});
	} else {
		object["genome_id"] = genomeId;
		search__(constructKeyword(object, name), cId, cType);
	}
}

function search__(_keyword, cId, cType){
	Ext.Ajax.request({
		url: url,
		method: 'POST',
		params: {
			cType: cType,
			cId: cId,
			sraction: "save_params",
			keyword: _keyword.replace(/\"/g, "%22").replace(/'/g, "%27").trim(),
			exact_search_term: keyword.trim(),
			search_on: "Keyword"
		},
		success: function(rs) {
			if (name == "Genome") {
				document.location.href = "GenomeFinder?cType=" + cType + "&cId=" + cId + "&dm=result&pk=" + rs.responseText;
			}
			else if (name == "Feature") {
				document.location.href = "GenomicFeature?cType=" + cType + "&cId=" + cId + "&dm=result&pk=" + rs.responseText;
			}
			else if (name == "SpecialtyGeneMapping") {
				document.location.href = "SpecialtyGeneSearch?cType=" + cType + "&cId=" + cId + "&dm=result&pk=" + rs.responseText;
			}
		}
	});
}

function getScope(parentId) {
	//var elements = document.getElementById(parent).getElementsByName("scope");
	var elements = document.querySelectorAll("#" + parentId + " input[name=scope]");
	for (var i=0; i<elements.length; ++i) {
		if (elements[i].checked) {
			return elements[i].value;
		}
	} 
}

function getOutgroupGenomes() {
	var elements = document.getElementsByName("outgroup");
	var selected = [];
	
	for (var i = 0; i < elements.length; ++i) {
		if (elements[i].checked) {
			selected.push(elements[i].value);
		}
	}
	return selected;
}

function getTargetProperties() {
	var elements = document.getElementsByName("property");
	var selected = [];
	
	for (var i = 0; i < elements.length; ++i) {
		if (elements[i].checked) {
			selected.push(elements[i].value);
		}
	}
	return selected;
}
function launchCPT() {
	getGenomeByTaxon('<%=mtbTaxon%>', function(genomeIds) {

		genomeIds.push('246196.19','216594.6'); //add two outgroup genomes

		_launchCPT(genomeIds.join(","));
	});
}

function _launchCPT(idList) {
	Ext.Ajax.request({
		url: '/portal/portal/patric/PathwayFinder/PathwayFinderWindow?action=b&cacheability=PAGE',
		method: 'POST',
		params: {cType: "taxon"
			,cId: ""
			,sraction: "save_params"
			,genomeId: idList
			,search_on: "Keyword"
			,taxonId: ""
			,keyword: ""
		},
		success: function(rs) {
			document.location.href="PathwayFinder?cType=taxon&cId=<%=mtbTaxon%>&dm=result&map=&ec_number=&algorithm=PATRIC&pk="+rs.responseText;
		}
	});
}

function launchPFS() {
	getGenomeByTaxon('<%=mtbTaxon%>', function(genomeIds) {

		genomeIds.push('246196.19','216594.6'); //add two outgroup genomes

		_launchPFS(genomeIds.join(","));
	});
}

function _launchPFS(idList) {
	Ext.Ajax.request({
		url: '/portal/portal/patric/FIGfam/FIGfamWindow?action=b&cacheability=PAGE',
		method: 'POST',
		timeout: 600000,
		params: {callType: "toSorter", genomeIds: idList, keyword:""},
		success: function(rs) {
			document.location.href = "FIGfam?cType=taxon&cId=<%=mtbTaxon%>&bm=tool&dm=result&pk=" + rs.responseText + "#gs_0=0";
		}
	});
}

function launchTranscriptomicsUploader() {
    alert("In order to support new functionality in PATRIC, we have implemented a new workspace which includes a new form for upload of transcriptomics data.");

    // link to new app page (/app/Expression)
    document.location = "/app/Expression";
}
//]]>
</script>
<script type="text/javascript" src="/patric-searches-and-tools/js/search_common.js"></script>
<script type="text/javascript" src="/patric-searches-and-tools/js/solrKeyword.js"></script>
<script type="text/javascript" src="/patric/js/vbi/AddToWorkspace.min.js"></script>
<script type="text/javascript" src="/patric/js/vbi/TranscriptomicsUpload.min.js"></script>

<div class="container tabs-above">
	<div id="utilitybox" class="smallest right no-underline-links">
		<a class="double-arrow-link" href="http://enews.patricbrc.org/what-is-tb-patric/" target="_blank">About TB @ PATRIC</a>
		<br><a class="double-arrow-link" href="Downloads?cType=taxon&amp;cId=<%=mtbTaxon %>" target="_blank">Download all TB @ PATRIC genomes</a>
	</div>
	<h1 class="bold">Welcome to TB @ PATRIC</h1>
	<div class="clear far2x"></div>
</div>
<%--<div class='container main-container'>--%>
<div class='container'>
	<section class='main'>
		<section class='two-thirds-col'>
			<div class='column' style='width:331px'>
				<h3 class="section-title normal-case close2x">
					<span class="wrap">TB Reference Genomes</span>
				</h3>
				<div class="far2x has-border">
					<div id="tb-home-genomes" class="no-underline-links">
						<table class="far">
							<tr>
								<td style="width:280px"><a href="Genome?cType=genome&amp;cId=83332.12">Mycobacterium tuberculosis H37Rv</a></td>
								<td>
									<a href="FeatureTable?cType=genome&amp;cId=83332.12&amp;featuretype=CDS&amp;annotation=PATRIC&amp;filtertype="><img src="/patric/images/icon_table.gif" alt="Feature Table" title="Feature Table"></a>
								</td>
							</tr>
							<tr>
								<td><a href="Genome?cType=genome&amp;cId=22151">Mycobacterium tuberculosis CDC1551</a></td>
								<td>
									<a href="FeatureTable?cType=genome&amp;cId=22151&amp;featuretype=CDS&amp;annotation=PATRIC&amp;filtertype="><img src="/patric/images/icon_table.gif" alt="Feature Table" title="Feature Table"></a>
								</td>
							</tr>
							<tr>
								<td><a href="Genome?cType=genome&amp;cId=80988">Mycobacterium bovis BCG str. Pasteur 1173P2</a></td>
								<td>
									<a href="FeatureTable?cType=genome&amp;cId=80988&amp;featuretype=CDS&amp;annotation=PATRIC&amp;filtertype="><img src="/patric/images/icon_table.gif" alt="Feature Table" title="Feature Table"></a>
								</td>
							</tr>
							<tr>
								<td><a href="Genome?cType=genome&amp;cId=246196.19">Mycobacterium smegmatis str. MC2 155</a></td>
								<td>
									<a href="FeatureTable?cType=genome&amp;cId=246196.19&amp;featuretype=CDS&amp;annotation=PATRIC&amp;filtertype="><img src="/patric/images/icon_table.gif" alt="Feature Table" title="Feature Table"></a>
								</td>
							</tr>
							<tr>
								<td><a href="Genome?cType=genome&amp;cId=216594.6">Mycobacterium marinum M</a></td>
								<td>
									<a href="FeatureTable?cType=genome&amp;cId=216594.6&amp;featuretype=CDS&amp;annotation=PATRIC&amp;filtertype="><img src="/patric/images/icon_table.gif" alt="Feature Table" title="Feature Table"></a>
								</td>
							</tr>
						</table>
						<h4>Find other Mtb genomes...</h4>
						<form action="#" onsubmit="return false;">
							<input type="text" id="keyword_genome" placeholder="Search using genome name or metadata" style="width:250px">
							<input type="submit" class="button" value="Search" onclick="searchGenome()"/>
						</form>
						<label>Examples:</label>
						<span class="hint">Mycobacterium Erdman</span>
						<br/>
						<span class="hint" style="padding-left:60px">South Africa 2009</span>
						<br/>
						<span class="hint" style="padding-left:60px">Homo sapiens</span>
					</div>
				</div>
				<!--  -->
				<h3 class="section-title normal-case close2x">
					<span class="wrap">Comparative TB Tools</span>
				</h3>
				<div class="far2x">
					<h4 class="bold">Protein Family Sorter</h4>
					<div>Compare protein families across groups of Mtb genomes via visualization and multiple sequence alignments.</div>
					<div class="no-underline-links right">
						<a class="double-arrow-link" href="http://enews.patricbrc.org/faqs/protein-family-sorter/" target="_blank">Learn more</a>
						&nbsp;
					</div>
					<div class="clear"></div>
					<div class="right">
						<button class="button no-radius" onclick="launchPFS()">Launch Protein Family Sorter</button>
					</div>
					<div class="clear"></div>
					<hr/>
					
					<h4 class="bold">Comparative Pathway Tool</h4>
					<div>Compare consistently annotated metabolic pathways across closely related or diverse groups of Mtb genomes.</div>
					<div class="no-underline-links right">
						<a class="double-arrow-link" href="http://enews.patricbrc.org/faqs/comparative-pathway-tool-faqs/" target="_blank">Learn more</a>
						&nbsp;
					</div>
					<div class="clear"></div>
					<div class="right">
						<button class="button no-radius" onclick="launchCPT()">Launch Comparative Pathway Tool</button>
					</div>
					<div class="clear"></div>
				</div>
			</div>
			<div class='column' style='width:619px'>
				<h3 class="section-title normal-case close2x">
					<span class="wrap">Search TB @ PATRIC</span>
				</h3>
				<div class="far2x has-border" id="tb-home-proteins">
					<h4 class="bold">Find Genes/Proteins in</h4>
					<div style='padding-left:70px;'>
						<form action="#" onsubmit="return false;">
							<div class="left">
								<input type="radio" name="scope" value="83332" checked="checked"> H37Rv Reference Genome <br/>
								<input type="radio" name="scope" value="1773"> Mtb genomes (<%=genomes.get(1773) %> genomes) <br/>
								<input type="radio" name="scope" value="77643"> Mtb complex genomes (<%=genomes.get(77643) %> genomes) <br/>
							</div>
							<div class="left" style="margin:0px 0px 10px 30px;padding-left:5px;border-left:1px dashed #DFDFEF">
								<span class="bold">Include:</span><br/>
								<input type="checkbox" name="outgroup" value="246196.19"> M.smegmatis str. MC2 155 <br/>
								<input type="checkbox" name="outgroup" value="216594.6"> M.marinum M
							</div>
							<div class="clear"></div>
							<input type="text" id="keyword_feature" style="width:440px">
							<input type="submit" class="button right" value="Search" onclick="searchFeature()">
							<br/>
							<label>Examples:</label>
								<span class="hint">Rv0002</span>
								<span class="hint" style="padding-left:10px">dnaN</span>
								<span class="hint" style="padding-left:10px">DNA polymerase III beta subunit</span>
							
						</form>
						<div class="clear"></div>
					</div>
				</div>
				<div class="far2x has-border" id="tb-home-sp-genes">
					<h4 class="bold">Search for Specialty Genes:</h4>
					<div style='padding-left:70px;'>
						<form action="#" onsubmit="return false;">
							<div class="left" style="width: 230px">
								<input type="checkbox" name="property" value="Antibiotic Resistance"> Antibiotic Resistance<br/>
								<input type="checkbox" name="property" value="Drug Target"> Drug Target<br/>
								<input type="checkbox" name="property" value="Human Homolog"> Human Homolog<br/>
								<input type="checkbox" name="property" value="Virulence Factor"> Virulence Factor<br/>
							</div>
							<div class="left" style="margin:0px 0px 10px 25px;padding-left:5px;border-left:1px dashed #DFDFEF">
								<span class="bold">In:</span><br/>
								<input type="radio" name="scope" value="83332" checked="checked"> H37Rv Reference Genome <br/>
								<input type="radio" name="scope" value="1773"> Mtb genomes (<%=genomes.get(1773) %> genomes) <br/>
								<input type="radio" name="scope" value="77643"> Mtb complex genomes (<%=genomes.get(77643) %> genomes) <br/>
							</div>
							<div class="clear"></div>
							<input type="text" id="keyword_spgene" style="width:440px">
							<input type="submit" class="button right" value="Search" onclick="searchSpecialtyGene()">
							<br/>
						</form>
						<div class="clear"></div>
					</div>
				</div>
				<h3 class="section-title normal-case close2x">
					<span class="wrap">TB Omics Data</span>
				</h3>
				<div class="far2x">
					<div class="left has-border" id="tb-home-omics">
						<h4 class="left"><b>Browse</b> Expression Data&nbsp;&nbsp;</h4>
						<div style="line-height:16px">(<%=cntExperiments %> experiments)</div>
						<div class="clear"></div>
						<div class="left no-underline-links" style="width:230px;padding:10px 0px;">
							<a href="SingleExperiment?cType=taxon&cId=1763&eid=356112">GSE13978: Cholesterol's effect on M. tuberculosis</a>
							<hr/>
							<a href="SingleExperiment?cType=taxon&cId=1763&eid=471306">GSE11096: Role of M.tuberculsis dosS and dosT in CO sensing</a>
							<hr/>
							<a href="SingleExperiment?cType=taxon&cId=1763&eid=318920">GSE7539: A PhoP point mutation discriminates between the virulent H37Rv and avirulent H37Ra strains of Mycobacterium tuberculosis</a>
						</div>
						<img src="/patric/images/heatmap.png" alt="heatmap" class="right" style="padding-top:25px">
						<div class="clear"></div>
						<button class="button" onclick="location.href='ExperimentList?cType=taxon&amp;cId=1763';">Browse All Expression Data</button>
						<div class="clear"></div>
					</div>
					<div class="left has-border" id="tb-home-upload">
						<h4><b>Upload</b> your Expression Data into Workspace</h4>
						<br/>
						<img src="/patric/images/transcriptomics_uploader_ad.png" alt="transcriptomics uploader" width=90 height=90 class="right" onclick="launchTranscriptomicsUploader()" style="cursor: pointer;">
						<div style="width:140px">
							Leverage PATRIC's private &amp; secure workspace to analyze your Mtb data and 
							compare with other published datasets.
							<br/><br/>
						</div>
						<div class="no-underline-links">
							<a class="double-arrow-link" href="http://enews.patricbrc.org/faqs/transcriptomics-faqs/upload-transcriptomics-data-to-workspace-faqs/" target="_blank">Learn more</a>
							&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
							<a href="javascript:void(0)" onclick="launchTranscriptomicsUploader()">Upload now</a>
						</div>
						
						<div class="clear"></div>
					</div>
					<div class="clear"></div>
				</div>
			</div>
		</section>
	</section>
</div>
