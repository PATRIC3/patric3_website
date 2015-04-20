<%@ page import="edu.vt.vbi.patric.common.SiteHelper"
%><%@ page import="edu.vt.vbi.patric.beans.GenomeFeature"
%><%@ page import="java.util.*"%><%

GenomeFeature feature = (GenomeFeature) request.getAttribute("feature");
String uniprotkbAccession = (String) request.getAttribute("uniprotkbAccession");
List<String> pdbIds = (List<String>) request.getAttribute("pdbIds");
List<Map<String, Object>> listStructure = (List<Map<String, Object>>) request.getAttribute("listStructure");
String genusName = (String) request.getAttribute("genusName");

String urlBEIR = SiteHelper.getExternalLinks("BEIR");
String ENDPOINT = System.getProperty("polyomic.baseUrl", "https://www.patricbrc.org/oldapi/");

%>
<table class="basic stripe far2x">
<tbody>
	<tr>
		<th scope="row" style="width:20%">GO Assignments</th>
		<td class="last"><%
			if (feature.hasGeneOntology()) {
				for (String go: feature.getGeneOntology()) {
					String[] items = go.split("\\|");
					%><a href="<%=SiteHelper.getExternalLinks("go_term")+items[0] %>" target="_blank"><%=items[0] %></a> <%=items[1] %><br /><%
				}
			} else {
				%>-<%
			}
		%></td> 
	</tr>
	<tr>
		<th scope="row">EC Assignments</th>
		<td><%
			if (feature.hasEnzymeClass()) {
				for (String ec : feature.getEnzymeClass()) {
					String[] items = ec.split("\\|");
					%><a href="<%=SiteHelper.getExternalLinks("ec_number")+items[0] %>" target="_blank"><%="EC:"+items[0] %></a> <%=items[1] %><br /><%
				}
			} else {
				%>-<%
			}
		%></td> 
	</tr>
	<tr>
		<th scope="row">FIGfam Assignments</th>
		<td><%
			if (feature.hasFigfamId()) {
				%><a href="javascript:submitFigfam('<%=feature.getFigfamId() %>')"><%=feature.getFigfamId() %></a><%
			} else {
				%>-<%
			}
		%></td> 
	</tr>
	<tr> 
		<th scope="row">Pathway Assignments</th>
		<td><%
			if (feature.hasPathway()) {
				for (String pw : feature.getPathway()) {
					String[] items = pw.split("\\|");
					%><a href="CompPathwayMap?cType=genome&amp;cId=<%=feature.getGenomeId() %>&amp;dm=feature&amp;feature_id=<%=feature.getId() %>&amp;map=<%=items[0] %>&amp;algorithm=<%=feature.getAnnotation() %>&amp;ec_number="><%="KEGG:"+items[0] %></a> <%=items[1] %><br /><%
				}
			} else {
				%>-<%
			}
		%></td>
	</tr>
	
	<!-- PDB Info -->
	<% if (!pdbIds.isEmpty()) { %>
	<tr>
		<th scope="row">Structure</th>
		<td>
			<% for (String pdbId: pdbIds) { %>
			<a href="Jmol?structureID=<%=pdbId %>"><%=pdbId %></a>&nbsp;
			<% } %>
		</td>
	</tr>
	<% } %>
	<!-- End of PDB Info -->

	<%
	// submit to CSGID/SSGCID
	if (listStructure == null && pdbIds.size() == 0 && feature.getFeatureType().equals("CDS")) {

		String regSSGCID = SiteHelper.getGenusByStructuralGenomicsCenter("ssgcid");
		String regCSGID = SiteHelper.getGenusByStructuralGenomicsCenter("csgid");

		if (genusName != null && genusName.matches(regSSGCID)) {
			%>
			<tr><th scope="row">Structure</th><td class="no-underline-links"><a href="#" class="double-arrow-link" onclick="SubmitToStructuralGenomicsCenter('ssgcid');return false;">Submit a request for structure determination to SSGCID</a></td></tr>
			<%
		} else if (genusName != null && genusName.matches(regCSGID)) {
			%>
			<tr><th scope="row">Structure</th><td class="no-underline-links"><a href="#" class="double-arrow-link" onclick="SubmitToStructuralGenomicsCenter('csgid');return false;">Submit a request for structure determination to CSGID</a></td></tr>
			<%
		} else {
			%>
			<tr><th scope="row">Structure</th><td>Not supported by SSGCID/CSGID</td></tr>
			<%
		}
	}
	%>
	<!-- end of structure center related -->
	
	<!-- PPI Info -->
	<tr>
		<th scope="row">Protein Interactions</th>
		<td>
			<span><b>Host-pathogen interactions:</b></span> <a href="javascript:searchPPIURL('<%=feature.getId() %>', 'Y')"><span id="hpi-count"></span></a>&nbsp;&nbsp;&nbsp;&nbsp;
			<span><b>All interactions:</b></span> <a href="javascript:searchPPIURL('<%=feature.getId() %>', 'N')"><span id="ppi-count"></span></a>
		</td>
	</tr>
	<!-- End of PPI Info -->
