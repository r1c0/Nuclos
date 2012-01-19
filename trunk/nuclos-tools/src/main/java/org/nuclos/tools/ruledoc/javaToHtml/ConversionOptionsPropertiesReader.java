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
package org.nuclos.tools.ruledoc.javaToHtml;

import java.util.Properties;
import java.util.StringTokenizer;

public class ConversionOptionsPropertiesReader {

	public Java2HtmlConversionOptions read(Properties properties) throws IllegalPropertyValueException {
		Java2HtmlConversionOptions conversionOptions = Java2HtmlConversionOptions.getRawDefault();
		adjustStyleTable(properties, conversionOptions);
		adjustShowFileName(properties, conversionOptions);
		adjustBorder(properties, conversionOptions);
		adjustLineNumbers(properties, conversionOptions);
		adjustShowJava2HtmlLink(properties, conversionOptions);
		adjustTabSize(properties, conversionOptions);
		adjustAlignment(properties, conversionOptions);
		adjustStyleEntries(properties, conversionOptions);
		return conversionOptions;
	}

	private void adjustAlignment(Properties properties, Java2HtmlConversionOptions conversionOptions)
			throws IllegalPropertyValueException {
		String alignmentValue = properties.getProperty(IConversionOptionsConstants.HORIZONTAL_ALIGNMENT);
		if (alignmentValue != null) {
			HorizontalAlignment alignment = HorizontalAlignment.getByName(alignmentValue);
			if (alignment == null) {
				throw new IllegalPropertyValueException(
						IConversionOptionsConstants.HORIZONTAL_ALIGNMENT,
						alignmentValue,
						ConversionOptionsUtilities.getAvailableHorizontalAlignmentNames());
			}
			conversionOptions.setHorizontalAlignment(alignment);
		}
	}

	private void adjustTabSize(Properties properties, Java2HtmlConversionOptions conversionOptions)
			throws IllegalPropertyValueException {
		String tabSizeValue = properties.getProperty(IConversionOptionsConstants.TAB_SIZE);
		if (tabSizeValue != null) {
			try {
				int tabSize = Integer.parseInt(tabSizeValue);
				if (tabSize < 0) {
					throw new NumberFormatException();
				}
				conversionOptions.setTabSize(tabSize);
			}
			catch (NumberFormatException e) {
				throw new IllegalPropertyValueException(IConversionOptionsConstants.TAB_SIZE, tabSizeValue);
			}
		}
	}

	private void adjustShowFileName(Properties properties, Java2HtmlConversionOptions conversionOptions)
			throws IllegalPropertyValueException {
		String value = properties.getProperty(IConversionOptionsConstants.SHOW_FILE_NAME);
		if (value != null) {
			conversionOptions.setShowFileName(parseBooleanValue(IConversionOptionsConstants.SHOW_FILE_NAME, value));
		}
	}

	private void adjustShowJava2HtmlLink(Properties properties, Java2HtmlConversionOptions conversionOptions)
			throws IllegalPropertyValueException {
		String value = properties.getProperty(IConversionOptionsConstants.SHOW_JAVA2HTML_LINK);
		if (value != null) {
			conversionOptions.setShowJava2HtmlLink(
					parseBooleanValue(IConversionOptionsConstants.SHOW_JAVA2HTML_LINK, value));
		}
	}

	private void adjustLineNumbers(Properties properties, Java2HtmlConversionOptions conversionOptions)
			throws IllegalPropertyValueException {
		String value = properties.getProperty(IConversionOptionsConstants.SHOW_LINE_NUMBERS);
		if (value != null) {
			conversionOptions.setShowLineNumbers(
					parseBooleanValue(IConversionOptionsConstants.SHOW_LINE_NUMBERS, value));
		}
	}

	private void adjustBorder(Properties properties, Java2HtmlConversionOptions conversionOptions)
			throws IllegalPropertyValueException {
		String value = properties.getProperty(IConversionOptionsConstants.SHOW_TABLE_BORDER);
		if (value != null) {
			conversionOptions.setShowTableBorder(
					parseBooleanValue(IConversionOptionsConstants.SHOW_TABLE_BORDER, value));
		}
	}

