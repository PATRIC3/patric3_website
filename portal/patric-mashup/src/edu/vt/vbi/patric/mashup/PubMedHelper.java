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
package edu.vt.vbi.patric.mashup;

import edu.vt.vbi.patric.beans.Genome;
import edu.vt.vbi.patric.beans.GenomeFeature;
import edu.vt.vbi.patric.beans.Taxonomy;
import edu.vt.vbi.patric.common.DataApiHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

public class PubMedHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(PubMedHelper.class);

	public static String getTitleString(Map<String, String> key) {

		DataApiHandler dataApi = new DataApiHandler();

		String title = null;

		if (key.get("context").equalsIgnoreCase("taxon")) {

			Taxonomy taxonomy = dataApi.getTaxonomy(Integer.parseInt(key.get("taxon_id")));
			title = taxonomy.getTaxonName();
		}
		else if (key.get("context").equalsIgnoreCase("genome")) {

			Genome genome = dataApi.getGenome(key.get("genome_id"));
			title = genome.getGenomeName();
		}
		else if (key.get("context").equalsIgnoreCase("feature")) {

			GenomeFeature feature = dataApi.getFeature(key.get("feature_id"));

			String qScope = key.get("scope");

			if (qScope != null && qScope.equals("g")) {
				title = feature.getGenomeName();
			}
			else {
				// default, feature level

				int offset1 = feature.getGenomeName().indexOf(" ");
				int offset2 = feature.getGenomeName().indexOf(" ", offset1 + 1);

				String org;
				if (offset2 > 0) {
					org = feature.getGenomeName().substring(0, offset2);
				}
				else {
					org = feature.getGenomeName().substring(0, offset1);
				}

				title = "(\"" + org.toLowerCase() + "\") AND (\"" + feature.getAltLocusTag();

				if (feature.hasProduct()) {
					title += "\" OR \"" + feature.getProduct().toLowerCase() + "\"";
				}

				if (feature.hasGene()) {
					title += " OR \"" + feature.getGene() + "\"";
				}

				if (feature.hasRefseqLocusTag()) {
					title += " OR \"" + feature.getRefseqLocusTag() + "\"";
				}

				if (feature.hasProteinId()) {
					title += " OR \"" + feature.getProteinId() + "\"";
				}

				title += ")";
			}
			// end of Solr query
		}
		return title;
	}

	public static String getPubmedQueryString(Map<String, String> key) throws NullPointerException {
		String title = getTitleString(key);
		if (title == null || title.equals("")) {
			throw new NullPointerException("title is not defined");
		}

		// String pubmedquery = "\""+title+"\"[ALL]";
		String pubmedquery = title;

		// keyword configuration
		String _str_kw = "";
		String qKeyword = key.get("keyword");

		if (qKeyword != null && !qKeyword.equals("none")) {
			Map<String, List<String>> hashKeyword = PubMedHelper.getKeywordHash();
			List<String> listQuerykey = hashKeyword.get(qKeyword);
			if (listQuerykey != null) {
				for (String aQuerykey : listQuerykey) {
					_str_kw = _str_kw + " or \"" + aQuerykey + "\"[ALL]";
				}
				pubmedquery = pubmedquery + " AND (\"" + qKeyword + "\"[ALL]" + _str_kw + ")";
			}
		}

		// date
		String qDate = key.get("date");
		if (qDate != null && !qDate.equals("")) {
			switch (qDate) {
			case "w":
				pubmedquery = pubmedquery + " \"last 7 days\"[dp]";
				break;
			case "m":
				pubmedquery = pubmedquery + " \"last 1 months\"[dp]";
				break;
			case "y":
				pubmedquery = pubmedquery + " \"last 1 year\"[dp]";
				break;
			case "f":
				Date todayDate = new Date();
				SimpleDateFormat sdfToday = new SimpleDateFormat("yyyy/MM/dd");
				SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");
				String strToday = sdfToday.format(todayDate);
				String strYear = sdfYear.format(todayDate);
				int intNextYear = Integer.parseInt(strYear) + 1;
				pubmedquery = pubmedquery + " " + strToday + ":" + intNextYear + " [dp]";
				break;
			}
		}
		try {
			pubmedquery = URLEncoder.encode(pubmedquery, "UTF8");
		}
		catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
		}

		return pubmedquery;
	}

	public static Map<String, List<String>> getKeywordHash() {

		Map<String, List<String>> hash = new HashMap<>();

		List<String> keyword1 = Arrays.asList("drug", "vaccine", "theraputics", "diagnostics", "target");
		hash.put("Countermeasures", keyword1);

		List<String> keyword2 = Arrays.asList("mass spectrometry", "2D-gels", "protein-protein interaction");
		hash.put("Proteomics", keyword2);

		List<String> keyword3 = Arrays
				.asList("microarray", "transcriptome", "expression profiling", "real time PCR", "immune response", "response to infection",
						"host response", "pathogenesis", "virulence", "disease response");
		hash.put("Gene expression", keyword3);

		List<String> keyword4 = Arrays
				.asList("culture", "microscopy", "haemagglutination", "complement fixation", "ELISA", "EIA", "immune double diffusion",
						"immunoelectrophoresis", "latex agglutination", "western blot", "antibody", "Polymerase chain reaction", "PCR", "PCR primer",
						"western blot");
		hash.put("Diagnosis", keyword4);

		List<String> keyword6 = Arrays.asList("symptom", "syndrome", "prognosis");
		hash.put("Disease", keyword6);

		List<String> emptyList = new ArrayList<>();
		hash.put("Pathogenesis", emptyList);
		hash.put("Prevention", emptyList);
		hash.put("Host", emptyList);
		hash.put("Reservoir", emptyList);
		hash.put("Transmission", emptyList);
		hash.put("Genome", emptyList);
		hash.put("Taxonomy", emptyList);

		List<String> keyword14 = Arrays.asList("outbreak", "epidemic");
		hash.put("Epidemiology", keyword14);

		List<String> keyword15 = Arrays
				.asList("Microarray", "Expression array", "Gene expression", "Expression profil", "Genome variation profil", "RNA profil",
						"Tiling array", "ArrayCGH", "ChIP-chip", "SAGE", "RNA-Seq", "Protein microarray", "Protein array", "Mass spec",
						"Protein identification", "Peptide identification", "2D gel", "Proteomics", "Protein structure",
						"three-dimensional structure", "3D structure", "NMR", "X-ray diffraction");
		hash.put("Experiment Data", keyword15);

		return hash;
	}
}
