<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" 
%><portlet:defineObjects/><%
String contextType = (String) request.getAttribute("contextType");
String contextId = (String) request.getAttribute("contextId");
String paramKey = (String) request.getAttribute("paramKey");

String to = (String) request.getAttribute("to");
String from = (String) request.getAttribute("from");
String keyword = (String) request.getAttribute("keyword");

boolean isLoggedIn = (Boolean) request.getAttribute("isLoggedIn");
%>
	<div id="intro" class="searchtool-intro"> 
		<p>The ID Mapping Tool enables researchers to locate synonymous identifiers across multiple-source databases.  
		For further explanation, please see <a href="http://enews.patricbrc.org/id-mapping-tool-faqs/">ID Mapping Tool User Guide</a>.</p>
	</div> 
 
	<div id="result-meta" class="search-results-form-wrapper" style="display:none"> 
		<a href="#" id="modify-search"><img src="/patric/images/btn_modify_search.gif" id="modify-search-btn" alt="Modify Search Criteria" /></a> 
	</div> 

	<form id="searchForm" name="searchForm" action="#" method="post" onsubmit="return false;"> 

	<div class="left" style="width:480px">
		<h3><img src="/patric/images/number1.gif" alt="1" height="14" width="14" /> IDs</h3><br/> 
		<textarea id="keyword" name="keyword" cols="60" rows="7"><%=keyword %></textarea> 
	</div>
	
	<div class="left" style="width:25px">&nbsp;</div>
	<div class="left" style="width:375px"> 
		<h3><img src="/patric/images/number2.gif" alt="2" height="14" width="14" /> ID Types</h3><br/> 
		<table class="querytable">
		<tr> 
			<td><b>FROM</b> ID Type:</td><td><div id="from"></div></td> 
		</tr>
		<tr>
			<td><b>TO</b> ID Type:</td><td><div id="to"></div></td> 
		</tr>
		<tr>
			<td class="formaction" colspan="2"><input class="button rightarrow" type="button" value="Search" onclick="searchbykeyword()" /></td>
		</tr>
		</table>
	</div>
	</form>
	<div class="clear"></div>
<script type="text/javascript"> 
//<![CDATA[
var store, combo, combo2, readerx;
var loggedIn = <%=isLoggedIn %>;
var combo_prev_value = "PATRIC Locus Tag", combo2_prev_value = "UniProtKB-ID";
Ext.onReady(function(){

	store = new Ext.data.JsonStore({
	    root: 'id_types',
	    fields: ['id', 'value', 'group']
	});
	
	Ext.Ajax.request({
	    url: "/portal/portal/patric/IDMapping/IDMappingWindow?action=b&cacheability=PAGE&sraction=filters",
	    method: 'GET',
	    success: function(response, opts) {
	
	        readerx = Ext.JSON.decode(response.responseText);
	        
	        store.loadData(readerx.id_types);
	        combo.setValue('<%=from%>');
	        combo2.setValue('<%=to%>');
	    }
	});
	
	combo = Ext.create('Ext.form.ComboBox',{
	    store: store,
	    displayField:'id',
	    valueField:'value',
	    queryMode: 'local',
	    hideTrigger: false,  //hide trigger so it doesn't look like a combobox.
	    renderTo: 'from',
	    width: 230,
	    triggerAction: 'all',
	    blankText: "PATRIC ID",
	    editable:false,
	    listeners: {
            'select' : function() {
 				
 				if(combo.rawValue.indexOf("Identifiers") > 0) {
 					combo.setValue(combo_prev_value);
 				}
 				else {
 					combo_prev_value = combo.rawValue;
                }
	        	if (combo.rawValue != "PATRIC Locus Tag") {
                    combo2.setValue("patric_id");
	    		}
    		}
		}
	      
	  });
 	
	combo2 = Ext.create('Ext.form.ComboBox', {
	    store: store,
	    displayField:'id',
	    valueField:'value',
	    queryMode: 'local',
	    hideTrigger: false,  //hide trigger so it doesn't look like a combobox.
	    renderTo: 'to',
	    width: 230,
	    triggerAction: 'all',
	    blankText: "UniProtKB-ID",
	    editable:false,
	    listeners: {
            'select' : function(){
 
 				if (combo2.rawValue.indexOf("Identifiers") > 0) {
 					combo2.setValue(combo2_prev_value);
 				}
 				else {
 					combo2_prev_value = combo2.rawValue;
 				}

	        	if (combo2.rawValue != "PATRIC Locus Tag") {
                    combo.setValue("patric_id");
	    		}
    		}
		}
	});
 	  
});
 
function searchbykeyword() {

	if (Ext.getDom("keyword").value == "") {
		Ext.MessageBox.alert("Empty Search Box...");
    }
	else {

		var value = Ext.getDom("keyword").value;

		value = value.replace(/,/g,"~");
		value = value.replace(/\n\n/g,"\n");
		value = value.replace(/\n/g,"~");

		var size = value.split("~").length;
	
		if (size > 5000) {
			Ext.MessageBox.alert(size+" IDs", 'Current resources can not handle more than 5000 ids...');
		}
		else {
			Ext.Ajax.request({
				url: '/portal/portal/patric/IDMapping/IDMappingWindow?action=b&cacheability=PAGE',
				method: 'POST',
				params: {cType: "taxon",
					cId: "",
					sraction: "save_params",
					keyword: value.replace(/~/g, ' OR '),
					from: combo.getValue(),
					fromGroup: combo.valueModels[0].data.group,
					to: combo2.getValue(),
					toGroup: combo2.valueModels[0].data.group
				},
				success: function(rs) {
					//relocate to result page
					document.location.href="IDMapping?cType=taxon&cId=<%=contextId%>&dm=result&pk="+rs.responseText;
				}
			});
		}
	}
}
//]]>
</script>