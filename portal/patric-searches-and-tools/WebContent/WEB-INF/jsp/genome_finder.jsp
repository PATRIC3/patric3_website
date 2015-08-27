<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" 
%><%@ page import="edu.vt.vbi.patric.common.OrganismTreeBuilder" 
%><%@ page import="java.util.*"
%><%
int taxonId = (Integer) request.getAttribute("taxonId");
String organismName = (String) request.getAttribute("organismName");
String cType = (String) request.getAttribute("cType");
String cId = (String) request.getAttribute("cId");
boolean isLoggedIn = (Boolean) request.getAttribute("isLoggedIn");

%>

	<p>The Genome Finder allows you to search for all PATRIC genomes based on genome names and available metadata.
		To learn more, see <a href="http://enews.patricbrc.org/genome-finder-faqs/" target="_blank">Genome Finder User Guide</a>.
	</p>

	<div class="left" style="width:480px">
		<h3><img src="/patric/images/number1.gif" alt="1" height="14" width="14" /> Select organism(s)</h3>
		<%=OrganismTreeBuilder.buildOrganismTreeListView() %>
	</div>
	<div class="left" style="width:25px">&nbsp;</div>
	<div class="left" style="width:375px">
	
		<h3><img src="/patric/images/number2.gif" alt="2" height="14" width="14" /> Enter keyword</h3><br />

		<form action="#" onsubmit="return false;">

		<select class="left" id="search_on" name="search_on" size="1" onchange="Combo_Change()" style="width:150px;"></select>
		<div class="right far2x">
			<textarea id="keyword" name="keyword" rows="5" cols="30"></textarea>
			<br/>
			<input class="button" type="submit" value="Search" onclick="searchbykeyword('<%=cId%>', '<%=cType %>')" />
		</div>
		<div class="clear"></div>

		</form>
		
		<div class="callout" id='expander'></div>
	</div>
	<div class="clear"></div>
<script type="text/javascript" src="/patric-searches-and-tools/js/solrKeyword.js"></script>
<script type="text/javascript" src="/patric-searches-and-tools/js/search_common.js"></script>
<script type="text/javascript" src="/patric/js/vbi/AddToWorkspace.min.js"></script>
<script type="text/javascript">
//<![CDATA[
var tabs = "";
var name = "Genome";
var url = "/portal/portal/patric/GenomeFinder/GenomeFinderWindow?action=b&cacheability=PAGE";
var loggedIn = <%=isLoggedIn %>;

Ext.onReady(function(){

	tabs = Ext.create('VBI.GenomeSelector.Panel', {
		renderTo: 'GenomeSelector',
		width: 480,
		height: 550,
		border:false,
		parentTaxon: <%=taxonId %>,
		organismName:'<%=organismName%>'
	});	
	
	loadComboFacets();
	updateFields();	
	Combo_Change();
});
//]]>
</script>