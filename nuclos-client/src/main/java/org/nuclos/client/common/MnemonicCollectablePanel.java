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
package org.nuclos.client.common;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;
import org.nuclos.client.genericobject.GenericObjectMetaDataCache;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.component.DelegatingCollectablePanel;
import org.nuclos.client.ui.labeled.LabeledComboBox;
import org.nuclos.client.ui.labeled.LabeledComponent;
import org.nuclos.client.ui.popupmenu.JPopupMenuListener;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.attribute.valueobject.AttributeValueVO;

/**
 * A <code>CollectableComponent</code> that presents a value in a composite component
 * consists of <code>LabeledComboBox/JComboBox</code> and <code>JTextArea</code>.
 * The ComboBox will be filled with attributes mnemonics, the TextArea is editable
 * and shows a value for in ComboBox selected attribute.
 * Attribute values schould be configured under "Attribute...".
 * Please configure for this component: 
 * 		- Werte: "Werteliste (explizit)"
 * 		- Anzeigen in: "ComboBox (editierbar)"
 * 		- Feldbreite: 2000 --- this depends on DB structure
 *		please enter values under "Werteliste". "K\u00fcrzel" are values for ComboBox and 
 *		"Wert" are values for TextArea.  
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Rostislav.Maksymovskyi@novabit.de">Rostislav Maksymovskyi</a>
 * @version	01.00.00
 */
public class MnemonicCollectablePanel extends DelegatingCollectablePanel {

	private static final Logger log = Logger.getLogger(MnemonicCollectablePanel.class);

	private static class MnemonicValuePanel extends LabeledComponent {
		
		protected LabeledComboBox mnemonicBox;
		protected JTextArea valueArea;
		JScrollPane scrlTxtArea;
		private String txtarealayout = BorderLayout.SOUTH;
		//private int txtAreaRowsCount;
		private int iColumns = 20;
		private int iRows = 5;
		private boolean cEnabled = true;
		private boolean visibleControl = true;
		private boolean visibleLabel = true;
		
		public MnemonicValuePanel(){
			super();
			this.setLayout(new BorderLayout());
			
			this.mnemonicBox = new LabeledComboBox();
			mnemonicBox.setEditable(false);		
			mnemonicBox.getControlComponent().setVisible(visibleControl);	
			mnemonicBox.getJLabel().setVisible(visibleLabel);	
			this.add(mnemonicBox, BorderLayout.CENTER);
			
			addValueArea(this.txtarealayout);
		}

		private void addValueArea(String txtarealayout) {
			int txtAreaRowsCount = iRows;
			//int txtAreaColumnsCount = mnemonicBox.getWidth();
			//if(iColumns > 0){
			int txtAreaColumnsCount = iColumns;
			//}
			valueArea = new JTextArea(txtAreaRowsCount,txtAreaColumnsCount);
			
			valueArea.setLineWrap(true);
			valueArea.setWrapStyleWord(true);
			
			UIUtils.setMaximumSizeToPreferredSize(valueArea);
			UIUtils.setMinimumSizeToPreferredSize(mnemonicBox);
			
			valueArea.setMargin(new Insets(2, 2, 2, 2));
			
			this.valueArea.setEnabled(cEnabled);
			this.valueArea.setEditable(cEnabled);
			this.mnemonicBox.setEnabled(cEnabled);
			this.mnemonicBox.getControlComponent().setVisible(visibleControl);	
			this.mnemonicBox.getJLabel().setVisible(visibleLabel);	
			
			final Font font = new JLabel().getFont();
			valueArea.setFont(font);
			scrlTxtArea = new JScrollPane(valueArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scrlTxtArea.setMinimumSize(valueArea.getPreferredSize());
			this.valueArea.setVisible(visibleControl);	
			this.scrlTxtArea.setVisible(visibleControl);	
			this.add(scrlTxtArea, txtarealayout);
		}

		public void setValueAreaLayout(String txtarealayout) {
			this.remove(this.scrlTxtArea);
			this.addValueArea(txtarealayout);
		}
		
		public JComboBox getMnemonicBox(){
			return mnemonicBox.getJComboBox();
		}

		public JTextArea getValueArea(){
			return valueArea;
		}
		
		public void setSelected(Object item, String itemValue){
			mnemonicBox.getJComboBox().setSelectedItem(item);
			valueArea.setText(itemValue);
		}
		
		public void setUndefined(){
			setSelected(null, "");
		}

		@Override
		public void setLabelText(String sLabel) {
			this.mnemonicBox.setLabelText(sLabel);
		}

		@Override
		public void setColumns(int piColumns){
			this.iColumns = piColumns;
		}

		@Override
		public void setRows(int piRows){
			this.iRows = piRows;
		}
		
		@Override
		public void setEnabled(boolean pEnabled){
			this.cEnabled = pEnabled;
		}

		public void setVisibleControl(boolean visible){
			this.visibleControl = visible;
		}

		public void setVisibleLabel(boolean visible){
			this.visibleLabel = visible;
		}

		@Override
		public JComponent getControlComponent() {
			return getMnemonicBox();
		}
	} // class MnemonicValuePanel

