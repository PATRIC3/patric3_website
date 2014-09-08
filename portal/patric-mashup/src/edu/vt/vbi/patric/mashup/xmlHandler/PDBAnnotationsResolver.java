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
package edu.vt.vbi.patric.mashup.xmlHandler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import edu.vt.vbi.patric.mashup.PDBInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

public class PDBAnnotationsResolver implements EntityResolver {

	private static final Logger LOGGER = LoggerFactory.getLogger(PDBInterface.class);

	@Override
	public InputSource resolveEntity(String publicId, String systemId) {
		InputSource is = null;
		try {
			is = new InputSource(new URL("http://www.biodas.org/dtd/dasgff.dtd").openStream());
		}
		catch (MalformedURLException e) {
			LOGGER.error(e.getMessage(), e);
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return is;
	}
}
