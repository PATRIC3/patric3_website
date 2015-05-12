<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ page import="java.util.List" %>
<%@ page import="edu.vt.vbi.patric.beans.Taxonomy" %>
<%
	String contextType = (String) request.getAttribute("contextType");
	String contextId = (String) request.getAttribute("contextId");

	List<Taxonomy> genusList = (List<Taxonomy>) request.getAttribute("genusList");
%>
<div id="SearchSummary">
<p>The Disease Overview presents infectious disease, virulence, and  
outbreak data associated with the current taxon, in three ways: the  
Summary associates MeSH disease terms and counts of associated  
pathogen and host genes with specific pathogens; the Disease-Pathogen Visualization tab  
presents relationships between pathogens, genes, and diseases in an  
interactive graph; and the Disease Map tab geolocates reports of  
associated diseases around the globe in real-time. For more  
information, see our <a href="http://enews.patricbrc.org/virulence-and-disease-faqs/" target="_blank">Virulence &amp; Disease FAQs</a>.
</p></div>

<div id="copy-button"></div>
<div id="TabTable"></div>
<div id="overview_container"></div>
<div id="overview-tree"></div>
<div id="diseaseview_pubmed"></div>
<div id="patric_idvgraph_graph" style="height:635px;  display:none;"></div>

<div id="graph_legend" style="display:none;">

<div id="idvg-legend-nodes" class="idvg-legend-block">
		<div class="idvg-legend-title">Nodes</div>
		<!-- disease -->
		<div class="idvg-legend-entry">
			<span class="idvg-legend-symbol">
				<img src="/patric/images/idvg-legend-imgs/idvg-node-disease.png" alt=""/>
			</span>
			<span class="idvg-legend-label">Disease</span>
		</div>
		<!-- pathogen -->
		<div class="idvg-legend-entry">
			<span class="idvg-legend-symbol">
				<img src="/patric/images/idvg-legend-imgs/idvg-node-pathogen.png" alt=""/>
			</span>
			<span class="idvg-legend-label">Pathogen</span>
			<br />
			<span class="idvg-legend-symbol">
				<img src="/patric/images/idvg-legend-imgs/idvg-node-pathogen-group.png" alt=""/>
			</span>
			<span class="idvg-legend-label">Group of pathogens</span>
		</div>
		<!-- virulence gene -->
		<div class="idvg-legend-entry">
			<span class="idvg-legend-symbol">
				<img src="/patric/images/idvg-legend-imgs/idvg-node-virulence-gene.png" alt=""/>
			</span>
			<span class="idvg-legend-label">Virulence gene</span>
			<br />
			<span class="idvg-legend-symbol">
				<img src="/patric/images/idvg-legend-imgs/idvg-node-virulence-gene-group.png" alt=""/>
			</span>
			<span class="idvg-legend-label">Group of virulence genes</span>
		</div>
		<!-- host gene -->
		<div class="idvg-legend-entry">
			<span class="idvg-legend-symbol">
				<img src="/patric/images/idvg-legend-imgs/idvg-node-host-gene.png" alt=""/>
			</span>
			<span class="idvg-legend-label">Host gene</span>
			<br />
			<span class="idvg-legend-symbol">
				<img src="/patric/images/idvg-legend-imgs/idvg-node-host-gene-group.png" alt=""/>
			</span>
			<span class="idvg-legend-label">Group of host genes</span>
		</div>
	</div>
	<br />
	<div class="idvg-legend-block" id="idvg-legend-edges">
		<div class="idvg-legend-title">Edges</div>
		<!-- hierarchy -->
		<div class="idvg-legend-entry">
			<div class="idvg-legend-entry-title">Hierarchies:</div>
			<span class="idvg-legend-symbol">
				<img src="/patric/images/idvg-legend-imgs/idvg-edge-hierarchy-mesh.png" alt=""/>
			</span>
			<span class="idvg-legend-label">MeSH</span>
			<br />
			<span class="idvg-legend-symbol">
				<img src="/patric/images/idvg-legend-imgs/idvg-edge-hierarchy-taxonomy.png" alt=""/>
			</span>
			<span class="idvg-legend-label">Taxonomy</span>
		</div>
		<!-- association -->
		<div class="idvg-legend-entry">
			<div class="idvg-legend-entry-title">Associations:</div>
			<span class="idvg-legend-symbol">
				<img src="/patric/images/idvg-legend-imgs/idvg-edge-association-pathogen.png" alt=""/>
			</span>
			<span class="idvg-legend-label">Virulence gene and pathogen</span>
			<br />
			<span class="idvg-legend-symbol">
				<img src="/patric/images/idvg-legend-imgs/idvg-edge-association-host.png" alt=""/>
			</span>
			<span class="idvg-legend-label">Host gene and disease</span>
			<br />
			<span class="idvg-legend-symbol">
				<img src="/patric/images/idvg-legend-imgs/idvg-edge-association-disease.png" alt=""/>
			</span>
			<span class="idvg-legend-label">Disease and pathogen</span>
		</div>
	</div>
