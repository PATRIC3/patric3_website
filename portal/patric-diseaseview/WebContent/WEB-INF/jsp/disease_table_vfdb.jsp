<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ page import="java.util.*" %>
<%
String cId = request.getParameter("context_id");
String cType = request.getParameter("context_type");
%>

<form id="fTableForm" action="#" method="post">
<input type="hidden" id="tablesource" name="tablesource" value="VFDBTable" />
<input type="hidden" id="cType" name="cType" value="<%=cType %>" />
<input type="hidden" id="cId" name="cId" value="<%=cId %>" />
<input type="hidden" id="sort" name="sort" value="" />
<input type="hidden" id="dir" name="dir" value="" />
<input type="hidden" id="VFGId" name="VFGId" value="" />

<!-- fasta download specific param -->

<input type="hidden" id="fastaaction" name="fastaaction" value="" />
<input type="hidden" id="fastatype" name="fastatype" value="" />
<input type="hidden" id="fastascope" name="fastascope" value="" />
<input type="hidden" id="fids" name="fids" value="" />
<input type="hidden" id="aT" name="aT" value="" />
<input type="hidden" id="fileformat" name="fileformat" value="" />
</form> 


<div id="SearchSummary" class="table-container">
<p>
	The Virulence Genes tab of this table lists bactertial genes that have been shown to play a role in virulence. 
	From here you can read general information about each gene, access the original entry in the Virulence Factor Database (VFDB), and find PATRIC homologs of a given gene. The Virulence Gene Homologs tab lists homologs of known virulence genes in related PATRIC species (i.e., organisms in the same genus). 
	From this tab you can access PATRIC features for each gene. For more information, see our <a href="http://enews.patricbrc.org/virulence-and-disease-faqs/" target="_blank">Virulence &amp; Disease FAQs</a>. 
</p>
</div>
<div id="grid_result_summary">Loading...</div>
<div id="copy-button" class="x-hidden"></div>
<div id="sample-layout"></div>
<script type="text/javascript" src="/patric-common/js/ZeroClipboard.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/copybutton.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/checkcolumn.js"></script>
<script type="text/javascript" src="/patric-common/js/parameters.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/loadgrid.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/pagingbar.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/toolbar.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/gridoptions.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/PATRICSelectionModel.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/PATRICGrid.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/table_checkboxes.js"></script>
<script type="text/javascript" src="/patric-diseaseview/js/diseaseview-grids.js"></script>
<script type="text/javascript" src="/patric-diseaseview/js/breadcrumb.js"></script>
<script type="text/javascript" src="/patric/js/vbi/AddToWorkspace.min.js"></script>

<script type="text/javascript">
//<![CDATA[
var $Page;

