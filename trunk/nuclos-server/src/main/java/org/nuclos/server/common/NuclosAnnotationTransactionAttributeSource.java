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
package org.nuclos.server.common;

import java.lang.reflect.AnnotatedElement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;

@Component
public class NuclosAnnotationTransactionAttributeSource extends AnnotationTransactionAttributeSource {
	
	private NuclosRemoteContextHolder ctx;
	
	public NuclosAnnotationTransactionAttributeSource() {
	}

	@Autowired
	void setNuclosRemoteContextHolder(NuclosRemoteContextHolder ctx) {
		this.ctx = ctx;
	}

	@Override
	protected TransactionAttribute determineTransactionAttribute(AnnotatedElement ae) {
		
		return new DefaultTransactionAttribute() {

			@Override
			public boolean rollbackOn(Throwable ex) {
				return Boolean.TRUE.equals(ctx.peek());
			}
			
		};
		
	}

}
