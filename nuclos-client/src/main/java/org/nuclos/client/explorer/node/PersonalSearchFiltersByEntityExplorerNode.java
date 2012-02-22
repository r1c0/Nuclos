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

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JTree;

import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.common.NuclosCollectControllerFactory;
import org.nuclos.client.explorer.ExplorerNode;
import org.nuclos.client.explorer.PersonalSearchFiltersByEntityTreeNode;
import org.nuclos.client.genericobject.GenericObjectDelegate;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.resource.NuclosResourceCache;
import org.nuclos.client.resource.ResourceCache;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.tree.TreeNodeAction;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.navigation.treenode.TreeNode;

/**
 * <code>ExplorerNode</code> for <code>PersonalSearchFiltersByModuleTreeNode</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
public class PersonalSearchFiltersByEntityExplorerNode extends ExplorerNode<PersonalSearchFiltersByEntityTreeNode> {

	private static final String ACTIONCOMMAND_SEARCHINENTITY = "SEARCH IN ENTITY";

	public PersonalSearchFiltersByEntityExplorerNode(TreeNode treenode) {
		super(treenode);
	}

	@Override
	public Icon getIcon() {
		if (Modules.getInstance().isModuleEntity(getTreeNode().getEntity())) {
			String sResourceName = GenericObjectDelegate.getInstance().getResourceMap().get(getTreeNode().getModuleId());
			String nuclosResource = MetaDataClientProvider.getInstance().getEntity(Modules.getInstance().getEntityNameByModuleId(getTreeNode().getModuleId())).getNuclosResource();
			if (sResourceName != null && ResourceCache.getInstance().getIconResource(sResourceName) != null) {
					return MainFrame.resizeAndCacheTabIcon(ResourceCache.getInstance().getIconResource(sResourceName));
			} else if (nuclosResource != null){
				ImageIcon nuclosIcon = NuclosResourceCache.getNuclosResourceIcon(nuclosResource);
				if (nuclosIcon != null) return MainFrame.resizeAndCacheTabIcon(nuclosIcon);
			}
			return Icons.getInstance().getIconModule();

		}
		else {
			Integer resId = MetaDataClientProvider.getInstance().getEntity(getTreeNode().getEntity()).getResourceId();
			String nuclosResource = MetaDataClientProvider.getInstance().getEntity(getTreeNode().getEntity()).getNuclosResource();
			if(resId != null) {
				ImageIcon standardIcon = ResourceCache.getInstance().getIconResource(resId);
				return MainFrame.resizeAndCacheTabIcon(standardIcon);		
			} else if (nuclosResource != null){
				ImageIcon nuclosIcon = NuclosResourceCache.getNuclosResourceIcon(nuclosResource);
				if (nuclosIcon != null) return MainFrame.resizeAndCacheTabIcon(nuclosIcon);
			}
			return Icons.getInstance().getIconGenericObject16();
		}
	}

	@Override
	public List<TreeNodeAction> getTreeNodeActions(JTree tree) {
		final List<TreeNodeAction> result = super.getTreeNodeActions(tree);
		result.add(TreeNodeAction.newSeparatorAction());
		result.add(new SearchInEntityAction(tree));
		return result;
	}

	@Override
	public String getDefaultTreeNodeActionCommand(JTree tree) {
		return ACTIONCOMMAND_SEARCHINENTITY;
	}

	private class SearchInEntityAction extends TreeNodeAction {

		SearchInEntityAction(JTree tree) {
			super(ACTIONCOMMAND_SEARCHINENTITY, 
					getSpringLocaleDelegate().getMessage("PersonalSearchFiltersByEntityExplorerNode.1","Suchen") + "...", tree);
		}

		@Override
        public void actionPerformed(ActionEvent ev) {
			final JComponent parent = MainFrame.getPredefinedEntityOpenLocation(getTreeNode().getEntity());
			UIUtils.runCommand(getJTree(), new CommonRunnable() {
				@Override
                public void run() throws CommonBusinessException {
					NuclosCollectControllerFactory.getInstance().newCollectController(parent, getTreeNode().getEntity(), null).run();
				}
			});
		}

	}	// inner class SearchInEntityAction

}	// class PersonalSearchFiltersByEntityExplorerNode
