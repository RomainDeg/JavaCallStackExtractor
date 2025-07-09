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

import logging.ILogger;
import logging.LoggerPrintTxt;

import com.sun.jdi.ReferenceType;

/**
 * This class extract all the information of a given stack frame to a text file
 */
public class StackExtractor {
	
	/**
	 * The logger used to collect extracted informations
	 * Default value is LoggerPrintTxt
	 */
	public static ILogger logger = new LoggerPrintTxt();

	/**
	 * represent the maximum recursion algorithm to study object's fields and array's value can make
	 */
	public static int maxDepth;
	
	public static void setLogger(ILogger log) {
		logger = log;
	}
	
	public static ILogger getLogger() {
		return logger;
	}

	/**
	 * Set the max depth recursion for the algorithm to study object's fields and array's value can make
	 * @param depth the new max depth
	 */
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
		logger.methodName(method);
	}

	/**
	 * Extracting all accessible arguments given in the method in this frame
	 * 
	 * @param frame the frame to extract
	 */
	public static void extractArguments(StackFrame frame) {
		logger.methodArgumentStart();

		// getting the method associated to this frame
		Method method = frame.location().method();

		// arguments can sometimes not be accessible, if that's the case, stop here
		Iterator<Value> argumentsValueIterator;
		try {
			argumentsValueIterator = frame.getArgumentValues().iterator();
		} catch (InternalException e) {
			//Happens for native calls, and can't be obtained
			logger.unaccessibleField();
			return;
		}
		Iterator<String> namesIterator = method.argumentTypeNames().iterator();

		while (namesIterator.hasNext()) {
			// Here we suppose that method.argumentTypeNames() and frame.getArgumentValues() have the same numbers of items
			// With this supposition being always true, we can just check if one have next and iterate in both
			logger.fieldName(namesIterator.next());
			
			extractValueRecursive(argumentsValueIterator.next(), 0);
		}
		logger.methodArgumentEnd();
	}

	/**
	 * Extracting the receiver of this frame
	 * 
	 * @param frame the frame to extract
	 */
	public static void extractReceiver(StackFrame frame) {
		logger.methodReceiverStart();
		extractValueRecursive(frame.thisObject(), 0);
		logger.methodReceiverEnd();
	}

	/**
	 * extract the given value recursively to make sure no information are lost in the process
	 * 
	 * @param value  the value to extract
	 * @param indent the indent to add to make human able to understand what happen
	 */
	private static void extractValueRecursive(Value value, int depth) {
		logger.valueStart();
		if (maxDepth != 0 & depth > maxDepth) {
			logger.maxDepth(depth);
			return;
		}
		else if (value == null) {
			logger.nullValue(depth);
		} else if (value instanceof PrimitiveValue) {
			extractPrimitiveValue((PrimitiveValue) value, depth);
		} else if (value instanceof ObjectReference) {
			extractObjectReference((ObjectReference) value, depth);
		} else if (value instanceof VoidValue) {
			// TODO
			// implements this if needed
			throw new IllegalStateException("VoidValue encountered, extracting not yet implemented");
		} else {
			// in case there would be another type
			throw new IllegalStateException("Unknown Value Type: " + value.type().name() + ", parsing not yet implemented for this type");
		}
		logger.valueEnd();
	}

	/**
	 * extract given the primitive value
	 * 
	 * @param value  the primitiveValue to extract
	 * @param indent the indent to add to make human able to understand what happen
	 */
	private static void extractPrimitiveValue(PrimitiveValue value, int depth) {
		logger.primitiveValue(value, depth);
	}

	/**
	 * extract the given ObjectReference
	 * 
	 * @param value  the ObjectReference to extract
	 * @param indent the indent to add to make human able to understand what happen
	 */
	private static void extractObjectReference(ObjectReference value, int depth) {
		// TODO maybe we can add these object to visited ?
		if (value instanceof StringReference) {
			logger.stringReference((StringReference) value, depth);
		} else if (value instanceof ArrayReference) {
			logger.objectReferenceStart(value, depth);

			// Parsing every value of the array
			List<Value> arrayValues = ((ArrayReference) value).getValues();
			if (arrayValues.size() == 0) {
				logger.emptyArray(depth);
			}
			// in case the max depth will be attained stop before the spam of [max depth attained]
			if (maxDepth != 0 & depth + 1 > maxDepth) {
				logger.maxDepth(depth);
				return;
			}
			for (int i = 0; i < arrayValues.size(); i++) {
				logger.arrayValueStart(i,depth);
				extractValueRecursive(arrayValues.get(i), depth + 1);
				logger.arrayValueEnd();
			}
			logger.objectReferenceEnd();
		} else if (value instanceof ClassObjectReference) {
			// using reflectedType because it is said to be more precise than referenceType
			extractAllFields(value, ((ClassObjectReference) value).reflectedType(), depth);

		} else {
			extractAllFields(value, value.referenceType(), depth);
		}

	}

	/**
	 * extract all the fields of an ObjectReference
	 * 
	 * @param ref    the ObjectReference having the fields to extract
	 * @param indent the indent to add to make human able to understand what happen
	 * @param type   the reference type of the ObjectReference
	 */
	private static void extractAllFields(ObjectReference ref, ReferenceType type, int depth) {
		logger.fieldsStart();
		if (visited.contains(ref)) {
			logger.objectReference(ref,depth);
			return;
		}
		visited.add(ref);
		
		logger.objectReferenceStart(ref, depth);

		// Check if the class is prepared, if not trying to get any field will throw an exception
		// TODO maybe there is a way to force load the class, is that useful ? maybe the fact that it didn't load mean it's not useful
		if (!type.isPrepared()) {
			// Preparation involves creating the static fields for a class or interface and
			// initializing such fields to their default values
			
			logger.classNotPrepared(depth);
			return;
		}

		for (Field field : type.allFields()) {
			try {
				// TODO
				// We actually extract the static and final fields, should we?
				// it's potential information but could also be noise
				Value fieldValue = ref.getValue(field);
				logger.fieldNameStart(field.name(),depth);
				
				extractValueRecursive(fieldValue, depth + 1);
				
				logger.fieldNameEnd(field.name());

			} catch (IllegalArgumentException e) {
				logger.unaccessibleField(depth);
			}
		}
		logger.objectReferenceEnd();
		
		logger.fieldsEnd();
	}

}
