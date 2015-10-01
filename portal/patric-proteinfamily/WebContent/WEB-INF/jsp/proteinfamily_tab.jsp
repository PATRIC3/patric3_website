<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"
%><%@ page import="java.util.*"
%><portlet:defineObjects /><%

String name = "FIGfamSorter";
String defaultpath = renderResponse.encodeURL(renderRequest.getContextPath());
String nameSpaceAids = defaultpath+ "/js/namespace.js";
String runBrowser = defaultpath+ "/js/FigFamSorter.js";
String gridObject = defaultpath + "/js/GroupGrid.js";
String gridState = defaultpath + "/js/GroupGridState.js";
String gridSupport = "/patric-common/js/grid/gridoptions.js";
String toolbar = "/patric-common/js/grid/toolbar.js";
String checkColumn = "/patric-common/js/grid/checkcolumn.js";
String checkBoxes = "/patric-common/js/grid/table_checkboxes.js";
String pagingBar = "/patric-common/js/grid/pagingbar.js";
String hashTracker = defaultpath + "/js/HashTracker.js";
String heatTypes = "/patric-common/js/heatmap/heatmapDatatypes.js";
String swfObject = "/patric-common/js/heatmap/swfobject.js";
String loadHeat = defaultpath + "/js/heatmap/loadHeatmap.js";
String heatMediate = defaultpath + "/js/heatmap/heatmapMediator.js";

String windowID = renderRequest.getWindowID();
String resourceURL = (renderResponse.createResourceURL()).toString();
String contextPath = renderResponse.encodeURL(renderRequest.getContextPath());

String contextType = (String) request.getAttribute("contextType");
String contextId = (String) request.getAttribute("contextId");
String pk = (String) request.getAttribute("pk");
String keyword = (String) request.getAttribute("keyword");
Map<String, String> key = (Map) request.getAttribute("key");
String familyType = (String) request.getAttribute("familyType");
String genomeFilter = (String) request.getAttribute("genomeFilter");
%>

<script type="text/javascript" src="<%=nameSpaceAids%>"></script>
<script type="text/javascript" src="<%=runBrowser%>"></script>
<script type="text/javascript" src="<%=toolbar%>"></script>
<script type="text/javascript" src="<%=gridObject%>"></script>
<script type="text/javascript" src="<%=gridState%>"></script>
<script type="text/javascript" src="<%=gridSupport%>"></script>
<script type="text/javascript" src="<%=hashTracker%>"></script>
<script type="text/javascript" src="<%=checkColumn%>"></script>
<script type="text/javascript" src="<%=checkBoxes%>"></script>
<script type="text/javascript" src="<%=pagingBar%>"></script>
<script type="text/javascript" src="<%=heatTypes%>"></script>
<script type="text/javascript" src="<%=loadHeat%>"></script>
<script type="text/javascript" src="<%=heatMediate%>"></script>
<script type="text/javascript" src="<%=swfObject%>"></script>
<script type="text/javascript" src="/patric-searches-and-tools/js/solrKeyword.js"></script>
<script type="text/javascript" src="/patric-common/js/ZeroClipboard.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/copybutton.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/PATRICSelectionModel.js"></script>
<script type="text/javascript" src="/patric-common/js/grid/PATRICGrid.js"></script>
<script type="text/javascript" src="/patric-common/js/parameters.js"></script>
<script type="text/javascript" src="/patric/js/vbi/AddToWorkspace.min.js"></script>
<script type="text/javascript" src="/patric-searches-and-tools/js/json2.js"></script>

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
<input type="hidden" id="_tablesource" name="_tablesource" value="FeatureTable_Cell" />
<input type="hidden" id="_fileformat" name="_fileformat" value="" />
<input type="hidden" id="_data" name="_data" value="" />
</form>

