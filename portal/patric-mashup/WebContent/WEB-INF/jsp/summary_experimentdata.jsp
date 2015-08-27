<%
String speciesName = (String) request.getAttribute("speciesName");
%>
<p>Below we provide general post-genomic data awareness for <%=speciesName %> including aggregation of post-genomic meta-data
	from multiple sources and consolidated access to specific experimental datasets, details, and results. 
	At this time, PATRIC retrieves post-geomic meta-data in real-time from world prominent databases by using taxonomy ID or name as the search term.
	Actual experimental data and results can be accessed via linkouts to respective databases. 			
	We categorize the post-genomic metadata into transcriptomics, proteomics, structure and interaction data types. 
	For further explanation, please see <a href="http://enews.patricbrc.org/post-genomics-faqs/" target="_blank">Experiment Data User Guide</a>.
</p>

<script type="text/javascript">
Ext.onReady(function () {
	if (Ext.get("tabs_experimentdata")!=null) {
		Ext.get("tabs_experimentdata").addCls("sel");
	}
});
</script>