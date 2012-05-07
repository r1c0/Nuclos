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
package org.nuclos.client.layout;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.border.Border;

import org.apache.log4j.Logger;
import org.nuclos.client.common.LocaleDelegate;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.layout.wysiwyg.LayoutMLGenerator;
import org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation;
import org.nuclos.client.layout.wysiwyg.component.ComponentProcessors;
import org.nuclos.client.layout.wysiwyg.component.TitledBorderWithTranslations;
import org.nuclos.client.layout.wysiwyg.component.TranslationMap;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGSubForm;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGTabbedPane;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyUtils;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueBoolean;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueBorder;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueString;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
import org.nuclos.client.layout.wysiwyg.editor.util.TableLayoutUtil;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.LayoutCell;
import org.nuclos.common.DefaultComponentTypes;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosImage;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.LocaleInfo;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.layoutml.LayoutMLConstants;

public abstract class AbstractLayoutMLFactory {
	
	private static final Logger LOG = Logger.getLogger(AbstractLayoutMLFactory.class);
	
	public static final String[] EDIT_FIELDS = {"STRCREATED", "DATCREATED", "STRCHANGED", "DATCHANGED" };
	
	public static final int I_CELL_HEIGHT = 35;
	public static final int I_CELL_HEIGHT_EXT = 30;
	public static final int I_CELL_WIDTH = 35;
	public static final int I_CELL_WIDTH_EXT = 50;
	public static final int I_MAX_WIDTH_GROUP = 35;
	public static final int I_SUBFORM_PANEL_HEIGHT = 300;
	public static final int I_SUBFORM_HEIGHT = 250;
	public static final int I_PANEL_SUBFORM_HEIGHT = 200;
	public static final int I_PANEL_WIDTH = 150;
	public static final int I_EDITCELL_WIDTH = 250;
	public static final int I_EDITCELL_HEIGTH = 50;
	public static final int I_CREATEAT_WIDTH = 85;
	public static final int I_CREATEBY_WIDTH = 75;
	
	public abstract String getResourceText(String resourceId);
	
	public abstract Collection<EntityMetaDataVO> getEntityMetaData();
	
	public abstract Collection<EntityFieldMetaDataVO> getEntityFieldMetaData(String entity);
	
	public abstract Map<Long, String> getAttributeGroups();

	public String generateLayout(String entity, List<EntityFieldMetaDataVO> fields, boolean groupAttributes, boolean withSubforms, boolean withEditFields) throws CommonBusinessException {		
		final WYSIWYGLayoutEditorPanel panel = new WYSIWYGLayoutEditorPanel(new WYSIWYGMetaInformation());
		final TableLayoutUtil util = panel.getTableLayoutUtil();
		util.createStandardLayout();

		final List<EntityFieldMetaDataVO> setEditFields = new ArrayList<EntityFieldMetaDataVO>();
		for(EntityFieldMetaDataVO voField : fields) {
			if(isEditField(voField)) {
				setEditFields.add(voField);
			}
		}

		 int maxWidth = 0;

		 for(EntityFieldMetaDataVO field : fields) {
			 if(isSystemField(field))
				 continue;

			 String sdesc = getResourceText(field.getLocaleResourceIdForDescription());
			 Component c = ComponentProcessors.getInstance().createComponent(LayoutMLConstants.ELEMENT_COLLECTABLECOMPONENT, LayoutMLConstants.ELEMENT_LABEL, panel.getMetaInformation(), field.getField());
			 Font font = new Font("Courier", Font.PLAIN, 12);
			 c.setFont(font);
			 FontMetrics fontmetrics = c.getFontMetrics(font);
			 if(maxWidth < fontmetrics.stringWidth(sdesc)) {
				 maxWidth = fontmetrics.stringWidth(sdesc);
			 }
		 }
		 if(maxWidth < InterfaceGuidelines.DEFAULT_COLUMN_WIDTH ) {
			 maxWidth = InterfaceGuidelines.DEFAULT_COLUMN_WIDTH;
		 }

		 int counter = 1;

		 if(groupAttributes) {
			 counter = buildAttributeGroupsForLayout(util, fields, maxWidth, counter);
		 }
		 else {
			 counter = createComponentForLayout(panel, util, fields, maxWidth, counter);
		 }

		 if(withSubforms) {

			 Map<String, String> mpSubEntites = getSubformEntities(entity);
			 if(mpSubEntites.size() > 1) {
				 counter = buildSubformsLayout(util, maxWidth, counter,	groupAttributes, mpSubEntites);
			 }
			 else if(mpSubEntites.size() == 1) {
				 counter = buildSubformLayout(util, counter, groupAttributes, mpSubEntites);
			 }

			 if(mpSubEntites.size() > 0) {
				 LayoutCell cell = createBetweenCell(counter);
				 util.addRow(cell);
				 counter++;
			 }

		 }
		 // subforms end

		 if(withEditFields)
			 buildEditFieldsPanel(panel, util, setEditFields, maxWidth, counter, groupAttributes);

		 createDefaultBorder(util);

		LayoutMLGenerator gen = new LayoutMLGenerator();
		String layoutml = gen.getLayoutML(panel);
		
		return layoutml;
	}
	
