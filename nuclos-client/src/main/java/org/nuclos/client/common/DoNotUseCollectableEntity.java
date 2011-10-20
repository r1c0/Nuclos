package org.nuclos.client.common;

import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.collect.collectable.AbstractCollectableEntity;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.entityobject.CollectableEOEntityField;

/**
 * @deprecated This class shouldn't exists. Don't use it.
 * @author Thomas Pasch
 * @since Nuclos 3.2.0
 */
public class DoNotUseCollectableEntity extends AbstractCollectableEntity {

	/**
	 * @deprecated This class shouldn't exists. Don't use it.
	 */
	public DoNotUseCollectableEntity(String sName, String sLabel) {
		super(sName, sLabel);
		final MetaDataProvider mdProv = MetaDataClientProvider.getInstance();
		for (EntityFieldMetaDataVO ef: mdProv.getAllEntityFieldsByEntity(sName).values()) {
			final CollectableEOEntityField ce = new CollectableEOEntityField(ef, sName);
			addCollectableEntityField(ce);
		}
	}

}
