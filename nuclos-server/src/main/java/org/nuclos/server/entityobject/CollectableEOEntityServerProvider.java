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
package org.nuclos.server.entityobject;

import org.nuclos.common.entityobject.CollectableEOEntityProvider;
import org.nuclos.server.common.MetaDataServerProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A CollectableEntityProvider implementation singleton for use on the server side.
 * 
 * @author Thomas Pasch
 * @since Nuclos 3.1.01
 */
@Component
public class CollectableEOEntityServerProvider extends CollectableEOEntityProvider {
	
	private static CollectableEOEntityServerProvider INSTANCE;
	
	@Autowired
	CollectableEOEntityServerProvider(MetaDataServerProvider prov) {
		super(prov);
		INSTANCE = this;
	}
	
	public static CollectableEOEntityProvider getInstance() {
		return INSTANCE;
	}

}
