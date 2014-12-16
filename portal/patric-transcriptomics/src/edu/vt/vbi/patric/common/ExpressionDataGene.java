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

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ExpressionDataGene {

	String refseq_locus_tag;

	JSONObject samples;

	String sample_order_binary;

	int up = 0;

	int down = 0;

	String feature_id;
	String p2_feature_id;

	private static final Logger LOGGER = LoggerFactory.getLogger(ExpressionDataGene.class);

	public ExpressionDataGene(JSONObject data) {
		samples = new JSONObject();
		this.refseq_locus_tag = (data.get("refseq_locus_tag") != null) ? data.get("refseq_locus_tag").toString() : data.get("exp_locus_tag")
				.toString();
		if (data.containsKey("feature_id")) {
			this.feature_id = data.get("feature_id").toString();
		}
		else if (data.containsKey("na_feature_id")) {
			this.p2_feature_id = data.get("na_feature_id").toString();
		}
	}

	@SuppressWarnings("unchecked")
	public void addSamplestoGene(JSONObject gene_data, Map<String, String> sample_data) {

		String sample = gene_data.get("pid").toString();
		String log_ratio = "", z_score = "";
		if (gene_data.get("z_score") != null) {
			z_score = gene_data.get("z_score").toString();
		}
		if (gene_data.get("log_ratio") != null) {
			log_ratio = gene_data.get("log_ratio").toString();
		}
		else {
			LOGGER.trace(gene_data.get("feature_id").toString());
		}

		if (!this.IsSamplePushed(sample)) {
			JSONObject a = new JSONObject();
			// a.put("id", sample);
			a.put("log_ratio", log_ratio);
			a.put("z_score", z_score);
			// a.put("description", sample_data.get(sample));

			this.samples.put(sample, a);

			if ((log_ratio != null && !log_ratio.equals("")) && Double.parseDouble(log_ratio) > Double.parseDouble("0")) {
				this.up++;
			}
			else if ((log_ratio != null && !log_ratio.equals("")) && Double.parseDouble(log_ratio) < Double.parseDouble("0")) {
				this.down++;
			}
		}
	}

	public boolean IsSamplePushed(String sample) {
		boolean flag = false;
		for (int i = 0; i < this.samples.size(); i++) {
			if (this.samples.get(sample) != null) {
				flag = true;
				break;
			}
		}
		return flag;
	}

	public void setSampleBinary(String samples) {

		String binary_order = "";

		for (String sample : samples.split(",")) {
			boolean flag = false;
			for (int j = 0; j < this.samples.size(); j++) {
				JSONObject a = (JSONObject) this.samples.get(sample);
				if (a != null && a.get("log_ratio") != null) {
					flag = true;
					break;
				}
			}
			if (flag) {
				binary_order += "1";
			}
			else {
				binary_order += "0";
			}
		}
		this.sample_order_binary = binary_order;
	}

	public int getSampleCounts() {

		int count = 0;
		for (int i = 0; i < this.sample_order_binary.length(); i++) {
			if (this.sample_order_binary.charAt(i) == '1') {
				count++;
			}
		}
		return count;
	}

	public String getRefSeqLocusTag() {
		return this.refseq_locus_tag;
	}

	public String getFeatureID() {
		return this.feature_id;
	}

	public boolean hasFeatureId() {
		return (this.feature_id != null);
	}

	public String getP2FeatureId() {
		return this.p2_feature_id;
	}

	public JSONObject getSamples() {
		return this.samples;
	}

	public String getSampleBinary() {
		return this.sample_order_binary;
	}
}