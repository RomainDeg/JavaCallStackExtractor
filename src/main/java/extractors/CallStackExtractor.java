package extractors;

import java.util.List;
import java.util.ListIterator;

import com.sun.jdi.*;

public class CallStackExtractor {
	
	//TODO
    //Actually printing the call stack, but should log all necessary information in a file  

	public static void extract(List<StackFrame> frames) {
		//iterating from the end of the list to start the logging from the first method called
        ListIterator<StackFrame> it = frames.listIterator(frames.size());
        for(int i = 1; i <= frames.size(); i++) {
        	System.out.println("---- Line " + i + " of the call stack ----");
        	
        	StackFrame frame = it.previous();
        	//parsing the frame
            StackFrameExtractor.extract(frame);
        }
	}
    
}