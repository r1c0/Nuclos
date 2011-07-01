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
package org.nuclos.client.ui.collect.indicator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.nuclos.client.synthetica.NuclosSyntheticaConstants;
import org.nuclos.client.ui.collect.CollectPanel;
import org.nuclos.common2.CommonLocaleDelegate;

public class CollectPanelIndicator {
	
	private final static Color colorMouseOver = new Color(212, 212, 212);
	private final static Color colorDeactivated = NuclosSyntheticaConstants.BACKGROUND_DARKER;
	
	private final static ImageIcon imgArrowActiveDeactivated = getImageIcon("org/nuclos/client/ui/collect/indicator/images/Arrow-active-deactivated.png");
	private final static ImageIcon imgArrowActiveNormal = getImageIcon("org/nuclos/client/ui/collect/indicator/images/Arrow-active-normal.png");
	private final static ImageIcon imgArrowDeactivatedActive = getImageIcon("org/nuclos/client/ui/collect/indicator/images/Arrow-deactivated-active.png");
	private final static ImageIcon imgArrowDeactivatedDeactivated = getImageIcon("org/nuclos/client/ui/collect/indicator/images/Arrow-deactivated-deactivated.png");
	private final static ImageIcon imgArrowDeactivatedNormal = getImageIcon("org/nuclos/client/ui/collect/indicator/images/Arrow-deactivated-normal.png");
	private final static ImageIcon imgArrowNormalActive = getImageIcon("org/nuclos/client/ui/collect/indicator/images/Arrow-normal-active.png");
	private final static ImageIcon imgArrowNormalDeactivated = getImageIcon("org/nuclos/client/ui/collect/indicator/images/Arrow-normal-deactivated.png");
	private final static ImageIcon imgArrowNormalNormal = getImageIcon("org/nuclos/client/ui/collect/indicator/images/Arrow-normal-normal.png");
	private final static ImageIcon imgBackgroundActive = getImageIcon("org/nuclos/client/ui/collect/indicator/images/Background-active.png");
	private final static ImageIcon imgBackgroundDeactivated = getImageIcon("org/nuclos/client/ui/collect/indicator/images/Background-deactivated.png");
	private final static ImageIcon imgBackgroundNormal = getImageIcon("org/nuclos/client/ui/collect/indicator/images/Background-normal.png");
	private final static ImageIcon imgLeftActive = getImageIcon("org/nuclos/client/ui/collect/indicator/images/Left-active.png");
	private final static ImageIcon imgLeftDeactivated = getImageIcon("org/nuclos/client/ui/collect/indicator/images/Left-deactivated.png");
	private final static ImageIcon imgLeftNormal = getImageIcon("org/nuclos/client/ui/collect/indicator/images/Left-normal.png");
	private final static ImageIcon imgRightActive = getImageIcon("org/nuclos/client/ui/collect/indicator/images/Right-active.png");
	private final static ImageIcon imgRightDeactivated = getImageIcon("org/nuclos/client/ui/collect/indicator/images/Right-deactivated.png");
	private final static ImageIcon imgRightNormal = getImageIcon("org/nuclos/client/ui/collect/indicator/images/Right-normal.png");
	
	private final JPanel jpButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
	
	private final IndicatorLabel lbSearch;
	private final IndicatorLabel lbResult;
	private final IndicatorLabel lbDetails;
	
	private final int tab;
	
	private final Set<SelectionListener> selectionListener = new HashSet<SelectionListener>(1);

	public CollectPanelIndicator(int tab) {		
		this.tab = tab;
		
		lbSearch = createLabel(CollectPanel.TAB_SEARCH);
		lbResult = createLabel(CollectPanel.TAB_RESULT);
		lbDetails = createLabel(CollectPanel.TAB_DETAILS);
		
		jpButtons.setOpaque(false);
		jpButtons.add(lbSearch);
		jpButtons.add(lbResult);
		jpButtons.add(lbDetails);
	}
	
	public JPanel getJPanel() {
		return jpButtons;
	}
	
	public void hideSearchOption() {
		lbSearch.setVisible(false);
	}
	
	public void setToolTip(int tab, String toolTip) {
		switch (tab) {
		case CollectPanel.TAB_SEARCH:
			lbSearch.setToolTipText(toolTip);
			break;
		case CollectPanel.TAB_RESULT:
			lbResult.setToolTipText(toolTip);
			break;
		case CollectPanel.TAB_DETAILS: 
			lbDetails.setToolTipText(toolTip);
			break;
		}
	}
	
	public void updateOptions(Map<Integer, Boolean> options) {
		for (Integer tab : options.keySet()) {
			updateOption(tab, options.get(tab), false);
		}
		lbSearch.repaint();
		lbResult.repaint();
		lbDetails.repaint();
	}
	
	public void updateOption(int tab, boolean avaiable) {
		updateOption(tab, avaiable, true);
	}
	
