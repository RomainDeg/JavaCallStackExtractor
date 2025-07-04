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

//TODO reduce this class duplications
//TODO does the uniqueId of a ObjectReference help to find the referred object ?
public class StackFrameParser {

	/**
	 * Used to indicates which Object has already been visited, to not visit again.
	 * By using its unique ID it should be possible to make a link to the one where
	 * the parsing system has developed the search.
	 */
	private static Set<ObjectReference> visited = new HashSet<ObjectReference>();

	/**
	 * parse a frame, by parsing the method signature, its arguments, and its
	 * receiver
	 * 
	 * @param frame the frame to parse
	 */
	public static void parse(StackFrame frame) {
		parseMethod(frame);
		parseArguments(frame);
		parseReceiver(frame);
	}

	/**
	 * Parsing the method signature used in the given frame
	 * 
	 * @param frame the frame to parse
	 */
	public static void parseMethod(StackFrame frame) {
		Method method = frame.location().method();
		System.out.println("Method: " + method.name() + "(" + String.join(",", method.argumentTypeNames()) + ")");
	}

	/**
	 * Parsing all accessible arguments given in the method in this frame
	 * 
	 * @param frame the frame to parse
	 */
	public static void parseArguments(StackFrame frame) {
		System.out.println("arguments : ");

		// getting the method associated to this frame
		Method method = frame.location().method();

		// arguments can sometimes not be accessible, if that's the case, stop here
		Iterator<Value> argumentsValueIterator;
		try {
			argumentsValueIterator = frame.getArgumentValues().iterator();
		} catch (InternalException e) {
			System.out.println("[Not Accessible]");
			return;
		}
		Iterator<String> namesIterator = method.argumentTypeNames().iterator();

		while (namesIterator.hasNext()) {
			// Here we suppose that method.argumentTypeNames() and frame.getArgumentValues()
			// have the same numbers of items
			// With this supposition being always true, we can just check if one have next
			// and iterate in both
			System.out.print(namesIterator.next() + " = ");
			parseValueRecursive(argumentsValueIterator.next(), "");
		}

	}

	/**
	 * Parsing the receiver of this frame
	 * 
	 * @param frame the frame to parse
	 */
	public static void parseReceiver(StackFrame frame) {
		System.out.println("receiver : ");
		parseValueRecursive(frame.thisObject(), "");
	}

	/**
	 * Parse the given value recursively to make sure no information are lost in the process
	 * @param value the value to parse
	 * @param indent the indent to add to make human able to understand what happen //TODO should be removed after
	 */ 
	private static void parseValueRecursive(Value value, String indent) {
		if (value == null) {
			System.out.println(indent + "null");
		} else if (value instanceof PrimitiveValue) {
			parsePrimitiveValue((PrimitiveValue) value, indent);
		} else if (value instanceof ObjectReference) {
			parseObjectReference((ObjectReference) value, indent);
		} else if (value instanceof VoidValue) {
			// TODO
			// implements this if needed
			throw new IllegalStateException("VoidValue encountered, parsing not yet implemented");
		} else {
			//in case there would be another type
			throw new IllegalStateException(
					"Unknown Value Type: " + value.type().name() + ", parsing not yet implemented for this type");
		}
	}
	
	/**
	 * Parse given the primitive value
	 * @param value the primitiveValue to parse
	 * @param indent the indent to add to make human able to understand what happen //TODO should be removed after
	 */
	private static void parsePrimitiveValue(PrimitiveValue value, String indent) {
		System.out.println(indent + value.type().name() + " = " + value.toString());
	}

	/**
	 * Parse the given ObjectReference
	 * @param value the ObjectReference to parse
	 * @param indent the indent to add to make human able to understand what happen //TODO should be removed after
	 */
	private static void parseObjectReference(ObjectReference value, String indent) {
		//TODO maybe we can add these object to visited ?
		if (value instanceof StringReference) {
			System.out.println(
					indent + "\"" + ((StringReference) value).value() + "\"" + "[id:" + value.uniqueID() + "]");

		} else if (value instanceof ArrayReference) {
			ReferenceType type = value.referenceType();
			System.out.println(indent + type.name() + " [id:" + value.uniqueID() + "] = ");
			
			//Parsing every value of the array
			List<Value> arrayValues = ((ArrayReference) value).getValues();

			for (int i = 0; i < arrayValues.size(); i++) {
				System.out.println(indent + "at: " + i + " = ");
				parseValueRecursive(arrayValues.get(i), indent + "  ");
			}

		} else if (value instanceof ClassObjectReference) {
			// using reflectedType because it is said to be more precise than referenceType
			parseAllFields(value, indent, ((ClassObjectReference) value).reflectedType());

		} else {
			parseAllFields(value, indent, value.referenceType());
		}

	}
	
	/**
	 * Parse all the fields of an ObjectReference
	 * @param ref the ObjectReference having the fields to parse
	 * @param indent the indent to add to make human able to understand what happen //TODO should be removed after
	 * @param type the reference type of the ObjectReference
	 */
	private static void parseAllFields(ObjectReference ref, String indent, ReferenceType type) {
		if (visited.contains(ref)) {
			System.out.println(indent + type.name() + "[id:" + ref.uniqueID() + "]");
			return;
		}
		visited.add(ref);

		System.out.println(indent + type.name() + " [id:" + ref.uniqueID() + "] = ");

		// Check if the class is prepared, if not trying to get any field will throw an
		// exception
		// TODO maybe there is a way to still get some information out of it, for the
		// non static fields ?
		if (!type.isPrepared()) {
			// Preparation involves creating the static fields for a class or interface and
			// initializing such fields to their default values
			return;
		}

		for (Field field : type.visibleFields()) {
			// TODO check if there is any difference using allFields instead of
			// visibleFields
			try {
				// TODO
				// We actually parse the static and final fields, should we?
				// it's potential information but could also be noise
				Value fieldValue = ref.getValue(field);
				System.out.println(indent + field.name() + " = ");
				parseValueRecursive(fieldValue, indent + "  ");

			} catch (IllegalArgumentException e) {
				// TODO Some fields are not valid, how is that possible?
			}
		}
	}

}
