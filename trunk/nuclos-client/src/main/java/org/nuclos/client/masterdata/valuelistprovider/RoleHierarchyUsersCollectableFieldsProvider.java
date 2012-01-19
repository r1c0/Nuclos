package org.nuclos.client.masterdata.valuelistprovider;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.nuclos.client.main.Main;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.valuelistprovider.cache.CacheableCollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

public class RoleHierarchyUsersCollectableFieldsProvider implements CacheableCollectableFieldsProvider {

	public RoleHierarchyUsersCollectableFieldsProvider(){
	}

	@Override
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		List<CollectableField> lstUsers = CollectionUtils.transform(MasterDataDelegate.getInstance().getUserHierarchy(Main.getMainController().getUserName()), new Transformer<MasterDataVO, CollectableField>() {
			@Override
			public CollectableField transform(MasterDataVO user) {
				return new CollectableValueIdField(user.getId(), user.getField("lastname")+", "+user.getField("firstname"));
			}
		});
		Set<CollectableField> setUsers = new TreeSet<CollectableField>(new Comparator<CollectableField>(){
			@Override
			public int compare(CollectableField o1, CollectableField o2) {
				return o1.getValue().toString().compareTo(o2.getValue().toString());
			}
		});
		setUsers.addAll(lstUsers);
		ArrayList<CollectableField> arrayList = new ArrayList<CollectableField>();
		arrayList.addAll(setUsers);
		return arrayList;
	}

	@Override
	public void setParameter(String sName, Object oValue) {
	}

	@Override
	public Object getCacheKey() {
		return "RoleHierarchyUsersCollectableFieldsProvider";
	}
}
