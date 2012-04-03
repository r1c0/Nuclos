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
 * @author Lars Rueckemann
 * @author Thomas Pasch (javadoc improvements)
 * @version 01.00.00
 */
public interface JMSConstants {
	
	// JNDI

	/**
	 * The intra-JVM ConnectionFactory to be used for JMS. Note that this JNDI name is JBoss specific.
	 * Requires that the EJB Container and the JMS provider are located inside the same JVM. If not
	 * use a different factory by specifying its JNDI name here.
	 */
	public static final String CONNECTIONFACTORY_JNDINAME = "ConnectionFactory";
	
	// TOPICS

	/**
	 * name of the JMS topic for client notification from rules.
	 * <dl>
	 * 		<dt>Sender</dt><dd>
	 * 			Server: ConsoleFacadeBean.killSession (to selected user), 
	 * 			ConsoleFacadeBean.sendClientNotification (to selected user),
	 * 			and RuleEngineFacadeBean.sendMessagesByJMS (to current user).</dd>
	 * 		<dt>Cause</dt><dd>
	 * 			Admin console, admin console, (unignored) exception in rule execution.</dd>
	 * 		<dt>Message</dt><dd>
	 * 			CommandMessage.CMD_SHUTDOWN, RuleNotification, RuleNotification.</dd>
	 * 		<dt>Receiver</dt><dd>
	 * 			Client: {@link org.nuclos.client.main.MainController#handleMessage(Message)}</dd>
	 * 		<dt>Action triggered</dt><dd>
	 * 			CommandMessage.CMD_SHUTDOWN will shutdown client. 
	 * 			RuleNotification adds a message to client message panel (priority could be set).
	 * 			Client could also handle CommandInformationMessage, but I'm not sure if it ever send.</dd>
	 * </dl>
	 */
	public static final String TOPICNAME_RULENOTIFICATION = "topic.ruleNotification";

	/**
	 * name of the JMS topic for client notification of master data changes.
	 * <dl>
	 * 		<dt>Sender</dt><dd>
	 * 			Server: {@link org.nuclos.server.masterdata.ejb3.MasterDataFacadeHelper#notifyClients(String)}</dd>
	 * 		<dt>Cause</dt><dd>
	 * 			{@link org.nuclos.server.masterdata.ejb3.MasterDataFacadeHelper#entityChanged(MasterDataMetaVO, MasterDataVO)} or
	 * 			{@link org.nuclos.server.masterdata.ejb3.MasterDataFacadeBean#notifyClients(String)} or
	 * 			{@link org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal#notifyClients(String)}</dd>
	 * 		<dt>Message</dt><dd>
	 * 			TextMessage: name of entity to invalidate or <code>null</code> for all entities.</dd>
	 * 		<dt>Receiver</dt><dd>
	 * 			Client: {@link org.nuclos.client.masterdata.MasterDataCache#messagelistener}</dd>
	 * 		<dt>Action triggered</dt><dd>
	 * 			{@link org.nuclos.client.masterdata.MasterDataCache#invalidate(String)}</dd>
	 * </dl>
	 */
	public static final String TOPICNAME_MASTERDATACACHE = "topic.masterdataCache";

	/**
	 * name of the JMS topic for client notification of meta data changes.
	 * <dl>
	 * 		<dt>Sender</dt><dd>
	 * 			Server: {@link org.nuclos.server.masterdata.ejb3.MasterDataFacadeBean#notifyClients(NuclosEntity)}</dd>
	 * 		<dt>Cause</dt><dd>
	 * 			Triggered from import layouts and changes/new master data or from
	 * 			{@link org.nuclos.server.common.MetaDataServerProvider#revalidate(boolean)}.</dd>
	 * 		<dt>Message</dt><dd>
	 * 			TextMessage: name of entity to reinvalidate. <code>null</code> is not allowed.</dd>
	 * 		<dt>Receiver</dt><dd>
	 * 			Client: MetaDataClientProvider and GenericObjectMetaDataCache and 
	 * 			MetaDataCache.
	 * 			Because there are several listeners, {@link org.nuclos.client.jms.MultiMessageListenerContainer}
	 * 			is used.</dd>
	 * 		<dt>Action triggered</dt><dd>
	 * 			{@link org.nuclos.client.common.MetaDataClientProvider#revalidate()}
	 * 			(message content is not honored) and
	 * 			{@link org.nuclos.client.genericobject.GenericObjectMetaDataCache#revalidate()}
	 * 			(message content is not honored) and
	 * 			{@link org.nuclos.client.main.MainController#refreshMenus()} (but only if entity is 
	 * 			<em>not</em> a dynamic entity or a layout).
	 * 			</dd>
	 * </dl>
	 */
	public static final String TOPICNAME_METADATACACHE = "topic.metadataCache";

