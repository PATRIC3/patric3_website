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
package edu.vt.vbi.patric.test;

import edu.vt.vbi.patric.mashup.PDBInterface;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class TestPDBInterface extends TestCase {

	private static final Logger LOGGER = LoggerFactory.getLogger(PDBInterface.class);

	protected boolean mode = false;

	public static void main(String[] args) {
		junit.textui.TestRunner.run(TestPDBInterface.class);
	}

	public void testGetResult() {
		if (mode) {
			PDBInterface api = new PDBInterface();
			List<Map<String, String>> result;
			Map<String,String> resultMap;
			List<String> resultList;

			try {
				result = api.getLigands("4hhb");
				LOGGER.debug("getLigands(\"4hhb\"): {}", result);

				resultMap = api.getDescription("3GOA");
				LOGGER.debug("getDescription(\"3GOA\"): {}", resultMap);

				result = api.getGOTerms("4hhb");
				LOGGER.debug("getGOTerms(\"4hhb\"): {}", result);

				result = api.getSequenceCluster("4hhb.A", 40);
				LOGGER.debug("getSequenceCluster(\"4hhb.A\", 40): {}", result);

				result = api.getAnnotations("1A8R.B");
				LOGGER.debug("getAnnotations(\"1A8R.B\"): {}", result);

				resultList = api.getPolymers("3op9");
				LOGGER.debug("getPolymers(\"3op9\"): {}", resultList);
			}
			catch (Exception ex) {
				LOGGER.error(ex.getMessage(), ex);
			}
		}
	}
}
