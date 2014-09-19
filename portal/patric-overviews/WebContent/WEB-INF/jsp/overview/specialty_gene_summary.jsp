<%@ page import="edu.vt.vbi.patric.common.SolrInterface" 
%><%@ page import="edu.vt.vbi.patric.common.SolrCore"
%><%@ page import="edu.vt.vbi.patric.common.SiteHelper" 
%><%@ page import="org.apache.solr.client.solrj.SolrQuery"
%><%@ page import="org.apache.solr.client.solrj.response.QueryResponse"
%><%@ page import="org.apache.solr.client.solrj.response.FacetField"
%><%@ page import="org.apache.solr.common.params.FacetParams"
%><%
	String cType = request.getParameter("context_type");
	String cId = request.getParameter("context_id");

	SolrInterface solr = new SolrInterface();
	solr.setCurrentInstance(SolrCore.SPECIALTY_GENE_MAPPING);
	
	SolrQuery query = new SolrQuery();
	if (cType.equals("taxon")) {
		query.setFilterQueries( SolrCore.GENOME.getSolrCoreJoin("genome_id", "genome_id", "taxon_lineage_ids:" + cId));
	}
	else { // genome
		query.setFilterQueries("genome_info_id:" + cId);
	}
	query.setFacet(true).setFacetMinCount(1).addFacetField("property_source").setFacetSort(FacetParams.FACET_SORT_INDEX);
	
	// facet query
	query.setQuery("*:*");

	QueryResponse qr = solr.getServer().query(query);
	FacetField ff = qr.getFacetField("property_source");
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
	int i = 0;
	for (FacetField.Count fc: ff.getValues()) {
		String property_source = fc.getName();
		long count = fc.getCount();
		String property = property_source.split(":")[0].trim();
		String source = property_source.split(":")[1].trim();
	%>
	<tr<%=(i%2 == 0)?" class=\"alt\"":"" %>>
		<th scope="row"><%=property %></th>
		<th scope="row" class="no-underline-links">
		<% if (SiteHelper.getExternalLinks(source+"_HOME").equals("") == false) { %>
			<a class="arrow-slate-e" href="<%=SiteHelper.getExternalLinks(source+"_HOME") %>" target="_blank"><%=source %></a>
		<% } else { %>
			<%=source %>
		<% } %>
		</th>
		<td class="right-align-text">
		<% if (count != 0) { %>
			<a href="SpecialtyGeneList?cType=<%=cType%>&amp;cId=<%=cId%>&kw=source:%22<%=source%>%22"><%=count%></a>
		<% } else { %>
			0
		<% } %>
		</td>
	</tr>
	<%
	i++;
	} 
	%>
</tbody>
</table>
