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
package org.nuclos.server.dbtransfer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dbtransfer.NucletContentUID;
import org.nuclos.common.dbtransfer.Transfer;
import org.nuclos.common.dbtransfer.TransferOption;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.dal.DalUtils;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.dbtransfer.content.AbstractNucletContent;
import org.nuclos.server.dbtransfer.content.INucletContent;
import org.nuclos.server.dbtransfer.content.ValidationType;
import org.nuclos.server.dbtransfer.content.ValidityLogEntry;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;

public class TransferUtils {

	/**
	 *
	 * @param Set<EntityFieldMetaDataVO> fields
	 * @return
	 */
	public static Collection<EntityFieldMetaDataVO> getUserEntityFields(Collection<EntityFieldMetaDataVO> fields) {
		return CollectionUtils.select(fields, new UserEntityFieldPredicate());
	}

	/**
	 *
	 * @param NuclosEntity entity
	 * @return
	 */
	public static EntityFieldMetaDataVO getForeignFieldToNuclet(NuclosEntity entity) {
		try {
			EntityFieldMetaDataVO efMeta = MetaDataServerProvider.getInstance().getEntityField(entity.getEntityName(), AbstractNucletContent.FOREIGN_FIELD_TO_NUCLET);
			if (LangUtils.equals(efMeta.getForeignEntity(), NuclosEntity.NUCLET.getEntityName())) {
				return efMeta;
			} else {
				throw new IllegalArgumentException();
			}
		} catch (Exception ex) {
			throw new IllegalArgumentException("Entity has no foreign field to nuclet");
		}
	}

	/**
	 *
	 * @param NuclosEntity entity
	 * @param NuclosEntity parententity
	 * @return
	 */
	public static String getForeignFieldToParent(NuclosEntity entity, NuclosEntity parententity) {
		for (EntityFieldMetaDataVO efMeta : MetaDataServerProvider.getInstance().getAllEntityFieldsByEntity(entity.getEntityName()).values()) {
			if (LangUtils.equals(parententity.getEntityName(), efMeta.getForeignEntity())) {
				return efMeta.getField();
			}
			else if (LangUtils.equals(parententity.getEntityName(), efMeta.getUnreferencedForeignEntity())) {
				return efMeta.getField();
			}
		}
		throw new NuclosFatalException("Foreign field to parent not found");
	}

	/**
	 *
	 * @param Set<EntityMetaDataVO> meta
	 * @param NuclosEntity entity
	 * @return
	 */
	public static boolean entityMetaContainsNuclosEntity(Set<EntityMetaDataVO> meta, NuclosEntity entity) {
		for (EntityMetaDataVO eMeta : meta) {
			if (LangUtils.equals(entity.getEntityName(), eMeta.getEntity()))
				return true;
		}
		return false;
	}

	/**
	 *
	 * @param Set<EntityFieldMetaDataVO> meta
	 * @param NuclosEntity entity
	 * @return
	 */
	public static boolean entityFieldMetaContainsNuclosEntity(Collection<EntityFieldMetaDataVO> meta, NuclosEntity entity) {
		for (EntityFieldMetaDataVO efMeta : meta) {
			if (LangUtils.equals(entity.getEntityName(), MetaDataServerProvider.getInstance().getEntity(efMeta.getEntityId()).getEntity()))
				return true;
		}
		return false;
	}

	/**
	 *
	 * @param NuclosEntity entity
	 * @return
	 */
	public static Collection<EntityFieldMetaDataVO> getFieldDependencies(NuclosEntity entity) {
		Collection<EntityFieldMetaDataVO> result = new ArrayList<EntityFieldMetaDataVO>();
		for (EntityMetaDataVO eMeta : MetaDataServerProvider.getInstance().getAllEntities()) {
			for (EntityFieldMetaDataVO efMeta : MetaDataServerProvider.getInstance().getAllEntityFieldsByEntity(eMeta.getEntity()).values()) {
				if (entity.getEntityName().equals(efMeta.getForeignEntity()) ||
					entity.getEntityName().equals(efMeta.getUnreferencedForeignEntity())) {
					result.add(efMeta);
				}
			}
		}
		return result;
	}

