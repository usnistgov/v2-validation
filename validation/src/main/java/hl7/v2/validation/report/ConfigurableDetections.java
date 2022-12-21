package hl7.v2.validation.report;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import expression.AsString;
import expression.Expression;
import gov.nist.validation.report.Entry;
import gov.nist.validation.report.Trace;
import gov.nist.validation.report.impl.EntryImpl;
import hl7.v2.instance.Element;
import hl7.v2.instance.Location;
import hl7.v2.profile.Range;
import hl7.v2.profile.ValueSetSpec;
import hl7.v2.validation.content.Classification;
import hl7.v2.validation.content.Constraint;
import hl7.v2.validation.content.ConstraintStrength;
import hl7.v2.validation.content.Predicate;
import hl7.v2.validation.vs.TripletEntry;
import hl7.v2.validation.vs.ValueSet;
import scala.Option;

public class ConfigurableDetections {
	
	
	Config conf = ConfigFactory.load();

	/*
	 * ========================================================================
	 * Structure related entries
	 * =======================================================================
	 */
	/**
	 * @return A report entry for the R usage detection
	 */
	public ConfigurableDetections(Config conf) {
		super();
		this.conf = conf;
	}
	
	public  Entry rusage(Location l) {
		String category = conf.getString("report.r-usage.category");
		String classification = conf.getString("report.r-usage.classification");
		String template = conf.getString("report.r-usage.template");
		String description = String.format(template, l.prettyString());
		return entry(l, description, category, classification);
	}

	public  Entry ousage(Location l, String value) {
		String category = conf.getString("report.o-usage.category");
		String classification = conf.getString("report.o-usage.classification");
		String template = conf.getString("report.o-usage.template");
		String desc = String.format(template, l.prettyString(), value);
		return entry(l, desc, category, classification);
	}

	/**
	 * @return A report entry for the X usage detection
	 */
	public  Entry xusage(Location l) {
		String category = conf.getString("report.x-usage.category");
		String classification = conf.getString("report.x-usage.classification");
		String template = conf.getString("report.x-usage.template");
		String description = String.format(template, l.prettyString());
		return entry(l, description, category, classification);
	}

	/**
	 * @return A report entry for the W usage detection
	 */
	public  Entry wusage(Location l) {
		String category = conf.getString("report.w-usage.category");
		String classification = conf.getString("report.w-usage.classification");
		String template = conf.getString("report.w-usage.template");
		String description = String.format(template, l.prettyString());
		return entry(l, description, category, classification);
	}

	/**
	 * @return A report entry for the RE usage detection
	 */
	public  Entry reusage(Location l) {
		String category = conf.getString("report.re-usage.category");
		String classification = conf
				.getString("report.re-usage.classification");
		String template = conf.getString("report.re-usage.template");
		String description = String.format(template, l.prettyString());
		return entry(l, description, category, classification);
	}

	/**
	 * @return A report entry for the W usage detection
	 */
	public  Entry cardinality(Location l, Range r, int count) {
		String category = conf.getString("report.cardinality.category");
		String classification = conf
				.getString("report.cardinality.classification");
		String template = conf.getString("report.cardinality.template");
		String description = String.format(template, l.prettyString(), r.min(),
				r.max(), count);
		return entry(l, description, category, classification);
	}

	public  Entry ncardinality(Location l, int count) {
		String category = conf.getString("report.null-cardinality.category");
		String classification = conf
				.getString("report.null-cardinality.classification");
		String template = conf.getString("report.null-cardinality.template");
		String description = String.format(template, l.prettyString(), count);
		return entry(l, description, category, classification);
	}

	/**
	 * @return A report entry for the length detection
	 */
	public  Entry length(Location l, Range r, String value) {
		String category = conf.getString("report.length.category");
		String classification = conf.getString("report.length.classification");
		String template = conf.getString("report.length.template");
		String desc = String.format(template, l.prettyString(), r.min(),
				r.max(), value);
		return entry(l, desc, category, classification);
	}
	
	public  Entry lengthSpecErrorNF(Location l) {
		String category = conf.getString("report.length-spec-error-no-valid.category");
		String classification = conf.getString("report.length-spec-error-no-valid.classification");
		String template = conf.getString("report.length-spec-error-no-valid.template");
		String desc = String.format(template, l.prettyString());
		return entry(l, desc, category, classification);
	}
	
	public  Entry lengthSpecErrorXOR(Location l) {
		String category = conf.getString("report.length-spec-error-xor.category");
		String classification = conf.getString("report.length-spec-error-xor.classification");
		String template = conf.getString("report.length-spec-error-xor.template");
		String desc = String.format(template, l.prettyString());
		return entry(l, desc, category, classification);
	}
	
