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

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.table.TableModel;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRReport;
import net.sf.jasperreports.engine.JRReportFont;
import net.sf.jasperreports.engine.JRTextElement;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;

import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.dal.vo.PivotInfo;
import org.nuclos.common.entityobject.CollectableEOEntityField;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonFatalException;

/**
 * Helper class for PDF export.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:uwe.allner@novabit.de">uwe.allner</a>
 * @version 01.00.00
 */
public class PDFHelper {

	public static String getFieldName(CollectableEntityField field) {
		final String rawName;
		if (field instanceof CollectableEOEntityField) {
			final CollectableEOEntityField f = (CollectableEOEntityField) field;
			final PivotInfo pinfo = f.getMeta().getPivotInfo();
			if (pinfo != null) {
				rawName = field.getEntityName() + ":" + pinfo.getKeyField() 
					+ ":" + f.getName() + ":" + pinfo.getValueField();   
			}
			else {
				rawName = field.getEntityName() + "." + field.getName();
			}
		}
		else {
			rawName = field.getEntityName() + "." + field.getName();
		}
		return getFieldName(rawName);
	}

	public static String getFieldName(String nuclosfieldname) {
		final String result = "fld" + nuclosfieldname.replace('[', '_').replace(']', '_');
		return result;
	}
	
	public static String getLabel(CollectableEntityField field) {
		final String result;
		if (field instanceof CollectableEOEntityField) {
			final CollectableEOEntityField f = (CollectableEOEntityField) field;
			final PivotInfo pinfo = f.getMeta().getPivotInfo();
			if (pinfo != null) {
				result = f.getName() + ":" + pinfo.getValueField();
			}
			else {
				result = field.getLabel();
			}
		}
		else {
			result = field.getLabel();
		}
		return result;
	}

	public static void createFields(JasperDesign jrdesign, TableModel tblmodel) {
		List<FieldDefinition> fields = getFieldDefinition(tblmodel);
		PDFHelper.createFieldsInternal(jrdesign, fields);
	}

	public static void createFields(JasperDesign jrdesign, List<? extends CollectableEntityField> lstclctefweSelected) {
		List<FieldDefinition> fields = new ArrayList<PDFHelper.FieldDefinition>();
		for (CollectableEntityField f : lstclctefweSelected) {
			fields.add(new PDFHelper.FieldDefinition(getFieldName(f), f.getJavaClass(), f.getMaxLength() != null ? f.getMaxLength() : 0, getLabel(f)));
		}
		PDFHelper.createFieldsInternal(jrdesign, fields);
	}

