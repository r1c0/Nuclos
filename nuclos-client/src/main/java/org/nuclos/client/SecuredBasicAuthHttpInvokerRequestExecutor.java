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
package org.nuclos.client;

import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.log4j.Logger;
import org.nuclos.common.ApplicationProperties;
import org.springframework.remoting.httpinvoker.CommonsHttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.HttpInvokerClientConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecuredBasicAuthHttpInvokerRequestExecutor extends CommonsHttpInvokerRequestExecutor {

	private static final Logger log = Logger.getLogger(SecuredBasicAuthHttpInvokerRequestExecutor.class);

	@SuppressWarnings("deprecation")
	public SecuredBasicAuthHttpInvokerRequestExecutor() {
		log.info("Register CustomSecureProtocolSocketFactory for HTTPS");
		Protocol.registerProtocol("https", new Protocol("https", new CustomSecureProtocolSocketFactory(), 443));

		super.setReadTimeout(ApplicationProperties.getInstance().isFunctionBlockDev() ? 0 : 1000 * 60 * 3);
	}

	@Override
	protected PostMethod createPostMethod(HttpInvokerClientConfiguration config) throws IOException {
		PostMethod postMethod = super.createPostMethod(config);

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if ((auth != null) && (auth.getName() != null) && (auth.getCredentials() != null)) {
			String base64 = auth.getName() + ":" + auth.getCredentials().toString();
			postMethod.setRequestHeader("Authorization", "Basic " + new String(Base64.encodeBase64(base64.getBytes())));
		}

		return postMethod;
	}

}
