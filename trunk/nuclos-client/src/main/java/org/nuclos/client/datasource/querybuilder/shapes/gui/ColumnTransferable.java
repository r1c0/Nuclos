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
package org.nuclos.client.datasource.querybuilder.shapes.gui;

import org.nuclos.common.database.query.definition.Column;
import java.awt.datatransfer.*;
import java.io.IOException;

/**
 * @todo enter class description.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class ColumnTransferable implements Transferable {
	//public static final DataFlavor ColumnDataFlavor = new DataFlavor(Column.class, "DBColumnEntry");
	public static final DataFlavor ColumnDataFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=org.nuclos.common.database.query.definition.Column", "DBColumnEntry");

	protected ConstraintColumn column;

	/**
	 *
	 * @param column
	 */
	public ColumnTransferable(ConstraintColumn column) {
		this.column = column;
	}

	/**
	 *
	 * @return
	 */
	public ConstraintColumn getColumn() {
		return column;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		//DataFlavor flavor = new DataFlavor(Column.class, "DBColumnEntry");
		DataFlavor flavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=org.nuclos.common.database.query.definition.Column", "DBColumnEntry");
		return new DataFlavor[] {flavor};
	}

	/**
	 *
	 * @param flavor
	 * @return
	 */
	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.getRepresentationClass().equals(Column.class);
	}

	/**
	 *
	 * @param flavor
	 * @return
	 * @throws java.awt.datatransfer.UnsupportedFlavorException
	 * @throws java.io.IOException
	 */
	@Override
	public synchronized Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		Object result = null;
		if (flavor.getRepresentationClass().equals(Column.class)) {
			result = column;
		}
		else {
			throw new UnsupportedFlavorException(flavor);
		}
		return result;
	}

}

