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

import java.util.List;

import org.apache.solr.client.solrj.beans.Field;

public class DNAFeature {
	@Field
	private String accession;

	@Field("aa_length")
	private int proteinLength;

	@Field("na_feature_id")
	private long id;

	@Field("sequence_info_id")
	private int sequenceInfoId;

	@Field("figfam_id")
	private String figfamId;

	@Field("locus_tag")
	private String locusTag;

	@Field("start_max")
	private int start;

	@Field("end_min")
	private int end;

	@Field("pseed_id")
	private String pseedId;

	@Field
	private String strand;

	@Field("feature_type")
	private String featureType;

	@Field
	private String annotation;

	@Field("genome_name")
	private String genomeName;

	@Field("gid")
	private int genomeInfoId;

	@Field("pos_group")
	private String posGroup;

	@Field("ncbi_tax_id")
	private int ncbiTaxonId;

	@Field
	private String product;

	@Field
	private String gene;

	@Field("ids")
	private List<String> externalId;
	
	@Field("ec")
	private List<String> ecNumber;
	
	@Field("go")
	private List<String> goTerm;

	@Field("pathway")
	private List<String> pathway;

	@Field("refseq_locus_tag")
	private String refseqLocusTag;

	@Field("refseq_protein_id")
	private String refseqProteinId;

	public String getAccession() {
		return accession;
	}

	public String getAnnotation() {
		return annotation;
	}

	public List<String> getEcNumber() {
		return ecNumber;
	}

	public int getEnd() {
		return end;
	}

	public List<String> getExternalId() {
		return externalId;
	}
	
	public String getFeatureType() {
		return featureType;
	}
	
	public String getFigfamId() {
		return figfamId;
	}

	public String getGene() {
		return gene;
	}

	public int getGenomeInfoId() {
		return genomeInfoId;
	}

	public String getGenomeName() {
		return genomeName;
	}

	public List<String> getGoTerm() {
		return goTerm;
	}

	public long getId() {
		return id;
	}

	public String getLocusTag() {
		return locusTag;
	}

	public int getNcbiTaxonId() {
		return ncbiTaxonId;
	}

	public List<String> getPathway() {
		return pathway;
	}

	public String getPosGroup() {
		return posGroup;
	}

	public String getPosGroupInQuote() {
		return "\"" + posGroup + "\"";
	}

	public String getProduct() {
		return product;
	}

	public int getProteinLength() {
		return proteinLength;
	}

	public String getPseedId() {
		return pseedId;
	}

	public String getRefseqLocusTag() {
		return refseqLocusTag;
	}

	public String getRefseqProteinId() {
		return refseqProteinId;
	}

	public int getSequenceInfoId() {
		return sequenceInfoId;
	}

	public int getStart() {
		return start;
	}

	public String getStrand() {
		return strand;
	}

	public boolean hasFigfamId() {
		return (this.figfamId != null);
	}
	public boolean hasGene() {
		return (this.gene != null);
	}

	public boolean hasLocusTag() {
		return (this.locusTag != null);
	}

	public boolean hasProduct() {
		return (this.product != null);
	}

	public boolean hasRefseqLocusTag() {
		return (this.refseqLocusTag != null);
	}

	public boolean hasRefseqProteinId() {
		return (this.refseqProteinId != null);
	}

	public boolean hasExternalId() {
		return (this.externalId != null && this.externalId.isEmpty() == false);
	}
	
	public boolean hasEcNumber() {
		return (this.ecNumber != null && this.ecNumber.isEmpty() == false);
	}
	
	public boolean hasGoTerm() {
		return (this.goTerm != null && this.goTerm.isEmpty() == false);
	}
	
	public boolean hasPathway() {
		return (this.pathway != null && this.pathway.isEmpty() == false);
	}
	public void setAccession(String accession) {
		this.accession = accession;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

	public void setEcNumber(List<String> ecNumber) {
		this.ecNumber = ecNumber;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public void setExternalId(List<String> externalId) {
		this.externalId = externalId;
	}

	public void setFeatureType(String featureType) {
		this.featureType = featureType;
	}

	public void setFigfamId(String figfamId) {
		this.figfamId = figfamId;
	}

	public void setGene(String gene) {
		this.gene = gene;
	}

	public void setGenomeInfoId(int genomeInfoId) {
		this.genomeInfoId = genomeInfoId;
	}

	public void setGenomeName(String genomeName) {
		this.genomeName = genomeName;
	}

	public void setGoTerm(List<String> goTerm) {
		this.goTerm = goTerm;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setLocusTag(String locusTag) {
		this.locusTag = locusTag;
	}

	public void setNcbiTaxonId(int ncbiTaxonId) {
		this.ncbiTaxonId = ncbiTaxonId;
	}

	public void setPathway(List<String> pathway) {
		this.pathway = pathway;
	}

	public void setPosGroup(String posGroup) {
		this.posGroup = posGroup;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public void setProteinLength(int proteinLength) {
		this.proteinLength = proteinLength;
	}

	public void setPseedId(String pseedId) {
		this.pseedId = pseedId;
	}

	public void setRefseqLocusTag(String refseqLocusTag) {
		this.refseqLocusTag = refseqLocusTag;
	}

	public void setRefseqProteinId(String refseqProteinId) {
		this.refseqProteinId = refseqProteinId;
	}

	public void setSequenceInfoId(int sequenceInfoId) {
		this.sequenceInfoId = sequenceInfoId;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public void setStrand(String strand) {
		this.strand = strand;
	}
}
