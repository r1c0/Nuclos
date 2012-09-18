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
import java.awt.LayoutManager;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JSplitPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.nuclos.api.Property;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.genericobject.GeneratorActions;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.JTABBEDPANE;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.STATIC_BUTTON;
import org.nuclos.client.layout.wysiwyg.component.TitledBorderWithTranslations;
import org.nuclos.client.layout.wysiwyg.component.TranslationMap;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGChart;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableCheckBox;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableComboBox;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableDateChooser;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableEmail;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableHyperlink;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableImage;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableLabel;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableListOfValues;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableOptionGroup;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectablePasswordfield;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableTextArea;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableTextfield;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGLayoutComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGScrollPane;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGSplitPane;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGStaticButton;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGStaticComboBox;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGStaticImage;
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
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueBoolean;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueBorder;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueColor;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueDimension;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueDouble;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueFont;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueInitialSortingOrder;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueInteger;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueScript;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueString;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueTranslations;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueValuelistProvider;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.TableLayoutPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGInitialFocusComponent;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGInitialSortingOrder;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGOption;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGOptions;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGValuelistProvider;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIYWYGParameter;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIYWYGProperty;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIYWYGPropertySet;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRule;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRuleAction;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRuleActions;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRuleCondition;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRuleEventType;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRules;
import org.nuclos.client.rule.RuleCache;
import org.nuclos.client.statemodel.StateDelegate;
import org.nuclos.common.NuclosScript;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.common2.layoutml.LayoutMLConstants;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
import org.nuclos.server.statemodel.valueobject.StateVO;

