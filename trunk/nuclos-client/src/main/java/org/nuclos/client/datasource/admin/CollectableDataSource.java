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
package org.nuclos.client.datasource.admin;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.PropertyUtils;
import org.nuclos.client.masterdata.MetaDataCache;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.collectable.AbstractCollectableBean;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.server.report.valueobject.DatasourceVO;
import org.nuclos.server.report.valueobject.DynamicEntityVO;
import org.nuclos.server.report.valueobject.RecordGrantVO;
import org.nuclos.server.report.valueobject.ValuelistProviderVO;

/**
 * <code>Collectable</code> datasource.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 * todo: can this class be replaced with the masterdata mechanism?
 */
public class CollectableDataSource extends AbstractCollectableBean<DatasourceVO> {

	public static final String FIELDNAME_NAME = "name";
	public static final String FIELDNAME_DESCRIPTION = "description";
//	public static final String FIELDNAME_VALID = "valid";
	public static final String FIELDNAME_DATASOURCEXML = "datasourceXML";
	public static final String FIELDNAME_ENTITY = "entity";
//	public static final String FIELDNAME_CREATEDBY = "createdBy";
//	public static final String FIELDNAME_CREATEDAT = "createdAt";
//	public static final String FIELDNAME_CHANGEDBY = "changedBy";
//	public static final String FIELDNAME_CHANGEDAT = "changedAt";

	public final CollectableEntity clcte;

	public CollectableDataSource(DatasourceVO datasourcevo) {
		super(datasourcevo);
		if (datasourcevo instanceof DynamicEntityVO) {
			clcte = new CollectableMasterDataEntity(MetaDataCache.getInstance().getMetaData(NuclosEntity.DYNAMICENTITY));
		} else if (datasourcevo instanceof ValuelistProviderVO) {
			clcte = new CollectableMasterDataEntity(MetaDataCache.getInstance().getMetaData(NuclosEntity.VALUELISTPROVIDER));
		} else if (datasourcevo instanceof RecordGrantVO) {
			clcte = new CollectableMasterDataEntity(MetaDataCache.getInstance().getMetaData(NuclosEntity.RECORDGRANT));
		} else {
			clcte = new CollectableMasterDataEntity(MetaDataCache.getInstance().getMetaData(NuclosEntity.DATASOURCE));
		}
	}

	public DatasourceVO getDatasourceVO() {
		return this.getBean();
	}

	@Override
	public Integer getId() {
		return this.getDatasourceVO().getId();
	}

	@Override
	protected CollectableEntity getCollectableEntity() {
		return clcte;
	}

	@Override
	public Object getValue(String sFieldName) {
		try {
			return PropertyUtils.getProperty(this.getDatasourceVO(), sFieldName);
		}
		catch (IllegalAccessException ex) {
			throw new NuclosFatalException(ex);
		}
		catch (InvocationTargetException ex) {
			throw new NuclosFatalException(ex);
		}
		catch (NoSuchMethodException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	@Override
	public String getIdentifierLabel() {
		return getDatasourceVO().getName();
	}

	@Override
	public int getVersion() {
		return getDatasourceVO().getVersion();
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("entity=").append(getCollectableEntity());
		result.append(",vo=").append(getBean());
		result.append(",id=").append(getId());
		result.append(",label=").append(getIdentifierLabel());
		result.append(",complete=").append(isComplete());
		result.append("]");
		return result.toString();
	}

	public static class MakeCollectable implements Transformer<DatasourceVO, CollectableDataSource> {
		@Override
		public CollectableDataSource transform(DatasourceVO datasourcevo) {
			return new CollectableDataSource(datasourcevo);
		}
	}

}	// class CollectableDataSource
