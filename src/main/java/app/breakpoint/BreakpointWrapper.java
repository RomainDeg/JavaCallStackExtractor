package app.breakpoint;

import com.sun.jdi.request.BreakpointRequest;

public class BreakpointWrapper {
	
	private BreakpointRequest breakpointRequest;
	private int repBefore;
	private int repetition;

	public BreakpointWrapper(BreakpointRequest breakpointRequest, int repBefore, int repetition) {
		this.breakpointRequest = breakpointRequest;
		this.repBefore = repBefore;
		this.repetition = repetition;
	}

	public Object getBreakpointRequest() {
		return breakpointRequest;
	}

}
