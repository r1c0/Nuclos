package org.nuclos.client.ui.collect.component;

import java.util.List;

import javax.swing.JComponent;
import javax.swing.event.EventListenerList;

import org.apache.log4j.Logger;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;

/**
 * The 'list of value' (LOV) representation used for object generation
 * (working step) with parameter object.
 * <p>
 * If the working step is triggered, the parameter entity is displayed as overlay.
 * The representation is controlled by its own CollectController. The user can chose
 * from the list of (parameter) entity instances. The LOV returns the instance
 * chosen.
 * </p><p>
 * This implementation of LOV is always a search component. The LOV is <em>not</em>
 * associated with a corresponding GUI component on the triggering tab. Hence it is
 * <em>not</em> a CollectableComponent.
 * </p>
 * @author Thomas Pasch
 * @since 3.1.01
 */
public class EntityListOfValues implements ICollectableListOfValues {

	private static final Logger LOG = Logger.getLogger(EntityListOfValues.class);

	private final EventListenerList	listeners	= new EventListenerList();

	private final JComponent eventSrc;

	/**
	 * Constructor.
	 *
	 * @param eventSrc EventObjects need a non-null source of the event, normally
	 * 		representing the (GUI) component the event stems from. In this is case
	 * 		this is rather esoteric - as this LOV is <em>not</em> associated with
	 * 		a corresponding GUI component.
	 */
	public EntityListOfValues(JComponent eventSrc) {
		this.eventSrc = eventSrc;
	}

	@Override
	public void acceptLookedUpCollectable(Collectable clctLookedUp) {
		acceptLookedUpCollectable(clctLookedUp, null);
	}

	@Override
	public void acceptLookedUpCollectable(Collectable clctLookedUp,
		List<Collectable> additionalCollectables) {
		fireLookupSuccessful(clctLookedUp, additionalCollectables);
	}

	@Override
	public void addLookupListener(LookupListener listener) {
		listeners.add(LookupListener.class, listener);
	}

	@Override
	public void removeLookupListener(LookupListener listener) {
		listeners.remove(LookupListener.class, listener);
	}

	/**
	 * notifies all registered LookupListeners
	 */
	protected void fireLookupSuccessful(Collectable chosen, List<Collectable> multiple) {
		LookupEvent ev = null;
		// Guaranteed to return a non-null array
		Object[] l = listeners.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for(int i = l.length - 2; i >= 0; i -= 2) {
			if(l[i] == LookupListener.class) {
				// lazy creation of event
				if (ev == null) {
					ev = new LookupEvent(eventSrc, chosen, multiple);
				}
				((LookupListener) l[i + 1]).lookupSuccessful(ev);
			}
		}
	}

	@Override
	public CollectableSearchCondition getCollectableSearchCondition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSearchComponent() {
		return false;
	}

	@Override
	public Object getProperty(String sName) {
		if(PROPERTY_MODAL_LOOKUP.equals(sName)) {
			return Boolean.FALSE;
		}
		return null;
	}

}
