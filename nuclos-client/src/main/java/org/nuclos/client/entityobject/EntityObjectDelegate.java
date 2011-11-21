package org.nuclos.client.entityobject;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.nuclos.common.EntityObjectCommon;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.common.ejb3.EntityObjectFacadeRemote;
import org.nuclos.server.genericobject.ProxyList;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;

public class EntityObjectDelegate implements EntityObjectCommon {

	private final static Logger LOG = Logger.getLogger(EntityObjectDelegate.class);

	private static EntityObjectDelegate singleton;

	private final EntityObjectFacadeRemote facade;

	private EntityObjectDelegate() {
		this.facade = ServiceLocator.getInstance().getFacade(EntityObjectFacadeRemote.class);
	}

	public static synchronized EntityObjectDelegate getInstance() {
		if (singleton == null) {
			try {
				singleton = new EntityObjectDelegate();
			}
			catch (RuntimeException ex) {
				throw new CommonFatalException(ex);
			}
		}
		return singleton;
	}

	@Override
	public EntityObjectVO get(String entity, Long id) throws CommonPermissionException {
		return facade.get(entity, id);
	}

	@Override
	public List<Long> getEntityObjectIds(Long id, CollectableSearchExpression cse) {
		return facade.getEntityObjectIds(id, cse);
	}

	@Override
	public ProxyList<EntityObjectVO> getEntityObjectProxyList(Long id, CollectableSearchExpression clctexpr,
			Collection<EntityFieldMetaDataVO> fields) {
		return facade.getEntityObjectProxyList(id, clctexpr, fields);
	}

	@Override
	public Collection<EntityObjectVO> getEntityObjectsMore(Long id, List<Long> lstIds,
			Collection<EntityFieldMetaDataVO> fields) {
		return facade.getEntityObjectsMore(id, lstIds, fields);
	}

	@Override
	public Collection<EntityObjectVO> getDependentEntityObjects(String sEntityName, String sForeignKeyField,
			Long oRelatedId) {
		return facade.getDependentEntityObjects(sEntityName, sForeignKeyField, oRelatedId);
	}

	@Override
	public Collection<EntityObjectVO> getDependentPivotEntityObjects(EntityFieldMetaDataVO pivot, String sForeignKeyField,
			Long oRelatedId) {
		return facade.getDependentPivotEntityObjects(pivot, sForeignKeyField, oRelatedId);
	}

	@Override
	public void removeEntity(String name, Long id) throws CommonPermissionException {
		facade.removeEntity(name, id);
	}

	@Override
	public void remove(EntityObjectVO entity) throws CommonPermissionException {
		facade.remove(entity);
	}

	@Override
	public EntityObjectVO getReferenced(String referencingEntity, String referencingEntityField, Long id) throws CommonBusinessException {
		return facade.getReferenced(referencingEntity, referencingEntityField, id);
	}

}
