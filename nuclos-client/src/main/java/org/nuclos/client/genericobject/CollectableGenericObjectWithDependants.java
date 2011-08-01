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
package org.nuclos.client.genericobject;

import java.util.Collection;
import java.util.List;

import org.nuclos.client.common.Utils;
import org.nuclos.client.entityobject.CollectableEntityObject;
import org.nuclos.client.masterdata.CollectableWithDependants;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityProvider;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.entityobject.CollectableEOEntity;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;

/**
 * Makes a <code>GenericObjectWithDependantsVO</code> <code>Collectable</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class CollectableGenericObjectWithDependants extends CollectableGenericObject implements CollectableWithDependants {

	/**
	 * @param govo
	 * @return
	 * @precondition govo != null
	 * @deprecated Does not honour dependant objects, use {@link #newCollectableGenericObjectWithDependants}.
	 */
	public static CollectableGenericObjectWithDependants newCollectableGenericObject(GenericObjectVO govo) {
		return new CollectableGenericObjectWithDependants(new GenericObjectWithDependantsVO(govo, new DependantMasterDataMap()));
	}

	/**
	 * @author Thomas Pasch
	 * @since Nuclos 3.1.01
	 */
	public static CollectableGenericObjectWithDependants newCollectableGenericObjectWithDependants(GenericObjectVO govo) {
		final DependantMasterDataMap dep;
		if (govo instanceof GenericObjectWithDependantsVO) {
			final GenericObjectWithDependantsVO gowd = (GenericObjectWithDependantsVO) govo;
			dep = gowd.getDependants();
		}
		else {
			dep = new DependantMasterDataMap();
		}
		final CollectableGenericObjectWithDependants result = new CollectableGenericObjectWithDependants(
				new GenericObjectWithDependantsVO(govo, dep));
		return result;
	}

	public CollectableGenericObjectWithDependants(GenericObjectWithDependantsVO lowdcvo) {
		super(lowdcvo);
	}

	/**
	 * @return the GenericObjectWithDependantsVO wrapped by this object.
	 */
	public GenericObjectWithDependantsVO getGenericObjectWithDependantsCVO() {
		return (GenericObjectWithDependantsVO) this.getGenericObjectCVO();
	}

	/**
	 * @param sSubEntityName
	 * @return a copy of the contained dependants for the subentity with the given name.
	 * @postcondition result != null
	 */
	@Override
	public Collection<CollectableEntityObject> getDependants(String sSubEntityName) {
		CollectableEntity ce = DefaultCollectableEntityProvider.getInstance().getCollectableEntity(sSubEntityName);
		if(ce instanceof CollectableEOEntity) {
			final CollectableEOEntity clctmde = (CollectableEOEntity) ce;
			final Collection<EntityObjectVO> collmdvoDependants = this.getGenericObjectWithDependantsCVO().getDependants().getData(sSubEntityName);
			final List<CollectableEntityObject> result = CollectionUtils.transform(collmdvoDependants, new CollectableEntityObject.MakeCollectable(clctmde));
			assert result != null;
			return result;
		}
		else {
			final CollectableMasterDataEntity clctmde = (CollectableMasterDataEntity)ce;
			final Collection<EntityObjectVO> collmdvoDependants = this.getGenericObjectWithDependantsCVO().getDependants().getData(sSubEntityName);
			final CollectableEOEntity cee = Utils.transformCollectableMasterDataEntityTOCollectableEOEntity(clctmde);
			final List<CollectableEntityObject> result = CollectionUtils.transform(collmdvoDependants, new CollectableEntityObject.MakeCollectable(cee));
			assert result != null;
			return result;
		}
		
	}
	
	/**
	 * inner class MakeCollectable
	 */
	public static class MakeCollectable implements Transformer<GenericObjectWithDependantsVO, CollectableGenericObjectWithDependants> {
		@Override
		public CollectableGenericObjectWithDependants transform(GenericObjectWithDependantsVO lowdcvo) {
			return new CollectableGenericObjectWithDependants(lowdcvo);
		}
	}

}	// class CollectableGenericObjectWithDependants
