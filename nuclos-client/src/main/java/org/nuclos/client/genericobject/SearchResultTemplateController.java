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
package org.nuclos.client.genericobject;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.nuclos.client.genericobject.resulttemplate.ResultTemplates;
import org.nuclos.client.genericobject.resulttemplate.SaveSearchResultTemplateController;
import org.nuclos.client.genericobject.resulttemplate.SearchResultTemplate;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.ui.BlackLabel;
import org.nuclos.client.ui.CommonAbstractAction;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.result.NuclosResultController;
import org.nuclos.client.ui.collect.result.ResultPanel;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.common2.exception.PreferencesException;

/**
 * Controller for handling {@link SearchResultTemplate}s of GenericObjects.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Rostislav.Maksymovskyi@novabit.de">Rostislav Maksymovskyi</a>
 * @version 01.00.00
 */
public class SearchResultTemplateController {

	private static final Logger LOG = Logger.getLogger(SearchResultTemplateController.class);

	private final Action actSaveTemplate = new CommonAbstractAction(SpringLocaleDelegate.getInstance().getMessage(
			"SearchResultTemplateController.1", "Suchergebnisvorlage speichern"),
			Icons.getInstance().getIconSave16(),
			SpringLocaleDelegate.getInstance().getMessage(
					"SearchResultTemplateController.2", "Eingestelltes Ergebnisformat als Suchergebnisvorlage speichern")) {

		@Override
		public void actionPerformed(ActionEvent ev) {
			cmdSaveTemplate();
		}
	};

	private final Action actRemoveTemplate = new CommonAbstractAction(SpringLocaleDelegate.getInstance().getMessage(
			"SearchResultTemplateController.3", "Suchergebnisvorlage l\u00f6schen"),
			Icons.getInstance().getIconDelete16(),
			SpringLocaleDelegate.getInstance().getMessage(
					"SearchResultTemplateController.4", "Ausgew\u00e4hlte Suchergebnisvorlage l\u00f6schen")) {

		@Override
		public void actionPerformed(ActionEvent ev) {
			cmdRemoveTemplate();
		}
	};

