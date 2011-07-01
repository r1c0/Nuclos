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

import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * Provides access to the <code>Collectable</code>s to be collected in a <code>CollectController</code>.
 * This is an approach to encapsulate all data (or model) access related methods in one interface.
 * Implementations of these methods must be side effect free (apart from the effects specified, respectively) and
 * must not contain UI related or controller related code. Especially, it's not allowed to pop up dialog boxes here.
 * <br>
 * There are, however, situations when a specific controller needs to get additional data or additional options needed
 * for performing the operation. For example: delete physically or mark as deleted. To have the methods in the
 * CollectController itself makes things much simpler here.
 * Another example is in DatasourceCollectController.updateCurrentCollectable(). The user must confirm the update in
 * some cases here. Asking the user definitely doesn't belong to the model. Another issue here: In multi edit mode
 * (not implemented for data sources currently) it would be bad to ask the user for each single object. It would be
 * necessary to have an "Yes for all"/"No for all" option here. It's unclear yet how to model such a behavior in a clean way.
 * <br>
 * Note that from a practical point of view, we can implement a CollectModel as inner class in a specific CollectController
 * and still access all members of the controller. But that is clearly not what a model is supposed to be like. From a
 * practical point of view, however, the CollectController works as it is, so there would be no gain from this change.
 * <br>
 * So, as long as these problems aren't solved, we will not go the "model" way - and this class is deprecated.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 * @deprecated It's not clear yet if things will work this way.
 */
@Deprecated
public interface CollectModel<Clct extends Collectable> {

	CollectableEntity getEntity();

	Clct insertCollectable(Clct clctNew) throws CommonBusinessException;

	Clct updateCollectable(Clct clct, Object oAdditionalData) throws CommonBusinessException;

	void deleteCollectable(Clct clct) throws CommonBusinessException;

	Clct newCollectable();

}	// interface CollectModel
