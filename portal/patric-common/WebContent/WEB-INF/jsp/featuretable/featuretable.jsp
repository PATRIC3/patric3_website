<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%
String taxonId = "";
String genomeId = "";

String cType = request.getParameter("context_type");
String cId = request.getParameter("context_id");

if (cType.equals("taxon")) {
	taxonId = cId;
} else if (cType.equals("genome")) {
	genomeId = cId;
}

String featuretype = request.getParameter("featuretype");
String annotation = request.getParameter("annotation");
String filtertype = request.getParameter("filtertype");

if (featuretype == null || featuretype.equals("")) { featuretype = "CDS"; }
if (annotation == null || annotation.equals("")) { annotation = "PATRIC"; }
if (filtertype == null || filtertype.equals("")) { filtertype = ""; }

%>
<form id="fTableForm" action="#" method="post">
	<input type="hidden" id="cType" name="cType" value="<%=cType %>" />
	<input type="hidden" id="cId" name="cId" value="<%=cId %>" />
	<input type="hidden" id="tablesource" name="tablesource" value="Feature" />
	<input type="hidden" id="fileformat" name="fileformat" value="" />
	<input type="hidden" id="taxonId" name="taxonId" value="<%=taxonId%>" />
	<input type="hidden" id="genomeId" name="genomeId" value="<%=genomeId%>" />
	<input type="hidden" id="sort" name="sort" value="" />
	<input type="hidden" id="dir" name="dir" value="" />
	
	<!-- fasta download specific param -->
	<input type="hidden" id="fastaaction" name="fastaaction" value="" />
	<input type="hidden" id="fastatype" name="fastatype" value="" />
	<input type="hidden" id="fastascope" name="fastascope" value="" />
	<input type="hidden" id="fids" name="fids" value="" />
	<input type="hidden" id="download_keyword" name="download_keyword" value="" />
</form>

<div id="copy-button" class="x-hidden"></div>
<div class="far">
	<p>
	Feature tables contain all of the identified features for all of the genomes in a particular genus.  
	Tables may be refined to show subsets of features via various user controls, as described in <a href="//enews.patricbrc.org/faqs/feature-table-faqs/" target="_blank">Feature Table FAQs</a>.
	</p>	
	<div id="panelFilter" style="display:block;">
		<div id="f_feature_type" style="float:left; padding:7px;"></div>
		<div id="f_annotation" style="float:left; padding:7px;"></div>
		<div id="f_keyword" style="float:left; padding:7px;"></div>
		<input class="button" type="button" value="Filter Table" onclick="filterFeatureTable()" style="margin: 5px;cursor: pointer;">
	</div>
</div>
<div class="clear"></div>
<div id="grid_result_summary"></div>
<div id='PATRICGrid'></div>
<script type="text/javascript" src="/patric-common/js/ZeroClipboard.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/copybutton.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/checkcolumn.js"></script>
<script type="text/javascript" src="/patric-common/js/parameters.js"></script>
<script type="text/javascript" src="/patric-common/js/featuretable.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/loadgrid.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/pagingbar.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/toolbar.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/gridoptions.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/PATRICSelectionModel.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/PATRICGrid.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/table_checkboxes.js"></script>
<script type="text/javascript" src="/patric/js/vbi/AddToWorkspace.min.js"></script>
<script type="text/javascript" src="/patric-searches-and-tools/js/solrKeyword.js"></script>

