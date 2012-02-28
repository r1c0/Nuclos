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
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.nuclos.common.AbstractParameterProvider;
import org.nuclos.common.JMSConstants;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonRemoteException;
import org.nuclos.server.common.ejb3.ParameterFacadeRemote;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * <code>ParameterProvider</code> for the client side.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
@Component("parameterProvider")
public class ClientParameterProvider extends AbstractParameterProvider implements MessageListener, InitializingBean {
	
	private static final Logger LOG = Logger.getLogger(ClientParameterProvider.class);
	
	private static ClientParameterProvider INSTANCE;
	
	//

	private final MessageListener serverListener = new MessageListener() {
		@Override
		public void onMessage(Message message) {
			TextMessage txtMessage = (TextMessage) message;
			try {
				LOG.debug("serverListener.onMessage(..) " + txtMessage.getText());
				if (JMS_MESSAGE_ALL_PARAMETERS_ARE_REVALIDATED.equals(txtMessage.getText())) {
					LOG.info("onMessage " + this + " pattern matches, revalidate cache...");
					ClientParameterProvider.this.revalidate();
				}
			}
			catch(JMSException e) {
				throw new CommonFatalException(e);
			}
		}
	};

	/**
	 * Map<String sName, String sValue>
	 */
	private final Map<String, String> mpParams = new ConcurrentHashMap<String, String>();
	
	private TopicNotificationReceiver tnr;
	
	private ParameterFacadeRemote parameterFacadeRemote;

	public static ClientParameterProvider getInstance() {
		// return (ClientParameterProvider) SpringApplicationContextHolder.getBean("parameterProvider");
		return INSTANCE;
	}

	/**
	 * Use getInstance() to get the one and only instance of this class.
	 */
	ClientParameterProvider() {
		INSTANCE = this;
	}
	
	@Autowired
	void setTopicNotificationReceiver(TopicNotificationReceiver tnr) {
		this.tnr = tnr;
	}
	
	@Autowired
	void setParameterService(ParameterFacadeRemote parameterFacadeRemote) {
		this.parameterFacadeRemote = parameterFacadeRemote;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		tnr.subscribe(JMSConstants.TOPICNAME_PARAMETERPROVIDER, serverListener);
		revalidate();
	}
	
	private void revalidate() {
		synchronized (mpParams) {
			mpParams.clear();
			mpParams.putAll(getParameterFromServer());
		}
		LOG.info("Revalidated cache " + this);
	}

	private Map<String, String> getParameterFromServer() {
		try {
			// ParameterFacadeRemote remote = serviceLocator.getFacade(ParameterFacadeRemote.class);
			// return remote.getParameters();
			return parameterFacadeRemote.getParameters();
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
	public String getValue(String sParameterName) {
		return mpParams.get(sParameterName);
	}

	public Map<String, String>getAllParameters() {
		return mpParams;
	}

	public Color getColorValue(String sParameterName, Color defaultColor) {
		Color result;
		try {
			String[] rgb = getValue(sParameterName).split(",");
			if (rgb.length == 3) {
				result = new Color(Integer.valueOf(rgb[0]),Integer.valueOf(rgb[1]),Integer.valueOf(rgb[2]));
			} else {
				result = defaultColor;
			}
		}
		catch (Exception ex) {
			LOG.warn("Parameter \"" + sParameterName + "\" cannot be retrieved from the parameter table.");
			result = defaultColor;
		}
		return result;
	}

	@Override
	public void onMessage(Message message) {
		TextMessage txtMessage = (TextMessage) message;
		try {
			LOG.debug("serverListener.onMessage(..) " + txtMessage.getText());
			if (JMS_MESSAGE_ALL_PARAMETERS_ARE_REVALIDATED.equals(txtMessage.getText())) {
				LOG.info("onMessage " + this + " pattern matches, clear cache...");
				ClientParameterProvider.this.revalidate();
			}
		}
		catch(JMSException e) {
			throw new CommonFatalException(e);
		}
	}

}	// class ClientParameterProvider
