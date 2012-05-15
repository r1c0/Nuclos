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
package org.nuclos.server.dbtransfer.content;

import static org.nuclos.server.dbtransfer.TransferUtils.getContentType;
import static org.nuclos.server.dbtransfer.TransferUtils.getEntityObjectVO;
import static org.nuclos.server.dbtransfer.TransferUtils.getForeignFieldToNuclet;
import static org.nuclos.server.dbtransfer.TransferUtils.getForeignFields;
import static org.nuclos.server.dbtransfer.TransferUtils.getForeignFieldsToParent;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.jfree.util.Log;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collection.BinaryPredicate;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.dal.DalCallResult;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dbtransfer.NucletContentUID;
import org.nuclos.common.dbtransfer.TransferOption;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.LocaleInfo;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.ServerServiceLocator;
import org.nuclos.server.common.ejb3.LocaleFacadeLocal;
import org.nuclos.server.dal.DalUtils;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dbtransfer.NucletContentMap;
import org.nuclos.server.dbtransfer.TransferUtils;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.navigation.treenode.nuclet.content.AbstractNucletContentEntryTreeNode;

public abstract class AbstractNucletContent implements INucletContent {
	private Logger log;

	/**
	 *
	 */
	public static final String FOREIGN_FIELD_TO_NUCLET = AbstractNucletContentEntryTreeNode.FOREIGN_FIELD_TO_NUCLET;

	/**
	 *
	 */
	public static final String LOCALE_RESOURCE_MAPPING_FIELD_NAME = "AbstractNucletContent.localeResourceMapping";
	
	private List<EntityObjectVO> dbContent;

	private final NuclosEntity entity;
	private final NuclosEntity parententity;
	
	protected final List<INucletContent> contentTypes;

	private boolean enabled = true;
	private final boolean ignoreReferenceToNuclet;

	public AbstractNucletContent(NuclosEntity entity, NuclosEntity parententity, List<INucletContent> contentTypes) {
		this(entity, parententity, contentTypes, false);
	}

	public AbstractNucletContent(NuclosEntity entity, NuclosEntity parententity, List<INucletContent> contentTypes, boolean ignoreReferenceToNuclet) {
		super();
		this.entity = entity;
		this.parententity = parententity;
		this.contentTypes = contentTypes;
		this.ignoreReferenceToNuclet = ignoreReferenceToNuclet;

		if (parententity != null) {
			boolean isParentForeignEntity = false;
			for (EntityMetaDataVO eMeta : getForeignEntities()) {
				if (LangUtils.equals(parententity.getEntityName(), eMeta.getEntity())) {
					isParentForeignEntity = true;
				}
			}

			if (!isParentForeignEntity) {
				throw new IllegalArgumentException("Parent entity must be a foreign entity");
			}
		} else {
			if (!ignoreReferenceToNuclet) {
				getForeignFieldToNuclet(entity); // check validity
			}
		}
	}

	@Override
	public NuclosEntity getEntity() {
		return entity;
	}

