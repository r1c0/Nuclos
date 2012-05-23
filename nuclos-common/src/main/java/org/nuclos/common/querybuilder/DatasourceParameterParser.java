package org.nuclos.common.querybuilder;

import java.util.Iterator;
import java.util.regex.Pattern;

import org.nuclos.common.dblayer.FieldRefIterator;
import org.nuclos.common.dblayer.IFieldRef;

public class DatasourceParameterParser implements Iterable<IFieldRef> {
	
	private static final Pattern PARAM_PATTERN = Pattern.compile("\\$(\\p{Alnum}[\\p{Alnum}_]*)", Pattern.MULTILINE);
	
	//
	
	private final String sql;
	
	public DatasourceParameterParser(String sql) {
		this.sql = sql;
	}

	@Override
	public Iterator<IFieldRef> iterator() {
		return new FieldRefIterator(PARAM_PATTERN, sql);
	}

}
