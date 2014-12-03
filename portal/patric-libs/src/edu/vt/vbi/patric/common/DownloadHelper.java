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

import java.util.List;
import java.util.Arrays;

public class DownloadHelper {

	public static List<String> getHeaderForFeatures() {
		return Arrays.asList("Genome", "Accession", "SEED ID", "RefSeq Locus Tag", "Alt Locus Tag", "Feature Id", "Annotation", "Feature Type", "Start", "End",
				"Length", "Strand", "Protein ID", "AA Length", "Gene Symbol", "Product");
	}

	public static List<String> getFieldsForFeatures() {
		return Arrays.asList("genome_name", "accession", "seed_id", "refseq_locus_tag", "alt_locus_tag", "feature_id", "annotation", "feature_type", "start",
				"end", "na_length", "strand", "protein_id", "aa_length", "gene", "product");
	}

	public static List<String> getHeaderForGenomes() {
		return Arrays.asList("Genome ID", "Genome Name", "Organism Name", "NCBI Taxon ID", "Genome Status",
				"Strain", "Serovar", "Biovar", "Pathovar", "MLST", "Culture Collection", "Type Strain",
				"Completion Date", "Publication",
				"NCBI Project ID", "RefSeq Project ID", "GenBank Accessions", "RefSeq Accessions",
				"Sequencing Centers", "Sequencing Status", "Sequencing Platform", "Sequencing Depth", "Assembly Method",
				"Chromosomes", "Plasmids", "Contigs", "Sequences", "Genome Length", "GC Content",
				"PATRIC CDS", "BRC1 CDS", "RefSeq CDS",
				"Isolation Site", "Isolation Source", "Isolation Comments", "Collection Date", "Isolation Country", "Geographic Location", "Latitude", "Longitude", "Altitude", "Depth",
				"Host Name", "Host Gender", "Host Age", "Host Health", "Body Sample Site", "Body Sample Subsite",
				"Gram Stain", "Cell Shape", "Motility", "Sporulation", "Temperature Range", "Optimal Temperature", "Salinity", "Oxygen Requirement", "Habitat",
				"Disease", "Comments");
	}

	public static List<String> getFieldsForGenomes() {
		return Arrays.asList("genome_id", "genome_name", "organism_name", "taxon_id", "genome_status",
				"strain", "serovar", "biovar", "pathovar", "mlst", "culture_collection", "type_strain",
				"completion_date", "publication",
				"ncbi_project_id", "refseq_project_id", "genbank_accessions", "refseq_accessions",
				"sequencing_centers", "sequencing_status", "sequencing_platform", "sequencing_depth", "assembly_method",
				"chromosomes", "plasmids", "contigs", "sequences", "genome_length", "gc_content",
				"patric_cds", "brc1_cds", "refseq_cds",
				"isolation_site", "isolation_source", "isolation_comments", "collection_date", "isolation_country", "geographic_location", "latitude", "longitude", "altitude", "depth",
				"host_name", "host_gender", "host_age", "host_health", "body_sample_site", "body_sample_subsite",
				"gram_stain", "cell_shape", "motility", "sporulation", "temperature_range", "optimal_temperature", "salinity", "oxygen_requirement", "habitat",
				"disease", "comments");
	}
}