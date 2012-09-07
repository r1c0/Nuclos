package org.nuclos.client.eventsupport.model;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.jfree.util.Log;
import org.nuclos.client.eventsupport.EventSupportRepository;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.server.eventsupport.valueobject.EventSupportSourceVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportTransitionVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportVO;
import org.nuclos.server.statemodel.valueobject.StateTransitionVO;

public class EventSupportStatePropertiesTableModel extends EventSupportPropertiesTableModel {

	static final String COL_EVENTSUPPORT = SpringLocaleDelegate.getInstance().getMessage("EventSupportEntityPropertyModelColumn.4","EventSupport");
	static final String COL_TRANSITION = SpringLocaleDelegate.getInstance().getMessage("EventSupportEntityPropertyModelColumn.5","Transition");
	
	static final String[] COLUMNS = new String[] {COL_EVENTSUPPORT, COL_TRANSITION};
	
	final List<EventSupportTransitionVO> entries = new ArrayList<EventSupportTransitionVO>();
	final List<StateTransitionVO> transitions = new ArrayList<StateTransitionVO>();
			
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		boolean retVal = false;
		if (columnIndex == 1)
		{
			retVal = true;
		}
		return retVal;
	}
	
	public void addTransitions(List<StateTransitionVO> pTransitions) {
		transitions.clear();
		transitions.addAll(pTransitions);
	}
	
	public String[] getTransitionsAsArray() {
		String[] vals = new String[transitions.size()];
		int  idx = 0;
		for (StateTransitionVO svo : transitions) {
			vals[idx++] = createTransitionString(svo);
		}
		return vals;
	}
	public void addEntry(EventSupportTransitionVO eseVO)
	{
		entries.add(eseVO);
		fireTableRowsInserted(entries.size(), entries.size());
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object retVal = null;
		EventSupportTransitionVO eseVO = entries.get(rowIndex);
		
		switch (columnIndex) {
		case 0:
			retVal = eseVO.getEventSupportClass();
			
			EventSupportSourceVO ese;
			try {
				ese = EventSupportRepository.getInstance().getEventSupportByClassname(eseVO.getEventSupportClass());
				if (ese != null && ese.getName() != null) {
					retVal = ese.getName();
				}
			} catch (RemoteException e) {
				Log.error(e.getMessage(), e);
			}
			break;
		case 1:
			 for (StateTransitionVO svo : transitions)
			 {
				 if (svo.getId().equals(eseVO.getTransitionId())) {
					 retVal = createTransitionString(svo);					 
				 }
			 }
			break;
		default:
			break;
		}
		return retVal;
	}
	
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (aValue != null) 
		{
			 String value = (String) aValue;
			 if (columnIndex == 1)
			 {
				 for (StateTransitionVO svo : transitions)
				 {
					 String sTransName = createTransitionString(svo);
					 
					 if (sTransName.equals(value))
					 { 
						 entries.get(rowIndex).setTransitionId(svo.getId());
						 entries.get(rowIndex).setTransitionName(sTransName);
						 setModelModified(true);
						 break;
					 }
				 }				 
			 }
			 fireTableDataChanged();
		}
    }
	
	private static String createTransitionString(StateTransitionVO vo) {
		 String sTransName = "-> " + vo.getStateTarget();
		 if (vo.getStateSource() != null) {
			 sTransName = vo.getStateSource() + " " + sTransName;
		 }
		 return sTransName;
	}

	@Override
	public List<? extends EventSupportVO> getEntries() {
		return this.entries;
	}

	@Override
	public String[] getColumns() {
		return this.COLUMNS;
	}
	
	@Override
	public void addEntry(int rowId, EventSupportVO elm) {
		this.entries.add(rowId, (EventSupportTransitionVO) elm);
		fireTableRowsInserted(rowId, rowId);
	}
}
