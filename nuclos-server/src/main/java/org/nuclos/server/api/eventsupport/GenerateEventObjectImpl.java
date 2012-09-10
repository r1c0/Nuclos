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
package org.nuclos.server.api.eventsupport;

import java.util.Collection;
import java.util.Map;

import org.nuclos.api.EntityObject;
import org.nuclos.api.Generator;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class GenerateEventObjectImpl extends AbstractEventObjectWithCache implements org.nuclos.api.eventsupport.GenerateEventObject {
	
	private final Generator generator;
	private final Collection<EntityObject> sourceObjects;
	private final EntityObject targetObject;
	private final EntityObject parameterObject;
	
	public GenerateEventObjectImpl(Map<String, Object> eventCache, Generator generator, Collection<EntityObject> sourceObjects, EntityObject targetObject, EntityObject parameterObject) {
		super(eventCache);
		
		this.generator = generator;
		this.sourceObjects = sourceObjects;
		this.targetObject = targetObject;
		this.parameterObject = parameterObject;
	}

	@Override
	public Generator getGenerator() {
		return generator;
	}


	@Override
	public Collection<EntityObject> getSourceObjects(){
		return sourceObjects;
	}

	@Override
	public EntityObject getTargetObject() {
		return targetObject;
	}

	@Override
	public EntityObject getParameterObject() {
		return parameterObject;
	}

}
