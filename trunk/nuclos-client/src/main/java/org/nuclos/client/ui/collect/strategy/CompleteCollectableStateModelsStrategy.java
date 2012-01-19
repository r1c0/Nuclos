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

import java.util.Set;

import org.nuclos.client.statemodel.admin.CollectableStateModel;
import org.nuclos.client.statemodel.admin.StateModelCollectController;

/**
 * Strategy for loading collectables: Just the necessary data for the Result panel,
 * everything for the Details panel.
 */
public class CompleteCollectableStateModelsStrategy extends AbstractCompleteCollectablesStrategy<CollectableStateModel> {
	
	public CompleteCollectableStateModelsStrategy(StateModelCollectController cc) {
		super(cc);
	}
	
	@Override
    public boolean getCollectablesInResultAreAlwaysComplete() {
		return false;
	}

	@Override
    public Set<String> getRequiredFieldNamesForResult() {
		return null;
	}

}	// class CompleteCollectableStateModelsStrategy

