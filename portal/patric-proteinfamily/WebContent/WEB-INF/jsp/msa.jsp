<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"
%><%@ page import="java.util.*"
%><%@ page import="edu.vt.vbi.patric.common.SessionHandler"
%>
<%@ page import="org.codehaus.jackson.map.ObjectMapper" %>
<%@ page import="org.codehaus.jackson.map.ObjectReader" %>
<portlet:defineObjects /><%
String nameSpaceAids = renderResponse.encodeURL(renderRequest.getContextPath() + "/js/namespace.js");

String runBrowser = renderResponse.encodeURL(renderRequest.getContextPath() + "/js/TreeAligner.js");

String actionURL = (renderResponse.createActionURL()).toString();

String windowID = renderRequest.getWindowID();

String resourceURL = (renderResponse.createResourceURL()).toString();

String contextPath = renderResponse.encodeURL(renderRequest.getContextPath());

String pk = request.getParameter("param_key");
String featureIds = "";
String figfamId = "";
String product = "";

ObjectMapper objectMapper = new ObjectMapper();
ObjectReader jsonReader = objectMapper.reader(Map.class);

Map<String, String> key = jsonReader.readValue(SessionHandler.getInstance().get(SessionHandler.PREFIX + pk));
if (key != null) {
	featureIds = key.get("featureIds");
	figfamId = key.get("figfamId");
	if (figfamId == null) {
		figfamId = "";
	}
	product = key.get("product");
	if (product == null) {
		product = "";
	}
}
%>

<script type="text/javascript" src="/node_modules/msa-tnt/node_modules/d3/d3.min.js"></script>
<script type="text/javascript" src="/js/msa/msa.min.js"></script>
<script type="text/javascript" src="/node_modules/biojs-io-newick/build/biojs-io-newick.min.js"></script>
<script type="text/javascript" src="/node_modules/msa-tnt/build/bundle.js"></script>

<script type="text/javascript" src="<%=nameSpaceAids%>"></script>
<script type="text/javascript" src="<%=runBrowser%>"></script>

<div id="<%=windowID%>">
	<div id = '<%=windowID%>_summary'></div>
	<div id = '<%=windowID%>_forApplet' style='width:100%; height:500px'></div>
</div>

<form id="<%=windowID%>_form" action="#" method="post">
	<input type="hidden" id="data" name="data" />
	<input type="hidden" id="fileformat" name="fileformat" value="" />
</form>

<script type="text/javascript">
//<![CDATA[
var path="";

Ext.onReady(function() {
	TreeAlignerOnReady("<%=windowID%>", "<%=resourceURL%>", "<%=contextPath%>",
		"<%=featureIds%>", "<%=figfamId%>", "<%=product%>");
});
//]]>
</script>
