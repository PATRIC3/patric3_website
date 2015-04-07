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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.patricbrc.Workspace.ObjectMeta;
import org.patricbrc.Workspace.Workspace_tuple_2;
import org.patricbrc.Workspace.get_params;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class ExpressionDataCollection {

	public final static String CONTENT_EXPRESSION = "expression";

	public final static String CONTENT_SAMPLE = "sample";

	public final static String CONTENT_MAPPING = "mapping";

	private static final Logger LOGGER = LoggerFactory.getLogger(ExpressionDataCollection.class);

	private List<String> expressionFileName;

	private List<String> sampleFileName;

	private List<String> mappingFileName;

	private JSONArray sample, expression;

	private String WORKSPACE_API_URL;

	private String WORKSPACE_TOKEN;

	public ExpressionDataCollection(String path, String token) {

		sample = new JSONArray();
		expression = new JSONArray();
		expressionFileName = new ArrayList<>();
		sampleFileName = new ArrayList<>();
		mappingFileName = new ArrayList<>();

		WORKSPACE_API_URL = System.getProperty("workspaceServiceURL", "http://p3.theseed.org/services/Workspace");
		WORKSPACE_TOKEN = token;

		try {
			org.patricbrc.Workspace.Workspace serviceWS = new org.patricbrc.Workspace.Workspace(WORKSPACE_API_URL, WORKSPACE_TOKEN);
			get_params gp = new get_params();
			gp.objects = Arrays.asList(path.split(","));
			gp.metadata_only = 1;
			gp.adminmode = 0;

			LOGGER.debug("{}", gp.objects);
			List<Workspace_tuple_2> r = serviceWS.get(gp);

			for (Workspace_tuple_2 item : r) {
				ObjectMeta meta = item.e_1;
				Map<String, Object> autoMeta = meta.e_9;
				List<String> outputFiles = (List) autoMeta.get("output_files");

				for (String filename : outputFiles) {
					if (filename.contains("expression.json")) {
						expressionFileName.add(filename);
					}
					else if (filename.contains("sample.json")) {
						sampleFileName.add(filename);
					}
					else if (filename.contains("mapping.json")) {
						mappingFileName.add(filename);
					}
				}
			}
		}
		catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	public String readFileContent(String path) {

		String content = null;
		try {
			org.patricbrc.Workspace.Workspace serviceWS = new org.patricbrc.Workspace.Workspace(WORKSPACE_API_URL, WORKSPACE_TOKEN);
			get_params gp = new get_params();
			gp.objects = Arrays.asList(path);
			gp.metadata_only = 0;
			gp.adminmode = 0;

			List<Workspace_tuple_2> r = serviceWS.get(gp);

			for (Workspace_tuple_2 item : r) {
				content = item.e_2;
			}
		}
		catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		return content;
	}

	public void read(String input) throws FileNotFoundException {

		List<String> temp = null;

		if (input.equals(CONTENT_SAMPLE)) {
			temp = sampleFileName;
		}
		else if (input.equals(CONTENT_EXPRESSION)) {
			temp = expressionFileName;
		}

		assert temp != null;
		for (String aTemp : temp) {

			try {
				String strLine = readFileContent(aTemp);
				JSONObject tmp = (JSONObject) new JSONParser().parse(strLine);
				AddToCurrentSet((JSONArray) tmp.get(input), input);
			}
			catch (ParseException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

	public void filter(String item, String input) throws FileNotFoundException {

		JSONArray temp = null;
		List items = Arrays.asList(item.split(","));

		if (CONTENT_SAMPLE.equals(input)) {
			temp = sample;
		}
		else if (CONTENT_EXPRESSION.equals(input)) {
			temp = expression;
		}

		JSONArray ret = new JSONArray();

		assert temp != null;
		for (Object aTemp : temp) {
			JSONObject a = (JSONObject) aTemp;

			if (items.contains(a.get("pid").toString())) {
				ret.add(a);
			}
		}

		if (CONTENT_SAMPLE.equals(input)) {
			sample = ret;
		}
		else if (CONTENT_EXPRESSION.equals(input)) {
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
