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
package org.nuclos.client.layout.wysiwyg.palette;

import javax.swing.Icon;

import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.PaletteItemElement;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableLabel;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;

/**
 * PaletteItem represents a element in the palette
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:thomas.schiffmann@novabit.de">thomas.schiffmann</a>
 * @version 01.00.00
 */
public class PaletteItem {

	/**
	 * the icon of a paletteitem
	 */
	private Icon icon;

	/**
	 * the name of the element
	 */
	private String element;

	/**
	 * the optional controltype of an element
	 */
	private String controltype;

	/**
	 * the label to show in palette
	 */
	private String label;

	/**
	 * the tooltip of the list item
	 */
	private String tooltip;

	private boolean displayLabelAndIcon = false;
	
	/**
	 * is the item a {@link WYSIWYGComponent} and a {@link WYSIWYGCollectableLabel}
	 * NUCLEUSINT-496
	 */
	private boolean labeledComponent = false;

	/* protected Class<?> classForIcon = JTextField.class; */

	/**
	 * ctor
	 * 
	 * @param element elementname
	 * @param desc elementdescription (from nodedescriptor)
	 */
	public PaletteItem(String element, Object paletteItemElement) {
		this.element = element;
		this.icon = ((PaletteItemElement) paletteItemElement).getIcon();
		this.label = ((PaletteItemElement) paletteItemElement).getLabel();
		this.tooltip = ((PaletteItemElement) paletteItemElement).getToolTip();
		this.displayLabelAndIcon = ((PaletteItemElement) paletteItemElement).displayLabelAndIcon();
		this.labeledComponent = ((PaletteItemElement) paletteItemElement).isLabeledComponent();
	}

	/**
	 * ctor
	 * 
	 * @param element
	 * @param desc
	 * @param controltype
	 */
	public PaletteItem(String element, String controltype, Object paletteItemElement) {
		this(element, paletteItemElement);
		this.controltype = controltype;
		this.labeledComponent = ((PaletteItemElement) paletteItemElement).isLabeledComponent();
	}

	/**
	 * 
	 * @return
	 */
	public String getElement() {
		return element;
	}

	/**
	 * 
	 * @return
	 */
	public Icon getIcon() {
		return icon;
	}

	/**
	 * 
	 * @return
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * 
	 * @return
	 */
	public String getTooltip() {
		return tooltip;
	}

	/**
	 * 
	 * @return
	 */
	public String getControltype() {
		return controltype;
	}

	/**
	 * 
	 * @return
	 */
	public boolean displayLabelAndIcon() {
		return displayLabelAndIcon;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isLabeledComponent() {
		return labeledComponent;		
	}
}
