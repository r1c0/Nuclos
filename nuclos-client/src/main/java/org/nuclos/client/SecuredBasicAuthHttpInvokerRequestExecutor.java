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

import javax.annotation.PostConstruct;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodRetryHandler;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.log4j.Logger;
import org.nuclos.common.ApplicationProperties;
import org.nuclos.common2.LangUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.remoting.httpinvoker.CommonsHttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.HttpInvokerClientConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecuredBasicAuthHttpInvokerRequestExecutor extends CommonsHttpInvokerRequestExecutor {

	private static final Logger LOG = Logger.getLogger(SecuredBasicAuthHttpInvokerRequestExecutor.class);

	private HttpMethodRetryHandler retryHandler;
	
	private ApplicationProperties applicationProperties;
	
	static {
		LOG.info("Register CustomSecureProtocolSocketFactory for HTTPS");
		Protocol.registerProtocol("https", new Protocol("https", new CustomSecureProtocolSocketFactory(), 443));		
	}

	public SecuredBasicAuthHttpInvokerRequestExecutor() {
	}
	
	@Autowired
	void setApplicationProperties(ApplicationProperties applicationProperties) {
		this.applicationProperties = applicationProperties;
	}
	
	@PostConstruct
	final void init() {
		// timeout 30 minutes (requested for entity transfer)
		super.setReadTimeout(applicationProperties.isFunctionBlockDev() ? 0 : 1000 * 60 * 30);
	}

	@Override
	protected PostMethod createPostMethod(HttpInvokerClientConfiguration config) throws IOException {
		PostMethod postMethod = super.createPostMethod(config);

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if ((auth != null) && (auth.getName() != null)) {
			String base64 = auth.getName() + ":" + LangUtils.defaultIfNull(auth.getCredentials(), "");
			postMethod.setRequestHeader("Authorization", "Basic " + new String(Base64.encodeBase64(base64.getBytes())));
		}

		return postMethod;
	}

	@Override
	public void setHttpClient(HttpClient httpClient) {
		if (this.retryHandler != null) {
			httpClient.getParams().getDefaults().setParameter(HttpMethodParams.RETRY_HANDLER, retryHandler);
		}
		super.setHttpClient(httpClient);
	}

	public void setRetryHandler(HttpMethodRetryHandler retryHandler) {
		this.retryHandler = retryHandler;
		if (getHttpClient() != null) {
			getHttpClient().getParams().getDefaults().setParameter(HttpMethodParams.RETRY_HANDLER, retryHandler);
		}
	}
}
