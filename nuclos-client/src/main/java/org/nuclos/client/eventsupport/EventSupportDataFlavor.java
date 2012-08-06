package org.nuclos.client.eventsupport;

import java.awt.datatransfer.DataFlavor;

public class EventSupportDataFlavor extends DataFlavor {

	public boolean equals(DataFlavor o) {
		
		boolean retVal = false;
		
		if (o instanceof EventSupportDataFlavor)
		{
			retVal = super.equals((EventSupportDataFlavor) o);
		}
		
		return retVal;
	}
}
