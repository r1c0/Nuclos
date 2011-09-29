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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.common.NuclosCollectControllerFactory;
import org.nuclos.client.common.Utils;
import org.nuclos.client.explorer.node.AbstractDatasourceExplorerNode;
import org.nuclos.client.explorer.node.AbstractRuleExplorerNode;
import org.nuclos.client.explorer.node.GenericObjectExplorerNode;
import org.nuclos.client.explorer.node.GroupExplorerNode;
import org.nuclos.client.explorer.node.datasource.DirectoryDatasourceNode;
import org.nuclos.client.explorer.node.rule.DirectoryRuleNode;
import org.nuclos.client.explorer.node.rule.RuleTreeModel;
import org.nuclos.client.genericobject.GenericObjectCollectController;
import org.nuclos.client.genericobject.GenericObjectDelegate;
import org.nuclos.client.genericobject.datatransfer.GenericObjectIdModuleProcess;
import org.nuclos.client.genericobject.datatransfer.TransferableGenericObjects;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.main.mainframe.workspace.ITabStoreController;
import org.nuclos.client.main.mainframe.workspace.TabRestoreController;
import org.nuclos.client.masterdata.MasterDataLayoutHelper;
import org.nuclos.client.task.TaskController;
import org.nuclos.client.ui.CommonAbstractAction;
import org.nuclos.client.ui.CommonClientWorkerAdapter;
import org.nuclos.client.ui.CommonMultiThreader;
import org.nuclos.client.ui.Controller;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.Icons.CompositeIcon;
import org.nuclos.client.ui.MainFrameTabAdapter;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.tree.TreeNodeAction;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.NuclosImage;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableFieldFormat;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.SearchConditionUtils;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.ClientPreferences;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.genericobject.searchcondition.CollectableGenericObjectSearchExpression;
import org.nuclos.server.navigation.treenode.AbstractTreeNode;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode;
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

	private final TransferHandler transferhandler;

	public final Preferences prefs = ClientPreferences.getUserPreferences().node("explorer");

	private Thread workerThread;

	private final Map<ExplorerView, MainFrameTab> explorerTabs = new HashMap<ExplorerView, MainFrameTab>();

	private final static AbstractTreeNode<Object> nullNode = new AbstractTreeNode<Object>(null, "(" + CommonLocaleDelegate.getMessage("ExplorerController.20","In Bearbeitung") + ")", CommonLocaleDelegate.getMessage("ExplorerController.21","Inhalt wird erstellt")) {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		protected List<? extends TreeNode> getSubNodesImpl() {
			return Collections.emptyList();
		}

		@Override
        public TreeNode refreshed() {
			return this;
		}

		@Override
		public String getIdentifier() {
			return null;
		}
	};

	/**
	 * @param parent the general parent (for displaying error messages etc.)
	 */
	public ExplorerController(Component parent) {
		super(parent);

		this.transferhandler = new TransferHandler(Main.getMainFrame());
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
		tab.setLayeredComponent(view);
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
			Errors.getInstance().showExceptionDialog(view, e);
		}
	}

	private List<JComponent> getToolbarComponents(final ExplorerView view) {
		List<JComponent> result = new ArrayList<JComponent>();

		result.add(new JButton(new AbstractAction("", Icons.getInstance().getIconRefresh16()) {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				cmdRefreshTab(view);
			}
		}));

		return result;
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
			view = new ExplorerView(new JTree(new ExplorerNode<TreeNode>(nullNode)));
			view.setToolBarComponents(getToolbarComponents(view));

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
					UIUtils.setWaitCursor(viewForWorker);
				}

				@Override
				public void work() {
					if (treenodeRoot != viewForWorker.getRootNode().getTreeNode()) {
						UIUtils.invokeOnDispatchThread(new Runnable() {
							@Override
							public void run() {
								viewForWorker.setJTree(newJTree(treenodeRoot));
							}
						});
					}
				}

				@Override
				public void paint() {
					addAdditionalToolBarComponents(viewForWorker);
					viewForWorker.setCursor(null);
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
				UIUtils.setWaitCursor(view);
				if (treenodeRoot != view.getRootNode().getTreeNode()) {
					view.setJTree(newJTree(treenodeRoot));
				}
				addAdditionalToolBarComponents(view);
				view.setCursor(null);

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

		if (selectTab)
			MainFrame.setSelectedTab(tab);

		return view;
	}

	private static void addAdditionalToolBarComponents(final ExplorerView view) {
		List<JComponent> toolBarComponents = view.getRootNode().getToolBarComponents(view.getJTree());
		if (toolBarComponents != null) {
			view.addAdditionalToolBarComponents(toolBarComponents);
		}
	}

	private static String getLabelForRoot(final JTree tree) {
		return ((ExplorerNode<?>) tree.getModel().getRoot()).getLabelForRoot();
	}

	/**
	 * refreshes the tab
	 * @param view
	 */
	private void cmdRefreshTab(final ExplorerView view) {
		UIUtils.runCommand(this.getParent(), new CommonRunnable() {
			@Override
            public void run() throws CommonFinderException {
				refreshTab(view);
			}
		});
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
	private void refreshTab(ExplorerView view) throws CommonFinderException {
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

	/**
	 * @return List<TreeNode>. The root tree nodes of all ExplorerViews.
	 */
	private List<TreeNode> getRootTreeNodes() {
		final List<TreeNode> result = new LinkedList<TreeNode>();

		for (final ExplorerView view : explorerTabs.keySet()) {
			final ExplorerNode<?> explorernodeRoot = (ExplorerNode<?>) view.getJTree().getModel().getRoot();
			final TreeNode treenodeRoot = explorernodeRoot.getTreeNode();
			result.add(treenodeRoot);
		}
		return result;
	}

	private Map<ExplorerNode<?>, JTree> getRootTreeNodesWithJTree() {
		Map<ExplorerNode<?>, JTree> result = new HashMap<ExplorerNode<?>, JTree>();

		for (final ExplorerView view : explorerTabs.keySet()) {
			final ExplorerNode<?> explorernodeRoot = (ExplorerNode<?>) view.getJTree().getModel().getRoot();

			result.put(explorernodeRoot, view.getJTree());
		}
		return result;
	}

	/**
	 * creates a new JTree containing the given treenode as root
	 * @param treenodeRoot
	 * @return the newly created JTree
	 */
	private JTree newJTree(TreeNode treenodeRoot) {
		final ExplorerNode<?> explorernodeRoot = this.newExplorerTree(treenodeRoot);

		final JTree result = new JTree(explorernodeRoot, true);

		result.putClientProperty("JTree.lineStyle", "Angled");
		result.setBackground(Color.WHITE);
		result.setRootVisible(true);
		result.setShowsRootHandles(true);

		// enable drag (as in "drag&drop"):
		this.setupDataTransfer(result);

		// don't expand on double click:
		result.setToggleClickCount(0);

		this.addMouseListenerTo(result);
		this.addKeyListenerTo(result);

		// enable tool tips:
		ToolTipManager.sharedInstance().registerComponent(result);

		/**
		 * inner class Renderer. Shows a tooltip for each node.
		 * Note that the label and thus the tooltip is reused for each node (it's amazing ;-)).
		 * @todo OPTIMIZE: We probably don't need a custom renderer for the tooltip (seems to be quite expensive). And for the icon?
		 */
		class Renderer extends DefaultTreeCellRenderer {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;
			class PaintImage {
				int x;
				NuclosImage image;
				
				public PaintImage(int x, NuclosImage image) {
					this.x = x;
					this.image = image;
				}
			}

			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object oValue, boolean bSelected, boolean bExpanded,
					boolean bLeaf, int iRow, boolean bHasFocus) {

				final JLabel lbl = (JLabel) super.getTreeCellRendererComponent(tree, oValue, bSelected, bExpanded, bLeaf, iRow,
						bHasFocus);
				
				String tmp = lbl.getText();

				final List<PaintImage> images = new LinkedList<PaintImage>();
					
				int idx = -1;
				int spaceX = SwingUtilities.computeStringWidth(lbl.getFontMetrics(lbl.getFont()), " ");
				
				while ((idx = tmp.indexOf("[$" + CollectableFieldFormat.class.getName() + ",")) != -1)
				{						
					int formatEnd = tmp.indexOf("$]");
					String format = tmp.substring(idx, formatEnd);
					
					String[] formatDef = format.split(",");
					try {
						CollectableFieldFormat clctformat = CollectableFieldFormat.getInstance(Class.forName(formatDef[1]));

						int x = SwingUtilities.computeStringWidth(lbl.getFontMetrics(lbl.getFont()), tmp.substring(0, idx));
						
						NuclosImage img = (NuclosImage) clctformat.parse(null, formatDef[3]);
						
						String tmp1 = tmp.substring(0, idx);
						for (int i = 0; i < img.getWidth() / spaceX; i++) {
							tmp1 += " ";
						}
						tmp1 += tmp.substring(formatEnd + 2);
						tmp = tmp1;
				
						images.add(new PaintImage(x, img));
					} catch (CollectableFieldFormatException e) {
						log.error("format exception at " + formatDef[1], e);
					} catch (ClassNotFoundException e) {
						log.error("class not found for " + formatDef[1], e);
					}
				}
				
				final ExplorerNode<?> explorernode = (ExplorerNode<?>) oValue;
				
				DefaultTreeCellRenderer lbComp = new DefaultTreeCellRenderer() {

					public void paintComponent(java.awt.Graphics g) {
						super.paintComponent(g);		
						
						Graphics2D g2d = (Graphics2D)g;
						for (PaintImage paintImage : images) {
							g2d.drawImage(new ImageIcon(paintImage.image.getContent()).getImage(), getIcon().getIconWidth()+ getIconTextGap() + paintImage.x, 1, null);
						}
					};
				};

				JComponent result = (JComponent)lbComp.getTreeCellRendererComponent(tree, oValue, bSelected, bExpanded, bLeaf, iRow,
						bHasFocus);

				lbComp.setText(tmp);
				
				// set tooltip text:
				final String sDescription = StringUtils.nullIfEmpty(explorernode.getTreeNode().getDescription());
				lbComp.setToolTipText(sDescription);

				// set icon:
				final Icon icon = explorernode.getIcon();
				//if (icon != null) {
					lbComp.setIcon(icon);
				//}
					
				if (explorernode instanceof GenericObjectExplorerNode) {
					final Icon iconRelation = ((GenericObjectExplorerNode) explorernode).getRelationIcon();
					if (iconRelation != null) {
						lbComp.setIcon(new CompositeIcon(iconRelation,icon));

						//result = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 0));
						//result.add(new JLabel(iconRelation));
						//result.add(lbl);
						//result.setOpaque(false);
					}
				}

				return result;
			}
		}	// inner class Renderer

		UIUtils.invokeOnDispatchThread(new Runnable() {
			@Override
			public void run() {
				result.setCellRenderer(new Renderer());
			}
		});

		// dynamic loading on node expansion:
		result.addTreeWillExpandListener(new TreeWillExpandListener() {
			@Override
            public void treeWillExpand(final TreeExpansionEvent ev) {
				UIUtils.setWaitCursor(ExplorerController.this.getParent());
				UIUtils.runCommand(ExplorerController.this.getParent(), new Runnable() {
					@Override
                    public void run() {
						final ExplorerNode<?> explorernode = (ExplorerNode<?>) ev.getPath().getLastPathComponent();
						//NUCLEUSINT-1129
						//final TreeNode treenode = explorernode.getTreeNode();
						final boolean bSubNodesHaventBeenLoaded = !(explorernode.getChildCount() > 0); //(treenode.hasSubNodes() == null);

						if (bSubNodesHaventBeenLoaded) {
							//explorernode.unloadChildren();
							explorernode.loadChildren(true);

							for (int i = 0; i < explorernode.getChildCount(); i++) {
								final ExplorerNode<?> explorernodeChild = (ExplorerNode<?>) explorernode.getChildAt(i);
								expandAllLoadedNodes(result, ev.getPath().pathByAddingChild(explorernodeChild));
							}
						}
					}
				});
				ExplorerController.this.getParent().setCursor(null);
			}

			@Override
            public void treeWillCollapse(TreeExpansionEvent ev) {
				// do nothing
			}
		});

		expandAllLoadedNodes(result, new TreePath(explorernodeRoot));

		return result;
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
	 * enables drag&drop, copy&paste
	 * @param tree
	 */
	private void setupDataTransfer(final JTree tree) {
		tree.setDragEnabled(true);
		tree.setTransferHandler(this.transferhandler);
	}

	/**
	 * creates a new tree from a <code>TreeNode</code>
	 * @param node
	 * @return the new tree
	 */
	private ExplorerNode<?> newExplorerTree(final TreeNode node) {

		final ExplorerNodeFactory explorernodefactory = ExplorerNodeFactory.getInstance();

		final ExplorerNode<?> result = explorernodefactory.newExplorerNode(node, true);

		// show the children of the root (one level, non-recursive):
		for (TreeNode treenodeChild : node.getSubNodes()) {
			result.add(explorernodefactory.newExplorerNode(treenodeChild, false));
		}
		return result;
	}

	private void addKeyListenerTo(final JTree tree) {
		tree.addKeyListener(new KeyAdapter() {

			@Override
            public void keyTyped(KeyEvent ev) {
				if (tree.getSelectionRows().length > 0) {
					final TreePath treepath = tree.getSelectionPath();
					ExplorerController.this.keyEventOnNode(treepath, ev);
				}
            }

		});
	}

	private void keyEventOnNode(TreePath pathEvent, KeyEvent ev) {
		final ExplorerNode<? extends TreeNode> node = (ExplorerNode<?>) pathEvent.getLastPathComponent();
		final JTree tree = (JTree) ev.getComponent();

		final TreePath[] aSelectionPaths = tree.getSelectionPaths();
		if (aSelectionPaths == null || !Arrays.asList(aSelectionPaths).contains(pathEvent)) {
			// select it (and unselect all others):
			tree.setSelectionPath(pathEvent);
		}

		assert tree.getSelectionCount() >= 1;
		if (tree.getSelectionCount() == 1) {
			node.handleKeyEvent(tree, ev);
		} else {
			// TODO "Sammelbearbeitung"
			/*for (ExplorerNode<?> exNode : getSelectedExplorerNodes(tree)) {
				exNode.handleKeyEvent(tree, ev);
			}*/
		}
	}

	/**
	 * adds a mouselistener to the given <code>tree</code>,
	 * in order to enable popup menus and double click.
	 * @param tree
	 */
	private void addMouseListenerTo(final JTree tree) {
		tree.addMouseListener(new MouseAdapter() {
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
					ExplorerController.this.mouseEventOnNode(treepath, ev);
				}
			}
		});
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
					this.newPopupMenuForMultipleNodes(getSelectedExplorerNodes(tree), tree);

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
				log.warn("exploreraction == null");
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
				/**
				 *
				 */
				private static final long serialVersionUID = 1L;

				@Override
                public void actionPerformed(ActionEvent ev) {
					UIUtils.runCommand(getParent(), new CommonRunnable() {
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
					/**
					 *
					 */
					private static final long serialVersionUID = 1L;

					@Override
                    public void actionPerformed(ActionEvent ev) {

						final String sMessage = CommonLocaleDelegate.getMessage("ExplorerController.28","Sollen die Beziehungen von den ausgew\u00e4hlten Objekten zu dem \u00fcbergeordneten Object entfernt werden")+ "?";

						final int iBtn = JOptionPane.showConfirmDialog(tree, sMessage, CommonLocaleDelegate.getMessage("ExplorerController.6","Beziehung entfernen"), JOptionPane.OK_CANCEL_OPTION);
						if (iBtn == JOptionPane.OK_OPTION) {
							CommonClientWorkerAdapter<Collectable> removeWorker = new CommonClientWorkerAdapter<Collectable>(null) {
								@Override
								public void init() {
									UIUtils.setWaitCursor(tree);
								}

								@SuppressWarnings("unchecked")
								@Override
								public void work() throws CommonBusinessException {
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
								}

								@Override
								public void paint() {
									tree.setCursor(null);
								}

								@Override
								public void handleError(Exception ex) {
									log.error(ex);
									Errors.getInstance().showExceptionDialog(null, CommonLocaleDelegate.getMessage("ExplorerController.18","Fehler beim entfernen der Beziehungen"), ex);
								}
							};

							workerThread = CommonMultiThreader.getInstance().execute(removeWorker);
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
					/**
					 *
					 */
					private static final long serialVersionUID = 1L;

					@Override
                    public void actionPerformed(ActionEvent ev) {

						final String sMessage = CommonLocaleDelegate.getMessage("ExplorerController.29","Sollen die Beziehungen von den ausgew\u00e4hlten Objekten zur Objektgruppe entfernt werden") + "?";

						final int iBtn = JOptionPane.showConfirmDialog(tree, sMessage, CommonLocaleDelegate.getMessage("ExplorerController.7","Beziehung entfernen"), JOptionPane.OK_CANCEL_OPTION);
						if (iBtn == JOptionPane.OK_OPTION) {
							CommonClientWorkerAdapter<Collectable> removeWorker = new CommonClientWorkerAdapter<Collectable>(null) {
								@Override
								public void init() {
									UIUtils.setWaitCursor(tree);
								}

								@Override
								public void work() throws CommonBusinessException {
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
								}

								@Override
								public void paint() {
									tree.setCursor(null);
								}

								@Override
								public void handleError(Exception ex) {
									log.error(ex);
									Errors.getInstance().showExceptionDialog(null, CommonLocaleDelegate.getMessage("ExplorerController.19","Fehler beim entfernen der Beziehungen"), ex);
								}
							};

							workerThread = CommonMultiThreader.getInstance().execute(removeWorker);
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

	private static List<ExplorerNode<?>> getSelectedExplorerNodes(JTree tree) {
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
	 * inner class TransferHandler. Handles drag&drop, copy&paste for the explorer trees.
	 */
	private class TransferHandler extends javax.swing.TransferHandler {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private final Component parent;
		private boolean result = false;

		TransferHandler(Component parent) {
			this.parent = parent;
		}

		@Override
		public int getSourceActions(JComponent comp) {
			int result = NONE;
			if (comp instanceof JTree) {
				final JTree tree = (JTree) comp;
				ExplorerNode<?> explorerNode = this.getSelectedTreeNode(tree);
				result = (explorerNode == null) ? NONE : explorerNode.getDataTransferSourceActions();
			}
			return result;
		}

		@Override
		protected Transferable createTransferable(JComponent comp) {
			Transferable result = null;
			if (comp instanceof JTree) {
//				final JTree tree = (JTree) comp;
//				ExplorerNode explorerNode = this.getSelectedTreeNode(tree);
//				result = (explorerNode == null) ? null : explorerNode.createTransferable();
//			}
			//Task TA0903#0158
				final JTree tree = (JTree)comp;

				List<ExplorerNode<?>> lsExplorerNodes = getSelectedExplorerNodes(tree);
				int iSelectionCount = lsExplorerNodes.size();

				if (iSelectionCount == 1) {
					result = this.getSelectedTreeNode(tree).createTransferable(tree);
				}
				else if (iSelectionCount > 1) {
					// note: this works only for genericobject not for masterdata explorer nodes
					final List<GenericObjectIdModuleProcess> lstloimp = new ArrayList<GenericObjectIdModuleProcess>(iSelectionCount);
					for (ExplorerNode<?> explorerNode : lsExplorerNodes) {
						if (explorerNode instanceof GenericObjectExplorerNode) {
							final GenericObjectTreeNode lotreenode = (GenericObjectTreeNode)explorerNode.getTreeNode();
							final GenericObjectIdModuleProcess loimp = new GenericObjectIdModuleProcess(lotreenode.getId(),
									lotreenode.getModuleId(), lotreenode.getProcessId(), lotreenode.getLabel());
							lstloimp.add(loimp);
						}
					}
					if (!lstloimp.isEmpty()) {
						result = new TransferableGenericObjects(lstloimp);
					}
				}
			}
			return result;
		}

		@Override
		public boolean canImport(JComponent comp, DataFlavor[] aflavors) {
			log.debug("TransferHandler.canImport");
			// Unfortunately, this method is not called for each node, so we only can say yes or no
			// for the whole tree here. We must say yes to enable drop at all.

			return true;

//			class IsIdOrCVO implements CollectionUtils.UnaryPredicate {
//				public boolean evaluate(Object o) {
//					final DataFlavor flavor = (DataFlavor) o;
//					return (flavor instanceof GenericObjectIdModuleProcess.DataFlavor);
//				}
//			}
//			final Object oFlavor = CollectionUtils.findFirst(Arrays.asList(aflavors), new IsIdOrCVO());
//
//			return (oFlavor != null);
		}

		@Override
		public boolean importData(JComponent comp, final Transferable transferable) {
			result = false;

			if (comp instanceof JTree) {
				final JTree tree = (JTree) comp;
				final JTree.DropLocation dl = tree.getDropLocation();

				CommonClientWorkerAdapter<Collectable> importDataWorker = new CommonClientWorkerAdapter<Collectable>(null) {
					@Override
					public void init() {
						UIUtils.setWaitCursor(tree);
					}

					@Override
					public void work() {
						TransferHandler.this.importTransferData(tree, dl, transferable);
					}

					@Override
					public void paint() {
						tree.setCursor(null);
					}

					@Override
					public void handleError(Exception ex) {
						log.error(ex);
					}
				};

				CommonMultiThreader.getInstance().execute(importDataWorker);
			}

			return result;
		}

		private void importTransferData(JTree tree, JTree.DropLocation dropLoc, Transferable transferable) {
				try {
					// NUCLEUSINT-670: use the drop target, not the selected path
					TreePath targetPath = null;
					if(dropLoc != null && dropLoc != null)
						targetPath = dropLoc.getPath();
					if(targetPath == null)
						targetPath = tree.getSelectionPath();
					if(targetPath == null)
						return;
					ExplorerNode<?> targetNode = (ExplorerNode<?>) targetPath.getLastPathComponent();
					result = targetNode.importTransferData(parent, transferable, tree);
				}
				catch (UnsupportedFlavorException ex) {
					UIUtils.invokeOnDispatchThread(new Runnable() {
						@Override
						public void run() {
							JOptionPane.showMessageDialog(parent, CommonLocaleDelegate.getMessage("ExplorerController.16","Dieser Datentransfer wird von dem ausgew\u00e4hlten Objekt nicht unterst\u00fctzt."));
						}
					});
				}
				catch (IOException ex) {
					throw new CommonFatalException(ex);
				}
			}

		private ExplorerNode<?> getSelectedTreeNode(final JTree tree) {
			TreePath treePath = tree.getSelectionPath();
			return (treePath == null) ? null : (ExplorerNode<?>) treePath.getLastPathComponent();
		}
	}	// inner class TransferHandler

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
