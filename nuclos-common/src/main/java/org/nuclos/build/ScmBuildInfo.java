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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Properties;

/**
 * Retrieve SVN information (like revision number) from the build environment during build time.
 * <p>
 * The main of this class is executed at mvn <em>build</em> time on phase 'prepare-package'.
 * It is <em>not</em> used during Nuclos runtime.
 * </p>
 * @author Thomas Pasch
 * @since Nuclos 3.2.0
 */
public class ScmBuildInfo {
	
	private static final Class<?>[] STRING_ARRAY_ARGS = new Class<?>[] { String[].class };
	
	private static final String EXPECTED_SUFFIX = "/nuclos-common/target/classes";
	
	public static void main(String[] args) throws Exception {
		NoExitSecurityManager sm = null;
		SvnkitBuildInfo svnkit = null;
		try {
			File resources = getResourcesMainSrcRoot();
			if (!resources.isDirectory()) {
				System.out.println("can't find resources dir: " + resources + ", falling back to classpath");
				resources = getClassRoot();
			}
			final File info = new File(resources, "info.txt");
			info.delete();
			final File status = new File(resources, "status.txt");
			status.delete();
			// printSystemProps();
			System.out.println("user.dir: " + System.getProperty("user.dir"));
			System.out.println("resources dir: " + resources);
			/*
			try {
				sm = new NoExitSecurityManager();
				System.setSecurityManager(sm);
				invokeMain("org.tmatesoft.svn.cli.SVN", new String[] {"info", "--xml"}, new File(resources, "info.txt"));
				invokeMain("org.tmatesoft.svn.cli.SVN", new String[] {"status", "--xml"}, new File(resources, "status.txt"));
			}
			catch (IllegalArgumentException e) {
				// no svn?
				System.out.println("svn info not available: " + e);
			}
			 */
			svnkit = new SvnkitBuildInfo(getSvnWorkingDir());
			svnkit.info(info);
			svnkit.status(status);
			System.out.println("ScmBuildInfo: finished execution, written " + info + " and " + status);
		}
		finally {
			if (sm != null) {
				sm.setEnable(false);
			}
			if (svnkit != null) {
				svnkit.dispose();
			}
		}
	}
	
	private static void printSystemProps() {
		final Properties props = System.getProperties();
		for (Object key : props.keySet()) {
			final Object value = props.get(key);
			System.out.println(key + " -> " + value);
		}
	}
	
	private static File getClassRoot() {
		final URL root = ScmBuildInfo.class.getProtectionDomain().getCodeSource().getLocation();
		if (!root.getProtocol().equals("file")) {
			throw new IllegalStateException("class path root is not a directory: " + root);
		}
		final String url = root.toExternalForm().substring(5);
		if (url.endsWith("target/classes")) {
			throw new IllegalStateException("class path does not end in 'target/classes': " + url);
		}
		return new File(url);
	}
	
	private static File getJavaMainSrcRoot() {
		return new File(getBaseDir(), "src/main/java");
	}
	
	private static File getResourcesMainSrcRoot() {
		return new File(getBaseDir(), "src/main/resources");
	}
	
	private static File getClassesRoot() {
		return new File(getBaseDir(), "target/classes");
	}
	
	private static File getBaseDir() {
		return getClassRoot().getParentFile().getParentFile();
	}
	
	private static File getSvnWorkingDir() {
		File result = getBaseDir().getParentFile();
		File svn = new File(result, ".svn");
		if (!svn.isDirectory()) {
			// fallback to java.class.path
			result = null;
			final String[] paths = System.getProperty("java.class.path").split(File.pathSeparator);
			for (String p: paths) {
				if (p.endsWith(EXPECTED_SUFFIX)) {
					svn = new File(p.substring(0, p.length() - EXPECTED_SUFFIX.length()), ".svn");
					if (svn.isDirectory()) {
						result = svn.getParentFile();
						break;
					}
				}
			}
			if (result == null) {
				throw new IllegalStateException("Can't determine svn working dir starting from " + result);
			}
		}
		return result;
	}
	
	private static void invokeMain(String className, String[] args, File out) throws FileNotFoundException {
		final PrintStream outStream = System.out;
		System.setOut(new PrintStream(out));
		try {
			final Class<?> clazz = Class.forName(className);
			final Method main = clazz.getMethod("main", STRING_ARRAY_ARGS);
			main.invoke(null, (Object) args);
		}
		catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(e);
		} 
		catch (SecurityException e) {
			throw new IllegalArgumentException(e);
		} 
		catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(e);
		} 
		catch (IllegalAccessException e) {
			throw new IllegalArgumentException(e);
		} 
		catch (InvocationTargetException e) {
			throw new IllegalArgumentException(e.getCause());
		}
		finally {
			System.out.flush();
			System.setOut(outStream);
		}
		// throw new IllegalArgumentException("Here we go");
	}
	
	private static void invokeMainOnThread(final String className, final String[] args, final File out) throws InterruptedException {
		final Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					invokeMain(className, args, out);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		thread.start();
		thread.join();
	}

}
