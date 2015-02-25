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
package edu.vt.vbi.patric.portlets;

import edu.vt.vbi.patric.beans.Genome;
import edu.vt.vbi.patric.common.SolrCore;
import edu.vt.vbi.patric.common.SolrInterface;
import edu.vt.vbi.patric.common.UIPreference;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.patricbrc.Workspace.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class WorkspacePortlet extends GenericPortlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(WorkspacePortlet.class);

	private final String WORKSPACE_API_URL = "http://p3.theseed.org/services/Workspace";

	private final String DEFAULT_WORKSPACE_NAME = "/home";

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		// do nothing
	}

	//	public void enumerateParameters(ResourceRequest request) {
	//		Enumeration<String> params = request.getParameterNames();
	//		while (params.hasMoreElements()) {
	//			String k = params.nextElement();
	//			LOGGER.info("{}: {}", k, request.getParameterValues(k)[0]);
	//		}
	//	}

	//	private PolyomicHandler getPolyomicHandler(ResourceRequest request) {
	//
	//		PolyomicHandler polyomic = new PolyomicHandler();
	//		PortletSession p_session = request.getPortletSession(true);
	//		String token = (String) p_session.getAttribute("PolyomicAuthToken", PortletSession.APPLICATION_SCOPE);
	//		Long defaultWId = (Long) p_session.getAttribute("DefaultWorkspaceID", PortletSession.APPLICATION_SCOPE);
	//
	//		if (token == null) {
	//			String userName = request.getUserPrincipal().getName();
	//			polyomic.authenticate(userName);
	//			p_session.setAttribute("PolyomicAuthToken", polyomic.getAuthenticationToken(), PortletSession.APPLICATION_SCOPE);
	//		}
	//		else {
	//			polyomic.setAuthenticationToken(token);
	//		}
	//		if (defaultWId == null) {
	//			polyomic.retrieveDefaultWorkspace();
	//			defaultWId = polyomic.getDefaultWorkspaceID();
	//			p_session.setAttribute("DefaultWorkspaceID", defaultWId, PortletSession.APPLICATION_SCOPE);
	//		}
	//		else {
	//			polyomic.setDefaultWorkspaceID(defaultWId);
	//		}
	//
	//		return polyomic;
	//	}

	//	private Workspace getValidWorkspace(ResourceRequest request) {
	//		PolyomicHandler polyomic = new PolyomicHandler();
	//		PortletSession p_session = request.getPortletSession(true);
	//		Workspace ws_from_session = (Workspace) p_session.getAttribute("workspace", PortletSession.APPLICATION_SCOPE);
	//		Workspace ws;
	//
	//		if (request.getUserPrincipal() == null) {
	//			if (ws_from_session != null) {
	//				ws = ws_from_session;
	//			}
	//			else {
	//				ws = new Workspace();
	//				saveWorkspace(request, ws);
	//			}
	//		}
	//		else {
	//			// polyomic or session.
	//			String userName = request.getUserPrincipal().getName();
	//			String token = (String) p_session.getAttribute("PolyomicAuthToken", PortletSession.APPLICATION_SCOPE);
	//			Long defaultWId = (Long) p_session.getAttribute("DefaultWorkspaceID", PortletSession.APPLICATION_SCOPE);
	//			if (token == null) {
	//				polyomic.authenticate(userName);
	//				p_session.setAttribute("PolyomicAuthToken", polyomic.getAuthenticationToken(), PortletSession.APPLICATION_SCOPE);
	//			}
	//			else {
	//				polyomic.setAuthenticationToken(token);
	//			}
	//			if (defaultWId == null) {
	//				polyomic.retrieveDefaultWorkspace();
	//				defaultWId = polyomic.getDefaultWorkspaceID();
	//				p_session.setAttribute("DefaultWorkspaceID", defaultWId, PortletSession.APPLICATION_SCOPE);
	//			}
	//			else {
	//				polyomic.setDefaultWorkspaceID(defaultWId);
	//			}
	//			ws = polyomic.getWorkspaceData(defaultWId);
	//		}
	//		return ws;
	//	}

	//	private UIPreference getValidUIPreference(ResourceRequest request) {
	//		PolyomicHandler polyomic = new PolyomicHandler();
	//		PortletSession p_session = request.getPortletSession(true);
	//		UIPreference uiPref_from_session = (UIPreference) p_session.getAttribute("preference", PortletSession.APPLICATION_SCOPE);
	//		UIPreference uiPref;
	//
	//		if (request.getUserPrincipal() == null) {
	//			if (uiPref_from_session != null) {
	//				uiPref = uiPref_from_session;
	//			}
	//			else {
	//				uiPref = new UIPreference();
	//				saveUIPreference(request, uiPref);
	//			}
	//		}
	//		else {
	//			String userName = request.getUserPrincipal().getName();
	//			String token = (String) p_session.getAttribute("PolyomicAuthToken", PortletSession.APPLICATION_SCOPE);
	//			Long defaultWId = (Long) p_session.getAttribute("DefaultWorkspaceID", PortletSession.APPLICATION_SCOPE);
	//			if (token == null) {
	//				polyomic.authenticate(userName);
	//				p_session.setAttribute("PolyomicAuthToken", polyomic.getAuthenticationToken(), PortletSession.APPLICATION_SCOPE);
	//			}
	//			else {
	//				polyomic.setAuthenticationToken(token);
	//			}
	//			if (defaultWId == null) {
	//				polyomic.retrieveDefaultWorkspace();
	//				defaultWId = polyomic.getDefaultWorkspaceID();
	//				p_session.setAttribute("DefaultWorkspaceID", defaultWId, PortletSession.APPLICATION_SCOPE);
	//			}
	//			else {
	//				polyomic.setDefaultWorkspaceID(defaultWId);
	//			}
	//			uiPref = polyomic.getUIPreference(defaultWId);
	//		}
	//		return uiPref;
	//	}

	private UIPreference getValidUIPreference(ResourceRequest request) {

		JSONParser jsonParser = new JSONParser();

		PortletSession p_session = request.getPortletSession(true);
		String strUiPref = (String) p_session.getAttribute("preference", PortletSession.APPLICATION_SCOPE);
		UIPreference uiPref_from_session = null;
		if (strUiPref != null) {
			try {
				strUiPref = (String) jsonParser.parse(strUiPref);
				LOGGER.debug("deserializing..{}", strUiPref);
				uiPref_from_session = new UIPreference((JSONObject) jsonParser.parse(strUiPref));
			}
			catch (ParseException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
		UIPreference uiPref = null;

		if (!isLoggedIn(request)) {
			if (uiPref_from_session != null) {
				uiPref = uiPref_from_session;
			}
			else {
				uiPref = new UIPreference();
				saveUIPreference(request, uiPref);
			}
		}
		else {
			String token = getAuthorizationToken(request);
			String path = getUserWorkspacePath(request, DEFAULT_WORKSPACE_NAME) + "/.preferences.json";

			try {
				Workspace serviceWS = new Workspace(WORKSPACE_API_URL, token);
				get_params gp = new get_params();
				gp.objects = Arrays.asList(path);
				gp.metadata_only = 0;
				gp.adminmode = 0;

				LOGGER.debug("requesting {}", path);
				List<Workspace_tuple_2> r = serviceWS.get(gp);

				for (Workspace_tuple_2 item : r) {
					if (item.e_2 != null) {
						strUiPref = (String) jsonParser.parse(item.e_2);
						JSONObject pref = (JSONObject) jsonParser.parse(strUiPref);

						if (pref != null) {
							uiPref = new UIPreference(pref);

							p_session.setAttribute("preference", stringfyUIPreference(uiPref.getUIPreference()), PortletSession.APPLICATION_SCOPE);
						}
					}
				}
			}
			catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
		return uiPref;
	}

	//	public void saveWorkspace(ResourceRequest request, Workspace ws) {
	//		if (request.getUserPrincipal() != null) {
	//			PolyomicHandler polyomic = getPolyomicHandler(request);
	//			Long defaultWId = polyomic.getDefaultWorkspaceID();
	//			polyomic.setWorkspaceData(defaultWId, ws);
	//
	//			PortletSession p_session = request.getPortletSession(true);
	//			p_session.setAttribute("workspace", ws, PortletSession.APPLICATION_SCOPE);
	//		}
	//		else {
	//			PortletSession p_session = request.getPortletSession(true);
	//			p_session.setAttribute("workspace", ws, PortletSession.APPLICATION_SCOPE);
	//		}
	//	}

	//	public void saveUIPreference(ResourceRequest request, UIPreference uiPref) {
	//		if (request.getUserPrincipal() != null) {
	//			PolyomicHandler polyomic = getPolyomicHandler(request);
	//			Long defaultWId = polyomic.getDefaultWorkspaceID();
	//			polyomic.setUIPreference(defaultWId, uiPref);
	//
	//			PortletSession p_session = request.getPortletSession(true);
	//			p_session.setAttribute("preference", uiPref, PortletSession.APPLICATION_SCOPE);
	//		}
	//		else {
	//			PortletSession p_session = request.getPortletSession(true);
	//			p_session.setAttribute("preference", uiPref, PortletSession.APPLICATION_SCOPE);
	//		}
	//	}

	public void saveUIPreference(ResourceRequest request, UIPreference uiPref) {
		if (isLoggedIn(request)) {
			String token = getAuthorizationToken(request);
			String path = getUserWorkspacePath(request, DEFAULT_WORKSPACE_NAME) + "/.preferences.json";

			try {
				Workspace serviceWS = new Workspace(WORKSPACE_API_URL, token);
				create_params cp = new create_params();
				Workspace_tuple_1 tuple = new Workspace_tuple_1();
				tuple.e_1 = path;
				tuple.e_2 = "unspecified";
				tuple.e_4 = stringfyUIPreference(uiPref.getUIPreference());
				cp.objects = Arrays.asList(tuple);
				cp.overwrite = 1;

				List<ObjectMeta> rs = serviceWS.create(cp);

				for (ObjectMeta r : rs) {
					LOGGER.debug("{},{}", r.toString(), r.e_1);
				}
			}
			catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}

			PortletSession p_session = request.getPortletSession(true);
			p_session.setAttribute("preference", stringfyUIPreference(uiPref.getUIPreference()), PortletSession.APPLICATION_SCOPE);
		}
		else {
			PortletSession p_session = request.getPortletSession(true);
			p_session.setAttribute("preference", stringfyUIPreference(uiPref.getUIPreference()), PortletSession.APPLICATION_SCOPE);
		}
	}

	private String stringfyUIPreference(JSONObject uipref) {
		StringBuffer sb = new StringBuffer();
		sb.append("\"").append(StringEscapeUtils.escapeJava(uipref.toJSONString())).append("\"");
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
		String action_type = request.getParameter("action_type");
		String action = request.getParameter("action");

		if (action_type != null && action != null) {
			// read workspace from session
			//			Workspace ws = getValidWorkspace(request);

			if (action_type.equals("groupAction")) {
				if (action.equals("create")) {
					// this.enumerateParameters(request);
					// TODO: implement with new Workspace API
					//					String grp_name = request.getParameter("group_name");
					//					String grp_desc = request.getParameter("group_desc");
					//					String grp_type = request.getParameter("group_type");
					//					String tracks = request.getParameter("tracks");
					//					String str_tags = request.getParameter("tags");
					//					String fid = request.getParameter("fid"); // this is a legacy parameter, but used by GSE
					//					String grp_element = request.getParameter("group_element");
					//
					//					if (grp_name == null || grp_name.equals("")) {
					//						grp_name = "(default)";
					//					}
					//					if (grp_desc == null) {
					//						grp_desc = "";
					//					}
					//					if (grp_type == null) {
					//						grp_type = "Feature";
					//					}
					//					if (tracks == null && fid != null) { // exception handling for GSE
					//						tracks = fid;
					//					}
					//					// Speical expception. if group is created from feature level, but user wanted to store as Genome group,
					//					// convert feature IDs to Genome IDs
					//					if (grp_type.equals("Feature") && (grp_element != null && grp_element.equals("Genome"))
					//							&& (tracks != null && !tracks.equals(""))) {
					//
					//						SolrInterface solr = new SolrInterface();
					//						solr.setCurrentInstance(SolrCore.FEATURE);
					//						JSONObject f = solr.queryFacet("feature_id:(" + tracks.replaceAll(",", " OR ") + ")", "genome_id");
					//						JSONArray GIDs = (JSONArray) f.get("facet");
					//						List<String> listGenomeId = new ArrayList<>();
					//						if (GIDs.size() > 0) {
					//							for (Object g : GIDs) {
					//								JSONObject genome = (JSONObject) g;
					//								listGenomeId.add(genome.get("value").toString());
					//							}
					//							tracks = StringUtils.join(listGenomeId, ",");
					//							grp_type = "Genome";
					//						}
					//					}
					//
					//					int tagId = ws.findTag("Group", grp_name, grp_type);
					//					if (tagId >= 0) {
					//						// add members to existing group
					//						if (tracks != null) {
					//							if (tracks.contains(",")) { // multiple entries
					//								Set<Integer> trackIds = ws.addTracks(grp_type, tracks);
					//								// add mappings
					//								for (int trackId : trackIds) {
					//									if (!ws.isMappingExist(tagId, trackId)) {
					//										ws.addMapping(tagId, trackId);
					//									}
					//								}
					//								// add tags
					//								if (str_tags != null && !str_tags.equals("")) {
					//									ws.addTagging(str_tags, trackIds);
					//								}
					//								// update group info: member count, date
					//								ws.updateGroupTag(tagId, null, null);
					//							}
					//							else {
					//								int	trackId = ws.addTrack(grp_type, tracks);
					//								// add mapping
					//								if (!ws.isMappingExist(tagId, trackId)) {
					//									ws.addMapping(tagId, trackId);
					//								}
					//								// add tags
					//								if (str_tags != null) {
					//									Set<Integer> trackIds = new HashSet<>();
					//									trackIds.add(trackId);
					//									ws.addTagging(str_tags, trackIds);
					//								}
					//								ws.updateGroupTag(tagId, null, null);
					//							}
					//						}
					//					}
					//					else {
					//						// create a new entry
					//						if (tracks != null) {
					//							Set<Integer> trackIds;
					//							if (tracks.contains(",")) {
					//								trackIds = ws.addTracks(grp_type, tracks);
					//							}
					//							else {
					//								int	trackId = ws.addTrack(grp_type, tracks);
					//								trackIds = new HashSet<>();
					//								trackIds.add(trackId);
					//							}
					//							// create tags
					//							int member_count = trackIds.size();
					//							tagId = ws.addGroup(grp_name, grp_type, grp_desc, member_count, null);
					//
					//							// add mappings
					//							ws.addMapping(tagId, trackIds);
					//
					//							// add tags
					//							if (str_tags != null) {
					//								ws.addTagging(str_tags, trackIds);
					//							}
					//						}
					//						else {
					//							// ERROR
					//							this.enumerateParameters(request);
					//						}
					//					}
					//
					//					saveWorkspace(request, ws);
				}
				//				else if (action.equals("removeGroup")) {
				//					String paramIdList = request.getParameter("idList");
				//					Set<Integer> tagIds = null;
				//					if (paramIdList != null && !paramIdList.equals("")) {
				//						tagIds = new HashSet<>();
				//						if (paramIdList.contains(",")) {
				//							for (String id : paramIdList.split(",")) {
				//								tagIds.add(Integer.parseInt(id));
				//							}
				//						}
				//						else {
				//							tagIds.add(Integer.parseInt(paramIdList));
				//						}
				//					}
				//					// find associated tracks
				//					List<JSONObject> mapping;
				//					List<Integer> trackIds = new ArrayList<>();
				//					for (int tagId : tagIds) {
				//						mapping = ws.findMappingByTagId(tagId);
				//						for (JSONObject track : mapping) {
				//							trackIds.add(Integer.parseInt(track.get("trackId").toString()));
				//						}
				//					}
				//					// remove group tag & mapping
				//					ws.removeTags(tagIds);
				//					ws.removeMapping(tagIds, null);
				//
				//					// remove associated tracks
				//					for (int trackId : trackIds) {
				//						Set<Integer> thisTrack = new HashSet<>();
				//						thisTrack.add(trackId);
				//						// if the track has group association
				//						if (ws.hasAssociation("Group", trackId)) {
				//							// do not delete track, but mapping (done)
				//						}
				//						else if (ws.hasAssociation("String", trackId)) { // if the track has string tag association only
				//							// delete tag association
				//							// find tags;
				//							List<JSONObject> maps = ws.findMappingByTrackId(trackId);
				//							Set<Integer> strTagIds = new HashSet<>();
				//							for (JSONObject map : maps) {
				//								strTagIds.add(Integer.parseInt(map.get("tagId").toString()));
				//							}
				//							ws.removeMapping(strTagIds, thisTrack);
				//							// if there is not other feature that associated the
				//							// tags, then delete the tag
				//							for (int tagId : strTagIds) {
				//								if (ws.countAssociation(tagId) == 0) {
				//									ws.removeTag(tagId);
				//								}
				//							}
				//							// delete track
				//							ws.removeTracks(thisTrack);
				//							// delete mapping (done)
				//						}
				//						else {
				//							ws.removeTracks(thisTrack);
				//							ws.removeMapping(null, thisTrack);
				//						}
				//					}
				//
				//					saveWorkspace(request, ws);
				//				}
				//				else if (action.equals("removeTrack")) {
				//					String paramRemoveFrom = request.getParameter("removeFrom");
				//					String paramGroups = request.getParameter("groups");
				//					String paramIdType = request.getParameter("idType");
				//					String paramIdList = request.getParameter("idList");
				//					Set<Integer> groups = null;
				//					Set<Object> internalIds = null;
				//
				//					if (paramGroups != null && !paramGroups.equals("")) {
				//						groups = new HashSet<>();
				//						if (paramGroups.contains(",")) {
				//							for (String id : paramGroups.split(",")) {
				//								groups.add(Integer.parseInt(id));
				//							}
				//						}
				//						else {
				//							groups.add(Integer.parseInt(paramGroups));
				//						}
				//					}
				//
				//					if (paramIdList != null && !paramIdList.equals("")) {
				//						internalIds = new HashSet<>();
				//						if (paramIdList.contains(",")) {
				//							for (String id : paramIdList.split(",")) {
				//								try {
				//									internalIds.add(Long.parseLong(id));
				//								}
				//								catch (NumberFormatException nfe) {
				//									internalIds.add(id);
				//								}
				//							}
				//						}
				//						else {
				//							try {
				//								internalIds.add(Long.parseLong(paramIdList));
				//							}
				//							catch (NumberFormatException nfe) {
				//								internalIds.add(paramIdList);
				//							}
				//						}
				//					}
				//
				//					Set<Integer> trackIds = ws.findTracks(paramIdType, internalIds);
				//
				//					if (paramRemoveFrom != null && paramRemoveFrom.equals("workspace")) {
				//						// need to update associated groups. collect groups
				//						// first.
				//						if (groups == null) {
				//							groups = new HashSet<>();
				//						}
				//						for (int trackId : trackIds) {
				//							List<JSONObject> maps = ws.findMappingByTrackId(trackId);
				//							for (JSONObject map : maps) {
				//								groups.add(Integer.parseInt(map.get("tagId").toString()));
				//							}
				//						}
				//
				//						ws.removeTracks(trackIds);
				//						ws.removeMapping(null, trackIds);
				//
				//						// update group info
				//						for (Integer tagId : groups) {
				//							ws.updateGroupTag(tagId, null, null);
				//						}
				//					}
				//					else if (paramRemoveFrom != null && paramRemoveFrom.equals("groups")) {
				//						// check if track is associated to other groups/tags
				//						ws.removeMapping(groups, trackIds);
				//
				//						// update group info
				//						for (Integer tagId : groups) {
				//							ws.updateGroupTag(tagId, null, null);
				//						}
				//
				//						for (int trackId : trackIds) {
				//							Set<Integer> thisTrack = new HashSet<>();
				//							thisTrack.add(trackId);
				//
				//							// if the track has group association
				//							if (ws.hasAssociation("Group", trackId)) {
				//								// do not delete track, but mapping (done)
				//							}
				//							else if (ws.hasAssociation("String", trackId)) { // if the track has string tag association only
				//								// delete tag association
				//								// find tags;
				//								List<JSONObject> maps = ws.findMappingByTrackId(trackId);
				//								Set<Integer> tagIds = new HashSet<>();
				//								for (JSONObject map : maps) {
				//									tagIds.add(Integer.parseInt(map.get("tagId").toString()));
				//								}
				//								ws.removeMapping(tagIds, thisTrack);
				//								// if there is not other feature that associated
				//								// the tags, then delete the tag
				//								for (int tagId : tagIds) {
				//									if (ws.countAssociation(tagId) == 0) {
				//										ws.removeTag(tagId);
				//									}
				//								}
				//								// delete track
				//								ws.removeTracks(thisTrack);
				//								// delete mapping (done)
				//							}
				//							else {
				//								ws.removeTracks(thisTrack);
				//								ws.removeMapping(null, thisTrack);
				//							}
				//						}
				//					}
				//
				//					saveWorkspace(request, ws);
				//				}
				//				else if (action.equals("updateGroupInfo")) {
				//					String groupInfo = request.getParameter("group_info");
				//
				//					JSONObject new_group = null;
				//					JSONParser parser = new JSONParser();
				//
				//					try {
				//						new_group = (JSONObject) parser.parse(groupInfo);
				//					}
				//					catch (Exception e) {
				//						LOGGER.error(e.getMessage(), e);
				//					}
				//
				//					String new_group_name = null, new_group_desc = null;
				//
				//					if (new_group.containsKey("name")) {
				//						new_group_name = new_group.get("name").toString();
				//					}
				//					if (new_group.containsKey("desc")) {
				//						new_group_desc = new_group.get("desc").toString();
				//					}
				//
				//					ws.updateGroupTag(Integer.parseInt(new_group.get("tagId").toString()), new_group_name, new_group_desc);
				//					saveWorkspace(request, ws);
				//				}
				//				else if (action.equals("updateExperimentInfo")) {
				//					String strExpUpdated = request.getParameter("experiment_info");
				//					String collectionId = null;
				//
				//					JSONObject jsonExpUpdated = null;
				//					JSONParser parser = new JSONParser();
				//
				//					try {
				//						jsonExpUpdated = (JSONObject) parser.parse(strExpUpdated);
				//						collectionId = jsonExpUpdated.get("expid").toString();
				//					}
				//					catch (Exception e) {
				//						LOGGER.error(e.getMessage(), e);
				//					}
				//
				//					PolyomicHandler polyomic = getPolyomicHandler(request);
				//					JSONObject jsonExpOrig = polyomic.getCollection(collectionId, "experiment");
				//
				//					if (jsonExpOrig.containsKey("title") && jsonExpUpdated.containsKey("title")) {
				//						jsonExpOrig.put("title", jsonExpUpdated.get("title").toString());
				//					}
				//					if (jsonExpOrig.containsKey("desc") && jsonExpUpdated.containsKey("desc")) {
				//						jsonExpOrig.put("desc", jsonExpUpdated.get("desc").toString());
				//					}
				//					if (jsonExpOrig.containsKey("organism") && jsonExpUpdated.containsKey("organism")) {
				//						jsonExpOrig.put("organism", jsonExpUpdated.get("organism").toString());
				//					}
				//					if (jsonExpOrig.containsKey("pmid") && jsonExpUpdated.containsKey("pmid")) {
				//						jsonExpOrig.put("pmid", jsonExpUpdated.get("pmid").toString());
				//					}
				//					if (jsonExpUpdated.containsKey("data_type")) {
				//						jsonExpOrig.put("data_type", jsonExpUpdated.get("data_type").toString());
				//					}
				//					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				//					String timestamp = sdf.format(Calendar.getInstance().getTime());
				//					jsonExpOrig.put("mdate", timestamp);
				//
				//					polyomic.saveJSONtoCollection(collectionId, "experiment.json", jsonExpOrig, PolyomicHandler.CONTENT_EXPERIMENT);
				//					polyomic.refreshWorkspaceCollection(collectionId);
				//				}

				response.setContentType("application/json");
				response.getWriter().write("{'success':true}");
				response.getWriter().close();
			}
			else if (action_type.equals("WSSupport")) {
				//				if (action.equals("getLibrary")) {
				//					JSONArray library = new JSONArray();
				//					JSONObject data = new JSONObject();
				//					data.put("id", 1);
				//					data.put("name", "OWNED BY ME");
				//					data.put("expanded", true);
				//					data.put("children", ws.getLibraryByDataType());
				//					library.add(data);
				//
				//					response.setContentType("application/json");
				//					library.writeJSONString(response.getWriter());
				//					response.getWriter().close();
				//				}
				//				else if (action.equals("getGroups")) {
				//					JSONObject rtn = new JSONObject();
				//					JSONArray groups = ws.getGroups();
				//
				//					rtn.put("success", true);
				//					rtn.put("results", groups);
				//
				//					response.setContentType("application/json");
				//					rtn.writeJSONString(response.getWriter());
				//					response.getWriter().close();
				//				}
				//				else if (action.equals("getFacets")) {
				//					JSONObject rtn = new JSONObject();
				//					JSONArray tags = ws.getTags();
				//
				//					rtn.put("success", true);
				//					rtn.put("results", tags);
				//
				//					response.setContentType("application/json");
				//					rtn.writeJSONString(response.getWriter());
				//					response.getWriter().close();
				//				}
				//				else if (action.equals("getTracks")) {
				//					JSONObject rtn = new JSONObject();
				//					JSONArray tags = ws.getTags();
				//
				//					rtn.put("success", true);
				//					rtn.put("results", tags);
				//
				//					response.setContentType("application/json");
				//					rtn.writeJSONString(response.getWriter());
				//					response.getWriter().close();
				//				}
				//				else if (action.equals("getMappings")) {
				//					JSONArray mapping = ws.getMapping();
				//
				//					response.setContentType("application/json");
				//					mapping.writeJSONString(response.getWriter());
				//					response.getWriter().close();
				//				}
				//				else if (action.equals("getGenomes")) {
				//					String trackIds = request.getParameter("trackIds");
				//					JSONObject filter = new JSONObject();
				//					filter.put("key", "trackId");
				//					filter.put("value", trackIds);
				//					JSONArray tracks = ws.getTracks(filter);
				//
				//					Map<String, Object> key = new HashMap<>();
				//					key.put("tracks", tracks);
				//					key.put("startParam", request.getParameter("start"));
				//					key.put("limitParam", request.getParameter("limit"));
				//					key.put("sortParam", request.getParameter("sort"));
				//
				//					SolrInterface solr = new SolrInterface();
				//					JSONObject res = solr.getGenomesByID(key);
				//
				//					response.setContentType("application/json");
				//					PrintWriter writer = response.getWriter();
				//					res.writeJSONString(writer);
				//					writer.close();
				//				}
				//				else if (action.equals("getFeatures")) {
				//					String trackIds = request.getParameter("trackIds");
				//					JSONObject filter = new JSONObject();
				//					filter.put("key", "trackId");
				//					filter.put("value", trackIds);
				//					JSONArray tracks;
				//
				//					tracks = ws.getTracks(filter);
				//
				//					Map<String, Object> key = new HashMap<>();
				//					key.put("tracks", tracks);
				//					key.put("startParam", request.getParameter("start"));
				//					key.put("limitParam", request.getParameter("limit"));
				//					key.put("sortParam", request.getParameter("sort"));
				//
				//					SolrInterface solr = new SolrInterface();
				//					JSONObject res = solr.getFeaturesByID(key);
				//
				//					response.setContentType("application/json");
				//					PrintWriter writer = response.getWriter();
				//					res.writeJSONString(writer);
				//					writer.close();
				//				}
				//				else if (action.equals("getPublicExperiments")) {
				//					JSONArray collectionIDs = null;
				//					DefaultHttpClient httpclient = new DefaultHttpClient();
				//					String url = "http://" + request.getServerName() + ":" + request.getServerPort() + "/patric/static/publicworkspace.json";
				//					LOGGER.debug(url);
				//					HttpGet httpRequest = new HttpGet(url);
				//					try {
				//						ResponseHandler<String> responseHandler = new BasicResponseHandler();
				//						String strResponseBody = httpclient.execute(httpRequest, responseHandler);
				//
				//						JSONParser parser = new JSONParser();
				//						collectionIDs = (JSONArray) parser.parse(strResponseBody);
				//					}
				//					catch (IOException | ParseException e) {
				//						LOGGER.error(e.getMessage(), e);
				//					}
				//					finally {
				//						httpclient.getConnectionManager().shutdown();
				//					}
				//
				//					/*
				//					 *
				//					 */
				//					JSONArray results = new JSONArray();
				//
				//					PolyomicHandler polyomic = new PolyomicHandler();
				//					polyomic.setAuthenticationToken("");
				//
				//					// for (String cId : collectionIds) {
				//					for (Object cId : collectionIDs) {
				//						JSONObject collection = polyomic.getCollection(cId.toString(), "experiment");
				//						if (collection != null) {
				//							collection.put("source", "Public");
				//
				//							results.add(collection);
				//						}
				//					}
				//
				//					JSONObject res = new JSONObject();
				//					int totalUser = results.size();
				//
				//					res.put("total", totalUser);
				//					res.put("results", results);
				//
				//					response.setContentType("application/json");
				//					PrintWriter writer = response.getWriter();
				//					res.writeJSONString(writer);
				//					writer.close();
				//				}
				//				else if (action.equals("getPublicSamples")) {
				//					String expId = request.getParameter("expid");
				//					// String expSource = request.getParameter("expsource");
				//					String strSampleIds = request.getParameter("sampleIds");
				//					JSONObject res = new JSONObject();
				//					List<String> sampleIds = null;
				//					if (strSampleIds != null) {
				//						sampleIds = Arrays.asList(strSampleIds.split(","));
				//					}
				//
				//					PolyomicHandler polyomic = new PolyomicHandler();
				//					polyomic.setAuthenticationToken("");
				//					JSONArray samples = polyomic.getSamples(expId, sampleIds);
				//					res.put("total", samples.size());
				//					res.put("results", samples);
				//
				//					response.setContentType("application/json");
				//					PrintWriter writer = response.getWriter();
				//					res.writeJSONString(writer);
				//					writer.close();
				//				}
				//				else if (action.equals("getExperiments")) {
				//					String trackIds = request.getParameter("trackIds");
				//					JSONObject filter = new JSONObject();
				//					filter.put("key", "trackId");
				//					filter.put("value", trackIds);
				//					JSONArray tracksMixed = ws.getTracks(filter);
				//					JSONArray tracksPATRIC = new JSONArray();
				//
				//					List<String> collectionIds = new ArrayList<>();
				//
				//					for (Object tr : tracksMixed) {
				//						JSONObject jsonTrk = (JSONObject) tr;
				//
				//						try {
				//							Integer.parseInt(jsonTrk.get("internalId").toString());
				//							tracksPATRIC.add(jsonTrk);
				//						}
				//						catch (NumberFormatException nfe) {
				//							collectionIds.add(jsonTrk.get("internalId").toString());
				//						}
				//					}
				//
				//					// reading USER Experiments
				//					PolyomicHandler polyomic;
				//					JSONObject resUser = null;
				//					if (request.getUserPrincipal() != null) {
				//						polyomic = getPolyomicHandler(request);
				//						resUser = polyomic.getExperiments(collectionIds);
				//					}
				//
				//					// reading PATRIC Experiments
				//					Map<String, Object> key = new HashMap<>();
				//					key.put("tracks", tracksPATRIC);
				//					key.put("startParam", request.getParameter("start"));
				//					key.put("limitParam", request.getParameter("limit"));
				//					if (request.getParameter("sort") != null
				//							&& (request.getParameter("sort").contains("\"property\":\"source\"") || request.getParameter("sort").contains(
				//							"\"property\":\"organism\""))) {
				//						// solr does not support sorting on multi-valued fields
				//						// source fields does not exist in solr config
				//						key.put("sortParam", null);
				//					}
				//					else {
				//						key.put("sortParam", request.getParameter("sort"));
				//					}
				//					SolrInterface solr = new SolrInterface();
				//					JSONObject resPATRIC = solr.getExperimentsByID(key);
				//
				//					// merging
				//					JSONObject res = new JSONObject();
				//					int totalPATRIC = 0;
				//					if (resPATRIC.containsKey("total")) {
				//						totalPATRIC = Integer.parseInt(resPATRIC.get("total").toString());
				//					}
				//					int totalUser = 0;
				//					if (resUser != null && resUser.containsKey("total")) {
				//						totalUser = Integer.parseInt(resUser.get("total").toString());
				//					}
				//					JSONArray results = new JSONArray();
				//					if (resPATRIC.containsKey("results")) {
				//						for (Object exp : (JSONArray) resPATRIC.get("results")) {
				//							JSONObject jsonExp = (JSONObject) exp;
				//							jsonExp.put("source", "PATRIC");
				//							results.add(jsonExp);
				//						}
				//					}
				//					if (resUser != null && resUser.containsKey("results")) {
				//						for (Object exp : (JSONArray) resUser.get("results")) {
				//							JSONObject jsonExp = (JSONObject) exp;
				//							jsonExp.put("source", "me");
				//							results.add(jsonExp);
				//						}
				//					}
				//
				//					res.put("total", totalPATRIC + totalUser);
				//					res.put("results", results);
				//
				//					response.setContentType("application/json");
				//					PrintWriter writer = response.getWriter();
				//					res.writeJSONString(writer);
				//					writer.close();
				//				}
				//				else if (action.equals("getSamples")) {
				//					String expId = request.getParameter("expid");
				//					String expSource = request.getParameter("expsource");
				//					String strSampleIds = request.getParameter("sampleIds");
				//					JSONObject res = new JSONObject();
				//					List<String> sampleIds = null;
				//					if (strSampleIds != null) {
				//						sampleIds = Arrays.asList(strSampleIds.split(","));
				//					}
				//
				//					if (expSource.equals("User")) {
				//						PolyomicHandler polyomic = getPolyomicHandler(request);
				//						JSONArray samples = polyomic.getSamples(expId, sampleIds);
				//						res.put("total", samples.size());
				//						res.put("results", samples);
				//					}
				//					else if (expSource.equals("PATRIC")) {
				//						SolrInterface solr = new SolrInterface();
				//						solr.setCurrentInstance(SolrCore.TRANSCRIPTOMICS_EXPERIMENT);
				//						ResultType rtKey = new ResultType();
				//						rtKey.put("keyword", "expid:" + expId);
				//						rtKey.put("fields", "eid,expid,accession,institution,pi,author,pmid,title,organism,strain,mutant,timeseries,condition,samples,platform,genes");
				//						JSONObject object = solr.getData(rtKey, null, null, 0, 10000, false, false, false);
				//
				//						JSONObject obj = (JSONObject) object.get("response");
				//						JSONArray obj1 = (JSONArray) obj.get("docs");
				//						String solrId = "";
				//
				//						for (Object ob : obj1) {
				//							JSONObject doc = (JSONObject) ob;
				//							if (solrId.length() == 0) {
				//								solrId += doc.get("eid").toString();
				//							}
				//							else {
				//								solrId += "," + doc.get("eid").toString();
				//							}
				//						}
				//
				//						ResultType key = new ResultType();
				//						key.put("keyword", "eid:" + solrId);
				//						key.put("fields", "eid,expid,accession,pid,samples,expname,channels,platform,pmid,organism,strain,mutant,condition,timepoint,expmean,expstddev,genes,sig_log_ratio,sig_z_score");
				//						solr.setCurrentInstance(SolrCore.TRANSCRIPTOMICS_COMPARISON);
				//						JSONObject samples = solr.getData(key, null, null, 0, 10000, false, false, false);
				//						obj = (JSONObject) samples.get("response");
				//
				//						JSONArray rtnDocs = new JSONArray();
				//						for (Object ob : (JSONArray) obj.get("docs")) {
				//							JSONObject tuple = (JSONObject) ob;
				//							tuple.put("source", "PATRIC");
				//							rtnDocs.add(tuple);
				//						}
				//
				//						res.put("total", obj.get("numFound"));
				//						res.put("results", rtnDocs);
				//					}
				//
				//					response.setContentType("application/json");
				//					PrintWriter writer = response.getWriter();
				//					res.writeJSONString(writer);
				//					writer.close();
				//				}
				//				else if (action.equals("getToken")) {
				//					String token;
				//
				//					if (request.getUserPrincipal() != null) {
				//						PolyomicHandler polyomic = getPolyomicHandler(request);
				//						token = polyomic.getAuthenticationToken();
				//					}
				//					else {
				//						token = "";
				//					}
				//					response.setContentType("text/plain");
				//					PrintWriter writer = response.getWriter();
				//					writer.write(token);
				//					writer.close();
				//				}
				//				else
				if (action.equals("inlinestatus")) {

					String linkWorkspace = "";
					if (isLoggedIn(request)) {
						linkWorkspace = "<a class=\"arrow-white-e\" href=\"/workspace/\">WORKSPACE: HOME</a>";
					}

					response.setContentType("text/plain");
					PrintWriter writer = response.getWriter();
					writer.write(linkWorkspace);
					writer.close();
				}
				else if (action.equals("getGenomeGroupList")) {
					JSONArray res = new JSONArray();

					if (isLoggedIn(request)) {
						String token = getAuthorizationToken(request);
						String pathGenomeGroup = getUserWorkspacePath(request, DEFAULT_WORKSPACE_NAME) + "/Genome Groups";

						try {
							SolrInterface solr = new SolrInterface();
							Workspace serviceWS = new Workspace(WORKSPACE_API_URL, token);
							list_params params = new list_params();
							params.paths = Arrays.asList(pathGenomeGroup);
							Map<String, List<ObjectMeta>> resp = serviceWS.ls(params);

							List<ObjectMeta> groupList = resp.get(pathGenomeGroup);

							JSONParser jsonParser = new JSONParser();

							for (ObjectMeta group : groupList) {
								JSONObject grp = new JSONObject();
								String genomeGroupId = group.e_5;  // e_5, object id
								grp.put("id", genomeGroupId);
								grp.put("name", group.e_1); // e_1, object name
								grp.put("leaf", false);
								grp.put("collapsed", true);

								JSONArray children = new JSONArray();
								// get genome associated in this group
								get_params gp = new get_params();
								gp.objects = Arrays.asList(group.e_3 + group.e_1); // e_3, path
								gp.metadata_only = 0;
								gp.adminmode = 0;

								//								LOGGER.debug("requesting.. {}", group.e_3 + group.e_1);
								List<Workspace_tuple_2> r = serviceWS.get(gp);

								for (Workspace_tuple_2 item : r) {
									if (item.e_2 != null) {

										JSONObject groupInfo = (JSONObject) jsonParser.parse(item.e_2); // objectMeta
										List<String> genomeIdList = (List<String>) ((JSONObject) groupInfo.get("id_list")).get("genome_id");
										Set<String> genomeIdSet = new HashSet<>(genomeIdList);
										genomeIdSet.remove("");

										SolrQuery query = new SolrQuery("genome_id:(" + StringUtils.join(genomeIdSet, " OR ") + ")");
										query.setRows(10000).addField("genome_id,genome_name,taxon_id");

										//										LOGGER.debug("{}", query.toString());

										QueryResponse qr = solr.getSolrServer(SolrCore.GENOME).query(query, SolrRequest.METHOD.POST);
										List<Genome> genomes = qr.getBeans(Genome.class);
										Map<String, Genome> genomeHash = new LinkedHashMap<>();
										for (Genome g : genomes) {
											genomeHash.put(g.getId(), g);
										}

										for (String genomeId : genomeIdSet) {
											Genome genome = genomeHash.get(genomeId);

											JSONObject leafGenome = new JSONObject();
											leafGenome.put("id", genomeId);
											leafGenome.put("parentID", genomeGroupId);
											leafGenome.put("name", genome.getGenomeName());
											leafGenome.put("leaf", true);
											leafGenome.put("genome_id", genomeId);
											leafGenome.put("taxon_id", genome.getTaxonId());

											children.add(leafGenome);
										}
									}
								}

								grp.put("children", children);
								res.add(grp);
							}
						}
						catch (Exception e) {
							LOGGER.error(e.getMessage(), e);
						}
					}

					//					JSONObject filters = new JSONObject();
					//					filters.put("key", "type");
					//					filters.put("value", "Genome");
					//
					//					JSONArray groups = ws.getGroups(filters);
					//					for (int id = 0; id < groups.size(); id++) {
					//						JSONObject group = (JSONObject) groups.get(id);
					//						grp = new JSONObject();
					//						grp.put("id", Integer.toString(id));
					//						grp.put("name", group.get("name").toString());
					//						grp.put("leaf", false);
					//						grp.put("collapsed", true);
					//						// get genomes associted in this group
					//						int tagId = Integer.parseInt(group.get("tagId").toString());
					//						List<JSONObject> members = ws.findMappingByTagId(tagId);
					//
					//						Set<Integer> trackIds = new HashSet<>();
					//						for (JSONObject member : members) {
					//							trackIds.add(Integer.parseInt(member.get("trackId").toString()));
					//						}
					//
					//						JSONArray tracks = ws.getTracks(trackIds);
					//
					//						Map<String, Object> key = new HashMap<>();
					//						key.put("tracks", tracks);
					//
					//						SolrInterface solr = new SolrInterface();
					//						JSONObject solr_res = solr.getGenomesByID(key);
					//						JSONArray genomes = (JSONArray) solr_res.get("results");
					//
					//						JSONArray children = new JSONArray();
					//						if (genomes != null) {
					//							for (Object genome : genomes) {
					//								JSONObject jsonGenome = (JSONObject) genome;
					//								JSONObject resGenome = new JSONObject();
					//								resGenome.put("id", Integer.toString(id) + "_" + jsonGenome.get("genome_id").toString());
					//								resGenome.put("parentID", Integer.toString(id));
					//								resGenome.put("name", jsonGenome.get("genome_name").toString());
					//								resGenome.put("leaf", true);
					//								resGenome.put("genome_id", jsonGenome.get("genome_id"));
					//								resGenome.put("taxon_id", Integer.parseInt(jsonGenome.get("taxon_id").toString()));
					//
					//								children.add(resGenome);
					//							}
					//						}
					//						grp.put("children", children);
					//						res.add(grp);
					//					}

					response.setContentType("application/json");
					PrintWriter writer = response.getWriter();
					res.writeJSONString(writer);
					writer.close();
				}
				else if (action.equals("getGroupList")) {
					String grp_type = request.getParameter("group_type");
					JSONArray res = new JSONArray();

					String grp_path = getUserWorkspacePath(request, DEFAULT_WORKSPACE_NAME);
					switch (grp_type) {
					case "Genome":
						grp_path += "/Genome Groups";
						break;
					case "Feature":
						grp_path += "/Feature Groups";
						break;
					case "ExpressionExperiment":
						grp_path += "/Experiment Groups";
						break;
					default:
						//
					}

					String token = getAuthorizationToken(request);
					Workspace serviceWS = new Workspace(WORKSPACE_API_URL, token);
					list_params params = new list_params();
					params.paths = Arrays.asList(grp_path);

					try {
						Map<String, List<ObjectMeta>> resp = serviceWS.ls(params);
						List<ObjectMeta> groupList = resp.get(grp_path);

						for (ObjectMeta group : groupList) {
							String groupId = group.e_5;  // e_5, object id
							String groupName = group.e_1; // e_1, object name
							// TODO: how to read group description???

							JSONObject grp = new JSONObject();
							grp.put("name", groupName);
							grp.put("description", "");
							grp.put("tag", "");
							grp.put("id", groupId);

							res.add(grp);
						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}

					//					JSONObject filters = new JSONObject();
					//					filters.put("key", "type");
					//					filters.put("value", grp_type);
					// TODO: implement with Workspac API
					//					JSONArray groups = ws.getGroups(filters);
					//
					//					for (int id = 0; id < groups.size(); id++) {
					//						JSONObject group = (JSONObject) groups.get(id);
					//						grp = new JSONObject();
					//
					//						grp.put("name", group.get("name").toString());
					//						grp.put("description", group.get("desc").toString());
					//
					//						List<JSONObject> mapping_trks = ws.findMappingByTagId(Integer.parseInt(group.get("tagId").toString()));
					//						List<JSONObject> mapping_tags;
					//						Set<Integer> tagIDs = new HashSet<>();
					//
					//						for (JSONObject track : mapping_trks) {
					//							mapping_tags = ws.findMappingByTrackId(Integer.parseInt(track.get("trackId").toString()));
					//
					//							for (JSONObject tag : mapping_tags) {
					//								tagIDs.add(Integer.parseInt(tag.get("tagId").toString()));
					//							}
					//						}
					//						String strTags = "";
					//						for (int tagId : tagIDs) {
					//							JSONObject t = ws.findTagByTagId(tagId);
					//							if (t.get("tagType").equals("String")) {
					//								if (strTags.length() > 0) {
					//									strTags += ",";
					//								}
					//								strTags += t.get("name").toString();
					//							}
					//						}
					//						grp.put("tags", strTags);
					//						res.add(grp);
					//					}

					response.setContentType("application/json");
					PrintWriter writer = response.getWriter();
					res.writeJSONString(writer);
					writer.close();
				}
				// for debugging purpose
				//				else if (action.equals("status")) {
				//					response.setContentType("application/json");
				//					PrintWriter writer = response.getWriter();
				//					ws.getWorkspace().writeJSONString(writer);
				//					writer.close();
				//				}
				else {
					response.setContentType("application/json");
					response.getWriter().write("sorry");
					response.getWriter().close();
				}
			}
			else if (action_type.equals("GSESupport")) {
				//				if (action.equals("group_list")) {
				//					// PersistentCartGroup group = null;
				//					JSONArray groups = ws.getGroups();
				//					StringBuilder output = new StringBuilder();
				//
				//					output.append("<group_set>\n");
				//					for (Object aGroup : groups) {
				//						JSONObject group = (JSONObject) aGroup;
				//
				//						output.append("\t<group>\n");
				//						output.append("\t\t<idx>").append(group.get("tagId")).append("</idx>\n");
				//						output.append("\t\t<name>").append(group.get("name")).append("</name>\n");
				//						output.append("\t</group>\n");
				//					}
				//					output.append("</group_set>");
				//
				//					response.setContentType("text/xml");
				//					response.getWriter().write(output.toString());
				//					response.getWriter().close();
				//				}
				//				else if (action.equals("groups")) {
				//					String strTagIds = request.getParameter("groupIds");
				//					JSONObject filter = new JSONObject();
				//					filter.put("key", "tagId");
				//					filter.put("value", strTagIds);
				//
				//					JSONArray groups = ws.getGroups(filter);
				//					StringBuilder o = new StringBuilder();
				//
				//					o.append("<group_set>\n");
				//
				//					for (Object aGroup : groups) {
				//						JSONObject group = (JSONObject) aGroup;
				//
				//						o.append("\t<group>\n");
				//						o.append("\t\t<name>").append(group.get("name")).append("</name>\n");
				//						o.append("\t\t<description>").append(((group.get("desc") != null) ? group.get("desc") : "")).append("</description>\n");
				//						o.append("\t\t<members>\n");
				//
				//						List<JSONObject> members = ws.findMappingByTagId(Integer.parseInt(group.get("tagId").toString()));
				//
				//						Set<Integer> trackIds = new HashSet<>();
				//						for (JSONObject member : members) {
				//							trackIds.add(Integer.parseInt(member.get("trackId").toString()));
				//						}
				//						JSONArray tracks = ws.getTracks(trackIds);
				//						for (Object track : tracks) {
				//							JSONObject member = (JSONObject) track;
				//							o.append(member.get("internalId")).append("\n");
				//						}
				//
				//						o.append("\t\t</members>\n");
				//						o.append("\t</group>\n");
				//					}
				//					o.append("</group_set>");
				//
				//					//
				//					response.setContentType("text/xml");
				//					response.getWriter().write(o.toString());
				//					response.getWriter().close();
				//				}
				//				else if (action.equals("items")) {
				//
				//					Set<Integer> trackIds = new HashSet<>();
				//					String strTagIds = request.getParameter("groupIds");
				//					String groupType;
				//					String _tagId = null;
				//
				//					if (strTagIds.contains(",")) {
				//						for (String tagId : strTagIds.split(",")) {
				//							List<JSONObject> mappings = ws.findMappingByTagId(Integer.parseInt(tagId));
				//							for (JSONObject mapping : mappings) {
				//								trackIds.add(Integer.parseInt(mapping.get("trackId").toString()));
				//							}
				//							_tagId = tagId;
				//						}
				//					}
				//					else {
				//						List<JSONObject> mappings = ws.findMappingByTagId(Integer.parseInt(strTagIds));
				//						for (JSONObject mapping : mappings) {
				//							trackIds.add(Integer.parseInt(mapping.get("trackId").toString()));
				//						}
				//						_tagId = strTagIds;
				//					}
				//
				//					JSONArray tracks = ws.getTracks(trackIds);
				//
				//					// get group type
				//					JSONObject filter = new JSONObject();
				//					filter.put("key", "tagId");
				//					filter.put("value", _tagId);
				//					JSONArray gr = ws.getTags(filter);
				//					groupType = ((JSONObject) gr.get(0)).get("type").toString();
				//
				//					SolrInterface solr = new SolrInterface();
				//					JSONObject res;
				//					Map<String, Object> key = new HashMap<>();
				//					key.put("tracks", tracks);
				//
				//					StringBuilder out_sb = new StringBuilder();
				//
				//					if (groupType.equals("Feature")) {
				//						res = solr.getFeaturesByID(key);
				//						JSONArray items = (JSONArray) res.get("results");
				//
				//						out_sb.append(
				//								"Feature ID\tGenome Name\tAccession\tPATRIC ID\tRefSeq Locus Tag\tAlt Locus Tag\tAnnotation\tFeature Type\tStart\tEnd\tLength(NT)\tStrand\t");
				//						out_sb.append("Protein Id\tLength(AA)\tGene Symbol\tProduct\n");
				//
				//						for (Object aItem : items) {
				//							JSONObject item = (JSONObject) aItem;
				//							out_sb.append(item.get("feature_id")).append("\t");
				//							out_sb.append(item.get("genome_name")).append("\t");
				//							out_sb.append(item.get("accession")).append("\t");
				//							out_sb.append(item.get("seed_id")).append("\t");
				//							out_sb.append((item.get("refseq_locus_tag") != null ? item.get("refseq_locus_tag") : "")).append("\t");
				//							out_sb.append((item.get("alt_locus_tag") != null ? item.get("alt_locus_tag") : "")).append("\t");
				//							out_sb.append(item.get("annotation")).append("\t");
				//							out_sb.append(item.get("feature_type")).append("\t");
				//							out_sb.append(item.get("start")).append("\t");
				//							out_sb.append(item.get("end")).append("\t");
				//							out_sb.append(item.get("na_length")).append("\t");
				//							out_sb.append(item.get("strand")).append("\t");
				//							out_sb.append((item.get("protein_id") != null ? item.get("protein_id") : "")).append("\t");
				//							out_sb.append(item.get("aa_length")).append("\t");
				//							out_sb.append((item.get("gene") != null ? item.get("gene") : "")).append("\t");
				//							out_sb.append(item.get("product")).append("\n");
				//						}
				//					}
				//					else if (groupType.equals("Genome")) {
				//						res = solr.getGenomesByID(key);
				//						JSONArray items = (JSONArray) res.get("results");
				//
				//						out_sb.append("Genome ID\tGenome Name\tStatus\tHost\tDisease\tIsolation Country\tCollection Date\tCompletion Date\n");
				//
				//						for (Object aItem : items) {
				//							JSONObject item = (JSONObject) aItem;
				//							out_sb.append(item.get("genome_id")).append("\t");
				//							out_sb.append(item.get("genome_name")).append("\t");
				//							out_sb.append(item.get("genome_status")).append("\t");
				//							out_sb.append((item.get("host_name") != null ? item.get("host_name") : "")).append("\t");
				//							out_sb.append((item.get("disease") != null ? item.get("disease") : "")).append("\t");
				//							out_sb.append((item.get("isolation_country") != null ? item.get("isolation_country") : "")).append("\t");
				//							out_sb.append((item.get("collection_date") != null ? item.get("collection_date") : "")).append("\t");
				//							out_sb.append((item.get("completion_date") != null ? item.get("completion_date") : "")).append("\n");
				//						}
				//					}
				//					else if (groupType.equals("ExpressionExperiment")) {
				//						List<String> collectionIds = new ArrayList<>();
				//
				//						JSONArray exptracks;
				//						JSONArray tracksPATRIC = new JSONArray();
				//
				//						if (key.containsKey("tracks")) {
				//							exptracks = (JSONArray) key.get("tracks");
				//
				//							if (exptracks.size() > 0) {
				//								for (Object exptrack : exptracks) {
				//									JSONObject tr = (JSONObject) exptrack;
				//									try {
				//										Integer.parseInt(tr.get("internalId").toString());
				//										tracksPATRIC.add(tr);
				//									}
				//									catch (NumberFormatException nfe) {
				//										collectionIds.add(tr.get("internalId").toString());
				//									}
				//								}
				//							}
				//						}
				//
				//						out_sb.append("Experiment Id\tSource\tTitle\tData Type\tAccession\n");
				//
				//						if (tracksPATRIC.size() > 0) {
				//							Map<String, Object> keyPATRIC = new HashMap<>();
				//							keyPATRIC.put("tracks", tracksPATRIC);
				//							res = solr.getExperimentsByID(keyPATRIC);
				//
				//							JSONArray items = (JSONArray) res.get("results");
				//							for (Object aItem : items) {
				//								JSONObject item = (JSONObject) aItem;
				//								out_sb.append(item.get("expid")).append("\t");
				//								out_sb.append("PATRIC\t");
				//								out_sb.append(item.get("title")).append("\t");
				//								out_sb.append("Transcriptomics\t"); // TODO: modify later
				//								out_sb.append((item.get("accession") != null ? item.get("accession") : "")).append("\n");
				//							}
				//						}
				//
				//						if (collectionIds.size() > 0) {
				//							PolyomicHandler polyomic = getPolyomicHandler(request);
				//							res = polyomic.getExperiments(collectionIds);
				//
				//							JSONArray items = (JSONArray) res.get("results");
				//							for (Object aItem : items) {
				//								JSONObject item = (JSONObject) aItem;
				//								out_sb.append(item.get("expid")).append("\t");
				//								out_sb.append("me\t");
				//								out_sb.append(item.get("title")).append("\t");
				//								out_sb.append((item.get("data_type") != null ? item.get("data_type") : "")).append("\t");
				//								out_sb.append((item.get("accession") != null ? item.get("accession") : "")).append("\n");
				//							}
				//						}
				//					}
				//					else {
				//						// error
				//					}
				//
				//					response.setContentType("text/plain");
				//					response.getWriter().write(out_sb.toString());
				//					response.getWriter().close();
				//				}
			}
			else if (action_type.equals("LoginStatus")) {
				if (action.equals("getLoginStatus")) {
					if (!isLoggedIn(request)) {
						response.setContentType("text/plain");
						response.getWriter().write("false");
						response.getWriter().close();
					}
					else {
						response.setContentType("text/plain");
						response.getWriter().write("true");
						response.getWriter().close();
					}
				}
			}
			//			else if (action_type.equals("PopupShowedStatus")) {
			//				PortletSession session = request.getPortletSession(true);
			//				if (action.equals("getPopupStatus")) {
			//					String popupshowed = (String) session.getAttribute("popupshowed", PortletSession.APPLICATION_SCOPE);
			//					if (popupshowed == null) {
			//						popupshowed = "false";
			//					}
			//					response.setContentType("text/plain");
			//					response.getWriter().write(popupshowed);
			//					response.getWriter().close();
			//				}
			//				else if (action.equals("setPopupStatus")) {
			//					session.setAttribute("popupshowed", "ture", PortletSession.APPLICATION_SCOPE);
			//					response.setContentType("text/plain");
			//					response.getWriter().write("true");
			//					response.getWriter().close();
			//				}
			//			}
			else if (action_type.equals("HTTPProvider")) {

				UIPreference uiPref = getValidUIPreference(request);

				switch (action) {
				case "storage":
					if (request.getMethod().equals("GET")) {

						String strUIPref = uiPref.getStateList().toJSONString();

						response.getWriter().write(strUIPref);
						response.getWriter().close();
					}
					else if (request.getMethod().equals("POST")) {

						JSONParser parser = new JSONParser();
						JSONObject param;
						JSONArray params;
						try {
							Object rt = parser.parse(request.getReader());
							if (rt instanceof JSONObject) {
								param = (JSONObject) rt;
								uiPref.setState(param);
							}
							else if (rt instanceof JSONArray) {
								params = (JSONArray) rt;
								uiPref.setStateList(params);
							}
							else {
								LOGGER.error(rt.toString());
							}

							this.saveUIPreference(request, uiPref);
						}
						catch (ParseException e) {
							LOGGER.error(e.getMessage(), e);
						}

						response.getWriter().write("");
						response.getWriter().close();
					}
					break;
				case "remove":
					if (request.getParameter("name") != null) {
						uiPref.resetState(request.getParameter("name"));
						this.saveUIPreference(request, uiPref);
					}

					response.getWriter().write("");
					response.getWriter().close();
					break;
				case "reset":
					uiPref.reset();
					this.saveUIPreference(request, uiPref);

					response.getWriter().write("");
					response.getWriter().close();
					break;
				}
			}
		}
	}

	public boolean isLoggedIn(PortletRequest request) {
		boolean isLoggedIn = false;

		PortletSession session = request.getPortletSession(true);

		if (session.getAttribute("authorizationToken", PortletSession.APPLICATION_SCOPE) != null) {
			isLoggedIn = true;
		}

		return isLoggedIn;
	}

	public String getUserId(PortletRequest request) {
		String userId = null;

		PortletSession session = request.getPortletSession(true);

		Map<String, Object> profile = (Map) session.getAttribute("userProfile", PortletSession.APPLICATION_SCOPE);
		if (profile != null) {
			userId = profile.get("id").toString().replace("@patricbrc.org", "");
		}

		return userId;
	}

	public String getAuthorizationToken(PortletRequest request) {
		String token = null;

		PortletSession session = request.getPortletSession(true);

		if (session.getAttribute("authorizationToken", PortletSession.APPLICATION_SCOPE) != null) {
			token = (String) session.getAttribute("authorizationToken", PortletSession.APPLICATION_SCOPE);
		}

		return token;
	}

	public String getUserWorkspacePath(PortletRequest request, String defaultWorkspaceName) {
		String path = null;
		PortletSession session = request.getPortletSession(true);

		Map<String, Object> profile = (Map) session.getAttribute("userProfile", PortletSession.APPLICATION_SCOPE);
		if (profile != null) {
			path = "/" + profile.get("id").toString() + defaultWorkspaceName;
		}

		return path;
	}
}
