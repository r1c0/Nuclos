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
package org.nuclos.client.remote.http;

import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.nuclos.common.ApplicationProperties;
import org.nuclos.common2.LangUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.httpinvoker.HttpComponentsHttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.HttpInvokerClientConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecuredBasicAuthHttpInvokerRequestExecutor extends HttpComponentsHttpInvokerRequestExecutor implements InitializingBean {

	private static final Logger LOG = Logger.getLogger(SecuredBasicAuthHttpInvokerRequestExecutor.class);

	//

	private HttpRequestRetryHandler retryHandler;
	
	// Spring injection
	
	private ApplicationProperties applicationProperties;
	
	// end of Spring injection
	
	public SecuredBasicAuthHttpInvokerRequestExecutor(HttpClient httpClient) {
		super(httpClient);
	}
	
	// @Autowired
	public final void setApplicationProperties(ApplicationProperties applicationProperties) {
		this.applicationProperties = applicationProperties;
	}
	
	// @PostConstruct
	public final void afterPropertiesSet() {
		// timeout disable for dev
		// timeout 30 minutes (requested for entity transfer)
		setReadTimeout(applicationProperties.isFunctionBlockDev() ? 0 : 1000 * 60 * 30);
	}

	@Override
	protected HttpPost createHttpPost(HttpInvokerClientConfiguration config) throws IOException {
		HttpPost postMethod = super.createHttpPost(config);
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if ((auth != null) && (auth.getName() != null)) {
			String base64 = auth.getName() + ":" + LangUtils.defaultIfNull(auth.getCredentials(), "");
			postMethod.setHeader("Authorization", "Basic " + new String(Base64.encodeBase64(base64.getBytes())));
		}
		return postMethod;
	}

	@Override
	public void setHttpClient(HttpClient httpClient) {
		if (this.retryHandler != null) {
			((DefaultHttpClient) httpClient).setHttpRequestRetryHandler(retryHandler);
		}
		super.setHttpClient(httpClient);
	}

	public void setRetryHandler(HttpRequestRetryHandler retryHandler) {
		this.retryHandler = retryHandler;
		if (getHttpClient() != null) {
			((DefaultHttpClient) getHttpClient()).setHttpRequestRetryHandler(retryHandler);
		}
	}
	
	/**
	 * This overridden doExecuteRequest could be used for performance tracking of remote calls.
	 * It should commented out in production environments.
	 * 
	 * @author Thomas Pasch
	 * @since Nuclos 3.8
	 */
	/*
	@Override
	protected RemoteInvocationResult doExecuteRequest(
			HttpInvokerClientConfiguration config, ByteArrayOutputStream baos)
			throws IOException, ClassNotFoundException {

		long start = System.currentTimeMillis();
		
		HttpPost postMethod = createHttpPost(config);
		setRequestBody(config, postMethod, baos);
		HttpResponse response = executeHttpPost(config, getHttpClient(), postMethod);
		
		long reqLength = postMethod.getEntity().getContentLength();
		long exec = System.currentTimeMillis();
		
		validateResponse(config, response);
		InputStream responseBody = getResponseBody(config, response);
		RemoteInvocationResult result = readRemoteInvocationResult(responseBody, config.getCodebaseUrl());
		
		long respLength = response.getEntity().getContentLength();
		long stop = System.currentTimeMillis();
		
		LOG.info("request: " + reqLength + " response: " + respLength + " time: " + (stop - start) 
				+ " (" + (exec - start) + " + " + (stop - exec) + ")");
		return result;
	}
	 */

}
