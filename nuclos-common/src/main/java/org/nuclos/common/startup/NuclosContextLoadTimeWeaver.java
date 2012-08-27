package org.nuclos.common.startup;

import java.lang.instrument.ClassFileTransformer;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.weaving.DefaultContextLoadTimeWeaver;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.instrument.classloading.SimpleThrowawayClassLoader;

public class NuclosContextLoadTimeWeaver extends DefaultContextLoadTimeWeaver {
	
	private static final Logger LOG = Logger.getLogger(NuclosContextLoadTimeWeaver.class); 
	
	public NuclosContextLoadTimeWeaver() {		
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		if (isLtw(classLoader)) {
			super.setBeanClassLoader(classLoader);
		}
		else {
			super.setBeanClassLoader(new DummyInstrumentedClassLoader(classLoader));
		}
	}
	
	private boolean isLtw(ClassLoader classLoader) {
		boolean result = false;
		final Boolean ws = Boolean.getBoolean("nuclos.client.webstart");
		// Only check for load-time weaving if this is not a web start client 
		// (web start doesn't allow that) (tp)
		if (ws == null || !ws.booleanValue()) {
			final String name = classLoader.getClass().getName();
			if (name.indexOf("InstrumentableClassLoader") >= 0) {
				LOG.info("Found instrumentable class loader: " + name);
				result = true;
			}
			else {
				final RuntimeMXBean RuntimemxBean = ManagementFactory.getRuntimeMXBean();
				final List<String> arguments = RuntimemxBean.getInputArguments();
				LOG.info("client started with " + arguments);
				for (String s : arguments) {
					if (s != null && s.indexOf("-javaagent:") >= 0) {
						LOG.info("Found javaagent vm arg: " + s);
						result = true;
						break;
					}
				}
			}
		}
		LOG.info("enable LTW: " + result);
		return result;
	}
	
	public static final class DummyInstrumentedClassLoader extends ClassLoader {
		
		private DummyInstrumentedClassLoader(ClassLoader parent) {
			super(parent);
		}
		
		public void addTransformer(ClassFileTransformer trans) {
			// ignore
		}
		
	}
	
	public final static class DummyLoadTimeWeaver implements LoadTimeWeaver {
		
		private final ClassLoader classLoader;
		
		private DummyLoadTimeWeaver(ClassLoader classLoader) {
			this.classLoader = classLoader;
		}

		@Override
		public void addTransformer(ClassFileTransformer transformer) {
			// ignore
		}

		@Override
		public ClassLoader getInstrumentableClassLoader() {
			return classLoader;
		}

		@Override
		public ClassLoader getThrowawayClassLoader() {
			return new SimpleThrowawayClassLoader(classLoader);
		}
		
	}
	
}
