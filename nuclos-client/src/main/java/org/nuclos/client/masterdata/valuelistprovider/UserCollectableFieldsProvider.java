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
package org.nuclos.client.masterdata.valuelistprovider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.nuclos.client.masterdata.MasterDataCache;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.LocalizedCollectableValueField;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * {@link CollectableFieldsProvider} which is filled with the values of a given Enum class.
 */
public class UserCollectableFieldsProvider implements CollectableFieldsProvider {

	private String entity = NuclosEntity.USER.getEntityName();
	
	@Override
	public void setParameter(String sName, Object oValue) {
//		if (sName.equals("entity")) {
//			entity = (String) oValue;
//		}
	}

	@Override
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		List<CollectableField> result = new ArrayList<CollectableField>();
		List<MasterDataVO> users = MasterDataCache.getInstance().get(entity);
        List<LocalizedCollectableValueField> lcfs = CollectionUtils.transform(users, new Transformer<MasterDataVO,LocalizedCollectableValueField>(){
    		@Override
    		public LocalizedCollectableValueField transform(MasterDataVO mvo) {
    			return new LocalizedCollectableValueField(mvo.getField("name", String.class), mvo.getField("lastname", String.class)+","+mvo.getField("firstname", String.class));
    		}}
        );
        result.addAll(lcfs);
 		
//		
//		
//		List<CollectableField> result = new ArrayList<CollectableField>();
//
//		try {
//			// asSubclass does not work with wild cards types (i.e. Enum<?>)...
//			Class<? extends Enum> clazz = Class.forName(this.showEnum).asSubclass(Enum.class);
//			for (Enum e : clazz.getEnumConstants()) {
//				Object value = (e instanceof KeyEnum) ? ((KeyEnum) e).getValue() : e.name();
//				String text = (e instanceof Localizable) ? SpringLocaleDelegate.getText((Localizable) e) : e.toString();
//				CollectableField cf = new LocalizedCollectableValueField(value, text);
//				result.add(cf);
//			}
//		} catch (Exception e) {
//			throw new CommonBusinessException("Invalid parameter: ", e);
//		}

		Collections.sort(result);
		return result;
	}

}	// class RoleActionsCollectableFieldsProvider
