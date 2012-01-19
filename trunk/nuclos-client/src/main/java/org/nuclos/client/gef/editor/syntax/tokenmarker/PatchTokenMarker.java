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
 * PatchTokenMarker.java - DIFF patch token marker
 * Copyright (C) 1999 Slava Pestov
 *
 * You may use and modify this package for any purpose. Redistribution is
 * permitted, in both source and binary form, provided that this notice
 * remains intact in all source distributions of this package.
 */

package org.nuclos.client.gef.editor.syntax.tokenmarker;

import javax.swing.text.Segment;

/**
 * Patch/diff token marker.
 *
 * @author Slava Pestov
 * @version $Id: PatchTokenMarker.java,v 1.1 2007-05-11 08:18:09 radig Exp $
 */
public class PatchTokenMarker extends TokenMarker {
	@Override
	public byte markTokensImpl(byte token, Segment line, int lineIndex) {
		if (line.count == 0) {
			return Token.NULL;
		}
		switch (line.array[line.offset]) {
			case '+':
			case '>':
				addToken(line.count, Token.KEYWORD1);
				break;
			case '-':
			case '<':
				addToken(line.count, Token.KEYWORD2);
				break;
			case '@':
			case '*':
				addToken(line.count, Token.KEYWORD3);
				break;
			default:
				addToken(line.count, Token.NULL);
				break;
		}
		return Token.NULL;
	}

	@Override
	public boolean supportsMultilineTokens() {
		return false;
	}
}
