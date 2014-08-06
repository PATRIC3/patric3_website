<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%
String name = request.getParameter("name");
String cId = request.getParameter("cId");
String type = request.getParameter("type");
%>
<form id="fTableForm" action="#" method="post">
<input type="hidden" id="tablesource" name="tablesource" value="DiseaseTable" />
<input type="hidden" id="fileformat" name="fileformat" value="" />
<input type="hidden" id="name" name="name" value="<%=name %>" />
<input type="hidden" id="cId" name="cId" value="<%=cId %>" />
<input type="hidden" id="type" name="type" value="<%=type %>" />
<input type="hidden" id="aP" name="aP" value="" />
<input type="hidden" id="pS" name="pS" value="" />
<input type="hidden" id="key" name="key" value="" />
<input type="hidden" id="sort" name="sort" value="" />
<input type="hidden" id="dir" name="dir" value="" />
<input type="hidden" id="sa" name="sa" value="load" />
</form> 
<div class="table-container">
	<div id="grid_result_summary"></div>
	<% if(type.equals("gad")) {%>
	<p>This table lists human genes that have been shown to have some genetic association with the given infectious disease(s). From this table, you can get more information about the gene from NCBI or GeneCards, 
	access the original database entry in the Genetic Association Database (GAD), and read the published study that reported the association. For more information, see our <a href="http://enews.patricbrc.org/virulence-and-disease-faqs/" target="_blank">Virulence &amp; Disease FAQs</a>. </p>
	<%}else {%>
	<p>This table lists human genes that have been shown to have some association with the given infectious disease(s) via chemical treatment or exposure. From this table, you can get more information about the gene from NCBI or GeneCards, access the original database entry in the Comparative Toxicogenomics Database (CTD), 
	and read the published study that reported the association. For more information, see our <a href="http://enews.patricbrc.org/virulence-and-disease-faqs/"  target="_blank">Virulence &amp; Disease FAQs</a>.</p> 
	<%} %>
</div>
<div id="copy-button" class="x-hidden"></div>
<div id='PATRICGrid'></div>
<script type="text/javascript" src="/patric-common/js/ZeroClipboard.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/copybutton.js"></script>
<script type="text/javascript" src="/patric-common/js/initialize.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/paging_controls.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/pagingbar.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/toolbar.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/gridoptions.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/PATRICSelectionModel.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/PATRICGrid.js"></script>
<script type="text/javascript" src="/patric-common/js/createurl.js"></script>
<script type="text/javascript" src="/patric-diseaseview/js/diseaseview-grids.js"></script>
<script type="text/javascript" src="/patric-diseaseview/js/breadcrumb.js"></script>

<script type="text/javascript">
//<![CDATA[
ZeroClipboard.setMoviePath('/patric-common/js/ZeroClipboard.swf');
var current_hash = "";
var name = "disease_table";

Ext.onReady(function () {

	setInterval( "check_hash()", 100 );
	
	if (Ext.get("tabs_disease")!=null) {
		Ext.get("tabs_disease").addCls("sel");
	}

	Ext.define('CTD', {
		extend: 'Ext.data.Model',
		fields: [
			{name:'gene_sym',	type:'string'},
			{name:'gene_id',	type:'string'},
			{name:'gene_disease_rel',	type:'string'},
			{name:'pubmed_id',	type:'string'},
			{name:'disease_id',	type:'string'},
			{name:'gd_app_name',	type:'string'},
			{name:'disease_name',		type:'string'}
		]
	});
	
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
	
	loadTable();
});

