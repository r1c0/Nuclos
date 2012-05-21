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
package org.nuclos.server.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.collection.multimap.MultiListHashMap;
import org.nuclos.common.collection.multimap.MultiListMap;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.validation.annotation.Validation;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class ValidationSupport implements BeanPostProcessor {

	private static final Logger LOG = Logger.getLogger(ValidationSupport.class);

	public static final String __GENERIC = "__GENERIC";

	private final MultiListMap<String, Pair<Integer, Validator>> validators = new MultiListHashMap<String, Pair<Integer, Validator>>();

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof AopInfrastructureBean) {
			// Ignore AOP infrastructure such as scoped proxies.
			return bean;
		}
		Class<?> targetClass = AopUtils.getTargetClass(bean);
		if (targetClass.isAnnotationPresent(Validation.class) && bean instanceof Validator) {
			Validation a = targetClass.getAnnotation(Validation.class);
			Validator v = (Validator) bean;
			Integer order = a.order();

			LOG.info("Processing Validator " + bean.getClass() + "[entity=" + a.entity() + ";entities=" + a.entities() + ";order=" + order + "]");

			Pair<Integer, Validator> entry = new Pair<Integer, Validator>(order, v);
			if (StringUtils.isNullOrEmpty(a.entity()) && (a.entities() == null || a.entities().length == 0)) {
				validators.addValue(__GENERIC, entry);
			}
			if (a.entities() != null) {
				for (String entity : a.entities()) {
					validators.addValue(entity, entry);
				}
			}
			if (!StringUtils.isNullOrEmpty(a.entity())) {
				validators.addValue(a.entity(), entry);
			}
		}
		return bean;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	public Validator[] getValidators(String entity) {
		List<Pair<Integer, Validator>> result = new ArrayList<Pair<Integer,Validator>>();
		if (validators.containsKey(__GENERIC)) {
			result.addAll(validators.getValues(__GENERIC));
		}
		if (validators.containsKey(entity)) {
			result.addAll(validators.getValues(entity));
		}
		Collections.sort(result, new Comparator<Pair<Integer, Validator>>() {
			@Override
			public int compare(Pair<Integer, Validator> o1, Pair<Integer, Validator> o2) {
				return LangUtils.compare(o1.x, o2.x);
			}
		});
		return CollectionUtils.transform(result, new Transformer<Pair<Integer, Validator>, Validator>() {
			@Override
			public Validator transform(Pair<Integer, Validator> i) {
				return i.y;
			}
		}).toArray(new Validator[result.size()]);
	}

	public void validate(EntityObjectVO eo, DependantMasterDataMap dependants) throws CommonValidationException {
		ValidationContext c = new ValidationContext();
		validate(eo, dependants, c);
		if (c.hasErrors()) {
			throw new CommonValidationException(c.getErrors(), c.getFieldErrors());
		}
	}

	private void validate(EntityObjectVO eo, DependantMasterDataMap dependants, ValidationContext c) {
		for (Validator v : getValidators(eo.getEntity())) {
			LOG.debug("Processing validator " + v.getClass() + " for object [" + eo.getDebugInfo() + "]");
			v.validate(eo, c);
		}
		if (dependants != null) {
			for (String entity : dependants.getEntityNames()) {
				for (EntityObjectVO eo2 : dependants.getData(entity)) {
					// ensure that entity name is set!
					eo2.setEntity(entity);
					c.setParent(eo.getEntity());
					validate(eo2, eo2.getDependants(), c);
				}
			}
		}
	}
}
