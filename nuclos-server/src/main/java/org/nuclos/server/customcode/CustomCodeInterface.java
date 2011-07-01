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
package org.nuclos.server.customcode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.attribute.DynamicAttributeVO;
import org.nuclos.common.caching.OneEntrySoftCache;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIsNullCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableLikeCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.attribute.valueobject.AttributeCVO;
import org.nuclos.server.attribute.valueobject.AttributeCVO.DataType;
import org.nuclos.server.common.AttributeCache;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.ModuleConstants;
import org.nuclos.server.common.ooxml.ExcelReader;
import org.nuclos.server.common.ooxml.WordXMLReader;
import org.nuclos.server.genericobject.Modules;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaFieldVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.ruleengine.NuclosFatalRuleException;
import org.nuclos.server.ruleengine.ejb3.RuleInterfaceFacadeLocal;

/**
 * Abstract class which defines the base API accessible by the customer.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Lars.Rueckemann@novabit.de">Lars Rueckemann</a>
 * @version 01.00.00
 */
public abstract class CustomCodeInterface {
	protected static final Logger logger = Logger.getLogger(CustomCodeInterface.class);

	protected RuleInterfaceFacadeLocal ruleInterface;

	protected abstract GenericObjectVO getGenericObject();
	protected abstract MasterDataVO getMasterData();

	private OneEntrySoftCache<File, ExcelReader> excelReader = new OneEntrySoftCache<File, ExcelReader>();

	/**
	 * @return the <code>RuleInterfaceLocal</code>
	 */
	protected RuleInterfaceFacadeLocal getRuleInterface() {
		if (ruleInterface == null)
			ruleInterface = ServiceLocator.getInstance().getFacade(RuleInterfaceFacadeLocal.class);
		return ruleInterface;
	}

	/**
	 * check the state of leased object with intid <code>iGenericObjectId</code>
	 * @param iGenericObjectId leased object vo
	 * @param iNumeral state numeral to compare to
	 * @return true or false
	 * @deprecated
	 */
	@Deprecated
	public boolean isStateEqual(Integer iGenericObjectId, int iNumeral) throws CommonFinderException {
		return this.isStateEqual(Modules.getInstance().getEntityNameByModuleId(this.getRuleInterface().getModuleId(iGenericObjectId)),iGenericObjectId, iNumeral);
	}

	/**
	 * check the state of generic object with intid <code>iObjectId</code>
	 * @param iObjectId generic object vo
	 * @param iNumeral state numeral to compare to
	 * @return true or false, for masterdata throw <code>NuclosFatalException</code>
	 */
	public boolean isStateEqual(String sEntityName, Integer iObjectId, int iNumeral) throws CommonFinderException {
		if (!Modules.getInstance().isModuleEntity(sEntityName)) {
			throw new NuclosFatalException("code.interface.exception.3");//"Stammdaten haben kein Statusmodell.");
		}
		return getRuleInterface().isStateEqual(iObjectId, iNumeral);
	}

	/**
	 * check the state of the current lesed object
	 * @param iStateNumeral
	 * @return Is the state numeral of the current leased object equal to the given state numeral?
	 */
	public boolean isStateEqual(int iStateNumeral) {
		if (this.getMasterData() != null) {
			throw new NuclosFatalException("code.interface.exception.3");//"Stammdaten haben kein Statusmodell.");
		}
		final DynamicAttributeVO attrvoCurrentStateNumeral = this.getGenericObject().getAttribute(NuclosEOField.STATENUMBER.getMetaData().getId().intValue());

		final boolean result;
		if (attrvoCurrentStateNumeral == null || attrvoCurrentStateNumeral.isRemoved() || attrvoCurrentStateNumeral.getValue() == null)
		{
			result = false;
		}
		else {
			final int iCurrentStateNumeral = (Integer) attrvoCurrentStateNumeral.getValue();
			result = (iStateNumeral == iCurrentStateNumeral);
		}
		return result;
	}

	/**
	 * get an attribute value for the current leased object.
	 * @param sAttributeName attribute name of attribute to get
	 * @return attribute value
	 * @precondition sAttributeName != null
	 * @deprecated
	 */
	@Deprecated
	public Object getAttributeValue(String sAttributeName) {
		return this.getFieldValue(sAttributeName);
	}

