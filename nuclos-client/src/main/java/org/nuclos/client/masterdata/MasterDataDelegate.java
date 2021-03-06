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
package org.nuclos.client.masterdata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import org.nuclos.client.common.ClientParameterProvider;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.MasterDataToEntityObjectTransformer;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.collection.PredicateUtils;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.EntityAndFieldName;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.TruncatableCollection;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.common.NuclosUpdateException;
import org.nuclos.server.genericobject.ProxyList;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeRemote;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMapImpl;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;
import org.nuclos.server.ruleengine.valueobject.RuleVO;

/**
 * Business Delegate for <code>MasterDataFacadeBean</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author      <a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
// @Component
public class MasterDataDelegate {

	private static final Logger LOG = Logger.getLogger(MasterDataDelegate.class);

	public static final String ENTITYNAME_ENTITY = "entity";
	
	private static MasterDataDelegate INSTANCE;

	// 

	private MasterDataFacadeRemote facade;

	private MasterDataLayoutCache mdlayoutcache;

	private Map<String, MasterDataMetaVO> metaDataCache;
	
	private SpringLocaleDelegate localeDelegate;
	
	private MasterDataCache masterDataCache;

	/**
	 * Use getInstance() to create an (the) instance of this class
	 */
	MasterDataDelegate() throws RuntimeException {
		INSTANCE = this;
	}

	public static MasterDataDelegate getInstance() {
		if (INSTANCE == null) {
			// lazy support
			INSTANCE = SpringApplicationContextHolder.getBean(MasterDataDelegate.class);
		}
		return INSTANCE;
	}
	
	// @Autowired
	public final void setMasterDataFacadeRemote(MasterDataFacadeRemote masterDataFacadeRemote) {
		this.facade = masterDataFacadeRemote;
	}
	
	// @Autowired
	public final void setSpringLocaleDelegate(SpringLocaleDelegate cld) {
		this.localeDelegate = cld;
	}
	
	// @Autowired
	public final void setMasterDataCache(MasterDataCache masterDataCache) {
		this.masterDataCache = masterDataCache;
	}

	private MasterDataFacadeRemote getMasterDataFacade() {
		return facade;
	}

	private synchronized MasterDataLayoutCache getLayoutCache() {
		if (mdlayoutcache == null) {
			mdlayoutcache = new MasterDataLayoutCache();
		}
		assert mdlayoutcache != null;
		return mdlayoutcache;
	}

	public synchronized void invalidateLayoutCache() {
		mdlayoutcache = null;
		LOG.info("invalidate LayoutCache done");
	}

	public void revalidateMasterDataMetaCache() {
		getMasterDataFacade().revalidateMasterDataMetaCache();
	}
	
	public String getLayoutML(String sEntityName, boolean bSearchMode, String sCustom) {
		 return getLayoutCache().get(sEntityName, bSearchMode, sCustom);
	 }

	 public Integer getLayoutId(String entityName, boolean searchMode, String sCustom) {
		 return getLayoutCache().getLayoutId(entityName, searchMode, sCustom);
	 }

	/**
	 * @param sEntityName
	 * @param bSearchMode Search mode? Otherwise: Details mode.
	 * @return the (cached) layout ml document, if any, for the given entity and mode.
	 */
	 public String getLayoutML(String sEntityName, boolean bSearchMode) {
		 return getLayoutML(sEntityName, bSearchMode, ClientParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
	 }

	 public Integer getLayoutId(String entityName, boolean searchMode) {
		 return getLayoutId(entityName, searchMode, ClientParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
	 }


	/**
	 * gets the meta data for all master data tables.
	 * @return Collection<MasterDataMetaVO>. A (possibly empty) collection of masterdata meta objects
	 */
	 public Collection<MasterDataMetaVO> getMetaData() {
		 try{
			 return getMetaDataCache().values();
		 }catch(RuntimeException ex){
			 final String sMessage = localeDelegate.getMessage(
					 "MasterDataDelegate.1", "Fehler beim Laden der Metadaten f\u00fcr alle Entit\u00e4ten.");
			 throw new CommonFatalException(sMessage,ex);
		 }
	 }

	 public MasterDataMetaVO getMetaData(NuclosEntity entity) {
		 return getMetaData(entity.getEntityName());
	 }

	 /**
	  * @return the meta data for an entity
	  * @postcondition result != null
	  * @throws CommonFatalException if there is no entity with the given name.
	  */
	 public MasterDataMetaVO getMetaData(String sEntityName) {
		 try{
			 MasterDataMetaVO result = getMetaDataCache().get(sEntityName);
			 if (result == null) {
				 result = getMetaDataNoCache(sEntityName);
				 if(result == null) {
					 throw new CommonFatalException(localeDelegate.getMessage(
							 "MasterDataDelegate.2", "Keine Metadaten f\u00fcr die Entit\u00e4t {0} vorhanden.", sEntityName));
				 }
				 else {
					 invalidateCaches();
				 }
			 }
			 assert result != null;
			 return result;
		 }catch(RuntimeException ex){
			 final String sMessage = localeDelegate.getMessage(
					 "MasterDataDelegate.3", "Fehler beim Laden der Metadaten f\u00fcr die Entit\u00e4t {0}.", sEntityName);
			 throw new CommonFatalException(sMessage, ex);
		 }

	 }

	 public boolean hasEntity(String sEntityName) {
		 if(getMetaDataCache().get(sEntityName) != null)
			 return true;
		 else
			 return false;
	 }

	 /**
	  * @return the meta data for an entity
	  * @postcondition result != null
	  * @throws CommonFatalException if there is no entity with the given id.
	  */
	 public MasterDataMetaVO getMetaData(Integer iEntityId) throws CommonFatalException {
		 try {
			 MasterDataMetaVO result = null;
			 for(MasterDataMetaVO mvo : getMetaDataCache().values()) {
				 if(mvo.getId().equals(iEntityId)) {
					 result = mvo;
					 break;
				 }
			 }
			 if (result == null) {
				 result = getMetaDataNoCache(iEntityId);
				 if(result == null) {
					 throw new CommonFatalException(localeDelegate.getMessage(
							 "MasterDataDelegate.2", "Keine Metadaten f\u00fcr die Entit\u00e4t {0} vorhanden.", iEntityId));
				 }
				 else {
					 invalidateCaches();
				 }
			 }
			 assert result != null;
			 return result;
		 }
		 catch (RuntimeException ex) {
			 final String sMessage = localeDelegate.getMessage(
					 "MasterDataDelegate.4", "Fehler beim Laden der Daten f\u00fcr die Entit\u00e4t {0}.", iEntityId);
			 throw new CommonFatalException(sMessage, ex);
		 }
	 }

	 /**
	  * @param iModuleId the id of the module whose subentities we are looking for
	  * @return Collection<MasterdataMetaCVO> the masterdata meta information for all entities having foreign keys to the given module.
	  */
	 public Collection<MasterDataMetaVO> getMetaDataByModuleId(Integer iModuleId) throws NuclosBusinessException {
		 try {
			 return this.getMasterDataFacade().getMetaDataByModuleId(iModuleId);
		 }
		 catch (RuntimeException ex) {
			 final String sMessage = localeDelegate.getMessage(
					 "MasterDataDelegate.5", "Fehler beim Ermitteln der verf\u00fcgbaren Unterentit\u00e4ten f\u00fcr das Modul {0}.", iModuleId);
			 throw new NuclosBusinessException(sMessage, ex);
		 }

	 }

	 /**
	  * @param iModuleId the id of the module whose subentities we are looking for
	  * @return the subentities having foreign keys to the given module.
	  */
	 public List<CollectableField> getSubEntities(Integer iModuleId) throws NuclosBusinessException {
		 try {
			 return this.getMasterDataFacade().getSubEntities(iModuleId);
		 }
		 catch (RuntimeException ex) {
			 final String sMessage = localeDelegate.getMessage(
					 "MasterDataDelegate.5", "Fehler beim Ermitteln der verf\u00fcgbaren Unterentit\u00e4ten f\u00fcr das Modul {0}.", iModuleId);
			 throw new NuclosBusinessException(sMessage, ex);
		 }

	 }

	 /**
	  * gets all the masterdata for an entity
	  * @param sEntityName
	  * @return a (possibly empty) collection of masterdata objects.
	  * @postcondition result != null
	  * @postcondition !result.isTruncated()
	  */
	 public Collection<MasterDataVO> getMasterData(String sEntityName) {
		 final TruncatableCollection<MasterDataVO> result = this.getMasterData(sEntityName, null);

		 assert result != null;
		 assert !result.isTruncated();
		 return result;
	 }

	 /**
	  * gets the masterdata for an entity, using the given search condition, if any. The result is never truncated.
	  * @param sEntityName
	  * @param cond may be <code>null</code>.
	  * @return a (possibly empty) collection of masterdata objects.
	  * @postcondition result != null
	  * @postcondition !result.isTruncated()
	  */
	 public TruncatableCollection<MasterDataVO> getMasterData(String sEntityName, CollectableSearchCondition cond) {
		 final TruncatableCollection<MasterDataVO> result = this.getMasterData(sEntityName, cond, false);

		 assert result != null;
		 assert !result.isTruncated();
		 return result;
	 }

	 /**
	  * @param sEntityName
	  * @param clctexpr
	  * @return
	  * @postcondition result != null
	  */
	 public ProxyList<MasterDataWithDependantsVO> getMasterDataProxyList(String sEntityName, CollectableSearchExpression clctexpr) {
		 try {
			 final ProxyList<MasterDataWithDependantsVO> result = this.getMasterDataFacade().getMasterDataProxyList(sEntityName, clctexpr);
			 assert result != null;
			 return result;
		 }
		 catch (RuntimeException ex) {
			 final String sMessage = localeDelegate.getMessage(
					 "MasterDataDelegate.4", "Fehler beim Laden der Daten f\u00fcr die Entit\u00e4t {0}.", sEntityName);
			 throw new CommonFatalException(sMessage, ex);
		 }
	 }

	 /**
	  * gets the masterdata for an entity, using the given search condition, if any.
	  * @param sEntityName
	  * @param cond may be <code>null</code>.
	  * @param bTruncate Are huge results to be truncated?
	  * @return a (possibly empty) collection of masterdata objects.
	  * @postcondition result != null
	  */
	 public TruncatableCollection<MasterDataVO> getMasterData(String sEntityName, CollectableSearchCondition cond, boolean bTruncate) {
		 try {
			 final TruncatableCollection<MasterDataVO> result = this.getMasterDataFacade().getMasterData(sEntityName, cond, !bTruncate);
			 assert result != null;
			 return result;
		 }
		 catch (RuntimeException ex) {
			 final String sMessage = localeDelegate.getMessage(
					 "MasterDataDelegate.4", "Fehler beim Laden der Daten f\u00fcr die Entit\u00e4t {0}.", sEntityName);
			 throw new CommonFatalException(sMessage, ex);
		 }
	 }

	 /**
	  * gets the dependent masterdata for an entity.
	  * @param sEntityName
	  * @param sForeignKeyFieldName
	  * @param oRelatedId the foreign key of the parent entity
	  * @return Collection<MasterDataVO>. A (possibly empty) collection of masterdata objects
	  */
	 public Collection<EntityObjectVO> getDependantMasterData(final String sEntityName, String sForeignKeyFieldName, Object oRelatedId) {
		 try {
			 Collection<EntityObjectVO> col = CollectionUtils.transform(
					 getMasterDataFacade().getDependantMasterData(sEntityName, sForeignKeyFieldName, oRelatedId), 
                     new MasterDataToEntityObjectTransformer(sEntityName));
			 return col;
		 }
		 catch (RuntimeException ex) {
			 throw new CommonFatalException(ex);
		 }
	 }

	 /**
	  * gets the dependent masterdata for an entity.
	  * @param sEntityName
	  * @param sForeignKeyFieldName
	  * @param oRelatedId the foreign key of the parent entity
	  * @return Collection<MasterDataVO>. A (possibly empty) collection of masterdata objects
	  */
	 public Collection<EntityObjectVO> getDependantMasterData(final String sEntityName, String sForeignKeyFieldName, Object oRelatedId, Map<String, Object> mpParams) {
		 try {
			 Collection<EntityObjectVO> col = CollectionUtils.transform(
					 getMasterDataFacade().getDependantMasterData(sEntityName, sForeignKeyFieldName, oRelatedId, mpParams), 
                     new MasterDataToEntityObjectTransformer(sEntityName));
			 return col;
		 }
		 catch (RuntimeException ex) {
			 throw new CommonFatalException(ex);
		 }
	 }
	 
	public List<Object> getMasterDataIds(String sEntityName, CollectableSearchExpression cse) {
		try {
			 List<Object> col = getMasterDataFacade().getMasterDataIds(sEntityName, cse);
			 return col;
		 }
		 catch (RuntimeException ex) {
			 throw new CommonFatalException(ex);
		 }
	}

	public Collection<EntityObjectVO>  getMasterDataMore(
			String sEntityName, final List<?> lstIntIds) {
		try {
			Collection<EntityObjectVO> col = CollectionUtils.transform(
					getMasterDataFacade().getMasterDataMore(
							 sEntityName, lstIntIds, Collections.EMPTY_LIST), 
                    new MasterDataToEntityObjectTransformer(sEntityName));
			 return col;
		 }
		 catch (RuntimeException ex) {
			 throw new CommonFatalException(ex);
		 }
	}

	 /**
	  * @param sEntityName
	  * @param oId the object's id (primary key)
	  * @return the masterdata object with the given entity and id.
	  * @throws CommonFinderException
	  * @throws CommonPermissionException
	  */
	 public MasterDataVO get(String sEntityName, Object oId) throws CommonFinderException, CommonPermissionException {
		 if (oId == null) {
			 throw new NullArgumentException("oId");
		 }
		 try {
			 return this.getMasterDataFacade().get(sEntityName, oId);
		 }
		 catch (RuntimeException ex) {
			 throw new CommonFatalException(ex);
		 }
	 }

	 /**
	  * @param sEntityName
	  * @param oId the object's id (primary key)
	  * @return the masterdata object with the given entity and id.
	  * @throws CommonFinderException
	  * @throws CommonPermissionException
	  */
	 public MasterDataVO get(String sEntityName, Object oId, boolean bUseCacheIfPossible) throws CommonFinderException, CommonPermissionException {
		 MasterDataVO result = null;
		 if (bUseCacheIfPossible) {
			 /** @todo optimize */
			 result = CollectionUtils.findFirst(masterDataCache.get(sEntityName),
				 PredicateUtils.transformedInputEquals(new MasterDataVO.GetId(), oId));
		 }
		 if (result == null) {
			 result = this.get(sEntityName, oId);
		 }
		 return result;
	 }

	 /**
	  * @param sEntityName
	  * @param oId the object's id (primary key)
	  * @return the masterdata object with the given entity and id.
	  * @throws CommonFinderException
	  * @throws CommonPermissionException
	  */
	 public MasterDataWithDependantsVO getWithDependants(String sEntityName, Object oId, List<EntityAndFieldName> lstsefk) throws CommonFinderException, NuclosBusinessException, CommonPermissionException {
		 if (oId == null) {
			 throw new NullArgumentException("oId");
		 }
		 /** @todo use single facade method for this: */
		 return new MasterDataWithDependantsVO(this.get(sEntityName, oId), this.getDependants(oId, lstsefk));
	 }

	 /**
	  * returns the version of the given masterdata
	  * @param sEntityName
	  * @param oId
	  * @throws CommonFinderException
	  * @throws CommonPermissionException
	  */
	 public Integer getVersion(String sEntityName, Object oId) throws CommonFinderException, CommonPermissionException {
		 try {
			 return this.getMasterDataFacade().getVersion(sEntityName, oId);
		 }
		 catch (RuntimeException ex) {
			 throw new CommonFatalException(ex);
		 }
	 }

	 public DependantMasterDataMap getDependants(Object oId, List<EntityAndFieldName> lsteafn) {
		 final DependantMasterDataMap result = new DependantMasterDataMapImpl();
		 for (EntityAndFieldName eafn : lsteafn) {
			 result.addAllData(eafn.getEntityName(), this.getDependantMasterData(eafn.getEntityName(), eafn.getFieldName(), oId, eafn.getMapParams()));
		 }
		 return result;
	 }

	 /**
	  * @param sEntityName
	  * @param oId
	  * @return <code>CollectableField</code> consisting of the id and the name (as value).
	  * @postcondition result != null
	  * @postcondition result.isIdField()
	  * @postcondition (oId == null) --> result.isNull()
	  * @deprecated This should not be used until it will be backuped by the masterdata cache...
	  */
	 @Deprecated
	 public CollectableField getCollectableField(String sEntityName, Object oId) throws CommonBusinessException {
		 /** @todo see above (deprecation) */
		 final String sName = (oId == null) ? null : this.get(sEntityName, oId).getField("name", String.class);
		 final CollectableField result = new CollectableValueIdField(oId, sName);

		 assert result != null;
		 assert result.isIdField();
		 assert !(oId == null) || result.isNull();

		 return result;
	 }

	 /**
	  * creates the given object, along with its dependants (if any).
	  * @param sEntityName
	  * @param mdvo must have an empty (<code>null</code>) id.
	  * @param mpDependants If <code>!= null</code>, all elements must have an empty (<code>null</code>) id.
	  * @return the created object
	  * @throws NuclosBusinessException
	  * @precondition mdvo.getId() != null
	  * @precondition mpDependants != null --> for(m : mpDependants.values()) { m.getId() == null }
	  */
	 public MasterDataVO create(String sEntityName, MasterDataVO mdvo, DependantMasterDataMap mpDependants, String customUsage)
	 throws CommonBusinessException {
		 try {
			 if (mdvo.getId() != null) {
				 throw new IllegalArgumentException("mdvo");
			 }
			 checkDependantsAreNew(mpDependants);
			 return this.getMasterDataFacade().create(sEntityName, mdvo, mpDependants, customUsage);
		 }
		 catch (RuntimeException ex) {
			// RuntimeException has the BAD habit to include its cause' message in its own message.
			// the default message of NuclosUpdateException is not always correct ("duplicate key").
			// cause of the exception will be added at Errors.java
			throw new CommonFatalException("MasterDataDelegate.7", ex);
		 }
		 catch (CommonCreateException ex) {
			 throw new NuclosBusinessException(ex.getMessage(), ex);
		 }
	 }

	 /**
	  * updates the given object, along with its dependants.
	  * @param sEntityName
	  * @param mdvo
	  * @param mpDependants May be <code>null</code>.
	  * @return the id of the updated object
	  * @throws CommonBusinessException
	  * @precondition mdvo.getId() != null
	  */
	 public Object update(String sEntityName, MasterDataVO mdvo, DependantMasterDataMap mpDependants, String customUsage)
	 throws CommonBusinessException {
		 if (mdvo.getId() == null) {
			 throw new IllegalArgumentException("mdvo");
		 }

		 final Logger log = Logger.getLogger(MasterDataDelegate.class);
		 if (log.isDebugEnabled()) {
			 log.debug("UPDATE: " + mdvo.getDebugInfo());
			 if (mpDependants != null) {
				 log.debug("Dependants:");
				 for (EntityObjectVO mdvoDependant : mpDependants.getAllData()) {
					 log.debug(mdvoDependant.getDebugInfo());
				 }
			 }
		 }
		 try {
			 return this.getMasterDataFacade().modify(sEntityName, mdvo, mpDependants, customUsage);
		 }
		 catch (RuntimeException ex) {
			// RuntimeException has the BAD habit to include its cause' message in its own message.
			// the default message of NuclosUpdateException is not always correct ("duplicate key").
			// cause of the exception will be added at Errors.java
			throw new NuclosUpdateException("MasterDataDelegate.6", ex);
		 }
	 }

	 public void remove(String sEntityName, MasterDataVO mdvo, String customUsage) throws CommonBusinessException {
		 try {
			 this.getMasterDataFacade().remove(sEntityName, mdvo, true, customUsage);
		 }
		 catch (RuntimeException ex) {
			 throw new CommonFatalException(ex);
		 }
	 }

	public Set<String> getSubFormEntityNamesByMasterDataEntity(String sEntityName, String customUsage) {
		return CollectionUtils.transformIntoSet(getSubFormEntitiesByMasterDataEntity(sEntityName, customUsage),
			new EntityAndFieldName.GetEntityName());
	}

	public Set<EntityAndFieldName> getSubFormEntitiesByMasterDataEntity(String sEntityName, String customUsage) {
		try {
			return this.getMasterDataFacade().getSubFormEntitiesByMasterDataEntity(sEntityName, customUsage);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}
	
	public boolean isSubformEntity(String sEntity) {
		return getLayoutCache().getLayoutId(sEntity, false, null) == null;
	}
	
	public Set<String> getCustomUsages(String sEntity) {
		return getLayoutCache().getCustomUsages(sEntity);
	}

	 /**
	  * @param sEntityName
	  * @return Does the entity with the given name use the rule engine?
	  */
	 public boolean getUsesRuleEngine(String sEntityName) {
		 try {
			 return this.getMasterDataFacade().getUsesRuleEngine(sEntityName);
		 }
		 catch (RuntimeException e) {
			 throw new CommonFatalException(e);
		 }
	 }

	 public void executeBusinessRules(String sEntityName, List<RuleVO>lstRuleVO, MasterDataWithDependantsVO mdvo, boolean bSaveAfterRuleExecution, String customUsage) throws CommonBusinessException {
		 try {
			 this.getMasterDataFacade().executeBusinessRules(sEntityName, lstRuleVO, mdvo, bSaveAfterRuleExecution, customUsage);
		 }
		 catch (RuntimeException ex) {
			 throw new CommonFatalException(ex);
		 }
	 }


	 /**
	  * Validate all masterdata entries against their meta information (length, format, min, max etc.).
	  * @param sOutputFileName the name of the csv file to which the results are written.
	  */
	 public void checkMasterDataValues(String sOutputFileName) {
		 try {
			 this.getMasterDataFacade().checkMasterDataValues(sOutputFileName);
		 }
		 catch (RuntimeException ex) {
			 throw new CommonFatalException(ex);
		 }
	 }

	 /** value list provider function
	  * @throws CommonPermissionException */
	  public Collection<MasterDataVO> getUserHierarchy(String rootUser) throws CommonPermissionException {
		  try {
			  return this.getMasterDataFacade().getUserHierarchy(rootUser);
		  } catch (RuntimeException ex) {
			  throw new NuclosFatalException(ex);
		  }
	  }

	 /**
	  * checks that all dependants (if any) have a <code>null</code> id.
	  * @param mpDependants may be <code>null</code>.
	  */
	 public static void checkDependantsAreNew(DependantMasterDataMap mpDependants) {
		 if (mpDependants != null && !mpDependants.areAllDependantsNew()) {
			 throw new IllegalArgumentException("mpDependants");
		 }
	 }

	 /** value list provider function */
	 public List<CollectableField> getProcessByUsage(Integer iModuleId, boolean bSearchMode) {
		 try {
			 return this.getMasterDataFacade().getProcessByUsage(iModuleId, bSearchMode);
		 }
		 catch (RuntimeException ex) {
			 throw new CommonFatalException(ex);
		 }
	 }

	 public Map<String, String> getRuleEventsWithLocaleResource() {
		 try {
			 return this.getMasterDataFacade().getRuleEventsWithLocaleResource();
		 }
		 catch (RuntimeException ex) {
			 throw new CommonFatalException(ex);
		 }
	 }

	 /** value list provider function
	  * @throws CommonPermissionException */
	  public Collection<MasterDataVO> getAllReports() throws CommonPermissionException {
		  try {
			  return this.getMasterDataFacade().getAllReports();
		  }
		  catch (CommonFinderException ex) {
			  throw new NuclosFatalException(ex);
		  }
		  catch (RuntimeException ex) {
			  throw new NuclosFatalException(ex);
		  }
	  }

	  private Map<String, MasterDataMetaVO> getMetaDataCache() throws RuntimeException {
		  if(metaDataCache == null) {
			  LOG.debug("Initializing metadata cache");
			  final Collection<MasterDataMetaVO> coll = getMasterDataFacade().getAllMetaData();
			  metaDataCache = new HashMap<String, MasterDataMetaVO>(coll.size() * 2);
			  for(MasterDataMetaVO mdmetavo : coll) {
				  metaDataCache.put(mdmetavo.getEntityName(), mdmetavo);
			  }
		  }
		  return metaDataCache;
	  }

	  private MasterDataMetaVO getMetaDataNoCache(String sEntityName) throws RuntimeException {
		  return getMasterDataFacade().getMetaData(sEntityName);
	  }

	  private MasterDataMetaVO getMetaDataNoCache(Integer iEntityId) throws RuntimeException {
		  return getMasterDataFacade().getMetaData(iEntityId);
	  }

	  public void invalidateCaches() {
		  LOG.debug("Invalidating meta data cache.");
		  metaDataCache = null;
	  }

	private static class Key {

		private final String sEntityName;
		private final boolean bSearch;
		private final String sCustom;

		private Key(String sEntityName, boolean bSearch, String sCustom) {
			this.sEntityName = sEntityName;
			this.bSearch = bSearch;
			this.sCustom = sCustom;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			final Key key = (Key) o;

			if (bSearch != key.bSearch) {
				return false;
			}
			if (!sEntityName.equals(key.sEntityName)) {
				return false;
			}
			if (!LangUtils.equals(sCustom, key.sCustom)) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			return 29 * (sEntityName+sCustom).hashCode() + (bSearch ? 1 : 0);
		}
	}

	  /**
	   * inner class MasterDataLayoutCache
	   */
	  private class MasterDataLayoutCache {

		  private Map<Key, Pair<Integer, String>> mpUsages;
		  
		  private MasterDataLayoutCache() {
			  mpUsages = getLayoutUsagesMap();
		  }

		  private Map<Key, Pair<Integer, String>> getLayoutUsagesMap() {
			  final Map<Key, Pair<Integer, String>> result = new HashMap<Key, Pair<Integer, String>>();
			  final Map<Integer, String> mpLayouts = getLayoutsMap();
	
			  for (MasterDataVO mdvoUsage : facade.getMasterData(NuclosEntity.LAYOUTUSAGE.getEntityName(), null, true)) {
				  final String sEntityName = mdvoUsage.getField("entity", String.class);
				  final boolean bSearch = mdvoUsage.getField("searchScreen", Boolean.class);
				  final Integer iLayoutId = mdvoUsage.getField("layoutId", Integer.class);
				  final String sCustom = mdvoUsage.getField("custom", String.class);
				  result.put(
					  new Key(sEntityName, bSearch, sCustom),
					  new Pair<Integer, String>(iLayoutId, mpLayouts.get(iLayoutId)));
			  }
			  return result;
		  }

		  private Map<Integer, String> getLayoutsMap() {
			  final Map<Integer, String> result = new HashMap<Integer, String>();

			  for (MasterDataVO mdvoLayout : facade.getMasterData(NuclosEntity.LAYOUT.getEntityName(), null, true)) {
				  final Integer iLayoutId = (Integer) mdvoLayout.getId();
				  final String sLayoutML = mdvoLayout.getField("layoutML", String.class);
				  result.put(iLayoutId, sLayoutML);
			  }
			  return result;
		  }

		  /**
		   * @param sEntityName
		   * @param bSearchMode Search mode? Otherwise: Details mode.
		   * @return the layout ml document, if any, for the given entity and mode.
		   */
		  public String get(String sEntityName, boolean bSearchMode, String sCustom) {
			  Pair<Integer, String> p = mpUsages.get(new Key(sEntityName, bSearchMode, sCustom));
			  if (sCustom != null && p == null) {
				  p = mpUsages.get(new Key(sEntityName, bSearchMode, null));
			  }
			  return p != null ? p.y : null;
		  }

		  public Integer getLayoutId(String sEntityName, boolean bSearchMode, String sCustom) {
			  Pair<Integer, String> p = mpUsages.get(new Key(sEntityName, bSearchMode, sCustom));
			  if (sCustom != null && p == null) {
				  p = mpUsages.get(new Key(sEntityName, bSearchMode, null));
			  }
			  return p != null ? p.x : null;
		  }
		  
		  public Set<String> getCustomUsages(String sEntityName) {
			  Set<String> result = new HashSet<String>();
			  for (Key key : mpUsages.keySet()) {
				  if (key.sCustom != null && LangUtils.equals(sEntityName, key.sEntityName)) {
					  result.add(key.sCustom);
				  }
			  }
			  return result;
		  }
		  
	  }       // class MasterDataLayoutCache
	  
}       // class MasterDataDelegate
