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

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.util.Timer;
import java.util.TimerTask;

import org.jfree.util.Log;
import org.nuclos.common.ParameterProvider;

public class OneDropNuclosDropTargetListener extends NuclosDropTargetListener {
	
	boolean stepInto;
	boolean onlyOnce;
	
	protected int milliseconds = ClientParameterProvider.getInstance().getIntValue(ParameterProvider.KEY_DRAG_CURSOR_HOLDING_TIME, 600);
	
	public OneDropNuclosDropTargetListener(NuclosDropTargetVisitor visitor) {
		super(visitor);		
	}
	
	public OneDropNuclosDropTargetListener(NuclosDropTargetVisitor visitor, Integer wait) {
		super(visitor);		
		this.milliseconds = wait;
	}
	
	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
		visit.visitDragEnter(dtde);
		stepInto = true;
		onlyOnce = false;
	}

	@Override
	public void dragExit(DropTargetEvent dte) {
		visit.visitDragExit(dte);
		stepInto = false;
		onlyOnce = false;
	}

	@Override
	public void dragOver(final DropTargetDragEvent dtde) {
		if(onlyOnce)
			return;
		onlyOnce = true;
		dtde.rejectDrag();
		final Point pointBeforeTimer = MouseInfo.getPointerInfo().getLocation();		
		Timer timer = new Timer("waitfordrop");
		TimerTask task = new TimerTask() {
			
			@Override
			public void run() {
				try {
					Point pointAfterTimer = MouseInfo.getPointerInfo().getLocation();
					if(pointAfterTimer.getX() == pointBeforeTimer.getX() && pointAfterTimer.getY() == pointBeforeTimer.getY()){
						stepInto = true;
						onlyOnce = true;
					}
					else {
						stepInto = false;
						onlyOnce = false;
					}
					
					if(stepInto) {					
						visit.visitDragOver(dtde);
					}
				}
				catch (Exception e) {
					Log.error("dragOver failed: " + e, e);
				}
			}
		};
		
		timer.schedule(task, milliseconds);
		
	}

	@Override
	public void drop(DropTargetDropEvent dtde) {
		visit.visitDrop(dtde);
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
		visit.visitDropActionChanged(dtde);
	}

}
