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
package org.nuclos.client.genericobject;

import javax.swing.event.ChangeListener;

import org.nuclos.client.ui.collect.CollectableListAdapter;
import org.nuclos.server.genericobject.ProxyList;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;

/**
 * Makes a <code>List<GenericObjectWithDependantsVO></code> look like a <code>List<Collectable></code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class CollectableGenericObjectProxyListAdapter
		extends CollectableListAdapter<GenericObjectWithDependantsVO, CollectableGenericObjectWithDependants>
		implements ProxyList<CollectableGenericObjectWithDependants> {

	/**
	 * @param lstlowdcvoAdaptee ProxyList<GenericObjectWithDependantsVO>
	 */
	public CollectableGenericObjectProxyListAdapter(ProxyList<GenericObjectWithDependantsVO> lstlowdcvoAdaptee) {
		super(lstlowdcvoAdaptee);
	}

	@Override
	protected ProxyList<GenericObjectWithDependantsVO> adaptee() {
		return (ProxyList<GenericObjectWithDependantsVO>) super.adaptee();
	}

	@Override
	protected CollectableGenericObjectWithDependants makeCollectable(GenericObjectWithDependantsVO vo) {
		return new CollectableGenericObjectWithDependants(vo);
	}

	@Override
	protected GenericObjectWithDependantsVO extractAdaptee(CollectableGenericObjectWithDependants clct) {
		return clct.getGenericObjectWithDependantsCVO();
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

}	// class CollectableGenericObjectProxyListAdapter
