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
package org.nuclos.common.collect.collectable;

import java.text.Collator;
import java.util.Comparator;

import org.nuclos.common.CollectableEntityFieldWithEntity;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.LangUtils;

/**
 * Provides structural (meta) information about a <code>CollectableField</code>.
 * This corresponds to a column in a relational database table schema.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public interface CollectableEntityField {

	/**
	 * Transformer: GetEntityName
	 */
	public static class GetEntityName implements Transformer<CollectableEntityField, String> {
		@Override
		public String transform(CollectableEntityField clctefwe) {
			return clctefwe.getEntityName();
		}
	}

	/**
	 * field type: undefined. This is needed for reading from the preferences.
	 */
	public static final int TYPE_UNDEFINED = -1;

	/**
	 * field type: value field. A value field has a value, but no id.
	 */
	public static final int TYPE_VALUEFIELD = 1;

	/**
	 * field type: id field. An id field has an id and a value.
	 */
	public static final int TYPE_VALUEIDFIELD = 2;

	/**
	 * @return the name of this field
	 */
	String getName();
	
	/**
	 * @return the name of the entity this field belongs to.
	 * 
	 * @author Thomas Pasch
	 * @since Nuclos 3.1.01
	 */
	String getEntityName();
	
	/**
	 * @return the input format of this field
	 */
	String getFormatInput();
	
	/**
	 * @return the output format of this field
	 */
	String getFormatOutput();

	/**
	 * @return the type of this field (value or id field)
	 */
	int getFieldType();

	/**
	 * (Often needed) shortcut for this.getFieldType() == TYPE_VALUEIDFIELD.
	 * @return result <--> (this.getFieldType() == TYPE_VALUEIDFIELD)
	 */
	boolean isIdField();

	/**
	 * Two <code>CollectableEntityField</code>s are considered equal iff their names are equal
	 * @param o
	 */
	@Override
    boolean equals(Object o);

	/**
	 * @return the type of this field, as Java class
	 * @postcondition result != null
	 */
	Class<?> getJavaClass();

	/**
	 * @return the (default) label that is presented to the user.
	 * It is shown in dialogs, column headers, search conditions etc.
	 */
	String getLabel();

	/**
	 * @return a description of this field, if any
	 */
	String getDescription();

	/**
	 * @return the maximum length of this field in characters, if any
	 */
	Integer getMaxLength();
	
	/**
	 * @return the precision of this field, if any
	 */
	Integer getPrecision();

	/**
	 * @return Is this field restricted to a value list? If no, values not contained in the value list can be entered
	 * also. A typical component for this is a combobox, as opposed to a dropdown.
	 * @precondition isIdField()
	 */
	boolean isRestrictedToValueList();

	/**
	 * @return Is this field nullable?
	 */
	boolean isNullable();

	/**
	 * @return Is this field referencing another <code>Collectable</code>?
	 * In a database application, this would be <code>true</code> for foreign key fields.
	 */
	boolean isReferencing();
	
	/**
	 * @return the parent <code>CollectableEntity</code>, if any.
	 * 
	 * @deprecated Not always present.
	 */
	CollectableEntity getCollectableEntity();
	
	/**
	 * sets the parent <code>CollectableEntity</code>
	 * @param the parent <code>CollectableEntity</code>
	 * 
	 * @deprecated Not always present.
	 */
	void setCollectableEntity(CollectableEntity clent);

	/**
	 * @return the name of the referenced <code>CollectableEntity</code>, if any.
	 * 
	 * @deprecated There is no such thing like a "referenced field" - only a whole Collectable can be referenced.
	 */
	String getReferencedEntityName();

	/**
	 * @return the name of the field in the referenced <code>CollectableEntity</code>, if any. That is the field
	 * that this field is a foreign key of. Defaults to <code>"name"</code>.
	 * The field name returned by this method is used for lookups and for displaying the identifier of a referenced Collectable (entity).
	 * In lookups, the field to transfer must be specified as it isn't always the same. For displaying the identifier,
	 * {@link Collectable#getIdentifierLabel()} should be used.
	 * 
	 * @deprecated There is no such thing like a "referenced field" - only a whole Collectable can be referenced.
	 */
	@Deprecated
	String getReferencedEntityFieldName();

	/**
	 * @return Can the referenced entity be displayed (and possibly edited)? This defaults to <code>true</code>,
	 * but may be <code>false</code> for some instances that are referenced externally.
	 * @precondition isReferencing()
	 * @deprecated This doesn't belong here! It's not the entity field's responsibility to know about that.
	 * @see CollectableEntityProvider#isEntityDisplayable(String sEntityName)
	 */
	@Deprecated
	boolean isReferencedEntityDisplayable();

	/**
	 * @return the default CollectableComponent type for this field.
	 * @see CollectableComponentTypes
	 */
	int getDefaultCollectableComponentType();

	/**
	 * @return a null value appropriate for this field.
	 * @postcondition result != null
	 * @postcondition result.isNull()
	 * @postcondition result.getFieldType() == this.getFieldType()
	 */
	CollectableField getNullField();

	/**
	 * @return the default value for this field. The default value is set when an instance of this
	 * field is created. Note that this only applies to situations where a new Collectable is about to
	 * be entered by the user. When specifying a search condition,  <code>getNullField()</code> should be used.
	 * @postcondition result != null
	 * @postcondition result.getFieldType() == this.getFieldType()
	 * @postcondition LangUtils.isInstanceOf(result.getValue(), this.getJavaClass())
	 */
	CollectableField getDefault();

	/**
	 * @return <code>this.getLabel()</code>
	 */
	@Override
    String toString();

	/**
	 * inner class <code>LabelComparator</code>. Compares <code>CollectableEntityField</code>s by their labels.
	 */
	public static class LabelComparator implements Comparator<CollectableEntityField> {
		private final Collator collator = LangUtils.getDefaultCollator();

		@Override
        public int compare(CollectableEntityField clctef1, CollectableEntityField clctef2) {
			return this.collator.compare(clctef1.getLabel(), clctef2.getLabel());
		}

	}	// inner class LabelComparator

	/**
	 * inner class <code>GetName</code>: transforms a <code>CollectableEntityField</code> into that field's name.
	 */
	public static class GetName implements Transformer<CollectableEntityField, String> {

		@Override
        public String transform(CollectableEntityField clctef) {
			return clctef.getName();
		}

	}	// inner class GetName
	
	/**
	 * inner class <code>CollectableEntityFieldSecurityAgent</code>:
	 * checks the permission (read, write, delete) for this <code>CollectableEntityField</code> according to the <code>Collectable</code>.
	 */
	public class CollectableEntityFieldSecurityAgent {
		private Collectable clct;
		
		/**
		 * sets the <code>Collectable</code> for the <code>CollectableEntityField</code>
		 * @param Collectable
		 */
		public void setCollectable(Collectable clct) {
			this.clct = clct;
		}
		
		/**
		 * @return the <code>Collectable</code> of the <code>CollectableEntityField</code>
		 */
		public Collectable getCollectable() {
			return clct;
		}
		
		/**
		 * you may use and overwrite this method for your own purpose
		 * @return true if read permission is granted to this field according to the <code>Collectable</code> otherwise false;
		 * in this default implementation return always true
		 */
		public boolean isReadable() {
			return true;
		}
	
		/**
		 * you may use and overwrite this method for your own purpose
		 * @return true if write permission is granted to this field according to the <code>Collectable</code> otherwise false;
		 * in this default implementation return always true
		 */
		public boolean isWritable() {
			return true;
		}
		
		/**
		 * you may use and overwrite this method for your own purpose
		 * @return true if delete permission is granted to this field according to the <code>Collectable</code> otherwise false;
		 * in this default implementation return always true
		 */
		public boolean isRemovable() {
			return true;
		}
	} // inner class CollectableEntityFieldSecurityAgent

	/**
	 * sets the security agent for this <code>CollectableEntityField</code>
	 * @param CollectableEntityFieldSecurityAgent
	 */
	void setSecurityAgent(CollectableEntityFieldSecurityAgent sa);
	
	/**
	 * get the security agent for this <code>CollectableEntityField</code>
	 * @return CollectableEntityFieldSecurityAgent
	 */
	CollectableEntityFieldSecurityAgent getSecurityAgent();
	
	/**
	 * checks whether read permission is granted to this field or not
	 */
	boolean isReadable();
	
	/**
	 * checks whether write permission is granted to this field or not
	 */
	boolean isWritable();
	
	/**
	 * checks whether delete permission is granted to this field or not
	 */
	boolean isRemovable();
	
}	// interface CollectableEntityField
