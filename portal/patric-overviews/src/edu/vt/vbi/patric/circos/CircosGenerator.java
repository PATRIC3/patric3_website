package edu.vt.vbi.patric.circos;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CircosGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(CircosGenerator.class);

	private final String DIR_CONFIG = "/conf";

	private final String DIR_DATA = "/data";

	private String appDir;

	private Template tmplPlotConf;

	private Template tmplImageConf;

	private Template tmplCircosConf;

	CircosData circosData;

	public CircosGenerator(String path) {
		appDir = path;
		circosData = new CircosData();
		try {
			tmplPlotConf = Mustache.compiler().compile(new BufferedReader(new FileReader(path + "/conf_templates/plots.mu")));
			tmplImageConf = Mustache.compiler().compile(new BufferedReader(new FileReader(path + "/conf_templates/image.mu")));
			tmplCircosConf = Mustache.compiler().compile(new BufferedReader(new FileReader(path + "/conf_templates/circos.mu")));
		}
		catch (FileNotFoundException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	public Circos createCircosImage(Map<String, Object> parameters) {
		if (parameters.isEmpty()) {
			LOGGER.error("Circos image could not be created");
			return null;
		}
		else {
			// 1. Create instance and set configs
			Circos circos = new Circos(appDir);
			circos.setGenomeId(parameters.get("genome_id").toString());

			// Record whether to include GC content track or not
			if (parameters.containsKey("gc_content_plot_type")) {
				circos.setGcContentPlotType(parameters.get("gc_content_plot_type").toString());
			}
			if (parameters.containsKey("gc_skew_plot_type")) {
				circos.setGcSkewPlotType(parameters.get("gc_skew_plot_type").toString());
			}

			// Record whether to include outer track or not
			if (parameters.containsKey("include_outer_track")) {
				circos.setIncludeOuterTrack(parameters.get("include_outer_track").equals("on"));
			}

			// Store image size parameter from form
			if (parameters.containsKey("image_dimensions") && !parameters.get("image_dimensions").equals("")) {
				circos.setImageSize(Integer.parseInt(parameters.get("image_dimensions").toString()));
			}

			// Convert track width parameter to percentage and store it
			if (parameters.containsKey("track_width")) {
				circos.setTrackWidth((float) (Integer.parseInt(parameters.get("track_width").toString()) / 100.0));
			}

			// Collect genome data using Solr API for PATRIC
			// circos.setGenomeData(this.getGenomeData(parameters));
			circos = this.getGenomeData(circos, parameters);

			// Create temp directory for this image's data
			String tmpFolderName = circos.getTmpDir();
			try {
				Files.createDirectory(Paths.get(tmpFolderName));
			}
			catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
				return null;
			}

			// Create data and config files for Circos
			circos = createCircosDataFiles(circos, parameters);
			circos = createCircosConfigFiles(circos);

			// Run Circos script to generate final image
			// `circos -conf #{folder_name}/circos_configs/circos.conf -debug_group summary,timer > circos.log.out`
			String command = "circos -conf " + tmpFolderName + DIR_CONFIG + "/circos.conf -debug_group summary,timer";
			try {
				LOGGER.debug("Starting Circos script: " + command);
				Process p = Runtime.getRuntime().exec(command);
				p.waitFor();
				LOGGER.debug(IOUtils.toString(p.getInputStream()));
			}
			catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}

			return circos;
		}
	}

	private Circos getGenomeData(Circos circosConf, Map<String, Object> parameters) {
		String genomeId = parameters.get("genome_id").toString();
		List<String> defaultDataTracks = new ArrayList<>();
		List<String> defaultTrackNames = new ArrayList<>();
		defaultDataTracks.addAll(Arrays.asList("cds_forward", "cds_reverse", "rna_both", "misc_both"));
		defaultTrackNames.addAll(Arrays.asList("CDS, forward strand", "CDS, reverse strand", "RNAs", "Miscellaneous features"));
		Map<String, List<Map<String, Object>>> genomeData = new LinkedHashMap<>();
		Map<String, String> trackNames = new HashMap<>();

		for (String parameter : parameters.keySet()) {
			// Skip over parameters that aren't track types
			int idx = defaultDataTracks.indexOf(parameter);
			if (idx < 0) {
				continue;
			}
			// Build query string based on user's input
			String featureType = parameter.split("_")[0];
			String paramStrand = parameter.split("_")[1];
			String strand;
			switch (paramStrand) {
			case "forward":
				strand = "+";
				break;
			case "reverse":
				strand = "-";
				break;
			default:
				strand = null;
			}

			genomeData.put(parameter, circosData.getFeatures(genomeId, featureType, strand, null));
			trackNames.put(parameter, defaultTrackNames.get(idx));
		}

		// Create a set of all the entered custom track numbers
		// parameters.keys.select{ |e| /custom_track_.*/.match e }.each { |parameter| track_nums << parameter[/.*_(\d+)$/, 1] }
		Set<Integer> trackNums = new HashSet<>();
		//		paramKeys = (Iterator<String>) parameters.keySet().iterator();
		//		while (paramKeys.hasNext()) {
		//			String key = paramKeys.next();
		for (String key : parameters.keySet()) {
			if (key.matches("custom_track_.*_(\\d+)$")) {
				int num = Integer.parseInt(key.substring(key.lastIndexOf("_") + 1));
				LOGGER.trace("{} matches {}", key, num);
				trackNums.add(num);
			}
		}

		// Gather data for each custom track
		for (Integer trackNum : trackNums) {
			String customTrackName = "custom_track_" + trackNum;
			String featureType = parameters.get("custom_track_type_" + trackNum).toString();
			String paramStrand = parameters.get("custom_track_strand_" + trackNum).toString();
			String strand;
			switch (paramStrand) {
			case "forward":
				strand = "+";
				break;
			case "reverse":
				strand = "-";
				break;
			default:
				strand = null;
			}
			String keywords = null;
			if (parameters.containsKey("custom_track_keyword_" + trackNum)) {
				keywords = parameters.get("custom_track_keyword_" + trackNum).toString();
			}
			genomeData.put(customTrackName, circosData.getFeatures(genomeId, featureType, strand, keywords));
			trackNames.put(customTrackName, featureType.toUpperCase() + " " + StringUtils.capitalize(paramStrand) + ": " + keywords);
		}

		circosConf.setGenomeData(genomeData);
		circosConf.setTrackNames(trackNames);
		return circosConf;
	}

	private Circos createCircosDataFiles(Circos circos, Map<String, Object> parameters) {
		Map<String, String> trackNames = circos.getTrackNames();
		// Create folder for all data files
		String dirData = circos.getTmpDir() + DIR_DATA;
		try {
			Files.createDirectory(Paths.get(dirData));
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		Map<String, List<Map<String, Object>>> genomeData = circos.getGenomeData();
		String genomeName = circosData.getGenomeName(circos.getGenomeId());
		List<Map<String, Object>> accessions = circosData.getAccessions(circos.getGenomeId());

		Iterator<String> iter;

		// 1. Write feature track data in file format: feature_type.strand.txt e.g. cds.forward.txt, rna.reverse.txt
		iter = genomeData.keySet().iterator();
		while (iter.hasNext()) {
			String track = iter.next();
			List<Map<String, Object>> featureData = genomeData.get(track);

			String fileName = "/" + track.replace("_", ".") + ".txt";
			try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(dirData + fileName)))) {
				for (Map<String, Object> gene : featureData) {
					writer.format("%s\t%d\t%d\tid=%s\n", gene.get("accession"), gene.get("start"), gene.get("end"), gene.get("feature_id"));
				}
			}
			catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}

		// 2. Write karyotype file (Accession list)
		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(dirData + "/karyotype.txt")))) {
			for (Map<String, Object> accession : accessions) {
				writer.format("chr\t-\t %s\t %s\t 0\t %d\t grey\n", accession.get("accession"), genomeName.replace(" ", "_"),
						accession.get("length"));
			}
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

		// 3. Calculate GC content & skew values if user selected
		int defaultWindowSize = 10000;
		int defaultStepSize = 2000;

		// 3.1 Create GC content data file
		if (circos.getGcContentPlotType() != null) {
			LOGGER.debug("Creating data file for GC content");
			trackNames.put("gc_content", "GC Content, " + circos.getGcContentPlotType() + " Plot");
			Map<String, Float> gcContentValues = new LinkedHashMap<>();
			float minGCContent = 1.0f;
			float maxGCContent = 0.0f;

			for (Map<String, Object> accession : accessions) {
				String accessionID = accession.get("accession").toString();
				String sequence = accession.get("sequence").toString();
				int totalSeqLength = sequence.length();

				// Iterate over each window_size-sized block and calculate its GC content.
				// For instance, if the sequence length were 1,234,567 and the window size were 1000, we would iterate 1234 times, with the last
				// iteration being the window from 1,234,001 to 1,234,566
				for (int i = 0; i < (totalSeqLength / defaultStepSize); i++) {

					// Only use 0 as start index for first iteration, otherwise with a window_size of 1000, start should be something like 1001, 2001,
					// and so on.
					int startIndex = (i == 0) ? 1 : (i * defaultStepSize + 1);

					// End index should either be 'window_size' greater than the start or if we are at the last iteration, the end of the sequence.
					int endIndex = Math.min(startIndex + defaultWindowSize - 1, totalSeqLength - 1);

					int currentWindowSize = endIndex - startIndex;

					// Store number of 'g' and 'c' characters from the sequence
					Pattern pattern = Pattern.compile("[gcGC]");
					Matcher matcher = pattern.matcher(sequence.subSequence(startIndex, endIndex));
					int gcCount;
					for (gcCount = 0; matcher.find(); gcCount++)
						;
					float gcPercentage = (gcCount / (float) currentWindowSize);
					minGCContent = Math.min(minGCContent, gcPercentage);
					maxGCContent = Math.max(maxGCContent, gcPercentage);

					// Store percentage in gc_content_values hash as value with the range from the start index to the end index as the key
					gcContentValues.put(accessionID + ":" + startIndex + ".." + endIndex, gcPercentage); // .round(5)
				}
			}
			// Write GC content data for this accession
			try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(dirData + "/gc.content.txt")))) {
				Iterator<String> iterGC = gcContentValues.keySet().iterator();
				while (iterGC.hasNext()) {
					String range = iterGC.next();
					String[] rangeId = range.split(":");
					String[] rangeLoc = rangeId[1].split("\\.\\.");
					// logger.info("{}, {}, {}", accession, strIndex, endIndex);
					writer.format("%s\t%s\t%s\t%f\n", rangeId[0], rangeLoc[0], rangeLoc[1], gcContentValues.get(range));
				}
			}
			catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
			float[] range = new float[2];
			range[0] = minGCContent;
			range[1] = maxGCContent;
			circos.setGcContentRange(range);
			genomeData.put("gc_content", new ArrayList<Map<String, Object>>());
		}

		// 3.2 Create GC skew data file
		if (circos.getGcSkewPlotType() != null) {
			LOGGER.debug("Creating data file for GC skew");
			trackNames.put("gc_skew", "GC Skew, " + circos.getGcSkewPlotType() + " Plot");

			Map<String, Float> gcSkewValues = new LinkedHashMap<>();
			float minGCSkew = 1.0f;
			float maxGCSkew = -1.0f;

			for (Map<String, Object> accession : accessions) {
				String accessionId = accession.get("accession").toString();
				String sequence = accession.get("sequence").toString();
				int totalSeqLength = sequence.length();

				for (int i = 0; i < (totalSeqLength / defaultStepSize); i++) {
					int startIndex = (i == 0) ? 1 : (i * defaultStepSize + 1);
					int endIndex = Math.min(startIndex + defaultWindowSize - 1, totalSeqLength - 1);

					Pattern ptrnGContent = Pattern.compile("[gG]");
					Pattern ptrnCContent = Pattern.compile("[cC]");
					Matcher mtchrG = ptrnGContent.matcher(sequence.subSequence(startIndex, endIndex));
					Matcher mtchrC = ptrnCContent.matcher(sequence.subSequence(startIndex, endIndex));

					int gCount, cCount;
					for (gCount = 0; mtchrG.find(); gCount++)
						;
					for (cCount = 0; mtchrC.find(); cCount++)
						;
					float gcSkew = (float) (gCount - cCount) / (gCount + cCount);
					minGCSkew = Math.min(minGCSkew, gcSkew);
					maxGCSkew = Math.max(maxGCSkew, gcSkew);

					gcSkewValues.put(accessionId + ":" + startIndex + ".." + endIndex, gcSkew);
				}
			}
			// Write GC skew data for this accession
			try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(dirData + "/gc.skew.txt")));) {
				for (String range : gcSkewValues.keySet()) {
					String[] rangeId = range.split(":");
					String[] rangeLoc = rangeId[1].split("\\.\\.");
					writer.format("%s\t%s\t%s\t%f\n", rangeId[0], rangeLoc[0], rangeLoc[1], gcSkewValues.get(range));
				}
			}
			catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}

			float[] range = new float[2];
			range[0] = minGCSkew;
			range[1] = maxGCSkew;
			circos.setGcSkewRange(range);
			genomeData.put("gc_skew", new ArrayList<Map<String, Object>>());
		}

		// 4. Write "large tiles" file for outer track
		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(dirData + "/large.tiles.txt")))) {
			for (Map<String, Object> accession : accessions) {
				writer.format("%s\t0\t%d\n", accession.get("accession"), accession.get("length"));
			}
			writer.close();
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

		// 5. Process upload files
		List<Map<String, Object>> fileupload = new ArrayList<>();
		Set<Integer> trackNums = new HashSet<>();

		for (String key : parameters.keySet()) {
			if (key.matches("file_(\\d+)$")) {
				int num = Integer.parseInt(key.substring(key.lastIndexOf("_") + 1));
				FileItem item = (FileItem) parameters.get("file_" + num);
				LOGGER.trace("{} matches, filename={}", key, item.getName());
				if (!item.getName().equals("")) {
					trackNums.add(num);
				}
			}
		}
		for (Integer trackNum : trackNums) {
			if (parameters.containsKey("file_" + trackNum)) {
				FileItem item = (FileItem) parameters.get("file_" + trackNum);
				try {
					String fileName = "user.upload." + trackNum + ".txt";
					String plotType = parameters.get("file_plot_type_" + trackNum).toString();
					float minValue = 0.0f;
					float maxValue = 1.0f;
					boolean isValid = true;
					StringBuffer sb = new StringBuffer();

					try (BufferedReader br = new BufferedReader(new InputStreamReader(item.getInputStream()))) {
						String line;
						while ((line = br.readLine()) != null && isValid) {
							// logger.info(line);
							String[] tab = line.split("\t");
							if (plotType.equals("tile")) {
								if (tab.length == 3) {
									sb.append(line);
									sb.append("\n");
								}
								else if (!tab[3].contains("id=")) {
									isValid = false;
								}
								else {
									isValid = false;
								}
							}
							else if (plotType.equals("line") || plotType.equals("histogram") || plotType.equals("heatmap")) {
								try {
									float value = Float.parseFloat(tab[3]);
									minValue = Math.min(minValue, value);
									maxValue = Math.max(maxValue, value);
									sb.append(line);
									sb.append("\n");
								}
								catch (NumberFormatException | NullPointerException ex) {
									isValid = false;
								}
							}
							else {
								isValid = false;
							}
						}
					}
					catch (IOException e) {
						LOGGER.error(e.getMessage(), e);
					}
					if (isValid) {

						try (BufferedWriter writer = new BufferedWriter(new FileWriter(dirData + "/" + fileName))) {
							writer.append(sb);
						}
						Map<String, Object> file = new HashMap<>();
						file.put("file_name", fileName);
						file.put("plot_type", plotType);
						file.put("track_key", "file_" + trackNum);
						file.put("minValue", minValue);
						file.put("maxValue", maxValue);
						if (minValue < 0 && maxValue > 0) {
							file.put("color_scheme", "rdylgn-7-div-rev"); // divergent
						}
						else {
							file.put("color_scheme", "ylorrd-7-seq"); // sequential, linear
						}
						fileupload.add(file);

						trackNames.put("file_" + trackNum, item.getName() + " (" + plotType + ")");
					}
				}
				catch (Exception e) {
					LOGGER.error(e.getMessage(), e);
				}
			}
		}
		if (trackNums.size() > 0) {
			genomeData.put("user_upload", fileupload);
		}

		circos.setTrackNames(trackNames); // update track names map
		return circos;
	}

	private Circos createCircosConfigFiles(Circos circos) {
		Map<String, List<Map<String, Object>>> genomeData = circos.getGenomeData();
		Map<String, String> trackNames = circos.getTrackNames();
		List<String> colors = new LinkedList<>();
		colors.addAll(Arrays.asList("vdgreen", "lgreen", "vdred", "lred", "vdpurple", "lpurple", "vdorange", "lorange", "vdyellow", "lyellow"));
		String dataDir = circos.getTmpDir() + DIR_DATA;
		String confDir = circos.getTmpDir() + DIR_CONFIG;

		// Create folder for config files & copy static config files (ideogram.conf, ticks.conf)
		try {
			Files.createDirectory(Paths.get(confDir));
			Files.copy(Paths.get(appDir + "/conf_templates/ideogram.conf"), Paths.get(confDir + "/ideogram.conf"));
			Files.copy(Paths.get(appDir + "/conf_templates/ticks.conf"), Paths.get(confDir + "/ticks.conf"));
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

		// plots.conf
		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(confDir + "/plots.conf")))) {
			List<Map<String, String>> tilePlots = new ArrayList<>();
			List<Map<String, String>> nonTilePlots = new ArrayList<>();
			float currentRadius = 1.0f;
			float trackThickness = circos.getImageSize() * circos.getTrackWidth();
			float trackBuffer = 0.025f;

			if (circos.isIncludeOuterTrack()) {
				Map<String, String> largeTileData = new HashMap<>();
				largeTileData.put("file", dataDir + "/large.tiles.txt");
				largeTileData.put("thickness", Float.toString((trackThickness / 2)) + "p");
				largeTileData.put("type", "tile");
				largeTileData.put("color", "vdblue");
				largeTileData.put("r1", Float.toString(currentRadius) + "r");
				largeTileData.put("r0", Float.toString((currentRadius -= 0.02)) + "r");

				tilePlots.add(largeTileData);
				circos.addTrackList("Chromosomes / Plasmids / Contigs");
			}

			Iterator<String> keys = genomeData.keySet().iterator();
			while (keys.hasNext()) {
				String track = keys.next();
				Map<String, String> plotData;

				// Handle user uploaded files
				if (track.contains("user_upload")) {
					List<Map<String, Object>> files = genomeData.get(track);

					for (Map<String, Object> file : files) {
						plotData = new HashMap<>();
						String plotType = file.get("plot_type").toString();

						if (plotType.equals("tile") || plotType.equals("heatmap")) {
							plotData.put("file", dataDir + "/" + file.get("file_name"));
							plotData.put("thickness", Float.toString(trackThickness) + "p");
							plotData.put("type", plotType);
							if (plotType.equals("tile")) {
								plotData.put("color", colors.remove(0));
							}
							else {
								plotData.put("color", file.get("color_scheme").toString());
							}
							float r1 = (currentRadius -= (0.01 + trackBuffer));
							float r0 = (currentRadius -= (0.04 + trackBuffer));
							plotData.put("r1", Float.toString(r1) + "r");
							plotData.put("r0", Float.toString(r0) + "r");

							tilePlots.add(plotData);
						}
						else {
							// histogram, line
							plotData.put("file", dataDir + "/" + file.get("file_name"));
							plotData.put("type", plotType);
							plotData.put("color", colors.remove(0));
							float r1 = (currentRadius -= (0.01 + trackBuffer));
							float r0 = (currentRadius -= (0.10 + trackBuffer));
							plotData.put("r1", Float.toString(r1) + "r");
							plotData.put("r0", Float.toString(r0) + "r");
							plotData.put("min", file.get("minValue").toString());
							plotData.put("max", file.get("maxValue").toString());
							if (plotType.equals("histogram")) {
								plotData.put("extendbin", "extend_bin = no");
							}
							else {
								plotData.put("extendbin", "");
							}
							// pick the base color and convert to v(ery)v(ery)l(ight)-color as a background
							plotData.put("plotbgcolor", "vvl" + plotData.get("color").replaceAll("^[vld]+", ""));

							nonTilePlots.add(plotData);
						}

						circos.addTrackList(trackNames.get(file.get("track_key")));
					}
				}
				else if (track.contains("gc")) { // gc_content or gc_skew
					plotData = new HashMap<>();
					String plotType;
					if (track.equals("gc_content")) {
						plotType = circos.getGcContentPlotType();
					}
					else {
						plotType = circos.getGcSkewPlotType();
					}
					if (plotType.equals("heatmap")) {
						plotData.put("file", dataDir + "/" + track.replace("_", ".") + ".txt");
						plotData.put("thickness", Float.toString(trackThickness) + "p");
						plotData.put("type", plotType);
						plotData.put("color", "rdbu-10-div");
						float r1 = (currentRadius -= (0.01 + trackBuffer)); // outer radius
						float r0 = (currentRadius -= (0.04 + trackBuffer)); // inner radius
						plotData.put("r1", Float.toString(r1) + "r");
						plotData.put("r0", Float.toString(r0) + "r");

						tilePlots.add(plotData);
					}
					else {
						plotData.put("file", dataDir + "/" + track.replace("_", ".") + ".txt");
						plotData.put("type", plotType);
						plotData.put("color", colors.remove(0));
						float r1 = (currentRadius -= (0.01 + trackBuffer)); // outer radius
						float r0 = (currentRadius -= (0.10 + trackBuffer)); // inner radius
						plotData.put("r1", Float.toString(r1) + "r");
						plotData.put("r0", Float.toString(r0) + "r");
						float[] range;
						if (track.equals("gc_content")) {
							range = circos.getGcContentRange();
						}
						else {
							range = circos.getGcSkewRange();
						}
						plotData.put("min", Float.toString(range[0]));
						plotData.put("max", Float.toString(range[1]));
						if (plotType.equals("histogram")) {
							plotData.put("extendbin", "extend_bin = no");
						}
						else {
							plotData.put("extendbin", "");
						}
						// pick the base color and convert to v(ery)v(ery)l(ight)-color as a background
						plotData.put("plotbgcolor", "vvl" + plotData.get("color").replaceAll("^[vld]+", ""));

						nonTilePlots.add(plotData);
					}
					circos.addTrackList(trackNames.get(track));
				}
				else {
					// handle default/custom tracks
					plotData = new HashMap<>();
					plotData.put("file", dataDir + "/" + track.replace("_", ".") + ".txt");
					plotData.put("thickness", Float.toString(trackThickness) + "p");
					plotData.put("type", "tile");
					plotData.put("color", colors.remove(0));
					float r1 = (currentRadius -= (0.01 + trackBuffer));
					float r0 = (currentRadius -= (0.04 + trackBuffer));
					plotData.put("r1", Float.toString(r1) + "r");
					plotData.put("r0", Float.toString(r0) + "r");

					tilePlots.add(plotData);
					circos.addTrackList(trackNames.get(track));
				}
			} // end of while

			// build plots.conf with Mustache template
			Map<String, List<Map<String, String>>> data = new HashMap<>();
			data.put("tileplots", tilePlots);
			data.put("nontileplots", nonTilePlots);

			tmplPlotConf.execute(data, writer);
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

		// Build image.conf with Mustache template
		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(confDir + "/image.conf")))) {
			Map<String, String> data = new HashMap<String, String>();
			data.put("path", circos.getTmpDir());
			data.put("image_size", Integer.toString(circos.getRadiusSize())); // radius

			tmplImageConf.execute(data, writer);
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

		// Build circos.conf with Mustache template
		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(confDir + "/circos.conf")))) {
			Map<String, String> data = new HashMap<String, String>();
			data.put("folder", circos.getTmpDir());

			tmplCircosConf.execute(data, writer);
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

		// return circos config
		return circos;
	}
}