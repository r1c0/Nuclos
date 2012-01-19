// Copyright (C) 2011 Novabit Informationssysteme GmbH
//
// This file is part of Nuclos.
//
// Nuclos is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Nuclos is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with Nuclos. If not, see <http://www.gnu.org/licenses/>.
package org.nuclos.client.ui.layoutml;

import javax.swing.JComponent;

import org.xml.sax.SAXException;

/**
 * ComponentBuilder are used in LayoutMLParser for construction the GUI
 * component tree from the corresponding XML representation.
 * 
 * @see ComponentBuilderStack
 * @see LayoutMLParser
 */
interface ComponentBuilder extends LocalizationHandler {

	/**
	 * @param oConstraints the constraints to use when adding the internal
	 *            component to its parent.
	 * @postcondition this.getConstraints() == oConstraints
	 */
	void setConstraints(Object oConstraints);

	/**
	 * adds <code>comp</code> to the internal component (as in
	 * <code>getComponent()</code>), using the given constraints
	 * 
	 * @param comp
	 * @param oConstraints
	 */
	void add(JComponent comp, Object oConstraints);

	/**
	 * Gets the translation to use (instead of the original text).
	 */
	String getTranslation();

	/**
	 * finishes the internal component
	 * 
	 * @param cbParent
	 */
	void finish(ComponentBuilder cbParent) throws SAXException;

	// was protected before

	/**
	 * @return the internal component (the component to build)
	 */
	JComponent getComponent();

	/**
	 * @return the constraints to use when adding the internal component to its
	 *         parent.
	 */
	Object getConstraints();

}
