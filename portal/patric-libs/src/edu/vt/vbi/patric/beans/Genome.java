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
package edu.vt.vbi.patric.beans;

import org.apache.solr.client.solrj.beans.Field;

import java.util.List;

public class Genome {

	@Field("genome_id")
	private int genomeId;

	@Field("p2_genome_id")
	private int p2GenomeId;

	@Field("genome_name")
	private String genomeName;

	@Field("common_name")
	private String commonName;

	@Field("organism_name")
	private String organismName;

	@Field("taxon_id")
	private int taxonId;

	// taxonomy related fields

	@Field("genome_status")
	private String genomeStatus;

	@Field("strain")
	private String strain;

	@Field("disease")
	private List<String> disease;

	@Field("host_name")
	private String hostName;

	@Field("collection_date")
	private String collectionDate;

	@Field("isolation_country")
	private String isolationCountry;

/*
	"serovar"
	"biovar"
	"pathovar"
	"culture_collection"
	"type_strain"
	"project_status"
	"availability"
	"sequencing_centers"
	"completion_date"
	"publication"
	"ncbi_project_id"
	"refseq_project_id"
	"genbank_accessions"
	"refseq_accessions"
	"sequencing_status"
	"sequencing_platform"
	"sequencing_depth"
	"assembly_method"
	"chromosomes"
	"plasmids"
	"contigs"
	"sequences"
	"genome_length"
	"gc_content"
	"rast_cds"
	"brc_cds"
	"refseq_cds"
	"isolation_site"
	"isolation_source"
	"isolation_comments"
	"geographic_location"
	"latitude"
	"longitude"
	"altitude"
	"depth"
	"host_gender"
	"host_age"
	"host_health"
	"body_sample_site"
	"body_sample_subsite"
	"gram_stain"
	"cell_shape"
	"motility"
	"sporulation"
	"temperature_range"
	"optimal_temperature"
	"salinity"
	"oxygen_requirement"
	"habitat"
	"mlst"
	"comments"
*/

	public int getP2GenomeId() {
		return p2GenomeId;
	}

	public void setP2GenomeId(int p2GenomeId) {
		this.p2GenomeId = p2GenomeId;
	}

	public String getCommonName() {
		return commonName;
	}

	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	public String getOrganismName() {
		return organismName;
	}

	public void setOrganismName(String organismName) {
		this.organismName = organismName;
	}

	public String getStrain() {
		return strain;
	}

	public void setStrain(String strain) {
		this.strain = strain;
	}

	public int getGenomeId() {
		return genomeId;
	}

	public void setGenomeId(int genomeId) {
		this.genomeId = genomeId;
	}

	public int getTaxonId() {
		return taxonId;
	}

	public void setTaxonId(int taxonId) {
		this.taxonId = taxonId;
	}

	public String getGenomeName() {
		return genomeName;
	}

	public void setGenomeName(String genomeName) {
		this.genomeName = genomeName;
	}

	public String getGenomeStatus() {
		return genomeStatus;
	}

	public void setGenomeStatus(String genomeStatus) {
		this.genomeStatus = genomeStatus;
	}

	public List<String> getDisease() {
		return disease;
	}

	public void setDisease(List<String> disease) {
		this.disease = disease;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getCollectionDate() {
		return collectionDate;
	}

	public void setCollectionDate(String collectionDate) {
		this.collectionDate = collectionDate;
	}

	public String getIsolationCountry() {
		return isolationCountry;
	}

	public void setIsolationCountry(String isolationCountry) {
		this.isolationCountry = isolationCountry;
	}
}
