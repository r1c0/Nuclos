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

public class ConversionOptionsPropertiesWriter {

	public Properties write(Java2HtmlConversionOptions options) {
		Properties properties = new LinkedProperties();
		properties.setProperty(IConversionOptionsConstants.DEFAULT_STYLE_NAME, options.getStyleTable().getName());
		properties.setProperty(IConversionOptionsConstants.SHOW_FILE_NAME, String.valueOf(options.isShowFileName()));
		properties.setProperty(
				IConversionOptionsConstants.SHOW_TABLE_BORDER,
				String.valueOf(options.isShowTableBorder()));
		properties.setProperty(
				IConversionOptionsConstants.SHOW_LINE_NUMBERS,
				String.valueOf(options.isShowLineNumbers()));
		properties.setProperty(
				IConversionOptionsConstants.SHOW_JAVA2HTML_LINK,
				String.valueOf(options.isShowJava2HtmlLink()));
		properties.setProperty(
				IConversionOptionsConstants.HORIZONTAL_ALIGNMENT,
				options.getHorizontalAlignment().getName());
		properties.setProperty(IConversionOptionsConstants.TAB_SIZE, String.valueOf(options.getTabSize()));

		addStyleEntries(properties, options.getStyleTable());
		return properties;
	}

	private void addStyleEntries(Properties properties, JavaSourceStyleTable table) {
		JavaSourceType[] sourceTypes = JavaSourceType.getAll();
		for (int i = 0; i < sourceTypes.length; i++) {
			JavaSourceType type = sourceTypes[i];
			JavaSourceStyleEntry entry = table.get(type);
			properties.setProperty(
					type.getName() + IConversionOptionsConstants.POSTFIX_COLOR,
					getRgbString(entry.getColor()));
			properties.setProperty(
					type.getName() + IConversionOptionsConstants.POSTFIX_BOLD,
					String.valueOf(entry.isBold()));
			properties.setProperty(
					type.getName() + IConversionOptionsConstants.POSTFIX_ITALIC,
					String.valueOf(entry.isItalic()));
		}
	}

	private String getRgbString(RGB color) {
		return color.getRed() + "," + color.getGreen() + "," + color.getBlue();
	}
}
