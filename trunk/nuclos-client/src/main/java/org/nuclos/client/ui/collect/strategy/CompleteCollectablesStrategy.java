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

import java.util.Collection;
import java.util.Set;

import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * interface CompleteCollectablesStrategy
 */
public interface CompleteCollectablesStrategy<Clct extends Collectable> {
	/**
	 * defines what it means for a <code>Collectable</code> to be complete in the context of the used <code>CollectController</code>.
	 * In general, <code>isCollectableComplete(Collectable)</code> means: The given <code>Collectable</code> is ready for display in the Details panel.
	 * (see the preconditions used in viewSingleCollectable() etc.)
	 * The default definition is given by <code>Collectable.isComplete()</code>: All fields of the Collectable have been loaded.
	 * There are alternative (more restrictive) definitions, though, when it comes to subforms.
	 * @param clct
	 * @return Is the given <code>Collectable</code> complete?
	 */
	boolean isComplete(Clct clct);

	/**
	 * @return Are <code>Collectable</code>s in the Result tab always complete, that means are all of their fields loaded
	 * when searching?
	 */
	boolean getCollectablesInResultAreAlwaysComplete();

	/**
	 * @return Set<String> the names of the required fields for the search result. It is merged with the names of the fields (columns)
	 * that the user has selected to be displayed, so these don't need to be given here.
	 * Successors should specify the names of the columns here that must always be loaded for the result set, eg. the name
	 * of the column(s) to build the identifier or to calculate the right to edit/delete etc.
	 * This method isn't actually needed in the CollectController currently. It's more like a reminder that you have to
	 * take care of required field names if you implement your own strategy.
	 */
	Set<String> getRequiredFieldNamesForResult();

	/**
	 * makes a bunch of <code>Collectable</code>s complete, by reading them from the database, if necessary.
	 * @param collclct Collection<Collectable> These are not changed.
	 * @return Collection<Collectable> contains the complete <code>Collectable</code>s.
	 * @throws CommonBusinessException
	 * @precondition collclct != null
	 * @postcondition result != null
	 * @postcondition result.size() == collclct.size()
	 */
	Collection<Clct> getCompleteCollectables(Collection<Clct> collclct) throws CommonBusinessException;

}	// interface CompleteCollectablesStrategy
