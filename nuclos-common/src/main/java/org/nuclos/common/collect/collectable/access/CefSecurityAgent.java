package org.nuclos.common.collect.collectable.access;

public interface CefSecurityAgent {

	boolean isReadable();
	
	boolean isWritable();
	
	boolean isRemovable();
}
