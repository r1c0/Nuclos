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

import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTree;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.explorer.ExplorerNode;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.masterdata.datatransfer.MasterDataIdAndEntity;
import org.nuclos.client.resource.NuclosResourceCache;
import org.nuclos.client.resource.ResourceCache;
import org.nuclos.client.ui.DefaultSelectObjectsPanel;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.SelectObjectsController;
import org.nuclos.client.ui.SelectObjectsPanel;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.ClientPreferences;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.PreferencesUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.server.navigation.ejb3.TreeNodeFacadeRemote;
import org.nuclos.server.navigation.treenode.TreeNode;
import org.nuclos.server.navigation.treenode.nuclet.NucletTreeNode;
import org.nuclos.server.navigation.treenode.nuclet.content.AbstractNucletContentEntryTreeNode;

/**
 * <code>ExplorerNode</code> presenting a <code>NucletTreeNode</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.nuclos.de">www.nuclos.de</a>
 *
 * @author	<a href="mailto:maik.stueker@nuclos.de">maik.stueker</a>
 * @version 01.00.00
 */
public class NucletExplorerNode extends ExplorerNode<NucletTreeNode> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(NucletExplorerNode.class);
	
	private static final String PREFS_NODE_NUCLET_EXPLORER = "nucletExplorer";
	
	private static final String PREFS_NODE_ADDREMOVE_DIALOG_SIZE = "addRemoveDialogSize";
	
	private final Preferences prefs = ClientPreferences.getUserPreferences().node(PREFS_NODE_NUCLET_EXPLORER);
	
	private final JButton btnAddContent = new JButton();
	private final JButton btnRemoveContent = new JButton();

	public NucletExplorerNode(TreeNode treenode) {
		super(treenode);
		
		btnAddContent.setFocusable(false);
		btnRemoveContent.setFocusable(false);
	}
	
	@Override
	public String getDefaultTreeNodeActionCommand(JTree tree) {
		return ACTIONCOMMAND_EXPAND;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean importTransferData(Component parent, Transferable transferable, JTree tree) throws IOException, UnsupportedFlavorException {
		
		if (transferable.isDataFlavorSupported(new MasterDataIdAndEntity.DataFlavor(null))) {
			final Object transferData = transferable.getTransferData(new MasterDataIdAndEntity.DataFlavor(null));
			final Collection<MasterDataIdAndEntity> collimp = (Collection<MasterDataIdAndEntity>)transferData;
			
			Set<AbstractNucletContentEntryTreeNode> contents = new HashSet<AbstractNucletContentEntryTreeNode>();
			for (MasterDataIdAndEntity mdiden : collimp) {
				NuclosEntity entity = NuclosEntity.getByName(mdiden.getEntity());
				if (entity == null) {
					return false;
				}
				Long eoid = ((Integer)mdiden.getId()).longValue();
				if (eoid >= 0) {
					contents.add(getTreeNodeFacade().getNucletContentEntryNode(entity, eoid));
				}
			}
			if (!contents.isEmpty()) {
				try {
					getTreeNodeFacade().addNucletContents(getTreeNode().getId().longValue(), contents);
					refresh(tree);
				} catch(Exception e) {
					Errors.getInstance().showExceptionDialog(getExplorerController().getParent(), e);
				}
			}
			
			return true;
		} else {
			return super.importTransferData(parent, transferable, tree);
		}
	}

	/**
	 * 
	 */
	@Override
	public List<JComponent> getToolBarComponents(final JTree jTree) {
		btnAddContent.setAction(new AbstractAction(null, Icons.getInstance().getIconPlus16()) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				cmdShowAddDialog(jTree);
			}
		});
		btnRemoveContent.setAction(new AbstractAction(null, Icons.getInstance().getIconMinus16()) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				cmdShowRemoveDialog(jTree);
			}
		});
		btnAddContent.setToolTipText(CommonLocaleDelegate.getText("NucletExplorerNode.1", "Hinzufuegen"));
		btnRemoveContent.setToolTipText(CommonLocaleDelegate.getText("NucletExplorerNode.3", "Entfernen"));
		
		List<JComponent> result = new ArrayList<JComponent>();
		
