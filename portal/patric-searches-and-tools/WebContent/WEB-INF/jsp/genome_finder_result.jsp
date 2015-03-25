<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"
%><portlet:defineObjects/><%

String contextType = (String) request.getAttribute("contextType");
String contextId = (String) request.getAttribute("contextId");
String pk = (String) request.getAttribute("pk");
String taxonId = (String) request.getAttribute("taxonId");
String genomeId = (String) request.getAttribute("genomeId");
String keyword = (String) request.getAttribute("keyword");
String searchOn = (String) request.getAttribute("searchOn");
String exactSearchTerm = (String) request.getAttribute("exactSearchTerm");

%>
<form id="fTableForm" action="#" method="post">
	<input type="hidden" id="tablesource" name="tablesource" value="Genome" />
	<input type="hidden" name="keyword" id="keyword" value="<%=keyword%>" />
	<input type="hidden" name="search_on" value="<%=searchOn %>" />
	<input type="hidden" name="cType" id="cType" value="<%=contextType %>" />
	<input type="hidden" name="cId" id="cId" value="<%=contextId %>" />
	<input type="hidden" name="gId" id="gId" value="" />
	<input type="hidden" id="aT" name="aT" value="" />
	<input type="hidden" id="sort" name="sort" value="" />
	<input type="hidden" id="dir" name="dir" value="" />
	
	<!-- fasta download specific param -->
	<input type="hidden" id="fileformat" name="fileformat" value=""/>
	<input type="hidden" id="fids" name="fids" value="" />
	<input type="hidden" id="download_keyword" name="download_keyword" value="" />
</form>
<div id="copy-button"style="display:none;"></div>
<div style="padding:3px;">
	<input type="button" class="button leftarrow" id="search_modify" value="Modify Search Criteria" onclick="returntoSearchPage();"/> 
	<span class="showing_result_for">Showing results for: <b><%=exactSearchTerm %></b></span>
</div>

<div>
	To learn how to filter results based on metadata or perform an advanced search, 
		see <a href="http://enews.patricbrc.org/genome-finder-faqs/" target="_blank">Genome Finder FAQs</a>.
	To learn more about available metadata, see .<a href="http://enews.patricbrc.org/genome-metadata-faqs/" target="_blank">Genome Metadata FAQs</a>.
</div>
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
<script type="text/javascript" src="/patric-common/js/parameters.js"></script>
<script type="text/javascript" src="/patric-common/js/createtree.js"></script>
<script type="text/javascript" src="/patric-searches-and-tools/js/genome_finder_grids.js"></script>
<script type="text/javascript" src="/patric-searches-and-tools/js/FacetTree.js"></script>
<script type="text/javascript" src="/patric-searches-and-tools/js/solrKeyword.js"></script>
<script type="text/javascript" src="/patric-searches-and-tools/js/json2.js" ></script>
<script type="text/javascript" src="/patric/js/vbi/AddToWorkspace.min.js"></script>

<script type="text/javascript">
//<![CDATA[
var $Page;

