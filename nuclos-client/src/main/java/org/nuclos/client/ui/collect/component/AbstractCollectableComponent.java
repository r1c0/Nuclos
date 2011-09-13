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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.Preferences;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.BufferedLayerUI;
import org.jdesktop.jxlayer.plaf.effect.BufferedImageOpEffect;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.nuclos.client.common.ClientParameterProvider;
import org.nuclos.client.common.Utils;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.synthetica.NuclosSyntheticaConstants;
import org.nuclos.client.ui.ColorProvider;
import org.nuclos.client.ui.ResourceIdMapper;
import org.nuclos.client.ui.ToolTipTextProvider;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModel;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelEvent;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelListener;
import org.nuclos.client.ui.collect.component.model.DetailsComponentModel;
import org.nuclos.client.ui.collect.component.model.DetailsComponentModelEvent;
import org.nuclos.client.ui.collect.component.model.SearchComponentModel;
import org.nuclos.client.ui.collect.component.model.SearchComponentModelEvent;
import org.nuclos.client.ui.collect.model.SortableCollectableTableModel;
import org.nuclos.client.ui.labeled.LabeledComboBox;
import org.nuclos.client.ui.popupmenu.DefaultJPopupMenuListener;
import org.nuclos.client.ui.popupmenu.JPopupMenuFactory;
import org.nuclos.client.ui.popupmenu.JPopupMenuListener;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableUtils;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityProvider;
import org.nuclos.common.collect.collectable.searchcondition.AtomicCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparisonWithOtherField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparisonWithParameter;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparisonWithParameter.ComparisonParameter;
import org.nuclos.common.collect.collectable.searchcondition.CollectableLikeCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.collectable.searchcondition.ToHumanReadablePresentationVisitor;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonFatalException;

import com.jhlabs.image.BoxBlurFilter;

/**
 * Component that displays and lets the user edit a <code>Collectable</code>.
 * Contains all properties that are common to all <code>CollectableComponents</code>.
 * Some properties are ignored by some components.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public abstract class AbstractCollectableComponent
		implements CollectableComponent, CollectableComponentModelListener, JPopupMenuFactory, ToolTipTextProvider {

	private static final Logger LOG = Logger.getLogger(AbstractCollectableComponent.class);

	/**
	 * the color to be used as background for multi editable components that don't share a common value.
	 */
	public static Color colorCommonValues = Utils.translateColorFromParameter(ParameterProvider.KEY_HISTORICAL_STATE_CHANGED_COLOR);//new Color(246,229,255);

	private final JComponent comp;
	private final CollectableEntityField clctef;
	private CollectableComponentModel clctcompmodel;
	private final boolean bSearchable;
	private boolean bViewLocked;

	private String sNextFocusComponent;

	private ReferencingListener reflistener;

	private Map<String, Object> mpProperties;

	private boolean bEnabledByInitial = true;

	protected static final String TEXT_NOCHANGE = CommonLocaleDelegate.getMessage("AbstractCollectableComponent.13","Keine \u00c4nderung vornehmen");
	protected static final String TEXT_CLEAR = CommonLocaleDelegate.getMessage("AbstractCollectableComponent.11","Feld leeren");
	protected static final String TEXT_SHOWDETAILS = CommonLocaleDelegate.getMessage("AbstractCollectableComponent.7","Details anzeigen...");
	protected static final String TEXT_NEW = CommonLocaleDelegate.getMessage("AbstractCollectableComponent.context.new","Neu...");
	protected static final String TEXT_REFRESH = CommonLocaleDelegate.getMessage("AbstractCollectableComponent.context.refresh","Aktualisieren...");

	/**
	 * the comparison operator, if any, that can be set by the user.
	 */
	private ComparisonOperator compop;

	/**
	 * the other field, used for comparison with other field.
	 */
	private CollectableEntityField clctefOtherField;

	/**
	 * the parameter, used for comparison.
	 */
	private ComparisonParameter compParameter;

	/**
	 * used to display the possible entity fields for comparison with other field.
	 */
	private CollectableEntity clcte;

