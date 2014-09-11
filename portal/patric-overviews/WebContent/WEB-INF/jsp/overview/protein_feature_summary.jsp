<%@ page import="java.util.HashMap" 
%><%@ page import="edu.vt.vbi.patric.common.SolrInterface"
%><%@ page import="edu.vt.vbi.patric.common.SolrCore"
%><%@ page import="org.apache.solr.client.solrj.SolrQuery"
%><%@ page import="org.apache.solr.client.solrj.response.QueryResponse"
%><%@ page import="org.apache.solr.client.solrj.response.FacetField"
%><%
	String cType = request.getParameter("context_type");
	String cId = request.getParameter("context_id");
	HashMap<String, Long> hypotheticalProteins = new HashMap<String, Long>();
	HashMap<String, Long> functionalProteins = new HashMap<String, Long>();
	HashMap<String, Long> ecAssignedProteins = new HashMap<String, Long>();
	HashMap<String, Long> goAssignedProteins = new HashMap<String, Long>();
	HashMap<String, Long> pathwayAssignedProteins = new HashMap<String, Long>();
	HashMap<String, Long> figfamAssignedProteins = new HashMap<String, Long>();

	SolrInterface solr = new SolrInterface();
	solr.setCurrentInstance(SolrCore.FEATURE);
	
	// set default params
	SolrQuery query = new SolrQuery();
	if (cType.equals("taxon")) {
		query.setFilterQueries( SolrCore.GENOME.getSolrCoreJoin("gid", "gid", "taxon_lineage_ids:" + cId));
	}
	else { // genome
		query.setFilterQueries("gid:" + cId);
	}
	query.setFacet(true).setFacetMinCount(1).addFacetField("annotation_f");


	// hypothetical
	query.setQuery("product:(hypothetical AND protein) AND feature_type_f:CDS");

	QueryResponse qr = solr.getServer().query(query);
	FacetField ff = qr.getFacetField("annotation_f");
	for (FacetField.Count fc: ff.getValues()) {
		hypotheticalProteins.put(fc.getName(), fc.getCount());
	}

	// funtional assigned
	query.setQuery("!product:(hypothetical AND protein) AND feature_type_f:CDS");
	qr = solr.getServer().query(query);
	ff = qr.getFacetField("annotation_f");
	for (FacetField.Count fc: ff.getValues()) {
		functionalProteins.put(fc.getName(), fc.getCount());
	}

	// ec assigned
	query.setQuery("ec:[* TO *]");
	qr = solr.getServer().query(query);
	ff = qr.getFacetField("annotation_f");
	for (FacetField.Count fc: ff.getValues()) {
		ecAssignedProteins.put(fc.getName(), fc.getCount());
	}

	// go assigned
	query.setQuery("go:[* TO *]");
	qr = solr.getServer().query(query);
	ff = qr.getFacetField("annotation_f");
	for (FacetField.Count fc: ff.getValues()) {
		goAssignedProteins.put(fc.getName(), fc.getCount());
	}

	// pathway assigned
	query.setQuery("pathway:[* TO *]");
	qr = solr.getServer().query(query);
	ff = qr.getFacetField("annotation_f");
	for (FacetField.Count fc: ff.getValues()) {
		pathwayAssignedProteins.put(fc.getName(), fc.getCount());
	}

	// figfam assigned
	query.setQuery("figfam_id: [* TO *]");
	
	qr = solr.getServer().query(query);
	ff = qr.getFacetField("annotation_f");
	for (FacetField.Count fc: ff.getValues()) {
		figfamAssignedProteins.put(fc.getName(), fc.getCount());
	}

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
		<th scope="row">Hypothetical proteins</th>
		<td class="right-align-text">
		<% if (hypotheticalProteins.isEmpty() == false && hypotheticalProteins.get("PATRIC") != null) 
		{
		%>
			<a href="FeatureTable?cType=<%=cType%>&amp;cId=<%=cId%>&amp;featuretype=CDS&amp;annotation=PATRIC&amp;filtertype=hypothetical_proteins"><%=hypotheticalProteins.get("PATRIC") %></a>
		<% } else { %>
			0
		<% } %>
		</td>
		<td class="right-align-text">
		<% if (hypotheticalProteins.isEmpty() == false && hypotheticalProteins.get("BRC") != null)
		{ 
		%>
			<a href="FeatureTable?cType=<%=cType%>&amp;cId=<%=cId%>&amp;featuretype=CDS&amp;annotation=BRC&amp;filtertype=hypothetical_proteins"><%=hypotheticalProteins.get("BRC")%></a>
		<% } else { %>
			0
		<% } %>
		</td>
		<td class="right-align-text last">
		<% if (hypotheticalProteins.isEmpty() == false && hypotheticalProteins.get("RefSeq") != null) 
		{ 
		%>
			<a href="FeatureTable?cType=<%=cType%>&amp;cId=<%=cId%>&amp;featuretype=CDS&amp;annotation=RefSeq&amp;filtertype=hypothetical_proteins"><%=hypotheticalProteins.get("RefSeq")%></a>
		<% } else { %>
			0
		<% } %>
		</td>
	</tr>
	<tr>
		<th scope="row">Proteins with functional assignments</th>
		<td class="right-align-text">
		<% if (functionalProteins.isEmpty() == false && functionalProteins.get("PATRIC") != null) 
		{
		%>
			<a href="FeatureTable?cType=<%=cType%>&amp;cId=<%=cId%>&amp;featuretype=CDS&amp;annotation=PATRIC&amp;filtertype=functional_proteins"><%=functionalProteins.get("PATRIC") %></a>
		<% } else { %>
			0
		<% } %>
		</td>
		<td class="right-align-text">
		<% if (functionalProteins.isEmpty() == false && functionalProteins.get("BRC") != null)
		{ 
		%>
			<a href="FeatureTable?cType=<%=cType%>&amp;cId=<%=cId%>&amp;featuretype=CDS&amp;annotation=BRC&amp;filtertype=functional_proteins"><%=functionalProteins.get("BRC")%></a>
		<% } else { %>
			0
		<% } %>
		</td>
		<td class="right-align-text last">
		<% if (functionalProteins.isEmpty() == false && functionalProteins.get("RefSeq") != null) 
		{ 
		%>
			<a href="FeatureTable?cType=<%=cType%>&amp;cId=<%=cId%>&amp;featuretype=CDS&amp;annotation=RefSeq&amp;filtertype=functional_proteins"><%=functionalProteins.get("RefSeq")%></a>
		<% } else { %>
			0
		<% } %>
		</td>
	</tr>
	<tr class="alt">
		<th scope="row">Proteins with EC number assignments</th>
		<td class="right-align-text">
		<% if (ecAssignedProteins.isEmpty() == false && ecAssignedProteins.get("PATRIC") != null) 
		{
		%>
			<a href="FeatureTable?cType=<%=cType%>&amp;cId=<%=cId%>&amp;featuretype=CDS&amp;annotation=PATRIC&amp;filtertype=ec"><%=ecAssignedProteins.get("PATRIC") %></a>
		<% } else { %>
			0
		<% } %>
		</td>
		<td class="right-align-text">
		<% if (ecAssignedProteins.isEmpty() == false && ecAssignedProteins.get("BRC") != null)
		{ 
		%>
			<a href="FeatureTable?cType=<%=cType%>&amp;cId=<%=cId%>&amp;featuretype=CDS&amp;annotation=BRC&amp;filtertype=ec"><%=ecAssignedProteins.get("BRC")%></a>
		<% } else { %>
			0
		<% } %>
		</td>
		<td class="right-align-text last">
		<% if (ecAssignedProteins.isEmpty() == false && ecAssignedProteins.get("RefSeq") != null) 
		{ 
		%>
			<a href="FeatureTable?cType=<%=cType%>&amp;cId=<%=cId%>&amp;featuretype=CDS&amp;annotation=RefSeq&amp;filtertype=ec"><%=ecAssignedProteins.get("RefSeq")%></a>
		<% } else { %>
			0
		<% } %>
		</td>
	</tr>
	<tr>
		<th scope="row">Proteins with GO assignments</th>
		<td class="right-align-text">
		<% if (goAssignedProteins.isEmpty() == false && goAssignedProteins.get("PATRIC") != null) 
		{
		%>
			<a href="FeatureTable?cType=<%=cType%>&amp;cId=<%=cId%>&amp;featuretype=CDS&amp;annotation=PATRIC&amp;filtertype=go"><%=goAssignedProteins.get("PATRIC") %></a>
		<% } else { %>
			0
		<% } %>
		</td>
		<td class="right-align-text">
		<% if (goAssignedProteins.isEmpty() == false && goAssignedProteins.get("BRC") != null)
		{ 
		%>
			<a href="FeatureTable?cType=<%=cType%>&amp;cId=<%=cId%>&amp;featuretype=CDS&amp;annotation=BRC&amp;filtertype=go"><%=goAssignedProteins.get("BRC")%></a>
		<% } else { %>
			0
		<% } %>
		</td>
		<td class="right-align-text last">
		<% if (goAssignedProteins.isEmpty() == false && goAssignedProteins.get("RefSeq") != null) 
		{ 
		%>
			<a href="FeatureTable?cType=<%=cType%>&amp;cId=<%=cId%>&amp;featuretype=CDS&amp;annotation=RefSeq&amp;filtertype=go"><%=goAssignedProteins.get("RefSeq")%></a>
		<% } else { %>
			0
		<% } %>
		</td>
	</tr>
	<tr class="alt">
		<th scope="row">Proteins with Pathway assignments</th>
		<td class="right-align-text">
		<% if (pathwayAssignedProteins.isEmpty() == false && pathwayAssignedProteins.get("PATRIC") != null) 
		{
		%>
			<a href="FeatureTable?cType=<%=cType%>&amp;cId=<%=cId%>&amp;featuretype=CDS&amp;annotation=PATRIC&amp;filtertype=pathway"><%=pathwayAssignedProteins.get("PATRIC") %></a>
		<% } else { %>
			0
		<% } %>
		</td>
		<td class="right-align-text">
		<% if (pathwayAssignedProteins.isEmpty() == false && pathwayAssignedProteins.get("BRC") != null)
		{ 
		%>
			<a href="FeatureTable?cType=<%=cType%>&amp;cId=<%=cId%>&amp;featuretype=CDS&amp;annotation=BRC&amp;filtertype=pathway"><%=pathwayAssignedProteins.get("BRC")%></a>
		<% } else { %>
			0
		<% } %>
		</td>
		<td class="right-align-text last">
		<% if (pathwayAssignedProteins.isEmpty() == false && pathwayAssignedProteins.get("RefSeq") != null) 
		{ 
		%>
			<a href="FeatureTable?cType=<%=cType%>&amp;cId=<%=cId%>&amp;featuretype=CDS&amp;annotation=RefSeq&amp;filtertype=pathway"><%=pathwayAssignedProteins.get("RefSeq")%></a>
		<% } else { %>
			0
		<% } %>
		</td>
	</tr>
	<tr>
		<th scope="row">Proteins with FIGfam assignments</th>
		<td class="right-align-text">
		<% if (figfamAssignedProteins.isEmpty() == false && figfamAssignedProteins.get("PATRIC") != null) 
		{
		%>
			<a href="FeatureTable?cType=<%=cType%>&amp;cId=<%=cId%>&amp;featuretype=CDS&amp;annotation=PATRIC&amp;filtertype=figfam_id"><%=figfamAssignedProteins.get("PATRIC") %></a>
		<% } else { %>
			0
		<% } %>
		</td>
		<td class="right-align-text">
		<% if (figfamAssignedProteins.isEmpty() == false && figfamAssignedProteins.get("BRC") != null)
		{ 
		%>
			<a href="FeatureTable?cType=<%=cType%>&amp;cId=<%=cId%>&amp;featuretype=CDS&amp;annotation=BRC&amp;filtertype=figfam_id"><%=figfamAssignedProteins.get("BRC")%></a>
		<% } else { %>
			0
		<% } %>
		</td>
		<td class="right-align-text last">
		<% if (figfamAssignedProteins.isEmpty() == false && figfamAssignedProteins.get("RefSeq") != null) 
		{ 
		%>
			<a href="FeatureTable?cType=<%=cType%>&amp;cId=<%=cId%>&amp;featuretype=CDS&amp;annotation=RefSeq&amp;filtertype=figfam_id"><%=figfamAssignedProteins.get("RefSeq")%></a>
		<% } else { %>
			0
		<% } %>
		</td>
	</tr>
</tbody>
</table>
