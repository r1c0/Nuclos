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
/*
 * Created on 08.11.2005
 *
 */
package org.nuclos.client.ui;

import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

/**
 * Central class (singleton) for the multithreading framework - builds threads - handles progress information
 * ensures threadsafety
 *
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:florian.speidel@novabit.de">florian.speidel</a>
 * @author	<a href="mailto:rostislav.maksymovskyi@novabit.de">rostislav.maksymovskyi</a>
 * @version 02.00.00
 */
public class CommonMultiThreader {
	
	private static final Logger LOG	 = Logger.getLogger(CommonMultiThreader.class);

	/**
	 * for statistic use, shutdown stuff, ...
	 */
	private Vector<WorkerThread> currentExecutions = new Vector<WorkerThread>();

	private static CommonMultiThreader instance = null;

	final ExecutorService cachedThreadPoolExecutor = Executors.newCachedThreadPool();
	
	/**
	 * 
	 */
	private CommonMultiThreader() {
		//use getInstance() !
	}

	public static CommonMultiThreader getInstance() {
		if (instance == null) {
			instance = new CommonMultiThreader();
		}
		return instance;
	}

	/**
	 * @param worker see CommonClientWorker
	 */
	public Future<?> executeInterruptible(CommonClientWorker worker) {
		try {
			worker.init();
		}
		catch (final Exception ex) {
			worker.handleError(ex);
			return null;
		}

		WorkerThread t = new WorkerThread(worker);	
		Future<?> future = cachedThreadPoolExecutor.submit(t);

		return future;
	}
	
	/**
	 * Starts work() in a dedicated thread - after this calls paint() in the event dispatcher
	 * Threadsafe! Can be called out of the event dispatcher or another thread.
	 * @param worker see CommonClientWorker
	 */
	public WorkerThread execute(CommonClientWorker worker) {
		try {
			worker.init();
		}
		catch (final Exception ex) {
			worker.handleError(ex);
			return null;
		}

		WorkerThread t = new WorkerThread(worker);
		//UIUtils.invokeOnDispatchThread(t);
		t.start();
		
		return t;
	}

	/**
	 * 
	 * Self synchronizing Thread (sync source object 'currentExecutions')
	 * 
	 * <br>Created by Novabit Informationssysteme GmbH
	 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 *
	 * @author	<a href="mailto:florian.speidel@novabit.de">florian.speidel</a>
	 */
	private class WorkerThread extends Thread {

		private CommonClientWorker worker = null;

		private WorkerThread(CommonClientWorker worker) {
			this.worker = worker;
		}

		@Override
		public void run() {
			//for getting control over running workers
			synchronized (currentExecutions) {
				currentExecutions.add(this);
			}

			//Ok lets do some work
			try {
				worker.work();
			}
			catch (final Exception ex) {
				//painting on screen -> event dispatcher
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {
							worker.handleError(ex);
						}
						catch (Exception e) {
							LOG.error("WorkerThread failed: " + e, e);
						}
					}
				});
			}
			finally {
				//show results...
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {

						try {
							worker.paint();
						}
						catch (Exception ex) {
							worker.handleError(ex);
						}
					}
				});
				synchronized (currentExecutions) {
					currentExecutions.remove(this);
				}
			}
		}
	}

	public synchronized Vector<WorkerThread> getCurrentExecutions() {
		return currentExecutions;
	}
}
