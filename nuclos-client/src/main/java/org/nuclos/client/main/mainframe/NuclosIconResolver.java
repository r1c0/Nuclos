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
import org.nuclos.client.NuclosIcons;
import org.nuclos.client.ui.Icons;

public class NuclosIconResolver implements IconResolver {
	
	private static final Logger LOG = Logger.getLogger(NuclosIconResolver.class);
	
	@Override
	public ImageIcon getIcon(String method) {
		try {
			return (ImageIcon) NuclosIcons.class.getMethod(method).invoke(NuclosIcons.getInstance()); 
		} catch (Exception ex) {
			LOG.warn(String.format("Nuclos icon (method=%s) not resolved", method), ex);
			return null;
		}
	}

}
