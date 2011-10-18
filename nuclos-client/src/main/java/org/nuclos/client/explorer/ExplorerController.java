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

import java.awt.Component;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.nuclos.client.explorer.node.AbstractDatasourceExplorerNode;
import org.nuclos.client.explorer.node.AbstractRuleExplorerNode;
import org.nuclos.client.explorer.node.datasource.DirectoryDatasourceNode;
import org.nuclos.client.explorer.node.rule.DirectoryRuleNode;
import org.nuclos.client.explorer.node.rule.RuleTreeModel;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.main.mainframe.workspace.ITabStoreController;
import org.nuclos.client.main.mainframe.workspace.TabRestoreController;
import org.nuclos.client.masterdata.MasterDataLayoutHelper;
import org.nuclos.client.task.TaskController;
import org.nuclos.client.ui.CommonClientWorkerAdapter;
import org.nuclos.client.ui.CommonMultiThreader;
import org.nuclos.client.ui.Controller;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.MainFrameTabAdapter;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.ClientPreferences;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.navigation.treenode.TreeNode;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Controller for explorer panel.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class ExplorerController extends Controller {
	private final Logger log = Logger.getLogger(this.getClass());

	@Deprecated
	public
	static final String PREFS_NODE_EXPLORERVIEWS = "explorerViews";
	@Deprecated
	public
	static final String PREFS_NODE_EXPLORERVIEWS_XML = "explorerViewsXML";
	@Deprecated
	public
	static final String PREFS_NODE_EXPLORER_EXPANDEDPATHS = "explorerViewsExapndedPaths";

	//private final ExplorerPanel pnlExplorer;

	private TaskController ctlTasks;

	public final Preferences prefs = ClientPreferences.getUserPreferences().node("explorer");

	private Thread workerThread;

	private final Map<ExplorerView, MainFrameTab> explorerTabs = new HashMap<ExplorerView, MainFrameTab>();

	/**
	 * @param parent the general parent (for displaying error messages etc.)
	 */
	public ExplorerController(Component parent) {
		super(parent);
	}

	/**
	 * @return the <code>TaskController</code>
	 */
	public TaskController getTaskController() {
		assert this.ctlTasks != null;
		return this.ctlTasks;
	}

	/**
	 * @param treenodeRoot
	 * @return Does the tabbed pane contain a view with the given treenode as root?
	 */
	public boolean containsTreeNode(TreeNode treenodeRoot) {
		return (getExplorerViewFor(treenodeRoot) != null);
	}

	public ExplorerView getExplorerViewFor(MainFrameTab tab) {
		ExplorerView result = null;
		for (ExplorerView view : explorerTabs.keySet()) {
			if (explorerTabs.get(view) == tab) {
				result = view;
				break;
			}
		}
		return result;
	}

	public ExplorerView getExplorerViewFor(TreeNode treenodeRoot) {
		ExplorerView result = null;
		for (ExplorerView view : explorerTabs.keySet()) {
			final ExplorerNode<?> explorernodeRoot = view.getRootNode();
			if (treenodeRoot.equals(explorernodeRoot.getTreeNode())) {
				result = view;
				break;
			}
		}
		return result;
	}

	public MainFrameTab getTabOfExplorerViewFor(TreeNode treenodeRoot) {
		MainFrameTab result = null;
		ExplorerView view = getExplorerViewFor(treenodeRoot);
		if (view != null) {
			result = getTabFor(view);
		}
		return result;
	}

	public MainFrameTab getTabFor(ExplorerView view) {
		MainFrameTab result = explorerTabs.get(view);
		if (result == null) {
			throw new NuclosFatalException("No tab for ExplorerView found");
		}
		return result;
	}

	private void setupMainFrameTab(MainFrameTab tab, final TreeNode treenodeRoot, final ExplorerView view, String sLabel) {
		tab.addMainFrameTabListener(new MainFrameTabAdapter() {
			@Override
			public boolean tabClosing(MainFrameTab tab) {
				removeExplorerView(view);
				return true;
			}
			@Override
			public void tabClosed(MainFrameTab tab) {
				tab.removeMainFrameTabListener(this);
			}
		});

		tab.setTabIcon(Icons.getInstance().getIconTree16());
		tab.setTitle(sLabel);
		tab.setLayeredComponent(view.getViewComponent());
		tab.setTabStoreController(new ExplorerTabStoreController(treenodeRoot, view));
	}

	private static class RestorePreferences implements Serializable {
		private static final long serialVersionUID = 6637996725938917463L;

		TreeNode treenodeRoot;
		List<String> lstExpandedPaths;
	}

	private static String toXML(RestorePreferences rp) {
		XStream xstream = new XStream(new DomDriver());
		return xstream.toXML(rp);
	}

	private static RestorePreferences fromXML(String xml) {
		XStream xstream = new XStream(new DomDriver());
		return (RestorePreferences) xstream.fromXML(xml);
	}

	/**
	 *
	 *
	 */
	public static class ExplorerTabStoreController implements ITabStoreController {
		final TreeNode treenodeRoot;
		final ExplorerView view;

		public ExplorerTabStoreController(TreeNode treenodeRoot, ExplorerView view) {
			super();
			this.treenodeRoot = treenodeRoot;
			this.view = view;
		}

		@Override
		public Class<?> getTabRestoreControllerClass() {
			return ExplorerController.ExplorerTabRestoreController.class;
		}

		@Override
		public String getPreferencesXML() {
			ExplorerNode<?> explorernodeRoot = (ExplorerNode<?>) view.getJTree().getModel().getRoot();
			List<String> lstExpandedPathsResult = new ArrayList<String>();

			ExplorerNode.createExpandendPathsForTree(new TreePath( explorernodeRoot), view.getJTree(), lstExpandedPathsResult);

			RestorePreferences rp = new RestorePreferences();
			rp.treenodeRoot = treenodeRoot;
			rp.lstExpandedPaths = lstExpandedPathsResult;

			return toXML(rp);
		}
	}

	/**
	 *
	 *
	 */
	public static class ExplorerTabRestoreController extends TabRestoreController {

		@Override
		public void restoreFromPreferences(String preferencesXML, final MainFrameTab tab) throws Exception {
			RestorePreferences rp = fromXML(preferencesXML);

			if (rp.treenodeRoot.getId() == null && rp.treenodeRoot.getIdentifier() == null && rp.treenodeRoot.getSubNodes().isEmpty()) {
				// nullNode stored in preferences
				return;
			}

			ExplorerView view = Main.getMainController().getExplorerController().addOrReplaceExplorerViewFor(tab, rp.treenodeRoot, false, false, true);
			ExplorerNode.expandTreeAsync(rp.lstExpandedPaths, view.getJTree());
		}

	}

	private void removeExplorerView(ExplorerView view) {
		explorerTabs.remove(view);
	}

	public void closeExplorerView(ExplorerView view) {
		final MainFrameTab tab = getTabFor(view);
		try {
			MainFrame.closeTab(tab);
			explorerTabs.remove(view);
		} catch(CommonBusinessException e) {
			Errors.getInstance().showExceptionDialog(view.getViewComponent(), e);
		}
	}

	/**
	 * adds a tab for the given treenode, if it is not contained already.
	 * Otherwise, replaces the view containing the treenode, if it is not identical to the given treenode.
	 * @param treenodeRoot
	 */
	public ExplorerView addOrReplaceExplorerViewFor(final TreeNode treenodeRoot, boolean withSearchWorker) {
		return this.addOrReplaceExplorerViewFor(null, treenodeRoot, withSearchWorker, true, false);
	}

	private ExplorerView addOrReplaceExplorerViewFor(MainFrameTab newTab, final TreeNode treenodeRoot, boolean withSearchWorker, boolean selectTab, boolean onlyAdd) {
		ExplorerView view = this.getExplorerViewFor(treenodeRoot);
		final String sLabel = treenodeRoot.getLabel();
		final String sLabelWorking = sLabel + " ("+ CommonLocaleDelegate.getMessage("ExplorerController.20","In Bearbeitung") + ")";
		final boolean addToTreeHome = newTab==null;
		if (newTab == null) {
			newTab = new MainFrameTab();
		}

		if (view == null) {
			view = ExplorerViewFactory.getInstance().newExplorerView(treenodeRoot);

			setupMainFrameTab(newTab, treenodeRoot, view, sLabelWorking);
			explorerTabs.put(view, newTab);
			if (addToTreeHome) {
				MainFrame.addTabToTreeHome(newTab);
			}
		} else {
			if (onlyAdd) {
				throw new NuclosFatalException("Only add explorer view, but view already exists: " + treenodeRoot.toString());
			}
		}

		final MainFrameTab tab = getTabFor(view);
		tab.setTitle(sLabelWorking);

		//this.pnlExplorer.getTabbedPane().setSelectedIndex((selectedTabIndex >= 0 && iTab>=selectedTabIndex)? selectedTabIndex :iTab);

		// Workaraound...
		//final int iFinalTab = iTab;

		final ExplorerView viewForWorker = view;

		if(withSearchWorker) {
			CommonClientWorkerAdapter<Collectable> searchWorker = new CommonClientWorkerAdapter<Collectable>(null) {
				@Override
				public void init() {
					UIUtils.setWaitCursor(viewForWorker.getViewComponent());
				}

				@Override
				public void work() {
					UIUtils.invokeOnDispatchThread(new Runnable() {
						@Override
						public void run() {
							expandAllLoadedNodes(viewForWorker.getJTree(), new TreePath(viewForWorker.getRootNode()));
						}
					});
				}

				@Override
				public void paint() {
					viewForWorker.getViewComponent().setCursor(null);
					try {
						tab.setTitle(sLabel);
					}
					catch(Exception ex) {
						// Do nothing; user may have closed the tab pane or similar
					}
				}

				@Override
				public void handleError(Exception ex) {
					log.error(ex);

					if (ex instanceof ExplorerNodeRefreshException) {
						String sMessage = ex.getMessage() + "\n\n" + CommonLocaleDelegate.getMessage("ExplorerController.13","Der Reiter des Knotens wird automatisch geschlossen.");
						JOptionPane.showMessageDialog(null, sMessage, CommonLocaleDelegate.getMessage("ExplorerController.30","Wiederherstellen der Baumansicht"), JOptionPane.ERROR_MESSAGE);
						closeExplorerView(viewForWorker);
					}
					else {
						Errors.getInstance().showExceptionDialog(null, CommonLocaleDelegate.getMessage("ExplorerController.17","Fehler beim Anzeigen des Explorerfensters"), ex);
					}
				}
			};

			workerThread = CommonMultiThreader.getInstance().execute(searchWorker);
		}
		else {
			try {
				UIUtils.setWaitCursor(view.getViewComponent());
				expandAllLoadedNodes(viewForWorker.getJTree(), new TreePath(viewForWorker.getRootNode()));
				view.getViewComponent().setCursor(null);

				tab.setTitle(sLabel);
			}
			catch(Exception ex) {
				log.error(ex);

				if (ex instanceof ExplorerNodeRefreshException) {
					String sMessage = ex.getMessage() + "\n\n" + CommonLocaleDelegate.getMessage("ExplorerController.13","Der Reiter des Knotens wird automatisch geschlossen.");
					JOptionPane.showMessageDialog(null, sMessage, CommonLocaleDelegate.getMessage("ExplorerController.30","Wiederherstellen der Baumansicht"), JOptionPane.ERROR_MESSAGE);
					closeExplorerView(viewForWorker);
				}
				else {
					Errors.getInstance().showExceptionDialog(null, CommonLocaleDelegate.getMessage("ExplorerController.17","Fehler beim Anzeigen des Explorerfensters"), ex);
				}
			}
		}

		if (selectTab) {
			MainFrame.setSelectedTab(tab);
		}

		return view;
	}

	private static String getLabelForRoot(final JTree tree) {
		return ((ExplorerNode<?>) tree.getModel().getRoot()).getLabelForRoot();
	}

	/**
	 * refreshes the tab label, when refresh has been chosen from a context menu of a node
	 * @param treenodeRoot
	 */
	public void refreshNode(TreeNode treenodeRoot) {
		final ExplorerView view = getExplorerViewFor(treenodeRoot);
		if (view != null) {
			refreshTabLabel(view);
		}
	}

	/**
	 * refreshes the tab
	 * @param view
	 */
	public void refreshTab(ExplorerView view) throws CommonFinderException {
		view.getRootNode().refresh(view.getJTree());

		refreshTabLabel(view);
	}

	/**
	 * refreshes the label of the tab with the given view
	 * @param view
	 */
	private void refreshTabLabel(ExplorerView view) {
		final String sString = getLabelForRoot(view.getJTree());
		getTabFor(view).setTitle(sString);
	}

	/**
	 * command: show the given node in a separate tab in the explorer,
	 * reusing an existing tab for this node, if any.
	 * @param node an existing TreeNode to be shown in its own tab
	 */
	public void cmdShowInOwnTab(final TreeNode node) {
		UIUtils.runCommand(this.getParent(), new Runnable() {
			@Override
            public void run() {
				showInOwnTab(node);
			}
		});
	}

	/**
	 * shows the given node in a separate tab in the explorer,
	 * reusing an existing tab for this node, if any.
	 * @param treenode an existing TreeNode to be shown in its own tab
	 */
	public void showInOwnTab(TreeNode treenode) {
		this.addOrReplaceExplorerViewFor(treenode, true);
	}

	/**
	 * shows the filters in its own tab in the explorer.
	 */
	public void cmdShowPersonalSearchFilters() {
		UIUtils.runCommand(this.getParent(), new Runnable() {
			@Override
            public void run() {
				ExplorerController.this.addOrReplaceExplorerViewFor(new PersonalSearchFiltersTreeNode(), true);
			}
		});
	}

	/**
	 * shows the "rule usage" in its own tab in the explorer.
	 * @param ruleIdToGoto	expand tree to the given ruleId, if the id s not null
	 */
	public void cmdShowRuleUsage(final Integer ruleIdToGoto, final String sRuleLabel) {
		UIUtils.runCommand(this.getParent(), new CommonRunnable() {
			@Override
            public void run() throws CommonFinderException {
				final DirectoryRuleNode treenodeRoot = new DirectoryRuleNode(true, CommonLocaleDelegate.getMessage("ExplorerController.24","Regelverwendungen"), CommonLocaleDelegate.getMessage("ExplorerController.25","Regelverwendungen"), null, false);
				final ExplorerView view = ExplorerController.this.addOrReplaceExplorerViewFor(treenodeRoot, true);

				if (ruleIdToGoto != null) {
					final Runnable inAWT = new Runnable() {
						@Override
						public void run() {
							final AbstractRuleExplorerNode ruleExplorerNode = (AbstractRuleExplorerNode) view.getRootNode();
							try {
								if(sRuleLabel.equals(RuleTreeModel.ALL_RULES_NODE_LABEL))
									ruleExplorerNode.expandToRuleWithId(ruleIdToGoto, view.getJTree());
								else if(sRuleLabel.equals(RuleTreeModel.FRIST_NODE_LABEL))
									ruleExplorerNode.expandToTimelimitRuleWithId(ruleIdToGoto, view.getJTree());
								else if(sRuleLabel.equals(RuleTreeModel.LIBRARY_LABEL))
									ruleExplorerNode.expandToLibraryRuleWithId(ruleIdToGoto, view.getJTree());
							}
							catch(CommonFinderException e) {
								Errors.getInstance().showExceptionDialog(getParent(), e);
							}
						}
					};

					runInAwtAfterWorkerThreadHasFinished(inAWT);
				}


			}
		});
	}

	/**
	 * shows the "rule usage" in its own tab in the explorer.
	 */
	public void cmdShowDatasources(final Integer datasourceIdToGoto) {
		UIUtils.runCommand(this.getParent(), new CommonRunnable() {
			@Override
            public void run() throws CommonFinderException {
				final DirectoryDatasourceNode treenodeRoot = new DirectoryDatasourceNode(true, CommonLocaleDelegate.getMessage("ExplorerController.10","Datenquellen"), CommonLocaleDelegate.getMessage("ExplorerController.11","Datenquellen"), null);
				final ExplorerView view = ExplorerController.this.addOrReplaceExplorerViewFor(treenodeRoot, true);

				if (datasourceIdToGoto != null) {
					final Runnable inAWT = new Runnable() {
						@Override
						public void run() {
							try {
								final AbstractDatasourceExplorerNode datasourceExplorerNode = (AbstractDatasourceExplorerNode) view.getRootNode();
								datasourceExplorerNode.expandToDatasourceWithId(datasourceIdToGoto, view.getJTree());
							}
							catch(CommonFinderException e) {
								Errors.getInstance().showExceptionDialog(getParent(), e);
							}
						}
					};

					runInAwtAfterWorkerThreadHasFinished(inAWT);
				}
			}
		});
	}


	private final void runInAwtAfterWorkerThreadHasFinished(final Runnable inAWT) {
		final Thread tWorkerThread = ExplorerController.this.workerThread;
		ExplorerController.this.workerThread = null;
		if(tWorkerThread != null) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						tWorkerThread.join();
					}
					catch (InterruptedException e) {
						log.error(e);
					}
					SwingUtilities.invokeLater(inAWT);
				}
			}, "Wait for worker").start();
		}
		else
			inAWT.run();
	}

	public static void expandAllLoadedNodes(JTree tree, TreePath path) {
		final ExplorerNode<?> explorernode = (ExplorerNode<?>) path.getLastPathComponent();
		if (Boolean.TRUE.equals(explorernode.getTreeNode().hasSubNodes())) {
			tree.expandPath(path);

			for (int i = 0; i < explorernode.getChildCount(); i++) {
				final ExplorerNode<?> explorernodeChild = (ExplorerNode<?>) explorernode.getChildAt(i);
				expandAllLoadedNodes(tree, path.pathByAddingChild(explorernodeChild));
			}
		}
	}

	/**
	 * creates a new tree from a <code>TreeNode</code>
	 * @param node
	 * @return the new tree
	 */
	public static ExplorerNode<?> newExplorerTree(final TreeNode node) {
		final ExplorerNodeFactory explorernodefactory = ExplorerNodeFactory.getInstance();
		final ExplorerNode<?> result = explorernodefactory.newExplorerNode(node, true);

		// show the children of the root (one level, non-recursive):
		for (TreeNode treenodeChild : node.getSubNodes()) {
			result.add(explorernodefactory.newExplorerNode(treenodeChild, false));
		}
		return result;
	}

	public static List<ExplorerNode<?>> getSelectedExplorerNodes(JTree tree) {
		return CollectionUtils.transform(Arrays.asList(tree.getSelectionPaths()), new Transformer<TreePath, ExplorerNode<?>>() {
			@Override
            public ExplorerNode<?> transform(TreePath treepath) {
				return (ExplorerNode<?>) treepath.getLastPathComponent();
			}
		});
	}

	public void setTaskController(TaskController ctlTasks) {
		this.ctlTasks = ctlTasks;
	}

	/**
	 *
	 * @param tab
	 * @return
	 */
	public boolean isExplorerTab(MainFrameTab tab) {
		if (explorerTabs.values().contains(tab))
			return true;

		if (tab.getContent() instanceof ExplorerView)
			return true;

		return false;
	}

	public boolean isDisplayable(String entityname) {
		return MasterDataLayoutHelper.isLayoutMLAvailable(entityname, false);
	}
}	// class ExplorerController
