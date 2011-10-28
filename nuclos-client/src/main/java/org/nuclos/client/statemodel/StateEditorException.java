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
package org.nuclos.client.statemodel;

import org.nuclos.client.gef.ShapeControllerException;

/**
 * State editor exception.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris@novabit.de">Boris</a>
 * @version 01.00.00
 */
public class StateEditorException extends ShapeControllerException {

	public StateEditorException() {
		super();
	}

	/**
	 * @param message
	 */
	public StateEditorException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public StateEditorException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public StateEditorException(Throwable cause) {
		super(cause);
	}
}