	public Object getFieldValue(String sFieldName) {
		if (sFieldName == null) {
			throw new NullArgumentException("sFieldName");
		}

		if (this.getGenericObject() != null) {
			if (AttributeCache.getInstance().getAttribute(this.getGenericObject().getModuleId(), sFieldName).isCalculated()) {
				try {
					return getCalculatedAttributeValue(getGenericObjectId(), sFieldName, null);
				}
				catch (NuclosBusinessRuleException e) {
					this.error(e);
					return null;
				}
			}
			else {
				return this.getAttribute(sFieldName).getValue();
			}
		}
		else {
			return this.getMasterData().getField(sFieldName);
		}
	}

	public Object getFieldValue(String sEntityName, String sFieldName) {
		if (sFieldName == null) {
			throw new NullArgumentException("sFieldName");
		}

		if (this.getGenericObject() != null) {
			if (AttributeCache.getInstance().getAttribute(this.getGenericObject().getModuleId(), sFieldName).isCalculated()) {
				try {
					return getCalculatedAttributeValue(getGenericObjectId(), sFieldName, null);
				}
				catch (NuclosBusinessRuleException e) {
					this.error(e);
					return null;
				}
			}
			else {
				return this.getAttribute(sEntityName, sFieldName).getValue();
			}
		}
		else {
			return this.getMasterData().getField(sFieldName);
		}
	}


	/**
	 * get an attribute for the current leased object
	 * @param sAttributeName attribute name of attribute to get
	 * @return attribute
	 * @precondition sAttributeName != null
	 * @postcondition result != null
	 * @postcondition !result.isRemoved()
	 */
	public DynamicAttributeVO getAttribute(String sAttributeName) {
		if (sAttributeName == null) {
			throw new NullArgumentException("sAttributeName");
		}
		final Integer iAttributeId = MetaDataServerProvider.getInstance().getEntityField(MetaDataServerProvider.getInstance().getEntity(LangUtils.convertId(getGenericObject().getModuleId())).getEntity(), sAttributeName).getId().intValue();

		final DynamicAttributeVO dynamicAttribute = this.getGenericObject().getAttribute(iAttributeId);
		final DynamicAttributeVO result = (dynamicAttribute == null || dynamicAttribute.isRemoved()) ? new DynamicAttributeVO(iAttributeId, null, null) : dynamicAttribute;

		assert result != null;
		assert !result.isRemoved();
		return result;
	}

	public DynamicAttributeVO getAttribute(String sEntityName, String sAttributeName) {
		if (sAttributeName == null) {
			throw new NullArgumentException("sAttributeName");
		}
		final Integer iAttributeId = LangUtils.convertId(MetaDataServerProvider.getInstance().getEntityField(sEntityName, sAttributeName).getId());
			//getAttributeId(sAttributeName);

		final DynamicAttributeVO dynamicAttribute = this.getGenericObject().getAttribute(iAttributeId);
		final DynamicAttributeVO result = (dynamicAttribute == null || dynamicAttribute.isRemoved()) ? new DynamicAttributeVO(iAttributeId, null, null) : dynamicAttribute;

		assert result != null;
		assert !result.isRemoved();
		return result;
	}

	public MasterDataMetaFieldVO getField(String sEntityName, String sFieldName) {
		if (sFieldName == null) {
			throw new NullArgumentException("sFieldName");
		}

		MasterDataMetaFieldVO fieldVO = MasterDataMetaCache.getInstance().getMetaData(sEntityName).getField(sFieldName);

		assert !this.getMasterData().isRemoved();

		return fieldVO;


	}

	protected static Integer getFieldId(String sEntityName, String sFieldName) {
		if (sEntityName == null) {
			return getAttributeProvider().getAttribute(sEntityName, sFieldName).getId();
		}
		else {
			return MasterDataMetaCache.getInstance().getMetaData(sEntityName).getField(sFieldName).getId();
		}
	}

	/**
	 * get an attribute for leased object with intid <code>iGenericObjectId</code>
	 * @param sAttributeName attribute name of attribute to get
	 * @return attribute
	 * @precondition iGenericObjectId != null
	 * @precondition sAttributeName != null
	 * @postcondition result != null
	 * @postcondition !result.isRemoved()
	 */
	public DynamicAttributeVO getAttribute(Integer iGenericObjectId, String sAttributeName) {
		if (iGenericObjectId == null) {
			throw new NullArgumentException("iGenericObjectId");
		}
		if (sAttributeName == null) {
			throw new NullArgumentException("sAttributeName");
		}
		final DynamicAttributeVO result = this.getRuleInterface().getAttribute(iGenericObjectId, sAttributeName);

		assert result != null;
		assert !result.isRemoved();
		return result;
	}

