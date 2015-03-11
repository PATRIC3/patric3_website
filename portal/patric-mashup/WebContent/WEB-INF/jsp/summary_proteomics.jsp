<%@ page import="org.json.simple.JSONObject"
%><%
String contextType = (String) request.getAttribute("contextType");
String contextId = (String) request.getAttribute("contextId");
JSONObject result = (JSONObject) request.getAttribute("result");
int result_ms = (Integer) request.getAttribute("result_ms");
String errorMsg = (String) request.getAttribute("errorMsg");
%>
	<p>Experiment datasets of large-scale studies of proteins are retrieved from PRIDE and PRC post-genomic databases as listed below.</p>
	
	<table class="basic far2x">
	<thead>
		<tr>
			<th scope="row" width="25%">Data Source</th>
			<th scope="col" width="38%"><a href="//www.ebi.ac.uk/pride/" target="_blank"><img src="/patric/images/logo_pride.png" alt="PRIDE" /></a><br/>
				<a href="http://www.ebi.ac.uk/pride/" target="_blank">PRoteomics IDEntification database (PRIDE)</a></th>
			<th scope="col" width="37%"><a href="//pathogenportal.net/portal/portal/PathPort/Data+Set+Summary/prc" target="_blank"><img src="/patric/images/logo_prc.png" alt="Proteomics Resource Center" /></a>
				<br/><a href="//pathogenportal.net/portal/portal/PathPort/Data+Set+Summary/prc" target="_blank">Proteomics Resource Center</a></th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<th scope="row">Taxonomy search</th>
			<td class="right-align-text"><!-- PRIDE/Taxonomy -->N/A</td>
			<td class="right-align-text"><!-- PRC/Taxonomy -->
				<% 
				if (result_ms < 0) {
					%><%=errorMsg%><%
				}
				else if (result_ms == 0) { 
					%>0<% 
				} else {
					%><a href="PRC?cType=<%=contextType%>&amp;cId=<%=contextId%>&amp;filter=MS"><%=result_ms %></a><%
				} %>
			</td>
		</tr>
		<tr>
			<th scope="row">Species search</th>
			<td class="right-align-text"><!-- PRIDE/Species -->
				<%
				if (result.get("hasData").equals(false)) {
					%><%=errorMsg%><%
				}
				else if (result.get("total").equals(0)) {
					%>0<%
				} else {
					%><a href="PRIDE?cType=<%=contextType%>&amp;cId=<%=contextId%>"><%=result.get("total") %></a><%
				}
			%>
			</td>
			<td class="right-align-text"><!-- PRC/Species -->N/A</td>
		</tr>
	</tbody>
	</table>