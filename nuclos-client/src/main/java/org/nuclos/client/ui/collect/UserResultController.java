package org.nuclos.client.ui.collect;

import java.util.ArrayList;
import java.util.List;

import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.client.masterdata.user.UserCollectController;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;

public class UserResultController<Clct extends CollectableMasterDataWithDependants> extends ResultController<Clct> {
	
	public UserResultController() {
	}

	/**
	 * @deprecated Remove this.
	 */
	@Override
	public List<CollectableEntityField> getFieldsAvailableForResult(CollectableEntity clcte) {
		final List<CollectableEntityField> result = new ArrayList<CollectableEntityField>();
		for (CollectableEntityField cef : super.getFieldsAvailableForResult(clcte)) {
			if (!UserCollectController.FIELD_PREFERENCES.equals(cef.getName()) && !UserCollectController.FIELD_PASSWORD.equals(cef.getName())) {
				result.add(cef);
			}
		}
		return result;
	}
}
