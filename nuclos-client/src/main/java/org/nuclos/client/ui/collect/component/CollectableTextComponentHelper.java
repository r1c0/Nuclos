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

import javax.swing.text.JTextComponent;

import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldFormat;
import org.nuclos.common.collect.collectable.CollectableUtils;
import org.nuclos.common.collect.collectable.searchcondition.AtomicCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIsNullCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableLikeCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common2.StringUtils;

/**
 * Contains common behavior from CollectableTextComponent and CollectableTextArea, which used to be no
 * CollectableTextComponent.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 * @deprecated move all functionality to CollectableTextComponent or AbstractCollectableComponent
 */
@Deprecated
public class CollectableTextComponentHelper {

	private CollectableTextComponentHelper() {
	}

	static CollectableField write(JTextComponent tc, CollectableEntityField clctef)
			throws CollectableFieldFormatException {
		// Text components cannot distinguish between null and "". By convention,
		// a CollectableTextComponent that is empty writes a CollectableField that isNull():
		return write(tc.getText(), clctef);
	}

	public static CollectableField write(String sText, CollectableEntityField clctef) throws CollectableFieldFormatException {
		/** @todo make the following comment part of the interface */
		/** @todo consider making this part of the CollectableFieldFormat interface. At the moment,
		 * "" is mapped to "" rather than to null for Strings. */
		// Text components cannot distinguish between null and "". By convention,
		// a CollectableTextComponent that is empty writes a CollectableField that isNull():

		final Object oValue = CollectableFieldFormat.getInstance(clctef.getJavaClass()).parse(clctef.getFormatOutput(),
				StringUtils.nullIfEmpty(sText));

		return CollectableUtils.newCollectableFieldForValue(clctef, oValue);
	}


	/**
	 * @param clctef
	 * @param compop
	 * @param clctcompValue
	 * @param sLikeComparand the comparand to use for LIKE conditions.
	 * @return
	 * @throws CollectableFieldFormatException
	 * @precondition compop != null
	 * @postcondition (compop == ComparisonOperator.NONE) <--> (result == null)
	 */
	public static AtomicCollectableSearchCondition getAtomicSearchConditionFromView(CollectableEntityField clctef,
			ComparisonOperator compop, CollectableComponent clctcompValue, String sLikeComparand)
			throws CollectableFieldFormatException {

		final AtomicCollectableSearchCondition result;
		switch (compop) {
			case NONE:
				result = null;
				break;
			case IS_NULL:
			case IS_NOT_NULL:
				result = new CollectableIsNullCondition(clctef, compop);
				break;
			case LIKE:
			case NOT_LIKE:
				// the 'like' comparand may never be null. For convenience, we convert null to "" here:
				result = new CollectableLikeCondition(clctef, compop, StringUtils.emptyIfNull(sLikeComparand));
				break;
			default:
				final CollectableField clctfComparand = clctcompValue.getFieldFromView();
				if (!clctfComparand.isNull()) {
					result = new CollectableComparison(clctef, compop, clctfComparand);
				}
				else {
					// special case: empty operand
					switch (compop) {
						case EQUAL:
							// The user should not have to distinguish between 'is null' and '== ""' for Strings:
							result = new CollectableIsNullCondition(clctef, ComparisonOperator.IS_NULL);
							break;
						case NOT_EQUAL:
							// The user should not have to distinguish between 'is not null' and '!= ""' for Strings:
							result = new CollectableIsNullCondition(clctef, ComparisonOperator.IS_NOT_NULL);
							break;
						default: {
							/** @todo the conditions for invalid conditions ;) shouldn't be hardcoded HERE. */
							// As Oracle does not distinguish between null and '', a comparison like "field op ''" doesn't make sense.
							// We cannot even transform null into "" as this makes no difference to Oracle.
//							final String sMessage = "Ung\u00fcltige Bedingung im Feld \"" + clctef.getLabel() + "\". " +
//									"Der Vergleichswert darf nicht leer sein.\n" +
//									"Bitte geben Sie einen Vergleichswert an oder verwenden Sie den Vergleichsoperator \"ist leer\" bzw. \"ist nicht leer\".";
							throw new CollectableFieldFormatException(StringUtils.getParameterizedExceptionMessage("collectable.textcomponent.exception", clctef.getLabel()));
						}
					}
				}
		}
		assert (compop == ComparisonOperator.NONE) == (result == null);
		return result;
	}

}	// class CollectableTextComponentHelper
