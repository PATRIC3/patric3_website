<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" 
%><%
	String source = request.getParameter("sp_source");
	String kw = (request.getParameter("keyword") != null)?request.getParameter("keyword"):"";
	if(kw != null && (kw.startsWith("/") || kw.startsWith("#"))){
		kw = "";
	}
	String pk = request.getParameter("param_key");
	if (pk == null)
		pk = "";

	String keyword = "(*)";
	//
	String property = null;
	if (source.equals("PATRIC_VF") || source.equals("VFDB") || source.equals("Victors")) {
		property = "Virulance Factor";
	}
	else if (source.equals("Human")) {
		property = "Human Homolog";
	}
	else if (source.equals("TTD") || source.equals("DrugBank")) {
		property = "Drug Target";
	}
	else if (source.equals("CARD") || source.equals("ARDB")) {
		property = "Antibiotic Resistance";
	}
%>
<h3 class="section-title normal-case close2x"><span class="wrap">Specialty Genes &gt; <%=(property != null)?property: "" %> &gt; <%=(source != null)?source:"" %> </span></h3>

<form id="fTableForm" action="#" method="post">
<input type="hidden" id="tablesource" name="tablesource" value="SpecialtyGene" />
<input type="hidden" name="keyword" id="keyword" value="<%=keyword%>" />
<input type="hidden" id="aT" name="aT" value="" />
<input type="hidden" id="sort" name="sort" value="" />
<input type="hidden" id="pk" name="pk" value="<%=pk%>" />

<!-- fasta download specific param -->
<input type="hidden" id="fileformat" name="fileformat" value=""/>
<input type="hidden" id="fids" name="fids" value="" />
<input type="hidden" id="download_keyword" name="download_keyword" value="" />
</form>
<% if (source.equals("PATRIC_VF")) { %>
	<p>PATRIC_VF is a manually curated virulence factor database, which contains the genes identified as playing a role in 
		virulence in certain organisms.  Each PATRIC_VF gene is linked to one or more journal articles that establish its 
		virulence based on experimental evidence. For more details, please see <a href="http://enews.patricbrc.org/faqs/patric-curated-virulence-factors-faqs/" target="_blank">FAQs</a>.</p>
<% } else { %>
	<p>[Placeholder for Description]</p>
<% } %>
<div id="copy-button" class="x-hidden"></div>
<div id="tree-panel" style="float:left"></div>
<div id="sample-layout" style="float:left"></div>
<div class="clear"></div>
<div id="information" style="background-color:#DDE8F4; font-size:1em; line-height:1.2em; padding:6px 8px;text-align:left; border-bottom:0px; border-right:0px;">
	<div id="grid_result_summary"><b>Loading...</b><br/></div>
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
<script type="text/javascript" src="/patric-common/js/grid/loadgrid.js"></script>
<script type="text/javascript" src="/patric/js/vbi/AddToWorkspace.min.js"></script>
<script type="text/javascript" src="/patric-common/js/parameters.js"></script>
<script type="text/javascript" src="/patric-common/js/createtree.js"></script>
<script type="text/javascript" src="/patric-searches-and-tools/js/specialty_gene_source_grids.js"></script>
<script type="text/javascript" src="/patric-searches-and-tools/js/FacetTree.js"></script>
<script type="text/javascript" src="/patric-searches-and-tools/js/solrKeyword.js"></script>
<script type="text/javascript" src="/patric-searches-and-tools/js/json2.js" ></script>

<script type="text/javascript">
//<![CDATA[
var $Page;

