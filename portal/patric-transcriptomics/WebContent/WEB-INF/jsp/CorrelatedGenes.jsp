<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%
String cType = request.getParameter("context_type");
String cId = (request.getParameter("context_id")!=null)?request.getParameter("context_id"):"";
%>
<div class="far" id="PATRICGridFilter"></div>
<div id="grid_result_summary"></div>
<div id="PATRICGrid"></div>
<form id="fTableForm" action="#" method="post">
	<input type="hidden" id="cType" name="cType" value="<%=cType %>" />
	<input type="hidden" id="cId" name="cId" value="<%=cId %>" />
	<input type="hidden" id="featureId" name="featureId" value="<%=cId %>" />

	<input type="hidden" id="tablesource" name="tablesource" value="Correlation" />
	<input type="hidden" id="fileformat" name="fileformat" value="" />

	<input type="hidden" id="fastaaction" name="fastaaction" value="" />
	<input type="hidden" id="fastatype" name="fastatype" value="" />
	<input type="hidden" id="fastascope" name="fastascope" value="" />
	<input type="hidden" id="fids" name="fids" value="" />
	<input type="hidden" id="sort" name="sort" value="" />
	<input type="hidden" id="dir" name="dir" value="" />
	<input type="hidden" id="cutoffDir" name="cutoffDir" value="" />
	<input type="hidden" id="cutoffValue" name="cutoffValue" value="" />
</form>

<div id="copy-button" class="x-hidden"></div>
<script type="text/javascript" src="/patric-common/js/ZeroClipboard.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/copybutton.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/checkcolumn.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/pagingbar.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/toolbar.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/gridoptions.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/PATRICSelectionModel.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/PATRICGrid.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/table_checkboxes.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/loadMemGrid.js"></script>
<script type="text/javascript" src="/patric-common/js/parameters.js"></script>
<script type="text/javascript" src="/patric-searches-and-tools/js/solrKeyword.js"></script>
<script type="text/javascript" src="/patric/js/vbi/AddToWorkspace.min.js">
</script><script type="text/javascript" src="/patric/js/vbi/CorrelatedGenesFilter.js"></script>
<script type="text/javascript" src="/patric/js/extjs/extjs/examples/ux/data/PagingMemoryProxy.js"></script>
<script type="text/javascript">
//<![CDATA[
var $Page;

