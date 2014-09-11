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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.jboss.portal.identity.IdentityContext;
import org.jboss.portal.identity.IdentityException;
import org.jboss.portal.identity.IdentityServiceController;
import org.jboss.portal.identity.User;
import org.jboss.portal.identity.UserProfileModule;
import org.jboss.portal.identity.db.HibernateUserModuleImpl;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import edu.vt.vbi.patric.identity.db.HibernateUserModuleImpl;

@SuppressWarnings("unchecked")
public class PolyomicHandler {
	private String ENDPOINT = null;

	private String APPTOKEN = null;

	private Long DefaultWorkspaceID = null;

	private String AuthenticationCode = null;

	private String AuthenticationToken = null;

	protected static final boolean _debug = false;

	// expression | sample | mapping | experiment
	public final static String CONTENT_EXPRESSION = "expression";

	public final static String CONTENT_SAMPLE = "sample";

	public final static String CONTENT_MAPPING = "mapping";

	public final static String CONTENT_EXPERIMENT = "experiment";

	private static final Logger LOGGER = LoggerFactory.getLogger(PolyomicHandler.class);

	public PolyomicHandler() {
		ENDPOINT = System.getProperty("polyomic.baseUrl", "http://polyomic.patricbrc.org");
		APPTOKEN = System.getProperty("polyomic.appToken", "testAppToken");
	}

	public String getEndpoint() {
		return ENDPOINT;
	}

	public Long getDefaultWorkspaceID() {
		return DefaultWorkspaceID;
	}

	public void setDefaultWorkspaceID(Long defaultWorkspaceID) {
		DefaultWorkspaceID = defaultWorkspaceID;
	}

	public String getAuthenticationCode() {
		return AuthenticationCode;
	}

	public void setAuthenticationCode(String authenticationCode) {
		AuthenticationCode = authenticationCode;
	}

	public String getAuthenticationToken() {
		return AuthenticationToken;
	}

	public void setAuthenticationToken(String authenticationToken) {
		AuthenticationToken = authenticationToken;
	}

	public void authenticate(String userName) {
		if (AuthenticationCode == null) {
			AuthenticationCode = retrieveAuthenticationCode(userName);
		}

		if (AuthenticationToken == null) {
			AuthenticationToken = retrieveAuthenticationToken(userName);
		}
	}

