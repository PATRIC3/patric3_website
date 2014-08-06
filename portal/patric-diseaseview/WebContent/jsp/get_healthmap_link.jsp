<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%
	Map<String, String> key = new HashMap<String, String>();
	String taxonId = request.getParameter("tId");

	key.put("1386", "160");
	key.put("773", "161");
	key.put("138", "162");
	key.put("234", "163");
	key.put("32008", "164");
	key.put("194", "165");
	key.put("83553", "166");
	key.put("1485", "167");
	key.put("776", "168");
	key.put("943", "");
	key.put("561", "169");
	key.put("262", "170");
	key.put("209", "171");
	key.put("1637", "172");
	key.put("1763", "173");
	key.put("780", "174");
	key.put("590", "175");
	key.put("620", "176");
	key.put("1279", "177");
	key.put("662", "178");
	key.put("629", "179");
	key.put("1301", "190");

	if(key.containsKey(taxonId)) {
		out.println(key.get(taxonId));
	} else {
		out.println("empty");
	}
%>