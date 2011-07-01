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

import javax.ejb.CreateException;

import org.apache.commons.lang.NullArgumentException;

import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.server.attribute.ejb3.AttributeFacadeRemote;
import org.nuclos.server.attribute.valueobject.AttributeCVO;
import org.nuclos.server.common.NuclosUpdateException;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;

/**
 * Business Delegate for <code>AttributeFacadeBean</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
@SuppressWarnings("deprecation")
public class AttributeDelegate {
	private static AttributeDelegate singleton;

	private AttributeFacadeRemote facade;

	public static synchronized AttributeDelegate getInstance() {
		if (singleton == null) {
			singleton = new AttributeDelegate();
		}
		return singleton;
	}

	private AttributeDelegate() {
		this.facade = newAttributeFacade();
	}

	private static AttributeFacadeRemote newAttributeFacade() {
		try {
			return ServiceLocator.getInstance().getFacade(AttributeFacadeRemote.class);
		}
		catch (RuntimeException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	/**
	 * @param iGroupId if null, all attributes are returned.
	 * @return all dynamic attributes (for all modules) that belong to the given group (if any)
	 */
	public Collection<AttributeCVO> getAllAttributeCVOs(Integer iGroupId) {
		try {
			return this.facade.getAttributes(iGroupId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}	// getAttributes

	public AttributeCVO update(AttributeCVO attrcvo, DependantMasterDataMap mpmdvoDependants) throws CommonBusinessException, CreateException{
		if (attrcvo == null) {
			throw new NullArgumentException("attrcvo");
		}
		try {
			// workaround: The server cannot return the new values of columns that were joined into the view.
			// So we call get() again (in another transaction) to get the changed values:
			return this.facade.get(this.facade.modify(attrcvo, mpmdvoDependants));
		}
		catch (RuntimeException ex) {
			throw new NuclosUpdateException(null, ex);
		}
		catch (CommonCreateException ex) {
			throw new NuclosUpdateException(null, ex);
		}

	}

	/**
	 * @param attrcvo
	 * @return
	 * @throws CommonBusinessException
	 * @postcondition result != null
	 */
	public AttributeCVO create(AttributeCVO attrcvo, DependantMasterDataMap mpmdvoDependants) throws CommonBusinessException {
		try {
			return this.facade.create(attrcvo, mpmdvoDependants);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public void remove(AttributeCVO attrcvo) throws CommonBusinessException {
		try {
			this.facade.remove(attrcvo);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

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