/**
 * This Class generates the LayoutML.
 *
 * Every {@link WYSIWYGLayoutEditorPanel} returns its {@link TableLayout} and
 * {@link WYSIWYGComponent} and the {@link WYSIWYGComponent} returns its
 * {@link ComponentProperties}.
 *
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class LayoutMLGenerator implements LayoutMLConstants {

	private static final Logger LOG = Logger.getLogger(LayoutMLGenerator.class);

	private LayoutMLRules layoutMLRules = new LayoutMLRules();

	/**
	 * Everything that is called here is in the sequence like it is in the Layoutml
	 * Header
	 * Components
	 * Rules
	 *
	 * @param editorPanel
	 * @return
	 * @throws CommonValidationException
	 */
	public synchronized String getLayoutML(WYSIWYGLayoutEditorPanel editorPanel) throws CommonValidationException {
		int blockDeep = 0;

		LayoutMLBlock block = new LayoutMLBlock(blockDeep, false);
		block.append("<?xml version=\"1.0\" encoding=\"ISO-8859-15\"?>");
		block.linebreak();
		block.append("<!DOCTYPE layoutml SYSTEM \"http://www.novabit.de/technologies/layoutml/layoutml.dtd\">");
		block.linebreak();
		block.append("<layoutml>");
		block.linebreak();
		block.append("<layout>");
		block.append(getLayoutMLForInitialFocusComponent(editorPanel, blockDeep + 1));
		// no constraint needed for the first panel
		layoutMLRules = new LayoutMLRules();
		/** at first the panel and its components */
		block.append(getLayoutMLForPanel(editorPanel, blockDeep + 1, new StringBuffer()));

		block.linebreak();
		block.append("</layout>");

		// if (editorPanel.getLayoutMLDependencies().getAllDependencies().size()
		// > 0) {
		// block.append(getLayoutMLLayoutMLDependencies(editorPanel.
		// getLayoutMLDependencies(), blockDeep + 1));
		// if (layoutMLRules.getRules().size() < 1)
		// block.linebreak();
		// }

		/** now the rules */
		if (layoutMLRules.getRules().size() > 0) {
			block.append(getLayoutMLLayoutMLRules(layoutMLRules, blockDeep + 1));
		}

		block.linebreak();

		block.append("</layoutml>");

		return block.toString();
	}

	/**
	 * Method returning the Layoutml for the Property {@link WYSIWYGInitialFocusComponent}
	 * @param c
	 * @param blockDeep
	 * @return
	 */
	private synchronized StringBuffer getLayoutMLForInitialFocusComponent(WYSIWYGLayoutEditorPanel c, int blockDeep) {
		if (c.getInitialFocusComponent() != null) {
			LayoutMLBlock block = new LayoutMLBlock(blockDeep);
			block.append("<" + ELEMENT_INITIALFOCUSCOMPONENT);
			if (!StringUtils.isNullOrEmpty(c.getInitialFocusComponent().getEntity())) {
				block.append(" " + ATTRIBUTE_ENTITY + "=\"" + StringUtils.xmlEncode(c.getInitialFocusComponent().getEntity()) + "\"");
			}
			block.append(" " + ATTRIBUTE_NAME + "=\"" + StringUtils.xmlEncode(c.getInitialFocusComponent().getName()) + "\" />");
			return block.getStringBuffer();
		} else {
			return new StringBuffer();
		}
	}

	/**
	 * Method returning the Layoutml for {@link WYSIWYGComponent}
	 * Does decide what kind of processor is needed (chooses with "instanceof")
	 *
	 * @param c
	 * @param tableLayout
	 * @param blockDeep
	 * @return
	 */
	private synchronized StringBuffer getLayoutMLForComponent(WYSIWYGComponent c, TableLayout tableLayout, int blockDeep) throws CommonValidationException {
		StringBuffer sb = new StringBuffer();

		if (c instanceof WYSIWYGCollectableComponent) {
			sb.append(getLayoutMLForCollectableComponent((WYSIWYGCollectableComponent) c, tableLayout, blockDeep));
		} else if (c instanceof WYSIWYGLayoutEditorPanel) {
			sb.append(getLayoutMLForPanel((WYSIWYGLayoutEditorPanel) c, blockDeep, getLayoutMLTableLayoutConstraints((Component) c, tableLayout, blockDeep + 1)));
		} else if (c instanceof WYSIWYGTabbedPane) {
			sb.append(getLayoutMLForTabbedPane((WYSIWYGTabbedPane) c, tableLayout, blockDeep));
		} else if (c instanceof WYSIWYGSubForm) {
			sb.append(getLayoutMLForSubForm((WYSIWYGSubForm) c, tableLayout, blockDeep));
		} else if (c instanceof WYSIWYGChart) {
			sb.append(getLayoutMLForChart((WYSIWYGChart) c, tableLayout, blockDeep));
		} else if (c instanceof WYSIWYGScrollPane) {
			sb.append(getLayoutMLForScrollPane((WYSIWYGScrollPane) c, tableLayout, blockDeep));
		} else if (c instanceof WYSIWYGSplitPane) {
			sb.append(getLayoutMLForSplitPane((WYSIWYGSplitPane) c, tableLayout, blockDeep));
		} else if (c instanceof WYSIWYGStaticComboBox) {
			sb.append(getLayoutMLForStaticComboBox((WYSIWYGStaticComboBox) c, tableLayout, blockDeep));
		} else if (c instanceof WYSIWYGStaticLabel) {
			sb.append(getLayoutMLForStaticLabel((WYSIWYGStaticLabel) c, tableLayout, blockDeep));
		} else if (c instanceof WYSIWYGStaticSeparator) {
			sb.append(getLayoutMLForStaticSeparator((WYSIWYGStaticSeparator) c, tableLayout, blockDeep));
		} else if (c instanceof WYSIWYGStaticTextarea) {
			sb.append(getLayoutMLForStaticTextarea((WYSIWYGStaticTextarea) c, tableLayout, blockDeep));
		} else if (c instanceof WYSIWYGStaticTextfield) {
			sb.append(getLayoutMLForStaticTextfield((WYSIWYGStaticTextfield) c, tableLayout, blockDeep));
		} else if (c instanceof WYSIWYGStaticTitledSeparator) {
			sb.append(getLayoutMLForStaticTitledSeparator((WYSIWYGStaticTitledSeparator) c, tableLayout, blockDeep));
		} else if (c instanceof WYSIWYGStaticButton) {
			sb.append(getLayoutMLForStaticButton((WYSIWYGStaticButton) c, tableLayout, blockDeep));
		} else if (c instanceof WYSIWYGStaticImage) {
			sb.append(getLayoutMLForStaticImage((WYSIWYGStaticImage) c, tableLayout, blockDeep));
		} else if (c instanceof WYSIWYGLayoutComponent) {
			sb.append(getLayoutMLForLayoutComponent((WYSIWYGLayoutComponent) c, tableLayout, blockDeep));
		}

		return sb;
	}

	/**
	 * Method converting the {@link WYSIWYGSubForm} to LayoutML XML.
	 *
	 * @see LayoutMLBlock
	 * @param subform
	 * @param tableLayout
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLForSubForm(WYSIWYGSubForm subform, TableLayout tableLayout, int blockDeep) {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);
		block.append("<" + ELEMENT_SUBFORM);
		block.append(getLayoutMLAttributesFromProperties(WYSIWYGSubForm.PROPERTIES_TO_LAYOUTML_ATTRIBUTES, subform.getProperties()));
		block.append(" >");
		block.append(getLayoutMLTableLayoutConstraints(subform, tableLayout, blockDeep + 1));
		block.append(getLayoutMLBordersFromProperty(subform.getProperties(), blockDeep + 1));
		block.append(getLayoutMLMinimumSizeFromComponent(subform, blockDeep + 1));
		block.append(getLayoutMLPreferredSizeFromProperty(subform.getProperties(), blockDeep + 1));
		block.append(getLayoutMLFontFromProperty(subform.getProperties(), blockDeep + 1));
		//NUCLEUSINT-563
		List<String> subformColumns = subform.getParentEditor().getMetaInformation().getSubFormColumns(subform.getEntityName());
		if (subform.getProperties().getProperty(WYSIWYGSubForm.PROPERTY_INITIALSORTINGORDER).getValue() != null)
			if (subformColumns.contains(((PropertyValueInitialSortingOrder) subform.getProperties().getProperty(WYSIWYGSubForm.PROPERTY_INITIALSORTINGORDER)).getValue().getName()))
		block.append(getLayoutMLInitialSortingOrder(((PropertyValueInitialSortingOrder) subform.getProperties().getProperty(WYSIWYGSubForm.PROPERTY_INITIALSORTINGORDER)).getValue(), blockDeep + 1));
		block.append(getLayoutMLBackgroundColorFromProperty(subform.getProperties(), blockDeep + 1));

		for (Iterator<WYSIWYGSubFormColumn> it = subform.getColumnsInOrder().iterator(); it.hasNext();) {
			WYSIWYGSubFormColumn column = it.next();
			if (!(Boolean) column.getProperties().getProperty(WYSIWYGSubFormColumn.PROPERTY_DEFAULTVALUES).getValue()) {
				block.append(getLayoutMLForSubFormColumn(column, blockDeep + 1));
			}
		}

		block.append(getLayoutMLScriptFromProperty(WYSIWYGSubForm.PROPERTY_NEW_ENABLED, ELEMENT_NEW_ENABLED, subform.getProperties(), blockDeep + 1));
		block.append(getLayoutMLScriptFromProperty(WYSIWYGSubForm.PROPERTY_EDIT_ENABLED, ELEMENT_EDIT_ENABLED, subform.getProperties(), blockDeep + 1));
		block.append(getLayoutMLScriptFromProperty(WYSIWYGSubForm.PROPERTY_DELETE_ENABLED, ELEMENT_DELETE_ENABLED, subform.getProperties(), blockDeep + 1));
		block.append(getLayoutMLScriptFromProperty(WYSIWYGSubForm.PROPERTY_CLONE_ENABLED, ELEMENT_CLONE_ENABLED, subform.getProperties(), blockDeep + 1));
		block.linebreak();
		block.append("</" + ELEMENT_SUBFORM + ">");

		return block.getStringBuffer();
	}

	/**
	 * Method converting the {@link WYSIWYGSubFormColumn} to LayoutML XML.
	 *
	 *
	 * @see LayoutMLBlock
	 * @param column
	 * @param tableLayout
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLForSubFormColumn(WYSIWYGSubFormColumn column, int blockDeep) {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);
		block.append("<" + ELEMENT_SUBFORMCOLUMN);
		block.append(getLayoutMLAttributesFromProperties(WYSIWYGSubFormColumn.PROPERTIES_TO_LAYOUTML_ATTRIBUTES, column.getProperties()));
		block.append(">");

		block.append(getLayoutMLTranslationsFromProperty(column.getProperties(), blockDeep + 1));

		boolean lb = false;
		WYSIWYGValuelistProvider wysiwygStaticValuelistProvider = (WYSIWYGValuelistProvider) column.getProperties().getProperty(WYSIWYGCollectableComponent.PROPERTY_VALUELISTPROVIDER).getValue();
		if (wysiwygStaticValuelistProvider != null && (!wysiwygStaticValuelistProvider.getType().equals(""))) {
			block.append(getLayoutMLValueListProvider(wysiwygStaticValuelistProvider, blockDeep + 1));
			lb = true;
		}
		WYSIYWYGProperty collectableComponentProperties = (WYSIYWYGProperty) column.getProperties().getProperty(WYSIWYGCollectableComponent.PROPERTY_COLLECTABLECOMPONENTPROPERTY).getValue();
		if (collectableComponentProperties != null)
			if (collectableComponentProperties.getAllPropertyEntries().size() > 0) {
				block.append(getLayoutMLCollectableComponentProperty(collectableComponentProperties, blockDeep + 1));
				lb = true;
			}

		if (lb) block.linebreak();
		block.append("</" + ELEMENT_SUBFORMCOLUMN + ">");
		return block.getStringBuffer();
	}

	/**
	 * Method converting the {@link WYSIWYGChart} to LayoutML XML.
	 * 
	 * @see LayoutMLBlock
	 * @param chart
	 * @param tableLayout
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLForChart(WYSIWYGChart chart, TableLayout tableLayout, int blockDeep) {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);
		block.append("<" + ELEMENT_CHART);
		block.append(getLayoutMLAttributesFromProperties(WYSIWYGChart.PROPERTIES_TO_LAYOUTML_ATTRIBUTES, chart.getProperties()));
		block.append(" >");
		block.append(getLayoutMLTableLayoutConstraints(chart, tableLayout, blockDeep + 1));
		block.append(getLayoutMLBordersFromProperty(chart.getProperties(), blockDeep + 1));
		block.append(getLayoutMLMinimumSizeFromComponent(chart, blockDeep + 1));
		block.append(getLayoutMLPreferredSizeFromProperty(chart.getProperties(), blockDeep + 1));
		if (chart.getProperties().getProperty(WYSIWYGChart.PROPERTY_PROPERTIES) != null) {
			WYSIYWYGProperty collectableComponentProperties = (WYSIYWYGProperty) chart.getProperties().getProperty(WYSIWYGChart.PROPERTY_PROPERTIES).getValue();
			if (collectableComponentProperties != null) {
				if (collectableComponentProperties.getAllPropertyEntries().size() > 0) {
					block.append(getLayoutMLCollectableComponentProperty(collectableComponentProperties, blockDeep + 1));
				}
			}
		}
		block.linebreak();
		block.append("</" + ELEMENT_CHART + ">");

		return block.getStringBuffer();
	}

	/**
	 * Method converting the {@link WYSIWYGSplitPane} to LayoutML XML.
	 *
	 *
	 * @see LayoutMLBlock
	 * @param splitPane
	 * @param tableLayout
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLForSplitPane(WYSIWYGSplitPane splitPane, TableLayout tableLayout, int blockDeep) throws CommonValidationException {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);
		block.append("<" + ELEMENT_SPLITPANE);
		block.append(getLayoutMLAttributesFromProperties(WYSIWYGSplitPane.PROPERTIES_TO_LAYOUTML_ATTRIBUTES, splitPane.getProperties()));
		block.append(" >");
		block.append(getLayoutMLTableLayoutConstraints(splitPane, tableLayout, blockDeep + 1));
		block.append(getLayoutMLBordersFromProperty(splitPane.getProperties(), blockDeep + 1));
		block.append(getLayoutMLMinimumSizeFromComponent(splitPane, blockDeep + 1));
		block.append(getLayoutMLPreferredSizeFromProperty(splitPane.getProperties(), blockDeep + 1));

		Component[] allComponents = splitPane.getComponents();
		for (int i = 0; i < allComponents.length; i++) {
			Component c = allComponents[i];
			if (c instanceof JSplitPane) {
				JSplitPane jpnSplit = ((JSplitPane) c);

				Object propOrientation = splitPane.getProperties().getProperty(WYSIWYGSplitPane.PROPERTY_ORIENTATION).getValue();

				if (propOrientation != null) {
					if (ATTRIBUTEVALUE_HORIZONTAL.equals(propOrientation)) {
						block.append(getLayoutMLForPanel((WYSIWYGLayoutEditorPanel) jpnSplit.getLeftComponent(), blockDeep + 1, getLayoutMLSplitPaneConstraints(ATTRIBUTEVALUE_LEFT, blockDeep + 2)));
						block.append(getLayoutMLForPanel((WYSIWYGLayoutEditorPanel) jpnSplit.getRightComponent(), blockDeep + 1, getLayoutMLSplitPaneConstraints(ATTRIBUTEVALUE_RIGHT, blockDeep + 2)));
					} else {
						block.append(getLayoutMLForPanel((WYSIWYGLayoutEditorPanel) jpnSplit.getTopComponent(), blockDeep + 1, getLayoutMLSplitPaneConstraints(ATTRIBUTEVALUE_TOP, blockDeep + 2)));
						block.append(getLayoutMLForPanel((WYSIWYGLayoutEditorPanel) jpnSplit.getBottomComponent(), blockDeep + 1, getLayoutMLSplitPaneConstraints(ATTRIBUTEVALUE_BOTTOM, blockDeep + 2)));
					}
				}
			}
		}

		block.linebreak();
		block.append("</" + ELEMENT_SPLITPANE + ">");
		return block.getStringBuffer();
	}

	/**
	 * Method converting the {@link WYSIWYGSplitPane} Constraints to LayoutML XML.
	 *
	 *
	 * @see LayoutMLBlock
	 * @param position
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLSplitPaneConstraints(String position, int blockDeep) {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);

		block.append("<" + ELEMENT_SPLITPANECONSTRAINTS + " ");
		block.append(ATTRIBUTE_POSITION + "=\"");
		block.append(position);
		block.append("\" />");

		return block.getStringBuffer();
	}

	/**
	 * Method converting the {@link WYSIWYGScrollPane} to LayoutML XML.
	 *
	 *
	 * @see LayoutMLBlock
	 * @param scrollPane
	 * @param tableLayout
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLForScrollPane(WYSIWYGScrollPane scrollPane, TableLayout tableLayout, int blockDeep) throws CommonValidationException {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);
		block.append("<" + ELEMENT_SCROLLPANE);
		block.append(getLayoutMLAttributesFromProperties(scrollPane.getPropertyAttributeLink(), scrollPane.getProperties()));
		block.append(" >");
		block.append(getLayoutMLTableLayoutConstraints(scrollPane, tableLayout, blockDeep + 1));
		block.append(getLayoutMLBordersFromProperty(scrollPane.getProperties(), blockDeep + 1));
		block.append(getLayoutMLMinimumSizeFromComponent(scrollPane, blockDeep + 1));
		block.append(getLayoutMLPreferredSizeFromProperty(scrollPane.getProperties(), blockDeep + 1));

		Component[] allComponents = scrollPane.getViewport().getComponents();
		for (int i = 0; i < allComponents.length; i++) {
			Component c = allComponents[i];
			if (c instanceof WYSIWYGLayoutEditorPanel) {
				block.append(getLayoutMLForPanel((WYSIWYGLayoutEditorPanel) c, blockDeep + 1, new StringBuffer()));
			}
		}

		block.linebreak();
		block.append("</" + ELEMENT_SCROLLPANE + ">");
		return block.getStringBuffer();
	}

	/**
	 * Method converting the {@link WYSIWYGTabbedPane} to LayoutML XML.
	 *
	 *
	 * @see LayoutMLBlock
	 * @param tabPane
	 * @param tableLayout
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLForTabbedPane(WYSIWYGTabbedPane tabPane, TableLayout tableLayout, int blockDeep) throws CommonValidationException {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);
		block.append("<" + ELEMENT_TABBEDPANE + " ");
		block.append(getLayoutMLAttributesFromProperties(tabPane.getPropertyAttributeLink(), tabPane.getProperties()));
		block.append(">");
		block.append(getLayoutMLTableLayoutConstraints(tabPane, tableLayout, blockDeep + 1));
		block.append(getLayoutMLBordersFromProperty(tabPane.getProperties(), blockDeep + 1));
		block.append(getLayoutMLMinimumSizeFromComponent(tabPane, blockDeep + 1));
		block.append(getLayoutMLPreferredSizeFromProperty(tabPane.getProperties(), blockDeep + 1));

		for (int i = 0; i < tabPane.getTabCount(); i++) {
			Component c = tabPane.getComponentAt(i);
			if (c instanceof WYSIWYGLayoutEditorPanel) {
				WYSIWYGLayoutEditorPanel panel = (WYSIWYGLayoutEditorPanel)c;
				String sTitle = null;
				if(panel.getName() != null) {
					sTitle = c.getName();
				}
				else {
					sTitle = tabPane.getTitleAt(i);
				}
				block.append(getLayoutMLForPanel((WYSIWYGLayoutEditorPanel) c, blockDeep + 1, getLayoutMLTabbedPaneConstraints(tabPane.getTitleAt(i), tabPane.isEnabledAt(i), blockDeep + 1, tabPane.getTabTranslations().get(i), sTitle, tabPane.getMnemonicAt(i))));
			}
		}

		block.linebreak();
		block.append("</" + ELEMENT_TABBEDPANE + ">");

		return block.getStringBuffer();
	}

	/**
	 * Method converting the {@link WYSIWYGLayoutEditorPanel} to LayoutML XML.
	 *
	 *
	 * @see LayoutMLBlock
	 * @param panel
	 * @param blockDeep
	 * @param sbLayoutConstraints
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLForPanel(WYSIWYGLayoutEditorPanel panel, int blockDeep, StringBuffer sbLayoutConstraints) throws CommonValidationException {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);
		block.append("<" + ELEMENT_PANEL);
		block.append(getLayoutMLAttributesFromProperties(WYSIWYGLayoutEditorPanel.PROPERTIES_TO_LAYOUTML_ATTRIBUTES, panel.getProperties()));
		block.append(">");
		block.append(sbLayoutConstraints);

		LayoutManager layoutManager = panel.getLayout();
		if (!(layoutManager instanceof TableLayout)) {
			throw new CommonValidationException("Only LayoutManagers of type TableLayout are allowed for a WYSIWYGLayout!");
		}
		TableLayout tableLayout = (TableLayout) layoutManager;
		block.append(getLayoutMLForTableLayoutColumnsAndRows(tableLayout, blockDeep + 1));
		block.append(getLayoutMLBordersFromProperty(panel.getProperties(), blockDeep + 1));
		// block.append(getLayoutMLMinimumSizeFromComponent(panel, blockDeep +
		// 1));
		//block.append(getLayoutMLPreferredSizeFromProperty(panel.getProperties(
		// ), blockDeep + 1));
		block.append(getLayoutMLBackgroundColorFromProperty(panel.getProperties(), blockDeep + 1));

		block.append(getLayoutMLForTableLayoutPanel(panel.getTableLayoutPanel(), blockDeep + 1, getLayoutMLTableLayoutConstraints(panel.getTableLayoutPanel(), tableLayout, blockDeep + 2)));

		block.linebreak();
		block.append("</" + ELEMENT_PANEL + ">");

		return block.getStringBuffer();
	}

	/**
	 * Method converting the {@link TableLayoutPanel} to LayoutML XML.
	 *
	 *
	 * @see LayoutMLBlock
	 * @param tableLayoutPanel
	 * @param blockDeep
	 * @param sbLayoutConstraints
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLForTableLayoutPanel(TableLayoutPanel tableLayoutPanel, int blockDeep, StringBuffer sbLayoutConstraints) throws CommonValidationException {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);
		block.append("<" + ELEMENT_PANEL + ">");
		block.append(sbLayoutConstraints);

		TableLayout tableLayout = (TableLayout) tableLayoutPanel.getLayout();
		block.append(getLayoutMLForTableLayoutColumnsAndRows(tableLayout, blockDeep + 1));

		Component[] allComponents = tableLayoutPanel.getComponents();
		for (int i = 0; i < allComponents.length; i++) {
			Component c = allComponents[i];
			if (c instanceof WYSIWYGComponent) {
				block.append(getLayoutMLForComponent((WYSIWYGComponent) c, tableLayout, blockDeep + 1));

				LayoutMLRules tempRules = null;
				if (c instanceof WYSIWYGSubForm) {
					Collection<WYSIWYGSubFormColumn> subformColumns = ((WYSIWYGSubForm) c).getColumnsInOrder();
					for (WYSIWYGSubFormColumn subformColumn : subformColumns) {
						tempRules = subformColumn.getLayoutMLRulesIfCapable();
						if (tempRules != null) {
							//NUCLEUSINT-435
							addRules(tempRules);
						}
					}
				} else {
					tempRules = ((WYSIWYGComponent) c).getLayoutMLRulesIfCapable();
					if (tempRules != null) {
						//NUCLEUSINT-435
						addRules(tempRules);
					}
				}
			}
		}

		block.linebreak();
		block.append("</" + ELEMENT_PANEL + ">");
		return block.getStringBuffer();
	}

	/**
	 * NUCLEUSINT-435
	 * Avoid duplicate Rules
	 * @param rulesToAdd
	 */
	private synchronized void addRules(LayoutMLRules rulesToAdd){
		boolean contains = false;
		for (LayoutMLRule singleRule : rulesToAdd.getRules()) {
			contains = false;
			if (this.layoutMLRules.getRules().size() == 0){
				this.layoutMLRules.addRule(singleRule);
				continue;
			}

			for (LayoutMLRule otherRule : this.layoutMLRules.getRules()){
				if (flatCompareRules(singleRule, otherRule)){
					contains = true;
					break;
				}
			}

			if (!contains)
				this.layoutMLRules.addRule(singleRule);

		}
	}

	/**
	 * NUCLEUSINT-435
	 * A Rule is the same if the Eventtype (and its fields), the Actions and the Rulename are equal
	 * @param oneRule
	 * @param otherRule
	 * @return true if equal, otherwise false
	 */
	private boolean flatCompareRules(LayoutMLRule oneRule, LayoutMLRule otherRule) {
		boolean areEqual = true;

		if (!oneRule.getRuleName().equals(otherRule.getRuleName()))
			areEqual = false;

		if (!oneRule.getLayoutMLRuleEventType().equals(otherRule.getLayoutMLRuleEventType()))
			areEqual = false;

		boolean actionsEqual = false;

		if (oneRule.getLayoutMLRuleActions().getSingleActions().size() != otherRule.getLayoutMLRuleActions().getSingleActions().size()) {
			areEqual = false;
		} else {
			for (LayoutMLRuleAction action : oneRule.getLayoutMLRuleActions().getSingleActions()) {
				for (LayoutMLRuleAction otherAction : otherRule.getLayoutMLRuleActions().getSingleActions()) {
					if (action.equals(otherAction))
						actionsEqual = true;
				}
				if (!actionsEqual)
					areEqual = false;
			}
		}

		return areEqual;
	}

	/**
	 * Method converting the {@link TableLayout} columns and rows to LayoutML XML.
	 *
	 *
	 * @see LayoutMLBlock
	 * @param tableLayout
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLForTableLayoutColumnsAndRows(TableLayout tableLayout, int blockDeep) {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);

		double[] columns = tableLayout.getColumn();
		double[] rows = tableLayout.getRow();

		block.append("<" + ELEMENT_TABLELAYOUT + " " + ATTRIBUTE_COLUMNS + "=\"");
		for (int i = 0; i < columns.length; i++) {
			block.append(columns[i]);
			if (i < columns.length - 1) {
				block.append("|");
			}
		}
		block.append("\" " + ATTRIBUTE_ROWS + "=\"");
		for (int i = 0; i < rows.length; i++) {
			block.append(rows[i]);
			if (i < rows.length - 1) {
				block.append("|");
			}
		}
		block.append("\" />");

		return block.getStringBuffer();
	}

	/**
	 * Method converting the {@link WYSIWYGUniversalComponent} to LayoutML XML.
	 *
	 *
	 * @see LayoutMLBlock
	 * @param c
	 * @param tableLayout
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLForCollectableComponent(WYSIWYGCollectableComponent c, TableLayout tableLayout, int blockDeep) {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);

		block.append("<" + ELEMENT_COLLECTABLECOMPONENT);
		block.append(getLayoutMLAttributesFromProperties(c.getPropertyAttributeLink(), c.getProperties()));
		//NUCLEUSINT-385
		/**
		 * The type WYSIWYGCollectableOptionGroup is a WYSIWYGUniversalComponent.
		 * There is no need to generate the
		 */
		if (c instanceof WYSIWYGCollectableTextfield) {
			block.append(" " + ATTRIBUTE_CONTROLTYPE + "=\"" + CONTROLTYPE_TEXTFIELD + "\" " + ATTRIBUTE_SHOWONLY + "=\"" + ATTRIBUTEVALUE_CONTROL + "\"");
		} else if (c instanceof WYSIWYGCollectableLabel) {
			block.append(" " + ATTRIBUTE_CONTROLTYPE + "=\"" + CONTROLTYPE_TEXTFIELD + "\" " + ATTRIBUTE_SHOWONLY + "=\"" + ATTRIBUTEVALUE_LABEL + "\"");
			/** use Textfield because DTD dont know a CollectableLabel */
		} else if (c instanceof WYSIWYGCollectableListOfValues) {
			block.append(" " + ATTRIBUTE_CONTROLTYPE + "=\"" + CONTROLTYPE_LISTOFVALUES + "\" " + ATTRIBUTE_SHOWONLY + "=\"" + ATTRIBUTEVALUE_CONTROL + "\"");
		} else if (c instanceof WYSIWYGCollectableCheckBox) {
			block.append(" " + ATTRIBUTE_CONTROLTYPE + "=\"" + CONTROLTYPE_CHECKBOX + "\" " + ATTRIBUTE_SHOWONLY + "=\"" + ATTRIBUTEVALUE_CONTROL + "\"");
		} else if (c instanceof WYSIWYGCollectableDateChooser) {
			block.append(" " + ATTRIBUTE_CONTROLTYPE + "=\"" + CONTROLTYPE_DATECHOOSER + "\" " + ATTRIBUTE_SHOWONLY + "=\"" + ATTRIBUTEVALUE_CONTROL + "\"");
		} else if (c instanceof WYSIWYGCollectableComboBox) {
			block.append(" " + ATTRIBUTE_CONTROLTYPE + "=\"" + CONTROLTYPE_COMBOBOX + "\" " + ATTRIBUTE_SHOWONLY + "=\"" + ATTRIBUTEVALUE_CONTROL + "\"");
		} else if (c instanceof WYSIWYGCollectableTextArea) {
			block.append(" " + ATTRIBUTE_CONTROLTYPE + "=\"" + CONTROLTYPE_TEXTAREA + "\" " + ATTRIBUTE_SHOWONLY + "=\"" + ATTRIBUTEVALUE_CONTROL + "\"");
		} else if (c instanceof WYSIWYGCollectableImage) {
			block.append(" " + ATTRIBUTE_CONTROLTYPE + "=\"" + CONTROLTYPE_IMAGE + "\" " + ATTRIBUTE_SHOWONLY + "=\"" + ATTRIBUTEVALUE_CONTROL + "\"");
		} else if (c instanceof WYSIWYGCollectablePasswordfield) {
			//NUCLEUSINT-1142
			block.append(" " + ATTRIBUTE_CONTROLTYPE + "=\"" + CONTROLTYPE_PASSWORDFIELD + "\" " + ATTRIBUTE_SHOWONLY + "=\"" + ATTRIBUTEVALUE_CONTROL + "\"");
		} else if (c instanceof WYSIWYGCollectableHyperlink) {
			block.append(" " + ATTRIBUTE_CONTROLTYPE + "=\"" + CONTROLTYPE_HYPERLINK + "\" " + ATTRIBUTE_SHOWONLY + "=\"" + ATTRIBUTEVALUE_CONTROL + "\"");
		} else if (c instanceof WYSIWYGCollectableEmail) {
			block.append(" " + ATTRIBUTE_CONTROLTYPE + "=\"" + CONTROLTYPE_EMAIL + "\" " + ATTRIBUTE_SHOWONLY + "=\"" + ATTRIBUTEVALUE_CONTROL + "\"");
		}

		block.append(">");

		block.append(getLayoutMLTableLayoutConstraints(c, tableLayout, blockDeep + 1));
		WYSIYWYGProperty collectableComponentProperties = (WYSIYWYGProperty) c.getProperties().getProperty(WYSIWYGCollectableComponent.PROPERTY_COLLECTABLECOMPONENTPROPERTY).getValue();
		if (collectableComponentProperties != null)
			if (collectableComponentProperties.getAllPropertyEntries().size() > 0)
				block.append(getLayoutMLCollectableComponentProperty(collectableComponentProperties, blockDeep + 1));
		block.append(getLayoutMLBordersFromProperty(c.getProperties(), blockDeep + 1));
		if (c.getProperties().getProperty(WYSIWYGCollectableComponent.PROPERTY_STRICTSIZE) != null) {
			block.append(getLayoutMLStrictSizeFromProperty(c.getProperties(), blockDeep + 1));
		} else {
			block.append(getLayoutMLMinimumSizeFromComponent(c, blockDeep + 1));
			block.append(getLayoutMLPreferredSizeFromProperty(c.getProperties(), blockDeep + 1));
		}
		block.append(getLayoutMLBackgroundColorFromProperty(c.getProperties(), blockDeep + 1));
		block.append(getLayoutMLDescription(c.getProperties(), blockDeep + 1));
		block.append(getLayoutMLFontFromProperty(c.getProperties(), blockDeep + 1));
		if (c.getProperties().getProperty(WYSIWYGCollectableOptionGroup.PROPERTY_OPTIONS) != null) {
			WYSIWYGOptions options = (WYSIWYGOptions) c.getProperties().getProperty(WYSIWYGCollectableOptionGroup.PROPERTY_OPTIONS).getValue();
			if (options != null) {
				block.append(getLayoutMLOptions(options, blockDeep + 1));
			}
		}
		block.append(getLayoutMLTranslationsFromProperty(c.getProperties(), blockDeep + 1));
		if (c.getProperties().getProperty(WYSIWYGCollectableComponent.PROPERTY_VALUELISTPROVIDER) != null) {
			WYSIWYGValuelistProvider wysiwygStaticValuelistProvider = (WYSIWYGValuelistProvider) c.getProperties().getProperty(WYSIWYGCollectableComponent.PROPERTY_VALUELISTPROVIDER).getValue();
			if (wysiwygStaticValuelistProvider != null && !StringUtils.isNullOrEmpty(wysiwygStaticValuelistProvider.getType())) {
				block.append(getLayoutMLValueListProvider(wysiwygStaticValuelistProvider, blockDeep + 1));
			}
		}
		block.append(getLayoutMLScriptFromProperty(WYSIWYGCollectableComponent.PROPERTY_ENABLED_DYNAMIC, ELEMENT_ENABLED, c.getProperties(), blockDeep + 1));
		block.linebreak();
		block.append("</" + ELEMENT_COLLECTABLECOMPONENT + ">");

		return block.getStringBuffer();
	}

	/**
	 * Method converting the {@link WYSIWYGStaticTitledSeparator} to LayoutML XML.
	 *
	 *
	 * @see LayoutMLBlock
	 * @param c
	 * @param tableLayout
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLForStaticTitledSeparator(WYSIWYGStaticTitledSeparator c, TableLayout tableLayout, int blockDeep) {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);

		block.append("<" + ELEMENT_TITLEDSEPARATOR);
		block.append(" " + ATTRIBUTE_TITLE + "=\"" +
			StringUtils.xmlEncode((String) c.getProperties().getProperty(
				WYSIWYGStaticTitledSeparator.PROPERTY_TITLE).getValue()) + "\"");
		block.append(">");

		block.append(getLayoutMLTableLayoutConstraints(c, tableLayout, blockDeep + 1));
		block.append(getLayoutMLMinimumSizeFromComponent(c, blockDeep + 1));
		block.append(getLayoutMLPreferredSizeFromProperty(c.getProperties(), blockDeep + 1));
		block.append(getLayoutMLFontFromProperty(c.getProperties(), blockDeep + 1));
		block.append(getLayoutMLTranslationsFromProperty(c.getProperties(), blockDeep + 1));
		block.linebreak();
		block.append("</" + ELEMENT_TITLEDSEPARATOR + ">");

		return block.getStringBuffer();
	}

	/**
	 * Method converting the {@link WYSIWYGStaticSeparator} to LayoutML XML.
	 *
	 *
	 * @see LayoutMLBlock
	 * @param c
	 * @param tableLayout
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLForStaticSeparator(WYSIWYGStaticSeparator c, TableLayout tableLayout, int blockDeep) {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);
		block.append("<" + ELEMENT_SEPARATOR);
		block.append(" " + ATTRIBUTE_ORIENTATION + "=\"");

		switch (c.getOrientation()) {
			case WYSIWYGStaticSeparator.HORIZONTAL :
				block.append(ATTRIBUTEVALUE_HORIZONTAL + "\"");
				break;
			case WYSIWYGStaticSeparator.VERTICAL :
				block.append(ATTRIBUTEVALUE_VERTICAL + "\"");
				break;
		}
		block.append(">");

		block.append(getLayoutMLTableLayoutConstraints(c, tableLayout, blockDeep + 1));
		block.append(getLayoutMLMinimumSizeFromComponent(c, blockDeep + 1));
		block.append(getLayoutMLPreferredSizeFromProperty(c.getProperties(), blockDeep + 1));
		block.linebreak();
		block.append("</" + ELEMENT_SEPARATOR + ">");
		return block.getStringBuffer();
	}

	/**
	 * Method converting the {@link WYSIWYGStaticButton} to LayoutML XML.
	 *
	 *
	 * @see LayoutMLBlock
	 * @param c
	 * @param tableLayout
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLForStaticButton(WYSIWYGStaticButton c, TableLayout tableLayout, int blockDeep) {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);
		block.append("<" + ELEMENT_BUTTON);
		block.append(getLayoutMLAttributesFromProperties(c.getPropertyAttributeLink(), c.getProperties()));
		block.append(">");

		block.append(getLayoutMLTableLayoutConstraints(c, tableLayout, blockDeep + 1));
		block.append(getLayoutMLBordersFromProperty(c.getProperties(), blockDeep + 1));
		block.append(getLayoutMLMinimumSizeFromComponent(c, blockDeep + 1));
		block.append(getLayoutMLPreferredSizeFromProperty(c.getProperties(), blockDeep + 1));
		block.append(getLayoutMLFontFromProperty(c.getProperties(), blockDeep + 1));
		block.append(getLayoutMLDescription(c.getProperties(), blockDeep + 1));
		
		//NUCLOSINT-743 if a rule is set generate a property for it
		String actionCommand = (String)c.getProperties().getProperty(WYSIWYGStaticButton.PROPERTY_ACTIONCOMMAND).getValue();
		if (STATIC_BUTTON.EXECUTE_RULE_ACTION_LABEL.equals(actionCommand)) {
			// is a execute rule action command
			try {
				String rule = (String)c.getProperties().getProperty(WYSIWYGStaticButton.PROPERTY_ACTIONCOMMAND_PROPERTIES).getValue();
				RuleVO ruleVO = RuleCache.getInstance().get(rule);
				WYSIYWYGProperty temp = new WYSIYWYGProperty();
				temp.addWYSIYWYGPropertySet(new WYSIYWYGPropertySet("ruletoexecute", ruleVO.getRule() + ""));
				block.append(getLayoutMLCollectableComponentProperty(temp, blockDeep + 1));
			} catch (Exception e) {
				LOG.warn("getLayoutMLForStaticButton failed: " + e, e);
			}
		}
		if (STATIC_BUTTON.GENERATOR_ACTION_LABEL.equals(actionCommand)) {
			// is a execute generator action command
			try {
				String sEntity = c.getParentEditor().getMetaInformation().getCollectableEntity().getName();
				final Integer iModuleId;
				if (Modules.getInstance().existModule(sEntity))
					iModuleId = Modules.getInstance().getModuleByEntityName(sEntity).getIntId();
				else
					iModuleId = IdUtils.unsafeToId(MetaDataClientProvider.getInstance().getEntity(sEntity).getId());

				String generator = (String)c.getProperties().getProperty(WYSIWYGStaticButton.PROPERTY_ACTIONCOMMAND_PROPERTIES).getValue();
				Collection<GeneratorActionVO> collGenerators = GeneratorActions.getGeneratorActions(iModuleId);

				GeneratorActionVO generatorActionVO = null;
				for (GeneratorActionVO gen: collGenerators) {
					if (gen.getName().equals(generator)) {
						generatorActionVO = gen;
						break;
					}
				}
				if (generatorActionVO != null) {
					WYSIYWYGProperty temp = new WYSIYWYGProperty();
					temp.addWYSIYWYGPropertySet(new WYSIYWYGPropertySet("generatortoexecute", generatorActionVO.getName() + ""));
					block.append(getLayoutMLCollectableComponentProperty(temp, blockDeep + 1));
				}
			} catch (Exception e) {
				LOG.warn("getLayoutMLForStaticButton failed: " + e, e);
			}
		}
		if (STATIC_BUTTON.STATE_CHANGE_ACTION_LABEL.equals(actionCommand)) {
			// is a execute generator action command
			try {
				final Collection<StateVO> collStates;
				String sEntity = c.getParentEditor().getMetaInformation().getCollectableEntity().getName();
				if (Modules.getInstance().existModule(sEntity)) {
					Integer iModuleId = Modules.getInstance().getModuleByEntityName(sEntity).getIntId();
					collStates = StateDelegate.getInstance().getStatesByModule(iModuleId);
				} else {
					collStates = Collections.emptyList();
				}

				String targetState = (String)c.getProperties().getProperty(WYSIWYGStaticButton.PROPERTY_ACTIONCOMMAND_PROPERTIES).getValue();

				StateVO stateVO = null;
				for (StateVO state: collStates) {
					if (state.getNumeral().toString().equals(targetState)) {
						stateVO = state;
						break;
					}
				}

				if (stateVO != null) {
					WYSIYWYGProperty temp = new WYSIYWYGProperty();
					temp.addWYSIYWYGPropertySet(new WYSIYWYGPropertySet("targetState", stateVO.getNumeral() + ""));
					block.append(getLayoutMLCollectableComponentProperty(temp, blockDeep + 1));
				}
			} catch (Exception e) {
				LOG.warn("getLayoutMLForStaticButton failed: " + e, e);
			}
		}
		
		//NUCLEUSINT-1159
		WYSIYWYGProperty collectableComponentProperties = (WYSIYWYGProperty) c.getProperties().getProperty(WYSIWYGCollectableComponent.PROPERTY_COLLECTABLECOMPONENTPROPERTY).getValue();
		if (collectableComponentProperties != null) {
			if (collectableComponentProperties.getAllPropertyEntries().size() > 0) {
				block.append(getLayoutMLCollectableComponentProperty(collectableComponentProperties, blockDeep + 1));
			}
		}
		
		block.append(getLayoutMLTranslationsFromProperty(c.getProperties(), blockDeep + 1));
		block.linebreak();
		block.append("</" + ELEMENT_BUTTON + ">");

		return block.getStringBuffer();
	}

	/**
	 * Method converting the {@link WYSIWYGStaticTextfield} to LayoutML XML.
	 *
	 *
	 * @see LayoutMLBlock
	 * @param c
	 * @param tableLayout
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLForStaticImage(WYSIWYGStaticImage c, TableLayout tableLayout, int blockDeep) {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);
		block.append("<" + ELEMENT_IMAGE);
		block.append(getLayoutMLAttributesFromProperties(c.getPropertyAttributeLink(), c.getProperties()));
		block.append(">");

		block.append(getLayoutMLTableLayoutConstraints(c, tableLayout, blockDeep + 1));
		block.append(getLayoutMLBordersFromProperty(c.getProperties(), blockDeep + 1));
		block.append(getLayoutMLMinimumSizeFromComponent(c, blockDeep + 1));
		block.append(getLayoutMLPreferredSizeFromProperty(c.getProperties(), blockDeep + 1));
		block.append(getLayoutMLFontFromProperty(c.getProperties(), blockDeep + 1));
		block.append(getLayoutMLDescription(c.getProperties(), blockDeep + 1));
		block.linebreak();
		block.append("</" + ELEMENT_IMAGE + ">");

		return block.getStringBuffer();
	}


	/**
	 * Method converting the {@link WYSIWYGStaticTextfield} to LayoutML XML.
	 *
	 *
	 * @see LayoutMLBlock
	 * @param c
	 * @param tableLayout
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLForStaticTextfield(WYSIWYGStaticTextfield c, TableLayout tableLayout, int blockDeep) {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);
		block.append("<" + ELEMENT_TEXTFIELD);
		block.append(getLayoutMLAttributesFromProperties(c.getPropertyAttributeLink(), c.getProperties()));
		block.append(">");

		block.append(getLayoutMLTableLayoutConstraints(c, tableLayout, blockDeep + 1));
		block.append(getLayoutMLBordersFromProperty(c.getProperties(), blockDeep + 1));
		block.append(getLayoutMLMinimumSizeFromComponent(c, blockDeep + 1));
		block.append(getLayoutMLPreferredSizeFromProperty(c.getProperties(), blockDeep + 1));
		block.append(getLayoutMLFontFromProperty(c.getProperties(), blockDeep + 1));
		block.append(getLayoutMLDescription(c.getProperties(), blockDeep + 1));
		block.linebreak();
		block.append("</" + ELEMENT_TEXTFIELD + ">");

		return block.getStringBuffer();
	}

	private synchronized StringBuffer getLayoutMLForLayoutComponent(WYSIWYGLayoutComponent c, TableLayout tableLayout, int blockDeep) {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);
		block.append("<" + ELEMENT_LAYOUTCOMPONENT);
		block.append(" ");
		block.append(ATTRIBUTE_CLASS);
		block.append("=\"");
		block.append(c.getLayoutComponentFactoryClass());
		block.append("\"");
		block.append(getLayoutMLAttributesFromProperties(c.getPropertyAttributeLink(), c.getProperties()));
		block.append(">");

		block.append(getLayoutMLTableLayoutConstraints(c, tableLayout, blockDeep + 1));
		if (c.getAdditionalProperties() != null && c.getAdditionalProperties().length > 0) {
			for (Property pt : c.getAdditionalProperties()) {
				PropertyValue<?> property = c.getProperties().getProperty(pt.name);
				if (property != null && property.getValue() != null) {
					block.append(getLayoutMLForProperty(pt.name, property, blockDeep + 1));
				}
			}
		}
		block.append(getLayoutMLBordersFromProperty(c.getProperties(), blockDeep + 1));
		block.append(getLayoutMLMinimumSizeFromComponent(c, blockDeep + 1));
		block.append(getLayoutMLPreferredSizeFromProperty(c.getProperties(), blockDeep + 1));

		block.linebreak();
		block.append("</" + ELEMENT_LAYOUTCOMPONENT + ">");

		return block.getStringBuffer();
	}

	/**
	 * Method converting the {@link WYSIWYGStaticTextarea} to LayoutML XML.
	 *
	 *
	 * @see LayoutMLBlock
	 * @param c
	 * @param tableLayout
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLForStaticTextarea(WYSIWYGStaticTextarea c, TableLayout tableLayout, int blockDeep) {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);
		block.append("<" + ELEMENT_TEXTAREA);
		block.append(getLayoutMLAttributesFromProperties(c.getPropertyAttributeLink(), c.getProperties()));
		block.append(">");

		block.append(getLayoutMLTableLayoutConstraints(c, tableLayout, blockDeep + 1));
		block.append(getLayoutMLBordersFromProperty(c.getProperties(), blockDeep + 1));
		block.append(getLayoutMLMinimumSizeFromComponent(c, blockDeep + 1));
		block.append(getLayoutMLPreferredSizeFromProperty(c.getProperties(), blockDeep + 1));
		block.append(getLayoutMLFontFromProperty(c.getProperties(), blockDeep + 1));
		block.append(getLayoutMLDescription(c.getProperties(), blockDeep + 1));
		block.linebreak();
		block.append("</" + ELEMENT_TEXTAREA + ">");

		return block.getStringBuffer();
	}

	/**
	 * Method converting the {@link WYSIWYGStaticLabel} to LayoutML XML.
	 *
	 *
	 * @see LayoutMLBlock
	 * @param c
	 * @param tableLayout
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLForStaticLabel(WYSIWYGStaticLabel c, TableLayout tableLayout, int blockDeep) {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);
		block.append("<" + ELEMENT_LABEL);
		block.append(getLayoutMLAttributesFromProperties(c.getPropertyAttributeLink(), c.getProperties()));
		block.append(">");

		block.append(getLayoutMLTableLayoutConstraints(c, tableLayout, blockDeep + 1));
		block.append(getLayoutMLBordersFromProperty(c.getProperties(), blockDeep + 1));
		block.append(getLayoutMLMinimumSizeFromComponent(c, blockDeep + 1));
		block.append(getLayoutMLPreferredSizeFromProperty(c.getProperties(), blockDeep + 1));
		block.append(getLayoutMLFontFromProperty(c.getProperties(), blockDeep + 1));
		block.append(getLayoutMLDescription(c.getProperties(), blockDeep + 1));
		block.append(getLayoutMLTranslationsFromProperty(c.getProperties(), blockDeep + 1));
		block.linebreak();
		block.append("</" + ELEMENT_LABEL + ">");
		return block.getStringBuffer();
	}

	/**
	 * Method converting the {@link WYSIWYGStaticComboBox} to LayoutML XML.
	 *
	 *
	 * @see LayoutMLBlock
	 * @param c
	 * @param tableLayout
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLForStaticComboBox(WYSIWYGStaticComboBox c, TableLayout tableLayout, int blockDeep) {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);
		block.append("<" + ELEMENT_COMBOBOX);
		block.append(getLayoutMLAttributesFromProperties(c.getPropertyAttributeLink(), c.getProperties()));
		block.append(">");

		block.append(getLayoutMLTableLayoutConstraints(c, tableLayout, blockDeep + 1));
		block.append(getLayoutMLBordersFromProperty(c.getProperties(), blockDeep + 1));
		block.append(getLayoutMLMinimumSizeFromComponent(c, blockDeep + 1));
		block.append(getLayoutMLPreferredSizeFromProperty(c.getProperties(), blockDeep + 1));
		block.append(getLayoutMLFontFromProperty(c.getProperties(), blockDeep + 1));
		block.append(getLayoutMLDescription(c.getProperties(), blockDeep + 1));
		block.linebreak();
		block.append("</" + ELEMENT_COMBOBOX + ">");
		return block.getStringBuffer();
	}

	/**
	 * Method converting the {@link ComponentProperties} to LayoutML XML.
	 *
	 *
	 * @see LayoutMLBlock
	 * @param propertyAttributeLink
	 * @param properties
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLAttributesFromProperties(String[][] propertyAttributeLink, ComponentProperties properties) {
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < propertyAttributeLink.length; i++) {
			sb.append(getLayoutMLAttributeFromProperty(properties.getProperty(propertyAttributeLink[i][0]), propertyAttributeLink[i][1]));
		}

		return sb;
	}

	/**
	 * Method converting the {@link PropertyValue} to LayoutML XML.
	 *
	 *
	 * @see LayoutMLBlock
	 * @param property
	 * @param attributeName
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLAttributeFromProperty(PropertyValue<?> property, String attributeName) {
		StringBuffer sb = new StringBuffer();

		if (property != null && property.getValue() != null) {
			//NUCLEUSINT-288 the display differs from the value to be set, if label and control should be shown, show only is empty
			if (ATTRIBUTE_SHOWONLY.equals(attributeName))
				if (WYSIWYGUniversalComponent.ATTRIBUTEVALUE_LABEL_AND_CONTROL.equals(property.getValue().toString()))
					return sb;


			sb.append(" ");
			sb.append(attributeName);
			sb.append("=\"");
			if (property instanceof PropertyValueString || property instanceof PropertyValueInteger) {
				//NUCLEUSINT-453
				String propertyValue = property.getValue().toString();
				if (JTABBEDPANE.SCROLL_TAB_LAYOUT.equals(propertyValue))
					sb.append(ATTRIBUTEVALUE_SCROLL);
				else if (JTABBEDPANE.WRAP_TAB_LAYOUT.equals(propertyValue))
					sb.append(ATTRIBUTEVALUE_WRAP);
				else if (STATIC_BUTTON.DUMMY_BUTTON_ACTION_LABEL.equals(propertyValue)) {
					//NUCLEUSINT-1159
					sb.append(STATIC_BUTTON.DUMMY_BUTTON_ACTION);
				} else if (STATIC_BUTTON.STATE_CHANGE_ACTION_LABEL.equals(propertyValue)) {
					//NUCLEUSINT-1159
					sb.append(STATIC_BUTTON.STATE_CHANGE_ACTION);
				} else if (STATIC_BUTTON.EXECUTE_RULE_ACTION_LABEL.equals(propertyValue)) {
					//NUCLOSINT-743
					sb.append(STATIC_BUTTON.EXECUTE_RULE_ACTION);
				} else if (STATIC_BUTTON.GENERATOR_ACTION_LABEL.equals(propertyValue)) {
					sb.append(STATIC_BUTTON.GENERATOR_ACTION);
				}	else
					sb.append(StringUtils.xmlEncode(propertyValue));
			} else if (property instanceof PropertyValueBoolean) {
				if (((Boolean) property.getValue())) {
					sb.append(ATTRIBUTEVALUE_YES);
				} else {
					sb.append(ATTRIBUTEVALUE_NO);
				}
			} else if (property instanceof PropertyValueInteger) {
				sb.append(property.getValue());
			} else if (property instanceof PropertyValueDouble) {
				sb.append(property.getValue());
			}
			sb.append("\"");
		}

		return sb;
	}

	/**
	 * Method converting the {@link WYSIWYGTabbedPane} Constraints to LayoutML XML.
	 *
	 *
	 * @see LayoutMLBlock
	 * @param title
	 * @param enabled
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLTabbedPaneConstraints(String title, boolean enabled, int blockDeep, TranslationMap translations, String internalname, Integer  mnemonic) {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);

		block.append("<" + ELEMENT_TABBEDPANECONSTRAINTS + " ");
		block.append(ATTRIBUTE_TITLE + "=\"");
		block.append(StringUtils.xmlEncode(title));
		block.append("\" " + ATTRIBUTE_ENABLED + "=\"");
		block.append(enabled ? ATTRIBUTEVALUE_YES : ATTRIBUTEVALUE_NO);
		if(internalname != null) {
			block.append("\" " + ATTRIBUTE_INTERNALNAME + "=\"");
			block.append(StringUtils.xmlEncode(internalname));
		}
		if(mnemonic != null && mnemonic > 0) {
			block.append("\" " + ATTRIBUTE_MNEMONIC + "=\"");
			block.append(mnemonic);
		}
		block.append("\"");
		if (translations != null && !translations.isEmpty()) {
			block.append(">");
			block.append(getLayoutMLTranslations(translations, blockDeep + 1));
			block.linebreak();
			block.append("</" + ELEMENT_TABBEDPANECONSTRAINTS + ">");
		} else {
			block.append(" />");
		}

		return block.getStringBuffer();
	}

	/**
	 * Method converting the {@link TableLayoutConstraints} to LayoutML XML.
	 *
	 *
	 * @see LayoutMLBlock
	 * @param c
	 * @param tableLayout
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLTableLayoutConstraints(Component c, TableLayout tableLayout, int blockDeep) {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);

		TableLayoutConstraints constraints = tableLayout.getConstraints(c);

		// <tablelayout-constraints col1="0" col2="0" row1="0" row2="0"
		// hAlign="0" vAlign="0" />
		block.append("<" + ELEMENT_TABLELAYOUTCONSTRAINTS + " ");
		block.append(ATTRIBUTE_COL1 + "=\"");
		block.append(constraints.col1);
		block.append("\" " + ATTRIBUTE_COL2 + "=\"");
		block.append(constraints.col2);
		block.append("\" " + ATTRIBUTE_ROW1 + "=\"");
		block.append(constraints.row1);
		block.append("\" " + ATTRIBUTE_ROW2 + "=\"");
		block.append(constraints.row2);
		block.append("\" " + ATTRIBUTE_HALIGN + "=\"");
		block.append(constraints.hAlign);
		block.append("\" " + ATTRIBUTE_VALIGN + "=\"");
		block.append(constraints.vAlign);
		block.append("\" />");

		return block.getStringBuffer();
	}

	/**
	 * Method converting PreferredSize to LayoutML XML.
	 *
	 *
	 * @see LayoutMLBlock
	 * @param cp
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLPreferredSizeFromProperty(ComponentProperties cp, int blockDeep) {
		return getLayoutMLSize(ELEMENT_PREFERREDSIZE, null, (PropertyValueDimension) cp.getProperty(WYSIWYGCollectableComponent.PROPERTY_PREFFEREDSIZE), blockDeep);
	}

	/**
	 * Method converting PreferredSize to LayoutML XML.
	 *
	 *
	 * @see LayoutMLBlock
	 * @param cp
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLSize(String element, String attributeName, PropertyValueDimension propertyValue, int blockDeep) {
		Dimension dim = null;
		if (propertyValue != null)
			dim = (Dimension) propertyValue.getValue();
		if (dim != null) {
			LayoutMLBlock block = new LayoutMLBlock(blockDeep);
			// <preferred-size height="30" width="80" />
			block.append("<" + element + " ");
			if (attributeName != null) {
				block.append(ATTRIBUTE_NAME + "=\"" + StringUtils.xmlEncode(attributeName) + "\" ");
			}
			block.append(ATTRIBUTE_HEIGHT + "=\"");
			block.append(dim.height);
			block.append("\" " + ATTRIBUTE_WIDTH + "=\"");
			block.append(dim.width);
			block.append("\" />");
			return block.getStringBuffer();
		}
		return new StringBuffer();
	}

	/**
	 * Method converting StrictSize to LayoutML XML.
	 *
	 *
	 * @see LayoutMLBlock
	 * @param cp
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLStrictSizeFromProperty(ComponentProperties cp, int blockDeep) {
		Dimension dim = null;
		if (cp.getProperty(WYSIWYGCollectableComponent.PROPERTY_STRICTSIZE) != null)
			dim = (Dimension) cp.getProperty(WYSIWYGCollectableComponent.PROPERTY_STRICTSIZE).getValue();
		if (dim != null) {
			LayoutMLBlock block = new LayoutMLBlock(blockDeep);
			// <preferred-size height="30" width="80" />
			block.append("<" + ELEMENT_STRICTSIZE + " ");
			block.append(ATTRIBUTE_HEIGHT + "=\"");
			block.append(dim.height);
			block.append("\" " + ATTRIBUTE_WIDTH + "=\"");
			block.append(dim.width);
			block.append("\" />");
			return block.getStringBuffer();
		}
		return new StringBuffer();
	}

	/**
	 * Only for WYSIWYGLayoutEditorPanel!
	 *
	 * @param cp
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLBackgroundColorFromProperty(ComponentProperties cp, int blockDeep) {
		return getLayoutMLColor(ELEMENT_BACKGROUND, null, (PropertyValueColor) cp.getProperty(WYSIWYGComponent.PROPERTY_BACKGROUNDCOLOR), blockDeep);
	}

	/**
	 * Only for WYSIWYGLayoutEditorPanel!
	 *
	 * @param cp
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLColor(String element, String attributeName, PropertyValueColor propertyValue, int blockDeep) {

		if (propertyValue != null && propertyValue.getValue() != null) {
			LayoutMLBlock block = new LayoutMLBlock(blockDeep);

			if (propertyValue.getValue() instanceof Color) {
				block.append("<" + element);
				if (attributeName != null) {
					block.append(" " +ATTRIBUTE_NAME + "=\"" + StringUtils.xmlEncode(attributeName) + "\"");
				}
				block.append(getLayoutMLColorAttributes((Color) propertyValue.getValue()));
				block.append(" />");
			}

			return block.getStringBuffer();
		}

		return new StringBuffer();
	}

	/**
	 * Method converting the Description {@link PropertyValueDescription} to LayoutML XML.
	 *
	 *
	 * @see LayoutMLBlock
	 * @param subform
	 * @param tableLayout
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLDescription(ComponentProperties cp, int blockDeep) {
		if (cp.getProperty(WYSIWYGStaticTextfield.PROPERTY_DESCRIPTION) != null && cp.getProperty(WYSIWYGStaticTextfield.PROPERTY_DESCRIPTION).getValue() != null) {
			LayoutMLBlock block = new LayoutMLBlock(blockDeep);
			block.append("<" + ELEMENT_DESCRIPTION + ">");
			block.append(StringUtils.xmlEncode((String) cp.getProperty(WYSIWYGStaticTextfield.PROPERTY_DESCRIPTION).getValue()));
			block.append("</" + ELEMENT_DESCRIPTION + ">");
			return block.getStringBuffer();
		}
		return new StringBuffer();
	}

	/**
	 * Method converting the Description {@link PropertyValueFont} to LayoutML XML.
	 *
	 *
	 * @see LayoutMLBlock
	 * @param subform
	 * @param tableLayout
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLFontFromProperty(ComponentProperties cp, int blockDeep) {
		return getLayoutMLFont(ELEMENT_FONT, null, (PropertyValueFont) cp.getProperty(WYSIWYGCollectableComponent.PROPERTY_FONT), blockDeep);
	}

	/**
	 * Method converting the Description {@link PropertyValueFont} to LayoutML XML.
	 *
	 *
	 * @see LayoutMLBlock
	 * @param subform
	 * @param tableLayout
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLFont(String element, String attributeName, PropertyValueFont propertyValue, int blockDeep) {
		if (propertyValue != null && propertyValue.getValue() != null) {
			LayoutMLBlock block = new LayoutMLBlock(blockDeep);
			block.append("<" + element + " ");
			if (attributeName != null) {
				block.append(ATTRIBUTE_NAME + "=\"" + StringUtils.xmlEncode(attributeName) + "\" ");
			}
			block.append(ATTRIBUTE_SIZE + "=\"" + ((Integer) propertyValue.getValue()).intValue() + "\"/>");
			return block.getStringBuffer();
		}
		return new StringBuffer();
	}

	/**
	 * Only for WYSIWYGLayoutEditorPanel!
	 *
	 * @param cp
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLBordersFromProperty(ComponentProperties cp, int blockDeep) {
		if (cp.getProperty(WYSIWYGComponent.PROPERTY_BORDER) != null && (cp.getProperty(WYSIWYGComponent.PROPERTY_BORDER).getValue() != null || ((PropertyValueBorder) cp.getProperty(WYSIWYGComponent.PROPERTY_BORDER)).isClearBorder())) {
			StringBuffer block = new StringBuffer();

			PropertyValueBorder value = (PropertyValueBorder) cp.getProperty(WYSIWYGComponent.PROPERTY_BORDER);


			if (value.getValue() != null) {
				Border border = value.getValue();
				/** Borders are nested, there can be one or more border */
				while (border instanceof CompoundBorder) {
					block = getLayoutMLBorderFromBorder(((CompoundBorder) border).getOutsideBorder(), blockDeep).append(block);
					border = ((CompoundBorder) border).getInsideBorder();
				}
				/** this single border is converted to layoutML */
				block = getLayoutMLBorderFromBorder(border, blockDeep).append(block);
			}

			if (value.isClearBorder()) {
				block = getLayoutMLClearBorder(blockDeep).append(block);
			}

			return block;
		}
		return new StringBuffer();
	}

	/**
	 * Method converting the ClearBorder to LayoutML XML.
	 *
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLClearBorder(int blockDeep) {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);
		block.append("<" + ELEMENT_CLEARBORDER + " />");
		return block.getStringBuffer();
	}

	/**
	 * Method converting Border(s) to LayoutML XML.
	 *
	 * @param blockDeep
	 * @param border
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLBorderFromBorder(Border border, int blockDeep) {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);

		if (border instanceof TitledBorder) {
			block.append("<" + ELEMENT_TITLEDBORDER + " ");
			block.append(ATTRIBUTE_TITLE + "=\"");
			block.append(StringUtils.xmlEncode(((TitledBorder) border).getTitle()) + "\">");
			block.append(getLayoutMLTranslations(((TitledBorderWithTranslations) border).getTranslations(), blockDeep + 1));
			block.linebreak();
			block.append("</" + ELEMENT_TITLEDBORDER + ">");
		} else if (border instanceof LineBorder) {
			block.append("<" + ELEMENT_LINEBORDER);
			block.append(getLayoutMLColorAttributes(((LineBorder) border).getLineColor()));
			block.append(" " + ATTRIBUTE_THICKNESS + "=\"");
			block.append(((LineBorder) border).getThickness());
			block.append("\" />");
		} else if (border instanceof BevelBorder) {
			block.append("<" + ELEMENT_BEVELBORDER + " ");
			block.append(ATTRIBUTE_TYPE + "=\"");
			block.append(((BevelBorder) border).getBevelType() == BevelBorder.RAISED ? ATTRIBUTEVALUE_RAISED : ATTRIBUTEVALUE_LOWERED);
			block.append("\" />");
		} else if (border instanceof EtchedBorder) {
			block.append("<" + ELEMENT_ETCHEDBORDER + " ");
			block.append(ATTRIBUTE_TYPE + "=\"");
			block.append(((EtchedBorder) border).getEtchType() == EtchedBorder.RAISED ? ATTRIBUTEVALUE_RAISED : ATTRIBUTEVALUE_LOWERED);
			block.append("\" />");
		} else if (border instanceof EmptyBorder) {
			block.append("<" + ELEMENT_EMPTYBORDER + " ");
			block.append(ATTRIBUTE_TOP + "=\"");
			block.append(((EmptyBorder) border).getBorderInsets().top + "\" ");
			block.append(ATTRIBUTE_LEFT + "=\"");
			block.append(((EmptyBorder) border).getBorderInsets().left + "\" ");
			block.append(ATTRIBUTE_BOTTOM + "=\"");
			block.append(((EmptyBorder) border).getBorderInsets().bottom + "\" ");
			block.append(ATTRIBUTE_RIGHT + "=\"");
			block.append(((EmptyBorder) border).getBorderInsets().right + "\" ");
			block.append("/>");
		}
		return block.getStringBuffer();
	}

	/**
	 * Method converting {@link LayoutMLRules}
	 *
	 * @param layoutMLRules
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLLayoutMLRules(LayoutMLRules layoutMLRules, int blockDeep) {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);

		block.append("<" + ELEMENT_RULES + ">");
		for (LayoutMLRule layoutMLRule : layoutMLRules.getRules()) {
			block.append(getLayoutMLLayoutMLRule(layoutMLRule, blockDeep + 1));
		}
		block.linebreak();
		block.append("</" + ELEMENT_RULES + ">");
		return block.getStringBuffer();
	}

	/**
	 * Method converting {@link LayoutMLRule}
	 *
	 * @param layoutMLRule
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLLayoutMLRule(LayoutMLRule layoutMLRule, int blockDeep) {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);

		if (layoutMLRule.getRuleName().length() > 0)
			block.append("<" + ELEMENT_RULE + " " + ATTRIBUTE_NAME + "=\"" + StringUtils.xmlEncode(layoutMLRule.getRuleName()) + "\">");
		else
			block.append("<" + ELEMENT_RULE + ">");

		block.append(getLayoutMLLayoutMLRuleEventType(layoutMLRule.getLayoutMLRuleEventType(), blockDeep + 1));
		if (layoutMLRule.getLayoutMLRuleCondition().getConditionString().length() > 0)
			block.append(getLayoutMLLayoutMLRuleCondition(layoutMLRule.getLayoutMLRuleCondition(), blockDeep + 1));

		block.append(getLayoutMLLayoutMLRuleActions(layoutMLRule.getLayoutMLRuleActions(), blockDeep + 1));
		block.linebreak();
		block.append("</" + ELEMENT_RULE + ">");

		return block.getStringBuffer();
	}

	/**
	 * Method converting {@link LayoutMLRuleEventType}
	 *
	 * @param layoutMLRuleEventType
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLLayoutMLRuleEventType(LayoutMLRuleEventType layoutMLRuleEventType, int blockDeep) {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);

		block.append("<" + ELEMENT_EVENT + " " + ATTRIBUTE_TYPE + "=\"");
		block.append(layoutMLRuleEventType.getEventType());
		block.append("\" ");

		if (!StringUtils.isNullOrEmpty(layoutMLRuleEventType.getEntity())) {
			block.append(ATTRIBUTE_ENTITY + "=\"" + layoutMLRuleEventType.getEntity() + "\" ");
		}

		block.append(ATTRIBUTE_SOURCECOMPONENT + "=\"" + layoutMLRuleEventType.getSourceComponent() + "\"");

		block.append("/>");

		return block.getStringBuffer();
	}

	/**
	 * Method converting {@link LayoutMLRuleCondition}
	 *
	 * NOT INTERPRETED BY LAYOUTMLPARSER!
	 *
	 * @param layoutMLRuleCondition
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLLayoutMLRuleCondition(LayoutMLRuleCondition layoutMLRuleCondition, int blockDeep) {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);

		if (layoutMLRuleCondition.getConditionString().length() > 0) {
			block.append("<" + ELEMENT_CONDITION + ">");
			block.linebreak();
			block.append(StringUtils.xmlEncode(layoutMLRuleCondition.getConditionString()));
			block.linebreak();
			block.append("<" + ELEMENT_CONDITION + ">");
			block.linebreak();
		}

		return block.getStringBuffer();
	}

	/**
	 * Method converting {@link LayoutMLRuleActions}
	 *
	 * NOT INTERPRETED BY LAYOUTMLPARSER!
	 *
	 * @param layoutMLRuleActions
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLLayoutMLRuleActions(LayoutMLRuleActions layoutMLRuleActions, int blockDeep) {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);

		block.append("<" + ELEMENT_ACTIONS + ">");
		if (layoutMLRuleActions.getSingleActions().size() > 0) {
			for (LayoutMLRuleAction singleAction : layoutMLRuleActions.getSingleActions()) {
				block.append(getLayoutMLLayoutMLRuleAction(singleAction, blockDeep + 1));
			}

		}
		block.linebreak();
		block.append("</" + ELEMENT_ACTIONS + ">");

		return block.getStringBuffer();
	}

	/**
	 * Method converting {@link LayoutMLRuleAction}
	 *
	 * @param layoutMLRuleAction
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLLayoutMLRuleAction(LayoutMLRuleAction layoutMLRuleAction, int blockDeep) {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);

		if (!layoutMLRuleAction.getRuleAction().equals("")) {
			if (layoutMLRuleAction.getRuleAction().equals(LayoutMLRuleAction.CLEAR)) {
				block.append("<" + ELEMENT_CLEAR + " ");
				if (!StringUtils.isNullOrEmpty(layoutMLRuleAction.getEntity())) {
					block.append(ATTRIBUTE_ENTITY + "=\"" + layoutMLRuleAction.getEntity() + "\" ");
				}
				block.append(ATTRIBUTE_TARGETCOMPONENT + "=\"" + layoutMLRuleAction.getTargetComponent() + "\"");
				block.append("/>");
			} else if (layoutMLRuleAction.getRuleAction().equals(LayoutMLRuleAction.ENABLE)) {
				block.append("<" + ELEMENT_ENABLE + " ");
				block.append(ATTRIBUTE_TARGETCOMPONENT + "=\"" + layoutMLRuleAction.getTargetComponent() + "\" ");
				String invertableValue = layoutMLRuleAction.isInvertable() ? "yes" : "no";
				block.append(ATTRIBUTE_INVERTABLE + "=\"" + invertableValue + "\"");
				block.append("/>");
			} else if (layoutMLRuleAction.getRuleAction().equals(LayoutMLRuleAction.REFRESH_VALUELIST)) {
				block.append("<" + ELEMENT_REFRESHVALUELIST + " ");
				if (!StringUtils.isNullOrEmpty(layoutMLRuleAction.getEntity())) {
					block.append(ATTRIBUTE_ENTITY + "=\"" + layoutMLRuleAction.getEntity() + "\" ");
				}
				block.append(ATTRIBUTE_TARGETCOMPONENT + "=\"" + layoutMLRuleAction.getTargetComponent() + "\" ");
				if (!StringUtils.isNullOrEmpty(layoutMLRuleAction.getParameterForSourceComponent())) {
					block.append(ATTRIBUTE_PARAMETER_FOR_SOURCECOMPONENT + "=\"" + layoutMLRuleAction.getParameterForSourceComponent() + "\"");
				}
				block.append("/>");
			} else if (layoutMLRuleAction.getRuleAction().equals(LayoutMLRuleAction.TRANSFER_LOOKEDUP_VALUE)) {
				block.append("<" + ELEMENT_TRANSFERLOOKEDUPVALUE + " ");
				if (!StringUtils.isNullOrEmpty(layoutMLRuleAction.getSourceField())) {
					// FIX NUCLEUSINT-305
					block.append(ATTRIBUTE_SOURCEFIELD + "=\"" + layoutMLRuleAction.getSourceField() + "\" ");
				}
				block.append(ATTRIBUTE_TARGETCOMPONENT + "=\"" + layoutMLRuleAction.getTargetComponent() + "\"");
				block.append("/>");
			}
		}

		return block.getStringBuffer();
	}

	/**
	 * Method for converting {@link WYSIWYGValuelistProvider} to LayoutML XML
	 *
	 * @param wysiwygStaticValuelistProvider
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLValueListProvider(WYSIWYGValuelistProvider wysiwygStaticValuelistProvider, int blockDeep) {
		return getLayoutMLValueListProvider(ELEMENT_VALUELISTPROVIDER, null, wysiwygStaticValuelistProvider, blockDeep);
	}

	/**
	 * Method for converting {@link WYSIWYGValuelistProvider} to LayoutML XML
	 *
	 * @param wysiwygStaticValuelistProvider
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLValueListProvider(String element, String attributeName, PropertyValueValuelistProvider propertyValue, int blockDeep) {
		if (propertyValue != null) {
			return getLayoutMLValueListProvider(element, attributeName, propertyValue.getValue(), blockDeep);
		}
		return new StringBuffer();
	}

	/**
	 * Method for converting {@link WYSIWYGValuelistProvider} to LayoutML XML
	 *
	 * @param wysiwygStaticValuelistProvider
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLValueListProvider(String element, String attributeName, WYSIWYGValuelistProvider wysiwygStaticValuelistProvider, int blockDeep) {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);

		block.append("<" + element);
		if (attributeName != null) {
			block.append(" " + ATTRIBUTE_NAME + "=\"" + StringUtils.xmlEncode(attributeName) + "\"");
		}
		block.append(" type=\"" + StringUtils.xmlEncode(wysiwygStaticValuelistProvider.getType()) + "\"");
		if (wysiwygStaticValuelistProvider.isEntityAndFieldAvaiable()) {
			block.append(" " + ATTRIBUTE_ENTITY + "=\"" + StringUtils.xmlEncode(wysiwygStaticValuelistProvider.getEntity()) + "\"");
			block.append(" " + ATTRIBUTE_FIELD + "=\"" + StringUtils.xmlEncode(wysiwygStaticValuelistProvider.getField()) + "\"");
		}

		if (wysiwygStaticValuelistProvider.getAllWYSIYWYGParameter().size() == 0) {
			block.append("/>");
		} else {
			block.append(">");
			for (WYSIYWYGParameter wysiwygParameter : wysiwygStaticValuelistProvider.getAllWYSIYWYGParameter()) {
				block.append(getLayoutMLParameter(wysiwygParameter, blockDeep + 1));
			}
			block.linebreak();
			block.append("</" + element + ">");
		}

		return block.getStringBuffer();
	}

	/**
	 * Method for converting {@link WYSIWYGOptions} to LayoutML XML
	 *
	 * @param options
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLOptions(WYSIWYGOptions options, int blockDeep) {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);

		block.append("<" + ELEMENT_OPTIONS);
		block.append(" name=\"" + StringUtils.xmlEncode(options.getName()) + "\"");
		block.append(" default=\"" + StringUtils.xmlEncode(options.getDefaultValue()) + "\"");
		block.append(" orientation=\"" + StringUtils.xmlEncode(options.getOrientation()) + "\"");
		block.append(">");

		for (WYSIWYGOption option : options.getAllOptionValues()) {
			block.append(getLayoutMLOption(option, blockDeep + 1));
		}

		block.linebreak();
		block.append("</" + ELEMENT_OPTIONS + ">");

		return block.getStringBuffer();
	}

	/**
	 * Method for converting {@link WYSIWYGOption} to LayoutML XML.
	 *
	 * @param option
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLOption(WYSIWYGOption option, int blockDeep) {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);

		block.append("<" + ELEMENT_OPTION);
		if (!option.getName().equals(""))
			block.append(" " + ATTRIBUTE_NAME + "=\"" + StringUtils.xmlEncode(option.getName()) + "\"");
		block.append(" " + ATTRIBUTE_VALUE + "=\"" + StringUtils.xmlEncode(option.getValue()) + "\"");
		block.append(" " + ATTRIBUTE_LABEL + "=\"" + StringUtils.xmlEncode(option.getLabel()) + "\"");
		if (!option.getMnemonic().equals(""))
			block.append(" " + ATTRIBUTE_MNEMONIC + "=\"" + StringUtils.xmlEncode(option.getMnemonic()) + "\"");
		block.append(">");
		block.append(getLayoutMLTranslations(option.getTranslations(), blockDeep + 1));
		block.linebreak();
		block.append("</" + ELEMENT_OPTION + ">");

		return block.getStringBuffer();
	}

	/**
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLForProperty(String name, PropertyValue<?> property, int blockDeep) {

		if (property instanceof PropertyValueString ||
				property instanceof PropertyValueInteger ||
				property instanceof PropertyValueBoolean) {
			LayoutMLBlock block = new LayoutMLBlock(blockDeep);
			block.append("<" + ELEMENT_PROPERTY);
			block.append(" " + ATTRIBUTE_NAME + "=\"" + StringUtils.xmlEncode(name) + "\"");
			block.append(" " + ATTRIBUTE_VALUE + "=\"");
			if (property instanceof PropertyValueString || property instanceof PropertyValueInteger) {
				block.append(StringUtils.xmlEncode(property.getValue().toString()));
			} else if (property instanceof PropertyValueBoolean) {
				if (((Boolean) property.getValue())) {
					block.append(ATTRIBUTEVALUE_YES);
				} else {
					block.append(ATTRIBUTEVALUE_NO);
				}
			}
			block.append("\"/>");
			return block.getStringBuffer();
		} else if (property instanceof PropertyValueDimension) {
			return getLayoutMLSize(ELEMENT_PROPERTY_SIZE, name, (PropertyValueDimension) property, blockDeep);
		} else if (property instanceof PropertyValueColor) {
			return getLayoutMLColor(ELEMENT_PROPERTY_COLOR, name, (PropertyValueColor) property, blockDeep);
		} else if (property instanceof PropertyValueFont) {
			return getLayoutMLFont(ELEMENT_PROPERTY_FONT, name, (PropertyValueFont) property, blockDeep);
		} else if (property instanceof PropertyValueScript) {
			return getLayoutMLScript(ELEMENT_PROPERTY_SCRIPT, name, (PropertyValueScript) property, blockDeep);
		} else if (property instanceof PropertyValueTranslations) {
			return getLayoutMLTranslations(ELEMENT_PROPERTY_TRANSLATIONS, name, (PropertyValueTranslations) property, blockDeep);
		} else if (property instanceof PropertyValueValuelistProvider) {
			return getLayoutMLValueListProvider(ELEMENT_PROPERTY_VALUELIST_PROVIDER, name, (PropertyValueValuelistProvider) property, blockDeep);
		} else  {
			throw new NotImplementedException("getLayoutMLForProperty with type " + property.getClass().getName());
		}

	}

	/**
	 * Method for converting {@link WYSIYWYGProperty} to LayoutML XML.
	 *
	 * @param wysiwygProperty
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLCollectableComponentProperty(WYSIYWYGProperty wysiwygProperty, int blockDeep) {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);

		Vector<WYSIYWYGPropertySet> vector = wysiwygProperty.getAllPropertyEntries();

		for (Iterator<WYSIYWYGPropertySet> it = vector.iterator(); it.hasNext(); ) {
			WYSIYWYGPropertySet propertySet = it.next();
			block.append("<" + ELEMENT_PROPERTY);
			block.append(" " + ATTRIBUTE_NAME + "=\"" + StringUtils.xmlEncode(propertySet.getPropertyName()) + "\"");
			block.append(" " + ATTRIBUTE_VALUE + "=\"" + StringUtils.xmlEncode(propertySet.getPropertyValue()) + "\"");
			block.append("/>");
			if (it.hasNext())
				block.linebreak();
		}

		return block.getStringBuffer();
	}

	/**
	 * Method for converting {@link WYSIYWYGParameter} to LayoutML XML.
	 *
	 * @param wysiwygParameter
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLParameter(WYSIYWYGParameter wysiwygParameter, int blockDeep) {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);

		/** <dependency dependant-field="process" depends-on="module"/> */

		block.append("<" + ELEMENT_PARAMETER + " ");
		block.append(ATTRIBUTE_NAME + "=\"" + StringUtils.xmlEncode(wysiwygParameter.getParameterName()) + "\" ");
		block.append(ATTRIBUTE_VALUE + "=\"" + StringUtils.xmlEncode(wysiwygParameter.getParameterValue()) + "\" ");
		block.append("/>");

		return block.getStringBuffer();
	}

	/**
	 * Method for converting {@link Color} to LayoutML XML.
	 *
	 * @param color
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLColorAttributes(Color color) {
		StringBuffer sb = new StringBuffer();

		sb.append(" " + ATTRIBUTE_RED + "=\"");
		sb.append(color.getRed());
		sb.append("\" " + ATTRIBUTE_GREEN + "=\"");
		sb.append(color.getGreen());
		sb.append("\" " + ATTRIBUTE_BLUE + "=\"");
		sb.append(color.getBlue());
		sb.append("\"");

		return sb;

	}

	/**
	 *  Method for converting {@link Dimension} minimumSize to LayoutML XML.
	 *
	 * @param c
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLMinimumSizeFromComponent(Component c, int blockDeep) {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);

		Dimension dim = c.getMinimumSize();
		if (dim != null) {
			// <minimum-size height="30" width="80" />
			block.append("<" + ELEMENT_MINIMUMSIZE + " ");
			block.append(ATTRIBUTE_HEIGHT + "=\"");
			block.append(dim.height);
			block.append("\" " + ATTRIBUTE_WIDTH + "=\"");
			block.append(dim.width);
			block.append("\" />");
		}

		return block.getStringBuffer();
	}

	/**
	 *  Method for converting {@link WYSIWYGInitialSortingOrder} minimumSize to LayoutML XML.
	 *
	 * @param value
	 * @param blockDeep
	 * @return {@link StringBuffer} with the LayoutML
	 */
	private synchronized StringBuffer getLayoutMLInitialSortingOrder(WYSIWYGInitialSortingOrder value, int blockDeep) {
		if (value != null) {
			LayoutMLBlock block = new LayoutMLBlock(blockDeep);
			block.append("<" + ELEMENT_INITIALSORTINGORDER + " " + ATTRIBUTE_NAME + "=\"" + value.getName() + "\" ");
			block.append(ATTRIBUTE_SORTINGORDER + "=\"" + value.getSortingOrder() + "\" />");
			return block.getStringBuffer();
		} else {
			return new StringBuffer();
		}
	}

	private synchronized StringBuffer getLayoutMLTranslations(TranslationMap translations, int blockDeep) {
		return getLayoutMLTranslations(ELEMENT_TRANSLATIONS, null, translations, blockDeep);
	}

	private synchronized StringBuffer getLayoutMLTranslationsFromProperty(ComponentProperties cp, int blockDeep) {
		return getLayoutMLTranslations(ELEMENT_TRANSLATIONS, null, (PropertyValueTranslations) cp.getProperty(WYSIWYGCollectableComponent.PROPERTY_TRANSLATIONS), blockDeep);
	}

	private synchronized StringBuffer getLayoutMLTranslations(String element, String attributeName, PropertyValueTranslations propertyValue, int blockDeep) {
		if (propertyValue != null) {
			TranslationMap translations = (TranslationMap) propertyValue.getValue();
			return getLayoutMLTranslations(element, attributeName, translations, blockDeep);
		}
		return new StringBuffer();
	}

	private synchronized StringBuffer getLayoutMLTranslations(String element, String attributeName, TranslationMap translations, int blockDeep) {
		if (translations != null && !translations.isEmpty()) {
			LayoutMLBlock block = new LayoutMLBlock(blockDeep);
			block.append("<" + element);
			if (attributeName != null) {
				block.append(" " + ATTRIBUTE_NAME + "=\"" + StringUtils.xmlEncode(attributeName) + "\"");
			}
			block.append(">");
			for (Map.Entry<String, String> e : translations.entrySet()) {
				String lang = e.getKey();
				String text = e.getValue();
				LayoutMLBlock block2 = new LayoutMLBlock(blockDeep + 1);
				block2.append("<" + ELEMENT_TRANSLATION);
				block2.append(" " + ATTRIBUTE_LANG + "=\"" + StringUtils.xmlEncode(lang) + "\"");
				block2.append(" " + ATTRIBUTE_TEXT + "=\"" + StringUtils.xmlEncode(text) + "\"");
				block2.append("/>");
				block.append(block2.getStringBuffer());
			}
			block.linebreak();
			block.append("</" + element + ">");
			return block.getStringBuffer();
		}
		return new StringBuffer();
	}


	private synchronized StringBuffer getLayoutMLScriptFromProperty(String property, String element, ComponentProperties cp, int blockDeep) {
		PropertyValue<?> propertyValue = cp.getProperty(property);
		if (propertyValue == null) {
			return new StringBuffer();
		}
		return getLayoutMLScript(element, null, (PropertyValueScript) propertyValue, blockDeep);
	}

	private synchronized StringBuffer getLayoutMLScript(String element, String attributeName, PropertyValueScript propertyValue, int blockDeep) {
		LayoutMLBlock block = new LayoutMLBlock(blockDeep);

		NuclosScript script = (NuclosScript) propertyValue.getValue();
		if (script == null) {
			return new StringBuffer();
		}

		block.append("<" + element + " ");
		if (attributeName != null) {
			block.append(ATTRIBUTE_NAME + "=\"" + StringUtils.xmlEncode(attributeName) + "\" ");
		}
		block.append(ATTRIBUTE_LANGUAGE + "=\"");
		block.append(StringUtils.xmlEncode(script.getLanguage()));
		block.append("\">");
		block.linebreak();
		block.append("<![CDATA[");
		block.linebreak();
		block.append(script.getSource());
		block.linebreak();
		block.append("]]>");
		block.linebreak();
		block.append("</" + element + ">");
		return block.getStringBuffer();
	}

	/** LayoutMLDependencies are not supported by the Parser, is therefor commented out */
	// private synchronized StringBuffer
	// getLayoutMLLayoutMLDependencies(LayoutMLDependencies
	// layoutMLDependencies, int blockDeep) {
	// LayoutMLBlock block = new LayoutMLBlock(blockDeep);
	//
	// block.append("<" + ELEMENT_DEPENDENCIES + ">");
	// for (LayoutMLDependency layoutMLDependency :
	// layoutMLDependencies.getAllDependencies()) {
	// block.append(getLayoutMLLayoutMLDependency(layoutMLDependency, blockDeep
	// + 1));
	// }
	// block.linebreak();
	// block.append("</" + ELEMENT_DEPENDENCIES + ">");
	// return block.getStringBuffer();
	// }
	//
	// private synchronized StringBuffer
	// getLayoutMLLayoutMLDependency(LayoutMLDependency layoutMLDependency, int
	// blockDeep) {
	// LayoutMLBlock block = new LayoutMLBlock(blockDeep);
	//
	// /** <dependency dependant-field="process" depends-on="module"/> */
	//
	// block.append("<" + ELEMENT_DEPENDENCY + " ");
	// block.append(ATTRIBUTE_DEPENDANTFIELD + "=\"" +
	// layoutMLDependency.getDependendField() + "\" ");
	// block.append(ATTRIBUTE_DEPENDSONFIELD + "=\"" +
	// layoutMLDependency.getDependsOnField() + "\" ");
	// block.append("/>");
	//
	// return block.getStringBuffer();
	// }

	/**
	 * Small Helperclass used for containing the LayoutML XML and making "pretty" XML by Indendation and new lines.
	 *
	 * Is using a StringBuffer internal so there is no need to work on Strings in the LayoutML Generationprocess
	 *
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 *
	 * @author <a href="mailto:maik.stueker@novabit.de">maik.stueker</a>
	 * @version 01.00.00
	 */


	private class LayoutMLBlock {

		private static final String distanceString = "  ";

		private int blockDeep;

		private StringBuffer sb;

		LayoutMLBlock(int blockDeep) {
			this(blockDeep, true);
		}

		LayoutMLBlock(int blockDeep, boolean withInitialLineBreak) {
			this.blockDeep = blockDeep;
			this.sb = new StringBuffer();
			if (withInitialLineBreak) {
				this.linebreak();
			} else {
				sb.append(getDistanceString());
			}
		}

		public void append(double d) {
			sb.append(d);
		}

		public void append(int i) {
			sb.append(i);
		}

		public void append(String s) {
			sb.append(s);
		}

		public void append(StringBuffer s) {
			sb.append(s);
		}

		/**
		 * appends a linebreak to the String
		 */
		public void linebreak() {
			sb.append("\n");
			sb.append(getDistanceString());
		}

		@Override
		public String toString() {
			return sb.toString();
		}

		public StringBuffer getStringBuffer() {
			return sb;
		}

		private StringBuffer getDistanceString() {
			StringBuffer sbDistance = new StringBuffer();
			for (int i = 0; i < blockDeep; i++) {
				sbDistance.append(distanceString);
			}
			return sbDistance;
		}
	}
}
