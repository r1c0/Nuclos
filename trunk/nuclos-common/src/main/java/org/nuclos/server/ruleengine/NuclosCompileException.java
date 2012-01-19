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
package org.nuclos.server.ruleengine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.nuclos.common2.exception.CommonBusinessException;

/**
 * Nucleus compile exception.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Lars.Rueckemann@novabit.de">Lars Rueckemann</a>
 * @version 01.00.00
 */
public class NuclosCompileException extends CommonBusinessException {

	private final List<ErrorMessage> messages;

	public NuclosCompileException(Throwable t) {
		super(t);
		this.messages = null;
	}

	public NuclosCompileException(String message) {
		super(message);
		this.messages = null;
	}

	public NuclosCompileException(List<ErrorMessage> messages) {
		this("ruleengine.error.exception.nucleuscompileexception", messages);
	}

	public NuclosCompileException(String resId, List<ErrorMessage> messages) {
		super(resId);
		this.messages = (messages != null) ? new ArrayList<ErrorMessage>(messages) : null;
	}

	public List<ErrorMessage> getErrorMessages() {
		return (messages != null) ? messages : Collections.<ErrorMessage>emptyList();
	}

	public static class ErrorMessage implements javax.tools.Diagnostic<String>, Serializable {

		private final Kind kind;
		private final String source;
		private final String message;
		private final long position, startPosition, endPosition;
		private final long lineNumber, columnNumber;
		private final String entityname;
		private final long id;

		public ErrorMessage(Kind kind, String source, String message) {
			this.kind = kind;
			this.source = source;
			this.message = message;
			this.position = this.startPosition = this.endPosition = NOPOS;
			this.lineNumber = this.columnNumber = NOPOS;
			this.entityname = null;
			this.id = -1;
		}

		public ErrorMessage(Kind kind, String source, String message, String entityname, long id, long lineNumber, long columnNumber, long position, long startPosition, long endPosition) {
			this.kind = kind;
			this.source = source;
			this.message = message;
			this.lineNumber = lineNumber;
			this.columnNumber = columnNumber;
			this.position = position;
			this.startPosition = startPosition;
			this.endPosition = endPosition;
			this.entityname = entityname;
			this.id = id;
		}

		@Override
		public Kind getKind() {
			return kind;
		}

		@Override
		public String getSource() {
			return source;
		}

		@Override
		public long getPosition() {
			return position;
		}

		@Override
		public long getStartPosition() {
			return startPosition;
		}

		@Override
		public long getEndPosition() {
			return endPosition;
		}

		@Override
		public long getLineNumber() {
			return lineNumber;
		}

		@Override
		public long getColumnNumber() {
			return columnNumber;
		}

		@Override
		public String getCode() {
			return null;
		}

		@Override
		public String getMessage(Locale locale) {
			return message;
		}

		public String getEntityname() {
			return this.entityname;
		}

		public Long getId() {
			return this.id;
		}

		@Override
		public String toString() {
			return String.format("%s:%d: %s", source, lineNumber, message);
		}
	}
}
