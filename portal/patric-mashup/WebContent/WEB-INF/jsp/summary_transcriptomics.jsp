<%@ page import="java.util.Map"
%><%@ page import="org.json.simple.JSONObject"
%><%
String contextType = (String) request.getAttribute("contextType");
String contextId = (String) request.getAttribute("contextId");
String speciesName = (String) request.getAttribute("speciesName");
Map<String, String> gds_taxon = (Map) request.getAttribute("gds_taxon");
Map<String, String> gds_keyword = (Map) request.getAttribute("gds_keyword");
JSONObject arex_keyword = (JSONObject) request.getAttribute("arex_keyword");
JSONObject arex_species = (JSONObject) request.getAttribute("arex_species");
String errorMsg = (String) request.getAttribute("errorMsg");
%>
	<p>Genome-wide gene expression profiling datasets are retrieved from ArrayExpress, GEO and  PRC post-genomic databases as listed below. 
	They include cDNA microarrays and oligo-microarrays, cDNA-AFLP , SAGE and RNA-Seq.</p>
	
	<table class="basic far2x">
	<thead>
		<tr>
			<th scope="row" width="30%">Data Source</th>
			<th scope="col" width="35%"><a href="//www.ncbi.nlm.nih.gov/geo/" target="_blank"><img src="/patric/images/logo_geo.png" alt="GEO" /></a>
				<br/><a href="//www.ncbi.nlm.nih.gov/geo/" target="_blank">Gene Expression Omnibus</a>
			</th>
			<th scope="col" width="35%"><a href="//www.ebi.ac.uk/microarray-as/ae/" target="_blank"><img src="/patric/images/logo_arrayexpress.png" alt="ArrayExpress" /></a>
				<br/><a href="//www.ebi.ac.uk/microarray-as/ae/" target="_blank">ArrayExpress</a></th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<th scope="row">Taxonomy search</th>
			<td class="right-align-text"><!-- GEO/taxonomy -->
				<% 
				if (gds_taxon == null || gds_taxon.get("hasData").equals("false")) { 
					%><%=errorMsg%><%
				} else if (gds_taxon.get("Count").equalsIgnoreCase("0")) { 
					%>0<% 
				} else {
					%><a href="GEO?cType=<%=contextType %>&amp;cId=<%=contextId%>&amp;filter=&amp;keyword="><%=gds_taxon.get("Count") %></a><%
				} %>
			</td>
			<td class="right-align-text"><!-- ArrayExpress/Taxonomy -->N/A</td>
		</tr>
		<tr>
			<th scope="row">Keyword search</th>
			<td class="right-align-text"><!-- GEO/keyword -->
				<% 
				if (gds_keyword == null || gds_keyword.get("hasData").equals("false")) {
					%><%=errorMsg%><%
				}
				else if (gds_keyword.get("Count").equalsIgnoreCase("0")) { 
					%>0<% 
				} else {
					%><a href="GEO?cType=<%=contextType%>&amp;cId=<%=contextId%>&amp;filter=&amp;keyword=<%=speciesName %>"><%=gds_keyword.get("Count") %></a><%
				} %>
			</td>
			<td class="right-align-text"><!-- ArrayExpress/Keyword -->
				<% 
				if (arex_keyword.get("hasData").equals(false)) {
					%><%=errorMsg%><%
				}
				else if (arex_keyword.get("total").equals(0)) { 
					%>0<% 
				} else {
					%><a href="ArrayExpress?cType=<%=contextType%>&amp;cId=<%=contextId%>&amp;kw=<%=speciesName%>"><%=arex_keyword.get("total") %></a><%
				}%>
			</td>
		</tr>
		<tr>
			<th scope="row">Species search</th>
			<td class="right-align-text"><!-- GEO/Species -->N/A</td>
			<td class="right-align-text"><!-- ArrayExpress/Species -->
				<% 
				if (arex_species.get("hasData").equals(false)) {
					%><%=errorMsg%><%
				} else if (arex_species.get("total").equals(0)) { 
					%>0<% 
				} else {
					%><a href="ArrayExpress?cType=<%=contextType%>&amp;cId=<%=contextId%>&amp;kw="><%=arex_species.get("total") %></a><%
				} %>
			</td>
		</tr>
	</tbody>
	</table>