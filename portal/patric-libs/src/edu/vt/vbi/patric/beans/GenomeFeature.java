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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class GenomeFeature {
	@Field("genome_id")
	private String genomeId;

	@Field("genome_name")
	private String genomeName;

	@Field("taxon_id")
	private int taxonId;

	@Field("sequence_id")
	private String sequenceId;

	@Field
	private String accession;

	@Field
	private String annotation;

	@Field("feature_type")
	private String featureType;

	@Field("feature_id")
	private String id;

	@Field("p2_feature_id")
	private long p2FeatureId;

	@Field("alt_locus_tag")
	private String altLocusTag;

	@Field("patric_id")
	private String patricId;

	@Field("refseq_locus_tag")
	private String refseqLocusTag;

	@Field("protein_id")
	private String proteinId;

	@Field("gene_id")
	private long geneId;

	@Field("gi")
	private long gi;

	@Field
	private int start;

	@Field
	private int end;

	@Field
	private String strand;

	@Field
	private String location;

	@Field
	private List<String> segments;

	@Field("pos_group")
	private String posGroup;

	@Field("na_length")
	private int naSequenceLength;

	@Field("aa_length")
	private int proteinLength;

	@Field("na_sequence")
	private String naSequence;

	@Field("aa_sequence")
	private String aaSequence;

	@Field
	private String gene;

	@Field
	private String product;

	// extra ids
	@Field("figfam_id")
	private String figfamId;

	@Field("ec")
	private List<String> enzymeClass;

	@Field("pathway")
	private List<String> pathway;

	@Field("go")
	private List<String> geneOntology;

	@Field("uniprotkb_accession")
	private List<String> uniprotkbAccession;

	@Field("ids")
	private List<String> externalId;

	@Field("public")
	private boolean isPublic;

	public JSONObject toJSONObject() {
		JSONObject json = new JSONObject();
		json.put("genome_id", getGenomeId());
		json.put("genome_name", getGenomeName());
		json.put("taxon_id", getTaxonId());
		json.put("sequence_id", getSequenceId());
		json.put("accession", getAccession());
		json.put("annotation", getAnnotation());
		json.put("feature_type", getFeatureType());
		json.put("feature_id", getId());
		// p2_feature_id
		json.put("alt_locus_tag", getAltLocusTag());
		json.put("patric_id", getPatricId());
		json.put("refseq_locus_tag", getRefseqLocusTag());
		json.put("protein_id", getProteinId());
		json.put("gene_id", getGeneId());
		json.put("gi", getGi());
		json.put("start", getStart());
		json.put("end", getEnd());
		json.put("strand", getStrand());
		// location, segments
		json.put("pos_group", getPosGroup());
		json.put("na_length", getNaSequenceLength());
		json.put("aa_length", getProteinLength());
		// na_sequence, translation, aa_sequence_md5
		json.put("gene", getGene());
		json.put("product", getProduct());
		json.put("figfam_id", getFigfamId());
		// ec, pathway, go, ids

		return json;
	}

	public String getAccession() {
		return accession;
	}

	public String getAnnotation() {
		return annotation;
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

	public String getGenomeName() {
		return genomeName;
	}

	public String getId() {
		return id;
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

	public String getPosGroupEncoded() {
		String pos = getPosGroupInQuote();
		try {
			return URLEncoder.encode(pos, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getProduct() {
		return product;
	}

	public int getProteinLength() {
		return proteinLength;
	}

	public String getRefseqLocusTag() {
		return refseqLocusTag;
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

	public boolean hasAltLocusTag() {
		return (this.altLocusTag != null);
	}

	public boolean hasProduct() {
		return (this.product != null);
	}

	public boolean hasRefseqLocusTag() {
		return (this.refseqLocusTag != null);
	}

	public boolean hasProteinId() {
		return (this.proteinId != null);
	}

	public boolean hasPatricId() {
		return (this.patricId != null);
	}

	public boolean hasNaSequence() {
		return (this.naSequence != null);
	}

	public boolean hasAaSequence() {
		return (this.aaSequence != null);
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

	public String getSequenceId() {
		return sequenceId;
	}

	public void setSequenceId(String sequenceId) {
		this.sequenceId = sequenceId;
	}

	public long getP2FeatureId() {
		return p2FeatureId;
	}

	public void setP2FeatureId(long p2FeatureId) {
		this.p2FeatureId = p2FeatureId;
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

	public void setPatricId(String patricId) {
		this.patricId = patricId;
	}

	public String getProteinId() {
		return proteinId;
	}

	public void setProteinId(String proteinId) {
		this.proteinId = proteinId;
	}

	public boolean hasGeneId() {
		return (this.geneId > 0);
	}

	public long getGeneId() {
		return geneId;
	}

	public void setGeneId(long geneId) {
		this.geneId = geneId;
	}

	public long getGi() {
		return gi;
	}

	public void setGi(long gi) {
		this.gi = gi;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public List<String> getSegments() {
		return segments;
	}

	public void setSegments(List<String> segments) {
		this.segments = segments;
	}

	public int getNaSequenceLength() {
		return naSequenceLength;
	}

	public void setNaSequenceLength(int naSequenceLength) {
		this.naSequenceLength = naSequenceLength;
	}

	public String getNaSequence() {
		return naSequence;
	}

	public void setNaSequence(String naSequence) {
		this.naSequence = naSequence;
	}

	public String getAaSequence() {
		return aaSequence;
	}

	public void setAaSequence(String aaSequence) {
		this.aaSequence = aaSequence;
	}

	public List<String> getEnzymeClass() {
		return enzymeClass;
	}

	public void setEnzymeClass(List<String> enzymeClass) {
		this.enzymeClass = enzymeClass;
	}

	public List<String> getGeneOntology() {
		return geneOntology;
	}

	public void setGeneOntology(List<String> geneOntology) {
		this.geneOntology = geneOntology;
	}

	public List<String> getUniprotkbAccession() {
		return uniprotkbAccession;
	}

	public void setUniprotkbAccession(List<String> uniprotkbAccession) {
		this.uniprotkbAccession = uniprotkbAccession;
	}

	public boolean hasExternalId() {
		return (this.externalId != null && !this.externalId.isEmpty());
	}

	public boolean hasEnzymeClass() {
		return (this.enzymeClass != null && !this.enzymeClass.isEmpty());
	}

	public boolean hasGeneOntology() {
		return (this.geneOntology != null && !this.geneOntology.isEmpty());
	}

	public boolean hasPathway() {
		return (this.pathway != null && !this.pathway.isEmpty());
	}

	public void setAccession(String accession) {
		this.accession = accession;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
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

	public void setGenomeName(String genomeName) {
		this.genomeName = genomeName;
	}

	public void setId(String id) {
		this.id = id;
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

	public void setRefseqLocusTag(String refseqLocusTag) {
		this.refseqLocusTag = refseqLocusTag;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public void setStrand(String strand) {
		this.strand = strand;
	}

	public boolean isPublic() {
		return this.isPublic;
	}
}
