package attach;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Type;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequestManager;

/**
 * This class offer methods to add a breakpoint on a method, from the method description
 */
public class BreakPointInstaller {

	/**
	 * Add a breakpoint at a specified method on the given VM Precise the method argument type names in case there is multiple method having the same
	 * name
	 * 
	 * @param vm              the VM
	 * @param methodInfos information on the method at which the breakpoint is installed
	 * @throws ClassNotLoadedException if no method matches the characteristics
	 */
	public static void addBreakpoint(VirtualMachine vm, JsonNode methodInfos) throws ClassNotLoadedException {
		// Getting the EventRequestManager of the VirtualMachine
		EventRequestManager requestManager = vm.eventRequestManager();

		// Getting the method, adapting the research depending of the amount of information given
		Method method = findMethod(vm, methodInfos);

		// Getting the location of the method
		Location location = method.location();

		// Creating the breakpoint at the wanted location
		BreakpointRequest breakpointRequest = requestManager.createBreakpointRequest(location);

		breakpointRequest.addCountFilter(1);
		breakpointRequest.enable(); // activate the breakpoint

	}

	/**
	 * Find a method matching the given characteristics in the Virtual Machine
	 * 
	 * @param vm              the Virtual Machine
	 * @param methodInfos information on the method at which the breakpoint is installed
	 * @return the method if one match
	 * @throws ClassNotLoadedException if no method matches the characteristics
	 */
	private static Method findMethod(VirtualMachine vm, JsonNode methodInfos) throws ClassNotLoadedException {
		// finding the class
		// TODO can only find classes in the JDK ?
		List<ReferenceType> classes = vm.classesByName(methodInfos.get("className").textValue());
		if (classes.isEmpty()) {
			throw new IllegalArgumentException("Class not found : " + methodInfos.get("className"));
		}
		ClassType classType = (ClassType) classes.get(0);

		// getting all the methods with the searched name in the class
		List<Method> allMethods = classType.methodsByName(methodInfos.get("methodName").textValue());

		// if no method found throw an exception
		if (allMethods.isEmpty()) {
			throw new IllegalArgumentException("No method named " + methodInfos.get("methodName") + " in class " + methodInfos.get("className"));
		}

		// if we have multiple methods and given arguments types, we search if one correspond
		for (Method m : allMethods) {
			List<Type> paramTypes = m.argumentTypes();
			// if not the same number of types just pass this method
			if (paramTypes.size() != methodInfos.get("methodArguments").size()) {
				continue;
			}
			// starting on the hypothesis that we found the method, and trying to invalidate it
			boolean matches = true;
			for (int i = 0; i < paramTypes.size(); i++) {
				if (!paramTypes.get(i).name().equals(methodInfos.get("methodArguments").get(i).textValue())) {
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
		throw new IllegalArgumentException("No method named " + methodInfos.get("methodName") + " in class " + methodInfos.get("className")
				+ " with argument types: " + methodInfos.get("methodArguments"));
	}

}