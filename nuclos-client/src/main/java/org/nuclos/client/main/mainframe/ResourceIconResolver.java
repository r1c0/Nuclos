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

package org.nuclos.client.main.mainframe;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.nuclos.client.resource.ResourceCache;

public class ResourceIconResolver implements IconResolver {

	private static final Logger LOG = Logger.getLogger(ResourceIconResolver.class);
	
	public static String RESOURCEID_PREFIX = "resourceid=";
	
	@Override
	public ImageIcon getIcon(String icon) {
		if (icon != null &&icon.startsWith(RESOURCEID_PREFIX)) {
			try {
				Integer resourceId = Integer.parseInt(icon.substring(RESOURCEID_PREFIX.length()));
				return ResourceCache.getInstance().getIconResource(resourceId);
			} catch (Exception ex) {
				LOG.warn(String.format("Icon %s not resolved", icon), ex);
				return null;
			}
		} else {
			return ResourceCache.getInstance().getIconResource(icon);
		}
	}

}
