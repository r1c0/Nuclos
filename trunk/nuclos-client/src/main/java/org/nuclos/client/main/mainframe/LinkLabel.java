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

import static org.nuclos.client.main.mainframe.MainFrameUtils.isActionSelected;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.nuclos.client.synthetica.NuclosThemeSettings;

public abstract class LinkLabel extends JLabel {
	private LinkMarker marker = LinkMarker.NONE;
	private boolean hover = false;
	private boolean selected = true;
	private boolean headline;
	private final Action action;

	public LinkLabel(final Action action) {
		this(action, false);
	}

	public LinkLabel(final Action action, final boolean isHeadline) {
		super(
			(String)action.getValue(Action.NAME),
			MainFrame.resizeAndCacheLinkIcon((Icon)action.getValue(Action.SMALL_ICON)),
			JLabel.LEFT);
		this.action = action;
		this.headline = isHeadline;
		setSelected(isActionSelected(action));
		setOpaque(false);
		setBackground(new Color(0,0,0,0));
		setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (action != null && action.isEnabled()) {
					if (SwingUtilities.isLeftMouseButton(e)) {
						setSelected(!selected);
						action.actionPerformed(new ActionEvent(LinkLabel.this, 0, (String)action.getValue(Action.ACTION_COMMAND_KEY)));
					} else if (SwingUtilities.isRightMouseButton(e)) {
						List<JMenuItem> cmis = getContextMenuItems();
						if (cmis != null && !cmis.isEmpty()) {
							JPopupMenu pm = new JPopupMenu();
							for (JMenuItem mi : cmis) {
								pm.add(mi);
							}
							pm.show(LinkLabel.this, 0, LinkLabel.this.getSize().height);
						}
					}
				}
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				hover = true;
				repaint();
			}
			@Override
			public void mouseExited(MouseEvent e) {
				hover = false;
				repaint();
			}
		});
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
		if (headline)
			setForeground(selected?Color.BLACK:Color.LIGHT_GRAY);
		setFont(getFont().deriveFont(headline&&selected?Font.BOLD:Font.PLAIN));
	}

	public boolean isSelected() {
		return selected;
	}

	/**
	 *
	 * @return
	 */
	protected abstract List<JMenuItem> getContextMenuItems();

	/**
	 *
	 * @param marker
	 */
	public void setMarker(LinkMarker marker) {
		this.marker = marker;
		repaint();
	}

	/**
	 *
	 * @return
	 */
	public LinkMarker getMarker() {
		return this.marker;
	}

	@Override
	public void paint(Graphics g) {
		if (action != null && action.isEnabled() && (hover || marker.isMarked())) {
			Graphics2D g2 = (Graphics2D) g;
			Object renderingHint = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			Rectangle bounds = getBounds();
			g2.setColor(hover ? (headline&&!selected?NuclosThemeSettings.BACKGROUND_COLOR3:NuclosThemeSettings.BACKGROUND_COLOR4) : marker.getColor());
			g2.fillRoundRect(0, 0, bounds.width, bounds.height, 4, 4);

			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, renderingHint);
		}

		super.paint(g);
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension res = super.getPreferredSize();
		if (!isVisible()) {
			res.height = 0;
		}
		return res;
	}
	
	

}

