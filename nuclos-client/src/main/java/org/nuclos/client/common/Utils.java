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

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import org.nuclos.client.genericobject.CollectableGenericObjectWithDependants;
import org.nuclos.client.genericobject.GenericObjectDelegate;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.CollectableMasterData;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.ui.collect.CollectableComponentsProvider;
import org.nuclos.client.ui.collect.DefaultEditView;
import org.nuclos.client.ui.collect.EditView;
import org.nuclos.client.ui.collect.ResultController;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.caching.NBCache;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityProvider;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.common.masterdata.MakeMasterDataValueIdField;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.EntityAndFieldName;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Utility methods for Nucleus client.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class Utils {

	private static final Logger log = Logger.getLogger(Utils.class);
	
	private static final String FIELDNAME_ACTIVE = "active";
	private static final String FIELDNAME_VALIDFROM = "validFrom";
	private static final String FIELDNAME_VALIDUNTIL = "validUntil";
	
	private Utils() {
	}

	/**
	 * @deprecated use <code>EntityObjectDelegate.getCollectableFieldsByName</code>
	 * 
	 * get masterdata for the given entity as collectable fields.
	 * @param sEntityName
	 * @param collmdvo
	 * @param sFieldName masterdata field name
	 * @param bCheckValidity Test for active sign and validFrom/validUntil
	 * @return list of collectable fields
	 */
	@Deprecated
	public static synchronized List<CollectableField> getCollectableFieldsByName(String sEntityName,
			Collection<MasterDataVO> collmdvo, String sFieldName, boolean bCheckValidity) {

		final Collection<MasterDataVO> collmdvoFiltered = bCheckValidity ? selectValidAndActive(sEntityName, collmdvo) : collmdvo;

		return CollectionUtils.transform(collmdvoFiltered, new MakeMasterDataValueIdField(sFieldName));
	}

	/**
	 * Tests whether the entity has {@code validFrom}/{@code validTo} or {@code active} fields.
	 */
	public static boolean hasValidOrActiveField(String entity) {
		final Collection<String> collFieldNames = DefaultCollectableEntityProvider.getInstance().getCollectableEntity(entity).getFieldNames();
		final boolean bContainsActive = collFieldNames.contains(FIELDNAME_ACTIVE);
		final boolean bContainsValidFromAndUntil = collFieldNames.contains(FIELDNAME_VALIDFROM) &&
				collFieldNames.contains(FIELDNAME_VALIDUNTIL);
		return bContainsActive || bContainsValidFromAndUntil;
	}

	/**
	 * selects the valid and/or active (if applicable) entries from the given Collection.
	 * @param sEntity
	 * @param collmdvo
	 * @return Collection<MasterDataVO>
	 * @todo define Predicate MasterDataVO.IsValidAndActive
	 */
	private static Collection<MasterDataVO> selectValidAndActive(String sEntity, Collection<MasterDataVO> collmdvo) {
		final List<MasterDataVO> result = new ArrayList<MasterDataVO>();

		final Collection<String> collFieldNames = DefaultCollectableEntityProvider.getInstance().getCollectableEntity(sEntity).getFieldNames();
		final boolean bContainsActive = collFieldNames.contains(FIELDNAME_ACTIVE);
		final boolean bContainsValidFromAndUntil = collFieldNames.contains(FIELDNAME_VALIDFROM) &&
				collFieldNames.contains(FIELDNAME_VALIDUNTIL);
		final Date dateNow = new Date(System.currentTimeMillis());

		for (MasterDataVO mdvo : collmdvo) {
			// separate valid entries...
			boolean bAddToResult = true;
			if (bContainsActive) {
				final Boolean bActive = (Boolean) mdvo.getField(FIELDNAME_ACTIVE);
				bAddToResult = (bActive != null) && bActive.booleanValue();
			}
			if (bAddToResult) {
				if (bContainsValidFromAndUntil) {
					final Date dateValidFrom = (Date) mdvo.getField(FIELDNAME_VALIDFROM);
					final Date dateValidUntil = (Date) mdvo.getField(FIELDNAME_VALIDUNTIL);

					bAddToResult =
							(dateValidFrom == null || dateValidFrom.before(dateNow)) &&
									(dateValidUntil == null || dateValidUntil.after(dateNow));
				}
			}
			if (bAddToResult) {
				result.add(mdvo);
			}
		}
		return result;
	}

	/**
	 * prepares the given <code>Collectable</code> for saving:
	 * Booleans that are <code>null</code> are mapped to <code>false</code>.
	 * @param clct
	 * @param clcte
	 * @precondition clct != null
	 * @precondition clct.isComplete()
	 * @precondition clcte != null
	 */
	public static void prepareCollectableForSaving(Collectable clct, CollectableEntity clcte) {
		for (String sFieldName : clcte.getFieldNames()) {
			final CollectableEntityField clctef = clcte.getEntityField(sFieldName);
			if (clctef.getJavaClass().equals(Boolean.class) && clct.getField(sFieldName).isNull()) {
				clct.setField(sFieldName, new CollectableValueField(Boolean.FALSE));
			}
		}
	}

	/**
	 * prepares the given dependants for saving:
	 * Booleans that are <code>null</code> are mapped to <code>false</code>.
	 */
	public static void prepareDependantsForSaving(DependantCollectableMasterDataMap mpDependants) {
		for (String sEntityName : mpDependants.getEntityNames()) {
			final CollectableEntity clcte = NuclosCollectableEntityProvider.getInstance().getCollectableEntity(sEntityName);
			for (CollectableMasterData clctmd : mpDependants.getValues(sEntityName)) {
				prepareCollectableForSaving(clctmd, clcte);
			}
		}
	}

	/**
	 * @param coll
	 * @return the common object, if any, based on equals. If all objects in the given Collection are equal, the first object is returned.
	 * Otherwise, <code>null</code> is returned.
	 * @precondition coll != null
	 */
	public static <E> E getCommonObject(Collection<E> coll) {
		if (coll == null) {
			throw new NullArgumentException("coll");
		}
		E result = null;
		for (E e : coll) {
			if (result == null) {
				result = e;
			}
			else if (!result.equals(e)) {
				result = null;
				break;
			}
		}
		return result;
	}

	/**
	 * sets the names for all fields in the given object that are <code>java.awt.Component</code>s.
	 * @todo move to org.nuclos.client.ui.UIUtils eventually.
	 */
	public static void setComponentNames(ComponentNameSetter o) {
		setComponentNames(o, o.getClass().getDeclaredFields());
	}

	private static void setComponentNames(ComponentNameSetter o, Field[] aDeclaredFields) {
		for (Field field : aDeclaredFields) {
			if (Component.class.isAssignableFrom(field.getType())) {
				o.setComponentName(field);
			}
		}
	}

	/**
	 * Read a comma separated String (R,G,B) from the parameter provider and convert it into a color if possible.
	 * @param sParameter
	 * @return the newly created color or Color.BLACK if not possible to read the parameter
	 */
	public static Color translateColorFromParameter(String sParameter) {
		Color result = Color.BLACK;
		final String sColor = ClientParameterProvider.getInstance().getValue(sParameter);
		if (sColor != null) {
			final String[] saColors = sColor.split(",");
			if (saColors.length == 3) {
				result = new Color(
						Integer.parseInt(saColors[0]),
						Integer.parseInt(saColors[1]),
						Integer.parseInt(saColors[2]));
			}
		}
		return result;
	}

	public static void setInitialComponentFocus(EditView editView, Map<String, ? extends SubFormController> mpsubformctl) {
		if ((editView instanceof DefaultEditView) && ((DefaultEditView) editView).getInitialFocusField() != null) {
			// frame can be null if bShowWarnings is false
			setInitialComponentFocus(((DefaultEditView) editView).getInitialFocusField(), editView, mpsubformctl, null, false);
		}
	}

	/**
	 * sets the input focus to a certain collectable component in a LayoutML mask.
	 * @param eafnInitialFocus entity name and field name of the component that is to receive to focus. The entity name is
	 * null, if the component is not in a subform.
	 * @param clctcompprovider map of all collectable components in the layout
	 * @param mpsubformctl map of all subformcontrollers
	 * @param frame frame of the layout (for possible warning dialogs only)
	 * @param bShowWarnings displays warnings for components not found for focussing
	 * @precondition eafnInitialFocus != null
	 */
	public static void setInitialComponentFocus(EntityAndFieldName eafnInitialFocus,
			final CollectableComponentsProvider clctcompprovider, final Map<String, ? extends SubFormController> mpsubformctl,
			final MainFrameTab frame, final boolean bShowWarnings) {

		final String sInitialFocusEntityName = eafnInitialFocus.getEntityName();
		final String sInitialFocusFieldName = eafnInitialFocus.getFieldName();

		// Must be invoked later, else focus is not set with compound components like LOVs
		EventQueue.invokeLater(new Runnable() {
			@Override
            public void run() {
				if (sInitialFocusEntityName == null) {
					if (sInitialFocusFieldName != null) {
						final Collection<CollectableComponent> collclctcomp = clctcompprovider.getCollectableComponentsFor(sInitialFocusFieldName);
						if (collclctcomp.isEmpty()) {
							if (bShowWarnings) {
								final String sMessage = CommonLocaleDelegate.getMessage("ClientUtils.1", "Das angegebene Feld f\u00fcr den initialen Fokus existiert nicht.");
								JOptionPane.showMessageDialog(frame, sMessage, CommonLocaleDelegate.getMessage("ClientUtils.2", "Hinweis"), JOptionPane.WARNING_MESSAGE);
							}
						}
						else {
							final CollectableComponent clctcomp = collclctcomp.iterator().next();
							final JComponent compFocus = clctcomp.getFocusableComponent();
							compFocus.requestFocusInWindow();
						}
					}
				}
				else {
					final SubFormController subformctl = mpsubformctl.get(sInitialFocusEntityName);
					if (subformctl != null) {
						final SubForm.SubFormTableModel subformtblmdl = (SubForm.SubFormTableModel) subformctl.getSubForm().getJTable().getModel();

						final JTable tbl = subformctl.getSubForm().getJTable();
						final int iColumn = tbl.convertColumnIndexToView(subformtblmdl.findColumnByFieldName(sInitialFocusFieldName));
						if (iColumn != -1) {
							if (subformtblmdl.getRowCount() > 0) {
								tbl.editCellAt(0, iColumn);

								Component comp = tbl.getCellEditor().getTableCellEditorComponent(tbl, tbl.getValueAt(0, iColumn), true, 0, iColumn);

								// Special case for multiline text editor components
								if (comp instanceof JScrollPane) {
									comp = ((JScrollPane) comp).getViewport().getView();
								}
								comp.requestFocusInWindow();
							}
						}
						else {
							if (bShowWarnings) {
								final String sMessage = CommonLocaleDelegate.getMessage("ClientUtils.3", "Das angegebene Feld in der Entit\u00e4t f\u00fcr den initialen Fokus existiert nicht.");
								JOptionPane.showMessageDialog(frame, sMessage, CommonLocaleDelegate.getMessage("ClientUtils.2", "Hinweis"), JOptionPane.WARNING_MESSAGE);
							}
						}
					}
					else {
						if (bShowWarnings) {
							final String sMessage = CommonLocaleDelegate.getMessage("ClientUtils.4", "Die angegebene Entit\u00e4t f\u00fcr den initialen Fokus existiert nicht.");
							JOptionPane.showMessageDialog(frame, sMessage, CommonLocaleDelegate.getMessage("ClientUtils.2", "Hinweis"), JOptionPane.WARNING_MESSAGE);
						}
					}
				}
			}
		});
	}

	/**
	 * Returns a {@code Collectable} object for the given entity and id. 
	 * This method works for master data as well as generic objects.
	 * @param id id (valid types are Integer or Long)
	 * @throws CommonFinderException
	 */
	@SuppressWarnings("deprecation")
	public static Collectable getCollectable(String entityName, Object id) throws CommonFinderException {
		if (entityName == null)
			throw new IllegalArgumentException("entityName is null");
		if (!(id == null || id instanceof Integer || id instanceof Long)) {
			throw new IllegalArgumentException("id must be Integer or Long");
		}
		final Integer intId = (id != null) ? ((Number) id).intValue() : null;
		Collectable clct = null;
		if (intId != null) {
			try {
				if (Modules.getInstance().existModule(entityName)) {
					Integer moduleId = Modules.getInstance().getModuleIdByEntityName(entityName);
					if (moduleId != null) {
						GenericObjectVO govo = GenericObjectDelegate.getInstance().get(moduleId, intId);
						clct = CollectableGenericObjectWithDependants.newCollectableGenericObject(govo);
					}
				} else {
					MasterDataMetaVO metaData = MasterDataDelegate.getInstance().getMetaData(entityName);
					CollectableMasterDataEntity clcte = new CollectableMasterDataEntity(metaData);
					MasterDataVO mdvo = MasterDataDelegate.getInstance().get(entityName, intId);
					clct = new CollectableMasterData(clcte, mdvo);
				}
			} catch (CommonBusinessException ex) {
				throw new CommonFinderException("Cannot lookup " + entityName + " #" + intId, ex);
			}
		}
		return clct;
	}
	
	public static class CollectableLookupProvider implements NBCache.LookupProvider<Object, Collectable> {
		private final String entityName;
		
		public CollectableLookupProvider(String entityName) {
			this.entityName = entityName;
		}
		@Override
		public Collectable lookup(Object key) {
			try {
				return getCollectable(entityName, key);
			} catch (CommonFinderException e) {
				throw new NuclosFatalException(e);
			}
		}
	}
	
	public static List<CollectableEntityField> createCollectableEntityFieldListFromFieldNames(ResultController<?> ctl, CollectableEntity clcte, List<String> lstSelectedFieldNames) {
		assert lstSelectedFieldNames != null;
		final List<CollectableEntityField> result = new ArrayList<CollectableEntityField>();
		for (String sFieldName : lstSelectedFieldNames) {
			try {
				result.add(ctl.getCollectableEntityFieldForResult(clcte, sFieldName));
			}
			catch (Exception ex) {
				// ignore unknown fields
				log.warn("Ein Feld mit dem Namen \"" + sFieldName + "\" ist nicht in der Entit\u00e4t " + clcte.getName() + " enthalten.", ex);
			}
		}
		return result;
	}

}	// class Utils