<script type="text/javascript">
//<![CDATA[ 
var $Page;
// Modernizr.history = false;
Ext.onReady(function()
{
	var checkbox = createCheckBox("Feature");
	var all_hidden = ["refseq_protein_id", "aa_length", "gene", "anticodon", "bound_moeity", "product"];
	var random = Math.floor(Math.random()*1000001);
	var pageProperties = {
		name: "Feature",
		model: ["Feature"],
		items: 1,
		cart: true,
		cartType: "cart",
		plugin:true,
		plugintype:"checkbox",
		scm:[[checkbox,
			{text:'Genome Name',		dataIndex:'genome_name',		orig_hidden_value: ("<%=cType%>" == "genome"),	hidden: ("<%=cType%>" == "genome")?true:false, flex:2,	renderer:renderGenomeName},
			{text:'Accession',			dataIndex:'accession',			orig_hidden_value: true,	hidden: true,	flex:1, renderer:renderAccession},
			{text:'SEED ID',			dataIndex:'seed_id',			orig_hidden_value: false,	flex:2, renderer:renderSeedId},
			{text:'RefSeq Locus Tag',	dataIndex:'refseq_locus_tag',	orig_hidden_value: false,	flex:2, renderer:BasicRenderer},
			{text:'Alt Locus Tag',		dataIndex:'alt_locus_tag',			orig_hidden_value: false,	flex:2, renderer:renderLocusTag},
			{text:'Gene Symbol',		dataIndex:'gene',				orig_hidden_value: false,	flex:1, renderer:BasicRenderer},
			{text:'Genome Browser',		dataIndex:'feature_id',		orig_hidden_value: true,	hidden: true,	align:'center', flex:1, sortable: false, renderer:renderGenomeBrowserByFeature},
			{text:'Annotation',			dataIndex:'annotation',			orig_hidden_value: true,	hidden: true,	flex:1, renderer:BasicRenderer},
			{text:'Feature Type',		dataIndex:'feature_type',		orig_hidden_value: true,	hidden: true,	flex:1, renderer:BasicRenderer}, 
			{text:'Start',				dataIndex:'start',			orig_hidden_value: true,	hidden: true,	flex:1, align:'right', renderer:BasicRenderer},
			{text:'End', 				dataIndex:'end', 			orig_hidden_value: true,	hidden: true,	flex:1, align:'right', renderer:BasicRenderer},
			{text:'Length (NT)',		dataIndex:'na_length',			orig_hidden_value: true,	hidden: true,	flex:1, align:'right', renderer:BasicRenderer},
			{text:'Strand',				dataIndex:'strand',				orig_hidden_value: true,	hidden: true,	flex:1, align:'right', renderer:BasicRenderer},
			{text:'Protein ID',			dataIndex:'protein_id',	orig_hidden_value: true,	hidden: true,	flex:1, align:'right', renderer:BasicRenderer},
			{text:'Length (AA)',		dataIndex:'aa_length',			orig_hidden_value: true,	hidden: true,	flex:1, align:'right', renderer:BasicRenderer},
			{text:'Product Description',dataIndex:'product',			orig_hidden_value: false,	flex:4, renderer:BasicRenderer}]],
		featureHiddenCols:{"CDS":[],
			"misc_RNA":["protein_id","aa_length","gene"],
			"misc_binding":["protein_id","aa_length","gene"],
			"misc_feature":["protein_id","aa_length","gene"],
			"misc_signal":["protein_id","aa_length","gene"],
			"ncRNA":["protein_id","aa_length"],
			"pseudogene":all_hidden,
			"rRNA":["protein_id","aa_length"],
			"region":all_hidden,
			"repeat_region":all_hidden,
			"source":all_hidden,
			"tRNA":["protein_id","aa_length","gene"],
			"tmRNA":all_hidden,
			"transcript":[],
			"ALL":["protein_id","aa_length"]},
		extraParams:getExtraParams,
		callBackFn:CallBack,
		sort: [[{
			property: 'genome_name',
			direction:'ASC'
			}/*,{
			property: 'seed_id',
			direction:'ASC'
		}*/]],
		hash:{
			aP: [1],
			fT: '<%=featuretype%>',
			alg: '<%=annotation%>',
			filter: '<%=filtertype%>',
			kW: '',
			key: random
		},
		remoteSort:true,
		fids: [],
		gridType: "Feature",
		current_hash: window.location.hash?window.location.hash.substring(1):"",
		url: ['/portal/portal/patric/GenomicFeature/GenomicFeatureWindow?action=b&cacheability=PAGE'],
		loaderFunction: function(){loadFBCD();},
		stateId: ['featurelist'],
		border: true,
		pagingBarMsg: ['Displaying features {0} - {1} of {2}']
	};
	
	SetPageProperties(pageProperties),
	$Page.checkbox = checkbox,
	Ext.QuickTips.init();
	overrideButtonActions(),
	loadFBCD(),
	$Page.doLayout(),
	SetIntervalOrAPI(),
	createLoadComboBoxes();
	if (Ext.get("tabs_featuretable")) {
		Ext.get("tabs_featuretable").addCls("sel");
	}
});

function getContext() {
	return {type:"<%=cType%>", id:<%=cId%>};
}
/*
function getGID(){
	return '<%=genomeId %>';
}*/
//]]
</script>
