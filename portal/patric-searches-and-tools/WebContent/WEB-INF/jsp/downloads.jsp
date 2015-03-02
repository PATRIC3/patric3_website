<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" 
%><%@ page import="edu.vt.vbi.patric.common.OrganismTreeBuilder" 
%><%
boolean isLoggedIn = (Boolean) request.getAttribute("isLoggedIn");
String organismName = (String) request.getAttribute("organismName");
int taxonId = (Integer) request.getAttribute("taxonId");

%>
	<div id="intro" class="searchtool-intro">
		<p>To use PATRIC's download tool, select one or more genomes of interest, then one or more annotation sources, followed by the file types.
		  For further explanation, please see <a href="http://enews.patricbrc.org/download-data-faqs/download-data-tool-faqs/" target="_blank">Download Data Tool FAQs</a>.</p>
	</div>

	<div id="result-meta" class="search-results-form-wrapper" style="display:none">
		<a href="#" id="modify-search"><img src="/patric/images/btn_modify_search.gif" id="modify-search-btn" alt="Modify Search Criteria" /></a>
	</div>

	<div class="left" style="width:480px">
		<h3><img src="/patric/images/number1.gif" alt="1" height="14" width="14" /> Select organism(s)</h3>
		<%=OrganismTreeBuilder.buildOrganismTreeListView() %>
	</div>
	<div class="left" style="padding-left: 25px">
		<form id="searchForm" name="searchForm" action="#" method="post" onsubmit="return false;">
		<input type="hidden" id="cType" name="cType" value="" />
		<input type="hidden" id="cId" name="cId" value="" />
		<input type="hidden" id="genomeId" name="genomeId" value="" />
		<input type="hidden" id="taxonId" name="taxonId" value="<%=taxonId %>" />
		<input type="hidden" id="finalfiletype" name="finalfiletype" value="" />
		<input type="hidden" id="finalalgorithm" name="finalalgorithm" value="" />

			<h3><img src="/patric/images/number2.gif" alt="2" height="14" width="14" /> Choose Annotation Source</h3>
			<div class="far queryblock">
				<input id="annotation_patric" type="checkbox" name="algorithm" value=".PATRIC" checked="checked"/> <label for="annotation_patric">PATRIC</label>
				<input id="annotation_refseq" type="checkbox" name="algorithm" value=".RefSeq"/> <label for="annotation_refseq">RefSeq</label>
			</div>
			
			<h3><img src="/patric/images/number3.gif" alt="3" height="14" width="14" /> Choose File Type</h3>
			<div class="far">
				<div class="left queryblock">
					<input type="radio" name="filetype" value=".fna" id="filetype_fna" checked="checked"/> <label for="filetype_fna">Genomic Sequences in FASTA (*.fna)</label>
					<input type="radio" name="filetype" value=".faa" id="filetype_faa"/> <label for="filetype_faa">Protein Sequences in FASTA (*.faa)</label>
					<input type="radio" name="filetype" value=".gbf" id="filetype_gbf"/> <label for="filetype_gbf">All annotations in GenBank file format (*.gbf)</label>
					<input type="radio" name="filetype" value=".features.tab" id="filetype_features"/> <label for="filetype_features">All genomic features in tab-delimited format (*.features)</label>
					<input type="radio" name="filetype" value=".cds.tab" id="filetype_cds"/> <label for="filetype_cds">Protein coding genes in tab-delimited format (*.cds.tab)</label>
					<input type="radio" name="filetype" value=".rna.tab" id="filetype_rna"/> <label for="filetype_rna">RNAs in tab-delimited format (*.rna.tab)</label>
				</div>
				<div class="right queryblock">
					<input type="radio" name="filetype" value=".ffn" id="filetype_ffn"/> <label for="filetype_ffn">DNA Sequences of Protein Coding Genes (*.ffn)</label>
					<input type="radio" name="filetype" value=".frn" id="filetype_frn"/> <label for="filetype_frn">DNA Sequences of RNA Coding Genes (*.frn)</label>
					<input type="radio" name="filetype" value=".pathway.tab" id="filetype_path"/> <label for="filetype_path">Pathway assignments in tab-delimited format (*.path)</label>
				</div>
				<div class="clear"></div>
			</div>
			<input type="submit" value="Download" onclick="download()" class="button right" style="cursor:pointer" />
		</form>
	</div>
	<div class="clear"></div>
