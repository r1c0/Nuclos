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
package org.nuclos.client.explorer;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.common.NuclosCollectControllerFactory;
import org.nuclos.client.common.Utils;
import org.nuclos.client.explorer.node.GenericObjectExplorerNode;
import org.nuclos.client.explorer.node.GroupExplorerNode;
import org.nuclos.client.genericobject.GenericObjectCollectController;
import org.nuclos.client.genericobject.GenericObjectDelegate;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.ui.CommonAbstractAction;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.tree.TreeNodeAction;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.SearchConditionUtils;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.genericobject.searchcondition.CollectableGenericObjectSearchExpression;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode;
import org.nuclos.server.navigation.treenode.TreeNode;

public class DefaultMouseListener extends MouseAdapter {

	private static final Logger LOG = Logger.getLogger(DefaultMouseListener.class);

	private final JTree tree;

	public DefaultMouseListener(JTree tree) {
		super();
		this.tree = tree;
	}

	@Override
	public void mousePressed(MouseEvent ev) {
		mouseEventOnNode(ev);
	}

	@Override
	public void mouseReleased(MouseEvent ev) {
		mouseEventOnNode(ev);
	}

	@Override
	public void mouseClicked(MouseEvent ev) {
		mouseEventOnNode(ev);
	}

	private void mouseEventOnNode(MouseEvent ev) {
		final int selRow = tree.getRowForLocation(ev.getX(), ev.getY());
		final TreePath treepath = tree.getPathForLocation(ev.getX(), ev.getY());
		if (selRow != -1) {
			mouseEventOnNode(treepath, ev);
		}
	}

	/**
	 * event: a mouse event occured on a node in the <code>view</code>
	 * @param pathEvent the path of the node where the mouse event occured.
	 * @param ev
	 */
	private void mouseEventOnNode(TreePath pathEvent, MouseEvent ev) {
		final ExplorerNode<? extends TreeNode> node = (ExplorerNode<?>) pathEvent.getLastPathComponent();
		final JTree tree = (JTree) ev.getComponent();

		// if the node isn't selected already:
		final TreePath[] aSelectionPaths = tree.getSelectionPaths();
		if (aSelectionPaths == null || !Arrays.asList(aSelectionPaths).contains(pathEvent)) {
			// select it (and unselect all others):
			tree.setSelectionPath(pathEvent);
		}

		if (ev.isPopupTrigger()) {
//			// if the node isn't selected already:
//			final TreePath[] aSelectionPaths = tree.getSelectionPaths();
//			if (aSelectionPaths == null || !Arrays.asList(aSelectionPaths).contains(pathEvent)) {
//				// select it (and unselect all others):
//				tree.setSelectionPath(pathEvent);
//			}

			assert tree.getSelectionCount() >= 1;
			final JPopupMenu popupmenu = (tree.getSelectionCount() == 1) ?
					this.newPopupMenuForSingleNode(node, tree) :
					this.newPopupMenuForMultipleNodes(ExplorerController.getSelectedExplorerNodes(tree), tree);

			if (popupmenu != null) {
				popupmenu.show(ev.getComponent(), ev.getX(), ev.getY());
			}
		}
		else if (ev.getID() == MouseEvent.MOUSE_CLICKED) {
			if (ev.getButton() == MouseEvent.BUTTON1) {
				if (ev.getClickCount() == 2) {
					// perform the node's default action:
					final Action actDefault = node.getDefaultTreeNodeAction(tree);
					if (actDefault != null) {
						actDefault.actionPerformed(null);
					}
				}
			}
		}
	}

	/**
	 * @param node
	 * @param tree the tree that is to contain the popup menu
	 * @return the popupmenu for the given node
	 */
	private JPopupMenu newPopupMenuForSingleNode(ExplorerNode<? extends TreeNode> node, JTree tree) {
		final JPopupMenu result = new JPopupMenu();

		final TreeNodeAction actDefault = node.getDefaultTreeNodeAction(tree);
		for (TreeNodeAction act : node.getTreeNodeActions(tree)) {
			if (act == null) {
				LOG.warn("exploreraction == null");
			}
			else {
				final boolean bDefault = (actDefault != null && LangUtils.equals(act.getValue(Action.ACTION_COMMAND_KEY), actDefault.getValue(Action.ACTION_COMMAND_KEY)));
				act.addToMenu(result, bDefault);
			}
		}
		return result;
	}

