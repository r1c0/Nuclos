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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Java2HtmlConversionOptions {
	//Attribute names for persistence (e.g. in the eclipse plugin

	private static final String PROPERTIES_FILE_NAME = "java2html.properties";

	public final static String TAB_SIZE = IConversionOptionsConstants.TAB_SIZE;

	public final static String SHOW_LINE_NUMBERS = IConversionOptionsConstants.SHOW_LINE_NUMBERS;

	public final static String SHOW_FILE_NAME = IConversionOptionsConstants.SHOW_FILE_NAME;

	public final static String SHOW_TABLE_BORDER = IConversionOptionsConstants.SHOW_TABLE_BORDER;

	private static Java2HtmlConversionOptions defaultOptions;

	public static Java2HtmlConversionOptions getRawDefault() {
		return new Java2HtmlConversionOptions();
	}

	public static Java2HtmlConversionOptions getDefault() throws IllegalConfigurationException {
		if (defaultOptions == null) {
			defaultOptions = createDefaultOptions();
		}
		return defaultOptions.getClone();
	}

	private static Java2HtmlConversionOptions createDefaultOptions() throws IllegalConfigurationException {
		InputStream inputStream =
				Java2HtmlConversionOptions.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE_NAME);
		if (inputStream == null) {
			return new Java2HtmlConversionOptions();
		}

		Properties properties = new Properties();
		try {
			properties.load(inputStream);
			return new ConversionOptionsPropertiesReader().read(properties);
		}
		catch (IOException exception) {
			throw new IllegalConfigurationException(
					"Error loading configuration file '" + PROPERTIES_FILE_NAME + "' from classpath",
					exception);
		}
		catch (IllegalArgumentException exception) {
			throw new IllegalConfigurationException(
					"Error loading configuration file '" + PROPERTIES_FILE_NAME + "' from classpath",
					exception);
		}
		finally {
			IoUtilities.close(inputStream);
		}
	}

	private JavaSourceStyleTable styleTable = JavaSourceStyleTable.getDefault();
	private int tabSize = 2;
	private boolean showLineNumbers = false;
	private boolean showFileName = false;
	private boolean showTableBorder = false;

	/**
	 * Flag indication whether html output contains a link to the
	 * Java2Html-Homepage or not.
	 */
	private boolean showJava2HtmlLink = false;
	private boolean addLineAnchors = false;
	private String lineAnchorPrefix = "";
	private HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;

	private Java2HtmlConversionOptions() {
		//nothing to do
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Java2HtmlConversionOptions)) {
			return false;
		}
		Java2HtmlConversionOptions other = (Java2HtmlConversionOptions) obj;
		return (
				other.tabSize == tabSize
						&& other.styleTable.equals(styleTable)
						&& other.showFileName == showFileName
						&& other.showJava2HtmlLink == showJava2HtmlLink
						&& other.showLineNumbers == showLineNumbers
						&& other.showTableBorder == showTableBorder
						&& other.horizontalAlignment == horizontalAlignment);
	}

	@Override
	public int hashCode() {
		return styleTable.hashCode() + tabSize;
	}

	public Java2HtmlConversionOptions getClone() {
		Java2HtmlConversionOptions options = new Java2HtmlConversionOptions();
		options.styleTable = styleTable.getClone();
		options.tabSize = tabSize;
		options.showLineNumbers = showLineNumbers;
		options.showFileName = showFileName;
		options.showJava2HtmlLink = showJava2HtmlLink;
		options.showTableBorder = showTableBorder;
		options.horizontalAlignment = horizontalAlignment;
		return options;
	}

	public void setStyleTable(JavaSourceStyleTable styleTable) {
		Ensure.ensureArgumentNotNull(styleTable);
		this.styleTable = styleTable;
	}

	public JavaSourceStyleTable getStyleTable() {
		return styleTable;
	}

	public int getTabSize() {
		return tabSize;
	}

	public void setTabSize(int tabSize) {
		this.tabSize = tabSize;
	}

	public boolean isShowLineNumbers() {
		return showLineNumbers;
	}

	public void setShowLineNumbers(boolean showLineNumbers) {
		this.showLineNumbers = showLineNumbers;
	}

	public boolean isShowFileName() {
		return showFileName;
	}

	public boolean isShowTableBorder() {
		return showTableBorder;
	}

	public void setShowFileName(boolean showFileName) {
		this.showFileName = showFileName;
	}

	public void setShowTableBorder(boolean showTableBorder) {
		this.showTableBorder = showTableBorder;
	}

	public boolean isAddLineAnchors() {
		return addLineAnchors;
	}

	public String getLineAnchorPrefix() {
		return lineAnchorPrefix;
	}

	public void setAddLineAnchors(boolean addLineAnchors) {
		this.addLineAnchors = addLineAnchors;
	}

	public void setLineAnchorPrefix(String lineAnchorPrefix) {
		this.lineAnchorPrefix = lineAnchorPrefix;
	}

	public HorizontalAlignment getHorizontalAlignment() {
		return horizontalAlignment;
	}

	public void setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
		Ensure.ensureArgumentNotNull(horizontalAlignment);
		this.horizontalAlignment = horizontalAlignment;
	}

	public boolean isShowJava2HtmlLink() {
		return showJava2HtmlLink;
	}

	public void setShowJava2HtmlLink(boolean isShowJava2HtmlLink) {
		this.showJava2HtmlLink = isShowJava2HtmlLink;
	}
}
