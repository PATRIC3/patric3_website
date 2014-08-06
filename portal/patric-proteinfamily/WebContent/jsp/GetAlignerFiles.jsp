<%
	String text = request.getParameter("data");
	String fileformat = request.getParameter("fileformat");

	if (fileformat.equals("newick")) {
		response.setContentType("application/octetstream");
		response.setHeader("Content-Disposition", "attachment; filename=\"tree.nwk\"");

		response.getWriter().write(text);
	}
	else {
		response.setContentType("application/octetstream");
		response.setHeader("Content-Disposition", "attachment; filename=\"aligner.aga\"");

		response.getWriter().write(text);
	}
%>
