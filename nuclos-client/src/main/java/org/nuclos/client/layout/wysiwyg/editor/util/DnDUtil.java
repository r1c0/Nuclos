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
package org.nuclos.client.layout.wysiwyg.editor.util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGLayoutComponent;
import org.nuclos.client.layout.wysiwyg.datatransfer.TransferableComponent;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;

/**
 * This class does all Actions that can be done for drag and drop.<br>
 *
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:stefan.geiling@novabit.de">stefan.geiling</a>
 * @version 01.00.00
 */
public class DnDUtil {
	public static class GhostGlassPane extends JPanel
	{
		private AlphaComposite composite;
		private BufferedImage dragged = null;
		private Point location = new Point(0, 0);

		public GhostGlassPane( )
		{
			setOpaque(false);
			composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f); 
			setBorder(BorderFactory.createLineBorder(Color.RED));

		}
		
		public void setImage(BufferedImage dragged)
		{
			this.dragged = dragged;
		}

		public void setPoint(Point location)
		{
			this.location = location;
		}

		public void paintComponent(Graphics g)
		{
			if (dragged == null)
				return;

			Graphics2D g2 = (Graphics2D) g;
			g2.setComposite(composite);

			g2.drawImage(dragged,
				(int) (location.getX( ) - (dragged.getWidth(this)  / 2d)),
				(int) (location.getY( ) - (dragged.getHeight(this) / 2d)),
				//	location.x, location.y,
				null);
		}
	}
	
	private static class MouseDragGestureListener implements DragGestureListener {
		private final WYSIWYGComponent component;
		private final WYSIWYGComponent wysiwygComponent;
		
		public MouseDragGestureListener(final WYSIWYGComponent component, final WYSIWYGComponent wysiwygComponent) {
			this.component = component;
			this.wysiwygComponent = wysiwygComponent;
		}
		
		public void dragGestureRecognized(DragGestureEvent e) {
   	    	WYSIWYGComponent item = (wysiwygComponent != null) ? wysiwygComponent: findWYSIWYGComponent(e.getComponent());
   	 		if (item != null) {	 
   	 			final WYSIWYGLayoutEditorPanel parent = component.getParentEditor();

   	 			if (parent != null && !parent.getTableLayoutPanel().isResizeDragPerformed()) {
   	 				parent.setComponentToMove(item);
   	 				parent.getTableLayoutPanel().initGlassPane((Component)item, e.getDragOrigin());
   	 				
    	 			try {
    	 				// NUCLEUSINT-496
	 					e.startDrag(null, new TransferableComponent(item), new DragSourceListener() {
							@Override
							public void dropActionChanged(DragSourceDragEvent dsde) {
							}
							@Override
							public void dragOver(DragSourceDragEvent dsde) {
							}
							@Override
							public void dragExit(DragSourceEvent dse) {
							}
							@Override
							public void dragEnter(DragSourceDragEvent dsde) {
							}
							@Override
							public void dragDropEnd(DragSourceDropEvent dsde) {
			   	 				parent.getTableLayoutPanel().hideGlassPane();
							}
						});
    	 			} catch (InvalidDnDOperationException ex) {
    	 				//do nothing
    	 			}
   	 			}
   	 		}
   	     }
	}

	public static void addDragGestureListener(final WYSIWYGComponent component) {
		addDragGestureListener(component, null);
	}
	
	public static void addDragGestureListener(final WYSIWYGComponent component, final WYSIWYGComponent wysiwygComponent) {
		MouseDragGestureListener dgListener = new MouseDragGestureListener(component, wysiwygComponent);

   	  	// component, action, listener
   	  	if (component instanceof JComponent)
   	  		addDragGestureListener((JComponent)component, dgListener);
	}


	private static void addDragGestureListener(final JComponent c,final MouseDragGestureListener dgListener) {
		DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
			     c, DnDConstants.ACTION_COPY_OR_MOVE, dgListener );
		Component[] comps = c.getComponents();
		for (int i = 0; i < comps.length; i++) {
			if (comps[i] instanceof JComponent)
				addDragGestureListener((JComponent)comps[i], dgListener);
		}		
	}
	
	private static WYSIWYGComponent findWYSIWYGComponent(Object o) {
		if (o instanceof WYSIWYGComponent)
			return (WYSIWYGComponent)o;
		
		if (o instanceof Container) {
			return findWYSIWYGComponent(((Container)o).getParent());
		}
		return null;
	}
}