<script type="text/javascript" src="/patric/js/vbi/AddToWorkspace.min.js"></script>
<script type="text/javascript">
//<![CDATA[
var menu1=null;
var tabs= "";
var loggedIn = <%=isLoggedIn %>;
Ext.onReady(function() {
	tabs = Ext.create('VBI.GenomeSelector.Panel', {
		renderTo: 'GenomeSelector',
		width: 480,
		height: 550,
		border:false,
		parentTaxon: <%=taxonId %>,
		organismName:'<%=organismName%>'
	});
});

function getSelected(type){
	var s = new Array();
	for (var i=0; i < document.searchForm[type].length; i++) {
		if (document.searchForm[type][i].checked){
			s.push(document.searchForm[type][i].value);
		}
	}
	return s.join(",");
}

function download() {

	var genomes, size;

	Ext.getDom("searchForm").action = "/portal/portal/patric/Downloads/DownloadsWindow?action=b&cacheability=PAGE&mode=download";
	Ext.getDom("finalfiletype").value = getSelected("filetype"); 
	Ext.getDom("finalalgorithm").value = getSelected("algorithm");
	Ext.getDom("searchForm").target = "";

	if(!tabs.getSelectedInString()){
		Ext.getDom("genomeId").value = "";
		
		Ext.Ajax.request({
			url: "/portal/portal/patric/Downloads/DownloadsWindow?action=b&cacheability=PAGE",
			method: 'GET',
			params: {mode:"getGenomeCount", taxonId:Ext.getDom("taxonId").value, data_source:Ext.getDom("finalalgorithm").value},
			success: function(response, opts) {
				size = response.responseText;
				if(size > 100){
					Ext.MessageBox.alert(size+" genomes", 'Current resources can not handle more than 100 genomes..');
				}else{
					
					if(Ext.getDom("finalfiletype").value != "" && Ext.getDom("finalalgorithm").value != "") {
						Ext.getDom("searchForm").submit();
					} else {
						Ext.MessageBox.alert('Error', 'Please choose at least one Annotation source and one File format');
					}
				}
			}
		});
	} else {
		if((tabs.getSelectedInString() == null || tabs.getSelectedInString() == "")){
			
			Ext.Ajax.request({
				url: "/portal/portal/patric/Downloads/DownloadsWindow?action=b&cacheability=PAGE",
				method: 'GET',
				params: {mode:"getGenomeCount", taxonId:Ext.getDom("taxonId").value, data_source:Ext.getDom("finalalgorithm").value},
				success: function(response, opts) {
					size = response.responseText;
					if(size > 100) {
						Ext.MessageBox.alert(size+" genomes", 'Current resources can not handle more than 100 genomes..');
					} else {
						
						if(Ext.getDom("finalfiletype").value != "" && Ext.getDom("finalalgorithm").value != ""){
							
							Ext.Ajax.request({
								url: "/portal/portal/patric/Downloads/DownloadsWindow?action=b&cacheability=PAGE&mode=download",
								method: 'POST',
								params: {genomeId:Ext.getDom("genomeId").value, taxonId:Ext.getDom("taxonId").value, finalfiletype:Ext.getDom("finalfiletype").value, finalalgorithm:Ext.getDom("finalalgorithm").value},
								success: function(response, opts) {
									alert(response.responseText);
								}
							});
						} else {
							Ext.MessageBox.alert('Error', 'Please choose at least one Annotation source and one File format');
						}
					}
				}
			});
		} else {
			genomes = tabs.getSelectedInString();
			size = genomes.split(",").length;
			if(size > 100) {
				Ext.MessageBox.alert(size+" genomes", 'Current resources can not handle more than 100 genomes..');
			} else {
				Ext.getDom("genomeId").value = genomes;
				if(Ext.getDom("finalfiletype").value != "" && Ext.getDom("finalalgorithm").value != "") {
					Ext.getDom("searchForm").submit();
				} else {
					Ext.MessageBox.alert('Error', 'Please choose at least one Annotation source and one File format');
				}
			}
		}
	}
}
//]]>
</script>