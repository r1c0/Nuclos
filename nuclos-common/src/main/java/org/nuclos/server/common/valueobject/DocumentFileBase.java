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
package org.nuclos.server.common.valueobject;

import org.nuclos.common2.File;
import org.nuclos.common2.LangUtils;
import org.nuclos.common.NuclosFatalException;
import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;

/**
 * DocumentFile that loads its contents lazily.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:uwe.allner@novabit.de">uwe.allner</a>
 * @version 01.00.00
 */
public abstract class DocumentFileBase extends File {

	private final static Logger log = Logger.getLogger(DocumentFileBase.class);

	private final Integer iDocumentFileId;
	private final boolean bContentsChanged;

	/**
	 * @param sFileName
	 * @param iDocumentFileId
	 * @precondition iDocumentFileId != null
	 * @postcondition this.contents == null
	 * @postcondition !this.getContentsChanged()
	 */
	public DocumentFileBase(String sFileName, Integer iDocumentFileId) {
		super(sFileName);
		if (iDocumentFileId == null) {
			throw new NullArgumentException("iDocumentFileId");
		}
		this.iDocumentFileId = iDocumentFileId;
		this.bContentsChanged = false;

		assert this.contents == null;
		assert !this.getContentsChanged();
	}
	
	public DocumentFileBase(String sFileName, Integer iDocumentFileId, byte[] abContents) {
		super(sFileName);
		if (iDocumentFileId == null) {
			throw new NullArgumentException("iDocumentFileId");
		}
		this.iDocumentFileId = iDocumentFileId;
		
	  this.contents = abContents;
	  this.bContentsChanged = true;
	}
	/**
	 * @param sFileName
	 * @param abContents
	 * @precondition abContents != null
	 * @postcondition this.getContentsChanged()
	 */
	public DocumentFileBase(String sFileName, byte[] abContents) {
		super(sFileName, abContents);

		this.iDocumentFileId = null;
		this.bContentsChanged = true;

		assert this.getContentsChanged();
	}

	public Integer getDocumentFileId() {
		return this.iDocumentFileId;
	}

	@Override
	public final byte[] getContents() {
		if (this.contents == null) {
			if (this.getDocumentFileId() != null) {
				log.debug("BEGIN getting stored contents.");
				this.contents = this.getStoredContents();
				log.debug("FINISHED getting stored contents.");
			}
			else {
				throw new NuclosFatalException();
			}
		}
		return contents;
	}

	/**
	 * gets the contents of this file from the (remote) storage.
	 * Note that this is generally not the local file system.
	 * @return the loaded contents.
	 */
	protected abstract byte[] getStoredContents();

	/**
	 * @return Have the contents changed? Must this file be stored?
	 */
	public boolean getContentsChanged() {
		return this.bContentsChanged;
	}

	@Override
	public boolean equals(Object o) {
		final boolean result;
		if (!(o instanceof DocumentFileBase)) {
			result = super.equals(o);
		}
		else {
			if (this == o) {
				result = true;
			}
			else {
				final DocumentFileBase that = (DocumentFileBase) o;
				result = LangUtils.equals(this.getFilename(), that.getFilename()) &&
						LangUtils.equals(this.getFiletype(), that.getFiletype()) &&
						this.areContentsEqual(that);
			}
		}
		return result;
	}

	/**
	 * @param that
	 * @return Are the contents of this and that equal? true, if both haven't been fetched from the (remote) storage,
	 * Otherwise, the contents are compared on a byte-per-byte basis.
	 */
	private boolean areContentsEqual(DocumentFileBase that) {
		return (this.contents == null && that.contents == null) ?
				true : org.apache.commons.lang.ArrayUtils.isEquals(this.getContents(), that.getContents());
	}

}	// class DocumentFileBase
