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

import org.nuclos.common.collect.collectable.AbstractCollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collect.collectable.DefaultCollectable;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityField;
import org.nuclos.common2.SpringLocaleDelegate;

import org.nuclos.common.NuclosEntity;

/**
 * User role for a state (for state dependent user rights).
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class CollectableStateRole extends DefaultCollectable {

	public static final String FIELDNAME_STATE = "state";
	public static final String FIELDNAME_ROLE = "role";

	public CollectableStateRole() {
		super(clcte);
	}

	private static class Entity extends AbstractCollectableEntity {
		private Entity() {
			super("staterole", 
					SpringLocaleDelegate.getInstance().getMessage(
							"CollectableStateRole.3","Rolle (Benutzergruppe) f\u00fcr Status"));
			final String entity = "staterole";
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_STATE, String.class, 
					getSpringLocaleDelegate().getMessage("CollectableStateRole.4","Status"),
					getSpringLocaleDelegate().getMessage("CollectableStateRole.5","\u00dcbergeordneter Status"), null, null, false, CollectableField.TYPE_VALUEIDFIELD, NuclosEntity.STATE.getEntityName(),
					CollectableValueIdField.NULL, null, null, entity, null));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_ROLE, String.class, 
					getSpringLocaleDelegate().getMessage("CollectableStateRole.1","Benutzergruppe"),
					getSpringLocaleDelegate().getMessage("CollectableStateRole.2","Benutzergruppe (Rolle)"), null, null, false, CollectableField.TYPE_VALUEIDFIELD, NuclosEntity.ROLE.getEntityName(),
					CollectableValueIdField.NULL, null, null, entity, null));
		}
	}

	public static final CollectableEntity clcte = new Entity();

}	// class CollectableStateRole
