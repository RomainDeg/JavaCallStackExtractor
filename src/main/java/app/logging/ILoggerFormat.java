package app.logging;

import java.io.IOException;

import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.StringReference;

public interface ILoggerFormat {

	void closeWriter() throws IOException;

	void framesStart();

	void framesEnd();

	void frameLineStart(int i);

	void frameLineEnd();

	void methodSignature(Method method);

	void methodArgumentStart();

	void methodArgumentEnd();

	void inaccessibleArgument(int depth);

	void inaccessibleField(int depth);

	void fieldsStart();

	void fieldsEnd();

	void fieldNameStart(String name, int depth);

	void fieldNameEnd();

	void methodReceiverStart();

	void methodReceiverEnd();

	void nullValue(int depth);

	void maxDepth(int depth);

	void primitiveValue(PrimitiveValue value, int depth);

	void stringReference(StringReference value, int depth);

	void objectReferenceAlreadyFound(ObjectReference value, int depth);

	void objectReferenceStart(ObjectReference value, int depth);

	void objectReferenceEnd();

	void emptyArray(int depth);

	void arrayValueStart(int number, int depth);

	void arrayValueEnd();

	void classNotPrepared(int depth);

	void joinElementListing();

	void arrayReferenceStart();

	void arrayReferenceEnd();

}
