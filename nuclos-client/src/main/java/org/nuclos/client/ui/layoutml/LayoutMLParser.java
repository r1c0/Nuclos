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
package org.nuclos.client.ui.layoutml;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import org.nuclos.api.Property;
import org.nuclos.api.ui.LayoutComponent;
import org.nuclos.api.ui.LayoutComponentFactory;
import org.nuclos.client.common.NuclosDropTargetListener;
import org.nuclos.client.common.NuclosDropTargetVisitor;
import org.nuclos.client.common.Utils.CollectableLookupProvider;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.STATIC_BUTTON;
import org.nuclos.client.nuclet.NucletComponentRepository;
import org.nuclos.client.resource.ResourceCache;
import org.nuclos.client.ui.CommonJScrollPane;
import org.nuclos.client.ui.CommonJSeparator;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.JInfoTabbedPane;
import org.nuclos.client.ui.LayoutComponentHolder;
import org.nuclos.client.ui.LineLayout;
import org.nuclos.client.ui.SizeKnownListener;
import org.nuclos.client.ui.StrictSizeComponent;
import org.nuclos.client.ui.TitledSeparator;
import org.nuclos.client.ui.collect.Chart;
import org.nuclos.client.ui.collect.CollectableComponentsProvider;
import org.nuclos.client.ui.collect.DefaultCollectableComponentsProvider;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.component.CollectableComboBox;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.CollectableComponentFactory;
import org.nuclos.client.ui.collect.component.CollectableComponentType;
import org.nuclos.client.ui.collect.component.CollectableIdTextField;
import org.nuclos.client.ui.collect.component.CollectableListOfValues;
import org.nuclos.client.ui.collect.component.CollectableOptionGroup;
import org.nuclos.client.ui.collect.component.DelegatingCollectablePanel;
import org.nuclos.client.ui.collect.component.LabeledCollectableComponent;
import org.nuclos.client.ui.collect.component.LabeledCollectableComponentWithVLP;
import org.nuclos.client.ui.collect.component.LookupEvent;
import org.nuclos.client.ui.collect.component.LookupListener;
import org.nuclos.client.ui.collect.component.Parameterisable;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModel;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelAdapter;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelEvent;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelListener;
import org.nuclos.client.ui.collect.component.model.DetailsComponentModelEvent;
import org.nuclos.client.ui.collect.component.model.SearchComponentModel;
import org.nuclos.client.ui.collect.component.model.SearchComponentModelEvent;
import org.nuclos.client.ui.labeled.LabeledComponent;
import org.nuclos.client.ui.labeled.LabeledTextArea;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.NuclosScript;
import org.nuclos.common.TranslationMap;
import org.nuclos.common.caching.GenCache;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableComponentTypes;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableFieldsProviderFactory;
import org.nuclos.common.collect.collectable.CollectableUtils;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.collection.multimap.MultiListHashMap;
import org.nuclos.common.collection.multimap.MultiListMap;
import org.nuclos.common2.EntityAndFieldName;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.layoutml.exception.LayoutMLException;
import org.nuclos.common2.layoutml.exception.LayoutMLParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Parser for the LayoutML. Constructs a panel containing components for display/editing
 * from a LayoutML definition file.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
@Configurable
public class LayoutMLParser extends org.nuclos.common2.layoutml.LayoutMLParser {

	private static final Logger log = Logger.getLogger(LayoutMLParser.class.getName());

	/**
	 * parses the LayoutML definition in <code>inputsource</code> and builds a component out of it.
	 * @param inputsource the <code>InputSource</code> containing the LayoutML definition
	 * @param clcte the meta information about the objects to collect in this form
	 * @param bCreateSearchableComponents Create searchable components? (false: create editable components)
	 * @param actionlistenerButtons the common <code>ActionListener</code>, if any, for all button elements.
	 * @param valuelistproviderfactory the factory that creates providers for value lists in <code>CollectableComboBoxes</code>.
	 * @param collectableComponentFactory
	 * @return the <code>LayoutRoot</code> that contains the objects constructed from the LayoutML definition
	 * @throws LayoutMLParseException when a parse exception occurs.
	 * @throws LayoutMLException when a general exception occurs.
	 * @throws IOException when an I/O error occurs (rather fatal)
	 * @precondition clcte != null
	 */
	public LayoutRoot getResult(InputSource inputsource, CollectableEntity clcte, boolean bCreateSearchableComponents, ActionListener actionlistenerButtons,
			CollectableFieldsProviderFactory valuelistproviderfactory, CollectableComponentFactory collectableComponentFactory)
			throws LayoutMLException, IOException {

		if (clcte == null) {
			throw new NullArgumentException("clcte");
		}
		final BuildFormHandler handler = new BuildFormHandler(clcte, bCreateSearchableComponents, actionlistenerButtons, collectableComponentFactory,
				valuelistproviderfactory);
		try {
			this.parse(inputsource, handler);

			return new LayoutRoot(bCreateSearchableComponents, handler.getRootComponent(), handler.getCollectableComponentsProvider(),
					handler.getMapOfCollectableComponentModels(), handler.stack.getMapOfSubForms(), handler.getMultiMapOfDependencies(),
					handler.getInitialFocusEntityAndFieldName());
		}
		catch (SAXParseException ex) {
			throw new LayoutMLParseException(ex);
		}
		catch (SAXException ex) {
			throw new LayoutMLException(ex, handler.getDocumentLocator());
		}
	}

	/**
	 * inner class <code>BuildFormHandler</code>. The handler listens to the events that are issued by
	 * the underlying SAX parser.
	 */
	static class BuildFormHandler extends BasicHandler {

		private static class TabbedPaneConstraints implements LocalizationHandler {
			private String sTitle;
			private boolean bEnabled = true;
			private String sInternalname;
			private String sMnemonic;


			private TabbedPaneConstraints() {
			}

			@Override
			public void setTranslation(String translation) {
				this.sTitle = translation;
			}
		}

		private static class Event {

			Event() {
			}

			/** @todo encapsulate fields */
			String sType;
			String sSourceComponentName;
			SubForm subform;

			@Override
			public String toString() {
				final StringBuilder result = new StringBuilder();
				result.append("LayoutMLParser.Event[type=").append(sType);
				result.append(", src=").append(sSourceComponentName);
				result.append(", subform=").append(subform);
				result.append("]");
				return result.toString();
			}
		}

		private interface Action {
//			void performAction() throws SAXException;
		}

		private static class TargetComponentAction implements Action {

			private final String sTargetComponentName;

			private TargetComponentAction(String sTargetComponentName) {
				this.sTargetComponentName = sTargetComponentName;
			}

			public String getTargetComponentName() {
				return sTargetComponentName;
			}

			@Override
			public String toString() {
				final StringBuilder result = new StringBuilder();
				result.append("TargetComponentAction[");
				result.append("target=");
				result.append(sTargetComponentName);
				result.append("]");
				return result.toString();
			}
		}

		private static class ClearAction extends TargetComponentAction {

			private ClearAction(String sTargetComponentName) {
				super(sTargetComponentName);
			}

			@Override
			public String toString() {
				final StringBuilder result = new StringBuilder();
				result.append("ClearAction[");
				result.append("target=");
				result.append(getTargetComponentName());
				result.append("]");
				return result.toString();
			}
		}

		private static class TransferLookedUpValueAction extends TargetComponentAction {

			private final String sSourceFieldName;

			private TransferLookedUpValueAction(String sTargetComponentName, String sSourceFieldName) {
				super(sTargetComponentName);
				this.sSourceFieldName = sSourceFieldName;
			}

			public String getSourceFieldName() {
				return sSourceFieldName;
			}

			@Override
			public String toString() {
				final StringBuilder result = new StringBuilder();
				result.append("TransferLookedUpValueAction[");
				result.append("target=");
				result.append(getTargetComponentName());
				result.append("srcField=");
				result.append(sSourceFieldName);
				result.append("]");
				return result.toString();
			}
		}

		private static class RefreshValueListAction extends TargetComponentAction {

			private final String sTargetComponentEntityName;
			private final String sParameterNameForSourceComponent;

			private RefreshValueListAction(String sTargetComponentName, String sTargetComponentEntityName,
					String sParameterNameForSourceComponent) {
				super(sTargetComponentName);
				this.sTargetComponentEntityName = sTargetComponentEntityName;
				this.sParameterNameForSourceComponent = sParameterNameForSourceComponent;
			}

			public String getTargetComponentEntityName() {
				return sTargetComponentEntityName;
			}

			/**
			 * @return the name of the parameter in the valuelistprovider for the source component.
			 */
			public String getParameterNameForSourceComponent() {
				return sParameterNameForSourceComponent;
			}

			@Override
			public String toString() {
				final StringBuilder result = new StringBuilder();
				result.append("RefreshValueListAction[");
				result.append("target=");
				result.append(getTargetComponentName());
				result.append(",targetEntity=");
				result.append(sTargetComponentEntityName);
				result.append(",parameterNameSrc=");
				result.append(sParameterNameForSourceComponent);
				result.append("]");
				return result.toString();
			}
		}

		private static class LookupCollectableComponentModelListener implements CollectableComponentModelListener {

			private final GenCache<Object, Collectable> cache;

			private final CollectableComponentsProvider provider;

			private final List<Pair<String, CollectableComponentModel>> transfers;

			private final JComponent rootComponent;

			//

			private LookupCollectableComponentModelListener(GenCache<Object, Collectable> cache,
					CollectableComponentsProvider provider,
					List<Pair<String, CollectableComponentModel>> transfers,
					JComponent rootComponent) {
				this.cache = cache;
				this.provider = provider;
				this.transfers = transfers;
				this.rootComponent = rootComponent;
			}

			@Override
			public void valueToBeChanged(DetailsComponentModelEvent ev) {
			}

			@Override
			public void searchConditionChangedInModel(SearchComponentModelEvent ev) {
			}

			@Override
			public void collectableFieldChangedInModel(CollectableComponentModelEvent ev) {
				if (ev.collectableFieldHasChanged() && !ev.getCollectableComponentModel().isInitializing()) {
					Object id = ev.getNewValue().getValueId();
					Collectable clctSelected = null;
					try {
						clctSelected = cache.get(id);
						for (Pair<String, CollectableComponentModel> t : transfers) {
							transferValue(provider, clctSelected, t.getX(), t.getY());
						}
					} catch (Exception ex) {
						Errors.getInstance().showExceptionDialog(rootComponent, ex);
					}
				}
			}
		}

		private static class RuleLookupListener implements LookupListener {

			private final CollectableComponentsProvider provider;

			private final String sourceFieldName;

			private final CollectableComponentModel clctcompmodelSource;

			private final CollectableComponentModel clctcompmodelTarget;

			private RuleLookupListener(CollectableComponentsProvider provider, String sourceFieldName,
					CollectableComponentModel clctcompmodelSource, CollectableComponentModel clctcompmodelTarget) {
				this.provider = provider;
				this.sourceFieldName = sourceFieldName;
				this.clctcompmodelSource = clctcompmodelSource;
				this.clctcompmodelTarget = clctcompmodelTarget;
			}

			@Override
			public void lookupSuccessful(LookupEvent ev) {
				if (!clctcompmodelSource.isInitializing()) {
					transferValue(provider, ev.getSelectedCollectable(), sourceFieldName, clctcompmodelTarget);
				}
			}

			@Override
            public int getPriority() {
                return 1;
            }
		}

		private static class HandleClearLookupListener implements LookupListener {

			private final CollectableComponentModel clctcompmodelSource;

			private final CollectableComponentModel clctcompmodelTarget;

			private HandleClearLookupListener(
					CollectableComponentModel clctcompmodelSource, CollectableComponentModel clctcompmodelTarget) {
				this.clctcompmodelSource = clctcompmodelSource;
				this.clctcompmodelTarget = clctcompmodelTarget;
			}

			@Override
            public void lookupSuccessful(LookupEvent ev) {
				if (!clctcompmodelSource.isInitializing()) {
					clctcompmodelTarget.clear();
				}
			}

			@Override
            public int getPriority() {
                return 1;
            }
		}

		private class Rule {

			private Event event;

			private final Collection<Action> collActions = new LinkedList<Action>();

			Rule() {
			}

			@Override
			public String toString() {
				final StringBuilder result = new StringBuilder();
				result.append("LayoutMLParser.Rule[event=").append(event).append("]");
				return result.toString();
			}

			public void finish() throws SAXException {
				/** @todo q&d - refactor: make this oo! */

				if (event == null) {
					/** @todo Why is this necessary here? The SAX parser should validate this before. */
					throw new SAXException("Das rule-Element ben\u00f6tigt ein event-Element.");
				}

				if (event.subform != null) {
					if (event.sType.equals(ATTRIBUTEVALUE_LOOKUP)) {
						handleLookUpEventInSubForm();
					}
					else if (event.sType.equals(ATTRIBUTEVALUE_VALUECHANGED)) {
						handleValueChangedEventInSubForm();
					}
					else {
						throw new SAXException(StringUtils.getParameterizedExceptionMessage("LayoutMLParser.1", event.sType));//"Unbekannter Ereignistyp: " + event.sType);
					}
				}
				else {
					if (event.sType.equals(ATTRIBUTEVALUE_VALUECHANGED)) {
						handleValueChangedEvent();
					}
					else if (event.sType.equals(ATTRIBUTEVALUE_LOOKUP)) {
						handleLookupEvent();
					}
					else {
						throw new SAXException(StringUtils.getParameterizedExceptionMessage("LayoutMLParser.1", event.sType));//"Unbekannter Ereignistyp: " + event.sType);
					}
				}
			}	// finish

			private void handleLookUpEventInSubForm() throws SAXException {
				for (Action act : this.collActions) {
					if (act instanceof TransferLookedUpValueAction) {
						final TransferLookedUpValueAction transferaction = (TransferLookedUpValueAction) act;
						SubForm.Column subformcolumn = event.subform.getColumn(event.sSourceComponentName);
						if (subformcolumn == null) {
							subformcolumn = new SubForm.Column(event.sSourceComponentName);
							event.subform.addColumn(subformcolumn);
						}
						subformcolumn.addTransferLookedUpValueAction(new SubForm.TransferLookedUpValueAction(transferaction.getTargetComponentName(), transferaction.getSourceFieldName()));
					}	// TransferLookedUpValueAction
					else {
						throw new SAXException(StringUtils.getParameterizedExceptionMessage("LayoutMLParser.2"));//"Unbekannte Aktion f\u00fcr Unterformular.");
					}
				}	// for
			}

			private void handleValueChangedEventInSubForm() throws SAXException {
				for (Action action : collActions) {
					if (action instanceof RefreshValueListAction) {
						final RefreshValueListAction rpvact = (RefreshValueListAction) action;

						// if an entity is specified in refresh-possible-values, it must be equal to the entity of the event:
						if (rpvact.getTargetComponentEntityName() != null) {
							if (!rpvact.getTargetComponentEntityName().equals(event.subform.getEntityName())) {
								//throw new SAXException(StringUtils.getParameterizedExceptionMessage("LayoutMLParser.3", rpvact.getTargetComponentEntityName(), event.subform.getEntityName()));
									//"Entit\u00e4t in refresh-possible-values (" + rpvact.getTargetComponentEntityName() +
										//") stimmt nicht mit der Entit\u00e4t des event (" + event.subform.getEntityName() + ") \u00fcberein.");
							}
						}
						final String sTargetComponentName = rpvact.getTargetComponentName();
						SubForm targetSubform;
						if (rpvact.sTargetComponentEntityName == null) {
							targetSubform = event.subform;
						} else {
							targetSubform = stack.getSubFormForEntity(rpvact.sTargetComponentEntityName);
							if (targetSubform == null) {
								throw new SAXException(String.format("RefreshValueListAction Error: Entity %s not found!",	rpvact.sTargetComponentEntityName));
							}
						}

						SubForm.Column subformcolumn = targetSubform.getColumn(sTargetComponentName);
						if (subformcolumn == null) {
							subformcolumn = new SubForm.Column(sTargetComponentName);
							targetSubform.addColumn(subformcolumn);
						}
                        
						subformcolumn.addRefreshValueListAction(new SubForm.RefreshValueListAction(sTargetComponentName,
								event.subform.getEntityName(), event.sSourceComponentName, rpvact.getParameterNameForSourceComponent()));
					} else if (action instanceof ClearAction) {
						final ClearAction clract = (ClearAction) action;
						SubForm.Column subformcolumn = event.subform.getColumn(event.sSourceComponentName);
						if (subformcolumn == null) {
							subformcolumn = new SubForm.Column(event.sSourceComponentName);
							event.subform.addColumn(subformcolumn);
						}
						subformcolumn.addClearAction(new SubForm.ClearAction(clract.getTargetComponentName()));
					} else {
						throw new SAXException(StringUtils.getParameterizedExceptionMessage("LayoutMLParser.2"));//"Unbekannte Aktion f\u00fcr Unterformular.");
					}
				}	// for
			}

			private void handleLookupEvent() throws SAXException {
				// get the event's source components:
				final Collection<CollectableComponent> collclctcompSource = BuildFormHandler.this.getCollectableComponentsProvider().getCollectableComponentsFor(event.sSourceComponentName);
				if (collclctcompSource.isEmpty()) {
					throw new SAXException(StringUtils.getParameterizedExceptionMessage("LayoutMLParser.4", event.sSourceComponentName));
						//"Die Quellkomponente (f\u00fcr ein Lookup-Event) namens \"" + event.sSourceComponentName + "\" ist im Layout nicht vorhanden.");
				}
				for (final CollectableComponent clctcompSource : collclctcompSource) {
					if (clctcompSource instanceof CollectableListOfValues) {
						final CollectableListOfValues clctlovSource = (CollectableListOfValues) clctcompSource;
						for (Action act : this.collActions) {
							this.handleAction(act, clctlovSource);
						}
					}
					else if (clctcompSource instanceof CollectableComboBox) {
						if (bCreateSearchableComponents) {
							return; //NUCLOSINT-1160
						}
//							alle Filtern/Gruppieren, so nur TransferActions mit gleicher SourceComp
//							dann, pro Gruppe ein Listener
						// List of associated transfers actions as pair (sourceFieldName -> targetCollectableComponentModel)
						final List<Pair<String, CollectableComponentModel>> transfers = new ArrayList<Pair<String, CollectableComponentModel>>();
						for (final Action act : this.collActions) {
							if (act instanceof TransferLookedUpValueAction) {
								TransferLookedUpValueAction transferAct = (TransferLookedUpValueAction) act;
								final String sourceFieldName = transferAct.getSourceFieldName();
								final String targetComponentName = transferAct.getTargetComponentName();
								final CollectableComponentModel targetClctCompModel = mpclctcompmodel.get(targetComponentName);
								if (targetClctCompModel == null) {
									throw new SAXException(StringUtils.getParameterizedExceptionMessage("LayoutMLParser.5", targetComponentName));
										//"Zielkomponente ist nicht im Layout vorhanden: " + sTargetComponentName);
								}
								transfers.add(Pair.makePair(sourceFieldName, targetClctCompModel));
							}
						}

						if (transfers.size() > 0) {
							String referencedEntityName = clctcompSource.getEntityField().getReferencedEntityName();
							if (referencedEntityName != null) {
								final GenCache<Object, Collectable> cache = new GenCache<Object, Collectable>(
										new CollectableLookupProvider(clctcompSource.getEntityField().getEntityName(),
												clctcompSource.getEntityField().getName()));

								clctcompSource.getModel().addCollectableComponentModelListener(new LookupCollectableComponentModelListener(
										cache, getCollectableComponentsProvider(), transfers, getRootComponent()));
							}
						}
					}
					else {
						log.info("Die Quellkomponente (f\u00fcr ein Lookup-Event) namens \"" + event.sSourceComponentName + "\" ist keine Liste von Werten (LOV).");
					}
				}
			}

