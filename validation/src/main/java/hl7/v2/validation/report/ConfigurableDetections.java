package hl7.v2.validation.report;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import expression.AsString;
import expression.Expression;
import gov.nist.validation.report.Entry;
import gov.nist.validation.report.Trace;
import gov.nist.validation.report.impl.EntryImpl;
import hl7.v2.instance.Element;
import hl7.v2.instance.Location;
import hl7.v2.instance.Simple;
import hl7.v2.profile.BindingStrength;
import hl7.v2.profile.Range;
import hl7.v2.profile.ValueSetSpec;
import hl7.v2.validation.content.*;
import hl7.v2.validation.vs.Extensibility;
import hl7.v2.validation.vs.Stability;
import hl7.v2.validation.vs.TripletEntry;
import hl7.v2.validation.vs.ValueSet;
import scala.Option;

public class ConfigurableDetections {
	
	
	Config conf = ConfigFactory.load();
	/*
	 * ========================================================================
	 * Value Set Validation related entries
	 * =======================================================================
	 */
	
	public  Entry rvs(Location l, String value, String vs) {
		String desc = vsTemplate3("rvs", value, l.prettyString(), vs);
		return vsEntry("rvs", desc, l, null, null);
	}
	
	public  Entry pvs(Location l, String value, String vs) {
		String desc = vsTemplate3("pvs", value, l.prettyString(), vs);
		return vsEntry("pvs", desc, l, null, null);
	}

	public Entry vsCodeFound(String key, Location l, String value, String vs, Stability stab, Extensibility ext, BindingStrength strength) {
		return vsCodeFound(key, l, value, vs, stab, ext, strength, false, "", "");
	}

	public Entry vsCodeFound(String key, Location l, String value, String vs, Stability stab, Extensibility ext, BindingStrength strength, boolean isCodePattern, String code, String pattern) {
		String extKey = extensibility(ext);
		String strKey = bindingStrength(strength);
		String staKey = stability(stab);

		SimpleEntryWrapper defaultEntryConfig = new SimpleEntryWrapper(
				conf.getString("report." + key + ".category"),
				conf.getString("report." + key + ".classification"),
				conf.getString("report." + key + ".template")
		);

		SimpleEntryWrapper effectiveEntryConfig = this.vsDetectionInfo(
				defaultEntryConfig,
				staKey,
				extKey,
				strKey,
				key
		);

		String desc = String.format(
				effectiveEntryConfig.getTemplate(),
				value,
				l.prettyString(),
				vs, isCodePattern ? "; Matched from code pattern '" + code + "' (regex = " + pattern + ")" : ""
		);

		return entry(l, desc, effectiveEntryConfig.getCategory(), effectiveEntryConfig.getClassification(), null, null);
	}

	public Entry duplicateCode(Location l, String value, String vs) {
		String desc = vsTemplate3("duplicate-code", value, l.prettyString(), vs);
		String classification = conf.getString("report." + "duplicate-code" + ".classification");
		String category = conf.getString("report." + "duplicate-code" + ".category");

		return entry(l, desc, category, classification, null, null);
	}

	public Entry duplicateCodeAndCodeSystem(Location l, String value, String codeSystem, String vs) {
		String desc = vsTemplate4("duplicate-code-codesystem", value, codeSystem, l.prettyString(), vs);
		String classification = conf.getString("report." + "duplicate-code-codesystem" + ".classification");
		String category = conf.getString("report." + "duplicate-code-codesystem" + ".category");

		return entry(l, desc, category, classification, null, null);
	}


