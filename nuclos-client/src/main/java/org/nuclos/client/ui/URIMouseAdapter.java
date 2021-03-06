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
package org.nuclos.client.ui;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;

import org.nuclos.client.ui.collect.component.TableCellCursor;
import org.nuclos.common.collect.collectable.AbstractCollectableField;
import org.nuclos.common2.LangUtils;

public class URIMouseAdapter extends TableRowMouseOverAdapter {
	
	
	public URIMouseAdapter() {

	}
	

	@Override
	public void mouseEntered(MouseEvent e) {
		if(e.getSource() instanceof JTextField) {
			JTextField textField = (JTextField)e.getSource();
			String text = textField.getText();
			if(LangUtils.isValidURI(text)){
				textField.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));							
			}
			else {
				textField.setCursor(Cursor.getDefaultCursor());
			}
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		super.mouseMoved(e);
		if(e.getSource() instanceof JTable) {
			JTable table = (JTable)e.getSource();
			int col = table.columnAtPoint(e.getPoint());
			int row = ((TableRowMouseOverSupport) e.getSource()).getMouseOverRow();	
			
			if(col < 0 || row < 0)
				return;			
			
			final TableCellRenderer cellRenderer = table.getCellRenderer(row, col);
			if ((cellRenderer instanceof TableCellCursor)) {
				int colX = 0;
				for (int i = 0; i < col; i++) {
					colX = colX + table.getColumnModel().getColumn(i).getWidth();
				}
				final Cursor cellCursor = ((TableCellCursor)cellRenderer).getCursor(
						table.getValueAt(row, col), 
						table.getColumnModel().getColumn(col).getWidth(),
						e.getX()-colX);
				if (cellCursor != null) {
					table.setCursor(cellCursor);
					return;
				}
			}
			
			Object obj = table.getValueAt(row, col);
			if(obj instanceof AbstractCollectableField) {
				AbstractCollectableField field = (AbstractCollectableField)obj;
				if(LangUtils.isValidURI(field.toString())) {
					table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				}
				else {
					table.setCursor(Cursor.getDefaultCursor());
				}
			} else if(obj instanceof String) {
				if(LangUtils.isValidURI(obj.toString())) {
					table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				}
				else {
					table.setCursor(Cursor.getDefaultCursor());
				}
			}	
		}
	}
	
	

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getSource() instanceof JTable) {
			if(SwingUtilities.isRightMouseButton(e))
				return;
			JTable table = (JTable)e.getSource();
			int col = table.columnAtPoint(e.getPoint());
			int row = table.rowAtPoint(e.getPoint());			
			if(col < 0 || row < 0)
				return;			
			Object obj = table.getValueAt(row, col);
			if(obj instanceof AbstractCollectableField) {
				AbstractCollectableField field = (AbstractCollectableField)obj;
				if(LangUtils.isValidURI(field.toString())) {
					openURI(field.toString(), table);	
				}				
			} else if(obj instanceof String) {
				if(LangUtils.isValidURI(obj.toString())) {
					openURI(obj.toString(), table);	
				}	
			}
		}
	}


	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getSource() instanceof JTextField) {
			JTextField textField = (JTextField)e.getSource();
			if(textField.getCursor().equals(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))) {							
				String text = textField.getText();
				if(LangUtils.isValidURI(text)){
					if(SwingUtilities.isRightMouseButton(e)) {									
						textField.selectAll();
						textField.requestFocusInWindow();
					}
					else {
						openURI(text, textField);
					}
				}
			}
		}
		else if(e.getSource() instanceof JTable) {
			JTable table = (JTable)e.getSource();
			int col = table.columnAtPoint(e.getPoint());
			int row = table.rowAtPoint(e.getPoint());			
			if(col < 0 || row < 0)
				return;			
			Object obj = table.getValueAt(row, col);
			if(obj instanceof AbstractCollectableField) {
				AbstractCollectableField field = (AbstractCollectableField)obj;
				if(LangUtils.isValidURI(field.toString())) {
					openURI(field.toString(), table);					
				}				
			} else if(obj instanceof String) {
				if(LangUtils.isValidURI(obj.toString())) {
					openURI(obj.toString(), table);	
				}	
			}
		}
	}	
	
	private void openURI(String sUri, JComponent parent) {
		if (org.nuclos.common2.StringUtils.isNullOrEmpty(sUri))
			return;
		String str = org.nuclos.common2.StringUtils.emptyIfNull(sUri).toLowerCase();
		boolean bHasProtocol = str.startsWith("http:") || str.startsWith("https:") || str.startsWith("file:") || str.startsWith("ftp:") || str.startsWith("mailto:");
	
		try {
			if (Desktop.isDesktopSupported()) {
				Desktop desktop = Desktop.getDesktop();
				
				URI uri;
				if (sUri.startsWith("file:")) {
					uri = new java.io.File(sUri).toURI();
					String path = uri.getPath();
					if (path.indexOf("file:/") != -1)
						path = path.substring(path.indexOf("file:/") + 5);
					uri = new URI(uri.getScheme(), uri.getHost(), path, uri.getFragment());
				} else {
					if(bHasProtocol)	
						uri = new URI(sUri);
					else
						uri = new URI("http://" + sUri);
				}
				if (sUri.startsWith("file:")) {
					if (desktop.isSupported(Desktop.Action.OPEN)) {
						java.io.File file = new java.io.File(uri);
						if (file.exists())
							Desktop.getDesktop().open(file);
						return;
					}
				}
				if (sUri.startsWith("mailto:")) {
					if (desktop.isSupported(Desktop.Action.MAIL)) {
						Desktop.getDesktop().mail(uri);
						return;
					}
				}
				
				if (desktop.isSupported(Desktop.Action.BROWSE)) {
					Desktop.getDesktop().browse(uri);				
				}
			}
			
		} catch (IOException ex) {
			Errors.getInstance().showExceptionDialog(parent, "URIMouseAdapter.1", ex);
		} catch (URISyntaxException ex) {
			Errors.getInstance().showExceptionDialog(parent, "URIMouseAdapter.1", ex);
		} 
	}

}