	/**
	 * @return A report entry for the format detection
	 */
	public  Entry format(Location l, String msg) {
		String category = conf.getString("report.format.category");
		String classification = conf.getString("report.format.classification");
		String template = conf.getString("report.format.template");
		String desc = String.format(template, msg);
		return entry(l, desc, category, classification);
	}

	/**
	 * @return A report entry for the extra detection
	 */
	public  Entry extra(Location l) {
		String category = conf.getString("report.extra.category");
		String classification = conf.getString("report.extra.classification");
		String template = conf.getString("report.extra.template");
		String desc = String.format(template, l.prettyString());
		return entry(l, desc, category, classification);
	}

	/**
	 * @return A report entry for unescaped separators detection
	 */
	public  Entry unescaped(Location l) {
		String category = conf.getString("report.unescaped.category");
		String classification = conf
				.getString("report.unescaped.classification");
		String template = conf.getString("report.unescaped.template");
		String desc = String.format(template, l.prettyString());
		return entry(l, desc, category, classification);
	}

	/**
	 * @return A report entry for the unexpected segment detection
	 */
	public  Entry unexpected(int line, String content) {
		String path = content.length() >= 3 ? content.substring(0, 3) : "";
		String category = conf.getString("report.unexpected.category");
		String classification = conf
				.getString("report.unexpected.classification");
		String template = conf.getString("report.unexpected.template");
		String desc = String.format(template, content);
		return entry(line, 1, path, desc, category, classification);
	}

	/**
	 * @return A report entry for the invalid content detection
	 */
	public  Entry invalid(int line, String content) {
		String category = conf.getString("report.invalid.category");
		String classification = conf.getString("report.invalid.classification");
		String template = conf.getString("report.invalid.template");
		String desc = String.format(template, content);
		return entry(line, 1, "", desc, category, classification);
	}

	/*
	 * ========================================================================
	 * Content related entries
	 * =======================================================================
	 */

	/**
	 * @return A report entry for a constraint failure detection
	 */

	public  Entry csFailure(Location errLoc, Element context,
			Constraint c, String message, List<Trace> stack, boolean cnt) {
		if (cnt)
			return cntFailure(errLoc, context, c, message, stack);
		else
			return csFailure(errLoc, context, c, message, stack);
	}

	public  Entry csFailure(Location errLoc, Element context,
			Constraint c, String message, List<Trace> stack) {
		return csEntry("constraint-failure", errLoc, context, c, message, stack);
	}

	public  Entry cntFailure(Location errLoc, Element context,
			Constraint c, String message, List<Trace> stack) {
		return csEntry("content-failure", errLoc, context, c, message, stack);
	}

	public  String constraintClassification(
			Option<Classification> classificationOption,
			Option<ConstraintStrength> strengthOption,
			String key){
		String root = conf.getString(key + ".classification");

		try {
			if(classificationOption.isDefined()) {
				Classification classification = classificationOption.get();
				if(classification instanceof Classification.W){
					return conf.getString("report.classification.warning");
				}
				else if(classification instanceof Classification.A){
					return conf.getString("report.classification.alert");
				}
				else
					return root;
			}

			if(strengthOption.isDefined()) {
				ConstraintStrength strength = strengthOption.get();
				if(strength instanceof ConstraintStrength.SHALL){
					return conf.getString(key + ".strength.shall");
				}
				else if(strength instanceof ConstraintStrength.SHOULD){
					return conf.getString(key + ".strength.should");
				}
				else
					return root;
			}

			return root;
		} catch (Exception e) {
			return root;
		}
	}
	
	public  Entry cntFailureCustom(Location errLoc, Element context,
			Constraint c, List<Trace> stack, String config, String val,
			String expected) {
		String category = conf.getString("report.content-failure.category");
		String classification = constraintClassification(c.classification(), c.strength(), "report.content-failure");
		String template = conf.getString("report.content-failure.template");
		Map<String, Object> metaData = new HashMap<String, Object>();
		if (c.reference().isDefined())
			metaData.put("reference", c.reference().get());

		if (conf.hasPath("context-based." + config)) {
			String description_template = conf.getString("context-based."
					+ config);
			String description = String.format(description_template, val,
					errLoc.prettyString(), expected);
			String str = String.format(template, c.id(), description);
			return entry(errLoc, str, category, classification, stack, metaData);
		} else {
			String str = String.format(template, c.id(), c.description());
			return entry(errLoc, str, category, classification, stack, metaData);
		}
	}

	/**
	 * @return A report entry for a constraint failure detection
	 */

