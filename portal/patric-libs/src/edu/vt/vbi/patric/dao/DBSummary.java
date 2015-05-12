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
package edu.vt.vbi.patric.dao;

import org.apache.commons.io.IOUtils;
import org.hibernate.*;
import org.hibernate.lob.SerializableClob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * <p>
 * An interface class for database queries. DBSummary includes queries that are used for patric-overview, patric-jbrowse, patric-phylogeny, and
 * patric-common. This class needs to be initialized (set SessionFactory) prior to use.
 * </p>
 * 
 * @author Harry Yoo (hyun@vbi.vt.edu)
 */
public class DBSummary {
	protected static SessionFactory factory;

	public static void setSessionFactory(SessionFactory sf) {
		factory = sf;
	}

	public static SessionFactory getSessionFactory() {
		return factory;
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(DBSummary.class);


	/**
	 * Return sub-SQL based on taxon_id to build a hierarchical query for a given taxon.
	 * 
	 * @param id ncbi_taxon_id
	 * @return sub-SQL string
	 */
	public static String getTaxonIdsInTaxonSQL(String id) {
		return "select ncbi_tax_id from sres.taxon connect by prior taxon_id = parent_id start with ncbi_tax_id = " + id;
	}


	/**
	 * Retrieves RNA detail info. This query on app.dnafeature and dots.nafeaturecomment tables.
	 * 
	 * @param id na_feature_id
	 * @return RNA info (na_feature_id, gene, label, anticodon, product, comment_string)
	 */
	public ResultType getRNAInfo(String id) {
		String sql = "select nf.na_feature_id, nf.gene, nf.label, nf.anticodon, nf.product, nfc.comment_string "
				+ "	from app.dnafeature nf, dots.nafeaturecomment nfc where nf.na_feature_id = ? "
				+ "		and nf.na_feature_id = nfc.na_feature_id(+)";

		Session session = factory.getCurrentSession();
		session.beginTransaction();
		SQLQuery q = session.createSQLQuery(sql);
		q.setString(0, id);

		session.getTransaction().commit();

		ResultType result = new ResultType();
		for (Object aRset : q.list()) {
			Object[] obj = (Object[]) aRset;

			result.put("na_feature_id", obj[0]);
			result.put("gene", obj[1]);
			result.put("label", obj[2]);
			result.put("anticodon", obj[3]);
			result.put("product", obj[4]);

			try {
				SerializableClob clobComment = (SerializableClob) obj[5];
				String strComment = IOUtils.toString(clobComment.getAsciiStream(), "UTF-8");
				result.put("comment_string", strComment);
			}
			catch (NullPointerException ex) {
				// this can be null
			}
			catch (Exception ex) {
				LOGGER.error("Problem in retrieving comments for RNA: {}", ex.getMessage(), ex);
			}
		}
		return result;
	}


	/**
	 * Identifies species that match PRIDE database for a given taxon. This is used for Experiment data API call.
	 * 
	 * @param id ncbi_tax_id
	 * @return list of species name
	 */
	public String getPRIDESpecies(String id) {
		String sql = "select pr.species, pr.ncbi_tax_id from app.pride pr, (" + getTaxonIdsInTaxonSQL(":ncbi_taxon_id") + ") tx "
				+ "	where pr.ncbi_tax_id = tx.ncbi_tax_id ";

		Session session = factory.getCurrentSession();
		session.beginTransaction();
		SQLQuery q = session.createSQLQuery(sql);
		q.setString("ncbi_taxon_id", id);

		List<?> rset = q.list();
		session.getTransaction().commit();

		StringBuilder results = new StringBuilder();
		Object[] obj = null;
		for (Object aRset : rset) {
			obj = (Object[]) aRset;
			if (results.length() > 0) {
				results.append("," + obj[0].toString());
			}
			else {
				results.append(obj[0].toString());
			}
		}

		return results.toString();
	}


	/**
	 * Finds taxonomy rank "Genus" for a given taxon node.
	 * 
	 * @param refseq_locus_tag RefSeq Locus Tag
	 * @return comments
	 */
	public List<Map<String, Object>> getTBAnnotation(String refseq_locus_tag) {
		String sql = "select distinct locus_tag, property, value, evidence_code, comments, source" + "	from app.tbcap_annotation "
				+ "	where locus_tag = :refseq_locus_tag and property != 'Interaction'" + "	order by property asc, evidence_code asc ";

		Session session = factory.getCurrentSession();
		session.beginTransaction();
		SQLQuery q = session.createSQLQuery(sql);
		q.setString("refseq_locus_tag", refseq_locus_tag);

		q.addScalar("locus_tag", Hibernate.STRING).addScalar("property", Hibernate.STRING).addScalar("value", Hibernate.STRING);
		q.addScalar("evidence_code", Hibernate.STRING).addScalar("comments", Hibernate.STRING).addScalar("source", Hibernate.STRING);

		List<Object[]> rset = q.list();

		List<Map<String, Object>> results = new ArrayList<>();
		for (Object[] obj: rset) {

			Map<String, Object> row = new HashMap<>();
			row.put("locus", obj[0]);
			row.put("property", obj[1]);
			row.put("value", obj[2]);
			row.put("evidencecode", obj[3]);
			row.put("comment", obj[4]);
			row.put("source", obj[5]);

			results.add(row);
		}

		// get Interactions
		sql = "select distinct locus_tag, property, value, evidence_code, comments, source" + "	from app.tbcap_annotation "
				+ "	where locus_tag = :refseq_locus_tag and property = 'Interaction' " + "	order by value asc, evidence_code asc ";

		q = session.createSQLQuery(sql);
		q.setString("refseq_locus_tag", refseq_locus_tag);

		q.addScalar("locus_tag", Hibernate.STRING).addScalar("property", Hibernate.STRING).addScalar("value", Hibernate.STRING);
		q.addScalar("evidence_code", Hibernate.STRING).addScalar("comments", Hibernate.STRING).addScalar("source", Hibernate.STRING);

		rset = q.list();

		for (Object[] obj: rset) {

			Map<String, Object> row = new HashMap<>();
			row.put("locus", obj[0]);
			row.put("property", obj[1]);
			row.put("value", obj[2]);
			row.put("evidencecode", obj[3]);
			row.put("comment", obj[4]);
			row.put("source", obj[5]);

			results.add(row);
		}

		return results;
	}

}
