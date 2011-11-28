package org.nuclos.build;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

public class ScmBuildInfo {
	
	private static final Class<?>[] STRING_ARRAY_ARGS = new Class<?>[] { String[].class };
	
	public static void main(String[] args) throws Exception {
		final NoExitSecurityManager sm = new NoExitSecurityManager();
		System.setSecurityManager(sm);
		try {
			final File classRoot = getResourcesMainSrcRoot();
			final File info = new File(classRoot, "info.xml");
			info.delete();
			final File status = new File(classRoot, "status.xml");
			status.delete();
			System.out.println("user.dir: " + System.getProperty("user.dir"));
			System.out.println("class root: " + classRoot);
			try {
				invokeMainOnThread("org.tmatesoft.svn.cli.SVN", new String[] {"info", "--xml"}, new File(classRoot, "info.xml"));
				invokeMainOnThread("org.tmatesoft.svn.cli.SVN", new String[] {"status", "--xml"}, new File(classRoot, "status.xml"));
			}
			catch (IllegalArgumentException e) {
				// no svn?
				System.out.println("svn info not available: " + e);
			}
			System.out.println("ScmBuildInfo: finished execution");
		}
		finally {
			sm.setEnable(false);
		}
	}
	
	public static File getClassRoot() {
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
	
	public static File getJavaMainSrcRoot() {
		return new File(getClassRoot().getParentFile().getParentFile(), "src/main/java");
	}
	
	public static File getResourcesMainSrcRoot() {
		return new File(getClassRoot().getParentFile().getParentFile(), "src/main/resources");
	}
	
	public static void invokeMain(String className, String[] args, File out) throws FileNotFoundException {
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
	
	public static void invokeMainOnThread(final String className, final String[] args, final File out) throws InterruptedException {
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
