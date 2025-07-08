package attach;

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import extractors.StackExtractor;

import java.io.IOException;
import java.net.ConnectException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class JDIAttach {

	public static void main(String[] args) throws Exception {
		// setting the variable that could become argument of the program
		String host = "localhost";
		String port = "5006";

		String threadName = "main"; // name of the method creating the callStack

		// all informations of the method where a breakpoint should be added
		String className = "java.lang.Runtime";
		String methodName = "exec";
		List<String> methodArguments = Arrays.asList("java.lang.String"); // can be null if there's only one occurrence on the method's name in the
																			// class

		// define the max depth of the recursive instance research
		int maxDepth = 10;// 0 for no max depth
		
		StackExtractor.setMaxDepth(maxDepth);

		// getting the VM
		VirtualMachine vm = attachToJVM(host, port);

		// Adding the breakpoint
		BreakPointInstaller.INSTANCE.addBreakpoint(vm, className, methodName, methodArguments);

		// Searching for the wanted thread
		ThreadReference thread = getThread(threadName, vm);

		// resuming the process of the main
		thread.resume();

		// waiting for the thread to either finish or stop at a breakpoint
		waitForBreakpoint(thread);

		// Start the extraction
		extractCallStack(thread);

		// properly disconnecting
		vm.dispose();
	}

	/**
	 * Extract the call stack on the searched VM starting form the given thread and stopping at the method described
	 * 
	 * @param thread the thread to study
	 */
	private static void extractCallStack(ThreadReference thread) {

		try {
			// iterating from the end of the list to start the logging from the first method called
			List<StackFrame> frames = thread.frames();
			ListIterator<StackFrame> it = frames.listIterator(frames.size());
			for (int i = 1; i <= frames.size(); i++) {
				System.out.println("---- Line " + i + " of the call stack ----");

				StackFrame frame = it.previous();
				// extracting the stack frame
				StackExtractor.extract(frame);
			}
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
	 * @param threadName the name of the searched thread
	 * @param vm         the virtual machine where the thread is supposed to be
	 * @return the thread with the chosen name if one exist in the given VM
	 * @throws IllegalStateException if no thread can be found
	 */
	private static ThreadReference getThread(String threadName, VirtualMachine vm) {
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
	 * Find the Virtual Machine to attach this program to
	 * 
	 * @param host name of the host
	 * @param port address of the VM
	 * @return the Virtual Machine if one found
	 * @throws IOException                        when unable to attach.
	 * @throws IllegalConnectorArgumentsException if no connector socket can be used.
	 */
	public static VirtualMachine attachToJVM(String host, String port) throws IllegalConnectorArgumentsException, IOException {
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
		arguments.get("hostname").setValue(host);
		arguments.get("port").setValue(port); // need to correspond to the JVM address

		// Connect to the JVM
		try {
			return connector.attach(arguments);
		} catch (ConnectException e) {
			throw new IllegalStateException("Connection to the JVM refused, maybe check that the adresses are corresponding");
		}

	}

}
