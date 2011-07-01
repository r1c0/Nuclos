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
package org.nuclos.client.ui;


/**
 * TextField for Runtime.
 * Calculates itself the best representation outgoing from millis, 
 * e.g. "7w 1d 14h 5m"
 *
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:maik.stueker@novabit.de">Maik Stueker</a>
 * @version 01.00.00
 */
public class RuntimeTextField extends CommonJTextField {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final double dOneMinute = 1000d * 60d;
	private static final double dOneHour   = dOneMinute * 60d;
	private static final double dOneDay    = dOneHour * 24d;
	private static final double dOneWeek   = dOneDay * 7d;
	
	private String backup = null ;
	
	public RuntimeTextField(){
		super();
	}
	
	public void setMillis(double runtimeInMillis){
		super.setText(getBestRepresentation(runtimeInMillis));
	}
	
	@Override
	public void setText(String text) {
		this.backup = text;
		try {
			if (text != null){
				final Double dText = new Double(text);
				this.setMillis(dText.doubleValue());
			}
		} catch (NumberFormatException ex){
			super.setText(text);
		}
	}
	
	@Override
	public String getText() {
		return this.backup;
	}

	@Override
	public String format(Object obj) {
		return obj.toString();
	}

	@Override
	public Object parse(String text) {
		return text;
	}

	private static String getBestRepresentation(double runtimeInMillis){
		StringBuffer result = new StringBuffer();
		
		if (runtimeInMillis >= dOneWeek){
			long weeks = trunc(runtimeInMillis / dOneWeek);
			result.append(weeks + "w ");
			
			runtimeInMillis = runtimeInMillis - (weeks * dOneWeek);
		}
		if (runtimeInMillis >= dOneDay){
			long days = trunc(runtimeInMillis / dOneDay);
			result.append(days + "d ");
			
			runtimeInMillis = runtimeInMillis - (days * dOneDay);
		}
		if (runtimeInMillis >= dOneHour){
			long hours = trunc(runtimeInMillis / dOneHour);
			result.append(hours + "h ");
			
			runtimeInMillis = runtimeInMillis - (hours * dOneHour);
		}
		if (trunc(runtimeInMillis) > 0){
			long minutes = trunc(runtimeInMillis / dOneMinute);
			result.append(minutes + "m");
		}
		
		return result.toString();
	}
	
	private static long trunc(double d) {
        return (long)d;
    }
}
