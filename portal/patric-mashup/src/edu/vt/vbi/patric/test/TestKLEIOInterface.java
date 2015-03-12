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

import edu.vt.vbi.patric.mashup.KLEIOInterface;
import junit.framework.TestCase;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestKLEIOInterface extends TestCase {

	private static final Logger LOGGER = LoggerFactory.getLogger(KLEIOInterface.class);

	protected boolean mode = false;

	public static void main(String[] args) {
		junit.textui.TestRunner.run(TestKLEIOInterface.class);

	}

	public void testGetResult() {
		if (mode) {
			KLEIOInterface api = new KLEIOInterface();

			try {
				JSONObject result = api.getDocumentList("expression of mRNA for MIP-1alpha", null, false, 0, 10);
				LOGGER.debug("{}", result);

				JSONArray resultArray = api.getFacetNames();
				LOGGER.debug("{}", resultArray);

				result = api.getFacets("CONTENT:expression of mRNA for MIP-1alpha");
				LOGGER.debug("{}", result);

				result = api.getDocument("15807277");
				LOGGER.debug("{}", result);
			}
			catch (Exception ex) {
				LOGGER.error(ex.getMessage(), ex);
			}
		}
	}
}
