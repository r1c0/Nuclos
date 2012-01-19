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

import java.io.Serializable;
import java.util.Date;
import java.util.prefs.Preferences;

import org.apache.commons.lang.NullArgumentException;
import org.nuclos.common.collect.exception.CollectableFieldValidationException;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.RelativeDate;



/**
 * Default implementation of a <code>CollectableEntityField</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * <p>
 * TODO: Why we have such a strange implementation? Can we get rid of it? If this is mainly
 * to write to {@link Preferences}, consider todo below.
 * </p><p>
 * TODO: Consider {@link org.nuclos.client.common.CollectableEntityFieldPreferencesUtil}
 * to write to {@link Preferences}.
 * </p>
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class DefaultCollectableEntityField extends AbstractCollectableEntityField implements Serializable {

	private final String entityName;
	private final String sName;
	private final Class<?> cls;
	private final String sLabel;
	private final String sDescription;
	private final Integer iMaxLength;
	private final Integer iPrecision;
	private final boolean bNullable;
	private final int iFieldType;
	private final CollectableField clctfDefault;
	private final String sReferencedEntityName;
	private final String sFormatInput;
	private final String sFormatOutput;

	/**
	 * @param sName
	 * @param cls
	 * @param sLabel
	 * @param sDescription
	 * @param iMaxLength
	 * @param bNullable
	 * @param iFieldType
     * @param sInputFormat
     * @param sOutputFormat
	 */
	public DefaultCollectableEntityField(String sName, Class<?> cls, String sLabel, String sDescription,
			Integer iMaxLength, Integer iPrecision, boolean bNullable, int iFieldType, String sFormatInput,
			String sFormatOutput, String entityName) {
		this(sName, cls, sLabel, sDescription, iMaxLength, iPrecision, bNullable, iFieldType, null,
				CollectableUtils.getNullField(iFieldType), sFormatInput, sFormatOutput, entityName);
		assert sName != null;
		assert entityName != null;
	}

	/**
	 * @param sName
	 * @param cls
	 * @param sLabel
	 * @param sDescription
	 * @param iMaxLength
	 * @param bNullable
	 * @param iFieldType
	 * @param clctfDefault
     * @param sInputFormat
     * @param sOutputFormat
	 * @precondition clctfDefault != null
	 * @todo add precondition (sReferencedEntityName != null) --> (iFieldType == IDFIELD)
	 */
	public DefaultCollectableEntityField(String sName, Class<?> cls, String sLabel, String sDescription, Integer iMaxLength,
			Integer iPrecision, boolean bNullable, int iFieldType, String sReferencedEntityName, CollectableField clctfDefault,
			String sFormatInput, String sFormatOutput, String entityName) {
		if (clctfDefault == null) {
			throw new NullArgumentException("clctfDefault");
		}
		this.entityName = entityName;
		this.sName = sName;
		this.cls = cls;
		this.sLabel = sLabel;
		this.sDescription = sDescription;
		this.iMaxLength = iMaxLength;
		this.iPrecision = iPrecision;
		this.bNullable = bNullable;
		this.iFieldType = iFieldType;
		this.clctfDefault = clctfDefault;
		this.sReferencedEntityName = sReferencedEntityName;
		this.sFormatInput = sFormatInput;
		this.sFormatOutput = sFormatOutput;
		try {
			CollectableUtils.validateFieldType(clctfDefault, this);
			if (this.getJavaClass().equals(Date.class) && clctfDefault.getValue() != null &&
				clctfDefault.getValue().equals(RelativeDate.today().toString())) {
				//ok
			}
			else {
				CollectableUtils.validateValueClass(clctfDefault, this);
			}
		}
		catch (CollectableFieldValidationException ex) {
			throw new IllegalArgumentException(CommonLocaleDelegate.getMessage("DefaultCollectableEntityField.1","Der angegebene Defaultwert passt nicht zum Datentyp."));
		}
		
		assert sName != null;
		assert entityName != null;
	}

	/**
	 * @param clctef
	 * @postcondition this.equals(clctef)
	 */
	public DefaultCollectableEntityField(CollectableEntityField clctef, String entityName) {
		this(clctef.getName(), clctef.getJavaClass(), clctef.getLabel(), clctef.getDescription(), clctef.getMaxLength(),
				clctef.getPrecision(), clctef.isNullable(), clctef.getFieldType(), clctef.getReferencedEntityName(),
				clctef.getDefault(), clctef.getFormatInput(), clctef.getFormatOutput(), entityName);
		
		assert sName != null;
		assert entityName != null;
	}

	@Override
	public String getName() {
		return this.sName;
	}

	@Override
	public String getFormatInput() {
		return this.sFormatInput;
	}

	@Override
	public String getFormatOutput() {
		return this.sFormatOutput;
	}

	@Override
	public int getFieldType() {
		return this.iFieldType;
	}

	@Override
	public Class<?> getJavaClass() {
		return this.cls;
	}

	@Override
	public String getLabel() {
		return this.sLabel == null ? this.sName : this.sLabel;
	}

	@Override
	public String getDescription() {
		return this.sDescription;
	}

	@Override
	public Integer getMaxLength() {
		return this.iMaxLength;
	}

	@Override
	public Integer getPrecision() {
		return this.iPrecision;
	}

	@Override
	public String getReferencedEntityName() {
		return this.sReferencedEntityName;
	}

	@Override
	public boolean isReferencing() {
		return this.getReferencedEntityName() != null;
	}

	@Override
	public boolean isNullable() {
		return this.bNullable;
	}

	@Override
	public CollectableField getDefault() {
		final CollectableField result = this.clctfDefault;

		assert result != null;
		assert result.getFieldType() == this.getFieldType();
		if (!(this.getJavaClass() == Date.class && result.getValue() != null && result.getValue().toString().equalsIgnoreCase(RelativeDate.today().toString())))
			assert LangUtils.isInstanceOf(result.getValue(), this.getJavaClass());
		return result;
	}

	@Override
	public String getEntityName() {
		return entityName;
	}

}	// class DefaultCollectableEntityField
