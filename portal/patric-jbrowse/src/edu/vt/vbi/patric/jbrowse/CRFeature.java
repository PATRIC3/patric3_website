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

import java.io.Serializable;
import java.util.List;

public class CRFeature implements Comparable<CRFeature>, Serializable {
	// [ "fig|511145.12.peg.1", "Thr operon leader peptide","FIG164298","83333.1:NC_000913",190,255,"+",0,1]

	private String featureID, product, figfam, contig, strand;

	private int startPosition, endPosition, rownum, grpnum, phase;

	public CRFeature(List feature) {

		if (feature.get(0) != null) {
			featureID = feature.get(0).toString();
		}
		else {
			featureID = "";
		}
		if (feature.get(1) != null) {
			product = feature.get(1).toString();
		}
		else {
			product = "";
		}
		if (feature.get(2) != null) {
			figfam = feature.get(2).toString();
		}
		else {
			figfam = "";
		}
		if (feature.get(3) != null) {
			contig = feature.get(3).toString();
		}
		else {
			contig = "";
		}
		if (feature.get(4) != null && feature.get(5) != null) {
			int osp = Integer.parseInt(feature.get(4).toString());
			int oep = Integer.parseInt(feature.get(5).toString());
			if (osp < oep) {
				startPosition = osp;
				endPosition = oep;
			}
			else {
				startPosition = oep;
				endPosition = osp;
			}
		}
		else {
			startPosition = 0;
			endPosition = 0;
		}
		if (feature.get(6) != null) {
			strand = feature.get(6).toString();
		}
		else {
			strand = "";
		}
		if (feature.get(7) != null) {
			rownum = Integer.parseInt(feature.get(7).toString());
		}
		else {
			rownum = 0;
		}
		if (feature.size() > 8 && feature.get(8) != null) {
			grpnum = Integer.parseInt(feature.get(8).toString());
			if (grpnum > 0) {
				this.phase = (grpnum - 1) % 7 + 1;
			}
			else {
				this.phase = grpnum;
			}
		}
		else {
			grpnum = 0;
			phase = 6;
		}
	}

	public Integer getCenterPosition() {
		return startPosition + (endPosition - startPosition) / 2;
	}

	public int compareTo(CRFeature f) {
		if (startPosition == f.startPosition) {
			return endPosition - f.endPosition;
		}
		else {
			return startPosition - f.startPosition;
		}
	}

	public String getfeatureID() {
		return featureID;
	}

	public String getProduct() {
		return product;
	}

	public String getFigfam() {
		return figfam;
	}

	public String getContig() {
		return contig;
	}

	public int getStartPosition() {
		return startPosition - 1;
	}

	public void setStartPosition(int p) {
		this.startPosition = p;
	}

	public int getStartString() {
		return startPosition;
	}

	public int getEndPosition() {
		return endPosition;
	}

	public void setEndPosition(int p) {
		this.endPosition = p;
	}

	public String getStrand() {
		return strand;
	}

	public void setStrand(String s) {
		this.strand = s;
	}

	public int getRowNum() {
		return rownum;
	}

	public int getGrpNum() {
		return grpnum;
	}

	public int getPhase() {
		return phase;
	}

	public void setPhase(int p) {
		this.phase = p;
	}

}
