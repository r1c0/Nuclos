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
package org.nuclos.client.layout.wysiwyg;

import java.util.Date;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.nuclos.client.layout.admin.LayoutCollectController;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.collect.component.AbstractCollectableComponent;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelEvent;
import org.nuclos.client.ui.collect.component.model.DetailsComponentModel;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.common2.layoutml.LayoutMLConstants;
import org.xml.sax.SAXException;

/**
 *
 * This is the CollectableComponent for the WYSIWYG Editor.
 *
 * It is assigned with layoutml to the Masks for editing the Layouts for
 * MasterData and GenericObjects.
 *
 * It does support only Layouts using TableLayout as Layoutmanager. There is a
 * downward compatiblity for old Layouts. If an old Layout is loaded
 * {@link CollectableWYSIWYGLayoutEditor#checkIfWYSIWYGLayout(String)} the old
 * Xerlin Editor is launched and replaces the WYSIWYG Editor.
 *
 *
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class CollectableWYSIWYGLayoutEditor extends AbstractCollectableComponent {

	private static final Logger LOG = Logger.getLogger(CollectableWYSIWYGLayoutEditor.class);

	private WYSIWYGLayoutControllingPanel jpnWYSIWYGController;

	private WYSIWYGLayoutEditorChangeDescriptor chgDescriptor;

	private static final Logger log = Logger.getLogger(CollectableWYSIWYGLayoutEditor.class);

	/**
	 * ctor for dynamic construction (especially in a LayoutML document).
	 *
	 * @param clctef
	 * @param bSearchable
	 */
	public CollectableWYSIWYGLayoutEditor(CollectableEntityField clctef, Boolean bSearchable) {
		super(clctef, new WYSIWYGLayoutControllingPanel(new WYSIWYGMetaInformation()), bSearchable.booleanValue());
		this.setModel(new WYSIWYGDetailsComponentModel(clctef));

		this.jpnWYSIWYGController = (WYSIWYGLayoutControllingPanel) getJComponent();
		this.chgDescriptor = new WYSIWYGLayoutEditorChangeDescriptor(this);
		this.jpnWYSIWYGController.setEditorChangeDescriptor(this.chgDescriptor);

		if (clctef.getJavaClass() != String.class) {
			throw new NuclosFatalException("String erwartet.");
		}

		this.getWYSIWYGEditorPanel().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent ev) {
				try {
					CollectableWYSIWYGLayoutEditor.this.viewToModel();
				} catch (CollectableFieldFormatException ex) {
					throw new NuclosFatalException("Fehler beim Lesen vom WYSIWYG Layout-Editor.", ex);
				}
			}
		});
	}

	/**
	 * This method is protected as it's important not to call methods of the
	 * XMLEditorPanel from outside, as this can mean changing the state of the
	 * editor without notifying this <code>CollectableComponent</code>.
	 *
	 * @return the panel
	 * changed to public by CM: to fetch the list of resource ids
	 */
	public WYSIWYGLayoutControllingPanel getWYSIWYGEditorPanel() {
		return this.jpnWYSIWYGController;
	}

	/**
	 * This Method is called to recieve the LayoutML from the Editor. It
	 * switches when the Xerlin is active, because it does contain the Layout in
	 * this time.
	 *
	 * @return CollectableField
	 * @throws org.nuclos.common.collect.exception.CollectableFieldFormatException
	 */
	@Override
	public CollectableField getFieldFromView() throws CollectableFieldFormatException {
		String layoutML = null;;
		try {
			layoutML = getWYSIWYGEditorPanel().getLayoutML().trim(); //NUCLEUSINT-430 trim
		} catch (CommonValidationException e) {

		} catch (CommonFatalException ex) {
			throw new CollectableFieldFormatException(ex);
		}
		if (log.isDebugEnabled()) {
			log.debug("######### GENERATING LayoutML START #########");
			log.debug(layoutML);
			log.debug("######### GENERATING LayoutML END   #########");
		}
		return new CollectableValueField(layoutML);
	}

	@Override
	protected void updateView(CollectableField clctfValue) {
		final Object oValue = clctfValue.getValue();
		if (oValue == null)
			this.jpnWYSIWYGController.getMetaInformation().setCollectableEntity(null);
		if (this.jpnWYSIWYGController.getMetaInformation().isMetaInformationSet()) {
			if (oValue == null) {
				jpnWYSIWYGController.clearEditorPanel();
				jpnWYSIWYGController.createStandartLayout();
				jpnWYSIWYGController.showPaletteAndEditorPanel();
			} else {
				String layoutML = (String) oValue;
				layoutML = layoutML.trim(); //NUCLEUSINT-430 trim
				String oldOne = null;
				try {
					oldOne = getWYSIWYGEditorPanel().getLayoutML();
					jpnWYSIWYGController.clearEditorPanel();
					// NUCLEUSINT-314 swapping the editors for the different
					// layouttypes
					if (checkIfWYSIWYGLayout(layoutML)) {
						if (log.isDebugEnabled()) {
							log.debug("######### LOADING LayoutML START #########");
							log.debug(layoutML);
						}

						Date startDate = new Date();
						getWYSIWYGEditorPanel().setLayoutML(layoutML);
						Date endDate = new Date();
						jpnWYSIWYGController.clearUndoRedoStack();
						if (log.isDebugEnabled()) {
							log.debug("######### LOADING LayoutML END   #########");
						}
						jpnWYSIWYGController.showPaletteAndEditorPanel();

					} else {
						// old xerlin layout

						JOptionPane.showMessageDialog(jpnWYSIWYGController, 
								getSpringLocaleDelegate().getText("layoutml.editor.unsupported.wysiwyg.layout.2", null), 
								getSpringLocaleDelegate().getText("layoutml.editor.unsupported.wysiwyg.layout.1", null), 
								JOptionPane.INFORMATION_MESSAGE);
						jpnWYSIWYGController.showPaletteAndEditorPanel();
						jpnWYSIWYGController.createStandartLayout();
					}
				} catch (CommonBusinessException ex) {
					getWYSIWYGEditorPanel().clearEditorPanel();
					JOptionPane.showConfirmDialog(getWYSIWYGEditorPanel(), ex.getMessage());
					jpnWYSIWYGController.showPaletteAndEditorPanel();
				} catch (SAXException e) {
					getWYSIWYGEditorPanel().clearEditorPanel();
					try {
						if(oldOne != null)
							getWYSIWYGEditorPanel().setLayoutML(oldOne);
						else
							getWYSIWYGEditorPanel().createStandartLayout();
                    }
                    catch(Exception e1) {
            			LOG.info("updateView: " + e);
	                    getWYSIWYGEditorPanel().createStandartLayout();
                    }
					//NUCLEUSINT-398
					if (e.getMessage().indexOf("must be followed by either attribute specifications, \">\" or \"/>\"") != -1 )
						Errors.getInstance().showExceptionDialog(this.getJComponent(), new CommonBusinessException(WYSIWYGStringsAndLabels.ERROR_MESSAGES.XML_MISSING_CLOSETAG));
					else
						Errors.getInstance().showExceptionDialog(this.getJComponent(), new CommonBusinessException(WYSIWYGStringsAndLabels.ERROR_MESSAGES.INVALID_LAYOUTML));
					jpnWYSIWYGController.showPaletteAndEditorPanel();
				}
			}
		} else {
			jpnWYSIWYGController.clearEditorPanel();
			jpnWYSIWYGController.createStandartLayout();
			jpnWYSIWYGController.showWaitingPanel();
		}
		this.getWYSIWYGEditorPanel().invalidate();
	}


	@Override
	public void setInsertable(boolean bInsertable) {
	}

	@Override
	public void setVisible(boolean bVisible) {
		this.getWYSIWYGEditorPanel().setVisible(bVisible);
	}

	@Override
	public void setEnabled(boolean bEnabled) {
		this.getWYSIWYGEditorPanel().setEnabled(bEnabled);
	}

	@Override
	public void setViewLocked(boolean bViewLocked) {
		super.setViewLocked(bViewLocked);
	}

	public void fireEditorChanged() {
		((WYSIWYGDetailsComponentModel) this.getModel()).fireEditorChanged();
	}

	public void setLayoutCollectController(LayoutCollectController layoutCollectController) {
		jpnWYSIWYGController.setLayoutCollectController(layoutCollectController);
	}

	@Override
	public void setPreferences(Preferences prefs) {
		jpnWYSIWYGController.setPreferences(prefs.node("WYSIWYGEditor"));
	}

	/**
	 * NUCLEUSINT-314 method for checking if layout is an old one or a new one
	 * (new layouts only contain tablelayouts...)
	 */
	private boolean checkIfWYSIWYGLayout(String layoutml) {
		String[] blacklistLayoutTypes = {LayoutMLConstants.ELEMENT_BORDERLAYOUT, LayoutMLConstants.ELEMENT_BOXLAYOUT, LayoutMLConstants.ELEMENT_COLUMNLAYOUT, LayoutMLConstants.ELEMENT_FLOWLAYOUT, LayoutMLConstants.ELEMENT_GRIDBAGLAYOUT, LayoutMLConstants.ELEMENT_GRIDLAYOUT, LayoutMLConstants.ELEMENT_ROWLAYOUT};

		for (String blacklistedLayout : blacklistLayoutTypes) {
			if (layoutml.indexOf(blacklistedLayout) != -1)
				return false;
		}
		return true;
	}

	/**
	 * This class is needed for notifying the ParentFrame that something
	 * changed.
	 *
	 *
	 * @author maik.stueker
	 */
	public class WYSIWYGLayoutEditorChangeDescriptor {

		private CollectableWYSIWYGLayoutEditor clctWYSIWYGEditor;

		public WYSIWYGLayoutEditorChangeDescriptor(CollectableWYSIWYGLayoutEditor clctWYSIWYGEditor) {
			this.clctWYSIWYGEditor = clctWYSIWYGEditor;
		}

		public synchronized void setContentChanged() {
			clctWYSIWYGEditor.setViewLocked(true);
			clctWYSIWYGEditor.fireEditorChanged();
			clctWYSIWYGEditor.setViewLocked(false);
		}
	}

	/**
	 * This model extends {@link DetailsComponentModel} for the usage in
	 * {@link CollectableWYSIWYGLayoutEditor} The main extension is the usage of
	 * {@link WYSIWYGMetaInformation}.
	 *
	 * @author maik.stueker
	 */
	public class WYSIWYGDetailsComponentModel extends DetailsComponentModel {

		private boolean isSendingCollectableFieldChangedInModel = false;

		public WYSIWYGDetailsComponentModel(CollectableEntityField clctef) {
			super(clctef);
		}

		public void setCollectableEntity(CollectableEntity entity) {
			jpnWYSIWYGController.getMetaInformation().setCollectableEntity(entity);
			fireEditorChanged();
		}

		public void fireEditorChanged() {
			helper.fireCollectableFieldChanged(this, null, null);
		}

		public boolean isSendingCollectableFieldChangedInModel() {
			return isSendingCollectableFieldChangedInModel;
		}

		public void setSendingCollectableFieldChangedInModel(boolean isSendingCollectableFieldChangedInModel) {
			this.isSendingCollectableFieldChangedInModel = isSendingCollectableFieldChangedInModel;
		}

		public void clearMetaInformation() {
			fireEditorChanged();
		}
	}

	@Override
	public void collectableFieldChangedInModel(CollectableComponentModelEvent ev) {
		if (this.getModel() instanceof WYSIWYGDetailsComponentModel) {
			if (((WYSIWYGDetailsComponentModel)this.getModel()).isSendingCollectableFieldChangedInModel()) {
				super.collectableFieldChangedInModel(ev);
			}
		} else {
			super.collectableFieldChangedInModel(ev);
		}
	}

	@Override
	protected void viewToModel() throws CollectableFieldFormatException {
		getWYSIWYGEditorPanel().safePendingPropertyChanges();
		super.viewToModel();
	}



}
