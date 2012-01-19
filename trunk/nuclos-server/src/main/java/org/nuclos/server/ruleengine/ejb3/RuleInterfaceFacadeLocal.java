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
package org.nuclos.server.ruleengine.ejb3;

import java.io.File;
import java.util.Collection;
import java.util.Date;

import org.nuclos.common.attribute.DynamicAttributeVO;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode.RelationDirection;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVO;
import org.nuclos.server.ruleengine.valueobject.RuleVO;

// @Local
public interface RuleInterfaceFacadeLocal {

	Integer getModuleId(Integer iGenericObjectId);

	GenericObjectVO getGenericObject(Integer iGenericObjectId)
		throws CommonFinderException, CommonPermissionException;

	boolean isStateEqual(Integer iGenericObjectId, int iNumeral)
		throws CommonFinderException;

	boolean isAttributeEqual(Integer iGenericObjectId,
		String sAttribute, Object oValue);

	boolean isAttributeNull(Integer iGenericObjectId,
		String sAttribute);

	void sendMessage(String[] asRecipients, String sSubject,
		String sMessage) throws NuclosBusinessRuleException;

	Integer createObject(Integer iGenericObjectId,
		String sGenerator) throws NuclosBusinessRuleException;

	Integer createObject(String sEntityName, Integer iObjectId,
		String sGenerator) throws NuclosBusinessRuleException;

	public abstract Integer createObject(RuleObjectContainerCVO loccvo,
		String sGenerator) throws NuclosBusinessRuleException;

	/**
	 * retrieves the attribute value with the given name in the leased object with the given id.
	 * @postcondition result != null
	 * @postcondition !result.isRemoved()
	 */
	DynamicAttributeVO getAttribute(Integer iGenericObjectId,
		String sAttribute);

	/**
	 * sets the attribute with the given name in the leased object with the given id to the given value id and value.
	 * The leased object is read from the database and stored later (after the change).
	 * @precondition iGenericObjectId != null
	 */
	void setAttribute(RuleVO ruleVO, Integer iGenericObjectId,
		String sAttribute, Integer iValueId, Object oValue)
		throws NuclosBusinessRuleException;

	/**
	 * sets the attribute with the given name value in the given GenericObjectVO to the given value id and value.
	 * This method does not apply any changes to the database.
	 */
	void setAttribute(RuleVO ruleVO, GenericObjectVO govo,
		String sAttribute, Integer iValueId, Object oValue);

	/**
	 * sets the field with the given name value in the given MasterDataVO to the given value id and value.
	 * This method does not apply any changes to the database.
	 */
	void setMasterDataField(String sEntityName,
		MasterDataVO mdvo, String sFieldName, Integer iValueId, Object oValue);

	/**
	 * sets the field with the given name in the masterdata object with the given id to the given value id and value.
	 * The masterdata object is read from the database and stored later (after the change).
	 * @precondition iId != null
	 */
	void setMasterDataField(String sEntityName, Integer iId,
		String sFieldName, Integer iValueId, Object oValue);

	/**
	 * @precondition iGenericObjectId != null
	 */
	GenericObjectVO changeState(GenericObjectVO govoCurrent,
		Integer iGenericObjectId, int iNumeral)
		throws NuclosBusinessRuleException;

	/**
	 * @precondition iGenericObjectId != null
	 */
	void changeState(Integer iGenericObjectId, int iNumeral)
		throws NuclosBusinessRuleException;

	/**
	 * performs a state change for the leased object with the given id at the given point in time. If an old job exists already, it
	 * is always removed.
	 * @param iGenericObjectId
	 * @param iNewState the new state for the object.
	 * @param dateToSchedule the date for the state change to happen. If <code>null</code> only a possibly existing job is removed.
	 * If <code>dateToSchedule</code> is in the future, a new job is scheduled for the given date. If <code>dateToSchedule</code> is in the past,
	 * the state change is executed immediately (synchronously).
	 * @precondition iGenericObjectId != null
	 * @throws NuclosBusinessRuleException if the transition from the current state to the new state is not possible for the given object.
	 */
	GenericObjectVO scheduleStateChange(
		GenericObjectVO govoCurrent, Integer iGenericObjectId, int iNewState,
		Date dateToSchedule) throws NuclosBusinessRuleException,
		CommonFinderException;

