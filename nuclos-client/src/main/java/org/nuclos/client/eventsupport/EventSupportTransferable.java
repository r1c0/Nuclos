package org.nuclos.client.eventsupport;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import org.nuclos.client.explorer.node.EventSupportExplorerNode;

public class EventSupportTransferable implements Transferable {

	DataFlavor[] dataFlavs = new DataFlavor[] {new EventSupportDataFlavor()};
	EventSupportExplorerNode element;
	
	public EventSupportTransferable(EventSupportExplorerNode curNodeElement)
	{
		this.element = curNodeElement;
	}
	
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return this.dataFlavs;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		boolean retVal = false;
		
		for (DataFlavor flav : this.dataFlavs)
		{
			if (flav.equals(flavor)) {
				retVal = true;
				break;
			}	
		}
		
		return retVal;
	}

	@Override
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		
		Object retVal = null;
		
		if (isDataFlavorSupported(flavor))
		{
			if (flavor instanceof EventSupportDataFlavor)
			{
				retVal = this.element;
			}
			else
			{
				throw new UnsupportedFlavorException(flavor);
			}
		}
		return retVal;
	}

}
