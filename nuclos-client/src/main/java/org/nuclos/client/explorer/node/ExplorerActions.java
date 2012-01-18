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

package org.nuclos.client.explorer.node;

import org.nuclos.client.main.Main;
import org.nuclos.client.masterdata.MasterDataLayoutHelper;
import org.nuclos.common2.exception.CommonBusinessException;

public class ExplorerActions {
	private ExplorerActions() {}
	
	/**
	 * Show the details view for an EntityExplorerNode. If the node does
	 * not have a layout, propagate through the tree, until we finally find
	 * a parent that can be opened, or finally fail
	 */
	public static void openDetails(EntityExplorerNode callEntity) throws CommonBusinessException {
		EntityExplorerNode en = callEntity;
		String entityName = en.getEntity();
		Long id = en.getId();
		
		// If no layout is available, we retrace the tree, looking
		// for the most recent ancestor that has a layout.
		while(!MasterDataLayoutHelper.isLayoutMLAvailable(entityName, false)) {
			// -> parent entity & parent id
			javax.swing.tree.TreeNode[] path = en.getPath();
			boolean foundParent = false;
			for(int i = path.length - 2; i >= 0 && !foundParent; i--) {
				if(path[i] instanceof EntityExplorerNode) {
					foundParent = true;
					en = (EntityExplorerNode) path[i];
					id = en.getId();
					entityName = en.getEntity();
				}
			}
			
			if(!foundParent) {
				// Let maincontroller.showDetails throw a missing layout error to the user
				// Restore original values for the message
				entityName = callEntity.getEntity();
				id = callEntity.getId();
				break;
			}
		}
		
		Main.getInstance().getMainController().showDetails(entityName, id);
	}
}
