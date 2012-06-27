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
package org.nuclos.client.layout;

import java.util.Collection;

import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.server.attribute.ejb3.LayoutFacadeRemote;
import org.nuclos.server.attribute.valueobject.LayoutVO;

/**
 * Business Delegate for <code>LayoutFacadeBean</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class LayoutDelegate {

	private static LayoutDelegate INSTANCE;
	
	// Spring injection

	private LayoutFacadeRemote layoutFacadeRemote;
	
	// end of Spring injection

	public static LayoutDelegate getInstance() {
		if (INSTANCE == null) {
			throw new IllegalStateException("too early");
		}
		return INSTANCE;
	}

	LayoutDelegate() {
		INSTANCE = this;
	}
	
	public final void setLayoutFacadeRemote(LayoutFacadeRemote layoutFacadeRemote) {
		this.layoutFacadeRemote = layoutFacadeRemote;
	}

	/**
	 * @param colllayoutvo
	 * @throws CommonBusinessException
	 */
	public void importLayouts(Collection<LayoutVO> colllayoutvo) throws CommonBusinessException {
		try {
			this.getLayoutFacade().importLayouts(colllayoutvo);
		}
		catch (RuntimeException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	/**
	 * refreshes the generic object views (console function)
	 */
	public void refreshAll() {
		try {
			this.getLayoutFacade().refreshAll();
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * gets the layoutFacadeRemote once for this object and stores it in a member variable.
	 */
	private LayoutFacadeRemote getLayoutFacade() throws NuclosFatalException {
		return this.layoutFacadeRemote;
	}

}	// class LayoutDelegate
