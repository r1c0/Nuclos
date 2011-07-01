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



public class NuclosRemoteContextHolder {
	
	private static final ThreadLocal<Stack<Boolean>> threadLocal = new ThreadLocal<Stack<Boolean>>();
	
	public static void setRemotly(Boolean bln) {
		Stack<Boolean> stack = threadLocal.get();
		if(stack == null) {
			stack = new Stack<Boolean>();
		}
		stack.push(bln);
		threadLocal.set(stack);
	}
	
	public static Integer getSize() {
		return threadLocal.get().size();
	}
	
	public static Boolean pop() {
		return threadLocal.get().pop();
	}
	
	public static Boolean peek() {
		Stack<Boolean> stack = threadLocal.get();
		return stack.size() > 0 ? stack.peek() : null;
	}
	
	public static Stack<Boolean> getRequestStack() {
		return threadLocal.get();
	}
	
	public static void clear() {
		threadLocal.remove();
	}

}
