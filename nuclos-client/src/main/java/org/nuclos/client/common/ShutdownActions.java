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

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Handler for shutdown actions.
 * The JDK's Runtime.addShutdownHook() lacks the possibility to define the order in which shutdown hooks
 * are executed. This class tries to help out.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class ShutdownActions {

	private static ShutdownActions singleton;

	/**
	 * shutdown order: unsubscribe jms queues
	 */
	public static final int SHUTDOWNORDER_JMS_QUEUES = 97;
	/**
	 * shutdown order: unsubscribe jms topics
	 */
	public static final int SHUTDOWNORDER_JMS_TOPICS = 98;

	/**
	 * shutdown order: save preferences
	 */
	public static final int SHUTDOWNORDER_SAVEPREFERENCES = 99;
	/**
	 * shutdown order: logout - this should be the last action
	 */
	public static final int SHUTDOWNORDER_LOGOUT = 100;

	/**
	 * SortedMap<Integer, Runnable>: contains <code>Runnable</code>s to be executed on system shutdown in the given order.
	 */
	private final SortedMap<Integer, Runnable> mpShutdownActions = new TreeMap<Integer, Runnable>();

	private ShutdownActions() {
		this.registerShutdownHook();
	}

	public static synchronized ShutdownActions getInstance() {
		if (singleton == null) {
			singleton = new ShutdownActions();
		}
		return singleton;
	}

	/**
	 * registers the given <code>runnable</code> to be executed on shutdown
	 * @param iOrder the order in which this runnable is executed. Lower numbers are executed before higher numbers.
	 * Please define a symbolic constant in Main when you add a new shutdown action.
	 * @param runnable the <code>Runnable</code> to be executed. May throw <code>RuntimeException</code>s.
	 */
	public synchronized void registerShutdownAction(int iOrder, Runnable runnable) {
		if (this.mpShutdownActions.containsKey(iOrder)) {
			throw new IllegalStateException("Slot no. " + iOrder + " isn't available in the list of shutdown actions.");
		}
		this.mpShutdownActions.put(iOrder, runnable);
	}

	/**
	 * registers the shutdown hook that executes the shutdown actions registered with <code>registerShutdownAction</code>
	 * in the order given there.
	 */
	private void registerShutdownHook() {
		final Runnable runnableHook = new Runnable() {
			@Override
			public void run() {
				for (Integer iOrder : mpShutdownActions.keySet()) {
					final Runnable runnable = mpShutdownActions.get(iOrder);
					try {
						runnable.run();
					}
					catch (Exception ex) {
						// Note that we don't use log4j here because we're somewhat paranoid:
						System.err.print("Exception occured in shutdown hook: ");
						ex.printStackTrace();
					}
				}
			}
		};
		Runtime.getRuntime().addShutdownHook(new Thread(runnableHook));
	}

	public synchronized boolean isRegistered(int iOrder) {
		return this.mpShutdownActions.containsKey(iOrder);
	}

}	// class ShutdownActions
