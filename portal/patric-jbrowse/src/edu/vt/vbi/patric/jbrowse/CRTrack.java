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

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.*;

public class CRTrack implements Serializable {

	private int rowID;

	private String pin, genomeID, genomeName;

	private Set<String> SeedIds;

	private List<CRFeature> featureList;

	public CRTrack(Map track) {
		rowID = Integer.parseInt(track.get("row_id").toString());
		pin = (String) track.get("pin");
		genomeID = (String) track.get("genome_id");
		genomeName = (String) track.get("genome_name");

		List jsonFeatures = (List) track.get("features");
		SeedIds = new HashSet<>();
		featureList = new LinkedList<>();
		for (Object jsonFeature : jsonFeatures) {
			CRFeature f = new CRFeature((List) jsonFeature);
			featureList.add(f);
			SeedIds.add(f.getfeatureID());
		}
	}

	public CRFeature findFeature(String featureID) {
		CRFeature f = null;
		for (CRFeature crFeature : featureList) {
			if (crFeature.getfeatureID().equals(featureID)) {
				f = crFeature;
				break;
			}
		}
		return f;
	}

	public String getSeedIds() {

		return StringUtils.join(SeedIds, " OR ");
	}

	public void relocateFeatures(int window_size, String pin_strand) {
		CRFeature genome_pin = findFeature(this.getPin());
		int center = genome_pin.getCenterPosition();

		boolean isThisGenomeReversed = false;
		if (!genome_pin.getStrand().equals(pin_strand)) {
			isThisGenomeReversed = true;
		}

		for (int idx = 0; idx < featureList.size(); idx++) {
			CRFeature f = featureList.get(idx);

			int tS = (f.getStartPosition() - center) + window_size / 2;
			int tE = (f.getEndPosition() - center) + window_size / 2;

			if (isThisGenomeReversed) {
				int _tS = window_size - tS;
				int _tE = window_size - tE;
				tS = _tE;
				tE = _tS;

				if (f.getStrand().equals("+")) {
					f.setStrand("-");
				}
				else {
					f.setStrand("+");
				}
			}
			f.setStartPosition(tS);
			f.setEndPosition(tE);

			if (genome_pin.getfeatureID().equals(f.getfeatureID())) {
				f.setPhase(0);
			}
			featureList.set(idx, f);
		}

	}

	public int getRowID() {
		return rowID;
	}

	public void setRowID(int rowID) {
		this.rowID = rowID;
	}

	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}

	public String getGenomeID() {
		return genomeID;
	}

	public void setGenomeID(String genomeID) {
		this.genomeID = genomeID;
	}

	public String getGenomeName() {
		return genomeName;
	}

	public void setGenomeName(String genomeName) {
		this.genomeName = genomeName;
	}

	public List<CRFeature> getFeatureList() {
		return featureList;
	}

	public void setFeatureList(List<CRFeature> featureList) {
		this.featureList = featureList;
	}
}
