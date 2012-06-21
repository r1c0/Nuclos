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
package org.nuclos.client.attribute;

import java.util.Collection;
import java.util.Set;

import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.attribute.ejb3.AttributeFacadeRemote;
import org.nuclos.server.attribute.valueobject.AttributeCVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Business Delegate for <code>AttributeFacadeBean</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
// @Component
// @Lazy
public class AttributeDelegate {
	
	private static AttributeDelegate INSTANCE;

	// 
	
	private AttributeFacadeRemote facade;

	public static AttributeDelegate getInstance() {
		if (INSTANCE.facade == null) throw new NullPointerException("too early");
		return INSTANCE;
	}

	private AttributeDelegate() {
		INSTANCE = this;
	}
	
	// @Autowired
	public final void setAttributeFacadeRemote(AttributeFacadeRemote facade) {
		this.facade = facade;
	}

	/**
	 * @param iGroupId if null, all attributes are returned.
	 * @return all dynamic attributes (for all modules) that belong to the given group (if any)
	 */
	public Collection<AttributeCVO> getAllAttributeCVOs(Integer iGroupId) {
		try {
			return facade.getAttributes(iGroupId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}	// getAttributes

	/**
	 * invalidates the attribute cache (console function)
	 */
	public void invalidateCache() {
		try {
			this.facade.invalidateCache();
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * get the available calculation functions for dynamic attributes
	 */
	public Collection<String> getCalculationFunctions() {
		try {
			return this.facade.getCalculationFunctions();
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * get the layouts that contained this attribute
	 */
	public Set<String> getAttributeLayouts(String sAttributeName){
		try{
			return this.facade.getAttributeLayouts(sAttributeName);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}
	
	public Set<String> getAttributeForModule(String sModuleId) {
		try{
			return this.facade.getAttributeForModule(sModuleId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}
}	// class AttributeDelegate
