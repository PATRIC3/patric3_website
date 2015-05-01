<%@ page import="java.util.Map" %>
<%@ page import="edu.vt.vbi.patric.beans.Genome" %>
<%
String cType = request.getParameter("context_type");
String cId = request.getParameter("context_id");

if (cType.equals("genome")) {

    Genome genome = (Genome) request.getAttribute("genome");

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