<%

	// getting common params
	String _filetext = request.getParameter("copy_text_to_file");

	response.setContentType("application/octetstream");
	response.setHeader("Content-Disposition", "attachment; filename=\"PATRICCopy.txt\"");

	out.println(_filetext);
%>