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

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.ItemSelectable;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import org.nuclos.client.ui.util.TableLayoutBuilder;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.PreferencesException;

public class SearchFilterBar implements ItemSelectable {
	
	private static final Logger LOG = Logger.getLogger(SearchFilterBar.class);
	
	private static final int BORDER_BOTTOM = 5;
	
	private Collection<ItemListener> itemListener;
	
	private Collection<EnabledListener> enabledListener;

	private Panel pnl;
	
	private JScrollPane scroll;
	
	private SearchFilter selected;
	
	public SearchFilterBar() {
		init();
	}
	
	void init() {
		pnl = new Panel();
		scroll = new JScrollPane(pnl, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED) {
			@Override
			public Dimension getPreferredSize() {
				final Dimension result = pnl.getPreferredSize();
				result.height = result.height +BORDER_BOTTOM + 10 + 16; //ScrollBar.thumbHeight
				return result;
			}
		};
		scroll.setBorder(BorderFactory.createEmptyBorder(0,0,BORDER_BOTTOM,0));
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
					
					double[] cols = new double[fastSelect.size()+2];
					cols[0] = TableLayout.FILL;
					for (int i = 0; i < fastSelect.size(); i++)
						cols[i+1] = TableLayout.PREFERRED;
					cols[fastSelect.size()+1] = TableLayout.FILL;
					TableLayoutBuilder tbllay = new TableLayoutBuilder(pnl).columns(cols);
					tbllay.newRow(TableLayout.FILL).skip();
					
					for (SearchFilter sf : fastSelect) {
						final SearchFilterButton sfb = new SearchFilterButton(sf);
						tbllay.add(sfb);
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
		
		private final Color defaultBackground = new Color(
				NuclosThemeSettings.BACKGROUND_COLOR3.getRed(),
				NuclosThemeSettings.BACKGROUND_COLOR3.getGreen(),
				NuclosThemeSettings.BACKGROUND_COLOR3.getBlue(), 200);
		
		private static final long serialVersionUID = 1L;

		public Panel() {
			super();
			setOpaque(false);
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			Object antialiasing = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
	        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	     
	        Rectangle bounds = getBounds();
	        
	        if (bounds.width>0 && bounds.height>22) {
		        g2.setPaint(new GradientPaint(
		        		0, 0, NuclosThemeSettings.BACKGROUND_ROOTPANE,
						0, 4, defaultBackground, false));
		        g2.fillRect(0, 0, bounds.width, bounds.height-20);
		        g2.setPaint(new GradientPaint(
		        		0, bounds.height-3, defaultBackground, 
						0, bounds.height, NuclosThemeSettings.BACKGROUND_ROOTPANE, false));
		        g2.fillRect(0, bounds.height-20, bounds.width, 20);
	        }
	        super.paintComponent(g2);
	        
	        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialiasing);
		}
			
	}
	
	private class SearchFilterButton extends JLabel implements MouseListener {
		
		private final Color mouseOverBackground = NuclosThemeSettings.BACKGROUND_COLOR3;
		
		private final Color mouseOverForeground = Color.WHITE;
		
		private final Color defaultForeground;
		
		private final SearchFilter sf;
		
		private ImageIcon icon;
		
		private int iconWidth;
		
		private boolean mouseOver = false;

		public SearchFilterButton(SearchFilter sf) {
			super(SpringLocaleDelegate.getInstance().getTextFallback(sf.getLabelResourceId(), sf.getName()));
			this.sf = sf;
			this.defaultForeground = Color.WHITE;//getForeground();
			
			setForeground(this.defaultForeground);
			setVerticalTextPosition(JLabel.BOTTOM);
			setHorizontalTextPosition(JLabel.CENTER);
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
			} else {
				setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
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
				mouseOver = false;
				fireItemStateChanged();
				pnl.repaint();
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
				if (sf != selected) {
					g2.setColor(mouseOverBackground);
					g2.fillRoundRect(0, 5, bounds.width, bounds.height-8, 4, 4);
				}
			}
			
			super.paintComponent(g2);
			
//			if (mouseOver) {
//				if (sf == selected) {
//					if (bounds.width > 30 && bounds.height > 30) {
//						final BufferedImage bi = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
//						final Graphics2D gbi = (Graphics2D) bi.getGraphics();
//						gbi.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//								RenderingHints.VALUE_ANTIALIAS_ON);
//						
//						// draw circle
//						final int x;
//						final int y;
//						final int w;
//						if (bounds.width < bounds.height) {
//							w = bounds.width-10;
//							x = 5;
//							y = (bounds.height - w) / 2;
//						} else {
//							w = bounds.height-10;
//							x = (bounds.width - w) / 2;
//							y = 5;
//						}
//						
//						gbi.setColor(mouseOverBackground);
//						gbi.fillOval(x, y, w, w);
//						
//						// draw X
//						final int thicknessX = w-w*8/10;
//						final int insetsX = 7;
//						final int longX = w-insetsX*2;
//						final int xX1 = x+w/2-thicknessX/2;
//						final int yX1 = y+insetsX;
//						final int xX2 = x+insetsX;
//						final int yX2 = y+w/2-thicknessX/2;
//						
//						gbi.rotate(Math.toRadians(45), bounds.getWidth()/2, bounds.getHeight()/2);
//						
//						gbi.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OUT));
//						gbi.setPaint(new Color(0, 0, 0, 255));
//						gbi.fillRect(xX1, yX1, thicknessX, longX);
//						gbi.fillRect(xX2, yX2, longX, thicknessX);
//						
//						gbi.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
//						gbi.setPaint(new Color(255, 255, 255, 70));
//						gbi.fillRect(xX1, yX1, thicknessX, longX);
//						gbi.fillRect(xX2, yX2, longX, thicknessX);
//						
//						gbi.dispose();
//						
//						// 90% opaque
//						float[] scales = { 1f, 1f, 1f, 0.9f };
//						float[] offsets = new float[4];
//						RescaleOp rop = new RescaleOp(scales, offsets, null);
//						
//						g2.drawImage(bi, rop, 0, 0);
//					}
//				}
//			}
				
			if (sf == selected) {
//				g2.setColor(mouseOver?NuclosThemeSettings.BACKGROUND_ROOTPANE:NuclosThemeSettings.BACKGROUND_PANEL);
				g2.setColor(NuclosThemeSettings.BACKGROUND_PANEL);
				final int w = 24;
				final int h = 12;
				int x = bounds.width/2-w/2;
				int y = bounds.height;
				Polygon p = new Polygon();
				
				p.addPoint(x, 		y);
				p.addPoint(x+w/2, 	y-h);
				p.addPoint(x+w, 	y);
				
				
				g2.fillPolygon(p);
				
				x = bounds.width/2-w/2;
				y = bounds.height;
				p = new Polygon();
				
				p.addPoint(x, 		0);
				p.addPoint(x+w/2, 	h);
				p.addPoint(x+w, 	0);
				g2.fillPolygon(p);
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
