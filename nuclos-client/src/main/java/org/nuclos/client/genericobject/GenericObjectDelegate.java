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
package org.nuclos.client.genericobject;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import org.nuclos.client.attribute.AttributeCache;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.common.GenericObjectMetaDataVO;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.attribute.DynamicAttributeVO;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.TruncatableCollection;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.server.attribute.BadGenericObjectException;
import org.nuclos.server.common.NuclosUpdateException;
import org.nuclos.server.genericobject.ProxyList;
import org.nuclos.server.genericobject.ejb3.GenericObjectFacadeRemote;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;
import org.nuclos.server.genericobject.valueobject.LogbookVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.ruleengine.valueobject.RuleVO;

/**
 * Business Delegate for <code>GenericObjectFacadeBean</code> and <code>GeneratorFacadeBean</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
// @Component
public class GenericObjectDelegate {
	
	private static final Logger LOG = Logger.getLogger(GenericObjectDelegate.class);
	
	private static GenericObjectDelegate INSTANCE;

	// 
	
	private GenericObjectFacadeRemote gofacade;

	private Map<Integer, String> resourceMapCache;

	/**
	 * Use getInstance() to get the one and only instance of this class.
	 */
	GenericObjectDelegate() {
		INSTANCE = this;
	}
	
	public static GenericObjectDelegate getInstance() {
		if (INSTANCE == null) {
			// lazy support
			SpringApplicationContextHolder.getBean(GenericObjectDelegate.class);			
		}
		return INSTANCE;
	}
	
	// @Autowired
	public final void setGenericObjectFacadeRemote(GenericObjectFacadeRemote genericObjectFacadeRemote) {
		this.gofacade = genericObjectFacadeRemote;
	}

	protected GenericObjectFacadeRemote getGenericObjectFacade() {
		return this.gofacade;
	}

	/**
	 * @return the leased object meta data
	 *
	 * @deprecated As GenericObjectMetaDataVO is deprecated.
	 */
	public synchronized GenericObjectMetaDataVO getMetaDataCVO() {
		try {
			return this.getGenericObjectFacade().getMetaData();
		}
		catch (RuntimeException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	/**
	 * gets all leased objects with the given module id, that match the given search condition.
	 * @param iModuleId May be <code>null</code>.
	 * @param clctcond May be <code>null</code>.
	 * @return list of leased object value objects including dependants, but excluding parent objects
	 */
	public List<GenericObjectWithDependantsVO> getCompleteGenericObjectsWithDependants(Integer iModuleId, CollectableSearchCondition clctcond, Set<String> stRequiredSubEntityNames) {
		try {
			LOG.debug("START getCompleteGenericObjectsWithDependants");
			return this.getGenericObjectFacade().getGenericObjectsWithDependants(iModuleId, new CollectableSearchExpression(clctcond), null, stRequiredSubEntityNames, false, true);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
		finally {
			LOG.debug("FINISHED getCompleteGenericObjectsWithDependants");
		}
	}

	/**
	 * gets all leased objects, along with the required subform data, with the given module id, that match the given search condition.
	 * @param bIncludeSubModules
	 * @param iModuleId May be null.
	 * @param clctexpr
	 * @param stRequiredAttributeIds Set<Integer> may be <code>null</code>, which means all attributes are required
	 * @param stRequiredSubEntities Set<String>
	 * @return List<GenericObjectWithDependantsVO> a proxy list that loads the result lazily (chunkwise).
	 */
	public ProxyList<GenericObjectWithDependantsVO> getGenericObjectsWithDependants(Integer iModuleId, CollectableSearchExpression clctexpr,
			Set<Integer> stRequiredAttributeIds, Set<String> stRequiredSubEntities, boolean bIncludeParentObjects, boolean bIncludeSubModules) {
		try {
			LOG.debug("START getGenericObjects");
			return this.getGenericObjectFacade().getGenericObjectsWithDependants(iModuleId, clctexpr, stRequiredAttributeIds, stRequiredSubEntities, bIncludeParentObjects, bIncludeSubModules);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
		finally {
			LOG.debug("FINISHED getGenericObjects");
		}
	}

	/**
	 * gets all leased objects, along with the required subform data, with the given module id, that match the given search condition,
	 * but the values of all attributes on which the current user has no read permission are cleared
	 * NOTE: use only within report mechanism
	 * @param bIncludeSubModules
	 * @param iModuleId May be null.
	 * @param clctexpr
	 * @param stRequiredAttributeIds Set<Integer> may be <code>null</code>, which means all attributes are required
	 * @param stRequiredSubEntities Set<String>
	 * @return List<GenericObjectWithDependantsVO> a proxy list that loads the result lazily (chunkwise).
	 */
	public ProxyList<GenericObjectWithDependantsVO> getPrintableGenericObjectsWithDependants(Integer iModuleId, CollectableSearchExpression clctexpr,
			Set<Integer> stRequiredAttributeIds, Set<String> stRequiredSubEntities, boolean bIncludeParentObjects, boolean bIncludeSubModules) {
		try {
			LOG.debug("START getGenericObjects");
			return this.getGenericObjectFacade().getPrintableGenericObjectsWithDependants(iModuleId, clctexpr, stRequiredAttributeIds, stRequiredSubEntities, bIncludeParentObjects, bIncludeSubModules);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
		finally {
			LOG.debug("FINISHED getGenericObjects");
		}
	}

	/*
	 * @return the generic object with the given id.
	 * @postcondition result != null
	 */
	public GenericObjectVO get(Integer iGenericObjectId) throws CommonFinderException, CommonPermissionException {
		try {
			return this.getGenericObjectFacade().get(iGenericObjectId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * gets the leased objects with the given module id, that match the given search condition, respecting the given max row count.
	 * @param iModuleId May be null.
	 * @param clctexpr
	 * @param stRequiredAttributeIds Set<Integer> may be <code>null</code>, which means all attributes are required
	 * @param stRequiredSubEntityNames Set<String>
	 * @param iMaxRowCount the maximum number of rows to return
	 * @return List<GenericObjectVO>
	 * @precondition iMaxRowCount > 0
	 * @precondition stRequiredSubEntityNames != null
	 * @postcondition result != null
	 * @postcondition result.size() <= iMaxRowCount
	 */
	public TruncatableCollection<GenericObjectWithDependantsVO> getRestrictedNumberOfGenericObjects(Integer iModuleId,
			CollectableSearchExpression clctexpr, Set<Integer> stRequiredAttributeIds, Set<String> stRequiredSubEntityNames,
			int iMaxRowCount) {
		try {
			final TruncatableCollection<GenericObjectWithDependantsVO> result =
					this.getGenericObjectFacade().getRestrictedNumberOfGenericObjects(iModuleId,
							clctexpr, stRequiredAttributeIds, stRequiredSubEntityNames, iMaxRowCount);
			assert result != null;
			assert result.size() <= iMaxRowCount;
			return result;
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * gets the leased object with the given id
	 * @param iModuleId (redundant, but needed for security)
	 * @param iGenericObjectId
	 * @return
	 * @throws CommonFinderException
	 * @deprecated use getWithDependants
	 */
	@Deprecated
	public GenericObjectVO get(int iModuleId, int iGenericObjectId) throws CommonBusinessException {
		try {
			LOG.debug("get start");
			final GenericObjectVO result = this.getGenericObjectFacade().get(iGenericObjectId);
			LOG.debug("get done");
			return result;
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * gets the leased object with the given id
	 * @param iGenericObjectId
	 * @return
	 * @throws CommonFinderException
	 */
	public GenericObjectWithDependantsVO getWithDependants(int iGenericObjectId) throws CommonBusinessException {
		try {
			LOG.debug("get start");
			final GenericObjectWithDependantsVO result = this.getGenericObjectFacade().getWithDependants(iGenericObjectId, null);
			LOG.debug("get done");
			return result;
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * returns the version of the given genericobject
	 * @param iGenericObjectId
	 * @throws CommonFinderException
	 * @throws CommonPermissionException
	 */
	public Integer getVersion(int iGenericObjectId) throws CommonFinderException, CommonPermissionException {
		try {
			return this.getGenericObjectFacade().getVersion(iGenericObjectId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * gets the (historical) leased object with the given id at the historical date.
	 * @param iGenericObjectId
	 * @param dateHistorical
	 * @return the contents of the leased object at the given point in time.
	 * @precondition dateHistorical != null
	 * @postcondition result != null
	 * @throws CommonFinderException if the given object didn't exist at the given point in time.
	 */
	public GenericObjectWithDependantsVO getHistorical(int iGenericObjectId, Date dateHistorical)
			throws CommonFinderException, CommonPermissionException {

		if (dateHistorical == null) {
			throw new NullArgumentException("dateHistorical");
		}
		try {
			final GenericObjectWithDependantsVO result = this.getGenericObjectFacade().getHistorical(iGenericObjectId, dateHistorical);
			assert result != null;
			return result;
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * updates the given leased object along with its dependants.
	 * @param lowdcvo
	 * @return the updated leased object (from the server) along with its dependants.
	 * @throws NuclosFatalException if a fatal error occured
	 * @throws CommonPermissionException if we have no permission to update the object
	 * @throws CommonStaleVersionException if the object was changed in the meantime by another user
	 * @throws NuclosBusinessRuleException
	 * @precondition lowdcvo.getId() != null
	 */
	public GenericObjectWithDependantsVO update(GenericObjectWithDependantsVO lowdcvo) throws CommonBusinessException {
		if (lowdcvo.getId() == null) {
			throw new IllegalArgumentException("lowdcvo");
		}
		try {
			LOG.debug("update start");
			final GenericObjectWithDependantsVO result = this.getGenericObjectFacade().modify(lowdcvo.getModuleId(), lowdcvo);
			LOG.debug("update done");
			return result;
		}
		catch (RuntimeException ex) {
			final Throwable tCause = ex.getCause();
			if (tCause instanceof BadGenericObjectException) {
				throw (BadGenericObjectException) tCause;
			}
			// RuntimeException has the BAD habit to include its cause' message in its own message.
			// the default message of NuclosUpdateException is not always correct ("duplicate key").
			/** @todo find a better solution */
			if (tCause == null) {
				throw new NuclosUpdateException(SpringLocaleDelegate.getInstance().getMessage(
						"GenericObjectDelegate.1", "Der Datensatz konnte nicht gespeichert werden.") + "\n" + ex.getMessage(), ex);
			} else {
				throw new NuclosUpdateException(SpringLocaleDelegate.getInstance().getMessage(
						"GenericObjectDelegate.1", "Der Datensatz konnte nicht gespeichert werden.") + "\n" + tCause.getMessage(), ex);
			}
		}
	}

	/**
	 * creates the given leased object and its dependants.
	 * @param lowdcvo must have an empty (<code>null</code>) id.
	 * @return the updated leased object (from the server)
	 * @precondition lowdcvo.getId() == null
	 * @precondition mpDependants != null --> for(m : mpDependants.values()) { m.getId() == null }
	 * @precondition stRequiredSubEntityNames != null
	 */
	public GenericObjectWithDependantsVO create(GenericObjectWithDependantsVO lowdcvo, Set<String> stRequiredSubEntityNames) throws CommonBusinessException {
		if (lowdcvo.getId() != null) {
			throw new IllegalArgumentException("lowdcvo");
		}
		MasterDataDelegate.checkDependantsAreNew(lowdcvo.getDependants());
		if (stRequiredSubEntityNames == null) {
			throw new NullArgumentException("stRequiredSubEntityNames");
		}
		debug(lowdcvo);
		try {
			LOG.debug("create start");
			final GenericObjectWithDependantsVO result = this.getGenericObjectFacade().create(lowdcvo, stRequiredSubEntityNames);
			LOG.debug("create done");
			return result;
		}
		catch (CommonFatalException ex) {
			if (ex.getCause() != null) // CreateException
			{
				final Throwable tCause = ex.getCause().getCause();
				if (tCause instanceof BadGenericObjectException) {
					throw (BadGenericObjectException) tCause;
				}
			}
			// RuntimeException has the BAD habit to include its cause' message in its own message.
			// the default message of NuclosUpdateException is not always correct ("duplicate key").
			/** @todo find a better solution */
			if (ex.getCause() == null) {
				throw new CommonCreateException(SpringLocaleDelegate.getInstance().getMessage(
						"GenericObjectDelegate.2", "Der Datensatz konnte nicht erzeugt werden.") + "\n" + ex.getMessage(), ex);
			} else {
				throw new CommonCreateException(SpringLocaleDelegate.getInstance().getMessage(
						"GenericObjectDelegate.2", "Der Datensatz konnte nicht erzeugt werden.") + "\n" + ex.getCause().getMessage(), ex);
			}
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * creates the given leased object and its dependants.
	 * @param lowdcvo must have an empty (<code>null</code>) id.
	 * @return the updated leased object (from the server)
	 * @precondition lowdcvo.getId() == null
	 * @precondition mpDependants != null --> for(m : mpDependants.values()) { m.getId() == null }
	 */
	public GenericObjectVO create(GenericObjectWithDependantsVO lowdcvo) throws CommonBusinessException {
		if (lowdcvo.getId() != null) {
			throw new IllegalArgumentException("lowdcvo");
		}
		MasterDataDelegate.checkDependantsAreNew(lowdcvo.getDependants());

		debug(lowdcvo);
		try {
			LOG.debug("create start");
			final GenericObjectVO result = this.getGenericObjectFacade().create(lowdcvo);
			LOG.debug("create done");
			return result;
		}
		catch (CommonFatalException ex) {
			if (ex.getCause() != null) // CreateException
			{
				final Throwable tCause = ex.getCause().getCause();
				if (tCause instanceof BadGenericObjectException) {
					throw (BadGenericObjectException) tCause;
				}
			}
			// RuntimeException has the BAD habit to include its cause' message in its own message.
			// the default message of NuclosUpdateException is not always correct ("duplicate key").
			/** @todo find a better solution */
			throw new CommonCreateException(SpringLocaleDelegate.getInstance().getMessage(
					"GenericObjectDelegate.2", "Der Datensatz konnte nicht erzeugt werden.") + "\n" + ex.getCause().getMessage(), ex);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}


	private void debug(GenericObjectVO govo) {
		/** @todo + GenericObjectVO.getDebugInfo() */
		final Collection<DynamicAttributeVO> collattrvo = govo.getAttributes();
		for (DynamicAttributeVO attrvo : collattrvo) {
			final StringBuffer sb = new StringBuffer();
			sb.append("name: " + AttributeCache.getInstance().getAttribute(attrvo.getAttributeId()).getName());
			sb.append(" - value: " + attrvo.getValue() + " - valueId: " + attrvo.getValueId());
			if (attrvo.isRemoved()) {
				sb.append(" - REMOVED");
			}
			LOG.debug(sb);
		}
	}

	/**
	 * removes the given leased object.
	 * @param gowdvo
	 * @param bDeletePhysically Remove from DB?
	 * @throws CommonPermissionException if we have no permission to remove the object
	 */
	public void remove(GenericObjectWithDependantsVO gowdvo, boolean bDeletePhysically) throws CommonBusinessException {
		try {
			this.getGenericObjectFacade().remove(gowdvo, bDeletePhysically);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * restore the given generic object.
	 * @param gowdvo
	 * @throws CommonPermissionException if we have no permission to restore the object
	 */
	public void restore(GenericObjectWithDependantsVO gowdvo) throws CommonBusinessException {
		try {
			this.getGenericObjectFacade().restore(gowdvo.getId());
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * adds the leased object with the given id to the group with the given id
	 * @param iGenericObjectId
	 * @param iGroupId
	 * @throws CommonCreateException
	 * @throws NuclosBusinessRuleException
	 */
	public void addToGroup(int iGenericObjectId, int iGroupId, boolean blnCheckWriteAllowedForObject)
		throws CommonCreateException, CommonFinderException, CommonPermissionException, NuclosBusinessRuleException
	{
		try {
			this.getGenericObjectFacade().addToGroup(iGenericObjectId, iGroupId, blnCheckWriteAllowedForObject);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * removes the generic object with the given id from the group with the given id
	 * @param iGenericObjectId
	 * @param iGroupId
	 * @throws CommonRemoveException
	 * @throws CommonFinderException
	 * @throws CommonCreateException
	 * @throws CommonStaleVersionException
	 * @throws NuclosBusinessRuleException
	 */
	public void removeFromGroup(int iGenericObjectId, int iGroupId)
		throws CommonRemoveException, CommonFinderException, CommonPermissionException,
			NuclosBusinessRuleException, CommonStaleVersionException, CommonCreateException
	{
		try {
			this.getGenericObjectFacade().removeFromGroup(iGenericObjectId, iGroupId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * removes the list of generic objects with the given id from the group with the given id
	 * @param mpGOGroupRelation
	 * @throws CommonRemoveException
	 * @throws CommonFinderException
	 * @throws CommonCreateException
	 * @throws CommonStaleVersionException
	 * @throws NuclosBusinessRuleException
	 */
	public void removeFromGroup(Map<Integer, Integer> mpGOGroupRelation)
		throws CommonRemoveException, CommonFinderException, CommonPermissionException,
		NuclosBusinessRuleException, CommonStaleVersionException, CommonCreateException
	{

		try {
			this.getGenericObjectFacade().removeFromGroup(mpGOGroupRelation);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public void relate(int iGenericObjectIdSource, String relationType, int iGenericObjectIdTarget, int iModuleIdTarget,
			Date dateValidFrom, Date dateValidUntil, String sDescription)
			throws CommonFinderException, CommonCreateException, CommonPermissionException, NuclosBusinessRuleException {
		try {
			this.getGenericObjectFacade().relate(iModuleIdTarget, iGenericObjectIdTarget,
					iGenericObjectIdSource, relationType, dateValidFrom, dateValidUntil, sDescription);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * removes the relation with the given id.
	 * @param iRelationId
	 * @param iGenericObjectIdTarget
	 * @param iModuleIdTarget
	 * @throws CommonFinderException
	 * @throws CommonRemoveException
	 */
	public void removeRelation(int iRelationId, int iGenericObjectIdTarget, int iModuleIdTarget)
			throws CommonFinderException, CommonRemoveException, CommonBusinessException {
		try {
			this.getGenericObjectFacade().removeRelation(iRelationId, iGenericObjectIdTarget, iModuleIdTarget);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * removes the relations with the given ids found in the map.
	 * @param mpGOTreeNodeRelation
	 * @throws CommonFinderException
	 * @throws CommonRemoveException
	 * @throws CommonPermissionException
	 */
	public void removeRelation(Map<Integer, GenericObjectTreeNode> mpGOTreeNodeRelation)
			throws CommonFinderException, CommonRemoveException, CommonBusinessException {
		try {
			this.getGenericObjectFacade().removeRelation(mpGOTreeNodeRelation);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * gets the logbook for the leased object with the given id.
	 * @param iGenericObjectId
	 * @return Collection<LogbookVO>
	 * @throws CommonFinderException
	 */
	public Collection<LogbookVO> getLogbook(int iGenericObjectId) throws CommonFinderException, CommonPermissionException {
		try {
			return this.getGenericObjectFacade().getLogbook(iGenericObjectId, null);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * @param iGenericObjectId
	 * @return the id of the module containing the object with the given id.
	 * @throws CommonFinderException if there is no generic object with the given id.
	 */
	public int getModuleContainingGenericObject(int iGenericObjectId) throws CommonFinderException {
		try {
			return this.getGenericObjectFacade().getModuleContainingGenericObject(iGenericObjectId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * @param iGenericObjectId
	 * @return the id of the state of the generic object with the given id
	 * @throws CommonFinderException
	 */
	public int getStateIdByGenericObject(int iGenericObjectId){
		try {
			return this.getGenericObjectFacade().getStateIdByGenericObject(iGenericObjectId);
		}
		catch(CommonFinderException ex) {
			throw new CommonFatalException(ex);
		}
		catch(RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * attach a document to a generic object
 	 */
	public void attachDocumentToObject(MasterDataVO mdvoDocument) throws CommonBusinessException {
		try {
			this.getGenericObjectFacade().attachDocumentToObject(mdvoDocument);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public void executeBusinessRules(List<RuleVO>lstRuleVO, GenericObjectWithDependantsVO govo, boolean bSaveAfterRuleExecution) throws CommonBusinessException {
		try {
			this.getGenericObjectFacade().executeBusinessRules(lstRuleVO, govo, bSaveAfterRuleExecution);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public Map<Integer, String> getResourceMap() {
		try {
			return getResourceMapCache();
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	private Map<Integer, String> getResourceMapCache() throws RuntimeException {
		if(resourceMapCache == null) {
			LOG.debug("Initializing resourceMap cache");
			resourceMapCache = getGenericObjectFacade().getResourceMap();
		}
		return resourceMapCache;
	}

	public void invalidateCaches() {
		LOG.debug("Invalidating resourceMap cache.");
		resourceMapCache = null;
	}

	/**
	 * gets the leased object's calculated attributes with the given quintuple
	 * @param quintuple
	 * @return
	 * @throws CommonPermissionException
	 * @throws CommonFinderException
	 * @throws CommonFinderException
	 */
//	public Collection<DynamicAttributeVO> getCalculatedAttributeValuesForGenericObject(final UsageCriteria quintuple, int iGenericObjectId)
//			throws CommonFinderException, CommonPermissionException {
//		try {
//			return this.getGenericObjectFacade().getCalculatedAttributeValuesForGenericObject(quintuple, iGenericObjectId);
//		}
//		catch (RuntimeException ex) {
//			throw new CommonFatalException(ex);
//		}
//	}

}	// class GenericObjectDelegate
