//Copyright (C) 2010  Novabit Informationssysteme GmbH
//
//This file is part of Nuclos.
//
//Nuclos is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//Nuclos is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with Nuclos.  If not, see <http://www.gnu.org/licenses/>.
package org.nuclos.common2.layoutml;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import org.nuclos.common2.EntityAndFieldName;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.layoutml.exception.LayoutMLException;
import org.nuclos.common2.layoutml.exception.LayoutMLParseException;

/**
 * Parser for the LayoutML. Provides methods to validate a LayoutML document and to extract
 * the names of the collectable fields used in the document.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public class LayoutMLParser implements LayoutMLConstants {

	/**
	 * validates the LayoutML document in <code>inputsource</code>.
	 * @param inputsource
	 * @throws org.nuclos.common2.layoutml.exception.LayoutMLParseException when a parse exception occurs.
	 * @throws org.nuclos.common2.layoutml.exception.LayoutMLException when a general exception occurs.
	 * @throws java.io.IOException when an I/O error occurs (rather fatal)
	 */
	public void validate(InputSource inputsource) throws LayoutMLException, IOException {
		final BasicHandler handler = new BasicHandler();
		try {
			this.parse(inputsource, handler);
		}
		catch (SAXParseException ex) {
			throw new LayoutMLParseException(ex);
		}
		catch (SAXException ex) {
			throw new LayoutMLException(ex, handler.getDocumentLocator());
		}
	}

	/**
	 * retrieves the names of the collectable fields used in the LayoutML document in <code>inputsource</code>.
	 * @param inputsource
	 * @return Set<String> the names of the collectable fields used in the LayoutML document in <code>inputsource</code>.
	 * @throws org.nuclos.common2.layoutml.exception.LayoutMLParseException when a parse exception occurs.
	 * @throws org.nuclos.common2.layoutml.exception.LayoutMLException when a general exception occurs.
	 * @throws org.nuclos.common2.exception.CommonFatalException when an I/O error occurs.
	 */
	public Set<String> getCollectableFieldNames(InputSource inputsource) throws LayoutMLException {
		return this.getElementNames(inputsource, ELEMENT_COLLECTABLECOMPONENT, ATTRIBUTE_NAME);
	}

	/**
	 * retrieves the names of the subform entities used in the LayoutML document in <code>inputsource</code>.
	 * @param inputsource
	 * @return Set<String> the names of the subform entities used in the LayoutML document in <code>inputsource</code>.
	 * @throws org.nuclos.common2.layoutml.exception.LayoutMLParseException when a parse exception occurs.
	 * @throws org.nuclos.common2.layoutml.exception.LayoutMLException when a general exception occurs.
	 * @throws org.nuclos.common2.exception.CommonFatalException when an I/O error occurs.
	 */
	public Set<String> getSubFormEntityNames(InputSource inputsource) throws LayoutMLException {
		return this.getElementNames(inputsource, ELEMENT_SUBFORM, ATTRIBUTE_ENTITY);
	}

	/**
	 * retrieves the names of the subform entities along with their foreign key fields used in the LayoutML document in <code>inputsource</code>.
	 * @param inputsource
	 * @return Collection<EntityAndFieldName> the names of the collectable fields used in the LayoutML document in <code>inputsource</code>.
	 * @throws org.nuclos.common2.layoutml.exception.LayoutMLParseException when a parse exception occurs.
	 * @throws org.nuclos.common2.layoutml.exception.LayoutMLException when a general exception occurs.
	 * @throws org.nuclos.common2.exception.CommonFatalException when an I/O error occurs.
	 */
	public Collection<EntityAndFieldName> getSubFormEntityAndForeignKeyFieldNames(InputSource inputsource) throws LayoutMLException {
		final GetSubFormEntityAndForeignKeyFieldHandler handler = new GetSubFormEntityAndForeignKeyFieldHandler();
		try {
			this.parse(inputsource, handler);
			return handler.getValues();
		}
		catch (SAXParseException ex) {
			throw new LayoutMLParseException(ex);
		}
		catch (SAXException ex) {
			throw new LayoutMLException(ex, handler.getDocumentLocator());
		}
		catch (IOException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * retrieves the names of the subform entities along with their parent subform entity used in the LayoutML document in <code>inputsource</code>.
	 * @param inputsource
	 * @return Collection<EntityAndFieldName> the names of the collectable fields used in the LayoutML document in <code>inputsource</code>.
	 * @throws org.nuclos.common2.layoutml.exception.LayoutMLParseException when a parse exception occurs.
	 * @throws org.nuclos.common2.layoutml.exception.LayoutMLException when a general exception occurs.
	 * @throws org.nuclos.common2.exception.CommonFatalException when an I/O error occurs.
	 */
	public Map<EntityAndFieldName, String> getSubFormEntityAndParentSubFormEntityNames(InputSource inputsource) throws LayoutMLException {
		final GetSubFormEntityAndParentSubEntityFormHandler handler = new GetSubFormEntityAndParentSubEntityFormHandler();
		try {
			this.parse(inputsource, handler);
			return handler.getValues();
		}
		catch (SAXParseException ex) {
			throw new LayoutMLParseException(ex, handler.getDocumentLocator());
		}
		catch (SAXException ex) {
			throw new LayoutMLException(ex, handler.getDocumentLocator());
		}
		catch (IOException ex) {
			throw new CommonFatalException(ex);
		}
	}
	
	/**
	 * retrieves the names of the collectable fields used in the LayoutML document in <code>inputsource</code>.
	 * @param sAttribute
	 * @param inputsource
	 * @return the names of the collectable fields used in the LayoutML document in <code>inputsource</code>.
	 * @throws org.nuclos.common2.layoutml.exception.LayoutMLParseException when a parse exception occurs.
	 * @throws org.nuclos.common2.layoutml.exception.LayoutMLException when a general exception occurs.
	 * @throws org.nuclos.common2.exception.CommonFatalException when an I/O error occurs.
	 */
	private Set<String> getElementNames(InputSource inputsource, String sElement, String sAttribute) throws LayoutMLException {
		final GetElementAttributeHandler handler = new GetElementAttributeHandler(sElement, sAttribute);
		try {
			this.parse(inputsource, handler);
			return handler.getValues();
		}
		catch (SAXParseException ex) {
			throw new LayoutMLParseException(ex);
		}
		catch (SAXException ex) {
			throw new LayoutMLException(ex, handler.getDocumentLocator());
		}
		catch (IOException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * WORKAROUND for memory leak in SAXParser.parse().
	 * @param inputsource
	 * @param handler
	 * @throws SAXException
	 * @throws IOException
	 */
	protected final void parse(InputSource inputsource, final DefaultHandler handler) throws SAXException, IOException {
		this.getParser().parse(inputsource, handler);
	}

	/**
	 * @return a new validating SAXParser. This parser cannot be reused, because of a bug in the
	 * used SAX implementation, the handler that is given to SAXParser.parse is not released.
	 * This causes a memory leak. So, do not cache the returned parser.
	 */
	private SAXParser getParser() {
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(true);
		try {
			return factory.newSAXParser();
		}
		catch (ParserConfigurationException ex) {
			throw new CommonFatalException(ex);
		}
		catch (SAXException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * inner class BasicHandler.
	 */
	protected static class BasicHandler extends DefaultHandler {
		private Locator locator;

		/**
		 * finds the location of the bundled DTD for the LayoutML
		 * @param sPublicId
		 * @param sSystemId
		 * @return
		 * @throws org.xml.sax.SAXException
		 */
		@Override
		public InputSource resolveEntity(String sPublicId, String sSystemId) throws SAXException {
			final String sMessage = "Could not find the DTD for the LayoutML.";//"Die DTD f\u00fcr die LayoutML konnte nicht aufgel\u00f6st werden.";
			try {
				InputSource result = null;
				if (sSystemId != null && sSystemId.equals(LAYOUTML_DTD_SYSTEMIDENTIFIER)) {
					final ClassLoader cl = LayoutMLParser.class.getClassLoader();
					final URL urlDtd = cl.getResource(LAYOUTML_DTD_RESSOURCEPATH);
					if (urlDtd == null) {
						throw new SAXException(sMessage);
					}
					result = new InputSource(new BufferedInputStream(urlDtd.openStream()));
				}
				return result;
			}
			catch (FileNotFoundException ex) {
				throw new SAXException(sMessage, ex);
			}
			catch (IOException ex) {
				throw new SAXException(sMessage, ex);
			}
		}

		/**
		 * This method is called by the SAX parser to provide a locator for this handler.
		 * @param locator
		 * @see #getDocumentLocator()
		 */
		@Override
		public void setDocumentLocator(Locator locator) {
			this.locator = locator;
		}

		/**
		 * @return a Locator reflecting the current location of the parser within the document. Should be non-null,
		 * but that depends on the SAX parser so don't rely on it.
		 */
		public Locator getDocumentLocator() {
			return this.locator;
		}

		/**
		 * called by the underlying SAX parser when a "regular" error occurs.
		 * Just throws <code>ex</code>, so the parser can take correspondent actions.
		 * @param ex
		 * @throws org.xml.sax.SAXException
		 */
		@Override
		public void error(SAXParseException ex) throws SAXException {
			throw ex;
		}

		/**
		 * called by the underlying SAX parser when a fatal error occurs.
		 * This method just calls its super method.
		 * @param ex
		 * @throws org.xml.sax.SAXException
		 */
		@Override
		public void fatalError(SAXParseException ex) throws SAXException {
			super.fatalError(ex);
		}

	}  // inner class BasicHandler

	/**
	 * inner class GetElementAttributeHandler.
	 * Gets the values of the given attribute in the given element occuring in the parsed LayoutML definition.
	 */
	private static class GetElementAttributeHandler extends BasicHandler {
		private final String sElement;
		private final String sAttribute;

		private final Set<String> stValues = new HashSet<String>();

		GetElementAttributeHandler(String sElement, String sAttribute) {
			this.sElement = sElement;
			this.sAttribute = sAttribute;
		}

		/**
		 * called by the underlying SAX parser when a start element event occurs.
		 * Only regards <code>sElement</code> elements and collects their names.
		 * @param sUriNameSpace
		 * @param sSimpleName
		 * @param sQualifiedName
		 * @param attributes
		 * @throws org.xml.sax.SAXException
		 */
		@Override
		public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
				throws SAXException {
			if (sQualifiedName.equals(this.sElement)) {
				this.stValues.add(attributes.getValue(this.sAttribute));
			}
		}

		/**
		 * @return Set<String>
		 */
		public Set<String> getValues() {
			return Collections.unmodifiableSet(this.stValues);
		}

	}  // class GetElementAttributeHandler

	/**
	 * inner class GetSubFormEntityAndForeignKeyFieldHandler.
	 */
	private static class GetSubFormEntityAndForeignKeyFieldHandler extends BasicHandler {

		private final Set<EntityAndFieldName> stValues = new HashSet<EntityAndFieldName>();

		/**
		 * called by the underlying SAX parser when a start element event occurs.
		 * @param sUriNameSpace
		 * @param sSimpleName
		 * @param sQualifiedName
		 * @param attributes
		 * @throws org.xml.sax.SAXException
		 */
		@Override
		public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
				throws SAXException {
			if (sQualifiedName.equals(ELEMENT_SUBFORM)) {
				final String sEntityName = attributes.getValue(ATTRIBUTE_ENTITY);
				final String sForeignKeyFieldName = attributes.getValue(ATTRIBUTE_FOREIGNKEYFIELDTOPARENT);
				this.stValues.add(new EntityAndFieldName(sEntityName, sForeignKeyFieldName));
			}
		}

		public Set<EntityAndFieldName> getValues() {
			return Collections.unmodifiableSet(this.stValues);
		}

	}  // class GetSubFormEntityAndForeignKeyFieldHandler

	/**
	 * inner class GetSubFormEntityAndSubFormParentHandler.
	 */
	private static class GetSubFormEntityAndParentSubEntityFormHandler extends BasicHandler {

		private final Map<EntityAndFieldName, String> mpValues = new HashMap<EntityAndFieldName, String>();

		/**
		 * called by the underlying SAX parser when a start element event occurs.
		 * @param sUriNameSpace
		 * @param sSimpleName
		 * @param sQualifiedName
		 * @param attributes
		 * @throws org.xml.sax.SAXException
		 */
		@Override
		public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
				throws SAXException {
			if (sQualifiedName.equals(ELEMENT_SUBFORM)) {
				final String sEntityName = attributes.getValue(ATTRIBUTE_ENTITY);
				final String sForeignKeyFieldName = attributes.getValue(ATTRIBUTE_FOREIGNKEYFIELDTOPARENT);
				final String sParentSubFormName = attributes.getValue(ATTRIBUTE_PARENTSUBFORM);
				this.mpValues.put(new EntityAndFieldName(sEntityName, sForeignKeyFieldName), sParentSubFormName);
			}
		}

		public Map<EntityAndFieldName, String> getValues() {
			return Collections.unmodifiableMap(this.mpValues);
		}

	}  // class GetSubFormEntityAndSubFormParentHandler
}  // class LayoutMLParser
