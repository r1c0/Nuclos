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
package org.nuclos.client.ui.collect;

import java.util.List;

import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collection.ListAdapter;

/**
 * Makes a <code>List<VO></code> look like a <code>List<Collectable></code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public abstract class CollectableListAdapter<VO, Clct extends Collectable> extends ListAdapter<VO, Clct> {

	/**
	 * @param lstvoAdaptee the wrapped <code>List</code> of value objects (adaptees).
	 */
	protected CollectableListAdapter(List<VO> lstvoAdaptee) {
		super(lstvoAdaptee);
	}

	/**
	 * makes <code>vo</code> collectable.
	 * @param vo
	 * @return a Collectable[Adapter] wrapped around <code>vo</code>.
	 */
	protected abstract Clct makeCollectable(VO vo);

	/**
	 * extracts the adaptee from the given Collectable[Adapter].
	 * @param clct
	 * @return the value object contained in <code>clct</code>.
	 */
	protected abstract VO extractAdaptee(Clct clct);

	@Override
	protected Clct wrap(VO vo) {
		return this.makeCollectable(vo);
	}

	@Override
	protected VO unwrap(Clct clct) {
		return this.extractAdaptee(clct);
	}

}  // class CollectableListAdapter
