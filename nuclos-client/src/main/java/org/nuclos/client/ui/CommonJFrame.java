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

import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.event.MouseInputAdapter;

import org.apache.log4j.Logger;
import org.nuclos.client.synthetica.NuclosThemeSettings;

/**
 * JFrame with some additional features, especially support for the MAXIMIZED state.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public class CommonJFrame extends JFrame {

	private static final Logger log = Logger.getLogger(CommonJFrame.class);
	
	private Map<Object, Object> clientProperties = new HashMap<Object, Object>();
	
	private final Collection<ActionListener> colEndResizingListener = new ArrayList<ActionListener>();
	
	private boolean isResizing=false;  

	/**
	 * the bounds of the frame in the NORMAL (non-maximized) state.
	 */
	private Rectangle rectNormalBounds;

	/**
	 * As we cannot distinguish a maximize operation from a regular move in componentMoved(),
	 * we have to remember the previous bounds here and restore them, after the window has been
	 * maximized.
	 */
	private Rectangle rectNormalBoundsBeforeLastMove;

	private final ComponentListener complistener = new ComponentAdapter() {

		@Override
		public void componentMoved(ComponentEvent ev) {
			log.debug("CommonJFrame.componentMoved");
			final CommonJFrame frame = CommonJFrame.this;
			assert ev.getComponent() == frame;
			frame.rectNormalBoundsBeforeLastMove = frame.rectNormalBounds;
			frame.setNormalBounds(frame.getBounds());
		}

		@Override
		public void componentResized(final ComponentEvent ev) {
			log.debug("CommonJFrame.componentResized");
			final CommonJFrame frame = CommonJFrame.this;
			assert ev.getComponent() == frame;
			if (frame.isMaximized()) {
				log.debug("frame maximized");
				frame.setNormalBounds(frame.rectNormalBoundsBeforeLastMove);
//				frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
			}
			else {
				log.debug("frame normal");
				frame.setNormalBounds(frame.getBounds());
				frame.setExtendedState(JFrame.NORMAL);
			}
		}
	};  // inner class ComponentListener
	
	private final MouseListener mouselistener = new MouseInputAdapter(){  
        @Override  
        public void mouseClicked(MouseEvent e) {  
            isResizing=true;
            if (log.isDebugEnabled())
            	log.debug("isResizing on clicked: " + isResizing);
        }  
        @Override  
        public void mouseReleased(MouseEvent e) {  
            isResizing=false;  
            if (log.isDebugEnabled())
            	log.debug("isResizing on released: " + isResizing);
       }  
    };

	public CommonJFrame() throws HeadlessException {
		super();
		this.addComponentListener(complistener);
		this.addMouseListener(mouselistener);  
	}

	public CommonJFrame(String sTitle) throws HeadlessException {
		super(sTitle);
		this.addComponentListener(complistener);
		this.addMouseListener(mouselistener);
	}
	
	public void putClientProperty(Object key, Object value) {
		clientProperties.put(key, value);
	}

	public Object getClientProperty(Object key) {
		return clientProperties.get(key);
	}

	/**
	 * @return Is this frame currently in the MAXIMIZED state?
	 */
	public boolean isMaximized() {
		return ((this.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH);
	}

	/**
	 * @return the bounds of this frame in the NORMAL (non-maximized) state, that is the bounds
	 * that are restored when the frame goes from the MAXIMIZED state into the NORMAL state.
	 */
	public Rectangle getNormalBounds() {
		if (this.isMaximized() && (this.rectNormalBounds != null)) {
			return new Rectangle(this.rectNormalBounds);
			// Ramin had a NullPointerException here. It can't be reproduced though. 13.10.2003
			// I had it too, but now it's vanished. As a workaround, we check if rectNormalBounds != null. 18.11.2003
			// Thank you, Javasoft, for ignoring developers' demands for "maximized" support in JFrame. :-(
		}
		else {
			return this.getBounds();
		}
	}

	/**
	 * sets the normal bounds of this frame
	 * @param rectBounds
	 */
	public void setNormalBounds(Rectangle rectBounds) {
		log.debug("CommonJFrame.setNormalBounds - rectBounds = " + rectBounds);
		if (rectBounds != null) {
			this.rectNormalBounds = rectBounds;
		}
//		else {
			// This case only happens when application is started in maximized state; therefore no
			// rectNormalBoundsBeforeLastMove exists; rectNormalBounds then is set to a valid value
			//  and nothing evil can happen / UA
//			log.warn("setNormalBounds(null)");
//		}
	}
	
	/**
     * 
     * @param al
     */
    public void addEndResizingListener(ActionListener al) {
      colEndResizingListener.add(al);
    }
    
    /**
     * 
     * @param al
     */
    public void removeEndResizingListener(ActionListener al) {
    		colEndResizingListener.remove(al);
    }
    
    /**
     * 
     * @param iWidth
     * @param iHeight
     */
      public void fireEndResizing(int iWidth, int iHeight) {
            for (ActionListener al : colEndResizingListener){
                  al.actionPerformed(new ActionEvent(this, 100, iWidth+"x"+iHeight));
            }
      }
	
	public boolean isResizing() {
		return isResizing;
	}

	@Override
	public void paintComponents(Graphics g) {
		if (isResizing) {
			g.setColor(NuclosThemeSettings.BACKGROUND_COLOR4);
			g.fillRect(0, 0, getWidth(), getHeight());
		} else {
			super.paintComponents(g);
		}
	}
	
	/**
	 * 
	 */
	@Override
	public void setVisible(final boolean visible) {
	  // let's handle visibility...
	  if (!visible || !isVisible()) { // have to check this condition simply because super.setVisible(true) invokes toFront if frame was already visible
	      super.setVisible(visible);
	  }
	  // ...and bring frame to the front.. in a strange and weird way
	  if (visible) {
	      int state = super.getExtendedState();
	      state &= ~JFrame.ICONIFIED;
	      super.setExtendedState(state);
	      super.setAlwaysOnTop(true);
	      super.toFront();
	      super.requestFocusInWindow();
	      super.setAlwaysOnTop(false);
	  }
	}

	/**
	 * 
	 */
	@Override
	public void toFront() {
	  super.setVisible(true);
	  int state = super.getExtendedState();
	  state &= ~JFrame.ICONIFIED;
	  super.setExtendedState(state);
	  super.setAlwaysOnTop(true);
	  super.toFront();
	  super.requestFocusInWindow();
	  super.setAlwaysOnTop(false);
	}

}  // class CommonJFrame