	public void updateOption(int tab, boolean avaiable, boolean repaintAll) {
		switch (tab) {
		case CollectPanel.TAB_SEARCH:
			lbSearch.setEnabled(avaiable);
			if (repaintAll ) {
				lbResult.repaint();
				lbDetails.repaint();
			}
			break;
		case CollectPanel.TAB_RESULT:
			lbResult.setEnabled(avaiable);
			if (repaintAll ) {
				lbSearch.repaint();
				lbDetails.repaint();
			}
			break;
		case CollectPanel.TAB_DETAILS: 
			lbDetails.setEnabled(avaiable);
			if (repaintAll ) {
				lbSearch.repaint();
				lbResult.repaint();
			}
			break;
		default:
			throw new IllegalArgumentException();	
		}
	}
	
	public void addSelectionListener(SelectionListener sel) {
		this.selectionListener.add(sel);
	}
	
	public void removeSelectionListener(SelectionListener sel) {
		this.selectionListener.remove(sel);
	}
	
	private IndicatorLabel createLabel(final int selectedTab) {
		IndicatorLabel result = new IndicatorLabel(selectedTab);
		
		result.setForeground(Color.WHITE);
		result.setOpaque(false);
		result.setFocusable(false);
		result.addMouseListener(new IndicatorMouseListener(selectedTab));
		
		return result;
	}
	
	protected static String getSearchLabel() {
		return CommonLocaleDelegate.getMessage("CollectPanel.5","Suche");
	}
	
	protected static String getResultLabel() {
		return CommonLocaleDelegate.getMessage("CollectPanel.3","Liste");
	}
	
	protected static String getDetailsLabel() {
		return CommonLocaleDelegate.getMessage("CollectPanel.1","Details");
	}
	
	public abstract static class SelectionListener {
		public abstract void selectionPerformed(int currentTab, int selectedTab);
	}
	
	private class IndicatorLabel extends JLabel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		final int presentation;
		
		boolean active = true;
		
		public IndicatorLabel(final int presentation) {
			this.presentation = presentation;
			
			setActive(true);
			switch (presentation) {
				case (CollectPanel.TAB_SEARCH) :
					this.setText(getSearchLabel());
					break;
				case (CollectPanel.TAB_RESULT) :
					this.setText(getResultLabel());
					break;
				case (CollectPanel.TAB_DETAILS) :
					this.setText(getDetailsLabel());
					break;
			}
		}

		@Override
		public void setEnabled(boolean enabled) {
			setActive(enabled);
		}
		
		private void setActive(boolean active) {
			this.active = active;
			if (tab==presentation)
				setForeground(Color.WHITE);
			else
				if (active)
					setForeground(Color.WHITE);
				else
					setForeground(colorDeactivated);
		}

		@Override
		public boolean isEnabled() {
			return this.active;
		}

		@Override
		public Dimension getPreferredSize() {
			Dimension result = super.getPreferredSize();
			result.height = imgLeftActive.getIconHeight();
			switch (presentation) {
				case (CollectPanel.TAB_SEARCH) :
					result.width = result.width + imgArrowActiveDeactivated.getIconWidth() + imgLeftActive.getIconWidth();
					break;
				case (CollectPanel.TAB_RESULT) :
					result.width = result.width + imgArrowActiveDeactivated.getIconWidth() + (lbSearch.isVisible()?0:imgLeftActive.getIconWidth());
					break;
				case (CollectPanel.TAB_DETAILS) :
					result.width = result.width + imgRightActive.getIconWidth();
					break;
			}
			return result;
		}
		
		@Override
		public void paintComponent(Graphics g) {
			BufferedImage superBI = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics superPC = superBI.createGraphics();
			super.paintComponent(superPC);
			
			Graphics2D g2 = ((Graphics2D)g);
			
			ImageIcon leftImage = null;
			ImageIcon backgroundImage = getBackgroundImage(this);
			ImageIcon rightImage = null;
			
			switch (presentation) {
				case (CollectPanel.TAB_SEARCH) :
					leftImage = getLeftImage(lbSearch);
					break;
				case (CollectPanel.TAB_RESULT) :
					if (!lbSearch.isVisible()) leftImage = getLeftImage(lbResult);
					break;
			}
			if (leftImage != null)
				g2.drawImage(leftImage.getImage(), 0, 0, null);
			
			final int leftWidth = leftImage == null ? 0 : leftImage.getIconWidth();
			final int textWidth = super.getPreferredSize().width;
			for (int i = 0; i < textWidth; i++) {
				g2.drawImage(backgroundImage.getImage(), leftWidth + i, 0, null);
			}
			
			switch (presentation) {
				case (CollectPanel.TAB_SEARCH) :
					rightImage = getRightArrowImage(lbSearch, lbResult);
					break;
				case (CollectPanel.TAB_RESULT) :
					rightImage = getRightArrowImage(lbResult, lbDetails);
					break;
				case (CollectPanel.TAB_DETAILS) :
					rightImage = getRightImage(lbDetails);
					break;
			}
			if (rightImage != null)
				g2.drawImage(rightImage.getImage(), leftWidth + textWidth, 0, null);
			
			g2.drawImage(superBI, leftWidth, 0, null);
		}
	
