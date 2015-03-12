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
package edu.vt.vbi.patric.mashup;

import edu.vt.vbi.patric.mashup.xmlHandler.ArrayExpressHandler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParserFactory;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class ArrayExpressInterface {

	private static final Logger LOGGER = LoggerFactory.getLogger(ArrayExpressInterface.class);

	protected String baseURL = "http://www.ebi.ac.uk/microarray-as/ae/xml/experiments";

	private XMLReader xmlReader = null;

	private ArrayExpressHandler handler = null;

	public ArrayExpressInterface() {
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			xmlReader = spf.newSAXParser().getXMLReader();
			handler = new ArrayExpressHandler();
			xmlReader.setContentHandler(handler);
		}
		catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
		}
	}

	@SuppressWarnings("unchecked")
	public JSONObject getResults(String keyword, String species) throws java.rmi.RemoteException {
		JSONObject result = new JSONObject();
		result.put("hasData", false);
		try {
			String params = "";
			if (!keyword.equals("")) {
				params = "?keywords=" + URLEncoder.encode(keyword, "UTF-8");
			}
			else if (!species.equals("")) {
				params = "?species=" + URLEncoder.encode(species, "UTF-8");
			}
			URL url = new URL(baseURL + params);
			URLConnection c = url.openConnection();
			c.setConnectTimeout(EutilInterface.TIMEOUT_CONN);
			c.setReadTimeout(EutilInterface.TIMEOUT_READ);

			LOGGER.debug(url.toString());

			xmlReader.parse(new InputSource(c.getInputStream()));
			JSONArray subList = handler.getParsedJSON();

			result.put("results", subList);
			result.put("total", subList.size());
			result.put("hasData", true);
		}
		catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
		return result;
	}
}
