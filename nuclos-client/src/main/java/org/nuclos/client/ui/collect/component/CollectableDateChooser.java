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
package org.nuclos.client.ui.collect.component;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

import org.nuclos.client.ui.DateChooser;
import org.nuclos.client.ui.labeled.LabeledDateChooser;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldFormat;
import org.nuclos.common.collect.collectable.CollectableUtils;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.RelativeDate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonFatalException;

/**
 * <code>CollectableComponent</code> to display/enter a date.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public class CollectableDateChooser extends CollectableTextComponent {

	/**
	 * @param clctef
	 * @postcondition this.isDetailsComponent()
	 */
	public CollectableDateChooser(CollectableEntityField clctef) {
		this(clctef, false);
		this.overrideActionMap();

		assert this.isDetailsComponent();
	}

// Override the tab key
	private void overrideActionMap() {
		JTextComponent component = getJTextComponent();

		// The actions
		Action nextFocusAction = new AbstractAction("insert-tab") {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
            public void actionPerformed(ActionEvent evt) {
				((Component) evt.getSource()).transferFocus();
			}
		};
		Action prevFocusAction = new AbstractAction("Move Focus Backwards") {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
            public void actionPerformed(ActionEvent evt) {
				((Component) evt.getSource()).transferFocusBackward();
			}
		};
		// Add actions
		component.getActionMap().put(nextFocusAction.getValue(Action.NAME), nextFocusAction);
		component.getKeymap().addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_MASK), prevFocusAction);
	}

	/**
	 * @param clctef
	 * @param bSearchable
	 */
	public CollectableDateChooser(CollectableEntityField clctef, boolean bSearchable) {
		this(clctef, bSearchable, false);
	}

	/**
	 * @param clctef
	 * @param bSearchable
	 * @param bTodayIsRelative shouldn't be used for editable components
	 * @see DateChooser#isTodayRelative()
	 */
	public CollectableDateChooser(CollectableEntityField clctef, boolean bSearchable, boolean bTodayIsRelative) {
		super(clctef, new LabeledDateChooser(bTodayIsRelative, clctef.isNullable(), clctef.getFormatInput(), clctef.getFormatOutput(), bSearchable), bSearchable);
		if (clctef.getJavaClass() != Date.class && clctef.getJavaClass() != InternalTimestamp.class) {
			throw new CommonFatalException("collectable.date.chooser.exception");//"Datum erwartet.");
		}

		this.getDateChooser().getJTextField().addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				try {
					updateView(getFieldFromView());
				} catch (CollectableFieldFormatException e1) {
					//e1.printStackTrace();
				}
			}
		});
		// just for debugging - remove!
		assert this.getJTextComponent() == this.getDateChooser().getJTextField();
	}

	public DateChooser getDateChooser() {
		return ((LabeledDateChooser) getJComponent()).getDateChooser();
	}

	@Override
	public JComponent getFocusableComponent() {
		return getDateChooser().getJTextField();
	}

	@Override
	public void setColumns(int iColumns) {
		this.getDateChooser().setColumns(iColumns);
	}

	@Override
	public void setComparisonOperator(ComparisonOperator compop) {
		super.setComparisonOperator(compop);

		if (compop.getOperandCount() < 2) {
			getDateChooser().setDate(null);
		}
	}

	@Override
	public CollectableField getFieldFromView() throws CollectableFieldFormatException {
		String sPattern = getDateChooser().getPattern();
		final Object oValue = CollectableFieldFormat.getInstance(this.getEntityField().getJavaClass()).parse(sPattern,
			StringUtils.nullIfEmpty(this.getDateChooser().getText()));
		return CollectableUtils.newCollectableFieldForValue(this.getEntityField(), oValue);
	}

	@Override
	protected void updateView(CollectableField clctfValue) {
		final Date date;
		if (clctfValue.getValue() != null && clctfValue.getValue().equals(RelativeDate.today().toString())) {
			date = RelativeDate.today();
		}
		else {
			date = (Date) clctfValue.getValue();
		}
		this.getDateChooser().setDate(date);

		this.adjustAppearance();
	}

	@Override
	protected void adjustBackground() {
//		this.getDateChooser().getJTextField().setBackground(this.getBackgroundColor());
	}

	@Override
	public void setEnabled(boolean flag) {
		this.getDateChooser().getJTextField().setEditable(flag);
		this.getDateChooser().getBrowseButton().setEnabled(flag);
	}


}  // class CollectableDateChooser
