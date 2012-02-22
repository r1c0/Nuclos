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
package org.nuclos.client.livesearch;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import org.nuclos.client.ui.event.PopupMenuMouseAdapter;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common2.SpringLocaleDelegate;

class LiveSearchPane extends JPanel {

	private static final int    ROW_HEIGHT = 48;
	
	private ResultTableModel	resModel;
	private JTable	            resTable;
	
	private List<LiveSearchResultPaneListener>   listeners;
	
	public LiveSearchPane() {
		super(new BorderLayout());

		listeners = new ArrayList<LiveSearchResultPaneListener>();
		
		resModel = new ResultTableModel();
		resTable = new JTable(resModel);
		for(ResultTableColDef cd : ResultTableColDef.values()) {
			TableColumn tc = resTable.getColumnModel().getColumn(cd.ordinal());
			tc.setWidth(cd.width);
			tc.setPreferredWidth(cd.width);
			if(cd.setMaxWidth)
				tc.setMaxWidth(cd.width);
			if(cd.colClass.equals(JComponent.class))
				tc.setCellRenderer(new ComponentRenderer());
			if(cd == ResultTableColDef.ICON_AND_TEXT)
				tc.setCellRenderer(new IconAndTextRenderer());
		}
		
		resTable.addMouseListener(doubleClickListener);
		resTable.setShowVerticalLines(false);
		resTable.setShowHorizontalLines(true);
		resTable.setRowHeight(ROW_HEIGHT);
		resTable.setTableHeader(null);
		
		add(new JScrollPane(resTable), BorderLayout.CENTER);
		
		// Recursively color everything in snow-white
		List<Component> q = new LinkedList<Component>();
		q.add(this);
		while(!q.isEmpty()) {
			Component c = q.remove(0);
			c.setBackground(Color.WHITE);
			c.setFocusable(false);
			if(c instanceof Container)
				CollectionUtils.addAll(q, ((Container) c).getComponents());
		}
		
		validate();
		
		addKeyListener(forwardAllListener);
		resTable.addKeyListener(tableKeyListener);
		resTable.addMouseListener(new PopupMenuMouseAdapter(resTable) {
			@Override
			public void doPopup(MouseEvent e) {
				List<LiveSearchResultRow> selected
					= CollectionUtils.indexedSelection(resModel.data, resTable.getSelectedRows());
				if(!selected.isEmpty()) {
					HashSet<String> entities = new HashSet<String>();
					for(LiveSearchResultRow row : selected)
						entities.add(row.entityName);
					
					JPopupMenu pop = new JPopupMenu();
					JMenuItem it = new JMenuItem(openMultiAction);
					JMenuItem det = new JMenuItem(openDetailsAction);
	
					if(entities.size() < selected.size()) // more than one of a type
						pop.add(it);
					pop.add(det);
					
					it.setFocusable(false);
					pop.setFocusable(false);
					
					if(resTable.getSelectedRows().length > 0)
						pop.show(resTable, e.getX(), e.getY());
				}
			}
		});
	}
	
	
	private KeyListener forwardAllListener = new KeyListener() {
		@Override
		public void keyTyped(KeyEvent e) {
			resTable.dispatchEvent(e);
		}
		
		@Override
		public void keyReleased(KeyEvent e) {
			resTable.dispatchEvent(e);
		}
		
		@Override
		public void keyPressed(KeyEvent e) {
			resTable.dispatchEvent(e);
		}
	};
	
	
    private KeyListener tableKeyListener = new KeyAdapter() {
    	@Override
    	public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_ENTER)
            	e.consume();
    	}
    	
        @Override
        public void keyReleased(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_ENTER) {
    			broadcastFunctionAction(CollectionUtils.indexedSelection(resModel.data, resTable.getSelectedRows()), Function.KB_OPEN);
            }
        }
    };
    
    private Action openMultiAction = new AbstractAction(SpringLocaleDelegate.getInstance().getResource(
    		"livesearch.reslist.open", "Open selected")) {

		@Override
		public void actionPerformed(ActionEvent e) {
			broadcastFunctionAction(CollectionUtils.indexedSelection(resModel.data, resTable.getSelectedRows()), Function.OPEN);
		}
	};

    private Action openDetailsAction = new AbstractAction(SpringLocaleDelegate.getInstance().getResource(
    		"livesearch.reslist.opendetails", "Open Details")) {

		@Override
		public void actionPerformed(ActionEvent e) {
			broadcastFunctionAction(CollectionUtils.indexedSelection(resModel.data, resTable.getSelectedRows()), Function.OPEN_DETAILS);
		}
	};

	
	// -- Interface called from LiveSearchActuion
	public void setResultData(List<LiveSearchResultRow> rows) {
		resModel.setData(rows);
		validate();
	}
	
	public void addResultData(List<LiveSearchResultRow> rows) {
		resModel.addData(rows);
	}
	
	public void removeResultData(List<LiveSearchResultRow> rows) {
		resModel.removeData(rows);
	}
	// -- to here
	
	public void addLiveSearchPaneListener(LiveSearchResultPaneListener l) {
		listeners.add(l);
	}
	
	public void removeLiveSearchPaneListener(LiveSearchResultPaneListener l) {
		listeners.remove(l);
	}
	
	private void broadcastFunctionAction(List<LiveSearchResultRow> rows, Function function) {
		for(LiveSearchResultPaneListener l : new ArrayList<LiveSearchResultPaneListener>(listeners))
	        l.functionAction(function, rows);
	}

	private enum ResultTableColDef {
		ICON_AND_TEXT("", Pair.class, 80, true) {
	        @Override
	        public Object readFromRow(LiveSearchResultRow row) {
		        return new Pair<Icon, String>(row.icon, row.entityLabel);
	        }
        },
		DESCRIPTION("Ergebnis", String.class, 400, false) {
	        @Override
	        public Object readFromRow(LiveSearchResultRow row) {
		        return row.description;
	        }
        };
		
		public final String title;
		public final Class<?> colClass;
		public final int width;
		public final boolean setMaxWidth;
		private ResultTableColDef(String title, Class<?> colClass, int width, boolean setMaxWidth) {
			this.title = title;
			this.colClass = colClass;
			this.width = width;
			this.setMaxWidth = setMaxWidth;
		}
		
		public abstract Object readFromRow(LiveSearchResultRow row);
	};
	
	private class ResultTableModel extends AbstractTableModel {

		private List<LiveSearchResultRow> data;
		
		public ResultTableModel() {
			this.data = new ArrayList<LiveSearchResultRow>();
		}
		
		public void setData(List<LiveSearchResultRow> rows) {
			data.clear();
			data.addAll(rows);
			fireTableDataChanged();
		}
		
		public void addData(List<LiveSearchResultRow> rows) {
			if(rows.isEmpty())
				return;
			int oldSize = data.size();
			data.addAll(rows);
			fireTableRowsInserted(oldSize, data.size());
        }

		public void removeData(List<LiveSearchResultRow> rows) {
			if(rows.isEmpty())
				return;
			for(LiveSearchResultRow row : rows) {
				int i = data.indexOf(row);
				if(i >= 0) {
					data.remove(i);
					fireTableRowsDeleted(i, i);
				}
			}
		}
		
		@Override
        public int getRowCount() {
	        return data.size();
        }

		@Override
        public int getColumnCount() {
	        return ResultTableColDef.values().length;
        }

		@Override
        public Object getValueAt(int rowIndex, int columnIndex) {
			LiveSearchResultRow rowObject = data.get(rowIndex);
			ResultTableColDef colDef = ResultTableColDef.values()[columnIndex];
	        return colDef.readFromRow(rowObject);
        }

		@Override
        public String getColumnName(int column) {
			ResultTableColDef colDef = ResultTableColDef.values()[column];
			return colDef.title;
        }


		@Override
        public Class<?> getColumnClass(int column) {
			ResultTableColDef colDef = ResultTableColDef.values()[column];
			return colDef.colClass;
        }
	}
	
	private class ComponentRenderer extends DefaultTableCellRenderer {

		@Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component component = (Component) value;
			component.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
			return component;
        }
	}
	
	private class IconAndTextRenderer extends DefaultTableCellRenderer {

		@Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Pair<Icon, String> p = (Pair<Icon, String>) value;
			
			String text = "<html><small><b>" + p.y + "</b></small></html>";
			
			JLabel c = (JLabel) super.getTableCellRendererComponent(table, text, isSelected, hasFocus, row, column);
			c.setHorizontalAlignment(JLabel.CENTER);
			c.setVerticalTextPosition(JLabel.BOTTOM);
			c.setHorizontalTextPosition(JLabel.CENTER);

			c.setIcon(p.x);
			return c;
        }
	}

	private MouseListener doubleClickListener = new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
			if(!e.isPopupTrigger() && e.getClickCount() >= 2) {
				JTable table = (JTable) e.getSource();
		    	int row = table.rowAtPoint(e.getPoint());
		    	if(row >= 0) {
		    		LiveSearchResultRow resRow = ((ResultTableModel) table.getModel()).data.get(row);
		    		broadcastFunctionAction(CollectionUtils.asList(resRow), Function.getDefaultFunction());
		    		e.consume();
		    	}
			}
		}
	};
}
