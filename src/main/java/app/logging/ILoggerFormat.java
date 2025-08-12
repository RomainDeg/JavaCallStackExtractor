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

	void inaccessibleArgument();

	void fieldsStart();

	void fieldsEnd();
	
	void fieldStart(String name);
	
	void fieldValueStart();
	
	void fieldValueEnd();
	
	void fieldEnd();
	
	void inaccessibleField();
	
	void methodReceiverStart();

	void methodReceiverEnd();

	void nullValue();

	void maxDepth();

	void primitiveValue(PrimitiveValue value);

	void stringReference(StringReference value);

	void objectReferenceAlreadyFound(ObjectReference value);

	void objectReferenceStart(ObjectReference value);

	void objectReferenceEnd();

	void emptyArray();

	void arrayValueStart(int number);

	void arrayValueEnd();

	void classNotPrepared();

	void joinElementListing();

	void arrayReferenceStart();

	void arrayReferenceEnd();

}
