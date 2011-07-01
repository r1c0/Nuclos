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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.server.autosync.XMLEntities;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class NuclosUserDetailsService implements UserDetailsService {

	/**
	 * Load user including roles (actions) from nuclos database (T_MD_USER, T_MD_ROLE_USER, T_MD_ROLE, T_MD_ROLE_ACTION, T_AD_ACTION)
	 * and JSON.
	 *
	 * We could add support for following features here:
	 * - Account locking
	 * - Account expiration
	 * - Password expiration
	 *
	 * TODO add support for sessions
	 */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom t = query.from("T_MD_USER").alias("t");
		query.multiselect(
			t.column("STRPASSWORD", String.class),
			t.column("BLNSUPERUSER", Boolean.class));
		query.where(builder.equal(builder.upper(t.column("STRUSER", String.class)), builder.upper(builder.literal(username))));

		DbTuple tuple = CollectionUtils.getFirst(DataBaseHelper.getDbAccess().executeQuery(query));
		Boolean isSuperUser;
		String password;

		if (tuple != null) {
			password = tuple.get(0, String.class);
			isSuperUser = Boolean.TRUE.equals(tuple.get(1, Boolean.class));
		} else {
			throw new UsernameNotFoundException("User not found");
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
				return new GrantedAuthorityImpl(i);
			}
		});

		return new User(username, password == null ? "" : password, true, true, true, true, authorities);
	}

	static Set<String> getSuperUserActions() {
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		Set<String> actions = new HashSet<String>();
		DbQuery<String> rolesQuery = builder.createQuery(String.class);
		DbFrom action = rolesQuery.from("T_AD_ACTION").alias("t");
		rolesQuery.select(action.column("STRACTION", String.class));
		actions.addAll(DataBaseHelper.getDbAccess().executeQuery(rolesQuery));

		for(MasterDataVO mdvo : XMLEntities.getData(NuclosEntity.ACTION).getAll()) {
			actions.add(mdvo.getField("action", String.class));
		}
		
		return actions;
	}

}
