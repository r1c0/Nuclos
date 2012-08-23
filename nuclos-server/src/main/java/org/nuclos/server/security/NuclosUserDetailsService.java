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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.autosync.XMLEntities;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.database.SpringDataBaseHelper;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.expression.DbCurrentDateTime;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.dblayer.statements.DbUpdateStatement;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeHelper;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation=Propagation.REQUIRES_NEW, noRollbackFor= {Exception.class})
public class NuclosUserDetailsService implements org.nuclos.server.security.UserDetailsService {

	private static final Logger log = Logger.getLogger(MasterDataFacadeHelper.class);

	private ParameterProvider paramprovider;
	
	private SpringDataBaseHelper dataBaseHelper;
	
	NuclosUserDetailsService() {
	}
	
	@Autowired
	void setDataBaseHelper(SpringDataBaseHelper dataBaseHelper) {
		this.dataBaseHelper = dataBaseHelper;
	}

	public void setParameterProvider(ParameterProvider paramprovider) {
		this.paramprovider = paramprovider;
	}

	/**
	 * Load user including roles (actions) from nuclos database
	 * (T_MD_USER, T_MD_ROLE_USER, T_MD_ROLE, T_MD_ROLE_ACTION, T_AD_ACTION) and JSON.
	 */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom t = query.from("T_MD_USER").alias(SystemFields.BASE_ALIAS);
		query.multiselect(
			t.baseColumn("INTID", Long.class),
			t.baseColumn("STRPASSWORD", String.class),
			t.baseColumn("BLNSUPERUSER", Boolean.class),
			t.baseColumn("DATLASTLOGIN", Date.class),
			t.baseColumn("BLNLOCKED", Boolean.class),
			t.baseColumn("DATEXPIRATIONDATE", Date.class),
			t.baseColumn("DATPASSWORDCHANGED", Date.class),
			t.baseColumn("BLNREQUIREPASSWORDCHANGE", Boolean.class),
			t.baseColumn("STRUSER", String.class));
		query.where(builder.equal(builder.upper(t.baseColumn("STRUSER", String.class)), builder.upper(builder.literal(username))));

		final DbTuple tuple = CollectionUtils.getFirst(dataBaseHelper.getDbAccess().executeQuery(query));
		final Long intid;
		final Boolean isSuperUser;
		final String password;
		final Date lastlogin;
		Boolean locked;
		final Date expiration;
		final Date passwordchanged;
		final Boolean requirechange;
		final String user;

		if (tuple != null) {
			intid = tuple.get(0, Long.class);
			password = tuple.get(1, String.class);
			isSuperUser = Boolean.TRUE.equals(tuple.get(2, Boolean.class));
			lastlogin = tuple.get(3, Date.class);
			locked = Boolean.TRUE.equals(tuple.get(4, Boolean.class));
			expiration = tuple.get(5, Date.class);
			passwordchanged = tuple.get(6, Date.class);
			requirechange = Boolean.TRUE.equals(tuple.get(7, Boolean.class));
			user = tuple.get(8, String.class);
		} else {
			throw new UsernameNotFoundException("User not found");
		}

		Boolean expired = false;
		if (expiration != null) {
			Calendar c = Calendar.getInstance();
			c.setTime(expiration);
			expired = c.before(Calendar.getInstance());
		}

		String lockDays = paramprovider.getValue(ParameterProvider.KEY_SECURITY_LOCK_DAYS);
		if (!locked && lastlogin != null && !StringUtils.isNullOrEmpty(lockDays)) {
			try {
				Integer days = Integer.parseInt(lockDays);
				Calendar c = Calendar.getInstance();
				c.setTime(lastlogin);
				c.add(Calendar.DAY_OF_MONTH, days);
				if (Calendar.getInstance().after(c)) {
					lockUser(intid);
					locked = true;
				}
			}
			catch (NumberFormatException ex) {
				log.error("Cannot parse parameter value for key " + ParameterProvider.KEY_SECURITY_LOCK_DAYS, ex);
			}
		}

		Boolean credentialsExpired = false;
		String passwordInterval = paramprovider.getValue(ParameterProvider.KEY_SECURITY_PASSWORD_INTERVAL);
		if (!StringUtils.isNullOrEmpty(passwordInterval)) {
			if (passwordchanged != null) {
				try {
					Integer days = Integer.parseInt(passwordInterval);
					Calendar c = Calendar.getInstance();
					c.setTime(passwordchanged);
					c.add(Calendar.DAY_OF_MONTH, days);
					credentialsExpired = c.before(Calendar.getInstance());
				}
				catch (NumberFormatException ex) {
					log.error("Cannot parse parameter value for key " + ParameterProvider.KEY_SECURITY_PASSWORD_INTERVAL, ex);
				}
			}
			else {
				setPasswordChanged(intid);
			}
		}

