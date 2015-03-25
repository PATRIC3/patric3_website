<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ page import="edu.vt.vbi.patric.common.SolrInterface" %>
<%@ page import="edu.vt.vbi.patric.beans.Genome" %>
<%@ page import="org.json.simple.JSONObject" %>
<%
String cType = request.getParameter("context_type");
String cId = request.getParameter("context_id");
String taxon_id = cId;
String hpi_only = request.getParameter("hpi_only");

String my_url = "/patric/pig/viewer/index.html?";
String hp_ppi_checked = "";
String ppi_checked = "";

if (hpi_only == null) {
	hpi_only = "false";
}

if (hpi_only.equals("true")) {
	hp_ppi_checked = "checked";
	ppi_checked = "";
}
else {
	hp_ppi_checked = "";
	ppi_checked = "checked";
}

if (cType.equals("genome")) {
	SolrInterface solr = new SolrInterface();
	Genome context = solr.getGenome(cId);
	taxon_id = "" + context.getTaxonId();

	my_url = my_url + "taxids=" + taxon_id + "&hpisOnly=" + hpi_only + "&btwnOnly=false&page=1&w=1100&h=800";
} 
else if (cType.equals("taxon")) {
	taxon_id = cId;
	my_url = my_url + "taxids=" + taxon_id + "&hpisOnly=" + hpi_only + "&btwnOnly=false&page=1&w=1100&h=800";
}
else {
	my_url = my_url + "keywds=" + cId + "&hpisOnly=" + hpi_only + "&btwnOnly=false&page=1&w=1100&h=800";
}

%>

	<div class="container">
		<span>Below is the graphical and tabular summary of the non-redundant, experimentally characterized 
			protein-protein interaction data available for the current taxon/genome/gene. For more information, 
			see <a href="http://enews.patricbrc.org/faqs/pathogen-interaction-gateway-pig-faqs/" target="_blank">Pathogen Interaction Gateway (PIG) FAQs</a>.
		</span><br>
		<input type="radio" name="hpi_type" id="hp_ppi" value="true" onclick="hpiChoice('<%=cType%>', '<%=cId%>','true');" <%=hp_ppi_checked %> /> <label for="hp_ppi">HP-PPIs</label>
		<input type="radio" name="hpi_type" id="ppi" value="false" onclick="hpiChoice('<%=cType%>', '<%=cId%>', 'false');" <%=ppi_checked %> /> <label for="ppi">PPIs</label>

		<iframe height=800 width=1100 src="<%=my_url%>"></iframe>
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