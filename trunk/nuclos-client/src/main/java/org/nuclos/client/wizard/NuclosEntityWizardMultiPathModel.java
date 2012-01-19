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
package org.nuclos.client.wizard;

import org.pietschy.wizard.models.MultiPathModel;
import org.pietschy.wizard.models.Path;

/**
* <br>
* Created by Novabit Informationssysteme GmbH <br>
* Please visit <a href="http://www.novabit.de">www.novabit.de</a>
* 
* @author <a href="mailto:marc.finke@novabit.de">Marc Finke</a>
* @version 01.00.00
*/

public class NuclosEntityWizardMultiPathModel extends MultiPathModel {
	
	String entityName;
	String labelSingular;
	String labelPlural;

	public NuclosEntityWizardMultiPathModel(Path path) {
		super(path);
	}
	
	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public String getLabelSingular() {
		return labelSingular;
	}

	public void setLabelSingular(String labelSingular) {
		this.labelSingular = labelSingular;
	}

	public String getLabelPlural() {
		return labelPlural;
	}

	public void setLabelPlural(String labelPlural) {
		this.labelPlural = labelPlural;
	}
	
	

}
