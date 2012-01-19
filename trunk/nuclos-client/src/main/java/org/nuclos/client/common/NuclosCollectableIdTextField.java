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
package org.nuclos.client.common;

import org.nuclos.client.ui.collect.component.CollectableIdTextField;
import org.nuclos.common.collect.collectable.CollectableEntityField;

/**
 * Custom <code>CollectableIdTextField</code> for Nucleus. The referencing listener is installed right here.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
public class NuclosCollectableIdTextField extends CollectableIdTextField {

	public NuclosCollectableIdTextField(CollectableEntityField clctef, Boolean bSearchable) {
		super(clctef, bSearchable);

		this.setReferencingListener(NuclosLOVListener.getInstance());
	}

}	// class NuclosCollectableIdTextField
