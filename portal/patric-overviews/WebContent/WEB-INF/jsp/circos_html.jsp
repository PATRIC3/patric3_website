<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"
%><%@ page import="edu.vt.vbi.patric.beans.Genome"
%><%
	String actionUrl = (String) request.getAttribute("actionUrl");
	String dataApiUrl = (String) request.getAttribute("dataApiUrl");
	Genome genome = (Genome) request.getAttribute("genome");
%>
<script type="text/javascript">
var genome_id = '<%=genome.getId()%>';
var form_url = '<%=actionUrl%>';
var data_api_url = '<%=dataApiUrl%>';
var countSequences = '<%=genome.getSequences()%>';
</script>
<script type="text/javascript" src="/patric-overviews/circos/main.js"></script>
<link rel="stylesheet" type="text/css" href="/patric-overviews/circos/styles.css">
<p>Below is the circular map of the genome, showing genome annotations and sequence properties. 
For more information on how to add custom tracks and/or upload your own data, see <a href="http://enews.patricbrc.org/faqs/circular-genome-viewer-faqs/" target="_blank">Circular Genome Viewer User Guide</a>.</p>
<div id="circosPanel">
	<div id="circosGraph"></div>
</div>