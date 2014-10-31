<%@ page import="java.util.*"
%><%@ page import="edu.vt.vbi.patric.beans.GenomeFeature"
%><%@ page import="edu.vt.vbi.patric.common.SiteHelper"
%><%@ page import="org.json.simple.JSONObject"
%><%
String source = (String) request.getAttribute("source");
String sourceId = (String) request.getAttribute("sourceId");

Map<String, Object> gene = (Map<String, Object>) request.getAttribute("gene");
List<String> properties = (List<String>) request.getAttribute("properties");
List<String> headers = (List<String>) request.getAttribute("headers");
GenomeFeature feature = (GenomeFeature) request.getAttribute("feature");
int cntHomolog = (Integer) request.getAttribute("cntHomolog");
List<Map<String, Object>> specialtyGeneEvidence = (List<Map<String, Object>>) request.getAttribute("specialtyGeneEvidence");

%>
	<h3 class="section-title normal-case close2x"><span class="wrap">Specialty Genes &gt; <%=gene.get("property") %> &gt; <%=gene.get("source") %> &gt; <%=gene.get("source_id") %> </span></h3>
	<table class="basic stripe far2x">
	<tbody>
	<% for (int i = 0; i < properties.size(); i++) {
		if (headers.get(i).equals("Source ID") && feature != null) { %>
		<tr>
			<th scope="row"><%=headers.get(i) %></th><td><a href="Feature?cType=feature&amp;cId=<%=feature.getId() %>"><%=gene.get(properties.get(i)) %></a></td>
		</tr>
		<% } else { %>
		<tr>
			<th scope="row"><%=headers.get(i) %></th><td><%=gene.get(properties.get(i)) %></td>
		</tr>
		<% } %>
	<% } %>
	<% if (feature != null) { %>
		<tr>
			<th scope="row">See this feature in </th>
			<td>
				<a href="GenomeBrowser?cType=feature&cId=<%=feature.getId() %>&loc=<%=(feature.getStart() - 1000) %>..<%=(feature.getEnd() + 1000) %>&tracks=PATRICGenes">Genome Browser</a>
				&nbsp; <a href="CompareRegionViewer?cType=feature&cId=<%=feature.getId() %>&tracks=&regions=5&window=10000&loc=1..10000">Compare Region Viewer</a>
				&nbsp; <a href="TranscriptomicsGeneExp?cType=feature&cId=<%=feature.getId() %>&sampleId=&colId=&log_ratio=&zscore=">Transcriptomics Data</a>
				&nbsp; <a href="TranscriptomicsGeneCorrelated?cType=feature&cId=<%=feature.getId() %>">Correlated genes</a>
			</td>
		</tr>
	<% } %>
	<% if (cntHomolog > 0) { %>
		<tr>
			<th scope="row">Homologs </th>
			<td><a href="SpecialtyGeneList?cType=taxon&cId=2&kw=source:<%=source %>+source_id:<%=sourceId%>"><%=cntHomolog %></a>
			</td>
		</tr>
	<% } %>
	</tbody>
	</table>

	<h3 class="section-title normal-case close2x"><span class="wrap">Evidence</span></h3>
	<table class="basic stripe far2x">
	<thead>
		<tr>
			<th scope="col">Organism</th>
			<th scope="col">Host</th>
			<th scope="col">Classification</th>
			<th scope="col">PubMed</th>
			<th scope="col">Assertion</th>
		</tr>
	</thead>
	<tbody>
		<%
		boolean alt = false;
		for (Map<String, Object> obj: specialtyGeneEvidence) {
		    alt = !alt;
		%>
		<tr <%=(alt)?" class=\"alt\"":"" %>>
			<td><%=(obj.get("specific_organism")!=null)?obj.get("specific_organism"):"&nbsp;" %></td>
			<td><%=(obj.get("specific_host")!=null)?obj.get("specific_host"):"&nbsp;" %></td>
			<td><%=(obj.get("classification")!=null)?obj.get("classification"):"&nbsp;" %></td>
			<td><%=(obj.get("pmid")!=null)?"<a class=\"arrow-slate-e\" href=\"//www.ncbi.nlm.nih.gov/pubmed/" + obj.get("pmid") + "\" target=_blank>" + obj.get("pmid") + "</a>":"&nbsp;" %></td>
			<td><%=(obj.get("assertion")!=null)?obj.get("assertion"):"&nbsp;" %></td>
		</tr>
		<% } %>
	</tbody>
	</table>