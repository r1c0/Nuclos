//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.server.dblayer.util;

import java.util.Iterator;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dblayer.FieldRefIterator;
import org.nuclos.common.dblayer.IFieldRef;

/**
 * Parser for the expression language used in field references.
 *  
 * @author Thomas Pasch
 * @since Nuclos 3.2.01
 * @see org.nuclos.common.dal.vo.EntityFieldMetaDataVO#getForeignEntityField()
 */
public class ForeignEntityFieldParser implements Iterable<IFieldRef> {
	
	private static final Logger LOG = Logger.getLogger(ForeignEntityFieldParser.class);
	
	private static final Pattern OLD_SIMPLE_REF_PATTERN = Pattern.compile("\\p{Alpha}[\\p{Alnum}_]*");
	
	private static final String DEFAULT_FOREIGN_ENTITY_FIELD = "${name}";
	
	private static final Pattern REF_PATTERN = Pattern.compile("\\$\\{(\\p{Alpha}[\\p{Alnum}_]*)\\}", Pattern.MULTILINE);
	
	// 
	
	private final String ffieldname;
	
	public ForeignEntityFieldParser(EntityFieldMetaDataVO foreignEntityField) {
		if (foreignEntityField == null) {
			throw new NullPointerException();
		}
		// this.foreignEntityField = foreignEntityField;
		
		String fef = null;
		if (foreignEntityField.getForeignEntity() != null)
			fef = foreignEntityField.getForeignEntityField();
		else if (foreignEntityField.getLookupEntity() != null)
			fef = foreignEntityField.getLookupEntityField();
		
		if (fef == null) {
			LOG.debug("Null/empty foreignEntityField in expression in " + foreignEntityField 
					+ " is deprecated, use " + DEFAULT_FOREIGN_ENTITY_FIELD + " instead!");
			this.ffieldname = DEFAULT_FOREIGN_ENTITY_FIELD;
		}
		else if (OLD_SIMPLE_REF_PATTERN.matcher(fef).matches()) {
			LOG.debug("Old style foreignEntityField expression '" + fef + "' in "
					+ foreignEntityField + " is deprecated, please enclose it: ${" + fef + "}");
			this.ffieldname = "${" + fef + "}";
		}
		else {
			this.ffieldname = fef;
		}
	}

	@Override
	public Iterator<IFieldRef> iterator() {
		return new FieldRefIterator(REF_PATTERN, ffieldname);
	}

	public static void main(String[] args) {
		// final String test = "prefix${a}${test} -\n ${main} hiho ${error} ->";
		// final String test = "${a}$b{";
		final String test = "test";
		System.out.println("Parse: '" + test + "'");
		final EntityFieldMetaDataVO field = new EntityFieldMetaDataVO();
		field.setForeignEntityField(test);
		field.setForeignEntity("foreignEntity");
		field.setField("fieldRefToForeignEntityField");
		for (IFieldRef r: new ForeignEntityFieldParser(field)) {
			System.out.println(r);
		}
		System.out.println("ForeignEntityFieldParser main ended");
	}

}
