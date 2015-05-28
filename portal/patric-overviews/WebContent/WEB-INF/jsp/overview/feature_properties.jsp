<%@ page import="java.util.*"
%><%@ page import="edu.vt.vbi.patric.beans.GenomeFeature"
%><%@ page import="edu.vt.vbi.patric.common.SiteHelper"
%><%
GenomeFeature feature = (GenomeFeature) request.getAttribute("feature");
List<GenomeFeature> listReleateFeatures = (List<GenomeFeature>) request.getAttribute("listReleateFeatures");
List<String> listUniprotkbAccessions = (List<String>) request.getAttribute("listUniprotkbAccessions");
List<Map<String, String>> listUniprotIds = (List<Map<String, String>>) request.getAttribute("listUniprotIds");
String refseqLink = (String) request.getAttribute("refseqLink");
String refseqLocusTag = (String) request.getAttribute("refseqLocusTag");
Map<String, String> virulenceFactor = (Map<String, String>) request.getAttribute("virulenceFactor");

if (feature != null) {
%>
	<% if (feature.getAnnotation().equals("RefSeq")) { %>
	<div class="close2x" id="note_refseq_only_feature"><b>NOTE:</b> There is no corresponding PATRIC feature. Comparative tools are not available at this time.</div>
	<% } %>
	<table class="basic stripe far2x left" style="width:600px">
	<tbody>
		<tr>
			<th scope="row" style="width:15%">Gene ID</th>
			<td>
				<% if (feature.hasPatricId()) { %>
					<span><b>PATRIC ID</b></span>:
					<span><%=feature.getPatricId() %> </span>
					&nbsp;
				<% } %>

				<% if (refseqLocusTag != null) { %>
					<span><b>RefSeq</b></span>: 
					<% if (refseqLink != null) { %>
						<a href="<%=refseqLink %> " target="_blank"><%=refseqLocusTag%></a>
					<% } else { %>
						<%=refseqLocusTag%>
					<% } %>
					&nbsp;
				<% } %>

				<% if (feature.getAnnotation().equals("PATRIC") && feature.hasAltLocusTag()) { %>
					<span><b>Alt Locus Tag</b></span>:
					<span><%=feature.getAltLocusTag() %> </span>
				<% } %>
			</td>
		</tr>
		<tr>
			<th scope="row">Protein ID</th>
			<td>
				<% if (feature.hasProteinId()) { %>
					<span><b>RefSeq</b></span>:
					<% if (feature.getGi() > 0) { %>
						<span><a href="<%=SiteHelper.getExternalLinks("ncbi_protein")+feature.getGi()%>" target="_blank"><%=feature.getProteinId() %></a></span>
					<% } else { %>
						<span><%=feature.getProteinId() %></span>
					<% } %>
					&nbsp;&nbsp;&nbsp;&nbsp;
				<% } %>
				<% if (listUniprotkbAccessions != null && listUniprotIds != null) { %>
				<span><b>UnitProt</b></span>: 
				<span><% for (String uniprotkbAccession: listUniprotkbAccessions) { %>
				    <a href="<%=SiteHelper.getExternalLinks("UniProtKB-Accession") %><%=uniprotkbAccession %>" target="_blank"><%=uniprotkbAccession %></a>&nbsp;
				    <% } %>
			    </span>
				&nbsp;
				<span> <a href="#" onclick="toggleLayer('uniprot_detail');return false;"><%=listUniprotIds.size() %> IDs are mapped</a></span>
				<div id="uniprot_detail" class="table-container" style="display:none">
					<table class="basic">
					<% for (Map<String, String> uniprot: listUniprotIds) { %>
					<tr>
						<th scope="row" style="width:20%"><%=uniprot.get("idType") %></th>
						<td><%
							String uniprot_link = SiteHelper.getExternalLinks(uniprot.get("idType")).replace("&","&amp;");
							if (uniprot_link != "" && uniprot.get("idType").matches("HOGENOM|OMA|ProtClustDB|eggNOG")) {
								%><a href="<%=uniprot_link%><%=uniprot.get("Accession")  %>" target="_blank"><%=uniprot.get("idValue") %></a><%
							} else if (uniprot_link != "") {
								%><a href="<%=uniprot_link%><%=uniprot.get("idValue") %>" target="_blank"><%=uniprot.get("idValue") %></a><%
							} else {
								%><%=uniprot.get("idValue") %><%
							}
						%></td>
					</tr>
					<% } %>
					</table>
				</div>
				<% } %>
				&nbsp;
			</td>
		</tr>
		<%
		// check Virulence factor
		if (virulenceFactor != null) {
			%><tr><td colspan="2"><span class="label-box-orange">VF</span> This gene is identified as a virulence factor by PATRIC based on literature curation.
			<a href="SpecialtyGeneEvidence?source=<%=virulenceFactor.get("source") %>&sourceId=<%=virulenceFactor.get("sourceId") %>">See details</a></td></tr><%
		}
		%>
	</tbody>
	</table>

	<div id="feature_box" class="far2x right">
		<div id="gene_symbol"><%=feature.hasGene()?feature.getGene():"" %></div>
		<% if (feature.getStrand().equals("+")) { %>
		<img id="strand" alt="forward strand" src="/patric/images/forward.png"/>
		<% } else { %>
		<img id="strand" alt="reverse strand" src="/patric/images/reverse.png"/>
		<% } %>
		<div id="feature_type"><%=feature.getFeatureType() %></div>
	</div>

	<div class="clear"></div>

	<table class="basic stripe far2x">
	<thead>
		<tr>
			<th scope="col">Annotation</th>
			<th scope="col">Locus Tag</th>
			<th scope="col">Start</th>
			<th scope="col">End</th>
			<th scope="col">NT Length</th>
			<th scope="col">AA Length</th>
			<th scope="col">Product</th>
		</tr>
	</thead>
	<tbody>
	<% for (GenomeFeature aFeature: listReleateFeatures) { %>
		<tr>
			<td><%=aFeature.getAnnotation() %></td>
			<td><%=aFeature.hasAltLocusTag()?aFeature.getAltLocusTag():"-" %></td>
			<td class="right-align-text"><%=aFeature.getStart() %></td>
			<td class="right-align-text"><%=aFeature.getEnd() %></td>
			<td class="right-align-text"><%=aFeature.getNaSequenceLength() %></td>
			<td class="right-align-text"><%=(aFeature.getProteinLength() > 0)?aFeature.getProteinLength():"-" %></td>
			<td><% if (aFeature.hasProduct()) { %>
					<%=aFeature.getProduct() %>
				<% } else if (aFeature.getFeatureType().equals("CDS") == false) { %>
					(feature type: <%=aFeature.getFeatureType() %>)
				<% } else { %>
					-
				<% } %>
			</td>
		</tr>
	<% } %>
	</tbody>
	</table>
	
<% } %>

<script type="text/javascript">
//<![CDATA[
Ext.onReady(function () {
	if (Ext.get("tabs_featureoverview")!=null) {
		Ext.get("tabs_featureoverview").addCls("sel");
	}
});
//]]>
</script>