	private Map<String, String> getSubformEntities(String entity) {
		Map<String, String> mpSubEntities = new HashMap<String, String>();
		for(EntityMetaDataVO voEntity : getEntityMetaData()) {
			if(voEntity.equals(entity))
				continue;
			for(EntityFieldMetaDataVO voField : MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(voEntity.getEntity()).values()) {
				if(voField.getForeignEntity() != null && voField.getForeignEntity().equals(entity) && voField.getForeignEntityField() == null) {
					mpSubEntities.put(voEntity.getEntity(), voField.getField());
					break;
				}
			}
		}
		return mpSubEntities;
	}
	
	private int buildSubformLayout(TableLayoutUtil util, int counter, boolean groupAttributes, Map<String, String> mpSubEntites) throws CommonBusinessException {
		 String sSubEntity = mpSubEntites.keySet().iterator().next();
		 WYSIWYGLayoutEditorPanel panelSubEntites = (WYSIWYGLayoutEditorPanel)ComponentProcessors.getInstance().createComponent(LayoutMLConstants.ELEMENT_PANEL, LayoutMLConstants.ELEMENT_LABEL, new WYSIWYGMetaInformation(), "subEntites");
		 LayoutCell cell = new LayoutCell();
		 cell.setCellX(1);
		 if(groupAttributes)
			 cell.setCell2X(2);
		 else
			 cell.setCell2X(4);
		 cell.setCellY(counter);
		 cell.setCell2Y(counter++);
		 cell.setCellHeight(I_SUBFORM_PANEL_HEIGHT);
		 cell.setCellWidth(I_MAX_WIDTH_GROUP);
		 util.addRow(cell);
		 util.insertComponentTo(panelSubEntites, cell);

		 WYSIWYGSubForm subForm = (WYSIWYGSubForm)ComponentProcessors.getInstance().createComponent(LayoutMLConstants.ELEMENT_SUBFORM, LayoutMLConstants.ELEMENT_SUBFORM, new WYSIWYGMetaInformation(), sSubEntity);
		 subForm.setProperty(WYSIWYGSubForm.PROPERTY_ENTITY, new PropertyValueString(sSubEntity), PropertyUtils.getValueClass(subForm, WYSIWYGSubForm.PROPERTY_ENTITY));
		 subForm.setProperty(WYSIWYGSubForm.PROPERTY_FOREIGNKEY, new PropertyValueString(mpSubEntites.get(sSubEntity)), PropertyUtils.getValueClass(subForm, WYSIWYGSubForm.PROPERTY_FOREIGNKEY));

		 subForm.setName(sSubEntity);
		 subForm.setProperty(WYSIWYGSubForm.PROPERTY_NAME, new PropertyValueString(sSubEntity), PropertyUtils.getValueClass(subForm, WYSIWYGSubForm.PROPERTY_NAME));
		 subForm.setSize(I_SUBFORM_PANEL_HEIGHT, I_SUBFORM_HEIGHT);

		 TableLayoutConstraints constr = new TableLayoutConstraints(1, 1, 1, 1, 2, 2);
		 panelSubEntites.getTableLayoutUtil().insertComponentTo(subForm, constr);
		 return counter;
	}
	
