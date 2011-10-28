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
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTree;

import org.apache.log4j.Logger;
import org.nuclos.client.common.NuclosCollectControllerFactory;
import org.nuclos.client.explorer.ExplorerNode;
import org.nuclos.client.genericobject.CollectableGenericObjectWithDependants;
import org.nuclos.client.genericobject.GenericObjectCollectController;
import org.nuclos.client.genericobject.GenericObjectDelegate;
import org.nuclos.client.main.Main;
import org.nuclos.client.ui.Bubble;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.tree.TreeNodeAction;
import org.nuclos.common.MutableBoolean;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.navigation.treenode.SubFormTreeNode;
import org.nuclos.server.navigation.treenode.TreeNode;

/**
 * <code>ExplorerNode</code> presenting a <code>SubFormTreeNode</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class SubFormExplorerNode<TN extends SubFormTreeNode<Integer>> extends ExplorerNode<TN> {

	private static final Logger log = Logger.getLogger(SubFormExplorerNode.class);

	public SubFormExplorerNode(TreeNode treenode) {
		super(treenode);
	}

	@Override
	public List<TreeNodeAction> getTreeNodeActions(JTree tree) {
		final List<TreeNodeAction> result = new LinkedList<TreeNodeAction>(super.getTreeNodeActions(tree));
		result.add(TreeNodeAction.newSeparatorAction());
		result.add(newShowListAction(tree));
		return result;
	}

	@Override
	public String getDefaultTreeNodeActionCommand(JTree tree) {
		return getDefaultFolderNodeAction();
	}

	@Override
    public boolean importTransferData(final Component parent, final Transferable transferable, final JTree tree) throws IOException, UnsupportedFlavorException {
	    // Import like drop in subform

		final MutableBoolean mb = new MutableBoolean();

		UIUtils.runCommand(parent, new CommonRunnable() {
			@Override
			public void run() throws CommonBusinessException {
				Integer iTargetObjectId = getTreeNode().getGenericObjectTreeNode().getId();
				Integer iTargetModuleId = getTreeNode().getGenericObjectTreeNode().getModuleId();
				final String sTargetSubFormEntity = getTreeNode().getMasterDataVO().getField("entity", String.class);
				String sTargetSubFormForeignField = getTreeNode().getMasterDataVO().getField("field", String.class);

				final GenericObjectCollectController goController = NuclosCollectControllerFactory.getInstance().newGenericObjectCollectController(
					Main.getMainFrame().getHomePane(), iTargetModuleId, null);

				goController.runViewSingleCollectable(
					CollectableGenericObjectWithDependants.newCollectableGenericObject(
						GenericObjectDelegate.getInstance().get(iTargetObjectId)), false);

				try {
					int[] importResult = goController.dropOnSubForm(sTargetSubFormEntity, transferable);
					if (importResult[0] > 0) {
						try {
							goController.save();
							refresh(tree);
							goController.getFrame().dispose();
						} catch (Exception e) {
							// save failed... open controller
							goController.getFrame().setVisible(true);
							showBubbleCenter(goController.getFrame(), CommonLocaleDelegate.getMessage("SubFormExplorerNode.1", "Ein Bearbeitungsfenster Objekt wurde geöffnet, ein Speichern ohne Benutzereingriff ist nicht möglich.<br/> Bitte prüfen Sie die Meldung: {0}", Errors.formatErrorForBubble(e.getMessage())));
						}
					}
					if (importResult[1] > 0) {
						showBubbleRight(tree, CommonLocaleDelegate.getMessage("MasterDataSubFormController.5", "Der Valuelist Provider verhindert das Anlegen von ${count} Unterformular Datensätzen.", importResult[1]));
					}
				} catch (NuclosBusinessException nbe) {
					// dropped failed
					showBubbleRight(tree, nbe.getMessage());
				}
			}
		});

		final boolean result = mb.getValue();

		return result;
    }

	private void showBubbleRight(JComponent component, String message) {
		new Bubble(
			component,
			message,
			10,
			Bubble.Position.NW)
		.setVisible(true);
	}

	private void showBubbleCenter(JComponent component, String message) {
		new Bubble(
			component,
			message,
			20,
			Bubble.Position.NO_ARROW_CENTER)
		.setVisible(true);
	}

}	// class MasterDataExplorerNode