	/**
	 * get an attribute value for leased object with intid <code>iGenericObjectId</code>.
	 * @param iGenericObjectId leased object id
	 * @param sAttributeName attribute name of attribute to get
	 * @return attribute value
	 * @precondition iGenericObjectId != null
	 * @precondition sAttributeName != null
	 * @deprecated
	 */
	@Deprecated
	public Object getAttributeValue(Integer iGenericObjectId, String sAttributeName) {
		String sEntityName = Modules.getInstance().getEntityNameByModuleId(this.getRuleInterface().getModuleId(iGenericObjectId));

		return this.getFieldValue(sEntityName, iGenericObjectId, sAttributeName);
	}

	/**
	 * get a field value for object with intid <code>iObjectId</code>.
	 * @param sEntityName entity name
	 * @param iObjectId object id
	 * @param sFieldName field name of field to get
	 * @return field value
	 * @precondition iObjectId != null
	 * @precondition sFieldName != null
	 */
	public Object getFieldValue(String sEntityName, Integer iObjectId, String sFieldName) {
		if (iObjectId == null) {
			throw new NullArgumentException("iObjectId");
		}
		if (sFieldName == null) {
			throw new NullArgumentException("sFieldName");
		}
		if (Modules.getInstance().isModuleEntity(sEntityName)) {
			if (AttributeCache.getInstance().getAttribute(sEntityName, sFieldName).isCalculated()) {
				try {
					return getCalculatedAttributeValue(iObjectId, sFieldName, null);
				}
				catch (NuclosBusinessRuleException e) {
					this.error(e);
					return null;
				}
			}
			else {
				return this.getAttribute(iObjectId, sFieldName).getValue();
			}
		}
		else {
			return this.getRuleInterface().getMasterData(sEntityName, iObjectId).getField(sFieldName);
		}
	}

	/**
	 * @param iModuleId id of the module to search for. Specify <code>null</code> to search in all modules.
	 * @param cond condition that the leased objects must satisfy.
	 * @return the found leased objects' ids.
	 */
	public Collection<Integer> getGenericObjectIds(Integer iModuleId, CollectableSearchCondition cond) {
		return getRuleInterface().getGenericObjectIds(iModuleId, cond);
	}

	/**
	 * @param iEntityId id of the master data entity to search for
	 * @param cond condition that the masterdata objects must satisfy
	 * @return the found masterdata objects' ids
	 */
	public Collection<Object> getMasterDataIds(Integer iEntityId, CollectableSearchExpression cond) {
		return this.getMasterDataIds(MasterDataMetaCache.getInstance().getMetaDataById(iEntityId).getEntityName(), cond);
	}

	/**
	 * @param sEntityName name of the module to search for.
	 * @param cond condition that the leased objects must satisfy.
	 * @return the found leased objects' ids.
	 * @precondition sEntityName != null
	 */
	public Collection<Integer> getGenericObjectIds(String sEntityName, CollectableSearchCondition cond) {
		return this.getGenericObjectIds(Modules.getInstance().getModuleIdByEntityName(sEntityName), cond);
	}

	/**
	 * @param sEntityName name of the master data entity to search for
	 * @param cond condition that the masterdata objects must satisfy
	 * @return the found masterdata objects' ids
	 */
	public Collection<Object> getMasterDataIds(String sEntityName, CollectableSearchExpression cond) {
		return getRuleInterface().getMasterDataIds(sEntityName, cond);
	}

	/**
	 *
	 * @param sEntityName
	 * @param sAttributeName name of the attribute to compare against
	 * @param compop the logical operator to use
	 * @param oValue the value to compare against
	 * @return a new condition in the form "sAttributeName compop oValue"
	 */
	public CollectableComparison newGOComparison(String sEntityName, String sAttributeName, ComparisonOperator compop, Object oValue) {
		return SearchConditionUtils.newLOComparison(getAttributeProvider(), sEntityName, sAttributeName, compop, oValue);
	}

	/**
	 *
	 * @param sEntityName
	 * @param sAttributeName name of the attribute to compare against
	 * @param sValue the "like" expression (probably) containing wildcards
	 * @return a new condition in the form "sAttributeName LIKE sValue"
	 */
	public CollectableLikeCondition newGOLikeCondition(String sEntityName, String sAttributeName, String sValue) {
		return SearchConditionUtils.newLOLikeCondition(getAttributeProvider(), sEntityName, sAttributeName, sValue);
	}

