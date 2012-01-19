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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.nuclos.client.genericobject.GenericObjectMetaDataCache;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;

/**
 * Value list provider for all subform entity names of a module
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:corina.mandoki@novabit.de">Corina Mandoki</a>
 * @version	01.00.00
 */
public class SubformEntityCollectableFieldsProvider implements CollectableFieldsProvider{

	private static Logger log = Logger.getLogger(SubformEntityCollectableFieldsProvider.class);
	String module = null;
	
	@Override
    public void setParameter(String sName, Object oValue) {
		if (sName.equals("module")) {
			this.module = (String)oValue;
		}
	}
	
	@Override
    public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		log.debug("getCollectableFields");
		
		Collection<MasterDataMetaVO> collmdmetavo = new ArrayList<MasterDataMetaVO>();
		
		Integer iModuleId;
		
		if (Modules.getInstance().existModule(module)) {
			iModuleId = Modules.getInstance().getModuleIdByEntityName(module);
		}
	    else {
			//if no such module, then no subforms for this module (the module is new)
			iModuleId = -1;
		}
		
		if (module == null) {
			for(String sSubform : GenericObjectMetaDataCache.getInstance().getSubFormEntityNamesByModuleId(null)) {
				try {
					collmdmetavo.add(MasterDataDelegate.getInstance().getMetaData(sSubform));
				} catch (Exception e) {
					// ignore! Subform does not exists any more...
				}
			}				

			final List<CollectableField> result = CollectionUtils.transform(collmdmetavo, new Transformer<MasterDataMetaVO, CollectableField>() {
				@Override
                public CollectableField transform(MasterDataMetaVO mdmetavo) {
					return new CollectableValueField(mdmetavo.getEntityName());
				}
			});
			Collections.sort(result);
			
			return result;
		}
		else {
			for(String sSubform : GenericObjectMetaDataCache.getInstance().getSubFormEntityNamesByModuleId(iModuleId)) {
				collmdmetavo.add(MasterDataDelegate.getInstance().getMetaData(sSubform));				
			}				

			final List<CollectableField> result = CollectionUtils.transform(collmdmetavo, new Transformer<MasterDataMetaVO, CollectableField>() {
				@Override
                public CollectableField transform(MasterDataMetaVO mdmetavo) {
					return new CollectableValueField(mdmetavo.getEntityName());
				}
			});
			
			
			Collections.sort(result);
			return result;
		}
		
	}
 	
}

