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
package org.nuclos.client.layout;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.nuclos.client.common.ClientParameterProvider;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.masterdata.CollectableMasterData;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.dal.DalSupportForMD;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeRemote;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class DefaultLayoutMLFactory extends AbstractLayoutMLFactory {
	
	private SpringLocaleDelegate localeDelegate;
	
	private MasterDataFacadeRemote masterDataFacadeRemote;
	
	private final Map<Long, String> attributeGroups;
	
	public DefaultLayoutMLFactory(Map<Long, String> attributeGroups) {
		this.attributeGroups = attributeGroups;
	}
	
	@Autowired
	void setSpringLocaleDelegate(SpringLocaleDelegate cld) {
		this.localeDelegate = cld;
	}
	
	@Autowired
	void setMasterDataFacadeRemote(MasterDataFacadeRemote masterDataFacadeRemote) {
		this.masterDataFacadeRemote = masterDataFacadeRemote;
	}

	@Override
	public String getResourceText(String resourceId) {
		return localeDelegate.getResource(resourceId, "");
	}

	@Override
	public Collection<EntityMetaDataVO> getEntityMetaData() {
		return MetaDataClientProvider.getInstance().getAllEntities();
	}

	@Override
	public Collection<EntityFieldMetaDataVO> getEntityFieldMetaData(String entity) {
		return MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(entity).values();
	}

	@Override
	public Map<Long, String> getAttributeGroups() {
		return attributeGroups;
	}
	
	public void createLayout(String entity, List<EntityFieldMetaDataVO> fields, boolean groupAttributes, boolean withSubforms, boolean withEditFields) throws CommonBusinessException {
		final String layoutml = generateLayout(entity, fields, groupAttributes, withSubforms, withEditFields);
		
		final String sLayoutType;
		final String sLayoutUsageType;
		sLayoutType = NuclosEntity.LAYOUT.getEntityName();
		sLayoutUsageType = NuclosEntity.LAYOUTUSAGE.getEntityName();
		
		final MasterDataMetaVO masterVO = MasterDataDelegate.getInstance().getMetaData(NuclosEntity.ENTITY.getEntityName());
		final CollectableMasterDataEntity masterDataEntity = new CollectableMasterDataEntity(masterVO);

		MasterDataMetaVO metaLayout = MasterDataDelegate.getInstance().getMetaData(sLayoutType);
		CollectableMasterDataEntity masterDataLayout = new CollectableMasterDataEntity(metaLayout);
		CollectableMasterData masterDataLayouml = new CollectableMasterData(masterDataEntity, new MasterDataVO(masterDataLayout.getMasterDataMetaCVO(), false));
		MasterDataVO mdLayout = masterDataLayouml.getMasterDataCVO();
		mdLayout.setField("name", entity);
		mdLayout.setField("description", entity);
		mdLayout.setField("layoutML", layoutml);

		MasterDataMetaVO metaLayoutUsage = MasterDataDelegate.getInstance().getMetaData(sLayoutUsageType);
		CollectableMasterDataEntity masterDataLayoutUsage = new CollectableMasterDataEntity(metaLayoutUsage);
		CollectableMasterData masterDataLayoumlUsage = new CollectableMasterData(masterDataEntity, new MasterDataVO(masterDataLayoutUsage.getMasterDataMetaCVO(), false));
		MasterDataVO mdLayoutUsage = masterDataLayoumlUsage.getMasterDataCVO();
		DependantMasterDataMap dependMapLayoutUsage = new DependantMasterDataMap();

		mdLayoutUsage.setField("entity", entity);

		mdLayoutUsage.setField("searchScreen", Boolean.FALSE);
		mdLayoutUsage.setField("layout", entity);
		dependMapLayoutUsage.addData(sLayoutUsageType, 
				DalSupportForMD.getEntityObjectVO(sLayoutUsageType, mdLayoutUsage));

		metaLayoutUsage = MasterDataDelegate.getInstance().getMetaData(sLayoutUsageType);
		masterDataLayoutUsage = new CollectableMasterDataEntity(metaLayoutUsage);
		masterDataLayoumlUsage = new CollectableMasterData(masterDataEntity, 
				new MasterDataVO(masterDataLayoutUsage.getMasterDataMetaCVO(), false));
		mdLayoutUsage = masterDataLayoumlUsage.getMasterDataCVO();

		mdLayoutUsage.setField("entity", entity);

		mdLayoutUsage.setField("searchScreen", Boolean.TRUE);
		mdLayoutUsage.setField("layout", entity);
		dependMapLayoutUsage.addData(sLayoutUsageType, DalSupportForMD.getEntityObjectVO(sLayoutUsageType, mdLayoutUsage));
		
		String sCompareField = "entity";

		CollectableComparison compare = SearchConditionUtils.newMDComparison(metaLayoutUsage, sCompareField, ComparisonOperator.EQUAL, entity);
		Collection<MasterDataVO> colLayout = masterDataFacadeRemote.getMasterData(sLayoutUsageType, compare, true);

		if(colLayout.size() > 0) {
			MasterDataVO voLayoutUsage = colLayout.iterator().next();
			MasterDataVO voLayout = MasterDataDelegate.getInstance().get(sLayoutType, voLayoutUsage.getField("layoutId"));
			voLayout.setField("layoutML", layoutml);
			MasterDataDelegate.getInstance().update(sLayoutType, voLayout, voLayout.getDependants(), ClientParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
		}
		else {
			MasterDataDelegate.getInstance().create(sLayoutType, mdLayout, dependMapLayoutUsage, ClientParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
		}
	}

}
