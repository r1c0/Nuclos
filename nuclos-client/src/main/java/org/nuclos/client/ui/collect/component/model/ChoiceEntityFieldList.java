package org.nuclos.client.ui.collect.component.model;

import java.util.Set;

import org.nuclos.client.ui.model.ChoiceList;
import org.nuclos.common.collect.collectable.CollectableEntityField;

/**
 * Encapsulates the lists of available and selected fields, resp.
 * The selected fields are shown as columns in the result table.
 * The selected fields are always in sync with the table column model, but not necessarily
 * with the table model's columns.
 */
public class ChoiceEntityFieldList extends ChoiceList<CollectableEntityField> {
	
	private Set<CollectableEntityField> fixed;
	
	public ChoiceEntityFieldList(Set<CollectableEntityField> fixed) {
		super();
		this.fixed = fixed;
	}
	
	public Set<CollectableEntityField> getFixed() {
		return fixed;
	}

}	// inner class Fields
