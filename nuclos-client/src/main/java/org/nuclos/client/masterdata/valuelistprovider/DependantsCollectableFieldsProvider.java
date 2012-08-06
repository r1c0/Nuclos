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

import org.apache.log4j.Logger;
import org.nuclos.client.masterdata.MasterDataCache;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Value list provider to get dependant data of an entity.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:martin.weber@novabit.de">Martin Weber</a>
 * @version 00.01.000
 */
public class DependantsCollectableFieldsProvider implements CollectableFieldsProvider {

	private static final Logger log = Logger.getLogger(FieldOrAttributeCollectableFieldsProvider.class);
	
	private Integer iRelatedEntityId = null;
	private String sForeignKeyFieldName = null;
	private String sEntity = null;
	private String sEntityField = null;
	
	@Override
	public void setParameter(String sName, Object oValue) {
		if ("entity".equals(sName)) {
			this.sEntity = (String)oValue;
		}
		else if ("foreignKeyFieldName".equals(sName)) {
			this.sForeignKeyFieldName = (String)oValue;
		}
		else if("relatedEntityId".equals(sName)) {
			this.iRelatedEntityId = (Integer)oValue;
		}
		else if ("entityField".equals(sName)) {
			this.sEntityField = (String)oValue;
		}
	}

	@Override
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		log.debug("getCollectableFields");
		
		final List<CollectableField> result = new ArrayList<CollectableField>();
		
		if (StringUtils.isNullOrEmpty(this.sEntity)) {
			throw new CommonBusinessException(SpringLocaleDelegate.getInstance().getMessage(
					"DependantsCollectableFieldsProvider.1", 
					"Der Parameter 'entity' des Valuelistproviders 'dependants' ist nicht gef\u00fcllt!"));
		}
		if (StringUtils.isNullOrEmpty(this.sForeignKeyFieldName)) {
			throw new CommonBusinessException(SpringLocaleDelegate.getInstance().getMessage(
					"DependantsCollectableFieldsProvider.2",
					"Der Parameter 'foreignKeyFieldName' des Valuelistproviders 'dependants' ist nicht gef\u00fcllt!"));
		}
		if (StringUtils.isNullOrEmpty(this.sEntityField)) {
			throw new CommonBusinessException(SpringLocaleDelegate.getInstance().getMessage(
					"DependantsCollectableFieldsProvider.3",
					"Der Parameter 'entityField' des Valuelistproviders 'dependants' ist nicht gef\u00fcllt!"));
		}
		
		if (this.iRelatedEntityId == null) {
			for (MasterDataVO mdVO : MasterDataCache.getInstance().get(this.sEntity)) {
				result.add(new CollectableValueIdField(mdVO.getId(), mdVO.getField(this.sEntityField)));
			}
		}
		else {
			for (EntityObjectVO mdVO : MasterDataDelegate.getInstance().getDependantMasterData(this.sEntity, this.sForeignKeyFieldName, iRelatedEntityId)) {
				result.add(new CollectableValueIdField(mdVO.getId(), mdVO.getField(this.sEntityField, Object.class)));
			}
		}
		
		Collections.sort(result);

		return result;
	}
}
