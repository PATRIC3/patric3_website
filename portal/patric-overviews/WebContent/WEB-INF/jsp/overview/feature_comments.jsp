<%@ page import="java.util.*"%><%

List<Map<String, Object>> listAnnotation = (List<Map<String, Object>>) request.getAttribute("listAnnotation");

%>
	<h3 class="section-title normal-case close2x"><span class="wrap">Comments</span></h3>
	<table class="basic stripe far2x">
	<thead>
		<tr>
			<th scope="col">Source</th>
			<th scope="col">Property</th>
			<th scope="col">Value</th>
			<th scope="col">Evidence Code</th>
			<th scope="col">Comment</th>
		</tr>
	</thead>
	<tbody>
	<% for (Map<String, Object> an: listAnnotation) { %>
		<tr>
			<td><%=an.get("source") %>&nbsp;</td>
			<td><%=an.get("property") %>&nbsp;</td>
			<td><%=an.get("value") %>&nbsp;</td>
			<td><%=an.get("evidence_code") %>&nbsp;</td>
			<td><%=(an.get("comment")!=null)?an.get("comment").toString().replaceAll("\"\"","\""):"" %>&nbsp;</td>
		</tr>
	<% } %>
	</tbody>
	</table>