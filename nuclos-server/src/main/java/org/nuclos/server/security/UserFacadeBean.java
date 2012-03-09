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
package org.nuclos.server.security;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.annotation.security.RolesAllowed;

import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common.mail.NuclosMail;
import org.nuclos.common.security.UserFacadeRemote;
import org.nuclos.common.security.UserVO;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.ServerParameterProvider;
import org.nuclos.server.common.ServerServiceLocator;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.common.mail.NuclosMailSender;
import org.nuclos.server.dal.DalUtils;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.dblayer.statements.DbDeleteStatement;
import org.nuclos.server.dblayer.statements.DbInsertStatement;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation=Propagation.REQUIRED, noRollbackFor= {Exception.class})
@RolesAllowed("Login")
public class UserFacadeBean extends NuclosFacadeBean implements UserFacadeRemote {
	
	private MasterDataFacadeLocal masterDataFacade;
	
	public UserFacadeBean() {
	}

	@Autowired
	final void setMasterDataFacade(MasterDataFacadeLocal masterDataFacade) {
		this.masterDataFacade = masterDataFacade;
	}
	
	private final MasterDataFacadeLocal getMasterDataFacade() {
		return masterDataFacade;
	}

	public UserVO create(UserVO vo, DependantMasterDataMap mpDependants) throws CommonBusinessException {
		checkWriteAllowed(NuclosEntity.USER);

		MasterDataVO mdvo = vo.toMasterDataVO();
		if (vo.getSetPassword()) {
			mdvo = setPassword(vo);
		}

		mdvo = getMasterDataFacade().create(NuclosEntity.USER.getEntityName(), mdvo, mpDependants);

		if (vo.getSetPassword() && vo.getNotifyUser()) {
			notifyUser(vo, "Nuclos - account created", "The password for your new Nuclos account is: {0}");
		}
		return new UserVO(mdvo);
	}

	public UserVO modify(UserVO vo, DependantMasterDataMap mpDependants) throws CommonBusinessException {
		checkWriteAllowed(NuclosEntity.USER);

		MasterDataVO mdvo = vo.toMasterDataVO();
		if (vo.getSetPassword()) {
			mdvo = setPassword(vo);
		}
		Object id = getMasterDataFacade().modify(NuclosEntity.USER.getEntityName(), mdvo, mpDependants);

		if (vo.getSetPassword() && vo.getNotifyUser()) {
			notifyUser(vo, "Nuclos - password notification", "The password for your Nuclos account has been reset to: {0}");
		}
		return new UserVO(getMasterDataFacade().get(NuclosEntity.USER.getEntityName(), id));
	}

	public void remove(UserVO vo) throws CommonBusinessException {
		checkWriteAllowed(NuclosEntity.USER);
		// clear password history
		getMasterDataFacade().remove(NuclosEntity.USER.getEntityName(), vo.toMasterDataVO(), true);
	}

