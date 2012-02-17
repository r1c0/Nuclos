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
package org.nuclos.client.wizard.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;


/**
* <br>
* Created by Novabit Informationssysteme GmbH <br>
* Please visit <a href="http://www.novabit.de">www.novabit.de</a>
*
* @author <a href="mailto:marc.finke@novabit.de">Marc Finke</a>
* @version 01.00.00
*/

public class DataTyp implements Cloneable {

	String name;
	String inputFormat;
	String outputFormat;
	String databaseTyp;
	Integer precision;
	Integer scale;
	String javaType;

	public DataTyp() {

	}

	public DataTyp(String name, String inputFormat, String outputFormat,
			String databaseTyp, String javaTyp) {
		super();
		this.name = name;
		this.inputFormat = inputFormat;
		this.outputFormat = outputFormat;
		this.databaseTyp = databaseTyp;
		this.javaType = javaTyp;
	}

	public DataTyp(String name, String inputFormat, String outputFormat,
			String databaseTyp, Integer scale,Integer precision, String javaTyp) {
		super();
		this.name = name;
		this.inputFormat = inputFormat;
		this.outputFormat = outputFormat;
		this.databaseTyp = databaseTyp;
		this.precision = precision;
		this.scale = scale;
		this.javaType = javaTyp;
	}

	public DataTyp(MasterDataVO voDataType) {
		super();
		this.name = (String)voDataType.getField("name");
		this.inputFormat =(String)voDataType.getField("inputformat");
		this.outputFormat =(String)voDataType.getField("outputformat");
		this.precision = (Integer)voDataType.getField("precision");
		this.scale = (Integer)voDataType.getField("scale");
		this.javaType = (String)voDataType.getField("javatyp");
		this.databaseTyp = (String)voDataType.getField("databasetyp");
	}

	public boolean isRefenceTyp() {
		if(name.equals("Referenz Feld")) {
			return true;
		}
		else {
			return false;
		}
	}

	public boolean isLookupTyp() {
		if(name.equals("Nachschlage Feld")) {
			return true;
		}
		else {
			return false;
		}
	}

	public boolean isValueListTyp() {
		if(name.equals("Werteliste")) {
			return true;
		}
		else {
			return false;
		}
	}

	public static String getDefaultTypForJavaClass(Class<?> clazz) {
		if(clazz.isAssignableFrom(String.class)) {
			return "Text";
		}
		else if(clazz.isAssignableFrom(Integer.class)) {
			return "Ganzzahl";
		}
		else if(clazz.isAssignableFrom(Double.class)) {
			return "Kommazahl (9,2)";
		}
		else if(clazz.isAssignableFrom(Boolean.class)) {
			return "Ja/Nein";
		}
		else if(clazz.isAssignableFrom(java.util.Date.class)) {
			return "Datum";
		}
		else if(clazz.isAssignableFrom(org.nuclos.server.report.ByteArrayCarrier.class)) {
			return "Bild";
		}
		else if(clazz.isAssignableFrom(org.nuclos.server.genericobject.valueobject.GenericObjectDocumentFile.class)) {
			return "Dokumentenanhang";
		}
		else {
			return "Text";
		}
	}

	public static boolean isConversionSupported(DataTyp source, DataTyp target) {
		int sourcescale = LangUtils.defaultIfNull(source.getScale(), 0);
		int sourceprecision = LangUtils.defaultIfNull(source.getPrecision(), 0);
		int targetscale = LangUtils.defaultIfNull(target.getScale(), 0);
		int targetprecision = LangUtils.defaultIfNull(target.getPrecision(), 0);

		if (!String.class.getName().equals(target.getJavaType())) {
			// no to-string conversion, check if source type equals target type
			if (source.getJavaType().equals(target.getJavaType())) {
				// check scale and precision if required
				if (Double.class.getName().equals(target.getJavaType())) {
					if (sourceprecision - targetprecision > 0) {
						return false;
					}
					if ((sourcescale - sourceprecision) - (targetscale - targetprecision) > 0) {
						return false;
					}
				}
				return true;
			}
			else {
				return false;
			}
		}
		else {
			if (!LangUtils.equals(source.isRefenceTyp(), target.isRefenceTyp())) {
				return false;
			}
			if (!LangUtils.equals(source.isValueListTyp(), target.isValueListTyp())) {
				return false;
			}
			if (!LangUtils.equals(source.isLookupTyp(), target.isLookupTyp())) {
				return false;
			}
			return (sourcescale <= targetscale);
		}
	}

