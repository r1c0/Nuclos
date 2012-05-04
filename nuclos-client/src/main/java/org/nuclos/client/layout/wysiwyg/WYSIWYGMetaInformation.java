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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.log4j.Logger;
import org.nuclos.client.attribute.AttributeCache;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.common.NuclosCollectableEntityProvider;
import org.nuclos.client.genericobject.GeneratorActions;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COMMON_LABELS;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.STATIC_BUTTON;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableCheckBox;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableComboBox;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableDateChooser;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableListOfValues;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGStaticButton;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGSubForm;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGSubFormColumn;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGUniversalComponent;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertiesPanel;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValue;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueString;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGValuelistProvider;
import org.nuclos.client.masterdata.MetaDataCache;
import org.nuclos.client.resource.ResourceDelegate;
import org.nuclos.client.rule.RuleDelegate;
import org.nuclos.client.statemodel.StateDelegate;
import org.nuclos.client.ui.collect.component.CollectableComponentType;
import org.nuclos.common.NuclosAttributeNotFoundException;
import org.nuclos.common.NuclosImage;
import org.nuclos.common.NuclosPassword;
import org.nuclos.common.collect.collectable.CollectableComponentTypes;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableEntityProvider;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.genericobject.CollectableGenericObjectEntityField;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.Localizable;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.layoutml.LayoutMLConstants;
import org.nuclos.server.attribute.valueobject.AttributeCVO;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaFieldVO;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.ruleengine.valueobject.RuleEventUsageVO;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
import org.nuclos.server.statemodel.valueobject.StateVO;