	/**
	 * @return the popup menu to be used when multiple nodes are selected.
	 * @precondition !CollectionUtils.isNullOrEmpty(collexplorernodeSelected)
	 */
	private JPopupMenu newPopupMenuForMultipleNodes(final Collection<ExplorerNode<?>> collexplorernodeSelected, final JTree tree) {
		if (CollectionUtils.isNullOrEmpty(collexplorernodeSelected)) {
			throw new IllegalArgumentException();
		}

		final JPopupMenu result;

		final Collection<GenericObjectExplorerNode> collloexplorernodeSelected =
				CollectionUtils.selectInstancesOf(collexplorernodeSelected, GenericObjectExplorerNode.class);

		if (collloexplorernodeSelected.size() < collexplorernodeSelected.size()) {
			// "In Liste anzeigen" is allowed only if all selected objects are generic objects.
			result = null;
		}
		else {
			result = new JPopupMenu();

			final CollectableSearchCondition cond = getCollectableSearchCondition(collexplorernodeSelected);

			final Action actShowInList = new CommonAbstractAction(CommonLocaleDelegate.getMessage("ExplorerController.22","In Liste anzeigen"), null, CommonLocaleDelegate.getMessage("ExplorerController.4","Ausgew\u00e4hlte Objekte in Ergebnisliste anzeigen")) {

				@Override
                public void actionPerformed(ActionEvent ev) {
					UIUtils.runCommand(UIUtils.getFrameForComponent(tree), new CommonRunnable() {
						@Override
                        public void run() throws CommonBusinessException {
							final Integer iModuleId = getCommonModuleId(collloexplorernodeSelected);
							final GenericObjectCollectController ctlGenericObject = NuclosCollectControllerFactory.getInstance().
									newGenericObjectCollectController(MainFrame.getPredefinedEntityOpenLocation(MetaDataClientProvider.getInstance().getEntity(iModuleId.longValue()).getEntity()), iModuleId, null);
							ctlGenericObject.setSearchDeleted(CollectableGenericObjectSearchExpression.SEARCH_BOTH);
							ctlGenericObject.runViewResults(cond);
						}
					});
				}
			};

			result.add(actShowInList);

			/* remove relation action */
			boolean bShowRemoveRelationAction = true;

			for(GenericObjectExplorerNode goexplorernode :collloexplorernodeSelected) {
				if (!goexplorernode.isRelated()) {
					bShowRemoveRelationAction = false;
					break;
				}
			}

			if (bShowRemoveRelationAction) {
				final Action actRemoveRelation = new CommonAbstractAction(CommonLocaleDelegate.getMessage("ExplorerController.5","Beziehungen entfernen"), null, CommonLocaleDelegate.getMessage("ExplorerController.9","Beziehung von ausgew\u00e4hlten Objekten zu \u00fcbergordnetem Object entfernen")) {

					@Override
                    public void actionPerformed(ActionEvent ev) {

						final String sMessage = CommonLocaleDelegate.getMessage("ExplorerController.28","Sollen die Beziehungen von den ausgew\u00e4hlten Objekten zu dem \u00fcbergeordneten Object entfernt werden")+ "?";

						final int iBtn = JOptionPane.showConfirmDialog(tree, sMessage, CommonLocaleDelegate.getMessage("ExplorerController.6","Beziehung entfernen"), JOptionPane.OK_CANCEL_OPTION);
						if (iBtn == JOptionPane.OK_OPTION) {
							SwingUtilities.invokeLater(new Runnable() {

								@Override
								public void run() {
									UIUtils.setWaitCursor(tree);
									try {
										Map<Integer, GenericObjectTreeNode> mpGOTreeNodeRelation = new HashMap<Integer, GenericObjectTreeNode>();
										Set<ExplorerNode<GenericObjectTreeNode>> stGOExplorerNodeParent = new HashSet<ExplorerNode<GenericObjectTreeNode>>();

										for(GenericObjectExplorerNode goexplorernode : collloexplorernodeSelected) {
											final GenericObjectTreeNode gotreenode = goexplorernode.getTreeNode();
											final ExplorerNode<GenericObjectTreeNode> explorernodeParent = (ExplorerNode<GenericObjectTreeNode>) goexplorernode.getParent();
											final boolean bForward = gotreenode.getRelationDirection().isForward();
											final GenericObjectTreeNode gotreenodeParent = explorernodeParent.getTreeNode();
											final GenericObjectTreeNode gotreenodeTarget = bForward ? gotreenode : gotreenodeParent;
											final Integer iRelationId = gotreenode.getRelationId();

											if (!stGOExplorerNodeParent.contains(explorernodeParent)) {
												stGOExplorerNodeParent.add(explorernodeParent);
											}

											if (iRelationId == null) {
												// for backwards compatibility only: this might happen for old deserialized nodes that don't have a relation id yet.
												throw new CommonBusinessException(CommonLocaleDelegate.getMessage("ExplorerController.14","Die Beziehung kann nicht entfernt werden, da die Beziehungs-Id mindestens eines Objektes fehlt. Bitte aktualisieren Sie die Baumansicht und versuchen Sie es erneut."));
											}
											else {
												mpGOTreeNodeRelation.put(iRelationId, gotreenodeTarget);
											}
										}
										GenericObjectDelegate.getInstance().removeRelation(mpGOTreeNodeRelation);

										for (ExplorerNode<GenericObjectTreeNode> explorernodeParent : stGOExplorerNodeParent) {
											explorernodeParent.refresh(tree);
										}
										tree.setCursor(null);
									}
									catch (Exception ex) {
										LOG.error(ex);
										Errors.getInstance().showExceptionDialog(null, CommonLocaleDelegate.getMessage("ExplorerController.18","Fehler beim entfernen der Beziehungen"), ex);
									}
								}
							});
						}
					}
				};
				result.add(actRemoveRelation);
			}

			/* remove from parent group action */
			boolean bShowRemoveFromParentGroupAction = true;

			for(GenericObjectExplorerNode goexplorernode :collloexplorernodeSelected) {
				if (goexplorernode.getParent() == null || (goexplorernode.getParent() != null && !(goexplorernode.getParent() instanceof GroupExplorerNode))) {
					bShowRemoveFromParentGroupAction = false;
				}
			}

			if (bShowRemoveFromParentGroupAction) {
				final Action actRemoveRelation = new CommonAbstractAction(CommonLocaleDelegate.getMessage("ExplorerController.3","Aus der Gruppe entfernen"), null, CommonLocaleDelegate.getMessage("ExplorerController.8","Beziehung von ausgew\u00e4hlten Objekten zur Objektgruppe entfernen")) {

					@Override
                    public void actionPerformed(ActionEvent ev) {

						final String sMessage = CommonLocaleDelegate.getMessage("ExplorerController.29","Sollen die Beziehungen von den ausgew\u00e4hlten Objekten zur Objektgruppe entfernt werden") + "?";

						final int iBtn = JOptionPane.showConfirmDialog(tree, sMessage, CommonLocaleDelegate.getMessage("ExplorerController.7","Beziehung entfernen"), JOptionPane.OK_CANCEL_OPTION);
						if (iBtn == JOptionPane.OK_OPTION) {
							SwingUtilities.invokeLater(new Runnable() {

								@Override
								public void run() {
									UIUtils.setWaitCursor(tree);
									try {
										Integer iGroupId = null;
										Map<Integer, Integer> mpGOGroupRelation = new HashMap<Integer, Integer>();
										Set<GroupExplorerNode> stGroupExplorerNode = new HashSet<GroupExplorerNode>();

										for(GenericObjectExplorerNode goexplorernode :collloexplorernodeSelected) {
											assert goexplorernode.getParent() instanceof GroupExplorerNode;
											if (!stGroupExplorerNode.contains(goexplorernode.getParent())) {
												stGroupExplorerNode.add((GroupExplorerNode)goexplorernode.getParent());
											}

											iGroupId = ((GroupExplorerNode) goexplorernode.getParent()).getTreeNode().getId();
											mpGOGroupRelation.put(goexplorernode.getTreeNode().getId(), iGroupId);
										}

										try {
											GenericObjectDelegate.getInstance().removeFromGroup(mpGOGroupRelation);

											for (GroupExplorerNode explorernodeParent : stGroupExplorerNode) {
												explorernodeParent.refresh(tree);
											}
										}
										catch (CommonBusinessException ex) {
											Errors.getInstance().showExceptionDialog(tree, ex);
										}
										tree.setCursor(null);
									}
									catch (Exception ex) {
										LOG.error(ex);
										Errors.getInstance().showExceptionDialog(null, CommonLocaleDelegate.getMessage("ExplorerController.19","Fehler beim entfernen der Beziehungen"), ex);
									}
								}
							});
						}
					}
				};

				result.add(actRemoveRelation);
			}
		}

		return result;
	}

	/**
	 * @param collExplorerNodes
	 * @return the module id shared by all explorer nodes, if any.
	 */
	private static Integer getCommonModuleId(Collection<GenericObjectExplorerNode> collExplorerNodes) {
		return Utils.getCommonObject(CollectionUtils.transform(collExplorerNodes, new Transformer<GenericObjectExplorerNode, Integer>() {
			@Override
            public Integer transform(GenericObjectExplorerNode explorernode) {
				return explorernode.getTreeNode().getModuleId();
			}
		}));
	}

	/**
	 * @param collExplorerNodes
	 * @return
	 * @precondition !CollectionUtils.isNullOrEmpty(collExplorerNodes)
	 */
	private static CollectableSearchCondition getCollectableSearchCondition(Collection<ExplorerNode<?>> collExplorerNodes) {
		final Collection<Object> collIds = CollectionUtils.transform(collExplorerNodes, new Transformer<ExplorerNode<?>, Object>() {
			@Override
            public Object transform(ExplorerNode<?> explorernode) {
				return explorernode.getTreeNode().getId();
			}
		});

		return SearchConditionUtils.getCollectableSearchConditionForIds(collIds);
	}
}
