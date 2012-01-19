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
package org.nuclos.common.collect.collectable.access;

import org.nuclos.common.collect.collectable.Collectable;


/**
 * inner class <code>CollectableEntityFieldSecurityAgent</code>:
 * checks the permission (read, write, delete) for this <code>CollectableEntityField</code> according to the <code>Collectable</code>.
 */
public class CefAllowAllSecurityAgentImpl implements CefSecurityAgent {
	
	private Collectable clct;
	
	public CefAllowAllSecurityAgentImpl() {
	}

	/**
	 * sets the <code>Collectable</code> for the <code>CollectableEntityField</code>
	 * @param Collectable
	 */
	public void setCollectable(Collectable clct) {
		this.clct = clct;
	}

	/**
	 * @return the <code>Collectable</code> of the <code>CollectableEntityField</code>
	 */
	public Collectable getCollectable() {
		return clct;
	}

	/**
	 * you may use and overwrite this method for your own purpose
	 * @return true if read permission is granted to this field according to the <code>Collectable</code> otherwise false;
	 * in this default implementation return always true
	 */
	public boolean isReadable() {
		return true;
	}

	/**
	 * you may use and overwrite this method for your own purpose
	 * @return true if write permission is granted to this field according to the <code>Collectable</code> otherwise false;
	 * in this default implementation return always true
	 */
	public boolean isWritable() {
		return true;
	}

	/**
	 * you may use and overwrite this method for your own purpose
	 * @return true if delete permission is granted to this field according to the <code>Collectable</code> otherwise false;
	 * in this default implementation return always true
	 */
	public boolean isRemovable() {
		return true;
	}
	
} // class CollectableEntityFieldSecurityAgent

