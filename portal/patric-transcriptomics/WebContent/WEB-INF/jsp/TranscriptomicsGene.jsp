<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ page import="javax.portlet.PortletSession" %>
<%@ page import="edu.vt.vbi.patric.dao.ResultType" %>
<portlet:defineObjects/>
<%
String name = "TranscriptomicsGene";
String windowID = renderRequest.getWindowID();
String resourceURL = (renderResponse.createResourceURL()).toString();
String contextPath = renderResponse.encodeURL(renderRequest.getContextPath());

String contextType = (String) request.getAttribute("contextType");
String contextId = (String) request.getAttribute("contextId");
String expId = (String) request.getAttribute("expId");
String sampleId = (String) request.getAttribute("sampleId");
String wsExperimentId = (String) request.getAttribute("wsExperimentId");
String wsSampleId = (String) request.getAttribute("wsSampleId");
String log_ratio = (String) request.getAttribute("log_ratio");
String zscore = (String) request.getAttribute("zscore");
String keyword = (String) request.getAttribute("keyword");
%>

<script type="text/javascript" src="/patric-transcriptomics/js/namespace.js"></script>

<script type="text/javascript" src="/patric-transcriptomics/js/TranscriptomicsGene.js"></script>

<script type="text/javascript" src="/patric-transcriptomics/js/TranscriptomicsGrid.js"></script>
<script type="text/javascript" src="/patric-transcriptomics/js/TranscriptomicsGridState.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/gridoptions.js"></script>
<script type="text/javascript" src="/patric-transcriptomics/js/HashTracker.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/checkcolumn.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/table_checkboxes.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/pagingbar.js"></script>
<script type="text/javascript" src="/patric-common/js/heatmap/heatmapDatatypes.js"></script>
<script type="text/javascript" src="/patric-transcriptomics/js/heatmap/loadHeatmap.js"></script>
<script type="text/javascript" src="/patric-transcriptomics/js/heatmap/heatmapMediator.js"></script>
<script type="text/javascript" src="/patric-common/js/heatmap/swfobject.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/toolbar.js"></script>
<div id="copy-button"  style="display: none"></div>

<script type="text/javascript" src="/patric-common/js/ZeroClipboard.js"></script>
<script type="text/javascript" src="/patric-common/js/parameters.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/copybutton.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/PATRICSelectionModel.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/PATRICGrid.js"></script>
<script type="text/javascript" src="/patric/js/vbi/AddToWorkspace.min.js"></script>

<form id="<%=name%>_fTableForm" action="#" method="post">
<input type="hidden" id="tablesource" name="tablesource" value="FeatureTable" />
<input type="hidden" id="fileformat" name="fileformat" value="" />
<!-- fasta download specific param -->
<input type="hidden" id="fastaaction" name="fastaaction" value="" />
<input type="hidden" id="fastatype" name="fastatype" value="" />
<input type="hidden" id="fastascope" name="fastascope" value="" />
<input type="hidden" id="fids" name="fids" value="" />
</form>
<form id="fTableForm_Cell" action="#" method="post">
<input type="hidden" id="_tablesource" name="_tablesource" value="Table_Cell" />
<input type="hidden" id="_fileformat" name="_fileformat" value="" />
<input type="hidden" id="_data" name="_data" value="" />
</form>
<form id="fTableForm_Feature" action="#" method="post">
<input type="hidden" id="_tablesource" name="_tablesource" value="Table_Feature" />
<input type="hidden" id="_fileformat" name="_fileformat" value="" />
<input id = "featureIds" name='featureIds' type='hidden' value="" />
</form>

<div id="information_panel"></div>
<div id="tree-panel" style="float:left"></div>
<div id="sample-layout" style="float:left"></div>
<div class="clear"></div>
<div id="information" style="background-color:#DDE8F4; visibility:hidden; height:0px; font-size:1em; line-height:1.2em; padding:6px 8px;text-align:left; border-bottom:0px; border-right:0px;">
	<div id="grid_result_summary"><b>Loading...</b><br/></div>
</div>

<form id='<%=name%>_geneToFile' action='<%=contextPath%>/jsp/GetMainTable.jsp' method="post">
	<input name='GeneFileType' type='hidden' value='xlsx' />
	<input name='GeneFileName' type='hidden' value='mainName' />
	<input name='data' type='hidden' value='' />
</form>

<script type="text/javascript">
//<![CDATA[
var $Page;

Ext.onReady(function() {

	var state = Ext.state.Manager.get('TRfeaturelist');
	
	var pageProperties = {
		cart:true, 
		name: "<%=name%>", 
		stateId: ['TRfeaturelist'],
		pagingBarMsg: ['Displaying genes {0} - {1} of {2}'],
		sort: (state && state.sort)?state.sort:[{property:'genome_name',
			direction:'ASC'
		}],
		hash:{
		}
	};
	Ext.QuickTips.init();
	SetPageProperties(pageProperties);
	TranscriptomicsGeneOnReady('<%=name%>', '<%=resourceURL%>',
		'<%=contextPath%>', '<%=contextType%>', '<%=contextId%>', '<%=sampleId%>', '<%=expId%>', '<%=wsExperimentId%>', '<%=wsSampleId%>', '<%=log_ratio%>', '<%=zscore%>', '<%=keyword%>');
	
	if (Ext.get("tabs_explist")!=null) {
		Ext.get("tabs_explist").addCls("sel");
	}
});
//]]>
</script>