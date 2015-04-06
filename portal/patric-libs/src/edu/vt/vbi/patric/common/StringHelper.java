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
package edu.vt.vbi.patric.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(StringHelper.class);

	// used in patric-diseaseview/WebContent/jsp/get_graph_data.jsp
	public static String strip_html_tag(String html) {
		return html.replaceAll("\\<.*?>", "");
	}

	public static String chunk_split(String str, int length, String end) {
		StringBuilder sb = new StringBuilder();
		if (str.length() <= length) {
			sb.append(str);
			sb.append(end);
		}
		else {
			int steps = (int) java.lang.Math.floor(str.length() / length);
			String sub;
			for (int i = 0; i < steps; i++) {
				sub = str.substring(i * length, (i + 1) * length);
				sb.append(sub);
				sb.append(end);
			}
			if (str.length() > steps * length) {
				sub = str.substring(steps * length, str.length());
				sb.append(sub);
				sb.append(end);
			}
		}
		return sb.toString();
	}

	public static String parseSolrKeywordOperator(String keyword) {
		keyword = keyword.replaceAll("\\s+or\\s+|\\s+Or\\s+|\\s+oR\\s+", " OR ").replaceAll("( )+", " ");

		return keyword;
	}
}
