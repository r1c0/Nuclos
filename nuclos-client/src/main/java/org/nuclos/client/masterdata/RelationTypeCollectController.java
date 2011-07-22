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

import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;

/**
 * Controller for collecting group types.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */

public class RelationTypeCollectController extends MasterDataCollectController {
	
	/**
	 * You should use {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton} 
	 * to get an instance.
	 * 
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	public RelationTypeCollectController(JComponent parent, MainFrameTab tabIfAny) {
		super(parent, NuclosEntity.RELATIONTYPE, tabIfAny);
	}

	@Override
	protected boolean isNewAllowed() {
		return isSaveAllowed();
	}

	/**
	 * @return save is not allowed for system defined group types.
	 */
	@Override
	protected boolean isSaveAllowed() {
		boolean result = super.isSaveAllowed();
		if (result) {
			final Collectable clct = this.getCollectStateModel().getEditedCollectable();
			if (clct != null && !isWriteAllowed(clct)) {
				result = false;
			}
		}
		return result;
	}

	/**
	 * @param clct
	 * @return delete is not allowed for system defined group types.
	 */
	@Override
	protected boolean isDeleteAllowed(CollectableMasterDataWithDependants clct) {
		return super.isDeleteAllowed(clct) && this.isSaveAllowed();
	}

	@Override
	protected void unsafeFillDetailsPanel(CollectableMasterDataWithDependants clct) throws NuclosBusinessException {
		super.unsafeFillDetailsPanel(clct);
		this.respectRights(isWriteAllowed(clct));
	}

	private static boolean isWriteAllowed(Collectable clct) {
		return !isSystemDefinedGroupType(clct);
	}

	private static boolean isSystemDefinedGroupType(Collectable clct) {
		return Boolean.TRUE.equals(clct.getValue("system"));
	}

	private void respectRights(boolean bWriteAllowed) {
		for (CollectableComponent clctcomp : this.getDetailsPanel().getEditView().getCollectableComponents()) {
			clctcomp.setEnabled(bWriteAllowed && clctcomp.isEnabledByInitial());
		}
	}

}	 // class RelationTypeCollectController
