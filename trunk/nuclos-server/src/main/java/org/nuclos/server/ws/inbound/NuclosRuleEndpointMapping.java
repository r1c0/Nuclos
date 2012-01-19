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
package org.nuclos.server.ws.inbound;

import java.lang.reflect.Method;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.nuclos.server.common.RuleCache;
import org.nuclos.server.customcode.codegenerator.RuleClassLoader;
import org.nuclos.server.customcode.valueobject.CodeVO;
import org.springframework.beans.BeansException;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.server.endpoint.mapping.PayloadRootAnnotationMethodEndpointMapping;

public class NuclosRuleEndpointMapping extends PayloadRootAnnotationMethodEndpointMapping {

	private static final Logger LOG = Logger.getLogger(NuclosRuleEndpointMapping.class);

	private RuleClassLoader cl;

	public void setRuleClassLoader(RuleClassLoader cl) {
        this.cl = cl;
    }

    public RuleClassLoader getRuleClassLoader() {
        return cl;
    }

	@Override
	protected void initApplicationContext() throws BeansException {
		super.initApplicationContext();
		registerRuleMethods();
	}

	protected void registerRuleMethods() {
		if (RuleCache.getInstance().getCommonCode().size() > 0) {
			for (CodeVO code : RuleCache.getInstance().getCommonCode()) {
				final Class<?> endpointClass;
				try {
					endpointClass = cl.loadClass(code.getName());
				}
				catch(ClassNotFoundException e) {
					LOG.error("registerRuleMethods failed: " + e, e);
					throw new RuntimeException(e);
				}
	            if (endpointClass != null && AnnotationUtils.findAnnotation(endpointClass, getEndpointAnnotationType()) != null) {
	            	ReflectionUtils.doWithMethods(endpointClass, new ReflectionUtils.MethodCallback() {
	                    @Override
						public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
	                        QName key = getLookupKeyForMethod(method);
	                        if (key != null) {
	                        	Object instance;
								try {
									instance = endpointClass.newInstance();
								}
								catch(InstantiationException e) {
									LOG.error("registerRuleMethods failed: " + e, e);
									throw new RuntimeException(e);
								}
	                            registerEndpoint(key, new MethodEndpoint(instance, method));
	                        }
	                    }
	                });
	            }
			}
		}
    }
}
