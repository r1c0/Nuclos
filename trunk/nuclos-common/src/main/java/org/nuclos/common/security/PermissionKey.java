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
package org.nuclos.common.security;

import org.nuclos.common2.LangUtils;

public class PermissionKey {
	/**
	 * defines a unique key to get the permission for an attribute used in a module within a status numeral
	 */
	public static class AttributePermissionKey {
		private String sEntity;
		private String sAttributeName;
		private Integer iState;

		public AttributePermissionKey(String sEntity, String sAttributeName, Integer iState) {
			this.sEntity = sEntity;
			this.sAttributeName = sAttributeName;
			this.iState = iState;
		}

		public String getEntity() {
			return sEntity;
		}

		public String getAttributeName() {
			return this.sAttributeName;
		}

		public Integer getStateId() {
			return this.iState;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || (this.getClass() != o.getClass())) {
				return false;
			}
			final AttributePermissionKey that = (AttributePermissionKey) o;
			return LangUtils.equals(this.sAttributeName, that.sAttributeName) &&
			LangUtils.equals(this.sEntity, that.sEntity) &&
			LangUtils.equals(this.iState, that.iState);
		}

		@Override
		public int hashCode() {
			return LangUtils.hashCode(this.sEntity) ^ LangUtils.hashCode(this.sAttributeName) ^ LangUtils.hashCode(this.iState);
		}
	}	// class AttributePermissionKey
	
	/**
	 * defines a unique key to get the permission for a subform used in an entity
	 */
	public static class SubFormPermissionKey {
		private String sEntityName;

		public SubFormPermissionKey(String sEntityName) {
			this.sEntityName = sEntityName;
		}

		public String getEntityName() {
			return this.sEntityName;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || (this.getClass() != o.getClass())) {
				return false;
			}
			final SubFormPermissionKey that = (SubFormPermissionKey) o;
			return LangUtils.equals(this.sEntityName, that.sEntityName);
		}

		@Override
		public int hashCode() {
			return LangUtils.hashCode(this.sEntityName) ^ LangUtils.hashCode(this.sEntityName);
		}
	}	// class SubFormPermissionKey
	
	
	public static class ModulePermissionKey {
		private Integer genericObjectId;
		private String entityName;
		
		public ModulePermissionKey(Integer genericObjectId, String entityName) {
			this.genericObjectId = genericObjectId;
			this.entityName = entityName;
		}
		
		public Integer getGenericObjectId() {
			return genericObjectId;
		}
		
		public String getEntityName() {
			return entityName;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || (this.getClass() != o.getClass())) {
				return false;
			}
			final ModulePermissionKey that = (ModulePermissionKey) o;
			return LangUtils.equals(this.genericObjectId, that.genericObjectId) &&
				LangUtils.equals(this.entityName, that.entityName);
		}

		@Override
		public int hashCode() {
			return LangUtils.hashCode(this.genericObjectId) ^ LangUtils.hashCode(this.entityName);
		}
	}
	
	public static class MasterDataPermissionKey {
		private String entityName;
		
		public MasterDataPermissionKey(String entityName) {
			this.entityName = entityName;
		}
		
		public String getEntityName() {
			return entityName;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || (this.getClass() != o.getClass())) {
				return false;
			}
			final MasterDataPermissionKey that = (MasterDataPermissionKey) o;
			return LangUtils.equals(this.entityName, that.entityName);
		}

		@Override
		public int hashCode() {
			return LangUtils.hashCode(this.entityName);
		}
	}
}
