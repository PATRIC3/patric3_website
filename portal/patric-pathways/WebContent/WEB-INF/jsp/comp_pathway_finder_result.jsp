<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ page import="java.util.*" %>
<%@ page import="edu.vt.vbi.patric.dao.ResultType" %>
<%@ page import="javax.portlet.PortletSession" %>
<portlet:defineObjects/>
<%
String contextType = (String) request.getAttribute("contextType");
String contextId = (String) request.getAttribute("contextId");

String pk = (String) request.getAttribute("pk");
String searchOn = (String) request.getAttribute("searchOn");
String ecNumber = (String) request.getAttribute("ecNumber");
String annotation = (String) request.getAttribute("annotation");
String pathwayId = (String) request.getAttribute("pathwayId");
String keyword = (String) request.getAttribute("keyword");

String taxonId = (String) request.getAttribute("taxonId");
String genomeId = (String) request.getAttribute("genomeId");

%>
<form id="fTableForm" action="<portlet:resourceURL />&need=download" method="post">
<input type="hidden" id="tablesource" name="tablesource" value="CompPathwayFinder" />
<input type="hidden" id="pk" name="pk" value="<%=(pk!=null)?pk:"" %>" />
<input type="hidden" id="search_on" name="search_on" value="<%=(searchOn!=null)?searchOn:"" %>" />
<input type="hidden" id="keyword" name="keyword" value="<%=(keyword!=null)?keyword:"" %>"/>	
<input type="hidden" id="genomeId" name="genomeId" value="<%=(genomeId!=null)?genomeId:"" %>" />
<input type="hidden" id="taxonId" name="taxonId" value="<%=(taxonId!=null)?taxonId:"" %>" />
<input type="hidden" id="alg" name="alg" value="<%=annotation%>"/>
<input type="hidden" id="ecN" name="ecN" value="<%=ecNumber%>" />
<input type="hidden" id="pId" name="pId" value="" />
<input type="hidden" id="sort" name="sort" value="" />
<input type="hidden" id="dir" name="dir" value="" />
<!-- fasta download specific param -->
<input type="hidden" id="fastaaction" name="fastaaction" value="" />
<input type="hidden" id="fastatype" name="fastatype" value="" />
<input type="hidden" id="fastascope" name="fastascope" value="" />
<input type="hidden" id="fids" name="fids" value="" />
<input type="hidden" id="aT" name="aT" value="" />
<input type="hidden" id="fileformat" name="fileformat" value="" />

</form>
<div style="padding:3px;">

<input type="button" class="button leftarrow" id="search_modify" value="Modify Search Criteria" onclick="returntoSearchPage();">
</div>

<div id="copy-button" style="display:none"></div>
<div class="table-container">
<table width="100%"><tr><td>
	To learn how to filer, sort, manipulate, refine, and save data within PATRIC feature tables, 
	please see <a href="http://enews.patricbrc.org/feature-table-faqs/" target="_blank">Feature Table FAQs</a>.   
Click on a pathway name to view a pathway map.
</td></tr></table>
</div>   
    
    
<div id="sample-layout"></div>
<div id="PATRICGrid"></div>
</div>

<script type="text/javascript" src="/patric-common/js/ZeroClipboard.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/copybutton.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/checkcolumn.js"></script>
<script type="text/javascript" src="/patric-common/js/parameters.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/loadMemGrid.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/pagingbar.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/toolbar.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/gridoptions.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/PATRICSelectionModel.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/PATRICGrid.js"></script>    
<script type="text/javascript" src="/patric-common/js/grid/table_checkboxes.js"></script>
<script type="text/javascript" src="/patric-pathways/js/comp_pathway_grids.js"></script>
<script type="text/javascript" src="/patric-pathways/js/pathway_breadcrumb.js"></script>
<script type="text/javascript" src="/patric/js/vbi/AddToWorkspace.min.js"></script>
<script type="text/javascript" src="/patric/js/extjs/extjs/examples/ux/data/PagingMemoryProxy.js"></script>

<script type="text/javascript">
//<![CDATA[           

var $Page;

