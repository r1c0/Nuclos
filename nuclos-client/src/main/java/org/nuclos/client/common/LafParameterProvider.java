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
package org.nuclos.client.common;

import java.util.Collection;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.main.mainframe.workspace.WorkspaceChooserController;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.masterdata.MetaDataDelegate;
import org.nuclos.client.ui.Errors;
import org.nuclos.common.Actions;
import org.nuclos.common.LafParameter;
import org.nuclos.common.LafParameterMap;
import org.nuclos.common.LafParameterStorage;
import org.nuclos.common.MasterDataMetaProvider;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.dal.DalSupportForMD;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

//@Component("lafParameterProvider")
public class LafParameterProvider {
	
	private static final Logger LOG = Logger.getLogger(LafParameterProvider.class);

	private static LafParameterProvider INSTANCE;
	
	private MetaDataClientProvider metaDataProvider;
	
	private ParameterProvider parameterProvider;
	
	public static LafParameterProvider getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new LafParameterProvider();
		}
		return INSTANCE;
	}
	
	public boolean isStorageAllowed(LafParameter<?> parameter, LafParameterStorage storage) {
		switch (storage) {
		case SYSTEMPARAMETER:
		case ENTITY:
			if (SecurityCache.getInstance().isSuperUser()) {
				return parameter.isStoragePossible(storage);
			} else {
				return false;
			}
		case WORKSPACE:
			if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_CUSTOMIZE_STARTTAB)) {
				return parameter.isStoragePossible(storage);
			} else {
				return false;
			}
		default:
			return false;
		}
	}
	
	public <T> T getValue(LafParameter<T> parameter) {
		return getValue(parameter, null);
	}
	
	public <T> T getValue(LafParameter<T> parameter, Long entityId) {
		T result = null;
		
		// 1. Entity
		if (result == null && entityId != null) {
			LafParameterMap entityParameters = getMetaDataProvider().getLafParameters(entityId);
			if (entityParameters != null) {
				result = entityParameters.getValue(parameter);
			}
		}
		
		// 2. Workspace
		if (result == null) {
			result = parameter.parse(WorkspaceChooserController.getInstance().getSelectedWorkspace().getWoDesc().getParameter(parameter.getName()));
		}
		
		// 3. Systemparameter
		if (result == null) {
			result = parameter.parse(getParameterProvider().getValue(parameter.getName()));
		}
		
		// 4. Nuclos default
		if (result == null) {
			result = parameter.getDefault();
		}
		
		assert result != null;
		return result;
	}
	
	public <T> T getValue(LafParameter<T> parameter, Long entityId, LafParameterStorage storage) {
		switch (storage) {
		case WORKSPACE:
			return parameter.parse(WorkspaceChooserController.getInstance().getSelectedWorkspace().getWoDesc().getParameter(parameter.getName()));
		case ENTITY:
			if (entityId == null) {
				throw new IllegalArgumentException("entityId must not be null");
			}
			LafParameterMap entityParameters = getMetaDataProvider().getLafParameters(entityId);
			if (entityParameters != null) {
				return entityParameters.getValue(parameter);
			} else {
				return null;
			}
		case SYSTEMPARAMETER:
			return parameter.parse(getParameterProvider().getValue(parameter.getName()));
		default:
			return null;
		}
	}
	
	public <T> void setValue(LafParameter<T> parameter, Long entityId, LafParameterStorage storage, String value) {
		if (!parameter.isStoragePossible(storage)) {
			throw new IllegalArgumentException(String.format("Storage %s for parameter %s is not possible", parameter, storage));
		}
		
		try {
			switch (storage) {
			case WORKSPACE:
				WorkspaceChooserController.getInstance().getSelectedWorkspace().getWoDesc().setParameter(parameter.getName(), value);
				break;
			case ENTITY:
				if (entityId == null) {
					throw new IllegalArgumentException("entityId must not be null");
				}
				String entity = NuclosEntity.ENTITYLAFPARAMETER.getEntityName();
				Collection<MasterDataVO> colParamter = MasterDataDelegate.getInstance().getMasterData(entity, 
						SearchConditionUtils.and(
								SearchConditionUtils.newEOComparison(
										entity, "parameter", ComparisonOperator.EQUAL, parameter.getName(), MetaDataClientProvider.getInstance()),
								SearchConditionUtils.newEOidComparison(
										entity, "entity", ComparisonOperator.EQUAL, entityId, MetaDataClientProvider.getInstance())
						));
				switch (colParamter.size()) {
				case 1:
					MasterDataVO mdParameter = colParamter.iterator().next();
					if (value == null) {
						MasterDataDelegate.getInstance().remove(entity, mdParameter);
					} else {
						mdParameter.setField("value", value);
						MasterDataDelegate.getInstance().update(entity, mdParameter, null);
					}
					break;
				default:
					if (value != null) {
						EntityObjectVO eoParameter = new EntityObjectVO();
						eoParameter.setEntity(entity);
						eoParameter.initFields(2, 1);
						eoParameter.flagNew();
						eoParameter.getFields().put("parameter", parameter.getName());
						eoParameter.getFields().put("value", value);
						eoParameter.getFieldIds().put("entity", entityId);
						mdParameter = DalSupportForMD.getMasterDataWithDependantsVO(eoParameter);
						MasterDataDelegate.getInstance().create(entity, mdParameter, null);
					}
				}
				break;
			case SYSTEMPARAMETER:
				entity = NuclosEntity.PARAMETER.getEntityName();
				colParamter = MasterDataDelegate.getInstance().getMasterData(entity, SearchConditionUtils.newEOComparison(
						entity, "name", ComparisonOperator.EQUAL, parameter.getName(), MetaDataClientProvider.getInstance()));
				switch (colParamter.size()) {
				case 1:
					MasterDataVO mdParameter = colParamter.iterator().next();
					if (value == null) {
						MasterDataDelegate.getInstance().remove(entity, mdParameter);
					} else {
						mdParameter.setField("value", value);
						MasterDataDelegate.getInstance().update(entity, mdParameter, null);
					}
					break;
				default:
					if (value != null) {
						EntityObjectVO eoParameter = new EntityObjectVO();
						eoParameter.setEntity(entity);
						eoParameter.initFields(2, 1);
						eoParameter.flagNew();
						eoParameter.getFields().put("name", parameter.getName());
						eoParameter.getFields().put("description", parameter.getName());
						eoParameter.getFields().put("value", value);
						mdParameter = DalSupportForMD.getMasterDataWithDependantsVO(eoParameter);
						MasterDataDelegate.getInstance().create(entity, mdParameter, null);
					}
				}
				break;
			default:
				throw new NotImplementedException("setLafParameterValue for storage " + storage);
			}
		} catch (Exception ex) {
			LOG.error(ex.getMessage(), ex);
			Errors.getInstance().showExceptionDialog(null, ex);
		}
	}
	
	private MetaDataClientProvider getMetaDataProvider() {
		if (metaDataProvider == null) {
			metaDataProvider = MetaDataClientProvider.getInstance();
		}
		return metaDataProvider;
	}
	
	private ParameterProvider getParameterProvider() {
		if (parameterProvider == null) {
			parameterProvider = ClientParameterProvider.getInstance();
		}
		return parameterProvider;
	}
}
