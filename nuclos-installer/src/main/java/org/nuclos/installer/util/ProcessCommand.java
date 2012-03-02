package org.nuclos.installer.util;

import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class ProcessCommand {
	
	private static final Logger LOG = Logger.getLogger(ProcessCommand.class);
	
	private static final Pattern UNIX_HOME_PAT = Pattern.compile("^/home/(\\p{Alnum}+)/?");
	
	private static final List<String> BROWSERS;
	
	private static final List<List<String>> BROWSERS_OPTIONS;
	
	private static final List<String> SUGUI;
	
	private static final List<List<String>> SUGUI_OPTIONS;
	
	static {
		List<String> l = new ArrayList<String>();
		l.add("firefox");
		l.add("sensible-browser");
		l.add("google-chrome");
		l.add("rundll32");
		BROWSERS = Collections.unmodifiableList(l);
		
		List<List<String>> o = new ArrayList<List<String>>();
		// firefox
		// o.add(Collections.singletonList("-no-remote"));
		o.add(Collections.<String>emptyList());
		// sensible
		o.add(Collections.<String>emptyList());
		// chrome
		o.add(Collections.<String>emptyList());
		// rundll32
		o.add(Collections.singletonList("url.dll,FileProtocolHandler"));
		BROWSERS_OPTIONS = Collections.unmodifiableList(o);
		
		l = new ArrayList<String>();
		l.add("su-to-root");
		l.add("gksu");
		SUGUI = Collections.unmodifiableList(l);
		o = new ArrayList<List<String>>();
		// su-to-root
		o.add(Arrays.asList(new String[] { "-X", "-c" }));
		// gksu
		o.add(Collections.singletonList("--"));
		SUGUI_OPTIONS = Collections.unmodifiableList(o);
	}
	
	//
	
	private ProcessBuilder pb;
	
	private List<File> path;
	
	private Process process;
	
	private List<Command> browsers;
	
	private List<Command> suguis;
	
	public ProcessCommand() {
	}
	
	public boolean canBrowse() throws IOException {
		if (browsers == null) {
			browsers = filterWithPath(BROWSERS, BROWSERS_OPTIONS);
		}
		return browsers != null && !browsers.isEmpty();
	}
	
	public Process browse(URI uri, boolean isPrivileged) throws IOException {
		if (!canBrowse()) {
			return null;
		}
		LOG.info("browse(" + uri + ", " + isPrivileged + ")");
		Process result = null;
		final List<String> args = new ArrayList<String>();
		final String realUser = guessRealUser(isPrivileged);
		final boolean useSu = realUser != null && EnvironmentUtils.isLinux();
		for (Command b: browsers) {
			args.clear();
			// use su to real username
			if (useSu) {
				args.add("su");
				args.add("-c");
				args.add(StringUtils.join(b.getPath().toString(), b.getOptions(), uri.toString()));
				args.add(realUser);
			}
			else {
				args.add(b.getPath().toString());
				args.addAll(b.getOptions());
				args.add(uri.toString());
			}
			try {
				result = exec(args);
				break;
			}
			catch (IOException e) {
				// ignore
				result = null;
			}
		}
		return result;
	}
	
	public boolean canGuiSu() throws IOException {
		if (suguis == null) {
			if (GraphicsEnvironment.isHeadless()) {
				suguis = Collections.emptyList();
			}
			else {
				suguis = filterWithPath(SUGUI, SUGUI_OPTIONS);
			}
		}
		return suguis != null && !suguis.isEmpty();
	}
	
	public Process guiSu(List<String> a, boolean isPrivileged) throws IOException {
		if (!canGuiSu()) {
			return null;
		}
		LOG.info("guiSu(" + a + ", " + isPrivileged + ")");
		Process result = null;
		final List<String> args = new ArrayList<String>();
		for (Command b: suguis) {
			args.clear();
			// use su to real username
			if (!isPrivileged) {
				args.add(b.getPath().toString());
				args.addAll(b.getOptions());
				args.add(StringUtils.join(a));
			}
			else {
				args.addAll(a);
			}
			try {
				result = exec(args);
				break;
			}
			catch (IOException e) {
				LOG.debug("exec failed", e);
				// ignore
				result = null;
			}
		}
		return result;		
	}
	
	private Process exec(List<String> args) throws IOException {
		pb = new ProcessBuilder(args);
		process = pb.start();
		if (process != null) {
			LOG.info("exec: " + args);
		}
		return process;
	}
	
	/**
	 * Find the 'real' user (i.e. the user executing 'sudo' on unix).
	 * 
	 * @param isPrivileged
	 * @return The real user or null if the user is the same as the real user.
	 */
	private String guessRealUser(boolean isPrivileged) {
		// String result = System.getProperty("user.name");
		String result = null;
		if (EnvironmentUtils.isLinux()) {
			// result = null;
			if (isPrivileged) {
				// final String home = System.getProperty("user.home");
				final String home = System.getenv("HOME");
				final Matcher m = UNIX_HOME_PAT.matcher(home);
				if (m.find()) {
					result = m.group(1);
				}
				else {
					result = null;
				}
			}
		}
		LOG.info("real user is " + result);
		return result;
	}
	
	private void mkPath() throws IOException {
		if (path == null) {
			final List<File> result = new ArrayList<File>();
			final String p = System.getenv("PATH");
			if (p != null) {
				for (String pe : p.split(File.pathSeparator)) {
					result.add(new File(pe).getCanonicalFile());
				}
			}
			path = result;
		}
	}
	
	private List<Command> filterWithPath(List<String> programs, List<List<String>> options) throws IOException {
		mkPath();
		List<Command> result = new ArrayList<Command>(programs.size());
		final Iterator<List<String>> it = options.iterator();
		for (String p: programs) {
			final List<String> opts = it.next();
			for (File d: path) {
				File f = new File(d, p);
				if (canExecute(f)) {
					result.add(new Command(p, f, opts));
					continue;
				}
				f = new File(d, p + ".exe");
				if (canExecute(f)) {
					result.add(new Command(p, f, opts));
					continue;
				}
				f = new File(d, p + ".dmg");
				if (canExecute(f)) {
					result.add(new Command(p, f, opts));
					continue;
				}
			}
		}
		return result;
	}
	
	private boolean canExecute(File f) throws IOException {
		boolean result = f.canExecute();
		if (result) return result;
		if (f.canRead()) {
			final BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(f)));
			try {
				final String line = reader.readLine();
				if (line.length() < 200 && line.startsWith("#!/")) {
					result = true;
				}
			}
			finally {
				reader.close();
			}
		}
		return result;
	}
	
	public static void main(String[] args) throws IOException, URISyntaxException {
		final ProcessCommand dut = new ProcessCommand();
		final List<String> cmd = new ArrayList<String>();
		cmd.add("firefox");
		cmd.add("-no-remote");
		cmd.add("http://localhost");
		
		// dut.browse(new URI("http://localhost"), false);
		// dut.guiSu(cmd, true);
		dut.guessRealUser(false);
	}
	
	private static class Command {
		
		private final String name;
		
		private final File path;
		
		private final List<String> options;
		
		private Command(String name, File path, List<String> options) {
			this.name = name;
			this.path = path;
			this.options = options;
		}
		
		public String getName() {
			return name;
		}
		
		public File getPath() {
			return path;
		}
		
		public List<String> getOptions() {
			return options;
		}
		
	}

}