</tbody>
</table>

<% if (listStructure != null) { %>
<h3 class="section-title normal-case close2x">Structure</h3>
<table class="basic stripe far2x">
	<thead>
		<tr>
			<th scope="col">Source</th>
			<th scope="col">Target</th>
			<th scope="col">Selection Criteria</th>
			<th scope="col">Status</th>
			<th scope="col">Clone Available</th>
			<th scope="col">Protein Available</th>
		</tr>
	</thead>
	<tbody>
	<%
		for (Map<String, Object> structural: listStructure) {
	%>
		<tr>
			<td><a href="http://www.ssgcid.org" target="_blank">SSGCID</a></td>
			<td><a href="https://apps.sbri.org/SSGCIDTargetStatus/Target/<%=structural.get("target_id") %>" target="_blank"><%=structural.get("target_id") %></a></td>
			<td><%=structural.get("selection_criteria") %></td>
			<td><%=structural.get("target_status") %> 
				<% if (structural.get("target_status").equals("in PDB") && pdbIds.size() > 0) { %>
					<br/>
					<% for (String pdbId: pdbIds) { %>
					<a href="Jmol?structureID=<%=pdbId %>"><%=pdbId %></a>&nbsp;
					<% } %>
				<% } %>
			</td>
			<td><%=structural.get("has_clones").equals("T")?"Yes":"No" %></td>
			<td><%=structural.get("has_proteins").equals("T")?"Yes":"No" %></td>
		</tr>
		<% } %>
	</tbody>
</table>
<% } %>

	<form id="sgc_form" method="POST" action="#" target="_blank">
		<input type="hidden" name="patric_feature_id" value="<%=feature.getId() %>" />
		<input type="hidden" name="patric_callback_url" value="/portal/portal/patric/Feature?cType=feature&amp;cId=<%=feature.getId() %>" />
		<input type="hidden" name="genome_name" value="<%=feature.getGenomeName() %>" />
		<input type="hidden" name="product" value="<%=feature.getProduct() %>" />
		<input type="hidden" id="dna_sequence" name="dna_sequence" value="" />
		<input type="hidden" id="protein_sequence" name="protein_sequence" value="" />
		<input type="hidden" name="refseq_locus_tag" value="<%=feature.hasRefseqLocusTag()?feature.getRefseqLocusTag():"" %>" />
		<input type="hidden" name="refseq_protein_id" value="<%=feature.hasProteinId()?feature.getProteinId():"" %>" />
		<input type="hidden" name="refseq_gi_number" value="<%--=(refseqInfo!=null)?refseqInfo.get("gi_number"):"" --%>" />
		<input type="hidden" name="uniprot_accession" value="<%=uniprotkbAccession %>" />
	</form>
	
