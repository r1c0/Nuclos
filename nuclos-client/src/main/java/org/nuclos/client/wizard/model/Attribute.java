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
package org.nuclos.client.wizard.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.nuclos.client.wizard.util.DefaultValue;
import org.nuclos.client.wizard.util.NuclosWizardUtils;
import org.nuclos.common.NuclosScript;
import org.nuclos.common.dal.vo.EntityMetaDataVO;

public class Attribute {

	Long id;
	Long internalId;
	String label;
	String description;
	DataTyp dataTyp;
	String internalName;
	String oldInternalName;
	String dbName;
	boolean mandatory;
	boolean logBook;
	boolean distinct;
	boolean readonly;
	boolean modifiable;
	EntityMetaDataVO metaVO;
	EntityMetaDataVO lookupMetaVO;
	boolean blnValueListProvider;
	String strField;
	String defaultValue;
	DefaultValue idDefaultValue;
	String attributeGroup;
	String calcFunction;
	String sValueListName;
	String outputFormat;
	String sInputValidation;
	List<ValueList> lstValueList;
	boolean blnValueListNew;
	boolean indexed;
	Object mandatoryValue;
	boolean onDeleteCascade;
	NuclosScript calculationScript;

	String labelRes;
	String descriptionRes;

	boolean blnResume;

	boolean blnRemove;

	public static int quantity = 10;

	public Attribute() {

	}

	public boolean hasInternalNameChanged() {
		if(oldInternalName != null && oldInternalName.length() > 0) {
			return !oldInternalName.equals(internalName);
		}
		return false;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
		this.internalId = id;
	}

	public Long getInternalId() {
		return this.internalId;
	}

