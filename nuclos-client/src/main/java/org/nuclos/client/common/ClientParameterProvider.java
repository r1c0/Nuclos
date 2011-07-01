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
package org.nuclos.client.common;


import java.awt.Color;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.nuclos.common.AbstractParameterProvider;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonRemoteException;
import org.nuclos.server.common.ejb3.ParameterFacadeRemote;
import org.springframework.beans.factory.InitializingBean;


/**
 * <code>ParameterProvider</code> for the client side.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class ClientParameterProvider extends AbstractParameterProvider implements MessageListener, InitializingBean {
	private static final Logger log = Logger.getLogger(ClientParameterProvider.class);

	private final MessageListener serverListener = new MessageListener() {
		@Override
		public void onMessage(Message message) {
			TextMessage txtMessage = (TextMessage) message;
			try {
				log.debug("serverListener.onMessage(..) " + txtMessage.getText());
				if (JMS_MESSAGE_ALL_PARAMETERS_ARE_REVALIDATED.equals(txtMessage.getText()))
					ClientParameterProvider.this.revalidate();
			}
			catch(JMSException e) {
				throw new CommonFatalException(e);
			}
		}
	};

	/**
	 * Map<String sName, String sValue>
	 */
	private Map<String, String> mpParams;

	public static synchronized ClientParameterProvider getInstance() {
		return (ClientParameterProvider) SpringApplicationContextHolder.getBean("parameterProvider");
	}

	/**
	 * Use getInstance() to get the one and only instance of this class.
	 */
	protected ClientParameterProvider() {

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		TopicNotificationReceiver.subscribe(JMSConstants.TOPICNAME_PARAMETERPROVIDER, serverListener);
		this.mpParams = getParameterFromServer();
	}

	private void revalidate() {
		this.mpParams = getParameterFromServer();
	}

	private synchronized Map<String, String> getParameterFromServer() {
		try {

			ParameterFacadeRemote remote = ServiceLocator.getInstance().getFacade(ParameterFacadeRemote.class);
			return remote.getParameters();
		}
		catch (RuntimeException ex) {
			throw new CommonRemoteException(ex);
		}
	}

	/**
	 * @param sParameterName
	 * @return the value for the parameter with the given name.
	 */
	@Override
	public synchronized String getValue(String sParameterName) {
		return this.mpParams.get(sParameterName);
	}

	public synchronized Map<String, String>getAllParameters() {
		return this.mpParams;
	}

	public synchronized Color getColorValue(String sParameterName, Color defaultColor) {
		Color result;
		try {
			String[] rgb = this.getValue(sParameterName).split(",");
			if (rgb.length == 3) {
				result = new Color(Integer.valueOf(rgb[0]),Integer.valueOf(rgb[1]),Integer.valueOf(rgb[2]));
			} else {
				result = defaultColor;
			}
		}
		catch (Exception ex) {
			log.warn("Parameter \"" + sParameterName + "\" cannot be retrieved from the parameter table.");
			result = defaultColor;
		}
		return result;
	}

	@Override
	public void onMessage(Message message) {
		TextMessage txtMessage = (TextMessage) message;
		try {
			log.debug("serverListener.onMessage(..) " + txtMessage.getText());
			if (JMS_MESSAGE_ALL_PARAMETERS_ARE_REVALIDATED.equals(txtMessage.getText()))
				ClientParameterProvider.this.revalidate();
		}
		catch(JMSException e) {
			throw new CommonFatalException(e);
		}
	}

}	// class ClientParameterProvider
