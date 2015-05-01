<%@ page import="java.util.*"
%><%@ page import="edu.vt.vbi.patric.common.SiteHelper"
%><%@ page import="edu.vt.vbi.patric.beans.SpecialtyGene"
%><%
	List<SpecialtyGene> listSpecialtyGenes = (List) request.getAttribute("listSpecialtyGenes");
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
	for (SpecialtyGene prop: listSpecialtyGenes) {
	    alt = !alt;
	%>
		<tr <%=(alt)?" class=\"alt\"":"" %>>
			<td><%=prop.getEvidence() %></td>
			<td><%=prop.getProperty() %></td>
			<td class="no-underline-links"><% 
				if (!SiteHelper.getExternalLinks(prop.getSource() + "_HOME").equals("")) {
					%><a class="arrow-slate-e" href="<%=SiteHelper.getExternalLinks(prop.getSource()+"_HOME")%>" target=_blank><%=prop.getSource() %></a><%
				} else {
					%><%=prop.getSource() %><%
				} %>
			</td>
			<td class="no-underline-links"><% 
				if (!SiteHelper.getExternalLinks(prop.getSource()).equals("")) {
					%><a class="arrow-slate-e" href="<%=SiteHelper.getExternalLinks(prop.getSource())%><%=prop.getSourceId()%>" target=_blank><%=prop.getSourceId() %></a><%
				} else {
					%><%=prop.getSourceId() %><%
				}
			%>
			</td>
			<td><%=prop.hasOrganism()?prop.getOrganism():"&nbsp;" %></td>
			<td><%=prop.hasPmid()?"<a class=\"arrow-slate-e\" href=\"//www.ncbi.nlm.nih.gov/pubmed/" + prop.getPmid().replaceAll(" ","") + "\" target=_blank>"+prop.getPmid()+"</a>":"&nbsp;" %></td>
			<td><%=prop.hasSubjectCoverage()?prop.getSubjectCoverage():"&nbsp;" %></td>
			<td><%=prop.hasQueryCoverage()?prop.getQueryCoverage():"&nbsp;" %></td>
			<td><%=prop.hasIdentity()?prop.getIdentity():"&nbsp;" %></td>
			<td><%=prop.haseValue()?prop.geteValue():"&nbsp;" %></td>
		</tr>
	<% } %>
	</tbody>
	</table>