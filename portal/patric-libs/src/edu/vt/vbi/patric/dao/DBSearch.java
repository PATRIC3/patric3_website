/**
 * ****************************************************************************
 * Copyright 2014 Virginia Polytechnic Institute and State University
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ****************************************************************************
 */
package edu.vt.vbi.patric.dao;

import org.hibernate.*;

import java.util.*;

/**
 * An interface class for database queries that is used for search tools.
 *
 * @author Harry Yoo (hyun@vbi.vt.edu)
 * @author Oral Dalay (orald@vbi.vt.edu)
 *
 */
public class DBSearch {
	protected static SessionFactory factory;

	public static void setSessionFactory(SessionFactory sf) {
		factory = sf;
	}

	public static SessionFactory getSessionFactory() {
		return factory;
	}

	protected final int SQL_TIMEOUT = 5 * 60;

	public ArrayList<ResultType> getTranscriptomicsIDSearchResult(Map<String, String> key, int start, int end) {
		String sql = "";

		sql = this.getIDSearchSQL(key, null, "shortversion");

		Session session = factory.getCurrentSession();
		session.beginTransaction();
		SQLQuery q = session.createSQLQuery(sql);

		if (end > 0) {
			q.setMaxResults(end);
		}
		q.setTimeout(240);

		ScrollableResults scr = q.scroll();
		ArrayList<ResultType> results = new ArrayList<ResultType>();
		Object[] obj = null;
		if (start > 1) {
			scr.setRowNumber(start - 1);
		}
		else {
			scr.beforeFirst();
		}

		for (int i = start; (end > 0 && i < end && scr.next() == true) || (end == -1 && scr.next() == true); i++) {
			obj = scr.get();
			ResultType row = new ResultType();

			row.put("na_feature_id", obj[0]);

			if (key.get("from").equalsIgnoreCase("RefSeq Locus Tag"))
				row.put("refseq_source_id", obj[1]);
			else if (key.get("from").equalsIgnoreCase("PATRIC Locus Tag")) {
				row.put("source_id", obj[1]);
				if (obj[2] == null) {
					obj[2] = "";
				}
				row.put("refseq_source_id", obj[2]);
			}

			results.add(row);
		}

		session.getTransaction().commit();

		return results;
	}

