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
package org.nuclos.client.genericobject.datatransfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;
import java.util.List;

/**
 * A <code>List</code> of generic objects as a <code>Transferable</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class TransferableGenericObjects implements Transferable {

	private static class GenericObjectsDataFlavor extends DataFlavor {
		public GenericObjectsDataFlavor() {
			super(List.class, "List<GenericObject>");
		}

		@Override
		public boolean equals(Object o) {
			return (this == o);
		}
	}

	public static DataFlavor dataFlavor = new GenericObjectsDataFlavor();

	private final DataFlavor[] aflavors;

	/**
	 * List<GenericObjectIdModuleProcess>
	 */
	private final List<GenericObjectIdModuleProcess> lstgoimp;

	/**
	 * creates a <code>Transferable</code> for a <code>GenericObjectVO</code>
	 * @param lstgoimp List<GenericObjectIdModuleProcess>
	 */
	public TransferableGenericObjects(List<GenericObjectIdModuleProcess> lstgoimp) {
		this.lstgoimp = lstgoimp;

		final List<DataFlavor> lstflavors = new ArrayList<DataFlavor>();
		lstflavors.add(dataFlavor);
		if (this.lstgoimp.size() == 1) {
			lstflavors.add(new GenericObjectIdModuleProcess.DataFlavor());
		}
		lstflavors.add(DataFlavor.stringFlavor);
		this.aflavors = lstflavors.toArray(new DataFlavor[0]);
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
		final Object result;

		if (flavor.equals(dataFlavor)) {
			result = this.lstgoimp;
		}
		else if (flavor instanceof GenericObjectIdModuleProcess.DataFlavor) {
			assert this.lstgoimp.size() == 1;
			result = this.lstgoimp.get(0);
		}
		else if (flavor.equals(DataFlavor.stringFlavor)) {
			result = this.getStringRepresentation();
		}
		else {
			throw new UnsupportedFlavorException(flavor);
		}

		return result;
	}

	/**
	 * @return a string representation compatible with MS Excel.
	 */
	private String getStringRepresentation() {
		final StringBuffer sb = new StringBuffer();
		for (GenericObjectIdModuleProcess goimp : lstgoimp) {
			sb.append(goimp.toString());
			sb.append('\n');
		}
		return sb.toString();
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return this.aflavors;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		final DataFlavor[] flavors = getTransferDataFlavors();
		for (int i = 0; i < flavors.length; i++) {
			if (flavors[i].equals(flavor)) {
				return true;
			}
		}
		return false;
	}

}	// class TransferableGenericObjects
