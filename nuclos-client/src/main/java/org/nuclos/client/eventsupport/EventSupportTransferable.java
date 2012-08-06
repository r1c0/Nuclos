package org.nuclos.client.eventsupport;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import org.nuclos.client.explorer.node.eventsupport.EventSupportTreeNode;

public class EventSupportTransferable implements Transferable {

	DataFlavor[] dataFlavs = new DataFlavor[] {new EventSupportDataFlavor(), DataFlavor.stringFlavor};
	EventSupportTreeNode element;
	String value;
	
	public EventSupportTransferable(EventSupportTreeNode curNodeElement)
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
			else if(flavor.equals(DataFlavor.stringFlavor))
			{
				retVal = value;
			}
			else
			{
				throw new UnsupportedFlavorException(flavor);
			}
		}
		return retVal;
	}

}