Ext.onReady(function () {

	var checkbox = null;//createCheckBox("Feature");
	var pageProperties = {
		name: "SpecialtyGene",
		items: 1,
		cart: true,
		cartType: '',
		WoWorkspace: true,
		scm: [[/*checkbox, */
			{header:'Property',				dataIndex:'property', 			flex:1, align:'center', renderer:BasicRenderer, hidden:true},
			{header:'Source',				dataIndex:'source', 			flex:1, align:'center', renderer:BasicRenderer, hidden:true},
			{header:'Source ID',			dataIndex:'source_id', 			flex:1, align:'center', renderer:renderSourceId},
			{header:'Gene',					dataIndex:'gene_name', 			flex:1, align:'center', renderer:BasicRenderer},
			{header:'Organism',				dataIndex:'organism', 			flex:2, align:'center', renderer:BasicRenderer},
			{header:'Locus Tag',			dataIndex:'locus_tag', 			flex:1, align:'center', renderer:BasicRenderer},
			{header:'Gene ID',				dataIndex:'gene_id', 			flex:1, align:'center', renderer:BasicRenderer, hidden:true},
			{header:'GI',					dataIndex:'gi', 				flex:1, align:'center', renderer:BasicRenderer, hidden:true},
			{header:'Product',				dataIndex:'product',			flex:2, renderer:BasicRenderer},
			{header:'Function',				dataIndex:'function', 			flex:1, align:'center', renderer:BasicRenderer, hidden:true},
			{header:'Classification',		dataIndex:'classification', 	flex:1, align:'center', renderer:BasicRenderer, sortable: false},
			{header:'Pubmed',				dataIndex:'pmid', 				flex:1, renderer:renderPubMed, align:'center'},
			{header:'Homologs',				dataIndex:'homologs',			flex:1, renderer:renderHomologCount, align:'center', sortable: false}]
		],
		plugin: false,
		plugintype:"",
		extraParams:getExtraParams,
		callBackFn:CallBack,
		sort: [[{
				property: "source_id",
				direction: "ASC"
			}, {
				property: "locus_tag",
				direction: "ASC"
			}]
		],
		hash:{
			aP: [1],
			aT: 0,
			key: "<%=pk%>",
			kW:'<%=kw%>',
			source: '<%=source%>'
		},
		remoteSort:true,
		model:["SpecialtyGene"],
		tree: null,
		treeDS: null,
		fids: [],
		gridType: "Feature",
		current_hash: window.location.hash?window.location.hash.substring(1):"",
		url: ['/portal/portal/patric/SpecialtyGeneSource/SpecialtyGeneSourceWindow?action=b&cacheability=PAGE'],
		loaderFunction: function(){loadFBCD();},
		stateId: ['sp_source'],
		pagingBarMsg: ['Displaying records {0} - {1} of {2}']
	};
	
	if('<%=source%>' == ''){
		Ext.getDom("grid_result_summary").innerHTML = "<b>No record found.</b>";
	}
	else {
		Ext.getDom("keyword").value = getOriginalKeyword(pageProperties.hash);
		if (pageProperties.hash.key) {
			SetPageProperties(pageProperties),
			$Page.checkbox = checkbox,
			createLayout(),
			loadFBCD(),
			$Page.doLayout(),
			Ext.QuickTips.init(),
			overrideButtonActions();
		}
		else {
			Ext.Ajax.request({
				url: pageProperties.url[0],
				method: 'POST',
				params: {source: '<%=source %>',
					sraction: "save_params",
					keyword: Ext.getDom("keyword").value.trim() + (pageProperties.hash.kW?" AND "+pageProperties.hash.kW:""),
					exact_search_term: Ext.getDom("keyword").value.trim(),
					search_on: 'Keyword'
				},
				success: function(rs) {	
					pageProperties.hash.key = rs.responseText,
					SetPageProperties(pageProperties),
					$Page.checkbox = checkbox,
					createLayout(),
					loadFBCD(),
					$Page.doLayout(),
					Ext.QuickTips.init(),
					overrideButtonActions();
				}
			});
		}
	}
});

function getOriginalKeyword(hash){
	var obj_ = {};

	if (hash && hash.kW != '') {
		obj_["Keyword"] = hash.kW + ' AND source:<%=source%>';
	}
	else {
		obj_["Keyword"] = 'source: <%=source %>';
	}
	return constructKeyword(obj_, "SpecialtyGene");
}
// ]]>
</script>
