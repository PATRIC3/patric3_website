<%@ page import="edu.vt.vbi.patric.beans.GenomeFeature"
%><%

GenomeFeature feature = (GenomeFeature) request.getAttribute("feature");
String dispRefseqLocusTag = (String) request.getAttribute("dispRefseqLocusTag");
String dispSeedId = (String) request.getAttribute("dispSeedId");
String dispSequenceID = (String) request.getAttribute("dispSequenceID");
String dispProteinSequence = (String) request.getAttribute("dispProteinSequence");

String fId = feature.getId();

if (feature != null) {
	
	%>
	<div class="far2x">
		<input type="button" class="button close2x" title="Add Feature to Workspace" value="Add <%=feature.getAnnotation() %> Feature to Workspace" onclick="saveFeature()" />

		<div class="close2x"><a href="/portal/portal/patric/FeatureTable/FeatureTableWindow?action=b&amp;cacheability=PAGE&amp;mode=fasta&amp;fastaaction=display&amp;fastatype=dna&amp;fastascope=Selected&amp;fids=<%=fId %>"
				onclick="window.open(this.href,'mywin','width=920,height=500,resizable,scrollbars');return false" 
				target="_blank" style="text-decoration:none">
				View NT Sequence</a>
		</div>
		<% if (dispProteinSequence != null) { %>
		<div class="close2x"><a href="/portal/portal/patric/FeatureTable/FeatureTableWindow?action=b&amp;cacheability=PAGE&amp;mode=fasta&amp;fastaaction=display&amp;fastatype=protein&amp;fastascope=Selected&amp;fids=<%=fId %>"
				onclick="window.open(this.href,'mywin','width=920,height=500,resizable,scrollbars');return false" 
				target="_blank" style="text-decoration:none">
				View AA Sequence</a>
		</div>
		<% } %>
	</div>

	<% if (dispRefseqLocusTag != null || dispSeedId != null || (dispSequenceID !=null && dispProteinSequence!=null)) { %>
	<h3 class="section-title normal-case close2x">
		<span class="wrap">External Tools</span>
	</h3>
	<div class="far2x">
		<% if (dispSeedId != null) { %>
		<div class="close2x">
			<a href="http://pubseed.theseed.org/?page=Annotation&amp;feature=<%=dispSeedId %>" target="_blank" style="text-decoration:none">The SEED Viewer</a>
		</div>
		<% } %>
		<% if (dispSequenceID != null && dispProteinSequence != null) { %>
		<div class="close2x">
			<a href="http://www.ncbi.nlm.nih.gov/Structure/cdd/wrpsb.cgi?SEQUENCE=%3E<%=dispSequenceID.replace(" ","%20") %>%0A<%=dispProteinSequence %>&amp;FULL" target="_blank" style="text-decoration:none">NCBI CDD Search</a>
		</div>
		<% } %>
		<% if (dispRefseqLocusTag != null) { %>
		<div class="close2x">
			<a href="http://string.embl.de/newstring_cgi/show_network_section.pl?identifier=<%=dispRefseqLocusTag %>" target="_blank" style="text-decoration:none">STRING: Protein-Protein Interactions</a>
		</div>
		<div class="close2x">
			<a href="http://stitch.embl.de/cgi/show_network_section.pl?identifier=<%=dispRefseqLocusTag %>" target="_blank" style="text-decoration:none">STITCH: Chemical-Protein Interactions</a>
		</div>
		<% } %>
	</div>
	<% } %>
	
	<script type="text/javascript" src="/patric/js/vbi/AddToWorkspace.min.js"></script>
	<script type="text/javascript" src="/patric-common/js/parameters.js"></script>
	<script type="text/javascript">
	//<![CDATA[
	var $Page,
		ZeroClipboard = null,
		pageProperties = {cart:true};
		SetPageProperties(pageProperties);
		
	var Page = $Page, btnGroupPopupSave = Page.getCartSaveButton();

	function saveFeature(){
		addSelectedItems("Feature");
	}
	btnGroupPopupSave.on('click', function(){
		if(saveToGroup("<%=fId%>", "Feature")){
			popup.hide();
		}
	});
	//]]>
	</script>

	<%
} else {
	%>&nbsp;<%
}
%>