Ext.onReady(function()
{
	var checkbox = createCheckBox("CompPathwayTable");
	
	var pageProperties = {
		name: "CompPathwayTable",
		model:["Pathway", "Ec", "Feature"],
		items: 3,
		cart: true,
		cartType:'cart',
		plugin:true,
		plugintype:"checkbox",
		extraParams:getExtraParams,
		callBackFn:CallBack,
		border: true,
		scm :[[checkbox, {header:'Pathway ID',			dataIndex:'pathway_id',		flex:1, renderer:BasicRenderer},
						{header:'Pathway Name',			dataIndex:'pathway_name',	flex:2, renderer:renderPathwayName},
						{header:'Pathway Class',		dataIndex:'pathway_class',	flex:2, renderer:BasicRenderer},
						{header:'Annotation',			dataIndex:'algorithm',		flex:1, align:'center', renderer:BasicRenderer},
						{header:'Unique Genome Count',	dataIndex:'genome_count',	flex:1, align:'center', renderer:BasicRenderer},
						{header:'Unique Gene Count',	dataIndex:'gene_count',		flex:1, align:'center', renderer:renderGeneCountPathway},
						{header:'Unique EC Count',		dataIndex:'ec_count',		flex:1, align:'center', renderer:renderAvgECCount},
						{header:'EC Conservation',		dataIndex:'ec_cons',		flex:1, align:'center', renderer:BasicRenderer},
						{header:'Gene Conservation',	dataIndex:'gene_cons',		flex:1, align:'center', renderer:BasicRenderer}
				],[checkbox, {header:'Pathway ID',		dataIndex:'pathway_id',		flex:1, renderer:BasicRenderer},
						{header:'Pathway Name',		dataIndex:'pathway_name',	flex:2, renderer:renderPathwayEc},
						{header:'Pathway Class',	dataIndex:'pathway_class',	flex:2, renderer:BasicRenderer},
						{header:'Annotation',		dataIndex:'algorithm',		flex:1, align:'center', renderer:BasicRenderer},
						{header:'EC Number',		dataIndex:'ec_number',		flex:1, align:'center', renderer:BasicRenderer},
						{header:'Description',		dataIndex:'ec_name',		flex:2, renderer:BasicRenderer},
						{header:'Genome Count',		dataIndex:'genome_count',	flex:1, align:'center', renderer:BasicRenderer},
						{header:'Unique GeneCount',	dataIndex:'gene_count',		flex:1, align:'center', renderer:renderGeneCountEc}
				],[checkbox, {header:'Feature ID',		dataIndex:'na_feature_id', 	flex:1, hidden:true, renderer:BasicRenderer},  
			          {header:'Genome Name',	dataIndex:'genome_name', 	flex:1, renderer:renderGenomeName},
			    	  {header:'Accession',		dataIndex:'accession', 		hidden: true, flex:1, renderer:renderAccession},
			    	  {header:'SEED ID',    	dataIndex:'seed_id', 		flex:2, renderer:renderSeedId},
			    	  {header:'Alt Locus Tag',	dataIndex:'alt_locus_tag', 	flex:2, renderer:renderLocusTag},
			    	  {header:'Gene Symbol',	dataIndex:'gene',			flex:1, align:'center', renderer:BasicRenderer},
			    	  {header:'Product Name',	dataIndex:'product',		flex:2, renderer:BasicRenderer},
			    	  {header:'Annotation',		dataIndex:'algorithm', 		flex:1, align:'center', renderer:BasicRenderer},  	  
			    	  {header:'Pathway ID',		dataIndex:'pathway_id', 	flex:1, align:'center', renderer:BasicRenderer},
			    	  {header:'Pathway Name',	dataIndex:'pathway_name', 	flex:2, renderer:renderPathwayFeature},
			    	  {header:'EC Number',		dataIndex:'ec_number', 		flex:1, align:'center', renderer:BasicRenderer},
			          {header:'EC Description',	dataIndex:'ec_name', 		flex:2, renderer:BasicRenderer}
			      ]],
		pk:'<%=pk%>',
		sort:[[{
			property: 'pathway_id',
			direction: 'ASC'
		}],[{
			property: 'pathway_id',
			direction: 'ASC'
		},{
			property: 'ec_number',
			direction: 'ASC'
		}],[{
			property: 'genome_name',
			direction: 'ASC'
		},{
			property: 'seed_id',
			direction: 'ASC'
		}]],
		hash:{
			aP: [1, 1, 1],
			aT: 0,
			alg: "<%=annotation%>",
			cwEC: false,
			cwP: false,
			pId: "<%=pathwayId%>",
			ecN: "<%=ecNumber%>"
		},
		reconfigure:true,
		remoteSort:false,
		fids: [],
		pageType: "Finder",
		gridType: "Feature",
		breadcrumbParams:{},
		current_hash: window.location.hash?window.location.hash.substring(1):"",
		url:[ '<portlet:resourceURL />'],
		loaderFunction: function(){loadFBCD();},
		stateId: ['pathwaylist','pathwayeclist','PWfeaturelist'],
		pagingBarMsg: ['Displaying pathways {0} - {1} of {2}','Displaying records {0} - {1} of {2}','Displaying records {0} - {1} of {2}']
	};
	
	SetPageProperties(pageProperties);
	$Page.checkbox = checkbox;
	createLayout();
	loadFBCD(),
	$Page.doLayout(),
	// SetIntervalOrAPI(),
	Ext.QuickTips.init(),
	overrideButtonActions();
});

function returntoSearchPage(){

	if ('<%=searchOn%>' == 'Keyword')
		document.location.href = "PathwayFinder?cType=<%=contextType%>&cId=<%=contextId%>&dm=#search_on=<%=searchOn %>&keyword=<%=keyword%>&algorithm=<%=annotation%>";
	else if ('<%=searchOn%>' == 'Map_ID')
		document.location.href = "PathwayFinder?cType=<%=contextType%>&cId=<%=contextId%>&dm=#search_on=<%=searchOn %>&keyword=<%=pathwayId%>&algorithm=<%=annotation%>";
	else if ('<%=searchOn%>' == 'Ec_Number')
		document.location.href = "PathwayFinder?cType=<%=contextType%>&cId=<%=contextId%>&dm=#search_on=<%=searchOn %>&keyword=<%=ecNumber%>&algorithm=<%=annotation%>";
	else if ('<%=searchOn%>' == '')
		document.location.href = "PathwayFinder?cType=<%=contextType%>&cId=<%=contextId%>&dm=#search_on=Keyword&keyword=&algorithm=<%=annotation%>";
	
}
//]]>
</script>
