package org.nuclos.client.customcode;

import org.nuclos.common.collect.collectable.AbstractCollectableBean;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.server.customcode.valueobject.CodeVO;

public class CollectableCode extends AbstractCollectableBean<CodeVO> {
	
	public CollectableCode(CodeVO cvo) {
		super(cvo);
	}

	@Override
	public Object getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getIdentifierLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected CollectableEntity getCollectableEntity() {
		// TODO Auto-generated method stub
		return null;
	}
}