	private String getIDSearchSQL(Map<String, String> key, Map<String, String> sort, String where) {

		String sql = "";

		if (where.equals("tocount")) {
			sql += "select count(distinct " + key.get("field") + ") as cnt ";

		}
		else if (where.equals("count")) {
			sql += "select count(*) as cnt ";

		}
		else if (where.equals("shortversion")) {
			sql += "select nf.na_feature_id ";

		}
		else {
			sql += "select nf.genome_info_id, nf.genome_name, nf.accession, nf.na_feature_id, nf.na_sequence_id, "
					+ "			nf.name, nf.source_id as locus_tag, "
					+ "				decode(nf.algorithm,'Curation','Legacy BRC','RAST','PATRIC','RefSeq') as algorithm, "
					+ "				decode(nf.is_reversed,1,'-','+') as strand, nf.debug_field, "
					+ "			nf.start_min, nf.start_max, nf.end_min, nf.end_max, nf.na_length, "
					+ "			nf.product, nf.gene, nf.aa_length, nf.is_pseudo, nf.bound_moiety, " + "			nf.anticodon"
					+ /* ,nf.protein_id */", nf.pseed_id ";
		}

		if (where.equals("shortversion") || where.equals("function")) {

			if (key.get("to").equalsIgnoreCase("PATRIC ID") || key.get("to").equalsIgnoreCase("PATRIC Locus Tag")
					|| key.get("to").equalsIgnoreCase("PSEED ID")) {

				if (key.get("from").equalsIgnoreCase("UniProtKB-ID")) {
					sql += ", pum.uniprotkb_accession, pum.uniprot_id ";
				}
				else if (key.get("from").equalsIgnoreCase("RefSeq Locus Tag")) {
					sql += ", rm.refseq_source_id ";
				}
				else if (key.get("from").equalsIgnoreCase("RefSeq")) {
					sql += ", rm.protein_id ";
				}
				else if (key.get("from").equalsIgnoreCase("Gene ID")) {
					sql += ", rm.gene_id ";
				}
				else if (key.get("from").equalsIgnoreCase("GI")) {
					sql += ", rm.gi_number ";
				}
				else if (!key.get("from").equalsIgnoreCase("PATRIC ID") && !key.get("from").equalsIgnoreCase("PATRIC Locus Tag")
						&& !key.get("from").equalsIgnoreCase("PSEED ID")) {
					sql += ", im.id requested_data ";
				}
				else if (key.get("from").equalsIgnoreCase("PATRIC Locus Tag")) {
					sql += ", nf.source_id ";
					if (key.get("to").equalsIgnoreCase("PATRIC Locus Tag")) {
						sql += ", prm.refseq_source_id ";
					}
				}
			}
			else if (key.get("to").equalsIgnoreCase("RefSeq") || key.get("to").equalsIgnoreCase("RefSeq Locus Tag")
					|| key.get("to").equalsIgnoreCase("Gene ID") || key.get("to").equalsIgnoreCase("GI")) {
				if (key.get("to").equalsIgnoreCase("RefSeq Locus Tag"))
					sql += ", rm.refseq_source_id ";
				if (key.get("to").equalsIgnoreCase("RefSeq"))
					sql += ", rm.protein_id ";
				if (key.get("to").equalsIgnoreCase("Gene ID"))
					sql += ", rm.gene_id ";
				if (key.get("to").equalsIgnoreCase("GI"))
					sql += ", rm.gi_number ";
			}
			else {
				if (key.get("to").equalsIgnoreCase("UniProtKB-ID")) {
					sql += ", pum.uniprotkb_accession, pum.uniprot_id ";
				}
				else {
					sql += ", im.id requested_data ";
				}
			}
		}

		if ((key.get("to").equalsIgnoreCase("RefSeq") || key.get("to").equalsIgnoreCase("RefSeq Locus Tag")
				|| key.get("to").equalsIgnoreCase("Gene ID") || key.get("to").equalsIgnoreCase("GI"))
				|| (key.get("from").equalsIgnoreCase("RefSeq") || key.get("from").equalsIgnoreCase("Gene ID") || key.get("from").equalsIgnoreCase(
				"GI"))) {

			sql += " from app.dnafeature nf, app.patricrefseqmapping rm " + "	where "/* nf.name='CDS' */
					+ " nf.na_feature_id=rm.patric_na_feature_id ";

		}
		else if (key.get("to").equalsIgnoreCase("UniProtKB-ID") || key.get("from").equalsIgnoreCase("UniProtKB-ID")) {

			sql += " from app.dnafeature nf, app.patricuniprotmapping pum " + "	where "/* nf.name='CDS' */
					+ " nf.na_feature_id=pum.na_feature_id ";

		}
		else if ((key.get("to").equalsIgnoreCase("PATRIC ID") || key.get("to").equalsIgnoreCase("PATRIC Locus Tag") || key.get("to")
				.equalsIgnoreCase("PSEED ID"))
				|| (key.get("from").equalsIgnoreCase("PATRIC ID") || key.get("from").equalsIgnoreCase("PATRIC Locus Tag") || key.get("from")
				.equalsIgnoreCase("PSEED ID"))) {

			if (key.get("from").equalsIgnoreCase("UniProtKB-ID")) {

				sql += " from app.dnafeature nf, app.patricuniprotmapping pum " + "	where "/* nf.name='CDS' */
						+ " nf.na_feature_id=pum.na_feature_id ";

			}
			else if (key.get("from").equalsIgnoreCase("RefSeq") || key.get("from").equalsIgnoreCase("RefSeq Locus Tag")
					|| key.get("from").equalsIgnoreCase("Gene ID") || key.get("from").equalsIgnoreCase("GI")) {

				sql += " from app.dnafeature nf, app.patricrefseqmapping rm " + "	where "/* nf.name='CDS' */
						+ " nf.na_feature_id=rm.patric_na_feature_id ";

			}
			else if ((key.get("from").equalsIgnoreCase("PATRIC ID") || key.get("from").equalsIgnoreCase("PSEED ID") || key.get("from")
					.equalsIgnoreCase("PATRIC Locus Tag"))
					&& (key.get("to").equalsIgnoreCase("PATRIC ID") || key.get("to").equalsIgnoreCase("PATRIC Locus Tag") || key.get("to")
					.equalsIgnoreCase("PSEED ID"))) {

				sql += " from app.dnafeature nf ";

				if (key.get("to").equalsIgnoreCase("PATRIC Locus Tag")) {
					sql += ", app.patricrefseqmapping prm ";
				}

				sql += " where 1=1 ";// nf.name='CDS' ";

				if (key.get("to").equalsIgnoreCase("PATRIC Locus Tag")) {
					sql += " and nf.na_feature_id = prm.patric_na_feature_id (+) ";
				}

			}
			else {

				if (key.get("from").equalsIgnoreCase("PATRIC ID") || key.get("from").equalsIgnoreCase("PSEED ID")
						|| key.get("from").equalsIgnoreCase("PATRIC Locus Tag")) {
					sql += " from app.dnafeature nf, app.idmapping im, app.patricuniprotmapping pum " + "	where "/* nf.name='CDS' */
							+ "nf.na_feature_id = pum.na_feature_id " + "	and im.uniprotkb_accession = pum.uniprotkb_accession "
							+ "	and im.id_type = '" + key.get("to") + "'";
				}
				else {
					sql += " from app.dnafeature nf, app.idmapping im, app.patricuniprotmapping pum " + "	where "/* nf.name='CDS' */
							+ "nf.na_feature_id = pum.na_feature_id " + "	and im.uniprotkb_accession = pum.uniprotkb_accession "
							+ "	and im.id_type = '" + key.get("from") + "'";
				}
			}

		}
		else {

			sql += " from app.dnafeature nf, app.idmapping im, app.patricuniprotmapping pum " + "	where "/* nf.name='CDS' */
					+ " nf.na_feature_id=pum.na_feature_id " + "	and im.uniprotkb_accession = pum.uniprotkb_accession " + "	and im.id_type = '"
					+ key.get("to") + "'";

		}

		if (key.containsKey("keyword") && key.get("keyword") != null) {

			// parse keyword
			String[] tmp = key.get("keyword").split("[,\\s]+");
			String keywords = "";

			if (tmp.length > 500) {
				keywords += " ('" + tmp[0].trim() + "',";
				for (int i = 1; i < tmp.length; i++) {
					if (i % 500 == 0) {
						keywords = keywords.substring(0, keywords.length() - 1);
						keywords += ") or REPLACE_ME in ('" + tmp[i].trim() + "',";
					}
					else {
						keywords += "'" + tmp[i].trim() + "',";
					}
				}
				keywords = keywords.substring(0, keywords.length() - 1) + ")";
			}
			else {
				keywords += " ('" + tmp[0].trim() + "'";
				for (int i = 1; i < tmp.length; i++) {
					keywords += ",'" + tmp[i].trim() + "'";
				}
				keywords += ")";
			}

			if (key.get("from").equalsIgnoreCase("PATRIC ID")) {
				keywords = keywords.replaceAll("REPLACE_ME", "nf.na_feature_id");
				sql += "	and (nf.na_feature_id in " + keywords + ") ";
			}
			else if (key.get("from").equalsIgnoreCase("PATRIC Locus Tag")) {
				keywords = keywords.replaceAll("REPLACE_ME", "nf.source_id");
				sql += "	and (nf.source_id in " + keywords + ") ";
			}
			else if (key.get("from").equalsIgnoreCase("PSEED ID")) {
				keywords = keywords.replaceAll("REPLACE_ME", "nf.pseed_id");
				sql += "	and (nf.pseed_id in " + keywords + ") ";
			}
			else if (key.get("from").equalsIgnoreCase("RefSeq Locus Tag") || key.get("from").equalsIgnoreCase("RefSeq")
					|| key.get("from").equalsIgnoreCase("Gene ID") || key.get("from").equalsIgnoreCase("GI")) {
				if (key.get("from").equalsIgnoreCase("RefSeq Locus Tag")) {
					keywords = keywords.replaceAll("REPLACE_ME", "rm.refseq_source_id");
					sql += "and (rm.refseq_source_id in " + keywords + ") ";
				}
				else if (key.get("from").equalsIgnoreCase("RefSeq")) {
					keywords = keywords.replaceAll("REPLACE_ME", "rm.protein_id");
					sql += "and (rm.protein_id in " + keywords + ") ";
				}
				else if (key.get("from").equalsIgnoreCase("Gene ID")) {
					keywords = keywords.replaceAll("REPLACE_ME", "rm.gene_id");
					sql += "and (rm.gene_id in " + keywords + ") ";
				}
				else if (key.get("from").equalsIgnoreCase("GI")) {
					keywords = keywords.replaceAll("REPLACE_ME", "rm.gi_number");
					sql += "and (rm.gi_number in " + keywords + ") ";
				}
			}
			else if (key.get("from").equalsIgnoreCase("UniProtKB-ID")) {
				String keywords_1 = keywords.replaceAll("REPLACE_ME", "pum.uniprotkb_accession");
				String keywords_2 = keywords.replaceAll("REPLACE_ME", "pum.uniprot_id");
				sql += "	and (pum.uniprotkb_accession in " + keywords_1 + ") OR  (pum.uniprot_id in " + keywords_2 + ")";
			}
			else {
				keywords = keywords.replaceAll("REPLACE_ME", "im.id");
				sql += "	and (im.id in " + keywords + ")";
			}
		}

		if (!where.equals("count") && !where.equals("tocount") && !where.equals("shortversion")) {
			if (sort != null) {
				sql += " order by " + sort.get("field") + " " + sort.get("direction");
				if (sort.get("field").equals("genome_name")) {
					sql += " order by " + sort.get("field") + " " + sort.get("direction") + ", locus_tag ASC ";
				}
			}
		}

		return sql;
	}


