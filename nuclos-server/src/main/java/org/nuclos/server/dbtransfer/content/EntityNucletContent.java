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
package org.nuclos.server.dbtransfer.content;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nuclos.common.NuclosEntity;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.dal.DalCallResult;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dbtransfer.TransferOption;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.NuclosSystemParameters;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dbtransfer.NucletContentMap;
import org.nuclos.server.dbtransfer.TransferUtils;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeHelper;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;

public class EntityNucletContent extends DefaultNucletContent {

	public EntityNucletContent(List<INucletContent> contentTypes) {
		super(NuclosEntity.ENTITY, null, contentTypes);
	}
	
	@Override
	public String getIdentifierField() {
		return "entity";
	}

	@Override
	public boolean checkValidity(EntityObjectVO ncObject, ValidityType validity, NucletContentMap importContentMap, Set<Long> existingNucletIds, ValidityLogEntry log, TransferOption.Map transferOptions) {
		boolean result = super.checkValidity(ncObject, validity, importContentMap, existingNucletIds, log, transferOptions); 
		if (!result) log.newCriticalLine("Entity has critical validation errors: " + ncObject.getField("entity", String.class));
		
		switch (validity) {
			case INSERT:
			case UPDATE:
				// check if references are possible
				for (EntityObjectVO fieldVO : importContentMap.getValues(NuclosEntity.ENTITYFIELD)) {
					if (LangUtils.equals(fieldVO.getFieldId("entity"), ncObject.getId())) {
						String foreignentity = org.nuclos.common2.LangUtils.defaultIfNull(fieldVO.getField("foreignentity", String.class), fieldVO.getField("unreferencedforeignentity", String.class));
						if (foreignentity != null && !NuclosEntity.isNuclosEntity(foreignentity) && TransferUtils.getEntityObjectVO(importContentMap.getValues(getEntity()), "entity", foreignentity) == null) {
							log.newCriticalLine("Entity " + ncObject.getField("entity", String.class) + " references to unknown entity " + foreignentity);
							result = false;
						}
					}
				}
				
				if (!transferOptions.containsKey(TransferOption.IS_NUCLOS_INSTANCE)) {
					for (EntityObjectVO eo : NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.ENTITY).getAll()) {
						if (!existingNucletIds.contains(eo.getFieldId("nuclet"))) {
							// entity is not part of this import
							if (StringUtils.equalsIgnoreCase(eo.getField("dbentity", String.class), ncObject.getField("dbentity", String.class))) {
								log.newCriticalLine("Entity " + ncObject.getField("entity", String.class) + " uses an occupied table name (" + ncObject.getField("dbentity", String.class) + ")");
								result = false;
							}
							if (StringUtils.equalsIgnoreCase(eo.getField("entity", String.class), ncObject.getField("entity", String.class))) {
								log.newCriticalLine("Entity " + ncObject.getField("entity", String.class) + " uses an occupied entity name");
								result = false;
							}
						}
					}
				}
				
		}
		return result;
	}

	@Override
	public List<EntityObjectVO> getNcObjects(Set<Long> nucletIds, TransferOption.Map transferOptions) {
		List<EntityObjectVO> result = super.getNcObjects(nucletIds, transferOptions);
		for (EntityObjectVO ncObject : result) {
			storeLocaleResources(ncObject, "localeresourcel", "localeresourcem", "localeresourced", "localeresourcetw", "localeresourcett");
		}
		return result;
	}

	@Override
	public void insertOrUpdateNcObject(DalCallResult result, EntityObjectVO ncObject, boolean isNuclon) {
		restoreLocaleResources(ncObject);
		super.insertOrUpdateNcObject(result, ncObject, isNuclon);
	}

	@Override
	public void deleteNcObject(DalCallResult result, EntityObjectVO ncObject) {
		for (Long goId : getReferencingEOids(NuclosEntity.GENERICOBJECT, "module", ncObject.getId())) {
			result.add(removeReferencingEOs(NuclosEntity.STATEHISTORY, "genericObject", goId));
			result.add(removeReferencingEOs(NuclosEntity.GENERICOBJECTLOGBOOK, "genericObject", goId));
			result.add(removeReferencingEOs(NuclosEntity.GENERICOBJECTGROUP, "genericObject", goId));
			result.add(removeReferencingEOs(NuclosEntity.TIMELIMITTASK, "genericobject", goId));
			result.add(removeReferencingEOs(NuclosEntity.GENERICOBJECTRELATION, "source", goId));
			result.add(removeReferencingEOs(NuclosEntity.GENERICOBJECTRELATION, "destination", goId));
			result.add(removeReferencingEOs(NuclosEntity.GENERALSEARCHCOMMENT, "genericObject", goId));
			result.add(removeReferencingEOs(NuclosEntity.GENERALSEARCHDOCUMENT, "genericObject", goId));
			try {
				NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.GENERICOBJECT).delete(goId);
			}
			catch (DbException e) {
				result.addBusinessException(e);
			}
		}
		super.deleteNcObject(result, ncObject);
	}

	@Override
	public Collection<String> getAdditionalValuesForUnreferencedForeignCheck(String unreferencedForeignEntityField, NucletContentMap importContentMap) {
		Collection<String> result = new ArrayList<String>();
		if ("entity".equals(unreferencedForeignEntityField)) {
			for (EntityObjectVO dynEntity : importContentMap.getValues(NuclosEntity.DYNAMICENTITY)) {
				result.add(MasterDataMetaVO.DYNAMIC_ENTITY_PREFIX + dynEntity.getField("name", String.class).toLowerCase());
			}
		}
		return result;
	}

	private DalCallResult removeReferencingEOs(NuclosEntity entity, String referencingField, Long id) {
		List<Long> refIds = getReferencingEOids(entity, referencingField, id);
		if (NuclosEntity.GENERALSEARCHDOCUMENT.equals(entity))
			for (Long refId : refIds)
				try {
					MasterDataFacadeHelper.remove(LangUtils.convertId(refId), null, NuclosSystemParameters.getDirectory(NuclosSystemParameters.DOCUMENT_PATH));
				} catch (Exception ex) {
					error(ex);
				}
		return NucletDalProvider.getInstance().getEntityObjectProcessor(entity).batchDelete(refIds, true);
	}
	
	private List<Long> getReferencingEOids(NuclosEntity entity, String referencingField, Long id) {
		return NucletDalProvider.getInstance().getEntityObjectProcessor(entity).getIdsBySearchExpression(
			new CollectableSearchExpression(SearchConditionUtils.newEOidComparison(
				entity.getEntityName(), referencingField, ComparisonOperator.EQUAL, id, MetaDataServerProvider.getInstance())));
	}
	
}