			private void handleAction(final Action act, final CollectableListOfValues clctlovSource) throws SAXException {
				if (act instanceof TransferLookedUpValueAction) {
					final TransferLookedUpValueAction transferaction = (TransferLookedUpValueAction) act;
					this.handleTransferLookedUpValueAction(transferaction, clctlovSource);
				}
				else if (act instanceof ClearAction) {
					final ClearAction clearaction = (ClearAction) act;
					handleClearAction(clearaction, clctlovSource);
				}
			}

			private void handleTransferLookedUpValueAction(final TransferLookedUpValueAction transferaction, final CollectableListOfValues clctlovSource) throws SAXException {
				if (bCreateSearchableComponents) {
					return; //NUCLOSINT-1160
				}
				final String sTargetComponentName = transferaction.getTargetComponentName();
				// Patch by Frank Pavlic (added by tp, verified by ts)
				final CollectableComponentModel clctcompmodelSource = clctlovSource.getModel();
				final CollectableComponentModel clctcompmodelTarget = mpclctcompmodel.get(sTargetComponentName);
				if (clctcompmodelTarget == null) {
					throw new SAXException(StringUtils.getParameterizedExceptionMessage("LayoutMLParser.5", sTargetComponentName));
						//"Zielkomponente ist nicht im Layout vorhanden: " + sTargetComponentName);
				}
				/** @todo removeLookupListener - but where/when? */
				clctlovSource.addLookupListener(new RuleLookupListener(
						getCollectableComponentsProvider(), transferaction.getSourceFieldName(),
						clctcompmodelSource, clctcompmodelTarget));
			}

			private void handleClearAction(final ClearAction clearaction, final CollectableListOfValues clctlovSource) throws SAXException {
				final String sTargetComponentName = clearaction.getTargetComponentName();
				final CollectableComponentModel clctcompmodelSource = clctlovSource.getModel();
				final CollectableComponentModel clctcompmodelTarget = mpclctcompmodel.get(sTargetComponentName);
				if (clctcompmodelTarget == null) {
					throw new SAXException(StringUtils.getParameterizedExceptionMessage("LayoutMLParser.5", sTargetComponentName));
						//"Zielkomponente ist nicht im Layout vorhanden: " + sTargetComponentName);
				}
				clctlovSource.addLookupListener(new HandleClearLookupListener(
						clctcompmodelSource, clctcompmodelTarget));
			}

			private void handleValueChangedEvent() throws SAXException {
				assert event.subform == null;

				// get the event's source component:
				final CollectableComponentModel clctcompmodelSource = mpclctcompmodel.get(event.sSourceComponentName);
				if (clctcompmodelSource == null) {
					throw new SAXException(StringUtils.getParameterizedExceptionMessage("LayoutMLParser.7", event.sSourceComponentName));
						//"Quellkomponente ist nicht im Layout vorhanden: " + event.sSourceComponentName);
				}
				for (Action act : collActions) {
					if (act instanceof ClearAction) {
						final ClearAction clearaction = (ClearAction) act;
						final String sTargetComponentName = clearaction.getTargetComponentName();

						// add a dependency for this rule so this needn't be specified manually:
						BuildFormHandler.this.addDependency(sTargetComponentName, event.sSourceComponentName);

						final CollectableComponentModel clctcompmodelTarget = mpclctcompmodel.get(sTargetComponentName);
						if (clctcompmodelTarget == null) {
							throw new SAXException(StringUtils.getParameterizedExceptionMessage("LayoutMLParser.5", sTargetComponentName));
								//"Zielkomponente ist nicht im Layout vorhanden: " + sTargetComponentName);
						}
						clctcompmodelSource.addCollectableComponentModelListener(new CollectableComponentModelAdapter() {
							@Override
							public void collectableFieldChangedInModel(CollectableComponentModelEvent ev) {
								/** @todo check if this is correct! */
								if (!ev.getCollectableComponentModel().isInitializing()) {
									clctcompmodelTarget.clear();
								}
							}
						});
					}
					else if (act instanceof RefreshValueListAction) {
						final RefreshValueListAction rpvact = (RefreshValueListAction) act;
						final String sTargetComponentName = rpvact.getTargetComponentName();
						final String sTargetComponentEntity = rpvact.getTargetComponentEntityName();
						if (sTargetComponentEntity != null) {
							// subform:
							final SubForm subform = stack.getSubFormForEntity(sTargetComponentEntity);
							if (subform == null) {
								/** @todo prepend the name of the rule/event/action in the error message */
								throw new SAXException(StringUtils.getParameterizedExceptionMessage("LayoutMLParser.8", sTargetComponentEntity));
									//"Unterformular f\u00fcr Entit\u00e4t " + sTargetComponentEntity + " nicht gefunden.");
							}
							SubForm.Column subformcolumn = subform.getColumn(sTargetComponentName);
							if (subformcolumn == null) {
								subformcolumn = new SubForm.Column(sTargetComponentName);
								subform.addColumn(subformcolumn);
							}
							subformcolumn.addRefreshValueListAction(new SubForm.RefreshValueListAction(sTargetComponentName,
									BuildFormHandler.this.getCollectableEntity().getName(), event.sSourceComponentName, rpvact.getParameterNameForSourceComponent()));
						}
						else {
							// regular form:
							final Collection<CollectableComponent> collclctcompTarget = BuildFormHandler.this.getCollectableComponentsProvider().getCollectableComponentsFor(sTargetComponentName);
							if (collclctcompTarget.isEmpty()) {
								throw new SAXException(StringUtils.getParameterizedExceptionMessage("LayoutMLParser.5", sTargetComponentName));
									//"Zielkomponente ist nicht im Layout vorhanden: " + sTargetComponentName);
							}

							// add a dependency for this rule so this needn't be specified manually:
							BuildFormHandler.this.addDependency(sTargetComponentName, event.sSourceComponentName);

							for (CollectableComponent clctcompTarget : collclctcompTarget) {
								if (!(clctcompTarget instanceof Parameterisable)) {
									log.info("Zielkomponente " + sTargetComponentName + " in action refresh-value-list muss eine parametrisierbare Komponente sein (Auswahlliste etc.).");
									return;
									// throw new SAXException("Zielkomponente muss eine Auswahlliste (Dropdown) sein.");
								}
								final Parameterisable clctParameterisableTarget = (Parameterisable) clctcompTarget;

								if(clctParameterisableTarget instanceof LabeledCollectableComponentWithVLP) {
									LabeledCollectableComponentWithVLP clctWithVLP = (LabeledCollectableComponentWithVLP) clctParameterisableTarget;
									// set a "dependant" value list provider if the combobox hasn't one yet:
									if (clctWithVLP.getValueListProvider() == null) {
										clctWithVLP.setValueListProvider(BuildFormHandler.this.valueListProviderFactory.newDependantCollectableFieldsProvider(
												null, clctWithVLP.getFieldName()));
									}
								}

								clctcompmodelSource.addCollectableComponentModelListener(
										new RefreshValueListCollectableComponentModelAdapter(event, clctcompmodelSource,
												clctParameterisableTarget, rpvact.getParameterNameForSourceComponent()));
							}	// for
						}
					}	// RefreshValueListAction
				}	// for
			}

		}	// inner class Rule

		private static class RefreshValueListCollectableComponentModelAdapter extends CollectableComponentModelAdapter {

			private final Event event;
			private final CollectableComponentModel clctcompmodelParent;
			private final Parameterisable clctParameterisable;
			private final String sParameterNameForSourceComponent;

			RefreshValueListCollectableComponentModelAdapter(Event event, CollectableComponentModel clctcompmodelParent, Parameterisable clctcmbbxTarget, String sParameterNameForSourceComponent) {
				this.event = event;
				this.clctcompmodelParent = clctcompmodelParent;
				this.clctParameterisable = clctcmbbxTarget;
				this.sParameterNameForSourceComponent = sParameterNameForSourceComponent;
			}

			@Override
			public void collectableFieldChangedInModel(CollectableComponentModelEvent ev) {
				try {
					final CollectableField field = clctcompmodelParent.getField();
					final Object oRelatedId;
					if (field.isIdField()) {
						oRelatedId = clctcompmodelParent.getField().getValueId();
					}
					else {
						oRelatedId = field.getValue();
					}
					clctParameterisable.setParameter(sParameterNameForSourceComponent, oRelatedId);
				}
				catch (UnsupportedOperationException ex) {
					// If this happens, there is nothing we can do about it:
					throw new CommonFatalException("set parameters [" + sParameterNameForSourceComponent + "] to " + clctParameterisable + " failed:\n"
							+ "parent model: " + clctcompmodelParent + " field: " + clctcompmodelParent.getFieldName()
							+ " on rule event " + event, ex);
				}
				try {
					log.debug("LayoutMLParser$BuildFormHandler$Rule.collectableFieldChangedInModel: refreshValueList()");
					clctParameterisable.applyParameters();
				}
				catch (CommonBusinessException ex) {
					// If this happens, there is nothing we can do about it:
					throw new CommonFatalException("apply parameters [" + sParameterNameForSourceComponent + "] to " + clctParameterisable
							+ " failed on rule event " + event, ex);
				}
			}
		}	// inner class RefreshValueListCollectableComponentModelAdapter

		/**
		 * Transfers looked-up values.
		 */
		private static void transferValue(CollectableComponentsProvider provider,
				Collectable clctSelected, String sourceFieldName, CollectableComponentModel clctcompmodelTarget) {

			if (clctcompmodelTarget instanceof SearchComponentModel) {
				return; //NUCLOSINT-1160
			}
			if (clctSelected == null) {
				clctcompmodelTarget.clear();
			}
			else {
				final CollectableField clctfSource = clctSelected.getField(sourceFieldName);

				// create a new CollectableField based on the target entity field type:
				final CollectableEntityField clctefTarget = clctcompmodelTarget.getEntityField();
				final CollectableField clctfTarget = CollectableUtils.copyCollectableField(clctfSource, clctefTarget, false);
				if (clctfTarget != null) {
					clctcompmodelTarget.setField(clctfTarget);
				} else {
					if (clctfSource.isNull()) {
						// This is a special case we can always allow:
						clctcompmodelTarget.clear();
					}
					else if (clctefTarget.isIdField() && !clctfSource.isIdField()) {
						// if the target field already has a valueId use this one
						if(clctcompmodelTarget.getField().getValueId() != null) {
							clctcompmodelTarget.setField(new CollectableValueIdField(clctcompmodelTarget.getField().getValueId(), clctfSource.getValue()));
						} else {
							// if the source field does not have a value id and the target field requires a value id,
							// try to match the value:
							/** @todo this is definitely a workaround - the valuelist provider should probably move from
							 * CollectableComboBox to CollectableComponentModel - but this can't be done at this time. 09.09.2004 CR */
							try {
								final Collection<CollectableComponent> collclctcompTarget = provider.getCollectableComponentsFor(clctefTarget.getName());
								assert !clctfSource.isNull();
								
								final CollectableComponent clctcomp = collclctcompTarget.iterator().next();
								if (clctcomp instanceof CollectableComboBox) {
									final CollectableComboBox clctcmbbx = (CollectableComboBox) clctcomp;
									try {
										clctcompmodelTarget.setField(CollectableUtils.findCollectableFieldByValue(clctcmbbx.getValueList(), clctfSource));
									} catch (NoSuchElementException e) {
										if (clctcmbbx.isInsertable())
											clctcompmodelTarget.setField(new CollectableValueIdField(null, clctfSource.getValue()));
									}
								} else if (clctcomp instanceof CollectableListOfValues) {
									final CollectableListOfValues clctlov = (CollectableListOfValues) clctcomp;
									if (clctlov.isInsertable())
										clctcompmodelTarget.setField(new CollectableValueIdField(null, clctfSource.getValue()));
								}
							}
							catch (Exception ex2) {
								throw new CommonFatalException(StringUtils.getParameterizedExceptionMessage("LayoutMLParser.6", clctefTarget.getName()), ex2);
							}
						}
					} else {
						throw new CommonFatalException("Cannot transfer field " + clctefTarget.getName());
					}
				}
			}
		}

		private final ComponentBuilderStack stack = new ComponentBuilderStack();

		/** @todo comment */
		private final ActionListener alButtons;

		/** @todo comment */
		private final CollectableFieldsProviderFactory valueListProviderFactory;

		/**
		 * the current rule, if any.
		 * @todo use stack?
		 */
		private Rule rule;

		/**
		 * the current subform column, if any.
		 * @todo use stack?
		 */
		private SubForm.Column subformcolumn;

		/**
		 * the current valuelistprovider, if any.
		 * @todo use stack?
		 */
		private CollectableFieldsProvider valuelistprovider;

		/** the target (component) of the localization; if not set the current component builder is used.. */
		private LocalizationHandler localizationHandler;

		/** the current translations (language -> translation), if any. */
		private Map<String, String> translations;


		private CollectableComponentFactory clctcompfactory;

		/**
		 * the root component
		 */
		private JComponent compRoot;

		/**
		 * the chars that are collected between start and end tags.
		 */
		private StringBuffer sbChars;

		/**
		 * the (optional) <code>CollectableEntity</code> that defines the structure for
		 * <code>CollectableFields</code> in the LayoutML
		 */
		private final CollectableEntity clcte;

		/**
		 * create searchable <code>CollectableComponent</code>s?
		 */
		private final boolean bCreateSearchableComponents;

		/**
		 * maps elements to <code>ElementProcessor</code>s
		 */
		private final Map<String, ElementProcessor> mpElementProcessors = new HashMap<String, ElementProcessor>();

		/**
		 * maps field names to (multiple) <code>CollectableComponent</code>s
		 */
		private final DefaultCollectableComponentsProvider clctcompprovider = new DefaultCollectableComponentsProvider();

		/**
		 * maps field names to <code>CollectableComponentModel</code>s.
		 */
		private final Map<String, CollectableComponentModel> mpclctcompmodel = new HashMap<String, CollectableComponentModel>();

		/**
		 * contains the parsed dependencies
		 */
		private final MultiListMap<String, String> mmpDependencies = new MultiListHashMap<String, String>();

		/**
		 * maps XML attribute values (e.g. <code>ATTRIBUTEVALUE_TEXTFIELD</code>)
		 * to control types (e.g. <code>TYPE_TEXTFIELD</code>)
		 */
		private final Map<String, Integer> mpEnumeratedControlTypes = new HashMap<String, Integer>(5);

		private final Map<String, Integer> mpFlowLayoutConstants = new HashMap<String, Integer>(5);
		private final Map<String, String> mpBorderLayoutConstraints = new HashMap<String, String>(5);
		private final Map<String, Integer> mpTabLayoutPolicies = new HashMap<String, Integer>(2);
		private final Map<String, Integer> mpTabPlacementConstants = new HashMap<String, Integer>(4);
		private final Map<String, Integer> mpGridBagConstraintAnchor = new HashMap<String, Integer>(10);
		private final Map<String, Integer> mpGridBagConstraintFill = new HashMap<String, Integer>(4);
		private final Map<String, Integer> mpHorizontalScrollBarPolicies = new HashMap<String, Integer>(3);
		private final Map<String, Integer> mpVerticalScrollBarPolicies = new HashMap<String, Integer>(3);
		private final Map<String, String> mpSplitPaneConstraints = new HashMap<String, String>(4);
		private final Map<String, Integer> mpSplitPaneOrientation = new HashMap<String, Integer>(2);
		private final Map<String, Integer> mpLineLayoutOrientation = new HashMap<String, Integer>(2);
		private final Map<String, Integer> mpSwingConstantsOrientation = new HashMap<String, Integer>(2);
		private final Map<String, Integer> mpSeparatorOrientation;
		private final Map<String, Integer> mpToolBarOrientation = new HashMap<String, Integer>(3);
		private final Map<String, Integer> mpScrollPane = new HashMap<String, Integer>(4);
		private final Map<String, Integer> mpSwingConstantsScrollpane = new HashMap<String, Integer>(3);

		private EntityAndFieldName eafnInitialFocus;

		private String sInitialSortingColumnName;
		private String sInitialSortingOrder;

