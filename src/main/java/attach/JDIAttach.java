package attach;

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequestManager;

import extractors.CallStackExtractor;

import java.io.IOException;
import java.net.ConnectException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class JDIAttach {

	// TODO divide the method extractCallStack and move it into the main to make more sense out of it
	public static void main(String[] args) throws Exception {
		// setting the variable that could become argument of the program
		String host = "localhost";
		String port = "5006";

		String threadName = "main"; // name of the method creating the callStack

		// all informations of the method where a breakpoint should be added
		String className = "java.lang.Runtime";
		String methodName = "exec";
		List<String> methodArguments = Arrays.asList("java.lang.String"); // can be null if there's no name repetition

		extractCallStack(host, port, className, methodName, methodArguments, threadName);
	}

	/**
	 * Extract the call stack on the searched VM starting form the given thread and stopping at the method described
	 * 
	 * @param host            name of the host
	 * @param port            address of the VM
	 * @param className       the name of the class where the method is situated
	 * @param methodName      name of the searched method
	 * @param methodArguments name of all arguments of the method in the declaration order
	 * @param threadName      name of the thread to study
	 * @throws IOException                        when unable to attach to a VM
	 * @throws IllegalConnectorArgumentsException if no connector socket can be used to attach to the VM
	 */
	private static void extractCallStack(String host, String port, String className, String methodName, List<String> methodArguments,
			String threadName) throws IOException, IllegalConnectorArgumentsException {
		VirtualMachine vm = attachToJVM(host, port);

		// adding the breakpoint
		addBreakpoint(vm, className, methodName, methodArguments);

		// Searching for the wanted thread
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

		// resuming the process of the main
		main.resume();

		// waiting for the thread to either finish or stop at a breakpoint
		while (!(main.status() == ThreadReference.THREAD_STATUS_ZOMBIE || main.isAtBreakpoint())) {
			try {
				TimeUnit.MILLISECONDS.sleep(5);
			} catch (InterruptedException e) {
				// No need to take note of this exception
			}
		}

		if (main.status() == ThreadReference.THREAD_STATUS_ZOMBIE) {
			throw new IllegalStateException("Thread has not encounter a breakpoint");
		}

		// Parsing the call stack
		try {
			CallStackExtractor.extract(main.frames());
		} catch (IncompatibleThreadStateException e) {
			// Should not happen because we are normally at a breakpoint
			throw new IllegalStateException("Thread should be at a breakpoint but isn't");
		}

		vm.dispose(); // properly disconnecting
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

	/**
	 * Add a breakpoint at a specified method on the given VM
	 * 
	 * @param vm         the VM
	 * @param className  the name of the class
	 * @param methodName the name of the method
	 */
	public static void addBreakpoint(VirtualMachine vm, String className, String methodName) {
		JDIAttach.addBreakpoint(vm, className, methodName, null);
	}

	/**
	 * Add a breakpoint at a specified method on the given VM Precise the method argument type names in case there is multiple method having the same
	 * name
	 * 
	 * @param vm              the VM
	 * @param className       the name of the class where the method is situated
	 * @param methodName      the name of the method
	 * @param methodArguments name of all arguments type of the method in the declaration order
	 */
	public static void addBreakpoint(VirtualMachine vm, String className, String methodName, List<String> methodArguments) {
		try {
			// Getting the EventRequestManager of the VirtualMachine
			EventRequestManager requestManager = vm.eventRequestManager();

			// Getting the method, adapting the research depending of the amount of information given
			Method method = findMethod(vm, className, methodName, methodArguments);

			// Getting the location of the method
			Location location = method.location();

			// Creating the breakpoint at the wanted location
			BreakpointRequest breakpointRequest = requestManager.createBreakpointRequest(location);
			breakpointRequest.enable(); // activate the breakpoint
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Find a method matching the given characteristics in the Virtual Machine
	 * 
	 * @param vm              the Virtual Machine
	 * @param className       the name of the class where the method is situated
	 * @param methodName      name of the searched method
	 * @param methodArguments name of all arguments type of the method in the declaration order
	 * @return the method if one match
	 * @throws ClassNotLoadedException if no method matches the characteristics
	 */
	private static Method findMethod(VirtualMachine vm, String className, String methodName, List<String> methodArguments)
			throws ClassNotLoadedException {
		// finding the class
		List<ReferenceType> classes = vm.classesByName(className);
		if (classes.isEmpty()) {
			throw new IllegalArgumentException("Class not found : " + className);
		}
		ClassType classType = (ClassType) classes.get(0);

		// getting all the methods with the searched name in the class
		List<Method> allMethods = classType.methodsByName(methodName);

		// if no method found throw an exception
		if (allMethods.isEmpty()) {
			throw new IllegalArgumentException("No method named " + methodName + " in class " + className);
		}

		// if no arguments given, either we only have one method found and that's fine
		// or we have multiple results, we don't make a random choice, we just throw an exception
		if (methodArguments == null) {
			if (allMethods.size() != 1) {
				throw new IllegalArgumentException("Multiple methods named " + methodName + " in class " + className);
			}
			return allMethods.get(0);
		}

		// if we have multiple methods and given arguments types, we search if one correspond
		for (Method m : allMethods) {
			List<Type> paramTypes = m.argumentTypes();
			// if not the same number of types just pass this method
			if (paramTypes.size() != methodArguments.size()) {
				continue;
			}
			// starting on the hypothesis that we found the method, and trying to invalidate it
			boolean matches = true;
			for (int i = 0; i < paramTypes.size(); i++) {
				if (!paramTypes.get(i).name().equals(methodArguments.get(i))) {
					matches = false;
					break;
				}
			}
			// if we can't invalidate the method, then we found it
			if (matches) {
				return m;
			}
		}
		// if we got here, then no method have been found
		throw new IllegalArgumentException("No method named " + methodName + " in class " + className + " with argument types: " + methodArguments);
	}

}
