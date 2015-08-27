<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ page import="java.util.*" %>
<%@ page import="edu.vt.vbi.patric.dao.DBSummary" %>
<%
String name = request.getParameter("name");
String cId = request.getParameter("cId");
String type = request.getParameter("type").split("/")[0];
%>

<form id="fTableForm" action="#" method="post">
<input type="hidden" id="tablesource" name="tablesource" value="DiseaseTable" />
<input type="hidden" id="fileformat" name="fileformat" value="" />
<input type="hidden" id="name" name="name" value="<%=name %>" />
<input type="hidden" id="cId" name="cId" value="<%=cId %>" />
<input type="hidden" id="type" name="type" value="<%=type %>" />
<input type="hidden" id="sort" name="sort" value="" />
<input type="hidden" id="dir" name="dir" value="" />
</form> 
<div class="table-container">
	<div id="grid_result_summary"></div>
	<p>This table lists human genes that have been shown to have some genetic association with the given infectious disease(s). From this table, you can get more information about the gene from NCBI or GeneCards, 
	access the original database entry in the Genetic Association Database (GAD), and read the published study that reported the association. For more information, see our <a href="http://enews.patricbrc.org/virulence-and-disease-faqs/" target="_blank">Virulence &amp; Disease User Guide</a>. </p>
</div>
<div id="copy-button" class="x-hidden"></div>
<div id='PATRICGrid'></div>
<script type="text/javascript" src="/patric-common/js/ZeroClipboard.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/copybutton.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/pagingbar.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/toolbar.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/gridoptions.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/PATRICSelectionModel.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/PATRICGrid.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/loadgrid.js"></script>
<script type="text/javascript" src="/patric-common/js/parameters.js"></script>
<script type="text/javascript" src="/patric-diseaseview/js/diseaseview-grids.js"></script>
<script type="text/javascript" src="/patric-diseaseview/js/breadcrumb.js"></script>

<script type="text/javascript">
//<![CDATA[
var $Page;

Ext.onReady(function () {
	Ext.define('GAD', {
		extend: 'Ext.data.Model',
		fields: [
			{name:'gene_id',	type:'string'},
			{name:'gene_sym',	type:'string'},
			{name:'association',	type:'string'},
			{name:'mesh_disease_terms',	type:'string'},
			{name:'broad_phenotype',	type:'string'},
			{name:'pubmed_id',		type:'string'},
			{name:'conclusion',		type:'string'},
			{name:'gd_app_name',		type:'string'}
		]
	});
	
	var pageProperties = {
		name: "GAD",
		model: ["GAD"],
		items: 1,
		cart: true,
		cartType: "",
		gridType: "",
		WoWorkspace: true,
		plugin:false,
		extraParams:getExtraParams,
		callBackFn:CallBack,
		sort: [[{
			property: 'gene_sym',
			direction: 'ASC'
		}]],
		hash:{
			aP: [1]
		},
		remoteSort:true,
		scm: [[
				{header: 'GAD Human Disease Gene', width:90, dataIndex: 'gene_sym', renderer:BasicRenderer},
				{header: "Gene Product", width:150, flex:1, dataIndex: 'gd_app_name', renderer:BasicRenderer},
				{header: "Disease Association", align:'center',width:110, dataIndex: 'association', renderer:renderAssociation},
				{header: "Reference", align:'center',width:60, dataIndex: 'pubmed_id', renderer:renderPubMed},
				{header: "Conclusion", width: 180, flex:1, dataIndex: 'conclusion', renderer:BasicRenderer},
				{header: "MeSH Disease Terms", width:180, flex:1, dataIndex: 'mesh_disease_terms', renderer:BasicRenderer},
				{header: "GAD Broad Phenotype", width:180, flex:1, dataIndex: 'broad_phenotype', renderer:BasicRenderer},
				{header: "XREF Link", align:'center',width:150, renderer:renderXREF}
			]],
		current_hash: window.location.hash?window.location.hash.substring(1):"",
		url: ['/portal/portal/patric/DiseaseTable/DiseaseTableWindow?action=b&cacheability=PAGE'],
		loaderFunction: function(){SetLoadParameters();loadGrid();},
		border: true
	};
	SetPageProperties(pageProperties),
	Ext.QuickTips.init();
	if (Ext.get("tabs_disease")) {
		Ext.get("tabs_disease").addCls("sel");
	}
	SetIntervalOrAPI(),
	loadGrid(),
	$Page.doLayout();
	
});


function getType(){
	return '<%=type%>';
}

function getName(){
	return '<%=name%>';
}
//]]>

</script>