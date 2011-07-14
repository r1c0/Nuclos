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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JTree;

import org.apache.log4j.Logger;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.explorer.ExplorerNode;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.tree.TreeNodeAction;
import org.nuclos.client.wizard.ShowNuclosWizard;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.navigation.ejb3.TreeNodeFacadeRemote;
import org.nuclos.server.navigation.treenode.TreeNode;
import org.nuclos.server.navigation.treenode.nuclet.content.AbstractNucletContentEntryTreeNode;
import org.nuclos.server.navigation.treenode.nuclet.content.NucletContentEntityTreeNode;

/**
 * <code>ExplorerNode</code> presenting a <code>NucletContentEntityTreeNode</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.nuclos.de">www.nuclos.de</a>
 *
 * @author	<a href="mailto:maik.stueker@nuclos.de">maik.stueker</a>
 * @version 01.00.00
 */
public class NucletContentEntityExplorerNode extends ExplorerNode<NucletContentEntityTreeNode> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(NucletContentEntityExplorerNode.class);

	public NucletContentEntityExplorerNode(TreeNode treenode) {
		super(treenode);
	}

	@Override
	public boolean getAllowsChildren() {
		return false;
	}

	@Override
	public List<TreeNodeAction> getTreeNodeActions(JTree tree) {
		final List<TreeNodeAction> result = new LinkedList<TreeNodeAction>();
		result.add(new ShowDetailsAction(tree));
		result.add(new RemoveFromNucletAction(tree));
		return result;
	}

	@Override
	public String getDefaultTreeNodeActionCommand(JTree tree) {
		return ACTIONCOMMAND_SHOW_DETAILS;
	}
	
	/**
	 * inner class ShowDetailsAction. Shows the details for a leased object.
	 */
	private class ShowDetailsAction extends TreeNodeAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		ShowDetailsAction(JTree tree) {
			super(ACTIONCOMMAND_SHOW_DETAILS, CommonLocaleDelegate.getMessage("NucletContentEntityExplorerNode.1","Entitaetenwizard starten"), tree);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(ActionEvent ev) {
			this.cmdShowDetails((NucletContentEntityExplorerNode) this.getJTree().getSelectionPath().getLastPathComponent());
		}

		/**
		 * command: show the details of the leased object represented by the given explorernode
		 * @param explorernode
		 */
		private void cmdShowDetails(final NucletContentEntityExplorerNode explorernode) {
			UIUtils.runCommand(this.getParent(), new CommonRunnable() {
				@Override
				public void run() throws CommonBusinessException {
					ShowNuclosWizard w = new ShowNuclosWizard(true);
					w.setEntityToEdit(MetaDataClientProvider.getInstance().getEntity(getTreeNode().getEntityObjectVO().getField("entity", String.class)));
					w.showWizard(Main.getMainFrame().getHomePane(), Main.getMainFrame());
				}
			});
		}
	}	// inner class ShowDetailsAction
	
	private class RemoveFromNucletAction extends TreeNodeAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public RemoveFromNucletAction(JTree tree) {
			super(ACTIONCOMMAND_REMOVE, CommonLocaleDelegate.getMessage("NucletContentEntryExplorerNode.1","Vom Nuclet entfernen"), tree);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(ActionEvent ev) {
			this.cmdRemoveFromNuclet((NucletContentEntityExplorerNode) this.getJTree().getSelectionPath().getLastPathComponent());
		}

		/**
		 * @param explorernode
		 */
		private void cmdRemoveFromNuclet(final NucletContentEntityExplorerNode explorernode) {
			UIUtils.runCommand(this.getParent(), new CommonRunnable() {
				@Override
				public void run() throws CommonBusinessException {
					final NucletContentExplorerNode explorernodeParent = (NucletContentExplorerNode) explorernode.getParent();
					
					Set<AbstractNucletContentEntryTreeNode> remove = new HashSet<AbstractNucletContentEntryTreeNode>();
					remove.add(explorernode.getTreeNode());
					getTreeNodeFacade().removeNucletContents(remove);
					explorernodeParent.refresh(getJTree());
				}
			});
		}
	} // inner class RemoveFromNucletAction
	
	@Override
	public Icon getIcon() {
		return MainFrame.resizeAndCacheIcon(Icons.getInstance().getIconTabGeneric(), 10);
	}
	
	private TreeNodeFacadeRemote getTreeNodeFacade() throws NuclosFatalException {
		return ServiceLocator.getInstance().getFacade(TreeNodeFacadeRemote.class);
	}
}	// class MasterDataExplorerNode