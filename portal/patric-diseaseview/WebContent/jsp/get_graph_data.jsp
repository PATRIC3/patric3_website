<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Random" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.io.File" %>
<%@ page import="java.io.FileWriter" %>
<%@ page import="java.io.BufferedWriter" %>
<%@ page import="edu.vt.vbi.ci.util.CommandResults" %>
<%@ page import="edu.vt.vbi.ci.util.ExecUtilities" %>
<%@ page import="edu.vt.vbi.patric.dao.DBDisease" %>
<%@ page import="edu.vt.vbi.patric.dao.ResultType" %>
<%@ page import="edu.vt.vbi.patric.common.StringHelper"%>
<%@ page import="org.json.simple.*" %>
<%
	String tmpDir = System.getProperty("java.io.tmpdir", "/tmp");
	boolean remove = true;
	String fileDiseases = null, fileGenes = null, fileVFS = null;
	
	String path = "";
	String machine = "";
	
	Random generator = new Random();
	int key = generator.nextInt(10000) + 1;
	
	if ((new File("/opt/jboss/jboss-epp-4.3/jboss-as/server/jboss-patric/deploy/jboss-web.deployer/ROOT.war/patric/idv-perl/server/idv-gidi.pl")).exists())
	{
		path = "/opt/jboss/jboss-epp-4.3/jboss-as/server/jboss-patric/deploy/jboss-web.deployer/ROOT.war/patric/idv-perl";
		machine = "dev";
	}
	else if((new File("/opt/jboss-patric/jboss_patric/deploy/jboss-web.deployer/ROOT.war/patric/idv-perl/server/idv-gidi.pl")).exists())
	{
		path = "/opt/jboss-patric/jboss_patric/deploy/jboss-web.deployer/ROOT.war/patric/idv-perl";
		machine = "production";
	}

	//System.out.print("diseaseview on " + machine);
	//System.out.print("diseaseview on " + path);
	
	String cId = request.getParameter("cId");

	DBDisease conn_disease = new DBDisease();

	try {
		List<ResultType> mesh_terms = conn_disease.getMeshTermGraphData(cId);
		List<String> _tbl_header = new ArrayList<String>();
		List<String> _tbl_field = new ArrayList<String>();
		
		_tbl_header.addAll(Arrays.asList("taxon_id", "organism_name", "organism_rank", "parent_id", "mesh_disease_id", "mesh_disease_name", "mesh_tree_node", "parent_tree_node", "description"));
		_tbl_field.addAll(Arrays.asList("taxon_id","organism_name","organism_rank", "parent_id", "mesh_disease_id", "mesh_disease_name", "mesh_tree_node", "parent_tree_node", "description"));
		
		String output = "";
		int i;
		
		for (i=0; i<_tbl_header.size()-1;i++) {
			output += _tbl_header.get(i)+"\t";
		}
		output += _tbl_header.get(i)+"\r\n";
		
		Iterator<ResultType> itr = mesh_terms.iterator();
		
		while (itr.hasNext()) {
			ResultType datarow = itr.next();
			String _f = "";
			for (i=0;i<_tbl_field.size()-1;i++) {
				_f = _tbl_field.get(i);
				if (datarow.get(_f)!=null) {
					output += StringHelper.strip_html_tag(datarow.get(_f))+"\t";
				} else {
					output += "\t";
				}
			}
			_f = _tbl_field.get(i);
			output += datarow.get(_f)+"\r\n";
		}
		
		try {
			fileDiseases = tmpDir + "/_"+key+"_diseases.txt";
			BufferedWriter fileout = new BufferedWriter(new FileWriter(fileDiseases));
			fileout.write(output);
			fileout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	catch (NullPointerException nex) {
		nex.printStackTrace();
	}
	
	// PROCESS gene info
	try {
		List<ResultType> ctd_gad = conn_disease.getCTDGADGraphData(cId);
		List<String> _tbl_header = new ArrayList<String>();
		List<String> _tbl_field = new ArrayList<String>();
		
		_tbl_header.addAll(Arrays.asList("gene_symbol", "gene_name", "disease_id", "evidence", "pubmed"));
		_tbl_field.addAll(Arrays.asList("gene_sym","gene_name","disease_id", "evidence", "pubmed"));
		
		String output = "";
		int i;
		
		for (i=0; i<_tbl_header.size()-1;i++) {
			output += _tbl_header.get(i)+"\t";
		}
		output += _tbl_header.get(i)+"\r\n";
		
		Iterator<ResultType> itr = ctd_gad.iterator();
		
		while (itr.hasNext()) {
			ResultType datarow = itr.next();
			String _f = "";
			for (i=0;i<_tbl_field.size()-1;i++) {
				_f = _tbl_field.get(i);
				if (datarow.get(_f)!=null) {
					output += StringHelper.strip_html_tag(datarow.get(_f))+"\t";
				} else {
					output += "\t";
				}
			}
			_f = _tbl_field.get(i);
			output += datarow.get(_f)+"\r\n";
		}
		
		try {
			fileGenes = tmpDir + "/_"+key+"_genes.txt";
			BufferedWriter fileout = new BufferedWriter(new FileWriter(fileGenes));
			fileout.write(output);
			fileout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	catch (NullPointerException nex) {
		nex.printStackTrace();
	}
	
	try {
		
		List<ResultType> vfdb = conn_disease.getVFDBGraphData(cId);
		List<String> _tbl_header = new ArrayList<String>();
		List<String> _tbl_field = new ArrayList<String>();
		
		_tbl_header.addAll(Arrays.asList("ncbi_tax_id", "rank", "parent_id", "genome_name", "accession", "na_feature_id", "source_id", "product", "vfg_id", "vf_id", "gene_name"));
		_tbl_field.addAll(Arrays.asList("ncbi_tax_id","rank","parent_id", "genome_name", "accession", "na_feature_id", "source_id", "product", "vfg_id", "vf_id", "gene_name"));
	
		String output = "";
		int i;
		
		for (i=0; i<_tbl_header.size()-1;i++) {
			output += _tbl_header.get(i)+"\t";
		}
		output += _tbl_header.get(i)+"\r\n";
		
		Iterator<ResultType> itr = vfdb.iterator();
		
		while (itr.hasNext()) {
			ResultType datarow = itr.next();
			String _f = "";
			for (i=0;i<_tbl_field.size()-1;i++) {
				_f = _tbl_field.get(i);
				if (datarow.get(_f)!=null) {
					output += StringHelper.strip_html_tag(datarow.get(_f).replaceAll("[\'\"]", ""))+"\t";
				} else {
					output += "\t";
				}
			}
			_f = _tbl_field.get(i);
			output += datarow.get(_f)+"\r\n";
		}
		
		try {
			fileVFS = tmpDir + "/_"+key+"_vfs.txt";
			BufferedWriter fileout = new BufferedWriter(new FileWriter(fileVFS));
			fileout.write(output);
			fileout.close();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	catch (NullPointerException nex) {
		nex.printStackTrace();
	}

	String exec = "perl " + path + "/server/idv-gidi.pl " + machine+ " " + cId +" "+ fileDiseases + " " + fileGenes + " " + fileVFS; 
	
	//System.out.println(exec);
	
	CommandResults callperl = ExecUtilities.exec(exec);
	
	String[] output = callperl.getStdout();
	for(int i=0; i< output.length; i++) {
		out.println(output[i]);
	}
	
	if (remove) {
		exec = "rm "+ tmpDir + "/_"+key+"_diseases.txt " + tmpDir + "/_"+key+"_genes.txt " + tmpDir + "/_"+key+"_vfs.txt"; 
		callperl = ExecUtilities.exec(exec);
	}
%>