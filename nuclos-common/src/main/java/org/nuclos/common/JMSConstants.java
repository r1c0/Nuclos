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
package org.nuclos.common;

/**
 * Constants for JMS Topic names.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Lars.Rueckemann@novabit.de">Lars Rueckemann</a>
 * @version 01.00.00
 */
public interface JMSConstants {

	/**
	 * The intra-JVM ConnectionFactory to be used for JMS. Note that this JNDI name is JBoss specific.
	 * Requires that the EJB Container and the JMS provider are located inside the same JVM. If not
	 * use a different factory by specifying its JNDI name here.
	 */
	public static final String CONNECTIONFACTORY_JNDINAME = "ConnectionFactory";

	/**
	 * name of the JMS topic for client notification from rules.
	 */
	public static final String TOPICNAME_RULENOTIFICATION = "topic.ruleNotification";

	/**
	 * name of the JMS topic for client notification of master data changes.
	 */
	public static final String TOPICNAME_MASTERDATACACHE = "topic.masterdataCache";

	/**
	 * name of the JMS topic for client notification of meta data changes.
	 */
	public static final String TOPICNAME_METADATACACHE = "topic.metadataCache";

	/**
	 * name of the JMS topic for client notification of security cache changes.
	 */
	public static final String TOPICNAME_SECURITYCACHE = "topic.securityCache";

	/**
	 * name of the JMS topic for client notification of searchfilter cache changes.
	 */
	public static final String TOPICNAME_SEARCHFILTERCACHE = "topic.searchfilterCache";

	/**
	 * name of the JMS topic for client notification of resource cache changes.
	 */
	public static final String TOPICNAME_RESOURCECACHE = "topic.resourceCache";

	/**
	 * name of the JMS topic for client notification of master data changes.
	 */
	public static final String TOPICNAME_PARAMETERPROVIDER = "topic.parameterProvider";

	/**
	 * name of the JMS topic for client notification of localization changes.
	 */
	public static final String TOPICNAME_LOCALE = "topic.localizationChanges";

	/**
	 * name of the JMS topic for client notification of state model changes.
	 */
	public static final String TOPICNAME_STATEMODEL = "topic.statemodel";

	/**
	 * name of the JMS topic for client notification of state model changes.
	 */
	public static final String TOPICNAME_PROGRESSNOTIFICATION = "topic.progressNotification";
	
	/**
	 * name of the JMS topic for client notification on locked tabs.
	 */
	public static final String TOPICNAME_LOCKEDTABPROGRESSNOTIFICATION = "topic.lockedTabProgressNotification";

	/**
	 * Alias which is used to send a message to all logged in users
	 */
	public static final String BROADCAST_MESSAGE = "ALL_USERS";

	/**
	 * name of the JMS topic for client notification of custom component changes.
	 */
	public static final String TOPICNAME_CUSTOMCOMPONENTCACHE = "topic.customcomponentCache";

}
