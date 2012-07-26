//Copyright (C) 2010  Novabit Informationssysteme GmbH
//
//This file is part of Nuclos.
//
//Nuclos is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//Nuclos is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with Nuclos.  If not, see <http://www.gnu.org/licenses/>.

package org.nuclos.client.ui.resplan;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.io.Serializable;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.nuclos.client.ui.util.CommonTransferable;
import org.nuclos.client.ui.util.CommonTransferable.CommonDataFlavor;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collection.CollectionUtils;

public class ResPlanTransferHandler extends TransferHandler{

	public static class EntryWrapper implements Serializable {

		private final Object entry;
		
		public EntryWrapper(Object entry) {
			this.entry = entry;
		}
		
		public <E> E unwrap(Class<E> clazz) {
			if (clazz.isInstance(entry)) {
				// we checked ourselves, so an "unchecked" cast is ok and faster
				return (E) entry;
			}
			return null;
		}
	}
	
	public static CommonDataFlavor<EntryWrapper> RESPLAN_ENTRY_FLAVOR = new CommonDataFlavor<EntryWrapper>(
			EntryWrapper.class, "Resource plan entry");
	
	@Override
	public void exportAsDrag(JComponent comp, InputEvent e, int action) {
		super.exportAsDrag(comp, e, action);
	}
	
	@Override
	public int getSourceActions(JComponent c) {
		return MOVE | COPY;
	}
	
	@Override
	protected Transferable createTransferable(JComponent c) {
		if (c instanceof JResPlanComponent<?, ?, ?, ?>) {
			return createResPlanTransferable((JResPlanComponent) c);
		} else {
			return super.createTransferable(c);
		}
	}
	
	protected <R, T extends Comparable<T>, E, L> Transferable createResPlanTransferable(JResPlanComponent<R, T, E, L> resPlan) {
		E entry = CollectionUtils.getFirst(resPlan.getSelectedEntries());
		if (entry != null) {
			// TODO_RESPLAN: should interval/duration part of the wrapper ???
//			Interval<T> interval = resPlan.getModel().getInterval(entry);
//			long duration = resPlan.getTimeModel().getDuration(interval.getStart(), interval.getEnd());
			EntryWrapper wrapper = new EntryWrapper(entry);
			CommonTransferable<EntryWrapper> transferable = new CommonTransferable<EntryWrapper>(RESPLAN_ENTRY_FLAVOR, wrapper);
			transferable.registerToStringFlavor();
			return transferable;
		}
		return null;		
	}
	
	@Override
	protected void exportDone(JComponent source, Transferable data, int action) {
		if (action == MOVE) {
			JResPlanComponent resPlan = (JResPlanComponent) source;
			if (data.isDataFlavorSupported(RESPLAN_ENTRY_FLAVOR)) {
				try {
					EntryWrapper wrapper = RESPLAN_ENTRY_FLAVOR.extractTransferData(data);
					resPlan.getModel().removeEntry(wrapper.entry);
				} catch (UnsupportedFlavorException e) {
					throw new NuclosFatalException(e);
				} catch (IOException e) {
					throw new NuclosFatalException(e);
				}
			}
		}
	}
	
	@Override
	public boolean canImport(TransferSupport support) {
		if (support.isDataFlavorSupported(RESPLAN_ENTRY_FLAVOR)) {
			JResPlanComponent<?, ?, ?, ?> resPlan = (JResPlanComponent<?, ?, ?, ?>) support.getComponent();
			return getTargetResource(resPlan, support) != null && getTargetInterval(resPlan, support) != null;
		} else {
			return super.importData(support);
		}
	}
	
	@Override
	public boolean importData(TransferSupport support) {
		if (support.isDataFlavorSupported(RESPLAN_ENTRY_FLAVOR)) {
			JResPlanComponent<?, ?, ?, ?> resPlan = (JResPlanComponent<?, ?, ?, ?>) support.getComponent();
			return importData(resPlan, support);
		} else {
			return super.importData(support);
		}
	}
	
	protected <R, T extends Comparable<? super T>, E, L> boolean importData(JResPlanComponent<R, T, E, L> resPlan, TransferSupport support) {
		R resource = getTargetResource(resPlan, support);
		Interval<T> interval = getTargetInterval(resPlan, support);
		if (resource != null && interval != null) {
			ResPlanModel<R, T, E, L> resPlanModel = resPlan.getModel();
			try {
				EntryWrapper wrapper = RESPLAN_ENTRY_FLAVOR.extractTransferData(support.getTransferable());
				E entry = wrapper.unwrap(resPlanModel.getEntryType());
				switch (support.getUserDropAction()) {
				case MOVE:
					if (resPlanModel.isUpdateEntryAllowed(entry)) {
						resPlanModel.updateEntry(entry, resource, interval);
						// Return false because we just update the entry internally (it's not a real Transferable import).
						return false;
					}
					break;
				case COPY:
					if (resPlanModel.isCreateEntryAllowed()) {
						resPlan.getModel().createEntry(resource, interval, entry);
						return true;
					}
					break;
				}
			} catch (UnsupportedFlavorException e) {
				throw new NuclosFatalException(e);
			} catch (IOException e) {
				throw new NuclosFatalException(e);
			}
		}
		return false;
	}

	protected <R> R getTargetResource(JResPlanComponent<R, ?, ?, ?> resPlan, TransferSupport support) {
		if (support.isDrop()) {
			return resPlan.getResourceAt(support.getDropLocation().getDropPoint());
		}
		return null;
	}
	
	protected <T extends Comparable<? super T>> Interval<T> getTargetInterval(JResPlanComponent<?, T, ?, ?> resPlan, TransferSupport support) {
		if (support.isDrop()) {
			Interval<T> interval = resPlan.getDropInterval();
			if (interval == null) {
				interval = resPlan.getTimeIntervalAt(support.getDropLocation().getDropPoint());
			}
			return interval;
		}
		return null;
	}
}
