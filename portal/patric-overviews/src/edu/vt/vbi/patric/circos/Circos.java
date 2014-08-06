package edu.vt.vbi.patric.circos;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Circos {
	private String genomeId;

	private String uuid;

	private String tmpDir;

	private boolean includeOuterTrack = false;

	private int imageSize = 1000;

	private float trackWidth = 0.03f;

	private String gcContentPlotType = null;

	private float[] gcContentRange;

	private String gcSkewPlotType = null;

	private float[] gcSkewRange;

	private List<String> trackList = null;

	private Map<String, List<Map<String, Object>>> genomeData;

	private Map<String, String> trackNames;

	public Circos(String dir) {
		uuid = UUID.randomUUID().toString();
		tmpDir = dir + "/images/" + uuid;
		trackList = new LinkedList<>();
	}

	public void addTrackList(String track) {
		trackList.add((trackList.size() + 1) + ") " + track);
	}

	public String getGcContentPlotType() {
		return gcContentPlotType;
	}

	public float[] getGcContentRange() {
		return gcContentRange;
	}

	public String getGcSkewPlotType() {
		return gcSkewPlotType;
	}

	public float[] getGcSkewRange() {
		return gcSkewRange;
	}

	public Map<String, List<Map<String, Object>>> getGenomeData() {
		return genomeData;
	}

	public String getGenomeId() {
		return genomeId;
	}

	public int getImageSize() {
		return imageSize;
	}

	public int getRadiusSize() {
		return (imageSize / 2);
	}

	public String getTmpDir() {
		return tmpDir;
	}

	public List<String> getTrackList() {
		return trackList;
	}

	public String getTrackName(String key) {
		return this.trackNames.get(key);
	}

	public Map<String, String> getTrackNames() {
		return trackNames;
	}

	public float getTrackWidth() {
		return trackWidth;
	}

	public String getUuid() {
		return uuid;
	}

	public boolean isIncludeOuterTrack() {
		return includeOuterTrack;
	}

	public void setGcContentPlotType(String gcContentPlotType) {
		this.gcContentPlotType = gcContentPlotType;
	}

	public void setGcContentRange(float[] gcContentRange) {
		this.gcContentRange = gcContentRange;
	}

	public void setGcSkewPlotType(String gcSkewPlotType) {
		this.gcSkewPlotType = gcSkewPlotType;
	}

	public void setGcSkewRange(float[] gcSkewRange) {
		this.gcSkewRange = gcSkewRange;
	}

	public void setGenomeData(Map<String, List<Map<String, Object>>> genomeData) {
		this.genomeData = genomeData;
	}

	public void setGenomeId(String id) {
		this.genomeId = id;
	}

	public void setImageSize(int imageSize) {
		this.imageSize = imageSize;
	}

	public void setIncludeOuterTrack(boolean includeOuterTrack) {
		this.includeOuterTrack = includeOuterTrack;
	}

	public void setTrackNames(Map<String, String> trackNames) {
		this.trackNames = trackNames;
	}

	public void setTrackWidth(float trackWidth) {
		this.trackWidth = trackWidth;
	}
}