	public  Entry csSuccess(Element context, Constraint c, boolean cnt) {

		if (cnt)
			return cntSuccess(context, c);
		else
			return csSuccess(context, c);
	}

	public  Entry csSuccess(Element context, Constraint c) {
		return csEntrySuccess("constraint-success", context.location(), context, c, null);
	}

	public  Entry cntSuccess(Element context, Constraint c) {
		return csEntrySuccess("content-success", context.location(), context, c, null);
	}
	/**
	 * @return A report entry for a constraint failure detection
	 */
	public  Entry csSpecError(Element context, Constraint c, String message,
			List<Trace> stack) {
		return csEntry("constraint-spec-error", context, c, message, stack);
	}

	public  Entry cntSpecError(Element context, Constraint c, String message,
			List<Trace> stack) {
		return csEntry("content-spec-error", context, c, message, stack);
	}

	public  Entry csSpecError(Element context, Constraint c, String message,
			List<Trace> stack, boolean cnt) {
		if (cnt)
			return cntSpecError(context, c, message, stack);
		else
			return csSpecError(context, c, message, stack);
	}
	
	public  Entry unresolvedField(String v1, String v2, Element e){
		String category = conf.getString("report.unresolved-field.category");
		String classification = conf.getString("report.unresolved-field.classification");
		String template = conf.getString("report.unresolved-field.template");
		Location l = e.location();
		String desc = String.format(template, v1, v2,l.prettyString());
		return entry(e.location(), desc, category, classification, null, null);
	}

	public  Entry coConstraintSuccess(Element e, String descr, Expression cond, Expression exp) {
		String category = conf.getString("report.coconstraint-success.category");
		String classification = conf.getString("report.coconstraint-success.classification");
		String template = conf.getString("report.coconstraint-success.template");
		String desc = String.format(template, AsString.condition(cond, e), AsString.expression(exp, e), "");
		return entry(e.location(), desc, category, classification);
	}

	public  Entry coConstraintFailure(Element e, String descr, Expression cond, Expression exp) {
		String category = conf.getString("report.coconstraint-failure.category");
		String classification = conf.getString("report.coconstraint-failure.classification");
		String template = conf.getString("report.coconstraint-failure.template");
		String desc = String.format(template, AsString.condition(cond, e), AsString.expression(exp, e), "");
		return entry(e.location(), desc, category, classification);
	}
	
	/**
	 * @return A report entry for a predicate failure detection
	 */
	public  Entry predicateSuccess(Element e, Predicate p) {
		String category = conf.getString("report.predicate-success.category");
		String classification = conf
				.getString("report.predicate-success.classification");
		String template = conf.getString("report.predicate-success.template");
		String desc = String.format(template, predicateAsString(p));
		return entry(e.location(), desc, category, classification);
	}

	/**
	 * @return A report entry for a predicate failure detection
	 */
	public  Entry predicateFailure(Location l, String usageErr,
			String expectedUsage, String predicateDesc) {
		String category = conf.getString("report.predicate-failure.category");
		String classification = conf
				.getString("report.predicate-failure.classification");
		String template = conf.getString("report.predicate-failure.template");
		String desc = String.format(template, usageErr, expectedUsage,
				predicateDesc);
		return entry(l, desc, category, classification);
	}

	/**
	 * @return A report entry for a predicate failure detection
	 */
	public  Entry predicateSpecErr(Element e, Predicate p,
			List<Trace> stack) {
		String category = conf
				.getString("report.predicate-spec-error.category");
		String classification = conf
				.getString("report.predicate-spec-error.classification");
		String template = conf
				.getString("report.predicate-spec-error.template");
		String desc = String.format(template, predicateAsString(p));
		return entry(e.location(), desc, category, classification, stack, null);
	}
	
	/*
	 * ========================================================================
	 * Content (Context-Based Order Indifferent) related entries
	 * =======================================================================
	 */
	
	public  Entry HLcontentErr(String message, Element e){
		String category = conf.getString("report.highlevel-content.category");
		String classification = conf.getString("report.highlevel-content.classification");
		return entry(e.location(), message, category, classification, null, null);
	}

	/*
	 * ========================================================================
	 * Value Set related entries
	 * =======================================================================
	 */
	/**
	 * @return A report entry for an excluded VS code detection
	 */
	public  Entry evs(Location l, String value, ValueSet vs,
			ValueSetSpec spec) {
		String desc = vsTemplate3("evs", value, l.prettyString(), vs.id());
		return vsEntry("evs", desc, l, vs, spec);
	}

