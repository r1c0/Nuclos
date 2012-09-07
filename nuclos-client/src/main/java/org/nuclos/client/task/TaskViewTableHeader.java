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
package org.nuclos.client.task;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.main.mainframe.MainFrameSpringComponent;
import org.nuclos.client.ui.table.SortableTableModel;
import org.nuclos.common.Actions;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;

public class TaskViewTableHeader extends JTableHeader {

	private final Icon ascendingSortIcon;
	private final Icon descendingSortIcon;
	
	// former Spring injection
	
	private MainFrame mainFrame;
	
	// end of former Spring injection

	public TaskViewTableHeader(TableColumnModel cm) {
		super(cm);
		setMainFrame(SpringApplicationContextHolder.getBean(MainFrameSpringComponent.class).getMainFrame());

		this.ascendingSortIcon = UIManager.getIcon("Table.ascendingSortIcon");
		this.descendingSortIcon = UIManager.getIcon("Table.descendingSortIcon");
		if (!SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_CUSTOMIZE_ENTITY_AND_SUBFORM_COLUMNS) &&
				getMainFrame().getWorkspace() != null && getMainFrame().getWorkspace().isAssigned()) {
			setReorderingAllowed(false);
		}
		
	}
	
	final void setMainFrame(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}
	
	final MainFrame getMainFrame() {
		return mainFrame;
	}
	
	@Override
	public TableCellRenderer getDefaultRenderer() {
		final TableCellRenderer tcrDefault = super.getDefaultRenderer(); 
		TableCellRenderer tcr = new TableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				final Component comp = tcrDefault.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				
				if (table == null) {
					// may be during close?
					return comp;
				}
				
				if(comp instanceof JLabel) {
					final JLabel jlabel = (JLabel) comp;
					List<? extends SortKey> sortKeys = null;
					if (table.getModel() instanceof SortableTableModel) {
						sortKeys = ((SortableTableModel) table.getModel()).getSortKeys();
					} else if (table.getRowSorter() != null) {
						sortKeys = table.getRowSorter().getSortKeys();
					}
					sortKeys = CollectionUtils.applyFilter(sortKeys, new Predicate<SortKey>() {
						@Override public boolean evaluate(SortKey t) { return t.getSortOrder() != SortOrder.UNSORTED; }
					});
					
					int sortKeyIndex = -1;
					SortOrder sortOrder = null;
					if (sortKeys != null) {
						final int modelColumn = table.convertColumnIndexToModel(column);
						sortKeyIndex = CollectionUtils.indexOfFirst(sortKeys, new Predicate<SortKey>() {
							@Override public boolean evaluate(SortKey t) { return t.getColumn() == modelColumn; }
						});
						if (sortKeyIndex != -1) {
							sortOrder = sortKeys.get(sortKeyIndex).getSortOrder();
						}
						
						Icon sortIcon = null;
						if (sortOrder != null) {
							switch (sortOrder) {
							case ASCENDING:
								sortIcon = ascendingSortIcon;
								break;
							case DESCENDING:
								sortIcon = descendingSortIcon;
								break;
							}
							
							if (sortIcon != null) {
								final Icon icon = sortIcon;
								final int index = sortKeyIndex + 1;
								sortIcon = new ImageIcon(){
									public int getIconHeight() {
										return icon.getIconHeight();
									};
									public int getIconWidth() {
										return icon.getIconWidth();
									};
									public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
										Graphics2D g2 = (Graphics2D)g;
										g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
										
										Font f = g2.getFont();
										g2.setFont(f.deriveFont(10f));
										g2.drawString(String.valueOf(index), x + getIconWidth() - 1, getIconHeight() / 2 + 1);
										g2.setFont(f);
										
										icon.paintIcon(c, g, x, y);
									}
								};

								//if (sortKeyIndex > 0 && sortIcon != null) {
								//	sortIcon = new Icons.ResizedIcon(sortIcon, 0.75d);
								//}
							}
						}

						jlabel.setHorizontalAlignment(SwingConstants.CENTER);
						jlabel.setHorizontalTextPosition(SwingConstants.LEADING);
						
						/*
						 * This prevents (strange) <em>vertical</em> alignment of table header cells filled with 
						 * HTML JLabels when the user clicks on the header.  
						 */
						jlabel.setVerticalAlignment(SwingConstants.TOP);
						jlabel.setVerticalTextPosition(SwingConstants.TOP);
						
						jlabel.setIcon(sortIcon);
					}
					jlabel.setPreferredSize(new Dimension(ascendingSortIcon.getIconWidth(), ascendingSortIcon.getIconWidth()));
				}
				
				return comp;
			}
			
		};
		return tcr;
	}
}