	public void setInternalId(Long id) {
		this.internalId = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public DataTyp getDatatyp() {
		return dataTyp;
	}

	public void setDatatyp(DataTyp datatyp) {
		this.dataTyp = datatyp;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	public boolean isLogBook() {
		return logBook;
	}

	public void setLogBook(boolean logBook) {
		this.logBook = logBook;
	}

	public boolean isModifiable() {
		return modifiable;
	}

	public void setModifiable(boolean bModifiable) {
		this.modifiable = bModifiable;
	}

	public boolean isRemove() {
		return blnRemove;
	}

	public void setRemove(boolean remove) {
		this.blnRemove = remove;
	}

	public boolean isDistinct() {
		return distinct;
	}

	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
	}

	public String getInternalName() {
		return internalName;
	}

	public void setInternalName(String internalName) {
		this.internalName = internalName;
	}

	public void setOldInternalName(String name) {
		this.oldInternalName = name;
	}

	public String getOldInternalName() {
		return this.oldInternalName;
	}

	public void setDbName(String sName) {
		this.dbName = sName;
	}

	public static String getDBPrefix(Attribute attr) {
		if(attr.getDatatyp().getJavaType().equals("java.lang.String")) {
			return NuclosWizardUtils.COLUMN_STRING_PREFFIX;
		}
		else if(attr.getDatatyp().getJavaType().equals("java.lang.Boolean")) {
			return NuclosWizardUtils.COLUMN_BOOLEAN_PREFFIX;
		}
		else if(attr.getDatatyp().getJavaType().equals("java.lang.Integer")) {
			return NuclosWizardUtils.COLUMN_INTEGER_PREFFIX;
		}
		else if(attr.getDatatyp().getJavaType().equals("java.lang.Double")) {
			return NuclosWizardUtils.COLUMN_DOUBLE_PREFFIX;
		}
		else if(attr.getDatatyp().getJavaType().equals("java.util.Date")) {
			return NuclosWizardUtils.COLUMN_DATE_PREFFIX;
		}
		else if(attr.getDatatyp().getJavaType().equals("org.nuclos.server.genericobject.valueobject.GenericObjectDocumentFile")) {
			return NuclosWizardUtils.COLUMN_STRING_PREFFIX;
		}
		else {
			return NuclosWizardUtils.COLUMN_PREFFIX;
		}
	}

	public String getDbName() {
		if(dbName != null)
			return dbName;
		else {
			if(this.getDatatyp().getJavaType().equals("java.lang.String")) {
				return NuclosWizardUtils.COLUMN_STRING_PREFFIX + internalName.replaceAll(" ", "");
			}
			else if(this.getDatatyp().getJavaType().equals("java.lang.Boolean")) {
				return NuclosWizardUtils.COLUMN_BOOLEAN_PREFFIX + internalName.replaceAll(" ", "");
			}
			else if(this.getDatatyp().getJavaType().equals("java.lang.Integer")) {
				return NuclosWizardUtils.COLUMN_INTEGER_PREFFIX + internalName.replaceAll(" ", "");
			}
			else if(this.getDatatyp().getJavaType().equals("java.lang.Double")) {
				return NuclosWizardUtils.COLUMN_DOUBLE_PREFFIX + internalName.replaceAll(" ", "");
			}
			else if(this.getDatatyp().getJavaType().equals("java.util.Date")) {
				return NuclosWizardUtils.COLUMN_DATE_PREFFIX + internalName.replaceAll(" ", "");
			}
			else if(this.getDatatyp().getJavaType().equals("org.nuclos.server.genericobject.valueobject.GenericObjectDocumentFile")) {
				return NuclosWizardUtils.COLUMN_STRING_PREFFIX + internalName.replaceAll(" ", "");
			}
			else {
				return NuclosWizardUtils.COLUMN_PREFFIX + internalName.replaceAll(" ", "");
			}
		}
	}

	public EntityMetaDataVO getMetaVO() {
		return metaVO;
	}

	public void setMetaVO(EntityMetaDataVO metaVO) {
		this.metaVO = metaVO;
	}

	public EntityMetaDataVO getLookupMetaVO() {
		return lookupMetaVO;
	}

	public void setLookupMetaVO(EntityMetaDataVO metaVO) {
		this.lookupMetaVO = metaVO;
	}

	public String getField() {
		return strField;
	}

	public void setField(String strField) {
		this.strField = strField;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public String toString() {
		return getLabel();
	}





	@Override
	public Object clone() {
		Attribute attr = new Attribute();

		attr.setDatatyp(this.getDatatyp());
		attr.setDbName(this.getDbName());
		attr.setDefaultValue(this.getDefaultValue());
		attr.setIdDefaultValue(this.idDefaultValue);
		attr.setDescription(this.getDescription());
		attr.setDistinct(this.isDistinct());
		attr.setField(this.getField());
		attr.setId(this.getId());
		attr.setInternalName(this.getInternalName());
		attr.setLabel(this.getLabel());
		attr.setLogBook(this.isLogBook());
		attr.setMandatory(this.isMandatory());
		attr.setMetaVO(this.getMetaVO());
		attr.setLookupMetaVO(this.getLookupMetaVO());
		attr.setOldInternalName(this.oldInternalName);
		attr.setRemove(this.isRemove());
		attr.setResume(this.isForResume());
		attr.setValueListProvider(this.isValueListProvider());
		attr.setValueList(this.getValueList());
		attr.setValueListNew(this.isValueListNew());
		attr.setValueListName(this.getValueListName());
		attr.setLabelResource(this.getLabelResource());
		attr.setDescriptionResource(this.getDescriptionResource());
		attr.setAttributeGroup(this.getAttributeGroup());
		attr.setCalcFunction(this.getCalcFunction());
		attr.setInputValidation(this.sInputValidation);
		attr.setIndexed(this.isIndexed());
		attr.setMandatoryValue(this.getMandatoryValue());
		attr.setOnDeleteCascade(this.isOnDeleteCascade());
		attr.setCalculationScript(this.getCalculationScript());

		return attr;
	}

	public boolean isForResume() {
		return blnResume;
	}

	public void setResume(boolean resume) {
		this.blnResume = resume;
	}

	public boolean isValueListProvider() {
		return blnValueListProvider;
	}

	public void setValueListProvider(boolean valuelistprovider) {
		this.blnValueListProvider = valuelistprovider;
	}

	public String getAttributeGroup() {
		return attributeGroup;
	}

	public void setAttributeGroup(String attributeGroup) {
		this.attributeGroup = attributeGroup;
	}

	public String getCalcFunction() {
		return calcFunction;
	}

	public void setCalcFunction(String calcFunction) {
		this.calcFunction = calcFunction;
	}

	public void setValueList(List<ValueList> lstValueList) {
		this.lstValueList = lstValueList;
	}

	public List<ValueList> getValueList() {
		if(lstValueList == null)
			lstValueList = new ArrayList<ValueList>();
		return lstValueList;
	}

	public String getLabelResource() {
		return labelRes;
	}

	public void setLabelResource(String labelRes) {
		this.labelRes = labelRes;
	}

	public String getDescriptionResource() {
		return descriptionRes;
	}

	public void setDescriptionResource(String descriptionRes) {
		this.descriptionRes = descriptionRes;
	}

	public void setValueListName(String sName) {
		this.sValueListName = sName;
	}

	public String getValueListName() {
		return this.sValueListName;
	}

	public boolean isValueList() {
		return this.sValueListName != null;
	}

	public void setValueListNew(boolean blnNew) {
		this.blnValueListNew = blnNew;
	}

	public boolean isValueListNew()  {
		return this.blnValueListNew;
	}

	public String getInputValidation() {
    	return sInputValidation;
    }

	public void setInputValidation(String inputValidation) {
    	this.sInputValidation = inputValidation;
    }

	public void setIdDefaultValue(DefaultValue dv){
		this.idDefaultValue = dv;
	}

	public DefaultValue getIdDefaultValue() {
		return this.idDefaultValue;
	}

	public void setIndexed(boolean indexed) {
		this.indexed = indexed;
	}

	public boolean isIndexed() {
		return indexed;
	}

	public boolean isImage() {
		return this.getDatatyp().getJavaType().equals("org.nuclos.common.NuclosImage");
	}

	public boolean isPasswordField() {
		return this.getDatatyp().getJavaType().equals("org.nuclos.common.NuclosPassword");
	}

	public boolean isFileType() {
		return this.getDatatyp().getJavaType().equals("org.nuclos.server.genericobject.valueobject.GenericObjectDocumentFile");
	}

	public Object getMandatoryValue() {
		return this.mandatoryValue;
	}

	public void setMandatoryValue(Object value) {
		this.mandatoryValue = value;
	}

	public String getOutputFormat() {
		return outputFormat;
	}

	public void setOutputFormat(String outputFormat) {
		this.outputFormat = outputFormat;
	}

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}

	public boolean isOnDeleteCascade() {
		return onDeleteCascade;
	}

	public void setOnDeleteCascade(boolean onDeleteCascade) {
		this.onDeleteCascade = onDeleteCascade;
	}

	public NuclosScript getCalculationScript() {
		return calculationScript;
	}

	public void setCalculationScript(NuclosScript calculationScript) {
		this.calculationScript = calculationScript;
	}

	@Override
	public int hashCode() {
		if(this.internalName != null){
			return this.internalName.hashCode();
		}
		else
			return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Attribute) {
			Attribute that = (Attribute)obj;
			return ObjectUtils.equals(that.getId(), this.getId()) || StringUtils.equals(that.getInternalName(), this.getInternalName());
		}
		return false;
	}
}