Ext.onReady(function () {

	Ext.define('VF', {
		extend: 'Ext.data.Model',
		fields: [
			{name:'vfg_id',			type:'string'},
			{name:'vf_id',			type:'string'},
			{name:'vf_name',		type:'string'},
			{name:'vf_fullname',	type:'string'},
			{name:'function',		type:'string'},
			{name:'gene_name',		type:'string'},
			{name:'gene_product',	type:'string'},
			{name:'gi_number',		type:'string'},
			{name:'organism',		type:'string'},
			{name:'feature_count',	type:'string'}
		]
	});
		
	Ext.define('VFFeature', {
		extend: 'Ext.data.Model',
		fields: [
			{name:'vfg_id',			type:'string'},
			{name:'gene_name',		type:'string'},
			{name:'vf_id',			type:'string'},
			{name:'vf_name',		type:'string'},
			{name:'genome_info_id',	type:'string'},
			{name:'genome_name',	type:'string'},
			{name:'accession',		type:'string'},
			{name:'locus_tag',		type:'string'},
			{name:'na_feature_id',	type:'string'},
			{name:'product',		type:'string'},
			{name:'genome_id',		type:'string'},
			{name:'feature_id',		type:'string'},
			{name:'patric_id',		type:'string'},
			{name:'refseq_locus_tag', type:'string'},
			{name:'alt_locus_tag',	type:'string'}
		]
	});
	
	
	var checkbox = createCheckBox("VFDB");
	
	var pageProperties = {
		name: "VFDB",
		model:["VF", "VFFeature"],
		items: 2,
		cart: true,
		cartType:'cart',
		plugin:true,
		plugintype:"checkbox",
		extraParams:getExtraParams,
		callBackFn:CallBack,
		remoteSort:true,
		scm :[[checkbox,
				{text:"VFG ID",						dataIndex:'vfg_id',			flex:1, align:'center', renderer:renderVFG},
				{text:"Gene Name",					dataIndex:'gene_name',		flex:1, align:'center', renderer:BasicRenderer},
				{text:"Gene Product",				dataIndex:'gene_product',	flex:2, renderer:BasicRenderer},
				{text:"VF ID",						dataIndex:'vf_id',			flex:1, align:'center', renderer:renderVF},
				{text:"VF Name",					dataIndex:'vf_name',		flex:2, renderer:BasicRenderer},
				{text:"VF Full Name",				dataIndex:'vf_fullname',	flex:2, renderer:BasicRenderer},
				{text:"VF Function",				dataIndex:'function',		flex:2, renderer:BasicRenderer},
				{text:"Virulence Gene Homologs",	dataIndex:'feature_count',	flex:1, align:'center', renderer:renderFeatureCount}
			],[checkbox,
				{text:"Genome Name",				dataIndex:'genome_name',	flex:2, renderer:renderGenomeNameVFDB},
				{text:"Accession",					dataIndex:'accession',		flex:1, hidden: true, renderer:renderAccession},
				{text:"PATRIC ID",					dataIndex:'patric_id',		flex:2, renderer:renderSeedId},
				{text:"RefSeq Locus Tag",			dataIndex:'refseq_locus_tag', flex:2, renderer:renderLocusTag},
				{text:"Alt Locus Tag",				dataIndex:'alt_locus_tag',	flex:2, renderer:renderLocusTag},
				{text:"Product Description",		dataIndex:'product',		flex:2, renderer:BasicRenderer},
				{text:"VFG ID",						dataIndex:'vfg_id',			flex:1, align:'center', renderer:renderVFG},
				{text:"Gene Name",					dataIndex:'gene_name',		flex:1, align:'center', renderer:BasicRenderer},
				{text:"VF ID",						dataIndex:'vf_id',			flex:1, align:'center', renderer:renderVF},
				{text:"VF Name",					dataIndex:'vf_name',		flex:2, renderer:BasicRenderer}
		]],
		sort: [[{
				property: 'gene_name', 
				direction: 'ASC'
			}],[{
				property: 'genome_name', 
				direction :'ASC'
			},{
				property: 'accession',
				direction: 'ASC'
			},{
				property: 'locus_tag',
				direction: 'ASC'
		}]],
		hash:{
			aP: [1, 1],
			aT: 0,
			cwVFG: false,
			VFGId: ""
		},
		fids: [],
		gridType: "Feature",
		current_hash: window.location.hash?window.location.hash.substring(1):"",
		url:[ '<portlet:resourceURL />'],
		loaderFunction: function(){loadFBCD();},
		stateId: ['DVgenes','DVhomologs']
	};
	
	SetPageProperties(pageProperties);
	$Page.checkbox = checkbox;
	createLayout();	
	loadFBCD(),
	$Page.doLayout();
	Ext.get("tabs_disease") && Ext.get("tabs_disease").addCls("sel");
	Ext.QuickTips.init(),
	overrideButtonActions();
	// SetIntervalOrAPI();
});
function getType(){
	var Page = $Page,
		property = Page.getPageProperties(),
		hash = property.hash;
		
	return hash.aT;
}

function getName(){
	return "";
}
//]]>
</script>
