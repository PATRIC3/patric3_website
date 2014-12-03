<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" 
%><%@ page import="java.util.*" 
%><%@ page import="edu.vt.vbi.patric.dao.DBSearch"
%><%@ page import="edu.vt.vbi.patric.dao.ResultType" 
%><%
	String cType = request.getParameter("context_type");
	String cId = request.getParameter("context_id");
	String kw = (request.getParameter("keyword") != null)?request.getParameter("keyword"):"";
	if(kw != null && (kw.startsWith("/") || kw.startsWith("#"))){
		kw = "";
	}
	String pk = request.getParameter("param_key");
	if (pk == null)
		pk = "";

	DBSearch db_search = new DBSearch();

	String keyword = "(*)";
	String gid = "NA";
	String taxonId = "";
	if(cId.equals("2")){
		keyword= "(*)";
		gid = "";
	}
	else {
		if (cType.equals("taxon")) {
			gid = "";
			taxonId = cId;
		}
		else if (cType.equals("genome")) {
			gid = cId;
		}
	}

%>
<form id="fTableForm" action="#" method="post">
	<input type="hidden" id="tablesource" name="tablesource" value="SpecialtyGeneMapping" />
	<input type="hidden" name="keyword" id="keyword" value="<%=keyword%>" />
	<input type="hidden" name="cType" id="cType" value="<%=cType %>" />
	<input type="hidden" name="cId" id="cId" value="<%=cId %>" />
	<input type="hidden" name="gId" id="gId" value="" />
	<input type="hidden" id="aT" name="aT" value="" />
	<input type="hidden" id="sort" name="sort" value="" />
	<input type="hidden" id="dir" name="dir" value="" />

	<!-- fasta download specific param -->
	<input type="hidden" id="fastaaction" name="fastaaction" value="" />
	<input type="hidden" id="fastatype" name="fastatype" value="" />
	<input type="hidden" id="fastascope" name="fastascope" value="" />
	<input type="hidden" id="fids" name="fids" value="" />
	<input type="hidden" id="download_keyword" name="download_keyword" value="" />
	<input type="hidden" id="fileformat" name="fileformat" value=""/>
</form>
<div id="copy-button" class="x-hidden"></div>
<p>For this taxonomy/genome level, the list below provides Specialty Genes, i.e. genes that are of particular interest to the infectious disease researches, 
such as virulence factors, antibiotic resistance genes, drug targets, and human homologs. 
<b>Genes with the designation "Literature" in the Evidence field have been experimentally verified. Those with Evidence designated as "BLASTP" are identified based on sequence homology.</b>
For more details, see <a href="//enews.patricbrc.org/faqs/specialty-genes-faqs/" target="_blank">Specialty Genes FAQs</a>.</p>
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
<script type="text/javascript" src="/patric-searches-and-tools/js/specialty_gene_list_grids.js"></script>
<script type="text/javascript" src="/patric-searches-and-tools/js/FacetTree.js"></script>
<script type="text/javascript" src="/patric-searches-and-tools/js/solrKeyword.js"></script>
<script type="text/javascript" src="/patric-searches-and-tools/js/json2.js" ></script>

<script type="text/javascript">
//<![CDATA[
var $Page;

