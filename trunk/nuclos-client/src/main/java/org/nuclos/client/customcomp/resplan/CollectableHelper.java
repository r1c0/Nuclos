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

package org.nuclos.client.customcomp.resplan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.swing.JComponent;

import org.apache.commons.lang.SerializationUtils;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.common.NuclosCollectControllerFactory;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.genericobject.CollectableGenericObjectEntity;
import org.nuclos.client.genericobject.CollectableGenericObjectWithDependants;
import org.nuclos.client.genericobject.GenericObjectCollectController;
import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.client.masterdata.MasterDataCollectController;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.masterdata.MetaDataCache;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableFactory;
import org.nuclos.common.collect.collectable.CollectableUtils;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.common2.EntityAndFieldName;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.genericobject.ejb3.GenericObjectFacadeRemote;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeRemote;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;

public abstract class CollectableHelper<C extends Collectable> implements CollectableFactory<C> {

	protected final EntityMetaDataVO entity;

	protected CollectableHelper(EntityMetaDataVO entity) {
		this.entity = entity;
	}

	public String getEntityName() {
		return getCollectableEntity().getName();
	}
	
	public abstract CollectableEntity getCollectableEntity();
	
	public CollectableEntityField findEntityFieldReferencing(final String refEntity) {
		List<CollectableEntityField> refs = CollectionUtils.applyFilter(CollectableUtils.getCollectableEntityFields(getCollectableEntity()),
			new Predicate<CollectableEntityField>() {
				@Override public boolean evaluate(CollectableEntityField f) {
					return f.isReferencing() && refEntity.equals(f.getReferencedEntityName());
				}
		});
		if (refs.isEmpty()) {
			throw new NuclosFatalException("No reference to " + refEntity + " in " + entity.getEntity());
		} else if (refs.size() > 1) {
			throw new NuclosFatalException("Ambigiuous reference fields to " + refEntity + " in " + entity.getEntity());
		}
		return CollectionUtils.getFirst(refs);
	}
	
	public abstract C check(Collectable clct);
	
	public abstract Collectable create(Collectable clct) throws CommonBusinessException;

	public abstract Collectable get(Object id) throws CommonBusinessException;
	
	public abstract List<? extends Collectable> get(List<?> ids) throws CommonBusinessException;
	
	public final List<? extends Collectable> get() throws CommonBusinessException {
		return get(getIds(new CollectableSearchExpression(null)));
	}
	
	public abstract List<?> getIds(CollectableSearchExpression searchExpr) throws CommonBusinessException;
	
	public final List<?> getIds(CollectableSearchCondition cond) throws CommonBusinessException {
		return getIds((new CollectableSearchExpression(cond)));
	}

	public abstract boolean isNewAllowed();

	public abstract boolean isModifyAllowed(Collectable clct);

	public abstract boolean isRemoveAllowed(Collectable clct);
	
	@Override
	public abstract C newCollectable();
	
	public abstract Collectable modify(Collectable clct) throws CommonBusinessException;
	
	public abstract void remove(Collectable clct) throws CommonBusinessException, NuclosBusinessException;
	
	public final C newCollectable(boolean withDefaults) {
		C clct = newCollectable();
		if (withDefaults)
			CollectableUtils.setDefaultValues(clct, getCollectableEntity());
		return clct;
	}

	/** Copy the given Collectable. The returned Collectable is identical but independant of the given
	 * instance.  You can use the copy if you do not want to modify.
	 */
	public abstract Collectable copyCollectable(Collectable clct);
	
	/** "Clones" the given Collectable. "Clones" in Nuclos means make a copy which can be used
	 * for creating a new record, i.e. the returned Collectable has no id.
	 */
	public abstract Collectable cloneCollectable(Collectable clct) throws CommonBusinessException;

	public abstract CollectController<C> newCollectController(JComponent parent) throws CommonBusinessException;
	
	public static CollectableHelper<?> getForEntity(String entityName) {
		return getForEntity(entityName, false);
	}

	public static CollectableHelper<?> getForEntity(String entityName, boolean withDependants) {
		EntityMetaDataVO entity = MetaDataClientProvider.getInstance().getEntity(entityName);
		if (entity.isStateModel()) {
			return new CollectableGenericObjectHelper(entity);
		} else {
			return new CollectableMasterDataHelper(entity, withDependants);
		}
	}
	
	private static class CollectableMasterDataHelper extends CollectableHelper<CollectableMasterDataWithDependants> {

		private final String entityName;
		private final CollectableMasterDataEntity collectableEntity;
		private final List<EntityAndFieldName> dependantEntities;

		public CollectableMasterDataHelper(EntityMetaDataVO entity, boolean withDependants) {
			super(entity);
			entityName = entity.getEntity();
			collectableEntity = new CollectableMasterDataEntity(MetaDataCache.getInstance().getMetaData(entityName));
			if (withDependants) {
				dependantEntities = new ArrayList<EntityAndFieldName>(
					MasterDataDelegate.getInstance().getSubFormEntitiesByMasterDataEntity(entityName));
			} else {
				dependantEntities = new ArrayList<EntityAndFieldName>();
			}
		}
		
