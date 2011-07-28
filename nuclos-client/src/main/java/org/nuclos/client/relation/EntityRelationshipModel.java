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
package org.nuclos.client.relation;

import java.util.HashMap;
import java.util.Map;

import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collect.collectable.AbstractCollectableBean;
import org.nuclos.common.collect.collectable.AbstractCollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityField;
import org.nuclos.common.valueobject.EntityRelationshipModelVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Makes a StateModelVO look like a Collectable.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class EntityRelationshipModel extends AbstractCollectableBean<EntityRelationshipModelVO> {

	public static final String FIELDNAME_NAME = "name";
	public static final String FIELDNAME_DESCRIPTION = "description";

	private final boolean bComplete;
	
	String xmlModel;
	
	static EntityRelationshipModelVO vo = new EntityRelationshipModelVO("", "");

	public static class Entity extends AbstractCollectableEntity {

		private Entity() {
			super(NuclosEntity.ENTITYRELATION.getEntityName(), "Relationen erstellen");

			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_NAME, String.class, CommonLocaleDelegate.getMessage("CollectableStateModel.3","Name"),
				CommonLocaleDelegate.getMessage("CollectableRelationModel.4","Name des Relationenmodells"), null, null, false, CollectableField.TYPE_VALUEFIELD, null, null));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_DESCRIPTION, String.class,
				CommonLocaleDelegate.getMessage("CollectableStateModel.1","Beschreibung"), CommonLocaleDelegate.getMessage("CollectableRelationModel.2","Beschreibung des Relationenmodells"), null, null, true, CollectableField.TYPE_VALUEFIELD, null, null));
		}

	}	// inner class Entity

	public static final CollectableEntity clcte = new Entity();

	public EntityRelationshipModel() {		
		this(vo, true);
	}
	
	public EntityRelationshipModel(EntityRelationshipModelVO voMaster) {		
		this(voMaster, true);		
	}

	private EntityRelationshipModel(EntityRelationshipModelVO vo, boolean bComplete) {
		super(vo);
		this.bComplete = bComplete;
	}

	@Override
	public boolean isComplete() {
		return this.bComplete;
	}


	@Override
	public Object getId() {
		return this.getBean().getId();
	}

	@Override
	protected CollectableEntity getCollectableEntity() {
		return clcte;
	}

	@Override
	public String getIdentifierLabel() {
		return this.getBean().getName();
	}

	@Override
	public int getVersion() {
		return this.getBean().getVersion();
	}

	@Override
	public Object getValue(String sFieldName) {
		return this.getField(sFieldName).getValue();
	}
	
	public String getXMLModel() {
		return xmlModel;
	}
	
	public void setXMLModel(String xml) {
		this.xmlModel = xml;
	}
	
	public MasterDataVO getMasterDataVO() {	
		Map<String, Object> mpFields = new HashMap<String, Object>();
		mpFields.put("name", this.getValue("name"));
		mpFields.put("description", this.getValue("description"));
		
		MasterDataVO mdvo = new MasterDataVO(this.getId(), this.getBean().getCreatedAt(), this.getBean().getCreatedBy(),
			this.getBean().getChangedAt(), this.getBean().getChangedBy(), this.getVersion(), mpFields);
		
		return mdvo;
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("entity=").append(getCollectableEntity());
		result.append(",vo=").append(getBean());
		result.append(",mdVo=").append(getMasterDataVO());
		result.append(",id=").append(getId());
		result.append(",label=").append(getIdentifierLabel());
		result.append(",complete=").append(isComplete());
		result.append("]");
		return result.toString();
	}	

}	// class CollectableStateModel
