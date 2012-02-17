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

import java.util.Stack;

import javax.annotation.PreDestroy;

import org.springframework.stereotype.Component;


@Component
public class NuclosRemoteContextHolder {
	
	private ThreadLocal<Stack<Boolean>> threadLocal = new ThreadLocal<Stack<Boolean>>();
	
	public NuclosRemoteContextHolder() {
	}
	
	public void setRemotly(Boolean bln) {
		Stack<Boolean> stack = threadLocal.get();
		if(stack == null) {
			stack = new Stack<Boolean>();
		}
		stack.push(bln);
		threadLocal.set(stack);
	}
	
	public Integer getSize() {
		return threadLocal.get().size();
	}
	
	public Boolean pop() {
		return threadLocal.get().pop();
	}
	
	public Boolean peek() {
		Stack<Boolean> stack = threadLocal.get();
		if (stack == null) {
			stack = new Stack<Boolean>();
			threadLocal.set(stack);
		}
		return stack.size() > 0 ? stack.peek() : null;
	}
	
	public Stack<Boolean> getRequestStack() {
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
	}

}
