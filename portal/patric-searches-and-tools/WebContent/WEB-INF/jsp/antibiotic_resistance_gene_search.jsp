<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" 
%><%@ page import="edu.vt.vbi.patric.common.OrganismTreeBuilder" 
%><%
int taxonId = (Integer) request.getAttribute("taxonId");
String organismName = (String) request.getAttribute("organismName");
String cType = (String) request.getAttribute("cType");
String cId = (String) request.getAttribute("cId");
boolean isLoggedIn = (Boolean) request.getAttribute("isLoggedIn");

%>

	<div id="intro" class="searchtool-intro">
		<p>Antibiotic Resistance refers to the ability of a bacteria to develop resistance to antibiotics through gene mutation or 
		acquisition of antibiotic resistance genes. This search tool allows researchers to find antibiotic resistance genes in their organisms of interest 
		based on taxonomy, special property class, and keyword search. 
		For more details, see <a href="http://enews.patricbrc.org/faqs/specialty-genes-faqs/" target="_blank">Specialty Genes User Guide</a>.
	</div>

	<div class="left" style="width:480px">
		<h3><img src="/patric/images/number1.gif" alt="1" height="14" width="14" /> Select organism(s)</h3>
		<%=OrganismTreeBuilder.buildOrganismTreeListView() %>
	</div>
	<div class="left" style="width:25px">&nbsp;</div>
	<div class="left" style="width:375px">
		<h3><img src="/patric/images/number2.gif" alt="2" height="14" width="14" /> Enter keyword</h3><br />

		<form action="#" onsubmit="return false;">

		<label class="left" for="keyword">Keyword:</label>
		<textarea class="right" id="keyword" name="keyword" rows="5" cols="30"><%--=(key!=null && key.containsKey("keyword") && !key.get("keyword").equalsIgnoreCase(""))?key.get("keyword"):""--%></textarea>
		<div class="clear"></div>
		
		<span class="small bold"><b>Examples</b></span>
		<table class="basic far">
		<tbody>
		<tr>
			<th width=125 scope="row">Keyword:</th>
			<td>gyrA <br>
				DNA gyrase <br>
				penicillin <br>
				multidrug resistance
			</td>
		</tr>
		</tbody>
		</table>
		
		<label class="left" for="Filter" style="padding-top: 15px">Source</label>
		<div id="Filter" class="right" style="width:200px; padding-top: 15px">
			<input type="checkbox" name="source" value="ARDB" /> ARDB
			<br/><input type="checkbox" name="source" value="CARD" /> CARD
		</div>
		<div class="clear"></div>
		
		<label class="left" for="Filter2" style="padding-top: 15px">Evidence</label>
		<div id="Filter2" class="right" style="width:200px; padding-top: 15px">
			<input type="checkbox" name="evidence" value="Literature" /> Literature
			<br/><input type="checkbox" name="evidence" value="BLASTP" /> BLASTP
		</div>
		<div class="clear"></div>
		
		<input class="right button rightarrow"  type="submit" value="Search" onclick="searchbykeyword('<%=cId%>', '<%=cType %>')" />
		
		</form>
	</div>
	<div class="clear"></div>
<script type="text/javascript" src="/patric-searches-and-tools/js/solrKeyword.js"></script>
<script type="text/javascript" src="/patric-searches-and-tools/js/search_common.js"></script>
<script type="text/javascript" src="/patric/js/vbi/AddToWorkspace.min.js"></script>
<script type="text/javascript">
//<![CDATA[
var tabs = "";
var name = "AntibioticResistanceGeneMapping";
var url = "/portal/portal/patric/AntibioticResistanceGeneSearch/AntibioticResistanceGeneSearchWindow?action=b&cacheability=PAGE";
var loggedIn = <%=isLoggedIn %>;

Ext.onReady(function(){

	updateFields();
	
	tabs = Ext.create('VBI.GenomeSelector.Panel', {
		renderTo: 'GenomeSelector',
		width: 480,
		height: 550,
		border:false,
		parentTaxon: <%=taxonId %>,
		organismName:'<%=organismName%>'
	});
});
//]]>
</script>