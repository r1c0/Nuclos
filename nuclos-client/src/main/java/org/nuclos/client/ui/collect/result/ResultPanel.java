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
package org.nuclos.client.ui.collect.result;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXBusyLabel;
import org.jfree.util.Log;
import org.nuclos.client.common.EnabledListener;
import org.nuclos.client.common.WorkspaceUtils;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.synthetica.NuclosThemeSettings;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.PopupButton;
import org.nuclos.client.ui.StatusBarTextField;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.WrapLayout;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.client.ui.collect.CollectPanel;
import org.nuclos.client.ui.collect.CollectableTableHelper;
import org.nuclos.client.ui.collect.SearchFilterBar;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.ToolTipsTableHeader;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.CollectableComponentFactory;
import org.nuclos.client.ui.collect.component.model.ChoiceEntityFieldList;
import org.nuclos.client.ui.collect.indicator.CollectPanelIndicator;
import org.nuclos.client.ui.collect.model.CollectableEntityFieldBasedTableModel;
import org.nuclos.client.ui.popupmenu.DefaultJPopupMenuListener;
import org.nuclos.client.ui.table.CommonJTable;
import org.nuclos.client.ui.table.TableUtils;
import org.nuclos.client.ui.util.TableLayoutBuilder;
import org.nuclos.common.Actions;
import org.nuclos.common.WorkspaceDescription.EntityPreferences;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * <br>Result panel for collecting data
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
@Configurable
public class ResultPanel<Clct extends Collectable> extends JPanel {

	private final CollectPanelIndicator cpi = new CollectPanelIndicator(CollectPanel.TAB_RESULT);
	
	private final SpringLocaleDelegate localeDelegate = SpringLocaleDelegate.getInstance();

	/**
	 * the toolbar.
	 */
	private final JToolBar toolBar = UIUtils.createNonFloatableToolBar();

	private int popbtnExtraIndex = -1;

	private final PopupButton popbtnExtra = new PopupButton(SpringLocaleDelegate.getInstance().getMessage("PopupButton.Extras","Extras"));

	/**
	 * Button: "Enter New mode"
	 * TODO: make private.
	 */
	public final JButton btnNew = new JButton();

	/**
	 * Button: "Clone selected object"
	 * TODO: make private.
	 */
	public final JMenuItem btnClone = new JMenuItem();

	/**
	 * Button: "Delete selected object(s)"
	 * TODO: make private.
	 */
	public final AbstractButton btnDelete;

	/**
	 * Button: "Edit selected object(s)"
	 * TODO: make private.
	 */
	public final JButton btnEdit = new JButton();

	/**
	 * Button: "Refresh search result"
	 * TODO: make private.
	 */
	public final JButton btnRefresh = new JButton();

	/**
	 * Button: "Select columns"
	 * TODO: make private.
	 */
	public final JMenuItem btnSelectColumns = new JMenuItem();


	/**
	 * Button: "Export selected Collectables."
	 * TODO: make private.
	 */
	public final JMenuItem btnExport = new JMenuItem();

	/**
	 * Button: "Import Collectables."
	 * TODO: make private.
	 */
	public final JMenuItem btnImport = new JMenuItem();

	/**
	 * Button: "Add Bookmark"
	 * TODO: make private.
	 */
	public final JMenuItem btnBookmark = new JMenuItem();
	
	public final JButton btnResetMainFilter = new JButton();
	
	public final JButton btnSelectAllRows = new StatusBarButton(Icons.getInstance().getIconSelectAll12(), Icons.getInstance().getIconSelectAllHover12(),
			localeDelegate.getMessage("ResultPanel.16","Alles auswählen"));
	
	public final JButton btnDeSelectAllRows = new StatusBarButton(Icons.getInstance().getIconDeSelectAll12(), Icons.getInstance().getIconDeSelectAllHover12(),
			localeDelegate.getMessage("ResultPanel.17","Auswahl aufheben"));

	protected static final String EXPORT_IMPORT_EXTENSION = ".zip";
	
	private JComponent compCenter = new JPanel(new BorderLayout());
	
	private final Collection<ResultKeyListener> keyListener = new ArrayList<ResultKeyListener>();

	private final JPanel pnlResultTable;
	protected final SearchFilterBar searchFilterBar;
	private final JScrollPane scrlpnResult = new JScrollPane();
	private final JTable tblResult;
	
	protected final JPanel pnlSouth;
	private final JPanel pnlActions;
	private final JPanel pnlShowActions;
	private final JPanel pnlHideActions;
	private final AbstractButton btnShowActions;
	private final AbstractButton btnHideActions;
	
	private final static int MIN_ACTIONS_HEIGHT = 30;
	
