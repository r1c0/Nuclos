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

import javax.swing.event.ChangeListener;

import org.nuclos.client.ui.collect.CollectableListAdapter;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.server.genericobject.ProxyList;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;

/**
 * Makes a <code>List<MasterDataVO></code> look like a <code>List<Collectable></code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Uwe.Allner@novabit.de">Uwe.Allner</a>
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph Radig</a>
 * @version 01.00.00
 */
public class CollectableMasterDataProxyListAdapter
		extends CollectableListAdapter<MasterDataWithDependantsVO, CollectableMasterDataWithDependants>
		implements ProxyList<CollectableMasterDataWithDependants> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * the entity of the contained master data elements.
	 */
	private CollectableMasterDataEntity clcte;

	public CollectableMasterDataProxyListAdapter(ProxyList<MasterDataWithDependantsVO> lstAdaptee, CollectableMasterDataEntity clcte) {
		super(lstAdaptee);
		this.clcte = clcte;
	}

	@Override
	protected ProxyList<MasterDataWithDependantsVO> adaptee() {
		return (ProxyList<MasterDataWithDependantsVO>) super.adaptee();
	}

	@Override
	protected CollectableMasterDataWithDependants makeCollectable(MasterDataWithDependantsVO mdvo) {
		return new CollectableMasterDataWithDependants(clcte, mdvo);
	}

	@Override
	protected MasterDataWithDependantsVO extractAdaptee(CollectableMasterDataWithDependants clctmd) {
		return clctmd.getMasterDataWithDependantsCVO();
	}

	@Override
	public int getLastIndexRead() {
		return this.adaptee().getLastIndexRead();
	}

	@Override
	public void fetchDataIfNecessary(int iIndex, ChangeListener changelistener) {
		this.adaptee().fetchDataIfNecessary(iIndex, changelistener);
	}

	@Override
	public boolean hasObjectBeenReadForIndex(int index) {
		return this.adaptee().hasObjectBeenReadForIndex(index);
	}

	@Override
	public int getIndexById(Object oId) {
		return this.adaptee().getIndexById(oId);
	}

}	// class CollectableMasterDataProxyListAdapter