	private int buildSubformsLayout(TableLayoutUtil util, int maxWidth,	int counter, boolean groupAttributes, Map<String, String> mpSubEntites)
			throws CommonBusinessException {
			WYSIWYGLayoutEditorPanel panelSubEntites = (WYSIWYGLayoutEditorPanel)ComponentProcessors.getInstance().createComponent(LayoutMLConstants.ELEMENT_PANEL, LayoutMLConstants.ELEMENT_LABEL, new WYSIWYGMetaInformation(), "subEntites");
			TranslationMap tm = new TranslationMap();
			tm.put("de", "");
			tm.put("en", "");
			Border border = new TitledBorderWithTranslations("", tm);

			PropertyValueBorder prop = new PropertyValueBorder();
			prop.setValue(border);


			panelSubEntites.setProperty(WYSIWYGComponent.PROPERTY_BORDER, prop, Border.class);

			LayoutCell cell = new LayoutCell();
			 cell.setCellX(1);
			 if(groupAttributes)
				 cell.setCell2X(2);
			 else
				 cell.setCell2X(4);
			 cell.setCellY(counter);
			 cell.setCell2Y(counter);
			 int iHeight = I_PANEL_SUBFORM_HEIGHT;

			 cell.setCellHeight(iHeight+100);
			 cell.setCellWidth(maxWidth);
			 panelSubEntites.setPreferredSize(new Dimension(I_PANEL_WIDTH, iHeight+35));
			 util.addRow(cell);
			 if (counter == 1) {
			 	util.addCol(cell);
			 }

			 util.insertComponentTo(panelSubEntites, cell);
			 TableLayoutUtil utilSubEntities = panelSubEntites.getTableLayoutUtil();

			 cell = new LayoutCell();
			 cell.setCellX(1);
			 cell.setCell2X(2);
			 cell.setCellY(1);
			 cell.setCell2Y(1);
			 iHeight = I_PANEL_SUBFORM_HEIGHT;

			 cell.setCellHeight(iHeight+70);
			 cell.setCellWidth(maxWidth);
			 WYSIWYGTabbedPane tabPane = (WYSIWYGTabbedPane)ComponentProcessors.getInstance().createComponent(LayoutMLConstants.ELEMENT_TABBEDPANE, LayoutMLConstants.ELEMENT_TABBEDPANE, new WYSIWYGMetaInformation(), "tab");
			 utilSubEntities.addRow(cell);
			 utilSubEntities.addCol(cell);
			 utilSubEntities.insertComponentTo(tabPane, cell);

			 int tabcounter = 0;
			 for(String sSubEntity : mpSubEntites.keySet()) {
				 Component comp = null;
				 try {
					comp = tabPane.getComponent(tabcounter++);
				 }
				 catch(Exception e) {
					 // new tab necessary
					 LOG.info("buildSubformsLayout: " + e + " (new tab?)", e);
					 comp = ComponentProcessors.getInstance().createComponent(LayoutMLConstants.ELEMENT_PANEL, LayoutMLConstants.ELEMENT_LABEL, new WYSIWYGMetaInformation(), "subEntites");
					 tabPane.addTab(sSubEntity, comp);
				 }

				 if(!(comp instanceof WYSIWYGLayoutEditorPanel))
					 continue;
				 WYSIWYGSubForm subForm = (WYSIWYGSubForm)ComponentProcessors.getInstance().createComponent(LayoutMLConstants.ELEMENT_SUBFORM, LayoutMLConstants.ELEMENT_SUBFORM, new WYSIWYGMetaInformation(), sSubEntity);
				 subForm.setProperty(WYSIWYGSubForm.PROPERTY_ENTITY, new PropertyValueString(sSubEntity), PropertyUtils.getValueClass(subForm, WYSIWYGSubForm.PROPERTY_ENTITY));
				 subForm.setProperty(WYSIWYGSubForm.PROPERTY_FOREIGNKEY, new PropertyValueString(mpSubEntites.get(sSubEntity)), PropertyUtils.getValueClass(subForm, WYSIWYGSubForm.PROPERTY_FOREIGNKEY));

				 subForm.setName(sSubEntity);
				 subForm.setProperty(WYSIWYGSubForm.PROPERTY_NAME, new PropertyValueString(sSubEntity), PropertyUtils.getValueClass(subForm, WYSIWYGSubForm.PROPERTY_NAME));
				 subForm.setSize(tabPane.getWidth(), tabPane.getHeight()*2);

				 WYSIWYGLayoutEditorPanel tabPanel = (WYSIWYGLayoutEditorPanel)comp;

				 cell = new LayoutCell();
				 cell.setCellX(1);
				 cell.setCell2X(1);
				 cell.setCellY(1);
				 cell.setCell2Y(1);
				 cell.setCellHeight(tabPanel.getHeight()*2);
				 cell.setCellWidth(tabPanel.getWidth());
				 cell.setCellDimensionsHeight(tabPanel.getHeight()*2);
				 cell.setCellDimensionsWidth(tabPanel.getWidth());
				 TableLayoutConstraints constr = new TableLayoutConstraints(1, 1, 1, 1, 2, 2);

				 tabPanel.getTableLayoutUtil().insertComponentTo(subForm, constr);

				 tabPane.setTitleAt(tabcounter-1, sSubEntity);

			 }
			 counter++;
			return counter;
		}
	
