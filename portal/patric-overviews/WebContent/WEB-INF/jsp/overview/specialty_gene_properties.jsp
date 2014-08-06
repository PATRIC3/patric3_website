<%@ page import="java.util.ArrayList" 
%><%@ page import="java.util.HashMap" 
%><%@ page import="edu.vt.vbi.patric.beans.DNAFeature" 
%><%@ page import="edu.vt.vbi.patric.dao.DBSummary" 
%><%@ page import="edu.vt.vbi.patric.dao.ResultType" 
%><%@ page import="edu.vt.vbi.patric.common.SolrInterface" 
%><%@ page import="edu.vt.vbi.patric.common.SolrCore" 
%><%@ page import="edu.vt.vbi.patric.common.SiteHelper" 
%><%@ page import="org.json.simple.JSONObject"
%><%@ page import="org.json.simple.JSONArray"
%><%
String fId = request.getParameter("context_id");
DNAFeature feature = null; // get corresponding patric feature
SolrInterface solr = new SolrInterface();

if (fId != null) {
	feature = solr.getPATRICFeature(fId);
}

if (feature != null) {

	solr.setCurrentInstance(SolrCore.SPECIALTY_GENE_MAPPING);
	ResultType key = new ResultType();
	key.put("keyword", "na_feature_id:"+ feature.getId());
	
	HashMap<String, String >sort = new HashMap<String, String>();
	sort.put("field", "evidence, property, source");
	sort.put("direction", "desc, asc, asc");

	JSONObject res = solr.getData(key, sort, null, 0, -1, false, false, false);
	JSONArray specialtyGeneProperties = (JSONArray)((JSONObject)res.get("response")).get("docs");
%>	
	<% if (specialtyGeneProperties!=null && specialtyGeneProperties.size() > 0) { %>
	<h3 class="section-title normal-case close2x"><span class="wrap">Special Properties</span></h3>
	<table class="basic stripe far2x">
	<thead>
		<tr>
			<th scope="col">Evidence</th>
			<th scope="col">Property</th>
			<th scope="col">Source</th>
			<th scope="col">Source ID</th>
			<th scope="col">Organism</th>
			<th scope="col">PubMed</th>
			<th scope="col">Subject Coverage</th>
			<th scope="col">Query Coverage</th>
			<th scope="col">Identity</th>
			<th scope="col">E-value</th>
		</tr>
	</thead>
	<tbody>
	<% for (int i = 0; i < specialtyGeneProperties.size(); i++) { 
		JSONObject prop = (JSONObject) specialtyGeneProperties.get(i); 
	%>
		<tr <%=(i%2==0)?" class=\"alt\"":"" %>>
			<td><%=prop.get("evidence") %></td>
			<td><%=prop.get("property") %></td>
			<td class="no-underline-links"><% 
				if (SiteHelper.getExternalLinks(prop.get("source").toString()+"_HOME").equals("") == false) {
					%><a class="arrow-slate-e" href="<%=SiteHelper.getExternalLinks(prop.get("source").toString()+"_HOME")%>" target=_blank><%=prop.get("source") %></a><%
				} else {
					%><%=prop.get("source") %><%
				} %>
			</td>
			<td class="no-underline-links"><% 
				if (SiteHelper.getExternalLinks(prop.get("source").toString()).equals("") == false) {
					%><a class="arrow-slate-e" href="<%=SiteHelper.getExternalLinks(prop.get("source").toString())%><%=prop.get("source_id")%>" target=_blank><%=prop.get("source_id") %></a><%
				} else {
					%><%=prop.get("source_id") %><%
				}
			%>
			</td>
			<td><%=(prop.get("organism")!=null)?prop.get("organism"):"&nbsp;" %></td>
			<td><%=(prop.get("pmid")!=null)?"<a class=\"arrow-slate-e\" href=\"//www.ncbi.nlm.nih.gov/pubmed/" + prop.get("pmid") + "\" target=_blank>"+prop.get("pmid")+"</a>":"&nbsp;" %></td>
			<td><%=(prop.get("subject_coverage")!=null)?prop.get("subject_coverage"):"&nbsp;" %></td>
			<td><%=(prop.get("query_coverage")!=null)?prop.get("query_coverage"):"&nbsp;" %></td>
			<td><%=(prop.get("identity")!=null)?prop.get("identity"):"&nbsp;" %></td>
			<td><%=(prop.get("e_value")!=null)?prop.get("e_value"):"&nbsp;" %></td>
		</tr>
	<% } %>
	</tbody>
	</table>
	<% } %>
	
<% } %>