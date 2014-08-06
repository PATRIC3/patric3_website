<%@ page import="java.util.*" 
%><%@ page import="java.net.URL"
%><%@ page import="java.net.URLConnection"
%><%@ page import="java.io.BufferedReader"
%><%@ page import="java.io.InputStreamReader"
%><%
String taxonID = request.getParameter("taxonID");
String baseURL = "http://"+request.getServerName()+"/patric/static/phylogeny/";

URL url = new URL(baseURL+taxonID+".tree");
URLConnection conn = url.openConnection();

try {
	BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	String line;
	StringBuffer sb = new StringBuffer();
	
	while ((line = rd.readLine())!=null) {
		sb.append(line);
	}
	rd.close();

	response.setContentType("application/octetstream");
	response.setHeader("Content-Disposition", "attachment; filename=\""+taxonID+".nwk\"");
	
	%><%=sb.toString()%><%

} catch (Exception ex) {
	%>No data is available.(<%=url.toString()%>)<%
	//ex.printStackTrace();
}
%>