		private ImageIcon getLeftArrowImage(IndicatorLabel lbLeft, IndicatorLabel label) {
			final boolean leftVisible = lbLeft.isVisible();
			final boolean leftActive = tab==lbLeft.presentation;
			final boolean leftEnabled = lbLeft.isEnabled();
			final boolean isActive = tab==label.presentation;
			final boolean isEnabled = label.isEnabled();
			
			if (leftVisible)
				if (isActive)
					return leftEnabled ? imgArrowNormalActive : imgArrowDeactivatedActive;
				else
					if (leftEnabled)
						if (isEnabled)
							return leftActive ? imgArrowActiveNormal : imgArrowNormalNormal;
						else
							return leftActive ? imgArrowActiveDeactivated : imgArrowNormalDeactivated;
					else
						return isEnabled ? imgArrowDeactivatedNormal : imgArrowDeactivatedDeactivated;
			else
				return getLeftImage(label);
		}
		
		private ImageIcon getLeftImage(IndicatorLabel label) {
			final boolean isActive = tab==label.presentation;
			final boolean isEnabled = label.isEnabled();
			return isActive ? imgLeftActive : (isEnabled ? imgLeftNormal : imgLeftDeactivated);
		}
		
		private ImageIcon getRightArrowImage(IndicatorLabel label, IndicatorLabel lbRight) {
			final boolean rightActive = tab==lbRight.presentation;
			final boolean rightEnabled = lbRight.isEnabled();
			final boolean isActive = tab==label.presentation;
			final boolean isEnabled = label.isEnabled();
			
			if (isActive)
				return rightEnabled ? imgArrowActiveNormal : imgArrowActiveDeactivated;
			else
				if (rightEnabled)
					if (isEnabled)
						return rightActive ? imgArrowNormalActive : imgArrowNormalNormal;
					else
						return rightActive ? imgArrowDeactivatedActive : imgArrowDeactivatedNormal;
				else
					return isEnabled ? imgArrowNormalDeactivated : imgArrowDeactivatedDeactivated;
		}
		
		private ImageIcon getRightImage(IndicatorLabel label) {
			final boolean isActive = tab==label.presentation;
			final boolean isEnabled = label.isEnabled();
			return isActive ? imgRightActive : (isEnabled ? imgRightNormal : imgRightDeactivated);
		}
		
		private ImageIcon getBackgroundImage(IndicatorLabel label) {
			final boolean isActive = tab==label.presentation;
			final boolean isEnabled = label.isEnabled();
			return isActive ? imgBackgroundActive : (isEnabled ? imgBackgroundNormal : imgBackgroundDeactivated);
		}
	}
	
	private class IndicatorMouseListener extends MouseAdapter {
		
		private final int selectedTab;
		
		public IndicatorMouseListener(int selectedTab) {
			this.selectedTab = selectedTab;
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			switch (selectedTab) {
				case (CollectPanel.TAB_SEARCH) :
					if (!lbSearch.isEnabled()) return;
					break;
				case (CollectPanel.TAB_RESULT) :
					if (!lbResult.isEnabled()) return;
					break;
				case (CollectPanel.TAB_DETAILS) :
					if (!lbDetails.isEnabled()) return;
					break;
			}
			
			for (SelectionListener sel : selectionListener)
				sel.selectionPerformed(tab, selectedTab);
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			switch (selectedTab) {
				case (CollectPanel.TAB_SEARCH) :
					if (lbSearch.isEnabled() && !(tab==selectedTab)) lbSearch.setForeground(colorMouseOver);
					break;
				case (CollectPanel.TAB_RESULT) :
					if (lbResult.isEnabled() && !(tab==selectedTab)) lbResult.setForeground(colorMouseOver);
					break;
				case (CollectPanel.TAB_DETAILS) :
					if (lbDetails.isEnabled() && !(tab==selectedTab)) lbDetails.setForeground(colorMouseOver);
					break;
			}
		}

		@Override
		public void mouseExited(MouseEvent e) {
			switch (selectedTab) {
				case (CollectPanel.TAB_SEARCH) :
					if (lbSearch.isEnabled() && !(tab==selectedTab)) lbSearch.setForeground(Color.WHITE);
					break;
				case (CollectPanel.TAB_RESULT) :
					if (lbResult.isEnabled() && !(tab==selectedTab)) lbResult.setForeground(Color.WHITE);
					break;
				case (CollectPanel.TAB_DETAILS) :
					if (lbDetails.isEnabled() && !(tab==selectedTab)) lbDetails.setForeground(Color.WHITE);
					break;
			}
		}
	};
	
	private static ImageIcon getImageIcon(String sFileName) {
		return new ImageIcon(CollectPanelIndicator.class.getClassLoader().getResource(sFileName));
	}
 	
}
