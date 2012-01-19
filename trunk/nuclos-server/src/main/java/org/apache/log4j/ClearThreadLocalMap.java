package org.apache.log4j;

import org.apache.log4j.helpers.ThreadLocalMap;

/**
 * This is a HACK to avoid the tomcat memory leak described in
 * https://issues.apache.org/bugzilla/show_bug.cgi?id=50486
 * 
 * @author Thomas Pasch
 */
public class ClearThreadLocalMap {
	
	public static synchronized void shutdown() {
		final MDC mdc = MDC.mdc;
		if (mdc != null) {
			final ThreadLocalMap tlm = (ThreadLocalMap) mdc.tlm;
			if (tlm != null) {
				tlm.remove();
				mdc.tlm = null;
			}
		}
	}

}
