/*******************************************************************************
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
 ******************************************************************************/
package edu.vt.vbi.patric.test;

import edu.vt.vbi.patric.mashup.EutilInterface;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class TestEutilInterface {

	private static final Logger LOGGER = LoggerFactory.getLogger(EutilInterface.class);

	private static EutilInterface eUtilInterface;

	@BeforeClass
	public static void setUp() {
		eUtilInterface = new EutilInterface();
	}

	@Test
	public void testPubmedRequest() {
		try {
			JSONObject response = eUtilInterface
					.getResults("pubmed", "(mycobacterium+tuberculosis)+AND+(dnaN)", "&sort=pub+date", "&sort=pub+date&retmode=xml", 1, 5);

			JSONArray results = (JSONArray) response.get("results");

			assertNotNull(results);
			assertEquals(5, results.size());
		}
		catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
		}
	}

	@Test
	public void testGdsGsm() {
		try {
			JSONObject response = eUtilInterface.getResults("gds", "txid138[Organism:exp]+AND+gsm[ETYP]", "", "", 1, 20);

			JSONArray results = (JSONArray) response.get("results");

			assertNotNull(results);
			assertTrue(results.size() <= 20);
		}
		catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
		}
	}

	@Test
	public void testGdsGse() {
		try {
			JSONObject response = eUtilInterface.getResults("gds", "txid2093[Organism:exp]+AND+gse[ETYP]", "", "", 1, 20);

			JSONArray results = (JSONArray) response.get("results");

			assertNotNull(results);
			assertTrue(results.size() <= 20);
		}
		catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
		}
	}

	@Test
	public void testPeptidomeRequest() {
		try {
			JSONObject response = eUtilInterface.getResults("pepdome", "txid1763[Organism:exp]+AND+PSM[ETYP]", "", "", 1, 20);

			JSONArray results = (JSONArray) response.get("results");

			assertNotNull(results);
			assertTrue(results.size() <= 20);
		}
		catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
		}
	}

	@Test
	public void testStructureRequest() {
		try {
			JSONObject response = eUtilInterface.getResults("structure", "txid262[Organism:exp]", "", "", 1, 20);

			JSONArray results = (JSONArray) response.get("results");

			assertNotNull(results);
			assertTrue(results.size() <= 20);
		}
		catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
		}
	}
}