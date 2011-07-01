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
package org.nuclos.client.ui.collect;

import java.awt.CardLayout;
import java.awt.Component;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.OverlayLayout;

import org.nuclos.client.NuclosIcons;
import org.nuclos.client.common.SearchConditionSubFormController;
import org.nuclos.client.ui.TransparentImagePanel;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.CollectableComponent.CanDisplay;
import org.nuclos.client.ui.collect.component.model.SearchEditModel;
import org.nuclos.client.ui.collect.indicator.CollectPanelIndicator;
import org.nuclos.client.ui.collect.searcheditor.SearchEditorPanel;
import org.nuclos.common.collect.collectable.searchcondition.AtomicCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIdCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIdListCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSelfSubCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSubCondition;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.LogicalOperator;
import org.nuclos.common.collect.collectable.searchcondition.PlainSubCondition;
import org.nuclos.common.collect.collectable.searchcondition.ReferencingCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.SearchConditionUtils;
import org.nuclos.common.collect.collectable.searchcondition.SearchConditionUtils.HasType;
import org.nuclos.common.collect.collectable.searchcondition.TrueCondition;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;

/**
 * A search panel used to search for <code>Collectable</code>s.
 * <br>
 * <b>Responsibilities:</b>
 * <ul>
 *   <li>Contains a toolbar.</li>
 *   <li>Contains a panel holding the search fields ({@link #setEditView(EditView)}).</li>
 *   <li>Contains the search editor.</li>
 * </ul>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public class SearchPanel extends SearchOrDetailsPanel {
	
	CollectPanelIndicator cpi = new CollectPanelIndicator(CollectPanel.TAB_SEARCH);

	/**
	 * the transparency of the magnifier image.
	 */
	private static final float ALPHA_MAGNIFIER = 0.15f;

	private static final String CONSTRAINT_SEARCHEDITOR = "SEARCHEDITOR";
	private static final String CONSTRAINT_EDITCOMPONENT = "EDITPANEL";

	private boolean bSearchEditorVisible;

	/**
	 * Button: "Perform a search."
	 */
	public final JButton btnSearch = new JButton();

	/**
	 * Button: "Clear search condition".
	 */
	public final JButton btnClearSearchCondition = new JButton();

	/**
	 * Button: "Enter New mode."
	 */
	public final JButton btnNew = new JButton();
	
	/**
	 * Button: "Enter New mode with search values."
	 */
	public final JButton btnNewWithSearchValues = new JButton();
	
	/**
	 * combobox for searchfilter
	 */
	public final JComboBox cmbbxSearchFilter = new JComboBox();

	/**
	 * CheckBox: "Toggle search editor on/off."
	 */
	public final JCheckBoxMenuItem btnSearchEditor = new JCheckBoxMenuItem();

	private final CardLayout cardlayout = new CardLayout();
	private final JPanel pnlCards = new JPanel(cardlayout);

	private final SearchEditorPanel pnlSearchEditor = new SearchEditorPanel();
	
	private TransparentImagePanel pnlTransparentImage;
	
	private final JScrollPane scrlpnSearch;

	/**
	 * creates a new SearchPanel.
	 */
	public SearchPanel() {
		super(true);
		super.init();

		pnlTransparentImage = new TransparentImagePanel(NuclosIcons.getInstance().getSearchWatermarkIcon(), ALPHA_MAGNIFIER);
		pnlCards.add(new OverlayPanel(pnlTransparentImage, pnlCenter), CONSTRAINT_EDITCOMPONENT);
		pnlCards.add(pnlSearchEditor, CONSTRAINT_SEARCHEDITOR);

		//this.add(pnlToolBar, BorderLayout.NORTH);
		
		this.scrlpnSearch = new JScrollPane(this.pnlCards, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.scrlpnSearch.setBorder(BorderFactory.createEmptyBorder());
		this.scrlpnSearch.getHorizontalScrollBar().setUnitIncrement(20);
		this.scrlpnSearch.getVerticalScrollBar().setUnitIncrement(20);
		
		//this.add(scrlpnSearch, BorderLayout.CENTER);
		//this.add(UIUtils.newStatusBar(tfStatusBar), BorderLayout.SOUTH);

		this.setCenterComponent(scrlpnSearch);
		this.setSouthComponent(UIUtils.newStatusBar(tfStatusBar));
		
		this.setSearchEditorVisible(false);
	}
	
	public void setBackgroundImage(ImageIcon icon) {
		pnlTransparentImage.setImage(icon);
	}

	@Override
	protected void setupDefaultToolBarActions(JToolBar toolBar) {
		
		toolBar.add(cpi.getJPanel());
		
		toolBar.add(btnSearch);
		toolBar.add(btnClearSearchCondition);
		
		btnSearchEditor.setOpaque(false);
		addPopupExtraComponent(btnSearchEditor);
		// result.add(btnSearchEditor);

		toolBar.add(btnNew);
		addToolBarComponent(btnNewWithSearchValues);
		// result.add(btnNewWithSearchValues);

		btnSearch.setName("btnSearch");
		btnSearch.setMnemonic('S');
		btnSearch.putClientProperty("hideActionText", Boolean.TRUE);

		btnClearSearchCondition.setName("btnSearchCondition");
		btnClearSearchCondition.setMnemonic('E');

		btnNew.setName("btnNew");
		btnNew.setMnemonic('N');
		btnNew.putClientProperty("hideActionText", Boolean.TRUE);
		
		btnNewWithSearchValues.setName("btnNewWithSearchValues");
		btnNewWithSearchValues.setMnemonic('E');
		btnNewWithSearchValues.putClientProperty("hideActionText", Boolean.TRUE);

		btnSearchEditor.setName("btnSearchEditor");
		btnSearchEditor.setMnemonic('D');
		
	}

	/**
	 * @return the model of the edit view.
	 * @see #getEditView()
	 */
	@Override
	public SearchEditModel getEditModel() {
		return (SearchEditModel) this.getEditView().getModel();
	}

	/**
	 * Toggles the search editor.
	 * @param bVisible true: shows the search editor. false: shows the "edit component" containing the search fields.
	 */
	public void setSearchEditorVisible(boolean bVisible) {
		this.bSearchEditorVisible = bVisible;
		this.btnSearchEditor.setSelected(bVisible);
		this.cardlayout.show(pnlCards, bVisible ? CONSTRAINT_SEARCHEDITOR : CONSTRAINT_EDITCOMPONENT);
	}

	/**
	 * @return Is the search editor visible?
	 */
	public boolean isSearchEditorVisible() {
		return this.bSearchEditorVisible;
	}

	/**
	 * @return the search editor panel.
	 */
	public SearchEditorPanel getSearchEditorPanel() {
		return pnlSearchEditor;
	}

	/**
	 * @param cond May be <code>null</code>.
	 * @return Can the given search condition be displayed in the search fields?
	 */
	protected boolean canDisplayConditionInFields(CollectableSearchCondition cond) {
		return SearchConditionUtils.trueIfNull(cond).accept(new CanDisplayConditionInFieldsVisitor(this));
	}

	/**
	 * A panel laying a transparent component over a main component.
	 */
	private static class OverlayPanel extends JPanel {

		OverlayPanel(Component compTransparent, Component compMain) {
			this.setLayout(new OverlayLayout(this));
			this.add(compTransparent);
			this.add(compMain);
		}

		@Override
		public boolean isOptimizedDrawingEnabled() {
			// enable drawing of siblings, otherwise repaint doesn't work properly:
			return false;
		}
	}

	/**
	 * Visitor: Can a given search condition be displayed in the SearchPanel's fields?
	 */
	protected static class CanDisplayConditionInFieldsVisitor implements CollectableSearchCondition.Visitor<Boolean, RuntimeException>, CollectableSearchCondition.CompositeVisitor<Boolean, RuntimeException> {

		protected final SearchPanel searchpanel;

		protected CanDisplayConditionInFieldsVisitor(SearchPanel searchpanel) {
			this.searchpanel = searchpanel;
		}

		@Override
		public Boolean visitTrueCondition(TrueCondition truecond) throws RuntimeException {
			return true;
		}

		/**
		 * @param atomiccond
		 * @return true iff the field in the given condition is contained in this SearchPanel and all components with the
		 * condition's field name can display the given condition.
		 * @throws RuntimeException
		 */
		@Override
		public Boolean visitAtomicCondition(final AtomicCollectableSearchCondition atomiccond) throws RuntimeException {
			final Collection<CollectableComponent> collclctcomp = searchpanel.getEditView().getCollectableComponentsFor(atomiccond.getFieldName());
			return !collclctcomp.isEmpty() && CollectionUtils.forall(collclctcomp, new CanDisplay(atomiccond));
		}

		@Override
		@SuppressWarnings("deprecation")
		public Boolean visitCompositeCondition(CompositeCollectableSearchCondition compositecond) throws RuntimeException {
			return (compositecond.getLogicalOperator() == LogicalOperator.AND) &&
					CollectionUtils.forall(compositecond.getOperands(), new HasType(CollectableSearchCondition.TYPE_ATOMIC)) &&
					CollectionUtils.forall(compositecond.getOperands(), new CanDisplayConditionInFields(searchpanel)) &&
					SearchConditionUtils.areAtomicConditionsUnique(compositecond.getOperands());
		}

		@Override
		public Boolean visitIdCondition(CollectableIdCondition idcond) throws RuntimeException {
			return false;
		}

		@Override
		public Boolean visitSubCondition(CollectableSubCondition subcond) throws RuntimeException {
			return SearchConditionSubFormController.canSubConditionBeDisplayed(subcond);
		}

		@Override
		public Boolean visitReferencingCondition(ReferencingCollectableSearchCondition refcond) throws RuntimeException {
			return false;
		}

		@Override
		public Boolean visitSelfSubCondition(CollectableSelfSubCondition subcond) throws RuntimeException {
			return true;
		}		

		@Override
		public Boolean visitPlainSubCondition(PlainSubCondition subcond) throws RuntimeException {
			return false;
		}		

		protected class CanDisplayConditionInFields implements Predicate<CollectableSearchCondition> {

			private final SearchPanel searchpanel;

			protected CanDisplayConditionInFields(SearchPanel searchpanel) {
				this.searchpanel = searchpanel;
			}

			@Override
			public boolean evaluate(CollectableSearchCondition cond) {
				return searchpanel.canDisplayConditionInFields(cond);
			}
		}

		@Override
        public Boolean visitIdListCondition(CollectableIdListCondition collectableIdListCondition) throws RuntimeException {
	        return false;
        }

	}  // inner class CanDisplayConditionInFieldsVisitor

}  // class SearchPanel
