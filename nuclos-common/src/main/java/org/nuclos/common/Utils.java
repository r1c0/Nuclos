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
package org.nuclos.common;

import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;

import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMapImpl;
import org.nuclos.server.navigation.ejb3.TreeNodeFacadeRemote;

/**
 * Common utility methods for Nucleus.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
public class Utils {

	/** @todo eliminate these constants */
	@Deprecated
	public static final Calendar MINDATE = GregorianCalendar.getInstance();
	@Deprecated
	public static final Calendar MAXDATE = GregorianCalendar.getInstance();

	static {
		MINDATE.clear();
		MINDATE.set(1900, 0, 1);
		MAXDATE.clear();
		MAXDATE.set(2900, 0, 1);
	}

	private Utils() {
	}

	/**
	 * transforms the given Collection into one containing <code>null</code> ids.
	 * @return
	 * @todo eliminate this workaround!
	 */
	@Deprecated
	public static Collection<EntityObjectVO> clearIds(final Collection<EntityObjectVO> collmdvo) {
		class ClearId implements Transformer<EntityObjectVO, EntityObjectVO> {
			@Override
			public EntityObjectVO transform(EntityObjectVO mdvo) {
				final EntityObjectVO result = mdvo.copy();
				/** @todo Should copy() respect the removed property? */
				if (mdvo.isFlagRemoved()) {
					result.flagRemove();
				}
				assert result.getId() == null;
				return result;
			}
		}

		return CollectionUtils.transform(collmdvo, new ClearId());
	}

	/**
	 * transforms the given map into one containing <code>null</code> ids.
	 * @return DependantMasterDataMap containing <code>null</code> ids.
	 * @todo eliminate this workaround!
	 */
	@Deprecated
	public static DependantMasterDataMap clearIds(DependantMasterDataMap mpDependants) {
		final DependantMasterDataMap result = new DependantMasterDataMapImpl();
		for (String sEntityName : mpDependants.getEntityNames()) {
			for (EntityObjectVO mdVO : mpDependants.getData(sEntityName)) {
				mdVO.setDependants(clearIds(mdVO.getDependants()));
			}
			result.addAllData(sEntityName, clearIds(mpDependants.getData(sEntityName)));
		}
		return result;
	}

	/**
	 * @return remote interface of tree node facade
	 */
	public static TreeNodeFacadeRemote getTreeNodeFacade() {
		try {
			return ServiceLocator.getInstance().getFacade(TreeNodeFacadeRemote.class);
		}
		catch (Exception ex) {
			throw new NuclosFatalException(ex);
		}
	}

}	// class Utils
