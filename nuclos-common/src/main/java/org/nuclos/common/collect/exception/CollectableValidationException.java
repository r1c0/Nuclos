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
package org.nuclos.common.collect.exception;

import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonValidationException;

/**
 * Exception that might occur during the validation of a <code>Collectable</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class CollectableValidationException extends CommonValidationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final CollectableEntityField clctef;

	/**
	 * @param clctef the offending entity field. May be <code>null</code> if the invalid state cannot be blamed on one field alone.
	 * @param tCause
	 */
	public CollectableValidationException(CollectableEntityField clctef, Throwable tCause) {
		super(tCause);
		this.clctef = clctef;
	}

	/**
	 * @return the offending entity field (if any)
	 */
	public CollectableEntityField getCollectableEntityField() {
		return this.clctef;
	}

	@Override
	public String getMessage() {
		final CollectableEntityField clctefInvalid = this.getCollectableEntityField();

		final StringBuilder sb = new StringBuilder("");
		if (clctefInvalid != null) {
			final String sLabel = clctefInvalid.getLabel();
			final String sName = clctefInvalid.getName();

			if (sLabel == null || sLabel.length() == 0) {
				sb.append(StringUtils.getParameterizedExceptionMessage("collectable.field.validation.exception.2", sName));
			}
			else {
				sb.append(StringUtils.getParameterizedExceptionMessage("collectable.field.validation.exception.1", sLabel));
			}
//			sb.append("Das Feld ");
//			if (sLabel == null || sLabel.length() == 0) {
//				sb.append("mit der internen Bezeichnung ");
//				sb.append(sName);
//			}
//			else {
//				sb.append('\"');
//				sb.append(sLabel);
//				sb.append('\"');
////			if(!sLabel.equals(sName)) {
////				sb.append(" (interne Bezeichnung: ");
////				sb.append(sName);
////				sb.append(')');
////			}
//			}
//			sb.append(" hat keinen g\u00fcltigen Wert.");
		}

		final String sCauseMessage = this.getCause().getLocalizedMessage();
		if (!StringUtils.isNullOrEmpty(sCauseMessage)) {
			if (sb.length() > 0) {
				sb.append('\n');
			}
			sb.append(sCauseMessage);
		}

		return sb.toString();
	}

}	// class CollectableValidationException
