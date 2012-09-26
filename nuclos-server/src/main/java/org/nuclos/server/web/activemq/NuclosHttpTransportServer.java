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
package org.nuclos.server.web.activemq;

import java.net.InetSocketAddress;
import java.net.URI;

import org.apache.activemq.command.BrokerInfo;
import org.apache.activemq.transport.TransportServerSupport;
import org.apache.activemq.transport.http.HttpTransportFactory;
import org.apache.activemq.transport.util.TextWireFormat;
import org.apache.activemq.transport.xstream.XStreamWireFormat;
import org.apache.activemq.util.ServiceStopper;

/**
 * A dummy TransportServerSupport implementation for ActiveMQ to prevent dependencies 
 * to jetty.
 * 
 * @author Thomas Pasch
 */
public class NuclosHttpTransportServer extends TransportServerSupport {

	// copied from HttpTransportServer
    private URI bindAddress;
    private TextWireFormat wireFormat;
    private HttpTransportFactory transportFactory;
	// end of copied from HttpTransportServer

	public NuclosHttpTransportServer(URI uri, HttpTransportFactory factory) {
		// super(uri, factory);
        super(uri);
        this.bindAddress = uri;
        this.transportFactory = factory;
	}

	// copied from HttpTransportServer
	
    // Properties
    // -------------------------------------------------------------------------
    public TextWireFormat getWireFormat() {
        if (wireFormat == null) {
            wireFormat = createWireFormat();
        }
        return wireFormat;
    }

    public void setWireFormat(TextWireFormat wireFormat) {
        this.wireFormat = wireFormat;
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    protected TextWireFormat createWireFormat() {
        return new XStreamWireFormat();
    }

	// end of copied from HttpTransportServer
	
	@Override
	public void setBrokerInfo(BrokerInfo brokerInfo) {
		// do nothing
	}

	@Override
	protected void doStart() throws Exception {
		// do nothing
	}

	@Override
	public InetSocketAddress getSocketAddress() {
		// return null as HttpTransportServer does
		return null;
	}

	@Override
	protected void doStop(ServiceStopper stopper) throws Exception {
		// do nothing
	}

}
