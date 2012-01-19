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

import java.util.Collections;
import java.util.Set;

import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.common.collect.collectable.Collectable;

/**
 * Default strategy: Always load complete <code>Collectable</code>s.
 */
public class AlwaysLoadCompleteCollectablesStrategy<Clct extends Collectable> extends AbstractCompleteCollectablesStrategy<Clct> {
	
	public AlwaysLoadCompleteCollectablesStrategy(CollectController<Clct> cc) {
		super(cc);
	}

	/**
	 * @return true
	 */
	@Override
    public boolean getCollectablesInResultAreAlwaysComplete() {
		return true;
	}

	/**
	 * @return Set<String> the names of the required fields for the search result. It is merged with the names of the fields (columns)
	 * that the user has selected to be displayed, so these don't need to be given here.
	 * Successors should specify the names of the columns here that must always be loaded for the result set, eg. the name
	 * of the column(s) to build the identifier or to calculate the right to edit/delete etc.
	 * This default implementation returns an empty set.
	 */
	@Override
    public Set<String> getRequiredFieldNamesForResult() {
		return Collections.emptySet();
	}

}	// inner class AlwaysLoadCompleteCollectablesStrategy
