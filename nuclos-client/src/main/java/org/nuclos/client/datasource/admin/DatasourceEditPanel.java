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
package org.nuclos.client.datasource.admin;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.client.datasource.querybuilder.QueryBuilderEditor;
import org.nuclos.client.datasource.querybuilder.gui.ParameterPanel;
import org.nuclos.client.gef.editor.syntax.JEditTextArea;
import org.nuclos.client.gef.editor.syntax.tokenmarker.TSQLTokenMarker;
import org.nuclos.client.genericobject.ReportController;
import org.nuclos.client.ui.CommonClientWorker;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.server.report.valueobject.ResultColumnVO;
import org.nuclos.server.report.valueobject.ResultVO;

/**
 * Panel for Datasource Editor.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:lars.rueckemann@novabit.de">Lars Rueckemann</a>
 * @version 01.00.00
 */
public class DatasourceEditPanel extends JPanel {

	private final DatasourceEditController controller;
	private final DatasourceHeaderPanel pnlHeader;
	private final QueryBuilderEditor pnlQueryEditor;
	private final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	public final SqlPanel sqlPanel;
	private final PreviewPanel previewPanel = new PreviewPanel();
	private boolean bModelUsed = true;

	/* constants for tabbed pane index */
	public static final int INDEX_MODEL = 0;
	public static final int INDEX_SQL = 1;
	public static final int INDEX_PREVIEW = 2;
	
	private Map<String, JComponent> tabs = new HashMap<String, JComponent>();

	public DatasourceEditPanel(DatasourceEditController controller,
			CollectableEntityField clctefName,
			CollectableEntityField clctefEntity,
			CollectableEntityField clctefDescription) {
		super(new BorderLayout());
		pnlQueryEditor = new QueryBuilderEditor(this, 
			controller.getQueryTypes(), 
			controller.isWithParameterEditor(),
			controller.isParameterEditorWithValuelistProviderColumn(),
			controller.isParameterEditorWithLabelColumn());
		sqlPanel = new SqlPanel(controller.isWithParameterEditor(),
			controller.isParameterEditorWithValuelistProviderColumn(),
			controller.isParameterEditorWithLabelColumn());
		
		this.controller = controller;
		this.pnlHeader = new DatasourceHeaderPanel(clctefName, clctefEntity, clctefDescription);
		this.init();
	}

