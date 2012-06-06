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
package org.nuclos.client.layout.wysiwyg.editor.util.valueobjects;

import java.io.Serializable;
import java.util.Vector;

/**
 * This Class collects multiple Properties.
 * 
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class WYSIYWYGProperty implements Cloneable, Serializable{
	
	public Vector<WYSIYWYGPropertySet> properties = null;

	public WYSIYWYGProperty(){
		properties = new Vector<WYSIYWYGPropertySet>(1);
	}
	
	/** 
	 * Add a {@link WYSIYWYGPropertySet} to this {@link WYSIYWYGProperty}
	 * @param wysiwygPropertySet to add
	 */
	public void addWYSIYWYGPropertySet(WYSIYWYGPropertySet wysiwygPropertySet){
		for (WYSIYWYGPropertySet propertySet : new Vector<WYSIYWYGPropertySet>(properties)) {
			if (propertySet.getPropertyName().equals(wysiwygPropertySet.getPropertyName()))
				removeWYSIYWYGPropertySet(propertySet);
		}
		properties.add(wysiwygPropertySet);
	}
	
	/**
	 * Remove a {@link WYSIYWYGPropertySet} from this {@link WYSIYWYGProperty}
	 * @param wysiwygPropertySet to remove
	 */
	public void removeWYSIYWYGPropertySet(WYSIYWYGPropertySet wysiwygPropertySet){
		properties.remove(wysiwygPropertySet);
	}
	
	/** 
	 * @return all {@link WYSIYWYGPropertySet} stored in this {@link WYSIYWYGProperty}
	 */
	public Vector<WYSIYWYGPropertySet> getAllPropertyEntries(){
		return this.properties;
	}
	
	/**
	 * @return the Number of {@link WYSIYWYGPropertySet} stored
	 */
	public int getSize() {
		return properties.size();
	}
	
	/** 
	 * Overwritten clone Method to create a new Instance of this {@link WYSIYWYGProperty}.
	 * calls {@link WYSIYWYGPropertySet#clone()}
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		WYSIYWYGProperty clonedProperty = new WYSIYWYGProperty();
		
		for (WYSIYWYGPropertySet propertySet : properties) {
			clonedProperty.addWYSIYWYGPropertySet((WYSIYWYGPropertySet)propertySet.clone());
		}
		
		return clonedProperty ;
	}
}
