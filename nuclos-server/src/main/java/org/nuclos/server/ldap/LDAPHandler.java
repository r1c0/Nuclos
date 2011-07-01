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
package org.nuclos.server.ldap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;

import org.apache.log4j.Logger;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;

/**
 * LDAP Handler class for connecting to and searching in LDAP repository.
 *
 * <br>
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author <a href="mailto:rostislav.maksymovskyi@novabit.de">Rostislav Maksymovskyi</a>
 * @author <a href="mailto:thomas.schiffmann@novabit.de">thomas.schiffmann</a>
 * @version 01.00
 */
public class LDAPHandler {
	private static final Logger log = Logger.getLogger(LDAPHandler.class);

	private Hashtable<String, String> env = null;
	private LdapContext ctx = null;

	public LDAPHandler(String providerURL, String bindDN, String bindCredential) {
		super();
		init(providerURL, bindDN, bindCredential);
	}

	private void init(String providerURL, String bindDN, String bindCredential) {
		env = new Hashtable<String, String>();
		// TODO make environment customizable
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, providerURL);
		env.put(Context.REFERRAL, "follow");
		if (bindDN != null) {
			env.put(Context.SECURITY_PRINCIPAL, bindDN);
			env.put(Context.SECURITY_CREDENTIALS, bindCredential);
		}

		try {
			ctx = new InitialLdapContext(env, null);
			log.debug("Connection to ldap server established (" + env.toString() + ")");
		} catch (NamingException e) {
			e.printStackTrace();
			throw new CommonFatalException("Could not establish connection to : " + providerURL);
		}
	}

	public <O> Collection<O> performSearch(String context, String filterExpr, Object[] filterArgs, SearchControls constraints, Transformer<SearchResult, O> transformer) throws CommonBusinessException {
		final Collection<O> result = new ArrayList<O>();
		// TODO make pagesize customizable
		int pageSize = 1000; // 1000 entries per page
		byte[] cookie = null;

		log.debug("Search LDAP directory for ");
		log.debug("SEARCH_CONTEXT : " + context);
		log.debug("SEARCH_FILTER : " + filterExpr);
		log.debug("SEARCH_ARGS : " + filterArgs);
		log.debug("CONSTRAINTS :" + constraints.toString());

		try {
			ctx.setRequestControls(new Control[] { new PagedResultsControl(pageSize, Control.CRITICAL) });

			do {
				// Perform the search
				NamingEnumeration<SearchResult> persons = ctx.search(context, filterExpr, filterArgs, constraints);

				// Iterate over a batch of search results
				while (persons != null && persons.hasMoreElements()) {
					// Display an entry
					SearchResult sr = persons.next();
					log.debug("SearchResult: " + sr.getName());
					result.add(transformer.transform(sr));
				}

				// Examine the paged results control response
				Control[] controls = ctx.getResponseControls();
				if (controls != null) {
					for (int i = 0; i < controls.length; i++) {
						if (controls[i] instanceof PagedResultsResponseControl) {
							PagedResultsResponseControl prrc = (PagedResultsResponseControl) controls[i];
							cookie = prrc.getCookie();
						}
					}
				}

				// Re-activate paged results
				ctx.setRequestControls(new Control[] { new PagedResultsControl(pageSize, cookie, Control.CRITICAL) });
			} while (cookie != null);

			ctx.close();

			log.debug("Search returned " + result.size() + " results.");
		}
		catch (Exception ex) {
			log.error("An error occured during ldap search.", ex);
			throw new CommonBusinessException("ldap.exception.search", ex);
		}
		return result;
	}

}