	@Override
	public NuclosEntity getParentEntity() {
		return parententity;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public Set<EntityMetaDataVO> getForeignEntities() {
		return TransferUtils.getForeignEntities(entity);
	}

	@Override
	public Collection<EntityFieldMetaDataVO> getFieldDependencies() {
		return TransferUtils.getFieldDependencies(entity);
	}

	@Override
	public NucletContentUID.Map getUIDMap(Set<Long> nucletIds, TransferOption.Map transferOptions) {
		NucletContentUID.Map result = new NucletContentUID.HashMap();
		for (EntityObjectVO uidObject : getUIDObjects(nucletIds, transferOptions)) {
			result.add(uidObject);
		}
		return result;
	}

	@Override
	public List<EntityObjectVO> getUIDObjects(Set<Long> nucletIds, TransferOption.Map transferOptions) {
		List<EntityObjectVO> result = new ArrayList<EntityObjectVO>();
		for (EntityObjectVO ncObject : getNcObjects(nucletIds, transferOptions)) {
			EntityObjectVO uidObject = TransferUtils.getUID(entity, ncObject.getId());
			if (uidObject != null) {
				result.add(uidObject);
			}
		}
		return result;
	}

	@Override
	public List<EntityObjectVO> getNcObjects(Set<Long> nucletIds, TransferOption.Map transferOptions) {
		List<EntityObjectVO> result = new ArrayList<EntityObjectVO>();
		Set<Long> distinctIds = new HashSet<Long>();

		if (!isEnabled())
			return result;
		if (transferOptions.containsKey(TransferOption.IS_NUCLOS_INSTANCE) || ignoreReferenceToNuclet)
			return NucletDalProvider.getInstance().getEntityObjectProcessor(entity).getAll();
		if (nucletIds == null || nucletIds.isEmpty())
			return result;

		boolean fieldToNucletExists = false;
		try {MetaDataServerProvider.getInstance().getEntityField(entity, FOREIGN_FIELD_TO_NUCLET); fieldToNucletExists = true;} catch (Exception ex) {}
		if (fieldToNucletExists) {
			// entity has reference to nuclet
			for (Long nucletId : nucletIds) {
				result.addAll(NucletDalProvider.getInstance().getEntityObjectProcessor(entity).getBySearchExpression(
					new CollectableSearchExpression(SearchConditionUtils.newEOidComparison(
						entity.getEntityName(), FOREIGN_FIELD_TO_NUCLET,
						ComparisonOperator.EQUAL, nucletId,
						MetaDataServerProvider.getInstance()))));
			}
		} else {
			// get nuclet references from parent(s)
			INucletContent ncParent = getContentType(contentTypes, parententity);
			for (EntityObjectVO parent : ncParent.getNcObjects(nucletIds, transferOptions)) {
				for (String fieldToParent : getForeignFieldsToParent(entity, parententity)) {
					EntityFieldMetaDataVO efMeta = MetaDataServerProvider.getInstance().getEntityField(entity, fieldToParent);
					if (efMeta.getForeignEntity() != null) {
						for (EntityObjectVO eo : NucletDalProvider.getInstance().getEntityObjectProcessor(entity).getBySearchExpression(
							new CollectableSearchExpression(SearchConditionUtils.newEOidComparison(
								entity.getEntityName(), efMeta.getField(),
								ComparisonOperator.EQUAL, parent.getId(),
								MetaDataServerProvider.getInstance())))) {
							if (!distinctIds.contains(eo.getId())) {
								distinctIds.add(eo.getId());
								result.add(eo);
							}
						}
					} else if (efMeta.getUnreferencedForeignEntity() != null) {
						for (EntityObjectVO eo : NucletDalProvider.getInstance().getEntityObjectProcessor(entity).getBySearchExpression(
							new CollectableSearchExpression(SearchConditionUtils.newEOComparison(
								entity.getEntityName(), efMeta.getField(),
								ComparisonOperator.EQUAL, parent.getFields().get(efMeta.getUnreferencedForeignEntityField()),
								MetaDataServerProvider.getInstance())))) {
							if (!distinctIds.contains(eo.getId())) {
								distinctIds.add(eo.getId());
								result.add(eo);
							}
						}
					} else {
						throw new IllegalArgumentException();
					}
				}
			}
		}
		return new ArrayList<EntityObjectVO>(CollectionUtils.distinct(result, new BinaryPredicate<EntityObjectVO, EntityObjectVO>() {
			@Override
			public boolean evaluate(EntityObjectVO t1, EntityObjectVO t2) {
				return LangUtils.equals(t1.getId(), t2.getId());
			}
		}));
	}

	@Override
	public void setNcObjectFieldNull(DalCallResult result, Long id, String field) {
		EntityObjectVO eo = NucletDalProvider.getInstance().getEntityObjectProcessor(entity).getByPrimaryKey(id);
		eo.getFieldIds().put(field, null);
		eo.getFields().put(field, null);
		eo.flagUpdate();
		DalUtils.updateVersionInformation(eo, eo.getChangedBy());
		try {
			NucletDalProvider.getInstance().getEntityObjectProcessor(entity).insertOrUpdate(eo);
		}
		catch (DbException e) {
			result.addBusinessException(e);
		}
	}

	@Override
	public void deleteNcObject(DalCallResult result, EntityObjectVO ncObject) {
		try {
			NucletDalProvider.getInstance().getEntityObjectProcessor(entity).delete(ncObject.getId());
		}
		catch (DbException e) {
			result.addBusinessException(e);
		}
	}

	@Override
	public void insertOrUpdateNcObject(DalCallResult result, EntityObjectVO ncObject, boolean isNuclon) throws SQLIntegrityConstraintViolationException{
		try {
			NucletDalProvider.getInstance().getEntityObjectProcessor(entity).insertOrUpdate(ncObject);
		}
		catch (DbException e) {
			if (e.getSqlCause() instanceof SQLIntegrityConstraintViolationException) {
				// Logical unique constraint violated, check later again
				throw (SQLIntegrityConstraintViolationException) e.getSqlCause();
			} else {
				result.addBusinessException(e);
			}
		}
	}

	@Override
	public void checkLogicalUnique(DalCallResult result, EntityObjectVO ncObject) {
		try {
			NucletDalProvider.getInstance().getEntityObjectProcessor(entity).checkLogicalUniqueConstraint(ncObject);
		}
		catch (DbException e) {
			result.addBusinessException(e);
		}
	}

	@Override
	public boolean validate(EntityObjectVO ncObject, ValidationType type, NucletContentMap importContentMap, NucletContentUID.Map uidMap, Set<Long> existingNucletIds, ValidityLogEntry log, TransferOption.Map transferOptions) {
		switch (type) {
			case INSERT:
			case UPDATE:
				boolean result = true;
//				if (transferOptions.containsKey(TransferOption.IS_NUCLOS_INSTANCE))
//					return result;

				Collection<Set<String>> uniqueFieldCombinations = TransferUtils.getAllUniqueFieldCombinations(entity.getEntityName());

				// check if unique constraint is violated
				List<EntityObjectVO> otherObjects = new ArrayList<EntityObjectVO>();
				for (EntityObjectVO eo : importContentMap.getValues(entity)) {
					if (!LangUtils.equals(ncObject.getId(), eo.getId()))
						otherObjects.add(eo);
				}
				for (EntityObjectVO eo : getDbContent()) {
					if (type == ValidationType.INSERT || (type == ValidationType.UPDATE && !LangUtils.equals(ncObject.getId(), eo.getId()))) {
						if (uidMap.getUID(eo) == null) { // otherwise belongs to nuclet
							otherObjects.add(eo);
						}
					}
				}
				for (Set<String> uniqueCombination : uniqueFieldCombinations) {
					for (EntityObjectVO eo : otherObjects) {
						int countUnique = 0;
						for (String field : uniqueCombination) {
							EntityFieldMetaDataVO efMeta = MetaDataServerProvider.getInstance().getEntityField(entity.getEntityName(), field);
							if (efMeta.getForeignEntity() == null) {
								if (NuclosEOField.CREATEDBY.getName().equals(field)) {
									if (LangUtils.equalsNullsFalse(eo.getCreatedBy(), ncObject.getCreatedBy()))
										countUnique++;
								} else {
									if (LangUtils.equalsNullsFalse(eo.getFields().get(field), ncObject.getFields().get(field)))
										countUnique++;
								}
							} else {
								if (LangUtils.equalsNullsFalse(eo.getFieldIds().get(field), ncObject.getFieldIds().get(field)))
									countUnique++;
							}
						}
						if (countUnique == uniqueCombination.size()) {
							result = false;
							final String logMessage = "Ignoring " + entity.getEntityName() + ": " + getIdentifier(ncObject) + " --> Unique constraint violated";
							warn(logMessage);
							if (hasNameIdentifier(ncObject)) {
								log.newWarningLine(logMessage);
							}
						}
					}
				}

				// check foreign keys
				if (result == true) {
					for (EntityFieldMetaDataVO efMeta : getForeignFields(entity)) {
						boolean isUniquePart = false;
						for (Set<String> uniqueCombination : uniqueFieldCombinations)
							if (uniqueCombination.contains(efMeta.getField())) isUniquePart = true;

						if (efMeta.getForeignEntity() != null) {
							//referenced field
							Long validateId = ncObject.getFieldIds().get(efMeta.getField());
							if (validateId != null && getEntityObjectVO(importContentMap.getValues(NuclosEntity.getByName(efMeta.getForeignEntity())), validateId) == null) {
								if (efMeta.isNullable() && !isUniquePart) {
									final String logMessage = "Removing reference from " + entity.getEntityName() + ": " + getIdentifier(ncObject) + " --> Unknown " + efMeta.getForeignEntity() + (ncObject.getField(efMeta.getField())==null? (": ID="+validateId): (" \""+ncObject.getField(efMeta.getField())+"\""));
									ncObject.getFieldIds().remove(efMeta.getField());
									ncObject.getFields().remove(efMeta.getField());
									warn(logMessage);
									if (hasNameIdentifier(ncObject)) {
										log.newWarningLine(logMessage);
									}
								} else {
									result = false;
									final String logMessage = "Ignoring " + entity.getEntityName() + ": " + getIdentifier(ncObject) + " --> References to unknown " + efMeta.getForeignEntity() + ": ID=" + validateId;
									warn(logMessage);
									if (hasNameIdentifier(ncObject)) {
										log.newWarningLine(logMessage);
									}
								}
							}
						} else {
							//unreferenced field
							String validateReference = ncObject.getField(efMeta.getField(), String.class);
							if (validateReference != null &&
								!NuclosEntity.isNuclosEntity(validateReference) &&
								getEntityObjectVO(importContentMap.getValues(NuclosEntity.getByName(efMeta.getUnreferencedForeignEntity())), 
									efMeta.getUnreferencedForeignEntityField(), validateReference) == null &&
								!getAdditionalValuesForUnreferencedForeignCheck(efMeta, importContentMap).contains(validateReference)) {
								if (efMeta.isNullable() && !isUniquePart) {
									ncObject.getFields().remove(efMeta.getField());
									final String logMessage = "Removing reference from " + entity.getEntityName() + ": " + getIdentifier(ncObject) + " --> Unknown " + efMeta.getUnreferencedForeignEntity() + ": \"" + validateReference + "\"";
									warn(logMessage);
									if (hasNameIdentifier(ncObject)) {
										log.newWarningLine(logMessage);
									}
								} else {
									result = false;
									final String logMessage = "Ignoring " + entity.getEntityName() + ": " + getIdentifier(ncObject) + " --> References to unknown " + efMeta.getUnreferencedForeignEntity() + ": \"" + validateReference + "\""; 
									warn(logMessage);
									if (hasNameIdentifier(ncObject)) {
										log.newWarningLine(logMessage);
									}
								}
							}
						}
					}
				}

				return result;
			default:
				return true;
		}
	}
	
	@Override
	public boolean removeReference(EntityObjectVO ncObject, EntityFieldMetaDataVO efMeta) {
		boolean remove = true;
		
		if (!efMeta.isNullable()) {
				remove = false;
		}
		
		Collection<Set<String>> uniqueFieldCombinations = TransferUtils.getAllUniqueFieldCombinations(entity.getEntityName());
		for (Set<String> uniqueCombination : uniqueFieldCombinations) {
			if (uniqueCombination.contains(efMeta.getField())) {
				remove = false;
				break;
			}
		}
		
		if (remove) {
			Long fieldIdValue = ncObject.getFieldIds().get(efMeta.getField());
			Object fieldValue = ncObject.getFields().get(efMeta.getField());
			
			ncObject.getFieldIds().remove(efMeta.getField());
			ncObject.getFields().remove(efMeta.getField());
			final String logMessage = "Removing reference from " + entity.getEntityName() + ": " + getIdentifier(ncObject) + " --> Field " + efMeta.getField() + " is referencing on deleted content";
			warn(logMessage);
			
			try {
				NucletDalProvider.getInstance().getEntityObjectProcessor(entity).insertOrUpdate(ncObject);
			}
			catch (DbException e) {
				error(e.getMessage(), e);
				ncObject.getFieldIds().put(efMeta.getField(), fieldIdValue);
				ncObject.getFields().put(efMeta.getField(), fieldValue);
				remove = false;
			}
		}
		
		return remove;
	}
	
	/**
	 * 
	 * @param String unreferencedForeignEntity
	 * @param String unreferencedForeignEntityField
	 * @param NucletContentMap importContentMap
	 * @return 
	 */
	private Collection<String> getAdditionalValuesForUnreferencedForeignCheck(EntityFieldMetaDataVO efMeta, NucletContentMap importContentMap) {
		for (INucletContent nc : contentTypes) {
			if (nc.getEntity().getEntityName().equals(efMeta.getUnreferencedForeignEntity())) 
				return nc.getAdditionalValuesForUnreferencedForeignCheck(efMeta.getUnreferencedForeignEntityField(), importContentMap);
		}
		
		return Collections.emptyList();
	}
	
	/**
	 * 
	 * @param String unreferencedForeignEntityField
	 * @param NucletContentMap importContentMap
	 * @return 
	 */
	@Override
	public Collection<String> getAdditionalValuesForUnreferencedForeignCheck(String unreferencedForeignEntityField, NucletContentMap importContentMap) {
		return Collections.emptyList();
	}

	/**
	 *
	 * @param ncObject
	 * @param fields
	 */
	protected void storeLocaleResources(EntityObjectVO ncObject, String...fields) {
		LocaleFacadeLocal localeFacade = ServerServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class);
		Collection<LocaleInfo> locales = localeFacade.getAllLocales(false);

		Map<LocaleInfo, Map<String, String>> localeResources = new HashMap<LocaleInfo, Map<String, String>>();
		for (LocaleInfo localeInfo : locales) {
			Map<String, String> resourceMap = new HashMap<String, String>();
			for (int i = 0; i < fields.length; i++) {
				resourceMap.put(fields[i], localeFacade.getResourceById(localeInfo, ncObject.getField(fields[i], String.class)));
			}
			localeResources.put(localeInfo, resourceMap);
		}
		ncObject.getFields().put(LOCALE_RESOURCE_MAPPING_FIELD_NAME, localeResources);
	}

