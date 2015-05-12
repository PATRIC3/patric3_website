<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"
%><%
	String contextType = (String) request.getAttribute("contextType");
	String contextId = (String) request.getAttribute("contextId");
	String myUrl = (String) request.getAttribute("myUrl");
	String hp_ppi_checked = (String) request.getAttribute("hp_ppi_checked");
	String ppi_checked = (String) request.getAttribute("ppi_checked");
%>
	<div class="container">
		<span>Below is the graphical and tabular summary of the non-redundant, experimentally characterized 
			protein-protein interaction data available for the current taxon/genome/gene. For more information, 
			see <a href="http://enews.patricbrc.org/faqs/pathogen-interaction-gateway-pig-faqs/" target="_blank">Pathogen Interaction Gateway (PIG) FAQs</a>.
		</span><br>
		<input type="radio" name="hpi_type" id="hp_ppi" value="true" onclick="hpiChoice('<%=contextType%>', '<%=contextId%>','true');" <%=hp_ppi_checked %> /> <label for="hp_ppi">HP-PPIs</label>
		<input type="radio" name="hpi_type" id="ppi" value="false" onclick="hpiChoice('<%=contextType%>', '<%=contextId%>', 'false');" <%=ppi_checked %> /> <label for="ppi">PPIs</label>

		<iframe height=800 width=1100 src="<%=myUrl%>"></iframe>
	</div>

<script type="text/javascript">
function hpiChoice(mytype, myid, myhpi) {
	var new_url = "HPITool?dm=tab&cType=" + mytype + "&cId=" + myid + "&hpi=" + myhpi;
	document.location.href = new_url;
}

Ext.onReady(function () {
	if (Ext.get("tabs_interaction")!=null) {
		Ext.get("tabs_interaction").addCls("sel");
	}
});
</script>