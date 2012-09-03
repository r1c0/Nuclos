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
package org.nuclos.server.genericobject;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.server.genericobject.ejb3.GenericObjectFacadeRemote;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;

/**
 * Proxy list for leased object search results.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph Radig</a>
 * @version 00.01.000
 */
public class GenericObjectProxyList extends AbstractProxyList<Integer, GenericObjectWithDependantsVO> {

	private static final Logger LOG = Logger.getLogger(GenericObjectProxyList.class);

	private static GenericObjectFacadeRemote facade;

	private final Integer iModuleId;
	private final CollectableSearchExpression clctexpr;
	private final Set<Integer> stRequiredAttributeIds;
	private final Set<String> stRequiredSubEntities;
	private final boolean bIncludeParentObjects;
	
	private final String customUsage;

	/**
	 * @param iModuleId
	 * @param clctexpr
	 * @param iRecordCount
	 * @param stRequiredAttributeIds Set<Integer> may be <code>null</code>, which means all attributes are required
	 * @param stRequiredSubEntityNames Set<String>
	 * @param bIncludeSubModules
	 * @precondition stRequiredSubEntityNames != null
	 */
	public GenericObjectProxyList(Integer iModuleId, CollectableSearchExpression clctexpr, Set<Integer> stRequiredAttributeIds, Set<String> stRequiredSubEntityNames, boolean bIncludeParentObjects, String customUsage) {
		super();

		this.iModuleId = iModuleId;
		this.clctexpr = clctexpr;
		this.stRequiredAttributeIds = stRequiredAttributeIds;
		this.stRequiredSubEntities = stRequiredSubEntityNames;
		this.bIncludeParentObjects = bIncludeParentObjects;
		
		this.customUsage = customUsage;

		this.initialize();
	}

	public GenericObjectProxyList() {
		super();
		this.clctexpr = null;
		this.bIncludeParentObjects = false;
		this.stRequiredSubEntities = Collections.emptySet();
		this.stRequiredAttributeIds = null;
		this.iModuleId = null;
		this.customUsage = null;

		this.initialize();
	}

	@Override
	protected Collection<GenericObjectWithDependantsVO> fetchNextChunk(List<Integer> lstIntIds) throws RuntimeException {
		return this.getGenericObjectFacade().getGenericObjectsMore(this.iModuleId, lstIntIds, this.stRequiredAttributeIds, this.stRequiredSubEntities, this.customUsage, this.bIncludeParentObjects);
	}

	@Override
	protected void fillListOfIds() {
		try {
			List<Integer> lstIds = getGenericObjectFacade().getGenericObjectIds(iModuleId, clctexpr);

			setListOfIds(lstIds);
		}
		catch(RuntimeException e) {
			throw new NuclosFatalException(e);
		}
	}

	private synchronized GenericObjectFacadeRemote getGenericObjectFacade() {
		if (facade == null) {
			try {
				facade = ServiceLocator.getInstance().getFacade(GenericObjectFacadeRemote.class);
			}
			catch (RuntimeException ex) {
				throw new NuclosFatalException(ex);
			}
		}
		return facade;
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		final int size = size();
		result.append("GenericObjectProxyList[");
		result.append("size=").append(size);
		result.append(",reqFields=").append(stRequiredAttributeIds);
		result.append(",reqSubforms=").append(stRequiredSubEntities);
		result.append(",inclParent=").append(bIncludeParentObjects);
		result.append(",search=").append(clctexpr);
		mapDescription(result, mpObjects, 5);
		result.append("]");
		return result.toString();
	}
	
}	// class GenericObjectProxyList
