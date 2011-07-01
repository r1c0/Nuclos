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

package org.nuclos.server.common.ooxml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPTab;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtBlock;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtContentBlock;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtContentRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTString;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * Helper class providing simple read-only access for examing OOXML wordprocessing documents.
 *
 * <p>Implementation Note: This API is based on Apache POI 3.6 which only covers a very small subset
 * for "common use cases". Especially,structured document tags are not supported by POI 3.6 directly.
 * However, it is possible to access the underlying XML structure directly using precompiled XMLBeans
 * (packages starting with org.openxmlformats.schemas contains the XML Schema Definition (XSD) compiled
 * as XMLBeans). For details about the XML structure, see the ECMA-376 specification (in particular
 * [ECMA-376,2nd], part 1, 17.5.2).
 */
public class WordXMLReader {

	/** WordprocessingML namespace URI. */
	private static final String WORDPROCESSINGML_NS = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";

	/** Namespace declaration for XMLBeans path selection. */
	private static final String DECLARE_NS_PREFIX = "declare namespace w='" + WORDPROCESSINGML_NS + "' ";

	private final XWPFDocument document;

	private List<StructuredDocumentTag> structuredDocumentTags;

	public WordXMLReader(InputStream is) throws IOException {
		this(new XWPFDocument(is));
	}

	public WordXMLReader(XWPFDocument document) {
		this.document = document;
	}

	public String getText() {
		// Note: in POI 3.6, text extraction does not always work correctly
		return new XWPFWordExtractor(document).getText();
	}

	/**
	 * Returns a map with the text content of the structured document tags
	 * contained in this document (cf. [ECMA-376,2nd], 17.5.2) with their
	 * tag name (17.5.2.42) as key.  Supported tags are comboBox, date,
	 * dropDownList, richText and text.
	 * <p>
	 * If the structured document tag is not filled, i.e. marked as
	 * placeholder (17.5.2.25), the text content is {@code null}.
	 * If the structured document tag does not provide a tag name, the
	 * alias (aka friendly name, 17.5.2.1). If both are omitted, the
	 * structured document tag is skipped.
	 */
	public Map<String, String> getStructuredDocumentTagTexts() {
		Map<String, String> tags = new HashMap<String, String>();
		for (StructuredDocumentTag sdt : structuredDocumentTags()) {
			String name = (sdt.tagName != null) ? sdt.tagName : sdt.alias;
			if (name != null && !tags.containsKey(name))
				tags.put(name, sdt.text);
		}
		return tags;
	}

	/**
	 * Similar to {@link #getStructuredDocumentTagTexts()}, but returns
	 * prepared values.
	 * <p>
	 * For combobox (17.5.2.5) and drop-down (17.5.2.15) elements,
	 * the text content (=display text) is resolved against the given
	 * list items (17.5.2.21/22) and replaced with its associated value.
	 * For date elements (17.5.2.7), a {@link java.util.Date} object based
	 * the cached full-date is returned ({@code toString} returns the
	 * original string). If no full-date is stored, the original string
	 * object is returned.
	 */
	public Map<String, Object> getStructuredDocumentTagValues() {
		Map<String, Object> tags = new HashMap<String, Object>();
		for (StructuredDocumentTag sdt : structuredDocumentTags()) {
			String name = (sdt.tagName != null) ? sdt.tagName : sdt.alias;
			if (name != null && !tags.containsKey(name))
				tags.put(name, sdt.value);
		}
		return tags;
	}

	private List<StructuredDocumentTag> structuredDocumentTags() {
		if (structuredDocumentTags == null) {
			structuredDocumentTags = new ArrayList<StructuredDocumentTag>();
			// TODO: tables, am besten getBodyElements(), siehe JavaDoc...
			for (XWPFParagraph p : document.getParagraphs()) {
				extractStructuredDocumentTags(p.getCTP().getSdtArray());
			}

			Iterator<XWPFTable> tableIter = document.getTablesIterator();
			while(tableIter.hasNext()) {
				extractStructuredDocumentTags(tableIter.next());
			}
		}
		return structuredDocumentTags;
	}

	private void extractStructuredDocumentTags(XWPFTable t) {
		CTTbl table = t.getCTTbl();
		for (CTRow row : table.getTrArray()) {
			for (CTTc cell : row.getTcArray()) {
				extractStructuredDocumentTags(cell.getSdtArray());
				for (CTP ctp : cell.getPArray()) {
					extractStructuredDocumentTags(ctp.getSdtArray());
				}
			}
		}
	}

	private void extractStructuredDocumentTags(CTSdtRun[] sdtRuns) {
		for (CTSdtRun sdtRun : sdtRuns) {
			CTSdtPr sdtPr = sdtRun.getSdtPr();
			CTSdtContentRun sdtContent = sdtRun.getSdtContent();
			createStructuredDocumentTag(sdtPr, getRText(sdtContent.getRArray()));
		}
	}

	private void extractStructuredDocumentTags(CTSdtBlock[] sdtBlocks) {
		for (CTSdtBlock sdtRun : sdtBlocks) {
			CTSdtPr sdtPr = sdtRun.getSdtPr();
			CTSdtContentBlock sdtContent = sdtRun.getSdtContent();
			createStructuredDocumentTag(sdtPr, getPText(sdtContent.getPArray()));
		}
	}


