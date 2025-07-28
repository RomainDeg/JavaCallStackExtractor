package attach;

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import extractors.StackExtractor;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Attach to a java virtual machine to extract the call stack to a text file
 */
public class JDIAttach {

	public static JsonNode config;
	
	public static StackExtractor extractor;

	public static void main(String[] args) throws Exception {
		// TODO change the fielName to be an argument of the program
		// TODO Maybe try to not attach to a vm but instatiate it ourselves
		// TODO Add a number of stop before activating the breakpoint in the config, so that if you wan't to stop on the third call, you can
		// TODO Add a readme for explaining how to add another logger format
		// reading the config file
		String fileName = "config.json";
		try {
			ObjectMapper mapper = new ObjectMapper();
			config = mapper.readTree(new File(fileName));

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// StackExtractor informations setting
		extractor = new StackExtractor(config.get("logging"),config.get("maxDepth").intValue());

		// getting the VM
		VirtualMachine vm = attachToJVM();

		// Adding the breakpoint
		BreakPointInstaller.addBreakpoint(vm, config.get("breakpoint"));

		// Searching for the wanted thread
		ThreadReference thread = getThread(vm);

		// resuming the process of the main
		thread.resume();

		// waiting for the thread to either finish or stop at a breakpoint
		waitForBreakpoint(thread);

		// Start the extraction
		extractCallStack(thread);

		// properly disconnecting
		vm.dispose();
		// close the writer in the logger
		extractor.closeLogger();
	}

	/**
	 * Extract the call stack on the searched VM starting form the given thread and stopping at the method described
	 * 
	 * @param thread the thread to study
	 */
	private static void extractCallStack(ThreadReference thread) {

		try {
			extractor.getLogger().framesStart();
			// iterating from the end of the list to start the logging from the first method called
			List<StackFrame> frames = thread.frames();
			ListIterator<StackFrame> it = frames.listIterator(frames.size());

			// doing the first iteration separately because the logging potentially need
			// to know if we are at the first element or not to join with a special character
			extractor.getLogger().frameLineStart(1);

			// extracting the stack frame
			extractor.extract(it.previous());
			extractor.getLogger().frameLineEnd();

			for (int i = 2; i <= frames.size(); i++) {
				extractor.getLogger().joinElementListing();

				extractor.getLogger().frameLineStart(i);
				// extracting the stack frame
				extractor.extract(it.previous());
				extractor.getLogger().frameLineEnd();
			}
			extractor.getLogger().framesEnd();
		} catch (IncompatibleThreadStateException e) {
			// Should not happen because we are supposed to be at a breakpoint
			throw new IllegalStateException("Thread should be at a breakpoint but isn't");
		}

	}

	/**
	 * Waiting for the thread to stop at a break point
	 * 
	 * @param thread the thread that should stop at a breakpoint
	 * @throws IllegalStateException if the thread has terminated instead of stopped at a break point
	 */
	private static void waitForBreakpoint(ThreadReference thread) {
		while (!(thread.status() == ThreadReference.THREAD_STATUS_ZOMBIE || thread.isAtBreakpoint())) {
			try {
				TimeUnit.MILLISECONDS.sleep(5);
			} catch (InterruptedException e) {
				// No need to take note of this exception
			}
		}

		if (thread.status() == ThreadReference.THREAD_STATUS_ZOMBIE) {
			throw new IllegalStateException("Thread has not encounter a breakpoint");
		}
	}

	/**
	 * Returns the thread with the chosen name if one exist in the given VM
	 * 
	 * @param vm         the virtual machine where the thread is supposed to be
	 * @return the thread with the chosen name if one exist in the given VM
	 * @throws IllegalStateException if no thread can be found
	 */
	private static ThreadReference getThread( VirtualMachine vm) {
		ThreadReference main = null;
		for (ThreadReference thread : vm.allThreads()) {
			if (thread.name().equals(config.get("sourceMethod").textValue())) {
				main = thread;
				break;
			}
		}
		if (main == null) {
			throw new IllegalStateException("No thread nammed " + config.get("threadName") + "was found");
		}
		return main;
	}

	/**
	 * Find the Virtual Machine to attach this program to
	 * 
	 * @return the Virtual Machine if one found
	 * @throws IOException                        when unable to attach.
	 * @throws IllegalConnectorArgumentsException if no connector socket can be used.
	 */
	public static VirtualMachine attachToJVM() throws IllegalConnectorArgumentsException, IOException {
		// Getting the configs for the vm
		JsonNode vmConfig = config.get("vm");
		
		VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
		AttachingConnector connector = null;

		// Searching for the connector socket
		for (AttachingConnector ac : vmm.attachingConnectors()) {
			if (ac.name().equals("com.sun.jdi.SocketAttach")) {
				connector = ac;
				break;
			}
		}
		if (connector == null) {
			throw new IllegalStateException("No connector socket found");
		}

		// Configure the arguments
		Map<String, Connector.Argument> arguments = connector.defaultArguments();

		arguments.get("hostname").setValue(vmConfig.get("host").textValue());
		arguments.get("port").setValue(vmConfig.get("port").textValue()); // need to correspond to the JVM address

		// Connect to the JVM
		try {
			return connector.attach(arguments);
		} catch (ConnectException e) {
			throw new IllegalStateException("Connection to the JVM refused, maybe check that the adresses are corresponding");
		}

	}

}
