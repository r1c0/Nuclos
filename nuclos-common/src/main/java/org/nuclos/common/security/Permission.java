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
package org.nuclos.common.security;

/**
 * A permission to read or write something.
 * Note that the order of these constants is significant and may be used for comparing permissions: NONE &lt; READONLY &lt; READWRITE.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @version 01.00.00
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 */
public enum Permission {

	/**
	 * May neither read nor write. This is often (but not always) represented as 
	 * <code>null</code>. However the value is useful as value part of a 
	 * ConcurrentHashMap (where null is not allowed).
	 */
	NONE("<Keine>"),

	/**
	 * may read, but not write.
	 */
	READONLY("Nur Lesen"),

	/**
	 * may read and write.
	 */
	READWRITE("Lesen/Schreiben");

	private final String sLabel;

	Permission(String sLabel) {
		this.sLabel = sLabel;
	}

	public String getLabel() {
		return this.sLabel;
	}

	@Override
	public String toString() {
		return this.getLabel();
	}

	/**
	 * @return Does this permission include the right to read?
	 */
	public boolean includesReading() {
		return this != NONE;
	}

	/**
	 * @return Does this permission include the right to write?
	 */
	public boolean includesWriting() {
		return this == READWRITE;
	}

	public Permission max(Permission other) {
		return (other != null && other.ordinal() > this.ordinal()) ? other : this;
	}
	
	/**
	 * @param bMayRead	Is reading allowed?
	 * @param bMayWrite Is writing allowed?
	 * @return the permission representing the combination of reading and writing.
	 * @precondition bMayWrite --> bMayRead
	 * @postcondition result != null
	 * @postcondition result.includesReading() == (bMayRead || bMayWrite)
	 * @postcondition result.includesWriting() == bMayWrite
	 */
	public static Permission getPermission(boolean bMayRead, boolean bMayWrite) {
		if (bMayWrite && !bMayRead) {
			throw new IllegalArgumentException();
		}

		// Note that it doesn't matter if we call getPermissionReadingOverrides or getPermissionWritingOverrides here.
		final Permission result = getPermissionReadingOverrides(bMayRead, bMayWrite);

		assert result != null;
		assert result.includesReading() == (bMayRead || bMayWrite);
		assert result.includesWriting() == bMayWrite;

		return result;
	}

	/**
	 * @param bMayRead	Is reading allowed? The right to read is a requirement for the right to write. If reading isn't allowed, bMayWrite is ignored.
	 * @param bMayWrite Is writing allowed?
	 * @return the permission representing the combination of reading and writing.
	 * @postcondition result != null
	 * @postcondition result.includesReading() == bMayRead
	 * @postcondition result.includesWriting() == (bMayRead && bMayWrite)
	 */
	public static Permission getPermissionReadingOverrides(boolean bMayRead, boolean bMayWrite) {
		final Permission result = (bMayRead ? (bMayWrite ? READWRITE : READONLY) : NONE);

		assert result != null;
		assert result.includesReading() == bMayRead;
		assert result.includesWriting() == (bMayRead && bMayWrite);

		return result;
	}

	/**
	 * @param bMayRead	Is reading allowed?
	 * @param bMayWrite Is writing allowed? The right to write always implies the right to read. If writing is allowed, bMayRead is ignored.
	 * @return the permission representing the combination of reading and writing.
	 * @postcondition result != null
	 * @postcondition result.includesReading() == (bMayRead || bMayWrite)
	 * @postcondition result.includesWriting() == bMayWrite
	 */
	public static Permission getPermissionWritingOverrides(boolean bMayRead, boolean bMayWrite) {
		final Permission result = (bMayWrite ? READWRITE : (bMayRead ? READONLY : NONE));

		assert result != null;
		assert result.includesReading() == (bMayRead || bMayWrite);
		assert result.includesWriting() == bMayWrite;

		return result;
	}

}	// enum Permission
