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
package edu.vt.vbi.patric.mashup;

import edu.vt.vbi.patric.mashup.xmlHandler.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParserFactory;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

public class PDBInterface {

	private static final Logger LOGGER = LoggerFactory.getLogger(PDBInterface.class);

	protected final String baseUrlDescription = "http://www.pdb.org/pdb/rest/describePDB";

	protected final String baseUrlLigand = "http://www.pdb.org/pdb/rest/ligandInfo";

	protected final String baseUrlGOTerm = "http://www.pdb.org/pdb/rest/goTerms";

	protected final String baseUrlCluster = "http://www.pdb.org/pdb/rest/sequenceCluster";

	protected final String baseUrlAnnotations = "http://www.pdb.org/pdb/rest/das/pdbchainfeatures/features";

	protected final String baseUrlPolymers = "http://www.pdb.org/pdb/rest/describeMol";

	private XMLReader xmlReader = null;

	public PDBInterface() {
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			xmlReader = spf.newSAXParser().getXMLReader();
		}
		catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
		}
	}

	public Map<String, String> getDescription(String pdbIDs) throws java.rmi.RemoteException {
		PDBDescriptionHandler handler = new PDBDescriptionHandler();
		try {
			String url = baseUrlDescription + "?structureId=" + pdbIDs;
			LOGGER.trace(url);

			URL u = new URL(url);
			URLConnection c = u.openConnection();
			c.setConnectTimeout(EutilInterface.TIMEOUT_CONN);
			c.setReadTimeout(EutilInterface.TIMEOUT_READ);
			xmlReader.setContentHandler(handler);
			xmlReader.parse(new InputSource(c.getInputStream()));
		}
		catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			return null;
		}
		return handler.getParsedData();
	}

	public List<Map<String, String>> getLigands(String pdbID) throws java.rmi.RemoteException {
		PDBLigandHandler handler = new PDBLigandHandler();
		try {
			String url = baseUrlLigand + "?structureId=" + pdbID;
			LOGGER.trace(url);

			URL u = new URL(url);
			URLConnection c = u.openConnection();
			c.setConnectTimeout(EutilInterface.TIMEOUT_CONN);
			c.setReadTimeout(EutilInterface.TIMEOUT_READ);
			xmlReader.setContentHandler(handler);
			xmlReader.parse(new InputSource(c.getInputStream()));
		}
		catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			return null;
		}
		return handler.getParsedData();
	}

	public List<Map<String, String>> getGOTerms(String pdbID) throws java.rmi.RemoteException {
		PDBGOTermsHandler handler = new PDBGOTermsHandler();
		try {
			String url = baseUrlGOTerm + "?structureId=" + pdbID;
			LOGGER.trace(url);

			URL u = new URL(url);
			URLConnection c = u.openConnection();
			c.setConnectTimeout(EutilInterface.TIMEOUT_CONN);
			c.setReadTimeout(EutilInterface.TIMEOUT_READ);
			xmlReader.setContentHandler(handler);
			xmlReader.parse(new InputSource(c.getInputStream()));
		}
		catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			return null;
		}
		return handler.getParsedData();
	}

	public List<Map<String, String>> getSequenceCluster(String pdbID, int cluster) throws java.rmi.RemoteException {
		PDBSequenceClusterHandler handler = new PDBSequenceClusterHandler();
		try {
			String url = baseUrlCluster + "?structureId=" + pdbID + "&cluster=" + cluster;
			LOGGER.trace(url);

			URL u = new URL(url);
			URLConnection c = u.openConnection();
			c.setConnectTimeout(EutilInterface.TIMEOUT_CONN);
			c.setReadTimeout(EutilInterface.TIMEOUT_READ);
			xmlReader.setContentHandler(handler);
			xmlReader.parse(new InputSource(c.getInputStream()));
		}
		catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			return null;
		}
		return handler.getParsedData();
	}

	public List<Map<String, String>> getAnnotations(String pdbID) throws java.rmi.RemoteException {
		PDBAnnotationsHandler handler = new PDBAnnotationsHandler();
		PDBAnnotationsResolver resolver = new PDBAnnotationsResolver();
		try {
			String url = baseUrlAnnotations + "?segment=" + pdbID;
			LOGGER.trace(url);

			URL u = new URL(url);
			URLConnection c = u.openConnection();
			c.setConnectTimeout(EutilInterface.TIMEOUT_CONN);
			c.setReadTimeout(EutilInterface.TIMEOUT_READ);

			xmlReader.setContentHandler(handler);
			xmlReader.setEntityResolver(resolver);
			xmlReader.parse(new InputSource(c.getInputStream()));
		}
		catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			return null;
		}
		return handler.getParsedData();
	}

	public List<String> getPolymers(String pdbID) throws java.rmi.RemoteException {
		PDBPolymersHandler handler = new PDBPolymersHandler();
		try {
			String url = baseUrlPolymers + "?structureId=" + pdbID;
			LOGGER.trace(url);

			URL u = new URL(url);
			URLConnection c = u.openConnection();
			c.setConnectTimeout(EutilInterface.TIMEOUT_CONN);
			c.setReadTimeout(EutilInterface.TIMEOUT_READ);
			xmlReader.setContentHandler(handler);
			xmlReader.parse(new InputSource(c.getInputStream()));
		}
		catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			return null;
		}
		return handler.getParsedData();
	}
}
