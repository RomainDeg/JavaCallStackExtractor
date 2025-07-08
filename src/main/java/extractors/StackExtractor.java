package extractors;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.Field;
import com.sun.jdi.InternalException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.Method;
import com.sun.jdi.StackFrame;
import com.sun.jdi.StringReference;
import com.sun.jdi.Value;
import com.sun.jdi.VoidValue;
import com.sun.jdi.ReferenceType;

public class StackExtractor {

	public static int maxDepth;

	public static void setMaxDepth(int depth) {
		maxDepth = depth;
	}

	/**
	 * Used to indicates which Object has already been visited, to not visit again.
	 */
	private static Set<ObjectReference> visited = new HashSet<ObjectReference>();

	/**
	 * extract a frame, by extracting the method signature, its arguments, and its receiver
	 * 
	 * @param frame the frame to extract
	 */
	public static void extract(StackFrame frame) {
		extractMethod(frame);
		extractArguments(frame);
		extractReceiver(frame);
	}

	/**
	 * Extracting the method signature used in the given frame
	 * 
	 * @param frame the frame to extract
	 */
	public static void extractMethod(StackFrame frame) {
		Method method = frame.location().method();
		System.out.println("Method signature: " + method.name() + "(" + String.join(",", method.argumentTypeNames()) + ")");
	}

	/**
	 * Extracting all accessible arguments given in the method in this frame
	 * 
	 * @param frame the frame to extract
	 */
	public static void extractArguments(StackFrame frame) {
		System.out.println("Method arguments values : ");

		// getting the method associated to this frame
		Method method = frame.location().method();

		// arguments can sometimes not be accessible, if that's the case, stop here
		Iterator<Value> argumentsValueIterator;
		try {
			argumentsValueIterator = frame.getArgumentValues().iterator();
		} catch (InternalException e) {
			//Happens for native calls, and can't be obtained
			System.out.println("[Not Accessible]");
			return;
		}
		Iterator<String> namesIterator = method.argumentTypeNames().iterator();

		while (namesIterator.hasNext()) {
			// Here we suppose that method.argumentTypeNames() and frame.getArgumentValues() have the same numbers of items
			// With this supposition being always true, we can just check if one have next and iterate in both
			System.out.print(namesIterator.next() + " = ");
			extractValueRecursive(argumentsValueIterator.next(), "", 0);
		}

	}

	/**
	 * Extracting the receiver of this frame
	 * 
	 * @param frame the frame to extract
	 */
	public static void extractReceiver(StackFrame frame) {
		System.out.println("Method receiver : ");
		extractValueRecursive(frame.thisObject(), "", 0);
	}

	/**
	 * extract the given value recursively to make sure no information are lost in the process
	 * 
	 * @param value  the value to extract
	 * @param indent the indent to add to make human able to understand what happen
	 */
	private static void extractValueRecursive(Value value, String indent, int depth) {
		if (maxDepth != 0 & depth > maxDepth) {
			System.out.println(indent + "[max depth attained]");
			return;
		}
		if (value == null) {
			System.out.println(indent + "null");
		} else if (value instanceof PrimitiveValue) {
			extractPrimitiveValue((PrimitiveValue) value, indent);
		} else if (value instanceof ObjectReference) {
			extractObjectReference((ObjectReference) value, indent, depth);
		} else if (value instanceof VoidValue) {
			// TODO
			// implements this if needed
			throw new IllegalStateException("VoidValue encountered, parsing not yet implemented");
		} else {
			// in case there would be another type
			throw new IllegalStateException("Unknown Value Type: " + value.type().name() + ", parsing not yet implemented for this type");
		}
	}

	/**
	 * extract given the primitive value
	 * 
	 * @param value  the primitiveValue to extract
	 * @param indent the indent to add to make human able to understand what happen
	 */
	private static void extractPrimitiveValue(PrimitiveValue value, String indent) {
		System.out.println(indent + value.type().name() + " = " + value.toString());
	}

	/**
	 * extract the given ObjectReference
	 * 
	 * @param value  the ObjectReference to extract
	 * @param indent the indent to add to make human able to understand what happen
	 */
	private static void extractObjectReference(ObjectReference value, String indent, int depth) {
		// TODO maybe we can add these object to visited ?
		if (value instanceof StringReference) {
			System.out.println(indent + "\"" + ((StringReference) value).value() + "\"" + "[ObjId:" + value.uniqueID() + "]");

		} else if (value instanceof ArrayReference) {
			ReferenceType type = value.referenceType();
			System.out.println(indent + type.name() + " [ObjId:" + value.uniqueID() + "] = ");

			// Parsing every value of the array
			List<Value> arrayValues = ((ArrayReference) value).getValues();
			if (arrayValues.size() == 0) {
				System.out.println("[Empty Array]");
			}
			// in case the max depth will be attained stop before the spam of [max depth attained]
			if (maxDepth != 0 & depth + 1 > maxDepth) {
				System.out.println(indent + "[max depth attained]");
				return;
			}
			for (int i = 0; i < arrayValues.size(); i++) {
				System.out.println(indent + "at: " + i + " = ");
				extractValueRecursive(arrayValues.get(i), indent + "  ", depth + 1);
			}

		} else if (value instanceof ClassObjectReference) {
			// using reflectedType because it is said to be more precise than referenceType
			extractAllFields(value, indent, ((ClassObjectReference) value).reflectedType(), depth);

		} else {
			extractAllFields(value, indent, value.referenceType(), depth);
		}

	}

	/**
	 * extract all the fields of an ObjectReference
	 * 
	 * @param ref    the ObjectReference having the fields to extract
	 * @param indent the indent to add to make human able to understand what happen
	 * @param type   the reference type of the ObjectReference
	 */
	private static void extractAllFields(ObjectReference ref, String indent, ReferenceType type, int depth) {
		if (visited.contains(ref)) {
			System.out.println(indent + type.name() + "[ObjId:" + ref.uniqueID() + "]");
			return;
		}
		visited.add(ref);

		System.out.println(indent + type.name() + " [ObjId:" + ref.uniqueID() + "] = ");

		// Check if the class is prepared, if not trying to get any field will throw an exception
		// TODO maybe there is a way to force load the class, is that useful ? maybe the fact that it didn't load mean it's not useful
		if (!type.isPrepared()) {
			// Preparation involves creating the static fields for a class or interface and
			// initializing such fields to their default values
			System.out.println(indent + "[not prepared]");
			return;
		}

		for (Field field : type.allFields()) {
			try {
				// TODO
				// We actually extract the static and final fields, should we?
				// it's potential information but could also be noise
				Value fieldValue = ref.getValue(field);
				System.out.println(indent + field.name() + " = ");
				extractValueRecursive(fieldValue, indent + "  ", depth + 1);

			} catch (IllegalArgumentException e) {
				System.out.println("[Not Accessible]");
			}
		}
	}

}
