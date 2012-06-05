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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.commons.lang.NullArgumentException;
import org.nuclos.client.ui.OptionGroup;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldFormat;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonFatalException;

/**
 * <code>Collectable OptionGroup</code> (group of radio buttons).
 *
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @todo check if this class is still up to date (together with Boris)
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version	01.00.00
 */
public class CollectableOptionGroup extends AbstractCollectableComponent {
	protected String sDefaultOption;
	private final CollectableFieldFormat clctfformat;

	public CollectableOptionGroup(CollectableEntityField clctef, boolean bSearchable) {
		super(clctef, new OptionGroup(), bSearchable);

		this.clctfformat = CollectableFieldFormat.getInstance(this.getEntityField().getJavaClass());

		/** @todo check if this fires when calling setField */
		getOptionGroup().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				try {
					CollectableOptionGroup.this.viewToModel();
				}
				catch (CollectableFieldFormatException ex) {
					/** @todo check if this is really a fatal error */
					throw new CommonFatalException("collectable.optiongroup.exception", ex);
						//"Fehler beim Lesen aus Optionsgruppe.", ex);
				}
			}
		});
	}

	public OptionGroup getOptionGroup() {
		return (OptionGroup) this.getControlComponent();
	}

	@Override
	public final void setInsertable(boolean bEditable) {
		/** @todo check if this is right */
//		getJComponent().setEnabled(bEditable);
	}

	@Override
	public CollectableField getFieldFromView() throws CollectableFieldFormatException {
		final Object oValue = this.clctfformat.parse(null, StringUtils.nullIfEmpty(getOptionGroup().getValue()));
		return new CollectableValueField(oValue);
	}

	@Override
	protected void updateView(CollectableField clctfValue) {
		String sText = this.clctfformat.format(this.getEntityField().getFormatOutput(), clctfValue.getValue());
		if (StringUtils.isNullOrEmpty(sText)) {
			sText = sDefaultOption;
		}
		getOptionGroup().setValue(sText);

		this.adjustAppearance();
	}

	@Override
	public JPopupMenu newJPopupMenu() {
		// Note that the default entries (as specified in AbstractCollectableComponent) are ignored.
		final JPopupMenu result = new JPopupMenu();

		final JMenuItem miClear = CollectableOptionGroup.this.newClearEntry();
		if (CollectableOptionGroup.this.isSearchComponent()) {
			miClear.setText(SpringLocaleDelegate.getInstance().getMessage("RootNode.2", "<Alle>"));
		}
		result.add(miClear);
		return result;
	}

//	public void setField(CollectableField clctfValue) {
//		/** @todo only change (and fire) if value has changed. */
//		String sText = this.clctfformat.format(null, clctfValue.getValue());
//		if (sText == null || sText.length() == 0)
//			sText = sDefaultOption;
//		getOptionGroup().setValue(sText);
//	}

//	public CollectableField getField() throws CollectableFieldFormatException {
//		final Object oValue = this.clctfformat.parse(null, StringUtils.nullIfEmpty(getOptionGroup().getValue()));
//		return new CollectableValueField(oValue);
//	}

	/**
	 * @param lstOptions
	 * @precondition lstOptions != null
	 */
	public void setOptions(List<String[]> lstOptions) {
		if(lstOptions == null) {
			throw new NullArgumentException("lstOptions");
		}
		getOptionGroup().setOptions(lstOptions);
	}

	public void setDefaultOption(String sDefaultOption) {
		this.sDefaultOption = sDefaultOption;
		getOptionGroup().setDefaultOption(sDefaultOption);
	}

	@Override
	protected void setEnabledState(boolean bValue) {
		getOptionGroup().setEnabled(bValue);
	}

	public void setOrientation(int iOrientation) {
		getOptionGroup().setOrientation(iOrientation);
	}

}  // class CollectableOptionGroup
