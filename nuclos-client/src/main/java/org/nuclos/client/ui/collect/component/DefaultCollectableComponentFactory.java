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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang.NullArgumentException;
import org.nuclos.client.common.NuclosCollectableImage;
import org.nuclos.common.collect.collectable.CollectableComponentTypes;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common2.exception.CommonFatalException;

/**
 * default <code>CollectableComponentFactory</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 * @see CollectableComponentFactory#setInstance(CollectableComponentFactory)
 */
public class DefaultCollectableComponentFactory extends CollectableComponentFactory {

	/**
	 * creates a <code>CollectableComponent</code> for the given entity field. The "control type" may be given as a hint.
	 * @param clctef
	 * @param clctcomptype may be <null>, which has the same effect as new CollectableComponentType(null, null).
	 * @return a suitable <code>CollectableComponent</code> for the given entity field.
	 * @precondition clctef != null
	 */
	@Override
	public CollectableComponent newCollectableComponent(CollectableEntityField clctef,
			CollectableComponentType clctcomptype, boolean bSearchable) {
		if (clctef == null) {
			throw new NullArgumentException("clctef");
		}
		if (clctcomptype == null) {
			clctcomptype = new CollectableComponentType(null, null);
		}
		CollectableComponent result;
		final Class<CollectableComponent> clsclctcomp = clctcomptype.getControlTypeClass();
		if (clsclctcomp == null) {
			result = newCollectableComponentByEnumeratedControlType(clctef, clctcomptype.getEnumeratedControlType(),
					bSearchable);
		}
		else {
			assert clctcomptype.getEnumeratedControlType() == null;
			try {
				final Constructor<CollectableComponent> ctor = clsclctcomp.getConstructor(CollectableEntityField.class, Boolean.class);
				result = ctor.newInstance(clctef, bSearchable);

				respectRestrictionToValueList(result, clctef);
			}
			catch (NoSuchMethodException ex) {
				try {
					final Constructor<CollectableComponent> ctor = clsclctcomp.getConstructor(CollectableEntityField.class, boolean.class);
					result = ctor.newInstance(clctef, bSearchable);

					respectRestrictionToValueList(result, clctef);
				}
				catch (NoSuchMethodException ex2) {
					final String sMessage = "The class " + clsclctcomp + " has not the required contructor ctor(CollectableEntityField, Boolean/boolean).";
						//"Die Klasse " + clsclctcomp + " hat nicht den erforderlichen Konstruktor ctor(CollectableEntityField, Boolean).";
					throw new CommonFatalException(sMessage, ex2);
				}
				catch (InstantiationException ex2) {
					throw new CommonFatalException(ex2);
				}
				catch (IllegalAccessException ex2) {
					throw new CommonFatalException(ex2);
				}
				catch (InvocationTargetException ex2) {
					throw new CommonFatalException(ex2.getTargetException());
				}
			}
			catch (InstantiationException ex) {
				throw new CommonFatalException(ex);
			}
			catch (IllegalAccessException ex) {
				throw new CommonFatalException(ex);
			}
			catch (InvocationTargetException ex) {
				throw new CommonFatalException(ex.getTargetException());
			}
		}
		return result;
	}

	/**
	 * creates a <code>CollectableComponent</code> for the given entity field. The "control type" may be given as a hint.
	 * @param clctef
	 * @param iEnumeratedControlType may be given as a hint which component to create. May be <code>null</code>, in which
	 * case the entity field's defaultCollectableComponentType is used.
	 * @return a suitable <code>CollectableComponent</code> for the given entity field.
	 * @see CollectableComponentTypes
	 */
	protected CollectableComponent newCollectableComponentByEnumeratedControlType(CollectableEntityField clctef, Integer iEnumeratedControlType,
			boolean bSearchable) {
		if (clctef == null) {
			throw new NullArgumentException("clctef");
		}

		/** @todo Naming convention: how to distinguish between int and Integer? (ii for int is stupid) */
		final int iiControlType = (iEnumeratedControlType != null) ? iEnumeratedControlType.intValue() : clctef.getDefaultCollectableComponentType();

		final CollectableComponent result = this.newCollectableComponentByEnumeratedControlType(clctef, iiControlType, bSearchable);

		respectRestrictionToValueList(result, clctef);

		return result;
	}

	/**
	 * creates a <code>CollectableComponent</code> for the given entity field, depending on the control type.
	 * Override this method if you want to use custom components in your application.
	 * @param clctef
	 * @param iiControlType @see CollectableComponentTypes
	 * @param bSearchable
	 * @return the newly created collectable component
	 */
	protected CollectableComponent newCollectableComponentByEnumeratedControlType(CollectableEntityField clctef, int iiControlType, boolean bSearchable) {
		final CollectableComponent result;

		switch (iiControlType) {
			case CollectableComponentTypes.TYPE_TEXTFIELD:
				result = new CollectableTextField(clctef, bSearchable);
				break;
			case CollectableComponentTypes.TYPE_COMBOBOX:
				result = new CollectableComboBox(clctef, bSearchable);
				break;
			case CollectableComponentTypes.TYPE_TEXTAREA:
				result = new CollectableTextArea(clctef, bSearchable);
				break;
			case CollectableComponentTypes.TYPE_CHECKBOX:
				result = new CollectableCheckBox(clctef, bSearchable);
				break;
			case CollectableComponentTypes.TYPE_DATECHOOSER:
				result = new CollectableDateChooser(clctef, bSearchable);
				break;
			case CollectableComponentTypes.TYPE_HYPERLINK:
				result = new CollectableHyperlink(clctef, bSearchable);
				break;
			case CollectableComponentTypes.TYPE_EMAIL:
				result = new CollectableEmail(clctef, bSearchable);
				break;
			case CollectableComponentTypes.TYPE_OPTIONGROUP:
				result = new CollectableOptionGroup(clctef, bSearchable);
				break;
			case CollectableComponentTypes.TYPE_LISTOFVALUES:
				result = new CollectableListOfValues(clctef, bSearchable);
				break;
			case CollectableComponentTypes.TYPE_IDTEXTFIELD:
				result = new CollectableIdTextField(clctef, bSearchable);
				break;
			case CollectableComponentTypes.TYPE_IMAGE:
				result = new NuclosCollectableImage(clctef);
				break;
			case CollectableComponentTypes.TYPE_PASSWORDFIELD:
				//NUCLEUSINT-1142
				result = new CollectablePasswordField(clctef, bSearchable);
				break;
			case CollectableComponentTypes.TYPE_SCRIPT:
				result = new CollectableScriptComponent(clctef, bSearchable);
				break;
			default:
				throw new IllegalArgumentException("iEnumeratedControlType");
		}
		return result;
	}

	/**
	 * respects the "restrictedToValueList" property of the entity field for non-searchable components.
	 * @param result
	 * @param clctef
	 */
	private static void respectRestrictionToValueList(CollectableComponent result, CollectableEntityField clctef) {
		if (!result.isSearchComponent()) {
			result.setInsertable(!(clctef.isIdField() && clctef.isRestrictedToValueList()));
		}
	}

}  // class DefaultCollectableComponentFactory
