package expression;

import java.util.Arrays;
import java.util.List;

import hl7.v2.instance.Element;

public class PluginCustomFailureMulti {
	
	public List<String> assertionWithCustomMessages(Element e){
		return Arrays.asList("FAIL");
	}

	
	public boolean assertion(Element e){
		return false;
	}

}
