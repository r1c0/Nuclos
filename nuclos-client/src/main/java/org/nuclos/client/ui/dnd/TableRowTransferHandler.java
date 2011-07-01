//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.client.ui.dnd;

import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragSource;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

/**
 * Drag and Drop row move support for within a JTables. Mainly based on the code at 
 * <a href="http://stackoverflow.com/questions/638807/how-do-i-drag-and-drop-a-row-in-a-jtable">
 * here</a>.
 * <p>
 * The model of the table must implement {@link IReorderable}. To enable, use the 
 * following code:
 * <code><pre>
 *  tblTreeView = new JTable(tableModel);
 *  tblTreeView.setDragEnabled(true);
 *  tblTreeView.setDropMode(DropMode.INSERT_ROWS);
 *  tblTreeView.setTransferHandler(new TableRowTransferHandler(tblTreeView));
 * </pre></code>
 * </p>
 * @author Thomas Pasch
 * @since 3.1.01
 */
public class TableRowTransferHandler extends TransferHandler {
	
	private static final Logger LOG = Logger.getLogger(TableRowTransferHandler.class);

	private final DataFlavor	localObjectFlavor	= new ActivationDataFlavor(
														Integer.class,
														DataFlavor.javaJVMLocalObjectMimeType,
														"Integer Row Index");
	
	private JTable				table				= null;

	public TableRowTransferHandler(JTable table) {
		this.table = table;
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		assert (c == table);
		return new DataHandler(new Integer(table.getSelectedRow()),
			localObjectFlavor.getMimeType());
	}

	@Override
	public boolean canImport(TransferHandler.TransferSupport info) {
		boolean b = info.getComponent() == table && info.isDrop()
			&& info.isDataFlavorSupported(localObjectFlavor);
		table.setCursor(b
			? DragSource.DefaultMoveDrop
			: DragSource.DefaultMoveNoDrop);
		return b;
	}

	@Override
	public int getSourceActions(JComponent c) {
		return TransferHandler.COPY_OR_MOVE;
	}

	@Override
	public boolean importData(TransferHandler.TransferSupport info) {
		JTable target = (JTable) info.getComponent();
		JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
		int index = dl.getRow();
		int max = table.getModel().getRowCount();
		if(index < 0 || index > max)
			index = max;
		target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		try {
			Integer rowFrom = (Integer) info.getTransferable().getTransferData(
				localObjectFlavor);
			if(rowFrom != -1 && rowFrom != index) {
				final TableModel model = table.getModel();
				((IReorderable) model).reorder(table.convertRowIndexToModel(rowFrom), table.convertRowIndexToModel(index));
				if(index > rowFrom)
					index--;
				target.getSelectionModel().addSelectionInterval(index, index);
				return true;
			}
		}
		catch(Exception e) {
			LOG.warn("DnD failed", e);
		}
		return false;
	}

	@Override
	protected void exportDone(JComponent c, Transferable t, int act) {
		if(act == TransferHandler.MOVE) {
			table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}
}
