<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"
%><portlet:defineObjects /><%
String contextType = (String) request.getAttribute("contextType");
String contextId = (String) request.getAttribute("contextId");
String genomeIds = (String) request.getAttribute("genomeIds");
String familyIds = (String) request.getAttribute("familyIds");
String familyType = (String) request.getAttribute("familyType");
String familyId = (String) request.getAttribute("familyId");
int length = (Integer) request.getAttribute("length");
String pk = (String) request.getAttribute("pk");
%>

<form id="fTableForm" action="#" method="post">
<input type="hidden" id="tablesource" name="tablesource" value="Feature" />
<input type="hidden" id="subtablesource" name="subtablesource" value="FigFam" />
<input type="hidden" id="fileformat" name="fileformat" value="" />
<input type="hidden" id="cType" name="cType" value="<%=contextType%>" />
<input type="hidden" id="cId" name="cId" value="<%=contextId%>" />
<input type="hidden" id="pk" name="pk" value="" />
<input type="hidden" id="sort" name="sort" value="" />
<!-- fasta download specific param -->
<input type="hidden" id="fastaaction" name="fastaaction" value="" />
<input type="hidden" id="fastatype" name="fastatype" value="" />
<input type="hidden" id="fastascope" name="fastascope" value="" />
<input type="hidden" id="fids" name="fids" value="" />
<input type="hidden" id="download_keyword" name="download_keyword" value="" />
</form>
<div id="copy-button" style="display:none"></div>
<div class="far">
	<div id="panelFilter" style="display:block;">
		<div id="f_keyword" style="float:left; padding:7px;"></div>
		<img src="/patric/images/filter_table.png" alt="Filter Table" onclick="filterTable()" style="cursor:pointer;vertical-align:middle; float:left; padding:7px;" />
	</div>
</div>
<div class="clear"></div>
<div id="grid_result_summary"></div>
<div id='PATRICGrid'></div>
<script type="text/javascript" src="/patric-common/js/ZeroClipboard.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/copybutton.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/checkcolumn.js"></script>
<script type="text/javascript" src="/patric-common/js/parameters.js"></script>
<script type="text/javascript" src="/patric-proteinfamily/js/DetailsGrid.js"></script>
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

Ext.onReady(function()
{
	var checkbox = createCheckBox("Feature");
	var pageProperties = {
		name: "Feature",
		model: ["Feature"],
		items: 1,
		cart: true,
		cartType: "cart",
		plugin:true,
		plugintype:"checkbox",
		scm:[[checkbox,
			{text:'ID',		            dataIndex:'<%=familyId%>',		hidden: ('<%=length%>' > 1)?false:true, flex:1,	renderer:BasicRenderer},
			{text:'Genome Name',		dataIndex:'genome_name',		hidden: false, flex:2,	renderer:renderGenomeName},
			{text:'Accession',			dataIndex:'accession',			hidden: true,	flex:1, renderer:renderAccession},
			{text:'PATRIC ID',			dataIndex:'patric_id',			flex:1, renderer:renderSeedId},
			{text:'RefSeq Locus Tag',	dataIndex:'refseq_locus_tag',	flex:1, renderer:renderLocusTag},
			{text:'Alt Locus Tag',		dataIndex:'alt_locus_tag',			flex:1, renderer:renderLocusTag},
			{text:'Gene Symbol',		dataIndex:'gene',		flex:1, renderer:BasicRenderer},
			{text:'Genome Browser',		dataIndex:''	,	hidden: true,	align:'center', flex:1, sortable: false, renderer:renderGenomeBrowserByFeature},
			{text:'Annotation',			dataIndex:'annotation',	hidden: true,	flex:1, renderer:BasicRenderer},
			{text:'Feature Type',		dataIndex:'feature_type',	hidden: true,	flex:1, renderer:BasicRenderer}, 
			{text:'Start',				dataIndex:'start',	hidden: true,	flex:1, align:'right', renderer:BasicRenderer},
			{text:'End', 				dataIndex:'end',	hidden: true,	flex:1, align:'right', renderer:BasicRenderer},
			{text:'Length (NT)',		dataIndex:'na_length',	hidden: true,	flex:1, align:'right', renderer:BasicRenderer},
			{text:'Strand',				dataIndex:'strand',	hidden: true,	flex:1, align:'right', renderer:BasicRenderer},
			{text:'Protein ID',			dataIndex:'protein_id',	hidden: true,	flex:1, align:'right', renderer:BasicRenderer},
			{text:'Length (AA)',		dataIndex:'aa_length',	hidden: true,	flex:1, align:'right', renderer:BasicRenderer},
			{text:'Product Description',	dataIndex:'product',	flex:3, renderer:BasicRenderer}]],
		extraParams:getExtraParams,
		callBackFn:CallBack,
		sort:[[{
			property: 'accession',
			direction:'ASC'
		},{
			property: 'start',
			direction:'ASC'
		}]],
		hash:{
			aP: [1],
			kW: '',
			pk: '<%=pk%>'
		},
		reconfigure:true,
		remoteSort:true,
		fids: [],
		gridType: "Feature",
		current_hash: window.location.hash?window.location.hash.substring(1):"",
		url: ['/portal/portal/patric/SingleFIGfam/SingleFIGfamWindow?action=b&cacheability=PAGE'],
		loaderFunction: function(){SetLoadParameters();loadGrid();},
		stateId: ['PFfeaturelist']
	};
	
	SetPageProperties(pageProperties),
	$Page.checkbox = checkbox,
	SetIntervalOrAPI(),
	Ext.QuickTips.init();
	if (Ext.get("tabs_proteinfamilysorter")) {
		Ext.get("tabs_proteinfamilysorter").addCls("sel");
	}
	createComboBoxes(),
	overrideButtonActions(),
	loadGrid(),
	$Page.doLayout();
});

function getGenomeIDs(){
	return '<%=genomeIds%>';
}
function getFamilyIds(){
	return '<%=familyIds%>';
}
function getFamilyType(){
	return '<%=familyType%>';
}
//]]>
</script>