		/**
		 * initializes the maps.
		 * @param clcte the optional <code>CollectableEntity</code> that defines the structure of
		 * collectable-component elements
		 * @param bCreateSearchableComponents
		 * @param alButtons the common <code>ActionListener</code>, if any, for all button elements.
		 * @param valueListProviderFactory
		 * @precondition clcte != null
		 */
		private BuildFormHandler(CollectableEntity clcte, boolean bCreateSearchableComponents, ActionListener alButtons,
				CollectableComponentFactory collectableComponentFactory, CollectableFieldsProviderFactory valueListProviderFactory) {
			this.clcte = clcte;
			this.bCreateSearchableComponents = bCreateSearchableComponents;
			this.alButtons = alButtons;
			this.valueListProviderFactory = valueListProviderFactory;
			this.clctcompfactory = collectableComponentFactory;

			this.setupElementProcessors();

			// control types:
			this.mpEnumeratedControlTypes.put(ATTRIBUTEVALUE_TEXTFIELD, CollectableComponentTypes.TYPE_TEXTFIELD);
			this.mpEnumeratedControlTypes.put(ATTRIBUTEVALUE_IDTEXTFIELD, CollectableComponentTypes.TYPE_IDTEXTFIELD);
			this.mpEnumeratedControlTypes.put(ATTRIBUTEVALUE_TEXTAREA, CollectableComponentTypes.TYPE_TEXTAREA);
			this.mpEnumeratedControlTypes.put(ATTRIBUTEVALUE_COMBOBOX, CollectableComponentTypes.TYPE_COMBOBOX);
			this.mpEnumeratedControlTypes.put(ATTRIBUTEVALUE_CHECKBOX, CollectableComponentTypes.TYPE_CHECKBOX);
			this.mpEnumeratedControlTypes.put(ATTRIBUTEVALUE_DATECHOOSER, CollectableComponentTypes.TYPE_DATECHOOSER);
			this.mpEnumeratedControlTypes.put(ATTRIBUTEVALUE_HYPERLINK, CollectableComponentTypes.TYPE_HYPERLINK);
			this.mpEnumeratedControlTypes.put(ATTRIBUTEVALUE_EMAIL, CollectableComponentTypes.TYPE_EMAIL);
			this.mpEnumeratedControlTypes.put(ATTRIBUTEVALUE_OPTIONGROUP, CollectableComponentTypes.TYPE_OPTIONGROUP);
			this.mpEnumeratedControlTypes.put(ATTRIBUTEVALUE_LISTOFVALUES, CollectableComponentTypes.TYPE_LISTOFVALUES);
			this.mpEnumeratedControlTypes.put(ATTRIBUTEVALUE_FILECHOOSER, CollectableComponentTypes.TYPE_FILECHOOSER);
			this.mpEnumeratedControlTypes.put(ATTRIBUTEVALUE_IMAGE, CollectableComponentTypes.TYPE_IMAGE);
			//NUCLEUSINT-1142
			this.mpEnumeratedControlTypes.put(ATTRIBUTEVALUE_PASSWORDFIELD, CollectableComponentTypes.TYPE_PASSWORDFIELD);

			// flow layoutml constants:
			this.mpFlowLayoutConstants.put("left", FlowLayout.LEFT);
			this.mpFlowLayoutConstants.put("right", FlowLayout.RIGHT);
			this.mpFlowLayoutConstants.put("leading", FlowLayout.LEADING);
			this.mpFlowLayoutConstants.put("trailing", FlowLayout.TRAILING);
			this.mpFlowLayoutConstants.put("center", FlowLayout.CENTER);

			// border layout constants:
			this.mpBorderLayoutConstraints.put("north", BorderLayout.NORTH);
			this.mpBorderLayoutConstraints.put("south", BorderLayout.SOUTH);
			this.mpBorderLayoutConstraints.put("west", BorderLayout.WEST);
			this.mpBorderLayoutConstraints.put("east", BorderLayout.EAST);
			this.mpBorderLayoutConstraints.put("center", BorderLayout.CENTER);

			// tab layoutml policies:
			this.mpTabLayoutPolicies.put("scroll", JTabbedPane.SCROLL_TAB_LAYOUT);
			this.mpTabLayoutPolicies.put("wrap", JTabbedPane.WRAP_TAB_LAYOUT);

			// tab placement constants:
			this.mpTabPlacementConstants.put("top", JTabbedPane.TOP);
			this.mpTabPlacementConstants.put("bottom", JTabbedPane.BOTTOM);
			this.mpTabPlacementConstants.put("left", JTabbedPane.LEFT);
			this.mpTabPlacementConstants.put("right", JTabbedPane.RIGHT);

			// horizontal scrollbar policies:
			this.mpHorizontalScrollBarPolicies.put("always", JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			this.mpHorizontalScrollBarPolicies.put("never", JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			this.mpHorizontalScrollBarPolicies.put("asneeded", JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

			// vertical scrollbar policies:
			this.mpVerticalScrollBarPolicies.put("always", JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			this.mpVerticalScrollBarPolicies.put("never", JScrollPane.VERTICAL_SCROLLBAR_NEVER);
			this.mpVerticalScrollBarPolicies.put("asneeded", JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

			// constants for GridBagConstraints "anchor" parameter (absolute values only):
			this.mpGridBagConstraintAnchor.put("north", GridBagConstraints.NORTH);
			this.mpGridBagConstraintAnchor.put("northeast", GridBagConstraints.NORTHEAST);
			this.mpGridBagConstraintAnchor.put("east", GridBagConstraints.EAST);
			this.mpGridBagConstraintAnchor.put("southeast", GridBagConstraints.SOUTHEAST);
			this.mpGridBagConstraintAnchor.put("south", GridBagConstraints.SOUTH);
			this.mpGridBagConstraintAnchor.put("southwest", GridBagConstraints.SOUTHWEST);
			this.mpGridBagConstraintAnchor.put("west", GridBagConstraints.WEST);
			this.mpGridBagConstraintAnchor.put("northwest", GridBagConstraints.NORTHWEST);
			this.mpGridBagConstraintAnchor.put("center", GridBagConstraints.CENTER);

			// constants for GridBagConstraints "fill" parameter:
			this.mpGridBagConstraintFill.put("none", GridBagConstraints.NONE);
			this.mpGridBagConstraintFill.put("horizontal", GridBagConstraints.HORIZONTAL);
			this.mpGridBagConstraintFill.put("vertical", GridBagConstraints.VERTICAL);
			this.mpGridBagConstraintFill.put("both", GridBagConstraints.BOTH);

			this.mpSplitPaneConstraints.put("top", JSplitPane.TOP);
			this.mpSplitPaneConstraints.put("bottom", JSplitPane.BOTTOM);
			this.mpSplitPaneConstraints.put("left", JSplitPane.LEFT);
			this.mpSplitPaneConstraints.put("right", JSplitPane.RIGHT);

			this.mpSplitPaneOrientation.put("horizontal", JSplitPane.HORIZONTAL_SPLIT);
			this.mpSplitPaneOrientation.put("vertical", JSplitPane.VERTICAL_SPLIT);

			// constants for LineLayout "orientation" parameter:
			this.mpLineLayoutOrientation.put("horizontal", LineLayout.HORIZONTAL);
			this.mpLineLayoutOrientation.put("vertical", LineLayout.VERTICAL);

			// constants for Separator "orientation" parameter:
			this.mpSwingConstantsOrientation.put("horizontal", SwingConstants.HORIZONTAL);
			this.mpSwingConstantsOrientation.put("vertical", SwingConstants.VERTICAL);

			this.mpSeparatorOrientation = this.mpSwingConstantsOrientation;
			this.mpToolBarOrientation.putAll(mpSwingConstantsOrientation);
			this.mpToolBarOrientation.put("hide", -1);

			// constants for Separator "scrollpane" parameter:
			this.mpSwingConstantsScrollpane.put("horizontal", 0);
			this.mpSwingConstantsScrollpane.put("vertical", 1);
			this.mpSwingConstantsScrollpane.put("both", 2);

			this.mpScrollPane.putAll(mpSwingConstantsScrollpane);
			this.mpScrollPane.put("none", -1);
		}

		private void setupElementProcessors() {
			// element processors:
			this.mpElementProcessors.put(ELEMENT_PANEL, new PanelElementProcessor());
			this.mpElementProcessors.put(ELEMENT_EMPTYPANEL, new PanelElementProcessor());
			this.mpElementProcessors.put(ELEMENT_TABBEDPANE, new TabbedPaneElementProcessor());
			this.mpElementProcessors.put(ELEMENT_SCROLLPANE, new ScrollPaneElementProcessor());
			this.mpElementProcessors.put(ELEMENT_SPLITPANE, new SplitPaneElementProcessor());
			this.mpElementProcessors.put(ELEMENT_SUBFORM, new SubFormElementProcessor());
			this.mpElementProcessors.put(ELEMENT_SUBFORMCOLUMN, new SubFormColumnElementProcessor());
			this.mpElementProcessors.put(ELEMENT_CHART, new ChartElementProcessor());
			this.mpElementProcessors.put(ELEMENT_LABEL, new LabelElementProcessor());
			this.mpElementProcessors.put(ELEMENT_IMAGE, new ImageElementProcessor());
			this.mpElementProcessors.put(ELEMENT_TEXTFIELD, new TextFieldElementProcessor());
			this.mpElementProcessors.put(ELEMENT_TEXTAREA, new TextAreaElementProcessor());
			this.mpElementProcessors.put(ELEMENT_COMBOBOX, new ComboBoxElementProcessor());
			this.mpElementProcessors.put(ELEMENT_BUTTON, new ButtonElementProcessor());
			this.mpElementProcessors.put(ELEMENT_COLLECTABLECOMPONENT, new CollectableComponentElementProcessor());
			this.mpElementProcessors.put(ELEMENT_DESCRIPTION, new DescriptionElementProcessor());
			this.mpElementProcessors.put(ELEMENT_BORDERLAYOUT, new BorderLayoutElementProcessor());
			this.mpElementProcessors.put(ELEMENT_FLOWLAYOUT, new FlowLayoutElementProcessor());
			this.mpElementProcessors.put(ELEMENT_GRIDLAYOUT, new GridLayoutElementProcessor());
			this.mpElementProcessors.put(ELEMENT_GRIDBAGLAYOUT, new GridBagLayoutElementProcessor());
			this.mpElementProcessors.put(ELEMENT_TABLELAYOUT, new TableLayoutElementProcessor());
			this.mpElementProcessors.put(ELEMENT_BOXLAYOUT, new BoxLayoutElementProcessor());
			this.mpElementProcessors.put(ELEMENT_ROWLAYOUT, new RowLayoutElementProcessor());
			this.mpElementProcessors.put(ELEMENT_COLUMNLAYOUT, new ColumnLayoutElementProcessor());
			this.mpElementProcessors.put(ELEMENT_BORDERLAYOUTCONSTRAINTS, new BorderLayoutConstraintsElementProcessor());
			this.mpElementProcessors.put(ELEMENT_TABBEDPANECONSTRAINTS, new TabbedPaneConstraintsElementProcessor());
			this.mpElementProcessors.put(ELEMENT_SPLITPANECONSTRAINTS, new SplitPaneConstraintsElementProcessor());
			this.mpElementProcessors.put(ELEMENT_GRIDBAGCONSTRAINTS, new GridBagConstraintsElementProcessor());
			this.mpElementProcessors.put(ELEMENT_TABLELAYOUTCONSTRAINTS, new TableLayoutConstraintsElementProcessor());
			this.mpElementProcessors.put(ELEMENT_MINIMUMSIZE, new MinimumSizeElementProcessor());
			this.mpElementProcessors.put(ELEMENT_PREFERREDSIZE, new PreferredSizeElementProcessor());
			this.mpElementProcessors.put(ELEMENT_STRICTSIZE, new StrictSizeElementProcessor());
			this.mpElementProcessors.put(ELEMENT_CLEARBORDER, new ClearBorderElementProcessor());
			this.mpElementProcessors.put(ELEMENT_EMPTYBORDER, new EmptyBorderElementProcessor());
			this.mpElementProcessors.put(ELEMENT_ETCHEDBORDER, new EtchedBorderElementProcessor());
			this.mpElementProcessors.put(ELEMENT_BEVELBORDER, new BevelBorderElementProcessor());
			this.mpElementProcessors.put(ELEMENT_LINEBORDER, new LineBorderElementProcessor());
			this.mpElementProcessors.put(ELEMENT_TITLEDBORDER, new TitledBorderElementProcessor());
			this.mpElementProcessors.put(ELEMENT_FONT, new FontElementProcessor());
			this.mpElementProcessors.put(ELEMENT_BACKGROUND, new BackgroundElementProcessor());
			this.mpElementProcessors.put(ELEMENT_SEPARATOR, new SeparatorElementProcessor());
			this.mpElementProcessors.put(ELEMENT_TITLEDSEPARATOR, new TitledSeparatorElementProcessor());
			this.mpElementProcessors.put(ELEMENT_OPTIONS, new OptionsElementProcessor());
			this.mpElementProcessors.put(ELEMENT_OPTION, new OptionElementProcessor());
			this.mpElementProcessors.put(ELEMENT_RULE, new RuleElementProcessor());
			this.mpElementProcessors.put(ELEMENT_EVENT, new EventElementProcessor());
			this.mpElementProcessors.put(ELEMENT_TRANSFERLOOKEDUPVALUE, new TransferLookedUpValueElementProcessor());
			this.mpElementProcessors.put(ELEMENT_CLEAR, new ClearElementProcessor());
			this.mpElementProcessors.put(ELEMENT_REFRESHVALUELIST, new RefreshValueListElementProcessor());
			this.mpElementProcessors.put(ELEMENT_DEPENDENCY, new DependencyElementProcessor());
			this.mpElementProcessors.put(ELEMENT_VALUELISTPROVIDER, new ValueListProviderElementProcessor());
			this.mpElementProcessors.put(ELEMENT_PARAMETER, new ParameterElementProcessor());

			this.mpElementProcessors.put(ELEMENT_PROPERTY, new PropertyElementProcessor());
			this.mpElementProcessors.put(ELEMENT_PROPERTY_SIZE, new PropertySizeElementProcessor());
			this.mpElementProcessors.put(ELEMENT_PROPERTY_COLOR, new PropertyColorElementProcessor());
			this.mpElementProcessors.put(ELEMENT_PROPERTY_FONT, new PropertyFontElementProcessor());
			this.mpElementProcessors.put(ELEMENT_PROPERTY_SCRIPT, new PropertyScriptElementProcessor());
			this.mpElementProcessors.put(ELEMENT_PROPERTY_TRANSLATIONS, new PropertyTranslationsElementProcessor());
			this.mpElementProcessors.put(ELEMENT_PROPERTY_VALUELIST_PROVIDER, new PropertyValuelistProviderElementProcessor());

			this.mpElementProcessors.put(ELEMENT_INITIALFOCUSCOMPONENT, new InitialFocusComponentProcessor());
			this.mpElementProcessors.put(ELEMENT_INITIALSORTINGORDER, new InitialSortingOrderProcessor());
			this.mpElementProcessors.put(ELEMENT_TRANSLATIONS, new TranslationsElementProcessor());
			this.mpElementProcessors.put(ELEMENT_TRANSLATION, new TranslationElementProcessor());
			this.mpElementProcessors.put(ELEMENT_LAYOUTCOMPONENT, new LayoutComponentElementProcessor());

			this.mpElementProcessors.put(ELEMENT_ENABLED, new ScriptElementProcessor());
			this.mpElementProcessors.put(ELEMENT_NEW_ENABLED, new ScriptElementProcessor());
			this.mpElementProcessors.put(ELEMENT_EDIT_ENABLED, new ScriptElementProcessor());
			this.mpElementProcessors.put(ELEMENT_DELETE_ENABLED, new ScriptElementProcessor());
			this.mpElementProcessors.put(ELEMENT_CLONE_ENABLED, new ScriptElementProcessor());
		}

		/**
		 * event: start document. Called by the underlying SAX parser.
		 * @throws SAXException
		 */
		@Override
		public void startDocument() throws SAXException {
			this.compRoot = null;
		}

		/**
		 * event: end document. Called by the underlying SAX parser.
		 * @throws SAXException
		 */
		@Override
		public void endDocument() throws SAXException {
			this.sbChars = null;

			this.setDefaultValueListProviders();
			try {
				this.refreshValueListsInComboBoxes();
			}
			catch (CommonBusinessException ex) {
				throw new SAXException("LayoutMLParser.9", ex);//"Beim F\u00fcllen der Dropdowns ist ein Fehler aufgetreten.", ex);
			}
		}

		/**
		 * sets the default value list provider for all comboboxes that don't have a provider yet.
		 */
		private void setDefaultValueListProviders() {
			for (CollectableComponent clctcomp : this.getCollectableComponentsProvider().getCollectableComponents()) {
				if (clctcomp instanceof LabeledCollectableComponentWithVLP) {
					final LabeledCollectableComponentWithVLP clctWithVLP = (LabeledCollectableComponentWithVLP) clctcomp;
					final String sEntity = BuildFormHandler.this.getCollectableEntity().getName();
					assert sEntity != null;
					if (clctWithVLP.getValueListProvider() == null) {
						clctWithVLP.setValueListProvider(this.valueListProviderFactory.newDefaultCollectableFieldsProvider(sEntity, clctcomp.getFieldName()));
					}
				} else {
					/*if (clctcomp instanceof DelegatingCollectableComboBox) {
						final DelegatingCollectableComboBox clctcmbbx = (DelegatingCollectableComboBox) clctcomp;
						final String sEntity = BuildFormHandler.this.getCollectableEntity().getName();
						assert sEntity != null;
						if (clctcmbbx.getValueListProvider() == null) {
							clctcmbbx.setValueListProvider(this.valueListProviderFactory.newDefaultCollectableFieldsProvider(sEntity, clctcomp.getFieldName()));
						}
					} else {*/
						if (clctcomp instanceof DelegatingCollectablePanel) {
							final DelegatingCollectablePanel clctpnl = (DelegatingCollectablePanel) clctcomp;
							final String sEntity = BuildFormHandler.this.getCollectableEntity().getName();
							assert sEntity != null;
							if (clctpnl.needValueListProvider() && clctpnl.getValueListProvider() == null) {
								clctpnl.setValueListProvider(this.valueListProviderFactory.newDefaultCollectableFieldsProvider(sEntity, clctcomp.getFieldName()));
							}
						}
					//}
				}
			}
		}

		private void refreshValueListsInComboBoxes() throws CommonBusinessException {
			for (CollectableComponent clctcomp : this.getCollectableComponentsProvider().getCollectableComponents()) {
				if (clctcomp instanceof CollectableComboBox) {
					final CollectableComboBox clctcmbbx = (CollectableComboBox) clctcomp;
					clctcmbbx.refreshValueList(true);
				} else {
					/*if (clctcomp instanceof DelegatingCollectableComboBox) {
						final DelegatingCollectableComboBox clctcmbbx = (DelegatingCollectableComboBox) clctcomp;
						clctcmbbx.refreshValueList();
					} else {*/
						if (clctcomp instanceof DelegatingCollectablePanel) {
							final DelegatingCollectablePanel clctpnl = (DelegatingCollectablePanel) clctcomp;
							if (clctpnl.needValueListProvider()) {
								clctpnl.refreshValueList();
							}
						}
					//}
				}
			}
		}

		/**
		 * @return the (optional) <code>CollectableEntity</code> that defines the structure of
		 * collectable-component elements used in the parsed LayoutML document
		 * @postcondition result != null
		 */
		CollectableEntity getCollectableEntity() {
			return this.clcte;
		}

		/**
		 * @return the root component constructed from the LayoutML
		 */
		JComponent getRootComponent() {
			return this.compRoot;
		}

		/**
		 * provides access to the map of collectable components
		 * @return the map of parsed/constructed collectable components.
		 * Maps field names to <code>CollectableComponent</code>s
		 */
		DefaultCollectableComponentsProvider getCollectableComponentsProvider() {
			return this.clctcompprovider;
		}

		Map<String, CollectableComponentModel> getMapOfCollectableComponentModels() {
			return this.mpclctcompmodel;
		}

		/**
		 * @return the entity name and field name of the component, if any, that is to get the focus initially.
		 */
		public EntityAndFieldName getInitialFocusEntityAndFieldName() {
			return this.eafnInitialFocus;
		}

		/**
		 * @return the map of inter-field dependencies
		 */
		MultiListMap<String, String> getMultiMapOfDependencies() {
			return this.mmpDependencies;
		}

		/**
		 * @param sControlType
		 * @param sControlTypeClass
		 * @return the CollectableComponentType for the given parameters.
		 * @throws SAXException
		 */
		private CollectableComponentType getCollectableComponentType(String sControlType, String sControlTypeClass)
				throws SAXException {
			final Integer iEnumeratedControlType;
			final Class<CollectableComponent> clsclctcomp;
			if (sControlTypeClass == null) {
				iEnumeratedControlType = mpEnumeratedControlTypes.get(sControlType);
				clsclctcomp = null;
			}
			else {
				if (sControlType != null) {
					throw new SAXException("LayoutMLParser.10");//"Die Attribute controltype und controltypeclass k\u00f6nnen nicht gleichzeitig angegeben werden.");
				}
				try {
					iEnumeratedControlType = null;
					/** @todo explicitly check that the class implements CollectableComponent */
					clsclctcomp = (Class<CollectableComponent>) Class.forName(sControlTypeClass);
				}
				catch (ClassNotFoundException ex) {
					final String sMessage = StringUtils.getParameterizedExceptionMessage("LayoutMLParser.11", sControlTypeClass);//"Unbekannte Klasse: " + sControlTypeClass;
					throw new SAXException(sMessage, ex);
				}
			}
			return new CollectableComponentType(iEnumeratedControlType, clsclctcomp);
		}

		/**
		 * Handles stored resource ids for legacy reasons. The localization target
		 * must already be set.
		 */
		private void handleLegacyResourceId(Attributes attributes) {
			final String resourceId = attributes.getValue(ATTRIBUTE_LOCALERESOURCEID);
			if (resourceId != null) {
				String text = SpringLocaleDelegate.getInstance().getTextForStaticLabel(resourceId);
				getStaticLocalizationHandler().setTranslation(text);
			}
		}

		private LocalizationHandler getStaticLocalizationHandler() {
			if (localizationHandler != null)
				return localizationHandler;
			return stack.peekComponentBuilder();
		}

		/**
		 * inner class <code>ComponentBuilder</code>. Builds a regular <code>JComponent</code>.
		 */
		static class DefaultComponentBuilder implements ComponentBuilder {

			private JComponent comp;
			private Object oConstraints;
			private String translation;

			/**
			 * @param comp the component to build
			 * @postcondition this.getComponent() == comp
			 */
			DefaultComponentBuilder(JComponent comp) {
				this.comp = comp;
			}

			/**
			 * @param oConstraints the constraints to use when adding the internal component to its parent.
			 * @postcondition this.getConstraints() == oConstraints
			 */
			@Override
			public void setConstraints(Object oConstraints) {
				this.oConstraints = oConstraints;
			}

			/**
			 * adds <code>comp</code> to the internal component (as in <code>getComponent()</code>),
			 * using the given constraints
			 * @param comp
			 * @param oConstraints
			 */
			@Override
			public void add(JComponent comp, Object oConstraints) {
				this.comp.add(comp, oConstraints);
			}

			/**
			 * @param comp
			 * @return the name (if any) and class name of the given component
			 */
			private static String getComponentNameAndClassName(JComponent comp) {
				final StringBuffer sb = new StringBuffer();
				final String sName = comp.getName();
				if (!StringUtils.isNullOrEmpty(sName)) {
					sb.append('"');
					sb.append(sName);
					sb.append('"');
					sb.append(' ');
				}
				sb.append("(").append(SpringLocaleDelegate.getInstance().getMessage(
						"LayoutMLParser.12", "Klasse")).append(": ");
				sb.append(comp.getClass().getName());
				sb.append(')');
				return sb.toString();
			}

			/**
			 * @return the internal component (the component to build)
			 */
			@Override
			public JComponent getComponent() {
				return this.comp;
			}

			/**
			 * @return the constraints to use when adding the internal component to its parent.
			 */
			@Override
			public Object getConstraints() {
				return this.oConstraints;
			}

			/** Gets the translation to use (instead of the original text). */
			@Override
			public String getTranslation() {
				return translation;
			}

			/** Sets the translation to use. */
			@Override
            public void setTranslation(String translation) {
				this.translation = translation;
			}

			/**
			 * finishes the internal component
			 * @param cbParent
			 */
			@Override
			public void finish(ComponentBuilder cbParent) throws SAXException {
				try {
					// add component to parent:
					cbParent.add(getComponent(), getConstraints());
				}
				catch (Exception ex) {
					final String sMessage = StringUtils.getParameterizedExceptionMessage("LayoutMLParser.13",
						getComponentNameAndClassName(comp), getComponentNameAndClassName(cbParent.getComponent()), oConstraints);
					throw new SAXException(sMessage, ex);
				}
			}
		}	// inner class ComponentBuilder

		/**
		 * inner class <code>CollectableComponentBuilder</code>. Builds a <code>CollectableComponent</code>.
		 * @todo this class is not really needed - only for the options mechanism (was: restrictValues mechanism).
		 */
		static class CollectableComponentBuilder extends DefaultComponentBuilder {
			private final CollectableComponent clctcomp;

			/**
			 * contains options for option group
			 */
			private List<String[]> lstOptions;

			/**
			 * contains default value for option group
			 */
			private String sDefaultOption;

			/**
			 * @param clctcomp the collectable component to build
			 * @postcondition this.getComponent() == clctcomp
			 */
			CollectableComponentBuilder(CollectableComponent clctcomp) {
				super(clctcomp.getJComponent());
				this.clctcomp = clctcomp;
			}

			/**
			 * @return the internal component (the component to build)
			 */
			public CollectableComponent getCollectableComponent() {
				return this.clctcomp;
			}

			/**
			 * Adds an option to a collectable component (option group)
			 * @param sValue
			 * @param sLabel
			 * @param sMnemonic
			 */
			public String[] addOption(String sValue, String sLabel, String sMnemonic) {
				if (lstOptions == null) {
					lstOptions = new ArrayList<String[]>();
				}
				final String[] asOptions = new String[3];
				asOptions[0] = sValue;
				asOptions[1] = sLabel;
				asOptions[2] = StringUtils.emptyIfNull(sMnemonic);
				lstOptions.add(asOptions);
				return asOptions;
			}

			public void setDefaultOption(String sValue) {
				this.sDefaultOption = sValue;
			}

			@Override
			public void finish(ComponentBuilder cbParent) throws SAXException {
				super.finish(cbParent);

				/** @todo Finally we got rid of values - now we have options here... */
				if (this.getCollectableComponent() instanceof CollectableOptionGroup) {
					final CollectableOptionGroup group = (CollectableOptionGroup) this.getCollectableComponent();
					if (lstOptions != null) {
						group.setOptions(lstOptions);
					}
					group.setDefaultOption(sDefaultOption);
				}
			}

		}	// inner class CollectableComponentBuilder

		/**
		 * inner class <code>TabbedPaneBuilder</code>. Builds a <code>JInfoTabbedPane</code>.
		 */
		static class TabbedPaneBuilder extends DefaultComponentBuilder implements NuclosDropTargetVisitor {

			private SubForm previousSubForm;

			private int previousTab = -1;

			TabbedPaneBuilder(JInfoTabbedPane tbdpane) {
				super(tbdpane);
			}

			/**
			 * adds <code>comp</code> as a tab, using the given constraints
			 * @param comp
			 * @param oConstraints the String to appear on the tab
			 * @precondition oConstraints != null
			 */
			@Override
			public void add(JComponent comp, Object oConstraints) {
				final TabbedPaneConstraints tbdpaneconstraints = (TabbedPaneConstraints) oConstraints;
				final JInfoTabbedPane tbdpn = (JInfoTabbedPane) getComponent();
				tbdpn.addTab(tbdpaneconstraints.sTitle, comp);
				final int index = tbdpn.indexOfComponent(comp);
				tbdpn.setEnabledAt(index, tbdpaneconstraints.bEnabled);
				if(tbdpaneconstraints.sMnemonic != null) {
					tbdpn.setMnemonicAt(index, Integer.parseInt(tbdpaneconstraints.sMnemonic));
				}
				final JComponent jcomp = (JComponent) tbdpn.getTabComponentAt(index);
				setupDragDrop(jcomp);
				if(tbdpaneconstraints.sInternalname != null)
					comp.setName(tbdpaneconstraints.sInternalname);
			}

			protected void setupDragDrop(final JComponent comp) {
				NuclosDropTargetListener listener = new NuclosDropTargetListener(this);
				DropTarget drop = new DropTarget(comp, listener);
				drop.setActive(true);
			}

			@Override
			public void visitDragEnter(DropTargetDragEvent dtde) {}

			@Override
			public void visitDragExit(DropTargetEvent dte) {}

			@Override
			public void visitDragOver(DropTargetDragEvent dtde) {
				final DropTarget target = (DropTarget)dtde.getSource();
				final JLabel label = (JLabel) target.getComponent();
				final JInfoTabbedPane tbdpn = (JInfoTabbedPane) getComponent();
				final int index = tbdpn.indexOfTabComponent(label);
				tbdpn.setSelectedIndex(index);
				dtde.rejectDrag();
			}

			@Override
			public void visitDrop(DropTargetDropEvent dtde) {}

			@Override
			public void visitDropActionChanged(DropTargetDragEvent dtde) {}

			/**
			 * NUCLOSINT-63: connect the subform with the corresponding tab via the SizeKnownListener.
			 */
			public void addSubForm(SubForm subform) {
				final JInfoTabbedPane tbdpn = (JInfoTabbedPane) getComponent();
				final int tabs = tbdpn.getTabCount();
				// Only add the listener to the subform if there is no previous
				// subform in this tab.
				if (previousTab != tabs) {
					previousSubForm = null;
					previousTab = tabs;
					subform.setSizeKnownListener(new SizeKnownListener(tbdpn, tabs));
				}
				// If there is a second subform in this tab remove the listener
				// from the first subform.
				else if (previousSubForm != null) {
					previousSubForm.setSizeKnownListener(null);
					previousSubForm = null;
				}
			}

		}	// inner class TabbedPaneBuilder

		/**
		 * inner class <code>ScrollPaneBuilder</code>. Builds a <code>JScrollPane</code>.
		 */
		static class ScrollPaneBuilder extends DefaultComponentBuilder {
			private JScrollPane scrlpn;

			ScrollPaneBuilder(JScrollPane tbdpane) {
				super(tbdpane);
				this.scrlpn = tbdpane;
			}

			/**
			 * adds <code>comp</code> to the scrollpane's viewport.
			 * @param comp
			 * @param constraints are ignored
			 */
			@Override
			public void add(JComponent comp, Object constraints) {
				this.scrlpn.getViewport().add(comp);
			}
		}	// inner class ScrollPaneBuilder

		/**
		 * convenience method: gets an int value from an XML attribute.
		 * @param attributes the XML attributes of an XML element
		 * @param sAttributeName the name of the attribute to query
		 * @param iDefault the default value that is taken if there is no attribute with the given name
		 * @return the int value of the attribute or the default, if no matching attribute was found
		 */
		private static int getIntValue(Attributes attributes, String sAttributeName, int iDefault) throws SAXException {
			final String sValue = attributes.getValue(sAttributeName);
			try {
				return (sValue == null) ? iDefault : Integer.parseInt(sValue);
			}
			catch (NumberFormatException ex) {
				throw new SAXException(StringUtils.getParameterizedExceptionMessage("LayoutMLParser.14", sAttributeName));
					//"Ganze Zahl erwartet f\u00fcr Attribut \"" + sAttributeName + "\".");
			}
		}

		/**
		 * convenience method: gets an Integer value from an XML attribute.
		 * @param attributes the XML attributes of an XML element
		 * @param sAttributeName the name of the attribute to query
		 * @return the Integer value of the attribute or null, if no matching attribute was found
		 */
		private static Integer getIntegerValue(Attributes attributes, String sAttributeName) throws SAXException {
			final String sValue = attributes.getValue(sAttributeName);
			try {
				return (sValue == null) ? null : new Integer(sValue);
			}
			catch (NumberFormatException ex) {
				throw new SAXException(StringUtils.getParameterizedExceptionMessage("LayoutMLParser.14", sAttributeName));
					//"Ganze Zahl erwartet f\u00fcr Attribut \"" + sAttributeName + "\".");
			}
		}

		/**
		 * convenience method: gets a double value from an XML attribute.
		 * @param attributes the XML attributes of an XML element
		 * @param sAttributeName the name of the attribute to query
		 * @param dDefault the default value that is taken if there is no attribute with the given name
		 * @return the double value of the attribute or the default, if no matching attribute was found
		 */
		private static double getDoubleValue(Attributes attributes, String sAttributeName, double dDefault)
				throws SAXException {
			final String sValue = attributes.getValue(sAttributeName);
			try {
				return (sValue == null) ? dDefault : Double.parseDouble(sValue);
			}
			catch (NumberFormatException ex) {
				throw new SAXException(StringUtils.getParameterizedExceptionMessage("LayoutMLParser.15", sAttributeName));
					//"Dezimalzahl erwartet f\u00fcr Attribut \"" + sAttributeName + "\".");
			}
		}

		/**
		 * adds a border to the top component in the stack. The old border, if any, is preserved
		 * as inner border.
		 * @param borderNew
		 */
		private void addBorder(final Border borderNew) {
			final JComponent comp = stack.peekComponent();
			final Border borderOld = comp.getBorder();
			if (borderOld == null) {
				comp.setBorder(borderNew);
			}
			else {
				comp.setBorder(BorderFactory.createCompoundBorder(borderNew, borderOld));
			}
		}

		/**
		 * adds a dependency to the multimap of dependencies.
		 * @param sDependantFieldName
		 * @param sDependsOnFieldName
		 */
		private void addDependency(final String sDependantFieldName, final String sDependsOnFieldName) {
			/** @todo Do we have to check redundant/contradictory entries? */
			BuildFormHandler.this.mmpDependencies.addValue(sDependantFieldName, sDependsOnFieldName);
		}

		/**
		 * inner interface <code>ElementProcessor</code>. Processes an XML element.
		 */
		private interface ElementProcessor {
			/**
			 * is called when a start element event occurs in the underlying SAX parser.
			 * @param sUriNameSpace
			 * @param sSimpleName
			 * @param sQualifiedName
			 * @param attributes the XML attributes of this element
			 */
			void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException;

			/**
			 * called when an end element event occurs in the underlying SAX parser
			 * @param sUriNameSpace
			 * @param sSimpleName
			 * @param sQualifiedName
			 */
			void endElement(String sUriNameSpace, String sSimpleName, String sQualifiedName) throws SAXException;
		}

		/**
		 * inner class <code>AbstractElementProcessor</code>. Useful for the most element processors.
		 */
		private abstract class AbstractElementProcessor implements ElementProcessor {
			/**
			 * default action for most element processors: do nothing on end element events.
			 * @param sUriNameSpace
			 * @param sSimpleName
			 * @param sQualifiedName
			 */
			@Override
            public void endElement(String sUriNameSpace, String sSimpleName, String sQualifiedName) {
				// do nothing
			}
		}

		/**
		 * inner class <code>ComponentElementProcessor</code>.
		 * Useful for element processors that contain a component.
		 */
		private abstract class ComponentElementProcessor implements ElementProcessor {
			/**
			 * default action for component processors: <code>finishComponent()</code>
			 * @param sUriNameSpace
			 * @param sSimpleName
			 * @param sQualifiedName
			 */
			@Override
            public void endElement(String sUriNameSpace, String sSimpleName, String sQualifiedName) throws SAXException {
				compRoot = stack.finishComponent();
			}
		}

		/**
		 * inner class <code>CollectableComponentElementProcessor</code>.
		 */
		private class CollectableComponentElementProcessor extends ComponentElementProcessor {
			/**
			 * <ul>
			 *   <li>constructs a <code>CollectableComponent</code></li>
			 *   <li>adds it to the map of collectable components</li>
			 *   <li>configures it according to the XML attributes</li>
			 *   <li>and pushes it on the stack.</li>
			 * </ul>
			 * @param sUriNameSpace
			 * @param sSimpleName
			 * @param sQualifiedName
			 * @param attributes
			 */
			@Override
			public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {
				final String sFieldName = attributes.getValue(ATTRIBUTE_NAME);
				final String sControlType = attributes.getValue(ATTRIBUTE_CONTROLTYPE);
				final String sControlTypeClass = attributes.getValue(ATTRIBUTE_CONTROLTYPECLASS);

				final CollectableEntity clcte = BuildFormHandler.this.getCollectableEntity();
				final CollectableEntityField clctef = clcte.getEntityField(sFieldName);

				// Automatische Analyse f\u00fcr die Konfiguration des Attributes: "Werteliste (explizit)" und "Anzeigen in: Combobox editierbar"
				/*
				boolean isEditableComboBoxValueList = !clctef.isReferencing() && (clctef.getFieldType() == CollectableEntityField.TYPE_VALUEIDFIELD);
				if(isEditableComboBoxValueList && sControlTypeClass == null && sControlType != null && sControlType.equals(LayoutMLConstants.ELEMENT_COMBOBOX)){
					sControlType = null;
					sControlTypeClass = "de.novabit.elisa.client.common.CollectableComboBoxWithTextArea"; //provisorisch fest. wird beim Einsatz konfigurierbar gemacht.
				}
				*/

				final CollectableComponentType clctcomptype =
						BuildFormHandler.this.getCollectableComponentType(sControlType, sControlTypeClass);

				final CollectableComponent clctcomp = clctcompfactory.newCollectableComponent(clcte, sFieldName,
						clctcomptype, bCreateSearchableComponents);

				/** @todo add preferences to clctcomp */

				// If there is a CollectableComponentModel already with this fieldname, reuse it (share it):
				final CollectableComponentModel clctcompmodel = BuildFormHandler.this.mpclctcompmodel.get(sFieldName);
				if (clctcompmodel != null) {
						clctcomp.setModel(clctcompmodel);
				}

				BuildFormHandler.this.mpclctcompmodel.put(sFieldName, clctcomp.getModel());

				//NUCLEUSINT-442
				if ((!ATTRIBUTE_CONTROLTYPE.equals(ATTRIBUTEVALUE_TEXTFIELD)) && (!ATTRIBUTEVALUE_LABEL.equals(attributes.getValue(ATTRIBUTE_SHOWONLY)))) {
					BuildFormHandler.this.clctcompprovider.addCollectableComponent(clctcomp);
				} else {
					BuildFormHandler.this.clctcompprovider.addCollectableLabel(clctcomp);
				}

				// enabled:
				final String sEnabled = attributes.getValue(ATTRIBUTE_ENABLED);
				if (sEnabled != null) {
					// override default:
					final boolean bEnabled = sEnabled.equals(ATTRIBUTEVALUE_YES);
					clctcomp.setEnabled(bEnabled);
				}

				// Id text fields may never be enabled except in search mode!
				if (clctcomp instanceof CollectableIdTextField && !BuildFormHandler.this.bCreateSearchableComponents) {
					clctcomp.setEnabled(false);
				}

				// insertable:
				// The default setting is taken care of by the CollectableComponentFactory.
				// It can be overridden in the LayoutML though.
				final String sInsertable = attributes.getValue(ATTRIBUTE_INSERTABLE);
				if (!BuildFormHandler.this.bCreateSearchableComponents && sInsertable != null) {
					// override:
					// NUCLOSINT-442 only disable, but never allow to insert a new value! We need a new concept for insertable...
					clctcomp.setInsertable(sInsertable.equals(ATTRIBUTEVALUE_YES));
					if (sInsertable.equals(ATTRIBUTEVALUE_NO)) {
						clctcomp.setInsertable(false);
					}
				}

				final String sScalable = attributes.getValue(ATTRIBUTE_SCALABLE);
				if(sScalable != null) {
					if(sScalable.equals(ATTRIBUTEVALUE_YES)) {
						clctcomp.setScalable(true);
					}
				}

				final String sKeepAspectRatio = attributes.getValue(ATTRIBUTE_ASPECTRATIO);
				if(sKeepAspectRatio != null) {
					if(sKeepAspectRatio.equals(ATTRIBUTEVALUE_YES)) {
						clctcomp.setKeepAspectRatio(true);
					}
				}

				// next focus component
				final String sNextFocusComponent = attributes.getValue(ATTRIBUTE_NEXTFOCUSCOMPONENT);
				if(sNextFocusComponent != null) {
					clctcomp.setNextFocusComponent(sNextFocusComponent);
					clctcomp.getControlComponent().putClientProperty(ATTRIBUTE_NEXTFOCUSCOMPONENT, sNextFocusComponent);
					clctcomp.getJComponent().putClientProperty(ATTRIBUTE_NEXTFOCUSCOMPONENT, sNextFocusComponent);

				}

				// visible:
				boolean bVisible = true;
				final String sVisible = attributes.getValue(ATTRIBUTE_VISIBLE);
				if (sVisible != null) {
					// override default:
					bVisible = sVisible.equals(ATTRIBUTEVALUE_YES);
					clctcomp.setVisible(bVisible);
				}

				boolean bHideLabel = false;
				if (bVisible) {
					// show-only:
					boolean bHideControl = false;
					boolean bShowBrowseButtonOnly = false;
					final String sShowOnly = attributes.getValue(ATTRIBUTE_SHOWONLY);
					if (sShowOnly != null) {
						if (sShowOnly.equals(ATTRIBUTEVALUE_LABEL)) {
							bHideControl = true;
						}
						else if (sShowOnly.equals(ATTRIBUTEVALUE_CONTROL)) {
							bHideLabel = true;
						}
						else if (sShowOnly.equals(ATTRIBUTEVALUE_BROWSEBUTTON)) {
							bShowBrowseButtonOnly = true;
							bHideLabel = true;
						}
					}
					if (clctcomp instanceof LabeledCollectableComponent) {
						final LabeledComponent labcomp = ((LabeledCollectableComponent) clctcomp).getLabeledComponent();
						labcomp.getJLabel().setVisible(!bHideLabel);
						labcomp.getControlComponent().setVisible(!bHideControl);

						if (bShowBrowseButtonOnly && (clctcomp instanceof CollectableListOfValues)) {
							final CollectableListOfValues clctlov = (CollectableListOfValues) clctcomp;
							clctlov.setBrowseButtonVisibleOnly(true);
						}
					}

					if (clctcomp instanceof DelegatingCollectablePanel) {
						final DelegatingCollectablePanel delpnl = (DelegatingCollectablePanel) clctcomp;
						delpnl.setVisibleLabel(!bHideLabel);
						delpnl.setVisibleControl(!bHideControl);
					}
				}	// if (bVisible)

				final boolean hasLabel = !bHideLabel;
				if (hasLabel) {
					String label = clctef.getLabel();
					clctcomp.setLabelText(label);
				}
				localizationHandler = new LocalizationHandler() {
					@Override
					public void setTranslation(String text) {
						if (hasLabel)
							clctcomp.setLabelText(text);
					}
				};

				// fill-control-horizontally:
				final String sFillControlHorizontally = attributes.getValue(ATTRIBUTE_FILLCONTROLHORIZONTALLY);
				if (sFillControlHorizontally != null) {
					clctcomp.setFillControlHorizontally(sFillControlHorizontally.equals(ATTRIBUTEVALUE_YES));
				}

				// rows:
				final Integer iRows = getIntegerValue(attributes, ATTRIBUTE_ROWS);
				if (iRows != null) {
					clctcomp.setRows(iRows);
				}

				// columns:
				Integer iColumns = getIntegerValue(attributes, ATTRIBUTE_COLUMNS);
				if (iColumns == null) {
					iColumns = clctef.getMaxLength();
				}
				if (iColumns != null) {
					clctcomp.setColumns(iColumns);
				}

				// mnemonic:
				final String sMnemonic = attributes.getValue(ATTRIBUTE_MNEMONIC);
				if (sMnemonic != null && sMnemonic.length() > 0) {
					clctcomp.setMnemonic(sMnemonic.charAt(0));
				}

				clctcomp.setToolTipText(clctef.getDescription());
				// set default tooltip from the field's description
				// may be overwritten by LayoutML description element

				// opaqueness / transparency:
				final String sOpaque = attributes.getValue(ATTRIBUTE_OPAQUE);
				if (sOpaque != null) {
					final boolean bOpaque = sOpaque.equals(ATTRIBUTEVALUE_YES);
					clctcomp.setOpaque(bOpaque);
				}

//				// constraining entity:
//				final String sConstrainingEntity = attributes.getValue(ATTRIBUTE_CONSTRAININGENTITY);
//				clctcomp.setConstrainingEntity(sConstrainingEntity);

				stack.addCollectableComponent(clctcomp);

				// The component that is parsed first gets the initial focus by default:
				if (BuildFormHandler.this.eafnInitialFocus == null) {
					// only if the initial focus hasn't been set already:
					if ((!ATTRIBUTE_CONTROLTYPE.equals(ATTRIBUTEVALUE_TEXTFIELD)) && (!ATTRIBUTEVALUE_LABEL.equals(attributes.getValue(ATTRIBUTE_SHOWONLY)))) {
						//NUCLEUSINT-442
						BuildFormHandler.this.eafnInitialFocus = new EntityAndFieldName((String) null, sFieldName);
					}
				}

				handleLegacyResourceId(attributes);
			}

			@Override
			public void endElement(String sUriNameSpace, String sSimpleName, String sQualifiedName) throws SAXException {
				localizationHandler = null;
				super.endElement(sUriNameSpace, sSimpleName, sQualifiedName);
			}

		}	// inner class CollectableComponentElementProcessor

		/**
		 * inner class <code>InitialFocusComponentProcessor</code>. Processes a initial-focus-component element.
		 */
		private class InitialFocusComponentProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes) {
				final String sEntityName = attributes.getValue(ATTRIBUTE_ENTITY);
				final String sFieldName = attributes.getValue(ATTRIBUTE_NAME);
				BuildFormHandler.this.eafnInitialFocus = new EntityAndFieldName(sEntityName, sFieldName);
			}
		} // inner class InitialFocusComponentProcessor

		/**
		 * inner class <code>InitialFocusComponentProcessor</code>. Processes a initial-focus-component element.
		 */
		private class InitialSortingOrderProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes) {
				final String sName = attributes.getValue(ATTRIBUTE_NAME);
				final String sOrder = attributes.getValue(ATTRIBUTE_SORTINGORDER);
				BuildFormHandler.this.setInitialSortingColumnName(sName);
				BuildFormHandler.this.setInitialSortingOrder(sOrder);
			}

			@Override
			public void endElement(String sUriNameSpace, String sSimpleName, String sQualifiedName) {
				final SubForm subform = (SubForm) stack.peekComponent();
				subform.setInitialSortingOrder(BuildFormHandler.this.getInitialSortingColumnName(), BuildFormHandler.this.getInitialSortingOrder());
			}
		} // inner class InitialFocusComponentProcessor

		private String getInitialSortingOrder() {
			return this.sInitialSortingOrder;
		}

		private String getInitialSortingColumnName() {
			return this.sInitialSortingColumnName;
		}

		private void setInitialSortingOrder(String sOrder) {
			this.sInitialSortingOrder = sOrder;
		}

		private void setInitialSortingColumnName(String sSortingColumnName) {
			this.sInitialSortingColumnName = sSortingColumnName;
		}

		/**
		 * inner class <code>PanelElementProcessor</code>. Processes a panel element.
		 */
		private class PanelElementProcessor extends ComponentElementProcessor {
			/**
			 * constructs a <code>JPanel</code>, configures it according to the XML attributes
			 * and pushes it on the stack.
			 * @param sUriNameSpace
			 * @param sSimpleName
			 * @param sQualifiedName
			 * @param attributes
			 */
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes) {
				final JPanel pnl = new JPanel();

				// name:
				final String sName = attributes.getValue(ATTRIBUTE_NAME);
				if (sName != null) {
					pnl.setName(sName);
				}

				// visible:
				final String sVisible = attributes.getValue(ATTRIBUTE_VISIBLE);
				if (sVisible != null) {
					// override default:
					final boolean bVisible = sVisible.equals(ATTRIBUTEVALUE_YES);
					pnl.setVisible(bVisible);
				}

				// opaque:
				final String sOpaque = attributes.getValue(ATTRIBUTE_OPAQUE);
				final boolean bOpaque = (sOpaque != null && sOpaque.equals(ATTRIBUTEVALUE_YES));
				pnl.setOpaque(bOpaque);

				stack.addComponent(pnl);
			}
		}	// inner class PanelElementProcessor

