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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ToolTipManager;

import org.nuclos.client.explorer.ui.ExplorerNodeRenderer;
import org.nuclos.client.main.Main;
import org.nuclos.client.synthetica.NuclosThemeSettings;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.navigation.treenode.TreeNode;

/**
 * An <code>ExplorerView</code> displays one tree in a tab of an <code>ExplorerPanel</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */

public class DefaultExplorerView extends JPanel implements ExplorerView {

	public static final int FADE = 2;

	private final JToolBar toolBar = UIUtils.createNonFloatableToolBar();

	private List<JComponent> additionalToolBarContents;

	private final JPanel content = new JPanel(new BorderLayout()) {

		@Override
		public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setPaint(new GradientPaint(0, 0, NuclosThemeSettings.BACKGROUND_PANEL, 0, FADE, Color.WHITE));
			g2.fillRect(0, 0, getWidth(), getHeight());

			super.paint(g);
		}
	};

	private final JScrollPane scrlpn = new JScrollPane();

	private final JTree tree;

	/**
	 * creates an explorer view
	 * @param tree
	 */
	public DefaultExplorerView(TreeNode tn) {
		super(new BorderLayout());
		this.setOpaque(false);
		this.setBorder(BorderFactory.createEmptyBorder());

		this.scrlpn.setBorder(BorderFactory.createEmptyBorder());
		this.scrlpn.setOpaque(false);
		this.scrlpn.getViewport().setOpaque(false);

		this.content.setOpaque(true);
		this.content.setBackground(new Color (0, 0, 0, 0));
		this.content.setBorder(BorderFactory.createEmptyBorder(FADE, 0, 0, 0));
		this.content.add(this.scrlpn, BorderLayout.CENTER);

		this.add(this.toolBar, BorderLayout.NORTH);
		this.add(this.content, BorderLayout.CENTER);

		final ExplorerNode<?> explorernodeRoot = ExplorerController.newExplorerTree(tn);
		this.tree = new JTree(explorernodeRoot, true);

		this.tree.putClientProperty("JTree.lineStyle", "Angled");
		this.tree.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
		this.tree.setBackground(Color.WHITE);
		this.tree.setRootVisible(true);
		this.tree.setShowsRootHandles(true);

		// enable drag:
		this.tree.setDragEnabled(true);
		this.tree.setTransferHandler(new DefaultTransferHandler(Main.getMainFrame()));

		// don't expand on double click:
		this.tree.setToggleClickCount(0);

		this.tree.addKeyListener(new DefaultKeyListener(tree));
		this.tree.addMouseListener(new DefaultMouseListener(tree));

		// enable tool tips:
		ToolTipManager.sharedInstance().registerComponent(this.tree);

		this.tree.setCellRenderer(new ExplorerNodeRenderer());
		this.tree.addTreeWillExpandListener(new DefaultTreeWillExpandListener(tree));

		this.scrlpn.getViewport().add(tree, null);
	}
	
	protected void init() {
		for (JComponent c : getToolBarComponents()) {
			toolBar.add(c);
		}	
	}

	protected List<JComponent> getToolBarComponents() {
		List<JComponent> components = new ArrayList<JComponent>();
		components.add(new JButton(new AbstractAction("", Icons.getInstance().getIconRefresh16()) {

			@Override
			public void actionPerformed(ActionEvent e) {
				UIUtils.runCommand(DefaultExplorerView.this.getParent(), new CommonRunnable() {
					@Override
		            public void run() throws CommonFinderException {
						Main.getMainController().getExplorerController().refreshTab(DefaultExplorerView.this);
					}
				});
			}
		}));
		return components;
	}

	/**
	 * @return the JTree contained in this ExplorerView
	 */
	public JTree getJTree() {
		return tree;
	}

	/**
	 * @return the root ExplorerNode contained in this view
	 */
	public ExplorerNode<?> getRootNode() {
		return (ExplorerNode<?>) this.getJTree().getModel().getRoot();
	}

	@Override
	public JComponent getViewComponent() {
		return this;
	}
}	// class ExplorerView
