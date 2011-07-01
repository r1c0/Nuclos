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
package org.nuclos.installer.mode;

import org.nuclos.installer.InstallException;
import org.nuclos.installer.unpack.Unpacker;

/**
 * Installer mode (collect information)
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.nuclos.de">www.nuclos.de</a>
 */
public interface Installer {

	public final static int QUESTION_YESNO = 0;
	public final static int QUESTION_OKCANCEL = 1;

	public final static int ANSWER_YES = 0;
	public final static int ANSWER_NO = 1;
	public final static int ANSWER_OK = 2;
	public final static int ANSWER_CANCEL = 3;

	public void install(Unpacker os) throws InstallException;

	public void uninstall(Unpacker os) throws InstallException;

	public void info(String resid, Object...args);

	public void warn(String resid, Object...args);

	public void error(String resid, Object...args);

	public int askQuestion(String text, int questiontype, int automatedAnswer, Object...args);

	public void close();

	public void cancel();

}
