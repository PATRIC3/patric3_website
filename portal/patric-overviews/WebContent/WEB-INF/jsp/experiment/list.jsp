<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" 
%><%@ page import="java.util.*" 
%><%

String cType = request.getParameter("context_type");
String cId = request.getParameter("context_id");

String keyword = (String) request.getAttribute("keyword");
String kw = (String) request.getAttribute("kw");
String eid = (String) request.getAttribute("eid");
String filter = (String) request.getAttribute("filter");
%>
<div style="display:none">
<form id="fTableForm" action="#" method="post">
<input type="hidden" id="tablesource" name="tablesource" value="GENEXP_Experiment" />
<input type="hidden" name="keyword" id="keyword" value="<%=keyword%>" />
<input type="hidden" name="filter" id="filter" value="<%=filter%>" />
<input type="hidden" name="cType" id="cType" value="<%=cType %>" />
<input type="hidden" name="cId" id="cId" value="<%=cId %>" />

<input type="hidden" id="aT" name="aT" value="" />
<input type="hidden" id="sort" name="sort" value="" />
<input type="hidden" id="pk" name="pk" value="" />

<!-- fasta download specific param -->
<input type="hidden" id="fileformat" name="fileformat" value=""/>
<input type="hidden" id="fids" name="fids" value="" />
<input type="hidden" id="download_keyword" name="download_keyword" value="" />
</form>
</div>
<div id="copy-button" class="x-hidden"></div>
<script type="text/javascript" src="/patric/js/vbi/AddToWorkspace.min.js"></script>
<script type="text/javascript" src="/patric/js/vbi/TranscriptomicsUpload.min.js"></script>
<script type="text/javascript">
var msgCt;
function launchTranscriptomicsUploader() {
    alert("In order to support new functionality in PATRIC, we have implemented a new workspace which includes a new form for upload of transcriptomics data.");

    // link to new app page (/app/Expression)
    document.location = "/app/Expression";
}
</script>
<div class="right no-underline-links" style="width:270px; font-size:11px;">
	<img alt="upload your transcriptomics data" src="/patric/images/transcriptomics_uploader_icon.png" onclick="launchTranscriptomicsUploader()" 
		class="left" style="cursor: pointer; padding:0px 10px 0px 20px" />
	<div id="uploader_div">
		<a href="javascript:void(0)" onclick="launchTranscriptomicsUploader()">Upload your transcriptomics data</a>
		<br/>to analyze using PATRIC tools and
		<br/>to compare with published datasets.
		<a href="http://enews.patricbrc.org/faqs/transcriptomics-faqs/upload-transcriptomics-data-to-workspace-faqs/" class="double-arrow-link" target="_blank">More</a>
	</div>
</div>

<p>
	The list below provides all of the available transcriptomics experiments and associated metadata 
	at this taxonomic level. The list of experiments can be filtered by metadata or keyword. 
	To learn more about PATRIC's transciptomics data and associated  metadata, 
	see <a href="http://enews.patricbrc.org/faqs/transcriptomics-faqs/transcriptomics-experiment-and-comparison-list-faqs" target=_blank>Transcriptomics User Guide</a>.
</p>

<div id="tree-panel" style="float:left"></div>
<div id="sample-layout" style="float:left"></div>
<div class="clear"></div>
<div id="information" class="table-container"  style="background-color:#DDE8F4;">
<table style="width:100%"><tr><td style="background-color:#DDE8F4; font-size:1em; line-height:1.2em; padding:6px 8px;text-align:left; border-bottom:0px; border-right:0px;">
	<div id="grid_result_summary"><b>Loading...</b></div>
</td></tr></table>
</div>

<script type="text/javascript" src="/patric-common/js/ZeroClipboard.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/copybutton.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/checkcolumn.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/pagingbar.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/toolbar.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/gridoptions.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/PATRICSelectionModel.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/PATRICGrid.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/table_checkboxes.js"></script>
<script type="text/javascript" src="/patric-common/js/parameters.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/loadgrid.js"></script>
<script type="text/javascript" src="/patric-common/js/createtree.js"></script>
<script type="text/javascript" src="/patric-searches-and-tools/js/experiment_list_grids.js"></script>
<script type="text/javascript" src="/patric-searches-and-tools/js/metadata.js"></script>
<script type="text/javascript" src="/patric-searches-and-tools/js/FacetTree.js"></script>
<script type="text/javascript" src="/patric-searches-and-tools/js/solrKeyword.js"></script>
<script type="text/javascript" src="/patric-searches-and-tools/js/json2.js" ></script>
<script type="text/javascript" src="/patric/js/vbi/AddToWorkspace.min.js"></script>
<script type="text/javascript">
//<![CDATA[
var $Page;

