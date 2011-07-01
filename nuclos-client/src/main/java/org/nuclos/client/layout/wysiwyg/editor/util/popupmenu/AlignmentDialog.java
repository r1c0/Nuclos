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
package org.nuclos.client.layout.wysiwyg.editor.util.popupmenu;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.ALIGNMENT_DIALOG;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;

/**
 * This class was the first try to set the Alignment of a Component with one Click.<br>
 * Its structured like {@link TwoPartedAlignmentPanel}.
 * 
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class AlignmentDialog extends JPanel implements ActionListener {
	private static final long serialVersionUID = -8490117388832667453L;

	int width = 300;
	int height = 300;

	JButton[] orientation;

	AlignmentDialog alignmentDialog;

	private String tooltip_00 = ALIGNMENT_DIALOG.TOOLTIP_00;
	private String tooltip_01 = ALIGNMENT_DIALOG.TOOLTIP_01;
	private String tooltip_02 = ALIGNMENT_DIALOG.TOOLTIP_02;
	private String tooltip_03 = ALIGNMENT_DIALOG.TOOLTIP_03;
	private String tooltip_04 = ALIGNMENT_DIALOG.TOOLTIP_04;
	private String tooltip_05 = ALIGNMENT_DIALOG.TOOLTIP_05;
	private String tooltip_06 = ALIGNMENT_DIALOG.TOOLTIP_06;
	private String tooltip_07 = ALIGNMENT_DIALOG.TOOLTIP_07;
	private String tooltip_08 = ALIGNMENT_DIALOG.TOOLTIP_08;
	private String tooltip_09 = ALIGNMENT_DIALOG.TOOLTIP_09;
	private String tooltip_10 = ALIGNMENT_DIALOG.TOOLTIP_10;
	private String tooltip_11 = ALIGNMENT_DIALOG.TOOLTIP_11;
	private String tooltip_12 = ALIGNMENT_DIALOG.TOOLTIP_12;
	private String tooltip_13 = ALIGNMENT_DIALOG.TOOLTIP_13;
	private String tooltip_14 = ALIGNMENT_DIALOG.TOOLTIP_14;
	private String tooltip_15 = ALIGNMENT_DIALOG.TOOLTIP_15;

	private String[] tooltips = {tooltip_00, tooltip_01, tooltip_02, tooltip_03, tooltip_04, tooltip_05, tooltip_06, tooltip_07, tooltip_08, tooltip_09, tooltip_10, tooltip_11, tooltip_12, tooltip_13, tooltip_14, tooltip_15};

	private static final int[] alignment = new int[2];

	public static final int HORIZONTAL_ALIGN = 0;
	public static final int VERTICAL_ALIGN = 1;

	/** the layout for this class */
	private double[][] layoutDefinition = {{0.23, 0.10, TableLayout.FILL, 0.33}, {0.25, TableLayout.FILL, TableLayout.FILL, 0.10, 0.15}};

	public AlignmentDialog(WYSIWYGComponent c) {


		TableLayout tableLayout = new TableLayout(layoutDefinition);
		tableLayout.setHGap(1);
		tableLayout.setVGap(1);

		setLayout(tableLayout);

		orientation = new JButton[16];
		for (int i = 0; i < orientation.length; i++) {
			orientation[i] = new JButton("");
			orientation[i].setActionCommand(i + "");
			orientation[i].addActionListener(this);
			orientation[i].setPreferredSize(new Dimension(40, 40));
			orientation[i].setToolTipText(tooltips[i]);
			if (i > 9)
				orientation[i].setBackground(Color.LIGHT_GRAY);
			else
				orientation[i].setBackground(Color.WHITE);
		}

		TableLayoutConstraints constraint = new TableLayoutConstraints();

		orientation[7].setText(ALIGNMENT_DIALOG.LABEL_CENTERED);
		orientation[9].setText(ALIGNMENT_DIALOG.LABEL_FULL);

		/** 1st column */
		constraint.col1 = 0;
		constraint.row1 = 0;
		constraint.col2 = 0;
		constraint.row2 = 0;
		add(orientation[0], constraint);
		orientation[0].setToolTipText(tooltip_01);
		constraint = new TableLayoutConstraints();
		constraint.col1 = 0;
		constraint.col2 = 0;
		constraint.row1 = 1;
		constraint.row2 = 2;
		add(orientation[1], constraint);
		constraint = new TableLayoutConstraints();
		constraint.col1 = 0;
		constraint.row1 = 4;
		constraint.col2 = 0;
		constraint.row2 = 4;
		add(orientation[2], constraint);
		constraint = new TableLayoutConstraints();

		constraint.col1 = 1;
		constraint.row1 = 0;
		constraint.col2 = 1;
		constraint.row2 = 0;
		add(orientation[10], constraint);
		constraint = new TableLayoutConstraints();
		constraint.col1 = 1;
		constraint.col2 = 1;
		constraint.row1 = 1;
		constraint.row2 = 2;
		add(orientation[11], constraint);
		constraint = new TableLayoutConstraints();
		constraint.col1 = 1;
		constraint.row1 = 3;
		constraint.col2 = 1;
		constraint.row2 = 4;
		add(orientation[12], constraint);
		constraint = new TableLayoutConstraints();

		constraint.col1 = 2;
		constraint.row1 = 0;
		constraint.col2 = 2;
		constraint.row2 = 0;
		add(orientation[6], constraint);
		constraint = new TableLayoutConstraints();
		constraint.col1 = 2;
		constraint.row1 = 1;
		constraint.col2 = 2;
		constraint.row2 = 1;
		add(orientation[7], constraint);
		constraint = new TableLayoutConstraints();
		constraint.col1 = 2;
		constraint.row1 = 2;
		constraint.col2 = 2;
		constraint.row2 = 2;
		add(orientation[9], constraint);
		constraint = new TableLayoutConstraints();
		constraint.col1 = 2;
		constraint.row1 = 4;
		constraint.col2 = 2;
		constraint.row2 = 4;
		add(orientation[8], constraint);
		constraint = new TableLayoutConstraints();

		constraint.col1 = 3;
		constraint.row1 = 0;
		constraint.col2 = 3;
		constraint.row2 = 0;
		add(orientation[3], constraint);
		constraint = new TableLayoutConstraints();
		constraint.col1 = 3;
		constraint.col2 = 3;
		constraint.row1 = 1;
		constraint.row2 = 2;
		add(orientation[4], constraint);
		constraint = new TableLayoutConstraints();
		constraint.col1 = 3;
		constraint.row1 = 4;
		constraint.col2 = 3;
		constraint.row2 = 4;
		add(orientation[5], constraint);
		constraint = new TableLayoutConstraints();

		constraint.col1 = 0;
		constraint.row1 = 3;
		constraint.col2 = 0;
		constraint.row2 = 3;
		add(orientation[13], constraint);
		constraint = new TableLayoutConstraints();
		constraint.col1 = 2;
		constraint.row1 = 3;
		constraint.col2 = 2;
		constraint.row2 = 3;
		add(orientation[14], constraint);
		constraint = new TableLayoutConstraints();
		constraint.col1 = 3;
		constraint.row1 = 3;
		constraint.col2 = 3;
		constraint.row2 = 3;
		add(orientation[15], constraint);
		constraint = new TableLayoutConstraints();
	}

	/**
	 * This Method opens the {@link AlignmentDialog}, works like a {@link JOptionPane#showInputDialog(Object)}
	 * @param mousePoint
	 * @param c
	 * @return
	 */
	public static int[] getAlignmentConstraints(WYSIWYGComponent c) {
		new AlignmentDialog(c);
		return alignment;
	}

	/**
	 * the {@link ActionListener} for this Class, handling the Events
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		int value = Integer.parseInt(e.getActionCommand());
		/**
		 * 6 0 3 1 7 4 9 2 5 8
		 * 
		 */
		switch (value) {
			case 0 :
				alignment[0] = TableLayout.LEFT;
				alignment[1] = TableLayout.TOP;
				break;
			case 1 :
				alignment[0] = TableLayout.LEFT;
				alignment[1] = TableLayout.CENTER;
				break;
			case 2 :
				alignment[0] = TableLayout.LEFT;
				alignment[1] = TableLayout.BOTTOM;
				break;
			case 3 :
				alignment[0] = TableLayout.RIGHT;
				alignment[1] = TableLayout.TOP;
				break;
			case 4 :
				alignment[0] = TableLayout.RIGHT;
				alignment[1] = TableLayout.CENTER;
				break;
			case 5 :
				alignment[0] = TableLayout.RIGHT;
				alignment[1] = TableLayout.BOTTOM;
				break;
			case 6 :
				alignment[0] = TableLayout.CENTER;
				alignment[1] = TableLayout.TOP;
				break;
			case 7 :
				alignment[0] = TableLayout.CENTER;
				alignment[1] = TableLayout.CENTER;
				break;
			case 8 :
				alignment[0] = TableLayout.CENTER;
				alignment[1] = TableLayout.BOTTOM;
				break;
			case 9 :
				alignment[0] = TableLayout.FULL;
				alignment[1] = TableLayout.FULL;
				break;
			/** aligned full */
			case 10 :
				alignment[0] = TableLayout.FULL;
				alignment[1] = TableLayout.TOP;
				break;
			case 11 :
				alignment[0] = TableLayout.FULL;
				alignment[1] = TableLayout.CENTER;
				break;
			case 12 :
				alignment[0] = TableLayout.FULL;
				alignment[1] = TableLayout.BOTTOM;
				break;
			case 13 :
				alignment[0] = TableLayout.LEFT;
				alignment[1] = TableLayout.FULL;
				break;
			case 14 :
				alignment[0] = TableLayout.CENTER;
				alignment[1] = TableLayout.FULL;
				break;
			case 15 :
				alignment[0] = TableLayout.RIGHT;
				alignment[1] = TableLayout.FULL;
				break;
		}

	}

}