		@Override
		public CollectableMasterDataEntity getCollectableEntity() {
			return collectableEntity;
		}

		@Override
		public CollectableMasterDataWithDependants check(Collectable clct) {
			if (clct instanceof CollectableMasterDataWithDependants
				&& ((CollectableMasterDataWithDependants) clct).getCollectableEntity().getName().equals(entityName))
			{
				return (CollectableMasterDataWithDependants) clct;
			}
			return null;
		}
		
		@Override
		public boolean isNewAllowed() {
			return SecurityCache.getInstance().isWriteAllowedForMasterData(getEntityName());
		}

		@Override
		public boolean isModifyAllowed(Collectable clct) {
			return SecurityCache.getInstance().isWriteAllowedForMasterData(getEntityName());
		}

		@Override
		public boolean isRemoveAllowed(Collectable clct) {
			return SecurityCache.getInstance().isDeleteAllowedForMasterData(getEntityName());
		}

		@Override
		public Collectable create(Collectable clct) throws CommonBusinessException {
			CollectableMasterDataWithDependants clctMasterData = (CollectableMasterDataWithDependants) clct;
			MasterDataVO createdMdvo = MasterDataDelegate.getInstance().create(
					entity.getEntity(), clctMasterData.getMasterDataCVO(), clctMasterData.getDependantMasterDataMap());
			return get(createdMdvo.getId());
		}
		
		@Override
		public Collectable get(Object id) throws CommonBusinessException {
			return new CollectableMasterDataWithDependants(collectableEntity,
					MasterDataDelegate.getInstance().getWithDependants(entity.getEntity(), id, dependantEntities));
		}

		@Override
		public List<?> getIds(CollectableSearchExpression searchExpr) throws CommonBusinessException {
			if (searchExpr == null)
				searchExpr = new CollectableSearchExpression(null);
			return getMDFacade().getMasterDataIds(entityName,searchExpr);
		}

		@Override
		public List<? extends Collectable> get(List<?> ids) throws CommonBusinessException {
			return CollectionUtils.transform(
					getMDFacade().getMasterDataMore(entityName, ids, dependantEntities),
					new CollectableMasterDataWithDependants.MakeCollectable(collectableEntity));
		}

		@Override
		public Collectable modify(Collectable clct) throws CommonBusinessException {
			CollectableMasterDataWithDependants clctMasterData = (CollectableMasterDataWithDependants) clct;
			MasterDataDelegate.getInstance().update(
					entity.getEntity(), clctMasterData.getMasterDataCVO(), clctMasterData.getDependantMasterDataMap());
			return get(clct.getId());
		}
		
		@Override
		public void remove(Collectable clct) throws CommonBusinessException, NuclosBusinessException {
			MasterDataVO mdvo = ((CollectableMasterDataWithDependants) clct).getMasterDataCVO();
			MasterDataDelegate.getInstance().remove(entity.getEntity(), mdvo);
		}
		
		@Override
		public CollectableMasterDataWithDependants newCollectable() {
			return CollectableMasterDataWithDependants.newInstance(
					collectableEntity, new MasterDataVO(collectableEntity.getMasterDataMetaCVO(), false));
		}
		
		@Override
		public Collectable copyCollectable(Collectable clct) {
			CollectableMasterDataWithDependants clctMasterData = (CollectableMasterDataWithDependants) clct;
			MasterDataWithDependantsVO clone = (MasterDataWithDependantsVO) SerializationUtils.clone(clctMasterData.getMasterDataWithDependantsCVO());
			return new CollectableMasterDataWithDependants(collectableEntity, clone);
		}
		
		@Override
		public Collectable cloneCollectable(Collectable clct) throws CommonBusinessException {
			CollectableMasterDataWithDependants clctMasterData = (CollectableMasterDataWithDependants) clct;
			HashMap<String, Object> fields = new HashMap<String, Object>(clctMasterData.getMasterDataCVO().getFields());
			return CollectableMasterDataWithDependants.newInstance(clctMasterData.getCollectableEntity(),
					new MasterDataVO(null, null, null, null, null, null, fields));
		}
		
		@Override
		public MasterDataCollectController newCollectController(JComponent parent) throws CommonBusinessException {
			return NuclosCollectControllerFactory.getInstance().newMasterDataCollectController(parent, entity.getEntity(), null);
		}

		private static MasterDataFacadeRemote getMDFacade() {
			return ServiceLocator.getInstance().getFacade(MasterDataFacadeRemote.class);
		}
	}

	private static class CollectableGenericObjectHelper extends CollectableHelper<CollectableGenericObjectWithDependants> {

		private final int moduleId;
		private final CollectableGenericObjectEntity collectableEntity;

		public CollectableGenericObjectHelper(EntityMetaDataVO entity) {
			super(entity);
			moduleId = entity.getId().intValue();
			collectableEntity = (CollectableGenericObjectEntity) CollectableGenericObjectEntity.getByModuleId(moduleId);
		}

