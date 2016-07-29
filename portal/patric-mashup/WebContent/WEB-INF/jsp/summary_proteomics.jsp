<%@ page import="org.json.simple.JSONObject"
%><%
String contextType = (String) request.getAttribute("contextType");
String contextId = (String) request.getAttribute("contextId");
JSONObject result = (JSONObject) request.getAttribute("result");
String errorMsg = (String) request.getAttribute("errorMsg");
%>
	<p>Experiment datasets of large-scale studies of proteins are retrieved from PRIDE and PRC post-genomic databases as listed below.</p>
	
	<table class="basic far2x">
	<thead>
		<tr>
			<th scope="row" width="60%">Data Source</th>
			<th scope="col" width="40%"><a href="//www.ebi.ac.uk/pride/" target="_blank"><img src="/patric/images/logo_pride.png" alt="PRIDE" /></a><br/>
				<a href="http://www.ebi.ac.uk/pride/" target="_blank">PRoteomics IDEntification database (PRIDE)</a></th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<th scope="row">Taxonomy search</th>
			<td class="right-align-text"><!-- PRIDE/Taxonomy -->N/A</td>
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
		</tr>
	</tbody>
	</table>