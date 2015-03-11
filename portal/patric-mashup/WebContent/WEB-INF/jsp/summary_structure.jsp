<%@ page import="java.util.Map"
%><%
String contextType = (String) request.getAttribute("contextType");
String contextId = (String) request.getAttribute("contextId");
String speciesName = (String) request.getAttribute("speciesName");
Map<String, String> st = (Map) request.getAttribute("st");
Map<String, String> st_ssgcid = (Map) request.getAttribute("st_ssgcid");
Map<String, String> st_csgid = (Map) request.getAttribute("st_csgid");
String errorMsg = (String) request.getAttribute("errorMsg");
%>
		<p>Protein structure data of <%=speciesName %> are retrieved from Protein Data Bank.</p>
		
		<table class="basic far2x">
		<thead>
			<tr>
				<th scope="row" width="75%">Data Source</th>
				<th scope="col"><a href="//www.ncbi.nlm.nih.gov/Structure/" target="_blank"><img src="/patric/images/logo_ncbi.png" alt="NCBI Structure" /></a>
					<br/><a href="//www.ncbi.nlm.nih.gov/Structure/" target="_blank">NCBI Structure</a></th>
			</tr>
		</thead>
		<tbody>
			<tr>
				<th scope="row">Taxonomy search</th>
				<td class="right-align-text"><% 
				if (st == null || st.get("hasData").equals("false")) {
					%><%=errorMsg%><%
				}
				else if (st.get("Count").equalsIgnoreCase("0")) { 
					%>0<% 
				} else {
					%><a href="Structure?cType=<%=contextType%>&amp;cId=<%=contextId%>&amp;filter="><%=st.get("Count") %></a><%
				} %>
				</td>
			</tr>
			<tr>
				<th scope="row">Taxonomy search - Seattle Structural Genomics Center for Infectious Disease (SSGCID)</th>
				<td class="right-align-text"><% 
				if (st_ssgcid == null || st_ssgcid.get("hasData").equals("false")) {
					%><%=errorMsg%><%
				}
				else if (st_ssgcid.get("Count").equalsIgnoreCase("0")) { 
					%>0<% 
				} else {
					%><a href="Structure?cType=<%=contextType%>&amp;cId=<%=contextId%>&amp;filter=ssgcid"><%=st_ssgcid.get("Count") %></a><%
				} %>
				</td>
			</tr>
			<tr>
				<th scope="row">Taxonomy search - Center for Structural Genomics of Infectious Diseases (CSGID)</th>
				<td class="right-align-text"><% 
				if (st_csgid == null || st_csgid.get("hasData").equals("false")) {
					%><%=errorMsg%><%
				}
				else if (st_csgid.get("Count").equalsIgnoreCase("0")) { 
					%>0<% 
				} else {
					%><a href="Structure?cType=<%=contextType%>&amp;cId=<%=contextId%>&amp;filter=csgid"><%=st_csgid.get("Count") %></a><%
				} %>
				</td>
			</tr>
		</tbody>
		</table>