	/**
	 * [what is this for?]
	 * @param cId
	 * @param cType
	 * @param input
	 * @param algorithm
	 * @return ?
	 */
	public ArrayList<ResultType> getGONaFeatureIdList(String cId, String cType, String input, String algorithm) {

		String sql = "	select	distinct(gs.na_feature_id) genes" + "	FROM app.gosummary gs WHERE 1=1 ";

		if (cType.equals("taxon")) {

			HashMap<String, String> key = new HashMap<String, String>();
			key.put("taxonId", cId);
			key.put("algorithm", algorithm);

			// sql += getGenomeListByTaxon(key.get("taxonId"),
			// key.get("algorithm"), 0, -1);

			sql += " AND gs.ncbi_tax_id in (" + DBSummary.getTaxonIdsInTaxonSQL(key.get("taxonId")) + ") ";
			sql += " AND (select count(*) from app.genomesummary gsu where gsu.genome_info_id = gs.genome_info_id and (gsu.complete='Complete' or gsu.complete ='WGS') ) > 0";

		}
		else if (cType.equals("genome")) {

			sql += " AND	gs.genome_info_id in ("
					+ cId
					+ ") AND ( select count(*) from app.genomesummary gsu where gsu.genome_info_id = gs.genome_info_id  and (gsu.complete='Complete' or gsu.complete ='WGS') ) > 0 ";
		}

		sql += "	AND gs.go_id in (" + input + ")" + "	AND gs.algorithm in (" + algorithm + ")";

		ArrayList<ResultType> results = new ArrayList<ResultType>();
		Object obj = null;

		Session session = factory.getCurrentSession();
		session.beginTransaction();
		SQLQuery q = session.createSQLQuery(sql).addScalar("genes", Hibernate.STRING);
		q.setCacheable(true);
		q.setTimeout(SQL_TIMEOUT);
		List<?> rset = q.list();
		session.getTransaction().commit();

		for (Iterator<?> iter = rset.iterator(); iter.hasNext(); ) {
			obj = iter.next();
			// HashMap<String,Object> row = new HashMap<String,Object>();
			ResultType row = new ResultType();
			row.put("genes", obj);

			results.add(row);
		}
		return results;

	}