	/**
	 * schedules a test job once for ten seconds later.
	 */
	void scheduleTestJob();

	/**
	 * @param iModuleId
	 * @param cond
	 * @return Collection<Integer>
	 */
	Collection<Integer> getGenericObjectIds(Integer iModuleId,
		CollectableSearchCondition cond);

	/**
	 * @param sEntityName
	 * @param cond
	 * @return Collection<Integer>
	 */
	Collection<Object> getMasterDataIds(String sEntityName,
		CollectableSearchExpression cond);

	/**
	 * @param sEntityName
	 * @param cond
	 * @return Collection<Integer>
	 */
	Collection<Object> getMasterDataIds(String sEntityName);

	/**
	 * @param iModuleId
	 * @param iGenericObjectId
	 * @param direction
	 * @param relationType
	 * @return ids of the leased objects of the given module related to the given leased object in the specified way.
	 * @postcondition result != null
	 * @postcondition (iModuleId == null) --> result.isEmpty()
	 * @postcondition (iGenericObjectId == null) --> result.isEmpty()
	 */
	Collection<Integer> getRelatedGenericObjectIds(
		Integer iModuleId, Integer iGenericObjectId, RelationDirection direction,
		String relationType);

	void relate(Integer iGenericObjectIdSource,
		Integer iGenericObjectIdTarget, String relationType, Date dateValidFrom,
		Date dateValidUntil, String sDescription) throws CommonFinderException,
		CommonCreateException, CommonPermissionException,
		NuclosBusinessRuleException;

	void unrelate(Integer iGenericObjectIdSource,
		Integer iGenericObjectIdTarget, String relationType)
		throws CommonFinderException, CommonPermissionException,
		NuclosBusinessRuleException, CommonRemoveException;

	/**
	 * invalidates the given relations by setting "validUntil" to the current date, if necessary
	 */
	void invalidateRelations(Integer iGenericObjectIdSource,
		Integer iGenericObjectIdTarget, String relationType)
		throws CommonFinderException, CommonBusinessException,
		NuclosBusinessRuleException;

	boolean isStateChangePossible(Integer iGenericObjectId,
		int iNewState) throws CommonFinderException;

	/**
	 * call a database procedure
	 * @param sProcedureName the name of the procedure to call
	 * @param oParams the parameters (note that it is not possible to use null as a parameter use {@code DbNull} instead)
	 * @throws NuclosBusinessRuleException
	 */
	void callDbProcedure(String sProcedureName,
		Object ... oParams) throws NuclosBusinessRuleException;

	/**
	 * call a database function
	 * @param sFunctionName the name of the function to call
	 * @param iResultType the type of the function result as defined in java.sql.Types
	 * @param oParams the parameters (note that it is not possible to use null as a parameter use {@code DbNull} instead)
	 * @return the result of the function the object is of the java type corresponding to iResultType
	 * @throws NuclosBusinessRuleException
	 */
	<T> T callDbFunction(String sFunctionName, Class<T> resultType,
		Object ... oParams) throws NuclosBusinessRuleException;

	/**
	 * 
	 * @param jndiName the JNDI Name set in the -ds.xml File
	 * @param selectStatement the select Statement to execute
	 * @return a {@link Collection} with {@link MasterDataVO} (easier to work with in a rule, columnames are the fields)
	 * @throws NuclosBusinessRuleException if the ds was not found
	 */
	Collection<MasterDataVO> executeSelectOnJCADatasource(String jndiName, String selectStatement) 
			throws NuclosBusinessRuleException;

	void setConnectionSettingsForWebservice(Object stub, String serviceName) 
			throws NuclosBusinessRuleException;
	
	File createTempFile(String fileName, byte[] data) throws NuclosBusinessRuleException;
	
	EntityObjectVO getEntityObject(String entity, Long id);
	
	MasterDataVO getMasterData(String sEntityName, Integer iId);

	void logInfo(Integer iSessionId, String sMessage,
		String sRuleName) throws NuclosBusinessRuleException;

	void logWarning(Integer iSessionId, String sMessage,
		String sRuleName) throws NuclosBusinessRuleException;

	void logError(Integer iSessionId, String sMessage,
		String sRuleName) throws NuclosBusinessRuleException;
}
