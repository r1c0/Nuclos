package org.nuclos.common2;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMapImpl;
import org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVO;
import org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVOImpl;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;

public class XStreamSupport {
	
	private static XStreamSupport INSTANCE;
	
	private Map<Class<?>, Class<?>> defaultImplementation;
	
	private GenericObjectPool<XStream> pool;
	
	public XStreamSupport() {
		init();
		INSTANCE = this;
	}
	
	private final void init() {
		final Map<Class<?>,Class<?>> map = new HashMap<Class<?>, Class<?>>();
		
		// In Nuclos 3.8 DependantMasterDataMap has become an interface (tp)
		map.put(DependantMasterDataMap.class, DependantMasterDataMapImpl.class);
		// In Nuclos 3.8 RuleObjectContainerCVO has become an interface (tp)
		map.put(RuleObjectContainerCVO.class, RuleObjectContainerCVOImpl.class);
		
		defaultImplementation = Collections.unmodifiableMap(map);
		
		// pool
		pool = new GenericObjectPool<XStream>(new PoolableXStreamFactory());
		pool.setTestOnBorrow(false);
		pool.setTestOnReturn(false);
		pool.setTestWhileIdle(false);
		pool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_FAIL);
		pool.setMaxActive(10);
		pool.setMaxIdle(10);
		pool.setMinIdle(0);
		pool.setSoftMinEvictableIdleTimeMillis(-1);
		pool.setMinEvictableIdleTimeMillis(-1);
	}
	
	public static XStreamSupport getInstance() {
		return INSTANCE;
	}
	
	/**
	 * Attention: You <em>must</em> return the XStream instance by calling
	 * {@link #returnXStream(XStream)}.
	 */
	public XStream getXStream() {
		try {
			return pool.borrowObject();
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	public void returnXStream(XStream xstream) {
		try {
			pool.returnObject(xstream);
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	/**
	 * This performs <em>very</em> badly!
	 * <p>
	 * This method is <em>never</em> needed if you (de)serialize to/from a String.
	 * </p>
	 * @deprecated Read http://stackoverflow.com/questions/1001899/xstream-fromxml-exception for alternatives.
	 */
	public XStream getXStreamUtf8() {
		// ???
		final XStream result = new XStream(new DomDriver("UTF-8")) {
		    protected MapperWrapper wrapMapper(MapperWrapper next) {
		        return new NuclosMapper(next);
		    }			
		};
		return result;
	}
	
	public static void main(String[] args) throws Exception {
		final DependantMasterDataMap test = new DependantMasterDataMapImpl();
		final XStreamSupport dut = new XStreamSupport();
		final XStream xstream = dut.getXStream();
		try {
			// xstream.toXML(test, new FileWriter("/home/tpasch2/test.xml"));
			final Object o = xstream.fromXML(new FileReader("/home/tpasch2/test2.xml"));
			System.out.println("Read: " + o + " of type " + o.getClass());
		}
		finally {
			dut.returnXStream(xstream);
		}
	}
	
	private class NuclosMapper extends MapperWrapper {
		
		public NuclosMapper(Mapper wrapped) {
			super(wrapped);
		}

		@Override
	    public Class<?> defaultImplementationOf(Class type) {
			Class<?> result = defaultImplementation.get(type);
			if (result == null) {
				result = super.defaultImplementationOf(type);
			}
			return result;
	    }

	}
	
	private class PoolableXStreamFactory implements PoolableObjectFactory<XStream> {
		
		private PoolableXStreamFactory() {
		}

		@Override
		public XStream makeObject() throws Exception {
			final XStream result = new XStream(new StaxDriver()) {
			    protected MapperWrapper wrapMapper(MapperWrapper next) {
			        return new NuclosMapper(next);
			    }
			};
			return result;
		}

		@Override
		public void destroyObject(XStream obj) throws Exception {
			// do nothing
		}

		@Override
		public boolean validateObject(XStream obj) {
			return true;
		}

		@Override
		public void activateObject(XStream obj) throws Exception {
			// do nothing
		}

		@Override
		public void passivateObject(XStream obj) throws Exception {
			// do nothing
		}
		
	}

}
