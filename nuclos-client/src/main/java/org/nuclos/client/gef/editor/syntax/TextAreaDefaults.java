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
/*
 * TextAreaDefaults.java - Encapsulates default values for various settings
 * Copyright (C) 1999 Slava Pestov
 *
 * You may use and modify this package for any purpose. Redistribution is
 * permitted, in both source and binary form, provided that this notice
 * remains intact in all source distributions of this package.
 */

package org.nuclos.client.gef.editor.syntax;

import java.awt.Color;

import javax.swing.JPopupMenu;

/**
 * Encapsulates default settings for a text area. This can be passed
 * to the constructor once the necessary fields have been filled out.
 * The advantage of doing this over calling lots of set() methods after
 * creating the text area is that this method is faster.
 */
public class TextAreaDefaults {
	public InputHandler inputHandler;
	public SyntaxDocument document;
	public boolean editable;

	public boolean caretVisible;
	public boolean caretBlinks;
	public boolean blockCaret;
	public int electricScroll;

	public int cols;
	public int rows;
	public SyntaxStyle[] styles;
	public Color caretColor;
	public Color selectionColor;
	public Color lineHighlightColor;
	public boolean lineHighlight;
	public Color bracketHighlightColor;
	public boolean bracketHighlight;
	public Color eolMarkerColor;
	public boolean eolMarkers;
	public boolean paintInvalid;

	public JPopupMenu popup;

	public TextAreaDefaults() {
		this.inputHandler = new DefaultInputHandler();
		this.inputHandler.addDefaultKeyBindings();
		this.document = new SyntaxDocument();
		this.editable = true;

		this.blockCaret = false;
		this.caretVisible = true;
		this.caretBlinks = true;
		this.electricScroll = 3;

		this.cols = 80;
		this.rows = 25;
		this.styles = SyntaxUtilities.getDefaultSyntaxStyles();
		this.caretColor = Color.black; // Color.red;
		this.selectionColor = new Color(0xccccff);
		this.lineHighlightColor = new Color(0xe0e0e0);
		this.lineHighlight = true;
		this.bracketHighlightColor = Color.black;
		this.bracketHighlight = true;
		this.eolMarkerColor = new Color(0x009999);
		this.eolMarkers = false; // true;
		this.paintInvalid = false; //true;
	}

}	// class TestAreaDefaults