	public void setPassword(String username, String password) throws CommonBusinessException {
		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Integer> query = builder.createQuery(Integer.class);
		DbFrom t = query.from("T_MD_USER").alias(SystemFields.BASE_ALIAS);
		query.select(t.baseColumn("INTID", Integer.class));
		query.where(builder.equal(builder.upper(t.baseColumn("STRUSER", String.class)), builder.upper(builder.literal(username))));
		Integer userId = dataBaseHelper.getDbAccess().executeQuerySingleResult(query);

		UserVO user = new UserVO(ServerServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class).get(NuclosEntity.USER.getEntityName(), userId));
		user.setSetPassword(true);
		user.setNewPassword(password);
		user.setPasswordChanged(Calendar.getInstance().getTime());
		user.setRequirePasswordChange(false);
		modify(user, null);
	}

	private MasterDataVO setPassword(UserVO user) throws CommonValidationException {
		MasterDataVO result = user.toMasterDataVO();

		String passwd = user.getNewPassword();

		if (StringUtils.isNullOrEmpty(passwd)) {
			throw new CommonValidationException("exception.password.empty");
		}
		else {
			String passwdEncrypted = StringUtils.encryptBase64(user.getName().toLowerCase() + user.getNewPassword());

			// check min length first
			String sMinLength = ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_SECURITY_PASSWORD_STRENGTH_LENGTH);
			if (!StringUtils.isNullOrEmpty(sMinLength)) {
				try {
					Integer length = Integer.parseInt(sMinLength);
					if (passwd.length() < length) {
						throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage("exception.password.length", length));
					}
				}
				catch (NumberFormatException ex) {
					throw new NuclosFatalException(StringUtils.getParameterizedExceptionMessage("exception.parameter.type", sMinLength), ex);
				}
			}

			// check regular expression
			String sRegExp = ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_SECURITY_PASSWORD_STRENGTH_REGEXP);
			if (!StringUtils.isNullOrEmpty(sRegExp)) {
				try {
					if (!Pattern.matches(sRegExp, passwd)) {
						throw new CommonValidationException("exception.password.regexp");
					}
				}
				catch (PatternSyntaxException ex) {
					throw new NuclosFatalException(StringUtils.getParameterizedExceptionMessage("exception.parameter.invalid.regexp", sRegExp), ex);
				}
			}

			if (user.getId() != null) {
				String oldpasswordencrypted = getEncryptedPassword(user.getId().longValue());
				if (LangUtils.equals(passwdEncrypted, oldpasswordencrypted)) {
					throw new CommonValidationException("exception.password.equals.previous");
				}
			}

			// check password history
			if (user.getId() != null) {
				List<String> history = getPasswordHistory(user.getId().longValue());
				for (String pwd : history) {
					if (LangUtils.equals(passwdEncrypted, pwd)) {
						throw new CommonValidationException("exception.password.history");
					}
				}
			}

			result.setField("password", passwdEncrypted);
			if (user.getId() != null) {
				updatePasswordHistory(user.getId().longValue(), passwdEncrypted);
			}
			return result;
		}
	}

	private List<String> getPasswordHistory(Long user) {
		Integer number = getIntegerParameter(ParameterProvider.KEY_SECURITY_PASSWORD_HISTORY_NUMBER, 0);
		Integer days = getIntegerParameter(ParameterProvider.KEY_SECURITY_PASSWORD_HISTORY_DAYS, 0);
		if (number == 0 && days == 0) {
			// no validation
			return new ArrayList<String>();
		}

		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom t = query.from("T_MD_PASSWORDHISTORY").alias(SystemFields.BASE_ALIAS);
		query.multiselect(t.baseColumn("INTID", Long.class), t.baseColumn("DATCREATED", Date.class), t.baseColumn("STRPASSWORD", String.class));
		query.where(builder.equal(t.baseColumn("INTID_T_MD_USER", Long.class), user));
		query.orderBy(builder.desc(t.baseColumn("DATCREATED", Date.class)), builder.desc(t.baseColumn("INTID", Long.class)));
		List<DbTuple> result = dataBaseHelper.getDbAccess().executeQuery(query);

		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, -1 * days);
		int counter = 0;

		List<String> history = new ArrayList<String>();

		for (DbTuple tuple : result) {
			Date created = tuple.get(1, Date.class);
			boolean relevant = false;
			if (days != 0 && created.after(c.getTime())) {
				relevant = true;
			}

			if (number != 0 && counter < number ) {
				relevant = true;
			}

			if (relevant) {
				history.add(tuple.get(2, String.class));
			}
			counter++;
		}
		return history;
	}

	private String getEncryptedPassword(Long user) {
		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<String> query = builder.createQuery(String.class);
		DbFrom t = query.from("T_MD_USER").alias(SystemFields.BASE_ALIAS);
		query.select(t.baseColumn("STRPASSWORD", String.class));
		query.where(builder.equal(t.baseColumn("INTID", Long.class), user));
		return dataBaseHelper.getDbAccess().executeQuerySingleResult(query);
	}

	private void updatePasswordHistory(Long user, String encryptedpassword) {
		Integer number = getIntegerParameter(ParameterProvider.KEY_SECURITY_PASSWORD_HISTORY_NUMBER, 0);
		Integer days = getIntegerParameter(ParameterProvider.KEY_SECURITY_PASSWORD_HISTORY_DAYS, 0);

		if (days != 0 || number != 0) {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("INTID", DalUtils.getNextId());
			m.put("INTID_T_MD_USER", user);
			m.put("STRPASSWORD", encryptedpassword);
			m.put("DATCREATED", new Date(System.currentTimeMillis()));
			m.put("STRCREATED", getCurrentUserName());
			m.put("DATCHANGED", new Date(System.currentTimeMillis()));
			m.put("STRCHANGED", getCurrentUserName());
			m.put("INTVERSION", 1);
			dataBaseHelper.getDbAccess().execute(new DbInsertStatement("T_MD_PASSWORDHISTORY", m));
		}

		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom t = query.from("T_MD_PASSWORDHISTORY").alias(SystemFields.BASE_ALIAS);
		query.multiselect(t.baseColumn("INTID", Long.class), t.baseColumn("DATCREATED", Date.class));
		query.where(builder.equal(t.baseColumn("INTID_T_MD_USER", Long.class), user));
		query.orderBy(builder.desc(t.baseColumn("DATCREATED", Date.class)), builder.desc(t.baseColumn("INTID", Long.class)));
		List<DbTuple> result = dataBaseHelper.getDbAccess().executeQuery(query);

		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, -1 * days);
		int counter = 0;

		for (DbTuple tuple : result) {
			Date created = tuple.get(1, Date.class);
			boolean remove = true;
			if (days != 0 && created.after(c.getTime())) {
				remove = false;
			}

			if (number != 0 && counter < number) {
				remove = false;
			}

			if (remove) {
				Long id = tuple.get(0, Long.class);
				Map<String, Object> condition = new HashMap<String, Object>(1);
				condition.put("INTID", id);
				dataBaseHelper.getDbAccess().execute(new DbDeleteStatement("T_MD_PASSWORDHISTORY", condition));
			}
			counter++;
		}
	}

	private void notifyUser(UserVO user, String subject, String message) throws CommonBusinessException {
		if (StringUtils.isNullOrEmpty(user.getEmail())) {
			throw new CommonBusinessException("exception.user.email.empty");
		}

		if (StringUtils.isNullOrEmpty(ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_SMTP_SERVER))) {
			throw new CommonBusinessException("exception.parameter.email.configuration");
		}

		NuclosMail mail = new NuclosMail(user.getEmail(), subject, MessageFormat.format(message, user.getNewPassword()));
		NuclosMailSender.sendMail(mail);
	}

	private Integer getIntegerParameter(String name,Integer defaultValue) {
		String value = ServerParameterProvider.getInstance().getValue(name);
		if (!StringUtils.isNullOrEmpty(value)) {
			try {
				return Integer.parseInt(value);
			}
			catch (NumberFormatException ex) {
				throw new NuclosFatalException(StringUtils.getParameterizedExceptionMessage("exception.parameter.type", value), ex);
			}
		}
		return defaultValue;
	}
}
