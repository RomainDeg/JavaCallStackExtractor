package app;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;

import app.breakpoint.BreakPointInstaller;
import app.breakpoint.BreakpointWrapper;
import app.csExtractors.CallstackExtractor;
import app.vmAttach.JDIAttach;

public class JDIAttachingExtractor {
	
	private JsonNode config;

	public JDIAttachingExtractor(JsonNode config) {
		this.config = config;
	}

	/**
	 * Extract the callstack, using the given configuration
	 * @throws IOException 
	 * @throws IllegalConnectorArgumentsException 
	 * @throws ClassNotLoadedException 
	 * @throws InterruptedException 
	 */
	public void extract() throws IllegalConnectorArgumentsException, IOException, ClassNotLoadedException, InterruptedException {

		// creating the VmManager using JDIAttach to find the vmx
		JDIAttach jdiAttach = new JDIAttach();
		VirtualMachine vm = jdiAttach.attachToJDI(config.get("vm"));
		VmManager vmManager = new VmManager(vm);

		// Adding the breakpoint
		BreakpointWrapper bkWrap = BreakPointInstaller.addBreakpoint(vm, config.get("breakpoint"));

		// resuming the process of the the thread
		vmManager.resumeThread(config.get("entryMethod").textValue());
		
		vmManager.waitForBreakpoint(bkWrap);
		
		CallstackExtractor csExtractor = new CallstackExtractor(config.get("logging"), config.get("maxDepth").intValue());
		csExtractor.extractCallStack(vmManager.getThreadNamed(config.get("entryMethod").textValue()));

		// properly disconnecting
		vmManager.disposeVM();
	}
	
}