	private final ActionListener alComboBoxChanged = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent ev) {
			Object selectedItem = MnemonicCollectablePanel.this.getJComboBox().getSelectedItem();
			if(MnemonicCollectablePanel.this.getJTextArea() != null && hasValidValue(selectedItem)){
				try {
					CollectableField fieldFromView = MnemonicCollectablePanel.this.getFieldFromView();
					CollectableValueIdField colvidv = (CollectableValueIdField)fieldFromView;
					String key = (String)colvidv.getValue();
					AttributeValueVO vo = vIds.get(key);
					String value = vo.getValue();
					MnemonicCollectablePanel.this.getJTextArea().setText(value);
				}
				catch (CollectableFieldFormatException ex) {
					// do nothing. The model can't be updated.
					assert !MnemonicCollectablePanel.this.isConsistent();
				}
			}
		}
		
		private boolean hasValidValue(Object selectedItem){
			return (selectedItem != null) && ((CollectableValueIdField)selectedItem).getValue() != null
					&& vIds.containsKey((((CollectableValueIdField)selectedItem).getValue()));
		}
	};
	
	private final DocumentListener alTxtAreaChanged = new DocumentListener() {		    
	    @Override
		public void changedUpdate(DocumentEvent e) {
			//MnemonicCollectablePanel.this.getModel().setField(newField);	
			try{
				MnemonicCollectablePanel.this.viewToModel();
			}catch (CollectableFieldFormatException ex) {
				// do nothing. The model can't be updated.
				assert !MnemonicCollectablePanel.this.isConsistent();
			}
	    }

	    @Override
		public void removeUpdate(DocumentEvent e) {
	    	changedUpdate(e);
	    }

	    @Override
		public void insertUpdate(DocumentEvent e) {
	    	changedUpdate(e);
	    }
	};
		
	// private CollectableComponentModel clctcompmodel;
	private Map<String, AttributeValueVO> vIds;
	
	/**
	 * @param clctef
	 * @postcondition this.isDetailsComponent()
	 */
	public MnemonicCollectablePanel(CollectableEntityField clctef) {
		this(clctef, false);
		//assert this.isDetailsComponent();
	}

	public MnemonicCollectablePanel(CollectableEntityField clctef, boolean bSearchable) {
		super(clctef, new MnemonicValuePanel(), bSearchable);
		
		this.setInsertable(this.isSearchComponent());
		this.getJComboBox().addActionListener(this.alComboBoxChanged);
		this.getJTextArea().getDocument().addDocumentListener(this.alTxtAreaChanged);
		this.setComboBoxModel(Collections.<CollectableField>emptyList());
	}

	/**
	 * ctor for dynamic creation (see attribute <code>controltypeclass</code> in LayoutML).
	 * @param clctef
	 * @param bSearchable
	 */
	public MnemonicCollectablePanel(CollectableEntityField clctef, Boolean bSearchable) {
		this(clctef, bSearchable.booleanValue());
	}
	
	@Override
	public void setProperty(String sName, Object oValue) {
		if("txtarealayout".equals(sName) && oValue != null && ((String)oValue).trim().length() > 0) {
			setTextAreaLayout((String)oValue);
		}
	}
	// ------------------------------------------------------------------
	
	@Override
	public JComponent getFocusableComponent() {
		return getMnemonicValuePanel().getValueArea();
	}

	public JComboBox getJComboBox() {
		return getMnemonicValuePanel().getMnemonicBox();
	}

	public JTextArea getJTextArea() {
		return getMnemonicValuePanel().getValueArea();
	}
	
	@Override
	public JComponent getControlComponent() {
		return getMnemonicValuePanel().getValueArea();
	}
	
	public MnemonicValuePanel getMnemonicValuePanel(){
		return (MnemonicValuePanel) getJComponent();
	}

	public void setTextAreaLayout(String valueAreaLayout) {
		getMnemonicValuePanel().setValueAreaLayout(valueAreaLayout);
		getJTextArea().getDocument().addDocumentListener(this.alTxtAreaChanged);
	}

	@Override
	public void setLabelText(String sLabel) {
		this.getMnemonicValuePanel().setLabelText(sLabel);
	}

	@Override
	public void setColumns(int iColumns){
		this.getMnemonicValuePanel().setColumns(iColumns);
	}

	@Override
	public void setRows(int iRows){
		this.getMnemonicValuePanel().setRows(iRows);
	}

	@Override
	public void setEnabled(boolean bEnabled){
		this.getMnemonicValuePanel().setEnabled(bEnabled);
	}
	
	@Override
	public void setMnemonic(char c) {
		this.getMnemonicValuePanel().setMnemonic(c);
	}

	@Override
	public void setOpaque(boolean bOpaque) {
		this.getMnemonicValuePanel().setOpaque(bOpaque);
	}

	@Override
	public void setVisibleControl(boolean visible){
		this.getMnemonicValuePanel().setVisibleControl(visible);
	}

	@Override
	public void setVisibleLabel(boolean visible){
		this.getMnemonicValuePanel().setVisibleLabel(visible);
	}
	
	@Override
	public void setToolTipText(String sToolTipText) {
		this.getMnemonicValuePanel().setToolTipText(sToolTipText);
	}
	
	// ----------------------------------------------------------

	@Override
	public void refreshValueList() throws CommonBusinessException {
		vIds = new HashMap<String, AttributeValueVO>();
		log.debug(this.getClass().getName() + ".refreshValueList called for field " + this.getFieldName());
		Collection<AttributeValueVO> values = GenericObjectMetaDataCache.getInstance().getAttribute(this.getEntityField().getCollectableEntity().getName(), this.getFieldName()).getValues();
		List<CollectableField> collectableFields = new ArrayList<CollectableField>();
		CollectableField field = null;
		for(AttributeValueVO vo : values){
			field = new CollectableValueIdField(null, vo.getMnemonic());
			collectableFields.add(field);
			vIds.put(vo.getMnemonic(),vo);
			
		}
		setComboBoxModel(collectableFields);
		int width = this.getJComboBox().getWidth();
		this.getJTextArea().setSize(new Dimension(5, width));
	}

	// ----------------------------------------------------------

	private CollectableField getNewField() {
		final String valueTxt = MnemonicCollectablePanel.this.getJTextArea().getText();
		final CollectableValueIdField newField = new CollectableValueIdField(null, valueTxt);
		return newField;
	}

	@Override
	public CollectableField getField() throws CollectableFieldFormatException {
		this.makeConsistent();

		//return this.getModel().getField();
		return getNewField();
	}
	
	@Override
	protected void updateView(CollectableField clctfValue) {
		final String bValue = (String) clctfValue.getValue();
		if (bValue == null) {
			getMnemonicValuePanel().setUndefined();
		}
		else {
			getMnemonicValuePanel().setSelected(clctfValue.getValue(), clctfValue.getValue().toString());
		}
	}

	@Override
	public void setInsertable(boolean bInsertable) {
		// do nothing
	}

	@Override
	public CollectableField getFieldFromView() throws CollectableFieldFormatException {
		final Object oCurrentItem = this.getMnemonicValuePanel().getMnemonicBox().getSelectedItem();
		final CollectableField result = (oCurrentItem == null) ? getEntityField().getNullField() : (CollectableField) oCurrentItem;
		return result;
	}

	/**
	 * uses a copy of the given Collection as model for this component.
	 * @param collEnumeratedFields Collection<CollectableField>
	 * @todo make private
	 */
	public void setComboBoxModel(Collection<? extends CollectableField> collEnumeratedFields) {
		this.setComboBoxModel(collEnumeratedFields, true);
	}

	/**
	 * uses a copy of the given Collection as model for this component. The selected value is not changed,
	 * thus no CollectableFieldEvents are fired.
	 * @param collEnumeratedFields Collection<CollectableField>
	 * @param bSort Sort the fields before adding them to the model?
	 * @todo make private
	 */
	public void setComboBoxModel(Collection<? extends CollectableField> collEnumeratedFields, boolean bSort) {
		final List<CollectableField> lst = new ArrayList<CollectableField>(collEnumeratedFields);

		if (bSort) {
			//Collections.sort(lst, CollectableComparator.getFieldComparator(this.getEntityField()));
		}

		this.runLocked(new Runnable() {
			@Override
			public void run() {
				// re-fill the model:
				final DefaultComboBoxModel model = getDefaultComboBoxModel();
				model.removeAllElements();
				// always add a null value as the first entry:
				model.addElement(getEntityField().getNullField());
				for (CollectableField clctf : lst) {
					model.addElement(clctf);
				}
				// set the view according to the model:
				modelToView();
			}
		});

	}	

	private DefaultComboBoxModel getDefaultComboBoxModel() {
		return (DefaultComboBoxModel) this.getJComboBox().getModel();
	}

	/**
	 * @deprecated Use constructor to initialize the model. 
	 * 		The model itself shouldn't be changed after construction of the view.
	 */
	/*
	@Override
	public void setModel(CollectableComponentModel clctcompmodel) {
		if (clctcompmodel.isSearchModel() != this.isSearchComponent()) {
			throw new CommonFatalException("The Model and View do not match with the property \"searchable\".");
				//"Model und View stimmen in der Eigenschaft \"searchable\" nicht \u00fcberein.");
		}
		if (this.getModel() != null) {
			this.getModel().removeCollectableComponentModelListener(this);
		}
		this.clctcompmodel = clctcompmodel;
		this.clctcompmodel.addCollectableComponentModelListener(this);
	}
	 */

	@Override
	protected void setupJPopupMenuListener(JPopupMenuListener popupmenulistener) {
		// ...
	}
	
	@Override
	protected void viewToModel() throws CollectableFieldFormatException {
		super.viewToModel();
	}

	@Override
	public boolean needValueListProvider(){
		return true;
	}
	
}
