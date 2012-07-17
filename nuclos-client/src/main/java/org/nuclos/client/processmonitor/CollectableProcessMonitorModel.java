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

import org.nuclos.common.collect.collectable.AbstractCollectableBean;
import org.nuclos.common.collect.collectable.AbstractCollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityField;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.NuclosEntity;
import org.nuclos.server.processmonitor.valueobject.ProcessMonitorGraphVO;
import org.nuclos.server.processmonitor.valueobject.ProcessMonitorVO;

/**
 * @author Marc.Finke
 * class represents the collectable bean for processmonitorcollectcontroller
 * like CollectableStateModel
 */
public class CollectableProcessMonitorModel extends AbstractCollectableBean<ProcessMonitorVO> {
	
	public static final String FIELDNAME_NAME = "name";
	public static final String FIELDNAME_DESCRIPTION = "description";
	private final ProcessMonitorVO monitorvo;
	private final ProcessMonitorGraphVO graphvo;
	private final boolean bComplete;
	
	
	public static class Entity extends AbstractCollectableEntity {

		private Entity() {
			super(NuclosEntity.PROCESSMONITOR.getEntityName(), "Prozessmonitor");
			final String entity = NuclosEntity.PROCESSMONITOR.getEntityName();
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_NAME, String.class, "Name",
					"Name des Prozessmodells", 255, null, false, CollectableField.TYPE_VALUEFIELD, null, null, entity, null));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_DESCRIPTION, String.class,
					"Beschreibung", "Beschreibung des Prozessmodells", 4000, null, true, CollectableField.TYPE_VALUEFIELD, null, null, entity, null));
		}

	}	// inner class Entity

	public static final CollectableEntity clcte = new Entity();
	
	public CollectableProcessMonitorModel(ProcessMonitorVO vo) {
		this(new ProcessMonitorGraphVO(vo));
	}

	public CollectableProcessMonitorModel(ProcessMonitorGraphVO vo) {
		super(vo.getStateModel());		
		this.graphvo = vo;
		this.monitorvo = vo.getStateModel();		
		this.bComplete = true;
		// TODO Auto-generated constructor stub
	}
	
	public ProcessMonitorVO getProcessMonitorModel() {
		return monitorvo;
	}
	
	public ProcessMonitorGraphVO getProcessMonitorGraphVO() {
		return graphvo;
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
		return this.getProcessMonitorModel().getId();
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
		return 0;
	}
	
	@Override
	public boolean isComplete() {
		return this.bComplete;
	}
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("entity=").append(getCollectableEntity());
		result.append(",vo=").append(getBean());
		result.append(",pmVo=").append(getProcessMonitorModel());
		result.append(",pmGraphVo=").append(getProcessMonitorGraphVO());
		result.append(",id=").append(getId());
		result.append(",label=").append(getIdentifierLabel());
		result.append(",complete=").append(isComplete());
		result.append("]");
		return result.toString();
	}
	
	public static class MakeCollectable implements Transformer<ProcessMonitorVO, CollectableProcessMonitorModel> {
		@Override
		public CollectableProcessMonitorModel transform(ProcessMonitorVO statemodelvo) {
			return new CollectableProcessMonitorModel(statemodelvo);
		}
	}

}