	/**
	 * @return A report entry for a permitted VS code detection
	 */
	public  Entry pvs(Location l, String value, ValueSet vs,
			ValueSetSpec spec) {
		String desc = vsTemplate3("pvs", value, l.prettyString(), vs.id());
		return vsEntry("pvs", desc, l, vs, spec);
	}

	/**
	 * @return A report entry for a VS code not found detection
	 */
	public  Entry codeNotFound(Location l, String value, ValueSet vs,
			ValueSetSpec spec) {
		String desc = vsTemplate3("code-not-found", value, l.prettyString(),
				vs.id());
		return vsEntry("code-not-found", desc, l, vs, spec);
	}
	
	public  Entry codeNotFound(Location l, String value, String vs,
			ValueSetSpec spec) {
		String desc = vsTemplate3("code-not-found", value, l.prettyString(),
				vs);
		return vsEntry("code-not-found", desc, l, null, spec);
	}

	public  Entry codeNotFoundInOpen(Location l, String value, ValueSet vs,
							   ValueSetSpec spec) {
		String desc = vsTemplateOpenVS("code-not-found-open", value, l.prettyString(), vs.id(), vs.id());
		return vsEntry("code-not-found-open", desc, l, vs, spec);
	}

	public  Entry codeNotFoundInOpen(Location l, String value, String bindings, String open, ValueSetSpec spec) {
		String desc = vsTemplateOpenVS("code-not-found-open", value, l.prettyString(), bindings, open);
		return vsEntry("code-not-found-open", desc, l, null, spec);
	}

	/**
	 * @return A report entry for a VS not found detection
	 */
	public  Entry vsNotFound(Location l, String value, ValueSetSpec spec) {
		String desc = vsTemplate3("vs-not-found", value, l.prettyString(),
				spec.valueSetId());
		return vsEntry("vs-not-found", desc, l, null, spec);
	}

	/**
	 * @return A report entry for an empty VS detection
	 */
	public  Entry emptyVS(Location l, ValueSet vs, ValueSetSpec spec) {
		String desc = vsTemplate1("empty-vs", vs.id());
		return vsEntry("empty-vs", desc, l, vs, spec);
	}

	/**
	 * @return A report entry for a VS error detection
	 */
	public  Entry vsError(Location l, String msg, ValueSet vs,
			ValueSetSpec spec) {
		String desc = vsTemplate1("vs-error", msg);
		return vsEntry("vs-error", desc, l, vs, spec);
	}

	public  Entry bindingLocation(Location l, String msg, ValueSet vs,
			ValueSetSpec spec) {
		String desc = vsTemplate1("binding-location", msg);
		return vsEntry("binding-location", desc, l, vs, spec);
	}

	public  Entry vsError(Location l, String msg) {
		String desc = vsTemplate1("vs-error", msg);
		return vsEntry("vs-error", desc, l);
	}

	public  Entry bindingLocation(Location l, String msg) {
		String desc = vsTemplate1("binding-location", msg);
		return vsEntry("binding-location", desc, l);
	}

	/**
	 * @return A report entry for an excluded VS detection
	 */
	public  Entry vsNoVal(Location l, String vsID) {
		String desc = vsTemplate1("vs-no-validation", vsID);
		return vsEntry("vs-no-validation", desc, l, null, null);
	}

	/**
	 * @return A report entry for a coded Elem detection
	 */
	public  Entry codedElem(Location l, String msg, ValueSet vs,
			ValueSetSpec spec, List<Trace> stack) {

		return vsEntry("coded-element", msg, l, vs, spec, stack);
	}

	public  Entry codedElem(Location l, String msg, List<Trace> stack) {

		return vsEntry("coded-element", msg, l, stack);
	}

	/*
	 * ========================================================================
	 * Helpers
	 * =======================================================================
	 */
	
	public  Entry toAlert(Entry e){
		if(e instanceof TripletEntry){
			return ((TripletEntry) e).toAlert();
		}
    	String classification = conf.getString("report.classification.alert");
    	return new EntryImpl(e.getLine(), e.getColumn(), e.getPath(), e.getDescription(), e.getCategory(),
				classification, e.getStackTrace(), e.getMetaData());
    }
	
	private  Entry entry(int line, int column, String path,
			String description, String category, String classification,
			List<Trace> stackTrace, Map<String, Object> metaData) {
		return new EntryImpl(line, column, path, description, category,
				classification, stackTrace, metaData);
	}

	private  Entry entry(int line, int column, String path,
			String description, String category, String classification) {
		return entry(line, column, path, description, category, classification,
				null, null);
	}

	private  Entry entry(Location l, String description, String category,
			String classification) {
		int line = l.line();
		int column = l.column();
		String path = l.uidPath();
		return entry(line, column, path, description, category, classification);
	}

