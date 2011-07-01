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
package org.nuclos.common2;

/**
 * An extended relative date (e.g. "today" + 1 day or "today" - 2 month). Useful for relative comparisons, esp. in search conditions.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Martin.Weber@novabit.de">Martin.Weber</a>
 * @version 01.00.00
 */
public class ExtendedRelativeDate extends RelativeDate {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String NEGATIVE_OPERAND = "-";
	public static final String POSITIVE_OPERAND = "+";
	
	public static final String UNIT_DAY_EN = "D";
	public static final String UNIT_DAY_DE = "T";
	public static final String UNIT_MONTH = "M";
	
	private String sUnit = UNIT_DAY_DE;
	private String sOperand = POSITIVE_OPERAND;
	private Integer iQuantity = 1;

	protected ExtendedRelativeDate() {
		super();
	}
	
	/**
	 * @return an instance of "today". It's !!!not!!! allowed to "==" against this object.
	 */
	public static RelativeDate today() {
		return new ExtendedRelativeDate();
	}

	/**
	 * @return the current date (with time set to 0:00:00) in consideration of the operand, the quantity and unit
	 */
	@Override
	public long getTime() {
		Integer iQty = getOperand().equals(NEGATIVE_OPERAND) ? (getQuantity() * -1) : getQuantity();
		
		if (getUnit().equals(UNIT_DAY_DE) || getUnit().equals(UNIT_DAY_EN)) {
			return DateUtils.addDays(DateUtils.today(), iQty).getTime();
		}
		else if (getUnit().equals(UNIT_MONTH)) {
			return DateUtils.addMonths(DateUtils.today(), iQty).getTime();
		}
		else {
			return super.getTime();
		}
	}

	/**
	 * @return "TODAY" + operand + quantity + unit
	 */
	@Override
	public String toString() {
		return getString("TODAY");
	}
	
	/**
	 * @return LABEL_TODAY + operand + quantity + unit
	 */
	public String getString(String labelToday) {
		return labelToday + getOperand() + getQuantity() + getUnit();
	}
	
	/**
	 * set the operand
	 * @param sOperand
	 */
	public void setOperand(String sOperand) {
		this.sOperand = sOperand;
	}

	/**
	 * get the operand
	 * @return String
	 */
	public String getOperand() {
		return this.sOperand;
	}
	
	/**
	 * set the unit
	 * @param sUnit
	 */
	public void setUnit(String sUnit) {
		this.sUnit = sUnit;
	}
	
	/**
	 * get the unit
	 * @return String
	 */
	public String getUnit() {
		return this.sUnit;
	}
	
	/**
	 * set the quantity
	 * @param iQuantity
	 */
	public void setQuantity(Integer iQuantity) {
		this.iQuantity = iQuantity;
	}
	
	/**
	 * get the quantity
	 * @return Integer
	 */
	public Integer getQuantity() {
		return this.iQuantity;
	}
} // class ExtendedRelativeDate