	private void init() {
		add(pnlHeader, BorderLayout.NORTH);
		final SpringLocaleDelegate localeDelegate = SpringLocaleDelegate.getInstance();
		tabbedPane.addTab(localeDelegate.getMessage("DatasourceEditPanel.10","Modell"), null, pnlQueryEditor, 
				localeDelegate.getMessage("DatasourceEditPanel.8","Grafische Modellierung der Abfrage"));
		tabbedPane.addTab(localeDelegate.getMessage("DatasourceEditPanel.11","SQL"), null, sqlPanel, 
				localeDelegate.getMessage("DatasourceEditPanel.12","SQL-Statement der Datenbankabfrage"));
		tabbedPane.addTab(localeDelegate.getMessage("DatasourceEditPanel.18","Vorschau"), null, previewPanel, 
				localeDelegate.getMessage("DatasourceEditPanel.6","Ergebnis-Ansicht"));
		add(tabbedPane, BorderLayout.CENTER);
		tabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				final int iTabIndex = ((JTabbedPane) e.getSource()).getSelectedIndex();
				if (iTabIndex == DatasourceEditPanel.INDEX_SQL) {
					
					if (bModelUsed) {
						UIUtils.runCommand(DatasourceEditPanel.this, new CommonRunnable() {
							@Override
							public void run() throws CommonBusinessException {
								try {
								sqlPanel.setSql(controller.generateSql());
								}
								catch (CommonBusinessException ex) {
									Errors.getInstance().showExceptionDialog(controller.getTab(), ex);
								}
							}
						});
					}
				}
				else if (iTabIndex == DatasourceEditPanel.INDEX_PREVIEW) {

				}
			}
		});
	}
	
	DatasourceHeaderPanel getHeader() {
		return pnlHeader;
	}
	
	QueryBuilderEditor getQueryEditor() {
		return pnlQueryEditor;
	}
	
	/**
	 *
	 * @return true if editing could be stopped
	 */
	public boolean stopEditing() {
		return pnlQueryEditor.stopEditing() &
				(sqlPanel.getParameterPanel() == null ? true : sqlPanel.getParameterPanel().stopEditing());
	}


	public String getDatasourceName() {
		return this.pnlHeader.getNameField().getJTextField().getText();
	}
	
	public String getEntity() {
		if (this.pnlHeader.getEntityComboBox() != null) {
			return this.pnlHeader.getEntityComboBox().getJComboBox().getSelectedItem().toString();
		}
		return null;
	}

	public String getDatasourceDescription() {
		return this.pnlHeader.getDescriptionField().getJTextField().getText();
	}

	public void setDatasourceName(String name) {
		this.pnlHeader.getNameField().getJTextField().setText(name);
	}
	
	public void setEntity(String entity) {
		if (entity != null) {
			for (int i = 0; i < pnlHeader.getEntityComboBox().getJComboBox().getItemCount(); i++) {
				if (entity.equals(pnlHeader.getEntityComboBox().getJComboBox().getItemAt(i).toString())) {
					pnlHeader.getEntityComboBox().getJComboBox().setSelectedIndex(i);
					return;
				}
			}
		}
		pnlHeader.getEntityComboBox().getJComboBox().setSelectedIndex(0);
	}

	public void setDatasourceDescription(String description) {
		pnlHeader.getDescriptionField().getJTextField().setText(description);
	}

	/**
	 * set tabs active or inactive and clean the preview panel
	 *
	 */
	public void refreshView() {
		if (isModelUsed()) {
			changeTab(DatasourceEditPanel.INDEX_MODEL);
			setTabEnabled(DatasourceEditPanel.INDEX_MODEL, true);
		}
		else {
			changeTab(DatasourceEditPanel.INDEX_SQL);
			setTabEnabled(DatasourceEditPanel.INDEX_MODEL, false);
		}
		previewPanel.reset();
	}

	/**
	 * @return the sql statement from the model or from the editor
	 */
	public String getSql() {
		String sSql = null;
		if (bModelUsed) {
			try {
				sSql = controller.generateSql();
			}
			catch (CommonBusinessException ex) {
				Errors.getInstance().showExceptionDialog(controller.getTab(), ex);
			}
		}
		else {
			sSql = sqlPanel.getSql();
		}
		return sSql;
	}

	/**
	 * enable / disable the specified tab
	 * @param iIndex
	 * @param bTabsEnabled
	 */
	public void setTabEnabled(final int iIndex, final boolean bTabsEnabled) {
		if (iIndex >= 0 && iIndex < tabbedPane.getTabCount()) {
			tabbedPane.setEnabledAt(iIndex, bTabsEnabled);
		}
	}

	/**
	 * change to specified tab
	 * @param iIndex
	 */
	public void changeTab(final int iIndex) {
		if (iIndex >= 0 && iIndex < tabbedPane.getTabCount()) {
			tabbedPane.setSelectedIndex(iIndex);
		}
	}

	/**
	 * set the sql String of the editor
	 * @param sSql
	 */
	public void setSql(String sSql) {
		this.sqlPanel.setSql(sSql);
	}

	/**
	 *
	 * @return true if model is used, false if sql string is used
	 */
	public boolean isModelUsed() {
		return bModelUsed;
	}

	/**
	 * set the bModelUsed flag
	 * @param isModelUsed
	 */
	public void setIsModelUsed(final boolean isModelUsed) {
		this.bModelUsed = isModelUsed;
		if (isModelUsed) {
			sqlPanel.unedit();
		}
		else {
			sqlPanel.edit();
		}
	}
	
	public void addComponent(String resourceId, JComponent c) {
		tabs.put(resourceId, c);
	}

	class SqlPanel extends JPanel {
		private ParameterPanel parameterpanel;
		private final boolean blnWithParameterPanel;
		private final boolean blnWithValuelistProviderColumn;
		private final boolean blnWithParameterLabelColumn;
		
		private final JEditTextArea sqlEditor = new JEditTextArea();
		private final JPanel sqlToolBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
		private final JButton btnGenerateSql = new JButton(SpringLocaleDelegate.getInstance().getMessage(
				"DatasourceEditPanel.13","Statement bearbeiten"));
		private FocusListener focusListener = new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				controller.detailsChanged(sqlEditor);
			}

			@Override
			public void focusLost(FocusEvent e) {
			}
		};

		SqlPanel(boolean blnWithParameterPanel, 
				boolean blnWithValuelistProviderColumn,
				boolean blnWithParameterLabelColumn) {
			this.blnWithParameterPanel = blnWithParameterPanel;
			this.blnWithValuelistProviderColumn = blnWithValuelistProviderColumn;
			this.blnWithParameterLabelColumn = blnWithParameterLabelColumn;
			init();
		}

		private void init() {
			this.setLayout(new BorderLayout());
			//header Panel
			btnGenerateSql.setToolTipText("");
			btnGenerateSql.setMnemonic('S');
			btnGenerateSql.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ev) {
					UIUtils.runCommand(controller.getTab(), new Runnable() {
						@Override
						public void run() {
							try {
								if (btnGenerateSql.isSelected()) {
									unedit();
									setSql(controller.generateSql());
								}
								else {
									edit();
								}
								controller.detailsChanged(sqlEditor);
							}
							catch (CommonBusinessException ex) {
								Errors.getInstance().showExceptionDialog(controller.getTab(), ex);
							}
						}
					});
				}
			});

			sqlToolBar.add(btnGenerateSql);
			this.add(sqlToolBar, BorderLayout.NORTH);

			

			sqlEditor.setTokenMarker(new TSQLTokenMarker());
			sqlEditor.setText("");
			sqlEditor.setCaretPosition(0);
			sqlEditor.setEditable(!bModelUsed);
			sqlEditor.setFont(new Font("Courier New", Font.PLAIN, 12));
			
			//content Panel
			if (blnWithParameterPanel) {
				final JSplitPane splitpn = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
				splitpn.setOneTouchExpandable(true);
				UIUtils.clearKeymaps(splitpn);
				//splitpn.setResizeWeight(0.72);
				
				splitpn.setRightComponent(sqlEditor);
	
				parameterpanel = new ParameterPanel(blnWithValuelistProviderColumn, blnWithParameterLabelColumn);
				parameterpanel.setParameterModel(pnlQueryEditor.getTableSelectionPanel().getParameterPanel().getParameterModel());
				
				splitpn.setLeftComponent(parameterpanel);
				splitpn.setDividerLocation(250);
				this.add(splitpn, BorderLayout.CENTER);
			} else {
				this.add(sqlEditor, BorderLayout.CENTER);
			}
		}

		public ParameterPanel getParameterPanel() {
			return parameterpanel;
		}

		public JButton getBtnGenerateSql(){
			return btnGenerateSql;
		}
		/**
		 * @return the sql string from the editor
		 */
		public String getSql() {
			return sqlEditor.getText();
		}

		/**
		 * set sql string in the editor
		 * @param sSql
		 */
		public void setSql(String sSql) {
			sqlEditor.setText(sSql);
		}

		/**
		 * change to edit mode: bModelUsed is set to false
		 *
		 */
		public void edit() {
			btnGenerateSql.setText(
					SpringLocaleDelegate.getInstance().getMessage(
							"DatasourceEditPanel.15","Statement wieder herstellen"));
			btnGenerateSql.setToolTipText(
					SpringLocaleDelegate.getInstance().getMessage(
							"DatasourceEditPanel.16","Stellt das SQL-Statement aus dem Modell wieder her."));
			btnGenerateSql.setSelected(true);
			sqlEditor.addFocusListener(focusListener);
			sqlEditor.setEditable(true);
			bModelUsed = false;
			setTabEnabled(DatasourceEditPanel.INDEX_MODEL, false);
		}

		/**
		 * change to model mode: bModelUsed is set to true
		 *
		 */
		public void unedit() {
			btnGenerateSql.setText(
					SpringLocaleDelegate.getInstance().getMessage("DatasourceEditPanel.14","Statement bearbeiten"));
			btnGenerateSql.setToolTipText("");
			btnGenerateSql.setSelected(false);
			sqlEditor.setEditable(false);
			sqlEditor.removeFocusListener(focusListener);
			bModelUsed = true;
			setTabEnabled(DatasourceEditPanel.INDEX_MODEL, true);
		}
	}

	class PreviewPanel extends JPanel {
		private final JPanel pnlOptions = new JPanel(new FlowLayout(FlowLayout.LEFT));
		private final JLabel lbMaxRowCount = new JLabel(SpringLocaleDelegate.getInstance().getMessage(
				"DatasourceEditPanel.3","Anzahl Zeilen: "));
		private final JTextField tfMaxRowCount = new JTextField("500", 5);
		private final JCheckBox cbTruncRows = new JCheckBox();
		private final JButton btnPreview = new JButton(SpringLocaleDelegate.getInstance().getMessage(
				"DatasourceEditPanel.4","Anzeigen"));
		private final JButton btnExport = new JButton(SpringLocaleDelegate.getInstance().getMessage(
				"DatasourceEditPanel.7","Exportieren"));
		private JTable table = new JTable();
		private final JScrollPane scrPane = new JScrollPane();

		PreviewPanel() {
			init();
		}

		private void init() {
			final SpringLocaleDelegate localeDelegate = SpringLocaleDelegate.getInstance();
			this.setLayout(new BorderLayout());
			//Options
			cbTruncRows.setSelected(true);
			cbTruncRows.setText(localeDelegate.getMessage("DatasourceEditPanel.9","Maximale Anzahl Zeilen einschr\u00e4nken"));
			cbTruncRows.setToolTipText(localeDelegate.getMessage("DatasourceEditPanel.17","Steuert, ob die Anzahl der Zeilen in der Ergebnisliste eingeschr\u00e4nkt werden."));
			cbTruncRows.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (cbTruncRows.isSelected()) {
						lbMaxRowCount.setVisible(true);
						tfMaxRowCount.setVisible(true);
					}
					else {
						lbMaxRowCount.setVisible(false);
						tfMaxRowCount.setVisible(false);
					}
				}
			});
			tfMaxRowCount.setToolTipText(localeDelegate.getMessage("DatasourceEditPanel.5","Die Anzahl der Zeilen, auf die das Ergebnis eingeschr\u00e4nkt wird."));

			btnPreview.setToolTipText(localeDelegate.getMessage("DatasourceEditPanel.1","Abfrage ausf\u00fchren"));
			btnPreview.setMnemonic('A');
			btnPreview.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ev) {
					UIUtils.runCommand(controller.getTab(), new Runnable() {
						@Override
						public void run() {
							cmdPreview();
						}
					});
				}
			});

			btnExport.setEnabled(false);
			btnExport.setToolTipText(localeDelegate.getMessage("DatasourceEditPanel.2","Angezeigtes Ergebnis in eine Datei exportieren"));
			btnExport.setMnemonic('E');
			btnExport.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ev) {
					UIUtils.runCommand(controller.getTab(), new Runnable() {
						@Override
						public void run() {
							cmdExport();
						}
					});
				}
			});

			pnlOptions.add(btnPreview);
			pnlOptions.add(btnExport);
			pnlOptions.add(cbTruncRows);
			pnlOptions.add(lbMaxRowCount);
			pnlOptions.add(tfMaxRowCount);
			this.add(pnlOptions, BorderLayout.NORTH);

			//table
			this.add(scrPane, BorderLayout.CENTER);
		}

		/**
		 * clears this view
		 */
		public void reset() {
			table = new JTable();
			scrPane.setViewportView(table);
			btnExport.setEnabled(false);
		}

		/**
		 * execute the statement and show result
		 */
		private void cmdPreview() {
			controller.execute(new CommonClientWorker() {
				Integer iMaxRowCount = null;
				ResultVO resultvo = null;
				
				@Override
				public void init() throws CommonBusinessException {
					if (cbTruncRows.isSelected()) {
						iMaxRowCount = new Integer(tfMaxRowCount.getText());
					}
				}

				@Override
				public void work() throws CommonBusinessException {
					resultvo = controller.cmdExecuteCurrentStatement(iMaxRowCount);
				}

				@Override
				public void paint() throws CommonBusinessException {
					if (resultvo != null) {
						table = new JTable(new ReadOnlyTableModel(resultvo));
						table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
						scrPane.setViewportView(table);
						btnExport.setEnabled(true);
					}
				}

				@Override
				public JComponent getResultsComponent() {
					return null;
				}

				@Override
				public void handleError(Exception ex) {
				}
			});
		}

		/**
		 * export current result
		 */
		void cmdExport() {
			try {
				new ReportController(controller.getTab()).export(table, getDatasourceName());
			}
			catch (CommonBusinessException ex) {
				Errors.getInstance().showExceptionDialog(this.getParent(), ex);
			}
		}
	}

	/**
	 *
	 * A read only table model.
	 * <br>
	 * <br>Created by Novabit Informationssysteme GmbH
	 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 *
	 * @author	<a href="mailto:Lars.Rueckemann@novabit.de">Lars Rueckemann</a>
	 * @version 01.00.00
	 */
	private class ReadOnlyTableModel implements TableModel {

		private final List<TableModelListener> lstListeners = new ArrayList<TableModelListener>();

		private final ResultVO resultvo;

		ReadOnlyTableModel(ResultVO resultvo) {
			this.resultvo = resultvo;
		}

		@Override
		public int getColumnCount() {
			return resultvo.getColumns().size();
		}

		@Override
		public int getRowCount() {
			return resultvo.getRows().size();
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		@Override
		public Object getValueAt(int iRow, int iColumn) {
			final ResultColumnVO columnVO = resultvo.getColumns().get(iColumn);
			return columnVO.format(resultvo.getRows().get(iRow)[iColumn]);
		}

		@Override
		public void setValueAt(Object oValue, int iRow, int iColumn) {
			// do nothing because this is a read only model
		}

		@Override
		public String getColumnName(int columnIndex) {
			return resultvo.getColumns().get(columnIndex).getColumnLabel();
		}

		@Override
		public void addTableModelListener(TableModelListener l) {
			lstListeners.add(l);
		}

		@Override
		public void removeTableModelListener(TableModelListener l) {
			lstListeners.remove(l);
		}
	}

}	// class DatasourceEditPanel