		/**
		 * inner class <code>TabbedPaneElementProcessor</code>. Processes a tabbedpane element.
		 */
		private class TabbedPaneElementProcessor extends ComponentElementProcessor {
			/**
			 * constructs a <code>JInfoTabbedPane</code>, configures it according to the XML attributes
			 * and pushes it on the stack.
			 * @param sUriNameSpace
			 * @param sSimpleName
			 * @param sQualifiedName
			 * @param attributes
			 */
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes) {
				//final JInfoTabbedPane tbdpn = new JInfoTabbedPane();
				final JInfoTabbedPane tbdpn = new JInfoTabbedPane();
				final String sName = attributes.getValue(ATTRIBUTE_NAME);
				if (sName != null) {
					tbdpn.setName(sName);
				}

				final String sTabLayoutPolicy = attributes.getValue(ATTRIBUTE_TABLAYOUTPOLICY);
				if (sTabLayoutPolicy != null) {
					tbdpn.setTabLayoutPolicy(BuildFormHandler.this.mpTabLayoutPolicies.get(sTabLayoutPolicy));
				}

				final String sTabPlacement = attributes.getValue(ATTRIBUTE_TABPLACEMENT);
				if (sTabPlacement != null) {
					tbdpn.setTabPlacement(BuildFormHandler.this.mpTabPlacementConstants.get(sTabPlacement));
				}

				stack.addTabbedPane(tbdpn);
			}

		}	// inner class TabbedPaneElementProcessor