	public static void createFieldsInternal(JasperDesign jrdesign, List<FieldDefinition> fields) {
		final int DIN_A4_HEIGHT = 842;
		final int DIN_A4_WIDTH = 595;
		final int MAX_DATE_WIDTH = 10;
		final int MAX_STRING_WIDTH = 25; // extended = long strings width char > 25
		final int MAX_BOOLEAN_WIDTH = 4;
		final int MAX_INTEGER_WIDTH = 5;
		final int MAX_DOUBLE_WIDTH = 7;

		final JRDesignBand pageHeader = (JRDesignBand) jrdesign.getPageHeader();
		final JRDesignBand columnHeader = (JRDesignBand) jrdesign.getColumnHeader();
		final JRDesignBand detail = (JRDesignBand) jrdesign.getDetail();
		final JRReportFont regularFont = (JRReportFont) jrdesign.getFontsMap().get("Regular");
		final JRReportFont boldFont = (JRReportFont) jrdesign.getFontsMap().get("Bold");
		final Font fontPlain = new Font(regularFont.getName(), Font.PLAIN, regularFont.getSize());
		final Font fontBold = new Font(regularFont.getName(), Font.BOLD, boldFont.getSize());
		if (pageHeader == null) {
			throw new CommonFatalException(SpringLocaleDelegate.getInstance().getMessage(
					"ReportController.5","Bereich <PageHeader> muss in der Suchergebnisvorlage definiert sein."));
		}
		if (columnHeader == null) {
			throw new CommonFatalException(SpringLocaleDelegate.getInstance().getMessage(
					"ReportController.3","Bereich <ColumnHeader> muss in der Suchergebnisvorlage definiert sein."));
		}
		if (detail == null) {
			throw new CommonFatalException(SpringLocaleDelegate.getInstance().getMessage(
					"ReportController.4","Bereich <Detail> muss in der Suchergebnisvorlage definiert sein."));
		}

		// set the column headers:
		final FontRenderContext fontrenderctx = new FontRenderContext(null, false, true);
		int iLabelWidth = 0;
		int iFieldWidth = 0;
		int iCurrentX = 0;

		final double dCharWidth = fontPlain.getStringBounds("M", fontrenderctx).getWidth() + 1;
		final int iIntegerFieldWidth = (int) dCharWidth * MAX_INTEGER_WIDTH;
		final int iDoubleFieldWidth = (int) dCharWidth * MAX_DOUBLE_WIDTH;
		final int iDateFieldWidth = (int) dCharWidth * MAX_DATE_WIDTH;
		final int iBooleanFieldWidth = (int) dCharWidth * MAX_BOOLEAN_WIDTH;
		final int iStringFieldWidth = (int) dCharWidth * MAX_STRING_WIDTH;

		for (FieldDefinition f : fields) {
			final String sLabel = f.getLabel();
			iLabelWidth = (int) fontBold.getStringBounds(sLabel, fontrenderctx).getWidth() + 1;

			byte textAlign = JRTextElement.TEXT_ALIGN_LEFT;

			final String sClassName = f.getJavaClass().getName();
			if (Date.class.isAssignableFrom(f.getJavaClass())) {
				iFieldWidth = Math.max(iLabelWidth, iDateFieldWidth);
			}
			else if (sClassName.equals("java.lang.String")) {
				final Integer iScale = f.getMaxLength();
				if (iScale != null && iScale > MAX_STRING_WIDTH) {
					iFieldWidth = Math.max(iLabelWidth, iStringFieldWidth);
				}
				else {
					iFieldWidth = iLabelWidth + 10;
				}
			}
			else if (sClassName.equals("java.lang.Boolean")) {
				iFieldWidth = Math.max(iLabelWidth, iBooleanFieldWidth);
			}
			else if (sClassName.equals("java.lang.Integer")) {
				iFieldWidth = Math.max(iLabelWidth, iIntegerFieldWidth);
				textAlign = JRTextElement.TEXT_ALIGN_RIGHT;
			}
			else if (sClassName.equals("java.lang.Double")) {
				iFieldWidth = Math.max(iLabelWidth, iDoubleFieldWidth);
				textAlign = JRTextElement.TEXT_ALIGN_RIGHT;
			}
			else if (Timestamp.class.isAssignableFrom(f.getJavaClass())) {
				iFieldWidth = Math.max(iLabelWidth, iDateFieldWidth);
			}
			else {
				iFieldWidth = MAX_STRING_WIDTH;
			}

			final String sFieldName = f.getName();

			final JRDesignField jrdesignfield = new JRDesignField();
			jrdesignfield.setName(sFieldName);
			try {
				jrdesignfield.setValueClass(String.class);
				jrdesign.addField(jrdesignfield);
			}
			catch (JRException ex) {
				throw new CommonFatalException(ex);
			}

			detail.setSplitAllowed(true);

			JRDesignStaticText staticField = new JRDesignStaticText();
			staticField.setX(iCurrentX);
			staticField.setWidth(iFieldWidth);
			staticField.setHeight(14);
			staticField.setTextAlignment(textAlign);
			staticField.setText(sLabel);
			staticField.setFont(boldFont);
			columnHeader.addElement(staticField);

			JRDesignTextField dataField = new JRDesignTextField();
			dataField.setX(iCurrentX);
			dataField.setWidth(iFieldWidth);
			dataField.setHeight(14);
			dataField.setTextAlignment(textAlign);
			dataField.setFont(regularFont);
			dataField.setBlankWhenNull(true);

			final String sFieldValue = "$F{" + sFieldName + "}";

			JRDesignExpression expression = new JRDesignExpression();
			expression.setText(sFieldValue);
			expression.setValueClass(String.class);
			dataField.setExpression(expression);
			dataField.setStretchWithOverflow(true);
			detail.addElement(dataField);

			iCurrentX += iFieldWidth + 10;
		}
		iCurrentX += 50;

		if (iCurrentX < DIN_A4_WIDTH) {
			jrdesign.setOrientation(JRReport.ORIENTATION_PORTRAIT);
			jrdesign.setPageWidth(DIN_A4_WIDTH);
			jrdesign.setPageHeight(DIN_A4_HEIGHT);
		}
		else if (iCurrentX < DIN_A4_HEIGHT) {
			jrdesign.setOrientation(JRReport.ORIENTATION_LANDSCAPE);
			jrdesign.setPageWidth(DIN_A4_HEIGHT);
			jrdesign.setPageHeight(DIN_A4_WIDTH);
		}
		else {
			jrdesign.setOrientation(JRReport.ORIENTATION_LANDSCAPE);
			jrdesign.setPageWidth(iCurrentX);
			jrdesign.setPageHeight((int) (iCurrentX / 1.41));	// page width/height relation by DIN...
		}
	}

	private static List<FieldDefinition> getFieldDefinition(TableModel tableModel) {
		List<FieldDefinition> fields = new ArrayList<FieldDefinition>();
		for(int i = 0; i < tableModel.getColumnCount(); i++) {
			Class<?> clazz = String.class;
			int rowCount = tableModel.getRowCount();
			rowCount = rowCount > 100 ? 100 : rowCount;
			int result = 25;
			for(int j = 0; j < rowCount; j++) {
				final Object oValue = tableModel.getValueAt(j, i);
				if(oValue != null) {
					clazz = oValue.getClass();
					final int iLength = oValue.toString().length();
					result = (iLength < result) ? result : iLength;
				}
			}
			fields.add(new FieldDefinition(getFieldName(tableModel.getColumnName(i)), clazz, result, tableModel.getColumnName(i)));
		}
		return fields;
	}

	public static class FieldDefinition implements Serializable {

		private String name;
		private Class<?> clazz;
		private int maxlength;
		private String label;

		public FieldDefinition(String name, Class<?> clazz, int maxlength, String label) {
			super();
			this.name = name;
			this.clazz = clazz;
			this.maxlength = maxlength;
			this.label = label;
		}

		public FieldDefinition(String name, Class<?> clazz, String label) {
			super();
			this.name = name;
			this.clazz = clazz;
			this.label = label;
		}

		public String getName() {
			return name;
		}

		public Class<?> getJavaClass() {
			return clazz;
		}

		public int getMaxLength() {
			return maxlength;
		}

		public String getLabel() {
			return label;
		}

		@Override
		public String toString() {
			return "FieldDefinition [name=" + name + ", clazz=" + clazz
				+ ", maxlength=" + maxlength + "]";
		}
	}
}