	/**
	 *
	 * @param NuclosEntity entity
	 * @return
	 */
	public static Set<EntityMetaDataVO> getForeignEntities(NuclosEntity entity) {
		Set<EntityMetaDataVO> result = new HashSet<EntityMetaDataVO>();
		for (EntityFieldMetaDataVO efMeta : MetaDataServerProvider.getInstance().getAllEntityFieldsByEntity(entity.getEntityName()).values()) {
			if (!StringUtils.looksEmpty(efMeta.getForeignEntity())) {
				result.add(MetaDataServerProvider.getInstance().getEntity(efMeta.getForeignEntity()));
			}
			if (!StringUtils.looksEmpty(efMeta.getUnreferencedForeignEntity())) {
				result.add(MetaDataServerProvider.getInstance().getEntity(efMeta.getUnreferencedForeignEntity()));
			}
		}
		return result;
	}

	/**
	 *
	 * @param NuclosEntity entity
	 * @return
	 */
	public static Set<EntityFieldMetaDataVO> getForeignFields(NuclosEntity entity) {
		Set<EntityFieldMetaDataVO> result = new HashSet<EntityFieldMetaDataVO>();
		for (EntityFieldMetaDataVO efMeta : MetaDataServerProvider.getInstance().getAllEntityFieldsByEntity(entity.getEntityName()).values()) {
			if (!StringUtils.looksEmpty(efMeta.getForeignEntity())) {
				result.add(efMeta);
			}
			if (!StringUtils.looksEmpty(efMeta.getUnreferencedForeignEntity())) {
				result.add(efMeta);
			}
		}
		return result;
	}

	/**
	 *
	 * @param INucletContent nc
	 * @param EntityObjectVO eo
	 * @return
	 */
	public static String getIdentifier(INucletContent nc, EntityObjectVO eo) {
		Object ident = eo.getFields().get(nc.getIdentifierField());
		if (ident == null) {
			return "ID="+LangUtils.defaultIfNull(getOriginId(eo), eo.getId());
		} else {
			return "\""+ident.toString()+"\"";
		}
	}

	/**
	 *
	 * @param eo
	 * @return
	 */
	public static Long getOriginId(EntityObjectVO eo) {
		return eo.getField(NucletContentMap.ORIGIN_ID_FIELD, Long.class);
	}

	/**
	 *
	 * @param EntityFieldMetaDataVO efMeta
	 * @return
	 */
	public static String getEntity(EntityFieldMetaDataVO efMeta) {
		return MetaDataServerProvider.getInstance().getEntity(efMeta.getEntityId()).getEntity();
	}

	/**
	 *
	 * @param EntityObjectVO eo
	 * @return
	 */
	public static NuclosEntity getEntity(EntityObjectVO eo) {
		return NuclosEntity.getByName(eo.getEntity());
	}

	/**
	 *
	 * @param String entity
	 * @param Long id
	 * @return
	 */
	public static EntityObjectVO getDummyEntityObject(String entity, Long id) {
		EntityObjectVO result = new EntityObjectVO();
		result.setEntity(entity);
		result.setId(id);
		return result;
	}

	/**
	 *
	 * @param NucletContentMap ncObjects
	 * @return
	 */
	public static Set<Long> getNucletIds(NucletContentMap ncObjects) {
		return getIds(ncObjects.getValues(NuclosEntity.NUCLET));
	}

	/**
	 *
	 * @param Collection<EntityObjectVO> eos
	 * @return
	 */
	public static Set<Long> getIds(Collection<EntityObjectVO> eos) {
		if (eos == null)
			return Collections.emptySet();
		return CollectionUtils.transformIntoSet(eos, new IdTransformer());
	}

	public static String getEntityFieldPresentations(Collection<EntityFieldMetaDataVO> fields) {
		StringBuffer sbResult = new StringBuffer();
		sbResult.append('[');
		for (EntityFieldMetaDataVO efMeta : fields) {
			sbResult.append(MetaDataServerProvider.getInstance().getEntity(efMeta.getEntityId()).getEntity());
			sbResult.append('.');
			sbResult.append(efMeta.getField());
			sbResult.append(", ");
		}
		sbResult.append(']');
		return sbResult.toString();
	}

