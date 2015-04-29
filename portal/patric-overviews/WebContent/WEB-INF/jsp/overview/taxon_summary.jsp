<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" 
%><%@ page import="java.util.*"
%><%
List<Map<String, Object>> lineage = (List<Map<String, Object>>) request.getAttribute("lineage");
long pk = (new Random()).nextLong();

String tId = request.getParameter("context_id");
if (tId != null) {
    int taxonId = Integer.parseInt(tId);

    if (!lineage.isEmpty()) {
	%>

	<table class="basic stripe far2x" id="data-table">
	<tbody>
	<tr>
		<th scope="row" style="width:20%">Taxonomy ID</th>
		<td><a href="http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?mode=Info&amp;id=<%=tId %>" target="_blank"><%=tId %></a></td>
	</tr>
	<tr>
		<th scope="row">Lineage</th>
		<td>
		<%
		for (Map<String, Object> node: lineage) {

			%><a href="Taxon?cType=taxon&amp;cId=<%=node.get("taxonId")%>" title="taxonomy rank:<%=node.get("rank")%>"><%=node.get("name")%></a> <%

			if ((Integer) node.get("taxonId") != taxonId) {
			%> &gt; <%
			}
		}
		%>
		</td>
	</tr>
	<tr>
		<th scope="row" >External Links</th>
		<td><a href="http://www.immuneepitope.org/sourceOrgId/<%=tId %>" target="_blank">Immune Epitope Database and Analysis Resource</a></td>
	</tr>
	<tr>
		<td colspan="2"><span>Summary Terms - Click on number to view genomes associated with term (<a href="http://enews.patricbrc.org/genome-finder-faqs/" target="_blank">see PATRIC FAQs</a>)</span></td>
	</tr>
	<tr>
		<td colspan="2"><img src="/patric/images/loading.gif" alt="Loading Icon"/> Loading...</td>
	</tr>
	<tr>
		<td class="no-underline-links" colspan="2" id="click_for_more"></td>
	</tr>
	</tbody>
	</table>
	<%
	}
}
%>
<script type="text/javascript" src="//ethn.io/remotes/27203" async="true" charset="utf-8"></script>
<script type="text/javascript" src="/patric-searches-and-tools/js/solrKeyword.js"></script>
<script type="text/javascript" src="/patric-searches-and-tools/js/json2.js" ></script>
<script type="text/javascript">
//<![CDATA[
var facet_data;
var name = "Genome";

Ext.onReady(function () {

	var object = {};
	<% if (tId != null) { %>
	object["Keyword"] =  'taxon_lineage_ids:<%=tId%>';
	<% } else { %>
	object["gid"] =  '';
	object["Keyword"] =  '(*)';
	<% } %>

	if (object["gid"] != "" || '<%=tId%>' == '2') {
		Ext.Ajax.request({
			url: "/portal/portal/patric/GenomeFinder/GenomeFinderWindow?action=b&cacheability=PAGE",
			method: 'POST',
			timeout: 600000, 
			params: {need:"tree_for_taxon"
				,state:JSON.stringify({})
				,pk: '<%=pk %>'
				,facet:JSON.stringify({
					'facet': configuration[name].display_facets.join(','),
					'facet_text': configuration[name].display_facets_texts.join(','),
					'field_facets': configuration[name].field_facets.join(','),
					'date_range_facets': configuration[name].date_range_facets.join(',')
				})
				,keyword:constructKeyword(object, name, "")
			},
			success: function(response, opts) {
				facet_data = Ext.JSON.decode(response.responseText);
				AppendData(facet_data);
			}
		});
	} else {
		document.getElementById("data-table").deleteRow(Ext.getDom("data-table").tBodies[0].rows.length-2);
	}

	if (Ext.get("tabs_taxonoverview")!=null) {
		Ext.get("tabs_taxonoverview").addCls("sel");
	}
});

function AppendData(data){
	
	document.getElementById("data-table").deleteRow(Ext.getDom("data-table").tBodies[0].rows.length-2);
	
	for(var i=0; i< data.length; i++){
		var obj = data[i];
		
		var table = Ext.getDom("data-table");
		var nextRow = table.tBodies[0].rows.length-1;
		var row = table.tBodies[0].insertRow(nextRow);

		if (i%2==0) {
			row.setAttribute('class', 'alt');
		}

		var cell0 = document.createElement('th');
		cell0.setAttribute('scope', 'row');
		row.appendChild(cell0);
		var cell1 = row.insertCell(1);
		
		var ind1_parent = obj.text.indexOf("<b>(");
		var ind2_parent = obj.text.indexOf(")</b>");
		
		cell0.innerHTML = obj.text.substring(0, ind1_parent-1);
		cell1.innerHTML = "&nbsp;";
		
		for(var j=0; j < obj.children.length; j++){
		
			var text = obj.children[j].text;
			
			var ind1 = obj.children[j].text.indexOf(">(");
			var ind2 = obj.children[j].text.indexOf(")</span>");
			var ind3 = obj.children[j].text.indexOf(" <span");
			
			var count = obj.children[j].text.substring(ind1+2, ind2);
			
			var newtext = "<a href=\"javascript:GotoGenomeList('"+obj.id+"', '"+text.substring(0, ind3).trim()+"', '"+text.substring(ind1+2, ind2)+"','single')\">"+count+"</a>";
			
			if (obj.children.length == 1 && j == 0) {
				cell1.innerHTML += text.substring(0, ind3)+" ("+newtext+")";
			}
			else if (j < 2) {
				cell1.innerHTML += text.substring(0, ind3)+" ("+newtext+")" + ", &nbsp;";
			}
			else if (j == 2) {
				cell1.innerHTML += "<a href=\"javascript:GotoGenomeList('"+obj.id+"', '*', '*','all')\"><u>show all "+obj.text.substring(ind1_parent+4, ind2_parent)+" genomes</u></a>";
			}
			
			if (obj.children.length == 2 && j == 1) {
				cell1.innerHTML = cell1.innerHTML.substring(0, cell1.innerHTML.length - 8);
			}
		}
	}
	
	Ext.getDom("click_for_more").innerHTML += "<a class=\"double-arrow-link\" href=\"javascript:GotoGenomeList('','','','more')\" id=\"metadata-more\">View all genomes and summary terms</a>";
	
}

