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
package org.nuclos.client.layout.wysiwyg.datatransfer;

import java.io.Serializable;

import org.nuclos.common2.layoutml.LayoutMLConstants;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableLabel;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;

/**
 * Small Class for Use with {@link TransferableElement}.<br>
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
@SuppressWarnings("serial")
public class DragElement implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String element;
	private String controltype;
	//NUCLEUSINT-496
	private boolean labeledComponent;
	
	/**
	 * The Constructor
	 * @param element
	 * @param controltype
	 */
	public DragElement(String element, String controltype, boolean labeledComponent) {
		this.element = element;
		this.controltype = controltype;
		//NUCLEUSINT-496
		this.labeledComponent = labeledComponent;
	}
	
	/**
	 * @return the Element Tag (e.g. {@link LayoutMLConstants#ELEMENT_CHECKBOX}}
	 */
	public String getElement() {
		return element;
	}

	/**
	 * @return the Controltyoe for the Component
	 */
	public String getControltype() {
		return controltype;
	}
	
	/**
	 * @return true if is {@link WYSIWYGComponent} with {@link WYSIWYGCollectableLabel}
	 * NUCLEUSINT-496
	 */
	public boolean isLabeledComponent() {
		return labeledComponent;
		
	}
	
}
