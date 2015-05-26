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

import edu.vt.vbi.patric.mashup.PSICQUICInterface;

import static org.junit.Assert.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestPSICQUICInterface {

	private static final Logger LOGGER = LoggerFactory.getLogger(PSICQUICInterface.class);

	private static PSICQUICInterface psicquicInterface;

	@BeforeClass
	public static void setUp() {
		psicquicInterface = new PSICQUICInterface();
	}

	@Test
	public void testGetCounts() {
		try {
			String count = psicquicInterface.getCounts("intact", "species:2");
			assertEquals("55168", count);
		}
		catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
		}
	}

	@Test
	public void testGetResults() {
		try {
			JSONObject response = psicquicInterface.getResults("intact", "species:63363", 0, 5);

			int total = Integer.parseInt((String) response.get("total"));
			JSONArray results = (JSONArray) response.get("results");

			if (total > 5) {
				assertTrue(results.size() == 5);
			}
			else {
				assertTrue(results.size() == total);
			}
		}
		catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
		}
	}
}
