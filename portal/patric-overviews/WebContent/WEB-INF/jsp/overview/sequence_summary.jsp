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

	Map<String, Long> patric_counts = (Map<String, Long>) request.getAttribute("PATRIC");
	Map<String, Long> refseq_counts = (Map<String, Long>) request.getAttribute("RefSeq");
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
			<% if (patric_counts != null && patric_counts.containsKey("Total") && patric_counts.get("Total") > 0) { %>
			<a href="GenomeList?cType=taxon&cId=<%=cId%>&displayMode=&dataSource=PATRIC&pk="><%=patric_counts.get("Total") %></a>
			<% } else { %> 0 <% } %>
		</td>
		<td class="right-align-text last">
			<% if (refseq_counts != null && refseq_counts.containsKey("Total") && refseq_counts.get("Total") > 0) { %>
				<a href="GenomeList?cType=taxon&cId=<%=cId%>&displayMode=&dataSource=RefSeq&pk="><%=refseq_counts.get("Total") %></a>
			<% } else { %> 0 <% } %>
		</td>
	</tr>
	<tr>
		<th scope="row">Number of Complete genomes</th>
		<td class="right-align-text">
			<% if (patric_counts != null && patric_counts.containsKey("Complete") && patric_counts.get("Complete") > 0) { %>
				<a href="GenomeList?cType=taxon&cId=<%=cId%>&displayMode=Complete&dataSource=PATRIC&pk="><%=patric_counts.get("Complete") %></a>
			<% } else { %> 0 <% } %>
		</td>
		<td class="right-align-text last">
			<% if (refseq_counts != null && refseq_counts.containsKey("Complete") && refseq_counts.get("Complete") > 0) { %>
				<a href="GenomeList?cType=taxon&cId=<%=cId%>&displayMode=Complete&dataSource=RefSeq&pk="><%=refseq_counts.get("Complete") %></a>
			<% } else { %> 0 <% } %>
		</td>
	</tr>
	<tr class="alt">
		<th scope="row">Number of WGS genomes</th>
		<td class="right-align-text">
			<% if (patric_counts != null && patric_counts.containsKey("WGS") && patric_counts.get("WGS") > 0) { %>
				<a href="GenomeList?cType=taxon&cId=<%=cId%>&displayMode=WGS&dataSource=PATRIC&pk="><%=patric_counts.get("WGS") %></a>
			<% } else { %> 0 <% } %>
		</td>
		<td class="right-align-text last">
			<% if (refseq_counts != null && refseq_counts.containsKey("WGS") && refseq_counts.get("WGS") > 0) { %>
			<a href="GenomeList?cType=taxon&cId=<%=cId%>&displayMode=WGS&dataSource=RefSeq&pk="><%=refseq_counts.get("WGS") %></a>
			<% } else { %> 0 <% } %>
		</td>
	</tr>
	<tr>
		<th scope="row">Number of Plasmid only genomes</th>
		<td class="right-align-text">
			<% if (patric_counts != null && patric_counts.containsKey("Plasmid") && patric_counts.get("Plasmid") > 0) { %>
				<a href="GenomeList?cType=taxon&cId=<%=cId%>&displayMode=Plasmid&dataSource=PATRIC&pk="><%=patric_counts.get("Plasmid") %></a>
			<% } else { %> 0 <% } %>
		</td>
		<td class="right-align-text last">
			<% if (refseq_counts != null && refseq_counts.containsKey("Plasmid") && refseq_counts.get("Plasmid") > 0) { %>
				<a href="GenomeList?cType=taxon&cId=<%=cId%>&displayMode=Plasmid&dataSource=RefSeq&pk="><%=refseq_counts.get("Plasmid") %></a>
			<% } else { %> 0 <% } %>
		</td>
	</tr>
	</tbody>
	</table>
<% } %>