	/**
	 * [what is this for?]
	 * @param cId
	 * @param cType
	 * @param input
	 * @param algorithm
	 * @return ?
	 */
	public ArrayList<ResultType> getECNaFeatureIdList(String cId, String cType, String input, String algorithm) {

		String sql = "	select	distinct(es.na_feature_id) genes" + "	FROM app.ecsummary es WHERE 1=1 ";

		if (cType.equals("taxon")) {

			HashMap<String, String> key = new HashMap<String, String>();
			key.put("taxonId", cId);
			key.put("algorithm", algorithm);

			sql += " AND es.ncbi_tax_id in (" + DBSummary.getTaxonIdsInTaxonSQL(key.get("taxonId")) + ") ";
			sql += " AND (select count(*) from app.genomesummary gsu where gsu.genome_info_id = es.genome_info_id and (gsu.complete='Complete' or gsu.complete ='WGS') ) > 0";

		}
		else if (cType.equals("genome")) {

			sql += " AND	es.genome_info_id in ("
					+ cId
					+ ") AND ( select count(*) from app.genomesummary gsu where gsu.genome_info_id = es.genome_info_id  and (gsu.complete='Complete' or gsu.complete ='WGS') ) > 0";
		}

		sql += "	AND es.ec_number in (" + input + ")" + "	AND es.algorithm in (" + algorithm + ")";

		ArrayList<ResultType> results = new ArrayList<ResultType>();
		Object obj = null;

		Session session = factory.getCurrentSession();
		session.beginTransaction();
		SQLQuery q = session.createSQLQuery(sql).addScalar("genes", Hibernate.STRING);
		q.setTimeout(SQL_TIMEOUT);
		List<?> rset = q.list();
		session.getTransaction().commit();

		for (Iterator<?> iter = rset.iterator(); iter.hasNext(); ) {
			obj = iter.next();

			ResultType row = new ResultType();

			row.put("genes", obj);

			results.add(row);
		}
		return results;

	}

}
