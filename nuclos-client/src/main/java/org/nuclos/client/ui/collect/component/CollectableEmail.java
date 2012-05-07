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
package org.nuclos.client.ui.collect.component;

import org.apache.log4j.Logger;
import org.nuclos.client.ui.HyperlinkTextFieldWithButton;
import org.nuclos.client.ui.labeled.LabeledComponentSupport;
import org.nuclos.client.ui.labeled.LabeledEmail;
import org.nuclos.common.collect.collectable.CollectableEntityField;

/**
 * <code>CollectableComponent</code> to display/enter an Email.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:maik.stueker@nuclos.de">Maik Stueker</a>
 * @version	01.00.00
 */
public class CollectableEmail extends CollectableHyperlink {

	private static final Logger LOG = Logger.getLogger(CollectableEmail.class);
	
	/**
	 * @param clctef
	 * @postcondition this.isDetailsComponent()
	 */
	public CollectableEmail(CollectableEntityField clctef) {
		this(clctef, false);
		this.overrideActionMap();

		assert this.isDetailsComponent();
	}

	/**
	 * @param clctef
	 * @param bSearchable
	 */
	public CollectableEmail(CollectableEntityField clctef, boolean bSearchable) {
		super(clctef, bSearchable, 
				new LabeledEmail(new LabeledComponentSupport(),
						clctef.isNullable(), bSearchable));
	}

	public HyperlinkTextFieldWithButton getEmail() {
		return ((LabeledEmail) getJComponent()).getEmail();
	}
	
	

}  // class CollectableEmail