		@Override
		public CollectableGenericObjectEntity getCollectableEntity() {
			return collectableEntity;
		}

		@Override
		public CollectableGenericObjectWithDependants check(Collectable clct) {
			if (clct instanceof CollectableGenericObjectWithDependants) {
				CollectableGenericObjectWithDependants clctWd = (CollectableGenericObjectWithDependants) clct;
				if (clctWd.getGenericObjectCVO().getModuleId() == moduleId) {
					return clctWd;
				}
			}
			return null;
		}

		@Override
		public boolean isNewAllowed() {
			return SecurityCache.getInstance().isNewAllowedForModule(getEntityName());
		}

		@Override
		public boolean isModifyAllowed(Collectable clct) {
			Integer id = (clct != null) ? (Integer) clct.getId() : null;
			return SecurityCache.getInstance().isWriteAllowedForModule(getEntityName(), id);
		}

		@Override
		public boolean isRemoveAllowed(Collectable clct) {
			Integer id = (clct != null) ? (Integer) clct.getId() : null;
			return SecurityCache.getInstance().isDeleteAllowedForModule(getEntityName(), id, false);
		}

		@Override
		public Collectable create(Collectable clct) throws CommonBusinessException {
			GenericObjectWithDependantsVO gowdvo = ((CollectableGenericObjectWithDependants) clct).getGenericObjectWithDependantsCVO();
			Integer id = getGOFacade().create(gowdvo).getId();
			return get(id);
		}

		@Override
		public Collectable get(Object id) throws CommonBusinessException {
			GenericObjectWithDependantsVO gowdvo = getGOFacade().getWithDependants((Integer) id, null);
			return new CollectableGenericObjectWithDependants(gowdvo);
		}
		
		@Override
		public List<?> getIds(CollectableSearchExpression searchExpr) throws CommonBusinessException {
			if (searchExpr == null)
				searchExpr = new CollectableSearchExpression(null);
			return getGOFacade().getGenericObjectIds(moduleId, searchExpr);
		}
		
		@Override
		public List<? extends Collectable> get(final List<?> ids) throws CommonBusinessException {
			List<Integer> intIds = CollectionUtils.typecheck(ids, Integer.class);
			Collection<GenericObjectWithDependantsVO> gowdvos = getGOFacade().getGenericObjectsMore(moduleId, intIds, null, Collections.<String>emptySet(), false);
			List<Collectable> result = new ArrayList<Collectable>(gowdvos.size());
			for (GenericObjectWithDependantsVO gowdvo : gowdvos) {
				result.add(new CollectableGenericObjectWithDependants(gowdvo));
			}
			Collections.sort(result, new Comparator<Collectable>() {
				@Override
				public int compare(Collectable c1, Collectable c2) {
					int id1Index = ids.indexOf(c1.getId());
					int id2Index = ids.indexOf(c2.getId());
					return id1Index < id2Index ? -1 : (id1Index == id2Index ? 0 : 1);
				}
			});
			return result;
		}

		@Override
		public Collectable modify(Collectable clct) throws CommonBusinessException {
			GenericObjectWithDependantsVO gowdvo = ((CollectableGenericObjectWithDependants) clct).getGenericObjectWithDependantsCVO();
			getGOFacade().modify(moduleId, gowdvo);
			return get(clct.getId());
		}
		
		@Override
		public void remove(Collectable clct) throws CommonBusinessException, NuclosBusinessException {
			GenericObjectWithDependantsVO gowdvo = ((CollectableGenericObjectWithDependants) clct).getGenericObjectWithDependantsCVO();
			getGOFacade().remove(gowdvo, false);
		}

		@Override
		public CollectableGenericObjectWithDependants newCollectable() {
			GenericObjectVO govo = new GenericObjectVO(moduleId, null, null, null);
			return CollectableGenericObjectWithDependants.newCollectableGenericObject(govo);
		}
		
		@Override
		public Collectable copyCollectable(Collectable clct) {
			CollectableGenericObjectWithDependants clctGenericObject = (CollectableGenericObjectWithDependants) clct;
			GenericObjectWithDependantsVO clone = (GenericObjectWithDependantsVO) SerializationUtils.clone(clctGenericObject.getGenericObjectWithDependantsCVO());
			return new CollectableGenericObjectWithDependants(clone);
		}
		
		@Override
		public Collectable cloneCollectable(Collectable clct) throws CommonBusinessException {
			CollectableGenericObjectWithDependants clctGenericObject = (CollectableGenericObjectWithDependants) clct;
			GenericObjectVO clonedGovo = clctGenericObject.getGenericObjectCVO().copy();
			return CollectableGenericObjectWithDependants.newCollectableGenericObject(clonedGovo);
		}

		@Override
		public GenericObjectCollectController newCollectController(JComponent parent) throws CommonBusinessException {
			return NuclosCollectControllerFactory.getInstance().newGenericObjectCollectController(parent, moduleId, null);
		}

		private static GenericObjectFacadeRemote getGOFacade() {
			return ServiceLocator.getInstance().getFacade(GenericObjectFacadeRemote.class);
		}
	}
}
