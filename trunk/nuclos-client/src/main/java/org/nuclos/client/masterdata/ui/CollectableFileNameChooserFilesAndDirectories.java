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
package org.nuclos.client.masterdata.ui;

import javax.swing.JFileChooser;

import org.nuclos.common.collect.collectable.CollectableEntityField;

/**
 *  A Collectable with a button opening a file chooser component, which shows files and directories, and stores the file name and path
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:uwe.allner@novabit.de">uwe.allner</a>
 * @version 01.00.00
 */
public class CollectableFileNameChooserFilesAndDirectories extends CollectableFileNameChooserBase {
	public CollectableFileNameChooserFilesAndDirectories(CollectableEntityField clctef, Boolean bSearchable) {
		super(clctef, bSearchable.booleanValue());
	}

	@Override
	protected void configureFileSelection(final JFileChooser filechooser) {
		filechooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	}
}
