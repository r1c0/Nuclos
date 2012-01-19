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

import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.StringUtils;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.authentication.BindAuthenticator;

/**
 * Implementation of LDAP authentication.
 * As the ldap server settings are defined in the nuclos entities, we cannot use spring beans for configuration.
 *
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
public class NuclosLdapBindAuthenticator {

	private BindAuthenticator authenticator;

	public NuclosLdapBindAuthenticator(String url, String baseDN, String bindDN, String bindCredential, String userSearchFilter, Integer scope) {
		LdapContextSource contextSource = new LdapContextSource();
		contextSource.setUrl(url);
		contextSource.setBase(baseDN);
		if (!StringUtils.isNullOrEmpty(bindDN)) {
			contextSource.setUserDn(bindDN);
			contextSource.setPassword(bindCredential);
		}
		else {
			contextSource.setAnonymousReadOnly(true);
		}
		contextSource.setReferral("ignore");
		try {
			contextSource.afterPropertiesSet();
		}
		catch(Exception e) {
			throw new NuclosFatalException(e);
		}

		authenticator = new BindAuthenticator(contextSource);
		// do not define searchbase because baseDN is already defined in contextSource
		authenticator.setUserSearch(new LdapUserSearch(baseDN, userSearchFilter, contextSource, scope));
	}

	public boolean authenticate(Authentication authentication) {
		return authenticator.authenticate(authentication) != null;
	}

}
