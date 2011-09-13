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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectController.CollectableEventListener;
import org.nuclos.client.ui.collect.CollectController.MessageType;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelEvent;
import org.nuclos.client.ui.collect.component.model.SearchComponentModelEvent;
import org.nuclos.client.ui.labeled.LabeledComboBox;
import org.nuclos.client.ui.popupmenu.JPopupMenuListener;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldComparatorFactory;
import org.nuclos.common.collect.collectable.CollectableUtils;
import org.nuclos.common.collect.collectable.searchcondition.AbstractCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.AtomicCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparisonWithOtherField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparisonWithParameter;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIsNullCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableLikeCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.collectable.searchcondition.ToHumanReadablePresentationVisitor;
import org.nuclos.common.collect.collectable.searchcondition.visit.AtomicVisitor;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * A <code>CollectableComponent</code> that presents a value in a <code>JComboBox</code>.
 * The value is always displayed, even if it is not contained in the dropdown list. To make this possible,
 * the dropdown list is dynamically extended by the selected entry, iff the selected entry is not in the list.
 * The additional entry is removed when it is deselected.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class CollectableComboBox extends LabeledCollectableComponentWithVLP implements CollectableEventListener {

	private static final Logger log = Logger.getLogger(CollectableComboBox.class);

	/**
	 * an additional entry that is dynamically added when a value is to be set that is not contained
	 * in the dropdown list.
	 */
	private CollectableField clctfExtra;

	private boolean insertable;

	/**
	 * <code>ActionListener</code> that gets notified whenever the selection changes (even on deselection).
	 * The ActionListener even gets notified when the new selected item is the same as the previously selected item.
	 */
	private final ActionListener alCollectableComponentChanged = new ActionListener() {
		@Override
        public void actionPerformed(ActionEvent ev) {
			try {
				if (isSearchComponent()) {
					adjustComparisonOperator();
				}
				viewToModel();
			}
			catch (CollectableFieldFormatException ex) {
				// do nothing. The model can't be updated.
				assert !isConsistent();
			}
		}

		/**
		 * adjusts the comparison operator for searchable (and insertable) comboboxes, after user changes
		 * @throws CollectableFieldFormatException
		 * @precondition CollectableComboBox.isSearchComponent()
		 */
		private void adjustComparisonOperator() throws CollectableFieldFormatException {
			if (!isSearchComponent()) {
				throw new IllegalStateException("isSearchComponent");
			}

			if (isInsertable()) {
				// For insertable comboboxes, the document listener sets the comparison operator,
				// so we needn't do anything here.
			}
			else {
				// For non-insertable comboboxes, we must set the comparison operator here:
				if (getFieldFromView().isNull()) {
					setComparisonOperator(ComparisonOperator.NONE);
				}
				else {
					if (getComparisonOperator().getOperandCount() < 2) {
						setComparisonOperator(ComparisonOperator.EQUAL);
					}
				}
			}
		}
	};

	private DocumentListener doclistener;

	/**
	 * @param clctef
	 * @postcondition isDetailsComponent()
	 */
	public CollectableComboBox(CollectableEntityField clctef) {
		this(clctef, false);

		assert isDetailsComponent();
	}

	/**
	 * @param clctef
	 */
	public CollectableComboBox(CollectableEntityField clctef, boolean bSearchable) {
		super(clctef, new LabeledComboBox(), bSearchable);

		assert !isInsertable();
		/** @todo setInsertable should not be used for this - it means something different! */
		setInsertable(isSearchComponent());

		setRenderer();
		getJComboBox().addActionListener(alCollectableComponentChanged);

		setComboBoxModel(Collections.<CollectableField>emptyList());

		assert isInsertable() == isSearchComponent();
	}

	@Override
	protected void setupJPopupMenuListener(JPopupMenuListener popupmenulistener) {
		final Component comp = getJComboBox().getEditor() != null ?
				getJComboBox().getEditor().getEditorComponent() :
				getControlComponent();

		comp.addMouseListener(popupmenulistener);
	}

	private void removeJPopupMenuListeners() {
		removeAllJPopupMenuListeners(getJComboBox().getEditor().getEditorComponent());
		removeAllJPopupMenuListeners(getControlComponent());
	}

	private static void removeAllJPopupMenuListeners(Component comp) {
		for (MouseListener ml : comp.getMouseListeners()) {
			if (ml instanceof JPopupMenuListener) {
				comp.removeMouseListener(ml);
			}
		}
	}

	/**
	 * @todo clarify semantics (insertable vs. searchable?)
	 */
	public boolean isInsertable() {
		return insertable;
	}

	@Override
	public void setInsertable(boolean bInsertable) {
		if (bInsertable != isInsertable()) {
			insertable = bInsertable;

			removeDocumentListenerForEditor();
			removeJPopupMenuListeners();

			getJComboBox().setEditable(bInsertable);

			if (bInsertable) {
				addDocumentListenerForEditor();
			}
			setupJPopupMenuListener(newJPopupMenuListener());
		}
	}

	private DocumentListener getDocumentListener() {
		if (doclistener == null) {
			doclistener = newDocumentListenerForTextComponentWithComparisonOperator();
		}
		return doclistener;
	}

	/**
	 * @return the text component of the editor, if any.
	 */
	private Document getEditorDocument() {
		Document result = null;
		final ComboBoxEditor editor = getJComboBox().getEditor();
		if (editor != null) {
			final Component comp = editor.getEditorComponent();
			if (comp instanceof JTextComponent) {
				result = ((JTextComponent) comp).getDocument();
			}
		}
		return result;
	}

	public void addDocumentListenerForEditor() {
		final Document document = getEditorDocument();
		if (document != null) {
			document.addDocumentListener(getDocumentListener());
		}
	}

	public void removeDocumentListenerForEditor() {
		if (doclistener != null) {
			final Document document = getEditorDocument();
			if (document != null) {
				document.removeDocumentListener(getDocumentListener());
			}
		}
	}

	public boolean hasDocumentListenerForEditor() {
		return doclistener != null;
	}

	private DefaultComboBoxModel getDefaultComboBoxModel() {
		return (DefaultComboBoxModel) getJComboBox().getModel();
	}

	/**
	 * refreshes the list of values by asking the value list provider.
	 * If no value list provider was set, the model will be empty.
	 * @return
	 * @throws CommonBusinessException
	 */
	@Override
    public void refreshValueList(boolean async) {
		log.debug("CollectableComboBox.refreshValueList called for field " + getFieldName());

		releasePreviousRefreshs();

		RefreshValueListWorker rvlr = new RefreshValueListWorker();
		runningRefreshs.add(rvlr);

		UIUtils.setWaitCursor(getJComboBox());

		if (async) {
			rvlr.execute();
		} else {
			rvlr.run();
		}
	}

	private Set<RefreshValueListWorker> runningRefreshs = Collections.synchronizedSet(new HashSet<CollectableComboBox.RefreshValueListWorker>());

	private void releasePreviousRefreshs() {
		synchronized(runningRefreshs) {
			for (RefreshValueListWorker refresh : runningRefreshs) {
				if (!refresh.cancel(true)) {
					refresh.cancelDone();
				}
			}
		}
	}

	private class RefreshValueListWorker extends SwingWorker<List<CollectableField>, Object> {

		boolean cancelDone = false;

		@Override
		protected List<CollectableField> doInBackground() throws Exception {
			return getValueListProvider() == null ? Collections.<CollectableField>emptyList() : getValueListProvider().getCollectableFields();
		}

		void cancelDone() {
			cancelDone = true;
		}

		@Override
		protected void done() {
			try {
				if (!isCancelled() && !cancelDone) {
					setComboBoxModel(get(), false);
					getJComboBox().setCursor(null);
				}
			} catch(Exception e) {
				Errors.getInstance().showExceptionDialog(CollectableComboBox.this.getJComponent(), e);
			} finally {
				runningRefreshs.remove(this);
			}
		}
	}

	/**
	 * @return the list of (possible) values (dropdown list)
	 */
	public List<CollectableField> getValueList() {
		final List<CollectableField> result = new ArrayList<CollectableField>();
		final DefaultComboBoxModel model = getDefaultComboBoxModel();
		for (int i = 0; i < model.getSize(); i++) {
			result.add((CollectableField) model.getElementAt(i));
		}
		return result;
	}

	/**
	 * uses a copy of the given Collection as model for this combobox.
	 * @param collEnumeratedFields Collection<CollectableField>
	 * @todo make private
	 */
	public void setComboBoxModel(Collection<? extends CollectableField> collEnumeratedFields) {
		setComboBoxModel(collEnumeratedFields, true);
	}

	/**
	 * uses a copy of the given Collection as model for this combobox. The selected value is not changed,
	 * thus no CollectableFieldEvents are fired.
	 * @param collEnumeratedFields Collection<CollectableField>
	 * @param bSort Sort the fields before adding them to the model?
	 * @todo make private
	 */
	public synchronized void setComboBoxModel(Collection<? extends CollectableField> collEnumeratedFields, boolean bSort) {
		final List<CollectableField> lst = new ArrayList<CollectableField>(collEnumeratedFields);

		if (bSort) {
			Collections.sort(lst, CollectableFieldComparatorFactory.getInstance().newCollectableFieldComparator(getEntityField()));
			//Collections.sort(lst, CollectableComparator.getFieldComparator(getEntityField()));
		}

		runLocked(new Runnable() {
			@Override
            public void run() {
				// re-fill the model:
				final DefaultComboBoxModel model = getDefaultComboBoxModel();

				model.removeAllElements();

				// forget the previous additional entry, if any. If an additional entry is needed after setting the model,
				// it will be set later:
				if (hasAdditionalEntry()) {
					removeAdditionalEntry();
				}

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

	public JComboBox getJComboBox() {
		return getLabeledComboBox().getJComboBox();
	}

	private LabeledComboBox getLabeledComboBox() {
		return ((LabeledComboBox) getJComponent());
	}

	@Override
    @SuppressWarnings("deprecation")
	public CollectableField getFieldFromView() throws CollectableFieldFormatException {
		final Object oCurrentItem = getCurrentItem();

		final CollectableField result;
		if (isInsertable() && (oCurrentItem instanceof String)) {
			final String sText = (String) oCurrentItem;
			result = CollectableTextComponentHelper.write(sText, getEntityField());
		}
		else {
			result = (oCurrentItem == null) ? getEntityField().getNullField() : (CollectableField) oCurrentItem;
		}
		return result;
	}

	/**
	 * @return the current item. That is the edited text, if this is insertable, or the selected item otherwise.
	 */
	private Object getCurrentItem() {
		final Object result;
		if (isInsertable()) {
			// Insertable: If the edited text differs from the selected item, the edited text is the master.
			result = getJComboBox().getEditor().getItem();
		}
		else {
			// Default: use the selected item.
			result = getJComboBox().getSelectedItem();
		}
		return result;
	}

	@Override
	protected void viewToModel() throws CollectableFieldFormatException {
		runLocked(new ExceptionalRunnable() {
			@Override
            public void run() throws CollectableFieldFormatException {
				if (isSearchComponent()) {
					getSearchModel().setSearchCondition(getSearchConditionFromView());
					// no need to care about additional entries for searchable combobox
				}
				else {
					try {
						final CollectableField clctf = getFieldFromView();
						viewToModel(clctf);

						/** @todo Does this belong here? This has nothing to do with updating the model. */
						final boolean bAdditionalEntrySelected = clctf.equals(clctfExtra);
// NUCLOS-82			getJComboBox().setForeground(bAdditionalEntrySelected ? Color.RED : null);
						if (!bAdditionalEntrySelected && hasAdditionalEntry()) {
							removeAdditionalEntry();
						}
					}
					catch (CollectableFieldFormatException ex) {
						/** @todo Is it right to clear the model here? See also AbstractCollectableComponent.viewToModel. */
						// the value in the view is not valid. At least, the model is cleared:
						getModel().clear();
						if (hasAdditionalEntry()) {
							removeAdditionalEntry();
						}
						throw ex;
					}
				}
			}
		});
	}

	@Override
	protected void updateView(CollectableField clctfValue) {
		if (hasAdditionalEntry()) {
			removeAdditionalEntry();
		}

		final Color colorOldForeground = getJComboBox().getForeground();
		Color colorNewForeground = null;

		int iIndex = getDefaultComboBoxModel().getIndexOf(clctfValue);
		// If the value could not be found, it might be the mnemonic, so look for the value id
		if(iIndex < 0 && clctfValue.isIdField() && clctfValue.getValueId() != null) {
			for(int i=0; i < getDefaultComboBoxModel().getSize(); i++) {
				CollectableField cf = (CollectableField) getDefaultComboBoxModel().getElementAt(i);
				Long valueId = null;
				if(cf.getValueId() instanceof Integer) {
					valueId =  new Long((Integer)cf.getValueId());
				}
				else {
					valueId = (Long) cf.getValueId();
				}
				if (clctfValue.getValueId() instanceof Integer && clctfValue.getValueId() != null) {
					if(LangUtils.equals(((Integer)clctfValue.getValueId()).longValue(), valueId)) {
						iIndex = i;
						break;
					}
				} else {
					if(clctfValue.getValueId().equals(valueId)) {
						iIndex = i;
						break;
					}
				}
			}
		}
		if (iIndex >= 0) {
			getJComboBox().setSelectedIndex(iIndex);
			// Note that setSelectedItem doesn't work here, as clctf might have no label.
		}
		else {
			assert iIndex == -1;

			// issue warning:
			final StringBuilder sbWarning = new StringBuilder("Wert in Dropdownliste nicht gefunden: " + clctfValue);
			Object oValueId = null;
			if (clctfValue.isIdField()) {
				oValueId = clctfValue.getValueId();
				// sbWarning.append(" (Id: ").append(oValueId).append(")");
				sbWarning.append(" (Id: ");
				sbWarning.append(oValueId);
				sbWarning.append(", ");
				sbWarning.append(clctfValue.toDescription());
				sbWarning.append(")");
			}
			log.warn(sbWarning.toString());

			if (isInsertable() && (oValueId == null) && (clctfValue.getValue() != null)) {
				final String sText = clctfValue.toString();
				getJComboBox().setSelectedItem(sText);
				// getJComboBox().getEditor().setItem(sText);
			}
			else {
				addAdditionalEntry(clctfValue);
// NUCLOS-82	colorNewForeground = Color.RED;
				getJComboBox().setSelectedIndex(getDefaultComboBoxModel().getIndexOf(clctfValue));
			}
		}

// NUCLOS-82
//		if (!LangUtils.equals(colorNewForeground, colorOldForeground)) {
//			getJComboBox().setForeground(colorNewForeground);
//			adjustAppearance();
//		}

		/** @todo maybe we can introduce this postcondition now? That would be very nice. :) */
		// Note: That postcondition worked until we allowed searchable comboboxes.
//		assert isConsistent();

		// Old comment:
		// Note that <code>isConsistent()</code> is not a postcondition here, because this
		// cannot be guaranteed in general. For example, one may set the <code>collectableField</code> for
		// a <code>CollectableComboBox</code> that doesn't contain this value in its list of possible values.
		// In those cases, the view is not consistent with the model.
	}

	private boolean hasAdditionalEntry() {
		return clctfExtra != null;
	}

	/**
	 * @param clctf
	 * @precondition !hasAdditionalEntry()
	 * @postcondition hasAdditionalEntry()
	 */
	private void addAdditionalEntry(CollectableField clctf) {
		assert !hasAdditionalEntry();

		getDefaultComboBoxModel().addElement(clctf);
		clctfExtra = clctf;

		assert hasAdditionalEntry();
	}

	/**
	 * @precondition hasAdditionalEntry()
	 * @postcondition !hasAdditionalEntry()
	 */
	private void removeAdditionalEntry() {
		assert hasAdditionalEntry();

		//NUCLEUSINT-470
		try {
			getDefaultComboBoxModel().removeElement(clctfExtra);
		} catch (IllegalStateException e) {
			log.info(e);
		}

		clctfExtra = null;

		assert !hasAdditionalEntry();
	}

	@Override
	public boolean hasComparisonOperator() {
		return true;
	}

	@Override
	public void setComparisonOperator(ComparisonOperator compop) {
		super.setComparisonOperator(compop);

		if (compop.getOperandCount() < 2) {
			runLocked(new Runnable() {
				@Override
                public void run() {
					getJComboBox().setSelectedItem(null);
					getJComboBox().getEditor().setItem(null);
				}
			});
		}
	}

	@Override
	protected CollectableSearchCondition getSearchConditionFromView() throws CollectableFieldFormatException {
		if (!isSearchComponent()) {
			throw new IllegalStateException("!isSearchComponent()");
		}
		return getSearchConditionFromViewImpl(LangUtils.toString(getCurrentItem()));
	}

	/**
	 * Implementation of <code>CollectableComponentModelListener</code>.
	 * This event is (and must be) ignored for a searchable combobox.
	 * @todo refactor!!! Maybe no event should be fired for searchable components in general!
	 * @param ev
	 */
	@Override
	public void collectableFieldChangedInModel(CollectableComponentModelEvent ev) {
		if (isSearchComponent()) {
			// simply ignore this event
		}
		else {
			super.collectableFieldChangedInModel(ev);
		}
	}

	/**
	 * Implementation of <code>CollectableComponentModelListener</code>.
	 * @param ev
	 */
	@Override
	public void searchConditionChangedInModel(final SearchComponentModelEvent ev) {
		// update the view:
		runLocked(new Runnable() {
			@Override
            public void run() {
				final CollectableSearchCondition cond = ev.getSearchComponentModel().getSearchCondition();
				if (cond == null) {
					resetWithComparison();
					updateView(getEntityField().getNullField());
					setComparisonOperator(ComparisonOperator.NONE);
				}
				else {
					final AtomicCollectableSearchCondition atomiccond = (AtomicCollectableSearchCondition) cond;

					updateView(atomiccond.accept(new AtomicVisitor<CollectableField, RuntimeException>() {
						@Override
						public CollectableField visitIsNullCondition(CollectableIsNullCondition isnullcond) {
							resetWithComparison();
							return getEntityField().getNullField();
						}

						@Override
						public CollectableField visitComparison(CollectableComparison comparison) {
							resetWithComparison();
							return comparison.getComparand();
						}

						@Override
						public CollectableField visitComparisonWithParameter(CollectableComparisonWithParameter comparisonwp) {
							setWithComparison(comparisonwp.getParameter());
							return getEntityField().getNullField();
						}

						@Override
						public CollectableField visitComparisonWithOtherField(CollectableComparisonWithOtherField comparisonwf) {
							setWithComparison(comparisonwf.getOtherField());
							return getEntityField().getNullField();
						}

						@Override
						public CollectableField visitLikeCondition(CollectableLikeCondition likecond) {
							resetWithComparison();
							return CollectableUtils.newCollectableFieldForValue(getEntityField(), likecond.getLikeComparand());
						}
					}));
					setComparisonOperator(atomiccond.getComparisonOperator());
				}
			}
		});
	}

	/**
	 * @return the number of columns for this combobox. This default implementation returns always null.
	 * For compatibility reasons, setColumns() has no effect on a <code>CollectableComboBox</code>.
	 */
	protected Integer getColumns() {
		return null;
	}

	@Override
	public TableCellRenderer getTableCellRenderer() {
		if (!isSearchComponent() && getValueListProvider() != null) {
			return new CollectableComponentDetailTableCellRenderer() {
				/**
				 *
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public Component getTableCellRendererComponent(JTable tbl, Object oValue, boolean bSelected, boolean bHasFocus, int iRow, int iColumn) {
					CollectableField cf = (CollectableField) oValue;
					boolean valid = true;
					// NUCLEUSINT-885
					Object valueId = (cf != null && cf.isIdField()) ? cf.getValueId() : null;
					if (valueId != null) {
						valid = false;
						DefaultComboBoxModel cbModel = getDefaultComboBoxModel();
						for (int i = 0, n = cbModel.getSize(); i < n; i++) {
							CollectableField cf2 = (CollectableField) cbModel.getElementAt(i);
							if (cf2 != null && cf2.isIdField() && IdUtils.equals(valueId, cf2.getValueId())) {
								cf = cf2;
								valid = true;
								break;
							}
						}
					} else if (!cf.isIdField()) {
						valid = insertable;
						Object value = cf.getValue();
						DefaultComboBoxModel cbModel = getDefaultComboBoxModel();
						for (int i = 0, n = cbModel.getSize(); i < n; i++) {
							CollectableField cf2 = (CollectableField) cbModel.getElementAt(i);
							if (cf2 != null && ObjectUtils.equals(value, cf2.getValue())) {
								cf = cf2;
								valid = true;
								break;
							}
						}
					}
					Component renderer = super.getTableCellRendererComponent(tbl, cf, bSelected, bHasFocus, iRow, iColumn);
					// NUCLEUSINT-525
					if (!valid) {
// NUCLOS-82			setForeground(Color.RED);
						if (StringUtils.looksEmpty((getText()))) {
							setText("<ung\u00fcltiger Eintrag>");
						}
					}
					return renderer;
				}
			};
		} else if (isSearchComponent()){
			//NOAINT-215
			return new CollectableComponentDetailTableCellRenderer() {
				/**
				 *
				 */
				private static final long serialVersionUID = 1L;

				@Override
				protected void setValue(Object value) {
					if (value instanceof AbstractCollectableSearchCondition)
						setToolTipText(((CollectableSearchCondition)value).accept(new ToHumanReadablePresentationVisitor()));
					if (value instanceof CollectableComparison) {
						value = ((CollectableComparison) value).getComparand();
					}
					if (value instanceof AtomicCollectableSearchCondition)
						value = ((AtomicCollectableSearchCondition)value).getComparandAsString();

					super.setValue(value);
				}
			};
		}
		else {
			return new CollectableComponentDetailTableCellRenderer() {
				/**
				 *
				 */
				private static final long serialVersionUID = 1L;

				@Override
				protected void setValue(Object value) {
					super.setValue(value);
				}
			};
		}
	}

	/**
	 * sets the renderer for the combo box. The value according to <code>CollectableField.getValue()</code>
	 * is rendered.
	 * The background color will not be displayed. This is a known Bug (http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6363287),
	 * and will hopefully be fixed by Sun, as there is no known workaround.
	 */
	protected void setRenderer() {
		getJComboBox().setRenderer(new CollectableFieldRenderer());
	}

	/**
	 * Overrides due to error in Swing: renderer/editor AND background color cannot be set at the same time!
	 * Temporary workaround is putting the information normally transported via color into tooltip.
	 */
	@Override
	protected String getToolTipTextForMultiEdit() {
		if (!isMultiEditable()) {
			throw new IllegalStateException("multiEditable");
		}
		String result;
		final String sLabel = getEntityField().getLabel();
		try {
			if (getDetailsModel().isValueToBeChanged()) {
				final CollectableField clctf = getFieldFromView();
				final String sValue = clctf.isNull() ? "<leer>" : (String) clctf.getValue();
				result = sLabel + " = " + sValue;
			}
			else {
				result = sLabel + " (<keine \u00c4nderung>)";
			}
		}
		catch (CollectableFieldFormatException ex) {
			result = "<Ung\u00fcltiger Wert>";
		}
		if (!getDetailsModel().hasCommonValue()) {
			// Due to an error in the Swing libraries which prevents the changing of background colors
			result += " - ACHTUNG! Dieses Feld hat unterschiedliche Werte in den bearbeiteten Objekten.";
		}

		return result;
	}

	public class CollectableFieldRenderer extends DefaultListCellRenderer {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public Component getListCellRendererComponent(JList list, Object oValue, int iIndex, boolean bSelected, boolean bCellHasFocus) {
			super.getListCellRendererComponent(list, oValue, iIndex, bSelected, bCellHasFocus);

			// reset preferred size:
			setPreferredSize(null);

			final CollectableField clctf = (CollectableField) oValue;
			Color colorForeground = bSelected ? list.getSelectionForeground() : list.getForeground();
			if (clctf == null) {
				log.warn("CollectableFieldRenderer.getListCellRendererComponent: oValue == null");
			}
			else {
				String sText = null;
				if (clctf.equals(clctfExtra)) {
					// NUCLOS-82	colorForeground = Color.RED;
					if (StringUtils.looksEmpty(LangUtils.toString(clctf.getValue()))) {
						sText = "<ung\u00fcltiger Eintrag>";
					}
				}
				// The space character for the null entry is important because otherwise the cell has minimal height.
				// Note that this is only for display. It doesn't have any impact on the internal value.
				if (sText == null) {
					sText = StringUtils.looksEmpty(LangUtils.toString(clctf.getValue())) ? " " : clctf.toString();
				}
				setText(sText);

				// add 10 pixel to the component width, otherwise the largest item
				// could not be shown completely in the combobox
				int height = getPreferredSize().height;
				int width = getPreferredSize().width;
				setPreferredSize(new Dimension(width+10, height));

				// Set a tooltip if the text cannot be displayed completely:
				final String sToolTipText = isToolTipNecessary() ? (StringUtils.looksEmpty(sText) ? null : sText) : null;
				setToolTipText(sToolTipText);
			}
			setForeground(colorForeground);

			// Does not work - see header!
			if (iIndex < 0) {
				Color background = getJComboBox().getBackground();
				setBackground(bSelected ? list.getSelectionBackground() : background);
			}

			// respect setColumns() - note that this has no effect by default as getColumns() always returns null:
			final Integer iColumns = getColumns();
			if (iColumns != null) {
				final Dimension dimPreferredSize = getPreferredSize();
				final int iPreferredWidth = Math.min(dimPreferredSize.width, getFontMetrics(getFont()).charWidth('m') * iColumns);
				setPreferredSize(new Dimension(iPreferredWidth, dimPreferredSize.height));
			}
			return this;
		}

		@Override
		public boolean isOpaque() {
			return true;
		}

		private boolean isToolTipNecessary() {
			// this is not exactly right, but it's probably the best guess we can make here:
			return getPreferredSize().width > getJComboBox().getSize().width;
		}
	}

	/**
	 * adds a "Refresh" entry to the popup menu, for a non-searchable component.
	 */
	@Override
	public JPopupMenu newJPopupMenu() {
		final JPopupMenu result = super.newJPopupMenu();
		if (!this.isSearchComponent()) {
			result.add(newRefreshEntry());
		}
		return result;
	}

	/**
	 * @precondition getEntityField().isReferencing()
	 * @return a new "refresh" entry for the context menu in edit mode
	 */
	protected final JMenuItem newRefreshEntry() {
		if (!getEntityField().isReferencing()) {
			throw new IllegalStateException();
		}
		final JMenuItem result = new JMenuItem(TEXT_REFRESH);
		result.addActionListener(new ActionListener() {
			@Override
            public void actionPerformed(ActionEvent ev) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						refreshValueList(false);
					}
				});
			}
		});
		result.setEnabled(getJComponent().isEnabled());
		return result;
	}

	@Override
	public void handleCollectableEvent(final Collectable collectable, final MessageType messageType) {
		switch (messageType) {
			case EDIT_DONE:
			case DELETE_DONE:
			case STATECHANGE_DONE:
				// data has changed -> simply refresh.
				refreshValueList();
				break;
			case NEW_DONE:
				// data has changed -> refresh and try to select new collectable.
				refreshValueList(false);
				for (int i = 0; i < getJComboBox().getItemCount(); i++) {
					Object o = getJComboBox().getItemAt(i);
					if (o instanceof CollectableField) {
						CollectableField field = (CollectableField) o;
						if (LangUtils.equals(IdUtils.toLongId(field.getValueId()), IdUtils.toLongId(collectable.getId()))) {
							getModel().setField(field);
							runLocked(new Runnable() {
								@Override
								public void run() {
									modelToView();
								}
							});
							break;
						}
					}
				}
				break;
		}
	}

}	// class CollectableComboBox