	public  Entry codeNotFound(Location l, String value, String vs, Stability stab, Extensibility ext, BindingStrength strength) {
		String extKey = extensibility(ext);
		String strKey = bindingStrength(strength);
		String stbKey = stability(stab);

		SimpleEntryWrapper defaultEntryConfig = new SimpleEntryWrapper(
				conf.getString("report.code-not-found-simple.category"),
				conf.getString("report.code-not-found-simple.classification"),
				conf.getString("report.code-not-found-simple.template")
		);

		SimpleEntryWrapper effectiveEntryConfig = this.vsDetectionInfo(
				defaultEntryConfig,
				stbKey,
				extKey,
				strKey,
				"code-not-found-simple"
		);

		String desc = String.format(
				effectiveEntryConfig.getTemplate(),
				value,
				l.prettyString(),
				vs
		);

		return entry(l, desc, effectiveEntryConfig.getCategory(), effectiveEntryConfig.getClassification(), null, null);
	}

	public  Entry codeNotFoundCodedElement(Location l, String code, String codeSystem, String vs, Stability stab, Extensibility ext, BindingStrength strength) {
		String extKey = extensibility(ext);
		String strKey = bindingStrength(strength);
		String stbKey = stability(stab);

		SimpleEntryWrapper defaultEntryConfig = new SimpleEntryWrapper(
				conf.getString("report.code-not-found-coded-element.category"),
				conf.getString("report.code-not-found-coded-element.classification"),
				conf.getString("report.code-not-found-coded-element.template")
		);

		SimpleEntryWrapper effectiveEntryConfig = this.vsDetectionInfo(
				defaultEntryConfig,
				stbKey,
				extKey,
				strKey,
				"code-not-found-coded-element"
		);

		String desc = String.format(
				effectiveEntryConfig.getTemplate(),
				code,
				codeSystem,
				l.prettyString(),
				vs
		);

		return entry(l, desc, effectiveEntryConfig.getCategory(), effectiveEntryConfig.getClassification(), null, null);
	}

	public  Entry codeNotFoundCsNotPopulated(Location l, String value, String cs, String csLocation, String vs, Stability stab, Extensibility ext, BindingStrength strength) {
		String extKey = extensibility(ext);
		String strKey = bindingStrength(strength);
		String stbKey = stability(stab);

		SimpleEntryWrapper defaultEntryConfig = new SimpleEntryWrapper(
				conf.getString("report.code-not-found-cs-empty.category"),
				conf.getString("report.code-not-found-cs-empty.classification"),
				conf.getString("report.code-not-found-cs-empty.template")
		);

		SimpleEntryWrapper effectiveEntryConfig = this.vsDetectionInfo(
				defaultEntryConfig,
				stbKey,
				extKey,
				strKey,
				"code-not-found-cs-empty"
		);

		String desc = String.format(
				effectiveEntryConfig.getTemplate(),
				value,
				l.prettyString(),
				vs,
				cs,
				csLocation
		);

		return entry(l, desc, effectiveEntryConfig.getCategory(), effectiveEntryConfig.getClassification(), null, null);
	}

	public  Entry codeNotFoundInvalidCodeSystem(Location l, String value, String vs, String expected, String found, Stability stab, Extensibility ext, BindingStrength strength) {
		String extKey = extensibility(ext);
		String strKey = bindingStrength(strength);
		String stbKey = stability(stab);

		SimpleEntryWrapper defaultEntryConfig = new SimpleEntryWrapper(
				conf.getString("report.code-not-found-cs.category"),
				conf.getString("report.code-not-found-cs.classification"),
				conf.getString("report.code-not-found-cs.template")
		);

		SimpleEntryWrapper effectiveEntryConfig = this.vsDetectionInfo(
				defaultEntryConfig,
				stbKey,
				extKey,
				strKey,
				"code-not-found-cs"
		);

		String desc = String.format(
				effectiveEntryConfig.getTemplate(),
				value, found, l.prettyString(), vs, expected
		);

		return entry(l, desc, effectiveEntryConfig.getCategory(), effectiveEntryConfig.getClassification(), null, null);
	}