Ext.onReady(function()
{
	var checkbox = createCheckBox("GENEXP_Experiment");
	
	var pageProperties = {
		name: "GENEXP_Experiment",
		items: 2,
		cart: true,
		cartType: 'exp_list',
		scm: [[checkbox,
				{header:'Title',				flex:2, dataIndex: 'title',			renderer:renderExpTitle},
				{header:'Comparisons',			flex:1, dataIndex: 'samples',		align: 'center', renderer:renderComparisons},
				{header:'Genes',				flex:1, dataIndex: 'genes',			align: 'center', renderer:linkToGeneList},
				{header:'PubMed',				flex:1, dataIndex: 'pmid',			align: 'center', renderer:renderPubMed},
				{header:'Link Out',				flex:1, dataIndex: 'accession',		renderer:renderID}, 
				{header:'Organism',				flex:2, dataIndex: 'organism',		renderer:BasicRenderer, sortable: false}, 
				{header:'Strain',				flex:1, dataIndex: 'strain',		renderer:BasicRenderer, sortable: false},
				{header:'Gene Modification',	flex:1, dataIndex: 'mutant',		renderer:BasicRenderer, sortable: false},
				{header:'Experimental Condition', flex:1, dataIndex: 'condition',	renderer:BasicRenderer, sortable: false}, 
				{header:'Time Series',			flex:1, dataIndex: 'timeseries',	renderer:BasicRenderer},
				{header:'Release Date',			flex:1, dataIndex: 'release_date',	renderer: BasicRenderer}, 
				{header:'Author',				flex:1, dataIndex: 'author', 		hidden: true},
				{header:'PI',					flex:1, dataIndex: 'pi', 			hidden: true},
				{header:'Institution',			flex:1, dataIndex: 'institution', 	hidden: true}
			],
			[checkbox,
				{header: "Title",						flex:3, dataIndex: 'expname',	renderer:BasicRenderer},
				{header: "Genes",						flex:1, dataIndex: 'genes',		renderer: linkToGeneList, align: 'center'},
				{header: "Significant genes(Log Ratio)",flex:1, dataIndex: 'sig_log_ratio',	renderer: linkToGeneListFold, align: 'center'},
				{header: "Significant genes(Z Score)",	flex:1, dataIndex: 'sig_z_score',	renderer: linkToGeneListZScore, align: 'center'},
				{header: "PubMed",						flex:1, dataIndex: 'pmid',		renderer: renderPubMed},
				{header: "Link Out",					flex:1, dataIndex: 'accession',	renderer: renderID},
				{header: "Organism",					flex:1, dataIndex: 'organism',	renderer: BasicRenderer},
				{header: "Strain",						flex:1, dataIndex: 'strain',	renderer: BasicRenderer},
				{header: "Gene Modification",			flex:1, dataIndex: 'mutant',	renderer: BasicRenderer, align: 'center'},
				{header: "Experimental Condition",		flex:1, dataIndex: 'condition',	renderer:BasicRenderer},
				{header: "Time Point",					flex:1, dataIndex: 'timepoint',	renderer: BasicRenderer, align: 'center'},
				{header: "Release Date",				flex:1, dataIndex: 'release_date',	renderer: BasicRenderer}
			]],
		plugin:true,
		plugintype:"checkbox",	
		extraParams:getExtraParams,
		callBackFn:CallBack,
		sort: [[{
				property:"title",
				direction: "ASC"
			}],[{
				property:"expname",
				direction: "ASC"
			}]
		],
		hash:{
			aP: [1, 1],
			aT: 0,
			key:'',
			cwG: false,
			eId: "",
			eName: "",
			kW:'<%=kw%>'
		},
		remoteSort:true,
		model:["Experiment", "Sample"],
		tree: null,
		treeDS: null,
		fids: [],
		gridType: "ExpressionExperiment",
		current_hash: window.location.hash?window.location.hash.substring(1):"",
		url: ['/portal/portal/patric/ExperimentList/ExperimentListWindow?action=b&cacheability=PAGE'],
		loaderFunction: function(){loadFBCD();},
		stateId: ['experimentlist','comparisonlist'],
		pagingBarMsg: ['Displaying experiments {0} - {1} of {2}','Displaying comparisons {0} - {1} of {2}']
	};
	
	
	if ( "0" == "<%=eid%>") {
		Ext.getDom("grid_result_summary").innerHTML = "<b>No experiments found.</b>";
	} else {
		
		Ext.getDom("keyword").value = getOriginalKeyword(pageProperties.hash);
		
		Ext.Ajax.request({
			url: pageProperties.url[0],
			method: 'POST',
			params: {
				cType: "<%=cType%>",
				cId: "<%=cId%>",
				sraction: "save_params",
				keyword: Ext.getDom("keyword").value.trim() + (pageProperties.hash.kW?" AND " + pageProperties.hash.kW:"")
			},
			success: function(rs) {	
				pageProperties.hash.key = rs.responseText,
				SetPageProperties(pageProperties),
				$Page.checkbox = checkbox,
				createLayout(),
				loadFBCD(),
				$Page.doLayout(),
				// SetIntervalOrAPI(),
				Ext.QuickTips.init(),
				overrideButtonActions();
			}
		});
	}
	
	if (Ext.get("tabs_explist")) {
		Ext.get("tabs_explist").addCls("sel");
	}
	
});

function getOriginalKeyword(hash){
	
	var list_object ={};
	
	list_object["eid"] =  '<%=eid%>';
	
	if(hash && hash.kW != '')
		list_object["Keyword"] =  hash.kW;
		
	return constructKeyword(list_object, "GENEXP_Experiment");
}
// ]]>
</script>
