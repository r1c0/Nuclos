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
package org.nuclos.tools.ruledoc.javaToHtml;

import java.util.*;

public abstract class HorizontalAlignment {
	private final static Map<String, HorizontalAlignment> byName = new HashMap<String, HorizontalAlignment>();
	private final static List<HorizontalAlignment> all = new ArrayList<HorizontalAlignment>();

	public final static HorizontalAlignment LEFT = new HorizontalAlignment("left") {
		@Override
		public void accept(IHorizontalAlignmentVisitor visitor) {
			visitor.visitLeftAlignment(this);
		}
	};
	public final static HorizontalAlignment CENTER = new HorizontalAlignment("center") {
		@Override
		public void accept(IHorizontalAlignmentVisitor visitor) {
			visitor.visitCenterAlignment(this);
		}
	};
	public final static HorizontalAlignment RIGHT = new HorizontalAlignment("right") {
		@Override
		public void accept(IHorizontalAlignmentVisitor visitor) {
			visitor.visitRightAlignment(this);
		}
	};

	public static HorizontalAlignment getByName(String name) {
		return byName.get(name);
	}

	public static HorizontalAlignment[] getAll() {
		return all.toArray(new HorizontalAlignment[all.size()]);
	}

	private String name;

	public HorizontalAlignment(String name) {
		this.name = name;
		byName.put(name, this);
		all.add(this);
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "HorizontalAlignment{" + getName() + "}";
	}

	public abstract void accept(IHorizontalAlignmentVisitor visitor);

}
