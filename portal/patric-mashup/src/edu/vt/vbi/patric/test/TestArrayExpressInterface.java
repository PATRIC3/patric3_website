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

import edu.vt.vbi.patric.mashup.ArrayExpressInterface;

import static org.junit.Assert.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestArrayExpressInterface {

	private static final Logger LOGGER = LoggerFactory.getLogger(ArrayExpressInterface.class);

	@Test
	public void testGetResult() {
		ArrayExpressInterface aei = new ArrayExpressInterface();

		try {
			// test case: Staphylococcus
			JSONObject result = aei.getResults("Staphylococcus", "");

			assertTrue((Boolean) result.get("hasData"));
			assertTrue(((JSONArray) result.get("results")).size() > 0);

			// test case: M.tb
			result = aei.getResults("Mycobacterium tuberculosis", "");

			assertTrue((Boolean) result.get("hasData"));
			assertTrue(((JSONArray) result.get("results")).size() > 0);
		}
		catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
		}
	}
}
