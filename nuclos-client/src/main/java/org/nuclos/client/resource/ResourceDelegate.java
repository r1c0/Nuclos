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

import java.util.HashSet;
import java.util.Set;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.common.NuclosUpdateException;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMapImpl;
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
// @Component
public class ResourceDelegate {

	private static final Logger LOG = Logger.getLogger(ResourceDelegate.class);

	private static ResourceDelegate INSTANCE;
	
	//

	private ResourceFacadeRemote facade;
	
	private ResourceCache resourceCache;

	/**
	 * Use getInstance() to create an (the) instance of this class
	 */
	ResourceDelegate() throws RuntimeException {
		INSTANCE = this;
	}

	public static ResourceDelegate getInstance() {
		return INSTANCE;
	}
	
	// @Autowired
	public final void setResourceCache(ResourceCache resourceCache) {
		this.resourceCache = resourceCache;
	}

	// @Autowired
	public final void setResourceFacadeRemote(ResourceFacadeRemote facade) {
		this.facade = facade;
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

	public boolean containsIconResources() {
		for (String resName : getResourceNames()) {
			try {
				ImageIcon ico = resourceCache.getIconResource(resName);
				if (ico.getIconWidth() > 0 && ico.getIconHeight() > 0) {
					return true;
				}
			} catch (Exception ex) {
				// do nothing
			}
		}
		return false;
	}

	public Set<String> getIconResources() {
		Set<String> result = new HashSet<String>();
		for (String resName : getResourceNames()) {
			try {
				ImageIcon ico = resourceCache.getIconResource(resName);
				if (ico.getIconWidth() > 0 && ico.getIconHeight() > 0) {
					result.add(resName);
				}
			} catch (Exception ex) {
				// do nothing
			}
		}
		return result;
	}

	public Set<String> getResourceNames() {
		try {
			return facade.getResourceNames();
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}
}