	/**
	 *
	 * @param contentTypes
	 * @return
	 */
	public static List<String> getEntities(List<INucletContent> contentTypes) {
		List<String> result = new ArrayList<String>();
		for (INucletContent ncType : contentTypes)
			result.add(ncType.getEntity().getEntityName());
		return result;
	}

	/**
	 *
	 * @param NucletContentMap contentMap
	 * @param List<INucletContent> contentTypes
	 * @param INucletContent targetType
	 * @param EntityObjectVO targetObject
	 * @return
	 */
	public static NucletContentMap getDependencies(NucletContentMap contentMap, List<INucletContent> contentTypes, INucletContent targetType, EntityObjectVO targetObject) {
		NucletContentMap result = new NucletContentHashMap();

		for(EntityFieldMetaDataVO efMeta : targetType.getFieldDependencies()) {
			NuclosEntity entity = NuclosEntity.getByName(getEntity(efMeta));
			if (entity != null) {
				for(EntityObjectVO eo : contentMap.getValues(entity)) {
					if (targetObject == null) {
						result.add(eo);
						result.addAll(getDependencies(contentMap, contentTypes, getContentType(contentTypes, entity), eo));
					} else {
						if (efMeta.getForeignEntity() != null) {
							if (LangUtils.equals(targetObject.getId(), eo.getFieldId(efMeta.getField()))) {
								result.add(eo);
								result.addAll(getDependencies(contentMap, contentTypes, getContentType(contentTypes, entity), eo));
							}
						}
						if (efMeta.getUnreferencedForeignEntity() != null) {
							if (LangUtils.equals(
								targetObject.getFields().get(efMeta.getUnreferencedForeignEntityField()), eo.getFields().get(efMeta.getField()))) {
								result.add(eo);
								result.addAll(getDependencies(contentMap, contentTypes, getContentType(contentTypes, entity), eo));
							}
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 *
	 * @param contentTypes
	 * @param entity
	 * @return
	 */
	public static INucletContent getContentType(List<INucletContent> contentTypes, NuclosEntity entity) {
		for (INucletContent nc : contentTypes) {
			if (nc.getEntity() == entity) {
				return nc;
			}
		}
		throw new NuclosFatalException("No content type for entity \"" + entity.getEntityName() + "\" found");
	}

	/**
	 *
	 * @param nc
	 * @param ncObject
	 * @param validity
	 * @param importContentMap
	 * @param existingNucletIds
	 * @param result
	 * @return
	 */
	public static boolean validate(INucletContent nc, EntityObjectVO ncObject, ValidationType validity, NucletContentMap importContentMap, NucletContentUID.Map uidMap, Set<Long> existingNucletIds, TransferOption.Map transferOptions, Transfer.Result result) {
		ValidityLogEntry log = new ValidityLogEntry();
		boolean isValid = nc.validate(ncObject, validity, importContentMap, uidMap, existingNucletIds, log, transferOptions);
		if (log.sbWarning.length() > 0) {
			result.addWarning(log.sbWarning);
		}
		if (log.sbCritical.length() > 0) {
			result.addCritical(log.sbCritical);
		}
		return isValid;
	}

	/**
	 *
	 * @param eos
	 * @param id
	 * @return
	 */
	public static EntityObjectVO getEntityObjectVO(List<EntityObjectVO> eos, Long id) {
		List<EntityObjectVO> result = CollectionUtils.select(eos, new EntityObjectIdPredicate(id));
		return result.isEmpty() ? null : result.get(0);
	}

	/**
	 *
	 * @param eos
	 * @param field
	 * @param value
	 * @return
	 */
	public static EntityObjectVO getEntityObjectVO(List<EntityObjectVO> eos, String field, String value) {
		List<EntityObjectVO> result = CollectionUtils.select(eos, new EntityObjectFieldValuePredicate(field, value));
		return result.isEmpty() ? null : result.get(0);
	}
	
	/**
	 * 
	 * @param eos
	 * @param field
	 * @return
	 */
	public static Collection<String> getStringValues(List<EntityObjectVO> eos, String field) {
		Collection<String> result = new ArrayList<String>();
		for (EntityObjectVO eo :eos) {
			result.add(eo.getField(field, String.class));
		}
		return result;
	}

	/**
	 *
	 * @param entity
	 * @return
	 */
	public static Collection<Set<String>> getAllUniqueFieldCombinations(String entity) {
		Collection<Set<String>> result = new ArrayList<Set<String>>();
		result.addAll(MetaDataServerProvider.getInstance().getEntity(entity).getUniqueFieldCombinations());
		result.addAll(MetaDataServerProvider.getInstance().getEntity(entity).getLogicalUniqueFieldCombinations());
		if (result.isEmpty()) {
			// legacy support for unique flag in fields
			Set<String> legacyFieldSet = new HashSet<String>();
			for (EntityFieldMetaDataVO efMeta : MetaDataServerProvider.getInstance().getAllEntityFieldsByEntity(entity).values()) {
				if (efMeta.isUnique())
					legacyFieldSet.add(efMeta.getField());
			}
			if (!legacyFieldSet.isEmpty())
				result.add(legacyFieldSet);
		}
		return result;
	}

	/**
	 *
	 * @param contentTypes
	 * @param entity
	 * @return
	 */
	public static INucletContent getNucletContent(List<INucletContent> contentTypes, NuclosEntity entity) {
		for (INucletContent nc : contentTypes) {
			if (nc.getEntity() == entity)
				return nc;
		}
		throw new NuclosFatalException("no content type for entity " + entity.getEntityName());
	}

	public static class NucletDependenceTransformer implements Transformer<EntityObjectVO, Long> {
		@Override
		public Long transform(EntityObjectVO i) {
			return i.getFieldId("nucletDependence");
		}
	}
	
	public static EntityObjectVO getUID(NuclosEntity entity, Long objectId) {
		for (EntityObjectVO uidObject : NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.NUCLETCONTENTUID).getBySearchExpression(
				new CollectableSearchExpression(SearchConditionUtils.and(
					SearchConditionUtils.newEOComparison(
						NuclosEntity.NUCLETCONTENTUID.getEntityName(), "nuclosentity",
						ComparisonOperator.EQUAL, entity.getEntityName(),
						MetaDataServerProvider.getInstance()),
					SearchConditionUtils.newEOComparison(
						NuclosEntity.NUCLETCONTENTUID.getEntityName(), "objectid",
						ComparisonOperator.EQUAL, objectId,
						MetaDataServerProvider.getInstance()))))) {
				return uidObject;
			}
		return null;
	}
	
	/**
	 *
	 * @param NucletContentUID uid
	 * @param NuclosEntity entity
	 * @param Long objectId
	 * @return
	 */
	public static EntityObjectVO createUIDObject(NucletContentUID uid, NuclosEntity entity, Long objectId) {
		if (uid == null) {
			throw new IllegalArgumentException("UID must not be null");
		}
		if (entity == null) {
			throw new IllegalArgumentException("entity must not be null");
		}
		if (objectId == null) {
			throw new IllegalArgumentException("objectId must not be null");
		}
		if (uid.uid == null) {
			throw new IllegalArgumentException("UID.uid must not be null");
		}
		if (uid.version == null) {
			throw new IllegalArgumentException("UID.version must not be null");
		}
		if (uid.id != null) {
			throw new IllegalArgumentException("UID.id != null");
		}
		EntityObjectVO result = new EntityObjectVO();
		result.setEntity(NuclosEntity.NUCLETCONTENTUID.getEntityName());
		result.initFields(4, 0);
		result.getFields().put("uid", uid.uid);
		result.getFields().put("nuclosentity", entity.getEntityName());
		result.getFields().put("objectid", objectId);
		result.getFields().put("objectversion", uid.version);
		result.flagNew();
		result.setId(DalUtils.getNextId());
		DalUtils.updateVersionInformation(result, "superuser");
		return result;
	}
	
	/**
	 *
	 * @param EntityObjectVO ncObject
	 * @return
	 */
	public static EntityObjectVO createUIDRecordForNcObject(EntityObjectVO ncObject) {
		EntityObjectVO uidObject = createUIDObject(new NucletContentUID(ncObject), NuclosEntity.getByName(ncObject.getEntity()), ncObject.getId());
		NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.NUCLETCONTENTUID).insertOrUpdate(uidObject);
		return uidObject;
	}

	/**
	 *
	 * @param NucletContentUID uid
	 * @param NuclosEntity entity
	 * @param Long objectId
	 * @param Integer objectVersion
	 * @return
	 */
	public static void createUIDRecord(NucletContentUID uid, NuclosEntity entity, Long objectId) {
		NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.NUCLETCONTENTUID).insertOrUpdate(createUIDObject(uid, entity, objectId));
	}
	
	/**
	 *
	 * @param Long id
	 * @param Integer version
	 * @return
	 */
	public static void updateUIDRecord(Long id, Integer version) {
		if (id == null) {
			throw new IllegalArgumentException("id must not be null");
		}
		if (version == null) {
			throw new IllegalArgumentException("version must not be null");
		}
		EntityObjectVO uidEO = NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.NUCLETCONTENTUID).getByPrimaryKey(id);
		uidEO.getFields().put("objectversion", version);
		uidEO.flagUpdate();
		DalUtils.updateVersionInformation(uidEO, "superuser");
		NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.NUCLETCONTENTUID).insertOrUpdate(uidEO);
	}
	
	/**
	 *
	 * @param NuclosEntity entity
	 * @param String uid
	 * @return
	 */
	public static Long getNcObjectIdFromNucletContentUID(NuclosEntity entity, String uid) {
		List<EntityObjectVO> result = NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.NUCLETCONTENTUID).getBySearchExpression(new CollectableSearchExpression(SearchConditionUtils.and(
			SearchConditionUtils.newEOComparison(
				NuclosEntity.NUCLETCONTENTUID.getEntityName(), "nuclosentity",
				ComparisonOperator.EQUAL, entity.getEntityName(),
				MetaDataServerProvider.getInstance()),
			SearchConditionUtils.newEOComparison(
				NuclosEntity.NUCLETCONTENTUID.getEntityName(), "uid",
				ComparisonOperator.EQUAL, uid,
				MetaDataServerProvider.getInstance()))));

		switch (result.size()) {
		case 0: return null;
		case 1: return result.get(0).getField("objectid", Long.class);
		default: throw new NuclosFatalException("Nuclet content UID is not unique [" + entity.getEntityName() + ", " + uid + "]");
		}
	}

