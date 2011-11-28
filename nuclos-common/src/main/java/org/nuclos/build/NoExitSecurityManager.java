//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.build;

import java.security.Permission;

/**
 * This class will hinder SVNKit CLI to call system exit during <em>build</em> time.
 * <p>
 * This class is used at mvn <em>build</em> time on phase 'prepare-package'.
 * It is <em>not</em> used during Nuclos runtime.
 * </p>
 * @author Thomas Pasch
 * @since Nuclos 3.2.0
 */
public class NoExitSecurityManager extends SecurityManager {
	
	private final SecurityManager parent;
	
	private boolean enabled = true;

	public NoExitSecurityManager(final SecurityManager parent) {
		assert parent != null;

		this.parent = parent;
	}

	public NoExitSecurityManager() {
		this(System.getSecurityManager());
	}

	@Override
	public void checkPermission(final Permission perm) {
		if (parent != null) {
			parent.checkPermission(perm);
		}
	}

	/**
	 * Always throws {@link SecurityException}.
	 */
	@Override
	public void checkExit(final int code) {
		if (enabled) {
			throw new SecurityException("Use of System.exit() is forbidden!");
		}
		else if (parent != null) {
			parent.checkExit(code);
		}
	}
	
	public void setEnable(boolean enable) {
		enabled = enable;
	}

	/*
	public void checkPermission(final Permission perm) {
	    assert perm != null;

	    if (perm.getName().equals("exitVM")) {
	        System.out.println("exitVM");
	    }
	}
	*/
}
