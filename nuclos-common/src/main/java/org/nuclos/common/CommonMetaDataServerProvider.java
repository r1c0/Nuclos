package org.nuclos.common;

import java.util.Map;

import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.PivotInfo;

/**
 * Common <em>server</em> interface implemented by MetaData Delegates, Facades, and Providers. 
 * 
 * @author Thomas Pasch
 * @since Nuclos 3.1.06
 */
public interface CommonMetaDataServerProvider extends CommonMetaDataProvider {

	/**
	 * Return all (pseudo) fields/columns that can be accessed in a subform used as pivot table. 
	 * 
	 * @param  info The subform and key of the pivot table. <p><b>Attention:</b> The value part of PivotInfo is
	 * 		<em>ignored</em> (together with the value type).</p>
	 * @return All (pseudo) fields as mapping (fieldname -> field meta data). <p><b>Attention:</b> The field meta
	 * 		data contains the info with <code>null</code> as value and value type. In general this is 
	 * 		<em>unsuited</em> for client use.
	 */
    Map<String, EntityFieldMetaDataVO> getAllPivotEntityFields(PivotInfo info);

}
