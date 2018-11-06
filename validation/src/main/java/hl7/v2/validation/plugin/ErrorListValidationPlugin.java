package hl7.v2.validation.plugin;

import java.util.List;

import hl7.v2.instance.Element;

public interface ErrorListValidationPlugin {

	public List<String> assertionWithCustomMessages(Element e);

}