	/**
	 * A <w:sdt> element contains 2 child elements: <w:sdtPr> for the properties,
	 * and <w:sdtContent> for content (here text).
	 */
	private void createStructuredDocumentTag(CTSdtPr sdtPr, String text) {
		// The properties contain (among others) aliases (<w:alias>), tag names (w:tag)
		// and a flag (<w:showingPlcHdr>) whether the content is placeholder or real content.
		String alias = getCTStringVal(getFirst(sdtPr.getAliasArray()));
		String tagName = getCTStringVal(getFirst(sdtPr.getTagArray()));
		boolean isPlaceholder = sdtPr.getShowingPlcHdrArray().length > 0;

		Object value = null;
		// If placeholder is set, the element is not filled by the user
		if (!isPlaceholder) {
			value = text;

			// The following child element can occur and determine the type of the structured
			// document tag: equation, comboBox (*), date (*), docPartObj, docPartList,
			// dropDownList (*), picture, richText (*), text (*), citation, group, bibliography.

			// Note that we can't use the typed method (e.g sdtPr.getComboBoxArray()) here
			// because in the small (poi-)ooxml-schemas.jar bundled with POI, the specialized
			// classes (e.g. CTStdComboBox) are missing. Trying to use these methods will fail
			// with a NoClassDefFoundError exception (cf. POI FAQ).
			// But we can work with the plain XmlObjects or DOM nodes, if we extract them by
			// a generic path expression.
			Element sdtType;
			if ((sdtType = getFirstAsDomElement(sdtPr, "w:text")) != null
				|| (sdtType = getFirstAsDomElement(sdtPr, "w:richText")) != null) {
				// Value is the text (in the case of richText without formatting)
			} else if ((sdtType = getFirstAsDomElement(sdtPr, "w:date")) != null) {
				// 17.5.2.7: fullDate contains the "full date and time last entered"
				// in XML Schema DateTime syntax
				String fullDate = sdtType.getAttributeNS(WORDPROCESSINGML_NS, "fullDate");
				if (fullDate != null) {
					XMLGregorianCalendar calendar;
					try {
						calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(fullDate);
						long timeMillis = calendar.toGregorianCalendar(null, null, null).getTimeInMillis();
						final String dateText = text;
						value = new Date(timeMillis) {
							@Override public String toString() { return dateText; };
						};
					} catch(DatatypeConfigurationException e) {
					}
				}
			} else if ((sdtType = getFirstAsDomElement(sdtPr, "w:comboBox")) != null
				|| (sdtType = getFirstAsDomElement(sdtPr, "w:dropDownList")) != null) {
				// 17.5.2.5 (comboBox), 17.5.2.15 (dropDownList)
				// Try to find the associated value with the extract text (if possible)
				NodeList listItems = sdtType.getElementsByTagNameNS(WORDPROCESSINGML_NS, "listItem");
				for (int i = 0, n = listItems.getLength(); i < n; i++) {
					Element listItem = (Element) listItems.item(i);
					String displayText = listItem.getAttributeNS(WORDPROCESSINGML_NS, "displayText");
					if (text.equals(displayText)) {
						value = listItem.getAttributeNS(WORDPROCESSINGML_NS, "value");
						break;
					}
				}
			} else if ((getFirstAsDomElement(sdtPr, "equation") != null)
					|| (getFirstAsDomElement(sdtPr, "docPartObj") != null)
					|| (getFirstAsDomElement(sdtPr, "docPartList") != null)
					|| (getFirstAsDomElement(sdtPr, "picture") != null)
					|| (getFirstAsDomElement(sdtPr, "citation") != null)
					|| (getFirstAsDomElement(sdtPr, "group") != null)
					|| (getFirstAsDomElement(sdtPr, "bibliography") != null)) {
				// ignore (unsupported type)
				return;
			} else {
				// type is unspecified, treat as text
			}
		}

		StructuredDocumentTag sdt = new StructuredDocumentTag(alias, tagName, value, text);
		structuredDocumentTags.add(sdt);
	}

	private static Element getFirstAsDomElement(XmlObject xmlObject, String path) {
		XmlObject[] children = xmlObject.selectPath(DECLARE_NS_PREFIX + path);
		if (children.length >= 1)
			return (Element) children[0].getDomNode();
		return null;
	}

	private static String getPText(CTP...ps) {
		StringBuilder sb = new StringBuilder();
		for (CTP p : ps) {
			sb.append(getRText(p.getRArray()));
		}
		return sb.toString();
	}

	private static String getRText(CTR[] rs) {
		// This method is inspired by the text extraction algorithm in the XWPFParagraph constructor
		StringBuilder sb = new StringBuilder();
		for (CTR r : rs) {
			XmlCursor c = r.newCursor();
			c.selectPath("./*");
			while (c.toNextSelection()) {
				XmlObject o = c.getObject();
				if (o instanceof CTText) {
					sb.append(((CTText) o).getStringValue());
				}
				if (o instanceof CTPTab) {
					sb.append("\t");
				}
			}
		}
		return sb.toString();
	}

	private static String getCTStringVal(CTString cts) {
		return (cts != null) ? cts.getVal() : null;
	}

	private static <T> T getFirst(T[] array) {
		return array.length >= 1 ? array[0] : null;
	}

	private static class StructuredDocumentTag {

		final String alias;
		final String tagName;
		final Object value;
		final String text;

		StructuredDocumentTag(String alias, String tagName, Object value, String text) {
			this.alias = alias;
			this.tagName = tagName;
			this.value = value;
			this.text = text;
		}
	}
}
