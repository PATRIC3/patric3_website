package edu.vt.vbi.patric.common;

public enum SolrCore {
	FEATURE("dnafeature"), SEQUENCE("sequenceinfo"), GENOME("genomesummary"), FIGFAM_DIC("figfam-dic"), EC("ecnumber"), GO("goterm"), PATHWAY(
			"pathway"), TAXONOMY("taxonomy"), PROTEIN_INTERACTION("pig"), STRUCTURE("structural-genomics"), SPECIALTY_GENE("specialty-gene"), SPECIALTY_GENE_EVIDENCE(
			"specialty-gene-evidence"), SPECIALTY_GENE_MAPPING("specialty-gene-mapping"), TRANSCRIPTOMICS_EXPERIMENT("genexp-experiment"), TRANSCRIPTOMICS_COMPARISON(
			"genexp-sample"), TRANSCRIPTOMICS_GENE("genexp-gene"), PROTEOMICS_EXPERIMENT("proteomics-experiment"), PROTEOMICS_PROTEIN(
			"proteomics-protein"), PROTEOMICS_PEPTIDE("proteomics-peptide");

	private String coreName;

	private SolrCore(String name) {
		coreName = name;
	}

	public String getSolrCoreJoin(String from, String to, String condition) {
		return "{!join from=" + from + " to=" + to + " fromIndex=" + coreName + "}" + condition;
	}

	public String getSolrCoreName() {
		return coreName;
	}
}
