package org.nuclos.server.dblayer.impl.standard;

import java.util.HashMap;
import java.util.Map;

import org.nuclos.server.dblayer.structure.DbSimpleView.DbSimpleViewColumn;

public final class TableAliasForSimpleView {
	
	private final Map<DbSimpleViewColumn, String> ref2Alias = new HashMap<DbSimpleViewColumn, String>();
	
	private int index = 0;
	
	public TableAliasForSimpleView() {
	}
	
	public String getTableAlias(DbSimpleViewColumn ref) {
		if (ref.getReference() == null) {
			throw new IllegalStateException();
		}
		String result = ref2Alias.get(ref);
		if (result == null) {
			result = "fk" + ++index;
			ref2Alias.put(ref, result);
		}
		return result;
	}

}
