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
package org.nuclos.common.dbtransfer;

import java.io.Serializable;
import java.util.List;

import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.LangUtils;

public class NucletContentUID implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final Long id;
	public final String uid;
	public final Integer version;

	/**
	 * Constructor for generating new UID
	 * @param EntityObjectVO
	 */
	public NucletContentUID(EntityObjectVO eo) {
		if (eo.getId() == null)
			throw new NuclosFatalException("ID must not be null");
		if (eo.getCreatedAt() == null)
			throw new NuclosFatalException("CreatedAt must not be null");
		this.uid = toString(eo.getId()) + toString(eo.getCreatedAt().getTime()) + toString(System.currentTimeMillis());
		this.version = eo.getVersion();
		this.id = null;
	}

	/**
	 * Constructor for existing UID
	 * @param String uid
	 * @param Integer version
	 * @param Long id
	 */
	public NucletContentUID(String uid, Integer version, Long id) {
		this.uid = uid;
		this.version = version;
		this.id = id;
	}

	/**
	 *
	 * @return new NucletContentUID without id
	 */
	public NucletContentUID copy() {
		return new NucletContentUID(uid, version, null);
	}

	private String toString(Long value) {
		String sValue = LangUtils.defaultIfNull(value, Long.valueOf(0l)).toString();
		return ("00000000000000000000"+sValue).substring(sValue.length());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof NucletContentUID) {
			NucletContentUID otherUid = (NucletContentUID) obj;
			return LangUtils.equals(this.uid, otherUid.uid);
		}
		return this == obj;
	}

	public static class Key extends Object implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public final NuclosEntity entity;
		public final Long id;
		public Key(NuclosEntity entity, Long objectId) {
			super();
			this.entity = entity;
			this.id = objectId;
		}
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Key) {
				Key otherKey = (Key) obj;
				return entity == otherKey.entity && LangUtils.equals(id, otherKey.id);
			}
			return super.equals(obj);
		}
		@Override
		public int hashCode() {
			return LangUtils.hashCode(entity.getEntityName()) ^ LangUtils.hashCode(id);
		}
		@Override
		public String toString() {
			return entity.getEntityName() + ", " + id;
		}
	}

	public static interface Map extends java.util.Map<NucletContentUID.Key, NucletContentUID> {
		public void add(EntityObjectVO uidObject);
		public void addAll(List<EntityObjectVO> uidObjects);
		public boolean containsUID(NucletContentUID uid, NuclosEntity entity);
		public NucletContentUID.Key getKey(EntityObjectVO eo);
		public NucletContentUID getUID(EntityObjectVO eo);
	}

	public static class HashMap extends java.util.HashMap<NucletContentUID.Key, NucletContentUID> implements NucletContentUID.Map {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void add(EntityObjectVO uidObject) {
			NuclosEntity entity = NuclosEntity.getByName(uidObject.getField("nuclosentity", String.class));
			if(entity != null) {
				put(new NucletContentUID.Key(entity, uidObject.getField("objectid",
					Long.class)),
					new NucletContentUID(uidObject.getField("uid", String.class),
						uidObject.getField("objectversion", Integer.class),
						uidObject.getId()));
			}
		}

		@Override
		public void addAll(List<EntityObjectVO> uidObjects) {
			for(EntityObjectVO uidObject : uidObjects) {
				add(uidObject);
			}
		}

		@Override
		public boolean containsUID(NucletContentUID uid, NuclosEntity entity) {
			if(uid == null)
				throw new IllegalArgumentException("uid must not be null");
			boolean result = false;
			for(NucletContentUID.Key key : keySet()) {
				NucletContentUID cuid = get(key);
				if(uid.equals(cuid) && key.entity == entity)
					result = true;
			}
			return result;
		}

		@Override
		public NucletContentUID.Key getKey(EntityObjectVO eo) {
			if(eo == null)
				throw new IllegalArgumentException("eo must not be null");
			NuclosEntity entity = NuclosEntity.getByName(eo.getEntity());
			if(entity != null) {
				return new NucletContentUID.Key(entity, eo.getId());
			}
			return null;
		}

		@Override
		public NucletContentUID getUID(EntityObjectVO eo) {
			NucletContentUID.Key key = getKey(eo);
			if(key != null) {
				return get(key);
			}
			return null;
		}
	}

	@Override
	public String toString() {
		return id + " v" + version + " uid=" + uid;
	}
}
