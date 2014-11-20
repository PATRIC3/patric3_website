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

package edu.vt.vbi.patric.tree;

import edu.vt.vbi.patric.beans.Taxonomy;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by hyun on 11/19/14.
 */
public class TaxonomyTreeNode {

	private int taxonId;

	private int parentId;

	private String rank;

	private String name;

	private int nodeCount;

	private Set<TaxonomyTreeNode> children;

	public TaxonomyTreeNode() {
		this.children = new LinkedHashSet<>();
	}

	public TaxonomyTreeNode(Taxonomy taxon) {
		this.taxonId = taxon.getId();
		this.rank = taxon.getTaxonRank();
		this.name = taxon.getTaxonName();
		this.nodeCount = taxon.getGenomeCount();
		this.children = new LinkedHashSet<>();
	}

	public String toString() {
		return this.getJSONObject().toJSONString();
	}

	public JSONObject getJSONObject() {
		JSONObject json = new JSONObject();

		json.put("id", getTaxonId());
		if (parentId > 0) {
			json.put("parentId", getParentId());
		}
		if (rank != null) {
			json.put("rank", getRank());
		}
		if (nodeCount > 0) {
			json.put("node_count", getNodeCount());
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
		if (!(obj instanceof TaxonomyTreeNode)) return false;

		final TaxonomyTreeNode target = (TaxonomyTreeNode) obj;
		return (this.getTaxonId() == target.getTaxonId());
	}

	public TaxonomyTreeNode getFirstChild() {
		TaxonomyTreeNode firstNode = null;
		for (TaxonomyTreeNode child: children) {
			firstNode = child; break;
		}

		return firstNode;
	}

	public TaxonomyTreeNode child(TaxonomyTreeNode target) {
		for (TaxonomyTreeNode child: children) {
			if (child.equals(target)) {
				return child;
			}
		}
		this.addChild(target);
		return target;
	}

	public JSONArray getChildrenJSON() {
		JSONArray json = new JSONArray();

		for (TaxonomyTreeNode child: children) {
			json.add(child.getJSONObject());
		}

		return json;
	}

	public boolean hasChildren() {
		return (children != null && children.size()>0);
	}

	public void addChild(TaxonomyTreeNode child) {
		children.add(child);
	}

	public int getTaxonId() {
		return taxonId;
	}

	public void setTaxonId(int taxonId) {
		this.taxonId = taxonId;
	}

	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	public String getRank() {
		return rank;
	}

	public void setRank(String rank) {
		this.rank = rank;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getNodeCount() {
		return nodeCount;
	}

	public void setNodeCount(int nodeCount) {
		this.nodeCount = nodeCount;
	}

	public Set<TaxonomyTreeNode> getChildren() {
		return children;
	}

	public void setChildren(Set<TaxonomyTreeNode> children) {
		this.children = children;
	}
}
