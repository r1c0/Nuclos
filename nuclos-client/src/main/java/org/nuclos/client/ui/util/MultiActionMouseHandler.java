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

package org.nuclos.client.ui.util;

import java.awt.Component;
import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;


public class MultiActionMouseHandler extends DelegatingMouseListener {

	public static abstract class MouseActionHandler extends MouseInputAdapter {

		private boolean active;
		
		public boolean isActive() {
			return active;
		}
		
		protected boolean isAutoTrigger(MouseEvent evt) {
			return false;
		}
		
		protected void start(MouseEvent evt) {
			active = true;
			evt.consume();
		}
		
		protected void stop() {
			active = false;
		}
	}
	
	private final MouseInputListener defaultListener;
	private final MouseActionHandler[] autoHandlers;
	private MouseActionHandler currentHandler = null;
	
	public MultiActionMouseHandler(MouseInputListener defaultListener, MouseActionHandler... autoHandlers) {
		this.defaultListener = defaultListener;
		this.autoHandlers = (autoHandlers != null && autoHandlers.length > 0) ? autoHandlers : null;
	}
	
	public void install(Component component) {
		component.addMouseListener(this);
		component.addMouseMotionListener(this);
	}
	
	public void uninstall(Component component) {
		component.removeMouseListener(this);
		component.removeMouseMotionListener(this);
	}
	
	@Override
	protected MouseInputListener getListener(MouseEvent evt) {
		MouseActionHandler handler = getActiveHandler();
		if (handler != null) {
			return handler;
		} else if (autoHandlers != null) {
			for (MouseActionHandler autoHandler : autoHandlers) {
				if (autoHandler.isAutoTrigger(evt)) {
					autoHandler.start(evt);
					return autoHandler;
				}
			}
		}
		return defaultListener;
	}
	
	public MouseActionHandler getActiveHandler() {
		if (currentHandler != null) {
			if (currentHandler.isActive()) {
				return currentHandler;
			} else {
				currentHandler = null;
			}
		}
		return null;
	}
	
	public void startHandler(MouseActionHandler listener, MouseEvent evt) {
		stopHandler();
		currentHandler = listener;
		currentHandler.start(evt);
	}
	
	public void stopHandler() {
		if (currentHandler != null && currentHandler.isActive()) {
			currentHandler.stop();
			currentHandler = null;
		}
	}
}