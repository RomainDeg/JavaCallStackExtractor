package extractors;

import java.util.List;
import java.util.ListIterator;

import com.sun.jdi.*;

public class CallStackParser {
	
	//TODO
    //Actually printing the call stack, but should log all necessary information in a file  

	public static void parse(List<StackFrame> frames) {
		//iterating from the end of the list to start the logging from the first method called
        ListIterator<StackFrame> it = frames.listIterator(frames.size());
        for(int i = 0; i < frames.size(); i++) {
        	System.out.println("---- " + i + " ----");
        	
        	StackFrame frame = it.previous();
        	//parsing the frame
            StackFrameParser.parse(frame);
        }
	}
    
}