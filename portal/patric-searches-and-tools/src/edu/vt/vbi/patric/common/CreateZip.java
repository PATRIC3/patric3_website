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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CreateZip {

	private static final Logger LOGGER = LoggerFactory.getLogger(CreateZip.class);

	private final String DOWNLOAD_ROOT = "/cid/brcdownloads/patric2/patric3/genomes/";

	public static void main(String args[]) throws IOException {

	}

	public byte[] ZipIt(List<String> genomeIdList, List<String> annotations, List<String> fileTypes) throws IOException {

		Set<String> files = new HashSet<>();

		for (String genomeId : genomeIdList) {

			File genomeDir = new File(DOWNLOAD_ROOT + genomeId);

			if (!genomeDir.exists()) {
				LOGGER.debug("Skipping Folder: {}", DOWNLOAD_ROOT + genomeId);
				continue;
			}
			else {
				for (String annotation : annotations) {
					for (String fileType : fileTypes) {
						File file = new File(DOWNLOAD_ROOT + genomeId + "/" + genomeId + (fileType.equals(".fna") ? "" : annotation) + fileType);

						if (!file.exists()) {
							System.err.println("Skipping File: " + file.getAbsolutePath());
						}
						else {
							files.add(file.getAbsolutePath());
						}
					}
				}
			}
		}

		try (
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ZipOutputStream zos = new ZipOutputStream(baos)
		) {

			if (!files.isEmpty()) {

				int bytesRead;
				byte[] buffer = new byte[1024];

				for (String path : files) {
					File file = new File(path);
					BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
					ZipEntry entry = new ZipEntry(file.getName());
					zos.putNextEntry(entry);
					while ((bytesRead = bis.read(buffer)) != -1) {
						zos.write(buffer, 0, bytesRead);
					}
					bis.close();
					zos.closeEntry();
				}
				zos.close();
			}
			return baos.toByteArray();
		}
	}
}