/**
 * This class connects the WYSIWYG Editor with the Backbone.
 * It collects all Data for CollectableComponents, Subforms, Columns shown in Subforms and Attributes.
 *
 * Every WYSIWYG Layout depends on set Metainformation. Otherwise collecting of fieldnames and Subformcolumns would not work.
 *
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class WYSIWYGMetaInformation implements LayoutMLConstants {

	private static final Logger LOG = Logger.getLogger(WYSIWYGMetaInformation.class);

	public static final String META_FIELD_NAMES = "meta_field_names";
	public static final String META_ENTITY_NAMES = "meta_entity_names";
	public static final String META_ENTITY_FIELD_NAMES = "meta_entity_field_names";
	public static final String META_ENTITY_FIELD_NAMES_REFERENCING = "meta_entity_field_names_referencing";
	public static final String META_CONTROLTYPE = "meta_controltype";
	public static final String META_SHOWONLY = "meta_showonly";
	public static final String META_ACTIONCOMMAND_PROPERTIES = "meta_actioncommand_properties";
	//NUCLEUSINT-390
	public static final String META_POSSIBLE_PARENT_SUBFORMS = "meta_subforms";
	public static final String META_ICONS = "meta_icons";

	private final Map<String, Integer> mpEnumeratedControlTypes = new HashMap<String, Integer>(5);
	{
		this.mpEnumeratedControlTypes.put(ATTRIBUTEVALUE_TEXTFIELD, CollectableComponentTypes.TYPE_TEXTFIELD);
		//NUCLEUSINT-1142
		this.mpEnumeratedControlTypes.put(ATTRIBUTEVALUE_PASSWORDFIELD, CollectableComponentTypes.TYPE_PASSWORDFIELD);
		this.mpEnumeratedControlTypes.put(ATTRIBUTEVALUE_IDTEXTFIELD, CollectableComponentTypes.TYPE_IDTEXTFIELD);
		this.mpEnumeratedControlTypes.put(ATTRIBUTEVALUE_TEXTAREA, CollectableComponentTypes.TYPE_TEXTAREA);
		this.mpEnumeratedControlTypes.put(ATTRIBUTEVALUE_COMBOBOX, CollectableComponentTypes.TYPE_COMBOBOX);
		this.mpEnumeratedControlTypes.put(ATTRIBUTEVALUE_CHECKBOX, CollectableComponentTypes.TYPE_CHECKBOX);
		this.mpEnumeratedControlTypes.put(ATTRIBUTEVALUE_DATECHOOSER, CollectableComponentTypes.TYPE_DATECHOOSER);
		this.mpEnumeratedControlTypes.put(ATTRIBUTEVALUE_OPTIONGROUP, CollectableComponentTypes.TYPE_OPTIONGROUP);
		this.mpEnumeratedControlTypes.put(ATTRIBUTEVALUE_LISTOFVALUES, CollectableComponentTypes.TYPE_LISTOFVALUES);
		this.mpEnumeratedControlTypes.put(ATTRIBUTEVALUE_FILECHOOSER, CollectableComponentTypes.TYPE_FILECHOOSER);
		this.mpEnumeratedControlTypes.put(ATTRIBUTEVALUE_IMAGE, CollectableComponentTypes.TYPE_IMAGE);
	}

	private CollectableEntity entity;
	
	private Collection<RuleVO> collRules;
	private Collection<StateVO> collStates;
	private Collection<GeneratorActionVO> collGenerators;

	/** used for collecting the informations */
	private CollectableEntityProvider provider = NuclosCollectableEntityProvider.getInstance();

	/**
	 * Setting the CollectableEntity for this Metainformation.
	 * Without a set CollectableEntity the Metainformation is not correctly initialized.
	 * @param entity
	 */
	public void setCollectableEntity(CollectableEntity entity) {
		this.entity = entity;
		
		this.collRules = null;
		this.collStates = null;
		this.collGenerators = null;
	}

	/**
	 * Method returning the fitting Entity for an attribute
	 *
	 * @param attribute
	 * @return
	 * @throws NuclosAttributeNotFoundException
	 */
	public String getLinkedEntityForAttribute(String entity, String attribute) throws NuclosAttributeNotFoundException{
		//FIX NUCLEUSINT-342
		provider.isEntityDisplayable(entity == null ? this.entity.getName() : entity);

		//FIX NUCLOSINT-864
		//AttributeCVO attributeVo = AttributeCache.getInstance().getAttribute(entity == null ? this.entity.getName() : entity, attribute);
		//return attributeVo.getExternalEntity();
		EntityFieldMetaDataVO efMeta = MetaDataClientProvider.getInstance().getEntityField(entity == null ? this.entity.getName() : entity, attribute);
		return efMeta.getForeignEntity() != null ? efMeta.getForeignEntity() : efMeta.getLookupEntity();
	}

	/**
	 *
	 * @param entity
	 * @param subformColumn
	 * @return
	 * @throws NuclosAttributeNotFoundException
	 * NUCLEUSINT-341
	 */
	public String getLinkedEntityForSubformColumn(String entity, String subformColumn) throws NuclosAttributeNotFoundException{
		CollectableEntityField column = null;
		CollectableEntity e = null;

		e = provider.getCollectableEntity(entity);
		if (e != null)
			column = e.getEntityField(subformColumn);
		if (column != null)
		return column.getReferencedEntityName();

		return null;
	}

	/**
	 * Gets the {@link CollectableComponentType} for a Subform Column
	 *
	 * @param entity the Entity of the Subform
	 * @param subformColumn the Subform Column which type should be found
	 * @return {@link CollectableComponentTypes}
	 */
	public int getTypeOfSubformField(String entity, String subformColumn) {
		CollectableEntityField column = null;
		CollectableEntity e = null;

		e = provider.getCollectableEntity(entity);
		if (e != null)
			column = e.getEntityField(subformColumn);
		if (column != null)
			return column.getFieldType();

		return -1;
	}

	/**
	 *
	 * @param attribute
	 * @return
	 * @throws CommonFinderException
	 */
	public List<String> getDependingAttributes(String attribute) {
		CollectableEntity temp = null;
		try {
			temp = provider.getCollectableEntity(attribute);
		} catch (Exception e) {
		}
		List<String> fieldNames = null;

		if (temp != null)
			fieldNames = sortValues(new ArrayList<String>(temp.getFieldNames()));
		return fieldNames;
	}

	/**
	 * Simple getter Method returning the stored CollectableEntity
	 * @return
	 */
	public CollectableEntity getCollectableEntity() {
		return this.entity;
	}

	/**
	 * Returns the Java Class for a Attribute
	 * @param attributeName
	 * @return {@link Class} of found {@link AttributeCVO}, else null
	 * NUCLEUSINT-811
	 */
	public Class<?> getDatatypeForAttribute(String attributeName) {
		return getCollectableEntity().getEntityField(attributeName).getJavaClass();
	}

	/**
	 * Method collecting different Kinds of Data.
	 *
	 *
	 * @param c
	 * @param meta
	 * @param dialog
	 * @return
	 */
	public List<StringResourceIdPair> getListOfMetaValues(WYSIWYGComponent c, String[] valueFromMeta, PropertiesPanel dialog) {
		List<StringResourceIdPair> result = new ArrayList<StringResourceIdPair>();
		String prop = valueFromMeta[0];
		String meta = valueFromMeta[1];
		if (META_FIELD_NAMES.equals(meta)) {
			if (prop.equals(WYSIWYGComponent.PROPERTY_NEXTFOCUSCOMPONENT))
				result = getFittingFieldnames();
			else
				result = getFittingFieldnamesForControlType(c);
		} else if (META_ENTITY_NAMES.equals(meta)) {
			Collection<MasterDataMetaVO> entities = MetaDataCache.getInstance().getMetaData();
			Set<String> allUsedEntities = new HashSet<String>(c.getParentEditor().getMainEditorPanel().getSubFormEntityNames());
			if (c instanceof WYSIWYGSubForm) {
				String currentUsedEntity = ((WYSIWYGSubForm)c).getEntityName();
				allUsedEntities.remove(currentUsedEntity); // null is ok for a HashSet
			}
			for (MasterDataMetaVO entity : entities) {
				if (!allUsedEntities.contains(entity.getEntityName()))
					result.add(new StringResourceIdPair(entity.getEntityName(), entity.getResourceId()));
			}
		} else if (META_ENTITY_FIELD_NAMES.equals(meta)) {
			if (c instanceof WYSIWYGSubForm) {
				String e = ((PropertyValueString)dialog.getModel().getValueAt(getDialogTableModelPropertyRowIndex(dialog, WYSIWYGSubForm.PROPERTY_ENTITY), 1)).getValue();
				if (!StringUtils.isNullOrEmpty(e)) {
					for (String s : provider.getCollectableEntity(e).getFieldNames()) {
						result.add(new StringResourceIdPair(s, null));
					}
				}
			}
		} else if (META_ENTITY_FIELD_NAMES_REFERENCING.equals(meta)) {
			if (c instanceof WYSIWYGSubForm) {
				String e = ((PropertyValueString)dialog.getModel().getValueAt(getDialogTableModelPropertyRowIndex(dialog, WYSIWYGSubForm.PROPERTY_ENTITY), 1)).getValue();
				if (!StringUtils.isNullOrEmpty(e)) {
					for (String s : provider.getCollectableEntity(e).getFieldNames()) {
						if (provider.getCollectableEntity(e).getEntityField(s).isReferencing()) {
							result.add(new StringResourceIdPair(s, null));
						}
					}
					//should be possible to unset the unique mastercolum
					result.add(new StringResourceIdPair("", null));
				}
			}
		} else if (META_CONTROLTYPE.equals(meta)) {
			result = makeStringsWithNullResourceId(getValidControlTypesForCollectableComponent(c, dialog));
		} else if (META_SHOWONLY.equals(meta)) {
			result = makeStringsWithNullResourceId(getShowOnlyTag(dialog,c));
		} else if (META_POSSIBLE_PARENT_SUBFORMS.equals(meta)) {
			//NUCLEUSINT-390
			WYSIWYGLayoutEditorPanel mainPanel = c.getParentEditor().getMainEditorPanel();
			List<Object> subforms = new ArrayList<Object>();
			c.getParentEditor().getWYSIWYGComponents(WYSIWYGSubForm.class, mainPanel, subforms);
			String ownEntity = ((WYSIWYGSubForm)c).getEntityName();
			String entity = null;
			for (Object subformFromPanel : subforms) {
				entity = ((WYSIWYGSubForm) subformFromPanel).getEntityName();
				//filter the own entity, only other subforms are valid entries
				if (ownEntity == null)
					result.add(new StringResourceIdPair(((WYSIWYGSubForm) subformFromPanel).getEntityName(), null));
				else if (!ownEntity.equals(entity))
					result.add(new StringResourceIdPair(((WYSIWYGSubForm) subformFromPanel).getEntityName(), null));
			}
			// should be possible to deselect a parent subform
			result.add(new StringResourceIdPair("", null));
		} else if (META_ACTIONCOMMAND_PROPERTIES.equals(meta)) {
			PropertyValue propActionCommand = c.getProperties().getProperty(WYSIWYGStaticButton.PROPERTY_ACTIONCOMMAND);
			if (propActionCommand != null) {
				String actionCommand = ((PropertyValueString)dialog.getModel().getValueAt(getDialogTableModelPropertyRowIndex(dialog, WYSIWYGStaticButton.PROPERTY_ACTIONCOMMAND), 1)).getValue();
				if (actionCommand != null) {
					if (STATIC_BUTTON.DUMMY_BUTTON_ACTION_LABEL.equals(actionCommand))
						; // do nothing
					else if (STATIC_BUTTON.STATE_CHANGE_ACTION_LABEL.equals(actionCommand)) {
						Collection<StateVO> collStates = getStates();
						for (StateVO state: collStates) {
							result.add(new StringResourceIdPair(state.getNumeral().toString(), null));
						}
					} else if (STATIC_BUTTON.EXECUTE_RULE_ACTION_LABEL.equals(actionCommand)) {
						//NUCLOSINT-743 get all the user driven rules for this entity
						Collection<RuleVO> collRules = getRules();
						for (RuleVO rule: collRules) {
							result.add(new StringResourceIdPair(rule.getRule(), null));
						}		
					} else if (STATIC_BUTTON.GENERATOR_ACTION_LABEL.equals(actionCommand)) {
						Collection<GeneratorActionVO> collGenerators = getGeneratorActions();
						for (GeneratorActionVO generator: collGenerators) {
							result.add(new StringResourceIdPair(generator.getName(), null));
						}
					} 
				}
			}} else if (META_ICONS.equals(meta)) {
			result.addAll(CollectionUtils.transform(ResourceDelegate.getInstance().getIconResources(), new Transformer<String, StringResourceIdPair>() {
				@Override
				public StringResourceIdPair transform(String i) {
					return new StringResourceIdPair(i, null);
				}
			}));
		}

		return result;
	}
	
	private Collection<RuleVO> getRules() {
		if (this.collRules == null) {
			//NUCLOSINT-743 get all the user driven rules for this entity
			this.collRules = RuleDelegate.getInstance().getByEventAndEntityOrdered(RuleEventUsageVO.USER_EVENT, getCollectableEntity().getName());
			// remove inactive rules
			CollectionUtils.removeAll(collRules, new Predicate<RuleVO>() {
				@Override
	            public boolean evaluate(RuleVO rulevo) {
					return !rulevo.isActive();
				}
			});
		}
		return collRules;
	}
	
	private Collection<StateVO> getStates() {
		if (this.collStates == null) {
			if (Modules.getInstance().existModule(getCollectableEntity().getName())) {
				Integer iModuleId = Modules.getInstance().getModuleByEntityName(getCollectableEntity().getName()).getIntId();
				this.collStates = StateDelegate.getInstance().getStatesByModule(iModuleId);
			} else {
				this.collStates = Collections.emptyList();
			}
		}
		return collStates;
	}

	private Collection<GeneratorActionVO> getGeneratorActions() {
		if (this.collGenerators == null) {
			final Integer iModuleId;
			if (Modules.getInstance().existModule(getCollectableEntity().getName())) 
				iModuleId = Modules.getInstance().getModuleByEntityName(getCollectableEntity().getName()).getIntId();
			else
				iModuleId = IdUtils.unsafeToId(MetaDataClientProvider.getInstance().getEntity(getCollectableEntity().getName()).getId());
			collGenerators = GeneratorActions.getGeneratorActions(iModuleId);
		}
		return collGenerators;
	}

	/**
	 * Externalized Method called by public List<String> getListOfMetaValues(WYSIWYGComponent c, String meta, PropertiesDialog dialog)
	 *
	 * Gets values to display in the show-only combobox in the PropertiesDialog
	 * @param dialog
	 * @return
	 */
	private List<String> getShowOnlyTag(PropertiesPanel dialog, WYSIWYGComponent c){
		List<String> result = new ArrayList<String>();
		result.add(COMMON_LABELS.EMPTY);
		result.add(ATTRIBUTEVALUE_LABEL);
		result.add(ATTRIBUTEVALUE_CONTROL);

		String fieldname;
		int modelIndex = getDialogTableModelPropertyRowIndex(dialog, WYSIWYGUniversalComponent.PROPERTY_NAME);
		if (modelIndex != -1) {
			fieldname = ((PropertyValue<String>)dialog.getModel().getValueAt(modelIndex, 1)).getValue();
		}
		else {
			fieldname = ((PropertyValue<String>)c.getProperties().getProperty(WYSIWYGUniversalComponent.PROPERTY_NAME)).getValue();
		}

		if (!StringUtils.isNullOrEmpty(fieldname)){
			if (getEntityField(entity.getName(), fieldname).getDefaultCollectableComponentType() == CollectableComponentTypes.TYPE_LISTOFVALUES) {
				result.add(ATTRIBUTEVALUE_BROWSEBUTTON);
			}
		}
		return result;
	}

	/**
	 * Externalized Method called by public List<String> getListOfMetaValues(WYSIWYGComponent c, String meta, PropertiesDialog dialog)
	 * Collects fitting ControlTypes for choosing in PropertiesDialog.
	 * @param c
	 * @param dialog
	 * @return List<String> with all fitting ControlTypes found
	 */
	private List<String> getValidControlTypesForCollectableComponent(WYSIWYGComponent c, PropertiesPanel dialog){
		List<String> result = new ArrayList<String>();
		String fieldname = null;
		String entityname = entity.getName();
		result.add(COMMON_LABELS.EMPTY);
		if (c instanceof WYSIWYGSubFormColumn) {
			entityname = ((WYSIWYGSubFormColumn)c).getSubForm().getEntityName();
			fieldname = ((WYSIWYGSubFormColumn)c).getEntityField().getName();
		}
		else if (c instanceof WYSIWYGUniversalComponent) {
			fieldname = ((PropertyValue<String>)dialog.getModel().getValueAt(getDialogTableModelPropertyRowIndex(dialog, WYSIWYGUniversalComponent.PROPERTY_NAME), 1)).getValue();
		}
		if (!StringUtils.isNullOrEmpty(fieldname)) {
			CollectableEntityField field = getEntityField(entityname, fieldname);
			switch (field.getDefaultCollectableComponentType()) {
				case CollectableComponentTypes.TYPE_COMBOBOX:
				case CollectableComponentTypes.TYPE_LISTOFVALUES:
				case CollectableComponentTypes.TYPE_IDTEXTFIELD:
					result.add(ATTRIBUTEVALUE_COMBOBOX);
					result.add(ATTRIBUTEVALUE_LISTOFVALUES);
					result.add(ATTRIBUTEVALUE_IDTEXTFIELD);
					break;
				case CollectableComponentTypes.TYPE_TEXTAREA:
				case CollectableComponentTypes.TYPE_TEXTFIELD:
					result.add(ATTRIBUTEVALUE_TEXTFIELD);
					result.add(ATTRIBUTEVALUE_TEXTAREA);
					break;
				case CollectableComponentTypes.TYPE_CHECKBOX:
					result.add(ATTRIBUTEVALUE_CHECKBOX);
					break;
				case CollectableComponentTypes.TYPE_DATECHOOSER:
					result.add(ATTRIBUTEVALUE_DATECHOOSER);
					break;
				case CollectableComponentTypes.TYPE_OPTIONGROUP:
					result.add(ATTRIBUTEVALUE_OPTIONGROUP);
					break;
				case CollectableComponentTypes.TYPE_FILECHOOSER:
					result.add(ATTRIBUTEVALUE_FILECHOOSER);
					break;
				case CollectableComponentTypes.TYPE_IMAGE:
					result.add(ATTRIBUTEVALUE_IMAGE);
					break;
				case CollectableComponentTypes.TYPE_PASSWORDFIELD:
					//NUCLEUSINT-1142
					result.add(ATTRIBUTEVALUE_PASSWORDFIELD);
					break;

			}
		}

		//NUCLEUSINT-429 if valuelist provider defined for subform colum a checkbox is also valid
		if (c instanceof WYSIWYGSubFormColumn){
			WYSIWYGValuelistProvider wysiwygStaticValuelistProvider = (WYSIWYGValuelistProvider) c.getProperties().getProperty(WYSIWYGSubFormColumn.PROPERTY_VALUELISTPROVIDER).getValue();
			if (wysiwygStaticValuelistProvider != null)
				if (!StringUtils.isNullOrEmpty(wysiwygStaticValuelistProvider.getType())){
					if (!result.contains(ATTRIBUTEVALUE_COMBOBOX))
						result.add(ATTRIBUTEVALUE_COMBOBOX);
				}

		}
		return sortValues(result);
	}


	/**
	 * Externalized Method called by public List<String> getListOfMetaValues(WYSIWYGComponent c, String meta, PropertiesDialog dialog)
	 * Performs a lookup on fitting Fields for a ControlType
	 *
	 * @param c
	 * @return
	 */
	private List<StringResourceIdPair> getFittingFieldnames(){
		List<StringResourceIdPair> result = new ArrayList<StringResourceIdPair>();
 		Integer iModuleId;
		try {
			iModuleId = Modules.getInstance().getModuleIdByEntityName(entity.getName());
		}
		catch (NoSuchElementException ex) {
			iModuleId = null;
		}

		if (iModuleId != null) {
			for (AttributeCVO a : AttributeCache.getInstance().getAttributes()) {
				result.add(new StringResourceIdPair(a.getName(), a.getResourceSIdForLabel()));
			}
		}
		else {
			for (String s : getEntityFieldNames(entity.getName())) {//entity.getFieldNames()) {
				result.add(new StringResourceIdPair(s, null));
			}
		}

		return result;
	}

	/**
	 * Externalized Method called by public List<String> getListOfMetaValues(WYSIWYGComponent c, String meta, PropertiesDialog dialog)
	 * Performs a lookup on fitting Fields for a ControlType
	 *
	 * @param c
	 * @return
	 */
	private List<StringResourceIdPair> getFittingFieldnamesForControlType(WYSIWYGComponent c){
		List<StringResourceIdPair> result = new ArrayList<StringResourceIdPair>();
 		Integer iModuleId;
		try {
			iModuleId = Modules.getInstance().getModuleIdByEntityName(entity.getName());
		}
		catch (NoSuchElementException ex) {
			iModuleId = null;
		}

		if (iModuleId != null) {
			for (AttributeCVO a : AttributeCache.getInstance().getAttributes()) {
				if (c instanceof WYSIWYGCollectableComboBox) {
					if (a.isIdField()) {
						result.add(new StringResourceIdPair(a.getName(), a.getResourceSIdForLabel()));
					}
				}
				else if (c instanceof WYSIWYGCollectableListOfValues) {
					if (a.getExternalEntity() != null) {
						result.add(new StringResourceIdPair(a.getName(), a.getResourceSIdForLabel()));
					}
				}
				else if (c instanceof WYSIWYGCollectableDateChooser) {
					if (Date.class.isAssignableFrom(a.getJavaClass())) {
						result.add(new StringResourceIdPair(a.getName(), a.getResourceSIdForLabel()));
					}
				}
				else if (c instanceof WYSIWYGCollectableCheckBox) {
					if (Boolean.class.isAssignableFrom(a.getJavaClass())) {
						result.add(new StringResourceIdPair(a.getName(), a.getResourceSIdForLabel()));
					}
				}
				else {
					result.add(new StringResourceIdPair(a.getName(), a.getResourceSIdForLabel()));
				}
			}
		}
		else {
			for (String s : getEntityFieldNames(entity.getName())) {//entity.getFieldNames()) {
				result.add(new StringResourceIdPair(s, null));
			}
		}

		return result;
	}

	private Set<String> getEntityFieldNames(String sEntity) {
		return MetaDataCache.getInstance().getMetaData(sEntity).getFieldNames();
	}

	/**
	 * Small HelperMethod sorting the Metainfomation
	 * NUCLEUSINT-384
	 * @param incoming the {@link List} to sort
	 * @return the sorted List (same as incoming)
	 */
	private List<String> sortValues(List<String> incoming) {
		Collections.sort(incoming, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				if (o1 != null && o2 != null)
					return o1.compareToIgnoreCase(o2);
				return 0;
			}
		});
		return incoming;
	}

	/**
	 * Method for checking if the Metainformation is correctly initialized.
	 * Without a set CollectableEntity it is not initialized.
	 * @return
	 */
	public synchronized boolean isMetaInformationSet(){
		if (entity != null) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * This Method returns the Label for a CollectableComponent
	 * @param name the CollectableComponents name
	 * @return
	 */
	public synchronized String getLabelForCollectableComponent(String name) {
		if (name != null && entity != null){
			if (getEntityFieldNames(entity.getName()).contains(name)) {
				MasterDataMetaFieldVO metafieldvo = MetaDataCache.getInstance().getMetaData(entity.getName()).getField(name);
				//String label = entity.getEntityField(name).getLabel();
				return SpringLocaleDelegate.getInstance().getResource(
						metafieldvo.getResourceSIdForLabel(), metafieldvo.getLabel());
				//entity.getEntityField(name).getLabel();
			}
			else {
				//NUCLEUSINT-388
				for (AttributeCVO a : AttributeCache.getInstance().getAttributes()) {
					if (a.getName().equals(name)) {
						return SpringLocaleDelegate.getInstance().getLabelFromAttributeCVO(a);
					}
				}
				return name;
			}
		}
		return name;
	}

	/**
	 * returns the SubformColumns for a SubformEntity
	 * @param entity
	 * @return
	 */
	public synchronized List<String> getSubFormColumns(String entity){
		List<String> result = new ArrayList<String>();

		if (!StringUtils.isNullOrEmpty(entity)) {
			CollectableEntity e = provider.getCollectableEntity(entity);
			for (String s : e.getFieldNames()) {
				result.add(s);
			}
		}

		return sortValues(result);
	}

	public CollectableEntityField getEntityField(String entityname, String fieldname) {
		if (StringUtils.isNullOrEmpty(entityname)) {
			return null;
		}
		else if (Modules.getInstance().isModuleEntity(entityname)) {
			return new CollectableGenericObjectEntityField(
				AttributeCache.getInstance().getAttribute(entityname, fieldname),
				MetaDataClientProvider.getInstance().getEntityField(entityname, fieldname),
				entityname);
		}
		else {
			return provider.getCollectableEntity(entityname).getEntityField(fieldname);
		}
	}

	public List<String> getFieldNamesByControlType(String controlType) {
		List<String> result = new ArrayList<String>();
		Integer iModuleId;
		try {
			iModuleId = Modules.getInstance().getModuleIdByEntityName(entity.getName());
		}
		catch (NoSuchElementException ex) {
			iModuleId = null;
		}

		if (iModuleId != null) {
			try {
				for (EntityFieldMetaDataVO efMeta : MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(entity.getName()).values()) {
					if (ATTRIBUTEVALUE_COMBOBOX.equals(controlType)) {
						if (efMeta.getForeignEntity()!=null) {
							result.add(efMeta.getField());
						}
						if (efMeta.getLookupEntity() != null) {
							result.add(efMeta.getField());
						}
					}
					else if (ATTRIBUTEVALUE_LISTOFVALUES.equals(controlType)) {
						if (efMeta.getForeignEntity()!=null) {
							result.add(efMeta.getField());
						}
						if (efMeta.getLookupEntity() != null) {
							result.add(efMeta.getField());
						}
					}
					else if (ATTRIBUTEVALUE_DATECHOOSER.equals(controlType)) {
						if (Date.class.isAssignableFrom(Class.forName(efMeta.getDataType()))) {
							result.add(efMeta.getField());
						}
					}
					else if (ATTRIBUTEVALUE_CHECKBOX.equals(controlType)) {
						if (Boolean.class.isAssignableFrom(Class.forName(efMeta.getDataType()))) {
							result.add(efMeta.getField());
						}
					}
					else if(ATTRIBUTEVALUE_IMAGE.equals(controlType)) {
						if(NuclosImage.class.isAssignableFrom(Class.forName(efMeta.getDataType()))) {
							result.add(efMeta.getField());
						}
					}
					else if(ATTRIBUTEVALUE_PASSWORDFIELD.equals(controlType)) {
						//NUCLEUSINT-1142
						if(NuclosPassword.class.isAssignableFrom(Class.forName(efMeta.getDataType()))) {
							result.add(efMeta.getField());
						}
					}
					else {
						result.add(efMeta.getField());
					}
				}
			} catch(ClassNotFoundException e) {
				throw new CommonFatalException(e);
			}
		}
		else {
			for (MasterDataMetaFieldVO f : MetaDataCache.getInstance().getMetaData(entity.getName()).getFields()) {
				if (ATTRIBUTEVALUE_COMBOBOX.equals(controlType)) {
					if (f.getForeignEntity() != null && !f.isSearchable()) {
						result.add(f.getFieldName());
					}
					if (f.getLookupEntity() != null && !f.isSearchable()) {
						result.add(f.getFieldName());
					}
				}
				else if (ATTRIBUTEVALUE_LISTOFVALUES.equals(controlType)) {
					if (f.getForeignEntity() != null && f.isSearchable()) {
						result.add(f.getFieldName());
					}
					if (f.getLookupEntity() != null && f.isSearchable()) {
						result.add(f.getFieldName());
					}
				}
				else if (ATTRIBUTEVALUE_DATECHOOSER.equals(controlType)) {
					if (Date.class.isAssignableFrom(f.getJavaClass())) {
						result.add(f.getFieldName());
					}
				}
				else if (ATTRIBUTEVALUE_CHECKBOX.equals(controlType)) {
					if (Boolean.class.isAssignableFrom(f.getJavaClass())) {
						result.add(f.getFieldName());
					}
				}
				else {
					result.add(f.getFieldName());
				}
			}
		}

		return sortValues(result);
	}

	/**
	 * Find the table model row index for a certain property
	 *
	 * @param dialog The current property dialog
	 * @param property The name of the property
	 * @return the index of a certain property, -1 if property was not found in table model
	 */
	private static int getDialogTableModelPropertyRowIndex(PropertiesPanel dialog, String property) {
		for (int i = 0; i < dialog.getModel().getRowCount(); i++) {
			if (((String)dialog.getModel().getValueAt(i, 0)).equals(property)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Maps the displayed controltype to the int that is used internal
	 * @param attributeValue
	 * @return
	 */
	public Integer getCollectableComponentType(String attributeValue) {
		return mpEnumeratedControlTypes.get(attributeValue);
	}

	public static List<StringResourceIdPair> makeStringsWithNullResourceId(Collection<String> values) {
		return CollectionUtils.transform(values, new Transformer<String, StringResourceIdPair>() {
			@Override
			public StringResourceIdPair transform(String value) {
				return new StringResourceIdPair(value, null);
			}
		});
	}

	/**
	 * A wrapper class for a string value with an associated resource id (x is the string value,
	 * y is the resource id or null).
	 */
	public static class StringResourceIdPair extends Pair<String, String> implements Localizable, Serializable {

		public StringResourceIdPair(String value, String resourceId) {
			super(value, resourceId);
		}

		@Override
		public String getResourceId() {
			return this.y;
		}
	}
}
