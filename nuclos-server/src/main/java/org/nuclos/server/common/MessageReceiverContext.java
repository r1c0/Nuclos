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

import javax.annotation.PreDestroy;

import org.springframework.stereotype.Component;

public class MessageReceiverContext {
	
	private static MessageReceiverContext INSTANCE = new MessageReceiverContext();
	
	private ThreadLocal<Integer> threadLocal = new ThreadLocal<Integer>();
	
	public MessageReceiverContext() {
	}
		
	public static MessageReceiverContext getInstance() {
		return INSTANCE;
	}
	
	public void setId(Integer id) {
		threadLocal.set(id);
	}
		
	public Integer getId() {
		return threadLocal.get();
	}
	
	public synchronized void clear() {
		threadLocal.remove();
	}

	@PreDestroy
	public synchronized void destroy() {
		if (threadLocal == null) return;
		
		threadLocal.remove();
		threadLocal = null;
		INSTANCE = null;
	}

}
