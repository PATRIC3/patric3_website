<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" 
%><portlet:defineObjects/><%%>
<div id="intro" class="searchtool-intro"> 
	<p>The ... Tool enables researchers to ... 
	For further explanation, please see ...</p> 
</div> 
 
<form id="searchForm" name="searchForm" action="#" method="post" onsubmit="return false;">
	<div class="left" style="width:480px">
		<h3><img src="/patric/images/number1.gif" alt="1" height="14" width="14" /> IDs</h3><br/> 
		<textarea id="keyword" name="keyword" cols="60" rows="7"></textarea> 
	</div>
	
	<div class="left" style="width:375px"> 
		<table class="querytable">
		<tr>
			<td class="formaction" colspan="2"><input class="button rightarrow" type="submit" value="Search" onclick="searchbykeyword()" /></td> 
		</tr>
		</table>
	</div>
</form>

<div class="clear"></div>
<script type="text/javascript"> 
//<![CDATA[
function searchbykeyword() {
	var value,
		i,
		size;

	if(Ext.getDom("keyword").value == "") {
		Ext.MessageBox.alert("Empty Search Box...");
	}
	else {

		value = Ext.getDom("keyword").value.trim();
		value = value.replace(/,/g,"~").replace(/\n/g,"~");
		
		size = value.split("~").length;
	
		if (size > 20) {
			Ext.MessageBox.alert(size+" IDs", 'Current resources can not handle more than 20 ids...');
		}
		else {
			value = value.split("~");
			
			for(i=0; i<value.length; i++){
				value[i] = value[i].trim();
			}
		
			Ext.Ajax.request({
				url: '/portal/portal/patric/TranscriptomicsGene/TranscriptomicsGeneWindow?action=b&cacheability=PAGE',
				method: 'POST',
				params: {
					callType: "saveParams",
					keyword: value.join(" OR ")
				},
				success: function(rs) {
					if(rs.responseText){
						document.location.href = "TranscriptomicsGene?cType=taxon&cId=2&pk="+rs.responseText+"&dm=result&expId=&sampleId=&colId=&log_ratio=&zscore=";
					}else{
						Ext.MessageBox.alert('Error', 'No experiments found.');
					}
				}
			});
		}
	}
}
//]]>
</script>