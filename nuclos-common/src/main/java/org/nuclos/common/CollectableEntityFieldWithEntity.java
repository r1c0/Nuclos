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
package org.nuclos.common;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.nuclos.common.collect.collectable.AbstractCollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityField;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.Transformer;

/**
 * A <code>CollectableEntityField</code>, along with the <code>CollectableEntity</code> it belongs to.
 * Note that there are <code>CollectableEntityField</code>s that don't belong to one unique entity.
 * This is especially the case for dynamic attributes that are used in more than one entity.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 * @todo Even CollectableEntityField should probably know to which entity it belongs. Try to eliminate this class.
 *
 * @deprecated CollectableEntityField now knows the entity name.
 */
public class CollectableEntityFieldWithEntity implements CollectableEntityField, Serializable {

	private static final long serialVersionUID = 6352766708611387488L;

	private final String entityName;

	/**
	 * @deprecated Why is this transient? How is a value after serialization enforced?
	 */
	private transient CollectableEntity clcte;

	/**
	 * @deprecated Why is this transient? How is a value after serialization enforced?
	 */
	private transient CollectableEntityField clctef;

	/**
	 * @deprecated Why is this transient? How is a value after serialization enforced?
	 */
	private transient CollectableEntityFieldSecurityAgent securityAgent = new CollectableEntityFieldSecurityAgent();

	/**
	 * @param clcte
	 * @param sFieldName
	 * @precondition clcte != null
	 * @precondition sFieldName != null
	 */
	public CollectableEntityFieldWithEntity(CollectableEntity clcte, String sFieldName) {
		this.clcte = clcte;
		this.clctef = clcte.getEntityField(sFieldName);
		this.entityName = clcte.getName();
	}

	/**
	 * @return the name of the <code>CollectableEntity</code> this field belongs to.
	 * @postcondition result != null
	 *
	 * @deprecated Not always set. Use {@link #getEntityName()} instead.
	 */
	public final String getCollectableEntityName() {
		return clcte.getName();
	}

	/**
	 * @return the label of the <code>CollectableEntity</code> this field belongs to.
	 * @postcondition result != null
	 */
	public String getCollectableEntityLabel() {
		return clcte.getLabel();
	}

	public CollectableEntityField getField() {
		return this.clctef;
	}

	@Override
	public String getName() {
		return this.getField().getName();
	}

	@Override
	public String getFormatInput() {
		return this.clctef.getFormatInput();
	}

	@Override
	public String getFormatOutput() {
		return this.clctef.getFormatOutput();
	}

	@Override
	public int getFieldType() {
		return this.getField().getFieldType();
	}

	@Override
	public boolean isIdField() {
		return this.getField().isIdField();
	}

	@Override
	public Class<?> getJavaClass() {
		return this.getField().getJavaClass();
	}

	@Override
	public String getLabel() {
		return this.getField().getLabel();
	}

	@Override
	public String getDescription() {
		return this.getField().getDescription();
	}

	@Override
	public Integer getMaxLength() {
		return this.getField().getMaxLength();
	}

	@Override
	public Integer getPrecision() {
		return this.getField().getPrecision();
	}

	@Override
	public boolean isRestrictedToValueList() {
		return this.getField().isRestrictedToValueList();
	}

	@Override
	public boolean isNullable() {
		return this.getField().isNullable();
	}

	@Override
	public boolean isReferencing() {
		return this.getField().isReferencing();
	}

	@Override
	public String getReferencedEntityName() {
		return this.getField().getReferencedEntityName();
	}

	@Override
	public String getReferencedEntityFieldName() {
		return this.getField().getReferencedEntityFieldName();
	}

	@Override
	public boolean isReferencedEntityDisplayable() {
		/** @TODO Keine Client Classen in Common! */
		//	return NuclosCollectableEntityProvider.getInstance().isEntityDisplayable(this.getReferencedEntityName());
		return false;
	}

	@Override
	public int getDefaultCollectableComponentType() {
		return this.getField().getDefaultCollectableComponentType();
	}

	@Override
	public CollectableField getNullField() {
		return this.getField().getNullField();
	}

	@Override
	public CollectableField getDefault() {
		return this.getField().getDefault();
	}