	/**
	 *
	 * @param sEntityName
	 * @param sAttributeName
	 * @return a new condition in the form "sAttributeName IS NULL"
	 */
	public CollectableIsNullCondition newGOIsNullCondition(String sEntityName, String sAttributeName) {
		return SearchConditionUtils.newLOIsNullCondition(getAttributeProvider(), sEntityName, sAttributeName);
	}

	public static CompositeCollectableSearchCondition newAndCondition(CollectableSearchCondition... acond) {
		return SearchConditionUtils.and(acond);
	}

	public static CompositeCollectableSearchCondition newOrCondition(CollectableSearchCondition... acond) {
		return SearchConditionUtils.or(acond);
	}

	public static CompositeCollectableSearchCondition newNotCondition(CollectableSearchCondition cond) {
		return SearchConditionUtils.not(cond);
	}

	/**
	 * gets the dependant masterdata records belonging to the entity with the given name,
	 * using the given foreign key field to that entity.
	 * Changes to these masterdata records will not be stored.
	 * The masterdata is merged, so that the result contains the data as if it was saved yet.
	 * @param iId id by which sEntity and sParentEntity are related
	 * @param sEntityName name of the dependant entity
	 * @param sForeignKeyFieldName name of the foreign key field to the entity of the leased object with the given id.
	 * @return Collection<MasterDataVO>
	 */
	public Collection<MasterDataVO> getDependants(Integer iId, String sEntityName, String sForeignKeyFieldName) {
		final MasterDataFacadeLocal mdfacade = ServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
		try {
			return mdfacade.getDependantMasterData(sEntityName, sForeignKeyFieldName, iId);
		}
		catch (RuntimeException ex) {
			throw new NuclosFatalRuleException(ex);
		}
	}

	/**
	 * gets the dependant masterdata records belonging to the given entity, for the leased object with the given id..
	 * Changes to these masterdata records are not stored.
	 * The masterdata is merged, so that the result contains the data as if it was saved yet.
	 * This is the same as <code>this.getDependants(sEntityName, "genericObject")</code>.
	 * @param iObjectId id of the leased object
	 * @param sEntityName name of the dependant entity
	 * @return Collection<MasterDataVO>
	 */
	public Collection<MasterDataVO> getDependants(Integer iObjectId, String sEntityName) throws NuclosBusinessRuleException{
		try {
			this.getRuleInterface().getGenericObject(iObjectId);
			return this.getDependants(iObjectId, sEntityName, ModuleConstants.DEFAULT_FOREIGNKEYFIELDNAME);
		}
		catch (CommonFinderException e) {
			//for masterdata dependants needed foreignkeyfieldname
			throw new NuclosBusinessRuleException("code.interface.exception.2", e);//"Bitte geben Sie das Fremdschl\u00fcssel an."
		}
		catch (CommonPermissionException e) {
			throw new NuclosFatalRuleException(e.getMessage(), e);
		}

	}

	/**
	 * allows loggin in rules, info level
	 * @param message
	 */
	public void info(Object message) {
		logger.info(message);
	}

	/**
	 * allows loggin in rules, warn level
	 * @param message
	 */
	public void warn(Object message) {
		logger.warn(message);
	}

	/**
	 * allows loggin in rules, error level
	 * @param message
	 */
	public void error(Object message) {
		logger.error(message);
	}

	/**
	 * allows loggin in rules, fatal level
	 * @param message
	 */
	public void fatal(Object message) {
		logger.fatal(message);
	}

	private static AttributeCache getAttributeProvider() {
		return AttributeCache.getInstance();
	}

	/**
	 *  call a database procedure
	 * @param sProcedureName
	 * @param oParams
	 */
	public void callDbProcedure(String sProcedureName, Object... oParams) throws NuclosBusinessRuleException {
		this.getRuleInterface().callDbProcedure(sProcedureName, oParams);
	}

	/**
	 *
	 * @param jndiName the JNDI Name set in the -ds.xml File
	 * @param selectStatement the select Statement to execute
	 * @return a {@link Collection} with {@link MasterDataVO} (easier to work with in a rule, columnames are the fields)
	 * @throws NuclosBusinessRuleException if the ds was not found
	 */
	public Collection<MasterDataVO> executeSelectOnJCADatasource(String jndiName, String selectStatement) throws NuclosBusinessRuleException {
		return this.getRuleInterface().executeSelectOnJCADatasource(jndiName, selectStatement);
	}

