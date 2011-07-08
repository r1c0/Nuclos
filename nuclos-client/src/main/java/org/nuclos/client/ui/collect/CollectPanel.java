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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.nuclos.client.ui.collect.indicator.CollectPanelIndicator;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common2.CommonLocaleDelegate;


/**
 * A panel for collecting data. Contains a tabbed pane with three tabs:
 * Search, Result and Details.
 * <br>
 * <b>Responsibilities:</b>
 * <ul>
 *   <li>Defines the triad of Search, Result and Details (as tabs).</li>
 *   <li>Contains a panel for each tab: a SearchPanel, a ResultPanel and a DetailsPanel.</li>
 *   <li>Allows omitting the search panel.</li>
 *   <li>@todo Should contain the outer CollectState as its model. The view is a reflection of the CollectState!
 * </ul>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 * @todo refactor tabbed pane methods
 */
public class CollectPanel<Clct extends Collectable> extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * external index of the Search tab
	 */
	public static final int TAB_SEARCH = 0;

	/**
	 * external index of the Result tab
	 */
	public static final int TAB_RESULT = 1;

	/**
	 * external index of the Details tab
	 */
	public static final int TAB_DETAILS = 2;

	//private final JTabbedPane tabpn = new JTabbedPane();
	
	private final JLayeredPane layer = new JLayeredPane();
	
	private final Map<Integer, Boolean> optionsEnabled = new HashMap<Integer, Boolean>();
	
	private final Set<ChangeListener> chgListeners = new HashSet<ChangeListener>();

	private final SearchPanel pnlSearch;

	private final ResultPanel<Clct> pnlResult;

	private final DetailsPanel pnlDetails;

	private final boolean bContainsSearchPanel;
	
	private final CollectPanelIndicator.SelectionListener selectionListener = new CollectPanelIndicator.SelectionListener() {
		@Override
		public void selectionPerformed(int currentTab, int selectedTab) {
			CollectPanel.this.setTabbedPaneSelectedIndex(selectedTab);
			for (ChangeListener chgListener : chgListeners) {
				chgListener.stateChanged(new ChangeEvent(layer));
			}
		}
	};

	/**
	 * creates a CollectPanel consisting of a <code>SearchPanel</code> (if <code>bSearchPanelAvailable</code>),
	 * a <code>ResultPanel</code> and a <code>DetailsPanel</code>. These subpanels are created by calling the respective
	 * creation methods.
	 * @param bSearchPanelAvailable
	 * @postcondition this.containsSearchPanel() == bSearchPanelAvailable
	 * @see #newSearchPanel()
	 * @see #newResultPanel()
	 * @see #newDetailsPanel()
	 */
	public CollectPanel(boolean bSearchPanelAvailable) {
		super(new BorderLayout(0,0));

		this.bContainsSearchPanel = bSearchPanelAvailable;

		// Note that the search panel is always created, even if it isn't visible.
		// @todo That is for compatibility reasons, but shouldn't be.
		this.pnlSearch = newSearchPanel();

		this.pnlResult = newResultPanel();

		this.pnlDetails = newDetailsPanel();
		
		optionsEnabled.put(0, true);
		optionsEnabled.put(1, true);
		optionsEnabled.put(2, true);
		
		if (!bSearchPanelAvailable) {
			this.pnlSearch.cpi.hideSearchOption();
			this.pnlResult.cpi.hideSearchOption();
			this.pnlDetails.cpi.hideSearchOption();
		}
		
		this.pnlSearch.cpi.addSelectionListener(selectionListener);
		this.pnlResult.cpi.addSelectionListener(selectionListener);
		this.pnlDetails.cpi.addSelectionListener(selectionListener);
		
		this.setupLayout();
		
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				pnlSearch.setBounds(0, 0, e.getComponent().getWidth(), e.getComponent().getHeight());
				pnlResult.setBounds(0, 0, e.getComponent().getWidth(), e.getComponent().getHeight());
				pnlDetails.setBounds(0, 0, e.getComponent().getWidth(), e.getComponent().getHeight());
			}
		});

		assert this.containsSearchPanel() == bSearchPanelAvailable;
	}

	private void setupLayout() {
		this.setBackground(Color.WHITE);
		//this.add(tabpn, BorderLayout.CENTER);
		
		this.add(layer, BorderLayout.CENTER);
		
		pnlSearch.setVisible(bContainsSearchPanel);
		pnlResult.setVisible(!bContainsSearchPanel);
		pnlDetails.setVisible(false);
		
		pnlSearch.setLocation(0, 0);
		pnlResult.setLocation(0, 0);
		pnlDetails.setLocation(0, 0);
		
		layer.add(pnlSearch, new Integer(0));
		layer.add(pnlResult, new Integer(0));
		layer.add(pnlDetails, new Integer(0));

		this.pnlSearch.setName("pnlSearch");
		this.pnlResult.setName("pnlResult");
		this.pnlDetails.setName("pnlDetails");
		
		setTabbedPaneToolTipTextAt(TAB_SEARCH, CommonLocaleDelegate.getMessage("CollectPanel.6","Suche (F3)"));
		setTabbedPaneToolTipTextAt(TAB_RESULT, CommonLocaleDelegate.getMessage("CollectPanel.4","Ergebnis (F4)"));
		setTabbedPaneToolTipTextAt(TAB_DETAILS, CommonLocaleDelegate.getMessage("CollectPanel.2","Details (F5)"));

		/*
		if (this.containsSearchPanel()) {
			this.tabpn.addTab(CommonLocaleDelegate.getMessage("CollectPanel.5","Suche"), this.pnlSearch);
			this.tabpn.setToolTipTextAt(this.getTabIndexOf(TAB_SEARCH), CommonLocaleDelegate.getMessage("CollectPanel.6","Suche (F3)"));
		}
		this.tabpn.addTab(CommonLocaleDelegate.getMessage("CollectPanel.3","Ergebnis"), this.pnlResult);
		this.tabpn.setToolTipTextAt(this.getTabIndexOf(TAB_RESULT), CommonLocaleDelegate.getMessage("CollectPanel.4","Ergebnis (F4)"));
		this.tabpn.addTab(CommonLocaleDelegate.getMessage("CollectPanel.1","Details"), this.pnlDetails);
		this.tabpn.setToolTipTextAt(this.getTabIndexOf(TAB_DETAILS), CommonLocaleDelegate.getMessage("CollectPanel.2","Details (F5)"));
		
		this.tabpn.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);*/
	}

	/**
	 * This default implementation creates an instance of <code>SearchPanel</code>.
	 * Successors may return a new instance of a custom <code>SearchPanel</code> here.
	 * @return a new SearchPanel which will be part of this panel.
	 * @postcondition result != null
	 */
	public SearchPanel newSearchPanel() {
		return new SearchPanel();
	}

	/**
	 * This default implementation creates an instance of <code>ResultPanel</code>.
	 * Successors may return a new instance of a custom <code>ResultPanel</code> here.
	 * @return a new ResultPanel which will be part of this panel.
	 * @postcondition result != null
	 */
	public ResultPanel<Clct> newResultPanel() {
		return new ResultPanel<Clct>();
	}

	/**
	 * This default implementation creates an instance of <code>DetailsPanel</code>.
	 * Successors may return a new instance of a custom <code>DetailsPanel</code> here.
	 * @return a new DetailsPanel which will be part of this panel.
	 * @postcondition result != null
	 */
	public DetailsPanel newDetailsPanel() {
		return new DetailsPanel();
	}

	/**
	 * @return Is the search panel available?
	 * 
	 * @deprecated The CollectPanel must not contain the ResultPanel. Refactoring needed! 
	 */
	public boolean containsSearchPanel() {
		return this.bContainsSearchPanel;
	}

	/**
	 * @return the Search panel
	 * @todo add precondition containsSearchPanel() OR add precondition "result != null <--> containsSearchPanel()
	 * 
	 * @deprecated The CollectPanel must not contain the ResultPanel. Refactoring needed! 
	 */
	public SearchPanel getSearchPanel() {
		return this.pnlSearch;
	}

	/**
	 * @return the Result panel
	 * @postcondition result != null
	 * 
	 * @deprecated The CollectPanel must not contain the ResultPanel. Refactoring needed! 
	 */
	public ResultPanel<Clct> getResultPanel() {
		return this.pnlResult;
	}

	/**
	 * @return the Details panel
	 * @postcondition result != null
	 */
	public DetailsPanel getDetailsPanel() {
		return this.pnlDetails;
	}

	/**
	 * @return the tabbed pane
	 * @postcondition result != null
	 */
	/*private JTabbedPane getTabbedPane() {
		return this.tabpn;
	}*/

	/**
	 * converts an external (public) tab index to the internal (private) tab index,
	 * depending on whether the Search panel is visible or not.
	 * @param iExternalTabIndex
	 * @return the internal tab index
	 */
	public int getTabIndexOf(int iExternalTabIndex) {
		return (this.containsSearchPanel() ? iExternalTabIndex : iExternalTabIndex - 1);
	}

	/**
	 * converts an internal (private) tab index to the external (public) tab index,
	 * depending on whether the Search panel is visible or not.
	 * @param iInternalTabIndex
	 * @return the external tab index
	 */
	public int getExternalTabIndexOf(int iInternalTabIndex) {
		return (this.containsSearchPanel() ? iInternalTabIndex : iInternalTabIndex + 1);
	}

	/**
	 * wrapper for JTabbedPane.getTabCount
	 * @return the number of tabs of the tabbed pane
	 */
	public int getTabCount() {
		//return this.getTabbedPane().getTabCount();
		return bContainsSearchPanel ? 3 : 2;
	}

	/**
	 * wrapper for JTabbedPane.isEnabledAt
	 * @param iExternalIndex
	 * @return Is the tabbed pane with the given external index enabled?
	 */
	public boolean isTabbedPaneEnabledAt(int iExternalIndex) {
		//final int iIndex = CollectPanel.this.getTabIndexOf(iExternalIndex);
		//return (iIndex >= 0) && this.tabpn.isEnabledAt(iIndex);
		return optionsEnabled.get(iExternalIndex);
	}

	/**
	 * wrapper for JTabbedPane.setEnabledAt
	 * @param iExternalIndex
	 * @param bEnabled
	 */
	public void setTabbedPaneEnabledAt(int iExternalIndex, boolean bEnabled) {
		/*final int iIndex = CollectPanel.this.getTabIndexOf(iExternalIndex);
		if (iIndex >= 0) {
			this.tabpn.setEnabledAt(iIndex, bEnabled);
		}*/
		
		optionsEnabled.put(iExternalIndex, bEnabled);
		pnlSearch.cpi.updateOption(iExternalIndex, bEnabled);
		pnlResult.cpi.updateOption(iExternalIndex, bEnabled);
		pnlDetails.cpi.updateOption(iExternalIndex, bEnabled);
	}

	/**
	 * wrapper for JTabbedPane.setToolTipTextAt
	 * @param iExternalIndex
	 * @param sToolTipText
	 */
	public void setTabbedPaneToolTipTextAt(int iExternalIndex, String sToolTipText) {
		/*final int iIndex = CollectPanel.this.getTabIndexOf(iExternalIndex);
		if (iIndex >= 0) {
			this.tabpn.setToolTipTextAt(iIndex, sToolTipText);
		}*/
		
		pnlSearch.cpi.setToolTip(iExternalIndex, sToolTipText);
		pnlResult.cpi.setToolTip(iExternalIndex, sToolTipText);
		pnlDetails.cpi.setToolTip(iExternalIndex, sToolTipText);
	}

	/**
	 * wrapper for JTabbedPane.getSelectedIndex
	 * @return the external index of the selected tab.
	 */
	public int getTabbedPaneSelectedIndex() {
		//return CollectPanel.this.getExternalTabIndexOf(this.tabpn.getSelectedIndex());
		
		if (pnlSearch.isVisible()) 
			return TAB_SEARCH;
		else if (pnlResult.isVisible()) 
			return TAB_RESULT;
		else if (pnlDetails.isVisible()) 
			return TAB_DETAILS;
		else
			throw new IllegalArgumentException("No panel is at position 0");
		
	}

	/**
	 * wrapper for JTabbedPane.setSelectedIndex
	 * @param iExternalIndex
	 */
	public void setTabbedPaneSelectedIndex(int iExternalIndex) {
		/*final int iIndex = CollectPanel.this.getTabIndexOf(iExternalIndex);
		if (iIndex >= 0) {
			this.tabpn.setSelectedIndex(iIndex);
		}*/
		
		pnlSearch.setVisible(TAB_SEARCH == iExternalIndex);
		pnlResult.setVisible(TAB_RESULT == iExternalIndex);
		pnlDetails.setVisible(TAB_DETAILS == iExternalIndex);
	}

	/**
	 * wrapper for JTabbedPane.setSelectedComponent
	 * @param comp
	 */
	public void setTabbedPaneSelectedComponent(Component comp) {
		//this.tabpn.setSelectedComponent(comp);
		
		pnlSearch.setVisible(pnlSearch == comp);
		pnlResult.setVisible(pnlResult == comp);
		pnlDetails.setVisible(pnlDetails == comp);
	}

	/**
	 * wrapper for JTabbedPane.addChangeListener
	 * @param tabChangeListener
	 */
	void addTabbedPaneChangeListener(ChangeListener tabChangeListener) {
		//this.tabpn.addChangeListener(tabChangeListener);
		
		chgListeners.add(tabChangeListener);
	}

	/**
	 * wrapper for JTabbedPane.removeChangeListener
	 * @param tabChangeListener
	 */
	void removeTabbedPaneChangeListener(ChangeListener tabChangeListener) {
		//this.tabpn.removeChangeListener(tabChangeListener);
		
		chgListeners.remove(tabChangeListener);
	}

	@Override
	public void setVisible(boolean aFlag) {
		super.setVisible(aFlag);
	}
	
	

}	// class CollectPanel
