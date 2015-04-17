<%@ page import="java.util.*" 
%><%
	HashMap<String, String> descriptions = new HashMap<String, String>();

	descriptions.put("Keyword", "<p class='largest far'>Find Genomes by Specifying Descriptive Search Summary Terms</p>"
			+ "<p class='close2x'><span class='bold'>Examples:</span>&nbsp;<span>Escherichia coli</span><br/><span>Escherichia coli USA 2006</span><br/><span>Escherichia \"Homo sapiens\"</span></p>");

	descriptions.put("genome_id", "<p class='largest far'>Find genomes by specifying genome IDs.</p>"
			+ "<p class='close2x'><span class='bold'>Examples:</span>&nbsp;<span>83332.12</span><br/><span>511145.12</span></p>");

	descriptions.put("genome_name", "<p class='largest far'>Find genomes by specifying genome name.</p>"
			+ "<p class='close2x'><span class='bold'>Examples:</span>&nbsp;<span>Mycobacterium tuberculosis H37Rv</span><br/><span>Escherichia coli str. K-12 substr. MG1655</span></p>");

	descriptions.put("isolation_country", "<p class='largest far'>Find genomes by specifying country from which the bacterial DNA sample was isolated.</p>"
			+ "<p class='close2x'><span class='bold'>Examples:</span>&nbsp;<span>USA</span><br/><span>USA or Brazil or China</span></p>");

	descriptions.put("genome_status", "<p class='largest far'>Find genomes by specifying genome completion status.</p>"
			+ "<p class='close2x'><span class='bold'>Examples:</span>&nbsp;<span>Complete</span>, <span>WGS</span>, <span>Plasmid</span></p>");

	descriptions.put("host_name", "<p class='largest far'>Find genomes that have been isolated from a specific host.</p>"
			+ "<p class='close2x'><span class='bold'>Examples:</span>&nbsp;<span>\"Homo sapiens\"</span><br/><span>pig or swine or sus</span></p>");

	descriptions.put("disease", "<p class='largest far'>Find genomes associated with a specific disease.</p>"
			+ "<p class='close2x'><span class='bold'>Examples:</span>&nbsp;<span>Tuberculosis</span><br/><span>Gastroenteritis or \"gas gangrene\"</span></p>");

	descriptions.put("collection_date", "<p class='largest far'>Find genomes by the year in which the bacterial sample was collected.</p>"
			+ "<p class='close2x'><span class='bold'>Examples:</span>&nbsp;<span>2006</span><br/><span>2010 or 2009</span></p>");

	descriptions.put("completion_date", "<p class='largest far'>Find genomes by the year in which sequencing was completed and/or the genome sequence was released.</p>"
			+ "<p class='close2x'><span class='bold'>Examples:</span>&nbsp;<span>2011</span><br/><span>2010 or 2009</span></p>");

	String search_on = request.getParameter("search_on");

	if (descriptions.containsKey(search_on)) {
		out.println(descriptions.get(search_on).toString());
	}
	else {
		out.println("");
	}
%>