</div>
<div id="graph_container"></div>


<div id="healthmap_container">
<% 
if (!genusList.isEmpty()) {

%>
<div id="tree_select_div" style="visibility:hidden">
	<p>The map below integrates outbreak data of varying reliability, ranging from news sources (such as Google News) to curated personal accounts (such as ProMED) to validated official alerts (such as World Health Organization). It is made available through a collaboration between PATRIC and <a href="http://healthmap.org/en/" target="_blank">HealthMap</a>.</p>
	<p>Select Genus 
		<select id="tree_select_list" onchange="selectChange()" >
		<%
			boolean isFirst = true;
			for (Taxonomy genus : genusList) { %>
				<option value="<%=genus.getId()%>" <%=(isFirst)?"selected='selected'":"" %>><%=genus.getTaxonName()%></option>
		<%
				isFirst = false;
			}
} %>
		</select>
	</p>	
</div>
</div>


<form action="#">
<input type="hidden" id="cType" name="cType" value="<%=contextType %>" />
<input type="hidden" id="cId" name="cId" value="<%=contextId %>" />
<input type="hidden" id="aT" name="aT" value="0" />
</form>
<script type="text/javascript" src="/patric-diseaseview/js/idv-shared.js"></script>
<script type="text/javascript" src="/patric-diseaseview/js/cytoscape/js/min/json2.min.js"></script>
<script type="text/javascript" src="/patric-diseaseview/js/cytoscape/js/min/AC_OETags.min.js"></script>
<script type="text/javascript" src="/patric-diseaseview/js/cytoscape/js/min/cytoscapeweb.min.js"></script>
<script type="text/javascript" src="/patric-diseaseview/js/IDVGraph.js"></script>
<script type="text/javascript" src="/patric-diseaseview/js/GraphOperations.js"></script>
<script type="text/javascript" src="/patric-diseaseview/js/diseaseview-grids.js"></script>
<script type="text/javascript">
//<![CDATA[
var overview_panel, graph_panel;
var tabs;
var overview_tree;
var g;
var tree_store;
var multiple_lines = 0;

var idvgraph;
var cursorX = 0;
var cursorY = 0;
var tooltip;
var topToolbar_graph;

var d = document.getElementById('healthmap_container'), d_child;

var d_select = document.getElementById('tree_select_div');

var newdiv = document.createElement('div');
newdiv.setAttribute('id', 'healthmap_map');

var grid_pubmed = null;

