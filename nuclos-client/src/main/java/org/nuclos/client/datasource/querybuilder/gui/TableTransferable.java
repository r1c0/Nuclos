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
package org.nuclos.client.datasource.querybuilder.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * @todo enter class description.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris@novabit.de">Boris</a>
 * @version 01.00.00
 */
public class TableTransferable implements Transferable {
	public static final DataFlavor TableDataFlavor = new DataFlavor(String.class, "DBTableEntry");
	protected String data;

	public TableTransferable(String data) {
		this.data = data;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] {TableDataFlavor};
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.getDefaultRepresentationClass().equals(String.class);
	}

	@Override
	public synchronized Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		Object result = null;
		if (flavor.getRepresentationClass().equals(String.class)) {
			result = data;
		}
		else {
			throw new UnsupportedFlavorException(flavor);
		}
		return result;
	}
}