	/**
	 *
	 * @param ncObject
	 * @param fields
	 */
	protected void restoreLocaleResources(EntityObjectVO ncObject) {
		LocaleFacadeLocal localeFacade = ServerServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class);

		Map<LocaleInfo, Map<String, String>> localeResources = (Map<LocaleInfo, Map<String, String>>) ncObject.getFields().get(LOCALE_RESOURCE_MAPPING_FIELD_NAME);
		if (localeResources != null) {
			Map<String, String> newResourceIds = new HashMap<String, String>();
			for (LocaleInfo localeInfo : localeResources.keySet()) {
				for (String resourceField : localeResources.get(localeInfo).keySet()) {
					String text = localeResources.get(localeInfo).get(resourceField);
					if (text != null) {
						if (ncObject.isFlagNew()) {
							// create new resource
							String resourceId = newResourceIds.get(resourceField);
							resourceId = localeFacade.insert(resourceId, localeInfo, text);
							newResourceIds.put(resourceField, resourceId);
							ncObject.getFields().put(resourceField, resourceId);
						} else if (ncObject.isFlagUpdated()) {
							// read resourceid from existing object and update it
							EntityObjectVO existingObject = NucletDalProvider.getInstance().getEntityObjectProcessor(entity).getByPrimaryKey(ncObject.getId());
							String resourceId = existingObject.getField(resourceField, String.class);
							if (resourceId != null) {
								if (localeFacade.getResourceById(localeInfo, resourceId) == null) {
									localeFacade.insert(resourceId, localeInfo, text);
								} else {
									localeFacade.update(resourceId, localeInfo, text);
								}
								
							} else {
								resourceId = newResourceIds.get(resourceField);
								resourceId = localeFacade.insert(resourceId, localeInfo, text);
								newResourceIds.put(resourceField, resourceId);
							}
							ncObject.getFields().put(resourceField, resourceId);
						}
					}
				}
		}
		}
	}
	
	@Override
	public List<EntityObjectVO> getDbContent() {
		if (dbContent == null) {
			dbContent = NucletDalProvider.getInstance().getEntityObjectProcessor(entity).getAll();
		}
		return dbContent;
	}
	
	@Override
	public void clearDbContent() {
		dbContent = null;
	}

	@Override
	public String toString() {
		return entity.getEntityName();
	}

	public Logger getLogger() {
		if (this.log == null)
			this.log = Logger.getLogger(this.getClass());
		return this.log;
	}

	protected void debug(Object o) {
		this.log(Level.DEBUG, o);
	}

	protected void info(Object o) {
		this.log(Level.INFO, o);
	}

	protected void warn(Object o) {
		this.log(Level.WARN, o);
	}

	protected void error(Object o) {
		this.log(Level.ERROR, o);
	}
	
	protected void error(Object o, Throwable t) {
		this.log(Level.ERROR, o, t);
	}

	protected void fatal(Object o) {
		this.log(Level.FATAL, o);
	}

	protected void log(Priority priority, Object oMessage, Throwable t) {
		this.getLogger().log(priority, oMessage, t);
	}

	protected void log(Priority priority, Object oMessage) {
		this.getLogger().log(priority, oMessage);
	}

}
