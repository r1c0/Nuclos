package org.nuclos.client.ui.model;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JList;
import javax.swing.ListModel;

public class ListModelUtils {
	
	private ListModelUtils() {
		// Never invoked.
	}
	
	public static int[] findIndices(ListModel model, Iterable<?> objects) {
		final int len = model.getSize();
		final int[] r = new int[len];
		int j = 0;
		
		for (int i = 0; i < len; ++i) {
			final Object item = model.getElementAt(i);
			boolean contains = false;
			for (Object o: objects) {
				if (o.equals(item)) {
					contains = true;
					break;
				}
			}
			if (contains) {
				r[j++] = i;
			}
		}
		final int result[] = new int[j];
		System.arraycopy(r, 0, result, 0, j);
		return result;
	}
	
	public static <T> int[] findIndices(ListModel model, Iterable<? extends T> objects, Comparator<T> comp) {
		final int len = model.getSize();
		final int[] r = new int[len];
		int j = 0;
		for (int i = 0; i < len; ++i) {
			final T item = (T) model.getElementAt(i);
			boolean contains = false;
			for (T o: objects) {
				if (comp.compare(o, item) == 0) {
					contains = true;
					break;
				}
			}
			if (contains) {
				r[j++] = i;
			}
		}
		final int result[] = new int[j];
		System.arraycopy(r, 0, result, 0, j);
		return result;
	}
	
	public static void setSelected(JList jlist, Iterable<?> objects) {
		final int[] indices = findIndices(jlist.getModel(), objects);
		jlist.setSelectedIndices(indices);
	}
	
	public static <T> void setSelected(JList jlist, Iterable<? extends T> objects, Comparator<T> comp) {
		final int[] indices = findIndices(jlist.getModel(), objects, comp);
		jlist.setSelectedIndices(indices);
	}
	
	public static void adjustForAllItem(JList jlist, Object all, boolean identity) {
		final ListModel lm = jlist.getModel();
		final int idx = findIndex(lm, all, identity);
		if (idx < 0) {
			return;
		}
		final int size = jlist.getModel().getSize();
		final Map<Integer,Object> selectedMap = selectedMap(jlist);
		final Integer idxInt = Integer.valueOf(idx);
		if (selectedMap.containsKey(idxInt)) {
			// all is selected -> select all items
			final int[] allIndices = allIndices(size);
			jlist.setSelectedIndices(allIndices);			
		}
		else {
			// all is not selected
			if (selectedMap.size() == size - 1) {
				// select all item if all others are selected
				final int[] allIndices = allIndices(size);
				jlist.setSelectedIndices(allIndices);
			}
		}
	}
	
	public static int findIndex(ListModel lm, Object item, boolean identity) {
		final int len = lm.getSize();
		int result = -1;
		for (int i = 0; i < len; ++i) {
			final Object o = lm.getElementAt(i);
			boolean isAll = false;
			if (identity) {
				isAll = o == item;
			}
			else {
				isAll = o.equals(item);
			}
			if (isAll) {
				result = i;
				break;
			}
		}
		return result;
	}
	
	public static <T> Map<Integer,T> selectedMap(JList jlist) {
		final ListModel lm = jlist.getModel();
		final Map<Integer, T> result = new HashMap<Integer, T>();
		for (int i: jlist.getSelectedIndices()) {
			result.put(Integer.valueOf(i), (T) lm.getElementAt(i));
		}
		return result;
	}
	
	public static int[] allIndices(int size) {
		final int[] allIndices = new int[size];
		for (int i = 0; i < size; ++i) {
			allIndices[i] = i;
		}
		return allIndices;
	}

}
