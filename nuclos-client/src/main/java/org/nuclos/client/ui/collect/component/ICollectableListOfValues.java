package org.nuclos.client.ui.collect.component;

import java.util.List;

import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;

/**
 * The minimal interface of Nuclos 'list of values' (LOV) feature.
 * <p> 
 * It represents a LOV <em>without</em> a corresponding CollectableComponent 
 * (and hence does not support the quick search). 
 * </p><p>
 * This is needed for object generation (working step) with parameter object.
 * In this case the current entity displayed is <em>not</em> connected with
 * an instance of the entity to choose the parameters from. Hence there is no
 * GUI component associated.
 * </p>  
 * @author Thomas Pasch
 * @since 3.1.01 
 */
public interface ICollectableListOfValues {

	int	QUICKSEARCH_MAX	= 256;
	
	/**
	 * Hint whether the lookup should be performed modal.
	 * @deprecated Probably not used in tabbed GUI any more,
	 * 		i.e. should always be Boolean.FALSE. (Thomas Pasch)
	 */
	String PROPERTY_MODAL_LOOKUP = "modalLookup";

	/**
	 * accepts the given <code>Collectable</code>, that was selected by the user in a lookup operation.
	 * Notifies all registered <code>LookupListener</code>s.
	 * @param clctLookedUp
	 * @precondition clctLookedUp != null
	 * @precondition clctLookedUp.isComplete()
	 */
	void acceptLookedUpCollectable(Collectable clctLookedUp);

	/**
	 * accepts the given <code>Collectable</code>, that was selected by the user in a lookup operation.
	 * Notifies all registered <code>LookupListener</code>s.
	 * @param clctLookedUp
	 * @param additionalCollectables
	 * @precondition clctLookedUp != null
	 * @precondition clctLookedUp.isComplete()
	 */
	void acceptLookedUpCollectable(Collectable clctLookedUp,
		List<Collectable> additionalCollectables);

	void addLookupListener(LookupListener listener);

	void removeLookupListener(LookupListener listener);

	// protected now
	// void fireLookupSuccessful(LookupEvent ev);

	CollectableSearchCondition getCollectableSearchCondition();
	
	boolean isSearchComponent();
	
	Object getProperty(String sName);

}