<script type="text/javascript">
//<![CDATA[
var featureID = "<%=feature.getId() %>";
function getDNASequence() {
	Ext.Ajax.request({
		url: '/portal/portal/patric/FeatureTable/FeatureTableWindow?action=b&cacheability=PAGE&mode=fasta&fastaaction=ajax&fastatype=FASTA DNA Sequence(s)&fastascope=Selected&fids='+featureID,
		success: function(rs, opts) {
			Ext.getDom("dna_sequence").value = rs.responseText;
		},
		callback: getProteinSequence
	});	
}
function getProteinSequence() {
	Ext.Ajax.request({
		url: '/portal/portal/patric/FeatureTable/FeatureTableWindow?action=b&cacheability=PAGE&mode=fasta&fastaaction=ajax&fastatype=FASTA Protein Sequence(s)&fastascope=Selected&fids='+featureID,
		success: function(rs, opts) {
			Ext.getDom("protein_sequence").value = rs.responseText;
		},
		callback: function() {
			Ext.getDom("sgc_form").submit();
		}
	});
}
function SubmitToStructuralGenomicsCenter(center) {
	if (center=="ssgcid") {
		Ext.getDom("sgc_form").action = "https://apps.sbri.org/SSGCIDCommTargReq/Default.aspx";
	} else {
		Ext.getDom("sgc_form").action = "http://www.biochem.ucl.ac.uk/cgi-bin/phil/csgid/submit_CSGID_targets/submit_PATRIC_protein.pl";
	}
	getDNASequence();
}
function submitFigfam(id) {

	Ext.Ajax.request({
		url: '/portal/portal/patric/SingleFIGfam/SingleFIGfamWindow?action=b&cacheability=PAGE',
		method: 'POST',
		timeout: 600000,
		params: {callType: "saveState",
			gid: '',
			figfam: id
		},
		success: function(rs) {
			document.location.href = "SingleFIGfam?"+"&cType=taxon&cId=131567&bm=tool&pk="+rs.responseText;
		}
	});	
}
function requestPPICount(fid, is_hpi, base_url) {
	var ppiCount;
	var keyword = fid;
	var myHPI = is_hpi;
	var myURL;
	
	if (myHPI === 'Y') {
		myURL = base_url + '/pig/?descendants((2,33208))&between()&keyword('+ keyword + ')&eq(is_hpi,Y)&facet((sort,+rownum))&limit(0,0)';
	}
	else {
		myURL = base_url + '/pig/?descendants((2,33208))&between()&keyword('+ keyword + ')&facet((sort,+rownum))&limit(0,0)';
	}
	
	Ext.Ajax.disableCaching = false;
	Ext.Ajax.request({
		url: myURL,
		async:false,
		success: function(rs) {
			var jsonStr = rs.responseText;
			var json = Ext.JSON.decode(jsonStr);
			ppiCount = json.totalRows;
		}
	});
	// document.write(ppiCount);
	if (is_hpi === 'Y') {
		Ext.getDom("hpi-count").innerHTML = ppiCount;
	} else {
		Ext.getDom("ppi-count").innerHTML = ppiCount;
	}
}
function searchPPIURL(fid, is_hpi) {
	var keyword = fid;
	var myHPI = is_hpi;
	var myURL;
	var hpiParam = "false";
	if (myHPI === 'Y') {
		hpiParam = "true";
	}

	myURL = "HPITool?dm=tab&cType=feature&cId=" + keyword + "&hpi=" + hpiParam;
	// console.log(myURL)
	document.location.href = myURL;
}
Ext.onReady(function() {
	requestPPICount('<%=feature.getId() %>', 'Y', '<%=ENDPOINT%>');
	requestPPICount('<%=feature.getId() %>', 'N', '<%=ENDPOINT%>');
});
//]]>
</script>