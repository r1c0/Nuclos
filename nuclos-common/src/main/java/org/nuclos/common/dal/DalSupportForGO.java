package org.nuclos.common.dal;

import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;

/**
 * Common functions to transform a GenericObjectVO into a EntityObjectVO and vice versa.
 * 
 * @author Thomas Pasch
 * @since Nuclos 3.1.01
 */
public class DalSupportForGO {
	
	private DalSupportForGO() {
		// Never invoked.
	}

	public static GenericObjectVO eovo2Govo(EntityObjectVO eovo) {
		final GenericObjectVO result = new GenericObjectVO(
				IdUtils.unsafeToId(eovo.getId()), null, null, null);
		return result;
	}
	
	public static EntityObjectVO govo2Eovo(GenericObjectVO govo, String entity) {
		final EntityObjectVO result = new EntityObjectVO();
		result.setChangedAt(InternalTimestamp.toInternalTimestamp(govo.getChangedAt()));
		result.setChangedBy(govo.getChangedBy());
		result.setCreatedAt(InternalTimestamp.toInternalTimestamp(govo.getChangedAt()));
		result.setCreatedBy(govo.getCreatedBy());
		result.setEntity(entity);
		result.setId(IdUtils.toLongId(govo.getId()));
		result.setVersion(govo.getVersion());
		return result;
	}

}
