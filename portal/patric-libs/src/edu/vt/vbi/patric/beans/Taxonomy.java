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
import org.json.simple.JSONObject;

import java.util.List;

public class Taxonomy {
	@Field("taxon_id")
	private int id;

	@Field("taxon_name")
	private String taxonName;

	@Field("taxon_rank")
	private String taxonRank;

	@Field("genomes")
	private int genomeCount;

	@Field("lineage_ids")
	private List<Integer> lineageIds;

	@Field("lineage_names")
	private List<String> lineageNames;

	@Field("lineage_ranks")
	private List<String> lineageRanks;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTaxonName() {
		return taxonName;
	}

	public void setTaxonName(String taxonName) {
		this.taxonName = taxonName;
	}

	public String getTaxonRank() {
		return taxonRank;
	}

	public void setTaxonRank(String taxonRank) {
		this.taxonRank = taxonRank;
	}

	public int getGenomeCount() {
		return genomeCount;
	}

	public void setGenomeCount(int genomeCount) {
		this.genomeCount = genomeCount;
	}

	public List<Integer> getLineageIds() {
		return lineageIds;
	}

	public void setLineageIds(List<Integer> lineageIds) {
		this.lineageIds = lineageIds;
	}

	public List<String> getLineageNames() {
		return lineageNames;
	}

	public void setLineageNames(List<String> lineageNames) {
		this.lineageNames = lineageNames;
	}

	public List<String> getLineageRanks() {
		return lineageRanks;
	}

	public void setLineageRanks(List<String> lineageRanks) {
		this.lineageRanks = lineageRanks;
	}

	public JSONObject toJSONObject() {
		JSONObject json = new JSONObject();

		json.put("taxon_id", getId());
		json.put("taxon_name", getTaxonName());
		json.put("taxon_rank", getTaxonRank());
		json.put("genomes", getGenomeCount());
		// lineage_ids, lineage_names, lineage_ranks
		return json;
	}
}
