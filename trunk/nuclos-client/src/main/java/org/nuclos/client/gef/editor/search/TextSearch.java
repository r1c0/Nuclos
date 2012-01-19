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
package org.nuclos.client.gef.editor.search;

import org.nuclos.client.gef.editor.CommonEditor;
import javax.swing.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Text searcher.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class TextSearch {
	/**
	 *
	 */
	public static final int SEARCH_FORWARD = 1;
	/**
	 *
	 */
	public static final int SEARCH_BACKWARD = 2;
	/**
	 *
	 */
	public static final int ACTION_SEARCH = 1;
	/**
	 *
	 */
	public static final int ACTION_REPLACE = 2;
	/**
	 *
	 */
	protected static TextSearch instance = null;

	protected String sSearchString = "";
	protected String sReplaceString = "";
	protected boolean bCaseSensitive = false;
	protected boolean bWholeWord = false;
	protected boolean bAgain = false;
	protected boolean bReplaceAll = false;
	protected boolean bApprove = false;
	protected boolean bCurrentPos = false;
	protected int iDirection = 0;
	protected int iLastAction = 0;
	protected int iStartIndex = -1;
	protected int iEndIndex = -1;
	protected int iLastFound = -1;
	protected CommonEditor editor = null;
	protected Pattern searchPattern;
	protected Matcher matcher;

	protected int lastFound;
	protected int lastCaretPos;

	/**
	 *
	 */
	private TextSearch() {
		sSearchString = "";
		sReplaceString = "";
		bCaseSensitive = false;
		bWholeWord = false;
		iDirection = SEARCH_FORWARD;
		lastFound = -1;
	}

	/**
	 *
	 * @param editor
	 * @param sSearchText
	 * @param bCaseSensitive
	 * @param bWholeWord
	 * @param iDirection
	 * @param bCurrentPos
	 * @return
	 */
	public static TextSearch prepareSearch(CommonEditor editor, String sSearchText, boolean bCaseSensitive, boolean bWholeWord, int iDirection,
			boolean bCurrentPos) {
		if (instance == null) {
			instance = new TextSearch();
		}
		instance.editor = editor;
		instance.sSearchString = sSearchText;
		instance.bCaseSensitive = bCaseSensitive;
		instance.bWholeWord = bWholeWord;
		instance.iDirection = iDirection;
		instance.lastFound = -1;
		instance.lastCaretPos = editor.getCaretPosition();
		instance.bAgain = false;
		instance.bReplaceAll = false;
		instance.bApprove = false;
		instance.bCurrentPos = bCurrentPos;
		instance.iLastAction = ACTION_SEARCH;

		String sPattern = instance.getPatternString();
		instance.searchPattern = instance.getPattern(sPattern);
		instance.initIndices();
		instance.matcher = instance.getMatcher();
		return instance;
	}

	/**
	 *
	 * @param editor
	 * @param sSearchText
	 * @param sReplaceText
	 * @param bCaseSensitive
	 * @param bWholeWord
	 * @param iDirection
	 * @param bReplaceAll
	 * @param bApprove
	 * @param bCurrentPos
	 * @return
	 */
	public static TextSearch prepareReplace(CommonEditor editor, String sSearchText, String sReplaceText, boolean bCaseSensitive, boolean bWholeWord, int iDirection,
			boolean bReplaceAll, boolean bApprove, boolean bCurrentPos) {
		prepareSearch(editor, sSearchText, bCaseSensitive, bWholeWord, iDirection, bCurrentPos);

		instance.sReplaceString = sReplaceText;
		instance.bReplaceAll = bReplaceAll;
		instance.bApprove = bApprove;
		instance.iLastAction = ACTION_REPLACE;
		return instance;
	}

	/**
	 *
	 * @return
	 */
	public synchronized static TextSearch getInstance() {
		if (instance == null) {
			instance = new TextSearch();
		}
		return instance;
	}

	/**
	 *
	 * @return
	 */
	public String getSearchString() {
		return sSearchString;
	}

	/**
	 *
	 * @param sSearchString
	 */
	public void setSearchString(String sSearchString) {
		this.sSearchString = sSearchString;
	}

	/**
	 *
	 * @return
	 */
	public String getReplaceString() {
		return sReplaceString;
	}

	/**
	 *
	 * @param sReplaceString
	 */
	public void setReplaceString(String sReplaceString) {
		this.sReplaceString = sReplaceString;
	}

	/**
	 *
	 * @return
	 */
	public boolean isCaseSensitive() {
		return bCaseSensitive;
	}

	/**
	 *
	 * @param bCaseSensitive
	 */
	public void setCaseSensitive(boolean bCaseSensitive) {
		this.bCaseSensitive = bCaseSensitive;
	}

	/**
	 *
	 * @return
	 */
	public boolean isWholeWord() {
		return bWholeWord;
	}

	/**
	 *
	 * @param bWholeWord
	 */
	public void setWholeWord(boolean bWholeWord) {
		this.bWholeWord = bWholeWord;
	}

	/**
	 *
	 * @return
	 */
	public int getDirection() {
		return iDirection;
	}

	/**
	 *
	 * @param iDirection
	 */
	public void setDirection(int iDirection) {
		this.iDirection = iDirection;
	}

	/**
	 *
	 * @return
	 */
	public boolean isApprove() {
		return bApprove;
	}

	/**
	 *
	 * @param bApprove
	 */
	public void setApprove(boolean bApprove) {
		this.bApprove = bApprove;
	}

	/**
	 *
	 * @return
	 */
	public boolean isCurrentPos() {
		return bCurrentPos;
	}

	/**
	 *
	 * @param bCurrentPos
	 */
	public void setCurrentPos(boolean bCurrentPos) {
		this.bCurrentPos = bCurrentPos;
	}

	/**
	 *
	 * @return
	 */
	public boolean isReplaceAll() {
		return bReplaceAll;
	}

	/**
	 *
	 * @param bReplaceAll
	 */
	public void setReplaceAll(boolean bReplaceAll) {
		this.bReplaceAll = bReplaceAll;
	}

	/**
	 *
	 * @return
	 */
	public CommonEditor getEditor() {
		return editor;
	}

	/**
	 *
	 * @param editor
	 */
	public void setEditor(CommonEditor editor) {
		this.editor = editor;
	}

	/**
	 *
	 * @return
	 */
	public boolean search() {
		boolean bResult = false;
		int iFoundStart = -1, iFoundEnd = -1;

		if (editor == null) {
			return false;
		}

		switch (iDirection) {
			case SEARCH_FORWARD:
				if (bResult = matcher.find()) {
					editor.setCaretPosition(iStartIndex + matcher.end() - (bWholeWord ? 1 : 0));
					editor.select(iStartIndex + matcher.start() + (bWholeWord ? 1 : 0), iStartIndex + matcher.end() - (bWholeWord ? 1 : 0));
					bAgain = true;
				}
				else {
					JOptionPane.showMessageDialog(editor, "Keine \u00dcbereinstimmungen mehr gefunden");
				}
				break;
			case SEARCH_BACKWARD:
				if (bAgain) {
					iStartIndex = iLastFound;
				}
				matcher = instance.searchPattern.matcher(editor.getText().subSequence(instance.iEndIndex, instance.iStartIndex));
				do {
					if (bResult = matcher.find()) {
						iLastFound = iFoundStart = matcher.start();
						iFoundEnd = matcher.end();
						bAgain = true;
					}
				} while (bResult);
				if (iFoundStart > 0) {
					editor.setCaretPosition(iFoundEnd - (bWholeWord ? 1 : 0));
					editor.select(iFoundStart + (bWholeWord ? 1 : 0), iFoundEnd - (bWholeWord ? 1 : 0));
				}
				else {
					JOptionPane.showMessageDialog(editor, "Keine \u00dcbereinstimmungen mehr gefunden");
				}
				break;
		}
		return bResult;
	}

	/**
	 *
	 * @return
	 */
	public boolean replace() {
		boolean bResult = false;
		if (editor == null) {
			return false;
		}

		switch (iDirection) {
			case SEARCH_FORWARD:
				if (bAgain) {
					String sPattern = getPatternString();
					searchPattern = getPattern(sPattern);
					initIndices();
					matcher = getMatcher();
				}

				try {

					String sMatched = "";
					if (bReplaceAll) {
						sMatched = matcher.replaceAll(sReplaceString);
						editor.setText(sMatched);
					}
					else {
						sMatched = matcher.replaceFirst(sReplaceString);
						bResult = sMatched.length() > 0;
						if (bResult) {
							editor.setText(sMatched);
							editor.setCaretPosition(matcher.start() + sReplaceString.length());
							editor.select(matcher.start(), matcher.start() + sReplaceString.length());
							bAgain = true;
						}
					}
				}
				catch (IllegalStateException e) {
					bResult = false;
					JOptionPane.showMessageDialog(editor, "Keine \u00dcbereinstimmungen mehr gefunden");
				}
			case SEARCH_BACKWARD:
				break;

		}
		return bResult;
	}

	/**
	 *
	 * @return
	 */
	private String getPatternString() {
		String sPattern;
		if (bWholeWord) {
			sPattern = "\\W" + sSearchString + "\\W";
		}
		else {
			sPattern = sSearchString;
		}
		return sPattern;
	}

	/**
	 *
	 * @param sPatternString
	 * @return
	 */
	private Pattern getPattern(String sPatternString) {
		Pattern pattern = null;
		if (bCaseSensitive) {
			pattern = Pattern.compile(sPatternString);
		}
		else {
			pattern = Pattern.compile(sPatternString, Pattern.CASE_INSENSITIVE);
		}
		return pattern;
	}

	/**
	 *
	 */
	private void initIndices() {
		switch (iDirection) {
			case SEARCH_FORWARD:
				instance.iStartIndex = (instance.bCurrentPos ? instance.lastCaretPos : 0);
				instance.iEndIndex = editor.getDocument().getLength() - 1;
				break;
			case SEARCH_BACKWARD:
				instance.iStartIndex = (instance.bCurrentPos ? instance.lastCaretPos : editor.getDocument().getLength() - 1);
				instance.iEndIndex = 0;
				break;
		}
	}

	private Matcher getMatcher() {
		Matcher matcher = null;
		switch (iDirection) {
			case SEARCH_FORWARD:
				matcher = instance.searchPattern.matcher(editor.getText().subSequence(instance.iStartIndex, instance.iEndIndex));
				break;
			case SEARCH_BACKWARD:
				matcher = instance.searchPattern.matcher(editor.getText().subSequence(instance.iEndIndex, instance.iStartIndex));
				break;
		}
		return matcher;
	}

	/**
	 *
	 * @return
	 */
	public int getLastAction() {
		return iLastAction;
	}
}
