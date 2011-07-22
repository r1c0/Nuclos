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
package org.nuclos.client.common;

import static org.nuclos.common2.CommonLocaleDelegate.getMessage;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputAdapter;

public class TableRowResizer extends MouseInputAdapter { 
    public static Cursor resizeCursor = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
    
    public static int RESIZE_ALL_ROWS = 0;
    public static int RESIZE_SELECTED_ROW = 1;
    public static String SUBFORM_ROW_HEIGHT = "rowheight";

    private int mouseYOffset, resizingRow; 
    private Cursor otherCursor = resizeCursor; 
    private JTable table;
    Set<JTable> sTables;
    Preferences prefs;
    
    
    List<ListSelectionListener> lstListener;
    int mode;

    public TableRowResizer(JTable table, int mode, Preferences pref) { 
        this.table = table;
        this.mode = mode;
        this.prefs = pref;
        table.addMouseListener(this); 
        table.addMouseMotionListener(this);
        sTables = new HashSet<JTable>();
        lstListener = new ArrayList<ListSelectionListener>();        
    } 
    
    public void addJTableToSynch(JTable table) {
    	sTables.add(table);
    }

    private int getResizingRow(Point p) { 
        return getResizingRow(p, table.rowAtPoint(p)); 
    } 

    private int getResizingRow(Point p, int row) { 
        if(row == -1){ 
            return -1; 
        } 
        int col = table.columnAtPoint(p); 
        if(col==-1) 
            return -1; 
        Rectangle r = table.getCellRect(row, col, true); 
        r.grow(0, -3); 
        if(r.contains(p)) 
            return -1; 

        int midPoint = r.y + r.height / 2; 
        int rowIndex = (p.y < midPoint) ? row - 1 : row; 

        return rowIndex; 
    } 
    
    @Override
    public void mousePressed(MouseEvent e) { 
        Point p = e.getPoint(); 

        resizingRow = getResizingRow(p); 
        mouseYOffset = p.y - table.getRowHeight(resizingRow); 
    } 
    
    

    private void swapCursor() { 
        Cursor tmp = table.getCursor(); 
        table.setCursor(otherCursor); 
        otherCursor = tmp; 
    }
    
    @Override
    public void mouseMoved(MouseEvent e) {
        if((getResizingRow(e.getPoint())>=0) != (table.getCursor() == resizeCursor)){
            swapCursor();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {    	
        int mouseY = e.getY();
        
        if(resizingRow >= 0) {
            int newHeight = mouseY - mouseYOffset;
            if(newHeight > 0) {
            	if(mode == RESIZE_ALL_ROWS)
            		setHighForAllRows(newHeight);
            	else
            		setHighForSelectedRow(newHeight);
            }
            
        }
        
    }
    
    private void setRowHeightInPreferences(int newHeight) {
    	this.prefs.putInt(SUBFORM_ROW_HEIGHT, newHeight);
    }
    
    private void setHighForAllRows(int newHeight) {
    	table.setRowHeight(newHeight);
    	for(JTable tab : sTables){
        	tab.setRowHeight(newHeight);
        }
    	setRowHeightInPreferences(newHeight);
    }
    
    private void setHighForSelectedRow(int newHeight){
    	table.setRowHeight(resizingRow, newHeight);	          
    	for(JTable tab : sTables){
    		tab.setRowHeight(resizingRow, newHeight);
    	}    	
    	setRowHeightInPreferences(newHeight);
    }


	@Override
	public void mouseClicked(MouseEvent e) {
		if(SwingUtilities.isRightMouseButton(e)) {
			JPopupMenu pop = new JPopupMenu();
			JMenuItem mi = new JMenuItem(getMessage("TableRowResizer.1", "zurück setzen"));
			mi.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					setHighForAllRows(20);				
				}
			});
			pop.add(mi);
			pop.setLocation(e.getLocationOnScreen());
			pop.show(table, e.getX(), e.getY());
		}
	}
    
    
}
