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

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import edu.vt.vbi.patric.beans.Genome;
import edu.vt.vbi.patric.common.SessionHandler;
import edu.vt.vbi.patric.common.SolrCore;
import edu.vt.vbi.patric.common.SolrInterface;
import edu.vt.vbi.patric.common.UIPreference;
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

	private String WORKSPACE_API_URL;

	private String DEFAULT_WORKSPACE_NAME;

	@Override
	public void init() throws PortletException {

		WORKSPACE_API_URL = System.getProperty("workspaceServiceURL", "http://p3.theseed.org/services/Workspace");
		DEFAULT_WORKSPACE_NAME = "/home"; // for Mar 2015 release
		super.init();
	}

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		// do nothing
	}



	private UIPreference getValidUIPreference(ResourceRequest request) {

		UIPreference uiPref = null;
		Gson gson = new Gson();

		if (!isLoggedIn(request)) {

			PortletSession p_session = request.getPortletSession(true);
			UIPreference uiPref_from_session = gson
					.fromJson((String) p_session.getAttribute("preference", PortletSession.APPLICATION_SCOPE), UIPreference.class);

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

				List<Workspace_tuple_2> r = serviceWS.get(gp);

				for (Workspace_tuple_2 item : r) {
					if (item.e_2 != null) {
						LOGGER.trace("reading preference from Workspace: {}", item.e_2);
						uiPref = gson.fromJson(item.e_2, UIPreference.class);
					}
				}

				if (uiPref == null) {
					uiPref = new UIPreference();
				}
			}
			catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
		return uiPref;
	}

	public void saveUIPreference(ResourceRequest request, UIPreference uiPref) {
		Gson gson = new Gson();
		String jsonUiPref = gson.toJson(uiPref, uiPref.getClass());
		LOGGER.trace("saving UIPreference: {}", jsonUiPref);

		if (isLoggedIn(request)) {
			String token = getAuthorizationToken(request);
			String path = getUserWorkspacePath(request, DEFAULT_WORKSPACE_NAME) + "/.preferences.json";

			try {
				Workspace serviceWS = new Workspace(WORKSPACE_API_URL, token);
				create_params cp = new create_params();
				Workspace_tuple_1 tuple = new Workspace_tuple_1();
				tuple.e_1 = path;
				tuple.e_2 = "unspecified";
				tuple.e_4 = jsonUiPref;
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
		}
		else {
			PortletSession p_session = request.getPortletSession(true);
			p_session.setAttribute("preference", jsonUiPref, PortletSession.APPLICATION_SCOPE);
		}
	}

	@SuppressWarnings("unchecked")
	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
		String action_type = request.getParameter("action_type");
		String action = request.getParameter("action");

		if (action_type != null && action != null) {

			if (action_type.equals("WSSupport")) {

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
								LOGGER.trace("reading: {},{},{},{},{},{},{}", group.e_1, group.e_2, group.e_3, group.e_5, group.e_7, group.e_8, group.e_9);

								if ("genome_group".equals(group.e_2)) {
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

									LOGGER.trace("requesting.. {}", group.e_3 + group.e_1);
									List<Workspace_tuple_2> r = serviceWS.get(gp);

									for (Workspace_tuple_2 item : r) {
										if (item.e_2 != null) {

											JSONObject groupInfo = (JSONObject) jsonParser.parse(item.e_2); // objectMeta
											List<String> genomeIdList = (List<String>) ((JSONObject) groupInfo.get("id_list")).get("genome_id");
											Set<String> genomeIdSet = new HashSet<>(genomeIdList);
											genomeIdSet.remove("");

											SolrQuery query = new SolrQuery("genome_id:(" + StringUtils.join(genomeIdSet, " OR ") + ")");
											query.setRows(10000).addField("genome_id,genome_name,taxon_id");

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
						}
						catch (Exception e) {
							LOGGER.error(e.getMessage(), e);
						}
					}

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

					response.setContentType("application/json");
					PrintWriter writer = response.getWriter();
					res.writeJSONString(writer);
					writer.close();
				}
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

		String sessionId = request.getPortletSession(true).getId();
		Gson gson = new Gson();
		LinkedTreeMap sessionMap = gson.fromJson(SessionHandler.getInstance().get(sessionId), LinkedTreeMap.class);

		return sessionMap.containsKey("authorizationToken");
	}

	public String getUserId(PortletRequest request) {
		String userId = null;

		String sessionId = request.getPortletSession(true).getId();
		Gson gson = new Gson();
		LinkedTreeMap sessionMap = gson.fromJson(SessionHandler.getInstance().get(sessionId), LinkedTreeMap.class);
		Map<String, Object> profile = (Map) sessionMap.get("userProfile");
		if (profile != null) {
			userId = profile.get("id").toString().replace("@patricbrc.org", "");
		}

		return userId;
	}

	public String getAuthorizationToken(PortletRequest request) {
		String token = null;

		String sessionId = request.getPortletSession(true).getId();
		Gson gson = new Gson();
		LinkedTreeMap sessionMap = gson.fromJson(SessionHandler.getInstance().get(sessionId), LinkedTreeMap.class);

		if (sessionMap.containsKey("authorizationToken")) {
			token = (String) sessionMap.get("authorizationToken");
		}

		return token;
	}

	public String getUserWorkspacePath(PortletRequest request, String defaultWorkspaceName) {
		String path = null;
		String sessionId = request.getPortletSession(true).getId();
		Gson gson = new Gson();
		LinkedTreeMap sessionMap = gson.fromJson(SessionHandler.getInstance().get(sessionId), LinkedTreeMap.class);
		Map<String, Object> profile = (Map) sessionMap.get("userProfile");
		if (profile != null) {
			path = "/" + profile.get("id").toString() + defaultWorkspaceName;
		}

		return path;
	}
}
