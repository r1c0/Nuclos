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
package org.nuclos.client.masterdata;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.builder.ToStringBuilder;

import org.nuclos.common.collect.collectable.AbstractCollectableField;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldFormat;
import org.nuclos.common.collect.collectable.CollectableUtils;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common.collect.exception.CollectableFieldValidationException;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;


/**
 * A <code>CollectableField</code> initialized with the contents of a <code>MasterDataVO</code> field.
 * This class is immutable.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */

public class CollectableMasterDataField extends AbstractCollectableField {
	
	private final Object oValue;
	private final Object oValueId;
	private final int iFieldType;
	
	private final CollectableMasterData clctmd;
	private final String sFieldName; 
	
	/**
	 * @param clctmd
	 * @param sFieldName
	 * @precondition clctmd != null
	 * @precondition sFieldName != null
	 */
	CollectableMasterDataField(CollectableMasterData clctmd, String sFieldName) {
		if (clctmd == null) {
			throw new NullArgumentException("clctmd");
		}
		if (sFieldName == null) {
			throw new NullArgumentException("sFieldName");
		}
		final MasterDataVO mdvo = clctmd.getMasterDataCVO();
		if (sFieldName.equals("entityfieldDefault") && mdvo.getField(sFieldName) != null) {
			try {
				this.iFieldType = CollectableField.TYPE_VALUEFIELD;
				this.oValue = CollectableFieldFormat.getInstance(Class.forName(mdvo.getField("datatype").toString())).parse(null, mdvo.getField(sFieldName).toString());
				this.oValueId = null;
			}
			catch(CollectableFieldFormatException e) {
				throw new NuclosFatalException(CommonLocaleDelegate.getMessage("CollectableMasterDataField.1", "Der Datentyp des Standardwerts der Entit\u00e4t {0} entspricht nicht dem Datentyp der Entit\u00e4t.", clctmd.getField("name")),e);
			}
			catch(ClassNotFoundException e) {
				throw new NuclosFatalException(e);
			}
		}
		else {
			final CollectableEntityField clctef = clctmd.getCollectableEntity().getEntityField(sFieldName);
			this.iFieldType = clctef.getFieldType();
			this.oValue = getValue(mdvo.getField(sFieldName), clctef);
			this.oValueId = clctef.isIdField() ? getValueId(sFieldName, mdvo) : null;
		}
		this.clctmd = clctmd;
		this.sFieldName = sFieldName;

	}

	@Override
	public int getFieldType() {
		return this.iFieldType;
	}

	@Override
	public Object getValue() {
		return this.oValue;
	}

	@Override
	public Object getValueId() throws UnsupportedOperationException {
		if (!this.isIdField()) {
			throw new UnsupportedOperationException("getValueId");
		}
		return this.oValueId;
	}

	private static Object getValue(Object oValue, CollectableEntityField clctef) {
		Object result = oValue;
		// workaround: The server returns java.sql.Timestamp for Dates. This is problematic when
		// comparing dates. Timestamps can only be compared to timestamps.
		/** @todo introduce data type "java.sql.Timestamp" and do this conversion on the server side! */
		if (result != null && clctef.getJavaClass().equals(Date.class)) {
			try {
				if (result instanceof String) {
					result = CommonLocaleDelegate.getDateFormat().parse((String)result);
					//result = DateFormat.getDateInstance().parse((String)result);
				}
				else {
					Date date = (Date)result;
					result = new Date(date.getTime());
				}
			}
			catch(ParseException ex) {
				throw new CommonFatalException(ex);
			}
		}
		return result;
	}

	private static Object getValueId(String sFieldName, MasterDataVO mdvo) {
		return mdvo.getField(sFieldName + "Id");
	}

	@Override
	public void validate(CollectableEntityField clctef) throws CollectableFieldValidationException {
		CollectableUtils.validate(this, clctef);

		/** @todo validate according to input/output format */
	}
	
	@Override
	public String toString() {
		final Object oValue = this.getValue();
		final String sOutputFormat = clctmd.getCollectableEntity().getEntityField(sFieldName).getFormatOutput();
		return (oValue == null) ? "" : CollectableFieldFormat.getInstance(oValue.getClass()).format(sOutputFormat, oValue);		
	}

	@Override
	public String toDescription() {
		final ToStringBuilder b = new ToStringBuilder(this).append(iFieldType).append(sFieldName).append(oValueId).append(oValue);
		return b.toString();
	}

}	// class CollectableMasterDataField
