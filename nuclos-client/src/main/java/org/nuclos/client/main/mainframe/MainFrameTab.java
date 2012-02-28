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
package org.nuclos.client.main.mainframe;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.apache.log4j.Logger;
import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.LayerUI;
import org.jdesktop.jxlayer.plaf.ext.LockableUI;
import org.jdesktop.swingx.painter.BusyPainter;
import org.jdesktop.swingx.painter.TextPainter;
import org.nuclos.client.common.NuclosDropTargetListener;
import org.nuclos.client.common.NuclosDropTargetVisitor;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.jms.TopicNotificationReceiver;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.workspace.ITabStoreController;
import org.nuclos.client.synthetica.NuclosThemeSettings;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.IMainFrameTabClosableController;
import org.nuclos.client.ui.IOverlayChangeListener;
import org.nuclos.client.ui.IOverlayComponent;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.MainFrameTabListener;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.Actions;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.LockedTabProgressNotification;
import org.nuclos.common.MutableBoolean;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * JInternalFrame replacement that corrects the ill minimum size behavior of the
 * corresponding javax.swing class.
 * Also it puts up its own management of glass pane to allow internal frames to be blocked by background processes.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
@Configurable(preConstruction=true)
public class MainFrameTab extends JPanel implements IOverlayComponent, NuclosDropTargetVisitor {

	public static final String IMAGE_ICON_PROPERTY = "NOVABIT_DESKTOP_ICON";

	private static final Logger LOG = Logger.getLogger(MainFrameTab.class);

	private final List<MainFrameTabListener> mainFrameTabListeners = new ArrayList<MainFrameTabListener>();

	private ITabStoreController storeController;

	private boolean neverClose;
	private boolean fromAssigned;

	private String title;

	private ImageIcon icon;

	private TabTitle tabTitle = new TabTitle();

	private JXLayer<JComponent> layer;
	private Color layerBusyColor = new Color(128, 128, 128, 128);
	private Color layerBusyColorDarker = new Color(72, 72, 72, 196);

	private static final Color overlayLockColor = new Color(255, 255, 255, 180);
	private static final Color overlayBorderColor = new Color(
		NuclosThemeSettings.BACKGROUND_ROOTPANE.getRed(),
		NuclosThemeSettings.BACKGROUND_ROOTPANE.getGreen(),
		NuclosThemeSettings.BACKGROUND_ROOTPANE.getBlue(),
		200);

	private final JLayeredPane layered = new JLayeredPane();
	private OverlayComponent overlay;

	private final List<IOverlayChangeListener> overlayChangeListeners = new ArrayList<IOverlayChangeListener>(1);
	
	private SpringLocaleDelegate localeDelegate;

	private final AbstractAction actMaximize = new AbstractAction(localeDelegate.getMessage("MainFrameTab.5","Maximieren"), 
			Icons.getInstance().getIconTabbedPaneMax()) {
		@Override
		public void actionPerformed(ActionEvent e) {
			Main.getInstance().getMainFrame().maximizeTabbedPane(MainFrame.getTabbedPane(MainFrameTab.this));
		}
		@Override
		public boolean isEnabled() {
			MainFrameTabbedPane tabbedPane = MainFrame.getTabbedPane(MainFrameTab.this);
			return MainFrame.isTabbedPaneMaximizable(tabbedPane) && !MainFrame.isTabbedPaneMaximized(tabbedPane);
		}
	};
	private final AbstractAction actRestore = new AbstractAction(localeDelegate.getMessage("MainFrameTab.6","Wiederherstellen"), 
			Icons.getInstance().getIconTabbedPaneSplit()) {
		@Override
		public void actionPerformed(ActionEvent e) {
			MainFrameTabbedPane tabbedPane = MainFrame.getTabbedPane(MainFrameTab.this);
			Main.getInstance().getMainFrame().restoreTabbedPaneContainingArea(tabbedPane);
		}
		@Override
		public boolean isEnabled() {
			MainFrameTabbedPane tabbedPane = MainFrame.getTabbedPane(MainFrameTab.this);
			return MainFrame.isTabbedPaneMaximizable(tabbedPane) && MainFrame.isTabbedPaneMaximized(tabbedPane);
		}
	};
	private final AbstractAction actNeverClose = new AbstractAction(localeDelegate.getMessage("MainFrameTab.7","Niemals Schließen")) {
		@Override
		public void actionPerformed(ActionEvent e) {
			neverClose = !neverClose;
		}
		@Override
		public Object getValue(String key) {
			if (Action.SELECTED_KEY.equals(key))
				return !_isClosable() || neverClose;
			else
				return super.getValue(key);
		}
		@Override
		public boolean isEnabled() {
			return _isClosable();
		}
	};
	private final AbstractAction actClose = new AbstractAction(localeDelegate.getMessage("MainFrameTab.4","Schließen")) {
		@Override
		public void actionPerformed(ActionEvent e) {
			dispose();
		}
		@Override
		public boolean isEnabled() {
			return isClosable();
		}
	};
	private final AbstractAction actCloseAll = new AbstractAction(localeDelegate.getMessage("MainFrameTab.3","Alle Schließen")) {

		@Override
		public void actionPerformed(ActionEvent e) {
			MainFrameTabbedPane tabbedPane = MainFrame.getTabbedPane(MainFrameTab.this);
			tabbedPane.closeAllTabs();
			tabbedPane.adjustTabs();
		}
	};
	private final AbstractAction actCloseOthers = new AbstractAction(localeDelegate.getMessage("MainFrameTab.2","Andere Schließen")) {

		@Override
		public void actionPerformed(ActionEvent e) {
			MainFrameTabbedPane tabbedPane = MainFrame.getTabbedPane(MainFrameTab.this);
			tabbedPane.closeAllTabs(MainFrameTab.this);
			tabbedPane.adjustTabs();
		}
	};

