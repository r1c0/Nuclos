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
package org.nuclos.client.masterdata;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableForeignKeyField;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * A CollectableField representing a foreign key to a master data entity.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
public class CollectableMasterDataForeignKeyField extends CollectableForeignKeyField {

	public CollectableMasterDataForeignKeyField(CollectableEntityField clctef, Object iValueId) {
		super(clctef, iValueId);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected Object readValue(CollectableEntityField clctef, Object iValueId) {
		Object result;
		try {
			final MasterDataVO mdvo = MasterDataDelegate.getInstance().get(clctef.getReferencedEntityName(), iValueId, true);
			result = mdvo.getField(clctef.getReferencedEntityFieldName());
		}
		catch (CommonFinderException ex) {
			result = null;
		}
		catch(CommonPermissionException ex){
			result = null;
		}
		return result;
	}

	@Override
	public String toDescription() {
		final ToStringBuilder b = new ToStringBuilder(this).append(oValueId).append(oValue);
		return b.toString();
	}

}	// class CollectableMasterDataForeignKeyField

