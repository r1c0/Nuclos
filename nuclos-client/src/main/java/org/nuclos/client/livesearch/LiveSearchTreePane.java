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
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.nuclos.common.collection.Factories;
import org.nuclos.common.collection.LazyInitMapWrapper;
import org.nuclos.common.dal.vo.EntityMetaDataVO;

class LiveSearchTreePane extends JPanel {

	private static final int    ROW_HEIGHT = 48;
	
	private List<LiveSearchResultPaneListener>   listeners;
	private JXTreeTable                    treeTable;
	private ResultTreeTableModel           treeModel;
	
	public LiveSearchTreePane() {
		listeners = new ArrayList<LiveSearchResultPaneListener>();
		
		setLayout(new BorderLayout());
		
		treeModel = new ResultTreeTableModel();
		treeTable = new JXTreeTable(treeModel);
		treeTable.setRowHeight(ROW_HEIGHT);
		treeTable.setTableHeader(null);
		treeTable.setTreeCellRenderer(new LabelIconRenderer());
		
		TableColumn treeColumn = treeTable.getColumnModel().getColumn(0);
		treeColumn.setMinWidth(80);
		treeColumn.setPreferredWidth(80);
		treeColumn.setMaxWidth(80);
		treeColumn.setWidth(80);
		
		TableColumn resColumn = treeTable.getColumnModel().getColumn(1);
		resColumn.setMinWidth(200);
		resColumn.setPreferredWidth(380);
		resColumn.setMaxWidth(Short.MAX_VALUE);
		resColumn.setWidth(380);
		
		treeTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		
		JScrollPane scrollPane = new JScrollPane(treeTable);
		scrollPane.setPreferredSize(new Dimension(500, 500));
		add(scrollPane, BorderLayout.CENTER);
	}
	
	// -- Interface called from LiveSearchActuion
	public void setResultData(List<LiveSearchResultRow> rows) {
		treeModel.setData(rows);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				TableColumn treeColumn = treeTable.getColumnModel().getColumn(0);
				treeColumn.setMinWidth(80);
				treeColumn.setPreferredWidth(80);
				treeColumn.setMaxWidth(80);
				treeColumn.setWidth(80);
				treeTable.expandAll();
			}
		});
	}
	
	public void addResultData(List<LiveSearchResultRow> rows) {
		//resModel.addData(rows);
	}
	
	public void removeResultData(List<LiveSearchResultRow> rows) {
		//resModel.removeData(rows);
	}
	// -- to here
	
	public void addLiveSearchPaneListener(LiveSearchResultPaneListener l) {
		listeners.add(l);
	}
	
	public void removeLiveSearchPaneListener(LiveSearchResultPaneListener l) {
		listeners.remove(l);
	}

	
	private class FolderNode {
		public final EntityMetaDataVO        entityMeta;
		public final Icon                    icon;
		
		public FolderNode(LiveSearchResultRow r) {
			entityMeta = r.entityMeta;
			icon = r.icon;
		}

		@Override
        public int hashCode() {
			return entityMeta.getEntity().hashCode();
        }

		@Override
        public boolean equals(Object obj) {
	        if(this == obj)
		        return true;
	        if(obj == null)
		        return false;
	        if(getClass() != obj.getClass())
		        return false;
	        FolderNode other = (FolderNode) obj;
	        return other.entityMeta.getEntity().equals(entityMeta.getEntity());
        }
	
		@Override
		public String toString() {
			return entityMeta.getEntity();
		}
	}
	
	// Module as folder | result as html-text
	private class ResultTreeTableModel extends AbstractTreeTableModel {
		private final Object ROOT = new Object();
		
		private List<LiveSearchResultRow> data;
		private Map<FolderNode, ArrayList<LiveSearchResultRow>> structLookup;
		private List<FolderNode> folders;
		
		public ResultTreeTableModel() {
			setData(Collections.<LiveSearchResultRow>emptyList());
		}
		
		public void setData(List<LiveSearchResultRow> rows) {
			data = rows;
			structLookup = new LazyInitMapWrapper<FolderNode, ArrayList<LiveSearchResultRow>>(
				new LinkedHashMap<FolderNode, ArrayList<LiveSearchResultRow>>(),
				Factories.cloneFactory(new ArrayList<LiveSearchResultRow>()));
			for(LiveSearchResultRow r : data)
				structLookup.get(new FolderNode(r)).add(r);
			
			folders = new ArrayList<FolderNode>(structLookup.keySet());
			
			TreePath p = new TreePath(ROOT);
			for(TreeModelListener l : getTreeModelListeners())
				l.treeStructureChanged(
					new TreeModelEvent(this, p));
		}
		
		@Override
        public Object getRoot() {
	        return ROOT;
        }

		@Override
        public int getColumnCount() {
	        return 2;
        }

		@Override
        public Object getValueAt(Object node, int column) {
			if(node == ROOT) {
				return null;
			}
			else if(node instanceof FolderNode) {
				FolderNode fnode = (FolderNode) node;
				switch(column) {
				case 0:
					return node;
				case 1: {
					int count = structLookup.get(node).size();
					String res;
					if(count == 0)         res = "Ergebnisse";
					else if(count == 1)    res = "Ergebnis";
					else                   res = "Ergebnisse";
					return "<html><i>" + count + " " + res + " in <b>" + fnode.entityMeta.getEntity() + "</b></i></html>";
				}
				}
			}
			else if(node instanceof LiveSearchResultRow && column == 1) {
				LiveSearchResultRow row = (LiveSearchResultRow) node;
				switch(column) {
				case 0:
					return row.icon;
				case 1:
					return row.description;
				}
			}
			return null;
        }

		@Override
        public Object getChild(Object parent, int index) {
			if(parent == ROOT)
				return folders.get(index);
			else if(parent instanceof FolderNode)
				return structLookup.get(parent).get(index);
	        return null;
        }

		@Override
        public int getChildCount(Object parent) {
			if(parent == ROOT)
				return folders.size();
			else if(parent instanceof FolderNode)
				return structLookup.get(parent).size();
	        return 0;
        }

		@Override
        public int getIndexOfChild(Object parent, Object child) {
			if(parent == ROOT)
				return folders.indexOf(child);
			else if(parent instanceof FolderNode)
				return structLookup.get(parent).indexOf(child);
	        return -1;
        }
	}
	
	private class LabelIconRenderer extends DefaultTreeCellRenderer {

		public LabelIconRenderer() {
		}

		@Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			JLabel c = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			if(value instanceof LiveSearchResultRow) {
				c.setIcon(((LiveSearchResultRow) value).icon);
				c.setText(null);
			}
			else if(value instanceof FolderNode) {
				c.setIcon(((FolderNode) value).icon);
				//c.setText("<html><small><b>" + ((FolderNode) value).entityMeta.getEntity() + "</b></small></html>");
				c.setText(null);

				c.setHorizontalAlignment(JLabel.CENTER);
				c.setVerticalTextPosition(JLabel.BOTTOM);
				c.setHorizontalTextPosition(JLabel.CENTER);
			}
			return c;
        }
	}
}
