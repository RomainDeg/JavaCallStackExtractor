package app.logging;

import java.util.Iterator;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.StringReference;

public class LoggerJson extends AbstractLoggerFormat {

	public LoggerJson(String outputName, String Extension) {
		super(outputName, Extension);
	}

	@Override
	public void framesStart() {
		this.objectStart();

		write(quotes("Lines") + ":");
		this.arrayStart();
	}

	@Override
	public void framesEnd() {
		this.arrayEnd();

		this.objectEnd();
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
		// Retrieving the type of the parameters is important because it provides the most general type that can be used

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

		// fill the array with the parameters names and type
		parameters(method);

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
	public void fieldsStart() {
		// open object for fields
		this.objectStart();

		write(quotes("fields") + ":");
		// open array
		this.arrayStart();
	}

	@Override
	public void fieldsEnd() {
		// open array
		this.arrayEnd();

		// close fields
		this.objectEnd();
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
		// open object for the primitive type
		this.objectStart();

		write(quotes("primitiveType") + ":");

		// open object for the description of the type
		this.objectStart();

		write(quotes("type") + ":" + quotes(value.type().name()));

		this.joinElementListing();

		write(quotes("value") + ":" + quotes(value.toString()));

		// close object for the description of the type
		this.objectEnd();

		// close object for the primitive type
		this.objectEnd();
	}

	@Override
	public void stringReference(StringReference value, int depth) {
		write(quotes(value.value()));
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
		write(quotes("refered") + ":");

	}

	@Override
	public void objectReferenceEnd() {
		// close object for the reference
		this.objectEnd();
		// close object for the description
		this.objectEnd();
	}

	@Override
	public void emptyArray(int depth) {
		// Nothing
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
	public void classNotPrepared(int depth) {
		write(quotes("<<CLASS_NOT_PREPARED>>"));

	}

	@Override
	public void joinElementListing() {
		write(",");
	}

	@Override
	public void arrayReferenceStart() {
		this.objectStart();
		write(quotes("elements") + ":");
		this.arrayStart();

	}

	@Override
	public void arrayReferenceEnd() {
		this.arrayEnd();
		this.objectEnd();
	}

	private void arrayStart() {
		write("[");
	}

	private void arrayEnd() {
		write("]");
	}

	private void parameters(Method method) {
		try {
			// trying to obtain the arguments informations
			Iterator<LocalVariable> ite = method.arguments().iterator();

			if (ite.hasNext()) {
				parameter(ite.next());
			}
			while (ite.hasNext()) {
				this.joinElementListing();
				parameter(ite.next());
			}

		} catch (AbsentInformationException e) {
			// arguments name could not be obtained
			// Since the name are not obtainable just log the parameters types
			Iterator<String> ite = method.argumentTypeNames().iterator();

			if (ite.hasNext()) {
				parameter(ite.next());
			}
			while (ite.hasNext()) {
				this.joinElementListing();
				parameter(ite.next());
			}
		}
	}

	/*
	 * log the name and type of the parameter if the LocalVariable could be obtained
	 */
	private void parameter(LocalVariable var) {
		this.objectStart();
		write(quotes("name") + ":");
		write(quotes(var.name()));
		this.joinElementListing();
		write(quotes("type") + ":");
		write(quotes(var.typeName()));
		this.objectEnd();
	}

	/*
	 * log only the type of the parameter if the LocalVariable could not be obtained
	 */
	private void parameter(String typeName) {
		this.objectStart();
		write(quotes("name") + ":");
		this.nullValue(0);
		this.joinElementListing();
		write(quotes("type") + ":");
		write(quotes(typeName));
		this.objectEnd();
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
