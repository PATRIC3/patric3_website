<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" 
%><%@ page import="java.util.*"
%><%
	String cType = request.getParameter("context_type");
	String cId = request.getParameter("context_id");
	String algorithm = request.getParameter("data_source");
	String status = request.getParameter("display_mode");
	String kw = (request.getParameter("keyword") != null)?request.getParameter("keyword"):"";
	if(kw != null && (kw.startsWith("/") || kw.startsWith("#"))){
		kw = "";
	}
	String pk = request.getParameter("param_key");
	if (pk == null)
		pk = "";
	if (status == null)
		status = "";
	if (algorithm == null)
		algorithm = "";
	
	String keyword = "(*)";
	String gid = "NA";	
	String taxonId = "";

    if (cType.equals("taxon")) {
        gid = "";
        taxonId = cId;
    }
    else if (cType.equals("genome")) {
        gid = cId;
    }

%>
<div style="display:none">
	<form id="fTableForm" action="#" method="post">
	<input type="hidden" id="tablesource" name="tablesource" value="Genome" />
	<input type="hidden" name="keyword" id="keyword" value="<%=keyword%>" />
	<input type="hidden" name="cType" id="cType" value="<%=cType %>" />
	<input type="hidden" name="cId" id="cId" value="<%=cId %>" />
	<input type="hidden" name="gId" id="gId" value="" />
	<input type="hidden" id="aT" name="aT" value="" />
	<input type="hidden" id="sort" name="sort" value="" />
	<input type="hidden" id="dir" name="dir" value="" />
	
	<!-- fasta download specific param -->
	<input type="hidden" id="fileformat" name="fileformat" value=""/>
	<input type="hidden" id="fids" name="fids" value="" />
	<input type="hidden" id="download_keyword" name="download_keyword" value="" />
	</form>
</div>
<div id="copy-button" class="x-hidden"></div>
<p>
	The Genome List provides all of the available genomes and associated metadata at this taxonomic level. 
	To learn more about available metadata, see <a href="http://enews.patricbrc.org/genome-metadata-faqs/" target="_blank">Genome Metadata FAQs</a>.
</p>
<div id="tree-panel" class="left"></div>
<div id="sample-layout" class="left"></div>
<div class="clear"></div>
<div id="information" style="background-color:#DDE8F4; font-size:1em; line-height:1.2em; padding:6px 8px;text-align:left; border-bottom:0px; border-right:0px;">
	<div id="grid_result_summary"><b>Loading...</b><br/></div>
</div>
<%--
<style>
	.axis path,
	.axis line {
		fill: none;
		stroke: black;
		shape-rendering: crispEdges;
	}
	.axis text {
		font-family: sans-serif;
		font-size: 11px;
	}
	.test-text {
		fill: #ffffff;
	}
	svg {
		overflow: hidden;
	}
	#test-div {
		width: 300px;
		height: 300px;
		float: left;
	}
	#test-div-2 {
		width: 300px;
		height: 300px;
		float: left;
	}
	#chart-main {
		width: 900px;
		height: 570px;
		float: left;
		background-color: #bbbbbb;
	}
	#disease-div {
		width: 50%;
		height: 40%;
		float: left;
	}
	#host-name-div {
		width: 50%;
		height: 40%;
		float: right;
	}
	#isolation-country-div {
		width: 50%;
		height: 40%;
		float: left;
	}
	#genome-status-div {
		width: 50%;
		height: 40%;
		float: right;
	}
	#collection-date-div {
		width: 100%;
		height: 20%;
		float: left;
		background-color: #aaaaaa;
	}
	.contained {
		width: 100%;
		height: 85%;
	}
	.contained-no-toolbar {
		width: 100%;
		height: 100%;
	}
	.toolbar {
		width: 100%;
		height: 15%;
	}
	.title { margin-bottom: 0px; margin-top: 0px; margin-left: 5px; }
	.radio {
	}
	.absolute {
		position: absolute;
	}
	.second-text { float: left; }
	.third-text { float: left; }
	.second-radio { }
	.third-radio { }
</style>
<div id = "chart-main">
	<div id = "disease-div">
		<svg id = "disease" class = "contained"></svg>
	</div>
	<div id = "host-name-div">
		<svg id = "host-name" class = "contained"></svg>
	</div>
	<div id = "isolation-country-div">
		<svg id = "isolation-country" class = "contained"></svg>
	</div>
	<div id = "genome-status-div">
		<svg id = "genome-status" class = "contained"></svg>
	</div>
	<div id = "collection-date-div">
		<p class = 'absolute first'>Collection<br>Date</p>
		<svg id = "collection-date" class = "contained-no-toolbar"></svg>
	</div>
