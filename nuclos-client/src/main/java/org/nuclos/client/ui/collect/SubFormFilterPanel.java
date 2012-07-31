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
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;

/**
 * A collapsible panel which consists of filter components (CollectableComponents)
 * which are used to filter SubForm table data. This panel is responsible for arranging
 * the filter components so they appear directly above the corresponding column. 
 *
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:martin.weber@novabit.de">Martin Weber</a>
 * @version 01.00.00
 */
public class SubFormFilterPanel extends JXCollapsiblePane implements Closeable {
	
	private static final Logger LOG = Logger.getLogger(SubFormFilterPanel.class);

	//
	
	private JPanel filterPanel = new JPanel(new HorizontalLayout());
	private JPanel resetFilterButtonPanel = new JPanel(new BorderLayout());
	private final JButton resetFilterButton = new JButton();
	
	private TableColumnModel columnModel;
	private Boolean fixedTable = false;;
	
	private Map<String, CollectableComponent> column2component = new HashMap<String, CollectableComponent>();
	
	private boolean closed = false;
	
	public SubFormFilterPanel(TableColumnModel columnModel, Map<String, CollectableComponent> column2component, Boolean fixedTable) {
		setLayout(new BorderLayout());
		this.columnModel = columnModel;
		this.column2component = column2component;
		this.fixedTable = fixedTable;
	
		add(filterPanel, BorderLayout.CENTER);
		init();
	}
	
	private void init() {
		setCollapsed(true);
		
		if (fixedTable) {
			resetFilterButtonPanel.setPreferredSize(new Dimension(SubformRowHeader.COLUMN_SIZE, 20));
			resetFilterButton.setIcon(Icons.getInstance().getIconClearSearch16());
			resetFilterButton.setToolTipText(SpringLocaleDelegate.getInstance().getMessage(
					"subform.filter.tooltip", "Filterkriterien l\u00f6schen"));
			resetFilterButtonPanel.add(resetFilterButton, BorderLayout.CENTER);
		}
		
		arrangeFilterComponents();
		
		addListener();
        // subformFilterPanel is invisible by default. @see NUCLOSINT-1630
		setVisible(false);
	}

	@Override
	public void close() {
		// Close is needed for avoiding memory leaks
		// If you want to change something here, please consult me (tp).  
		if (!closed) {
			LOG.debug("close(): " + this);
			filterPanel = null;
			columnModel = null;
			column2component.clear();
			column2component = null;
			
			// JXCollapsiblePane
			removeAll();
			// setContentPane(null);
			
			closed = true;
		}
	}

	/**
	 * adds a listener on the columnmodel to get informed, when e.g. a column margin was changed
	 * and to arrange the filter components again
	 */
	public void addListener() {
		TableColumnModelListener listener = new TableColumnModelListener() {

			@Override
			public void columnAdded(TableColumnModelEvent e) {
				// do nothing
			}

			@Override
			public void columnMarginChanged(ChangeEvent e) {
				arrangeFilterComponents();
			}

			@Override
			public void columnMoved(TableColumnModelEvent e) {
				arrangeFilterComponents();
			}

			@Override
			public void columnRemoved(TableColumnModelEvent e) {
				// do nothing
			}

			@Override
			public void columnSelectionChanged(ListSelectionEvent e) {
				arrangeFilterComponents();
			}
		};
		
		columnModel.addColumnModelListener(listener);
	}
	
	/**
	 * arranges all filter components, because e.g a column was moved,
	 * so that the filter components stay directly above the corresponding columns
	 */
	private void arrangeFilterComponents() {
		int index = 0;
		filterPanel.removeAll();
		if(this.fixedTable) {
			filterPanel.add(this.resetFilterButtonPanel);
			index++;
		}
		
		for(TableColumn tc : CollectionUtils.iterableEnum(columnModel.getColumns())) {
			if(tc.getIdentifier() == null)
				continue;
			CollectableComponent clctcomp = getCollectableComponentByName(tc.getIdentifier().toString());
			JComponent comp = (clctcomp == null) ? null : clctcomp.getControlComponent();
			if(comp != null) {
				// place checkboxes in the center
				if (comp instanceof JCheckBox) {
					JPanel p = new JPanel(new GridBagLayout());
					p.add(comp);
					comp = p;
				}

				comp.setPreferredSize(new Dimension(tc.getWidth(), 20));
				filterPanel.add(comp);
				index++;
			}
		}
		filterPanel.repaint();
		repaint();
	}
	
	private CollectableComponent getCollectableComponentByName(String name) {
		if (StringUtils.isNullOrEmpty(name)) {
			return null;
		}
		
		for (CollectableComponent comp : column2component.values()) {
			if (comp.getEntityField().getName().equals(name)) {
				return comp;
			}
		}
		
		return null;
	}
	
	public JButton getResetFilterButton() {
		return this.resetFilterButton;
	}
	
	/**
	 * @return the active filter components
	 */
	public Map<String, CollectableComponent> getActiveFilterComponents() {
		Map<String, CollectableComponent> activeActiveFilterComponents = new HashMap<String, CollectableComponent>();
		
		for (int index=0 ; index<columnModel.getColumnCount() ; index++) {
			TableColumn column = columnModel.getColumn(index);
			if(column.getIdentifier() == null)
				continue;
			String header = column.getIdentifier().toString();
			CollectableComponent comp = column2component.get(header);
			
			if (!this.fixedTable) {
				activeActiveFilterComponents.put(header, comp);
			}
			else if (!"".equals(header)) {
				activeActiveFilterComponents.put(header, comp);
			}
		}
		
		return activeActiveFilterComponents;
	}
}
