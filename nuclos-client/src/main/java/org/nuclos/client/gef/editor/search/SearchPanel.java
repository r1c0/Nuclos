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

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class SearchPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	GridBagLayout gridBagLayout1 = new GridBagLayout();
	JLabel lblSearch = new JLabel();
	JLabel lblReplace = new JLabel();
	JTextField edSearch = new JTextField();
	JTextField edReplace = new JTextField();
	JPanel pnlParent = new JPanel();
	GridBagLayout gridBagLayout2 = new GridBagLayout();
	JPanel pnlOptions = new JPanel();
	JPanel pnlDirection = new JPanel();
	JRadioButton rbForward = new JRadioButton();
	JRadioButton rbBackward = new JRadioButton();
	JCheckBox cbCase = new JCheckBox();
	JCheckBox cbWholeWord = new JCheckBox();
	TitledBorder titleOption;
	TitledBorder titleDirection;
	ButtonGroup bgDirection = new ButtonGroup();
	JCheckBox cbCurrentPos = new JCheckBox();
	JCheckBox cbReplaceAll = new JCheckBox();
	JCheckBox cbApprove = new JCheckBox();
	GridBagLayout gridBagLayout3 = new GridBagLayout();
	GridBagLayout gridBagLayout4 = new GridBagLayout();

	public SearchPanel(TextSearch search) {
		try {
			jbInit();

			edSearch.setText(search.getSearchString());
			edReplace.setText(search.getReplaceString());
			cbCase.setSelected(search.isCaseSensitive());
			cbWholeWord.setSelected(search.isWholeWord());
			cbReplaceAll.setSelected(search.isReplaceAll());
			cbApprove.setSelected(search.isApprove());
			cbCurrentPos.setSelected(search.isCurrentPos());

			switch (search.getDirection()) {
				case TextSearch.SEARCH_FORWARD:
					rbForward.setSelected(true);
					break;
				case TextSearch.SEARCH_BACKWARD:
					rbBackward.setSelected(true);
					break;
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		titleOption = new TitledBorder("");
		titleDirection = new TitledBorder("");
		lblSearch.setLabelFor(null);
		lblSearch.setText("Suchtext:");
		this.setLayout(gridBagLayout1);
		lblReplace.setLabelFor(null);
		lblReplace.setText("Ersetzen:");
		edSearch.setMinimumSize(new Dimension(6, 21));
		edSearch.setPreferredSize(new Dimension(150, 21));
		edSearch.setText("");
		edReplace.setPreferredSize(new Dimension(150, 21));
		edReplace.setToolTipText("");
		edReplace.setText("");
		pnlParent.setLayout(gridBagLayout2);
		pnlOptions.setLayout(gridBagLayout3);
		rbForward.setMnemonic('V');
		rbForward.setText("Vorw\u00e4rts");
		rbBackward.setMnemonic('R');
		rbBackward.setText("R\u00fcckw\u00e4rts");
		pnlDirection.setLayout(gridBagLayout4);
		cbCase.setAlignmentX((float) 0.0);
		cbCase.setAlignmentY((float) 1.0);
		cbCase.setHorizontalAlignment(SwingConstants.LEADING);
		cbCase.setHorizontalTextPosition(SwingConstants.TRAILING);
		cbCase.setMnemonic('G');
		cbCase.setText("Gross-/Kleinschreibung");
		cbCase.setVerticalAlignment(SwingConstants.CENTER);
		cbCase.setVerticalTextPosition(SwingConstants.CENTER);
		cbWholeWord.setAlignmentY((float) 1.0);
		cbWholeWord.setMnemonic('N');
		cbWholeWord.setText("Nur ganze W\u00f6rter");
		pnlOptions.setBorder(titleOption);
		pnlDirection.setAlignmentX((float) 0.5);
		pnlDirection.setAlignmentY((float) 0.5);
		pnlDirection.setBorder(titleDirection);
		titleOption.setTitle("Optionen");
		titleDirection.setTitle("Richtung");
		cbCurrentPos.setMnemonic('A');
		cbCurrentPos.setText("Ab aktueller Cursorposition");
		cbReplaceAll.setAlignmentY((float) 1.0);
		cbReplaceAll.setToolTipText("");
		cbReplaceAll.setMnemonic('L');
		cbReplaceAll.setText("Alles ersetzen");
		cbApprove.setAlignmentY((float) 1.0);
		cbApprove.setMnemonic('B');
		cbApprove.setText("Best\u00e4tigen");
		this.add(lblSearch, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(4, 4, 0, 0), 0, 0));
		this.add(lblReplace, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(4, 4, 0, 0), 0, 0));
		this.add(edSearch, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(4, 4, 0, 4), 0, 0));
		this.add(edReplace, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(4, 4, 0, 4), 0, 0));
		this.add(pnlParent, new GridBagConstraints(0, 2, 2, 1, 1.0, 1.0
				, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		pnlParent.add(pnlOptions, new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0
				, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(8, 0, 4, 4), 0, 0));
		pnlParent.add(pnlDirection, new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0
				, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(8, 4, 4, 4), 0, 0));
		pnlDirection.add(rbForward, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		pnlDirection.add(rbBackward, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
				, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		pnlDirection.add(cbCurrentPos, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
				, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 23, 0), 0, 0));
		pnlOptions.add(cbCase, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		pnlOptions.add(cbWholeWord, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
				, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		bgDirection.add(rbForward);
		bgDirection.add(rbBackward);
		pnlOptions.add(cbReplaceAll, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
				, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		pnlOptions.add(cbApprove, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
				, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
	}

	public void setReplaceVisible(boolean value) {
		lblReplace.setVisible(value);
		edReplace.setVisible(value);
		doLayout();
	}

	public String getSearchText() {
		return edSearch.getText();
	}

	public void setSearchText(String sText) {
		edSearch.setText(sText);
	}

	public String getReplaceText() {
		return edReplace.getText();
	}

	public void setReplaceText(String sText) {
		edReplace.setText(sText);
	}

	public boolean isCaseSensitive() {
		return cbCase.isSelected();
	}

	public void setCaseSensitive(boolean bValue) {
		cbCase.setSelected(bValue);
	}

	public boolean isWholeWord() {
		return cbWholeWord.isSelected();
	}

	public void setWholeWord(boolean bValue) {
		cbWholeWord.setSelected(bValue);
	}

	public int getDirection() {
		return (rbBackward.isSelected() ? TextSearch.SEARCH_BACKWARD : (rbForward.isSelected() ? TextSearch.SEARCH_FORWARD : -1));
	}

	public void setDirection(int iDirection) {
		switch (iDirection) {
			case TextSearch.SEARCH_FORWARD:
				rbForward.setSelected(true);
				break;
			case TextSearch.SEARCH_BACKWARD:
				rbBackward.setSelected(true);
				break;
		}
	}

	public void setReplaceAllEnabled(boolean bValue) {
		cbReplaceAll.setSelected(false);
		cbReplaceAll.setEnabled(bValue);
	}

	public void setApproveEnabled(boolean bValue) {
		cbApprove.setEnabled(bValue);
		cbApprove.setEnabled(bValue);
	}

	public boolean isReplaceAll() {
		return cbReplaceAll.isSelected();
	}

	public void setReplaceAll(boolean bValue) {
		cbReplaceAll.setSelected(bValue);
	}

	public boolean isApprove() {
		return cbApprove.isSelected();
	}

	public void setApprove(boolean bValue) {
		cbApprove.setSelected(bValue);
	}

	public boolean isCurrentPos() {
		return cbCurrentPos.isSelected();
	}

	public void setCurrentPos(boolean bValue) {
		cbCurrentPos.setSelected(bValue);
	}

	public void setDirectionEnabled(boolean bValue) {
		rbBackward.setEnabled(bValue);
		rbForward.setEnabled(bValue);
		rbForward.setSelected(true);
	}
}
