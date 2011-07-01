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
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Implementation for Spring's <code>LdapUserSearch</code> that uses Spring LDAP's <code>LdapTemplate</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
public class LdapUserSearch implements org.springframework.security.ldap.search.LdapUserSearch {

	private static final Logger log = Logger.getLogger(LdapUserSearch.class);

	private final ContextSource contextSource;
    private final String searchBase;
    private final String searchFilter;
    private final int scope;

    public LdapUserSearch(String searchBase, String searchFilter, BaseLdapPathContextSource contextSource, int scope) {
        this.contextSource = contextSource;
        this.searchBase = searchBase;
        this.searchFilter = searchFilter;
        this.scope = scope;
    }

	@SuppressWarnings("rawtypes")
	@Override
	public DirContextOperations searchForUser(String username) throws UsernameNotFoundException {
		if (log.isDebugEnabled()) {
			log.debug("Searching for user '" + username + "'.");
        }

		LdapTemplate template = new LdapTemplate(contextSource);
		template.setIgnorePartialResultException(true);

		ContextMapper mapper = new ContextMapper() {
            @Override
			public Object mapFromContext(Object ctx) {
            	return ctx;
            }
        };

        List l = template.search("", MessageFormat.format(searchFilter, username), scope, mapper);
		if (l.size() == 0) {
			throw new UsernameNotFoundException("User " + username + " not found in directory.", username);
		}
		else if (l.size() != 1) {
			throw new IncorrectResultSizeDataAccessException(1, l.size());
		}

		return (DirContextOperations) l.get(0);
	}

}