Ext.onReady(function(){

	Ext.define('Disease', {
		extend: 'Ext.data.Model',
		fields: [
			{name:'abbrAuthorList',	type:'string'},
			{name:'FullJournalName',	type:'string'},
			{name:'PubDate',	type:'string'},
			{name:'pubmed_id',	type:'string'},
			{name:'fullAuthorList',	type:'string'},
			{name:'Title',	type:'string'}
		]
	});
	
	Ext.define('DiseaseTree', {
		extend: 'Ext.data.Model',
		fields: [
			{name: 'disease_name',	type: 'string'},
			{name: 'pathogen',	type: 'string'},
			{name: 'genome',	type: 'string'},
			{name: 'vfdb',	type: 'string'},
			{name: 'gad',	type: 'string'},
			{name: 'ctd',	type: 'string'},
			{name: 'taxon_id',	type: 'string'},
			{name: 'disease_id',	type: 'string'},
		]
	});

	d.removeChild(d_select);
		
	createToolbar_Graph();
	
	tooltip = new Ext.Window({
		id: 'tip',
		width: 220,
		height: 150,
		html: '',
		draggable: true,
		closeAction:'hide',
		autoScroll:true
	});
	
	tabs = Ext.create('Ext.tab.Panel', {
		activeTab: 0,
		renderTo: 'TabTable',
		id:	'tabs', 
		enableTabScroll:true,
		height:22,
		border: true,
		layoutOnTabChange: true,
		forceFit: true,
		items: [{title:"Summary", id:"overview-tab"},{title:'Disease-Pathogen Visualization', id:'graph-tab'}, {title:"Disease Map", id:"healthmap-tab"}],
		listeners: {
			tabchange: function(){

				tooltip.setVisible(false);

				if(Ext.getCmp('tabs').getActiveTab().getId() == "healthmap-tab"){
					if(overview_panel != null){
						overview_panel.setVisible(false);
					}
					if(graph_panel != null){
						graph_panel.setVisible(false);
					}

					if(Ext.getDom('healthmap_map') == null){
						document.getElementById('healthmap_container').appendChild(d_select);
						document.getElementById('healthmap_container').appendChild(newdiv);

						Ext.getDom('tree_select_div').style.visibility = 'visible';

						updateTree(document.getElementById('tree_select_list').options[document.getElementById('tree_select_list').selectedIndex].value);

						Ext.getDom('healthmap_map').style.visibility = 'visible';
					}
					
				}else if(Ext.getCmp('tabs').getActiveTab().getId() == "overview-tab"){

					if(overview_panel != null)
						overview_panel.setVisible(true);

					d_child = document.getElementById('healthmap_map');
					d = document.getElementById('healthmap_container');
					if(d_child != null){
						d.removeChild(d_child);
						d.removeChild(d_select);
					}

					if(graph_panel != null)
						graph_panel.setVisible(false);

				}else if(Ext.getCmp('tabs').getActiveTab().getId() == "graph-tab"){

					graph_panel.setVisible(true);
					Ext.getDom("patric_idvgraph_graph").style.display = 'block';
					
					d_child = document.getElementById('healthmap_map');
					
					if(d_child != null){

						d.removeChild(d_child);
						d.removeChild(d_select);
					}
					
					if(overview_panel != null)
						overview_panel.setVisible(false);
					
					
				}
			}
		}
	});
	
	overview_panel = Ext.create('Ext.panel.Panel', {
		id: 'overview_panel',
		border: true,
		renderTo: 'overview_container',
		height:690,
		layout: 'border',
		items: [{
			region: 'center',
			id:'overview_center-panel',
			title: 'Infectious Disease Overview',
			border:false,
			contentEl: 'overview-tree',
			autoScroll: true
		},{
			region: 'west',
			id:'overview_west-panel',
			title: 'Recent PubMed Articles',
				border:false,
				contentEl: 'diseaseview_pubmed',
				width: 290,
				collapsed:false,
				collapsible:true
		}]
	});

	if (Ext.get("tabs_disease")!=null) {
		Ext.get("tabs_disease").addCls("sel");
	}

	loadPubMed();
	createOverviewTree();
	loadGraph();

	graph_panel = Ext.create('Ext.panel.Panel', {
		id: 'graph_panel',
		hidden:true,
		border: true,
		height:690,
		renderTo: 'graph_container',
		title: 'Virulence and Disease',
		layout:'border',
		cls:'overflow-x:hidden; overflow-y:hidden;',
		items: [{
			tbar: topToolbar_graph,
			region: 'center',
			id:'graph_center-panel',
			contentEl: 'patric_idvgraph_graph',
			border: false
		},{
			region: 'west',
			id: 'graph_west-panel',
			stateful: true,
			stateEvents: ['collapse','expand','resize'],
			stateId: 'DVLengendPanel',
			split: true,
			collapsible:true,
			width: 215,
			bodyStyle:'padding:2px; background-color:white;',
			title:'Legend',
			contentEl: 'graph_legend',
			border: false,
			listeners: {
				render :function(){
					this.getEl().setOpacity('.85');
					Ext.getDom("graph_legend").style.display = "block";
				}
			}
		}]
	});
	tabs.setActiveTab(parseInt(Ext.getDom("aT").value));
});


function loadGraph(){

	idvgraph = new edu.vt.vbi.patric.network.IDVGraph({
		caller: "idvgraph", 
		exportHandler: "/patric-diseaseview/jsp/graph_download_handler.jsp",
		swfPath: "/patric-diseaseview/js/cytoscape/swf/CytoscapeWeb", 
		flashInstallerPath: "/patric-diseaseview/js/cytoscape/swf/playerProductInstall"});
	
	getGraphData(Ext.getDom("cId").value);
}

function selectChange() {
	updateTree(document.getElementById('tree_select_list').options[document.getElementById('tree_select_list').selectedIndex].value);
}

