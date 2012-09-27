package org.nuclos.common2;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
	}
	
	public static XStreamSupport getInstance() {
		return INSTANCE;
	}
	
	public XStream getXStream() {
		final XStream result = new XStream(new StaxDriver()) {
		    protected MapperWrapper wrapMapper(MapperWrapper next) {
		        return new NuclosMapper(defaultImplementation, next);
		    }			
		};
		return result;
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
		        return new NuclosMapper(defaultImplementation, next);
		    }			
		};
		return result;
	}
	
	public static void main(String[] args) throws Exception {
		final DependantMasterDataMap test = new DependantMasterDataMapImpl();
		final XStreamSupport dut = new XStreamSupport();
		final XStream xstream = dut.getXStream();
		// xstream.toXML(test, new FileWriter("/home/tpasch2/test.xml"));
		final Object o = xstream.fromXML(new FileReader("/home/tpasch2/test2.xml"));
		System.out.println("Read: " + o + " of type " + o.getClass());
	}
	
	private static class NuclosMapper extends MapperWrapper {
		
		private final Map<Class<?>, Class<?>> defaultImplementation;

		public NuclosMapper(Map<Class<?>, Class<?>> defaultImplementation, Mapper wrapped) {
			super(wrapped);
			this.defaultImplementation = defaultImplementation;
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

}
