<%@ page import="java.util.*" %>
<%
Map<String, Map<String, Long>> summary = (Map<String, Map<String, Long>>) request.getAttribute("summary");

String contextLink = (String) request.getAttribute("contextLink");

String viewOption = (String) request.getAttribute("viewOption");
%>
	<table class="basic stripe far2x">
	<thead>
		<tr>
			<th width="40%"></th>
			<th scope="col" width="20%">PATRIC</th>
			<th scope="col" width="20%">BRC1</th>
			<th scope="col" width="20%">RefSeq</th>
		</tr>
	</thead>
	<tbody>
	<%
		boolean alt = false;

		for (Map.Entry<String, Map<String, Long>> entry: summary.entrySet()) {

            String type = entry.getKey();
            Map<String, Long> counts = entry.getValue();

            alt = !alt;
            String patric, brc, refseq;

            if (counts.containsKey("PATRIC")) {
                patric = "<a href=\"FeatureTable?" + contextLink + "&amp;featuretype=" + type + "&amp;annotation=PATRIC&amp;filtertype=\">" + counts.get("PATRIC") + "</a>";
            } else {
                patric = "0";
            }
            if (counts.containsKey("BRC1")) {
                brc = "<a href=\"FeatureTable?" + contextLink + "&amp;featuretype=" + type + "&amp;annotation=BRC&amp;filtertype=\">" + counts.get("BRC1") + "</a>";
            } else {
                brc = "0";
            }
            if (counts.containsKey("RefSeq")) {
                refseq = "<a href=\"FeatureTable?" + contextLink + "&amp;featuretype=" + type + "&amp;annotation=RefSeq&amp;filtertype=\">" + counts.get("RefSeq") + "</a>";
            } else {
                refseq = "0";
            }
		%>
			<tr <%=(alt)?"class=\"alt\"":"" %>>
				<th scope="row"><%=type %></th>
				<td class="right-align-text"><%=patric %></td>
				<td class="right-align-text"><%=brc %></td>
				<td class="right-align-text last"><%=refseq %></td>
			</tr>
		<%
		}
	%>
		<tr>
			<td class="no-underline-links" colspan="4"><%
				if (viewOption.equals("full")) {
					%><a class="arrow-slate-e-up" href="javascript:void(0)" onClick="showShortList()">View less feature types</a><%
				} else {
					%><a class="arrow-slate-e-down" href="javascript:void(0)" onClick="showFullList()">View more feature types</a><%
				}
			%></td>
		</tr>
	</tbody>
	</table>
