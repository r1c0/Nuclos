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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.PopupButton;
import org.nuclos.client.ui.StatusBarTextField;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.ResultController.Fields;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.CollectableComponentFactory;
import org.nuclos.client.ui.collect.indicator.CollectPanelIndicator;
import org.nuclos.client.ui.popupmenu.AbstractJPopupMenuListener;
import org.nuclos.client.ui.popupmenu.DefaultJPopupMenuListener;
import org.nuclos.client.ui.table.CommonJTable;
import org.nuclos.client.ui.table.TableUtils;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableUtils;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.PredicateUtils;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.PreferencesUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.PreferencesException;

/**
 * <br>Result panel for collecting data
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public class ResultPanel<Clct extends Collectable> extends JPanel {
	
	CollectPanelIndicator cpi = new CollectPanelIndicator(CollectPanel.TAB_RESULT);

	/**
	 * the toolbar.
	 */
	private final JToolBar toolBar = UIUtils.createNonFloatableToolBar();
	
	private int popbtnExtraIndex = -1;
	
	private final PopupButton popbtnExtra = new PopupButton(CommonLocaleDelegate.getMessage("PopupButton.Extras","Extras"));
	
	//private final JPanel pnlToolBar = new JPanel(new FlowLayout(FlowLayout.LEFT));

	//private Component compCustomToolBarArea = createDefaultCustomToolBarArea();

	/**
	 * Button: "Enter New mode"
	 */
	public final JButton btnNew = new JButton();

	/**
	 * Button: "Clone selected object"
	 */
	public final JMenuItem btnClone = new JMenuItem();

	/**
	 * Button: "Delete selected object(s)"
	 */
	public final AbstractButton btnDelete;

	/**
	 * Button: "Edit selected object(s)"
	 */
	public final JButton btnEdit = new JButton();

	/**
	 * Button: "Refresh search result"
	 */
	public final JButton btnRefresh = new JButton();

	/**
	 * Button: "Select columns"
	 */
	public final JMenuItem btnSelectColumns = new JMenuItem();
	

	/**
	 * Button: "Export selected Collectables."
	 */
	public final JMenuItem btnExport = new JMenuItem();
	
	/**
	 * Button: "Import Collectables."
	 */
	public final JMenuItem btnImport = new JMenuItem();
	
	/**
	 * Button: "Add Bookmark"
	 */
	public final JMenuItem btnBookmark = new JMenuItem();
	
	protected static final String EXPORT_IMPORT_EXTENSION = ".zip";
	
	private JComponent compCenter = new JPanel(new BorderLayout());

	private final JPanel pnlResultTable;
	private final JScrollPane scrlpnResult = new JScrollPane();
	private final JTable tblResult;
	
	public final StatusBarTextField tfStatusBar = new StatusBarTextField(" ");

	/**
	 * the popup menu for a row
	 */
	public final JPopupMenu popupmenuRow = new JPopupMenu();
	public final JMenuItem miPopupEdit = new JMenuItem(CommonLocaleDelegate.getMessage("ResultPanel.3","Bearbeiten"));
	public final JMenuItem miPopupClone = new JMenuItem(CommonLocaleDelegate.getMessage("ResultPanel.7","Klonen"));
	public final JMenuItem miPopupDelete = new JMenuItem(CommonLocaleDelegate.getMessage("ResultPanel.8","L\u00f6schen..."));
	public final JMenuItem miPopupDefineAsNewSearchResult = new JMenuItem(CommonLocaleDelegate.getMessage("ResultPanel.1","Als neues Suchergebnis"));
	public final JMenuItem miPopupOpenInNewTab = new JMenuItem();
	public final JMenuItem miPopupBookmark = new JMenuItem();
	public final JMenu miGenerations = new JMenu(CommonLocaleDelegate.getMessage("ResultPanel.12","Arbeitsschritte"));
	
	
	TableHeaderColumnPopupListener tableHeaderColumnListener;
	
	public ResultPanel() {
		super(new BorderLayout());
		
		this.btnDelete = getDeleteButton();

		this.tblResult = newResultTable();
		this.pnlResultTable = newResultTablePanel();
		
		//this.add(compCenter, BorderLayout.CENTER);
		//this.add(UIUtils.newStatusBar(tfStatusBar), BorderLayout.SOUTH);
		this.setCenterComponent(compCenter);
		this.setSouthComponent(UIUtils.newStatusBar(tfStatusBar));
		
		this.popupmenuRow.setName("popupmenuRow");
		this.popupmenuRow.add(this.miPopupEdit);
		this.popupmenuRow.add(this.miPopupClone);
		this.popupmenuRow.add(this.miPopupDelete);
		this.popupmenuRow.addSeparator();
		this.popupmenuRow.add(this.miPopupOpenInNewTab); 
		this.popupmenuRow.add(this.miPopupBookmark);
		this.miGenerations.setVisible(false);
		this.popupmenuRow.add(this.miGenerations);
		
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
		
		init();
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
		
		addPopupExtraMenuItem(btnBookmark);

		//result.add(btnClone, null);
		//result.add(btnSelectColumns, null);
		addPopupExtraMenuItem(btnClone);
		addPopupExtraMenuItem(btnSelectColumns);
		
		this.btnEdit.setName("btnEdit");
		this.btnEdit.setIcon(Icons.getInstance().getIconEdit16());
		this.btnEdit.setEnabled(false);
		this.btnEdit.setToolTipText(CommonLocaleDelegate.getMessage("ResultPanel.2","Ausgew\u00e4hlte Datens\u00e4tze ansehen/bearbeiten"));
		this.btnEdit.setText(CommonLocaleDelegate.getMessage("ResultPanel.4","Bearbeiten"));
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
		this.btnRefresh.setToolTipText(CommonLocaleDelegate.getMessage("ResultPanel.9","Liste aktualisieren (Erneut suchen)"));

		this.btnSelectColumns.setName("btnSelectColumns");
		this.btnSelectColumns.setIcon(Icons.getInstance().getIconSelectVisibleColumns16());
		this.btnSelectColumns.setText(CommonLocaleDelegate.getMessage("ResultPanel.11","Spalten ein-/ausblenden"));
		
		//result.addSeparator();
		//result.add(btnExport);
		//result.add(btnImport);
		addPopupExtraSeparator();
		addPopupExtraMenuItem(btnExport);
		addPopupExtraMenuItem(btnImport);
		
		this.btnExport.setName("btnExport");
		this.btnExport.setIcon(Icons.getInstance().getIconExport16());
		this.btnExport.setText(CommonLocaleDelegate.getMessage("ResultPanel.10","Selektierte Daten Exportieren"));

		this.btnImport.setName("btnImport");
		this.btnImport.setIcon(Icons.getInstance().getIconImport16());
		this.btnImport.setText(CommonLocaleDelegate.getMessage("ResultPanel.5","Daten Importieren"));
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

	/**
	 * @return the table used in this result panel to display data
	 */
	protected CommonJTable newResultTable() {
		CommonJTable result = new CommonJTable();
		
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

		return result;
	}

	protected void setModel(CollectableTableModel<Clct> tblmodel, final CollectableEntity clcte, final CollectController<Clct> ctl) {
		this.getResultTable().setModel(tblmodel);
		((ToolTipsTableHeader) this.getResultTable().getTableHeader()).setExternalModel(tblmodel);
		
		this.tableHeaderColumnListener = new TableHeaderColumnPopupListener( tblResult.getTableHeader()){

			@Override
			protected void removeColumnVisibility(TableColumn column) {
				final Map<CollectableEntityField, Integer> mpWidths = getVisibleColumnWidth(ctl.getFields().getSelectedFields());
				final CollectableEntityField clctef = ((CollectableEntityFieldBasedTableModel) tblResult.getModel()).getCollectableEntityField(column.getModelIndex());
				cmdRemoveColumn(ctl.getFields(), clctef, ctl);
				restoreColumnWidths(ctl.getFields().getSelectedFields(), mpWidths);
			}

			@Override
			public List<? extends CollectableEntityField> getSelectedFields() {
				return ctl.fields.getSelectedFields();
			}
		};
		tblResult.getTableHeader().addMouseListener(this.tableHeaderColumnListener);
	}

	protected void toggleColumnVisibility(TableColumn columnBefore, final String sFieldName, final CollectController<Clct> ctl,  final CollectableEntity clcte)  {
		try {
			final Map<CollectableEntityField, Integer> mpWidths = getVisibleColumnWidth(ctl.getFields().getSelectedFields());
			final CollectableEntityField clctef = clcte.getEntityField(sFieldName);
			final int iIndex = ctl.fields.getSelectedFields().indexOf(clctef);
			if (iIndex == -1) {
				cmdAddColumn(ctl.fields, columnBefore, sFieldName);
				if (!ctl.getCollectablesInResultAreAlwaysComplete()) {
					ctl.refreshResult();
				}
			}
			else {
				cmdRemoveColumn(ctl.getFields(), clctef, ctl);
			}
			restoreColumnWidths(ctl.getFields().getSelectedFields(), mpWidths);
		} 
		catch (CommonBusinessException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
	}

	/**
	 * removes the given column from the table
	 * @param entityField the column of the column model (as opposed to the column of the table model)
	 */
	protected void cmdRemoveColumn(final ResultController.Fields fields, CollectableEntityField entityField, CollectController<Clct> ctl) {
		fields.moveToAvailableFields(entityField);

		// Note that it is not enough to remove the column from the result table model.
		// We must rebuild the table model's columns in order to sync it with the table column model:
//		this.getResultTableModel().removeColumn(iColumn);
		((SortableCollectableTableModel<?>)tblResult.getModel()).setColumns(fields.getSelectedFields());
	}

	/**
	 * adds the column with the given field name before column columnBefore to the table.
	 * @param fields available and selected fields of the result table
	 * @param columnBefore the column of the column model (as opposed to the column of the table model)
	 * @param sFieldNameToAdd name of the column to add
	 */
	protected void cmdAddColumn(final ResultController.Fields fields, TableColumn columnBefore, final String sFieldNameToAdd) throws CommonBusinessException {
		// find the field with the given name in available fields:
		final CollectableEntityField clctef = CollectionUtils.findFirst(fields.getAvailableFields(),
				PredicateUtils.transformedInputEquals(new CollectableEntityField.GetName(), sFieldNameToAdd));

		assert clctef != null;

		final CollectableTableModel<?> tblmodel = (CollectableTableModel<?>) tblResult.getModel();
		final CollectableEntityField clctefBefore = tblmodel.getCollectableEntityField(columnBefore.getModelIndex());
		int insertPos = fields.getSelectedFields().indexOf(clctefBefore);
		insertPos = (insertPos >= 0) ? insertPos : 0;
		fields.moveToSelectedFields(insertPos, clctef);

		// Note that it is not enough to add the column to the result table model.
		// We must rebuild the table tblmodel's columns in order to sync it with the table column model:
		tblmodel.setColumns(fields.getSelectedFields());
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
	 * command: select columns
	 * Lets the user select the columns to show in the result list.
	 */
	@SuppressWarnings("unchecked")
	public void cmdSelectColumns(final ResultController.Fields fields, final CollectController<Clct> clctctl) {

		final SelectColumnsController ctl = new SelectColumnsController(clctctl.getFrame());
		final List<CollectableEntityField> lstAvailable = (List<CollectableEntityField>) fields.getAvailableFields();
		final List<CollectableEntityField> lstSelected = (List<CollectableEntityField>) fields.getSelectedFields();

		final JTable tbl = getResultTable();

		final Map<CollectableEntityField, Integer> mpWidths = getVisibleColumnWidth(lstSelected);

		final boolean bOK = ctl.run(lstAvailable, lstSelected, (Comparator<CollectableEntityField>) clctctl.getCollectableEntityFieldComparator());

		if (bOK) {
			UIUtils.runCommand(clctctl.getFrame(), new CommonRunnable() {
				@Override
                public void run() throws CommonBusinessException {
					final int iSelectedRow = tbl.getSelectedRow();
					fields.set(ctl.getAvailableColumns(), ctl.getSelectedColumns());
					final List<? extends CollectableEntityField> lstSelectedNew = fields.getSelectedFields();
					((CollectableTableModel<?>) getResultTable().getModel()).setColumns(lstSelectedNew);
					setupTableCellRenderers(getResultTable());
					Collection<CollectableEntityField> collNewlySelected = new ArrayList<CollectableEntityField>(lstSelectedNew);
					collNewlySelected.removeAll(lstSelected);
					if (!collNewlySelected.isEmpty()) {
						if (!clctctl.getCollectablesInResultAreAlwaysComplete()) {
							// refresh the result:
							clctctl.refreshResult();
						}
					}

					// reselect the previously selected row (which gets lost be refreshing the model)
					if (iSelectedRow != -1) {
						tbl.setRowSelectionInterval(iSelectedRow, iSelectedRow);
					}

					restoreColumnWidths(ctl.getSelectedColumns(), mpWidths);

					// Set the newly added columns to optimal width
					for (CollectableEntityField clctef : collNewlySelected) {
						TableUtils.setOptimalColumnWidth(tbl, tbl.getColumnModel().getColumnIndex(clctef.getLabel()));
					}
				}
			});
		}
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
	
	protected Map<CollectableEntityField, Integer> getVisibleColumnWidth(List<? extends CollectableEntityField> lstclctefSelected) {
		// remember the widths of the currently visible columns
		final Map<CollectableEntityField, Integer> result = new HashMap<CollectableEntityField, Integer>();
		for (CollectableEntityField clctef : lstclctefSelected) {
			final Integer iWidth = this.getResultTable().getColumn(clctef.getLabel()).getWidth();
			result.put(clctef, iWidth);
		}
		return result; 
	}

	protected void restoreColumnWidths(List<? extends CollectableEntityField> lstclctefColumns, Map<CollectableEntityField, Integer> mpWidths) {
		// restore the widths of the still present columns
		for (final CollectableEntityField clctef : lstclctefColumns) {
			if (mpWidths.containsKey(clctef)) {
				getResultTable().getColumn(clctef.getLabel()).setPreferredWidth(mpWidths.get(clctef));
			}
		}
	}

	/**
	 * initializes the <code>fields</code> field.
	 * @param clcte
	 * @param preferences
	 */
	protected void initializeFields(CollectableEntity clcte, CollectController<Clct> clctctl, Preferences preferences) {
		clctctl.fields.set(clctctl.getFieldsAvailableForResult(clcte), new ArrayList<CollectableEntityField>());

		// select the previously selected fields according to user preferences:
		clctctl.fields.moveToSelectedFields(getSelectedFieldsFromPreferences(clcte, clctctl));
	}
	
	/**
	 * initializes the <code>fields</code> field, while setting selected columns.
	 * @param clcte
	 * @param clctctl
	 * @param lstSelectedNew
	 * @param lstFixedNew
	 * @param lstColumnWiths
	 */
	public void initializeFields(final ResultController.Fields fields, final CollectController<Clct> clctctl, final List<CollectableEntityField> lstSelectedNew, 
			final List<CollectableEntityField> lstFixedNew, final Map<CollectableEntityField,Integer> lstColumnWiths) {		
		clctctl.fields.setSelectedFields(lstSelectedNew);
	}

	protected void setVisibleTable(boolean visibility){
		getResultTable().setVisible(visibility);
	}
	
	/**
	 * tries to read the selected fields from the preferences, making sure they contain at least one field.
	 * @param clcte
	 * @return List<CollectableEntityField> the selected fields from the user preferences.
	 * @postcondition !result.isEmpty()
	 */
	@SuppressWarnings("unchecked")
	private List<CollectableEntityField> getSelectedFieldsFromPreferences(CollectableEntity clcte, CollectController<Clct> clctctl) {
		final List<CollectableEntityField> result = (List<CollectableEntityField>) clctctl.readSelectedFieldsFromPreferences(clcte);

		clctctl.makeSureSelectedFieldsAreNonEmpty(clcte, result);

		// Here we have at least one field as selected column:
		assert !result.isEmpty();
		return result;
	}
	
	/**
	 *
	 * @param table for compatibility with old code the tabel is geven to this method
	 * 			could be removed later
	 */
	protected void setupTableCellRenderers(JTable table) {
		int iRowHeight = SubForm.MIN_ROWHEIGHT;
		// setup a table cell renderer for each column:
		for (Enumeration<TableColumn> enumeration = table.getColumnModel().getColumns(); enumeration.hasMoreElements();) {
			final TableColumn column = enumeration.nextElement();
			final CollectableEntityField clctef = ((CollectableEntityFieldBasedTableModel) table.getModel()).getCollectableEntityField(column.getModelIndex());
			final CollectableComponent clctcomp = CollectableComponentFactory.getInstance().newCollectableComponent(clctef, null, false);

			final TableCellRenderer renderer = clctcomp.getTableCellRenderer();
			iRowHeight = Math.max(iRowHeight, renderer.getTableCellRendererComponent(table, CollectableUtils.getNullField(clctef), true, true, 0, 0).getPreferredSize().height);
			column.setCellRenderer(renderer);
		}
		table.setRowHeight(iRowHeight);
	}


	/**
	 * sets all column widths to user prefs; set optimal width if no preferences yet saved
	 * Copied from the SubFormController
	 * @param tblResult
	 */
	protected void setColumnWidths(JTable tblResult, boolean bUseCustomColumnWidths, Preferences prefs) {
		//log.debug("setColumnWidths");

		final List<Integer> lstWidthsFromPreferences =  this.getTableColumnWidthsFromPreferences(prefs);
		//System.out.println(tblResult.getColumnCount()+ " " + tblResult.getModel().getColumnCount() + " " +  lstWidthsFromPreferences.size());
		bUseCustomColumnWidths = !lstWidthsFromPreferences.isEmpty() && lstWidthsFromPreferences.size() == tblResult.getColumnCount();
		if (bUseCustomColumnWidths) {
			Logger.getLogger(this.getClass()).debug("Restoring column widths from user preferences");
			assert(lstWidthsFromPreferences.size() == tblResult.getColumnCount());
			final Enumeration<TableColumn> enumeration = tblResult.getColumnModel().getColumns();
			int iColumn = 0;
			while (enumeration.hasMoreElements()) {
				final TableColumn column = enumeration.nextElement();
				final int iPreferredCellWidth = lstWidthsFromPreferences.get(iColumn++);
				column.setPreferredWidth(iPreferredCellWidth);
				column.setWidth(iPreferredCellWidth);
			}
		}
		else {
			// If there are no stored field widths or the number of stored field widths differs from the column count
			// (that is, the number of columns has changed since the last invocation of the client), set optimal column widths:
			Logger.getLogger(this.getClass()).debug("Setting optimal column widths");
			TableUtils.setOptimalColumnWidths(tblResult);
//			assert !bUseCustomColumnWidths;
			// use custom column widths as soon as a column width was changed after setting the optimal column width:
		}

//		tblResult.revalidate();
	}

	/**
	 * @return the table columns widths. If there are stored user preferences, the sizes will be restored.
	 * Size and order of list entries is determined by number and order of visible columns
	 */
	private List<Integer> getTableColumnWidthsFromPreferences(Preferences prefs) {
		List<Integer> result;
		try {
			result = PreferencesUtils.getIntegerList(prefs, CollectController.PREFS_NODE_SELECTEDFIELDWIDTHS);
		}
		catch (PreferencesException ex) {
			result = new ArrayList<Integer>();
		}

		return result;
	}

	/**
	 * writes the widths of the selected columns (fields) to the user preferences.
	 * @param prefs
	 */
	protected void writeFieldWidthsToPreferences(Preferences prefs) throws PreferencesException {
		final List<Integer> lstFieldWidths = CollectableTableHelper.getColumnWidths(getResultTable());
		PreferencesUtils.putIntegerList(prefs, CollectController.PREFS_NODE_SELECTEDFIELDWIDTHS, lstFieldWidths);
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

	protected void addDoubleClickMouseListener(MouseListener mouselistener) {
		tblResult.addMouseListener(mouselistener);
	}

	protected void removeDoubleClickMouseListener(MouseListener mouselistener) {
		tblResult.removeMouseListener(mouselistener);
	}

	protected void addPopupMenuListener() {
		// popup menu for rows:
		tblResult.addMouseListener(new PopupMenuRowsListener(popupmenuRow, tblResult));
	}


	public void addColumnModelListener(TableColumnModelListener tblcolumnlistener) {
		tblResult.getColumnModel().addColumnModelListener(tblcolumnlistener);
	}

	/** called when a column was moved in the header*/
	public void columnMovedInHeader(Fields fields) {
		fields.setSelectedFields(CollectableTableHelper.getCollectableEntityFieldsFromColumns(this.tblResult));
	}

	/**
	 * Popup menu for the columns in the Result table.
	 */
	protected static abstract class TableHeaderColumnPopupListener extends AbstractJPopupMenuListener {

		private Point ptLastOpened;
		public JPopupMenu popupmenuColumn = new JPopupMenu();
		public JTableHeader usedHeader;

		public TableHeaderColumnPopupListener(final JTableHeader aUsedHeader) {
			super();

			this.usedHeader = aUsedHeader;
			this.popupmenuColumn.add(createHideColumnItem(usedHeader));
		}

		private JMenuItem createHideColumnItem(final JTableHeader usedHeader) {
			final JMenuItem miPopupHideThisColumn = new JMenuItem(CommonLocaleDelegate.getMessage("ResultPanel.6","Diese Spalte ausblenden"));
			miPopupHideThisColumn.setIcon(Icons.getInstance().getIconRemoveColumn16());
			miPopupHideThisColumn.addActionListener(new ActionListener() {
				@Override
                public void actionPerformed(ActionEvent ev) {
					final int iColumnIndex = usedHeader.columnAtPoint(getLatestOpenPoint());
					removeColumnVisibility(usedHeader.getColumnModel().getColumn(iColumnIndex));
				}
			});

			return miPopupHideThisColumn;
		}

		@Override
		protected final JPopupMenu getJPopupMenu(MouseEvent ev) {
			this.ptLastOpened = ev.getPoint();
			return popupmenuColumn;
		}

		public abstract List<? extends CollectableEntityField> getSelectedFields();

		/**
		 * the point where the popup menu was opened latest
		 */
		public Point getLatestOpenPoint() {
			return this.ptLastOpened;
		}

		protected abstract void removeColumnVisibility(TableColumn column);

	}  // inner class PopupMenuColumnListener
	
	protected void setupCopyAction() {
	}
	
}  // class ResultPanel
