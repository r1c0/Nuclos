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
package org.nuclos.server.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.nuclos.common.AbstractParameterProvider;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.server.dal.processor.ProcessorFactorySingleton;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.jms.NuclosJMSUtils;
import org.nuclos.server.mbean.MBeanAgent;
import org.nuclos.server.mbean.ServerParameterProviderMBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * <code>ParameterProvider</code> for the server side.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph Radig</a>
 * @version 00.01.000
 */
public class ServerParameterProvider extends AbstractParameterProvider implements ServerParameterProviderMBean, InitializingBean {

	//private static final Logger log = Logger.getLogger(ServerParameterProvider.class);

	/**
	 * Map<String sName, String sValue>
	 */
	private Map<String, String> mpParameters;

	//private final ClientNotifier clientnotifier = new ClientNotifier(JMSConstants.TOPICNAME_PARAMETERPROVIDER);

	public static synchronized ServerParameterProvider getInstance() {
		return (ServerParameterProvider) SpringApplicationContextHolder.getBean("parameterProvider");
	}

	protected ServerParameterProvider() { }

	@Override
	public void afterPropertiesSet() throws Exception {
		this.mpParameters = this.loadParameters();
		MBeanAgent.registerConfiguration(this, ServerParameterProviderMBean.class);
	}

	/**
	 * @return Map<String sName, String sValue>
	 */
	@Override
	public synchronized Map<String, String> getAllParameters() {
		return Collections.unmodifiableMap(this.mpParameters);
	}

	@Override
	public synchronized String getValue(String sParameter) {
		return mpParameters.get(sParameter);
	}

	/**
	 * gets all parameters
	 * @return Map<String name, String value>
	 */
	private Map<String,String> loadParameters(){
		Map<String,String> rawParameters = new HashMap<String, String>();
		Map<String, String> properties = getParameterDefaults();
		if (properties != null)
			rawParameters.putAll(properties);
		rawParameters.putAll(getParametersFromDB());

		Map<String,String> mpParameters = new HashMap<String,String>();
		for(String sParameter : rawParameters.keySet()){
			String value = rawParameters.get(sParameter);
			if(value != null && !value.isEmpty())
				mpParameters.put(sParameter, replaceParameter(rawParameters, value));
			else
				mpParameters.put(sParameter, null);

			String ovr = System.getProperty("override." + sParameter.replace(' ', '_'));
			if(ovr != null)
				mpParameters.put(sParameter, ovr);
		}
		return mpParameters;
	}

	/**
	 * gets all parameter entries from the database.
	 * @return Map<String name, String value>
	 */
	private static Map<String, String> getParametersFromDB() {
		final Logger log = Logger.getLogger(ServerParameterProvider.class);
		log.debug("START building parameter cache.");

		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom t = query.from("T_AD_PARAMETER").alias(ProcessorFactorySingleton.BASE_ALIAS);
		query.multiselect(t.baseColumn("STRPARAMETER", String.class), t.baseColumn("STRVALUE", String.class));

		Map<String, String> result = new HashMap<String, String>();
		for (DbTuple tuple : DataBaseHelper.getDbAccess().executeQuery(query)) {
			result.put(tuple.get(0, String.class), tuple.get(1, String.class));
		}

		log.debug("FINISHED building parameter cache.");
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Map<String, String> getParameterDefaults() {
		Properties defaults = new Properties();
		InputStream is = ServerParameterProvider.class.getClassLoader().getResourceAsStream("resources/parameter-defaults.properties");
		if (is == null) {
			throw new NuclosFatalException("Missing server resource parameter-defaults.properties");
		}
		try {
			defaults.load(is);
			is.close();
		} catch (IOException ex) {
			throw new NuclosFatalException("Error loading server resource parameter-defaults.properties", ex);
		}
		return (Map) defaults;
	}

	/**
	 * replace the user defined pattern with the parameter values
	 * @param mpParameters
	 * @param sParameterValue
	 * @return
	 */
    private String replaceParameter(Map<String, String> mpParameters,String sParameterValue) {
       int sidx = 0;
       while ((sidx = sParameterValue.indexOf("${", sidx)) >= 0) {
           int eidx = sParameterValue.indexOf("}", sidx);
           String sParameter = sParameterValue.substring(sidx + 2, eidx);

           String rep = findReplacement(mpParameters, sParameter);
           sParameterValue = sParameterValue.substring(0, sidx) + rep + sParameterValue.substring(eidx + 1);
           if (rep != null) // nullpointer
         	  sidx = sidx + rep.length();
      }
       // check if there is more than one variable to replace (e.g. ${NuclosCodeGenerator Output Path}/wsdl), if so, do it again
       if (sParameterValue.indexOf("${") != -1)
      	 sParameterValue = replaceParameter(mpParameters, sParameterValue);

      return sParameterValue;
  }

	/**
	 * replace a single parameter pattern with the value
	 * @param sParameter
	 * @param mpParameter
	 * @return parameter value or <code>IllegalArgumentException</code> if parameter has no value
	 */
    private String findReplacement(Map<String, String> mpParameters, String sParameter) {

       if(mpParameters.containsKey(sParameter))
    	   return mpParameters.get(sParameter);
       else if(getSystemParameters().containsKey(sParameter))
    	   return getSystemParameters().get(sParameter);
       else
    	   throw new IllegalArgumentException("sParameter");

    }

    public synchronized void revalidate() {
   	 this.mpParameters = this.loadParameters();
   	 this.notifyClients(JMS_MESSAGE_ALL_PARAMETERS_ARE_REVALIDATED);
    }

    private synchronized void notifyClients(String sMessage) {
    	NuclosJMSUtils.sendMessage(JMS_MESSAGE_ALL_PARAMETERS_ARE_REVALIDATED, JMSConstants.TOPICNAME_PARAMETERPROVIDER);
    }

    /**
     * @return current system environment
     */
    @Override
		public synchronized Map<String,String> getSystemParameters(){
			return System.getenv();
		}

		@Override
		public synchronized Collection<String> getParameterNames() {
			return this.mpParameters.keySet();
		}

		@Override
		protected void finalize() throws Throwable {
			//this.clientnotifier.close();
			super.finalize();
		}
}	// class ServerParameterProvider
