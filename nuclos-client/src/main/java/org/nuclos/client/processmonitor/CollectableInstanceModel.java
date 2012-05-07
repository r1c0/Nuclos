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
/**
 * 
 */
package org.nuclos.client.processmonitor;

import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collect.collectable.AbstractCollectableBean;
import org.nuclos.common.collect.collectable.AbstractCollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityField;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.masterdata.CollectableMasterDataForeignKeyEntityField;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.processmonitor.valueobject.InstanceVO;
import org.nuclos.server.processmonitor.valueobject.ProcessMonitorGraphVO;
import org.nuclos.server.processmonitor.valueobject.ProcessMonitorVO;
import org.nuclos.common2.DateTime;

/**
 * @author Marc.Finke
 * class represents the collectable bean for processmonitorcollectcontroller
 * like CollectableStateModel
 */
public class CollectableInstanceModel extends AbstractCollectableBean<InstanceVO> {
	
	public static final String FIELDNAME_NAME = "name";
	public static final String FIELDNAME_PROCESSMODEL = "processmonitor";
	public static final String FIELDNAME_PLANSTART = "planstart";
	public static final String FIELDNAME_PLANEND = "planend";
	public static final String FIELDNAME_REALSTART = "realstart";
	public static final String FIELDNAME_REALEND = "realend";
//	public static final String FIELDNAME_DESCRIPTION = "description";
		
	public static class Entity extends AbstractCollectableEntity {

		private static MasterDataMetaVO mdMetaVO = MasterDataDelegate.getInstance().getMetaData(NuclosEntity.INSTANCE);
		
		private Entity() {
			super(mdMetaVO.getEntityName(), mdMetaVO.getLabel());
			final String entity = mdMetaVO.getEntityName();
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_NAME, String.class, mdMetaVO.getField(FIELDNAME_NAME).getLabel(),
					mdMetaVO.getField(FIELDNAME_NAME).getDescription(), null, null, false, CollectableField.TYPE_VALUEFIELD, null, null, entity, null));
			this.addCollectableEntityField(new CollectableMasterDataForeignKeyEntityField(mdMetaVO.getField(FIELDNAME_PROCESSMODEL), entity));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_PLANSTART, DateTime.class, mdMetaVO.getField(FIELDNAME_PLANSTART).getLabel(),
					mdMetaVO.getField(FIELDNAME_PLANSTART).getDescription(), null, null, false, CollectableField.TYPE_VALUEFIELD, null, null, entity, null));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_PLANEND, DateTime.class, mdMetaVO.getField(FIELDNAME_PLANEND).getLabel(),
					mdMetaVO.getField(FIELDNAME_PLANEND).getDescription(), null, null, false, CollectableField.TYPE_VALUEFIELD, null, null, entity, null));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_REALSTART, DateTime.class, mdMetaVO.getField(FIELDNAME_REALSTART).getLabel(),
					mdMetaVO.getField(FIELDNAME_REALSTART).getDescription(), null, null, false, CollectableField.TYPE_VALUEFIELD, null, null, entity, null));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_REALEND, DateTime.class, mdMetaVO.getField(FIELDNAME_REALEND).getLabel(),
					mdMetaVO.getField(FIELDNAME_REALEND).getDescription(), null, null, false, CollectableField.TYPE_VALUEFIELD, null, null, entity, null));
		}

	}	// inner class Entity

	public static final CollectableEntity clcte = new Entity();
	
	public CollectableInstanceModel(MasterDataVO mdVO) {
		this(new InstanceVO(mdVO));
	}
	
	public CollectableInstanceModel(InstanceVO instanceVO) {
		super(instanceVO);
	}
	
	public ProcessMonitorVO getProcessMonitorModel() {
		return null;
	}
	
	public ProcessMonitorGraphVO getProcessMonitorGraphVO() {
		if (super.getBean().getProcessmonitorId() != null){
			try {
				return ProcessMonitorDelegate.getInstance().getStateGraph(super.getBean().getProcessmonitorId());
			} catch (CommonFinderException e) {
				return null;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.nuclos.common.collect.collectable.AbstractCollectableBean#getCollectableEntity()
	 */
	@Override
	public CollectableEntity getCollectableEntity() {
		// TODO Auto-generated method stub
		return clcte;
	}

	/* (non-Javadoc)
	 * @see org.nuclos.common.collect.collectable.Collectable#getId()
	 */
	@Override
	public Object getId() {
		// TODO Auto-generated method stub
		return super.getBean().getId();
	}

	/* (non-Javadoc)
	 * @see org.nuclos.common.collect.collectable.Collectable#getIdentifierLabel()
	 */
	@Override
	public String getIdentifierLabel() {
		// TODO Auto-generated method stub
		return "Prozessmonitor";
	}

	/* (non-Javadoc)
	 * @see org.nuclos.common.collect.collectable.Collectable#getVersion()
	 */
	@Override
	public int getVersion() {
		// TODO Auto-generated method stub
		return super.getBean().getVersion();
	}

	public MasterDataVO getMasterDataVO() {
		return getBean().getMasterDataVO();
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("entity=").append(getCollectableEntity());
		result.append(",vo=").append(getBean());
		result.append(",mdVo=").append(getMasterDataVO());
		result.append(",pmVo=").append(getProcessMonitorModel());
		result.append(",pmGraphVo=").append(getProcessMonitorGraphVO());
		result.append(",id=").append(getId());
		result.append(",label=").append(getIdentifierLabel());
		result.append(",complete=").append(isComplete());
		result.append("]");
		return result.toString();
	}
	
	public static class MakeCollectable implements Transformer<MasterDataVO, CollectableInstanceModel> {
		@Override
		public CollectableInstanceModel transform(MasterDataVO mdVO) {
			return new CollectableInstanceModel(mdVO);
		}
	}
	
}
