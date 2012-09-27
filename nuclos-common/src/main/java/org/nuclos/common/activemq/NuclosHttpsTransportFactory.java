//Copyright (C) 2012  Novabit Informationssysteme GmbH
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
package org.nuclos.common.activemq;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.activemq.transport.Transport;
import org.apache.activemq.transport.https.HttpsClientTransport;
import org.apache.activemq.transport.https.HttpsTransportFactory;
import org.apache.activemq.wireformat.WireFormat;
import org.apache.http.client.HttpClient;
import org.nuclos.common.http.NuclosHttpClientFactory;

/**
 * A nuclos custom extension to ActiveMQ {@link HttpsTransportFactory}.
 * <p>
 * This factory is bound to ActiveMQ scheme 'myhttps' in server and client (i.e.
 * in common). It uses the (apache httpcomponents) HttpClient instance in the 
 * Spring context in the client.
 * </p>
 * @author Thomas Pasch
 */
public class NuclosHttpsTransportFactory extends HttpsTransportFactory {
	
	private NuclosHttpClientFactory factory;

	private HttpClient httpClient;
	
	public NuclosHttpsTransportFactory() {
	}
	
	final HttpClient getHttpClient() {
		if (httpClient == null) {
			// Use a separate HttpClient here because ActiveMQ will
			// always set connection timeout to 30000 (30 seconds). (tp)
			
			// httpClient = NuclosHttpClientFactory.getInstance().getObject();
			factory = new NuclosHttpClientFactory();
			httpClient = factory.getObject();
		}
		return httpClient;
	}
	
	@Override
    protected Transport createTransport(URI location, WireFormat wf) throws MalformedURLException {
		if (location.getScheme().equals("myhttps")) {
			try {
				location = new URI(location.toString().substring(2));
			}
			catch (URISyntaxException e) {
				throw new MalformedURLException(e.toString());
			}
		}
        final HttpsClientTransport result = (HttpsClientTransport) super.createTransport(location, wf);
        final HttpClient httpClient = getHttpClient();
        result.setReceiveHttpClient(httpClient);
        result.setSendHttpClient(httpClient);
        return result;
    }
	
	@Override
	public void finalize() {
		if (factory != null) {
			factory.close();
		}
		factory = null;
		httpClient = null;
	}
	
}
