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
package org.nuclos.client.explorer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import org.nuclos.client.explorer.node.NucletExplorerNode;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.resource.NuclosResourceCache;
import org.nuclos.client.resource.ResourceCache;
import org.nuclos.client.ui.DefaultSelectObjectsPanel;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.SelectObjectsController;
import org.nuclos.client.ui.model.ChoiceList;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.ClientPreferences;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.PreferencesUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.server.navigation.ejb3.TreeNodeFacadeRemote;
import org.nuclos.server.navigation.treenode.nuclet.NucletTreeNode;
import org.nuclos.server.navigation.treenode.nuclet.content.AbstractNucletContentEntryTreeNode;

public class NucletExplorerView extends DefaultExplorerView implements ExplorerView {

	private static final long serialVersionUID = 5014512105916869563L;

	private static final Logger LOG = Logger.getLogger(NucletExplorerNode.class);

	private static final String PREFS_NODE_NUCLET_EXPLORER = "nucletExplorer";

	private static final String PREFS_NODE_ADDREMOVE_DIALOG_SIZE = "addRemoveDialogSize";

	private final Preferences prefs = ClientPreferences.getUserPreferences().node(PREFS_NODE_NUCLET_EXPLORER);

	private final NucletTreeNode nucletnode;

	private final JButton btnAddContent = new JButton();
	private final JButton btnRemoveContent = new JButton();

	public NucletExplorerView(NucletTreeNode treenode) {
		super(treenode);
		this.nucletnode = treenode;

		btnAddContent.setFocusable(false);
		btnRemoveContent.setFocusable(false);
	}

	@Override
	public List<JComponent> getToolBarComponents() {
		btnAddContent.setAction(new AbstractAction(null, Icons.getInstance().getIconPlus16()) {

			@Override
			public void actionPerformed(ActionEvent e) {
				cmdShowAddDialog(getJTree());
			}
		});

		btnRemoveContent.setAction(new AbstractAction(null, Icons.getInstance().getIconMinus16()) {

			@Override
			public void actionPerformed(ActionEvent e) {
				cmdShowRemoveDialog(getJTree());
			}
		});
		btnAddContent.setToolTipText(CommonLocaleDelegate.getText("NucletExplorerNode.1", "Hinzufuegen"));
		btnRemoveContent.setToolTipText(CommonLocaleDelegate.getText("NucletExplorerNode.3", "Entfernen"));

		List<JComponent> result = new ArrayList<JComponent>();

		result.add(btnAddContent);
		result.add(btnRemoveContent);

		return result;
	}

	private void cmdShowAddDialog(final JTree jTree) {
		SelectObjectsController<AbstractNucletContentEntryTreeNode> selectCtrl =
			new SelectObjectsController<AbstractNucletContentEntryTreeNode>(null, new NucletContentSelectObjectPanel());

		ChoiceList<AbstractNucletContentEntryTreeNode> ro = new ChoiceList<AbstractNucletContentEntryTreeNode>();
		ro.set(getTreeNodeFacade().getAvailableNucletContents(),
				new AbstractNucletContentEntryTreeNode.Comparator());

		selectCtrl.setModel(ro);
		final boolean userPressedOk = selectCtrl.run(
				CommonLocaleDelegate.getText("NucletExplorerNode.2", "Zum Nuclet hinzufuegen") + "...");
		final NucletContentSelectObjectPanel selectPanel = (NucletContentSelectObjectPanel) selectCtrl.getPanel();
		PreferencesUtils.putRectangle(prefs, PREFS_NODE_ADDREMOVE_DIALOG_SIZE, selectPanel.getBounds());

		if (userPressedOk) {
			try {
				getTreeNodeFacade().addNucletContents(nucletnode.getId().longValue(), new HashSet<AbstractNucletContentEntryTreeNode>((List<AbstractNucletContentEntryTreeNode>) selectCtrl.getSelectedObjects()));
				getExplorerController().refreshTab(NucletExplorerView.this);
			} catch(Exception e) {
				Errors.getInstance().showExceptionDialog(getExplorerController().getParent(), e);
			}
		}
	}

	private void cmdShowRemoveDialog(final JTree jTree) {
		SelectObjectsController<AbstractNucletContentEntryTreeNode> selectCtrl =
				new SelectObjectsController<AbstractNucletContentEntryTreeNode>(null, new NucletContentSelectObjectPanel());

		final ChoiceList<AbstractNucletContentEntryTreeNode> ro = new ChoiceList<AbstractNucletContentEntryTreeNode>();
		ro.set(getTreeNodeFacade().getNucletContent(nucletnode),
				new AbstractNucletContentEntryTreeNode.Comparator());
		selectCtrl.setModel(ro);
		final boolean userPressedOk = selectCtrl.run(
				CommonLocaleDelegate.getText("NucletExplorerNode.4", "Vom Nuclet entfernen") + "...");

		NucletContentSelectObjectPanel selectPanel = (NucletContentSelectObjectPanel) selectCtrl.getPanel();
		PreferencesUtils.putRectangle(prefs, PREFS_NODE_ADDREMOVE_DIALOG_SIZE, selectPanel.getBounds());

		if (userPressedOk) {
			try {
				getTreeNodeFacade().removeNucletContents(new HashSet<AbstractNucletContentEntryTreeNode>((List<AbstractNucletContentEntryTreeNode>) selectCtrl.getSelectedObjects()));
				getExplorerController().refreshTab(NucletExplorerView.this);
			} catch(Exception e) {
				Errors.getInstance().showExceptionDialog(getExplorerController().getParent(), e);
			}
		}
	}

	private class NucletContentSelectObjectPanel<T> extends DefaultSelectObjectsPanel<T> {

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

				@Override
				public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
					if (value instanceof AbstractNucletContentEntryTreeNode) {
						AbstractNucletContentEntryTreeNode node = (AbstractNucletContentEntryTreeNode) value;
						String text = node.getLabelWithEntity();
						JLabel lb = new JLabel(text, NucletExplorerView.getIcon(node.getEntity().getEntityName()), SwingConstants.LEFT);
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

	private ExplorerController getExplorerController() {
		return Main.getMainController().getExplorerController();
	}
}
