<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<portlet:defineObjects/>
<%
String contextType = (String) request.getAttribute("contextType");
String contextId = (String) request.getAttribute("contextId");
String pk = (String) request.getAttribute("pk");
String featureList = (String) request.getAttribute("featureList");
%>
<form id="fTableForm" action="#" method="post">
<input type="hidden" id="cType" name="cType" value="<%=contextType %>" />
<input type="hidden" id="cId" name="cId" value="<%=contextId %>" />
<input type="hidden" id="tablesource" name="tablesource" value="TranscriptomicsEnrichment" />
<input type="hidden" id="fileformat" name="fileformat" value="" />

<!-- fasta download specific param -->
<input type="hidden" id="fastaaction" name="fastaaction" value="" />
<input type="hidden" id="fastatype" name="fastatype" value="" />
<input type="hidden" id="fastascope" name="fastascope" value="" />
<input type="hidden" id="fids" name="fids" value="" />
<input type="hidden" id="featureList" name="featureList" value="<%=featureList %>" />
<input type="hidden" id="sort" name="sort" value="" />
<input type="hidden" id="dir" name="dir" value="" />
</form>

<div id="copy-button" style="display:none"></div>
<div id="grid_result_summary"></div>
<div id='PATRICGrid'></div>

<script type="text/javascript" src="/patric-common/js/ZeroClipboard.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/copybutton.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/checkcolumn.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/loadgrid.js"></script>
<script type="text/javascript" src="/patric-common/js/parameters.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/pagingbar.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/toolbar.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/gridoptions.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/PATRICSelectionModel.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/PATRICGrid.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/table_checkboxes.js"></script>
<script type="text/javascript" src="/patric/js/vbi/AddToWorkspace.min.js"></script>
<script type="text/javascript" src="/patric-transcriptomics/js/TranscriptomicsEnrichment.js"></script>
<script type="text/javascript">
//<![CDATA[

var $Page;

Ext.onReady(function () {
	
	Ext.define('Feature', {
		extend: 'Ext.data.Model',
		fields: [
			{name:'pathway_id',	type:'string'},
			{name:'pathway_name',	type:'string'},
			{name:'ocnt',	type:'string'},
			{name:'ecnt',	type:'string'},
			{name:'percentage',	type:'float'}
			]
		});
	var checkbox = createCheckBox("Enrichment");
	
	var pageProperties = {
		name: "Enrichment",
		model: ["Feature"],
		items: 1,
		cart: true,
		cartType:'cart',
		plugin:true,
		plugintype:"checkbox",
		extraParams:getExtraParams,
		callBackFn:CallBack,
		sort:[[{
			property: 'percentage',
			direction: 'DESC'
		}]],
		hash:{
			aP: [1],
			key:'<%=pk%>'
		},
		remoteSort: false,
		fids: [],
		gridType: "Feature",
		scm: [[checkbox,
				{header:'Pathway Name',	dataIndex:'pathway_name',  flex:2, renderer:renderPathwayEnrichment},
				{header:'# of Genes Selected', dataIndex:'ocnt', 	align:'center',	flex:1, renderer:BasicRenderer}, 
				{header:'# of Genes Annotated',dataIndex:'ecnt', 	align:'center',	flex:2, renderer:BasicRenderer}, 
				{header:'% Coverage',	dataIndex:'percentage', 	align:'center',	flex:2, renderer:BasicRenderer}
			]],
		current_hash: window.location.hash?window.location.hash.substring(1):"",
		url: ['<portlet:resourceURL />'],
		loaderFunction: function(){SetLoadParameters();loadGrid();}
	};

	SetPageProperties(pageProperties);
	$Page.checkbox = checkbox;
	// SetIntervalOrAPI();
	Ext.QuickTips.init();
	overrideButtonActions();
	loadGrid();
	$Page.doLayout();
});
// ]]
</script>
