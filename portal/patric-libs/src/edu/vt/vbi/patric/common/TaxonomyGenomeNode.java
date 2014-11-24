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

package edu.vt.vbi.patric.common;

import edu.vt.vbi.patric.beans.Genome;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.LinkedHashSet;
import java.util.Set;

public class TaxonomyGenomeNode {

	private String id;

	private int taxonId;

	private String parentId;

	private String name;

	private Set<TaxonomyGenomeNode> children;

	public TaxonomyGenomeNode() {
		this.children = new LinkedHashSet<>();
	}

	public TaxonomyGenomeNode(Genome genome) {
		this.id = genome.getId();
		this.taxonId = genome.getTaxonId();
		this.name = genome.getGenomeName();
		this.children = new LinkedHashSet<>();
	}

	public String toString() {
		return this.getJSONObject().toJSONString();
	}

	public JSONObject getJSONObject() {
		JSONObject json = new JSONObject();

		json.put("id", getId());
		json.put("genome_id", getId());

		if (parentId != null) {
			json.put("parentId", getParentId());
		}
		if (taxonId > 0) {
			json.put("taxon_id", getTaxonId());
		}
		if (name != null) {
			json.put("name", getName());
		}
		if (hasChildren()) {
			json.put("children", getChildrenJSON());
		}
		else {
			json.put("leaf", true);
		}

		return json;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TaxonomyGenomeNode)) {
			return false;
		}

		final TaxonomyGenomeNode target = (TaxonomyGenomeNode) obj;
		return (this.getTaxonId() == target.getTaxonId());
	}

	public TaxonomyGenomeNode child(TaxonomyGenomeNode target) {
		for (TaxonomyGenomeNode child : children) {
			if (child.equals(target)) {
				return child;
			}
		}
		this.addChild(target);
		return target;
	}

	public JSONArray getChildrenJSON() {
		JSONArray json = new JSONArray();

		for (TaxonomyGenomeNode child : children) {
			json.add(child.getJSONObject());
		}

		return json;
	}

	public boolean hasChildren() {
		return (children != null && children.size() > 0);
	}

	public void addChild(TaxonomyGenomeNode child) {
		children.add(child);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getTaxonId() {
		return taxonId;
	}

	public void setTaxonId(int taxonId) {
		this.taxonId = taxonId;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<TaxonomyGenomeNode> getChildren() {
		return children;
	}

	public void setChildren(Set<TaxonomyGenomeNode> children) {
		this.children = children;
	}
}
