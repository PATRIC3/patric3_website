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
package edu.vt.vbi.patric.jbrowse;

import edu.vt.vbi.patric.common.StringHelper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.Serializable;
import java.util.*;

public class CRResultSet implements Serializable {

	private static final long serialVersionUID = 4687109777629511114L;

	private static final Logger LOGGER = LoggerFactory.getLogger(CRResultSet.class);

	private String pinStrand;

	private String pinGenome;

	private Set<String> genomeNames;

	private Set<String> genomeIds;

	private List<String> defaultTracks;

	private Map<Integer, CRTrack> trackMap;

	public CRResultSet(String pin, BufferedReader br) {
		pinGenome = pin.replace("fig|", "").split(".peg.[0-9]*")[0];

		try {
			if (br != null) {
				JSONObject res = (JSONObject) (new JSONParser()).parse(br);

				JSONArray tracks = (JSONArray) res.get(pin);
				genomeNames = new HashSet<>();
				genomeIds = new HashSet<>();
				defaultTracks = new ArrayList<>();
				trackMap = new HashMap<>();

				for (Object track : tracks) {
					JSONObject tr = (JSONObject) track;

					CRTrack crTrk = new CRTrack(tr);
					trackMap.put(crTrk.getRowID(), crTrk);
					genomeNames.add(crTrk.getGenomeName());
					genomeIds.add(crTrk.getGenomeID());
					if (pinGenome.equals(crTrk.getGenomeID())) {
						pinStrand = crTrk.findFeature(pin).getStrand();
					}
				}
			}
			else {
				LOGGER.error("BufferedReader is null");
			}
		}
		catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	public String getPinStrand() {
		return pinStrand;
	}

	public Set<String> getGenomeNames() {
		return genomeNames;
	}

	public Set<String> getGenomeIds() {
		return genomeIds;
	}

	public void addToDefaultTracks(CRTrack crTrk) {
		if (pinGenome.equals(crTrk.getGenomeID())) {
			ArrayList<String> newTrack = new ArrayList<String>();
			newTrack.add("CR" + crTrk.getRowID());
			newTrack.addAll(defaultTracks);
			defaultTracks = newTrack;
		}
		else {
			defaultTracks.add("CR" + crTrk.getRowID());
		}
	}

	public String getDefaultTracks() {
		return StringHelper.implode(defaultTracks.toArray(), ",");
	}

	public Map<Integer, CRTrack> getTrackMap() {
		return trackMap;
	}

	public void setTrackMap(Map<Integer, CRTrack> map) {
		this.trackMap = map;
	}
}