	private  Entry entry(Location l, String description, String category,
			String classification, List<Trace> stackTrace,
			Map<String, Object> metaData) {
		int line = l.line();
		int column = l.column();
		String path = l.uidPath();
		return entry(line, column, path, description, category, classification,
				stackTrace, metaData);
	}


	private  Entry csEntrySuccess(String configKey, Location errLoc,
			Element context, Constraint c, List<Trace> stack) {
		String category = conf.getString("report." + configKey + ".category");
		String classification = conf.getString("report." + configKey + ".classification");
		String template = conf.getString("report." + configKey + ".template");
		String desc = String.format(template, c.id(), c.description());
		Map<String, Object> metaData = new HashMap<String, Object>();
		if (c.reference().isDefined())
			metaData.put("reference", c.reference().get());
		return entry(errLoc, desc, category, classification, stack, metaData);
	}

	private  Entry csEntry(String configKey, Location errLoc,
						   Element context, Constraint c, String message, List<Trace> stack) {
		String category = conf.getString("report." + configKey + ".category");
		String classification = constraintClassification(c.classification(), c.strength(), "report." + configKey);
		String template = conf.getString("report." + configKey + ".template");
		String desc = String.format(template, c.id(), message);
		Map<String, Object> metaData = new HashMap<String, Object>();
		if (c.reference().isDefined())
			metaData.put("reference", c.reference().get());
		return entry(errLoc, desc, category, classification, stack, metaData);
	}

	private  Entry csEntry(String configKey, Element context,
			Constraint c, String message, List<Trace> stack) {
		return csEntry(configKey, context.location(), context, c, message, stack);
	}

	private  Entry vsEntry(String configKey, String desc, Location l,
			ValueSet vs, ValueSetSpec spec) {
		return vsEntry(configKey, desc, l, vs, spec, null);
	}

	private  Entry vsEntry(String configKey, String desc, Location l) {
		return vsEntry(configKey, desc, l, null);
	}

	private  Entry vsEntry(String configKey, String desc, Location l,
			ValueSet vs, ValueSetSpec spec, List<Trace> stack) {
		String category = conf.getString("report." + configKey + ".category");
		String classification = conf.getString("report." + configKey
				+ ".classification");
		Map<String, Object> metaData = new HashMap<String, Object>();
		metaData.put("valueSet", new ValueSetDetails(vs, spec));
		return entry(l, desc, category, classification, stack, metaData);
	}

	private  Entry vsEntry(String configKey, String desc, Location l,
			List<Trace> stack) {
		String category = conf.getString("report." + configKey + ".category");
		String classification = conf.getString("report." + configKey
				+ ".classification");
		Map<String, Object> metaData = new HashMap<String, Object>();
		return entry(l, desc, category, classification, stack, metaData);
	}

	private  String predicateAsString(Predicate p) {
		return String.format("Predicate C(%s/%s) target: %s description: %s",
				p.trueUsage(), p.falseUsage(), p.target(), p.description());
	}

	private  String vsTemplate1(String configKey, String s1) {
		String template = conf.getString("report." + configKey + ".template");
		return String.format(template, s1);
	}

	private  String vsTemplate3(String configKey, String s1, String s2,
			String s3) {
		String template = conf.getString("report." + configKey + ".template");
		return String.format(template, s1, s2, s3);
	}

	private  String vsTemplateOpenVS(String configKey, String s1, String s2, String s3, String s4) {
		String template = conf.getString("report." + configKey + ".template");
		return String.format(template, s1, s2, s3, s4);
	}

	 class ValueSetDetails {

		private String id;
		private String stability;
		private String extensibility;
		private String bindingStrength;
		private String bindingLocation;

		public ValueSetDetails(ValueSet vs, ValueSetSpec spec) {
			if (vs != null) {
				this.id = vs.id();
				this.stability = optionAsString(vs.stability());
				this.extensibility = optionAsString(vs.extensibility());
			}
			if (spec != null) {
				this.id = spec.valueSetId(); // Should be the same value as
												// above
				this.bindingStrength = optionAsString(spec.bindingStrength());
				this.bindingLocation = optionAsString(spec.bindingLocation());
			}

		}

		public String getId() {
			return id;
		}

		public String getStability() {
			return stability;
		}

		public String getExtensibility() {
			return extensibility;
		}

		public String getBindingStrength() {
			return bindingStrength;
		}

		public String getBindingLocation() {
			return bindingLocation;
		}

		public  <T> String optionAsString(scala.Option<T> o) {
			try {
				return o.get().toString();
			} catch (Exception e) {
				return null;
			}
		}
	}

}
