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

package org.nuclos.server.webservice.ejb3;

import java.io.IOException;
import java.util.ArrayList;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.nuclos.common2.exception.CommonFatalException;

@Stateless
//@WebService(serviceName = "webEntry", endpointInterface = "org.nuclos.server.webservice.ejb3.WebEntryWS")
public class WebEntryBean implements WebEntryWS {
	@EJB private WebAccessWS webAccess;
	
	/**
	 * The function that must be called by EVERY public service function here.
	 * 
	 * Will either set the authenticated principal, so that following service
	 * calls accept it, or throw a fatal exception.
	 * 
	 * @param user  the user
	 * @param pass  his password
	 */
	private void authenticate(final String user, final String pass) {
		final char[] pchars = new char[pass.length()];
		pass.getChars(0, pass.length(), pchars, 0);
		try {
			LoginContext lc
			= new LoginContext("NuclosSecurityDomain", new CallbackHandler() {
				@Override
				public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
					for(int i = 0; i < callbacks.length; i++) {
						if(callbacks[i] instanceof NameCallback) {
							((NameCallback) callbacks[i]).setName(user);
						}
						else if(callbacks[i] instanceof PasswordCallback) {
							((PasswordCallback) callbacks[i]).setPassword(pchars);
						}
						else {
							throw new UnsupportedCallbackException(callbacks[i],
							"Callback class not supported");
						}
					}
				}
			});
			lc.login();
			//SecurityAssociation.setPrincipal(new NuclosWebServiceAuthenticatedPrincipal(user, pchars));
		}
		catch(LoginException e) {
			throw new CommonFatalException(e);
		}
	}
	
	@Override
	public ArrayList<String> listEntities_A(String user, String pass) {
		authenticate(user, pass);
		return webAccess.listEntities();
	}
	
	@Override
    public ArrayList<Long> list_A(String user, String pass, String entityName) {
		authenticate(user, pass);
		return webAccess.list(entityName);
    }

	@Override
    public ArrayList<String> read_A(String user, String pass, String entityName, Long id) {
		authenticate(user, pass);
		return webAccess.read(entityName, id);
    }

	@Override
    public void executeBusinessRule_A(String user, String pass, String entityName, Long id, String rulename) {
		authenticate(user, pass);
		webAccess.executeBusinessRule(entityName, id, rulename);
	}
}