	private void adjustStyleTable(Properties properties, Java2HtmlConversionOptions conversionOptions)
			throws IllegalPropertyValueException {
		String defaultTableName = properties.getProperty(IConversionOptionsConstants.DEFAULT_STYLE_NAME);
		if (defaultTableName != null) {
			JavaSourceStyleTable table = JavaSourceStyleTable.getPredefinedTable(defaultTableName);
			if (table == null) {
				throw new IllegalPropertyValueException(
						IConversionOptionsConstants.DEFAULT_STYLE_NAME,
						defaultTableName,
						ConversionOptionsUtilities.getPredefinedStyleTableNames());
			}
			conversionOptions.setStyleTable(table);
		}
	}

	private boolean parseBooleanValue(String propertyName, String value) throws IllegalPropertyValueException {
		if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes")) {
			return true;
		}
		if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("no")) {
			return false;
		}
		throw new IllegalPropertyValueException(propertyName, value, new String[] {"true", "false"});
	}

	private RGB parseColorValue(String propertyName, String colorValue) throws IllegalPropertyValueException {
		StringTokenizer tokenizer = new StringTokenizer(colorValue, ","); //$NON-NLS-1$
		if (tokenizer.countTokens() != 3) {
			throw new IllegalPropertyValueException(propertyName, colorValue);
		}
		String redValue = tokenizer.nextToken();
		String greenValue = tokenizer.nextToken();
		String blueValue = tokenizer.nextToken();
		int red = 0, green = 0, blue = 0;
		try {
			red = Integer.parseInt(redValue);
			green = Integer.parseInt(greenValue);
			blue = Integer.parseInt(blueValue);
		}
		catch (NumberFormatException e) {
			throw new IllegalPropertyValueException(propertyName, colorValue);
		}
		if (red > 255 || red < 0 || green > 255 || green < 0 || blue > 255 || blue < 0) {
			throw new IllegalPropertyValueException(propertyName, colorValue);
		}
		return new RGB(red, green, blue);
	}

	private void adjustStyleEntries(Properties properties, Java2HtmlConversionOptions conversionOptions)
			throws IllegalPropertyValueException {
		JavaSourceType[] sourceTypes = JavaSourceType.getAll();
		for (int i = 0; i < sourceTypes.length; i++) {
			adjustStyleEntry(properties, conversionOptions, sourceTypes[i]);
		}
	}

	private void adjustStyleEntry(
			Properties properties,
			Java2HtmlConversionOptions conversionOptions,
			JavaSourceType type)
			throws IllegalPropertyValueException {
		RGB color = getColor(properties, conversionOptions, type);
		boolean bold = getBold(properties, conversionOptions, type);
		boolean italic = getItalic(properties, conversionOptions, type);
		conversionOptions.getStyleTable().put(type, new JavaSourceStyleEntry(color, bold, italic));
	}

	private boolean getItalic(
			Properties properties,
			Java2HtmlConversionOptions conversionOptions,
			JavaSourceType type)
			throws IllegalPropertyValueException {

		String italicPropertyName = type.getName() + IConversionOptionsConstants.POSTFIX_ITALIC;
		String italicValue = properties.getProperty(italicPropertyName);
		if (italicValue != null) {
			return parseBooleanValue(italicPropertyName, italicValue);
		}
		return conversionOptions.getStyleTable().get(type).isItalic();
	}

	private boolean getBold(
			Properties properties,
			Java2HtmlConversionOptions conversionOptions,
			JavaSourceType type)
			throws IllegalPropertyValueException {
		String boldPropertyName = type.getName() + IConversionOptionsConstants.POSTFIX_BOLD;
		String boldValue = properties.getProperty(boldPropertyName);
		if (boldValue != null) {
			return parseBooleanValue(boldPropertyName, boldValue);
		}
		return conversionOptions.getStyleTable().get(type).isBold();
	}

	private RGB getColor(
			Properties properties,
			Java2HtmlConversionOptions conversionOptions,
			JavaSourceType type)
			throws IllegalPropertyValueException {
		String colorPropertyName = type.getName() + IConversionOptionsConstants.POSTFIX_COLOR;
		String colorValue = properties.getProperty(colorPropertyName);
		if (colorValue != null) {
			return parseColorValue(colorPropertyName, colorValue);
		}
		return conversionOptions.getStyleTable().get(type).getColor();
	}
}
