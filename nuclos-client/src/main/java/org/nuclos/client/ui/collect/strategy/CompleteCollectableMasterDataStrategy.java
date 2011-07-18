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

import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.client.masterdata.MasterDataCollectController;
import org.nuclos.client.ui.collect.CollectController;

/**
 * The strategy for completing master data. The search result always contains
 * all fields, but never contains dependants. Those must be loaded when entering
 * Details mode.
 */
public class CompleteCollectableMasterDataStrategy extends
		AbstractCompleteCollectablesStrategy<CollectableMasterDataWithDependants> {
	
	public CompleteCollectableMasterDataStrategy(CollectController<CollectableMasterDataWithDependants> cc) {
		super(cc);
	}

	@Override
	public boolean getCollectablesInResultAreAlwaysComplete() {
		return false;
	}

	@Override
	public boolean isComplete(CollectableMasterDataWithDependants clct) {
		return super.isComplete(clct);
	}

	@Override
	public Set<String> getRequiredFieldNamesForResult() {
		return getCollectController().getCollectableEntity().getFieldNames();
	}

} // inner class CompleteCollectableMasterDataStrategy
