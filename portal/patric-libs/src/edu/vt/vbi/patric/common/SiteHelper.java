/*******************************************************************************
 * Copyright 2014 Virginia Polytechnic Institute and State University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package edu.vt.vbi.patric.common;

import edu.vt.vbi.patric.beans.Genome;
import edu.vt.vbi.patric.beans.GenomeFeature;
import edu.vt.vbi.patric.beans.Taxonomy;
import org.w3c.dom.Element;

import javax.portlet.MimeResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

public class SiteHelper {

	public static String getLinks(String target, String id) {
		String link = "";
		if (target.equals("taxon_overview")) {
			link = "<a href=\"Taxon?cType=taxon&amp;cId=" + id
					+ "\"><img src=\"/patric/images/icon_taxon.gif\" alt=\"Taxonomy Overview\" title=\"Taxonomy Overview\" /></a>";
		}
		else if (target.equals("genome_list")) {
			link = "<a href=\"GenomeList?cType=taxon&amp;cId="
					+ id
					+ "&amp;dataSource=All&amp;displayMode=genome\"><img src=\"/patric/images/icon_sequence_list.gif\" alt=\"Genome List\" title=\"Genome List\" /></a>";
		}
		else if (target.equals("feature_table")) {
			link = "<a href=\"FeatureTable?cType=taxon&amp;cId="
					+ id
					+ "&amp;featuretype=CDS&amp;annotation=All&amp;filtertype=\"><img src=\"/patric/images/icon_table.gif\" alt=\"Feature Table\" title=\"Feature Table\"/></a>";
		}
		return link;
	}

	public static String getExternalLinks(String target) {
		String link = "";

		if (target.equalsIgnoreCase("ncbi_gene")) {
			link = "//www.ncbi.nlm.nih.gov/sites/entrez?db=gene&cmd=Retrieve&dopt=full_report&list_uids=";
		}
		else if (target.equalsIgnoreCase("ncbi_accession")) {
			link = "//www.ncbi.nlm.nih.gov/entrez/viewer.fcgi?db=nucleotide&val=";
		}
		else if (target.equalsIgnoreCase("ncbi_protein") || target.equalsIgnoreCase("RefSeq") || target.equalsIgnoreCase("GI")) {
			link = "//www.ncbi.nlm.nih.gov/protein/";
		}
		else if (target.equalsIgnoreCase("RefSeq_NT")) {
			link = "//www.ncbi.nlm.nih.gov/nuccore/"; // NC_010067.1 - // nucleotide db
		}
		else if (target.equalsIgnoreCase("go_term")) {
			link = "//amigo.geneontology.org/cgi-bin/amigo/term_details?term="; // GO:0004747
		}
		else if (target.equalsIgnoreCase("ec_number")) {
			link = "//enzyme.expasy.org/EC/"; // 2.7.1.15
		}
		else if (target.equalsIgnoreCase("kegg_pathwaymap") || target.equalsIgnoreCase("KEGG")) {
			link = "//www.genome.jp/dbget-bin/www_bget?"; // pathway+map00010
		}
		else if (target.equalsIgnoreCase("UniProtKB-Accession") || target.equalsIgnoreCase("UniProtKB-ID")) {
			link = "//www.uniprot.org/uniprot/"; // A9MFG0 or ASTD_SALAR
		}
		else if (target.equalsIgnoreCase("UniRef100") || target.equalsIgnoreCase("UniRef90") || target.equalsIgnoreCase("UniRef50")) {
			link = "//www.uniprot.org/uniref/"; // UniRef100_A9MFG0, UniRef90_B5F7J0, or // UniRef50_Q1C8A9
		}
		else if (target.equalsIgnoreCase("UniParc")) {
			link = "//www.uniprot.org/uniparc/"; // UPI0001603B3F
		}
		else if (target.equalsIgnoreCase("EMBL") || target.equalsIgnoreCase("EMBL-CDS")) {
			link = "//www.ebi.ac.uk/ena/data/view/"; // CP000880, ABX21565
		}
		else if (target.equalsIgnoreCase("GeneID")) {
			link = "//www.ncbi.nlm.nih.gov/sites/entrez?db=gene&term="; // 5763416;
		}
		else if (target.equalsIgnoreCase("GenomeReviews")) {
			link = "//www.genomereviews.ebi.ac.uk/GR/contigview?chr="; // CP000880_GR
		}
		else if (target.equalsIgnoreCase("eggNOG")) {
			link = "//eggnog.embl.de/cgi_bin/display_multi_clusters.pl?linksource=uniprot&level=0&1="; // Q2YII1 -- uniprot accession
		}
		else if (target.equalsIgnoreCase("HOGENOM")) {
			link = "//pbil.univ-lyon1.fr/cgi-bin/acnuc-ac2tree?db=HOGENOM&query="; // A9MFG0 -- uniprot accession
		}
		else if (target.equalsIgnoreCase("OMA")) {
			link = "//omabrowser.org/cgi-bin/gateway.pl?f=DisplayGroup&p1="; // A9MFG0 -- uniprot accession
		}
		else if (target.equalsIgnoreCase("ProtClustDB")) {
			link = "//www.ncbi.nlm.nih.gov/sites/entrez?Db=proteinclusters&Cmd=DetailsSearch&Term="; // A9MFG0 -- uniprot accession
		}
		else if (target.equalsIgnoreCase("BioCyc")) {
			link = "//biocyc.org/getid?id="; // BMEL359391:BAB2_0179-MONOMER
		}
		else if (target.equalsIgnoreCase("NMPDR")) {
			link = "//www.nmpdr.org/linkin.cgi?id="; // fig|382638.8.peg.1669"
		}
		else if (target.equalsIgnoreCase("EnsemblGenome") || target.equalsIgnoreCase("EnsemblGenome_TRS")
				|| target.equalsIgnoreCase("EnsemblGenome_PRO")) {
			link = "//www.ensemblgenomes.org/id/"; // EBMYCT00000005579
		}
		else if (target.equalsIgnoreCase("BEIR")) {
			link = "//www.beiresources.org/Catalog/ItemDetails/tabid/522/Default.aspx?Template=Clones&BEINum=";
		}
		else if (target.equalsIgnoreCase("PDB")) {
			link = "Jmol?structureID=";
		}
		else if (target.equalsIgnoreCase("STRING")) { // 204722.BR0001
			link = "//string.embl.de/newstring_cgi/show_network_section.pl?identifier=";
		}
		else if (target.equalsIgnoreCase("MEROPS")) { // M50.005
			link = "//merops.sanger.ac.uk/cgi-bin/pepsum?id=";
		}
		else if (target.equalsIgnoreCase("PATRIC")) { // 17788255
			link = "Feature?cType=feature&cId=";
		}
		else if (target.equalsIgnoreCase("OrthoDB")) { // EOG689HR1
			link = "//cegg.unige.ch/orthodb7/results?searchtext=";
		}
		else if (target.equalsIgnoreCase("NCBI_TaxID")) { // 29461
			link = "//www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?mode=Info&id=";
		}
		else if (target.equalsIgnoreCase("KO")) { // K04756
			link = "//www.genome.jp/dbget-bin/www_bget?ko:";
		}
		else if (target.equalsIgnoreCase("TubercuList")) { // Rv2429
			link = "//tuberculist.epfl.ch/quicksearch.php?gene+name=";
		}
		else if (target.equalsIgnoreCase("PeroxiBase")) { // 4558
			link = "//peroxibase.toulouse.inra.fr/browse/process/view_perox.php?id=";
		}
		else if (target.equalsIgnoreCase("Reactome")) { // REACT_116125
			link = "//www.reactome.org/cgi-bin/eventbrowser_st_id?ST_ID=";
		}
		else if (target.equalsIgnoreCase("VFDB")) {
			link = "//www.mgc.ac.cn/cgi-bin/VFs/gene.cgi?GeneID="; // VFG1817
		}
		else if (target.equalsIgnoreCase("VFDB_HOME")) {
			link = "//www.mgc.ac.cn/VFs/";
		}
		else if (target.equalsIgnoreCase("Victors")) {
			link = "//www.phidias.us/victors/gene_detail.php?c_mc_victor_id="; // 220
		}
		else if (target.equalsIgnoreCase("Victors_HOME")) {
			link = "//www.phidias.us/victors/";
		}
		else if (target.equalsIgnoreCase("PATRIC_VF")) {
			link = "SpecialtyGeneEvidence?source=PATRIC_VF&sourceId="; // Rv3875
		}
		else if (target.equalsIgnoreCase("PATRIC_VF_HOME")) {
			link = "SpecialtyGeneSource?source=PATRIC_VF&kw=";
		}
		else if (target.equalsIgnoreCase("ARDB")) {
			link = "//ardb.cbcb.umd.edu/cgi/search.cgi?db=R&term="; // AAL09826
		}
		else if (target.equalsIgnoreCase("ARDB_HOME")) {
			link = "//ardb.cbcb.umd.edu/";
		}
		else if (target.equalsIgnoreCase("CARD")) {
			link = ""; //TODO: need to add
		}
		else if (target.equalsIgnoreCase("CARD_HOME")) {
			link = "//arpcard.mcmaster.ca";
		}
		else if (target.equalsIgnoreCase("DrugBank")) {
			link = "//v3.drugbank.ca/molecules/"; // 1
		}
		else if (target.equalsIgnoreCase("DrugBank_HOME")) {
			link = "//v3.drugbank.ca";
		}
		else if (target.equalsIgnoreCase("TTD")) {
			link = "//bidd.nus.edu.sg/group/TTD/ZFTTDDetail.asp?ID="; // TTDS00427
		}
		else if (target.equalsIgnoreCase("TTD_HOME")) {
			link = "//bidd.nus.edu.sg/group/TTD/ttd.asp";
		}
		else if (target.equalsIgnoreCase("Human")) {
			link = "//www.ncbi.nlm.nih.gov/protein/"; // NP_001005484.1
		}
		else if (target.equalsIgnoreCase("Human_HOME")) {
			link = "//www.ncbi.nlm.nih.gov/assembly/GCF_000001405.26";
		}
		return link;
	}

	/*
	 * @ called by patric-overview/WebContents/WEB-INF/jsp/feature_summary.jsp
	 */
	public static String getGenusByStructuralGenomicsCenter(String name) {
		// 'Mycobacterium', 'Bartonella', 'Brucella', 'Ehrlichia', 'Rickettsia',
		// 'Burkholderia', 'Borrelia', 'Anaplasma'

		// 1763,138,780,773,234,943,32008,768
		String ssgcid = "Mycobacterium|Bartonella|Brucella|Ehrlichia|Rickettsia|Burkholderia|Borrelia|Anaplasma";

		// 'Bacillus', 'Listeria', 'Staphylococcus', 'Streptococcus',
		// 'Clostridium',
		// 'Coxiella', 'Escherichia', 'Francisella', 'Salmonella', 'Shigella',
		// 'Vibrio', 'Yersinia', 'Campylobacter', 'Helicobacter'

		// 1485,1279,1386,1301,1637,194,662,209,776,620,262,561,629,590

		String csgid = "Bacillus|Listeria|Staphylococcus|Streptococcus|Clostridium|Coxiella|Escherichia|Francisella|Salmonella|Shigella|Vibrio|Yersinia|Campylobacter|Helicobacter";

		switch (name) {
		case "ssgcid":
			return ssgcid;
		case "csgid":
			return csgid;
		default:
			return "";
		}
	}

	public static void addHtmlMetaElements(RenderRequest req, RenderResponse res, String key, Element el) {
		res.addProperty(key, el);
	}

	public void setHtmlMetaElements(RenderRequest req, RenderResponse res, String context) {
		String strTitle = "PATRIC::";
		String strKeywords = "";
		String contextType = req.getParameter("context_type");
		String contextId = req.getParameter("context_id");

		if (contextType != null && contextId != null && !contextId.equals("")) {
			// Get taxon/genome/feature info
			SolrInterface solr = new SolrInterface();

			switch (contextType) {
			case "taxon":

				Taxonomy taxonomy = solr.getTaxonomy(Integer.parseInt(contextId));
				if (taxonomy != null) {
					strTitle += taxonomy.getTaxonName() + "::" + context;
					strKeywords = context + ", " + taxonomy.getTaxonName() + ", PATRIC";
				}
				else {
					strTitle += context;
				}
				break;
			case "genome":

				Genome genome = solr.getGenome(contextId);
				if (genome != null) {
					strTitle += genome.getGenomeName() + "::" + context;
					strKeywords = context + ", " + genome.getGenomeName() + ", PATRIC";
				}
				break;
			case "feature":

				GenomeFeature feature = solr.getFeature(contextId);
				if (feature != null) {
					strTitle += feature.getSeedId() + ":" + feature.getProduct() + "::" + context;
					strKeywords = context + ", " + feature.getSeedId() + ":" + feature.getProduct() + ", PATRIC";
				}
				break;
			}
		}
		else {
			strTitle += context;
			strKeywords = context + ", PATRIC";
		}

		// Setup elements
		Element elTitle = res.createElement("title");
		Element elKeywords = res.createElement("meta");

		elTitle.setTextContent(strTitle);
		elKeywords.setAttribute("name", "Keywords");
		elKeywords.setAttribute("content", strKeywords);

		// Set to headerContents
		res.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, elTitle);
		res.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, elKeywords);
	}
}
