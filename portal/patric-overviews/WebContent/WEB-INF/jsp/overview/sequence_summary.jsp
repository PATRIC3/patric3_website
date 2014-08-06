<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="edu.vt.vbi.patric.dao.DBShared" %>
<%@ page import="edu.vt.vbi.patric.dao.DBSummary" %>
<%@ page import="edu.vt.vbi.patric.dao.ResultType" %>
<%
String cType = request.getParameter("context_type");
String cId = request.getParameter("context_id");

if (cType != null && cType.equals("genome")) {

	DBSummary conn_summary = new DBSummary();
	ResultType d = conn_summary.getGenomeSummary(cId);
	%>
	<input type="button" class="button right" title="Add Genome to Workspace" value="Add Genome to Workspace" onclick="saveGenome()" />
	<div style="clear:right;height:3px"></div> 
	<table class="basic stripe far2x" id="metadata-td">
	<tbody>
	<tr>
		<th scope="row" colspan="2">Summary</th>
		<td>Length: <%=d.get("length") %>bp, Chromosomes: <%=d.get("chromosome") %>, Plasmids: <%=d.get("plasmid") %>, Contigs: <%=d.get("contig") %> </td>
	</tr>
	</tbody>
	<tr>
		<td class="no-underline-links" colspan="3" id="click_for_more"></td>
	</tr>
	</table>
<%
} else if (cType != null && cType.equals("taxon")) {


	DBSummary conn_summary = new DBSummary();
	Map<String,String> key = new HashMap<String,String>();
	key.put("ncbi_taxon_id",cId);
	key.put("data_source", "PATRIC");
	ResultType patric_counts = conn_summary.getGenomeCount(key);

	key.put("data_source", "Legacy BRC");
	ResultType brc_counts = conn_summary.getGenomeCount(key);

	key.put("data_source", "RefSeq");
	ResultType refseq_counts = conn_summary.getGenomeCount(key);
	%>
	<table class="basic stripe far2x">
	<thead>
	<tr>
		<th width="40%"></th>
		<th scope="col" width="20%">PATRIC</th>
		<th scope="col" width="20%">Legacy BRC</th>
		<th scope="col" width="20%">RefSeq</th>
	</tr>
	</thead>
	<tbody>
	<tr class="alt">
		<th scope="row">Number of genomes</th>
		<td class="right-align-text">
			<% if (!patric_counts.get("cnt_all").equals("0")) { %>
			<a href="GenomeList?cType=taxon&cId=<%=cId%>&displayMode=&dataSource=RAST&pk="><%=patric_counts.get("cnt_all") %></a>
			<% } else { %> 0 <% } %>
		</td>
		<td class="right-align-text">
			<% if (!brc_counts.get("cnt_all").equals("0")) { %>
				<a href="GenomeList?cType=taxon&cId=<%=cId%>&displayMode=&dataSource=BRC&pk="><%=brc_counts.get("cnt_all") %></a>
			<% } else { %> 0 <% } %>
		</td>
		<td class="right-align-text last">
			<% if ( !refseq_counts.get("cnt_all").equals("0")) { %>
				<a href="GenomeList?cType=taxon&cId=<%=cId%>&displayMode=&dataSource=RefSeq&pk="><%=refseq_counts.get("cnt_all") %></a>
			<% } else { %> 0 <% } %>
		</td>
	</tr>
	<tr>
		<th scope="row">Number of Complete genomes</th>
		<td class="right-align-text">
			<% if ( !patric_counts.get("cnt_complete").equals("0")) { %>
				<a href="GenomeList?cType=taxon&cId=<%=cId%>&displayMode=Complete&dataSource=RAST&pk="><%=patric_counts.get("cnt_complete") %></a>
			<% } else { %> 0 <% } %>
		</td>
		<td class="right-align-text">
			<% if ( !brc_counts.get("cnt_complete").equals("0")) { %>
			<a href="GenomeList?cType=taxon&cId=<%=cId%>&displayMode=Complete&dataSource=BRC&pk="><%=brc_counts.get("cnt_complete") %></a>
			<% } else { %> 0 <% } %>
		</td>
		<td class="right-align-text last">
			<% if ( !refseq_counts.get("cnt_complete").equals("0")) { %>
				<a href="GenomeList?cType=taxon&cId=<%=cId%>&displayMode=Complete&dataSource=RefSeq&pk="><%=refseq_counts.get("cnt_complete") %></a>
			<% } else { %> 0 <% } %>
		</td>
	</tr>
	<tr class="alt">
		<th scope="row">Number of WGS genomes</th>
		<td class="right-align-text">
			<% if ( !patric_counts.get("cnt_wgs").equals("0")) { %>
				<a href="GenomeList?cType=taxon&cId=<%=cId%>&displayMode=WGS&dataSource=RAST&pk="><%=patric_counts.get("cnt_wgs") %></a>
			<% } else { %> 0 <% } %>
		</td>
		<td class="right-align-text">
			<% if ( !brc_counts.get("cnt_wgs").equals("0")) { %>
			<a href="GenomeList?cType=taxon&cId=<%=cId%>&displayMode=WGS&dataSource=BRC&pk="><%=brc_counts.get("cnt_wgs") %></a>
			<% } else { %> 0 <% } %>
		</td>
		<td class="right-align-text last">
			<% if ( !refseq_counts.get("cnt_wgs").equals("0")) { %>
			<a href="GenomeList?cType=taxon&cId=<%=cId%>&displayMode=WGS&dataSource=RefSeq&pk="><%=refseq_counts.get("cnt_wgs") %></a>
			<% } else { %> 0 <% } %>
		</td>
	</tr>
	<tr>
		<th scope="row">Number of Plasmid only genomes</th>
		<td class="right-align-text">
			<% if ( !patric_counts.get("cnt_plasmid").equals("0")) { %>
				<a href="GenomeList?cType=taxon&cId=<%=cId%>&displayMode=Plasmid&dataSource=RAST&pk="><%=patric_counts.get("cnt_plasmid") %></a>
			<% } else { %> 0 <% } %>
		</td>
		<td class="right-align-text">
			<% if ( !brc_counts.get("cnt_plasmid").equals("0")) { %>
				<a href="GenomeList?cType=taxon&cId=<%=cId%>&displayMode=Plasmid&dataSource=BRC&pk="><%=brc_counts.get("cnt_plasmid") %></a>
			<% } else { %> 0 <% } %>
		</td>
		<td class="right-align-text last">
			<% if ( !refseq_counts.get("cnt_plasmid").equals("0")) { %>
				<a href="GenomeList?cType=taxon&cId=<%=cId%>&displayMode=Plasmid&dataSource=RefSeq&pk="><%=refseq_counts.get("cnt_plasmid") %></a>
			<% } else { %> 0 <% } %>
		</td>
	</tr>
	</tbody>
	</table>
<% } %>