Ext.onReady(function()
{
	var checkbox = createCheckBox("Correlation");

	Ext.define('Correlation', {
		extend: 'Ext.data.Model',
		idProperty: 'feature_id',
		fields: [
			'genome_id', 'genome_name', 'accession', 'alt_locus_tag',
			'feature_id', {name:'start', type:'int'}, {name:'end', type:'int'},
			{name:'na_length', type:'int'}, {name:'correlation', type:'float'}, {name:'count', type:'int'},
			'strand', 'product', 'refseq_locus_tag', 'seed_id', 'gene', 'annotation', 'feature_type', 'protein_id', 'aa_length'
		]
	});
	
	var pageProperties = {
		name: "Correlation",
		model: ["Correlation"],
		items: 1,
		cart: true,
		cartType: "cart",
		plugin:true,
		plugintype:"checkbox",
		scm:[[checkbox,
				{text:'Genome Name',		dataIndex:'genome_name',		flex:3,	align:'center', renderer:renderGenomeName},
				{text:'Accession',			dataIndex:'accession',			flex:1,	hidden:true, align:'center', renderer:renderAccession},
				{text:'PATRIC ID',			dataIndex:'seed_id',	    	flex:2,	align:'center', renderer:renderSeedId},
				{text:'Alt Locus Tag',		dataIndex:'alt_locus_tag',		flex:2,	align:'center', renderer:renderLocusTag},
				{text:'RefSeq Locus Tag',	dataIndex:'refseq_locus_tag',	flex:2,	align:'center', renderer:renderLocusTag},
				{text:'Gene Symbol',		dataIndex:'gene',				flex:1,	align:'center', renderer:BasicRenderer},
				{text:'Annotation',			dataIndex:'annotation',			flex:1, hidden:true, renderer:BasicRenderer}, 
				{text:'Feature Type',		dataIndex:'feature_type',		flex:1, hidden:true, renderer:BasicRenderer},
				{text:'Start',				dataIndex:'start',  			flex:1,	hidden:true, align:'right'},
				{text:'End',				dataIndex:'end',	    		flex:1,	hidden:true, align:'right'},
				{text:'Length(NT)',			dataIndex:'na_length',			flex:1,	hidden:true, align:'right'},
				{text:'Strand',				dataIndex:'strand',				flex:1,	hidden:true, align:'center', renderer:BasicRenderer},
				{text:'Protein ID',			dataIndex:'protein_id',			flex:1, hidden:true, renderer:BasicRenderer}, 
				{text:'Length (AA)',		dataIndex:'aa_length',			flex:1, hidden:true, align:'right', renderer:BasicRenderer},
				{text:'Product Description',dataIndex:'product',			flex:3,	align:'left',	renderer:BasicRenderer},
				{text:'Correlation',		dataIndex:'correlation',		flex:1, align:'center', renderer:Ext.util.Format.numberRenderer('0.000')},
				{text:'Comparisons',		dataIndex:'count',				flex:1,	align:'right'}
			]],
		extraParams:getExtraParams,
		callBackFn:CallBack,
		sort: [[{
			property: 'correlation',
			direction: 'DESC'
		}]],
		hash:{
			aP: [1],
			cutoffDir:'positive',
			cutoffValue:'0.4'
		},
		remoteSort: true,
		fids: [],
		gridType: "Feature",
		current_hash: window.location.hash?window.location.hash.substring(1):"",
		url: ['/portal/portal/patric/TranscriptomicsGeneExp/TranscriptomicsGeneExpWindow?action=b&cacheability=PAGE'],
		loaderFunction: function(){loadFBCD();},
		stateId: ['correlated'],
		border: true,
		pagingBarMsg: ['Displaying features {0} - {1} of {2}']
	};
	
	SetPageProperties(pageProperties),
	$Page.checkbox = checkbox;
	SetIntervalOrAPI();
	Ext.QuickTips.init();
	Ext.get("tabs_correlated") && Ext.get("tabs_correlated").addCls("sel");
	overrideButtonActions();
	loadFBCD();
});

function loadFBCD() {

	SetLoadParameters();
	var hash = $Page.getPageProperties().hash;
	Ext.getCmp("PATRICGridFilterPanel").child("#cutoffValue").setValue( Math.abs(hash.cutoffValue));
	Ext.getCmp("PATRICGridFilterPanel").child("#cutoffDir").setValue(hash.cutoffDir);
	Ext.getDom("grid_result_summary").innerHTML = "<b>loading...</b>";
	loadMemStore(true);
}

function getExtraParams(){
	var Page = $Page,
		property = Page.getPageProperties(),
		hash = property.hash;
	
	return {
		featureId: Ext.getDom("featureId").value,
		storeType: 'correlation',
		cutoffValue: hash.cutoffValue,
		cutoffDir: hash.cutoffDir
	};
}

function CallBack(){
	var Page = $Page,
		property = Page.getPageProperties(),
		hash = property.hash,
		which = hash.aT?hash.aT:0;

	if(Page.getGrid().sortchangeOption)
		Page.getGrid().setSortDirectionColumnHeader();
	Ext.getDom("grid_result_summary").innerHTML = '<b>'+Page.getStore(which).getTotalCount()+' features found</b>';
}

function DownloadFile(type){
	"use strict";
	
	var form = Ext.getDom("fTableForm");
	
	form.action = "/patric-searches-and-tools/jsp/grid_download_handler.jsp",
	form.target = "",
	form.fileformat.value = arguments[0];
	getHashFieldsToDownload(form);
	form.submit();
};

function getSelectedFeatures() {
	"use strict";
	
	var Page = $Page,
		property = Page.getPageProperties(),
		sl = Page.getCheckBox().getSelections(),
		i,
		fids = property.fids;
	
	for (i=0; i<sl.length;i++) 
		fids.push(sl[i].data.feature_id);
};
//]]>
</script>