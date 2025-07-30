package app.breakpoint;

import com.sun.jdi.event.Event;
import com.sun.jdi.request.BreakpointRequest;

public class BreakpointWrapper {
	
	private BreakpointRequest breakpointRequest;
	private int repBefore;
	private int repetition;//TODO repetitions are not yet used

	public BreakpointWrapper(BreakpointRequest breakpointRequest, int repBefore, int repetition) {
		this.breakpointRequest = breakpointRequest;
		this.repBefore = repBefore;
		this.repetition = repetition;
	}

	public Object getBreakpointRequest() {
		return breakpointRequest;
	}

	/**
	 * Returns true iff this event should be resolved as a breakpoint
	 * @param event the event that could be resolved as a breakpoint
	 * @return true iff this event should be resolved as a breakpoint
	 */
	public boolean shouldStopAt(Event event) {
		boolean res = false;
		if(breakpointRequest.equals(event.request())) {
			res = repBefore == 0;
			repBefore--;
		}
		return res;
	}

}