Ext.onReady(function () {

	var checkbox = createCheckBox("SpecialtyGeneMapping");
	var pageProperties = {
		name: "SpecialtyGeneMapping",
		items: 1,
		cart: true,
		cartType: 'cart',
		scm: [[checkbox,
			{header:'Evidence',				dataIndex:'evidence',			flex:1, align:'center'},
			{header:'Property',				dataIndex:'property', 			flex:2, align:'center', renderer:BasicRenderer},
			{header:'Source',				dataIndex:'source', 			flex:1, align:'center', renderer:renderSource},
			{header:'Genome Name',			dataIndex:'genome_name',		flex:2, renderer:renderGenomeName <%=cType.equals("genome")?", hidden:true":""%>},
			{header:'SEED ID',	        	dataIndex:'seed_id',       		flex:2, renderer:renderSeedId, align:'center'},
			{header:'RefSeq Locus Tag',		dataIndex:'refseq_locus_tag',	flex:1, align:'center', renderer:BasicRenderer},
			{header:'Alt Locus Tag',		dataIndex:'alt_locus_tag', 		flex:2, renderer:renderLocusTag, align:'center'},
			{header:'Source ID',			dataIndex:'source_id', 			flex:1, align:'center', renderer:renderSourceId},
			{header:'Source Organism',		dataIndex:'organism', 			flex:1, align:'center', renderer:BasicRenderer, hidden:true},
			{header:'Gene',					dataIndex:'gene', 				flex:1, align:'center', renderer:BasicRenderer},
			{header:'Product',				dataIndex:'product',			flex:2, align:'center', renderer:BasicRenderer},
			{header:'Function',				dataIndex:'function', 			flex:1, align:'center', renderer:BasicRenderer, hidden:true},
			{header:'Classification',		dataIndex:'classification', 	flex:1, align:'center', renderer:BasicRenderer, hidden:true},
			{header:'Pubmed',				dataIndex:'pmid', 				flex:1, renderer:renderPubMed, align:'center'},
			{header:'Subject Coverage',		dataIndex:'subject_coverage', 	flex:1, align:'center', hidden:true},
			{header:'Query Coverage',		dataIndex:'query_coverage', 	flex:1, align:'center', hidden:true},
			{header:'Identity',				dataIndex:'identity', 			flex:1, align:'center'},
			{header:'E-value',				dataIndex:'e_value',			flex:1, align:'center', sortable: false}]
		],
		plugin:true,
		plugintype:"checkbox",
		extraParams:getExtraParams,
		callBackFn:CallBack,
		sort: [[{
				property: "genome_name",
				direction: "ASC"
			}/*, {
				property: "alt_locus_tag",
				direction: "ASC"
			}*/]
		],
		hash:{
			aP: [1],
			aT: 0,
			key: "<%=pk%>",
			kW:'<%=kw%>',
			tId: "<%=taxonId%>"
		},
		remoteSort:true,
		model:["SpecialtyGeneMapping"],
		tree: null,
		treeDS: null,
		fids: [],
		gridType: "Feature",
		current_hash: window.location.hash?window.location.hash.substring(1):"",
		url: ['/portal/portal/patric/SpecialtyGeneSearch/SpecialtyGeneSearchWindow?action=b&cacheability=PAGE'],
		loaderFunction: function(){loadFBCD();},
		stateId: ['genelist'],
		pagingBarMsg: ['Displaying records {0} - {1} of {2}']
	};
	
	if('<%=gid%>' == 'NA'){
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
				params: {cType: '<%=cType%>',
					cId: '<%=cId%>',
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
		if (Ext.get("tabs_specialtygenes"))
			Ext.get("tabs_specialtygenes").addCls("sel");
	}
});

function getOriginalKeyword(hash){
	var obj_ = {}, filter = "", scope = "";
	
	if ('<%=gid%>' != 'NA' && '<%=gid%>' != '') {
		obj_["genome_id"] =  '<%=gid%>';
	}
	if (hash && hash.kW != '') {
		obj_["Keyword"] = hash.kW;
	}
	
	if (hash && hash.sbjCoverage != undefined && hash.sbjCoverage != '0') {
		if (filter.length > 0) {
			filter += ' AND subject_coverage:[' + hash.sbjCoverage +' TO 100]';
		} else {
			filter = 'subject_coverage:[' + hash.sbjCoverage +' TO 100]';
		}
	}
	if (hash && hash.qryCoverage != undefined && hash.qryCoverage != '0') {
		if (filter.length > 0) {
			filter += ' AND query_coverage:[' + hash.qryCoverage +' TO 100]';
		} else {
			filter = 'query_coverage:[' + hash.qryCoverage +' TO 100]';
		}
	}
	if (hash && hash.identity != undefined && hash.identity != '0') {
		if (filter.length > 0) {
			filter += ' AND identity:[' + hash.identity +' TO 100]';
		} else {
			filter = 'identity:[' + hash.identity +' TO 100]';
		}
	}
	if (hash && hash.scope != undefined && hash.scope != "All") {
		if (hash.scope == 'Genome') {
			scope = "same_genome:1";
		}
		else if (hash.scope == 'Species') {
			scope = "same_species:1";
		}
		else if (hash.scope == 'Genus') {
			scope = "same_genus:1";
		}
		//
		if (filter.length > 0) {
			filter += ' AND ' + scope;
		} else {
			filter = scope;
		}
	}
	if (filter.length > 0) {
		if (obj_["Keyword"] == null) {
			obj_["Keyword"] = filter;
		} else {
			obj_["Keyword"] += " AND " + filter;
		}
	}
	return constructKeyword(obj_, "SpecialtyGeneMapping");
}
// ]]>
</script>
