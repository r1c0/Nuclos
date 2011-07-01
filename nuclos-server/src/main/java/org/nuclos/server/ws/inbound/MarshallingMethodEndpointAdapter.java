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
package org.nuclos.server.ws.inbound;

import org.apache.log4j.Logger;
import org.springframework.oxm.Marshaller;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MethodEndpoint;

@SuppressWarnings("deprecation")
public class MarshallingMethodEndpointAdapter extends org.springframework.ws.server.endpoint.adapter.MarshallingMethodEndpointAdapter {

	private static final Logger log = Logger.getLogger(MarshallingMethodEndpointAdapter.class);

	public MarshallingMethodEndpointAdapter(Marshaller marshaller) {
		super(marshaller);
	}

	@Override
	protected void invokeInternal(MessageContext messageContext,
		MethodEndpoint methodEndpoint) throws Exception {
		try {
			super.invokeInternal(messageContext, methodEndpoint);
		}
		catch (Exception ex) {
			log.error("Nuclos Webservice Error", ex);
			throw ex;
		}
	}
}