	private final JXBusyLabel busyActions = new JXBusyLabel(new Dimension(16, 16));
	
	private boolean alternateSelectionToggle = true;
	private boolean isActionsVisible = true;
	private boolean isActionsEnabled = true;
	
	private Collection<EnabledListener> actionsVisibleListener = new ArrayList<EnabledListener>();

	public final StatusBarTextField tfStatusBar = new StatusBarTextField(" ");

	/**
	 * the popup menu for a row
	 * TODO: make private.
	 */
	public final JPopupMenu popupmenuRow = new JPopupMenu();
	public final JMenuItem miPopupEdit = new JMenuItem(localeDelegate.getMessage("ResultPanel.3","Bearbeiten"));
	public final JMenuItem miPopupClone = new JMenuItem(localeDelegate.getMessage("ResultPanel.7","Klonen"));
	public final JMenuItem miPopupDelete = new JMenuItem(localeDelegate.getMessage("ResultPanel.8","L\u00f6schen..."));
	public final JMenuItem miPopupDefineAsNewSearchResult = new JMenuItem(localeDelegate.getMessage("ResultPanel.1","Als neues Suchergebnis"));
	public final JMenuItem miPopupOpenInNewTab = new JMenuItem();
	public final JMenuItem miPopupBookmark = new JMenuItem();
	public final JMenuItem miPopupCopyCells = new JMenuItem(localeDelegate.getMessage("ResultPanel.13","Kopiere markierte Zellen"));
	public final JMenuItem miPopupCopyRows = new JMenuItem(localeDelegate.getMessage("ResultPanel.14","Kopiere markierte Zeilen"));
	public final JMenu miGenerations = new JMenu(localeDelegate.getMessage("ResultPanel.12","Arbeitsschritte"));
	public final JMenu miStates = new JMenu(localeDelegate.getMessage("ResultPanel.15","Statuswechsel"));
	
	// Spring injection
	
	protected WorkspaceUtils workspaceUtils;
	
	// end of Spring injection

	public ResultPanel() {
		super(new BorderLayout());

		this.btnDelete = getDeleteButton();

		this.pnlShowActions = new JPanel(new BorderLayout());
		this.btnShowActions = new UpDownButton(true);
		this.pnlShowActions.add(btnShowActions, BorderLayout.CENTER);
		
		this.pnlHideActions = new JPanel(new BorderLayout());
		this.btnHideActions = new UpDownButton(false);
		this.pnlHideActions.add(btnHideActions, BorderLayout.CENTER);
		
		this.pnlActions = new JPanel(new FlowLayout());
		
		this.pnlSouth = new JPanel(new BorderLayout());
		this.pnlSouth.add(pnlHideActions, BorderLayout.NORTH);
		this.pnlSouth.add(pnlActions, BorderLayout.CENTER);
		this.pnlSouth.add(pnlShowActions, BorderLayout.SOUTH);
		

		this.tblResult = newResultTable();
		this.searchFilterBar = new SearchFilterBar();
		this.pnlResultTable = newResultTablePanel();
		
		setActionsPanelEmpty(MIN_ACTIONS_HEIGHT);
		setActionsVisible(isActionsVisible);

		//this.add(compCenter, BorderLayout.CENTER);
		//this.add(UIUtils.newStatusBar(tfStatusBar), BorderLayout.SOUTH);
		this.setCenterComponent(compCenter);
		this.setSouthComponent(UIUtils.newStatusBar(btnSelectAllRows, btnDeSelectAllRows, Box.createHorizontalStrut(10), tfStatusBar));

		this.popupmenuRow.setName("popupmenuRow");
		this.popupmenuRow.add(this.miPopupEdit);
		this.popupmenuRow.add(this.miPopupClone);
		this.popupmenuRow.add(this.miPopupDelete);
		this.popupmenuRow.addSeparator();
		this.popupmenuRow.add(this.miPopupOpenInNewTab);
		this.popupmenuRow.add(this.miPopupBookmark);
		this.miGenerations.setVisible(false);
		this.popupmenuRow.add(this.miGenerations);
		this.miStates.setVisible(false);
		this.popupmenuRow.add(this.miStates);
		this.popupmenuRow.add(this.miPopupCopyCells);
		this.popupmenuRow.add(this.miPopupCopyRows);

		/** @todo implement: */
//		this.popupmenuRow.add(this.miPopupDefineAsNewSearchResult);

		this.miPopupDelete.setEnabled(false);

		this.compCenter.add(pnlResultTable, BorderLayout.CENTER);

		this.tfStatusBar.setMinimumSize(new Dimension(0, this.tfStatusBar.getPreferredSize().height));

		// popup menu: PLAIN font, DefaultAction BOLD:
		final Font fontPlain = this.popupmenuRow.getFont().deriveFont(Font.PLAIN);
		for (Component comp : this.popupmenuRow.getComponents()) {
			comp.setFont(fontPlain);
		}

		// Edit is default action:
		this.miPopupEdit.setFont(this.miPopupEdit.getFont().deriveFont(Font.BOLD));
		
		this.busyActions.getBusyPainter().setBaseColor(NuclosThemeSettings.BACKGROUND_COLOR3);
		this.busyActions.getBusyPainter().setHighlightColor(Color.WHITE);
		
		init();
	}
	
