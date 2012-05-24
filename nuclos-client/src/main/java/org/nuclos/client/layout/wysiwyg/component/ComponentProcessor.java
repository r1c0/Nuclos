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
package org.nuclos.client.layout.wysiwyg.component;

import java.awt.Component;

import org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation;
import org.nuclos.client.layout.wysiwyg.component.properties.ComponentProperties;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * This interface must be implemented to create a new empty Component.<br>
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:maik.stueker@novabit.de">maik.stueker</a>
 * @version 01.00.00
 */
public interface ComponentProcessor {

	/**
	 * This Method is called to create a new Component with the given Parameters.<br>
	 * The Component returned is a "default" Component.
	 * Individual Settings for the Component are set with the {@link ComponentProperties}.<br>
	 * 
	 * Creates a new empty Set of {@link ComponentProperties}.<br>
	 * 
	 * @param iNumber ongoing Number for enumerating the Components (e.g. Label_1)
	 * @param metaInf the {@link WYSIWYGMetaInformation} for getting values etc.
	 * @param name the Name of the Component (the name of the Component)
	 * @return {@link Component} instance of {@link WYSIWYGComponent}
	 * @throws CommonBusinessException
	 */
	Component createEmptyComponent(Integer iNumber, WYSIWYGMetaInformation metaInf, String name) throws CommonBusinessException;
}
