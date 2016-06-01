<%@ page import="java.util.Map" %>
<%@ page import="edu.vt.vbi.patric.beans.Genome" %>
<%@ page import="java.util.List" %>
<%
String cType = request.getParameter("context_type");
String cId = request.getParameter("context_id");

if (cType.equals("genome")) {

    Genome genome = (Genome) request.getAttribute("genome");
	List<Map> amr = (List<Map>) request.getAttribute("amr");
	boolean isAlter = true;

	%>
	<input type="button" class="button right" title="Add Genome to Workspace" value="Add Genome to Workspace" onclick="saveGenome()" />
	<div style="clear:right;height:3px"></div> 
	<table class="basic stripe far2x" id="metadata-td">
	<tbody>
	<tr>
		<th scope="row" colspan="2">Summary</th>
		<td>
		    Length: <%=genome.getGenomeLength() %>bp, Chromosomes: <%=genome.getChromosomes() %>,
		    Plasmids: <%=genome.getPlasmids() %>, Contigs: <%=genome.getContigs() %>
		</td>
	</tr>
	</tbody>
	<tr>
		<td class="no-underline-links" colspan="3" id="click_for_more"></td>
	</tr>
	</table>

	<% if (amr != null && amr.size() > 0) { %>
	<table class="basic stripe far2x">
	<thead>
		<tr>
			<th rowspan="2">Antibiotic</th>										<%-- antibiotic --%>
			<th rowspan="2">Resistance phenotype</th>							<%-- resistant_phenotype --%>
			<th colspan="3">Measurement</th>
			<th colspan="4">Laboratory typing</th>
			<th rowspan="2">Testing standard</th>								<%-- testing_standard --%>
		</tr>
		<tr>
			<th>sign</th>								<%-- measurement_sign --%>
			<th>value</th>									<%-- measurement_value --%>
			<th>units</th>								<%-- measurement_unit --%>
			<th>method</th>						<%-- laboratory_typing_method --%>
			<th>platform</th>						<%-- laboratory_typing_platform --%>
			<th>Vendor</th>											<%-- vendor --%>
			<th>method version or reagent</th>	<%-- laboratory_typing_method_version --%>
		</tr>
	</thead>
	<tbody>
		<% for (Map row : amr) { %>
		<tr <%=(isAlter)?" class=\"alt\"":""%><% isAlter = !isAlter; %>>
			<td><%=(row.containsKey("antibiotic"))?row.get("antibiotic"):"&nbsp;"%>&nbsp;</td>
			<td><%=(row.containsKey("resistant_phenotype"))?row.get("resistant_phenotype"):"&nbsp;"%>&nbsp;</td>
			<td><%=(row.containsKey("measurement_sign"))?row.get("measurement_sign"):"&nbsp;"%>&nbsp;</td>
			<td><%=(row.containsKey("measurement_value"))?row.get("measurement_value"):"&nbsp;"%>&nbsp;</td>
			<td><%=(row.containsKey("measurement_unit"))?row.get("measurement_unit"):"&nbsp;"%>&nbsp;</td>
			<td><%=(row.containsKey("laboratory_typing_method"))?row.get("laboratory_typing_method"):"&nbsp;"%>&nbsp;</td>
			<td><%=(row.containsKey("laboratory_typing_platform"))?row.get("laboratory_typing_platform"):"&nbsp;"%>&nbsp;</td>
			<td><%=(row.containsKey("vendor"))?row.get("vendor"):"&nbsp;"%>&nbsp;</td>
			<td><%=(row.containsKey("laboratory_typing_method_version"))?row.get("laboratory_typing_method_version"):"&nbsp;"%>&nbsp;</td>
			<td><%=(row.containsKey("testing_standard"))?row.get("testing_standard"):"&nbsp;"%>&nbsp;</td>
		</tr>
		<% } %>
	</tbody>
	</table>
	<% } %>
<%
} else if (cType.equals("taxon")) {

	Map patric_counts = (Map) request.getAttribute("PATRIC");
	Map refseq_counts = (Map) request.getAttribute("RefSeq");

	Map patric_status = (Map) ((Map) patric_counts.get("facets")).get("genome_status");
	Map refseq_status = (Map) ((Map) refseq_counts.get("facets")).get("genome_status");
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
		<th scope="row">Number of genomes</th>
		<td class="right-align-text">
			<% if (patric_counts != null && patric_counts.containsKey("total") && (Integer) patric_counts.get("total") > 0) { %>
			<a href="GenomeList?cType=taxon&cId=<%=cId%>&displayMode=&dataSource=PATRIC&pk="><%=patric_counts.get("total") %></a>
			<% } else { %> 0 <% } %>
		</td>
		<td class="right-align-text last">
			<% if (refseq_counts != null && refseq_counts.containsKey("total") && (Integer) refseq_counts.get("total") > 0) { %>
				<a href="GenomeList?cType=taxon&cId=<%=cId%>&displayMode=&dataSource=RefSeq&pk="><%=refseq_counts.get("total") %></a>
			<% } else { %> 0 <% } %>
		</td>
	</tr>
	<tr>
		<th scope="row">Number of Complete genomes</th>
		<td class="right-align-text">
			<% if (patric_status != null && patric_status.containsKey("Complete") && (Integer) patric_status.get("Complete") > 0) { %>
				<a href="GenomeList?cType=taxon&cId=<%=cId%>&displayMode=Complete&dataSource=PATRIC&pk="><%=patric_status.get("Complete") %></a>
			<% } else { %> 0 <% } %>
		</td>
		<td class="right-align-text last">
			<% if (refseq_status != null && refseq_status.containsKey("Complete") && (Integer) refseq_status.get("Complete") > 0) { %>
				<a href="GenomeList?cType=taxon&cId=<%=cId%>&displayMode=Complete&dataSource=RefSeq&pk="><%=refseq_status.get("Complete") %></a>
			<% } else { %> 0 <% } %>
		</td>
	</tr>
	<tr class="alt">
		<th scope="row">Number of WGS genomes</th>
		<td class="right-align-text">
			<% if (patric_status != null && patric_status.containsKey("WGS") && (Integer) patric_status.get("WGS") > 0) { %>
				<a href="GenomeList?cType=taxon&cId=<%=cId%>&displayMode=WGS&dataSource=PATRIC&pk="><%=patric_status.get("WGS") %></a>
			<% } else { %> 0 <% } %>
		</td>
		<td class="right-align-text last">
			<% if (refseq_status != null && refseq_status.containsKey("WGS") && (Integer) refseq_status.get("WGS") > 0) { %>
			<a href="GenomeList?cType=taxon&cId=<%=cId%>&displayMode=WGS&dataSource=RefSeq&pk="><%=refseq_status.get("WGS") %></a>
			<% } else { %> 0 <% } %>
		</td>
	</tr>
	<tr>
		<th scope="row">Number of Plasmid only genomes</th>
		<td class="right-align-text">
			<% if (patric_status != null && patric_status.containsKey("Plasmid") && (Integer) patric_status.get("Plasmid") > 0) { %>
				<a href="GenomeList?cType=taxon&cId=<%=cId%>&displayMode=Plasmid&dataSource=PATRIC&pk="><%=patric_status.get("Plasmid") %></a>
			<% } else { %> 0 <% } %>
		</td>
		<td class="right-align-text last">
			<% if (refseq_status != null && refseq_status.containsKey("Plasmid") && (Integer) refseq_status.get("Plasmid") > 0) { %>
				<a href="GenomeList?cType=taxon&cId=<%=cId%>&displayMode=Plasmid&dataSource=RefSeq&pk="><%=refseq_status.get("Plasmid") %></a>
			<% } else { %> 0 <% } %>
		</td>
	</tr>
	</tbody>
	</table>
<% } %>