</div>

<script src="/patric/js/libs/d3.v3.min.js"></script>
<script type="text/javascript" src="/patric/js/genome-metadata/polished-graph-script.js" ></script>
<script type="text/javascript" src="/patric/js/genome-metadata/horizontal-bar-chart.js" ></script>
<script>
var graphSpaceDisease, graphSpaceHostName, graphSpaceIsolationCountry, graphSpaceGenomeStatus;
var color1 = { color: '#1f497d', weight: 1 };
var color2 = { color: '#4f81bd', weight: 1 };

$('#taxon-id-button').on('change', function(d) { 
	var id = parseInt($(this)[0].value, 10);
	getAjaxData(id,updateCharts);
	getCompletionDateData(id, initializeDateChart);
});

function updateCharts(data) {
	graphSpaceDisease.changeData(data);
	graphSpaceHostName.changeData(data);
	graphSpaceIsolationCountry.changeData(data);
	graphSpaceGenomeStatus.changeData(data);
}
var initializeCharts = function(data) {
	graphSpaceDisease = createGraphSpaceBarAndPie('disease', 'disease', 'Diseases', data);
	graphSpaceHostName = createGraphSpaceBarAndPie('host-name', 'host_name', 'Host Name', data);
	graphSpaceIsolationCountry = createGraphSpaceBarAndPie('isolation-country', 'isolation_country', 'Isolation Country', data);
	graphSpaceGenomeStatus = createGraphSpaceBarAndPie('genome-status', 'genome_status', 'Genome Status', data);
	
	graphSpaceDisease();
	graphSpaceHostName();
	graphSpaceIsolationCountry();
	graphSpaceGenomeStatus();
};
var initializeDateChart = function(data) {
	createFlexibleHorizontalBarChart("collection-date", data,
	{ 'segmented' : true,
	  'left'  : 75,
	  'top'   : 5,
	  'right' : 5,
	  'bottom': 1,
	  'colors': [color2, color1],
	  'bar-spacing' : 20,
	  'font-color':"#ebebeb",
	  'text-location' : 'bottom' //Working on a better way to create these than a list of parameters
	});
};
// var data = getAjaxData(234,initializeCharts);
// var completion_date_data = getCompletionDateData(234, initializeDateChart);

var data, completion_date_data;

function updateChart() {
	data = getAjaxData(<%=cId %>,initializeCharts);
	completion_date_data = getCompletionDateData(<%=cId%>, initializeDateChart);
}
</script>
--%>
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
<script type="text/javascript" src="/patric-searches-and-tools/js/genome_finder_grids.js"></script>
<script type="text/javascript" src="/patric-searches-and-tools/js/FacetTree.js"></script>
<script type="text/javascript" src="/patric-searches-and-tools/js/solrKeyword.js"></script>
<script type="text/javascript" src="/patric-searches-and-tools/js/json2.js" ></script>


<script type="text/javascript">
//<![CDATA[
var $Page;

