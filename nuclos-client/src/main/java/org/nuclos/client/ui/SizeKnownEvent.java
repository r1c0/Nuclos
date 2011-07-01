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
package org.nuclos.client.ui;

import java.awt.event.ActionEvent;

/**
 * SizeKnownEvent is the ActionEvent a SizeKnownListener is listening to.
 * 
 * @see SizeKnownListener
 * @see JInfoTabbedPane
 * @author Thomas Pasch
 * @since Nuclos 3.1.00
 */
public class SizeKnownEvent extends ActionEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int	SIZE_KNOWN_ID	= 927287;

	private static final String	SIZE_KNOWN_STR	= "SizeKnown";

	private final Integer		size;

	/**
	 * Constructor.
	 * 
	 * @param source of ActionEvent.
	 * @param size to display on the corresponding GUI component (i.e.
	 *            JInfoTabbedPane). If <code>null</code> the component displays
	 *            the 'size is loading' state.
	 */
	public SizeKnownEvent(Object source, Integer size) {
		super(source, SIZE_KNOWN_ID, SIZE_KNOWN_STR);
		this.size = size;
	}

	public Integer getSize() {
		return size;
	}

}
