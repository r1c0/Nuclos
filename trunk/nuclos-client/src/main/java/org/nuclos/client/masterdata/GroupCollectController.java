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

import javax.swing.JComponent;

import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.common.NuclosEntity;

/**
 * <code>MasterDataCollectController</code> for entity "group".
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class GroupCollectController extends MasterDataCollectController {

	/**
	 * You should use {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton} 
	 * to get an instance.
	 * 
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	public GroupCollectController(JComponent parent, MainFrameTab tabIfAny) {
		super(parent, NuclosEntity.GROUP, tabIfAny);
	}

	@Override
	protected void deleteCollectable(CollectableMasterDataWithDependants clct) throws CommonBusinessException {
		super.deleteCollectable(clct);
		SecurityCache.getInstance().revalidate();
	}

	@Override
	protected CollectableMasterDataWithDependants insertCollectable(CollectableMasterDataWithDependants clctNew) throws CommonBusinessException {
		CollectableMasterDataWithDependants cmdwd = super.insertCollectable(clctNew);
		SecurityCache.getInstance().revalidate();
		return cmdwd;
	}

	@Override
	protected CollectableMasterDataWithDependants updateCollectable(CollectableMasterDataWithDependants clct, Object oAdditionalData) throws CommonBusinessException {
		CollectableMasterDataWithDependants cmdwd = super.updateCollectable(clct, oAdditionalData);
		SecurityCache.getInstance().revalidate();
		return cmdwd;
	}
}	// class GroupCollectController
