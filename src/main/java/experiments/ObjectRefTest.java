package experiments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ObjectRefTest {

public static void main(String[] args) throws Exception {
		
		List<String> l = new ArrayList<>();
		addAnElement(l);
	}
	
	
	public static void addAnElement(List<String> l) {
		l.add("AnElement");
		addAnotherElement(l);
	}
	
	public static void addAnotherElement(List<String> l) {
		l.add("AnotherElement");
		wowAnElementWasAdded(l);
	}
	
	public static void wowAnElementWasAdded(List<String> l) {
		try {
			Runtime.getRuntime().exec("open -a calculator");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
