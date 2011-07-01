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

import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

public class NuclosDropTargetListener implements DropTargetListener {
	
	public static int ADD_VALUE_WHEN_DROP = 0;
	public static int CHANGE_VALUE_WHEN_DROP = 1;
	
	final NuclosDropTargetVisitor visit;
	
	public NuclosDropTargetListener(NuclosDropTargetVisitor visitor){
		this.visit = visitor;
	}

	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
		visit.visitDragEnter(dtde);
	}

	@Override
	public void dragExit(DropTargetEvent dte) {
		visit.visitDragExit(dte);
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
		visit.visitDragOver(dtde);
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