	/**
	 *
	 */
	public MainFrameTab() {
		this(null);
	}

	/**
	 *
	 * @param sTitle
	 */
	public MainFrameTab(String sTitle) {
		super();
		setLayout(new BorderLayout());

		add(layered, BorderLayout.CENTER);
		this.setTitle(sTitle);
		this.layer = null;

		layered.setOpaque(false);

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				final Dimension size = MainFrameTab.this.getBounds().getSize();
				if (layer != null) {
					layer.setBounds(0, 0, size.width, size.height);
				}
				if (overlay != null) {
					overlay.setSizes(size);
				}
				revalidate();
			}
		});
		setupDragDrop();
	}
	
	@Autowired
	void setSpringLocaleDelegate(SpringLocaleDelegate cld) {
		this.localeDelegate = cld;
	}

	protected void setupDragDrop() {
		DropTarget drop = new DropTarget(this.tabTitle, new NuclosDropTargetListener(this));
		drop.setActive(true);
	}

	/**
	 *
	 * @return
	 */
	public boolean hasTabStoreController() {
		return this.storeController != null;
	}

	/**
	 *
	 * @param storeController
	 */
	public void setTabStoreController(ITabStoreController storeController) {
		this.storeController = storeController;
	}

	/**
	 *
	 * @return
	 */
	public ITabStoreController getTabStoreController() {
		return this.storeController;
	}

	/**
	 *
	 * @param layerComponent
	 */
	public void setLayeredComponent(final JComponent layerComponent){
		if(layerComponent != null){
			layer = new JXLayer<JComponent>(layerComponent);
			layer.setName("JXLayerGlasspane");
			layer.setOpaque(false);
			layer.setLocation(0, 0);

			layered.add(layer, new Integer(1));
		}
	}

	/**
	 *
	 * @param layerComponent
	 * @param layoutPosition
	 */
	public void setLayeredComponent(JComponent layerComponent, String layoutPosition){
		if(layerComponent != null && layoutPosition != null){
			layer = new JXLayer<JComponent>(layerComponent);
	      add(layer, layoutPosition);
		}
	}

	/**
	 * adjusts the ill JInternalFrame.getMinimumSize() behavior
	 * {@inheritDoc}
	 */
	@Override
	public Dimension getMinimumSize() {
		Dimension result = super.getMinimumSize();

		/*Dimension dimRootPane = this.getRootPane().getMinimumSize();
		Insets insetsFrame = this.getInsets();

		result.width = Math.max(result.width, dimRootPane.width + insetsFrame.left + insetsFrame.right);
		result.height += dimRootPane.height;*/

		return result;
	}

	/**
	 *
	 * @return
	 */
	public Color getLayerBusyColor() {
		return layerBusyColor;
	}

	/**
	 *
	 * @param layerBusyColor
	 */
	public void setLayerBusyColor(Color layerBusyColor) {
		this.layerBusyColor = layerBusyColor;
	}

	/**
	 *
	 */
	public void lockLayer() {
		lockLayer(new TranslucentLockableUI(layerBusyColor));
	}

	/**
	 *
	 * @param correlationId
	 */
	public void lockLayerWithProgress(String correlationId) {
		lockLayer(new TranslucentLockableWithProgressUI(layerBusyColorDarker, correlationId));
	}

	/**
	 *
	 */
	public void lockLayerBusy() {
		lockLayer(new BusyLockableUI(layerBusyColor));
	}

	/**
	 *
	 * @param lockableUI
	 */
	protected void lockLayer(LockableUI lockableUI) {
		if (layer != null) {
			LayerUI<?> currentUI = layer.getUI();
			if (currentUI instanceof LockableUI && ((LockableUI) currentUI).isLocked()) {
				// if current layer is locked, do nothing
				return;
			}
			layer.setUI(lockableUI);
			lockableUI.setLocked(true);
		}
	}

	/**
	 *
	 */
	public void unlockLayer() {
		if (layer != null) {
			LayerUI<?> currentUI = layer.getUI();
			if (currentUI instanceof LockableUI) {
				((LockableUI) currentUI).setLocked(false);
			}
			try {layer.setUI(null);} catch (Exception ignore) {}
		}
	}

	/**
	 *
	 */
	@Override
	protected void addImpl(Component comp, Object constraints, int index) {
		if (comp instanceof IOverlayComponent) {
			setOverlayComponent((IOverlayComponent) comp);
		} else {
			super.addImpl(comp, constraints, index);
		}
	}

	/**
	 *
	 * @param oc
	 */
	public void setOverlayComponent(IOverlayComponent oc) {
		try {
			removeOverlayComponent(oc);
		} catch(CommonBusinessException e) {
			Errors.getInstance().showExceptionDialog(this, e);
			return;
		}

		final Component c = (Component) oc;

		overlay = new OverlayComponent(oc);
		oc.addOverlayChangeListener(overlay);

		overlay.setSizes(getSize());

		layered.add(overlay.getLockPanel(), new Integer(2));
		layered.add(overlay, new Integer(3));
	}

	/**
	 *
	 * @param oc
	 * @throws CommonBusinessException
	 */
	public void removeOverlayComponent(final IOverlayComponent oc) throws CommonBusinessException {
		if (oc instanceof MainFrameTab) {
			((MainFrameTab)oc).notifyClosing();
		}

		if (overlay != null) {
			UIUtils.runCommand(layered, new Runnable() {

				@Override
				public void run() {
					oc.removeOverlayChangeListener(overlay);
					layered.remove(overlay.getLockPanel());
					layered.remove(overlay);
					overlay = null;
					layered.revalidate();
					layered.repaint();

					if (oc instanceof MainFrameTab) {
						((MainFrameTab)oc).notifyClosed();
					}
				}
			});
		}
	}

	/**
	 *
	 *
	 */
	private class OverlayComponent extends JPanel implements IOverlayChangeListener {

		public static final int HEADER_HEIGHT = 18;

		public static final int BORDER_SIZE = 2;

		public static final int OUTER_INSET_TOP = 20;
		public static final int OUTER_INSET_LEFT = 20;
		public static final int OUTER_INSET_BOTTOM = 10;
		public static final int OUTER_INSET_RIGHT = 20;

		private final Component c;

		private final IOverlayComponent oc;

		private final JLabel title = new JLabel();

		private final JPanel lockPanel = new JPanel() {

			@Override
			public void paint(Graphics g) {
				g.setColor(overlayLockColor);
				g.fillRect(0, 0, getWidth(), getHeight());
			}
		};

		/**
		 *
		 * @param oc
		 */
		public OverlayComponent(final IOverlayComponent oc) {
			super(new BorderLayout());
			this.oc = oc;
			this.c = (Component) oc;

			setOpaque(false);
			setBorder(BorderFactory.createEmptyBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE));

			final JPanel header = new JPanel(new BorderLayout());
			header.setOpaque(false);
			header.setMinimumSize(new Dimension(new Dimension(0, HEADER_HEIGHT)));
			header.setMaximumSize(new Dimension(new Dimension(Integer.MAX_VALUE, HEADER_HEIGHT)));

			final JLabel close = new JLabel(localeDelegate.getMessage("MainFrameTab.1","Close"), Icons.getInstance().getIconTabCloseButton(), JLabel.RIGHT);
			close.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					close.setIcon(Icons.getInstance().getIconTabCloseButton_hover());
				}
				@Override
				public void mouseExited(MouseEvent e) {
					close.setIcon(Icons.getInstance().getIconTabCloseButton());
				}
				@Override
				public void mouseClicked(MouseEvent e) {
					if (oc.isClosable()) {
						try {
							removeOverlayComponent(oc);
						} catch(CommonBusinessException e1) {
							Errors.getInstance().showExceptionDialog(null, e1);
							return;
						}
					}
				}
			});
			close.setAlignmentX(Component.RIGHT_ALIGNMENT);
			close.setHorizontalTextPosition(JLabel.LEFT);
			close.setForeground(Color.WHITE);

			title.setForeground(Color.WHITE);
			header.add(title, BorderLayout.WEST);
			header.add(close, BorderLayout.EAST);

			add(header, BorderLayout.NORTH);
			add(c, BorderLayout.CENTER);

			setupLockPanel();
		}

		/**
		 *
		 * @param size
		 * @return
		 */
		protected Dimension getOverlayInnerSize(Dimension size) {
			final Rectangle outerBounds = getOverlayOuterBounds(size);
			return new Dimension(outerBounds.width-BORDER_SIZE-BORDER_SIZE,
				outerBounds.height-BORDER_SIZE-BORDER_SIZE-HEADER_HEIGHT);
		}

		/**
		 *
		 * @param size
		 * @return
		 */
		protected Rectangle getOverlayOuterBounds(Dimension size) {
			return new Rectangle(OUTER_INSET_LEFT, OUTER_INSET_TOP, size.width-OUTER_INSET_LEFT-OUTER_INSET_RIGHT, size.height-OUTER_INSET_TOP-OUTER_INSET_BOTTOM);
		}

		/**
		 *
		 * @param size
		 */
		private void setSizes(Dimension size) {
			lockPanel.setSize(size);
			setBounds(getOverlayOuterBounds(size));

			oc.transferSize(getOverlayInnerSize(size));
		}

		/**
		 *
		 */
		private void setupLockPanel() {
			lockPanel.setOpaque(false);
			lockPanel.addMouseListener(new MouseAdapter() {});
			lockPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}

		/**
		 *
		 * @return
		 */
		public JPanel getLockPanel() {
			return lockPanel;
		}

		@Override
		public void paint(Graphics g) {
			g.setColor(overlayBorderColor);
			g.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
			super.paint(g);
		}

		/**
		 *
		 * @param newTitle
		 * @param newIcon
		 */
		@Override
		public void titleChanged(String newTitle, Icon newIcon) {
			title.setText(newTitle);
			title.setIcon(newIcon);
		}

		/**
		 *
		 */
		@Override
		public void closeOverlay() {
			try {
				removeOverlayComponent(oc);
			} catch(CommonBusinessException e) {
				Errors.getInstance().showExceptionDialog(this, e);
				return;
			}
		}
	}

	// class TranslucentLockableUI --->
	public static class TranslucentLockableUI extends LockableUI {

		protected final Color busyColor;

		public TranslucentLockableUI(Color busyColor) {
			this.busyColor = busyColor;
		}

		@Override
		protected void paintLayer(Graphics2D g2, JXLayer<JComponent> l) {
			super.paintLayer(g2, l);
         if (isLocked()) {
            g2.setColor(busyColor);
            g2.fillRect(0, 0, l.getWidth(), l.getHeight());
        }
		}
   } // class TranslucentLockableUI

	@Configurable
	public static class TranslucentLockableWithProgressUI extends TranslucentLockableUI implements MessageListener {

		private static final Logger LOG = Logger.getLogger(TranslucentLockableWithProgressUI.class);

		private final TextPainter textProgress;
		private final TextPainter textMessage;
		private final String correlationId;

		private final MutableBoolean mutLocked = new MutableBoolean(false);
		
		private TopicNotificationReceiver tnr;

		public TranslucentLockableWithProgressUI(Color busyColor, String correlationId) {
			super(busyColor);
			this.correlationId = correlationId;
			Font fProgress = new Font("Monospaced", Font.PLAIN, 24);
			Font fMessage = new Font("Monospaced", Font.PLAIN, 12);
			textProgress = new TextPainter("", Color.WHITE);
			textProgress.setFont(fProgress);
			textMessage = new TextPainter("", Color.WHITE);
			textMessage.setFont(fMessage);
			setText("", 0);
		}
		
		@Autowired
		void setTopicNotificationReceiver(TopicNotificationReceiver tnr) {
			this.tnr = tnr;
		}

		@Override
		public void setLocked(boolean locked) {
			mutLocked.setValue(locked);
			if (locked) {
				LOG.info("subscribe to " + JMSConstants.TOPICNAME_LOCKEDTABPROGRESSNOTIFICATION + " with correlationId=" + correlationId);
				tnr.subscribe(JMSConstants.TOPICNAME_LOCKEDTABPROGRESSNOTIFICATION, correlationId, TranslucentLockableWithProgressUI.this);
			} else {
				LOG.info("unsubscribe from " + JMSConstants.TOPICNAME_LOCKEDTABPROGRESSNOTIFICATION);
				tnr.unsubscribe(TranslucentLockableWithProgressUI.this);
			}
			while (isDirty()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					LOG.error(null, e);
				}
			}
			super.setLocked(locked);
		}

		@Override
		public void onMessage(Message message) {
			try {
			    if (message instanceof ObjectMessage) {
			    	ObjectMessage objectMessage = (ObjectMessage) message;
			    	if (objectMessage.getObject() instanceof LockedTabProgressNotification) {
			    		LockedTabProgressNotification notification = (LockedTabProgressNotification) objectMessage.getObject();
			    		LOG.info("onMessage: Received LockedTabProgressNotification " + notification);
			    		setText(notification.getMessage(), LangUtils.defaultIfNull(notification.getPercent(), 0));
			    	}
			    }
			}
			catch (JMSException ex) {
				LOG.error(ex);
			}
		}

		private void setText(String message, int percent) {
			if (mutLocked.getValue() && isLocked()) {
				percent = percent < 0 ? 0 : percent > 100 ? 100 : percent;
				int progress = percent / 5;
				StringBuffer sbProgress = new StringBuffer(27);
				sbProgress.append('[');
				for (int i = 1; i <= 20; i++) {
					sbProgress.append(i <= progress ? '=' : '-');
				}
				sbProgress.append("] ");
				if (percent < 100) sbProgress.append(' ');
				if (percent < 10) sbProgress.append(' ');
				sbProgress.append(percent);
				sbProgress.append('%');

				textProgress.setText(sbProgress.toString());
				textMessage.setText(message);
				// this will repaint the layer
				setDirty(true);
			}
		}

		@Override
		protected void paintLayer(Graphics2D g2, JXLayer<JComponent> l) {
			super.paintLayer(g2, l);
			if (mutLocked.getValue()) {
				textProgress.paint(g2, l, l.getWidth(), l.getHeight());
				textMessage.paint(g2, l, l.getWidth(), l.getHeight()+50);
			}
		}

	}

	public static class BusyLockableUI extends TranslucentLockableUI implements ActionListener {
		private BusyPainter busyPainter;
		private Timer timer;
		private int frameNumber;

		public BusyLockableUI(Color busyColor) {
			super(busyColor);
			busyPainter = new BusyPainter();
			busyPainter.setPaintCentered(true);
			busyPainter.setPointShape(new Ellipse2D.Double(0, 0, 20, 20));
			busyPainter.setTrajectory(new Ellipse2D.Double(0, 0, 100, 100));
			busyPainter.setBaseColor(busyColor.darker());
			busyPainter.setHighlightColor(busyColor.darker().darker().darker());
			timer = new Timer(100, this);
		}

		@Override
		protected void paintLayer(Graphics2D g2, JXLayer<JComponent> l) {
			super.paintLayer(g2, l);
			if (isLocked()) {
				busyPainter.paint(g2, l, l.getWidth(), l.getHeight());
			}
		}

		@Override
		public void setLocked(boolean isLocked) {
			super.setLocked(isLocked);
			if (isLocked) {
				timer.start();
			} else {
				timer.stop();
			}
		}

		// Change the frame for the busyPainter
		// and mark BusyPainterUI as dirty
		@Override
		public void actionPerformed(ActionEvent e) {
			frameNumber = (frameNumber + 1) % 8;
			busyPainter.setFrame(frameNumber);
			// this will repaint the layer
			setDirty(true);
		}
	}


	/*
	* moves this frame to the specified position with offset (20,20).
	*
	*/
    public void setAnchorLocation(Point anchorLocation){
    	Point newAnchorLocation = new Point(anchorLocation);
    	newAnchorLocation.setLocation(anchorLocation.getX()+20, anchorLocation.getY()+20);
    	this.setLocation(newAnchorLocation);
    }

    /**
     *
     * @param listener
     */
	public void addMainFrameTabListener(MainFrameTabListener listener) {
		mainFrameTabListeners.add(listener);
	}

	/**
	 *
	 * @param listener
	 */
	public void removeMainFrameTabListener(MainFrameTabListener listener) {
		mainFrameTabListeners.remove(listener);
	}

	/**
	 *
	 */
	public void notifySelected() {
		for (MainFrameTabListener listener : new ArrayList<MainFrameTabListener>(mainFrameTabListeners)) {
			listener.tabSelected(this);
		}
	}

	/**
	 *
	 */
	public void notifyAdded() {
		for (MainFrameTabListener listener : new ArrayList<MainFrameTabListener>(mainFrameTabListeners)) {
			listener.tabAdded(this);
		}
	}

	/**
	 *
	 * @return
	 * @throws CommonBusinessException
	 */
	public boolean notifyClosing() throws CommonBusinessException {
		boolean result = true;
		for (MainFrameTabListener listener : new ArrayList<MainFrameTabListener>(mainFrameTabListeners)) {
			if (!listener.tabClosing(this)) {
				result = false;
			}
		}
		return result;
	}

	/**
	 *
	 */
	public void notifyClosed() {
		for (MainFrameTabListener listener : new ArrayList<MainFrameTabListener>(mainFrameTabListeners)) {
			listener.tabClosed(this);
		}
	}

	/**
	 *
	 */
	public void notifyHidden() {
		for (MainFrameTabListener listener : new ArrayList<MainFrameTabListener>(mainFrameTabListeners)) {
			listener.tabHidden(this);
		}
	}

	/**
	 *
	 */
	public void notifyRestoredFromHidden() {
		for (MainFrameTabListener listener : new ArrayList<MainFrameTabListener>(mainFrameTabListeners)) {
			listener.tabRestoredFromHidden(this);
		}
	}

	/**
	 *
	 * @param icon
	 */
	public void setTabIcon(Icon icon) {
		if (icon instanceof ImageIcon) {
			ImageIcon imageIcon = MainFrame.resizeAndCacheTabIcon((ImageIcon) icon);
			this.icon = imageIcon;
			tabTitle.updateIcon(imageIcon);
			notifyTitleChanged();
		}
	}

	/**
	 *
	 * @return
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * Closes this tab, or if overlay hide it.
	 */
	public final void dispose() {
		if (!closeOuterOverlay()) {
			try {
				MainFrame.closeTab(MainFrameTab.this);
			} catch(CommonBusinessException e) {
				Errors.getInstance().showExceptionDialog(this, e);
			}
		}
	}

	/**
	 *
	 * @return
	 */
	public MainFrameTabbedPane getHomePane() {
		return Main.getInstance().getMainFrame().getHomePane();
	}

	/**
	 *
	 * @return
	 */
	public ImageIcon getTabIcon() {
		return this.icon;
	}

	/**
	 *
	 * @param sTitle
	 */
	public void setTitle(String sTitle) {
		this.title = sTitle;
		this.tabTitle.updateTitle(sTitle);
		setToolTip(sTitle);
		notifyTitleChanged();
	}

	/**
	 *
	 * @param sToolTip
	 */
	public void setToolTip(String sToolTip) {
		MainFrameTabbedPane tabbedPane = getTabbedPane();
		if (tabbedPane != null) {
			int index = getTabIndex();
			if (index > 0) {
				tabbedPane.setToolTipTextAt(index, sToolTip);
			}
		}
	}

	/**
	 *
	 * @return
	 */
	private int getTabIndex() {
		JTabbedPane tabPane = getTabbedPane();
		if (tabPane == null) return -1;

		for (int i = 0; i < tabPane.getTabCount() ; i++) {
			if (this == tabPane.getComponentAt(i)) {
				return i;
			}
		}

		return -1;
	}

	/**
	 *
	 * @return
	 */
	public MainFrameTabbedPane getTabbedPane() {
		Container parent = getParent();
		if (parent instanceof MainFrameTabbedPane) {
			MainFrameTabbedPane tabbedPane = (MainFrameTabbedPane) parent;
			return tabbedPane;
		}
		return null;
	}

	/**
	 *
	 */
	@Override
	public void transferSize(Dimension size) {
		if (layer != null) {
			layer.setBounds(0, 0, size.width, size.height);
		}

	}

	/**
	 *
	 */
	public void postAdd() {
		transferSize(getBounds().getSize());
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				revalidate();
			}
		});
	}

	/**
	 *
	 * @return
	 */
	public Component getContent() {
		if (layer != null) {
			return layer.getComponent(1);
		}
		return null;
	}

	/**
	 *
	 */
	@Override
	public boolean isClosable() {
		return _isClosable() && !isNeverClose();
	}

	/**
	 *
	 * @return
	 */
	private boolean _isClosable() {
		if (fromAssigned && !SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_ASSIGN)) {
			return false;
		}
		if (layer != null && getContent() instanceof IMainFrameTabClosableController) {
			return ((IMainFrameTabClosableController)getContent()).isClosable();
		}
		return true;
	}

	/**
	 *
	 */
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			final MainFrameTabbedPane tabPane = getTabbedPane();
			if (tabPane != null) {
				int iTab = getTabIndex();
				if (iTab > -1) {
					tabPane.setTabComponentAt(iTab, tabTitle);
					tabPane.setToolTipTextAt(iTab, tabTitle.getToolTipText());
					if (tabPane.getSelectedIndex() != iTab) {
						tabPane.setSelectedIndex(iTab);
					}
				}
			}
		}
	}

	/**
	 *
	 * @return
	 */
	public TabTitle getTabTitle() {
		return tabTitle;
	}

	public class TabTitle extends JLayeredPane{

		/**
		 * sum of: look & feel, insets, empty Component...
		 */
		public final static int TAB_WIDTH_CONSTRUCT = 23;

		/**
		 * inset of tab component
		 */
		public final static int MOUSE_POS_X_OFFSET = 3;

		private final JLabel lbTitle = new JLabel("");

		private final ImageIcon closeHoverIcon = Icons.getInstance().getIconTabCloseButton_hover();
		private final ImageIcon closeIcon = Icons.getInstance().getIconTabCloseButton();

		private Point mouseOverPosition;

		public TabTitle() {
			setOpaque(false);
			add(lbTitle, 1);

			// +20 = draw cut of "..." outside, so we can fade out the label...
			lbTitle.setBounds(0, 0, MainFrameTabbedPane.TAB_WIDTH_MAX+20, MainFrameTabbedPane.DEFAULT_TAB_COMPONENT_HEIGHT);
		}

		/**
		 *
		 * @param position
		 */
		public void setMouseOverPosition(Point position) {
			final Rectangle closeBounds = getCloseBoundsAbsolute();
			final boolean repaint =
				(this.mouseOverPosition == null && position != null) ||
				(this.mouseOverPosition != null && position == null) ||
				(this.mouseOverPosition != null && position != null && closeBounds.contains(this.mouseOverPosition) != closeBounds.contains(position));
			this.mouseOverPosition = position;
			if (repaint) {
				repaint();
			}
		}

		/**
		 *
		 * @param position
		 * @return boolean true if click is consumed
		 */
		public boolean mouseClicked(Point position, boolean left) {
			if (left && getCloseBoundsAbsolute().contains(position) && isClosable()) {
				try {
					MainFrame.closeTab(MainFrameTab.this, position);
				} catch(CommonBusinessException e) {
					Errors.getInstance().showExceptionDialog(this, e);
				}
				return true;
			} else if (!left) {
				showContextMenu();
				return true;
			}
			return false;
		}

		/**
		 *
		 */
		private void showContextMenu() {
			JPopupMenu popup = new JPopupMenu();

			popup.add(new JMenuItem(actMaximize));
			popup.add(new JMenuItem(actRestore));
			popup.addSeparator();
			popup.add(new JMenuItem(actClose));
			popup.add(new JMenuItem(actCloseAll));
			popup.add(new JMenuItem(actCloseOthers));
			popup.addSeparator();
			popup.add(new JCheckBoxMenuItem(actNeverClose));

			popup.show(TabTitle.this, 10, 10);
		}

		/**
		 *
		 * @return
		 */
		Rectangle getCloseBoundsAbsolute() {
			final Rectangle bounds = getBounds();
			final Rectangle result = new Rectangle(
				bounds.x+bounds.width-closeIcon.getIconWidth()+MOUSE_POS_X_OFFSET, 0, closeIcon.getIconWidth(), bounds.height+6);
			return result;
		}

		/**
		 *
		 * @param width
		 */
		void setWidth(int width) {
			setPreferredSize(new Dimension(width - TAB_WIDTH_CONSTRUCT, MainFrameTabbedPane.DEFAULT_TAB_COMPONENT_HEIGHT));
			revalidate();
			repaint();
		}

		/**
		 *
		 * @param icon
		 */
		void updateIcon(Icon icon) {
			lbTitle.setIcon(icon);
			repaint();
		}

		/**
		 *
		 * @param title
		 */
		void updateTitle(String title) {
			lbTitle.setText(title);
			repaint();
		}

		/**
		 *
		 * @param title
		 * @param icon
		 */
		void updateTitleAndIcon(String title, Icon icon) {
			lbTitle.setText(title);
			lbTitle.setIcon(icon);
			repaint();
		}

		/**
		 *
		 */
		@Override
		public String getToolTipText() {
			return lbTitle.getText();
		}

		@Override
		public void paint(Graphics g) {
			final Graphics2D g2 = (Graphics2D) g;

			final Rectangle bounds = getBounds();
			final BufferedImage bi = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
			//final BufferedImage biShadowDst = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
			final Graphics2D g2bi = bi.createGraphics();

//			if (mouseOverPosition != null) {
//				final BufferedImage biMouseOverBackground = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
//				final Graphics2D g2bimob = biMouseOverBackground.createGraphics();
//				g2bimob.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//				g2bimob.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//				g2bimob.setColor(new Color(255, 255, 255, 80));
//				g2bimob.fillRoundRect(0, 0, bounds.width, bounds.height, 8, 8);
//				g2bimob.fillRoundRect(1, 1, bounds.width-2, bounds.height-2, 8, 8);
//				g2bimob.fillRoundRect(2, 2, bounds.width-4, bounds.height-4, 8, 8);
//				g2bimob.fillRoundRect(3, 3, bounds.width-6, bounds.height-6, 8, 8);
//				g2bimob.dispose();
//				g2.drawRenderedImage(biMouseOverBackground, null);
//			}

			super.paint(g2bi);

			final int fade = 8;

			g2bi.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN));
			if (mouseOverPosition != null) {
				final boolean closable = isClosable();
				final int iconWidth = closable ? (closeIcon.getIconWidth()+2) : 0;
				g2bi.setPaint(new GradientPaint(bounds.width-iconWidth-fade, 0,
                    new Color(0.0f, 0.0f, 0.0f, 1.0f),
                    bounds.width-iconWidth, 0,
                    new Color(0.0f, 0.0f, 0.0f, 0.0f)));

				g2bi.fillRect(bounds.width-iconWidth-fade, 0, iconWidth+fade, bounds.height);

				/*GaussianBlurFilter gbf = new GaussianBlurFilter(4);
				gbf.filter(bi, biShadowDst);

				g2bi.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN));
				g2bi.setColor(Color.WHITE);
				g2bi.fillRect(0, 0,bounds.width, bounds.height);
				*/

				// draw close button
				if (closable) {
					final ImageIcon closeIconToDraw;
					if (getCloseBoundsAbsolute().contains(mouseOverPosition)) {
						closeIconToDraw = closeHoverIcon;
					} else {
						closeIconToDraw = closeIcon;
					}

					g2bi.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
					g2bi.drawImage(closeIconToDraw.getImage(), bounds.width-closeIconToDraw.getIconWidth(), 1, null);
				}
			} else {
				g2bi.setPaint(new GradientPaint(bounds.width-fade, 0,
	                new Color(0.0f, 0.0f, 0.0f, 1.0f),
	                bounds.width, 0,
	                new Color(0.0f, 0.0f, 0.0f, 0.0f)));

				g2bi.fillRect(bounds.width-fade, 0, fade, bounds.height);
			}

	        g2bi.dispose();

	        //g2.drawRenderedImage(biShadowDst, null);
	        //g2.drawRenderedImage(biShadowDst, null);
	        g2.drawRenderedImage(bi, null);
		}


	}

	/**
	 *
	 * @return true if close is send to listeners; false if no listener is registered
	 */
	private boolean closeOuterOverlay() {
		final List<IOverlayChangeListener> ocls = new ArrayList<IOverlayChangeListener>(overlayChangeListeners);
		for (IOverlayChangeListener tcl : ocls) {
			tcl.closeOverlay();
		}
		return ocls.size()>0;
	}

	/**
	 *
	 */
	private void notifyTitleChanged() {
		for (MainFrameTabListener listener : new ArrayList<MainFrameTabListener>(mainFrameTabListeners)) {
			listener.tabTitleChanged(this);
		}
		for (IOverlayChangeListener tcl : overlayChangeListeners) {
			tcl.titleChanged(title, icon);
		}
	}

	/**
	 *
	 */
	@Override
	public void addOverlayChangeListener(IOverlayChangeListener tcl) {
		overlayChangeListeners.add(tcl);
	}

	/**
	 *
	 */
	@Override
	public void removeOverlayChangeListener(IOverlayChangeListener tcl) {
		overlayChangeListeners.remove(tcl);
	}

	/**
	 *
	 * @return neverClos (user decision)
	 */
	public boolean isNeverClose() {
		return neverClose;
	}

	/**
	 *
	 * @param neverClose (is user decision)
	 */
	public void setNeverClose(boolean neverClose) {
		this.neverClose = neverClose;
	}

	/**
	 * 
	 * @return fromAssigned (is workspace decision)
	 */
	public boolean isFromAssigned() {
		return fromAssigned;
	}

	/**
	 * 
	 * @param fromAssigned (is workspace decision)
	 */
	public void setFromAssigned(boolean fromAssigned) {
		this.fromAssigned = fromAssigned;
	}

	/**
	 *
	 * @param parentComponent
	 * @return
	 */
	public static MainFrameTab getMainFrameTabForComponent(Component parentComponent) {
        if(parentComponent == null)
            return null;
        if(parentComponent instanceof MainFrameTab)
            return (MainFrameTab)parentComponent;
        return getMainFrameTabForComponent(parentComponent.getParent());
    }

	@Override
	public void visitDragEnter(DropTargetDragEvent dtde) {}

	@Override
	public void visitDragExit(DropTargetEvent dte) {}

	@Override
	public void visitDragOver(DropTargetDragEvent dtde) {
		MainFrame.setSelectedTab(MainFrameTab.this);
		dtde.rejectDrag();
	}

	@Override
	public void visitDrop(DropTargetDropEvent dtde) {}

	@Override
	public void visitDropActionChanged(DropTargetDragEvent dtde) {}

}  // class CommonJInternalFrame
