package org.nuclos.server.eventsupport.ejb3;

import java.util.Collection;
import java.util.List;

import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.eventsupport.valueobject.EventSupportEventVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportVO;

/**
 * @author reichama
 *
 */
// @Remote
public interface EventSupportFacadeRemote 
{

	
	/**
	 * This Method returns all available Classes that can be used a EventSupport-Files
	 * due to the given interface(s)
	 * 
	 * @param liste - List of all interfaces to searched for
	 * @return
	 * @throws CommonPermissionException
	 */
	public Collection<EventSupportVO> getAllEventSupports() throws CommonPermissionException;
	
	public List<EventSupportVO> getAllEventSupportTypes() throws CommonPermissionException;
	
	public Collection<EventSupportVO> getEventSupportsByClasstype(List<Class<?>> listOfinterfaces)
			throws CommonPermissionException;

	public Collection<EventSupportEventVO> getAllEventSupportsForEntity(String entityname, String eventtype) throws CommonPermissionException;
}
