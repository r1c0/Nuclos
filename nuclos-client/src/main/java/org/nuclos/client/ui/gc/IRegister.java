package org.nuclos.client.ui.gc;

import java.lang.ref.Reference;
import java.util.EventListener;

interface IRegister {
	
	void register();
	
	void unregister();
	
	Reference<EventListener> getReference();

}
