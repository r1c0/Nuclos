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
package org.nuclos.client.resource;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collection.CollectionUtils;

/**
 * A cache for resources.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:maik.stueker@novabit.de">maik.stueker</a>
 * @version 00.01.000
 */
public class NuclosResourceCache {
	
	private static final Logger LOG = Logger.getLogger(NuclosResourceCache.class);

	public static SortedSet<String> getNuclosResourceIcons() {
		return _getInstance()._getNuclosResourceIcons();
	}
	
	public static ImageIcon getNuclosResourceIcon(String resourceIcon) {
		return _getInstance()._getNuclosResourceIcon(resourceIcon);
	}

	private static NuclosResourceCache     instance;
	
	private Map<String, ImageIcon>    nuclosResourceIconsAll;
	private Map<String, ImageIcon>    nuclosResourceIconsVisible;
	
	private NuclosResourceCache() {
		nuclosResourceIconsAll = CollectionUtils.newHashMap();
		nuclosResourceIconsVisible = CollectionUtils.newHashMap();
		initNuclosResourceIcons();
	}
	
	private static NuclosResourceCache _getInstance() {
		if(instance == null)
			instance = new NuclosResourceCache();
		return instance;
	}
	
	private SortedSet<String> _getNuclosResourceIcons() {
		return new TreeSet<String>(nuclosResourceIconsVisible.keySet());
	}

	private ImageIcon _getNuclosResourceIcon(String resourceIcon) {
		return nuclosResourceIconsAll.get(resourceIcon);
	}
		
	private void initNuclosResourceIcons() {
		try {
			Properties prop = new Properties();
			prop.load(this.getClass().getClassLoader().getResourceAsStream("org/nuclos/client/resource/nuclos-resource-icon.properties"));
			for (Object key : prop.keySet()) {
				String sKey = ((String) key).trim();
				ImageIcon icon = getImageIcon(prop.getProperty(sKey).trim());
				nuclosResourceIconsAll.put(sKey, icon);
				nuclosResourceIconsVisible.put(sKey, icon);
			}
			
			prop = new Properties();
			prop.load(this.getClass().getClassLoader().getResourceAsStream("org/nuclos/client/resource/nuclos-resource-icon-hidden.properties"));
			for (Object key : prop.keySet()) {
				String sKey = ((String) key).trim();
				ImageIcon icon = getImageIcon(prop.getProperty(sKey).trim());
				nuclosResourceIconsAll.put(sKey, icon);
			}
		} catch(IOException e) {
			throw new NuclosFatalException(e);
		}
	}
	
	private ImageIcon getImageIcon(String name) {
		return new ImageIcon(this.getClass().getClassLoader().getResource(name));
	}
}