	/**
	 * name of the JMS topic for client notification of security cache changes.
	 * <dl>
	 * 		<dt>Sender</dt><dd>
	 * 			Server: {@link org.nuclos.server.common.SecurityCache#notifyUser(String)}</dd>
	 * 		<dt>Cause</dt><dd>
	 * 			Triggered from {@link org.nuclos.server.common.SecurityCache#invalidate()}
	 * 			(all user case) or from {@link org.nuclos.server.common.SecurityCache#invalidate(String)}.</dd>
	 * 		<dt>Message</dt><dd>
	 * 			TextMessage: name of user to revalidate (SecurityCache, AttributeCache and refreshMenus) 
	 * 			or <code>null</code> for all users.</dd>
	 * 		<dt>Receiver</dt><dd>
	 * 			Client: {@link org.nuclos.client.common.security.SecurityCache#listener}</dd>
	 * 		<dt>Action triggered</dt><dd>
	 * 			{@link org.nuclos.client.main.MainController#refreshMenus()} and
	 * 			{@link org.nuclos.client.attribute.AttributeCache#revalidate()} and
	 * 			{@link org.nuclos.client.common.security.SecurityCache#revalidate()}.
	 * 			All actions are only triggered if the message was for all users or the logged-in user
	 * 			is the user from the message.
	 * 			</dd>
	 * </dl>
	 */
	public static final String TOPICNAME_SECURITYCACHE = "topic.securityCache";

	/**
	 * name of the JMS topic for client notification of searchfilter cache changes.
	 * <dl>
	 * 		<dt>Sender</dt><dd>
	 * 			Server: {@link org.nuclos.server.searchfilter.ejb3.SearchFilterFacadeBean#notifyClients(String[])}</dd>
	 * 		<dt>Cause</dt><dd>
	 * 			Triggered from 
	 * 			{@link org.nuclos.server.searchfilter.ejb3.SearchFilterFacadeBean#modify(String, MasterDataVO, DependantMasterDataMap, List<TranslationVO>)}
	 * 			.</dd>
	 * 		<dt>Message</dt><dd>
	 * 			Array of Strings (String[]) of all users that have access to a certain filter.</dd>
	 * 		<dt>Receiver</dt><dd>
	 * 			Client: {@link org.nuclos.client.searchfilter.SearchFilterCache#messagelistener}</dd>
	 * 		<dt>Action triggered</dt><dd>
	 * 			{@link org.nuclos.client.searchfilter.SearchFilterCache#validate()} (always) and
	 * 			{@link org.nuclos.client.main.MainController#refreshTaskController()} (but only if the logged-in
	 * 			user is not in the Array of Users from the message).
	 * 			</dd>
	 * </dl>
	 */
	public static final String TOPICNAME_SEARCHFILTERCACHE = "topic.searchfilterCache";

	/**
	 * name of the JMS topic for client notification of resource cache changes.
	 * <dl>
	 * 		<dt>Sender</dt><dd>
	 * 			Server: {@link org.nuclos.server.resource.ResourceCache#notifyClients()}</dd>
	 * 		<dt>Cause</dt><dd>
	 * 			Triggered from 
	 * 			{@link org.nuclos.server.resource.ResourceCache#invalidate()}
	 * 			.</dd>
	 * 		<dt>Message</dt><dd>
	 * 			Always <code>null</code>.</dd>
	 * 		<dt>Receiver</dt><dd>
	 * 			Client: {@link org.nuclos.client.resource.ResourceCache#messagelistener}</dd>
	 * 		<dt>Action triggered</dt><dd>
	 * 			{@link org.nuclos.client.resource.ResourceCache#invalidate()}.
	 * 			Message content is ignored.
	 * 			</dd>
	 * </dl>
	 */
	public static final String TOPICNAME_RESOURCECACHE = "topic.resourceCache";