	private void buildEditFieldsPanel(WYSIWYGLayoutEditorPanel panel, TableLayoutUtil util, List<EntityFieldMetaDataVO> setEditFields, int maxWidth, int counter, boolean groupAttributes) throws CommonBusinessException {
		int columncounter = 1;
		 int rowcounter = 1;
		 List<EntityFieldMetaDataVO> lstEditFields = sortEditFields(setEditFields);

		 WYSIWYGLayoutEditorPanel panelEdit = (WYSIWYGLayoutEditorPanel)ComponentProcessors.getInstance().createComponent(LayoutMLConstants.ELEMENT_PANEL, LayoutMLConstants.ELEMENT_LABEL, new WYSIWYGMetaInformation(), "Editierung");
		 LayoutCell upperLeftCorner = panelEdit.getTableLayoutUtil().getLayoutCellByPosition(0, 0);
		 panelEdit.getTableLayoutUtil().modifyTableLayoutSizes(TableLayoutUtil.ACTION_TOGGLE_STANDARDBORDER, true, upperLeftCorner, false, false);
		 panelEdit.getTableLayoutUtil().modifyTableLayoutSizes(TableLayoutUtil.ACTION_TOGGLE_STANDARDBORDER, false, upperLeftCorner, false, false);

		 TranslationMap tm = new TranslationMap(LocaleDelegate.getInstance().getAllResourcesByStringId("nuclos.layout.editfield"));
		 Border border = new TitledBorderWithTranslations(tm.remove(LocaleInfo.I_DEFAULT_TAG), tm);

		 PropertyValueBorder prop = new PropertyValueBorder();
		 prop.setValue(border);


		 panelEdit.setProperty(WYSIWYGComponent.PROPERTY_BORDER, prop, Border.class);
		 LayoutCell cellEdit = new LayoutCell();
		 cellEdit.setCellX(columncounter);
		 cellEdit.setCell2X(columncounter + (groupAttributes ? 1 : 3));
		 cellEdit.setCellY(counter);
		 cellEdit.setCell2Y(counter);

		 cellEdit.setCellHeight(I_EDITCELL_HEIGTH);
		 cellEdit.setCellWidth(I_EDITCELL_WIDTH);
		 panelEdit.setPreferredSize(new Dimension(I_CELL_WIDTH, I_CELL_HEIGHT));

		 util.addRow(cellEdit);

		 util.insertComponentTo(panelEdit, cellEdit);

		 TableLayoutUtil utilEdit = panelEdit.getTableLayoutUtil();

		 columncounter = 1;
		 rowcounter = 1;

		 for(EntityFieldMetaDataVO field : lstEditFields) {
			 String controltype = getCollectableComponentType(field);
			 // Label
			 Component c = ComponentProcessors.getInstance().createComponent(LayoutMLConstants.ELEMENT_COLLECTABLECOMPONENT, LayoutMLConstants.ELEMENT_LABEL, panel.getMetaInformation(), field.getField());
			 LayoutCell cell = new LayoutCell();
			 cell.setCellX(columncounter);
			 cell.setCell2X(columncounter);
			 cell.setCellY(rowcounter);
			 cell.setCell2Y(rowcounter);
			 cell.setCellHeight(c.getPreferredSize().height);
			 cell.setCellWidth(75);
			 c.setPreferredSize(new Dimension(maxWidth, c.getPreferredSize().height));
			 utilEdit.addCol(cell);

			 utilEdit.insertComponentTo((WYSIWYGComponent)c, cell);

			 cell = new LayoutCell();
			 cell.setCellX(++columncounter);
			 cell.setCell2X(columncounter);
			 cell.setCellY(rowcounter);
			 cell.setCell2Y(rowcounter);
			 cell.setCellHeight(c.getPreferredSize().height);
			 cell.setCellWidth(InterfaceGuidelines.MARGIN_BETWEEN);
			 utilEdit.addCol(cell);

			 // Component
			 c = ComponentProcessors.getInstance().createComponent(LayoutMLConstants.ELEMENT_COLLECTABLECOMPONENT, controltype, panel.getMetaInformation(), field.getField());
			 ((WYSIWYGComponent)c).getProperties().setProperty(WYSIWYGComponent.PROPERTY_ENABLED, new PropertyValueBoolean(Boolean.FALSE), null);

			 cell = new LayoutCell();
			 cell.setCellX(++columncounter);
			 cell.setCell2X(columncounter);
			 cell.setCellY(rowcounter);
			 cell.setCell2Y(rowcounter);
			 cell.setCellHeight(c.getPreferredSize().height);
			 if(controltype.equals(LayoutMLConstants.CONTROLTYPE_DATECHOOSER))
				 cell.setCellWidth(I_CREATEAT_WIDTH);
			 else
				 cell.setCellWidth(I_CREATEBY_WIDTH);
			 c.setPreferredSize(new Dimension(maxWidth+I_CELL_HEIGHT_EXT, c.getPreferredSize().height));
			 utilEdit.addCol(cell);
			 utilEdit.insertComponentTo((WYSIWYGComponent)c, cell);

			 cell = new LayoutCell();
			 cell.setCellX(++columncounter);
			 cell.setCell2X(columncounter);
			 cell.setCellY(rowcounter);
			 cell.setCell2Y(rowcounter);
			 cell.setCellHeight(c.getPreferredSize().height);
			 cell.setCellWidth(InterfaceGuidelines.DISTANCE_TO_OTHER_OBJECTS);
			 utilEdit.addCol(cell);

			 columncounter++;
		 }
	}
	
