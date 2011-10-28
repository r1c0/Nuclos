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
package org.nuclos.client.relation;

import info.clearthought.layout.TableLayout;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common2.CommonLocaleDelegate;

public class EntityChoicePanel extends JPanel {
	
	private JTable tblEntities;
	private JScrollPane paneEntities;
	private EntityChoiceTableModel model;
	
	private JButton btnOk;
	private JButton btnSelectAll;
	private JButton btnSelectNon;
	
	private List<EntityMetaDataVO> lstEntites;
	private List<EntityMetaDataVO> lstEntitesExclude;
	private Map<EntityMetaDataVO, Boolean> mpEntites;
	private JDialog dialog;
	
	public EntityChoicePanel(JDialog dia, List<EntityMetaDataVO> lstExclude) {
		super();
		dialog = dia;
		lstEntitesExclude = lstExclude;
		lstEntites = new ArrayList<EntityMetaDataVO>();
		mpEntites = new HashMap<EntityMetaDataVO, Boolean>();
		loadEntites();
		init();	
	}
	
	public List<EntityMetaDataVO> getSelectedEntites() {
		List<EntityMetaDataVO> lst = new ArrayList<EntityMetaDataVO>();
		
		for(EntityMetaDataVO vo : mpEntites.keySet()) {
			if(mpEntites.get(vo))
				lst.add(vo);
		}
		
		return lst;
	}


	private void init() {
		double cells [][] = {{5, TableLayout.PREFERRED, 10}, {5, TableLayout.PREFERRED,5,30,5}};		
		this.setLayout(new TableLayout(cells));
		model = new EntityChoiceTableModel();
		tblEntities = new JTable(model);
		paneEntities = new JScrollPane(tblEntities);
		
		double cellsPanel [][] = {{5, TableLayout.PREFERRED,5,TableLayout.PREFERRED,5,TableLayout.PREFERRED,5}, {5, TableLayout.PREFERRED,5}};
		JPanel pnlButtons = new JPanel();		
		pnlButtons.setLayout(new TableLayout(cellsPanel));		
		btnOk = new JButton("OK");		
		btnSelectAll = new JButton(CommonLocaleDelegate.getMessage("nuclos.entityrelation.choicepanel.1", "Alle ausw\u00e4hlen"));
		btnSelectNon = new JButton(CommonLocaleDelegate.getMessage("nuclos.entityrelation.choicepanel.2", "Keine ausw\u00e4hlen"));
		pnlButtons.add(btnOk, "1,1");
		pnlButtons.add(btnSelectAll, "3,1");
		pnlButtons.add(btnSelectNon, "5,1");
		
		btnOk.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		});
		
		
		btnSelectAll.addActionListener(new SelectAction(true));
		btnSelectNon.addActionListener(new SelectAction(false));
		
		TableColumn col = tblEntities.getColumnModel().getColumn(1);
		DefaultCellEditor editor = new DefaultCellEditor(new JCheckBox());		
		col.setCellEditor(editor);
		editor.setClickCountToStart(0);
		
		col.setCellRenderer(new CheckboxRenderer());
		
		this.add(paneEntities, "1,1");
		this.add(pnlButtons, "1, 3");
	}
	
	class SelectAction implements ActionListener {
		
		Boolean bSelect;
		
		public SelectAction(Boolean bln) {
			this.bSelect = bln;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			for(EntityMetaDataVO vo : mpEntites.keySet()) {
				mpEntites.put(vo, bSelect);
			}
			model.fireTableDataChanged();			
		}
		
	}


	protected void loadEntites() {
		for(EntityMetaDataVO voMeta : MetaDataClientProvider.getInstance().getAllEntities()) {
			if(voMeta.getEntity().startsWith("nuclos_") || voMeta.getEntity().equals("entityfields"))
				continue;
			
			boolean blnAdd = true;
			for(EntityMetaDataVO vo : lstEntitesExclude) {
				if(voMeta.getEntity().equals(vo.getEntity())){
					blnAdd = false;
					break;
				}
			}
			if(blnAdd){
				lstEntites.add(voMeta);
				mpEntites.put(voMeta, false);
			}
		}
		
		lstEntites = CollectionUtils.sorted(lstEntites, new Comparator<EntityMetaDataVO>() {

			@Override
			public int compare(EntityMetaDataVO o1, EntityMetaDataVO o2) {
				return o1.getEntity().compareTo(o2.getEntity());
			}			
			
		});
		
	}
	
	class EntityChoiceTableModel extends AbstractTableModel {
		
		@Override
		public String getColumnName(int column) {
			switch(column) {
			case 0:
				return CommonLocaleDelegate.getMessage("nuclos.entityrelation.choicepanel.3", "Entit\u00e4t");
			case 1:
				return CommonLocaleDelegate.getMessage("nuclos.entityrelation.choicepanel.4", "\u00dcbernehmen");
			default:
				return "";
			}
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public int getRowCount() {
			return lstEntites.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch(columnIndex) {
			case 0:
				return lstEntites.get(rowIndex).getEntity();
			case 1:
				return mpEntites.get(lstEntites.get(rowIndex));
			default:
				return "";
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if(columnIndex > 0)
				return true;
			return false;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if(columnIndex == 1) {				
				mpEntites.put(lstEntites.get(rowIndex), (Boolean)aValue);				
			}
		}
		
	}
	
	class CheckboxRenderer extends JCheckBox implements TableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			this.setSelected((Boolean)value);
			return this;
		}
		
	}

}