	/**
	 * name of the JMS topic for client notification of master data changes.
	 * <dl>
	 * 		<dt>Sender</dt><dd>
	 * 			Server: {@link org.nuclos.server.common.ServerParameterProvider#notifyClients(String)}</dd>
	 * 		<dt>Cause</dt><dd>
	 * 			Triggered from 
	 * 			{@link org.nuclos.server.common.ServerParameterProvider#revalidate()}.</dd>
	 * 		<dt>Message</dt><dd>
	 * 			Always {@link org.nuclos.common.ParameterProvider#JMS_MESSAGE_ALL_PARAMETERS_ARE_REVALIDATED}.</dd>
	 * 		<dt>Receiver</dt><dd>
	 * 			Client: {@link org.nuclos.client.common.ClientParameterProvider#serverListener}</dd>
	 * 		<dt>Action triggered</dt><dd>
	 * 			{@link org.nuclos.client.common.ClientParameterProvider#revalidate()}.
	 * 			Message content is ignored.
	 * 			</dd>
	 * </dl>
	 */
	public static final String TOPICNAME_PARAMETERPROVIDER = "topic.parameterProvider";

	/**
	 * name of the JMS topic for client notification of localization changes.
	 * <dl>
	 * 		<dt>Sender</dt><dd>
	 * 			Server: {@link org.nuclos.server.common.ejb3.LocaleFacadeBean#internalFlush()} 
	 * 			(after commit)</dd>
	 * 		<dt>Cause</dt><dd>
	 * 			Triggered from 
	 * 			{@link org.nuclos.server.common.ejb3.LocaleFacadeBean} in many methods that change 
	 * 			the resources.</dd>
	 * 		<dt>Message</dt><dd>
	 * 			Always <code>null</code>.</dd>
	 * 		<dt>Receiver</dt><dd>
	 * 			Client: {@link org.nuclos.client.common.LocaleDelegate#onMessage(Message)}</dd>
	 * 		<dt>Action triggered</dt><dd>
	 * 			Reset resource bundle to <code>null</code>.
	 * 			Message content is ignored.
	 * 			</dd>
	 * </dl>
	 */
	public static final String TOPICNAME_LOCALE = "topic.localizationChanges";

	/**
	 * name of the JMS topic for client notification of state model changes.
	 * <dl>
	 * 		<dt>Sender</dt><dd>
	 * 			Server: {@link org.nuclos.server.statemodel.ejb3.StateFacadeBean#invalidateCache()}
	 * 			(after commit).</dd>
	 * 		<dt>Cause</dt><dd>
	 * 			Triggered from 
	 * 			{@link org.nuclos.client.statemodel.StateDelegate#invalidateCache()} (client!!!)
	 * 			out from 
	 * 			{@link org.nuclos.client.statemodel.admin.StateModelCollectController#saveStateModelAndUsages(StateModelEditor, StateModelVO)}
	 * 			.</dd>
	 * 		<dt>Message</dt><dd>
	 * 			Always <code>null</code>.</dd>
	 * 		<dt>Receiver</dt><dd>
	 * 			Client: {@link org.nuclos.client.statemodel.StateDelegate#closureCache}
	 * 			(Only instance of {@link org.nuclos.client.caching.JMSFlushingCache}.)</dd>
	 * 		<dt>Action triggered</dt><dd>
	 * 			{@link org.nuclos.client.statemodel.StateDelegate#getStatemodelClosure(Integer)} is reset
	 * 			(new cache provider). Message content is ignored.
	 * 			</dd>
	 * </dl>
	 */
	public static final String TOPICNAME_STATEMODEL = "topic.statemodel";

	/**
	 * name of the JMS topic for client notification of state model changes.
	 * <dl>
	 * 		<dt>Sender</dt><dd>
	 * 			Server: {@link org.nuclos.server.fileimport.ImportProgressNotifier#notify(ProgressNotification)}</dd>
	 * 		<dt>Cause</dt><dd>
	 * 			Triggered from 
	 * 			{@link org.nuclos.server.fileimport.ImportProgressNotifier}
	 * 			.</dd>
	 * 		<dt>Message</dt><dd>
	 * 			{@link org.nuclos.common.ProgressNotification}</dd>
	 * 		<dt>Receiver</dt><dd>
	 * 			Client: {@link org.nuclos.client.genericobject.GenericObjectImportCollectController#onMessage(Message)}
	 * 			(subscribed dynamically when inner state is DETAILSMODE_VIEW; unsubscribed from
	 * 			{@link org.nuclos.client.genericobject.GenericObjectImportCollectController#init().new CollectStateAdapter#detailsModeLeft(CollectStateEvent)}).</dd>
	 * 		<dt>Action triggered</dt><dd>
	 * 			Changes to {@link org.nuclos.client.genericobject.GenericObjectImportCollectController#progressBar}.
	 * 			</dd>
	 * </dl>
	 */
	public static final String TOPICNAME_PROGRESSNOTIFICATION = "topic.progressNotification";
	
