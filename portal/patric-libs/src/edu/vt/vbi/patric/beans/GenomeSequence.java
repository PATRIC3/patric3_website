/*
 * Copyright 2014. Virginia Polytechnic Institute and State University
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

import java.util.Date;

public class GenomeSequence {
	@Field("genome_id")
	private String genomeId;

	@Field("genome_name")
	private String genomeName;

	@Field("taxon_id")
	private int taxonId;

	@Field("sequence_id")
	private String id;

	@Field
	private String accession;

	@Field
	private int gi;

	@Field("sequence_type")
	private String sequenceType;

	@Field
	private String topology;

	@Field
	private String description;

	@Field
	private String chromosome;

	@Field
	private String plasmid;

	@Field("gc_content")
	private float gcContent;

	@Field
	private int length;

	@Field
	private String sequence;

	@Field("release_date")
	private Date releaseDate;

	@Field
	private int version;

	public String getGenomeId() {
		return genomeId;
	}

	public void setGenomeId(String genomeId) {
		this.genomeId = genomeId;
	}

	public String getGenomeName() {
		return genomeName;
	}

	public void setGenomeName(String genomeName) {
		this.genomeName = genomeName;
	}

	public int getTaxonId() {
		return taxonId;
	}

	public void setTaxonId(int taxonId) {
		this.taxonId = taxonId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAccession() {
		return accession;
	}

	public void setAccession(String accession) {
		this.accession = accession;
	}

	public int getGi() {
		return gi;
	}

	public void setGi(int gi) {
		this.gi = gi;
	}

	public String getSequenceType() {
		return sequenceType;
	}

	public void setSequenceType(String sequenceType) {
		this.sequenceType = sequenceType;
	}

	public String getTopology() {
		return topology;
	}

	public void setTopology(String topology) {
		this.topology = topology;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getChromosome() {
		return chromosome;
	}

	public void setChromosome(String chromosome) {
		this.chromosome = chromosome;
	}

	public String getPlasmid() {
		return plasmid;
	}

	public void setPlasmid(String plasmid) {
		this.plasmid = plasmid;
	}

	public float getGcContent() {
		return gcContent;
	}

	public void setGcContent(float gcContent) {
		this.gcContent = gcContent;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
}
