<%@ page import="java.util.*"
%><%@ page import="edu.vt.vbi.patric.common.SiteHelper"
%><%
List<Map<String, Object>> listSpecialtyGenes = (List<Map<String, Object>>) request.getAttribute("listSpecialtyGenes");

%>
	<h3 class="section-title normal-case close2x"><span class="wrap">Special Properties</span></h3>
	<table class="basic stripe far2x">
	<thead>
		<tr>
			<th scope="col">Evidence</th>
			<th scope="col">Property</th>
			<th scope="col">Source</th>
			<th scope="col">Source ID</th>
			<th scope="col">Organism</th>
			<th scope="col">PubMed</th>
			<th scope="col">Subject Coverage</th>
			<th scope="col">Query Coverage</th>
			<th scope="col">Identity</th>
			<th scope="col">E-value</th>
		</tr>
	</thead>
	<tbody>
	<%
	boolean alt = false;
	for (Map<String, Object> prop: listSpecialtyGenes) {
	    alt = !alt;
	%>
		<tr <%=(alt)?" class=\"alt\"":"" %>>
			<td><%=prop.get("evidence") %></td>
			<td><%=prop.get("property") %></td>
			<td class="no-underline-links"><% 
				if (SiteHelper.getExternalLinks(prop.get("source").toString()+"_HOME").equals("") == false) {
					%><a class="arrow-slate-e" href="<%=SiteHelper.getExternalLinks(prop.get("source").toString()+"_HOME")%>" target=_blank><%=prop.get("source") %></a><%
				} else {
					%><%=prop.get("source") %><%
				} %>
			</td>
			<td class="no-underline-links"><% 
				if (SiteHelper.getExternalLinks(prop.get("source").toString()).equals("") == false) {
					%><a class="arrow-slate-e" href="<%=SiteHelper.getExternalLinks(prop.get("source").toString())%><%=prop.get("sourceId")%>" target=_blank><%=prop.get("sourceId") %></a><%
				} else {
					%><%=prop.get("sourceId") %><%
				}
			%>
			</td>
			<td><%=(prop.get("organism")!=null)?prop.get("organism"):"&nbsp;" %></td>
			<td><%=(prop.get("pmid")!=null)?"<a class=\"arrow-slate-e\" href=\"//www.ncbi.nlm.nih.gov/pubmed/" + prop.get("pmid").toString().replaceAll(" ","") + "\" target=_blank>"+prop.get("pmid")+"</a>":"&nbsp;" %></td>
			<td><%=(prop.get("subjectCoverage")!=null)?prop.get("subjectCoverage"):"&nbsp;" %></td>
			<td><%=(prop.get("queryCoverage")!=null)?prop.get("queryCoverage"):"&nbsp;" %></td>
			<td><%=(prop.get("identity")!=null)?prop.get("identity"):"&nbsp;" %></td>
			<td><%=(prop.get("eValue")!=null)?prop.get("eValue"):"&nbsp;" %></td>
		</tr>
	<% } %>
	</tbody>
	</table>