	private int buildAttributeGroupsForLayout(TableLayoutUtil util, List<EntityFieldMetaDataVO> fields, int maxWidth, int counter) throws CommonFinderException, CommonPermissionException,	CommonBusinessException {
		Map<Long, String> attributeGroups = getAttributeGroups(); 
		for(Long groupId : attributeGroups.keySet()) {
			 final String sGroup = org.nuclos.common2.StringUtils.emptyIfNull(attributeGroups.get(groupId));

			 WYSIWYGLayoutEditorPanel panelGroup = (WYSIWYGLayoutEditorPanel)ComponentProcessors.getInstance().createComponent(LayoutMLConstants.ELEMENT_PANEL, LayoutMLConstants.ELEMENT_LABEL, new WYSIWYGMetaInformation(), sGroup);
			 TranslationMap tm = new TranslationMap();
			 tm.put("de", sGroup);
			 tm.put("en", sGroup);
			 Border border = new TitledBorderWithTranslations(sGroup, tm);

			 PropertyValueBorder prop = new PropertyValueBorder();
			 prop.setValue(border);


			 panelGroup.setProperty(WYSIWYGComponent.PROPERTY_BORDER, prop, Border.class);

			 createAttributeGroupPanel(panelGroup, groupId, fields);
		  	 LayoutCell cell = new LayoutCell();
			 cell.setCellX(1);
			 cell.setCell2X(2);
			 cell.setCellY(counter);
			 cell.setCell2Y(counter);
			 int iHeight = I_CELL_HEIGHT;
			 iHeight *= getAttributeGroupCount(fields, groupId);

			 cell.setCellHeight(iHeight + I_CELL_HEIGHT_EXT);
			 cell.setCellWidth(maxWidth);
			 panelGroup.setPreferredSize(new Dimension(I_CELL_WIDTH, iHeight*2));
			 util.addRow(cell);
			 if (counter == 1) {
			 	util.addCol(cell);
			 }

			 util.insertComponentTo(panelGroup, cell);
			 counter++;

		 }
		return counter;
	}
	
