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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.vt.vbi.patric.common.PolyomicHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class ExpressionDataCollection {

	private List<String> expressionFileName;

	private List<String> sampleFileName;

	private List<String> mappingFileName;

	private JSONArray sample, expression;

	private InputStream inp;

	public final static String CONTENT_EXPRESSION = "expression";

	public final static String CONTENT_SAMPLE = "sample";

	public final static String CONTENT_MAPPING = "mapping";

	private static final Logger LOGGER = LoggerFactory.getLogger(ExpressionDataCollection.class);

	public ExpressionDataCollection(String id, String token) {

		PolyomicHandler polyomic = new PolyomicHandler();
		polyomic.setAuthenticationToken(token);
		String[] collectionIds = id.split(",");

		sample = new JSONArray();
		expression = new JSONArray();

		JSONObject collection;
		expressionFileName = new ArrayList<>();
		sampleFileName = new ArrayList<>();
		mappingFileName = new ArrayList<>();

		for (String collectionId : collectionIds) {
			collection = polyomic.getCollection(collectionId, null);

			expressionFileName.add(polyomic.findJSONUrl(collection, CONTENT_EXPRESSION));
			sampleFileName.add(polyomic.findJSONUrl(collection, CONTENT_SAMPLE));
			mappingFileName.add(polyomic.findJSONUrl(collection, CONTENT_MAPPING));
		}
	}

	public InputStream getInputStreamReader(String path) {

		try {
			URL url = new URL(path);
			URLConnection connection = url.openConnection();
			inp = connection.getInputStream();
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return inp;
	}

	public void read(String input) throws FileNotFoundException {

		List<String> temp = null;

		if (input.equals(CONTENT_SAMPLE)) {
			temp = sampleFileName;
		}
		else if (input.equals(CONTENT_EXPRESSION)) {
			temp = expressionFileName;
		}

		InputStreamReader stream;
		BufferedReader reader;
		String strLine;

		assert temp != null;
		for (String aTemp : temp) {

			inp = getInputStreamReader(aTemp);
			stream = new InputStreamReader(inp);
			reader = new BufferedReader(stream);

			try {
				while ((strLine = reader.readLine()) != null) {
					try {
						JSONObject tmp = (JSONObject) new JSONParser().parse(strLine);
						AddToCurrentSet((JSONArray) tmp.get(input), input);
					}
					catch (ParseException e) {
						LOGGER.error(e.getMessage(), e);
					}
				}
				inp.close();
			}
			catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

	public void filter(String item, String input) throws FileNotFoundException {

		JSONArray temp = null;
		String[] items = item.split(",");

		if (input.equals(CONTENT_SAMPLE)) {
			temp = sample;
		}
		else if (input.equals(CONTENT_EXPRESSION)) {
			temp = expression;
		}

		JSONArray ret = new JSONArray();

		assert temp != null;
		for (Object aTemp : temp) {
			JSONObject a = (JSONObject) aTemp;

			for (String item1 : items) {
				if (a.get("pid").toString().equals(item1)) {
					ret.add(a);
					break;
				}
			}
		}

		if (input.equals(CONTENT_SAMPLE)) {
			sample = ret;
		}
		else if (input.equals(CONTENT_EXPRESSION)) {
			expression = ret;
		}
	}

	public void AddToCurrentSet(JSONArray b, String type) {
		for (Object aB : b) {
			JSONObject c = (JSONObject) aB;
			if (type.equals(CONTENT_SAMPLE)) {
				this.sample.add(c);
			}
			else if (type.equals(CONTENT_EXPRESSION)) {
				this.expression.add(c);
			}
		}
	}

	public JSONArray append(JSONArray array, String input) {
		JSONArray items = null;

		if (input.equals(CONTENT_SAMPLE)) {
			items = this.sample;
		}
		else if (input.equals(CONTENT_EXPRESSION)) {
			items = this.expression;
		}

		assert items != null;
		for (Object item : items) {
			JSONObject obj = (JSONObject) item;
			array.add(obj);
		}
		return array;
	}

	public JSONArray get(String type) {
		if (type.equals(CONTENT_EXPRESSION)) {
			return this.expression;
		}
		else if (type.equals(CONTENT_SAMPLE)) {
			return this.sample;
		}
		return null;
	}
}
