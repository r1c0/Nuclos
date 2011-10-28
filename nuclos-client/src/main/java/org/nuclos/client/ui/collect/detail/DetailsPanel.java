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
package org.nuclos.client.ui.collect.detail;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.plaf.basic.BasicButtonUI;

import org.apache.log4j.Logger;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectPanel;
import org.nuclos.client.ui.collect.SearchOrDetailsPanel;
import org.nuclos.client.ui.collect.component.model.DetailsComponentModel;
import org.nuclos.client.ui.collect.component.model.DetailsEditModel;
import org.nuclos.client.ui.collect.indicator.CollectPanelIndicator;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common2.CommonLocaleDelegate;

/**
 * Details panel for collecting data.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public class DetailsPanel extends SearchOrDetailsPanel {
	
	protected static final Logger log = Logger.getLogger(SearchOrDetailsPanel.class);
	
	private final CollectPanelIndicator cpi = new CollectPanelIndicator(CollectPanel.TAB_DETAILS);

	/**
	 * Button: "Save"
	 */
	public final JButton btnSave = new JButton();

	/**
	 * Button: "Enter New mode"
	 */
	public final JButton btnNew = new JButton();

	/**
	 * MenuItem: "Clone the current object"
	 */
	public final JMenuItem btnClone = new JMenuItem();

	/**
	 * Button: "Delete the current object"
	 */
	public final AbstractButton btnDelete;

	/**
	 * Button: "Refresh (Reload) the current object"
	 */
	public final JButton btnRefreshCurrentCollectable = new JButton();

	/**
	 * Button: "Navigate to the first object"
	 */
	public final JButton btnFirst = new JButton();

	/**
	 * Button: "Navigate to the previous object"
	 */
	public final JButton btnPrevious = new JButton();

	/**
	 * Button: "Navigate to the last object"
	 */
	public final JButton btnLast = new JButton();

	/**
	 * Button: "Navigate to the first object"
	 */
	public final JButton btnNext = new JButton();
	
	/**
	 * Button: "Open in new tab"
	 */
	public final JMenuItem btnOpenInNewTab = new JMenuItem();
	
	/**
	 * Button: "Add Bookmark"
	 */
	public final JMenuItem btnBookmark = new JMenuItem();
	
	private final JScrollPane scrlpnDetails;
	
	public static final int recordNavIconSize = 11;
	
	/**
	 * constructs the details panel
	 */
	public DetailsPanel() {
		this(true);
	}

	/**
	 * constructs the details panel
	 */
	public DetailsPanel(boolean withScrollbar) {
		super(false);
		btnDelete = getDeleteButton();
		super.init();

		//this.add(pnlToolBar, BorderLayout.NORTH);
		
		if (withScrollbar) {
			this.scrlpnDetails = new JScrollPane(getCenteringPanel(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			this.scrlpnDetails.setBorder(BorderFactory.createEmptyBorder());
			this.scrlpnDetails.getHorizontalScrollBar().setUnitIncrement(20);
			this.scrlpnDetails.getVerticalScrollBar().setUnitIncrement(20);
			
			//this.add(scrlpnDetails, BorderLayout.CENTER);
			//this.add(UIUtils.newStatusBar(tfStatusBar), BorderLayout.SOUTH);
			
			this.setCenterComponent(scrlpnDetails);
		} else {
			this.scrlpnDetails = null;
			this.setCenterComponent(getCenteringPanel());
		}
		this.setSouthComponent(UIUtils.newStatusBar(this.btnFirst, this.btnPrevious, this.btnNext, this.btnLast, Box.createHorizontalStrut(10), this.tfStatusBar));
		
		this.btnFirst.setOpaque(false);
		this.btnFirst.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
		this.btnFirst.setContentAreaFilled(false);
		this.btnFirst.setName("btnFirst");
		this.btnFirst.setRolloverIcon(MainFrame.resizeAndCacheIcon(Icons.getInstance().getIconFirstWhiteHover16(), recordNavIconSize));
		this.btnFirst.setUI(new BasicButtonUI());
		
		this.btnPrevious.setOpaque(false);
		this.btnPrevious.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
		this.btnPrevious.setContentAreaFilled(false);
		this.btnPrevious.setName("btnPrevious");
		this.btnPrevious.setRolloverIcon(MainFrame.resizeAndCacheIcon(Icons.getInstance().getIconPreviousWhiteHover16(), recordNavIconSize));
		this.btnPrevious.setUI(new BasicButtonUI());
		
		this.btnNext.setOpaque(false);
		this.btnNext.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
		this.btnNext.setContentAreaFilled(false);
		this.btnNext.setName("btnNext");
		this.btnNext.setRolloverIcon(MainFrame.resizeAndCacheIcon(Icons.getInstance().getIconNextWhiteHover16(), recordNavIconSize));
		this.btnNext.setUI(new BasicButtonUI());
		
		this.btnLast.setOpaque(false);
		this.btnLast.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
		this.btnLast.setContentAreaFilled(false);
		this.btnLast.setName("btnLast");
		this.btnLast.setRolloverIcon(MainFrame.resizeAndCacheIcon(Icons.getInstance().getIconLastWhiteHover16(), recordNavIconSize));
		this.btnLast.setUI(new BasicButtonUI());
	}

	public final CollectPanelIndicator getCollectPanelIndicator() {
		return cpi;
	}
	
	@Override
	protected void setupDefaultToolBarActions( JToolBar toolBar) {
		
		toolBar.add(cpi.getJPanel());
		
		toolBar.add(btnSave, null);
		toolBar.add(btnRefreshCurrentCollectable, null);
		toolBar.add(btnNew, null);
		
		addPopupExtraMenuItem(btnClone);
		addPopupExtraSeparator();
		addPopupExtraMenuItem(btnBookmark);
		addPopupExtraMenuItem(btnOpenInNewTab);
		
		toolBar.add(btnDelete, null);

		this.btnSave.setName("btnSave");
		/** @todo why setEnabled here? */
		this.btnSave.setEnabled(true);
//		this.btnSave.setToolTipText("\u00c4nderungen an diesem Datensatz speichern");
//		this.btnSave.setIcon(Icons.getInstance().getIconSave16());
		this.btnSave.putClientProperty("hideActionText", Boolean.TRUE);

		this.disguiseRefreshButton(false);

		this.btnNew.setName("btnNew");
		this.btnNew.putClientProperty("hideActionText", Boolean.TRUE);
		btnBookmark.setName("btnBookmark");
		this.btnClone.setName("btnClone");
		//this.btnClone.putClientProperty("hideActionText", Boolean.TRUE);
		this.btnRefreshCurrentCollectable.setName("btnRefreshCurrentCollectable");
		this.btnRefreshCurrentCollectable.putClientProperty("hideActionText", Boolean.TRUE);
		this.btnDelete.setName("btnDelete");
		this.btnDelete.putClientProperty("hideActionText", Boolean.TRUE);

	}

	/**
	 * @return the model of the edit view.
	 * @see #getEditView()
	 */
	@Override
	public DetailsEditModel getEditModel() {
		return (DetailsEditModel) this.getEditView().getModel();
	}

	/**
	 * disguises the refresh button as a cancel button
	 */
	public void disguiseRefreshButton(boolean bDisguise) {

		this.btnRefreshCurrentCollectable.setIcon(bDisguise ?
				Icons.getInstance().getIconCancel16() :
				Icons.getInstance().getIconRefresh16());

		this.btnRefreshCurrentCollectable.setToolTipText(bDisguise ?
			CommonLocaleDelegate.getMessage("DetailsPanel.1", "Die Bearbeitung des Datensatzes abbrechen (\u00c4nderungen verwerfen)") :
				CommonLocaleDelegate.getMessage("DetailsPanel.2", "Aktualisieren (Datensatz neu laden und \u00c4nderungen verwerfen)"));
	}

	public static String getTextForMultiEditChange(DetailsPanel pnlDetails) {
		return pnlDetails.getMultiEditChangeMessage();
	}

	/**
	 * @return the message to display in the status bar when changes in multi edit mode occured.
	 * @precondition this.getCollectStateModel().getCollectState().isDetailsModeMultiViewOrEdit()
	 * @todo this.isMultiEditable() bzw. this.getState().isMulti[ViewOrEdit]()
	 */
	public String getMultiEditChangeMessage() {
		final StringBuffer sb = new StringBuffer();

		for (DetailsComponentModel clctcompmodel : this.getEditModel().getCollectableComponentModels()) {
			if (clctcompmodel.isValueToBeChanged()) {
				if (sb.length() > 0) {
					sb.append(", ");
				}
				sb.append(clctcompmodel.getEntityField().getLabel());
				sb.append(" = ");
				final CollectableField clctfValue = clctcompmodel.getField();
				if (clctfValue.isNull()) {
					sb.append(CommonLocaleDelegate.getMessage("DetailsPanel.3", "<leer>"));
				}
				else {
					sb.append(clctfValue.toString());
				}
			}
		}
		return sb.toString();
	}
	
	protected AbstractButton getDeleteButton() {
		return new JButton();
	}
	
	/**
	 * 
	 * @param component
	 * @return false if component not found or location on screen could not be determinded
	 */
	public boolean ensureComponentIsVisible(JComponent component) {	
		List<JComponent> startPath = new ArrayList<JComponent>();
		startPath.add(getEditComponent());
		List<JComponent> resultPath = getComponentPath(startPath, component);
		if (resultPath != null) {
			for (int i = 0; i < resultPath.size(); i++) {
				JComponent c = resultPath.get(i);
				if (c instanceof JTabbedPane) {
					JTabbedPane tab = (JTabbedPane) c;
					if (resultPath.size() >= i+1) {
						int tabindex = tab.indexOfComponent(resultPath.get(i+1));
						if (tab.getSelectedIndex() != tabindex)
							tab.setSelectedIndex(tabindex);
					}
				}
			}
			
			for (int i = 0; i < resultPath.size(); i++) {
				Point location = resultPath.get(i).getLocation();
				resultPath.get(i).scrollRectToVisible(new Rectangle(location, resultPath.get(i).getPreferredSize()));
			}
			
			Point targetLocation = component.getLocation();
			component.scrollRectToVisible(new Rectangle(targetLocation, component.getPreferredSize()));
			
			try {
				component.getLocationOnScreen();
				return true;
			} catch (IllegalComponentStateException e) {
				return false;
			}
		}
		return false;
	}
	
	private List<JComponent> getComponentPath(List<JComponent> path, JComponent target) {
		for (Component c : path.get(path.size()-1).getComponents()) {
			if (c == target)
				return path;
			else
				if (c instanceof JComponent) {
					List<JComponent> extendedPath = new ArrayList<JComponent>(path);
					extendedPath.add((JComponent) c);
					List<JComponent> resultTemp = getComponentPath(extendedPath, target);
					if (resultTemp != null)
						return resultTemp;
				}
		}
		
		return null;
	}
	
	/**
	 * shows a spot on <code>comp</code>
	 * @param comp
	 */
	public void spotComponent(JComponent comp) {
		try {
			(new ComponentSpot(comp)).setVisible(true);
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	class ComponentSpot extends Window implements AncestorListener {
		
		private JComponent parent;

		ComponentSpot(JComponent parent) throws HeadlessException {
			super(null);
			this.parent = parent;
			if(parent != null) {
				addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) { ComponentSpot.this.dispose(); }
				});
				parent.addAncestorListener(this);
				Window windowAncestor = SwingUtilities.getWindowAncestor(parent);
				if(windowAncestor != null)
					windowAncestor.addWindowListener(new WindowAdapter() {
						@Override
						public void windowDeactivated(WindowEvent e) { ComponentSpot.this.dispose(); }
						@Override
						public void windowIconified(WindowEvent e) { ComponentSpot.this.dispose(); }
						@Override
						public void windowClosing(WindowEvent e) { ComponentSpot.this.dispose(); }
					});
				relocate(parent);
			}
			final Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					dispose();
					timer.cancel();
				}}, 5 * 1000);
			setAlwaysOnTop(true);
			UIUtils.setWindowOpacity(ComponentSpot.this, 0.5f);
		}
		
		@Override
		public void paint(Graphics g) {
			if (parent != null) {
				Graphics2D g2 = (Graphics2D) g;
				
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
				g2.setColor(Color.BLACK);
				
				Point locationParent = parent.getLocationOnScreen();
				Point locationDetails = DetailsPanel.this.getEditComponent().getLocationOnScreen();
				Dimension sizeParent = parent.getSize();
				Dimension sizeDetails = DetailsPanel.this.getEditComponent().getSize();
				
				int x1, x2, x3, x4;
				int y1, y2, y3, y4;
				
				x1 = 0;
				x2 = locationParent.x-locationDetails.x;
				x3 = x2 + sizeParent.width;
				x4 = sizeDetails.width;
				
				y1 = 0;
				y2 = locationParent.y-locationDetails.y;
				y3 = y2 + sizeParent.height;
				y4 = sizeDetails.height;
				
				g2.fillRect(x1, y1, x4-x1, y4-y1);
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OUT));
				g2.setPaint(new GradientPaint(new Point(x2+5, y2-10), new Color(255,255,255,0), new Point(x3-20, y3), Color.BLACK));
				g2.fillOval(x2-10, y2-10, x3-x2+20, y3-y2+20);
			}
		}

		private void relocate(Component parent) {
			Point location = DetailsPanel.this.getEditComponent().getLocationOnScreen();
			setBounds(location.x, location.y, DetailsPanel.this.getEditComponent().getSize().width, DetailsPanel.this.getEditComponent().getSize().height);
	    }
		
		@Override
		public void ancestorAdded(AncestorEvent event) {
		}

		@Override
		public void ancestorRemoved(AncestorEvent event) {
			ComponentSpot.this.dispose();
		}

		@Override
		public void ancestorMoved(AncestorEvent event) {
			relocate (event.getComponent());
		}
	}

}  // class DetailsPanel
