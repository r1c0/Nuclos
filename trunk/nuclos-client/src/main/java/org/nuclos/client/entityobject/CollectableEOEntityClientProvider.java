//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.client.entityobject;

import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.common.entityobject.CollectableEOEntityProvider;

/**
 * A CollectableEntityProvider implementation singleton for use on the client side.
 * 
 * @author Thomas Pasch
 * @since Nuclos 3.1.01
 */
public class CollectableEOEntityClientProvider extends CollectableEOEntityProvider {
	
	private static final CollectableEOEntityClientProvider INSTANCE = new CollectableEOEntityClientProvider();
	
	private CollectableEOEntityClientProvider() {
		super(MetaDataClientProvider.getInstance());
	}
	
	public static CollectableEOEntityProvider getInstance() {
		return INSTANCE;
	}

}
