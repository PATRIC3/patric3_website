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

import java.util.Arrays;
import java.util.List;

public class DownloadHelper {

	public static List<String> getHeaderForFeatures() {
		return Arrays.asList("Genome", "Accession", "PATRIC ID", "RefSeq Locus Tag", "Alt Locus Tag", "Feature ID",
				"Annotation", "Feature Type", "Start", "End", "Length", "Strand", "Protein ID", "AA Length", "Gene Symbol", "Product");
	}

	public static List<String> getFieldsForFeatures() {
		return Arrays.asList("genome_name", "accession", "seed_id", "refseq_locus_tag", "alt_locus_tag", "feature_id", "annotation", "feature_type",
				"start", "end", "na_length", "strand", "protein_id", "aa_length", "gene", "product");
	}

	public static List<String> getHeaderForGenomes() {
		return Arrays.asList("Genome ID", "Genome Name", "Organism Name", "NCBI Taxon ID", "Genome Status",
				"Strain", "Serovar", "Biovar", "Pathovar", "MLST", "Other Typing",
				"Culture Collection", "Type Strain",
				"Completion Date", "Publication",
				"BioProject Accession", "BioSample Accession", "Assembly Accession", "GenBank Accessions",
				"RefSeq Accessions",
				"Sequencing Centers", "Sequencing Status", "Sequencing Platform", "Sequencing Depth", "Assembly Method",
				"Chromosomes", "Plasmids", "Contigs", "Sequences", "Genome Length", "GC Content",
				"PATRIC CDS", "BRC1 CDS", "RefSeq CDS",
				"Isolation Site", "Isolation Source", "Isolation Comments", "Collection Date",
				"Isolation Country", "Geographic Location", "Latitude", "Longitude", "Altitude", "Depth", "Other Environmental",
				"Host Name", "Host Gender", "Host Age", "Host Health", "Body Sample Site", "Body Sample Subsite", "Other Clinical",
				"AntiMicrobial Resistance", "AntiMicrobial Resistance Evidence",
				"Gram Stain", "Cell Shape", "Motility", "Sporulation", "Temperature Range", "Optimal Temperature", "Salinity", "Oxygen Requirement",
				"Habitat",
				"Disease", "Comments", "Additional Metadata");
	}

	public static List<String> getFieldsForGenomes() {
		return Arrays.asList("genome_id", "genome_name", "organism_name", "taxon_id", "genome_status",
				"strain", "serovar", "biovar", "pathovar", "mlst", "other_typing",
				"culture_collection", "type_strain",
				"completion_date", "publication",
				"bioproject_accession", "biosample_accession", "assembly_accession", "genbank_accessions",
				"refseq_accessions",
				"sequencing_centers", "sequencing_status", "sequencing_platform", "sequencing_depth", "assembly_method",
				"chromosomes", "plasmids", "contigs", "sequences", "genome_length", "gc_content",
				"patric_cds", "brc1_cds", "refseq_cds",
				"isolation_site", "isolation_source", "isolation_comments", "collection_date",
				"isolation_country", "geographic_location", "latitude", "longitude", "altitude", "depth", "other_environmental",
				"host_name", "host_gender", "host_age", "host_health", "body_sample_site", "body_sample_subsite", "other_clinical",
				"antimicrobial_resistance", "antimicrobial_resistance_evidence",
				"gram_stain", "cell_shape", "motility", "sporulation", "temperature_range", "optimal_temperature", "salinity", "oxygen_requirement",
				"habitat",
				"disease", "comments", "additional_metadata");
	}

	public static List<String> getHeaderForGenomeSequence() {
		return Arrays.asList("Genome ID", "Genome Name", "Sequence ID", "GI", "Accession", "Sequence Type", "Topology", "Description",
				"GC Content", "Length (bp)", "Release Date", "Version");
	}

	public static List<String> getFieldsForGenomeSequence() {
		return Arrays.asList("genome_id", "genome_name", "sequence_id", "gi", "accession", "sequence_type", "topology", "description",
				"gc_content", "length", "release_date", "version");
	}

	public static List<String> getHeaderForSpecialtyGeneMapping() {
		return Arrays.asList("Evidence", "Property", "Source", "Genome Name", "PATRIC ID", "RefSeq Locus Tag", "Alt Locus Tag", "Source ID",
				"Source Organism", "Gene", "Product", "Function", "Classification", "PubMed", "Subject Coverage", "Query Coverage",
				"Identity", "E-value");
	}

	public static List<String> getFieldsForSpecialtyGeneMapping() {
		return Arrays.asList("evidence", "property", "source", "genome_name", "seed_id", "refseq_locus_tag", "alt_locus_tag", "source_id",
				"organism", "gene", "product", "function", "classification", "pmid", "subject_coverage", "query_coverage", "identity",
				"e_value");
	}

	public static List<String> getHeaderForSpecialtyGeneSource() {
		return Arrays.asList("Property", "Source", "Source ID", "Gene", "Organism", "Locus Tag", "Gene ID", "GI", "Product",
				"Function", "Classification", "PubMed");
	}

	public static List<String> getFieldsForSpecialtyGeneSource() {
		return Arrays.asList("property", "source", "source_id", "gene_name", "organism", "locus_tag", "gene_id", "gi", "product",
				"function", "classification", "pmid");
	}

	public static List<String> getHeaderForTranscriptomicsExperiment() {
		return Arrays.asList("Experiment ID", "Title", "Comparisons", "Genes", "PubMed", "Accession", "Organism", "Strain",
				"Gene Modification", "Experimental Condition", "Time Series", "Release Date", "Author", "PI", "Institution");
	}

	public static List<String> getFieldsForTranscriptomicsExperiment() {
		return Arrays.asList("eid", "title", "samples", "genes", "pmid", "accession", "organism", "strain", "mutant",
				"condition", "timeseries", "release_date", "author", "pi", "institution");
	}

	public static List<String> getHeaderForTranscriptomicsComparison() {
		return Arrays.asList("Experiment ID", "Comparison ID", "Title", "Genes", "Significant genes(Log Ratio)",
				"Significant genes(Z Score)", "PubMed", "Accession", "Organism", "Strain", "Gene Modification", "Experiment Condition",
				"Time Point", "Release Date");
	}

	public static List<String> getFieldsForTranscriptomicsComparison() {
		return Arrays.asList("eid", "pid", "expname", "genes", "sig_log_ratio", "sig_z_score", "pmid", "accession",
				"organism", "strain", "mutant", "condition", "timepoint", "release_date");
	}
}