	private int getAttributeGroupCount(List<EntityFieldMetaDataVO> fields, Long lGroup) {
		int i = 0;
		for(EntityFieldMetaDataVO field : fields) {
			if(lGroup.equals(field.getFieldGroupId()) || (lGroup.longValue() == -1L && field.getFieldGroupId() == null)) {
				i++;
				String controltype = getCollectableComponentType(field);
				if(controltype.equals(LayoutMLConstants.CONTROLTYPE_TEXTAREA))
					i++;
			}
		}

		return i;
	}
	
	private void createAttributeGroupPanel(WYSIWYGLayoutEditorPanel panel, Long lAttributeGroup, List<EntityFieldMetaDataVO> fields) {
		try {
			TableLayoutUtil util = panel.getTableLayoutUtil();
			int counter = 1;
			int maxWidth = I_MAX_WIDTH_GROUP*2;

			List<EntityFieldMetaDataVO> fields1 = new ArrayList<EntityFieldMetaDataVO>();

			for(EntityFieldMetaDataVO field : fields) {
				if(isSystemField(field))
					continue;
				if(field.getForeignEntity() != null && field.getForeignEntityField() == null)
					continue;
				if(lAttributeGroup.longValue() == -1L && field.getFieldGroupId() == null) {

				}
				else if(!lAttributeGroup.equals(field.getFieldGroupId()))
					continue;
				fields1.add(field);
			}
			createComponentForLayout(panel, util, fields1, maxWidth, counter);
		}
		catch(Exception e) {
			LOG.error("createAttributeGroupPanel failed: " + e, e);
		}
	}
	
