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

package org.nuclos.tools.dbsetup;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;

import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXTable;
import org.nuclos.client.ui.Icons;
import org.nuclos.common.collection.Pair;
import org.nuclos.server.autosync.AutoDbSetup;
import org.nuclos.server.dblayer.DbAccess;
import org.nuclos.server.dblayer.impl.util.PreparedString;
import org.nuclos.server.dblayer.statements.DbStatement;
import org.nuclos.server.dblayer.structure.DbArtifact;

@SuppressWarnings("serial")
public class StatementsFrame extends JXFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Color ERROR_COLOR = Color.RED;
	
	private final JComboBox versionComboBox;
	private final JXTable table;
	private final JTextArea editorPane;
	
	private final DbAccess dbAccess;
	private final AutoDbSetup autoSetup;
	private final StatementsTableModel stmtModel;
	private final JCheckBox stopOnFirstError;

	public StatementsFrame(String title, DbAccess dbAccess) {
		super("Nuclos DbSetupTool", true);
		
		this.dbAccess = dbAccess;
		this.autoSetup = new AutoDbSetup(dbAccess);
		
		setTitle("Nuclos DbSetupTool - " + autoSetup.getCurrentRelease().getReleaseString() + " - " + title);
		
		this.stmtModel = new StatementsTableModel();
		
		List<Pair<String, Date>> versions = autoSetup.getInstalledVersions();
		versions.add(null);
		versionComboBox = new JComboBox(versions.toArray());
		versionComboBox.setRenderer(new DefaultListCellRenderer() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			@SuppressWarnings("unchecked")
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				Pair<String, Date> p = (Pair<String, Date>) value;
				setText(p != null ? p.x + " (" + p.y + ")" : "(Install)");
				return this;
			}
		});
		versionComboBox.addActionListener(new ActionListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent e) {
				selectStartVersion((Pair<String, Date>) versionComboBox.getSelectedItem());
			}
		});
		versionComboBox.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		versionComboBox.setMaximumSize(versionComboBox.getPreferredSize());
		
		stopOnFirstError = new JCheckBox("Stop on first error");
		stopOnFirstError.setSelected(true);
		
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		JLabel label = new JLabel("From:");
		label.setLabelFor(versionComboBox);
		toolBar.add(label);
		toolBar.add(versionComboBox);
		toolBar.addSeparator();
		toolBar.add(new AbstractAction("Run Selected") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				executeStatements(table.getSelectedRows());
			}
		});
		toolBar.add(new AbstractAction("Run All") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				int[] all = new int[table.getRowCount()];
				for (int i = 0; i < all.length; i++)
					all[i] = i;
				executeStatements(all);
			}
		});
		toolBar.add(stopOnFirstError);
		toolBar.addSeparator();
		toolBar.add(new AbstractAction("View Metadata") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				Collection<DbArtifact> metaData = StatementsFrame.this.dbAccess.getAllMetaData();
				MetaDataFrame frame = new MetaDataFrame("Metadata", metaData);
				frame.pack();
				frame.setVisible(true);
			}
		});
		toolBar.add(Box.createHorizontalGlue());
		
		table = new JXTable();
		table.setModel(stmtModel);
		table.getColumn(0).setPreferredWidth(50);
		table.getColumn(1).setPreferredWidth(300);
		table.getColumn(2).setPreferredWidth(150);
		table.getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			protected void setValue(Object value) {
				super.setValue(value);
				if (value instanceof Exception) {
					setIcon(Icons.getInstance().getIconJobError());
					setText(((Exception) value).getMessage());
				} else {
					setIcon(value != null ? Icons.getInstance().getIconJobSuccessful() : null);
				}
			}
		});
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				showDetails(table.getSelectedRow());
			}
		});
		
		editorPane = new JTextArea(10, 60);
		JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
			new JScrollPane(table),
			new JScrollPane(editorPane));
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(sp);
		
		getContentPane().add(toolBar, BorderLayout.NORTH);

		selectStartVersion(versions.get(0));
	}
	
	protected void selectStartVersion(Pair<String, Date> p) {
		List<DbStatement> setupStatements;
		if (p == null) {
			setupStatements = autoSetup.getSetupStatements();
		} else {
			setupStatements = autoSetup.getUpdateStatementsSince(p.x);
		}
		stmtModel.setStatements(setupStatements);
	}
	
	protected void executeStatements(int[] rows) {
		boolean stopOnFirstError = this.stopOnFirstError.isSelected();
		for (int i : rows) {
			DbStatement stmt = stmtModel.getStatements()[i];
			try {
				int updates = dbAccess.execute(stmt);
				stmtModel.setSuccess(i, "ok (" + updates + ")");
			} catch (Exception ex) {
				stmtModel.setSuccess(i, ex);
				showDetails(i);
				if (stopOnFirstError)
					break;
			} finally {
			}
		}		
	}
	
	protected void showDetails(int row) {
		String text = "";
		if (table.getSelectedRow() != -1) {
			DbStatement stmt = stmtModel.getStatement(table.getSelectedRow());
			text = getSqlStrings(stmt);
			
			Object success = stmtModel.getSuccess(table.getSelectedRow());
			if (success instanceof Exception) {
				StringWriter w = new StringWriter();
				((Exception) success).printStackTrace(new PrintWriter(w));
				text += "-- Exception\n" + w.toString();
				editorPane.setForeground(ERROR_COLOR);
			} else {
				editorPane.setForeground(null);
			}
		}
		
		editorPane.setText(text);
		editorPane.select(0, 0);
	}


	private String getSqlStrings(DbStatement stmt) {
		StringBuilder sb = new StringBuilder();
		for (PreparedString ps : dbAccess.getPreparedSqlFor(stmt)) {
			if (ps.hasParameters())
				sb.append("-- Parameters: " + Arrays.toString(ps.getParameters()) + "\n");
			sb.append(ps);
			sb.append("\n\n");
		}
		return sb.toString();
	}	
}