//		JPanel jpnComponents = new JPanel(new FlowLayout());
//		jpnComponents.add(btnAddContent);
//		jpnComponents.add(btnRemoveContent);
//		BlackLabel blComponents = new BlackLabel(jpnComponents, CommonLocaleDelegate.getText("NucletExplorerNode.5", "Bestandteile"));
//		result.add(blComponents);
		result.add(btnAddContent);
		result.add(btnRemoveContent);
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private void cmdShowAddDialog(final JTree jTree) {
		final SelectObjectsPanel selectPanel = new NucletContentSelectObjectPanel();
		SelectObjectsController selectCtrl = new SelectObjectsController(null) {
			@Override
			protected SelectObjectsPanel getPanel() {
				return selectPanel;
			}
		};
		
		final boolean userPressedOk = selectCtrl.run(
			getTreeNodeFacade().getAvailableNucletContents(), 
			new ArrayList<AbstractNucletContentEntryTreeNode>(), 
			new AbstractNucletContentEntryTreeNode.Comparator(), CommonLocaleDelegate.getText("NucletExplorerNode.2", "Zum Nuclet hinzufuegen") + "...");
		PreferencesUtils.putRectangle(prefs, PREFS_NODE_ADDREMOVE_DIALOG_SIZE, selectPanel.getBounds());
		
		if (userPressedOk) {
			try {
				getTreeNodeFacade().addNucletContents(getTreeNode().getId().longValue(), new HashSet<AbstractNucletContentEntryTreeNode>((List<AbstractNucletContentEntryTreeNode>) selectCtrl.getSelectedObjects()));
				refresh(jTree);
			} catch(Exception e) {
				Errors.getInstance().showExceptionDialog(getExplorerController().getParent(), e);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void cmdShowRemoveDialog(final JTree jTree) {
		final SelectObjectsPanel selectPanel = new NucletContentSelectObjectPanel();
		SelectObjectsController selectCtrl = new SelectObjectsController(null) {
			@Override
			protected SelectObjectsPanel getPanel() {
				return selectPanel;
			}
		};
		
		final boolean userPressedOk = selectCtrl.run(
			getTreeNodeFacade().getNucletContent(getTreeNode()), 
			new ArrayList<AbstractNucletContentEntryTreeNode>(), 
			new AbstractNucletContentEntryTreeNode.Comparator(), CommonLocaleDelegate.getText("NucletExplorerNode.4", "Vom Nuclet entfernen") + "...");
		PreferencesUtils.putRectangle(prefs, PREFS_NODE_ADDREMOVE_DIALOG_SIZE, selectPanel.getBounds());
		
		if (userPressedOk) {
			try {
				getTreeNodeFacade().removeNucletContents(new HashSet<AbstractNucletContentEntryTreeNode>((List<AbstractNucletContentEntryTreeNode>) selectCtrl.getSelectedObjects()));
				refresh(jTree);
			} catch(Exception e) {
				Errors.getInstance().showExceptionDialog(getExplorerController().getParent(), e);
			}
		}
	}
	
	private class NucletContentSelectObjectPanel extends DefaultSelectObjectsPanel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public NucletContentSelectObjectPanel() {
			super();
			btnDown.setVisible(false);
			btnUp.setVisible(false);
			setPreferredSize(PreferencesUtils.getRectangle(prefs, PREFS_NODE_ADDREMOVE_DIALOG_SIZE, 720, 480).getSize());
		}

		@Override
		protected JList newList() {
			final JList result = super.newList();
			result.setCellRenderer(new DefaultListCellRenderer() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
					if (value instanceof AbstractNucletContentEntryTreeNode) {
						AbstractNucletContentEntryTreeNode node = (AbstractNucletContentEntryTreeNode) value;
						String text = node.getLabelWithEntity();
						JLabel lb = new JLabel(text, NucletExplorerNode.getIcon(node.getEntity().getEntityName()), SwingConstants.LEFT);
						if (isSelected) {
							lb.setOpaque(true);
							lb.setBackground(result.getSelectionBackground());
							lb.setForeground(result.getSelectionForeground());
						}
						return lb;
					}
					return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				}
			});
			return result;
		}
	}
	
	@Override
	protected void cmdShowInOwnTabAction() {
		NucletTreeNode node = getTreeNode();
		getExplorerController().cmdShowInOwnTab(new NucletTreeNode(node.getId(), node.getLabel(), node.getDescription(), false));
	}
	
	private static Icon getIcon(String entity) {
		Integer resId = MetaDataClientProvider.getInstance().getEntity(entity).getResourceId();
		String nuclosResource = MetaDataClientProvider.getInstance().getEntity(entity).getNuclosResource();
		if(resId != null) {
			ImageIcon standardIcon = ResourceCache.getIconResource(resId);
			return MainFrame.resizeAndCacheTabIcon(standardIcon);		
		} else if (nuclosResource != null){
			ImageIcon nuclosIcon = NuclosResourceCache.getNuclosResourceIcon(nuclosResource);
			if (nuclosIcon != null) return MainFrame.resizeAndCacheTabIcon(nuclosIcon);
		}
		return Icons.getInstance().getIconGenericObject16();
	}
	
	private TreeNodeFacadeRemote getTreeNodeFacade() throws NuclosFatalException {
		return ServiceLocator.getInstance().getFacade(TreeNodeFacadeRemote.class);
	}
	
}