<div id="legend" style="visibility:hidden; height:0px;">
	<div class="idvg-legend-title">HeatMap Cells</div>
	<p style="padding-left:4px;">Cell color represents the number of proteins from a specific genome in a given protein family.</p>
	<br/>
	<div class="idvg-legend-entry">
		<span class="idvg-legend-symbol">
			<img src="/patric/images/heatmap/heatmap-black-0.png" alt=""/>
		</span>
		<span class="idvg-legend-label">0</span>
	</div>
	<div class="idvg-legend-entry">
		<span class="idvg-legend-symbol">
			<img src="/patric/images/heatmap/heatmap-yellow-1.png" alt=""/>
		</span>
		<span class="idvg-legend-label">1</span>
	</div>
	<div class="idvg-legend-entry">
		<span class="idvg-legend-symbol">
			<img src="/patric/images/heatmap/heatmap-orange-2.png" alt=""/>
		</span>
		<span class="idvg-legend-label">2</span>
	</div>
	<div class="idvg-legend-entry">
		<span class="idvg-legend-symbol">
			<img src="/patric/images/heatmap/heatmap-red-3.png" alt=""/>
		</span>
		<span class="idvg-legend-label">3+</span>
	</div>
</div>

<div id="copy-button" style="display:none"></div>

<form id='orthoToFile' action='/portal/portal/patric/FIGfam/FIGfamWindow?action=b&cacheability=PAGE&callType=GetMainTable' method="post">
	<input name='OrthoFileType' type='hidden' value='xlsx' />
	<input name='OrthoFileName' type='hidden' value='mainName' />
	<input name='data' type='hidden' value='' />
</form>

<form id='detailsToFile' action='/portal/portal/patric/FIGfam/FIGfamWindow?action=b&cacheability=PAGE&callType=DetailsFromMain' method="post">
	<input name='detailsType' type='hidden' value='' />
	<input name='detailsGenomes' type='hidden' />
	<input name='detailsFamilyIds' type='hidden' />
	<input name='familyType' type='hidden' />
</form>

<div id="information_panel"></div>
<div>
	<form id="familyType">
		<input type="radio" id="familyType_plfam" name="famType" value="plfam" <%=(familyType.equals("plfam")?"checked='checked'":"")%> onchange="changeFamilyType('plfam')" /> <label for="familyType_plfam">PATRIC genus-specific families (PLfams) &nbsp; </label>
		<input type="radio" id="familyType_pgfam" name="famType" value="pgfam" <%=(familyType.equals("pgfam")?"checked='checked'":"")%> onchange="changeFamilyType('pgfam')" /> <label for="familyType_pgfam">PATRIC cross-genus families (PGfams) &nbsp; </label>
		<input type="radio" id="familyType_figfam" name="famType" value="figfam" <%=(familyType.equals("figfam")?"checked='checked'":"")%> onchange="changeFamilyType('figfam')" /> <label for="familyType_figfam">FIGFam &nbsp; </label>
	</form>
</div>
<div id="tree-panel" style="float:left"></div>
<div id="sample-layout" style="float:left"></div>
<div class="clear"></div>
<div id="information" style="background-color:#DDE8F4; font-size:1em; line-height:1.2em; padding:6px 8px;text-align:left; border-bottom:0px; border-right:0px;">
	<div id="grid_result_summary"><b>Loading...</b><br/></div>
</div>

<script type="text/javascript">
//<![CDATA[
var $Page;

Ext.onReady(function() {
	
	var state = Ext.state.Manager.get('figfamlist');
	var genomeIds = ('<%=key.get("genomeIds") %>' != '')?'<%=key.get("genomeIds") %>':null;
	
	var pageProperties = {
		cart:true,
		name: "<%=name%>",
		stateId: ['figfamlist'],
		pagingBarMsg: ['Displaying families {0} - {1} of {2}'],
		sort: (state && state.sort)?state.sort:[{property:'groupId',
			direction:'ASC'
		}],
		hash:{
		}
	};
	Ext.QuickTips.init();
	SetPageProperties(pageProperties);
	FigFamSorterOnReady('<%=name%>', '<%=resourceURL%>',
						'<%=contextPath%>', '<%=contextType%>', '<%=contextId%>', '<%=keyword%>', genomeIds, '<%=familyType%>','<%=genomeFilter%>');
	if (Ext.get("tabs_proteinfamilysorter")!=null) {
	 	Ext.get("tabs_proteinfamilysorter").addCls("sel");
	}
}
);
//]]>
</script>