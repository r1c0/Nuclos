package org.nuclos.client.ui.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.nuclos.common.collection.CollectionUtils;

public class ViewIndex {
	
	private static final Logger LOG = Logger.getLogger(ViewIndex.class); 
	
	/**
	 * Mapping (model index) -> (view index).
	 */
	private final List<Integer> model2View = new LinkedList<Integer>();
	
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
			final Integer mi = removeFromViewByVi(oldViewIndex);
			assert mi == null 
				: "map(" + modelIndex + ", " + viewIndex + "): old view index " + oldViewIndex 
				+ " taken by " + mi + " on " + this;
		}
	}
	
	public void pushModelItem(int viewIndex) {
		incFromViewIndex(viewIndex);
		assert checkNewViewIndex(viewIndex);
		model2View.add(Integer.valueOf(viewIndex));
	}
	
	public void popModelItem() {
		final int oldModelIndex = model2View.size() - 1;
		removeFromViewByMi(oldModelIndex);
	}
	
	public void removeFromViewByMi(int modelIndex) {
		final Integer mi = removeFromViewByVi(model2View.get(modelIndex));
		// assert mi != null && mi.intValue() == modelIndex : "deleteModelItem(" + modelIndex + "): mi is " + mi;
	}
	
	/**
	 * Return the modelIndex removed but maybe null.
	 */
	public Integer removeFromViewByVi(int viewIndex) {
		Integer miToRemove = null; 
		int mi = 0;
		for (Integer vi: model2View) {
			if (vi != null) {
				int i = vi.intValue();
				if (i > viewIndex) {
					// dec viewIndex
					model2View.set(mi, Integer.valueOf(i - 1));
				}
				else if (i == viewIndex) {
					miToRemove = mi;
				}
			}
			++mi;
		}
		
		if (miToRemove != null) {
			if (miToRemove == model2View.size() - 1) {
				model2View.remove(miToRemove.intValue());
			}
			else {
				model2View.set(miToRemove.intValue(), null);
			}
			CollectionUtils.trimTail(model2View);
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
			if (vi != null) {
				int i = vi.intValue();
				if (i >= viewIndex) {
					model2View.set(mi, Integer.valueOf(i + 1));
					result = true;
				}
			}
			++mi;
		}
		return result;
	}
	
	private boolean checkNewViewIndex(int viewIndex) {
		final boolean result = 
			viewIndex >= 0 && !model2View.contains(Integer.valueOf(viewIndex)) && viewIndex - 1 <= maxViewIndex();
		if (!result)  LOG.warn("index " + viewIndex + " on " + this);
		return result;
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
