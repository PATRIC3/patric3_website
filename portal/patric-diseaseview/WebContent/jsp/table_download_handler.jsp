<%@ page import="edu.vt.vbi.patric.common.ExcelHelper"%><%@ page
	import="edu.vt.vbi.patric.dao.ResultType"%><%@ page
	import="edu.vt.vbi.patric.dao.*"%><%@ page
	import="java.io.OutputStream"%><%@ page import="java.util.List"%><%@ page
	import="java.util.ArrayList"%><%@ page import="java.util.Arrays"%><%@ page
	import="java.util.HashMap"%><%@ page import="java.util.Map" %>
<%
	DBDisease disease_db = new DBDisease();

	String _filename = "";
	List<String> _tbl_header = new ArrayList<String>();
	List<String> _tbl_field = new ArrayList<String>();
	List<ResultType> _tbl_source = null;

	// getting common params
	String _fileformat = request.getParameter("fileformat");
	String _tablesource = request.getParameter("tablesource");

	Map<String, String> key = new HashMap<String, String>();
	Map<String, String> sort = new HashMap<String, String>();

	_filename = "DiseaseView_Result";

	ExcelHelper excel = null;

	if (_tablesource.equalsIgnoreCase("DiseaseTable")) {

		String type = request.getParameter("type");

		if (request.getParameter("name") != null) {
			key.put("name", request.getParameter("name"));
		}

		if (type.equals("ctd") || type.equals("ctdgraph")) {
			_tbl_source = disease_db.getCTDList(key, sort, 0, -1);
			_tbl_header.addAll(Arrays.asList("CTD Human Disease Gene", "Gene Product", "Association", "Pubmed ID", "MeSH Terms"));
			_tbl_field.addAll(Arrays.asList("gene_sym", "gd_app_name", "Association", "pubmed_id", "disease_name"));
		}
		else if (type.equals("gad") || type.equals("gadgraph")) {

			_tbl_source = disease_db.getGADList(key, sort, 0, -1);
			_tbl_header.addAll(Arrays.asList("GAD Human Disease Gene", "Gene Product", "Association", "Pubmed ID", "Conclusion",
					"MeSH Disease Terms", "GAD Broad Phenotype"));
			_tbl_field.addAll(Arrays.asList("gene_sym", "gd_app_name", "association", "pubmed_id", "conclusion", "mesh_disease_terms",
					"broad_phenotype"));
		}
	}
	else if (_tablesource.equalsIgnoreCase("VFDBTable")) {

		String currenttab = request.getParameter("currenttab");

		if (request.getParameter("aT") != null && request.getParameter("aT").equals("0")) {

			if (request.getParameter("cId") != null) {
				key.put("cId", request.getParameter("cId"));
			}

			_tbl_source = disease_db.getVFDBList(key, sort, 0, -1);
			_tbl_header.addAll(Arrays.asList("VFG ID", "Gene Name", "Gene Product", "VF ID", "VF Name", "VF Full Name", "VF Function",
					"Virulence Gene Homologs"));
			_tbl_field.addAll(Arrays.asList("vfg_id", "gene_name", "vf_id", "gene_product", "vf_name", "vf_fullname", "function",
					"feature_count"));
		}
		else if (request.getParameter("aT").equals("1")) {

			if (request.getParameter("cId") != null) {
				key.put("cId", request.getParameter("cId"));
			}

			if (request.getParameter("VFGId") != null) {
				key.put("vfgId", request.getParameter("VFGId"));
			}
			_tbl_source = disease_db.getVFDBFeatureList(key, sort, 0, -1);
			_tbl_header.addAll(Arrays.asList("Genome Name", "Accession", "Locus Tag", "Product Description", "VFG ID", "Gene Name", "VF ID",
					"VF Name"));
			_tbl_field.addAll(Arrays.asList("genome_name", "accession", "locus_tag", "product", "vfg_id", "vf_id", "gene_name", "vf_name"));
		}
	}

	excel = new ExcelHelper("xssf", _tbl_header, _tbl_field, _tbl_source);
	excel.buildSpreadsheet();

	if (_fileformat.equalsIgnoreCase("xlsx")) {
		response.setContentType("application/octetstream");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + _filename + "." + _fileformat + "\"");

		OutputStream outs = response.getOutputStream();
		excel.writeSpreadsheettoBrowser(outs);
	}
	else {
		response.setContentType("application/octetstream");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + _filename + "." + _fileformat + "\"");

		response.getWriter().write(excel.writeToTextFile());
	}
%>