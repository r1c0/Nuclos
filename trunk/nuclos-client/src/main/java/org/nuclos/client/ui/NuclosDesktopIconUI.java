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
/*
 * Created on 24.09.2009
 */
package org.nuclos.client.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicDesktopIconUI;

import org.nuclos.client.main.mainframe.MainFrameTab;


public class NuclosDesktopIconUI extends BasicDesktopIconUI {

	private MouseListener mouseListener;

	public static ComponentUI createUI(JComponent c)    {
		return new NuclosDesktopIconUI();
	}

	JLabel titleLabel;
	JLabel iconLabel;
	Icon icon;
	Icon highlightIcon;

	/*
	 * (non-Javadoc)
	 * @see javax.swing.plaf.basic.BasicDesktopIconUI#installComponents()
	 */
	@Override
	protected void installComponents() {
		if(iconPane == null) {
			iconPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
			titleLabel = new JLabel("X"); // initiale Labelh\u00f6he!
			titleLabel.setForeground(Color.WHITE);
			titleLabel.setBorder(null);

			iconPane.setOpaque(false);
			desktopIcon.setOpaque(false);
		}

		desktopIcon.setLayout(new BorderLayout());

		final JInternalFrame ifrm = desktopIcon.getInternalFrame();
		iconLabel = new JLabel(icon = Icons.getInstance().getIconDesktopFolder());
		deriveIcon();
		iconPane.add(iconLabel);
		desktopIcon.add(iconPane, BorderLayout.NORTH);
		desktopIcon.add(titleLabel, BorderLayout.CENTER);
		desktopIcon.setBorder(null);

		ifrm.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if(evt.getPropertyName().equals(MainFrameTab.IMAGE_ICON_PROPERTY)) {
					Object v = evt.getNewValue();
					if(v != null && v instanceof Icon) {
						iconLabel.setIcon(icon = (Icon)v);
						deriveIcon();
					}
				}
				if((evt.getPropertyName().equals("title") || evt.getPropertyName().equals("icon"))
				&& desktopIcon != null && ifrm != null  && ifrm.getTitle() != null) {
					String title = ifrm.getTitle();
					titleLabel.setText(calculateStringLength(title));
					desktopIcon.setToolTipText(title);
				}
			}
		});

		ifrm.addInternalFrameListener(new InternalFrameAdapter() {
			/*
			 * (non-Javadoc)
			 * @see javax.swing.event.InternalFrameAdapter#internalFrameDeiconified(javax.swing.event.InternalFrameEvent)
			 */
			@Override
			public void internalFrameDeiconified(InternalFrameEvent e) {
				if(desktopIcon != null)
					iconLabel.setIcon(icon);
			}
			/*
			 * (non-Javadoc)
			 * @see javax.swing.event.InternalFrameAdapter#internalFrameIconified(javax.swing.event.InternalFrameEvent)
			 */
			@Override
			public void internalFrameIconified(InternalFrameEvent e) {
				if(desktopIcon == null || desktopIcon.getDesktopPane() == null)
					return;

				JDesktopPane desktop = desktopIcon.getDesktopPane();
				Rectangle parentBounds = desktop.getBounds();
				Dimension prefSize = desktopIcon.getPreferredSize();

				if(desktopIcon.getClientProperty("everIconified") != null) 
					return; // icon position has been set before, so don't change it.

				desktopIcon.putClientProperty("everIconified", Boolean.TRUE);

				Rectangle availableRectangle = null;
				JInternalFrame.JDesktopIcon currentIcon = null;

				int x = 0;
				int y = 0;
				int w = prefSize.width;
				int h = prefSize.height;

				boolean found = false;

				while(!found) {
					availableRectangle = new Rectangle(x, y, w, h);
					found = true;

					for(JInternalFrame frame : desktop.getAllFrames()) {
						currentIcon = frame.getDesktopIcon();
						if(!currentIcon.equals(desktopIcon) && (currentIcon.getClientProperty("everIconified") != null)) {
							if(availableRectangle.intersects(currentIcon.getBounds())) {
								found = false;
								break;
							}
						}
					}

					y += currentIcon.getBounds().height;
					if(y + h > parentBounds.height) {
						y = 0;
						x += w;
					}
				}

				desktopIcon.setLocation(availableRectangle.x, availableRectangle.y);
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.plaf.basic.BasicDesktopIconUI#uninstallComponents()
	 */
	@Override
	public void uninstallComponents() {
		if(iconPane != null)
			super.uninstallComponents();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.plaf.basic.BasicDesktopIconUI#installListeners()
	 */
	@Override
	protected void installListeners() {
		super.installListeners();
		mouseListener = new MouseAdapter() {
			/*
			 * (non-Javadoc)
			 * @see java.awt.event.MouseAdapter#mouseEntered(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseEntered(MouseEvent e) {
				iconLabel.setIcon(highlightIcon);
			}
			/*
			 * (non-Javadoc)
			 * @see java.awt.event.MouseAdapter#mouseExited(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseExited(MouseEvent e) {
				iconLabel.setIcon(icon);
			}
		};
		desktopIcon.addMouseListener(mouseListener);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.plaf.basic.BasicDesktopIconUI#uninstallListeners()
	 */
	@Override
	protected void uninstallListeners() {
		desktopIcon.removeMouseListener(mouseListener);
		mouseListener = null;
		super.uninstallListeners();
	}

	private String calculateStringLength(String s) {
		Font font = UIManager.getFont("TextField.font");
		FontMetrics fm = desktopIcon.getInternalFrame().getFontMetrics(font);

		if(fm.stringWidth(s) <= desktopIcon.getWidth())
			return s;

		while(fm.stringWidth(s + "...") > desktopIcon.getWidth())
			s = s.substring(0, s.length() - 1);

		return s + "...";
	}

	private void deriveIcon() {
		if(icon != null && icon instanceof ImageIcon) {
			ImageIcon ii = (ImageIcon)icon;

			BufferedImage img = UIUtils.toBufferedImage(ii.getImage());
			int w = img.getWidth(null);
			int h = img.getHeight(null);
			BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

			Graphics2D g2d = bi.createGraphics();
			g2d.drawImage(img, 0, 0, null);

			// RGBA
			RescaleOp rop = new RescaleOp(new float[] {1.2f, 1.2f, 1.2f, 1f}, new float[] {0f, 0f, 0f, 0f}, null);
			g2d.drawImage(bi, rop, 0, 0);

			highlightIcon = new ImageIcon(bi);
		}
	}
}
