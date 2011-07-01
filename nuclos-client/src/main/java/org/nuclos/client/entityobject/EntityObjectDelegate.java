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
package org.nuclos.client.entityobject;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common2.EntityAndFieldName;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.masterdata.ejb3.EntityFacadeRemote;

public class EntityObjectDelegate {

	private static EntityObjectDelegate singleton;

	private final Logger log = Logger.getLogger(this.getClass());

	private final EntityFacadeRemote facade;

	public EntityObjectDelegate() {
		this.facade = ServiceLocator.getInstance().getFacade(EntityFacadeRemote.class);
	}

	public static synchronized EntityObjectDelegate getInstance() {
		if (singleton == null) {
			try {
				singleton = new EntityObjectDelegate();
			}
			catch (RuntimeException ex) {
				throw new CommonFatalException(ex);
			}
		}
		return singleton;
	}

	public List<CollectableField> getCollectableFieldsByName(
		String sEntityName,
		String sFieldName,
		boolean bCheckValidity) {
		return facade.getCollectableFieldsByName(sEntityName, sFieldName, bCheckValidity);
	}

	public Map<EntityAndFieldName, String> getSubFormEntityAndParentSubFormEntityNames(
		String sEntity, Integer ilaoyutId) throws RemoteException {
		return facade.getSubFormEntityAndParentSubFormEntityNames(sEntity,
			ilaoyutId);
	}


}