function GotoGenomeList(search_on, keyword, count, type) 
{

	var object = {};
	object["gid"] =  '';
	
	if(type == 'single'){
	
		object[search_on] = keyword.replace(/\'/g, "%27");

	}else if(type == 'all'){
	
		object[search_on] = "*";
	}
		
	Ext.Ajax.request({
		url: '/portal/portal/patric/GenomeFinder/GenomeFinderWindow?action=b&cacheability=PAGE',
		method: 'POST',
		params: {cType: 'taxon',
			cId: '<%=tId%>',
			sraction: "save_params",
			keyword: constructKeyword(object, name),
			state:JSON.stringify(getState(search_on, keyword, count, type))
		},
		success: function(rs) {
			//relocate to result page
			document.location.href="GenomeList?cType=taxon&cId="+<%=tId%>+"&displayMode=&dataSource=&pk="+rs.responseText;
		}
	});

}

function getState(search_on, keyword, count, type){

	var state = {};
	
	for(var i=0; i< facet_data.length; i++){
		
		var facet_obj = facet_data[i];
		
		if(facet_obj.id == search_on){
		
			var obj = {};
			var obj_children = new Array();
		
			obj.id = search_on;
			obj.text = "<span style=\"color: #CC6600; margin: 0; padding: 0 0 2px; font-weight: bold;\">"+configuration[name].display_facets_texts[i]+ "</span><span style=\"color: #888;\"> ("+facet_obj.count+")</span>";
			obj.checked = true;
			obj.leaf = false;
			obj.expanded = true;
			
			var clear_obj = {};
			
			clear_obj.leaf = true;
			clear_obj.parentID = search_on;
			clear_obj.checked = false;
			clear_obj.id = facet_obj.id+"_clear";
			clear_obj.text = "clear";
				
			obj_children.push(clear_obj);
			
			for(var j=0; j < facet_obj.children.length; j++){
				
				var sub_obj = {};
			
				if(facet_obj.children[j].id.indexOf('_more') == -1){

					sub_obj.leaf = true;
					sub_obj.parentID = search_on;
					sub_obj.checked = true;
					sub_obj.id = facet_obj.children[j].id;
					
					if(keyword == facet_obj.children[j].id.split("##")[0]){
						sub_obj.text = keyword+"<span style=\"color: #888;\"> ("+count+")</span>";
						sub_obj.checked = true;
					}else{
						if(type == 'single')
							sub_obj.checked = false;
						sub_obj.text = format(facet_obj.children[j].text);
					}
				
				}else{
				
					var subchildren = facet_obj.children[j].children;
											
					sub_obj.leaf = false;
					sub_obj.parentID = search_on;
					sub_obj.checked = false;
					sub_obj.text = "<b>more</b>";
					sub_obj.id = facet_obj.children[j].id;
					
					if(type=='all'){
						sub_obj.checked = true;
						sub_obj.showAll = true;
					}
					
					var new_obj_children = new Array();
					
					for(var k=0; k < subchildren.length-1; k++){
					
						var subsubobj = {};
							
						subsubobj.leaf = true;
						subsubobj.parentID = search_on;
						subsubobj.checked = false;
						
						if(type=='all')
							subsubobj.checked = true;
							
						subsubobj.id = subchildren[k].id;
						subsubobj.text = format(subchildren[k].text);
						new_obj_children.push(subsubobj);
						
						if(k == subchildren.length -2){
							subsubobj = {};
							subsubobj.leaf = true;
							subsubobj.parentID = search_on;
							subsubobj.id = facet_obj.id+"_less";
							subsubobj.text = "<b>less</b>";
							
							new_obj_children.push(subsubobj);
						}
					}
					sub_obj.children = new_obj_children;
				}
				obj_children.push(sub_obj);
			}
			obj.children = obj_children;
			state[search_on] = obj;
		}
	}
	
	return state;
}

function format(text){
	
	var ind1 = text.indexOf("<b>(");
	var ind2 = text.indexOf(")</b>");
	
	return text.substring(0, ind1).trim()+"<span style=\"color: #888;\"> ("+text.substring(ind1+4, ind2)+")</span>";	
}
//]]>
</script>