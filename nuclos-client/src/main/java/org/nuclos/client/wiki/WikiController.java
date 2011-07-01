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
package org.nuclos.client.wiki;

import java.awt.Component;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.client.common.ClientParameterProvider;
import org.nuclos.client.masterdata.wiki.WikiDelegate;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModel;
import org.nuclos.common.NuclosFatalException;

/**
 * This class open a wikipage for a component in standard browser
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:oliver.helbig@novabit.de">Oliver Helbig</a>
 * @author	<a href="mailto:corina.mandoki@novabit.de">Corina Mandoki</a>
 * @version 01.00.00
 */

public class WikiController {
	//private static final Logger log = Logger.getLogger(WikiController.class);
	
	private static WikiController singleton;
	
	/**
	 * @return the one (and only) instance of WikiController
	 */
	public static synchronized WikiController getInstance() {
		if (singleton == null) {
			singleton = new WikiController();
		}
		return singleton;
	}
	
	
	/**
	 * @return BaseURL of Wiki.
	 */
	public String getBaseURL() {
		if (ClientParameterProvider.getInstance().getValue(ClientParameterProvider.KEY_WIKI_BASE_URL) == null) {
			throw new NuclosFatalException(CommonLocaleDelegate.getMessage("WikiController.1", "Die BaseURL f\u00fcr das Wiki muss in T_AD_PARAMETER angegeben werden."));
		}
		return ClientParameterProvider.getInstance().getValue(ClientParameterProvider.KEY_WIKI_BASE_URL);
	}
	
	/** 
	 * open an URL in Standardbrowser
	 * @param sURL
	 */
	public void openURLinBrowser(String sURL) {
		try {
			Desktop.getDesktop().browse(new URI(getBaseURL()+sURL));
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		catch(URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * get wiki page for the given component
	 * @param comp the component
	 * @param clctctrl CollectController 
	 * @return wiki page for the given component
	 */
	public String getWikiPageForComponent(Component comp, CollectController<?> clctctrl) {		
		boolean bGeneralMapping = false;
		
		String sEntityName = null;
		String sComponentName = null;
		
		if (comp != null) {			
			if (clctctrl != null) {
				sEntityName = clctctrl.getEntityName();
			}
			else {
				bGeneralMapping = true;
			}
			
			if (bGeneralMapping) {
				//for application general components (menubar, explorer ...) exists always a parent
				sComponentName = comp.getName() != null ? comp.getName() : comp.getParent().getName();
				
				return WikiDelegate.getInstance().getWikiPageFor(sComponentName);
			}
			else {
				sComponentName = comp.getName();
				if (sComponentName == null && comp.getParent() != null) {
					sComponentName = comp.getParent().getName();	// For combo boxes and other components with editors
				}
				if (sComponentName != null) {
					int iDotPos = sComponentName.indexOf(".");
					if (iDotPos != -1) {
						sComponentName = sComponentName.substring(0, iDotPos);
					}
				}
			
				if(comp.getParent() instanceof SubForm.SubFormTable){
					SubForm subform = (SubForm) comp.getParent().getParent().getParent().getParent();
					sEntityName = subform.getEntityName();
				}

				CollectableComponentModel ccm = clctctrl.getDetailsPanel().getEditModel().getCollectableComponentModelFor(sComponentName);
				if (ccm != null) {
					CollectableEntityField clctef = ccm.getEntityField();
					if(clctef != null) {
						sComponentName = clctef.getName();
					} 
					else {
						sComponentName = null;
					}
				}

				if(isMappingEnabled()) {					
					return WikiDelegate.getInstance().getWikiPageFor(sEntityName, sComponentName);
				}
				else {
					return removeInvalidCharacters(sComponentName);
				}
			}
			
		}
		else {
			throw new NuclosFatalException(CommonLocaleDelegate.getMessage("WikiController.2", "Keine Komponente zum Anzeigen im Wiki gefunden."));
		}
	}
	
	
	/**
	 * @param sComponentName
	 * @return adjusted component name. Several wiki not allow some special characters. This will be in t_ad_parameter defined.
	 */
	private String removeInvalidCharacters(String sComponentName) {	
		String characters = ClientParameterProvider.getInstance().getValue(ClientParameterProvider.KEY_WIKI_INVALID_CHARACTERS);
		Pattern pattern = Pattern.compile(characters);
  		Matcher matcher = pattern.matcher(sComponentName);
		
  		return matcher.replaceAll(" ");
	}
	
	/**
	 * @return true: mapping is enabled false: mapping is not enabled
	 */
	public boolean isMappingEnabled() {
		Integer mapping = Integer.parseInt(ClientParameterProvider.getInstance().getValue(ClientParameterProvider.KEY_WIKI_MAPPING_ENABLED));
		switch (mapping) {
		case 0:
			return false;
		case 1:
			return true;
		default:
			break;
		}
		return false;
	}
}
