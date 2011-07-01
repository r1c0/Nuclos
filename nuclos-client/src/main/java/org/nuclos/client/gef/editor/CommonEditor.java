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
package org.nuclos.client.gef.editor;

import org.nuclos.client.gef.editor.search.SearchPanel;
import org.nuclos.client.gef.editor.search.TextSearch;
import org.nuclos.client.gef.editor.syntax.JEditTextArea;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Implementation of a syntax highlighting text edit component.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class CommonEditor extends JEditTextArea {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int iTabSize;

	public class SearchAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public SearchAction() {
			super("Suchen");
			this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			search();
		}
	}

	public class SearchAgainAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public SearchAgainAction() {
			super("Weitersuchen");
			this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("F3"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			searchAgain();
		}
	}

	public class ReplaceAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ReplaceAction() {
			super("Ersetzen");
			this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			replace();
		}
	}

	public class SelectAllAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public SelectAllAction() {
			super("Alles markieren");
			this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			selectAll();
		}
	}

	public class CutAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public CutAction() {
			super("Ausschneiden");
			this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			cut();
		}
	}

	public class CopyAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public CopyAction() {
			super("Kopieren");
			this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			copy();
		}
	}

	public class PasteAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public PasteAction() {
			super("Einf\u00fcgen");
			this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			paste();
		}
	}

	public class UndoAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public UndoAction() {
			super("R\u00fcckg\u00e4ngig");
			this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			undo();
		}
	}

	private final SearchAction actSearch = new SearchAction();
	private final SearchAgainAction actSearchAgain = new SearchAgainAction();
	private final ReplaceAction actReplace = new ReplaceAction();
	private final SelectAllAction actSelectAll = new SelectAllAction();
	private final CutAction actCut = new CutAction();
	private final CopyAction actCopy = new CopyAction();
	private final PasteAction actPaste = new PasteAction();

	public CommonEditor() {
		super();

		this.setTabSize(3);
		this.getPainter().setLineHighlightEnabled(false);
		this.setFocusable(true);
		this.requestFocusInWindow(true);
		this.getPainter().setFont(new Font("Courier new", Font.PLAIN, 13));
		this.setBorder(new BevelBorder(BevelBorder.LOWERED));
	}

	public Action getSearchAction() {
		return actSearch;
	}

	public Action getSearchAgainAction() {
		return actSearchAgain;
	}

	public Action getReplaceAction() {
		return actReplace;
	}

	public Action getSelectAllAction() {
		return actSelectAll;
	}

	public Action getCutAction() {
		return actCut;
	}

	public Action getCopyAction() {
		return actCopy;
	}

	public Action getPasteAction() {
		return actPaste;
	}

	public int getTabSize() {
		return iTabSize;
	}

	public void setTabSize(int iTabSize) {
		this.iTabSize = iTabSize;
		this.getDocument().putProperty("tabSize", new Integer(2));
	}

	public void search() {
		final SearchPanel searchpnl = new SearchPanel(TextSearch.getInstance());
		searchpnl.setReplaceVisible(false);
		searchpnl.setReplaceAllEnabled(false);
		searchpnl.setApproveEnabled(false);
		final int iBtn = JOptionPane.showConfirmDialog(this, searchpnl, "Suchen", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (iBtn == JOptionPane.OK_OPTION) {
			final TextSearch search = TextSearch.prepareSearch(this, searchpnl.getSearchText(), searchpnl.isCaseSensitive(),
					searchpnl.isWholeWord(), searchpnl.getDirection(), searchpnl.isCurrentPos());
			search.search();
		}
	}

	public void searchAgain() {
		final TextSearch search = TextSearch.getInstance();
		switch (search.getLastAction()) {
			case TextSearch.ACTION_SEARCH:
				search.search();
				break;
			case TextSearch.ACTION_REPLACE:
				search.replace();
				break;
		}
	}

	public void replace() {
		final SearchPanel searchpnl = new SearchPanel(TextSearch.getInstance());
		searchpnl.setReplaceVisible(true);
		searchpnl.setDirectionEnabled(false);
		final int iBtn = JOptionPane.showConfirmDialog(this, searchpnl, "Ersetzen", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (iBtn == JOptionPane.OK_OPTION) {
			final TextSearch search = TextSearch.prepareReplace(this, searchpnl.getSearchText(), searchpnl.getReplaceText(),
					searchpnl.isCaseSensitive(), searchpnl.isWholeWord(), searchpnl.getDirection(), searchpnl.isReplaceAll(),
					searchpnl.isApprove(), searchpnl.isCurrentPos());
			search.replace();
		}
	}

	public void undo() {
//		this.u
	}

}  // CommonEditor