Ext.onReady(function()
{
	var checkbox = createCheckBox("Genome");
	
	var pageProperties = {
		name: "Genome",
		items: 2,
		cart: true,
		cartType:'',
		scm: [[checkbox,
			{header:'Genome Name',	dataIndex:'genome_name',		flex:2, renderer:renderGenomeName},
			{header:'NCBI Taxon ID',	dataIndex:'ncbi_tax_id',		flex:1, hidden:true, align:'right'},
			{header:'Genome Status',	dataIndex:'genome_status',		flex:1, align:'center'},
			{header:'Genome Browser',	dataIndex:'genome_browser',		flex:1, sortable:false, align: 'center', renderer:renderGenomeBrowserByGenome},
			{header:'Size',			dataIndex:'genome_length',		flex:1, hidden:true, align:'right'},
			{header:'Chromosome',		dataIndex:'chromosomes',		flex:1, hidden:true, align:'center'},
			{header:'Plasmids',		dataIndex:'plasmids',			flex:1, hidden:true, align:'center'},
			{header:'Contigs',		dataIndex:'contigs',			flex:1, hidden:true, align:'center'},
			{header:'Sequences',		dataIndex:'sequences',			flex:1, align:'center', renderer:renderTotal},
			{header:'PATRIC CDS',		dataIndex:'patric_cds',			flex:1, align:'center', renderer:renderCDS_Count_RAST},
			{header:'BRC1 CDS',		dataIndex:'brc1_cds',			flex:1, hidden:true, align:'center', renderer:renderCDS_Count_BRC},
			{header:'RefSeq CDS',		dataIndex:'refseq_cds',			flex:1, hidden:true, align:'center', renderer:renderCDS_Count_RefSeq},
			{header:'Isolation Country',	dataIndex:'isolation_country',		flex:1, align:'center'},
			{header:'Host Name',		dataIndex:'host_name',			flex:1, align:'center'},
			{header:'Disease',		dataIndex:'disease',			flex:1, align:'center'},
			{header:'Collection Date',	dataIndex:'collection_date',		flex:1, align:'center'},
			{header:'Completion Date',	dataIndex:'completion_date',		flex:1, align:'center', renderer:renderCompletionDate},
			{header:'MLST', 		dataIndex:'mlst', 	flex:1, align:'center', renderer:BasicRenderer, hidden:true},
			{header:'Other Typing',		dataIndex:'other_typing', 		flex:1, align:'center', renderer:BasicRenderer, hidden:true},
			{header:'Strain',		dataIndex:'strain', 			flex:1, align:'center', hidden:true},
			{header:'Serovar',		dataIndex:'serovar',			flex:1, align:'center', hidden:true},
			{header:'Biovar',		dataIndex:'biovar',			flex:1, align:'center', hidden:true},
			{header:'Pathovar',		dataIndex:'pathovar',			flex:1, align:'center', hidden:true},
			{header:'Culture Collection',	dataIndex:'culture_collection',		flex:1, align:'center', hidden:true},
			{header:'Type Strain',		dataIndex:'type_strain',		flex:1, align:'center', hidden:true},
			{header:'Sequencing Center',	dataIndex:'sequencing_centers',		flex:1, align:'center', hidden:true},
			{header:'Publication', 		dataIndex:'publication',		flex:1, align:'center', hidden:true},
			{header:'BioProject Accession', dataIndex:'bioproject_accession',		flex:1, align:'center', hidden:true},
			{header:'BioSample Accession',  dataIndex:'biosample_accession',		flex:1, align:'center', hidden:true},
			{header:'Assembly Accession', 	dataIndex:'assembly_accession',		flex:1, align:'center', hidden:true},
			{header:'GenBank Accessions',	dataIndex:'genbank_accessions',		flex:1, align:'center', hidden:true},
			{header:'RefSeq Accessions',	dataIndex:'refseq_accessions',		flex:1, align:'center', hidden:true},
			{header:'Sequencing Platform',	dataIndex:'sequencing_platform',	flex:1, align:'center', hidden:true},
			{header:'Sequencing Depth',	dataIndex:'sequencing_depth',		flex:1, align:'center', hidden:true},
			{header:'Assembly Method',	dataIndex:'assembly_method',		flex:1, align:'center', hidden:true},
			{header:'GC Content',		dataIndex:'gc_content',			flex:1, align:'center', hidden:true},
			{header:'Isolation Site', 	dataIndex:'isolation_site',		flex:1, align:'center', hidden:true},
			{header:'Isolation Source', 	dataIndex:'isolation_source',		flex:1, align:'center', hidden:true},
			{header:'Isolation Comments',	dataIndex:'isolation_comments',		flex:1, align:'center', hidden:true},
			{header:'Geographic Location',	dataIndex:'geographic_location',	flex:1, align:'center', hidden:true},
			{header:'Latitude',		dataIndex:'latitude',			flex:1, align:'center', hidden:true},
			{header:'Longitude',		dataIndex:'longitude',			flex:1, align:'center', hidden:true},
			{header:'Altitude', 		dataIndex:'altitude',			flex:1, align:'center', hidden:true},
			{header:'Depth', 		dataIndex:'depth', 			flex:1, align:'center', hidden:true},
			{header:'Other Environmental',	dataIndex:'other_environmental',	flex:1, align:'center', renderer:BasicRenderer, hidden:true},
			{header:'Host Gender',		dataIndex:'host_gender',		flex:1, align:'center', hidden:true},
			{header:'Host Age', 		dataIndex:'host_age',			flex:1, align:'center', hidden:true},
			{header:'Host Health',		dataIndex:'host_health',		flex:1, align:'center', hidden:true},
			{header:'Body Sample Site',	dataIndex:'body_sample_site',		flex:1, align:'center', hidden:true},
			{header:'Body Sample Subsite',	dataIndex:'body_sample_subsite',	flex:1, align:'center', hidden:true},
			{header:'Other Clinical',   	dataIndex:'other_clinical',		flex:1, align:'center', hidden:true},
			{header:'Antimicrobial Resistance', dataIndex:'antimicrobial_resistance',   flex:1, align:'center', renderer:BasicRenderer, hidden:true},
			{header:'Antimicrobial Resistance Evidence',    dataIndex:'antimicrobial_resistance_evidence',  flex:1, align:'center', renderer:BasicRenderer, hidden:true},
			{header:'Gram Stain',		dataIndex:'gram_stain',			flex:1, align:'center', hidden:true},
			{header:'Cell Shape',		dataIndex:'cell_shape',			flex:1, align:'center', hidden:true},
			{header:'Motility',		dataIndex:'motility',			flex:1, align:'center', hidden:true},
			{header:'Sporulation',		dataIndex:'sporulation',		flex:1, align:'center', hidden:true},
			{header:'Temperature Range',	dataIndex:'temperature_range',		flex:1, align:'center', hidden:true},
			{header:'Optimal Temperature',	dataIndex:'optimal_temperature',	flex:1, align:'center', hidden:true},
			{header:'Salinity',		dataIndex:'salinity', 			flex:1, align:'center', hidden:true},
			{header:'Oxygen Requirement',	dataIndex:'oxygen_requirement',		flex:1, align:'center', hidden:true},
			{header:'Habitat',		dataIndex:'habitat',			flex:1, align:'center', hidden:true},
			{header:'Comments',		dataIndex:'comments',			flex:1, align:'center', renderer:BasicRenderer, hidden:true},
			{header:'Additional Metadata',	dataIndex:'additional_metadata',	flex:1, align:'center', renderer:BasicRenderer, hidden:true}],
			[checkbox,
			{header:"Genome Name",		dataIndex:'genome_name', 		flex:3, renderer:renderGenomeName},
			{header:"Accession",		dataIndex:'accession',			flex:1, renderer:renderAccession},
			{header:"Genome Browser",	dataIndex:'sequence_id',		flex:1, sortable:false, align:'center', renderer:renderGenomeBrowserBySequence},
			{header:"Length (bp)",		dataIndex:'length', 			flex:1, align:'right', renderer:BasicRenderer},
			{header:"GC Content (%)",	dataIndex:'gc_content',			flex:1, align:'center', renderer:BasicRenderer},
			{header:"Sequence Type",	dataIndex:'sequence_type',		flex:1, align:'center', renderer:BasicRenderer},
			{header:"Topology",		dataIndex:'topology',			flex:1, align:'center', renderer:BasicRenderer},
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
			gName: ""
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
		stateId: ['genomelist', 'sequencelist'],
		pagingBarMsg: ['Displaying genomes {0} - {1} of {2}','Displaying sequences {0} - {1} of {2}']
	};

	SetPageProperties(pageProperties),
	$Page.checkbox = checkbox,
	createLayout(),
	loadFBCD(),
	$Page.doLayout(),
	// SetIntervalOrAPI(),
	Ext.QuickTips.init(),
	overrideButtonActions();
});
<%--
function getOriginalKeyword(){
	return "<%=keyword%>";
}
--%>
function getOriginalKeyword(hash) {
	var genome_list_object = {};
	if(hash && hash.kW != '')
		genome_list_object["Keyword"] = hash.kW;

	return constructKeyword(genome_list_object, "Genome");
}
function returntoSearchPage(){
	var key = DecodeKeyword('<%=java.net.URLEncoder.encode(exactSearchTerm, "UTF-8") %>');
	document.location.href = "GenomeFinder?cType=<%=contextType%>&cId=<%=contextId%>&dm=#search_on=<%=searchOn%>&keyword="+key;
}

//]]>
</script>
