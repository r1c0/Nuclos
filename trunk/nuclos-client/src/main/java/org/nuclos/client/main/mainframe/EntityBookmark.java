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
package org.nuclos.client.main.mainframe;

import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;

/**
 *
 *
 */
public class EntityBookmark {
	private static final long serialVersionUID = 6637996725938917463L;

	private final String entity;
	private final Integer id;
	private String label;
	public final long timestamp;

	/**
	 *
	 * @param entity
	 * @param id
	 * @param label
	 */
	public EntityBookmark(String entity, Integer id, String label) {
		this(entity, id, label, System.currentTimeMillis());
	}

	/**
	 *
	 * @param entity
	 * @param id
	 * @param label
	 * @param timestamp
	 */
	public EntityBookmark(String entity, Integer id, String label, long timestamp) {
		super();
		this.entity = entity;
		this.id = id;
		this.timestamp = timestamp;
		this.setLabel(label);
	}

	/**
	 *
	 * @return
	 */
	public String getEntity() {
		return entity;
	}

	/**
	 *
	 * @return
	 */
	public Integer getId() {
		return id;
	}

	/**
	 *
	 * @return
	 */
	public String getLabel() {
		return label;
	}

	/**
	 *
	 * @param label
	 */
	public void setLabel(String label) {
		if (StringUtils.looksEmpty(label)) {
			this.label = "<NO LABEL>";
		} else {
			this.label = label;
		}
	}

	/**
	 *
	 * @return
	 */
	public EntityBookmark copy() {
		EntityBookmark result = new EntityBookmark(getEntity(), getId(), getLabel());
		return result;
	}

	/**
	 *
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EntityBookmark) {
			EntityBookmark ebo = (EntityBookmark) obj;
			return LangUtils.equals(this.getEntity(), ebo.getEntity()) && LangUtils.equals(this.getId(), ebo.getId());
		}
		return super.equals(obj);
	}

	@Override
	public String toString() {
		return "EntityBookmark [entity=" + entity + ", id=" + id + ", label=" + label + ", timestamp=" + timestamp + "]";
	}
}