		Set<String> actions;

		if (isSuperUser) {
			actions = getSuperUserActions();
		}
		else {
			actions = SecurityCache.getInstance().getAllowedActions(username);
		}

		List<GrantedAuthority> authorities = CollectionUtils.transform(actions, new Transformer<String, GrantedAuthority>() {
			@Override
			public GrantedAuthority transform(String i) {
				return new SimpleGrantedAuthority(i);
			}
		});

		return new User(user, password == null ? "" : password, true, !expired, !credentialsExpired && !requirechange, !locked, authorities);
	}

	@Override
	public void logAttempt(String username, boolean authenticated) {
		Integer maxattempts = 0;
		String sMaxattempts = paramprovider.getValue(ParameterProvider.KEY_SECURITY_LOCK_ATTEMPTS);
		if (!StringUtils.isNullOrEmpty(sMaxattempts)) {
			try {
				maxattempts = Integer.parseInt(sMaxattempts);
			}
			catch (NumberFormatException ex) {
				log.error("Cannot parse parameter value for key " + ParameterProvider.KEY_SECURITY_LOCK_ATTEMPTS, ex);
				return;
			}
		}

		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom t = query.from("T_MD_USER").alias(SystemFields.BASE_ALIAS);
		query.multiselect(t.baseColumn("INTID", Long.class), t.baseColumn("INTLOGINATTEMPTS", Integer.class));
		query.where(builder.equal(builder.upper(t.baseColumn("STRUSER", String.class)), builder.upper(builder.literal(username))));
		DbTuple tuple = dataBaseHelper.getDbAccess().executeQuerySingleResult(query);

		Long id;
		Integer loginattempts;

		if (tuple != null) {
			id = tuple.get(0, Long.class);
			loginattempts = tuple.get(1, Integer.class);
		} else {
			throw new UsernameNotFoundException("User not found");
		}

		Map<String, Object> values = new HashMap<String, Object>(2);
		if (!authenticated) {
			if (loginattempts == null) {
				loginattempts = 1;
			}
			else {
				loginattempts++;
			}
			values.put("INTLOGINATTEMPTS", loginattempts);
			if (maxattempts > 0 && loginattempts.compareTo(maxattempts) >= 0) {
				values.put("BLNLOCKED", true);
			}
		}
		else {
			values.put("INTLOGINATTEMPTS", 0);
			values.put("DATLASTLOGIN", DbCurrentDateTime.CURRENT_DATETIME);
		}
		Map<String, Object> conditions = new HashMap<String, Object>(1);
		conditions.put("INTID", id);
		dataBaseHelper.getDbAccess().execute(new DbUpdateStatement("T_MD_USER", values, conditions));
	}

	static Set<String> getSuperUserActions() {
		DbQueryBuilder builder = SpringDataBaseHelper.getInstance().getDbAccess().getQueryBuilder();
		Set<String> actions = new HashSet<String>();
		DbQuery<String> rolesQuery = builder.createQuery(String.class);
		DbFrom action = rolesQuery.from("T_AD_ACTION").alias(SystemFields.BASE_ALIAS);
		rolesQuery.select(action.baseColumn("STRACTION", String.class));
		actions.addAll(SpringDataBaseHelper.getInstance().getDbAccess().executeQuery(rolesQuery));

		for(MasterDataVO mdvo : XMLEntities.getData(NuclosEntity.ACTION).getAll()) {
			actions.add(mdvo.getField("action", String.class));
		}

		return actions;
	}

	private void lockUser(Long user) {
		Map<String, Object> values = new HashMap<String, Object>(1);
		values.put("BLNLOCKED", Boolean.TRUE);
		Map<String, Object> conditions = new HashMap<String, Object>(1);
		conditions.put("INTID", user);
		dataBaseHelper.getDbAccess().execute(new DbUpdateStatement("T_MD_USER", values, conditions));
	}

	private void setPasswordChanged(Long user) {
		Map<String, Object> values = new HashMap<String, Object>(1);
		values.put("DATPASSWORDCHANGED", Calendar.getInstance().getTime());
		Map<String, Object> conditions = new HashMap<String, Object>(1);
		conditions.put("INTID", user);
		dataBaseHelper.getDbAccess().execute(new DbUpdateStatement("T_MD_USER", values, conditions));
	}
}
