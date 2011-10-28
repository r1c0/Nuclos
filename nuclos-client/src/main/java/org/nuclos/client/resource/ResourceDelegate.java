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

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.common.NuclosUpdateException;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.resource.ejb3.ResourceFacadeRemote;
import org.nuclos.server.resource.valueobject.ResourceVO;

/**
 * Business Delegate for <code>ResourceFacade</code>
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:corina.mandoki@novabit.de">Corina Mandoki</a>
 * @version 01.00.00
 */
public class ResourceDelegate {

	private static final Logger LOG = Logger.getLogger(ResourceDelegate.class);

	private static ResourceDelegate singleton;

	private final ResourceFacadeRemote facade;

	/**
	 * Use getInstance() to create an (the) instance of this class
	 */
	private ResourceDelegate() throws RuntimeException {
		this.facade = ServiceLocator.getInstance().getFacade(ResourceFacadeRemote.class);
	}

	public static synchronized ResourceDelegate getInstance() {
		if (singleton == null) {
			try {
				singleton = new ResourceDelegate();
			}
			catch (RuntimeException ex) {
				throw new CommonFatalException(ex);
			}
		}
		return singleton;
	}


	public ResourceVO getResourceByName(String sResourceName) {
		return facade.getResourceByName(sResourceName);
	}

	public ResourceVO getResourceById(Integer iResourceId) {
		return facade.getResourceById(iResourceId);
	}

	public Pair<ResourceVO, byte[]> getResource(String sResourceName) {
		return facade.getResource(sResourceName);
	}

	public Pair<ResourceVO, byte[]> getResource(Integer resourceId) {
		return facade.getResource(resourceId);
	}
	
	public MasterDataVO create(String sEntityName, MasterDataVO mdvo, DependantMasterDataMap mpDependants) throws CommonBusinessException {
		try {
			if (mdvo.getId() != null) {
				throw new IllegalArgumentException("mdvo");
			}
			return facade.create(sEntityName, mdvo, mpDependants);
		}
		catch(RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
		catch (CommonCreateException ex) {
			throw new NuclosBusinessException(ex.getMessage(), ex);
		}
	}

	public Object modify(String sEntityName, MasterDataVO mdvo, DependantMasterDataMap mpDependants) throws CommonBusinessException {
		if (mdvo.getId() == null) {
			throw new IllegalArgumentException("mdvo");
		}

		final Logger log = Logger.getLogger(ResourceDelegate.class);
		if (log.isDebugEnabled()) {
			log.debug("UPDATE: " + mdvo.getDebugInfo());
			if (mpDependants != null) {
				log.debug("Dependants:");
				for (EntityObjectVO mdvoDependant : mpDependants.getAllData()) {
					log.debug(mdvoDependant.getDebugInfo());
				}
			}
		}
		try {
			return facade.modify(sEntityName, mdvo, mpDependants);
		}
		catch (RuntimeException ex) {
			if(ex.getCause() != null && (ex.getCause() instanceof CommonFatalException)){
				throw new NuclosUpdateException(ex.getCause().getMessage());
			} else {
				throw new NuclosUpdateException(ex);
			}
		}
	}

	public void remove(String sEntityName, MasterDataVO mdvo) throws CommonBusinessException {
		try {
			facade.remove(sEntityName, mdvo);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}
}