	/**
	 * name of the JMS topic for client notification on locked tabs.
	 * <dl>
	 * 		<dt>Sender</dt><dd>
	 * 			Server: {@link org.nuclos.server.common.LockedTabProgressNotifier#notify(String, Integer)}</dd>
	 * 		<dt>Cause</dt><dd>
	 * 			Triggered from 
	 * 			{@link org.nuclos.server.dbtransfer.TransferFacadeBean} (several methods) and from
	 * 			{@link org.nuclos.server.dbtransfer.TransferNotifierHelper#notifyNextStep()}.
	 * 			.</dd>
	 * 		<dt>Message</dt><dd>
	 * 			{@link org.nuclos.common.LockedTabProgressNotification}</dd>
	 * 		<dt>Receiver</dt><dd>
	 * 			Client: {@link org.nuclos.client.main.mainframe.MainFrameTab.TranslucentLockableWithProgressUI#onMessage(Message)}
	 * 			((un)subscribed dynamically from
	 * 			{@link org.nuclos.client.main.mainframe.MainFrameTab.TranslucentLockableWithProgressUI#setLocked(boolean)}).</dd>
	 * 		<dt>Action triggered</dt><dd>
	 * 			{@link org.nuclos.client.main.mainframe.MainFrameTab.TranslucentLockableWithProgressUI#setText(String, int)}.
	 * 			</dd>
	 * </dl>
	 */
	public static final String TOPICNAME_LOCKEDTABPROGRESSNOTIFICATION = "topic.lockedTabProgressNotification";

	/**
	 * name of the JMS topic for client notification of custom component changes.
	 * <dl>
	 * 		<dt>Sender</dt><dd>
	 * 			Server: {@link org.nuclos.server.customcomp.ejb3.CustomComponentFacadeBean#notifyClients()} and
	 * 			{@link org.nuclos.server.dbtransfer.TransferFacadeBean#revalidateCaches()}</dd>
	 * 		<dt>Cause</dt><dd>
	 * 			Triggered from 
	 * 			{@link org.nuclos.server.customcomp.ejb3.CustomComponentFacadeBean} (several methods) and from
	 * 			end of {@link org.nuclos.server.dbtransfer.TransferFacadeBean#runTransfer(Transfer)}.
	 * 			.</dd>
	 * 		<dt>Message</dt><dd>
	 * 			Always <code>null</code>.</dd>
	 * 		<dt>Receiver</dt><dd>
	 * 			Client: {@link org.nuclos.client.customcomp.CustomComponentCache#messagelistener}.</dd>
	 * 		<dt>Action triggered</dt><dd>
	 * 			{@link org.nuclos.client.customcomp.CustomComponentCache#revalidate()} and
	 * 			{@link org.nuclos.client.main.MainController#refreshMenus()}.
	 * 			Message content is ignored.</dd>
	 * </dl>
	 */
	public static final String TOPICNAME_CUSTOMCOMPONENTCACHE = "topic.customcomponentCache";

	/**
	 * Name of the JMS topic for sending a heartbeat to the client in order to avoid
	 * socket connection reset.
	 * <dl>
	 * 		<dt>Sender</dt><dd>
	 * 			Server: {@link org.nuclos.server.web.NuclosContextLoaderListener#contextInitialized(ServletContextEvent)}
	 * 			</dd>
	 * 		<dt>Cause</dt><dd>
	 *			Interval triggered by a timer.</dd>
	 * 		<dt>Message</dt><dd>
	 * 			An (incremented) Integer (no meaning).</dd>
	 * 		<dt>Receiver</dt><dd>
	 * 			Client: {@link org.nuclos.client.jms.TopicNotificationReceiver.HeartBeatMessageListener}.</dd>
	 * 		<dt>Action triggered</dt><dd>
	 * 			Nothing. It is just to ensure that the connection is still valid.
	 * 			Message content is ignored.</dd>
	 * </dl>
	 */
	public static final String TOPICNAME_HEARTBEAT = "topic.heartBeat";
	
}