Ext.onReady(function () {

	var checkbox = createCheckBox("Genome");
	var pageProperties = {
		name: "Genome",
		items: 2,
		cart: true,
		cartType: '',
		scm: [[checkbox,
			{header:'Organism Name',		dataIndex:'genome_name',		flex:2, renderer:renderGenomeName}, 
			{header:'NCBI Taxon ID',		dataIndex:'taxon_id',		flex:1, hidden:true, align:'right'},
			{header:'Genome Status',		dataIndex:'genome_status',		flex:1, align:'center'}, 
			{header:'Genome Browser',		dataIndex:'genome_browser', 	flex:1,	align:'center', sortable:false, renderer:renderGenomeBrowserByGenome},
			{header:'Size',					dataIndex:'genome_length',		flex:1, align:'right',  hidden:true},
			{header:'Chromosome',			dataIndex:'chromosomes',		flex:1, align:'center', hidden:true},
			{header:'Plasmids',				dataIndex:'plasmids',			flex:1, align:'center', hidden:true},
			{header:'Contigs',				dataIndex:'contigs',			flex:1, align:'center', hidden:true},
			{header:'Sequences',			dataIndex:'sequences',			flex:1, align:'center', hidden:true, renderer:renderTotal},
			{header:'PATRIC CDS',			dataIndex:'patric_cds',			flex:1, align:'center', renderer:renderCDS_Count_RAST},
			{header:'BRC1 CDS',	        	dataIndex:'brc1_cds',			flex:1, align:'center', hidden:true, renderer:renderCDS_Count_BRC},
			{header:'RefSeq CDS',			dataIndex:'refseq_cds',			flex:1, align:'center', hidden:true, renderer:renderCDS_Count_RefSeq},
			{header:'Isolation Country',	dataIndex:'isolation_country', 	flex:1, align:'center'},
			{header:'Host Name',			dataIndex:'host_name',			flex:1, align:'center'},
			{header:'Disease', 				dataIndex:'disease',			flex:1, align:'center'},
			{header:'Collection Date', 		dataIndex:'collection_date', 	flex:1, align:'center'},
			{header:'Completion Date', 		dataIndex:'completion_date', 	flex:1, align:'center', renderer:renderCompletionDate},
			{header:'MLST', 				dataIndex:'mlst', 				flex:1, align:'center', renderer:BasicRenderer, hidden:true},
			{header:'Other Typing',			dataIndex:'other_typing', 		flex:1, align:'center', renderer:BasicRenderer, hidden:true},
			{header:'Strain',				dataIndex:'strain', 			flex:1, align:'center', hidden:true},
			{header:'Serovar',				dataIndex:'serovar',			flex:1, align:'center', hidden:true},
			{header:'Biovar',				dataIndex:'biovar',				flex:1, align:'center', hidden:true},
			{header:'Pathovar',				dataIndex:'pathovar',			flex:1, align:'center', hidden:true},
			{header:'Culture Collection',	dataIndex:'culture_collection', flex:1, align:'center', hidden:true},
			{header:'Type Strain',			dataIndex:'type_strain',		flex:1, align:'center', hidden:true},
			{header:'Sequencing Center',	dataIndex:'sequencing_centers', flex:1, align:'center', hidden:true},
			{header:'Publication', 			dataIndex:'publication',		flex:1, align:'center', hidden:true},
			{header:'BioProject Accession', dataIndex:'bioproject_accession',	flex:1, align:'center', hidden:true},
			{header:'BioSample Accession',  dataIndex:'biosample_accession',	flex:1, align:'center', hidden:true},
			{header:'Assembly Accession', 	dataIndex:'assembly_accession',	flex:1, align:'center', hidden:true},
			{header:'GenBank Accessions',	dataIndex:'genbank_accessions',	flex:1, align:'center', hidden:true},
			{header:'RefSeq Accessions',	dataIndex:'refseq_accessions',	flex:1, align:'center', hidden:true},
			{header:'Sequencing Platform',	dataIndex:'sequencing_platform',flex:1, align:'center', hidden:true},
			{header:'Sequencing Depth',		dataIndex:'sequencing_depth',	flex:1, align:'center', hidden:true},
			{header:'Assembly Method',		dataIndex:'assembly_method',	flex:1, align:'center', hidden:true},
			{header:'GC Content',			dataIndex:'gc_content',			flex:1, align:'center', hidden:true},
			{header:'Isolation Site', 		dataIndex:'isolation_site',		flex:1, align:'center', hidden:true},
			{header:'Isolation Source', 	dataIndex:'isolation_source',	flex:1, align:'center', hidden:true},
			{header:'Isolation Comments',	dataIndex:'isolation_comments',	flex:1, align:'center', hidden:true},
			{header:'Geographic Location',	dataIndex:'geographic_location',flex:1, align:'center', hidden:true},
			{header:'Latitude',				dataIndex:'latitude',			flex:1, align:'center', hidden:true},
			{header:'Longitude',			dataIndex:'longitude',			flex:1, align:'center', hidden:true},
			{header:'Altitude', 			dataIndex:'altitude',			flex:1, align:'center', hidden:true},
			{header:'Depth', 				dataIndex:'depth', 				flex:1, align:'center', hidden:true},
			{header:'Other Environmental',	dataIndex:'other_environmental',flex:1, align:'center', renderer:BasicRenderer, hidden:true},
			{header:'Host Gender',			dataIndex:'host_gender',		flex:1, align:'center', hidden:true},
			{header:'Host Age', 			dataIndex:'host_age',			flex:1, align:'center', hidden:true},
			{header:'Host Health',			dataIndex:'host_health',		flex:1, align:'center', hidden:true},
			{header:'Body Sample Site',		dataIndex:'body_sample_site',	flex:1, align:'center', hidden:true},
			{header:'Body Sample Subsite',	dataIndex:'body_sample_subsite',flex:1, align:'center', hidden:true},
			{header:'Other Clinical',   	dataIndex:'other_clinical',     flex:1, align:'center', hidden:true},
			{header:'Antimicrobial Resistance', dataIndex:'antimicrobial_resistance',   flex:1, align:'center', renderer:BasicRenderer, hidden:true},
			{header:'Antimicrobial Resistance Evidence',    dataIndex:'antimicrobial_resistance_evidence',  flex:1, align:'center', renderer:BasicRenderer, hidden:true},
			{header:'Gram Stain',			dataIndex:'gram_stain',			flex:1, align:'center', hidden:true},
			{header:'Cell Shape',			dataIndex:'cell_shape',			flex:1, align:'center', hidden:true},
			{header:'Motility',				dataIndex:'motility',			flex:1, align:'center', hidden:true},
			{header:'Sporulation',			dataIndex:'sporulation',		flex:1, align:'center', hidden:true},
			{header:'Temperature Range',	dataIndex:'temperature_range',	flex:1, align:'center', hidden:true},
			{header:'Optimal Temperature',	dataIndex:'optimal_temperature',flex:1, align:'center', hidden:true},
			{header:'Salinity',				dataIndex:'salinity', 			flex:1, align:'center', hidden:true},
			{header:'Oxygen Requirement',	dataIndex:'oxygen_requirement',	flex:1, align:'center', hidden:true},
			{header:'Habitat',				dataIndex:'habitat',			flex:1, align:'center', hidden:true},
			{header:'Comments',				dataIndex:'comments',			flex:1, align:'center', renderer:BasicRenderer, hidden:true},
			{header:'Additional Metadata',	dataIndex:'additional_metadata', flex:1, align:'center', renderer:BasicRenderer, hidden:true}
		],
			[checkbox,
			{header:"Genome Name",		dataIndex:'genome_name', 		flex:3, renderer:renderGenomeName},
			{header:"Accession",			dataIndex:'accession',			flex:1, renderer:renderAccession},
			{header:"Genome Browser",		dataIndex:'sequence_id',			flex:1, align:'center', sortable:false, renderer:renderGenomeBrowserBySequence},
			{header:"Length (bp)",		dataIndex:'length', 			flex:1, align:'right', renderer:BasicRenderer},
			{header:"GC Content (%)",		dataIndex:'gc_content',			flex:1, align:'center', renderer:BasicRenderer},
			{header:"Sequence Type",		dataIndex:'sequence_type',		flex:1, align:'center', renderer:BasicRenderer},
			{header:"Topology",   		dataIndex:'topology',		    flex:1, align:'center', renderer:BasicRenderer},
			{header:"Description",		dataIndex:'description',		flex:2, renderer:BasicRenderer}]],
		plugin:true,
		plugintype:"checkbox",
		extraParams:getExtraParams,
		callBackFn:CallBack,
		sort: [[{
				property:"genome_name",
				direction: "ASC"
			}],[{
				property:"genome_name",
				direction: "ASC"
			}]
		],
		hash:{
			aP: [1, 1],
			aT: 0,
			key: "<%=pk%>",
			cwG: false,
			tId: "<%=taxonId%>",
			gName: "",
			kW:'<%=kw%>'
		},
		remoteSort:true,
		model:["Genome", "Sequence"],
		tree: null,
		treeDS: null,
		fids: [],
		gridType: "Genome",
		current_hash: window.location.hash?window.location.hash.substring(1):"",
		url: ['/portal/portal/patric/GenomeFinder/GenomeFinderWindow?action=b&cacheability=PAGE'],
		loaderFunction: function(){loadFBCD();},
		stateId: ['genomelist','sequencelist'],
		pagingBarMsg: ['Displaying genomes {0} - {1} of {2}','Displaying sequences {0} - {1} of {2}'],
		maxPageSize: 500
	};
	
	if('<%=gid%>' == 'NA'){
		Ext.getDom("grid_result_summary").innerHTML = "<b>No genomes found.</b>";
	}
	else {
		Ext.getDom("keyword").value = getOriginalKeyword(pageProperties.hash);
		if(pageProperties.hash.key){
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
		if (Ext.get("tabs_genomelist")) {
			Ext.get("tabs_genomelist").addCls("sel");
		}
	}
});

function getOriginalKeyword(hash){
	var genome_list_object = {};
	
	if('<%=gid%>' != 'NA' && '<%=gid%>' != '')
		genome_list_object["gid"] =  '<%=gid%>';
	if('<%=status%>' != '')
		genome_list_object["genome_status"] =  '<%=status%>';
	if('<%=algorithm%>' != '')
		genome_list_object["annotation"] =  '<%=algorithm%>';
	if(hash && hash.kW != '')
		genome_list_object["Keyword"] = hash.kW;
		
	return constructKeyword(genome_list_object, "Genome");
}

// ]]>
</script>