function updateTree(tID) {
	Ext.Ajax.request({
		url: "/patric-diseaseview/jsp/get_healthmap_link.jsp",
		method: 'GET',
		params: {tId:tID},
		success: function(response, opts) {
			Ext.getDom('healthmap_map').innerHTML = "<br><div style=\"background-color: #FFFFFF\"><iframe id=\"healthmap\" src=\"http://healthmap.org/mapframe/?ps="+response.responseText+"\" width=\"100%\" height=\"770\" frameborder=\"0\"></iframe></div>"
		}
	});
}

function loadPubMed(){
	Ext.create('Ext.data.Store', {
		storeId: 'ds',
		model: 'Disease',
		proxy: {
			type: 'ajax',
			url:'/portal/portal/patric/Literature/PubMedWindow?action=b&cacheability=PAGE',
			noCache:false,
			timeout: 600000, //10*60*1000
			reader: {
				type:'json',
				root:'results',
				totalProperty:'total'
			},
			extraParams: {keyword:'Disease', date:'a', 'cType':Ext.getDom('cType').value, 'cId':Ext.getDom('cId').value}
		},
		autoLoad: false,
		pageSize: 6
	});
		
	grid_pubmed = Ext.create('Ext.grid.Panel', {
		cls:'preview',
		height:665,
		store: 'ds', // use the datasource
		columns: [{width:220, flex:1, header:'<a href=\"/portal/portal/patric/Literature?cType='+Ext.getDom("cType").value+'&cId='+Ext.getDom("cId").value+'&time=a&kw=Disease\">more articles... </a>', renderer:renderDiseaseOverviewPubMed}],
		viewConfig: {
			headersDisabled: true,
			forceFit:true
		},
		renderTo: "diseaseview_pubmed"
	});
	
	Ext.getStore('ds').loadPage(1);
}

function createOverviewTree(){

	tree_store = Ext.create('Ext.data.TreeStore', {
		model: 'DiseaseTree',
		autoLoad:true,
		proxy: {
			type: 'ajax',
			url:'<portlet:resourceURL />',
			noCache: false,
			extraParams: {type: "disease_tree", cId: Ext.getDom("cId").value}
		}
	});

	overview_tree = Ext.create('Ext.tree.Panel', {
		rootVisible:false,
		autoScroll:true,
		border:false,
		renderTo: 'overview-tree',
		store: tree_store,
		height:650,
		columns:[{
			xtype:'treecolumn',
			header:'MeSH Disease Terms</br>',
			width:260,
			dataIndex:'disease_name',
			renderer:renderMesHOverview,
			flex:1,
			sortable:false,
			hideable:false,
			menuDisabled:true
		},{
			header:'Pathogen</br>',
			width:260,
			dataIndex:'pathogen',
			renderer:renderGenomeName,
			align:'center',
			flex:1,
			sortable:false,
			hideable:false,
			menuDisabled:true
		},{
			header:'Genomes</br>',
			width:100,
			dataIndex:'genome',
			renderer:renderGenome,
			align:'center',
			flex:1,
			sortable:false,
			hideable:false,
			menuDisabled:true
		},{
			header:'<p style=\"line-height: 1.3;\">Pathogen Virulence Genes<br>source: <a href=\"http://www.mgc.ac.cn/VFs/main.htm\" target=\"_blank\">VFDB</a></p>',
			width:135,
			align: 'center',
			dataIndex:'vfdb',
			renderer:renderVFDB,
			flex:1,
			sortable:false,
			hideable:false,
			menuDisabled:true
		},{
			header:'<p style=\"line-height: 0.95;\">Human Disease Genes<br>(Genetic Association)<br>source: <a href=\"http://geneticassociationdb.nih.gov/\" target=\"_blank\">GAD</a></p>',
			align: 'center',
			width:140,
			dataIndex:'gad',
			renderer:renderGAD,
			flex:1,
			sortable:false,
			hideable:false,
			menuDisabled:true
		},{
			header:'<p style=\"line-height: 0.95;\">Human Disease Genes<br>(Chemical Association)<br>source: <a href=\"http://ctd.mdibl.org/\" target=\"_blank\">CTD</a></p>',
			width:140,
			align: 'center',
			dataIndex:'ctd',
			renderer:renderCTD,
			flex:1,
			sortable:false,
			hideable:false,
			menuDisabled:true
		}]
	});
}
//]]>
</script>