	@Override
	public String toString() {
		return this.getCollectableEntityLabel() + "." + this.getField().getLabel();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof CollectableEntityField)) return false;
		final CollectableEntityField that = (CollectableEntityField) o;
		final boolean result;
		if (that instanceof CollectableEntityFieldWithEntity)  {
			result = getEntityName().equals(that.getEntityName()) &&
					getField().equals(((CollectableEntityFieldWithEntity) that).getField());
		}
		else {
			result = getEntityName().equals(that.getEntityName()) &&
					getField().equals(that);
		}
		return result;
	}

	@Override
	public int hashCode() {
		return getField().hashCode();
	}

	/**
	 * Predicate: HasEntity
	 *
	 * @deprecated Use CollectableEntityField.HasEntity
	 */
	public static class HasEntity implements Predicate<CollectableEntityFieldWithEntity> {
		private final String sEntityName;

		public HasEntity(CollectableEntity clcte) {
			this(clcte.getName());
		}

		public HasEntity(String sEntityName) {
			this.sEntityName = sEntityName;
		}

		@Override
		public boolean evaluate(CollectableEntityFieldWithEntity clctefwe) {
			return this.sEntityName.equals(clctefwe.getCollectableEntityName());
		}
	}

	/**
	 * Qualified field name. Format: EntityName.FieldName
	 *
	 * @deprecated Use CollectableEntityField.QualifiedEntityFieldName
	 */
	public static class QualifiedEntityFieldName {
		private final String sEntityName;
		private final String sFieldName;

		public QualifiedEntityFieldName(String sQualifiedEntityFieldName){
			StringBuffer sb = new StringBuffer(sQualifiedEntityFieldName);
			int pointPosition = sb.indexOf(".");
			if(pointPosition > 0){
				this.sEntityName = sb.substring(0, pointPosition);
				this.sFieldName = sb.substring(pointPosition+1);
			} else {
				this.sEntityName = null;
				this.sFieldName = sQualifiedEntityFieldName;
			}
		}

		public boolean isQualifiedEntityFieldName(){
			return sEntityName != null && sFieldName != null;
		}

		public String getEntityName(){
			return this.sEntityName;
		}

		public String getFieldName(){
			return this.sFieldName;
		}
	}

	/**
	 * Transformer: GetEntityName
	 *
	 * @deprecated Use CollectableEntityField.GetEntityName
	 */
	public static class GetEntityName implements Transformer<CollectableEntityFieldWithEntity, String> {
		@Override
		public String transform(CollectableEntityFieldWithEntity clctefwe) {
			return clctefwe.getCollectableEntityName();
		}
	}

	/**
	 * Transformer: GetQualifiedEntityFieldName
	 *
	 * @deprecated Use CollectableEntityField.GetQualifiedEntityFieldName
	 */
	public static class GetQualifiedEntityFieldName implements Transformer<CollectableEntityFieldWithEntity, String> {
		@Override
		public String transform(CollectableEntityFieldWithEntity clctefwe) {
			return clctefwe.getCollectableEntityName() + "." + clctefwe.getName();
		}
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
		// CollectableEntityField generally is not serializable, but DefaultCollectableEntityField is:
		oos.writeObject(new DefaultCollectableEntityField(this.clctef, entityName));

		oos.writeObject(clcte != null ? clcte.getName() : "");
		oos.writeObject(clcte != null ? clcte.getLabel() : "");
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();
		// CollectableEntityField is not serializable:
		this.clctef = (CollectableEntityField) ois.readObject();
		this.clcte = new AbstractCollectableEntity((String)ois.readObject(),(String)ois.readObject()) {};
	}

	/**
	 * set security agent
	 */
	@Override
	public void setSecurityAgent(CollectableEntityFieldSecurityAgent sa) {
		this.securityAgent = sa;
	}

	/**
	 * get security agent
	 */
	@Override
	public CollectableEntityFieldSecurityAgent getSecurityAgent() {
		return this.securityAgent;
	}


	/**
	 * is this field readable
	 */
	@Override
	public boolean isReadable() {
		return getSecurityAgent().isReadable();
	}


	/**
	 * is this field writable
	 */
	@Override
	public boolean isWritable() {
		return getSecurityAgent().isWritable();
	}


	/**
	 * is this field removable
	 */
	@Override
	public boolean isRemovable() {
		return getSecurityAgent().isRemovable();
	}

	/**
	 * @deprecated Not always set. Better use {@link #getEntityName()}.
	 */
	@Override
	public CollectableEntity getCollectableEntity() {
		return clcte;
	}

	@Override
	public void setCollectableEntity(CollectableEntity clent) {
		clcte = clent;
	}

	@Override
	public String getEntityName() {
		return clcte.getName();
	}

}	// class CollectableEntityFieldWithEntity
