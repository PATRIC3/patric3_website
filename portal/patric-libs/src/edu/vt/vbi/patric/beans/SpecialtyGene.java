/*
 * Copyright 2015. Virginia Polytechnic Institute and State University
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package edu.vt.vbi.patric.beans;

import org.apache.solr.client.solrj.beans.Field;
import org.json.simple.JSONObject;

public class SpecialtyGene {

	@Field
	private String id;

	@Field("genome_id")
	private String genomeId;

	@Field("genome_name")
	private String genomeName;

	@Field("taxon_id")
	private int taxonId;

	@Field("feature_id")
	private String featureId;

	@Field("alt_locus_tag")
	private String altLocusTag;

	@Field("refseq_locus_tag")
	private String refseqLocusTag;

	@Field("patric_id")
	private String patricId;

	@Field
	private String gene;

	@Field
	private String product;

	@Field
	private String property;

	@Field
	private String source;

	@Field("property_source")
	private String propertySource;

	@Field("source_id")
	private String sourceId;

	@Field
	private String organism;

	@Field
	private String function;

	@Field
	private String classification;

	@Field
	private String pmid;

	@Field
	private String assertion;

	@Field("query_coverage")
	private int queryCoverage;

	@Field("subject_coverage")
	private int subjectCoverage;

	@Field
	private int identity;

	@Field("e_value")
	private String eValue;

	@Field("same_species")
	private int sameSpecies;

	@Field("same_genus")
	private int sameGenus;

	@Field("same_genome")
	private int sameGenome;

	@Field
	private String evidence;

	public JSONObject toJSONObject() {
		JSONObject json = new JSONObject();
		json.put("genome_id", getGenomeId());
		json.put("genome_name", getGenomeName());
		json.put("taxon_id", getTaxonId());

		json.put("feature_id", getFeatureId());
		json.put("alt_locus_tag", getAltLocusTag());
		json.put("refseq_locus_tag", getRefseqLocusTag());
		json.put("patric_id", getPatricId());

		json.put("gene", getGene());
		json.put("product", getProduct());

		json.put("property", getProperty());
		json.put("source", getSource());
		json.put("property_source", getPropertySource());

		json.put("source_id", getSourceId());
		json.put("organism", getOrganism());
		json.put("function", getFunction());
		json.put("classification", getClassification());

		json.put("pmid", getPmid());
		json.put("assertion", getAssertion());

		json.put("query_coverage", getQueryCoverage());
		json.put("subject_coverage", getSubjectCoverage());
		json.put("identity", getIdentity());
		json.put("e_value", geteValue());

		json.put("same_species", getSameSpecies());
		json.put("same_genus", getSameGenus());
		json.put("same_genome", getSameGenome());
		json.put("evidence", getEvidence());

		return json;
	}

	public String getGene() {
		return gene;
	}

	public String getGenomeName() {
		return genomeName;
	}

	public String getId() {
		return id;
	}

	public String getProduct() {
		return product;
	}

	public String getRefseqLocusTag() {
		return refseqLocusTag;
	}

	public boolean hasGene() {
		return (this.gene != null);
	}

	public boolean hasAltLocusTag() {
		return (this.altLocusTag != null);
	}

	public boolean hasProduct() {
		return (this.product != null);
	}

	public boolean hasRefseqLocusTag() {
		return (this.refseqLocusTag != null);
	}

	public boolean hasPatricId() {
		return (this.patricId != null);
	}

	public boolean hasOrganism() {
		return (this.organism != null);
	}

	public boolean hasPmid() {
		return (this.pmid != null);
	}

	public boolean hasSubjectCoverage() {
		return (this.subjectCoverage > 0);
	}

	public boolean hasQueryCoverage() {
		return (this.queryCoverage > 0);
	}

	public boolean hasIdentity() {
		return (this.identity > 0);
	}

	public boolean haseValue() {
		return (this.eValue != null);
	}

	public String getGenomeId() {
		return genomeId;
	}

	public void setGenomeId(String genomeId) {
		this.genomeId = genomeId;
	}

	public int getTaxonId() {
		return taxonId;
	}

	public void setTaxonId(int taxonId) {
		this.taxonId = taxonId;
	}

	public String getAltLocusTag() {
		return altLocusTag;
	}

	public void setAltLocusTag(String altLocusTag) {
		this.altLocusTag = altLocusTag;
	}

	public String getPatricId() {
		return patricId;
	}

	public void setPatricId(String seedId) {
		this.patricId = seedId;
	}

	public void setGene(String gene) {
		this.gene = gene;
	}

	public void setGenomeName(String genomeName) {
		this.genomeName = genomeName;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public String getFeatureId() {
		return featureId;
	}

	public void setFeatureId(String featureId) {
		this.featureId = featureId;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getPropertySource() {
		return propertySource;
	}

	public void setPropertySource(String propertySource) {
		this.propertySource = propertySource;
	}

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public String getOrganism() {
		return organism;
	}

	public void setOrganism(String organism) {
		this.organism = organism;
	}

	public String getFunction() {
		return function;
	}

	public void setFunction(String function) {
		this.function = function;
	}

	public String getClassification() {
		return classification;
	}

	public void setClassification(String classification) {
		this.classification = classification;
	}

	public String getPmid() {
		return pmid;
	}

	public void setPmid(String pmid) {
		this.pmid = pmid;
	}

	public String getAssertion() {
		return assertion;
	}

	public void setAssertion(String assertion) {
		this.assertion = assertion;
	}

	public int getQueryCoverage() {
		return queryCoverage;
	}

	public void setQueryCoverage(int queryCoverage) {
		this.queryCoverage = queryCoverage;
	}

	public int getSubjectCoverage() {
		return subjectCoverage;
	}

	public void setSubjectCoverage(int subjectCoverage) {
		this.subjectCoverage = subjectCoverage;
	}

	public int getIdentity() {
		return identity;
	}

	public void setIdentity(int identity) {
		this.identity = identity;
	}

	public String geteValue() {
		return eValue;
	}

	public void seteValue(String eValue) {
		this.eValue = eValue;
	}

	public int getSameSpecies() {
		return sameSpecies;
	}

	public void setSameSpecies(int sameSpecies) {
		this.sameSpecies = sameSpecies;
	}

	public int getSameGenus() {
		return sameGenus;
	}

	public void setSameGenus(int sameGenus) {
		this.sameGenus = sameGenus;
	}

	public int getSameGenome() {
		return sameGenome;
	}

	public void setSameGenome(int sameGenome) {
		this.sameGenome = sameGenome;
	}

	public String getEvidence() {
		return evidence;
	}

	public void setEvidence(String evidence) {
		this.evidence = evidence;
	}

	public void setRefseqLocusTag(String refseqLocusTag) {
		this.refseqLocusTag = refseqLocusTag;
	}
}
