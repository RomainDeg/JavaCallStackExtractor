package logging;

import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.StringReference;

public class LoggerText implements ILoggerFormat {

	private static String indent = "  ";

	private void addIndent(int depth) {
		System.out.print(indent.repeat(depth));
	}

	@Override
	public void framesStart() {
		// Nothing
	}

	@Override
	public void framesEnd() {
		// Nothing
	}

	@Override
	public void frameLineStart(int number) {
		System.out.println("---- Line " + number + " of the call stack ----");
	}

	@Override
	public void frameLineEnd() {
		// Nothing
	}

	@Override
	public void methodSignature(Method method) {
		System.out.println("Method signature: " + method.name() + "(" + String.join(",", method.argumentTypeNames()) + ")");
	}

	@Override
	public void methodArgumentStart() {
		System.out.println("Method arguments values : ");
	}

	@Override
	public void methodArgumentEnd() {
		// Nothing

	}

	@Override
	public void unaccessibleField(int depth) {
		addIndent(depth);
		System.out.println("[Not Accessible]");
	}

	@Override
	public void fieldNameStart(String name, int depth) {
		addIndent(depth);
		System.out.println(name + " = ");
	}

	@Override
	public void fieldNameEnd() {
		// Nothing
	}


	@Override
	public void methodReceiverStart() {
		System.out.println("Method receiver : ");
		
	}
	
	@Override
	public void methodReceiverEnd() {
		// Nothing
	}

	@Override
	public void nullValue(int depth) {
		addIndent(depth);
		System.out.println("null");
	}

	@Override
	public void maxDepth(int depth) {
		addIndent(depth);
		System.out.println("[max depth attained]");
	}

	@Override
	public void primitiveValue(PrimitiveValue value, int depth) {
		addIndent(depth);
		System.out.println(value.type().name() + " = " + value.toString());
	}

	@Override
	public void stringReference(StringReference value, int depth) {
		addIndent(depth);
		System.out.println("\"" + value.value() + "\"" + "[ObjId:" + value.uniqueID() + "]");
	}

	@Override
	public void objectReferenceAlreadyFound(ObjectReference value, int depth) {
		//TODO change this to just print something like [Already studied]
		addIndent(depth);
		System.out.println("[Already studied]");
	}

	@Override
	public void objectReferenceStart(ObjectReference value, int depth) {
		addIndent(depth);
		System.out.println(value.referenceType().name() + " [ObjId:" + value.uniqueID() + "] = ");
	}

	@Override
	public void objectReferenceEnd() {
		// Nothing
	}

	@Override
	public void emptyArray(int depth) {
		System.out.println("[Empty Array]");
	}

	@Override
	public void arrayValueStart(int number, int depth) {
		addIndent(depth);
		System.out.println("at: " + number + " = ");
	}

	@Override
	public void arrayValueEnd() {
		// Nothing
	}

	@Override
	public void fieldsStart() {
		// Nothing
	}

	@Override
	public void fieldsEnd() {
		// Nothing
	}

	@Override
	public void classNotPrepared(int depth) {
		addIndent(depth);
		System.out.println("[not prepared]");
	}

	@Override
	public void joinElementListing() {
		// Nothing
	}

	@Override
	public void arrayStart() {
		// Nothing
		
	}

	@Override
	public void arrayEnd() {
		// Nothing
		
	}

}
