package logging;

import java.util.Iterator;

import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.StringReference;

public class LoggerJson implements ILoggerFormat {

	@Override
	public void framesStart() {
		this.arrayStart();
	}

	@Override
	public void framesEnd() {
		this.arrayEnd();

	}

	@Override
	public void frameLineStart(int i) {
		this.objectStart();

	}

	@Override
	public void frameLineEnd() {
		this.objectEnd();

	}

	@Override
	public void methodSignature(Method method) {
		// TODO the informations about the arguments types will also be obtained from the arguments values is that necessary to do it here?
		// pay attention: while the arguments give their types, maybe it's a subtype of the type that the method can accept

		System.out.print(quotes("method") + ":");
		// open object
		this.objectStart();
		// writing the name
		System.out.print(quotes("name") + ":" + quotes(method.name()));
		this.joinElementListing();
		// writing all arguments types
		System.out.print(quotes("parameters") + ":");
		// open array
		this.arrayStart();

		// fill the array with the parameters types
		Iterator<String> ite = method.argumentTypeNames().iterator();

		if (ite.hasNext()) {
			System.out.print(quotes(ite.next()));
		}
		while (ite.hasNext()) {
			this.joinElementListing();
			System.out.print(quotes(ite.next()));
		}
		// close array
		this.arrayEnd();

		// close object
		this.objectEnd();

		this.joinElementListing();
	}

	@Override
	public void methodArgumentStart() {
		System.out.print(quotes("arguments") + ":");

		// open array
		this.arrayStart();

	}

	@Override
	public void methodArgumentEnd() {
		// close array
		this.arrayEnd();

		this.joinElementListing();
	}

	@Override
	public void unaccessibleField(int depth) {
		System.out.print(quotes("<<UNACCESSIBLE>>"));
	}

	@Override
	public void fieldNameStart(String name, int depth) {
		// open object for field
		this.objectStart();
		System.out.print(quotes("field") + ":");
		// open object for field description
		this.objectStart();

		System.out.print(quotes("name") + ":" + quotes(name));
		this.joinElementListing();
		System.out.print(quotes("value") + ":");
	}

	@Override
	public void fieldNameEnd() {
		// close object in field
		this.objectEnd();
		// close object field
		this.objectEnd();
	}

	@Override
	public void methodReceiverStart() {
		System.out.print(quotes("receiver") + ":");
		// open object
		// Resolve the case of {
		// this.objectStart();
	}

	@Override
	public void methodReceiverEnd() {
		// close object
		// this.objectEnd();
	}

	@Override
	public void nullValue(int depth) {
		System.out.print("null");
	}

	@Override
	public void maxDepth(int depth) {
		System.out.print(quotes("<<MAX_DEPTH_REACHED>>"));
	}

	@Override
	public void primitiveValue(PrimitiveValue value, int depth) {
		System.out.print(value.toString());
	}

	@Override
	public void stringReference(StringReference value, int depth) {
		System.out.print(quotes(value.value()));
	}

	@Override
	public void objectReferenceAlreadyFound(ObjectReference value, int depth) {
		System.out.print(quotes("<<Already_Studied>>"));
	}

	@Override
	public void objectReferenceStart(ObjectReference value, int depth) {
		// open object for the reference
		this.objectStart();
		System.out.print(quotes("reference") + ":");
		// open object for the description
		this.objectStart();
		System.out.print(quotes("uniqueId") + ":" + value.uniqueID());
		this.joinElementListing();
		System.out.print(quotes("object") + ":");
		// open array for the object fields
		this.arrayStart();

	}

	@Override
	public void objectReferenceEnd() {
		// close array for the object fields
		this.arrayEnd();
		// close object for the reference
		this.objectEnd();
		// close object for the description
		this.objectEnd();
	}

	@Override
	public void emptyArray(int depth) {
		System.out.print("[]");
	}

	@Override
	public void arrayValueStart(int number, int depth) {
		// Nothing
	}

	@Override
	public void arrayValueEnd() {
		// Nothing

	}

	@Override
	public void fieldsStart() {
		// open array
		this.arrayStart();
	}

	@Override
	public void fieldsEnd() {
		// open array
		this.arrayEnd();
	}

	@Override
	public void classNotPrepared(int depth) {
		System.out.print(quotes("<<CLASS_NOT_PREPARED>>"));

	}

	@Override
	public void joinElementListing() {
		System.out.print(",");
	}

	public void arrayStart() {
		System.out.print("[");
	}

	public void arrayEnd() {
		System.out.print("]");
	}

	private String quotes(String str) {
		return "\"" + str + "\"";
	}

	private void objectStart() {
		System.out.print("{");
	}

	private void objectEnd() {
		System.out.print("}");
	}

}
