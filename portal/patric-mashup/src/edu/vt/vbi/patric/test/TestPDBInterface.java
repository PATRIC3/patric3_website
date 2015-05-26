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

import edu.vt.vbi.patric.mashup.PDBInterface;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class TestPDBInterface {

	private static final Logger LOGGER = LoggerFactory.getLogger(PDBInterface.class);

	private static PDBInterface pdbInterface;

	@BeforeClass
	public static void setUp() {
		pdbInterface = new PDBInterface();
	}

	@Test
	public void testLigands() {
		try {
			List<Map<String, String>> result = pdbInterface.getLigands("4hhb");
			assertNotNull(result);
			assertTrue(result.size() > 0);
		}
		catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
		}
	}

	@Test
	public void testDescription() {
		try {
			Map<String, String> result = pdbInterface.getDescription("3GOA");

			assertNotNull(result);
			assertEquals("Crystal structure of the Salmonella typhimurium FadA 3-ketoacyl-CoA thiolase", result.get("title"));
		}
		catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
		}

	}

	@Test
	public void testGOTerms() {
		try {
			List<Map<String, String>> result = pdbInterface.getGOTerms("4hhb");

			assertNotNull(result);
			assertTrue(result.size() > 0);
		}
		catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
		}
	}

	@Test
	public void testSequenceCluster() {
		try {
			List<Map<String, String>> result = pdbInterface.getSequenceCluster("4hhb.A", 40);

			assertNotNull(result);
			assertTrue(result.size() > 0);
		}
		catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
		}
	}

	@Test
	public void testAnnotations() {
		try {
			List<Map<String, String>> result = pdbInterface.getAnnotations("1A8R.B");

			assertNotNull(result);
			// assertTrue(result.size() > 0);
		}
		catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
		}
	}

	@Test
	public void testPolymers() {
		try {
			List<String> result = pdbInterface.getPolymers("3op9");

			assertNotNull(result);
			assertTrue(result.size() > 0);
		}
		catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
		}
	}
}
