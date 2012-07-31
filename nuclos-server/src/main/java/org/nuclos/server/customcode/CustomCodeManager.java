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
package org.nuclos.server.customcode;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.nuclos.api.annotation.Function;
import org.nuclos.api.event.AfterDeleteEvent;
import org.nuclos.api.event.AfterSaveEvent;
import org.nuclos.api.event.DeleteEvent;
import org.nuclos.api.event.SaveEvent;
import org.nuclos.api.event.StateChangeEvent;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.common.NuclosSystemParameters;
import org.nuclos.server.customcode.codegenerator.NuclosJavaCompilerComponent;
import org.nuclos.server.customcode.codegenerator.RuleClassLoader;
import org.nuclos.server.customcode.codegenerator.RuleCodeGenerator;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.eventsupport.valueobject.EventSupportVO;
import org.nuclos.server.ruleengine.NuclosCompileException;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Provides the classloader for dynamically loaded code (Rules, Wsdl).
 */
@Component
public class CustomCodeManager implements ApplicationContextAware, MessageListener {

	private static final Logger log = Logger.getLogger(CustomCodeManager.class);

	// Spring injection

	private NuclosJavaCompilerComponent nuclosJavaCompilerComponent;

	private ApplicationContext parent;

	private Map<Class<?>, List<EventSupportVO>> executableEventSupportFiles;
	
	// End of Spring injection

	private RuleClassLoader cl;

	private AnnotationConfigApplicationContext context;

	private Map<String, BeanFunction> functions = new HashMap<String, BeanFunction>();

	private final Class[] registeredEvenTypes = new Class[] {StateChangeEvent.class, DeleteEvent.class, AfterDeleteEvent.class, SaveEvent.class, AfterSaveEvent.class};
	
	@Autowired
	private ServletContext servletContext;
	
	CustomCodeManager() {
		
	}

	@Autowired
	final void setNuclosJavaCompilerComponent(NuclosJavaCompilerComponent nuclosJavaCompilerComponent) {
		this.nuclosJavaCompilerComponent = nuclosJavaCompilerComponent;
	}

	public <T> T getInstance(RuleCodeGenerator<T> generator) throws NuclosCompileException {
		try {
			return (T) getClassLoader().loadClass(generator.getClassName()).newInstance();
		}
		catch(InstantiationException e) {
			throw new NuclosCompileException(e);
		}
		catch(IllegalAccessException e) {
			throw new NuclosCompileException(e);
		}
		catch(ClassNotFoundException e) {
			throw new NuclosCompileException(e);
		}
	}
	
	public <T> T getInstance(String sClazz) throws NuclosCompileException {
		try {
			if (nuclosJavaCompilerComponent.validate()) {
				this.cl = null;
			}
			return (T) getClassLoader().loadClass(sClazz).newInstance();
		}
		catch(InstantiationException e) {
			throw new NuclosCompileException(e);
		}
		catch(IllegalAccessException e) {
			throw new NuclosCompileException(e);
		}
		catch(ClassNotFoundException e) {
			throw new NuclosCompileException(e);
		}
	}

	@Override
	public void onMessage(Message message) {
		if(message instanceof TextMessage) {
			try {
				String text = ((TextMessage) message).getText();
				if (StringUtils.isNullOrEmpty(text) || text.equals(NuclosEntity.NUCLET.getEntityName())) {
					log.info("Reload nuclet classloader and application context.");
					this.cl = null;
				}
			}
			catch(JMSException e) {
				log.error(getClass().getName() + ".onMessage() failed: " + e, e);
			}
		}
	}

