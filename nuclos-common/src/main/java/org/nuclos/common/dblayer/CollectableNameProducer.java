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
package org.nuclos.common.dblayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.nuclos.common.collect.collectable.Collectable;

/**
 * @author Thomas Pasch
 * @since Nuclos 3.3.0
 */
public class CollectableNameProducer implements INameProducer<Collectable> {
	
	private static final Pattern REF_PATTERN = Pattern.compile("\\$\\{(\\p{Alpha}[\\p{Alnum}_]*)\\}", Pattern.MULTILINE);
	
	//
	
	private final List<IFieldRef> refs;
	
	public CollectableNameProducer(String name) {
		final FieldRefIterator fri = new FieldRefIterator(REF_PATTERN, name);
		final List<IFieldRef> r = new ArrayList<IFieldRef>();
		while (fri.hasNext()) {
			r.add(fri.next());
		}
		refs = Collections.unmodifiableList(r);
	}
	
	public String makeName(Collectable c) {
		final StringBuilder result = new StringBuilder();
		for (IFieldRef r: refs) {
			if (r.isConstant()) {
				result.append(r.getContent());
			}
			else {
				result.append(c.getValue(r.getContent()));
			}
		}
		return result.toString();
	}

}
