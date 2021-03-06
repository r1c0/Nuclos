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
import java.util.Collection;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

	private static final Logger LOG = Logger.getLogger(NuclosAuthenticationProvider.class);

	private UserDetailsService userDetailsService;

	private List<NuclosLdapBindAuthenticator> ldapAuthenticators;

	public void setUserDetailsService(UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		if (authentication instanceof NuclosLocalServerAuthenticationToken) {
			LOG.debug("Local server authentication: ...");
			return authentication;
		}

		boolean authenticated = false;

		final String username = authentication.getPrincipal().toString();
		final String password = authentication.getCredentials().toString();

		final UserDetails userDetails = userDetailsService.loadUserByUsername(authentication.getName());
		LOG.debug("NuclosAuthenticationProvider.authenticate(" + userDetails.getUsername() + "), locale:" + LocaleContextHolder.getLocale().toString());

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
					LOG.error(ex);
				}
			}
		}
		catch (Exception ex) {
			LOG.error("Configuration of ldap authenticators failed.", ex);
		}

		// if ldap authentication is not active or the user is a superuser, try authentication against db
		if ((getLdapBindAuthenticators() == null || getLdapBindAuthenticators().size() == 0) || SecurityCache.getInstance().isSuperUser(username)) {
			// fallback, if password was deleted in database
			if (StringUtils.isNullOrEmpty(userDetails.getPassword()) && StringUtils.isNullOrEmpty(password)) {
				authenticated = true;
			}

			final String sPasswordFromUser = StringUtils.encryptPw(username, password);
			if(sPasswordFromUser.equals(userDetails.getPassword())) {
				authenticated = true;
			}

			// Allow user to change password if credential is expired
			if (authenticated && !userDetails.isCredentialsNonExpired()) {
				
				// Authenticate the user for ChangeOwnPassword only.
				final Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
				authorities.add(new SimpleGrantedAuthority("Login"));
				// authorities.add(new SimpleGrantedAuthority("ChangeOwnPassword"));
				final UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
						userDetails.getUsername(), userDetails.getPassword(), authorities);
				SecurityContextHolder.getContext().setAuthentication(auth);
				LOG.info("User " + userDetails.getUsername() + " gets authenticated only for ChangeOwnPassword: " + auth.isAuthenticated());
				
				// This exception trigger the change password dialog on login.
				throw new CredentialsExpiredException("nuclos.security.authentication.credentialsexpired");
			}
		}

		if (!authenticated) {
			throw new BadCredentialsException("invalid.login.exception");//"Benutzername/Kennwort ung\u00fcltig.");
		}

		final UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
				userDetails.getUsername(), userDetails.getPassword(), userDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(auth);
		LOG.info("User " + userDetails.getUsername() + " gets authenticated: " + auth.isAuthenticated());
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
					LOG.info("onMessage: Invalidate ldap servers.");
					this.ldapAuthenticators = null;
				}
			}
			catch(JMSException e) {
				LOG.error("onMessage failed: " + e, e);
			}
		}
	}

}