	/**
	 * This method creates a user in polyomic system
	 * 
	 * @param userName
	 * @return authcode
	 */
	public String createUser(String userName) {
		String code = null;

		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPost httpRequest = new HttpPost(ENDPOINT + "/User/");
		httpRequest.setHeader("Content-Type", "application/json");
		httpRequest.setHeader("Accept", "application/json");

		JSONObject jsonRequest = new JSONObject();
		JSONObject jsonParam = new JSONObject();
		jsonRequest.put("id", 1);
		jsonRequest.put("method", "createUser");
		jsonParam.put("applicationToken", APPTOKEN);
		jsonParam.put("username", userName);
		jsonRequest.put("params", jsonParam);

		try {
			StringEntity entity;
			entity = new StringEntity(jsonRequest.toJSONString());
			httpRequest.setEntity(entity);
		}
		catch (UnsupportedEncodingException e) {
			LOGGER.error(e.getMessage(), e);
		}

		try {
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			code = httpclient.execute(httpRequest, responseHandler);
			code = code.replaceAll("\"", "");
			AuthenticationCode = code;

			LOGGER.debug("PolyomicHandler.createUser is called. creating [{}]", userName);
			LOGGER.debug("request in createUser:", httpRequest.getRequestLine().toString());
			LOGGER.debug("response in createUser: ", code);
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		finally {
			httpclient.getConnectionManager().shutdown();
		}

		return code;
	}

	/**
	 * This retrieves authcode from JBoss User Property and set in an attribute. If authcode is not set, create one and save it in User Property.
	 * 
	 * @param userName
	 * @return authcode
	 */
	public String retrieveAuthenticationCode(String userName) {
		String code = null;

		try {
			SessionFactory identitySessionFactory = (SessionFactory) new InitialContext().lookup("java:/portal/IdentitySessionFactory");
			Session session = identitySessionFactory.openSession();
			Transaction transaction = session.beginTransaction();

			IdentityServiceController identityServiceController = (IdentityServiceController) new InitialContext()
					.lookup("java:/portal/IdentityServiceController");
			HibernateUserModuleImpl userModule = (HibernateUserModuleImpl) identityServiceController.getIdentityContext().getObject(
					IdentityContext.TYPE_USER_MODULE);
			UserProfileModule userProfileModule = (UserProfileModule) identityServiceController.getIdentityContext().getObject(
					IdentityContext.TYPE_USER_PROFILE_MODULE);

			try {
				User user = userModule.findUserByUserName(userName);
				Object objStorage = userProfileModule.getProperty(user, "portal.user.storage");
				if (objStorage != null) {
					code = objStorage.toString();
				}
				else {
					// no token is set & need to create one
					LOGGER.debug("no user code found. we are creating one for {}", userName);
					code = createUser(userName);
					// register to JBoss user property
					setAuthcodeToUserProperty(userName, code);
				}
			}
			finally {
				transaction.commit();
			}
			LOGGER.debug("PolyomicHandler.retrieveAuthenticationCode is called");
		}
		catch (IdentityException | NamingException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return code;
	}

	private void setAuthcodeToUserProperty(String userName, String code) {
		IdentityServiceController identityServiceController;
		HibernateUserModuleImpl userModule;
		UserProfileModule userProfileModule;

		try {
			SessionFactory identitySessionFactory = (SessionFactory) new InitialContext().lookup("java:/portal/IdentitySessionFactory");
			Session session = identitySessionFactory.openSession();
			Transaction transaction = session.beginTransaction();

			identityServiceController = (IdentityServiceController) new InitialContext().lookup("java:/portal/IdentityServiceController");
			userModule = (HibernateUserModuleImpl) identityServiceController.getIdentityContext().getObject(IdentityContext.TYPE_USER_MODULE);
			userProfileModule = (UserProfileModule) identityServiceController.getIdentityContext()
					.getObject(IdentityContext.TYPE_USER_PROFILE_MODULE);

			try {
				User user = userModule.findUserByUserName(userName);
				userProfileModule.setProperty(user, "portal.user.storage", code);
			}
			finally {
				transaction.commit();
			}
		}
		catch (IdentityException | NamingException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	/**
	 * Issue an authtoken
	 * 
	 * @param userName
	 * @return authtoken
	 */
	public String retrieveAuthenticationToken(String userName) {
		String token = null;

		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPost httpRequest = new HttpPost(ENDPOINT + "/Token/");
		httpRequest.setHeader("Content-Type", "application/json");
		httpRequest.setHeader("Accept", "application/json");

		JSONObject jsonRequest = new JSONObject();
		JSONObject jsonParam = new JSONObject();
		jsonRequest.put("id", 1);
		jsonRequest.put("method", "getAuthorizationToken");
		jsonParam.put("applicationToken", "testAppToken");
		jsonParam.put("authorizationCode", AuthenticationCode);
		try {
			jsonParam.put("authorizationUserId", URLEncoder.encode(userName, "UTF-8"));
		}
		catch (UnsupportedEncodingException e1) {
			LOGGER.error(e1.getMessage(), e1);
		}
		jsonRequest.put("params", jsonParam);

		try {
			StringEntity entity;
			entity = new StringEntity(jsonRequest.toJSONString());
			httpRequest.setEntity(entity);
		}
		catch (UnsupportedEncodingException e) {
			LOGGER.error(e.getMessage(), e);
		}

		try {
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			token = httpclient.execute(httpRequest, responseHandler);
			if (token != null) {
				token = token.replaceAll("\"", "");
				AuthenticationToken = token;
			}
			else {
				LOGGER.debug("token is null");
			}

			LOGGER.debug("PolyomicHandler.retrieveAuthenticationToken is called");
			LOGGER.debug("authentication tokien: {}", token);
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		finally {
			httpclient.getConnectionManager().shutdown();
		}

		return token;
	}

	/**
	 * Read collection meta data
	 * 
	 * @param id collection id
	 * @param scope currently this is not used
	 * @return
	 */
	public JSONObject getCollection(String id, String scope) {

		if (AuthenticationToken == null)
			return null;

		JSONObject collection = null;
		DefaultHttpClient httpclient = new DefaultHttpClient();

		// scoping
		String url = ENDPOINT + "/Collection/" + id;
		if (scope != null) {
			url += "." + scope;
		}

		HttpGet httpRequest = new HttpGet(url);
		httpRequest.setHeader("Accept", "application/json");
		httpRequest.setHeader("authorized_session", "polyomic authorization_token=" + AuthenticationToken);

		try {
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String strResponseBody = httpclient.execute(httpRequest, responseHandler);

			JSONParser parser = new JSONParser();
			collection = (JSONObject) parser.parse(strResponseBody);

		}
		catch (IOException | ParseException e) {
			LOGGER.error(e.getMessage(), e);
		}
		finally {
			httpclient.getConnectionManager().shutdown();
		}

		return collection;
	}

	public JSONObject getExpressionDataFileReaderConfig(String id) {
		JSONObject config = new JSONObject();

		JSONObject collection = getCollection(id, null);
		JSONObject jsonSampleFile = findRawFile(collection, CONTENT_SAMPLE);
		JSONObject jsonExpressionFile = findRawFile(collection, CONTENT_EXPRESSION);

		if (jsonSampleFile.containsKey("name")) {
			config.put("sampleFilePresent", true);
			config.put("sampleURL", jsonSampleFile.get("url"));
			config.put("sampleFileType", jsonSampleFile.get("type"));
		}
		else {
			config.put("sampleFilePresent", false);
		}

		if (jsonExpressionFile.containsKey("name")) {
			config.put("dataFileName", jsonExpressionFile.get("name"));
			config.put("dataURL", jsonExpressionFile.get("url"));
			config.put("dataFileType", jsonExpressionFile.get("type"));
			config.put("dataFileFormat", jsonExpressionFile.get("format"));
			config.put("dataFileOrientation", jsonExpressionFile.get("orientation"));
		}

		config.put("collectionID", id);

		return config;
	}

	private JSONObject findRawFile(JSONObject collection, String content) {
		JSONObject fileObj = new JSONObject();

		String collectionId = collection.get("id").toString();
		JSONArray manifest = (JSONArray) collection.get("manifest");
		JSONObject _file = null;
		String _content = null, _file_name = null, _file_ext = null;
		String _file_type = null, _file_format = null, _file_orientation = null;
		String _accept = null;

		for (int i = 0; i < manifest.size(); i++) {
			_file = (JSONObject) manifest.get(i);

			if (_file.containsKey("content") && _file.containsKey("filename")) {
				_content = _file.get("content").toString();
				_file_name = FilenameUtils.getName(_file.get("filename").toString()).replaceAll(" ", "%20");
				_file_ext = FilenameUtils.getExtension(_file.get("filename").toString());

				if (_content.equals(content) && !_file_ext.equals("json")) {

					// add file_name, file_ext
					fileObj.put("name", _file_name);
					fileObj.put("extension", _file_ext);

					// add file_format
					if (_file.containsKey("type")) {
						_file_type = (String) _file.get("type");
						fileObj.put("type", _file_type);
					}

					// add file_format
					if (_file.containsKey("format")) {
						_file_format = (String) _file.get("format");
						fileObj.put("format", _file_format);
					}

					// add file_format
					if (_file.containsKey("orientation")) {
						_file_orientation = (String) _file.get("orientation");
						fileObj.put("orientation", _file_orientation);
					}

					// build url and add the file_url
					/*
					 * if (_file_ext.equals("xls") || _file_ext.equals("xlsx")) { _accept = ""; } else { _accept = "http_accept=text/plain"; }
					 */
					_accept = "http_accept=*/*";
					String _url = ENDPOINT + "/Collection/" + collectionId + "/" + _file_name + "?" + _accept
							+ "&http_authorized_session=polyomic%20authorization_token%3D" + AuthenticationToken;
					fileObj.put("url", _url);

					// no more need to iterate
					break;
				}
			}
		}

		return fileObj;
	}

	/**
	 * Find an associate JSON file and build an URL with corresponding headers
	 * 
	 * @param collection
	 * @param content file content (expression | sample | mapping | experiment)
	 * @return String url
	 */
	public String findJSONUrl(JSONObject collection, String content) {

		String collectionId = collection.get("id").toString();
		JSONArray manifest = (JSONArray) collection.get("manifest");
		JSONObject _file = null;
		String _file_name = null, _file_ext = null;

		for (int i = 0; i < manifest.size(); i++) {
			_file = (JSONObject) manifest.get(i);
			if (_file.containsKey("content") && _file.containsKey("filename")) {

				_file_ext = FilenameUtils.getExtension(_file.get("filename").toString());

				if (_file.get("content").toString().equals(content) && _file_ext.equals("json")) {
					_file_name = FilenameUtils.getName(_file.get("filename").toString());
				}
			}
		}
		if (_file_name != null) {
			return ENDPOINT + "/Collection/" + collectionId + "/" + _file_name
					+ "?http_accept=*/*&http_authorized_session=polyomic%20authorization_token%3D" + AuthenticationToken;
		}
		else {
			return null;
		}
	}

	/**
	 * Save JSON object in collection
	 * 
	 * @param id collection id
	 * @param filename filename
	 * @param json JSONObject
	 * @param content (expression | sample | idmappig | experiment)
	 */
	public void saveJSONtoCollection(String id, String filename, JSONObject json, String content) {

		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPost httpRequest = new HttpPost(ENDPOINT + "/Collection/" + id);
		httpRequest.setHeader("authorized_session", "polyomic authorization_token=" + AuthenticationToken);
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		try {
			/*
			 * InputStreamBody will set chunked = true whereas ByteArrayBody will set length of request properly
			 */
			/*
			 * InputStreamBody body = new InputStreamBody( new ByteArrayInputStream(json.toJSONString().getBytes("UTF-8")),
			 * "application/json; charset=UTF-8", filename);
			 */
			ByteArrayBody body = new ByteArrayBody(json.toJSONString().getBytes("UTF-8"), "application/json; charset=UTF-8", filename);

			LOGGER.trace("ContentLength in body: {}", body.getContentLength());
			LOGGER.trace("TransferEncoding in body: {}", body.getTransferEncoding());

			entity.addPart("file0", body);
		}
		catch (UnsupportedEncodingException e) {
			LOGGER.error(e.getMessage(), e);
		}

		LOGGER.trace("ContentLength in entity: {}", entity.getContentLength());
		LOGGER.trace("ContentEncoding in entity: {}", entity.getContentEncoding());
		LOGGER.trace("ContentType in entity: {}", entity.getContentType());
		LOGGER.trace("isChunked: {}", entity.isChunked());


		try {
			entity.addPart("file0_content", new StringBody(content));
		}
		catch (UnsupportedEncodingException e) {
			LOGGER.error(e.getMessage(), e);
		}

		httpRequest.setEntity(entity);

		try {
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			httpclient.execute(httpRequest, responseHandler);
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		finally {
			httpclient.getConnectionManager().shutdown();
		}
	}

	public void saveJSONFilesToCollection(String id, ExpressionDataFileReader reader) {

		DefaultHttpClient httpclient = new DefaultHttpClient();

		HttpPost httpRequest = new HttpPost(ENDPOINT + "/Collection/" + id);
		httpRequest.setHeader("authorized_session", "polyomic authorization_token=" + AuthenticationToken);
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		int fileIndex = 0;
		JSONObject json;
		ByteArrayBody body;
		byte[] bytes = null;

		json = reader.get(CONTENT_SAMPLE);
		if (json != null) {
			try {
				bytes = json.toJSONString().getBytes("UTF-8");
			}
			catch (UnsupportedEncodingException e) {
				LOGGER.error(e.getMessage(), e);
			}

			LOGGER.debug("sample size: {}", bytes.length);
			body = new ByteArrayBody(bytes, "application/json; charset=UTF-8", CONTENT_SAMPLE + ".json");

			entity.addPart("file" + fileIndex, body);
			try {
				entity.addPart("file" + fileIndex + "_content", new StringBody(CONTENT_SAMPLE));
			}
			catch (UnsupportedEncodingException e) {
				LOGGER.error(e.getMessage(), e);
			}
			fileIndex++;
		}

		json = reader.get(CONTENT_MAPPING);
		if (json != null) {
			try {
				bytes = json.toJSONString().getBytes("UTF-8");
			}
			catch (UnsupportedEncodingException e) {
				LOGGER.error(e.getMessage(), e);
			}

			LOGGER.debug("mapping size: {}", bytes.length);
			body = new ByteArrayBody(bytes, "application/json; charset=UTF-8", CONTENT_MAPPING + ".json");

			entity.addPart("file" + fileIndex, body);
			try {
				entity.addPart("file" + fileIndex + "_content", new StringBody(CONTENT_MAPPING));
			}
			catch (UnsupportedEncodingException e) {
				LOGGER.error(e.getMessage(), e);
			}
			fileIndex++;
		}

		json = reader.get(CONTENT_EXPRESSION);
		if (json != null) {
			try {
				bytes = json.toJSONString().getBytes("UTF-8");
			}
			catch (UnsupportedEncodingException e) {
				LOGGER.error(e.getMessage(), e);
			}

			LOGGER.debug("expression size: {}", bytes.length);
			body = new ByteArrayBody(bytes, "application/json; charset=UTF-8", CONTENT_EXPRESSION + ".json");

			entity.addPart("file" + fileIndex, body);
			try {
				entity.addPart("file" + fileIndex + "_content", new StringBody(CONTENT_EXPRESSION));
			}
			catch (UnsupportedEncodingException e) {
				LOGGER.error(e.getMessage(), e);
			}
			fileIndex++;
		}

		httpRequest.setEntity(entity);

		try {
			LOGGER.trace("request in saveJSONFilesToCollection: {}", httpRequest.getRequestLine().toString());
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			httpclient.execute(httpRequest, responseHandler);
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		finally {
			httpclient.getConnectionManager().shutdown();
		}
	}

	public String createCollection(String name) {
		String collection = null;

		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPost httpRequest = new HttpPost(ENDPOINT + "/Collection/");
		httpRequest.setHeader("Content-Type", "application/json");
		httpRequest.setHeader("Accept", "application/json");
		httpRequest.setHeader("authorized_session", "polyomic authorization_token=" + AuthenticationToken);

		JSONObject jsonRequest = new JSONObject();
		jsonRequest.put("id", 1);
		jsonRequest.put("method", "create");
		jsonRequest.put("params", name);

		try {
			StringEntity entity;
			entity = new StringEntity(jsonRequest.toJSONString());
			httpRequest.setEntity(entity);
		}
		catch (UnsupportedEncodingException e) {
			LOGGER.error(e.getMessage(), e);
		}

		try {
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody = httpclient.execute(httpRequest, responseHandler);

			JSONParser parser = new JSONParser();
			JSONObject jsonResponseBody = (JSONObject) parser.parse(responseBody);
			collection = (String) jsonResponseBody.get("id");

			LOGGER.trace("request in createCollection: {}", httpRequest.getRequestLine().toString());
			LOGGER.trace("response in createCollection: {}", responseBody);
			LOGGER.trace("collection id: {}", collection);
		}
		catch (IOException | ParseException e) {
			LOGGER.error(e.getMessage(), e);
		}
		finally {
			httpclient.getConnectionManager().shutdown();
		}

		return collection;
	}

	private Long createWorkspace(String name) {

		Long workspaceId = null;
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPost httpRequest = new HttpPost(ENDPOINT + "/Workspace/");
		httpRequest.setHeader("Content-Type", "application/json");
		httpRequest.setHeader("Accept", "application/json");
		httpRequest.setHeader("authorized_session", "polyomic authorization_token=" + AuthenticationToken);

		JSONObject jsonRequest = new JSONObject();
		jsonRequest.put("id", 1);
		jsonRequest.put("method", "create");
		JSONObject jsonParam = new JSONObject();
		jsonParam.put("workspace", name);
		jsonRequest.put("params", jsonParam);

		try {
			StringEntity entity;
			entity = new StringEntity(jsonRequest.toJSONString());
			httpRequest.setEntity(entity);
		}
		catch (UnsupportedEncodingException e) {
			LOGGER.error(e.getMessage(), e);
		}

		try {
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody = httpclient.execute(httpRequest, responseHandler);

			JSONParser parser = new JSONParser();
			JSONObject jsonResponseBody = (JSONObject) parser.parse(responseBody);

			workspaceId = (Long) jsonResponseBody.get("id");

			LOGGER.trace("request in createWorkspace: {}", httpRequest.getRequestLine().toString());
			LOGGER.trace("response in createWorkspace: {}", responseBody);

		}
		catch (IOException | ParseException e) {
			LOGGER.error(e.getMessage(), e);
		}
		finally {
			httpclient.getConnectionManager().shutdown();
		}

		return workspaceId;
	}

	public void retrieveDefaultWorkspace() {

		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpGet httpRequest = new HttpGet(ENDPOINT + "/Workspace/default");
		httpRequest.setHeader("Accept", "application/json");
		httpRequest.setHeader("authorized_session", "polyomic authorization_token=" + AuthenticationToken);

		try {
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String strResponseBody = httpclient.execute(httpRequest, responseHandler);

			JSONParser parser = new JSONParser();
			JSONObject jsonResponseBody = (JSONObject) parser.parse(strResponseBody);

			DefaultWorkspaceID = (Long) jsonResponseBody.get("id");

			LOGGER.trace("response in retrieveDefaultWorkspace: {}", strResponseBody);
			LOGGER.trace("DefaultWorkspaceId: {}", DefaultWorkspaceID);
		}
		catch (IOException e) {
			LOGGER.debug("creating a default workspace");
			DefaultWorkspaceID = createWorkspace("default");
		}
		catch (ParseException e) {
			LOGGER.error(e.getMessage(), e);
		}
		finally {
			httpclient.getConnectionManager().shutdown();
		}
	}

	public Workspace getWorkspaceData(Long id) {

		Workspace ws;
		JSONObject jsonItems = readWorkspace(id);
		JSONObject jsonData = (JSONObject) jsonItems.get("data");

		if (jsonData == null || jsonData.toJSONString().equals("{}")) {
			ws = new Workspace();
			setWorkspaceData(id, ws);
		}
		else {
			ws = new Workspace(jsonData);
		}

		return ws;
	}

	public UIPreference getUIPreference(Long id) {
		UIPreference uiPref;

		JSONObject jsonItems = readWorkspace(id);
		JSONObject jsonData = (JSONObject) jsonItems.get("preference");

		if (jsonData == null || jsonData.isEmpty()) {
			uiPref = new UIPreference();
			setUIPreference(id, uiPref);
		}
		else {
			uiPref = new UIPreference(jsonData);
		}

		return uiPref;
	}

	private JSONObject readWorkspace(Long id) {
		JSONObject workspace = null;
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpGet httpRequest = new HttpGet(ENDPOINT + "/Workspace/" + id);
		httpRequest.setHeader("Accept", "application/json");
		httpRequest.setHeader("authorized_session", "polyomic authorization_token=" + AuthenticationToken);

		try {
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String strResponseBody = httpclient.execute(httpRequest, responseHandler);

			JSONParser parser = new JSONParser();
			JSONObject jsonResponseBody = (JSONObject) parser.parse(strResponseBody);
			workspace = (JSONObject) jsonResponseBody.get("items");
		}
		catch (IOException | ParseException e) {
			LOGGER.error(e.getMessage(), e);
		}
		finally {
			httpclient.getConnectionManager().shutdown();
		}

		return workspace;
	}

	public void setWorkspaceData(Long id, Workspace ws) {

		JSONObject jsonItems = readWorkspace(id);
		jsonItems.put("data", ws.getWorkspace());

		JSONObject jsonBody = new JSONObject();
		jsonBody.put("items", jsonItems);

		updateWorkspace(id, jsonBody);
	}

	public void setUIPreference(Long id, UIPreference uiPref) {

		JSONObject jsonItems = readWorkspace(id);
		jsonItems.put("preference", uiPref.getUIPreference());

		JSONObject jsonBody = new JSONObject();
		jsonBody.put("items", jsonItems);

		updateWorkspace(id, jsonBody);
	}

	/**
	 * set collection state and create a map
	 * 
	 * @param id
	 * @param state
	 * @return
	 */
	public String setCollectionState(String id, String state) {
		String collection = null;

		DefaultHttpClient httpclient = new DefaultHttpClient();

		HttpPost httpRequest = new HttpPost(ENDPOINT + "/Collection/" + id);
		httpRequest.setHeader("Content-Type", "application/json");
		httpRequest.setHeader("Accept", "application/json");
		httpRequest.setHeader("authorized_session", "polyomic authorization_token=" + AuthenticationToken);

		JSONObject jsonRequest = new JSONObject();
		jsonRequest.put("state", state);

		JSONObject map = new JSONObject();
		JSONObject exp = new JSONObject();
		exp.put("path", "experiment.json");
		map.put("experiment", exp);
		JSONObject sample = new JSONObject();
		sample.put("path", "sample.json");
		map.put("sample", sample);
		jsonRequest.put("map", map);

		try {
			StringEntity entity;
			entity = new StringEntity(jsonRequest.toJSONString());
			httpRequest.setEntity(entity);
		}
		catch (UnsupportedEncodingException e) {
			LOGGER.error(e.getMessage(), e);
		}

		try {
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody = httpclient.execute(httpRequest, responseHandler);

			LOGGER.trace("request in setCollectionState: {}", httpRequest.getRequestLine().toString());
			LOGGER.trace("response in setCollectionState: {}", responseBody);
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		finally {
			httpclient.getConnectionManager().shutdown();
		}

		return collection;
	}

	/**
	 * @param id collection id
	 * @param experiment JSONObject format experiment metadata
	 */
	public void addWorkspaceCollection(String id, JSONObject experiment) {

		JSONObject jsonItems = readWorkspace(DefaultWorkspaceID);

		JSONObject jsonCollections = new JSONObject();
		if (jsonItems.containsKey("collections") && !jsonItems.get("collections").toString().equals("")) {
			jsonCollections = (JSONObject) jsonItems.get("collections");
		}
		jsonCollections.put(id, experiment);
		jsonItems.put("collections", jsonCollections);
		JSONObject jsonBody = new JSONObject();
		jsonBody.put("items", jsonItems);

		updateWorkspace(DefaultWorkspaceID, jsonBody);
	}

	public void refreshWorkspaceCollection(String id) {

		JSONObject experiment = getCollection(id, "experiment");
		JSONObject items = readWorkspace(DefaultWorkspaceID);
		JSONObject collections = (JSONObject) items.get("collections");

		collections.put(id, experiment);
		items.put("collections", collections);

		JSONObject jsonBody = new JSONObject();
		jsonBody.put("items", items);

		if (DefaultWorkspaceID != null) {
			updateWorkspace(DefaultWorkspaceID, jsonBody);
		}
	}

	private void updateWorkspace(Long workspaceId, JSONObject body) {

		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPost httpRequest = new HttpPost(ENDPOINT + "/Workspace/" + workspaceId);
		httpRequest.setHeader("Content-Type", "application/json");
		httpRequest.setHeader("Accept", "application/json");
		httpRequest.setHeader("authorized_session", "polyomic authorization_token=" + AuthenticationToken);

		try {
			StringEntity entity = new StringEntity(body.toJSONString());
			httpRequest.setEntity(entity);
		}
		catch (UnsupportedEncodingException e) {
			LOGGER.error(e.getMessage(), e);
		}

		try {
			httpclient.execute(httpRequest);
			LOGGER.debug("saving workspace: {}", body.toJSONString());
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		finally {
			httpclient.getConnectionManager().shutdown();
		}
	}

	/**
	 * @param id
	 */
	private void deleteWorkspaceCollection(String id) {
		// TODO: implement when needed
	}

	/**
	 * 
	 * @param id
	 */
	public void deleteCollection(String id) {
		deleteWorkspaceCollection(id);
		// actually delete collection
	}

	/**
	 * [this should be moved to workspace supporter]
	 * 
	 * @param collectionIds
	 * @return
	 */
	public JSONObject getExperiments(List<String> collectionIds) {
		JSONObject res = new JSONObject();
		JSONArray results = new JSONArray();

		if (DefaultWorkspaceID == null) {
			retrieveDefaultWorkspace();
		}

		JSONObject jsonItems = readWorkspace(DefaultWorkspaceID);
		JSONObject jsonCollections = (JSONObject) jsonItems.get("collections");

		for (String id : collectionIds) {
			if (jsonCollections.containsKey(id)) {
				results.add(jsonCollections.get(id));
			}
		}

		res.put("total", results.size());
		res.put("results", results);

		return res;
	}

	public JSONArray getSamples(String expId, List<String> sampleIds) {

		JSONArray results = new JSONArray();
		JSONObject jsonSamples = getCollection(expId, "sample");
		JSONArray samples = (JSONArray) jsonSamples.get("sample");

		if (sampleIds == null) {
			results = samples;
		}
		else {

			Map<String, JSONObject> hashSamples = new HashMap<>();
			for (Object s : samples) {
				JSONObject jsonSample = (JSONObject) s;
				jsonSample.put("source", "me");
				hashSamples.put(jsonSample.get("pid").toString(), jsonSample);
			}

			for (String id : sampleIds) {
				if (hashSamples.containsKey(expId + id)) {
					results.add(hashSamples.get(expId + id));
				}
			}
		}

		return results;
	}
}
