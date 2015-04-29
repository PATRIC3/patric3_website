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
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Genome {

	private static final Logger LOGGER = LoggerFactory.getLogger(Genome.class);

	DateFormat dfCompletionDate = new SimpleDateFormat("yyyy-MM-dd");

	@Field("genome_id")
	private String id;

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

	// taxonomy related fields: taxon_lineage_ids, taxon_lineage_names, kingdom, phylum, class, order, family, genus, species

	@Field("genus")
	private String genus;

	@Field("genome_status")
	private String genomeStatus;

	@Field("strain")
	private String strain;

	@Field
	private String serovar;

	@Field
	private String biovar;

	@Field
	private String pathovar;

	@Field
	private String mlst;

	@Field("other_typing")
	private List<String> otherTyping;

	@Field("culture_collection")
	private String cultureCollection;

	@Field("type_strain")
	private String typeStrain;

	@Field("reference_genome")
	private String referenceGenome;

	@Field("completion_date")
	private Date completionDate;

	@Field("publication")
	private String publication;

	@Field("bioproject_accession")
	private String bioprojectAccession;

	@Field("biosample_accession")
	private String biosampleAccession;

	@Field("assembly_accession")
	private String assemblyAccession;

	@Field("ncbi_project_id")
	private String ncbiProjectId;

	@Field("refseq_project_id")
	private String refseqProjectId;

	@Field("genbank_accessions")
	private String genbankAccessions;

	@Field("refseq_accessions")
	private String refseqAccessions;

	@Field("sequencing_centers")
	private String sequencingCenters;

	@Field("sequencing_status")
	private String sequencingStatus;

	@Field("sequencing_platform")
	private String sequencingPlatform;

	@Field("sequencing_depth")
	private String sequencingDepth;

	@Field("assembly_method")
	private String assemblyMethod;

	@Field("chromosomes")
	private int chromosomes;

	@Field("plasmids")
	private int plasmids;

	@Field("contigs")
	private int contigs;

	@Field("sequences")
	private int sequences;

	@Field("genome_length")
	private int genomeLength;

	@Field("gc_content")
	private float gcContent;

	@Field("patric_cds")
	private int patricCds;

	@Field("brc1_cds")
	private int brc1Cds;

	@Field("refseq_cds")
	private int refseqCds;

	@Field("isolation_site")
	private String isolationSite;

	@Field("isolation_source")
	private String isolationSource;

	@Field("isolation_comments")
	private String isolationComments;

	@Field("collection_date")
	private String collectionDate;

	@Field("isolation_country")
	private String isolationCountry;

	@Field("geographic_location")
	private String geographicLocation;

	@Field("latitude")
	private String latitude;

	@Field("longitude")
	private String longitude;

	@Field("altitude")
	private String altitude;

	@Field("depth")
	private String depth;

	@Field("other_environmental")
	private List<String> otherEnvironmental;

	@Field("host_name")
	private String hostName;

	@Field("host_gender")
	private String hostGender;

	@Field("host_age")
	private String hostAge;

	@Field("host_health")
	private String hostHealth;

	@Field("body_sample_site")
	private String bodySampleSite;

	@Field("body_sample_subsite")
	private String bodySampleSubsite;

	@Field("other_clinical")
	private List<String> otherClinical;

	@Field("antimicrobial_resistance")
	private List<String> antimicrobialResistance;

	@Field("antimicrobial_resistance_evidence")
	private String antimicrobialResistanceEvidence;

	@Field("gram_stain")
	private String gramStain;

	@Field("cell_shape")
	private String cellShape;

	@Field("motility")
	private String motility;

	@Field("sporulation")
	private String sporulation;

	@Field("temperature_range")
	private String temperatureRange;

	@Field("optimal_temperature")
	private String optimalTemperature;

	@Field("salinity")
	private String salinity;

	@Field("oxygen_requirement")
	private String oxygenRequirement;

	@Field("habitat")
	private String habitat;

	@Field("disease")
	private List<String> disease;

	@Field("comments")
	private List<String> comments;

	@Field("additional_metadata")
	private List<String> additionalMetadata;

	public JSONObject toJSONObject() {
		JSONObject json = new JSONObject();

		json.put("genome_id", getId());
		// p2_genome_id
		json.put("genome_name", getGenomeName());
		// common_name, organism_name

		json.put("taxon_id", getTaxonId());
		// taxon_lineage_ids, taxon_lineage_names
		// kingdom, phylum, class, order, family, genus, species

		json.put("genome_status", getGenomeStatus());

		json.put("strain", getStrain());
		json.put("serovar", getSerovar());
		json.put("biovar", getBiovar());
		json.put("pathovar", getPathovar());
		json.put("mlst", getMlst());
		json.put("other_typing", getOtherTyping());
		json.put("cluture_collection", getCultureCollection());
		json.put("type_strain", getTypeStrain());
		json.put("reference_genome", getReferenceGenome());

		json.put("completion_date", getCompletionDate());
		json.put("publication", getPublication());

		json.put("bioproject_accession", getBioprojectAccession());
		json.put("biosample_accession", getBiosampleAccession());
		json.put("assembly_accession", getAssemblyAccession());
		json.put("ncbi_project_id", getNcbiProjectId());
		json.put("refseq_project_id", getRefseqProjectId());
		json.put("genbank_accessions", getGenbankAccessions());
		json.put("refseq_accessions", getRefseqAccessions());

		json.put("sequencing_centers", getSequencingCenters());
		json.put("sequencing_status", getSequencingStatus());
		json.put("sequencing_platform", getSequencingPlatform());
		json.put("sequencing_depth", getSequencingDepth());
		json.put("assembly_method", getAssemblyMethod());

		json.put("chromosomes", getChromosomes());
		json.put("plasmids", getPlasmids());
		json.put("contigs", getContigs());
		json.put("sequences", getSequences());
		json.put("genome_length", getGenomeLength());
		json.put("gc_content", getGcContent());

		json.put("patric_cds", getPatricCds());
		json.put("brc1_cds", getBrc1Cds());
		json.put("refseq_cds", getRefseqCds());

		json.put("isolation_site", getIsolationSite());
		json.put("isolation_source", getIsolationSource());
		json.put("isolation_comments", getIsolationComments());
		json.put("collection_date", getCollectionDate());
		json.put("isolation_country", getIsolationCountry());
		json.put("geographic_location", getGeographicLocation());
		json.put("latitude", getLatitude());
		json.put("longitude", getLongitude());
		json.put("altitude", getAltitude());
		json.put("depth", getDepth());
		json.put("other_environmental", getOtherEnvironmental());

		json.put("host_name", getHostName());
		json.put("host_gender", getHostGender());
		json.put("host_age", getHostAge());
		json.put("host_health", getHostHealth());
		json.put("body_sample_site", getBodySampleSite());
		json.put("body_sample_subsite", getBodySampleSubsite());
		json.put("other_clinical", getOtherClinical());

		json.put("antimicrobial_resistance", getAntimicrobialResistance());
		json.put("antimicrobial_resistance_evidence", getAntimicrobialResistanceEvidence());

		json.put("gram_stain", getGramStain());
		json.put("cell_shape", getCellShape());
		json.put("motility", getMotility());
		json.put("sporulation", getSporulation());
		json.put("temperature_range", getTemperatureRange());
		json.put("optimal_temporature", getOptimalTemperature());
		json.put("salinity", getSalinity());
		json.put("oxygen_requirement", getOxygenRequirement());
		json.put("habitat", getHabitat());

		json.put("disease", getDisease());

		json.put("comments", getComments());

		json.put("additional_metadata", getAdditionalMetadata());

		return json;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getP2GenomeId() {
		return p2GenomeId;
	}

	public void setP2GenomeId(int p2GenomeId) {
		this.p2GenomeId = p2GenomeId;
	}

	public String getGenomeName() {
		return genomeName;
	}

	public void setGenomeName(String genomeName) {
		this.genomeName = genomeName;
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

	public int getTaxonId() {
		return taxonId;
	}

	public void setTaxonId(int taxonId) {
		this.taxonId = taxonId;
	}

	public String getGenus() {
		return genus;
	}

	public void setGenus(String genus) {
		this.genus = genus;
	}

	public String getGenomeStatus() {
		return genomeStatus;
	}

	public void setGenomeStatus(String genomeStatus) {
		this.genomeStatus = genomeStatus;
	}

	public String getStrain() {
		return strain;
	}

	public void setStrain(String strain) {
		this.strain = strain;
	}

	public String getSerovar() {
		return serovar;
	}

	public void setSerovar(String serovar) {
		this.serovar = serovar;
	}

	public String getBiovar() {
		return biovar;
	}

	public void setBiovar(String biovar) {
		this.biovar = biovar;
	}

	public String getPathovar() {
		return pathovar;
	}

	public void setPathovar(String pathovar) {
		this.pathovar = pathovar;
	}

	public String getMlst() {
		return mlst;
	}

	public void setMlst(String mlst) {
		this.mlst = mlst;
	}

	public List<String> getOtherTyping() {
		return otherTyping;
	}

	public void setOtherTyping(List<String> otherTyping) {
		this.otherTyping = otherTyping;
	}

	public String getCultureCollection() {
		return cultureCollection;
	}

	public void setCultureCollection(String cultureCollection) {
		this.cultureCollection = cultureCollection;
	}

	public String getTypeStrain() {
		return typeStrain;
	}

	public void setTypeStrain(String typeStrain) {
		this.typeStrain = typeStrain;
	}

	public String getReferenceGenome() {
		return referenceGenome;
	}

	public void setReferenceGenome(String referenceGenome) {
		this.referenceGenome = referenceGenome;
	}

	public String getCompletionDate() {
		if (completionDate != null) {
			return dfCompletionDate.format(completionDate);
		}
		else {
			return null;
		}
	}

	public void setCompletionDate(Date completionDate) {
		this.completionDate = completionDate;
	}

	public String getPublication() {
		return publication;
	}

	public void setPublication(String publication) {
		this.publication = publication;
	}

	public String getBioprojectAccession() {
		return bioprojectAccession;
	}

	public void setBioprojectAccession(String bioprojectAccession) {
		this.bioprojectAccession = bioprojectAccession;
	}

	public String getBiosampleAccession() {
		return biosampleAccession;
	}

	public void setBiosampleAccession(String biosampleAccession) {
		this.biosampleAccession = biosampleAccession;
	}

	public String getAssemblyAccession() {
		return assemblyAccession;
	}

	public void setAssemblyAccession(String assemblyAccession) {
		this.assemblyAccession = assemblyAccession;
	}

	public String getNcbiProjectId() {
		return ncbiProjectId;
	}

	public void setNcbiProjectId(String ncbiProjectId) {
		this.ncbiProjectId = ncbiProjectId;
	}

	public String getRefseqProjectId() {
		return refseqProjectId;
	}

	public void setRefseqProjectId(String refseqProjectId) {
		this.refseqProjectId = refseqProjectId;
	}

	public String getGenbankAccessions() {
		return genbankAccessions;
	}

	public void setGenbankAccessions(String genbankAccessions) {
		this.genbankAccessions = genbankAccessions;
	}

	public String getRefseqAccessions() {
		return refseqAccessions;
	}

	public void setRefseqAccessions(String refseqAccessions) {
		this.refseqAccessions = refseqAccessions;
	}

	public String getSequencingCenters() {
		return sequencingCenters;
	}

	public void setSequencingCenters(String sequencingCenters) {
		this.sequencingCenters = sequencingCenters;
	}

	public String getSequencingStatus() {
		return sequencingStatus;
	}

	public void setSequencingStatus(String sequencingStatus) {
		this.sequencingStatus = sequencingStatus;
	}

	public String getSequencingPlatform() {
		return sequencingPlatform;
	}

	public void setSequencingPlatform(String sequencingPlatform) {
		this.sequencingPlatform = sequencingPlatform;
	}

	public String getSequencingDepth() {
		return sequencingDepth;
	}

	public void setSequencingDepth(String sequencingDepth) {
		this.sequencingDepth = sequencingDepth;
	}

	public String getAssemblyMethod() {
		return assemblyMethod;
	}

	public void setAssemblyMethod(String assemblyMethod) {
		this.assemblyMethod = assemblyMethod;
	}

	public int getChromosomes() {
		return chromosomes;
	}

	public void setChromosomes(int chromosomes) {
		this.chromosomes = chromosomes;
	}

	public int getPlasmids() {
		return plasmids;
	}

	public void setPlasmids(int plasmids) {
		this.plasmids = plasmids;
	}

	public int getContigs() {
		return contigs;
	}

	public void setContigs(int contigs) {
		this.contigs = contigs;
	}

	public int getSequences() {
		return sequences;
	}

	public void setSequences(int sequences) {
		this.sequences = sequences;
	}

	public int getGenomeLength() {
		return genomeLength;
	}

	public void setGenomeLength(int genomeLength) {
		this.genomeLength = genomeLength;
	}

	public float getGcContent() {
		return gcContent;
	}

	public void setGcContent(float gcContent) {
		this.gcContent = gcContent;
	}

	public int getPatricCds() {
		return patricCds;
	}

	public void setPatricCds(int patricCds) {
		this.patricCds = patricCds;
	}

	public int getBrc1Cds() {
		return brc1Cds;
	}

	public void setBrc1Cds(int brc1Cds) {
		this.brc1Cds = brc1Cds;
	}

	public int getRefseqCds() {
		return refseqCds;
	}

	public void setRefseqCds(int refseqCds) {
		this.refseqCds = refseqCds;
	}

	public String getIsolationSite() {
		return isolationSite;
	}

	public void setIsolationSite(String isolationSite) {
		this.isolationSite = isolationSite;
	}

	public String getIsolationSource() {
		return isolationSource;
	}

	public void setIsolationSource(String isolationSource) {
		this.isolationSource = isolationSource;
	}

	public String getIsolationComments() {
		return isolationComments;
	}

	public void setIsolationComments(String isolationComments) {
		this.isolationComments = isolationComments;
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

	public String getGeographicLocation() {
		return geographicLocation;
	}

	public void setGeographicLocation(String geographicLocation) {
		this.geographicLocation = geographicLocation;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getAltitude() {
		return altitude;
	}

	public void setAltitude(String altitude) {
		this.altitude = altitude;
	}

	public String getDepth() {
		return depth;
	}

	public void setDepth(String depth) {
		this.depth = depth;
	}

	public List<String> getOtherEnvironmental() {
		return otherEnvironmental;
	}

	public void setOtherEnvironmental(List<String> otherEnvironmental) {
		this.otherEnvironmental = otherEnvironmental;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getHostGender() {
		return hostGender;
	}

	public void setHostGender(String hostGender) {
		this.hostGender = hostGender;
	}

	public String getHostAge() {
		return hostAge;
	}

	public void setHostAge(String hostAge) {
		this.hostAge = hostAge;
	}

	public String getHostHealth() {
		return hostHealth;
	}

	public void setHostHealth(String hostHealth) {
		this.hostHealth = hostHealth;
	}

	public String getBodySampleSite() {
		return bodySampleSite;
	}

	public void setBodySampleSite(String bodySampleSite) {
		this.bodySampleSite = bodySampleSite;
	}

	public String getBodySampleSubsite() {
		return bodySampleSubsite;
	}

	public void setBodySampleSubsite(String bodySampleSubsite) {
		this.bodySampleSubsite = bodySampleSubsite;
	}

	public List<String> getOtherClinical() {
		return otherClinical;
	}

	public void setOtherClinical(List<String> otherClinical) {
		this.otherClinical = otherClinical;
	}

	public List<String> getAntimicrobialResistance() {
		return antimicrobialResistance;
	}

	public void setAntimicrobialResistance(List<String> antimicrobialResistance) {
		this.antimicrobialResistance = antimicrobialResistance;
	}

	public String getAntimicrobialResistanceEvidence() {
		return antimicrobialResistanceEvidence;
	}

	public void setAntimicrobialResistanceEvidence(String antimicrobialResistanceEvidence) {
		this.antimicrobialResistanceEvidence = antimicrobialResistanceEvidence;
	}

	public String getGramStain() {
		return gramStain;
	}

	public void setGramStain(String gramStain) {
		this.gramStain = gramStain;
	}

	public String getCellShape() {
		return cellShape;
	}

	public void setCellShape(String cellShape) {
		this.cellShape = cellShape;
	}

	public String getMotility() {
		return motility;
	}

	public void setMotility(String motility) {
		this.motility = motility;
	}

	public String getSporulation() {
		return sporulation;
	}

	public void setSporulation(String sporulation) {
		this.sporulation = sporulation;
	}

	public String getTemperatureRange() {
		return temperatureRange;
	}

	public void setTemperatureRange(String temperatureRange) {
		this.temperatureRange = temperatureRange;
	}

	public String getOptimalTemperature() {
		return optimalTemperature;
	}

	public void setOptimalTemperature(String optimalTemperature) {
		this.optimalTemperature = optimalTemperature;
	}

	public String getSalinity() {
		return salinity;
	}

	public void setSalinity(String salinity) {
		this.salinity = salinity;
	}

	public String getOxygenRequirement() {
		return oxygenRequirement;
	}

	public void setOxygenRequirement(String oxygenRequirement) {
		this.oxygenRequirement = oxygenRequirement;
	}

	public String getHabitat() {
		return habitat;
	}

	public void setHabitat(String habitat) {
		this.habitat = habitat;
	}

	public List<String> getDisease() {
		return disease;
	}

	public void setDisease(List<String> disease) {
		this.disease = disease;
	}

	public List<String> getComments() {
		return comments;
	}

	public void setComments(List<String> comments) {
		this.comments = comments;
	}

	public List<String> getAdditionalMetadata() {
		return additionalMetadata;
	}

	public void setAdditionalMetadata(List<String> additionalMetadata) {
		this.additionalMetadata = additionalMetadata;
	}

	public boolean hasCollectionDate() {
		return (this.collectionDate != null);
	}

	public boolean hasCompletionDate() {
		return (this.completionDate != null);
	}

	public boolean hasHostName() {
		return (this.hostName != null);
	}

	public boolean hasDisease() {
		return (this.disease != null);
	}

	public boolean hasIsolationCountry() {
		return (this.isolationCountry != null);
	}
}
