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
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="edu.vt.vbi.patric.portlets.DiseaseOverview" %>
<%@ page import="org.json.simple.*" %>
<%
	String tmpDir = System.getProperty("java.io.tmpdir", "/tmp");
	boolean remove = true;
	String fileDiseases = null, fileGenes = null, fileVFS = null;
    final Logger LOGGER = LoggerFactory.getLogger(DiseaseOverview.class);

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
	else if ((new File("/usr/share/jboss_deploy/jboss-as/server/patric/patric_website/webRoot/patric/idv-perl/server/idv-gidi.pl")).exists())
	{
	    path = "/usr/share/jboss_deploy/jboss-as/server/patric/patric_website/webRoot/patric/idv-perl";
	    machine = "cluster";
	}

	String cId = request.getParameter("cId");

	DBDisease conn_disease = new DBDisease();

	try {
		List<ResultType> mesh_terms = conn_disease.getMeshTermGraphData(cId);
		List<String> _tbl_header = new ArrayList<String>();
		List<String> _tbl_field = new ArrayList<String>();
		
		_tbl_header.addAll(Arrays.asList("taxon_id", "organism_name", "organism_rank", "parent_id", "mesh_disease_id", "mesh_disease_name", "mesh_tree_node", "parent_tree_node", "description"));
		_tbl_field.addAll(Arrays.asList("taxon_id","organism_name","organism_rank", "parent_id", "mesh_disease_id", "mesh_disease_name", "mesh_tree_node", "parent_tree_node", "description"));
		
		StringBuilder output = new StringBuilder();
		int i;

		for (i=0; i<_tbl_header.size()-1;i++) {
			output.append(_tbl_header.get(i)).append("\t");
		}
		output.append(_tbl_header.get(i)).append("\r\n");

		for (ResultType datarow: mesh_terms) {
			String _f = "";
			for (i=0;i<_tbl_field.size()-1;i++) {
				_f = _tbl_field.get(i);
				if (datarow.get(_f)!=null) {
					output.append(StringHelper.strip_html_tag(datarow.get(_f))).append("\t");
				} else {
					output.append("\t");
				}
			}
			_f = _tbl_field.get(i);
			output.append(datarow.get(_f)).append("\r\n");
		}
		
		try {
			fileDiseases = tmpDir + "/_" + key + "_diseases.txt";
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileDiseases));
			writer.write(output.toString());
			writer.close();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
	catch (NullPointerException nex) {
		LOGGER.error(nex.getMessage(), nex);
	}
	
	// PROCESS gene info
	try {
		List<ResultType> ctd_gad = conn_disease.getCTDGADGraphData(cId);
		List<String> _tbl_header = new ArrayList<String>();
		List<String> _tbl_field = new ArrayList<String>();
		
		_tbl_header.addAll(Arrays.asList("gene_symbol", "gene_name", "disease_id", "evidence", "pubmed"));
		_tbl_field.addAll(Arrays.asList("gene_sym","gene_name","disease_id", "evidence", "pubmed"));
		
		StringBuilder output = new StringBuilder();
		int i;

		for (i=0; i<_tbl_header.size()-1;i++) {
			output.append(_tbl_header.get(i)).append("\t");
		}
		output.append(_tbl_header.get(i)).append("\r\n");

	    for (ResultType datarow: ctd_gad) {
			String _f = "";
			for (i=0;i<_tbl_field.size()-1;i++) {
				_f = _tbl_field.get(i);
				if (datarow.get(_f)!=null) {
					output.append(StringHelper.strip_html_tag(datarow.get(_f))).append("\t");
				} else {
					output.append("\t");
				}
			}
			_f = _tbl_field.get(i);
			output.append(datarow.get(_f)).append("\r\n");
		}
		
		try {
			fileGenes = tmpDir + "/_" + key + "_genes.txt";
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileGenes));
			writer.write(output.toString());
			writer.close();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
	catch (NullPointerException nex) {
		LOGGER.error(nex.getMessage(), nex);
	}
	
	try {
		
		List<ResultType> vfdb = conn_disease.getVFDBGraphData(cId);
		List<String> _tbl_header = new ArrayList<String>();
		List<String> _tbl_field = new ArrayList<String>();
		
		_tbl_header.addAll(Arrays.asList("ncbi_tax_id", "rank", "parent_id", "genome_name", "accession", "na_feature_id", "source_id", "product", "vfg_id", "vf_id", "gene_name"));
		_tbl_field.addAll(Arrays.asList("ncbi_tax_id","rank","parent_id", "genome_name", "accession", "na_feature_id", "source_id", "product", "vfg_id", "vf_id", "gene_name"));
	
		StringBuilder output = new StringBuilder();
		int i;
		
		for (i=0; i<_tbl_header.size()-1;i++) {
			output.append(_tbl_header.get(i)).append("\t");
		}
		output.append(_tbl_header.get(i)).append("\r\n");

		for (ResultType datarow: vfdb) {
			String _f = "";
			for (i=0;i<_tbl_field.size()-1;i++) {
				_f = _tbl_field.get(i);
				if (datarow.get(_f)!=null) {
					output.append(StringHelper.strip_html_tag(datarow.get(_f).replaceAll("[\'\"]", ""))).append("\t");
				} else {
					output.append("\t");
				}
			}
			_f = _tbl_field.get(i);
			output.append(datarow.get(_f)).append("\r\n");
		}
		
		try {
			fileVFS = tmpDir + "/_" + key + "_vfs.txt";
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileVFS));
			writer.write(output.toString());
			writer.close();
		} catch (Exception e){
			LOGGER.error(e.getMessage(), e);
		}
	}
	catch (NullPointerException nex) {
		LOGGER.error(nex.getMessage(), nex);
	}

	String exec = "perl " + path + "/server/idv-gidi.pl " + machine+ " " + cId +" "+ fileDiseases + " " + fileGenes + " " + fileVFS; 

    LOGGER.debug(exec);
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