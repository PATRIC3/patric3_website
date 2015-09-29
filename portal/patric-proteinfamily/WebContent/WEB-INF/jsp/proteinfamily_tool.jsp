<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ page import="edu.vt.vbi.patric.common.OrganismTreeBuilder" %>
<portlet:defineObjects />
<%
String resourceURL = (renderResponse.createResourceURL()).toString();
String taxonName = (String) request.getAttribute("taxonName");
int taxonId = (Integer) request.getAttribute("taxonId");
String familyType = (String) request.getAttribute("familyType");
String expander = ((taxonId != 131567)?"<div style='float:right'><div class='searchtool-inside'><h2 align='center'>Want to make comparisons not limited to " + taxonName + "?</h2><br />Use PATRIC's <a href='FIGfam?cType=taxon&cId=131567&dm='>Protein Family Sorter</a> located in Searches & Tools.<br/><b><br/>* Any genome selections you have made will be discarded!<b/></div></div>":"");

boolean isLoggedIn = (Boolean) request.getAttribute("isLoggedIn");
%>

<div style="padding: 5px;" >
	<div id="intro" class="searchtool-intro">
	<p>The Protein Family Sorter tool enables researchers to examine the distribution of specific protein families, known as FIGfams, across different genomes. <i><b>Protein Family Sorter currently supports comparison up to 500 genomes.</b></i> Select genomes using 'Taxonomy Tree' or 'A-Z List' tab below or create custom Genome Groups using <a href="GenomeFinder?cType=taxon&cId=&dm=">Genome Finder</a>. For further explanation, please see 
	<a href="http://enews.patricbrc.org/protein-family-sorter/">Protein Family Sorter User Guide.</a>
	</p>
	</div>
	<div class="left" style="width:480px">
		<h3><img src="/patric/images/number1.gif" alt="1" height="14" width="14" border="0" /> Select organism(s)</h3>
		<%=OrganismTreeBuilder.buildOrganismTreeListView() %>
	</div>
	<div class="left" style="width:25px">&nbsp;</div>
	<div class="left" style="width:375px">
		<h3><img src="/patric/images/number2.gif" alt="2" height="14" width="14" border="0" />
		Enter keyword</h3><br />
		<form action="#" onsubmit="return false;">

		<label class="left" for="family_type">Family Type:</label>
		<select class="right far" id="family_type" name="family_type" size="1">
			<option value="plfam" <%=familyType.equals("plfam")?"selected=\"selected\"":"" %>>PATRIC Genus Specific Family</option>
			<option value="pgfam" <%=familyType.equals("pgfam")?"selected=\"selected\"":"" %>>PATRIC Global Family</option>
			<option value="figfam" <%=familyType.equals("figfam")?"selected=\"selected\"":"" %>>FIGFam</option>
		</select>
		<div class="clear"></div>

		<label class="left" for="keyword">Keyword:</label>
		<div class="right" id="keyword"></div>
		<div class="clear"></div>

		<input class="right button rightarrow" type="submit" value="Search" onclick="checkGenomes()" />
		
		<%=expander %>
		</form>
	</div>
	<div class="clear"></div>
</div>
<script type="text/javascript" src="/patric/js/vbi/AddToWorkspace.min.js"></script>
<script type="text/javascript">
//<![CDATA[
var loggedIn = <%=isLoggedIn %>;
var tabs = "";

Ext.onReady(function(){
	tabs = Ext.create('VBI.GenomeSelector.Panel', {
		renderTo: 'GenomeSelector',
		width: 480,
		height: 550,
		border:false,
		parentTaxon: '<%=taxonId %>',
		organismName:'<%=taxonName %>'
	});
	
	Ext.create('Ext.form.field.TextArea', {
		id:'tb',
		renderTo: 'keyword',
		width: 285,
		height: 200
	});
});

function checkGenomes() {
	var idList = tabs.getSelectedInString();
	
	if(idList == null || idList.length == 0 ) {
		
		Ext.Ajax.request({
			url : '<%=resourceURL%>',
			method : 'GET',
			timeout : 600000,
			params : {
				callType : "getTaxonIds",
				taxonId : '<%=taxonId %>'
			},
			success : function(rs) {
				idList = rs.responseText;
				
				var idCounter = idList.split(",");
				if (Ext.getCmp("tb").getValue() == "") {
					if (idCounter.length > 500) {
						Ext.Msg.alert('More than 500 genomes selected!', 'Current resources can not handle more the 500 genomes with empty keyword.');
					}
					else {
						submitToFigFamSorter(idList);
					}
				}
				else {
					submitToFigFamSorter(idList);
				}
			},
			failure: function(rs) {
				console.log(rs);
			}
		});
	}
	else {
		submitToFigFamSorter(idList);
	}
}

function submitToFigFamSorter(idList) {
	
	var value = Ext.getCmp("tb").getValue().trim().toLowerCase();
	value = value.replace(/,/g,"~").replace(/\n/g,"~");
	value = value.split("~");

	var familyType = Ext.getDom("family_type").value;
	
	for(var i=0; i<value.length; i++){
		value[i] = value[i].trim();
	}

	Ext.Ajax.request({
		url: '<%=resourceURL%>',
		method: 'POST',
		timeout: 600000,
		params: {callType: "toSorter", genomeIds: idList, keyword:EncodeKeyword(value.join(" OR ")), familyType:familyType},
		success: function(rs) {
			document.location.href = "FIGfam?cType=taxon&cId=131567&dm=result&pk=" + rs.responseText + "&famType=" + familyType + "#gs_0=0";
		}
	});
}
//]]>
</script>
