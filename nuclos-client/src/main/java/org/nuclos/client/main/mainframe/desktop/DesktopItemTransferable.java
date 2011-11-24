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
package org.nuclos.client.main.mainframe.desktop;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Serializable;

import org.nuclos.common.WorkspaceDescription;
import org.nuclos.common.WorkspaceDescription.Desktop;
import org.nuclos.common.WorkspaceDescription.DesktopItem;

public class DesktopItemTransferable implements Transferable {
	
	private final WorkspaceDescription.DesktopItem desktopItem;
	private final WorkspaceDescription.Desktop desktop;

	public DesktopItemTransferable(WorkspaceDescription.DesktopItem desktopItem, WorkspaceDescription.Desktop desktop) {
		super();
		this.desktopItem = desktopItem;
		this.desktop = desktop;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor fl) {
		if (DESKTOP_ITEM_FLAVOR.equals(fl))
			return true;
		return false;
	}

	@Override
	public Object getTransferData(DataFlavor fl) throws UnsupportedFlavorException, IOException {
		if (DESKTOP_ITEM_FLAVOR.equals(fl)) {
			return new TransferData(desktopItem, desktop);
		}
			return null;
	}
	
	public static class TransferData implements Serializable{
		private static final long serialVersionUID = 1L;
		
		public WorkspaceDescription.DesktopItem desktopItem;
		public WorkspaceDescription.Desktop desktop;
		public TransferData(DesktopItem desktopItem, Desktop desktop) {
			super();
			this.desktopItem = desktopItem;
			this.desktop = desktop;
		}
	}
	
	public static final DataFlavor DESKTOP_ITEM_FLAVOR = new DataFlavor(WorkspaceDescription.DesktopItem.class, "WorkspaceDescription.DesktopItem");
	private static final DataFlavor[] flavors = new DataFlavor[] {DESKTOP_ITEM_FLAVOR};
}


