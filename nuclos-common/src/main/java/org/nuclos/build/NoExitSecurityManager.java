package org.nuclos.build;

import java.security.Permission;

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

	public void checkPermission(final Permission perm) {
		if (parent != null) {
			parent.checkPermission(perm);
		}
	}

	/**
	 * Always throws {@link SecurityException}.
	 */
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
