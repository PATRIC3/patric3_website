<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" 
%><%@ page import="edu.vt.vbi.patric.dao.DBShared" 
%><%@ page import="edu.vt.vbi.patric.dao.ResultType" 
%><%@ page import="java.util.List" 
%><%
int taxonId = (Integer) request.getAttribute("taxonId");

String txUrlBase = "/portal/portal/patric/TaxonomyTree/TaxonomyTreeWindow?action=b&cacheability=PAGE&mode=txtree&taxonId=";
String txUrlAll = "/patric-common/txtree-bacteria.js";
String txUrl = "";
if (taxonId == 2) {
	txUrl = txUrlAll;
} else {
	txUrl = txUrlBase + taxonId;
}
%>
<div id="bacteria-tree" class="left"></div>
<div class="callout right" style="width:230px">
	If you know the taxonomy of your genomes of interest, you can follow this taxonomy tree (based on NCBI Taxonomy)
	and then use one of the links (eg., Sequence List) for more information at any taxonomic level.
	Alternatively, use our <a href="GenomeFinder?cType=taxon&amp;cId=&amp;dm=">Genome Finder</a> to search for specific genome(s) by name.
</div>
<div class="clear"></div>
<script type="text/javascript">
//<![CDATA[
var store, tree;

Ext.onReady(function(){
	Ext.define('Taxon', {
		extend: 'Ext.data.Model',
		fields: [
			{name: 'name',		type: 'string'},
			{name: 'node_count',type: 'int'},
			{name: 'leaf',		type: 'boolean'},
			{name: 'id',		type: 'int'},
			{name: 'rank',		type: 'string'}
		]
	});

	store = Ext.create('Ext.data.TreeStore', {
		model: 'Taxon',
		proxy: {
			type: 'ajax',
			url:'<%=txUrl %>',
			noCache: false,
			sortParam: false
		},
		sortParam: undefined,
		listeners: {
			load: function() {
				var r = this.getRootNode();
				r.firstChild.expand(false, false);
			}
		}
	});

	tree = Ext.create('Ext.tree.Panel', {
		width: 700,
		height: 600,
		renderTo: 'bacteria-tree',
		rootVisible: false,
		store: store,
		columns: [{
			xtype: 'treecolumn', //this is so we know which column will show the tree
			text: 'Name',
			flex: 2,
			dataIndex: 'name'
		}, {
			text: 'Rank',
			flex: 1,
			dataIndex: 'rank',
			align: 'center'
		}, {
			text: 'Genomes',
			flex: 1,
			dataIndex: 'node_count',
			align: 'center'
		}, {
			xtype: 'templatecolumn',
			text: 'Links',
			flex: 1,
			dataIndex: 'id',
			sortable: false,
			tpl: '<a href="Taxon?cType=taxon&cId={id}"><img src="/patric/images/icon_taxon.gif" alt="Taxonomy Overview" title="Taxonomy Overview" /></a>'
				+ ' <a href="GenomeList?cType=taxon&cId={id}&dataSource=&displayMode=&pk="><img src="/patric/images/icon_sequence_list.gif" alt="Genome List" title="Genome List" /></a>'
				+ ' <a href="FeatureTable?cType=taxon&cId={id}&annotation=&filtertype=&featuretype="><img src="/patric/images/icon_table.gif" alt="Feature Table" title="Feature Table" /></a>'
				+ ' <a href="Literature?cType=taxon&cId={id}"><img src="/patric/images/icon_pubmed.gif" alt="PubMed" title="PubMed" /></a>'
		}]
	});
});

Ext.onReady(function () {
	if (Ext.get("tabs_taxontree")!=null) {
		Ext.get("tabs_taxontree").addCls("sel");
	}
});
//]]>
</script>
