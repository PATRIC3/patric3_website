package edu.vt.vbi.patric.common;

public enum SolrCore {
	FEATURE("genome_feature"), SEQUENCE("genome_sequence"), GENOME("genome"), FIGFAM_DIC("protein_family_ref"),
	EC("ecnumber"), GO("goterm"), PATHWAY("pathway"), PATHWAY_REF("pathway_ref"), TAXONOMY("taxonomy"), PROTEIN_INTERACTION("pig"),
	STRUCTURE("misc_niaid_sgc"),
	SPECIALTY_GENE("sp_gene_ref"), SPECIALTY_GENE_EVIDENCE("sp_gene_evidence"), SPECIALTY_GENE_MAPPING("sp_gene"),
	TRANSCRIPTOMICS_EXPERIMENT("transcriptomics_experiment"), TRANSCRIPTOMICS_COMPARISON("transcriptomics_sample"),
	TRANSCRIPTOMICS_GENE("transcriptomics_gene"),
	PROTEOMICS_EXPERIMENT("proteomics_experiment"), PROTEOMICS_PROTEIN("proteomics_protein"), PROTEOMICS_PEPTIDE("proteomics_peptide"),
	ID_REF("id_ref"), GENOME_AMR("genome_amr");

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