	public Entry codedElementXOR(Location l, String locations, String vs, Stability stab, Extensibility ext, BindingStrength strength) {
		String extKey = extensibility(ext);
		String strKey = bindingStrength(strength);
		String stbKey = stability(stab);

		SimpleEntryWrapper defaultEntryConfig = new SimpleEntryWrapper(
				conf.getString("report.coded-element-xor.category"),
				conf.getString("report.coded-element-xor.classification"),
				conf.getString("report.coded-element-xor.template")
		);

		SimpleEntryWrapper effectiveEntryConfig = this.vsDetectionInfo(
				defaultEntryConfig,
				stbKey,
				extKey,
				strKey,
				"coded-element-xor"
		);

		String desc = String.format(
				effectiveEntryConfig.getTemplate(),
				locations, vs
		);

		return entry(l, desc, effectiveEntryConfig.getCategory(), effectiveEntryConfig.getClassification(), null, null);
	}

	public Entry singleCodeMultiLocationXOR(String expectedCode, String expectedCs, List<Element> locations, Element context) {
		String category = conf.getString("report.single-code-xor.category");
		String template = conf.getString("report.single-code-xor.template");
		String classification = conf.getString("report.single-code-xor.classification");
		String locationsDefs = locations.stream().map(l -> l.location().path()).collect(Collectors.joining(", ", ": [", "]"));
		String desc = String.format(
				template,
				expectedCode,
				expectedCs,
				locationsDefs,
				context.location().prettyString()
		);
		return entry(context.location(), desc, category, classification, null, null);
	}

	public Entry singleCodeInvalidCodeSystem(String expectedCode, String expectedCs, Simple code, Simple codeSystem, Element context) {
		String category = conf.getString("report.single-code-cs-invalid.category");
		String template = conf.getString("report.single-code-cs-invalid.template");
		String classification = conf.getString("report.single-code-cs-invalid.classification");
		String desc = String.format(
				template,
				expectedCode,
				expectedCs,
				code.location().prettyString(),
				codeSystem.value().raw(),
				codeSystem.location().prettyString(),
				expectedCs
		);
		return entry(context.location(), desc, category, classification, null, null);
	}

	public Entry singleCodeCSNotFound(String expectedCode, String expectedCs, Simple code, String csLocation, Element context) {
		String category = conf.getString("report.single-code-cs-not-found.category");
		String template = conf.getString("report.single-code-cs-not-found.template");
		String classification = conf.getString("report.single-code-cs-not-found.classification");
		String desc = String.format(
				template,
				expectedCode,
				expectedCs,
				code.location().prettyString(),
				csLocation
		);
		return entry(context.location(), desc, category, classification, null, null);
	}

	public Entry singleCodeCodeNotFound(String expectedCode, String expectedCs, Simple code, Element context) {
		String category = conf.getString("report.single-code-not-found.category");
		String template = conf.getString("report.single-code-not-found.template");
		String classification = conf.getString("report.single-code-not-found.classification");
		String desc = String.format(
				template,
				expectedCode,
				expectedCs,
				code.value().raw(),
				code.location().prettyString(),
				expectedCode
		);
		return entry(context.location(), desc, category, classification, null, null);
	}

	public Entry legacy0396(Element context) {
		String category = conf.getString("report.legacy-0396.category");
		String template = conf.getString("report.legacy-0396.template");
		String classification = conf.getString("report.legacy-0396.classification");
		return entry(context.location(), template, category, classification, null, null);
	}

	public Entry singleCodeSuccess(String expectedCode, String expectedCs, Simple code, Element context) {
		String category = conf.getString("report.single-code-success.category");
		String template = conf.getString("report.single-code-success.template");
		String classification = conf.getString("report.single-code-success.classification");
		String desc = String.format(
				template,
				expectedCode,
				expectedCs,
				code.location().prettyString()
		);
		return entry(context.location(), desc, category, classification, null, null);
	}

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

