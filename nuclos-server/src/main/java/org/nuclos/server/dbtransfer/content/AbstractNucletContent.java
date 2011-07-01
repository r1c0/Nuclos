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
import static org.nuclos.server.dbtransfer.TransferUtils.getForeignFieldToParent;
import static org.nuclos.server.dbtransfer.TransferUtils.getIdentifier;
import static org.nuclos.server.dbtransfer.TransferUtils.getForeignFields;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.dal.DalCallResult;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dbtransfer.NucletContentUID;
import org.nuclos.common.dbtransfer.TransferOption;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.LocaleInfo;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.ejb3.LocaleFacadeLocal;
import org.nuclos.server.dal.DalUtils;
import org.nuclos.server.dal.provider.NucletDalProvider;
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
			for (EntityObjectVO uidObject : NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.NUCLETCONTENTUID).getBySearchExpression(
				new CollectableSearchExpression(SearchConditionUtils.and(
					SearchConditionUtils.newEOComparison(
						NuclosEntity.NUCLETCONTENTUID.getEntityName(), "nuclosentity",
						ComparisonOperator.EQUAL, entity.getEntityName(),
						MetaDataServerProvider.getInstance()),
					SearchConditionUtils.newEOComparison(
						NuclosEntity.NUCLETCONTENTUID.getEntityName(), "objectid",
						ComparisonOperator.EQUAL, ncObject.getId(),
						MetaDataServerProvider.getInstance()))))) {
				result.add(uidObject);
			}
		}
		return result;
	}

	@Override
	public List<EntityObjectVO> getNcObjects(Set<Long> nucletIds, TransferOption.Map transferOptions) {
		List<EntityObjectVO> result = new ArrayList<EntityObjectVO>();

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
				EntityFieldMetaDataVO efMeta = MetaDataServerProvider.getInstance().getEntityField(entity, getForeignFieldToParent(entity, parententity));
				if (efMeta.getForeignEntity() != null) {
					result.addAll(NucletDalProvider.getInstance().getEntityObjectProcessor(entity).getBySearchExpression(
						new CollectableSearchExpression(SearchConditionUtils.newEOidComparison(
							entity.getEntityName(), getForeignFieldToParent(entity, parententity),
							ComparisonOperator.EQUAL, parent.getId(),
							MetaDataServerProvider.getInstance()))));
				} else if (efMeta.getUnreferencedForeignEntity() != null) {
					result.addAll(NucletDalProvider.getInstance().getEntityObjectProcessor(entity).getBySearchExpression(
						new CollectableSearchExpression(SearchConditionUtils.newEOComparison(
							entity.getEntityName(), getForeignFieldToParent(entity, parententity),
							ComparisonOperator.EQUAL, parent.getFields().get(efMeta.getUnreferencedForeignEntityField()),
							MetaDataServerProvider.getInstance()))));
				} else {
					throw new IllegalArgumentException();
				}
			}
		}
		return result;
	}

	@Override
	public List<DalCallResult> setNcObjectFieldNull(Long id, String field) {
		EntityObjectVO eo = NucletDalProvider.getInstance().getEntityObjectProcessor(entity).getByPrimaryKey(id);
		eo.getFieldIds().put(field, null);
		eo.getFields().put(field, null);
		eo.flagUpdate();
		DalUtils.updateVersionInformation(eo, eo.getChangedBy());
		return Collections.singletonList(NucletDalProvider.getInstance().getEntityObjectProcessor(entity).insertOrUpdate(eo));
	}

	@Override
	public List<DalCallResult> deleteNcObject(Long id) {
		return Collections.singletonList(NucletDalProvider.getInstance().getEntityObjectProcessor(entity).delete(id));
	}

	@Override
	public List<DalCallResult> insertOrUpdateNcObject(EntityObjectVO ncObject, boolean isNuclon) {
		return Collections.singletonList(NucletDalProvider.getInstance().getEntityObjectProcessor(entity).insertOrUpdate(ncObject));
	}

	@Override
	public boolean checkValidity(EntityObjectVO ncObject, ValidityType validity, NucletContentMap importContentMap, Set<Long> existingNucletIds, ValidityLogEntry log, TransferOption.Map transferOptions) {
		switch (validity) {
			case INSERT:
			case UPDATE:
				boolean result = true;
				if (transferOptions.containsKey(TransferOption.IS_NUCLOS_INSTANCE))
					return result;

				Collection<Set<String>> uniqueFieldCombinations = TransferUtils.getAllUniqueFieldCombinations(entity.getEntityName());

				// check if unique constraint is violated
				List<EntityObjectVO> otherObjects = new ArrayList<EntityObjectVO>();
				for (EntityObjectVO eo : importContentMap.getValues(entity)) {
					if (!LangUtils.equals(ncObject.getId(), eo.getId()))
						otherObjects.add(eo);
				}
				for (EntityObjectVO eo : NucletDalProvider.getInstance().getEntityObjectProcessor(entity).getAll()) {
					if (validity == ValidityType.INSERT || (validity == ValidityType.UPDATE && !LangUtils.equals(ncObject.getId(), eo.getId())))
						otherObjects.add(eo);
				}
				for (Set<String> uniqueCombination : uniqueFieldCombinations) {
					for (EntityObjectVO eo : otherObjects) {
						int countUnique = 0;
						for (String field : uniqueCombination) {
							EntityFieldMetaDataVO efMeta = MetaDataServerProvider.getInstance().getEntityField(entity.getEntityName(), field);
							if (efMeta.getForeignEntity() == null) {
								if (LangUtils.equals(eo.getFields().get(field), ncObject.getFields().get(field)))
									countUnique++;
							} else {
								if (LangUtils.equals(eo.getFieldIds().get(field), ncObject.getFieldIds().get(field)))
									countUnique++;
							}
						}
						if (countUnique == uniqueCombination.size()) {
							result = false;
							log.newWarningLine("Ignoring " + entity.getEntityName() + ": " + getIdentifier(this, ncObject) + " --> Unique constraint violated");
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
									ncObject.getFieldIds().remove(efMeta.getField());
									ncObject.getFields().remove(efMeta.getField());
									log.newWarningLine("Removing reference from " + entity.getEntityName() + ": " + getIdentifier(this, ncObject) + " --> Unknown " + efMeta.getForeignEntity() + ": ID=" + validateId);
								} else {
									result = false;
									log.newWarningLine("Ignoring " + entity.getEntityName() + ": " + getIdentifier(this, ncObject) + " --> References to unknown " + efMeta.getForeignEntity() + ": ID=" + validateId);
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
									log.newWarningLine("Removing reference from " + entity.getEntityName() + ": " + getIdentifier(this, ncObject) + " --> Unknown " + efMeta.getUnreferencedForeignEntity() + ": \"" + validateReference + "\"");
								} else {
									result = false;
									log.newWarningLine("Ignoring " + entity.getEntityName() + ": " + getIdentifier(this, ncObject) + " --> References to unknown " + efMeta.getUnreferencedForeignEntity() + ": \"" + validateReference + "\"");
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
		LocaleFacadeLocal localeFacade = ServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class);
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
		LocaleFacadeLocal localeFacade = ServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class);

		@SuppressWarnings("unchecked")
		Map<LocaleInfo, Map<String, String>> localeResources = (Map<LocaleInfo, Map<String, String>>) ncObject.getFields().get(LOCALE_RESOURCE_MAPPING_FIELD_NAME);
		for (LocaleInfo localeInfo : localeResources.keySet()) {
			for (String resourceField : localeResources.get(localeInfo).keySet()) {
				String text = localeResources.get(localeInfo).get(resourceField);
				if (text != null) {
					if (ncObject.isFlagNew()) {
						// create new resource
						String resourceId = localeFacade.insert(null, localeInfo, text);
						ncObject.getFields().put(resourceField, resourceId);
					} else if (ncObject.isFlagUpdated()) {
						// read resourceid from existing object and update it
						EntityObjectVO existingObject = NucletDalProvider.getInstance().getEntityObjectProcessor(entity).getByPrimaryKey(ncObject.getId());
						String resourceId = existingObject.getField(resourceField, String.class);
						localeFacade.update(resourceId, localeInfo, text);
						ncObject.getFields().put(resourceField, resourceId);
					}
				}
			}
		}
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
