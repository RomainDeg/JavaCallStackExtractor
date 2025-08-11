package app.vmAttach;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;

public class JDIAttach {
	
	/**
	 * Attach to a java virtual machine located with the given information
	 * @param vmConfig all information needed to find the vm
	 * @return the Virtual Machine if one found
	 * @throws IOException                        when unable to attach.
	 * @throws IllegalConnectorArgumentsException if no connector socket can be used.
	 */
	public VirtualMachine attachToJDI(JsonNode vmConfig) throws IllegalConnectorArgumentsException, IOException {

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
