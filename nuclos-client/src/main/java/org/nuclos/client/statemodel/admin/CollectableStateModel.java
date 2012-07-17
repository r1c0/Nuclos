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
package org.nuclos.client.statemodel.admin;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.PropertyUtils;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.collectable.AbstractCollectableBean;
import org.nuclos.common.collect.collectable.AbstractCollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityField;
import org.nuclos.common.collection.Transformer;
import org.nuclos.server.statemodel.valueobject.StateGraphVO;
import org.nuclos.server.statemodel.valueobject.StateModelVO;

/**
 * Makes a StateModelVO look like a Collectable.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class CollectableStateModel extends AbstractCollectableBean<StateModelVO> {

	public static final String FIELDNAME_NAME = "name";
	public static final String FIELDNAME_DESCRIPTION = "description";
	private final StateGraphVO stategraphvo;
	private final boolean bComplete;

	public static class Entity extends AbstractCollectableEntity {

		private Entity() {
			super(NuclosEntity.STATEMODEL.getEntityName(), "Statusmodell");
			final String entity = NuclosEntity.STATEMODEL.getEntityName();
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_NAME, String.class, 
					getSpringLocaleDelegate().getMessage("CollectableStateModel.3","Name"),
					getSpringLocaleDelegate().getMessage("CollectableStateModel.4","Name des Statusmodells"), 
					255, null, false, CollectableField.TYPE_VALUEFIELD, null, null, entity, null));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_DESCRIPTION, String.class,
					getSpringLocaleDelegate().getMessage("CollectableStateModel.1","Beschreibung"), 
					getSpringLocaleDelegate().getMessage("CollectableStateModel.2","Beschreibung des Statusmodells"), 
					4000, null, true, CollectableField.TYPE_VALUEFIELD, null, null, entity, null));
		}

	}	// inner class Entity

	public static final CollectableEntity clcte = new Entity();

	/**
	 * @param statemodelvo
	 * @postcondition !this.isComplete()
	 */
	public CollectableStateModel(StateModelVO statemodelvo) {
		this(new StateGraphVO(statemodelvo), false);
		assert !this.isComplete();
	}

	/**
	 * @param stategraphvo
	 * @postcondition this.isComplete()
	 */
	public CollectableStateModel(StateGraphVO stategraphvo) {
		this(stategraphvo, true);
		assert this.isComplete();
	}

	private CollectableStateModel(StateGraphVO stategraphvo, boolean bComplete) {
		super(stategraphvo.getStateModel());
		this.stategraphvo = stategraphvo;
		this.bComplete = bComplete;
	}

	@Override
	public boolean isComplete() {
		return this.bComplete;
	}

	public StateGraphVO getStateGraphVO() {
		return this.stategraphvo;
	}

	public StateModelVO getStateModelVO() {
		return this.getBean();
	}

	@Override
	public Object getId() {
		return this.getStateModelVO().getId();
	}

	@Override
	protected CollectableEntity getCollectableEntity() {
		return clcte;
	}

	@Override
	public String getIdentifierLabel() {
		return this.getStateModelVO().getName();
	}

	@Override
	public int getVersion() {
		return this.getStateModelVO().getVersion();
	}

	@Override
	public Object getValue(String sFieldName) {
		try {
			return PropertyUtils.getSimpleProperty(this.getStateModelVO(), sFieldName);
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
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("entity=").append(getCollectableEntity());
		result.append(",vo=").append(getBean());
		result.append(",stateVo=").append(getStateModelVO());
		result.append(",stateGraphVo=").append(getStateGraphVO());
		result.append(",id=").append(getId());
		result.append(",label=").append(getIdentifierLabel());
		result.append(",complete=").append(isComplete());
		result.append("]");
		return result.toString();
	}
	
	public static class MakeCollectable implements Transformer<StateModelVO, CollectableStateModel> {
		@Override
		public CollectableStateModel transform(StateModelVO statemodelvo) {
			return new CollectableStateModel(statemodelvo);
		}
	}

}	// class CollectableStateModel