//	/**
//	 * @param clctef
//	 * @param comp
//	 * @postcondition isDetailsComponent()
//	 */
//	protected AbstractCollectableComponent(CollectableEntityField clctef, JComponent comp) {
//		this(clctef, comp, false);
//	}

	/**
	 * @param clctef
	 * @param comp
	 * @param bSearchable
	 * @precondition clctef != null
	 * @precondition comp != null
	 * @postcondition isSearchComponent() == bSearchable
	 */
	protected AbstractCollectableComponent(CollectableEntityField clctef, JComponent comp, boolean bSearchable) {
		if (clctef == null) {
			throw new NullArgumentException("clctef");
		}
		if (comp == null) {
			throw new NullArgumentException("comp");
		}
		this.clctef = clctef;
		this.comp = comp;
		// set the name of the JComponent so it can be identified by GUI testing tools:
		comp.setName(getFieldName());
		this.bSearchable = bSearchable;
		setModel(CollectableComponentModel.newCollectableComponentModel(clctef, bSearchable));

		if (isSearchComponent() && hasComparisonOperator()) {
			setComparisonOperator(ComparisonOperator.NONE);
		}

		setupJPopupMenuListener(newJPopupMenuListener());

		assert isSearchComponent() == bSearchable;
	}

	public static synchronized void setCommonValuesBackgroundColor(Color color) {
		AbstractCollectableComponent.colorCommonValues = color;
	}

	@Override
    public CollectableEntityField getEntityField() {
		return clctef;
	}

	@Override
    public CollectableComponentModel getModel() {
		return clctcompmodel;
	}

	@Override
    public SearchComponentModel getSearchModel() {
		if (!isSearchComponent()) {
			throw new IllegalStateException("searchComponent");
		}
		return (SearchComponentModel) getModel();
	}

	@Override
    public DetailsComponentModel getDetailsModel() {
		if (!isDetailsComponent()) {
			throw new IllegalStateException("detailsComponent");
		}
		return (DetailsComponentModel) getModel();
	}

	@Override
    public void setModel(CollectableComponentModel clctcompmodel) {
		if (clctcompmodel.isSearchModel() != isSearchComponent()) {
			throw new CommonFatalException(CommonLocaleDelegate.getMessage("AbstractCollectableComponent.14","Model und View stimmen in der Eigenschaft \"searchable\" nicht \u00fcberein."));
		}
		if (getModel() != null) {
			getModel().removeCollectableComponentModelListener(this);
		}
		this.clctcompmodel = clctcompmodel;
		clctcompmodel.addCollectableComponentModelListener(this);
	}

	@Override
    public boolean isSearchComponent() {
		return bSearchable;
	}

	@Override
    public boolean isDetailsComponent() {
		return !isSearchComponent();
	}

	@Override
    public boolean isMultiEditable() {
		return isDetailsComponent() && getDetailsModel().isMultiEditable();
	}

	@Override
    public void setVisible(boolean bVisible) {
		getJComponent().setVisible(bVisible);
	}

	@Override
    public boolean isEnabledByInitial() {
		return bEnabledByInitial;
	}

	@Override
    public String getFieldName() {
		return clctef.getName();
	}

	@Override
    public JComponent getJComponent() {
		return comp;
	}

	/**
	 * @return default implementation: the whole JComponent is the control component.
	 */
	@Override
    public JComponent getControlComponent() {
		return getJComponent();
	}

	@Override
    public JComponent getFocusableComponent() {
		return getControlComponent();
	}

	/**
	 * locks the view. The view will not be updated (by the model) while it is locked.
	 * @param bViewLocked
	 */
	protected void setViewLocked(boolean bViewLocked) {
		this.bViewLocked = bViewLocked;
	}

	/**
	 * @return Ist updating the view prohibited?
	 */
	boolean isViewLocked() {
		return bViewLocked;
	}

	/**
	 * runs the given runnable in locked mode. The view will not be updated (by the model) during this operation.
	 * @param runnable
	 */
	protected void runLocked(Runnable runnable) {
		if (!isViewLocked()) {
			try {
				setViewLocked(true);

				runnable.run();
			}
			finally {
				setViewLocked(false);
			}
		}
		// It seems to be quiet common to <em>not</em> execute the Runnable...
		/*
		else if (LOG.isDebugEnabled()) {
			LOG.debug("runLocked(" + runnable + ") not executed: view is already locked: " +
				this + " field: " + getFieldName() + " -> " + getModel().getField().getValue());
		}
		 */
	}

	protected final CollectableEntityField getComparisonOtherField() {
		return clctefOtherField;
	}

	protected final ComparisonParameter getComparisonParameter() {
		return compParameter;
	}

	protected final void resetWithComparison() {
		this.clctefOtherField = null;
		this.compParameter = null;
	}

	protected final void setWithComparison(CollectableEntityField clctefOtherField) {
		this.clctefOtherField = clctefOtherField;
		this.compParameter = null;
	}

	protected final void setWithComparison(ComparisonParameter compParameter) {
		this.clctefOtherField = null;
		this.compParameter = compParameter;
	}

	protected static interface ExceptionalRunnable {
		void run() throws CollectableFieldFormatException;
	}

	/**
	 * runs the given runnable in locked mode. The view will not be updated (by the model) during this operation.
	 * @param runnable
	 */
	protected void runLocked(ExceptionalRunnable runnable) throws CollectableFieldFormatException {
		if (!isViewLocked()) {
			try {
				setViewLocked(true);

				runnable.run();
			}
			finally {
				setViewLocked(false);
			}
		}
	}

	/**
	 * @return Is the model consistent with the view?
	 */
	@Override
    public boolean isConsistent() {
		try {
			if (isSearchComponent()) {
				final CollectableSearchCondition condModel = getSearchModel().getSearchCondition();
				final CollectableSearchCondition condView = getSearchConditionFromView();
				return LangUtils.equals(condModel, condView);
			}
			else {
				final CollectableField clctfModel = getModel().getField();
				final CollectableField clctfView = getFieldFromView();
				return clctfView.equals(clctfModel);
			}
		}
		catch (CollectableFieldFormatException ex) {
			return false;
		}
	}

	/**
	 * tries to make the model consistent with the view, if this isn't the case already.
	 * @throws CollectableFieldFormatException if the model can't be made consistent.
	 * @postcondition isConsistent()
	 */
	@Override
    public void makeConsistent() throws CollectableFieldFormatException {
		// NUCLOSINT-839: If the view is locked and isConsistent() is false,
		// viewToModel() does nothing. This seems to be a problem for search
		// ComboBoxes. As a quick fix, we simply return, doing nothing as before
		// but avoiding the assert. (Thomas Pasch)
		//
		if (isSearchComponent() && isViewLocked()) return;

		if (!isConsistent()) {
			viewToModel();
		}
		/*
		if (!isConsistent()) {
			final CollectableSearchCondition condModel = getSearchModel().getSearchCondition();
			final CollectableSearchCondition condView = getSearchConditionFromView();
			LOG.warn("makeConsistent failed on " + this + " field: " + getFieldName() + " -> " + getModel().getField().getValue());
		}
		 */
		assert isConsistent();
	}

	/**
	 * updates the view with the value in the model. This method must not update the model again!
	 * Note that <code>isConsistent()</code> is not a postcondition here, because this cannot be guaranteed
	 * in general. For example, one may set the <code>collectableField</code> for a <code>CollectableComboBox</code>,
	 * that doesn't contain this value in its list of possible values. In those cases, the view is not
	 * consistent with the model.
	 * @todo adjust comment and implementation
	 * @precondition isViewLocked()
	 * @todo add precondition isDetailsComponent()?
	 * @todo add postcondition isConsistent()
	 */
	protected final void modelToView() {
		if (!isViewLocked()) {
			throw new IllegalStateException("View must be locked.");
		}
		updateView(getModel().getField());
	}

	/**
	 * sets the view according to the given value.
	 * @param clctfValue
	 */
	protected abstract void updateView(CollectableField clctfValue);

	/**
	 * sets the given condition in this component, using text comp as this component's text component.
	 * @param atomiccond
	 * @param textcomp
	 * @precondition isViewLocked()
	 * @precondition canDisplay(atomiccond)
	 */
	protected final void modelToView(AtomicCollectableSearchCondition atomiccond, JTextComponent textcomp) {
		if(!canDisplay(atomiccond)) {
			throw new IllegalArgumentException("Condition cannot be displayed: " + LangUtils.toString(atomiccond));
		}
		final ComparisonOperator compop = (atomiccond == null) ? ComparisonOperator.NONE : atomiccond.getComparisonOperator();

		setComparisonOperator(compop);

		if (atomiccond instanceof CollectableComparisonWithOtherField) {
			assert canDisplayComparisonWithOtherField();
			final CollectableComparisonWithOtherField comparisonwf = (CollectableComparisonWithOtherField) atomiccond;
			setWithComparison(comparisonwf.getOtherField());
		}
		else if (atomiccond instanceof CollectableComparisonWithParameter) {
			assert canDisplayComparisonWithOtherField();
			final CollectableComparisonWithParameter comparisonwp = (CollectableComparisonWithParameter) atomiccond;
			setWithComparison(comparisonwp.getParameter());
		}
		else {
			resetWithComparison();
		}

		final String sText;
		if (compop.getOperandCount() < 2) {
			sText = null;
		}
		else {
			assert atomiccond != null;
			sText = atomiccond.getComparandAsString();
		}

		assert isViewLocked();
		textcomp.setText(sText);
	}

	/**
	 * updates the model with the value in the view. If the value in the view is invalid,
	 * the model is cleared, and a CollectableFieldFormatException is thrown.
	 * The view is locked during the execution of this method, to prevent recursive updates
	 * between view and model. If the view is locked already, this method does nothing.
	 * @throws CollectableFieldFormatException if the value in the view is invalid.
	 * @todo try to make this final.
	 */
	protected void viewToModel() throws CollectableFieldFormatException {
		runLocked(new ExceptionalRunnable() {
			@Override
            public void run() throws CollectableFieldFormatException {
				if (isSearchComponent()) {
					getSearchModel().setSearchCondition(getSearchConditionFromView());
				}
				else {
					try {
						viewToModel(getFieldFromView());
					}
					catch (CollectableFieldFormatException ex) {
						/** @todo Is it right to clear the model here? However note that it is required that the model
						 * fires a change event, so the CollectController is notified in Details mode and switches the CollectState
						 * as soon as the user starts typing eg. in a DateChooser field. */
						// The value in the view is not valid. At least, the model is cleared:
						getModel().clear();
						throw ex;
					}
				}
			}
		});
	}

	/**
	 * updates the model with the given value.
	 * @param clctfView
	 * @todo add precondition !isSearchComponent(), eliminate parameter and write a second method viewToModel(CollectableSearchCondition) for the isSearchComponent() case.
	 */
	protected final void viewToModel(CollectableField clctfView) {
		/** @todo check if it is right to do that here: */
		/** @todo No, it is not! The parameter is ignored in case of searchable. */
		// 22.01.04:
		if (isSearchComponent()) {
			updateSearchConditionInModel();
		}
		// :22.01.04
		else {
			getModel().setField(clctfView);
			// We need to adjust the appearance of the view here, as viewToModel is not (and must not be)
			// called from here:
			adjustAppearance();
		}
	}

	/**
	 * adjusts the appearance of the view reflecting the model. Only those changes to the view are allowed that don't
	 * cause the value (as defined in <code>getField()</code>) of the component to be changed again,
	 * that is: don't trigger another <code>CollectableComponentEvent</code>.
	 * Only the view's appearance (like foreground/background colors, borders etc.) may be adjusted here.
	 */
	protected void adjustAppearance() {
//		adjustBackground();
		getControlComponent().repaint();
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	protected void adjustBackground() {
//		getControlComponent().setBackground(getBackgroundColor());
	}

	/**
	 * A convenient method for '<code>makeConsistent(); getModel().getField()</code>', i.e.
	 * <em>different</em> from '<code>getModel().getField()</code>'.
	 *
	 * @return CollectableField
	 * @throws CollectableFieldFormatException
	 */
	@Override
    public CollectableField getField() throws CollectableFieldFormatException {
		makeConsistent();

		return getModel().getField();
	}

	@Override
    public final void setField(CollectableField clctfValue) {
		getModel().setField(clctfValue);
	}

	@Override
    public final CollectableSearchCondition getSearchCondition() throws CollectableFieldFormatException {
		if (!isSearchComponent()) {
			throw new IllegalStateException("searchComponent");
		}
		makeConsistent();

		return getSearchModel().getSearchCondition();
	}

	/**
	 * @param cond
	 * @postcondition LangUtils.equals(getSearchModel().getSearchCondition(), cond)
	 * @postcondition canDisplay(cond) --> isConsistent()
	 * @todo pull down to CollectableComponent?
	 */
	public final void setSearchCondition(CollectableSearchCondition cond) {
		if (!isSearchComponent()) {
			throw new IllegalStateException("searchComponent");
		}
		getSearchModel().setSearchCondition(cond);

		assert LangUtils.equals(getSearchModel().getSearchCondition(), cond);
		assert !canDisplay(cond) || isConsistent();
	}

	@Override
    public boolean canDisplay(CollectableSearchCondition cond) {
		return !(cond instanceof CollectableComparisonWithOtherField || cond instanceof CollectableComparisonWithParameter)
			|| canDisplayComparisonWithOtherField();
	}

	/**
	 * @return Can this component display a {@link CollectableComparisonWithOtherField}?
	 * @precondition isSearchComponent()
	 * @todo move to CollectableComponent interface?
	 */
	public boolean canDisplayComparisonWithOtherField() {
		return clcte != null;
	}

	/**
	 * sets the collectable entity that getEntityField() belongs to.
	 * This is needed to support <code>ComparisonWithOtherField</code>.
	 * @param clcte May be <code>null</code>.
	 * @precondition clcte != null --> clcte.getFieldNames().contains(getEntityField().getName())
	 */
	@Override
    public void setCollectableEntity(CollectableEntity clcte) {
		if (!(clcte == null || clcte.getEntityField(getEntityField().getName()) != null )) {
			throw new IllegalArgumentException("clcte");
		}
		this.clcte = clcte;
	}

	/**
	 * sets the given <code>CollectableField</code> or <code>CollectableSearchCondition</code>.
	 * @param oValue
	 * @precondition isSearchComponent() --> (oValue instanceof CollectableField)
	 * @precondition !isSearchComponent() --> (oValue instanceof CollectableSearchCondition)
	 */
	protected void setObjectValue(Object oValue) {
		if (isSearchComponent()) {
			setSearchCondition((CollectableSearchCondition) oValue);
		}
		else {
			setField((CollectableField) oValue);
		}
	}

	/**
	 * clears the field.
	 * @postcondition getField().isNull()
	 * @postcondition isSearchComponent() -> (getSearchCondition() == null)
	 */
	@Override
    public final void clear() {
		getModel().clear();
	}

	/**
	 * @param bEnabled
	 */
	@Override
    public void setEnabled(boolean bEnabled) {
		getJComponent().setEnabled(bEnabled);
	}

	/**
	 * @param bScalable
	 */
	@Override
	public void setScalable(boolean bln) {
		// only for special components
	}

	/**
	 * @param sNextFocusComponent
	 */
	@Override
	public void setNextFocusComponent(String sNextFocusComponent) {
		this.sNextFocusComponent = sNextFocusComponent;
	}

	/**
	 * @param bEnabled
	 */
	@Override
    public void setEnabledByInitial(boolean bEnabled) {
		bEnabledByInitial = bEnabled;
	}

	@Override
    public void setToolTipText(String sToolTipText) {
		getJComponent().setToolTipText(sToolTipText);
	}

	@Override
    public void setOpaque(boolean bOpaque) {
		getJComponent().setOpaque(bOpaque);
	}

	@Override
    public void setFillControlHorizontally(boolean bFill) {
		// do nothing
	}

	/**
	 * makes the component insertable (or not). At the moment, this applies for comboboxes only.
	 * @param bInsertable Can new values (apart from this component's given list of values) be inserted?
	 */
	@Override
    public abstract void setInsertable(boolean bInsertable);

	/**
	 * sets the text for the contained label, if any.
	 * @param sLabel
	 */
	@Override
    public void setLabelText(String sLabel) {
		// do nothing here
	}

	/**
	 * sets the mnemonic for this component (or a contained label), if applicable.
	 * @param cMnemonic
	 */
	@Override
    public void setMnemonic(char cMnemonic) {
		// do nothing here
	}

	/**
	 * sets the number of columns for this component, if applicable.
	 * @param iColumns
	 */
	@Override
    public void setColumns(int iColumns) {
		// do nothing here
	}

	/**
	 * sets the number of rows for this component, if reasonable.
	 * Note that this doesn't apply to comboboxes.
	 * @param iRows
	 */
	@Override
    public void setRows(int iRows) {
		// do nothing here
	}

	private void lockedModelToView() {
		runLocked(new Runnable() {
			@Override
            public void run() {
				modelToView();
			}
		});
	}

	/**
	 * Implementation of <code>CollectableComponentModelListener</code>.
	 * The model has changed. Updates the view.
	 * @param ev
	 */
	@Override
    public void collectableFieldChangedInModel(CollectableComponentModelEvent ev) {
		lockedModelToView();
	}

	/**
	 * Implementation of <code>CollectableComponentModelListener</code>.
	 * The model has changed. Updates the view.
	 * @param ev
	 */
	@Override
    public void valueToBeChanged(DetailsComponentModelEvent ev) {
		lockedModelToView();
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
					clear();
				}
				else {
					if (cond instanceof CollectableComparisonWithOtherField && canDisplayComparisonWithOtherField())
					{
						final CollectableComparisonWithOtherField comparisonwf = (CollectableComparisonWithOtherField) cond;
						handleComparisonOperator(comparisonwf.getComparisonOperator());
						setWithComparison(comparisonwf.getOtherField());
						setField(null);
					}
					else if (cond instanceof CollectableComparisonWithParameter && canDisplayComparisonWithOtherField()) {
						final CollectableComparisonWithParameter comparisonwp = (CollectableComparisonWithParameter) cond;
						handleComparisonOperator(comparisonwp.getComparisonOperator());
						setWithComparison(comparisonwp.getParameter());
						setField(null);
					}
					else if (cond instanceof CollectableComparison) {
						final CollectableComparison comparison = (CollectableComparison) cond;
						handleComparisonOperator(comparison.getComparisonOperator());
						resetWithComparison();
						setField(comparison.getComparand());
						// Note that setSearchCondition() needs setField(), but not vice versa.
					}
					else {
						// If this happens, there needs to be a more specific implementation for the component.
						throw new CommonFatalException(getBadSearchConditionErrorMessage());
					}
				}
			}

			private void handleComparisonOperator(ComparisonOperator compop) {
				if (hasComparisonOperator()) {
					setComparisonOperator(compop);
				}
				else if (compop != ComparisonOperator.EQUAL) {
					// If this happens, there needs to be a more specific implementation for the component.
					throw new CommonFatalException(getBadSearchConditionErrorMessage());
				}
			}

			private String getBadSearchConditionErrorMessage() {
				return CommonLocaleDelegate.getMessage("AbstractCollectableComponent.8","Die angegebene Suchbedingung kann in der Komponente f\u00fcr das Feld {0} nicht dargestellt werden.", getFieldName());
			}
		});
	}

	/**
	 * default implementation.
	 * @return
	 * @precondition isSearchComponent()
	 * @throws CollectableFieldFormatException
	 */
	protected CollectableSearchCondition getSearchConditionFromView() throws CollectableFieldFormatException {
		return SearchComponentModel.getDefaultSearchCondition(getFieldFromView(), getEntityField());
	}

	/**
	 * updates the search condition in the model, ignoring a possible
	 * <code>CollectableFieldFormatException</code>.
	 * @precondition isSearchComponent()
	 */
	protected void updateSearchConditionInModel() {
		try {
			getSearchModel().setSearchCondition(getSearchConditionFromView());
		}
		catch (CollectableFieldFormatException ex) {
			// this is ignored here by definition of this method.
		}
	}

	/**
	 * @return the tooltip text to show when the search condition is NONE.
	 * @precondition isSearchComponent()
	 */
	protected String getTooltipTextForSearchConditionNone() {
		final StringBuffer sb = new StringBuffer(getEntityField().getLabel());
		sb.append(" (");
		sb.append(CommonLocaleDelegate.getMessage("comparisonOperator.NONE.description", "NONE"));
		sb.append(")");
		return sb.toString();
	}

	/**
	 * @return the tooltip text for the current searchcondition
	 * @precondition isSearchComponent()
	 */
	protected String getToolTipTextForCurrentSearchCondition() {
		String result;
		try {
			final CollectableSearchCondition searchcond = getSearchConditionFromView();
			result = (searchcond == null) ? getTooltipTextForSearchConditionNone() : searchcond.accept(new ToHumanReadablePresentationVisitor());
		}
		catch (CollectableFieldFormatException ex) {
			result = CommonLocaleDelegate.getMessage("AbstractCollectableComponent.5","<Ung\u00fcltige Suchbedingung:"+ex.getMessage()+">",ex.getMessage());
		}
		return result;
	}

	/**
	 * @return the tool tip text to show in multi edit mode
	 * @precondition isMultiEditable()
	 */
	protected String getToolTipTextForMultiEdit() {
		if (!isMultiEditable()) {
			throw new IllegalStateException("multiEditable");
		}
		String result;
		final String sLabel = getEntityField().getLabel();
		try {
			if (getDetailsModel().isValueToBeChanged()) {
				final CollectableField clctf = getFieldFromView();
				final String sValue = clctf.isNull() ? CommonLocaleDelegate.getMessage("AbstractCollectableComponent.2","<gel\u00f6scht>") : CommonLocaleDelegate.getMessage("AbstractCollectableComponent.1","<ge\u00e4ndert>");
				result = sLabel + " = " + sValue;
			}
			else {
				result = sLabel + " (" + CommonLocaleDelegate.getMessage("AbstractCollectableComponent.3","<keine \u00c4nderung>") + ")";
			}
		}
		catch (CollectableFieldFormatException ex) {
			result = CommonLocaleDelegate.getMessage("AbstractCollectableComponent.4","<Ung\u00fcltiger Wert>");
		}
		return result;
	}

	@Override
    public final ReferencingListener getReferencingListener() {
		return reflistener;
	}

	// @todo include in interface (again)?
	public void setReferencingListener(ReferencingListener listener) {
		reflistener = listener;
	}

	private void fireShowDetails() {
		final ReferencingListener reflistener = getReferencingListener();
		if (reflistener != null) {
			reflistener.showDetails(new CollectableComponentEvent(this));
		}
	}

	private void fireCreateNew() {
		final ReferencingListener reflistener = getReferencingListener();
		if (reflistener != null) {
			reflistener.createNew(new CollectableComponentEvent(this));
		}
	}

	/**
	 * @return Does this component have a comparison operator that can be set by the user?
	 * This default implementation returns <code>false</code>.
	 * @precondition isSearchComponent()
	 */
	public boolean hasComparisonOperator() {
		return false;
	}

	/**
	 * @precondition hasComparisonOperator()
	 */
	public final ComparisonOperator getComparisonOperator() {
		if (!hasComparisonOperator()) {
			throw new IllegalStateException();
		}
		return compop;
	}

	/**
	 * @precondition hasComparisonOperator()
	 */
	public void setComparisonOperator(ComparisonOperator compop) {
		if (!hasComparisonOperator()) {
			throw new IllegalStateException();
		}
		this.compop = compop;
	}

	/**
	 * Subclasses may add menu items to the default popup menu or provide their own completely.
	 * @return a new popup menu for this component.
	 * @see JPopupMenuFactory
	 */
	@Override
    public JPopupMenu newJPopupMenu() {
		JPopupMenu result;

		if (isSearchComponent() && hasComparisonOperator()) {
			result = newComparisonOperatorPopupMenu();
		}
		else {
			// regular popup menu:
			result = new JPopupMenu();

			if (isMultiEditable()) {
				result.add(newNoChangeEntry());
				result.add(newClearEntry());
			}

			if (getEntityField().isReferencing()) {
				if (result.getComponentCount() > 0) {
					result.addSeparator();
				}
				result.add(newShowDetailsEntry());
				result.add(newInsertEntry());
			}

			if (result.getComponentCount() == 0) {
				result = null;
			}
		}
		return result;
	}

	/**
	 * Subclasses should redefine {@link #newJPopupMenu()} rather than this method, to change the popup menu for this component.
	 * @return the popup menu listener for this component. This default implementation returns a dynamic popup menu listener
	 * that creates the popup menu on demand, each time it is about to be displayed.
	 * @postcondition result != null
	 */
	protected JPopupMenuListener newJPopupMenuListener() {
		return new DefaultJPopupMenuListener(this);
	}

	/**
	 * sets up the popup menu listener for this component.
	 * This default implementation adds a mouse listener to the control component.
	 * May be overridden if the popup menu shouldn't be activated from the control component and/or should be activated
	 * from other components contained in this component (the label etc.).
	 * @param popupmenulistener
	 */
	protected void setupJPopupMenuListener(JPopupMenuListener popupmenulistener) {
		getControlComponent().addMouseListener(popupmenulistener);
	}

	/**
	 * @return
	 * @precondition hasComparisonOperator()
	 */
	public JPopupMenu newComparisonOperatorPopupMenu() {
		if(!hasComparisonOperator()) {
			throw new IllegalStateException();
		}
		final JPopupMenu result = new JPopupMenu(CommonLocaleDelegate.getMessage("AbstractCollectableComponent.16","Vergleichsoperator"));

		// 1. comparison operators:
		final ButtonGroup btngrpComparisonOperators = new ButtonGroup();

		final ItemListener itemlistener = new ItemListener() {
			@Override
            public void itemStateChanged(ItemEvent ev) {
				if (ev.getStateChange() == ItemEvent.SELECTED) {
					final String sOperatorName = ((AbstractButton) ev.getItem()).getActionCommand();
					setComparisonOperator(ComparisonOperator.getInstance(sOperatorName));
					runLocked(new Runnable() {
						@Override
                        public void run() {
							updateSearchConditionInModel();
						}
					});
				}
			}
		};

		ComparisonOperator[] supportedComparisonOperators = getSupportedComparisonOperators();
		if(supportedComparisonOperators == null)
			return null;

		for(ComparisonOperator compop : supportedComparisonOperators) {
			JMenuItem mi = new JRadioButtonMenuItem(CommonLocaleDelegate.getMessage(compop.getResourceIdForLabel(), null));
			mi.setActionCommand(compop.name());
			mi.setToolTipText(CommonLocaleDelegate.getMessage(compop.getResourceIdForDescription(), null));
			result.add(mi);
			btngrpComparisonOperators.add(mi);
			mi.addItemListener(itemlistener);
		}

		result.addPopupMenuListener(new PopupMenuListener() {
			@Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent ev) {
				for(int i = 0, n = result.getComponentCount(); i < n; i++) {
					Component c = result.getComponent(i);
					if(c instanceof JMenuItem
						&& StringUtils.emptyIfNull(((JMenuItem) c).getActionCommand()).equals(getComparisonOperator().name()))
						((JMenuItem) c).setSelected(true);
				}
			}

			@Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent ev) {
			}

			@Override
            public void popupMenuCanceled(PopupMenuEvent ev) {
			}
		});

		// 2. right operand (value or other field):
		if (canDisplayComparisonWithOtherField()) {
			addRightOperandToPopupMenu(result, this);
		}

		return result;
	}


	protected ComparisonOperator[] getSupportedComparisonOperators() {
		return ComparisonOperator.getComparisonOperators();
	}


	/**
	 * @param result
	 * @param clctcomp
	 */
	private static void addRightOperandToPopupMenu(JPopupMenu result, final AbstractCollectableComponent clctcomp) {
		result.addSeparator();
		final ButtonGroup btngrpCompareWith = new ButtonGroup();

		final JRadioButtonMenuItem miValue = new JRadioButtonMenuItem(CommonLocaleDelegate.getMessage("AbstractCollectableComponent.17","Wertvergleich"));
		miValue.setToolTipText(CommonLocaleDelegate.getMessage("AbstractCollectableComponent.10","Dieses Feld mit einem festen Wert vergleichen"));
		result.add(miValue);
		btngrpCompareWith.add(miValue);
		miValue.addActionListener(new ActionListener() {
			@Override
            public void actionPerformed(ActionEvent ev) {
				clctcomp.resetWithComparison();
				clctcomp.runLocked(new Runnable() {
					@Override
                    public void run() {
						clctcomp.updateSearchConditionInModel();
					}
				});
			}
		});


		final JRadioButtonMenuItem miOtherField = new JRadioButtonMenuItem(CommonLocaleDelegate.getMessage("AbstractCollectableComponent.12","Feldvergleich..."));
		miOtherField.setToolTipText(CommonLocaleDelegate.getMessage("AbstractCollectableComponent.9","Dieses Feld mit dem Inhalt eines anderen Felds vergleichen"));
		result.add(miOtherField);
		btngrpCompareWith.add(miOtherField);
		miOtherField.addActionListener(new ActionListener() {
			@Override
            public void actionPerformed(ActionEvent ev) {
				assert clctcomp.clcte != null;

				// select entity field with the same data type:
				final List<CollectableEntityField> lstclctefFiltered = CollectionUtils.select(
						CollectableUtils.getCollectableEntityFields(clctcomp.clcte),
						new Predicate<CollectableEntityField>() {
							@Override
                            public boolean evaluate(CollectableEntityField clctef) {
								return clctef.getJavaClass() == clctcomp.clctef.getJavaClass();
							}
						});
				// and sort by label:
				final List<CollectableEntityField> lstclctefSorted =
						CollectionUtils.sorted(lstclctefFiltered, new CollectableEntityField.LabelComparator());

				final JComboBox cmbbx = new JComboBox(lstclctefSorted.toArray());
				cmbbx.setSelectedItem(clctcomp.getComparisonOtherField());

				final int iBtn = JOptionPane.showConfirmDialog(clctcomp.getJComponent(), new Object[] {CommonLocaleDelegate.getMessage("AbstractCollectableComponent.6","Anderes Feld: "), cmbbx},
					CommonLocaleDelegate.getMessage("AbstractCollectableComponent.15","Vergleich mit anderem Feld"), JOptionPane.OK_CANCEL_OPTION);

				if (iBtn == JOptionPane.OK_OPTION) {
					clctcomp.setWithComparison((CollectableEntityField) cmbbx.getSelectedItem());
					if (clctcomp.getComparisonOtherField() != null) {
						// clear the view:
						clctcomp.updateView(CollectableUtils.getNullField(clctcomp.getEntityField()));

						if (clctcomp.compop.getOperandCount() < 2) {
							// If the user selects "other field" and forgot to set the operator, we assume "EQUAL":
							clctcomp.compop = ComparisonOperator.EQUAL;
						}
					}
					clctcomp.runLocked(new Runnable() {
						@Override
                        public void run() {
							clctcomp.updateSearchConditionInModel();
						}
					});
				}
			}
		});

		final List<ComparisonParameter> compatibleParameters = ComparisonParameter.getCompatibleParameters(clctcomp.getEntityField());
		final JRadioButtonMenuItem miParameterField = new JRadioButtonMenuItem(CommonLocaleDelegate.getMessage("AbstractCollectableComponent.18", null));
		miParameterField.setToolTipText(CommonLocaleDelegate.getMessage("AbstractCollectableComponent.19", null));
		btngrpCompareWith.add(miParameterField);
		if (compatibleParameters.size() > 0) {
			result.add(miParameterField);
			miParameterField.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ev) {
					ResourceIdMapper<ComparisonParameter> mapper = new ResourceIdMapper<ComparisonParameter>(compatibleParameters);
					JComboBox cmbbx = new JComboBox(CollectionUtils.sorted(compatibleParameters, mapper).toArray());
					cmbbx.setRenderer(new DefaultListRenderer(mapper));
					cmbbx.setSelectedItem(clctcomp.getComparisonParameter());

					final int opt = JOptionPane.showConfirmDialog(clctcomp.getJComponent(),
						new Object[] { CommonLocaleDelegate.getMessage("AbstractCollectableComponent.20", null), cmbbx },
						CommonLocaleDelegate.getMessage("AbstractCollectableComponent.19", null), JOptionPane.OK_CANCEL_OPTION);

					if (opt == JOptionPane.OK_OPTION) {
						clctcomp.setWithComparison((ComparisonParameter) cmbbx.getSelectedItem());
						if (clctcomp.getComparisonParameter() != null) {
							clctcomp.updateView(CollectableUtils.getNullField(clctcomp.getEntityField()));
							if (clctcomp.compop.getOperandCount() < 2) {
								clctcomp.compop = ComparisonOperator.EQUAL;
							}
						}
						clctcomp.runLocked(new Runnable() {
							@Override
							public void run() {
								clctcomp.updateSearchConditionInModel();
							}
						});
					}
				}
			});
		}

		result.addPopupMenuListener(new PopupMenuListener() {
			@Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent ev) {
				if (clctcomp.getComparisonParameter() != null) {
					miParameterField.setSelected(true);
				} else if (clctcomp.getComparisonOtherField() == null || clctcomp.getComparisonOperator().getOperandCount() < 2) {
					miValue.setSelected(true);
				}
				else {
					miOtherField.setSelected(true);
				}
			}

			@Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent ev) {
			}

			@Override
            public void popupMenuCanceled(PopupMenuEvent ev) {
			}
		});
	}

	/**
	 * @precondition isMultiEditable()
	 * @return a new "no change" entry for the context menu in multi edit mode
	 */
	protected final JMenuItem newNoChangeEntry() {
		if (!isMultiEditable()) {
			throw new IllegalStateException();
		}
		final JMenuItem result = new JMenuItem(TEXT_NOCHANGE);
		result.addActionListener(new ActionListener() {
			@Override
            public void actionPerformed(ActionEvent ev) {
				// restore common value, if any - otherwise clear the field:
				final DetailsComponentModel clctcompmodel = getDetailsModel();
				if (clctcompmodel.hasCommonValue()) {
					clctcompmodel.setField(clctcompmodel.getCommonValue());
				}
				else {
					clctcompmodel.clear();
				}
				clctcompmodel.setValueToBeChanged(false);
			}
		});
		return result;
	}

	/**
	 * @precondition getEntityField().isReferencing()
	 * @return a new "show details" entry for the context menu in edit mode
	 */
	protected final JMenuItem newShowDetailsEntry() {
		if (!getEntityField().isReferencing()) {
			throw new IllegalStateException();
		}
		final JMenuItem result = new JMenuItem(TEXT_SHOWDETAILS);
		boolean bShowDetailsEnabled;
		try {
			bShowDetailsEnabled = isReferencedEntityDisplayable() && getField().getValueId() != null;
		}
		catch (CollectableFieldFormatException ex) {
			bShowDetailsEnabled = false;
		}
		result.setEnabled(bShowDetailsEnabled);

		result.addActionListener(new ActionListener() {
			@Override
            public void actionPerformed(ActionEvent ev) {
				fireShowDetails();
			}
		});
		return result;
	}

	/**
	 * @precondition getEntityField().isReferencing()
	 * @return a new "new" entry for the context menu in edit mode
	 */
	protected final JMenuItem newInsertEntry() {
		if (!getEntityField().isReferencing()) {
			throw new IllegalStateException();
		}
		final JMenuItem result = new JMenuItem(TEXT_NEW);
		String referencedEntity = getEntityField().getReferencedEntityName();
		boolean bInsertEnabled = isReferencedEntityDisplayable();
		if (bInsertEnabled) {
			if (Modules.getInstance().existModule(referencedEntity)) {
				bInsertEnabled = SecurityCache.getInstance().isNewAllowedForModule(referencedEntity);
			}
			else {
				bInsertEnabled = SecurityCache.getInstance().isWriteAllowedForMasterData(referencedEntity);
			}
		}
		result.setEnabled(bInsertEnabled && getJComponent().isEnabled());

		result.addActionListener(new ActionListener() {
			@Override
            public void actionPerformed(ActionEvent ev) {
				fireCreateNew();
			}
		});
		return result;
	}

	/**
	 * @precondition getEntityField().isReferencing()
	 * @return true, if the referenced entity is displayable
	 */
	@SuppressWarnings("deprecation")
	private boolean isReferencedEntityDisplayable() {
		final CollectableEntityField clctef = getEntityField();
		return clctef.isReferencedEntityDisplayable() && isEntityDisplayable(clctef.getReferencedEntityName());

		/** @todo if isReferencedEntityDisplayable() is eliminated, this should be: */
//		return isEntityDisplayable(clctef.getReferencedEntityName());
	}

	private boolean isEntityDisplayable(String sEntityName) {
		return DefaultCollectableEntityProvider.getInstance().isEntityDisplayable(sEntityName);
	}

	/**
	 * @return a new "clear" entry for the context menu in edit mode
	 */
	protected final JMenuItem newClearEntry() {
		final JMenuItem result = new JMenuItem(TEXT_CLEAR);
		result.addActionListener(new ActionListener() {
			@Override
            public void actionPerformed(ActionEvent ev) {
				clear();
			}
		});
		return result;
	}

	/** @todo comment */
	protected Color getBackgroundColor() {
		final Color result;

		/** @todo + isEnabled() && */
		if (isMultiEditable() && !getDetailsModel().hasCommonValue() && !getDetailsModel().isValueToBeChanged())
		{
			result = colorCommonValues;
		}
		else {
			boolean hasValue = !getModel().getField().isNull();

			if (isDetailsComponent() && getDetailsModel().isMandatoryAdded()) {
				result = hasValue || hasFocus() ? null : ClientParameterProvider.getInstance().getColorValue(ParameterProvider.KEY_MANDATORY_ADDED_ITEM_BACKGROUND_COLOR, new Color(255,255,200));
			} else if (isDetailsComponent() && getDetailsModel().isMandatory()) {
				result = hasValue || hasFocus() ? null : ClientParameterProvider.getInstance().getColorValue(ParameterProvider.KEY_MANDATORY_ITEM_BACKGROUND_COLOR, new Color(255,255,200));
			} else {
				result = hasFocus() ? null : NuclosSyntheticaConstants.DEFAULT_BACKGROUND.equals(comp.getBackground()) ? Color.WHITE : comp.getBackground();
			}
		}

		//Logger.getLogger(AbstractCollectableComponent.class).debug("getBackgroundColor: result = " + result);
		return result;
	}

	private boolean hasFocus() {
		// if the component has focus, other code far, far away sets the nice yellow
		// background which we don't want to overwride here ...
		JComponent focusComponent = getControlComponent();

		if(getJComponent() instanceof LabeledComboBox) {
			JComboBox cb = ((LabeledComboBox)getJComponent()).getJComboBox();
			focusComponent = (JComponent) (cb.getEditor() != null ? cb.getEditor().getEditorComponent() : cb);
		}

		return focusComponent.hasFocus();
	}

	@Override
    public String getDynamicToolTipText() {
		final String result;
		if (isSearchComponent()) {
			result = getToolTipTextForCurrentSearchCondition();
		}
		else if (isMultiEditable()) {
			result = getToolTipTextForMultiEdit();
		}
		else {
			// the default tooltip text:
			result = getJComponent().getToolTipText();
		}
		return result;
	}

	/**
	 * provides a common implementation of getSearchConditionFromView()
	 * @param sLikeComparand
	 * @return
	 * @throws CollectableFieldFormatException
	 */
	@SuppressWarnings("deprecation")
	protected CollectableSearchCondition getSearchConditionFromViewImpl(String sLikeComparand) throws CollectableFieldFormatException {
		if (viewSupportsComparisonWith()) {
			if (getComparisonOtherField() != null) {
				return new CollectableComparisonWithOtherField(getEntityField(), getComparisonOperator(), getComparisonOtherField());
			} else if (getComparisonParameter() != null) {
				return new CollectableComparisonWithParameter(getEntityField(), getComparisonOperator(), getComparisonParameter());
			}
		}
		return CollectableTextComponentHelper.getAtomicSearchConditionFromView(getEntityField(), getComparisonOperator(), this, sLikeComparand);
	}

	/**
	 * @return Does the view currently contain a comparison with another field or a parameter?
	 */
	private boolean viewSupportsComparisonWith() {
		return isSearchComponent() &&
				canDisplayComparisonWithOtherField() &&
				getComparisonOperator().getOperandCount() == 2;
	}

	DocumentListener newDocumentListenerForTextComponentWithComparisonOperator() {
		return new MyDocumentListener(this);
	}

	/**
	 * @return a suitable <code>TableCellRenderer</code> for this component.
	 * This default implementation returns a new javax.swing.table.DefaultTableCellRenderer for Details components
	 * (and a special renderer for Search components).
	 * Successors may provide a more specific renderer here.
	 */
	@Override
    public TableCellRenderer getTableCellRenderer() {
		if (isSearchComponent()) {
			return new CollectableComponentDefaultTableCellRenderer();
		}
		else {
			return new CollectableComponentDetailTableCellRenderer();
		}
	}

	@Override
    public Object getProperty(String sName) {
		return getProperties().get(sName);
	}

	@Override
    public void setProperty(String sName, Object oValue) {
		synchronized (this) {
			if (mpProperties == null) {
				mpProperties = new TreeMap<String, Object>();
			}
		}
		mpProperties.put(sName, oValue);

		assert LangUtils.equals(getProperty(sName), oValue);
	}

	@Override
    public synchronized Map<String, Object> getProperties() {
		final Map<String, Object> result = (mpProperties == null) ? Collections.<String, Object>emptyMap() : Collections.unmodifiableMap(mpProperties);
		assert result != null;
		return result;
	}

	/**
	 * @return <code>null</code>
	 */
	@Override
    public Preferences getPreferences() {
		return null;
	}

	/**
	 * NOP.
	 * @param prefs
	 */
	@Override
    public void setPreferences(Preferences prefs) {
		// do nothing here
	}

	/**
	 * CollectableComponents are equal iff they are identical. This behavior may not be changed by subclasses.
	 * @param o
	 */
	@Override
	public final boolean equals(Object o) {
		return super.equals(o);
	}

	/**
	 * @see #equals(Object)
	 */
	@Override
	public final int hashCode() {
		return super.hashCode();
	}

	protected class BackgroundColorProvider implements ColorProvider {
		@Override
        public Color getColor(Color colorDefault) {
			final Color color = getBackgroundColor();
			return (color != null) ? color : colorDefault;
		}
	}	// inner class BackgroundColorProvider

	/**
	 * DocumentListener for text components
	 */
	private static class MyDocumentListener implements DocumentListener {
		private final AbstractCollectableComponent clctcomp;

		MyDocumentListener(AbstractCollectableComponent clctcomp) {
			this.clctcomp = clctcomp;
		}

		@Override
        public void changedUpdate(DocumentEvent ev) {
			// this is never called.
			assert false;
		}

		@Override
        public void insertUpdate(final DocumentEvent ev) {
			if (clctcomp.isSearchComponent()) {
				clctcomp.runLocked(new Runnable() {
					@Override
                    public void run() {
						// We automatically switch to "compare by value" when anything is entered:
						clctcomp.resetWithComparison();

						// We automatically switch to LIKE, if a wildcard was entered:
						final String sInsertedText;
						try {
							sInsertedText = ev.getDocument().getText(ev.getOffset(), ev.getLength());
						}
						catch (BadLocationException ex) {
							throw new CommonFatalException(ex);
						}
						if (CollectableLikeCondition.containsWildcard(sInsertedText)) {
							clctcomp.setComparisonOperator(ComparisonOperator.LIKE);
						}
						else {
							// We automatically switch from NONE/IS_NULL/IS_NOT_NULL to EQUAL when a character is entered:
							if (clctcomp.getComparisonOperator().getOperandCount() < 2) {
								clctcomp.setComparisonOperator(ComparisonOperator.EQUAL);
							}
						}
						clctcomp.updateSearchConditionInModel();
					}
				});
			}
			else {
				updateModel();
			}
		}

		@Override
        public void removeUpdate(final DocumentEvent ev) {
			if (clctcomp.isSearchComponent()) {
				clctcomp.runLocked(new Runnable() {
					@Override
                    public void run() {
						final String sText;
						try {
							sText = ev.getDocument().getText(0, ev.getDocument().getLength());
						}
						catch (BadLocationException ex) {
							throw new CommonFatalException(ex);
						}
						if (StringUtils.isNullOrEmpty(sText)) {
							if (clctcomp.getComparisonOperator().getOperandCount() >= 2) {
								clctcomp.setComparisonOperator(ComparisonOperator.NONE);
							}
						}
						clctcomp.updateSearchConditionInModel();
					}
				});
			}
			else {
				updateModel();
			}
		}

		private void updateModel() {
			try {
				clctcomp.viewToModel();
			}
			catch (CollectableFieldFormatException ex) {
				// do nothing. The model can't be updated.
				assert !clctcomp.isConsistent();
			}
		}
	}	// inner class MyDocumentListener


	/**
	 * default table cell renderer for (search) CollectableComponents.
	 */
	protected class CollectableComponentDefaultTableCellRenderer implements TableCellRenderer {

		public CollectableComponentDefaultTableCellRenderer() {
		}

		@Override
        public Component getTableCellRendererComponent(JTable tbl, Object oValue, boolean bSelected, boolean bHasFocus, int iRow, int iColumn) {
			setObjectValue(oValue);

			final JComponent result = getControlComponent();

			if (result instanceof JLabel) {
				((JLabel) result).setVerticalAlignment(SwingConstants.TOP);
			}

			return result;
		}
	}

	protected static class CollectableComponentDetailTableCellRenderer extends javax.swing.table.DefaultTableCellRenderer implements TableCellRenderer {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		// blurfilter to hide data on which the user has no read permission
		BoxBlurFilter filter = new BoxBlurFilter(20, 10, 1);
		BufferedImageOpEffect blurEffect = new BufferedImageOpEffect(filter);

		public CollectableComponentDetailTableCellRenderer() {
			setVerticalAlignment(SwingConstants.TOP);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Component getTableCellRendererComponent(JTable tbl, Object oValue, boolean bSelected, boolean bHasFocus, int iRow, int iColumn) {

			super.getTableCellRendererComponent(tbl, oValue, bSelected, bHasFocus, iRow, iColumn);

			setAlignmentX(JLabel.CENTER_ALIGNMENT);
			setBackground(bSelected ? tbl.getSelectionBackground() : tbl.getBackground());
			setForeground(bSelected ? tbl.getSelectionForeground() : tbl.getForeground());

			// check whether the data of the component is readable for current user, by asking the security agent of the actual field
			if (tbl.getModel() instanceof SortableCollectableTableModel<?>) {
				SortableCollectableTableModel<Collectable> tblModel = (SortableCollectableTableModel<Collectable>)tbl.getModel();
				if (tblModel.getRowCount() >= iRow+1) {
					Collectable clct = tblModel.getCollectable(iRow);
					Integer iTColumn = tbl.getColumnModel().getColumn(iColumn).getModelIndex();
					CollectableEntityField clctef = tblModel.getCollectableEntityField(iTColumn);
					clctef.getSecurityAgent().setCollectable(clct);
					if (!clctef.isReadable()) {
						BufferedLayerUI<JComponent> layerUI = new BufferedLayerUI<JComponent>();
						JXLayer<JComponent> layer = new JXLayer<JComponent>(this, layerUI);
						layerUI.setLayerEffects(blurEffect);
						return layer;
					}
				}
			}
			return this;
		}
	}

}	// class AbstractCollectableComponent
