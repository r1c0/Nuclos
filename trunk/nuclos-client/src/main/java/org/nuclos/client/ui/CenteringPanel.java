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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

import org.apache.commons.lang.NullArgumentException;

/**
 * A panel that centers a given component in its bounds.
 * This is far more useful than a plain JPanel with its default <code>FlowLayout</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 * @invariant this.getComponentCount() <= 1
 */
public class CenteringPanel extends JPanel {

	private final boolean bFilled;

	/**
	 * creates a new CenteringPanel. The child to be centered can be added later.
	 * @param bFilled If true, the size of the child is adjusted to <code>this</code>.
	 * If <code>false</code>, the size of the child is not adjusted, but its location is adjusted
	 * so it is always centered in <code>this</code>.
	 * @postcondition this.getComponentCount() == 1
	 * @postcondition this.isFilled() == bFilled
	 */
	public CenteringPanel(boolean bFilled) {
		super(bFilled ? new BorderLayout(0, 0) : new GridBagLayout());

		this.bFilled = bFilled;

		assert this.getComponentCount() == 0;
		assert this.isFilled() == bFilled;
	}

	/**
	 * creates a new CenteringPanel around the given component.
	 * @param compChild
	 * @precondition compChild != null
	 * @postcondition this.getComponentCount() == 1
	 * @postcondition compChild.getParent() == this
	 * @postcondition !this.isFilled()
	 */
	public CenteringPanel(Component compChild) {
		this(compChild, false);

		assert this.getComponentCount() == 1;
		assert compChild.getParent() == this;
		assert !this.isFilled();
	}

	/**
	 * creates a new CenteringPanel around the given component.
	 * @param compChild
	 * @param bFilled If true, the size of the child is adjusted to <code>this</code>.
	 * If <code>false</code>, the size of the child is not adjusted, but its location is adjusted
	 * so it is always centered in <code>this</code>.
	 * @precondition compChild != null
	 * @postcondition this.getComponentCount() == 1
	 * @postcondition compChild.getParent() == this
	 * @postcondition this.isFilled() == bFilled
	 */
	public CenteringPanel(Component compChild, boolean bFilled) {
		this(bFilled);

		if (compChild == null) {
			throw new NullArgumentException("compChild");
		}
		this.add(compChild);

		assert this.getComponentCount() == 1;
		assert compChild.getParent() == this;
		assert this.isFilled() == bFilled;
	}

	/**
	 * @return Is the size of the child adjusted to <code>this</code>?
	 * If <code>false</code>, the size of the child is not adjusted, but its location is adjusted
	 * so it is always centered in <code>this</code>.
	 */
	public boolean isFilled() {
		return this.bFilled;
	}

	/**
	 * sets the given component as the (only) child of <code>this</code>.
	 * @param compChild
	 * @return compChild
	 * @precondition compChild != null
	 * @postcondition result == compChild
	 */
	@Override
	public Component add(Component compChild) {
		this.setCenteredComponent(compChild);
		return compChild;
	}

	/**
	 * @return the centered component, if any.
	 */
	public Component getCenteredComponent() {
		return (this.getComponentCount() == 0) ? null : this.getComponent(0);
	}

	/**
	 * sets compChild as the centered component. Does nothing, if <code>compChild</code> is already the centered component.
	 * @param compChild
	 * @postcondition compChild != null --> compChild.getParent() == this
	 */
	public void setCenteredComponent(Component compChild) {
		if(this.getCenteredComponent() != compChild) {
			this.removeAll();
			if (compChild != null) {
				super.add(compChild, bFilled ? BorderLayout.CENTER : null);
			}
			this.validate();
		}
		assert compChild == null || compChild.getParent() == this;
	}

	@Override
	public Component add(String name, Component comp) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("add");
	}

	@Override
	public Component add(Component comp, int index) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("add");
	}

	@Override
	public void add(Component comp, Object constraints) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("add");
	}

	@Override
	public void add(Component comp, Object constraints, int index) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("add");
	}

}	// class CenteringPanel
