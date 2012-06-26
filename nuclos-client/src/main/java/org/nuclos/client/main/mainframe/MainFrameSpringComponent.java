/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nuclos.client.main.mainframe;

import org.nuclos.client.NuclosIcons;
import org.nuclos.client.common.ClientParameterProvider;
import org.nuclos.client.livesearch.LiveSearchController;
import org.nuclos.client.main.mainframe.workspace.WorkspaceChooserController;
import org.nuclos.client.resource.ResourceCache;
import org.nuclos.common2.SpringLocaleDelegate;
import org.springframework.beans.factory.InitializingBean;

/**
 * An delegate for MainFrame as java.awt.Component's are slow as spring beans.
 * 
 * @author Thomas Pasch
 */
// @org.springframework.stereotype.Component
public class MainFrameSpringComponent implements InitializingBean {
	
	private MainFrame mainFrame;
	
	//
	
	private LiveSearchController liveSearchController;
	
	private WorkspaceChooserController workspaceChooserController;

	private SpringLocaleDelegate localeDelegate;

	private ClientParameterProvider clientParameterProvider;

	private NuclosIcons nuclosIcons;

	private ResourceCache resourceCache;
	
	public MainFrameSpringComponent() {
	}

	public void afterPropertiesSet() throws Exception {
		mainFrame = new MainFrame();
		mainFrame.setResourceCache(getResourceCache());
		mainFrame.setSpringLocaleDelegate(getLocaleDelegate());
		mainFrame.setNuclosIcons(getNuclosIcons());
		mainFrame.setClientParameterProvider(getClientParameterProvider());
		mainFrame.setLiveSearchController(getLiveSearchController());
		mainFrame.setWorkspaceChooserController(getWorkspaceChooserController());
	}

	// @Autowired
	public final void setResourceCache(ResourceCache resourceCache) {
		this.resourceCache = resourceCache;
	}

	// @Autowired
	public final void setSpringLocaleDelegate(SpringLocaleDelegate cld) {
		this.localeDelegate = cld;
	}

	// @Autowired
	public final void setClientParameterProvider(ClientParameterProvider clientParameterProvider) {
		this.clientParameterProvider = clientParameterProvider;
	}

	// @Autowired
	public final void setNuclosIcons(NuclosIcons nuclosIcons) {
		this.nuclosIcons = nuclosIcons;
	}

	/*
	@Autowired
	void prepare(@Value("#{mainController.userName}") String sUserName,
			@Value("#{mainController.nuclosServerName}") String sNucleusServerName)
	{
		init(sUserName, sNucleusServerName);
	}
	 */

	// @Autowired
	public final void setWorkspaceChooserController(WorkspaceChooserController wcc) {
		this.workspaceChooserController = wcc;
	}

	// @Autowired
	public final void setLiveSearchController(LiveSearchController lsc) {
		this.liveSearchController = lsc;
	}
	
	// 

	private LiveSearchController getLiveSearchController() {
		return liveSearchController;
	}

	private WorkspaceChooserController getWorkspaceChooserController() {
		return workspaceChooserController;
	}

	private SpringLocaleDelegate getLocaleDelegate() {
		return localeDelegate;
	}

	private ClientParameterProvider getClientParameterProvider() {
		return clientParameterProvider;
	}

	private NuclosIcons getNuclosIcons() {
		return nuclosIcons;
	}

	private ResourceCache getResourceCache() {
		return resourceCache;
	}
	
	public MainFrame getMainFrame() {
		return mainFrame;
	}

}
