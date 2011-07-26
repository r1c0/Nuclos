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

import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * AuthenticationProvider for Nuclos.
 *
 * TODO add support for caching (ldap servers and users)
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
public class NuclosAuthenticationProvider implements AuthenticationProvider, MessageListener {

	private static final Logger log = Logger.getLogger(NuclosAuthenticationProvider.class);

	private UserDetailsService userDetailsService;

	private List<NuclosLdapBindAuthenticator> ldapAuthenticators;

	public void setUserDetailsService(UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		if (authentication instanceof NuclosLocalServerAuthenticationToken) {
			log.debug("Local server authentication: ...");
			return authentication;
		}

		boolean authenticated = false;

		String username = authentication.getPrincipal().toString();
		String password = authentication.getCredentials().toString();
		log.debug("NuclosAuthenticationProvider.authenticate(" + username + "), locale:" + LocaleContextHolder.getLocale().toString());

		UserDetails userDetails = userDetailsService.loadUserByUsername(authentication.getName());

		if (!userDetails.isAccountNonLocked()) {
			throw new LockedException("nuclos.security.authentication.locked");
		}

		if (!userDetails.isAccountNonExpired()) {
			throw new AccountExpiredException("nuclos.security.authentication.accountexpired");
		}

		try	{
			for (NuclosLdapBindAuthenticator authenticator : getLdapBindAuthenticators()) {
				try {
					if (authenticator.authenticate(authentication)) {
						authenticated = true;
						break;
					}
				}
				catch (BadCredentialsException ex) {
					// authentication not successful, continue
				}
				catch (Exception ex) {
					log.error(ex);
				}
			}
		}
		catch (Exception ex) {
			log.error("Configuration of ldap authenticators failed.", ex);
		}

		// if ldap authentication is not active or the user is a superuser, try authentication against db
		if ((getLdapBindAuthenticators() == null || getLdapBindAuthenticators().size() == 0) || SecurityCache.getInstance().isSuperUser(username)) {
			// fallback, if password was deleted in database
			if (StringUtils.isNullOrEmpty(userDetails.getPassword()) && StringUtils.isNullOrEmpty(password)) {
				authenticated = true;
			}

			final String sPasswordFromUser = StringUtils.encryptBase64(username + ((password == null) ? "" : new String(password)));
			if(sPasswordFromUser.equals(userDetails.getPassword())) {
				authenticated = true;
			}

			if (authenticated && !userDetails.isCredentialsNonExpired()) {
				throw new CredentialsExpiredException("nuclos.security.authentication.credentialsexpired");
			}
		}

		if (!authenticated) {
			throw new BadCredentialsException("invalid.login.exception");//"Benutzername/Kennwort ung\u00fcltig.");
		}

		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, userDetails.getPassword(), userDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(auth);
		return auth;
	}

	private List<NuclosLdapBindAuthenticator> getLdapBindAuthenticators() {
		if (ldapAuthenticators == null) {
			this.ldapAuthenticators = new ArrayList<NuclosLdapBindAuthenticator>();

			List<EntityObjectVO> servers = CollectionUtils.applyFilter(NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.LDAPSERVER).getAll(), new Predicate<EntityObjectVO>() {
				@Override
				public boolean evaluate(EntityObjectVO t) {
					Boolean active = t.getField("active", Boolean.class);
					String authfilter = t.getField("userfilter", String.class);
					if (active != null && active && !StringUtils.isNullOrEmpty(authfilter)) {
						return true;
					}
					else {
						return false;
					}
				}
			});

			this.ldapAuthenticators.addAll(CollectionUtils.transform(servers, new Transformer<EntityObjectVO, NuclosLdapBindAuthenticator>() {
				@Override
				public NuclosLdapBindAuthenticator transform(EntityObjectVO i) {
					String url = i.getField("serverurl", String.class);
					String baseDN = i.getField("serversearchcontext", String.class);
					String bindDN = i.getField("binddn", String.class);
					String bindCredential = i.getField("bindcredential", String.class);
					String userSearchFilter = i.getField("userfilter", String.class);
					Integer scope = i.getField("serversearchscope", Integer.class);
					return new NuclosLdapBindAuthenticator(url, baseDN, bindDN, bindCredential, userSearchFilter, scope);
				}
			}));
		}
		return ldapAuthenticators;
	}

	@Override
	public boolean supports(Class<? extends Object> clazz) {
		if (UsernamePasswordAuthenticationToken.class.isAssignableFrom(clazz)) {
			return true;
		} else if (clazz == NuclosLocalServerAuthenticationToken.class) {
			return true;
		}
		return false;
	}

	@Override
	public void onMessage(Message message) {
		if(message instanceof TextMessage) {
			try {
				String text = ((TextMessage) message).getText();
				if (StringUtils.isNullOrEmpty(text) || text.equals(NuclosEntity.LDAPSERVER.getEntityName())) {
					log.info("Invalidate ldap servers.");
					this.ldapAuthenticators = null;
				}
			}
			catch(JMSException ex) {
				log.error(ex);
			}
		}
	}

}