	private final ActionListener alSearchResultTemplates = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent ev) {
			cmdSetSearchResultFormatAccordingToTemplate();
		}
	};

	//private final JButton btnSaveTemplate;
	//private final JButton btnRemoveTemplate;
	private final JComboBox cmbbxSearchResultTemplate = new JComboBox();
	private final GenericObjectCollectController ctl;

	SearchResultTemplateController(ResultPanel<?> resultPanel, GenericObjectCollectController ctl){
		this.ctl = ctl;
		cmbbxSearchResultTemplate.setVisible(false);
		//toolbar.add(getSearchResultTemplateComboBox());
		///*btnSaveTemplate =*/ toolbar.add(actSaveTemplate);
		///*btnRemoveTemplate =*/ toolbar.add(actRemoveTemplate);
		resultPanel.addToolBarComponent(new BlackLabel(getSearchResultTemplateComboBox(),
				SpringLocaleDelegate.getInstance().getMessage("R00016434","Suchergebnisvorlage")));
		resultPanel.addPopupExtraSeparator();
		resultPanel.addPopupExtraMenuItem(new JMenuItem(actSaveTemplate));
		resultPanel.addPopupExtraMenuItem(new JMenuItem(actRemoveTemplate));
		init();
		refreshSearchResultTemplateView();
	}

	/**
	 * @return the combo box containing the search result templates.
	 */
	private JComboBox getSearchResultTemplateComboBox() {
		return this.cmbbxSearchResultTemplate;
	}

	/**
	 * initializes tooltips and some listeners for search result templates ComboBox.
	 */
	private void init(){
		this.getSearchResultTemplateComboBox().setName("cmbbxSearchResultTemplate");
		this.getSearchResultTemplateComboBox().setToolTipText(SpringLocaleDelegate.getInstance().getMessage(
				"SearchResultTemplateController.5", "W\u00e4hlen Sie hier eine Suchergebnisvorlage aus"));
		this.getSearchResultTemplateComboBox().addActionListener(this.alSearchResultTemplates);

		// set tool tips dynamically:
		this.getSearchResultTemplateComboBox().setRenderer(new DefaultListCellRenderer() {

			@Override
			public Component getListCellRendererComponent(JList lst, Object oValue, int index, boolean bSelected,
					boolean bCellHasFocus) {
				final JComponent result = (JComponent) super.getListCellRendererComponent(lst, oValue, index, bSelected,
						bCellHasFocus);
				String sToolTip = null;
				if (oValue != null) {
					final SearchResultTemplate filter = (SearchResultTemplate) oValue;
					sToolTip = filter.getDescription();
				}
				result.setToolTipText(sToolTip);

				return result;
			}
		});

		// set the tool tip for the combobox also, as the tool tip for the renderer seems to be taken in dropped down items only:
		this.getSearchResultTemplateComboBox().addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent ev) {
				final boolean bSelected = (ev.getStateChange() == ItemEvent.SELECTED);
				boolean bRegularTemplateSelected = bSelected;

				String sToolTip = null;
				if (bSelected) {
					final SearchResultTemplate template = (SearchResultTemplate) ev.getItem();
					assert template != null;
					sToolTip = template.getDescription();
					LOG.debug("Template selected: " + template.getName());
					bRegularTemplateSelected = !template.isDefaultTemplate();
				}
				getSearchResultTemplateComboBox().setToolTipText(sToolTip);

				// enable/disable remove template action - the empty filter cannot be removed:
				actRemoveTemplate.setEnabled(bRegularTemplateSelected);
			}
		});
	}

	/**
	 * refreshes the combobox containing the result search templates
	 */
	private void refreshSearchResultTemplateView() {
		// remember search result template, if any:
		final SearchResultTemplate templateSelected = getSelectedSearchResultTemplate();

		final List<? extends SearchResultTemplate> lstTemplates;
		try {
			lstTemplates = this.getSearchResultTemplates().getAll();
		}
		catch (PreferencesException ex) {
			throw new NuclosFatalException(ex);
		}
		final DefaultComboBoxModel model = (DefaultComboBoxModel) this.getSearchResultTemplateComboBox().getModel();

		// don't fire changed events here:
		this.getSearchResultTemplateComboBox().removeActionListener(this.alSearchResultTemplates);
		try {
			model.removeAllElements();
			model.addElement(newDefaultTemplate());
			for (SearchResultTemplate template : lstTemplates) {
				model.addElement(template);
			}

			this.cmbbxSearchResultTemplate.setVisible(model.getSize() > 1);

			UIUtils.setMaximumWidth(this.getSearchResultTemplateComboBox(), UIUtils.getPreferredWidth(this.getSearchResultTemplateComboBox()));

			// try to restore the previously selected filter, if any:
			if (templateSelected != null) {
				this.setSelectedSearchResultTemplate(templateSelected);
			}
		}
		finally {
			this.getSearchResultTemplateComboBox().addActionListener(this.alSearchResultTemplates);
		}

		// perform alSearchResultTemplates's action manually:
		this.alSearchResultTemplates.actionPerformed(null);
	}

	/**
	 * @return the search result templates that can be edited in this collect controller. By default, it's the search result templates for this
	 *         collect controller's module.
	 */
	private ResultTemplates.SearchResultTemplates getSearchResultTemplates() {
		return ResultTemplates.templatesForModule(ctl.getModuleId());
	}

	/**
	 * selects the given search result template in the search result templates combo box.
	 *
	 * @param template
	 * @postcondition LangUtils.equals(this.getSelectedSearchResultTemplate(), template)
	 */
	private final void setSelectedSearchResultTemplate(SearchResultTemplate template) {
		this.getSearchResultTemplateComboBox().setSelectedItem(template);
	}

	/**
	 * selects the search result template by name in the search result templates combo box.
	 *
	 * @param template
	 * @postcondition LangUtils.equals(this.getSelectedSearchResultTemplate(), template)
	 */
	public final void setSelectedSearchResultTemplate(String sTemplateName) {
		for (int i = 1; i < this.getSearchResultTemplateComboBox().getItemCount(); ++i) {
			if (((SearchResultTemplate) this.getSearchResultTemplateComboBox().getItemAt(i)).getName().equals(sTemplateName)) {
				this.getSearchResultTemplateComboBox().setSelectedIndex(i);
				break;
			}
		}
	}

	/**
	 * @return empty search result template to be used as default
	 */
	private SearchResultTemplate newDefaultTemplate() {
		final SearchResultTemplate result = this.getSearchResultTemplates().newDefaultTemplate();
		return result;
	}

	/**
	 * @return the search result template (if any) selected in the combo box
	 */
	public final SearchResultTemplate getSelectedSearchResultTemplate() {
		return (SearchResultTemplate) cmbbxSearchResultTemplate.getSelectedItem();
	}

	/**
	 * @return true if selected template is default template
	 */
	public final boolean isSelectedDefaultSearchResultTemplate() {
		return this.getSelectedSearchResultTemplate().isDefaultTemplate();
	}

	/**
	 * selects the default template (the first entry in the combobox).
	 */
	public void selectDefaultTemplate() {
		this.getSearchResultTemplateComboBox().setSelectedIndex(0);
	}

	/**
	 * sets the search result format according to selected template.
	 */
	private void cmdSetSearchResultFormatAccordingToTemplate() {
		UIUtils.runShortCommand(getFrame(), new CommonRunnable() {
			@Override
			public void run() {
				final SearchResultTemplate templateSelected = getSelectedSearchResultTemplate();
				if(templateSelected != null && !templateSelected.isDefaultTemplate()){
					ctl.makeSureSelectedFieldsAreNonEmpty(ctl.getCollectableEntity(), templateSelected.getVisibleColumns());
					final List<CollectableEntityField> lstSelectedNew = templateSelected.getVisibleColumns();
					final Set<CollectableEntityField> fixedColumns = new HashSet<CollectableEntityField>(
							templateSelected.getListColumnsFixed());
					final Map<String, Integer> listColumnsWidths = templateSelected.getListColumnsWidths();
					final Map<String, Integer> clefListColumnsWidths = new HashMap<String, Integer>();
					for(CollectableEntityField clFiled : lstSelectedNew) {
						if(listColumnsWidths.containsKey(clFiled.getName())) {
							clefListColumnsWidths.put(clFiled.getName(), listColumnsWidths.get(clFiled.getName()));
						}
					}
					// historic code:
					// ctl.getResultController().initializeFields(ctl.getFields(), ctl, lstSelectedNew);

					final NuclosResultController<CollectableGenericObjectWithDependants> resultController = (NuclosResultController)
							ctl.getResultController();
					/*
					final SortedSet<CollectableEntityField> available = resultController.getFieldsAvailableForResult(
							ctl.getCollectableEntity(), resultController.getCollectableEntityFieldComparator());
					resultController.setSelectColumns(ctl.getFields(), ctl, available, lstSelectedNew, fixedColumns);
					 */
					resultController.initializeFields(ctl.getFields(), lstSelectedNew, fixedColumns, clefListColumnsWidths);
				}
			}
		});
	}

	/**
	 * gets CommonJInternalFrame.
	 */
	private MainFrameTab getFrame() {
		return ctl.getTab();
	}

	/**
	 * Command: save template
	 */
	private void cmdSaveTemplate() {
		UIUtils.runCommand(getFrame(), new CommonRunnable() {
			@Override
			public void run() throws CommonValidationException {
				try {
					final SearchResultTemplate templateSelected = getSelectedSearchResultTemplate();
					final SearchResultTemplate templateCurrent = ctl.getCurrentSearchResultFormatFromResultPanel();
					final DefaultComboBoxModel model = (DefaultComboBoxModel) getSearchResultTemplateComboBox().getModel();
					final SaveSearchResultTemplateController.Command cmd = new SaveSearchResultTemplateController(getFrame(), getSearchResultTemplates()).runSave(
							templateSelected, templateCurrent);
					switch (cmd) {
					case None:
						// do nothing
						break;

					case Overwrite:
						selectDefaultTemplate();
						getSearchResultTemplateComboBox().removeItem(templateSelected);
						// note that there is no "break" here!

					case New:
						model.addElement(templateCurrent);
						UIUtils.setMaximumWidth(getSearchResultTemplateComboBox(), UIUtils.getPreferredWidth(getSearchResultTemplateComboBox()));
						getSearchResultTemplateComboBox().setSelectedItem(templateCurrent);
						break;

					default:
						assert false;
					}
					cmbbxSearchResultTemplate.setVisible(model.getSize() > 1);
				}
				catch (CommonBusinessException ex) {
					Errors.getInstance().showExceptionDialog(getFrame(), ex);
				}
			}
		});
	}

	/**
	 * command: remove template
	 */
	private void cmdRemoveTemplate() {
		final SearchResultTemplate template = this.getSelectedSearchResultTemplate();
		if (template != null) {
			final String sTemplateName = template.getName();
			final String sMessage = SpringLocaleDelegate.getInstance().getMessage(
					"SearchResultTemplateController.6", "Wollen Sie die Suchergebnisvorlage \"{0}\" wirklich l\u00f6schen?", sTemplateName);
			final int iBtn = JOptionPane.showConfirmDialog(getFrame(), sMessage, SpringLocaleDelegate.getInstance().getMessage(
					"SearchResultTemplateController.3", "Suchergebnisvorlage l\u00f6schen"),
					JOptionPane.OK_CANCEL_OPTION);
			if (iBtn == JOptionPane.OK_OPTION) {
				UIUtils.runCommand(getFrame(), new Runnable() {
					@Override
					public void run() {
						try {
							getSearchResultTemplates().remove(sTemplateName);
							refreshSearchResultTemplateView();
						}
						catch (Exception e) {
							LOG.error("cmdRemoveTemplate failed: " + e, e);
						}
					}
				});
			}
		}
	}

}	// class SearchResultTemplateController