	public  Entry oUsageComplex(Location l) {
		String category = conf.getString("report.o-usage-complex.category");
		String classification = conf.getString("report.o-usage-complex.classification");
		String template = conf.getString("report.o-usage-complex.template");
		String desc = String.format(template, l.prettyString());
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

	/**
	 * @return A report entry for the constant value detection
	 */
	public  Entry constantValue(Location l, String constant, String value) {
		String category = conf.getString("report.constant-value.category");
		String classification = conf.getString("report.constant-value.classification");
		String template = conf.getString("report.constant-value.template");
		String desc = String.format(template, l.prettyString(), constant, value);
		return entry(l, desc, category, classification);
	}

	/**
	 * @return A report entry for the constant value detection
	 */
	public  Entry constantValueSpecError(Location l, String constant) {
		String category = conf.getString("report.constant-value-spec-error.category");
		String classification = conf.getString("report.constant-value-spec-error.classification");
		String template = conf.getString("report.constant-value-spec-error.template");
		String desc = String.format(template, l.prettyString(), constant);
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

	public  Entry dynMappingNote(Location l, String match, String references) {
		String category = conf.getString("report.dynamic-mapping-match.category");
		String classification = conf.getString("report.dynamic-mapping-match.classification");
		String template = conf.getString("report.dynamic-mapping-match.template");
		String desc = String.format(template, l.prettyString(), match, references);
		return entry(l, desc, category, classification);
	}

	public  Entry predicateUsageNote(Location l, String usage, String trueUsage, String falseUsage, String condition, boolean eval) {
		String category = conf.getString("report.predicate-usage-selection.category");
		String classification = conf.getString("report.predicate-usage-selection.classification");
		String template = conf.getString("report.predicate-usage-selection.template");
		String desc = String.format(template, usage, l.prettyString(), trueUsage, falseUsage, condition, eval ? "True" : "False");
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

	
	public String bindingStrength(BindingStrength strength){
		if(strength == null) return "no-value";
		if(strength instanceof BindingStrength.R$) return "required";
		if(strength instanceof BindingStrength.S$) return "suggested";
		return "no-value";
	}
	
	public String stability(Stability stability){
		if(stability == null) return "no-value";
		if(stability instanceof Stability.Static$) return "static";
		if(stability instanceof Stability.Dynamic$) return "dynamic";
		return "no-value";
	}
	
	public String extensibility(Extensibility ext){
		return ext == null ? "no-value" : ext instanceof Extensibility.Closed$ ? "closed" : "open";
	}

	public SimpleEntryWrapper vsDetectionInfo(SimpleEntryWrapper entry, String stability, String extensibility, String strength, String key) {
		SimpleEntryWrapper cursor = entry;
		if(conf.hasPath("report." + key + ".overrides")) {
			StringBuilder configPath = new StringBuilder("report.").append(key).append(".overrides");
			for(String step: Arrays.asList(strength, extensibility, stability)) {
				if(conf.hasPath(configPath + ".default")) {
					cursor = overrideEntryConfig(cursor, configPath + ".default");
				}
				if(conf.hasPath(configPath + "." + step)) {
					configPath.append(".").append(step);
				} else {
					return cursor;
				}
			}

			return overrideEntryConfig(cursor, configPath.toString());
		} else {
			return cursor;
		}
	}

	public SimpleEntryWrapper overrideEntryConfig(SimpleEntryWrapper entry, String key) {
		String classification = key + ".classification";
		String template = key + ".template";
		String category = key + ".category";

		return new SimpleEntryWrapper(
				conf.hasPath(category) ? conf.getString(category) : entry.getCategory(),
				conf.hasPath(classification) ? conf.getString(classification) : entry.getClassification(),
				conf.hasPath(template) ? conf.getString(template) : entry.getTemplate()
		);
	}

	public boolean stringIsDefined(String key) {
		return key != null && !key.isEmpty();
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

	public  Entry ubs(Location l, String valueSets) {
		String desc = vsTemplate2("ubs", valueSets, l.prettyString());
		return vsEntry("ubs", desc, l, null, null);
	}

	public  Entry multiVsSimple(Location l, String valueSets) {
		String desc = vsTemplate2("multi-vs-simple", valueSets, l.prettyString());
		return vsEntry("multi-vs-simple", desc, l, null, null);
	}

	public  Entry incompatibleUsageAndExtensibility(Location l, String value, String valueSet) {
		String desc = vsTemplate3("usage-and-extensibility", value, l.prettyString(), valueSet);
		return vsEntry("usage-and-extensibility", desc, l, null, null);
	}

	public Entry dynamicVs(Location l, String valueSet) {
		String desc = vsTemplate2("dynamic-vs", valueSet, l.prettyString());
		return vsEntry("dynamic-vs", desc, l, null, null);
	}

	/**
	 * @return A report entry for a VS not found detection
	 */

	public  Entry vsNotFound(Location l, String vId) {
		String desc = vsTemplate2("vs-not-found-binding", vId, l.prettyString());
		return vsEntry("vs-not-found-binding", desc, l, null, null);
	}

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
	public  Entry codedElemXOR(Location l, String msg, ValueSet vs, ValueSetSpec spec, List<Trace> stack) {

		return vsEntry("coded-element", msg, l, vs, spec, stack);
	}

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

	private  String vsTemplate2(String configKey, String s1, String s2) {
		String template = conf.getString("report." + configKey + ".template");
		return String.format(template, s1, s2);
	}

	private  String vsTemplate3(String configKey, String s1, String s2,
			String s3) {
		String template = conf.getString("report." + configKey + ".template");
		return String.format(template, s1, s2, s3);
	}

	private  String vsTemplate4(String configKey, String s1, String s2, String s3, String s4) {
		String template = conf.getString("report." + configKey + ".template");
		return String.format(template, s1, s2, s3, s4);
	}
	
	private  String vsTemplate5(String configKey, String s1, String s2,
			String s3, String s4, String s5) {
		String template = conf.getString("report." + configKey + ".template");
		return String.format(template, s1, s2, s3, s4, s5);
	}

	public Entry ccRequiredGroup(Element e, String group, String cc) {
		String category = conf.getString("report.required-group.category");
		String classification = conf.getString("report.required-group.classification");
		String template = conf.getString("report.required-group.template");
		String description = String.format(template, group, group, cc);
		return entry(e.location(), description, category, classification);
	}

	public Entry ccRequiredCoConstraint(String groupInstance, Element e, String cc) {
		String category = conf.getString("report.required-co-constraint.category");
		String classification = conf.getString("report.required-co-constraint.classification");
		String template = conf.getString("report.required-co-constraint.template");
		String description = String.format(template, groupInstance, cc);
		return entry(e.location(), description, category, classification);
	}

	public Entry ccCardinalityGroup(Element e, String group, Range r, int nb, String grouper, String values) {
		String category = conf.getString("report.cardinality-group.category");
		String classification = conf.getString("report.cardinality-group.classification");
		String template = conf.getString("report.cardinality-group.template");
		String description = String.format(template, group, r.min(), r.max(), nb, grouper, values);
		return entry(e.location(), description, category, classification);
	}

	public Entry ccCardinalityCoConstraint(String groupInstance, Element e, String cc, Range r, int nb) {
		String category = conf.getString("report.cardinality-co-constraint.category");
		String classification = conf.getString("report.cardinality-co-constraint.classification");
		String template = conf.getString("report.cardinality-co-constraint.template");
		String description = String.format(template, groupInstance, cc, r.min(), r.max(), nb);
		return entry(e.location(), description, category, classification);
	}

	public Entry ccElementIsNotSegment(Element e, String path, Element context) {
		String category = conf.getString("report.cc-element-not-segment.category");
		String classification = conf.getString("report.cc-element-not-segment.classification");
		String template = conf.getString("report.cc-element-not-segment.template");
		String description = String.format(template, path, context.location().prettyString(), e.location().prettyString());
		return entry(e.location(), description, category, classification);
	}

	public Entry ccElementIsNotGroupOrMessage(Element e, String path, Element context) {
		String category = conf.getString("report.cc-context-not-group-or-message.category");
		String classification = conf.getString("report.cc-context-not-group-or-message.classification");
		String template = conf.getString("report.cc-context-not-group-or-message.template");
		String description = String.format(template, path, context.location().prettyString(), e.location().prettyString());
		return entry(e.location(), description, category, classification);
	}

	public Entry ccTargetSpecError(Element e, String path, String reason) {
		String category = conf.getString("report.cc-target-spec-error.category");
		String classification = conf.getString("report.cc-target-spec-error.classification");
		String template = conf.getString("report.cc-target-spec-error.template");
		String description = String.format(template, path, reason);
		return entry(e.location(), description, category, classification);
	}

	public Entry ccContextSpecError(Element e, String path, String reason) {
		String category = conf.getString("report.cc-context-spec-error.category");
		String classification = conf.getString("report.cc-context-spec-error.classification");
		String template = conf.getString("report.cc-context-spec-error.template");
		String description = String.format(template, path, reason);
		return entry(e.location(), description, category, classification);
	}

	public Entry ccGrouperSpecError(Element e, String path, String reason) {
		String category = conf.getString("report.cc-grouper-spec-error.category");
		String classification = conf.getString("report.cc-grouper-spec-error.classification");
		String template = conf.getString("report.cc-grouper-spec-error.template");
		String description = String.format(template, path, reason);
		return entry(e.location(), description, category, classification);
	}

	public Entry ccNotDistinctGrouper(Element e, String cc, String value, String context) {
		String category = conf.getString("report.cc-grouper-not-distinct.category");
		String classification = conf.getString("report.cc-grouper-not-distinct.classification");
		String template = conf.getString("report.cc-grouper-not-distinct.template");
		String description = String.format(template, cc, e.location().prettyString(), value, context);
		return entry(e.location(), description, category, classification);
	}

	public Entry ccSpecError(Location location, String cell, String message) {
		String category = conf.getString("report.cc-spec-error.category");
		String classification = conf.getString("report.cc-spec-error.classification");
		String template = conf.getString("report.cc-spec-error.template");
		String description = String.format(template, cell, location.prettyString(), message);
		return entry(location, description, category, classification);
	}

	public Entry ccBindingSpecError(Location location, String cell) {
		String category = conf.getString("report.cc-binding-spec-error.category");
		String classification = conf.getString("report.cc-binding-spec-error.classification");
		String template = conf.getString("report.cc-binding-spec-error.template");
		String description = String.format(template, cell, location.prettyString());
		return entry(location, description, category, classification);
	}

	public Entry ccConditionSpecError(Location location, String condition, List<Trace> stack) {
		String category = conf.getString("report.cc-condition-spec-error.category");
		String classification = conf.getString("report.cc-condition-spec-error.classification");
		String template = conf.getString("report.cc-condition-spec-error.template");
		String description = String.format(template, condition);
		return entry(location, description, category, classification, stack, null);
	}

	public Entry ccPlainTextFailure(String groupInstance, Element e, String cc, String cell, String found) {
		String category = conf.getString("report.cc-plain-text-failure.category");
		String classification = conf.getString("report.cc-plain-text-failure.classification");
		String template = conf.getString("report.cc-plain-text-failure.template");
		String description = String.format(template, groupInstance, cc, cell, found);
		return entry(e.location(), description, category, classification);
	}

	public Entry ccCodeFailure(String groupInstance, Element e, String cc, String cell, String codeFound, String codeExpected) {
		String category = conf.getString("report.cc-code-failure.category");
		String classification = conf.getString("report.cc-code-failure.classification");
		String template = conf.getString("report.cc-code-failure.template");
		String description = String.format(template, groupInstance, cc, cell, codeFound, codeExpected);
		return entry(e.location(), description, category, classification);
	}

	public Entry ccCodeFailureInvalidCs(String groupInstance, Element e, String cc, String cell, String cs, String csExpected) {
		String category = conf.getString("report.cc-code-failure-invalid-codesys.category");
		String classification = conf.getString("report.cc-code-failure-invalid-codesys.classification");
		String template = conf.getString("report.cc-code-failure-invalid-codesys.template");
		String description = String.format(template, groupInstance, cc, cell, cs, csExpected);
		return entry(e.location(), description, category, classification);
	}

	public Entry ccCodeFailureNotFoundCs(String groupInstance, Element e, String cc, String cell, String csLocation) {
		String category = conf.getString("report.cc-code-failure-notfound-codesys.category");
		String classification = conf.getString("report.cc-code-failure-notfound-codesys.classification");
		String template = conf.getString("report.cc-code-failure-notfound-codesys.template");
		String description = String.format(template, groupInstance, cc, cell, csLocation);
		return entry(e.location(), description, category, classification);
	}

	public Entry ccVsFailure(String groupInstance, Element e, String cc, String cell) {
		String category = conf.getString("report.cc-vs-failure.category");
		String classification = conf.getString("report.cc-vs-failure.classification");
		String template = conf.getString("report.cc-vs-failure.template");
		String description = String.format(template, groupInstance, cc, cell);
		return entry(e.location(), description, category, classification);
	}

	public Entry ccCellSuccess(String groupInstance, Element e, String cc, String cell) {
		String category = conf.getString("report.cc-success.category");
		String classification = conf.getString("report.cc-success.classification");
		String template = conf.getString("report.cc-success.template");
		String description = String.format(template, groupInstance, cc, cell);
		return entry(e.location(), description, category, classification);
	}

	// Slicing Detections

	public Entry assertionSlicingMatch(Element e, String assertion, String type, String ref) {
		String category = conf.getString("report.assertion-slicing-match.category");
		String classification = conf.getString("report.assertion-slicing-match.classification");
		String template = conf.getString("report.assertion-slicing-match.template");
		String description = String.format(template, assertion, e.location().prettyString(), type, ref);
		return entry(e.location(), description, category, classification);
	}

	public Entry occurrenceSlicingMatch(Element e, int occurrence, String type, String ref) {
		String category = conf.getString("report.occurrence-slicing-match.category");
		String classification = conf.getString("report.occurrence-slicing-match.classification");
		String template = conf.getString("report.occurrence-slicing-match.template");
		String description = String.format(template, occurrence, e.location().prettyString(), type, ref);
		return entry(e.location(), description, category, classification);
	}

	public Entry slicingNoMatch(Element e, String flavorName) {
		String category = conf.getString("report.slicing-no-match.category");
		String classification = conf.getString("report.slicing-no-match.classification");
		String template = conf.getString("report.slicing-no-match.template");
		String description = String.format(template, e.location().prettyString(), flavorName);
		return entry(e.location(), description, category, classification);
	}

	public Entry assertionSlicingInconclusive(Element e, String assertion, String type, String ref, String reason) {
		String category = conf.getString("report.assertion-slicing-inconclusive.category");
		String classification = conf.getString("report.assertion-slicing-inconclusive.classification");
		String template = conf.getString("report.assertion-slicing-inconclusive.template");
		String description = String.format(template, assertion, type, ref, e.location().prettyString(), reason);
		return entry(e.location(), description, category, classification);
	}

	public Entry slicingTargetGroup(Element e) {
		String category = conf.getString("report.slicing-target-group.category");
		String classification = conf.getString("report.slicing-target-group.classification");
		String template = conf.getString("report.slicing-target-group.template");
		String description = String.format(template, e.location().prettyString());
		return entry(e.location(), description, category, classification);
	}

	public Entry slicingMatchError(Element e, String type, String ref, String reason) {
		String category = conf.getString("report.slicing-match-error.category");
		String classification = conf.getString("report.slicing-match-error.classification");
		String template = conf.getString("report.slicing-match-error.template");
		String description = String.format(template, type, ref, e.location().prettyString(), reason);
		return entry(e.location(), description, category, classification);
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
				this.id = spec.valueSetId(); // Should be the same value as above
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
