<%
String contextType = (String) request.getAttribute("contextType");
String contextId = (String) request.getAttribute("contextId");
String speciesName = (String) request.getAttribute("speciesName");
String result = (String) request.getAttribute("result");
int result_pi = (Integer) request.getAttribute("result_pi");
String errorMsg = (String) request.getAttribute("errorMsg");

%>
	<p>Interaction data of <%=speciesName %> are retrieved from some prominent databases, e.g.,  InAct and PRC and displayed below.
		 Interaction experiment data covers protein-protein, protein-DNA, protein-carbohydrate and antibody-antigen. 
	</p>
	
	<table class="basic far2x">
	<thead>
		<tr>
			<th scope="row" width="25%">Data Source</th>
			<th scope="col" width="38%"><a href="http://www.ebi.ac.uk/intact/" target="_blank"><img src="/patric/images/logo_intact.png" alt="IntAct" /></a>
				<br/><a href="http://www.ebi.ac.uk/intact/" target="_blank">IntAct</a></th>
			<th scope="col" width="37%"><a href="http://pathogenportal.net/portal/portal/PathPort/Data+Set+Summary/prc" target="_blank"><img src="/patric/images/logo_prc.png" alt="Proteomics Resource Center" /></a>
				<br/><a href="http://pathogenportal.net/portal/portal/PathPort/Data+Set+Summary/prc" target="_blank">Proteomics Resource Center</a></th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<th scope="row">Taxonomy search</th>
			<td class="right-align-text"><%
				if (result.equals("-1")) {
					%><%=errorMsg %><%
				}
				else if (result.equalsIgnoreCase("0")) { 
					%>0<% 
				} else {
					%><a href="IntAct?cType=<%=contextType%>&amp;cId=<%=contextId%>"><%=result %></a><%
				}%>
			</td>
			<td class="right-align-text"><%
				if (result_pi < 0) {
					%><%=errorMsg %><%
				}
				else if (result_pi == 0) { 
					%>0<% 
				} else {
					%><a href="PRC?cType=<%=contextType%>&amp;cId=<%=contextId%>&amp;filter=PI"><%=result_pi %></a><%
				}%>
			</td>
		</tr>
	</tbody>
	</table>