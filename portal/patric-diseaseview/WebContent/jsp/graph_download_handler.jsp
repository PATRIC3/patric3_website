<%@ page import="java.awt.*"%>
<%@ page import="java.awt.image.BufferedImage"%>
<%@ page import="javax.imageio.ImageIO"%>
<%@ page import="java.io.OutputStream"%>
<%@ page import="java.io.ByteArrayOutputStream"%>
<%@ page import="java.io.InputStream"%>
<%@ page import="java.io.BufferedReader"%>
<%@ page import="java.io.StringWriter"%>
<%@ page import="java.io.InputStreamReader"%>
<%@ page import="java.io.Reader"%>
<%@ page import="java.io.Writer"%>
<%
	response.setContentType("application/octetstream");
	response.setHeader("Content-Disposition", "attachment; filename=\"graph."+request.getParameter("type")+"\"");
	response.setHeader("Cache-Control", "cache");

	byte[] bytes = null;
	
	InputStream is = request.getInputStream();
	
	if(request.getParameter("type").equals("png")){
	
		BufferedImage image = ImageIO.read(is);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, "png", baos);
		
		bytes = baos.toByteArray();
		baos.close();
		
	}else if(request.getParameter("type").equals("pdf") || request.getParameter("type").equals("xgmml")){
		
		Reader reader = new BufferedReader(new InputStreamReader(is));
		Writer writer = new StringWriter();
		char[] buffer = new char[1024];
		int n;
		while ((n = reader.read(buffer)) != -1) {
			writer.write(buffer, 0, n);
		}
		bytes = writer.toString().getBytes();
	}
	
	response.setContentLength(bytes.length);
	
	OutputStream outs = response.getOutputStream();
	outs.write(bytes);
	outs.flush();
	outs.close();
%>