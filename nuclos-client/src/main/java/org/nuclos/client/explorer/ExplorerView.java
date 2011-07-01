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
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;

import org.nuclos.client.synthetica.NuclosSyntheticaConstants;
import org.nuclos.client.ui.UIUtils;

/**
 * An <code>ExplorerView</code> displays one tree in a tab of an <code>ExplorerPanel</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */

public class ExplorerView extends JPanel {
	
	public static final int FADE = 2;
	
	private final JToolBar toolBar = UIUtils.createNonFloatableToolBar();
	
	List<JComponent> additionalToolBarContents;
	
	private final JPanel content = new JPanel(new BorderLayout()) {
		@Override
		public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setPaint(new GradientPaint(0, 0, NuclosSyntheticaConstants.DEFAULT_BACKGROUND, 0, FADE, Color.WHITE));
			g2.fillRect(0, 0, getWidth(), getHeight());
			
			super.paint(g);
		}
	};
	
	private final JScrollPane scrlpn = new JScrollPane();

	/**
	 * creates an explorer view
	 * @param tree
	 */
	public ExplorerView(JTree tree) {
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

		this.setJTree(tree);
	}

	/**
	 * @return the JTree contained in this ExplorerView
	 */
	public JTree getJTree() {
		return (JTree) this.scrlpn.getViewport().getComponent(0);
	}
	
	/**
	 * 
	 * @param contents
	 */
	public void setToolBarComponents(List<JComponent> contents) {
		toolBar.removeAll();
		for (JComponent c : contents) {
			toolBar.add(c);
		}
	}
	
	/**
	 * 
	 * @param contents
	 */
	public void addAdditionalToolBarComponents(List<JComponent> contents) {
		
		if (additionalToolBarContents != null) {
			for (JComponent c : additionalToolBarContents) {
				toolBar.remove(c);
			}
		}
		
		additionalToolBarContents = new ArrayList<JComponent>(contents);
		for (JComponent c : additionalToolBarContents) {
			toolBar.add(c);
		}
	}

	/**
	 * sets the tree for this view
	 * @param tree
	 */
	public void setJTree(final JTree tree) {
		if (this.scrlpn.getViewport().getComponentCount() > 0) {
			this.scrlpn.getViewport().remove(0);
		}
		
		UIUtils.invokeOnDispatchThread(new Runnable() {
			@Override
			public void run() {
				ExplorerView.this.scrlpn.getViewport().add(tree, null);
				tree.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
			}
		});
	}

	/**
	 * @return the root ExplorerNode contained in this view
	 */
	public ExplorerNode<?> getRootNode() {
		return (ExplorerNode<?>) this.getJTree().getModel().getRoot();
	}

}	// class ExplorerView
