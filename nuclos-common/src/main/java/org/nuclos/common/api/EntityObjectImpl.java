//Copyright (C) 2012  Novabit Informationssysteme GmbH
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
package org.nuclos.common.api;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.util.LangUtils;
import org.apache.log4j.Logger;
import org.nuclos.api.EntityObject;
import org.nuclos.api.Flag;
import org.nuclos.api.SystemAttribute;
import org.nuclos.common.EventSupportProvider;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.exception.CommonFatalException;

public class EntityObjectImpl implements org.nuclos.api.EntityObject {
	
	private final EntityObjectVO vo;
	private Map<String, Boolean> dependantsLoaded;
	private static final Logger LOG = Logger.getLogger(EntityObjectImpl.class);
	
	private EventSupportProvider evtSupProvider;
	
	public EntityObjectImpl(EntityObjectVO vo) {
		super();
		
		evtSupProvider = SpringApplicationContextHolder.getBean(EventSupportProvider.class);
		
		if (vo == null) {
			throw new IllegalArgumentException("vo must not be null");
		}
		dependantsLoaded = new HashMap<String, Boolean>();
		for (String key : vo.getDependants().getEntityNames()) {
			dependantsLoaded.put(key, !vo.getDependants().getData(key).isEmpty());
		}
		
		this.vo = vo;
	}

	@Override
	public Object getValue(String attribute) {
		return vo.getField(attribute);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <S> S getValue(String attribute, Class<S> cls) {
		if (BigDecimal.class.equals(cls)) {
			Object value = vo.getField(attribute);
			if (value instanceof Double) {
				return (S) new BigDecimal((Double)value);
			} else if (value instanceof Integer) {
				return (S) new BigDecimal((Integer)value);
			} else if (value instanceof Long) {
				return (S) new BigDecimal((Long)value);
			} else {
				throw new UnsupportedOperationException(String.format("Cast attribute %s value %s to %s", value, attribute, cls.getName()));
			}
		} else {
			return vo.getField(attribute, cls);
		}
	}

	@Override
	public void setValue(String attribute, Object value) {
		vo.getFields().put(attribute, value);
	}

	@Override
	public Long getId(String attribute) {
		return vo.getFieldId(attribute);
	}

	@Override
	public void setId(String attribute, Long id) {
		vo.getFieldIds().put(attribute, id);
		if (!vo.isFlagNew() && !vo.isFlagRemoved()) {
			vo.flagUpdate();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <S> S getSystemValue(SystemAttribute<S> attribute) {
		if (attribute == SystemAttribute.ID) {
			return (S) vo.getId();
		}
		if (attribute == SystemAttribute.VERSION) {
			return (S) vo.getVersion();
		}
		if (attribute == SystemAttribute.CREATED_BY) {
			return (S) vo.getCreatedBy();
		}
		if (attribute == SystemAttribute.CREATED_AT) {
			if (vo.getCreatedAt() == null) {
				return null;
			}
			return (S) new Date(vo.getCreatedAt().getTime());
		}
		if (attribute == SystemAttribute.CHANCHED_BY) {
			return (S) vo.getChangedBy();
		}
		if (attribute == SystemAttribute.CHANCHED_AT) {
			if (vo.getChangedAt() == null) {
				return null;
			}
			return (S) new Date(vo.getChangedAt().getTime());
		}
		if (attribute == SystemAttribute.STATE) {
			Long id = vo.getFieldIds().get(NuclosEOField.STATE.getName());
			if (id == null) {
				return null;
			} else {
				StateImpl result = new StateImpl();
				result.setId(id);
				result.setName(vo.getField(NuclosEOField.STATE.getName(), String.class));
				result.setNumeral(vo.getField(NuclosEOField.STATENUMBER.getName(), Integer.class));
				return (S) result;
			}
		}
		if (attribute == SystemAttribute.PROCESS) {
			Long id = vo.getFieldIds().get(NuclosEOField.PROCESS.getName());
			if (id == null) {
				return null;
			} else {
				ProcessImpl result = new ProcessImpl();
				result.setId(id);
				result.setName(vo.getField(NuclosEOField.PROCESS.getName(), String.class));
				return (S) result;
			}
		}
		if (attribute == SystemAttribute.LOGICAL_DELETED) {
			return (S) vo.getField(NuclosEOField.LOGGICALDELETED.getName(), Boolean.class);
		}
		return null;
	}

	@Override
	public <S> void setSystemValue(SystemAttribute<S> attribute, S value) {
		if (attribute == SystemAttribute.PROCESS) {
			ProcessImpl process = (ProcessImpl) value;
			vo.getFieldIds().put(NuclosEOField.PROCESS.getName(), process.getId());
			vo.getFields().put(NuclosEOField.PROCESS.getName(), process.getName());
			if (!vo.isFlagNew() && !vo.isFlagRemoved()) {
				vo.flagUpdate();
			}
		} else {
			throw new UnsupportedOperationException(String.format("SystemAttribute \"%s\" is not writable!", attribute.getName()));
		}
	}

	@Override
	public void delete() {
		vo.flagRemove();
	}

	@Override
	public Flag getFlag() {
		if (vo.isFlagNew()) {
			return Flag.INSERT;
		}
		if (vo.isFlagUpdated()) {
			return Flag.UPDATE;
		}
		if (vo.isFlagRemoved()) {
			return Flag.DELETE;
		}
		return Flag.NONE;
	}

	@Override
	public int hashCode() {
		if (vo.getId() == null) {
			return super.hashCode();
		} else {
			return vo.getId().hashCode();
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof EntityObjectImpl) {
			return LangUtils.equals(vo.getId(), ((EntityObjectImpl) obj).getSystemValue(SystemAttribute.ID));
		}
		return super.equals(obj);
	}

	public EntityObjectVO getEntityObjectVO() {
		return this.vo;
	}

	@Override
	public String getEntity() {
		return vo.getEntity();
	}
	
	@Override
	public Collection<EntityObject> getDependants(String entity) {
		return getDependants(entity, false);
	}
	
	@Override
	public Collection<EntityObject> getDependantsWithDeleted(String entity) {
		return getDependants(entity, true);
	}

	private Collection<EntityObject> getDependants(String entity, boolean withDeleted) {
		try {
			if (!Boolean.TRUE.equals(dependantsLoaded.get(entity))) {
				vo.getDependants().addAllData(entity, this.evtSupProvider.getDependants(entity));
				dependantsLoaded.put(entity, true);
			}
		} catch (CommonFatalException e) {
			LOG.error(e.getMessage(), e);
		}
		
		Collection<EntityObjectVO> result = vo.getDependants().getAllData();
		if (!withDeleted) {
			result = CollectionUtils.select(result, new Predicate<EntityObjectVO>() {
				@Override
				public boolean evaluate(EntityObjectVO t) {
					return !t.isFlagRemoved();
				}
			});
		}
		return Collections.unmodifiableCollection(CollectionUtils.transform(result, 
			new Transformer<EntityObjectVO, EntityObject>() {
				@Override
				public EntityObject transform(EntityObjectVO i) {
					return new EntityObjectImpl(vo);
				}
			}));
	}

	@Override
	public EntityObject newDependant(String entity) {
		EntityObjectVO internal = new EntityObjectVO();
		internal.setEntity(entity);
		internal.initFields(10, 5);
		internal.flagNew();
		vo.getDependants().addData(entity, internal);
		return new EntityObjectImpl(internal);
	}
}
