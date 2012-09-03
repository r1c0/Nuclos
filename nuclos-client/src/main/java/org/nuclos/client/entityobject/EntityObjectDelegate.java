package org.nuclos.client.entityobject;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.nuclos.common.EntityObjectCommon;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.common.ejb3.EntityObjectFacadeRemote;
import org.nuclos.server.genericobject.ProxyList;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;

public class EntityObjectDelegate implements EntityObjectCommon {

	private final static Logger LOG = Logger.getLogger(EntityObjectDelegate.class);

	private static EntityObjectDelegate INSTANCE;
	
	//

	private EntityObjectFacadeRemote entityObjectFacadeRemote;

	EntityObjectDelegate() {
		INSTANCE = this;
	}

	public static EntityObjectDelegate getInstance() {
		if (INSTANCE == null) {
			throw new IllegalStateException("too early");
		}
		return INSTANCE;
	}
	
	public final void setEntityObjectFacadeRemote(EntityObjectFacadeRemote entityObjectFacadeRemote) {
		this.entityObjectFacadeRemote = entityObjectFacadeRemote;
	}

	@Override
	public EntityObjectVO get(String entity, Long id) throws CommonPermissionException {
		return entityObjectFacadeRemote.get(entity, id);
	}

	@Override
	public List<Long> getEntityObjectIds(Long id, CollectableSearchExpression cse) {
		return entityObjectFacadeRemote.getEntityObjectIds(id, cse);
	}

	@Override
	public ProxyList<EntityObjectVO> getEntityObjectProxyList(Long id, CollectableSearchExpression clctexpr,
			Collection<EntityFieldMetaDataVO> fields, String customUsage) {
		return entityObjectFacadeRemote.getEntityObjectProxyList(id, clctexpr, fields, customUsage);
	}

	@Override
	public Collection<EntityObjectVO> getEntityObjectsMore(Long id, List<Long> lstIds,
			Collection<EntityFieldMetaDataVO> fields, String customUsage) {
		return entityObjectFacadeRemote.getEntityObjectsMore(id, lstIds, fields, customUsage);
	}

	@Override
	public Collection<EntityObjectVO> getDependentEntityObjects(String sEntityName, String sForeignKeyField,
			Long oRelatedId) {
		return entityObjectFacadeRemote.getDependentEntityObjects(sEntityName, sForeignKeyField, oRelatedId);
	}

	@Override
	public Collection<EntityObjectVO> getDependentPivotEntityObjects(EntityFieldMetaDataVO pivot, String sForeignKeyField,
			Long oRelatedId) {
		return entityObjectFacadeRemote.getDependentPivotEntityObjects(pivot, sForeignKeyField, oRelatedId);
	}

	@Override
	public void removeEntity(String name, Long id) throws CommonPermissionException {
		entityObjectFacadeRemote.removeEntity(name, id);
	}

	@Override
	public void remove(EntityObjectVO entity) throws CommonPermissionException {
		entityObjectFacadeRemote.remove(entity);
	}

	@Override
	public EntityObjectVO getReferenced(String referencingEntity, String referencingEntityField, Long id) throws CommonBusinessException {
		return entityObjectFacadeRemote.getReferenced(referencingEntity, referencingEntityField, id);
	}

	@Override
	public void createOrUpdatePlain(EntityObjectVO entity) throws CommonPermissionException {
		entityObjectFacadeRemote.createOrUpdatePlain(entity);
	}

	@Override
	public Integer getVersion(String entity, Long id) throws CommonPermissionException {
		return entityObjectFacadeRemote.getVersion(entity, id);
	}

}
