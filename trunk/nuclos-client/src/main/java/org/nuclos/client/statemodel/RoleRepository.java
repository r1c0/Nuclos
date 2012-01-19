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
package org.nuclos.client.statemodel;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common.NuclosEntity;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeRemote;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Repository for roles.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class RoleRepository {
	private static RoleRepository singleton;

	private final Map<Object, MasterDataVO> mpRoles = CollectionUtils.newHashMap();

	public static synchronized RoleRepository getInstance() throws RemoteException {
		if (singleton == null) {
			singleton = new RoleRepository();
		}
		return singleton;
	}

	protected RoleRepository() throws RemoteException {
		updateRoles();
	}

	public void updateRoles() throws RemoteException {
		mpRoles.clear();

		final MasterDataFacadeRemote facade = ServiceLocator.getInstance().getFacade(MasterDataFacadeRemote.class);
		for (MasterDataVO mdvo : facade.getMasterData(NuclosEntity.ROLE.getEntityName(), null, true)) {
			mpRoles.put(mdvo.getId(), mdvo);
		}
	}

	public MasterDataVO getRole(Integer iId) {
		return mpRoles.get(iId);
	}

	/**
	 *
	 * @param collID
	 * @return
	 */
	public List<MasterDataVO> selectRolesById(Collection<Integer> collID) {
		final List<MasterDataVO> result = new LinkedList<MasterDataVO>();

		for (Iterator<Integer> i = collID.iterator(); i.hasNext();) {
			final Integer iId = i.next();
			final MasterDataVO cvo = mpRoles.get(iId);
			if (cvo != null) {
				result.add(cvo);
			}
		}
		return result;
	}

	/**
	 *
	 * @param collCVO
	 * @return
	 */
	public List<MasterDataVO> filterRolesByVO(Collection<MasterDataVO> collCVO) {
		Map<Object, MasterDataVO> filterMap = new HashMap<Object, MasterDataVO>();
		List<MasterDataVO> result = new LinkedList<MasterDataVO>();

		for (Iterator<MasterDataVO> i = collCVO.iterator(); i.hasNext();) {
			final MasterDataVO cvo = i.next();
			filterMap.put(cvo.getId(), cvo);
		}
		for (Iterator<MasterDataVO> i = mpRoles.values().iterator(); i.hasNext();) {
			final MasterDataVO cvo = i.next();
			if (!filterMap.containsKey(cvo.getId())) {
				result.add(cvo);
			}
		}
		return result;
	}
}
