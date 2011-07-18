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
package org.nuclos.client.ui.collect.strategy;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.NullArgumentException;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * inner class AbstractCompleteCollectablesStrategy: provides default implementations for some <code>CompleteCollectablesStrategy</code> methods.
 */
public abstract class AbstractCompleteCollectablesStrategy<Clct extends Collectable> implements CompleteCollectablesStrategy<Clct> {
	
	private final CollectController<Clct> cc;
	
	public AbstractCompleteCollectablesStrategy(CollectController<Clct> cc) {
		this.cc = cc;
	}
	
	protected final CollectController<Clct> getCollectController() {
		return cc;
	}

	/**
	 * @param clct
	 * @return <code>clct.isComplete()</code>: Have all fields of the given Collectable been loaded?
	 */
	@Override
    public boolean isComplete(Clct clct) {
		return clct.isComplete();
	}

	/**
	 * reads a bunch of <code>Collectable</code>s from the database.
	 * This default implementation reads them one by one. Successors may implement a more efficient version here.
	 * @param collclct Collection<Collectable>
	 * @return Collection<Collectable> contains the read <code>Collectable</code>s.
	 * @throws CommonBusinessException
	 * @precondition collclct != null
	 * @postcondition result != null
	 * @postcondition result.size() == collclct.size()
	 */
	@Override
    public Collection<Clct> getCompleteCollectables(Collection<Clct> collclct) throws CommonBusinessException {
		if (collclct == null) {
			throw new NullArgumentException("collclct");
		}
		final Collection<Clct> result = new ArrayList<Clct>();
		for (Clct clct : collclct) {
			final Clct clctComplete = this.isComplete(clct) ? clct : cc.readCollectable(clct);
			result.add(clctComplete);
		}
		assert result != null;
		assert result.size() == collclct.size();
		return result;
	}

}	// inner class AbstractCompleteCollectablesStrategy
