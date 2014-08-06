<%@ page import="java.util.*"
%><%@ page import="edu.vt.vbi.patric.dao.DBSummary"
%><%@ page import="edu.vt.vbi.patric.dao.ResultType"
%><%@ page import="edu.vt.vbi.patric.beans.DNAFeature"
%><%
String _accession = request.getParameter("accession");
String _algorithm = request.getParameter("algorithm");

String _accn = null;
String _sid = null;

if (_accession!=null && _algorithm!=null) {
	
	String[] names = _accession.split("\\|");
	if (names[3] != null) {
		_accn = names[3];
	}
	if (names[1] != null) {
		_sid = names[1];
	}
	//System.out.println(_accession+","+_accn+","+_sid);
	
	HashMap<String,String> key = new HashMap<String,String>();
	key.put("accession",_accn);
	key.put("algorithm",_algorithm);
	key.put("sid", _sid);

	DBSummary conn_summary = new DBSummary();
	//ArrayList<ResultType> features = conn_summary.getFeatures(key);
	List<DNAFeature> features = conn_summary.getDNAFeatures(key);
	
	ArrayList<Integer> hist = conn_summary.getHistogram(key);
	//calculate avg
	int hist_sum = 0;
	double hist_avg = 0;
	for (int i=0; i< hist.size(); i++) {
		hist_sum += hist.get(0);
	}
	if (hist.size() > 0) {
		hist_avg = hist_sum / hist.size();
	}
	//
	StringBuilder nclist = new StringBuilder();
	Formatter formatter = new Formatter(nclist, Locale.US);
	
	int features_count = features.size();

	for (int i=0; i<features_count; i++) {
		if (nclist.length()>0) {
			nclist.append(",");
		}
		DNAFeature f = features.get(i);
		formatter.format("[0, %d, %d, %d, %d, \"%s\", %d, \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", %d]",
			(f.getStart()-1),
			f.getStart(),
			f.getEnd(),
			(f.getStrand().equals("+")?1:-1),
			f.getStrand(),

			f.getId(),
			f.getLocusTag(),
			_algorithm,
			f.getFeatureType(),
			f.hasProduct()?f.getProduct().replace("\\","").replace("\"", "\\\""):"",

			f.getGene(),
			f.getRefseqLocusTag(),
			(f.getFeatureType().equals("CDS")?0:(f.getFeatureType().contains("RNA")?1:2))
		);
	}
	response.setContentType("application/json");
%>
{
	"featureCount": <%=features_count %>,
	"formatVersion": 1,
	"histograms": {
		"meta": [{
			"arrayParams": {
				"chunkSize": 10000,
				"length": <%=hist.size()%>,
				"urlTemplate": "Hist.json.jsp?accession=<%=_accession%>&algorithm=<%=_algorithm%>&chunk={Chunk}&format=.json"
			},
			"basesPerBin": "10000"
		}],
		"stats": [{
			"basesPerBin": "10000",
			"max": <%=(hist.isEmpty())?"0":Collections.max(hist)%>,
			"mean": <%=hist_avg%>
		}]
	},
	"intervals": {
		"classes": [{
			"attributes": [
				"Start", "Start_str", "End", "Strand", "strand_str",
				"id", "locus_tag", "source", "type", "product",
				"gene", "refseq", "phase"
			],
			"isArrayAttr": {}
		}],
		"count": <%=features_count %>,
		"lazyClass": 5,
		"maxEnd": 20000,
		"minStart": 1,
		"nclist": [<%=nclist.toString() %>],
		"urlTemplate": "lf-{Chunk}.json"
	}
}
<%	} else {	%>
	wrong parameters!
<%	}	%>