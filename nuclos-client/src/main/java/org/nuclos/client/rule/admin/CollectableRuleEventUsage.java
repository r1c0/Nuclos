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
import org.nuclos.common.collect.collectable.DefaultCollectableEntityField;
import org.nuclos.server.ruleengine.valueobject.RuleEventUsageVO;

/**
 * <code>Collectable</code> RuleEventUsage.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class CollectableRuleEventUsage extends AbstractCollectableBean<RuleEventUsageVO> {

	public static final String FIELDNAME_MODULE = "module";
	public static final String FIELDNAME_EVENT = "event";
//	public static final String FIELDNAME_RULE = "rule";
	public static final String FIELDNAME_ORDER = "order";

	/**
	 * inner class <code>LayoutUsageCollectableAdapter.Entity</code>.
	 * Contains meta information about <code>LayoutUsageCollectableAdapter</code>.
	 */
	private static class Entity extends AbstractCollectableEntity {

		private Entity() {
			super("eventusage", "Ereignis-Verwendung");

			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_MODULE, String.class, "Entität", "Entität",
					null, null, false, CollectableField.TYPE_VALUEIDFIELD, null, null));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_EVENT, String.class, "Ereignis",
					"Ereignis", null, null, false, CollectableField.TYPE_VALUEIDFIELD, null, null));
//			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_RULE, String.class, "Regel", "Regel", null, true, false, CollectableField.TYPE_VALUEIDFIELD, true));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_ORDER, Integer.class, "Reihenfolge",
					"Reihenfolge", null, null, false, CollectableField.TYPE_VALUEFIELD, null, null));
		}
	}	// inner class Entity

	public static final CollectableEntity clcte = new Entity();

	public CollectableRuleEventUsage(RuleEventUsageVO ruleeventusagevo) {
		super(ruleeventusagevo);
	}

	public RuleEventUsageVO getRuleEventUsageVO() {
		return this.getBean();
	}

	@Override
	public Object getId() {
		return this.getRuleEventUsageVO().getId();
	}

	@Override
	protected CollectableEntity getCollectableEntity() {
		return clcte;
	}

	@Override
	public String getIdentifierLabel() {
		return getRuleEventUsageVO().getEvent();
	}

	@Override
	public int getVersion() {
		return this.getRuleEventUsageVO().getVersion();
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("entity=").append(getCollectableEntity());
		result.append(",vo=").append(getBean());
		result.append(",ruleEventUsageVo=").append(getRuleEventUsageVO());
		result.append(",id=").append(getId());
		result.append(",label=").append(getIdentifierLabel());
		result.append(",complete=").append(isComplete());
		result.append("]");
		return result.toString();
	}
	
}	// class CollectableRuleEventUsage
