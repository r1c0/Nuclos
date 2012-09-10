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

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.nuclos.api.context.SpringInputContext;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.Assert;

public class NuclosRemoteInvocationExecutor implements RemoteInvocationExecutor {

	private static final Logger LOG = Logger.getLogger(NuclosRemoteInvocationExecutor.class);
	
	/**
	 * Whether to turn INFO logging of profiling data on.
	 */
	private static final boolean PROFILE = true;
	
	/**
	 * Minimum delay for calls in order to appear in LOG. 
	 */
	private static final long PROFILE_MIN_MS = 200L;
	
	/**
	 * Spring injected.
	 */
	private SpringInputContext inputContext;
	
	/**
	 * Spring injected.
	 */
	private NuclosUserDetailsContextHolder userContext;
	
	/**
	 * Spring injected.
	 */
	private NuclosRemoteContextHolder remoteContext;
	
	/**
	 * Spring injected.
	 */
	private PlatformTransactionManager txManager;
	
	public NuclosRemoteInvocationExecutor() {
	}
	
	/**
	 * Spring injected.
	 */
	public void setInputContext(SpringInputContext inputContext) {
		this.inputContext = inputContext; 
	}
	
	/**
	 * Spring injected.
	 */
	final SpringInputContext getInputContext() {
		return inputContext;
	}
	
	/**
	 * Spring injected.
	 */
	public void setNuclosRemoteContextHolder(NuclosRemoteContextHolder remoteContext) {
		this.remoteContext = remoteContext;
	}
	
	/**
	 * Spring injected.
	 */
	public void setNuclosUserDetailsContextHolder(NuclosUserDetailsContextHolder userContext) {
		this.userContext = userContext;
	}
	
	/**
	 * Spring injected.
	 */
	public void setPlatformTransactionManager(PlatformTransactionManager txManager) {
		Assert.notNull(txManager);
		this.txManager = txManager;
	}
	
	@Override
	public Object invoke(RemoteInvocation invoke, Object param) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		final DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("nuclosRemoveInvocationTxDef");
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		final TransactionStatus tx = txManager.getTransaction(def);
		
		long before = 0L, after = 0L;
		try {
			userContext.setTimeZone((TimeZone) invoke.getAttribute("user.timezone"));
			remoteContext.setRemotly(true);
			final SpringInputContext inputContext = getInputContext();

			if (invoke.getAttribute("org.nuclos.api.context.InputContextSupported") != null) {
				Object o = invoke.getAttribute("org.nuclos.api.context.InputContextSupported");
				if (o instanceof Boolean) {
					inputContext.setSupported(((Boolean) o).booleanValue());
				}
			}
			if (invoke.getAttribute("org.nuclos.api.context.InputContext") != null) {
				final Map<String, Serializable> context = (Map<String, Serializable>) 
						invoke.getAttribute("org.nuclos.api.context.InputContext");
				if (LOG.isDebugEnabled()) {
					LOG.debug("Receiving call with dynamic context:");
					for (Map.Entry<String, Serializable> entry : context.entrySet()) {
						LOG.debug(entry.getKey() + ": " + String.valueOf(entry.getValue()));
					}
				}
				inputContext.set(context);
			}
			
			if (PROFILE) {
				before = System.currentTimeMillis();
			}
			
			final Object result = invoke.invoke(param);
			
			if (PROFILE) {
				after = System.currentTimeMillis();
			}
			
			if (tx.isRollbackOnly()) {
				LOG.warn("Transaction is marked for rollback-only, rolling back now: " + invoke + "(" + param + "): " + result);
				txManager.rollback(tx);
			}
			else {
				txManager.commit(tx);
			}			
			return result;
		} 
		catch (InvocationTargetException e) {
			LOG.warn("Transaction rolling back now, because of " + e.getTargetException() + ": " + invoke + "(" + param + ")");
			txManager.rollback(tx);
			throw e;
		}
		catch (RuntimeException e) {
			LOG.warn("Transaction rolling back now, because of unexpected " + e + ": " + invoke + "(" + param + ")", e);
			txManager.rollback(tx);
			throw e;
		}
		finally {
			userContext.clear();
			remoteContext.clear();
			inputContext.clear();
			
			if (PROFILE) {
				final long call = after - before;
				if (call >= PROFILE_MIN_MS) {
					final long now = System.currentTimeMillis();
					LOG.info("client invocation of " + invoke + " on " + param + " took " + (now - before) 
							+ " (" + call + ") ms");
				}
			}
		}
	}
	
	public synchronized void destroy() {
		inputContext.destroy();
		inputContext = null;
	}

}
