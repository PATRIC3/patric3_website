<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" 
%><%@ page import="edu.vt.vbi.patric.common.OrganismTreeBuilder"
%><%@ page import="java.util.*"
%><%
int taxonId = (Integer) request.getAttribute("taxonId");
String organismName = (String) request.getAttribute("organismName");
String cType = (String) request.getAttribute("cType");
String cId = (String) request.getAttribute("cId");
List<String> featureTypes = (List<String>) request.getAttribute("featureTypes");
boolean isLoggedIn = (Boolean) request.getAttribute("isLoggedIn");

%>

	<div id="intro" class="searchtool-intro">
		<p>Feature Finder allows you to locate specific features(s) based on taxonomy (e.g., genus or species), feature type (e.g., CDS, rRNA, etc.), 
		keyword, sequence status, and/or annotation type.
		Select organism(s) and enter a keyword to search for features by Gene Name, Locus Tag, or Protein Function. 
		Refine search by specifying feature type, sequence status, and/or annotations.</p>
	</div>

	<div class="left" style="width:480px">
		<h3><img src="/patric/images/number1.gif" alt="1" height="14" width="14" /> Select organism(s)</h3>
		<%=OrganismTreeBuilder.buildOrganismTreeListView() %>
	</div>
	<div class="left" style="width:25px">&nbsp;</div>
	<div class="left" style="width:375px">
		<h3><img src="/patric/images/number2.gif" alt="2" height="14" width="14" /> Enter keyword</h3><br />

		<form action="#" onsubmit="return false;">

		<label class="left" for="feature_type">Feature Type:</label>
		<select class="right far" id="feature_type" name="feature_type" size="1">
			<option value="">ALL</option>
	<%
        for (String featureType: featureTypes) {

		    %><option value="<%=featureType%>" <%=featureType.equals("CDS")?"selected=\"selected\"":"" %>><%=featureType%></option><%
		} %>
		</select>
		<div class="clear"></div>
		
		<label class="left" for="keyword">Keyword:</label>
		<textarea class="right" id="keyword" name="keyword" rows="5" cols="30"><%--=(key!=null && key.containsKey("keyword") && !key.get("keyword").equalsIgnoreCase(""))?key.get("keyword"):""--%></textarea>
		<div class="clear"></div>
		
		<span class="small bold"><b>Examples</b></span>
		<table class="basic far">
		<tbody>
		<tr>
			<th width=125 scope="row">Keyword:</th>
			<td>DNA polymerase</td>
		</tr>
		<tr>
			<th scope="row">Keyword:</th>
			<td>dnaN</td>
		</tr>
		<tr>
			<th scope="row">Keyword:</th>
			<td>VBIBruSui107850_0001</td>
		</tr>
		</tbody>
		</table>
		
		<label class="left" for="annotation">Annotation:</label>
		<select class="right far2x" id="annotation" name="annotation" size="1">
			<option value="">ALL</option>
			<%
			List<String> dbList = new ArrayList<String>();
			dbList.add("PATRIC");
			// dbList.add("BRC1");
			dbList.add("RefSeq");

			for (String annotation: dbList) {
			%>
			    <option value="<%=annotation%>" <%=annotation.equals("PATRIC")?"selected=\"selected\"":""%>><%=annotation%></option>
		<%	} %>
		</select>
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
var name = "Feature";
var url = "/portal/portal/patric/GenomicFeature/GenomicFeatureWindow?action=b&cacheability=PAGE";
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