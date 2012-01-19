package org.nuclos.client.main.mainframe.workspace;


public interface ITabStoreController {

	/**
	 * 
	 * @return preferences in XML
	 */
	public String getPreferencesXML();
	
	/**
	 * 
	 * @return
	 */
	public Class<?> getTabRestoreControllerClass();
	
}
