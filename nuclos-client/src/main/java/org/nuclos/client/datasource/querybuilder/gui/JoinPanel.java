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
package org.nuclos.client.datasource.querybuilder.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.MessageFormat;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.client.datasource.querybuilder.shapes.RelationConnector;

/**
 * @todo enter class description.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class JoinPanel extends JPanel {

	private static final String sJoin = SpringLocaleDelegate.getInstance().getMessage(
			"JoinPanel.1", "<html>Nur verkn\u00fcpfte Datens\u00e4tze ber\u00fccksichtigen,<br>deren Feldinhalte auf beiden Seiten identisch sind</html>");
	private static final String sOuterJoin = SpringLocaleDelegate.getInstance().getMessage(
			"JoinPanel.2", "<html>ALLE Datens\u00e4tze von {0}<br>und nur Datens\u00e4tze von {1} selektieren,<br>wenn die verkn\u00fcpften Felder identisch sind ({2} Outer Join)</html>");

	public final JLabel lblLeftTable = new JLabel(SpringLocaleDelegate.getInstance().getMessage(
			"JoinPanel.3", "Linke Tabelle"));
	public final JLabel lblLeftColumn = new JLabel(SpringLocaleDelegate.getInstance().getMessage(
			"JoinPanel.4", "Linke Spalte"));
	public final JLabel lblRightTable = new JLabel(SpringLocaleDelegate.getInstance().getMessage(
			"JoinPanel.5", "Rechte Tabelle"));
	public final JLabel lblRightColumn = new JLabel(SpringLocaleDelegate.getInstance().getMessage(
			"JoinPanel.6", "Rechte Spalte"));
	public final JTextField edLeftTable = new JTextField();
	public final JTextField edLeftColumn = new JTextField();
	public final JTextField edRightTable = new JTextField();
	public final JTextField edRightColumn = new JTextField();
	public final ButtonGroup buttonGroup = new ButtonGroup();
	public final JRadioButton rbJoinEqual = new JRadioButton();
	public final JRadioButton rbJoinLeft = new JRadioButton();
	public final JRadioButton rbJoinRight = new JRadioButton();

	protected String sLeftTable, sLeftColumn, sRightTable, sRightColumn;

	public JoinPanel(String sLeftTable, String sLeftColumn, String sRightTable, String sRightColumn) {
		super();

		this.sLeftColumn = sLeftColumn;
		this.sLeftTable = sLeftTable;
		this.sRightColumn = sRightColumn;
		this.sRightTable = sRightTable;
		init();
	}

	private void init() {
		buttonGroup.add(rbJoinEqual);
		buttonGroup.add(rbJoinLeft);
		buttonGroup.add(rbJoinRight);

		rbJoinEqual.setText(sJoin);
//      rbJoinEqual.setVerticalTextPosition(SwingConstants.TOP);
		rbJoinEqual.setVerticalAlignment(SwingConstants.TOP);
		rbJoinLeft.setText(MessageFormat.format(sOuterJoin, new Object[] {sLeftTable, sRightTable, "Left"}));
//      rbJoinLeft.setVerticalTextPosition(SwingConstants.TOP);
		rbJoinLeft.setVerticalAlignment(SwingConstants.TOP);
		rbJoinRight.setText(MessageFormat.format(sOuterJoin, new Object[] {sRightTable, sLeftTable, "Right"}));
//      rbJoinRight.setVerticalTextPosition(SwingConstants.TOP);
		rbJoinRight.setVerticalAlignment(SwingConstants.TOP);

		edLeftTable.setText(sLeftTable);
		edLeftTable.setEditable(false);

		edLeftColumn.setText(sLeftColumn);
		edLeftColumn.setEditable(false);

		edRightTable.setText(sRightTable);
		edRightTable.setEditable(false);

		edRightColumn.setText(sRightColumn);
		edRightColumn.setEditable(false);

		setLayout(new GridBagLayout());
		add(lblLeftTable, new GridBagConstraints(0, 0, 1, 1, 0d, 0d, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));
		add(edLeftTable, new GridBagConstraints(1, 0, 1, 1, 1d, 0d, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(4, 4, 4, 4), 0, 0));
		add(lblLeftColumn, new GridBagConstraints(0, 1, 1, 1, 0d, 0d, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));
		add(edLeftColumn, new GridBagConstraints(1, 1, 1, 1, 1d, 0d, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(4, 4, 4, 4), 0, 0));

		add(new JSeparator(), new GridBagConstraints(0, 2, 2, 1, 1d, 0d, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(4, 4, 4, 4), 0, 0));

		add(lblRightTable, new GridBagConstraints(0, 3, 1, 1, 0d, 0d, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));
		add(edRightTable, new GridBagConstraints(1, 3, 1, 1, 1d, 0d, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(4, 4, 4, 4), 0, 0));
		add(lblRightColumn, new GridBagConstraints(0, 4, 1, 1, 0d, 0d, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));
		add(edRightColumn, new GridBagConstraints(1, 4, 1, 1, 1d, 0d, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(4, 4, 4, 4), 0, 0));

		add(new JSeparator(), new GridBagConstraints(0, 5, 2, 1, 1d, 0d, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(4, 4, 4, 4), 0, 0));

		add(rbJoinEqual, new GridBagConstraints(0, 6, 2, 1, 1d, 0d, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(4, 4, 4, 4), 0, 0));
		add(rbJoinLeft, new GridBagConstraints(0, 7, 2, 1, 1d, 0d, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(4, 4, 4, 4), 0, 0));
		add(rbJoinRight, new GridBagConstraints(0, 8, 2, 1, 1d, 0d, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(4, 4, 4, 4), 0, 0));
	}

	public String getLeftColumn() {
		return sLeftColumn;
	}

	public void setLeftColumn(String sLeftColumn) {
		this.sLeftColumn = sLeftColumn;
	}

	public String getLeftTable() {
		return sLeftTable;
	}

	public void setLeftTable(String sLeftTable) {
		this.sLeftTable = sLeftTable;
	}

	public String getRightColumn() {
		return sRightColumn;
	}

	public void setRightColumn(String sRightColumn) {
		this.sRightColumn = sRightColumn;
	}

	public String getRightTable() {
		return sRightTable;
	}

	public void setRightTable(String sRightTable) {
		this.sRightTable = sRightTable;
	}

	public static void main(String[] args) {
		JOptionPane.showMessageDialog(null, new JoinPanel("Test", "Test", "Test", "Test"));
	}

	public int getJoinType() {
		int iResult = 0;
		if (rbJoinEqual.isSelected()) {
			iResult = RelationConnector.TYPE_JOIN;
		}
		else if (rbJoinLeft.isSelected()) {
			iResult = RelationConnector.TYPE_LEFTJOIN;
		}
		else if (rbJoinRight.isSelected()) {
			iResult = RelationConnector.TYPE_RIGHTJOIN;
		}
		return iResult;
	}
}
