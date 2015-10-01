<%@ page import="java.util.*"%><%
	String contextType = (String) request.getAttribute("contextType");
	String contextId = (String) request.getAttribute("contextId");

    Map<String, Integer> hypotheticalProteins = (Map) request.getAttribute("hypotheticalProteins");
    Map<String, Integer> functionalProteins = (Map) request.getAttribute("functionalProteins");
    Map<String, Integer> ecAssignedProteins = (Map) request.getAttribute("ecAssignedProteins");
    Map<String, Integer> goAssignedProteins = (Map) request.getAttribute("goAssignedProteins");
    Map<String, Integer> pathwayAssignedProteins = (Map) request.getAttribute("pathwayAssignedProteins");
    Map<String, Integer> figfamAssignedProteins = (Map) request.getAttribute("figfamAssignedProteins");
	Map<String, Integer> plfamAssignedProteins = (Map) request.getAttribute("plfamAssignedProteins");
	Map<String, Integer> pgfamAssignedProteins = (Map) request.getAttribute("pgfamAssignedProteins");
%>
<table class="basic stripe far2x">
<thead>
	<tr>
		<th width="40%"></th>
		<th scope="col" width="30%">PATRIC</th>
		<th scope="col" width="30%">RefSeq</th>
	</tr>
</thead>
<tbody>
	<tr class="alt">
		<th scope="row">Hypothetical proteins</th>
		<td class="right-align-text">
		<% if (hypotheticalProteins != null && hypotheticalProteins.containsKey("PATRIC")) { %>
			<a href="FeatureTable?cType=<%=contextType%>&amp;cId=<%=contextId%>&amp;featuretype=CDS&amp;annotation=PATRIC&amp;filtertype=hypothetical_proteins"><%=hypotheticalProteins.get("PATRIC") %></a>
		<% } else { %>
			0
		<% } %>
		</td>
		<td class="right-align-text last">
		<% if (hypotheticalProteins != null && hypotheticalProteins.containsKey("RefSeq")) { %>
			<a href="FeatureTable?cType=<%=contextType%>&amp;cId=<%=contextId%>&amp;featuretype=CDS&amp;annotation=RefSeq&amp;filtertype=hypothetical_proteins"><%=hypotheticalProteins.get("RefSeq")%></a>
		<% } else { %>
			0
		<% } %>
		</td>
	</tr>
	<tr>
		<th scope="row">Proteins with functional assignments</th>
		<td class="right-align-text">
		<% if (functionalProteins != null && functionalProteins.containsKey("PATRIC")) { %>
			<a href="FeatureTable?cType=<%=contextType%>&amp;cId=<%=contextId%>&amp;featuretype=CDS&amp;annotation=PATRIC&amp;filtertype=functional_proteins"><%=functionalProteins.get("PATRIC") %></a>
		<% } else { %>
			0
		<% } %>
		</td>
		<td class="right-align-text last">
		<% if (functionalProteins != null && functionalProteins.containsKey("RefSeq")) { %>
			<a href="FeatureTable?cType=<%=contextType%>&amp;cId=<%=contextId%>&amp;featuretype=CDS&amp;annotation=RefSeq&amp;filtertype=functional_proteins"><%=functionalProteins.get("RefSeq")%></a>
		<% } else { %>
			0
		<% } %>
		</td>
	</tr>
	<tr class="alt">
		<th scope="row">Proteins with EC number assignments</th>
		<td class="right-align-text">
		<% if (ecAssignedProteins != null && ecAssignedProteins.containsKey("PATRIC")) { %>
			<a href="FeatureTable?cType=<%=contextType%>&amp;cId=<%=contextId%>&amp;featuretype=CDS&amp;annotation=PATRIC&amp;filtertype=ec"><%=ecAssignedProteins.get("PATRIC") %></a>
		<% } else { %>
			0
		<% } %>
		</td>
		<td class="right-align-text last">
		<% if (ecAssignedProteins != null && ecAssignedProteins.containsKey("RefSeq")) { %>
			<a href="FeatureTable?cType=<%=contextType%>&amp;cId=<%=contextId%>&amp;featuretype=CDS&amp;annotation=RefSeq&amp;filtertype=ec"><%=ecAssignedProteins.get("RefSeq")%></a>
		<% } else { %>
			0
		<% } %>
		</td>
	</tr>
	<tr>
		<th scope="row">Proteins with GO assignments</th>
		<td class="right-align-text">
		<% if (goAssignedProteins != null && goAssignedProteins.containsKey("PATRIC")) { %>
			<a href="FeatureTable?cType=<%=contextType%>&amp;cId=<%=contextId%>&amp;featuretype=CDS&amp;annotation=PATRIC&amp;filtertype=go"><%=goAssignedProteins.get("PATRIC") %></a>
		<% } else { %>
			0
		<% } %>
		</td>
		<td class="right-align-text last">
		<% if (goAssignedProteins != null && goAssignedProteins.containsKey("RefSeq")) { %>
			<a href="FeatureTable?cType=<%=contextType%>&amp;cId=<%=contextId%>&amp;featuretype=CDS&amp;annotation=RefSeq&amp;filtertype=go"><%=goAssignedProteins.get("RefSeq")%></a>
		<% } else { %>
			0
		<% } %>
		</td>
	</tr>
	<tr class="alt">
		<th scope="row">Proteins with Pathway assignments</th>
		<td class="right-align-text">
		<% if (pathwayAssignedProteins != null && pathwayAssignedProteins.containsKey("PATRIC")) { %>
			<a href="FeatureTable?cType=<%=contextType%>&amp;cId=<%=contextId%>&amp;featuretype=CDS&amp;annotation=PATRIC&amp;filtertype=pathway"><%=pathwayAssignedProteins.get("PATRIC") %></a>
		<% } else { %>
			0
		<% } %>
		</td>
		<td class="right-align-text last">
		<% if (pathwayAssignedProteins != null && pathwayAssignedProteins.containsKey("RefSeq")) { %>
			<a href="FeatureTable?cType=<%=contextType%>&amp;cId=<%=contextId%>&amp;featuretype=CDS&amp;annotation=RefSeq&amp;filtertype=pathway"><%=pathwayAssignedProteins.get("RefSeq")%></a>
		<% } else { %>
			0
		<% } %>
		</td>
	</tr>
	<tr>
		<th scope="row">Proteins with PATRIC genus-specific families (PLfams) assignments</th>
		<td class="right-align-text">
			<% if (plfamAssignedProteins != null && plfamAssignedProteins.containsKey("PATRIC")) { %>
			<a href="FeatureTable?cType=<%=contextType%>&amp;cId=<%=contextId%>&amp;featuretype=CDS&amp;annotation=PATRIC&amp;filtertype=plfam_id"><%=plfamAssignedProteins.get("PATRIC") %></a>
			<% } else { %>
			0
			<% } %>
		</td>
		<td class="right-align-text last">
			<% if (plfamAssignedProteins != null && plfamAssignedProteins.containsKey("RefSeq")) { %>
			<a href="FeatureTable?cType=<%=contextType%>&amp;cId=<%=contextId%>&amp;featuretype=CDS&amp;annotation=RefSeq&amp;filtertype=plfam_id"><%=plfamAssignedProteins.get("RefSeq")%></a>
			<% } else { %>
			0
			<% } %>
		</td>
	</tr>
	<tr>
		<th scope="row">Proteins with PATRIC cross-genus families (PGfams) assignments</th>
		<td class="right-align-text">
			<% if (pgfamAssignedProteins != null && pgfamAssignedProteins.containsKey("PATRIC")) { %>
			<a href="FeatureTable?cType=<%=contextType%>&amp;cId=<%=contextId%>&amp;featuretype=CDS&amp;annotation=PATRIC&amp;filtertype=pgfam_id"><%=pgfamAssignedProteins.get("PATRIC") %></a>
			<% } else { %>
			0
			<% } %>
		</td>
		<td class="right-align-text last">
			<% if (pgfamAssignedProteins != null && pgfamAssignedProteins.containsKey("RefSeq")) { %>
			<a href="FeatureTable?cType=<%=contextType%>&amp;cId=<%=contextId%>&amp;featuretype=CDS&amp;annotation=RefSeq&amp;filtertype=pgfam_id"><%=pgfamAssignedProteins.get("RefSeq")%></a>
			<% } else { %>
			0
			<% } %>
		</td>
	</tr>
	<tr>
		<th scope="row">Proteins with FIGfam assignments</th>
		<td class="right-align-text">
		<% if (figfamAssignedProteins != null && figfamAssignedProteins.containsKey("PATRIC")) { %>
			<a href="FeatureTable?cType=<%=contextType%>&amp;cId=<%=contextId%>&amp;featuretype=CDS&amp;annotation=PATRIC&amp;filtertype=figfam_id"><%=figfamAssignedProteins.get("PATRIC") %></a>
		<% } else { %>
			0
		<% } %>
		</td>
		<td class="right-align-text last">
		<% if (figfamAssignedProteins != null && figfamAssignedProteins.containsKey("RefSeq")) { %>
			<a href="FeatureTable?cType=<%=contextType%>&amp;cId=<%=contextId%>&amp;featuretype=CDS&amp;annotation=RefSeq&amp;filtertype=figfam_id"><%=figfamAssignedProteins.get("RefSeq")%></a>
		<% } else { %>
			0
		<% } %>
		</td>
	</tr>
</tbody>
</table>
