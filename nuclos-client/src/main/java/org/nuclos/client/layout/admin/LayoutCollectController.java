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
package org.nuclos.client.layout.admin;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.RowSorter.SortKey;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;
import org.nuclos.client.common.DetailsSubFormController;
import org.nuclos.client.common.LocaleDelegate;
import org.nuclos.client.common.NuclosCollectControllerFactory;
import org.nuclos.client.common.Utils;
import org.nuclos.client.entityobject.CollectableEntityObject;
import org.nuclos.client.layout.wysiwyg.CollectableWYSIWYGLayoutEditor;
import org.nuclos.client.layout.wysiwyg.CollectableWYSIWYGLayoutEditor.WYSIWYGDetailsComponentModel;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.main.mainframe.MainFrameTabbedPane;
import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.client.masterdata.MasterDataCollectController;
import org.nuclos.client.ui.CommonJTextField;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.CollectableTextArea;
import org.nuclos.client.ui.collect.component.CollectableTextField;
import org.nuclos.client.ui.collect.result.LayoutResultController;
import org.nuclos.client.ui.collect.result.NuclosSearchResultStrategy;
import org.nuclos.client.ui.layoutml.LayoutRoot;
import org.nuclos.common.NuclosAttributeNotFoundException;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.WorkspaceDescription.EntityPreferences;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.EntityAndFieldName;
import org.nuclos.common2.IOUtils;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.LocaleInfo;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.XMLUtils;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * Controller for collecting the layouts for LayoutML based dialogs. <br>
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author <a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public abstract class LayoutCollectController extends MasterDataCollectController {

	private static final Logger LOG = Logger.getLogger(LayoutCollectController.class);
	
	protected static final String PREFS_KEY_LASTIMPORTEXPORTPATH = "lastImportExportPath";
	protected static final String LAYOUTML_EXTENSION = ".layoutml";

	/**
	 * @deprecated Move to LayoutResultController.
	 */
	private List<? extends SortKey> lastSortKeys = Collections.emptyList();
	
	LocaleInfo DEFAULT_LOCALE = LocaleDelegate.getInstance().getDefaultLocale();
	LocaleDelegate locale = LocaleDelegate.getInstance();

	protected final FileFilter filefilterLayoutml = new FileFilter() {
		@Override
		public boolean accept(File file) {
			return file.isDirectory() || file.getName().toLowerCase().endsWith(LAYOUTML_EXTENSION);
		}

		@Override
		public String getDescription() {
			return getSpringLocaleDelegate().getMessage("LayoutCollectController.12","LayoutML-Dateien (*.layoutml)");
		}
	};
	protected final FileFilter filefilterXml = new FileFilter() {
		@Override
		public boolean accept(File file) {
			return file.isDirectory() || file.getName().toLowerCase().endsWith(".xml");
		}

		@Override
		public String getDescription() {
			return getSpringLocaleDelegate().getMessage("LayoutCollectController.18","XML-Dateien (*.xml)");
		}
	};

	/**
 	 * @deprecated You should normally do sth. like this:<code><pre>
 	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
     */
	protected LayoutCollectController(NuclosEntity entity, MainFrameTab tabIfAny) {
		super(entity, tabIfAny, new LayoutResultController<CollectableMasterDataWithDependants>(
				entity.getEntityName(), new NuclosSearchResultStrategy<CollectableMasterDataWithDependants>()));

		this.setupDetailsToolBar();

		/** @todo The old temp files will be deleted by a shutdown hook. */
		// this.getFrame().addInternalFrameListener(new InternalFrameAdapter() {
		// public void internalFrameClosed(InternalFrameEvent ev) {
		// // pnlEdit.pnlXmlEditor.endEditing();
		// // performs cleanup (remove temp. file) in the XML editor
		// }
		// });
	}
	
	/**
	 * @deprecated Move to LayoutResultController.
	 */
	public List<? extends SortKey> getLastSortKeys() {
		return lastSortKeys;
	}
	
	/**
	 * @deprecated Move to LayoutResultController.
	 */
	public void setLastSortKeys(List<? extends SortKey> lastSortKeys) {
		this.lastSortKeys = lastSortKeys;
	}

	@Override
	protected void unsafeFillDetailsPanel(final CollectableMasterDataWithDependants clct) throws NuclosBusinessException {
		CollectableWYSIWYGLayoutEditor collectableComponent = ((CollectableWYSIWYGLayoutEditor)LayoutCollectController.this.getDetailsPanel().getEditView().getCollectableComponentsFor("layoutML").toArray()[0]);
		collectableComponent.setLayoutCollectController(LayoutCollectController.this);
		collectableComponent.setPreferences(getPreferences());

		WYSIWYGDetailsComponentModel wysiwygModel = (WYSIWYGDetailsComponentModel) LayoutCollectController.this.getDetailsPanel().getEditModel().getCollectableComponentModelFor("layoutML");
		wysiwygModel.setSendingCollectableFieldChangedInModel(false);
		if (clct.getDependants(getLayoutUsageEntity().getEntityName()) != null && !clct.getDependants(getLayoutUsageEntity().getEntityName()).isEmpty()) {
			setMetaInformation(wysiwygModel);
		} else {
			wysiwygModel.clearMetaInformation();
		}
		wysiwygModel.setSendingCollectableFieldChangedInModel(true);		
		
		super.unsafeFillDetailsPanel(clct);
	}

	protected abstract NuclosEntity getLayoutUsageEntity();

	public abstract void setMetaInformation(WYSIWYGDetailsComponentModel wysiwygModel);

	protected void setupDetailsToolBar() {
		//final JToolBar toolbarCustomDetails = UIUtils.createNonFloatableToolBar();

		final JButton btnTest = new JButton(Icons.getInstance().getIconTest());
		btnTest.setToolTipText(getSpringLocaleDelegate().getMessage("LayoutCollectController.14","Layout testen"));
		btnTest.setMnemonic('T');
		btnTest.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				cmdTestLayout();
			}
		});
		//toolbarCustomDetails.add(btnTest);
		this.getDetailsPanel().addToolBarComponent(btnTest);

		final JButton btnImport = new JButton(Icons.getInstance().getIconImport16());
		btnImport.setToolTipText(getSpringLocaleDelegate().getMessage("LayoutCollectController.11","Layout importieren"));
		btnImport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				cmdImport();
			}
		});
		//toolbarCustomDetails.add(btnImport);
		this.getDetailsPanel().addToolBarComponent(btnImport);

		final JButton btnExport = new JButton(Icons.getInstance().getIconExport16());
		btnExport.setToolTipText(getSpringLocaleDelegate().getMessage("LayoutCollectController.10","Layout exportieren"));
		btnExport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				cmdExport();
			}
		});
		//toolbarCustomDetails.add(btnExport);
		this.getDetailsPanel().addToolBarComponent(btnExport);

		//this.getDetailsPanel().setCustomToolBarArea(toolbarCustomDetails);
	}

	@Override
	protected void validate(CollectableMasterDataWithDependants clct) throws CommonBusinessException {
		super.validate(clct);

		try {
			this.parseLayoutML();
		} catch (IOException ex) {
			throw new NuclosFatalException(ex);
		} catch (NuclosAttributeNotFoundException ex){
			//NUCLEUSINT-399
			throw new CommonBusinessException(ex.getMessage());
		}
	}

	protected void cmdImport() {
		final JFileChooser filechooser = this.getFileChooser();
		final int iBtn = filechooser.showOpenDialog(this.getTab());
		if (iBtn == JFileChooser.APPROVE_OPTION) {
			final File file = filechooser.getSelectedFile();
			if (file != null) {
				this.getPreferences().put(PREFS_KEY_LASTIMPORTEXPORTPATH, file.getAbsolutePath());
				UIUtils.runCommand(this.getTab(), new Runnable() {
					@Override
					public void run() {
						try {
							importXml(file);
						} catch (IOException ex) {
							Errors.getInstance().showExceptionDialog(getTab(), 
									getSpringLocaleDelegate().getMessage(
											"LayoutCollectController.3","Beim Lesen der Datei ist ein Fehler aufgetreten."), ex);
						} catch (/* CommonBusiness */ Exception ex) {
							Errors.getInstance().showExceptionDialog(getTab(), ex.getMessage(), ex);
						}
					}
				});
			}
		}
	}

	protected void cmdExport() {
		/** @todo try...catch or UIUtils.runCommand */
		final JFileChooser filechooser = this.getFileChooser();
		final int iBtn = filechooser.showSaveDialog(this.getTab());
		if (iBtn == JFileChooser.APPROVE_OPTION) {
			assert filechooser.getSelectedFile() != null;
			String sPathName = filechooser.getSelectedFile().getAbsolutePath();
			if (!sPathName.toLowerCase().endsWith(LAYOUTML_EXTENSION) && !sPathName.toLowerCase().endsWith(".xml")) {
				sPathName += LAYOUTML_EXTENSION;
			}
			final File file = new File(sPathName);
			this.getPreferences().put(PREFS_KEY_LASTIMPORTEXPORTPATH, file.getAbsolutePath());

			boolean bDoExport = true;
			if (file.exists()) {
				final String sMessage = getSpringLocaleDelegate().getMessage(
						"LayoutCollectController.6","Die angegebene Datei (\"{0}\") existiert schon. Soll sie \u00fcberschrieben werden?", file.getName());
				final int iBtnConfirm = JOptionPane.showConfirmDialog(this.getTab(), sMessage, getSpringLocaleDelegate().getMessage(
						"LayoutCollectController.9","Layout-Export"), JOptionPane.OK_CANCEL_OPTION);
				bDoExport = (iBtnConfirm == JOptionPane.OK_OPTION);
			}
			if (bDoExport) {
				UIUtils.runCommand(this.getTab(), new Runnable() {
					@Override
					public void run() {
						try {
							exportXml(file);
						} catch (Exception ex) {
							Errors.getInstance().showExceptionDialog(getTab(), ex);
						}
					}
				});
			}
		}
	}

	/**
	 * Command: test current layout.
	 */
	private void cmdTestLayout() {
		UIUtils.runCommand(this.getTabbedPane().getComponentPanel(), new CommonRunnable() {
			@Override
			public void run() throws CommonBusinessException {
				final LayoutRoot layoutRoot;
				try {
					layoutRoot = parseLayoutML();
				} catch (IOException ex) {
					throw new NuclosFatalException(ex);
				} catch (NuclosAttributeNotFoundException ex){
					//NUCLEUSINT-399
					throw new CommonBusinessException(ex.getMessage());
				}

				setFieldInformation(layoutRoot.getCollectableComponents());

				Map<String, DetailsSubFormController<CollectableEntityObject>> mpsubformctl;
				try {
					// Note that we always use DetailsSubFormControllers - even
					// if the layout is used for Search:
					mpsubformctl = newDetailsSubFormControllersForLayout(getUsedEntityName(), layoutRoot);
				} catch (CommonBusinessException ex) {
					Errors.getInstance().showExceptionDialog(getTab(), ex);
					mpsubformctl = null;
				}

				final String sLayoutName = (String) getDetailsPanel().getEditModel().getCollectableComponentModelFor("name").getField().getValue();
				String sTitle = getSpringLocaleDelegate().getMessage("LayoutCollectController.13","LayoutML Test");
				if (sLayoutName != null) {
					sTitle += ": " + sLayoutName;
				}

				//NUCLEUSINT-285
				Dimension preferredSize = getDetailsPanel().getPreferredSize();

				final MainFrameTab newTab = Main.getInstance().getMainController().newMainFrameTab(null, sTitle);
				// ifrm.getContentPane().add(layoutRoot.getRootComponent());
				newTab.setLayeredComponent(layoutRoot.getRootComponent());
				UIUtils.ensureSize(newTab, preferredSize);
				//NUCLEUSINT-285
				UIUtils.ensureMinimumSize(newTab);
				getTabbedPane().add(newTab);
				newTab.setVisible(true);

				if (mpsubformctl != null) {
					final EntityAndFieldName eafnInitialFocus = layoutRoot.getInitialFocusEntityAndFieldName();
					if (eafnInitialFocus != null) {
						Utils.setInitialComponentFocus(eafnInitialFocus, layoutRoot, mpsubformctl, newTab, true);
					}
				}
			}
		});
	}

	/**
	 * parses the layoutml document contained in the editor.
	 *
	 * @return the parsed layoutml document.
	 * @throws CommonBusinessException
	 * @throws IOException
	 */
	protected abstract LayoutRoot parseLayoutML() throws CommonBusinessException, IOException;

	/**
	 * @return the entity name the layout is used for, as specified in the
	 *         layouts's usages. If there are more than one usages, the first
	 *         one is taken.
	 * @throws CommonBusinessException
	 *             if there are no usages defined for this layout.
	 * @postcondition result != null
	 */
	protected abstract String getUsedEntityName() throws CommonBusinessException;

	/**
	 * @return FileChooser for import/export
	 */
	protected final JFileChooser getFileChooser() {
		final String sCurrentDirectory = this.getPreferences().get(PREFS_KEY_LASTIMPORTEXPORTPATH, null);
		final JFileChooser result = new JFileChooser(sCurrentDirectory);
		result.addChoosableFileFilter(filefilterLayoutml);
		result.addChoosableFileFilter(filefilterXml);
		result.setFileFilter(filefilterLayoutml);

		return result;
	}

	protected final String getLayoutMLFromEditor() throws CollectableFieldFormatException {
		final String sFieldName = "layoutML";
		// The LayoutMLEditor might be inconsistent for two reasons:
		// 1. The user entered a value in one of the fields, but didn't leave
		// the field. In this case we want to retrieve
		// the new value (desired behavior).
		// 2. The layout was inserted into the database by calling
		// InitLayoutController. In this case, the textual format
		// might be different from the formatting of the LayoutMLEditor, which
		// means an inconsistency leading to a
		// call to CollectController.detailsChanged(). The effect of this is
		// switching to CollectState.EDIT.
		// This is undesired behavior, but we can live with it, as
		// InitLayoutController isn't used anymore.
		this.makeConsistent(false, sFieldName);
		final String result = (String) this.getDetailsPanel().getEditModel().getCollectableComponentModelFor(sFieldName).getField().getValue();

		assert result != null;
		return result;
	}

	/**
	 * imports an XML file, using the character encoding specified in the XML
	 * document.
	 *
	 * @param file
	 * @throws IOException
	 * @throws CommonBusinessException 
	 */
	private void importXml(File file) throws IOException, CommonBusinessException {
		final String sXml = IOUtils.readFromTextFile(file);
		final String sEncoding = XMLUtils.getXMLEncoding(sXml);
		final String sValue = sEncoding.equals(System.getProperty("file.encoding")) ? sXml : IOUtils.readFromTextFile(file, sEncoding);
		this.getDetailsPanel().getEditModel().getCollectableComponentModelFor("layoutML").setField(new CollectableValueField(sValue));
	}

	/**
	 * writes the XML document in the editor to the given file, using the
	 * character encoding specified in the XML document.
	 *
	 * @param file
	 * @throws IOException
	 * @throws CollectableFieldFormatException
	 */
	private void exportXml(File file) throws IOException, CollectableFieldFormatException {
		final String sLayoutML = this.getLayoutMLFromEditor();
		final String sEncoding = XMLUtils.getXMLEncoding(sLayoutML);
		final Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), sEncoding));
		w.write(sLayoutML);
		w.close();
		final String sMessage = "Written " + sLayoutML.length() + " characters to file " + file;
		LOG.info(sMessage);

		this.getDetailsPanel().setStatusBarText(sMessage);
	}

	/**
	 * sets text and tooltip text of the components to show the columns for text
	 * components and the minimum sizes for all components.
	 *
	 * @param collclctcomp
	 */
	protected static void setFieldInformation(Collection<CollectableComponent> collclctcomp) {
		for (CollectableComponent clctcomp : collclctcomp) {
			clctcomp.setToolTipText(getTooltipText(clctcomp));

			if (clctcomp instanceof CollectableTextField) {
				final CollectableTextField clcttf = (CollectableTextField) clctcomp;
				final CommonJTextField tf = clcttf.getJTextField();
				/**
				 * @todo This doesn't work if there is more than one
				 *       CollectableComponent for an attribute, as the text is
				 *       written to all CollectableComponents for that
				 *       attribute.
				 */
				tf.setText(Integer.toString(tf.getColumns()));
			} else if (clctcomp instanceof CollectableTextArea) {
				final CollectableTextArea clctta = (CollectableTextArea) clctcomp;
				final JTextArea ta = clctta.getJTextArea();
				ta.setText(Integer.toString(ta.getColumns()) + "," + Integer.toString(ta.getRows()));
			}
		}
	}

	private static String getTooltipText(CollectableComponent clctcomp) {
		final CollectableEntityField clctef = clctcomp.getEntityField();
		return "<html>" + "<b>"+SpringLocaleDelegate.getInstance().getMessage("LayoutCollectController.2","Attributname:")+"</b> " 
				+ clctcomp.getFieldName() + "<br>" + "<b>"
				+ SpringLocaleDelegate.getInstance().getMessage("LayoutCollectController.17","Min. Breite/H\u00f6he in Pixeln:")
				+ "</b> " + getText(clctcomp.getJComponent().getMinimumSize()) + "<br>" + "<b>"
				+ SpringLocaleDelegate.getInstance().getMessage("LayoutCollectController.15","Max. Breite/H\u00f6he in Pixeln:")
				+ "</b>" + getText(clctcomp.getJComponent().getMaximumSize()) + "<br>" + "<b>"
				+ SpringLocaleDelegate.getInstance().getMessage("LayoutCollectController.5","Bevorz. Breite/H\u00f6he in Pixeln:")
				+ "</b>" + getText(clctcomp.getJComponent().getPreferredSize()) + "<br>" + "<b>"
				+ SpringLocaleDelegate.getInstance().getMessage("LayoutCollectController.1","Anzeigename des Attributs:")+"</b> " 
				+ clctef.getLabel() + "<br>"
				+ "<b>"+ SpringLocaleDelegate.getInstance().getMessage("LayoutCollectController.16","Maximall\u00e4nge des Attributs:")
				+ "</b> " + LangUtils.defaultIfNull(LangUtils.toString(clctef.getMaxLength()), "&lt;keine&gt;") + "<br>" + "<b>"
				+ SpringLocaleDelegate.getInstance().getMessage("LayoutCollectController.8","Komponentenklasse:")
				+ "</b> " + clctcomp.getClass().getSimpleName() + "<br>" + "<b>"
				+ SpringLocaleDelegate.getInstance().getMessage("LayoutCollectController.4","Beschreibung des Attributs:")
				+ "</b><br>" + htmlFormatted(clctef.getDescription(), 60) + "</html>";
	}

	private static String htmlFormatted(String s, int iColumns) {
		return StringUtils.splitIntoSeparateLines(s, iColumns).replaceAll("\n", "<br>");
	}

	private static String getText(Dimension dim) {
		return "(" + dim.width + ", " + dim.height + ")";
	}

	protected final Map<String, DetailsSubFormController<CollectableEntityObject>> newDetailsSubFormControllersForLayout(String sParentEntityName, LayoutRoot layoutroot) {
		final Map<String, DetailsSubFormController<CollectableEntityObject>> result = CollectionUtils.newHashMap();
		for (SubForm subform : layoutroot.getMapOfSubForms().values()) {
			result.put(subform.getEntityName(), newDetailsSubFormControllerForLayout(subform, sParentEntityName, layoutroot));
		}
		return result;
	}

	/**
	 * @param subform
	 * @param sParentEntityName
	 * @param layoutroot
	 * @postcondition result != null
	 */
	private DetailsSubFormController<CollectableEntityObject> newDetailsSubFormControllerForLayout(SubForm subform, String sParentEntityName, LayoutRoot layoutroot) {
		// Note that we misuse the preferences root of the
		// LayoutCollectController for the subform controller's preferences.
		try {
			this.getPreferences().node("tmp").removeNode();
		} catch(BackingStoreException e) {
			LOG.info("newDetailsSubFormControllerForLayout failed: " + e);
		}
		final Preferences prefs = this.getPreferences().node("tmp");

		// if parent of subform is another subform, change given parent entity name
		String sParentSubForm = subform.getParentSubForm();
		if (sParentSubForm != null) {
			sParentEntityName = sParentSubForm;
		}

		return NuclosCollectControllerFactory.getInstance().newDetailsSubFormController(subform, sParentEntityName, layoutroot,
				this.getTab(), layoutroot.getRootComponent(), prefs, new EntityPreferences(), null);
	}

	@Override
	protected void cloneSelectedCollectable() throws CommonBusinessException {
		super.cloneSelectedCollectable();
	}
	
}	// class LayoutCollectController
