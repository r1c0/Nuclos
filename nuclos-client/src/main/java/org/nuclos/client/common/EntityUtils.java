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
/*
 * Created on 23.05.2005
 *
 */
package org.nuclos.client.common;

import java.util.Map;

import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

public class EntityUtils {	
	
	public static boolean hasEntityDocumentType(String entity) {
		boolean yes = false;
		final Map<String, EntityFieldMetaDataVO> mpFields = MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(entity);
		for(String sField : mpFields.keySet()) {
			if("org.nuclos.server.genericobject.valueobject.GenericObjectDocumentFile".equals(mpFields.get(sField).getDataType())) {
				return true;
			}
		}
		
		return yes;
	}
	
	public static boolean hasEntityGeneralDocumentSubform(String entity) {
		boolean yes = false;
		
		CollectableComparison compare = SearchConditionUtils.newComparison(NuclosEntity.LAYOUTUSAGE.getEntityName(), "entity", ComparisonOperator.EQUAL, entity);
		for(MasterDataVO layout : MasterDataDelegate.getInstance().getMasterData(NuclosEntity.LAYOUTUSAGE.getEntityName(), compare)) {
			Integer iLayoutId = (Integer)layout.getField("layoutId");
			MasterDataVO voLayout;
			try {
				voLayout = MasterDataDelegate.getInstance().get(NuclosEntity.LAYOUT.getEntityName(), iLayoutId);
				String sLayout = (String)voLayout.getField("layoutML");
				if(sLayout.indexOf("entity=\"nuclos_generalsearchdocument\"") >= 0) 
					return true;
			}
			catch(CommonFinderException e) {
				e.printStackTrace();
			}
			catch(CommonPermissionException e) {
				e.printStackTrace();
			}
			
			
				
		}
		return yes;
	}
	
	public static boolean hasAnySubformDocumentType(String entity) {
		boolean yes = false;		
		
		for(EntityMetaDataVO voEntity : MetaDataClientProvider.getInstance().getAllEntities()) {
			if(voEntity.getEntity().equals(entity))
				continue;
			
			for(EntityFieldMetaDataVO voField : MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(voEntity.getEntity()).values()) {
				if(voField.getForeignEntity() != null && voField.getForeignEntity().equals(entity) && voField.getForeignEntityField() == null) { 
					return hasEntityDocumentType(voEntity.getEntity());
				}
			}
			
		}
		
		return yes;
	}

}
