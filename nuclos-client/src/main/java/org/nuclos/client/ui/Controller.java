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
package org.nuclos.client.ui;

import org.nuclos.client.main.Main;
import org.nuclos.client.main.MainController;
import org.nuclos.common2.SpringLocaleDelegate;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * General controller (as in "model-view-controller").
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
//@Configurable(preConstruction=true)
public abstract class Controller<Parent> {
	
	private Parent parent;
	
	private MainController mainController;
	
	public Controller(Parent parent) {
		this.parent = parent;
	}
	
	public void setParent(Parent parent) {
		if (this.parent != null) {
			throw new IllegalArgumentException("Parent already set");
		}
		this.parent = parent;
	}
		
	protected SpringLocaleDelegate getSpringLocaleDelegate() {
		return SpringLocaleDelegate.getInstance();
	}

	protected MainController getMainController() {
		if (mainController == null) {
			mainController = Main.getInstance().getMainController();
		}
		return mainController;
	}

	/**
	 * @return the parent component of this controller, if any. This is generally used for issuing messages, as long as
	 * the controller doesn't create its own GUI or as parent for the controller's own GUI.
	 */
	public Parent getParent() {
		if (parent == null) {
			throw new IllegalArgumentException("Parent not set");
		}
		return parent;
	}

}  // class Controller
