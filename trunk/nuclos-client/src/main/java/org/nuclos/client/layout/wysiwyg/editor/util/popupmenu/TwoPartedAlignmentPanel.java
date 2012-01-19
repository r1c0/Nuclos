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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.TWO_PARTED_ALIGNMENT_DIALOG;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;

/**
 * This Dialog is for editing {@link TableLayoutConstraints}.<br>
 * 
 * It works like the {@link JOptionPane} calls, started modal and on close returning the new Alignment.
 * 
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class TwoPartedAlignmentPanel extends JPanel implements ActionListener{
	private static final long serialVersionUID = -8490117388832667453L;

	int width = 300;
	int height = 300;

	JButton[] orientation;

	TwoPartedAlignmentPanel alignmentDialog;

	private String tooltip_00 = TWO_PARTED_ALIGNMENT_DIALOG.TOOLTIP_00;
	private String tooltip_01 = TWO_PARTED_ALIGNMENT_DIALOG.TOOLTIP_01;
	private String tooltip_02 = TWO_PARTED_ALIGNMENT_DIALOG.TOOLTIP_02;
	private String tooltip_03 = TWO_PARTED_ALIGNMENT_DIALOG.TOOLTIP_03;
	private String tooltip_04 = TWO_PARTED_ALIGNMENT_DIALOG.TOOLTIP_04;
	private String tooltip_05 = TWO_PARTED_ALIGNMENT_DIALOG.TOOLTIP_05;
	private String tooltip_06 = TWO_PARTED_ALIGNMENT_DIALOG.TOOLTIP_06;
	private String tooltip_07 = TWO_PARTED_ALIGNMENT_DIALOG.TOOLTIP_07;

	/** this is where the icons are stored */
	private String path = "org/nuclos/client/layout/wysiwyg/editor/util/popupmenu/images/";
	/** the icons used in this dialog - order matters! */
	private String[] labels = {"go-previous.png", "mitte.png", "go-next.png", "links-rechts.png", "go-up.png", "mitte.png", "go-down.png", "oben-unten.png"};

	/** the tooltips used - as before - order matters! */
	private String[] tooltips = {tooltip_00, tooltip_01, tooltip_02, tooltip_03, tooltip_04, tooltip_05, tooltip_06, tooltip_07};

	/** the new alignment (to be returned if successfull ended - means ok clicked */
	private static final int[] alignment = new int[2];

	public static final int HORIZONTAL_ALIGN = 0;
	public static final int VERTICAL_ALIGN = 1;

	/** the layout for this dialog */
	private double[][] layoutDefinition = {{TableLayout.FILL,40,12,40,12,40,40,40,12,40, TableLayout.FILL}, 
			{TableLayout.FILL, 40, 12, 40, 12, 40,12, TableLayout.PREFERRED,  TableLayout.FILL}};

	/**
	 * Constructor
	 * @param c
	 * @see #getAlignmentConstraints(WYSIWYGComponent, int, int)
	 */
	public TwoPartedAlignmentPanel(WYSIWYGComponent c) {
		TableLayout tableLayout = new TableLayout(layoutDefinition);
		setLayout(tableLayout);
		
		orientation = new JButton[8];
		for (int i = 0; i < orientation.length; i++) {
			Icon icon = new ImageIcon(this.getClass().getClassLoader().getResource(path + labels[i]));
			orientation[i] = new JButton(icon);
			orientation[i].setActionCommand(i + "");
			orientation[i].addActionListener(this);
			orientation[i].setToolTipText(tooltips[i]);
		}

		TableLayoutConstraints constraint = new TableLayoutConstraints();

		/** 1st column */
		constraint.col1 = 1;
		constraint.col2 = 1;
		constraint.row1 = 1;
		constraint.row2 = 1;
		add(orientation[0], constraint);
		constraint = new TableLayoutConstraints();

		constraint.col1 = 3;
		constraint.col2 = 3;
		constraint.row1 = 1;
		constraint.row2 = 1;
		add(orientation[1], constraint);
		constraint = new TableLayoutConstraints();

		constraint.col1 = 5;
		constraint.col2 = 5;
		constraint.row1 = 1;
		constraint.row2 = 1;
		add(orientation[2], constraint);
		constraint = new TableLayoutConstraints();

		constraint.col1 = 1;
		constraint.col2 = 5;
		constraint.row1 = 5;
		constraint.row2 = 5;
		add(orientation[3], constraint);
		constraint = new TableLayoutConstraints();

		constraint.col1 = 7;
		constraint.col2 = 7;
		constraint.row1 = 1;
		constraint.row2 = 1;
		add(orientation[4], constraint);
		constraint = new TableLayoutConstraints();

		constraint.col1 = 7;
		constraint.col2 = 7;
		constraint.row1 = 1;
		constraint.row2 = 1;
		add(orientation[4], constraint);
		constraint = new TableLayoutConstraints();

		constraint.col1 = 7;
		constraint.col2 = 7;
		constraint.row1 = 3;
		constraint.row2 = 3;
		add(orientation[5], constraint);
		constraint = new TableLayoutConstraints();

		constraint.col1 = 7;
		constraint.col2 = 7;
		constraint.row1 = 5;
		constraint.row2 = 5;
		add(orientation[6], constraint);
		constraint = new TableLayoutConstraints();

		constraint.col1 = 9;
		constraint.col2 = 9;
		constraint.row1 = 1;
		constraint.row2 = 5;
		add(orientation[7], constraint);

		//NUCLEUSINT-354
		this.setBounds(this.getBounds().x - (width/2), this.getBounds().y - (height/2), width, height);
	}

	/**
	 * Setting the initial Alignment for display purpose.
	 * @param hAlign
	 * @param vAlign
	 */
	public void setComponentAlignment(int hAlign, int vAlign) {
		alignment[HORIZONTAL_ALIGN] = hAlign;
		alignment[VERTICAL_ALIGN] = vAlign;
		changeIconToActivated(hAlign, vAlign);
	}

	/**
	 * This Method to be called to display the {@link TwoPartedAlignmentPanel}
	 * @param c the {@link WYSIWYGComponent} to change the Alignment for
	 * @param hAlign the horizontal Alignment (read from the {@link TableLayoutConstraints})
	 * @param vAlign the vertical Alignment (read from the {@link TableLayoutConstraints})
	 * @return the new Alignment for the Component (returns the old value if not saved)
	 */
	public int[] getAlignmentConstraints() {
		return alignment;
	}

	/**
	 * Handled the Click event to highlight the clicked Button
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		int value = Integer.parseInt(e.getActionCommand());
		/**
		 * 6 0 3 1 7 4 9 2 5 8
		 * 
		 */
		switch (value) {
			/** h_Align */
			case 0 :
				alignment[HORIZONTAL_ALIGN] = TableLayout.LEFT;
				break;
			case 1 :
				alignment[HORIZONTAL_ALIGN] = TableLayout.CENTER;
				break;
			case 2 :
				alignment[HORIZONTAL_ALIGN] = TableLayout.RIGHT;
				break;
			case 3 :
				alignment[HORIZONTAL_ALIGN] = TableLayout.FULL;
				break;
			/** v_Align */
			case 4 :
				alignment[VERTICAL_ALIGN] = TableLayout.LEFT;
				break;
			case 5 :
				alignment[VERTICAL_ALIGN] = TableLayout.CENTER;
				break;
			case 6 :
				alignment[VERTICAL_ALIGN] = TableLayout.RIGHT;
				break;
			case 7 :
				alignment[VERTICAL_ALIGN] = TableLayout.FULL;
				break;
		}
		changeIconToActivated(alignment[HORIZONTAL_ALIGN], alignment[VERTICAL_ALIGN]);
		
	}

	/**
	 * This Method handles the Icon switching
	 * @param hAlign
	 * @param vAlign
	 */
	private void changeIconToActivated(int hAlign, int vAlign) {
		Icon icon;

		if (hAlign != -1) {
			for (int i = 0; i <= 3; i++) {
				icon = new ImageIcon(this.getClass().getClassLoader().getResource(path + labels[i]));
				orientation[i].setIcon(icon);
			}

			for (int i = 4; i <= 7; i++) {
				icon = new ImageIcon(this.getClass().getClassLoader().getResource(path + labels[i]));
				orientation[i].setIcon(icon);
			}

			/** set from outside, initial setting */
			switch (hAlign) {
				case TableLayout.LEFT :
					icon = new ImageIcon(this.getClass().getClassLoader().getResource(path + "pressed/" + labels[0]));
					orientation[0].setIcon(icon);
					break;
				case TableLayout.CENTER :
					icon = new ImageIcon(this.getClass().getClassLoader().getResource(path + "pressed/" + labels[1]));
					orientation[1].setIcon(icon);
					break;
				case TableLayout.RIGHT :
					icon = new ImageIcon(this.getClass().getClassLoader().getResource(path + "pressed/" + labels[2]));
					orientation[2].setIcon(icon);
					break;
				case TableLayout.FULL :
					icon = new ImageIcon(this.getClass().getClassLoader().getResource(path + "pressed/" + labels[3]));
					orientation[3].setIcon(icon);
					break;
			}

			switch (vAlign) {
				case TableLayout.LEFT :
					icon = new ImageIcon(this.getClass().getClassLoader().getResource(path + "pressed/" + labels[4]));
					orientation[4].setIcon(icon);
					break;
				case TableLayout.CENTER :
					icon = new ImageIcon(this.getClass().getClassLoader().getResource(path + "pressed/" + labels[5]));
					orientation[5].setIcon(icon);
					break;
				case TableLayout.RIGHT :
					icon = new ImageIcon(this.getClass().getClassLoader().getResource(path + "pressed/" + labels[6]));
					orientation[6].setIcon(icon);
					break;
				case TableLayout.FULL :
					icon = new ImageIcon(this.getClass().getClassLoader().getResource(path + "pressed/" + labels[7]));
					orientation[7].setIcon(icon);
					break;
			}
		}
	}
}
