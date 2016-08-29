<%@ page import="java.util.Map"
%><%@ page import="org.json.simple.JSONObject"
%><%

String cType = (String) request.getAttribute("cType");
String cId = (String) request.getAttribute("cId");
String errorMsg = (String) request.getAttribute("errorMsg");
Map<String, String> gds_taxon = (Map) request.getAttribute("gds_taxon");
JSONObject arex_keyword = (JSONObject) request.getAttribute("arex_keyword");
Map<String, String> st = (Map) request.getAttribute("st");
//String result = (String) request.getAttribute("result");
String species_name = (String) request.getAttribute("species_name");

%>
	<table class="basic far2x">
	<tbody>
	<tr>
		<th scope="row" width="75%">Transcriptomics from GEO</th>
		<td class="right-align-text"><!-- GEO/keyword -->
			<% 
			if (gds_taxon == null || gds_taxon.get("hasData").equals("false")) {
				%><%=errorMsg%><%
			}
			else if (gds_taxon.get("Count").equalsIgnoreCase("0")) { 
				%>0<% 
			} else {
				%><a href="GEO?cType=<%=cType %>&amp;cId=<%=cId%>&amp;filter=&amp;keyword="><%=gds_taxon.get("Count") %></a><%
			} %>
		</td>
	</tr>
	<tr>
		<th scope="row">Transcriptomics from ArrayExpress</th>
		<td class="right-align-text"><!-- ArrayExpress/Keyword -->
			<% 
			if (arex_keyword.get("hasData").equals(false)) {
				%><%=errorMsg%><%
			}
			else if (arex_keyword.get("total").equals(0)) { 
				%>0<% 
			} else {
				%><a href="ArrayExpress?cType=<%=cType%>&amp;cId=<%=cId%>&amp;kw=<%=species_name%>"><%=arex_keyword.get("total") %></a><%
			}%>
		</td>
	</tr>
	<tr>
		<th scope="row">Structure from NCBI</th>
		<td class="right-align-text"><% 
			if (st == null || st.get("hasData").equals("false")) {
				%><%=errorMsg%><%
			}
			else if (st.get("Count").equalsIgnoreCase("0")) { 
				%>0<% 
			} else {
				%><a href="Structure?cType=<%=cType%>&amp;cId=<%=cId%>&amp;filter="><%=st.get("Count") %></a><%
			} %>
		</td>
	</tr><%--
	<tr>
		<th scope="row">Protein Protein Interaction from IntAct</th>
		<td class="right-align-text"><%
			if (result.equals("-1")) {
				%><%=errorMsg %><%
			}
			else if (result.equalsIgnoreCase("0")) { 
				%>0<% 
			} else {
				%><a href="IntAct?cType=<%=cType%>&amp;cId=<%=cId%>"><%=result %></a><%
			}%>
		</td>
	</tr>
	<tr>
		<td class="no-underline-links" colspan="2">
			<a class="double-arrow-link" href="ExperimentData?cType=taxon&cId=<%=cId %>&kw=Experiment%20Data">more</a>
		</td>
	</tr>--%>
	</tbody>
	</table>