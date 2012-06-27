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
package org.nuclos.client.masterdata.wiki;

import org.nuclos.common.NuclosFatalException;
import org.nuclos.server.wiki.ejb3.WikiFacadeRemote;

/**
 * Business Delegate for <code>WikiFacade</code>
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:corina.mandoki@novabit.de">Corina Mandoki</a>
 * @version 01.00.00
 */

public class WikiDelegate {
	
	private static WikiDelegate INSTANCE;

	// Spring injection
	
	private WikiFacadeRemote wikiFacadeRemote;
	
	// end of Spring injection


	/**
	 * Use getInstance() to create an (the) instance of this class
	 */
	private WikiDelegate() {
		INSTANCE = this;
	}

	public static WikiDelegate getInstance() {
		if (INSTANCE == null) {
			throw new IllegalStateException("too early");
		}
		return INSTANCE;
	}
	
	public final void setWikiFacadeRemote(WikiFacadeRemote wikiFacadeRemote) {
		this.wikiFacadeRemote = wikiFacadeRemote;
	}

	public String getWikiPageFor(String sEntityName, String sAttributeName) {
		try {
			return this.wikiFacadeRemote.getWikiPageFor(sEntityName, sAttributeName);
		}
		catch (RuntimeException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	public String getWikiPageFor(String sComponentName) {
		try {
			return this.wikiFacadeRemote.getWikiPageFor(sComponentName);
		}
		catch (RuntimeException ex) {
			throw new NuclosFatalException(ex);
		}
	}

}
