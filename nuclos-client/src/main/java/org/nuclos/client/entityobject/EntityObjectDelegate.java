package org.nuclos.client.entityobject;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.nuclos.common.EntityObjectCommon;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.PivotInfo;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonFatalException;
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
	public List<Long> getEntityObjectIds(Long id, CollectableSearchExpression cse) {
		return facade.getEntityObjectIds(id, cse);
	}

	@Override
	public ProxyList<EntityObjectVO> getEntityObjectProxyList(Long id, CollectableSearchExpression clctexpr,
			Set<Long> stRequiredAttributeIds, Set<String> stRequiredSubEntityNames, Set<PivotInfo> pivots, 
			boolean includeDependents) {
		return facade.getEntityObjectProxyList(id, clctexpr, stRequiredAttributeIds, stRequiredSubEntityNames, 
				pivots, includeDependents);
	}

	@Override
	public Collection<EntityObjectVO> getEntityObjectsMore(Long id, List<Long> lstIds,
			Set<Long> stRequiredAttributeIds, Set<String> stRequiredSubEntityNames, Set<PivotInfo> pivots, boolean includeDependents) {
		return facade.getEntityObjectsMore(id, lstIds, stRequiredAttributeIds, stRequiredSubEntityNames, pivots, includeDependents);
	}

	@Override
	public Collection<EntityObjectVO> getDependentEntityObjects(String sEntityName, String sForeignKeyField,
			Long oRelatedId) {
		return facade.getDependentEntityObjects(sEntityName, sForeignKeyField, oRelatedId);
	}

	@Override
	public Collection<EntityObjectVO> getDependentPivotEntityObjects(PivotInfo pivot, String sForeignKeyField,
			Long oRelatedId) {
		return facade.getDependentPivotEntityObjects(pivot, sForeignKeyField, oRelatedId);
	}

}