	/**
	 * Obtain an instance of the classloader for a given rule artifact.
	 *
	 * @param parent the parent classloader for classloader delegation (usually the application classloader)
	 * @param rulegenerator a generator for the loaded artifact
	 * @return classloader
	 * @throws NuclosCompileException
	 */
	public ClassLoader getClassLoader() throws NuclosCompileException {
		if (this.cl == null || nuclosJavaCompilerComponent.validate()) {
			if (this.context != null) {
				this.context = null;
				this.functions.clear();
			}

			this.cl = new RuleClassLoader(CustomCodeManager.class.getClassLoader());

			JarFile jar = null;
			try {
				nuclosJavaCompilerComponent.validate();
				jar = new JarFile(NuclosJavaCompilerComponent.JARFILE);
				this.cl.addJarsToClassPath(NuclosSystemParameters.getDirectory(NuclosSystemParameters.WSDL_GENERATOR_LIB_PATH));
				this.cl.addURL(NuclosJavaCompilerComponent.JARFILE.toURL());

				// see http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/beans.html#beans-java-instantiating-container
				this.context = new AnnotationConfigApplicationContext();
				this.context.setParent(this.parent);
				this.context.setClassLoader(this.cl);
				this.context.getBeanFactory().addBeanPostProcessor(new BeanFunctionPostProcessor());
				
				// add all nuclet packages to scan:
				List<String> packages = new ArrayList<String>();
				for (EntityObjectVO nuclet : NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.NUCLET).getAll()) {
					String p = nuclet.getField("package", String.class);
					if (!StringUtils.isNullOrEmpty(p)) {
						packages.add(p);
					}
				}
				if (packages.size() > 0) {
					this.context.scan(packages.toArray(new String[packages.size()]));
				}

				this.context.refresh();
				this.context.start();
				
				// load and cache all executable rules
				this.executableEventSupportFiles = new CustomCodeRuleScanner(this.cl, this.servletContext).
						getExecutableRulesFromClasspath(registeredEvenTypes);

			}
			catch (IOException ex) {
				throw new NuclosFatalException(ex);
			}
			finally {
				try {
					if (jar != null) {
						jar.close();
					}
				}
				catch(IOException e) {
					log.warn("getInstance: " + e);
				}
			}
		}
		return this.cl;
	}
	
	public List<Class> getRegisteredSupportEventTypes()
	{
		return Arrays.asList(this.registeredEvenTypes);
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.parent = applicationContext;
	}

	public List<EventSupportVO> getExecutableEventSupportFiles()
	{
		try {
			getClassLoader();
		}
		catch (NuclosCompileException e) {
			throw new NuclosFatalException(e);
		}
		List<EventSupportVO> list = new ArrayList<EventSupportVO>();
	
		for(Class n : this.executableEventSupportFiles.keySet())
		{
			list.addAll(0,this.executableEventSupportFiles.get(n));
		}
		
		return list;
	}
	
	public List<EventSupportVO> getExecutableEventSupportFilesByClassType(List<Class<?>> listOfInterfaces)
	{
		try {
			getClassLoader();
		}
		catch (NuclosCompileException e) {
			throw new NuclosFatalException(e);
		}
		List<EventSupportVO> list = new ArrayList<EventSupportVO>();
	
		for(Class n : listOfInterfaces)
		{
			list.addAll(0,this.executableEventSupportFiles.get(n));
		}
		
		return list;
	}
	
	
	public Object invokeFunction(String functionname, Object[] args) {
		try {
			getClassLoader();
		}
		catch (NuclosCompileException e) {
			throw new NuclosFatalException(e);
		}

		BeanFunction bf = functions.get(functionname);
		if (bf != null) {
			try {
				return bf.getMethod().invoke(bf.getBean(), args);
			}
			catch (IllegalArgumentException e) {
				log.warn("Function invoked with illegal arguments.", e);
				throw new NuclosFatalException(e);
			}
			catch (IllegalAccessException e) {
				log.warn("Function invoked with illegal access.", e);
				throw new NuclosFatalException(e);
			}
			catch (InvocationTargetException e) {
				log.warn("Invoked function threw an exception:", e.getTargetException());
				throw new NuclosFatalException(e.getTargetException());
			}
		}
		else {
			throw new NuclosFatalException("Unknown function:" + functionname);
		}
	}

	
	private class BeanFunctionPostProcessor implements BeanPostProcessor {

		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
			return bean;
		}

		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
			if (bean instanceof AopInfrastructureBean) {
				// Ignore AOP infrastructure such as scoped proxies.
				return bean;
			}
			Class<?> targetClass = AopUtils.getTargetClass(bean);
			for (Method m : targetClass.getMethods()) {
				if (m.isAnnotationPresent(Function.class)) {
					Function f = m.getAnnotation(Function.class);
					log.info("Processing Function " + bean.getClass() + "." + m.getName() + "[name=" + f.value()+ "]");
					functions.put(f.value(), new BeanFunction(bean, m));
				}
			}
			return bean;
		}

	}

	public static class BeanFunction {

		private final Object bean;

		private final Method method;

		public BeanFunction(Object bean, Method method) {
			super();
			this.bean = bean;
			this.method = method;
		}

		public Object getBean() {
			return bean;
		}

		public Method getMethod() {
			return method;
		}
	}
	
}
