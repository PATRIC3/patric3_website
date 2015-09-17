<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"
%><%
String cType = request.getParameter("context_type");
String cId = request.getParameter("context_id");
String genomeFilter = request.getParameter("genome_filter");
if (genomeFilter == null) genomeFilter = "";
%>
<div id="sg_tbl">
	<span class="right">Retrieving data...&nbsp;
		<img src="/patric/images/icon_please_wait.gif" alt="Please Wait" style="vertical-align:middle" />
	</span>
	<div style="clear:both"></div>
</div>
<script type="text/javascript">
//<![CDATA[
Ext.onReady(function () {
	Ext.Ajax.request({
		//url: '<portlet:resourceURL />',
		url: '/portal/portal/patric/Taxon/SpecialtyGeneSummaryWindow?action=b&cacheability=PAGE',
		method: 'GET',
		params: {context_type:'<%=cType%>',context_id:'<%=cId%>',genome_filter:'<%=genomeFilter%>'},
		success: function(rs) {
			Ext.getDom("sg_tbl").innerHTML = rs.responseText;
		},
		failure: function(rs) {
			Ext.getDom("sg_tbl").innerHTML = "Data is not available now. Please try again later.";
		}
	});
});
//]]>
</script>