function loadTable(){

	Ext.getDom("name").value = "<%=name%>";

	initialize_key();

	if (Ext.getDom("type").value == "ctd" || Ext.getDom("type").value == "ctdgraph") {

		Ext.create('Ext.data.Store', {
			storeId: 'ds',
			model: 'CTD',
			proxy: {
				type: 'ajax',
				url:'<portlet:resourceURL/>',
				noCache:false,
				timeout: 600000, //10*60*1000
				reader: {
					type:'json',
					root:'results',
					totalProperty:'total'
				},
				extraParams: {name:Ext.getDom("name").value}
			},
			autoLoad: false,
			pageSize: Ext.getDom("pS").value,
			remoteSort:true,
			listeners:{
				datachanged: function(){
					writeBreadCrumb(Ext.getDom("type").value);	
					grid.setSortDirectionColumnHeader(Ext.getDom("sort").value, Ext.getDom("dir").value);
				}
			}
		});

		scm = [
			{header: 'CTD Human Disease Gene', width:110, dataIndex: 'gene_sym'},
			{header: "Gene Product", width:150, flex:1, dataIndex: 'gd_app_name'},
			{header: "Association Inferred By", flex:1,align:'center', width:150, dataIndex: 'gene_disease_rel'},
			{header: "Reference", width:70, align:'center', dataIndex: 'pubmed_id', renderer:renderPubMed},
			{header: "MeSH Disease Terms", align:'left', width: 150, dataIndex: 'disease_name', renderer:renderMesH},
			{header: "XREF Link", align:'center', width: 150, renderer:renderXREFCTD}
		];
		
	} else if(Ext.getDom("type").value == "gad" || Ext.getDom("type").value == "gadgraph") {

		Ext.create('Ext.data.Store', {
			storeId: 'ds',
			model: 'GAD',
			proxy: {
				type: 'ajax',
				url:'<portlet:resourceURL/>',
				noCache:false,
				timeout: 600000, //10*60*1000
				reader: {
					type:'json',
					root:'results',
					totalProperty:'total'	
				},
				extraParams: {name:Ext.getDom("name").value}
			},
			autoLoad: false,
			pageSize: Ext.getDom("pS").value,
			remoteSort:true,
			sorters:getSorters(),
			listeners:{
				datachanged: function(){
					writeBreadCrumb(Ext.getDom("type").value);
					grid.setSortDirectionColumnHeader(Ext.getDom("sort").value, Ext.getDom("dir").value);
				}
			}
		});

		scm = [
			{header: 'GAD Human Disease Gene', width:90, dataIndex: 'gene_sym', renderer:BasicRenderer},
			{header: "Gene Product", width:150, flex:1, dataIndex: 'gd_app_name', renderer:BasicRenderer},
			{header: "Disease Association", align:'center',width:110, dataIndex: 'association', renderer:renderAssociation},
			{header: "Reference", align:'center',width:60, dataIndex: 'pubmed_id', renderer:renderPubMed},
			{header: "Conclusion", width: 180, flex:1, dataIndex: 'conclusion', renderer:BasicRenderer},
			{header: "MeSH Disease Terms", width:180, flex:1, dataIndex: 'mesh_disease_terms', renderer:BasicRenderer},
			{header: "GAD Broad Phenotype", width:180, flex:1, dataIndex: 'broad_phenotype', renderer:BasicRenderer},
			{header: "XREF Link", align:'center',width:150, renderer:renderXREF, renderer:renderXREF}
		];
	}
		
	if (grid == null) {

		grid = Ext.create('Ext.grid.PATRICGrid',{
			store: 'ds',
			columns: scm,
			tbar: createToolbar("","table_wo_workspace",""),
			viewConfig: {forceFit:true},
			dockedItems: [{
				dock: 'bottom',
				xtype: 'patricpagingtoolbar',
				store: Ext.getStore('ds'),
				gridType: name,
				id: 'pagingtoolbar'
			}],
			renderTo:'PATRICGrid',
			border: true
		});
	} else {
		grid.reconfigure('ds');
	}
	
	Ext.getStore('ds').loadPage(parseInt(Ext.getDom("aP").value));
	createURL(name);
	current_hash = window.location.hash;
	Ext.EventManager.onWindowResize(grid.doLayout, grid);
}

function check_hash() {
	if ( window.location.hash != current_hash ) {
		loadTable();
	}
}

function DownloadFile(type){
	Ext.getDom("fTableForm").action = "/patric-diseaseview/jsp/table_download_handler.jsp";
	Ext.getDom("fileformat").value = type;
	Ext.getDom("fTableForm").submit();
}
//]]>
</script>