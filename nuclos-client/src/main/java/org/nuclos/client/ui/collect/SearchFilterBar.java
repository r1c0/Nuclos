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
package org.nuclos.client.ui.collect;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.ItemSelectable;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.nuclos.client.common.EnabledListener;
import org.nuclos.client.resource.NuclosResourceCache;
import org.nuclos.client.resource.ResourceCache;
import org.nuclos.client.searchfilter.SearchFilter;
import org.nuclos.client.searchfilter.SearchFilters;
import org.nuclos.client.synthetica.NuclosThemeSettings;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.PreferencesException;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class SearchFilterBar implements ItemSelectable {
	
	private static final Logger LOG = Logger.getLogger(SearchFilterBar.class);
	
	private Collection<ItemListener> itemListener;
	
	private Collection<EnabledListener> enabledListener;

	private Panel pnl;
	
	private JScrollPane scroll;
	
	private SearchFilter selected;
	
	@PostConstruct
	void init() {
		pnl = new Panel();
		scroll = new JScrollPane(pnl, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED) {
			@Override
			public Dimension getPreferredSize() {
				final Dimension result = pnl.getPreferredSize();
				result.height = result.height + 16; //ScrollBar.thumbHeight
				return result;
			}
		};
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.setVisible(false);
		itemListener = new ArrayList<ItemListener>();
		enabledListener = new ArrayList<EnabledListener>();
	}
	
	public JComponent getJComponent() {
		return scroll;
	}
	
	public void setSelected(SearchFilter selected) {
		boolean revalidate = !LangUtils.equals(this.selected, selected);
		this.selected = selected;
		
		if (revalidate) {
			fireItemStateChanged();
		}
	}
	
	public SearchFilter getSelected() {
		return this.selected;
	}
	
	public void setSearchFilters(SearchFilters sfs) {
		pnl.removeAll();
		try {
			if (sfs == null || sfs.getAll().isEmpty()) {
				if (scroll.isVisible()) {
					scroll.setVisible(false);
					fireEnabledChanged(false);
				}
			} else {
				
				List<SearchFilter> fastSelect = new ArrayList<SearchFilter>();
				for (SearchFilter sf : sfs.getAll()) {
					if (Boolean.TRUE.equals(sf.getSearchFilterVO().getFastSelectInResult())) {
						fastSelect.add(sf);
					}
				}
				
				if (fastSelect.isEmpty()) {
					if (scroll.isVisible()) {
						scroll.setVisible(false);
						fireEnabledChanged(false);
					}
				} else {
					
//					Collections.sort(fastSelect, new Comparator<SearchFilter>() {
//						@Override
//						public int compare(SearchFilter o1, SearchFilter o2) {
//							return LangUtils.compare(o1.getSearchFilterVO().getOrder(), o2.getSearchFilterVO().getOrder());
//						}});
					
					for (SearchFilter sf : fastSelect) {
						pnl.add(new SearchFilterButton(sf));
					}
					
					if (!scroll.isVisible()) {
						scroll.setVisible(true);
						fireEnabledChanged(true);
					}
				}
			}
		} catch (PreferencesException e) {
			LOG.error(e.getMessage(), e);
		}
	}
	
	public void addItemListener(ItemListener il) {
		this.itemListener.add(il);
	}
	
	public void removeItemListener(ItemListener il) {
		this.itemListener.remove(il);
	}
	
	private void fireItemStateChanged() {
		ItemEvent ie = new ItemEvent(this, (getSelected()!=null?getSelected().getId():-1), getSelected(), ItemEvent.ITEM_STATE_CHANGED);
		for (ItemListener il : itemListener) {
			il.itemStateChanged(ie);
		}
	}
	
	public void addEnabledListener(EnabledListener el) {
		this.enabledListener.add(el);
	}
	
	public void removeEnabledListener(EnabledListener el) {
		this.enabledListener.remove(el);
	}
	
	private void fireEnabledChanged(boolean enabled) {
		for (EnabledListener el : enabledListener) {
			el.enabledChanged(enabled);
		}
	}
	
	private class Panel extends JPanel {
		
		private static final long serialVersionUID = 1L;

		public Panel() {
			super(new FlowLayout(FlowLayout.CENTER,0 ,0));
		}
	}
	
	private class SearchFilterButton extends JLabel implements MouseListener {
		
		private final Color mouseOverBackground = new Color(
				NuclosThemeSettings.BACKGROUND_ROOTPANE.getRed(),
				NuclosThemeSettings.BACKGROUND_ROOTPANE.getGreen(),
				NuclosThemeSettings.BACKGROUND_ROOTPANE.getBlue(),
				150);
		
		private final Color mouseOverForeground = Color.WHITE;
		
		private final Color defaultForeground;
		
		private final SearchFilter sf;
		
		private ImageIcon icon;
		
		private int iconWidth;
		
		private boolean mouseOver = false;

		public SearchFilterButton(SearchFilter sf) {
			super(SpringLocaleDelegate.getInstance().getTextFallback(sf.getLabelResourceId(), sf.getName()));
			this.sf = sf;
			this.defaultForeground = getForeground();
			
			setVerticalTextPosition(JLabel.BOTTOM);
			setHorizontalTextPosition(JLabel.CENTER);
			setBorder(BorderFactory.createEmptyBorder());
			setIcon();
			
			addMouseListener(this);
		}
		
		private void setIcon() {
			Integer iResourceIconId = sf.getSearchFilterVO().getFastSelectIconId();
			String sNuclosResourceIcon = sf.getSearchFilterVO().getFastSelectNuclosIcon();
			
			if (iResourceIconId != null) {
				try {
					icon = ResourceCache.getInstance().getIconResource(iResourceIconId);
				} catch (Exception ex) {
					LOG.error(String.format("ResourceIcon not found (ID=%s)", iResourceIconId), ex);
				}
			}
			if (icon == null && sNuclosResourceIcon != null) {
				try {
					icon = NuclosResourceCache.getNuclosResourceIcon(sNuclosResourceIcon);
				} catch (Exception ex) {
					LOG.error(String.format("NuclosResourceIcon not found (ID=%s)", sNuclosResourceIcon), ex);
				}
			}
			
			if (icon != null) {
				setIcon(icon);
				iconWidth = icon.getIconWidth();
			}
		}

		@Override
		public Dimension getPreferredSize() {
			final Dimension result = super.getPreferredSize();
			if (icon != null) {
				result.width = iconWidth;
			}
			return result;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (SwingUtilities.isLeftMouseButton(e)) {
				selected = sf;
				fireItemStateChanged();
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			mouseOver = true;
			setForeground(mouseOverForeground);
			repaint();
		}

		@Override
		public void mouseExited(MouseEvent e) {
			mouseOver = false;
			setForeground(defaultForeground);
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			Object renderingHint = g2
					.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

			Rectangle bounds = getBounds();
			
			if (mouseOver) {
				g2.setColor(mouseOverBackground);
				g2.fillRoundRect(0, 0, bounds.width, bounds.height, 4, 4);
			}
			
			if (!mouseOver && sf != selected) {
				final BufferedImage bi = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
				final Graphics gbi = bi.getGraphics();
				super.paintComponent(gbi);
				gbi.dispose();
				
				// 20% opaque
				float[] scales = { 1f, 1f, 1f, 0.2f };
				float[] offsets = new float[4];
				RescaleOp rop = new RescaleOp(scales, offsets, null);
				
				g2.drawImage(bi, rop, 0, 0);
			} else {
				super.paintComponent(g2);
			}
			
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					renderingHint);
		}
		
		
	}

	@Override
	public Object[] getSelectedObjects() {
		return new SearchFilter[] {selected};
	}
	
}
