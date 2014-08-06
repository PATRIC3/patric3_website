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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SQLHelper {
	/*
	 * @ called by DBSearch
	 */
	public static List<List<String>> splitIDStringtoArray(String ids, String delimiter) {
		List<String> lstIDS = Arrays.asList(ids.split(delimiter));

		List<List<String>> result = new ArrayList<List<String>>();
		int countIds = lstIDS.size();
		int countGroups = (int) java.lang.Math.floor(countIds / 333);
		int i = 1;
		for (i = 1; i <= countGroups; i++) {
			List<String> grp = new ArrayList<String>(lstIDS.subList((i - 1) * 333, i * 333));
			result.add(grp);
		}
		List<String> grp = new ArrayList<String>(lstIDS.subList((i - 1) * 333, countIds));
		result.add(grp);

		return result;
	}
}
