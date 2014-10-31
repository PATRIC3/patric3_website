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

import edu.vt.vbi.patric.beans.GenomeFeature;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.List;

public class FASTAHelper {

	static SolrInterface solr = new SolrInterface();

	private static final Logger LOGGER = LoggerFactory.getLogger(FASTAHelper.class);

	public static String getFASTASequence(List<String> featureIds, String type) {
		StringBuilder fasta = new StringBuilder();

		try {
			SolrQuery query = new SolrQuery("feature_id:(" + StringUtils.join(featureIds, " OR ") + ")");
			query.setFields("feature_id,seed_id,product,genome_name,na_sequence,aa_sequence");
			query.setRows(featureIds.size());

			QueryResponse qr = solr.getSolrServer(SolrCore.FEATURE).query(query);
			List<GenomeFeature> features = qr.getBeans(GenomeFeature.class);

			for (GenomeFeature feature: features) {

				if (type.equals("dna") || type.equals("both")) {
					fasta.append(">fid|").append(feature.getId());
					if (feature.hasSeedId()) {
						fasta.append("|locus|").append(feature.getSeedId());
					}
					if (feature.hasProduct()) {
						fasta.append("|   ").append(feature.getProduct());
					}
					fasta.append("   [").append(feature.getGenomeName()).append("]");

					fasta.append("\n");
					if (feature.hasNaSequence()) {
						fasta.append(StringHelper.chunk_split(feature.getNaSequence(), 60, "\n"));
					}
				}

				if (type.equals("both")) {
					fasta.append("\n");
				}

				if (type.equals("protein") || type.equals("both")) {
					fasta.append(">fid|").append(feature.getId());
					if (feature.hasSeedId()) {
						fasta.append("|locus|").append(feature.getSeedId());
					}
					if (feature.hasProduct()) {
						fasta.append("|   ").append(feature.getProduct());
					}
					fasta.append("   [").append(feature.getGenomeName()).append("]");

					fasta.append("\n");
					if (feature.hasAaSequence()) {
						fasta.append(StringHelper.chunk_split(feature.getAaSequence(), 60, "\n"));
					}
				}
				fasta.append("\n");
			}

		} catch (MalformedURLException | SolrServerException e) {
			LOGGER.error(e.getMessage(), e);
		}

		return fasta.toString();
	}
}
