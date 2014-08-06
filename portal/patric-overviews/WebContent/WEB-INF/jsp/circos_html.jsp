<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"
%><%@ page import="java.util.ArrayList" 
%><%@ page import="java.util.Arrays" 
%><%@ page import="org.json.simple.JSONObject" 
%><%@ page import="edu.vt.vbi.patric.common.SolrInterface"
%><%@ page import="edu.vt.vbi.patric.common.SolrCore"
%><%
	String actionUrl = "/portal/portal/patric/CircosGenomeViewer/CircosGenomeViewerWindow?action=1";
	String genomeId = request.getParameter("context_id");
	String polymoicUrl = System.getProperty("polyomic.baseUrl", "http://polyomic.patricbrc.org:8888");
	SolrInterface solr = new SolrInterface();
	solr.setCurrentInstance(SolrCore.SEQUENCE);
	JSONObject genomeInfo = solr.getGenome(genomeId);
	// System.out.println(genomeInfo.toJSONString());
	// This genome has more than 200 contigs. The circular genome map is not displayed as it can be too busy and uninterpretable.
%>
<script type="text/javascript">
var genome_info_id = '<%=genomeId%>';
var form_url = '<%=actionUrl%>';
var ds_url = '<%=polymoicUrl%>';
var countSequences = '<%=genomeInfo.get("sequences")%>';
</script>
<script type="text/javascript" src="/patric-overviews/circos/main.js"></script>
<link rel="stylesheet" type="text/css" href="/patric-overviews/circos/styles.css">
<p>Below is the circular map of the genome, showing genome annotations and sequence properties. 
For more information on how to add custom tracks and/or upload your own data, see <a href="//enews.patricbrc.org/faqs/circular-genome-viewer-faqs/" target="_blank">Circular Genome Viewer FAQs</a>.</p>
<div id="circosPanel">
	<div id="circosGraph"></div>
</div>