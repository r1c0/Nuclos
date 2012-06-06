package org.nuclos.client.ui.model;

import java.util.Collection;

import javax.swing.JList;
import javax.swing.ListModel;

public class ListModelUtils {
	
	private ListModelUtils() {
		// Never invoked.
	}
	
	public static int[] findIndices(ListModel model, Collection<?> objects) {
		final int len = model.getSize();
		final int[] r = new int[len];
		int j = 0;
		for (int i = 0; i < len; ++i) {
			final Object item = model.getElementAt(i);
			if (objects.contains(item)) {
				r[j++] = i;
			}
		}
		final int result[] = new int[j];
		System.arraycopy(r, 0, result, 0, j);
		return result;
	}
	
	public static void setSelected(JList jlist, Collection<?> objects) {
		final int[] indices = findIndices(jlist.getModel(), objects);
		jlist.setSelectedIndices(indices);
	}

}
