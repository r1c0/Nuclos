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
package org.nuclos.client.common;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nuclos.client.entityobject.CollectableEOEntityClientProvider;
import org.nuclos.client.entityobject.CollectableEntityObject;
import org.nuclos.client.masterdata.CollectableWithDependants;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.collect.collectable.AbstractCollectable;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.Collectable.GetId;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityProvider;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.PredicateUtils;
import org.nuclos.common.collection.SymmetricBinaryPredicate;
import org.nuclos.common.collection.ValueObjectList;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.entityobject.CollectableEOEntity;
import org.nuclos.common.entityobject.CollectableEOEntityProvider;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.common2.DateUtils;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;

/**
 * Provides support for multi-update of dependant masterdata.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class MultiUpdateOfDependants {

	private final IdMapping idmapping;

	/**
	 * the color to be used as background for multi editable subforms that don't share a common value.
	 */
	public static Color colorCommonValues = Utils.translateColorFromParameter(ParameterProvider.KEY_HISTORICAL_STATE_CHANGED_COLOR);//new Color(246,229,255);

	/**
	 * initiates the multi-editing of dependants. Calculates the objects that are common in the dependants of all the given
	 * collectables and puts these in the given subform controllers, for each subentity.
	 * @param collsubformctl
	 * @param collclctwd
	 */
	public MultiUpdateOfDependants(Collection<? extends DetailsSubFormController<CollectableEntityObject>> collsubformctl,
			Collection<? extends CollectableWithDependants> collclctwd) {
		this.idmapping = prepareSubFormsForMultiEdit(collsubformctl, collclctwd);
	}

	/**
	 * @param collsubformctl the subform controllers containing the data entered by the user during multi-edit.
	 * @param clctwd the collectable to update
	 * @return the dependant collectables needed for update of the given collectable
	 * @todo we should return a DependantMasterDataMap here
	 */
	public DependantCollectableMasterDataMap getDependantCollectableMapForUpdate(
			Collection<? extends DetailsSubFormController<CollectableEntityObject>> collsubformctl, CollectableWithDependants clctwd) {

		final DependantMasterDataMap mpDependants = new DependantMasterDataMap();

		// iterate all enabled subforms:

		for (DetailsSubFormController<CollectableEntityObject> subformctl : collsubformctl) {
			if (subformctl.getSubForm().isEnabled()) {
				final String sSubEntityName = subformctl.getSubForm().getEntityName();
				final String sForeignKeyFieldName = subformctl.getForeignKeyFieldName();
				final Object oParentId = clctwd.getId();

				String parentSubform = subformctl.getSubForm().getParentSubForm();

				// special handling for child subforms
				if (!StringUtils.isNullOrEmpty(parentSubform)) {
					// to be done
					continue;
				}

				// get added, removed and changed objects from the subform. Note that the data contained in our subform is not
				// real data, but "prototype" data that serves as an "example" which changes the user wants to be performed
				// on each Collectable.

				final ValueObjectList<CollectableEntityObject> valuobjectlist = subformctl.getValueObjectList();

				// 1. Added objects: can be taken directly from the subform. They are cloned so we don't alter the subform data:
				final Collection<EntityObjectVO> collmdvoAdded = extractClonedMasterDataCVOs(valuobjectlist.getAddedObjects());

				// the "old" subform data of the given Collectable:
				//final Collection<EntityObjectVO> collmdvoOld = new ArrayList<EntityObjectVO>();
				final Collection<EntityObjectVO> collmdvoOld = CollectionUtils.transform(clctwd.getDependants(sSubEntityName), new CollectableEntityObject.ExtractAbstractCollectableVO());
					//CollectionUtils.transform(clctwd.getDependants(sSubEntityName), new CollectableEntityObject.ExtractMasterDataVO());

				// 2. Removed objects: first get the prototypes for removal, then get the objects of the old record that are to be removed:
				final Collection<EntityObjectVO> collmdvoRemoved = getFromPrototypes(new GetRemovedFromPrototype(), collmdvoOld, getPrototypesForRemoval(valuobjectlist), sSubEntityName, oParentId, this.idmapping);

				// 3. Changed objects: first get the prototypes for change, then get the objects of the old record that are to be changed:
				final Collection<EntityObjectVO> collmdvoChanged = getFromPrototypes(new GetChangedFromPrototype(), collmdvoOld, getPrototypesForChange(valuobjectlist), sSubEntityName, oParentId, this.idmapping);

				// 4. Put together removed and changed objects:
				final Collection<EntityObjectVO> collmdvo = CollectionUtils.concat(collmdvoRemoved, collmdvoChanged);

				// 5. Add unchanged objects:
				for (EntityObjectVO mdvo : collmdvoOld) {
					if (!CollectionUtils.exists(collmdvo, PredicateUtils.transformedInputEquals(new EntityObjectVO.GetId(), mdvo.getId())))
					{
						collmdvo.add(mdvo);
					}
				}

				// 6. add new objects
				final Collection<EntityObjectVO> collresult = CollectionUtils.concat(collmdvoAdded, collmdvo);

				// 7. Correct the parent ids of those objects:
				setParentIds(collresult, sForeignKeyFieldName, oParentId);

				// and put them to the result:
				mpDependants.addAllData(sSubEntityName, collresult);

			}
		}

		// LOCC.updateCollectable expects a DependantCollectableMap:
		return new DependantCollectableMasterDataMap(mpDependants);
	}

	public boolean isCollectableEditable(String entity, Collectable clct) {
		for (IdMapping.Key key : idmapping.get(entity).keySet()) {
			if (LangUtils.equals(clct.getId(), key.oPrototypeId)) {
				return true;
			}
		}
		for (Collection<Object> collIds : idmapping.get(entity).values()) {
			if (CollectionUtils.contains(clct.getId(), collIds)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * prepares the subforms for multi edit. Creates the id mapping for multi edit.
	 * @param collsubformctl
	 * @param collclctwd
	 * @return the id mapping for multi edit, which is needed when updating is started.
	 */
	private IdMapping prepareSubFormsForMultiEdit(Collection<? extends DetailsSubFormController<CollectableEntityObject>> collsubformctl,
			Collection<? extends CollectableWithDependants> collclctwd) {
		final IdMapping result = new IdMapping();
		for (DetailsSubFormController<CollectableEntityObject> subformctl : collsubformctl) {
			subformctl.clear();
			subformctl.setMultiUpdateOfDependants(this);
			final String sSubEntityName = subformctl.getSubForm().getEntityName();
			final String sParentFieldName = subformctl.getForeignKeyFieldName();

			String parentSubform = subformctl.getSubForm().getParentSubForm();

			// load data of subforms of the first hierarchie
			if (StringUtils.isNullOrEmpty(parentSubform)) {
				Pair<Collection<CollectableEntityObject>, Collection<CollectableEntityObject>> intersection = getCommonSubCollectables(collclctwd, sSubEntityName, sParentFieldName);
				List<CollectableEntityObject> data = new ArrayList<CollectableEntityObject>();
				data.addAll(intersection.x);
				data.addAll(intersection.y);
				/** @todo try to use MasterDataSubFormController.fillSubForm instead */
				subformctl.updateTableModel(data);

				result.put(sSubEntityName, IdMapping.createMap(collclctwd, sSubEntityName, sParentFieldName, intersection.x));
			}
			// load data of child subforms
			else {
				// to be done
				continue;
			}
		}
		return result;
	}

	/**
	 * @param collclct
	 * @param sSubEntityName
	 * @return dependant data records of the given sub entity that are identical for all given Collectables
	 */
	private static Pair<Collection<CollectableEntityObject>, Collection<CollectableEntityObject>> getCommonSubCollectables(Collection<? extends CollectableWithDependants> collclct,
			String sSubEntityName, String sParentFieldName) {

		// compare all fields except the parent field:
		List<Collection<CollectableEntityObject>> dependants = new ArrayList<Collection<CollectableEntityObject>>();

		IdMapping.AreFieldsEqual predicate = new IdMapping.AreFieldsEqual(IdMapping.getAllFieldsExceptParentField(sSubEntityName, sParentFieldName));

		Collection<CollectableEntityObject> allDependants = new ArrayList<CollectableEntityObject>();
		// reload subform data from the database instead of using the tablemodel data like it was made before
		for (CollectableWithDependants clctWithDependants : collclct) {
			final Collection<EntityObjectVO> collmdcvo = (clctWithDependants.getId() == null) ?
				new ArrayList<EntityObjectVO>() :
					MasterDataDelegate.getInstance().getDependantMasterData(sSubEntityName, sParentFieldName, clctWithDependants.getId());
			CollectableEOEntityProvider provider = CollectableEOEntityClientProvider.getInstance();
			CollectableEOEntity eo = (CollectableEOEntity) provider.getCollectableEntity(sSubEntityName);
			List<CollectableEntityObject> list = CollectionUtils.transform(collmdcvo, new CollectableEntityObject.MakeCollectable(eo));
			allDependants.addAll(list);
            if (dependants.isEmpty()) {
                  dependants.add(CollectionUtils.distinct(list, predicate));
            }
            else {
                  dependants.add(list);
            }
		}

		Set<CollectableEntityObject> distinct = CollectionUtils.distinct(allDependants, predicate);
		Set<CollectableEntityObject> intersection =  CollectionUtils.intersectionAll(dependants, predicate);

		for (Iterator<CollectableEntityObject> iter = distinct.iterator(); iter.hasNext();) {
			if (CollectionUtils.contains(intersection, iter.next(), predicate)) {
				iter.remove();
			}
		}

		return new Pair<Collection<CollectableEntityObject>, Collection<CollectableEntityObject>>(intersection, distinct);
	}

	private static boolean areDependantsEmpty(Collection<? extends CollectableWithDependants> collclct,
		String sSubEntityName, String sParentFieldName) {

		List<EntityObjectVO> dependants = new ArrayList<EntityObjectVO>();

		for (CollectableWithDependants clctWithDependants : collclct) {
			if (clctWithDependants.getId() != null)
				dependants.addAll(MasterDataDelegate.getInstance().getDependantMasterData(sSubEntityName, sParentFieldName, clctWithDependants.getId()));
		}
		 return dependants.isEmpty();

	}

	/** @todo document! */
	private static Collection<EntityObjectVO> getFromPrototypes(GetFromPrototype getFromPrototype, Collection<EntityObjectVO> collmdvoOld,
			Collection<EntityObjectVO> collmdvoPrototype, String sSubEntityName, Object oParentId, IdMapping idmapping) {
		final Collection<EntityObjectVO> result = new ArrayList<EntityObjectVO>();
		for (EntityObjectVO mdvoPrototype : collmdvoPrototype) {
			Collection<Object> mappedIds = idmapping.getMappedIds(mdvoPrototype.getId(), sSubEntityName, oParentId);
			if (mappedIds != null) {
				for (Object oMappedId : mappedIds) {
					Object Id = oMappedId;
					if(oMappedId instanceof Integer) {
						Id = new Long(((Integer)oMappedId).intValue());
					}
					final EntityObjectVO mdvo = CollectionUtils.findFirst(collmdvoOld, PredicateUtils.transformedInputEquals(new EntityObjectVO.GetId(), /*oMapped*/Id));
					assert mdvo != null;
					getFromPrototype.apply(mdvo, mdvoPrototype);
					result.add(mdvo);
				}
			}
		}
		return result;
	}

	/**
	 * @param valueobjectlist
	 * @return the prototypes for change
	 */
	private static Collection<EntityObjectVO> getPrototypesForChange(ValueObjectList<CollectableEntityObject> valueobjectlist) {
		final Collection<EntityObjectVO> result = extractMasterDataCVOs(valueobjectlist);
		/** @todo OPTIMIZE: remove objects that didn't change */
		// remove added objects from the list of changed objects (added objects have null ids):
		CollectionUtils.removeAll(result, PredicateUtils.transformedInputIsNull(new EntityObjectVO.GetId()));
		return result;
	}

	/**
	 * @param valuobjectlist
	 * @return the prototypes for removal
	 */
	private static Collection<EntityObjectVO> getPrototypesForRemoval(ValueObjectList<CollectableEntityObject> valuobjectlist) {
		return extractMasterDataCVOs(valuobjectlist.getRemovedObjects());
	}

	private static void setParentIds(Collection<EntityObjectVO> collmdvo, String sForeignKeyFieldName, Object iParentId) {
		for (EntityObjectVO mdvo : collmdvo) {
			mdvo.getFields().put(sForeignKeyFieldName + "Id", iParentId);

			if(iParentId instanceof Integer) {
				Long id = new Long((Integer)iParentId);
				mdvo.getFieldIds().put(sForeignKeyFieldName, id);
			}
			else {
				mdvo.getFieldIds().put(sForeignKeyFieldName, (Long)iParentId);
			}
		}
	}

	private static Collection<EntityObjectVO> extractClonedMasterDataCVOs(Collection<CollectableEntityObject> collmdclct) {
		return CollectionUtils.transform(collmdclct, new CollectableEntityObject.ExtractMasterDataVO());

//		return CollectionUtils.transform(collmdclct, new Transformer<CollectableEntityObject, EntityObjectVO>() {
//			@Override
//			public EntityObjectVO transform(CollectableEntityObject clctmd) {
//
//				return new EntityObjectVO();
//			}
//		});
	}

	private static Collection<EntityObjectVO> extractMasterDataCVOs(Collection<CollectableEntityObject> collmdclct) {
		return CollectionUtils.transform(collmdclct, new CollectableEntityObject.ExtractMasterDataVO());
	}

	private static interface GetFromPrototype {
		/**
		 * applies whatever is necessary on the mdvo to make it ready for update.
		 * @param mdvo
		 * @param mdvoPrototype
		 */
		void apply(EntityObjectVO mdvo, EntityObjectVO mdvoPrototype);
	}

	private static class GetChangedFromPrototype implements GetFromPrototype {
		/**
		 * applies the changes: copies the fields from the prototype.
		 * @param mdvo
		 * @param mdvoPrototype
		 */
		@Override
		public void apply(EntityObjectVO mdvo, EntityObjectVO mdvoPrototype) {
			mdvo.getFields().putAll(mdvoPrototype.getFields());
			mdvo.getFieldIds().putAll(mdvoPrototype.getFieldIds());
			mdvo.flagUpdate();
		}
	}

	private static class GetRemovedFromPrototype implements GetFromPrototype {
		/**
		 * applies the changes: marks <code>mdvo</code> as removed.
		 * @param mdvo
		 * @param mdvoPrototype
		 */
		@Override
		public void apply(EntityObjectVO mdvo, EntityObjectVO mdvoPrototype) {
			mdvo.flagRemove();
		}
	}

	/**
	 * inner class IdMapping
	 * Map<String sEntity, Map<(iParentId, oPrototypeId), Collection<Object collMappedIds>>>
	 */
	private static class IdMapping extends HashMap<String, Map<IdMapping.Key, Collection<Object>>> {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * maps (sEntity, oParentId, oPrototypeId) to a collection of ids.
		 * @param oPrototypeId
		 * @param sSubEntityName
		 * @param oParentId
		 * @return the mapped ids for the given subentity, the given parent and the given prototype record.
		 */
		public Collection<Object> getMappedIds(Object oPrototypeId, String sSubEntityName, Object oParentId) {
			Integer id = null;
			if(oPrototypeId instanceof Long) {
				id = ((Long)oPrototypeId).intValue();
				return this.get(sSubEntityName).get(new Key(oParentId, id));
			}

			return this.get(sSubEntityName).get(new Key(oParentId, oPrototypeId));
		}

		private static Map<Key, Collection<Object>> createMap(Collection<? extends CollectableWithDependants> collclctwd,
				String sSubEntityName, String sParentFieldName, Collection<? extends Collectable> collclctCommon) {
			final Map<Key, Collection<Object>> result = CollectionUtils.newHashMap();

			final Collection<String> collFieldNames = getAllFieldsExceptParentField(sSubEntityName, sParentFieldName);

			for (CollectableWithDependants clctwd : collclctwd) {
				final Object oParentId = clctwd.getId();
				for (final Collectable clctCommon : collclctCommon) {
					final Object oPrototypeId = clctCommon.getId();
					final Predicate<Collectable> areFieldsEqual = PredicateUtils.bindFirst(new AreFieldsEqual(collFieldNames), clctCommon);

					final Collection<? extends AbstractCollectable> collclctEqual = CollectionUtils.select(clctwd.getDependants(sSubEntityName), areFieldsEqual);
					final Collection<Object> collMappedIds = CollectionUtils.transform(collclctEqual, new GetId());
					result.put(new Key(oParentId, oPrototypeId), collMappedIds);
				}
			}
			return result;
		}

		private static Collection<String> getAllFieldsExceptParentField(String sSubEntityName, String sParentFieldName) {
			final Collection<String> result = getFieldsForEquality(sSubEntityName, sParentFieldName);//DefaultCollectableEntityProvider.getInstance().getCollectableEntity(sSubEntityName).getFieldNames();
			result.remove(sParentFieldName);
			return result;
		}

		/**
		 * @return Collection<String> of field names, which define the equality of masterdata entities. This can be set in the masterdata entity.
 		 */
		private static Collection<String> getFieldsForEquality(String sSubEntityName, String sParentFieldName) {
			final Collection<String> result;
			org.nuclos.common.collect.collectable.CollectableEntity ce = DefaultCollectableEntityProvider.getInstance().getCollectableEntity(sSubEntityName);
			if(ce instanceof CollectableMasterDataEntity) {
				CollectableMasterDataEntity cmde = (CollectableMasterDataEntity) ce;
				result = cmde.getMasterDataMetaCVO().getFieldsForEquality();
			}
			else {
				result = ce.getFieldNames();
			}
			result.remove(sParentFieldName);
			return result;
		}

		/**
		 * inner class Key - Key used in IdMapping
		 * @todo try to use org.nuclos.common.Pair - but Pair needs Serializable
		 */
		private static class Key {
			final Object oParentId;
			final Object oPrototypeId;

			/**
			 * @param oParentId
			 * @param oPrototypeId
			 */
			Key(Object oParentId, Object oPrototypeId) {
				this.oParentId = oParentId;
				this.oPrototypeId = oPrototypeId;
			}

			@Override
			public boolean equals(Object o) {
				if (this == o) {
					return true;
				}
				if (!(o instanceof Key)) {
					return false;
				}
				final Key that = (Key) o;

				return LangUtils.equals(this.oParentId, that.oParentId) && LangUtils.equals(this.oPrototypeId, that.oPrototypeId);
			}

			@Override
			public int hashCode() {
				return LangUtils.hashCode(this.oParentId) ^ LangUtils.hashCode(this.oPrototypeId);
			}
		}	 // inner class Key

		private static class AreFieldsEqual implements SymmetricBinaryPredicate<Collectable> {
			private Collection<String> collFieldNames;

			AreFieldsEqual(Collection<String> collFieldNames) {
				this.collFieldNames = collFieldNames;
			}

			@Override
			public boolean evaluate(Collectable clct1, Collectable clct2) {
				return areFieldsEqual(clct1, clct2, this.collFieldNames);
			}

			private static boolean areFieldsEqual(Collectable clct1, Collectable clct2, Collection<String> collFieldNames) {
				boolean result = true;
				for (String sFieldName : collFieldNames) {
					final CollectableField clctf1 = clct1.getField(sFieldName);
					final CollectableField clctf2 = clct2.getField(sFieldName);
					if (!isFieldEqual(clctf1, clctf2)) {
						result = false;
						break;
					}
				}
				return result;
			}

			/**
			 * Compares the given CollectableFields using equals with one exception:
			 * If both fields contain Date objects, they are compared on a per-day basis (the time of day is ignored).
			 * @param clctf1
			 * @param clctf2
			 * @return Is clctf1 equal to clctf2?
			 */
			private static boolean isFieldEqual(CollectableField clctf1, CollectableField clctf2) {
				final boolean result;

				final Object oValue1 = clctf1.getValue();
				final Object oValue2 = clctf2.getValue();
				if (oValue1 instanceof Date && oValue2 instanceof Date) {
					// special case: dates/timestamps are always compared by day (ignoring hours/minutes/seconds)
					final Date oDate1 = (Date) oValue1;
					final Date oDate2 = (Date) oValue2;
					result = DateUtils.equalsPureDate(oDate1, oDate2);
				}
				else {
					result = clctf1.equals(clctf2);
				}
				return result;
			}

		}	// inner class AreFieldsEqual

	}	 // inner class IdMapping

}	// class MultiUpdateOfDependants
