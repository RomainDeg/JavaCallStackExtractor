package app;

import java.io.IOException;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;

import app.breakpoint.BreakpointWrapper;

public class VmManager {

	VirtualMachine vm;
	
	public VmManager(VirtualMachine vm) {
		this.vm = vm;
	}

	public void resumeThread(String threadName) {
		this.getThreadNamed(threadName).resume();
	}
	

	/**
	 * Returns the thread with the chosen name if one exist in the given VM
	 * 
	 * @param vm the virtual machine where the thread is supposed to be
	 * @return the thread with the chosen name if one exist in the given VM
	 * @throws IllegalStateException if no thread can be found
	 */
	public ThreadReference getThreadNamed(String threadName) {
		ThreadReference main = null;
		for (ThreadReference thread : vm.allThreads()) {
			if (thread.name().equals(threadName)) {
				main = thread;
				break;
			}
		}
		if (main == null) {
			throw new IllegalStateException("No thread nammed " + threadName + "was found");
		}
		return main;
	}

	
	/**
	 * Waiting for the vm to stop at a break point
	 * 
	 * @param bkWrap the wrapper for the breakpoint searched
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void waitForBreakpoint(BreakpointWrapper bkWrap) throws InterruptedException, IOException {

		EventSet eventSet = null;
		boolean stop = false;

		while ((!stop && (eventSet = vm.eventQueue().remove()) != null)) {
			for (Event event : eventSet) {
				if (event instanceof VMDeathEvent) {
					throw new IllegalStateException("Thread has terminated without encountering the wanted breakpoint");
				} else if (event instanceof VMDisconnectEvent) {
					throw new IllegalStateException("VM has been disconected");
				} else if (event instanceof BreakpointEvent) {
					// BreakPoint attained we can stop here
					// TODO make this able to stop on a count or on  
					stop = event.request().equals(bkWrap.getBreakpointRequest());
					break;
				} else {
					// Not noticeable now
					// TODO by looking at the type hierarchy of Event it will be possible to take steps into accounts
				}
			}
		}
	}
	
	/**
	 * Dispose of the VM to make sure the vm disconnect properly
	 */
	public void disposeVM() {
		vm.dispose();
	}

}
