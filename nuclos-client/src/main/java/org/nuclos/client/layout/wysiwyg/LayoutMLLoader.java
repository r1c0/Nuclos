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
package org.nuclos.client.layout.wysiwyg;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

import org.apache.log4j.Logger;
import org.nuclos.client.common.LocaleDelegate;
import org.nuclos.client.common.NuclosCollectableEntityProvider;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.ERROR_MESSAGES;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.PROPERTY_LABELS;
import org.nuclos.client.layout.wysiwyg.component.ComponentProcessors;
import org.nuclos.client.layout.wysiwyg.component.TitledBorderWithTranslations;
import org.nuclos.client.layout.wysiwyg.component.TranslationMap;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableOptionGroup;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGScrollPane;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGSplitPane;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGStaticButton;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGStaticComboBox;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGStaticLabel;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGStaticSeparator;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGStaticTextarea;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGStaticTextfield;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGStaticTitledSeparator;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGSubForm;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGSubFormColumn;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGTabbedPane;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGUniversalComponent;
import org.nuclos.client.layout.wysiwyg.component.properties.ComponentProperties;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyOptions;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyUtils;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueBoolean;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueFont;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueInitialSortingOrder;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueString;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueTranslations;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueValuelistProvider;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.LayoutCell;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.TableLayoutPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGInitialFocusComponent;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGInitialSortingOrder;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGOption;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGOptions;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGValuelistProvider;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIYWYGParameter;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRule;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRuleAction;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRuleCondition;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRuleEventType;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRules;
import org.nuclos.client.masterdata.MetaDataCache;
import org.nuclos.client.rule.RuleDelegate;
import org.nuclos.client.ui.Errors;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.LocaleInfo;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.XMLUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.layoutml.LayoutMLConstants;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * This class loads a LayoutML XML with the SAX Parser and creates the
 * WYSIWYGComponents shown in the Editor.
 *
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class LayoutMLLoader implements LayoutMLConstants {
	
	private static final Logger LOG = Logger.getLogger(LayoutMLLoader.class);
	
	private static final String SYSTEMID = "http://www.novabit.de/technologies/layoutml/layoutml.dtd";
	private static final String RESOURCE_PATH = "org/nuclos/common2/layoutml/layoutml.dtd";
	//NUCLEUSINT-1137
	private boolean subformEntityMissing = false;

	private boolean subformColumnMissing = false;

	private Collection<MasterDataMetaVO> entities = MetaDataCache.getInstance().getMetaData();

	/**
	 * this vector collects all the WYSIWYGComponents. This is needed to attach
	 * the LayoutMLRules to the component
	 */
	private Vector<WYSIWYGComponent> allWYSIWYGComponents = null;
	private LayoutMLRules rules = null;

	private static final Logger log = Logger.getLogger(LayoutMLLoader.class);

	/**
	 * This Method is called to start the loading of the LayoutML XML
	 *
	 * @see WYSIWYGLayoutControllingPanel#setLayoutML(String)
	 */
	public synchronized void setLayoutML(WYSIWYGLayoutEditorPanel editorPanel, String layoutML) throws CommonBusinessException, SAXException {
		final XMLReader parser = XMLUtils.newSAXParser();
		String warning = null;

		LayoutMLContentHandler mlContentHandler = new LayoutMLContentHandler(editorPanel, warning);
		parser.setContentHandler(mlContentHandler);

		parser.setEntityResolver(new EntityResolver() {
			@Override
			public InputSource resolveEntity(String publicId, String systemId) throws IOException {
				InputSource result = null;
				if (systemId.equals(SYSTEMID)) {
					final URL url = Thread.currentThread().getContextClassLoader().getResource(RESOURCE_PATH);
					if (url == null) {
						throw new NuclosFatalException("Missing DTD for SYSTEMID " + SYSTEMID);
					}
					/** Creation of the Vector. Everything must be clean */
					allWYSIWYGComponents = new Vector<WYSIWYGComponent>();
					rules = new LayoutMLRules();
					// dependencies = null;
					result = new InputSource(new BufferedInputStream(url.openStream()));
				}
				return result;
			}
		});

		try {
			Date startDate = new Date();
			parser.parse(new InputSource(new StringReader(layoutML)));
			Date endDate = new Date();
		} catch (IOException e) {
			log.error(e);
			throw new CommonFatalException(e);
		} catch (SAXException e) {
			log.error(e);
			//NUCLEUSINT-398
			throw new SAXException(e);
		}
	}

	/**
	 * Small Interface used by the Parser for processing the SAX XML Events
	 */
	private interface ElementProcessor {
		public void startElement(Attributes atts) throws SAXException;
		public void closeElement() throws SAXException;
	}

	private class LayoutMLContentHandler implements ContentHandler {

		private Map<String, ElementProcessor> mapProcessors;

		private Stack<Object> stack = new Stack<Object>();

		private WYSIWYGLayoutEditorPanel editorPanel;

		// @SuppressWarnings("unused")
		private String warning;

		private LayoutMLRule actualRule = null;

		/**
		 * The WYSIWYGEditorPanel inculdes the TableLayoutPanel. So these 2
		 * panels would be represented in the LayoutML with 2 <panel> tags. But
		 * when the Editor loads the LayoutML we have to "ignore" one of this
		 * tags because with creating the WYSIWYGEditorPanel a TableLayoutPanel
		 * is also created.
		 */
		public LayoutMLContentHandler(WYSIWYGLayoutEditorPanel editorPanel, String warning) {
			this.editorPanel = editorPanel;
			this.warning = warning;

			this.mapProcessors = new HashMap<String, ElementProcessor>();
			this.mapProcessors.put(ELEMENT_PANEL, new PanelElementProcessor());
			this.mapProcessors.put(ELEMENT_TABLELAYOUT, new TableLayoutElementProcessor());
			this.mapProcessors.put(ELEMENT_TABLELAYOUTCONSTRAINTS, new TableLayoutConstraintsElementProcessor());
			this.mapProcessors.put(ELEMENT_COLLECTABLECOMPONENT, new CollectableComponentElementProcessor());
			this.mapProcessors.put(ELEMENT_PREFERREDSIZE, new PreferredSizeElementProcessor());
			this.mapProcessors.put(ELEMENT_MINIMUMSIZE, new MinimumSizeElementProcessor());
			this.mapProcessors.put(ELEMENT_TABBEDPANE, new TabbedPaneElementProcessor());
			this.mapProcessors.put(ELEMENT_TABBEDPANECONSTRAINTS, new TabbedPaneConstraintsElementProcessor());
			this.mapProcessors.put(ELEMENT_SUBFORM, new SubFormElementProcessor());
			this.mapProcessors.put(ELEMENT_SUBFORMCOLUMN, new SubFormColumnElementProcessor());
			this.mapProcessors.put(ELEMENT_SCROLLPANE, new ScrollPaneElementProcessor());
			this.mapProcessors.put(ELEMENT_BACKGROUND, new BackgroundElementProcessor());
			this.mapProcessors.put(ELEMENT_CLEARBORDER, new ClearBorderElementProcessor());
			this.mapProcessors.put(ELEMENT_TITLEDBORDER, new TitledBorderElementProcessor());
			this.mapProcessors.put(ELEMENT_LINEBORDER, new BorderElementProcessor(ELEMENT_LINEBORDER));
			this.mapProcessors.put(ELEMENT_BEVELBORDER, new BorderElementProcessor(ELEMENT_BEVELBORDER));
			this.mapProcessors.put(ELEMENT_ETCHEDBORDER, new BorderElementProcessor(ELEMENT_ETCHEDBORDER));
			this.mapProcessors.put(ELEMENT_EMPTYBORDER, new BorderElementProcessor(ELEMENT_EMPTYBORDER));
			this.mapProcessors.put(ELEMENT_SPLITPANE, new SplitPaneElementProcessor());
			this.mapProcessors.put(ELEMENT_SPLITPANECONSTRAINTS, new SplitPaneConstraintsElementProcessor());
			/** layoutmlrules */
			this.mapProcessors.put(ELEMENT_RULES, new LayoutMLRulesProcessor());
			this.mapProcessors.put(ELEMENT_RULE, new LayoutMLRuleProcessor());
			this.mapProcessors.put(ELEMENT_EVENT, new LayoutMLRuleEventProcessor());
			this.mapProcessors.put(ELEMENT_CONDITION, new LayoutMLRuleConditionProcessor());
			this.mapProcessors.put(ELEMENT_ACTIONS, new LayoutMLRuleActionsProcessor());

			this.mapProcessors.put(ELEMENT_TRANSFERLOOKEDUPVALUE, new LayoutMLRuleActionTransferLookedupValueProcessor());
			this.mapProcessors.put(ELEMENT_CLEAR, new LayoutMLRuleActionClearProcessor());
			this.mapProcessors.put(ELEMENT_ENABLE, new LayoutMLRuleActionEnableProcessor());
			this.mapProcessors.put(ELEMENT_REFRESHVALUELIST, new LayoutMLRuleActionRefreshValueListProcessor());

			this.mapProcessors.put(ELEMENT_VALUELISTPROVIDER, new LayoutMLValueListProviderProcessor());
			this.mapProcessors.put(ELEMENT_PARAMETER, new LayoutMLParameterProcessor());
			this.mapProcessors.put(ELEMENT_INITIALFOCUSCOMPONENT, new InitialFocusComponentElementProcessor());

			this.mapProcessors.put(ELEMENT_TITLEDSEPARATOR, new StaticElementTitledSeperatorProcessor());
			this.mapProcessors.put(ELEMENT_SEPARATOR, new StaticElementSeperatorProcessor());
			this.mapProcessors.put(ELEMENT_BUTTON, new StaticElementButtonProcessor());
			this.mapProcessors.put(ELEMENT_TEXTFIELD, new StaticElementTextfieldProcessor());
			this.mapProcessors.put(ELEMENT_TEXTAREA, new StaticElementTextareaProcessor());
			this.mapProcessors.put(ELEMENT_LABEL, new StaticElementLabelProcessor());
			this.mapProcessors.put(ELEMENT_COMBOBOX, new StaticElementComboboxProcessor());

			this.mapProcessors.put(ELEMENT_DESCRIPTION, new DescriptionProcessor());
			this.mapProcessors.put(ELEMENT_FONT, new FontElementProcessor());
			this.mapProcessors.put(ELEMENT_INITIALSORTINGORDER, new InitialSortingOrderElementProcessor());

			this.mapProcessors.put(ELEMENT_OPTIONS, new OptionsProcessor());
			this.mapProcessors.put(ELEMENT_OPTION, new OptionProcessor());

			this.mapProcessors.put(ELEMENT_PROPERTY, new CollectableComponentPropertyProcessor());

			this.mapProcessors.put(ELEMENT_TRANSLATIONS, new TranslationsProcessor());
			this.mapProcessors.put(ELEMENT_TRANSLATION, new TranslationProcessor());

			// this.mapProcessors.put(ELEMENT_DEPENDENCY, new
			// LayoutMLDependencyProcessor());
			// this.mapProcessors.put(ELEMENT_DEPENDENCIES, new
			// LayoutMLDependenciesProcessor());
		}



		/**
		 * Peeking the Stack to look whats on Top. Returns the WYSIWYGComponent
		 * on top without removing it from the Stack. Needed for attaching
		 * everything in between ComponentTags (like preferred-size etc)
		 *
		 * @return @see WYSIWYGComponent on Top
		 */
		private WYSIWYGComponent peekComponent() {
			if (stack.peek() instanceof TableLayoutPanel) {
				return (WYSIWYGComponent) stack.get(stack.size() - 2);
			} else {
				return (WYSIWYGComponent) stack.peek();
			}
		}

		/**
		 * Due the nested TableLayoutpanels this Method is needed to return the
		 * fitting @see TableLayoutPanel
		 *
		 * @return @see WYSIWYGComponent
		 */
		private WYSIWYGComponent peekParentComponent() {
			if (stack.peek() instanceof TableLayoutPanel) {
				if (stack.get(stack.size() - 3) instanceof TableLayoutPanel) {
					return (WYSIWYGComponent) stack.get(stack.size() - 4);
				} else {
					return (WYSIWYGComponent) stack.get(stack.size() - 3);
				}
			} else {
				if (stack.get(stack.size() - 2) instanceof TableLayoutPanel) {
					return (WYSIWYGComponent) stack.get(stack.size() - 3);
				} else {
					return (WYSIWYGComponent) stack.get(stack.size() - 2);
				}
			}
		}

		/**
		 * Method called by the SAX Parser
		 *
		 * Element is coming in and the fitting processor is taken from
		 * {@link #mapProcessors} and the Attributes are passed.
		 */
		@Override
		public synchronized void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
			ElementProcessor ep = mapProcessors.get(name);
			if (ep != null) {
				ep.startElement(atts);
			} else if (!"layoutml".equals(name) && !"layout".equals(name)) {
				throw new SAXException("No Processor registered for an Element with name: " + name + "!");
			}
		}

		/**
		 * Method called by the SAX Parser
		 *
		 * Element has ended and the fitting ElementProcessor is taken from {@link #mapProcessors} and calls {@link ElementProcessor#closeElement())
		 */
		@Override
		public synchronized void endElement(String uri, String localName, String name) throws SAXException {
			ElementProcessor ep = mapProcessors.get(name);
			if (ep != null) {
				ep.closeElement();
			}
		}

		/**
		 * the chars that are collected between start and end tags.
		 */
		private StringBuffer sbChars;

		@Override
		public synchronized void characters(char[] ch, int start, int length) throws SAXException {
			if (this.sbChars != null) {
				this.sbChars.append(ch, start, length);
			}
		}

		@Override
		public void endDocument() throws SAXException {}

		@Override
		public void endPrefixMapping(String prefix) throws SAXException {}

		@Override
		public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}

		@Override
		public void processingInstruction(String target, String data) throws SAXException {}

		@Override
		public void setDocumentLocator(Locator locator) {}

		@Override
		public void skippedEntity(String name) throws SAXException {}

		@Override
		public void startDocument() throws SAXException {}

		@Override
		public void startPrefixMapping(String prefix, String uri) throws SAXException {}

		/**
		 * {@link ElementProcessor} for Layoutpanels
		 * @see WYSIWYGLayoutEditorPanel
		 *
		 */
		private class PanelElementProcessor implements ElementProcessor {

			@Override
			public void startElement(Attributes atts) {
				if (stack.empty()) {
					stack.push(editorPanel);
				} else {
					if (!(stack.peek() instanceof WYSIWYGLayoutEditorPanel)) {
						WYSIWYGLayoutEditorPanel subEditor = new WYSIWYGLayoutEditorPanel(editorPanel.getMetaInformation());
						setPropertiesFromAttributes(subEditor, atts);
						if (stack.peek() instanceof WYSIWYGScrollPane) {
							((WYSIWYGScrollPane) stack.peek()).setViewportView(subEditor);
						}
						stack.push(subEditor);
					} else {
						stack.push(((WYSIWYGLayoutEditorPanel) peekComponent()).getTableLayoutPanel());
					}
				}
			}

			@Override
			public void closeElement() throws SAXException {
				stack.pop();
			}
		}

		/**
		 * {@link ElementProcessor} for TableLayout
		 * @see TableLayout
		 */
		private class TableLayoutElementProcessor implements ElementProcessor {

			@Override
			public void startElement(Attributes atts) throws SAXException {
				if (stack.peek() instanceof TableLayoutPanel) {
					final char cSeparator = '|';

					String sCols = atts.getValue(ATTRIBUTE_COLUMNS);
					String sRows = atts.getValue(ATTRIBUTE_ROWS);

					try {
						double[] columns = StringUtils.getDoubleArrayFromString(sCols, cSeparator);
						double[] rows = StringUtils.getDoubleArrayFromString(sRows, cSeparator);

						LayoutCell lc = new LayoutCell();
						for (int i = 0; i < columns.length; i++) {
							lc.setCellWidth(columns[i]);
							lc.setCellX(i);
							((WYSIWYGLayoutEditorPanel) peekComponent()).getTableLayoutUtil().addCol(lc);
						}

						lc = new LayoutCell();
						for (int i = 0; i < rows.length; i++) {
							lc.setCellHeight(rows[i]);
							lc.setCellY(i);
							((WYSIWYGLayoutEditorPanel) peekComponent()).getTableLayoutUtil().addRow(lc);
						}

					} catch (NumberFormatException ex) {
						log.error(ex);
						throw new SAXException("Liste der Spalten und Zeilen eines TableLayouts darf nur Dezimalzahlen enthalten.");
					}
				}
			}

			@Override
			public void closeElement() {
			}
		}

		/**
		 * {@link ElementProcessor} for TableLayoutConstraints
		 * @see TableLayoutConstraints
		 */
		private class TableLayoutConstraintsElementProcessor implements ElementProcessor {

			@Override
			public void startElement(Attributes atts) throws SAXException {
				if (stack.peek() instanceof WYSIWYGComponent) {
					final TableLayoutConstraints contraints = new TableLayoutConstraints();

					try {
						contraints.col1 = Integer.parseInt(atts.getValue(ATTRIBUTE_COL1));
						contraints.col2 = Integer.parseInt(atts.getValue(ATTRIBUTE_COL2));
						contraints.row1 = Integer.parseInt(atts.getValue(ATTRIBUTE_ROW1));
						contraints.row2 = Integer.parseInt(atts.getValue(ATTRIBUTE_ROW2));
						contraints.hAlign = Integer.parseInt(atts.getValue(ATTRIBUTE_HALIGN));
						contraints.vAlign = Integer.parseInt(atts.getValue(ATTRIBUTE_VALIGN));

						((WYSIWYGLayoutEditorPanel) peekParentComponent()).getTableLayoutUtil().insertComponentTo(peekComponent(), contraints);

					} catch (NumberFormatException ex) {
						log.error(ex);
						throw new SAXException("Ganze Zahl erwartet f\u00fcr Constraints des Elements \"" + ELEMENT_TABLELAYOUTCONSTRAINTS + "\".");
					}
				}
			}

			@Override
			public void closeElement() {
			}
		}

		/**
		 * {@link ElementProcessor} for CollectableComponents
		 * @see WYSIWYGCollectableComponent
		 */
		private class CollectableComponentElementProcessor implements ElementProcessor {

			@Override
			public void startElement(Attributes atts) throws SAXException {
				String controlType = atts.getValue(ATTRIBUTE_CONTROLTYPE);
				String showOnly = atts.getValue(ATTRIBUTE_SHOWONLY);
				String name = atts.getValue(ATTRIBUTE_NAME);

				if (showOnly != null && showOnly.equals(ATTRIBUTEVALUE_LABEL)) {
					controlType = CONTROLTYPE_LABEL;
				}
				Component component = null;
				try {
					component = ComponentProcessors.getInstance().createComponent(ELEMENT_COLLECTABLECOMPONENT, controlType, 0, editorPanel.getMetaInformation(), name);
				} catch (CommonBusinessException e) {
					log.error(e);
					Errors.getInstance().showExceptionDialog(null, e);
				}

				if (component instanceof WYSIWYGComponent) {
					WYSIWYGComponent c = (WYSIWYGComponent) component;
					setPropertiesFromAttributes(c, atts);
					migrateLocaleResourceId(c, atts);
					//NUCLEUSINT-288
					if (showOnly == null)
						try {
							c.getProperties().setProperty(WYSIWYGUniversalComponent.PROPERTY_SHOWONLY, new PropertyValueString(WYSIWYGUniversalComponent.ATTRIBUTEVALUE_LABEL_AND_CONTROL), String.class);
						} catch (CommonBusinessException e) {
							LOG.warn("startElement failed: " + e, e);
						}
					stack.push(c);
					if (c.getLayoutMLRulesIfCapable() != null)
						allWYSIWYGComponents.add(c);
				}
			}

			@Override
			public void closeElement() {
				stack.pop();
			}
		}

		/**
		 * {@link ElementProcessor} for WYSIYWYGStaticTitledSeparator
		 * @see WYSIWYGStaticTitledSeparator
		 */
		private class StaticElementTitledSeperatorProcessor implements ElementProcessor {

			@Override
			public void closeElement() throws SAXException {
				stack.pop();
			}

			@Override
			public void startElement(Attributes atts) throws SAXException {
				try {
					Component component = ComponentProcessors.getInstance().createComponent(ELEMENT_TITLEDSEPARATOR, "", 0, editorPanel.getMetaInformation(), null);
					if (component instanceof WYSIWYGComponent) {
						WYSIWYGComponent c = (WYSIWYGComponent) component;
						setPropertiesFromAttributes(c, atts);
						migrateLocaleResourceId(c, atts);
						stack.push(c);
					}
				} catch (CommonBusinessException e) {
					log.error(e);
					Errors.getInstance().showExceptionDialog(null, e);
				}
			}

		}

		/**
		 * {@link ElementProcessor} for WYSIYWYGStaticSeparator
		 * @see WYSIWYGStaticSeparator
		 */
		private class StaticElementSeperatorProcessor implements ElementProcessor {

			@Override
			public void closeElement() throws SAXException {
				stack.pop();
			}

			@Override
			public void startElement(Attributes atts) throws SAXException {
				try {
					Component component = ComponentProcessors.getInstance().createComponent(ELEMENT_SEPARATOR, "", 0, editorPanel.getMetaInformation(), null);
					if (component instanceof WYSIWYGComponent) {
						WYSIWYGComponent c = (WYSIWYGComponent) component;
						setPropertiesFromAttributes(c, atts);
						stack.push(c);
					}
				} catch (CommonBusinessException e) {
					log.error(e);
					Errors.getInstance().showExceptionDialog(null, e);
				}
			}

		}

		/**
		 * {@link ElementProcessor} for WYSIYWYGStaticButton
		 * @see WYSIWYGStaticButton
		 */
		private class StaticElementButtonProcessor implements ElementProcessor {

			@Override
			public void closeElement() throws SAXException {
				stack.pop();
			}

			@Override
			public void startElement(Attributes atts) throws SAXException {
				try {
					Component component = ComponentProcessors.getInstance().createComponent(ELEMENT_BUTTON, "", 0, editorPanel.getMetaInformation(), null);
					if (component instanceof WYSIWYGComponent) {
						WYSIWYGComponent c = (WYSIWYGComponent) component;
						setPropertiesFromAttributes(c, atts);
						migrateLocaleResourceId(c, atts);
						stack.push(c);
					}
				} catch (CommonBusinessException e) {
					log.error(e);
					Errors.getInstance().showExceptionDialog(null, e);
				}
			}

		}

		/**
		 * {@link ElementProcessor} for WYSIYWYGStaticTextfield
		 * @see WYSIWYGStaticTextfield
		 */
		private class StaticElementTextfieldProcessor implements ElementProcessor {

			@Override
			public void closeElement() throws SAXException {
				stack.pop();
			}

			@Override
			public void startElement(Attributes atts) throws SAXException {
				try {
					Component component = ComponentProcessors.getInstance().createComponent(ELEMENT_TEXTFIELD, "", 0, editorPanel.getMetaInformation(), null);
					if (component instanceof WYSIWYGComponent) {
						WYSIWYGComponent c = (WYSIWYGComponent) component;
						setPropertiesFromAttributes(c, atts);
						stack.push(c);
					}
				} catch (CommonBusinessException e) {
					log.error(e);
					Errors.getInstance().showExceptionDialog(null, e);
				}
			}

		}

		/**
		 * {@link ElementProcessor} for WYSIYWYGStaticTextfield
		 * @see WYSIWYGStaticTextfield
		 */
		private class StaticElementImageProcessor implements ElementProcessor {

			@Override
			public void closeElement() throws SAXException {
				stack.pop();
			}

			@Override
			public void startElement(Attributes atts) throws SAXException {
				try {
					Component component = ComponentProcessors.getInstance().createComponent(ELEMENT_IMAGE, "", 0, editorPanel.getMetaInformation(), null);
					if (component instanceof WYSIWYGComponent) {
						WYSIWYGComponent c = (WYSIWYGComponent) component;
						setPropertiesFromAttributes(c, atts);
						stack.push(c);
					}
				} catch (CommonBusinessException e) {
					log.error(e);
					Errors.getInstance().showExceptionDialog(null, e);
				}
			}

		}

		/**
		 * {@link ElementProcessor} for WYSIYWYGStaticTextarea
		 * @see WYSIWYGStaticTextarea
		 */
		private class StaticElementTextareaProcessor implements ElementProcessor {

			@Override
			public void closeElement() throws SAXException {
				stack.pop();
			}

			@Override
			public void startElement(Attributes atts) throws SAXException {
				try {
					Component component = ComponentProcessors.getInstance().createComponent(ELEMENT_TEXTAREA, "", 0, editorPanel.getMetaInformation(), null);
					if (component instanceof WYSIWYGComponent) {
						WYSIWYGComponent c = (WYSIWYGComponent) component;
						setPropertiesFromAttributes(c, atts);
						stack.push(c);
					}
				} catch (CommonBusinessException e) {
					log.error(e);
					Errors.getInstance().showExceptionDialog(null, e);
				}
			}

		}

		/**
		 * {@link ElementProcessor} for WYSIYWYGStaticLabel
		 * @see WYSIWYGStaticLabel
		 */
		private class StaticElementLabelProcessor implements ElementProcessor {

			@Override
			public void closeElement() throws SAXException {
				stack.pop();
			}

			@Override
			public void startElement(Attributes atts) throws SAXException {
				try {
					Component component = ComponentProcessors.getInstance().createComponent(ELEMENT_LABEL, "", 0, editorPanel.getMetaInformation(), null);
					if (component instanceof WYSIWYGComponent) {
						WYSIWYGComponent c = (WYSIWYGComponent) component;
						setPropertiesFromAttributes(c, atts);
						migrateLocaleResourceId(c, atts);
						stack.push(c);
					}
				} catch (CommonBusinessException e) {
					log.error(e);
					Errors.getInstance().showExceptionDialog(null, e);
				}
			}

		}

		/**
		 * {@link ElementProcessor} for WYSIYWYGStaticComboBox
		 * @see WYSIWYGStaticComboBox
		 */
		private class StaticElementComboboxProcessor implements ElementProcessor {

			@Override
			public void closeElement() throws SAXException {
				stack.pop();
			}

			@Override
			public void startElement(Attributes atts) throws SAXException {
				try {
					Component component = ComponentProcessors.getInstance().createComponent(ELEMENT_COMBOBOX, "", 0, editorPanel.getMetaInformation(), null);
					if (component instanceof WYSIWYGComponent) {
						WYSIWYGComponent c = (WYSIWYGComponent) component;
						setPropertiesFromAttributes(c, atts);
						stack.push(c);
					}
				} catch (CommonBusinessException e) {
					log.error(e);
					Errors.getInstance().showExceptionDialog(null, e);
				}
			}

		}

		/**
		 * {@link ElementProcessor} for PropertyValueDescription
		 * @see PropertyValueDescription
		 */
		private class DescriptionProcessor implements ElementProcessor {

			@Override
			public void closeElement() throws SAXException {
				String sDescription = sbChars.toString();
				sDescription = sDescription.trim();
				sbChars = null;
				PropertyValueString description = new PropertyValueString();
				description.setValue(sDescription);
				try {
					peekComponent().setProperty(WYSIWYGComponent.PROPERTY_DESCRIPTION, description, String.class);
				} catch (CommonBusinessException e) {
					log.error(e);
					Errors.getInstance().showExceptionDialog(null, e);
				}
			}

			@Override
			public void startElement(Attributes atts) throws SAXException {
				sbChars = new StringBuffer();
			}
		}

		private PropertyOptions propertyOptions = null;

		/**
		 * {@link ElementProcessor} for CollectableOptionGroup
		 * @see WYSIWYGCollectableOptionGroup
		 */
		private class OptionsProcessor implements ElementProcessor {

			@Override
			public void closeElement() throws SAXException {
				try {
					peekComponent().setProperty(WYSIWYGCollectableOptionGroup.PROPERTY_OPTIONS, propertyOptions, WYSIWYGOptions.class);
				} catch (CommonBusinessException e) {
					log.error(e);
					Errors.getInstance().showExceptionDialog(null, e);
				}
				propertyOptions = null;
			}

			@Override
			public void startElement(Attributes atts) throws SAXException {
				propertyOptions = new PropertyOptions();
				propertyOptions.setValue(ELEMENT_OPTIONS, atts);
			}
		}

		/**
		 * {@link ElementProcessor} for PropertyOptions
		 * @see PropertyOptions
		 */
		private class OptionProcessor implements ElementProcessor {

			@Override
			public void startElement(Attributes atts) throws SAXException {
				propertyOptions.setValue(ELEMENT_OPTION, atts);
				List<WYSIWYGOption> options = propertyOptions.getValue().getAllOptionValues();
				if (options.size() > 0) {
					translations = new TranslationMap();
					options.get(options.size() - 1).setTranslations(translations);
				}
			}

			@Override
			public void closeElement() throws SAXException {
				translations = null;
			}
		}

		/**
		 * {@link ElementProcessor} for CollectableComponent Property
		 * @see PropertyOptions
		 */
		private class CollectableComponentPropertyProcessor implements ElementProcessor {

			@Override
			public void closeElement() throws SAXException {
			}
			//NUCLOSINT-743 if there is a rule set as collectablecomponent-property restore as normal property
			@Override
			public void startElement(Attributes atts) throws SAXException {
				if (peekComponent() instanceof WYSIWYGStaticButton) {
						String label = atts.getValue("name");
						String ruleId = atts.getValue("value");
					if ("ruletoexecute".equals(label)) {
						RuleVO ruleToExecute;
						try {
							ruleToExecute = RuleDelegate.getInstance().get(Integer.parseInt(ruleId));
							final PropertyValue<String> pv = (PropertyValue<String>) 
									peekComponent().getProperties().getProperty(WYSIWYGStaticButton.PROPERTY_RULE);
							pv.setValue(ruleToExecute.getName());
						} catch (Exception e) {
							LOG.warn("startElement failed: " + e, e);
						}
					} else {
						peekComponent().getProperties().getProperty(WYSIWYGCollectableComponent.PROPERTY_COLLECTABLECOMPONENTPROPERTY).setValue(ELEMENT_PROPERTY, atts);
					}
				} else
					peekComponent().getProperties().getProperty(WYSIWYGCollectableComponent.PROPERTY_COLLECTABLECOMPONENTPROPERTY).setValue(ELEMENT_PROPERTY, atts);
			}
		}

		/**
		 * {@link ElementProcessor} for PreferredSize
		 * @see PropertyOptions
		 */
		private class PreferredSizeElementProcessor implements ElementProcessor {

			@Override
			public void startElement(Attributes atts) throws SAXException {
				try {
					PropertyValue<?> value = PropertyUtils.getPropertyValue(peekComponent(), WYSIWYGCollectableComponent.PROPERTY_PREFFEREDSIZE);
					value.setValue(ELEMENT_PREFERREDSIZE, atts);

					peekComponent().setProperty(WYSIWYGCollectableComponent.PROPERTY_PREFFEREDSIZE, value, PropertyUtils.getValueClass(peekComponent(), WYSIWYGCollectableComponent.PROPERTY_PREFFEREDSIZE));
				} catch (CommonBusinessException e) {
					log.error(e);
					Errors.getInstance().showExceptionDialog(null, e);
				}
			}

			@Override
			public void closeElement() {
			}
		}

		/**
		 * {@link ElementProcessor} for MinimumSize
		 * @see PropertyOptions
		 */
		private class MinimumSizeElementProcessor implements ElementProcessor {

			@Override
			public void startElement(Attributes atts) throws SAXException {
				String sHeight = atts.getValue(ATTRIBUTE_HEIGHT);
				String sWidth = atts.getValue(ATTRIBUTE_WIDTH);

				try {
					Dimension dim = new Dimension(Integer.parseInt(sWidth), Integer.parseInt(sHeight));

					((Component) peekComponent()).setMinimumSize(dim);

				} catch (NumberFormatException ex) {
					log.error(ex);
					throw new SAXException("Ganze Zahl erwartet f\u00fcr Attribut \"" + ELEMENT_MINIMUMSIZE + "\".");
				}
			}

			@Override
			public void closeElement() {
			}
		}

		/**
		 * {@link ElementProcessor} for WYSIWYGSplitPane
		 * @see WYSIWYGSplitPane
		 */
		private class SplitPaneElementProcessor implements ElementProcessor {

			@Override
			public void startElement(Attributes atts) throws SAXException {
				try {
					Component component = ComponentProcessors.getInstance().createComponent(ELEMENT_SPLITPANE, null, 0, editorPanel.getMetaInformation(), null);
					if (component instanceof WYSIWYGComponent) {
						WYSIWYGComponent c = (WYSIWYGComponent) component;
						setPropertiesFromAttributes(c, atts);
						stack.push(c);
					}
				} catch (CommonBusinessException e) {
					log.error(e);
					Errors.getInstance().showExceptionDialog(null, e);
				}
			}

			@Override
			public void closeElement() {
				stack.pop();
			}
		}

		/**
		 * {@link ElementProcessor} for WYSIWYGSplitPane Constraints
		 * @see WYSIWYGSplitPane
		 */
		private class SplitPaneConstraintsElementProcessor implements ElementProcessor {

			@Override
			public void startElement(Attributes atts) throws SAXException {
				WYSIWYGLayoutEditorPanel component = (WYSIWYGLayoutEditorPanel) peekComponent();
				WYSIWYGSplitPane splitpane = (WYSIWYGSplitPane) peekParentComponent();

				String position = atts.getValue(ATTRIBUTE_POSITION);
				if (ATTRIBUTEVALUE_LEFT.equals(position) || ATTRIBUTEVALUE_TOP.equals(position)) {
					splitpane.setFirstEditor(component);
				} else if (ATTRIBUTEVALUE_RIGHT.equals(position) || ATTRIBUTEVALUE_BOTTOM.equals(position)) {
					splitpane.setSecondEditor(component);
				}
			}

			@Override
			public void closeElement() {
			}
		}

		/**
		 * {@link ElementProcessor} for WYSIWYGTabbedPane
		 * @see WYSIWYGTabbedPane
		 */
		private class TabbedPaneElementProcessor implements ElementProcessor {

			@Override
			public void startElement(Attributes atts) throws SAXException {
				try {
					WYSIWYGTabbedPane tabbedPane = (WYSIWYGTabbedPane) ComponentProcessors.getInstance().createComponent(ELEMENT_TABBEDPANE, null, 0, editorPanel.getMetaInformation(), null);
					setPropertiesFromAttributes(tabbedPane, atts);
					stack.push(tabbedPane);
				} catch (CommonBusinessException e) {
					log.error(e);
					Errors.getInstance().showExceptionDialog(null, e);
				}
			}

			@Override
			public void closeElement() {
				stack.pop();
			}
		}

		/**
		 * {@link ElementProcessor} for WYSIWYGTabbedPane Constraints
		 * @see WYSIWYGTabbedPane
		 */
		private class TabbedPaneConstraintsElementProcessor implements ElementProcessor {

			@Override
			public void startElement(Attributes atts) throws SAXException {
				WYSIWYGLayoutEditorPanel panel = (WYSIWYGLayoutEditorPanel) peekComponent();
				WYSIWYGTabbedPane tabbedPane = (WYSIWYGTabbedPane) peekParentComponent();
				String sInternalName = atts.getValue(ATTRIBUTE_INTERNALNAME);
				panel.setName(sInternalName);

				String title = atts.getValue(ATTRIBUTE_TITLE);
				tabbedPane.addTab(title, panel);
				tabbedPane.getTabTitles().put(tabbedPane.getTabCount() - 1, title);
				// setup translation map as target for the processing of translation elements
				translations = new TranslationMap();
				migrateLocaleResourceId(translations, atts);
				tabbedPane.getTabTranslations().put(tabbedPane.getTabCount() - 1, translations);

				String sMnemonic = atts.getValue(ATTRIBUTE_MNEMONIC);
				if(sMnemonic != null) {
					Integer iMnemonic = Integer.parseInt(sMnemonic);
					tabbedPane.setMnemonicAt(tabbedPane.getTabCount() - 1 , iMnemonic);
				}

				String enabled = atts.getValue(ATTRIBUTE_ENABLED);
				if (enabled != null) {
					if (enabled.equals(ATTRIBUTEVALUE_YES)) {
						tabbedPane.setEnabledAt(tabbedPane.getTabCount() - 1, true);
					} else if (enabled.equals(ATTRIBUTEVALUE_NO)) {
						tabbedPane.setEnabledAt(tabbedPane.getTabCount() - 1, false);
					}
				}
			}

			@Override
			public void closeElement() {
				// remove translation target
				translations = null;
			}
		}

		/**
		 * {@link ElementProcessor} for WYSIWYGSubForm
		 * @see WYSIWYGSubForm
		 */
		private class SubFormElementProcessor implements ElementProcessor {


			@Override
			public void startElement(Attributes atts) throws SAXException {
				try {
					WYSIWYGSubForm subform = (WYSIWYGSubForm) ComponentProcessors.getInstance().createComponent(ELEMENT_SUBFORM, null, 0, editorPanel.getMetaInformation(), null);
					String entity = atts.getValue(LayoutMLConstants.ATTRIBUTE_ENTITY);
						//NUCLEUSINT-1137
						// does this entity still exist?

						boolean found = false;
						for (MasterDataMetaVO subformEntity : entities) {
							if (entity.equals(subformEntity.getEntityName())) {
								found = true;
								break;
							}
						}
						if (found) {
							setPropertiesFromAttributes(subform, atts);
							stack.push(subform);
							allWYSIWYGComponents.add(subform);
							subformEntityMissing = false;
						} else {
							// fallback handling for a entity that does not exist anymore. the subformcolumns are not restored, instead of the subform a label with information is shown
							subformEntityMissing = true;
							Component component = ComponentProcessors.getInstance().createComponent(ELEMENT_LABEL, "", 0, editorPanel.getMetaInformation(), null);
							((WYSIWYGStaticLabel)component).setText(WYSIWYGStringsAndLabels.partedString(ERROR_MESSAGES.ENTITY_USED_IN_LAYOUT_MISSING, entity));
							((WYSIWYGStaticLabel)component).setHorizontalAlignment(javax.swing.JLabel.CENTER);
							((WYSIWYGStaticLabel)component).setForeground(Color.RED);
							stack.push(component);
					}
				} catch (CommonBusinessException e) {
					log.error(e);
					Errors.getInstance().showExceptionDialog(null, e);
				}
			}

			@Override
			public void closeElement() {
				if (!subformEntityMissing){
					//NUCLEUSINT-1137
					// refresh subform afterwards, otherwise there are display related problems like error messages occoured during loading
					WYSIWYGSubForm subform = (WYSIWYGSubForm)stack.pop();
					subform.finalizeInitialLoading();
				} else {
					stack.pop();
				}
			}
		}

		/**
		 * {@link ElementProcessor} for WYSIWYGSubFormColumn
		 * @see WYSIWYGSubFormColumn
		 */
		private class SubFormColumnElementProcessor implements ElementProcessor {

			@Override
			public void startElement(Attributes atts) throws SAXException {
				//NUCLEUSINT-1137
				if (!subformEntityMissing) {
					if (stack.peek() instanceof WYSIWYGSubForm) {
						WYSIWYGSubForm subform = (WYSIWYGSubForm) stack.peek();

						String name = atts.getValue(ATTRIBUTE_NAME);
						String label = atts.getValue(ATTRIBUTE_LABEL);
						try {
							WYSIWYGSubFormColumn column = new WYSIWYGSubFormColumn(subform, NuclosCollectableEntityProvider.getInstance().getCollectableEntity(subform.getEntityName()).getEntityField(name));
							ComponentProperties columnProperties = PropertyUtils.getEmptyProperties(column, editorPanel.getMetaInformation());
							PropertyValueString value = new PropertyValueString(name);
							PropertyValueString valueLabel = new PropertyValueString(label);
							try {
								columnProperties.setProperty(WYSIWYGSubFormColumn.PROPERTY_DEFAULTVALUES, new PropertyValueBoolean(false), null);
								columnProperties.setProperty(WYSIWYGSubFormColumn.PROPERTY_NAME, value, null);
								columnProperties.setProperty(WYSIWYGSubFormColumn.PROPERTY_LABEL, valueLabel, null);
							} catch (CommonBusinessException e) {
								log.error(e);
								Errors.getInstance().showExceptionDialog(null, e);
							}

							column.setProperties(columnProperties);
							setPropertiesFromAttributes(column, atts);
							migrateLocaleResourceId(column, atts);

							subform.addColumn(name, column);
							stack.push(column);
						} catch (Exception e) {
							subformColumnMissing = true;
						}
					}
				}
			}

			@Override
			public void closeElement() {
				if (!subformColumnMissing) {
					//NUCLEUSINT-1137
					if (!subformEntityMissing)
						stack.pop();
				}

				subformColumnMissing = false;
			}
		}

		/**
		 * {@link ElementProcessor} for WYSIWYGScrollPane
		 * @see WYSIWYGScrollPane
		 */
		private class ScrollPaneElementProcessor implements ElementProcessor {

			@Override
			public void startElement(Attributes atts) throws SAXException {
				try {
					Component component = ComponentProcessors.getInstance().createComponent(ELEMENT_SCROLLPANE, null, 0, editorPanel.getMetaInformation(), null);
					setPropertiesFromAttributes((WYSIWYGScrollPane) component, atts);
					stack.push(component);
				} catch (CommonBusinessException e) {
					log.error(e);
					Errors.getInstance().showExceptionDialog(null, e);
				}
			}

			@Override
			public void closeElement() {
				stack.pop();
			}
		}

		/**
		 * {@link ElementProcessor} for BackgroundColor
		 * @see Color
		 */
		private class BackgroundElementProcessor implements ElementProcessor {

			@Override
			public void startElement(Attributes atts) throws SAXException {
				try {
					WYSIWYGComponent c = peekComponent();
					PropertyValue<?> value = PropertyUtils.getPropertyValue(c, WYSIWYGComponent.PROPERTY_BACKGROUNDCOLOR);
					value.setValue(ELEMENT_BACKGROUND, atts);

					c.setProperty(WYSIWYGComponent.PROPERTY_BACKGROUNDCOLOR, value, PropertyUtils.getValueClass(c, WYSIWYGComponent.PROPERTY_BACKGROUNDCOLOR));
				} catch (CommonBusinessException e) {
					log.error(e);
					Errors.getInstance().showExceptionDialog(null, e);
				}
			}

			@Override
			public void closeElement() {
			}
		}

		/**
		 * {@link ElementProcessor} for ClearBorder
		 * @see Border
		 */
		private class ClearBorderElementProcessor implements ElementProcessor {

			@Override
			public void startElement(Attributes atts) throws SAXException {
				WYSIWYGComponent c = peekComponent();
				PropertyValue<?> value;
				if (c.getProperties() != null && c.getProperties().getProperty(WYSIWYGComponent.PROPERTY_BORDER) != null) {
					value = c.getProperties().getProperty(WYSIWYGComponent.PROPERTY_BORDER);
				} else {
					value = PropertyUtils.getPropertyValue(c, WYSIWYGComponent.PROPERTY_BORDER);
				}
				value.setValue(ELEMENT_CLEARBORDER, atts);
				try {
					c.setProperty(WYSIWYGComponent.PROPERTY_BORDER, value, PropertyUtils.getValueClass(c, WYSIWYGComponent.PROPERTY_BORDER));
				} catch (CommonBusinessException ex) {
					log.error(ex);
					Errors.getInstance().showExceptionDialog(null, ex);
				}
			}

			@Override
			public void closeElement() {
			}
		}

		/**
		 * {@link ElementProcessor} for ClearBorder
		 * @see Border
		 */
		private class BorderElementProcessor implements ElementProcessor {

			private final String element;

			public BorderElementProcessor(String element) {
				this.element = element;
			}

			@Override
			public void startElement(Attributes atts) throws SAXException {
				WYSIWYGComponent c = peekComponent();
				PropertyValue<?> value;
				if (c.getProperties() != null && c.getProperties().getProperty(WYSIWYGComponent.PROPERTY_BORDER) != null) {
					value = c.getProperties().getProperty(WYSIWYGComponent.PROPERTY_BORDER);
				} else {
					value = PropertyUtils.getPropertyValue(c, WYSIWYGComponent.PROPERTY_BORDER);
				}
				value.setValue(this.element, atts);
				try {
					c.setProperty(WYSIWYGComponent.PROPERTY_BORDER, value, PropertyUtils.getValueClass(c, WYSIWYGComponent.PROPERTY_BORDER));
				} catch (CommonBusinessException ex) {
					log.error(ex);
					Errors.getInstance().showExceptionDialog(null, ex);
				}
			}

			@Override
			public void closeElement() {
			}
		}

		public class TitledBorderElementProcessor extends BorderElementProcessor {

			public TitledBorderElementProcessor() {
				super(ELEMENT_TITLEDBORDER);
			}

			@Override
			public void startElement(Attributes atts) throws SAXException {
				super.startElement(atts);
				WYSIWYGComponent c = peekComponent();
				if (c.getProperties() != null && c.getProperties().getProperty(WYSIWYGComponent.PROPERTY_BORDER) != null) {
					PropertyValue<?> p = c.getProperties().getProperty(WYSIWYGComponent.PROPERTY_BORDER);
					Object value = p.getValue();
					if (value instanceof CompoundBorder)
						value = ((CompoundBorder) value).getOutsideBorder();
					if (value instanceof TitledBorderWithTranslations) {
						TitledBorderWithTranslations b = (TitledBorderWithTranslations) value;
						// setup target for nested translation elements
						translations = b.getTranslations();
						migrateLocaleResourceId(translations, atts);
					}
				}
			}

			@Override
			public void closeElement() {
				super.closeElement();
				// remove translation target
				translations = null;
			}
		}

		/**
		 * {@link ElementProcessor} for LayoutMLRules
		 * @see LayoutMLRules
		 */
		private class LayoutMLRulesProcessor implements ElementProcessor {

			@Override
			public void closeElement() throws SAXException {
				if (editorPanel != null) {
					editorPanel.handoverRules(rules, allWYSIWYGComponents);
				}
			}

			@Override
			public void startElement(Attributes atts) throws SAXException {
			}

		}

		/**
		 * {@link ElementProcessor} for LayoutMLRule
		 * @see LayoutMLRule
		 */
		private class LayoutMLRuleProcessor implements ElementProcessor {

			@Override
			public void closeElement() throws SAXException {
				rules.addRule(actualRule);
			}

			@Override
			public void startElement(Attributes atts) throws SAXException {
				String ruleName = atts.getValue(ATTRIBUTE_NAME);
				actualRule = new LayoutMLRule(ruleName);
			}

		}

		/**
		 * {@link ElementProcessor} for LayoutMLRuleEventType
		 * @see LayoutMLRuleEventType
		 */
		private class LayoutMLRuleEventProcessor implements ElementProcessor {

			@Override
			public void closeElement() throws SAXException {
			}

			@Override
			public void startElement(Attributes atts) throws SAXException {
				String type = atts.getValue(ATTRIBUTE_TYPE);
				String sourcecomponent = atts.getValue(ATTRIBUTE_SOURCECOMPONENT);
				String entity = atts.getValue(ATTRIBUTE_ENTITY);

				actualRule.getLayoutMLRuleEventType().setEventType(type);
				actualRule.getLayoutMLRuleEventType().setEntity(entity);
				if (entity != null)
					actualRule.setSubformEntity();
				actualRule.getLayoutMLRuleEventType().setSourceComponent(sourcecomponent);
			}
		}

		/**
		 * {@link ElementProcessor} for LayoutMLRuleCondition
		 * @see LayoutMLRuleCondition#LayoutMLRulesProcessor
		 *
		 * Not implemented by LayoutML Parser
		 */
		private class LayoutMLRuleConditionProcessor implements ElementProcessor {

			@Override
			public void closeElement() throws SAXException {
			}

			@Override
			public void startElement(Attributes atts) throws SAXException {
			}

		}

		/**
		 * {@link ElementProcessor} for LayoutMLRuleAction
		 * @see LayoutMLRuleAction
		 */
		private class LayoutMLRuleActionsProcessor implements ElementProcessor {

			@Override
			public void closeElement() throws SAXException {
			}

			@Override
			public void startElement(Attributes atts) throws SAXException {
			}

		}

		/**
		 * {@link ElementProcessor} for LayoutMLRuleAction
		 * @see LayoutMLRuleAction#TRANSFER_LOOKEDUP_VALUE
		 */
		private class LayoutMLRuleActionTransferLookedupValueProcessor implements ElementProcessor {

			private LayoutMLRuleAction singleAction = null;

			@Override
			public void closeElement() throws SAXException {
				actualRule.getLayoutMLRuleActions().addAction(singleAction);
			}

			@Override
			public void startElement(Attributes atts) throws SAXException {
				singleAction = new LayoutMLRuleAction();

				String targetComponent = atts.getValue(ATTRIBUTE_TARGETCOMPONENT);
				// FIX NUCLEUSINT-305
				String sourceField = atts.getValue(ATTRIBUTE_SOURCEFIELD);

				singleAction.setRuleAction(ELEMENT_TRANSFERLOOKEDUPVALUE);
				singleAction.setSourceField(sourceField);
				singleAction.setTargetComponent(targetComponent);
			}
		}

		/**
		 * {@link ElementProcessor} for LayoutMLRuleAction
		 * @see LayoutMLRuleAction#CLEAR
		 */
		private class LayoutMLRuleActionClearProcessor implements ElementProcessor {
			private LayoutMLRuleAction singleAction = null;

			@Override
			public void closeElement() throws SAXException {
				actualRule.getLayoutMLRuleActions().addAction(singleAction);
			}

			@Override
			public void startElement(Attributes atts) throws SAXException {
				singleAction = new LayoutMLRuleAction();

				String targetComponent = atts.getValue(ATTRIBUTE_TARGETCOMPONENT);
				String entity = atts.getValue(ATTRIBUTE_ENTITY);

				singleAction.setRuleAction(ELEMENT_CLEAR);
				singleAction.setEntity(entity);
				singleAction.setTargetComponent(targetComponent);
			}

		}

		/**
		 * {@link ElementProcessor} for LayoutMLRuleAction
		 * @see LayoutMLRuleAction#ENABLE
		 *
		 * Not implemented by LayoutMLParser!
		 */
		private class LayoutMLRuleActionEnableProcessor implements ElementProcessor {

			private LayoutMLRuleAction singleAction = null;

			@Override
			public void closeElement() throws SAXException {
				actualRule.getLayoutMLRuleActions().addAction(singleAction);
			}

			@Override
			public void startElement(Attributes atts) throws SAXException {
				singleAction = new LayoutMLRuleAction();

				String targetComponent = atts.getValue(ATTRIBUTE_TARGETCOMPONENT);
				String invertable = atts.getValue(ATTRIBUTE_INVERTABLE);
				boolean blnInvertable = invertable.equals("yes") ? true : false;

				singleAction.setRuleAction(ELEMENT_ENABLE);
				singleAction.setInvertable(blnInvertable);
				singleAction.setTargetComponent(targetComponent);
			}

		}

		/**
		 * {@link ElementProcessor} for LayoutMLRuleAction
		 * @see LayoutMLRuleAction#REFRESH_VALUELIST
		 */
		private class LayoutMLRuleActionRefreshValueListProcessor implements ElementProcessor {

			private LayoutMLRuleAction singleAction = null;

			@Override
			public void closeElement() throws SAXException {
				actualRule.getLayoutMLRuleActions().addAction(singleAction);
			}

			@Override
			public void startElement(Attributes atts) throws SAXException {
				singleAction = new LayoutMLRuleAction();

				String targetComponent = atts.getValue(ATTRIBUTE_TARGETCOMPONENT);
				String entity = atts.getValue(ATTRIBUTE_ENTITY);
				String parameter = atts.getValue(ATTRIBUTE_PARAMETER_FOR_SOURCECOMPONENT);

				singleAction.setRuleAction(ELEMENT_REFRESHVALUELIST);
				singleAction.setEntity(entity);
				singleAction.setTargetComponent(targetComponent);
				singleAction.setParameterForSourceComponent(parameter);
			}

		}

		private WYSIWYGValuelistProvider currentValuelistProvider = null;

		/**
		 * {@link ElementProcessor} for PropertyValueValuelistProvider
		 * @see PropertyValueValuelistProvider
		 */
		private class LayoutMLValueListProviderProcessor implements ElementProcessor {

			@Override
			public void closeElement() throws SAXException {
				currentValuelistProvider = null;
			}

			@Override
			public void startElement(Attributes atts) throws SAXException {
				if (!subformColumnMissing) {
					PropertyValueValuelistProvider value = new PropertyValueValuelistProvider();
					value.setValue(ELEMENT_VALUELISTPROVIDER, atts);
					try {
						((WYSIWYGComponent) stack.peek()).setProperty(WYSIWYGComponent.PROPERTY_VALUELISTPROVIDER, value, null);
					} catch (CommonBusinessException ex) {
						log.error(ex);
						Errors.getInstance().showExceptionDialog(null, ex);
					}
					currentValuelistProvider = value.getValue();
				}
			}
		}

		/**
		 * {@link ElementProcessor} for WYSIYWYGParameter
		 * @see WYSIYWYGParameter
		 */
		private class LayoutMLParameterProcessor implements ElementProcessor {

			@Override
			public void closeElement() throws SAXException {

			}

			@Override
			public void startElement(Attributes atts) throws SAXException {
				if (currentValuelistProvider != null) {
					// currentValuelistProvider can be null, for instance if subform column has been deleted
					String attributeName = atts.getValue(ATTRIBUTE_NAME);
					String attributeValue = atts.getValue(ATTRIBUTE_VALUE);
					WYSIYWYGParameter parameter = new WYSIYWYGParameter(attributeName, attributeValue);
					currentValuelistProvider.addWYSIYWYGParameter(parameter);
				}
			}
		}

		/**
		 * {@link ElementProcessor} for WYSIWYGInitialFocusComponent
		 * @see WYSIWYGInitialFocusComponent
		 */
		private class InitialFocusComponentElementProcessor implements ElementProcessor {

			@Override
			public void closeElement() throws SAXException {

			}

			@Override
			public void startElement(Attributes atts) throws SAXException {
				String attributeName = atts.getValue(ATTRIBUTE_NAME);
				String attributeEntity = atts.getValue(ATTRIBUTE_ENTITY);
				editorPanel.setInitialFocusComponent(new WYSIWYGInitialFocusComponent(attributeEntity, attributeName));
			}
		}

		/**
		 * {@link ElementProcessor} for WYSIWYGInitialSortingOrder
		 * @see WYSIWYGInitialSortingOrder
		 */
		private class InitialSortingOrderElementProcessor implements ElementProcessor {

			@Override
			public void closeElement() throws SAXException {

			}

			@Override
			public void startElement(Attributes atts) throws SAXException {
				if (stack.peek() instanceof WYSIWYGSubForm) {
					WYSIWYGSubForm subform = (WYSIWYGSubForm) stack.peek();

					PropertyValueInitialSortingOrder value = new PropertyValueInitialSortingOrder();
					value.setValue(ELEMENT_INITIALSORTINGORDER, atts);
					////NUCLEUSINT-563
					List<String> subformColumns = editorPanel.getMetaInformation().getSubFormColumns(subform.getEntityName());
					if (!subformColumns.contains(value.getValue().getName()))
						return;
					try {
						subform.setProperty(WYSIWYGSubForm.PROPERTY_INITIALSORTINGORDER, value, WYSIWYGInitialSortingOrder.class);
					} catch (CommonBusinessException ex) {
						log.error(ex);
						Errors.getInstance().showExceptionDialog(null, ex);
					}
				}
			}
		}

		/**
		 * {@link ElementProcessor} for PropertyValueFont
		 * @see PropertyValueFont
		 */
		private class FontElementProcessor implements ElementProcessor {

			@Override
			public void closeElement() throws SAXException {

			}

			@Override
			public void startElement(Attributes atts) throws SAXException {
				WYSIWYGComponent c = peekComponent();

				String attributeSize = atts.getValue(ATTRIBUTE_SIZE);

				PropertyValueFont value = (PropertyValueFont) PropertyUtils.getPropertyValue(c, WYSIWYGComponent.PROPERTY_FONT);
				if (!StringUtils.isNullOrEmpty(attributeSize)) {
					value.setValue(Integer.parseInt(attributeSize));
				} else {
					value.setValue(0);
				}

				try {
					c.setProperty(WYSIWYGComponent.PROPERTY_FONT, value, PropertyUtils.getValueClass(c, WYSIWYGComponent.PROPERTY_FONT));
				} catch (CommonBusinessException ex) {
					log.error(ex);
					Errors.getInstance().showExceptionDialog(null, ex);
				}
			}
		}
		private TranslationMap translations;

		/**
		 * {@link ElementProcessor} for Locale Resources.
		 */
		private class TranslationsProcessor implements ElementProcessor {

			@Override
			public void startElement(Attributes atts) throws SAXException {
				if (translations == null) {
					WYSIWYGComponent c = peekComponent();
					try {
						translations = new TranslationMap();
						PropertyValueTranslations p = new PropertyValueTranslations(translations);
						c.setProperty(WYSIWYGCollectableComponent.PROPERTY_TRANSLATIONS, p , TranslationMap.class);
					} catch (CommonBusinessException e) {
						log.error(e);
						Errors.getInstance().showExceptionDialog(null, e);
					}
				}
			}

			@Override
			public void closeElement() throws SAXException {
				translations = null;
			}
		}

		/**
		 * {@link ElementProcessor} for Locale Resources.
		 */
		private class TranslationProcessor implements ElementProcessor {

			@Override
			public void startElement(Attributes atts) throws SAXException {
				if (translations != null) {
					String attributeLang = atts.getValue(ATTRIBUTE_LANG);
					String attributeText = atts.getValue(ATTRIBUTE_TEXT);
					translations.put(attributeLang, attributeText);
				}
			}

			@Override
			public void closeElement() throws SAXException {
			}
		}
	}

 	/**
	 * Method for getting the Propertys from the Attributes and Setting them.
	 * @param c
	 * @param atts
	 */
	public void setPropertiesFromAttributes(WYSIWYGComponent c, Attributes atts) {
		for (int i = 0; i < c.getPropertyAttributeLink().length; i++) {
			try {
				String propertyName = c.getPropertyAttributeLink()[i][0];

				PropertyValue<?> value = PropertyUtils.getPropertyValue(c, propertyName);
				value.setValue(c.getPropertyAttributeLink()[i][1], atts);

				c.setProperty(propertyName, value, PropertyUtils.getValueClass(c, propertyName));
			} catch (CommonBusinessException ex) {
				log.error(ex);
				Errors.getInstance().showExceptionDialog(null, ex);
			}
		}
	}

	/**
	 * Migrates existing resource ids into the translation map.
	 */
	public void migrateLocaleResourceId(WYSIWYGComponent c, Attributes atts) {
		String resId = atts.getValue(ATTRIBUTE_LOCALERESOURCEID);
		if (resId != null) {
			PropertyValueTranslations p = (PropertyValueTranslations) c.getProperties().getProperty(PROPERTY_LABELS.TRANSLATIONS);
			if (p != null) {
				Map<String, String> map = LocaleDelegate.getInstance().getAllResourcesByStringId(resId);
				map.remove(LocaleInfo.I_DEFAULT_TAG);
				p.getValue().putAll(map);
			}
		}
	}

	public void migrateLocaleResourceId(TranslationMap translations, Attributes atts) {
		String resId = atts.getValue(ATTRIBUTE_LOCALERESOURCEID);
		if (resId != null) {
			Map<String, String> map = LocaleDelegate.getInstance().getAllResourcesByStringId(resId);
			map.remove(LocaleInfo.I_DEFAULT_TAG);
			translations.merge(map);
		}
	}

	/** layoutmldependencys exist in the layoutml but the parser does do nothing with it */
	// private class LayoutMLDependencyProcessor implements ElementProcessor
	// {
	//
	// public void closeElement() throws SAXException {
	// }
	//
	// public void startElement(Attributes atts) throws SAXException {
	// String dependentField = atts.getValue(ATTRIBUTE_DEPENDANTFIELD);
	// String dependsOnField = atts.getValue(ATTRIBUTE_DEPENDSONFIELD);
	//
	// LayoutMLDependency dependency = new
	// LayoutMLDependency(dependentField, dependsOnField);
	// dependencies.addDependency(dependency);
	// }
	//
	// }
	//
	// private class LayoutMLDependenciesProcessor implements
	// ElementProcessor {
	//
	// public void closeElement() throws SAXException {
	// if (dependencies.getAllDependencies().size() > 0)
	// editorPanel.handoverDependencies(dependencies);
	// }
	//
	// public void startElement(Attributes atts) throws SAXException {
	// dependencies = editorPanel.getLayoutMLDependencies();
	//
	// }
	//
	// }
}
