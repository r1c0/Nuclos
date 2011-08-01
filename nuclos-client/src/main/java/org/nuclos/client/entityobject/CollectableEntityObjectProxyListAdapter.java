//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.client.entityobject;

import java.util.List;

import javax.swing.event.ChangeListener;

import org.nuclos.client.ui.collect.CollectableListAdapter;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.entityobject.CollectableEOEntity;
import org.nuclos.server.genericobject.ProxyList;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;

/**
 * Makes a <code>List&lt;EntityObjectVO&gt;</code> look like a <code>List&lt;CollectableEntityObject&gt;</code>.
 * <p>
 * This is needed for lazy loading of CollectableEntityObjects on the client side.
 * </p><p>
 * Created by Novabit Informationssysteme GmbH
 * </p><p>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * </p>
 * @author Thomas Pasch
 * @since Nuclos 3.1.01
 */
public class CollectableEntityObjectProxyListAdapter extends
		CollectableListAdapter<EntityObjectVO, CollectableEntityObject> implements
		ProxyList<CollectableEntityObject> {
	
	private final CollectableEOEntity meta;
	
	public CollectableEntityObjectProxyListAdapter(List<EntityObjectVO> list, CollectableEOEntity meta) {
		super(list);
		this.meta = meta;
	}

	@Override
	protected ProxyList<EntityObjectVO> adaptee() {
		return (ProxyList<EntityObjectVO>) super.adaptee();
	}

	@Override
	protected CollectableEntityObject makeCollectable(EntityObjectVO vo) {
		final CollectableEntityObject result = new CollectableEntityObject(meta, vo);
		final DependantMasterDataMap deps = vo.getDependants();
		result.setDependantMasterDataMap(deps);
		// TODO: ???
		/*
		final CollectableEntityProvider cep = CollectableEOEntityClientProvider.getInstance();
		for (String s: deps.getEntityNames()) {
			final Collection<EntityObjectVO> vos = deps.getData(s);
			final CollectableEOEntity sMeta = (CollectableEOEntity) cep.getCollectableEntity(s);
			final Collection<CollectableEntityObject> col = new ArrayList<CollectableEntityObject>();
			for (EntityObjectVO eo: vos) {
				col.add(new CollectableEntityObject(sMeta, eo));
			}
			result.getDependantCollectableMasterDataMap().addValues(s, col);
		}
		 */
		return result;
	}

	@Override
	protected EntityObjectVO extractAdaptee(CollectableEntityObject clct) {
		final EntityObjectVO result = clct.getEntityObjectVO();
		// TODO: what todo with the dependants stuff???
		return clct.getEntityObjectVO();
	}

	@Override
	public void fetchDataIfNecessary(int iIndex, ChangeListener changelistener) {
		adaptee().fetchDataIfNecessary(iIndex, changelistener);
	}

	@Override
	public int getLastIndexRead() {
		return adaptee().getLastIndexRead();
	}

	@Override
	public boolean hasObjectBeenReadForIndex(int index) {
		return this.adaptee().hasObjectBeenReadForIndex(index);
	}

	@Override
	public int getIndexById(Object oId) {
		return this.adaptee().getIndexById(oId);
	}

}
