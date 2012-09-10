package org.nuclos.client.eventsupport;

import java.awt.datatransfer.DataFlavor;

import org.nuclos.common2.LangUtils;

public class EventSupportDataFlavor extends DataFlavor {

	public static final String dataFlavorPresentableName = "EventSupportDataFlavor";
	
	public EventSupportDataFlavor()
	{
		super(EventSupportExplorerNodeTransferable.class, dataFlavorPresentableName);
	}
	
	public boolean equals(DataFlavor o) {
		
		final boolean result;
		if (!super.equals(o)) {
			result = false;
		}
		else {
			result = LangUtils.equals(this.getHumanPresentableName(), o.getHumanPresentableName());
		}
		return result;
	}
}
