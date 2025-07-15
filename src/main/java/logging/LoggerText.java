package logging;

import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.StringReference;

public class LoggerText extends AbstractLoggerFormat {

	private static String indent = "  ";

	public LoggerText(String outputName) {
		super(outputName, "txt");
	}

	private void addIndent(int depth) {
		write(indent.repeat(depth));
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
		writeln("---- Line " + number + " of the call stack ----");
	}

	@Override
	public void frameLineEnd() {
		// Nothing
	}

	@Override
	public void methodSignature(Method method) {
		writeln("Method signature: " + method.name() + "(" + String.join(",", method.argumentTypeNames()) + ")");
	}

	@Override
	public void methodArgumentStart() {
		writeln("Method arguments values : ");
	}

	@Override
	public void methodArgumentEnd() {
		// Nothing

	}

	@Override
	public void unaccessibleField(int depth) {
		addIndent(depth);
		writeln("[Not Accessible]");
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
	public void fieldNameStart(String name, int depth) {
		addIndent(depth);
		writeln(name + " = ");
	}

	@Override
	public void fieldNameEnd() {
		// Nothing
	}

	@Override
	public void methodReceiverStart() {
		writeln("Method receiver : ");

	}

	@Override
	public void methodReceiverEnd() {
		// Nothing
	}

	@Override
	public void nullValue(int depth) {
		addIndent(depth);
		writeln("null");
	}

	@Override
	public void maxDepth(int depth) {
		addIndent(depth);
		writeln("[max depth attained]");
	}

	@Override
	public void primitiveValue(PrimitiveValue value, int depth) {
		addIndent(depth);
		writeln(value.type().name() + " = " + value.toString());
	}

	@Override
	public void stringReference(StringReference value, int depth) {
		addIndent(depth);
		writeln("\"" + value.value() + "\"" + "[ObjId:" + value.uniqueID() + "]");
	}

	@Override
	public void objectReferenceAlreadyFound(ObjectReference value, int depth) {
		addIndent(depth);
		writeln("[Already studied]");
	}

	@Override
	public void objectReferenceStart(ObjectReference value, int depth) {
		addIndent(depth);
		writeln(value.referenceType().name() + " [ObjId:" + value.uniqueID() + "] = ");
	}

	@Override
	public void objectReferenceEnd() {
		// Nothing
	}

	@Override
	public void emptyArray(int depth) {
		writeln("[Empty Array]");
	}

	@Override
	public void arrayValueStart(int number, int depth) {
		addIndent(depth);
		writeln("at: " + number + " = ");
	}

	@Override
	public void arrayValueEnd() {
		// Nothing
	}

	@Override
	public void classNotPrepared(int depth) {
		addIndent(depth);
		writeln("[not prepared]");
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
