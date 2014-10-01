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
package edu.vt.vbi.patric.msa;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class SequenceData implements Comparable<SequenceData> {
	private String taxonName;

	private String locusTag;

	private String sequence;

	private int fastaOrder = 0;

	SequenceData(String locus) {
		this.locusTag = locus;
		taxonName = null;
	}

	public SequenceData(String taxonName, String locusTag, String sequence) {
		this.taxonName = taxonName;
		this.locusTag = locusTag;
		this.sequence = sequence;
	}

	public void writeLongName(PrintWriter writer) {
		writer.write(locusTag + "  " + taxonName + "\n");
	}

	public String getLocusTag() {
		return locusTag;
	}

	public String getTaxonName() {
		return taxonName;
	}

	public String getLongName() {
		return (taxonName + " " + locusTag);
	}

	public void setFastaOrder(int i) {
		fastaOrder = i;
	}
	public int getFastaOrder() {
		return fastaOrder;
	}

	public String getSequence() {
		return sequence;
	}

	public String setFasta(int maxName, BufferedWriter writer, int[] aaRange) throws IOException {
		if (aaRange != null) {
			int set = sequence.length();
			if (set < aaRange[0]) {
				aaRange[0] = set;
			}
			if (aaRange[1] < set) {
				aaRange[1] = set;
			}
		}

		writer.write(">");
		if (locusTag.length() < maxName) {
			writer.write(locusTag);
		}
		else {
			writer.write(locusTag.substring(0, maxName - 1));
		}
		writer.write("\n" + sequence + "\n");
		return (taxonName);
	}

	public void writeToFasta(BufferedWriter writer) throws IOException {
		writer.write(">" + locusTag);
		writer.newLine();
		writer.write(sequence);
		writer.newLine();
	}

	public int compareTo(SequenceData other) {
		return locusTag.compareTo(other.getLocusTag());
	}
}
