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
package org.nuclos.server.common.valueobject;

import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.exception.CommonValidationException;
import java.util.Date;

/**
 * General value object template for Nucleus. Has an Integer id as primary key.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph Radig</a>
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 01.00.00
 */
public class NuclosValueObject extends AbstractNuclosValueObject<Integer> {
	private static final long	serialVersionUID	= 6637996725938917463L;
	
	private final Integer iId;

	/**
	 * creates a value object representing a new object (that is to be inserted into the database)
	 * @postcondition this.getId() == null
	 */
	public NuclosValueObject() {
		this(null, null, null, null, null, null);
		assert this.getId() == null;
	}

	/**
	 * creates a value object representing an existing object (that was read from the database).
	 * @param iId primary key of underlying database record
	 * @param dateCreatedAt creation date of underlying database record
	 * @param sCreatedBy creator of underlying database record
	 * @param dateChangedAt last changed date of underlying database record
	 * @param sChangedBy last changer of underlying database record
	 * @postcondition this.getId() == iId
	 */
	public NuclosValueObject(Integer iId, Date dateCreatedAt, String sCreatedBy, Date dateChangedAt, String sChangedBy, Integer iVersion) {
		super(dateCreatedAt, sCreatedBy, dateChangedAt, sChangedBy, iVersion);
		this.iId = iId;
		assert this.getId() == iId;
	}

	/**
	 * creates a new NuclosValueObject out of a Collectable.
	 * @param clct must have the following fields: "createdAt", "createdBy", "changedAt", "changedBy".
	 */
	public NuclosValueObject(Collectable clct) {
		this((Integer) clct.getId(), (Date) clct.getValue("createdAt"), (String) clct.getValue("createdBy"),
				(Date) clct.getValue("changedAt"), (String) clct.getValue("changedBy"), clct.getVersion());
	}

	/**
	 * copy constructor.
	 * @param that
	 * @precondition that != null
	 * @postcondition this.getId() == that.getId()
	 */
	protected NuclosValueObject(NuclosValueObject that) {
		super(that);
		this.iId = that.getId();
		assert this.getId() == that.getId();
	}

	/**
	 * get primary key (intid) of underlying database record
	 * @return primary key of underlying database record
	 */
	@Override
	public Integer getId() {
		return this.iId;
	}

	/**
	 * @return a clone of this object.
	 */
	@Override
	public NuclosValueObject clone() {
		return (NuclosValueObject) super.clone();
	}

	/**
	 * Subclasses should perform all validation here that can be done from the value object itself.
	 * If more validation is needed that requires additional parameters, the following pattern should be used:
	 * <ol>
	 * <li>implement <code>validate()</code>: perform all validation here that can be done from the value object itself.</li>
	 * <li>write an additional method <code>validate(SomeParameter)</code>.</li>
	 * <li>let <code>validate(SomeParameter)</code> call <code>validate()</code> to perform the basic validation first.</li>
	 * <li>implement the additional validation that depends on the parameter in <code>validate(SomeParameter)</code>.</li>
	 * </ol>
	 * For an example, see <code>TariffValiditySchemaVO.validate(...)</code>.
	 * <p>
	 * Note that this method is currently never called from the base class, it rather serves as a general guide for
	 * successors how to implement their validation.
	 * <p>
	 * This default implementation is empty.
	 * @throws CommonValidationException if this value object isn't valid.
	 */
	public void validate() throws CommonValidationException {
	}

	/**
	 * inner class <code>GetId</code>: transforms an <code>NuclosValueObject</code> into its id.
	 */
	public static class GetId implements Transformer<NuclosValueObject, Integer> {
		@Override
		public Integer transform(NuclosValueObject vo) {
			return vo.getId();
		}
	}

}	// class NuclosValueObject
