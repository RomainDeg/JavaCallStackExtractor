package logging;

import java.util.Iterator;

import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.StringReference;

public class LoggerJson extends AbstractLoggerFormat {

	public LoggerJson(String outputName) {
		super(outputName, "json");
	}

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

		write(quotes("method") + ":");
		// open object
		this.objectStart();
		// writing the name
		write(quotes("name") + ":" + quotes(method.name()));
		this.joinElementListing();
		// writing all arguments types
		write(quotes("parameters") + ":");
		// open array
		this.arrayStart();

		// fill the array with the parameters types
		Iterator<String> ite = method.argumentTypeNames().iterator();

		if (ite.hasNext()) {
			write(quotes(ite.next()));
		}
		while (ite.hasNext()) {
			this.joinElementListing();
			write(quotes(ite.next()));
		}
		// close array
		this.arrayEnd();

		// close object
		this.objectEnd();

		this.joinElementListing();
	}

	@Override
	public void methodArgumentStart() {
		write(quotes("arguments") + ":");

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
		write(quotes("<<UNACCESSIBLE>>"));
	}

	@Override
	public void fieldNameStart(String name, int depth) {
		// open object for field
		this.objectStart();
		write(quotes("field") + ":");
		// open object for field description
		this.objectStart();

		write(quotes("name") + ":" + quotes(name));
		this.joinElementListing();
		write(quotes("value") + ":");
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
		write(quotes("receiver") + ":");
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
		write("null");
	}

	@Override
	public void maxDepth(int depth) {
		write(quotes("<<MAX_DEPTH_REACHED>>"));
	}

	@Override
	public void primitiveValue(PrimitiveValue value, int depth) {
		this.objectStart();

		write(quotes("type") + ":" + quotes(value.type().name()));

		this.joinElementListing();

		write(quotes("value") + ":" + quotes(value.toString()));

		this.objectEnd();
	}

	@Override
	public void stringReference(StringReference value, int depth) {
		System.out.print(quotes(value.value()));
	}

	@Override
	public void objectReferenceAlreadyFound(ObjectReference value, int depth) {
		write(quotes("<<Already_Studied>>"));
	}

	@Override
	public void objectReferenceStart(ObjectReference value, int depth) {
		// open object for the reference
		this.objectStart();
		write(quotes("reference") + ":");
		// open object for the description
		this.objectStart();
		write(quotes("type") + ":" + quotes(value.referenceType().name()));
		this.joinElementListing();
		write(quotes("uniqueId") + ":" + value.uniqueID());
		this.joinElementListing();
		write(quotes("object") + ":");
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
		write("[]");
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
		write(quotes("<<CLASS_NOT_PREPARED>>"));

	}

	@Override
	public void joinElementListing() {
		write(",");
	}

	public void arrayStart() {
		write("[");
	}

	public void arrayEnd() {
		write("]");
	}

	private String quotes(String str) {
		return "\"" + str + "\"";
	}

	private void objectStart() {
		write("{");
	}

	private void objectEnd() {
		write("}");
	}

}
