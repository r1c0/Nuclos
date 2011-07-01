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
package org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules;

import java.io.Serializable;

import org.nuclos.common2.layoutml.LayoutMLConstants;

/**
 * This Method represents {@link LayoutMLConstants#ELEMENT_EVENT}
 * Valid Events are:
 * <ul>
 * 	<li> {@link LayoutMLConstants#ATTRIBUTEVALUE_LOOKUP}</li>
 *  <li> {@link LayoutMLConstants#ATTRIBUTEVALUE_VALUECHANGED}</li>
 * </ul>
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
@SuppressWarnings("serial")
public class LayoutMLRuleEventType implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String LOOKUP = LayoutMLConstants.ATTRIBUTEVALUE_LOOKUP;
	public static final String VALUE_CHANGED = LayoutMLConstants.ATTRIBUTEVALUE_VALUECHANGED;

	private String eventType = null;

	private String entity = null;
	private String sourceComponent = null;
	
	/**
	 * Valid Events are:
	 * <ul>
	 * 	<li> {@link LayoutMLConstants#ATTRIBUTEVALUE_LOOKUP}</li>
	 *  <li> {@link LayoutMLConstants#ATTRIBUTEVALUE_VALUECHANGED}</li>
	 * </ul>
	 * @param eventType to Set
	 */
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	/**
	 * Events are:
	 * <ul>
	 * 	<li> {@link LayoutMLConstants#ATTRIBUTEVALUE_LOOKUP}</li>
	 *  <li> {@link LayoutMLConstants#ATTRIBUTEVALUE_VALUECHANGED}</li>
	 * </ul>
	 * @return the EventType set
	 */
	public String getEventType() {
		return eventType;
	}

	/**
	 * Setting the Entity if SourceComponent is a SubForms
	 * @param entity the SubformEntity
	 */
	public void setEntity(String entity) {
		this.entity = entity;
	}

	/**
	 * @return the SubformEntity (may be <b>null</b>)
	 */
	public String getEntity() {
		return entity;
	}
	
	/**
	 * Setting the SourceComponent, is a SubformColumn if the Entity is set
	 * @param sourceComponent
	 * @see #setEntity(String)
	 * @see #getEntity()
	 */
	public void setSourceComponent(String sourceComponent) {
		this.sourceComponent = sourceComponent;
	}

	/**
	 * @return the SourceComponent set (may be <b>null</b>)
	 */
	public String getSourceComponent() {
		return sourceComponent;
	}
	
	/**
	 * Overwritten clone Method to create a new Instance of this Object
	 * @return new Instance of this Object
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		LayoutMLRuleEventType layoutMLRuleEventType = new LayoutMLRuleEventType();
		if (eventType != null)
			layoutMLRuleEventType.eventType = new String(eventType);
		if (entity != null)
			layoutMLRuleEventType.entity = new String(entity);
		if (sourceComponent != null)
			layoutMLRuleEventType.sourceComponent = new String(sourceComponent);

		return layoutMLRuleEventType;
	}

	/**
	 * Overwritten equals Method to compare the incoming {@link LayoutMLRuleEventType} object with this Objects
	 * 
	 * @param obj the Object to compare to this Object
	 * @return true if the content is equal, false if not
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LayoutMLRuleEventType))
			return false;
		
		LayoutMLRuleEventType type = (LayoutMLRuleEventType)obj;
		
		if ((type.eventType != null && !type.eventType.equals(eventType)) || (type.eventType == null && eventType != null)) {
			return false;
		}
		if ((type.entity != null && !type.entity.equals(entity)) || (type.entity == null && entity != null)) {
			return false;
		}
		if ((type.sourceComponent != null && !type.sourceComponent.equals(sourceComponent)) || (type.sourceComponent == null && sourceComponent != null)) {
			return false;
		}
		
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("Entity= " + this.entity + "\n");
		sb.append("EventType= " + this.eventType + "\n");
		sb.append("Sourcecomponent= " + this.sourceComponent + "\n");
		
		return sb.toString();
	}
	
	
}
