<%@ page import="java.util.*"
%><%@ page import="edu.vt.vbi.patric.common.SiteHelper"
%><%
String contextType = (String) request.getAttribute("contextType");
String contextId = (String) request.getAttribute("contextId");

Map<String, Map<String, Integer>> summary = (Map<String, Map<String, Integer>>) request.getAttribute("summary");

boolean alt = false;
%>
<table class="basic stripe far2x">
<thead>
	<tr>
		<th scope="col" width="40%"></th>
		<th scope="col" width="20%">Source</th>
		<th scope="col" width="40%">Genes</th>
	</tr>
</thead>
<tbody>
	<%
	for (Map.Entry<String, Map<String, Integer>> entry: summary.entrySet()) {
	    String property = entry.getKey();

        for (Map.Entry<String, Integer> row: entry.getValue().entrySet()) {
            String source = row.getKey();
            int count = row.getValue();

            alt = !alt;
	%>
	<tr <%=(alt)?"class=\"alt\"":"" %>>
		<th scope="row"><%=property %></th>
		<th scope="row" class="no-underline-links">
		<% if (!SiteHelper.getExternalLinks(source + "_HOME").equals("")) { %>
			<a class="arrow-slate-e" href="<%=SiteHelper.getExternalLinks(source+"_HOME") %>" target="_blank"><%=source %></a>
		<% } else { %>
			<%=source %>
		<% } %>
		</th>
		<td class="right-align-text">
		<% if (count != 0) { %>
			<a href="SpecialtyGeneList?cType=<%=contextType%>&amp;cId=<%=contextId%>&kw=source:%22<%=source%>%22"><%=count%></a>
		<% } else { %>
			0
		<% } %>
		</td>
	</tr>
	<%
	    }
	}
	%>
</tbody>
</table>
