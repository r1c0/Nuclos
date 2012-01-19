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
package org.nuclos.client.ui.collect.result;

import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common2.exception.CommonBusinessException;

public class NuclosSearchResultStrategy<Clct extends Collectable> extends SearchResultStrategy<Clct> {
	
	public NuclosSearchResultStrategy() {
	}

	// search stuff
	
	/**
	 * @deprecated Use {@link #cmdRefreshResult(List)}
	public void refreshResult(List<Observer> lstObserver) throws CommonBusinessException {
		cmdRefreshResult(lstObserver);
	}
	 */

	/**
	 * Make visible for ResultPanel
	 * 
	 * @deprecated Use {@link #cmdRefreshResult()}
	 */
	@Override
	public void refreshResult() throws CommonBusinessException {
		//super.refreshResult();
		cmdRefreshResult();
	}	

}
