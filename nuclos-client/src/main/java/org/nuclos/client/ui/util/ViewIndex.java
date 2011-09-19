package org.nuclos.client.ui.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ViewIndex {
	
	/**
	 * Mapping (model index) -> (view index).
	 */
	final List<Integer> model2View = new LinkedList<Integer>();
	
	public ViewIndex() {
	}
	
	public ViewIndex(int size) {
		for (int i = 0; i < size; ++i) {
			final Integer v = Integer.valueOf(i);
			model2View.add(v);
		}
	}
	
	/**
	 * may return null.
	 */
	public Integer getViewIndex(int modelIndex) {
		if (modelIndex >= size()) return null;
		return model2View.get(modelIndex);
	}
	
	/**
	 * may return -1.
	 */
	public int getModelIndex(int viewIndex) {
		return model2View.indexOf(Integer.valueOf(viewIndex));
	}
	
	public int size() {
		return model2View.size();
	}
	
	public Iterator<Integer> iterator() {
		return model2View.iterator();
	}
	
	public void map(int modelIndex, int viewIndex) {
		Integer oldViewIndex = null;
		if (modelIndex >= model2View.size()) {
			final boolean changed = incFromViewIndex(viewIndex);
			// assert !changed;
			assert checkNewViewIndex(viewIndex);
			model2View.add(modelIndex, Integer.valueOf(viewIndex));
		}
		else {
			// may be null
			oldViewIndex = model2View.get(modelIndex);
			if (oldViewIndex != null) {
				int ovi = oldViewIndex.intValue();
				if (ovi != viewIndex) {
					incFromViewIndex(viewIndex);		
				}
				else {
					// same value is set -> do nothing
					return;
				}
			}
			assert checkNewViewIndex(viewIndex);
			model2View.set(modelIndex, Integer.valueOf(viewIndex));
		}
		
		if (oldViewIndex != null) {
			final Integer mi = removeFromView(oldViewIndex);
			assert mi == null;
		}
	}
	
	public void pushModelItem(int viewIndex) {
		incFromViewIndex(viewIndex);
		assert checkNewViewIndex(viewIndex);
		model2View.add(Integer.valueOf(viewIndex));
	}
	
	public void popModelItem() {
		final int oldModelIndex = model2View.size() - 1;
		deleteModelItem(oldModelIndex);
	}
	
	public void deleteModelItem(int modelIndex) {
		final Integer mi = removeFromView(model2View.get(modelIndex));
		assert mi != null && mi.intValue() == modelIndex;
	}
	
	/**
	 * Return the modelIndex but maybe null.
	 */
	public Integer removeFromView(int viewIndex) {
		Integer miToRemove = null; 
		int mi = 0;
		for (Integer vi: model2View) {
			if (vi == null) continue;
			int i = vi.intValue();
			if (i > viewIndex) {
				// dec viewIndex
				model2View.set(mi, Integer.valueOf(i - 1));
			}
			else if (i == viewIndex) {
				miToRemove = mi;
			}
			++mi;
		}
		
		if (miToRemove != null) {
			model2View.remove(miToRemove.intValue());
		}
		return miToRemove;
	}
	
	/**
	 * Return true if ViewIndex has changed.
	 */
	private boolean incFromViewIndex(int viewIndex) {
		boolean result = false;
		int mi = 0;
		for (Integer vi: model2View) {
			if (vi == null) continue;
			int i = vi.intValue();
			if (i >= viewIndex) {
				model2View.set(mi, Integer.valueOf(i + 1));
				result = true;
			}
			++mi;
		}
		return result;
	}
	
	private boolean checkNewViewIndex(int viewIndex) {
		return viewIndex >= 0 && !model2View.contains(Integer.valueOf(viewIndex)) && viewIndex - 1 <= maxViewIndex();   
	}
	
	private int maxViewIndex() {
		int result = -1;
		for (Integer vi: model2View) {
			if (vi == null) continue;
			int i = vi.intValue();
			if (result < i) result = i;
		}
		return result;
	}
	
	public String toString() {
		return "ViewIndex" + model2View;
	}

}
