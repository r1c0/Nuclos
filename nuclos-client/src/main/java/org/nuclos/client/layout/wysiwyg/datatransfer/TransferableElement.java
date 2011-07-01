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
package org.nuclos.client.layout.wysiwyg.datatransfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * TransferableElement implements the Transferable interface to enable 
 * drag&drop and copy&paste
 * 
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author <a href="mailto:thomas.schiffmann@novabit.de">thomas.schiffmann</a>
 * @version 01.00.00
 */
public class TransferableElement implements Transferable {

	/**
	 * Element to transfer
	 */
	private DragElement element;
	
	public final static DataFlavor flavor = new DataFlavor(DragElement.class, "Element");
	
	/**
	 * Constructor
	 * @param element element that should be transferred
	 * NUCLEUSINT-496
	 */
	public TransferableElement(String element, String controltype, boolean labeledComponent) {
		this.element = new DragElement(element, controltype, labeledComponent);
	}
	
	
	/**
	 * return DataFlavor for ElementImpl
	 */
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] {flavor};
	}

	/**
	 * Checks if a dataflavor ist supported
	 */
	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		if (flavor.getRepresentationClass().equals(flavor.getRepresentationClass())) {
			return true;
		}
		else {
			return true;
		}
	}

	/**
	 * returns the transferred element
	 */
	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (!isDataFlavorSupported(flavor)) {
			throw new UnsupportedFlavorException(flavor);
		}
		else {
			return element;
		}
	}

}
