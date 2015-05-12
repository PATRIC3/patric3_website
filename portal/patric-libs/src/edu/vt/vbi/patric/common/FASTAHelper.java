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
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class FASTAHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(FASTAHelper.class);

	private static ObjectReader jsonReader = (new ObjectMapper()).reader(Map.class);

	public static String getFASTASequence(List<String> featureIds, String type) throws IOException {
		StringBuilder fasta = new StringBuilder();

		SolrQuery query = new SolrQuery("feature_id:(" + StringUtils.join(featureIds, " OR ") + ")");
		query.setFields("feature_id,seed_id,alt_locus_tag,refseq_locus_tag,annotation,gi,product,genome_id,genome_name,na_sequence,aa_sequence");
		query.setRows(featureIds.size());

		DataApiHandler dataApi = new DataApiHandler();
		String apiResponse = dataApi.solrQuery(SolrCore.TAXONOMY, query);
		Map resp = jsonReader.readValue(apiResponse);
		Map respBody = (Map) resp.get("response");

		List<GenomeFeature> features = dataApi.bindDocuments((List<Map>) respBody.get("docs"), GenomeFeature.class);

		for (GenomeFeature feature: features) {

			if (type.equals("dna") || type.equals("both")) {
				fasta.append(">");
				if (feature.getAnnotation().equals("PATRIC")) {
					if (feature.hasSeedId()) {
						fasta.append(feature.getSeedId()).append("|");
					}
				} else if (feature.getAnnotation().equals("RefSeq")) {
					if (feature.getGi() > 0) {
						fasta.append("gi|").append(feature.getGi()).append("|");
					}
				}

				if (feature.hasRefseqLocusTag()) {
					fasta.append(feature.getRefseqLocusTag()).append("|");
				}
				if (feature.hasAltLocusTag()) {
					fasta.append(feature.getAltLocusTag()).append("|");
				}
				if (feature.hasProduct()) {
					fasta.append("   ").append(feature.getProduct());
				}
				fasta.append("   [").append(feature.getGenomeName()).append(" | ").append(feature.getGenomeId()).append("]");

				fasta.append("\n");
				if (feature.hasNaSequence()) {
					fasta.append(StringHelper.chunk_split(feature.getNaSequence(), 60, "\n"));
				}
			}

			if (type.equals("protein") || type.equals("both")) {
				fasta.append(">");
				if (feature.getAnnotation().equals("PATRIC")) {
					if (feature.hasSeedId()) {
						fasta.append(feature.getSeedId()).append("|");
					}
				} else if (feature.getAnnotation().equals("RefSeq")) {
					if (feature.getGi() > 0) {
						fasta.append("gi|").append(feature.getGi()).append("|");
					}
				}

				if (feature.hasRefseqLocusTag()) {
					fasta.append(feature.getRefseqLocusTag()).append("|");
				}
				if (feature.hasAltLocusTag()) {
					fasta.append(feature.getAltLocusTag()).append("|");
				}
				if (feature.hasProduct()) {
					fasta.append("   ").append(feature.getProduct());
				}
				fasta.append("   [").append(feature.getGenomeName()).append(" | ").append(feature.getGenomeId()).append("]");

				fasta.append("\n");
				if (feature.hasAaSequence()) {
					fasta.append(StringHelper.chunk_split(feature.getAaSequence(), 60, "\n"));
				}
			}
		}

		return fasta.toString();
	}
}
