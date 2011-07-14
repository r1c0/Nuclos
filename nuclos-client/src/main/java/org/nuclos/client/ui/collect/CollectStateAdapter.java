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

import org.nuclos.common2.exception.CommonBusinessException;

/**
 * Provides default implementations for the <code>CollectStateListener</code> methods.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public class CollectStateAdapter implements CollectStateListener {

	@Override
	public void searchModeEntered(CollectStateEvent ev) throws CommonBusinessException {
	}

	@Override
	public void searchModeLeft(CollectStateEvent ev) throws CommonBusinessException {
	}

	@Override
	public void resultModeEntered(CollectStateEvent ev) throws CommonBusinessException {
	}

	@Override
	public void resultModeLeft(CollectStateEvent ev) throws CommonBusinessException {
	}

	@Override
	public void detailsModeEntered(CollectStateEvent ev) throws CommonBusinessException {
	}

	@Override
	public void detailsModeLeft(CollectStateEvent ev) throws CommonBusinessException {
	}

}  // class CollectStateAdapter