		/**
		 * inner class <code>SplitPaneElementProcessor</code>. Processes a splitpane element.
		 */
		private class SplitPaneElementProcessor extends ComponentElementProcessor {
			/**
			 * constructs a <code>JSplitPane</code>, configures it according to the XML attributes
			 * and pushes it on the stack.
			 * @param sUriNameSpace
			 * @param sSimpleName
			 * @param sQualifiedName
			 * @param attributes
			 */
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes) {
				final JSplitPane splitpn = new JSplitPane();

				final String sOrientation = attributes.getValue(ATTRIBUTE_ORIENTATION);
				final String sDividerSize = attributes.getValue(ATTRIBUTE_DIVIDERSIZE);
				final String sResizeWeight = attributes.getValue(ATTRIBUTE_RESIZEWEIGHT);
				final String sExpandable = attributes.getValue(ATTRIBUTE_EXPANDABLE);
				final String sContinuousLayout = attributes.getValue(ATTRIBUTE_CONTINUOUSLAYOUT);
				final String sName = attributes.getValue(ATTRIBUTE_NAME);

				splitpn.setOrientation(BuildFormHandler.this.mpSplitPaneOrientation.get(sOrientation));

				if (sDividerSize != null) {
					splitpn.setDividerSize(Integer.parseInt(sDividerSize));
				}
				if (sResizeWeight != null) {
					splitpn.setResizeWeight(Double.parseDouble(sResizeWeight));
				}
				if (sExpandable != null) {
					splitpn.setOneTouchExpandable(sExpandable.equals(ATTRIBUTEVALUE_YES));
				}
				if (sContinuousLayout != null) {
					splitpn.setContinuousLayout(sContinuousLayout.equals(ATTRIBUTEVALUE_YES));
				}
				if (sName != null) {
					splitpn.setName(sName);
				}

				stack.addComponent(splitpn);
			}
		}	// inner class SplitPaneElementProcessor

		/**
		 * inner class <code>SubFormElementProcessor</code>. Processes a subform element.
		 */
		private class SubFormElementProcessor extends ComponentElementProcessor {

			private SubForm subform;

			/**
			 * constructs a <code>SubForm</code>, configures it according to the XML attributes
			 * and pushes it on the stack.
			 * @param sUriNameSpace
			 * @param sSimpleName
			 * @param sQualifiedName
			 * @param attributes
			 */
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {

				final String sEntityName = attributes.getValue(ATTRIBUTE_ENTITY);
				// entity is a required attribute:
				assert sEntityName != null;

				// An attribute "visible" is not a good idea as inserting the component in a tab
				// implicitly sets visible to false. This is true for all components, not only subforms.

				// enabled:
				boolean bEnabled = true;
				final String sEnabled = attributes.getValue(ATTRIBUTE_ENABLED);
				if (sEnabled != null && !bCreateSearchableComponents) {
					// override default:
					bEnabled = sEnabled.equals(ATTRIBUTEVALUE_YES);
				}

				// toolbar orientation:
				Integer iOrientation = null;
				final String sOrientation = attributes.getValue(ATTRIBUTE_TOOLBARORIENTATION);
				if (sOrientation != null) {
					iOrientation = BuildFormHandler.this.mpToolBarOrientation.get(sOrientation);
				}

				// field referencing parent entity:
				final String sForeignKeyFieldToParent = attributes.getValue(ATTRIBUTE_FOREIGNKEYFIELDTOPARENT);

				subform = new SubForm(sEntityName, LangUtils.defaultIfNull(iOrientation, JToolBar.HORIZONTAL),
						sForeignKeyFieldToParent);

				subform.setEnabled(bEnabled || bCreateSearchableComponents);

				final String sControllerType = attributes.getValue(ATTRIBUTE_CONTROLLERTYPE);
				subform.setControllerType(sControllerType);

				final String sUniqueMasterColumnName = attributes.getValue(ATTRIBUTE_UNIQUEMASTERCOLUMN);
				subform.setUniqueMasterColumnName(sUniqueMasterColumnName);

				// referenced parent subform
				final String sParentSubForm = attributes.getValue(ATTRIBUTE_PARENTSUBFORM);
				subform.setParentSubForm(sParentSubForm);

				// dynamic cell heights default:
				final String sDynamicCellHeightsDefault = attributes.getValue(ATTRIBUTE_DYNAMIC_CELL_HEIGHTS_DEFAULT);
				if (sDynamicCellHeightsDefault != null && !bCreateSearchableComponents) {
					// override default:
					if (sDynamicCellHeightsDefault.equals(ATTRIBUTEVALUE_YES)) {
						subform.setDynamicRowHeightsDefault();
					}
				}

				stack.addSubForm(sEntityName, subform);
			}


		}	// inner class SubFormElementProcessor

		/**
		 * inner class <code>SubFormColumnElementProcessor</code>. Processes a subform element.
		 */
		private class SubFormColumnElementProcessor extends AbstractElementProcessor {
			/**
			 * constructs a <code>SubForm.Column</code>, configures it according to the XML attributes
			 * and adds it to the <code>SubForm</code> on top of the stack.
			 * @param sUriNameSpace
			 * @param sSimpleName
			 * @param sQualifiedName
			 * @param attributes
			 */
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {

				final String sName = attributes.getValue(ATTRIBUTE_NAME);

				String sLabel = attributes.getValue(ATTRIBUTE_LABEL);

				final String sControlType = attributes.getValue(ATTRIBUTE_CONTROLTYPE);
				final String sControlTypeClass = attributes.getValue(ATTRIBUTE_CONTROLTYPECLASS);

				final CollectableComponentType clctcomptype =
						BuildFormHandler.this.getCollectableComponentType(sControlType, sControlTypeClass);

				// visible:
				boolean bVisible = true;
				final String sVisible = attributes.getValue(ATTRIBUTE_VISIBLE);
				if (sVisible != null) {
					// override default:
					bVisible = sVisible.equals(ATTRIBUTEVALUE_YES);
				}

				// enabled:
				boolean bEnabled = true;
				final String sEnabled = attributes.getValue(ATTRIBUTE_ENABLED);
				if (sEnabled != null && !bCreateSearchableComponents) {
					// override default:
					bEnabled = sEnabled.equals(ATTRIBUTEVALUE_YES);
				}

				// insertable:
				boolean bInsertable = false;
				final String sInsertable = attributes.getValue(ATTRIBUTE_INSERTABLE);
				if (sInsertable != null) {
					// override default:
					bInsertable = sInsertable.equals(ATTRIBUTEVALUE_YES);
				}

				// rows & columns
				final Integer iRows = getIntegerValue(attributes, ATTRIBUTE_ROWS);
				final Integer iColumns = getIntegerValue(attributes, ATTRIBUTE_COLUMNS);

				// width
				final Integer width = getIntegerValue(attributes, ATTRIBUTE_WIDTH);

				// next focus component
				final String sNextFocusComponent = attributes.getValue(ATTRIBUTE_NEXTFOCUSCOMPONENT);

				final SubForm.Column column = new SubForm.Column(sName, sLabel, clctcomptype, bVisible, bEnabled, bInsertable, iRows, iColumns, width, sNextFocusComponent);

				localizationHandler = new LocalizationHandler() {
					@Override public void setTranslation(String translation) {
						column.setLabel(translation);
					}
				};

				BuildFormHandler.this.subformcolumn = column;

				handleLegacyResourceId(attributes);
			}

			@Override
			public void endElement(String sUriNameSpace, String sSimpleName, String sQualifiedName) {
				localizationHandler = null;
				final SubForm subform = (SubForm) stack.peekComponent();
				subform.addColumn(BuildFormHandler.this.subformcolumn);
				BuildFormHandler.this.subformcolumn = null;
			}

		}	// inner class SubFormColumnElementProcessor

		/**
		 * inner class <code>ChartElementProcessor</code>. Processes a chart element.
		 */
		private class ChartElementProcessor extends ComponentElementProcessor {

			private Chart chart;

			/**
			 * constructs a <code>Chart</code>, configures it according to the XML attributes
			 * and pushes it on the stack.
			 * @param sUriNameSpace
			 * @param sSimpleName
			 * @param sQualifiedName
			 * @param attributes
			 */
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {

				final String sEntityName = attributes.getValue(ATTRIBUTE_ENTITY);
				// entity is a required attribute:
				assert sEntityName != null;

				// An attribute "visible" is not a good idea as inserting the component in a tab
				// implicitly sets visible to false. This is true for all components, not only subforms.

				// enabled:
				boolean bEnabled = true;
				final String sEnabled = attributes.getValue(ATTRIBUTE_ENABLED);
				if (sEnabled != null && !bCreateSearchableComponents) {
					// override default:
					bEnabled = sEnabled.equals(ATTRIBUTEVALUE_YES);
				}

				// toolbar orientation:
				Integer iOrientation = null;
				final String sOrientation = attributes.getValue(ATTRIBUTE_TOOLBARORIENTATION);
				if (sOrientation != null) {
					iOrientation = BuildFormHandler.this.mpToolBarOrientation.get(sOrientation);
				}

				// scrollpane:
				Integer iScrollPane = null;
				final String sScrollpane = attributes.getValue(ATTRIBUTE_SCROLLPANE);
				if (sScrollpane != null) {
					iScrollPane = BuildFormHandler.this.mpScrollPane.get(sScrollpane);
				}

				// field referencing parent entity:
				final String sForeignKeyFieldToParent = attributes.getValue(ATTRIBUTE_FOREIGNKEYFIELDTOPARENT);
				
				chart = new Chart(sEntityName, 
						LangUtils.defaultIfNull(iScrollPane, -1),
						LangUtils.defaultIfNull(iOrientation, JToolBar.HORIZONTAL),
							sForeignKeyFieldToParent, false, bCreateSearchableComponents);

				chart.setEnabled(bEnabled);
				chart.setReadOnly(!bEnabled && !bCreateSearchableComponents);
				
				stack.addChart(sEntityName, chart);
			}
			
		}	// inner class ChartElementProcessor

		/**
		 * inner class <code>ScrollPaneElementProcessor</code>. Processes a scrollpane element.
		 */
		private class ScrollPaneElementProcessor extends ComponentElementProcessor {
			/**
			 * constructs a <code>JScrollPane</code>, configures it according to the XML attributes
			 * and pushes it on the stack.
			 * @param sUriNameSpace
			 * @param sSimpleName
			 * @param sQualifiedName
			 * @param attributes
			 */
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes) {
				/** @todo maybe add an attribute setMinimumSizeToPreferredSize or shrinkBelowPreferredSize */
				final JScrollPane scrlpn = new CommonJScrollPane();

				final String sHorizontalScrollBarPolicy = attributes.getValue(ATTRIBUTE_HORIZONTALSCROLLBARPOLICY);
				if (sHorizontalScrollBarPolicy != null) {
					scrlpn.setHorizontalScrollBarPolicy(BuildFormHandler.this.mpHorizontalScrollBarPolicies.get(sHorizontalScrollBarPolicy));
				}

				final String sVerticalScrollBarPolicy = attributes.getValue(ATTRIBUTE_VERTICALSCROLLBARPOLICY);
				if (sVerticalScrollBarPolicy != null) {
					scrlpn.setVerticalScrollBarPolicy(BuildFormHandler.this.mpVerticalScrollBarPolicies.get(sVerticalScrollBarPolicy));
				}

				// set unit increments to fix values:
				scrlpn.getHorizontalScrollBar().setUnitIncrement(10);
				scrlpn.getVerticalScrollBar().setUnitIncrement(10);

				stack.addScrollPane(scrlpn);
			}
		}	// inner class ScrollPaneElementProcessor

		/**
		 * inner class <code>LabelElementProcessor</code>. Processes a label element.
		 */
		private class LabelElementProcessor extends ComponentElementProcessor {
			/**
			 * constructs a <code>JLabel</code>, configures it according to the XML attributes
			 * and pushes it on the stack.
			 * @param sUriNameSpace
			 * @param sSimpleName
			 * @param sQualifiedName
			 * @param attributes
			 */
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes) {
				final JLabel lab = new JLabel();

				// name:
				final String sName = attributes.getValue(ATTRIBUTE_NAME);

				if (sName != null) {
					lab.setName(sName);
				}

				lab.setText(attributes.getValue(ATTRIBUTE_TEXT));

				stack.addComponent(lab);
				handleLegacyResourceId(attributes);
			}

			@Override
			public void endElement(String sUriNameSpace, String sSimpleName, String sQualifiedName) throws SAXException {
				JComponent comp = stack.peekComponent();
				String text = stack.peekComponentBuilder().getTranslation();
				if (comp instanceof JLabel && text != null) {
					((JLabel) comp).setText(text);
				}
				super.endElement(sUriNameSpace, sSimpleName, sQualifiedName);
			}
		}	// inner class LabelElementProcessor

		/**
		 * LocaleResourceElementProcessor processes the provided set of translations.
		 */
		private class TranslationsElementProcessor extends AbstractElementProcessor {
			@Override
			public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes) throws SAXException {
				translations = new HashMap<String, String>();
			}

			@Override
			public void endElement(String sUriNameSpace, String sSimpleName, String sQualifiedName) {
				String text = SpringLocaleDelegate.getInstance().selectBestTranslation(translations);
				if (text != null) {
					getStaticLocalizationHandler().setTranslation(text);
				}
				translations = null;
			}
		}

		/**
		 * LocaleResourceElementProcessor processes the provided set of translations.
		 */
		private class PropertyTranslationsElementProcessor extends AbstractElementProcessor {
			String sProperty;

			@Override
			public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes) throws SAXException {
				sProperty = attributes.getValue(ATTRIBUTE_NAME);
				translations = new TranslationMap();
			}

			@Override
			public void endElement(String sUriNameSpace, String sSimpleName, String sQualifiedName) {
				if (stack.peekComponent() instanceof LayoutComponent) {
					LayoutComponent lc = (LayoutComponent) stack.peekComponent();
					if (lc.getComponentProperties() != null) {
						for (Property pt : lc.getComponentProperties()) {
							if (pt.name.equals(sProperty)) {
								lc.setProperty(sProperty, translations);
								break;
							}
						}
					}
				}
				translations = null;
			}
		}

		/**
		 * LocaleResourceElementProcessor processes one translation.
		 */
		private class TranslationElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes) throws SAXException {
				String attributeLang = attributes.getValue(ATTRIBUTE_LANG);
				String attributeText = attributes.getValue(ATTRIBUTE_TEXT);
				translations.put(attributeLang, attributeText);
			}
		}

		/**
		 * inner class <code>TextFieldElementProcessor</code>. Processes a textfield element.
		 */
		private class ImageElementProcessor extends ComponentElementProcessor {
			/**
			 * constructs a <code>JTextField</code>, configures it according to the XML attributes
			 * and pushes it on the stack.
			 * @param sUriNameSpace
			 * @param sSimpleName
			 * @param sQualifiedName
			 * @param attributes
			 */
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {
				final JLabel lb = new JLabel() {
					/**
					 * set the minimum size equal to the preferred size in order to avoid
					 * GridBagLayout flaws.
					 * @return the value of the <code>preferredSize</code> property
					 */
					@Override
					public Dimension getMinimumSize() {
						return this.getPreferredSize();
					}
				};

				// name:
				final String sName = attributes.getValue(ATTRIBUTE_NAME);
				if (sName != null) {
					lb.setName(sName);
				}

				// columns:
				final Integer iColumns = getIntegerValue(attributes, ATTRIBUTE_COLUMNS);
				if (iColumns != null) {

				}

				// enabled:
				boolean bEnabled = true;
				final String sEnabled = attributes.getValue(ATTRIBUTE_ENABLED);
				if (sEnabled != null) {
					// override default:
					bEnabled = sEnabled.equals(ATTRIBUTEVALUE_YES);
				}
				lb.setEnabled(bEnabled);

				// editable:
				boolean bEditable = true;
				final String sEditable = attributes.getValue(ATTRIBUTE_EDITABLE);
				if (sEditable != null) {
					// override default:
					bEditable = sEditable.equals(ATTRIBUTEVALUE_YES);
				}


				stack.addComponent(lb);
			}
		}	// inner class TextFieldElementProcessor

		/**
		 * inner class <code>TextFieldElementProcessor</code>. Processes a textfield element.
		 */
		private class TextFieldElementProcessor extends ComponentElementProcessor {
			/**
			 * constructs a <code>JTextField</code>, configures it according to the XML attributes
			 * and pushes it on the stack.
			 * @param sUriNameSpace
			 * @param sSimpleName
			 * @param sQualifiedName
			 * @param attributes
			 */
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {
				final JTextField tf = new JTextField() {
					/**
					 * set the minimum size equal to the preferred size in order to avoid
					 * GridBagLayout flaws.
					 * @return the value of the <code>preferredSize</code> property
					 */
					@Override
					public Dimension getMinimumSize() {
						return this.getPreferredSize();
					}
				};

				// name:
				final String sName = attributes.getValue(ATTRIBUTE_NAME);
				if (sName != null) {
					tf.setName(sName);
				}

				// columns:
				final Integer iColumns = getIntegerValue(attributes, ATTRIBUTE_COLUMNS);
				if (iColumns != null) {
					tf.setColumns(iColumns);
				}

				// enabled:
				boolean bEnabled = true;
				final String sEnabled = attributes.getValue(ATTRIBUTE_ENABLED);
				if (sEnabled != null) {
					// override default:
					bEnabled = sEnabled.equals(ATTRIBUTEVALUE_YES);
				}
				tf.setEnabled(bEnabled);

				// editable:
				boolean bEditable = true;
				final String sEditable = attributes.getValue(ATTRIBUTE_EDITABLE);
				if (sEditable != null) {
					// override default:
					bEditable = sEditable.equals(ATTRIBUTEVALUE_YES);
				}
				tf.setEditable(bEditable);

				stack.addComponent(tf);
			}
		}	// inner class TextFieldElementProcessor

		/**
		 * inner class <code>TextAreaElementProcessor</code>. Processes a textarea element.
		 */
		private class TextAreaElementProcessor extends ComponentElementProcessor {
			/**
			 * constructs a <code>JTextArea</code>, configures it according to the XML attributes
			 * and pushes it on the stack.
			 * @param sUriNameSpace
			 * @param sSimpleName
			 * @param sQualifiedName
			 * @param attributes
			 */
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {

				//NUCLEUSINT-317 for scrollbars etc
				final LabeledTextArea ta = new LabeledTextArea();
				ta.getJLabel().setVisible(false);

				// name:
				final String sName = attributes.getValue(ATTRIBUTE_NAME);
				if (sName != null) {
					ta.setName(sName);
				}

				// rows:
				final Integer iRows = getIntegerValue(attributes, ATTRIBUTE_ROWS);
				if (iRows != null) {
					ta.setRows(iRows);
				}

				// columns:
				final Integer iColumns = getIntegerValue(attributes, ATTRIBUTE_COLUMNS);
				if (iColumns != null) {
					ta.setColumns(iColumns);
				}

				// enabled:
				boolean bEnabled = true;
				final String sEnabled = attributes.getValue(ATTRIBUTE_ENABLED);
				if (sEnabled != null) {
					// override default:
					bEnabled = sEnabled.equals(ATTRIBUTEVALUE_YES);
				}
				ta.setEnabled(bEnabled);

				// editable:
				boolean bEditable = true;
				final String sEditable = attributes.getValue(ATTRIBUTE_EDITABLE);
				if (sEditable != null) {
					// override default:
					bEditable = sEditable.equals(ATTRIBUTEVALUE_YES);
				}
				ta.setEditable(bEditable);

				stack.addComponent(ta);
			}
		}	// inner class TextAreaElementProcessor

		/**
		 * inner class <code>ComboBoxElementProcessor</code>. Processes a combobox element.
		 */
		private class ComboBoxElementProcessor extends ComponentElementProcessor {
			/**
			 * constructs a <code>JComboBox</code>, configures it according to the XML attributes
			 * and pushes it on the stack.
			 * @param sUriNameSpace
			 * @param sSimpleName
			 * @param sQualifiedName
			 * @param attributes
			 */
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes) {
				final JComboBox cmbbx = new JComboBox();

				// name:
				final String sName = attributes.getValue(ATTRIBUTE_NAME);
				if (sName != null) {
					cmbbx.setName(sName);
				}

				// editable:
				final String sEditable = attributes.getValue(ATTRIBUTE_EDITABLE);
				final boolean bEditable = sEditable != null && sEditable.equals("yes");
				cmbbx.setEditable(bEditable);

				//NUCLEUSINT-407 enabled:
				final String sEnabled = attributes.getValue(ATTRIBUTE_ENABLED);
				final boolean bEnabled = sEnabled != null && sEnabled.equals("yes");
				cmbbx.setEnabled(bEnabled);

				stack.addComponent(cmbbx);
			}
		}	// inner class ComboBoxElementProcessor

		public static class LayoutMLButton extends JButton {

			private String sActionkey;
			
			private LayoutMLButton() {
			}
			
			public void setActionKey(String sActionkey) {
				this.sActionkey = sActionkey;
			}
			public String getActionKey() {
				return sActionkey;
			}

			@Override
			public void setActionCommand(String actionCommand) {
				super.setActionCommand(actionCommand);

				ActionListener[] als = getActionListeners();
				for (int i = 0; i < als.length; i++) {
					ActionListener al = als[i];
					if (al instanceof LayoutMLButtonActionListener)
						((LayoutMLButtonActionListener)al).setParentComponent(this, actionCommand);
				}
			}

		}

		public static class LayoutMLButtonLocalizationHandler implements LocalizationHandler {

			private final JButton btn;

			private LayoutMLButtonLocalizationHandler(JButton btn) {
				this.btn = btn;
			}

			@Override
			public void setTranslation(String translation) {
				btn.setText(translation);
			}
		}

		/**
		 * inner class <code>ButtonElementProcessor</code>. Processes a button element.
		 */
		private class ButtonElementProcessor extends ComponentElementProcessor {

			/**
			 * constructs a <code>JButton</code>, configures it according to the XML attributes
			 * and pushes it on the stack.
			 * @param sUriNameSpace
			 * @param sSimpleName
			 * @param sQualifiedName
			 * @param attributes
			 */
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes) {
				final LayoutMLButton btn = new LayoutMLButton();

				// name:
				final String sName = attributes.getValue(ATTRIBUTE_NAME);
				if (sName != null) {
					btn.setName(sName);
				}

				final String sActionCommand = attributes.getValue(ATTRIBUTE_ACTIONCOMMAND);

				final String sLabel = attributes.getValue(ATTRIBUTE_LABEL);

				boolean bEnabled = true;
				final String sEnabled = attributes.getValue(ATTRIBUTE_ENABLED);
				if (sEnabled != null && sEnabled.equals("no")) {
					bEnabled = false;
				}
				btn.setEnabled(bEnabled);

				btn.setText(sLabel);
				localizationHandler = new LayoutMLButtonLocalizationHandler(btn);
				handleLegacyResourceId(attributes);

				final String sToolTip = attributes.getValue(ATTRIBUTE_TOOLTIP);
				btn.setToolTipText(sToolTip);

				btn.setActionCommand(sActionCommand);
				
				final String sActionkey = attributes.getValue(ATTRIBUTE_ACTIONKEYSTROKE);
				btn.setActionKey(sActionkey);

				final String sIcon = attributes.getValue(ATTRIBUTE_ICON);
				if (!StringUtils.isNullOrEmpty(sIcon)) {
					try {
						ImageIcon ico = ResourceCache.getInstance().getIconResource(sIcon);
						btn.setIcon(ico);
					}
					catch (Exception ex) {
						log.warn("Icon not found or invalid", ex);
					}
				}

				if (BuildFormHandler.this.alButtons != null) {
					btn.addActionListener(BuildFormHandler.this.alButtons);
				}
				stack.addComponent(btn);
				handleLegacyResourceId(attributes);
			}

			@Override
			public void endElement(String sUriNameSpace, String sSimpleName, String sQualifiedName) throws SAXException {
				localizationHandler = null;
				super.endElement(sUriNameSpace, sSimpleName, sQualifiedName);
			}
		}	// inner class ButtonElementProcessor

		/**
		 * inner class <code>SeparatorElementProcessor</code>. Processes a separator element.
		 */
		private class SeparatorElementProcessor extends ComponentElementProcessor {
			/**
			 * constructs a <code>JSeparator</code>, configures it according to the XML attributes
			 * and pushes it on the stack.
			 * @param sUriNameSpace
			 * @param sSimpleName
			 * @param sQualifiedName
			 * @param attributes the XML attributes of this element
			 */
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {

				Integer iOrientation = null;
				final String sOrientation = attributes.getValue(ATTRIBUTE_ORIENTATION);
				if (sOrientation != null) {
					iOrientation = BuildFormHandler.this.mpSeparatorOrientation.get(sOrientation);
				}
				final JSeparator separator = (iOrientation == null) ? new CommonJSeparator() : new CommonJSeparator(iOrientation);

				stack.addComponent(separator);
			}
		}	// class SeparatorElementProcessor

		/**
		 * inner class <code>TitledSeparatorElementProcessor</code>. Processes a titled-separator element.
		 */
		private class TitledSeparatorElementProcessor extends ComponentElementProcessor {
			/**
			 * constructs a <code>TitledSeparator</code>, configures it according to the XML attributes
			 * and pushes it on the stack.
			 * @param sUriNameSpace
			 * @param sSimpleName
			 * @param sQualifiedName
			 * @param attributes the XML attributes of this element
			 */
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {

				String sTitle = attributes.getValue(ATTRIBUTE_TITLE);

				final TitledSeparator separator = new TitledSeparator(sTitle);
				stack.addComponent(separator);
				handleLegacyResourceId(attributes);
			}

			@Override
			public void endElement(String sUriNameSpace, String sSimpleName, String sQualifiedName) throws SAXException {
				JComponent comp = stack.peekComponent();
				String text = stack.peekComponentBuilder().getTranslation();
				if (comp instanceof TitledSeparator && text != null) {
					((TitledSeparator) comp).setTitle(text);
				}
				super.endElement(sUriNameSpace, sSimpleName, sQualifiedName);
			}

		}	// class SeparatorElementProcessor

		/**
		 * inner class <code>OptionsElementProcessor</code>. Processes a options element.
		 */
		private class OptionsElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {
				final CollectableComponentBuilder ccb = (CollectableComponentBuilder) stack.peekComponentBuilder();

				if (!BuildFormHandler.this.bCreateSearchableComponents) {
					// set a default for non-searchable components:
					ccb.setDefaultOption(attributes.getValue(ATTRIBUTE_DEFAULT));
				}

				final String sOrientation = attributes.getValue(ATTRIBUTE_ORIENTATION);
				if (sOrientation != null) {
					final CollectableOptionGroup clctoptgrp = (CollectableOptionGroup) ccb.getCollectableComponent();
					clctoptgrp.setOrientation(BuildFormHandler.this.mpSwingConstantsOrientation.get(sOrientation));
				}
			}
		}

		/**
		 * inner class <code>OptionElementProcessor</code>. Processes a option element.
		 */
		private class OptionElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {
				final String sValue = attributes.getValue(ATTRIBUTE_VALUE);
				final String sLabel = attributes.getValue(ATTRIBUTE_LABEL);
				final String sMnemonic = attributes.getValue(ATTRIBUTE_MNEMONIC);

				final CollectableComponentBuilder ccb = (CollectableComponentBuilder) stack.peekComponentBuilder();
				final String[] addOption = ccb.addOption(sValue, sLabel, sMnemonic);
				localizationHandler = new LocalizationHandler() {
					@Override
					public void setTranslation(String translation) {
						addOption[1] = translation;
					}
				};
			}

			@Override
			public void endElement(String sUriNameSpace, String sSimpleName, String sQualifiedName) {
				super.endElement(sUriNameSpace, sSimpleName, sQualifiedName);
				localizationHandler = null;
			}
		}

		/**
		 * inner class <code>ValueListProviderElementProcessor</code>. Processes a valuelist-provider element.
		 */
		private class ValueListProviderElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {
				final String sType = attributes.getValue(ATTRIBUTE_TYPE);

				final String sEntityName;
				final String sFieldName;

				// <valuelist-provider> may have <collectable-component> or <subform-column> as parent:
				if (BuildFormHandler.this.subformcolumn != null) {
					final SubForm subform = (SubForm) stack.peekComponent();
					sEntityName = subform.getEntityName();
					assert sEntityName != null;
					sFieldName = BuildFormHandler.this.subformcolumn.getName();
				}
				else {
					final CollectableComponentBuilder ccb = (CollectableComponentBuilder) stack.peekComponentBuilder();
					sEntityName = BuildFormHandler.this.getCollectableEntity().getName();
					// this used to be sEntityName == null. 12.10.2005 CR
					sFieldName = ccb.getCollectableComponent().getFieldName();
				}

				final CollectableFieldsProvider valuelistprovider;
				if (StringUtils.looksEmpty(sType) || sType.equals(ATTRIBUTEVALUE_DEFAULT)) {
					// default provider:
					valuelistprovider = valueListProviderFactory.newDefaultCollectableFieldsProvider(sEntityName, sFieldName);
				}
				else if (sType.equals(ATTRIBUTEVALUE_DEPENDANT)) {
					// "dependant" provider:
					valuelistprovider = valueListProviderFactory.newDependantCollectableFieldsProvider(sEntityName, sFieldName);
				}
				else {
					// custom provider:
					valuelistprovider = valueListProviderFactory.newCustomCollectableFieldsProvider(sType, sEntityName, sFieldName);
				}

				BuildFormHandler.this.valuelistprovider = valuelistprovider;
			}

			@Override
			public void endElement(String sUriNameSpace, String sSimpleName, String sQualifiedName) {
				assert BuildFormHandler.this.valuelistprovider != null;

				if (BuildFormHandler.this.subformcolumn != null) {
					BuildFormHandler.this.subformcolumn.setValueListProvider(BuildFormHandler.this.valuelistprovider);
				}
				else {
					final CollectableComponentBuilder ccb = (CollectableComponentBuilder) stack.peekComponentBuilder();
					assert ccb != null;
					if (ccb.getCollectableComponent() instanceof LabeledCollectableComponentWithVLP) {
						((LabeledCollectableComponentWithVLP) ccb.getCollectableComponent()).setValueListProvider(BuildFormHandler.this.valuelistprovider);
					}
				}

				BuildFormHandler.this.valuelistprovider = null;
			}
		}

		/**
		 * inner class <code>PropertyValuelistProviderElementProcessor</code>. Processes a property-valuelist-provider element.
		 */
		private class PropertyValuelistProviderElementProcessor extends AbstractElementProcessor {

			String sProperty;

			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {
				sProperty = attributes.getValue(ATTRIBUTE_NAME);
				final String sType = attributes.getValue(ATTRIBUTE_TYPE);

				final String sEntityName = attributes.getValue(ATTRIBUTE_ENTITY);
				final String sFieldName = attributes.getValue(ATTRIBUTE_FIELD);

				final CollectableFieldsProvider valuelistprovider;
				if (StringUtils.looksEmpty(sType) || sType.equals(ATTRIBUTEVALUE_DEFAULT)) {
					// default provider:
					valuelistprovider = valueListProviderFactory.newDefaultCollectableFieldsProvider(sEntityName, sFieldName);
				}
				else if (sType.equals(ATTRIBUTEVALUE_DEPENDANT)) {
					// "dependant" provider:
					valuelistprovider = valueListProviderFactory.newDependantCollectableFieldsProvider(sEntityName, sFieldName);
				}
				else {
					// custom provider:
					valuelistprovider = valueListProviderFactory.newCustomCollectableFieldsProvider(sType, sEntityName, sFieldName);
				}

				BuildFormHandler.this.valuelistprovider = valuelistprovider;
			}

			@Override
			public void endElement(String sUriNameSpace, String sSimpleName, String sQualifiedName) {
				assert BuildFormHandler.this.valuelistprovider != null;

				if (stack.peekComponent() instanceof LayoutComponent) {
					LayoutComponent lc = (LayoutComponent) stack.peekComponent();
					if (lc.getComponentProperties() != null) {
						for (Property pt : lc.getComponentProperties()) {
							if (pt.name.equals(sProperty)) {
								lc.setProperty(sProperty, BuildFormHandler.this.valuelistprovider);
								break;
							}
						}
					}
				}
				BuildFormHandler.this.valuelistprovider = null;
			}
		}

		/**
		 * inner class <code>ParameterElementProcessor</code>. Processes a parameter element.
		 */
		private class ParameterElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {
				final String sName = attributes.getValue(ATTRIBUTE_NAME);
				final String sValue = attributes.getValue(ATTRIBUTE_VALUE);

				/** @todo this _still_ must be changed if <parameter> is used in other than for valuelist-provider */
				assert BuildFormHandler.this.valuelistprovider != null;

				BuildFormHandler.this.valuelistprovider.setParameter(sName, sValue);
			}
		}

		/**
		 * inner class <code>PropertyElementProcessor</code>. Processes a property element.
		 */
		private class PropertyElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {
				final String sName = attributes.getValue(ATTRIBUTE_NAME);
				final String oValue = attributes.getValue(ATTRIBUTE_VALUE);

				if (BuildFormHandler.this.subformcolumn != null) {
					BuildFormHandler.this.subformcolumn.setProperty(sName, oValue);
				} else {
					//NUCLEUSINT-1159
					//NUCLOSINT-743
					ComponentBuilder c = stack.peekComponentBuilder();
					if (c.getComponent() instanceof LayoutComponent) {
						LayoutComponent lc = (LayoutComponent) c.getComponent();
						if (lc.getComponentProperties() != null) {
							for (Property pt : lc.getComponentProperties()) {
								if (pt.name.equals(sName)) {
									if (Boolean.class.equals(pt.type) || boolean.class.equals(pt.type)) {
										lc.setProperty(sName, ATTRIBUTEVALUE_YES.equals(oValue));
									} else if (Integer.class.equals(pt.type) || int.class.equals(pt.type)) {
										lc.setProperty(sName, StringUtils.looksEmpty(oValue)? null : Integer.parseInt(oValue));
									} else {
										lc.setProperty(sName, oValue);
									}
									break;
								}
							}
						}
					} else
					if (c.getComponent() instanceof JButton) {
						JButton button = ((JButton)c.getComponent());
						if (STATIC_BUTTON.STATE_CHANGE_ACTION.equals(button.getActionCommand())) {
							if ("targetState".equals(sName)) {
								button.setActionCommand(button.getActionCommand() + "_targetState=" + oValue);
							}
						} else if (STATIC_BUTTON.EXECUTE_RULE_ACTION.equals(button.getActionCommand())) {
							if ("ruletoexecute".equals(sName)) {
								button.setActionCommand(button.getActionCommand() + "_ruletoexecute=" + oValue);
							}
						} else if (STATIC_BUTTON.GENERATOR_ACTION.equals(button.getActionCommand())) {
							if ("generatortoexecute".equals(sName)) {
								button.setActionCommand(button.getActionCommand() + "_generatortoexecute=" + oValue);
							}
						} else
							if (stack.peekComponentBuilder() instanceof CollectableComponentBuilder) {
								final CollectableComponentBuilder ccb = (CollectableComponentBuilder) stack.peekComponentBuilder();
								ccb.getCollectableComponent().setProperty(sName, oValue);
							}
					} else
					if (c.getComponent() instanceof Chart) {
						Chart chart = ((Chart)c.getComponent());
						chart.setProperty(sName, oValue);
					} else
					if (stack.peekComponentBuilder() instanceof CollectableComponentBuilder) {
						final CollectableComponentBuilder ccb = (CollectableComponentBuilder) stack.peekComponentBuilder();
						ccb.getCollectableComponent().setProperty(sName, oValue);
					}
				}
			}
		}

		/**
		 * inner class <code>DescriptionElementProcessor</code>. Processes a description element.
		 */
		private class DescriptionElementProcessor implements ElementProcessor {
			/**
			 * starts the accumulation of characters
			 * @param sUriNameSpace
			 * @param sSimpleName
			 * @param sQualifiedName
			 * @param attributes
			 */
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes) {
				// begin accumulating characters (in between the description element):
				BuildFormHandler.this.sbChars = new StringBuffer();
			}

			/**
			 * fetches the accumulated characters and sets the resulting String as the peek component's
			 * tooltip.
			 * @param sUriNameSpace
			 * @param sSimpleName
			 * @param sQualifiedName
			 */
			@Override
            public void endElement(String sUriNameSpace, String sSimpleName, String sQualifiedName) {
				// Note that we copy the String from the StringBuffer in order not to waste memory.
				final String sDescription = new String(BuildFormHandler.this.sbChars.toString());
				stack.peekComponent().setToolTipText(sDescription);
				BuildFormHandler.this.sbChars = null;
			}
		}	// inner class DescriptionElementProcessor

		/**
		 * inner class <code>BorderLayoutElementProcessor</code>. Processes a borderlayout element.
		 */
		private class BorderLayoutElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {
				final int iHGap = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_HGAP, 0);
				final int iVGap = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_VGAP, 0);

				stack.peekComponent().setLayout(new BorderLayout(iHGap, iVGap));
			}
		}	// inner class BorderLayoutElementProcessor

		/**
		 * inner class <code>FlowLayoutElementProcessor</code>. Processes a flowlayout element.
		 */
		private class FlowLayoutElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {
				final int iHGap = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_HGAP, 0);
				final int iVGap = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_VGAP, 0);

				final String sAlign = attributes.getValue(ATTRIBUTE_ALIGN);
				Integer iAlign = BuildFormHandler.this.mpFlowLayoutConstants.get(sAlign);
				if (iAlign == null) {
					iAlign = FlowLayout.CENTER;
				}

				stack.peekComponent().setLayout(new FlowLayout(iAlign, iHGap, iVGap));
			}
		}	// inner class FlowLayoutElementProcessor

		/**
		 * inner class <code>GridLayoutElementProcessor</code>. Processes a gridlayout element.
		 */
		private class GridLayoutElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {
				final int iColumns = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_COLUMNS, 0);
				final int iRows = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_ROWS, 0);
				final int iHGap = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_HGAP, 0);
				final int iVGap = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_VGAP, 0);

				stack.peekComponent().setLayout(new GridLayout(iRows, iColumns, iHGap, iVGap));
			}
		}	// inner class GridLayoutElementProcessor

		/**
		 * inner class <code>GridBagLayoutElementProcessor</code>. Processes a gridbaglayout element.
		 */
		private class GridBagLayoutElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes) {
				stack.peekComponent().setLayout(new GridBagLayout());
			}
		}	// inner class GridBagLayoutElementProcessor

		/**
		 * inner class <code>TableLayoutElementProcessor</code>. Processes a tablelayout element.
		 */
		private class TableLayoutElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes) throws SAXException {
				final char cSeparator = '|';
				String sCols = attributes.getValue(ATTRIBUTE_COLUMNS);
				String sRows = attributes.getValue(ATTRIBUTE_ROWS);

				try {
					double[] columns = StringUtils.getDoubleArrayFromString(sCols, cSeparator);
					double[] rows = StringUtils.getDoubleArrayFromString(sRows, cSeparator);

					double[][] tablelayoutDescription = { {}, {} };
					tablelayoutDescription[0] = columns;
					tablelayoutDescription[1] = rows;

					stack.peekComponent().setLayout(new TableLayout(tablelayoutDescription));
				}
				catch (NumberFormatException ex) {
					throw new SAXException("LayoutMLParser.17");
						//"Liste der Spalten und Zeilen eines TableLayouts darf nur Dezimalzahlen enthalten.");
				}
			}
		}	// inner class TableLayoutElementProcessor

		/**
		 * inner class <code>BoxLayoutElementProcessor</code>. Processes a boxlayout element.
		 */
		private class BoxLayoutElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {

				// axis:
				final String sAxis = attributes.getValue(ATTRIBUTE_AXIS);
				if (sAxis == null || sAxis.length() == 0) {
					throw new SAXException("LayoutMLParser.18");
						//"Das Attribut axis muss angegeben werden.");
				}

				final int iAxis;
				switch (Character.toUpperCase(sAxis.charAt(0))) {
					case 'X':
						iAxis = BoxLayout.X_AXIS;
						break;
					case 'Y':
						iAxis = BoxLayout.Y_AXIS;
						break;
					default:
						throw new SAXException("LayoutMLParser.19");
							//"x/y als Wert f\u00fcr das Attribut axis erwartet.");
				}

				final JComponent comp = stack.peekComponent();
				comp.setLayout(new BoxLayout(comp, iAxis));
			}
		}	// inner class BoxLayoutElementProcessor

		/**
		 * inner class <code>RowLayoutElementProcessor</code>. Processes a rowlayout element.
		 */
		private class RowLayoutElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {

				// gap:
				final int iGap = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_GAP, 0);

				// fill:
				boolean bFill = true;
				final String sFill = attributes.getValue(ATTRIBUTE_FILLVERTICALLY);
				if (sFill != null) {
					bFill = sFill.equals(ATTRIBUTEVALUE_YES);
				}

				stack.peekComponent().setLayout(new LineLayout(LineLayout.HORIZONTAL, iGap, bFill));
			}
		}	// inner class RowLayoutElementProcessor

		/**
		 * inner class <code>ColumnLayoutElementProcessor</code>. Processes a rowlayout element.
		 */
		private class ColumnLayoutElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {

				// gap:
				final int iGap = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_GAP, 0);

				// fill:
				boolean bFill = true;
				final String sFill = attributes.getValue(ATTRIBUTE_FILLHORIZONTALLY);
				if (sFill != null) {
					bFill = sFill.equals(ATTRIBUTEVALUE_YES);
				}

				stack.peekComponent().setLayout(new LineLayout(LineLayout.VERTICAL, iGap, bFill));
			}
		}	// inner class ColumnLayoutElementProcessor

		/**
		 * inner class <code>BorderLayoutConstraintsElementProcessor</code>. Processes a borderlayout-constraints element.
		 */
		private class BorderLayoutConstraintsElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes) {
				final String sPosition = attributes.getValue(ATTRIBUTE_POSITION);
				final String sConstraint = BuildFormHandler.this.mpBorderLayoutConstraints.get(sPosition);
				stack.peekComponentBuilder().setConstraints(sConstraint);
			}
		}	// inner class BorderLayoutConstraintsElementProcessor

		/**
		 * inner class <code>TabbedPaneConstraintsElementProcessor</code>. Processes a tabbedpane-constraints element.
		 */
		private class TabbedPaneConstraintsElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes) {
				final TabbedPaneConstraints tbc = new TabbedPaneConstraints();
				tbc.sTitle = attributes.getValue(ATTRIBUTE_TITLE);

				final String sEnabled = attributes.getValue(ATTRIBUTE_ENABLED);
				if (sEnabled != null) {
					tbc.bEnabled = sEnabled.equals(ATTRIBUTEVALUE_YES);
				}

				final String sInternalname = attributes.getValue(ATTRIBUTE_INTERNALNAME);
				if(sInternalname != null) {
					tbc.sInternalname = sInternalname;
				}
				final String sMnemonic = attributes.getValue(ATTRIBUTE_MNEMONIC);
				if(sMnemonic != null) {
					tbc.sMnemonic = sMnemonic;
				}

				localizationHandler = tbc;
				handleLegacyResourceId(attributes);

				stack.peekComponentBuilder().setConstraints(tbc);
			}

			@Override
			public void endElement(String sUriNameSpace, String sSimpleName, String sQualifiedName) {
				localizationHandler = null;
				super.endElement(sUriNameSpace, sSimpleName, sQualifiedName);
			}
		}	// inner class TabbedPaneConstraintsElementProcessor

		/**
		 * inner class <code>SplitPaneConstraintsElementProcessor</code>. Processes a splitpane-constraints element.
		 */
		private class SplitPaneConstraintsElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes) {
				final String sConstraints = BuildFormHandler.this.mpSplitPaneConstraints.get(attributes.getValue(ATTRIBUTE_POSITION));

				stack.peekComponentBuilder().setConstraints(sConstraints);
			}
		}	// inner class SplitPaneConstraintsElementProcessor

		/**
		 * inner class <code>ElementGridBagLayoutConstraintsProcessor</code>. Processes a gridbaglayout-constraints element.
		 */
		private class GridBagConstraintsElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {
				final int iGridX = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_GRIDX, GridBagConstraints.RELATIVE);
				final int iGridY = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_GRIDY, GridBagConstraints.RELATIVE);
				final int iGridWidth = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_GRIDWIDTH, 1);
				final int iGridHeight = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_GRIDHEIGHT, 1);

				final double dWeightX = BuildFormHandler.getDoubleValue(attributes, ATTRIBUTE_WEIGHTX, 0.0);
				final double dWeightY = BuildFormHandler.getDoubleValue(attributes, ATTRIBUTE_WEIGHTY, 0.0);

				final String sAnchor = attributes.getValue(ATTRIBUTE_ANCHOR);
				final int iAnchor = (sAnchor == null) ? GridBagConstraints.CENTER : BuildFormHandler.this.mpGridBagConstraintAnchor.get(sAnchor);

				final String sFill = attributes.getValue(ATTRIBUTE_FILL);
				final int iFill = (sFill == null) ? GridBagConstraints.NONE : BuildFormHandler.this.mpGridBagConstraintFill.get(sFill);

				final int iInsetTop = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_INSETTOP, 0);
				final int iInsetLeft = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_INSETLEFT, 0);
				final int iInsetBottom = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_INSETBOTTOM, 0);
				final int iInsetRight = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_INSETRIGHT, 0);

				final Insets insets = new Insets(iInsetTop, iInsetLeft, iInsetBottom, iInsetRight);

				final int iPadX = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_PADX, 0);
				final int iPadY = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_PADY, 0);

				final GridBagConstraints gbc = new GridBagConstraints(iGridX, iGridY, iGridWidth, iGridHeight, dWeightX, dWeightY,
						iAnchor, iFill, insets, iPadX, iPadY);
				stack.peekComponentBuilder().setConstraints(gbc);
			}
		}	// inner class GridBagConstraintsElementProcessor

		/**
		 * inner class <code>ElementGridBagLayoutConstraintsProcessor</code>. Processes a gridbaglayout-constraints element.
		 */
		private class TableLayoutConstraintsElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {
				final TableLayoutConstraints constraint = new TableLayoutConstraints();
				constraint.hAlign = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_HALIGN, 0);
				constraint.vAlign = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_VALIGN, 0);
				constraint.col1 = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_COL1, 0);
				constraint.row1 = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_ROW1, 0);
				constraint.col2 = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_COL2, 0);
				constraint.row2 = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_ROW2, 0);

				stack.peekComponentBuilder().setConstraints(constraint);
			}
		}	// inner class ElementGridBagLayoutConstraintsProcessor

		/**
		 * inner class <code>MinimumSizeElementProcessor</code>. Processes a minimum-size element.
		 */
		private class MinimumSizeElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {
				final int iWidth = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_WIDTH, 0);
				final int iHeight = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_HEIGHT, 0);

				stack.peekComponent().setMinimumSize(new Dimension(iWidth, iHeight));
			}
		}	// inner class MinimumSizeElementProcessor

		/**
		 * inner class <code>PreferredSizeElementProcessor</code>. Processes a preferred-size element.
		 */
		private class PreferredSizeElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {
				final int iWidth = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_WIDTH, 0);
				final int iHeight = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_HEIGHT, 0);

				stack.peekComponent().setPreferredSize(new Dimension(iWidth, iHeight));
			}
		}	// inner class PreferredSizeElementProcessor

		/**
		 * inner class <code>StrictSizeElementProcessor</code>. Processes a strict-size element.
		 */
		private class StrictSizeElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {
				final int iWidth = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_WIDTH, 0);
				final int iHeight = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_HEIGHT, 0);

				if (stack.peekComponent() instanceof StrictSizeComponent) {
					((StrictSizeComponent)stack.peekComponent()).setStrictSize(new Dimension(iWidth, iHeight));
				} else {
					throw new NuclosFatalException("StrictSize not avaiable for class " + stack.peekComponent().getClass().getName());
				}
			}
		}	// inner class StrictSizeElementProcessor

		/**
		 * inner class <code>PropertySizeElementProcessor</code>. Processes a property-size element.
		 */
		private class PropertySizeElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {
				final String sProperty = attributes.getValue(ATTRIBUTE_NAME);
				final int iWidth = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_WIDTH, 0);
				final int iHeight = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_HEIGHT, 0);

				if (stack.peekComponent() instanceof LayoutComponent) {
					LayoutComponent lc = (LayoutComponent) stack.peekComponent();
					if (lc.getComponentProperties() != null) {
						for (Property pt : lc.getComponentProperties()) {
							if (pt.name.equals(sProperty)) {
								lc.setProperty(sProperty, new Dimension(iWidth, iHeight));
							}
						}
					}
				}
			}
		}	// inner class PropertySizeElementProcessor

		/**
		 * inner class <code>ClearBorderElementProcessor</code>. Processes a clear-border element.
		 */
		private class ClearBorderElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {

				stack.peekComponent().setBorder(null);
			}
		}	// inner class ClearBorderElementProcessor

		/**
		 * inner class <code>EmptyBorderElementProcessor</code>. Processes an empty-border element.
		 */
		private class EmptyBorderElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {
				final int iTop = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_TOP, 0);
				final int iLeft = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_LEFT, 0);
				final int iBottom = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_BOTTOM, 0);
				final int iRight = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_RIGHT, 0);

				BuildFormHandler.this.addBorder(BorderFactory.createEmptyBorder(iTop, iLeft, iBottom, iRight));
			}
		}	// inner class EmptyBorderElementProcessor

		/**
		 * inner class <code>EtchedBorderElementProcessor</code>. Processes an etched-border element.
		 */
		private class EtchedBorderElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes) {
				int iType = BevelBorder.LOWERED;	// default;
				final String sType = attributes.getValue(ATTRIBUTE_TYPE);
				if (sType != null && sType.equals(ATTRIBUTEVALUE_RAISED)) {
					iType = BevelBorder.RAISED;
				}
				BuildFormHandler.this.addBorder(BorderFactory.createEtchedBorder(iType));
			}
		}	// inner class EtchedBorderElementProcessor

		/**
		 * inner class <code>BevelBorderElementProcessor</code>. Processes a bevel-border element.
		 */
		private class BevelBorderElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes) {
				int iType = BevelBorder.LOWERED;	// default;
				final String sType = attributes.getValue(ATTRIBUTE_TYPE);
				if (sType != null && sType.equals(ATTRIBUTEVALUE_RAISED)) {
					iType = BevelBorder.RAISED;
				}
				BuildFormHandler.this.addBorder(BorderFactory.createBevelBorder(iType));
			}
		}	// inner class BevelBorderElementProcessor

		/**
		 * inner class <code>LineBorderElementProcessor</code>. Processes a line-border element.
		 */
		private class LineBorderElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {
				final int iRed = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_RED, 0);
				final int iGreen = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_GREEN, 0);
				final int iBlue = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_BLUE, 0);
				final Color color = new Color(iRed, iGreen, iBlue);

				final int iThickness = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_THICKNESS, 1);

				BuildFormHandler.this.addBorder(BorderFactory.createLineBorder(color, iThickness));
			}
		}	// inner class LineBorderElementProcessor

		/**
		 * inner class <code>TitledBorderElementProcessor</code>. Processes a titled-border element.
		 */
		private class TitledBorderElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes) {
				String sTitle = attributes.getValue(ATTRIBUTE_TITLE);

				final TitledBorder titledBorder = BorderFactory.createTitledBorder(sTitle);

				localizationHandler = new LocalizationHandler() {
					@Override
					public void setTranslation(String translation) {
						titledBorder.setTitle(translation);
					}
				};
				handleLegacyResourceId(attributes);

				BuildFormHandler.this.addBorder(titledBorder);
			}

			@Override
			public void endElement(String sUriNameSpace, String sSimpleName, String sQualifiedName) {
				localizationHandler = null;
				super.endElement(sUriNameSpace, sSimpleName, sQualifiedName);
			}
		}	// inner class TitledBorderElementProcessor

		/**
		 * inner class <code>FontElementProcessor</code>. Processes a font element.
		 */
		private class FontElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes) throws SAXException {
				final int iRelativeSize = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_SIZE, 0);

				JComponent comp = stack.peekComponent();

				if (comp instanceof LabeledComponent) {
					if (comp instanceof LabeledTextArea) {
						// NUCLEUSINT-276 NUCLEUSINT-192
						setFontSize((((LabeledTextArea) comp).getJLabel()), iRelativeSize);
						setFontSize((((LabeledTextArea) comp).getJTextArea()), iRelativeSize);
					} else {
						// NUCLEUSINT-276 NUCLEUSINT-192
						setFontSize((((LabeledComponent) comp).getJLabel()), iRelativeSize);
						setFontSize((((LabeledComponent) comp).getControlComponent()), iRelativeSize);
					}
				} else if (comp instanceof TitledSeparator) {
					// NUCLEUSINT-276 NUCLEUSINT-192
					setFontSize((((TitledSeparator) comp).getJLabel()), iRelativeSize);
				} else {
					setFontSize(comp, iRelativeSize);
				}
			}

			/**
			 * Externalised for avoing duplicate code
			 *
			 * NUCLEUSINT-276
			 * @param comp
			 * @param iRelativeSize
			 */
			private void setFontSize(JComponent comp, int iRelativeSize) {
				final Font fontOld = comp.getFont();
				if (fontOld != null) {
					final float fNewFontSize = fontOld.getSize() + iRelativeSize;
					final Font fontNew = fontOld.deriveFont(fNewFontSize);
					comp.setFont(fontNew);
				}
			}
		}	// inner class FontElementProcessor

		/**
		 * inner class <code>PropertyFontElementProcessor</code>. Processes a property font element.
		 */
		private class PropertyFontElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes) throws SAXException {
				final String sProperty = attributes.getValue(ATTRIBUTE_NAME);
				final int iRelativeSize = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_SIZE, 0);

				if (stack.peekComponent() instanceof LayoutComponent) {
					LayoutComponent lc = (LayoutComponent) stack.peekComponent();
					if (lc.getComponentProperties() != null) {
						for (Property pt : lc.getComponentProperties()) {
							if (pt.name.equals(sProperty)) {
								lc.setProperty(sProperty, getFont(stack.peekComponent(), iRelativeSize));
								break;
							}
						}
					}
				}
			}

			private Font getFont(JComponent comp, int iRelativeSize) {
				final Font fontOld = comp.getFont();
				if (fontOld != null) {
					final float fNewFontSize = fontOld.getSize() + iRelativeSize;
					final Font fontNew = fontOld.deriveFont(fNewFontSize);
					return fontNew;
				}
				return fontOld;
			}
		}	// inner class FontElementProcessor

		/**
		 * inner class <code>BackgroundElementProcessor</code>. Processes a background element.
		 */
		private class BackgroundElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {
				final int iRed = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_RED, 0);
				final int iGreen = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_GREEN, 0);
				final int iBlue = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_BLUE, 0);
				final Color color = new Color(iRed, iGreen, iBlue);

				final JComponent comp = stack.peekComponent();
				comp.setBackground(color);
				comp.setOpaque(true);
				// set opaque implicitly in order so the background color is shown
			}
		}	// inner class BackgroundElementProcessor

		/**
		 * inner class <code>PropertyColorElementProcessor</code>. Processes a property color.
		 */
		private class PropertyColorElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {
				final String sProperty = attributes.getValue(ATTRIBUTE_NAME);
				final int iRed = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_RED, 0);
				final int iGreen = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_GREEN, 0);
				final int iBlue = BuildFormHandler.getIntValue(attributes, ATTRIBUTE_BLUE, 0);

				if (stack.peekComponent() instanceof LayoutComponent) {
					LayoutComponent lc = (LayoutComponent) stack.peekComponent();
					if (lc.getComponentProperties() != null) {
						for (Property pt : lc.getComponentProperties()) {
							if (pt.name.equals(sProperty)) {
								lc.setProperty(sProperty, new Color(iRed, iGreen, iBlue));
							}
						}
					}
				}
			}
		}	// inner class PropertyColorElementProcessor

		/**
		 * inner class <code>DependencyElementProcessor</code>. Processes a dependency element.
		 */
		private class DependencyElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {
				final String sDependantFieldName = attributes.getValue(ATTRIBUTE_DEPENDANTFIELD);
				final String sDependsOnFieldName = attributes.getValue(ATTRIBUTE_DEPENDSONFIELD);
				BuildFormHandler.this.addDependency(sDependantFieldName, sDependsOnFieldName);
			}
		}

		/**
		 * inner class <code>RuleElementProcessor</code>. Processes a rule element.
		 */
		private class RuleElementProcessor implements ElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {
				BuildFormHandler.this.rule = new Rule();
			}

			@Override
            public void endElement(String sUriNameSpace, String sSimpleName, String sQualifiedName) throws SAXException {
				BuildFormHandler.this.rule.finish();
				BuildFormHandler.this.rule = null;
			}
		}

		/**
		 * inner class <code>EventElementProcessor</code>. Processes an event element.
		 */
		private class EventElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {
				BuildFormHandler.this.rule.event = new Event();
				BuildFormHandler.this.rule.event.sType = attributes.getValue(ATTRIBUTE_TYPE);
				BuildFormHandler.this.rule.event.sSourceComponentName = attributes.getValue(ATTRIBUTE_SOURCECOMPONENT);
				final String sEntity = attributes.getValue(ATTRIBUTE_ENTITY);
				if (sEntity != null) {
					final SubForm subform = stack.getSubFormForEntity(sEntity);
//					if (subform == null) {
//						throw new SAXException("Unterformular f\u00fcr Entit\u00e4t " + sEntity + " nicht gefunden.");
//					}
					BuildFormHandler.this.rule.event.subform = subform;
				}
			}
		}

		private class TransferLookedUpValueElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {
				final TransferLookedUpValueAction act = new TransferLookedUpValueAction(attributes.getValue(ATTRIBUTE_TARGETCOMPONENT), attributes.getValue(ATTRIBUTE_SOURCEFIELD));
				BuildFormHandler.this.rule.collActions.add(act);
			}
		}

		private class ClearElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {
				final ClearAction act = new ClearAction(attributes.getValue(ATTRIBUTE_TARGETCOMPONENT));
				BuildFormHandler.this.rule.collActions.add(act);
			}
		}

		private class RefreshValueListElementProcessor extends AbstractElementProcessor {
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {
				String sParameterNameForSourceComponent = attributes.getValue(ATTRIBUTE_PARAMETER_FOR_SOURCECOMPONENT);
				if (sParameterNameForSourceComponent == null) {
					sParameterNameForSourceComponent = "relatedId";
				}
				final RefreshValueListAction act = new RefreshValueListAction(attributes.getValue(ATTRIBUTE_TARGETCOMPONENT), attributes.getValue(ATTRIBUTE_ENTITY),
						sParameterNameForSourceComponent);
				BuildFormHandler.this.rule.collActions.add(act);
			}
		}

		private class ScriptElementProcessor implements ElementProcessor {

			private NuclosScript script;

			@Override
			public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes) throws SAXException {
				sbChars = new StringBuffer();
				script = new NuclosScript();
				script.setLanguage(attributes.getValue(ATTRIBUTE_LANGUAGE));
			}

			@Override
			public void endElement(String sUriNameSpace, String sSimpleName, String sQualifiedName) throws SAXException {
				script.setSource(sbChars.toString().trim());
				if (stack.peekComponent() instanceof SubForm) {
					SubForm subform = (SubForm) stack.peekComponent();
					if (ELEMENT_NEW_ENABLED.equals(sQualifiedName)) {
						subform.setNewEnabledScript(script);
					}
					else if (ELEMENT_EDIT_ENABLED.equals(sQualifiedName)) {
						subform.setEditEnabledScript(script);
					}
					else if (ELEMENT_DELETE_ENABLED.equals(sQualifiedName)) {
						subform.setDeleteEnabledScript(script);
					}
					else if (ELEMENT_CLONE_ENABLED.equals(sQualifiedName)) {
						subform.setCloneEnabledScript(script);
					}
				}
				else {
					final CollectableComponentBuilder ccb = (CollectableComponentBuilder) stack.peekComponentBuilder();
					assert ccb != null;
					if (ELEMENT_ENABLED.equals(sQualifiedName)) {
						ccb.clctcomp.setEnabledScript(script);
					}
				}
				sbChars = null;
			}

		}

		private class PropertyScriptElementProcessor implements ElementProcessor {

			private String sProperty;
			private NuclosScript script;

			@Override
			public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes) throws SAXException {
				sProperty = attributes.getValue(ATTRIBUTE_NAME);
				sbChars = new StringBuffer();
				script = new NuclosScript();
				script.setLanguage(attributes.getValue(ATTRIBUTE_LANGUAGE));
			}

			@Override
			public void endElement(String sUriNameSpace, String sSimpleName, String sQualifiedName) throws SAXException {
				script.setSource(sbChars.toString().trim());

				if (stack.peekComponent() instanceof LayoutComponent) {
					LayoutComponent lc = (LayoutComponent) stack.peekComponent();
					if (lc.getComponentProperties() != null) {
						for (Property pt : lc.getComponentProperties()) {
							if (pt.name.equals(sProperty)) {
								lc.setProperty(sProperty, script);
								break;
							}
						}
					}
				}
				sbChars = null;
			}

		}

		/**
		 * inner class <code>LayoutComponentElementProcessor</code>. Processes a layout component element.
		 */
		@Configurable
		class LayoutComponentElementProcessor extends ComponentElementProcessor {

			private NucletComponentRepository nucletComponentRepository;

			public LayoutComponentElementProcessor() {

			}

			@Autowired
			void setNucletComponentRepository(NucletComponentRepository nucletComponentRepository) {
				this.nucletComponentRepository = nucletComponentRepository;
			}

			/**
			 * constructs a <code>LayoutComponent</code>, configures it according to the XML attributes
			 * and pushes it on the stack.
			 * @param sUriNameSpace
			 * @param sSimpleName
			 * @param sQualifiedName
			 * @param attributes
			 */
			@Override
            public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
					throws SAXException {

				// name: (used for preferences)
				final String sName = attributes.getValue(ATTRIBUTE_NAME);
				// class:
				final String sClass = attributes.getValue(ATTRIBUTE_CLASS);
				LayoutComponentFactory layoutComponentFactory = null;
				if (sClass != null) {
					for (LayoutComponentFactory lcf : nucletComponentRepository.getLayoutComponentFactories()) {
						if (lcf.getClass().getName().equals(sClass)) {
							layoutComponentFactory = lcf;
						}
					}
					if (layoutComponentFactory == null) {
						stack.addComponent(new JLabel(String.format("LayoutComponent %s not found", sClass)));
						return;
					}
				} else {
					stack.addComponent(new JLabel("LayoutComponent class must not be null"));
					return;
				}

				final LayoutComponentHolder holder = new LayoutComponentHolder(layoutComponentFactory.newInstance(), false);
				holder.setName(sName);
				stack.addComponent(holder);
			}
		}	// inner class LayoutComponentElementProcessor

		/**
		 * called by the underlying SAX parser when a start element event occurs.
		 * Maps the element to the corresponding element processor and delegates the processing to
		 * <code>startElement()</code> method of the element processor.
		 * @param sUriNameSpace
		 * @param sSimpleName
		 * @param sQualifiedName
		 * @param attributes
		 * @throws SAXException
		 */
		@Override
		public void startElement(String sUriNameSpace, String sSimpleName, String sQualifiedName, Attributes attributes)
				throws SAXException {
			final ElementProcessor elementProcessor = mpElementProcessors.get(sQualifiedName);
			if (elementProcessor != null) {
				elementProcessor.startElement(sUriNameSpace, sSimpleName, sQualifiedName, attributes);
			}
		}

		/**
		 * called by the underlying SAX parser when an end element event occurs.
		 * Maps the element to the corresponding element processor and delegates the processing to
		 * <code>endElement()</code> method of the element processor.
		 * @param sUriNameSpace
		 * @param sSimpleName
		 * @param sQualifiedName
		 * @throws SAXException
		 */
		@Override
		public void endElement(String sUriNameSpace, String sSimpleName, String sQualifiedName) throws SAXException {
			final ElementProcessor elementProcessor = mpElementProcessors.get(sQualifiedName);
			if (elementProcessor != null) {
				elementProcessor.endElement(sUriNameSpace, sSimpleName, sQualifiedName);
			}
		}

		/**
		 * called by the underlying SAX parser when "free characters" are parsed.
		 * They are accumulated here.
		 * @param ac
		 * @param start
		 * @param length
		 * @throws SAXException
		 */
		@Override
		public void characters(char[] ac, int start, int length) throws SAXException {
			if (this.sbChars != null) {
				this.sbChars.append(ac, start, length);
			}
		}

	}	// class BuildFormHandler

}	// class LayoutMLParser
