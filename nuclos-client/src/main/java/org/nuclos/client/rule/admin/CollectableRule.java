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
package org.nuclos.client.rule.admin;

import org.nuclos.common.collect.collectable.AbstractCollectableBean;
import org.nuclos.common.collect.collectable.AbstractCollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityField;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.LangUtils;
import org.nuclos.common.NuclosEntity;
import org.nuclos.server.ruleengine.valueobject.RuleVO;

/**
 * Makes a RuleVO look like a Collectable.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class CollectableRule extends AbstractCollectableBean<RuleVO> {

	public static final String FIELDNAME_NAME = "name";
	public static final String FIELDNAME_DESCRIPTION = "description";
	public static final String FIELDNAME_RULESOURCE = "ruleSource";
	public static final String FIELDNAME_ACTIVE = "active";
	public static final String FIELDNAME_DEBUG = "debug";

	/**
	 * inner class <code>CollectableRule.Entity</code>.
	 * Contains meta information about <code>CollectableRule</code>.
	 */
	public static class Entity extends AbstractCollectableEntity {

		private Entity() {
			super(NuclosEntity.RULE.getEntityName(), "Regel");

			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_NAME, String.class, "Name",
					"Name der Regel", null, null, false, CollectableField.TYPE_VALUEFIELD, null, null));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_DESCRIPTION, String.class,
					"Beschreibung", "Beschreibung der Regel", null, null, true, CollectableField.TYPE_VALUEFIELD, null, null));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_RULESOURCE, String.class, "Code",
					"Quellcode der Regel", null, null, false, CollectableField.TYPE_VALUEFIELD, null, null));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_ACTIVE, Boolean.class,
					"Aktiv?", "Aktivkennzeichen der Regel", null, null, false, CollectableField.TYPE_VALUEFIELD, null, null));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_DEBUG, Boolean.class,
					"Debug?", "Debug-Flag der Regel", null, null, false, CollectableField.TYPE_VALUEFIELD, null, null));
		}
	}	// inner class Entity

	public static final CollectableEntity clcte = new Entity();

	public CollectableRule(RuleVO rulevo) {
		super(rulevo);
	}

	public RuleVO getRuleVO() {
		return this.getBean();
	}

	@Override
	public Object getId() {
		return this.getRuleVO().getId();
	}

	@Override
	protected CollectableEntity getCollectableEntity() {
		return clcte;
	}

	@Override
	public String getIdentifierLabel() {
		return getRuleVO().getName();
	}

	@Override
	public int getVersion() {
		return getRuleVO().getVersion();
	}

	@Override
	public CollectableField getField(String sFieldName) {
		final CollectableField result;

		if (sFieldName.equals(FIELDNAME_ACTIVE)) {
			result = new CollectableValueField(this.getRuleVO().isActive());
		}
		else if (sFieldName.equals(FIELDNAME_DEBUG)) {
			result = new CollectableValueField(this.getRuleVO().isDebug());
		}
		else {
			result = super.getField(sFieldName);
		}
		return result;
	}

	@Override
	public void setField(String sFieldName, CollectableField clctfValue) {
		if (sFieldName.equals(FIELDNAME_ACTIVE)) {
			this.getRuleVO().setActive(LangUtils.defaultIfNull((Boolean) clctfValue.getValue(), false));
		}
		else if (sFieldName.equals(FIELDNAME_DEBUG)) {
			this.getRuleVO().setDebug(LangUtils.defaultIfNull((Boolean) clctfValue.getValue(), false));
		}
		else {
			super.setField(sFieldName, clctfValue);
		}
	}

	public static class MakeCollectable implements Transformer<RuleVO, CollectableRule> {
		@Override
		public CollectableRule transform(RuleVO rulevo) {
			return new CollectableRule(rulevo);
		}
	}

}	// class CollectableRule