	private int createComponentForLayout(WYSIWYGLayoutEditorPanel panel, TableLayoutUtil util, List<EntityFieldMetaDataVO> fields, int maxWidth, int counter) throws CommonBusinessException {
		for(EntityFieldMetaDataVO field : fields) {
			 if(isEditField(field)) {
				 continue;
			 }
			 if(isSystemField(field) || isBinaryField(field))
				 continue;
			 if(field.getForeignEntity() != null && field.getForeignEntityField() == null)
				 continue;
			 if(field.getLookupEntity() != null && field.getLookupEntityField() == null)
				 continue;

		  	 String controltype = getCollectableComponentType(field);

			 //label
		  	 Component c = ComponentProcessors.getInstance().createComponent(LayoutMLConstants.ELEMENT_COLLECTABLECOMPONENT, LayoutMLConstants.ELEMENT_LABEL, panel.getMetaInformation(), field.getField());

		  	 LayoutCell cell = new LayoutCell();
			 cell.setCellX(counter);
			 cell.setCellY(counter);
			 int iHeight = c.getPreferredSize().height;
			 if(controltype.equals(LayoutMLConstants.CONTROLTYPE_TEXTAREA) || controltype.equals(LayoutMLConstants.CONTROLTYPE_IMAGE))
				 iHeight *=3;

			 cell.setCellHeight(iHeight);
			 cell.setCellWidth(maxWidth);
			 c.setPreferredSize(new Dimension(maxWidth, iHeight));
			 util.addRow(cell);
			 if (counter == 1) {
			 	util.addCol(cell);
			 	cell = new LayoutCell();
				cell.setCellX(2);
				cell.setCellY(counter);
		 		cell.setCellWidth(maxWidth*2);
				util.addCol(cell);
			 	if(field.getDataType().equals("java.lang.String")) {
					cell = new LayoutCell();
					cell.setCellX(3);
					cell.setCellY(counter);
			 		cell.setCellWidth(maxWidth*2);
					util.addCol(cell);
			 	}
			 	else {
			 		util.addCol(cell);
			 	}
			 }

			 if(controltype.equals(LayoutMLConstants.CONTROLTYPE_TEXTAREA) && !controltype.equals(LayoutMLConstants.CONTROLTYPE_IMAGE))
				 util.insertComponentTo((WYSIWYGComponent)c, new TableLayoutConstraints(1, counter, 1, counter, 2,0));
			 else if (!controltype.equals(LayoutMLConstants.CONTROLTYPE_IMAGE))
				 util.insertComponentTo((WYSIWYGComponent)c, new TableLayoutConstraints(1, counter));
			 //component
			 c = ComponentProcessors.getInstance().createComponent(LayoutMLConstants.ELEMENT_COLLECTABLECOMPONENT, controltype, panel.getMetaInformation(), field.getField());

			 if(controltype.equals(LayoutMLConstants.CONTROLTYPE_TEXTAREA)) {
				 util.insertComponentTo((WYSIWYGComponent)c, new TableLayoutConstraints(2, counter, 4, counter));
				 util.modifyTableLayoutSizes(TableLayout.FILL, false, cell, true, false);
			 }
			 else if(controltype.equals(LayoutMLConstants.CONTROLTYPE_TEXTFIELD)){
				 util.insertComponentTo((WYSIWYGComponent)c, new TableLayoutConstraints(2, counter, 3, counter,2,0));

				 //util.modifyTableLayoutSizes(TableLayout.PREFERRED, true, cell, true, false);
			 }
			 else if(controltype.equals(LayoutMLConstants.CONTROLTYPE_HYPERLINK)){
				 util.insertComponentTo((WYSIWYGComponent)c, new TableLayoutConstraints(2, counter, 3, counter,2,0));

				 //util.modifyTableLayoutSizes(TableLayout.PREFERRED, true, cell, true, false);
			 }
			 else if(controltype.equals(LayoutMLConstants.CONTROLTYPE_EMAIL)){
				 util.insertComponentTo((WYSIWYGComponent)c, new TableLayoutConstraints(2, counter, 3, counter,2,0));

				 //util.modifyTableLayoutSizes(TableLayout.PREFERRED, true, cell, true, false);
			 }
			 else
				 util.insertComponentTo((WYSIWYGComponent)c, new TableLayoutConstraints(2, counter));
			 counter++;
		 }
		return counter;
	}
	
	private LayoutCell createBetweenCell(int counter) {
		LayoutCell cell = new LayoutCell();
		 cell.setCellX(counter);
		 cell.setCellY(counter);
		 cell.setCellHeight(InterfaceGuidelines.MARGIN_BETWEEN);
		 cell.setCellWidth(InterfaceGuidelines.MARGIN_BETWEEN);
		return cell;
	}
	
	private void createDefaultBorder(TableLayoutUtil util) {
		try {
			LayoutCell cell = util.getLayoutCellByPosition(0, util.getNumRows());
			util.delRow(cell);

			LayoutCell cellForEditing = new LayoutCell();
			cellForEditing.setCellX(util.getNumColumns()-1);
			cellForEditing.setCellWidth(InterfaceGuidelines.MARGIN_BETWEEN);
			if (util.getNumColumns() > 0)
				cellForEditing.setCellX(cellForEditing.getCellX() + 1);
			util.addCol(cellForEditing);

			cellForEditing = new LayoutCell();
			cellForEditing.setCellY(util.getNumRows()-1);
			cellForEditing.setCellHeight(InterfaceGuidelines.MARGIN_BETWEEN);
	        if (util.getNumRows() > 0)
				cellForEditing.setCellY(cellForEditing.getCellY() + 1);
			util.addRow(cellForEditing);
		}
		catch(Exception e) {
			// don't create default border when exception
			LOG.info("createDefaultBorder failed: " + e);
		}
	}
	