	private static class IdTransformer implements Transformer<EntityObjectVO, Long> {
		@Override
		public Long transform(EntityObjectVO i) {
			return i.getId();
		}
	}

	private static class EntityObjectFieldValuePredicate implements Predicate<EntityObjectVO> {
		private final String field;
		private final String value;
		public EntityObjectFieldValuePredicate(String field, String value) {
			this.field = field;
			this.value = value;
		}
		@Override
		public boolean evaluate(EntityObjectVO t) {
			return LangUtils.equals(t.getField(field, String.class), value);
		}
	}

	private static class EntityObjectIdPredicate implements Predicate<EntityObjectVO> {
		private final Long id;
		public EntityObjectIdPredicate(Long id) {
			this.id = id;
		}
		@Override
		public boolean evaluate(EntityObjectVO t) {
			return LangUtils.equals(t.getId(), id);
		}
	}

	private static class UserEntityFieldPredicate implements Predicate<EntityFieldMetaDataVO> {
		@Override
		public boolean evaluate(EntityFieldMetaDataVO t) {
			final String entity = MetaDataServerProvider.getInstance().getEntity(t.getEntityId()).getEntity();
			if (NuclosEntity.STATEHISTORY.getEntityName().equals(entity)) {
				return true;
			}
			if (t.getEntityId() < 0) {
				return false;
			}
			if (NuclosEntity.getByName(entity) != null) {
				return false;
			}
			return true;
		}
	}
}