	@Autowired
	final void setWorkspaceUtils(WorkspaceUtils workspaceUtils) {
		this.workspaceUtils = workspaceUtils;
	}

	public final CollectPanelIndicator getCollectPanelIndicator() {
		return cpi;
	}

	/**
	 * init after construct...
	 */
	protected void init() {
		setupDefaultToolBarActions(toolBar);
		setNorthComponent(toolBar);
		popbtnExtraIndex = getToolBarNextIndex();
		if (popbtnExtra.getComponentCount() > 0)
			toolBar.add(popbtnExtra);

		this.popupmenuRow.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				if (miPopupOpenInNewTab.getAction() != null) {
					miPopupOpenInNewTab.setEnabled(miPopupOpenInNewTab.getAction().isEnabled());
				}
				if (miPopupBookmark.getAction() != null) {
					miPopupBookmark.setEnabled(miPopupBookmark.getAction().isEnabled());
				}

			}
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {}
		});
		
		this.btnShowActions.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setActionsVisible(true);
			}
		});
		this.btnHideActions.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setActionsVisible(false);
			}
		});
	}
	
	public void setActionsEnabled(boolean enabled) {
		this.isActionsEnabled = enabled;
		if (enabled) {
			setActionsVisible(isActionsVisible);
		} else {
			this.pnlHideActions.setVisible(false);
			this.pnlActions.setVisible(false);
			this.pnlShowActions.setVisible(false);
		}
	}
	
	public boolean isActionsVisible() {
		return this.isActionsVisible;
	}
	
	public void setActionsVisible(boolean visible) {
		if (isActionsEnabled) {
			this.isActionsVisible = visible;
			this.pnlHideActions.setVisible(visible);
			this.pnlActions.setVisible(visible);
			this.pnlShowActions.setVisible(!visible);
			notifyActionsVisibleListener();
		}
	}
	
	public void addActionsVisibleListener(EnabledListener visibleListener) {
		this.actionsVisibleListener.add(visibleListener);
	}
	
	public void removeActionsVisibleListener(EnabledListener visibleListener) {
		this.actionsVisibleListener.remove(visibleListener);
	}
	
	private void notifyActionsVisibleListener() {
		for (EnabledListener visibleListener : actionsVisibleListener) {
			visibleListener.enabledChanged(isActionsVisible);
		}
	}

	public void updatePopupExtraVisibility() {
		if (popbtnExtraIndex != -1 && toolBar.getComponentIndex(popbtnExtra) < 0) {
			toolBar.add(popbtnExtra, popbtnExtraIndex);
		}
	}

	private void setNorthComponent(JComponent comp) {
		add(comp, BorderLayout.NORTH);
	}

	protected void setSouthComponent(JComponent comp) {
		add(comp, BorderLayout.SOUTH);
	}

	protected void setupDefaultToolBarActions( JToolBar toolBar) {

		toolBar.add(cpi.getJPanel());

		toolBar.add(btnEdit, null);
		toolBar.add(btnRefresh, null);
		toolBar.add(btnNew, null);
		toolBar.add(btnDelete, null);
		toolBar.add(btnResetMainFilter, null);

		addPopupExtraMenuItem(btnBookmark);

		//result.add(btnClone, null);
		//result.add(btnSelectColumns, null);
		addPopupExtraMenuItem(btnClone);
		if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_CUSTOMIZE_ENTITY_AND_SUBFORM_COLUMNS) ||
				!workspaceUtils.getWorkspace().isAssigned()) {
			addPopupExtraMenuItem(btnSelectColumns);
		}

		this.btnEdit.setName("btnEdit");
		this.btnEdit.setIcon(Icons.getInstance().getIconEdit16());
		this.btnEdit.setEnabled(false);
		this.btnEdit.setToolTipText(localeDelegate.getMessage("ResultPanel.2","Ausgew\u00e4hlte Datens\u00e4tze ansehen/bearbeiten"));
		this.btnEdit.setText(localeDelegate.getMessage("ResultPanel.4","Bearbeiten"));
		this.btnEdit.putClientProperty("hideActionText", Boolean.TRUE);

		this.btnDelete.setName("btnDelete");
		this.btnDelete.setEnabled(false);
		this.btnDelete.putClientProperty("hideActionText", Boolean.TRUE);

		this.btnNew.setName("btnNew");
		this.btnNew.putClientProperty("hideActionText", Boolean.TRUE);

		this.btnClone.setName("btnClone");
		//this.btnClone.putClientProperty("hideActionText", Boolean.TRUE);

		this.btnRefresh.setName("btnRefresh");
		this.btnRefresh.setIcon(Icons.getInstance().getIconRefresh16());
		this.btnRefresh.setToolTipText(localeDelegate.getMessage("ResultPanel.9","Liste aktualisieren (Erneut suchen)"));

		this.btnSelectColumns.setName("btnSelectColumns");
		this.btnSelectColumns.setIcon(Icons.getInstance().getIconSelectVisibleColumns16());
		this.btnSelectColumns.setText(localeDelegate.getMessage("ResultPanel.11","Spalten ein-/ausblenden"));

		// disabled 	NUCLOSINT-1480
		//addPopupExtraSeparator();
		//addPopupExtraMenuItem(btnExport);
		//addPopupExtraMenuItem(btnImport);

		this.btnExport.setName("btnExport");
		this.btnExport.setIcon(Icons.getInstance().getIconExport16());
		this.btnExport.setText(localeDelegate.getMessage("ResultPanel.10","Selektierte Daten Exportieren"));

		this.btnImport.setName("btnImport");
		this.btnImport.setIcon(Icons.getInstance().getIconImport16());
		this.btnImport.setText(localeDelegate.getMessage("ResultPanel.5","Daten Importieren"));
	}

	public void addPopupExtraSeparator() {
		updatePopupExtraVisibility();
		popbtnExtra.addSeparator();
	}

	public Component addPopupExtraComponent(Component comp) {
		updatePopupExtraVisibility();
		return popbtnExtra.add(comp);
	}

	public void removePopupExtraComponent(Component comp) {
		popbtnExtra.remove(comp);
	}

	public JMenuItem addPopupExtraMenuItem(JMenuItem mi) {
		updatePopupExtraVisibility();
		return popbtnExtra.add(mi);
	}

	public void removePopupExtrasMenuItem(JMenuItem mi) {
		popbtnExtra.remove(mi);
	}

	/**
	 *
	 * @param comp
	 * @return index of comp in toolbar
	 */
	public int addToolBarComponent(Component comp) {
		toolBar.add(comp);
		toolBar.validate();
		return toolBar.getComponentIndex(comp);
	}

	/**
	 *
	 * @param comps
	 * @return index of comp in toolbar
	 */
	public int addToolBarComponents(List<Component> comps) {
		if (comps.size() == 0)
			return -1;

		for (Component comp : comps)
			toolBar.add(comp);
		toolBar.validate();
		return toolBar.getComponentIndex(comps.get(0));
	}

	public void addToolBarComponents(List<Component> comps, int index) {
		if (comps.size() == 0)
			return;

		// add last list entry first to toolbar
		List<Component> reversedComps = new ArrayList<Component>(comps);
		Collections.reverse(reversedComps);
		for (Component comp : reversedComps)
			toolBar.add(comp, index);
		toolBar.validate();
	}

	public int getToolBarNextIndex() {
		return toolBar.getComponentCount();
	}

	public int addToolBarSeparator() {
		toolBar.addSeparator();
		return toolBar.getComponentCount()-1;
	}

	public void addToolBarComponent(Component comp, int index) {
		toolBar.add(comp, index);
		toolBar.validate();
	}

	public void addToolBarHorizontalStruct(int width) {
		toolBar.add(Box.createHorizontalStrut(width));
	}

	public void removeToolBarComponent(Component comp) {
		toolBar.remove(comp);
		toolBar.revalidate();
	}

	public void removeToolBarComponents(List<Component> comps) {
		for (Component comp : comps)
			toolBar.remove(comp);
		toolBar.revalidate();
	}

	/**
	 * cleans up the toolbars
	 */
	public void cleanUpToolBar() {
		UIUtils.cleanUpToolBar(this.toolBar);
	}
	
	public void setAlternateSelectionToggle(boolean toggle) {
		this.alternateSelectionToggle = toggle;
	}

	/**
	 * @return the table used in this result panel to display data
	 */
	protected CommonJTable newResultTable() {
		CommonJTable result = new CommonJTable() {
			
			@Override
			public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
//				System.out.println("row="+rowIndex+" ,col="+columnIndex+" ,toggle="+toggle+" ,extend="+extend);
				super.changeSelection(rowIndex, columnIndex, alternateSelectionToggle? !toggle: toggle, extend);
			}
			
			@Override
			protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
				boolean processed = false;
				for (ResultKeyListener rkl : keyListener) {
					if (rkl.processKeyBinding(ks, e, condition, pressed)) {
						processed = true;
					}
				}
				if (processed) {
					return true;
				} else {
					return super.processKeyBinding(ks, e, condition, pressed);
				}
			}
		};

		ToolTipsTableHeader tblHeader = new ToolTipsTableHeader(null, result.getColumnModel());

		tblHeader.setName("tblHeader");
		result.setTableHeader(tblHeader);

		return result;
	}

	/**
	 * Create the Panel which holds the ResultTable
	 * @return the newly created panel
	 */
	protected JPanel newResultTablePanel() {
		this.scrlpnResult.setName("scrlpnResult");
		this.scrlpnResult.setAutoscrolls(true);
		this.scrlpnResult.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.scrlpnResult.setPreferredSize(new Dimension(300, 200));
		this.scrlpnResult.setViewportView(tblResult);
		this.scrlpnResult.getViewport().setBackground(tblResult.getBackground());

		this.tblResult.setName("tblResult");
		this.tblResult.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.tblResult.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		final JPanel result = new JPanel(new BorderLayout());
		result.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		result.add(scrlpnResult, BorderLayout.CENTER);

		result.add(searchFilterBar.getJComponent(), BorderLayout.NORTH);
		result.add(pnlSouth, BorderLayout.SOUTH);
		
		return result;
	}

	/**
	 * @return the table containing the search results
	 */
	public JTable getResultTable() {
		return tblResult;
	}

	protected AbstractButton getDeleteButton() {
		return new JButton();
	}


	/**
	 * defines the initial custom toolbar area
	 * @return an empty nonfloatable toolbar occupying the free space
	 */
	/*private static Component createDefaultCustomToolBarArea() {
		final JToolBar result = UIUtils.createNonFloatableToolBar();
		result.add(Box.createHorizontalGlue());
		return result;
	}*/

	/**
	 * sets the custom toolbar area. This area occupies the center of the toolbar panel.
	 * For best results, add a non-floatable JToolBar containing additional buttons here.
	 * Note that a toolbar can contain labels, struts and horizontal glue as well.
	 * @param compCustomToolBarArea
	 */
	/*public void setCustomToolBarArea(Component compCustomToolBarArea) {
		this.pnlToolBar.remove(this.compCustomToolBarArea);
		this.compCustomToolBarArea = compCustomToolBarArea;
		this.pnlToolBar.add(this.compCustomToolBarArea, BorderLayout.CENTER);
	}*/

	public JScrollPane getResultTableScrollPane() {
		return this.scrlpnResult;
	}

	/**
	 * @return the panel containing the result table (including the surrounding scrollpane).
	 * This panel should not be modified from outside.
	 */
	public JPanel getResultTablePanel() {
		return this.pnlResultTable;
	}

	/**
	 * sets (replaces) the center component, that is the component occupying the remaining space between the toolbar
	 * and the statusbar.
	 * As a default, this component is a JPanel with a BorderLayout and the result table panel
	 * (as in getResultTablePanel()) in its center.
	 * A new center component may be specified from outside. A typical sequence could be:
	 * <code>
	 *   final JSplitPane splitpn = new JSplitPane(JSplitPane.VERTICAL);
	 *   final ResultPanel pnlResult = this.getCollectPanel().getResultPanel();
	 *   pnlResult.setCenterComponent(splitpn);
	 *   splitpn.add(pnlResult.getResultTablePanel());
	 *   splitpn.add(pnlCustom);
	 * </code>
	 */
	public void setCenterComponent(JComponent compCenter) {
		this.remove(this.compCenter);
		this.compCenter = compCenter;
		this.add(this.compCenter, BorderLayout.CENTER);
	}

	/**
	 * command: export
	 * export selected data
	 */
	public void cmdExport(final CollectController<Clct> clctctl) {
		throw new UnsupportedOperationException("cmdExport");
	}

	/**
	 * command: import
	 * import data
	 */
	public void cmdImport(final CollectController<Clct> clctctl) {
		throw new UnsupportedOperationException("cmdImport");
	}

	/**
	 * @param lstclctefSelected
	 * @return field -> column width
	 *
	 * TODO: Make protected again.
	 */
	public Map<String, Integer> getVisibleColumnWidth(List<? extends CollectableEntityField> lstclctefSelected) {
		// remember the widths of the currently visible columns
		final Map<String, Integer> result = new HashMap<String, Integer>();
		for (CollectableEntityField clctef : lstclctefSelected) {
			final Integer iWidth = this.getResultTable().getColumn(clctef.getLabel()).getWidth();
			result.put(clctef.getName(), iWidth);
		}
		return result;
	}

	/**
	 * @param lstclctefColumns
	 * @param mpWidths field name -> column width
	 *
	 * TODO: Make this protected again.
	 */
	public void restoreColumnWidths(List<? extends CollectableEntityField> lstclctefColumns, Map<String, Integer> mpWidths) {
		// restore the widths of the still present columns
		for (final CollectableEntityField clctef : lstclctefColumns) {
			if (mpWidths.containsKey(clctef.getName())) {
				getResultTable().getColumn(clctef.getLabel()).setPreferredWidth(mpWidths.get(clctef.getName()));
			}
		}
	}

	protected void setVisibleTable(boolean visibility){
		getResultTable().setVisible(visibility);
	}

	/**
	 *
	 * @param table for compatibility with old code the tabel is geven to this method
	 * 			could be removed later
	 *
	 * TODO: Make this protected again.
	 */
	public void setupTableCellRenderers(JTable table) {
		int iRowHeight = SubForm.MIN_ROWHEIGHT;
		// setup a table cell renderer for each column:
		for (Enumeration<TableColumn> enumeration = table.getColumnModel().getColumns(); enumeration.hasMoreElements();) {
			final TableColumn column = enumeration.nextElement();
			final CollectableEntityField clctef = ((CollectableEntityFieldBasedTableModel) table.getModel()).getCollectableEntityField(column.getModelIndex());
			final CollectableComponent clctcomp = CollectableComponentFactory.getInstance().newCollectableComponent(clctef, null, false);

			final TableCellRenderer renderer = clctcomp.getTableCellRenderer(false);
			iRowHeight = Math.max(iRowHeight, renderer.getTableCellRendererComponent(table, CollectableUtils.getNullField(clctef), true, true, 0, 0).getPreferredSize().height);
			column.setCellRenderer(renderer);
		}
		table.setRowHeight(iRowHeight);
	}


	/**
	 * sets all column widths to user prefs; set optimal width if no preferences yet saved
	 * Copied from the SubFormController
	 * @param tblResult
	 *
	 * TODO: Make this protected again.
	 */
	public void setColumnWidths(JTable tblResult, EntityPreferences entityPreferences) {
		final List<Integer> lstWidthsFromPreferences = workspaceUtils.getColumnWidthsWithoutFixed(entityPreferences);

		boolean bUseCustomColumnWidths = !lstWidthsFromPreferences.isEmpty();
		if (bUseCustomColumnWidths) {
			Logger.getLogger(this.getClass()).debug("Restoring column widths from user preferences");
			for (int iColumn = 0; iColumn < tblResult.getColumnModel().getColumnCount(); iColumn++) {
				final TableColumn column = tblResult.getColumnModel().getColumn(iColumn);
				try {
					final Integer iPreferredCellWidth = lstWidthsFromPreferences.get(iColumn);
					if (iPreferredCellWidth != null) {
						column.setPreferredWidth(iPreferredCellWidth);
						column.setWidth(iPreferredCellWidth);
					}
				} catch (Exception e) {
					try {
						if (tblResult.getModel() instanceof CollectableEntityFieldBasedTableModel) {
							CollectableEntityFieldBasedTableModel cefbtm = (CollectableEntityFieldBasedTableModel) tblResult.getModel();
							final int width = Math.max(
									TableUtils.getPreferredColumnWidth(tblResult, iColumn, 50, TableUtils.TABLE_INSETS),
									TableUtils.getMinimumColumnWidth(cefbtm.getCollectableEntityField(iColumn).getJavaClass()));
							column.setPreferredWidth(width);
							column.setWidth(width);
						} else {
							TableUtils.setOptimalColumnWidth(tblResult, iColumn);
						}
					} catch (Exception ex) {
						Log.error("Restoring column widths from user preferences", ex);
						TableUtils.setOptimalColumnWidth(tblResult, iColumn);
					}
				}
			}
		}
		else {
			// If there are no stored field widths or the number of stored field widths differs from the column count
			// (that is, the number of columns has changed since the last invocation of the client)

			if (tblResult.getModel() instanceof CollectableEntityFieldBasedTableModel) {
				CollectableEntityFieldBasedTableModel cefbtm = (CollectableEntityFieldBasedTableModel) tblResult.getModel();

				for (int iColumn = 0; iColumn < cefbtm.getColumnCount(); iColumn++) {
					if (tblResult.getColumnModel().getColumnCount() > iColumn) {
						final TableColumn column = tblResult.getColumnModel().getColumn(iColumn);
						final int width = Math.max(
								TableUtils.getPreferredColumnWidth(tblResult, iColumn, 50, TableUtils.TABLE_INSETS),
								TableUtils.getMinimumColumnWidth(cefbtm.getCollectableEntityField(iColumn).getJavaClass()));
						column.setPreferredWidth(width);
						column.setWidth(width);
					}
				}
			}

			tblResult.revalidate();
		}
	}

	public Map<String, Integer> getCurrentFieldWithsMap(){
		JTable resultTable = getResultTable();
		return CollectableTableHelper.getColumnWidthsMap(resultTable);
	}

	/**
	 * Popup menu for the rows in the Result table.
	 */
	protected static class PopupMenuRowsListener extends DefaultJPopupMenuListener {
		private final JTable tbl;

		public PopupMenuRowsListener(JPopupMenu popup, JTable tbl) {
			super(popup);

			this.tbl = tbl;
		}

		@Override
		protected void showPopupMenu(MouseEvent ev) {
			// first select/deselect the row:
			final int iRow = tbl.rowAtPoint(ev.getPoint());

			// Nur, wenn nicht selektiert, selektieren:
			if (!tbl.isRowSelected(iRow)) {
				if ((ev.getModifiers() & MouseEvent.CTRL_MASK) != 0) {
					// Control gedr\u00fcckt:
					// Zeile zur Selektion hinzuf\u00fcgen:
					tbl.addRowSelectionInterval(iRow, iRow);
				}
				else {
					// Sonst nur diese Zeile selektieren:
					tbl.setRowSelectionInterval(iRow, iRow);
				}
			}  // if

			super.showPopupMenu(ev);
		}
	}  // inner class PopupMenuRowsListener

	/**
	 * TODO: Make this protected again.
	 */
	public void addDoubleClickMouseListener(MouseListener mouselistener) {
		tblResult.addMouseListener(mouselistener);
	}

	/**
	 * TODO: Make this protected again.
	 */
	public void removeDoubleClickMouseListener(MouseListener mouselistener) {
		tblResult.removeMouseListener(mouselistener);
	}

	/**
	 * TODO: Make this protected again.
	 */
	public void addPopupMenuListener() {
		// popup menu for rows:
		tblResult.addMouseListener(new PopupMenuRowsListener(popupmenuRow, tblResult));
	}

	public void addColumnModelListener(TableColumnModelListener tblcolumnlistener) {
		tblResult.getColumnModel().addColumnModelListener(tblcolumnlistener);
	}

	/** called when a column was moved in the header*/
	public void columnMovedInHeader(ChoiceEntityFieldList fields) {
		fields.setSelectedFields(CollectableTableHelper.getCollectableEntityFieldsFromColumns(this.tblResult));
	}

	protected interface ITableHeaderColumnPopupListener {

	}

	protected void setupCopyAction() {
	}

	public JToolBar getToolBar() {
		return toolBar;
	}
	
	public SearchFilterBar getSearchFilterBar() {
		return searchFilterBar;
	}
	
	private static class StatusBarButton extends JButton implements MouseListener {
		
		private final ImageIcon icon;
		private final ImageIcon iconHover;
		
		public StatusBarButton(ImageIcon icon, ImageIcon iconHover, String tooltip) {
			super(icon);
			this.icon = icon;
			this.iconHover = iconHover;
			this.setToolTipText(tooltip);
			this.addMouseListener(this);
			this.setBorderPainted(false);
			this.setBorder(BorderFactory.createEmptyBorder(4, 2, 2, 2));
		}

		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			this.setIcon(iconHover);
		}

		@Override
		public void mouseExited(MouseEvent e) {
			this.setIcon(icon);
		}
		
	}

	public void addResultKeyListener(ResultKeyListener resultKeyListener) {
		keyListener.add(resultKeyListener);
	}
	
	public void loadingResultActions() {
		int height = Math.max(MIN_ACTIONS_HEIGHT, this.pnlActions.getPreferredSize().height);
		this.pnlActions.removeAll();
		setActionsPanelEmpty(height).add(busyActions);
		busyActions.setBusy(true);
		this.pnlActions.invalidate();
		this.pnlActions.revalidate();
		this.pnlActions.repaint();
	}

	public void setResultActions(List<ResultActionCollection> actions) {
		this.busyActions.setBusy(false);
		this.pnlActions.removeAll();
		if (actions != null && !actions.isEmpty()) {
			double[] cols = new double[actions.size()];
			for (int i = 0; i < actions.size(); i++) {
				cols[i] = TableLayout.FILL;
			}
			TableLayoutBuilder tbllay = new TableLayoutBuilder(this.pnlActions).columns(cols);
			tbllay.newRow();
			for (int i = 0; i < actions.size(); i++) {
				ResultActionCollection rac = actions.get(i);
				JPanel pnlCol = new JPanel(new WrapLayout(WrapLayout.LEFT));
				pnlCol.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0), rac.getLabel()));
				
				for (Action act : rac.getActions()) {
					pnlCol.add(new ActionButton(act));
				}
				tbllay.add(pnlCol);
			}
		} else {
			setActionsPanelEmpty(MIN_ACTIONS_HEIGHT).addLabel("");
		}
		this.pnlActions.invalidate();
		this.pnlActions.revalidate();
		this.pnlActions.repaint();
	}

	public void removeResultActions() {
		this.busyActions.setBusy(false);
		this.pnlActions.removeAll();
		setActionsPanelEmpty(MIN_ACTIONS_HEIGHT).addLabel("Dummy");
		this.pnlActions.invalidate();
		this.pnlActions.revalidate();
		this.pnlActions.repaint();
	}
	
	private TableLayoutBuilder setActionsPanelEmpty(int height) {
		TableLayoutBuilder tbllay = new TableLayoutBuilder(pnlActions).columns(TableLayout.FILL, TableLayout.PREFERRED, TableLayout.FILL);
		return tbllay.newRow(height).skip();
	}
	
	private class UpDownButton extends JButton implements MouseListener {

		private final boolean up;
		
		private final int width = 16;
		private final int height = 8;
		private final int border = 4;
		
		private boolean hover;
		
		public UpDownButton(boolean up) {
			this.up = up;
			this.setSize(width, border+height+border);
			this.setPreferredSize(new Dimension(width, border+height+border));
			this.addMouseListener(this);
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			Object renderingHint = g2
					.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			
			final Rectangle bounds = getBounds();
			
			final int w = width;
			final int h = height;
			final int thickness = h/2;
			
			final int x = bounds.width/2-w/2;
			final int y = border;
			
			if (hover) {
				g2.setColor(NuclosThemeSettings.BACKGROUND_COLOR3);
				g2.fillRect(0, 0, bounds.width, bounds.height);
			}
			
			final Polygon p = new Polygon();
			
			if (up) {
				p.addPoint(x+ 0, 	y+ h-thickness);
				p.addPoint(x+ w/2, 	y+ 0);
				p.addPoint(x+ w, 	y+ h-thickness);
				p.addPoint(x+ w, 	y+ h);
				p.addPoint(x+ w/2, 	y+ thickness);
				p.addPoint(x+ 0, 	y+ h);
			} else {
				p.addPoint(x+ 0, 	y+ 0);
				p.addPoint(x+ w/2, 	y+ h-thickness);
				p.addPoint(x+ w, 	y+ 0);
				p.addPoint(x+ w, 	y+ thickness);
				p.addPoint(x+ w/2, 	y+ h);
				p.addPoint(x+ 0, 	y+ h-thickness);
			}
			
			if (hover) {
				g2.setColor(NuclosThemeSettings.BACKGROUND_PANEL);
			} else {
				g2.setColor(NuclosThemeSettings.BACKGROUND_COLOR3);
			}
			g2.fillPolygon(p);
			
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					renderingHint);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			hover=false;
			repaint();
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			hover=true;
			repaint();
		}

		@Override
		public void mouseExited(MouseEvent e) {
			hover=false;
			repaint();
		}
		
	}
	
	private class ActionButton extends JLabel implements MouseListener {
		
		private final Action action;
		
//		private final Color defaultForeground;
		
//		private final Color hoverForeground = Color.WHITE;
		
		private boolean hover = false;

		public ActionButton(Action action) {
			super((String) action.getValue(Action.NAME));
			this.addMouseListener(this);
			this.action = action;
			this.setOpaque(false);
			this.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
//			this.defaultForeground = getForeground();
			setForeground(Color.WHITE);
		}

		@Override
		public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			Object renderingHint = g2
					.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			
			final Rectangle bounds = getBounds();
			g2.setColor(hover?NuclosThemeSettings.BACKGROUND_COLOR3:NuclosThemeSettings.BACKGROUND_COLOR4);
			g2.fillRoundRect(0, 0, bounds.width, bounds.height, bounds.height*2/3, bounds.height*2/3);
			
			super.paint(g);
			
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					renderingHint);
		}



		@Override
		public void mouseClicked(MouseEvent e) {
			if (SwingUtilities.isLeftMouseButton(e)) {
				action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "ActionButton"));
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
			hover = true;
//			setForeground(hoverForeground);
			repaint();
		}

		@Override
		public void mouseExited(MouseEvent e) {
			hover = false;
//			setForeground(defaultForeground);
			repaint();
		}
		
	}
	
}  // class ResultPanel