	private String getCollectableComponentType(EntityFieldMetaDataVO f) {
		
		if(DefaultComponentTypes.EMAIL.equalsIgnoreCase(f.getDefaultComponentType()))
			return LayoutMLConstants.CONTROLTYPE_EMAIL;
		
		if(DefaultComponentTypes.HYPERLINK.equalsIgnoreCase(f.getDefaultComponentType()))
			return LayoutMLConstants.CONTROLTYPE_HYPERLINK;

		if(f.getForeignEntity() != null && !f.isSearchable())
			return LayoutMLConstants.CONTROLTYPE_COMBOBOX;

		if(f.getForeignEntity() != null && f.isSearchable())
			return LayoutMLConstants.CONTROLTYPE_LISTOFVALUES;
		
		if(f.getLookupEntity() != null && !f.isSearchable())
			return LayoutMLConstants.CONTROLTYPE_COMBOBOX;

		if(f.getLookupEntity() != null && f.isSearchable())
			return LayoutMLConstants.CONTROLTYPE_LISTOFVALUES;

		if(Date.class.getName().equals(f.getDataType()) || InternalTimestamp.class.getName().equals(f.getDataType()))
			return LayoutMLConstants.CONTROLTYPE_DATECHOOSER;

		if(Boolean.class.getName().equals(f.getDataType()))
			return LayoutMLConstants.CONTROLTYPE_CHECKBOX;

		if(NuclosImage.class.getName().equals(f.getDataType()))
			return LayoutMLConstants.CONTROLTYPE_IMAGE;

		if(f.getScale() != null && f.getScale().intValue() > 255 || f.getDataType().equals("java.lang.String") && f.getScale() == null)
			return LayoutMLConstants.CONTROLTYPE_TEXTAREA;

		return LayoutMLConstants.CONTROLTYPE_TEXTFIELD;

	}
	
	private List<EntityFieldMetaDataVO> sortEditFields(List<EntityFieldMetaDataVO> lstFields) {
		List<EntityFieldMetaDataVO> lstNewList = new ArrayList<EntityFieldMetaDataVO>();

		for(int i = 0; i < EDIT_FIELDS.length; i++) {
			for(EntityFieldMetaDataVO field : lstFields) {
				String sDBField = field.getDbColumn();
				if(sDBField.equalsIgnoreCase(EDIT_FIELDS[i])){
					lstNewList.add(field);
					break;
				}
			}
		}

		return lstNewList;
	}
	
	private boolean isEditField(EntityFieldMetaDataVO f) {
		for(String s : EDIT_FIELDS) {
			if(s.equals(f.getDbColumn())) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isSystemField(EntityFieldMetaDataVO field) {
		 if(field.getField().equals(NuclosEOField.SYSTEMIDENTIFIER.getMetaData().getField()))
			 return true;
		 if(field.getField().equals(NuclosEOField.PROCESS.getMetaData().getField()))
			 return true;
		 if(field.getField().equals(NuclosEOField.STATE.getMetaData().getField()))
			 return true;
		 if(field.getField().equals(NuclosEOField.STATENUMBER.getMetaData().getField()))
			 return true;
		 if(field.getField().equals(NuclosEOField.STATEICON.getMetaData().getField()))
			 return true;
		 if(field.getField().equals(NuclosEOField.LOGGICALDELETED.getMetaData().getField()))
			 return true;
		 if(field.getField().equals(NuclosEOField.ORIGIN.getMetaData().getField()))
			 return true;
		 return false;
	}
	
	private boolean isBinaryField(EntityFieldMetaDataVO field) {
		if(field.getDataType().equals("[B"))
			return true;
		return false;
	}
}