	public static List<DataTyp> getConvertibleTypes(DataTyp dataTyp) {
		List<DataTyp> lst = new ArrayList<DataTyp>();
		for(DataTyp typ : getAllDataTyps()) {
			if(isConversionSupported(dataTyp, typ)) {
				lst.add(typ);
			}
		}
		return lst;
	}

	public String getName() {
		return name;
	}

	public void setName(String typ) {
		this.name = typ;
	}

	public String getInputFormat() {
		return inputFormat;
	}

	public void setInputFormat(String inputFormat) {
		this.inputFormat = inputFormat;
	}

	public String getOutputFormat() {
		return outputFormat;
	}

	public void setOutputFormat(String outputFormat) {
		this.outputFormat = outputFormat;
	}

	public String getDatabaseTyp() {
		return databaseTyp;
	}

	public void setDatabaseTyp(String databaseTyp) {
		this.databaseTyp = databaseTyp;
	}

	public Integer getPrecision() {
		if(precision != null && precision.intValue() == 0)
			return null;
		return precision;
	}

	public void setPrecision(Integer precision) {
		this.precision = precision;
	}

	public Integer getScale() {
		if(scale != null && scale.intValue() == 0)
			return null;
		return scale;
	}

	public void setScale(Integer scale) {
		this.scale = scale;
	}



	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof DataTyp))
			return false;
		DataTyp typ = (DataTyp)obj;
		boolean blnEquals = typ.getName().equals(this.getName());
		return blnEquals;
	}

	public String getJavaType() {
		return javaType;
	}

	public void setJavaType(String javaType) {
		this.javaType = javaType;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return new DataTyp(name, inputFormat, outputFormat, databaseTyp, javaType);
	}

	public static List<DataTyp> getAllDataTyps() {
		List<DataTyp> lst = new ArrayList<DataTyp>();

		lst.addAll(DataTyp.getAllDataTypsFromDB());
		lst.add(getReferenzTyp());
		lst.add(getLookupTyp());

		return lst;
	}

	private static List<DataTyp> getAllDataTypsFromDB() {
		List<DataTyp> lst = new ArrayList<DataTyp>();

		Collection<MasterDataVO> colVO = MasterDataDelegate.getInstance().getMasterData(NuclosEntity.DATATYP.getEntityName());
		for(MasterDataVO vo : colVO) {
			DataTyp typ = new DataTyp((String)vo.getField("name"), (String)vo.getField("inputformat"), (String)vo.getField("outputformat"),
				(String)vo.getField("databasetyp"), (Integer)vo.getField("scale"), (Integer)vo.getField("precision"), (String)vo.getField("javatyp"));
			if(typ.getName().equals("Referenz Feld"))
				continue;
			if(typ.getName().equals("Nachschlage Feld"))
				continue;
			lst.add(typ);
		}

		return lst;
	}

	public static DataTyp getReferenzTyp() {
		return new DataTyp("Referenz Feld", null, null, "varchar", 255, 0, "java.lang.String");
	}

	public static DataTyp getLookupTyp() {
		return new DataTyp("Nachschlage Feld", null, null, "varchar", 255, 0, "java.lang.String");
	}

	public static DataTyp getValueListTyp() {
		return new DataTyp("explizite Werteliste", null, null, "varchar", 255, 0, "java.lang.String");
	}

	public static DataTyp getDefaultDataTyp() throws CommonFinderException, CommonPermissionException  {
		MasterDataVO vo = null;
		for(MasterDataVO voDataType : MasterDataDelegate.getInstance().getMasterData(NuclosEntity.DATATYP.getEntityName())) {
			if("Text".equals(voDataType.getField("name"))) {
				vo = voDataType;
				break;
			}
		}
		DataTyp typ = new DataTyp((String)vo.getField("name"), (String)vo.getField("inputformat"), (String)vo.getField("outputformat"),
			(String)vo.getField("databasetyp"), (Integer)vo.getField("scale"), (Integer)vo.getField("precision"), (String)vo.getField("javatyp"));
		return typ;
	}

	public static DataTyp getDefaultStringTyp() {
		return new DataTyp("Text", null, null, "varchar", 255, 0, "java.lang.String");
	}

	public static DataTyp getDefaultDateTyp() {
		return new DataTyp("Date", null, null, "varchar", null, null, "java.util.Date");
	}

}