	/**
	 * call a database function
	 * @param sFunctionName
	 * @param resultType
	 * @param oParams
	 * @return return value of the database function
	 */
	public <T> T callDbFunction(String sFunctionName, Class<T> resultType, Object... oParams) throws NuclosBusinessRuleException {
		return this.getRuleInterface().callDbFunction(sFunctionName, resultType, oParams);
	}

	/**
	 * Get the calculated value of the specified attribute for the given genericobject calling the database
	 * function defined in the metadata of the attribute. As return type use iType (if set) or the type
	 * specified in the metadata of the attribute.
	 * @param iGenericObjectId
	 * @param sAttribute
	 * @return return value of calculated attribute
	 */
	public Object getCalculatedAttributeValue(Integer iGenericObjectId, String sAttribute, Class<?> type) throws NuclosBusinessRuleException {
		final AttributeCVO attributeCVO = AttributeCache.getInstance().getAttribute(this.getRuleInterface().getModuleId(iGenericObjectId), sAttribute);
		final String sFunction = attributeCVO.getCalcFunction();
		final DataType dataType = attributeCVO.getDataType();
		Object param = iGenericObjectId;

		if (type == null) {
			switch (dataType) {
				case BOOLEAN:
				case STRING:
				case DATE:
				case INTEGER:
				case DOUBLE:
					type = dataType.getJavaClass();
					break;
				default:
					throw new NuclosBusinessRuleException(StringUtils.getParameterizedExceptionMessage("code.interface.exception.1", dataType.getJavaClass()));
						//"Der Datentyp ["+dataType.getJavaClass()+"] ist ung\u00fcltig.");
			}
		}

		return callDbFunction(sFunction, type, param);
	}

	/**
	 * @return the intid of the current leased object, if any.
	 * @deprecated
	 */
	@Deprecated
	public Integer getGenericObjectId() {
		return this.getObjectId();
	}

	/**
	 * @return the intid of the current object, if any
	 */
	public Integer getObjectId() {
		if (this.getGenericObject() != null) {
			return this.getGenericObject().getId();
		}
		else {
			return this.getMasterData().getIntId();
		}
	}

	public void setConnectionSettingsForWebservice(Object stub, String serviceName) throws NuclosBusinessRuleException {
		this.getRuleInterface().setConnectionSettingsForWebservice(stub, serviceName);
	}

	public File createTempFile(String fileName, byte[] data) throws NuclosBusinessRuleException {
		return this.getRuleInterface().createTempFile(fileName, data);
	}

	public List<?> getCellValues(InputStream stream, String...cellNames) throws NuclosBusinessRuleException {
		try {
			ExcelReader reader = new ExcelReader(stream);
			return reader.getCellValues(cellNames);
		} catch (IOException e) {
			throw new NuclosBusinessRuleException("import.exception.3", e);
		}
	}

	public List<?> getCellValues(File file, String...cellNames) throws NuclosBusinessRuleException {
		ExcelReader reader = excelReader.get(file);
		if (reader == null) {
			try {
				reader = new ExcelReader(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				throw new NuclosBusinessRuleException(StringUtils.getParameterizedExceptionMessage("import.exception.2", file.getAbsolutePath()));
			} catch (IOException e) {
				throw new NuclosBusinessRuleException("import.exception.3", e);
			}
			excelReader.put(file, reader);
		}
		return reader.getCellValues(cellNames);
	}

	public Map<String, ?> getDocumentTags(InputStream stream) throws NuclosBusinessRuleException {
		try {
			WordXMLReader reader = new WordXMLReader(stream);
			return reader.getStructuredDocumentTagValues();
		} catch (IOException e) {
			throw new NuclosBusinessRuleException("import.exception.3", e);
		}
	}

	public Map<String, ?> getDocumentTags(File file) throws NuclosBusinessRuleException {
		WordXMLReader reader;
		try {
			reader = new WordXMLReader(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw new NuclosBusinessRuleException(StringUtils.getParameterizedExceptionMessage("import.exception.2", file.getAbsolutePath()));
		} catch (IOException e) {
			throw new NuclosBusinessRuleException("import.exception.3", e);
		}
		return reader.getStructuredDocumentTagValues();
	}
}	// class CustomCodeInterface
