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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.nuclos.server.common.valueobject.NuclosValueObject;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Value object representing a user for administration purposes (exclude preferences).
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
public class UserVO extends NuclosValueObject {

	public static final String FIELD_NAME = "name";
	public static final String FIELD_EMAIL = "email";
	public static final String FIELD_LASTNAME = "lastname";
	public static final String FIELD_FIRSTNAME = "firstname";
	public static final String FIELD_GROUP = "groupId";
	public static final String FIELD_SUPERUSER = "superuser";
	public static final String FIELD_LOCKED = "locked";
	public static final String FIELD_PASSWORDCHANGED = "passwordchanged";
	public static final String FIELD_EXPIRATIONDATE = "expirationdate";
	public static final String FIELD_REQUIREPASSWORDCHANGE = "requirepasswordchange";

	// non-persistent attributes
	public static final String FIELD_SETPASSWORD = "setPassword";
	public static final String FIELD_NEWPASSWORD = "newPassword";
	public static final String FIELD_NOTIFYUSER = "notifyUser";

	private String name;
	private String email;
	private String lastname;
	private String firstname;
	private Integer group;
	private Boolean superuser;
	private Boolean locked;
	private Date passwordChanged;
	private Date expirationDate;
	private Boolean requirePasswordChange;

	// non-persistent attributes
	private Boolean setPassword;
	private String newPassword;
	private Boolean notifyUser;

	private Map<String, Object> originalFields;

	public UserVO(MasterDataVO mdvo) {
		super(mdvo.getIntId(), mdvo.getCreatedAt(), mdvo.getCreatedBy(), mdvo.getChangedAt(), mdvo.getChangedBy(), mdvo.getVersion());
		this.name = (String) mdvo.getField(FIELD_NAME);
		this.email = (String) mdvo.getField(FIELD_EMAIL);
		this.lastname = (String) mdvo.getField(FIELD_LASTNAME);
		this.firstname = (String) mdvo.getField(FIELD_FIRSTNAME);
		this.group = (Integer) mdvo.getField(FIELD_GROUP);
		this.superuser = (Boolean) mdvo.getField(FIELD_SUPERUSER);
		this.locked = (Boolean) mdvo.getField(FIELD_LOCKED);
		this.passwordChanged = (Date) mdvo.getField(FIELD_PASSWORDCHANGED);
		this.expirationDate = (Date) mdvo.getField(FIELD_EXPIRATIONDATE);
		this.requirePasswordChange = (Boolean) mdvo.getField(FIELD_REQUIREPASSWORDCHANGE);
		this.originalFields = mdvo.getFields();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public Integer getGroup() {
		return group;
	}

	public void setGroup(Integer group) {
		this.group = group;
	}

	public Boolean getSuperuser() {
		return superuser;
	}

	public void setSuperuser(Boolean superuser) {
		this.superuser = superuser;
	}

	public Boolean getLocked() {
		return locked;
	}

	public void setLocked(Boolean locked) {
		this.locked = locked;
	}

	public Date getPasswordChanged() {
		return passwordChanged;
	}

	public void setPasswordChanged(Date passwordChanged) {
		this.passwordChanged = passwordChanged;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	public Boolean getRequirePasswordChange() {
		return requirePasswordChange;
	}

	public void setRequirePasswordChange(Boolean requirePasswordChange) {
		this.requirePasswordChange = requirePasswordChange;
	}

	public Boolean getSetPassword() {
		return setPassword;
	}

	public void setSetPassword(Boolean setPassword) {
		this.setPassword = setPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public Boolean getNotifyUser() {
		return notifyUser == null ? Boolean.FALSE : notifyUser;
	}

	public void setNotifyUser(Boolean notifyUser) {
		this.notifyUser = notifyUser;
	}

	public MasterDataVO toMasterDataVO() {
		Map<String, Object> fields = new HashMap<String, Object>(this.originalFields);
		fields.put(FIELD_NAME, name);
		fields.put(FIELD_EMAIL, email);
		fields.put(FIELD_LASTNAME, lastname);
		fields.put(FIELD_FIRSTNAME, firstname);
		fields.put(FIELD_GROUP, group);
		fields.put(FIELD_SUPERUSER, superuser);
		fields.put(FIELD_LOCKED, locked);
		fields.put(FIELD_PASSWORDCHANGED, passwordChanged);
		fields.put(FIELD_EXPIRATIONDATE, expirationDate);
		fields.put(FIELD_REQUIREPASSWORDCHANGE, requirePasswordChange);

		return new MasterDataVO(getId(), getCreatedAt(), getCreatedBy(), getChangedAt(), getChangedBy(), getVersion(), fields);
	}

	@Override
	public int hashCode() {
		return (getName() != null ? getName().hashCode() : 0);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof UserVO) {
			final UserVO that = (UserVO) o;
			// rules are equal if there names are equal
			return this.getName().equals(that.getName());
		}
		return false;
	}

	@Override
	public String toString() {
		return this.getName();
	}

}	// class UserVO
